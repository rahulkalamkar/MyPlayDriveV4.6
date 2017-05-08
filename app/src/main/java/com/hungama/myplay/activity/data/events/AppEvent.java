package com.hungama.myplay.activity.data.events;

import java.io.Serializable;
import java.util.Map;

public class AppEvent extends Event implements Serializable {

	private final String eventType;
	private final Map<String, Object> extraData;

	public AppEvent(long consumerId, String deviceId, String eventType,
					String timestamp, float latitude, float longitude, Map<String, Object> extraData) {
		super(consumerId, deviceId, false, 0, timestamp,
				latitude, longitude, 0);
		this.eventType = eventType;
		this.extraData = extraData;
	}

	public String getEventType() {
		return eventType;
	}

	public Map<String, Object> getExtraData() {
		return extraData;
	}
}
