package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;

/*
 {
 "last_modified": 1429005084,
 "total": 18,
 "start": 1,
 "length": 30,
 "content": [
 {
 "content_id": 1776413,
 "title": "Sunn Raha Hai - Female",
 "album_id": 1768518,
 "album_name": "Aashiqui 2",
 "type": "song"
 }
 ]
 }*/
public class ProfileFavoriteMediaItems {

	public static final String KEY_LAST_MODIFIED = "last_modified";
	public static final String KEY_START_INDEX = "start";
	public static final String KEY_LENGTH = "length";
	public static final String KEY_TOTAL_COUNT = "total";
	public static final String KEY_ALBUMS = "content";

	@SerializedName(KEY_START_INDEX)
	public final int startIndex;
	@SerializedName(KEY_LENGTH)
	public final int length;
	@SerializedName(KEY_TOTAL_COUNT)
	public final int totalCount;
	@SerializedName(KEY_ALBUMS)
	public final List<MediaItem> mediaItems;
	@SerializedName(KEY_LAST_MODIFIED)
	private long timestamp;

	private MediaType mediaType;

	public ProfileFavoriteMediaItems(int startIndex, int length,
			int totalCount, List<MediaItem> mediaItems) {
		this.startIndex = startIndex;
		this.length = length;
		this.totalCount = totalCount;
		this.mediaItems = mediaItems;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public long getTimestamp() {
		return timestamp;
	}

}
