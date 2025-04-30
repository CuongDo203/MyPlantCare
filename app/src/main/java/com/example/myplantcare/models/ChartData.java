package com.example.myplantcare.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ChartData implements Parcelable {
    private int xValue;
    private float yValue;
    private String label;

    public ChartData(int xValue, float yValue,String label) {
        this.xValue = xValue;
        this.yValue = yValue;
        this.label = label;
    }

    protected ChartData(Parcel in) {
        xValue = in.readInt();
        yValue = in.readFloat();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(xValue);
        dest.writeFloat(yValue);
    }

    // getters
    public int getXValue() { return xValue; }
    public float getYValue() { return yValue; }
    public String getLabel() { return label != null ? label : ""; }
}
