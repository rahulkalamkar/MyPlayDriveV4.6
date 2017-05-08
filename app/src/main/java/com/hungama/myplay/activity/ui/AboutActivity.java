package com.hungama.myplay.activity.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.fragments.AboutFragment;
import com.hungama.myplay.activity.ui.fragments.AboutWebViewFragment;

public class AboutActivity extends SecondaryActivity {

	private static final String TAG = "AboutActivity";

	private FragmentManager mFragmentManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_with_title);
		onCreateCode();
		// setContentView(R.layout.activity_with_title);
		mFragmentManager = getSupportFragmentManager();

		// sets the Title.
		// LanguageTextView title = (LanguageTextView)
		// findViewById(R.id.main_title_bar_text);
		// title.setText(Utils.getMultilanguageTextLayOut(getApplicationContext(),
		// getString(R.string.about_title)));

		// adds the feedback fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();

		AboutFragment feedbackFragment = new AboutFragment();
		fragmentTransaction.add(R.id.main_fragmant_container, feedbackFragment);
		fragmentTransaction.commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setActionBarTitle(getString(R.string.about_title));
	}

	@Override
	public void onBackPressed() {
		// checks if the webview exists and calls its back button support.
		Fragment fragment = mFragmentManager
				.findFragmentByTag(AboutWebViewFragment.FRAGMENT_WEBVIEW);
		if (fragment != null) {
			AboutWebViewFragment aboutWebViewFragment = (AboutWebViewFragment) fragment;
			boolean hasSupported = aboutWebViewFragment.onBackPressed();
			if (hasSupported) {
				return;
			}
		}
		super.onBackPressed();
	}

	public void showWebviewPage(String url) {

		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();

		AboutWebViewFragment aboutWebViewFragment = new AboutWebViewFragment();

		Bundle arguments = new Bundle();
		arguments.putString(AboutWebViewFragment.FRAGMENT_ARGUMENT_URL, url);
		aboutWebViewFragment.setArguments(arguments);

		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		fragmentTransaction.replace(R.id.main_fragmant_container,
				aboutWebViewFragment, AboutWebViewFragment.FRAGMENT_WEBVIEW);

		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

}
