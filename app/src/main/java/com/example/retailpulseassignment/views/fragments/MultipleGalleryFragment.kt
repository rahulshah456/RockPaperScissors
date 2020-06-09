package com.example.retailpulseassignment.views.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retailpulseassignment.R
import com.example.retailpulseassignment.mlkit.FirebaseMethods
import com.example.retailpulseassignment.mlkit.FirebaseMethods.FirebaseMethodsListener
import com.example.retailpulseassignment.utils.Constants.IMAGE_URLS_LIST
import com.example.retailpulseassignment.views.adapters.MultipleGalleryAdapter
import com.google.firebase.ml.common.FirebaseMLException
import java.io.IOException
import java.util.*

class MultipleGalleryFragment : Fragment() {

    private var imageUrlsList: ArrayList<String>? = ArrayList()
    private var mContext: Context? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: MultipleGalleryAdapter? = null


    companion object {
        val TAG = MultipleGalleryFragment::class.java.simpleName
        fun newInstance(imageUrlsList: ArrayList<String>?): MultipleGalleryFragment {
            val fragment = MultipleGalleryFragment()
            val args = Bundle()
            args.putSerializable(IMAGE_URLS_LIST, imageUrlsList)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_multiple, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mContext = context
        mRecyclerView = view.findViewById(R.id.rv_gallery)

        val bundle = arguments
        if (bundle != null) {
            imageUrlsList = bundle.getSerializable(IMAGE_URLS_LIST) as ArrayList<String>?
            generateRecyclerView()
            PredictionsTask().execute()
        }
    }

    private fun generateRecyclerView() {
        val mOrientation = resources.configuration.orientation
        val gridSize = if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) 2 else 1
        val mLayoutManager = GridLayoutManager(mContext, gridSize, RecyclerView.VERTICAL, false)
        mRecyclerView!!.layoutManager = mLayoutManager
        mRecyclerView!!.itemAnimator = DefaultItemAnimator()
        mRecyclerView!!.setHasFixedSize(true)

        // Adapter initialization
        mAdapter = MultipleGalleryAdapter(mContext!!, imageUrlsList!!, generateProcessingLabels(imageUrlsList!!.size))
        mRecyclerView!!.adapter = mAdapter
    }

    @SuppressLint("StaticFieldLeak")
    inner class PredictionsTask : AsyncTask<Void?, Void?, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean {
            for (pos in imageUrlsList!!.indices) {
                try {
                    val methods = FirebaseMethods(mContext!!)
                    methods.runInference(getMediaBitmap(imageUrlsList!![pos]))
                    methods.setOnMethodsListener(object : FirebaseMethodsListener {
                        override fun onResult(label: String) {
                            mAdapter!!.updateItem(pos, label)
                        }

                        override fun onFailed(e: Exception?) {}
                    })
                } catch (e: FirebaseMLException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return true
        }
    }

    /** Extra Utils Methods for Multiple processing  */
    @Throws(IOException::class)
    fun getMediaBitmap(imageURL: String?): Bitmap {
        // Normalizing and Scaling the input image
        return MediaStore.Images.Media.getBitmap(mContext!!.contentResolver, Uri.parse(imageURL))
    }

    private fun generateProcessingLabels(maxSize: Int): ArrayList<String> {
        val mLabelsList = ArrayList<String>()
        for (i in 0 until maxSize) {
            mLabelsList.add(mContext!!.resources.getString(R.string.processing))
        }
        return mLabelsList
    }
}