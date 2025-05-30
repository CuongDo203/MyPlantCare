package com.example.myplantcare.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class ChartData implements Parcelable {
    private Date date;
    private float yValue;
    private String label;

    public ChartData(Date date, float yValue, String label) {
        this.date = date;
        this.yValue = yValue;
        this.label = label;
    }

    protected ChartData(Parcel in) {
        long timeMillis = in.readLong();
        this.date = new Date(timeMillis);
        this.yValue = in.readFloat();
        this.label = in.readString();
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
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(date.getTime());
        dest.writeFloat(yValue);
        dest.writeString(label);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // Getters
    public Date getDate() {
        return date;
    }

    public float getYValue() {
        return yValue;
    }

    public String getLabel() {
        return label != null ? label : "";
    }
}
