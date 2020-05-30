package com.example.retailpulseassignment.utils;

import android.content.Context;
import android.util.Log;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringTokenizer;

public class CalculateResult {

    public static final String TAG = CalculateResult.class.getSimpleName();
    private Context mContext;

    public CalculateResult(Context mContext) {
        this.mContext = mContext;
    }


    public int getResult(float[] outProbabilities) throws IOException {
        Map<Integer,float[]> allProbabilities = getAllProbabilities();
        double[] euclideanDistances = new double[32];
        for (int i =0;i<allProbabilities.size();i++){
            euclideanDistances[i] = new EuclideanDistance().compute(convertFloatsToDoubles(outProbabilities),
                    Objects.requireNonNull(convertFloatsToDoubles(allProbabilities.get(i))));
        }
        return getLabelResult(euclideanDistances);
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


    private int getLabelResult(double[] euclideanDistances) throws IOException {
        int[] allLabels =  getAllLabels();
        double minDistance = euclideanDistances[0];
        int pos = 0;
        for (int i=0;i<euclideanDistances.length;i++){
            if (minDistance>euclideanDistances[i]){
                minDistance = euclideanDistances[i];
                pos = i;
            }
        }
        Log.d(TAG, "getLabelResult: pos = " + pos);
        Log.d(TAG, "getLabelResult: minDistance = " + minDistance);
        Log.d(TAG, "getLabelResult: label = " + allLabels[pos]);
        return allLabels[pos];
    }


    public Map<Integer,float[]> getAllProbabilities() throws IOException {
        Map<Integer,float[]> integerMap = new HashMap<>();
        StringTokenizer st ;
        BufferedReader TSVFile = new BufferedReader(new InputStreamReader(mContext.getAssets().open("rps_vecs.tsv")));
        String dataRow = TSVFile.readLine(); // Read first line.
        int count = 0;
        while (dataRow != null){
            st = new StringTokenizer(dataRow,"\t");
            List<String> dataArray = new ArrayList<String>() ;
            while(st.hasMoreElements()){
                dataArray.add(st.nextElement().toString());
            }
            float[] list = new float[16];
            for (int i =0;i<dataArray.size();i++){
                list[i] = Float.parseFloat(dataArray.get(i));
            }
            integerMap.put(count,list);
            count++;
            dataRow = TSVFile.readLine(); // Read next line of data.
        }
        // Close the file once all data has been read.
        TSVFile.close();
        // End the printout with a blank line.
        return integerMap;
    }
    private int[] getAllLabels() throws IOException{
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(mContext.getAssets().open("rps_labels.tsv")));
        int[] labels = new int[32];
        int count = 0;
        String labelRow = reader.readLine();
        while (labelRow!=null){
            labels[count] = Integer.parseInt(labelRow);
            count++;
            labelRow = reader.readLine();
        }
        return labels;
    }

}
