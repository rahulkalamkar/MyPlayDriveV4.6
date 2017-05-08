package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * DownloadResponse a response for all types of download flow web service call.
 */
public class MediaItemsResponseCatalog implements Serializable {

	public static final String KEY_CATALOG = "response";
	public static final String KEY_TimeStamp = "last_modified";
	public static final String KEY_MESSAGE = "message";

	@Expose
	@SerializedName(KEY_CATALOG)
	private final MediaItemsResponse catalog;

	@Expose
	@SerializedName(KEY_TimeStamp)
	private final String last_modified;

	@Expose
	@SerializedName(KEY_MESSAGE)
	private final MessageFromResponse message;

	public MediaItemsResponseCatalog(MediaItemsResponse catalog,
			String last_modified, MessageFromResponse message) {
		this.catalog = catalog;
		this.last_modified = last_modified;
		this.message = message;
	}

	public MediaItemsResponse getCatalog() {
		return catalog;
	}

	public String getTimeStamp() {
		return last_modified;
	}

	public MessageFromResponse getMessage() {
		return message;
	}
}
