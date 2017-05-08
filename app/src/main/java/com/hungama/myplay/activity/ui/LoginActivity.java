package com.hungama.myplay.activity.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupFieldType;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.data.dao.hungama.UserProfileResponse;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment.OnTwitterLoginListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.PartnerInfoReadOperation;
import com.hungama.myplay.activity.operations.hungama.ForgotPasswordOperation;
import com.hungama.myplay.activity.operations.hungama.GetUserProfileOperation;
import com.hungama.myplay.activity.services.CampaignsPreferchingService;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.fragments.AccountSettingsFragment.ObjLanguagePackage;
import com.hungama.myplay.activity.ui.fragments.LoginForgotPasswordFragment;
import com.hungama.myplay.activity.ui.fragments.LoginForgotPasswordFragment.OnForgotPasswordSubmitListener;
import com.hungama.myplay.activity.ui.fragments.LoginFragment;
import com.hungama.myplay.activity.ui.fragments.LoginFragment.OnLoginOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.LanguageEditText;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import com.hungama.myplay.activity.ui.fragments.LoginSignupFragment;
//import com.hungama.myplay.activity.ui.fragments.LoginSignupFragment.OnSignupOptionSelectedListener;
//import com.hungama.myplay.activity.ui.fragments.LoginWithSocialNetworkFragment.OnSocialNetworkSubmitCredentialsListener;

/**
 * Activity for performing first application's Login or Sign up </br> and
 * retrieves result for the {@link OnApplicationStartsActivity} if it successes
 * performing login. </br> Manages the following: </br> 1. delegating Views
 * handling to fragments. </br> 2. manages the business logic of parsing fields
 * before submitting them. </br>
 */
public class LoginActivity extends FragmentActivity implements
		CommunicationOperationListener, OnLoginOptionSelectedListener,
		OnForgotPasswordSubmitListener, //OnSignupOptionSelectedListener,
		OnGigyaResponseListener, // OnSocialNetworkSubmitCredentialsListener,
		OnTwitterLoginListener {

	private static final String TAG = "LoginActivity";

	private static final int TUTORIAL_ACTIVITY_CODE = 1;

	public static final String FLURRY_SOURCE = "flurry_source";
	public static final String IS_FROM_OTP = "is_from_otp";
	public static final String OTP_MOBILE_NO = "otp_mobile_no";

	private FragmentManager mFragmentManager;

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private DeviceConfigurations mDeviceConfigurations;

	private List<SignOption> mSignOptions;

	// Gigya integration.
	private TwitterLoginFragment mTwitterLoginFragment;

	private Bundle fromActivity;

	private GigyaManager mGigyaManager;

	private boolean isFirstVisitToApp;
	private boolean isFirstVisitToAppFromAppTour = true;
	private volatile boolean finishedLoadingAllData = false;

	private boolean mIsActivityResumed = false;

	private volatile boolean mIsAnyOperationRunning = false;

	private volatile boolean mIsDestroyed = false;

	private boolean mIsGigyaLoginProcess = false;

	private String mFlurrySource;

	// ======================================================
	// Activity lifecycle.
	// ======================================================

	MyProgressDialog otpProgrss;
	private SocialNetwork provider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// if(getIntent().getBooleanExtra(LoginActivity.IS_FROM_OTP, false))
		// setTheme(android.R.style.Theme_Translucent_NoTitleBar);
		// else
		// setTheme(R.style.AppThemeHome);
		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		isFirstVisitToApp = mApplicationConfigurations.isFirstVisitToApp();
		if (!isFirstVisitToApp && !getIntent().getBooleanExtra(LoginActivity.IS_FROM_OTP, false)) {
			setTheme(R.style.AppThemelogin);
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// if (Logger.enableLanguageLibraryThread) {
		// Utils.startReverieSDK(getApplicationContext());
		// } else
//		if (Logger.enableLanguageLibrary) {
//			new LM(this).RegisterSDK(HomeActivity.SDK_ID);
//		}
		Logger.s("debugLogin onCreate");
		// initializes managers:
		mFragmentManager = getSupportFragmentManager();
		mIsAnyOperationRunning = false;
//		mDataManager = DataManager.getInstance(getApplicationContext());

//		mApplicationConfigurations = mDataManager
//				.getApplicationConfigurations();
		mDeviceConfigurations = mDataManager.getDeviceConfigurations();

		// initializes the Gigya connection service.
		// GSAPI GSAPI = new GSAPI(getResources()
		// .getString(R.string.gigya_api_key), this);
//		GSAPI.getInstance().initialize(this,
//				getResources().getString(R.string.gigya_api_key));
//		GSAPI.getInstance()
//				.setLoginBehavior(GSAPI.LoginBehavior.WEBVIEW_DIALOG);

//		isFirstVisitToApp = mApplicationConfigurations.isFirstVisitToApp();
		// if (isFirstVisitToApp) {
		// mApplicationConfigurations.setIsFirstVisitToApp(false);
		//
		// // Cancel the application tour loading for the first time - I
		// // startTutorialActivity();
		// }

		// disables any action bar.
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (!isFirstVisitToApp) {
			if (!getIntent().getBooleanExtra(LoginActivity.IS_FROM_OTP, false)) {
				setContentView(R.layout.activity_login);
				findViewById(R.id.ivDownArrow).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								finish();
							}
						});
			} else {
				setContentView(R.layout.activity_start_up);
				// findViewById(R.id.progress_login).setVisibility(View.VISIBLE);
				otpProgrss = new MyProgressDialog(this);
				// getWindow().setBackgroundDrawable(new
				// ColorDrawable(Color.TRANSPARENT));
			}
			String deviceId = mApplicationConfigurations.getDeviceID();
//			if (!Logger.enableConsumerDeviceLogin && TextUtils.isEmpty(deviceId)) {
//				mDataManager.createDevice(this);
//			} else {
				mDataManager.readPartnerInfo(this);
