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
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileLeaderboard;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class SocialProfileLeaderboardOperation extends SocialOperation {

	public static final String RESULT_KEY_PROFILE_LEADERBOARD = "result_key_profile_leaderboard";
	public static final String RESULT_KEY_PROFILE_LEADERBOARD_TYPE = "result_key_profile_leaderboard_type";
	public static final String RESULT_KEY_PROFILE_LEADERBOARD_PERIOD = "result_key_profile_leaderboard_period";

	private static final String KEY_PARAMS_TYPE = "type";
	private static final String KEY_PARAMS_PERIOD = "period";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final ProfileLeaderboard.TYPE mType;
	private final ProfileLeaderboard.PERIOD mPeriod;
	private final Context mContext;
	private final String timestamp_cache;

	public SocialProfileLeaderboardOperation(Context c, String serviceUrl,
			String authKey, String userId, ProfileLeaderboard.TYPE type,
			ProfileLeaderboard.PERIOD period, String timestamp_cache) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.mType = type;
		this.mPeriod = period;
		mContext = c;
		this.timestamp_cache = timestamp_cache;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_LEADERBOARD;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);

		String serviceUrl = mServiceUrl
				+ URL_SEGMENT_SOCIAL_PROFILE_LEADERBOARD + PARAMS_USER_ID
				+ EQUALS + mUserId + AMPERSAND + KEY_PARAMS_TYPE + EQUALS
				+ mType.name + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId();

		if (mPeriod == ProfileLeaderboard.PERIOD.SEVEN) {
			serviceUrl = serviceUrl + AMPERSAND + KEY_PARAMS_PERIOD + EQUALS
					+ mPeriod.name;
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
		if (!TextUtils.isEmpty(response.response))
			response.response = removeUglyResponseWrappingObjectFromResponse(response.response);

		Gson gsonParser = new Gson();

		try {

			ProfileLeaderboard profileLeaderboard = gsonParser.fromJson(
					response.response, ProfileLeaderboard.class);

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_PROFILE_LEADERBOARD, profileLeaderboard);
			resultMap.put(RESULT_KEY_PROFILE_LEADERBOARD_TYPE, mType);
			resultMap.put(RESULT_KEY_PROFILE_LEADERBOARD_PERIOD, mPeriod);

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
		return timestamp_cache;
	}

}
