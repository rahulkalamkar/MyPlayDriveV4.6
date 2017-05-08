package com.hungama.myplay.activity.services;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemClock;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Campaign;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.CampaignCreateOperation;
import com.hungama.myplay.activity.operations.catchmedia.CampaignListCreateOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.util.AlaramReceiver;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Background service for prefetching the campagins.
 */
public class CampaignsPreferchingService extends Service {
	// public static boolean isFinished = false;
	// public static boolean isRunning = false;
	private static final String TAG = "CampaignsPreferchingService";

	// private final String TimeCampaign = "TimeCampaign";
	private static final String PrefTimeCampaign = "PrefTimeCampaign";
	private static SharedPreferences pref;
	private static boolean isProcessRunning = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onCreate()
	 */
	@Override
	public void onCreate() {
		Logger.s(" ::::::::::::::-- onCreate");
		pref = getSharedPreferences("TimeCampaign", MODE_PRIVATE);
		super.onCreate();

		long duration = ApplicationConfigurations.getInstance(this)
				.getAppConfigRefreshAds() * 1000;// (30 * 60000L)
		Logger.s(isProcessRunning + " ::::::::::::::-- onCreate " + duration);
		if (isAppRunning()
				&& !isProcessRunning
				&& (System.currentTimeMillis()
						- pref.getLong(PrefTimeCampaign, 0) >= duration))
			new LoadCampaign().execute();
		else
			stopSelf();
	}

	class LoadCampaign implements Runnable {
		@Override
		public void run() {
			doInBackground();
		}

		public void execute() {
			new Thread(this).start();
		}

