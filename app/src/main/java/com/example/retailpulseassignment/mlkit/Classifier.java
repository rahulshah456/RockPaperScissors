package com.example.retailpulseassignment.mlkit;

import android.graphics.Bitmap;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Objects;

import static com.example.retailpulseassignment.utils.Constants.BATCH_SIZE;
import static com.example.retailpulseassignment.utils.Constants.IMAGE_MEAN;
import static com.example.retailpulseassignment.utils.Constants.IMAGE_STD;
import static com.example.retailpulseassignment.utils.Constants.INPUT_SIZE;
import static com.example.retailpulseassignment.utils.Constants.PIXEL_SIZE;

public class Classifier {

    private static final String TAG = Classifier.class.getSimpleName();
    private int[] mLabels;
    private Map<Integer,double[]> mVectorOutputs;

    public Classifier(int[] mLabels, Map<Integer,double[]> mVectorOutputs) {
        this.mLabels = mLabels;
        this.mVectorOutputs = mVectorOutputs;
    }


    public int getResult(float[] outProbabilities) throws IOException {
        double[] euclideanDistances = new double[32];
        for (int i =0; i < mVectorOutputs.size(); i++){
            euclideanDistances[i] = new EuclideanDistance()
                    .compute(Objects.requireNonNull(mVectorOutputs.get(i)),
                            convertFloatsToDoubles(outProbabilities));
        }
        return getMinEuclideanDistance(euclideanDistances);
    }


    private int getMinEuclideanDistance(double[] euclideanDistances) throws IOException {
        double minDistance = euclideanDistances[0];
        int pos = 0;
        for (int i=0;i<euclideanDistances.length;i++){
            if (minDistance>euclideanDistances[i]){
                minDistance = euclideanDistances[i];
                pos = i;
            }
        }
        return mLabels[pos];
    }


    public static ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; ++i) {
            for (int j = 0; j < INPUT_SIZE; ++j) {
                final int val = intValues[pixel++];
                byteBuffer.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                byteBuffer.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
        return byteBuffer;
    }


    public static double[] convertFloatsToDoubles(float[] input) {
        if (input == null) {
            return null; // Or throw an exception - your choice
        }
        double[] output = new double[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = input[i];
        }
        return output;
    }

}
