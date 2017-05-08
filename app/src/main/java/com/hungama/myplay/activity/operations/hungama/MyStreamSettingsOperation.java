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
import com.hungama.myplay.activity.data.dao.hungama.MyStreamSettingsResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Receives the profile for the given user.
 */
public class MyStreamSettingsOperation extends SocialOperation {

	public static final String RESULT_KEY_MY_STREAM_SETTINGS = "result_key_my_stream_settings";

	private final String mServiceUrl;
	// private final String mAuthKey;
	private final String mUserId;
	private final boolean mIsUpdate;
	private final String mKey;
	private final String mHardwareid;
	private final Integer mValue;

	public MyStreamSettingsOperation(String hardverid, String serviceUrl,
			String authKey, String userId, boolean isUpdate, String key,
			Integer value) {
		this.mHardwareid = hardverid;
		this.mServiceUrl = serviceUrl;
		// this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mIsUpdate = isUpdate;
		this.mKey = key;
		this.mValue = value;
	}

	@Override
	public int getOperationId() {

		if (mIsUpdate) {
			return OperationDefinition.Hungama.OperationId.MY_STREAM_SETTINGS_UPDATE;
		} else {
			return OperationDefinition.Hungama.OperationId.MY_STREAM_SETTINGS;
		}
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {

		String serviceUrl = "";

		if (mIsUpdate) {
			serviceUrl = mServiceUrl + URL_SEGMENT_MY_STREAM_SETTINGS_UPDATE
					+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
					+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + mHardwareid
					+ AMPERSAND + mKey + EQUALS + mValue;

		} else {
			serviceUrl = mServiceUrl + URL_SEGMENT_MY_STREAM_SETTINGS
					+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
					+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + mHardwareid;
		}

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

		// response =
		// "{\"response\":{\"code\":1,\"message\":\"success\",\"data\":{\"musiclisten\":1,\"likes\":1,\"downloads\":1,\"comments\":1,\"videowatched\":1,\"shares\":1,\"badges\":1,\"trivia\":1}}}";
		response.response = removeUglyResponseWrappingObjectFromResponse(response.response);

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		Gson gsonParser = new Gson();

		try {

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			MyStreamSettingsResponse myStreamSettingsResponse = gsonParser
					.fromJson(response.response, MyStreamSettingsResponse.class);

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_MY_STREAM_SETTINGS,
					myStreamSettingsResponse);

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
