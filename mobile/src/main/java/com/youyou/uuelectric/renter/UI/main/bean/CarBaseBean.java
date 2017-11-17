package com.youyou.uuelectric.renter.UI.main.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.uu.facade.usecar.protobuf.iface.UsecarCommon;

/**
 * Created by liuchao on 2015/12/3.
 * 点击费用预估时传递的参数
 */
public class CarBaseBean implements Parcelable {

    private float pricePerKm;// 公里价（每公里xx元）
    private float pricePerMinute;// 分钟价（每分钟xx元）
    private String carId; // 车辆唯一标识
    private String LabelColor;  // 价格标签颜色
    private String labelContent;    // 价格标签内容
    private String carMileage; //剩余公里数



    private String carImgUrl;   //车辆图片地址
    private String carName;  //车辆名称


    private String carNumber; //车辆号码



    public static CarBaseBean getBeanFromInfo(UsecarCommon.CarBaseInfo currentCarBaseInfo) {
        if (currentCarBaseInfo == null)
            return null;
        CarBaseBean carBaseBean = new CarBaseBean();
        carBaseBean.setCarId(currentCarBaseInfo.getCarId());
        carBaseBean.setPricePerKm(currentCarBaseInfo.getPricePerKm());
        carBaseBean.setPricePerMinute(currentCarBaseInfo.getPricePerMinute());
        carBaseBean.setLabelColor(currentCarBaseInfo.getLabelColor());
        carBaseBean.setLabelContent(currentCarBaseInfo.getLabelContent());
        carBaseBean.setCarName(currentCarBaseInfo.getBrand());
        carBaseBean.setCarNumber(currentCarBaseInfo.getCarLicense());
        carBaseBean.setCarImgUrl(currentCarBaseInfo.getCarImgUrl());
        carBaseBean.setCarMileage(currentCarBaseInfo.getEndurance()+"");
        return carBaseBean;
    }

    public float getPricePerKm() {
        return pricePerKm;
    }

    public void setPricePerKm(float pricePerKm) {
        this.pricePerKm = pricePerKm;
    }

    public float getPricePerMinute() {
        return pricePerMinute;
    }

    public void setPricePerMinute(float pricePerMinute) {
        this.pricePerMinute = pricePerMinute;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public String getLabelColor() {
        return LabelColor;
    }

    public void setLabelColor(String labelColor) {
        LabelColor = labelColor;
    }

    public String getLabelContent() {
        return labelContent;
    }

    public void setLabelContent(String labelContent) {
        this.labelContent = labelContent;
    }

    public String getCarMileage() {
        return carMileage;
    }

    public void setCarMileage(String carMileage) {
        this.carMileage = carMileage;
    }

    public String getCarImgUrl() {
        return carImgUrl;
    }

    public void setCarImgUrl(String carImgUrl) {
        this.carImgUrl = carImgUrl;
    }

    public String getCarName() {
        return carName;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(this.pricePerKm);
        dest.writeFloat(this.pricePerMinute);
        dest.writeString(this.carId);
        dest.writeString(this.LabelColor);
        dest.writeString(this.labelContent);
        dest.writeString(this.carMileage);
        dest.writeString(this.carImgUrl);
        dest.writeString(this.carName);
        dest.writeString(this.carNumber);
    }

    public CarBaseBean() {
    }

    protected CarBaseBean(Parcel in) {
        this.pricePerKm = in.readFloat();
        this.pricePerMinute = in.readFloat();
        this.carId = in.readString();
        this.LabelColor = in.readString();
        this.labelContent = in.readString();
        this.carMileage = in.readString();
        this.carImgUrl = in.readString();
        this.carName = in.readString();
        this.carNumber = in.readString();
    }

    public static final Creator<CarBaseBean> CREATOR = new Creator<CarBaseBean>() {
        @Override
        public CarBaseBean createFromParcel(Parcel source) {
            return new CarBaseBean(source);
        }

        @Override
        public CarBaseBean[] newArray(int size) {
            return new CarBaseBean[size];
        }
    };
}
