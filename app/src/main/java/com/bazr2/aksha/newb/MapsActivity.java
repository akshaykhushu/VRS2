package com.bazr2.aksha.newb;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.ui.BubbleIconFactory;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    public static GoogleMap mMap;
    private Location currentLocation;
    public static int upload = 0;
    protected Bitmap bitmap, circularBitmap;
    protected String title;
    protected String description;
    protected String cost;
    public static Map<String, MarkerInfo> markerInfoMap;
    public static Map<String, Double> markerInfoDistanceMap;
    public static ClusterManager<MarkerInfo> mClusterManager;
//    ClusterManager<MarkerItem> clusterManager;
    int count=0;


    public static String UserId;
    Double myLatitude;
    Double myLongitude;
    Uri imageUri;
    private FirebaseAuth firebaseAuth;
    ImageButton imgNavButton;

    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    DatabaseReference reference;
    public void setCurrentLocation(Location current) {
        this.currentLocation = current;
    }
    private static final int CONTENT_REQUEST=1337;
    private File output=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        markerInfoMap = new HashMap<>();
        markerInfoMap.clear();
        firebaseAuth = FirebaseAuth.getInstance();
        //mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        setNavigationViewListner();
        UserId = getIntent().getStringExtra("UserId");
        FloatingActionButton b = findViewById(R.id.CameraActionButton);
        markerInfoDistanceMap = new HashMap<>();
        markerInfoDistanceMap.clear();
        final NavigationView navigationView =  findViewById(R.id.navigation_view);
        View hView =  navigationView.getHeaderView(0);
        TextView nav_user = hView.findViewById(R.id.textViewUserId);
        nav_user.setText(firebaseAuth.getCurrentUser().getEmail());
        EditText et = findViewById(R.id.editTextSearchBar);

        imgNavButton = findViewById(R.id.imageButton);
        imgNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = findViewById(R.id.drwaerLayout);
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        GPSTracker tracker = new GPSTracker(getApplicationContext());
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {
            myLatitude = tracker.getLatitude();
            myLongitude = tracker.getLongitude();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET, Manifest.permission.CAMERA,android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
            }, 200);
            return;
        }

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = Environment.getExternalStorageDirectory().getPath() + "/test/testfile.jpg";
                File file =  new File(filename);
                imageUri = FileProvider.getUriForFile(getApplicationContext(),"com.bazr2.android.fileprovider", file );
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, 123);
            }
        });


        reference = firebaseDatabase.getReference();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                markerInfoMap.clear();
                markerInfoDistanceMap.clear();
                mClusterManager.clearItems();
                mMap.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MarkerInfo markerInfo = new MarkerInfo();
                    ArrayList<String> bitmapUrl = new ArrayList<>();
                    String userId = new String();
                    try {
                        userId = snapshot.child("Id").getValue().toString();
                        markerInfo.setTotalImages(Integer.parseInt(snapshot.child("TotalImages").getValue().toString()));
                        for (int i=0; i < markerInfo.getTotalImages(); i++){
                            bitmapUrl.add(snapshot.child("Bitmap"+i).getValue().toString());
                        }
                        markerInfo.setBitmapUrl(bitmapUrl);
                        markerInfo.setDescription(snapshot.child("Description").getValue().toString());
                        markerInfo.setCost(snapshot.child("Cost").getValue().toString());
                        cost = snapshot.child("Cost").getValue().toString();
                        markerInfo.setLongitude(snapshot.child("LocationLong").getValue().toString());
                        markerInfo.setLatitude(snapshot.child("LocationLati").getValue().toString());
                        markerInfo.setId(snapshot.child("Id").getValue().toString());
                        markerInfo.setTitle(snapshot.child("Title").getValue().toString());
                        title = snapshot.child("Title").getValue().toString();


                        float[] distance = new float[10];
                        Location.distanceBetween(myLatitude, myLongitude, Double.parseDouble(markerInfo.getLatitude()), Double.parseDouble(markerInfo.getLongitude()), distance);
                        double distMiles = (double) distance[0] * 0.000621371;
                        String dist = new DecimalFormat("#.##").format(Double.valueOf(distMiles));

                        Double distDouble = Double.parseDouble(dist);

                        if (!markerInfoMap.containsKey(userId)){
                            MapsActivity.markerInfoMap.put(userId, markerInfo);
                            MapsActivity.markerInfoDistanceMap.put(userId, distDouble);
                        }
                        setMarker(markerInfo);
                    }catch(Exception e){
                        Log.e("Exception Caught","UserId Not found");
                    }



                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("Value from DB", "OnCancelledCalled");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent(this, Info.class);
                intent.putExtra("Image", imageUri.toString());
                startActivity(intent);
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "There was some problem while Taking the Picture. Try Again", Toast.LENGTH_SHORT).show();
        }

    }

    public void refresh(){
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
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
        mMap.clear();
        mClusterManager.clearItems();
        mClusterManager.cluster();
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
        rlp.setMargins(100, 0, 0, 200);


        GPSTracker tracker = new GPSTracker(this);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {
            myLatitude = tracker.getLatitude();
            myLongitude = tracker.getLongitude();
        }

//        clusterManager = new ClusterManager<MarkerItem>(getApplicationContext(), mMap);
//        ClusterRenderer clusterRenderer = new ClusterRenderer(this, mMap, clusterManager);
//        addItems(myLatitude, myLongitude);

        LatLng myLocation = new LatLng(myLatitude, myLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));


    }

    public void ListView(View view){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }


    public void setMarker(MarkerInfo markerInfo) {

        LatLng current = new LatLng(Double.parseDouble(markerInfo.getLatitude()), Double.parseDouble(markerInfo.getLongitude()));
//        IconGenerator iconGenerator = new IconGenerator(this);
//        iconGenerator.setStyle(IconGenerator.STYLE_ORANGE);
//        iconGenerator.setTextAppearance(R.style.iconGenText);
//        Bitmap iconBitmap = iconGenerator.makeIcon(title + " | " + cost);
//        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(iconBitmap);
//        markerInfo.setIcon(bitmapDescriptor);
//        MarkerOptions mo = new MarkerOptions().position(current).icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).title(markerInfo.getId());
//        Marker marker = mMap.addMarker(mo);
        mClusterManager.addItem(markerInfo);
        mClusterManager.cluster();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerInfo>() {
            @Override
            public boolean onClusterItemClick(MarkerInfo markerInfo) {
                Intent intent = new Intent(getApplicationContext(), MakerClickedLayout.class);
                intent.putExtra("TotalImages",markerInfo.getTotalImages());
                intent.putStringArrayListExtra("Bitmap", markerInfo.getBitmapUrl());
                intent.putExtra("Title", markerInfo.getTitle());
                intent.putExtra("Description", markerInfo.getDescription());
                intent.putExtra("Cost", markerInfo.getCost());
                intent.putExtra("Id", markerInfo.getId());
                intent.putExtra("Longitude", markerInfo.getLongitude());
                intent.putExtra("Latitude", markerInfo.getLatitude());
                startActivity(intent);
                return true;
            }
        });


        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MarkerInfo>() {
            @Override
            public boolean onClusterClick(Cluster<MarkerInfo> cluster) {

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        cluster.getPosition(), (float) Math.floor(mMap
                                .getCameraPosition().zoom + 2)), 300,
                        null);
                return true;
            }
        });
