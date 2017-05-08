package com.hungama.myplay.activity.operations.hungama;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MyPreferencesResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Retrieve saved Discoveries of the application's user.
 */
public class PreferencesSaveOperation extends HungamaOperation {

	private static final String TAG = "PreferencesRetrieveOperation";

	public static final String RESPONSE_KEY_PREFERENCES_SAVE = "response_key_preferences_save";

	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mPreferenceId;
	private final MediaContentType mediaContenetType;
	private final String category;
	private final String gener;

	public PreferencesSaveOperation(String serverUrl, String authKey,
			String userId, String preferenceId,
			MediaContentType mediaContenetType, String category, String gener) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		mPreferenceId = preferenceId;
		this.mediaContenetType = mediaContenetType;
		this.category = category;
		this.gener = gener;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.PREFERENCES_SAVE;
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
			serverURL.append(URL_SEGMENT_PREFERENCES_VIDEO_SAVE);
		else
			serverURL.append(URL_SEGMENT_PREFERENCES_SAVE);
		// authentication properties.

		serverURL.append(PARAMS_USER_ID).append(EQUALS).append(mUserId)
				.append(AMPERSAND);
		// serverURL.append("preference_id").append(EQUALS).append(mPreferenceId).append(AMPERSAND);
		serverURL.append(PARAMS_DOWNLOAD_HARDWARE_ID).append(EQUALS)
				.append(config.getHardwareId()).append(AMPERSAND);
		serverURL.append(PARAMS_CATEGORY_NAME).append(EQUALS).append(category)
				.append(AMPERSAND);
		serverURL.append(PARAMS_GENER_NAME).append(EQUALS).append(gener);

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

		try {
			response.response = response.response.replace("{\"response\":", "");
			response.response = response.response.substring(0,
					response.response.length() - 1);

			MyPreferencesResponse myPreferencesResponse = (MyPreferencesResponse) gson
					.fromJson(response.response, MyPreferencesResponse.class);
			myPreferencesResponse.setCode(response.responseCode);
			resultMap.put(RESPONSE_KEY_PREFERENCES_SAVE, myPreferencesResponse);

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
		return null;
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
