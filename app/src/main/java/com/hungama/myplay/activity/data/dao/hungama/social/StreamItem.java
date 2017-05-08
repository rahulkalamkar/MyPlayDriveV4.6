/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.util.JsonUtils;

/**
 * An item in My Stream
 */
public class StreamItem {

	// "action": "is listening to a song",
	// "user_id": 84038755,
	// "user_name": "hungama surat",
	// "photo_url":
	// "http://www.hungama.com/themes/hungamaTheme/images/default_profile.jpg",
	// "content_id": 403642,
	// "title": "",
	// "album_name": "Chandra Chakori",
	// "images": {
	// "image_100x100": [
	// "http://repos.hungama.com/audio/display%20image/100x100%20jpeg/6292335.jpg"
	// ],
	// "image_400x400": [
	// "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/55323221.jpeg"
	// ]
	// },
	// "type": "track",
	// "time": "2015-04-20 19:33:22",
	// "more_songs_count": 20,
	// "more_songs": "and 20 more songs",
	// "more_songs_data": [

	public static final String TYPE_BADGE = "badge";

	public static final String KEY_ACTION = "action";
	public static final String KEY_USER_ID = "user_id";
	public static final String KEY_USER_NAME = "user_name";
	public static final String KEY_PHOTO_URL = "photo_url";
	public static final String KEY_CONTENT_ID = "content_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ALBUM_NAME = "album_name";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_IMAGES = "images";
	public static final String KEY_BIG_IMAGE = "big_image";
	public static final String KEY_TYPE = "type";
	public static final String KEY_TIME = "time";
	public static final String KEY_MORE_SONGS_COUNT = "more_songs_count";
	public static final String KEY_MORE_SONGS = "more_songs";
	public static final String KEY_MORE_SONGS_DATA = "more_songs_data";
	public static final String KEY_ALBUM_ID = "album_id";

	@SerializedName(KEY_ACTION)
	public final String action;
	@SerializedName(KEY_USER_ID)
	public final long userId;
	@SerializedName(KEY_USER_NAME)
	public final String userName;
	@SerializedName(KEY_PHOTO_URL)
	public final String photoUrl;
	@SerializedName(KEY_CONTENT_ID)
	public final long conentId;
	@SerializedName(KEY_TITLE)
	public final String title;
	@SerializedName(KEY_ALBUM_NAME)
	public final String albumName;
	@SerializedName(KEY_IMAGE)
	public final String imageUrl;
	@SerializedName(KEY_BIG_IMAGE)
	public final String bigImageUrl;
	@SerializedName(KEY_TYPE)
	public final String type;
	@SerializedName(KEY_TIME)
	public final String time;
	@SerializedName(KEY_MORE_SONGS_COUNT)
	public final int songsCount;
	@SerializedName(KEY_MORE_SONGS)
	public final String moreSongs;
	@SerializedName(KEY_MORE_SONGS_DATA)
	public final List<MediaItem> moreSongsItems;
	@Expose
	@SerializedName(KEY_IMAGES)
	protected Map<String, List<String>> imagesUrlArray;

	private Date date;

	@Expose
	@SerializedName(KEY_ALBUM_ID)
	private long albumId = 0;

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public StreamItem(String action, long userId, String userName,
			String photoUrl, long conentId, String title, String albumName,
			String imageUrl, String bigImageUrl, String type, String time,
			int songsCount, String moreSongs, List<MediaItem> moreSongsItems,
			long albumId) {

		this.action = action;
		this.userId = userId;
		this.userName = userName;
		this.photoUrl = photoUrl;
		this.conentId = conentId;
		this.title = title;
		this.albumName = albumName;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
		this.type = type;
		this.time = time;
		this.songsCount = songsCount;
		this.moreSongs = moreSongs;
		this.moreSongsItems = moreSongsItems;
		setAlbumId(albumId);
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return this.date;
	}

	public String getImagesUrlArray() {
		try {
			return new JsonUtils().mapToJson(imagesUrlArray).toString();
			// return new JSONObject((Map<String,List<String>>)
			// imagesUrlArray).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, List<String>> getImages() {
		return imagesUrlArray;
	}

	public void setImagesUrlArray(Map<String, List<String>> images) {
		imagesUrlArray = images;
	}

}