//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                String idMarker = marker.getTitle();
//                for (String id : markerInfoMap.keySet()) {
//                    if (id.equals(idMarker)){
//                        Intent intent = new Intent(getApplicationContext(), MakerClickedLayout.class);
//                        intent.putExtra("TotalImages", markerInfoMap.get(id).getTotalImages());
//                        intent.putStringArrayListExtra("Bitmap", markerInfoMap.get(id).getBitmapUrl());
//                        intent.putExtra("Title", markerInfoMap.get(id).getTitle());
//                        intent.putExtra("Description", markerInfoMap.get(id).getDescription());
//                        intent.putExtra("Cost", markerInfoMap.get(id).getCost());
//                        intent.putExtra("Id", markerInfoMap.get(id).getId());
//                        intent.putExtra("Longitude", markerInfoMap.get(id).getLongitude());
//                        intent.putExtra("Latitude", markerInfoMap.get(id).getLatitude());
//                        startActivity(intent);
//                    }
//                }
//                return true;
//            }
//        });


        LatLng myLocation = new LatLng(myLatitude, myLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

    }

    private void setNavigationViewListner() {
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {

            case R.id.nav_Help: {
                Intent intent = new Intent(getApplicationContext(), HelpActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.nav_Images: {
                String filename = Environment.getExternalStorageDirectory().getPath() + "/test/testfile.jpg";
                File file =  new File(filename);
                imageUri = FileProvider.getUriForFile(getApplicationContext(),"com.bazr2.android.fileprovider", file );
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(cameraIntent, 123);
                break;
            }

            case R.id.nav_ListView :{
                Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(intent);
                break;
            }

            case R.id.nav_TandC :{
                Intent intent = new Intent(getApplicationContext(), TermsAndConditions.class);
                startActivity(intent);
                break;
            }

//            case R.id.nav_Categories : {
//                Intent intent = new Intent(getApplicationContext(), CategoryActivity.class);
//                startActivity(intent);
//                break;
//            }

            case R.id.nav_LogOut  :{
                firebaseAuth.signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                finish();
                startActivity(intent);
                break;
            }
        }

        DrawerLayout drawerLayout = findViewById(R.id.drwaerLayout);
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}






