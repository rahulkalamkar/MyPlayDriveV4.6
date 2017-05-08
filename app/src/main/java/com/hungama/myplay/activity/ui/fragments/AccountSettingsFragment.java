package com.hungama.myplay.activity.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.hungama.myplay.activity.services.InventoryLightService;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.adapters.SettingsAdapter;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Allows to view properties about the given user singed to the application with
 * the given account.
 */
public class AccountSettingsFragment extends Fragment implements
		OnClickListener, CommunicationOperationListener,
		OnGigyaResponseListener, OnCheckedChangeListener {

	private static final String TAG = "AccountSettingsFragment";

	private static final String SONGS_LISTEN = "songs_listen";
	private static final String MY_FAVORITES = "my_favorites";
	private static final String SONGS_DOWNLOAD = "songs_download";
	private static final String MY_COMMENTS = "my_comments";
	private static final String MY_BADGES = "my_badges";
	private static final String VIDEOS_WATCHED = "videos_watched";
	private static final String VIDEOS_DOWNLOAD = "videos_download";

	public static final String PROVIDER = "provider";

	private Context mContext;

	// Views
	private ImageView thumbImageView;
	private TextView nameTextView;
	private TextView emailTextView;
	private TextView logoutTextView;
	private RelativeLayout secondaryLayout;
	private RelativeLayout sharingSettingsLayout;
	private RelativeLayout accountDetailsLayout;
	private ListView settingsListView;

	// Managers
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private GigyaManager mGigyaManager;

	// Data members
	private SocialNetwork providerType;
	private Map<String, Integer> settingsMap;
	private String fname = "";
	private String lname = "";
	private String email = "";

	// Adapter
	private SettingsAdapter adapter;
	private boolean mIsActivityResumed = false;

	// ======================================================
	// Life cycle callbacks.
	// ======================================================

	public AccountSettingsFragment() {
        mContext = getActivity();
	}

//	public AccountSettingsFragment(Context context) {
//		mContext = context;
//	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);

		providerType = (SocialNetwork) getArguments().get(PROVIDER);

		// creates the cache.
		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
				getActivity(), DataManager.FOLDER_THUMBNAILS_FRIENDS);
		cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);

		Analytics.postCrashlitycsLog(getActivity(), AccountSettingsFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Fetch the root view
		View rootView = inflater.inflate(R.layout.fragment_account_settings,
				container, false);

		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}
		thumbImageView = (ImageView) rootView
				.findViewById(R.id.thumbnail_imageview);
		nameTextView = (TextView) rootView.findViewById(R.id.name_textview);
		emailTextView = (TextView) rootView.findViewById(R.id.email_textview);
		logoutTextView = (TextView) rootView.findViewById(R.id.logout_textview);
		secondaryLayout = (RelativeLayout) rootView
				.findViewById(R.id.secondary_layout);
		sharingSettingsLayout = (RelativeLayout) rootView
				.findViewById(R.id.sharing_settings_layout);
		settingsListView = (ListView) rootView
				.findViewById(R.id.settings_listview);
		accountDetailsLayout = (RelativeLayout) rootView
				.findViewById(R.id.acccount_details_layout);

		if (providerType == null) {
			secondaryLayout.setVisibility(View.VISIBLE);
		}

		if (providerType != null && providerType == SocialNetwork.FACEBOOK) {
			// Get the share settings for FaceBook
			mDataManager.getSharingSettings(this, false, "", 0);

			// Show the share settings
			sharingSettingsLayout.setVisibility(View.VISIBLE);
		}

		setViewsListeners();

		setViews();

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		mIsActivityResumed = true;
	}

	@Override
	public void onPause() {
		super.onPause();
		mIsActivityResumed = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.logout_textview:
			if (Utils.isConnected()) {
				showLogoutDialog();
			} else {
				CustomAlertDialog alertBuilder = new CustomAlertDialog(
						getActivity());
				alertBuilder.setMessage(getResources().getString(
						R.string.go_online_network_error));
				alertBuilder.setNegativeButton("OK",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								startActivity(new Intent(
										android.provider.Settings.ACTION_SETTINGS));
							}
						});
				alertBuilder.show();
			}

			break;

		// case R.id.toggle_button:
		//
		// ToggleButton tb = (ToggleButton) view;
		// String str = (String) view.getTag();
		// updateSharingSettings(str, tb.isChecked());
		//
		// break;
		default:
			break;
		}
	}

	// ======================================================
	// Operation callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		showLoadingDialogFragment();
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case (OperationDefinition.Hungama.OperationId.SHARE_SETTINGS):

				ShareSettingsResponse response = (ShareSettingsResponse) responseObjects
						.get(ShareSettingsOperation.RESULT_KEY_SHARE_SETTINGS);

				// Build the map for the adapter
				settingsMap = new HashMap<String, Integer>();

				if (getActivity() != null) {
					String[] keys = getResources().getStringArray(
							R.array.facebook_sharing_properties);

					settingsMap.put(keys[0], response.data.songs_listen);
					settingsMap.put(keys[1], response.data.my_favorites);
					settingsMap.put(keys[2], response.data.songs_download);
					settingsMap.put(keys[3], response.data.my_comments);
					settingsMap.put(keys[4], response.data.my_badges);
					settingsMap.put(keys[5], response.data.videos_watched);
					settingsMap.put(keys[6], response.data.videos_download);

					List<String> propList = new ArrayList<String>();

					propList = Arrays.asList(keys);

					adapter = new SettingsAdapter(getActivity(), propList,
							settingsMap, this, mDataManager);

					TextView headerView = (TextView) ((LayoutInflater) getActivity()
							.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
							.inflate(R.layout.settings_title_row_layout, null,
									false);

					headerView.setText(Utils.getMultilanguageTextLayOut(
							mContext, getString(R.string.sharing_settings)));

					settingsListView.addHeaderView(headerView);

					settingsListView.setAdapter(adapter);
				}

				hideLoadingDialogFragment();

				break;
			case (OperationDefinition.Hungama.OperationId.SHARE_SETTINGS_UPDATE):

				// hideLoadingDialog();
				hideLoadingDialogFragment();

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

				hideLoadingDialogFragment();

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
				mDataManager.GetUserLanguageMap(AccountSettingsFragment.this);

				break;
            case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):
