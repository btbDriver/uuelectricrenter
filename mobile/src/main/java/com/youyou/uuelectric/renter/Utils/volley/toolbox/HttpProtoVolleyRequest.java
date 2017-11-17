

package com.youyou.uuelectric.renter.Utils.volley.toolbox;

import android.app.Activity;
import android.telephony.TelephonyManager;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.uu.access.app.header.HeaderCommon;
import com.youyou.uuelectric.renter.Network.AESUtils;
import com.youyou.uuelectric.renter.Network.NetworkTask;
import com.youyou.uuelectric.renter.Network.user.UserConfig;
import com.youyou.uuelectric.renter.Network.user.UserSecurityMap;
import com.youyou.uuelectric.renter.UUApp;

/**
 * A canned request for retrieving the response body at a given URL as a String.
 */
public abstract class HttpProtoVolleyRequest extends Request<HeaderCommon.ResponsePackage>
{
    private final Listener<HeaderCommon.ResponsePackage> mListener;

    /**
     * Creates a new request with the given method.
     *
     * @param method        the request {@link com.android.volley.Request.Method} to use
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public HttpProtoVolleyRequest(int method, String url, Listener<HeaderCommon.ResponsePackage> listener,
                                  ErrorListener errorListener) {
        super(method, url, errorListener);
        mListener = listener;
    }

    /**
     * Creates a new GET request.
     *
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public HttpProtoVolleyRequest(String url, Listener<HeaderCommon.ResponsePackage> listener, ErrorListener errorListener) {
        this(Method.POST, url, listener, errorListener);
    }

    @Override
    protected Response<HeaderCommon.ResponsePackage> parseNetworkResponse(NetworkResponse response) {


        HeaderCommon.ResponsePackage resPackage = null;
        try {
            resPackage = HeaderCommon.ResponsePackage.parseFrom(response.data);
            return Response.success(resPackage, HttpHeaderParser.parseCacheHeaders(response));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(HeaderCommon.ResponsePackage response) {
        mListener.onResponse(response);

    }

    @Override
    public byte[] getBody() throws AuthFailureError
    {
        final NetworkTask task = getNetworkTask();
        boolean isPublic = UserConfig.isPuilc(task) || task.isUsePublic();
        HeaderCommon.CommonReqHeader.Builder headerBuilder = HeaderCommon.CommonReqHeader.newBuilder();
        //L.d("securityMap:" + GsonUtils.getInstance().toJson(UserConfig.getUserInfo()));
        UserSecurityMap.SecurityItem securityItem = new UserSecurityMap.SecurityItem(task, isPublic, UserConfig.getUserInfo().getB3Key(), UserConfig.getUserInfo().getB2(), UserConfig.getUserInfo().getB3(), UserConfig.getUserInfo().getSessionKey());
        UserSecurityMap.put(task.getSeq(), securityItem);
        headerBuilder.setSeq(task.getSeq());
        headerBuilder.setCmd(task.getCmd());
        // 登出状态时，不设置B2
        if (isPublic) {
//            headerBuilder.setB2(ByteString.copyFrom(UserSecurityConfig.b2));
        } else {
            headerBuilder.setB2(ByteString.copyFrom(securityItem.b2Item));
        }
        headerBuilder.setUuid(UserConfig.getUUID());
        if (task.getType() == NetworkTask.CAMERA_TYPE) {
            headerBuilder.setUa(task.getCameraUa());
        } else {
            headerBuilder.setUa(task.getUa());
        }
        TelephonyManager tm = (TelephonyManager) UUApp.getInstance().getContext().getSystemService(Activity.TELEPHONY_SERVICE);
        String imei = tm.getDeviceId();
        if (imei != null && !"".equals(imei)) {
            headerBuilder.setImei(imei);
        }
        HeaderCommon.RequestData.Builder requestData = HeaderCommon.RequestData.newBuilder();
        requestData.setBusiData(ByteString.copyFrom(task.getBusiData()));
        requestData.setHeader(headerBuilder.build());
        /**
         * desc:添加参数Jump2ClientSource，主要用于客户端H5页面通过scheme动作请求服务器
         */
        if (task.getSourceSessionKey() != null) {
            requestData.setJump2ClientSourceSessionKey(task.getSourceSessionKey());
        }


        HeaderCommon.RequestPackage.Builder prpBuilder = HeaderCommon.RequestPackage.newBuilder();
        if (isPublic) {
            // 登出状态，不设置B3 不设置sessionKey
            // prpBuilder.setSessionKey(ByteString.copyFrom("".getBytes()));
            // prpBuilder.setB3(ByteString.copyFrom(UserSecurityConfig.b3));
            prpBuilder.setReqData(ByteString.copyFrom(AESUtils.encrypt(UserConfig.b3Key, requestData.build().toByteArray())));
        } else {
            prpBuilder.setSessionKey(ByteString.copyFrom(securityItem.sessionKeyItem));
            prpBuilder.setB3(ByteString.copyFrom(securityItem.b3Item));
            prpBuilder.setReqData(ByteString.copyFrom(AESUtils.encrypt(securityItem.b3KeyItem, requestData.build().toByteArray())));
        }

        byte[] toSendData = prpBuilder.build().toByteArray();
        return toSendData;
    }

    public abstract NetworkTask getNetworkTask();

}
