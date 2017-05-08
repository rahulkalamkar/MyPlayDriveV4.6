package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class LanguageSaveResponse implements Serializable {
	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";

	@SerializedName(KEY_CODE)
	private int code;

	@SerializedName(KEY_MESSAGE)
	private final String message;

	@SerializedName(KEY_DISPLAY)
	private final int display;

	public LanguageSaveResponse(int code, String message, int display,
			int pointsEarned, String badgeUrl) {
		this.code = code;
		this.message = message;
		this.display = display;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public int getDisplay() {
		return display;
	}

}
