package com.hungama.myplay.activity.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.DiscoveryActivity;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

/**
 * Example about replacing fragments inside a ViewPager. I'm using
 * android-support-v7 to maximize the compatibility.
 * 
 * @author hungama
 * 
 */
public class RootFragmentDiscovery extends Fragment {

	public DiscoveryActivity fragment;
	View rootView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		/* Inflate the layout for this fragment */
		try {
			if (rootView == null) {
				rootView = inflater.inflate(R.layout.root_fragment_discovery,
						container, false);

				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				/*
				 * When this container fragment is created, we fill it with our
				 * first "real" fragment
				 */
				fragment = new DiscoveryActivity();
				Bundle arguments = getArguments();
				fragment.setArguments(arguments);
				transaction.replace(R.id.root_frame_discovery, fragment);

				transaction.commit();
			} else {
				ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
				parent.removeView(rootView);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		return rootView;
	}

	OnMediaItemOptionSelectedListener listener;

	public void setOnMediaItemOptionSelectedListener(
			OnMediaItemOptionSelectedListener homeActivity) {
		this.listener = homeActivity;
	}

	public void openOptions() {
		fragment.openOptions();
	}

	public void closeOptions() {
		fragment.openOptions();
	}

	public void postAd() {
		fragment.postAd();
	}
}
