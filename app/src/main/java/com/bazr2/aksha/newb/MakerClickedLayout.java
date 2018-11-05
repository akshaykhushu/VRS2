package com.bazr2.aksha.newb;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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
    String imageUrl;
    String id;
    ArrayList<String> bitmapUrl;
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

        firebaseAuth = FirebaseAuth.getInstance();
        eT.setText(getIntent().getStringExtra("Title").toString());
        eTdes.setText(getIntent().getStringExtra("Description").toString());
        eTcost.setText(getIntent().getStringExtra("Cost").toString());
        latitude = getIntent().getStringExtra("Latitude").toString();
        longitude = getIntent().getStringExtra("Longitude").toString();
        id = getIntent().getStringExtra("Id").toString();
        totalImages = getIntent().getIntExtra("TotalImages", 1);
        bitmapUrl = getIntent().getStringArrayListExtra("Bitmap");

        storageReference = FirebaseStorage.getInstance().getReference(id);
        Picasso.with(getApplicationContext()).setIndicatorsEnabled(true);

        Glide.with(getApplicationContext()).load(bitmapUrl.get(0)).into(imageView);

        if (!id.equals(firebaseAuth.getUid())){
            imageButtonDelete.setVisibility(View.INVISIBLE);
            imageButtonEdit.setVisibility(View.INVISIBLE);
        }
        context = getApplicationContext();


        //******************************************************************************************
//        imageButtonEdit.setVisibility(View.INVISIBLE);
        //******************************************************************************************

        imageButtonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EditInfoActivity.class);
                intent.putExtra("Name", eT.getText().toString());
                intent.putExtra("Desc", eTdes.getText().toString());
                intent.putExtra("Cost", eTcost.getText().toString());
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
        imageView.setImageBitmap(bitmap);

        buttonNextMarkerClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current >= bitmapUrl.size() - 1) {
//                    Toast.makeText(getApplicationContext(), "No More Images", Toast.LENGTH_SHORT).show();
                    current = 0;
                    Glide.with(getApplicationContext()).load(bitmapUrl.get(current)).into(imageView);
                    return;
                }
                current++;
                Glide.with(getApplicationContext()).load(bitmapUrl.get(current)).into(imageView);

            }
        });

        buttonPreviousMarkerClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current <= 0) {
//                    Toast.makeText(getApplicationContext(), "No More Images", Toast.LENGTH_SHORT).show();
                    current = bitmapUrl.size()-1;
                    Glide.with(getApplicationContext()).load(bitmapUrl.get(current)).into(imageView);
                    return;
                }
                current--;
                Glide.with(getApplicationContext()).load(bitmapUrl.get(current)).into(imageView);

            }
        });

    }

    public void FullScreen(View view) {
        Intent intent = new Intent(this, FullImageView.class);
        intent.putExtra("image", bitmapUrl.get(current));
        startActivity(intent);
    }

}
