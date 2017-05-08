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
import com.hungama.myplay.activity.data.dao.hungama.CommentsPostResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Receives the profile for the given user.
 */
public class ShareOperation extends SocialOperation {

	public static final String RESULT_KEY_SHARE = "result_key_share";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mContentId;
	private final String mType;
	private final String mProvider;
	private final String mUserText;

	public ShareOperation(String serviceUrl, String authKey, String contentId,
			String type, String userId, String provider, String userText) {

		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mContentId = contentId;
		this.mType = type;
		this.mUserId = userId;
		this.mProvider = provider;
		this.mUserText = userText;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_SHARE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {

		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_SHARE
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_CONTENT_ID + EQUALS + mContentId + AMPERSAND + "type"
				+ EQUALS + mType + AMPERSAND + "provider" + EQUALS + mProvider
				+ AMPERSAND + "share_comment" + EQUALS + mUserText + AMPERSAND
				+ "device" + EQUALS + "Android" + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId();
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

			CommentsPostResponse mCommentsPostResponse = gsonParser.fromJson(
					response.response, CommentsPostResponse.class);

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_SHARE, mCommentsPostResponse);

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
