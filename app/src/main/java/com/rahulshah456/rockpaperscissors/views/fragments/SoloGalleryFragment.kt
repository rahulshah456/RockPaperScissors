package com.rahulshah456.rockpaperscissors.views.fragments

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.rahulshah456.rockpaperscissors.R
import com.rahulshah456.rockpaperscissors.mlkit.FirebaseMethods
import com.rahulshah456.rockpaperscissors.mlkit.FirebaseMethods.FirebaseMethodsListener
import com.rahulshah456.rockpaperscissors.utils.Constants.IMAGE_URL
import com.google.firebase.ml.common.FirebaseMLException

class SoloGalleryFragment : Fragment() {

    private var mContext: Context? = null
    private var textView: TextView? = null
    private var thumbnail: ImageView? = null


    companion object {
        val TAG = SoloGalleryFragment::class.java.simpleName
        fun newInstance(imageURL: String?): SoloGalleryFragment {
            val fragment = SoloGalleryFragment()
            val args = Bundle()
            args.putString(IMAGE_URL, imageURL)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_solo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mContext = context
        textView = view.findViewById(R.id.tv_header)
        thumbnail = view.findViewById(R.id.iv_thumbnail)
        val bundle = arguments
        if (bundle != null) {
            loadImage(bundle.getString(IMAGE_URL))
        }
    }

    private fun loadImage(imageURL: String?) {
        Glide.with(this)
                .asBitmap()
                .load(imageURL)
                .apply(RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .listener(object : RequestListener<Bitmap?> {
                    override fun onLoadFailed(e: GlideException?, model: Any,
                                              target: Target<Bitmap?>, isFirstResource: Boolean): Boolean {
                        return false
                    }

                    override fun onResourceReady(resource: Bitmap?, model: Any, target: Target<Bitmap?>,
                                                 dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        try {
                            val methods = FirebaseMethods(mContext!!)
                            methods.runInference(resource!!)
                            methods.setOnMethodsListener(methodsListener)
                        } catch (e: FirebaseMLException) {
                            e.printStackTrace()
                        }
                        return false
                    }
                })
                .into(thumbnail!!)
    }

    private val methodsListener: FirebaseMethodsListener = object : FirebaseMethodsListener {
        override fun onResult(label: String) {
            textView?.text = label
        }

        override fun onFailed(e: Exception?) {
            if (e != null) {
                Toast.makeText(mContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}