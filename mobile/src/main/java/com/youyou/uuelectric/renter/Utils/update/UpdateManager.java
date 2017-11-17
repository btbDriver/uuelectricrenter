package com.youyou.uuelectric.renter.Utils.update;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;
import com.uu.facade.base.cmd.Cmd;
import com.uu.facade.ext.protobuf.bean.ExtInterface;
import com.youyou.uuelectric.renter.Network.HttpResponse;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.NetworkUtils;
import com.youyou.uuelectric.renter.Network.UUResponseData;
import com.youyou.uuelectric.renter.R;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.VersionUtils;

import java.io.File;

/**
 * Created by aaron on 16/5/4.
 * app更新管理类
 * - app打开时进行检测是否需要更新
 * # 用户未登录，则检测是否需要更新
 * # 用户登录未有订单信息，则检测是否需要更新
 * # App有网发送广播，则检测是否需要更新
 */
public class UpdateManager {
    // 客户端渠道 1:Android;2:IOS
    public static final int CHANNEL_ANDROID = 1;
    // 用户保存App当次打开是否已经更新成功
    public static boolean isQueryAppUpdated = false;
    // 当前更新功能是否有效
    public static final int EFFECT = 0;
    public static final int ENEFFECT = 1;
    public static long lastProgress = -1;

