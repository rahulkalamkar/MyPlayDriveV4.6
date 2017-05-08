package com.hungama.myplay.activity.ui;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.DownloadOperationType;
import com.hungama.myplay.activity.data.dao.hungama.DownloadResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionNotifyBillingResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionPlan;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.DownloadOperation;
import com.hungama.myplay.activity.operations.hungama.SocialBadgeAlertOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionOperation;
import com.hungama.myplay.activity.services.DownloadFileService2;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.fragments.MembershipDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.fragments.RadioDetailsFragment;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.billing.IabHelper;
import com.hungama.myplay.activity.util.billing.IabResult;
import com.hungama.myplay.activity.util.billing.Inventory;
import com.hungama.myplay.activity.util.billing.Purchase;

import org.xml.sax.XMLReader;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HungamaPayActivity extends SecondaryActivity implements
		CommunicationOperationListener, IabHelper.OnIabSetupFinishedListener,
		IabHelper.OnIabPurchaseFinishedListener, IabHelper.QueryInventoryFinishedListener {
	private String TAG = "HungamaPayActivity";
	WebView view;
	Handler handler = new Handler();

	public static final String EXTRA_TRANSACTION_SESSION = "transaction_session";
	public static final String EXTRA_LONG_CONTENT_ID = "content_id";
	public static final String EXTRA_LONG_ALBUM_ID = "album_id";
	public static final String EXTRA_TITLE = "extra_title";
	public static final String EXTRA_MEDIA_ITEM = "extra_media_item";

	private Dialog upgradeDialog;
	long contentId, albumId;
	String transactionSession;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	private boolean isBindToGoogle = false;
	private String selectedPlan = null;
	// In App Billing
	private IabHelper billingHelper;
	public static final int PURCHASE_REQUEST_CODE = 123;
	private boolean isTransacted = false;

	private MediaItem mMediaItem;
	private Purchase sharedPurchase;
	private String plandetail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webbrowser);
		onCreateCode();
//		view = (WebView) findViewById(R.id.webview);
//		view.setWebViewClient(new HelloWebViewClient());

		initWebView();

		transactionSession = getIntent().getStringExtra(
				EXTRA_TRANSACTION_SESSION);

		contentId = getIntent().getLongExtra(EXTRA_LONG_CONTENT_ID, 0);
		albumId = getIntent().getLongExtra(EXTRA_LONG_ALBUM_ID, 0);

		mDataManager = DataManager.getInstance(HungamaPayActivity.this);
		mApplicationConfigurations = ApplicationConfigurations
				.getInstance(HungamaPayActivity.this);

		if (getIntent().getBooleanExtra("is_download", false)) {
			if (getIntent().getExtras().containsKey(EXTRA_MEDIA_ITEM)) {
				mMediaItem = (MediaItem) getIntent().getSerializableExtra(EXTRA_MEDIA_ITEM);
			}
		}

