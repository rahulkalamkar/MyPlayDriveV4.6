package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

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
import com.hungama.myplay.activity.data.dao.hungama.ReadTimeStamp;
import com.hungama.myplay.activity.data.dao.hungama.social.Profile;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

/**
 * Receives the profile for the given user.
 */
public class SocialProfileOperation extends SocialOperation {

	public static final String RESULT_KEY_PROFILE = "result_key_profile";

	private final String mServiceUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final Context mContext;
	private final String timestamp_cache;

	public SocialProfileOperation(Context c, String serviceUrl, String authKey,
			String userId, String timestamp_cache) {
		this.mServiceUrl = serviceUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		mContext = c;
		this.timestamp_cache = timestamp_cache;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serviceUrl = mServiceUrl + URL_SEGMENT_SOCIAL_PROFILE
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

		Gson gsonParser = new Gson();

		try {

			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				response.response = new CacheManager(mContext)
						.getUserProfileResponse();
			}

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

//			String lastTimesStamp = null;
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations.getInstance(mContext);
			try {
				if(mUserId.equals(mApplicationConfigurations.getPartnerUserId())) {
					ReadTimeStamp time = (ReadTimeStamp) gsonParser.fromJson(
							response.response, ReadTimeStamp.class);
					String lastTimesStamp = time.getTimeStamp();
//				DataManager mDataManager;
//				mDataManager = DataManager.getInstance(mContext);
//				ApplicationConfigurations mApplicationConfigurations = mDataManager
//						.getApplicationConfigurations();
					mApplicationConfigurations
							.setUserProfileTimeStamp(lastTimesStamp);
					Logger.e("lastTimesStamp profile++", lastTimesStamp);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			// JSONParser jsonParser = new JSONParser();
			// Map<String, Object> catalogMap = null;
			// try {
			// catalogMap = (Map<String, Object>) jsonParser
			// .parse(response.response);
			// } catch (ParseException e1) {
			// e1.printStackTrace();
			// }
			//
			// String lastTimesStamp = null;
			// try {
			// if (catalogMap.containsKey(KEY_LAST_MODIFIED)) {
			// lastTimesStamp = catalogMap.get(KEY_LAST_MODIFIED)
			// .toString();
			// }
			// DataManager mDataManager;
			// mDataManager = DataManager.getInstance(mContext);
			// ApplicationConfigurations mApplicationConfigurations =
			// mDataManager
			// .getApplicationConfigurations();
			// mApplicationConfigurations
			// .setUserProfileTimeStamp(lastTimesStamp);
			// Logger.e("lastTimesStamp 111111 user profile", lastTimesStamp);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }

			Profile profile = gsonParser.fromJson(response.response,
					Profile.class);

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_PROFILE, profile);

			Callback callback = new Callback() {
				@Override
				public void onResult(Boolean gotResponse) {

				}
			};
			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response) &&
					mUserId.equals(mApplicationConfigurations.getPartnerUserId()))
				new CacheManager(mContext).storeUserProfileResponse(
						response.response, callback);

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
