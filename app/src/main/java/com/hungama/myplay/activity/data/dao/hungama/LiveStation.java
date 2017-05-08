package com.hungama.myplay.activity.data.dao.hungama;

import android.text.TextUtils;

public class LiveStation extends MediaItem {

	private final String description;
	private final String streamingUrl;
	private String streamingUrl_128, streamingUrl_320;
	private final String imageUrl;
	private final String bigImageUrl;

	public LiveStation(long id, String title, String description,
			String streamingUrl, String imageUrl, String bigImageUrl) {
		super(id, title, null, null, imageUrl, bigImageUrl, MediaType.TRACK
				.toString().toLowerCase(), 0, 0);
		this.description = description;
		this.streamingUrl = streamingUrl;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;

	}

	public String getDescription() {
		return description;
	}

	public String getStreamingUrl() {
		return streamingUrl;
	}

	public LiveStation getCopy() {
		long id = Long.valueOf(this.id);
		String title = TextUtils.isEmpty(this.title) ? null : this.title;
		String description = TextUtils.isEmpty(this.description) ? null
				: this.description;
		String streamingUrl = TextUtils.isEmpty(this.streamingUrl) ? null
				: this.streamingUrl;
		String imageUrl = TextUtils.isEmpty(this.imageUrl) ? null
				: this.imageUrl;
		String bigImageUrl = TextUtils.isEmpty(this.bigImageUrl) ? null
				: this.bigImageUrl;

		LiveStation ls = new LiveStation(id, title, description, streamingUrl,
				imageUrl, bigImageUrl);
		ls.setStreamingUrl_128(this.streamingUrl_128);
		ls.setStreamingUrl_320(this.streamingUrl_320);
		return ls;
	}

	public String getStreamingUrl_128() {
		return streamingUrl_128;
	}

	public void setStreamingUrl_128(String streamingUrl_128) {
		this.streamingUrl_128 = streamingUrl_128;
	}

	public String getStreamingUrl_320() {
		return streamingUrl_320;
	}

	public void setStreamingUrl_320(String streamingUrl_320) {
		this.streamingUrl_320 = streamingUrl_320;
	}
}
