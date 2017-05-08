package com.hungama.myplay.activity.data.dao.hungama;

import com.hungama.myplay.activity.operations.hungama.HungamaOperation;

import java.io.Serializable;

/**
 * Response of the {@link HungamaOperation} for getting internal protocol
 * messaging.
 */
public class LanguageResponse implements Serializable {

	private int id;
	private String language;
	private String display_text;

	public LanguageResponse() {
	}

	public int getId() {
		return id;
	}

	public String getLanguage() {
		return language;
	}

	public String getDisplayText() {
		return display_text;
	}
}
