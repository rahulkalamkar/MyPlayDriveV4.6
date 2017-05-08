package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
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
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.ReadTimeStamp;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

/**
 * Retrieves Categories for filtering Media Items.
 */
public class MediaCategoriesOperation extends HungamaOperation {

	private static final String TAG = "MediaCategoriesOperation";

	public static final String RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE = "result_key_object_media_content_type";
	public static final String RESULT_KEY_OBJECT_CATEGORIES = "result_key_object_categories";

	private final String mServerUrl;
	private final String mAuthKey;
	private final MediaContentType mMediaContentType;
	private final String timestamp_cache;
	private final String mUserId;
	private final Context context;

	public MediaCategoriesOperation(Context context, String serverUrl,
			String authKey, MediaContentType mediaContentType, String userId,
			String timestamp_cache) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mMediaContentType = mediaContentType;
		this.context = context;
		this.timestamp_cache = timestamp_cache;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		return mServerUrl + URL_SEGMENT_CONTENT
				+ mMediaContentType.toString().toLowerCase() + "/"
				+ URL_SEGMENT_CATEGORIES + "?" + PARAMS_DOWNLOAD_HARDWARE_ID
				+ HungamaOperation.EQUALS + config.getHardwareId() + "&"
				+ PARAMS_USER_ID + "=" + mUserId;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		// List<Category> resultCategories;
		Gson gsonParser = new Gson();

		try {
			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				response.response = new CacheManager(context)
						.getCategoriesGenerResponse();
			}

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			String lastTimesStamp = null;
			try {
				ReadTimeStamp time = (ReadTimeStamp) gsonParser.fromJson(
						response.response, ReadTimeStamp.class);
				lastTimesStamp = time.getTimeStamp();
				DataManager mDataManager;
				mDataManager = DataManager.getInstance(context);
				ApplicationConfigurations mApplicationConfigurations = mDataManager
						.getApplicationConfigurations();
				mApplicationConfigurations
						.setCategoriesGenerTimeStamp(lastTimesStamp);
				Logger.e("lastTimesStamp //", lastTimesStamp);
			} catch (Exception e) {
				e.printStackTrace();
			}

			MusicCategoriesResponse mMusicCategoriesResponse = gsonParser
					.fromJson(
							new JSONObject(response.response).getJSONObject(
									"response").toString(),
							MusicCategoriesResponse.class);

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_OBJECT_CATEGORIES,
					mMusicCategoriesResponse);
			resultMap.put(RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE,
					mMediaContentType);

			Callback callback = new Callback() {
				@Override
				public void onResult(Boolean gotResponse) {

				}
			};
			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response))
				new CacheManager(context).storeCategoriesGenerResponse(
						response.response, callback);

			return resultMap;

		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Parsing error.");
		}
	}

	// private List<CategoryTypeObject> parseGenres(Map<String, Object> map){
	// if (map.containsKey("genre")) {
	//
	// Map<String, Object> attributeMap;
	// long id;
	// String name;
	// List<CategoryTypeObject> genres = new ArrayList<CategoryTypeObject>();
	//
	// List<Map<String, Object>> genreMaps = (List<Map<String, Object>>)
	// map.get("genre");
	// for (Map<String, Object> genreMap : genreMaps) {
	// attributeMap = (Map<String, Object>) genreMap.get(KEY_ATTRIBUTES);
	// id = (Long) attributeMap.get(KEY_ID);
	// name = (String) attributeMap.get(KEY_NAME);
	//
	// genres.add(new Genre(id, name));
	// }
	//
	// return genres;
	// }
	//
	// return null;
	// }
	//
	// private List<CategoryTypeObject> parseSubCategories(Map<String, Object>
	// map){
	// if (map.containsKey("subcategory")) {
	//
	// Map<String, Object> attributeMap;
	// long id;
	// String name;
	// List<CategoryTypeObject> subcategories = new
	// ArrayList<CategoryTypeObject>();
	//
	// List<Map<String, Object>> categoriesMaps = (List<Map<String, Object>>)
	// map.get("subcategory");
	// for (Map<String, Object> categoryMap : categoriesMaps) {
	// attributeMap = (Map<String, Object>) categoryMap.get(KEY_ATTRIBUTES);
	// id = (Long) attributeMap.get(KEY_ID);
	// name = (String) attributeMap.get(KEY_NAME);
	//
	// subcategories.add(new Category(id, name, null));
	// }
	//
	// return subcategories;
	// }
	//
	// return null;
	// }

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return timestamp_cache;
	}

}
