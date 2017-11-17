package com.youyou.uuelectric.renter.UI.nearstation;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * User: qing
 * Date: 2015/9/14 17:45
 * Desc:
 */
public class NearDotListMode implements Parcelable {

    public List<NearDotMode> getNearDotModes() {
        return nearDotModes;
    }

    public void setNearDotModes(List<NearDotMode> nearDotModes) {
        this.nearDotModes = nearDotModes;
    }

    List<NearDotMode> nearDotModes;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(nearDotModes);
    }

    public NearDotListMode() {
    }

    protected NearDotListMode(Parcel in) {
        this.nearDotModes = in.createTypedArrayList(NearDotMode.CREATOR);
    }

    public static final Creator<NearDotListMode> CREATOR = new Creator<NearDotListMode>() {
        public NearDotListMode createFromParcel(Parcel source) {
            return new NearDotListMode(source);
        }

        public NearDotListMode[] newArray(int size) {
            return new NearDotListMode[size];
        }
    };
}
