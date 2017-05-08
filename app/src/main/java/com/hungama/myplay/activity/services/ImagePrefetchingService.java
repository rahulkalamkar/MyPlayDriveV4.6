package com.hungama.myplay.activity.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.text.TextUtils;

import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.operations.hungama.ApplicationImagesOperation;
import com.hungama.myplay.activity.operations.hungama.DiscoverOptionsOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaWrapperOperation;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.images.DiskLruCache;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Background service for prefetching moods and thier images in the
 * application's internal storage for late use.
 */
public class ImagePrefetchingService extends Service {

	private static final String TAG = "ImagePrefetchingService";

	// public static final String INTENT_ACTION_PREFETCHING_IMAGES_SYNC_EVENT =
	// "com.hungama.myplay.activity.intents.INTENT_ACTION_PREFETCHING_IMAGES_SYNC_EVENT";
	// public static final String EXTRA_PREFETCHING_IMAGES_SYNC_SUCCESS_FLAG =
	// "com.hungama.myplay.activity.intents.EXTRA_PREFETCHING_IMAGES_SYNC_SUCCESS_FLAG";
	// public static final String EXTRA_PREFETCHING_IMAGES_SYNC_STATE_RUNNING =
	// "com.hungama.myplay.activity.intents.EXTRA_PREFETCHING_IMAGES_SYNC_STATE_RUNNING";

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private DiskLruCache mDiskLruCache;
	private File mImagesFile;

	private static final int IO_BUFFER_SIZE = 8 * 1024;

	private String mServiceUrl;

