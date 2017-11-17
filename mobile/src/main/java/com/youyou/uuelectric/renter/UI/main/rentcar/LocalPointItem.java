package com.youyou.uuelectric.renter.UI.main.rentcar;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User: qing
 * Date: 2015/9/11 15:28
 * Desc: 保存本地搜索的历史数据
 */
public class LocalPointItem implements Parcelable  {

    public String address;
    public String snippet;
    public double lat;
    public double lng;

    public LocalPointItem(String address, String snippet, double lat, double lng) {
        this.address = address;
        this.snippet = snippet;
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof LocalPointItem) {
            LocalPointItem lpi = (LocalPointItem) o;
            if (lpi.getLat() == this.lat && lpi.getLng() == this.lng) {
                return true;
            }
        }
        return false;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.address);
        dest.writeString(this.snippet);
        dest.writeDouble(this.lat);
        dest.writeDouble(this.lng);
    }

    protected LocalPointItem(Parcel in) {
        this.address = in.readString();
        this.snippet = in.readString();
        this.lat = in.readDouble();
        this.lng = in.readDouble();
    }

    public static final Creator<LocalPointItem> CREATOR = new Creator<LocalPointItem>() {
        public LocalPointItem createFromParcel(Parcel source) {
            return new LocalPointItem(source);
        }

        public LocalPointItem[] newArray(int size) {
            return new LocalPointItem[size];
        }
    };
}
