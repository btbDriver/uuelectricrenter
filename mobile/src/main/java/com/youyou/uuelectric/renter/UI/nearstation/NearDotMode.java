package com.youyou.uuelectric.renter.UI.nearstation;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: qing
 * Date: 2015/9/15 11:50
 * Desc: 附近网点模型，用于Intent中的数据传递
 */
public class NearDotMode implements Parcelable {

    private String dotName;
    private String dotDesc;
    private int distance;
    private int carTotal;
    private String dotId;
    private double dotLat;
    private double dotLon;

    public String getDotName() {
        return dotName;
    }

    public void setDotName(String dotName) {
        this.dotName = dotName;
    }

    public String getDotDesc() {
        return dotDesc;
    }

    public void setDotDesc(String dotDesc) {
        this.dotDesc = dotDesc;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getCarTotal() {
        return carTotal;
    }

    public void setCarTotal(int carTotal) {
        this.carTotal = carTotal;
    }

    public String getDotId() {
        return dotId;
    }

    public void setDotId(String dotId) {
        this.dotId = dotId;
    }

    public double getDotLat() {
        return dotLat;
    }

    public void setDotLat(double dotLat) {
        this.dotLat = dotLat;
    }

    public double getDotLon() {
        return dotLon;
    }

    public void setDotLon(double dotLon) {
        this.dotLon = dotLon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.dotName);
        dest.writeString(this.dotDesc);
        dest.writeInt(this.distance);
        dest.writeInt(this.carTotal);
        dest.writeString(this.dotId);
        dest.writeDouble(this.dotLat);
        dest.writeDouble(this.dotLon);
    }

    public NearDotMode() {
    }

    protected NearDotMode(Parcel in) {
        this.dotName = in.readString();
        this.dotDesc = in.readString();
        this.distance = in.readInt();
        this.carTotal = in.readInt();
        this.dotId = in.readString();
        this.dotLat = in.readDouble();
        this.dotLon = in.readDouble();
    }

    public static final Creator<NearDotMode> CREATOR = new Creator<NearDotMode>() {
        public NearDotMode createFromParcel(Parcel source) {
            return new NearDotMode(source);
        }

        public NearDotMode[] newArray(int size) {
            return new NearDotMode[size];
        }
    };
}
