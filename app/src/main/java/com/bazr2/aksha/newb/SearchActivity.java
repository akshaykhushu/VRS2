package com.bazr2.aksha.newb;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {

    EditText searchField;
    ImageButton searchButton;
    RecyclerView resultList;
    DatabaseReference databaseReference;
    ArrayList<String> titleList;
    ArrayList<ArrayList<String>> costList2D;
    ArrayList<ArrayList<String>> bitmapList2D;
    ArrayList<String> bitmapList;
    ArrayList<String> costList;
    ArrayList<String> descriptionList;
    ArrayList<ArrayList<String>> descriptionList2D;
    ArrayList<String> uidList;
    ArrayList<String> latiList;
    ArrayList<String> longList;
    ArrayList<String> stateList;
    ArrayList<String> reportedList;
    Double myLatitude;
    Double myLongitude;
    ArrayList<Double> distanceList;
    SearchAdapter searchAdapter;
    public static HashMap<String, MarkerInfoSearch> hashMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchField = findViewById(R.id.editTextSearchBar);
        resultList = findViewById(R.id.dynamicList);
        resultList.setHasFixedSize(true);
        resultList.setLayoutManager(new LinearLayoutManager(this));
        resultList.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        databaseReference = FirebaseDatabase.getInstance().getReference();

        titleList = new ArrayList<>();
        costList2D = new ArrayList<ArrayList<String>>();
        bitmapList2D = new ArrayList<ArrayList<String>>();
        descriptionList2D = new ArrayList<ArrayList<String>>();
        hashMap = new HashMap<>();
        bitmapList = new ArrayList<>();
        descriptionList = new ArrayList<>();
        costList = new ArrayList<>();
        latiList = new ArrayList<>();
        distanceList = new ArrayList<>();
        longList = new ArrayList<>();
        uidList = new ArrayList<>();
        stateList = new ArrayList<>();
        reportedList = new ArrayList<>();


        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if(!s.toString().isEmpty()){
                    setAdapter(s.toString());
                }
                else {
                    titleList.clear();
                    costList2D.clear();
                    bitmapList.clear();
                    distanceList.clear();
                    descriptionList2D.clear();
                    latiList.clear();
                    uidList.clear();
                    longList.clear();
                    bitmapList2D.clear();
                    costList.clear();
                    descriptionList.clear();
                    stateList.clear();
                    reportedList.clear();
                    resultList.removeAllViews();
                    hashMap.clear();
                }

            }
        });
    }

    public void MapView(View view){

        if (searchField.getText().toString().equals("") ){
            super.onBackPressed();
        }
        else{
            Intent intent = new Intent(this, MapsActivitySearch.class);
            intent.putExtra("Search", searchField.getText().toString());
            startActivity(intent);
        }
    }

    private void setAdapter(final String searchedString) {

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int counter = 0;

                titleList.clear();
                costList.clear();
                bitmapList.clear();
                descriptionList.clear();
                descriptionList2D.clear();
                costList2D.clear();
                bitmapList2D.clear();
                latiList.clear();
                longList.clear();
                uidList.clear();
                stateList.clear();
                reportedList.clear();
                distanceList.clear();
                hashMap.clear();
                resultList.removeAllViews();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    bitmapList = new ArrayList<>();
                    descriptionList = new ArrayList<>();
                    costList = new ArrayList<>();
                    String title = snapshot.child("Title").getValue(String.class);
//                    String cost = snapshot.child("Cost").getValue(String.class);
                    //String bitmap = snapshot.child("Bitmap").getValue(String.class);
//                    String description = snapshot.child("Description").getValue(String.class);
                    String longitude = snapshot.child("LocationLong").getValue(String.class);
                    String latitude = snapshot.child("LocationLati").getValue(String.class);
                    String id = snapshot.child("Id").getValue(String.class);
                    String state = snapshot.child("State").getValue(String.class);
                    String reported = snapshot.child("Reported").getValue(String.class);
                    Integer totalImages = Integer.parseInt(snapshot.child("TotalImages").getValue().toString());
                    for (int i=0; i < totalImages; i++){
                        bitmapList.add(snapshot.child("Bitmap"+i).getValue().toString());
                        descriptionList.add(snapshot.child("Description"+i).getValue().toString());
                        costList.add(snapshot.child("Cost"+i).getValue().toString());
                    }


                    MarkerInfoSearch markerInfoSearch = new MarkerInfoSearch();
                    markerInfoSearch.setBitmapUrl(bitmapList);
                    markerInfoSearch.setTitle(title);
                    markerInfoSearch.setDescriptionList(descriptionList);
                    markerInfoSearch.setLatitude(latitude);
                    markerInfoSearch.setLongitude(longitude);
                    markerInfoSearch.setCostList(costList);
                    markerInfoSearch.setId(id);
                    markerInfoSearch.setState(state);
                    markerInfoSearch.setReported(reported);
                    markerInfoSearch.setTotalImages(totalImages);


                    GPSTracker tracker = new GPSTracker(getApplicationContext());
                    if (!tracker.canGetLocation()) {
                        tracker.showSettingsAlert();
                    } else {
                        myLatitude = tracker.getLatitude();
                        myLongitude = tracker.getLongitude();
                    }
                    float[] distance = new float[10];

                    if(title.toLowerCase().contains(searchedString.toLowerCase())){
                        Location.distanceBetween(myLatitude, myLongitude, Double.parseDouble(latitude), Double.parseDouble(longitude), distance);
                        double distMiles = (double) distance[0] * 0.000621371;
                        String dist = new DecimalFormat("#.##").format(Double.valueOf(distMiles));
                        Double distDouble = Double.parseDouble(dist);
                        distanceList.add(distDouble);
                        titleList.add(title);
                        costList2D.add(costList);
                        bitmapList2D.add(bitmapList);
                        descriptionList2D.add(descriptionList);
                        latiList.add(latitude);
                        longList.add(longitude);
                        uidList.add(id);
                        stateList.add(state);
                        reportedList.add(reported);
                        hashMap.put(id, markerInfoSearch);
                        counter++;
                    }else{
                        for(int i=0;i<totalImages;i++){
                            String description = descriptionList.get(i);
                            if (description.toLowerCase().contains(searchedString.toLowerCase())){
                                Location.distanceBetween(myLatitude, myLongitude, Double.parseDouble(latitude), Double.parseDouble(longitude), distance);
                                double distMiles = (double) distance[0] * 0.000621371;
                                String dist = new DecimalFormat("#.##").format(Double.valueOf(distMiles));
                                Double distDouble = Double.parseDouble(dist);
                                distanceList.add(distDouble);
                                titleList.add(title);
                                costList2D.add(costList);
                                bitmapList2D.add(bitmapList);
                                descriptionList2D.add(descriptionList);
                                latiList.add(latitude);
                                longList.add(longitude);
                                uidList.add(id);
                                stateList.add(state);
                                reportedList.add(reported);
                                hashMap.put(id, markerInfoSearch);
                                counter++;
                                break;
                            }
                        }
                    }
                    if(counter == 15)
                        break;
                }

                searchAdapter = new SearchAdapter(SearchActivity.this, titleList, costList2D, bitmapList2D, descriptionList2D, uidList, latiList, longList,  stateList, reportedList, distanceList);
                resultList.setAdapter(searchAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