		protected Boolean doInBackground() {
			isProcessRunning = true;
			Logger.s(" ::::::::::::::-- onCreate doInBackground");
			Logger.i(TAG, "Start prefetching campaigns.");
			try {
				// OnApplicationStartsActivity.isFreshLaunch = false;
				android.os.Process
						.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

				Context applicationContext = getApplicationContext();
				DataManager dataManager = DataManager
						.getInstance(applicationContext);

				ServerConfigurations serverConfigurations = dataManager
						.getServerConfigurations();

				CommunicationManager communicationManager = new CommunicationManager();

				try {
					// gets the list of the campains.
					Map<String, Object> listResult = communicationManager
							.performOperation(new CMDecoratorOperation(
									serverConfigurations.getServerUrl(),
									new CampaignListCreateOperation(
											applicationContext)),
									applicationContext);

					// if (listResult != null
					// && listResult
					// .containsKey(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_APP_CONFIG))
					// {
					// try {
					// ApplicationConfigurations mApplicationConfigurations =
					// dataManager
					// .getApplicationConfigurations();
					// Map<String, Object> reponseMapAppConfig = (Map<String,
					// Object>) listResult
					// .get(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_APP_CONFIG);
					//
					// // Map<String, Object> reponseMapAppConfigSplashAd =
					// (Map<String, Object>) reponseMapAppConfig
					// // .get("splash_ad");
					// if (reponseMapAppConfig
					// .containsKey("splash_launch")) {
					// boolean splash_launch = (Boolean) reponseMapAppConfig
					// .get("splash_launch");
					// mApplicationConfigurations
					// .setAppConfigSplashLaunch(splash_launch);
					// }
					// if (reponseMapAppConfig
					// .containsKey("splash_relaunch")) {
					// boolean splash_relaunch = (Boolean) reponseMapAppConfig
					// .get("splash_relaunch");
					// mApplicationConfigurations
					// .setAppConfigSplashReLaunch(splash_relaunch);
					// }
					// if (reponseMapAppConfig
					// .containsKey("splash_unlock")) {
					// boolean splash_unlock = (Boolean) reponseMapAppConfig
					// .get("splash_unlock");
					// mApplicationConfigurations
					// .setAppConfigSplashUnlock(splash_unlock);
					// }
					// if (reponseMapAppConfig
					// .containsKey("splash_maximize")) {
					// boolean splash_maximize = (Boolean) reponseMapAppConfig
					// .get("splash_maximize");
					// mApplicationConfigurations
					// .setAppConfigSplashMaximize(splash_maximize);
					// }
					// if (reponseMapAppConfig
					// .containsKey("splash_next")) {
					// int splash_refresh_limit = ((Long) reponseMapAppConfig
					// .get("splash_next")).intValue();
					// mApplicationConfigurations
					// .setAppConfigSplashAdRefreshLimit(splash_refresh_limit);
					// }
					// if (reponseMapAppConfig
					// .containsKey("splash_count")) {
					// int splash_session_limit = ((Long) reponseMapAppConfig
					// .get("splash_count")).intValue();
					// mApplicationConfigurations
					// .setSplashAdCountLimit(splash_session_limit);
					// }
					// if (reponseMapAppConfig
					// .containsKey("splash_delay")) {
					// int splash_auto_skip = ((Long) reponseMapAppConfig
					// .get("splash_delay")).intValue();
					// mApplicationConfigurations
					// .setAppConfigSplashAdAutoSkip(splash_auto_skip);
					// }
					//
					// // Map<String, Object> reponseMapAppConfigPlayerOverlayAd
					// = (Map<String, Object>) reponseMapAppConfig
					// // .get("player_overlay");
					// if (reponseMapAppConfig
					// .containsKey("art_overlay_freq")) {
					// int refresh = ((Long) reponseMapAppConfig
					// .get("art_overlay_freq")).intValue();
					// mApplicationConfigurations
					// .setAppConfigPlayerOverlayRefresh(refresh);
					// }
					// if (reponseMapAppConfig
					// .containsKey("art_overlay_start")) {
					// int start = ((Long) reponseMapAppConfig
					// .get("art_overlay_start")).intValue();
					// mApplicationConfigurations
					// .setAppConfigPlayerOverlayStart(start);
					// }
					// // if (reponseMapAppConfig
					// // .containsKey("art_overlay_count")) {
					// // int start = ((Long) reponseMapAppConfig
					// // .get("art_overlay_count")).intValue();
					// // }
					//
					// // Map<String, Object> reponseMapAppConfigAudioAd =
					// (Map<String, Object>) reponseMapAppConfig
					// // .get("audio_add");
					// if (reponseMapAppConfig
					// .containsKey("audio_ad_min_length")) {
					// int audio_frequency = ((Long) reponseMapAppConfig
					// .get("audio_ad_min_length")).intValue();
					// mApplicationConfigurations
					// .setAppConfigAudioAdFrequency(audio_frequency);
					// }
					// if (reponseMapAppConfig
					// .containsKey("audio_ad_freq")) {
					// String audio_add_rule = (String) reponseMapAppConfig
					// .get("audio_ad_freq");
					// mApplicationConfigurations
					// .setAppConfigAudioAdRule(audio_add_rule);
					// }
					// if (reponseMapAppConfig
					// .containsKey("audio_ad_count")) {
					// int audio_add_session_limit = ((Long) reponseMapAppConfig
					// .get("audio_ad_count")).intValue();
					// mApplicationConfigurations
					// .setAppConfigAudioAdSessionLimit(audio_add_session_limit);
					// }
					//
					// if (reponseMapAppConfig.containsKey("campaign_refresh"))
					// {
					// int refreshAds = ((Long) reponseMapAppConfig
					// .get("campaign_refresh")).intValue();
					// mApplicationConfigurations
					// .setAppConfigRefreshAds(refreshAds);
					// }
					// } catch (Exception e) {
					// e.printStackTrace();
					// }
					// }

					if (listResult != null
							&& listResult
									.containsKey(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST)) {

						// stores the list of campaigns.
						List<String> campaignList = (List<String>) listResult
								.get(CampaignListCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN_LIST);

						// List<String> storedCampaign = dataManager
						// .getStoredCampaignList();

						if (campaignList == null)
							campaignList = new ArrayList<String>();

						dataManager.storeCampaignList(campaignList);

						if (!campaignList.isEmpty()) {
							List<Campaign> campaigns = new ArrayList<Campaign>();
							Map<String, Object> campaignsResult;
							// List<String> tempCampaignList = new
							// ArrayList<String>();
							// ;
							// for (String campaign : campaignList) {
							// tempCampaignList.clear();
							// tempCampaignList.add(campaign);
							// gets the campaigns themselves.
							campaignsResult = communicationManager
									.performOperation(
											new CMDecoratorOperation(
													serverConfigurations
															.getServerUrl(),
													new CampaignCreateOperation(
															applicationContext,
															campaignList)),
											applicationContext);

							if (campaignsResult != null
									&& campaignsResult
											.containsKey(CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN)) {
								// List<Campaign> campaigns =
								// (List<Campaign>)
								// campaignsResult
								// .get(CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN);
								// if (!Utils.isListEmpty(campaigns)) {
								//
								// // stores the campaigns for case
								// // use.
								// dataManager
								// .storeCampaign(campaigns);
								//
								// // extracts the placements from the
								// // campaigns.
								// // List<Placement> radioPlacements =
								// CampaignsManager
								// // .getAllPlacementsOfType(
								// // campaigns,
								// // ForYouActivity.PLACEMENT_TYPE_RADIO);
								// // List<Placement> splashPlacements =
								// CampaignsManager
								// // .getAllPlacementsOfType(
								// // campaigns,
								// // ForYouActivity.PLACEMENT_TYPE_SPLASH);
								// //
								// // // Store the placements.
								// // dataManager
								// // .storeRadioPlacement(radioPlacements);
								// // dataManager
								// //
								// .storeSplashPlacement(splashPlacements);
								//
								// try{
								// Log.i(TAG,
								// "Done prefetching Campaigns! " +
								// campaigns.get(0).getPlacements().get(0).getPlacementType());
								// } catch(Exception e){
								// Logger.printStackTrace(e);
								// }
								// Logger.i(TAG,
								// "Done prefetching Campaigns!");
								//
								// } else {
								// Logger.e(TAG,
								// "Campaign list is empty!");
								// }

								if (Utils.isListEmpty(campaigns)) {
									campaigns = (List<Campaign>) campaignsResult
											.get(CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN);
								} else {
									campaigns
											.addAll((List<Campaign>) campaignsResult
													.get(CampaignCreateOperation.RESPONSE_KEY_OBJECT_CAMPAIGN));
								}
								/*
								 * try { Log.i(TAG,
								 * "Done prefetching Campaigns! " +
								 * ((List<Campaign>) campaignsResult
								 * .get(CampaignCreateOperation
								 * .RESPONSE_KEY_OBJECT_CAMPAIGN)) .get(0)
								 * .getPlacements() .get(0)
								 * .getPlacementType()); } catch (Exception e) {
								 * Logger.printStackTrace(e); }
								 */
							} else {
								Logger.e(TAG, "Campaign list is empty!");
							}
							// }

							if (!Utils.isListEmpty(campaigns)) {

								// stores the campaigns for case
								// use.
								dataManager.storeCampaign(campaigns, null);
								pref.edit()
										.putLong(PrefTimeCampaign,
												System.currentTimeMillis())
										.commit();
								try {
									Logger.i(TAG, "Done prefetching Campaigns! "
											+ campaigns.get(0).getPlacements()
													.get(0).getPlacementType());
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
								Logger.i(TAG, "Done prefetching Campaigns!");

							} else {
								dataManager.storeCampaign(
										new ArrayList<Campaign>(), null);
								pref.edit()
										.putLong(PrefTimeCampaign,
												System.currentTimeMillis())
										.commit();
								Logger.e(TAG, "Campaign list is empty!");
							}
						} else {
							dataManager.storeCampaign(
									new ArrayList<Campaign>(), null);
							pref.edit()
									.putLong(PrefTimeCampaign,
											System.currentTimeMillis())
									.commit();
							Logger.e(TAG, "Campaign list is empty!");
						}
					} else {
						dataManager.storeCampaignList(new ArrayList<String>());
						dataManager.storeCampaign(new ArrayList<Campaign>(),
								null);
						pref.edit()
								.putLong(PrefTimeCampaign,
										System.currentTimeMillis()).commit();
						Logger.e(TAG, "Campaign list is empty!");
					}
					Logger.s(" ::::::::::::::::::- - ACTION_NOTIFY_ADAPTER");
					Intent new_intent = new Intent();
					new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
					sendBroadcast(new_intent);
				} catch (InvalidRequestException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed prefetching campaigns!");
				} catch (InvalidResponseDataException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed prefetching campaigns!");
				} catch (OperationCancelledException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed prefetching campaigns!");
				} catch (NoConnectivityException e) {
					e.printStackTrace();
					Logger.e(TAG, "Failed prefetching campaigns!");
				}
			} catch (Exception e) {

			} catch (Error e) {

			}
			// isRunning = false;
			// isFinished = true;

			isProcessRunning = false;
			stopSelf();
			// }
			// }.start();
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Logger.s(" ::::::::::::::-- onDestroy");
		if (isAppRunning()) {
			AlarmManager alarmMgr;
			PendingIntent alarmIntent;
			alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(this, AlaramReceiver.class);
			alarmIntent = PendingIntent.getBroadcast(this, 0, intent,
					PendingIntent.FLAG_ONE_SHOT);

			alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime()
							+ ApplicationConfigurations.getInstance(this)
									.getAppConfigRefreshAds() * 1000,
					alarmIntent);
			// + 2 * 1000, alarmIntent); // For Testing
		}

//		System.gc();
		super.onDestroy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public static final void resetPrefTimeCampaign(Context context) {
		if (pref == null) {
			pref = context.getSharedPreferences("TimeCampaign", MODE_PRIVATE);
		}
		pref.edit().putLong(PrefTimeCampaign, 0).commit();
	}

	public static final long getPrefTimeCampaign(Context context) {
		if (pref == null) {
			pref = context.getSharedPreferences("TimeCampaign", MODE_PRIVATE);
		}
		return pref.getLong(PrefTimeCampaign, 0);
	}

	@SuppressWarnings("deprecation")
	private boolean isAppRunning() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> procInfos = activityManager.getRunningTasks(1);
		boolean running = false;
		// Logger.s(" ::::::::::::::-- AlaramReceiver " + procInfos.size());
		for (int i = 0; i < procInfos.size(); i++) {
			ComponentName componentInfo = procInfos.get(0).topActivity;
			// Logger.s(" ::::::::::::::-- AlaramReceiver " +
			// componentInfo.getPackageName());
			if (componentInfo.getPackageName().equals(
					"com.hungama.myplay.activity")) {
				running = true;
				break;
			}
		}
		Logger.s((PlayerService.service != null)
				+ " ::::::::::::::-- AlaramReceiver " + running + " :: "
				+ ScreenLockStatus.isHomePressed());
		return ((PlayerService.service != null) || running || ScreenLockStatus
				.isHomePressed());
	}
}