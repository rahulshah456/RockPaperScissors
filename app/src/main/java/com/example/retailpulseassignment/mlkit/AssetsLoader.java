package com.example.retailpulseassignment.mlkit;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class AssetsLoader {

    public static final String TAG = AssetsLoader.class.getSimpleName();
    private Context mContext;

    public AssetsLoader(Context mContext) {
        this.mContext = mContext;
    }

    public Map<Integer,double[]> LoadOutputVectors() throws IOException {
        Map<Integer,double[]> integerMap = new HashMap<>();
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
            double[] list = new double[16];
            for (int i =0;i<dataArray.size();i++){
                list[i] = Double.parseDouble(dataArray.get(i));
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

    public int[] LoadOutputLabels() throws IOException{
        BufferedReader reader = new BufferedReader(new
                InputStreamReader(mContext.getAssets().open("rps_labels.tsv")));
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
