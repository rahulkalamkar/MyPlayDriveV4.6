package com.hungama.myplay.activity;


import android.app.Activity;
import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.bosch.myspin.serversdk.MySpinException;
import com.bosch.myspin.serversdk.MySpinServerSDK;
import com.crashlytics.android.Crashlytics;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.DiskLruCache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.urbanairship.UAirship;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import io.fabric.sdk.android.Fabric;

//import com.crittercism.app.Crittercism;
//import com.urbanairship.Logger;

public class HungamaApplication extends MultiDexApplication {
	public static final String MESSAGE_ID_RECEIVED_KEY = "com.hungama.myplay.activity.MESSAGE_ID_RECEIVED";
	private static String TAG = "HungamaApplication";
	public static int splashAdDisplyedCount = 0;

	private static CacheManager cacheManager;
	private static Context context;

	//-----------CromeCast--------------//

	public static final boolean isAppRequiredMusicCast = true;
	public static final boolean isAppRequiredVideoCast = false;
	public static final double VOLUME_INCREMENT = 0.05;
	public static final int PRELOAD_TIME_S = 20;
	public static boolean isAppReCreated = false;

	@Override
	public void onCreate() {
		try {
			MySpinServerSDK.sharedInstance().registerApplication(this);
		} catch (MySpinException e) {
			e.printStackTrace();
		}

		Logger.s(System.currentTimeMillis()
				+ " :::::::::::::Stratup::::::::::::: " + getClass().getName());
		com.hungama.myplay.activity.util.Logger
				.s("Track HungamaApplication onCreate:");
		try {
			Class.forName("android.os.AsyncTask");
		} catch (ClassNotFoundException e) {
		}

		super.onCreate();
//		Fabric.with(this, new TweetComposer());
		isAppReCreated = true;
		Fabric.with(this, new TweetComposer(), new Crashlytics());

		com.hungama.myplay.activity.util.Logger.e("Hungama APP Config",
				"Hello>>>>>>>>>>");

		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
//		System.out.println(System.currentTimeMillis()
//				+ " :::::::::::::Stratup:::::::::::::Crittercism ");
//
//		try {
//			String appId = "5518fda27fa1f3d21c006311";
//			Crittercism.initialize(getApplicationContext(), appId);
//		} catch (Exception e) {
//			e.printStackTrace();
//
//		} catch (Error e) {
//			e.printStackTrace();
//
//		}
//
//		System.out.println(System.currentTimeMillis()
//				+ " :::::::::::::Stratup:::::::::::::Crittercism finish");

		context = getApplicationContext();
		cacheManager = new CacheManager(context);

		Analytics.init(HungamaApplication.this);
		Logger.s(System.currentTimeMillis()
				+ " :::::::::::::Stratup:::::::::::::Flurry finish");

		try {

			final RichPushNotificationFactory factory = new RichPushNotificationFactory(
					HungamaApplication.this);

			UAirship.takeOff(HungamaApplication.this,
					new UAirship.OnReadyCallback() {
						@Override
						public void onAirshipReady(UAirship airship) {
							// Set the factory
							airship.getPushManager().setPushEnabled(true);
						}
					});
			UAirship.shared().getPushManager().setNotificationFactory(factory);
//			UAirship.shared().getPushManager()
//					.setUserNotificationsEnabled(true);
			// UAirship.shared().getPushManager().setPushEnabled(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		com.hungama.myplay.activity.util.Logger
				.s("Track HungamaApplication finish");
		Logger.s(System.currentTimeMillis()
				+ " :::::::::::::Stratup:::::::::::::UA finish");

		// }
		// }).start();;

		// prefetchImagesIfNotExists();
		// new MoodAsync1().start();
		initVideoCastManager();
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		Logger.e("onTrimMemory", "onTrimMemory" + level);
		if (level != Activity.TRIM_MEMORY_RUNNING_MODERATE
				&& level != Activity.TRIM_MEMORY_MODERATE)
			Utils.clearCache(true);
	}

	/**
	 * Starts prefetching Application images if they are not exist in the cache.
	 */
	// private void prefetchImagesIfNotExists() {
	// Map<String, Object> imageMap = new
	// com.hungama.myplay.activity.data.CacheManager(
	// getBaseContext()).getStoredApplicationImages();
	// ApplicationConfigurations mappConfig = new ApplicationConfigurations(
	// getBaseContext());
	// boolean hasSuccessed = new ApplicationConfigurations(getBaseContext())
	// .hasSuccessedPrefetchingApplicationImages();
	// if (!mappConfig.getApiDate().equals(
	// Utils.getDate(System.currentTimeMillis(), "dd-MM-yy"))) {
	// hasSuccessed = false;
	// }
	// if (imageMap == null || !hasSuccessed) {
	// new ImagePrefetchAsync().start();
	// }
	// }

	private static final int IO_BUFFER_SIZE = 8 * 1024;
//
//	private static final String INTENT_ACTION_PREFETCHING_MOODS_SYNC_EVENT = "com.hungama.myplay.activity.intents.INTENT_ACTION_PREFETCHING_MOODS_SYNC_EVENT";
//	private static final String EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG = "com.hungama.myplay.activity.intents.EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG";
//	private static final String EXTRA_PREFETCHING_MOODS_SYNC_STATE_RUNNING = "com.hungama.myplay.activity.intents.EXTRA_PREFETCHING_MOODS_SYNC_STATE_RUNNING";

	/**
	 * Downloads the images to their directory.
	 */
	public static void downloadBitmapToInternalStorage(
			DiskLruCache mDiskLruCache, String urlString) throws IOException {
		final File cacheFile = new File(mDiskLruCache.createFilePath(urlString));
		final File cacheFileTmp = new File(
				mDiskLruCache.createFilePath(urlString + ".tmp"));
		if (!cacheFile.exists()) {
			if(Logger.enableOkHTTP) {
				BufferedOutputStream out = null;
				OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//				Request.Builder requestBuilder = new Request.Builder();
				try {
					URL url = new URL(urlString);
//					requestBuilder.url(url);
					Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(context, url);
					com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
					final InputStream in = responseOk.body().byteStream();
					out = new BufferedOutputStream(new FileOutputStream(
							cacheFileTmp), IO_BUFFER_SIZE);
					int b;
					while ((b = in.read()) != -1) {
						out.write(b);
					}
					cacheFileTmp.renameTo(cacheFile);
				} catch (Exception e) {
					Logger.printStackTrace(e);
					throw new IOException("Error in downloadBitmap - " + e);
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (final IOException e) {
							Logger.e(TAG, "Error in downloadBitmap - " + e);
						}
					}

					if (cacheFileTmp.exists())
						cacheFileTmp.delete();
				}
			} else {
				HttpURLConnection urlConnection = null;
				BufferedOutputStream out = null;
				try {
					final URL url = new URL(urlString);
					urlConnection = (HttpURLConnection) url.openConnection();
					final InputStream in = new BufferedInputStream(
							urlConnection.getInputStream(), IO_BUFFER_SIZE);
					out = new BufferedOutputStream(new FileOutputStream(
							cacheFileTmp), IO_BUFFER_SIZE);
					int b;
					while ((b = in.read()) != -1) {
						out.write(b);
					}
					cacheFileTmp.renameTo(cacheFile);

				} catch (final Exception e) {
					com.hungama.myplay.activity.util.Logger.e(TAG,
							"Error in downloadBitmap - " + e);
					throw new IOException("Error in downloadBitmap - " + e);
				} finally {
					if (urlConnection != null) {
						urlConnection.disconnect();
					}
					if (out != null) {

						try {
							out.close();
						} catch (final IOException e) {
							Logger.e(TAG, "Error in downloadBitmap - " + e);
						}
					}

					if (cacheFileTmp.exists())
						cacheFileTmp.delete();
				}
			}
		}
	}

//	private static final String TWITTER_KEY = "yq9AnMI2aRF4M4xL1GnNynU92";
//
//	private static final String TWITTER_SECRET = "Qi3w2vrapNnVZIZ0Fk38rR348zpRKUwKlKAMVmhmi7AjYTuNYY";


//	private class MoodAsync1 extends Thread {
//		private DataManager mDataManager;
//
//		private File mImagesFile;
//
//		private String mServiceUrl;
//		private String mAuthKey;
//
//		@Override
//		public void run() {
//			try {
//				android.os.Process
//						.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
//
//				mDataManager = DataManager.getInstance(getApplicationContext());
//				ServerConfigurations serverConfigurations = mDataManager
//						.getServerConfigurations();
//
//				mImagesFile = getDir(DataManager.FOLDER_MOODS_IMAGES,
//						Context.MODE_PRIVATE);
//
//				mServiceUrl = serverConfigurations.getHungamaServerUrl_2();
//				mAuthKey = serverConfigurations.getHungamaAuthKey();
//
//				// removes any existing state of the intent.
//				Intent prefetchingIntentSyncEvent = new Intent(
//						INTENT_ACTION_PREFETCHING_MOODS_SYNC_EVENT);
//				removeStickyBroadcast(prefetchingIntentSyncEvent);
//
//				// resets the intent and broadcasts it again.
//				prefetchingIntentSyncEvent.putExtra(
//						EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG, false);
//				prefetchingIntentSyncEvent.putExtra(
//						EXTRA_PREFETCHING_MOODS_SYNC_STATE_RUNNING, true);
//
//				sendStickyBroadcast(prefetchingIntentSyncEvent);
//
//				boolean hasSuccess = false;
//
//				Logger.d(TAG, "Starts prefetching moods.");
//				// gets the moods from the servers.
//				CommunicationManager communicationManager = new CommunicationManager();
//				try {
//					// stores the discover preferences.
//
//					DiscoverOptionsOperation discoverOptionsOperation = new DiscoverOptionsOperation(
//							mServiceUrl, mDataManager
//									.getApplicationConfigurations()
//									.getPartnerUserId(), mDataManager
//									.getDeviceConfigurations().getHardwareId());
//					Map<String, Object> resultMoodsMap = communicationManager
//							.performOperation(new HungamaWrapperOperation(null,
//									getApplicationContext(),
//									discoverOptionsOperation),
//									getApplicationContext());
//					List<Mood> moods = (List<Mood>) resultMoodsMap
//							.get(DiscoverOptionsOperation.RESULT_KEY_OBJECT_MOODS);
//					// stores the objects in an internal file dir.
//					mDataManager.storeMoods(moods);
//
//					// deletes any existing images
//
//					DiskLruCache mDiskLruCache = DiskLruCache.open(mImagesFile,
//							1, 1, DataManager.CACHE_SIZE_MOODS_IMAGES);
//					// mDiskLruCache.delete();
//
//					// for each mood.
//					for (Mood mood : moods) {
//						if (!TextUtils.isEmpty(mood.getBigImageUrl())) {
//							downloadBitmapToInternalStorage(mDiskLruCache,
//									mood.getBigImageUrl());
//						}
//						if (!TextUtils.isEmpty(mood.getSmallImageUrl())) {
//							downloadBitmapToInternalStorage(mDiskLruCache,
//									mood.getSmallImageUrl());
//						}
//					}
//
//					Logger.d(TAG, "Done prefetching moods.");
//					hasSuccess = true;
//
//					// updates the preferences.
//					mDataManager.getApplicationConfigurations()
//							.setHasSuccessedPrefetchingMoods(hasSuccess);
//
//				} catch (InvalidRequestException e) {
//					e.printStackTrace();
//					Logger.e(TAG, "Failed to prefetch moods.>>" + e);
//				} catch (InvalidResponseDataException e) {
//					e.printStackTrace();
//					Logger.e(TAG, "Failed to prefetch moods.##" + e);
//				} catch (OperationCancelledException e) {
//					e.printStackTrace();
//					Logger.e(TAG, "Failed to prefetch moods.$$" + e);
//				} catch (NoConnectivityException e) {
//					e.printStackTrace();
//					Logger.e(TAG, "Failed to prefetch moods.%%" + e);
//				}
//				// indicates for the success / failure.
//				prefetchingIntentSyncEvent.putExtra(
//						EXTRA_PREFETCHING_MOODS_SYNC_SUCCESS_FLAG, hasSuccess);
//				// indicates it's not running anymore. mDiskLruCache =
//				// DiskLruCache.open(mImagesFile, 1, 1,
//				// DataManager.CACHE_SIZE_MOODS_IMAGES);
//				prefetchingIntentSyncEvent.putExtra(
//						EXTRA_PREFETCHING_MOODS_SYNC_STATE_RUNNING, false);
//				// bang!
//				sendStickyBroadcast(prefetchingIntentSyncEvent);
//			} catch (Exception e) {
//				Logger.e(TAG, "Failed to prefetch moods.**" + e);
//			} catch (Error e) {
//				Logger.e(TAG, "Failed to prefetch moods.((" + e);
//			}
//		}
//	}

