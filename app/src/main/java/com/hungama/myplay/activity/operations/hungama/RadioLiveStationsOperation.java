package com.hungama.myplay.activity.operations.hungama;

import android.content.Context;
import android.text.TextUtils;

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
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MessageFromResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Retrieves the available live stations from Hungama as list of
 * {@link MediaItem} implementation. The driven retrieved {@link MediaItem} is
 * of the type {@link LiveStation}.
 */
public class RadioLiveStationsOperation extends WebRadioOperation {

	private static final String TAG = "RadioLiveStationsOperation";

	public static final String KEY_RADIO_ID = "radio_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_DESCRIPTION = "description";
	public static final String KEY_STREAMING_URL_64 = "streaming_url_64";
	public static final String KEY_STREAMING_URL_128 = "streaming_url_128";
	public static final String KEY_STREAMING_URL_320 = "streaming_url_320";

	// public static final String KEY_STREAMING_THUMB = "images";
	// public static final String KEY_STREAMING_BIG_URL = "big_image";

	private final String mServerUrl;
	private final String mAuthKey;
	private final Context context;
	private final String timestamp_cache;
	private final String mUserId;
	private final String mImages;

	public RadioLiveStationsOperation(Context context, String serverUrl,
			String authKey, String userId, String timestamp_cache, String images) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		this.context = context;
		this.timestamp_cache = timestamp_cache;
		mUserId = userId;
		mImages = images;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS;
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

		String serverUrl = mServerUrl + URL_SEGMENT_RADIO_LIVE_STATIONS
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId() + "&" + PARAMS_USER_ID + "=" + mUserId
				+ imageParams + "&device=android";
		return serverUrl;
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
		return parseResponseFromCache(response, false);
	}

	public Map<String, Object> parseResponseFromCache(Response response,
			boolean isFromCache) throws InvalidResponseDataException,
			InvalidRequestParametersException, InvalidRequestTokenException,
			OperationCancelledException {

		JSONParser parser = new JSONParser();

		try {
			if (response.responseCode == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304
					|| response.responseCode == CommunicationManager.RESPONSE_SERVER_ERROR_500
					|| response.responseCode == CommunicationManager.RESPONSE_BAD_REQUEST_400
					|| response.responseCode == CommunicationManager.RESPONSE_FORBIDDEN_403) {
				response.response = new CacheManager(context)
						.getLiveRadioResponse();
			}

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			Map<String, Object> catalogMap = (Map<String, Object>) parser
					.parse(response.response);

			String lastTimesStamp = null;
			try {
				if (catalogMap.containsKey(KEY_LAST_MODIFIED)) {
					lastTimesStamp = catalogMap.get(KEY_LAST_MODIFIED)
							.toString();
				}
				DataManager mDataManager;
				mDataManager = DataManager.getInstance(context);
				ApplicationConfigurations mApplicationConfigurations = mDataManager
						.getApplicationConfigurations();
				mApplicationConfigurations
						.setLiveRadioTimeStamp(lastTimesStamp);
				Logger.e("lastTimesStamp 111111 radio live", lastTimesStamp);
			} catch (Exception e) {
				e.printStackTrace();
			}

			MessageFromResponse message = null;
			if (catalogMap.containsKey("message")) {
				Map<String, Object> responseMessage = (Map<String, Object>) catalogMap
						.get("message");
				message = new MessageFromResponse(
						(Long) responseMessage.get("show_message"),
						(String) responseMessage.get("message_text"));
			}

			if (catalogMap.containsKey(KEY_RESPONSE)) {
				catalogMap = (Map<String, Object>) catalogMap.get(KEY_RESPONSE);
			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no catalog available");
			}

			if (catalogMap.containsKey(KEY_CONTENT)) {

				List<Map<String, Object>> contentMap = (List<Map<String, Object>>) catalogMap
						.get(KEY_CONTENT);

				List<MediaItem> mediaItems = new ArrayList<MediaItem>();

				long id = 0;

				String title;
				String description;
				String streamingUrl = null;
				String streamingUrl_128 = null;
				String streamingUrl_320 = null;
				String imageUrl = null;
				String bigImageUrl = null;
				Map<String, List<String>> images = null;

				MediaItem mediaItem;

				for (Map<String, Object> stationMap : contentMap) {
					id = (Long) stationMap
							.get(RadioLiveStationsOperation.KEY_RADIO_ID);
					title = (String) stationMap.get(KEY_TITLE);
					description = (String) stationMap.get(KEY_DESCRIPTION);
					try {
						streamingUrl = (String) stationMap
								.get(KEY_STREAMING_URL_64);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						streamingUrl_128 = (String) stationMap
								.get(KEY_STREAMING_URL_128);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						streamingUrl_320 = (String) stationMap
								.get(KEY_STREAMING_URL_320);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						images = (Map<String, List<String>>) stationMap
								.get(MediaItem.KEY_IMAGES);
					} catch (Exception e) {
					}
					LiveStation temp = new LiveStation(id, title, description,
							streamingUrl, imageUrl, bigImageUrl);
					temp.setStreamingUrl_128(streamingUrl_128);
					temp.setStreamingUrl_320(streamingUrl_320);
					mediaItem = temp;
					mediaItem.setMediaContentType(MediaContentType.RADIO);
					mediaItem.setImagesUrlArray(images);

					mediaItems.add(mediaItem);

					id++;
				}

				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEM, mediaItems);
				if (message != null)
					resultMap.put(HashTagListOperation.RESULT_KEY_MESSAGE,
							message);

				if (!isFromCache) {
					Callback callback = new Callback() {

						@Override
						public void onResult(Boolean gotResponse) {

						}
					};
					if (response.responseCode == CommunicationManager.RESPONSE_SUCCESS_200
							&& !TextUtils.isEmpty(response.response))
						new CacheManager(context).storeLiveRadioResponse(
								response.response, callback);
				}

				return resultMap;

			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no content available");
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
			throw new InvalidResponseDataException("Parsing error.");
		}
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return timestamp_cache;
	}

}
