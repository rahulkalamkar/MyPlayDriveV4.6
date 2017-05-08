package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;

import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.operations.OperationDefinition.Hungama.OperationId;

/**
 * 
 * INPUT PARAMETERS (when USER is logged in) user_id subject app_exp feed_txt
 * phone_details debug_txt auth_key
 *
 * INPUT PARAMETERS (when USER is not logged) first_name last_name email mobile
 * subject app_exp feed_txt phone_details debug_txt auth_key
 */
public class FeedbackSubmitOperation extends HungamaOperation {

	private static final String TAG = "FeedbackSubmitOperation";

	private final String mServerUrl;

	private final String mHardwareid;
	private final String mUserId;
	private final Map<String, String> mFeedbackFields;

	public FeedbackSubmitOperation(String serverUrl, String hardwareid,
			String userId, Map<String, String> feedbackFields) {
		mServerUrl = serverUrl;
		this.mHardwareid = hardwareid;
		this.mUserId = userId;

		mFeedbackFields = feedbackFields;
	}

	@Override
	public int getOperationId() {
		return OperationId.FEEDBACK_SUBMIT;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {

		StringBuilder requestUrlBuilder = new StringBuilder();
		requestUrlBuilder.append(mServerUrl).append(URL_SEGMENT_FEEDBACK_SAVE);
		requestUrlBuilder.append(PARAMS_USER_ID).append(EQUALS).append(mUserId)
				.append(AMPERSAND);

		// build the params.
		Iterator<Entry<String, String>> iterator = mFeedbackFields.entrySet()
				.iterator();
		Map.Entry<String, String> entry = null;
		while (iterator.hasNext()) {
			entry = (Map.Entry<String, String>) iterator.next();
			requestUrlBuilder.append(entry.getKey()).append(EQUALS)
					.append(entry.getValue()).append(AMPERSAND);
		}
		requestUrlBuilder.append(PARAMS_DOWNLOAD_HARDWARE_ID).append(EQUALS)
				.append(mHardwareid);
		// adds the authentication key.

		String requestUrl = requestUrlBuilder.toString().replace(" ", "%20");

		return requestUrl;
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
		// la la la la la la la la la la la la la la la.
		return new HashMap<String, Object>();
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
