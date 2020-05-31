package com.example.retailpulseassignment.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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
import com.example.retailpulseassignment.mlkit.AssetsLoader;
import com.example.retailpulseassignment.mlkit.Classifier;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.IOException;
import java.nio.ByteBuffer;

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


    /** Local Image Actions */
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


    /** Firebase MlKit Methods */
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

        // Input and Outputs init
        FirebaseModelInputOutputOptions inputOutputOptions = createInputOutputOptions();

        // Normalizing and Scaling the input image
        Bitmap bitmap = resource;
        bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
        ByteBuffer byteBuffer = Classifier.convertBitmapToByteBuffer(bitmap);
        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                .add(byteBuffer)  // add() as many input arrays as your model requires
                .build();


        // [START mlKit_run_inference]
        firebaseInterpreter.run(inputs, inputOutputOptions)
                .addOnSuccessListener(
                        new OnSuccessListener<FirebaseModelOutputs>() {
                            @Override
                            public void onSuccess(FirebaseModelOutputs result) {
                                // [START_EXCLUDE]
                                // [START mlKit_read_result]
                                float[][] output = result.getOutput(0);
                                float[] probabilities = output[0];
                                FindImageResult(probabilities);

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


    /** Result Computation for selected Image */
    private void FindImageResult(float[] mProbabilities){

        AssetsLoader loader = new AssetsLoader(GalleryActivity.this);
        int resultLabel = -1;
        try {
            resultLabel = new Classifier(loader.LoadOutputLabels(), loader.LoadOutputVectors())
                    .getResult(mProbabilities);
            UpdateResult(resultLabel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void UpdateResult(int label){
        if (label!=-1){
            switch (label){
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
        }
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(IMAGE_URL,imageUri);
        super.onSaveInstanceState(outState);
    }
}