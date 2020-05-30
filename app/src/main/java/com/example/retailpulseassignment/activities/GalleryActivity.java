package com.example.retailpulseassignment.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.retailpulseassignment.R;
import com.example.retailpulseassignment.utils.CalculateResult;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomLocalModel;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class GalleryActivity extends AppCompatActivity {

    public static final String TAG = GalleryActivity.class.getSimpleName();
    public static final int PICK_IMAGE_REQUEST = 2;
    public static final String IMAGE_URL = "image_url";
    private TextView textView;
    private String imageUri = null;
    private ImageView thumbnail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        MaterialToolbar toolbar = findViewById(R.id.mainToolbarId);
        thumbnail = findViewById(R.id.iv_thumbnail);
        textView = findViewById(R.id.tv_header);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        if (savedInstanceState!=null){
            imageUri = savedInstanceState.getString(IMAGE_URL,null);
            loadImage();
        } else pickImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST){

            if (data == null) {
                Toast.makeText(this,"Failed to load image!",Toast.LENGTH_LONG).show();
                onBackPressed();
            } else {
                Uri uri = data.getData();
                if (uri!=null){
                    imageUri = String.valueOf(uri);
                    loadImage();
                }else {
                    Toast.makeText(this,"No Image Selected!",Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            }
        }

    }



    /** Firebase MlKit Methods */
    private void pickImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }
    private void loadImage(){
        Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Bitmap> target, boolean isFirstResource) {
                        Log.d(TAG, "onLoadFailed: ");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        Log.d(TAG, "onResourceReady: ");
                        try {
                            runInference(resource);
                        } catch (FirebaseMLException e) {
                            e.printStackTrace();
                        }
                        return false;
                    }
                })
                .into(thumbnail);
    }
    private float[][][][] bitmapToInputArray(Bitmap resource) {
        // [START mlKit_bitmap_input]
        Bitmap bitmap = resource;
        bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
        thumbnail.setImageBitmap(bitmap);

        int batchNum = 0;
        float[][][][] input = new float[1][300][300][3];
        for (int x = 0; x < 300; x++) {
            for (int y = 0; y < 300; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                // model. For example, some models might require values to be normalized
                // to the range [0.0, 1.0] instead.
                input[batchNum][x][y][0] = (Color.red(pixel)) / 255.0f;
                input[batchNum][x][y][1] = (Color.green(pixel)) / 255.0f;
                input[batchNum][x][y][2] = (Color.blue(pixel)) / 255.0f;
            }
        }
        // [END mlKit_bitmap_input]

        return input;
    }
    private FirebaseModelInputOutputOptions createInputOutputOptions() throws FirebaseMLException {
        return new FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 300, 300, 3})
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 16})
                .build();
    }
    private FirebaseModelInterpreter createInterpreter(FirebaseCustomRemoteModel remoteModel) throws FirebaseMLException {
        // [START mlKit_create_interpreter]
        FirebaseModelInterpreter interpreter = null;
        try {
            FirebaseModelInterpreterOptions options =
                    new FirebaseModelInterpreterOptions.Builder(remoteModel).build();
            interpreter = FirebaseModelInterpreter.getInstance(options);
        } catch (FirebaseMLException e) {
            //.
        }
        // [END mlKit_create_interpreter]

        return interpreter;
    }
    private void runInference(Bitmap resource) throws FirebaseMLException {

        FirebaseCustomRemoteModel remoteModel = new FirebaseCustomRemoteModel.Builder("rps_model").build();
        FirebaseModelInterpreter firebaseInterpreter = createInterpreter(remoteModel);

        float[][][][] input = bitmapToInputArray(resource);
        FirebaseModelInputOutputOptions inputOutputOptions = createInputOutputOptions();

        // [START mlKit_run_inference]
        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                .add(input)  // add() as many input arrays as your model requires
                .build();
        firebaseInterpreter.run(inputs, inputOutputOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseModelOutputs>() {
                            @Override
                            public void onSuccess(FirebaseModelOutputs result) {
                                // [START_EXCLUDE]
                                // [START mlKit_read_result]
                                float[][] output = result.getOutput(0);
                                float[] probabilities = output[0];
                                try {
                                    int actualResult = new CalculateResult(GalleryActivity.this).getResult(probabilities);
                                    switch (actualResult){
                                        case 0:
                                            textView.setText(getResources().getString(R.string.rock));
                                            break;
                                        case 1:
                                            textView.setText(getResources().getString(R.string.paper));
                                            break;
                                        case 2:
                                            textView.setText(getResources().getString(R.string.scissor));
                                            break;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // [END mlKit_read_result]
                                // [END_EXCLUDE]
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Task failed with an exception
                                Log.d(TAG, "onFailure: ");
                            }
                        });
        // [END mlKit_run_inference]
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(IMAGE_URL,imageUri);
        super.onSaveInstanceState(outState);
    }
}