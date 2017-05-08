package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Represents an aggregation of details for Album or Playlist {@link MediaItem}
 * kind from Hungama that contains list of tracks.
 */
public class MusicCategoriesResponse implements Serializable {

	@Expose
	@SerializedName("categories")
	private final List<String> categories;
	@Expose
	@SerializedName("genres")
	private final List<MusicCategoryGenre> genres;

	public MusicCategoriesResponse(List<String> categories,
			List<MusicCategoryGenre> genres) {
		this.categories = categories;
		this.genres = genres;
	}

	public List<String> getCategories() {
		return categories;
	}

	public List<MusicCategoryGenre> getGenres() {
		return genres;
	}
}
