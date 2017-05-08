package com.hungama.myplay.activity.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.text.TextUtils;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CacheManager.Callback;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.inventory.InventoryLightOperation;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.util.Logger;

/**
 * Background service for getting Catchmedia's InventoryLight
 */
public class InventoryLightService extends Service {

	public static void callService(Context mContext, boolean isRealUser) {
		// if(isRealUser){
		mContext.getApplicationContext().startService(
				new Intent(mContext.getApplicationContext(),
						InventoryLightService.class));

		// }
		// mDataManager = DataManager.getInstance(getApplicationContext());
		// mServerConfigurations = mDataManager.getServerConfigurations();
	}

	private static final String TAG = "InventoryLightService";
	private static final String LAST_PAGE = "last_page";
	private static final String DATA = "data";
	private static final String ACTION = "action";
	private static final String ID = "id";
	private static final String NAME = "name";

	// Actions type
	public static final String ADD = "Add";
	public static final String DEL = "Del";
	public static final String MOD = "Mod";

	private static final String TRACKS = "tracks";
	private static final String PLAYLISTS = "playlists";

	private DataManager mDataManager;
	private ServerConfigurations mServerConfigurations;
	protected ApplicationConfigurations pApplicationConfigurations;

	@Override
	public void onCreate() {
		super.onCreate();
		// isRunning = false;
		Logger.i(TAG, "onCreate InventoryLight Service.");
		mDataManager = DataManager.getInstance(getApplicationContext());
		mServerConfigurations = ServerConfigurations
				.getInstance(getApplicationContext());
		pApplicationConfigurations = ApplicationConfigurations
				.getInstance(getApplicationContext());
		// if(pApplicationConfigurations.isRealUser())
		if (isAppRunning())
			onHandleIntent();
		else
			stopSelf();

	}

	// static boolean isRunning = false;

	protected void onHandleIntent() {
		Logger.i(TAG, "Starts InventoryLight Service.");

		ThreadPoolManager.getInstance().submit(new Runnable() {

			@Override
			public void run() {
				try {

					android.os.Process
							.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

					CommunicationManager communicationManager = new CommunicationManager();

					int consumerRevision = 0;
					int consumerServerRevision = 0;

					int householdRevision = 0;
					int householdServerRevision = 0;

					boolean lastPage = true;
					Map<String, Object> resultMap;
					Map<String, Object> meta;
					if (pApplicationConfigurations.getSessionID() != null
							&& pApplicationConfigurations.getSessionID()
									.length() > 0) {
						do {

							try {
								resultMap = communicationManager
										.performOperation(
												new CMDecoratorOperation(
														mServerConfigurations
																.getServerUrl(),
														new InventoryLightOperation(
																InventoryLightService.this)),
												getApplicationContext());
								// Update Data
								updateInventory(resultMap);

								// Assign revisions
								meta = (Map<String, Object>) resultMap
										.get("meta");
								consumerRevision = ((Long) meta
										.get(ApplicationConfigurations.CONSUMER_REVISION))
										.intValue();
								consumerServerRevision = ((Long) meta
										.get(ApplicationConfigurations.CONSUMER_SERVER_REVISION))
										.intValue();

								householdRevision = ((Long) meta
										.get(ApplicationConfigurations.HOUSEHOLD_REVISION))
										.intValue();
								householdServerRevision = ((Long) meta
										.get(ApplicationConfigurations.HOUSEHOLD_SERVER_REVISION))
										.intValue();

								lastPage = (Boolean) meta.get(LAST_PAGE);

								pApplicationConfigurations
										.setConsumerRevision(consumerRevision);
								pApplicationConfigurations
										.setHouseholdRevision(householdRevision);
//								System.gc();
							} catch (InvalidRequestException e) {
								e.printStackTrace();
								Logger.e(TAG, "Failed in InventoryLight.");
							} catch (InvalidResponseDataException e) {
								e.printStackTrace();
								Logger.e(TAG, "Failed InventoryLight.");
							} catch (OperationCancelledException e) {
								e.printStackTrace();
								Logger.e(TAG, "Failed InventoryLight.");
							} catch (NoConnectivityException e) {
								e.printStackTrace();
								Logger.e(TAG, "Failed InventoryLight.");
							}
							Logger.i(TAG,
									"lastPage consumerRevision consumerServerRevision"
											+ lastPage + ":" + consumerRevision
											+ ":" + consumerServerRevision);

						} while ((consumerRevision < consumerServerRevision)
								&& (householdRevision < householdServerRevision)
								&& lastPage);
					}
					ApplicationConfigurations.getInstance(
							getApplicationContext()).setIsInventoryFetch(true);
				} catch (Exception e) {
					e.printStackTrace();
				} catch (Error e) {
					e.printStackTrace();
				}
				Logger.i(TAG, "Done InventoryLight Service.");

				stopSelf();
			}

		});

	}

