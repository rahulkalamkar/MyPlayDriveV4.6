package com.hungama.myplay.activity.operations.hungama;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

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
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;

public class MediaDetailsOperation extends HungamaOperation {

	private static final String TAG = "MediaDetailsOperation";

	public static final String RESPONSE_KEY_MEDIA_ITEM = "response_key_media_item";
	public static final String RESPONSE_KEY_MEDIA_DETAILS = "response_key_media_details";
	public static final String RESPONSE_KEY_PLAYER_OPTION = "response_key_player_option";

	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final MediaItem mMediaItem;
	private final PlayerOption mPlayerOption;
	private final String images;
	private final String locale;

	public MediaDetailsOperation(String serverUrl, String authKey,
			String userId, MediaItem mediaItem, PlayerOption playerOption,
			String images) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		mMediaItem = mediaItem;
		mPlayerOption = playerOption;
		this.images = images;
		locale = null;
	}

	public MediaDetailsOperation(String serverUrl, String authKey,
			String userId, MediaItem mediaItem, PlayerOption playerOption,
			String images, String locale) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		mMediaItem = mediaItem;
		mPlayerOption = playerOption;
		this.images = images;
		this.locale = locale;
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

	public MediaDetailsOperation(String serverUrl, String authKey,
			String userId, MediaTrackDetails songmediaItem,
			PlayerOption playerOption, boolean isVideo, String images) {

		mServerUrl = serverUrl;
		mAuthKey = authKey;
		mUserId = userId;
		if (isVideo)
			mMediaItem = new MediaItem(songmediaItem.getMusicAlbumId(),
					songmediaItem.getAlbumName(), "",
					songmediaItem.getMusicDirector(),
					songmediaItem.getImageUrl(),
					songmediaItem.getBigImageUrl(), MediaType.ALBUM.toString(),
					0, songmediaItem.getAlbumId());
		else
			mMediaItem = new MediaItem(songmediaItem.getAlbumId(),
					songmediaItem.getAlbumName(), "",
					songmediaItem.getMusicDirector(),
					songmediaItem.getImageUrl(),
					songmediaItem.getBigImageUrl(), MediaType.ALBUM.toString(),
					0, songmediaItem.getAlbumId());
		mPlayerOption = playerOption;
		this.images = images;
		locale = null;
	}

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
		DeviceConfigurations dc = DeviceConfigurations.getInstance(context);
		// builds the segment for the media item request url.
		String mediaDetailsSegment = null;
		String videoOrMusicSegment = null;
		String albumIDParameter = "";
		if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			mediaDetailsSegment = HungamaOperation.URL_SEGMENT_DETAILS_ALBUM;
			videoOrMusicSegment = URL_SEGMENT_MUSIC;
		} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			mediaDetailsSegment = HungamaOperation.URL_SEGMENT_DETAILS_PLAYLIST;
			videoOrMusicSegment = URL_SEGMENT_MUSIC;
		} else if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO
				&& mMediaItem.getMediaType() == MediaType.TRACK) {
			mediaDetailsSegment = HungamaOperation.URL_SEGMENT_DETAILS_VIDEO;
			videoOrMusicSegment = URL_SEGMENT_VIDEO;
		} else if (mMediaItem.getMediaType() == MediaType.TRACK) {
			mediaDetailsSegment = HungamaOperation.URL_SEGMENT_DETAILS_SONG;
			videoOrMusicSegment = URL_SEGMENT_MUSIC;
			if (mMediaItem.getAlbumId() != 0)
				albumIDParameter = "&album_id=" + mMediaItem.getAlbumId();
		} else if (mMediaItem.getMediaContentType() == MediaContentType.VIDEO
				&& mMediaItem.getMediaType() == MediaType.VIDEO) {
			mediaDetailsSegment = HungamaOperation.URL_SEGMENT_DETAILS_VIDEO;
			videoOrMusicSegment = URL_SEGMENT_VIDEO;
		}

		String imageParams = "";
		if (!TextUtils.isEmpty(images))
			imageParams = "&images=" + images;

		String localeParams = "";
		if (!TextUtils.isEmpty(locale))
			localeParams = "&locale=" + locale;

		// return mServerUrl + URL_SEGMENT_CONTENT + videoOrMusicSegment +
		// mediaDetailsSegment + Long.toString(mMediaItem.getId()) +
		// "?" + PARAMS_AUTH_KEY + "=" + mAuthKey + "&" + PARAMS_USER_ID + "=" +
		// mUserId + albumIDParameter;
		return mServerUrl + URL_SEGMENT_CONTENT + videoOrMusicSegment
				+ mediaDetailsSegment + "?" + PARAMS_USER_ID + "=" + mUserId
				+ albumIDParameter + "&content_id="
				+ Long.toString(mMediaItem.getId()) + AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + dc.getHardwareId()
				+ imageParams + localeParams;
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

		if (Thread.currentThread().isInterrupted()) {
			throw new OperationCancelledException();
		}

		try {
			if (mMediaItem.getMediaType() == MediaType.ALBUM) {
				// removes all the shitty wrapping stuff from hungama response.
				response.response = response.response.replace(
						"\"response\":{\"musicalbum\":{", "");
				response.response = response.response.replace(
						"},\"musiclisting\":{", ",");
				response.response = response.response.replace(
						"},\"videolisting\":{\"track\"", ",\"video\"");

				if (response.response.length() < 2) {

					return new HashMap<String, Object>();
				}

				response.response = response.response.substring(0,
						response.response.length() - 2);

				if (Thread.currentThread().isInterrupted()) {
					throw new OperationCancelledException();
				}
				Logger.e("response.response--",response.response);
				MediaSetDetails setDetails = (MediaSetDetails) gson.fromJson(
						response.response, MediaSetDetails.class);
				mMediaItem.setMusicTrackCount(setDetails.getNumberOfTracks());

				if (Thread.currentThread().isInterrupted()) {
					throw new OperationCancelledException();
				}

				resultMap.put(RESPONSE_KEY_MEDIA_DETAILS, setDetails);

			} else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
				// removes all the shitty wrapping stuff from hungama response.
				response.response = response.response.replace(
						"\"response\":{\"playlist\":{", "");
				response.response = response.response.replace(
						"},\"musiclisting\":{", ",");
				response.response = response.response.replace(
						"},\"videolisting\":{\"track\"", ",\"video\"");

				if (response.response.length() < 2) {

					return new HashMap<String, Object>();
				}

				response.response = response.response.substring(0,
						response.response.length() - 2);

				if (Thread.currentThread().isInterrupted()) {
					throw new OperationCancelledException();
				}

				MediaSetDetails setDetails = (MediaSetDetails) gson.fromJson(
						response.response, MediaSetDetails.class);
				mMediaItem.setMusicTrackCount(setDetails.getNumberOfTracks());

				if (Thread.currentThread().isInterrupted()) {
					throw new OperationCancelledException();
				}

				resultMap.put(RESPONSE_KEY_MEDIA_DETAILS, setDetails);

			} else {
				// response.response =
				// response.response.replace("\"response\":{",
				// "");//{\"response\":{\"content\":
				// response.response = response.response.substring(0,
				// response.response.length() - 1);

				if (Thread.currentThread().isInterrupted()) {
					throw new OperationCancelledException();
				}

				MediaTrackDetails trackDetails = (MediaTrackDetails) gson
						.fromJson(new JSONObject(response.response)
								.getJSONObject("response").toString(),
								MediaTrackDetails.class);

				if (Thread.currentThread().isInterrupted()) {
					throw new OperationCancelledException();
				}

				resultMap.put(RESPONSE_KEY_MEDIA_DETAILS, trackDetails);
			}
			// adds the media item itself so we can recognize it.
			resultMap.put(RESPONSE_KEY_MEDIA_ITEM, mMediaItem);
			resultMap.put(RESPONSE_KEY_PLAYER_OPTION, mPlayerOption);

			return resultMap;

		} catch (JsonSyntaxException exception) {
			Logger.e(TAG, exception.toString());
			Logger.printStackTrace(exception);
			throw new InvalidResponseDataException();
		} catch (JsonParseException exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch (Exception exception) {
			Logger.e(TAG, exception.toString());
			throw new InvalidResponseDataException();
		} catch(Error e){
            Logger.e(TAG, e.toString());
            throw new InvalidResponseDataException();
        }
	}

	@Override
	public String getTimeStampCache() {
		// TODO Auto-generated method stub
		return null;
	}

}
