package com.hungama.myplay.activity.data.dao.hungama;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.JsonUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 * 
 */
public class MediaItem implements Serializable {

	public static final String KEY_CONTENT_ID = "content_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ALBUM_NAME = "album_name";
	public static final String KEY_ARTIST_NAME = "artist_name";
	public static final String KEY_IMAGE = "image";
	public static final String KEY_IMAGES = "images";
	public static final String KEY_BIG_IMAGE = "big_image";
	public static final String KEY_TYPE = "type";
	public static final String KEY_USER_FAVORITE = "user_fav";
	public static final String KEY_MUSIC_TRACKS_COUNT_SEARCH = "music_tracks_count";
	public static final String KEY_MUSIC_TRACKS_COUNT = "songs_count";
	public static final String KEY_ALBUM_ID = "album_id";

	public static final String KEY_CONTENT_ID_RADIO = "radio_id";
	public static final String KEY_TITLE_RADIO = "radio_name";
	public static final String KEY_PLAYLIST_ARTWORK = "playlist_artwork";
	public static final String KEY_SONG_IDS = "song_ids";
	public static final String KEY_SONG_ID_TEST = "song_ids_test";


	@Expose
	@SerializedName(KEY_CONTENT_ID_RADIO)
	protected long id_radio;
	@Expose
	@SerializedName(KEY_TITLE_RADIO)
	protected String title_radio;

	@Expose
	@SerializedName(KEY_CONTENT_ID)
	protected final long id;
	@Expose
	@SerializedName(KEY_TITLE)
	protected final String title;
	@Expose
	@SerializedName(KEY_ALBUM_NAME)
	protected final String albumName;
	@Expose
	@SerializedName(KEY_ARTIST_NAME)
	protected final String artistName;
	@Expose
	@SerializedName(KEY_IMAGE)
	protected final String imageUrl;
	@Expose
	@SerializedName(KEY_IMAGES)
	protected Map<String, List<String>> imagesUrlArray;
	@Expose
	@SerializedName(KEY_BIG_IMAGE)
	protected final String bigImageUrl;
	@Expose
	@SerializedName(KEY_TYPE)
	protected final String type;
	@Expose
	@SerializedName(KEY_MUSIC_TRACKS_COUNT)
	protected int musicTrackCount;
	@Expose
	@SerializedName(KEY_MUSIC_TRACKS_COUNT_SEARCH)
	protected int musicTrackCountSearch;
	@Expose
	@SerializedName(KEY_USER_FAVORITE)
	protected int user_fav;
	@Expose
	@SerializedName(KEY_PLAYLIST_ARTWORK)
	protected String playlist_artwork;

	public List<Long> getSongIds() {
		return songIds;
	}

	@Expose
	@SerializedName(KEY_SONG_IDS)
	protected List<Long> songIds;

	@Expose
	@SerializedName(KEY_SONG_ID_TEST)
	protected List<MediaItemPlaylist> songIdList;

	public List<MediaItemPlaylist> getSongIdsList() {
		return songIdList;
	}


	public boolean getUserFav() {
		return (user_fav == 1) ? true : false;
	}

	public void setUserFav(boolean user_fav) {
		this.user_fav = (user_fav ? 1 : 0);
	}

	protected MediaType mediaType = null;
	protected MediaContentType mediaContentType = null;

	@Expose
	@SerializedName(KEY_ALBUM_ID)
	private long albumId = 0;

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public MediaItem(long id, String title, String albumName,
			String artistName, String imageUrl, String bigImageUrl,
			String type, int musicTrackCount, long albumId) {
		this.id = id;
		this.title = title;
		this.albumName = albumName;
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
		this.type = type;
		this.musicTrackCount = musicTrackCount;
		this.musicTrackCountSearch = musicTrackCount;

		this.user_fav = 0;
		this.imagesUrlArray = null;
		setAlbumId(albumId);
	}

	public MediaItem(long id, String title, String albumName,
			String artistName, String imageUrl, String bigImageUrl,
			String type, int musicTrackCount, int user_fav, long albumId) {

		this.id = id;
		this.title = title;
		this.albumName = albumName;
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
		this.type = type;
		this.musicTrackCount = musicTrackCount;
		this.musicTrackCountSearch = musicTrackCount;
		this.user_fav = user_fav;
		this.imagesUrlArray = null;
		setAlbumId(albumId);
	}

