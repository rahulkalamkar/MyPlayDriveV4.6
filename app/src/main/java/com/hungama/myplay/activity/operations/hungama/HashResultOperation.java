package com.hungama.myplay.activity.operations.hungama;

import android.content.Context;
import android.text.TextUtils;

import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HashResultOperation extends DiscoverOperation {

	private static final String TAG = "HashResultOperation";

	public static final String RESULT_KEY_HASH_TAG_RESULT = "result_key_hash_tag_result";
	// public static final String CATALOG = "response";

	private final String mServerUrl;

	private final String mUserId;
	private final String timestamp_cache;
	private String hash_tag;
	private final String images;

	public HashResultOperation(Context context, String serverUrl,
			String userId, String hash_tag, String timestamp_cache,
			String images) {
		mServerUrl = serverUrl;// "http://cdnapi.hungama.com/myplay2/v2/";
		this.timestamp_cache = timestamp_cache;
		mUserId = userId;

		this.hash_tag = hash_tag;
		// this.hash_tag = "bollywoodsongs";

		this.images = images;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DISCOVERY_HASH_TAG_RESULT;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {

		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String finalurl = "";
		String encodedQuery = hash_tag;
		try {
			encodedQuery = HungamaApplication.encodeURL(encodedQuery, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		finalurl = mServerUrl + URL_SEGMENT_HAHS_TAG_SEARCH + PARAMS_USER_ID
				+ "=" + mUserId + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId() + "&hash_tag=" + encodedQuery;
		if (!TextUtils.isEmpty(images))
			finalurl = finalurl + "&images=" + images;

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
		Logger.i("DiscoverResponce", response.response);

		String KEY_START_INDEX = this.KEY_START_INDEX;

		JSONParser jsonParser = new JSONParser();
		try {
			Map<String, Object> resultMap = new HashMap<String, Object>();

			if (response.responseCode == CommunicationManager.RESPONSE_NO_CONTENT_204) {
				resultMap.put(RESULT_KEY_HASH_TAG_RESULT,
						new ArrayList<MediaItem>());

				return resultMap;
			}
			Map<String, Object> responseMap = (Map<String, Object>) jsonParser
					.parse(response.response);

			if (responseMap.containsKey(KEY_RESPONSE)) {
				responseMap = (Map<String, Object>) responseMap
						.get(KEY_RESPONSE);
			} else {
				Logger.d(TAG,
						"Incorrect server responce! Key Catalog is absent");
			}

			Map<String, Object> attrResponseMap = null;
			if (responseMap.containsKey(KEY_ATTRIBUTES)) {
				attrResponseMap = (Map<String, Object>) responseMap
						.get(KEY_ATTRIBUTES);
			} else {
				attrResponseMap = responseMap;
			}

			// gets the maps.
			// Map<String, Object> indexerMap = (Map<String, Object>)
			// responseMap.get(KEY_ATTRIBUTES);
			List<Map<String, Object>> mediaItemMapList = null;
			if (responseMap.containsKey(KEY_CONTENT)) {
				mediaItemMapList = (List<Map<String, Object>>) responseMap
						.get(KEY_CONTENT);
			} else {
				Logger.d(TAG,
						"Incorrect server responce! Key Content is absent");
			}

			if (responseMap.containsKey("start"))
				KEY_START_INDEX = "start";

			if (!responseMap.containsKey(KEY_START_INDEX)
					|| !responseMap.containsKey(KEY_LENGTH)
					|| !responseMap.containsKey(KEY_TOTAL)) {
				throw new InvalidResponseDataException();
			}

			// gets the indexer.
			int startIndex = 0;
			if (attrResponseMap.containsKey(KEY_START_INDEX)) {
				startIndex = ((Long) attrResponseMap.get(KEY_START_INDEX))
						.intValue();
			} else {
				Logger.d(TAG,
						"Incorrect server responce! Key startIndex is absent");
			}

			int length = 0;
			if (attrResponseMap.containsKey(KEY_LENGTH)) {
				length = ((Long) attrResponseMap.get(KEY_LENGTH)).intValue();
			} else {
				Logger.d(TAG, "Incorrect server responce! Key length is absent");
			}

			int total = 0;
			if (attrResponseMap.containsKey(KEY_TOTAL)) {
				total = ((Long) attrResponseMap.get(KEY_TOTAL)).intValue();
			} else if (attrResponseMap.containsKey(KEY_MAX)) {
				total = ((Long) attrResponseMap.get(KEY_MAX)).intValue();
			} else {
				Logger.d(TAG, "Incorrect server responce! Key max is absent");
			}

			resultMap
					.put(DiscoverSearchResultsOperation.RESULT_KEY_DISCOVER_SEARCH_RESULT_INDEXER,
							new DiscoverSearchResultIndexer(startIndex, length,
									total));

			// gets the list of media items.
			List<MediaItem> mediaItems = new ArrayList<MediaItem>();
			int contentId;
			String albumName;
			String title;
			String imageUrl;
			String bigImageUrl;
			String type;
			int trackCount;
			long albumId;
			Map<String, List<String>> images;

			MediaItem mediaItem;

			for (Map<String, Object> mediaItemMap : mediaItemMapList) {
				contentId = ((Long) mediaItemMap.get(MediaItem.KEY_CONTENT_ID))
						.intValue();
				albumName = (String) mediaItemMap.get(MediaItem.KEY_ALBUM_NAME);
				title = (String) mediaItemMap.get(MediaItem.KEY_TITLE);
				imageUrl = (String) mediaItemMap.get(MediaItem.KEY_IMAGE);
				bigImageUrl = (String) mediaItemMap
						.get(MediaItem.KEY_BIG_IMAGE);
				type = (String) mediaItemMap.get(MediaItem.KEY_TYPE);
				images = new HashMap<String, List<String>>();
				albumId = (Long) mediaItemMap.get(MediaItem.KEY_ALBUM_ID);
				try {

					images = (Map<String, List<String>>) mediaItemMap
							.get(MediaItem.KEY_IMAGES);
				} catch (Exception e) {
				}

				trackCount = 0;

				if (type.equalsIgnoreCase(MediaType.PLAYLIST.toString())) {
					if (mediaItemMap
							.containsKey(MediaItem.KEY_MUSIC_TRACKS_COUNT)) {
						trackCount = ((Long) mediaItemMap
								.get(MediaItem.KEY_MUSIC_TRACKS_COUNT))
								.intValue();
					}
				}

				mediaItem = new MediaItem(contentId, title, albumName, null,
						imageUrl, bigImageUrl, type, trackCount, 0, images,
						albumId);
				mediaItem.setMediaContentType(MediaContentType.MUSIC);

				mediaItems.add(mediaItem);
			}

			resultMap.put(RESULT_KEY_HASH_TAG_RESULT, mediaItems);

			return resultMap;

		} catch (ParseException e) {
			e.printStackTrace();
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		return timestamp_cache;
	}
}