package com.rahulshah456.rockpaperscissors.views.activities

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.rahulshah456.rockpaperscissors.R
import com.rahulshah456.rockpaperscissors.mlkit.FirebaseMethods
import com.rahulshah456.rockpaperscissors.mlkit.FirebaseMethods.FirebaseMethodsListener
import com.google.firebase.ml.common.FirebaseMLException
import com.wonderkiln.camerakit.*

class CameraActivity : AppCompatActivity() {

    private var cameraKitView: CameraView? = null
    private var resultLayout: RelativeLayout? = null
    private var thumbnail: ImageView? = null
    private var textViewLabel: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_camera)
        cameraKitView = findViewById(R.id.ck_preview)
        resultLayout = findViewById(R.id.rl_prediction)
        thumbnail = findViewById(R.id.iv_thumbnail)
        textViewLabel = findViewById(R.id.tv_label)
        val predictButton = findViewById<Button>(R.id.b_predict)

        cameraKitView?.addCameraKitListener(object : CameraKitEventListener {
            override fun onEvent(cameraKitEvent: CameraKitEvent) {}
            override fun onError(cameraKitError: CameraKitError) {}
            override fun onImage(cameraKitImage: CameraKitImage) {
                if (resultLayout?.visibility == View.INVISIBLE || resultLayout?.visibility == View.GONE) {
                    resultLayout?.visibility = View.VISIBLE
                }
                val bitmap = cameraKitImage.bitmap
                loadImage(bitmap)
                try {
                    val methods = FirebaseMethods(this@CameraActivity)
                    methods.runInference(bitmap)
                    methods.setOnMethodsListener(methodsListener)
                } catch (e: FirebaseMLException) {
                    e.printStackTrace()
                }
            }

            override fun onVideo(cameraKitVideo: CameraKitVideo) {}
        })
        predictButton.setOnClickListener { cameraKitView?.captureImage() }
    }

    private fun loadImage(bitmap: Bitmap) {
        Glide.with(this)
                .load(bitmap)
                .apply(RequestOptions()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .centerCrop())
                .into(thumbnail!!)
    }

    private val methodsListener: FirebaseMethodsListener = object : FirebaseMethodsListener {
        override fun onResult(label: String) {
            textViewLabel!!.text = label
        }

        override fun onFailed(e: Exception?) {
            Toast.makeText(this@CameraActivity, e!!.localizedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        cameraKitView!!.start()
    }

    override fun onResume() {
        super.onResume()
        cameraKitView!!.start()
    }

    override fun onPause() {
        cameraKitView!!.stop()
        super.onPause()
    }

    override fun onStop() {
        cameraKitView!!.stop()
        super.onStop()
    }
}