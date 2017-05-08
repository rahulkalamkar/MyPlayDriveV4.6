package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Genre object given from Hungama server to filter {@link MediaItem}s.
 */
public class MessageFromResponse implements Serializable {

	@Expose
	@SerializedName("show_message")
	private final long show_message;
	@Expose
	@SerializedName("message_text")
	private final String message_text;

	public MessageFromResponse(long show_message, String message_text) {
		this.show_message = show_message;
		this.message_text = message_text;
	}

	public long getShowMessage() {
		return show_message;
	}

	public String getMessageText() {
		return message_text;
	}
}