//				ApsalarEvent.postEvent(getActivity(), ApsalarEvent.LOGIN_COMPLETED);

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

                MainActivity.isUserPreferenceLoaded = false;
                mDataManager.GetUserLanguageMap(AccountSettingsFragment.this);

                try {
                    String secret = mApplicationConfigurations
                            .getGigyaSessionSecret();
                    String token = mApplicationConfigurations
                            .getGigyaSessionToken();

                    if (!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)) {
                        GigyaManager mGigyaManager = new GigyaManager(
                                getActivity());
                        mGigyaManager.setSession(token, secret);
                    }
                } catch (Exception e) {
                    Logger.e(getClass().getName() + ":400", e.toString());
                }

                performInventoryLightService();
                break;

			case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
				try {
//					ApsalarEvent.postEvent(getActivity(), ApsalarEvent.LOGIN_COMPLETED);

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

					String secret = mApplicationConfigurations
							.getGigyaSessionSecret();
					String token = mApplicationConfigurations
							.getGigyaSessionToken();

					if (!TextUtils.isEmpty(secret) && !TextUtils.isEmpty(token)) {
						GigyaManager mGigyaManager = new GigyaManager(
								getActivity());
						mGigyaManager.setSession(token, secret);
					}
				} catch (Exception e) {
					Logger.e(getClass().getName() + ":400", e.toString());
				}
				performInventoryLightService();

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
					ObjLanguagePackage lp = new ObjLanguagePackage(
							responseObjects.toString());
					if (!lp.getLanguage().equals(mApplicationConfigurations.getUserSelectedLanguageText())) {
						Utils.makeText(
								getActivity(),
								getString(R.string.your_langugae_has_been_changed),
								Toast.LENGTH_LONG).show();
					}
					mApplicationConfigurations.setUserSelectedLanguageText(lp
							.getLanguage());
					mApplicationConfigurations
							.setUserSelectedLanguage(lp.getId());
					Utils.changeLanguage(mContext, lp.getLanguage());

