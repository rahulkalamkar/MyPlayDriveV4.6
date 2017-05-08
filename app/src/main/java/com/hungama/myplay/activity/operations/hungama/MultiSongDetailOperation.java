package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemsResponseCatalog;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Retrieves {@link MediaItem}s similar to the given {@link Track}
 */
public class MultiSongDetailOperation extends HungamaOperation {

	private static final String TAG = "MultiSongDetailOperation";

	public static final String RESULT_KEY_OBJECT_MEDIA_ITEMS = "result_key_object_media_items";
	public static final String RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE = "response_key_object_media_items_response";
	public static final String RESPONSE_SERVER = "response_server";

	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mcontent_id;
	private final String msize;
	private final String mImages;

	public MultiSongDetailOperation(String serverUrl, String authKey,
			String userId, String content_id, String size, String images) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		mcontent_id = content_id;
		msize = size;
		mImages = images;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.MULTI_SONG_DETAIL_SONGCATCHER;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);

		String imageParams = "";
		if (!TextUtils.isEmpty(mImages))
			imageParams = "&images=" + mImages;

		String serviceUrl = mServerUrl + URL_SEGMENT_MULTI_SONG_DETAIL_GET
				+ PARAMS_USER_ID + EQUALS + mUserId
				+ HungamaOperation.AMPERSAND + PARAMS_CONTENT_ID + EQUALS
				+ mcontent_id + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId() + imageParams;

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

		if (TextUtils.isEmpty(response.response)) {
			throw new InvalidResponseDataException("Response is Empty!");
		}

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		List<MediaItem> items = null;
		try {
			MediaItemsResponseCatalog mediaItemsResponseCatalog = (MediaItemsResponseCatalog) gson
					.fromJson(response.response,
							MediaItemsResponseCatalog.class);

			if (mediaItemsResponseCatalog != null) {
				items = mediaItemsResponseCatalog.getCatalog().getContent();
			}

			if (items == null) {
				items = new ArrayList<MediaItem>();
			}

			// TODO: temporally solving the differentiating issue between Music
			// and
			// Videos, solve this when inserting also campaigns.
			for (MediaItem mediaItem : items) {
				mediaItem.setMediaContentType(MediaContentType.MUSIC);
			}

			HashMap<String, Object> resultMap = new HashMap<String, Object>();
			resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEMS, items);
			resultMap.put(RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE,
					mediaItemsResponseCatalog.getCatalog());
			resultMap.put(RESPONSE_SERVER, response.response);
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
