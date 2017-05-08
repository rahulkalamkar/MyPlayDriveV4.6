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
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

public class RadioTopArtistSongsOperation extends WebRadioOperation {

	private static final String TAG = "RadioTopArtistSongsOperation";

	private static final String PARAMS_ARTIST_ID = "radio_id";

	public static final String RESULT_KEY_OBJECT_TRACKS = "result_key_object_tracks";
	public static final String RESULT_KEY_OBJECT_MEDIA_ITEM = "result_key_object_media_item";
	public static final String RESULT_KEY_OBJECT_USER_FAVORITE = "result_key_object_user_favorite";

	private final String mServerUrl;
	private final String mAuthKey;
	private final String mUserId;
	private final String mImages;
	private final MediaItem mArtistItem;

	public RadioTopArtistSongsOperation(String serverUrl, String authKey,
			String mUserId, MediaItem artistItem, String mImages) {
		mServerUrl = serverUrl;
		mAuthKey = authKey;
		this.mUserId = mUserId;
		mArtistItem = artistItem;
		this.mImages = mImages;
	}

	@Override
	public int getOperationId() {
		return OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS;
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
		String serverUrl;
		if(mArtistItem.getMediaType() == MediaType.ARTIST_OLD)
			serverUrl = mServerUrl + URL_SEGMENT_RADIO_TOP_CELEB_SONGS;
		else
			serverUrl = mServerUrl + URL_SEGMENT_RADIO_TOP_ARTIST_SONGS
					+ PARAMS_ARTIST_ID + EQUALS;
		serverUrl += Long.toString(mArtistItem.getId()) + AMPERSAND
				+ PARAMS_USER_ID + EQUALS + mUserId + AMPERSAND
				+ PARAMS_DOWNLOAD_HARDWARE_ID + EQUALS + config.getHardwareId()
				+ "&device=android";
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
			// response =
			// "{\"response\":{\"code\":3,\"message\":\"There are no songs for this artist\",\"display\":0}}";
			// // TESTING
			// response = "{\"catalog\":{\"results\":48,\"content\":[]}}"; //
			// TESTING
			Map<String, Object> catalogMap = (Map<String, Object>) parser
					.parse(response.response);

			if (catalogMap.containsKey(KEY_CATALOG)) {
				catalogMap = (Map<String, Object>) catalogMap.get(KEY_CATALOG);
			} else {
				if (catalogMap.containsKey(KEY_RESPONSE)) {
					catalogMap = (Map<String, Object>) catalogMap
							.get(KEY_RESPONSE);
					if (catalogMap.containsKey(KEY_MESSAGE)) {
						throw new InvalidResponseDataException(
								(String) catalogMap.get(KEY_MESSAGE));
					}
				}
			}

			MediaItem updatedMediaItem = null;
			if (mArtistItem.getTitle().equalsIgnoreCase("Celeb Radio")
					&& catalogMap.containsKey("artist_name")) {
				Map<String, List<String>> images = null;
				try {
					images = (Map<String, List<String>>) catalogMap
							.get(MediaItem.KEY_IMAGES);
				} catch (Exception e){
					Logger.printStackTrace(e);
				}
				updatedMediaItem = new MediaItem(mArtistItem.getId(),
						(String) catalogMap.get("artist_name"), "", "", "", "",
						MediaType.ARTIST.toString(), 0, 0, images, 0);
				updatedMediaItem.setMediaType(mArtistItem.getMediaType());
				updatedMediaItem.setMediaContentType(MediaContentType.RADIO);
			}

			int userFav = 0;
			if (catalogMap.containsKey(KEY_USER_FAV)) {
				long temp = (Long) catalogMap.get(KEY_USER_FAV);
				userFav = (int) temp;
			}

			if (catalogMap.containsKey("tracks")) {

				List<Map<String, Object>> contentMap = (List<Map<String, Object>>) catalogMap
						.get("tracks");

				if (Utils.isListEmpty(contentMap)) {
					throw new InvalidResponseDataException(
							"There are no songs for this artist");
				}

				List<Track> radioTracks = new ArrayList<Track>();

				long id;
				String title;
				String albumName;
				String artistName;
				String imageUrl;
				Map<String, List<String>> images;
				long albumId;

				Track track;

				for (Map<String, Object> stationMap : contentMap) {

					id = ((Long) stationMap.get(MediaItem.KEY_CONTENT_ID))
							.longValue();
					title = (String) stationMap.get(MediaItem.KEY_TITLE);
					albumName = (String) stationMap
							.get(MediaItem.KEY_ALBUM_NAME);
					artistName = (String) stationMap
							.get(MediaItem.KEY_ARTIST_NAME);
					imageUrl = (String) stationMap.get(MediaItem.KEY_IMAGE);
					images = (Map<String, List<String>>) stationMap
							.get(MediaItem.KEY_IMAGES);
					albumId = (Long) stationMap.get(MediaItem.KEY_ALBUM_ID);

					track = new Track(id, title, albumName, artistName,
							imageUrl, imageUrl, images, albumId);
					radioTracks.add(track);
				}

				Map<String, Object> resultMap = new HashMap<String, Object>();
				resultMap.put(RESULT_KEY_OBJECT_TRACKS, radioTracks);
				if (updatedMediaItem == null)
					resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEM, mArtistItem);
				else
					resultMap.put(RESULT_KEY_OBJECT_MEDIA_ITEM,
							updatedMediaItem);
				resultMap.put(RESULT_KEY_OBJECT_USER_FAVORITE, userFav);

				return resultMap;

			} else {
				throw new InvalidResponseDataException(
						"Parsing error - no content available");
			}
		} catch (ParseException e) {
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
