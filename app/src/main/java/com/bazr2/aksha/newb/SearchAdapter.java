package com.bazr2.aksha.newb;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder>{

    Context context;
    ArrayList<String> titleList;
    ArrayList<ArrayList<String>> costList2D;
    ArrayList<ArrayList<String>> bitmapList2D;
    ArrayList<String> bitmapList;
    ArrayList<ArrayList<String>> descriptionList2D;
    ArrayList<String> uidList;
    ArrayList<String> latiList;
    ArrayList<Double> distanceList;
    ArrayList<String> longList;
    ArrayList<String> stateList;
    ArrayList<String> reportList;
    Double myLatitude;
    Double myLongitude;

    class SearchViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textViewName;
        TextView textViewCost;
//        TextView textViewDistance;

        public SearchViewHolder(final View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewListItemName);
            textViewCost = itemView.findViewById(R.id.textViewListItemCost);
//            textViewDistance=itemView.findViewById(R.id.textViewDistance);
            imageView = itemView.findViewById(R.id.imageViewListItem);

        }
    }

    public SearchAdapter(Context context, ArrayList<String> titleList, ArrayList<ArrayList<String>> costList2D, ArrayList<ArrayList<String>> bitmapList2D, ArrayList<ArrayList<String>> descriptionList2D, ArrayList<String> uidList,ArrayList<String> latiList, ArrayList<String> longList, ArrayList<String> stateList,ArrayList<String> reportList , ArrayList<Double> distanceList ) {
        this.context = context;
        this.titleList = titleList;
        this.costList2D = costList2D;
        this.bitmapList2D = bitmapList2D;
        this.descriptionList2D = descriptionList2D;
        this.uidList = uidList;
        this.latiList = latiList;
        this.longList = longList;
        this.distanceList = distanceList;
        this.stateList = stateList;
        this.reportList = reportList;

        try {
            for (int i = 0; i < distanceList.size(); i++) {
                for (int j = i + 1; j < distanceList.size(); j++) {
                    if (distanceList.get(i) > distanceList.get(j)) {
                        double tempNum = distanceList.get(i);
                        String tempTitle = titleList.get(i);
                        ArrayList<String> tempCostList = costList2D.get(i);
                        ArrayList<String> tempDescriptionList = descriptionList2D.get(i);
                        String tempUid = uidList.get(i);
                        String templati = latiList.get(i);
                        ArrayList<String> tempArrayList = bitmapList2D.get(i);
                        String templong = longList.get(i);
                        String tempState = stateList.get(i);
                        String tempReport = reportList.get(i);

                        distanceList.add(i, distanceList.get(j));
                        titleList.add(i, titleList.get(j));
                        costList2D.add(i, costList2D.get(j));
                        descriptionList2D.add(i, descriptionList2D.get(j));
                        uidList.add(i, uidList.get(j));
                        latiList.add(i, latiList.get(j));
                        longList.add(i, longList.get(j));
                        stateList.add(i, stateList.get(j));
                        reportList.add(i, reportList.get(j));
                        bitmapList2D.add(i, bitmapList2D.get(j));

                        distanceList.remove(i + 1);
                        titleList.remove(i + 1);
                        costList2D.remove(i + 1);
                        descriptionList2D.remove(i + 1);
                        uidList.remove(i + 1);
                        latiList.remove(i + 1);
                        longList.remove(i + 1);
                        stateList.remove(i + 1);
                        reportList.remove(i + 1);
                        bitmapList2D.remove(i + 1);


                        distanceList.add(j, tempNum);
                        titleList.add(j, tempTitle);
                        descriptionList2D.add(j, tempDescriptionList);
                        costList2D.add(j, tempCostList);
                        uidList.add(j, tempUid);
                        latiList.add(j, templati);
                        longList.add(j, templong);
                        stateList.add(j, tempState);
                        reportList.add(j, tempReport);
                        bitmapList2D.add(j, tempArrayList);

                        distanceList.remove(j + 1);
                        titleList.remove(j + 1);
                        costList2D.remove(j + 1);
                        descriptionList2D.remove(j + 1);
                        uidList.remove(j + 1);
                        latiList.remove(j + 1);
                        longList.remove(j + 1);
                        stateList.remove(j + 1);
                        reportList.remove(j + 1);
                        bitmapList2D.remove(j + 1);


                    }
                }
            }
        }
        catch(IndexOutOfBoundsException e){

        }


    }

    @Override
    public SearchAdapter.SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.listitem, parent, false);
        return new SearchAdapter.SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SearchViewHolder holder, final int position) {

        GPSTracker tracker = new GPSTracker(context);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {
            myLatitude = tracker.getLatitude();
            myLongitude = tracker.getLongitude();
        }
        float[] distance = new float[10];
        Location.distanceBetween(myLatitude, myLongitude, Double.parseDouble(latiList.get(position)),  Double.parseDouble(longList.get(position)), distance);

        double distMiles = (double) distance[0] * 0.000621371;
        String dist = new DecimalFormat("#.##").format(Double.valueOf(distMiles));

        Double distDouble = Double.parseDouble(dist);
        holder.textViewName.setText(titleList.get(position));
        holder.textViewCost.setText(costList2D.get(position).get(0)+ " | " + dist + " Mi");

        Glide.with(context).load(bitmapList2D.get(position).get(0)).into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), MakerClickedLayout.class);
                intent.putExtra("Title", titleList.get(position));
                intent.putExtra("Cost", costList2D.get(position));
                intent.putExtra("Description", descriptionList2D.get(position));
                intent.putStringArrayListExtra("Bitmap", bitmapList2D.get(position));
                intent.putExtra("Latitude", latiList.get(position));
                intent.putExtra("Longitude", longList.get(position));
                intent.putExtra("Id", uidList.get(position));
                intent.putExtra("Reported", reportList.get(position));
                intent.putExtra("State", stateList.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return titleList.size();
    }
}
