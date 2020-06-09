package com.example.retailpulseassignment.views.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.retailpulseassignment.R
import com.example.retailpulseassignment.views.fragments.DownloadFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //adding base fragment
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fl_main, DownloadFragment(), DownloadFragment.TAG)
                .commit()

    }
}