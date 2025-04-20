package com.example.myplantcare.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class StatisticItem implements Parcelable {
    private String treeName;
    private List<ChartData> chartData;

    public StatisticItem(String treeName, List<ChartData> chartData) {
        this.treeName = treeName;
        this.chartData = chartData;
    }

    protected StatisticItem(Parcel in) {
        treeName = in.readString();
        chartData = in.createTypedArrayList(ChartData.CREATOR);  // Đọc dữ liệu chartData
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(treeName);
        dest.writeTypedList(chartData);  // Ghi dữ liệu chartData vào Parcel
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<StatisticItem> CREATOR = new Creator<StatisticItem>() {
        @Override
        public StatisticItem createFromParcel(Parcel in) {
            return new StatisticItem(in);
        }

        @Override
        public StatisticItem[] newArray(int size) {
            return new StatisticItem[size];
        }
    };

    public String getTreeName() {
        return treeName;
    }

    public List<ChartData> getChartData() {
        return chartData;
    }
}

