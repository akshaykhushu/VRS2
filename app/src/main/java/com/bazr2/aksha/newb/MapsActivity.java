package com.bazr2.aksha.newb;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
    public Switch aSwitch;
    public Button stateButton;
    public boolean isGuest = false;
    int count=0;

    LatLng myLocation;


    public static int MapsTotalCount= 0;
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
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
        setNavigationViewListner();
        UserId = LoginActivity.userId;
        FloatingActionButton b = findViewById(R.id.CameraActionButton);


        stateButton = findViewById(R.id.stateButton);

        stateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (stateButton.getText().equals("I am open")){
                    stateButton.setText("I am closed");
                    firebaseDatabase.getReference(UserId).child("State").setValue("closed");
                }
                else{
                    stateButton.setText("I am open");
                    firebaseDatabase.getReference(UserId).child("State").setValue("open");
                }

            }
        });

        final NavigationView navigationView =  findViewById(R.id.navigation_view);
        View hView =  navigationView.getHeaderView(0);
        TextView nav_user = hView.findViewById(R.id.textViewUserId);
        if (LoginActivity.isGuest) {
            b.setVisibility(View.INVISIBLE);
            stateButton.setVisibility(View.INVISIBLE);
            nav_user.setText("Guest");
        }
        else{
            nav_user.setText(firebaseAuth.getCurrentUser().getEmail());
            stateButton.setVisibility(View.VISIBLE);
        }
        markerInfoDistanceMap = new HashMap<>();
        markerInfoDistanceMap.clear();


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


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET, Manifest.permission.CAMERA,android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
            }, 200);
            return;
        }

        GPSTracker tracker = new GPSTracker(MapsActivity.this);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {
            myLatitude = tracker.getLatitude();
            myLongitude = tracker.getLongitude();
            myLocation = new LatLng(myLatitude, myLongitude);
        }

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File folder = new File(Environment.getExternalStorageDirectory()+ File.separator + "bazr");
                if(!folder.exists()){
                    folder.mkdir();
                }
                String filename = Environment.getExternalStorageDirectory().getPath() + "/bazr/testfile.jpg";
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
                    ArrayList<String> descriptionList = new ArrayList<>();
                    ArrayList<String> costList = new ArrayList<>();
                    String userId = new String();
                    try {
                        userId = snapshot.child("Id").getValue().toString();
                        markerInfo.setTotalImages(Integer.parseInt(snapshot.child("TotalImages").getValue().toString()));
                        for (int i=0; i < markerInfo.getTotalImages(); i++){
                            bitmapUrl.add(snapshot.child("Bitmap"+i).getValue().toString());
                            descriptionList.add(snapshot.child("Description"+i).getValue().toString());
                            costList.add(snapshot.child("Cost"+i).getValue().toString());

                        }
                        markerInfo.setBitmapUrl(bitmapUrl);
                        markerInfo.setDescriptionList(descriptionList);
                        markerInfo.setCostList(costList);
                        markerInfo.setLongitude(snapshot.child("LocationLong").getValue().toString());
                        markerInfo.setLatitude(snapshot.child("LocationLati").getValue().toString());
                        markerInfo.setId(snapshot.child("Id").getValue().toString());
                        markerInfo.setReport(snapshot.child("Reported").getValue().toString());
                        markerInfo.setTitle(snapshot.child("Title").getValue().toString());
                        markerInfo.setState(snapshot.child("State").getValue().toString());
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

                if (!MapsActivity.markerInfoMap.containsKey(MapsActivity.UserId)){
                    stateButton.setVisibility(View.INVISIBLE);

                }
                else{
                    if(LoginActivity.isGuest){
                        stateButton.setVisibility(View.INVISIBLE);
                    }
                    else{
                        stateButton.setVisibility(View.VISIBLE);
                        firebaseDatabase.getReference(UserId).child("State").addValueEventListener(new ValueEventListener() {

                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                try{
                                    if (dataSnapshot.getValue().toString().equals("open")){
                                        stateButton.setText("I am open");
                                    }
                                    else{
                                        stateButton.setText("I am closed");
                                    }
                                }
                                catch (Exception e){
                                    Log.e("stateButtonChecked", "Not present. App will continue");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
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



        myLocation = new LatLng(myLatitude, myLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

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


        GPSTracker tracker = new GPSTracker(MapsActivity.this);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {
            myLatitude = tracker.getLatitude();
            myLongitude = tracker.getLongitude();
        }


        try{
            myLocation = new LatLng(myLatitude, myLongitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));

        }
        catch (Exception e){
            Toast.makeText(getApplicationContext(),"Please Enable GPS Servcies", Toast.LENGTH_SHORT).show();
        }
    }

    public void ListView(View view){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    public void setMarker(MarkerInfo markerInfo) {

        LatLng current = new LatLng(Double.parseDouble(markerInfo.getLatitude()), Double.parseDouble(markerInfo.getLongitude()));
        mClusterManager.addItem(markerInfo);
        mClusterManager.cluster();
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
//        mMap.animateCamera(CameraUpdateFactory.zoomTo(17.0f));
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerInfo>() {
            @Override
            public boolean onClusterItemClick(MarkerInfo markerInfo) {
                Intent intent = new Intent(getApplicationContext(), MakerClickedLayout.class);
                intent.putExtra("TotalImages",markerInfo.getTotalImages());
                intent.putStringArrayListExtra("Bitmap", markerInfo.getBitmapUrl());
                intent.putStringArrayListExtra("Description", markerInfo.getDescriptionList());
                intent.putStringArrayListExtra("Cost", markerInfo.getCostList());
                intent.putExtra("Title", markerInfo.getTitle());
                intent.putExtra("Id", markerInfo.getId());
                intent.putExtra("Reported", markerInfo.getReport());
                intent.putExtra("State", markerInfo.getReport());
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

                if (LoginActivity.isGuest){
                    Toast.makeText(getApplicationContext(), "Please sign in to post something", Toast.LENGTH_SHORT).show();

                    break;
                }

                File folder = new File(Environment.getExternalStorageDirectory()+ File.separator + "bazr");
                if(!folder.exists()){
                    folder.mkdir();
                }
                String filename = Environment.getExternalStorageDirectory().getPath() + "/bazr/testfile.jpg";
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