//		final String url;
//		if(getIntent().getBooleanExtra("is_download", false)){
//			url = mDataManager.getHungamaPayDownlaodURL(getIntent().getLongExtra("content_id",0));
//		} else {
//			url = mDataManager.getHungamaPaySubscribeURL();
//		}
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
////				view.loadUrl(getIntent().getData().toString());
//				view.loadUrl(url, getHeader());
//				Logger.e("WEbView", "getIntent().getData().toString() "
//						+ url);
//				view.getSettings().setJavaScriptCanOpenWindowsAutomatically(
//						true);
//				view.getSettings().setJavaScriptEnabled(true);
//			}
//		}, 500);
		proceedToUpgrade();
	}

	private void initWebView(){
		findViewById(R.id.ll_webview).setVisibility(View.VISIBLE);
		findViewById(R.id.webview).setVisibility(View.GONE);
		((LinearLayout) findViewById(R.id.ll_webview)).removeAllViews();
		view = new WebView(this);
		((LinearLayout) findViewById(R.id.ll_webview)).addView(view);
		view.setWebViewClient(new HelloWebViewClient());
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (getIntent().getBooleanExtra("is_download", false)) {
			setActionBarTitle(getIntent().getStringExtra(EXTRA_TITLE));
		} else {
			setActionBarTitle(getString(R.string.text_hungama_pro));
		}
	}

	private class HelloWebViewClient extends WebViewClient {

//		private boolean clearHistory = false;

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
//			Logger.s("onPageStarted :::::::::::: " + url);
			findViewById(R.id.ll_loading).setVisibility(View.VISIBLE);
			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
//			Logger.s("onPageFinished :::::::::::: " + url);
			Logger.s(previousURL);
			findViewById(R.id.ll_loading).setVisibility(View.INVISIBLE);
//			Logger.s(backCounter + " ::::::::::::::::: History clear. " + view.copyBackForwardList().getSize());
			if (clearHistory) {
				clearHistory = false;
//				view.clearHistory();
//				Logger.s("::::::::::::::::: History cleared. " + view.copyBackForwardList().getSize());
			}

			super.onPageFinished(view, url);
		}



		@Override
		public void onReceivedError(WebView view, int errorCode,
									String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Toast.makeText(HungamaPayActivity.this,
					"Problem :" + description, Toast.LENGTH_SHORT).show();
			finish();
			Logger.e("WEbView", "WEbView onReceivedError " + errorCode + " "
					+ description);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Logger.e("WEbView", "WEbView shouldOverrideUrlLoading " + url);
//			if(isBackTracking && !TextUtils.isEmpty(previousURL) && previousURL.equals(url)){
//				backCount++;
//				if(backCount>2) {
//					finish();
//					return true;
//				}
//			}
//			url = "tel:18002342342";
			Uri uri = Uri.parse(url);
			recentURL = null;
			//String webclose = uri.getQueryParameter("webvclose");
			// String dologin=uri.getQueryParameter("dologin");
			if (url.startsWith("tel:")) {
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
			} else if (url.contains("google_subscription_id=")) {
				String id = uri.getQueryParameter("google_subscription_id");
				try {
					if(!TextUtils.isEmpty(id)){
						selectedPlan = id;
						setupInAppBilling();
						return true;
					}/* else if(url.contains("dologin=")) {
						String dologin=uri.getQueryParameter("dologin");
						if(dologin.equals("1")){
							recentURL = url;
							startLoginActivity();
						}else if(url.contains("webvclose=")){
							String webclose=uri.getQueryParameter("webvclose");
							if(webclose.equals("1")){
								isTransacted = true;
								proceedToUpgrade();
							} else{
								view.loadUrl(url, getHeader());
							}
						} else{
							view.loadUrl(url, getHeader());
						}
					}*/ else if(url.contains("webvclose=")){
						String webclose=uri.getQueryParameter("webvclose");
						if(webclose.equals("1")){
//						clearHistory = true;
							isTransacted = true;
							proceedToUpgrade();
						} else{
							return false;
//							view.loadUrl(url, getHeader());
						}
					} else{
						return false;
//						view.loadUrl(url, getHeader());
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			} else if(url.contains("dologin=")) {
				String dologin=uri.getQueryParameter("dologin");
				if(dologin.equals("1")){
//					clearHistory = true;
//					recentURL = new String(previousURL);
//					previousURL = null;
					recentURL = url;
					startLoginActivity();
				}else if(url.contains("webvclose=")){
					String webclose=uri.getQueryParameter("webvclose");
					if(webclose.equals("1")){
//						clearHistory = true;
						isTransacted = true;
						proceedToUpgrade();
					} else{
						return false;
//						view.loadUrl(url, getHeader());
					}
				} else{
					return false;
//					view.loadUrl(url, getHeader());
				}
			} else if(url.contains("webvclose=")){
				String webclose=uri.getQueryParameter("webvclose");
				if(webclose.equals("1")){
 					isTransacted = true;
					proceedToUpgrade();
				} else{
					return false;
//					view.loadUrl(url, getHeader());
				}
			} else {
				previousURL = url;
				return false;
//				view.loadUrl(url, getHeader());
             }
			return true;
		}

		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			if (Logger.enableSSL) {
//					super.onReceivedSslError(view, handler, error);
				Toast.makeText(HungamaPayActivity.this, "SSL error.", Toast.LENGTH_SHORT).show();
				handler.cancel();
				finish();
			}else
				handler.proceed();
		}
	}

	private String strCallIntent = null;
	private String recentURL = null, previousURL = null;
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

  	@Override
	public void onBackPressed() {
//		Logger.s("::::::::::::::::: onBackPressed() " + view.copyBackForwardList().getSize());
//		Logger.s("::::::::::::::::: onBackPressed() " + view.copyBackForwardList().getCurrentItem().getUrl());
		if (view.canGoBack()) {
 			try {
				if (view.copyBackForwardList().getSize() <= 2 && !view.copyBackForwardList().getItemAtIndex(0).getUrl().startsWith
						(getString(R.string.hungama_pay_url))) {
					super.onBackPressed();
					return;
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			view.goBack();
		} else
			super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_hungama_pay_actionbar, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_close){
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK){
			showLoadingDialog();
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.DOWNLOAD) {
				DownloadResponse downloadResponse = (DownloadResponse) responseObjects
						.get(DownloadOperation.RESPONSE_KEY_DOWNLOAD);
                if (downloadResponse.getDownloadType() == DownloadOperationType.CONTENT_DELIVERY)
				{
					if(sharedPurchase!=null)
					{
						billingHelper.consumeAsync(sharedPurchase, new IabHelper.OnConsumeFinishedListener() {
							@Override
							public void onConsumeFinished(Purchase purchase, IabResult result) {
							}
						});
					}



//                    Intent intent = new Intent(this,
//                            DownloadFileService2.class);
//
//                    intent.putExtra(DownloadFileService2.DOWNLOAD_URL,
//							downloadResponse.getUrl());
//                    startService(intent);
//                    setResult(RESULT_OK);
//                    finish();

					String contentType = "song";
					if(getIntent().getStringExtra(EXTRA_TITLE).equals(getString(R.string.general_download_mp4))){
						contentType = MediaContentType.VIDEO.toString()
								.toLowerCase();
					}
					if(!TextUtils.isEmpty(downloadResponse.getMessage())) {
						if (mMediaItem != null && downloadResponse.getMessage().equalsIgnoreCase("success")) {
							if (mMediaItem.getMediaContentType() == null
									|| mMediaItem.getMediaContentType() != MediaContentType.VIDEO) {
								mMediaItem
										.setMediaContentType(MediaContentType.MUSIC);
							}
							Intent intent = new Intent(this,
									DownloadFileService2.class);
							intent.putExtra(DownloadFileService2.TRACK_KEY,
									(Serializable) mMediaItem);
							intent.putExtra(DownloadFileService2.DOWNLOAD_URL,
									downloadResponse.getUrl());
							startService(intent);

							mDataManager.checkBadgesAlert(
									String.valueOf(contentId),
									contentType, "music_video_download", this);

							// show a download success dialog
							showDownloadDialog(
									Utils.getMultilanguageTextLayOut(
											getApplicationContext(),
											getResources()
													.getString(
															R.string.download_media_success_title_thank_you)),
									Utils.getMultilanguageTextLayOut(
											getApplicationContext(),
											getResources()
													.getString(
															R.string.download_media_success_body)),
									false, false);
						} else {
							CustomAlertDialog cad = new CustomAlertDialog(this);
							cad.setMessage(downloadResponse.getMessage());
							cad.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									finish();
								}
							});
							cad.setOnCancelListener(new DialogInterface.OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									finish();
								}
							});
							cad.show();
						}
					} else {

						// show a download success dialog
						showDownloadDialog(
								Utils.getMultilanguageTextLayOut(
										getApplicationContext(),
										getResources()
												.getString(
														R.string.download_media_unsucceded_toast)),
								Utils.getMultilanguageTextLayOut(
										getApplicationContext(),
										getResources()
												.getString(
														R.string.download_media_unsucceded_toast_text)),
								false, false);
					}
                }
			} else if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION) {
				SubscriptionNotifyBillingResponse subscriptionsubscriptionNotifyBillingResponse = (SubscriptionNotifyBillingResponse) responseObjects
						.get(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION);
				if (subscriptionsubscriptionNotifyBillingResponse != null) {
					if (subscriptionsubscriptionNotifyBillingResponse.getValidToken() == 1)
					{
						String accountType = Utils
								.getAccountName(getApplicationContext());
						mDataManager.getCurrentSubscriptionPlan(this, accountType, contentId);
					} else {
						Toast.makeText(HungamaPayActivity.this,
								subscriptionsubscriptionNotifyBillingResponse.getStatus(), Toast.LENGTH_SHORT).show();
							setResult(RESULT_CANCELED);
							finish();
						isTransacted = false;
					}
				}
			} else if(operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK) {
				SubscriptionStatusResponse subscriptionStatusResponse = (SubscriptionStatusResponse) responseObjects
						.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);

				if (subscriptionStatusResponse != null/* && mApplicationConfigurations.isRealUser()*/) {
                    String aff_code=subscriptionStatusResponse.getAffCode();
					mApplicationConfigurations.setSubscriptionAffCode(aff_code);
					SubscriptionPlan subscriptionPlan = subscriptionStatusResponse.getSubscription();
					if(getIntent().getBooleanExtra("is_download", false))
					{

//						if(!TextUtils.isEmpty(paramsBilling) && paramsBilling.equalsIgnoreCase("subscription")) {
							if (subscriptionStatusResponse.getSubscription()!=null &&
									subscriptionStatusResponse.getSubscription().getSubscriptionStatus()==1)
							{
								mApplicationConfigurations
										.setIsUserHasSubscriptionPlan(true);

								Intent new_intent = new Intent();
								new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
								sendBroadcast(new_intent);
							}
//						}

						if(subscriptionStatusResponse.getDownload().getBalanceCredit()>0)
						{
							int plan_id=subscriptionStatusResponse.getDownload().getPlanId();
							String accountemail = Utils
									.getAccountName(getApplicationContext());
							mDataManager.getDownload(plan_id,contentId,null,"",DownloadOperationType.CONTENT_DELIVERY,this,null,accountemail,aff_code, "" + albumId);
							if (getIntent().getBooleanExtra("player", false))
							{
								RadioDetailsFragment.isbackFromUpgrade = true;
								PlayerBarFragment.isbackFromUpgrade = true;
							}
							if(isTransacted) {
								ApsalarEvent.postEvent(this, ApsalarEvent.TRANSACTED);
							}
						}else{
							openPlansPage();
						}
					} else if (!getIntent().getBooleanExtra("is_download", false) &&
							subscriptionStatusResponse.getSubscription()!=null &&
							subscriptionStatusResponse.getSubscription().getSubscriptionStatus()==1)
					{
						mApplicationConfigurations
								.setIsUserHasSubscriptionPlan(true);

						if(isTransacted) {
							if (mApplicationConfigurations
                                    .isUserHasSubscriptionPlan()
                                    && !mApplicationConfigurations
                                    .isUserHasTrialSubscriptionPlan()) {
								plandetail=subscriptionPlan.getPlanDetails();
								showUpgradeOnBoardDialog();
							}else if (mApplicationConfigurations
                                    .isUserHasTrialSubscriptionPlan()) {
								showFreeTrialUpgradeOnBoardDialog();
							}
							if (subscriptionStatusResponse.getSubscription().isTrial()) {
//								ApsalarEvent.postEvent(this, ApsalarEvent.FREE_TRIAL_TAKEN);
							} else {
								ApsalarEvent.postEvent(this, ApsalarEvent.TRANSACTED);
							}
							mDataManager.checkBadgesAlert("", "", SocialBadgeAlertOperation.ACTION_MUSIC_SUBSCRIPTION, this);
						} else
						{
							Intent new_intent = new Intent();
							new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
							sendBroadcast(new_intent);
							setResult(RESULT_OK);
							finish();
						}

						if (getIntent().getBooleanExtra("player", false)) {
							RadioDetailsFragment.isbackFromUpgrade = true;
							PlayerBarFragment.isbackFromUpgrade = true;
						}


					} else {
						try {
							openPlansPage();
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}
				}else if(!isTransacted){
                   try {
                        openPlansPage();
                    } catch (Exception e) {
                        Logger.printStackTrace(e);
                    }
                }else{
                    hideLoadingDialog();
                    finish();
                    return;
                }
				hideLoadingDialog();
				isTransacted = false;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.DOWNLOAD) {
			finish();
		}
		if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION) {
			Utils.makeText(this, getString(R.string.hungama_pay_error), Toast.LENGTH_SHORT).show();
			finish();
		}
		if(operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK) {
			Utils.makeText(this, getString(R.string.hungama_pay_error), Toast.LENGTH_SHORT).show();
			hideLoadingDialog();
			finish();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
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

	private HashMap<String, String> getHeader(){
		String md5 = Utils.toMD5(CommunicationManager.SECRET_KEY_PAY + mApplicationConfigurations.getPartnerUserId());
		HashMap<String, String> map = new HashMap<String, String>();
		map.put("API-KEY", md5);
		map.put("APP-VERSION", mApplicationConfigurations.getApplicationVersion());
		map.put("DEVICE-MODEL", Utils.getDeviceName());
		map.put("DEVICE-OS", "ANDROID");
		return map;
	}

	private void proceedToUpgrade() {
		if(CacheManager.isProUser(this)) {
			if(getIntent().getBooleanExtra("is_download", false)){
				String accountType = Utils
						.getAccountName(getApplicationContext());
				mDataManager.getCurrentSubscriptionPlan(this, accountType, contentId);
			} else {
				finish();
			}
		} else {
//		String sesion = mApplicationConfigurations.getSessionID();
//		boolean isRealUser = mApplicationConfigurations.isRealUser();
			// if(Utils.getSimOperator(UpgradeActivity.this).toLowerCase().contains("aircel")){
//		if (mApplicationConfigurations.isCheckForProUser()) {
//			if (!TextUtils.isEmpty(sesion) && isRealUser) {
//				openPlansPage();
//			} else {
//				startLoginActivity();
//				if (!getIntent().getBooleanExtra(UpgradeActivity.EXTRA_IS_GO_OFFLINE, false))
//					finish();
//			}
//		} else if (getIntent().getBooleanExtra(UpgradeActivity.EXTRA_IS_GO_OFFLINE, false)) {
//			if (!TextUtils.isEmpty(sesion) && isRealUser) {
//				openPlansPage();
			String accountType = Utils
					.getAccountName(getApplicationContext());
			mDataManager.getCurrentSubscriptionPlan(this, accountType, contentId);
//			} else
//				startLoginActivity();
//		} else {
//			openPlansPage();
//		}
		}
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

	private boolean clearHistory = false;

	private void openPlansPage(){
//		recentURL = "https://paystag.hungama.com/wvclose.php?auth=9be3afe40da75fe768fe2ed7b7fce99a&identity=17028774&pro" +
//				"duct=hungamamusic&platform=android&hardware_id=359299054269937&billing=subscription&aff_code=HUNG" +
//				"AMA-MUSIC-APP&plan_id=1&plan_details_id=1&dologin=1&webvclose=1";
		String extraParams = "";
		if(!TextUtils.isEmpty(recentURL))
		{
			Logger.s("URL ::::: " + recentURL);
//			int identityIndex = recentURL.indexOf("identity=");
//			String newUrl = recentURL.substring(0, identityIndex);
//			newUrl += "identity=" + mApplicationConfigurations.getPartnerUserId();
//			newUrl += recentURL.substring(recentURL.indexOf("&", identityIndex));
//			newUrl = newUrl.replace("dologin=1", "");
//			newUrl = newUrl.replace("webvclose=1","");
//			while (newUrl.lastIndexOf('&')==newUrl.length()-1)
//				newUrl = newUrl.substring(0, newUrl.length()-1);
//			Logger.s("NEW URL ::::: " + newUrl);
//			view.loadUrl(newUrl, getHeader());
			Uri uri = Uri.parse(recentURL);
			for(String key : uri.getQueryParameterNames()){
				if(key.equalsIgnoreCase("e") || key.equalsIgnoreCase("plan_id") || key.equalsIgnoreCase("plan_detail_id")){
					extraParams += "&" + key + "=" + uri.getQueryParameter(key);
				}
			}
		}

		clearHistory = true;
		initWebView();
		final String url;
		if (getIntent().getBooleanExtra("is_download", false)) {
			url = mDataManager.getHungamaPayDownlaodURL(contentId, albumId) + extraParams;
		} else {
			url = mDataManager.getHungamaPaySubscribeURL() + extraParams;
		}
		view.loadUrl(url, getHeader());
		Logger.e("WEbView", "getIntent().getData().toString() "
				+ url);
		view.getSettings().setJavaScriptCanOpenWindowsAutomatically(
				true);
		view.getSettings().setJavaScriptEnabled(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Logger.s("onActivityResult ::::::::::::::::::  " + requestCode + " :::: " + resultCode);
//		if (requestCode == VerifyMobileNumberDialog.VERIFY_MOBILE_LOGIN_ACTIVITY || requestCode == OtpConfirmationDialog.OTP_MOBILE_LOGIN_ACTIVITY) {
//			UpgradeFragment upgradeFragment = (UpgradeFragment) getSupportFragmentManager()
//					.findFragmentByTag(UpgradeFragment.TAG);
//			if (upgradeFragment != null) {
//				upgradeFragment.getActivityResult(requestCode, resultCode, data);
//			}
//			return;
//		}else if (requestCode == CONCENT_BILLING) {
//			UpgradeFragment upgradeFragment = (UpgradeFragment) getSupportFragmentManager()
//					.findFragmentByTag(UpgradeFragment.TAG);
//			upgradeFragment.getActivityResult(requestCode, resultCode, data);
//		} else

		if (requestCode == UpgradeActivity.LOGIN_ACTIVITY_CODE && resultCode == RESULT_OK) {
			// checks for valid session.
			String session = mDataManager.getApplicationConfigurations()
					.getSessionID();
			Boolean isRealUser = mDataManager.getApplicationConfigurations()
					.isRealUser();

			if (!TextUtils.isEmpty(session) && isRealUser)
			{
				String accountType = Utils
						.getAccountName(getApplicationContext());
				isTransacted=false;
				mDataManager.getCurrentSubscriptionPlan(this, accountType, contentId);
				// openPlansPage();
			} else {
				Toast toast = Utils.makeText(this,
						getString(R.string.before_upgrade_login),
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_VERTICAL
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
				// startLoginActivity();
			}
			// }
		} /*else if (requestCode == UpgradeFragment.LOGIN_ACTIVITY_CODE
				&& resultCode == RESULT_OK) {
			UpgradeFragment upgradeFragment = (UpgradeFragment) getSupportFragmentManager()
					.findFragmentByTag(UpgradeFragment.TAG);
			upgradeFragment.getActivityResult(requestCode, resultCode, data);
		} */ else if (requestCode == PURCHASE_REQUEST_CODE) {
			Logger.e("onActivityResult UpgradeActivity : onActivityResults", ""
					+ resultCode);
			try
			{
				if (!billingHelper.handleActivityResult(requestCode, resultCode, data)) {
					super.onActivityResult(requestCode, resultCode, data);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		} else if (requestCode == UpgradeActivity.LOGIN_ACTIVITY_CODE
				&& resultCode == RESULT_CANCELED) {
			finish();
		} else if (getIntent().getBooleanExtra(UpgradeActivity.EXTRA_IS_GO_OFFLINE, false)) {
			mApplicationConfigurations.setSaveOfflineMode(true);
			sendBroadcast(new Intent(
					MainActivity.ACTION_OFFLINE_MODE_CHANGED));
			Map<String, String> reportMap = new HashMap<String, String>();
			if (getIntent().getBooleanExtra(
					UpgradeActivity.EXTRA_IS_FROM_NO_INTERNET_PROMT, false))
				reportMap.put(FlurryConstants.FlurryCaching.Source
								.toString(),
						FlurryConstants.FlurryCaching.NoInternetPrompt
								.toString());
			else
				reportMap.put(FlurryConstants.FlurryCaching.Source
								.toString(),
						FlurryConstants.FlurryCaching.LeftMenuToggleButton
								.toString());
			reportMap.put(
					FlurryConstants.FlurryCaching.UserStatus.toString(),
					Utils.getUserState(this));
			Analytics.logEvent(
					FlurryConstants.FlurryCaching.GoOffline.toString(),
					reportMap);
		}
	}

	private MyProgressDialog mProgressDialog;

	private void showLoadingDialog() {
		if (!isFinishing()) {
			if (mProgressDialog == null) {
				try {
					mProgressDialog = new MyProgressDialog(this);
				} catch (Error e) {
					System.gc();
					System.runFinalization();
					System.gc();
					mProgressDialog = new MyProgressDialog(this);
				}
				mProgressDialog.setCancelable(true);
				mProgressDialog.setCanceledOnTouchOutside(false);
			}
		}
	}

	private void hideLoadingDialog() {
		try {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	protected void purchaseItem(String sku, String type) {
		try {
			if (isBindToGoogle)
			{
				if(type!=null && type.equalsIgnoreCase("inapp")) {
					billingHelper.launchPurchaseFlow(this, sku,
							PURCHASE_REQUEST_CODE, this);
				} else {
					billingHelper.launchSubscriptionPurchaseFlow(this, sku,
							PURCHASE_REQUEST_CODE, this);
				}
//				mDataManager.getSubscriptionChargeNew(
//						selectedPlan, "ITEM_TYPE_SUBS",
//						SubscriptionType.CHARGE, this,
//						Utils.TEXT_EMPTY, "token_subscription_test", Utils.getAccountName(this),
//						false,// isTrialFree,
//						transactionSessi`on, "", "");
			} else {
				showDialog(
						getString(R.string.google_wallet),
						getString(R.string.for_using_google_wallet_please_accept_terms_in_google_play));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Utils.makeText(this, getString(R.string.please_try_again), 0)
					.show();
		}
	}

	private void showDialog(String title, String text) {
		CustomAlertDialog alertDialogBuilder = new CustomAlertDialog(this);

		// set title
		alertDialogBuilder
				.setTitle(Utils.getMultilanguageText(this, title));

		// set dialog message
		alertDialogBuilder
				.setMessage(Utils.getMultilanguageText(this, text))
				.setCancelable(true)
				.setNegativeButton(
						Utils.getMultilanguageText(this, getResources()
								.getString(R.string.exit_dialog_text_ok)),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});

		// show it
		alertDialogBuilder.show();
	}

	private void setupInAppBilling() {
		try {
			if(billingHelper==null) {
				billingHelper = new IabHelper(TAG, this, getResources()
						.getString(R.string.base_64_key));
				billingHelper.startSetup(this);
			} else{
//				purchaseItem(selectedPlan);
//				Logger.i(TAG, "In-app Billing set up" + result);
				List<String> moreSkus = new ArrayList<String>();
				moreSkus.add(selectedPlan);
				try {
					billingHelper
							.queryInventoryAsync(handler, true, moreSkus, this);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onIabSetupFinished(IabResult result) {
		if (result.isSuccess()) {
			Logger.i(TAG, "In-app Billing set up" + result);
			List<String> moreSkus = new ArrayList<String>();
			moreSkus.add(selectedPlan);
			try {
				billingHelper
						.queryInventoryAsync(handler, true, moreSkus, this);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			isBindToGoogle=true;
		} else {
			Logger.i(TAG, "Problem setting up In-app Billing: " + result);
			isBindToGoogle = false;
//			if (plansAdapter != null) {
//				plansAdapter.notifyDataSetChanged();
//			}
		}
	}

	@Override
	public void onIabFailedBindToService(boolean isBind) {
		isBindToGoogle = isBind;

//		if (plansAdapter != null) {
//			plansAdapter.notifyDataSetChanged();
//		}
	}

	@Override
	public void onIabPurchaseFinished(IabResult result, Purchase info) {
		try {
			Logger.e(TAG + " : onIabPurchaseFinished", result + " " + info);
			if (result.isFailure()) {
				// if (result.getResponse() == 7) {
				// mDataManager.getSubscriptionCharge(clickedPLan.getPlanId(),
				// clickedPLan.getType(), SubscriptionType.CHARGE,
				// UpgradeFragment.this);
				// }
				Logger.i(TAG, "Failed to Purchase Item");
				// Flurry report: payment
				Analytics.logEvent(
						FlurryConstants.FlurrySubscription.PaymentFail
								.toString(), true);
			} /*else if (clickedPLan != null && clickedPLan.getSkudetails()!=null
					&& clickedPLan.getSkudetails().getSku()
					.equals(info.getSku())) {
				// String code =
				// mApplicationConfigurations.getSubscriptionIABcode();
				// // not needed anymore
				mApplicationConfigurations.setSubscriptionIABpurchseToken(info
						.getToken());
				String accountType = Utils.getAccountName(mContext);
				if (accountType != null) {
					mDataManager.getSubscriptionCharge(clickedPLan.getPlanId(),
							clickedPLan.getType(), SubscriptionType.CHARGE,
							UpgradeFragment.this, Utils.TEXT_EMPTY,
							info.getToken(), accountType, false,
							transactionSession);
					// Flurry report: payment
					Analytics
							.logEvent(
									FlurryConstants.FlurrySubscription.PaymentSuccessful
											.toString(), true);
				}
			} */else {

				Logger.e(TAG, "onIabPurchaseFinished>>>>>>>>");
				if (info.getToken() != null) {
					mApplicationConfigurations
							.setSubscriptionIABpurchseToken(info.getToken());
					String accountType = Utils.getAccountName(this);
					if (accountType != null && selectedPlan != null)
					{
						isTransacted = true;
						sharedPurchase=info;
						mDataManager.getSubscriptionChargeNew(
								selectedPlan, info.getItemType(),
								SubscriptionType.CHARGE, this,
								Utils.TEXT_EMPTY, info.getToken(), accountType,
								false,// isTrialFree,
								transactionSession, mApplicationConfigurations.getSubscriptionAffCode(), "", "" + contentId);
						// Flurry report: payment
						Analytics
								.logEvent(
										FlurryConstants.FlurrySubscription.PaymentSuccessful
												.toString(), true);
					}
//					Plan googleplan = null, p = null;
//					for (int i = 0; i < plans.size(); i++) {
//						p = plans.get(i);
//						if (p.getPlanType() == PlanType.GOOGLE) {
//							googleplan = p;
//							i = plans.size();
//						}
//					}

//					if (accountType != null && googleplan != null) {
//						mDataManager.getSubscriptionCharge(
//								googleplan.getPlanId(), googleplan.getType(),
//								SubscriptionType.CHARGE, UpgradeFragment.this,
//								Utils.TEXT_EMPTY, info.getToken(), accountType,
//								false,// isTrialFree,
//								transactionSession);
//						// Flurry report: payment
//						Analytics
//								.logEvent(
//										FlurryConstants.FlurrySubscription.PaymentSuccessful
//												.toString(), true);
//					}
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onQueryInventoryFinished(IabResult result, Inventory inv) {
		if (result.isSuccess())
		{
			if (inv != null && inv.mPurchaseMap != null)
			{
				Purchase purchase = inv.mPurchaseMap.get(selectedPlan);

				Logger.d(TAG, "purchase >>" + purchase);
				if (purchase != null) {
					onIabPurchaseFinished(result, purchase);
					return;
				}
			}
//			for (Plan plan : plans) {
//				if (plan.getPlanType() == PlanType.GOOGLE) {
//					plan.setSkudetails(inv.getSkuDetails(mResources
//							.getString(R.string.hungama_premium_subscription)));
//				}
//			}
//
//			if (plansAdapter != null) {
//				plansAdapter.notifyDataSetChanged();
//			}

		} else {
			Logger.i(TAG, "Failed Querying Inventory");
		}

		try {
			String type = "subs";
			if (inv != null && inv.getSkuDetails(selectedPlan)!=null)
			{
				type = inv.getSkuDetails(selectedPlan).getType();
				Logger.s("Purchase type ::::: " + type);
			}
//			Plan tempPlan = (Plan) getIntent()
//					.getSerializableExtra("plan_clicked");
//			if (tempPlan != null
//					&& tempPlan.getPlanId() == mApplicationConfigurations
//					.getTempClickedPlan().getPlanId()) {
////				getActivity().getIntent().removeExtra("plan_clicked");
//				clickedPLan = tempPlan;
//				mApplicationConfigurations.setTempClickedPlan(clickedPLan);
////				planClickedEvent(clickedPLan);
//			}
			purchaseItem(selectedPlan, type);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void showDownloadDialog(String header, String body,
								   boolean isLeftButtonVisible, boolean isRightButtonVisible) {
		// set up custom dialog
		final Dialog downloadDialog = new Dialog(this);
		downloadDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		downloadDialog.getWindow().setBackgroundDrawable(
				new ColorDrawable(Color.TRANSPARENT));
		// LinearLayout root = (LinearLayout) LayoutInflater.from(
		// DownloadActivity.this).inflate(
		// R.layout.dialog_download_same_song, null);
		downloadDialog.setContentView(R.layout.custom_dialog_eng);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(downloadDialog.getWindow().getDecorView(),
					HungamaPayActivity.this);
		}

		LanguageTextView title = (LanguageTextView) downloadDialog
				.findViewById(R.id.download_custom_dialog_title_text);
		title.setText(header);

		LanguageTextView text = (LanguageTextView) downloadDialog
				.findViewById(R.id.text_custom_alert_message);
		text.setText(body);

		LanguageButton goToMyCollectionButton = (LanguageButton) downloadDialog
				.findViewById(R.id.button_custom_alert_positive);
		LanguageButton downloadAgainButton = (LanguageButton) downloadDialog
				.findViewById(R.id.button_custom_alert_negative);

		goToMyCollectionButton.setText(getResources().getString(R.string.ok));
		downloadAgainButton.setText(getResources().getString(R.string.cancel));

		downloadAgainButton.setVisibility(View.GONE);

		goToMyCollectionButton.setVisibility(View.VISIBLE);
		goToMyCollectionButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				downloadDialog.dismiss();
				setResult(RESULT_OK);
				finish();
			}
		});

		downloadDialog.setCancelable(true);
		downloadDialog
				.setOnCancelListener(new DialogInterface.OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						downloadDialog.dismiss();
						setResult(RESULT_OK);
						finish();
					}
				});
		try {
			DisplayMetrics displaymetrics = new DisplayMetrics();
			downloadDialog.getWindow().getWindowManager().getDefaultDisplay()
					.getMetrics(displaymetrics);
			int width = (int) (displaymetrics.widthPixels);

			WindowManager.LayoutParams params = downloadDialog.getWindow()
					.getAttributes();
			params.width = width;
			downloadDialog.getWindow().setAttributes(params);

			downloadDialog.show();
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":365", e.toString());
		}

	}

    private void showUpgradeOnBoardDialog() {
        try {
            // set up custom dialog
            upgradeDialog = new Dialog(this);
            upgradeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            upgradeDialog.getWindow().setBackgroundDrawable(
					new ColorDrawable(Color.TRANSPARENT));
            upgradeDialog.setContentView(R.layout.dialog_upgrade_on_board);


            final Runnable getstarted = new Runnable() {

                @Override
                public void run() {
                    upgradeDialog.dismiss();
                    if(HungamaPayActivity.this!=null) {
                        Intent new_intent = new Intent();
                        new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
                        sendBroadcast(new_intent);
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            };

			((LanguageTextView) upgradeDialog.findViewById(R.id.text_benifits_on_board)).setText(Html.fromHtml(plandetail, null,new MyHtmlTagHandler()));

            ImageButton closeButton = (ImageButton) upgradeDialog
                    .findViewById(R.id.close_button);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getstarted.run();
                }
            });

            upgradeDialog.findViewById(R.id.button_getStared).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getstarted.run();
                        }
                    });

            upgradeDialog.setCancelable(true);
            upgradeDialog
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            getstarted.run();
                        }
                    });
            upgradeDialog.show();
        } catch (Exception e){
            Logger.printStackTrace(e);
            if(HungamaPayActivity.this!=null) {
                Intent new_intent = new Intent();
                new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
                sendBroadcast(new_intent);
                setResult(RESULT_OK);
                finish();
            }
        }
    }


    private void showFreeTrialUpgradeOnBoardDialog() {
        // set up custom dialog
        try {
            upgradeDialog = new Dialog(this);
//			upgradeDialog.setTitle(R.string.free_trial_popup_title);
            upgradeDialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(Color.TRANSPARENT));
            upgradeDialog
                    .setContentView(R.layout.dialog_free_trial_upgrade_on_board);

            final Runnable getstarted = new Runnable() {

                @Override
                public void run() {
                    upgradeDialog.dismiss();
                    if(HungamaPayActivity.this!=null) {
                        Intent new_intent = new Intent();
                        new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
                        sendBroadcast(new_intent);
                        setResult(RESULT_OK);
                        finish();
                    }
                }
            };
            Button buttonOk = (Button) upgradeDialog.findViewById(R.id.button_ok);
            buttonOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getstarted.run();
                }
            });

            upgradeDialog.setCancelable(true);
            upgradeDialog
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            getstarted.run();
                        }
                    });

            DisplayMetrics displaymetrics = new DisplayMetrics();
            upgradeDialog.getWindow().getWindowManager().getDefaultDisplay()
                    .getMetrics(displaymetrics);
            int width = (int) (displaymetrics.widthPixels);

            WindowManager.LayoutParams params = upgradeDialog.getWindow()
                    .getAttributes();
            params.width = width;
            upgradeDialog.getWindow().setAttributes(params);

            upgradeDialog.show();
//			if(getActivity()!=null) {
//				getActivity().getWindow().getDecorView().setVisibility(View.INVISIBLE);
//			}
        } catch (Exception e){
            if(HungamaPayActivity.this!=null) {
                Intent new_intent = new Intent();
                new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
                sendBroadcast(new_intent);
                setResult(RESULT_OK);
                finish();
            }
        }
    }
	public class MyHtmlTagHandler implements Html.TagHandler {
		boolean first= true;
		String parent=null;
		int index=1;
		@Override
		public void handleTag(boolean opening, String tag, Editable output,
							  XMLReader xmlReader) {
			if(tag.equals("ul")) parent="ul";
			else if(tag.equals("ol")) parent="ol";
			if(tag.equals("li")){
				if(parent.equals("ul")){
					if(first){
						if(output.length()==0)
							output.append("\t• ");
						else
							output.append("\n\t• ");
						first= false;
					}else{
						first = true;
					}
				}
				else{
					if(first){
						if(output.length()==0)
							output.append("\t"+index+". ");
						else
							output.append("\n\t"+index+". ");
						first= false;
						index++;
					}else{
						first = true;
					}
				}
			}
		}
	}
}
