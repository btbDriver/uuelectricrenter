package com.youyou.uuelectric.renter.Network;

import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.access.app.header.HeaderCommon;
import com.youyou.uuelectric.renter.Network.exception.InterfaceFallException;
import com.youyou.uuelectric.renter.Network.exception.NetWorkFallException;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Network.user.UserSecurityMap;
import com.youyou.uuelectric.renter.UUApp;
import com.youyou.uuelectric.renter.Utils.Support.Config;
import com.youyou.uuelectric.renter.Utils.Support.L;
import com.youyou.uuelectric.renter.Utils.Support.SysConfig;
import com.youyou.uuelectric.renter.Utils.eventbus.BaseEvent;
import com.youyou.uuelectric.renter.Utils.eventbus.EventBusConstant;
import com.youyou.uuelectric.renter.Utils.volley.toolbox.BaseMultipartRequest;
import com.youyou.uuelectric.renter.Utils.volley.toolbox.HttpProtoVolleyRequest;
import com.youyou.uuelectric.renter.Utils.volley.toolbox.JsonMultipartRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;


/**
 * Created by taurusxi on 14-8-6.
 */
public class VolleyNetworkHelper<T> implements HttpNetwork<T> {
    private static RequestQueue mRequestQueue;

    public VolleyNetworkHelper() {
        if (mRequestQueue == null) {
            mRequestQueue = UUApp.getRequestQueue();
        }
    }


    @Override
    public void doPost(int seq, NetworkTask networkTask, HttpResponse.NetWorkResponse baseResponse) {
        switch (networkTask.getType()) {
            // 数据网络请求
            case NetworkTask.PROTOBUF_TYPE:
                getProtobufPostHttp(seq, networkTask, baseResponse);
                break;
            // 二进制网络请求
            case NetworkTask.CAMERA_TYPE:
                getCameraPostHttp(seq, networkTask, baseResponse);
                break;
        }
    }

