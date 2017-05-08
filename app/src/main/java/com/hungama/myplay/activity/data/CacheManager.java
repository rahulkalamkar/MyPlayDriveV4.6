package com.hungama.myplay.activity.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.DataManager.MoodIcon;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.Genre;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.events.CampaignPlayEvent;
import com.hungama.myplay.activity.data.events.Event;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.playlist.PlaylistRequest;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.images.DiskLruCache;

/**
 * Manages access to all available cached resources.
 */
public class CacheManager {

	private static final String TAG = "CacheManager";

	private static final String FILE_MEDIA_ITEMS = "media_items";
	private static final String FILE_EVENTS = "events";
	private static final String FILE_MOODS = "moods";
	private static final String FILE_PREFERENCES = "preferences";
	private static final String FILE_PREFERENCES_VIDEO = "preferences_video";
	private static final String FILE_CURRENT_PLAN = "current_plan";
	private static final String FILE_CURRENT_PLAN_NEW = "current_plan_new";
	private static final String FILE_PLANS = "plans";
	private static final String FILE_CAMPAIGN_LIST = "campaign_list";
	private static final String FILE_CAMPAIGN = "campaign";
	private static final String FILE_PLAYLIST = "playlist";
	private static final String FILE_PLAYLIST_REQUEST = "playlist_request";
	private static final String FILE_HUNGAMA_TRACK = "hungma_track";
	private static final String FILE_FEEDBACK_SUBJECTS = "feedback_subjects";
	private static final String FILE_APPLICATION_IMAGES = "application_images";
	private static final String FILE_MUSIC_LATEST = "music_latest";
	private static final String FILE_MUSIC_FEATURED = "music_featured";
	private static final String FILE_VIDEO_LATEST = "video_latest";
	private static final String FILE_LIVE_RADIO = "live_radio";
	private static final String FILE_SEARCH_POPULAR = "search_popular";
	private static final String FILE_CELEB_RADIO = "celeb_radio";
	private static final String FILE_CATEGORIES_GENER = "categories_gener";
	private static final String FILE_PREF_GET_CATEGORY = "pref_get_category";
	private static final String FILE_GET_ALL_LANGUAGES = "get_all_languages";
	private static final String FILE_GET_USER_LANGUAGE = "get_user_language";
	private static final String FILE_LEFT_MENU = "left_menu";
	private static final String FILE_USER_PROFILE = "user_profile";
	private static final String FILE_USER_DOWNLOAD = "user_download";
	private static final String FILE_USER_LEADERBOARD = "user_leaderboard";
	private static final String FILE_USER_FAVORTIE = "user_favorite";
	private static final String FILE_USER_BADGES = "user_badges";
	private static final String FILE_INAPP_PROMPT = "inapp_prompt";

	private static final String FILE_NQHistory = "NQ_HISTORY";

	static final String FOLDER_MOODS_IMAGES = "moods_images";
	static final int CACHE_SIZE_MOODS_IMAGES = 2 * 1024 * 1024; // 2M

	static final String FOLDER_APPLICATION_IMAGES = "application_images";
	public static final int CACHE_SIZE_APP_IMAGES = 15 * 1024 * 1024; // 10M

	private Context mContext;
	private String mInternalCachePath;

	private File mMoodsImageCache;
	private File mApplicationImageCache;

	public CacheManager(Context context) {
		mContext = context;
		mInternalCachePath = null;
		mMoodsImageCache = null;
		try {
			if (mContext != null) {
				mInternalCachePath = mContext.getCacheDir().getAbsolutePath();
				mMoodsImageCache = mContext.getDir(FOLDER_MOODS_IMAGES,
						Context.MODE_PRIVATE);
				mApplicationImageCache = mContext.getDir(
						FOLDER_APPLICATION_IMAGES, Context.MODE_PRIVATE);
			}
		} catch (Exception e) {
		}
	}

	// ======================================================
	// Media Content
	// ======================================================

	private final Object mMediaItemsMutext = new Object();

	/**
	 * Stores Media items in the application's internal storage.
	 * 
	 * @return true if success, false otherwise.
	 */
	public boolean storeMediaItems(MediaContentType mediaContentType,
			MediaCategoryType mediaCategoryType, List<MediaItem> mediaItems) {
		synchronized (mMediaItemsMutext) {
			try {
				if (mediaItems != null && mediaItems.size() > 0) {
					// serialize the media items to a json structure.
					Gson gson = new GsonBuilder()
							.excludeFieldsWithoutExposeAnnotation().create();
					String serializedMediaItems = gson.toJson(mediaItems);
					// generates the file location.
					String fileName = FILE_MEDIA_ITEMS + "_"
							+ mediaContentType.toString().toLowerCase() + "_"
							+ mediaCategoryType.toString().toLowerCase();
					File mediaItemsFile = new File(mInternalCachePath, fileName);
					// stores it.
					return writeSerializedToCacheFile(serializedMediaItems,
							mediaItemsFile);
				}
			} catch (Exception e) {
			} catch (Error e) {
				System.gc();
				System.runFinalization();
				System.gc();
			}
			return false;
		}
	}

	/**
	 * Receives list of {@link MediaItem}s stored in the application's internal
	 * storage.
	 * 
	 * @param mediaContentType
	 * @param mediaCategoryType
	 * @return List of {@link MediaItem} if success, null otherwise.
	 */
	// public List<MediaItem> getStoredMediaItems(
	// MediaContentType mediaContentType,
	// MediaCategoryType mediaCategoryType) {
	// synchronized (mMediaItemsMutext) {
	// // generates the file location.
	// String fileName = FILE_MEDIA_ITEMS + "_"
	// + mediaContentType.toString().toLowerCase() + "_"
	// + mediaCategoryType.toString().toLowerCase();
	// File mediaItemsFile = new File(mInternalCachePath, fileName);
	//
	// String mediaItemsJson = readSerializedFromCacheFile(mediaItemsFile);
	// if (!TextUtils.isEmpty(mediaItemsJson)) {
	// // deserialize the json to the list of media items.
	// Type listType = new TypeToken<ArrayList<MediaItem>>() {
	// }.getType();
	// Gson gson = new GsonBuilder()
	// .excludeFieldsWithoutExposeAnnotation().create();
	// List<MediaItem> items = null;
	// // the magic.
	// try {
	// items = gson.fromJson(mediaItemsJson, listType);
	// // populate the items with their region.
	// for (MediaItem mediaItem : items) {
	// mediaItem.setMediaContentType(mediaContentType);
	// }
	// return items;
	// } catch (JsonSyntaxException exception) {
	// exception.printStackTrace();
	// } catch (JsonParseException exception) {
	// exception.printStackTrace();
	// }
	// }
	// return null;
	// }
	// }

	// ======================================================
	// Events.
	// ======================================================

	private final Object mEventsMutext = new Object();

