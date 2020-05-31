package com.example.retailpulseassignment.views.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.retailpulseassignment.R;
import com.example.retailpulseassignment.mlkit.AssetsLoader;
import com.example.retailpulseassignment.mlkit.Classifier;
import com.example.retailpulseassignment.views.adapters.MultipleGalleryAdapter;
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
import java.util.ArrayList;
import static com.example.retailpulseassignment.utils.Constants.IMAGE_URLS_LIST;

public class MultipleGalleryFragment extends Fragment {

    public static final String TAG = MultipleGalleryFragment.class.getSimpleName();
    private ArrayList<String> imageUrlsList = new ArrayList<>();
    private Context mContext;

    private RecyclerView mRecyclerView;
    private MultipleGalleryAdapter mAdapter;

    public static MultipleGalleryFragment newInstance(ArrayList<String> imageUrlsList) {
        MultipleGalleryFragment fragment = new MultipleGalleryFragment();
        Bundle args = new Bundle();
        args.putSerializable(IMAGE_URLS_LIST, imageUrlsList);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_multiple,container,false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mContext = getContext();
        mRecyclerView = view.findViewById(R.id.rv_gallery);

        Bundle bundle = getArguments();
        if (bundle!=null){
            imageUrlsList = (ArrayList<String>) bundle.getSerializable(IMAGE_URLS_LIST);
            generateRecyclerView();
            new PredictionsTask().execute();
        }
    }


    public void generateRecyclerView(){

        int mOrientation = getResources().getConfiguration().orientation;
        int gridSize = ((mOrientation == Configuration.ORIENTATION_LANDSCAPE)? 2:1);

        GridLayoutManager mLayoutManager = new GridLayoutManager(mContext, gridSize, RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        // Adapter initialization
        mAdapter = new MultipleGalleryAdapter(mContext,imageUrlsList,generateProcessingLabels(imageUrlsList.size()));
        mRecyclerView.setAdapter(mAdapter);

    }

    @SuppressLint("StaticFieldLeak")
    public class PredictionsTask extends AsyncTask<Void,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Void... voids) {
            for (int pos=0;pos<imageUrlsList.size();pos++){
                try {
                    runInference(normalizeBitmap(imageUrlsList.get(pos)),pos);
                } catch (FirebaseMLException | IOException e) {
                    e.printStackTrace();
                }
            }
            return true;
        }
    }
    public ByteBuffer normalizeBitmap(String imageURL) throws IOException {
        // Normalizing and Scaling the input image
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), Uri.parse(imageURL));
        bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, false);
        return Classifier.convertBitmapToByteBuffer(bitmap);
    }
    public ArrayList<Integer> generateProcessingLabels(int maxSize){
        ArrayList<Integer> mLabelsList = new ArrayList<>();
        for (int i=0;i<maxSize;i++){
            mLabelsList.add(-1);
        }
        return mLabelsList;
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
    private void runInference(ByteBuffer byteBuffer, final int position) throws FirebaseMLException {

        FirebaseCustomRemoteModel remoteModel = new FirebaseCustomRemoteModel.Builder("rps_model").build();
        FirebaseModelInterpreter firebaseInterpreter = createInterpreter(remoteModel);

        // Input and Outputs format
        FirebaseModelInputOutputOptions inputOutputOptions = createInputOutputOptions();

        // Generating Model Input
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
                                int label = FindImageResult(probabilities);

                                // updating the adapter with result
                                mAdapter.updateItem(position,label);

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
    private int FindImageResult(float[] mProbabilities){
        AssetsLoader loader = new AssetsLoader(mContext);
        int resultLabel = -1;
        try {
            return new Classifier(loader.LoadOutputLabels(), loader.LoadOutputVectors())
                    .getResult(mProbabilities);

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
