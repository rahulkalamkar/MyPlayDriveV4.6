package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.Context;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.operations.OperationDefinition;

public class LiveRadioDetailsOperation extends WebRadioOperation {

	private static final String TAG = "RadioTopArtistSongsOperation";

	private static final String PARAMS_ARTIST_ID = "radio_id";

	// public static final String RESULT_KEY_OBJECT_TRACKS =
	// "result_key_object_tracks";
	public static final String RESULT_KEY_OBJECT_MEDIA_ITEM = "result_key_object_media_item";
	// public static final String RESULT_KEY_OBJECT_USER_FAVORITE =
	// "result_key_object_user_favorite";

	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mImages;
	private final String mRadioId;

	public LiveRadioDetailsOperation(String serverUrl, String authKey,
			String mUserId, String radioId, String mImages) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		this.mUserId = mUserId;
		mRadioId = radioId;
		this.mImages = mImages;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATION_DETAIL;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		// mServerUrl.replace("myplay2", "myplay2livefinal");
		// String serverUrl = mServerUrl + URL_SEGMENT_RADIO_TOP_ARTIST_SONGS +
		// PARAMS_ARTIST_ID + EQUALS + Long.toString(mArtistItem.getId()) +
		// AMPERSAND +
		// PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND +
		// PARAMS_AUTH_KEY + EQUALS + mAuthKey;
		String serverUrl = mServerUrl + URL_SEGMENT_LIVE_RADIO_DETAIL
				+ PARAMS_ARTIST_ID + EQUALS + mRadioId + AMPERSAND
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + config.getHardwareId();
		if (!TextUtils.isEmpty(mImages)) {
			serverUrl = serverUrl + AMPERSAND + "images" + EQUALS + mImages;
		}

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

		JSONParser parser = new JSONParser();

		try {
			// if (response.responseCode ==
			// CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304||
			// response.responseCode ==
			// CommunicationManager.RESPONSE_SERVER_ERROR_500
			// || response.responseCode ==
			// CommunicationManager.RESPONSE_BAD_REQUEST_400||
			// response.responseCode ==
			// CommunicationManager.RESPONSE_FORBIDDEN_403) {
			// response.response = new CacheManager(context)
			// .getLiveRadioResponse();
			// }

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			Map<String, Object> catalogMap = (Map<String, Object>) parser
					.parse(response.response);

			// String lastTimesStamp = null;
			// try {
			// if (catalogMap.containsKey(KEY_LAST_MODIFIED)) {
			// lastTimesStamp = catalogMap.get(KEY_LAST_MODIFIED).toString();
			// }
			// DataManager mDataManager;
			// mDataManager = DataManager.getInstance(context);
			// ApplicationConfigurations mApplicationConfigurations =
			// mDataManager
			// .getApplicationConfigurations();
			// mApplicationConfigurations
			// .setLiveRadioTimeStamp(lastTimesStamp);
			// Logger.e("lastTimesStamp 111111 radio live", lastTimesStamp);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }

			if (catalogMap.containsKey(KEY_RESPONSE)) {
				catalogMap = (Map<String, Object>) catalogMap.get(KEY_RESPONSE);
			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no catalog available");
			}

			if (catalogMap.containsKey(KEY_CONTENT)) {

				Map<String, Object> contentMap = (Map<String, Object>) catalogMap
						.get(KEY_CONTENT);

				// List<MediaItem> mediaItems = new ArrayList<MediaItem>();

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

				// for (Map<String, Object> stationMap : contentMap) {
				id = (Long) contentMap
						.get(RadioLiveStationsOperation.KEY_RADIO_ID);
				title = (String) contentMap
						.get(RadioLiveStationsOperation.KEY_TITLE);
				description = (String) contentMap
						.get(RadioLiveStationsOperation.KEY_DESCRIPTION);
				try {
					streamingUrl = (String) contentMap
							.get(RadioLiveStationsOperation.KEY_STREAMING_URL_64);
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					streamingUrl_128 = (String) contentMap
							.get(RadioLiveStationsOperation.KEY_STREAMING_URL_128);
				} catch (Exception e) {
					e.printStackTrace();
				}

				try {
					streamingUrl_320 = (String) contentMap
							.get(RadioLiveStationsOperation.KEY_STREAMING_URL_320);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// try {
				// Map<String, Object> temp = (Map<String, Object>)
				// stationMap.get(KEY_STREAMING_THUMB);
				// List<Map<String, Object>> temp1 = (List<Map<String, Object>>)
				// temp
				// .get("image_"+mImages.substring(0,mImages.indexOf(",")));
				// List<String> keywords = (List<String>) temp
				// .get("image_"+mImages.substring(0,mImages.indexOf(",")));
				// imageUrl = keywords.get(0).toString();
				// } catch (Exception e) {
				// }

				// try {
				// try {
				// Map<String, Object> temp = (Map<String, Object>)
				// stationMap.get(KEY_STREAMING_THUMB);
				// List<Map<String, Object>> temp1 = (List<Map<String, Object>>)
				// temp
				// .get("image_"+mImages.substring(0,mImages.indexOf(",")));
				// List<String> keywords = (List<String>) temp
				// .get("image_"+mImages.substring(mImages.indexOf(",")+1,mImages.length()));
				// bigImageUrl = keywords.get(0).toString();
				// } catch (Exception e) {
				// }
				//
				// // bigImageUrl = (String) stationMap
				// // .get(KEY_STREAMING_BIG_URL);
				// } catch (Exception e) {
				// }
				try {
					images = (Map<String, List<String>>) contentMap
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

				// mediaItems.add(mediaItem);

				// id++;
				// }

				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEM, mediaItem);

				// if (!isFromCache) {
				// Callback callback = new Callback() {
				//
				// @Override
				// public void onResult(Boolean gotResponse) {
				//
				// }
				// };
				// if (response.responseCode ==
				// CommunicationManager.RESPONSE_SUCCESS_200
				// && !TextUtils.isEmpty(response.response))
				// new CacheManager(context).storeLiveRadioResponse(
				// response.response, callback);
				// }

				return resultMap;

			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no content available");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new InvalidResponseDataException("Parsing error.");
		}
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
