package com.hungama.myplay.activity.data;

import android.text.TextUtils;

import com.hungama.myplay.activity.util.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class ImagesManager {

	public static final int HOME_MUSIC_TILE = 0;
	public static final int HOME_VIDEO_TILE = 1;
	public static final int MUSIC_ART_SMALL = 2;
	public static final int MUSIC_ART_BIG = 3;
	// public static final int ARTIST_ART_BIG = 4;
	public static final int RADIO_LIST_ART = 5;
	public static final int PROMO_UNIT_SIZE = 6;

	// private static final String[] arrayHomeMusicTileSize = { "120x120",
	// "175x175", "300x300", "400x400" };
	// // private static final String[] arrayHomeMusicTileSize = { "168x168",
	// // "252x252", "200x200", "504x504" };
	//
	// private static final String[] arrayHomeVideoTileSize = { "200x200",
	// "300x300", "400x400", "500x500" };
	// // private static final String[] arrayMusicArtSmallSize = { "80x80",
	// // "100x100", "200x200", "300x300" };
	// private static final String[] arrayMusicArtSmallSize = { "100x100",
	// "200x200", "300x300", "400x400" };
	// private static final String[] arrayMusicArtBigSize = { "200x200",
	// "300x300", "400x400", "800x800" };
	// private static final String[] arrayArtistArtBigSize = { "200x200",
	// "300x300", "400x400", "550x550" };
	// private static final String[] arrayRadioListArtSize = { "50x50",
	// "100x100", "150x150", "200x200" };
	// private static final String[] arrayPromoUnitSize = { "344x86",
	// "516x129", "688x172", "1032x259" };

	private static final String[] arrayHomeMusicTileSize = { "150x150",
			"200x200", "300x300", "500x500" };
	// private static final String[] arrayHomeMusicTileSize = { "168x168",
	// "252x252", "200x200", "504x504" };

	private static final String[] arrayHomeVideoTileSize = { "320x180",
			"350x197", "525x296", "700x394" };
	// private static final String[] arrayMusicArtSmallSize = { "80x80",
	// "100x100", "200x200", "300x300" };
	private static final String[] arrayMusicArtSmallSize = { "150x150",
			"200x200", "300x300", "500x500" };
	private static final String[] arrayMusicArtBigSize = { "300x300",
			"400x400", "500x500", "500x500" };
	private static final String[] arrayArtistArtBigSize = { "175x175",
			"200x200", "300x300", "400x400" };
	private static final String[] arrayRadioListArtSize = { "100x100",
			"100x100", "100x100", "100x100" };
	private static final String[] arrayPromoUnitSize = { "344x86", "516x129",
			"688x172", "1032x259" };

	private static final String[][] arrayImageSizes = { arrayHomeMusicTileSize,
			arrayHomeVideoTileSize, arrayMusicArtSmallSize,
			arrayMusicArtBigSize, arrayArtistArtBigSize, arrayRadioListArtSize,
			arrayPromoUnitSize };

	public static String getImageSize(int imgPlace, String displayDensity) {
		if (displayDensity.equalsIgnoreCase("ldpi")) {
			return arrayImageSizes[imgPlace][0];
		} else if (displayDensity.equalsIgnoreCase("mdpi")) {
			return arrayImageSizes[imgPlace][1];
		} else if (displayDensity.equalsIgnoreCase("hdpi")) {
			return arrayImageSizes[imgPlace][2];
		} else if (displayDensity.equalsIgnoreCase("xdpi")) {
			return arrayImageSizes[imgPlace][3];
		} else {
			return arrayImageSizes[imgPlace][1];
		}
	}

	// public static Iterator<String> getImagesKeys(String imagesUrlArray) {
	// try {
	// if (!TextUtils.isEmpty(imagesUrlArray)) {
	// JSONObject jsonImages = new JSONObject(imagesUrlArray);
	// return jsonImages.keys();
	// }
	// } catch (Exception e) {
	// // Logger.printStackTrace(e);
	// }
	// return null;
	// }

	public static String[] getImagesUrlArray(String imagesUrlArray,
			int imgPlace, String displayDensity) {
		String[] urls = new String[0];
		try {
			if (!TextUtils.isEmpty(imagesUrlArray)) {
				JSONObject jsonImages = new JSONObject(imagesUrlArray);
				JSONArray images = jsonImages.getJSONArray("image_"
						+ getImageSize(imgPlace, displayDensity));
				urls = new String[images.length()];
				for (int i = 0; i < images.length(); i++) {
					urls[i] = images.getString(i);
				}
			}
		} catch (Exception e) {
			// Logger.printStackTrace(e);
			urls = new String[0];
		}
		return urls;
	}

	public static String[] getPlaylistTileImagesUrlArray(String imagesUrlArray,
											 int imgPlace, String displayDensity) {
		String[] urls = new String[0];
		try {
			if (!TextUtils.isEmpty(imagesUrlArray)) {
				JSONObject jsonImages = new JSONObject(imagesUrlArray);
				JSONArray images = null;
				if(Logger.hardcodePlaylistImage)
					images = jsonImages.getJSONArray("image_100x100");
				if(images==null || images.length()<6)
					images = jsonImages.getJSONArray("image_"
							+ getImageSize(imgPlace, displayDensity));
				urls = new String[images.length()];
				for (int i = 0; i < images.length(); i++) {
					urls[i] = images.getString(i);
				}
			}
		} catch (Exception e) {
			// Logger.printStackTrace(e);
			urls = new String[0];
		}
		return urls;
	}

	public static String[] getImagesUrlArray(String imagesUrlArray, int imgPlace) {
		String[] urls = new String[0];
		String displayDensity = DataManager.getDisplayDensityLabel();
		try {
			if (!TextUtils.isEmpty(imagesUrlArray)) {
				JSONObject jsonImages = new JSONObject(imagesUrlArray);
				JSONArray images = jsonImages.getJSONArray("image_"
						+ getImageSize(imgPlace, displayDensity));
				urls = new String[images.length()];
				for (int i = 0; i < images.length(); i++) {
					urls[i] = images.getString(i);
				}
			}
		} catch (Exception e) {
			// Logger.printStackTrace(e);
			urls = new String[0];
		}
		return urls;
	}

	public static String getMusicArtSmallImageUrl(String imageArray) {
		try {
			String[] images = ImagesManager.getImagesUrlArray(imageArray,
					ImagesManager.MUSIC_ART_SMALL);
			if (images != null && images.length > 0)
				return images[0];
			else {
				images = ImagesManager.getImagesUrlArray(imageArray,
						ImagesManager.MUSIC_ART_BIG);
				if (images != null && images.length > 0)
					return images[0];
				else {
					images = ImagesManager.getImagesUrlArray(imageArray,
							ImagesManager.RADIO_LIST_ART);
					if (images != null && images.length > 0)
						return images[0];
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return "";
	}

	public static String getMusicArtBigImageUrl(String imageArray) {
		String[] images = ImagesManager.getImagesUrlArray(imageArray,
				ImagesManager.MUSIC_ART_BIG);
		if (images != null && images.length > 0)
			return images[0];
		else {
			images = ImagesManager.getImagesUrlArray(imageArray,
					ImagesManager.MUSIC_ART_SMALL);
			if (images != null && images.length > 0)
				return images[0];
		}
		return "";
	}

	public static String getRadioArtImageUrl(String imageArray) {
		try {
			String[] images = ImagesManager.getImagesUrlArray(imageArray,
					ImagesManager.MUSIC_ART_SMALL);
			if (images != null && images.length > 0)
				return images[0];
			else {
				images = ImagesManager.getImagesUrlArray(imageArray,
						ImagesManager.RADIO_LIST_ART);
				if (images != null && images.length > 0)
					return images[0];
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return "";
	}

	public static String getRadioListArtImageUrl(String imageArray) {
		String[] images = ImagesManager.getImagesUrlArray(imageArray,
				ImagesManager.RADIO_LIST_ART);
		if (images != null && images.length > 0)
			return images[0];
		return "";
	}
}
