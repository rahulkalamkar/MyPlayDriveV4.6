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
import com.hungama.myplay.activity.data.dao.hungama.PromoUnit;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Utils;

public class GetPromoUnitOperation extends HungamaOperation {

	private static final String TAG = "PromoUnitOperation";

	public static final String RESPONSE_KEY_PROMO_UNIT = "response_key_user_detail";

	private final String mServerUrl;
	private final String mUserId;
	private final String mHardwareId;

	public GetPromoUnitOperation(String serverUrl, String userId,
			String hardwareId) {
		mServerUrl = serverUrl;
		mUserId = userId;
		mHardwareId = hardwareId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.GET_PROMO_UNIT;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String urlParams = "";
		urlParams = URL_SEGMENT_PROMO_UNIT + PARAMS_USER_ID
				+ HungamaOperation.EQUALS + mUserId
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

//		response.response = "{\"last_modified\":1453382012,\"response\":{\"promo_name\":\"MOBIKWIK Offer\",\"images\":{\"image_306x86\"" +
//				":[\"http:\\/\\/cms.hungama.com.s3.amazonaws.com\\/live\\/mp_promounitbanner\\/MOBIKWIK_Offer_1_iphone_306x86_1_306x86.jpeg\"]" +
//				",\"image_613x172\":[\"http:\\/\\/cms.hungama.com.s3.amazonaws.com\\/live\\/mp_promounitbanner\\/MOBIKWIK_Offer_1_iphone_" +
//				"613x172_1_613x172.jpeg\"],\"image_344x86\":[\"http:\\/\\/cms.hungama.com.s3.amazonaws.com\\/live\\/mp_promounitbanner\\/" +
//				"MOBIKWIK_Offer_1_android_344x86_1_343x86.jpeg\"],\"image_516x129\":[\"http:\\/\\/cms.hungama.com.s3.amazonaws.com\\/live\\/" +
//				"mp_promounitbanner\\/MOBIKWIK_Offer_1_android_516x129_1_516x129.jpeg\"],\"image_688x172\":[\"http:\\/\\/" +
//				"cms.hungama.com.s3.amazonaws.com\\/live\\/mp_promounitbanner\\/MOBIKWIK_Offer_1_android_688x172_1_688x173.jpeg\"],\"image_" +
//				"1032x259\":[\"http:\\/\\/cms.hungama.com.s3.amazonaws.com\\/live\\/mp_promounitbanner\\/MOBIKWIK_Offer_1_android_1032x259_1_" +
//				"1032x259.jpeg\"]},\"landing_url\":\"ua:\\/\\/callback\\/?code=20\",\"show_profile\":" +
////				"[\"Logged in User\",\"Non Logged Users\",\"Free Trial Users\",\"FREE Users\"],\"show_category\":" +
//				"[\"Logged in User\",\"Non Logged Users\",\"Pro User\",\"FREE Users\"],\"show_category\":" +
//				"[\"Editors Picks\",\"Hindi\",\"Old Hindi\",\"English\",\"Punjabi\",\"Telugu\",\"Tamil\",\"Kannada\",\"Malayalam\",\"Bengali\",\"Ghazal\",\"Devotional\",\"Marathi\",\"Bhojpuri\",\"Gujarati\",\"Instrumental\",\"Rajasthani\",\"Oriya\",\"Urdu\",\"Haryanvi\"],\"show_language\":[\"English\",\"Hindi\",\"Tamil\",\"Telugu\",\"Punjabi\"]}}";

		try {
			if (!response.response.equalsIgnoreCase(Utils.TEXT_EMPTY)) {
				PromoUnit promoUnit = (PromoUnit) gson.fromJson(
						new JSONObject(response.response).getJSONObject(
								"response").toString(), PromoUnit.class);
				resultMap.put(RESPONSE_KEY_PROMO_UNIT, promoUnit);
			}
		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();
		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} catch (Exception e) {
			throw new InvalidResponseDataException();
		}
		return resultMap;
	}

	@Override
	public String getTimeStampCache() {
		return null;
	}
}
