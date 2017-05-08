package com.hungama.myplay.activity.data;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.google.gson.Gson;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CacheManager.Callback;
import com.hungama.myplay.activity.data.CacheManager.ReadCallback;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.DownloadOperationType;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.RedeemCouponType;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileLeaderboard;
import com.hungama.myplay.activity.data.dao.hungama.social.StreamItemCategory;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.data.events.Event;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.CampaignCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.ConsumerDeviceLoginOperation;
import com.hungama.myplay.activity.operations.catchmedia.DeviceActivationLoginCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.DeviceCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;
import com.hungama.myplay.activity.operations.catchmedia.PartnerInfoReadOperation;
import com.hungama.myplay.activity.operations.catchmedia.ThirdPartyTokenUpdate;
import com.hungama.myplay.activity.operations.catchmedia.TimeReadOperation;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.DiscoverSearchResultsOperation;
import com.hungama.myplay.activity.operations.hungama.DownloadOperation;
import com.hungama.myplay.activity.operations.hungama.FeedbackSubjectsOperation;
import com.hungama.myplay.activity.operations.hungama.FeedbackSubmitOperation;
import com.hungama.myplay.activity.operations.hungama.ForgotPasswordOperation;
import com.hungama.myplay.activity.operations.hungama.GetPromoUnitOperation;
import com.hungama.myplay.activity.operations.hungama.GetUserProfileOperation;
import com.hungama.myplay.activity.operations.hungama.HashResultOperation;
import com.hungama.myplay.activity.operations.hungama.HashTagListOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaWrapperOperation;
import com.hungama.myplay.activity.operations.hungama.LanguageListSettingsOperation;
import com.hungama.myplay.activity.operations.hungama.LanguagePostOperation;
import com.hungama.myplay.activity.operations.hungama.LanguageSelectedGetOperation;
import com.hungama.myplay.activity.operations.hungama.LeftMenuOperation;
import com.hungama.myplay.activity.operations.hungama.LiveRadioDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.MediaCategoriesOperation;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperationPaging;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.MultiSongDetailOperation;
import com.hungama.myplay.activity.operations.hungama.MultiSongHistoryOperation;
import com.hungama.myplay.activity.operations.hungama.MyStreamSettingsOperation;
import com.hungama.myplay.activity.operations.hungama.NewVersionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.PlaylistIdOperation;
import com.hungama.myplay.activity.operations.hungama.PreferencesRetrieveOperation;
import com.hungama.myplay.activity.operations.hungama.PreferencesSaveOperation;
import com.hungama.myplay.activity.operations.hungama.RadioLiveStationsOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistSongsOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistsOperation;
import com.hungama.myplay.activity.operations.hungama.RedeemCouponOperation;
import com.hungama.myplay.activity.operations.hungama.RelatedVideoOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SearchAutoSuggestOperation;
import com.hungama.myplay.activity.operations.hungama.SearchKeyboardOperation;
import com.hungama.myplay.activity.operations.hungama.SearchPopularKeywordOperation;
import com.hungama.myplay.activity.operations.hungama.ShareOperation;
import com.hungama.myplay.activity.operations.hungama.ShareSettingsOperation;
import com.hungama.myplay.activity.operations.hungama.SocialBadgeAlertOperation;
import com.hungama.myplay.activity.operations.hungama.SocialCommentsListingOperation;
import com.hungama.myplay.activity.operations.hungama.SocialCommentsPostOperation;
import com.hungama.myplay.activity.operations.hungama.SocialGetUrlOperation;
import com.hungama.myplay.activity.operations.hungama.SocialMyCollectionOperation;
import com.hungama.myplay.activity.operations.hungama.SocialMyStreamOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileBadgesOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileLeaderboardOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionTelcoOperation;
import com.hungama.myplay.activity.operations.hungama.TestApiOperation;
import com.hungama.myplay.activity.operations.hungama.TrackLyricsOperation;
import com.hungama.myplay.activity.operations.hungama.TrackSimilarOperation;
import com.hungama.myplay.activity.operations.hungama.TrackTriviaOperation;
import com.hungama.myplay.activity.operations.hungama.VideoStreamingOperationAdp;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayingQueue;
import com.hungama.myplay.activity.playlist.PlaylistOperation;
import com.hungama.myplay.activity.playlist.PlaylistRequest;
import com.hungama.myplay.activity.services.ImagePrefetchingService;
import com.hungama.myplay.activity.services.InventoryLightService;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import org.json.JSONArray;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Global application's data provider.
 */
public class DataManager {

	private static final String TAG = "DataManager";

	public static final String FOLDER_MOODS_IMAGES = CacheManager.FOLDER_MOODS_IMAGES;
	public static final int CACHE_SIZE_MOODS_IMAGES = CacheManager.CACHE_SIZE_MOODS_IMAGES;
	public static final String FOLDER_APPLICATION_IMAGES = CacheManager.FOLDER_APPLICATION_IMAGES;

	public static final String FOLDER_TILES_CACHE = "tiles";
	// public static final String FOLDER_PLAYER_MEDIA_ART_CACHE =
	// "player_media_art";
	public static final String FOLDER_THUMBNAILS_CACHE = "thumbnails";
	// public static final String FOLDER_CAMPAIGNS_CACHE = "campaigns";
	public static final String FOLDER_THUMBNAILS_FRIENDS = "social_friends_thumbnail";

	// public static final String DEVICE = "android";

	private static final String VALUE = "value";

	private static DataManager sIntance;
	private boolean mIsTimeReadAlreadyCalled;

	private Context mContext;

	private ServerConfigurations mServerConfigurations;
	private final ApplicationConfigurations mApplicationConfigurations;
	private final DeviceConfigurations mDeviceConfigurations;

	private CacheManager mCacheManager;
	private EventManager mEventManager;

	private static String mDeviceDensity;

	private CommunicationManager mMediaDetailsCommunicationManager = null;

	private CommunicationManager mTrackSimilarCommunicationManager = null;
	private CommunicationManager mTrackLyricsCommunicationManager = null;
	private CommunicationManager mTrackTriviaCommunicationManager = null;

	private CommunicationManager mSearchSuggestedCommunicationManager = null;
	private CommunicationManager mSearchCommunicationManager = null;
	private CommunicationManager mRelatedVideoCommunicationManager = null;
	private CommunicationManager mSubscriptionPlansCommunicationManager = null;
	private CommunicationManager mRedeemCouponsCommunicationManager = null;

	// ======================================================
	// General.
	// ======================================================

	public static final synchronized DataManager getInstance(
			Context applicationContext) {
		if (sIntance == null) {
			sIntance = new DataManager(applicationContext);
		}
		return sIntance;
	}

	private void clearInstance() {
		sIntance = null;
		// System.gc();
	}

	private boolean ismIsTimeReadAlreadyCalled() {
		return mIsTimeReadAlreadyCalled;
	}

	public void setmIsTimeReadAlreadyCalled(boolean mIsTimeReadAlreadyCalled) {
		this.mIsTimeReadAlreadyCalled = mIsTimeReadAlreadyCalled;
	}

	private DataManager(Context context) {
		mContext = context;

		// initializes application's configuration managers.
		mServerConfigurations = ServerConfigurations.getInstance(mContext);
		mApplicationConfigurations = ApplicationConfigurations
				.getInstance(mContext);
		mDeviceConfigurations = DeviceConfigurations.getInstance(mContext);

		// initializes application's resources managers.
		mCacheManager = new CacheManager(mContext);

		// sets the scale name
		mDeviceDensity = getDisplayDensity();

		// sets the time read to false for the first time.
		setmIsTimeReadAlreadyCalled(false);
	}

	public Context getApplicationContext() {
		return mContext;
	}

	public ServerConfigurations getServerConfigurations() {
		return mServerConfigurations;
	}

	public ApplicationConfigurations getApplicationConfigurations() {
		return mApplicationConfigurations;
	}

	public DeviceConfigurations getDeviceConfigurations() {
		return mDeviceConfigurations;
	}

	public static String getDisplayDensityLabel() {
		return mDeviceDensity;
	}

	/**
	 * Notifies the module that the application has been in the process to be
	 * started when the user clicked on it's launcher icon.
	 */
	public synchronized void notifyApplicationStarts() {
		Logger.e("EventTrack", "DM :notifyApplicationStarts ");

		List<Event> events = getStoredEvents();
		mEventManager = EventManager.getInstance(mContext,
				mServerConfigurations.getServerUrl(), events);
		// flush all stored evens.
		if (isDeviceOnLine())
			mEventManager.flushEvents();
	}

	//
	// // /**
	// // * Notifies the module that the application has been in the process to
	// be
	// // * started when the user clicked on it's launcher icon.
	// // */
	// public void flushEvents() {
	// if (!ExitProcessing) {
	// Logger.e("EventTrack", "DM :flushEvents ");
	//
	// if (mEventManager != null) {
	// notifyApplicationExits();
	// } else {
	// ExitProcessing = true;
	// List<Event> events = getStoredEvents();
	// mEventManager = EventManager.getInstance(mContext,
	// mServerConfigurations.getServerUrl(), events);
	// // flush all stored evens.
	// if (isDeviceOnLine()) {
	// mEventManager.flushEvents();
	// mEventManager = null;
	// } else {
	// List<Event> pandingEvents = mEventManager.getEvents();
	// if (!Utils.isListEmpty(pandingEvents)) {
	// storeEvents(pandingEvents, false);
	// }
	// }
	// mEventManager.clearQueue();
	//
	// }
	// }
	// }

	// boolean ExitProcessing = false;

	/**
	 * Notifies the module that the application has been in the process to be
	 * finished when the user has clicked the last "Back" button to exit it.
	 */
	public void notifyApplicationExits() {
		Logger.e("EventTrack", "DM :notifyApplicationExits ");
		// ExitProcessing = true;
		if (mEventManager != null) {
			// stop any posting events and stores the rest.
			mEventManager.stopPostingEvents();
			if (isDeviceOnLine()) {
				mEventManager.flushEvents();
//				mEventManager = null;
			} else {
				// stores the events in the internal storage.
				List<Event> pandingEvents = mEventManager.getEvents();
				if (!Utils.isListEmpty(pandingEvents)) {
					storeEvents(pandingEvents, false);
				}
                // clears the queue.
                mEventManager.clearQueue();
			}
//			// clears the queue.
//			mEventManager.clearQueue();
		}
		clearInstance();
		// ExitProcessing = false;

	}

	// ======================================================
	// Application internal data flow methods.
	// ======================================================

