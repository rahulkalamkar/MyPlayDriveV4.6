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
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class RelatedVideoOperation extends HungamaOperation {

	private static final String TAG = "RelatedVideoOperation";

	public static final String RESPONSE_KEY_RELATED_VIDEO = "response_key_related_video";
	public static final String RESPONSE_KEY_RELATED_VIDEO_MEDIA_CONTENT_TYPE = "response_key_related_video_media_content_type";

	private final String mServerUrl;
	private final String mAlbumId;
	private final String mAuthKey;
	private final MediaContentType mMediaContentType;
	private final MediaType mMediaType;
	private final String mImages;
	private final String mUserId;

	public RelatedVideoOperation(String serverUrl, String albumId,
			MediaContentType mediaContentType, MediaType mediaType,
			String authKey, String images, String userId) {
		mServerUrl = serverUrl;
		mAlbumId = albumId;
		mMediaContentType = mediaContentType;
		mMediaType = mediaType;
		mAuthKey = authKey;
		mImages = images;
		mUserId = userId;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.VIDEO_RELATED;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		DeviceConfigurations config = DeviceConfigurations.getInstance(context);
		String serviceUrl = mServerUrl + URL_SEGMENT_CONTENT
				+ URL_SEGMENT_VIDEO + URL_SEGMENT_RELATED_VIDEO + "?"
				+ PARAMS_DOWNLOAD_HARDWARE_ID + HungamaOperation.EQUALS
				+ config.getHardwareId() + "&album_id=" + mAlbumId + "&"
				+ PARAMS_USER_ID + "=" + mUserId;
		if (mImages != null && !TextUtils.isEmpty(mImages)) {
			serviceUrl = serviceUrl + "&images=" + mImages;
		}
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
		Map<String, Object> resultMap = new HashMap<String, Object>();
		List<MediaItem> items = null;

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		try {

			if (TextUtils.isEmpty(response.response)) {
				response.response = "";
			}

			if (response.response.contains("\"images\":[]")) {
				response.response.replace("\"images\":[]", "\"images\":{}");
			}

			MediaItemsResponseCatalog mediaItemsResponseCatalog = (MediaItemsResponseCatalog) gson
					.fromJson(response.response,
							MediaItemsResponseCatalog.class);

			if (mediaItemsResponseCatalog != null) {
				items = mediaItemsResponseCatalog.getCatalog().getContent();
			}

			if (items == null) {
				items = new ArrayList<MediaItem>();
			}

			Logger.e("items:--- ", "" + items.size());
			// TODO: temporally solving the differentiating issue between Music
			// and Videos, solve this when inserting also campaigns.
			for (MediaItem mediaItem : items) {
				if (mediaItem != null) {
					mediaItem.setMediaContentType(mMediaContentType);
					mediaItem.setMediaType(MediaType.TRACK);
				}
			}

			resultMap.put(RESPONSE_KEY_RELATED_VIDEO_MEDIA_CONTENT_TYPE,
					mMediaContentType);
			// resultMap.put(RESULT_KEY_OBJECT_MEDIA_CATEGORY_TYPE,
			// mItemCategoryType);
			resultMap.put(RESPONSE_KEY_RELATED_VIDEO, items);

		} catch (JsonSyntaxException exception) {
			throw new InvalidResponseDataException();

		} catch (JsonParseException exception) {
			throw new InvalidResponseDataException();
		} catch (Exception exception) {
			throw new InvalidResponseDataException();
		}
		return resultMap;
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
