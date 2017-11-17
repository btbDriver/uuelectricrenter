package com.youyou.uuelectric.renter.UI.order;

import com.uu.facade.activity.pb.common.ActivityCommon;

/**
 * Created by liuchao on 2015/9/22.
 */
public class FavourInfo {

    public int couponId;// 优惠券ID
    public String couponName;// 优惠券名称
    public int startUseTime;// 开始使用时间
    public int endUseTime;// 截至使用时间
    public ActivityCommon.CouponState couponState;// 优惠券状态
    public String couponDes;// 优惠券描述
    public String couponAmount;// 优惠券金额
    public String orderUseMsg;// 当前订单可用文字信息
    public boolean isSelected;// 是否选中
    public int couponType;// 优惠券类型 0-普通金额优惠券；1-折扣优惠券
    public String couponDiscount;// 优惠券折扣值 如：8.5折(带'折'字)
    public String couponMaxReduce;// 折扣优惠券最多减免金额


    public void setCouponId(int couponId) {
        this.couponId = couponId;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public void setStartUseTime(int startUseTime) {
        this.startUseTime = startUseTime;
    }

    public void setEndUseTime(int endUseTime) {
        this.endUseTime = endUseTime;
    }

    public void setCouponState(ActivityCommon.CouponState couponState) {
        this.couponState = couponState;
    }

    public void setCouponDes(String couponDes) {
        this.couponDes = couponDes;
    }

    public void setCouponAmount(String couponAmount) {
        this.couponAmount = couponAmount;
    }

    public void setOrderUseMsg(String orderUseMsg) {
        this.orderUseMsg = orderUseMsg;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getCouponId() {

        return couponId;
    }

    public String getCouponName() {
        return couponName;
    }

    public int getStartUseTime() {
        return startUseTime;
    }

    public int getEndUseTime() {
        return endUseTime;
    }

    public ActivityCommon.CouponState getCouponState() {
        return couponState;
    }

    public String getCouponDes() {
        return couponDes;
    }

    public String getCouponAmount() {
        return couponAmount;
    }

    public String getOrderUseMsg() {
        return orderUseMsg;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getCouponType() {
        return couponType;
    }

    public void setCouponType(int couponType) {
        this.couponType = couponType;
    }

    public String getCouponDiscount() {
        return couponDiscount;
    }

    public void setCouponDiscount(String couponDiscount) {
        this.couponDiscount = couponDiscount;
    }

    public String getCouponMaxReduce() {
        return couponMaxReduce;
    }

    public void setCouponMaxReduce(String couponMaxReduce) {
        this.couponMaxReduce = couponMaxReduce;
    }
}