	// private class ImagePrefetchAsync extends Thread {
	//
	// private DataManager mDataManager;
	// private ApplicationConfigurations mApplicationConfigurations;
	// private DiskLruCache mDiskLruCache;
	// private File mImagesFile;
	//
	// // private static final int IO_BUFFER_SIZE = 8 * 1024;
	//
	// private String mServiceUrl;
	//
	// @Override
	// public void run() {
	//
	// mDataManager = DataManager.getInstance(getApplicationContext());
	// mApplicationConfigurations = mDataManager
	// .getApplicationConfigurations();
	// ServerConfigurations serverConfigurations = mDataManager
	// .getServerConfigurations();
	//
	// mImagesFile = getDir(DataManager.FOLDER_APPLICATION_IMAGES,
	// Context.MODE_PRIVATE);
	//
	// mServiceUrl = serverConfigurations.getHungamaServerUrl_2();
	//
	// boolean hasSuccess = false;
	//
	// Logger.d(TAG, "Starts prefetching application images.");
	// // gets the moods from the servers.
	// CommunicationManager communicationManager = new CommunicationManager();
	// try {
	// // stores the discover preferences.
	// ApplicationImagesOperation applicationImagesOperation = new
	// ApplicationImagesOperation(
	// mServiceUrl/* , mAuthKey */, getApplicationContext());
	// Map<String, Object> resultMoodsMap = communicationManager
	// .performOperation(new HungamaWrapperOperation(null,
	// getApplicationContext(),
	// applicationImagesOperation),
	// getApplicationContext());
	// try {
	// String texts = resultMoodsMap.get(
	// ApplicationImagesOperation.RESULT_KEY_OBJECT_TEXTS)
	// .toString();
	// Logger.s("Text list ::::::::::: " + texts);
	// if (texts != null) {
	// mDataManager.getApplicationConfigurations()
	// .setApplicationTextList(texts);
	// }
	// mDataManager
	// .getApplicationConfigurations()
	// .setAdRefreshInterval(
	// (Integer) resultMoodsMap
	// .get(ApplicationImagesOperation.RESULT_KEY_OBJECT_ADREFRESH_TIME));
	// mDataManager
	// .getApplicationConfigurations()
	// .setFreeCacheLimit(
	// (Integer) resultMoodsMap
	// .get(ApplicationImagesOperation.RESULT_KEY_OBJECT_FREE_CACHE_LIMIT));
	// mDataManager
	// .getApplicationConfigurations()
	// .setTimeout(
	// (Integer) resultMoodsMap
	// .get(ApplicationImagesOperation.RESULT_KEY_OBJECT_TIME_OUT));
	// mDataManager
	// .getApplicationConfigurations()
	// .setRetry(
	// (Integer) resultMoodsMap
	// .get(ApplicationImagesOperation.RESULT_KEY_OBJECT_RETRY));
	// mDataManager
	// .getApplicationConfigurations()
	// .setSplashAdTimeWait(
	// (Integer) resultMoodsMap
	// .get(ApplicationImagesOperation.RESULT_KEY_OBJECT_SPLASHAD_TIME_WAIT));
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// Map<String, Object> reponseMap = (Map<String, Object>) resultMoodsMap
	// .get(ApplicationImagesOperation.RESULT_KEY_OBJECT_IMAGES);
	// Map<String, Object> reponseMap_Strings = (Map<String, Object>)
	// resultMoodsMap
	// .get(ApplicationImagesOperation.RESULT_KEY_OBJECT_STRINGS);
	// String density = mDataManager.getDisplayDensity();
	// Map<String, Object> densityImageMap = null;
	// StringBuilder str_keys = new StringBuilder();
	// StringBuilder str_values = new StringBuilder();
	// try {
	// Iterator it = reponseMap_Strings.entrySet().iterator();
	// while (it.hasNext()) {
	// Map.Entry pairs = (Map.Entry) it.next();
	// str_keys.append(reponseMap_Strings.get(pairs.getKey())
	// .toString() + "#");
	// str_values.append(pairs.getKey() + "#");
	// }
	// mApplicationConfigurations.setStringUrls(str_keys
	// .toString().substring(0, str_keys.length() - 1));
	// mApplicationConfigurations.setStringValues(str_values
	// .toString().substring(0, str_values.length() - 1));
	// try {
	// Logger.s("before storeDatatoDatabse");
	// DBOHandler.cleanAllStringTable();
	// String[] arrurls = mApplicationConfigurations
	// .getStringUrls().split("#");
	// String[] arrvalues = mApplicationConfigurations
	// .getStringValues().split("#");
	// ArrayList<String> arr_urls = new ArrayList<String>();
	// ArrayList<String> arr_values = new ArrayList<String>();
	//
	// for (int i = 0; i < arrvalues.length; i++) {
	// arr_values.add(arrvalues[i].trim());
	// }
	// for (int i = 0; i < arrurls.length; i++) {
	// arr_urls.add(arrurls[i].trim());
	// parseXML(arr_urls.get(i), arr_values.get(i));
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// // Logger.s("after storeDatatoDatabse");
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// Logger.e("Desity", "" + density);
	// if (density.equalsIgnoreCase("ldpi")) {
	// densityImageMap = (Map<String, Object>) reponseMap
	// .get(ApplicationImagesOperation.KEY_IMAGE_LDPI);
	// } else if (density.equalsIgnoreCase("mdpi")) {
	// densityImageMap = (Map<String, Object>) reponseMap
	// .get(ApplicationImagesOperation.KEY_IMAGE_MDPI);
	// } else if (density.equalsIgnoreCase("hdpi")) {
	// densityImageMap = (Map<String, Object>) reponseMap
	// .get(ApplicationImagesOperation.KEY_IMAGE_HDPI);
	// } else if (density.equalsIgnoreCase("xdpi")) {
	// densityImageMap = (Map<String, Object>) reponseMap
	// .get(ApplicationImagesOperation.KEY_IMAGE_XHDPI);
	// }
	// Map<String, Object> nodpImageMap = (Map<String, Object>) reponseMap
	// .get(ApplicationImagesOperation.KEY_IMAGE_NODPI);
	// // System.out.println("densityImageMap :::: " +
	// // densityImageMap.toString());
	// // System.out.println("nodpImageMap :::: " +
	// // nodpImageMap.toString());
	//
	// Map<String, Object> imageMap = new HashMap<String, Object>();
	// if (densityImageMap != null) {
	// imageMap.putAll(densityImageMap);
	// }
	// if (nodpImageMap != null) {
	// imageMap.putAll(nodpImageMap);
	// }
	// // System.out.println("imageMap :::: " + imageMap.toString());
	// mDataManager.storeApplicationImages(imageMap, new Callback() {
	//
	// @Override
	// public void onResult(Boolean gotResponse) {
	// }
	// });
	//
	// // deletes any existing images
	// mDiskLruCache = DiskLruCache.open(mImagesFile, 1, 1,
	// DataManager.CACHE_SIZE_MOODS_IMAGES);
	// // mDiskLruCache.delete();
	//
	// for (String key : imageMap.keySet()) {
	// Logger.d(TAG, "Done prefetching application images.");
	//
	// String imageUrl = (String) imageMap.get(key);
	// if (!TextUtils.isEmpty(imageUrl)) {
	// downloadBitmapToInternalStorage(mDiskLruCache, imageUrl);
	// }
	// }
	// Logger.d(TAG, "Done prefetching application images.");
	// hasSuccess = true;
	//
	// // updates the preferences.
	// mDataManager
	// .getApplicationConfigurations()
	// .setHasSuccessedPrefetchingApplicationImages(hasSuccess);
	// } catch (InvalidRequestException e) {
	// e.printStackTrace();
	// Logger.e(TAG, "Failed to prefetch application images.");
	// } catch (InvalidResponseDataException e) {
	// e.printStackTrace();
	// Logger.e(TAG, "Failed to prefetch application images.");
	// } catch (OperationCancelledException e) {
	// e.printStackTrace();
	// Logger.e(TAG, "Failed to prefetch application images.");
	// } catch (NoConnectivityException e) {
	// e.printStackTrace();
	// Logger.e(TAG, "Failed to prefetch application images.");
	// } catch (IOException e) {
	// e.printStackTrace();
	// Logger.e(TAG, "Failed to create / delete cache.");
	// } catch (Exception e) {
	// e.printStackTrace();
	// Logger.e(TAG, "Failed to create / delete cache.");
	// }
	// Logger.e(TAG, "images loaded");
	//
	// }
	//
	// }

