package com.example.myplantcare.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class StatisticItem implements Parcelable {
    private String treeName;
    private List<ChartData> heights;
    private List<ChartData> leaf;
    private List<ChartData> flower;
    private List<ChartData> fruit;


    public StatisticItem(String treeName,
                         List<ChartData> heights,
                         List<ChartData> leaf,
                         List<ChartData> flower,
                         List<ChartData> fruit) {
        this.treeName = treeName;
        this.heights  = heights;
        this.leaf     = leaf;
        this.flower   = flower;
        this.fruit    = fruit;
    }

    protected StatisticItem(Parcel in) {
        treeName = in.readString();
        heights  = in.createTypedArrayList(ChartData.CREATOR);
        leaf     = in.createTypedArrayList(ChartData.CREATOR);
        flower   = in.createTypedArrayList(ChartData.CREATOR);
        fruit    = in.createTypedArrayList(ChartData.CREATOR);
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

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(treeName);
        dest.writeTypedList(heights);
        dest.writeTypedList(leaf);
        dest.writeTypedList(flower);
        dest.writeTypedList(fruit);
    }

    // getters
    public String getTreeName() { return treeName; }
    public List<ChartData> getHeights() { return heights; }
    public List<ChartData> getLeaf() { return leaf; }
    public List<ChartData> getFlower() { return flower; }
    public List<ChartData> getFruit() { return fruit; }
}