//					if (lp.getLanguage().equals("Hindi")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_HINDI);
//						Utils.changeLanguage(mContext, "Hindi");
//					} else if (lp.getLanguage().equals("Tamil")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_TAMIL);
//						Utils.changeLanguage(mContext, "Tamil");
//					} else if (lp.getLanguage().equals("Telugu")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_TELUGU);
//						Utils.changeLanguage(mContext, "Telugu");
//					} else if (lp.getLanguage().equals("Punjabi")) {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_PUNJABI);
//						Utils.changeLanguage(mContext, "Punjabi");
//					} else {
//						mApplicationConfigurations
//								.setUserSelectedLanguage(Constants.LANGUAGE_ENGLISH);
//						Utils.changeLanguage(mContext, "English");
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

					hideLoadingDialogFragment();
					getActivity().sendBroadcast(
							new Intent(MainActivity.ACTION_LANGUAGE_CHANGED));
					Intent new_intent = new Intent();
					new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
					getActivity().sendBroadcast(new_intent);
				} catch (Exception e) {
					try {
						mApplicationConfigurations
								.setUserSelectedLanguage(Constants.LANGUAGE_ENGLISH);
						Utils.changeLanguage(getActivity().getBaseContext(),
								"English");
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

	/**
	 * start inventory service for getting user playlist, tracks and other
	 * details
	 */
	private void performInventoryLightService() {
		try {
			// Sync's the inventory.
			Intent inventoryLightService = new Intent(mContext,
					InventoryLightService.class);
			// mContext.startService(inventoryLightService);
			FragmentManager fragmentManager = getFragmentManager();
			if (fragmentManager != null) {

				fragmentManager.popBackStack();
				hideLoadingDialogFragment();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		try {
			hideLoadingDialogFragment();
			if (!TextUtils.isEmpty(errorMessage)) {
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT)
						.show();
			}
		} catch (Exception e) {
		}
	}

	/**
	 * set user detials for particular social account
	 */
	private void setViews() {

		// Get the FirstName, LastNamt, Email

		// Get the ThumbUrl

		if (providerType == SocialNetwork.FACEBOOK) {
			String fbThumbUrl = mApplicationConfigurations.getGiGyaFBThumbUrl();
			loadThumbnailUrl(fbThumbUrl);

			setFacebookDetails();

		} else if (providerType == SocialNetwork.TWITTER) {
			String twitterThumbUrl = mApplicationConfigurations
					.getGiGyaTwitterThumbUrl();
			loadThumbnailUrl(twitterThumbUrl);

			setTwitterDetails();

		} else if (providerType == SocialNetwork.GOOGLE) {

			thumbImageView.setVisibility(View.GONE);

			setGoogleDetails();

			if (TextUtils.isEmpty(fname) && TextUtils.isEmpty(lname)) {
				fname = email;
				emailTextView.setVisibility(View.INVISIBLE);
			}

		} else {
			Logger.i("HunEmail", email);
			thumbImageView.setVisibility(View.GONE);

			setHungamaDetails();

			if (TextUtils.isEmpty(fname) && TextUtils.isEmpty(lname)) {

				if (mGigyaManager.isFBConnected()) {

					setFacebookDetails();

				} else if (mGigyaManager.isTwitterConnected()) {

					setTwitterDetails();

				} else if (mGigyaManager.isGoogleConnected()) {

					setGoogleDetails();

				} else if (TextUtils.isEmpty(email)) {
					// We have no first name and last name to show so invisible
					// this layout
					accountDetailsLayout.setVisibility(View.INVISIBLE);
				}
			}
		}

		nameTextView.setText(fname + " " + lname);
		emailTextView.setText(email);

	}

	/**
	 * set logout listener
	 */
	private void setViewsListeners() {
		logoutTextView.setOnClickListener(this);
	}

	/**
	 * set user profile image
	 * 
	 * @param url
	 */
	private void loadThumbnailUrl(String url) {

		Picasso.with(getActivity()).cancelRequest(thumbImageView);
		if (getActivity().getApplicationContext() != null
				&& !TextUtils.isEmpty(url)) {
			// mImageFetcher.loadImage(url, thumbImageView);
			Picasso.with(getActivity()).load(url)
					.placeholder(R.drawable.background_home_tile_album_default)
					.into(thumbImageView);
		}
	}

	/**
	 * Get the FirstName LastNamt, Email for facebook
	 */
	private void setFacebookDetails() {

		fname = mApplicationConfigurations.getGigyaFBFirstName();
		lname = mApplicationConfigurations.getGigyaFBLastName();
		email = mApplicationConfigurations.getGigyaFBEmail();
	}

	/**
	 * Get the FirstName LastNamt, Email for Twitter
	 */
	private void setTwitterDetails() {

		// Get the FirstName LastNamt, Email
		fname = mApplicationConfigurations.getGigyaTwitterFirstName();
		lname = mApplicationConfigurations.getGigyaTwitterLastName();
		email = mApplicationConfigurations.getGigyaTwitterEmail();

	}

	/**
	 * Get the FirstName LastNamt, Email for Google
	 */
	private void setGoogleDetails() {

		// Get the FirstName LastNamt, Email
		fname = mApplicationConfigurations.getGigyaGoogleFirstName();
		lname = mApplicationConfigurations.getGigyaGoogleLastName();
		email = mApplicationConfigurations.getGigyaGoogleEmail();
	}

	/**
	 * Get the FirstName LastNamt, Email for Hungma user
	 */
	private void setHungamaDetails() {

		// Get the FirstName LastNamt, Email
		fname = mApplicationConfigurations.getHungmaFirstName();
		lname = mApplicationConfigurations.getHungamaLastName();
		email = mApplicationConfigurations.getHungamaEmail();
	}

	/**
	 * Updating share setting
	 */
	private void updateSharingSettings(String key, boolean value) {

		int state;
		String shareSettingType = "";

		if (key.equalsIgnoreCase(getActivity().getString(
				R.string.songs_i_listen_to))) {
			shareSettingType = SONGS_LISTEN;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.my_favorite))) {
			shareSettingType = MY_FAVORITES;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.songs_i_downloaded))) {
			shareSettingType = SONGS_DOWNLOAD;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.my_comments))) {
			shareSettingType = MY_COMMENTS;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.my_badges))) {
			shareSettingType = MY_BADGES;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.videos_watched))) {
			shareSettingType = VIDEOS_WATCHED;
		} else if (key.equalsIgnoreCase(getActivity().getString(
				R.string.videos_downloaded))) {
			shareSettingType = VIDEOS_DOWNLOAD;
		}

		if (value) {
			state = 1;
		} else {
			state = 0;
		}

		mDataManager.getSharingSettings(this, true, shareSettingType, state);
	}

	MyProgressDialog progressDialog;

	private void showLoadingDialogFragment() {

		// FragmentManager fragmentManager = getFragmentManager();
		//
		// if (fragmentManager != null) {
		//
		// Fragment fragment = fragmentManager
		// .findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
		//
		// if (fragment == null && mIsActivityResumed) {
		//
		// LoadingDialogFragment dialogFragment = LoadingDialogFragment
		// .newInstance(R.string.application_dialog_loading);
		// dialogFragment.setCancelable(true);
		// dialogFragment.show(fragmentManager,
		// LoadingDialogFragment.FRAGMENT_TAG);
		// }
		// }
		if (progressDialog == null) {
			progressDialog = new MyProgressDialog(getActivity());
		}
	}

	private void hideLoadingDialogFragment() {

		// if (getActivity() != null) {
		//
		// FragmentManager fragmentManager = getActivity()
		// .getSupportFragmentManager();
		//
		// if (fragmentManager != null) {
		//
		// Fragment fragment = fragmentManager
		// .findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
		//
		// if (fragment != null) {
		//
		// DialogFragment fragmentDialog = (DialogFragment) fragment;
		// FragmentTransaction fragmentTransaction = fragmentManager
		// .beginTransaction();
		// fragmentTransaction.remove(fragmentDialog);
		// fragmentDialog.dismissAllowingStateLoss();
		// }
		// }
		// }
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}

	/**
	 * show logout dialog
	 */
	private void showLogoutDialog() {
		CustomAlertDialog alertDialogBuilder = new CustomAlertDialog(
				this.getActivity());

		// set title
		alertDialogBuilder.setTitle(Utils.getMultilanguageText(mContext,
				getResources().getString(R.string.logout_dialog_title)));

		// set dialog message
		alertDialogBuilder
				.setMessage(
						Utils.getMultilanguageText(mContext, getResources()
								.getString(R.string.logout_dialog_text)))
				.setCancelable(true)
				.setPositiveButton(
						Utils.getMultilanguageText(mContext,
								getString(R.string.exit_dialog_text_yes)),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								try {
									SettingFragmentNew.isAutoSelected = true;
									showLoadingDialogFragment();
									// Log out from Gigya
									mGigyaManager.logout();

									//
//									mApplicationConfigurations
//											.setSessionID(null);
//									mApplicationConfigurations.setConsumerID(0);
//									mApplicationConfigurations
//											.setConsumerRevision(0);
//									mApplicationConfigurations
//											.setHouseholdID(0);
//									mApplicationConfigurations
//											.setHouseholdRevision(0);
//									mApplicationConfigurations.setPasskey(null);
//									mApplicationConfigurations
//											.setPartnerUserId(mApplicationConfigurations
//													.getSkippedPartnerUserId());
//									mApplicationConfigurations
//											.setIsRealUser(false);
//
//									mApplicationConfigurations
//											.setGigyaSessionSecret(null);
//									mApplicationConfigurations
//											.setGigyaSessionToken(null);

									mApplicationConfigurations
											.setHungamaFirstName("");
									mApplicationConfigurations
											.setHungamaLastName("");
									mApplicationConfigurations
											.setHungamaEmail("");
									Utils.setAlias(null,
											mDataManager.getDeviceConfigurations().getHardwareId());
//									mApplicationConfigurations
//											.setSeletedPreferences("");
//									mApplicationConfigurations
//											.setSeletedPreferencesVideo("");

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
												.getInstance(getActivity()
														.getApplicationContext());
										mCampaignsManager.clearCampaigns();
										mCampaignsManager
												.setWeightsMap(new HashMap<Float, Placement>());

										// Delete all locale playlists on device
										DataManager mDataManager = DataManager
												.getInstance(getActivity());
										Map<Long, Playlist> empty = new HashMap<Long, Playlist>();
										mDataManager
												.storePlaylists(empty, null);
									} catch (Exception e) {
										Logger.printStackTrace(e);
									}
									performSilentLogin();
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
							}
						})
				.setNegativeButton(
						Utils.getMultilanguageText(mContext,
								getString(R.string.exit_dialog_text_no)),
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

	// ======================================================
	// Gigya callbacks.
	// ======================================================

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
		// performSilentLogin();
	}

	@Override
	public void onFacebookInvite() {
	}

	@Override
	public void onTwitterInvite() {
	}

	private void performSilentLogin() {
        if (TextUtils.isEmpty(mApplicationConfigurations.getSilentPartnerUserId())
                || TextUtils.isEmpty(mApplicationConfigurations.getSilentUserSessionID())
                || TextUtils.isEmpty(mApplicationConfigurations.getSilentUserPasskey())) {
            mDataManager.readPartnerInfo(this);
        } else {
            resetUserDetails();

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
                    mGigyaManager = new GigyaManager(getActivity());
                mGigyaManager.setSession(token, secret);
            }
//        mDataManager.GetUserLanguageMap(this);
            try {
                if (mApplicationConfigurations.getUserSelectedLanguage() != Constants.LANGUAGE_ENGLISH) {
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
                    Utils.changeLanguage(getActivity(), mApplicationConfigurations.getSilentUserSelectedLanguageText());
                    String tagToAdd = "pref_display_English";
                    Set<String> tags = Utils.getTags();
                    if (!tags.contains(tagToAdd)) {
                        if (tags.contains(tagToRemove))
                            tags.remove(tagToRemove);
                        tags.add(tagToAdd);
                        Utils.AddTag(tags);
                    }
                }
                getActivity().sendBroadcast(new Intent(
                        MainActivity.ACTION_LANGUAGE_CHANGED));
				Intent new_intent = new Intent();
				new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
				getActivity().sendBroadcast(new_intent);
            } catch (Exception e1) {
                Logger.printStackTrace(e1);
            }
            hideLoadingDialogFragment();
        }
	}

	/**
	 * Resetting user detail
	 */
	private void resetUserDetails() {
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

	public static class ObjLanguagePackage extends JSONObject {

		public ObjLanguagePackage(String response) throws JSONException {
			super(response);
		}

		public int getCode() {
			try {
				return getJSONObject("response").getInt("code");

			} catch (Exception e) {
			}
			return 0;
		}

		public String getLanguage() {
			try {
				return getJSONObject("response").getJSONObject("language")
						.getString("language_name: ");

			} catch (Exception e) {
			}
			return null;
		}

		public int getId(){
			try {
				if(getJSONObject("response").getJSONObject("language").has("id"))
					return getJSONObject("response").getJSONObject("language")
							.getInt("id");
				else{
					if (getLanguage().equals("Hindi")) {
						return Constants.LANGUAGE_HINDI;
					} else if (getLanguage().equals("Tamil")) {
						return Constants.LANGUAGE_TAMIL;
					} else if (getLanguage().equals("Telugu")) {
						return Constants.LANGUAGE_TELUGU;
					} else if (getLanguage().equals("Punjabi")) {
						return Constants.LANGUAGE_PUNJABI;
					} else {
						return Constants.LANGUAGE_ENGLISH;
					}
				}
			} catch (Exception e) {
			}
			return 0;
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
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
		String str = (String) buttonView.getTag();
		updateSharingSettings(str, isChecked);
	}
}
