package com.youyou.uuelectric.renter.Network;

import android.content.Context;


import com.youyou.uuelectric.renter.UI.web.H5Constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by taurusxi on 14-8-6.
 */
public class NetworkTask {
    // 测试环境IP
    public static String TEST_IP = "115.28.82.160";
    // public static final String TEST_IP = "115.28.82.160";// 阿里测试环境
    // 正式环境IP
    public static String NORMAL_IP = "115.28.253.237";
    // 40环境
    public static String TEST_IP_40 = "192.168.255.40";

    // 网络协议类型
    public static String HTTPS_TYPE = "https://";
    public static String HTTP_TYPE = "http://";

    // 请求IP地址、方式
    private static String BASEURL = NORMAL_IP; //网络请求中的IP地址
    public static final int DEFALT_METHOD = Method.POST;
    public static final String DEFALT_TAG = "Http_connect";

    // 上传文件，请求网络
    public static final int CAMERA_TYPE = 1;
    public static final int PROTOBUF_TYPE = 2;
    public static final String PHOTO_UPLOAD_HTTP = HTTP_TYPE + getBASEURL() + ":18149/upload";

    public static final int SSL_NETWORK = 1;  //走 SSL
    public static final int HTTP_NETWORK = 2; // 走 http
    private int seq;
    private int method;
    private int type;
    private UUParams uuParams;
    private Context context;
    private String httpUrl;
    private String tag;
    private byte[] busiData;
    private boolean isUsePublic = false;

    private String ua;
    public static String DEFEALT_UA = "A_121&ADR&Android4.4.2&qq";
    private int cmd;

    //H5调用Native时传递的jump2ClientSource;
    private String sourceSessionKey;
    private int platformType;


    public NetworkTask(UUParams uuParams) {
        this.httpUrl = PHOTO_UPLOAD_HTTP;
        method = DEFALT_METHOD;
        tag = DEFALT_TAG;
        this.uuParams = uuParams;
        ua = DEFEALT_UA;
        this.type = CAMERA_TYPE;
        this.isUsePublic = false;
    }

    public NetworkTask(int cmd) {
        method = DEFALT_METHOD;
        tag = DEFALT_TAG;
        ua = DEFEALT_UA;
        this.cmd = cmd;
        this.tag = cmd + "";
        this.type = PROTOBUF_TYPE;
        this.isUsePublic = false;
    }

    /**
     * 重写构造方法，获取H5页面传递的URL中的SessionKey参数
     *
     * @param cmd
     * @param url
     */
    public NetworkTask(int cmd, String url) {
        method = DEFALT_METHOD;
        tag = DEFALT_TAG;
        ua = DEFEALT_UA;
        this.cmd = cmd;
        this.tag = cmd + "";
        this.type = PROTOBUF_TYPE;
        this.isUsePublic = false;

        String[] urlSplit = url.split("\\?");
        Map<String, String> queryMap = new HashMap<String, String>();
        String h5Url = null;
        if (urlSplit.length == 2) {
            queryMap = H5Constant.parseUriQuery(urlSplit[1]);
        }

        try {
            if (queryMap.containsKey("sourceSessionKey")) {
                sourceSessionKey = queryMap.get("sourceSessionKey");
            }
            if (queryMap.containsKey("platformType")) {
                String type = queryMap.get("platformType");
                platformType = Integer.valueOf(type);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface Method {
        int DEPRECATED_GET_OR_POST = -1;
        int GET = 0;
        int POST = 1;
        int PUT = 2;
        int DELETE = 3;
        int HEAD = 4;
        int OPTIONS = 5;
        int TRACE = 6;
        int PATCH = 7;
    }


    //################################# get set 方法##############################################

    public String getSourceSessionKey() {
        return sourceSessionKey;
    }

    public void setSourceSessionKey(String sourceSessionKey) {
        this.sourceSessionKey = sourceSessionKey;
    }

    public int getPlatformType() {
        return platformType;
    }

    public void setPlatformType(int platformType) {
        this.platformType = platformType;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }


    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public String getUa() {
        return ua;
    }

    public String getCameraUa() {
        String cameraUa = ua.replace("&", "|");
        return cameraUa;
    }

    public void setUa(String ua) {
        this.ua = ua;
    }

    public byte[] getBusiData() {
        return busiData;
    }

    public void setBusiData(byte[] busiData) {
        this.busiData = busiData;
    }

    public void setMethod(int method) {
        this.method = method;
    }

    public int getMethod() {
        return method;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UUParams getUuParams() {
        return uuParams;
    }

    public void setUuParams(UUParams uuParams) {
        this.uuParams = uuParams;
    }

    public boolean isUsePublic() {
        return isUsePublic;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public void setUsePublic(boolean isUsePublic) {
        this.isUsePublic = isUsePublic;
    }

    public static String getBASEURL() {
        return BASEURL;
    }

    public static void setBASEURL(String BASEURL) {
        NetworkTask.BASEURL = BASEURL;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("NetworkTask___DEFALT_METHOD =");
        stringBuilder.append(getMethod() + "_");
        stringBuilder.append("TYPE =");
        stringBuilder.append(getType() + "_");
        stringBuilder.append("CMD =");
        stringBuilder.append(getCmd() + "_");
        stringBuilder.append("TAG=" + getTag());
        return stringBuilder.toString();
    }
}
