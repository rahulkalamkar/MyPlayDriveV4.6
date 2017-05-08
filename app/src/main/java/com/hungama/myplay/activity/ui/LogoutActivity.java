package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.hungama.ShareSettingsResponse;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.gigya.FBFriend;
import com.hungama.myplay.activity.gigya.GigyaManager;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.GoogleFriend;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerInfoReadOperation;
import com.hungama.myplay.activity.operations.hungama.ShareSettingsOperation;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.fragments.AccountSettingsFragment.ObjLanguagePackage;
import com.hungama.myplay.activity.ui.fragments.SettingFragmentNew;
import com.hungama.myplay.activity.ui.widgets.CustomDialog;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LogoutActivity extends Activity implements OnClickListener,
		CommunicationOperationListener, OnGigyaResponseListener {

	private static final String TAG = "LogoutActivity";

	// Managers
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private GigyaManager mGigyaManager;

	// Data members
	private SocialNetwork providerType;

	// public static final String PROVIDER = "provider";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(this);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		mGigyaManager = new GigyaManager(this);
		mGigyaManager.setOnGigyaResponseListener(this);

		// providerType = (SocialNetwork) getIntent().getSerializableExtra(
		// PROVIDER);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mApplicationConfigurations.isRealUser()) {
			providerType = null;
		} else if (mGigyaManager.isFBConnected()) {
			providerType = SocialNetwork.FACEBOOK;
		} else if (mGigyaManager.isTwitterConnected()) {
			providerType = SocialNetwork.TWITTER;
		} else if (mGigyaManager.isGoogleConnected()) {
			providerType = SocialNetwork.GOOGLE;
		}
		showLogoutDialog();
	}

	public void showLogoutDialog() {
		CustomDialog alertDialogBuilder = new CustomDialog(this);

		// // set title
		alertDialogBuilder.setTitle(Utils.getMultilanguageText(this,
				getResources().getString(R.string.logout_dialog_title)));

		// set dialog message
		alertDialogBuilder.setMessage(Utils.getMultilanguageText(this,
				getResources().getString(R.string.logout_dialog_text)));
		alertDialogBuilder.setCancelable(true);
		alertDialogBuilder.setPositiveButton(Utils.getMultilanguageText(this,
				getString(R.string.exit_dialog_text_yes)),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						try {
							SettingFragmentNew.isAutoSelected = true;
							showLoadingDialog(getString(R.string.application_dialog_loading));
							// Log out from Gigya
							mGigyaManager.logout();

							//
							mApplicationConfigurations.setSessionID(null);

							mApplicationConfigurations
									.setPartnerUserId(mApplicationConfigurations
											.getSkippedPartnerUserId());
							mApplicationConfigurations
									.setGigyaSessionSecret(null);
							mApplicationConfigurations
									.setGigyaSessionToken(null);

							mApplicationConfigurations.setHungamaFirstName("");
							mApplicationConfigurations.setHungamaLastName("");
							mApplicationConfigurations.setHungamaEmail("");
							mApplicationConfigurations
									.setSeletedPreferences("");
							mApplicationConfigurations
									.setSeletedPreferencesVideo("");

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

							try {
								// Delete Campaigns
								CampaignsManager mCampaignsManager = CampaignsManager
										.getInstance(LogoutActivity.this);
								mCampaignsManager.clearCampaigns();
								CampaignsManager
										.setWeightsMap(new HashMap<Float, Placement>());

								// Delete all locale playlists on device
								DataManager mDataManager = DataManager
										.getInstance(LogoutActivity.this);
								Map<Long, Playlist> empty = new HashMap<Long, Playlist>();
								mDataManager.storePlaylists(empty, null);
							} catch (Exception e) {
								Logger.printStackTrace(e);
							}
							performSilentLogin();
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}
				});
		alertDialogBuilder.setNegativeButton(Utils.getMultilanguageText(this,
				getString(R.string.exit_dialog_text_no)),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close
						// the dialog box and do nothing
						dialog.cancel();
						finish();
					}
				});
		alertDialogBuilder
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface arg0) {
						finish();
					}
				});

		// create alert dialog
		// AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialogBuilder.show();
	}

	private void performSilentLogin() {
        if (TextUtils.isEmpty(mApplicationConfigurations.getSilentPartnerUserId())
                || TextUtils.isEmpty(mApplicationConfigurations.getSilentUserSessionID())
                || TextUtils.isEmpty(mApplicationConfigurations.getSilentUserPasskey())) {
            mDataManager.readPartnerInfo(this);
        } else {
            resetUserDetails();

            boolean reloadApp = false;
            mApplicationConfigurations.setUserLoginPhoneNumber(mApplicationConfigurations.getSilentUserLoginPhoneNumber());
            mApplicationConfigurations.setPartnerUserId(mApplicationConfigurations.getSilentPartnerUserId());
            mApplicationConfigurations
                    .setGigyaSessionToken(mApplicationConfigurations
                            .getSilentUserGigyaSessionToken());
            mApplicationConfigurations
                    .setGigyaSessionSecret(mApplicationConfigurations
                            .getSilentUserGigyaSessionSecret());
            mApplicationConfigurations.setSessionID(mApplicationConfigurations.getSilentUserSessionID());
            mApplicationConfigurations.setHouseholdID(mApplicationConfigurations.getSilentUserHouseholdID());
            mApplicationConfigurations.setConsumerID(mApplicationConfigurations.getSilentUserConsumerID());
            mApplicationConfigurations.setPasskey(mApplicationConfigurations.getSilentUserPasskey());
            mApplicationConfigurations.setConsumerRevision(0);
            mApplicationConfigurations.setHouseholdRevision(0);
            mApplicationConfigurations.setUserSelectedLanguageText(mApplicationConfigurations.getSilentUserSelectedLanguageText());

            if (!mApplicationConfigurations.getSelctedMusicPreference().equalsIgnoreCase(mApplicationConfigurations.getSilentUserSelctedMusicPreference())
                    || !mApplicationConfigurations.getSelctedMusicGenre().equalsIgnoreCase(mApplicationConfigurations.getSilentUserSelctedMusicGenre()))
                reloadApp = true;
            mApplicationConfigurations.setSelctedMusicPreference(mApplicationConfigurations.getSilentUserSelctedMusicPreference());
            mApplicationConfigurations.setSelctedMusicGenre(mApplicationConfigurations.getSilentUserSelctedMusicGenre());
            mApplicationConfigurations.setIsRealUser(false);

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

			if(Logger.allowPlanForSilentUser) {
				String accountType = Utils.getAccountName(this);
				mDataManager.getCurrentSubscriptionPlan(this, accountType);
			}
//        mDataManager.GetUserLanguageMap(this);
            try {
                if (mApplicationConfigurations.getUserSelectedLanguage() != mApplicationConfigurations.getSilentUserSelectedLanguage()) {
                    final String tagToRemove;
                    switch (mApplicationConfigurations.getUserSelectedLanguage()) {
                        case Constants.LANGUAGE_HINDI:
                            tagToRemove = "pref_display_"
                                    + getResources().getString(R.string.lang_hindi);
                            break;
                        case Constants.LANGUAGE_TAMIL:
                            tagToRemove = "pref_display_"
                                    + getResources().getString(R.string.lang_tamil);
                            break;
                        case Constants.LANGUAGE_TELUGU:
                            tagToRemove = "pref_display_"
                                    + getResources().getString(R.string.lang_telugu);
                            break;
                        case Constants.LANGUAGE_PUNJABI:
                            tagToRemove = "pref_display_"
                                    + getResources().getString(R.string.lang_punjabi);
                            break;
                        case Constants.LANGUAGE_ENGLISH:
                        default:
                            tagToRemove = "pref_display_"
                                    + getResources().getString(R.string.lang_english);
                            break;
                    }
                    mApplicationConfigurations
                            .setUserSelectedLanguage(mApplicationConfigurations.getSilentUserSelectedLanguage());
                    Utils.changeLanguage(this, mApplicationConfigurations.getSilentUserSelectedLanguageText());
                    String tagToAdd = "pref_display_English";
                    Set<String> tags = Utils.getTags();
                    if (!tags.contains(tagToAdd)) {
                        if (tags.contains(tagToRemove))
                            tags.remove(tagToRemove);
                        tags.add(tagToAdd);
                        Utils.AddTag(tags);
                    }
                    reloadApp = true;
                }
                if(reloadApp)
                    sendBroadcast(new Intent(
                            MainActivity.ACTION_LANGUAGE_CHANGED));
                else{
                    Intent new_intent = new Intent();
                    new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
                    sendBroadcast(new_intent);
                }
                finish();
            } catch (Exception e1) {
                Logger.printStackTrace(e1);
            }
        }
	}

	private MyProgressDialog mProgressDialog;

	public void showLoadingDialog(String message) {
		if (!isFinishing()) {
			if (mProgressDialog == null) {
				// mProgressDialog = new ProgressDialog(this);
				// mProgressDialog = ProgressDialog.show(this, "", Utils
				// .getMultilanguageTextHindi(getApplicationContext(),
				// message), true);
				mProgressDialog = new MyProgressDialog(this);
				mProgressDialog.setCancelable(true);
				mProgressDialog.setCanceledOnTouchOutside(false);
			}
		}
	}

	public void hideLoadingDialogNew() {
		try{
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
				mProgressDialog = null;
			}
		}catch (Exception e){
		}
	}

	@Override
	public void onGigyaLoginListener(SocialNetwork provider,
			Map<String, Object> signupFields, long setId) {

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

	@Override
	public void onFailSocialGetFriendsContactsListener() {

	}

	@Override
	public void onCancelRequestListener() {

	}

	@Override
	public void onConnectionRemoved() {

	}

	@Override
	public void onStart(int operationId) {
		showLoadingDialog(getString(R.string.application_dialog_loading));
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case (OperationDefinition.Hungama.OperationId.SHARE_SETTINGS):

				ShareSettingsResponse response = (ShareSettingsResponse) responseObjects
						.get(ShareSettingsOperation.RESULT_KEY_SHARE_SETTINGS);

				// Build the map for the adapter
				// settingsMap = new HashMap<String, Integer>();
				//
				// if (getActivity() != null) {
				// String[] keys = getResources().getStringArray(
				// R.array.facebook_sharing_properties);
				//
				// settingsMap.put(keys[0], response.data.songs_listen);
				// settingsMap.put(keys[1], response.data.my_favorites);
				// settingsMap.put(keys[2], response.data.songs_download);
				// settingsMap.put(keys[3], response.data.my_comments);
				// settingsMap.put(keys[4], response.data.my_badges);
				// settingsMap.put(keys[5], response.data.videos_watched);
				// settingsMap.put(keys[6], response.data.videos_download);
				//
				// List<String> propList = new ArrayList<String>();
				//
				// propList = Arrays.asList(keys);
				//
				// adapter = new SettingsAdapter(getActivity(), propList,
				// settingsMap, this, mDataManager);
				//
				// TextView headerView = (TextView) ((LayoutInflater)
				// getActivity()
				// .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
				// .inflate(R.layout.settings_title_row_layout, null,
				// false);
				//
				// headerView.setText(Utils.getMultilanguageTextLayOut(
				// mContext, getString(R.string.sharing_settings)));
				//
				// settingsListView.addHeaderView(headerView);
				//
				// settingsListView.setAdapter(adapter);
				// }
				//
				hideLoadingDialogNew();

				break;
			case (OperationDefinition.Hungama.OperationId.SHARE_SETTINGS_UPDATE):

				// hideLoadingDialog();
				hideLoadingDialogNew();

				break;

			case (OperationDefinition.CatchMedia.OperationId.PARTNER_INFO_READ):
				Logger.i(TAG, "Successed getting partners info.");

				List<SignOption> signOptions = (List<SignOption>) responseObjects
						.get(PartnerInfoReadOperation.RESPONSE_KEY_OBJECT_SIGN_OPTIONS);

				// Set the Gigya setID
				SignOption gigyaSignup = signOptions.get(2);// Utils.getSignOption(signOptions,
															// SignOption.SET_ID_GIGYA_LOGIN);//
				mApplicationConfigurations.setGigyaSignup(gigyaSignup);

				/*
				 * performs silent login.
				 */
				SignOption signOption = signOptions.get(3);// Utils.getSignOption(signOptions,
															// SignOption.SET_ID_SILENT_LOGIN);//

				Map<String, Object> signupFields = new HashMap<String, Object>();
				SignupField phoneNumberFields = signOption.getSignupFields()
						.get(0);
				SignupField hardwareIDFields = signOption.getSignupFields()
						.get(1);

				DeviceConfigurations deviceConfigurations = mDataManager
						.getDeviceConfigurations();
				// adds the device's phone number if available.
				String phoneNumber = deviceConfigurations
						.getDevicePhoneNumber();
				Logger.d(TAG, "device phone number: " + phoneNumber);
				if (!TextUtils.isEmpty(phoneNumber)) {
					Map<String, Object> phoneNumberMap = new HashMap<String, Object>();
					phoneNumberMap.put(SignupField.VALUE, phoneNumber);
					signupFields.put(phoneNumberFields.getName(),
							phoneNumberMap);
				}
				// adds the device's hardware id if available.
				Map<String, Object> hardwareIDMap = new HashMap<String, Object>();
				hardwareIDMap.put(SignupField.VALUE,
						deviceConfigurations.getHardwareId());
				signupFields.put(hardwareIDFields.getName(), hardwareIDMap);

				hideLoadingDialogNew();

				mDataManager.createPartnerConsumerProxy(signupFields,
						signOption.getSetID(), this, true);

				break;

			case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):

				resetUserDetails();

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
					value = (String) fieldMap.get(SignupField.VALUE);
				}
				mApplicationConfigurations.setUserLoginPhoneNumber(value);

				// stores partner user id to connect with Hungama REST API.
				mApplicationConfigurations.setPartnerUserId(partnerUserId);
				// mApplicationConfigurations.setIsRealUser(isRealUser);

				mDataManager.createDeviceActivationLogin(activationCode, this);
				MainActivity.isUserPreferenceLoaded = false;

				break;

			case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
				try {
					Map<String, Object> responseMap = (Map<String, Object>) responseObjects
							.get(CMOperation.RESPONSE_KEY_GENERAL_OBJECT);
					// stores the session and other crucial properties.
					String sessionID = (String) responseMap
							.get(ApplicationConfigurations.SESSION_ID);
					int householdID = ((Long) responseMap
							.get(ApplicationConfigurations.HOUSEHOLD_ID))
							.intValue();
					int consumerID = ((Long) responseMap
							.get(ApplicationConfigurations.CONSUMER_ID))
							.intValue();
					String passkey = (String) responseMap
							.get(ApplicationConfigurations.PASSKEY);

					mApplicationConfigurations.setSessionID(sessionID);
					mApplicationConfigurations.setHouseholdID(householdID);
					mApplicationConfigurations.setConsumerID(consumerID);
					mApplicationConfigurations.setPasskey(passkey);

					// CampaignsManager mCampaignsManager = CampaignsManager
					// .getInstance(this);
					// mCampaignsManager.clearCampaigns();
					// mCampaignsManager.getCampignsList();

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

					mDataManager.GetUserLanguageMap(this);
				} catch (Exception e) {
					Logger.e(getClass().getName() + ":400", e.toString());
				}

				break;
            case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):

                resetUserDetails();

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
                    value = (String) fieldMap.get(SignupField.VALUE);
                }
                mApplicationConfigurations.setUserLoginPhoneNumber(value);

                // stores partner user id to connect with Hungama REST API.
