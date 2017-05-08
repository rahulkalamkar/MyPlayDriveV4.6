/**
 * 
 */
package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CacheManager;
import com.hungama.myplay.activity.data.CacheManager.Callback;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class SocialProfileFavoriteMediaItemsOperation extends SocialOperation {

	public static final String RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS = "result_key_profile_favorite_media_items";
	public static final String RESULT_KEY_MEDIA_TYPE = "result_key_profile_leaderboard";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final MediaType mMediaType;
	private final Context mContext;
	private final String timestamp_cache;
	private final String images;
	String type = null;

	public SocialProfileFavoriteMediaItemsOperation(Context context,
			String serviceUrl, String authKey, String userId,
			MediaType mediaType, String timestamp_cache, String images) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mMediaType = mediaType;
		mContext = context;
		this.timestamp_cache = timestamp_cache;
		this.images = images;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {

		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serviceName = null;
		serviceName = URL_SEGMENT_SOCIAL_PROFILE_FAVORITE;

		// album/song/video/playlist/artist/liveradio/ondemandradio

		if (mMediaType == MediaType.ALBUM) {
			type = "album";
		} else if (mMediaType == MediaType.TRACK) {
			type = "song";
		} else if (mMediaType == MediaType.PLAYLIST) {
			type = "playlist";
		} else if (mMediaType == MediaType.VIDEO) {
			type = "video";
		} else if (mMediaType == MediaType.ARTIST) {
			type = "ondemandradio";
		}

		String imageParams = "";
		if (!TextUtils.isEmpty(images))
			imageParams = "&images=" + images;

		String serviceUrl = mServiceUrl + serviceName + PARAMS_USER_ID + EQUALS
				+ mUserId + AMPERSAND + PARAMS_TYPE + EQUALS + type
				+ HungamaOperation.AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID
				+ HungamaOperation.EQUALS + config.getHardwareId()
				+ imageParams;

		return serviceUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		if (!TextUtils.isEmpty(response.response))
			response.response = removeUglyResponseWrappingObjectFromResponse(response.response);

		Gson gsonParser = new Gson();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {

			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				response.response = new CacheManager(mContext)
						.getUserProfileFavoriteResponse(type);
			}

			Logger.e("response profile", "sss" + response.response);

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			ProfileFavoriteMediaItems profileFavoriteMediaItems = gsonParser
					.fromJson(response.response,
							ProfileFavoriteMediaItems.class);

			// fixes the mediaItems
			List<MediaItem> mediaItems = profileFavoriteMediaItems.mediaItems;

			DataManager mDataManager;
			mDataManager = DataManager.getInstance(mContext);
			ApplicationConfigurations mApplicationConfigurations = mDataManager
					.getApplicationConfigurations();
			if(mUserId.equals(mApplicationConfigurations.getPartnerUserId())) {
				if (mMediaType == MediaType.ALBUM) {
					mApplicationConfigurations
							.setUserProfileFavoriteAlbumTimeStamp(""
									+ profileFavoriteMediaItems.getTimestamp());
				} else if (mMediaType == MediaType.TRACK) {
					mApplicationConfigurations.setUserProfileFavoriteTimeStamp(""
							+ profileFavoriteMediaItems.getTimestamp());
				} else if (mMediaType == MediaType.PLAYLIST) {
					mApplicationConfigurations
							.setUserProfileFavoritePlaylistTimeStamp(""
									+ profileFavoriteMediaItems.getTimestamp());
				} else if (mMediaType == MediaType.VIDEO) {
					mApplicationConfigurations
							.setUserProfileFavoriteVideosTimeStamp(""
									+ profileFavoriteMediaItems.getTimestamp());
				} else if (mMediaType == MediaType.ARTIST) {
					mApplicationConfigurations
							.setUserProfileFavoriteArtistTimeStamp(""
									+ profileFavoriteMediaItems.getTimestamp());
				}
			}

			for (MediaItem mediaItem : mediaItems) {
				if (mMediaType == MediaType.ALBUM
						|| mMediaType == MediaType.PLAYLIST
						|| mMediaType == MediaType.TRACK) {
					mediaItem.setMediaContentType(MediaContentType.MUSIC);
				} else if (mMediaType == MediaType.VIDEO) {
					mediaItem.setMediaContentType(MediaContentType.VIDEO);
				} else if (mMediaType == MediaType.ARTIST) {
					mediaItem.setMediaContentType(MediaContentType.RADIO);
				} else {
					continue;
				}

				mediaItem.setMediaType(mMediaType);
			}

			resultMap.put(RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS,
					profileFavoriteMediaItems);
			resultMap.put(RESULT_KEY_MEDIA_TYPE, mMediaType);

			Callback callback = new Callback() {
				@Override
				public void onResult(Boolean gotResponse) {
				}
			};
			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response) &&
					mUserId.equals(mApplicationConfigurations.getPartnerUserId()))
				new CacheManager(mContext).storeUserProfileFavoriteResponse(
						response.response, type, callback);

			return resultMap;

		} catch (JsonSyntaxException e) {
			e.printStackTrace();
			// throw new InvalidResponseDataException();
		} catch (Exception e) {
			e.printStackTrace();
			// throw new InvalidResponseDataException();
		}

		resultMap.put(RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS, null);
		resultMap.put(RESULT_KEY_MEDIA_TYPE, mMediaType);
		return resultMap;

	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return timestamp_cache;
	}

	public String getUserId(){
		return mUserId;
	}
}