	/**
	 * Stores events in the application's internal storage.
	 * 
	 * @param events
	 * @return true if success, false otherwise.
	 */
	public boolean storeEvents(List<Event> events) {
		synchronized (mEventsMutext) {
			Logger.v(TAG, "Storing events in internal storage.");
			if (events != null && events.size() > 0) {
				// serialize the media items to a json structure.
				Gson gson = new GsonBuilder().create();
				String serializedEvents = gson.toJson(events);

				// stores it.
				return writeSerializedToCacheFile(serializedEvents, new File(
						mInternalCachePath, FILE_EVENTS));
			} else {
				return writeSerializedToCacheFile("", new File(
						mInternalCachePath, FILE_EVENTS));
			}
		}
	}

	public List<Event> getStoredEvents() {
		synchronized (mEventsMutext) {
			List<Event> events = new ArrayList<Event>();
			try {
				Logger.v(TAG, "Getting events in internal storage.");
				// generates the file location.
				String serializedEvents = readSerializedFromCacheFile(new File(
						mInternalCachePath, FILE_EVENTS));
				// deserialize the json to the list of media items.
				Type listType = new TypeToken<ArrayList<Event>>() {
				}.getType();
				Gson gson = new GsonBuilder().registerTypeAdapter(listType,
						new EventElementAdapter()).create();
				// the magic.
				events = gson.fromJson(serializedEvents, listType);
				return events;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			} catch (Exception exception) {
				Logger.printStackTrace(exception);
			}
			return events;
		}
	}

	// public boolean storeEvent(Event event) {
	// synchronized (mEventsMutext) {
	// Logger.v(TAG, "Storing event in internal storage.");
	// List<Event> storedEvents = null;
	//
	// boolean result = false;
	//
	// if (event != null) {
	// storedEvents = getStoredEvents();
	// }
	//
	// if (storedEvents != null) {
	// storedEvents.add(event);
	// result = storeEvents(storedEvents);
	// } else {
	// storedEvents = new ArrayList<Event>();
	// storedEvents.add(event);
	// result = storeEvents(storedEvents);
	// }
	//
	// return result;
	// }
	// }

	// ======================================================
	// Moods.
	// ======================================================

	private final Object mMoodsMutext = new Object();

	public boolean storeMoods(List<Mood> moods) {
		synchronized (mMoodsMutext) {
			Logger.v(TAG, "Storing moods in internal storage.");
			// serialize the moods to a json structure.
			Gson gson = new GsonBuilder().create();
			String serializedMoods = gson.toJson(moods);
			return writeSerializedToFile(serializedMoods, FILE_MOODS);
		}
	}

