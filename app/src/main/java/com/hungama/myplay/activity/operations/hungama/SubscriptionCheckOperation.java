package com.hungama.myplay.activity.operations.hungama;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.util.Logger;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionCheckOperation extends HungamaOperation {

	private static final String TAG = "SubscriptionCheckOperation";

	public static final String RESPONSE_KEY_SUBSCRIPTION_CHECK = "response_key_subscription_check";

	private final Context mContext;
	private final String mServerUrl;
	private final String mUserId;
	private final String mAuthKey;
	private final String mGoogleEmailId;
	private final String mContentId;
	private final String msisdn;
	private final String imsi;

	public SubscriptionCheckOperation(Context context, String serverUrl,
			String userId, String authKey, String googleEmailId, String contentId) {
		mContext = context;
		mServerUrl = serverUrl;
		mUserId = userId;
		mAuthKey = authKey;
		mGoogleEmailId = googleEmailId;
		mContentId = contentId;
		msisdn = null;
		imsi = null;
	}

	public SubscriptionCheckOperation(Context context, String serverUrl,
									  String userId, String authKey, String googleEmailId, String contentId, String msisdn,
											  String imsi) {
		mContext = context;
		mServerUrl = serverUrl;
		mUserId = userId;
		mAuthKey = authKey;
		mGoogleEmailId = googleEmailId;
		mContentId = contentId;
		this.msisdn = msisdn;
		this.imsi = imsi;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {

		return mServerUrl
				+ HungamaOperation.URL_SEGMENT_SUBSCRIPTION_CHECK_SUBSCRIPTION;
	}

	@Override
	public String getRequestBody() {

		StringBuilder urlBuilder = new StringBuilder();

		DeviceConfigurations config = DeviceConfigurations
				.getInstance(mContext);
		String HardID = config.getHardwareId();

		urlBuilder.append(PARAMS_IDENTITY).append(EQUALS).append(mUserId);
		urlBuilder.append(HungamaOperation.AMPERSAND)
				.append(PARAMS_DOWNLOAD_HARDWARE_ID).append(EQUALS)
				.append(HardID);
		urlBuilder.append(HungamaOperation.AMPERSAND).append(PARAMS_PLATFORM)
				.append(EQUALS).append(VALUE_DEVICE);
		urlBuilder.append(HungamaOperation.AMPERSAND).append(PARAMS_PRODUCT)
				.append(EQUALS).append(VALUE_PRODUCT);

		if (mGoogleEmailId != null) {
			urlBuilder.append(AMPERSAND).append(PARAMS_GOOGLE_EMAIL_ID)
					.append(EQUALS).append(mGoogleEmailId);
		}
		if (!TextUtils.isEmpty(mContentId) && !mContentId.equals("0")) {
			urlBuilder.append(AMPERSAND).append(PARAMS_CONTENT_ID)
					.append(EQUALS).append(mContentId);
		}
		if (!TextUtils.isEmpty(msisdn)) {
			urlBuilder.append(AMPERSAND).append("msisdn")
					.append(EQUALS).append(msisdn);
		}
		if (!TextUtils.isEmpty(imsi)) {
			urlBuilder.append(AMPERSAND).append("imsi")
					.append(EQUALS).append(imsi);
		}
		return urlBuilder.toString();
	}

	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		Logger.s("Check subscription :::::::: " + response);
		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}
		// response =
		// "{\"response\": {\"code\": 1,\"trial_plan_display\": 0,\"previous_plan\": {\"plan_id\": 165,\"subscription_status\": \"Y\",\"purchase_date\": \"2014-05-28 15:44:59\",\"validity_date\": \"2014-06-27 15:44:59\",\"type\": \"google\",\"subscription_id\": \"hungama_premium_subscription_freetrial\",\"trial\": \"Y\",\"trial_expiry_days_left\": 30}}}";
		try {
			response.response = response.response.replace("{\"response\":", "");
			response.response = response.response.substring(0,
					response.response.length() - 1);

			SubscriptionStatusResponse subscriptionStatusResponse = gson
					.fromJson(response.response, SubscriptionStatusResponse.class);
			resultMap.put(RESPONSE_KEY_SUBSCRIPTION_CHECK,
					subscriptionStatusResponse);

			DataManager dataManager = DataManager.getInstance(mContext);
			// if store in cache succeeded - set UserHasSubscriptionPlan=true
			// and plan validity date in ApplicationConfigurations
			dataManager.storeCurrentSubscriptionPlanNew(subscriptionStatusResponse);
			if(subscriptionStatusResponse!=null && !TextUtils.isEmpty(subscriptionStatusResponse.getAffCode())) {
				dataManager.getApplicationConfigurations().setSubscriptionAffCode(subscriptionStatusResponse.getAffCode());;
			}
			if(subscriptionStatusResponse.getSubscription()!=null && subscriptionStatusResponse.getSubscription()
					.getSubscriptionStatus()==1){
				mContext.sendBroadcast(new Intent(HomeActivity.ACTION_NOTIFY_ADAPTER));
			}
		} catch (JsonSyntaxException exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException();
		} catch (StringIndexOutOfBoundsException exception) {
			exception.printStackTrace();
			throw new InvalidResponseDataException();
		} catch(Exception e){
            e.printStackTrace();
        }

		return resultMap;
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
