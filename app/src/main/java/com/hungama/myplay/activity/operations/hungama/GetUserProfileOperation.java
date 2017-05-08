package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.dao.hungama.UserProfileResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Utils;

public class GetUserProfileOperation extends HungamaOperation {

	private static final String TAG = "NewVersionCheckOperation";

	public static final String RESPONSE_KEY_USER_DETAIL = "response_key_user_detail";

	private final String mServerUrl;
	private final String mUserId;
	private final String mHardwareId;

	public GetUserProfileOperation(String serverUrl, String userId,
			String hardwareId) {
		mServerUrl = serverUrl;
		mUserId = userId;
		mHardwareId = hardwareId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.GET_USER_PROFILE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String urlParams = null;

		urlParams = PARAMS_USER_ID + HungamaOperation.EQUALS + mUserId
				+ HungamaOperation.AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID
				+ HungamaOperation.EQUALS + mHardwareId;

		return mServerUrl + urlParams;
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

		Gson gson = new Gson();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		try {
			if (!response.response.equalsIgnoreCase(Utils.TEXT_EMPTY)) {
				// Map<String, Object> responseMap = (Map<String, Object>)
				// jsonParser
				// .parse(response.response);
				UserProfileResponse userProfileResponse = (UserProfileResponse) gson
						.fromJson(new JSONObject(response.response)
								.getJSONObject("response").toString(),
								UserProfileResponse.class);
				if (userProfileResponse.getCode() == 200) {
					resultMap
							.put(RESPONSE_KEY_USER_DETAIL, userProfileResponse);
				} else {
					resultMap = null;
				}
			}
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} /*
		 * catch (ParseException e) { Logger.i(TAG, e.getMessage());
		 * e.printStackTrace(); }
		 */catch (Exception e) {
			throw new InvalidResponseDataException();
		}
		return resultMap;
	}

	@Override
	public String getTimeStampCache() {
		return null;
	}
}