    /**
     * 请求网络--文件
     *
     * @param seq
     * @param networkTask
     * @param baseResponse
     */
    private void getCameraPostHttp(int seq, NetworkTask networkTask, final HttpResponse.NetWorkResponse baseResponse) {
        // 获取二进制数据请求路径
        String httpUrl = "http://" + NetworkTask.getBASEURL() + ":18149/upload";
        networkTask.setHttpUrl(httpUrl);
        BaseMultipartRequest baseMultipartRequest = new JsonMultipartRequest(networkTask.getHttpUrl(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (baseResponse != null) {
                    baseResponse.networkFinish();
                    try {
                        baseResponse.onSuccessResponse(response);
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (baseResponse != null) {
                    baseResponse.networkFinish();
                    baseResponse.onError(error);
                }
            }
        });
        UUParams params = networkTask.getUuParams();
        networkTask.setSeq(seq);
        LinkedHashMap<String, String> uriMap = params.getUrlParams();
        ConcurrentHashMap<String, UUParams.FileWrapper> fileMap = params.getFileParams();
        Map<String, String> header = new HashMap<String, String>();
        header.put("sessionKey", HexUtil.bytes2HexStr(UserConfig.getUserInfo().getSessionKey()));
        header.put("device", networkTask.getCameraUa());
        baseMultipartRequest.setHeader(header);
        for (Map.Entry<String, String> uri : uriMap.entrySet()) {
            baseMultipartRequest.addStringBody(uri.getKey(), uri.getValue());
        }
        for (Map.Entry<String, UUParams.FileWrapper> fileWrapper : fileMap.entrySet()) {
            baseMultipartRequest.addFileBody(fileWrapper.getKey(), fileWrapper.getValue().file);
        }
        baseMultipartRequest.buildMultipartEntity();
        baseMultipartRequest.setTag(networkTask.getTag());
        baseMultipartRequest.setRetryPolicy(new DefaultRetryPolicy(15 * 1000, 0, 1.0f));

        mRequestQueue.add(baseMultipartRequest);
    }


    /**
     * 请求网络--数据
     *
     * @param seq
     * @param networkTask
     * @param baseResponse
     */
    private void getProtobufPostHttp(final int seq, final NetworkTask networkTask, final HttpResponse.NetWorkResponse<UUResponseData> baseResponse) {
        String httpUrl = "";
        final int type = UserConfig.getHttpUrl(networkTask);
        // 根据不同的网络请求，赋值不同的请求地址
        if (type == NetworkTask.SSL_NETWORK) {
            httpUrl = "https://" + NetworkTask.getBASEURL() + ":18082";
        } else if (type == NetworkTask.HTTP_NETWORK) {
            httpUrl = "http://" + NetworkTask.getBASEURL() + ":18081";
        } else {
            L.d("网络请求异常，type值异常");
            return;
        }

        networkTask.setSeq(seq);
        HttpProtoVolleyRequest httpProtoVolleyRequest = null;
        httpProtoVolleyRequest = new HttpProtoVolleyRequest(httpUrl, new Response.Listener<HeaderCommon.ResponsePackage>() {
            @Override
            public void onResponse(HeaderCommon.ResponsePackage publicResponsePackage) {
                baseResponse.networkFinish();
                if (publicResponsePackage == null) {
                    baseResponse.onError(new VolleyError("publicResponsePackage为null"));
                    return;
                }

                int ret = publicResponsePackage.getRet();
                final UserSecurityMap.SecurityItem securityItem = UserSecurityMap.get(seq);
//                L.d("ret = " + ret + "__securityItem = ", securityItem.toString() + "_isPublic = " + securityItem.isPublic);
                // 若当前为开发模式，则显示当前的
                if (SysConfig.DEVELOP_MODE) {
                    if (ret != 0) {
                        Toast.makeText(UUApp.getInstance().getApplicationContext(), "securityItem__ret返回:" + ret + "和cmd:" + securityItem.networkItem.getCmd(), Toast.LENGTH_LONG).show();
                    }
                }
                //   0 网络操作成功
                //  -1 提示“网络失败”
                // -11 用户的登录状态失效，需要强制客户端重新登录；
                // -12 客户端的匿名票据失效，如果server返回该值，客户端需要使用公共秘钥在重试一次接口访问，并且启动客户端的后台异步匿名登录
                // -13 该用户在另一个设备上登录，当前设备的登录态失效，客户端需要弹出提示框，让用户登录页登录
                // -14 从页面通过scheme，页面传入的身份和客户端本地的身份不匹配，返回该错误码，客户端让用户去登录页
                // -21 频率限制
                if (ret == 0) {
                    UUResponseData data = new UUResponseData();
                    data.setRet(ret);
                    data.setHttp(true);
                    data.setSeq(publicResponsePackage.getSeq());
                    data.setToastMsg("");
                    //表示接口调用成功
                    boolean isPublic = securityItem.isPublic;
                    byte[] output;
                    if (isPublic) {
                        output = AESUtils.decrypt(UserConfig.b3Key, publicResponsePackage.getResData().toByteArray());
                    } else {
//                        L.d("b3KeyItem:" + GsonUtils.getInstance().toJson(securityItem.b3KeyItem));
                        output = AESUtils.decrypt(securityItem.b3KeyItem, publicResponsePackage.getResData().toByteArray());
                    }
                    try {
                        HeaderCommon.ResponseData responseData = HeaderCommon.ResponseData.parseFrom(output);
                        data.setCmd(responseData.getCmd());
                        data.setBusiData(responseData.getBusiData().toByteArray());
                        data.setResponseCommonMsg(responseData.getCommonMsg());
//                        data.setLocationTracking(responseData.getOpenLocationTracking());
                        baseResponse.onSuccessResponse(data);
                    } catch (InvalidProtocolBufferException e) {
                        baseResponse.onError(new InterfaceFallException());
                    }
                } else if (ret == -1) {
                    //表示接口调用失败
                    baseResponse.onError(new NetWorkFallException("网络失败"));
                } else if (ret == -11) {
                    //登录态失效，客户端强制用户重新登录
                    //如果不是游客模式的话才弹窗
                    // mainActivity和BaseActivity中接受事件
                    EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_NETWORK_TOLOGIN, "您的账号已失效，请重新登录"));
                } else if (ret == -12) {
                    //客户端的匿名票据失效，如果server返回该值，客户端需要使用公共密钥再重试一次接口访问，并且启动客户端的后台异步匿名登陆
                    // NetworkTask networkItem = securityItem.networkItem;
                    // 此处是接续重试上一次的接口，不能把命令字改成更新票据，否则会返回-31错误，所以注掉
//                    networkItem.setCmd(CmdCodeDef.CmdCode.UpdateTicketSSL_VALUE);
                    // networkItem.setUsePublic(true);
                    // 当前没有匿名登陆，则注释该语句
                    // getProtobufPostHttp(seq, networkItem, baseResponse);
                    // mainActivity和BaseActivity中接受事件
                    L.d("服务器端返回-12");
                    EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_NETWORK_TOLOGIN, "您的登录已失效，请重新登录"));
                } else if (ret == -13) {
                    //该用户在另一个设备上登录，当前设备的登录态失效，客户端需要弹出提示框，让用户去登录页登录
                    if (!NetworkUtils.isShowDialog.get()) {
                        NetworkUtils.isShowDialog.set(true);
                        // mainActivity和BaseActivity中接受事件
                        EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_NETWORK_TOLOGIN, "您的账号已在其他设备登录，请重新登录"));
                    }
                }
                /**
                 * desc:从页面通过scheme，页面传入的身份和客户端本地的身份不匹配，返回错误码，客户端让用户去登录页
                 */
                else if (ret == -14) {
                    if (!NetworkUtils.isShowDialog.get()) {
                        NetworkUtils.isShowDialog.set(true);
                        // mainActivity和BaseActivity中接受事件
                        EventBus.getDefault().post(new BaseEvent(EventBusConstant.EVENT_TYPE_NETWORK_TOLOGIN, "身份已失效，请重新登录"));
                    }
                } else if (ret == -21) {
                    //频率限制
                    Config.showToast(UUApp.getInstance().getApplicationContext(), "您操作太快，请稍候重试！");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                baseResponse.networkFinish();
                baseResponse.onError(volleyError);
            }
        }) {
            @Override
            public NetworkTask getNetworkTask() {
                return networkTask;
            }
        };
        httpProtoVolleyRequest.setTag(networkTask.getTag());
        httpProtoVolleyRequest.setRetryPolicy(new DefaultRetryPolicy(15 * 1000, 0, 1.0f));
        mRequestQueue.add(httpProtoVolleyRequest);
    }
}
