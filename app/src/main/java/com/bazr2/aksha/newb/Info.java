package com.bazr2.aksha.newb;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcel;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.provider.Settings.Secure;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.apache.commons.lang3.SerializationUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.zelory.compressor.Compressor;

public class Info extends AppCompatActivity {
    protected String android_id;

    LocationManager locationManager;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    protected Bitmap bitmap;
    ImageButton addImages;
    Uri imageUri;
    String imageStr;
    ImageButton buttonNext;
    Location myLocation;
    ImageButton buttonPrevious;
    Map<String, Object> map;
    int current = 0;
    private StorageReference storageReference;
    ArrayList<Uri> downloadUrl = new ArrayList<>();
    ImageView imageView;
    Double longitude;
    Double latitude;
    DatabaseReference databaseReference;
    ArrayList<String> descriptionList = new ArrayList<>();
    ArrayList<String> costList = new ArrayList<>();
    EditText eT;
    int imgLoc =0;
    Button uploadButton;
    boolean uploadEnabled= false;
    Button saveButton;
    boolean saveEnabled = true;

    EditText eTdes;
    EditText eTcost;
    static int countPost=2;
    String path;


    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            if (location != null) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        imageStr = Environment.getExternalStorageDirectory() + "/bazr/testfile.jpg";
        File actualImage = new File(imageStr);
        File compressedImageFile=null;
        try {
            compressedImageFile = new Compressor(this).compressToFile(actualImage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        uploadButton = findViewById(R.id.button);
//        uploadButton.setClickable(uploadEnabled);
//        uploadButton.setEnabled(uploadEnabled);
        saveButton = findViewById(R.id.buttonSave);
        eT = findViewById(R.id.nameEditText);
        eTdes = findViewById(R.id.editTextDescription);
        eTcost = findViewById(R.id.editTextCost);
        imageUri = Uri.parse(imageStr);
        imageView = findViewById(R.id.imageView);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8;

        Bitmap bm = BitmapFactory.decodeFile(imageStr,options);
        imageView.setImageBitmap(bm);

        eT = findViewById(R.id.nameEditText);

        try {

                databaseReference = FirebaseDatabase.getInstance().getReference(MapsActivity.UserId);


            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    try{

                        eT.setText(dataSnapshot.child("Title").getValue().toString());
                    }
                    catch (Exception e){
                        Log.e("Error: ", "NEW USER: First time upload " );
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch(Exception e){
            Log.e("Error: ", "NEW USER: First time upload " );
        }

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 0, locationListener);

        map = new HashMap<>();
        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference(firebaseAuth.getCurrentUser().getUid());
        addImages = findViewById(R.id.buttonMoreImages);
        addImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename = Environment.getExternalStorageDirectory().getPath() + "/bazr/testfile.jpg";
                File file =  new File(filename);
                if (TextUtils.isEmpty(eT.getText().toString()) || TextUtils.isEmpty((eT.getText().toString().trim()))){
                    Toast.makeText(getApplicationContext(), "Name is a required Field", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(eTdes.getText().toString()) || TextUtils.isEmpty((eT.getText().toString().trim()))){
                    Toast.makeText(getApplicationContext(), "Description is a required Field", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(eTcost.getText().toString()) || TextUtils.isEmpty((eT.getText().toString().trim()))){
                    Toast.makeText(getApplicationContext(), "Cost is a required Field", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(uploadEnabled != true){
                    Toast.makeText(getApplicationContext(), "You need to save before you can add more images", Toast.LENGTH_SHORT).show();
                    return;
                }



                imageUri = FileProvider.getUriForFile(getApplicationContext(),"com.bazr2.android.fileprovider", file );
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imageUri);
//                uploadButton.setClickable(false);
//                uploadButton.setEnabled(false);
//                uploadButton.setActivated(false);
                uploadEnabled=false;
                saveEnabled=true;
                startActivityForResult(cameraIntent, 123);

            }
        });
        firebaseDatabase = FirebaseDatabase.getInstance();
        buttonNext = findViewById(R.id.buttonNext);
        buttonPrevious = findViewById(R.id.buttonPrevious);

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if (current >= downloadUrl.size() - 1) {
                        current = 0;
                        Glide.with(getApplicationContext()).load(downloadUrl.get(current)).into(imageView);
                        eTdes.setText(descriptionList.get(current));
                        eTcost.setText(costList.get(current));
                        return;
                    }
                    current++;
                    Glide.with(getApplicationContext()).load(downloadUrl.get(current)).into(imageView);
                    eTdes.setText(descriptionList.get(current));
                    eTcost.setText(costList.get(current));
                }
                catch(Exception e) {
                    Toast.makeText(getApplicationContext(), "No More Images", Toast.LENGTH_SHORT);
                }

            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{

                    if (current <= 0) {
                        current = downloadUrl.size()-1;
                        Glide.with(getApplicationContext()).load(downloadUrl.get(current)).into(imageView);
                        eTdes.setText(descriptionList.get(current));
                        eTcost.setText(costList.get(current));
                        return;
                    }
                    current--;
                    Glide.with(getApplicationContext()).load(downloadUrl.get(current)).into(imageView);
                    eTdes.setText(descriptionList.get(current));
                    eTcost.setText(costList.get(current));
                }
                catch(Exception e) {
                    Toast.makeText(getApplicationContext(), "No More Images", Toast.LENGTH_SHORT);
                }
            }
        });



        Uri file = Uri.fromFile(compressedImageFile);
        final StorageReference fileRef = storageReference.child("Image" + downloadUrl.size());

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setMessage("Loading");
        progressDialog.setCancelable(false);
        progressDialog.show();


        fileRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                while(!urlTask.isSuccessful());
                Uri downloadURL = urlTask.getResult();
                downloadUrl.add(Uri.parse(downloadURL.toString()));
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Could Not Upload Data", Toast.LENGTH_SHORT).show();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Spinner spinner = findViewById(R.id.spinnerCurrency);
                String text = spinner.getSelectedItem().toString();

                if (saveEnabled != true){
                    Toast.makeText(getApplicationContext(), "Already Saved", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(eTdes.getText().toString()) || TextUtils.isEmpty((eT.getText().toString().trim()))){
                    Toast.makeText(getApplicationContext(), "Description is a required Field", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(eTcost.getText().toString()) || TextUtils.isEmpty((eT.getText().toString().trim()))){
                    Toast.makeText(getApplicationContext(), "Cost is a required Field", Toast.LENGTH_SHORT).show();
                    return;
                }
                    descriptionList.add(imgLoc, eTdes.getText().toString());
                    costList.add(imgLoc,text + eTcost.getText().toString());
                    Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                    imgLoc++;
                saveEnabled =false;

                uploadEnabled=true;
            }
        });

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == RESULT_OK) {
                imageStr = Environment.getExternalStorageDirectory() + "/bazr/testfile.jpg";
                File actualImage = new File(imageStr);
                File compressedImageFile = null;
                try {
                    compressedImageFile = new Compressor(this).compressToFile(actualImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final StorageReference fileRef = storageReference.child("Image"+downloadUrl.size());
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setTitle("Please Wait");
                progressDialog.setMessage("Loading");
                progressDialog.setCancelable(false);
                eTcost.setText("");
                eTdes.setText("");
                progressDialog.show();
                Uri file = Uri.fromFile(compressedImageFile);
                fileRef.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!urlTask.isSuccessful());
                        Uri downloadURL = urlTask.getResult();
                        downloadUrl.add(Uri.parse(downloadURL.toString()));
                        Glide.with(getApplicationContext()).load(downloadURL.toString()).into(imageView);
                        progressDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Could Not Upload Data", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } catch (Exception e) {

        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void Upload(View view){

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.INTERNET, Manifest.permission.CAMERA
            }, 200);
            return;
        }

        if(uploadEnabled != true){
            Toast.makeText(getApplicationContext(), "You need to save before you can post", Toast.LENGTH_SHORT).show();
            return;
        }

        GPSTracker tracker = new GPSTracker(this);
        if (!tracker.canGetLocation()) {
            tracker.showSettingsAlert();
        } else {
            latitude = tracker.getLatitude();
            longitude = tracker.getLongitude();
        }

        android_id = MapsActivity.UserId;
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference(MapsActivity.UserId);

        Log.e("Longitude", String.valueOf(longitude));
        Log.e("Latitude", String.valueOf(latitude));

        if (TextUtils.isEmpty(eT.getText().toString()) || TextUtils.isEmpty((eT.getText().toString().trim()))){
            Toast.makeText(getApplicationContext(), "Name is a required Field", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(eTdes.getText().toString()) || TextUtils.isEmpty((eT.getText().toString().trim()))){
            Toast.makeText(getApplicationContext(), "Description is a required Field", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(eTcost.getText().toString()) || TextUtils.isEmpty((eT.getText().toString().trim()))){
            Toast.makeText(getApplicationContext(), "Cost is a required Field", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth firebaseAuth1 = FirebaseAuth.getInstance();

        databaseReference.child("TotalImages").setValue(String.valueOf(downloadUrl.size()));
        for (int i=0;i<downloadUrl.size();i++){
            databaseReference.child("Bitmap"+i).setValue(downloadUrl.get(i).toString());
            databaseReference.child("Description"+i).setValue(descriptionList.get(i));
            databaseReference.child("Cost"+i).setValue(costList.get(i));
        }
        databaseReference.child("LocationLong").setValue(String.valueOf(longitude));
        databaseReference.child("LocationLati").setValue(String.valueOf(latitude));
        databaseReference.child("Title").setValue(eT.getText().toString());
        databaseReference.child("State").setValue("open");
        databaseReference.child("Reported").setValue("False");


        databaseReference.child("Id").setValue(firebaseAuth1.getCurrentUser().getUid());

        MapsActivity.upload = 1;
        Intent intent = new Intent(this, MapsActivity.class);
        saveEnabled=true;
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }


}
