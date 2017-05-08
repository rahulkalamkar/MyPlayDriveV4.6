package com.hungama.hungamamusic.lite.carmode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout.DialogType;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout.IDialogListener;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.data.dao.hungama.UserProfileResponse;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerInfoReadOperation;
import com.hungama.myplay.activity.operations.hungama.GetUserProfileOperation;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DisconnectFragment extends Fragment implements CommunicationOperationListener, OnGigyaResponseListener {
	public final static String TAG = DisconnectFragment.class.getSimpleName();

	// UI Elements
	private Button btnLogin;
	private RelativeLayout rlLogin;
	private LinearLayout llLogout;
	private CustomDialogLayout mDialog;
	private EditText etUserName;
	private EditText etPassword;

	private boolean bLoggedIn;
	private DataManager mDataManager;
	private List<SignOption> mSignOptions;
	private GigyaManager mGigyaManager;

	public static DisconnectFragment newInstance() {
		final DisconnectFragment fragment = new DisconnectFragment();

		return fragment;
	}

	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.carmode_fragment_disconnection, container, false);
		btnLogin = (Button) rootView.findViewById(R.id.btn_login);
		rlLogin = (RelativeLayout) rootView.findViewById(R.id.rl_login);
		llLogout = (LinearLayout) rootView.findViewById(R.id.ll_logout);
		etUserName = (EditText) rootView.findViewById(R.id.et_username);
		etPassword = (EditText) rootView.findViewById(R.id.et_password);

		return rootView;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity());
		bLoggedIn = mDataManager.getApplicationConfigurations().isuserLoggedIn();
		if (bLoggedIn) {
			mGigyaManager = new GigyaManager(getActivity());
			mGigyaManager.setOnGigyaResponseListener(this);

			btnLogin.setText(R.string.title_log_out);
			rlLogin.setVisibility(View.GONE);
			llLogout.setVisibility(View.VISIBLE);

			etUserName.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						etPassword.requestFocus();
						return true;
					}

					return false;
				}
			});

			etPassword.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if ((event.getAction() == KeyEvent.ACTION_UP) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
						// Perform silent login.
						mDataManager.readPartnerInfo(DisconnectFragment.this);
						return true;
					}
					return false;
				}
			});
		} else {
			btnLogin.setText(R.string.title_log_in);
			rlLogin.setVisibility(View.VISIBLE);
			llLogout.setVisibility(View.GONE);
		}
	}

	public void vHandleLoginClicks(View v) {
		switch (v.getId()) {
		case R.id.btn_login:

			if (bLoggedIn) { // Log out
				// Log out from Gigya
				mGigyaManager.logout();

				final ApplicationConfigurations appConfig = mDataManager.getApplicationConfigurations();
				appConfig.setSessionID(null);
				appConfig.setPartnerUserId(appConfig.getSkippedPartnerUserId());
				appConfig.setGigyaSessionSecret(null);
				appConfig.setGigyaSessionToken(null);
				appConfig.setHungamaFirstName("");
				appConfig.setHungamaLastName("");
				appConfig.setHungamaEmail("");
				appConfig.setSeletedPreferences("");
				appConfig.setSeletedPreferencesVideo("");

				Set<String> tags = Utils.getTags();
				try {
					if (!tags.contains("not-logged-in")) {
						if (tags.contains("logged-in"))
							tags.remove("logged-in");
						tags.add("not-logged-in");
						Utils.AddTag(tags);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				try {
					String tagToAdd = "free-user";
					String tagToRemove = "paid-user";
					if (!tags.contains(tagToAdd)) {
						if (tags.contains(tagToRemove))
							tags.remove(tagToRemove);
						tags.add(tagToAdd);
						Utils.AddTag(tags);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				try {
					if (tags.contains("Trial_expired"))
						tags.remove("Trial_expired");
					if (tags.contains("Trial"))
						tags.remove("Trial");
					Utils.AddTag(tags);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				// Delete all locale playlists on device
				mDataManager.storePlaylists(new HashMap<Long, Playlist>(), null);

				// Perform silent login.
				mDataManager.readPartnerInfo(this);
			} else {

				boolean isValid = true;
				String msg = "";

				if (etUserName.getEditableText().toString().isEmpty()) {
					msg = "Username is empty";
					isValid = false;
				} else if (etPassword.getEditableText().toString().isEmpty()) {
					msg = "Password is empty";
					isValid = false;
				}

				if (isValid) {
					// Perform silent login.
					mDataManager.readPartnerInfo(this);
				} else {
					GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, msg, null);
				}
			}

			break;

		default:
			break;
		}
	}

	@Override
	public void onStart(int operationId) {
		final String msg;

		if (bLoggedIn) {
			msg = getResources().getString(R.string.msg_logout);
		} else {
			msg = getResources().getString(R.string.msg_login_account);
		}

		mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, msg, null);

		switch (operationId) {
		case (OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE):
			Log.i(TAG, "Starting to get device ID.");
			break;

		case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
			Log.i(TAG, "Starting get partner info - sign up / in options.");
			break;
		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			Log.i(TAG, "Starting getting activation code.");
			break;
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {

		Set<String> tags;

		switch (operationId) {
		//		case (OperationDefinition.CatchMedia.OperationId.TIME_READ):
		//			if (TextUtils.isEmpty(deviceId)) {
		//				mDataManager.createDevice(this);
		//			} else {
		//				mDataManager.readPartnerInfo(this);
		//			}
		//			break;

		case (OperationDefinition.CatchMedia.OperationId.DEVICE_CEREATE):
			Log.i(TAG, "Successed getting device ID.");
			// get partener's info - sign up options.
			mDataManager.readPartnerInfo(this);
			break;

		case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
			Log.i(TAG, "Successed getting partner info.");
			mSignOptions = (List<SignOption>) responseObjects.get(PartnerInfoReadOperation.RESPONSE_KEY_OBJECT_SIGN_OPTIONS);

			// Set the Gigya setID
			SignOption gigyaSignup = mSignOptions.get(2);
			mDataManager.getApplicationConfigurations().setGigyaSignup(gigyaSignup);
			mGigyaManager = new GigyaManager(getActivity());
			mGigyaManager.setOnGigyaResponseListener(this);

			if (bLoggedIn) {
				silentLogin();
			} else {
				accountLogin();
			}

			break;

		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
			if (bLoggedIn) {
				resetUserDetails();
			}

			// let's party!
			// get user language & set hashmap
			String activationCode = (String) responseObjects.get(mDataManager.getApplicationConfigurations().ACTIVATION_CODE);
			mDataManager.createDeviceActivationLogin(activationCode, this);
			mDataManager.getApplicationConfigurations().setIsUserRegistered(true);
			mDataManager.getUserProfileDetail(this);

			tags = Utils.getTags();
			if (!tags.contains("registered_user")) {
				if (tags.contains("non_registered_user"))
					tags.remove("non_registered_user");

				tags.add("registered_user");
				Utils.AddTag(tags);
			}
			break;

		case OperationDefinition.Hungama.OperationId.GET_USER_PROFILE:
			UserProfileResponse userProfileResponse = (UserProfileResponse) responseObjects.get(GetUserProfileOperation.RESPONSE_KEY_USER_DETAIL);
			if (userProfileResponse != null && userProfileResponse.getCode() == 200) {
				mDataManager.getApplicationConfigurations().setHungamaEmail(userProfileResponse.getUsername());
				mDataManager.getApplicationConfigurations().setHungamaFirstName(userProfileResponse.getFirst_name());
				mDataManager.getApplicationConfigurations().setHungamaLastName(userProfileResponse.getLast_name());
			}
			break;
		case (OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET):

			final String tagToRemove;
			final int oldLanguageSelected = mDataManager.getApplicationConfigurations().getUserSelectedLanguage();
			switch (oldLanguageSelected) {
			case Constants.LANGUAGE_HINDI:
				tagToRemove = "pref_display_" + getResources().getString(R.string.lang_hindi);
				break;
			case Constants.LANGUAGE_TAMIL:
				tagToRemove = "pref_display_" + getResources().getString(R.string.lang_tamil);
				break;
			case Constants.LANGUAGE_TELUGU:
				tagToRemove = "pref_display_" + getResources().getString(R.string.lang_telugu);
				break;
			case Constants.LANGUAGE_PUNJABI:
				tagToRemove = "pref_display_" + getResources().getString(R.string.lang_punjabi);
				break;
			case Constants.LANGUAGE_ENGLISH:
			default:
				tagToRemove = "pref_display_" + getResources().getString(R.string.lang_english);
				break;
			}

			// End of processing Log In/Out.
			if (bLoggedIn) {
				btnLogin.setText(R.string.title_log_in);
				rlLogin.setVisibility(View.VISIBLE);
				llLogout.setVisibility(View.GONE);
			} else {
				btnLogin.setText(R.string.title_log_out);
				rlLogin.setVisibility(View.GONE);
				llLogout.setVisibility(View.VISIBLE);
			}
			bLoggedIn = !bLoggedIn;
			mDialog.hide();

			break;
		case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
			tags = Utils.getTags();
			if (mDataManager.getApplicationConfigurations().isRealUser()) {
				if (!tags.contains("logged-in")) {
					if (tags.contains("not-logged-in"))
						tags.remove("not-logged-in");

					tags.add("logged-in");
					Utils.AddTag(tags);
				}
				try {
					mDataManager.getCurrentSubscriptionPlan(this, Utils.getAccountName(getActivity()));
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
			}

			Map<String, Object> responseMap = (Map<String, Object>) responseObjects.get(CMOperation.RESPONSE_KEY_GENERAL_OBJECT);
			// stores the session and other crucial properties.
			String sessionID = (String) responseMap.get(ApplicationConfigurations.SESSION_ID);
			int householdID = ((Long) responseMap.get(ApplicationConfigurations.HOUSEHOLD_ID)).intValue();
			int consumerID = ((Long) responseMap.get(ApplicationConfigurations.CONSUMER_ID)).intValue();
			String passkey = (String) responseMap.get(ApplicationConfigurations.PASSKEY);

			final ApplicationConfigurations appConfigs = mDataManager.getApplicationConfigurations();
			appConfigs.setSessionID(sessionID);
			appConfigs.setHouseholdID(householdID);
			appConfigs.setConsumerID(consumerID);
			appConfigs.setPasskey(passkey);

			String secret = mDataManager.getApplicationConfigurations().getGigyaSessionSecret();
			String token = mDataManager.getApplicationConfigurations().getGigyaSessionToken();

			if (!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)) {
				if (mGigyaManager == null)
					mGigyaManager = new GigyaManager(getActivity());
				mGigyaManager.setSession(token, secret);
			}

			mDataManager.GetUserLanguageMap(this);
			break;
		}

	}

	@Override
	public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
		mDialog.hide();

		mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, errorMessage, new IDialogListener() {

			@Override
			public void onPositiveBtnClick() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNegativeBtnClick() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onNegativeBtnClick(CustomDialogLayout layout) {

			}

			@Override
			public void onMessageAction() {
				getFragmentManager().popBackStack();
			}
		});
	}

	private void silentLogin() {
		// gets the default values of the log
		try {
			SignOption signOption = mSignOptions.get(3);// Utils.getSignOption(mSignOptions,
														// SignOption.SET_ID_SILENT_LOGIN);//

			Map<String, Object> signupFields = new HashMap<String, Object>();

			SignupField phoneNumberFields = signOption.getSignupFields().get(0);
			SignupField hardwareIDFields = signOption.getSignupFields().get(1);

			// adds the device's phone number if available.
			String phoneNumber = mDataManager.getDeviceConfigurations().getDevicePhoneNumber();
			Logger.d(TAG, "device phone number: " + phoneNumber);
			if (!TextUtils.isEmpty(phoneNumber)) {
				Map<String, Object> phoneNumberMap = new HashMap<String, Object>();
				phoneNumberMap.put(SignupField.VALUE, phoneNumber);
				signupFields.put(phoneNumberFields.getName(), phoneNumberMap);
			}

			// adds the device's hardware id if available.
			Map<String, Object> hardwareIDMap = new HashMap<String, Object>();
			hardwareIDMap.put(SignupField.VALUE, mDataManager.getDeviceConfigurations().getHardwareId());
			signupFields.put(hardwareIDFields.getName(), hardwareIDMap);

			mDataManager.createPartnerConsumerProxy(signupFields, signOption.getSetID(), this, true);
		} catch (Exception e) {
			Logger.e(TAG + ":505", e.toString());
		}
	}

	private void accountLogin() {
		Map<String, Object> signupFieldsMap = new HashMap<String, Object>();

		// generate as a field to upload.
		Map<String, Object> fieldMapEmail = new HashMap<String, Object>();
		//		fieldMapEmail.put(SignupField.VALUE, "nagasanthosh.kalicheti@in.bosch.com");
		fieldMapEmail.put(SignupField.VALUE, etUserName.getEditableText().toString());
		signupFieldsMap.put("email_mobile", fieldMapEmail);

		Map<String, Object> fieldMapPass = new HashMap<String, Object>();
		//		fieldMapPass.put(SignupField.VALUE, "XKJA");
		fieldMapPass.put(SignupField.VALUE, etPassword.getEditableText().toString());
		signupFieldsMap.put("password", fieldMapPass);

		mDataManager.createPartnerConsumerProxy(signupFieldsMap, mSignOptions.get(0).getSetID(), this, false);

	}

	@Override
	public void onGigyaLoginListener(SocialNetwork provider, Map<String, Object> signupFields, long setId) {
		mDataManager.createPartnerConsumerProxy(signupFields, setId, this, false);
	}

	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSocializeGetContactsListener(List<GoogleFriend> googleFriendsList) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSocializeGetUserInfoListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onGigyaLogoutListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFacebookInvite() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTwitterInvite() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFailSocialGetFriendsContactsListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCancelRequestListener() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnectionRemoved() {

	}

	private void resetUserDetails() {
		final ApplicationConfigurations appConfigs = mDataManager.getApplicationConfigurations();
		String skippedPartnerUserId = appConfigs.getSkippedPartnerUserId();
		if (!TextUtils.isEmpty(skippedPartnerUserId)) {
			appConfigs.setPartnerUserId(skippedPartnerUserId);
		}

		appConfigs.setIsRealUser(false);
		appConfigs.setIsUserHasSubscriptionPlan(false);
		appConfigs.setIsUserHasTrialSubscriptionPlan(false);
		appConfigs.setTrialExpiryDaysLeft(0);
		appConfigs.setGigyaSessionSecret("");
		appConfigs.setGigyaSessionToken("");
		appConfigs.setSeletedPreferences("");
		appConfigs.setSeletedPreferencesVideo("");

		mDataManager.deleteCurrentSubscriptionPlan();
		appConfigs.setIsUserHasSubscriptionPlan(false);
	}
}
