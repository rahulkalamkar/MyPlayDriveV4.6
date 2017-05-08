package com.hungama.myplay.activity.ui.fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.AboutActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

public class AboutFragment extends Fragment {

	private static final String TAG = "AboutFragment";

	private DataManager mDataManager;

	private String mFacebookUrl;
	private String mTwitterUrl;
	private String mGoogleUrl;
	private String mTermsUrl;
	// private String mPrivacyUrl;

	private LanguageTextView mTextTitle;
	private ImageButton mButtonFacebook;
	private ImageButton mButtonTwitter;
	private ImageButton mButtonGooglePlus;

	private LanguageButton mButtonTerms;

	// private Button mButtonPrivacy;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());

		Resources resources = getResources();

		mFacebookUrl = resources
				.getString(R.string.hungama_server_url_follow_facebook);
		mTwitterUrl = resources
				.getString(R.string.hungama_server_url_follow_twitter);
		mGoogleUrl = resources
				.getString(R.string.hungama_server_url_follow_google);
		mTermsUrl = resources
				.getString(R.string.hungama_server_url_term_of_use);
		// mPrivacyUrl =
		// resources.getString(R.string.hungama_server_url_privacy_policy);
		Analytics.postCrashlitycsLog(getActivity(), AboutFragment.class.getName());
	}

	@Override
	public void onStart() {
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(getActivity(),
		// getString(R.string.flurry_app_key));
		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
	}

	@Override
	public void onStop() {
		super.onStop();

		Analytics.onEndSession(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		View rootView = inflater.inflate(R.layout.fragment_about, container,
				false);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}
		// initializes and populates the views.

		mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.about_title_version);
		mButtonFacebook = (ImageButton) rootView
				.findViewById(R.id.about_button_facebook);
		mButtonTwitter = (ImageButton) rootView
				.findViewById(R.id.about_button_twitter);
		mButtonGooglePlus = (ImageButton) rootView
				.findViewById(R.id.about_button_google_plus);

		mButtonTerms = (LanguageButton) rootView
				.findViewById(R.id.about_button_terms);
		mButtonTerms.setText(Utils.getMultilanguageTextLayOut(getActivity(),
				getString(R.string.about_terms_of_use))
				+ " & "
				+ Utils.getMultilanguageTextLayOut(getActivity(),
						getString(R.string.about_privacy_policy)));
		// mButtonPrivacy = (Button)
		// rootView.findViewById(R.id.about_button_privacy);

		String appVersion = mDataManager.getServerConfigurations()
				.getAppVersion();
		String title = getResources().getString(R.string.about_title_version);
		mTextTitle.setText(Utils.getMultilanguageTextLayOut(getActivity(),
				title) + " " + appVersion);
		((LanguageTextView) rootView.findViewById(R.id.textView1))
				.setText(Utils.getMultilanguageTextLayOut(getActivity(),
						getString(R.string.about_section_play))
						+ " & "
						+ Utils.getMultilanguageTextLayOut(getActivity(),
								getString(R.string.about_section_play_2))
						+ " & "
						+ Utils.getMultilanguageTextLayOut(getActivity(),
								getString(R.string.about_section_play_3)));

		mButtonFacebook.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ScreenLockStatus.getInstance(getActivity()).dontShowAd();
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(mFacebookUrl));
				startActivity(i);
			}
		});
		mButtonTwitter.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ScreenLockStatus.getInstance(getActivity()).dontShowAd();
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(mTwitterUrl));
				startActivity(i);
			}
		});
		mButtonGooglePlus.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				ScreenLockStatus.getInstance(getActivity()).dontShowAd();
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(mGoogleUrl));
				startActivity(i);
			}
		});

		mButtonTerms.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showWebviewPage(mTermsUrl);
			}
		});

		// mButtonPrivacy.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// showWebviewPage(mPrivacyUrl);
		// }
		// });

		return rootView;
	}

	private void showWebviewPage(String url) {
		((AboutActivity) getActivity()).showWebviewPage(url);
	}

	@Override
	public void onDestroyView() {
		// System.gc();
		// System.runFinalization();
		super.onDestroyView();
	}
}
