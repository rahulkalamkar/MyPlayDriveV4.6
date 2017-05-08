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
public class TrackSimilarOperation extends HungamaOperation {

	private static final String TAG = "TrackSimilarOperation";

	public static final String RESULT_KEY_OBJECT_MEDIA_ITEMS = "result_key_object_media_items";
	public static final String RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE = "response_key_object_media_items_response";

	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final Track mTrack;
	private final String mStart;
	private final String mLength;
	private final String mImages;

	public TrackSimilarOperation(String serverUrl, String authKey,
			String userId, Track track, String start, String length,
			String images) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		mTrack = track;
		mStart = start;
		mLength = length;
		mImages = images;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.TRACK_SIMILAR;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		String id = "";
		if (mTrack != null)
			id = "" + mTrack.getId();

		DeviceConfigurations config = DeviceConfigurations.getInstance(context);

		String imageParams = "";
		if (!TextUtils.isEmpty(mImages))
			imageParams = "&images=" + mImages;

		String serviceUrl = mServerUrl + URL_SEGMENT_CONTENT
				+ URL_SEGMENT_MUSIC + URL_SEGMENT_SIMILAR + "?"
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND + PARAMS_START
				+ EQUALS + mStart + AMPERSAND + PARAMS_LENGTH + EQUALS
				+ mLength + HungamaOperation.AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId() + HungamaOperation.AMPERSAND
				+ PARAMS_CONTENT_ID + HungamaOperation.EQUALS + id
				+ imageParams;

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

			return resultMap;

		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		}

	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
