package com.bazr2.aksha.newb;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class MakerClickedLayout extends AppCompatActivity {

    ImageView imageView;
    Bitmap image;
    String longitude;
    String latitude;
    String reported;
    String imageUrl;
    String id;
    ArrayList<String> bitmapUrl;
    ArrayList<String> descriptionList;
    ArrayList<String> costList;
    int totalImages;
    int current = 0;
    HashMap<String, String> storedImagePath = new HashMap<>();
    ImageButton buttonNextMarkerClicked;
    ImageButton buttonPreviousMarkerClicked;
    ImageButton imageButtonEdit;
    StorageReference storageReference;
    FirebaseAuth firebaseAuth;
    File outputFile;
    ImageButton imageButtonDelete;
    int i;
    Context context;
    EditText eT;
    EditText eTdes;
    EditText eTcost;
    TextView reportPost;
    DatabaseReference databaseReference;

    File myDir = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maker_clicked_layout);
//        SGD = new ScaleGestureDetector(this, new ScaleListener());
        eT = findViewById(R.id.editTextNameMarkerClicked);
        eTdes = findViewById(R.id.editTextDescriptionMarkerClicked);
        eTcost = findViewById(R.id.editTextCostMarkerClicked);
        imageView = findViewById(R.id.imageViewMarkerClicked);
        buttonNextMarkerClicked = findViewById(R.id.buttonNextMarkerClicked);
        buttonPreviousMarkerClicked = findViewById(R.id.buttonPreviousMarkerClicked);
        imageButtonDelete = findViewById(R.id.imageButtonDelete);
        imageButtonEdit = findViewById(R.id.imageButtonEdit);
        reportPost = findViewById(R.id.reportPost);

        firebaseAuth = FirebaseAuth.getInstance();
        eT.setText(getIntent().getStringExtra("Title").toString());
        latitude = getIntent().getStringExtra("Latitude").toString();
        longitude = getIntent().getStringExtra("Longitude").toString();
        reported = getIntent().getStringExtra("Reported").toString();
        if (LoginActivity.isGuest){
            id = "1";
            reportPost.setVisibility(View.INVISIBLE);
            databaseReference = FirebaseDatabase.getInstance().getReference("1");
        }
        else{
            id = getIntent().getStringExtra("Id").toString();
            databaseReference = FirebaseDatabase.getInstance().getReference(id);
        }
        totalImages = getIntent().getIntExtra("TotalImages", 1);
        bitmapUrl = getIntent().getStringArrayListExtra("Bitmap");
        descriptionList = getIntent().getStringArrayListExtra("Description");
        costList = getIntent().getStringArrayListExtra("Cost");

        eTdes.setText(descriptionList.get(0));
        eTcost.setText(costList.get(0));


        storageReference = FirebaseStorage.getInstance().getReference(id);
        Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);

        Glide.with(getApplicationContext()).load(bitmapUrl.get(0)).into(imageView);

        if (!id.equals(firebaseAuth.getUid())){
            imageButtonDelete.setVisibility(View.INVISIBLE);
        }

        imageButtonEdit.setVisibility(View.INVISIBLE);
        context = getApplicationContext();



        reportPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MakerClickedLayout.this, AlertDialog.THEME_HOLO_LIGHT);
                dlgAlert.setMessage("Are you sure you want to report this post?");
                dlgAlert.setTitle("Report Post");
                dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        databaseReference.child("Reported").setValue("True");
                        Toast.makeText(getApplicationContext(), "This post has been reported. A decision will be made in 24hrs regarding it.", Toast.LENGTH_LONG).show();
                        imageView.setVisibility(View.INVISIBLE);
                        imageView.setImageResource(android.R.color.transparent);
                        imageView.setClickable(false);
                        buttonNextMarkerClicked.setVisibility(View.INVISIBLE);
                        buttonPreviousMarkerClicked.setVisibility(View.INVISIBLE);
                        reportPost.setVisibility(View.INVISIBLE);
                    }
                });
                dlgAlert.setNegativeButton("Cancel", null);
                dlgAlert.setCancelable(true);
                dlgAlert.create().show();

            }
        });




        //******************************************************************************************
//        imageButtonEdit.setVisibility(View.INVISIBLE);
        //******************************************************************************************

        imageButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditInfoActivity.class);
                intent.putExtra("Name", eT.getText().toString());
                intent.putStringArrayListExtra("Description", descriptionList);
                intent.putStringArrayListExtra("Cost", costList);
                intent.putStringArrayListExtra("BitmapURL", bitmapUrl);
                intent.putExtra("Id", id);
                finish();
                startActivity(intent);

            }
        });




        imageButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for( DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                                if (id.equals(dataSnapshot1.getKey())){
                                    for (DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()){
                                        dataSnapshot2.getRef().removeValue();
                                    }
                                    MapsActivity.markerInfoMap.remove(id);
                                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    finish();
                                    startActivity(intent);
                                }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "Could Not Delete", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        Button imageButton = findViewById(R.id.buttonDirections);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f", Float.parseFloat(latitude), Float.parseFloat(longitude));
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                startActivity(intent);
            }
        });


        Bitmap bitmap = BitmapFactory.decodeFile(storedImagePath.get(bitmapUrl.get(0)));
        if(reported.equals("True")){
            imageView.setVisibility(View.INVISIBLE);
            imageView.setImageResource(android.R.color.transparent);
            imageView.setClickable(false);
            reportPost.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "This post has been flagged by some user.", Toast.LENGTH_LONG).show();
            buttonNextMarkerClicked.setVisibility(View.INVISIBLE);
            buttonPreviousMarkerClicked.setVisibility(View.INVISIBLE);
        }
        else{
            imageView.setImageBitmap(bitmap);
        }


        buttonNextMarkerClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reported.equals("True")){
                    imageView.setImageResource(android.R.color.transparent);
                    return;
                }
                if (current >= bitmapUrl.size() - 1) {
                    current = 0;
                    Glide.with(getApplicationContext()).load(bitmapUrl.get(current)).into(imageView);
                    eTdes.setText(descriptionList.get(current));
                    eTcost.setText(costList.get(current));
                    return;
                }
                current++;
                    Glide.with(getApplicationContext()).load(bitmapUrl.get(current)).into(imageView);
                    eTdes.setText(descriptionList.get(current));
                    eTcost.setText(costList.get(current));
            }
        });

        buttonPreviousMarkerClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(reported.equals("True")){
                    imageView.setImageResource(android.R.color.transparent);
                    return;
                }
                if (current <= 0) {
                    current = bitmapUrl.size()-1;
                    Glide.with(getApplicationContext()).load(bitmapUrl.get(current)).into(imageView);
                    eTdes.setText(descriptionList.get(current));
                    eTcost.setText(costList.get(current));
                    return;
                }
                current--;
                    Glide.with(getApplicationContext()).load(bitmapUrl.get(current)).into(imageView);
                    eTdes.setText(descriptionList.get(current));
                    eTcost.setText(costList.get(current));
            }
        });

    }

    public void FullScreen(View view) {
        Intent intent = new Intent(this, FullImageView.class);
        intent.putExtra("image", bitmapUrl.get(current));
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
