/**
 * 
 */
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
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * @author Idan
 *
 */
public class AddToFavoriteOperation extends SocialOperation {

	public static final String RESULT_KEY_ADD_TO_FAVORITE = "result_key_add_to_favorite";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mContentId;
	private final String mMediaType;
	private final String mHardwareID;

	public AddToFavoriteOperation(String serviceUrl, String authKey,
			String userId, String mediaType, String contentId, String hardwareID) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mMediaType = mediaType.toLowerCase();
		this.mContentId = contentId;
		this.mHardwareID = hardwareID;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {

		String serviceUrl = mServiceUrl
				+ URL_SEGMENT_SOCIAL_ADD_TO_FAVORITE
				// + PARAMS_AUTH_KEY + EQUALS + mAuthKey + AMPERSAND
				+ PARAMS_CONTENT_ID + EQUALS + mContentId + AMPERSAND
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_MEDIA_TYPE + EQUALS + mMediaType + AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + mHardwareID;

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

			BaseHungamaResponse hungamaResponse = gsonParser.fromJson(
					response.response, BaseHungamaResponse.class);
			if (response.responseCode == 200)
				hungamaResponse.setCode(1);
			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_ADD_TO_FAVORITE, hungamaResponse);

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
