package com.rahulshah456.rockpaperscissors.mlkit

import android.graphics.Bitmap
import com.rahulshah456.rockpaperscissors.utils.Constants
import org.apache.commons.math3.ml.distance.EuclideanDistance
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class Classifier(private val mLabels: IntArray, private val mVectorOutputs: Map<Int, DoubleArray>) {


    @Throws(IOException::class)
    fun getResult(outProbabilities: FloatArray?): Int {
        val euclideanDistances = DoubleArray(32)
        for (i in 0 until  mVectorOutputs.size) {
            euclideanDistances[i] = EuclideanDistance()
                    .compute(Objects.requireNonNull(mVectorOutputs[i]),
                            convertFloatsToDoubles(outProbabilities))
        }
        return getMinEuclideanDistance(euclideanDistances)
    }

    @Throws(IOException::class)
    private fun getMinEuclideanDistance(euclideanDistances: DoubleArray): Int {
        var minDistance = euclideanDistances[0]
        var pos = 0
        for (i in euclideanDistances.indices) {
            if (minDistance > euclideanDistances[i]) {
                minDistance = euclideanDistances[i]
                pos = i
            }
        }
        return mLabels[pos]
    }


    companion object {

        @JvmStatic
        fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
            val byteBuffer = ByteBuffer.allocateDirect(4 * Constants.BATCH_SIZE * Constants.INPUT_SIZE * Constants.INPUT_SIZE * Constants.PIXEL_SIZE)
            byteBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(Constants.INPUT_SIZE * Constants.INPUT_SIZE)
            bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            var pixel = 0
            for (i in 0 until Constants.INPUT_SIZE) {
                for (j in 0 until Constants.INPUT_SIZE) {
                    val `val` = intValues[pixel++]
                    byteBuffer.putFloat(((`val` shr 16 and 0xFF) - Constants.IMAGE_MEAN) / Constants.IMAGE_STD)
                    byteBuffer.putFloat(((`val` shr 8 and 0xFF) - Constants.IMAGE_MEAN) / Constants.IMAGE_STD)
                    byteBuffer.putFloat(((`val` and 0xFF) - Constants.IMAGE_MEAN) / Constants.IMAGE_STD)
                }
            }
            return byteBuffer
        }

        @JvmStatic
        private fun convertFloatsToDoubles(input: FloatArray?): DoubleArray? {
            if (input == null) {
                return null // Or throw an exception - your choice
            }
            val output = DoubleArray(input.size)
            for (i in input.indices) {
                output[i] = input[i].toDouble()
            }
            return output
        }

    }




}