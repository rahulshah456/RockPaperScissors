package com.example.retailpulseassignment.views.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.example.retailpulseassignment.utils.Constants.IMAGE_URL;

public class SoloGalleryFragment extends Fragment {

    public static final String TAG = SoloGalleryFragment.class.getSimpleName();
    private Context mContext;
    private TextView textView;
    private ImageView thumbnail;


    public static SoloGalleryFragment newInstance(String imageURL) {
        SoloGalleryFragment fragment = new SoloGalleryFragment();
        Bundle args = new Bundle();
        args.putString(IMAGE_URL, imageURL);
        fragment.setArguments(args);
        return fragment;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_solo,container,false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getContext();
        textView = view.findViewById(R.id.tv_header);
        thumbnail = view.findViewById(R.id.iv_thumbnail);

        Bundle bundle = getArguments();
        if (bundle!=null){
            loadImage(bundle.getString(IMAGE_URL));
        }

    }


    private void loadImage(String imageURL){
        Glide.with(this)
                .asBitmap()
                .load(imageURL)
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

        AssetsLoader loader = new AssetsLoader(mContext);
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
}
