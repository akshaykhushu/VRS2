package com.bazr2.aksha.newb;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.AdapterView;

import com.bumptech.glide.Glide;
import com.bazr2.aksha.newb.*;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<MarkerInfo> listItems;
    ArrayList<Double> distanceListItems;
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference reference;
    File outputFile;
    ImageView imageView;
    private StorageReference storageReference;
    Context currentContext;
    ProgressDialog progressDialog;
    StorageReference url;
    File outpurtDir;
    int selected;
    TextView textViewName;
    TextView textViewDistance;
    TextView textViewCost;
    View view;
    Double myLatitude;
    Double myLongitude;
    Double thresholdDistance = 10.0;

    public void MapView(View view){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        listView = findViewById(R.id.dynamicList);
        listItems = new ArrayList<>(MapsActivity.markerInfoMap.values());
        distanceListItems = new ArrayList<>(MapsActivity.markerInfoDistanceMap.values());

        //******************************************************************************************

        try {
            for (int i = 0; i < distanceListItems.size(); i++) {
                for (int j = i + 1; j < distanceListItems.size(); j++) {
                    if (distanceListItems.get(i) > distanceListItems.get(j)) {
                        double tempNum = distanceListItems.get(i);
                        MarkerInfo tempMarkerInfo = listItems.get(i);


                        distanceListItems.add(i, distanceListItems.get(j));
                        listItems.add(i, listItems.get(j));


                        distanceListItems.remove(i + 1);
                        listItems.remove(i + 1);

                        distanceListItems.add(j, tempNum);
                        listItems.add(j, tempMarkerInfo);

                        distanceListItems.remove(j + 1);
                        listItems.remove(j + 1);
                    }
                }
            }
        }
        catch(IndexOutOfBoundsException e){

        }

        //******************************************************************************************


        EditText et = findViewById(R.id.editTextSearchBar);
        et.setFocusableInTouchMode(true);
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });
        currentContext = getApplicationContext();
        CustomAdapter customAdapter = new CustomAdapter();
        listView.setAdapter(customAdapter);
        listView.setItemsCanFocus(false);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                finish();
                Intent intent = new Intent(getApplicationContext(), MakerClickedLayout.class);
                intent.putStringArrayListExtra("Bitmap", listItems.get(position).getBitmapUrl());
                intent.putExtra("Title",listItems.get(position).getTitle());
                intent.putExtra("Description", listItems.get(position).getDescriptionList());
                intent.putExtra("Cost",listItems.get(position).getCostList());
                intent.putExtra("Longitude", listItems.get(position).getLongitude());
                intent.putExtra("Latitude", listItems.get(position).getLatitude());
                intent.putExtra("Reported", listItems.get(position).getReport());
                intent.putExtra("Id", listItems.get(position).getId());
                startActivity(intent);
            }
        });

    }

    class CustomAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return MapsActivity.markerInfoMap.size();
        }

        @Override
        public Object getItem(int position) {
            return 0;

        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            view = getLayoutInflater().inflate(R.layout.listitem, null);
            imageView = view.findViewById(R.id.imageViewListItem);
            textViewName = view.findViewById(R.id.textViewListItemName);
            textViewCost = view.findViewById(R.id.textViewListItemCost);
//            textViewDistance = view.findViewById(R.id.textViewDistance);
            GPSTracker tracker = new GPSTracker(getApplicationContext());
            if (!tracker.canGetLocation()) {
                tracker.showSettingsAlert();
            } else {
                myLatitude = tracker.getLatitude();
                myLongitude = tracker.getLongitude();
            }
            float[] distance = new float[10];
            Location.distanceBetween(myLatitude, myLongitude, Double.parseDouble(listItems.get(position).getLatitude()), Double.parseDouble(listItems.get(position).getLongitude()), distance);
            Glide.with(ListActivity.this).load(Uri.parse(listItems.get(position).getBitmapUrl().get(0))).into(imageView);

            double distMiles = (double) distance[0] * 0.000621371;
            String dist = new DecimalFormat("#.##").format(Double.valueOf(distMiles));

            Double distDouble = Double.parseDouble(dist);
//            if (distDouble > thresholdDistance){
//                return null;
//            }
//            Picasso.with(ListActivity.this).load(Uri.parse(listItems.get(position).getBitmapUrl().get(0))).into(imageView);
//            textViewDistance.setText(" | " + dist + " Mi");
            if (listItems.get(position).getTitle().length() > 15){
                StringBuffer one = new StringBuffer();
                one.append(listItems.get(position).getTitle().substring(0,15));
                one.append("...");
                textViewName.setText(one.toString());
            }
            else{
                textViewName.setText(listItems.get(position).getTitle());
            }
            textViewCost.setText(listItems.get(position).getCostList().get(0) + " | " + dist + "Mi");
            return view;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
