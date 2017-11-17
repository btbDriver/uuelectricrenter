package com.youyou.uuelectric.renter.Network.user;

/**
 * Created by liuchao on 2015/9/14.
 * 用户状态信息
 */
public class UserStatus {
    /* 新注册 */
    public static final int NEW_REG = 0;
    public static final String NEW_REG_STR = "未认证";

    /* 待审核 */
    public static final int WAIT_VERIFY = 1;
    public static final String WAIT_VERIFY_STR = "认证中...";

    /* 申请被驳回 */
    public static final int APPLY_REJECTED = 2;
    public static final String APPLY_REJECTED_STR = "认证失败，请重新上传";

    /* 已通过审核 */
    public static final int APPLY_PASSED = 3;
    public static final String APPLY_PASSED_STR = "认证成功";

    /* 申请被驳回，并且不能再次提交审核数据 */
    public static final int APPLY_REJECTED_NOT_ALLOW_TRY_AGAIN = 4;
    public static final String APPLY_REJECTED_NOT_ALLOW_TRY_AGAIN_STR = "认证失败，请重新上传";

    /**
     * 获取用户状态信息
     *
     * @param userStatus
     * @return
     */
    public static String getUserStatusStrFromStatusInt(int userStatus) {
        StringBuffer result = new StringBuffer(NEW_REG_STR);
        if (userStatus == NEW_REG) {
            result = new StringBuffer(NEW_REG_STR);
        } else if (userStatus == WAIT_VERIFY) {
            result = new StringBuffer(WAIT_VERIFY_STR);
        } else if (userStatus == APPLY_REJECTED) {
            result = new StringBuffer(APPLY_REJECTED_STR);
        } else if (userStatus == APPLY_PASSED) {
            result = new StringBuffer(APPLY_PASSED_STR);
        } else if (userStatus == APPLY_REJECTED_NOT_ALLOW_TRY_AGAIN) {
            result = new StringBuffer(APPLY_REJECTED_NOT_ALLOW_TRY_AGAIN_STR);
        }

        return result.toString();
    }
}
