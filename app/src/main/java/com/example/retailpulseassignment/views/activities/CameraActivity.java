package com.example.retailpulseassignment.views.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelInterpreterOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.IOException;
import java.nio.ByteBuffer;

public class CameraActivity extends AppCompatActivity {

    public static final String TAG = CameraActivity.class.getSimpleName();
    private CameraView cameraKitView;
    private RelativeLayout resultLayout;
    private ImageView thumbnail;
    private TextView textViewLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraKitView = findViewById(R.id.ck_preview);
        resultLayout = findViewById(R.id.rl_prediction);
        thumbnail = findViewById(R.id.iv_thumbnail);
        textViewLabel = findViewById(R.id.tv_label);
        Button predictButton = findViewById(R.id.b_predict);


        cameraKitView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                if (resultLayout.getVisibility()==View.INVISIBLE || resultLayout.getVisibility() == View.GONE){
                    resultLayout.setVisibility(View.VISIBLE);
                }
                Bitmap bitmap = cameraKitImage.getBitmap();
                loadImage(bitmap);
                try {
                    runInference(bitmap);
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });


        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraKitView.captureImage();
            }
        });
    }


    private void loadImage(Bitmap bitmap){
        Glide.with(this)
                .load(bitmap)
                .apply(new RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
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

        AssetsLoader loader = new AssetsLoader(CameraActivity.this);
        int resultLabel = -1;
        try {
            resultLabel = new Classifier(loader.LoadOutputLabels(), loader.LoadOutputVectors())
                    .getResult(mProbabilities);
            updateLabel(resultLabel);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void updateLabel(int label){
        switch (label){
            case 0:
                textViewLabel.setText(getResources().getString(R.string.rock));
                break;
            case 1:
                textViewLabel.setText(getResources().getString(R.string.paper));
                break;
            case 2:
                textViewLabel.setText(getResources().getString(R.string.scissor));
                break;
            case -1:
            default:
                textViewLabel.setText(getResources().getString(R.string.processing));
                break;

        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        cameraKitView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraKitView.start();
    }

    @Override
    protected void onPause() {
        cameraKitView.stop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        cameraKitView.stop();
        super.onStop();
    }
}