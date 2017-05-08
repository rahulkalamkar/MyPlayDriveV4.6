package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

/**
 * Enumeration definition of different media content types.
 */
public enum MediaContentType implements Serializable {
	MUSIC, VIDEO, RADIO, BADGE;

	public static final MediaContentType getCopy(
			MediaContentType mediaContentType) {

		if (mediaContentType == MUSIC) {
			return MUSIC;
		} else if (mediaContentType == VIDEO) {
			return VIDEO;
		} else if (mediaContentType == RADIO) {
			return RADIO;
		} else if (mediaContentType == BADGE) {
			return BADGE;
		}

		return MUSIC;
	}

	public static final String getMediaKind(MediaContentType mediaContentType) {

		if (mediaContentType == VIDEO) {
			return "video";
		} else
			return "track";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Enum#toString()
	 */
	@Override
	public String toString() {

		return super.toString();
	}

	// public static final MediaContentType getCopy(MediaContentType
	// mediaContentType) {
	//
	// if (mediaContentType == MUSIC) {
	// return MUSIC;
	// } else if (mediaContentType == VIDEO) {
	// return VIDEO;
	// } else if (mediaContentType == RADIO) {
	// return RADIO;
	// } else if (mediaContentType == BADGE) {
	// return BADGE;
	// }
	//
	// return MUSIC;
	// }
}
