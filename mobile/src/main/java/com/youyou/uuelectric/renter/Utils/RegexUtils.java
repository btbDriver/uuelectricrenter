package com.youyou.uuelectric.renter.Utils;

import android.text.TextUtils;

/**
 * Created by liuchao on 2015/9/10.
 * 正则表达式工具类
 */
public class RegexUtils {

    /**
     * 正则验证--手机号
     *
     * @param phone
     * @return
     */
    public static boolean regexMobilePhone(String phone) {
        if (TextUtils.isEmpty(phone))
            return false;
        return phone.length() == 11;
    }

    /**
     * 正则验证-验收手机验证码
     *
     * @param code
     * @return
     */
    public static boolean regexMobileCode(String code) {
        if (TextUtils.isEmpty(code))
            return false;
        return code.length() == 4;
    }

    /**
     * 正则验证-验证驾驶证号
     *
     * @param license
     * @return
     */
    public static boolean regexLicense(String license) {
        if (TextUtils.isEmpty(license))
            return false;
//        (\d{14}[0-9a-zA-Z])|
        return license.matches("(\\d{17}[0-9a-zA-Z])");
    }

    /**
     * 正则验证-验证档案编号
     *
     * @param record
     * @return
     */
    public static boolean regexRecord(String record) {
        if (TextUtils.isEmpty(record))
            return false;
        return record.matches("\\d{12}");
    }

    /**
     * 正则验证-验证优惠券、分享码
     *
     * @param couponCode
     * @return
     */
    public static boolean regexCouponCode(String couponCode) {
        if (TextUtils.isEmpty(couponCode))
            return false;
        return true;
    }
}
