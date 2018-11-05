package com.bazr2.aksha.newb;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.SyncStateContract;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public  class ClusterRenderer<T extends ClusterItem> extends DefaultClusterRenderer<T> {
    Context context;
    ClusterManager<T> clusterManager;
    ClusterRenderer(Context context, GoogleMap map, ClusterManager<T> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.clusterManager = clusterManager;
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<T> cluster) {
        //start clustering if 2 or more items overlap
        return cluster.getSize() > 1;
    }

    @Override
    protected void onBeforeClusterItemRendered(T item,
                                               MarkerOptions markerOptions) {


        try{
            MarkerInfo markerInfo = (MarkerInfo) item;
            IconGenerator iconGenerator = new IconGenerator(context);
            iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
            iconGenerator.setTextAppearance(R.style.iconGenText);
            Bitmap iconBitmap = iconGenerator.makeIcon(markerInfo.getTitle() + " | " + markerInfo.getCost());
//        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(iconBitmap);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
        }catch(ClassCastException e){
            MarkerInfoSearch markerInfo = (MarkerInfoSearch) item;
            IconGenerator iconGenerator = new IconGenerator(context);
            iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
            iconGenerator.setTextAppearance(R.style.iconGenText);
            Bitmap iconBitmap = iconGenerator.makeIcon(markerInfo.getTitle() + " | " + markerInfo.getCost());
//        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(iconBitmap);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
        }


    }

    @Override
    public void setOnClusterItemClickListener(ClusterManager.OnClusterItemClickListener<T> listener) {
        super.setOnClusterItemClickListener(listener);

        }

    @Override
    protected int getColor(int clusterSize) {
        return Color.parseColor("#FF8C00");
    }

    @Override
    protected void onClusterItemRendered(T clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

    }

    @Override
    protected void onClusterRendered(Cluster<T> cluster, Marker marker) {
        super.onClusterRendered(cluster, marker);
//        IconGenerator iconGenerator = new IconGenerator(context);
//        iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
//        iconGenerator.setTextAppearance(R.style.iconGenText);
//        Bitmap iconBitmap = iconGenerator.makeIcon(String.valueOf(cluster.getSize()));
//        marker.setIcon(BitmapDescriptorFactory.fromBitmap(iconBitmap));
//        marker.setVisible(false);
    }
}