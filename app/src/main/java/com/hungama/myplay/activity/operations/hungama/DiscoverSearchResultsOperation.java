package com.hungama.myplay.activity.operations.hungama;

import java.util.ArrayList;
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
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

/**
 * Retrieves list of media items from the Discover properties query.
 */
public class DiscoverSearchResultsOperation extends DiscoverOperation {

	private static final String TAG = "DiscoverSearchResultsOperation";

	/**
	 * Key for getting the index properties of the response paging.
	 */
	public static final String RESULT_KEY_DISCOVER_SEARCH_RESULT_INDEXER = "result_key_discover_search_result_indexer";

	/**
	 * Key for getting list of media items in type of Tracks.
	 */
	public static final String RESULT_KEY_MEDIA_ITEMS = "result_key_media_items";

	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String images;

	private final Discover mDiscover;
	private final DiscoverSearchResultIndexer mDiscoverSearchResultIndexer;

	public DiscoverSearchResultsOperation(String serverUrl, String authKey,
			String userId, Discover discover,
			DiscoverSearchResultIndexer discoverSearchResultIndexer,
			String images) {

		this.mServerUrl = serverUrl;
		this.mAuthKey = authKey;
		this.mUserId = userId;
		this.images = images;

		this.mDiscover = discover;
		this.mDiscoverSearchResultIndexer = discoverSearchResultIndexer;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.DISCOVER_SEARCH_RESULT;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		StringBuilder serverURL = new StringBuilder();
		// adds the base server url.
		serverURL.append(mServerUrl);
		// adds the service path.
		serverURL.append(URL_SEGMENT_DISCOVER_SEARCH);
		// authentication properties.

		serverURL.append(PARAMS_USER_ID).append(EQUALS).append(mUserId)
				.append(AMPERSAND);
		serverURL.append(PARAMS_DOWNLOAD_HARDWARE_ID).append(EQUALS)
				.append(config.getHardwareId());
		if (!TextUtils.isEmpty(images))
			serverURL.append(AMPERSAND).append("images").append(EQUALS)
					.append(images);

		// builds the discover params
		serverURL.append(buildURLParametersFromDiscoverObject(mDiscover));

		// builds the indexer.
		serverURL
				.append(buildURLParametersFromDiscoverSearchResultIndexer(mDiscoverSearchResultIndexer));

		return serverURL.toString();
	}

	@Override
	public String getRequestBody() {
		// GET request, nothing to do.
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
		List<MediaItem> mediaItems = new ArrayList<MediaItem>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		try {

			Map<String, Object> responseMap = (Map<String, Object>) jsonParser
					.parse(response.response);

			// checks if the given response is not an error.
			// if (responseMap.containsKey(KEY_RESPONSE)) {
			// // gets the error message.
			// Map<String, Object> errorMap = (Map<String, Object>)
			// responseMap.get(KEY_RESPONSE);
			// int code = ((Long) errorMap.get(KEY_CODE)).intValue();
			// String message = (String) errorMap.get(KEY_MESSAGE);
			// throw new InvalidRequestParametersException(code, message);
			// }

			// gets the "catalog".

			// Gson gson = new
			// GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			// List<MediaItem> items = null;
			// try {
			// MediaItemsResponseCatalog mediaItemsResponseCatalog =
			// (MediaItemsResponseCatalog) gson.fromJson(response,
			// MediaItemsResponseCatalog.class);
			//
			// if (mediaItemsResponseCatalog != null) {
			// items = mediaItemsResponseCatalog.getCatalog().getContent();
			// }
			//
			// if (items == null) {
			// items = new ArrayList<MediaItem>();
			// }
			//
			// // TODO: temporally solving the differentiating issue between
			// Music
			// and Videos, solve this when inserting also campaigns.
			// for (MediaItem mediaItem : items) {
			// mediaItem.setMediaContentType(MediaContentType.MUSIC);
			// }
			//
			// HashMap<String, Object> resultMap = new HashMap<String,
			// Object>();
			// resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEMS, items);
			// resultMap.put(RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE,
			// mediaItemsResponseCatalog.getCatalog());
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

			resultMap.put(RESULT_KEY_DISCOVER_SEARCH_RESULT_INDEXER,
					new DiscoverSearchResultIndexer(startIndex, length, total));

			// gets the list of media items.

			int contentId;
			String albumName;
			String title;
			String imageUrl;
			String bigImageUrl;
			String type;
			int trackCount;
			Map<String, List<String>> images;
			long albumId;
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
				images = (Map<String, List<String>>) mediaItemMap
						.get(MediaItem.KEY_IMAGES);
				albumId = (Long) mediaItemMap.get(MediaItem.KEY_ALBUM_ID);

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
		} catch (ParseException e) {
			e.printStackTrace();

		}
		resultMap.put(RESULT_KEY_MEDIA_ITEMS, mediaItems);

		return resultMap;

	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
