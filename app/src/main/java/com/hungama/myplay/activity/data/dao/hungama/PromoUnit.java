package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.JsonUtils;

public class PromoUnit implements Serializable {

	@Expose
	@SerializedName("promo_id")
	private final int promo_id;

	@Expose
	@SerializedName("promo_name")
	private final String promo_name;

	@Expose
	@SerializedName("images")
	private final Map<String, List<String>> images;

	@Expose
	@SerializedName("landing_url")
	private final String landing_url;

	@Expose
	@SerializedName("show_profile")
	private final List<String> show_profile;

	@Expose
	@SerializedName("show_category")
	private final List<String> show_category;

	@Expose
	@SerializedName("show_language")
	private final List<String> show_language;

	public PromoUnit(int promo_id, String promo_name, Map<String, List<String>> images,
			String landing_url, List<String> show_profile,
			List<String> show_category, List<String> show_language) {
		super();
		this.promo_id = promo_id;
		this.promo_name = promo_name;
		this.images = images;
		this.landing_url = landing_url;
		this.show_profile = show_profile;
		this.show_category = show_category;
		this.show_language = show_language;
	}

	public int getPromo_id() {
		try {
			return (promo_id == 0 ? 1 : promo_id);
		} catch (Exception e) {
			return 1;
		}
	}

	public String getPromo_name() {
		return promo_name;
	}

	public Map<String, List<String>> getImages() {
		return images;
	}

	public String getLanding_url() {
		return landing_url;
	}

	public List<String> getShow_profile() {
		return show_profile;
	}

	public List<String> getShow_category() {
		return show_category;
	}

	public List<String> getShow_language() {
		return show_language;
	}

	public String getImagesUrlArray() {
		try {
			return JsonUtils.mapToJson(images).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
