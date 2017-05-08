package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class SearchAutoSuggestOperation extends HungamaOperation {

	private static final String TAG = "SearchAutoSuggestOperation";

	public static final String RESULT_KEY_LIST_SUGGESTED_KEYWORDS = "result_key_list_suggested_keywords";
	public static final String QUERY_STRING = "query";

	public static final String KEYWORD = "keyword";
	public static final String LENGTH = "length";
	public static final String CATALOG = "response";

	private final String mServerUrl;
	private final String mKeyword;
	private final String mLength;
	private final String mAuthKey;
	private final String mUserId;

	public SearchAutoSuggestOperation(String serverUrl, String Keyword,
			String length, String authKey, String userId) {
//		if (Logger.isCdnSearch)
			mServerUrl = "http://cdnapi.hungama.com/webservice/hungama/";
//		else
//			mServerUrl = serverUrl;// "http://cdnapi.hungama.com/myplay2/v2/";
		mKeyword = Keyword;
		mLength = length;
		mAuthKey = authKey;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.SEARCH_AUTO_SUGGEST;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	long diffTime;

	@Override
	public String getServiceUrl(final Context context) {
		String encodedQuery = mKeyword /* .replace(" ", "%20") */;

		try {
			encodedQuery = HungamaApplication.encodeURL(encodedQuery, "utf-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		String finalurl = "";

//		if (Logger.isCdnSearch)
			finalurl = mServerUrl + URL_SEGMENT_SEARCH_AUTO_SUGGEST + KEYWORD
					+ "=" + encodedQuery;
//		else
//			finalurl = mServerUrl + URL_SEGMENT_SEARCH_AUTO_SUGGEST + KEYWORD
//					+ "=" + encodedQuery + "&" + LENGTH + "=" + mLength + "&" +
//					PARAMS_USER_ID + "=" + mUserId;
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
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		try {

			JSONParser parser = new JSONParser();

			Map<String, Object> responseMap = (Map<String, Object>) parser
					.parse(response.response);
			List<Map<String, Object>> catalogMap = (List<Map<String, Object>>) responseMap
					.get(CATALOG);

			if (Utils.isListEmpty(catalogMap)) {
				throw new InvalidResponseDataException("list is empty.");
			}

			List<String> keywords = new ArrayList<String>();

			for (Map<String, Object> keywordMap : catalogMap) {
				keywords.add((String) keywordMap.get(KEYWORD));
			}

			resultMap.put(RESULT_KEY_LIST_SUGGESTED_KEYWORDS, keywords);
			resultMap.put(QUERY_STRING, mKeyword);

		} catch (JsonSyntaxException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (JsonParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (ParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		}catch (Exception e){
            Logger.e(TAG, e.toString());
        }catch (Error e) {
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