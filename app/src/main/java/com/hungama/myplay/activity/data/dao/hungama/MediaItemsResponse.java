package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * DownloadResponse a response for all types of download flow web service call.
 */
public class MediaItemsResponse implements Serializable {

	public static final String KEY_TOTAL = "total";
	public static final String KEY_START = "start";
	public static final String KEY_LENGTH = "length";
	public static final String KEY_CONTENT = "content";

	@Expose
	@SerializedName(KEY_TOTAL)
	private final int total;
	@Expose
	@SerializedName(KEY_START)
	private final int start;
	@Expose
	@SerializedName(KEY_LENGTH)
	private final int length;
	@Expose
	@SerializedName(KEY_CONTENT)
	private final List<MediaItem> content;

	public MediaItemsResponse(int total, int start, int length,
			List<MediaItem> content) {
		this.total = total;
		this.start = start;
		this.length = length;
		this.content = content;
	}

	public int getTotal() {
		return total;
	}

	public int getStart() {
		return start;
	}

	public int getLength() {
		return length;
	}

	public List<MediaItem> getContent() {
		return content;
	}

}