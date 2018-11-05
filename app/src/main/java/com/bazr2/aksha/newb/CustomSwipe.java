package com.bazr2.aksha.newb;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class CustomSwipe extends PagerAdapter{

    public Context context;
    public ArrayList<String> bitmapUrl;
    public LayoutInflater layoutInflater;
    public ImageView imageView;

    public CustomSwipe(Context context, ArrayList<String> bitmapUrl) {
        this.context = context;
        this.bitmapUrl = bitmapUrl;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = layoutInflater.inflate(R.layout.activity_maker_clicked_layout, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.markerClickedImageView);
        Glide.with(context).load(bitmapUrl.get(position)).into(imageView);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view ==object);
    }
}