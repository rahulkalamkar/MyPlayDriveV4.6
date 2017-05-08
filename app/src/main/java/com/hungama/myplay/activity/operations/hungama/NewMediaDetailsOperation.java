package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.RequestMethod;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class NewMediaDetailsOperation extends HungamaOperation {

	private static final String TAG = "MediaDetailsOperation";

	public static final String RESPONSE_KEY_MEDIA_TITLE = "response_key_media_title";
	// public static final String RESPONSE_KEY_MEDIA_DETAILS =
	// "response_key_media_details";
	// public static final String RESPONSE_KEY_PLAYER_OPTION =
	// "response_key_player_option";

	private final String mServerUrl;
	// private final String mAuthKey;
	// private final String mUserId;
	private final String mContentId;

	// private final PlayerOption mPlayerOption;

	public NewMediaDetailsOperation(String serverUrl, String contentId) {
		mServerUrl = serverUrl;
		// mAuthKey = authKey;
		// mUserId = userId;
		mContentId = contentId;
		// mPlayerOption = playerOption;
	}

	// public MediaDetailsOperation(String serverUrl, String authKey, String
	// userId, MediaTrackDetails songmediaItem, PlayerOption playerOption)
	// {
	// mServerUrl = serverUrl;
	// mAuthKey = authKey;
	// mUserId = userId;
	// mMediaItem = new MediaItem(songmediaItem.getAlbumId(),
	// songmediaItem.getAlbumName(), "",
	// songmediaItem.getMusicDirector(),songmediaItem.getImageUrl(),
	// songmediaItem.getBigImageUrl(), MediaType.ALBUM.toString(), 0);
	// mPlayerOption = playerOption;
	// }

	// public NewMediaDetailsOperation(String serverUrl, String authKey,
	// String userId, MediaTrackDetails songmediaItem,
	// PlayerOption playerOption, boolean isVideo) {
	//
	// mServerUrl = serverUrl;
	// // mAuthKey = authKey;
	// // mUserId = userId;
	// if (isVideo)
	// mMediaItem = new MediaItem(songmediaItem.getMusicAlbumId(),
	// songmediaItem.getAlbumName(), "",
	// songmediaItem.getMusicDirector(),
	// songmediaItem.getImageUrl(),
	// songmediaItem.getBigImageUrl(), MediaType.ALBUM.toString(),
	// 0);
	// else
	// mMediaItem = new MediaItem(songmediaItem.getAlbumId(),
	// songmediaItem.getAlbumName(), "",
	// songmediaItem.getMusicDirector(),
	// songmediaItem.getImageUrl(),
	// songmediaItem.getBigImageUrl(), MediaType.ALBUM.toString(),
	// 0);
	// // mPlayerOption = playerOption;
	// }

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.MEDIA_DETAILS;
	}

	@Override
	public RequestMethod getRequestMethod() {
		return RequestMethod.GET;
	}

	@Override
	public String getServiceUrl(final Context context) {
		// builds the segment for the media item request url.
		String mediaDetailsSegment = null;
		// String videoOrMusicSegment = null;
		// String albumIDParameter = "";
		// if (mMediaItem.getMediaType() == MediaType.ALBUM) {
		mediaDetailsSegment = HungamaOperation.URL_SEGMENT_MEDIA_DETAILS;
		// videoOrMusicSegment = URL_SEGMENT_MUSIC;
		// } else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
		// mediaDetailsSegment = HungamaOperation.URL_SEGMENT_DETAILS_PLAYLIST;
		// videoOrMusicSegment = URL_SEGMENT_MUSIC;
		// } else if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO
		// &&
		// mMediaItem.getMediaType() == MediaType.TRACK ) {
		// mediaDetailsSegment = HungamaOperation.URL_SEGMENT_DETAILS_VIDEO;
		// videoOrMusicSegment = URL_SEGMENT_VIDEO;
		// } else if (mMediaItem.getMediaType() == MediaType.TRACK ) {
		// mediaDetailsSegment = HungamaOperation.URL_SEGMENT_DETAILS_SONG;
		// videoOrMusicSegment = URL_SEGMENT_MUSIC;
		// if(mMediaItem.getAlbumId()!=0)
		// albumIDParameter = "&album_id=" + mMediaItem.getAlbumId();
		// } else if(mMediaItem.getMediaContentType() == MediaContentType.VIDEO
		// &&
		// mMediaItem.getMediaType() == MediaType.VIDEO ){
		// mediaDetailsSegment = HungamaOperation.URL_SEGMENT_DETAILS_VIDEO;
		// videoOrMusicSegment = URL_SEGMENT_VIDEO;
		// }

		return mServerUrl + URL_SEGMENT_CONTENT + mediaDetailsSegment
				+ mContentId + "&type=playlist";
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

		// Gson gson = new
		// GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		Map<String, Object> resultMap = new HashMap<String, Object>();

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		try {
			// if (mMediaItem.getMediaType() == MediaType.ALBUM ||
			// mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			// // removes all the shitty wrapping stuff from hungama response.
			// response = response.replace("\"content\":{\"musicalbum\":{", "");
			// response = response.replace("},\"musiclisting\":{", ",");
			// response = response.replace("},\"videolisting\":{\"track\"",
			// ",\"video\"");
			//
			// if (response.length() < 2) {
			//
			// return new HashMap<String, Object>();
			// }
			//
			// response = response.substring(0, response.length() - 2);
			//
			// if (Thread.currentThread().isInterrupted()) { throw new
			// OperationCancelledException(); }
			//
			// MediaSetDetails setDetails = (MediaSetDetails)
			// gson.fromJson(response, MediaSetDetails.class);
			// mMediaItem.setMusicTrackCount(setDetails.getNumberOfTracks());
			//
			// if (Thread.currentThread().isInterrupted()) { throw new
			// OperationCancelledException(); }
			//
			// resultMap.put(RESPONSE_KEY_MEDIA_DETAILS, setDetails);
			//
			// } else {
			// response = response.replace("{\"catalog\":{\"content\":", "");
			// response = response.substring(0, response.length() - 2);
			//
			// if (Thread.currentThread().isInterrupted()) { throw new
			// OperationCancelledException(); }
			//
			// MediaTrackDetails trackDetails = (MediaTrackDetails)
			// gson.fromJson(response, MediaTrackDetails.class);
			//
			// if (Thread.currentThread().isInterrupted()) { throw new
			// OperationCancelledException(); }
			//
			// resultMap.put(RESPONSE_KEY_MEDIA_DETAILS, trackDetails);
			// }
			// adds the media item itself so we can recognize it.
			// resultMap.put(RESPONSE_KEY_MEDIA_ITEM, mMediaItem);
			// resultMap.put(RESPONSE_KEY_PLAYER_OPTION, mPlayerOption);
			JSONObject jsonResponse = new JSONObject(response.response);
			jsonResponse = jsonResponse.getJSONObject("response");
			if (jsonResponse.getInt("code") == 1) {
				JSONObject jsonMetadata = jsonResponse
						.getJSONObject("metadata");
				resultMap.put(RESPONSE_KEY_MEDIA_TITLE,
						jsonMetadata.getString("title"));
			}

			return resultMap;

		} catch (JsonSyntaxException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (JsonParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (Exception exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		}
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
