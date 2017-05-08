package com.hungama.myplay.activity.ui.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.RedeemActivity;
import com.hungama.myplay.activity.ui.WebviewNativeActivity;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class RedeemFragment extends Fragment implements OnClickListener {

	private View rootView;
	private Bundle detailsData;
	private int numOfCoins;
	private TextView mTextViewSendEmail;

	// ======================================================
	// Fragment lifecycle methods
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.postCrashlitycsLog(getActivity(), RedeemFragment.class.getName());
	}

	@Override
	public void onStart() {

		super.onStart();
		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryKeys.Source.toString(),
				"My Profile");
		Analytics.logEvent(FlurryConstants.FlurryKeys.Rewards.toString(),
				reportMap);
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

    ProfileActivity profileActivity;
    public void setProfileActivity(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }

	public ProfileActivity getProfileActivity() {
		return profileActivity;
	}

	public void setTitle(){
        if(profileActivity!=null)
            profileActivity.setTitle(true);
    }
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {

		if (v.equals(mTextViewSendEmail)) {
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("plain/text");
			intent.putExtra(Intent.EXTRA_EMAIL, new String[] { getActivity()
					.getResources().getString(R.string.redeem_email_to) });
			intent.putExtra(Intent.EXTRA_SUBJECT, (String) getActivity()
					.getString(R.string.redeem_email_subject));
			startActivity(Intent.createChooser(intent, ""));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		try {
			rootView = inflater.inflate(R.layout.fragment_redeem_new,
					container, false);
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(getActivity());
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
			rootView = inflater.inflate(R.layout.fragment_redeem_new,
					container, false);
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(getActivity());
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}
		}
		LinearLayout myCoinsTitle = (LinearLayout) rootView
				.findViewById(R.id.left_invite_text_title);

		detailsData = getArguments();
		numOfCoins = detailsData.getInt(RedeemActivity.ARGUMENT_REDEEM);

		if (numOfCoins == RedeemActivity.FROM_MAIN_MENU) {

			myCoinsTitle.setVisibility(View.GONE);

		} else {

			myCoinsTitle.setVisibility(View.VISIBLE);
			LanguageTextView numCoins = (LanguageTextView) rootView
					.findViewById(R.id.free_song_text);
			numCoins.setText(Utils.getMultilanguageTextLayOut(getActivity(),
					getResources().getString(R.string.redeem_my_coins))
					+ " "
					+ numOfCoins);
		}

		Logger.i("RedeemFragment", rootView.toString());

		WebView webview = (WebView) rootView
				.findViewById(R.id.web_view_redeem_content);
		try {
			WebSettings webSettings = webview.getSettings();
			webSettings.setJavaScriptEnabled(true);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		webview.setWebViewClient(new WebViewClient() {
			MyProgressDialog mProgressDialog;

			@TargetApi(Build.VERSION_CODES.M)
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if(url.startsWith("mailto:")){
					Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse(url));
					startActivity(i);
				} else if (url.startsWith("tel:")) {
					strCallIntent = null;
					if (Utils.isAndroidM() &&
							getActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
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
//					Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//					startActivity(intent);
					Intent i = new Intent(getActivity(), WebviewNativeActivity.class);
					i.putExtra("url", url);
					i.putExtra("title_menu", getString(R.string.redeem_text));
					startActivity(i);
				}
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				try {
					if (mProgressDialog == null) {

						mProgressDialog = new MyProgressDialog(getActivity());
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
					Toast.makeText(getActivity(), "SSL error.", Toast.LENGTH_SHORT).show();
					handler.cancel();
					getActivity().finish();
				} else
					handler.proceed();
			}
		});
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations.getInstance(getActivity());
		if(TextUtils.isEmpty(mApplicationConfigurations.getRedeemUrl())) {
			webview.loadUrl(getResources().getString(
					R.string.hungama_server_url_rewards));
		} else {
			webview.loadUrl(mApplicationConfigurations.getRedeemUrl());
		}
		return rootView;
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	private String strCallIntent = null;
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if(requestCode==10001){
			if (Utils.isAndroidM()) {
				if (getActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
					if(!TextUtils.isEmpty(strCallIntent)){
						Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(strCallIntent));
						startActivityForResult(callIntent, 1001);
					}
				}
			}
		}
	}
}