	public List<Mood> getStoredMoods() {
		synchronized (mMoodsMutext) {
			Logger.v(TAG, "Getting moods from internal storage.");
			// generates the file location.
			String serializedMoods = readSerializedFromFile(FILE_MOODS);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<Mood>>() {
			}.getType();
			Gson gson = new GsonBuilder().create();
			List<Mood> moods = null;
			// the magic.
			try {
				moods = gson.fromJson(serializedMoods, listType);
				return moods;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	public Drawable getMoodIcon(Mood mood, DataManager.MoodIcon moodIcon)
			throws IOException {
		try {
			DiskLruCache diskLruCache = DiskLruCache.open(mMoodsImageCache, 1,
					1, CACHE_SIZE_MOODS_IMAGES);
			// gets the right image URL, will be used as the key of the image.
			String imageUrl = moodIcon == MoodIcon.SMALL ? mood
					.getSmallImageUrl() : mood.getBigImageUrl();
			// creates new instance of a bitmap drawable.
			return BitmapDrawable.createFromPath(diskLruCache
					.createFilePath(imageUrl));
		} catch (OutOfMemoryError e) {
		}catch (Exception e) {
		}
		return null;
	}

	public Drawable getMoodIcon(String imageUrl) throws IOException {
		try {
			DiskLruCache diskLruCache = DiskLruCache.open(mMoodsImageCache, 1,
					1, CACHE_SIZE_MOODS_IMAGES);
			// gets the right image URL, will be used as the key of the image.
			// String imageUrl = moodIcon == MoodIcon.SMALL ? mood
			// .getSmallImageUrl() : mood.getBigImageUrl();
			// creates new instance of a bitmap drawable.
			return BitmapDrawable.createFromPath(diskLruCache
					.createFilePath(imageUrl));
		} catch (OutOfMemoryError e) {
		}catch (Exception e) {
		}
		return null;
	}

	// ======================================================
	// Preferences - categories.
	// ======================================================

	private final Object mPreferencesMutext = new Object();

	// public void storePreferences(final List<CategoryTypeObject> categories,
	// final Callback callback) {
	// new Thread() {
	// public void run() {
	// synchronized (mPreferencesMutext) {
	// Logger.v(TAG, "Storing preferences in internal storage.");
	// // serialize the moods to a json structure.
	// Gson gson = new GsonBuilder()
	// .excludeFieldsWithoutExposeAnnotation().create();
	// String serializedMoods = gson.toJson(categories);
	// writeSerializedToFile(serializedMoods, FILE_PREFERENCES,
	// callback);
	// }
	// }
	// }.start();
	//
	// }
	//
	// public List<CategoryTypeObject> getStoredPreferences() {
	// synchronized (mPreferencesMutext) {
	// Logger.v(TAG, "Getting preferences from internal storage.");
	// // generates the file location.
	// String serializedCategories = readSerializedFromFile(FILE_PREFERENCES);
	// // deserialize the json to the list of categories.
	// Type listType = new TypeToken<ArrayList<CategoryTypeObject>>() {
	// }.getType();
	// Gson gson = new GsonBuilder().registerTypeAdapter(listType,
	// new CategoryTypeObjectElementAdapter()).create();
	// List<CategoryTypeObject> categoryTypeObject = null;
	// // the magic.
	// try {
	// categoryTypeObject = gson.fromJson(serializedCategories,
	// listType);
	// return categoryTypeObject;
	// } catch (JsonSyntaxException exception) {
	// exception.printStackTrace();
	// } catch (JsonParseException exception) {
	// exception.printStackTrace();
	// }
	// return null;
	// }
	// }

	public void storePreferences(final MusicCategoriesResponse categories,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mPreferencesMutext) {
					Logger.v(TAG, "Storing preferences in internal storage.");
					// serialize the moods to a json structure.
					Gson gson = new GsonBuilder()
							.excludeFieldsWithoutExposeAnnotation().create();
					String serializedMoods = gson.toJson(categories);
					writeSerializedToFile(serializedMoods, FILE_PREFERENCES,
							callback);
				}
			}
		});

	}

	public MusicCategoriesResponse getStoredPreferences() {
		synchronized (mPreferencesMutext) {
			Logger.v(TAG, "Getting preferences from internal storage.");
			// generates the file location.
			String serializedCategories = readSerializedFromFile(FILE_PREFERENCES);
			// deserialize the json to the list of categories.
			// Type listType = new TypeToken<ArrayList<CategoryTypeObject>>() {
			// }.getType();
			// Gson gson = new GsonBuilder().registerTypeAdapter(listType,
			// new CategoryTypeObjectElementAdapter()).create();
			// MusicCategoriesResponse musicCategoriesResponse = null;
			// the magic.
			try {
				MusicCategoriesResponse mMusicCategoriesResponse = new Gson()
						.fromJson(serializedCategories,
								MusicCategoriesResponse.class);
				return mMusicCategoriesResponse;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	// public void storePreferencesVideo(
	// final List<CategoryTypeObject> categories, final Callback callback) {
	// new Thread() {
	// public void run() {
	// synchronized (mPreferencesMutext) {
	// Logger.v(TAG, "Storing preferences in internal storage.");
	// // serialize the moods to a json structure.
	// Gson gson = new GsonBuilder()
	// .excludeFieldsWithoutExposeAnnotation().create();
	// String serializedMoods = gson.toJson(categories);
	// writeSerializedToFile(serializedMoods,
	// FILE_PREFERENCES_VIDEO, callback);
	// }
	// }
	// }.start();
	// }

	public List<CategoryTypeObject> getStoredPreferencesVideo() {
		synchronized (mPreferencesMutext) {
			Logger.v(TAG, "Getting preferences from internal storage.");
			// generates the file location.
			String serializedCategories = readSerializedFromFile(FILE_PREFERENCES_VIDEO);
			// deserialize the json to the list of categories.
			Type listType = new TypeToken<ArrayList<CategoryTypeObject>>() {
			}.getType();
			Gson gson = new GsonBuilder().registerTypeAdapter(listType,
					new CategoryTypeObjectElementAdapter()).create();
			List<CategoryTypeObject> categoryTypeObject = null;
			// the magic.
			try {
				categoryTypeObject = gson.fromJson(serializedCategories,
						listType);
				return categoryTypeObject;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	// ======================================================
	// Current Subscription Plan
	// ======================================================

	private final Object mSubscriptionMutext = new Object();

	/**
	 * Stores Subscription Plan in the application's internal storage.
	 * 
	 * @return true if success, false otherwise.
	 */
//	public boolean storeSubscriptionCurrentPlan(
//			SubscriptionCheckResponse subscriptionCheckResponse) {
//		synchronized (mSubscriptionMutext) {
//			if (subscriptionCheckResponse != null
//					&& subscriptionCheckResponse.getPlan() != null) {
//				// serialize the media items to a json structure.
//				Gson gson = new GsonBuilder()
//						.excludeFieldsWithoutExposeAnnotation().create();
//				String serializedCurrentPlan = gson
//						.toJson(subscriptionCheckResponse);
//				// generates the file location.
//				String fileName = FILE_CURRENT_PLAN;
//				File currentPlanFile = new File(mInternalCachePath, fileName);
//				// stores it.
//				return writeSerializedToCacheFile(serializedCurrentPlan,
//						currentPlanFile);
//			}
//			return false;
//		}
//	}

	public boolean storeSubscriptionCurrentPlanNew(
			SubscriptionStatusResponse subscriptionStatusResponse) {
		synchronized (mSubscriptionMutext) {
			if (subscriptionStatusResponse != null
					&& subscriptionStatusResponse.getSubscription() != null) {
				// serialize the media items to a json structure.
				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation().create();
				String serializedCurrentPlan = gson
						.toJson(subscriptionStatusResponse);
				// generates the file location.
				String fileName = FILE_CURRENT_PLAN_NEW;
				File currentPlanFile = new File(mInternalCachePath, fileName);
				// stores it.
				return writeSerializedToCacheFile(serializedCurrentPlan,
						currentPlanFile);
			}
			return false;
		}
	}

	public interface ReadCallback {
		public void onRead(Object respose);
	}

//	/**
//	 * @return current subscribed {@link Plan} if success, null otherwise.
//	 */
//	public void getStoredCurrentPlan(final ReadCallback callback) {
//		final Handler handler = new Handler();
//		new Thread() {
//			public void run() {
//				synchronized (mSubscriptionMutext) {
//					boolean isSuccess = false;
//					// generates the file location.
//					String fileName = FILE_CURRENT_PLAN;
//					File currentPlanFile = new File(mInternalCachePath,
//							fileName);
//
//					String currentPlanJson = readSerializedFromCacheFile(currentPlanFile);
//					if (!TextUtils.isEmpty(currentPlanJson)) {
//						Gson gson = new GsonBuilder()
//								.excludeFieldsWithoutExposeAnnotation()
//								.create();
//						final SubscriptionCheckResponse subscribedPlan;
//						// the magic.
//						try {
//							subscribedPlan = gson.fromJson(currentPlanJson,
//									SubscriptionCheckResponse.class);
//							handler.post(new Runnable() {
//
//								@Override
//								public void run() {
//									callback.onRead(subscribedPlan);
//
//								}
//							});
//							isSuccess = true;
//						} catch (JsonSyntaxException exception) {
//							exception.printStackTrace();
//						} catch (JsonParseException exception) {
//							exception.printStackTrace();
//						}
//					}
//					if (!isSuccess)
//						handler.post(new Runnable() {
//
//							@Override
//							public void run() {
//								callback.onRead(null);
//
//							}
//						});
//
//				}
//			}
//		}.start();
//
//	}

	public void getStoredCurrentPlanNew(final ReadCallback callback) {
		final Handler handler = new Handler();
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mSubscriptionMutext) {
					boolean isSuccess = false;
					// generates the file location.
					String fileName = FILE_CURRENT_PLAN_NEW;
					File currentPlanFile = new File(mInternalCachePath,
							fileName);

					String currentPlanJson = readSerializedFromCacheFile(currentPlanFile);
					if (!TextUtils.isEmpty(currentPlanJson)) {
						Gson gson = new GsonBuilder()
								.excludeFieldsWithoutExposeAnnotation()
								.create();
						final SubscriptionStatusResponse subscribedPlan;
						// the magic.
						try {
							subscribedPlan = gson.fromJson(currentPlanJson,
									SubscriptionStatusResponse.class);
							handler.post(new Runnable() {
								@Override
								public void run() {
									callback.onRead(subscribedPlan);
								}
							});
							isSuccess = true;
						} catch (JsonSyntaxException exception) {
							exception.printStackTrace();
						} catch (JsonParseException exception) {
							exception.printStackTrace();
						}
					}
					if (!isSuccess)
						handler.post(new Runnable() {
							@Override
							public void run() {
								callback.onRead(null);
							}
						});
				}
			}
		});
	}

	public SubscriptionStatusResponse getStoredCurrentPlanNewSync() {
		synchronized (mSubscriptionMutext) {
			// generates the file location.
			String fileName = FILE_CURRENT_PLAN_NEW;
			File currentPlanFile = new File(mInternalCachePath,
					fileName);

			String currentPlanJson = readSerializedFromCacheFile(currentPlanFile);
			if (!TextUtils.isEmpty(currentPlanJson)) {
				Gson gson = new GsonBuilder()
						.excludeFieldsWithoutExposeAnnotation()
						.create();
				final SubscriptionStatusResponse subscribedPlan;
				// the magic.
				try {
					subscribedPlan = gson.fromJson(currentPlanJson,
							SubscriptionStatusResponse.class);
					return subscribedPlan;
//					isSuccess = true;
				} catch (JsonSyntaxException exception) {
					exception.printStackTrace();
				} catch (JsonParseException exception) {
					exception.printStackTrace();
				}
			}
			return null;
		}
	}

//	/**
//	 * @return current subscribed {@link Plan} if success, null otherwise.
//	 */
	public boolean deleteStoredCurrentPlanNew() {
		synchronized (mSubscriptionMutext) {
			// generates the file location.
			String fileName = FILE_CURRENT_PLAN_NEW;
			File currentPlanFile = new File(mInternalCachePath, fileName);
			return deleteCurrentPlanFile(currentPlanFile);
		}
	}

	// ======================================================
	// Subscription Plans For Upgrade
	// ======================================================

	// private final Object mSubscriptionPlansMutext = new Object();

	/**
	 * Stores Subscription Plans For Upgrade in the application's internal
	 * storage.
	 * 
	 * @return true if success, false otherwise.
	 */
	// public boolean storeSubscriptionPlans(List<Plan> plans) {
	// synchronized (mSubscriptionPlansMutext) {
	// if (plans != null) {
	// // serialize the media items to a json structure.
	// Gson gson = new GsonBuilder()
	// .excludeFieldsWithoutExposeAnnotation().create();
	// String serializedPlans = gson.toJson(plans);
	// // generates the file location.
	// String fileName = FILE_PLANS;
	// File currentPlanFile = new File(mInternalCachePath, fileName);
	// // stores it.
	// return writeSerializedToCacheFile(serializedPlans,
	// currentPlanFile);
	// }
	// return false;
	// }
	// }

	// /**
	// * @return {@link Plan}s if success, null otherwise.
	// */
	// public void getStoredPlans(final ReadCallback callback) {
	// new Thread() {
	// @Override
	// public void run() {
	// synchronized (mSubscriptionPlansMutext) {
	// // generates the file location.
	// String fileName = FILE_PLANS;
	// File currentPlanFile = new File(mInternalCachePath,
	// fileName);
	//
	// String currentPlanJson = readSerializedFromCacheFile(currentPlanFile);
	// boolean success = false;
	// if (!TextUtils.isEmpty(currentPlanJson)) {
	// Gson gson = new GsonBuilder()
	// .excludeFieldsWithoutExposeAnnotation()
	// .create();
	// List<Plan> subscriptionPlans = null;
	// Type listType = new TypeToken<List<Plan>>() {
	// }.getType();// TODO: check
	// // the magic.
	// try {
	// subscriptionPlans = gson.fromJson(currentPlanJson,
	// listType);
	// callback.onRead(subscriptionPlans);
	// success = true;
	// } catch (JsonSyntaxException exception) {
	// exception.printStackTrace();
	// } catch (JsonParseException exception) {
	// exception.printStackTrace();
	// }
	// }
	//
	// if (success == false)
	// callback.onRead(null);
	//
	// }
	// }
	// }.start();
	//
	// }

	// ======================================================
	// Private helper methods.
	// ======================================================

	private boolean writeSerializedToCacheFile(String serializedItems,
			File destination) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(destination);
			fileOutputStream.write(serializedItems.getBytes());

			fileOutputStream.close();
			fileOutputStream = null;
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fileOutputStream = null;
				return false;
			}
		}
	}

	private String readSerializedFromCacheFile(File source) {
		if (source.exists()) {
			BufferedReader inputBufferedReader = null;
			StringBuilder responseBuilder = new StringBuilder();
			try {
				inputBufferedReader = new BufferedReader(new InputStreamReader(
						new FileInputStream(source)));
				char[] BUFF = new char[500];
				int len = 0;
				while ((len = inputBufferedReader.read(BUFF)) > 0
				/* && !Thread.currentThread().isInterrupted() */) {
					responseBuilder.append(BUFF, 0, len);
				}

				inputBufferedReader.close();
				inputBufferedReader = null;

				return responseBuilder.toString();

			} catch (FileNotFoundException exception) {
				exception.printStackTrace();
			} catch (IOException exception) {
				exception.printStackTrace();
			} finally {
				if (inputBufferedReader != null) {
					try {
						inputBufferedReader.close();
						inputBufferedReader = null;

					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	private boolean deleteCurrentPlanFile(File currentPlanFile) {
		if (currentPlanFile.exists()) {
			return currentPlanFile.delete();
		}
		return false;
	}

	public interface Callback {
		public abstract void onResult(Boolean gotResponse);
	}

	private void writeSerializedToFile(final String serializedItems,
			final String fileName, final Callback callback) {

		// Log.e("writeSerializedToFile", fileName);

		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = mContext.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			fileOutputStream.write(serializedItems.getBytes());
			fileOutputStream.close();
			fileOutputStream = null;
			if (callback != null)
				callback.onResult(true);
			// Log.e("writeSerializedToFile", fileName + ">>>>>>X");
			return;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if (callback != null)
				callback.onResult(false);
			// Log.e("writeSerializedToFile", fileName + ">>>>>>X");

			return;
		} catch (Exception e) {
			e.printStackTrace();
			if (callback != null)
				callback.onResult(false);
			// Log.e("writeSerializedToFile", fileName + ">>>>>>X");

			return;
		} finally {
			// Log.e("writeSerializedToFile", fileName + ">>>>>>X");

			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
					fileOutputStream = null;

				} catch (IOException e) {
					e.printStackTrace();
				}
				fileOutputStream = null;
				if (callback != null)
					callback.onResult(false);

				return;
			}
		}
	}

	private boolean writeSerializedToFile(String serializedItems,
			String fileName) {

		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = mContext.openFileOutput(fileName,
					Context.MODE_PRIVATE);
			fileOutputStream.write(serializedItems.getBytes());

			fileOutputStream.close();
			fileOutputStream = null;

			return true;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
					fileOutputStream = null;

				} catch (IOException e) {
					e.printStackTrace();
				}
				fileOutputStream = null;
				return false;
			}
		}
	}

	private String readSerializedFromFile(String fileName) {
		try {
			// Log.e("writeSerializedToFile.....READ", fileName);

			BufferedReader inputBufferedReader = null;
			StringBuilder responseBuilder = new StringBuilder();
			try {
				inputBufferedReader = new BufferedReader(new InputStreamReader(
						mContext.openFileInput(fileName)));
				char[] BUFF = new char[500];
				int len = 0;
				while ((len = inputBufferedReader.read(BUFF)) > 0
				/* && !Thread.currentThread().isInterrupted() */) {
					responseBuilder.append(BUFF, 0, len);
				}

				inputBufferedReader.close();
				inputBufferedReader = null;
				// Log.e("writeSerializedToFile.....READ", fileName
				// + ">>>>>>>>>>>>>>X");

				return responseBuilder.toString();

			} catch (FileNotFoundException exception) {
				Logger.printStackTrace(exception);
			} catch (IOException exception) {
				Logger.printStackTrace(exception);
			} finally {
				if (inputBufferedReader != null) {
					try {
						inputBufferedReader.close();
						inputBufferedReader = null;

					} catch (IOException e) {
						e.printStackTrace();
					}
					inputBufferedReader = null;
				}
			}
		} catch (Exception e) {
		} catch (OutOfMemoryError e) {
		}
		// Log.e("writeSerializedToFile.....READ", fileName +
		// ">>>>>>>>>>>>>>X");

		return null;
	}

	/**
	 * Adapter for Gsonning different concrete implementations of the type
	 * {@link Event}.
	 */
	private class EventElementAdapter implements JsonDeserializer<List<Event>> {

		private static final String UNIQUE_FIELD_PLAY_EVENT = "playingSourceType";
		private static final String EVENT_PATH = "com.hungama.myplay.activity.data.events.";

		@Override
		public List<Event> deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			JsonObject jsonObject = null;

			List<Event> events = new ArrayList<Event>();

			for (JsonElement jsonElement : jsonArray) {
				jsonObject = jsonElement.getAsJsonObject();
				try {
					if (jsonObject.has(UNIQUE_FIELD_PLAY_EVENT)) {
						events.add((Event) context.deserialize(
								jsonObject,
								Class.forName(EVENT_PATH
										+ PlayEvent.class.getSimpleName())));
					} else {
						events.add((Event) context.deserialize(
								jsonObject,
								Class.forName(EVENT_PATH
										+ CampaignPlayEvent.class
												.getSimpleName())));
					}
				} catch (ClassNotFoundException cnfe) {
					throw new JsonParseException("Unknown element type: "
							+ PlayEvent.class, cnfe);
				}
			}

			return events;
		}

	}

	/**
	 * Adapter for Gsonning different concrete implementations of the type
	 * {@link CategoryTypeObject}.
	 */
	private class CategoryTypeObjectElementAdapter implements
			JsonDeserializer<List<CategoryTypeObject>> {

		private static final String UNIQUE_FIELD_CATEGORY_TYPE_OBJECT = "type";
		private static final String CATEGORY_TYPE_OBJECT_PATH = "com.hungama.myplay.activity.data.dao.hungama.";

		private static final String MEMBER_ID = "id";
		private static final String MEMBER_NAME = "name";
		private static final String MEMBER_CATEGORY_OBJECT_TYPES = "categoryTypeObjects";

		@Override
		public List<CategoryTypeObject> deserialize(JsonElement json,
				Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			JsonArray jsonArray = json.getAsJsonArray();
			JsonObject jsonObject = null;

			String type = null;

			List<CategoryTypeObject> categoryTypeObjects = new ArrayList<CategoryTypeObject>();

			// iterates thru the root categories.
			for (JsonElement jsonElement : jsonArray) {
				jsonObject = jsonElement.getAsJsonObject();
				type = jsonObject.get(UNIQUE_FIELD_CATEGORY_TYPE_OBJECT)
						.getAsString();

				try {
					if (type.equalsIgnoreCase((CategoryTypeObject.TYPE_CATEGORY
							.toString().toLowerCase()))) {
						// checks if we need to parse the inner category types
						// of the
						// given category type.
						if (jsonObject.get(MEMBER_CATEGORY_OBJECT_TYPES) != null) {

							/*
							 * Due to the problem the both Category and Genre
							 * are from the same type, and Category can contain
							 * list of both categories and genres, we must pares
							 * it manually :(
							 */

							/*
							 * First creates the Category object, then populates
							 * its children by a reference to the list.
							 */
							long typeId = jsonObject.get(MEMBER_ID).getAsLong();
							String typeName = jsonObject.get(MEMBER_NAME)
									.getAsString();

							/*
							 * this list is first initializes and sets as the
							 * children in the category, will be populate when
							 * parsing the category's children.
							 */
							List<CategoryTypeObject> subCategoryTypes = new ArrayList<CategoryTypeObject>();

							Category parentCategory = new Category(typeId,
									typeName, subCategoryTypes);

							// gets the list of the sub category types.
							JsonArray subCategoryTypesObjects = jsonObject
									.getAsJsonArray(MEMBER_CATEGORY_OBJECT_TYPES);

							JsonObject subObjectType;
							for (int i = 0; i < subCategoryTypesObjects.size(); i++) {
								subObjectType = (JsonObject) subCategoryTypesObjects
										.get(i);
								type = subObjectType.get(
										UNIQUE_FIELD_CATEGORY_TYPE_OBJECT)
										.getAsString();

								if (type.equalsIgnoreCase((CategoryTypeObject.TYPE_CATEGORY
										.toString().toLowerCase()))) {
									long subCategoryTypeId = subObjectType.get(
											MEMBER_ID).getAsLong();
									String subCategoryTypeName = subObjectType
											.get(MEMBER_NAME).getAsString();

									Category subCategory = new Category(
											subCategoryTypeId,
											subCategoryTypeName, null);
									subCategory
											.setParentCategory(parentCategory);

									subCategoryTypes.add(subCategory);

								} else {
									// creates the genre.
									Genre childGenre = context
											.deserialize(
													subObjectType,
													Class.forName(CATEGORY_TYPE_OBJECT_PATH
															+ Genre.class
																	.getSimpleName()));
									// sets a reference to its parent.
									childGenre
											.setParentCategory(parentCategory);

									subCategoryTypes.add(childGenre);

								}
							}

							categoryTypeObjects.add(parentCategory);

						} else {
							categoryTypeObjects
									.add((CategoryTypeObject) context.deserialize(
											jsonObject,
											Class.forName(CATEGORY_TYPE_OBJECT_PATH
													+ Category.class
															.getSimpleName())));
						}

					} else {
						categoryTypeObjects
								.add((CategoryTypeObject) context.deserialize(
										jsonObject,
										Class.forName(CATEGORY_TYPE_OBJECT_PATH
												+ Genre.class.getSimpleName())));
					}
				} catch (ClassNotFoundException cnfe) {
					throw new JsonParseException("Unknown element type", cnfe);
				}
			}

			return categoryTypeObjects;
		}

	}

	// ======================================================
	// Campaigns.
	// ======================================================

	private final Object mCampaignListMutext = new Object();

	public boolean storeCampaignList(List<String> list) {
		synchronized (mCampaignListMutext) {
			Logger.v(TAG, "Storing Campaign List id's in internal storage.");

			Gson gson = new GsonBuilder().create();
			String serializedCampaignList = gson.toJson(list);
			Logger.writetofileCampaign(new Date() + " ::: "
					+ serializedCampaignList, true);
			return writeSerializedToFile(serializedCampaignList,
					FILE_CAMPAIGN_LIST);
		}
	}

	public List<String> getStoredCampaignList() {
		synchronized (mCampaignListMutext) {
			Logger.v(TAG, "Getting Campaign List id's from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_CAMPAIGN_LIST);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<String>>() {
			}.getType();
			Gson gson = new GsonBuilder().create();
			List<String> list = null;
			// the magic.
			try {
				list = gson.fromJson(serializedCampaignList, listType);
				return list;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	private final Object mCampaignMutext = new Object();

	public void storeCampaign(final List<Campaign> list, final Callback callback) {
		try {
//			ThreadPoolManager.getInstance().submit(new Runnable() {
//				public void run() {
					synchronized (mCampaignMutext) {
						Logger.v(TAG, "Storing Campaigns in internal storage.");

						Gson gson = new GsonBuilder().create();
						String serializedCampaign = gson.toJson(list);
						Logger.writetofileCampaign(new Date() + " ::: "
								+ serializedCampaign, true);
						writeSerializedToFile(serializedCampaign,
								FILE_CAMPAIGN, callback);
					}
//				}
//			});
		} catch (OutOfMemoryError e) {
		}

	}

	public List<Campaign> getStoredCampaign() {
		synchronized (mCampaignListMutext) {
			Logger.v(TAG, "Getting Campaign  from internal storage.");
			// generates the file location.
			String serializedCampaign = readSerializedFromFile(FILE_CAMPAIGN);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<Campaign>>() {
			}.getType();
			Gson gson = new GsonBuilder().create();
			List<Campaign> list = null;
			// the magic.
			try {
				list = gson.fromJson(serializedCampaign, listType);
				return list;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	// ======================================================
	// Feedback subjects.
	// ======================================================

	private final Object mFeedbackSubjectMutext = new Object();

	public boolean storeFeedbackSubjects(List<String> subjects) {
		synchronized (mFeedbackSubjectMutext) {
			Logger.v(TAG, "Storing feedback's subjects in internal storage.");
			Gson gson = new GsonBuilder().create();
			String serializedSubjects = gson.toJson(subjects);
			return writeSerializedToFile(serializedSubjects,
					FILE_FEEDBACK_SUBJECTS);
		}
	}

	public List<String> getStoredFeedbackSubjects() {
		synchronized (mFeedbackSubjectMutext) {
			Logger.v(TAG, "Getting feedback's subjects from internal storage.");
			String serializedSubjects = readSerializedFromFile(FILE_FEEDBACK_SUBJECTS);
			// deserialize the JSON to the list of subjects (AKA. list of stupid
			// strings).
			Type listType = new TypeToken<ArrayList<String>>() {
			}.getType();
			Gson gson = new GsonBuilder().create();
			List<String> subjects = null;
			// the magic...
			try {
				subjects = gson.fromJson(serializedSubjects, listType);
				return subjects;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	// ======================================================
	// Itemables (Playlist).
	// ======================================================

	private final Object mItemableListMutext = new Object();

	public void storePlaylists(final Map<Long, Playlist> list,
			final Callback callback) {
		// new Thread() {
		// public void run() {
		synchronized (mItemableListMutext) {
			Logger.v(TAG, "Storing Itemables List in internal storage.");

			String fileName = FILE_PLAYLIST;

			Gson gson = new GsonBuilder().create();
			String serializedItemableList = gson.toJson(list);
			writeSerializedToFile(serializedItemableList, fileName, callback);
		}
		// }
		// }.start();
	}

	public Map<Long, Playlist> getStoredPlaylists() {

		synchronized (mItemableListMutext) {
			Logger.v(TAG, "Getting Itemables from internal storage.");

			String serializedItemables = readSerializedFromFile(FILE_PLAYLIST);

			Type listType = new TypeToken<Map<Long, Playlist>>() {
			}.getType();

			Gson gson = new GsonBuilder().create();

			Map<Long, Playlist> list = null;

			// the magic.
			try {
				list = gson.fromJson(serializedItemables, listType);
				return list;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	// ======================================================
	// Playlist Request.
	// ======================================================

	private final Object mPlaylistRequestMutext = new Object();

	public boolean storeRequestList(List<PlaylistRequest> list) {
		synchronized (mPlaylistRequestMutext) {
			Logger.v(TAG, "Storing Playlist Request List in internal storage.");

			String fileName = FILE_PLAYLIST_REQUEST;

			Gson gson = new GsonBuilder().create();
			String serializedItemableList = gson.toJson(list);
			return writeSerializedToFile(serializedItemableList, fileName);
		}
	}

	public List<PlaylistRequest> getStoredRequestList() {
		synchronized (mCampaignListMutext) {
			Logger.v(TAG,
					"Getting Playlist Request List from internal storage.");
			// generates the file location.
			String serializedCampaign = readSerializedFromFile(FILE_PLAYLIST_REQUEST);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<ArrayList<PlaylistRequest>>() {
			}.getType();
			Gson gson = new GsonBuilder().create();
			List<PlaylistRequest> list = null;
			// the magic.
			try {
				list = gson.fromJson(serializedCampaign, listType);
				return list;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	// ======================================================
	// Track (Hungama)
	// ======================================================

	private final Object mTrackListMutext = new Object();

	public void storeTrackList(final Map<Long, Track> list,
			final Callback callback) {
		// new Thread() {
		// public void run() {
		synchronized (mTrackListMutext) {
			Logger.v(TAG, "Storing Tracks List in internal storage.");
			// Logger.i(TAG, "Set Tracks: " + list.keySet().toString());
			String fileName = FILE_HUNGAMA_TRACK;
			// File file = new File(fileName);
			// if(file.exists())
			// file.delete();
			Gson gson = new GsonBuilder().create();
			String serializedItemableList = gson.toJson(list);
			writeSerializedToFile(serializedItemableList, fileName, callback);
		}
		// }
		// }.start();
	}

	public Map<Long, Track> getStoredTracks() {

		synchronized (mTrackListMutext) {
			Logger.v(TAG, "Getting Tracks from internal storage.");

			String serializedItemables = readSerializedFromFile(FILE_HUNGAMA_TRACK);

			Type listType = new TypeToken<Map<Long, Track>>() {
			}.getType();

			Gson gson = new GsonBuilder().create();

			Map<Long, Track> list = null;

			// the magic.
			try {
				list = gson.fromJson(serializedItemables, listType);

				if (list != null) {
					// Logger.i(TAG, "Get Tracks :" + list.keySet().toString());
				}

				return list;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	// ======================================================
	// Moods.
	// ======================================================

	private final Object mApplicationImagesMutext = new Object();

	public void storeApplicationImages(final Map<String, Object> imageMap,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mApplicationImagesMutext) {
					Logger.v(TAG,
							"Storing application images in internal storage.");
					// serialize the moods to a json structure.
					Gson gson = new GsonBuilder().create();
					String serializedMoods = gson.toJson(imageMap);
					writeSerializedToFile(serializedMoods,
							FILE_APPLICATION_IMAGES, callback);
				}
			}
		});
	}

	public Map<String, Object> getStoredApplicationImages() {
		synchronized (mApplicationImagesMutext) {
			Logger.v(TAG, "Getting moods from internal storage.");
			// generates the file location.
			String serializedMoods = readSerializedFromFile(FILE_APPLICATION_IMAGES);
			// deserialize the json to the list of media items.
			Type listType = new TypeToken<Map<String, Object>>() {
			}.getType();
			Gson gson = new GsonBuilder().create();
			Map<String, Object> imageMap = null;
			// the magic.
			try {
				imageMap = gson.fromJson(serializedMoods, listType);
				return imageMap;
			} catch (JsonSyntaxException exception) {
				exception.printStackTrace();
			} catch (JsonParseException exception) {
				exception.printStackTrace();
			}
			return null;
		}
	}

	private Map<String, Object> imageMap;

	public Drawable getApplicationImage(String imageName) throws IOException {
		DiskLruCache diskLruCache = null;
		Drawable d = null;
		try {
			diskLruCache = DiskLruCache.open(mApplicationImageCache, 1, 1,
					CACHE_SIZE_APP_IMAGES);
			// gets the right image URL, will be used as the key of the image.
			if (imageMap == null)
				imageMap = getStoredApplicationImages();
			if (imageMap != null) {
				// Logger.e("imageUrl", "" + imageMap);

				String imageUrl = (String) imageMap.get(imageName);

				Logger.e("imageUrl", "" + imageUrl);
				// creates new instance of a bitmap drawable.
				if (imageUrl != null && imageUrl.length() > 0) {
					Logger.s("imageUrl>> "
							+ diskLruCache.createFilePath(imageUrl));
					d = BitmapDrawable.createFromPath(diskLruCache
							.createFilePath(imageUrl));
					if (d == null) {
						d = new ColorDrawable(0x000);
						Logger.e("imageUrl >", "d is null");
					}
					// return d;

				}
			} else
				Logger.e("imageUrl >", "null");

		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (Error e) {
			Logger.e("imageUrl >", "d is null");
			e.printStackTrace();
			Logger.printStackTrace(e);
			d = new ColorDrawable(Color.BLACK);
			// return d;
		} finally {
			if (diskLruCache != null)
				diskLruCache.close();
		}
		return d;
	}

	public Drawable getApplicationImageNew(String imageName) throws IOException {
		DiskLruCache diskLruCache = null;
		Drawable d = null;
		try {
			diskLruCache = DiskLruCache.open(mApplicationImageCache, 1, 1,
					CACHE_SIZE_APP_IMAGES);
			// gets the right image URL, will be used as the key of the image.
			if (imageMap == null)
				imageMap = getStoredApplicationImages();
			if (imageMap != null) {
				// Logger.e("imageUrl", "" + imageMap);

				String imageUrl = (String) imageMap.get(imageName);

				Logger.e("imageUrl", "" + imageUrl);
				// creates new instance of a bitmap drawable.
				if (imageUrl != null && imageUrl.length() > 0) {
					d = BitmapDrawable.createFromPath(diskLruCache
							.createFilePath(imageUrl));
					if (d == null) {
						// d= new ColorDrawable(0x000);
						Logger.e("imageUrl >", "d is null");
					}
					d.setCallback(null);
					// return d;

				}
			} else
				Logger.e("imageUrl >", "null");
		} catch (Exception e) {
			Logger.printStackTrace(e);
		} catch (Error e) {
		} finally {
			if (diskLruCache != null)
				diskLruCache.close();
		}
		return d;
	}

	// public String getApplicationImagePath(String imageName) throws
	// IOException {
	// try {
	// // DiskLruCache diskLruCache =
	// // DiskLruCache.open(mApplicationImageCache, 1, 1,
	// // CACHE_SIZE_MOODS_IMAGES);
	// // gets the right image URL, will be used as the key of the image.
	// if(imageMap==null)
	// imageMap = getStoredApplicationImages();
	// if (imageMap != null) {
	// // Logger.e("imageUrl", ""+imageMap);
	//
	// String imageUrl = (String) imageMap.get(imageName);
	//
	// Logger.e("imageUrl", "" + imageUrl);
	// // creates new instance of a bitmap drawable.
	// if (imageUrl != null && imageUrl.length() > 0) {
	// // Logger.s("imageUrl>> " +
	// // diskLruCache.createFilePath(imageUrl));
	// // Drawable d=
	// // BitmapDrawable.createFromPath(diskLruCache.createFilePath(imageUrl));
	// // if(d==null)
	// // {
	// // d= new ColorDrawable(0x000);
	// // Logger.e("imageUrl >", "d is null");
	// // }
	// return imageUrl;
	//
	// }
	// } else
	// Logger.e("imageUrl >", "null");
	// } catch (Exception e) {
	// Logger.printStackTrace(e);
	// } catch (Error e) {
	// Logger.printStackTrace(e);
	// }
	// return null;
	// }

	private final Object mMusicLatestMutext = new Object();

	public void storeMusicLatestResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mMusicLatestMutext) {
					Logger.v(TAG, "Storing Music Latest in internal storage.");
					writeSerializedToFile(response, FILE_MUSIC_LATEST, callback);
				}
			}
		});
	}

	public String getMusicLatestResponse() {
		synchronized (mMusicLatestMutext) {
			Logger.v(TAG, "Getting Music Latest from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_MUSIC_LATEST);
			Logger.s("internal storage ::: " + serializedCampaignList);

			Logger.v(TAG,
					"Getting Music Latest from internal storage.xxxxxxxxxxxxxx");

			return serializedCampaignList;
		}
	}

	private final Object mMusicFeaturedMutext = new Object();

	public void storeMusicFeaturedResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mMusicFeaturedMutext) {
					Logger.v(TAG, "Storing Music Featured in internal storage.");
					writeSerializedToFile(response, FILE_MUSIC_FEATURED,
							callback);
				}
			}
		});
	}

	public String getMusicFeaturedResponse() {
		synchronized (mMusicFeaturedMutext) {
			Logger.v(TAG, "Getting Music Featured from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_MUSIC_FEATURED);
			Logger.s("internal storage ::: " + serializedCampaignList);
			Logger.v(TAG,
					"Getting Music Featured from internal storage.xxxxxxxxxxxxxxx");

			return serializedCampaignList;
		}
	}

	private final Object mVideoLatestMutext = new Object();

	public void storeVideoLatestResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mVideoLatestMutext) {
					Logger.v(TAG, "Storing Video Latest in internal storage.");
					writeSerializedToFile(response, FILE_VIDEO_LATEST, callback);
				}
			}
		});
	}

	public String getVideoLatestResponse() {
		synchronized (mVideoLatestMutext) {
			Logger.v(TAG, "Getting Video Latest from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_VIDEO_LATEST);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mSearchPopularMutext = new Object();

	public void storeSearchPopularResponse(final String response,
			final Callback callback)
	{
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mSearchPopularMutext) {
					Logger.v(TAG, "Storing Live Radio in internal storage.");
					writeSerializedToFile(response, FILE_SEARCH_POPULAR,
							callback);
				}
			}
		});
	}

	public String getSearchPopularResponse() {
		synchronized (mSearchPopularMutext) {
			Logger.v(TAG, "Getting Live Radio from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_SEARCH_POPULAR);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mLiveRadioMutext = new Object();

	public void storeLiveRadioResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mLeftMenuMutext) {
					Logger.v(TAG, "Storing Live Radio in internal storage.");
					writeSerializedToFile(response, FILE_LIVE_RADIO, callback);
				}
			}
		});
	}

	public String getLiveRadioResponse() {
		synchronized (mLeftMenuMutext) {
			Logger.v(TAG, "Getting Live Radio from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_LIVE_RADIO);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mCelebRadioMutext = new Object();

	public void storeCelebRadioResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mCelebRadioMutext) {
					Logger.v(TAG, "Storing Celeb Radio in internal storage.");
					writeSerializedToFile(response, FILE_CELEB_RADIO, callback);
				}
			}
		});
	}

	public String getCelebRadioResponse() {
		synchronized (mCelebRadioMutext) {
			Logger.v(TAG, "Getting Celeb Radio from internal storage.");
			String serializedCampaignList = readSerializedFromFile(FILE_CELEB_RADIO);
			Logger.s("internal storage ::: " + serializedCampaignList);
			Logger.v(TAG,
					"Getting Celeb Radio from internal storage.xxxxxxxxxxx");

			return serializedCampaignList;
		}
	}

	private final Object mCategoriesGenerMutext = new Object();

	public void storeCategoriesGenerResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mLeftMenuMutext) {
					Logger.v(TAG,
							"Storing Categories Gener in internal storage.");
					writeSerializedToFile(response, FILE_CATEGORIES_GENER,
							callback);
				}
			}
		});
	}

	public String getCategoriesGenerResponse() {
		synchronized (mLeftMenuMutext) {
			Logger.v(TAG, "Getting Categories Gener from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_CATEGORIES_GENER);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mPrefGetCategoryMutext = new Object();

	public void storePrefGetCategoryResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
		public void run() {
			synchronized (mPrefGetCategoryMutext) {
				Logger.v(TAG,
						"Storing Preference Get Category in internal storage.");
				writeSerializedToFile(response, FILE_PREF_GET_CATEGORY,
						callback);
			}
		}
	});
	}

	public String getPrefGetCategoryResponse() {
		synchronized (mPrefGetCategoryMutext) {
			Logger.v(TAG,
					"Getting Preference Get Category from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_PREF_GET_CATEGORY);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mGetAllLanguagesMutext = new Object();

	public void storeGetAllLanguagesResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mGetAllLanguagesMutext) {
					Logger.v(TAG,
							"Storing Get Languages List in internal storage.");
					writeSerializedToFile(response, FILE_GET_ALL_LANGUAGES,
							callback);
				}
			}
		});
	}

	public String getAllLanguagesResponse() {
		synchronized (mGetAllLanguagesMutext) {
			Logger.v(TAG, "Getting Get Languages List from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_GET_ALL_LANGUAGES);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mGetUserLanguageMutext = new Object();

	public void storeGetUserLanguageResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mGetUserLanguageMutext) {
					Logger.v(TAG,
							"Storing Get User Language in internal storage.");
					writeSerializedToFile(response, FILE_GET_USER_LANGUAGE,
							callback);
				}
			}
		});
	}

	public String getUserLanguageResponse() {
		synchronized (mGetUserLanguageMutext) {
			Logger.v(TAG, "Getting Get User Language from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_GET_USER_LANGUAGE);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mLeftMenuMutext = new Object();

	public void storeLeftMenuResponse(final String response,
			final Callback callback) {
//		new Thread() {
//			public void run() {
				synchronized (mLeftMenuMutext) {
					Logger.v(TAG, "Storing Left Menu in internal storage.");
					writeSerializedToFile(response, FILE_LEFT_MENU, callback);
				}
//			}
//		}.start();
	}

	public String getLeftMenuResponse() {
		synchronized (mLeftMenuMutext) {
			Logger.v(TAG, "Getting Left Menu from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_LEFT_MENU);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	// nq history file read-write
	private final Object mNQHistoryMutext = new Object();

	public void storeNQHistoryResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mNQHistoryMutext) {
					Logger.v(TAG, "Storing Left Menu in internal storage.");
					writeSerializedToFile(response, FILE_NQHistory, callback);
				}
			}
		});
	}

	public String getNQHistoryResponse() {
		synchronized (mNQHistoryMutext) {
			Logger.v(TAG, "Getting Left Menu from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_NQHistory);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	// private static final String FILE_USER_PROFILE = "user_profile";
	// private static final String FILE_USER_DOWNLOAD = "user_download";
	// private static final String FILE_USER_LEADERBOARD = "user_leaderboard";
	// private static final String FILE_USER_FAVORTIE = "user_favorite";
	private final Object mUserProfileDownload = new Object();

	// public void storeUserProfileDownloadResponse(final String response,
	// final Callback callback) {
	// new Thread() {
	// public void run() {
	// synchronized (mUserProfileDownload) {
	// Logger.v(TAG, "Storing Left Menu in internal storage.");
	// writeSerializedToFile(response, FILE_USER_DOWNLOAD, callback);
	// }
	// }
	// }.start();
	// }

	public String getUserProfileDownloadResponse() {
		synchronized (mUserProfileDownload) {
			Logger.v(TAG, "Getting Left Menu from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_USER_DOWNLOAD);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mUserProfileLeaderBoard = new Object();

	// public void storeUserProfileLeaderBoardResponse(final String response,
	// final Callback callback) {
	// new Thread() {
	// public void run() {
	// synchronized (mUserProfileLeaderBoard) {
	// Logger.v(TAG, "Storing Left Menu in internal storage.");
	// writeSerializedToFile(response, FILE_USER_LEADERBOARD, callback);
	// }
	// }
	// }.start();
	// }

	public String getUserProfileLeaderBoardResponse() {
		synchronized (mUserProfileLeaderBoard) {
			Logger.v(TAG, "Getting Left Menu from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_USER_LEADERBOARD);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mUserProfileFavorite = new Object();

	public void storeUserProfileFavoriteResponse(final String response,
			final String fileName, final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mUserProfileFavorite) {
					Logger.v(TAG, "Storing Left Menu in internal storage.");
					writeSerializedToFile(response, FILE_USER_FAVORTIE
							+ fileName, callback);
				}
			}
		});
	}

	public String getUserProfileFavoriteResponse(final String fileName) {
		synchronized (mUserProfileFavorite) {
			Logger.v(TAG, "Getting Left Menu from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_USER_FAVORTIE
					+ fileName);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mUserProfile = new Object();

	public void storeUserProfileResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mUserProfile) {
					Logger.v(TAG,
							"storeUserProfileResponse in internal storage."
									+ response);
					writeSerializedToFile(response, FILE_USER_PROFILE, callback);
				}
			}
		});
	}

	public String getUserProfileResponse() {
		synchronized (mUserProfile) {
			Logger.v(TAG, "Getting user profile from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_USER_PROFILE);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mUserBadges = new Object();

	public void storeUserProfileBadgesResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mUserBadges) {
					Logger.v(TAG, "Storing Left Menu in internal storage."
							+ response);
					writeSerializedToFile(response, FILE_USER_BADGES, callback);
				}
			}
		});
	}

	public String getUserProfileBadgesResponse() {
		synchronized (mUserBadges) {
			Logger.v(TAG, "Getting Left Menu from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_USER_BADGES);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}

	private final Object mInappPrompt = new Object();

	public void storeInAppPromptResponse(final String response,
			final Callback callback) {
		ThreadPoolManager.getInstance().submit(new Runnable() {
			public void run() {
				synchronized (mInappPrompt) {
					Logger.v(TAG, "Storing InApp Prompt in internal storage."
							+ response);
					writeSerializedToFile(response, FILE_INAPP_PROMPT, callback);
				}
			}
		});
	}

	public String getInAppPromptResponse() {
		synchronized (mInappPrompt) {
			Logger.v(TAG, "Getting InApp Prompt from internal storage.");
			// generates the file location.
			String serializedCampaignList = readSerializedFromFile(FILE_INAPP_PROMPT);
			Logger.s("internal storage ::: " + serializedCampaignList);
			return serializedCampaignList;
		}
	}
}