	public void createDevice(CommunicationOperationListener listener) {

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new CMDecoratorOperation(
				mServerConfigurations.getServerUrl(),
				new DeviceCreateOperation(getApplicationContext())), listener,
				mContext);
	}

	public void readPartnerInfo(CommunicationOperationListener listener) {

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new CMDecoratorOperation(
				mServerConfigurations.getServerUrl(),
				new PartnerInfoReadOperation(getApplicationContext())),
				listener, mContext);
	}

	/**
	 * Posts signup / login fields to retrieve an activation code for the
	 * application.
	 */
	public void createPartnerConsumerProxy(Map<String, Object> signupFields,
			long setId, CommunicationOperationListener listener,
			boolean isSkipSelected) {
		// populates the hidden fields.
//        if(Logger.enableConsumerDeviceLogin){
            createConsumerDeviceLogin(signupFields, setId, listener, isSkipSelected);
//        } else {
//            if (signupFields.containsKey(DeviceConfigurations.HARDWARE_ID)) {
//                // creates the new value for it.
//                Map<String, Object> valueMap = new HashMap<String, Object>();
//                valueMap.put(VALUE, mDeviceConfigurations.getHardwareId());
//                // override the existing one.
//                signupFields.put(DeviceConfigurations.HARDWARE_ID, valueMap);
//            }
//
//            if (signupFields.containsKey(ApplicationConfigurations.PARTNER_USER_ID)) {
//                // creates the new value for it.
//                Map<String, Object> valueMap = new HashMap<String, Object>();
//                valueMap.put(VALUE, mApplicationConfigurations.getPartnerUserId());
//                // override the existing one.
//                signupFields.put(ApplicationConfigurations.PARTNER_USER_ID,
//                        valueMap);
//            }
//
//            // performs the execution to the web service.
//            CommunicationManager communicationManager = new CommunicationManager();
//            communicationManager.performOperationAsync(new CMDecoratorOperation(
//                    mServerConfigurations.getServerUrl(),
//                    new PartnerConsumerProxyCreateOperation(mContext, signupFields,
//                            setId, isSkipSelected)), listener, mContext);
//        }
	}

    /**
     * Posts signup / login fields to retrieve an activation code for the
     * application.
     */
    public void createConsumerDeviceLogin(Map<String, Object> signupFields,
                                           long setId, CommunicationOperationListener listener,
                                           boolean isSkipSelected) {
        // populates the hidden fields.

        if (signupFields.containsKey(DeviceConfigurations.HARDWARE_ID)) {
            // creates the new value for it.
            Map<String, Object> valueMap = new HashMap<String, Object>();
            valueMap.put(VALUE, mDeviceConfigurations.getHardwareId());
            // override the existing one.
            signupFields.put(DeviceConfigurations.HARDWARE_ID, valueMap);
        }

        if (signupFields.containsKey(ApplicationConfigurations.PARTNER_USER_ID)) {
            // creates the new value for it.
            Map<String, Object> valueMap = new HashMap<String, Object>();
            valueMap.put(VALUE, mApplicationConfigurations.getPartnerUserId());
            // override the existing one.
            signupFields.put(ApplicationConfigurations.PARTNER_USER_ID,
                    valueMap);
        }

        // performs the execution to the web service.
        CommunicationManager communicationManager = new CommunicationManager();
        communicationManager.performOperationAsync(new CMDecoratorOperation(
                mServerConfigurations.getServerUrl(),
                new ConsumerDeviceLoginOperation(mContext, signupFields,
                        setId, isSkipSelected)), listener, mContext);
    }

	public void createDeviceActivationLogin(String activationCode,
			CommunicationOperationListener listener) {

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new CMDecoratorOperation(
				mServerConfigurations.getServerUrl(),
				new DeviceActivationLoginCreateOperation(mContext,
						activationCode)), listener, mContext);
	}

	public void forgotPassword(String userEmail,
			CommunicationOperationListener listener) {

		String serviceUrl = mContext.getResources().getString(
				R.string.hungama_forgot_password_server_url);
		String forgotPasswordAuthKey = mContext.getResources().getString(
				R.string.hungama_forgot_password_key);

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new ForgotPasswordOperation(
				serviceUrl, forgotPasswordAuthKey, userEmail), listener,
				mContext);
	}

	// ======================================================
	// Campaign
	// ======================================================

	// public void getCampignsList(CommunicationOperationListener listener) {
	//
	// if (isDeviceOnLine()) {
	//
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager.performOperationAsync(
	// new CMDecoratorOperation(mServerConfigurations
	// .getServerUrl(), new CampaignListCreateOperation(
	// mContext)),
	// new WrapperCampaignListOperationListener(listener),
	// mContext);
	// } else {
	//
	// listener.onStart(OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ);
	//
	// List<String> list = getStoredCampaignList();
	// if (list != null) {
	//
	// Map<String, Object> resultMap = new HashMap<String, Object>();
	// resultMap
	// .put(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST,
	// list);
	//
	// listener.onSuccess(
	// OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ,
	// resultMap);
	// } else {
	// // No internet connectivity and no cached media items, failure.
	// listener.onFailure(
	// OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ,
	// ErrorType.NO_CONNECTIVITY,
	// Utils.getMultilanguageText(
	// mContext,
	// mContext.getResources()
	// .getString(
	// R.string.application_error_no_connectivity)));
	// }
	// }
	// }

	// public void getCampigns(CommunicationOperationListener listener,
	// List<String> campaignList) {
	//
	// if (isDeviceOnLine()) {
	// if (campaignList != null && !campaignList.isEmpty()) {
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager.performOperationAsync(
	// new CMDecoratorOperation(mServerConfigurations
	// .getServerUrl(), new CampaignCreateOperation(
	// mContext, campaignList)),
	// new WrapperCampaignOperationListener(listener),
	// mContext);
	// } else {
	// // Fake empty Campaigns cause the call to CamapignsList got back
	// // empty
	// Map<String, Object> resultMap = new HashMap<String, Object>();
	// listener.onSuccess(
	// OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ,
	// resultMap);
	// }
	//
	// } else {
	// listener.onStart(OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ);
	//
	// List<Campaign> list = getStoredCampaign();
	// if (list != null) {
	// Map<String, Object> resultMap = new HashMap<String, Object>();
	// resultMap.put(
	// CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN,
	// list);
	//
	// listener.onSuccess(
	// OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ,
	// resultMap);
	// } else {
	// // No internet connectivity and no cached media items, failure.
	// listener.onFailure(
	// OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ,
	// ErrorType.NO_CONNECTIVITY,
	// Utils.getMultilanguageText(
	// mContext,
	// mContext.getResources()
	// .getString(
	// R.string.application_error_no_connectivity)));
	// }
	//
	// }
	// }

	// ======================================================
	// Playlist
	// ======================================================

	public void playlistOperation(CommunicationOperationListener listener,
			long playlistId, String playlistName, String trackList,
			JsonRPC2Methods method) {
		if (isDeviceOnLine()) {
			CommunicationManager communicationManager = new CommunicationManager();
			communicationManager.performOperationAsync(
					new CMDecoratorOperation(mServerConfigurations
							.getServerUrl(), new PlaylistOperation(
							getApplicationContext(), playlistId, playlistName,
							trackList, method)), listener, mContext);
		}
	}

	// ======================================================
	// Time Read from CM
	// ======================================================
	public void getTimeRead(CommunicationOperationListener listener) {

		if (!ismIsTimeReadAlreadyCalled()) {

			if (isDeviceOnLine()) {
				CommunicationManager communicationManager = new CommunicationManager();
				communicationManager.performOperationAsync(
						new CMDecoratorOperation(mServerConfigurations
								.getServerUrl(),
								new TimeReadOperation(mContext)), listener,
						mContext);
				setmIsTimeReadAlreadyCalled(true);

				// Toast.makeText(getApplicationContext(),
				// "Time read called ! ",
				// Toast.LENGTH_LONG).show();
			}
		}
	}

	// ======================================================
	// Application data getters - Media getters.
	// ======================================================
	private CommunicationManager mMediaItemsPagingCM;


	public void getMediaItemsPaging(MediaContentType mediaContentType,
			MediaCategoryType mediaCategoryType, Category category,
			String start, String length,
			CommunicationOperationListener listener, String timestamp_cache) {

		String images = null;

		if (mediaContentType == MediaContentType.MUSIC)
			images = ImagesManager.getImageSize(ImagesManager.HOME_MUSIC_TILE,
					getDisplayDensity()) + (Logger.hardcodePlaylistImage ? ",100x100" : "");
		else if (mediaContentType == MediaContentType.VIDEO)
			images = ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
					getDisplayDensity());

		final MediaContentOperationPaging mediaContentOperationPaging = new MediaContentOperationPaging(
				mContext, mServerConfigurations.getHungamaServerUrl_2(),
				mServerConfigurations.getHungamaAuthKey(), mediaContentType,
				mediaCategoryType, category,
				mApplicationConfigurations.getPartnerUserId(), start, length,
				timestamp_cache, images);

		// final Handler handle = new Handler(Looper.getMainLooper());
		// String response = null;
		// if (mediaContentType == MediaContentType.MUSIC
		// && mediaCategoryType == MediaCategoryType.LATEST
		// && start.equals(String.valueOf(1))) {
		// response = mCacheManager.getMusicLatestResponse();
		// } else if (mediaContentType == MediaContentType.MUSIC
		// && mediaCategoryType == MediaCategoryType.POPULAR
		// && start.equals(String.valueOf(1))) {
		// response = mCacheManager.getMusicFeaturedResponse();
		// } else if (mediaContentType == MediaContentType.VIDEO
		// && mediaCategoryType == MediaCategoryType.LATEST
		// && start.equals(String.valueOf(1))) {
		// response = mCacheManager.getVideoLatestResponse();
		// }
		//
		// if (!TextUtils.isEmpty(response)) {
		// final CommunicationOperationListener listenerOld = listener;
		// listener = new CommunicationOperationListener() {
		// @Override
		// public void onSuccess(int operationId,
		// Map<String, Object> responseObjects) {
		// }
		//
		// @Override
		// public void onStart(int operationId) {
		// }
		//
		// @Override
		// public void onFailure(int operationId, ErrorType errorType,
		// String errorMessage) {
		// }
		// };
		// final Response finalResponse = new Response();
		// finalResponse.response = response;
		// finalResponse.responseCode =
		// CommunicationManager.RESPONSE_SUCCESS_200;
		//
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// listenerOld.onStart(mediaContentOperationPaging
		// .getOperationId());
		//
		// try {
		//
		// final Map<String, Object> map = mediaContentOperationPaging
		// .parseResponseFromCache(finalResponse, true);
		// handle.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// listenerOld.onSuccess(
		// mediaContentOperationPaging
		// .getOperationId(), map);
		// }
		// }, threadsleep);
		// } catch (InvalidRequestParametersException e) {
		// e.printStackTrace();
		// } catch (InvalidRequestTokenException e) {
		// e.printStackTrace();
		// } catch (InvalidResponseDataException e) {
		// e.printStackTrace();
		// } catch (OperationCancelledException e) {
		// e.printStackTrace();
		// }
		// }
		// }).start();
		//
		// }

		// performs server call to get the media items.
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, mediaContentOperationPaging), listener,
				mContext);

	}

	public void cancelGetMediaItemsPaging() {
		if (mMediaItemsPagingCM != null
				&& mMediaItemsPagingCM.isRunning()) {
			mMediaItemsPagingCM.cancelAnyRunningOperation();
			mMediaItemsPagingCM = null;
		}
	}

	public static int getOperationIdForMediaCategoryType(
			MediaCategoryType mediaCategoryType) {

		if (mediaCategoryType == MediaCategoryType.LATEST) {
			return OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST;
		}

		if (mediaCategoryType == MediaCategoryType.POPULAR) {
			return OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED;
		}

		return OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_RECOMMANDED;
	}

	public void storeMediaItems(MediaContentType mediaContentType,
			MediaCategoryType mediaCategoryType, List<MediaItem> mediaItems) {
		mCacheManager.storeMediaItems(mediaContentType, mediaCategoryType,
				mediaItems);
	}

	public static MediaItem mediaItem;
	public static PlayerOption playerOption;
	public static CommunicationOperationListener listener;

	public void getMediaDetails(MediaItem mediaItem, PlayerOption playerOption,
			CommunicationOperationListener listener) {
		String images = ImagesManager.getImageSize(
				ImagesManager.MUSIC_ART_SMALL, getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_BIG,
						getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
						getDisplayDensity());

		if (mediaItem.getMediaType() == MediaType.ALBUM) {
			images = ImagesManager.getImageSize(
					ImagesManager.MUSIC_ART_SMALL, getDisplayDensity())
					+ ","
					+ ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
					getDisplayDensity());
		} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
			images = ImagesManager.getImageSize(
					ImagesManager.MUSIC_ART_SMALL, getDisplayDensity());
		} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO
				&& mediaItem.getMediaType() == MediaType.TRACK) {
			images = ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
					getDisplayDensity());
		} else if (mediaItem.getMediaType() == MediaType.TRACK) {
			images = ImagesManager.getImageSize(
					ImagesManager.MUSIC_ART_SMALL, getDisplayDensity())
					+ ","
					+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_BIG,
					getDisplayDensity())
					+ ","
					+ ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
					getDisplayDensity());
		} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO
				&& mediaItem.getMediaType() == MediaType.VIDEO) {
			images = ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
					getDisplayDensity());
		}

		DataManager.mediaItem = mediaItem;
		DataManager.playerOption = playerOption;
		DataManager.listener = listener;
		if (mMediaDetailsCommunicationManager == null)
			mMediaDetailsCommunicationManager = new CommunicationManager();
		mMediaDetailsCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new MediaDetailsOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mServerConfigurations
								.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								mediaItem, playerOption, images)), listener,
				mContext);
	}

	public void getAlbumDetails(MediaTrackDetails mCurrentTrackDetails,
			PlayerOption playerOption, CommunicationOperationListener listener,
			boolean isVideo) {
		String images = ImagesManager.getImageSize(
				ImagesManager.MUSIC_ART_SMALL, getDisplayDensity());
//				+ ","
//				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_BIG,
//						getDisplayDensity());

		if (mMediaDetailsCommunicationManager == null)
			mMediaDetailsCommunicationManager = new CommunicationManager();
		mMediaDetailsCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new MediaDetailsOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mServerConfigurations
								.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								mCurrentTrackDetails, playerOption, isVideo,
								images)), listener, mContext);
	}

	public void cancelGetMediaDetails() {
		// if (mMediaDetailsCommunicationManager != null
		// && mMediaDetailsCommunicationManager.isRunning()) {
		// mMediaDetailsCommunicationManager.cancelAnyRunningOperation();
		// mMediaDetailsCommunicationManager = null;
		// }
	}

	// public void getMediaCategories(MediaContentType mediaContentType,
	// CommunicationOperationListener listener) {
	//
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager.performOperationAsync(
	// new HungamaWrapperOperation(listener, mContext,
	// new MediaCategoriesOperation(mContext,
	// mServerConfigurations.getHungamaServerUrl(),
	// mServerConfigurations.getHungamaAuthKey(),
	// mediaContentType, mApplicationConfigurations
	// .getPartnerUserId(),
	// mApplicationConfigurations
	// .getCategoriesGenerTimeStamp())),
	// listener, mContext);
	// }

	public void getLeftMenu(Context context,
			CommunicationOperationListener listener, String timestamp_cache) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new LeftMenuOperation(context,
						mServerConfigurations.getHungamaServerUrl_2(),
						mApplicationConfigurations.getPartnerUserId(),
						mApplicationConfigurations.getLeftMenuTimeStamp())),
				listener, mContext);
	}

	// ======================================================
	// Application data getters - Search media.
	// ======================================================

	public void getSearchPopularSerches(Context context,
			CommunicationOperationListener listener, String timestamp_cache) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new SearchPopularKeywordOperation(context,
						mServerConfigurations.getHungamaServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(),
						mApplicationConfigurations.getPartnerUserId(),
						timestamp_cache)), listener, mContext);
	}

	public void getHashTagList(Context context,
			CommunicationOperationListener listener, String timestamp_cache) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new HashTagListOperation(context,
						mServerConfigurations.getHungamaServerUrl_2(),
						mApplicationConfigurations.getPartnerUserId(),
						timestamp_cache)), listener, mContext);
	}

	public void getHashTagResutl(Context context,
			CommunicationOperationListener listener, String hash_tag,
			String timestamp_cache) {
		String images = ImagesManager.getImageSize(
				ImagesManager.MUSIC_ART_SMALL, getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_BIG,
						getDisplayDensity());

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager
				.performOperationAsync(new HungamaWrapperOperation(listener,
						mContext, new HashResultOperation(context,
								mServerConfigurations.getHungamaServerUrl_2(),
								mApplicationConfigurations.getPartnerUserId(),
								hash_tag, timestamp_cache, images)), listener,
						mContext);
	}

	public void getSearchAutoSuggest(String query, String queryLength,
			CommunicationOperationListener listener) {

		mSearchSuggestedCommunicationManager = new CommunicationManager();
		mSearchSuggestedCommunicationManager
				.performOperationAsync(
						new HungamaWrapperOperation(listener, mContext,
								new SearchAutoSuggestOperation(
										mServerConfigurations
												.getHungamaServerUrl_2(),
										query, queryLength,
										mServerConfigurations
												.getHungamaAuthKey(),
										mApplicationConfigurations
												.getPartnerUserId())),
						listener, mContext);
	}

	/**
	 * Cancels the search operation of suggested keywords.
	 */
	public void cancelGetSearchAutoSuggest() {
		if (mSearchSuggestedCommunicationManager != null
				&& mSearchSuggestedCommunicationManager.isRunning()) {
			mSearchSuggestedCommunicationManager.cancelAnyRunningOperation();
			mSearchSuggestedCommunicationManager = null;
		}
	}

	public void getSearchKeyboard(String query, String type, String startIndex,
			String queryLength, CommunicationOperationListener listener) {

		String images = ImagesManager.getImageSize(
				ImagesManager.HOME_MUSIC_TILE, getDisplayDensity());
		if (!TextUtils.isEmpty(type) && type.toLowerCase().contains("video"))
			images = images
					+ ","
					+ ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
							getDisplayDensity());

		mSearchCommunicationManager = new CommunicationManager();
		mSearchCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new SearchKeyboardOperation(mServerConfigurations
								.getHungamaServerUrl_2(), query, type,
								startIndex, queryLength, mServerConfigurations
										.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								images)), listener, mContext);
	}

	/**
	 * Cancels the search operation.
	 */
	public void cancelGetSearch() {
		// if (mSearchCommunicationManager != null
		// && mSearchCommunicationManager.isRunning()) {
		// mSearchCommunicationManager.cancelAnyRunningOperation();
		// mSearchCommunicationManager = null;
		// }
	}

	// public void getVideoDetails(MediaItem mediaItem,
	// CommunicationOperationListener listener) {
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager.performOperationAsync(
	// new HungamaWrapperOperation(listener, mContext,
	// new VideoStreamingOperation(mServerConfigurations
	// .getHungamaServerUrl(),
	// mApplicationConfigurations.getPartnerUserId(),
	// String.valueOf(mediaItem.getId()),
	// getDisplayDensity(), mServerConfigurations
	// .getHungamaAuthKey())), listener,
	// mContext);
	// }

	// ======================================================
	// Application data getters - Video.
	// ======================================================

	public void getVideoDetailsAdp(MediaItem mediaItem, int networkSpeed,
			String networkType, String contentFormat,
			CommunicationOperationListener listener, String googleEmailId) {
		CommunicationManager communicationManager = new CommunicationManager();

		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new VideoStreamingOperationAdp(mServerConfigurations
								.getHungamaServerUrl_2(),
								mApplicationConfigurations.getPartnerUserId(),
								String.valueOf(mediaItem.getId()),
								getDisplayDensity(), mServerConfigurations
										.getHungamaAuthKey(), networkSpeed,
								networkType, contentFormat, googleEmailId,
								false)), listener, mContext);

	}

	public void getRelatedVideo(MediaTrackDetails mediaTrackDetails,
			MediaItem mediaItem, CommunicationOperationListener listener) {

		String images = null;

		images = ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
				getDisplayDensity());

		mRelatedVideoCommunicationManager = new CommunicationManager();
		mRelatedVideoCommunicationManager
				.performOperationAsync(
						new HungamaWrapperOperation(listener, mContext,
								new RelatedVideoOperation(mServerConfigurations
										.getHungamaServerUrl_2(),
										String.valueOf(mediaTrackDetails
												.getAlbumId()), mediaItem
												.getMediaContentType(),
										mediaItem.getMediaType(),
										mServerConfigurations
												.getHungamaAuthKey(), images,
										mApplicationConfigurations
												.getPartnerUserId())),
						listener, mContext);
	}

	// public void cancelGetRelatedVideo() {
	// if (mRelatedVideoCommunicationManager != null
	// && mRelatedVideoCommunicationManager.isRunning()) {
	// mRelatedVideoCommunicationManager.cancelAnyRunningOperation();
	// mRelatedVideoCommunicationManager = null;
	// }
	// }

	// ======================================================
	// Upgrade (Subscription)
	// ======================================================

	public void getCurrentSubscriptionPlan(
			final CommunicationOperationListener listener,
			final String googleEmailId) {
		getCurrentSubscriptionPlan(listener, googleEmailId, 0);
	}

	public void getCurrentSubscriptionPlan(
			final CommunicationOperationListener listener,
			final String googleEmailId, long contentId) {
		getSubscriptionTelcoId(mServerConfigurations
				.getHungamaPayUrl(), listener, googleEmailId, contentId);
	}

	public boolean deleteCurrentSubscriptionPlan() {
		return mCacheManager.deleteStoredCurrentPlanNew();
	}

	public void getSubscriptionTelcoId(final String serverUrl, final CommunicationOperationListener listener,
									   final String googleEmailId, final long contentId) {
		CommunicationOperationListener listenerTelco = new CommunicationOperationListener() {
			@Override
			public void onStart(int operationId) {
			}

			@Override
			public void onSuccess(int operationId, Map<String, Object> responseObjects) {
				String msisdn = null;
				String imsi = null;
				if(responseObjects!=null){
					if(responseObjects.containsKey(SubscriptionTelcoOperation.RESPONSE_KEY_MSISDN)){
						msisdn = (String) responseObjects.get(SubscriptionTelcoOperation.RESPONSE_KEY_MSISDN);
					}
					if(responseObjects.containsKey(SubscriptionTelcoOperation.RESPONSE_KEY_IMSI)){
						imsi = (String) responseObjects.get(SubscriptionTelcoOperation.RESPONSE_KEY_IMSI);
					}
				}
				mSubscriptionPlansCommunicationManager = new CommunicationManager();
				mSubscriptionPlansCommunicationManager.performOperationAsync(
						new HungamaWrapperOperation(
								listener,
								mContext,
								new SubscriptionCheckOperation(
										mContext,
										serverUrl,
										mApplicationConfigurations
												.getPartnerUserId(),
										mServerConfigurations
												.getHungamaAuthKey(),
										googleEmailId, "" + contentId, msisdn, imsi)), listener, mContext);
			}

			@Override
			public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
				listener.onFailure(OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK, errorType, errorMessage);
			}
		};
		mSubscriptionPlansCommunicationManager = new CommunicationManager();
		mSubscriptionPlansCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listenerTelco, mContext,
						new SubscriptionTelcoOperation(mContext,
								mContext.getResources().getString(R.string.hungama_pay_url_telco_api))),
				new WrapperMediaContentOperationListener(listenerTelco), mContext);
	}

	public void getSubscriptionCharge(int planId, String planType,
			SubscriptionType subscriptionType,
			CommunicationOperationListener listener, String code,
			String purchaseToken, String googleEmailId, boolean trial,
			String session, String mSubscriptionId) {
		getSubscriptionChargeNew(String.valueOf(planId), planType, subscriptionType, listener, code, purchaseToken,
				googleEmailId, trial, session, "", mSubscriptionId, "");
	}

	public void getSubscriptionChargeNew(String planId, String planType,
									  SubscriptionType subscriptionType,
									  CommunicationOperationListener listener, String code,
									  String purchaseToken, String googleEmailId, boolean trial,
									  String session, String affCode, String mSubscriptionId, String contentId) {
		mSubscriptionPlansCommunicationManager = new CommunicationManager();
		mSubscriptionPlansCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new SubscriptionOperation(mContext,
								mServerConfigurations
										.getHungamaPayUrl(),
								planId, planType.toLowerCase(),
								mApplicationConfigurations.getPartnerUserId(),
								subscriptionType, mServerConfigurations
								.getHungamaAuthKey(), code,
								purchaseToken, googleEmailId, trial, session, affCode, mSubscriptionId, contentId)),
				listener, mContext);
	}

	public void storeCurrentSubscriptionPlanNew(
			SubscriptionStatusResponse subscriptionStatusResponse) {
		if (subscriptionStatusResponse!=null && subscriptionStatusResponse.getSubscription()!=null
				&& subscriptionStatusResponse.getSubscription().getSubscriptionStatus()==1) {
			mCacheManager
					.storeSubscriptionCurrentPlanNew(subscriptionStatusResponse);
			mApplicationConfigurations.setIsUserHasSubscriptionPlan(true);
			mApplicationConfigurations
					.setIsUserHasTrialSubscriptionPlan(subscriptionStatusResponse.getSubscription().isTrial());
			mApplicationConfigurations.setIsShowAds(subscriptionStatusResponse.getSubscription().isShowAds());
			mApplicationConfigurations
					.setTrialExpiryDaysLeft(subscriptionStatusResponse.getSubscription()
							.getTrailExpiryDaysLeft());
			mApplicationConfigurations
					.setUserSubscriptionPlanDate(subscriptionStatusResponse.getSubscription()
							.getEndDate());
			mApplicationConfigurations
					.setUserSubscriptionPlanDatePurchase(subscriptionStatusResponse.getSubscription()
							.getStartDate());
			mApplicationConfigurations
					.setUserSubscriptionPlanDetails(subscriptionStatusResponse.getSubscription()
							.getPlanDetails());
			mApplicationConfigurations.setFreeUserDeleteCount(0);
		} else {
			mApplicationConfigurations.setIsUserHasSubscriptionPlan(false);
			mApplicationConfigurations.setIsUserHasTrialSubscriptionPlan(false);
			mApplicationConfigurations.setTrialExpiryDaysLeft(0);
			mApplicationConfigurations.setIsShowAds(true);
		}
		try {
			String tagToAdd = "free-user";
			String tagToRemove = "paid-user";
			if (mApplicationConfigurations.isUserHasSubscriptionPlan()
					&& !mApplicationConfigurations
					.isUserHasTrialSubscriptionPlan()) {
				tagToRemove = "free-user";
				tagToAdd = "paid-user";
			}

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

		try {
			if (subscriptionStatusResponse != null
					&& subscriptionStatusResponse.getSubscription() != null
					&& subscriptionStatusResponse.getSubscription().isTrial()) {
				Set<String> tags = Utils.getTags();
				if (!tags.contains("Trial")) {
					if (tags.contains("Trial_expired"))
						tags.remove("Trial_expired");
					tags.add("Trial");
					Utils.AddTag(tags);
				}
			}
//			if (subscriptionStatusResponse != null
//					&& subscriptionStatusResponse.getSubscription() != null
//					&& subscriptionStatusResponse.getSubscription().isTrial()) {
//				mApplicationConfigurations
//						.setIsUserTrialSubscriptionExpired(true);
//				Set<String> tags = Utils.getTags();
//				if (!tags.contains("Trial_expired")) {
//					if (tags.contains("Trial"))
//						tags.remove("Trial");
//					tags.add("Trial_expired");
//					Utils.AddTag(tags);
//				}
//			} else {
//				mApplicationConfigurations
//						.setIsUserTrialSubscriptionExpired(false);
//			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

//	public void getStoredCurrentPlan(ReadCallback callback) {
//		mCacheManager.getStoredCurrentPlan(callback);
//	}

	public void getStoredCurrentPlanNew(ReadCallback callback) {
		mCacheManager.getStoredCurrentPlanNew(callback);
	}

	public SubscriptionStatusResponse getStoredCurrentPlanNew() {
		return  mCacheManager.getStoredCurrentPlanNewSync();
	}

//	public boolean storeSubscriptionCurrentPlan(
//			SubscriptionCheckResponse subscriptionCheckResponse) {
//		return mCacheManager
//				.storeSubscriptionCurrentPlan(subscriptionCheckResponse);
//	}

	public boolean storeSubscriptionCurrentPlanNew(
			SubscriptionStatusResponse subscriptionStatusResponse) {
		return mCacheManager
				.storeSubscriptionCurrentPlanNew(subscriptionStatusResponse);
	}

	//
	// public void getStoredSubscriptionPlans(ReadCallback callback) {
	// mCacheManager.getStoredPlans(callback);
	// }
	//
	// public boolean storeSubscriptionPlans(List<Plan> plans) {
	// return mCacheManager.storeSubscriptionPlans(plans);
	// }

	// public void versionCheck(CommunicationOperationListener listener) {
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager.performOperationAsync(new HungamaWrapperOperation(
	// listener, mContext, new VersionCheckOperation(
	// mServerConfigurations.getHungamaServerUrl(),
	// mApplicationConfigurations.getPartnerUserId(),
	// mServerConfigurations.getHungamaAuthKey(),
	// mServerConfigurations.getReferralId())), listener,
	// mContext);
	// }

	public void newVersionCheck(CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new NewVersionCheckOperation(mServerConfigurations
								.getmHungamaVersionCheckServerUrl(), mContext
								.getPackageName(), getVersionName(mContext))),
				listener, mContext);
	}

	/**
	 * returns application version like 1.0.0/1.1.0/2.0.1 ...etc
	 * 
	 * */
	public static String getVersionName(Context context) {
		try {
			PackageInfo pinfo = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			return pinfo.versionName;
		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
			return null;
		}
	}

	// ======================================================
	// Mobile Verification.
	// ======================================================

	// public void getMobileVerification(String msisdn, String password,
	// MobileOperationType mobileOperationType,
	// CommunicationOperationListener listener) {
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager.performOperationAsync(
	// new HungamaWrapperOperation(listener, mContext,
	// new MobileVerifyOperation(mServerConfigurations
	// .getHungamaMobileVerificationServerUrl(),
	// msisdn, password, mApplicationConfigurations
	// .getPartnerUserId(),
	// mobileOperationType, mServerConfigurations
	// .getHungamaAuthKey())), listener,
	// mContext);
	// }

	// public void checkCountry(String msisdn,
	// CommunicationOperationListener listener) {
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager
	// .performOperationAsync(
	// new HungamaWrapperOperation(
	// listener,
	// mContext,
	// new MobileVerifyCountryCheckOperation(
	// mServerConfigurations
	// .getHungamaMobileVerificationServerUrl(),
	// msisdn, mServerConfigurations
	// .getHungamaAuthKey())),
	// listener, mContext);
	// }

	// ======================================================
	// Download
	// ======================================================

//	public void getDownload(int planId, long contentId, String msisdn,
//			String contentType, DownloadOperationType downloadOperationType,
//			CommunicationOperationListener listener, String transactionKey) {
////		String serverUrl = mServerConfigurations.getHungamaDownloadServerUrl();
////		CommunicationManager communicationManager = new CommunicationManager();
////		if(downloadOperationType == DownloadOperationType.CONTENT_DELIVERY)
////			serverUrl = mServerConfigurations.getHungamaPayUrl();
////		communicationManager.performOperationAsync(
////				new HungamaWrapperOperation(listener, mContext,
////						new DownloadOperation(serverUrl,
////								mApplicationConfigurations.getPartnerUserId(),
////								msisdn, String.valueOf(planId), String
////										.valueOf(contentId), contentType,
////								"android", getDisplayDensity(),
////								downloadOperationType, mServerConfigurations
////										.getHungamaAuthKey(), transactionKey, Utils.getAccountName(mContext), "")),
////				listener, mContext);
//		getDownload(planId, contentId, msisdn, contentType, downloadOperationType, listener, transactionKey, Utils
//				.getAccountName(mContext), "", "");
//	}

	public void getDownload(int planId, long contentId, String msisdn,
							String contentType, DownloadOperationType downloadOperationType,
							CommunicationOperationListener listener, String transactionKey, String googleEmailId, String affCode,
							String albumId) {
		String serverUrl = mServerConfigurations.getHungamaDownloadServerUrl();
		CommunicationManager communicationManager = new CommunicationManager();
		if(downloadOperationType == DownloadOperationType.CONTENT_DELIVERY)
			serverUrl = mServerConfigurations.getHungamaPayUrl();
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new DownloadOperation(serverUrl,
								mApplicationConfigurations.getPartnerUserId(),
								msisdn, String.valueOf(planId), String
								.valueOf(contentId), contentType,
								"android", getDisplayDensity(),
								downloadOperationType, mServerConfigurations
								.getHungamaAuthKey(), transactionKey, googleEmailId, affCode, albumId)),
				listener, mContext);
	}

//	public String getConcentBilling_URL(Context cntext,
//			DownloadPlan clickedPlan, String transactionSession,
//			String contentID) {
//		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
//				.getInstance(cntext);
//		ServerConfigurations mServerConfigurations = ServerConfigurations
//				.getInstance(cntext);
//
//		DeviceConfigurations config = DeviceConfigurations.getInstance(cntext);
//		String HardID = config.getHardwareId();
//		ServerConfigurations Sconfig = ServerConfigurations.getInstance(cntext);
//		String affiliate_id = Sconfig.getReferralId();
//
//		String url = mServerConfigurations.getHungamaDownloadServerUrl();
//		url += HungamaOperation.URL_SEGMENT_CONSENT_BILLING
//				+ HungamaOperation.PARAMS_TRANSACTION_SESSION
//				+ HungamaOperation.EQUALS + transactionSession
//				+ HungamaOperation.AMPERSAND + HungamaOperation.PARAMS_PLAN_ID
//				+ HungamaOperation.EQUALS + clickedPlan.getPlanId()
//				+ HungamaOperation.AMPERSAND
//				+ HungamaOperation.PARAMS_CONTENT_ID + HungamaOperation.EQUALS
//				+ contentID + HungamaOperation.AMPERSAND
//				+ HungamaOperation.PARAMS_USER_ID + HungamaOperation.EQUALS
//				+ mApplicationConfigurations.getPartnerUserId()
//				+ HungamaOperation.AMPERSAND
//				+ HungamaOperation.PARAMS_DOWNLOAD_AFFILIATE_ID
//				+ HungamaOperation.EQUALS + affiliate_id
//				+ HungamaOperation.AMPERSAND
//				+ HungamaOperation.PARAMS_DOWNLOAD_HARDWARE_ID
//				+ HungamaOperation.EQUALS + HardID;
//		return url;
//
//	}

//	public String getConcentBilling_URL(Context cntext, Plan clickedPlan,
//			String transactionSession) {
//		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
//				.getInstance(cntext);
//		ServerConfigurations mServerConfigurations = ServerConfigurations
//				.getInstance(cntext);
//
//		DeviceConfigurations config = DeviceConfigurations.getInstance(cntext);
//		String HardID = config.getHardwareId();
//		ServerConfigurations Sconfig = ServerConfigurations.getInstance(cntext);
//		String affiliate_id = Sconfig.getReferralId();
//
//		String url = mServerConfigurations.getHungamaSubscriptionServerUrl();
//		url += HungamaOperation.URL_SEGMENT_CONSENT_BILLING
//				+ HungamaOperation.PARAMS_TRANSACTION_SESSION
//				+ HungamaOperation.EQUALS + transactionSession
//				+ HungamaOperation.AMPERSAND + HungamaOperation.PARAMS_PLAN_ID
//				+ HungamaOperation.EQUALS + clickedPlan.getPlanId()
//				+ HungamaOperation.AMPERSAND + HungamaOperation.PARAMS_USER_ID
//				+ HungamaOperation.EQUALS
//				+ mApplicationConfigurations.getPartnerUserId()
//				+ HungamaOperation.AMPERSAND
//				+ HungamaOperation.PARAMS_DOWNLOAD_AFFILIATE_ID
//				+ HungamaOperation.EQUALS + affiliate_id
//				+ HungamaOperation.AMPERSAND
//				+ HungamaOperation.PARAMS_DOWNLOAD_HARDWARE_ID
//				+ HungamaOperation.EQUALS + HardID;
//		return url;
//	}

	public String getHungamaPaySubscribeURL() {
		String md5 = Utils.toMD5(CommunicationManager.SECRET_KEY_PAY + mApplicationConfigurations.getPartnerUserId());

		String url = mServerConfigurations.getHungamaPayUrl() + "?auth=" + md5 +
				"&identity=" + mApplicationConfigurations.getPartnerUserId() +
				"&product=" + HungamaOperation.VALUE_PRODUCT + "&platform=" + HungamaOperation.VALUE_DEVICE +
				"&hardware_id=" + mDeviceConfigurations.getHardwareId() +
				"&billing=subscription&aff_code=" + mApplicationConfigurations.getSubscriptionAffCode();
		return url;
	}

	public String getHungamaPayDownlaodURL(long contentId, long albumId) {
		String md5 = Utils.toMD5(CommunicationManager.SECRET_KEY_PAY + mApplicationConfigurations.getPartnerUserId());

		String url = mServerConfigurations.getHungamaPayUrl() + "?auth=" + md5 +
				"&identity=" + mApplicationConfigurations.getPartnerUserId() +
				"&product=" + HungamaOperation.VALUE_PRODUCT + "&platform=" + HungamaOperation.VALUE_DEVICE +
				"&hardware_id=" + mDeviceConfigurations.getHardwareId() +
				"&billing=download&aff_code=" + mApplicationConfigurations.getSubscriptionAffCode() +
				"&content_id=" + contentId + "&album_id=" + albumId;
		return url;
	}

	public String getHungamaRechargeURL() {
		String md5 = Utils.toMD5(CommunicationManager.SECRET_KEY_PAY + mApplicationConfigurations.getPartnerUserId());

		String url = mServerConfigurations.getHungamaPayUrl() + "/recharge?auth=" + md5 +
				"&identity=" + mApplicationConfigurations.getPartnerUserId() +
				"&product=hungamamusic&platform=android&hardware_id=" +
				mDeviceConfigurations.getHardwareId();
		return url;
	}

	// ======================================================
	// Music.
	// ======================================================

	public PlayingQueue getStoredPlayingQueue(
			ApplicationConfigurations mApplicationConfigurations) {
		try {
			String savedQueue = mApplicationConfigurations.getPlayerQueue();
			if (savedQueue == null) {
				return new PlayingQueue(null, 0, PlayerService.service);
			} else {
				JSONArray jsonTracks = new JSONArray(savedQueue);
				// System.out.println("Number of tracks saved ::::: " +
				// jsonTracks.length());
				List<Track> tracks = new ArrayList<Track>();
				for (int i = 0; i < jsonTracks.length(); i++) {
					try {
                        tracks.add(new Gson().fromJson(jsonTracks.getString(i),
                                Track.class));
                    } catch (Exception e) {
                    } catch (Error e) {
                    }
				}
				// System.out.println("Number of tracks added to queue ::::: " +
				// tracks.size());
				return new PlayingQueue(tracks, 0, PlayerService.service);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.printStackTrace(e);
			return new PlayingQueue(null, 0, PlayerService.service);
		}
	}

	public void getTrackSimilar(Track track, String start, String length,
			CommunicationOperationListener listener) {

		String images = ImagesManager.getImageSize(
				ImagesManager.MUSIC_ART_SMALL, getDisplayDensity());

		mTrackSimilarCommunicationManager = new CommunicationManager();
		mTrackSimilarCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new TrackSimilarOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mServerConfigurations
								.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								track, start, length, images)), listener,
				mContext);
	}

	public void cancelGetTrackSimilar() {
		if (mTrackSimilarCommunicationManager != null
				&& mTrackSimilarCommunicationManager.isRunning()) {
			mTrackSimilarCommunicationManager.cancelAnyRunningOperation();
			mTrackSimilarCommunicationManager = null;
		}
	}

	public void getTrackLyrics(Track track,
			CommunicationOperationListener listener) {
		mTrackLyricsCommunicationManager = new CommunicationManager();
		mTrackLyricsCommunicationManager
				.performOperationAsync(
						new HungamaWrapperOperation(listener, mContext,
								new TrackLyricsOperation(mServerConfigurations
										.getHungamaServerUrl_2(),
										mServerConfigurations
												.getHungamaAuthKey(), track,
										mApplicationConfigurations
												.getPartnerUserId())),
						listener, mContext);
	}

	public void cancelGetTrackLyrics() {
		if (mTrackLyricsCommunicationManager != null
				&& mTrackLyricsCommunicationManager.isRunning()) {
			mTrackLyricsCommunicationManager.cancelAnyRunningOperation();
			mTrackLyricsCommunicationManager = null;
		}
	}

	public void getTrackTrivia(Track track,
			CommunicationOperationListener listener) {
		mTrackTriviaCommunicationManager = new CommunicationManager();
		mTrackTriviaCommunicationManager
				.performOperationAsync(
						new HungamaWrapperOperation(listener, mContext,
								new TrackTriviaOperation(mServerConfigurations
										.getHungamaServerUrl_2(),
										mServerConfigurations
												.getHungamaAuthKey(), track,
										mApplicationConfigurations
												.getPartnerUserId())),
						listener, mContext);
	}

	public void cancelGetTrackTrivia() {
		if (mTrackTriviaCommunicationManager != null
				&& mTrackTriviaCommunicationManager.isRunning()) {
			mTrackTriviaCommunicationManager.cancelAnyRunningOperation();
			mTrackTriviaCommunicationManager = null;
		}
	}

	// ======================================================
	// Web Radio.
	// ======================================================

	int threadsleep = 0;

	/**
	 * Retrieves all the Live stations as Media items.
	 */
	public void getRadioLiveStations(CommunicationOperationListener listener,
			String timestamp_cache) {

		String images = ImagesManager.getImageSize(
				ImagesManager.RADIO_LIST_ART, getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_SMALL,
						getDisplayDensity());

		final RadioLiveStationsOperation mRadioLiveStationsOperation = new RadioLiveStationsOperation(
				mContext, mServerConfigurations.getHungamaServerUrl_2(),
				mServerConfigurations.getHungamaAuthKey(),
				mApplicationConfigurations.getPartnerUserId(), timestamp_cache,
				images);
		// final Handler handle = new Handler(Looper.getMainLooper());
		//
		// String response = mCacheManager.getLiveRadioResponse();
		//
		// if (!TextUtils.isEmpty(response)) {
		//
		// final CommunicationOperationListener listenerOld = listener;
		// listener = new CommunicationOperationListener() {
		// @Override
		// public void onSuccess(int operationId,
		// Map<String, Object> responseObjects) {
		// }
		//
		// @Override
		// public void onStart(int operationId) {
		// }
		//
		// @Override
		// public void onFailure(int operationId, ErrorType errorType,
		// String errorMessage) {
		// }
		// };
		// final Response finalResponse = new Response();
		// finalResponse.response = response;
		// finalResponse.responseCode =
		// CommunicationManager.RESPONSE_SUCCESS_200;
		//
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// listenerOld.onStart(mRadioLiveStationsOperation
		// .getOperationId());
		// final Map<String, Object> map = mRadioLiveStationsOperation
		// .parseResponseFromCache(finalResponse, true);
		//
		// handle.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// listenerOld.onSuccess(
		// mRadioLiveStationsOperation
		// .getOperationId(), map);
		// }
		// }, threadsleep);
		// } catch (InvalidRequestParametersException e) {
		// e.printStackTrace();
		// } catch (InvalidRequestTokenException e) {
		// e.printStackTrace();
		// } catch (InvalidResponseDataException e) {
		// e.printStackTrace();
		// } catch (OperationCancelledException e) {
		// e.printStackTrace();
		// }
		// }
		// }).start();
		//
		// }

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, mRadioLiveStationsOperation), listener,
				mContext);
	}

	/**
	 * Retrieves all the Top Artists as Media items.
	 */
	public void getRadioTopArtists(CommunicationOperationListener listener,
			String timestamp_cache) {
		// String images = ImagesManager.getImageSize(
		// ImagesManager.MUSIC_ART_SMALL, getDisplayDensity())
		// + ","
		// + ImagesManager.getImageSize(ImagesManager.MUSIC_ART_BIG,
		// getDisplayDensity());
		String images = ImagesManager.getImageSize(
				ImagesManager.RADIO_LIST_ART, getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_SMALL,
						getDisplayDensity());
		final RadioTopArtistsOperation mRadioTopArtistsOperation = new RadioTopArtistsOperation(
				mContext, mServerConfigurations.getHungamaServerUrl_2(),
				mServerConfigurations.getHungamaAuthKey(),
				mApplicationConfigurations.getPartnerUserId(), timestamp_cache,
				images);
		// final Handler handle = new Handler();
		//
		// String response = mCacheManager.getCelebRadioResponse();
		//
		// if (!TextUtils.isEmpty(response)) {
		//
		// final CommunicationOperationListener listnerold = listener;
		// listener = new CommunicationOperationListener() {
		// @Override
		// public void onSuccess(int operationId,
		// Map<String, Object> responseObjects) {
		// }
		//
		// @Override
		// public void onStart(int operationId) {
		// }
		//
		// @Override
		// public void onFailure(int operationId, ErrorType errorType,
		// String errorMessage) {
		// }
		// };
		// final Response finalResponse = new Response();
		// finalResponse.response = response;
		// finalResponse.responseCode =
		// CommunicationManager.RESPONSE_SUCCESS_200;
		//
		// new Thread(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// listnerold.onStart(mRadioTopArtistsOperation
		// .getOperationId());
		// final Map<String, Object> map = mRadioTopArtistsOperation
		// .parseResponseFromCache(finalResponse, true);
		//
		// handle.postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// listnerold.onSuccess(mRadioTopArtistsOperation
		// .getOperationId(), map);
		// }
		// }, threadsleep);
		// } catch (InvalidRequestParametersException e) {
		// e.printStackTrace();
		// } catch (InvalidRequestTokenException e) {
		// e.printStackTrace();
		// } catch (InvalidResponseDataException e) {
		// e.printStackTrace();
		// } catch (OperationCancelledException e) {
		// e.printStackTrace();
		// }
		// }
		// }).start();
		// }

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, mRadioTopArtistsOperation), listener,
				mContext);
	}

	/**
	 * Retrieves all the Artists songs as Media items.
	 */
	public void getRadioTopArtistSongs(MediaItem artistItem,
			CommunicationOperationListener listener) {
		// String images = ImagesManager.getImageSize(
		// ImagesManager.ARTIST_ART_BIG, getDisplayDensity());
		String images = ImagesManager.getImageSize(
				ImagesManager.MUSIC_ART_SMALL, getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_BIG,
						getDisplayDensity());
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new RadioTopArtistSongsOperation(
						mServerConfigurations.getHungamaServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(),
						mApplicationConfigurations.getPartnerUserId(),
						artistItem, images)), listener, mContext);
	}

	/**
	 * Retrieves all the Artists songs as Media items.
	 */
	public void getLiveRadioDetails(String radioId,
			CommunicationOperationListener listener) {
		// String images = ImagesManager.getImageSize(
		// ImagesManager.ARTIST_ART_BIG, getDisplayDensity());
		String images = ImagesManager.getImageSize(
				ImagesManager.RADIO_LIST_ART, getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_SMALL,
						getDisplayDensity());
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new LiveRadioDetailsOperation(
						mServerConfigurations.getHungamaServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(),
						mApplicationConfigurations.getPartnerUserId(), radioId,
						images)), listener, mContext);
	}

	// ======================================================
	// Events.
	// ======================================================

	// private final Object mEventMutext = new Object();

	/**
	 * Posts an event in the CM servers.
	 * 
	 * @param event
	 */
	public synchronized void addEvent(Event event) {

		Logger.e("EventTrack", "DM :addEvent ");

		// xtpl
		if (event == null)
			return;

		if (event instanceof PlayEvent) {
			if (((PlayEvent) event).getDeliveryId() == -1) {
				Logger.e("EventTrack", "DM :addEvent return");
				return;
			}
		}
		if (event instanceof CampaignPlayEvent) {
			CampaignPlayEvent campEvent = ((CampaignPlayEvent) event);
			if (campEvent.getCampaignId() == 0 || TextUtils.isEmpty(campEvent.getCampaignMediaId()) || campEvent.getCampaignMediaId().equalsIgnoreCase("None") || campEvent.getPlayType().equals("None") ||TextUtils.isEmpty(campEvent.getPlayType())){
				Logger.e("EventTrack", "DM :CampaignPlayEvent addEvent return");
				return;
			}
		}

		// xtpl
		if (mEventManager == null) {
			notifyApplicationStarts();
		} else
			Logger.e("EventTrack", "DM :mEventManager not null ");

		if (mEventManager != null)
			mEventManager.addEvent(event);
		else
			Logger.e("EventTrack",
					"DM :mEventManager null addEvent not called ");

	}

	// ======================================================
	// Moods.
	// ======================================================

	public enum MoodIcon {
		SMALL;// , BIG;
	}

	public boolean storeMoods(List<Mood> moods) {
		return mCacheManager.storeMoods(moods);
	}

	public List<Mood> getStoredMoods() {
		return mCacheManager.getStoredMoods();
	}

	/**
	 * Starts prefetching moods if they are not exist in the cache.
	 */
	// public void prefetchMoodsIfNotExists() {
	// List<Mood> moods = mCacheManager.getStoredMoods();
	// boolean hasSuccessed = mApplicationConfigurations
	// .hasSuccessedPrefetchingMoods();
	// if (Utils.isListEmpty(moods) || hasSuccessed) {
	// // Intent prefetchMoods = new Intent(getApplicationContext(),
	// // MoodPrefetchingService.class);
	// // mContext.startService(prefetchMoods);
	// }
	// }

	// public Drawable getMoodIcon(Mood mood, MoodIcon moodIcon) {
	// try {
	// return mCacheManager.getMoodIcon(mood, moodIcon);
	// } catch (IOException e) {
	// e.printStackTrace();
	// return null;
	// }
	// }

	public Drawable getMoodIcon(String url) {
		try {
			return mCacheManager.getMoodIcon(url);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

        public int getMoodIcon(int id, String name, boolean isSmall) {
		if(TextUtils.isEmpty(name) && id == 0){
			return 0;
		} else if(id==1 || name.equalsIgnoreCase("Heart Broken")){
			if(isSmall)
				return R.drawable.mood_smblack_1;
			else
				return R.drawable.mood_bgblack_1;
		} else if(id==2 || name.equalsIgnoreCase("Sad")){
			if(isSmall)
				return R.drawable.mood_smblack_2;
			else
				return R.drawable.mood_bgblack_2;
		} else if(id==3 || name.equalsIgnoreCase("Chilled Out")){
			if(isSmall)
				return R.drawable.mood_smblack_3;
			else
				return R.drawable.mood_bgblack_3;
		} else if(id==4 || name.equalsIgnoreCase("Happy")){
			if(isSmall)
				return R.drawable.mood_smblack_4;
			else
				return R.drawable.mood_bgblack_4;
		} else if(id==5 || name.equalsIgnoreCase("Ecstatic")){
			if(isSmall)
				return R.drawable.mood_smblack_5;
			else
				return R.drawable.mood_bgblack_5;
		} else if(id==6 || name.equalsIgnoreCase("Romantic")){
			if(isSmall)
				return R.drawable.mood_smblack_6;
			else
				return R.drawable.mood_bgblack_6;
		} else if(id==7 || name.equalsIgnoreCase("Party")){
			if(isSmall)
				return R.drawable.mood_smblack_7;
			else
				return R.drawable.mood_bgblack_7;
		} else {
			return 0;
		}
	}

	// ======================================================
	// Preferences.
	// ======================================================

	public void getPreferences(final CommunicationOperationListener listener) {
		/*
		 * Most of the time the preferences should be get from the cache and
		 * won't be necessary to perform the "online ? webserice : cache"
		 * pattern.
		 */

		// checks in the cache first.
		// List<CategoryTypeObject> categoryTypeObject = mCacheManager
		// .getStoredPreferences();
		// if (categoryTypeObject != null && categoryTypeObject.size() > 0) {
		// listener.onStart(OperationDefinition.Hungama.OperationId.PREFERENCES_GET);
		// // creates the result map.
		// Map<String, Object> resultMap = new HashMap<String, Object>();
		// resultMap.put(
		// MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES,
		// categoryTypeObject);
		// resultMap
		// .put(MediaCategoriesOperation.RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE,
		// MediaContentType.MUSIC);
		//
		// // done, invokes success.
		// listener.onSuccess(
		// OperationDefinition.Hungama.OperationId.PREFERENCES_GET,
		// resultMap);
		// } else {
		if (isDeviceOnLine()) {
			CommunicationManager communicationManager = new CommunicationManager();
			communicationManager.performOperationAsync(
					new HungamaWrapperOperation(listener, mContext,
							new MediaCategoriesOperation(mContext,
									mServerConfigurations
											.getHungamaServerUrl_2(),
									mServerConfigurations.getHungamaAuthKey(),
									MediaContentType.MUSIC,
									mApplicationConfigurations
											.getPartnerUserId(),
									mApplicationConfigurations
											.getCategoriesGenerTimeStamp())),
					new CommunicationOperationListener() {

						@Override
						public void onStart(int operationId) {
							listener.onStart(OperationDefinition.Hungama.OperationId.PREFERENCES_GET);
						}

						@Override
						public void onSuccess(int operationId,
								Map<String, Object> responseObjects) {
							if (responseObjects
									.containsKey(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES)) {
								MusicCategoriesResponse musicCategoriesResponse = (MusicCategoriesResponse) responseObjects
										.get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
								mCacheManager.storePreferences(
										musicCategoriesResponse, null);
								// gets the categoris and cache them.
								// List<CategoryTypeObject> categories =
								// (List<CategoryTypeObject>) responseObjects
								// .get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
								// mCacheManager
								// .storePreferences(categories, null);
							}
							listener.onSuccess(
									OperationDefinition.Hungama.OperationId.PREFERENCES_GET,
									responseObjects);
						}

						@Override
						public void onFailure(int operationId,
								ErrorType errorType, String errorMessage) {
							listener.onFailure(
									OperationDefinition.Hungama.OperationId.PREFERENCES_GET,
									errorType, errorMessage);
						}
					}, mContext);
		}
		// }

	}

	// public void getPreferences(final CommunicationOperationListener listener,
	// final MediaContentType mediaContentType) {
	// /*
	// * Most of the time the preferences should be get from the cache and
	// * won't be necessary to perform the "online ? webserice : cache"
	// * pattern.
	// */
	//
	// // checks in the cache first.
	// // List<CategoryTypeObject> categoryTypeObject;
	// // if (mediaContentType == MediaContentType.VIDEO)
	// // categoryTypeObject = mCacheManager.getStoredPreferencesVideo();
	// // else
	// // categoryTypeObject = mCacheManager.getStoredPreferences();
	// // if (categoryTypeObject != null && categoryTypeObject.size() > 0) {
	// //
	// listener.onStart(OperationDefinition.Hungama.OperationId.PREFERENCES_GET);
	// // // creates the result map.
	// // Map<String, Object> resultMap = new HashMap<String, Object>();
	// // resultMap.put(
	// // MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES,
	// // categoryTypeObject);
	// // resultMap
	// // .put(MediaCategoriesOperation.RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE,
	// // mediaContentType);
	// //
	// // // done, invokes success.
	// // listener.onSuccess(
	// // OperationDefinition.Hungama.OperationId.PREFERENCES_GET,
	// // resultMap);
	// // } else {
	// if (isDeviceOnLine()) {
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager.performOperationAsync(
	// new HungamaWrapperOperation(listener, mContext,
	// new MediaCategoriesOperation(mContext, mServerConfigurations
	// .getHungamaServerUrl(),
	// mServerConfigurations.getHungamaAuthKey(),
	// mediaContentType, mApplicationConfigurations.getPartnerUserId(),
	// mApplicationConfigurations.getCategoriesGenerTimeStamp())),
	// new CommunicationOperationListener() {
	//
	// @Override
	// public void onStart(int operationId) {
	// listener.onStart(OperationDefinition.Hungama.OperationId.PREFERENCES_GET);
	// }
	//
	// @Override
	// public void onSuccess(int operationId,
	// Map<String, Object> responseObjects) {
	// if (responseObjects
	// .containsKey(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES)) {
	// // gets the categoris and cache them.
	// List<CategoryTypeObject> categories = (List<CategoryTypeObject>)
	// responseObjects
	// .get(MediaCategoriesOperation.RESULT_KEY_OBJECT_CATEGORIES);
	// if (mediaContentType == MediaContentType.VIDEO)
	// mCacheManager.storePreferencesVideo(
	// categories, null);
	// else
	// mCacheManager.storePreferences(categories,
	// null);
	// }
	// listener.onSuccess(
	// OperationDefinition.Hungama.OperationId.PREFERENCES_GET,
	// responseObjects);
	// }
	//
	// @Override
	// public void onFailure(int operationId,
	// ErrorType errorType, String errorMessage) {
	// listener.onFailure(
	// OperationDefinition.Hungama.OperationId.PREFERENCES_GET,
	// errorType, errorMessage);
	// }
	// }, mContext);
	// }
	// // }
	//
	// }

	// ======================================================
	// Discover.
	// ======================================================

	public void getDiscoverSearchResult(Discover discover,
			DiscoverSearchResultIndexer discoverSearchResultIndexer,
			CommunicationOperationListener listener) {
		String images = ImagesManager.getImageSize(
				ImagesManager.MUSIC_ART_SMALL, getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_BIG,
						getDisplayDensity());

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new DiscoverSearchResultsOperation(
						mServerConfigurations.getHungamaServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(),
						mApplicationConfigurations.getPartnerUserId(),
						discover, discoverSearchResultIndexer, images)),
				listener, mContext);
	}

	// public void saveDiscover(Discover discover, boolean
	// shouldRestartIfSuccess,
	// CommunicationOperationListener listener) {
	//
	// CommunicationManager communicationManager = new CommunicationManager();
	//
	// /*
	// * Due to a weird crash causes by the BadgesAndCoinsActivity, if we
	// * should restart the Discover activity after saving, we disable the
	// * ability to launch the BadgesAndCoinsActivity checks.
	// */
	// CommunicationOperation operation = null;
	// if (!shouldRestartIfSuccess) {
	// operation = new HungamaWrapperOperation(listener, mContext,
	// new DiscoverSaveOperation(
	// mServerConfigurations.getHungamaServerUrl(),
	// mServerConfigurations.getHungamaAuthKey(),
	// mApplicationConfigurations.getPartnerUserId(),
	// discover, shouldRestartIfSuccess));
	// } else {
	// operation = new DiscoverSaveOperation(
	// mServerConfigurations.getHungamaServerUrl(),
	// mServerConfigurations.getHungamaAuthKey(),
	// mApplicationConfigurations.getPartnerUserId(), discover,
	// shouldRestartIfSuccess);
	// }
	//
	// communicationManager.performOperationAsync(operation, listener,
	// mContext);
	// }

	// public void getDiscoveries(String userId,
	// final CommunicationOperationListener listener) {
	//
	// /*
	// * To get the saved discoveries for the user we must get his preferences
	// * first.
	// */
	// // List<CategoryTypeObject> categoryTypeObjects = mCacheManager
	// // .getStoredPreferences();
	// // List<CategoryTypeObject> categoryTypeObjects = new
	// // ArrayList<CategoryTypeObject>();
	// //
	// // final CommunicationManager communicationManager = new
	// // CommunicationManager();
	// //
	// // if (!Utils.isListEmpty(categoryTypeObjects)) {
	// // // can retrieves the discoveries.
	// // DiscoverRetrieveOperation discoverRetrieveOperation = new
	// // DiscoverRetrieveOperation(
	// // mServerConfigurations.getHungamaServerUrl(),
	// // mServerConfigurations.getHungamaAuthKey(), userId,
	// // categoryTypeObjects, mCacheManager.getStoredMoods());
	// // communicationManager.performOperationAsync(
	// // new HungamaWrapperOperation(listener, mContext,
	// // discoverRetrieveOperation), listener, mContext);
	// // } else {
	// // /*
	// // * Wrapping the listener with a custom one to catch the success of
	// // * storing the preferences in the cache / getting from the WS - and
	// // * then calling the get discoveries.
	// // */
	// // getPreferences(new WrapperGetPreferencesOperationListener(listener));
	// // }
	//
	// }

	// ======================================================
	// My Preferences.
	// ======================================================

	public void getMyPreferences(final CommunicationOperationListener listener) {
		if (isDeviceOnLine()) {
			CommunicationManager communicationManager = new CommunicationManager();
			communicationManager
					.performOperationAsync(
							new HungamaWrapperOperation(
									listener,
									mContext,
									new PreferencesRetrieveOperation(
											mContext,
											mServerConfigurations
													.getHungamaServerUrl_2(),
											mServerConfigurations
													.getHungamaAuthKey(),
											mApplicationConfigurations
													.getPartnerUserId(),
											MediaContentType.MUSIC,
											mApplicationConfigurations
													.getPreferenceGetCategoryTimeStamp())),
							listener, mContext);
		}
	}

	// public void getMyPreferences(final CommunicationOperationListener
	// listener,
	// MediaContentType mediaContenetType) {
	// if (isDeviceOnLine()) {
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager
	// .performOperationAsync(
	// new HungamaWrapperOperation(listener, mContext,
	// new PreferencesRetrieveOperation(mContext,
	// mServerConfigurations
	// .getHungamaServerUrl(),
	// mServerConfigurations
	// .getHungamaAuthKey(),
	// mApplicationConfigurations
	// .getPartnerUserId(),
	// mediaContenetType,
	// mApplicationConfigurations.getPreferenceGetCategoryTimeStamp())),
	// listener,
	// mContext);
	// }
	// }

	public void saveMyPreferences(String preferencesIdList,
			final CommunicationOperationListener listener, String category,
			String genre) {
		if (isDeviceOnLine()) {
			CommunicationManager communicationManager = new CommunicationManager();
			communicationManager.performOperationAsync(
					new HungamaWrapperOperation(listener, mContext,
							new PreferencesSaveOperation(mServerConfigurations
									.getHungamaServerUrl_2(),
									mServerConfigurations.getHungamaAuthKey(),
									mApplicationConfigurations
											.getPartnerUserId(),
									preferencesIdList, MediaContentType.MUSIC,
									URLEncoder.encode(category), URLEncoder
											.encode(genre))), listener,
					mContext);
		}
	}

	// public void saveMyPreferences(String preferencesIdList,
	// final CommunicationOperationListener listener,
	// MediaContentType mediaContenetType, String category, String gener) {
	// if (isDeviceOnLine()) {
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager.performOperationAsync(
	// new HungamaWrapperOperation(listener, mContext,
	// new PreferencesSaveOperation(mServerConfigurations
	// .getHungamaServerUrl(),
	// mServerConfigurations.getHungamaAuthKey(),
	// mApplicationConfigurations
	// .getPartnerUserId(),
	// preferencesIdList, mediaContenetType,
	// category, gener)), listener, mContext);
	// }
	// }

	// ======================================================
	// Social.
	// ======================================================

	private CommunicationManager mMyStreamItemsCommunicationManager = null;
	private CommunicationManager mProfileLeaderboardCommunicationManager = null;
	private CommunicationManager mProfileBadgescommunicationManager = null;

	/**
	 * Retrieves the StreamItems to the given category.
	 */
	public void getMyStreamItems(StreamItemCategory streamItemCategory,
			CommunicationOperationListener listener) {

		String images = ImagesManager.getImageSize(
				ImagesManager.RADIO_LIST_ART, getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_SMALL,
						getDisplayDensity());

		SocialMyStreamOperation socialMyStreamOperation = new SocialMyStreamOperation(
				mServerConfigurations.getHungamaSocialServerUrl_2(),
				mServerConfigurations.getHungamaAuthKey(),
				mApplicationConfigurations.getPartnerUserId(),
				streamItemCategory, images);

		mMyStreamItemsCommunicationManager = new CommunicationManager();
		mMyStreamItemsCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						socialMyStreamOperation), listener, mContext);
	}

	public void cancelGetMyStreamItems() {
		if (mMyStreamItemsCommunicationManager != null
				&& mMyStreamItemsCommunicationManager.isRunning())
			mMyStreamItemsCommunicationManager.cancelAnyRunningOperation();
		mMyStreamItemsCommunicationManager = null;
	}

	public void getUserProfile(Context c, String userId,
			CommunicationOperationListener listener) {

		if (TextUtils.isEmpty(userId)) {
			// sets the application user's id as default.
			userId = mApplicationConfigurations.getPartnerUserId();
		}

		SocialProfileOperation socialProfileOperation = new SocialProfileOperation(
				c, mServerConfigurations.getHungamaSocialServerUrl_2(),
				mServerConfigurations.getHungamaAuthKey(), userId,
				mApplicationConfigurations.getUserProfileTimeStamp());

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager
				.performOperationAsync(new HungamaWrapperOperation(listener,
						mContext, socialProfileOperation), listener, mContext);
	}

	public void getProfileLeaderboard(String userId,
			ProfileLeaderboard.TYPE type, ProfileLeaderboard.PERIOD period,
			CommunicationOperationListener listener) {
		SocialProfileLeaderboardOperation operation = new SocialProfileLeaderboardOperation(
				mContext, mServerConfigurations.getHungamaSocialServerUrl_2(),
				mServerConfigurations.getHungamaAuthKey(), userId, type,
				period,
				mApplicationConfigurations.getUserProfileLeaderboardTimeStamp());
		mProfileLeaderboardCommunicationManager = new CommunicationManager();
		mProfileLeaderboardCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext, operation),
				listener, mContext);
	}

	public void cancelGetProfileLeaderboard() {
		if (mProfileLeaderboardCommunicationManager != null
				&& mProfileLeaderboardCommunicationManager.isRunning()) {

			mProfileLeaderboardCommunicationManager.cancelAnyRunningOperation();
			mProfileLeaderboardCommunicationManager = null;
		}
	}

	public void getProfileBadges(String userId,
			CommunicationOperationListener listener) {
		mProfileBadgescommunicationManager = new CommunicationManager();
		mProfileBadgescommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new SocialProfileBadgesOperation(mContext,
								mServerConfigurations
										.getHungamaSocialServerUrl_2(),
								mServerConfigurations.getHungamaAuthKey(),
								userId, mApplicationConfigurations
										.getUserProfileBadgesTimeStamp())),
				listener, mContext);
	}

	public void cancelGetProfileBadges() {
		if (mProfileBadgescommunicationManager != null
				&& mProfileBadgescommunicationManager.isRunning()) {
			mProfileBadgescommunicationManager.cancelAnyRunningOperation();
		}
	}

	public void checkBadgesAlert(String contentId, String mediaType,
			String action, CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new SocialBadgeAlertOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mServerConfigurations
								.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								contentId, mediaType, action,
								mDeviceConfigurations.getHardwareId())),
				listener, mContext);
	}

	// ======================================================
	// Favorites
	// ======================================================

	public void addToFavorites(String contentId, String mediaType,
			CommunicationOperationListener listener) {
        Set<String> tags = Utils.getTags();
        if(mediaType.equals(MediaType.VIDEO.toString())){

           String tag= Constants.UA_TAG_FAVOURITED_VIDEO;
           if (!tags.contains(tag)) {
               tags.add(tag);
               Utils.AddTag(tags);
           }
       }else{
            String tag= Constants.UA_TAG_FAVOURITED_SONG;
            if (!tags.contains(tag)) {
                tags.add(tag);
                Utils.AddTag(tags);
            }
       }


		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new AddToFavoriteOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mServerConfigurations
								.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								mediaType, contentId, mDeviceConfigurations
										.getHardwareId())), listener, mContext);
	}

	public void removeFromFavorites(String contentId, String mediaType,
			CommunicationOperationListener listener) {
		if (mediaType.equals(MediaType.TRACK.toString())) {
			mediaType = "song";
		}

		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new RemoveFromFavoriteOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mServerConfigurations
								.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								mediaType, contentId, mDeviceConfigurations
										.getHardwareId())), listener, mContext);
	}


	private CommunicationManager mFavoritesCM;

	public void getFavorites(Context c, MediaType mediaType, String userId,
			CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();

		String images = null;

		if (mediaType == MediaType.VIDEO)
			images = ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
					getDisplayDensity());
		else
			images = ImagesManager.getImageSize(ImagesManager.HOME_MUSIC_TILE,
					getDisplayDensity());

		String timeStamp = "";
		if (mediaType == MediaType.ALBUM) {
			timeStamp = mApplicationConfigurations
					.getUserProfileFavoriteAlbumTimeStamp();
		} else if (mediaType == MediaType.TRACK) {
			timeStamp = mApplicationConfigurations
					.getUserProfileFavoriteTimeStamp();
		} else if (mediaType == MediaType.PLAYLIST) {
			timeStamp = mApplicationConfigurations
					.getUserProfileFavoritePlaylistTimeStamp();
		} else if (mediaType == MediaType.VIDEO) {
			timeStamp = mApplicationConfigurations
					.getUserProfileFavoriteVideosTimeStamp();
		} else if (mediaType == MediaType.ARTIST) {
			timeStamp = mApplicationConfigurations
					.getUserProfileFavoriteArtistTimeStamp();
		}

		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext,
				new SocialProfileFavoriteMediaItemsOperation(c,
						mServerConfigurations.getHungamaSocialServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(), userId,
						mediaType, timeStamp, images)), listener, mContext);
	}

	public void cancelGetListFavorites() {
		if (mFavoritesCM != null
				&& mFavoritesCM.isRunning()) {
			mFavoritesCM.cancelAnyRunningOperation();
			mFavoritesCM = null;
		}
	}


	// ======================================================
	// My Collection
	// ======================================================

	public void getMyCollection(CommunicationOperationListener listener,
			String type) {
		String images = null;

		// if (mediaContentType == MediaContentType.MUSIC)
		if (type.equalsIgnoreCase("video"))
			images = ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
					getDisplayDensity());
		else
			images = ImagesManager.getImageSize(ImagesManager.HOME_MUSIC_TILE,
					getDisplayDensity());
		// else if (mediaContentType == MediaContentType.VIDEO)
		// images = ImagesManager.getImageSize(ImagesManager.HOME_VIDEO_TILE,
		// getDisplayDensity());
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new SocialMyCollectionOperation(
						mServerConfigurations.getHungamaSocialServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(),
						mApplicationConfigurations.getPartnerUserId(), images,
						type)), listener, mContext);
	}

	// ======================================================
	// Comments
	// ======================================================

	public void getComments(long contentId, MediaType type, int startIndex,
			int length, CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		String type_str = type.toString().toLowerCase();
		if (type == MediaType.TRACK) {
			type_str = "song";
		}
		communicationManager
				.performOperationAsync(
						new HungamaWrapperOperation(listener, mContext,
								new SocialCommentsListingOperation(
										mServerConfigurations
												.getHungamaSocialServerUrl_2(),
										mServerConfigurations
												.getHungamaAuthKey(), String
												.valueOf(contentId), type_str,
										String.valueOf(startIndex), String
												.valueOf(length),
										mApplicationConfigurations
												.getPartnerUserId())),
						listener, mContext);
	}

	public void postComment(long contentId, MediaType type, String provider,
			String comment, CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		String type_str = type.toString().toLowerCase();
		if (type == MediaType.TRACK) {
			type_str = "song";
		}
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new SocialCommentsPostOperation(mContext,
								mServerConfigurations
										.getHungamaSocialServerUrl_2(),
								mServerConfigurations.getHungamaAuthKey(),
								String.valueOf(contentId), type_str,
								mApplicationConfigurations.getPartnerUserId(),
								provider, comment)), listener, mContext);
	}

	// ======================================================
	// Share
	// ======================================================

	public void share(int contentId, String type, String provider,
			String userText, CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		String type_str = type;
		if (MediaType.getMediaItemByName(type) == MediaType.TRACK) {
			type_str = "song";
		}
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new ShareOperation(mServerConfigurations
								.getHungamaSocialServerUrl_2(),
								mServerConfigurations.getHungamaAuthKey(),
								String.valueOf(contentId), type_str,
								mApplicationConfigurations.getPartnerUserId(),
								provider, userText)), listener, mContext);
	}

	public void getShareUrl(String contentId, String mediaType,
			CommunicationOperationListener listener) {

		CommunicationManager communicationManager = new CommunicationManager();
		String type_str = mediaType;
		if (MediaType.getMediaItemByName(mediaType) == MediaType.TRACK) {
			type_str = "song";
		}
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new SocialGetUrlOperation(
						mServerConfigurations.getHungamaSocialServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(),
						mApplicationConfigurations.getPartnerUserId(),
						contentId, type_str)), listener, mContext);

	}

	// ======================================================
	// Feedback.
	// ======================================================

	public void getFeedbackSubjects(Context context,
			final CommunicationOperationListener listener) {
		// checks if we already have the subjects to perform some nitty tests
		// upon.

		// List<String> cahcedSubjects =
		// mCacheManager.getStoredFeedbackSubjects();

		/*
		 * if (!Utils.isListEmpty(cahcedSubjects)) { // Yes! we've got some
		 * suckers, just like Apple funboz - pack them' // up // and snd to the
		 * Test Chamber. listener.onStart(OperationId.FEEDBACK_SUBJECTS);
		 * 
		 * Map<String, Object> suckersMap = new HashMap<String, Object>();
		 * suckersMap.put(
		 * FeedbackSubjectsOperation.RESULT_OBJECT_SUBJECTS_LIST,
		 * cahcedSubjects);
		 * 
		 * // Bum... Bumm.. Bum.. enoter one bites the dust...
		 * listener.onSuccess(OperationId.FEEDBACK_SUBJECTS, suckersMap);
		 * 
		 * 
		 * } else {
		 */
		// Crapp!!! we've run out of suckers, searching for volunteers.
		CommunicationManager communicationManager = new CommunicationManager();
		/*
		 * Sheep go in, sheep go out.
		 */
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new FeedbackSubjectsOperation(context,
						mServerConfigurations.getHungamaServerUrl_2(),
						mDeviceConfigurations.getHardwareId(),
						mApplicationConfigurations.getPartnerUserId(),
						mApplicationConfigurations.getFeedbackTimeStamp())),
				listener, mContext);
		/*
		 * new CommunicationOperationListener() {
		 * 
		 * @Override public void onSuccess(int operationId, Map<String, Object>
		 * responseObjects) {
		 * 
		 * Putting our new test subjects in the cache, promising them there will
		 * be a cake. No!!!! it's not a lie!~
		 * 
		 * List<String> brandNewSuckerTestSubjects = (List<String>)
		 * responseObjects
		 * .get(FeedbackSubjectsOperation.RESULT_OBJECT_SUBJECTS_LIST);
		 * mCacheManager .storeFeedbackSubjects(brandNewSuckerTestSubjects);
		 * 
		 * listener.onSuccess(operationId, responseObjects); }
		 * 
		 * @Override public void onStart(int operationId) {
		 * listener.onStart(operationId); }
		 * 
		 * @Override public void onFailure(int operationId, ErrorType errorType,
		 * String errorMessage) { listener.onFailure(operationId, errorType,
		 * errorMessage); } }, mContext);
		 */
		// The answer is 42.

		// / }
	}

	public void postFeedback(Map<String, String> feedback,
			CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new FeedbackSubmitOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mDeviceConfigurations
								.getHardwareId(), mApplicationConfigurations
								.getPartnerUserId(), feedback)), listener,
				mContext);
	}

	// ======================================================
	// Sharing Settings
	// ======================================================

	public void getSharingSettings(CommunicationOperationListener listener,
			boolean isUpdate, String key, Integer value) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new ShareSettingsOperation(
						mDeviceConfigurations.getHardwareId(),
						mServerConfigurations.getHungamaServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(),
						mApplicationConfigurations.getPartnerUserId(),
						isUpdate, key, value)), listener, mContext);
	}

	// ======================================================
	// My Stream Settings
	// ======================================================

	public void getMyStreamSettings(CommunicationOperationListener listener,
			boolean isUpdate, String key, Integer value) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new MyStreamSettingsOperation(
						mDeviceConfigurations.getHardwareId(),
						mServerConfigurations.getHungamaServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(),
						mApplicationConfigurations.getPartnerUserId(),
						isUpdate, key, value)), listener, mContext);
	}

	// ======================================================
	// Public helper methods.
	// ======================================================

	public boolean isDeviceOnLine() {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	// ======================================================
	// Private helper methods.
	// ======================================================
	public String getDisplayDensity() {
		String profile = null;
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		try {
			if (metrics != null) {
				if (metrics.widthPixels <= 240) {
					profile = "ldpi";
				} else if (metrics.widthPixels <= 320) {
					profile = "mdpi";
				} else if (metrics.widthPixels <= 480) {// 540
					profile = "hdpi";
				} else {
					profile = "xdpi";
				}
			}
		} catch (Exception e) {
		}
		if (TextUtils.isEmpty(profile)) {
			int densityDpi = mContext.getResources().getDisplayMetrics().densityDpi;
			switch (densityDpi) {
			case DisplayMetrics.DENSITY_LOW:
				return "ldpi";
			case DisplayMetrics.DENSITY_MEDIUM:
				return "mdpi";
			case DisplayMetrics.DENSITY_HIGH:
				return "hdpi";
			case DisplayMetrics.DENSITY_XHIGH:
			case DisplayMetrics.DENSITY_XXHIGH:
				return "xdpi";
			}
			return "hdpi";
		}
		return profile;
	}

	public String getDisplayDensityCampaign() {
		String profile = null;
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		try {
			if (metrics != null) {
				if (metrics.widthPixels <= 240) {
					profile = "ldpi";
				} else if (metrics.widthPixels <= 320) {
					profile = "mdpi";
				} else if (metrics.widthPixels <= 480) {// 540
					profile = "hdpi";
				} else if (metrics.widthPixels <= 1200) {
					profile = "xdpi";
				} else {
					profile = "xxhdpi";
				}
			}
		} catch (Exception e) {
		}
		if (TextUtils.isEmpty(profile)) {
			int densityDpi = mContext.getResources().getDisplayMetrics().densityDpi;
			switch (densityDpi) {
			case DisplayMetrics.DENSITY_LOW:
				return "ldpi";
			case DisplayMetrics.DENSITY_MEDIUM:
				return "mdpi";
			case DisplayMetrics.DENSITY_HIGH:
				return "hdpi";
			case DisplayMetrics.DENSITY_XHIGH:
			case DisplayMetrics.DENSITY_TV:
				return "xdpi";
			case DisplayMetrics.DENSITY_XXHIGH:
			case DisplayMetrics.DENSITY_XXXHIGH:
				return "xxhdpi";
			}
			return "hdpi";
		}
		return profile;
	}

	public int getShowcaseButtonMargin() {
		int margin = 0;
		DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
		try {
			if (metrics != null) {
				if (metrics.widthPixels <= 240) {
					margin = 5;
				} else if (metrics.widthPixels <= 320) {
					margin = 5;
				} else if (metrics.widthPixels <= 480) {// 540
					margin = 5;
				} else {
					margin = 10;
				}
			}
		} catch (Exception e) {
		}
		if (margin == 0) {
			int densityDpi = mContext.getResources().getDisplayMetrics().densityDpi;
			switch (densityDpi) {
			case DisplayMetrics.DENSITY_LOW:
				return 5;
			case DisplayMetrics.DENSITY_MEDIUM:
				return 5;
			case DisplayMetrics.DENSITY_HIGH:
			case DisplayMetrics.DENSITY_XHIGH:
			case DisplayMetrics.DENSITY_XXHIGH:
				return 10;
			}
			return 10;
		}
		return margin;
	}

	// ======================================================
	// Private Operations listeners.
	// ======================================================

	/**
	 * Wrapper CommunicationOperationListener for caching the media items before
	 * they are being sent to the client listener.
	 */
	private class WrapperMediaContentOperationListener implements
			CommunicationOperationListener {

		private final CommunicationOperationListener listener;

		public WrapperMediaContentOperationListener(
				CommunicationOperationListener listener) {
			this.listener = listener;
		}

		@Override
		public void onStart(int operationId) {
			listener.onStart(operationId);
		}

		@Override
		public void onSuccess(int operationId,
				Map<String, Object> responseObjects) {
			// switch (operationId) {
			//
			// case (OperationDefinition.Hungama.OperationId.SUBSCRIPTION): {
			// Logger.i(TAG,
			// "******************** SUBSCRIPTION *********************");
			// SubscriptionResponse subscriptionResponse =
			// (SubscriptionResponse) responseObjects
			// .get(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION);
			// if (subscriptionResponse != null
			// && subscriptionResponse.getPlan() != null
			// && subscriptionResponse.getPlan().size() > 0) {
			//
			// if (subscriptionResponse.getSubscriptionType() ==
			// SubscriptionType.PLAN) {
			// mCacheManager
			// .storeSubscriptionPlans(subscriptionResponse
			// .getPlan());
			// }
			// }
			// break;
			//
			// }
			// }
			listener.onSuccess(operationId, responseObjects);
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType,
				String errorMessage) {
			listener.onFailure(operationId, errorType, errorMessage);
		}

	}

	// private class WrapperCampaignListOperationListener implements
	// CommunicationOperationListener {
	//
	// private final CommunicationOperationListener listener;
	//
	// public WrapperCampaignListOperationListener(
	// CommunicationOperationListener listener) {
	// this.listener = listener;
	// }
	//
	// @Override
	// public void onStart(int operationId) {
	// listener.onStart(operationId);
	// }
	//
	// @Override
	// public void onSuccess(int operationId,
	// Map<String, Object> responseObjects) {
	// switch (operationId) {
	//
	// case OperationDefinition.CatchMedia.OperationId.CAMPAIGN_LIST_READ:
	//
	// List<String> list = (List<String>) responseObjects
	// .get(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST);
	//
	// storeCampaignList(list);
	//
	// break;
	//
	// }
	// listener.onSuccess(operationId, responseObjects);
	// }
	//
	// @Override
	// public void onFailure(int operationId, ErrorType errorType,
	// String errorMessage) {
	// listener.onFailure(operationId, errorType, errorMessage);
	// }
	// }

	private class WrapperCampaignOperationListener implements
			CommunicationOperationListener {

		private final CommunicationOperationListener listener;

		public WrapperCampaignOperationListener(
				CommunicationOperationListener listener) {
			this.listener = listener;
		}

		@Override
		public void onStart(int operationId) {
			listener.onStart(operationId);
		}

		@Override
		public void onSuccess(int operationId,
				Map<String, Object> responseObjects) {
			switch (operationId) {

			case OperationDefinition.CatchMedia.OperationId.CAMPAIGN_READ:

				List<Campaign> campaigns = (List<Campaign>) responseObjects
						.get(CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN);

				// Iterator<Campaign> campaign = campaigns.iterator();
				// Campaign cmp;
				// Placement plc;
				// List<Placement> placements;
				// Iterator<Placement> campaignPl;
				// while (campaign.hasNext()) {
				// cmp = campaign.next();
				// placements = cmp.getPlacements();
				// if (placements == null || placements.size() == 0){
				// campaigns.remove(cmp);
				// System.out.println("Camp removed ::::::::::: " +
				// cmp.getID());
				// } else {
				// campaignPl = placements.iterator();
				// long curr = System.currentTimeMillis();
				// while (campaignPl.hasNext()) {
				// plc = campaignPl.next();
				// if (plc.getEffectiveTillInLong() < curr) {
				// placements.remove(plc);
				// System.out.println("Placement removed ::::::::::: " +
				// plc.getCampaignID());
				// }
				// }
				// if(placements.isEmpty()){
				// campaigns.remove(cmp);
				// System.out.println("Camp1 removed ::::::::::: " +
				// cmp.getID());
				// }
				// }
				// }

				storeCampaign(campaigns, new Callback() {

					@Override
					public void onResult(Boolean gotResponse) {
						// TODO Auto-generated method stub

					}
				});

				// stores the campaigns for case use.
				// storeCampaign(campaigns);

				// extracts the placements from the campaigns.
				// List<Placement> radioPlacements = CampaignsManager
				// .getAllPlacementsOfType(campaigns,
				// ForYouActivity.PLACEMENT_TYPE_RADIO);
				// List<Placement> splashPlacements = CampaignsManager
				// .getAllPlacementsOfType(campaigns,
				// ForYouActivity.PLACEMENT_TYPE_SPLASH);

				// Store the placements.
				// storeRadioPlacement(radioPlacements);
				// storeSplashPlacement(splashPlacements);

				break;

			}
			listener.onSuccess(operationId, responseObjects);
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType,
				String errorMessage) {
			listener.onFailure(operationId, errorType, errorMessage);
		}
	}

	/**
	 * Wrapper listener for the getting the discoveries if there are not stored
	 * preferences for the user.
	 * 
	 * This in the onSucces() will call to the get Discoveries operation and
	 * will take car on invoking the client's listener callback.
	 */
	// private class WrapperGetPreferencesOperationListener implements
	// CommunicationOperationListener {
	//
	// private final CommunicationOperationListener listener;
	//
	// public WrapperGetPreferencesOperationListener(
	// CommunicationOperationListener listener) {
	// this.listener = listener;
	// }
	//
	// @Override
	// public void onStart(int operationId) {
	// this.listener.onStart(operationId);
	// }
	//
	// @Override
	// public void onSuccess(int operationId,
	// Map<String, Object> responseObjects) {
	//
	// CommunicationManager communicationManager = new CommunicationManager();
	// // List<CategoryTypeObject> categoryTypeObjects = mCacheManager
	// // .getStoredPreferences();
	// List<CategoryTypeObject> categoryTypeObjects = new
	// ArrayList<CategoryTypeObject>();
	//
	// DiscoverRetrieveOperation discoverRetrieveOperation = new
	// DiscoverRetrieveOperation(
	// mServerConfigurations.getHungamaServerUrl(),
	// mServerConfigurations.getHungamaAuthKey(),
	// mApplicationConfigurations.getPartnerUserId(),
	// categoryTypeObjects, mCacheManager.getStoredMoods());
	//
	// /*
	// * Creates internal listener to avoid recalling the client's
	// * onStart() and this onSuccess().
	// */
	// communicationManager.performOperationAsync(
	// new HungamaWrapperOperation(listener, mContext,
	// discoverRetrieveOperation),
	// new CommunicationOperationListener() {
	//
	// @Override
	// public void onSuccess(int operationId,
	// Map<String, Object> responseObjects) {
	// WrapperGetPreferencesOperationListener.this.listener
	// .onSuccess(operationId, responseObjects);
	// }
	//
	// @Override
	// public void onStart(int operationId) {
	// }
	//
	// @Override
	// public void onFailure(int operationId,
	// ErrorType errorType, String errorMessage) {
	// WrapperGetPreferencesOperationListener.this.listener
	// .onFailure(operationId, errorType,
	// errorMessage);
	// }
	// }, mContext);
	// }
	//
	// @Override
	// public void onFailure(int operationId, ErrorType errorType,
	// String errorMessage) {
	// this.listener.onFailure(operationId, errorType, errorMessage);
	// }
	//
	// }

	// ======================================================
	// Campaigns.
	// ======================================================

	public boolean storeCampaignList(List<String> list) {
		return mCacheManager.storeCampaignList(list);
	}

	public List<String> getStoredCampaignList() {
		return mCacheManager.getStoredCampaignList();
	}

	public void storeCampaign(List<Campaign> list, Callback callback) {
		mCacheManager.storeCampaign(list, callback);
	}

	public List<Campaign> getStoredCampaign() {
		return mCacheManager.getStoredCampaign();
		// return (new ArrayList<Campaign>());
	}

	// public boolean storeRadioPlacement(List<Placement> list) {
	// return mCacheManager.storeRadioPlacement(list);
	// }
	//
	// public List<Placement> getStoredRadioPlacement() {
	// return mCacheManager.getStoredRadioPlacement();
	// }
	//
	// public boolean storeSplashPlacement(List<Placement> list) {
	// return mCacheManager.storeSplashPlacement(list);
	// }
	//
	// public List<Placement> getStoredSplashPlacement() {
	// return mCacheManager.getStoredSplashPlacement();
	// }

	public Map<Long, Playlist> getStoredPlaylists() {
		return mCacheManager.getStoredPlaylists();

	}

	public void storePlaylists(Map<Long, Playlist> list, Callback callback) {
		mCacheManager.storePlaylists(list, callback);
	}

	public Map<Long, Track> getStoredTracks() {
		return mCacheManager.getStoredTracks();
	}

	public void storeTracks(Map<Long, Track> tracks, Callback callback) {
		mCacheManager.storeTrackList(tracks, callback);
	}

	public boolean storeEvents(List<Event> events, boolean fromPost) {
		if (events != null)
			Logger.e("EventTrack DM.storeEvents fromPost" + fromPost, ""
					+ events.size());
		return mCacheManager.storeEvents(events);
	}

	public List<Event> getStoredEvents() {

		List<Event> list = mCacheManager.getStoredEvents();
		if (list != null)
			Logger.e("EventTrack DM.getStoredEvents", "" + list.size());
		return list;
	}

	// public boolean storeEvent(Event event) {
	// return mCacheManager.storeEvent(event);
	// }

	/**
	 * Update the list of Playlists that are stored in the cache
	 * 

	 * @return
	 */
	public synchronized void updateItemable(Playlist newItem, String action,
			Callback callback) {

		Map<Long, Playlist> itemables = getStoredPlaylists();

		if (itemables == null) {
			// Guess it's the first time using that stored list
			itemables = new HashMap<Long, Playlist>();
		}

		// Check if the Playlist (item) exist
		Playlist playlist = (Playlist) itemables.get(newItem.getId());

		if (action.equalsIgnoreCase(InventoryLightService.ADD)) {

			Logger.i(TAG,
					"Playlist: " + newItem.getName() + " " + newItem.getId()
							+ " " + action);

			if (playlist != null) {
				// Do Nothing
			} else {
				itemables.put(newItem.getId(), newItem);
				mCacheManager.storePlaylists(itemables, callback);
			}

		} else if (action.equalsIgnoreCase(InventoryLightService.MOD)) {

			Logger.i(TAG,
					"Playlist: " + newItem.getName() + " " + newItem.getId()
							+ " " + action);

			if (playlist != null) {

				if (newItem.getName() != null) {
					playlist.setName(newItem.getName());
				}

				if (newItem.getTrackList() != null) {
					playlist.setTrackList(newItem.getTrackList());
				}

				itemables.put(playlist.getId(), playlist);

			} else {
				// MOD is equal to ADD when the PlayList does not exist
				itemables.put(newItem.getId(), newItem);
			}

			mCacheManager.storePlaylists(itemables, callback);

		} else if (action.equalsIgnoreCase(InventoryLightService.DEL)) {

			if (playlist != null) {
				Logger.i(
						TAG,
						"Playlist: " + newItem.getName() + " "
								+ newItem.getId() + " " + action);

				itemables.remove(newItem.getId());
				mCacheManager.storePlaylists(itemables, callback);
			} else {
				// Do Nothing
			}
		}

		// return updated;
	}

	public synchronized void updateTracks(String trackID, String trackName,
			Callback callback) {
		Map<Long, Track> tracks = getStoredTracks();

		if (tracks == null) {
			// Guess it's the first time using that stored list
			tracks = new HashMap<Long, Track>();
		}

		Track track = tracks.get(Long.parseLong(trackID));

		if (track == null) {
			long id = Long.valueOf(trackID);
			// Track newTrack = new Track(id, trackName, "", "", "", "");
			// tracks.put(id, newTrack);
			track = new Track(id, trackName, "", "", "", "", null, 0);
			tracks.put(id, track);
			storeTracks(tracks, callback);
		}
		// System.out.println("inserted Total size ::::: " + tracks.size() +
		// " :: " + trackID);
	}

	public synchronized void updateTracks(MediaTrackDetails mediaTrackDetails,
			Callback callback) {
		if (mediaTrackDetails == null) {
			callback.onResult(false);
			return;
		}

		Map<Long, Track> tracks = getStoredTracks();

		if (tracks == null) {
			callback.onResult(false);
			return;
		}

		Track track = tracks.get(mediaTrackDetails.getId());

		if (track == null) {
			// return false;
		}

		track = new Track(mediaTrackDetails.getId(),
				mediaTrackDetails.getTitle(), mediaTrackDetails.getAlbumName(),
				mediaTrackDetails.getSingers(),
				mediaTrackDetails.getImageUrl(),
				mediaTrackDetails.getBigImageUrl(),
				mediaTrackDetails.getImages(), mediaTrackDetails.getAlbumId());

		tracks.put(track.getId(), track);

		storeTracks(tracks, callback);
	}

	// PlayList request
	public boolean storePlaylistRequest(List<PlaylistRequest> list) {
		return mCacheManager.storeRequestList(list);
	}

	public List<PlaylistRequest> getPlaylistRequest() {
		return mCacheManager.getStoredRequestList();
	}

	// public Intent cretaeForYouActivityIntent(Context context, Node node,
	// String source) {
	//
	// if (node != null) {
	// Intent intent = new Intent(context, ForYouActivity.class);
	//
	// // Save the campaign's text_1 for the header title
	// intent.putExtra(ForYouActivity.CAMPAIGN_TITLE,
	// node.getCampaignTitle());
	//
	// // Source of entry
	// intent.putExtra(ForYouActivity.FLURRY_SOURCE_OF_ENTRTY, source);
	//
	// if (node.getChildNodes() != null && node.getChildNodes().size() > 0) {
	// // Node has children
	// intent.putExtra(ForYouActivity.CLICKED_NODES_CHILDS,
	// (ArrayList<Node>) node.getChildNodes());
	// return intent;
	// // context.startActivity(intent);
	//
	// } else {
	// // Node has no children
	// intent.putExtra(ForYouActivity.CLICKED_NODE, node);
	//
	// if (node.getAction() != null) {
	// Toast.makeText(context, "Action: " + node.getAction(),
	// Toast.LENGTH_LONG).show();
	// return intent;
	// // context.startActivity(intent);
	// } else {
	// // Do nothing for now
	// return null;
	// }
	// }
	// } else {
	// return null;
	// }
	// }

	public void storeApplicationImages(Map<String, Object> imageMap,
			Callback callback) {
		mCacheManager.storeApplicationImages(imageMap, callback);
	}

	public Map<String, Object> getStoredApplicationImages() {
		return mCacheManager.getStoredApplicationImages();
	}

	/**
	 * Starts prefetching Application images if they are not exist in the cache.
	 */
	public void prefetchImagesIfNotExists() {
		Map<String, Object> imageMap = mCacheManager
				.getStoredApplicationImages();
		boolean hasSuccessed = mApplicationConfigurations
				.hasSuccessedPrefetchingApplicationImages();
		Logger.s("set date:" + mApplicationConfigurations.getApiDate());
		Logger.s("current date:"
				+ Utils.getDate(System.currentTimeMillis(), "dd-MM-yy"));
		if (!mApplicationConfigurations.getApiDate().equals(
				Utils.getDate(System.currentTimeMillis(), "dd-MM-yy"))) {
			hasSuccessed = false;
		}
		if (imageMap == null || !hasSuccessed) {
			Intent prefetchMoods = new Intent(getApplicationContext(),
					ImagePrefetchingService.class);
			mContext.startService(prefetchMoods);
		}
	}

	public Drawable getApplicationImage(String imageName) {
		try {
			Drawable image = mCacheManager.getApplicationImage(imageName);
			if (image == null)
				image = new ColorDrawable(Color.BLACK);
			return image;
		} catch (IOException e) {
			e.printStackTrace();
			return new ColorDrawable(Color.BLACK);
		} catch (Error e) {
			e.printStackTrace();
			return new ColorDrawable(Color.BLACK);
		}
	}

	// public String getApplicationImagePath(String imageName) {
	// try {
	// return mCacheManager.getApplicationImagePath(imageName);
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (Error e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	public boolean isApplicationImageExist(String imageName) {
		try {
			Drawable d = mCacheManager.getApplicationImageNew(imageName);
			final boolean isImageExist = (d != null);
			d = null;
			return isImageExist;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	// public boolean getTokenUpdate(String token,
	// CommunicationOperationListener listener) {
	// if (isDeviceOnLine()) {
	// CommunicationManager communicationManager = new CommunicationManager();
	// communicationManager.performOperationAsync(
	// new CMDecoratorOperation(mServerConfigurations
	// .getServerUrl(), new ThirdPartyTokenUpdate(
	// mContext, token)), listener, mContext);
	// return true;
	// }
	// return false;
	// }

	// ======================================================
	// All languages Settings
	// ======================================================

	public void getAllLanguages(CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();

		final LanguageListSettingsOperation langOperation = new LanguageListSettingsOperation(
				mContext, mServerConfigurations.getHungamaServerUrl_2(),
				mServerConfigurations.getHungamaAuthKey(),
				mApplicationConfigurations.getPartnerUserId(),
				mApplicationConfigurations.getAllLanguagesTimeStamp());

		final Handler handle = new Handler(Looper.getMainLooper());

		String response = mCacheManager.getAllLanguagesResponse();

		if (!TextUtils.isEmpty(response)) {

			final CommunicationOperationListener listenerOld = listener;
			listener = new CommunicationOperationListener() {
				@Override
				public void onSuccess(int operationId,
						Map<String, Object> responseObjects) {
				}

				@Override
				public void onStart(int operationId) {
				}

				@Override
				public void onFailure(int operationId, ErrorType errorType,
						String errorMessage) {
				}
			};
			final Response finalResponse = new Response();
			finalResponse.response = response;
			finalResponse.responseCode = CommunicationManager.RESPONSE_SUCCESS_200;

			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						listenerOld.onStart(langOperation.getOperationId());
						final Map<String, Object> map = langOperation
								.parseResponseFromCache(finalResponse, true);

						handle.postDelayed(new Runnable() {
							@Override
							public void run() {
								listenerOld.onSuccess(
										langOperation.getOperationId(), map);
							}
						}, threadsleep);
					} catch (InvalidRequestParametersException e) {
						e.printStackTrace();
					} catch (InvalidRequestTokenException e) {
						e.printStackTrace();
					} catch (InvalidResponseDataException e) {
						e.printStackTrace();
					} catch (OperationCancelledException e) {
						e.printStackTrace();
					}
				}
			}).start();

		}

		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, langOperation), listener, mContext);
	}

	// ======================================================
	// set user selected language Settings
	// ======================================================
	public void postUserLanguageMap(String language,
			CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new LanguagePostOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mServerConfigurations
								.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								language)), listener, mContext);
	}

	// ======================================================
	// Get user selected language Settings
	// ======================================================
	public void GetUserLanguageMap(CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new LanguageSelectedGetOperation(mContext,
								mServerConfigurations.getHungamaServerUrl_2(),
								mServerConfigurations.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								mApplicationConfigurations
										.getUserLanguageTimeStamp())),
				listener, mContext);
	}

	public void getMultiSongDetail(String content_id,
			CommunicationOperationListener listener) {

		String images = ImagesManager.getImageSize(
				ImagesManager.MUSIC_ART_SMALL, getDisplayDensity())
				+ ","
				+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_SMALL,
						getDisplayDensity());

		mTrackSimilarCommunicationManager = new CommunicationManager();
		mTrackSimilarCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new MultiSongDetailOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mServerConfigurations
								.getHungamaAuthKey(),
								mApplicationConfigurations.getPartnerUserId(),
								content_id, getDisplayDensity(), images)),
				listener, mContext);
	}

	// http://apistaging.hungama.com/myplay2/v2/content/music/nqhistory/17028774?auth_key=4bbaa8370603ed0556eda28ab7c4573a&device=android&size=xdpi
	public void getSongCatcherHistory(CommunicationOperationListener listener) {
		mTrackSimilarCommunicationManager = new CommunicationManager();
		mTrackSimilarCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new MultiSongHistoryOperation(mServerConfigurations
								.getHungamaServerUrl_2(),
								mApplicationConfigurations.getPartnerUserId(),
								mContext, mApplicationConfigurations
										.getNQHistoryTimeStamp())), listener,
				mContext);
	}

	public CacheManager getCacheManager() {
		return mCacheManager;
	}

	public void redeemValidateCoupon(CommunicationOperationListener listener,
			String code) {
		if (mRedeemCouponsCommunicationManager == null)
			mRedeemCouponsCommunicationManager = new CommunicationManager();
		mRedeemCouponsCommunicationManager
				.performOperationAsync(
						new HungamaWrapperOperation(
								listener,
								mContext,
								new RedeemCouponOperation(
										mContext,
										mServerConfigurations
												.getHungamaCouponRedeemServerUrl(),
										mApplicationConfigurations
												.getPartnerUserId(),
										RedeemCouponType.VALIDATE_COUPON, code,
										"", "")), listener, mContext);
	}

	public void redeemSendOtp(CommunicationOperationListener listener,
			String mobile) {
		if (mRedeemCouponsCommunicationManager == null)
			mRedeemCouponsCommunicationManager = new CommunicationManager();
		mRedeemCouponsCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new RedeemCouponOperation(mContext,
								mServerConfigurations
										.getHungamaCouponRedeemServerUrl(),
								mApplicationConfigurations.getPartnerUserId(),
								RedeemCouponType.SEND_MOBILE_OTP, "", mobile,
								"")), listener, mContext);
	}

	public void redeemValidateOtp(CommunicationOperationListener listener,
			String mobile, String mobileOtp) {
		if (mRedeemCouponsCommunicationManager == null)
			mRedeemCouponsCommunicationManager = new CommunicationManager();
		mRedeemCouponsCommunicationManager.performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						new RedeemCouponOperation(mContext,
								mServerConfigurations
										.getHungamaCouponRedeemServerUrl(),
								mApplicationConfigurations.getPartnerUserId(),
								RedeemCouponType.VALIDATE_MOBILE_OTP, "",
								mobile, mobileOtp)), listener, mContext);
	}

	public void getPlaylistId(String playlistKey,
			CommunicationOperationListener listener) {
		// try {
		final PlaylistIdOperation playlistIdOperation = new PlaylistIdOperation(
				mContext, mServerConfigurations.getHungamaServerUrl_2(),
				playlistKey);
		new CommunicationManager().performOperationAsync(
				new HungamaWrapperOperation(listener, mContext,
						playlistIdOperation), listener, mContext);
		// } catch (InvalidRequestException e) {
		// e.printStackTrace();
		// } catch (InvalidResponseDataException e) {
		// e.printStackTrace();
		// } catch (OperationCancelledException e) {
		// e.printStackTrace();
		// } catch (NoConnectivityException e) {
		// e.printStackTrace();
		// }
	}

	public void getUserProfileDetail(CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new GetUserProfileOperation(
						mServerConfigurations.getHungamaUserProfileServerUrl(),
						mApplicationConfigurations.getPartnerUserId(),
						getDeviceConfigurations().getHardwareId())), listener,
				mContext);
	}

	public void testApi(CommunicationOperationListener listener) {
		String url = "http://apistaging.hungama.com/webservice/hungama/user/my_profile?";
		CommunicationManager communicationManager = new CommunicationManager();
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url = "http://202.87.41.147/hungamacm_signup/spa/user_profile.php?";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_downloads?";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId(), null)), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_stream?for=everyone&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_stream?for=friends&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_stream?for=me&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_badges?";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/leaderboard?period=all&type=self&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/leaderboard?period=7days&type=self&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/leaderboard?period=all&type=self&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/leaderboard?period=7days&type=self&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/leaderboard?period=all&type=friend&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/leaderboard?period=7days&type=friend&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/leaderboard?period=all&type=other&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/leaderboard?period=7days&type=other&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// ///////
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_favorites?type=album&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_favorites?type=song&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_favorites?type=video&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_favorites?type=playlist&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_favorites?type=liveradio&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/my_favorites?type=ondemandradio&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);

		// album/song/video/playlist/artist/liveradio/ondemandradio
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/add_favorite?"
		// + "&type=album&content_id=359362&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/add_favorite?"
		// + "&type=song&content_id=1539721&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/add_favorite?"
		// + "&type=video&content_id=5646725&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/add_favorite?"
		// + "&type=playlist&content_id=2311&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/add_favorite?"
		// + "&type=liveradio&content_id=1420632539&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/add_favorite?"
		// + "&type=ondemandradio&content_id=12&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/remove_favorite?"
		// + "&type=album&content_id=359362&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/remove_favorite?"
		// + "&type=song&content_id=1539721&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/remove_favorite?"
		// + "&type=video&content_id=5646725&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/remove_favorite?"
		// + "&type=playlist&content_id=2311&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId(), null)),
		// listener, mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/remove_favorite?"
		// + "&type=liveradio&content_id=1420632539&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);
		//
		// url =
		// "http://apistaging.hungama.com/webservice/hungama/user/favorite/remove_favorite?"
		// + "&type=ondemandradio&content_id=12&";
		// communicationManager.performOperationAsync(new
		// HungamaWrapperOperation(
		// listener, mContext, new TestApiOperation(
		// url,
		// mApplicationConfigurations.getPartnerUserId(),
		// getDeviceConfigurations().getHardwareId())), listener,
		// mContext);

		// test("http://apistaging.hungama.com/webservice/hungama/user/settings/save_mystream_settings"
		// +
		// "?musiclisten=1&likes=1&downloads=1&comments=1&videoswatched=1&shares=1&badges=1&",
		// communicationManager,
		// "");
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/settings/get_mystream_settings?",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/settings/save_share_settings"
		// +
		// "?songs_listen=1&my_favorites=1&songs_download=1&my_comments=1&my_badges=1&videos_watched=1&videos_download=1&",
		// communicationManager,
		// "");
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/settings/get_share_settings?",
		// communicationManager, null);

		// test("http://apistaging.hungama.com/webservice/hungama/user/badge_alert?action=favorite&type=song&content_id=1539721&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/badge_alert?action=musicstreaming&type=song&content_id=1539721&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/badge_alert?action=videostreaming&video&content_id=5646725&",
		// communicationManager, null);

		// //
		// test("http://apistaging.hungama.com/webservice/hungama/user/comment/post_comment"
		// + "?content_id=359362&type=album&provider=facebook&comment=nice&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/comment/post_comment"
		// + "?content_id=1539721&type=song&provider=facebook&comment=nice&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/comment/post_comment"
		// + "?content_id=5646725&type=video&provider=facebook&comment=nice&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/comment/post_comment"
		// + "?content_id=2311&type=playlist&provider=facebook&comment=nice&",
		// communicationManager, null);
		//
		test("http://apistaging.hungama.com/webservice/hungama/user/comment/get_comments?content_id=2311&type=playlist&",
				communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/comment/get_comments?content_id=359362&type=album&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/comment/get_comments?content_id=1539721&type=song&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/comment/get_comments?content_id=5646725&type=video&",
		// communicationManager, null);

		// test("http://apistaging.hungama.com/webservice/hungama/settings/appimages.php?",
		// communicationManager, null);

		// Share
		// test("http://apistaging.hungama.com/webservice/hungama/user/share/post_share?"
		// +
		// "content_id=359362&type=album&provider=facebook&share_comment=nice&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share/post_share?"
		// +
		// "content_id=1539721&type=song&provider=facebook&share_comment=nice&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share/post_share?"
		// +
		// "content_id=2311&type=playlist&provider=facebook&share_comment=nice&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share/post_share?"
		// +
		// "content_id=5646725&type=video&provider=facebook&share_comment=nice&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share/post_share?"
		// +
		// "content_id=1592732&type=lyrics&provider=facebook&share_comment=nice&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share/post_share?"
		// +
		// "content_id=1592732&type=trivia&provider=facebook&share_comment=nice&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share_url?"
		// + "content_id=359362&type=album&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share_url?"
		// + "content_id=1539721&type=song&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share_url?"
		// + "content_id=2311&type=playlist&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share_url?"
		// + "content_id=5646725&type=video&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share_url?"
		// + "content_id=1592732&type=lyrics&",
		// communicationManager, null);
		//
		// test("http://apistaging.hungama.com/webservice/hungama/user/share_url?"
		// + "content_id=1592732&type=trivia&",
		// communicationManager, null);

		// Promo unit
		// test("http://apistaging.hungama.com/webservice/hungama/settings/promo?",
		// communicationManager, null);
	}

	private void test(String url, CommunicationManager communicationManager,
			String post) {
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new TestApiOperation(url,
						mApplicationConfigurations.getPartnerUserId(),
						getDeviceConfigurations().getHardwareId(), post)),
				listener, mContext);
	}

	public void getPromoUnit(CommunicationOperationListener listener) {
		CommunicationManager communicationManager = new CommunicationManager();
		communicationManager.performOperationAsync(new HungamaWrapperOperation(
				listener, mContext, new GetPromoUnitOperation(
						mServerConfigurations.getHungamaServerUrl_2(),
						mApplicationConfigurations.getPartnerUserId(),
						getDeviceConfigurations().getHardwareId())), listener,
				mContext);
	}

    public void  getTokenUpdate(String token,
                                  CommunicationOperationListener listener) {
        if (isDeviceOnLine()) {
            CommunicationManager communicationManager = new CommunicationManager();
            communicationManager.performOperationAsync(
                    new CMDecoratorOperation(mServerConfigurations
                            .getServerUrl(), new ThirdPartyTokenUpdate(
                            mContext, token)), listener, mContext);

        }

    }


}
