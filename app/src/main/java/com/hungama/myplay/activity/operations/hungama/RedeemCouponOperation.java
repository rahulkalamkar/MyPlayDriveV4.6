package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

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
import com.hungama.myplay.activity.data.dao.hungama.RedeemCouponResponse;
import com.hungama.myplay.activity.data.dao.hungama.RedeemCouponType;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class RedeemCouponOperation extends HungamaOperation {

	private static final String TAG = "SubscriptionOperation";

	public static final String RESPONSE_KEY_REDEEM_COUPON = "response_key_redeem_coupon";

	private final String mServerUrl;
	private final String mUserId;
	private final String mMobileNo;
	private final String mOtpCode;
	private final RedeemCouponType mRedeemCouponType;
	// private final String mAuthKey;
	// private final String mPlanType;
	private final String mCode;
	// private final String mPurchaseToken;
	// private final String mGoogleEmailId;
	// private final String session;
	// private final boolean mTrial;
	final Context mcontext;

	public RedeemCouponOperation(Context context, String serverUrl,
	/* String planId, String planType, */String userId,
			RedeemCouponType redeemCouponType, /* String authKey, */
			String code, String mobileNo, String otpCode
	// String purchaseToken, String googleEmailId, boolean trial,
	/* String transactionSession */) {
		mcontext = context;
		mServerUrl = serverUrl;
		mUserId = userId;
		// mPlanId = planId;
		mRedeemCouponType = redeemCouponType;
		// mAuthKey = authKey;
		// mPlanType = planType;
		mCode = code;
		// mPurchaseToken = purchaseToken;
		// mGoogleEmailId = googleEmailId;
		// mTrial = trial;
		// session = transactionSession;
		mMobileNo = mobileNo;
		mOtpCode = otpCode;
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
		if (mRedeemCouponType == RedeemCouponType.VALIDATE_COUPON) {
			segmentType = HungamaOperation.URL_SEGMENT_REDEEM_VALIDATE_COUPON;
		} else if (mRedeemCouponType == RedeemCouponType.SEND_MOBILE_OTP) {
			segmentType = HungamaOperation.URL_SEGMENT_REDEEM_SEND_MOBILE_OTP;
		} else if (mRedeemCouponType == RedeemCouponType.VALIDATE_MOBILE_OTP) {
			segmentType = HungamaOperation.URL_SEGMENT_REDEEM_VALIDATE_MOBILE_OTP;
		}
		return mServerUrl + segmentType;
	}

	@Override
	public String getRequestBody() {
		String params = null;
		DeviceConfigurations config = DeviceConfigurations
				.getInstance(mcontext);
		String HardID = config.getHardwareId();

		// ServerConfigurations Sconfig = new ServerConfigurations(mcontext);
		// String affiliate_id = Sconfig.getReferralId();
		// String mGoogleEmailId = Utils.getAccountName(mcontext);

		params = PARAMS_USER_ID + EQUALS + mUserId + HungamaOperation.AMPERSAND
				+ PARAMS_DEVICE + EQUALS + VALUE_DEVICE
				+ HungamaOperation.AMPERSAND + PARAMS_DOWNLOAD_HARDWARE_ID
				+ EQUALS + HardID;

		if (mRedeemCouponType == RedeemCouponType.VALIDATE_COUPON) {
			params += HungamaOperation.AMPERSAND + PARAMS_COUPON_CODE + EQUALS
					+ mCode;
		} else if (mRedeemCouponType == RedeemCouponType.SEND_MOBILE_OTP) {
			params += HungamaOperation.AMPERSAND + PARAMS_MOBILE + EQUALS
					+ mMobileNo;
		} else if (mRedeemCouponType == RedeemCouponType.VALIDATE_MOBILE_OTP) {
			params += HungamaOperation.AMPERSAND + PARAMS_MOBILE + EQUALS
					+ mMobileNo + HungamaOperation.AMPERSAND + PARAMS_OTP_CODE
					+ EQUALS + mOtpCode;
		}
		// if (affiliate_id != null)
		// params += HungamaOperation.AMPERSAND
		// + PARAMS_DOWNLOAD_AFFILIATE_ID
		// + HungamaOperation.EQUALS + affiliate_id;
		//
		// }
		//
		// if (mGoogleEmailId != null && mRedeemCouponType !=
		// SubscriptionType.CHARGE) {
		// params += AMPERSAND + PARAMS_GOOGLE_EMAIL_ID + EQUALS
		// + mGoogleEmailId;
		// }

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
			response.response = response.response.replace("{\"response\":", "");
			response.response = response.response.substring(0,
					response.response.length() - 1);

			RedeemCouponResponse redeemCouponResponse = (RedeemCouponResponse) gson
					.fromJson(response.response, RedeemCouponResponse.class);
			redeemCouponResponse.setRedeemCouponType(mRedeemCouponType);
			resultMap.put(RESPONSE_KEY_REDEEM_COUPON, redeemCouponResponse);

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
