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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MessageFromResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RadioTopArtistsOperation extends WebRadioOperation {

	private static final String TAG = "RadioTopArtistsOperation";

	public static final String KEY_ARTIST_ID = "radio_id";
	public static final String KEY_ARTIST_NAME = "title";
	private static final String KEY_IMAGE = "images";

	private final String mServerUrl;
	private final String mAuthKey;
	private final Context context;
	private final String timestamp_cache;
	private final String mUserId;
	private final String mImages;

	public RadioTopArtistsOperation(Context context, String serverUrl,
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
		return OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String imageParams = "";
		if (!TextUtils.isEmpty(mImages))
			imageParams = "&images=" + mImages;
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serverUrl = mServerUrl + URL_SEGMENT_RADIO_TOP_ARTISTS
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
						.getCelebRadioResponse();
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
				mApplicationConfigurations.setOnDemandTimeStamp(lastTimesStamp);
				Logger.e("lastTimesStamp 111111 radio ondemand", lastTimesStamp);
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
				String name;
				String imageUrl = null;
				Map<String, List<String>> images;
				// long albumId;

				MediaItem mediaItem;

				if (contentMap != null) {
					for (Map<String, Object> stationMap : contentMap) {

						try {
							id = ((Long) stationMap.get(KEY_ARTIST_ID)).longValue();

							name = (String) stationMap.get(KEY_ARTIST_NAME);
							// albumId = (Long)
							// stationMap.get(MediaItem.KEY_ALBUM_ID);

							// try {
							// try {
							// Map<String, Object> temp = (Map<String, Object>)
							// stationMap.get(KEY_IMAGE);
							// List<Map<String, Object>> temp1 = (List<Map<String,
							// Object>>) temp
							// .get("image_"+mImages.substring(0,mImages.indexOf(",")));
							// List<String> keywords = (List<String>) temp
							// .get("image_"+mImages.substring(0,mImages.indexOf(",")));
							// imageUrl = keywords.get(0).toString();
							// } catch (Exception e) {
							// }
							// // imageUrl = (String) stationMap.get(KEY_IMAGE);
							// } catch (Exception e) {
							// }
							images = (Map<String, List<String>>) stationMap
									.get(MediaItem.KEY_IMAGES);

							mediaItem = new MediaItem(id, name, null, null,
									imageUrl, imageUrl, MediaType.ALBUM.toString()
									.toLowerCase(), 0, 0, images, 0);
							mediaItem.setMediaContentType(MediaContentType.RADIO);

							mediaItems.add(mediaItem);
						}catch (Exception e){}


					}
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
						new CacheManager(context).storeCelebRadioResponse(
								response.response, callback);
				}

				return resultMap;

			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no content available");
			}

		}catch (Exception e){
            e.printStackTrace();
            throw new InvalidResponseDataException("Parsing error.");
        }
	}

	@Override
	public String getTimeStampCache() {
		return timestamp_cache;
	}
}
