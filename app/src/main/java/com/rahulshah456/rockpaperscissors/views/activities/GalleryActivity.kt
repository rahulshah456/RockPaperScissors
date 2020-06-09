package com.rahulshah456.rockpaperscissors.views.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rahulshah456.rockpaperscissors.R
import com.rahulshah456.rockpaperscissors.utils.Constants.IMAGE_URL
import com.rahulshah456.rockpaperscissors.utils.Constants.IMAGE_URLS_LIST
import com.rahulshah456.rockpaperscissors.utils.Constants.PICK_IMAGE_REQUEST
import com.rahulshah456.rockpaperscissors.views.fragments.MultipleGalleryFragment
import com.rahulshah456.rockpaperscissors.views.fragments.SoloGalleryFragment
import com.google.android.material.appbar.MaterialToolbar
import java.util.*

class GalleryActivity : AppCompatActivity() {

    private var imageURL: String? = null
    private var imageUrlsList: ArrayList<String>? = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        val toolbar = findViewById<MaterialToolbar>(R.id.mainToolbarId)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        if (savedInstanceState != null) {
            updateFragment(savedInstanceState)
        } else pickImage()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST) {
            if (data == null) {
                Toast.makeText(this, "Failed to load image!", Toast.LENGTH_LONG).show()
                onBackPressed()
                return
            }
            changeFragment(data)
        }
    }

    private fun changeFragment(data: Intent) {
        if (data.data != null) {
            val uri = data.data
            if (uri != null) {
                imageURL = uri.toString()
                supportFragmentManager.beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fl_gallery, SoloGalleryFragment.newInstance(imageURL), SoloGalleryFragment.TAG)
                        .commit()
            } else {
                Toast.makeText(this, "No Image Selected!", Toast.LENGTH_LONG).show()
                onBackPressed()
            }
        } else if (data.clipData != null) {
            for (i in 0 until data.clipData!!.itemCount) {
                val imageURL = data.clipData!!.getItemAt(i).uri.toString()
                imageUrlsList!!.add(imageURL)
            }
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fl_gallery, MultipleGalleryFragment.newInstance(imageUrlsList), MultipleGalleryFragment.TAG)
                    .commit()
        }
    }

    private fun updateFragment(savedBundle: Bundle) {
        imageURL = savedBundle.getString(IMAGE_URL, null)
        imageUrlsList = savedBundle.getSerializable(IMAGE_URLS_LIST) as ArrayList<String>?
        if (imageURL != null) {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fl_gallery, SoloGalleryFragment.newInstance(imageURL), SoloGalleryFragment.TAG)
                    .commit()
        } else if (imageUrlsList != null) {
            supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fl_gallery, MultipleGalleryFragment.newInstance(imageUrlsList), MultipleGalleryFragment.TAG)
                    .commit()
        } else onBackPressed()
    }

    /** Local Image Actions  */
    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(IMAGE_URL, imageURL)
        outState.putSerializable(IMAGE_URLS_LIST, imageUrlsList)
        super.onSaveInstanceState(outState)
    }
}