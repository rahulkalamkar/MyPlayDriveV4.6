package com.hungama.myplay.activity.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.fragments.FeedbackFragment;

public class FeedbackActivity extends SecondaryActivity {

	private static final String TAG = "FeedbackActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_with_title);
		onCreateCode();

		// setContentView(R.layout.activity_with_title);

		// sets the Title.
		// LanguageTextView title = (LanguageTextView)
		// findViewById(R.id.main_title_bar_text);
		// title.setText(Utils.getMultilanguageTextLayOut(getApplicationContext(),
		// getString(R.string.feedback_title)));

		// adds the feedback fragment.
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager
				.beginTransaction();

		FeedbackFragment feedbackFragment = new FeedbackFragment();
		fragmentTransaction.add(R.id.main_fragmant_container, feedbackFragment);
		fragmentTransaction.commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		setActionBarTitle(getString(R.string.feedback_title));
	}
}
