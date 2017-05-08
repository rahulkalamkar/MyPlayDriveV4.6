package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
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
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemsResponseCatalog;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class MediaContentOperationPaging extends HungamaOperation {

	private static final String TAG = "MediaContentOperationPaging";

	public static final String RESULT_KEY_OBJECT_MEDIA_ITEMS = "result_key_object_media_items";
	public static final String RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE = "result_key_object_media_content_type";
	public static final String RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE = "result_key_object_media_category_type";
	// public static final String RESULT_KEY_OBJECT_MEDIA_CATEGORY =
	// "result_key_object_media_category";
	public static final String RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE = "response_key_object_media_items_response";

	private final Context mContext;

	private final String mServerUrl;
	private final String mAuthKey;
	private final MediaContentType mContentType;
	private final MediaCategoryType mItemCategoryType;
	private final Category mCategory;
	private final String mUserId;
	private final String mStart;
	private final String mLength;
	private final String mTimestamp_cache;
	private final String mImages;

	public MediaContentOperationPaging(Context context, String serverUrl,
			String authKey, MediaContentType contentType,
			MediaCategoryType itemCategoryType, Category category,
			String userId, String start, String length, String timestamp_cache,
			String images) {
		mContext = context;
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mContentType = contentType;
		mItemCategoryType = itemCategoryType;
		mCategory = category;
		mUserId = userId;
		mStart = start;
		mLength = length;
		mTimestamp_cache = timestamp_cache;
		mImages = images;
	}

	@Override
	public int getOperationId() {
		return DataManager
				.getOperationIdForMediaCategoryType(mItemCategoryType);
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {

		String stringContentType = mContentType.toString().toLowerCase();
		String stringItemCategoryType = mItemCategoryType.toString()
				.toLowerCase();
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);

		String serviceUrl = mServerUrl + URL_SEGMENT_CONTENT
				+ stringContentType + "/" + stringItemCategoryType + "?"
				+ PARAMS_USER_ID + "=" + mUserId + HungamaOperation.AMPERSAND
				+ PARAMS_START + HungamaOperation.EQUALS + mStart
				+ HungamaOperation.AMPERSAND + PARAMS_LENGTH
				+ HungamaOperation.EQUALS + mLength
				+ HungamaOperation.AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID
				+ HungamaOperation.EQUALS + config.getHardwareId();
		if (mImages != null && !TextUtils.isEmpty(mImages)) {
			serviceUrl = serviceUrl + "&images=" + mImages;
		}

		if (mCategory != null && !TextUtils.isEmpty(mCategory.getName())) {
			serviceUrl = serviceUrl + "&category=" + mCategory.getName();
		}

		Logger.i(TAG + "Target", "URL = " + serviceUrl);

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
		return parseResponseFromCache(response, false);
	}

	public Map<String, Object> parseResponseFromCache(Response response,
			boolean isFromCache) throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		List<MediaItem> items = null;
		try {
			if ((TextUtils.isEmpty(response.response) || (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500 || response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400))
					&& mStart.equals(String.valueOf(1))) {
				if (mContentType == MediaContentType.MUSIC
						&& mItemCategoryType == MediaCategoryType.LATEST) {
					response.response = new CacheManager(mContext)
							.getMusicLatestResponse();
				} else if (mContentType == MediaContentType.MUSIC
						&& mItemCategoryType == MediaCategoryType.POPULAR) {
					response.response = new CacheManager(mContext)
							.getMusicFeaturedResponse();
				} else if (mContentType == MediaContentType.VIDEO
						&& mItemCategoryType == MediaCategoryType.LATEST) {
					response.response = new CacheManager(mContext)
							.getVideoLatestResponse();
				}
			}

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			if (response.response.contains("\"images\":[]")) {
				response.response.replace("\"images\":[]", "\"images\":{}");
			}

			MediaItemsResponseCatalog mediaItemsResponseCatalog = (MediaItemsResponseCatalog) gson
					.fromJson(response.response,
							MediaItemsResponseCatalog.class);

			String lastTimesStamp = null;
			lastTimesStamp = mediaItemsResponseCatalog.getTimeStamp();

			// try {
			// ReadTimeStamp time = (ReadTimeStamp) gson.fromJson(
			// response.response, ReadTimeStamp.class);
			// lastTimesStamp = time.getTimeStamp();
			// // JSONObject object=new JSONObject(response);
			// // lastTimesStamp=object.getString("last_modified");
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			//
			Logger.e("lastTimesStamp ---*", lastTimesStamp);

			if (mediaItemsResponseCatalog != null) {
				items = mediaItemsResponseCatalog.getCatalog().getContent();
			}

			if (items == null) {
				items = new ArrayList<MediaItem>();
			}

			Logger.e("items:--- ", "" + items.size());
			// TODO: temporally solving the differentiating issue between Music
			// and Videos, solve this when inserting also campaigns.
			for (MediaItem mediaItem : items) {
				if (mediaItem != null) {
					mediaItem.setMediaContentType(mContentType);
				}
			}

			// stores it in the cache manager.
			if (!Utils.isListEmpty(items)) {
				DataManager dataManager = DataManager.getInstance(mContext);
				dataManager.storeMediaItems(mContentType, mItemCategoryType,
						items);
			}

			HashMap<String, Object> resultMap = new HashMap<String, Object>();

			resultMap.put(RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE, mContentType);
			resultMap.put(RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE,
					mItemCategoryType);
			resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEMS, items);
			resultMap.put(RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE,
					mediaItemsResponseCatalog.getCatalog());
			resultMap.put(HashTagListOperation.RESULT_KEY_MESSAGE,
					mediaItemsResponseCatalog.getMessage());

			Callback callback = new Callback() {

				@Override
				public void onResult(Boolean gotResponse) {

				}
			};

			if (mContentType == MediaContentType.MUSIC
					&& mItemCategoryType == MediaCategoryType.LATEST
					&& mStart.equals(String.valueOf(1))) {
				DataManager mDataManager;
				mDataManager = DataManager.getInstance(mContext);
				ApplicationConfigurations mApplicationConfigurations = mDataManager
						.getApplicationConfigurations();
				mApplicationConfigurations
						.setMusicLatestTimeStamp(lastTimesStamp);
			} else if (mContentType == MediaContentType.MUSIC
					&& mItemCategoryType == MediaCategoryType.POPULAR
					&& mStart.equals(String.valueOf(1))) {
				DataManager mDataManager;
				mDataManager = DataManager.getInstance(mContext);
				ApplicationConfigurations mApplicationConfigurations = mDataManager
						.getApplicationConfigurations();
				mApplicationConfigurations
						.setMusicPopularTimeStamp(lastTimesStamp);
			} else if (mContentType == MediaContentType.VIDEO
					&& mStart.equals(String.valueOf(1))) {
				DataManager mDataManager;
				mDataManager = DataManager.getInstance(mContext);
				ApplicationConfigurations mApplicationConfigurations = mDataManager
						.getApplicationConfigurations();
				mApplicationConfigurations
						.setVideoLatestTimeStamp(lastTimesStamp);
			}

			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response)) {
				if (mContentType == MediaContentType.MUSIC
						&& mItemCategoryType == MediaCategoryType.LATEST
						&& mStart.equals(String.valueOf(1)) && !isFromCache)
					new CacheManager(mContext).storeMusicLatestResponse(
							response.response, callback);
				else if (mContentType == MediaContentType.MUSIC
						&& mItemCategoryType == MediaCategoryType.POPULAR
						&& mStart.equals(String.valueOf(1)) && !isFromCache)
					new CacheManager(mContext).storeMusicFeaturedResponse(
							response.response, callback);
				else if (mContentType == MediaContentType.VIDEO
						&& mItemCategoryType == MediaCategoryType.LATEST
						&& mStart.equals(String.valueOf(1)) && !isFromCache)
					new CacheManager(mContext).storeVideoLatestResponse(
							response.response, callback);
			}

			return resultMap;

		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} catch (Exception exception) {
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		if (mStart.equals("1")) {
			Logger.e("getTimeStampCache:", mTimestamp_cache);
			return mTimestamp_cache;
		} else {
			return "";
		}
	}

}