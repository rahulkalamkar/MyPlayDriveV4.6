package com.hungama.myplay.activity.ui.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.data.dao.hungama.UserProfileResponse;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment.OnTwitterLoginListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.hungama.GetUserProfileOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.preference.PushPreferencesActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingFragmentNew extends Fragment implements OnClickListener,
		CommunicationOperationListener, OnSeekBarChangeListener,
		OnCheckedChangeListener,
		android.widget.CompoundButton.OnCheckedChangeListener,
		OnGigyaResponseListener, OnTwitterLoginListener {
	// public static final int UPGRADE_ACTIVITY_RESULT_CODE = 1001;

	private static final String TAG = "SettingsFragment";
	private static final String VALUE = "value";

	// Views
	private LinearLayout mLayoutAccountFacebook;
	private LinearLayout mLayoutAccountTwitter;
	private LinearLayout mLayoutAccountGooglePlus;
	private LinearLayout mLayoutAccountHungama;
	private LinearLayout mLayoutSettingsMembership;
	private LinearLayout mLayoutSettingsSaveOffline;
	private LinearLayout mLayoutSettingAudioQuality;
	private LinearLayout mLayoutSettingsMyStream;
	private LinearLayout mLayoutSettingsNotifications;
	private LinearLayout mLayoutSettingsLanguages, ll_mystream_trivia,
			ll_mystream_lyrics, ll_mystream_offline;

	private View dialogParentView;

	private ImageView fbLoginSign;
	private ImageView twitterLoginSign;
	private ImageView googlLoginSign;
	private ImageView hungamaLoginSign;
	private ImageView membershipArrow;

	private LanguageTextView membershipTextView;
	private LanguageTextView languagesummary;
	Dialog dialog, dialoglanguage;

	private Switch switch_trivia, switch_lyrics, switch_offline;

	private LanguageTextView audio_summary;
	private SeekBar volumeSeekBar;
	private RadioGroup bitrateRadioGroup;

	private RadioButton bitrateRadioButtonAuto;
	private RadioButton bitrateRadioButtonLow;
	private RadioButton bitrateRadioButtonMedium;
	private RadioButton bitrateRadioButtonHigh;
	private RadioButton bitrateRadioButtonHD;

	// Managers
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private AudioManager mAudioManager;
	private GigyaManager mGigyaManager;

	// Data members
	private String mSubscriptionPlan;
	private boolean mHasSubscriptionPlan;
	// private Dialog dialog ;
	private TwitterLoginFragment mTwitterLoginFragment;

	private boolean mIsActivityResumed = false;

	private boolean isHideLoadingDialog;

	private ScrollView settings_scroll_view;
	private View rootView;
	private View dialogview;
	private View dialoglanguageView;

	private MyProgressDialog mProgressDialog;

	SettingsContentObserver mSettingsContentObserver;

	// ======================================================
	// Life cycle callbacks.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		mGigyaManager = new GigyaManager(getActivity());

		mSettingsContentObserver = new SettingsContentObserver(getActivity(),
				new Handler());
		getActivity().getContentResolver().registerContentObserver(
				android.provider.Settings.System.CONTENT_URI, true,
				mSettingsContentObserver);
		Analytics.postCrashlitycsLog(getActivity(), SettingFragmentNew.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			// // Fetch the root view
			rootView = inflater.inflate(R.layout.setting_ui_new, container,
					false);
			dialogview = inflater.inflate(R.layout.audio_quality_dialog,
					container, false);

			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
				Utils.traverseChild(dialogview, getActivity());
				Utils.traverseChild(dialoglanguageView, getActivity());
			}

			settings_scroll_view = (ScrollView) rootView
					.findViewById(R.id.settings_scroll_view);

			mLayoutAccountFacebook = (LinearLayout) rootView
					.findViewById(R.id.facebook_view);
			mLayoutAccountTwitter = (LinearLayout) rootView
					.findViewById(R.id.twitter_view);
			mLayoutAccountGooglePlus = (LinearLayout) rootView
					.findViewById(R.id.google_plus_view);
			mLayoutAccountHungama = (LinearLayout) rootView
					.findViewById(R.id.hungama_view);
			mLayoutSettingsMembership = (LinearLayout) rootView
					.findViewById(R.id.membership_status_view);
			mLayoutSettingsSaveOffline = (LinearLayout) rootView
					.findViewById(R.id.save_offline_settings_view);
			mLayoutSettingsMyStream = (LinearLayout) rootView
					.findViewById(R.id.mystream_settings_view);
			mLayoutSettingsNotifications = (LinearLayout) rootView
					.findViewById(R.id.notifications_settings_view);
			mLayoutSettingsLanguages = (LinearLayout) rootView
					.findViewById(R.id.languages_settings_view);

			ll_mystream_trivia = (LinearLayout) rootView
					.findViewById(R.id.ll_mystream_trivia);
			ll_mystream_lyrics = (LinearLayout) rootView
					.findViewById(R.id.ll_mystream_lyrics);
			ll_mystream_offline = (LinearLayout) rootView
					.findViewById(R.id.ll_mystream_autooffline);

			ll_mystream_trivia.setOnClickListener(this);
			ll_mystream_lyrics.setOnClickListener(this);
			ll_mystream_offline.setOnClickListener(this);

			mLayoutSettingAudioQuality = (LinearLayout) rootView
					.findViewById(R.id.audio_main);

			volumeSeekBar = (SeekBar) rootView
					.findViewById(R.id.volume_seek_bar);

			fbLoginSign = (ImageView) rootView
					.findViewById(R.id.fb_loging_sign);
			twitterLoginSign = (ImageView) rootView
					.findViewById(R.id.twitter_login_sign);
			googlLoginSign = (ImageView) rootView
					.findViewById(R.id.google_login_sign);
			hungamaLoginSign = (ImageView) rootView
					.findViewById(R.id.hungama_login_sign);
			membershipTextView = (LanguageTextView) rootView
					.findViewById(R.id.membership_textview);
			membershipArrow = (ImageView) rootView
					.findViewById(R.id.membership_iv_arrow);

			switch_trivia = (Switch) rootView.findViewById(R.id.switch_trivia);
			switch_lyrics = (Switch) rootView.findViewById(R.id.switch_lyrics);
			switch_offline = (Switch) rootView
					.findViewById(R.id.switch_offline);

			audio_summary = (LanguageTextView) rootView
					.findViewById(R.id.audio_summary_text);
			languagesummary = (LanguageTextView) rootView
					.findViewById(R.id.language_summary_text);

			dialog = new Dialog(
					getActivity(),
					android.support.v7.appcompat.R.style.Theme_AppCompat_Light_Dialog);

			dialog.getWindow();
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			dialog.setContentView(R.layout.audio_quality_dialog);
			dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			dialogParentView = dialog.findViewById(R.id.rlAudioQualityMain);
			bitrateRadioGroup = (RadioGroup) dialog
					.findViewById(R.id.bitrateRadioGroup);

			// Volume settings
			setVolume();

			// Bit-Rate settings
			bitrateRadioButtonAuto = (RadioButton) dialog
					.findViewById(R.id.radio_button_auto);
			bitrateRadioButtonLow = (RadioButton) dialog
					.findViewById(R.id.radio_button_low);
			bitrateRadioButtonMedium = (RadioButton) dialog
					.findViewById(R.id.radio_button_medium);
			bitrateRadioButtonHigh = (RadioButton) dialog
					.findViewById(R.id.radio_button_high);
			bitrateRadioButtonHD = (RadioButton) dialog
					.findViewById(R.id.radio_button_hd);

			bitrateRadioButtonAuto.setOnClickListener(onRadioBtnClick);
			bitrateRadioButtonLow.setOnClickListener(onRadioBtnClick);
			bitrateRadioButtonMedium.setOnClickListener(onRadioBtnClick);
			bitrateRadioButtonHigh.setOnClickListener(onRadioBtnClick);
			bitrateRadioButtonHD.setOnClickListener(onRadioBtnClick);

			TextView radio_txt_auto = (TextView) dialog
					.findViewById(R.id.radio_txt_auto);
			radio_txt_auto.setOnClickListener(onRadioBtnClick);

			TextView radio_txt_low = (TextView) dialog
					.findViewById(R.id.radio_txt_low);
			radio_txt_low.setOnClickListener(onRadioBtnClick);

			TextView radio_txt_medium = (TextView) dialog
					.findViewById(R.id.radio_txt_medium);
			radio_txt_medium.setOnClickListener(onRadioBtnClick);

			TextView radio_txt_high = (TextView) dialog
					.findViewById(R.id.radio_txt_high);
			radio_txt_high.setOnClickListener(onRadioBtnClick);

			TextView radio_txt_hd = (TextView) dialog
					.findViewById(R.id.radio_txt_hd);
			radio_txt_hd.setOnClickListener(onRadioBtnClick);

			// App Hints settings
			setlanguage();

			setBitrate();
			// Membership text settings
			isHideLoadingDialog = false;
			setMembershipText();

			// Set listeners
			setViewsListeners();

			// Set login check marks
			setSocialLoginStatus();

			if (((SettingsActivity) getActivity()).isAudioSetting) {
				settings_scroll_view.post(new Runnable() {
					@Override
					public void run() {
						settings_scroll_view.fullScroll(View.FOCUS_DOWN);
						audiodialog();
					}
				});
			}
		} else {
			ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
			parent.removeView(rootView);
		}

		return rootView;
	}

	/**
	 * hanlde radio click for audio quality
	 */
	OnClickListener onRadioBtnClick = new OnClickListener() {
		public void onClick(View v) {
			int checkedId = v.getId();
			if (v instanceof TextView) {
				if (v.getId() == R.id.radio_txt_auto)
					checkedId = bitrateRadioButtonAuto.getId();
				else if (v.getId() == R.id.radio_txt_hd)
					checkedId = bitrateRadioButtonHD.getId();
				else if (v.getId() == R.id.radio_txt_high)
					checkedId = bitrateRadioButtonHigh.getId();
				else if (v.getId() == R.id.radio_txt_low)
					checkedId = bitrateRadioButtonLow.getId();
				else if (v.getId() == R.id.radio_txt_medium)
					checkedId = bitrateRadioButtonMedium.getId();
			}
			onCheckedChanged(bitrateRadioGroup, checkedId);
			setBitrate();
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Start dialog until GetuserInfo returns
		mIsActivityResumed = true;

		// Get the social networks info (logged in or not?)
		isHideLoadingDialog = false;
		setMembershipText();
		mGigyaManager.socializeGetUserInfo();

	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
	}

	@Override
	public void onPause() {
		super.onPause();
		mIsActivityResumed = false;
	}

	private boolean isShowMembership = false;

	@Override
	public void onResume() {
		super.onResume();

		if (mHasSubscriptionPlan
				&& !mApplicationConfigurations.isUserHasSubscriptionPlan()) {
			String accountType = Utils.getAccountName(getActivity());
			mDataManager.getCurrentSubscriptionPlan(this, accountType);
			setMembershipText();
		}

		mIsActivityResumed = true;
		if(isShowMembership){
			isShowMembership = false;
			getActivity().finish();
		} else if (getActivity().getIntent().getBooleanExtra("show_membership", false)) {
			getActivity().getIntent().removeExtra("show_membership");
			addMembershipDetailsFragment();
			isShowMembership = true;
		}
		if (getActivity().getIntent().getBooleanExtra("show_languages", false)) {
			getActivity().getIntent().removeExtra("show_languages");
			addLanguageSettingsFragment();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == UpgradeActivity.LOGIN_ACTIVITY_CODE
				&& resultCode == getActivity().RESULT_OK) {
			String session = mDataManager.getApplicationConfigurations()
					.getSessionID();
			Boolean isRealUser = mDataManager.getApplicationConfigurations()
					.isRealUser();
			if (!TextUtils.isEmpty(session) && (isRealUser || Logger.allowPlanForSilentUser)) {
				if (mApplicationConfigurations
						.isTrialCheckedForUserId(mApplicationConfigurations
								.getPartnerUserId())) {
					openUpgradeActivity();
				} else {
					String accountType = Utils.getAccountName(getActivity());
					mDataManager.getCurrentSubscriptionPlan(
							offlineUpgradeListener, accountType);
				}
				Intent new_intent = new Intent();
				new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
				getActivity().sendBroadcast(new_intent);
			} else {
				Toast toast = Utils.makeText(getActivity(),
						getString(R.string.before_upgrade_login),
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER_VERTICAL
						| Gravity.CENTER_HORIZONTAL, 0, 0);
				toast.show();
			}
		}

		if (resultCode == Activity.RESULT_OK
				&& !mApplicationConfigurations.isUserHasSubscriptionPlan()) {
			String accountType = Utils.getAccountName(getActivity());

			mDataManager.getCurrentSubscriptionPlan(this, accountType);

			isHideLoadingDialog = true;
			setMembershipText();
		} else if (isHDSelected) {
			if (mApplicationConfigurations.isUserHasSubscriptionPlan()
					|| mApplicationConfigurations
							.isUserHasTrialSubscriptionPlan()) {
				mApplicationConfigurations
						.setBitRateState(ApplicationConfigurations.BITRATE_HD);
				setMembershipText();

				Intent new_intent = new Intent();
				new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
				getActivity().sendBroadcast(new_intent);
			}
			setBitrate();
			isHDSelected = false;
		} else if (resultCode == Activity.RESULT_OK
				&& mApplicationConfigurations.isUserHasSubscriptionPlan()) {
			String accountType = Utils.getAccountName(getActivity());

			mDataManager.getCurrentSubscriptionPlan(this, accountType);

			isHideLoadingDialog = true;
			setMembershipText();

			Intent new_intent = new Intent();
			new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
			getActivity().sendBroadcast(new_intent);
		}
	};

	/**
	 * open upgrade option for free user
	 */
	private void openUpgradeActivity() {

		mApplicationConfigurations.setSaveOfflineMode(true);
		Intent intent = new Intent(MainActivity.ACTION_OFFLINE_MODE_CHANGED);
		intent.putExtra("open_upgrade_popup", true);
		getActivity().sendBroadcast(intent);

		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
				FlurryConstants.FlurryCaching.Settings.toString());
		reportMap.put(FlurryConstants.FlurryCaching.UserStatus.toString(),
				Utils.getUserState(getActivity()));
		Analytics.logEvent(FlurryConstants.FlurryCaching.GoOffline.toString(),
				reportMap);
	}

	/**
	 * go offline mode
	 * 
	 * @param isFromNoInternetPrompt
	 */
	private void goToOfflineMode(final boolean isFromNoInternetPrompt) {
		mApplicationConfigurations.setSaveOfflineMode(true);
		getActivity().sendBroadcast(
				new Intent(MainActivity.ACTION_OFFLINE_MODE_CHANGED));

		Map<String, String> reportMap = new HashMap<String, String>();
		if (isFromNoInternetPrompt)
			reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
					FlurryConstants.FlurryCaching.NoInternetPrompt.toString());
		else
			reportMap.put(FlurryConstants.FlurryCaching.Source.toString(),
					FlurryConstants.FlurryCaching.Settings.toString());
		reportMap.put(FlurryConstants.FlurryCaching.UserStatus.toString(),
				Utils.getUserState(getActivity()));
		Analytics.logEvent(FlurryConstants.FlurryCaching.GoOffline.toString(),
				reportMap);
	}

	/**
	 * hide loading dialog
	 */
	private void hideLoadingDialog() {
		try {
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * show loading dialog
	 */
	private void showLoadingDialog() {
		try {
			// showLoadingDialog(Utils.getMultilanguageTextHindi(getActivity(),
			// getResources().getString(messageResource)));
			if (!getActivity().isFinishing()) {
				if (mProgressDialog == null) {
					mProgressDialog = new MyProgressDialog(getActivity());
					mProgressDialog.setCancelable(true);
					mProgressDialog.setCanceledOnTouchOutside(false);
					if(!mProgressDialog.isShowing())
						mProgressDialog.show();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} catch (Error e) {
			e.printStackTrace();
		}
	}

	/**
	 * Call back listener for subscription and plan check
	 */
	CommunicationOperationListener offlineUpgradeListener = new CommunicationOperationListener() {
		@Override
		public void onSuccess(int operationId,
				Map<String, Object> responseObjects) {
			switch (operationId) {
			case OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK: {
				SubscriptionStatusResponse subscriptionStatusResponse = (SubscriptionStatusResponse) responseObjects
						.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
				if (subscriptionStatusResponse != null) {
					if (subscriptionStatusResponse.getSubscription()!=null &&
							subscriptionStatusResponse.getSubscription().getSubscriptionStatus()==1) {
						mDataManager
								.storeCurrentSubscriptionPlanNew(subscriptionStatusResponse);

						if (subscriptionStatusResponse.getSubscription().isTrial()) {
							Utils.makeText(
									getActivity(),
									getResources().getString(
											R.string.already_subscribed)
											+ " "
											+ subscriptionStatusResponse
											.getSubscription()
											.getTrailExpiryDaysLeft()
											+ " "
											+ getString(R.string.days_left),
									Toast.LENGTH_SHORT).show();
						} else {
							Utils.makeText(
									getActivity(),
									getResources().getString(
											R.string.already_subscribed),
									Toast.LENGTH_SHORT).show();
						}
						mApplicationConfigurations
								.setTrialCheckedForUserId(mApplicationConfigurations
										.getPartnerUserId());
						goToOfflineMode(false);
						hideLoadingDialog();
						return;
					} else {
						mApplicationConfigurations
								.setTrialCheckedForUserId(mApplicationConfigurations
										.getPartnerUserId());
//							mDataManager.getSubscriptionPlans(0,
//									SubscriptionType.PLAN, this, true);
						openUpgradeActivity();
						return;
					}
				}
				openUpgradeActivity();
				hideLoadingDialog();
				break;
			}
			}
		}

		@Override
		public void onStart(int operationId) {
			showLoadingDialog();
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType,
				String errorMessage) {
			hideLoadingDialog();
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// case R.id.facebook_button:
		case R.id.facebook_view:
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			if (mGigyaManager.isFBConnected()) {
				addAccountSettingsFragment(SocialNetwork.FACEBOOK);
			} else {
				mGigyaManager.facebookLogin();
			}
			SettingsActivity.ReoladHomeScreen = true;
			break;

		case R.id.twitter_button:
		case R.id.twitter_view:
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			if (mGigyaManager.isTwitterConnected()) {
				addAccountSettingsFragment(SocialNetwork.TWITTER);
			} else {
				mGigyaManager.twitterLogin();
			}
			SettingsActivity.ReoladHomeScreen = true;
			break;

		case R.id.google_plus_button:
		case R.id.google_plus_view:
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			if (mGigyaManager.isGoogleConnected()) {
				addAccountSettingsFragment(SocialNetwork.GOOGLE);
			} else {
				mGigyaManager.googleLogin();
			}
			SettingsActivity.ReoladHomeScreen = true;
			break;

		case R.id.hungama_button:
		case R.id.hungama_view:

			boolean isRealUser = mApplicationConfigurations.isRealUser();
			if (isRealUser) {
				addAccountSettingsFragment(null);
			} else {
				// Call LoginActivity
				startLoginActivity();
			}
			SettingsActivity.ReoladHomeScreen = true;
			break;
		case R.id.membership_textview:
		case R.id.membership_status_view:
			SettingsActivity.ReoladHomeScreen = true;
			addMembershipDetailsFragment();

			break;

		case R.id.mystream_settings_view:
		case R.id.mystream_button:
			addMyStreamSettingsFragment();
			break;

		case R.id.notifications_settings_view:
		case R.id.notification_button:
			startActivity(new Intent(getActivity().getApplicationContext(),
					PushPreferencesActivity.class));
			break;
		case R.id.languages_settings_view:
		case R.id.language_button:
			addLanguageSettingsFragment();
			break;
		case R.id.audio_main:
		case R.id.audio_quality_text:
			audiodialog();
			break;

		case R.id.save_offline_settings_view:
		case R.id.save_offline_textview:
			// if (CacheManager.isProUser(getActivity())) {
			addSaveOfflineSettingsFragment();
			// } else {
			// Utils.makeText(getActivity(),getActivity().getString(R.string.offline_setting_text),
			// Toast.LENGTH_SHORT).show();
			// }
			break;
		case R.id.ll_mystream_trivia:
			if (switch_trivia.isChecked()) {
				switch_trivia.setChecked(false);
			} else
				switch_trivia.setChecked(true);
			break;
		case R.id.ll_mystream_lyrics:
			if (switch_lyrics.isChecked()) {
				switch_lyrics.setChecked(false);
			} else
				switch_lyrics.setChecked(true);
			break;
		case R.id.ll_mystream_autooffline:
			if (switch_offline.isChecked()) {
				switch_offline.setChecked(false);
			} else
				switch_offline.setChecked(true);
			break;
		default:
			break;
		}
	}

	// ======================================================
	// Operations callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		showLoadingDialog();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case (OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET):

				break;

			case (OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK):
				isHideLoadingDialog = false;
				setMembershipText();

				// Start dialog until GetuserInfo returns
				mIsActivityResumed = true;

				mGigyaManager = new GigyaManager(getActivity());
				mGigyaManager.setOnGigyaResponseListener(this);

				// Get the social networks info (logged in or not?)
				mGigyaManager.socializeGetUserInfo();

				if (isHDSelected) {
					if (mApplicationConfigurations.isUserHasSubscriptionPlan()
							|| mApplicationConfigurations
									.isUserHasTrialSubscriptionPlan()) {
						mApplicationConfigurations
								.setBitRateState(ApplicationConfigurations.BITRATE_HD);
					}
					setBitrate();
					isHDSelected = false;
				}
				break;

			case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):

				String activationCode = (String) responseObjects
						.get(ApplicationConfigurations.ACTIVATION_CODE);
				String partnerUserId = (String) responseObjects
						.get(ApplicationConfigurations.PARTNER_USER_ID);
				boolean isRealUser = (Boolean) responseObjects
						.get(ApplicationConfigurations.IS_REAL_USER);
				Map<String, Object> signupFieldsMap = (Map<String, Object>) responseObjects
						.get(PartnerConsumerProxyCreateOperation.RESPONSE_KEY_OBJECT_SIGNUP_FIELDS);

				/*
				 * iterates thru the original signup fields, looking for the
				 * registered phone number, if exists, stores it in the
				 * application configuration as part of the user's credentials.
				 */
				Map<String, Object> fieldMap = (Map<String, Object>) signupFieldsMap
						.get("phone_number");
				String value = "";
				if (fieldMap != null) {
					value = (String) fieldMap.get(VALUE);
				}
				mApplicationConfigurations.setUserLoginPhoneNumber(value);

				// stores partner user id to connect with Hungama REST API.
				mApplicationConfigurations.setPartnerUserId(partnerUserId);
				// mApplicationConfigurations.setIsRealUser(isRealUser);

				// let's party!
				mDataManager.createDeviceActivationLogin(activationCode, this);

				mApplicationConfigurations.setIsUserRegistered(true);
				mDataManager.getUserProfileDetail(this);

				Set<String> tags = Utils.getTags();
				if (!tags.contains("registered_user")) {
					if (tags.contains("non_registered_user"))
						tags.remove("non_registered_user");

					tags.add("registered_user");
					Utils.AddTag(tags);
				}
				break;

            case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):

//                String activationCode = (String) responseObjects
//                        .get(ApplicationConfigurations.ACTIVATION_CODE);
//                String partnerUserId = (String) responseObjects
//                        .get(ApplicationConfigurations.PARTNER_USER_ID);
//                boolean isRealUser = (Boolean) responseObjects
//                        .get(ApplicationConfigurations.IS_REAL_USER);
                signupFieldsMap = (Map<String, Object>) responseObjects
                        .get(PartnerConsumerProxyCreateOperation.RESPONSE_KEY_OBJECT_SIGNUP_FIELDS);

            /*
             * iterates thru the original signup fields, looking for the
             * registered phone number, if exists, stores it in the
             * application configuration as part of the user's credentials.
             */
                fieldMap = (Map<String, Object>) signupFieldsMap
                        .get("phone_number");
                value = "";
                if (fieldMap != null) {
                    value = (String) fieldMap.get(VALUE);
                }
                mApplicationConfigurations.setUserLoginPhoneNumber(value);

                // stores partner user id to connect with Hungama REST API.
//                mApplicationConfigurations.setPartnerUserId(partnerUserId);
                // mApplicationConfigurations.setIsRealUser(isRealUser);

                // let's party!
//                mDataManager.createDeviceActivationLogin(activationCode, this);
//				ApsalarEvent.postEvent(getActivity(), ApsalarEvent.LOGIN_COMPLETED);
				ApsalarEvent.postLoginEvent(provider);

                mApplicationConfigurations.setIsUserRegistered(true);
                mDataManager.getUserProfileDetail(this);

                tags = Utils.getTags();
                if (!tags.contains("registered_user")) {
                    if (tags.contains("non_registered_user"))
                        tags.remove("non_registered_user");

                    tags.add("registered_user");
                    Utils.AddTag(tags);
                }

                if (mTwitterLoginFragment != null) {
                    mTwitterLoginFragment.finish();
                }

                String secret = mApplicationConfigurations
                        .getGigyaSessionSecret();
                String token = mApplicationConfigurations
                        .getGigyaSessionToken();

                if (!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)) {
                    GigyaManager mGigyaManager = new GigyaManager(getActivity());
                    mGigyaManager.setSession(token, secret);
                }

                mGigyaManager.socializeGetUserInfo();
                break;

			case OperationDefinition.Hungama.OperationId.GET_USER_PROFILE:
				hideLoadingDialog();
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
				break;

			case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
//				ApsalarEvent.postEvent(getActivity(), ApsalarEvent.LOGIN_COMPLETED);
				ApsalarEvent.postLoginEvent(provider);

				Map<String, Object> responseMap = (Map<String, Object>) responseObjects
						.get(CMOperation.RESPONSE_KEY_GENERAL_OBJECT);
				// stores the session and other crucial properties.
				String sessionID = (String) responseMap
						.get(ApplicationConfigurations.SESSION_ID);
				int householdID = ((Long) responseMap
						.get(ApplicationConfigurations.HOUSEHOLD_ID))
						.intValue();
				int consumerID = ((Long) responseMap
						.get(ApplicationConfigurations.CONSUMER_ID)).intValue();
				String passkey = (String) responseMap
						.get(ApplicationConfigurations.PASSKEY);

				mApplicationConfigurations.setSessionID(sessionID);
				mApplicationConfigurations.setHouseholdID(householdID);
				mApplicationConfigurations.setConsumerID(consumerID);
				mApplicationConfigurations.setPasskey(passkey);

				if (mTwitterLoginFragment != null) {
					mTwitterLoginFragment.finish();
				}

				secret = mApplicationConfigurations
						.getGigyaSessionSecret();
				token = mApplicationConfigurations
						.getGigyaSessionToken();

				if (!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)) {
					GigyaManager mGigyaManager = new GigyaManager(getActivity());
					mGigyaManager.setSession(token, secret);
				}

				mGigyaManager.socializeGetUserInfo();
				break;
			case (OperationDefinition.Hungama.OperationId.MY_STREAM_SETTINGS_UPDATE):

				hideLoadingDialog();

				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.printStackTrace(e);
		}
		// hideLoadingDialogFragment();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		switch (operationId) {
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
        case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):
			if (GigyaManager.provider != SocialNetwork.TWITTER) {
				mGigyaManager.cancelGigyaProviderLogin();
			}
			break;
		}

		hideLoadingDialog();

		if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
			Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * set listeners for all different event click
	 */
	private void setViewsListeners() {

		volumeSeekBar.setOnSeekBarChangeListener(this);

		mLayoutAccountFacebook.setOnClickListener(this);
		mLayoutAccountTwitter.setOnClickListener(this);
		mLayoutAccountGooglePlus.setOnClickListener(this);
		mLayoutAccountHungama.setOnClickListener(this);
		mLayoutSettingsMyStream.setOnClickListener(this);
		mLayoutSettingAudioQuality.setOnClickListener(this);

		bitrateRadioGroup.setOnCheckedChangeListener(this);

		switch_trivia.setOnCheckedChangeListener(this);
		switch_lyrics.setOnCheckedChangeListener(this);
		switch_offline.setOnCheckedChangeListener(this);

		mLayoutSettingsMembership.setOnClickListener(this);
		mLayoutSettingsSaveOffline.setOnClickListener(this);
		mLayoutSettingsNotifications.setOnClickListener(this);

		mLayoutSettingsLanguages.setOnClickListener(this);

		// ((LanguageButton) rootView.findViewById(R.id.audio_quality_text))
		// .setOnClickListener(this);

		mGigyaManager.setOnGigyaResponseListener(this);

		if (mApplicationConfigurations.needToShowTrivia())
			switch_trivia.setChecked(true);
		else
			switch_trivia.setChecked(false);

		if (mApplicationConfigurations.needToShowLyrics())
			switch_lyrics.setChecked(true);
		else
			switch_lyrics.setChecked(false);

		if (mApplicationConfigurations.getSaveOfflineAutoModeRemember())
			switch_offline.setChecked(true);
		else
			switch_offline.setChecked(false);
	}

	/**
	 * set volume of system
	 */
	private void setVolume() {
		mAudioManager = (AudioManager) getActivity().getSystemService(
				Context.AUDIO_SERVICE);
		int maxVol = mAudioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int cutVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, cutVol, 0);

		volumeSeekBar.setMax(maxVol);
		volumeSeekBar.setProgress(cutVol);

		IntentFilter filter = new IntentFilter();
		filter.addAction("android.media.VOLUME_CHANGED_ACTION");
	}

	/**
	 * set selected language for display when user open setting
	 */
	private void setlanguage() {
		if (mApplicationConfigurations.getUserSelectedLanguage() == Constants.LANGUAGE_HINDI) {
			Utils.SetMultilanguageTextOnTextView(getActivity(),
					languagesummary,
					getResources().getString(R.string.lang_hindi_converted));
		} else if (mApplicationConfigurations.getUserSelectedLanguage() == Constants.LANGUAGE_TELUGU) {
			Utils.SetMultilanguageTextOnTextView(getActivity(),
					languagesummary,
					getResources().getString(R.string.lang_telugu_converted));
		} else if (mApplicationConfigurations.getUserSelectedLanguage() == Constants.LANGUAGE_TAMIL) {
			Utils.SetMultilanguageTextOnTextView(getActivity(),
					languagesummary,
					getResources().getString(R.string.lang_tamil_converted));
		} else if (mApplicationConfigurations.getUserSelectedLanguage() == Constants.LANGUAGE_PUNJABI) {
			Utils.SetMultilanguageTextOnTextView(getActivity(),
					languagesummary,
					getResources().getString(R.string.lang_punjabi_converted));
		} else {
			Utils.SetMultilanguageTextOnTextView(getActivity(),
					languagesummary,
					getResources().getString(R.string.lang_english));
		}
	}

	/**
	 * set selected audio quality bit rate for display when user open setting
	 */
	private void setBitrate() {
		if (getActivity() != null) {
			int bitrate = mApplicationConfigurations.getBitRateState();
			bitrateRadioButtonAuto.setChecked(false);
			bitrateRadioButtonHigh.setChecked(false);
			bitrateRadioButtonHD.setChecked(false);
			bitrateRadioButtonLow.setChecked(false);
			bitrateRadioButtonMedium.setChecked(false);

			if (bitrate == ApplicationConfigurations.BITRATE_AUTO) {
				bitrateRadioButtonAuto.setChecked(true);
				Utils.SetMultilanguageTextOnTextView(
						getActivity(),
						audio_summary,
						getResources().getString(
								R.string.settings_audio_quality_auto));

			} else if (bitrate == ApplicationConfigurations.BITRATE_HIGH) {
				bitrateRadioButtonHigh.setChecked(true);
				Utils.SetMultilanguageTextOnTextView(
						getActivity(),
						audio_summary,
						getResources().getString(
								R.string.settings_audio_quality_high));

			} else if (bitrate == ApplicationConfigurations.BITRATE_MEDIUM) {
				bitrateRadioButtonMedium.setChecked(true);
				Utils.SetMultilanguageTextOnTextView(
						getActivity(),
						audio_summary,
						getResources().getString(
								R.string.settings_audio_quality_medium));
			} else if (bitrate == ApplicationConfigurations.BITRATE_HD) {
				bitrateRadioButtonHD.setChecked(true);
				Utils.SetMultilanguageTextOnTextView(
						getActivity(),
						audio_summary,
						getResources().getString(
								R.string.settings_audio_quality_hd));
			} else {
				bitrateRadioButtonLow.setChecked(true);
				Utils.SetMultilanguageTextOnTextView(
						getActivity(),
						audio_summary,
						getResources().getString(
								R.string.settings_audio_quality_low));
			}
		}
	}

	/**
	 * set membership status for particular user
	 */
	private void setMembershipText() {
		try {
			mHasSubscriptionPlan = mApplicationConfigurations
					.isUserHasSubscriptionPlan();

			if (getActivity() != null) {
				if (mHasSubscriptionPlan) {
					if (mApplicationConfigurations
							.isUserHasTrialSubscriptionPlan()) {
						mSubscriptionPlan = Utils.getMultilanguageTextLayOut(
								getActivity(),
								getResources().getString(
										R.string.txt_hungama_pro))
								+ " - "
								+ Utils.getMultilanguageTextLayOut(
										getActivity(),
										getResources().getString(
												R.string.txt_free_trial));

						mSubscriptionPlan += "\n"
								+ getActivity()
										.getString(
												R.string.free_upgrade_trial_days_remaining,
												mApplicationConfigurations
														.getTrialSubscriptionDaysLeft());
						membershipArrow.setVisibility(View.GONE);
					} else {
						mSubscriptionPlan = Utils.getMultilanguageTextLayOut(
								getActivity(),
								getActivity().getString(
										R.string.premium_membership));
					}
				} else {

					mSubscriptionPlan = Utils.getMultilanguageTextLayOut(
							getActivity(),
							getResources().getString(R.string.txt_free_user))
							+ " - "
							+ Utils.getMultilanguageTextLayOut(
									getActivity(),
									getResources().getString(
											R.string.txt_get_hungama_pro));
				}
				// Add language specific text
				if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
					membershipTextView.setText(mSubscriptionPlan);
				} else {
					Utils.SetMultilanguageTextOnTextView(getActivity(),
							membershipTextView, mSubscriptionPlan);
				}
			}

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		if (isHideLoadingDialog) {
			hideLoadingDialog();
		}
		// hideLoadingDialog();
	}

	/**
	 * set user social account status for user has corrently login in app
	 */
	private void setSocialLoginStatus() {

		// Hungama login
		boolean isRealUser = mApplicationConfigurations.isRealUser();
		if (isRealUser) {
			hungamaLoginSign.setVisibility(View.VISIBLE);

			// FaceBook login
			if (mGigyaManager.isFBConnected()) {
				fbLoginSign.setVisibility(View.VISIBLE);
			} else {
				fbLoginSign.setVisibility(View.INVISIBLE);
			}

			// Twitter login
			if (mGigyaManager.isTwitterConnected()) {
				twitterLoginSign.setVisibility(View.VISIBLE);
			} else {
				twitterLoginSign.setVisibility(View.INVISIBLE);
			}

			// Google Plus login
			if (mGigyaManager.isGoogleConnected()) {
				googlLoginSign.setVisibility(View.VISIBLE);
			} else {
				googlLoginSign.setVisibility(View.INVISIBLE);
			}

		} else {
			hungamaLoginSign.setVisibility(View.INVISIBLE);
			fbLoginSign.setVisibility(View.INVISIBLE);
			mGigyaManager.setIsFBConnected(false);
			twitterLoginSign.setVisibility(View.INVISIBLE);
			mGigyaManager.setIsTwitterConnected(false);
			googlLoginSign.setVisibility(View.INVISIBLE);
			mGigyaManager.setIsGoogleConnected(false);
		}
	}

	/**
	 * start login screen if user has not logged in yet
	 */
	private void startLoginActivity() {
		Intent startLoginActivityIntent = new Intent(getActivity()
				.getApplicationContext(), LoginActivity.class);
		startLoginActivityIntent.putExtra(
				SettingsActivity.ARGUMENT_SETTINGS_ACTIVITY,
				"settings_activity");
		startLoginActivityIntent.putExtra(LoginActivity.FLURRY_SOURCE,
				FlurryConstants.FlurryUserStatus.Settings.toString());
		startActivityForResult(startLoginActivityIntent,
				SettingsActivity.LOGIN_ACTIVITY_CODE);
	}

	/**
	 * open member ship detail for user specific account else open login for
	 * same
	 */
	private void addMembershipDetailsFragment() {
		if (mApplicationConfigurations.isUserHasTrialSubscriptionPlan()) {
		} else if (mApplicationConfigurations.isUserHasSubscriptionPlan()) {
			FragmentManager mFragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit, R.anim.slide_right_enter,
					R.anim.slide_right_exit);

			MembershipDetailsFragment membershipDetailsFragment = new MembershipDetailsFragment();
			fragmentTransaction.replace(R.id.main_fragmant_container,
					membershipDetailsFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} else {
			// Flurry report: upgrade clicked
			Boolean loggedIn = mApplicationConfigurations.isRealUser();
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(
					FlurryConstants.FlurrySubscription.SourcePage.toString(),
					FlurryConstants.FlurrySubscription.Membership.toString());
			reportMap.put(
					FlurryConstants.FlurrySubscription.LoggedIn.toString(),
					loggedIn.toString());
			Analytics
					.logEvent(FlurryConstants.FlurrySubscription.TapsOnUpgrade
							.toString(), reportMap);

			Intent intent = new Intent(getActivity(), UpgradeActivity.class);
			startActivityForResult(intent, 0);
		}
	}

	/**
	 * open user social account setting. if is open only if user has logged in
	 * using social
	 * 
	 * @param provider
	 */
	private void addAccountSettingsFragment(SocialNetwork provider) {
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		// sets the given account as the argument.
		AccountSettingsFragment accountSettingsFragment = new AccountSettingsFragment();
//				getActivity());
		Bundle b = new Bundle();
		b.putSerializable(AccountSettingsFragment.PROVIDER, provider);
		accountSettingsFragment.setArguments(b);

		// bang!
		fragmentTransaction.replace(R.id.main_fragmant_container,
				accountSettingsFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	/**
	 * open stream setting
	 */
	private void addMyStreamSettingsFragment() {
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		MyStreamSettingsFragment myStreamSettingsFragment = new MyStreamSettingsFragment();
		fragmentTransaction.replace(R.id.main_fragmant_container,
				myStreamSettingsFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	/**
	 * open language setting dialogue
	 */
	private void addLanguageSettingsFragment() {
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		LanguageSettingsFragment languageSettingsFragment = new LanguageSettingsFragment();

		// Show DialogFragment
		languageSettingsFragment.show(mFragmentManager,
				"LanguageSettingsFragment");
	}

	/**
	 * show audio setting dialog
	 */
	private void audiodialog() {
		Logger.i("Dialog", "1");

		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(dialogParentView, getActivity());
		}
		dialog.show();
	}

	// ======================================================
	// Volume SeekBar callbacks.
	// ======================================================

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		try{
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
		}catch (Exception e){}

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	// ======================================================
	// Bitrate radio group callbacks.
	// ======================================================
	private boolean isHDSelected = false;// , isAutoSelected = false;
	public static boolean isAutoSelected = false;

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {

		switch (group.getId()) {
		case R.id.bitrateRadioGroup:
			if (checkedId == R.id.radio_button_auto) {
				mApplicationConfigurations
						.setBitRateState(ApplicationConfigurations.BITRATE_AUTO);

				audio_summary.setText(bitrateRadioButtonAuto.getText());
				dialog.dismiss();

			} else if (checkedId == R.id.radio_button_high) {
				mApplicationConfigurations
						.setBitRateState(ApplicationConfigurations.BITRATE_HIGH);

				audio_summary.setText(bitrateRadioButtonHigh.getText());
				dialog.dismiss();

			} else if (checkedId == R.id.radio_button_medium) {
				mApplicationConfigurations
						.setBitRateState(ApplicationConfigurations.BITRATE_MEDIUM);

				audio_summary.setText(bitrateRadioButtonMedium.getText());
				dialog.dismiss();

			} else if (checkedId == R.id.radio_button_hd) {
				if (isHDSelected || isAutoSelected) {
					if (isAutoSelected)
						setBitrate();
					isAutoSelected = false;
					dialog.dismiss();
					return;
				}
				if (!CacheManager.isProUser(getActivity())) {
					isHDSelected = true;
					if (getActivity() != null) {
						Intent intent = new Intent(getActivity(),
								UpgradeActivity.class);
						intent.putExtra(UpgradeActivity.IS_TRIAL_PLANS, true);
						startActivityForResult(intent,
								HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
					}

				} else {
					mApplicationConfigurations
							.setBitRateState(ApplicationConfigurations.BITRATE_HD);
					audio_summary.setText(bitrateRadioButtonHD.getText());
					dialog.dismiss();
				}
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(
						FlurryConstants.FlurryCaching.UserStatus.toString(),
						Utils.getUserState(getActivity()));
				Analytics.logEvent(
						FlurryConstants.FlurrySubscription.TapsOnHDAudioQuality
								.toString(), reportMap);
			} else {
				mApplicationConfigurations
						.setBitRateState(ApplicationConfigurations.BITRATE_LOW);
				audio_summary.setText(bitrateRadioButtonLow.getText());
				dialog.dismiss();
			}
			break;
		default:
			break;
		}
	}

	// ======================================================
	// Gigya callbacks.
	// ======================================================
	private SocialNetwork provider;

	@Override
	public void onGigyaLoginListener(SocialNetwork provider,
			Map<String, Object> signupFields, long setId) {
		try {
			this.provider = provider;

			if (provider == SocialNetwork.TWITTER) {
				// Twitter
				FragmentManager mFragmentManager = getFragmentManager();
				FragmentTransaction fragmentTransaction = mFragmentManager
						.beginTransaction();
				fragmentTransaction.setCustomAnimations(
						R.anim.slide_left_enter, R.anim.slide_left_exit,
						R.anim.slide_right_enter, R.anim.slide_right_exit);

				TwitterLoginFragment fragment = new TwitterLoginFragment();
                fragment.init(signupFields, setId);
				fragmentTransaction.replace(R.id.main_fragmant_container,
						fragment, TwitterLoginFragment.FRAGMENT_TWITTER_LOGIN);
				fragmentTransaction.addToBackStack(TwitterLoginFragment.class
						.toString());
				fragmentTransaction.commit();

				// Listen to result from TwitterLoinFragment
				fragment.setOnTwitterLoginListener(this);

			} else {
				// FaceBook, Google

				// Call PCP
				if((provider== SocialNetwork.FACEBOOK && TextUtils.isEmpty(mApplicationConfigurations.getGigyaFBEmail()))
						|| (provider== SocialNetwork.GOOGLE && TextUtils.isEmpty(mApplicationConfigurations.getGigyaGoogleEmail()))){
					mGigyaManager.removeConnetion(provider);
					Toast.makeText(getActivity(), R.string.gigya_login_error_email_required, Toast.LENGTH_SHORT).show();
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
		}
	}

	@Override
	public void onSocializeGetUserInfoListener() {

		setSocialLoginStatus();
		hideLoadingDialog();
	}

	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {
	}

	@Override
	public void onSocializeGetContactsListener(
			List<GoogleFriend> googleFriendsList) {
	}

	@Override
	public void onGigyaLogoutListener() {
	}

	@Override
	public void onTwitterLoginListener(TwitterLoginFragment fragment,
			Map<String, Object> signupFields, long setId) {
		provider = SocialNetwork.TWITTER;
		// Call PCP
		// It's include the email and password that user insert in
		// TwitterLoginFragment
		mDataManager.createPartnerConsumerProxy(signupFields, setId, this,
				false);
		mTwitterLoginFragment = fragment;
		// fragment.getFragmentManager().popBackStack();
	}

	@Override
	public void onCancelLoginListener() {
		provider = null;
		mGigyaManager.removeConnetion(SocialNetwork.TWITTER);
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
	}

	/**
	 * open save offline setting for only pro user
	 */
	public void addSaveOfflineSettingsFragment() {
		try {
			FragmentManager mFragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit, R.anim.slide_right_enter,
					R.anim.slide_right_exit);

			SaveOfflineSettingsFragment saveOfflineSettingsFragment = new SaveOfflineSettingsFragment();
			fragmentTransaction.replace(R.id.main_fragmant_container,
					saveOfflineSettingsFragment);
			fragmentTransaction.addToBackStack(null);
			fragmentTransaction.commit();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
		if (mSettingsContentObserver != null)
			getActivity().getContentResolver().unregisterContentObserver(
					mSettingsContentObserver);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener
	 * #onCancelRequestListener()
	 */
	@Override
	public void onCancelRequestListener() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onConnectionRemoved() {

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		switch (buttonView.getId()) {
		case R.id.switch_lyrics:
			mApplicationConfigurations.setLyricsShow(isChecked);
			break;
		case R.id.switch_trivia:
			mApplicationConfigurations.setTriviaShow(isChecked);
			break;
		case R.id.switch_offline:
			mApplicationConfigurations
					.setSaveOfflineAutoModeRemember(isChecked);
			break;
		default:
			break;
		}
	}

	public class SettingsContentObserver extends ContentObserver {
		int previousVolume;
		Context context;

		public SettingsContentObserver(Context c, Handler handler) {
			super(handler);
			context = c;

			AudioManager audio = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			previousVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return super.deliverSelfNotifications();
		}

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);

			AudioManager audio = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			int currentVolume = audio
					.getStreamVolume(AudioManager.STREAM_MUSIC);

			if(volumeSeekBar!=null)
				volumeSeekBar.setProgress(currentVolume);
		}
	}
}
