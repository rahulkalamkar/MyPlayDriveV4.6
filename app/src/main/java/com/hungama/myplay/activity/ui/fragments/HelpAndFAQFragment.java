package com.hungama.myplay.activity.ui.fragments;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.HelpAndFAQActivity;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Utils;

public class HelpAndFAQFragment extends Fragment implements OnClickListener {

	private static final String TAG = "HelpAndFAQFragment";

	private RelativeLayout mSectionAppCrashing;
	private ImageView mButtonAppCrashingOpenContent;
	// private TextView mTextApplCrashingContent;
	private WebView mTextApplCrashingContent;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private LinearLayout mContainerSlidingSections;
	private RelativeLayout mSectionReadFAQ;
	private RelativeLayout mSectionReportProblem;

	private static final long ANIMATION_DURATION = 500;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		super.onCreate(savedInstanceState);
		Analytics.postCrashlitycsLog(getActivity(), HelpAndFAQFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_help_faq, container,
				false);
		if (mApplicationConfigurations.getUserSelectedLanguage() != Constants.LANGUAGE_ENGLISH) {
			Utils.traverseChild(rootView, getActivity());
		}
		mSectionAppCrashing = (RelativeLayout) rootView
				.findViewById(R.id.help_faq_section_app_crashing);
		mButtonAppCrashingOpenContent = (ImageView) rootView
				.findViewById(R.id.help_faq_section_app_crashing_button_content);
		// mTextApplCrashingContent = (TextView)
		// rootView.findViewById(R.id.help_faq_section_app_crashing_text);
		String text = "<html><body style=\"text-align:justify;color:#666666;font-size:80%;\"><p align=\"justify\">"
				+ getString(R.string.help_faq_section_app_crashes_content)
				+ "</p></body></html>";
		// mTextApplCrashingContent.setText(Html.fromHtml("<html><body style=\"text-align:justify;\"><p align=\"justify\">"
		// + getString(R.string.help_faq_section_app_crashes_content) +
		// "</p></body></html>"));

		mTextApplCrashingContent = (WebView) rootView
				.findViewById(R.id.help_faq_section_app_crashing_web_text);
		mTextApplCrashingContent.loadData(text, "text/html", "utf-8");
		mTextApplCrashingContent.setBackgroundColor(getResources().getColor(
				R.color.application_background_grey));

		mContainerSlidingSections = (LinearLayout) rootView
				.findViewById(R.id.help_faq_container_sliding_sections);
		mSectionReadFAQ = (RelativeLayout) rootView
				.findViewById(R.id.help_faq_section_read_faq);
		mSectionReportProblem = (RelativeLayout) rootView
				.findViewById(R.id.help_faq_section_report_a_problem);

		mSectionAppCrashing.setOnClickListener(this);
		mSectionReadFAQ.setOnClickListener(this);
		mSectionReportProblem.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		if (viewId == R.id.help_faq_section_app_crashing) {

			if (mTextApplCrashingContent.getVisibility() == View.VISIBLE) {
				// hides the content.
				hideAppCrashingContent();
			} else {
				// shows the content.
				showAppCrashingContent();
			}

		} else if (viewId == R.id.help_faq_section_read_faq) {
			String url = getResources().getString(
					R.string.hungama_server_url_faq);
			((HelpAndFAQActivity) getActivity()).showWebviewPage(url);

		} else if (viewId == R.id.help_faq_section_report_a_problem) {
			((HelpAndFAQActivity) getActivity()).showFeedbackPage();
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStop()
	 */
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	private void showAppCrashingContent() {

		// final int marginFromSection = mTextApplCrashingContent.getHeight();

		// slides down the other sections.
		// ViewPropertyAnimator.animate(mContainerSlidingSections)
		// .setDuration(ANIMATION_DURATION)
		// .yBy(marginFromSection)
		// .setListener(new AnimatorListener() {
		//
		// @Override
		// public void onAnimationStart(Animator animation) {
		// mTextApplCrashingContent.setVisibility(View.VISIBLE);
		// }
		//
		// @Override
		// public void onAnimationEnd(Animator animation) {}
		//
		// @Override
		// public void onAnimationRepeat(Animator animation) {}
		//
		// @Override
		// public void onAnimationCancel(Animator animation) {}
		//
		// }).start();

		mTextApplCrashingContent.setVisibility(View.VISIBLE);

		ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(
				mButtonAppCrashingOpenContent, "rotation", 0f, -180f);
		imageViewObjectAnimator.setDuration(ANIMATION_DURATION); // miliseconds
		imageViewObjectAnimator.start();

		// rotates the button.
		// new ViewPropertyAnimator().animate(mButtonAppCrashingOpenContent)
		// .setDuration(ANIMATION_DURATION).rotation(-180).start();
	}

	private void hideAppCrashingContent() {

		// final int marginFromSection = mTextApplCrashingContent.getHeight();

		// slides up the other sections.
		// ViewPropertyAnimator.animate(mContainerSlidingSections)
		// .setDuration(ANIMATION_DURATION)
		// .yBy(-marginFromSection) // beware of the "-" sign.
		// .setListener(new AnimatorListener() {
		//
		// @Override
		// public void onAnimationStart(Animator animation) {}
		//
		// @Override
		// public void onAnimationEnd(Animator animation) {
		// // hides the text.
		// mTextApplCrashingContent.setVisibility(View.INVISIBLE);
		// }
		//
		// @Override
		// public void onAnimationRepeat(Animator animation) {}
		//
		// @Override
		// public void onAnimationCancel(Animator animation) {}
		//
		// }).start();

		mTextApplCrashingContent.setVisibility(View.GONE);

		ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(
				mButtonAppCrashingOpenContent, "rotation", 0f, 0f);
		imageViewObjectAnimator.setDuration(ANIMATION_DURATION); // miliseconds
		imageViewObjectAnimator.start();

		// rotates the button.
		// ViewPropertyAnimator.animate(mButtonAppCrashingOpenContent)
		// .setDuration(ANIMATION_DURATION).rotation(0) // beware of the
		// // "-" sign.
		// .start();
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}
}
