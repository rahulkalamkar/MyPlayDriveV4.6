package com.hungama.myplay.activity.gigya;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.data.dao.hungama.UserProfileResponse;
import com.hungama.myplay.activity.data.dao.hungama.social.ShareURL;
import com.hungama.myplay.activity.gigya.GigyaManager.OnGigyaResponseListener;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMOperation;
import com.hungama.myplay.activity.operations.catchmedia.PartnerConsumerProxyCreateOperation;
import com.hungama.myplay.activity.operations.hungama.GetUserProfileOperation;
import com.hungama.myplay.activity.operations.hungama.SocialBadgeAlertOperation;
import com.hungama.myplay.activity.operations.hungama.SocialGetUrlOperation;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FileUtils;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.ImageCache.ImageCacheParams;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ShareDialogFragment extends DialogFragment implements
		OnClickListener, OnGigyaResponseListener,
		CommunicationOperationListener {

	public static final String FRAGMENT_TAG = "ShareDialogFragment";
	public static final String TRIVIA = "trivia";
	public static final String LYRICS = "lyrics";

	private static final String VALUE = "value";

	// Statics
	public final static String DATA = "data";
	public final static String THUMB_URL_DATA = "thumb_url_data";
	public final static String TITLE_DATA = "title_data";
	public final static String SUB_TITLE_DATA = "sub_title_data";
	public final static String MEDIA_TYPE_DATA = "media_type_data";
	public final static String EDIT_TEXT_DATA = "edit_text_data";
	public final static String TRACK_NUMBER_DATA = "track_number_data";
	public final static String CONTENT_ID_DATA = "content_id_data";
	public final static String TYPE_DATA = "type_data";

	public final static String FLURRY_SOURCE_SECTION = "flurry_source_section";

	// Views
	private Button facebookPostButton;
	private Button twitterPostButton;
	private Button emailPostButton;
	private Button smsPostButton;
	private Button morePostButton;
	private Button postButton;
	private ImageButton closeButton;
	private EditText shareEditText;
	private TextView title;
	private LanguageTextView subTitle;
	private ImageView thumbImageView;
	private ImageView mediaImageType;

	// Data members
	private TwitterLoginFragment mTwitterLoginFragment;
	private Map<String, Object> mData;
	private SocialNetwork provider;

	private String thumbUrlStr;
	private String titleStr;
	private String subTitleStr;
	private String editTextStr;
	private String typeStr;
	private String media;
	private String generatedUrl;

	private Integer trackNubmerStr;

	private Long contentId;

	private MediaType mediaType;

	// Prefix
	private String trackPrefix;
	private String albumPrefix;
	private String playlistPrefix;
	private String artistPrefix;
	private String videoPrefix;
	private String playlistAlbumSuffix;

	private boolean fbClicked = false;
	private boolean ttClicked = false;
    Set<String> tags;
	private String shareViaType = null;

	// Image Fetcher
	// private ImageFetcher mImageFetcher;

	private Context mContext;

	// Managers
	private GigyaManager mGigyaManager;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	private FragmentManager mFragmentManager;
	// private LoadingDialogFragment mLoadingDialogFragment = null;

	private String mFlurrySourceSection;

	public static ShareDialogFragment newInstance(Map<String, Object> data,
			String flurrySourceSection) {
		ShareDialogFragment f = new ShareDialogFragment();

		// Supply data input as an argument.
		Bundle args = new Bundle();
		args.putSerializable(DATA, new HashMap<String, Object>(data));
		args.putString(FLURRY_SOURCE_SECTION, flurrySourceSection);
		f.setArguments(args);

		return f;
	}

	// ======================================================
	// Life cycle callbacks.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity().getApplicationContext();
        tags = Utils.getTags();
		setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_AppCompat_Dialog);

		mGigyaManager = new GigyaManager(getActivity());
		mGigyaManager.setOnGigyaResponseListener(this);
		mGigyaManager.socializeGetUserInfo();

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		mData = (Map<String, Object>) getArguments().getSerializable(DATA);
		mFlurrySourceSection = (String) getArguments().getSerializable(
				FLURRY_SOURCE_SECTION);

		if(mData==null || mData.get(CONTENT_ID_DATA)==null) {
			dismiss();
			return;
		}

		if(mData.get(THUMB_URL_DATA)!=null)
			thumbUrlStr =  mData.get(THUMB_URL_DATA).toString();

		if(mData.get(TITLE_DATA)!=null)
			titleStr = mData.get(TITLE_DATA).toString();

		if(mData.get(SUB_TITLE_DATA)!=null)
			subTitleStr = mData.get(SUB_TITLE_DATA).toString();

		if(mData.get(EDIT_TEXT_DATA)!=null)
			editTextStr =  mData.get(EDIT_TEXT_DATA).toString();

		if(mData.get(MEDIA_TYPE_DATA)!=null)
			mediaType = (MediaType) mData.get(MEDIA_TYPE_DATA);

		if(mData.get(TRACK_NUMBER_DATA)!=null)
			trackNubmerStr = (Integer) mData.get(TRACK_NUMBER_DATA);

		contentId = (Long) mData.get(CONTENT_ID_DATA);

		if(mData.get(TYPE_DATA)!=null)
			typeStr =  mData.get(TYPE_DATA).toString();

		// creates the prefixes.
		trackPrefix = Utils.getMultilanguageText(
				mContext,
				getResources().getString(
						R.string.search_results_layout_bottom_text_for_track));
		albumPrefix = Utils.getMultilanguageText(
				mContext,
				getResources().getString(
						R.string.search_results_layout_bottom_text_for_album));
		playlistPrefix = Utils
				.getMultilanguageText(
						mContext,
						getResources()
								.getString(
										R.string.search_results_layout_bottom_text_for_playlist));
		artistPrefix = Utils.getMultilanguageText(mContext, getResources()
				.getString(R.string.search_result_line_type_and_name_artist));
		videoPrefix = Utils.getMultilanguageText(
				mContext,
				getResources().getString(
						R.string.search_results_layout_bottom_text_for_video));
		playlistAlbumSuffix = Utils
				.getMultilanguageText(
						mContext,
						getResources()
								.getString(
										R.string.search_results_layout_bottom_text_album_playlist));

		// Get the share url
		String contentIdStr = "";

		if (contentId != null) {
			contentIdStr = String.valueOf(contentId);
		}

		mFragmentManager = getActivity().getSupportFragmentManager();

		mDataManager.getShareUrl(contentIdStr, mediaType.toString()
				.toLowerCase(), this);

		ScreenLockStatus.getInstance(mContext).dontShowAd();
	}

	@Override
	public void onStart() {
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(getActivity(),
		// getString(R.string.flurry_app_key));
		Analytics.startSession(getActivity());
		Analytics.onPageView();
	}

	@Override
	public void onStop() {
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_share_dialog, container);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(view, getActivity());
		}
		// Fetch views
		facebookPostButton = (Button) view
				.findViewById(R.id.facebook_post_button);
		twitterPostButton = (Button) view
				.findViewById(R.id.twitter_post_button);
		emailPostButton = (Button) view.findViewById(R.id.email_post_button);
		smsPostButton = (Button) view.findViewById(R.id.sms_post_button);
		morePostButton = (Button) view.findViewById(R.id.more_post_button);
		postButton = (Button) view.findViewById(R.id.post_button);
		closeButton = (ImageButton) view.findViewById(R.id.close_button);

		shareEditText = (EditText) view.findViewById(R.id.share_edit_text);

		thumbImageView = (ImageView) view.findViewById(R.id.thumb_image_view);
		mediaImageType = (ImageView) view.findViewById(R.id.media_image_type);

		title = (TextView) view.findViewById(R.id.title);
		subTitle = (LanguageTextView) view.findViewById(R.id.sub_title);

		// Set listeners
		facebookPostButton.setOnClickListener(this);
		twitterPostButton.setOnClickListener(this);
		emailPostButton.setOnClickListener(this);
		smsPostButton.setOnClickListener(this);
		morePostButton.setOnClickListener(this);
		postButton.setOnClickListener(this);
		closeButton.setOnClickListener(this);

		// Disabling these buttons until socializeGetUserInfo method will
		// triggered it's finish callback
		twitterPostButton
				.setBackgroundResource(R.drawable.icon_twitter_unselected);
		facebookPostButton
				.setBackgroundResource(R.drawable.icon_facebook_unselected);

		twitterPostButton.setEnabled(false);
		facebookPostButton.setEnabled(false);

		// these buttons will be disabled until the share URL will generated via
		// mDataManager.getShareUrl
		emailPostButton.setEnabled(false);
		smsPostButton.setEnabled(false);
		morePostButton.setEnabled(false);
		view.setVisibility(View.GONE);

		try {
			// Set the title text
			title.setText(titleStr);

			// Set the edit text
			if (editTextStr != null)
				shareEditText.setText(editTextStr);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Get the thumb image
		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(),
				DataManager.FOLDER_TILES_CACHE);

		// Set memory cache to 25% of mem class
		cacheParams.setMemCacheSizePercent(getActivity(), 0.25f);

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		// mImageFetcher = new ImageFetcher(getActivity(), 50);
		// mImageFetcher.setLoadingImage(R.drawable.background_home_tile_album_default);
		// mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(),
		// cacheParams);

		// Load it!
		// mImageFetcher.loadImage(thumbUrlStr, thumbImageView);
		try {
			Picasso.with(mContext).cancelRequest(thumbImageView);
			if (mContext != null && thumbUrlStr != null
					&& !TextUtils.isEmpty(thumbUrlStr)) {
				Picasso.with(mContext)
						.load(thumbUrlStr)
						.placeholder(
								R.drawable.background_home_tile_album_default)
						.into(thumbImageView);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (Error e) {
		}

		// Render the sub title and it's suitable icon (track/album/play list)
		if (mediaType == MediaType.TRACK) {
			mediaImageType
					.setBackgroundResource(R.drawable.icon_main_settings_music);
			subTitle.setText(Utils.getMultilanguageTextLayOut(mContext,
					trackPrefix + " - " + subTitleStr));

			media = trackPrefix.toLowerCase();

		} else if (mediaType == MediaType.ALBUM) {
			mediaImageType
					.setBackgroundResource(R.drawable.icon_main_search_album);
			subTitle.setText(Utils.getMultilanguageTextLayOut(
					mContext,
					albumPrefix
							+ " - "
							+ String.valueOf(trackNubmerStr + " "
									+ playlistAlbumSuffix)));

			media = albumPrefix.toLowerCase();

		} else if (mediaType == MediaType.PLAYLIST) {
			mediaImageType
					.setBackgroundResource(R.drawable.icon_home_music_tile_playlist);
			subTitle.setText(Utils.getMultilanguageTextLayOut(
					mContext,
					playlistPrefix
							+ " - "
							+ String.valueOf(trackNubmerStr + " "
									+ playlistAlbumSuffix)));

			media = playlistPrefix.toLowerCase();

		} else if (mediaType == MediaType.ARTIST) {
			mediaImageType
					.setBackgroundResource(R.drawable.icon_main_settings_live_radio);
			subTitle.setText(Utils.getMultilanguageTextLayOut(mContext,
					artistPrefix));

			media = artistPrefix.toLowerCase();

		} else if (mediaType == MediaType.VIDEO) {
			mediaImageType
					.setBackgroundResource(R.drawable.icon_main_settings_videos);
			subTitle.setText(Utils.getMultilanguageTextLayOut(mContext,
					videoPrefix + " - " + subTitleStr));

			media = videoPrefix.toLowerCase();
		}

		try {
			// Set the title text
			title.setText(titleStr);

			// Set the edit text
			if (editTextStr != null)
				shareEditText.setText(editTextStr);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		// if(mImageFetcher != null){
		// mImageFetcher.setExitTasksEarly(false);
		// }
	}

	@Override
	public void onPause() {
		super.onPause();

		// if(mImageFetcher != null){
		// mImageFetcher.setExitTasksEarly(true);
		// }
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// if(mImageFetcher != null){
		// mImageFetcher.closeCache();
		// }
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.close_button:

			dismiss();

			break;
		case R.id.email_post_button:

			shareViaType = FlurryConstants.FlurryShare.Email.toString();

			String editTextStr = "";
			editTextStr = shareEditText.getText().toString();

			if (typeStr != null) {
				if (typeStr.equalsIgnoreCase(LYRICS)) {
					// Share only ~4 lines of the Lyrics text (200 chars)
					editTextStr = editTextStr.substring(0, 200).trim();
					editTextStr = editTextStr + "...";
					media = LYRICS;
				} else if (typeStr.equalsIgnoreCase(TRIVIA)) {
					// Share the whole Trivia text
					media = TRIVIA;
				}
			}

			String subject = getString(R.string.share_subject, media, titleStr);

			String fname = mApplicationConfigurations.getHungmaFirstName();
			String lname = mApplicationConfigurations.getHungamaLastName();

			if (TextUtils.isEmpty(fname) && TextUtils.isEmpty(lname)) {
				if (mGigyaManager.isFBConnected()) {
					fname = mApplicationConfigurations.getGigyaFBFirstName();
					lname = mApplicationConfigurations.getGigyaFBLastName();
				} else if (mGigyaManager.isTwitterConnected()) {
					fname = mApplicationConfigurations
							.getGigyaTwitterFirstName();
					lname = mApplicationConfigurations
							.getGigyaTwitterLastName();
				} else if (mGigyaManager.isGoogleConnected()) {
					fname = mApplicationConfigurations
							.getGigyaGoogleFirstName();
					lname = mApplicationConfigurations.getGigyaGoogleLastName();
				}
			}

			// System.out.println(" ::::::::::::::::::::::: " + fname +
			// " :::::: " + lname);

			String extraText = getString(R.string.share_email_body, media,
					generatedUrl, titleStr, fname, lname, editTextStr);

			// Send Email
			Utils.invokeEmailApp(this, null, subject, extraText);

			dismiss();

			break;

		case R.id.sms_post_button:

			if (getActivity().getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_TELEPHONY)) {

				shareViaType = FlurryConstants.FlurryShare.SMS.toString();

				// THIS PHONE HAS SMS FUNCTIONALITY

				String smsText = getString(R.string.share_sms_text, media,
						generatedUrl, titleStr);

				// Send SMS
				Utils.invokeSMSApp(getActivity(), smsText);

				dismiss();
			} else {
				// NO SMS HERE
				Toast.makeText(getActivity(),
						R.string.share_dialog_no_sms_capabilities,
						Toast.LENGTH_LONG).show();
			}

			break;

		case R.id.more_post_button:
			shareViaType = FlurryConstants.FlurryShare.More.toString();

			String filePath = null;
			try {
				FileUtils fileUtils = new FileUtils(getActivity());
				// create directory and return it or just return it if already
				// exists
				// String hungamaFolder =
				// getResources().getString(R.string.download_media_folder);
				// System.out.println("typeStr :::::::::::::::: " + typeStr);
				if (fileUtils.isExternalStoragePresent() && typeStr == null) {
					MediaContentType mediaContentType = mediaType == MediaType.VIDEO ? MediaContentType.VIDEO
							: MediaContentType.MUSIC;
					File path = fileUtils.getStoragePath(mediaContentType);
					// hungamaCollectionDir = fileUtils.createDirectory(path);
					if (path != null) {
						String mediaFileName;
						if (mediaContentType == MediaContentType.VIDEO) {
							mediaFileName = titleStr + "_"
									+ String.valueOf(contentId) + ".mp4";
						} else {
							mediaFileName = titleStr + "_"
									+ String.valueOf(contentId) + ".mp3";
						}

						String encodedMediaFileName = "";
						try {
							encodedMediaFileName = HungamaApplication
									.encodeURL(mediaFileName, "UTF-8");
						} catch (Exception e) {
							Logger.i("Share Dialog", e.getMessage());
							e.printStackTrace();
						}
						// File file = new File(path, mediaFileName);

						if (fileUtils.isFileInDirectory(path,
								encodedMediaFileName)) {
							filePath = path + "/" + encodedMediaFileName;
					
						}
					}
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}


			if (typeStr != null) {
				if (typeStr.equalsIgnoreCase(LYRICS)) {
					// Share only ~4 lines of the Lyrics text (200 chars)
					// editTextStr = editTextStr.substring(0,200).trim();
					// editTextStr = editTextStr + "...";
					titleStr = this.editTextStr;
					media = LYRICS;
				} else if (typeStr.equalsIgnoreCase(TRIVIA)) {
					// Share the whole Trivia text
					titleStr = this.editTextStr;
					media = TRIVIA;
				}
			}
			// generatedUrl = "";
			String smsText = getString(R.string.share_sms_text, media,
					generatedUrl, titleStr);
			// smsText = smsText.replace("www.hungama.com", "");

			Utils.invokeMoreShareOptions(getActivity(),
			// subject,
			// extraText);
					"", smsText, filePath, mediaType);

			dismiss();

			break;

		case R.id.facebook_post_button:
			shareViaType = FlurryConstants.FlurryShare.Facebook.toString();// xtpl
			facebookButtonClicked();

			break;

		case R.id.twitter_post_button:
			shareViaType = FlurryConstants.FlurryShare.Twitter.toString();// xtpl
			twitterButtonClicked();

			break;

		case R.id.post_button:

			if (fbClicked || ttClicked) {

				StringBuilder providers = new StringBuilder();
				if (fbClicked) {
					providers.append(SocialNetwork.FACEBOOK.toString()
							.toLowerCase());
				}
				if (ttClicked) {

					if (providers.length() != 0) {
						providers.append(",");
					}

					providers.append(SocialNetwork.TWITTER.toString()
							.toLowerCase());
				}

				String shareType;
				if (typeStr != null) {
					shareType = typeStr;
				} else {
					shareType = mediaType.toString().toLowerCase();
				}

				// contentId: media item id
				// type: "facebook" or "twitter", if both then
				// "facebook,twitter"
				// provider: track video album playlist lyrics trivia
				// userText:

				String encodedTextToPost = "";
				String editTextToPost = shareEditText.getText().toString();

				if (!TextUtils.isEmpty(editTextToPost)) {
					try {
						encodedTextToPost = URLEncoder.encode(shareEditText
								.getText().toString(), "UTF-8");

					} catch (UnsupportedEncodingException e) {

						e.printStackTrace();
					}
				}

				// Post it!
				mDataManager.share(contentId.intValue(), shareType,
						providers.toString(), encodedTextToPost, this);

				shareViaType = providers.toString();

			} else {

				// Non of facebook or twitter selected
				Utils.makeText(getActivity(),
						getString(R.string.please_select_social_network),
						Toast.LENGTH_LONG).show();
			}

			break;

		default:
			break;
		}

		if (shareViaType != null) {
			// Flurry report: share button
			Map<String, String> reportMap = new HashMap<String, String>();
			reportMap.put(FlurryConstants.FlurryShare.SourceSection.toString(),
					mFlurrySourceSection);
			reportMap.put(FlurryConstants.FlurryShare.Title.toString(),
					titleStr);
			reportMap.put(FlurryConstants.FlurryShare.Type.toString(),
					mediaType.toString());
			reportMap.put(FlurryConstants.FlurryShare.ShareVia.toString(),
					shareViaType);
			Analytics.logEvent(
					FlurryConstants.FlurryShare.ShareButton.toString(),
					reportMap);
		}
	}

	// ======================================================
	// Communication Callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
		if (operationId != OperationDefinition.Hungama.OperationId.SOCIAL_BADGE_ALERT)
			showLoadingDialog(R.string.processing);
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
				// let's party!
				mDataManager.createDeviceActivationLogin(activationCode, this);

				mApplicationConfigurations.setIsUserRegistered(true);
				mDataManager.getUserProfileDetail(this);

				 tags = Utils.getTags();
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
                // let's party!
//                mDataManager.createDeviceActivationLogin(activationCode, this);
//				ApsalarEvent.postEvent(getActivity(), ApsalarEvent.LOGIN_COMPLETED);
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

                if (provider == SocialNetwork.FACEBOOK) {

                    facebookPostButton
                            .setBackgroundResource(R.drawable.icon_facebook_selected);
                    fbClicked = true;

                } else if (provider == SocialNetwork.TWITTER) {

                    twitterPostButton
                            .setBackgroundResource(R.drawable.icon_twitter_selected);
                    ttClicked = true;
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
				break;

			case (OperationDefinition.CatchMedia.OperationId.DEVICE_ACTIVATION_LOGIN_CREATE):
//				ApsalarEvent.postEvent(getActivity(), ApsalarEvent.LOGIN_COMPLETED);
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

				// try{
				// CampaignsManager mCampaignsManager =
				// CampaignsManager.getInstance(getActivity().getApplicationContext());
				// mCampaignsManager.clearCampaigns();
				// mCampaignsManager.getCampignsList();
				// } catch (Exception e) {
				// Logger.e(getClass().getName()+":573", e.toString());
				// }

				if (mTwitterLoginFragment != null) {
					mTwitterLoginFragment.finish();
				}

				if (provider == SocialNetwork.FACEBOOK) {

					facebookPostButton
							.setBackgroundResource(R.drawable.icon_facebook_selected);
					fbClicked = true;

				} else if (provider == SocialNetwork.TWITTER) {

					twitterPostButton
							.setBackgroundResource(R.drawable.icon_twitter_selected);
					ttClicked = true;
				}

				break;

			case (OperationDefinition.Hungama.OperationId.SOCIAL_SHARE):
				try {
					// Flurry report: Share completed
					Map<String, String> reportMap = new HashMap<String, String>();
					reportMap.put(FlurryConstants.FlurryShare.SourceSection
							.toString(), mFlurrySourceSection);
					reportMap.put(FlurryConstants.FlurryShare.Title.toString(),
							titleStr);
					reportMap.put(FlurryConstants.FlurryShare.Type.toString(),
							mediaType.toString());
					reportMap.put(
							FlurryConstants.FlurryShare.ShareVia.toString(),
							shareViaType);
					Analytics.logEvent(
							FlurryConstants.FlurryShare.ShareCompleted
									.toString(), reportMap);

					dismiss();
				} catch (Exception e) {
					Logger.e(getClass().getName() + ":605", e.toString());
				}
				break;

			case (OperationDefinition.Hungama.OperationId.SOCIAL_GET_URL):
				try {
					ShareURL shareURL = (ShareURL) responseObjects
							.get(SocialGetUrlOperation.RESULT_KEY_GET_SOCIAL_URL);
					if (shareURL != null) {
						generatedUrl = shareURL.url;
						// generatedUrl = "market://com.hungama.myplay.activity/" +
						// generatedUrl;

						mDataManager.checkBadgesAlert("" + contentId, mediaType
										.toString().toLowerCase(),
								SocialBadgeAlertOperation.ACTION_SHARE, this);
					}
					String tag;
					tags = Utils.getTags();
					if (mediaType == MediaType.VIDEO) {
						tag = Constants.UA_TAG_SHARED_VIDEO;
						if (!tags.contains(tag)) {
							tags.add(tag);
							Utils.AddTag(tags);
						}


					} else if (mediaType == MediaType.TRACK) {

						tag = Constants.UA_TAG_SHARED_SONGS;
						if (!tags.contains(tag)) {
							tags.add(tag);
							Utils.AddTag(tags);
						}

					} else if (mediaType == MediaType.ALBUM) {
						tag = Constants.UA_TAG_SHARED_ALBUM;
						if (!tags.contains(tag)) {
							tags.add(tag);
							Utils.AddTag(tags);
						}

					} else if (mediaType == MediaType.PLAYLIST) {
						tag = Constants.UA_TAG_SHARED_PLAYLIST;
						if (!tags.contains(tag)) {
							tags.add(tag);
							Utils.AddTag(tags);
						}
					}
					emailPostButton.setEnabled(true);
					smsPostButton.setEnabled(true);
					morePostButton.setEnabled(true);
					morePostButton.performClick();
				} catch (Exception e){
					Logger.printStackTrace(e);
				}
				break;
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":619", e.toString());
		}
		try {
			if (operationId != OperationDefinition.Hungama.OperationId.SOCIAL_BADGE_ALERT)
				hideLoadingDialog();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId != OperationDefinition.Hungama.OperationId.SOCIAL_BADGE_ALERT)
			hideLoadingDialog();

		switch (operationId) {
		case (OperationDefinition.Hungama.OperationId.SOCIAL_GET_URL):
			// No URL exist in response
			generatedUrl = "";
			emailPostButton.setEnabled(true);
			smsPostButton.setEnabled(true);
			morePostButton.setEnabled(true);

			break;

		case (OperationDefinition.CatchMedia.OperationId.PARTNER_CONSUMER_PROXY_CREATE):
        case (OperationDefinition.CatchMedia.OperationId.CONSUMER_DEVICE_LOGIN):
			mGigyaManager.cancelGigyaProviderLogin();

			if (!TextUtils.isEmpty(errorMessage) && getActivity() != null) {
				Toast.makeText(getActivity(), errorMessage, Toast.LENGTH_SHORT)
						.show();
			}
			break;

		default:
			break;
		}

	}

	// ======================================================
	// Helper methods.
	// ======================================================

	private void facebookButtonClicked() {

		if (fbClicked) {
			fbClicked = false;
			facebookPostButton
					.setBackgroundResource(R.drawable.icon_invite_facebook);

		} else {

			if (mGigyaManager.isFBConnected()) {
				fbClicked = true;
				facebookPostButton
						.setBackgroundResource(R.drawable.icon_facebook_selected);
			} else {
				mGigyaManager.facebookLogin();
			}
		}
	}

	private void twitterButtonClicked() {

		if (ttClicked) {
			ttClicked = false;
			twitterPostButton
					.setBackgroundResource(R.drawable.icon_invite_twitter);

		} else {

			if (mGigyaManager.isTwitterConnected()) {
				ttClicked = true;
				twitterPostButton
						.setBackgroundResource(R.drawable.icon_twitter_selected);
			} else {
				mGigyaManager.twitterLogin();
			}
		}
	}

	@Override
	public void onGigyaLoginListener(SocialNetwork provider,
			Map<String, Object> signupFields, long setId) {

		if (provider == SocialNetwork.TWITTER) {
			// Twitter

			// FragmentManager mFragmentManager = getFragmentManager();
			// FragmentTransaction fragmentTransaction =
			// mFragmentManager.beginTransaction();
			// fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
			// R.anim.slide_left_exit,
			// R.anim.slide_right_enter,
			// R.anim.slide_right_exit);
			//
			// TwitterLoginFragment fragment = new
			// TwitterLoginFragment(signupFields, setId);
			// fragmentTransaction.replace(R.id.main_fragmant_container,
			// fragment);
			// fragmentTransaction.addToBackStack(TwitterLoginFragment.class.toString());
			// fragmentTransaction.commit();
			//
			// // Listen to result from TwitterLoinFragment
			// fragment.setOnTwitterLoginListener(this);

			Intent i = new Intent(getActivity(), TwitterLoginActivity.class);
			Bundle b = new Bundle();
			b.putSerializable("signup_fields", (Serializable) signupFields);
			b.putLong("set_id", setId);
			i.putExtras(b);
			startActivityForResult(i, 0);

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

		this.provider = provider;

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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == 200) {
			// Call PCP
			// It's include the email and password that user insert in
			// TwitterLoginFragment
			Bundle b = data.getExtras();
			mDataManager.createPartnerConsumerProxy(
					(Map<String, Object>) b.getSerializable("signup_fields"),
					b.getLong("set_id"), this, false);

		} else if (resultCode == 500) {
			mGigyaManager.removeConnetion(SocialNetwork.TWITTER);
		}

		super.onActivityResult(requestCode, resultCode, data);
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
	public void onSocializeGetUserInfoListener() {
		try {
			if (mGigyaManager.isFBConnected()) {
				facebookPostButton
						.setBackgroundResource(R.drawable.icon_facebook_selected);
				fbClicked = true;
			} else {
				facebookPostButton
						.setBackgroundResource(R.drawable.icon_facebook_unselected);
			}

			if (mGigyaManager.isTwitterConnected()) {
				twitterPostButton
						.setBackgroundResource(R.drawable.icon_twitter_selected);
				ttClicked = true;
			} else {
				twitterPostButton
						.setBackgroundResource(R.drawable.icon_twitter_unselected);
			}

			twitterPostButton.setEnabled(true);
			facebookPostButton.setEnabled(true);
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":790", e.toString());
		}
	}

	protected void showLoadingDialog(int messageResource) {
		try {
			((MainActivity) getActivity())
					.showLoadingDialog(R.string.application_dialog_loading_content);

			// if (mLoadingDialogFragment == null && getActivity() != null &&
			// !getActivity().isFinishing()) {
			// mLoadingDialogFragment =
			// LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
			// mLoadingDialogFragment.setCancelable(true);
			// mLoadingDialogFragment.show(mFragmentManager,
			// LoadingDialogFragment.FRAGMENT_TAG);
			// }
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":804", e.toString());
		}
	}

	protected void showLoadingDialogWithoutVisibleCheck(int messageResource) {
		try {
			((MainActivity) getActivity())
					.showLoadingDialog(R.string.application_dialog_loading_content);
		} catch (Exception e) {
			// TODO: handle exception
		}

		// if (mLoadingDialogFragment == null && getActivity() != null &&
		// !getActivity().isFinishing()) {
		// mLoadingDialogFragment =
		// LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
		// mLoadingDialogFragment.setCancelable(true);
		// mLoadingDialogFragment.show(mFragmentManager,
		// LoadingDialogFragment.FRAGMENT_TAG);
		// }
	}

	protected void hideLoadingDialog() {
		try {
			((MainActivity) getActivity()).hideLoadingDialog();
		} catch (Exception e) {
		}

		// if (mLoadingDialogFragment != null && getActivity() != null &&
		// !getActivity().isFinishing()) {
		// FragmentTransaction fragmentTransaction =
		// mFragmentManager.beginTransaction();
		// fragmentTransaction.remove(mLoadingDialogFragment);
		// fragmentTransaction.commitAllowingStateLoss();
		// mLoadingDialogFragment = null;
		// }
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