    /**
     * 检测App是否需要更新
     *
     * @param mContext
     * @param isShow   若不需要更新是否需要弹出文案
     */
    public static void queryAppBaseVersionInfo(final Activity mContext, final boolean isOneUpdate, final boolean isShow) {
        try {
            // 若当前网络异常，则直接return
            if (!Config.isNetworkConnected(mContext)) {
                // 关闭进度条
                dismissProgress(isShow);
                return;
            }
            // 控制变量，App更新接口进程生命周期中只会调用一次
            if (isQueryAppUpdated && isOneUpdate) {
                return;
            }
            L.i("开始调用请求是否需要版本更新的接口....");
            ExtInterface.QueryAppBaseVersionInfoNL.Request.Builder request = ExtInterface.QueryAppBaseVersionInfoNL.Request.newBuilder();
            request.setClientChannel(CHANNEL_ANDROID);
            // 查询最新的版本信息，不需要传入版本号
            // request.setVersionCode(VersionUtils.getVersionName(mContext));
            NetworkTask task = new NetworkTask(Cmd.CmdCode.QueryAppBaseVersionInfo_VALUE);
            task.setBusiData(request.build().toByteArray());
            NetworkUtils.executeNetwork(task, new HttpResponse.NetWorkResponse<UUResponseData>() {
                @Override
                public void onSuccessResponse(UUResponseData responseData) {
                    if (responseData.getRet() == 0) {
                        try {
                            isQueryAppUpdated = true;
                            ExtInterface.QueryAppBaseVersionInfoNL.Response response = ExtInterface.QueryAppBaseVersionInfoNL.Response.parseFrom(responseData.getBusiData());
                            if (response.getRet() == 0) {
                                L.i("请求检测App是否更新接口成功，开始解析返回结果");
                                // 解析检测结果
                                parserUpdateResule(mContext, response, isShow);
                            } else {
                                if (isShow) {
                                    showDefaultNetworkSnackBar(mContext);
                                }
                            }
                        } catch (InvalidProtocolBufferException e) {
                            e.printStackTrace();
                            if (isShow) {
                                showDefaultNetworkSnackBar(mContext);
                            }
                        }
                    }
                }

                @Override
                public void onError(VolleyError errorResponse) {
                    L.e("请求检测更新接口失败....");
                    if (isShow) {
                        showDefaultNetworkSnackBar(mContext);
                    }
                }

                @Override
                public void networkFinish() {
                    L.i("请求检测更新接口完成....");
                    // 关闭进度条
                    dismissProgress(isShow);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 解析更新检查结果
     *
     * @param response
     */
    private static void parserUpdateResule(Activity mContext, ExtInterface.QueryAppBaseVersionInfoNL.Response response, boolean isShow) {
        if (mContext == null) {
            return;
        }
        // 判断是否需要更新
        ExtInterface.AppBaseVersionInfo appBaseVersionInfo = response.getAppBaseVersionInfo();
        // 若当前更新是否有效
        if (appBaseVersionInfo.getIsDel() == ENEFFECT) {
            return;
        }
        String updateVersionCode = appBaseVersionInfo.getVersionCode();
        int updateCode = changeVersionNameToCode(updateVersionCode);
        int localCode = changeVersionNameToCode(VersionUtils.getVersionName(mContext));
        // 本地应用版本号小于更新的应用版本号，则需要更新
        L.i("本地版本号：" + localCode + "  " + VersionUtils.getVersionName(mContext) + "  远程版本号：" + updateCode
                            + "  " + updateVersionCode);
        if (localCode < updateCode) {
            // 显示更新文案
            L.i("开始显示更新弹窗...");
            showUpdateDialog(mContext, appBaseVersionInfo);
        }
        // 不需要更新
        else {
            if (isShow) {
                Config.showToast(mContext, mContext.getResources().getString(R.string.about_new));
            }
        }
    }

    /**
     * 显示更新文案
     */
    public static void showUpdateDialog(final Activity mContext, ExtInterface.AppBaseVersionInfo appBaseVersionInfo) {
        String title = appBaseVersionInfo.getTitle();
        String msg = appBaseVersionInfo.getUpdateMsg();
        final String downButtonMsg = appBaseVersionInfo.getActionButtonMsg();
        String buttonMsg = appBaseVersionInfo.getButtonMsg();
        final String downloadUrl = appBaseVersionInfo.getDownLinkUrl();

        // 强制更新
        if (TextUtils.isEmpty(appBaseVersionInfo.getButtonMsg())) {
            DownLoadDialog.getInstance(mContext).showMaterialTipDialog(title, msg, downButtonMsg,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            doDownLoad(mContext, downloadUrl, downButtonMsg, true);
                    }
            });
        }
        // 普通更新提示
        else {
            if (DownLoadDialog.materialDialog != null && DownLoadDialog.materialDialog.isShowing()) {
                return;
            }
            DownLoadDialog.getInstance(mContext).showMaterialDialog(title, msg, buttonMsg, downButtonMsg,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // 取消下载
                            // DownloadManager.getInstance().cancelAll();
                            // 取消下载通知栏
                            // UUApp.notificationManager.cancel(DownloadNotification.notofyId);
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            doDownLoad(mContext, downloadUrl, downButtonMsg, false);
                        }
                    });

             /**
              * 判断当前是否下载中
              */
             if (DownloadManager.downloadId != -1) {
                 int downStatus = DownloadManager.getInstance().query(DownloadManager.downloadId);
                 showPositiveText(downStatus == ThinDownloadManager.STATUS_RUNNING, downButtonMsg);
                 L.i("downId:" + DownloadManager.downloadId + "   downStatus:" + downStatus + "  RUNNING:" + ThinDownloadManager.STATUS_RUNNING);
             }
        }
    }

    /**
     * 关闭进度条
     * @param isShow
     */
    private static void dismissProgress(boolean isShow) {
        if (isShow) {
            Config.dismissProgress();
        }
    }

    /**
     * 开始执行下载动作
     */
    private static void doDownLoad(final Activity mContext, String downloadUrl, final String actionButtonMsg, final boolean isFocusUpdate) {
        // 强制更新
        if (isFocusUpdate) {
            DownLoadDialog.updateRela.setVisibility(View.VISIBLE);
            DownLoadDialog.progressBar.setProgress(0);
            DownLoadDialog.progressBar.start();
            DownLoadDialog.updatePercent.setText("0%");
            DownLoadDialog.materialDialog.getPositiveButton().setEnabled(false);
            DownLoadDialog.materialDialog.getPositiveButton().setText("下载中");
        }
        Config.showToast(mContext, "开始下载安装包.......");
        // 删除下载的apk文件
        doDeleteDownApk(mContext);
        L.i("安装包下载地址：" + downloadUrl);
        DownloadManager.getInstance().cancelAll();
        DownloadManager.downloadId = DownloadManager.getInstance().add(DownloadManager.getDownLoadRequest(mContext, downloadUrl, new DownloadStatusListenerV1() {
            @Override
            public void onDownloadComplete(DownloadRequest downloadRequest) {
                L.i("onDownloadComplete_____...");
                // 设置按钮是否可点击
                showPositiveText(false, actionButtonMsg);
                if (isFocusUpdate) {
                    // 更新进度条显示
                    DownLoadDialog.updatePercent.setText("100%");
                    DownLoadDialog.progressBar.stop();
                } else {
                    String title = "正在下载友友用车...";
                    String content = "下载成功";
                    DownloadNotification.showNotification(mContext, title, content, DownloadNotification.notofyId);
                    // 关闭通知栏消息
                    UUApp.notificationManager.cancel(DownloadNotification.notofyId);
                }
                // 下载完成，执行安装逻辑
                doInstallApk(mContext);
                // 退出App
                UUApp.getInstance().exit();
            }

            @Override
            public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                L.i("onDownloadFiled______...");
                L.i("errorMessage:" + errorMessage);
                // 设置按钮是否可点击
                showPositiveText(false, actionButtonMsg);
                if (isFocusUpdate) {
                    // DownLoadDialog.progressBar.stop();
                    DownLoadDialog.updatePercent.setText("更新失败");
                } else {
                    String title = "正在下载友友用车...";
                    String content = "下载失败";
                    DownloadNotification.showNotification(mContext, title, content, DownloadNotification.notofyId);
                }
            }

            @Override
            public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                if (lastProgress != progress) {
                    lastProgress = progress;
                    L.i("onProgress_____progress:" + progress + "  totalBytes:" + totalBytes + "  downloadedBytes:" + downloadedBytes);
                    // 设置按钮是否可点击
                    showPositiveText(true, actionButtonMsg);
                    // 强制更新则更新进度条
                    if (isFocusUpdate) {
                        String content = downloadedBytes * 100 / totalBytes + "%";
                        float result = progress / (float)100.00;
                        DownLoadDialog.progressBar.setProgress(result);
                        DownLoadDialog.updatePercent.setText(content);
                    } else {
                        String title = "正在下载友友用车...";
                        String content = downloadedBytes * 100 / totalBytes + "%";
                        DownloadNotification.showNotification(mContext, title, content, DownloadNotification.notofyId);
                    }
                }
            }
        }));
    }


    /**
     * 设置按钮是否可点击
     * @param isDowning
     * @param actionButtonMsg
     */
    private static void showPositiveText(boolean isDowning, String actionButtonMsg) {
        if (DownLoadDialog.materialDialog == null) {
            return;
        }
        if (isDowning) {
            DownLoadDialog.materialDialog.getPositiveButton().setEnabled(false);
            DownLoadDialog.materialDialog.getPositiveButton().setText("下载中");
        } else {
            DownLoadDialog.materialDialog.getPositiveButton().setEnabled(true);
            DownLoadDialog.materialDialog.getPositiveButton().setText(actionButtonMsg);
        }
    }

    /**
     * 删除下载的apk文件
     * @param mContext
     */
    private static void doDeleteDownApk(Activity mContext) {
        L.i("开始执行删除下载apk文件的逻辑...");
        File file = new File(DownloadManager.getApkPath(mContext));
        if (file.exists()) {
            boolean result = file.delete();
            L.i("删除下载文件:" + result);
        } else {
            L.i("apk文件不存在....");
        }
    }

    /**
     * 执行安装apk文件
     */
    private static void doInstallApk(Activity mContext) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(DownloadManager.getApkPath(mContext))),
                "application/vnd.android.package-archive");
        mContext.startActivity(intent);
    }


    /**
     * APP版本号转换为Integer如：3.2.0 转化 320
     */
    public static final int changeVersionNameToCode(String versionCode) {
        //根据正则表达式判断版本号是否合法
        try {
            String pattern = "\\d\\.\\d\\.\\d";
            if (versionCode.matches(pattern)) {
                String[] nums = versionCode.split("\\.");
                if (nums != null && nums.length > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (String num : nums) {
                        sb.append(num);
                    }

                    return Integer.valueOf(sb.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * 默认的网络错误SnackBar
     */
    public static void showDefaultNetworkSnackBar(Activity mContext) {
        String msg = mContext.getResources().getString(R.string.network_error_tip);
        Config.showToast(mContext, msg);
    }
}