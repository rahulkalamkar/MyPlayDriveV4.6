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
public class SocialCommentsPostOperation extends SocialOperation {

	public static final String RESULT_KEY_COMMENTS_POST = "result_key_comments_post";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mContentId;
	private final String mType;
	private final String mProvider;
	private final String mComment;
	private final Context context;

	public SocialCommentsPostOperation(Context context, String serviceUrl,
			String authKey, String contentId, String type, String userId,
			String provider, String comment) {
		this.context = context;
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mContentId = contentId;
		this.mType = type;
		this.mUserId = userId;
		this.mProvider = provider;
		this.mComment = comment;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_COMMENT_POST;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {

		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_COMMENT_POST;
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String urlParams = serviceUrl + PARAMS_USER_ID + EQUALS + mUserId
				+ AMPERSAND + PARAMS_CONTENT_ID + EQUALS + mContentId
				+ AMPERSAND + "type" + EQUALS + mType + AMPERSAND + "provider"
				+ EQUALS + mProvider + AMPERSAND + "comment" + EQUALS
				+ mComment + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId();

		return urlParams;
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
			resultMap.put(RESULT_KEY_COMMENTS_POST, mCommentsPostResponse);

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
