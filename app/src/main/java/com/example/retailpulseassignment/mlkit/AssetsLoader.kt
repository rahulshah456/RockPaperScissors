package com.example.retailpulseassignment.mlkit

import android.content.Context
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*

class AssetsLoader(private val mContext: Context) {

    @Throws(IOException::class)
    fun loadOutputVectors(): Map<Int, DoubleArray> {
        val integerMap: MutableMap<Int, DoubleArray> = HashMap()
        var st: StringTokenizer
        val tsvFile = BufferedReader(InputStreamReader(mContext.assets.open("rps_vecs.tsv")))
        var dataRow = tsvFile.readLine() // Read first line.
        var count = 0
        while (dataRow != null) {
            st = StringTokenizer(dataRow, "\t")
            val dataArray: MutableList<String> = ArrayList()
            while (st.hasMoreElements()) {
                dataArray.add(st.nextElement().toString())
            }
            val list = DoubleArray(16)
            for (i in dataArray.indices) {
                list[i] = dataArray[i].toDouble()
            }
            integerMap[count] = list
            count++
            dataRow = tsvFile.readLine() // Read next line of data.
        }
        // Close the file once all data has been read.
        tsvFile.close()
        // End the printout with a blank line.
        return integerMap
    }

    @Throws(IOException::class)
    fun loadOutputLabels(): IntArray {
        val reader = BufferedReader(InputStreamReader(mContext.assets.open("rps_labels.tsv")))
        val labels = IntArray(32)
        var count = 0
        var labelRow = reader.readLine()
        while (labelRow != null) {
            labels[count] = labelRow.toInt()
            count++
            labelRow = reader.readLine()
        }
        return labels
    }

}