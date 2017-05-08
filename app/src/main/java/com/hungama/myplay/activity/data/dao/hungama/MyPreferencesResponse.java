package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.Utils;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class MyPreferencesResponse implements Serializable {

	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_MY_CATEGORIES = "preference";
	public static final String KEY_CATEGORY_NAME = "category_name";
	public static final String KEY_GENER_NAME = "genre_name";

	@SerializedName(KEY_CODE)
	private int code;

	@SerializedName(KEY_MESSAGE)
	private final String message;

	@SerializedName(KEY_DISPLAY)
	private final int display;
	@SerializedName(KEY_MY_CATEGORIES)
	private final List<MyCategory> mycategories;

	@SerializedName(KEY_CATEGORY_NAME)
	private final String categoryName;

	@SerializedName(KEY_GENER_NAME)
	private final String generName;

	public MyPreferencesResponse(int code, String message, int display,
			List<MyCategory> mycategories, String categoryName,
			String genereName) {
		this.code = code;
		this.message = message;
		this.display = display;
		this.mycategories = mycategories;
		this.categoryName = categoryName;
		this.generName = genereName;
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

	public List<MyCategory> getMycategories() {
		return mycategories;
	}

	public int getChildCount() {
		if (!Utils.isListEmpty(mycategories)) {
			return mycategories.size();
		}
		return 0;
	}

	public String getCategoryName() {
		return categoryName.replaceAll("_", " ");
	}

	public String getGenerName() {
		return generName.replaceAll("_", " ");
	}
}
