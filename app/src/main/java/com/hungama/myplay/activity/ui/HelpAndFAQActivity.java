package com.hungama.myplay.activity.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.fragments.AboutWebViewFragment;
import com.hungama.myplay.activity.ui.fragments.FeedbackFragment;
import com.hungama.myplay.activity.ui.fragments.HelpAndFAQFragment;
import com.hungama.myplay.activity.util.Utils;

public class HelpAndFAQActivity extends SecondaryActivity {

	private FragmentManager mFragmentManager;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_with_title);
		onCreateCode();
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		// RelativeLayout root = (RelativeLayout) LayoutInflater.from(
		// HelpAndFAQActivity.this).inflate(R.layout.activity_with_title,
		// null);
		// setContentView(root);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(getWindow().getDecorView(),
					HelpAndFAQActivity.this);
		}

		mFragmentManager = getSupportFragmentManager();

		// sets the Title.
		// LanguageTextView title = (LanguageTextView)
		// findViewById(R.id.main_title_bar_text);
		// Utils.SetMultilanguageTextOnTextView(HelpAndFAQActivity.this, title,
		// getResources()
		// .getString(R.string.help_faq_title));

		// adds the feedback fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();

		HelpAndFAQFragment feedbackFragment = new HelpAndFAQFragment();
		fragmentTransaction.add(R.id.main_fragmant_container, feedbackFragment);
		fragmentTransaction.commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setActionBarTitle(getString(R.string.help_faq_title));
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

	public void showFeedbackPage() {
		// adds the feedback fragment.
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();

		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		FeedbackFragment feedbackFragment = new FeedbackFragment();
		fragmentTransaction.replace(R.id.main_fragmant_container,
				feedbackFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}
}
