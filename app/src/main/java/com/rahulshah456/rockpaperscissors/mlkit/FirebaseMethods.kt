package com.rahulshah456.rockpaperscissors.mlkit

import android.content.Context
import android.graphics.Bitmap
import com.rahulshah456.rockpaperscissors.R
import com.rahulshah456.rockpaperscissors.mlkit.Classifier.Companion.convertBitmapToByteBuffer
import com.rahulshah456.rockpaperscissors.utils.Constants.INPUT_SIZE
import com.google.firebase.ml.common.FirebaseMLException
import com.google.firebase.ml.custom.*
import java.io.IOException

class FirebaseMethods(private val mContext: Context) {
    private var methodsListener: FirebaseMethodsListener? = null

    interface FirebaseMethodsListener {
        fun onResult(label: String)
        fun onFailed(e: Exception?)
    }

    fun setOnMethodsListener(methodsListener: FirebaseMethodsListener?) {
        this.methodsListener = methodsListener
    }

    /** Firebase MlKit Methods  */
    @Throws(FirebaseMLException::class)
    private fun createInputOutputOptions(): FirebaseModelInputOutputOptions {
        return FirebaseModelInputOutputOptions.Builder()
                .setInputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, INPUT_SIZE, INPUT_SIZE, 3))
                .setOutputFormat(0, FirebaseModelDataType.FLOAT32, intArrayOf(1, 16))
                .build()
    }

    @Throws(FirebaseMLException::class)
    private fun createInterpreter(remoteModel: FirebaseCustomRemoteModel): FirebaseModelInterpreter? {
        // [START mlKit_create_interpreter]
        var interpreter: FirebaseModelInterpreter? = null
        try {
            val options = FirebaseModelInterpreterOptions.Builder(remoteModel).build()
            interpreter = FirebaseModelInterpreter.getInstance(options)
        } catch (e: FirebaseMLException) {
            //.
        }
        // [END mlKit_create_interpreter]
        return interpreter
    }

    @Throws(FirebaseMLException::class)
    fun runInference(resource: Bitmap) {
        val remoteModel = FirebaseCustomRemoteModel.Builder("rps_model").build()
        val firebaseInterpreter = createInterpreter(remoteModel)

        // Input and Outputs init
        val inputOutputOptions = createInputOutputOptions()

        // Normalizing and Scaling the input image
        var bitmap: Bitmap? = resource
        bitmap = Bitmap.createScaledBitmap(bitmap!!, INPUT_SIZE, INPUT_SIZE, false)
        val byteBuffer = convertBitmapToByteBuffer(bitmap)
        val inputs = FirebaseModelInputs.Builder()
                .add(byteBuffer) // add() as many input arrays as your model requires
                .build()


        // [START mlKit_run_inference]
        firebaseInterpreter!!.run(inputs, inputOutputOptions)
                .addOnSuccessListener { result ->
                    // [START_EXCLUDE]
                    // [START mlKit_read_result]
                    val output = result.getOutput<Array<FloatArray>>(0)
                    val probabilities = output[0]
                    methodsListener!!.onResult(updateResult(probabilities))
                    // [END mlKit_read_result]
                    // [END_EXCLUDE]
                }
                .addOnFailureListener { e -> // Task failed with an exception
                    methodsListener!!.onFailed(e)
                }
        // [END mlKit_run_inference]
    }

    /** Result Computation for selected Image  */
    private fun findImageResult(mProbabilities: FloatArray): Int {
        val loader = AssetsLoader(mContext)
        return try {
            Classifier(loader.loadOutputLabels(), loader.loadOutputVectors())
                    .getResult(mProbabilities)
        } catch (e: IOException) {
            e.printStackTrace()
            -1
        }
    }

    private fun updateResult(mProbabilities: FloatArray): String = when (findImageResult(mProbabilities)) {
        0 -> mContext.resources.getString(R.string.rock)
        1 -> mContext.resources.getString(R.string.paper)
        2 -> mContext.resources.getString(R.string.scissor)
        else -> mContext.resources.getString(R.string.error)
    }

}