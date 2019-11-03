package com.bazr2.aksha.newb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.IconGenerator;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

public class MapsActivitySearch extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    public ClusterManager<MarkerInfoSearch> mClusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_search);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        EditText et = findViewById(R.id.editTextSearchBar);
        et.setText(getIntent().getStringExtra("Search"));


        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET, Manifest.permission.CAMERA
            }, 200);
            return;
        }

        mMap.setMyLocationEnabled(true);

        mClusterManager = new ClusterManager<>(this, mMap);
        mClusterManager.setRenderer(new ClusterRenderer(getApplicationContext(), mMap, mClusterManager));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);

        View locationButton = ((View) findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();

        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);

        rlp.addRule(RelativeLayout.ALIGN_PARENT_END, 0);
        rlp.addRule(RelativeLayout.ALIGN_END, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlp.setMargins(30, 0, 0, 40);

        for (String id : SearchActivity.hashMap.keySet()) {
            MarkerInfoSearch markerInfoSearch = SearchActivity.hashMap.get(id);
            setMarker(markerInfoSearch);
        }
    }

    public void ListView(View view){
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void setMarker(MarkerInfoSearch markerInfo) {

        LatLng current = new LatLng(Double.parseDouble(markerInfo.getLatitude()), Double.parseDouble(markerInfo.getLongitude()));
        IconGenerator iconGenerator = new IconGenerator(this);
////        iconGenerator.setColor(R.color.Green);
//        iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
//        iconGenerator.setTextAppearance(R.style.iconGenText);
//        Bitmap iconBitmap = iconGenerator.makeIcon(markerInfo.getTitle() + " | " + markerInfo.getCost());
//        MarkerOptions mo = new MarkerOptions().position(current).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).title(markerInfo.getId());
        mClusterManager.addItem(markerInfo);
        mClusterManager.cluster();

//        Marker marker = mMap.addMarker(mo);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerInfoSearch>() {
            @Override
            public boolean onClusterItemClick(MarkerInfoSearch markerInfoSearch) {
                Intent intent = new Intent(getApplicationContext(), MakerClickedLayout.class);
                intent.putStringArrayListExtra("Bitmap", markerInfoSearch.getBitmapUrl());
                intent.putExtra("Title", markerInfoSearch.getTitle());
                intent.putStringArrayListExtra("Description", markerInfoSearch.getDescriptionList());
                intent.putStringArrayListExtra("Cost", markerInfoSearch.getCostList());
                intent.putExtra("Latitude", markerInfoSearch.getLatitude());
                intent.putExtra("Longitude", markerInfoSearch.getLongitude());
                intent.putExtra("TotalImages", markerInfoSearch.getTotalImages());
                intent.putExtra("Reported", markerInfoSearch.getReported());
                intent.putExtra("State", markerInfoSearch.getState());
                intent.putExtra("Id", markerInfoSearch.getId());
                startActivity(intent);
                return true;
            }
        });

        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerInfoSearch>() {
            @Override
            public boolean onClusterClick(Cluster<MarkerInfoSearch> cluster) {

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        cluster.getPosition(), (float) Math.floor(mMap
                                .getCameraPosition().zoom + 2)), 300,
                        null);
                return true;
            }
        });
    }
}