//                mApplicationConfigurations.setPartnerUserId(partnerUserId);
                // mApplicationConfigurations.setIsRealUser(isRealUser);

//                mDataManager.createDeviceActivationLogin(activationCode, this);
                MainActivity.isUserPreferenceLoaded = false;
                try {
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

					if(Logger.allowPlanForSilentUser) {
						String accountType = Utils.getAccountName(this);
						mDataManager.getCurrentSubscriptionPlan(this, accountType);
					}

                    mDataManager.GetUserLanguageMap(this);
                } catch (Exception e) {
                    Logger.e(getClass().getName() + ":400", e.toString());
                }

                break;
			case (OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET):
				final String tagToRemove;
//				switch (mApplicationConfigurations.getUserSelectedLanguage()) {
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
					boolean isLanguageChanged = false;
					ObjLanguagePackage lp = new ObjLanguagePackage(
							responseObjects.toString());
					if (!lp.getLanguage().equals(mApplicationConfigurations.getUserSelectedLanguageText())) {
						isLanguageChanged = true;
						Utils.makeText(
								this,
								getString(R.string.your_langugae_has_been_changed),
								Toast.LENGTH_LONG).show();
					}
					mApplicationConfigurations.setUserSelectedLanguageText(lp
							.getLanguage());
					mApplicationConfigurations
							.setUserSelectedLanguage(lp.getId());
					Utils.changeLanguage(this, lp.getLanguage());

