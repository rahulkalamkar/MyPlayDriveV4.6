package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CacheManager;
import com.hungama.myplay.activity.data.CacheManager.Callback;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileBadges;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class SocialProfileBadgesOperation extends SocialOperation {

	public static final String RESULT_KEY_PROFILE_BADGES = "result_key_profile_badges";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final Context mContext;
	private final String timestamp_cache;

	public SocialProfileBadgesOperation(Context c, String serviceUrl,
			String authKey, String userId, String timestamp_cache) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		mContext = c;
		this.timestamp_cache = timestamp_cache;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_BADGES;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_PROFILE_BADGES
				+ PARAMS_USER_ID + EQUALS + mUserId
				+ HungamaOperation.AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID
				+ HungamaOperation.EQUALS + config.getHardwareId();
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

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		Gson gsonParser = new Gson();

		try {

			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				response.response = new CacheManager(mContext)
						.getUserProfileBadgesResponse();
			}

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			JSONParser jsonParser = new JSONParser();
			Map<String, Object> catalogMap = null;
			try {
				catalogMap = (Map<String, Object>) jsonParser
						.parse(response.response);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}

			String lastTimesStamp = null;
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations.getInstance(mContext);
			try {
				if(mUserId.equals(mApplicationConfigurations.getPartnerUserId())) {
					if (catalogMap.containsKey(KEY_LAST_MODIFIED)) {
						lastTimesStamp = catalogMap.get(KEY_LAST_MODIFIED)
								.toString();
					}
//				DataManager mDataManager;
//				mDataManager = DataManager.getInstance(mContext);
//				ApplicationConfigurations mApplicationConfigurations = mDataManager
//						.getApplicationConfigurations();
					mApplicationConfigurations
							.setUserProfileBadgesTimeStamp(lastTimesStamp);
					Logger.e("lastTimesStamp 111111 user profile", lastTimesStamp);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			ProfileBadges profileBadges = gsonParser.fromJson(
					response.response, ProfileBadges.class);

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_PROFILE_BADGES, profileBadges);

			Callback callback = new Callback() {
				@Override
				public void onResult(Boolean gotResponse) {

				}
			};
			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response) &&
					mUserId.equals(mApplicationConfigurations.getPartnerUserId()))
				new CacheManager(mContext).storeUserProfileBadgesResponse(
						response.response, callback);

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

	public String getUserId(){
		return mUserId;
	}
}
