package com.hungama.myplay.activity.operations.hungama;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;

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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MyPreferencesResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

/**
 * Retrieve saved Discoveries of the application's user.
 */
public class PreferencesRetrieveOperation extends HungamaOperation {

	private static final String TAG = "PreferencesRetrieveOperation";

	public static final String RESPONSE_KEY_PREFERENCES_RETRIEVE = "response_key_preferences";
	public static final String RESPONSE_KEY_PREFERENCES_CONTENT_TYPE = "response_key_preferences_content_type";

	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final MediaContentType mediaContenetType;
	private final Context context;
	private final String timestamp_cache;

	public PreferencesRetrieveOperation(Context context, String serverUrl,
			String authKey, String userId, MediaContentType mediaContenetType,
			String timestamp_cache) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		this.mediaContenetType = mediaContenetType;
		this.context = context;
		this.timestamp_cache = timestamp_cache;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.PREFERENCES_RETRIEVE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		StringBuilder serverURL = new StringBuilder();
		// adds the base server url.
		serverURL.append(mServerUrl);
		// adds the service path.
		if (mediaContenetType == MediaContentType.VIDEO)
			serverURL.append(URL_SEGMENT_PREFERENCES_VIDEO_RETREIVE);
		else
			serverURL.append(URL_SEGMENT_PREFERENCES_RETREIVE);
		// authentication properties.

		serverURL.append(PARAMS_USER_ID).append(EQUALS).append(mUserId)
				.append(AMPERSAND);
		serverURL.append(PARAMS_DOWNLOAD_HARDWARE_ID).append(EQUALS)
				.append(config.getHardwareId());

		return serverURL.toString();
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

		Gson gson = new GsonBuilder().create();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}
		JSONParser parser = new JSONParser();
		try {
			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				response.response = new CacheManager(context)
						.getPrefGetCategoryResponse();
			}

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			Map<String, Object> catalogMap = (Map<String, Object>) parser
					.parse(response.response);

			String lastTimesStamp = null;
			try {
				if (catalogMap.containsKey(KEY_LAST_MODIFIED)) {
					lastTimesStamp = catalogMap.get(KEY_LAST_MODIFIED)
							.toString();
				}
				DataManager mDataManager;
				mDataManager = DataManager.getInstance(context);
				ApplicationConfigurations mApplicationConfigurations = mDataManager
						.getApplicationConfigurations();
				mApplicationConfigurations
						.setPreferenceGetCategoryTimeStamp(lastTimesStamp);
				Logger.e("lastTimesStamp 111111 Preferences Get Category",
						lastTimesStamp);
			} catch (Exception e) {
				e.printStackTrace();
			}

			response.response = catalogMap.get("response").toString();

			MyPreferencesResponse myPreferencesResponse = (MyPreferencesResponse) gson
					.fromJson(response.response, MyPreferencesResponse.class);
			myPreferencesResponse.setCode(response.responseCode);
			resultMap.put(RESPONSE_KEY_PREFERENCES_RETRIEVE,
					myPreferencesResponse);
			resultMap.put(RESPONSE_KEY_PREFERENCES_CONTENT_TYPE,
					mediaContenetType);

			Callback callback = new Callback() {

				@Override
				public void onResult(Boolean gotResponse) {

				}
			};
			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response))
				new CacheManager(context).storePrefGetCategoryResponse(
						response.response, callback);

		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} catch (Exception exception) {
			throw new InvalidResponseDataException();
		}

		return resultMap;
	}

	// private Mood findMoodById(int id) {
	// for (Mood mood : mCachedMoods) {
	// if (mood.getId() == id)
	// return mood;
	// }
	// return null;
	// }
	//
	// private Mood findMoodByTag(String tag) {
	// for (Mood mood : mCachedMoods) {
	// if (mood.getName().equals(tag))
	// return mood;
	// }
	// return null;
	// }

	private List<String> aggregateString(String stringToAggregate) {
		String[] aggregation = stringToAggregate.split(",");
		return Arrays.asList(aggregation);
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return timestamp_cache;
	}

	// private Genre findGenreByName(String genreName) {
	//
	// Category category;
	// CategoryTypeObject genre = null;
	//
	// for (CategoryTypeObject categoryTypeObject : mCachedMyPreferences) {
	// if (categoryTypeObject instanceof Category){
	// category = (Category) categoryTypeObject;
	// for (int i = 0; i < category.getChildCount(); i++) {
	// genre = category.getChildAt(i);
	// if (genre.getName().equals(genreName)) {
	// return (Genre) genre;
	// }
	// }
	// }
	// }
	// return null;
	// }
	//
	// private Category findCategoryByName(String categoryName) {
	// for (CategoryTypeObject categoryTypeObject : mCachedMyPreferences) {
	// if (categoryTypeObject instanceof Category &&
	// categoryTypeObject.getName().equals(categoryName)){
	// return (Category) categoryTypeObject;
	// }
	// }
	// return null;
	// }

}
