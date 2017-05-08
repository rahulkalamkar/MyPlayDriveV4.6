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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemsResponseCatalog;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;

/**
 * Retrieves {@link MediaItem}s similar to the given {@link Track}
 */
public class MultiSongHistoryOperation extends HungamaOperation {

	private static final String TAG = "MultiSongHistoryOperation";

	public static final String RESULT_KEY_OBJECT_MEDIA_ITEMS = "result_key_object_media_items";
	public static final String RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE = "response_key_object_media_items_response";

	private final String mServerUrl;
	private final String mUserId;
	private final Context mContext;
	private final String timestamp_cache;

	public MultiSongHistoryOperation(String serverUrl, String userId,
			Context context, String timestamp_cache) {
		mServerUrl = serverUrl;
		mUserId = userId;
		mContext = context;
		this.timestamp_cache = timestamp_cache;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.MULTI_SONG_HISTORY_SONGCATCHER;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serviceUrl = mServerUrl + URL_SEGMENT_MULTI_SONG_HISTORY_GET
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId() + HungamaOperation.AMPERSAND
				+ PARAMS_USER_ID + "=" + mUserId;
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

		if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
				|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
				|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
				|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
			response.response = new CacheManager(mContext)
					.getNQHistoryResponse();
		}

		if (TextUtils.isEmpty(response.response)) {
			response.response = "";
		}

		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
				.create();
		List<MediaItem> items = null;
		try {
			MediaItemsResponseCatalog mediaItemsResponseCatalog = (MediaItemsResponseCatalog) gson
					.fromJson(response.response,
							MediaItemsResponseCatalog.class);

			DataManager mDataManager;
			mDataManager = DataManager.getInstance(mContext);
			ApplicationConfigurations mApplicationConfigurations = mDataManager
					.getApplicationConfigurations();
			mApplicationConfigurations.setNQHistoryTimeStamp(""
					+ mediaItemsResponseCatalog.getTimeStamp());

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

			Callback callback = new Callback() {
				@Override
				public void onResult(Boolean gotResponse) {

				}
			};

			if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
					&& !TextUtils.isEmpty(response.response))
				new CacheManager(mContext).storeNQHistoryResponse(
						response.response, callback);

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
		return timestamp_cache;
	}

}
