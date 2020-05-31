package com.example.retailpulseassignment.views.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.retailpulseassignment.R;
import com.example.retailpulseassignment.views.fragments.MultipleGalleryFragment;
import com.example.retailpulseassignment.views.fragments.SoloGalleryFragment;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

import static com.example.retailpulseassignment.utils.Constants.IMAGE_URL;
import static com.example.retailpulseassignment.utils.Constants.IMAGE_URLS_LIST;
import static com.example.retailpulseassignment.utils.Constants.PICK_IMAGE_REQUEST;

public class GalleryActivity extends AppCompatActivity {

    public static final String TAG = GalleryActivity.class.getSimpleName();
    private String imageURL = null;
    private ArrayList<String> imageUrlsList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        MaterialToolbar toolbar = findViewById(R.id.mainToolbarId);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        if (savedInstanceState!=null){
            updateFragment(savedInstanceState);
        } else pickImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST){

            if (data == null) {
                Toast.makeText(this,"Failed to load image!",Toast.LENGTH_LONG).show();
                onBackPressed();
                return;
            }
            changeFragment(data);
        }

    }

    public void changeFragment(Intent data){
        if (data.getData()!=null){
            Uri uri = data.getData();
            if (uri!=null){
                imageURL = String.valueOf(uri);
                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                        .replace(R.id.fl_gallery, SoloGalleryFragment.newInstance(imageURL), SoloGalleryFragment.TAG)
                        .commit();
            }else {
                Toast.makeText(this,"No Image Selected!",Toast.LENGTH_LONG).show();
                onBackPressed();
            }
        } else if (data.getClipData()!=null){
            for (int i=0;i<data.getClipData().getItemCount();i++){
                String imageURL = String.valueOf(data.getClipData().getItemAt(i).getUri());
                Log.d(TAG, "changeFragment: pos = " + i + " & ImageURl = " + imageURL);
                imageUrlsList.add(imageURL);
            }
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fl_gallery, MultipleGalleryFragment.newInstance(imageUrlsList), MultipleGalleryFragment.TAG)
                    .commit();
        }
    }
    public void updateFragment(Bundle savedBundle){

        imageURL = savedBundle.getString(IMAGE_URL,null);
        imageUrlsList = (ArrayList<String>) savedBundle.getSerializable(IMAGE_URLS_LIST);

        if (imageURL!=null){
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fl_gallery, SoloGalleryFragment.newInstance(imageURL), SoloGalleryFragment.TAG)
                    .commit();
        } else if (imageUrlsList!=null){
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                    .replace(R.id.fl_gallery, MultipleGalleryFragment.newInstance(imageUrlsList), MultipleGalleryFragment.TAG)
                    .commit();
        } else onBackPressed();

    }


    /** Local Image Actions */
    private void pickImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putString(IMAGE_URL, imageURL);
        outState.putSerializable(IMAGE_URLS_LIST, imageUrlsList);
        super.onSaveInstanceState(outState);
    }
}