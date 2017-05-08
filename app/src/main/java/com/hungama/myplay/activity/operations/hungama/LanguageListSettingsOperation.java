package com.hungama.myplay.activity.operations.hungama;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
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
import com.hungama.myplay.activity.data.dao.hungama.LanguageResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.FileUtils;
import com.hungama.myplay.activity.util.Logger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Receives the profile for the given user.
 */
public class LanguageListSettingsOperation extends SocialOperation {

	 public static final String RESULT_KEY_LANGUAGE_SETTINGS_LIST = "new_languages";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final Context context;
	private final String timestamp_cache;

	public LanguageListSettingsOperation(Context context, String serviceUrl,
			String authKey, String mUserId, String timestamp_cache) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.context = context;
		this.mUserId = mUserId;
		this.timestamp_cache = timestamp_cache;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = "";
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		serviceUrl = mServiceUrl + URL_SEGMENT_LANGUAGE_SETTINGS_LIST
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + config.getHardwareId();

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

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return timestamp_cache;
	}

	public Map<String, Object> parseResponseFromCache(Response response,
			boolean isFromCache) throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

//		if(!mServiceUrl.contains("apistaging"))
//			response.response = FileUtils.readFileFromAssets(context, "LanguageResponse.json");

		JSONParser jsonParser = new JSONParser();
		try {
			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400) {
				response.response = new CacheManager(context)
						.getAllLanguagesResponse();
			}

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			Map<String, Object> responseMap = (Map<String, Object>) jsonParser
					.parse(response.response);
			String lastTimesStamp = null;
			try {
				if (responseMap.containsKey(KEY_LAST_MODIFIED)) {
					lastTimesStamp = responseMap.get(KEY_LAST_MODIFIED)
							.toString();
				}
				DataManager mDataManager;
				mDataManager = DataManager.getInstance(context);
				ApplicationConfigurations mApplicationConfigurations = mDataManager
						.getApplicationConfigurations();
				mApplicationConfigurations
						.setAllLanguagesTimeStamp(lastTimesStamp);
				Logger.e("lastTimesStamp 111111 all languages", lastTimesStamp);
			} catch (Exception e) {
				e.printStackTrace();
			}

//			List<LanguageResponse> languageResponse = new ArrayList<LanguageResponse>();
			if (responseMap.containsKey(KEY_RESPONSE)) {
				try {
					JSONObject responseLanguagesMap = (JSONObject) responseMap.get(KEY_RESPONSE);
					//				List<Object> objects = (List<Object>) responseLanguagesMap.get("languages");
					//				Logger.s(" ::::::::::::: " + responseLanguagesMap.toString());
					Type listType = new TypeToken<ArrayList<LanguageResponse>>() {
					}.getType();
					Gson gson = new GsonBuilder().create();
					List<LanguageResponse> languageResponse = gson.fromJson(responseLanguagesMap.get(RESULT_KEY_LANGUAGE_SETTINGS_LIST).toString(),
							listType);
					//				Logger.s(" ::::::::::::: " + moods.size());
					responseMap.clear();
					responseMap.put(RESULT_KEY_LANGUAGE_SETTINGS_LIST, languageResponse);
				} catch (Exception e){
					Logger.printStackTrace(e);
				}
			}

			if (!isFromCache) {
				Callback callback = new Callback() {
					@Override
					public void onResult(Boolean gotResponse) {

					}
				};
				if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
						&& !TextUtils.isEmpty(response.response))
					new CacheManager(context).storeGetAllLanguagesResponse(
							response.response, callback);
			}
			return responseMap;

		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Parsing error.");
		}
	}

}
