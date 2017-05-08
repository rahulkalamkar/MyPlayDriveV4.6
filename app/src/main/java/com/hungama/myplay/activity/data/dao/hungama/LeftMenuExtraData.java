package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

/**
 * Created by hungama2 on 15/9/15.
 */
public class LeftMenuExtraData implements Serializable {

	private final String link_text;
	private final String clickable_text;
	private final String clickable_link;
	private String title;

	public LeftMenuExtraData(String link_text, String clickable_text, String link) {
		this.link_text = link_text;
		this.clickable_text = clickable_text;
		this.clickable_link = link;
		title = null;
	}

	public String getLink_text() {
		return link_text;
	}

	public String getClickable_text() {
		return clickable_text;
	}

	public String getClickable_link() {
		return clickable_link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
