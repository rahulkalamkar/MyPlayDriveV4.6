package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents a single song to be played from a {@link MediaItem}.
 */
public class Video implements Serializable {

	// public static final String VIDEO_URL = "video_url";
	public static final String VIDEO_URL = "url";
	public static final String SUBSCRIPTION_STATUS = "subscription_status";
	public static final String TYPE = "type";

	@Expose
	@SerializedName(VIDEO_URL)
	private final String videoUrl;
	@Expose
	@SerializedName(SUBSCRIPTION_STATUS)
	private final int subscriptionStatus;
	@Expose
	@SerializedName(TYPE)
	private final String type;
	@Expose
	@SerializedName("comments_count")
	private final int numOfComments;
	@Expose
	@SerializedName("fav_count")
	private final int numOfFav;
	@Expose
	@SerializedName("plays_count")
	private final int numOfPlays;
	@Expose
	@SerializedName("user_fav")
	private final int isFavorite;

	private boolean isCached;
	private String mediaHandle;
	private int limitDuration;
	private long deliveryId;
	private boolean doNotCache;

	public Video(String video_url, int subscription_status, String type,
			int numOfComments, int numOfFav, int numOfPlays, int isFavorite) {
		this.videoUrl = video_url;
		this.subscriptionStatus = subscription_status;
		this.type = type;
		this.numOfComments = numOfComments;
		this.numOfFav = numOfFav;
		this.numOfPlays = numOfPlays;
		this.isFavorite = isFavorite;
	}

	// getters.

	public String getVideoUrl() {
		//return "http://movieshls001-a.erosnow.com/hls/movie/7/1005087/fulllength/6115431/1005087_6115431_IPAD_SD_NEW_multi.m3u8";
		//Log.i("videoUrl","getVideoUrl():"+videoUrl);
		//return "http://cp292877.hdsods.hungama.com/i/1c/af/1cafc714-255a-494e-8a34-118cedc715c8/FF-2015-00000735_,1600k,400k,100k,1000k,750k,.mp4.csmil/master.m3u8?hdnea=exp=1443090804~acl=/i/1c/af/1cafc714-255a-494e-8a34-118cedc715c8/FF-2015-00000735_,1600k,400k,100k,1000k,750k,.mp4.csmil/*~hmac=50b8fc2063d240b1a09ea346583dee9e645634aa42987fea723c22f178f08f78";
		return videoUrl;
	}

	public int getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public String getType() {
		return type;
	}

	public synchronized boolean isCached() {
		return isCached;
	}

	public synchronized String getMediaHandle() {
		return mediaHandle;
	}

	public synchronized int getLimitDuration() {
		return limitDuration;
	}

	public synchronized long getDeliveryId() {
		return deliveryId;
	}

	public synchronized boolean isDoNotCache() {
		return doNotCache;
	}

	// setters.

	public synchronized void setCached(boolean isCached) {
		this.isCached = isCached;
	}

	public synchronized void setMediaHandle(String mediaHandle) {
		this.mediaHandle = mediaHandle;
	}

	public synchronized void setLimitDuration(int limitDuration) {
		this.limitDuration = limitDuration;
	}

	public synchronized void setDeliveryId(long deliveryId) {
		this.deliveryId = deliveryId;
	}

	public synchronized void setDoNotCache(boolean doNotCache) {
		this.doNotCache = doNotCache;
	}

	public Video newCopy() {
		Video video = new Video(videoUrl, subscriptionStatus, type,
				numOfComments, numOfFav, numOfPlays, isFavorite);
		video.setCached(isCached);
		video.setMediaHandle((TextUtils.isEmpty(mediaHandle) ? null
				: mediaHandle));
		video.setLimitDuration(limitDuration);
		video.setDeliveryId(deliveryId);
		video.setDoNotCache(doNotCache);

		return video;
	}

	public int getNumOfComments() {
		return numOfComments;
	}

	public int getNumOfFav() {
		return numOfFav;
	}

	public int getNumOfPlays() {
		return numOfPlays;
	}

	public boolean IsFavorite() {
		if (isFavorite > 0) {
			return true;
		}
		return false;
	}

}
