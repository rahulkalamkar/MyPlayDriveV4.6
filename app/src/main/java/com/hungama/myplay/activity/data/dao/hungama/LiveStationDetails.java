/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * @author offerperetz
 *
 */
public class LiveStationDetails implements Serializable {

	@SerializedName("id")
	private long id;

	@SerializedName("track")
	private String track;

	@Expose
	@SerializedName("album id")
	private long albumId = 0;

	public long getAlbumId() {
		if (albumId != 0)
			return albumId;
		else
			return 0;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	/**
	 * @return the track
	 */
	public String getTrack() {
		return track;
	}

	/**
	 * @return the album
	 */
	public String getAlbum() {
		return album;
	}

	/**
	 * @return the duration_left
	 */
	public int getDuration_left() {
		return duration_left;
	}

	/**
	 * @return the nextTrack
	 */
	public LiveStationDetails getNextTrack() {
		return nextTrack;
	}

	@SerializedName("album")
	private String album;

	@SerializedName("duration_left")
	private int duration_left;

	@SerializedName("next_song")
	private LiveStationDetails nextTrack;

	public LiveStationDetails() {
	}

	public long getId() {
		return id;
	}
}
