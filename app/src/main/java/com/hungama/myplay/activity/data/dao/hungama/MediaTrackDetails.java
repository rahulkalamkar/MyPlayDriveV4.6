package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.JsonUtils;

/**
 * Represents an aggregation of details for track / song {@link MediaItem} kind
 * from Hungama that contains list of tracks.
 */
public class MediaTrackDetails implements Serializable {

	public static final String KEY_IMAGES = "images";

	@Expose
	@SerializedName("content_id")
	private final long id;
	@Expose
	@SerializedName("album_id")
	private final long albumId;
	@Expose
	@SerializedName("music_album_id")
	private final long music_album_id;

	@Expose
	@SerializedName("album_name")
	private final String albumName;
	@Expose
	@SerializedName("title")
	private final String title;
	@Expose
	@SerializedName("image")
	private final String imageUrl;
	@Expose
	@SerializedName("big_image")
	private final String bigImageUrl;
	@Expose
	@SerializedName("relyear")
	private final String releaseYear;
	@Expose
	@SerializedName("genre")
	private final String genre;
	@Expose
	@SerializedName("language")
	private final String language;
	@Expose
	@SerializedName("mood")
	private final String mood;
	@Expose
	@SerializedName("music_director")
	private final String musicDirector;
	@Expose
	@SerializedName("singers")
	private final String singers;
	@Expose
	@SerializedName("lyricist")
	private final String lyricist;
	@Expose
	@SerializedName("cast")
	private final String cast;
	@Expose
	@SerializedName("has_lyrics")
	private final int hasLyrics;
	@Expose
	@SerializedName("has_trivia")
	private final int hasTrivia;
	@Expose
	@SerializedName("has_video")
	private final int hasVideo;
	@Expose
	@SerializedName("video_id")
	private final long videoId;
	@Expose
	@SerializedName("comments_count")
	private final int numOfComments;
	@Expose
	@SerializedName("fav_count")
	private int numOfFav;
	@Expose
	@SerializedName("plays_count")
	private final int numOfPlays;
	@Expose
	@SerializedName("user_fav")
	private int isFavorite;
	@Expose
	@SerializedName("has_download")
	private final int hasDownload;
	@Expose
	@SerializedName("label")
	private final String label;
	@Expose
	@SerializedName("intl_content")
	private final int intl_content;
	@Expose
	@SerializedName(KEY_IMAGES)
	protected Map<String, List<String>> imagesUrlArray;

	public MediaTrackDetails(long id, long albumId, String albumName,
			String title, String imageUrl, String bigImageUrl,
			String releaseYear, String genre, String language, String mood,
			String musicDirector, String singers, String lyricist, String cast,
			int hasLyrics, int hasTrivia, int hasVideo, long videoId,
			int numOfComments, int numOfFav, int numOfPlays, int isFavorite,
			int hasDownload, long music_album_id, String label,
			int intl_content, Map<String, List<String>> imagesUrlArray) {

		this.id = id;
		this.albumId = albumId;
		this.albumName = albumName;
		this.title = title;
		this.imageUrl = imageUrl;
		this.bigImageUrl = bigImageUrl;
		this.releaseYear = releaseYear;
		this.genre = genre;
		this.language = language;
		this.mood = mood;
		this.musicDirector = musicDirector;
		this.singers = singers;
		this.lyricist = lyricist;
		this.cast = cast;
		this.hasLyrics = hasLyrics;
		this.hasTrivia = hasTrivia;
		this.hasVideo = hasVideo;
		this.videoId = videoId;
		this.numOfComments = numOfComments;
		this.numOfFav = numOfFav;
		this.numOfPlays = numOfPlays;
		this.isFavorite = isFavorite;
		this.hasDownload = hasDownload;
		this.music_album_id = music_album_id;
		this.label = label;
		this.intl_content = intl_content;
		this.imagesUrlArray = imagesUrlArray;
	}

	public long getId() {
		return id;
	}

	public long getAlbumId() {
		return albumId;
	}

	public long getMusicAlbumId() {
		if (music_album_id <= 0)
			return 2405464;
		else
			return music_album_id;
	}

	public String getAlbumName() {
		return albumName;
	}

	public String getTitle() {
		return title;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public String getBigImageUrl() {
		return bigImageUrl;
	}

	public String getReleaseYear() {
		return releaseYear;
	}

	public String getGenre() {
		return genre;
	}

	public String getLanguage() {
		return language;
	}

	public String getMood() {
		return mood;
	}

	public String getMusicDirector() {
		return musicDirector;
	}

	public String getSingers() {
		return singers;
	}

	public String getLyricist() {
		return lyricist;
	}

	public String getCast() {
		return cast;
	}

	public boolean hasLyrics() {
		if (hasLyrics > 0) {
			return true;
		}
		return false;
	}

	public boolean hasTrivia() {
		if (hasTrivia > 0) {
			return true;
		}
		return false;
	}

	public boolean hasVideo() {
		if (hasVideo > 0) {
			return true;
		}
		return false;
	}

	public long getVideoId() {
		return videoId;
	}

	public int getNumOfComments() {
		return numOfComments;
	}

	public int getNumOfFav() {
		return numOfFav;
	}

	public int getNumOfPlays() {
		return numOfPlays;
	}

	public boolean IsFavorite() {
		if (isFavorite > 0) {
			return true;
		}
		return false;
	}

	public void setIsFavorite(boolean setIsFavorite) {
		if (setIsFavorite) {
			this.isFavorite = 1;
		} else {
			this.isFavorite = 0;
		}
	}

	public void setNumOfFav(int numberOfFavorites) {
		this.numOfFav = numberOfFavorites;
	}

	public boolean hasDownload() {
		if (hasDownload > 0) {
			return true;
		}
		return false;
	}

	public String getLabel() {
		return label;
	}

	public int getIntl_content() {
		return intl_content;
	}

	public String getImagesUrlArray() {
		try {
			return JsonUtils.mapToJson(imagesUrlArray).toString();
			// return new JSONObject((Map<String,List<String>>)
			// imagesUrlArray).toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, List<String>> getImages() {
		return imagesUrlArray;
	}

	public void setImagesUrlArray(Map<String, List<String>> images) {
		imagesUrlArray = images;
	}
}
