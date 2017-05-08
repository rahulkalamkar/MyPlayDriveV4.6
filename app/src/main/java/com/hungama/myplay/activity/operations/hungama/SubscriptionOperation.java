package com.hungama.myplay.activity.operations.hungama;

import android.content.Context;
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
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionNotifyBillingResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class SubscriptionOperation extends HungamaOperation {

	private static final String TAG = "SubscriptionOperation";

	public static final String RESPONSE_KEY_SUBSCRIPTION = "response_key_subscription";

	private final String mServerUrl;
	private final String mUserId;
	private final String mPlanId;
	private final SubscriptionType mSubscriptionType;
	private final String mAuthKey;
	private final String mPlanType;
	private final String mCode;
	private final String mPurchaseToken;
	private final String mGoogleEmailId;
	private final String session;
	private final boolean mTrial;
	private final String mAffCode;
	final Context mcontext;
	private final String mSubscriptionId;
	private final String mContentId;

	public SubscriptionOperation(Context context, String serverUrl,
			String planId, String planType, String userId,
			SubscriptionType subscriptionType, String authKey, String code,
			String purchaseToken, String googleEmailId, boolean trial,
			String transactionSession, String affCode, String mSubscriptionId, String mContentId) {
		mcontext = context;
		mServerUrl = serverUrl;
		mUserId = userId;
		mPlanId = planId;
		mSubscriptionType = subscriptionType;
		mAuthKey = authKey;
		mPlanType = planType;
		mCode = code;
		mPurchaseToken = purchaseToken;
		mGoogleEmailId = googleEmailId;
		mTrial = trial;
		session = transactionSession;
		mAffCode = affCode;
		this.mSubscriptionId = mSubscriptionId;
		this.mContentId = mContentId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SUBSCRIPTION;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.POST;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String segmentType = null;
		if (mSubscriptionType == SubscriptionType.CHARGE){
			segmentType = HungamaOperation.URL_SEGMENT_SUBSCRIPTION_CHARGE;
		} else if (mSubscriptionType == SubscriptionType.UNSUBSCRIBE) {
			segmentType = HungamaOperation.URL_SEGMENT_SUBSCRIPTION_UNSUBSCRIBE;
		}
		return mServerUrl + segmentType;
	}

	@Override
	public String getRequestBody() {
		String params = null;
		DeviceConfigurations config = DeviceConfigurations
				.getInstance(mcontext);
		String HardID = config.getHardwareId();
		String mGoogleEmailId = Utils.getAccountName(mcontext);

		if (mSubscriptionType == SubscriptionType.CHARGE) {
			params = PARAMS_IDENTITY + EQUALS + mUserId
					+ AMPERSAND + PARAMS_PRODUCT + EQUALS + VALUE_PRODUCT
					+ AMPERSAND + PARAMS_PLATFORM + EQUALS + VALUE_DEVICE
					+ AMPERSAND + PARAMS_GOOGLE_EMAIL_ID + EQUALS + mGoogleEmailId
					+ AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + HardID
					+ AMPERSAND + PARAMS_SUBSCRIPTION_ID + EQUALS + mPlanId
					+ AMPERSAND + PARAMS_PURCHASE_TOKEN + EQUALS + mPurchaseToken
					+ AMPERSAND + PARAMS_AFF_CODE + EQUALS + mAffCode;

			if(!TextUtils.isEmpty(mContentId) && !mContentId.equals("0")){
				params += AMPERSAND + PARAMS_CONTENT_ID + EQUALS + mContentId;
			}
		} else if (mSubscriptionType == SubscriptionType.UNSUBSCRIBE) {
			params = PARAMS_AUTH_KEY + EQUALS + mAuthKey
					+ HungamaOperation.AMPERSAND + PARAMS_USER_ID + EQUALS
					+ mUserId + HungamaOperation.AMPERSAND + PARAMS_PLAN_ID
					+ EQUALS + mPlanId;

			params += HungamaOperation.AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID
					+ EQUALS + HardID;
			params += HungamaOperation.AMPERSAND + PARAMS_DEVICE + EQUALS
					+ VALUE_DEVICE;
		}

		if (mGoogleEmailId != null
				&& mSubscriptionType != SubscriptionType.CHARGE) {
			params += AMPERSAND + PARAMS_GOOGLE_EMAIL_ID + EQUALS
					+ mGoogleEmailId;
		}
		return params;
	}

	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		try {
			// response.response =
			// "{\"response\":{\"code\":1,\"transaction_session\":\"csuhigr0ta1stsomtnv0e8tor4\","
			// +
			// "\"plan\":[{\"plan_id\":122,\"plan_name\":\"Monthly\",\"plan_price\":110,\"plan_currency\":\"Rs.\""
			// +
			// ",\"plan_duration\":30,\"type\":\"google\"},{\"plan_id\":158,\"plan_name\":\"Monthly\","
			// +
			// "\"plan_price\":110,\"plan_currency\":\"Rs.\",\"plan_duration\":30,\"type\":\"windows\"},"
			// +
			// "{\"plan_id\":5,\"plan_name\":\"Daily\",\"plan_price\":5,\"plan_currency\":\"Rs.\","
			// +
			// "\"plan_duration\":1,\"type\":\"mobile\",\"consent\":1,\"plan_image_key\":\"daily5\"},"
			// +
			// "{\"plan_id\":6,\"plan_name\":\"Weekly\",\"plan_price\":30,\"plan_currency\":\"Rs.\","
			// +
			// "\"plan_duration\":7,\"type\":\"mobile\",\"consent\":1,\"plan_image_key\":\"weekly30\"}]}}";
			response.response = response.response.replace("{\"response\":", "");
			response.response = response.response.substring(0,
					response.response.length() - 1);

			if (mSubscriptionType == SubscriptionType.CHARGE) {
				SubscriptionNotifyBillingResponse subscriptionsubscriptionNotifyBillingResponse = gson
						.fromJson(response.response, SubscriptionNotifyBillingResponse.class);
				resultMap.put(RESPONSE_KEY_SUBSCRIPTION, subscriptionsubscriptionNotifyBillingResponse);
			} else {
				SubscriptionResponse subscriptionResponse = gson
						.fromJson(response.response, SubscriptionResponse.class);
				subscriptionResponse.setSubscriptionType(mSubscriptionType);
				resultMap.put(RESPONSE_KEY_SUBSCRIPTION, subscriptionResponse);
			}
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} catch (IndexOutOfBoundsException e) {
			throw new InvalidResponseDataException();
		}

		return resultMap;
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