	// private void parseXML(String urls, String language) {
	// HttpURLConnection urlConnection = null;
	// int IO_BUFFER_SIZE = 8 * 1024;
	// URL url = null;
	// try {
	// url = new URL(urls.trim());
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// }
	// try {
	// urlConnection = (HttpURLConnection) url.openConnection();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// try {
	// final InputStream in = new BufferedInputStream(
	// urlConnection.getInputStream(), IO_BUFFER_SIZE);
	// DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	// DocumentBuilder db;
	// try {
	// db = dbf.newDocumentBuilder();
	// Document doc = db.parse(in);
	// NodeList nodes = doc.getElementsByTagName("string");
	//
	// Logger.s("Start string db :::: " + System.currentTimeMillis());
	// DBOHandler.bulkInsertOneHundredRecords(nodes, language);
	// Logger.s("End string db :::: " + System.currentTimeMillis());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }

	public static String encodeURL(String input) {
		try {
			return URLEncoder.encode(input, "utf-8");
		} catch (Exception e) {
			try {
				return URLEncoder.encode(input);
			} catch (Exception ee) {
			}
		}
		return input;

	}

	public static String encodeURL(String input, String encoding) {

		try {
			return URLEncoder.encode(input, encoding);
		} catch (Exception e) {
			try {
				return URLEncoder.encode(input);
			} catch (Exception ee) {
			}
		}
		return input;

	}

