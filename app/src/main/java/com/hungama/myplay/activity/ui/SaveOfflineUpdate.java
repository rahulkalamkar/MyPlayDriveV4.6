package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

public class SaveOfflineUpdate extends Activity implements OnClickListener {

	boolean isLearnMore = false;
	boolean isBackToInfo = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getApplicationContext());
		RelativeLayout root = (RelativeLayout) LayoutInflater.from(
				SaveOfflineUpdate.this).inflate(R.layout.save_offline_update,
				null);
		setContentView(root);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(root, SaveOfflineUpdate.this);
		}

		isLearnMore = getIntent().getBooleanExtra("isLearnMore", false);

		WebView webview = (WebView) findViewById(R.id.web_information);
		try {
			WebSettings webSettings = webview.getSettings();
			webSettings.setJavaScriptEnabled(true);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		webview.setWebViewClient(new WebViewClient() {
			MyProgressDialog mProgressDialog;

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				isBackToInfo = true;
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				try {
					if (mProgressDialog == null) {
						// mProgressDialog = ProgressDialog.show(
						// SaveOfflineUpdate.this,
						// "",
						// Utils.getMultilanguageTextHindi(
						// getApplicationContext(),
						// getString(R.string.application_dialog_loading_content)),
						// true);
						mProgressDialog = new MyProgressDialog(
								SaveOfflineUpdate.this);
						mProgressDialog.setCancelable(true);
						mProgressDialog.setCanceledOnTouchOutside(false);
					}
					super.onPageStarted(view, url, favicon);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				try {
					if (mProgressDialog != null) {
						mProgressDialog.dismiss();
						mProgressDialog = null;
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}

			@Override
			public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
				if (Logger.enableSSL) {
//					super.onReceivedSslError(view, handler, error);
					Toast.makeText(SaveOfflineUpdate.this, "SSL error.", Toast.LENGTH_SHORT).show();
					handler.cancel();
					finish();
				} else
					handler.proceed();
			}
		});
		if (isLearnMore)
			webview.loadUrl(getResources().getString(
					R.string.hungama_server_url_save_offline_learn_more));
		else
			webview.loadUrl(getResources().getString(
					R.string.hungama_server_url_new_goodies));

		if (!isLearnMore) {
			if (CacheManager.isProUser(this))
				findViewById(R.id.button_go_pro).setVisibility(View.INVISIBLE);
			else
				findViewById(R.id.button_go_pro).setOnClickListener(this);
			findViewById(R.id.button_get_started).setOnClickListener(this);
		} else
			findViewById(R.id.ll_bottom_buttons).setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_go_pro:
			Intent intent = new Intent(this, UpgradeActivity.class);
			startActivityForResult(intent,
					HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
			// finish();
			break;
		case R.id.button_get_started:
			finished();
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE:
			// No specific action needed
			finished();
			break;
		case RESULT_ACTIVITY_CODE_REPLACEMENTS:
			Intent startHomeActivityIntent = new Intent(
					getApplicationContext(), HomeActivity.class);
			if (ApplicationConfigurations.getInstance(this)
					.getSaveOfflineMode())
				startHomeActivityIntent = new Intent(getApplicationContext(),
						GoOfflineActivity.class);
			startHomeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NO_ANIMATION);
			startHomeActivityIntent.putExtra("show_onboarding", true);
			startActivity(startHomeActivityIntent);
			finish();
		}

	}

	@Override
	public void onBackPressed() {
		if (isBackToInfo) {
			isBackToInfo = false;
			WebView webview = (WebView) findViewById(R.id.web_information);
			webview.loadUrl(getResources().getString(
					R.string.hungama_server_url_new_goodies));
		} else
			super.onBackPressed();
	}

	private static final int RESULT_ACTIVITY_CODE_REPLACEMENTS = 2;

	public void finished() {
		// if (!isLearnMore) {
		// Intent startHomeActivityIntent = new Intent(
		// getApplicationContext(), HomeActivity.class);
		// if (ApplicationConfigurations.getInstance(this).getSaveOfflineMode())
		// startHomeActivityIntent = new Intent(getApplicationContext(),
		// GoOfflineActivity.class);
		// startHomeActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
		// | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		// startActivity(startHomeActivityIntent);
		// }
		if (CacheManager.isProUser(this)) {
			Intent intent = new Intent(this, SaveFavoritesOfflineActivity.class);
			intent.putExtra("isLearnMore",
					getIntent().getBooleanExtra("isLearnMore", false));
			startActivity(intent);
			finish();

		} else if (!isLearnMore) {
			Intent replacementsIntent = new Intent(this,
					PlacementSplashActivity.class);
			replacementsIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
					| Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivityForResult(replacementsIntent,
					RESULT_ACTIVITY_CODE_REPLACEMENTS);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).dontShowAd();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();
	}

	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onResume() {
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
		super.onResume();
	}
}
