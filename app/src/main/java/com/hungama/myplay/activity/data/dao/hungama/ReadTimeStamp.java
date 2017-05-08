package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * DownloadResponse a response for all types of download flow web service call.
 */
public class ReadTimeStamp implements Serializable {

	public static final String KEY_Time = "last_modified";

	@Expose
	@SerializedName(KEY_Time)
	private final String last_modified;

	// @Expose
	// @SerializedName(KEY_TimeStamp)
	// private final String last_modified;

	public ReadTimeStamp(String last_modified) {
		this.last_modified = last_modified;
	}

	public String getTimeStamp() {
		return last_modified;
	}

	// public String getTimeStamp(){
	// return last_modified;
	// }

}