//			}
		} else {
			setContentView(R.layout.activity_start_up);
			findViewById(R.id.progress).setVisibility(View.VISIBLE);
			// setContentView(R.layout.application_splash_layout);
			mDataManager.getTimeRead(this);
		}

		// get the extra to know which activity invoked this activity
		fromActivity = getIntent().getExtras();

		// Application tour was removed - II
		// if (isFirstVisitToApp) {
		// setResult(RESULT_OK);
		// finish();
		// }

		if (fromActivity != null && fromActivity.containsKey(FLURRY_SOURCE)) {
			mFlurrySource = fromActivity.getString(FLURRY_SOURCE);
		} else {
			mFlurrySource = FlurryConstants.FlurryUserStatus.AppLaunch
					.toString();
		}
		if (getIntent().getBooleanExtra(AlertActivity.ALERT_MARK, false)) {
			onSkipSelected();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// if (requestCode == TUTORIAL_ACTIVITY_CODE && resultCode == RESULT_OK
		// && finishedLoadingAllData) {
		if (resultCode == RESULT_OK && finishedLoadingAllData) {
			setResult(RESULT_OK);
			finish();
		} else {
			showLoadingDialogFragment();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		Analytics.startSession(this);
		Analytics.onPageView();

		// xtpl
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryUserStatus.Source.toString(),
				mFlurrySource);
		Analytics.logEvent(
				FlurryConstants.FlurryUserStatus.SeesLoginScreen.toString(),
				reportMap);
		// xtpl
	}

	@Override
	protected void onResume() {
		super.onResume();
		mIsActivityResumed = true;
		if(isFirstVisitToApp)
			ScreenLockStatus.getInstance(getBaseContext()).dontShowAd();
		else
			ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
		// Hide the dialog when getting back to visible
		if (mIsAnyOperationRunning) {
			showLoadingDialogFragment();
		} else {
			hideLoadingDialogFragment();
		}

		if (mIsGigyaLoginProcess) {
			// Show progress bar after Gigya login and until Login to Hungama
			// will finish
			mIsGigyaLoginProcess = false;
			// showLoadingDialogFragment();
		}
	}

	@Override
	protected void onUserLeaveHint() {
		if(!isFirstVisitToApp)
			ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onPause() {
		mIsActivityResumed = false;

		hideLoadingDialogFragment();

		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();
		Analytics.onEndSession(this);
	}

	@Override
	protected void onDestroy() {
		mIsDestroyed = true;
//		System.gc();
		// try {
		// int version = Integer.parseInt(""
		// + android.os.Build.VERSION.SDK_INT);
		// unbindDrawables(getWindow().getDecorView().getRootView(), version);
		// } catch (Exception e) {
		// Logger.printStackTrace(e);
		// }
		try {
			super.onDestroy();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// ======================================================
	// Communication operations callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {

		mIsAnyOperationRunning = true;

		switch (operationId) {

		case (OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE):
			Logger.i(TAG, "Starting to get device ID.");
			// if (!isFirstVisitToApp) {
			showLoadingDialogFragment();
			// }
			break;

		case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
			Logger.i(TAG, "Starting get partner info - sign up / in options.");
			if (mIsActivityResumed || isFirstVisitToApp) {
				showLoadingDialogFragment();
			}
			break;

		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			Logger.i(TAG, "Starting getting activation code.");
			if (!isFirstVisitToApp || mIsActivityResumed) {
				showLoadingDialogFragment();
			}
			break;

        case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):
            Logger.i(TAG, "Starting getting activation code.");
            if (!isFirstVisitToApp || mIsActivityResumed) {
                showLoadingDialogFragment();
            }
            break;

		case (OperationDefinition.Hungama.OperationId.FORGOT_PASSWORD):
			Logger.i(TAG, "Sending Hungama user's email for password.");
			showLoadingDialogFragment();
			break;

		case (OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ):
			showLoadingDialogFragment();
			break;

		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			mIsAnyOperationRunning = false;

			switch (operationId) {

			case (OperationDefinition.CatchMedia.OperationId.TIME_READ):
				String deviceId = mApplicationConfigurations.getDeviceID();
//				if (!Logger.enableConsumerDeviceLogin && TextUtils.isEmpty(deviceId)) {
//					mDataManager.createDevice(this);
//				} else {
					mDataManager.readPartnerInfo(this);
//				}
				break;
			case (OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE):
				Logger.i(TAG, "Successed getting device ID.");
				// get partener's info - sign up options.
				mDataManager.readPartnerInfo(this);
				break;

			case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
				Logger.i(TAG, "Successed getting partner info.");
				mSignOptions = (List<SignOption>) responseObjects
						.get(PartnerInfoReadOperation.RESPONSE_KEY_OBJECT_SIGN_OPTIONS);
				// Set the Gigya setID
				SignOption gigyaSignup = mSignOptions.get(2);
				// SignOption gigyaSignup = Utils.getSignOption(mSignOptions,
				// SignOption.SET_ID_GIGYA_LOGIN);
				mApplicationConfigurations.setGigyaSignup(gigyaSignup);

				mGigyaManager = new GigyaManager(this);
				mGigyaManager.setOnGigyaResponseListener(this);

				if (isFirstVisitToApp && isFirstVisitToAppFromAppTour) {
					isFirstVisitToAppFromAppTour = false;
					onSkipSelected();
				} else if (getIntent().getBooleanExtra(
						LoginActivity.IS_FROM_OTP, false)) {
					doOtpLogin();
				} else {
					showLoginFields();
				}

				if (!isFirstVisitToApp)
					hideLoadingDialogFragment();

				break;

			case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
				// let's party!
				// get user language & set hashmap
				String activationCode = (String) responseObjects
						.get(ApplicationConfigurations.ACTIVATION_CODE);
				mDataManager.createDeviceActivationLogin(activationCode, this);
				// if(mTwitterLoginFragment != null){
				// mTwitterLoginFragment.finish();
				// }

				if(isFromLogin) {
//					ApsalarEvent.postEvent(this, ApsalarEvent.LOGIN_COMPLETED);
					if(mApplicationConfigurations.isRealUser())
						ApsalarEvent.postLoginEvent(provider);
				} else if(isRegistering)
					ApsalarEvent.postEvent(this, ApsalarEvent.SIGNUP_COMPLITION);

				if (isRegistering) {
					mApplicationConfigurations.setIsUserRegistered(true);
					isRegistering = false;
					mDataManager.getUserProfileDetail(this);

					Set<String> tags = Utils.getTags();
					if (!tags.contains("registered_user")) {
						if (tags.contains("non_registered_user"))
							tags.remove("non_registered_user");

						tags.add("registered_user");
						Utils.AddTag(tags);
					}
				}
				break;

            case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):
                // let's party!
                // get user language & set hashmap
//                String activationCode = (String) responseObjects
//                        .get(ApplicationConfigurations.ACTIVATION_CODE);
//                mDataManager.createDeviceActivationLogin(activationCode, this);
                // if(mTwitterLoginFragment != null){
                // mTwitterLoginFragment.finish();
                // }
				if(isFromLogin) {
//					ApsalarEvent.postEvent(this, ApsalarEvent.LOGIN_COMPLETED);
					if(mApplicationConfigurations.isRealUser())
						ApsalarEvent.postLoginEvent(provider);
				} else if(isRegistering) {
					ApsalarEvent.postEvent(this, ApsalarEvent.SIGNUP_COMPLITION, ApsalarEvent.TYPE_LOGIN_HUNGAMA);
				}
                if (isRegistering) {
                    mApplicationConfigurations.setIsUserRegistered(true);
                    isRegistering = false;
                    mDataManager.getUserProfileDetail(this);

                    Set<String> tags1 = Utils.getTags();
                    if (!tags1.contains("registered_user")) {
                        if (tags1.contains("non_registered_user"))
                            tags1.remove("non_registered_user");

                        tags1.add("registered_user");
                        Utils.AddTag(tags1);
                    }
                }
                Set<String> tags = Utils.getTags();
                if (mApplicationConfigurations.isRealUser()) {
                    if (!tags.contains("logged-in")) {
                        if (tags.contains("not-logged-in"))
                            tags.remove("not-logged-in");

                        tags.add("logged-in");
                        Utils.AddTag(tags);
                    }
                    try {
                        mDataManager.getCurrentSubscriptionPlan(this,
                                Utils.getAccountName(this));
                    } catch (Exception e) {
                        Logger.printStackTrace(e);
                    }
                } else {
                    if (!tags.contains("not-logged-in")) {
                        if (tags.contains("logged-in"))
                            tags.remove("logged-in");
                        tags.add("not-logged-in");
                        Utils.AddTag(tags);
                    }
					try {
						mDataManager.getCurrentSubscriptionPlan(this,
								Utils.getAccountName(this));
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
                }

                String secret = mApplicationConfigurations
                        .getGigyaSessionSecret();
                String token = mApplicationConfigurations
                        .getGigyaSessionToken();

                if (!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)) {
                    // GigyaManager mGigyaManager = new GigyaManager(this);
                    if (mGigyaManager == null)
                        mGigyaManager = new GigyaManager(this);
                    mGigyaManager.setSession(token, secret);
                }
                if (mTwitterLoginFragment != null) {
                    mTwitterLoginFragment.finish();
                }
//				mDataManager.getLeftMenu(this, this, null);
				mDataManager.GetUserLanguageMap(this);
                if (mApplicationConfigurations.isRealUser() && chkForGigyaLogin) {
                    chkForGigyaLogin = false;
                    if (mGigyaManager == null)
                        mGigyaManager = new GigyaManager(this);
                    mGigyaManager.setOnGigyaResponseListener(this);
                    mGigyaManager.socializeGetUserInfo();
                }
                if(isFirstVisitToApp){
                    mApplicationConfigurations.setSilentUserLoginPhoneNumber(mApplicationConfigurations.getUserLoginPhoneNumber());
                    mApplicationConfigurations.setSilentPartnerUserId(mApplicationConfigurations.getPartnerUserId());
                    mApplicationConfigurations
                            .setSilentUserGigyaSessionToken(mApplicationConfigurations
                                    .getGigyaSessionToken());
                    mApplicationConfigurations
                            .setSilentUserGigyaSessionSecret(mApplicationConfigurations
                                    .getGigyaSessionSecret());
                    mApplicationConfigurations.setSilentUserSessionID(mApplicationConfigurations.getSessionID());
                    mApplicationConfigurations.setSilentUserHouseholdID(mApplicationConfigurations.getHouseholdID());
                    mApplicationConfigurations.setSilentUserConsumerID(mApplicationConfigurations.getConsumerID());
                    mApplicationConfigurations.setSilentUserPasskey(mApplicationConfigurations.getPasskey());
                }
                break;

			case OperationDefinition.Hungama.OperationId.GET_USER_PROFILE:
				UserProfileResponse userProfileResponse = (UserProfileResponse) responseObjects
						.get(GetUserProfileOperation.RESPONSE_KEY_USER_DETAIL);
				if (userProfileResponse != null
						&& userProfileResponse.getCode() == 200) {
					mApplicationConfigurations
							.setHungamaEmail(userProfileResponse.getUsername());
					mApplicationConfigurations
							.setHungamaFirstName(userProfileResponse
									.getFirst_name());
					mApplicationConfigurations
							.setHungamaLastName(userProfileResponse
									.getLast_name());
				}

				if (!mApplicationConfigurations.getGigyaFBFirstName().equals("")
						|| !mApplicationConfigurations.getGigyaFBLastName().equals(
						"")) {
						Analytics.loginEvent("Facebook",true);
				} else if (!mApplicationConfigurations.getGigyaGoogleFirstName()
						.equals("")
						|| !mApplicationConfigurations.getGigyaGoogleLastName()
						.equals("")) {
					Analytics.loginEvent("G+",true);
				} else if (!mApplicationConfigurations.getGigyaTwitterFirstName()
						.equals("")
						|| !mApplicationConfigurations.getGigyaTwitterLastName()
						.equals("")) {
					Analytics.loginEvent("Twitter",true);
				} else {
					Analytics.loginEvent("Hungama Login",true);
				}
				break;
			case (OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET):
				final String tagToRemove;
				final int oldLanguageSelected = mApplicationConfigurations
						.getUserSelectedLanguage();
//				switch (oldLanguageSelected) {
//				case Constants.LANGUAGE_HINDI:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_hindi);
//					break;
//				case Constants.LANGUAGE_TAMIL:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_tamil);
//					break;
//				case Constants.LANGUAGE_TELUGU:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_telugu);
//					break;
//				case Constants.LANGUAGE_PUNJABI:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_punjabi);
//					break;
//				case Constants.LANGUAGE_ENGLISH:
//				default:
//					tagToRemove = "pref_display_"
//							+ getResources().getString(R.string.lang_english);
//					break;
//				}
				if(!TextUtils.isEmpty(mApplicationConfigurations.getUserSelectedLanguageText())){
					tagToRemove = "pref_display_"
							+ mApplicationConfigurations.getUserSelectedLanguageText();
				} else{
					tagToRemove = "pref_display_"
							+ getResources().getString(
							R.string.lang_english);
				}
				try {
					ObjLanguagePackage lp = new ObjLanguagePackage(
							responseObjects.toString());
					if (!isFirstVisitToApp
							&& !lp.getLanguage().equals(mApplicationConfigurations.getUserSelectedLanguageText())) {
						Utils.makeText(
								this,
								getString(R.string.your_langugae_has_been_changed),
								Toast.LENGTH_LONG).show();
					}
					mApplicationConfigurations.setUserSelectedLanguageText(lp
							.getLanguage());
					mApplicationConfigurations
							.setUserSelectedLanguage(lp.getId());
					Utils.changeLanguage(this.getBaseContext(), lp.getLanguage());
//					if (lp.getLanguage().equals("Hindi")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_HINDI);
//						Utils.changeLanguage(this.getBaseContext(), "Hindi");
//					} else if (lp.getLanguage().equals("Tamil")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_TAMIL);
//						Utils.changeLanguage(this.getBaseContext(), "Tamil");
//					} else if (lp.getLanguage().equals("Telugu")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_TELUGU);
//						Utils.changeLanguage(this.getBaseContext(), "Telugu");
//					} else if (lp.getLanguage().equals("Punjabi")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_PUNJABI);
//						Utils.changeLanguage(this.getBaseContext(), "Punjabi");
//					} else {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_ENGLISH);
//						Utils.changeLanguage(this.getBaseContext(), "English");
//						mApplicationConfigurations
//								.setUserSelectedLanguageText("English");
//					}
					try {
						String tagToAdd = "pref_display_" + lp.getLanguage();
						Set<String> tags1 = Utils.getTags();
						if (!tags1.contains(tagToAdd)) {
							if (tags1.contains(tagToRemove))
								tags1.remove(tagToRemove);
							tags1.add(tagToAdd);
							Utils.AddTag(tags1);
						}
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
					// Toast.makeText(
					// getApplicationContext(),
					// lp.getLanguage()
					// + " "
					// + mApplicationConfigurations
					// .getPartnerUserId(),
					// Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					try {
						mApplicationConfigurations
								.setUserSelectedLanguage(Constants.LANGUAGE_ENGLISH);
						Utils.changeLanguage(this.getBaseContext(), "English");
						String tagToAdd = "pref_display_English";
						Set<String> tags1 = Utils.getTags();
						if (!tags1.contains(tagToAdd)) {
							if (tags1.contains(tagToRemove))
								tags1.remove(tagToRemove);
							tags1.add(tagToAdd);
							Utils.AddTag(tags1);
						}
					} catch (Exception e1) {
						Logger.printStackTrace(e1);
					}
					e.printStackTrace();
				}
				finishedLoadingAllData = true;
				setResult(RESULT_OK);
				if (isFirstVisitToApp) {
					mApplicationConfigurations.setIsFirstVisitToApp(false);
				} else if (oldLanguageSelected != mApplicationConfigurations
						.getUserSelectedLanguage()) {

					sendBroadcast(new Intent(
							MainActivity.ACTION_LANGUAGE_CHANGED));
				}

				if (isFirstVisitToApp) {
					startService(new Intent(getBaseContext(),
							CampaignsPreferchingService.class));
				} else
					performInventoryLightService();
				if (otpProgrss != null)
					otpProgrss.dismiss();
				finish();
				break;
			case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
				Set<String> tags1 = Utils.getTags();
				if (mApplicationConfigurations.isRealUser()) {
					if (!tags1.contains("logged-in")) {
						if (tags1.contains("not-logged-in"))
							tags1.remove("not-logged-in");

						tags1.add("logged-in");
						Utils.AddTag(tags1);
					}
					try {
						mDataManager.getCurrentSubscriptionPlan(this,
								Utils.getAccountName(this));
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				} else {
					if (!tags1.contains("not-logged-in")) {
						if (tags1.contains("logged-in"))
							tags1.remove("logged-in");
						tags1.add("not-logged-in");
						Utils.AddTag(tags1);
					}
					try {
						mDataManager.getCurrentSubscriptionPlan(this,
								Utils.getAccountName(this));
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}

				String secret1 = mApplicationConfigurations
						.getGigyaSessionSecret();
				String token1 = mApplicationConfigurations
						.getGigyaSessionToken();

				if (!TextUtils.isEmpty(secret1) && !TextUtils.isEmpty(token1)) {
					// GigyaManager mGigyaManager = new GigyaManager(this);
					if (mGigyaManager == null)
						mGigyaManager = new GigyaManager(this);
					mGigyaManager.setSession(token1, secret1);
				}
				if (mTwitterLoginFragment != null) {
					mTwitterLoginFragment.finish();
				}
				mDataManager.GetUserLanguageMap(this);
				if (mApplicationConfigurations.isRealUser() && chkForGigyaLogin) {
					chkForGigyaLogin = false;
					if (mGigyaManager == null)
						mGigyaManager = new GigyaManager(this);
					mGigyaManager.setOnGigyaResponseListener(this);
					mGigyaManager.socializeGetUserInfo();
				}

                if(isFirstVisitToApp){
                    mApplicationConfigurations.setSilentUserLoginPhoneNumber(mApplicationConfigurations.getUserLoginPhoneNumber());
                    mApplicationConfigurations.setSilentPartnerUserId(mApplicationConfigurations.getPartnerUserId());
                    mApplicationConfigurations
                            .setSilentUserGigyaSessionToken(mApplicationConfigurations
                                    .getGigyaSessionToken());
                    mApplicationConfigurations
                            .setSilentUserGigyaSessionSecret(mApplicationConfigurations
                                    .getGigyaSessionSecret());
                    mApplicationConfigurations.setSilentUserSessionID(mApplicationConfigurations.getSessionID());
                    mApplicationConfigurations.setSilentUserHouseholdID(mApplicationConfigurations.getHouseholdID());
                    mApplicationConfigurations.setSilentUserConsumerID(mApplicationConfigurations.getConsumerID());
                    mApplicationConfigurations.setSilentUserPasskey(mApplicationConfigurations.getPasskey());
                }

				break;
			case (OperationDefinition.Hungama.OperationId.FORGOT_PASSWORD):
				hideLoadingDialogFragment();
				String message = (String) responseObjects
						.get(ForgotPasswordOperation.RESPONSE_KEY_MESSAGE);

				Utils.makeText(this,

				message, Toast.LENGTH_SHORT).show();
				if (message.contains("successfully sent"))
					onBackPressed();
				break;

			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		//hideLoadingDialogFragment();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		mIsAnyOperationRunning = false;
		switch (operationId) {

		case (OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE):
			Logger.i(TAG, "Failed getting device ID.");
			break;

		case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
			Logger.i(TAG, "Failed getting partner info.");
			break;

		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
        case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):
//			Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
			if(mTwitterLoginFragment!=null)
				mTwitterLoginFragment.setIsFailerLogin(true);
			mGigyaManager.cancelGigyaProviderLogin();
			isRegistering = false;
			break;

		}

		hideLoadingDialogFragment();
		if (otpProgrss != null)
			otpProgrss.dismiss();
		if (getIntent().getBooleanExtra(LoginActivity.IS_FROM_OTP, false)) {
			finish();
		}
		Logger.i(TAG, errorType.toString() + " : " + errorMessage);
		if (TextUtils.isEmpty(errorMessage)) {
			errorMessage = getString(R.string.application_error_no_connectivity);
		}

		if (errorType != ErrorType.OPERATION_CANCELLED) {
			Utils.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
		}

		if (errorType == ErrorType.NO_CONNECTIVITY && (operationId == OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE
				|| operationId == OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ)) {
			finish();
		}
	}

	// ======================================================
	// Flow callbacks.
	// ======================================================

	@Override
	public void onConnectWithSocialNetworkSelected(
			SocialNetwork selectedSocialNetwork) {
		Logger.d(TAG, "Login with social network was selected.");

		mIsGigyaLoginProcess = true;
		if (selectedSocialNetwork == SocialNetwork.FACEBOOK) {
			ScreenLockStatus.getInstance(getBaseContext()).dontShowAd();
			mGigyaManager.facebookLogin();

		} else if (selectedSocialNetwork == SocialNetwork.GOOGLE) {
			ScreenLockStatus.getInstance(getBaseContext()).dontShowAd();
			mGigyaManager.googleLogin();

		} else if (selectedSocialNetwork == SocialNetwork.TWITTER) {
			ScreenLockStatus.getInstance(getBaseContext()).dontShowAd();
			mGigyaManager.twitterLogin();
		}
	}

	@Override
	public void onLoginWithHungamaSelected(List<SignupField> signupFields) {
		isFromLogin = true;
		Boolean result = checkAndUploadFields(signupFields, mSignOptions.get(0)// Utils.getSignOption(mSignOptions,
																				// SignOption.SET_ID_MYPLAY_LOGIN)//
				.getSetID());

		// Flurry report: Hungama login;
		Map<String, String> reportMap = new HashMap<String, String>();
		Analytics.logEvent(
				FlurryConstants.FlurryUserStatus.HungamaLogin.toString(),
				reportMap);
	}

	@Override
	public void onLoginWithHungamaForgotPasswordSelected() {
		showForgotPasswordPanel();
	}

//	@Override
//	public void onSignUpSelected() {
//		showSignupPanel();
//	}

//	@Override
	public void onSkipSelected() {
		/*
		 * If the user has session id there is no need to call PCP again to
		 * avoid to many dumb users in CatchMedia's servers.
		 */
		String sessionID = mApplicationConfigurations.getSessionID();
		if (!TextUtils.isEmpty(sessionID)) {

			// Call InventoryLight
			// Intent inventoryLightService = new Intent(this,
			// InventoryLightService.class);
			// startService(inventoryLightService);

			/*
			 * no need to call all the registration methods, continue with the
			 * application.
			 */
			hideLoadingDialogFragment();

			setResult(RESULT_OK);
			finish();

			finishedLoadingAllData = true;

		} else {
			// gets the default values of the log
			try {
				SignOption signOption = mSignOptions.get(3);// Utils.getSignOption(mSignOptions,
															// SignOption.SET_ID_SILENT_LOGIN);//

				Map<String, Object> signupFields = new HashMap<String, Object>();

				SignupField phoneNumberFields = signOption.getSignupFields()
						.get(0);
				SignupField hardwareIDFields = signOption.getSignupFields()
						.get(1);

				// adds the device's phone number if available.
				String phoneNumber = mDeviceConfigurations
						.getDevicePhoneNumber();
				Logger.d(TAG, "device phone number: " + phoneNumber);
				if (!TextUtils.isEmpty(phoneNumber)) {
					Map<String, Object> phoneNumberMap = new HashMap<String, Object>();
					phoneNumberMap.put(SignupField.VALUE, phoneNumber);
					signupFields.put(phoneNumberFields.getName(),
							phoneNumberMap);
				}
				isFromLogin = false;
				// adds the device's hardware id if available.
				Map<String, Object> hardwareIDMap = new HashMap<String, Object>();
				hardwareIDMap.put(SignupField.VALUE,
						mDeviceConfigurations.getHardwareId());
				signupFields.put(hardwareIDFields.getName(), hardwareIDMap);
				provider = null;
				mDataManager.createPartnerConsumerProxy(signupFields,
						signOption.getSetID(), this, true);
			} catch (Exception e) {
				Logger.e(TAG + ":505", e.toString());
			}
		}

		// Flurry report: Skips login page;
		Map<String, String> reportMap = new HashMap<String, String>();
		Analytics.logEvent(
				FlurryConstants.FlurryUserStatus.SkipsLoginPage.toString(),
				reportMap);
	}

	@Override
	public void onForgotPasswordSubmit(String identicationString) {
		/*
		 * Sends the user's email to hungama, he will retrieve the password via
		 * Email.
		 */
		try {
			mDataManager.forgotPassword(identicationString, this);
		} catch (RuntimeException e) {
		} catch (Exception e) {
		} catch (Error e) {
		}
	}

	@Override
	public void onPerformSignup(List<SignupField> signupFields) {
		isFromLogin = false;
		Boolean result = checkAndUploadFields(signupFields, mSignOptions.get(1)// Utils.getSignOption(mSignOptions,
																				// SignOption.SET_ID_MYPLAY_SIGNUP)//
				.getSetID());
		// AdXEvent.postEvent(getApplicationContext(), AdXEvent.REGISTRATION);
		// AdXEvent.postEvent(getApplicationContext(), AdXEvent.SIGNUP_COMPLITION);
		// Flurry report: Hungama Sign up;
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(
				FlurryConstants.FlurryUserStatus.UserEntersInfo.toString(),
				result.toString());
		Analytics.logEvent(
				FlurryConstants.FlurryUserStatus.HungamaSignUp.toString(),
				reportMap);
	}

//	@Override
//	public void onPerformLogin() {
//		// go back to the login page.
//		mFragmentManager.popBackStack();
//	}

	// ======================================================
	// Private helper methods.
	// ======================================================

	// private void startTutorialActivity() {
	// // starts the Tutorial activity.
	// Intent startTutorialActivityIntent = new Intent(getApplicationContext(),
	// AppTourActivity.class);
	// startTutorialActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
	// Intent.FLAG_ACTIVITY_NO_ANIMATION);
	// startActivityForResult(startTutorialActivityIntent,
	// TUTORIAL_ACTIVITY_CODE);
	// overridePendingTransition(0, 0);
	// }
	MyProgressDialog mProgressDialog;
	private void showLoadingDialogFragment() {
		try {
			if (!isFirstVisitToApp
					&& !getIntent().getBooleanExtra(LoginActivity.IS_FROM_OTP,
							false)) {
				if (mProgressDialog == null) {
					mProgressDialog = new MyProgressDialog(this);
					mProgressDialog.setCancelable(true);
					mProgressDialog.setCanceledOnTouchOutside(false);
					mProgressDialog.show();
				}
/*
				DialogFragment fragmentDialog = (DialogFragment) mFragmentManager
						.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
				if (fragmentDialog == null && mIsActivityResumed
						&& !isFinishing()) {
					LoadingDialogFragment fragment = LoadingDialogFragment
							.newInstance(R.string.application_dialog_loading);
					fragment.setCancelable(true);
					fragment.show(mFragmentManager,
							LoadingDialogFragment.FRAGMENT_TAG);
				}
*/
			}
		} catch (Exception e) {
		} catch (Error e) {
		}
	}

	private void hideLoadingDialogFragment() {
		try {
			/*if (!isFirstVisitToApp
					&& !getIntent().getBooleanExtra(LoginActivity.IS_FROM_OTP,
							false)) {*/
				/*DialogFragment fragmentDialog = (DialogFragment) mFragmentManager
						.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
				if (fragmentDialog != null) {
					FragmentTransaction fragmentTransaction = mFragmentManager
							.beginTransaction();
					fragmentTransaction.remove(fragmentDialog);
					fragmentDialog.dismissAllowingStateLoss();
					fragmentTransaction.commit();
				}*/
				if (mProgressDialog != null) {
					mProgressDialog.dismiss();
					mProgressDialog = null;
				}
			//}
		} catch (Exception e) {
		} catch (Error e) {
		}
	}

//	@Override
//	public void onBackPressed() {
//
//		if(mFragmentManager.getBackStackEntryCount()<=1)
//			finish();
//		else
//			super.onBackPressed();
//	}

	private void showLoginFields() {
		try {
			if (!isFirstVisitToApp) {
				// attaches the login fragment.
				LoginFragment loginFragment = new LoginFragment();
				if (!mApplicationConfigurations.isuserLoggedIn())
					loginFragment.setSignOprions(mSignOptions);
				loginFragment.setOnLoginOptionSelectedListener(this);
//                loginFragment.setOnSignupOptionSelectedListener(this);

				loginFragment.setArguments(fromActivity);

				FragmentTransaction fragmentTransaction = mFragmentManager
						.beginTransaction();
				fragmentTransaction.add(R.id.login_fragmant_container,
						loginFragment);
				if (isActivityDestroyed()) {
					return;
				}
				if(Constants.IS_COMMITALLOWSTATE)
					fragmentTransaction.commitAllowingStateLoss();
				else
					fragmentTransaction.commit();
			}

		} catch (Exception e) {
		} catch (Error e) {
		}
	}

	private void showForgotPasswordPanel() {
		try {
			// replace the current fragment with the forgot password submission
			// fragment.
			LoginForgotPasswordFragment forgotPasswordFragment = new LoginForgotPasswordFragment();
			forgotPasswordFragment.setOnForgotPasswordSubmitListener(this);

			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit, R.anim.slide_right_enter,
					R.anim.slide_right_exit);
			fragmentTransaction.replace(R.id.login_fragmant_container,
					forgotPasswordFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} catch (Exception e) {
		} catch (Error e) {
		}
	}

//	private void showSignupPanel() {
//		try {
//			// replace the current fragment with the forgot password fragment.
//			LoginSignupFragment loginSignupFragment = new LoginSignupFragment();
//			loginSignupFragment.setOnSignupOptionSelectedListener(this);
//			loginSignupFragment.setSignupFields(mSignOptions.get(1)// Utils.getSignOption(mSignOptions,
//																	// SignOption.SET_ID_MYPLAY_SIGNUP)//
//					.getSignupFields());
//
//			loginSignupFragment.setArguments(fromActivity);
//
//			FragmentTransaction fragmentTransaction = mFragmentManager
//					.beginTransaction();
//			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
//					R.anim.slide_left_exit, R.anim.slide_right_enter,
//					R.anim.slide_right_exit);
//			fragmentTransaction.replace(R.id.login_fragmant_container,
//					loginSignupFragment);
//			fragmentTransaction.addToBackStack(null);
//			fragmentTransaction.commit();
//		} catch (Exception e) {
//		} catch (Error e) {
//		}
//	}

	boolean isRegistering = false, chkForGigyaLogin = false, isFromLogin = false;

	/**
	 * Checks validation of the fields, if they are valid, they will be posted
	 * to the server.
	 */
	private boolean checkAndUploadFields(List<SignupField> signupFields,
			long setID) {

		Map<String, Object> fieldMap = null;
		String value = null;
		Map<String, Object> signupFieldsMap = new HashMap<String, Object>();

		boolean userEntersInfoSuccessful = true;

		for (SignupField signupField : signupFields) {

			/*
			 * validate field - the value is not empty and the field. If the
			 * given field is hidden, the PCP will handle it.
			 */
			if (!TextUtils.isEmpty(signupField.getValue())
					|| SignupFieldType.getSignupFieldTypeByName(signupField
							.getType()) == SignupFieldType.HIDDEN) {

				value = signupField.getValue();

				if (signupField.getName().equalsIgnoreCase(
						SignupField.KEY_FIRST_NAME)
						|| signupField.getName().equalsIgnoreCase(
								SignupField.KEY_LAST_NAME)) {

					if (value != null && value.length() < 2) {
						if (signupField.getName().equalsIgnoreCase(
								SignupField.KEY_FIRST_NAME))
							Utils.makeText(
									this,

									getResources()
											.getString(
													R.string.login_signup_error_first_name),
									Toast.LENGTH_LONG).show();
						else
							Toast.makeText(
									this,

									getResources()
											.getString(
													R.string.login_signup_error_last_name),
									Toast.LENGTH_LONG).show();
						userEntersInfoSuccessful = false;
						return userEntersInfoSuccessful;
					} else if (!Utils.validateName(value)) {
						if (Utils.isAlphaNumeric(value)) {
							Utils.makeText(
									this,

									getResources()
											.getString(
													R.string.login_signup_error_no_in_name)
											+ " " + signupField.getDisplay(),
									Toast.LENGTH_LONG).show();
						} else {
							Utils.makeText(
									this,

									getString(R.string.login_signup_error_special_character)
											+ " " + signupField.getDisplay(),
									Toast.LENGTH_LONG).show();
						}
						userEntersInfoSuccessful = false;
						return userEntersInfoSuccessful;
					}
				} else if (signupField.getName().equalsIgnoreCase(
						SignupField.KEY_EMAIL)) {
					if (!Utils.validateEmailAddress(value.trim())) {
						Utils.makeText(
								this,

								getResources().getString(
										R.string.login_signup_error_email),
								Toast.LENGTH_LONG).show();
						userEntersInfoSuccessful = false;
						return userEntersInfoSuccessful;
					}

				}
				// generate as a field to upload.
				fieldMap = new HashMap<String, Object>();
				fieldMap.put(SignupField.VALUE, value);
				// add to signupfields.
				signupFieldsMap.put(signupField.getName().trim(), fieldMap);
			} else {
				// checks for optional fields as default.
				if (signupField.getOptional() != null
						&& signupField.getOptional().equalsIgnoreCase("true")) {
					// skip, we don't care about empty optional fields.
					continue;
				} else {
					// a mendatory field is empty.
					// TODO: throw an error message to the user..
					Utils.makeText(
							this,
							Utils.getMultilanguageText(
									getApplicationContext(),
									signupField.getDisplay()
											+ " "
											+ getResources()
													.getString(
															R.string.login_signup_error_mandatory)),
							Toast.LENGTH_LONG).show();
					userEntersInfoSuccessful = false;
					return userEntersInfoSuccessful;
				}
			}
		}

		// Save the: enail, first name, last name to shared preferences for
		// later use
		Map<String, String> emailMap = (Map<String, String>) signupFieldsMap
				.get(SignupField.KEY_EMAIL);
		Map<String, String> fnameMap = (Map<String, String>) signupFieldsMap
				.get(SignupField.KEY_FIRST_NAME);
		Map<String, String> lnameMap = (Map<String, String>) signupFieldsMap
				.get(SignupField.KEY_LAST_NAME);

		if (emailMap != null && emailMap.containsKey(SignupField.VALUE)) {
//			mApplicationConfigurations.setHungamaEmail((String) emailMap
//					.get(SignupField.VALUE));
			Utils.setAlias((String) emailMap.get(SignupField.VALUE),
					mDataManager.getDeviceConfigurations().getHardwareId());
		}
		// if (fnameMap != null && fnameMap.containsKey(SignupField.VALUE)) {
		// mApplicationConfigurations.setHungamaFirstName((String) fnameMap
		// .get(SignupField.VALUE));
		// }
		// if (lnameMap != null && lnameMap.containsKey(SignupField.VALUE)) {
		// mApplicationConfigurations.setHungamaLastName((String) lnameMap
		// .get(SignupField.VALUE));
		// }
		isRegistering = true;
		chkForGigyaLogin = true;
		provider = null;
		mDataManager.createPartnerConsumerProxy(signupFieldsMap, setID, this,
				false);
		return userEntersInfoSuccessful;
	}

	protected boolean isActivityDestroyed() {
		return mIsDestroyed;
	}

	// ======================================================
	// STATIC PUBLIC HELPER FIELDS.
	// ======================================================

	public static void buildTextFieldsFromSignupFields(
			LinearLayout mFieldContainer, List<SignupField> signupFields) {
		try {
			// clean all the fields.
			mFieldContainer.removeAllViews();
			// get the sign fields for hungama login.

			Resources resources = mFieldContainer.getResources();
			Context context = mFieldContainer.getContext();

			int lastIndex = signupFields.size() - 1;

			List<SignupField> IMEList = new ArrayList<SignupField>();
			for (SignupField signupField : signupFields) {
				if (signupField.getType().equalsIgnoreCase("hidden")) {
					IMEList.add(signupField);
				}
			}
			IMEList = signupFields;

			SignupField signupField = null;

			LayoutInflater inflater = (LayoutInflater) context
					.getApplicationContext().getSystemService(
							Context.LAYOUT_INFLATER_SERVICE);

			// sets the "Next" and the "Done" button.
			for (int j = 0; j <= IMEList.size() - 1; j++) {
//				LanguageEditText fieldText = (LanguageEditText) inflater
//						.inflate(R.layout.view_text_field, mFieldContainer,
//								false);
                LinearLayout llField = (LinearLayout) inflater
                        .inflate(R.layout.view_text_field, mFieldContainer,
                                false);
                LanguageEditText fieldText = (LanguageEditText) llField.findViewById(R.id.edit_field);
				if (j < lastIndex) {
					fieldText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				} else {
					fieldText.setImeOptions(EditorInfo.IME_ACTION_DONE);
				}
			}

			for (int i = 0; i <= lastIndex; i++) {
				// gets the signup field.
				signupField = signupFields.get(i);
				// constructs field.
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				params.topMargin = resources
						.getDimensionPixelSize(R.dimen.login_content_top_margin);

//				LanguageEditText fieldText = (LanguageEditText) inflater
//						.inflate(R.layout.view_text_field, mFieldContainer,
//								false);
                LinearLayout llField = (LinearLayout) inflater
                        .inflate(R.layout.view_text_field, mFieldContainer,
                                false);

                ImageView ivTypeIcon = (ImageView) llField.findViewById(R.id.iv_type_icon);
                if(signupField.getName().equalsIgnoreCase("email_mobile") ||
                        signupField.getName().equalsIgnoreCase("email")){
                    ivTypeIcon.setImageResource(R.drawable.icon_login_email_field);
                } else if(signupField.getName().equalsIgnoreCase("password")){
                    ivTypeIcon.setImageResource(R.drawable.icon_login_password_field);
                } else if(signupField.getName().equalsIgnoreCase("phone_number") ||
                        signupField.getName().equalsIgnoreCase("mobile")){
                    ivTypeIcon.setImageResource(R.drawable.icon_login_mobile_field);
                }

                LanguageEditText fieldText = (LanguageEditText) llField.findViewById(R.id.edit_field);

                        fieldText.setHint(Utils.getMultilanguageTextLayOut(context,
						signupField.getDisplay()));

				Logger.s("getDisplay------------------"
						+ new com.google.gson.Gson().toJson(signupField)
								.toString());

				// sets input type.
				SignupFieldType signupFieldType = SignupFieldType
						.getSignupFieldTypeByName(signupField.getType());
				if (signupFieldType != SignupFieldType.HIDDEN) {
					fieldText.setInputType(signupFieldType.getInputType());
				}

				// sets max length of input text.
				if (signupField.getMaximumLength() > 0) {
					InputFilter maxLengthFilter = new InputFilter.LengthFilter(
							(int) signupField.getMaximumLength());
					fieldText.setFilters(new InputFilter[] { maxLengthFilter });
				}
				// // sets the "Next" and the "Done" button.
				// if (signupFields.get(i+1) != null &&
				// !signupFields.get(i+1).getType().equalsIgnoreCase("hidden"))
				// {
				// fieldText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
				// } else {
				// fieldText.setImeOptions(EditorInfo.IME_ACTION_DONE);
				// }

				/*
				 * setting the signup field as tag for pulling it for building
				 * the Login / signup request.
				 */
				fieldText.setTag(signupField);

				/*
				 * Hides the hidden fields, stores them as part of the view list
				 * to be count later when posting the credentials to the server.
				 */
				if (signupFieldType == SignupFieldType.HIDDEN) {
					fieldText.setVisibility(View.GONE);
                    llField.setVisibility(View.GONE);
				}

				ApplicationConfigurations appConfig = ApplicationConfigurations
						.getInstance(context);
				if (appConfig.getNawrasMsisdn() != null
						&& appConfig.getNawrasMsisdn().length() > 0
						&& !appConfig.getNawrasMsisdn()
								.equalsIgnoreCase("null")
						&& signupField.getName().equalsIgnoreCase(
								"phone_number")) {
					fieldText.setText(appConfig.getNawrasMsisdn());
					fieldText.setEnabled(false);
				}

//				mFieldContainer.addView(fieldText, params);
                mFieldContainer.addView(llField, params);
			}
			// updates the population.
			mFieldContainer.requestLayout();
		} catch (Exception e) {
            Logger.printStackTrace(e);
		}
	}

	public static List<SignupField> generateSignupFieldsFromTextFields(
			LinearLayout mFieldContainer) {

		List<SignupField> signupFields = new ArrayList<SignupField>();
		try {
			// iterate thru all the fields an retrieves the fields.
			int fieldsCount = mFieldContainer.getChildCount();
			for (int i = 0; i < fieldsCount; i++) {
				// get the Text Field and it's signup field.
				View view = mFieldContainer.getChildAt(i);
				LanguageEditText textField = (LanguageEditText) view.findViewById(R.id.edit_field);
				SignupField signupField = (SignupField) textField.getTag();

				/*
				 * Only visible fields values must be not empty and hidden
				 * values will be populated by the PCP call.
				 */
				if (textField.getVisibility() == View.VISIBLE
						&& textField.getText() != null
						&& !TextUtils.isEmpty(textField.getText().toString())) {

					signupField.setValue(textField.getText().toString());
				} else {
					signupField.setValue(null);
				}

				signupFields.add(signupField);
			}
		} catch (Exception e) {
		}
		return signupFields;
	}

	// ======================================================
	// Gigya Login Listeners.
	// ======================================================

	// @Override
	// public void onSocialNetworkSubmitCredentials(SocialNetwork socialNetwork,
	// String email, String password) {
	// // closes the keyboard and moves back to the login page.
	// mFragmentManager.popBackStack();
	// // TODO: call CPC.
	// }

	@Override
	public void onGigyaLoginListener(SocialNetwork provider,
			Map<String, Object> signupFields, long setId) {
		try {
			this.provider = provider;
			if (provider == SocialNetwork.TWITTER) {
				// Twitter

				FragmentTransaction fragmentTransaction = mFragmentManager
						.beginTransaction();
				fragmentTransaction.setCustomAnimations(
						R.anim.slide_left_enter, R.anim.slide_left_exit,
						R.anim.slide_right_enter, R.anim.slide_right_exit);

				TwitterLoginFragment fragment = new TwitterLoginFragment();
				fragment.init(signupFields, setId);
				fragmentTransaction.replace(R.id.login_fragmant_container,
						fragment, TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
				fragmentTransaction.addToBackStack(null);
				if(Constants.IS_COMMITALLOWSTATE)
					fragmentTransaction.commitAllowingStateLoss();
				else
					fragmentTransaction.commit();

				// Listen to result from TwitterLoinFragment
				fragment.setOnTwitterLoginListener(this);
				hideLoadingDialogFragment();
			} else {
				isFromLogin = true;
				// FaceBook, Google
				isRegistering = true;
				// Call PCP
				if((provider== SocialNetwork.FACEBOOK && TextUtils.isEmpty(mApplicationConfigurations.getGigyaFBEmail()))
						|| (provider== SocialNetwork.GOOGLE && TextUtils.isEmpty(mApplicationConfigurations.getGigyaGoogleEmail()))){
					mGigyaManager.removeConnetion(provider);
					Toast.makeText(this, R.string.gigya_login_error_email_required, Toast.LENGTH_SHORT).show();
				} else {
					mDataManager.createPartnerConsumerProxy(signupFields, setId,
							this, false);
				}
			}

			// Flurry report: Social login
			String registrationStatus;
			if (mApplicationConfigurations.isRealUser()) {
				registrationStatus = FlurryConstants.FlurryUserStatus.Login
						.toString();
			} else {
				registrationStatus = FlurryConstants.FlurryUserStatus.NewRegistration
						.toString();
			}
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(
					FlurryConstants.FlurryUserStatus.TypeOfLogin.toString(),
					provider.name());
			reportMap.put(FlurryConstants.FlurryUserStatus.RegistrationStatus
					.toString(), registrationStatus);
			Analytics.logEvent(
					FlurryConstants.FlurryUserStatus.SocialLogin.toString(),
					reportMap);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	@Override
	public void onTwitterLoginListener(TwitterLoginFragment fragment,
			Map<String, Object> signupFields, long setId) {
		isFromLogin = true;
		isRegistering = true;
		provider = SocialNetwork.TWITTER;
		// Call PCP
		// It's include the email and password that user insert in
		// TwitterLoginFragment
		mDataManager.createPartnerConsumerProxy(signupFields, setId, this,
				false);
		mTwitterLoginFragment = fragment;
	}

	@Override
	public void onCancelLoginListener() {
		provider = null;
		mGigyaManager.removeConnetion(SocialNetwork.TWITTER);
	}

	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {
	}

	@Override
	public void onSocializeGetContactsListener(
			List<GoogleFriend> googleFriendsList) {
	}

	@Override
	public void onSocializeGetUserInfoListener() {
	}

	@Override
	public void onGigyaLogoutListener() {
	}

	@Override
	public void onFacebookInvite() {

	}

	@Override
	public void onTwitterInvite() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener
	 * #onFailSocialGetFriendsContactsListener()
	 */
	@Override
	public void onFailSocialGetFriendsContactsListener() {
		// TODO Auto-generated method stub

	}

	// Handler handle = new Handler();

	// // xtpl
	// // inserted by Hungama
	// private void messageThread() {
	//
	// final Activity _this = this;
	//
	// Thread t = new Thread() {
	// public void run() {
	// URL url;
	// try {
	// DeviceConfigurations deviceConfig = mDataManager
	// .getDeviceConfigurations();
	// String mHardwareId = deviceConfig.getHardwareId();
	// String userAgent = deviceConfig.getDefaultUserAgentString(
	// LoginActivity.this, handle);
	// String macAddress = deviceConfig.getMac(LoginActivity.this);
	// com.hungama.myplay.activity.util.Logger.i("",
	// "SecuredThread");
	// Logger.i("trystart--", "");
	// url = new URL(
	// "https://secure.hungama.com/myplayhungama/device_offer_v2.php?imei="
	// + HungamaApplication.encodeURL(
	// /*
	// * OnApplicationStartsActivity .
	// */mHardwareId, "utf-8")
	// + "&mac="
	// + HungamaApplication.encodeURL(
	// /*
	// * OnApplicationStartsActivity .
	// */macAddress, "utf-8")
	// + "&user_agent="
	// + HungamaApplication.encodeURL(userAgent,
	// "utf-8") + "&login=1");
	// // +"&mac="
	// // +URLEncoder.encode(OnApplicationStartsActivity.macAddress,
	// // "utf-8")
	// // HttpURLConnection urlConnection = (HttpURLConnection)
	// // url.openConnection();
	// Logger.i("URL fetched-", url.toString());
	// HttpsURLConnection urlConnection = (HttpsURLConnection) url
	// .openConnection();
	// if (urlConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
	// Logger.i("URL OK-", "OK");
	// InputStream in = new BufferedInputStream(
	// urlConnection.getInputStream());
	// StringBuilder sb = new StringBuilder();
	// int ch = -1;
	// while ((ch = in.read()) != -1) {
	// sb.append((char) ch);
	// }
	// final String response = parseJSON(sb.toString());
	// _this.runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// showToast(response);
	// }
	// });
	//
	// // Log.i("Response--", response);
	// // parsed=ConnectionStatus.READY;
	// return;
	// } else {
	// Logger.i("URL OK-", "Not OK");
	// }
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// Logger.i("Error-response-", "" + e);
	// } catch (IOException e) {
	// e.printStackTrace();
	// Logger.i("Error-response-", "" + e);
	// } catch (Exception e) {
	// e.printStackTrace();
	// Logger.i("Error-response-", "" + e);
	// }
	// // parsed=ConnectionStatus.FAILED;
	// };
	// };
	// t.start();
	// }

//	private JSONObject jsonObject;
	// String strParsedValue = null;

	// public String parseJSON(String response) throws JSONException {
	// try {
	// com.hungama.myplay.activity.util.Logger.i(TAG, "TrackIMEI>>>"
	// + response);
	// jsonObject = new JSONObject(response);
	//
	// if (jsonObject.getInt("code") == 200) {
	// response = jsonObject.getString("message");
	// } else {
	// response = null;
	// }
	// } catch (Exception e) {
	// response = null;
	// }
	// return response;
	// }

	// public void showToast(String response) {
	// if (response == null)
	// return;
	// // Toast--------------------------
	//
	// try {
	// final PopupWindow popupWindow = new PopupWindow(this, response);
	// popupWindow.show(findViewById(R.id.lmain), 0, 0);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }

//	private static String ua;

	// public static String getDefaultUserAgentString(final Activity activity) {
	// if (Build.VERSION.SDK_INT >= 17) {
	// return NewApiWrapper.getDefaultUserAgent(activity);
	// }
	//
	// try {
	// Constructor<WebSettings> constructor = WebSettings.class
	// .getDeclaredConstructor(Context.class, WebView.class);
	// constructor.setAccessible(true);
	// try {
	// WebSettings settings = constructor.newInstance(activity, null);
	// return settings.getUserAgentString();
	// } finally {
	// constructor.setAccessible(false);
	// }
	// } catch (Exception e) {
	// // return new WebView(context).getSettings().getUserAgentString();
	//
	// if (Thread.currentThread().getName().equalsIgnoreCase("main")) {
	// WebView m_webview = new WebView(activity);
	// return m_webview.getSettings().getUserAgentString();
	// } else {
	// final Object runObj = new Object();
	// Runnable runnable = new Runnable() {
	// @Override
	// public void run() {
	// // Looper.prepare();
	// WebView m_webview = new WebView(activity);
	// ua = m_webview.getSettings().getUserAgentString();
	// synchronized (runObj) {
	// runObj.notifyAll();
	// }
	// // Looper.loop();
	// }
	// };
	//
	// // mContext = context;
	// synchronized (runObj) {
	// try {
	// activity.runOnUiThread(runnable);
	// runObj.wait();
	// } catch (InterruptedException e1) {
	// e1.printStackTrace();
	// Logger.e(TAG, "run sync" + e1);
	// }
	// }
	// return ua;
	// }
	// }
	// }

	// @TargetApi(17)
	static class NewApiWrapper {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		static String getDefaultUserAgent(Context context) {
			return WebSettings.getDefaultUserAgent(context);
		}
	}

	// finish Hungama
	// xtpl

	private void performInventoryLightService() {
		try {
			// Sync's the inventory.
//			Intent inventoryLightService = new Intent(this,
//					InventoryLightService.class);
			// startService(inventoryLightService);

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// private void unbindDrawables(View view, int version) {
	// // view.getBackground().setCallback(null);
	// if (view.getBackground() != null) {
	// view.getBackground().setCallback(null);
	// if (version >= 16)
	// view.setBackground(null);
	// else
	// view.setBackgroundDrawable(null);
	// } else if (view instanceof ImageView) {
	// if (version >= 16)
	// view.setBackground(null);
	// else
	// view.setBackgroundDrawable(null);
	// ((ImageView) view).setImageDrawable(null);
	// } else if (view instanceof ImageButton) {
	// if (version >= 16)
	// view.setBackground(null);
	// else
	// view.setBackgroundDrawable(null);
	// ((ImageButton) view).setImageDrawable(null);
	// }
	// if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
	// for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
	// unbindDrawables(((ViewGroup) view).getChildAt(i), version);
	// }
	// ((ViewGroup) view).removeAllViews();
	// }
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener
	 * #onCancelRequestListener()
	 */
	@Override
	public void onCancelRequestListener() {
	}

	@Override
	public void onConnectionRemoved() {

	}

	private void doOtpLogin() {
		try {
			SignOption signOption = mSignOptions.get(3);
			// Utils.getSignOption(mSignOptions,
			// SignOption.SET_ID_MOBILE_LOGIN);

			Map<String, Object> signupFields = new HashMap<String, Object>();

			SignupField phoneNumberFields = signOption.getSignupFields().get(0);
			SignupField hardwareIDFields = signOption.getSignupFields().get(1);
			SignupField isMobileLogin = signOption.getSignupFields().get(2);

			// adds the device's phone number if available.
			// String phoneNumber = mDeviceConfigurations
			// .getDevicePhoneNumber();
			String phoneNumber = getIntent().getStringExtra(OTP_MOBILE_NO);
			Logger.d(TAG, "device phone number: " + phoneNumber);
			if (!TextUtils.isEmpty(phoneNumber)) {
				Map<String, Object> phoneNumberMap = new HashMap<String, Object>();
				phoneNumberMap.put(SignupField.VALUE, phoneNumber);
				signupFields.put(phoneNumberFields.getName(), phoneNumberMap);
			}

			// adds the device's hardware id if available.
			Map<String, Object> hardwareIDMap = new HashMap<String, Object>();
			hardwareIDMap.put(SignupField.VALUE,
					mDeviceConfigurations.getHardwareId());
			signupFields.put(hardwareIDFields.getName(), hardwareIDMap);

			Map<String, Object> isMobileLoginMap = new HashMap<String, Object>();
			isMobileLoginMap.put(SignupField.VALUE, "1");
			signupFields.put(isMobileLogin.getName(), isMobileLoginMap);
			isFromLogin = true;
			isRegistering = true;
			chkForGigyaLogin = true;
			mDataManager.createPartnerConsumerProxy(signupFields,
					signOption.getSetID(), this, true);
		} catch (Exception e) {
			Logger.e(TAG + ":505", e.toString());
		}
	}
}
