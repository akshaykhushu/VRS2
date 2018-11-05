package com.bazr2.aksha.newb;

import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

public class MarkerRenderer extends DefaultClusterRenderer<MarkerInfo> {

    Context context = null;
    public IconGenerator iconGenerator;
    public IconGenerator clusterIconGenerator;


    public MarkerRenderer(Context context, GoogleMap map, ClusterManager<MarkerInfo> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        iconGenerator = new IconGenerator(context);
        clusterIconGenerator = new IconGenerator(context);

        iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
        iconGenerator.setTextAppearance(R.style.iconGenText);

    }
}
