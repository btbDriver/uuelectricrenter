package com.youyou.uuelectric.renter.Utils.andfix;

import android.content.Context;
import android.text.TextUtils;

import com.alipay.euler.andfix.patch.PatchManager;
import com.android.volley.VolleyError;
import com.google.protobuf.ByteString;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.ext.protobuf.bean.ExtInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.VersionUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by liuchao on 2016/3/7.
 * 主要用于实现热修复逻辑
 *   采用阿里巴巴开源框架-andfix
 *
 */
public class AndfixManager {
    public static final String TAG = AndfixManager.class.getSimpleName();

    // AndfixManager单例对象
    private static AndfixManager instance = null;
    // 补丁文件名称
    public static final String PATCH_FILENAME = "/patchname.apatch";

    public static PatchManager patchManager = null;
    private AndfixManager() {}

    /**
     * 线程安全之懒汉模式实现单例模型
     * @return
     */
    public static synchronized AndfixManager getInstance() {
        return instance == null ? new AndfixManager() : instance;
    }

    /**
     * 执行andfix初始化操作
     */
    public static void init(Context mContext) {
        if (mContext == null) {
            L.i("初始化热修复框架，参数错误！！！");
            return;
        }
        L.i("开始执行AndFix热修复初始化操作...");
        patchManager = new PatchManager(mContext);
        // 初始化patch版本，这里初始化的是当前的App版本；
        patchManager.init(VersionUtils.getVersionName(mContext));
        // 加载已经添加到PatchManager中的patch
        patchManager.loadPatch();


        downLoadAndAndPath(mContext);

    }

    /**
     * 请求服务器获取补丁文件并加载
     */
    public static void downLoadAndAndPath(final Context mContext) {
        L.i("开始执行请求服务器,判断是否有热更新包...");
        // 请求服务器获取差异包
        ExtInterface.GetShContent.Request.Builder request = ExtInterface.GetShContent.Request.newBuilder();

        // 获取本地保存的补丁包版本号
        final String patchVersion = AndfixSp.getPatchVersion(mContext);
        L.i("patchVersion:" + patchVersion);
        if (!TextUtils.isEmpty(patchVersion)) {
            request.setShVersion(patchVersion);
        } else {
            request.setShVersion("0");
        }
        NetworkTask task = new NetworkTask(Cmd.CmdCode.GetShContent_SSL_VALUE);
        task.setBusiData(request.build().toByteArray());
        NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
            @Override
            public void onSuccessResponse(UUResponseData responseData) {
                if (responseData.getRet() == 0) {
                    try {
                        ExtInterface.GetShContent.Response response = ExtInterface.GetShContent.Response.parseFrom(responseData.getBusiData());
                        // 若返回成功，则更新脚本下载补丁包
                        L.i("热更新请求返回ret:" + response.getRet());
                        if (response.getRet() == 0) {
                            ByteString zipDatas = response.getContent();
                            // 数据解压缩
                            byte[] oriDatas = GZipUtils.decompress(zipDatas.toByteArray());
                            String patchFileName = mContext.getCacheDir() + PATCH_FILENAME;
                            L.i("patchFileName:" + patchFileName);
                            // 将byte数组数据写入文件
                            boolean boolResult = getFileFromBytes(patchFileName, oriDatas);
                            // 写入文件成功则加载
                            if (boolResult) {
                                patchManager.removeAllPatch();
                                patchManager.addPatch(patchFileName);
                                L.i("开始执行加载热更新包的操作...");
                                // 保存补丁版本号
                                AndfixSp.putPatchVersion(mContext, response.getShVersion());
                                // 删除补丁文件
                                File files = new File(patchFileName);
                                if (files.exists()) {
                                    files.delete();
                                }
                            }

                        } else {
                            // -1 请求失败
                            // 1 请求成功，但是没有更新版本的脚本
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(VolleyError errorResponse) {
                L.i("热更新请求失败,执行onError方法...");
            }

            @Override
            public void networkFinish() {
            }
        });

    }


    /**
     * 根据数组获取文件
     * @param path
     * @param oriDatas
     */
    public static boolean getFileFromBytes(String path, byte[] oriDatas) {
        boolean result = false;
        if (TextUtils.isEmpty(path)) {
            return result;
        }
        if (oriDatas == null || oriDatas.length == 0) {
            return result;
        }

        try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(oriDatas);
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
