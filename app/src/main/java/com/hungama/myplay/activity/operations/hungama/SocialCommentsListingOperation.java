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
import com.hungama.myplay.activity.data.dao.hungama.CommentsListingResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Receives the profile for the given user.
 */
public class SocialCommentsListingOperation extends SocialOperation {

	public static final String RESULT_KEY_COMMENTS_LISTING = "result_key_comments_listing";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mContentId;
	private final String mType;
	private final String mStartIndex;
	private final String mLength;
	private final String mUserId;

	public SocialCommentsListingOperation(String serviceUrl, String authKey,
			String contentId, String type, String startIndex, String length,
			String userId) {

		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mContentId = contentId;
		this.mType = type;
		this.mStartIndex = startIndex;
		this.mLength = length;
		this.mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_GET;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);

		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_COMMENT_GET
				+ PARAMS_CONTENT_ID + EQUALS + mContentId + AMPERSAND + "type"
				+ EQUALS + mType + AMPERSAND + "start" + EQUALS + mStartIndex
				+ AMPERSAND + "length" + EQUALS + mLength
				+ HungamaOperation.AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID
				+ HungamaOperation.EQUALS + config.getHardwareId()
				+ HungamaOperation.AMPERSAND + PARAMS_USER_ID + EQUALS
				+ mUserId;
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

			CommentsListingResponse mCommentsListingResponse = gsonParser
					.fromJson(response.response, CommentsListingResponse.class);

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap
					.put(RESULT_KEY_COMMENTS_LISTING, mCommentsListingResponse);

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
