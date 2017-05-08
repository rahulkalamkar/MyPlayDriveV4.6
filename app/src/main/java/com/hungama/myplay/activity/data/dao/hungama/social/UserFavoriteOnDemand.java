package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;

public class UserFavoriteOnDemand {

	public static final String KEY_SONGS_COUNT = "ondemandradio_count";
	public static final String KEY_SONGS = "data";

	@SerializedName(KEY_SONGS_COUNT)
	public final int artistsCount;
	@SerializedName(KEY_SONGS)
	public final List<MediaItem> artists;

	public UserFavoriteOnDemand(int songsCount, List<MediaItem> songs) {
		this.artistsCount = songsCount;
		this.artists = songs;
	}
}
