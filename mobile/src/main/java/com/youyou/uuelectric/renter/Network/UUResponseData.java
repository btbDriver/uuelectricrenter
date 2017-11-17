package com.youyou.uuelectric.renter.Network;


import com.uu.access.app.header.HeaderCommon;

public class UUResponseData {

    private int ret; // 0代表接口调用成功

    private int cmd;

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    private int seq;


    public HeaderCommon.ResponseCommonMsg getResponseCommonMsg() {
        return responseCommonMsg;
    }

    public void setResponseCommonMsg(HeaderCommon.ResponseCommonMsg responseCommonMsg) {
        this.responseCommonMsg = responseCommonMsg;
    }

    private HeaderCommon.ResponseCommonMsg responseCommonMsg;
    private String toastMsg; // 接口返回的吐司

    private byte[] busiData; // 实际的业务数据

    private boolean isHttp;// 区别  SSL  和 http ，判断是否需要加密

    public boolean isHttp() {
        return isHttp;
    }

    public void setHttp(boolean isHttp) {
        this.isHttp = isHttp;
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getToastMsg() {
        return toastMsg == null ? "" : toastMsg;
    }

    public void setToastMsg(String toastMsg) {
        this.toastMsg = toastMsg;
    }

    public byte[] getBusiData() {
        return busiData;
    }

    public void setBusiData(byte[] busiData) {
        this.busiData = busiData;
    }
//
//    public UuCommon.LocationTracking locationTracking;
//
//    public void setLocationTracking(UuCommon.LocationTracking locationTracking) {
//        this.locationTracking = locationTracking;
//        Config.saveLocationMsg(this.locationTracking);
//        if (locationTracking.getOpenLocationTracking()) {
////            Config.startLocationService(Config.currentContext);
//            ((UUApp) Config.currentContext.getApplication()).startLocationTrackingService();
//        } else {
////            Config.closeLocationService(Config.currentContext);
//            ((UUApp) Config.currentContext.getApplication()).quitLocationTrackingService();
//        }
//    }


}
