package com.example.retailpulseassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.retailpulseassignment.fragments.DownloadFragment;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.fl_main, new DownloadFragment(), DownloadFragment.TAG)
                .commit();

    }
}
