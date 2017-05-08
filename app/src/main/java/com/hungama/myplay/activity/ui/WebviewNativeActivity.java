package com.hungama.myplay.activity.ui;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class WebviewNativeActivity extends SecondaryActivity {
	WebView view;
	String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_webview);
		onCreateCode();

		if(getIntent().getBooleanExtra("is_inapp", false)) {
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations.getInstance(this);
			String sesion = mApplicationConfigurations.getSessionID();
			boolean isRealUser = mApplicationConfigurations.isRealUser();
			if (!TextUtils.isEmpty(sesion) && isRealUser) {
			} else {
				startLoginActivity();
				return;
			}
		}
		init();
	}

	private void init(){
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			if(getIntent().getBooleanExtra("is_inapp", false)){
				url = DataManager.getInstance(this).getHungamaRechargeURL();
			} else {
				url = bundle.getString("url");
			}
			setActionBarTitle(bundle.getString("title_menu"));
		}

		view = (WebView) findViewById(R.id.webview);
		view.getSettings().setAllowContentAccess(true);
		view.getSettings().setAppCacheEnabled(true);
		view.getSettings().setJavaScriptEnabled(true);
		view.loadUrl(url);
		view.setWebViewClient(new WebViewClient() {
			@TargetApi(Build.VERSION_CODES.M)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Logger.i("url override:", url);
				if (url != null && url.endsWith("upgrade_popup")) {
					Intent intent = new Intent(WebviewNativeActivity.this,
							UpgradeActivity.class);
					intent.putExtra(UpgradeActivity.IS_TRIAL_PLANS, true);

					startActivity(intent);
					finish();
					return true;
				} else if (url != null && url.contains("webvclose=1")) {
					finish();
					return true;
				}
				if (url != null && url.startsWith("mailto:")) {
					Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
					startActivity(i);
				} else if (url != null && url.startsWith("tel:")) {
					strCallIntent = null;
					if (Utils.isAndroidM() &&
							checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
						strCallIntent = url;
						String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
						requestPermissions(permissions, 10001);
						return true;
					} else {
						Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
						startActivityForResult(intent, 1001);
						return true;
					}
				} else {
					view.loadUrl(url);
				}
				return false;
			}

			MyProgressDialog mProgressDialog;

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				try {
					if (mProgressDialog == null) {
						mProgressDialog = new MyProgressDialog(WebviewNativeActivity.this);
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
					Toast.makeText(WebviewNativeActivity.this, "SSL error.", Toast.LENGTH_SHORT).show();
					handler.cancel();
					finish();
				} else
					handler.proceed();
			}
		});
	}

	@Override
	public void onBackPressed() {

		if (view.canGoBack()) {
			view.goBack();
			return;
		}

		super.onBackPressed();
	}

	private void startLoginActivity() {
		Intent startLoginActivityIntent = new Intent(getApplicationContext(),
				LoginActivity.class);
		startLoginActivityIntent.putExtra(UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY,
				"upgrade_activity");
		startLoginActivityIntent.putExtra(LoginActivity.FLURRY_SOURCE,
				FlurryConstants.FlurryUserStatus.Upgrade.toString());
		startActivityForResult(startLoginActivityIntent, UpgradeActivity.LOGIN_ACTIVITY_CODE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == UpgradeActivity.LOGIN_ACTIVITY_CODE && resultCode == RESULT_OK) {
			init();
		} else {
			finish();
		}
	}

	private String strCallIntent = null;
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode==10001){
			if (Utils.isAndroidM()) {
				if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
					if(!TextUtils.isEmpty(strCallIntent)){
						Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(strCallIntent));
						startActivityForResult(callIntent, 1001);
					}
				}
			}
		}
	}
}
