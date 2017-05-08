package com.hungama.myplay.activity.gigya;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.data.dao.hungama.UserProfileResponse;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.gigya.TwitterLoginFragment.OnTwitterLoginListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.hungama.GetUserProfileOperation;
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

/**
 * @author DavidSvilem
 * 
 */
public class InviteFriendsFragment extends Fragment implements OnClickListener,
		CommunicationOperationListener, OnGigyaResponseListener,
		OnTwitterLoginListener {

	private static final String TAG = "InviteFriendsFragment";
	private static final String VALUE = "value";

	// Views
	private LanguageTextView mTextTitle;
	private Button mInviteFacebookFriends;
	private Button mInviteTwitterFriends;
	private Button mInviteGooleFriends;
	private Button mInviteEmailFriends;
	private ProgressBar mProgressBar1;

	// Managers
	private GigyaManager mGigyaManager;
	private DataManager mDataManager;

	private ApplicationConfigurations mApplicationConfigurations;

	private SocialNetwork provider;

	private TwitterLoginFragment mTwitterLoginFragment;
	private MyProgressDialog mProgressDialog;
	private String processing;

	private SocialNetwork mProvider;
	private List<FBFriend> mFbFriends;
	private List<GoogleFriend> mGoogleFriends;

	private String mFlurrySource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		processing = Utils.getMultilanguageText(getActivity(), getActivity()
				.getResources().getString(R.string.processing));

		mFlurrySource = getArguments().getString(
				InviteFriendsActivity.FLURRY_SOURCE);
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
	}

	@Override
	public void onStop() {
		if (mProgressBar1 != null
				&& mProgressBar1.getVisibility() == View.VISIBLE) {
			mProgressBar1.setVisibility(View.GONE);
		}
		super.onStop();

		Analytics.onEndSession(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Fetch the root view
		View rootView = inflater.inflate(R.layout.fragment_invite_friends,
				container, false);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}

		// Views initialize
		mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.main_title_bar_text);

		mInviteFacebookFriends = (Button) rootView
				.findViewById(R.id.invite_facebook_friends);
		mInviteTwitterFriends = (Button) rootView
				.findViewById(R.id.invite_twitter_friends);
		mInviteGooleFriends = (Button) rootView
				.findViewById(R.id.invite_google_friends);
		mInviteEmailFriends = (Button) rootView
				.findViewById(R.id.invite_email_friends);
		mProgressBar1 = (ProgressBar) rootView.findViewById(R.id.progressBar1);

		// Set click listener
		mInviteFacebookFriends.setOnClickListener(this);
		mInviteTwitterFriends.setOnClickListener(this);
		mInviteGooleFriends.setOnClickListener(this);
		mInviteEmailFriends.setOnClickListener(this);

		// Set title
		String title = getString(R.string.invite_friends_title);
		Utils.SetMultilanguageTextOnTextView(getActivity(), mTextTitle, title);
		// mTextTitle.setText(title);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onResume() {
		super.onResume();
		mInviteFacebookFriends.setEnabled(true);
		mInviteTwitterFriends.setEnabled(true);
		mInviteGooleFriends.setEnabled(true);
		mInviteEmailFriends.setEnabled(true);
		mProgressBar1.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.invite_facebook_friends:
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			// showInviteFBUnderConstructionDialog();
			provider = SocialNetwork.FACEBOOK;
			mGigyaManager.getSocialNetworkFriends(SocialNetwork.FACEBOOK);
			mProgressBar1.setVisibility(View.VISIBLE);
			mInviteFacebookFriends.setEnabled(false);
			break;

		case R.id.invite_twitter_friends:
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			provider = SocialNetwork.TWITTER;
			mGigyaManager.getSocialNetworkFriends(SocialNetwork.TWITTER);
			mProgressBar1.setVisibility(View.VISIBLE);
			mInviteTwitterFriends.setEnabled(false);
			break;

		case R.id.invite_google_friends:
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			provider = SocialNetwork.GOOGLE;
			mGigyaManager.getSocialNetworkFriends(SocialNetwork.GOOGLE);
			mProgressBar1.setVisibility(View.VISIBLE);
			mInviteGooleFriends.setEnabled(false);
			break;

		case R.id.invite_email_friends:
			mInviteEmailFriends.setEnabled(false);
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			// Open Native Email application
			// Send mail to all checked friends
			Utils.invokeEmailApp(
					this,
					null,
					getActivity()
							.getString(R.string.invite_friend_mail_subject),
					getActivity()
							.getString(
									R.string.invite_friend_mail_text,
									mApplicationConfigurations
											.getGigyaGoogleFirstName(),
									mApplicationConfigurations
											.getGigyaGoogleLastName()));

			// xtpl
			// Flurry report: Invite friends
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryInvite.Source.toString(),
					mFlurrySource);
			reportMap.put(FlurryConstants.FlurryInvite.Mode.toString(),
					"Email Section selected");
			// reportMap.put(FlurryConstants.FlurryInvite.CountOfFriends.toString(),
			// String.valueOf(inviteFriendsCount));
			Analytics.logEvent(
					FlurryConstants.FlurryInvite.InviteFriends.toString(),
					reportMap);
			// xtpl
			break;

		default:
			break;
		}

	}

	private void addFriendsListFragment(SocialNetwork provider,
			List<FBFriend> fbFriends, List<GoogleFriend> googleFriends,
			final String flurrySource) {

		mProvider = provider;
		mFbFriends = fbFriends;
		mGoogleFriends = googleFriends;

		// For avoiding perform an action after onSaveInstanceState.
		new Handler().post(new Runnable() {
			public void run() {
				if (getActivity() != null) {
					FragmentManager mFragmentManager = getActivity()
							.getSupportFragmentManager();
					FragmentTransaction fragmentTransaction = mFragmentManager
							.beginTransaction();
					FriendsListFragment friendsListFragment = new FriendsListFragment();
					friendsListFragment.init(mProvider, mFbFriends, mGoogleFriends, flurrySource);
					fragmentTransaction.replace(R.id.main_fragmant_container,
							friendsListFragment);
					fragmentTransaction.addToBackStack(null);
					if(Constants.IS_COMMITALLOWSTATE)
						fragmentTransaction.commitAllowingStateLoss();
					else
						fragmentTransaction.commit();
					// fragmentTransaction.commit();
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onStart(int operationId) {
		// Show Dialog
		showLoadingDialog(processing);
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {

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

				hideLoadingDialog();

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
//                isRealUser = (Boolean) responseObjects
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

//                hideLoadingDialog();

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

                hideLoadingDialog();
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
				hideLoadingDialog();
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

				hideLoadingDialog();

				break;

			case (OperationDefinition.Hungama.OperationId.SOCIAL_BADGE_ALERT):

				FragmentManager fm = getFragmentManager();
				if (fm != null) {
					fm.popBackStack();
				}

				hideLoadingDialog();

				break;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		try {
			switch (operationId) {
			case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
            case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):
				getFragmentManager().popBackStack();
				mGigyaManager.cancelGigyaProviderLogin();
				hideLoadingDialog();

				break;

			case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
				getFragmentManager().popBackStack();
				hideLoadingDialog();

				break;

			default:
				break;
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":338", e.toString());
		}
	}

	@Override
	public void onGigyaLoginListener(SocialNetwork provider,
			Map<String, Object> signupFields, long setId) {
		try {
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
						fragment);
				fragmentTransaction.addToBackStack(TwitterLoginFragment.class
						.toString());
				fragmentTransaction
						.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
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
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {
		mProgressBar1.setVisibility(View.GONE);

		addFriendsListFragment(provider, fbFriendsList, null, mFlurrySource);
	}

	@Override
	public void onSocializeGetContactsListener(
			List<GoogleFriend> googleFriendsList) {
		mProgressBar1.setVisibility(View.GONE);

		addFriendsListFragment(provider, null, googleFriendsList, mFlurrySource);
	}

	@Override
	public void onSocializeGetUserInfoListener() {
	}

	@Override
	public void onGigyaLogoutListener() {
	}

	@Override
	public void onFacebookInvite() {
		Logger.i(TAG, "onFacebookInvite");
	}

	@Override
	public void onTwitterInvite() {
	}

	@Override
	public void onTwitterLoginListener(TwitterLoginFragment fragment,
			Map<String, Object> signupFields, long setId) {

		// Call PCP
		// It's include the email and password that user insert in
		// TwitterLoginFragment
		mDataManager.createPartnerConsumerProxy(signupFields, setId, this,
				false);
		mTwitterLoginFragment = fragment;

	}

	@Override
	public void onCancelLoginListener() {
		mGigyaManager.removeConnetion(SocialNetwork.TWITTER);
	}

	public void showLoadingDialog(String message) {
		try {
			if (!getActivity().isFinishing()) {
				if (mProgressDialog == null) {
					mProgressDialog = new MyProgressDialog(getActivity());
					mProgressDialog.setCancelable(true);
				}
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":452", e.toString());
		}
	}

	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
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
		if (mProgressBar1 != null
				&& mProgressBar1.getVisibility() == View.VISIBLE) {
			mProgressBar1.setVisibility(View.GONE);
		}

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
}