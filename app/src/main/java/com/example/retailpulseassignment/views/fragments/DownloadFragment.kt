package com.example.retailpulseassignment.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.retailpulseassignment.R
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel

class DownloadFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_download, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val remoteModel = FirebaseCustomRemoteModel.Builder("rps_model").build()
        val conditions = FirebaseModelDownloadConditions.Builder()
                .build()
        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                .addOnCompleteListener { // Success.
                    replaceFragment()
                }
    }

    private fun replaceFragment() {
        if (fragmentManager != null) {
            fragmentManager!!.beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fl_main, MethodsFragment(), MethodsFragment.TAG)
                    .commit()
        }
    }

    companion object {
        val TAG = DownloadFragment::class.java.simpleName
    }
}