	public static void reset() {
		cacheManager = new CacheManager(context);
	}

	public static CacheManager getCacheManager() {
		return cacheManager;
	}

	public static Context getContext() {
		return context;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Utils.clearCache(true);
	}

	public static boolean isActivityVisible() {
		return activityVisible;
	}

	public static void activityResumed() {
		activityVisible = true;
		com.hungama.myplay.activity.util.Logger
				.s("activityResumed HungamaApplication:" + activityVisible);
	}

	public static void activityPaused() {
		activityVisible = false;
		com.hungama.myplay.activity.util.Logger
				.s("activityPaused HungamaApplication:" + activityVisible);
	}

	private static boolean activityVisible;

	//-------------CromeCast---------------------//

	public void initVideoCastManager(){
//		String applicationId = getString(R.string.crome_cast_app_id);
//
//		// initialize VideoCastManager
//		VideoCastManager.
//				initialize(this, applicationId, VideoCastControllerActivity.class, null).
//				setVolumeStep(VOLUME_INCREMENT).
//				enableFeatures(/*VideoCastManager.FEATURE_NOTIFICATION |*/
//						VideoCastManager.FEATURE_LOCKSCREEN |
//						VideoCastManager.FEATURE_WIFI_RECONNECT |
//						VideoCastManager.FEATURE_CAPTIONS_PREFERENCE |
//						VideoCastManager.FEATURE_DEBUGGING);
//
//		// this is the default behavior but is mentioned to make it clear that it is configurable.
//		VideoCastManager.getInstance().setNextPreviousVisibilityPolicy(
//				VideoCastController.NEXT_PREV_VISIBILITY_POLICY_DISABLED);
//
//		// this is the default behavior but is mentioned to make it clear that it is configurable.
//		VideoCastManager.getInstance().setCastControllerImmersive(true);
//
	}
}
