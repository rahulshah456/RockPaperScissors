package com.example.retailpulseassignment.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.retailpulseassignment.R;
import com.example.retailpulseassignment.activities.GalleryActivity;
import com.google.android.material.card.MaterialCardView;

public class MethodsFragment extends Fragment {

    public static final String TAG = MethodsFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_methods,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // init click declarations
        view.findViewById(R.id.cv_gallery).setOnClickListener(onClickListener);
        view.findViewById(R.id.cv_camera).setOnClickListener(onClickListener);
        view.findViewById(R.id.cv_live).setOnClickListener(onClickListener);
    }


    // init listeners
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch (v.getId()){
                case R.id.cv_gallery:
                    startActivity(new Intent(getContext(),GalleryActivity.class));
                    break;
                case R.id.cv_camera:
                    // TODO activity intent
                    break;
                case R.id.cv_live:
                    // TODO activity intent
                    break;
            }

        }
    };
}
