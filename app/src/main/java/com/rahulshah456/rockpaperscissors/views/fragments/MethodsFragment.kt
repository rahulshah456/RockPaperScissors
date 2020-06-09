package com.rahulshah456.rockpaperscissors.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.rahulshah456.rockpaperscissors.R
import com.rahulshah456.rockpaperscissors.views.activities.CameraActivity
import com.rahulshah456.rockpaperscissors.views.activities.GalleryActivity

class MethodsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_methods, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // init click declarations
        view.findViewById<View>(R.id.cv_gallery).setOnClickListener(onClickListener)
        view.findViewById<View>(R.id.cv_camera).setOnClickListener(onClickListener)
    }

    // init listeners
    private val onClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.cv_gallery -> startActivity(Intent(context, GalleryActivity::class.java))
            R.id.cv_camera -> startActivity(Intent(context, CameraActivity::class.java))
        }
    }

    companion object {
        val TAG = MethodsFragment::class.java.simpleName
    }
}