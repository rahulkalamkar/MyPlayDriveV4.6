package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents an aggregation of details for Album or Playlist {@link MediaItem}
 * kind from Hungama that contains list of tracks.
 */
public class LeftMenuResponse implements Serializable {

	@Expose
	@SerializedName("last_modified")
	private final long last_modified;

	@Expose
	@SerializedName("response")
	private final List<LeftMenuItem> leftMenuItems;

	public LeftMenuResponse(List<LeftMenuItem> leftMenuItems, long last_modified) {
		this.leftMenuItems = leftMenuItems;
		this.last_modified = last_modified;
	}

	public List<LeftMenuItem> getLeftMenuItems() {
		return leftMenuItems;
	}

	public long getLast_modified() {
		return last_modified;
	}
}