//					if (lp.getLanguage().equals("Hindi")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_HINDI);
//						Utils.changeLanguage(this, "Hindi");
//					} else if (lp.getLanguage().equals("Tamil")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_TAMIL);
//						Utils.changeLanguage(this, "Tamil");
//					} else if (lp.getLanguage().equals("Telugu")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_TELUGU);
//						Utils.changeLanguage(this, "Telugu");
//					} else if (lp.getLanguage().equals("Punjabi")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_PUNJABI);
//						Utils.changeLanguage(this, "Punjabi");
//					} else {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_ENGLISH);
//						Utils.changeLanguage(this, "English");
//						mApplicationConfigurations
//								.setUserSelectedLanguageText("English");
//					}
					try {
						String tagToAdd = "pref_display_" + lp.getLanguage();
						Set<String> tags = Utils.getTags();
						if (!tags.contains(tagToAdd)) {
							if (tags.contains(tagToRemove))
								tags.remove(tagToRemove);
							tags.add(tagToAdd);
							Utils.AddTag(tags);
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

					hideLoadingDialogNew();
					if (isLanguageChanged) {
						sendBroadcast(new Intent(
								MainActivity.ACTION_LANGUAGE_CHANGED));
					} else {
                        Intent new_intent = new Intent();
                        new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
                        sendBroadcast(new_intent);
						finish();
					}
				} catch (Exception e) {
					try {
						mApplicationConfigurations
								.setUserSelectedLanguage(Constants.LANGUAGE_ENGLISH);
						Utils.changeLanguage(this, "English");
						String tagToAdd = "pref_display_English";
						Set<String> tags = Utils.getTags();
						if (!tags.contains(tagToAdd)) {
							if (tags.contains(tagToRemove))
								tags.remove(tagToRemove);
							tags.add(tagToAdd);
							Utils.AddTag(tags);
						}
					} catch (Exception e1) {
						Logger.printStackTrace(e1);
					}
					e.printStackTrace();
				}
				break;
			}

		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		hideLoadingDialogNew();

		if (!TextUtils.isEmpty(errorMessage)) {
			Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onClick(View arg0) {

	}

	private void resetUserDetails() {
		// Delete media folder
		// FileUtils fileUtils = new FileUtils(getActivity());
		// String hungamaFolder =
		// getResources().getString(R.string.download_media_folder);
		// File directory = new File(Environment.getExternalStorageDirectory()
		// .getPath() + "/" + hungamaFolder);
		//
		// if (directory.exists()) {
		// fileUtils.deleteDirectoryRecursively(directory);
		// }
		// When logging out we need to restore the skipped partner user id
		String skippedPartnerUserId = mApplicationConfigurations
				.getSkippedPartnerUserId();
		if (!TextUtils.isEmpty(skippedPartnerUserId)) {
			mApplicationConfigurations.setPartnerUserId(skippedPartnerUserId);
		}

		mApplicationConfigurations.setIsRealUser(false);
		mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
		mApplicationConfigurations.setIsUserHasTrialSubscriptionPlan(false);
		mApplicationConfigurations.setTrialExpiryDaysLeft(0);
		mApplicationConfigurations.setGigyaSessionSecret("");
		mApplicationConfigurations.setGigyaSessionToken("");
		mApplicationConfigurations.setSeletedPreferences("");
		mApplicationConfigurations.setSeletedPreferencesVideo("");

		mDataManager.deleteCurrentSubscriptionPlan();
		mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
	}

}
