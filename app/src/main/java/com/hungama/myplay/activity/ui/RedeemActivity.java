package com.hungama.myplay.activity.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.ui.fragments.RedeemFragment;
import com.hungama.myplay.activity.util.ScreenLockStatus;

public class RedeemActivity extends SecondaryActivity {

	public static final int FROM_MAIN_MENU = -1;
	public static final String ARGUMENT_REDEEM = "argument_redeem";

	// ======================================================
	// ACTIVITY'S LIFECYCLE.
	// ======================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// setOverlayAction();
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_redeem);
		onCreateCode();
		// setContentView(R.layout.activity_redeem);

		// getDrawerLayout();
		// SetS title bar
		// LanguageTextView mTitleBarText = (LanguageTextView)
		// findViewById(R.id.main_title_bar_text);
		// mTitleBarText.setText(Utils.getMultilanguageTextLayOut(
		// getApplicationContext(),
		// getResources().getString(R.string.redeem_title)));

		// ImageButton arrow = (ImageButton)
		// findViewById(R.id.main_title_bar_button_options);
		// arrow.setVisibility(View.GONE);

		Bundle data = new Bundle();
		data.putInt(ARGUMENT_REDEEM, FROM_MAIN_MENU);
		addFragment(data);

		// WebView webview = (WebView)
		// findViewById(R.id.web_view_redeem_content);
		// try {
		// WebSettings webSettings = webview.getSettings();
		// webSettings.setJavaScriptEnabled(true);
		// } catch (Exception e) {
		// Logger.printStackTrace(e);
		// }
		// webview.setWebViewClient(new WebViewClient() {
		// ProgressDialog mProgressDialog;
		// @Override
		// public boolean shouldOverrideUrlLoading(WebView view, String url) {
		// // isBackToInfo = true;
		// // view.loadUrl(url);
		// Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		// startActivity(intent);
		// return true;
		// }
		//
		// @Override
		// public void onPageStarted(WebView view, String url, Bitmap favicon) {
		// try {
		// if (mProgressDialog == null) {
		// mProgressDialog = ProgressDialog.show(
		// RedeemActivity.this,
		// "",
		// Utils.getMultilanguageTextHindi(
		// getApplicationContext(),
		// getString(R.string.application_dialog_loading_content)),
		// true);
		// mProgressDialog.setCancelable(true);
		// mProgressDialog.setCanceledOnTouchOutside(false);
		// }
		// super.onPageStarted(view, url, favicon);
		// } catch (Exception e) {
		// Logger.printStackTrace(e);
		// }
		// }
		//
		// @Override
		// public void onPageFinished(WebView view, String url) {
		// super.onPageFinished(view, url);
		// try {
		// if (mProgressDialog != null) {
		// mProgressDialog.dismiss();
		// mProgressDialog = null;
		// }
		// } catch (Exception e) {
		// Logger.printStackTrace(e);
		// }
		// }
		// });
		// webview.loadUrl(getResources().getString(
		// R.string.hungama_server_url_rewards));
	}

	@Override
	protected void onStart() {
		super.onStart();
		setActionBarTitle(getString(R.string.redeem_title));
	}

	@Override
	protected void onResume() {
		ScreenLockStatus.getInstance(getBaseContext()).dontShowAd();
		HungamaApplication.activityResumed();
		// ApplicationConfigurations mApplicationConfigurations = new
		// ApplicationConfigurations(getBaseContext());

		super.onResume();
	}

	// @Override
	// protected void onStop() {
	// HungamaApplication.activityStoped();
	// super.onStop();
	// }
	@Override
	protected void onPause() {
		HungamaApplication.activityPaused();
		super.onPause();
	}

	// ======================================================
	// Helper Methods.
	// ======================================================

	public void addFragment(Bundle detailsData) {

		RedeemFragment mRedeemFragment = new RedeemFragment();
		mRedeemFragment.setArguments(detailsData);
		// mMediaTileGridFragment.setOnMediaItemOptionSelectedListener(this);

		FragmentManager mFragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);
		fragmentTransaction.add(R.id.main_fragmant_container, mRedeemFragment);
		fragmentTransaction.commit();
	}
}