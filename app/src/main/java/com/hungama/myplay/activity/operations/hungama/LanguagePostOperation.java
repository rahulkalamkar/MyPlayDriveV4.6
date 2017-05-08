package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.LanguageSaveResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Receives the profile for the given user.
 */
public class LanguagePostOperation extends SocialOperation {

	public static final String RESULT_KEY_LANGUAGE_POST = "result_key_language_post";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String selected_language;

	public LanguagePostOperation(String serviceUrl, String authKey,
			String userId, String selected_language) {

		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.selected_language = selected_language;

	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.LANGUAGE_SETTINGS_POST;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serviceUrl = mServiceUrl + URL_SEGMENT_LANGUAGE_SETTINGS_POST
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_LANGUAGE_NAME + EQUALS + selected_language + AMPERSAND

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

		response.response = removeUglyResponseWrappingObjectFromResponse(response.response);

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		Gson gsonParser = new Gson();

		try {

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			LanguageSaveResponse mlangsaveResponse = gsonParser.fromJson(
					response.response, LanguageSaveResponse.class);
			mlangsaveResponse.setCode(response.responseCode);

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_LANGUAGE_POST, mlangsaveResponse);

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			return resultMap;

		} catch (JsonSyntaxException e) {
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
