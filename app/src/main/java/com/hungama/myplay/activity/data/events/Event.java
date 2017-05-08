package com.hungama.myplay.activity.data.events;

import java.io.Serializable;

/**
 * Abstract event to be sent to the application's server that something has
 * happened.
 */
public class Event implements Serializable {

	protected final long consumerId;
	protected final String deviceId;
	protected final int duration;
	protected final boolean completePlay;

	protected final String timestamp; // UTC time zone.
	protected final float latitude;
	protected final float longitude;
	protected final long Id;

	public Event(long consumerId, String deviceId, boolean completePlay,
			int duration, String timestamp, float latitude, float longitude,
			long mediaId) {
		this.consumerId = consumerId;
		this.deviceId = deviceId;
		this.completePlay = completePlay;
		this.duration = duration;
		this.timestamp = timestamp;
		this.latitude = latitude;
		this.longitude = longitude;
		this.Id = mediaId;
	}

	// getters:

	public long getConsumerId() {
		return consumerId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public int getDuration() {
		return duration;
	}

	public boolean isCompletePlay() {
		return completePlay;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getRegularTimestamp() {
		return timestamp;
	}

	public float getLatitude() {
		return latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public long getMediaId() {
		return Id;
	}

}
