package com.hungama.myplay.activity.operations.hungama;

import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.text.TextUtils;

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
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

/**
 * Receives the profile for the given user.
 */
public class LanguageSelectedGetOperation extends SocialOperation {

	// public static final String RESULT_KEY_LANGUAGE_GET =
	// "result_key_language_get";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final Context context;
	private final String timestamp_cache;

	public LanguageSelectedGetOperation(Context context, String serviceUrl,
			String authKey, String userId, String timestamp_cache) {

		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.context = context;
		this.mUserId = userId;
		this.timestamp_cache = timestamp_cache;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_GET;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serviceUrl = mServiceUrl + URL_SEGMENT_LANGUAGE_SETTINGS_GET
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND

				+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + config.getHardwareId();

		return serviceUrl;
	}

	@Override
	public String getRequestBody() {

		// String urlParams = PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
		// + "language" + EQUALS + selected_language + AMPERSAND
		// + PARAMS_AUTH_KEY + EQUALS + mAuthKey;
		// ;
		return null;
	}

	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		JSONParser jsonParser = new JSONParser();
		try {
			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				response.response = new CacheManager(context)
						.getUserLanguageResponse();
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
						.setUserLanguageTimeStamp(lastTimesStamp);
				Logger.e("lastTimesStamp 111111 user language", lastTimesStamp);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Callback callback = new Callback() {
				@Override
				public void onResult(Boolean gotResponse) {

				}
			};
			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response))
				new CacheManager(context).storeGetUserLanguageResponse(
						response.response, callback);

			return responseMap;

		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
