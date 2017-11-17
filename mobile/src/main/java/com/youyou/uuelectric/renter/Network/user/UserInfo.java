package com.youyou.uuelectric.renter.Network.user;

/**
 * Created by liuchao on 2015/9/9.
 */
public class UserInfo {
    // 字符串类型
    private String b4Domain = null;
    // byte[] 类型
    private byte[] sessionKey = null;
    private byte[] b2 = null;
    private byte[] b3 = null;
    private byte[] b3Key = null;
    private byte[] b4 = null;

    private boolean isLoginState = false;// 是否是登陆状态，指的是是否登陆过用户的真是身份，匿名登陆除外
    private int validSecs = 0;// 票据有效时间
    private int unvalidSecs = 0;// 票据失效时间
    private int needFlushSecs = 0;// 需要更新票据时间

    private String phone = null;// 用户手机号
    private String imgUrl = null;// 用户头像URL
    private int userStatus = 0;// 用户状态
    private String displayName = null;// 用户显示名称
    // 0 新注册
    // 1 待审核
    // 2 申请被驳回
    // 3 已通过审核
    // 4 申请被驳回，并且不能再次提交审核数据


    //############################# get set #######################################

    public String getB4Domain() {
        return b4Domain;
    }

    public boolean isLoginState() {
        return isLoginState;
    }

    public int getValidSecs() {
        return validSecs;
    }

    public void setB4Domain(String b4Domain) {
        this.b4Domain = b4Domain;
    }

    public void setIsLoginState(boolean isLoginState) {
        this.isLoginState = isLoginState;
    }

    public void setValidSecs(int validSecs) {
        this.validSecs = validSecs;
    }


    public byte[] getSessionKey() {
        return sessionKey;
    }

    public byte[] getB2() {
        return b2;
    }

    public byte[] getB3() {
        return b3;
    }

    public byte[] getB3Key() {
        return b3Key;
    }

    public byte[] getB4() {
        return b4;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void setB2(byte[] b2) {
        this.b2 = b2;
    }

    public void setB3(byte[] b3) {
        this.b3 = b3;
    }

    public void setB3Key(byte[] b3Key) {
        this.b3Key = b3Key;
    }

    public void setB4(byte[] b4) {
        this.b4 = b4;
    }

    public int getUnvalidSecs() {
        return unvalidSecs;
    }

    public void setUnvalidSecs(int unvalidSecs) {
        this.unvalidSecs = unvalidSecs;
    }

    public int getNeedFlushSecs() {
        return needFlushSecs;
    }

    public void setNeedFlushSecs(int needFlushSecs) {
        this.needFlushSecs = needFlushSecs;
    }

    public String getPhone() {
        return phone;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public int getUserStatus() {
        return userStatus;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public void setUserStatus(int userStatus) {
        this.userStatus = userStatus;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
