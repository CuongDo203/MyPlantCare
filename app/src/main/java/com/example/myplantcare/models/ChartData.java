package com.example.myplantcare.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ChartData implements Parcelable {
    private float xValue;
    private float yValue;

    public ChartData(float xValue, float yValue) {
        this.xValue = xValue;
        this.yValue = yValue;
    }

    protected ChartData(Parcel in) {
        xValue = in.readFloat();
        yValue = in.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloat(xValue);
        dest.writeFloat(yValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChartData> CREATOR = new Creator<ChartData>() {
        @Override
        public ChartData createFromParcel(Parcel in) {
            return new ChartData(in);
        }

        @Override
        public ChartData[] newArray(int size) {
            return new ChartData[size];
        }
    };

    public float getXValue() {
        return xValue;
    }

    public float getYValue() {
        return yValue;
    }
}