	public MediaItem(long id, String title, String albumName,
			String artistName, String imageUrl, String bigImageUrl,
			String type, int musicTrackCount, int user_fav,
			Map<String, List<String>> imagesUrlArray, long albumId) {

		this.id = id;
		this.title = title;
		this.albumName = albumName;
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
		this.type = type;
		this.musicTrackCount = musicTrackCount;
		this.musicTrackCountSearch = musicTrackCount;
		this.user_fav = user_fav;
		this.imagesUrlArray = imagesUrlArray;
		setAlbumId(albumId);
	}

	public long getId() {
		if (id == 0 && id_radio > 0) {
			return id_radio;
		}
		return id;
	}

	public String getTitle() {
		if (TextUtils.isEmpty(title) && !TextUtils.isEmpty(title_radio)) {
			return title_radio;
		}
		return title;
	}

	public String getAlbumName() {
		return albumName;
	}

	public String getArtistName() {
		return artistName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getBigImageUrl() {
		return bigImageUrl;
	}

	public MediaType getMediaType() {
		if (mediaType == null)
			if (type != null)
				mediaType = MediaType.getMediaItemByName(type);
			else
				mediaType = MediaType.TRACK;

		return mediaType;
	}

	public MediaContentType getMediaContentType() {
		if (mediaContentType == null)
			mediaContentType = MediaContentType.MUSIC;
		return mediaContentType;
	}

	public void setMediaContentType(MediaContentType mediaContentType) {
		this.mediaContentType = mediaContentType;
	}

	public int getMusicTrackCount() {
		if (musicTrackCount != 0)
			return musicTrackCount;
		else
			return musicTrackCountSearch;
	}

	public void setMusicTrackCount(int value) {
		this.musicTrackCount = value;
		this.musicTrackCountSearch = musicTrackCount;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	// public Iterator<String> getImagesKeys(){
	// try{
	// if(!TextUtils.isEmpty(imagesUrlArray)){
	// JSONObject jsonImages = new JSONObject(imagesUrlArray);
	// return jsonImages.keys();
	// }
	// } catch(Exception e){
	// Logger.printStackTrace(e);
	// }
	// return null;
	// }

	// public String[] getImagesUrlArray(){
	// String[] urls = new String[0];
	// try{
	// if(!TextUtils.isEmpty(imagesUrlArray)){
	// JSONObject jsonImages = new JSONObject(imagesUrlArray);
	// Iterator<String> imagesKey = jsonImages.keys();
	// }
	// } catch(Exception e){
	// Logger.printStackTrace(e);
	// urls = new String[0];
	// }
	// return urls;
	// }

	public String getImagesUrlArray() {
		try {
			return JsonUtils.mapToJson(imagesUrlArray).toString();
			// return new JSONObject((Map<String,List<String>>)
			// imagesUrlArray).toString();
		} catch (Exception e) {
			// e.printStackTrace();
			return null;
		}
	}

	public Map<String, List<String>> getImages() {
		return imagesUrlArray;
	}

	public void setImagesUrlArray(Map<String, List<String>> images) {
		imagesUrlArray = images;
	}

	public MediaItem getCopy() {
		long id = Long.valueOf(this.id);
		String title = TextUtils.isEmpty(this.title) ? null : this.title;
		String albumName = TextUtils.isEmpty(this.albumName) ? null
				: this.albumName;
		String artistName = TextUtils.isEmpty(this.artistName) ? null
				: this.artistName;
		String imageUrl = TextUtils.isEmpty(this.imageUrl) ? null
				: this.imageUrl;
		String bigImageUrl = TextUtils.isEmpty(this.bigImageUrl) ? null
				: this.bigImageUrl;
		String type = TextUtils.isEmpty(this.type) ? null : this.type;
		Map<String, List<String>> images = (this.imagesUrlArray == null) ? null
				: this.imagesUrlArray;
		int musicTrackCount = Integer.valueOf(this.musicTrackCount);

		MediaType mediaType = MediaType.getMediaItemByName(type);
		MediaContentType mediaContentType = null;
		mediaContentType = MediaContentType.getCopy(mediaContentType);

		MediaItem mediaItem = new MediaItem(id, title, albumName, artistName,
				imageUrl, bigImageUrl, type, musicTrackCount, user_fav, images,
				albumId);
		mediaItem.setMediaType(mediaType);
		mediaItem.setMediaContentType(mediaContentType);
		// mediaItem.setAlbumId(albumId);

		return mediaItem;
	}

	public Object tag;

	public String getPlaylistArtwork(){
//		return "http://content.hungama.com/audio%20album/display%20image/400x400%20jpeg/431636121.jpg";
		return playlist_artwork;
	}
}
