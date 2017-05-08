package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.JsonParseException;
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
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class SearchPopularKeywordOperation extends HungamaOperation {

	private static final String TAG = "SearchPopularKeywordOperation";

	public static final String RESULT_KEY_LIST_KEYWORDS = "result_key_list_keywords";
	// public static final String KEYWORD = "keyword";
	public static final String CATALOG = "response";

	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final Context context;
	private final String timestamp_cache;

	public SearchPopularKeywordOperation(Context context, String serverUrl,
			String authKey, String userId, String timestamp_cache) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		this.context = context;
		this.timestamp_cache = timestamp_cache;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SEARCH_POPULAR_KEYWORDS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		return mServerUrl + URL_SEGMENT_SEARCH_POPULAR_KEYWORD + PARAMS_USER_ID
				+ "=" + mUserId + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId();
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
			DataManager mDataManager = null;
			mDataManager = DataManager.getInstance(context);
			ApplicationConfigurations mApplicationConfigurations = mDataManager
					.getApplicationConfigurations();

			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				response.response = new CacheManager(context)
						.getSearchPopularResponse();
			}

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			Logger.e("response searchkey", response.response);
			JSONParser parser = new JSONParser();

			Map<String, Object> responseMap = (Map<String, Object>) parser
					.parse(response.response);
			Logger.e("response searchkey 1", "" + responseMap);

			List<String> keywords = (List<String>) responseMap.get(CATALOG);

			String lastTimesStamp = null;
			try {
				if (responseMap.containsKey(KEY_LAST_MODIFIED)) {
					lastTimesStamp = responseMap.get(KEY_LAST_MODIFIED)
							.toString();
					mApplicationConfigurations
							.setSearchPopularTimeStamp(lastTimesStamp);
					Logger.e("lastTimesStamp 111111 search", lastTimesStamp);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

			// List<String> keywords = new ArrayList<String>();
			// try {
			// for (Map<String, Object> keywordMap : catalogMap) {
			// keywords.add((String) keywordMap.get(KEYWORD));
			// }
			// } catch (Exception e) {
			// e.printStackTrace();
			// }

			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response)) {
				Callback callback = new Callback() {

					@Override
					public void onResult(Boolean gotResponse) {

					}
				};
				new CacheManager(context).storeSearchPopularResponse(
						response.response, callback);

			}

			resultMap.put(RESULT_KEY_LIST_KEYWORDS, keywords);

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