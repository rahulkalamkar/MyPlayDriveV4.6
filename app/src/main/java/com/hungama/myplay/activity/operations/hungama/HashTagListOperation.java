package com.hungama.myplay.activity.operations.hungama;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MessageFromResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashTagListOperation extends HungamaOperation {

	private static final String TAG = "HashTagListOperation";

	public static final String RESULT_KEY_MESSAGE = "result_key_message";
	public static final String RESULT_KEY_HASH_TAG_LIST = "result_key_hash_tag_list";
	public static final String RESULT_show_hash_text = "show_hash_text";
	public static final String CATALOG = "response";
	public static final String show_hash_text = "show_hash_text";

	private final String mServerUrl;

	private final String mUserId;
	private final Context context;
	private final String timestamp_cache;

	public HashTagListOperation(Context context, String serverUrl,
			String userId, String timestamp_cache) {
		this.context = context;
		mServerUrl = serverUrl;// "http://cdnapi.hungama.com/myplay2/v2/";
		this.timestamp_cache = timestamp_cache;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DISCOVERY_HASH_TAG;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	// long diffTime;

	@Override
	public String getServiceUrl(final Context context) {

		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String finalurl = "";

		finalurl = mServerUrl + URL_SEGMENT_HAHS_TAG_LIST + PARAMS_USER_ID
				+ "=" + mUserId + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId();

		// diffTime = System.currentTimeMillis();
        Logger.writetofile(TAG, new Date().toString() + " REQUEST : "
				+ finalurl);
		return finalurl;
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

		Map<String, Object> resultMap = new HashMap<String, Object>();

		try {

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			Logger.e("response searchkey", response.response);
			JSONParser parser = new JSONParser();

			Map<String, Object> responseMap = (Map<String, Object>) parser
					.parse(response.response);
			Logger.e("response searchkey 1", "" + responseMap);

			if (responseMap.containsKey("message")) {
				Map<String, Object> responseMessage = (Map<String, Object>) responseMap
						.get("message");
				MessageFromResponse message = new MessageFromResponse(
						(Long) responseMessage.get("show_message"),
						(String) responseMessage.get("message_text"));
				resultMap.put(RESULT_KEY_MESSAGE, message);
			}

			long hashToBeDisplay = (Long) responseMap.get(show_hash_text);
			Logger.e("response hashToBeDisplay 1", "" + hashToBeDisplay);
			List<String> keywords = (List<String>) responseMap.get(CATALOG);

			resultMap.put(RESULT_KEY_HASH_TAG_LIST, keywords);
			resultMap.put(RESULT_show_hash_text, hashToBeDisplay);

			return resultMap;

		} catch (JsonSyntaxException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (JsonParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (ParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (Exception e) {
			Logger.printStackTrace(e);
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		return timestamp_cache;
	}
}