package com.hungama.myplay.activity.data.events;

import java.io.Serializable;

/**
 * Implementation of {@link Event} for logging Playing actions like
 * "play track", "stop track".
 */
public class PlayEvent extends Event implements Serializable {

	public enum PlayingSourceType {
		STREAM, DOWNLOAD, CACHED, CAST/*
								 * , SAVED
								 */
	}

	private final long mediaId;
	private final String mediaKind;
	private PlayingSourceType playingSourceType;
	private final long deliveryId;

	private final int startPosition;
	private final int stopPosition;
	private boolean isPlaylist, isOnDemandRadio, isLiveRadio, isDiscovery, isArtistRadio;
	private String id;
    private String name;
    private String user_playlist_name;
    private String hashTag;

    public String getOndemandname() {
        return ondemandname;
    }

    private String ondemandname;
	private long album_id;

	public long getAlbum_id() {
		return album_id;
	}

	public void setAlbum_id(long album_id) {
		this.album_id = album_id;
	}

	public PlayEvent(long consumerId, String deviceId, long deliveryId,
			boolean completePlay, int duration, String timestamp,
			float latitude, float longitude, long mediaId, String mediaKind,
			PlayingSourceType playingSourceType, int startPosition,
			int stopPosition) {

		// new PlayEvent(consumerId,
		// deviceId,
		// mCurrentTrack.getDeliveryId(),
		// hasCompletePlay /* ? "true" : "false" */,
		// playDuration,
		// mEventStartTimestamp,
		// 0,
		// 0,
		// mCurrentTrack.getId(),
		// "track",
		// playingSourceType,
		// 0,
		// playCurrentPostion);

		super(consumerId, deviceId, completePlay, duration, timestamp,
				latitude, longitude, mediaId);

		this.mediaId = mediaId;
		this.mediaKind = mediaKind;
		this.playingSourceType = playingSourceType;
		this.deliveryId = deliveryId;
		this.startPosition = startPosition;
		this.stopPosition = stopPosition;

		this.isPlaylist = false;
		this.isOnDemandRadio = false;
		this.isLiveRadio = false;
		this.isDiscovery = false;
		this.isArtistRadio = false;
		hashTag = null;
	}

	public PlayEvent(long consumerId, String deviceId, long deliveryId,
			boolean completePlay, String timestamp, float latitude,
			float longitude, long mediaId, String mediaKind,
			PlayingSourceType playingSourceType, int startPosition,
			int stopPosition) {
		super(consumerId, deviceId, completePlay, stopPosition - startPosition,
				timestamp, latitude, longitude, mediaId);

		this.mediaId = mediaId;
		this.mediaKind = mediaKind;
		this.playingSourceType = playingSourceType;
		this.deliveryId = deliveryId;
		this.startPosition = startPosition;
		this.stopPosition = stopPosition;

		this.isPlaylist = false;
		this.isOnDemandRadio = false;
		this.isArtistRadio = false;
		this.isLiveRadio = false;
		this.isDiscovery = false;
		hashTag = null;
	}

	// getters:

	public long getMediaId() {
		return mediaId;
	}

	public String getMediaKind() {
		return mediaKind;
	}

	public long getDeliveryId() {
		return deliveryId;
	}

	public PlayingSourceType getPlayingSourceType() {
		return playingSourceType;
	}

	public void setPlayingSourceType(PlayingSourceType playingSourceType) {
		this.playingSourceType = playingSourceType;
	}

	public boolean isCompletePlay() {
		return completePlay;
	}

	public int getStartPosition() {
		return startPosition;
	}

	public int getStopPosition() {
		return stopPosition;
	}

	public void setPlaylistDetails(String id, String name) {
		isPlaylist = true;
		this.id = id;
		this.name = name;
	}

	public void setOnDemandRadioDetails(String id, String name) {
		isOnDemandRadio = true;
		this.id = id;
        this.ondemandname=name;
	}

	public boolean isFromPlaylist() {
		return isPlaylist;
	}

	public boolean isFromOnDemandRadio() {
		return isOnDemandRadio;
	}

	public void setArtistRadioDetails(String id) {
		isArtistRadio = true;
		this.id = id;
	}

	public boolean isFromArtistRadio() {
		return isArtistRadio;
	}

	public void setLiveRarioDetails(String id, String name) {
		isLiveRadio = true;
		this.id = id;
		this.name = name;
	}

	public boolean isFromLiveRadio() {
		return isLiveRadio;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setUserPlaylistName(String name) {
		this.user_playlist_name = name;
	}

	public String getUserPlaylistName() {
		return user_playlist_name;
	}

	public PlayEvent newInstance() {
		return new PlayEvent(consumerId, deviceId, deliveryId, completePlay,
				duration, timestamp, latitude, longitude, mediaId, mediaKind,
				playingSourceType, startPosition, stopPosition);
	}

	public boolean isDiscovery() {
		return isDiscovery;
	}

	public void setDiscovery(boolean isDiscovery) {
		this.isDiscovery = isDiscovery;
	}

	public String getHashTag() {
		return hashTag;
	}

	public void setHashTag(String hashTag) {
		this.hashTag = hashTag;
	}
}
