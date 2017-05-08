package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * DownloadResponse a response for all types of download flow web service call.
 */
public class VideoStreamingResponseCatalog implements Serializable {

	// public static final String KEY_CATALOG = "catalog";
	public static final String KEY_CATALOG = "response";
	public static final String KEY_TimeStamp = "last_modified";

	@Expose
	@SerializedName(KEY_CATALOG)
	private final Video catalog;

	@Expose
	@SerializedName(KEY_TimeStamp)
	private final String last_modified;

	public VideoStreamingResponseCatalog(Video catalog, String last_modified) {
		this.catalog = catalog;
		this.last_modified = last_modified;
	}

	public Video getCatalog() {
		return catalog;
	}

	public String getTimeStamp() {
		return last_modified;
	}

}
