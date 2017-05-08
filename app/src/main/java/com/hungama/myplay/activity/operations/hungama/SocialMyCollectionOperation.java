package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MyCollectionResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Receives the profile for the given user.
 */
public class SocialMyCollectionOperation extends SocialOperation {

	public static final String RESULT_KEY_MY_COLLECTION = "result_key_my_collection";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mImages;
	private final String type;

	public static final String STARTINDEX = "startIndex";

	public SocialMyCollectionOperation(String serviceUrl, String authKey,
			String userId, String images, String type) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mImages = images;
		this.type = type;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_MY_COLLECTION;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serviceUrl = mServiceUrl
				+ URL_SEGMENT_SOCIAL_PROFILE_MY_COLLECTION + PARAMS_USER_ID
				+ EQUALS + mUserId + AMPERSAND + STARTINDEX + EQUALS + "0"
				+ AMPERSAND + PARAMS_LENGTH + EQUALS + "1000"
				+ HungamaOperation.AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID
				+ HungamaOperation.EQUALS + config.getHardwareId()
				+ HungamaOperation.AMPERSAND + PARAMS_TYPE + EQUALS + type;
		if (mImages != null && !TextUtils.isEmpty(mImages)) {
			serviceUrl = serviceUrl + "&images=" + mImages;
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

		response.response = removeUglyResponseWrappingObjectFromResponse(response.response);

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		Gson gsonParser = new Gson();

		try {

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			MyCollectionResponse mMyCollectionResponse = gsonParser.fromJson(
					response.response, MyCollectionResponse.class);

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_MY_COLLECTION, mMyCollectionResponse);

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
