package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.JsonUtils;

/**
 * Represents a single song to be played from a {@link MediaItem}.
 */
public class Track implements Serializable {

	public static final String KEY_CONTENT_ID = "content_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_ALBUM_NAME = "album_name";
	public static final String KEY_ARTIST_NAME = "artist_name";
	public static final String KEY_IMAGE_URL = "image";
	public static final String KEY_BIG_IMAGE_URL = "big_image";
	public static final String KEY_IMAGES = "images";
	public static final String KEY_ALBUM_ID = "album_id";

	// public static final String ID = "id";
	// public static final String NAME = "name";
	public static final String TXT_NO_ALBUM = "";

	@Expose
	@SerializedName(KEY_CONTENT_ID)
	private final long id;
	@Expose
	@SerializedName(KEY_TITLE)
	private String title;
	@Expose
	@SerializedName(KEY_ALBUM_NAME)
	private String albumName;
	@Expose
	@SerializedName(KEY_ARTIST_NAME)
	private final String artistName;
	@Expose
	@SerializedName(KEY_IMAGE_URL)
	private final String imageUrl;
	@Expose
	@SerializedName(KEY_BIG_IMAGE_URL)
	private final String bigImageUrl;
	@Expose
	@SerializedName(KEY_IMAGES)
	protected Map<String, List<String>> imagesUrlArray;

	private boolean isCached = false;
	private String mediaHandle = null;

	@Expose
	@SerializedName("delivery_id")
	private long deliveryId = -1;
	private boolean doNotCache = true;
	private boolean isFavorite = false;

	@Expose
	@SerializedName(KEY_ALBUM_ID)
	private long albumId = 0;

	public MediaTrackDetails details;

	public long getAlbumId() {
		if (albumId != 0)
			return albumId;
		else if (details != null && details.getAlbumId() != 0)
			return details.getAlbumId();
		else
			return 0;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	/*
	 * Timestamp pointining for the last time this track has updated his media
	 * handle, we use this time stamp as the device's time stamp to compare if
	 * it's the time to recall Media Handle's create operation.
	 */
	private long currentPrefetchTimestamp = -1;

	private Object tag;

	public Track() {
		this(0, "", "", "", "", "", null, 0);
	}

	public Track(long id, String title, String albumName, String artistName,
			String imageUrl, String bigImageUrl,
			Map<String, List<String>> imagesUrlArray, long albumId) {
		this.id = id;
		this.title = title;
		this.albumName = albumName;
		this.artistName = artistName;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
		this.imagesUrlArray = imagesUrlArray;
		setAlbumId(albumId);
	}

	// getters.

	public long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}
	public String getAlbumName() {
		// if (TextUtils.isEmpty(albumName))
		// return TXT_NO_ALBUM;
		return albumName;
	}

	public String getArtistName() {
		if (TextUtils.isEmpty(artistName) && tag != null
				&& tag instanceof MediaItem) {
			return ((MediaItem) tag).getTitle();
		}
		return artistName;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getBigImageUrl() {
		return bigImageUrl;
	}

	public synchronized boolean isCached() {
		return isCached;
	}

	public synchronized String getMediaHandle() {
		return mediaHandle;
	}

	public synchronized long getDeliveryId() {
		return deliveryId;
	}

	public synchronized boolean isDoNotCache() {
		return doNotCache;
	}

	public synchronized long getCurrentPrefetchTimestamp() {
		return currentPrefetchTimestamp;
	}

	public Object getTag() {
		return tag;
	}

	// setters.

	public synchronized void setCached(boolean isCached) {
		this.isCached = isCached;
	}

	public synchronized void setMediaHandle(String mediaHandle) {
		this.mediaHandle = mediaHandle;
	}

	public synchronized void setDeliveryId(long deliveryId) {

		this.deliveryId = deliveryId;
	}

	public synchronized void setDoNotCache(boolean doNotCache) {
		this.doNotCache = doNotCache;
	}

	public synchronized void setCurrentPrefetchTimestamp(
			long currentPrefetchTimestamp) {
		this.currentPrefetchTimestamp = currentPrefetchTimestamp;
	}

	public void setTag(Object tag) {
		this.tag = tag;
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

	public Track newCopy() {
		Track track = new Track(id, TextUtils.isEmpty(title) ? "" : new String(
				title), TextUtils.isEmpty(albumName) ? "" : new String(
				albumName), TextUtils.isEmpty(artistName) ? "" : new String(
				artistName), TextUtils.isEmpty(imageUrl) ? "" : new String(
				imageUrl), TextUtils.isEmpty(bigImageUrl) ? "" : new String(
				bigImageUrl), (this.imagesUrlArray == null) ? null
				: this.imagesUrlArray, albumId);
		track.setCached(isCached);
		track.setMediaHandle((TextUtils.isEmpty(mediaHandle) ? null
				: new String(mediaHandle)));
		track.setDeliveryId(deliveryId);
		track.setDoNotCache(doNotCache);
		track.setCurrentPrefetchTimestamp(currentPrefetchTimestamp);
		// track.setAlbumId(albumId);

		return track;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Track)) {
			return false;
		}

		Track other = (Track) o;

		if (this.id != other.getId()) {
			return false;
		}

		return true;
	}

	public boolean isFavorite() {
		return isFavorite;
	}

	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	public static final int STREAMABLE_STATE = 0;
	public static final int CACHED_STATE = 1;
	public static final int LOCKED_STATE = 2;
	public static final int LOCAL_STATE = 3;

	private boolean doNotRetry = false;
	private int trackState;
	private boolean isEncrypted;

	/**
	 * @param doNotRetry
	 *            the doNotRetry to set
	 */
	public void setDoNotRetry(boolean doNotRetry) {
		this.doNotRetry = doNotRetry;
	}

	/**
	 * @return the doNotRetry
	 */
	public boolean isDoNotRetry() {
		return doNotRetry;
	}

	public void setCachedNew(boolean isCached) {
		if (isCached)
			setTrackState(CACHED_STATE);
		else
			setTrackState(STREAMABLE_STATE);
	}

	public void setCached(boolean isCached, boolean isEncrypted) {
		if (isCached)
			setTrackState(CACHED_STATE);
		else
			setTrackState(STREAMABLE_STATE);
		this.isEncrypted = isEncrypted;

	}

	public void setEncrypt(boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}

	public void setTrackState(int trackState) {
		this.trackState = trackState;
	}

	public int getTrackState() {
		return trackState;
	}

	private int trackNumber;

	public int getTrackNumber() {
		if (trackNumber == 0) {
			// DatabaseManager mDatabaseManager =
			// HungamaApplication.getDatabaseManager();
			// trackNumber = mDatabaseManager.getTrackNumberByTrackId(id);
		}
		return trackNumber;
	}

	public byte[] getOwnerBlob() {
		return new byte[0];// ownerBlob == null ? new byte[0] : ownerBlob;
	}

	private int lastPlayed;

	public void setLastPlayed(int lastPlayed) {
		this.lastPlayed = lastPlayed;
	}

	public int getLastPlayed() {
		return lastPlayed;
	}

	private int progress = -1;

	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public boolean isCachedNew() {
		return getTrackState() == CACHED_STATE ? true : false;
	}

	public boolean isLocked() {
		return getTrackState() == LOCKED_STATE ? true : false;
	}

	public boolean isLocal() {
		return getTrackState() == LOCAL_STATE ? true : false;
	}
}
