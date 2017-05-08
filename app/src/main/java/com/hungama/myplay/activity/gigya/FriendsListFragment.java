package com.hungama.myplay.activity.gigya;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
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
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageCache;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author DavidSvilem
 */
public class FriendsListFragment extends Fragment implements
		OnGigyaResponseListener, OnClickListener,
		CommunicationOperationListener, OnTwitterLoginListener,
		OnItemClickListener {

	private static final String TAG = "FriendsListFragment";

	private static final String VALUE = "value";

	private static final int MAXIMUM_FACEBOOK_FRIENDS_TO_INVITE = 10;

	// Views
	private LanguageTextView mTextTitle;
	private ListView mListView;
	private LanguageButton mInviteFriendsButton;
	private LanguageButton mFriendsSelectionButton;
	private MyProgressDialog mProgressDialog;
	private View mView;

	// Friends Data
	private List<FBFriend> friendsList;
	private List<GoogleFriend> googleFriendsList;

	// Managers
	private GigyaManager mGigyaManager;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	// Image Fetcher
	// private ImageFetcher mImageFetcher = null;
	private LayoutInflater mInflater;
	private FriendsAdapter mFriendsAdapter;
	private boolean selectAll;

	// Causing the Badge and Alert to be triggered only one time
	// when multiple friends invitation in FaceBook.
	private boolean oneShotInvite;

	private SocialNetwork provider;

	private TwitterLoginFragment mTwitterLoginFragment;

	private String processing;

	private Context mContext;

	private String mFlurrySource;

	private int inviteFriendsCount = 0;

	public FriendsListFragment() {
	}

	public void init(SocialNetwork provider,
			List<FBFriend> fbFriends, List<GoogleFriend> googleFriends,
			String flurrySource) {

		this.provider = provider;

		if (fbFriends != null) {
			this.friendsList = fbFriends;
			Collections.sort(friendsList);
		}

		if (googleFriends != null) {
			this.googleFriendsList = googleFriends;
			Collections.sort(googleFriendsList);
		}

		mFlurrySource = flurrySource;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity().getApplicationContext();
		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);

		mInflater = (LayoutInflater) getActivity().getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		selectAll = true;

		processing = Utils.getMultilanguageText(mContext, getActivity()
				.getResources().getString(R.string.processing));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Fetch the root view
		View rootView;
		try {
			rootView = inflater.inflate(R.layout.fragment_friends_list,
					container, false);
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}
		} catch (Error e) {
			System.gc();
			System.runFinalization();
			System.gc();
			rootView = inflater.inflate(R.layout.fragment_friends_list,
					container, false);
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}
		}

		// Views initialize
		mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.main_title_bar_text);
		mListView = (ListView) rootView.findViewById(R.id.friends_listview);
		mInviteFriendsButton = (LanguageButton) rootView
				.findViewById(R.id.invite_friend_button);
		mFriendsSelectionButton = (LanguageButton) rootView
				.findViewById(R.id.friends_selection_button);
		mView = (View) rootView.findViewById(R.id.header);

		mListView.setOnItemClickListener(this);

		// Set title
		String title = Utils.getMultilanguageTextLayOut(mContext,
				getString(R.string.invite_friends_title));
		mTextTitle.setText(title);

		// Set Buttons listeners
		mInviteFriendsButton.setOnClickListener(this);
		mFriendsSelectionButton.setOnClickListener(this);

		// Set ListView properties
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		mListView.setItemsCanFocus(false);

		// Set adapter
		mFriendsAdapter = new FriendsAdapter();
		mListView.setAdapter(mFriendsAdapter);

		if (provider == SocialNetwork.FACEBOOK) {
			mFriendsSelectionButton.setVisibility(View.GONE);
		} else {
			mFriendsSelectionButton.setVisibility(View.VISIBLE);
		}

		if ((provider == SocialNetwork.FACEBOOK || provider == SocialNetwork.TWITTER)
				&& Utils.isListEmpty(friendsList)) {
			Utils.makeText(getActivity(),
					getString(R.string.no_friends_in_your_account),
					Toast.LENGTH_LONG).show();
		} else if (provider == SocialNetwork.GOOGLE
				&& Utils.isListEmpty(googleFriendsList)) {
			Utils.makeText(getActivity(),
					getString(R.string.no_friends_in_your_account),
					Toast.LENGTH_LONG).show();
		}

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	//
	// OnGigyaResponseListener Call backs
	//
	@Override
	public void onSocializeGetFriendsInfoListener(List<FBFriend> fbFriendsList) {
		this.friendsList = fbFriendsList;

		mFriendsAdapter.notifyDataSetChanged();

		hideLoadingDialog();

	}

	@Override
	public void onSocializeGetContactsListener(
			List<GoogleFriend> googleFriendsList) {
		this.googleFriendsList = googleFriendsList;

		mFriendsAdapter.notifyDataSetChanged();

		hideLoadingDialog();
	}

	@Override
	public void onGigyaLoginListener(SocialNetwork provider,
			Map<String, Object> signupFields, long setId) {
		if (provider == SocialNetwork.TWITTER) {
			// Twitter
			FragmentManager mFragmentManager = getFragmentManager();
			FragmentTransaction fragmentTransaction = mFragmentManager
					.beginTransaction();
			fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
					R.anim.slide_left_exit, R.anim.slide_right_enter,
					R.anim.slide_right_exit);

			TwitterLoginFragment fragment = new TwitterLoginFragment();
            fragment.init(signupFields, setId);
			fragmentTransaction.replace(R.id.main_fragmant_container, fragment);
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
				mDataManager.createPartnerConsumerProxy(signupFields, setId, this,
						false);
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
		reportMap.put(FlurryConstants.FlurryUserStatus.TypeOfLogin.toString(),
				provider.name());
		reportMap.put(
				FlurryConstants.FlurryUserStatus.RegistrationStatus.toString(),
				registrationStatus);
		Analytics.logEvent(
				FlurryConstants.FlurryUserStatus.SocialLogin.toString(),
				reportMap);
	}

	//
	// OnTwitterLoginListener
	//
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

	//
	// Adapter
	//
	public class FriendsAdapter extends BaseAdapter {

		public FriendsAdapter() {
			// initializes the image loader.
			int imageSize = getResources().getDimensionPixelSize(
					R.dimen.search_result_line_image_size);

			// creates the cache.
			ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(
					getActivity(), DataManager.FOLDER_THUMBNAILS_FRIENDS);
			cacheParams.setMemCacheSizePercent(getActivity(), 0.10f);

			// mImageFetcher = new ImageFetcher(getActivity(), imageSize);
			// mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
			// mImageFetcher.addImageCache(getChildFragmentManager(),
			// cacheParams);
			// mImageFetcher.setImageFadeIn(true);
		}

		@Override
		public int getCount() {

			if (provider == SocialNetwork.GOOGLE) {
				// Google
				return (Utils.isListEmpty(googleFriendsList) ? 0
						: googleFriendsList.size());
			} else {
				// FaceBook / Twitter
				return (Utils.isListEmpty(friendsList) ? 0 : friendsList.size());
			}
		}

		@Override
		public Object getItem(int position) {

			if (provider == SocialNetwork.GOOGLE) {
				return googleFriendsList.get(position);
			} else {
				return friendsList.get(position);
			}
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder viewHolder;

			final int pos = position;

			if (convertView == null) {

				convertView = mInflater.inflate(R.layout.list_item_friend_item,
						parent, false);

				viewHolder = new ViewHolder();

				viewHolder.friendNickname = (TextView) convertView
						.findViewById(R.id.friend_nickname);
				viewHolder.friendEmail = (TextView) convertView
						.findViewById(R.id.friend_email);
				viewHolder.friendTumbnail = (ImageView) convertView
						.findViewById(R.id.friend_thumbnail);
				viewHolder.checkedTextView = (CheckedTextView) convertView
						.findViewById(R.id.checked_item);
				// viewHolder.checkedTextView.setOnClickListener(new
				// OnClickListener() {
				//
				// @Override
				// public void onClick(View v) {
				// viewHolder.checkedTextView.toggle();
				//
				// //
				// viewHolder.checkedTextView.setChecked(!viewHolder.checkedTextView.isChecked());
				//
				//
				// mListView.setItemChecked(pos,
				// viewHolder.checkedTextView.isChecked());
				// }
				// });

				convertView.setTag(viewHolder);

			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			// xtpl
			viewHolder.friendNickname.setText("");
			viewHolder.friendEmail.setText("");
			// xtpl

			// convertView =
			// mInflater.inflate(R.layout.list_item_friend_item,parent, false);
			//
			// TextView nicknameTV = (TextView)
			// convertView.findViewById(R.id.friend_nickname);
			// TextView emailTV = (TextView)
			// convertView.findViewById(R.id.friend_email);
			// ImageView thumbnailIV = (ImageView)
			// convertView.findViewById(R.id.friend_thumbnail);
			// final CheckedTextView checkedTV = (CheckedTextView)
			// convertView.findViewById(R.id.checked_item);

			String thumbUrl = null;
			String nickname = null;
			String email = null;

			if (provider == SocialNetwork.GOOGLE) {

				GoogleFriend googleFriend = googleFriendsList.get(position);

				nickname = googleFriend.nickname;
				email = googleFriend.email;

				viewHolder.friendTumbnail.setVisibility(View.GONE);
				// thumbnailIV.setVisibility(View.GONE);

			} else {
				FBFriend friend = friendsList.get(position);

				nickname = friend.nickname;
				thumbUrl = friend.thumbnailURL;
			}

			if (!TextUtils.isEmpty(nickname)) {
				viewHolder.friendNickname.setText(nickname);
				// nicknameTV.setText(nickname);
			}

			if (!TextUtils.isEmpty(email)) {
				viewHolder.friendEmail.setText(email);
				// emailTV.setText(email);
			}

			if (!TextUtils.isEmpty(thumbUrl)) {
				// mImageFetcher.loadImage(thumbUrl, viewHolder.friendTumbnail);
				// // This was the last one
				// mImageFetcher.loadImage(thumbUrl, thumbnailIV);
				Picasso.with(mContext).cancelRequest(viewHolder.friendTumbnail);
				if (mContext != null && thumbUrl != null
						&& !TextUtils.isEmpty(thumbUrl)) {
					Picasso.with(mContext)
							.load(thumbUrl)
							.placeholder(
									R.drawable.background_home_tile_album_default)
							.into(viewHolder.friendTumbnail);
				}
			}

			viewHolder.checkedTextView
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							viewHolder.checkedTextView.toggle();
							mListView.setItemChecked(pos,
									viewHolder.checkedTextView.isChecked());
						}
					});

			return convertView;
		}

		@Override
		public boolean isEnabled(int position) {
			return true;
		}
	}

	private static class ViewHolder {
		ImageView friendTumbnail;
		TextView friendNickname;
		TextView friendEmail;
		CheckedTextView checkedTextView;
	}

	public void showLoadingDialog(String message) {
		if (getActivity() != null && !getActivity().isFinishing()) {
			if (mProgressDialog == null) {
				// mProgressDialog = new ProgressDialog(getActivity());
				// mProgressDialog = ProgressDialog.show(getActivity(), "",
				// message, true, true);
				mProgressDialog = new MyProgressDialog(getActivity());
				mProgressDialog.setCancelable(true);
			}
		}
	}

	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	@Override
	public void onResume() {
		Logger.s("onResume  FriendListFragment");
		super.onResume();
		// if (mImageFetcher != null)
		// mImageFetcher.setExitTasksEarly(false);
	}

	@Override
	public void onPause() {
		super.onPause();
		// if (mImageFetcher != null)
		// mImageFetcher.setExitTasksEarly(true);
	}

	@Override
	public void onStop() {
		// if (mImageFetcher != null)
		// mImageFetcher.flushCache();
		super.onStop();

		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onStart() {
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(getActivity(),
		// getString(R.string.flurry_app_key));
//		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.invite_friend_button:

			// Invite
			SparseBooleanArray positions = mListView.getCheckedItemPositions();

			if (positions.size() > 0) {

				if (provider == SocialNetwork.FACEBOOK
						|| provider == SocialNetwork.TWITTER) {

					// Get a list of the checked friends
					FBFriend friend;
					List<FBFriend> inviteFriendList = new ArrayList<FBFriend>();

					StringBuilder friendsProviderUIDs = new StringBuilder();

					for (int i = 0; i < positions.size(); i++) {

						int checkedPosition = positions.keyAt(i);
						boolean checked = positions.valueAt(i);

						if (checked) {
							friend = friendsList.get(checkedPosition);

							// For Facebbok
							friendsProviderUIDs.append(
									friend.identities.get(0).providerUID)
									.append(",");

							// For Twitter
							inviteFriendList.add(friend);
						}
					}
					if (friendsProviderUIDs.length() > 0)
						friendsProviderUIDs.deleteCharAt(friendsProviderUIDs
								.length() - 1);

					if (provider == SocialNetwork.FACEBOOK) {
						// oneShotInvite = true;
						// mGigyaManager.socializeFacebookGraphOperation(inviteFriendList);
						mInviteFriendsButton.setEnabled(false);
						mGigyaManager.inviteFacebookFriends(
								friendsProviderUIDs.toString(), getActivity());

					} else if (provider == SocialNetwork.TWITTER) {
						mInviteFriendsButton.setEnabled(false);
						mGigyaManager
								.socializeSendNotification(inviteFriendList);
					}

					inviteFriendsCount = inviteFriendList.size();

				} else if (provider == SocialNetwork.GOOGLE) {

					// Get a list of the checked friends
					GoogleFriend friend;
					List<GoogleFriend> inviteFriendList = new ArrayList<GoogleFriend>();

					List<String> emailBccTo = new ArrayList<String>();

					for (int i = 0; i < positions.size(); i++) {

						int checkedPosition = positions.keyAt(i);
						boolean checked = positions.valueAt(i);

						friend = googleFriendsList.get(checkedPosition);

						if (checked) {
							inviteFriendList.add(friend);
							emailBccTo.add(friend.email);
						}
					}

					inviteFriendsCount = inviteFriendList.size();

					// Send Email to all checked friends
					Utils.invokeEmailApp(
							this,
							emailBccTo,
							getResources().getString(
									R.string.invite_friend_mail_subject),
							getResources().getString(
									R.string.invite_friend_mail_text,
									mApplicationConfigurations
											.getGigyaGoogleFirstName(),
									mApplicationConfigurations
											.getGigyaGoogleLastName()));
				}
			}

			// Flurry report: Invite friends
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryInvite.Source.toString(),
					mFlurrySource);
			reportMap.put(FlurryConstants.FlurryInvite.Mode.toString(),
					provider.toString());
			reportMap.put(
					FlurryConstants.FlurryInvite.CountOfFriends.toString(),
					String.valueOf(inviteFriendsCount));
			Analytics.logEvent(
					FlurryConstants.FlurryInvite.InviteFriends.toString(),
					reportMap);

			break;

		case R.id.friends_selection_button:

			// Select All / Cancel All
			if (selectAll) {
				selectAll();
				mFriendsSelectionButton.setText(Utils
						.getMultilanguageTextLayOut(
								mContext,
								getActivity().getResources().getString(
										R.string.cancel_all_social_friends)));
				selectAll = false;
			} else {
				cancelAll();
				mFriendsSelectionButton.setText(Utils
						.getMultilanguageTextLayOut(
								mContext,
								getActivity().getResources().getString(
										R.string.select_all_social_friends)));
				selectAll = true;
			}

			break;
		default:
			break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 100) {
			mDataManager.checkBadgesAlert("", "", "invite_friends", this);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private void selectAll() {
		// Select all
		for (int i = 0; i < mFriendsAdapter.getCount(); i++) {
			mListView.setItemChecked(i, true);
		}

		mFriendsAdapter.notifyDataSetChanged();
	}

	private void cancelAll() {
		// Cancel all
		for (int i = 0; i < mFriendsAdapter.getCount(); i++) {
			mListView.setItemChecked(i, false);
		}

		mFriendsAdapter.notifyDataSetChanged();
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

//                activationCode = (String) responseObjects
//                        .get(ApplicationConfigurations.ACTIVATION_CODE);
//                partnerUserId = (String) responseObjects
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

                hideLoadingDialog();

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

				// CampaignsManager mCampaignsManager =
				// CampaignsManager.getInstance(mContext);
				// mCampaignsManager.getCampignsList();

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
			case (OperationDefinition.Hungama.OperationId.SOCIAL_BADGE_ALERT):

				getFragmentManager().popBackStack();

				hideLoadingDialog();
				break;

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
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onSocializeGetUserInfoListener() {
	}

	@Override
	public void onGigyaLogoutListener() {
	}

	@Override
	public void onFacebookInvite() {

		// Flurry report: Invite friends
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryInvite.Source.toString(),
				mFlurrySource);
		reportMap.put(FlurryConstants.FlurryInvite.Mode.toString(),
				provider.toString());
		reportMap.put(FlurryConstants.FlurryInvite.CountOfFriends.toString(),
				String.valueOf(inviteFriendsCount));
		Analytics.logEvent(FlurryConstants.FlurryInvite.InviteSent.toString(),
				reportMap);

		mDataManager.checkBadgesAlert("", "", "invite_friends", this);
	}

	@Override
	public void onTwitterInvite() {

		// Flurry report: Invite friends
		Map<String, String> reportMap = new HashMap<String, String>();
		reportMap.put(FlurryConstants.FlurryInvite.Source.toString(),
				mFlurrySource);
		reportMap.put(FlurryConstants.FlurryInvite.Mode.toString(),
				provider.toString());
		reportMap.put(FlurryConstants.FlurryInvite.CountOfFriends.toString(),
				String.valueOf(inviteFriendsCount));
		Analytics.logEvent(FlurryConstants.FlurryInvite.InviteSent.toString(),
				reportMap);

		mDataManager.checkBadgesAlert("", "", "invite_friends", this);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {

		SparseBooleanArray positions = mListView.getCheckedItemPositions();

		int numOfChecked = 0;

		for (int i = 0; i < positions.size(); i++) {

			int checkedPosition = positions.keyAt(i);
			boolean checked = positions.valueAt(i);

			if (checked && (provider == SocialNetwork.FACEBOOK)) {
				numOfChecked++;
				if (numOfChecked > MAXIMUM_FACEBOOK_FRIENDS_TO_INVITE) {

					// Show limitation dialog
					showDialog(
							getActivity().getString(
									R.string.facebook_invetation_pick_a_friend),
							getActivity().getString(
									R.string.facebook_inventation_limit_msg));

					// Cancel the last friend that causes the over limit
					mListView.setItemChecked(position, false);

					break;
				}
			}
		}
	}

	private void showDialog(String title, String text) {
		CustomAlertDialog alertDialogBuilder = new CustomAlertDialog(
				getActivity());

		// set title
		alertDialogBuilder
				.setTitle(Utils.getMultilanguageText(mContext, title));

		// set dialog message
		alertDialogBuilder
				.setMessage(Utils.getMultilanguageText(mContext, text))
				.setCancelable(true)
				.setNegativeButton(
						Utils.getMultilanguageText(mContext,
								getString(R.string.exit_dialog_text_ok)),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, just close
								// the dialog box and do nothing
								dialog.cancel();
							}
						});

		// create alert dialog
		// AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialogBuilder.show();
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

	@Override
	public void onCancelRequestListener() {
		mInviteFriendsButton.setEnabled(true);

	}

	@Override
	public void onConnectionRemoved() {

	}

}