	public boolean updateInventory(Map<String, Object> resultMap) {
		Logger.i(TAG, "updateInventory");

		Map<String, ArrayList<Map<String, Object>>> data = (Map<String, ArrayList<Map<String, Object>>>) resultMap
				.get(DATA);

		boolean updateResult = true;
		boolean tmp;

		ArrayList<Map<String, Object>> playlists = data.get(PLAYLISTS);

		if (playlists != null && !playlists.isEmpty()) {
			tmp = updateMediaItem(playlists, PLAYLISTS);
			if (!tmp) {
				updateResult = false;
			}
		}

		ArrayList<Map<String, Object>> tracks = data.get(TRACKS);

		if (tracks != null && !tracks.isEmpty()) {
			updateTracks(tracks, TRACKS, null);
		}

		return updateResult;

	}

	public Playlist getItemableType(String type) {

		if (type.equalsIgnoreCase(PLAYLISTS)) {
			return new Playlist();
		} else {
			return null;
		}
	}

	/**
	 * Insert/Delete/Update MediaItem PlayLists in cache
	 * 
	 * @param arr
	 */
	public boolean updateMediaItem(ArrayList<Map<String, Object>> arr,
			String type) {

		boolean updateResult = true;

		if (arr != null && !arr.isEmpty()) {

			Playlist itemable = getItemableType(type);

			for (Map<String, Object> map : arr) {

				if (map != null && !map.isEmpty()) {

					String action = (String) map.get(ACTION);

					if (action == null) {
						// If there is no Action then "Add" is the default
						// action
						action = ADD;
					}

					// boolean tmp = true;
					if (itemable instanceof Playlist) {
						// CatchMedia PlayList
						itemable = itemable.getInitializedObject(map);
						mDataManager.updateItemable((Playlist) itemable,
								action, null);
					}

					// if (!tmp) {
					updateResult = false;
					// }
				}
			}
		}

		return updateResult;
	}

	public void updateTracks(ArrayList<Map<String, Object>> arr, String type,
			Callback callback) {

		if (arr != null && !arr.isEmpty()) {
			// System.out.println("Total size ::::: " + arr.size());
			for (Map<String, Object> map : arr) {

				if (map != null && !map.isEmpty()) {

					String trackID = (String) map.get(ID);
					String trackName = (String) map.get(NAME);

					if (!TextUtils.isEmpty(trackID)) {
						mDataManager.updateTracks(trackID, trackName, callback);
					}
				}
			}
		}
		// return updateResult;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Logger.i(TAG, "onDestroy InventoryLight Service.");
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
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("deprecation")
	private boolean isAppRunning() {
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> procInfos = activityManager.getRunningTasks(1);
		boolean running = false;
		for (int i = 0; i < procInfos.size(); i++) {
			ComponentName componentInfo = procInfos.get(0).topActivity;
			if (componentInfo.getPackageName().equals(
					"com.hungama.myplay.activity")) {
				running = true;
				break;
			}
		}

		Logger.s((PlayerService.service != null)
				+ " ::::::::::::::-- AlaramReceiver " + running);
		return ((PlayerService.service != null) || running);
	}
}
