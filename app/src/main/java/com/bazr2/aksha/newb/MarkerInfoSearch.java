package com.bazr2.aksha.newb;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.ArrayList;

public class MarkerInfoSearch implements ClusterItem {
    ArrayList<String> bitmapUrl;
    ArrayList<String> costList;
    ArrayList<String> descriptionList;
    String longitude;
    String latitude;
    String id;
    String title;
    Integer totalImages;

    public Integer getTotalImages() {
        return totalImages;
    }

    public void setTotalImages(Integer totalImages) {
        this.totalImages = totalImages;
    }

    @Override
    public LatLng getPosition() {
        LatLng latLng = new LatLng(Double.parseDouble(getLatitude()), Double.parseDouble(getLongitude()));
        return latLng;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<String> getBitmapUrl() {
        return bitmapUrl;
    }

    public void setBitmapUrl(ArrayList<String> bitmapUrl) {
        this.bitmapUrl = bitmapUrl;
    }

    public ArrayList<String> getCostList() {
        return costList;
    }

    public void setCostList(ArrayList<String> costList) {
        this.costList = costList;
    }

    public ArrayList<String> getDescriptionList() {
        return descriptionList;
    }

    public void setDescriptionList(ArrayList<String> descriptionList) {
        this.descriptionList = descriptionList;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
