package com.hungama.myplay.activity.operations.hungama;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.SearchResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class SearchKeyboardOperation extends HungamaOperation {

	private static final String TAG = "SearchKeyboardOperation";

	public static final String RESPONSE_KEY_SEARCH = "response_key_search";
	public static final String RESPONSE_KEY_TOAST = "response_key_toast";
	public static final String RESPONSE_KEY_QUERY = "response_key_query";
	public static final String RESPONSE_KEY_TYPE = "response_key_type";

	public static final String KEYWORD = "keyword";
	public static final String TYPE = "type";
	public static final String STARTINDEX = "start";
	public static final String LENGTH = "length";
	// public static final String CATALOG = "response";

	private final String mServerUrl;
	private final String mKeyword;
	private final String mType;
	private final String mStartIndex;
	private final String mLength;
	private final String mAuthKey;
	private final String mUserId;
	private final String mImages;

	public SearchKeyboardOperation(String serverUrl, String keyword,
			String type, String startIndex, String length, String authKey,
			String userId, String images) {
		// if (Logger.isCdnSearch)
		// mServerUrl = "http://cdnapi.hungama.com/myplay2/v2/";
		// else
		mServerUrl = serverUrl;// "http://cdnapi.hungama.com/myplay2/v2/";
		mKeyword = keyword;
		mType = type;
		mStartIndex = startIndex;
		mLength = length;
		mAuthKey = authKey;
		mUserId = userId;
		mImages = images;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SEARCH;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	long diffTime;

	@Override
	public String getServiceUrl(final Context context) {
		String encodedQuery = mKeyword;

		// Querying like a bous!
		try {
			encodedQuery = HungamaApplication.encodeURL(encodedQuery, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		String finalurl = "";
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		// if (Logger.isCdnSearch)
		// finalurl = mServerUrl + URL_SEGMENT_SEARCH + KEYWORD + "="
		// + encodedQuery + "&" + TYPE + "=" + mType + "&"
		// + STARTINDEX + "=" + mStartIndex + "&" + LENGTH + "="
		// + mLength + HungamaOperation.AMPERSAND
		// + PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
		// + config.getHardwareId();
		// else
		{
			String imageParams = "";
			if (!TextUtils.isEmpty(mImages))
				imageParams = "&images=" + mImages;

			String type = mType.toLowerCase();
			if (TextUtils.isEmpty(type))
				type = "all";
			else if (type.equals("videos"))
				type = "video";
			finalurl = mServerUrl + URL_SEGMENT_SEARCH + type + "?" + KEYWORD
					+ "=" + encodedQuery + "&" + STARTINDEX + "=" + mStartIndex
					+ "&" + LENGTH + "=" + mLength + "&" + PARAMS_USER_ID + "="
					+ mUserId + HungamaOperation.AMPERSAND
					+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
					+ config.getHardwareId() + imageParams;
		}
		diffTime = System.currentTimeMillis();
		Logger.writetofileSearchlog(new Date().toString() + " REQUEST : "
				+ finalurl, true);
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
		Logger.writetofileSearchlog(new Date().toString() + "  Dureation : "
				+ (System.currentTimeMillis() - diffTime) + " RESPONSE : "
				+ response, true);

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		// {"response":{"code":2,"message":"Search is supported for English keywords only. Please enter search text in English and try","display":1}}
		JSONParser jsonParser = new JSONParser();
		if (response.response.contains("\"response\"")
				&& response.response.contains("\"Search is supported for\"")) {
			try {
				String responseNew = response.response.replace(
						"{\"response\":", "");
				responseNew = responseNew
						.substring(0, responseNew.length() - 1);
				Map<String, Object> responseMap = (Map<String, Object>) jsonParser
						.parse(responseNew);

				if (responseMap.get("code").toString().equals("2")
						&& responseMap.get("display").toString().equals("1")) {
					resultMap.put(RESPONSE_KEY_TOAST, responseMap
							.get("message").toString());

					String query = mKeyword;

					// Querying like a bous!
					try {
						query = URLDecoder.decode(query, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}

					resultMap.put(RESPONSE_KEY_QUERY, query);

				}
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
			return resultMap;
		}

		try {

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			Map<String, Object> responseMap = (Map<String, Object>) jsonParser
					.parse(response.response);
			Object contentObj1 = (Object) responseMap.get(KEY_RESPONSE);
			String contentStr1 = String.valueOf(contentObj1);
			if (responseMap.containsKey(KEY_RESPONSE)) {
				responseMap = (Map<String, Object>) responseMap
						.get(KEY_RESPONSE);
			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no catalog available");
			}

			if (responseMap.containsKey(KEY_CONTENT)) {
				Object contentObj = (Object) responseMap.get(KEY_CONTENT);
				String contentStr = String.valueOf(contentObj);
				if (contentStr.equals("0")) {
					response.response = response.response.replace(
							",\"content\":0", ",\"content\":[]");
				}

				SearchResponse searchResponse = (SearchResponse) gson.fromJson(
						contentStr1, SearchResponse.class);
				List<MediaItem> mediaItems = searchResponse.getContent();

				for (MediaItem mediaItem : mediaItems) {
					if (mediaItem.getMediaType() == MediaType.VIDEO) {
						mediaItem.setMediaContentType(MediaContentType.VIDEO);

					} else if (mediaItem.getMediaType() == MediaType.ARTIST) {
						mediaItem.setMediaContentType(MediaContentType.RADIO);
					} else {
						mediaItem.setMediaContentType(MediaContentType.MUSIC);
					}
				}

				String query = mKeyword;

				// Querying like a bous!
				try {
					query = URLDecoder.decode(query, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				resultMap.put(RESPONSE_KEY_SEARCH, searchResponse);
				resultMap.put(RESPONSE_KEY_QUERY, query);
				resultMap.put(RESPONSE_KEY_TYPE, mType);
			}
			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			return resultMap;

		} catch (JsonSyntaxException exception) {
			exception.printStackTrace();
			Logger.e(TAG, exception.toString());

		} catch (JsonParseException exception) {
			exception.printStackTrace();
			Logger.e(TAG, exception.toString());

		} catch (ParseException exception) {
			exception.printStackTrace();
			Logger.e(TAG, exception.toString());

		} catch (Exception e) {
			e.printStackTrace();
			Logger.printStackTrace(e);
			throw new InvalidResponseDataException();
		}

		String query = mKeyword;

		// Querying like a bous!
		try {
			query = URLDecoder.decode(query, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		resultMap.put(RESPONSE_KEY_SEARCH, new SearchResponse(1, 30, 0, 0, 0,
				0, 0, 0, new ArrayList<MediaItem>()));
		resultMap.put(RESPONSE_KEY_QUERY, query);
		resultMap.put(RESPONSE_KEY_TYPE, mType);
		return resultMap;
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}
}