	@Override
	public void onCreate() {
		super.onCreate();

		mDataManager = DataManager.getInstance(getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		ServerConfigurations serverConfigurations = mDataManager
				.getServerConfigurations();

		mImagesFile = getDir(DataManager.FOLDER_APPLICATION_IMAGES,
				Context.MODE_PRIVATE);

		mServiceUrl = serverConfigurations.getHungamaServerUrl_2();
		onHandleIntent();
	}

	protected void onHandleIntent() {
		// removes any existing state of the intent.

		ThreadPoolManager.getInstance().submit(new Runnable() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			public void run() {
				Process
						.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

				// Intent prefetchingIntentSyncEvent = new Intent(
				// INTENT_ACTION_PREFETCHING_IMAGES_SYNC_EVENT);
				// removeStickyBroadcast(prefetchingIntentSyncEvent);
				//
				// // resets the intent and broadcasts it again.
				// prefetchingIntentSyncEvent.putExtra(
				// EXTRA_PREFETCHING_IMAGES_SYNC_SUCCESS_FLAG, false);
				// prefetchingIntentSyncEvent.putExtra(
				// EXTRA_PREFETCHING_IMAGES_SYNC_STATE_RUNNING, true);
				//
				// sendStickyBroadcast(prefetchingIntentSyncEvent);

				boolean hasSuccess = false;

				Logger.d(TAG, "Starts prefetching application images.");
				// gets the moods from the servers.
				CommunicationManager communicationManager = new CommunicationManager();
				try {
					// stores the discover preferences.
					ApplicationImagesOperation applicationImagesOperation = new ApplicationImagesOperation(
							mServiceUrl/* , mAuthKey */,
							getApplicationContext(),
							mApplicationConfigurations.getPartnerUserId(),
							mDataManager.getDeviceConfigurations()
									.getHardwareId(), ""
									+ mDataManager
											.getApplicationConfigurations()
											.getAppImagesTimestamp());
					Map<String, Object> resultMoodsMap = communicationManager
							.performOperation(new HungamaWrapperOperation(null,
									getApplicationContext(),
									applicationImagesOperation),
									getApplicationContext());
					int code = (Integer) resultMoodsMap
							.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_CODE);
					if (code == CommunicationManager.RESPONSE_SUCCESS_200) {
						try {
							String texts = resultMoodsMap
									.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_TEXTS)
									.toString();
							Logger.s("Text list ::::::::::: " + texts);
							if (texts != null) {
								mDataManager.getApplicationConfigurations()
										.setApplicationTextList(texts);
							}
							mDataManager
									.getApplicationConfigurations()
									.setAdRefreshInterval(
											(Integer) resultMoodsMap
													.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_ADREFRESH_TIME));
							mDataManager
									.getApplicationConfigurations()
									.setFreeCacheLimit(
											(Integer) resultMoodsMap
													.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_FREE_CACHE_LIMIT));
							mDataManager
									.getApplicationConfigurations()
									.setTimeout(
											(Integer) resultMoodsMap
													.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_TIME_OUT));
							mDataManager
									.getApplicationConfigurations()
									.setRetry(
											(Integer) resultMoodsMap
													.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_RETRY));
							mDataManager
									.getApplicationConfigurations()
									.setSplashAdTimeWait(
											(Integer) resultMoodsMap
													.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_SPLASHAD_TIME_WAIT));
						} catch (Exception e) {
							e.printStackTrace();
						}

						try {
							Map<String, Object> reponseMapAppConfig = (Map<String, Object>) resultMoodsMap
									.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_APP_CONFIG);

							Map<String, Object> reponseMapAppConfigSplashAd = (Map<String, Object>) reponseMapAppConfig
									.get("splash_ad");
							if (reponseMapAppConfigSplashAd
									.containsKey("splash_launch")) {
								String splash_launch = (String) reponseMapAppConfigSplashAd
										.get("splash_launch");
								if (!TextUtils.isEmpty(splash_launch))
									mApplicationConfigurations
											.setAppConfigSplashLaunch(splash_launch
													.equalsIgnoreCase("true") ? true
													: false);
							}
							if (reponseMapAppConfigSplashAd
									.containsKey("splash_relaunch")) {
								String splash_relaunch = (String) reponseMapAppConfigSplashAd
										.get("splash_relaunch");
								if (!TextUtils.isEmpty(splash_relaunch))
									mApplicationConfigurations
											.setAppConfigSplashReLaunch(splash_relaunch
													.equalsIgnoreCase("true") ? true
													: false);
							}
							if (reponseMapAppConfigSplashAd
									.containsKey("splash_unlock")) {
								String splash_unlock = (String) reponseMapAppConfigSplashAd
										.get("splash_unlock");
								if (!TextUtils.isEmpty(splash_unlock))
									mApplicationConfigurations
											.setAppConfigSplashUnlock(splash_unlock
													.equalsIgnoreCase("true") ? true
													: false);
							}
							if (reponseMapAppConfigSplashAd
									.containsKey("splash_maximize")) {
								String splash_maximize = (String) reponseMapAppConfigSplashAd
										.get("splash_maximize");
								if (!TextUtils.isEmpty(splash_maximize))
									mApplicationConfigurations
											.setAppConfigSplashMaximize(splash_maximize
													.equalsIgnoreCase("true") ? true
													: false);
							}
							if (reponseMapAppConfigSplashAd
									.containsKey("splash_refresh_limit")) {
								int splash_refresh_limit = ((Long) reponseMapAppConfigSplashAd
										.get("splash_refresh_limit"))
										.intValue();
								mApplicationConfigurations
										.setAppConfigSplashAdRefreshLimit(splash_refresh_limit);
							}
							if (reponseMapAppConfigSplashAd
									.containsKey("splash_session_limit")) {
								int splash_session_limit = ((Long) reponseMapAppConfigSplashAd
										.get("splash_session_limit"))
										.intValue();
								mApplicationConfigurations
										.setSplashAdCountLimit(splash_session_limit);
							}
							if (reponseMapAppConfigSplashAd
									.containsKey("splash_auto_skip")) {
								int splash_auto_skip = ((Long) reponseMapAppConfigSplashAd
										.get("splash_auto_skip")).intValue();
								mApplicationConfigurations
										.setAppConfigSplashAdAutoSkip(splash_auto_skip);
							}

							Map<String, Object> reponseMapAppConfigPlayerOverlayAd = (Map<String, Object>) reponseMapAppConfig
									.get("player_overlay");
							if (reponseMapAppConfigPlayerOverlayAd
									.containsKey("refresh")) {
								int refresh = ((Long) reponseMapAppConfigPlayerOverlayAd
										.get("refresh")).intValue();
								mApplicationConfigurations
										.setAppConfigPlayerOverlayRefresh(refresh);
							}
							if (reponseMapAppConfigPlayerOverlayAd
									.containsKey("start")) {
								int start = ((Long) reponseMapAppConfigPlayerOverlayAd
										.get("start")).intValue();
								mApplicationConfigurations
										.setAppConfigPlayerOverlayStart(start);
							}
							if (reponseMapAppConfigPlayerOverlayAd
									.containsKey("flip_back_duration")) {
								int flip_back_duration = ((Long) reponseMapAppConfigPlayerOverlayAd
										.get("flip_back_duration")).intValue();
								mApplicationConfigurations
										.setAppConfigPlayerOverlayFlipBackDuration(flip_back_duration);
							}

							Map<String, Object> reponseMapAppConfigAudioAd = (Map<String, Object>) reponseMapAppConfig
									.get("audio_add");
							if (reponseMapAppConfigAudioAd
									.containsKey("audio_frequency")) {
								int audio_frequency = ((Long) reponseMapAppConfigAudioAd
										.get("audio_frequency")).intValue();
								mApplicationConfigurations
										.setAppConfigAudioAdFrequency(audio_frequency);
							}
							if (reponseMapAppConfigAudioAd
									.containsKey("audio_add_rule")) {
								String audio_add_rule = (String) reponseMapAppConfigAudioAd
										.get("audio_add_rule");
								if (!TextUtils.isEmpty(audio_add_rule))
									mApplicationConfigurations
											.setAppConfigAudioAdRule(audio_add_rule);
							}
							if (reponseMapAppConfigAudioAd
									.containsKey("audio_add_session_limit")) {
								int audio_add_session_limit = ((Long) reponseMapAppConfigAudioAd
										.get("audio_add_session_limit"))
										.intValue();
								mApplicationConfigurations
										.setAppConfigAudioAdSessionLimit(audio_add_session_limit);
							}

							Map<String, Object> reponseMapAppConfigVideoAd = (Map<String, Object>) reponseMapAppConfig
									.get("video_ad");
							if (reponseMapAppConfigVideoAd
									.containsKey("video_add_play")) {
								int video_add_play = ((Long) reponseMapAppConfigVideoAd
										.get("video_add_play")).intValue();
								mApplicationConfigurations
										.setAppConfigVideoAdPlay(video_add_play);
							}
							if (reponseMapAppConfigVideoAd
									.containsKey("video_add_session_limit")) {
								int video_add_session_limit = ((Long) reponseMapAppConfigVideoAd
										.get("video_add_session_limit"))
										.intValue();
								mApplicationConfigurations
										.setAppConfigVideoAdSessionLimit(video_add_session_limit);
							}

							if (reponseMapAppConfig.containsKey("refresh_ads")) {
								int refreshAds = ((Long) reponseMapAppConfig
										.get("refresh_ads")).intValue();
								mApplicationConfigurations
										.setAppConfigRefreshAds(refreshAds);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

						try {
							JSONObject reponseMapInAppPrompt = (JSONObject) resultMoodsMap
									.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_INAPP_PROMPT);
							if (reponseMapInAppPrompt != null) {
								mDataManager
										.getCacheManager()
										.storeInAppPromptResponse(
												reponseMapInAppPrompt
														.toJSONString(),
												null);
							}
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}

						Map<String, Object> reponseMap_Strings = (Map<String, Object>) resultMoodsMap
								.get(ApplicationImagesOperation.RESULT_KEY_OBJECT_STRINGS);
						StringBuilder str_keys = new StringBuilder();
						StringBuilder str_values = new StringBuilder();
						try {
							Iterator it = reponseMap_Strings.entrySet()
									.iterator();
							while (it.hasNext()) {
								Map.Entry pairs = (Map.Entry) it.next();
								str_keys.append(reponseMap_Strings.get(
										pairs.getKey()).toString()
										+ "#");
								str_values.append(pairs.getKey() + "#");

							}
							mApplicationConfigurations.setStringUrls(str_keys
									.toString().substring(0,
											str_keys.length() - 1));
							mApplicationConfigurations
									.setStringValues(str_values.toString()
											.substring(0,
													str_values.length() - 1));
							storeDatatoDatabse();
						} catch (Exception e) {
							e.printStackTrace();
						}
						Logger.d(TAG, "Done prefetching application images.");
						hasSuccess = true;
					}
					// updates the preferences.
					if (code == CommunicationManager.RESPONSE_SUCCESS_200
							|| code == CommunicationManager.RESPONSE_CONTENT_NOT_MODIFIED_304) {
						mDataManager.getApplicationConfigurations()
								.setHasSuccessedPrefetchingApplicationImages(
										hasSuccess);
					}
				} catch (InvalidRequestException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed to prefetch application images.");
				} catch (InvalidResponseDataException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed to prefetch application images.");
				} catch (OperationCancelledException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed to prefetch application images.");
				} catch (NoConnectivityException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed to prefetch application images.");
				} catch (Exception e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed to create / delete cache.");
				}

				ThreadPoolManager.getInstance().submit(new MoodAsyncApiCall());



				// indicates for the success / failure.
				// prefetchingIntentSyncEvent.putExtra(
				// EXTRA_PREFETCHING_IMAGES_SYNC_SUCCESS_FLAG, hasSuccess);
				// // indicates it's not running anymore.
				// prefetchingIntentSyncEvent.putExtra(
				// EXTRA_PREFETCHING_IMAGES_SYNC_STATE_RUNNING, false);
				// // bang!
				// sendStickyBroadcast(prefetchingIntentSyncEvent);

			}

		});

	}

	private class MoodAsyncApiCall extends Thread {
		private DataManager mDataManager;

		private DiskLruCache mDiskLruCache;
		private File mImagesFile;

		private String mServiceUrl;

		// private String mAuthKey;

		@Override
		public void run() {
			try {
//				getActivity().runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						if (rootView != null
//								&& rootView.findViewById(R.id.progressBar_init) != null)
//							rootView.findViewById(R.id.progressBar_init)
//									.setVisibility(View.VISIBLE);
//					}
//				});

				mDataManager = DataManager.getInstance(ImagePrefetchingService.this);
				ServerConfigurations serverConfigurations = mDataManager
						.getServerConfigurations();

				mImagesFile = getDir(
						DataManager.FOLDER_MOODS_IMAGES, Context.MODE_PRIVATE);

				mServiceUrl = serverConfigurations.getHungamaServerUrl_2();
				// mAuthKey = serverConfigurations.getHungamaAuthKey();

				boolean hasSuccess = false;

				Logger.d(TAG, "Starts prefetching moods.");
				// gets the moods from the servers.
				CommunicationManager communicationManager = new CommunicationManager();
//				try {
				// stores the discover preferences.
				DiscoverOptionsOperation discoverOptionsOperation = new DiscoverOptionsOperation(
						mServiceUrl, mDataManager
						.getApplicationConfigurations()
						.getPartnerUserId(), mDataManager
						.getDeviceConfigurations().getHardwareId());
				Map<String, Object> resultMoodsMap = communicationManager
						.performOperation(new HungamaWrapperOperation(null,
										ImagePrefetchingService.this, discoverOptionsOperation),
								ImagePrefetchingService.this);
				List<Mood> moods = (List<Mood>) resultMoodsMap
						.get(DiscoverOptionsOperation.RESULT_KEY_OBJECT_MOODS);
				// stores the objects in an internal file dir.
				mDataManager.storeMoods(moods);

				// deletes any existing images
				mDiskLruCache = DiskLruCache.open(mImagesFile, 1, 1,
						DataManager.CACHE_SIZE_MOODS_IMAGES);
				// mDiskLruCache.delete();

				// for each mood.
				for (Mood mood : moods) {
					if (!TextUtils.isEmpty(mood.getBigImageUrl())) {
						HungamaApplication.downloadBitmapToInternalStorage(
								mDiskLruCache, mood.getBigImageUrl());
					}
					if (!TextUtils.isEmpty(mood.getSmallImageUrl())) {
						HungamaApplication.downloadBitmapToInternalStorage(
								mDiskLruCache, mood.getSmallImageUrl());
					}
				}

				Logger.d(TAG, "Done prefetching moods.");
				hasSuccess = true;

				// updates the preferences.
				mDataManager.getApplicationConfigurations()
						.setHasSuccessedPrefetchingMoods(hasSuccess);

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
//				} catch (IOException e) {
//					e.printStackTrace();
//					Logger.e(TAG, "Failed to create / delete cache.&&" + e);
//				}

			} catch (InvalidRequestException e) {
				e.printStackTrace();
				Logger.e(TAG, "Failed to prefetch moods.>>" + e);
			} catch (InvalidResponseDataException e) {
				e.printStackTrace();
				Logger.e(TAG, "Failed to prefetch moods.##" + e);
			} catch (OperationCancelledException e) {
				e.printStackTrace();
				Logger.e(TAG, "Failed to prefetch moods.$$" + e);
			} catch (NoConnectivityException e) {
				e.printStackTrace();
				Logger.e(TAG, "Failed to prefetch moods.%%" + e);
			} catch (IOException e) {
				e.printStackTrace();
				Logger.e(TAG, "Failed to create / delete cache.&&" + e);
			} catch (Exception e) {
				Logger.e(TAG, "Failed to prefetch moods.**" + e);
			} catch (Error e) {
				Logger.e(TAG, "Failed to prefetch moods.((" + e);
			}
			stopSelf();
		}
	}

	private void storeDatatoDatabse() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					Logger.s("before storeDatatoDatabse");
					DBOHandler.cleanAllStringTable();
					String[] arrurls = mApplicationConfigurations
							.getStringUrls().split("#");
					String[] arrvalues = mApplicationConfigurations
							.getStringValues().split("#");
					ArrayList<String> arr_urls = new ArrayList<String>();
					ArrayList<String> arr_values = new ArrayList<String>();

					for (int i = 0; i < arrvalues.length; i++) {
						arr_values.add(arrvalues[i].trim());
					}
					for (int i = 0; i < arrurls.length; i++) {
						arr_urls.add(arrurls[i].trim());
						parseXML(arr_urls.get(i), arr_values.get(i));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				// Logger.s("after storeDatatoDatabse");
			}
		});
		t.start();
	}

	private void parseXML(String urls, String language) {
		if(Logger.enableOkHTTP){
			OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//			Request.Builder requestBuilder = new Request.Builder();
			try {
				URL url = new URL(urls.trim());
				client.setCache(new Cache(getCacheDir(), 20 * 1024 * 1024L));
//				requestBuilder.url(url);
				Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(ImagePrefetchingService.this, url);
				com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
				final InputStream in = responseOk.body().byteStream();
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db;
				try {
					db = dbf.newDocumentBuilder();
					Document doc = db.parse(in);
					NodeList nodes = doc.getElementsByTagName("string");

					Logger.s("Start string db :::: " + System.currentTimeMillis());
					DBOHandler.bulkInsertOneHundredRecords(nodes, language);
					Logger.s("End string db :::: " + System.currentTimeMillis());

					// for (int i = 0; i < nodes.getLength(); i++) {
					// Element element = (Element) nodes.item(i);
					// DBOHandler.saveToDB(element.getAttribute("name"),
					// getCharacterDataFromElement(element), language);
					// }
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
				if (in != null)
					in.close();
			} catch (MalformedURLException e) {
				Logger.printStackTrace(e);
			} catch (IOException e) {
				Logger.printStackTrace(e);
			}
		} else {
			HttpURLConnection urlConnection = null;
			int IO_BUFFER_SIZE = 8 * 1024;
			URL url = null;
			try {
				url = new URL(urls.trim());
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				urlConnection = (HttpURLConnection) url.openConnection();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				final InputStream in = new BufferedInputStream(
						urlConnection.getInputStream(), IO_BUFFER_SIZE);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db;
				try {
					db = dbf.newDocumentBuilder();
					Document doc = db.parse(in);
					NodeList nodes = doc.getElementsByTagName("string");

					Logger.s("Start string db :::: " + System.currentTimeMillis());
					DBOHandler.bulkInsertOneHundredRecords(nodes, language);
					Logger.s("End string db :::: " + System.currentTimeMillis());

					// for (int i = 0; i < nodes.getLength(); i++) {
					// Element element = (Element) nodes.item(i);
					// DBOHandler.saveToDB(element.getAttribute("name"),
					// getCharacterDataFromElement(element), language);
					// }
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (in != null)
					in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (urlConnection != null)
					urlConnection.disconnect();
				urlConnection = null;
			}
		}
	}

	// public static String getCharacterDataFromElement(Element e) {
	// Node child = e.getFirstChild();
	// if (child instanceof CharacterData) {
	// CharacterData cd = (CharacterData) child;
	// return cd.getData();
	// }
	// return "?";
	// }

	// private synchronized boolean saveToDB(String name, String value,
	// String language) {
	// DataBase db = DataBase.getInstance(HungamaApplication.getContext());
	// db.open();
	// long rowId = -1;
	// try {
	//
	// rowId = db.insert(DataBase.All_String_Values_table,
	// DataBase.All_Strings_int, new String[] { name, value,
	// language });
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// db.close();
	// // System.out.println(rowId + " : " + language);
	// return rowId != -1;
	// }
	/**
	 * Downloads the images to their directory.
	 */
	private void downloadBitmapToInternalStorage(String urlString)
			throws IOException {
		final File cacheFile = new File(mDiskLruCache.createFilePath(urlString));
		Logger.e(TAG, "downloadBitmapToInternalStorage" + urlString);
		if (cacheFile.exists())
			cacheFile.delete();
		cacheFile.createNewFile();
		if(Logger.enableOkHTTP){
			BufferedOutputStream out = null;
			try {
				final URL url = new URL(urlString);
				OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//				Request.Builder requestBuilder = new Request.Builder();
//				requestBuilder.url(url);
				Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(ImagePrefetchingService.this, url);
				com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
				final InputStream in = responseOk.body().byteStream();
				out = new BufferedOutputStream(new FileOutputStream(cacheFile),
						IO_BUFFER_SIZE);

				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				if (in != null)
					in.close();
			} catch (final IOException e) {
				Logger.e(TAG, "Error in downloadBitmap - " + e);
				throw new IOException("Error in downloadBitmap - " + e);
			} catch (final Exception e) {
				Logger.e(TAG, "Error in downloadBitmap - " + e);
				throw new IOException("Error in downloadBitmap - " + e);
			} catch (final Error e) {
				Logger.e(TAG, "Error in downloadBitmap - " + e);
				throw new IOException("Error in downloadBitmap - " + e);
			} finally {
				if (out != null) {
					try {
						out.close();
					} catch (final IOException e) {
						Logger.e(TAG, "Error in downloadBitmap - " + e);
					}
				}
			}
		} else {
			HttpURLConnection urlConnection = null;
			BufferedOutputStream out = null;
			try {
				final URL url = new URL(urlString);
				urlConnection = (HttpURLConnection) url.openConnection();
				final InputStream in = new BufferedInputStream(
						urlConnection.getInputStream(), IO_BUFFER_SIZE);
				out = new BufferedOutputStream(new FileOutputStream(cacheFile),
						IO_BUFFER_SIZE);

				int b;
				while ((b = in.read()) != -1) {
					out.write(b);
				}
				if (in != null)
					in.close();
			} catch (final IOException e) {
				Logger.e(TAG, "Error in downloadBitmap - " + e);
				throw new IOException("Error in downloadBitmap - " + e);
			} catch (final Exception e) {
				Logger.e(TAG, "Error in downloadBitmap - " + e);
				throw new IOException("Error in downloadBitmap - " + e);
			} catch (final Error e) {
				Logger.e(TAG, "Error in downloadBitmap - " + e);
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
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
//		System.gc();
		super.onDestroy();
	}
}
