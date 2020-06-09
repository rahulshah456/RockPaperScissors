package com.rahulshah456.rockpaperscissors.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.rahulshah456.rockpaperscissors.R
import com.rahulshah456.rockpaperscissors.views.adapters.MultipleGalleryAdapter.GalleryViewHolder
import java.util.*

class MultipleGalleryAdapter(private val mContext: Context, private val mImagesList: ArrayList<String>,
                             private val mLabelsList: ArrayList<String>) : RecyclerView.Adapter<GalleryViewHolder>() {

    class GalleryViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var thumbnail: ImageView = itemView.findViewById(R.id.iv_thumbnail)
        var label: TextView = itemView.findViewById(R.id.tv_label)
        var probability: TextView = itemView.findViewById(R.id.tv_probability)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val itemView = LayoutInflater.from(mContext).inflate(R.layout.item_prediction, parent, false)
        return GalleryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {

        // setting thumbnail of selected image
        val imageURL = mImagesList[position]
        Glide.with(mContext)
                .load(imageURL)
                .apply(RequestOptions()
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(holder.thumbnail)


        // setting image prediction
        holder.label.text = mLabelsList[position];
    }

    override fun getItemCount(): Int {
        return mImagesList.size
    }

    fun updateItem(position: Int, label: String) {
        mLabelsList[position] = label
        notifyItemChanged(position)
    }

}