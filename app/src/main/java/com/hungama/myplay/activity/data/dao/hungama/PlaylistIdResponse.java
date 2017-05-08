package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * DownloadResponse a response for all types of download flow web service call.
 */
public class PlaylistIdResponse implements Serializable {

	public static final String KEY_PLAYLIST_ID = "playlist_id";
	public static final String KEY_PLAYLIST_NAME = "playlist_name";
	public static final String KEY_UNIQUE_KEY = "uniquekey";

	@Expose
	@SerializedName(KEY_PLAYLIST_ID)
	private final long playlist_id;

	@Expose
	@SerializedName(KEY_PLAYLIST_NAME)
	private final String playlist_name;

	@Expose
	@SerializedName(KEY_UNIQUE_KEY)
	private final String uniquekey;

	public PlaylistIdResponse(long playlist_id, String playlist_name,
			String uniquekey) {
		this.playlist_id = playlist_id;
		this.playlist_name = playlist_name;
		this.uniquekey = uniquekey;
	}

	public long getPlaylistId() {
		return playlist_id;
	}

	public String getPlaylist_name() {
		return playlist_name;
	}

	public String getUniquekey() {
		return uniquekey;
	}
}
