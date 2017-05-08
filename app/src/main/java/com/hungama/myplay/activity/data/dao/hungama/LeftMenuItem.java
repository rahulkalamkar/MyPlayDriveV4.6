package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Represents an aggregation of details for Album or Playlist {@link MediaItem}
 * kind from Hungama that contains list of tracks.
 */
public class LeftMenuItem implements Serializable {

	@Expose
	@SerializedName("main_menu")
	private final String main_menu;
	@Expose
	@SerializedName("menu_title")
	private final String menu_title;
	@Expose
	@SerializedName("spacing_edge")
	private final int spacing_edge;
	@Expose
	@SerializedName("spacing_icon")
	private final int spacing_icon;
	@Expose
	@SerializedName("link_type")
	private final String link_type;
	@Expose
	@SerializedName("inapp_action")
	private final String inapp_action;
	@Expose
	@SerializedName("images")
	private final LeftMenuImages images;
	@Expose
	@SerializedName("sub_menu")
	private final List<LeftMenuItem> sub_menu;

	@Expose
	@SerializedName("html_url")
	private final String html_url;

	@Expose
	@SerializedName("popup_message")
	private final String popup_message;

	@Expose
	@SerializedName("extra_data")
	private final LeftMenuExtraData extra_data;

	public LeftMenuItem(String main_menu, String menu_title, int spacing_edge,
			int spacing_icon, String link_type, String inapp_action,
			LeftMenuImages images, List<LeftMenuItem> sub_menu,
			String html_url, String popup_message, LeftMenuExtraData extra_data) {
		this.main_menu = main_menu;
		this.menu_title = menu_title;
		this.spacing_edge = spacing_edge;
		this.spacing_icon = spacing_icon;
		this.link_type = link_type;
		this.inapp_action = inapp_action;
		this.images = images;
		this.sub_menu = sub_menu;
		this.html_url = html_url;
		this.popup_message = popup_message;
		this.extra_data = extra_data;
	}

	public String getMainMenu() {
		return main_menu;
	}

	public String getPopUpMessage() {
		return popup_message;
	}

	public String getHtmlURL() {
		return html_url;
	}

	public String getMenu_title() {
		return menu_title;
	}

	public int getSpacing_edge() {
		return spacing_edge;
	}

	public int getSpacing_icon() {
		return spacing_icon;
	}

	public String getLink_type() {
		return link_type;
	}

	public String getInapp_action() {
		return inapp_action;
	}

	public LeftMenuImages getImages() {
		return images;
	}

	public List<LeftMenuItem> getSubMenu() {
		return sub_menu;
	}

	public LeftMenuExtraData getExtra_data() {
		return extra_data;
	}
}
