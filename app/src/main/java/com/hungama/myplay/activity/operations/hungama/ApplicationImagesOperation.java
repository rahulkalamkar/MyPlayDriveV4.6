package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.content.Context;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class ApplicationImagesOperation extends HungamaOperation {

	public static final String RESULT_KEY_OBJECT_CODE = "result_key_object_code";
	public static final String RESULT_KEY_OBJECT_IMAGES = "result_key_object_images";
	public static final String RESULT_KEY_OBJECT_TEXTS = "result_key_object_texts";
	public static final String RESULT_KEY_OBJECT_ADREFRESH_TIME = "result_key_object_adrefresh_time";
	public static final String RESULT_KEY_OBJECT_FREE_CACHE_LIMIT = "result_key_object_free_cache_limit";
	public static final String RESULT_KEY_OBJECT_TIME_OUT = "result_key_object_timeout";
	public static final String RESULT_KEY_OBJECT_RETRY = "result_key_object_retry";
	public static final String RESULT_KEY_OBJECT_SPLASHAD_TIME_WAIT = "result_key_object_splashad_time_wait";
	public static final String RESULT_KEY_OBJECT_STRINGS = "result_key_object_strings";
	public static final String RESULT_KEY_OBJECT_APP_CONFIG = "result_key_object_app_config";
	public static final String RESULT_KEY_OBJECT_INAPP_PROMPT = "result_key_object_inapp_prompt";

	public static final String KEY_IMAGE_LDPI = "drawable-ldpi";
	public static final String KEY_IMAGE_MDPI = "drawable-mdpi";
	public static final String KEY_IMAGE_HDPI = "drawable-hdpi";
	public static final String KEY_IMAGE_XHDPI = "drawable-xhdpi";
	// public static final String KEY_IMAGE_NODPI = "drawable-nodpi";

	public static final String BACKGROUND_DISCOVERY = "background_discovery";
	public static final String BACKGROUND_NAVIGATION = "background_navigation";
	// public static final String BACKGROUND_DISCOVERY_ERA =
	// "background_discovery_era";// discovery_era_container
	// public static final String BACKGROUND_DISCOVERY_TEMPO =
	// "background_discovery_tempo";// discovery_tempo_container
	// public static final String DRAWABLE_CHECK = "check";
	public static final String DRAWABLE_BASE = "base";
	// public static final String DRAWABLE_INFO = "info";
	// public static final String DRAWABLE_GREY_DIVIDER = "grey-divider";
	public static final String DRAWABLE_PLAN_FREE = "free";
	public static final String DRAWABLE_PLAN_GOOGLE = "110";
	public static final String DRAWABLE_PLAN_MOBILE = "99";
	public static final String DRAWABLE_GO_PRO_NOW = "go_pro_now";
	// public static final String CAROUSEL_FRAME1 = "carousel_frame1";
	// public static final String CAROUSEL_FRAME2 = "carousel_frame2";
	// public static final String CAROUSEL_FRAME3 = "carousel_frame3";
	// public static final String CAROUSEL_FRAME4 = "carousel_frame4";

	private final String mServerUrl;
	private final Context mContext;
	private final String mUserId;
	private final String mHardwareId;
	private final String mTimestamp_cache;

	public ApplicationImagesOperation(String serverUrl, Context mContext,
			String mUserId, String mHardwareId, String timestamp_cache) {
		this.mServerUrl = serverUrl;
		this.mContext = mContext;
		this.mUserId = mUserId;
		this.mHardwareId = mHardwareId;
		mTimestamp_cache = timestamp_cache;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DOWNLOAD_APPLICATION_IMAGES;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String serviceUrl = mServerUrl
				+ URL_SEGMENT_DOWNLOAD_APPLICATION_IMAGES + "?"
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + mHardwareId;
		return serviceUrl;
	}

	@Override
	public String getRequestBody() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> parseResponse(Response response)
			throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		Logger.e("ApplicationImagesOperation", response.response);

		JSONParser jsonParser = new JSONParser();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {
			Map<String, Object> reponseMap = (Map<String, Object>) jsonParser
					.parse(response.response);

			long lastTimesStamp = (Long) reponseMap.get("last_modified");
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(mContext);
			mApplicationConfigurations.setAppImagesTimestamp(lastTimesStamp);

			resultMap.put(RESULT_KEY_OBJECT_CODE, response.responseCode);
			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200) {
				Map<String, Object> reponseMapObj = (Map<String, Object>) reponseMap
						.get("response");
				Map<String, Object> reponseMapImages = (Map<String, Object>) reponseMapObj
						.get("appimages");

				// Map<String, Object> reponseMapStrings = (Map<String, Object>)
				// jsonParser
				// .parse(response);
				Map<String, Object> reponseMapStrings = (Map<String, Object>) reponseMapObj
						.get("strings");
				Map<String, Object> reponseMapAppConfig = (Map<String, Object>) reponseMapObj
						.get("app_config");
				// Map<String, Object> reponseMapInAppPrompts = (Map<String,
				// Object>) reponseMapObj
				// .get("inapp_prompt");
				JSONObject reponseMapInAppPrompts = (JSONObject) reponseMapObj
						.get("inapp_prompt");

				// Map<String, Object> ldpiMap = (Map<String, Object>)
				// reponseMap.get(KEY_IMAGE_LDPI);
				// Map<String, Object> mdpiMap = (Map<String, Object>)
				// reponseMap.get(KEY_IMAGE_MDPI);
				// Map<String, Object> hdpiMap = (Map<String, Object>)
				// reponseMap.get(KEY_IMAGE_HDPI);
				// Map<String, Object> xhdpiMap = (Map<String, Object>)
				// reponseMap.get(KEY_IMAGE_XHDPI);
				// Map<String, Object> nodpiMap = (Map<String, Object>)
				// reponseMap.get(KEY_IMAGE_NODPI);
				// System.out.println("ldpi map ::::: " +
				// reponseMap.get(KEY_IMAGE_LDPI).toString());

				// JSONArray arrayTextList = (JSONArray) new JSONObject(
				// response.response)
				// .get("text-list");
				// int adrefrehTime = new JSONObject(
				// response.response).getInt("adrefresh");
				// int free_cache_limit = new JSONObject(
				// response.response).getInt("free_cache_limit");
				// int timeout = new JSONObject(
				// response.response).getInt("timeout");
				// int retry = new JSONObject(
				// response.response).getInt("retry");
				// int splashad_time_wait = new JSONObject(
				// response.response).getInt("splashad_time_wait");

				JSONArray arrayTextList = (JSONArray) reponseMapObj
						.get("text-list");
				int adrefrehTime = ((Long) reponseMapObj.get("adrefresh"))
						.intValue();
				int free_cache_limit = ((Long) reponseMapObj
						.get("free_cache_limit")).intValue();
				int timeout = ((Long) reponseMapObj.get("timeout")).intValue();
				int retry = ((Long) reponseMapObj.get("retry")).intValue();
				int splashad_time_wait = ((Long) reponseMapObj
						.get("splashad_time_wait")).intValue();

				resultMap.put(RESULT_KEY_OBJECT_IMAGES, reponseMapImages);
				resultMap
						.put(RESULT_KEY_OBJECT_TEXTS, arrayTextList.toString());
				resultMap.put(RESULT_KEY_OBJECT_ADREFRESH_TIME, adrefrehTime);
				resultMap.put(RESULT_KEY_OBJECT_FREE_CACHE_LIMIT,
						free_cache_limit);
				resultMap.put(RESULT_KEY_OBJECT_TIME_OUT, timeout);
				resultMap.put(RESULT_KEY_OBJECT_RETRY, retry);
				resultMap.put(RESULT_KEY_OBJECT_SPLASHAD_TIME_WAIT,
						splashad_time_wait);
				resultMap.put(RESULT_KEY_OBJECT_STRINGS, reponseMapStrings);
				resultMap
						.put(RESULT_KEY_OBJECT_APP_CONFIG, reponseMapAppConfig);
				resultMap.put(RESULT_KEY_OBJECT_INAPP_PROMPT,
						reponseMapInAppPrompts);
			}
			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					|| response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304) {
				mApplicationConfigurations.setApiDate(Utils.getDate(
						System.currentTimeMillis(), "dd-MM-yy"));
			}
			return resultMap;
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		} catch (Error e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		return mTimestamp_cache;
	}
}
