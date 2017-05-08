package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents an aggregation of details for Album or Playlist {@link MediaItem}
 * kind from Hungama that contains list of tracks.
 */
public class MusicCategoryGenre implements Serializable {

	@Expose
	@SerializedName("category")
	private final String category;
	@Expose
	@SerializedName("genre")
	private final List<String> genre;

	public MusicCategoryGenre(String category, List<String> genre) {
		this.category = category;
		this.genre = genre;
	}

	public String getCategory() {
		return category;
	}

	public List<String> getGenre() {
		return genre;
	}
}
