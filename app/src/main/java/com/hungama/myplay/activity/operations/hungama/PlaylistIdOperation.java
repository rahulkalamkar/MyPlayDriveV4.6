package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.PlaylistIdResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class PlaylistIdOperation extends HungamaOperation {

	private static final String TAG = "MediaContentOperationPaging";

	public static final String RESULT_KEY_OBJECT_PLAYLIST_ID = "result_key_object_playlist_id";
	// public static final String RESULT_KEY_OBJECT_MEDIA_CONTENT_TYPE =
	// "result_key_object_media_content_type";
	// public static final String RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE =
	// "result_key_object_media_category_type";
	// public static final String RESULT_KEY_OBJECT_MEDIA_CATEGORY =
	// "result_key_object_media_category";
	// public static final String RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE =
	// "response_key_object_media_items_response";

	private final Context mContext;

	private final String mServerUrl;
	private final String playlistKey;

	// private final MediaContentType mContentType;
	// private final MediaCategoryType mItemCategoryType;
	// private final Category mCategory;
	// private final String mUserId;
	// private final String mStart;
	// private final String mLength;

	public PlaylistIdOperation(Context context, String serverUrl,
			String playlistKey) {

		mContext = context;
		mServerUrl = serverUrl;
		this.playlistKey = playlistKey;
		// mContentType = contentType;
		// mItemCategoryType = itemCategoryType;
		// mCategory = category;
		// mUserId = userId;
		// mStart = start;
		// mLength = length;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.GET_PLAYLIST_ID;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {

		String stringContentType = MediaContentType.MUSIC.toString()
				.toLowerCase();
		// String stringItemCategoryType =
		// mItemCategoryType.toString().toLowerCase();

		String serviceUrl = mServerUrl + URL_SEGMENT_CONTENT
				+ stringContentType + "/" + URL_SEGMENT_PLAYLIST_ID + "?"
				+ PARAMS_PLAYLIST_KEY + "=" + playlistKey;
		Logger.i(TAG + "Target", "URL = " + serviceUrl);

		// if (mCategory != null && !TextUtils.isEmpty(mCategory.getName())) {
		// serviceUrl = serviceUrl + "&category=" + mCategory.getName();
		// }

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
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		try {
			PlaylistIdResponse playlistIdResponse = (PlaylistIdResponse) gson
					.fromJson(new JSONObject(response.response)
							.getString("response"), PlaylistIdResponse.class);

			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCancelledException();
			}

			Map<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_OBJECT_PLAYLIST_ID, playlistIdResponse);

			return resultMap;

		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} catch (Exception exception) {
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}
}