package com.hungama.hungamamusic.lite.carmode.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hungama.hungamamusic.lite.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SplashScreenFragment extends Fragment {

    public static final String TAG = SplashScreenFragment.class.getSimpleName();

    public SplashScreenFragment() {
        // Required empty public constructor
    }

    public static final SplashScreenFragment newInstance() {
        SplashScreenFragment fragment = new SplashScreenFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_landscape_splashscreen, container, false);
    }


}
