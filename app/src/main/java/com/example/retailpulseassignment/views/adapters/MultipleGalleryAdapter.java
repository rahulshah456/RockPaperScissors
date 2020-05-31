package com.example.retailpulseassignment.views.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.retailpulseassignment.R;

import java.util.ArrayList;

public class MultipleGalleryAdapter extends RecyclerView.Adapter<MultipleGalleryAdapter.GalleryViewHolder> {

    private static final String TAG = MultipleGalleryAdapter.class.getSimpleName();
    private ArrayList<String> mImagesList;
    private ArrayList<Integer> mLabelsList;
    private Context mContext;


    public MultipleGalleryAdapter(Context mContext, ArrayList<String> mImagesList, ArrayList<Integer> mLabelsList) {
        this.mContext = mContext;
        this.mImagesList = mImagesList;
        this.mLabelsList = mLabelsList;
    }

    public static class GalleryViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnail;
        TextView label,probability;

        GalleryViewHolder(View itemView) {
            super(itemView);

            thumbnail = itemView.findViewById(R.id.iv_thumbnail);
            label = itemView.findViewById(R.id.tv_label);
            probability = itemView.findViewById(R.id.tv_probability);
        }
    }
    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_prediction, parent, false);
        return new GalleryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {

        // setting thumbnail of selected image
        String imageURL = mImagesList.get(position);
        Glide.with(mContext)
                .load(imageURL)
                .apply(new RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.thumbnail);


        // setting image prediction
        int label = mLabelsList.get(position);
        switch (label){
            case 0:
                holder.label.setText(mContext.getResources().getString(R.string.rock));
                break;
            case 1:
                holder.label.setText(mContext.getResources().getString(R.string.paper));
                break;
            case 2:
                holder.label.setText(mContext.getResources().getString(R.string.scissor));
                break;
            case -1:
            default:
                holder.label.setText(mContext.getResources().getString(R.string.processing));
                break;
        }

    }

    @Override
    public int getItemCount() {
        return mImagesList.size();
    }


    public void updateItem(int position, int label){
        mLabelsList.set(position,label);
        notifyItemChanged(position);
    }


}
