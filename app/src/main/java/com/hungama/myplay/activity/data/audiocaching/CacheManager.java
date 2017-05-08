package com.hungama.myplay.activity.data.audiocaching;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.audiocaching.DownloaderService.OnSaveTrackListener;
import com.hungama.myplay.activity.data.audiocaching.DownloaderService.OnSyncTrackListener;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;

//Getting Tracks from internal storage.
/**
 * Provider for all cached and non cached MediaArts and Tracks in the
 * application.
 * <p>
 * <b>Note that current this lib support only caching if trucks, MediaArt albums
 * and MediaArt tracks.</b>
 */
public final class CacheManager implements OnSyncTrackListener,
		OnSaveTrackListener {
	public static final int FREE_USER_CACHE_LIMIT = 3;
	// public static final int FREE_USER_AUTO_CACHE_LIMIT = 1;

	public static final int UPDATE_FREQUENCY_DELAY_ON_DOWNLOAD_MS = 500;

	public static final String ACTION_TRACK_CACHED = "com.hungama.myplay.activity.intent.action.track_cached";
	public static final String ACTION_CACHE_STATE_UPDATED = "com.hungama.myplay.activity.intent.action.cache_state_updated";
	public static final String ACTION_UPDATED_CACHE = "com.hungama.myplay.activity.intent.action.updated_cache";
	public static final String ACTION_VIDEO_TRACK_CACHED = "com.hungama.myplay.activity.intent.action.video_track_cached";
	public static final String ACTION_VIDEO_CACHE_STATE_UPDATED = "com.hungama.myplay.activity.intent.action.video_cache_state_updated";
	public static final String ACTION_VIDEO_UPDATED_CACHE = "com.hungama.myplay.activity.intent.action.updated_video_cache";

	private static final String TAG = "CacheManager";
	// Hardcoded Strings
	// public static final String IMAGE_FORMAT = ".jpg";
	public static final String TRACK_FORMAT = ".cache";// ".mp3";
	public static final String TEMP_TRACK_FORMAT = "_temp.cache";// ".mp3";
	public static final String TRACK_PART_FORMAT = ".part";
	// public static final String TRACK_PROXY_FORMAT = ".proxy";
	// public static final String IMAGES_FOLDER_NAME = "images";
	public static final String TRACKS_FOLDER_NAME = "track";
	public static final String TEMP_TRACKS_FOLDER_NAME = "track_temp";
	public static final String VIDEO_TRACKS_FOLDER_NAME = "video";
	// public static final String ALBUMS_FOLDER_NAME = "album";
	// public static final String CAMPAIGNS_FOLDER_NAME = "campaigns";
	// Strings representing folder paths
	// private static String mImageFolderPath;
	private static String mTracksFolderPath;
	private static String mTracksExternalFolderPath;
	private static String mTracksUnecrryptedFolderPath;

	private static String mVideoTracksFolderPath;
	private static String mVideoTracksExternalFolderPath;
	private static String mVideoTracksUnecrryptedFolderPath;

	// private static String mCampaignsFolderPath;
	// Access to Context and data
	private static Context context;
	// private static DatabaseManager mDatabaseManager;
	// private static LockingManager mLockingManager;
	// private static SystemCachingManager mSystemCachingManager;
	// private TrackQueueReceiver mTrackQueueReceiver;

	private List<Track> listOfItemable;
	// public static LinkedList<Track> cachedTracks;
	// private ConnectivityManager conMgr;

	private CMEncryptor cmEncryptor;

	// Listener for GUI CallBacks
	private OnCachingTrackLister mOnCachingTrackLister;

	private OnCachingTrackLister mOnCachingTrackServiceLister;

	private RefCountHashMap<Long, Track> mCachingTracks;
	private RefCountHashMap<Long, MediaItem> mCachingVideoTracks;

	private static Boolean isWorking = false;

	private static Boolean isActive = true;

	public static final long SAFETY_BUFFER = 200 * 1024 * 1024;

	// private String appType;

	public CacheManager(Context context) {
		try {
			initialize(context);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// public static void release(Context context){
	//
	// Logger.i(TAG, "CacheManager release");
	//
	// isActive = false;
	//
	// //SyncTrackService.setOnSyncTrackListener(null);
	// //Intent intent = new Intent(context.getApplicationContext(),
	// SyncTrackService.class);
	// //context.getApplicationContext().stopService(intent);
	// }

	/**
	 * Makes the images folder, tracks folder, and images/albums folder.
	 */
	private void initialize(Context context) {

		Logger.i(TAG, "initialize");

		// File imageFolder = new
		// File(context.getApplicationContext().getCacheDir() + "/" +
		// IMAGES_FOLDER_NAME);
		// if (!imageFolder.exists()) {
		// imageFolder.mkdir();
		// }
		CacheManager.context = context;

		reset();

		DownloaderService.setOnSyncTrackListener(this);
		// StreamProxy.setOnSyncTrackListener(this);

		listOfItemable = new ArrayList<Track>();

		// mDatabaseManager = HungamaApplication.getDatabaseManager();
		// mSystemCachingManager = new SystemCachingManager(this, context,
		// mDatabaseManager);
		// mLockingManager = new LockingManager(context, mDatabaseManager);
		// mTrackQueueReceiver = new TrackQueueReceiver();

		mCachingTracks = new RefCountHashMap<Long, Track>();
		mCachingVideoTracks = new RefCountHashMap<Long, MediaItem>();
		// cachedTracks = mDatabaseManager.getCachedLinkedList();

		isActive = true;

		// appType = context.getResources().getString(R.string.app_type);

		DownloaderService.setOnSaveTrackListener(this);

	}

	// public Track nextTrackBeingCached()
	// {
	// if (listOfItemable != null && listOfItemable.size() > 0)
	// return listOfItemable.get(0);
	// else
	// return null;
	//
	// }

	// public static int listSearch(long id, LinkedList<Track> list)
	// {
	// int lo = 0;
	// int hi = list.size() - 1;
	// while (lo <= hi) {
	//
	// int mid = lo + (hi - lo) / 2;
	// if (id < list.get(mid).getId()) hi = mid - 1;
	// else if (id > list.get(mid).getId()) lo = mid + 1;
	// else return mid;
	// }
	// return -1;
	//
	// }

	public synchronized static boolean CacheLimitCheck(boolean isInternal) {
		// if (isInternal)
		// {
		// if (currentlyCached() >=
		// (HungamaApplication.getDatabaseManager().getCacheLimit()*.01*MemoryInfoHelper.getTotalInternalMemorySizeLong()))
		// {
		// while(mSystemCachingManager.lruRemove()){
		// if (currentlyCached() <=
		// (HungamaApplication.getDatabaseManager().getCacheLimit()*.01*MemoryInfoHelper.getTotalInternalMemorySizeLong()))
		// return true;
		// }
		// return false;
		// }else
		// return true;
		//
		// }else{
		// if (currentlyCached() >=
		// (HungamaApplication.getDatabaseManager().getCacheLimit()*.01*(MemoryInfoHelper.getTotalExternalMemorySizeLong())))
		// {
		// while(mSystemCachingManager.lruRemove()){
		// if (currentlyCached() <=
		// (HungamaApplication.getDatabaseManager().getCacheLimit()*.01*(MemoryInfoHelper.getTotalExternalMemorySizeLong())))
		// return true;
		// }
		// return false;
		// }
		// else
		// return true;
		// }
		return true;
	}

	public static String getCacheTracksFolderPath(boolean encrypt) {

		// Logger.i(TAG,"External stroage state : "+android.os.Environment.getExternalStorageState());
		boolean isInternal = true;
		String path = null;
		// Check for space in external
		// if(!(isExternalMemoryOverLimit()) && (CacheLimitCheck(!isInternal))){
		// String externalPath = Environment.getExternalStorageDirectory() +
		// "/.hungama/" + TRACKS_FOLDER_NAME;
		if (!(isExternalMemoryOverLimit()) && (CacheLimitCheck(!isInternal))) {
			File externalTracksFolder = null;
			// there is space.
			Logger.i(TAG, "external path : " + mTracksExternalFolderPath);
			// if (context.getApplicationContext().getExternalCacheDir() != null
			// &&
			// android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)
					&& Environment.getExternalStorageDirectory() != null) {
				if (encrypt) {
					externalTracksFolder = new File(
							Environment.getExternalStorageDirectory()
									+ "/.hungama/" + TRACKS_FOLDER_NAME);
					mTracksExternalFolderPath = externalTracksFolder
							.getAbsolutePath();
				} else {
					externalTracksFolder = new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
									+ "/"
									+ context.getResources().getString(
											R.string.application_name));
					mTracksUnecrryptedFolderPath = externalTracksFolder
							.getAbsolutePath();
				}
				if (!externalTracksFolder.exists()) {
					externalTracksFolder.mkdirs();
				}
			}
			if (encrypt)
				path = mTracksExternalFolderPath;
			else
				path = mTracksUnecrryptedFolderPath;

		}// else check for internal limit
		else if (!(isInternalMemoryOverLimit())
				&& (CacheLimitCheck(isInternal))) {
			// there is space in the internal
			// Logger.i(TAG,"internal path : "+mTracksFolderPath);

			path = mTracksFolderPath;
		}// else there is no space and null will be returned.

		return path;
	}

	public static String getTempCacheTracksFolderPath(boolean encrypt) {

		// Logger.i(TAG,"External stroage state : "+android.os.Environment.getExternalStorageState());
		boolean isInternal = true;
		String path = null;
		// Check for space in external
		if (!(isExternalMemoryOverLimit()) && (CacheLimitCheck(!isInternal))) {
			File externalTracksFolder = null;
			// there is space.
			if (context.getApplicationContext().getExternalCacheDir() != null
					&& android.os.Environment.getExternalStorageState().equals(
							android.os.Environment.MEDIA_MOUNTED)) {

				if (encrypt) {
					externalTracksFolder = new File(context
							.getApplicationContext().getExternalCacheDir()
							+ "/" + TEMP_TRACKS_FOLDER_NAME);
					mTracksExternalFolderPath = externalTracksFolder
							.getAbsolutePath();
				} else {
					externalTracksFolder = new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
									+ "/"
									+ context.getResources().getString(
											R.string.application_name));
					mTracksUnecrryptedFolderPath = externalTracksFolder
							.getAbsolutePath();
				}
				if (!externalTracksFolder.exists()) {
					externalTracksFolder.mkdirs();
				}
			}
			if (encrypt)
				path = mTracksExternalFolderPath;
			else
				path = mTracksUnecrryptedFolderPath;

		}// else check for internal limit
		else if (!(isInternalMemoryOverLimit())
				&& (CacheLimitCheck(isInternal))) {
			// there is space in the internal
			// Logger.i(TAG,"internal path : "+mTracksFolderPath);

			File tracksFolder = new File(context.getApplicationContext()
					.getCacheDir() + "/" + TEMP_TRACKS_FOLDER_NAME);
			if (!tracksFolder.exists()) {
				tracksFolder.mkdirs();
			}
			path = tracksFolder.getAbsolutePath();
		}// else there is no space and null will be returned.

		return path;
	}

	public static String getCacheVideoTracksFolderPath(boolean encrypt) {
		Logger.i(
				TAG,
				"External stroage state : "
						+ android.os.Environment.getExternalStorageState());
		boolean isInternal = true;
		String path = null;
		// Check for space in external
		if (!(isExternalMemoryOverLimit()) && (CacheLimitCheck(!isInternal))) {
			File externalTracksFolder = null;
			// there is space.
			Logger.i(TAG, "external path : " + mVideoTracksExternalFolderPath);
			// if (context.getApplicationContext().getExternalCacheDir() != null
			// &&
			// android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)){
			if (android.os.Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED)
					&& Environment.getExternalStorageDirectory() != null) {
				if (encrypt) {
					externalTracksFolder = new File(
							Environment.getExternalStorageDirectory()
									+ "/.hungama/"
									+ VIDEO_TRACKS_FOLDER_NAME);
					mVideoTracksExternalFolderPath = externalTracksFolder
							.getAbsolutePath();
				} else {
					externalTracksFolder = new File(
							Environment
									.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
									+ "/"
									+ context.getResources().getString(
											R.string.application_name));
					mVideoTracksUnecrryptedFolderPath = externalTracksFolder
							.getAbsolutePath();
				}
				if (!externalTracksFolder.exists()) {
					externalTracksFolder.mkdirs();
				}
			}
			if (encrypt)
				path = mVideoTracksExternalFolderPath;
			else
				path = mVideoTracksUnecrryptedFolderPath;
		}// else check for internal limit
		else if (!(isInternalMemoryOverLimit())
				&& (CacheLimitCheck(isInternal))) {
			// there is space in the internal
			Logger.i(TAG, "internal path : " + mVideoTracksFolderPath);
			path = mVideoTracksFolderPath;
		}// else there is no space and null will be returned.
		return path;
	}

	// /**
	// * A song has been played decrease its delete priority.
	// * @param track
	// */
	// public void touch(Track track){
	// mSystemCachingManager.touch(track);
	// }

	// public void makeFreeSpace() {
	// if(!(isExternalMemoryOverLimit())) {
	// CacheLimitCheck(false);
	// } else if(!(isInternalMemoryOverLimit())) {
	// CacheLimitCheck(true);
	// }
	// }

	// public static String getCacheCampaignsFolderName() {
	// return mCampaignsFolderPath;
	// }
	// public static String getCacheImageFolderName() {
	// return mImageFolderPath;
	// }
	// ###### Track ######
	// public static boolean isTrackFileExist(long trackId) {
	// Logger.i(TAG,"Does Track id : " + trackId + "Exist in location - " +
	// mTracksFolderPath + Long.toString(trackId) + TRACK_FORMAT + " Or in " +
	// mTracksExternalFolderPath + Long.toString(trackId) + TRACK_FORMAT);
	// File trackCacheLocation = new File(mTracksFolderPath,
	// Long.toString(trackId) + TRACK_FORMAT);
	// File trackExternalCacheLocation = new File(mTracksExternalFolderPath,
	// Long.toString(trackId) + TRACK_FORMAT);
	// File trackUncryptedCacheLocation = new File(mTracksUnecrryptedFolderPath,
	// Long.toString(trackId) + TRACK_FORMAT);
	// if(trackExternalCacheLocation.exists()){
	// return true;
	// }else if(trackCacheLocation.exists()){
	// return true;
	// }else if (trackUncryptedCacheLocation.exists())
	// return true;
	//
	// return false;
	// }
	// public static boolean deleteTrackFile(long trackId) {
	//
	// File trackCacheLocation = new File(mTracksFolderPath,
	// Long.toString(trackId) + TRACK_FORMAT);
	// File trackExternalCacheLocation = new File(mTracksExternalFolderPath,
	// Long.toString(trackId) + TRACK_FORMAT);
	// File trackUnencryptedCacheLocation = new
	// File(mTracksUnecrryptedFolderPath, Long.toString(trackId) +
	// TRACK_FORMAT);
	// if(trackExternalCacheLocation.delete()){
	// return true;
	// }else if(trackCacheLocation.delete()){
	// return true;
	// }else if (trackUnencryptedCacheLocation.delete())
	// return true;
	// return false;
	// }

	public static boolean deleteInternalTrackFile(long trackId) {

		File trackCacheLocation = new File(mTracksFolderPath,
				Long.toString(trackId) + TRACK_FORMAT);
		Logger.i("deleteInternalTrackFile",
				mTracksFolderPath + Long.toString(trackId) + TRACK_FORMAT
						+ " : path - exists: " + trackCacheLocation.exists());
		return trackCacheLocation.delete();

	}

	public static boolean deleteExternalTrackFile(long trackId) {

		boolean sucess = false;

		File trackExternalCacheLocation = new File(mTracksExternalFolderPath,
				Long.toString(trackId) + TRACK_FORMAT);
		Logger.i("deleteExternalTrackFile",
				mTracksExternalFolderPath + Long.toString(trackId)
						+ TRACK_FORMAT + " : path - exists: "
						+ trackExternalCacheLocation.exists());

		sucess = trackExternalCacheLocation.delete();
		if (!sucess) {
			trackExternalCacheLocation = new File(mTracksUnecrryptedFolderPath,
					Long.toString(trackId) + TRACK_FORMAT);
			sucess = trackExternalCacheLocation.delete();
		}

		return sucess;
	}

	// public boolean deleteTrack(Track track, boolean isInternal)
	// {
	// if (isInternal)
	// {
	// if (deleteInternalTrackFile(track.getId()[Track.TRACK_ID_KEY_INDEX]))
	// {
	// track.setCached(false);
	// mDatabaseManager.update(track);
	// if(mOnCachingTrackLister != null){
	// mOnCachingTrackLister.onDeleteCachedTrack(track);
	// }
	//
	// Logger.i(TAG, "setCached(false) on Internal Track: " + track.getName() +
	// " ID: " + track.getId()[Track.TRACK_ID_KEY_INDEX]);
	// return true;
	// }
	// else
	// return false;
	// }
	// else
	// {
	// if (deleteExternalTrackFile(track.getId()[Track.TRACK_ID_KEY_INDEX]))
	// {
	// track.setCached(false);
	// mDatabaseManager.update(track);
	// if(mOnCachingTrackLister != null){
	// mOnCachingTrackLister.onDeleteCachedTrack(track);
	// }
	// Logger.i(TAG, "setCached(false) on External Track: " + track.getName() +
	// " ID: " + track.getId()[Track.TRACK_ID_KEY_INDEX]);
	// return true;
	// }
	// else
	// return false;
	// }
	//
	// }
	/**
	 * Send Track to be removed. IO - Database
	 * 
	 * @param track
	 * @return
	 */
	public boolean deleteTrack(Track track) {
		long id = track.getId();
		if (deleteExternalTrackFile(id)) {
			onDeleteingTrack(track);
			return true;
		} else if (deleteInternalTrackFile(id)) {
			onDeleteingTrack(track);
			return true;
		} else {
			onDeleteingTrack(track);
			return false;
		}
	}

	/**
	 * handles database and gui updates on deletion.
	 * 
	 * @param track
	 */
	private void onDeleteingTrack(Track track) {
		int date = 0;
		track.setLastPlayed(date);
		track.setTrackState(Track.STREAMABLE_STATE);
		// mDatabaseManager.update(track);
		//
		// setMixedAlbumStatus(track.getPrimaryId());
		//
		if (mOnCachingTrackLister != null) {
			mOnCachingTrackLister.onDeleteCachedTrack(track);
		}
		if (mOnCachingTrackServiceLister != null)
			mOnCachingTrackServiceLister.onDeleteCachedTrack(track);
		Logger.i(TAG, "setCached(false) on " + track.getTitle() + " ID: "
				+ track.getId());
	}

	/**
	 * Retrieves caching file path of the given track.
	 * 
	 * @param trackId
	 *            - id of the desired track.
	 * @return track cache path, null if it was not found
	 */
	public String getTrack(long trackId) {

		File trackCacheLocation = new File(mTracksFolderPath,
				Long.toString(trackId) + TRACK_FORMAT);
		File trackExternalCacheLocation = new File(mTracksExternalFolderPath,
				Long.toString(trackId) + TRACK_FORMAT);
		File trackUnencryptedLocation = new File(mTracksUnecrryptedFolderPath,
				Long.toString(trackId) + TRACK_FORMAT);
		if (trackExternalCacheLocation.exists()) {
			return trackExternalCacheLocation.toString();
		} else if (trackCacheLocation.exists()) {
			return trackCacheLocation.toString();
		} else if (trackUnencryptedLocation.exists())
			return trackUnencryptedLocation.toString();

		return null;
	}

	public String getVideoTrack(long trackId) {
		File trackCacheLocation = new File(mVideoTracksFolderPath,
				Long.toString(trackId) + TRACK_FORMAT);
		File trackExternalCacheLocation = new File(
				mVideoTracksExternalFolderPath, Long.toString(trackId)
						+ TRACK_FORMAT);
		File trackUnencryptedLocation = new File(
				mVideoTracksUnecrryptedFolderPath, Long.toString(trackId)
						+ TRACK_FORMAT);
		if (trackExternalCacheLocation.exists()) {
			return trackExternalCacheLocation.toString();
		} else if (trackCacheLocation.exists()) {
			return trackCacheLocation.toString();
		} else if (trackUnencryptedLocation.exists())
			return trackUnencryptedLocation.toString();

		return null;
	}

	public void cacheTrack(Context context, Track track) {
		try {
			// SyncTrackService.setOnSyncTrackListener(this);
			Intent intnet = new Intent(context, DownloaderService.class);
			intnet.putExtra(DownloaderService.DOWNLOAD_TYPE,
					DownloaderService.TRACK_CACHE);
			intnet.putExtra(DownloaderService.DOWNLOAD_ITEMABLE, track);
			context.startService(intnet);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public void cacheVideoTrack(Context context, Video video,
			MediaItem mediaItem) {
		try {
			// SyncTrackService.setOnSyncTrackListener(this);
			Intent intnet = new Intent(context, DownloaderService.class);
			intnet.putExtra(DownloaderService.DOWNLOAD_TYPE,
					DownloaderService.VIDEO_TRACK_CACHE);
			intnet.putExtra(DownloaderService.DOWNLOAD_ITEMABLE, video);
			intnet.putExtra(DownloaderService.DOWNLOAD_MEDIA_ITEM, mediaItem);
			context.startService(intnet);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	// public Bitmap getImage(String fileName, String folderName) {
	//
	// File itemCacheLocation = new File(mImageFolderPath + "/" + folderName,
	// fileName);
	//
	// // validates that the file exist and can be read and is not a directory
	// if(itemCacheLocation.exists() && itemCacheLocation.canRead() &&
	// !itemCacheLocation.isDirectory()){
	// try{
	// return BitmapFactory.decodeFile(itemCacheLocation.getAbsolutePath());
	// }catch(OutOfMemoryError e){
	// e.printStackTrace();
	// }
	// }
	// return null;
	// }

	// public void insertImage(String fileName, String folderName, Bitmap image)
	// throws FileNotFoundException, IOException {
	//
	// if (image == null) {
	// return;
	// }
	//
	// File imageFolder = new File(mImageFolderPath);
	// if (!imageFolder.exists()) {
	// imageFolder.mkdir();
	// }
	//
	// File folder = new File(mImageFolderPath + "/" + folderName);
	// if (!folder.exists()) {
	// folder.mkdir();
	// }
	//
	// File imageFileCacheLocation = new File(mImageFolderPath + "/" +
	// folderName, fileName);
	// FileOutputStream out;
	//
	// try {
	//
	// out = new FileOutputStream(imageFileCacheLocation);
	// image.compress(Bitmap.CompressFormat.PNG, 90, out);
	//
	// out.close();
	// out = null;
	//
	// } catch (FileNotFoundException exception) {
	// throw new FileNotFoundException();
	// }
	//
	// }

	// public void renameMediaArtPath(long oldItem, long newItem)
	// {
	// File imageFileLocation = new File(mImageFolderPath + "/albums/"+ oldItem
	// + IMAGE_FORMAT);
	// File newImageFileLocation = new File(mImageFolderPath + "/albums/"+
	// newItem + IMAGE_FORMAT);
	//
	// Logger.i(TAG + " - renameMediaArt", "Old item : " + oldItem + "/" +
	// imageFileLocation.getAbsolutePath() + " --> new item : " + newItem + "/"
	// +
	// newImageFileLocation.getAbsolutePath());
	//
	// if (!imageFileLocation.exists())
	// {
	// Logger.i(TAG + " - renameMediaArt", "Old File Doesn't Exist");
	// return;
	// }
	//
	// // File (or directory) with new name
	// if(newImageFileLocation.exists())
	// {
	// Logger.i(TAG + " - renameMediaArt", "New File Exists");
	// return;
	// }
	// // Rename file (or directory)
	// boolean success = imageFileLocation.renameTo(newImageFileLocation);
	// if (!success) {
	// Logger.i(TAG + " - renameMediaArt", "Failed To Rename File");
	// return;
	// }
	// Logger.i(TAG + " - renameMediaArt", "File successfully Renamed");
	// return;
	// }
	// public static void copyMediaArtToCache(Itemable item, byte[] mediaArt)
	// {
	// if (mediaArt == null || mediaArt.length == 0){
	// Logger.i(TAG + " - copyMediaArtToCache", "Empty Byte Stream");
	// return;
	// }
	// if (item != null && mediaArt != null)
	// Logger.i(TAG + " - copyMediaArtToCache",
	// "Entered CopyMediaArtToCache with following parameters item : " + item +
	// " path : " + mediaArt);
	// String fileName = Long.toString(item.getId()[Track.TRACK_ID_KEY_INDEX]) +
	// CacheManager.IMAGE_FORMAT;
	// String folderName = item.getTableName();
	//
	// File imageFolder = new File(mImageFolderPath);
	// if (!imageFolder.exists()) {
	// imageFolder.mkdir();
	// }
	//
	// File folder = new File(mImageFolderPath + "/" + folderName);
	// if (!folder.exists()) {
	// folder.mkdir();
	// }
	//
	// File imageFileCacheLocation = new File(mImageFolderPath + "/" +
	// folderName, fileName);
	// FileOutputStream out;
	// ByteArrayInputStream in;
	//
	// try {
	//
	// out = new FileOutputStream(imageFileCacheLocation);
	// in = new ByteArrayInputStream(mediaArt);
	// byte[] buf = new byte[1024];
	// int len;
	// while ((len = in.read(buf)) > 0) {
	// out.write(buf, 0, len);
	// }
	// in.close();
	// out.close();
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// Logger.i(TAG + " - copyMediaArtToCache", "Finished Copying Image");
	//
	//
	// }
	// public static void copyMediaArtToCache(Itemable item, String
	// mediaArtPath)
	// {
	// if (mediaArtPath == null || mediaArtPath == ""){
	// Logger.i(TAG + " - copyMediaArtToCache", "Media Art path is null or empty");
	// return;
	// }
	// if (item != null && mediaArtPath != null)
	// Logger.i(TAG + " - copyMediaArtToCache",
	// "Entered CopyMediaArtToCache with following parameters item : " + item +
	// " path : " + mediaArtPath);
	// String fileName = Long.toString(item.getId()[Track.TRACK_ID_KEY_INDEX]) +
	// CacheManager.IMAGE_FORMAT;
	// String folderName = item.getTableName();
	//
	// File localImageFileCacheLocation = new File(mediaArtPath);
	// if (!localImageFileCacheLocation.exists()) {
	// Logger.i(TAG + " - copyMediaArtToCache", "File Doesn't Exist");
	// return;
	// }
	// File imageFolder = new File(mImageFolderPath);
	// if (!imageFolder.exists()) {
	// imageFolder.mkdir();
	// }
	//
	// File folder = new File(mImageFolderPath + "/" + folderName);
	// if (!folder.exists()) {
	// folder.mkdir();
	// }
	//
	// File imageFileCacheLocation = new File(mImageFolderPath + "/" +
	// folderName, fileName);
	// FileOutputStream out;
	// FileInputStream in;
	//
	// try {
	//
	// out = new FileOutputStream(imageFileCacheLocation);
	// in = new FileInputStream(localImageFileCacheLocation);
	// byte[] buf = new byte[1024];
	// int len;
	// while ((len = in.read(buf)) > 0) {
	// out.write(buf, 0, len);
	// }
	// in.close();
	// out.close();
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// Logger.i(TAG + " - copyMediaArtToCache", "Finished Copying Image");
	//
	//
	// }
	// public boolean deleteImage(String fileName, String folderName) {
	//
	// File itemCacheLocation = new File(mImageFolderPath + "/" + folderName,
	// fileName);
	// boolean success = itemCacheLocation.delete();
	//
	// return success;
	// }
	//
	// public boolean isImageExist(String fileName, String folderName) {
	//
	// File imageFileCacheLocation = new File(mImageFolderPath + "/" +
	// folderName, fileName);
	//
	// return imageFileCacheLocation.exists();
	// }

	/**
	 * Interface definition for a callback to be invoked when a {@link Track} is
	 * caching.
	 */
	public interface OnCachingTrackLister {

		public void onStartCachingTrack(Track track);

		public void onFinishCachingTrack(Track track);

		public void onUpdateCachingTrack(Track track, int percent);

		public void onFailCachingTrack(Track track);

		public void onDeleteCachedTrack(Track track);

	}

	/**
	 * Sets listener to be invoked when {@link Track}s are caching.
	 */
	public void setOnCachingTrackLister(OnCachingTrackLister listener) {
		mOnCachingTrackLister = listener;
	}

	/**
	 * Sets listener to be invoked when {@link Track}s are caching.
	 */
	public void setOnCachingTrackServiceLister(OnCachingTrackLister listener) {
		mOnCachingTrackServiceLister = listener;
	}

	// listeners for the process of caching tracks

	/*
	 * public void startCachingTrack(Track track){ if (mOnCachingTrackLister !=
	 * null) {
	 * 
	 * mOnCachingTrackLister.onStartCachingTrack(track);
	 * 
	 * mCachingTracks.put(track.getId(), track);
	 * 
	 * //mDatabaseManager.setIsOnSyncing(true); isWorking = true; } }
	 * 
	 * public void finishCachingTrack(Track track){ if (mOnCachingTrackLister !=
	 * null) { mOnCachingTrackLister.onFinishCachingTrack(track);
	 * mCachingTracks.remove(track.getId());
	 * 
	 * removeCachedTrackFromList(track);
	 * 
	 * if(isActive){ Logger.i("CacheManager", isActive + " Continue Pre-Caching");
	 * cacheTrackFromList(GlobalApplicationData.getContext(), null); }else{
	 * Logger.i("CacheManager", isActive + " Stop Pre-Caching"); }
	 * 
	 * //mDatabaseManager.setIsOnSyncing(false); isWorking = false; } }
	 * 
	 * public void failCachingTrack(Track track){ if (mOnCachingTrackLister !=
	 * null) {
	 * 
	 * mOnCachingTrackLister.onFailCachingTrack(track);
	 * mCachingTracks.remove(track.getId());
	 * 
	 * //mDatabaseManager.setIsOnSyncing(false); isWorking = false; } }
	 */

	/**
	 * CallBacks for System Caching Songs.
	 */
	@Override
	public void onStartCachingTrack(Track track) {
		if (track == null)
			return;
		Logger.i(TAG,
				"Start Caching Track " + track.getTitle() + " " + track.getId());

		addCachingTrack(track);

		isWorking = true;

		cancelSave();
		// if we do double caching of the same track in different threads then
		// track will be deleted
		// from the map mCachingTracks resulting in bad UI behavior

		if (mOnCachingTrackLister != null) {
			mOnCachingTrackLister.onStartCachingTrack(track);
		}
	}

	// public int numOfPlaylists = 0;
	public static int numOfCached = 0;

	@Override
	public void onFinishCachingTrack(Track track) {
		if (track == null)
			return;

		Logger.i(
				TAG,
				"Finish Caching Track " + track.getTitle() + " "
						+ track.getId());
		DBOHandler.updateTrackListState(context, "" + track.getId(), "");
		numOfCached++;
		removeCachingTrack(track);
		removeCachedTrackFromList(track);

		// mSystemCachingManager.touch(track);
		// mTrackQueueReceiver.clearPreCache(track.getId());
		// setCachedAlbumStatus(track.getPrimaryId());
		if (isActive) {
			Logger.i(TAG, isActive + " Continue Pre-Caching");
			cacheTrackFromList(HungamaApplication.getContext(), null);
		} else {
			Logger.i(TAG, isActive + " Stop Pre-Caching");
		}

		// mDatabaseManager.setIsOnSyncing(false);
		isWorking = false;

		// if (mDatabaseManager.getAppType().equals("Insense") && numOfCached%10
		// == 0)
		// {
		// Playlist dumbPlayList = new Playlist();
		// Track dumbTrack = new Track();
		//
		//
		// List<Itemable> templist1 = mDatabaseManager.query(dumbPlayList, null,
		// InventoryContract.Playlists.NAME);
		//
		// if (templist1 != null)
		// {
		//
		// List<Itemable> iterList = new ArrayList<Itemable>();
		//
		// iterList.addAll(templist1);
		//
		// for (Itemable i : iterList)
		// {
		// if (((Playlist) i).getTrackList() != null)
		// {
		// String tempTrackList = ((Playlist)i).getTrackList().replace("  ",
		// " ");
		//
		// // do a super duper smart query for pulling the tracks.
		// tempTrackList = tempTrackList.trim();
		// String trackList = tempTrackList.replace(" ", ",");
		// //listOfItemable = databaseManager.query(dumbTrack,
		// InventoryContract.Tracks.ID + " IN (" + trackList + ")" , null);
		// List<Itemable> tmpList = mDatabaseManager.query(dumbTrack,
		// InventoryContract.Tracks.ID + " IN (" + trackList + ") " +
		// "AND "+ InventoryContract.Tracks.TRACK_STATE + " = " +
		// Track.STREAMABLE_STATE , null);
		// if (tmpList != null)
		// templist1.remove(i);
		// }
		// }
		//
		// if (templist1.size() != numOfPlaylists){
		// numOfPlaylists = templist1.size();
		// Intent playlistCached = new Intent(PLAYLIST_CACHED);
		// Logger.i("SyncDataService", " SENDING BROADCAST PLAYLIST_CACHED");
		// HungamaApplication.getContext().sendBroadcast(playlistCached);
		// }
		// }
		// }else if (mDatabaseManager.getDebugMode())
		// {
		// Intent playlistCached = new Intent(PLAYLIST_CACHED);
		// Logger.i("SyncDataService", " SENDING BROADCAST PLAYLIST_CACHED");
		// HungamaApplication.getContext().sendBroadcast(playlistCached);
		// }

		if (mOnCachingTrackLister != null) {
			track.setTrackState(Track.CACHED_STATE);
			mOnCachingTrackLister.onFinishCachingTrack(track);
			mOnCachingTrackServiceLister.onFinishCachingTrack(track);
		}

		resumeSave();
		ApsalarEvent.postEvent(HungamaApplication.getContext(), ApsalarEvent.SONG_CACHED);
		Intent TrackCached = new Intent(ACTION_TRACK_CACHED);
		Logger.i("onFinishedCaching", " SENDING BROADCAST TRACK_CACHED");
		HungamaApplication.getContext().sendBroadcast(TrackCached);
	}

	@Override
	public void onUpdateCachingTrack(Track track, int percent) {
		if (track != null) {
			Track cachingTrack = getCachingTrack(track.getId());
			if (cachingTrack != null) {
				cachingTrack.setProgress(percent);
			}
			if (track != null) {
				Logger.s("onUpdateCachingTrack :::: " + track.getId() + " ::: "
						+ percent);
				DBOHandler.updateTrackCacheProgress(context,
						"" + track.getId(), percent);
				track.setProgress(percent);
			}

			if (mOnCachingTrackLister != null) {
				mOnCachingTrackLister.onUpdateCachingTrack(track, percent);
			}
			// Intent TrackCached = new Intent(ACTION_UPDATED_CACHE);
			// Logger.i("onFinishedCaching", " SENDING BROADCAST UPDATED_CACHE");
			// HungamaApplication.getContext().sendBroadcast(TrackCached);
		}
	}

	@Override
	public void onFailCachingTrack(Track track, boolean update) {
		if (track == null)
			return;

		DBOHandler.updateTrackCacheState(context, "" + track.getId(),
				DataBase.CacheState.NOT_CACHED.toString());
		DBOHandler.updateTrackListFailedState(context, "" + track.getId(), "");
		resumeSave();
		Logger.i(TAG,
				"Fail Caching Track " + track.getTitle() + " " + track.getId());
		removeCachingTrack(track);
		// if (appType.equals(HungamaApplication.INSENSE) ||
		// appType.equals(HungamaApplication.ALBUM))
		// {
		// removeCachedTrackFromList(track);
		// listOfItemable.add(track);
		// if(isActive){
		// Logger.i(TAG, isActive + " Continue Pre-Caching");
		// cacheTrackFromList(HungamaApplication.getContext(), null);
		// }else{
		// Logger.i(TAG, isActive + " Stop Pre-Caching");
		// }
		// }
		isWorking = false;

		if (mOnCachingTrackLister != null && update) {

			mOnCachingTrackLister.onFailCachingTrack(track);
		}

		if (DBOHandler.isTrackExist(HungamaApplication.getContext(),
				"" + track.getId())) {
			Toast.makeText(
					HungamaApplication.getContext(),
					"Song "
							+ track.getTitle()
							+ " could not be downloaded due to low network bandwidth.",
					Toast.LENGTH_SHORT).show();
		}

		Intent TrackCached = new Intent(ACTION_TRACK_CACHED);
		Logger.i("onFinishedCaching", " SENDING BROADCAST TRACK_CACHED Failed");
		HungamaApplication.getContext().sendBroadcast(TrackCached);
	}

	public synchronized void cacheTrackFromList(Context context, Track track) {

		String availabeInternalMemory = MemoryInfoHelper
				.getAvailableInternalMemorySize();
		String totalInternalMemory = MemoryInfoHelper
				.getTotalInternalMemorySize();
		String availableExternalMemory = MemoryInfoHelper
				.getAvailableExternalMemorySize();
		String totalExternalMemory = MemoryInfoHelper
				.getTotalExternalMemorySize();

		Logger.i(TAG, "AVAILABLE INTERNAL MEMORY: " + availabeInternalMemory);
		Logger.i(TAG, "TOTAL INTERNAL MEMORY: " + totalInternalMemory);
		Logger.i(TAG, "AVAILABLE EXTERNAL MEMORY: " + availableExternalMemory);
		Logger.i(TAG, "TOTAL EXTERNAL MEMORY: " + totalExternalMemory);

		// Cache the next track in list
		Track nextTrackToCache = track;// null;
		// if(track == null){
		// if(listOfItemable != null && listOfItemable.size()>0){
		// nextTrackToCache = (Track) listOfItemable.get(0);
		// }
		// }else{
		// nextTrackToCache = getTrackFromList(track);
		// }

		if (nextTrackToCache != null) {
			cacheTrack(context, nextTrackToCache);

			Logger.i(TAG, "Uncached Track List: " + getTracksStringsFromList());
			Logger.i(TAG, "Cache Track: " + nextTrackToCache.getTitle());
		} else {
			Intent playlistCached = new Intent(PLAYLIST_CACHED);
			Logger.i("SyncDataService", " SENDING BROADCAST PLAYLIST_CACHED");
			HungamaApplication.getContext().sendBroadcast(playlistCached);
		}
	}

	public synchronized void cacheVideoTrackFromList(Context context,
			Video video, MediaItem mediaItem) {

		String availabeInternalMemory = MemoryInfoHelper
				.getAvailableInternalMemorySize();
		String totalInternalMemory = MemoryInfoHelper
				.getTotalInternalMemorySize();
		String availableExternalMemory = MemoryInfoHelper
				.getAvailableExternalMemorySize();
		String totalExternalMemory = MemoryInfoHelper
				.getTotalExternalMemorySize();

		Logger.i(TAG, "AVAILABLE INTERNAL MEMORY: " + availabeInternalMemory);
		Logger.i(TAG, "TOTAL INTERNAL MEMORY: " + totalInternalMemory);
		Logger.i(TAG, "AVAILABLE EXTERNAL MEMORY: " + availableExternalMemory);
		Logger.i(TAG, "TOTAL EXTERNAL MEMORY: " + totalExternalMemory);

		// Cache the next track in list
		Video nextTrackToCache = video;// null;
		// if(track == null){
		// if(listOfItemable != null && listOfItemable.size()>0){
		// nextTrackToCache = (Track) listOfItemable.get(0);
		// }
		// }else{
		// nextTrackToCache = getTrackFromList(track);
		// }

		if (nextTrackToCache != null) {
			cacheVideoTrack(context, nextTrackToCache, mediaItem);

			Logger.i(TAG, "Uncached Track List: " + getTracksStringsFromList());
			Logger.i(TAG, "Cache Track: " + mediaItem.getTitle());
		} else {
			Intent playlistCached = new Intent(PLAYLIST_CACHED);
			Logger.i("SyncDataService", " SENDING BROADCAST PLAYLIST_CACHED");
			HungamaApplication.getContext().sendBroadcast(playlistCached);
		}
	}

	private synchronized boolean removeCachedTrackFromList(Track track) {

		Iterator<Track> iter = listOfItemable.iterator();

		while (iter.hasNext()) {
			Track tmpTrack = (Track) iter.next();

			if (track.getId() == tmpTrack.getId()) {
				iter.remove();
				Logger.i(TAG, "Remove Track: " + track.getTitle());

				return true;
			}
		}

		return false;
	}

	private synchronized Track getTrackFromList(Track track) {

		Track tmp = new Track();
		for (Track item : listOfItemable) {
			tmp = (Track) item;

			if (track.getId() == tmp.getId()) {
				break;
			}
		}
		return tmp;
	}

	private String getTracksStringsFromList() {

		StringBuilder tracksString = new StringBuilder();

		if (listOfItemable != null && listOfItemable.size() > 0) {

			for (Track item : listOfItemable) {
				Track track = (Track) item;
				tracksString.append(track.getTitle() + ", ");
			}
			tracksString.delete(tracksString.length() - 2,
					tracksString.length());
		}
		return tracksString.toString();
	}

	// public synchronized void cacheTrackOnclick(Track track){
	// // If track is cached or in caching process then Do Anything
	// boolean isLocal =
	// !MediaScanUtil.getLocalMediaPath(String.valueOf(track.getId())).equals("");
	// boolean trackCached = (track.isCached() || track.isLocked()) &&
	// isTrackFileExist(track.getId());
	//
	// boolean doNothing = trackCached || isLocal || isCaching(track);
	// if(doNothing){
	// Logger.i(TAG,
	// "cacheTrackOnclick( "+track.getTitle()+"="+track.getId()+" )" +
	// " Do Nothing");
	// return;
	// }
	// Logger.i(TAG,
	// "cacheTrackOnclick( "+track.getTitle()+"="+track.getId()+" )");
	// if(appType.equalsIgnoreCase(GlobalApplicationData.ACCESS_ANYWHERE) ||
	// appType.equalsIgnoreCase(GlobalApplicationData.JUKEBOX)){
	// //if (SyncTrackService.mHandler != null)
	// //
	// SyncTrackService.mHandler.sendEmptyMessage(SyncTrackService.MESSAGE_CANCEL_CACHING_TRACK);
	// cacheTrack(context, track);
	// }else{
	// // Find the track in listOfItemable and give it priority
	// removeCachedTrackFromList(track);
	// listOfItemable.add(0, track);
	// Logger.i(TAG, "Add Track: " + track.getTitle());
	// Logger.i(TAG, "Uncached Track List: " + getTracksStringsFromList());
	// }
	// }

	// public void encrypt(byte[] bytes, int off, int len){
	// cmEncryptor = new CMEncryptor();
	// cmEncryptor.encrypt(bytes, off, len);
	// }
	//
	// public void decrypt(byte[] bytes, int off, int len){
	// cmEncryptor = new CMEncryptor();
	// cmEncryptor.decrypt(bytes, off, len);
	// }

	// public void checkDeletedCachedTracks(){
	// Track dumbTrack = new Track();
	// List<Track> listOfCachedTracks =
	// mDatabaseManager.query(dumbTrack, InventoryContract.Tracks.TRACK_STATE +
	// " != " + Track.STREAMABLE_STATE + " AND " +
	// InventoryContract.Tracks.TRACK_STATE + " != " + Track.LOCAL_STATE, null);
	//
	// if(listOfCachedTracks != null && listOfCachedTracks.size()>0){
	// for(Track itemable : listOfCachedTracks){
	// Track track = (Track)itemable;
	//
	// if(!isTrackFileExist(track.getId()) &&
	// MediaScanUtil.getLocalMediaPath(String.valueOf(track.getId())).equals("")){
	// onDeleteingTrack(track);
	// }
	// }
	// }
	// }
	//
	// public void checkDeletedCachedTracksAsync() {
	// new Thread(new Runnable() {
	// @Override
	// public void run() {
	// Logger.d(TAG, "checkDeletedCachedTracksAsync");
	// checkDeletedCachedTracks();
	// }
	// }).start();
	// }

	private View view;
	private TextView min, diff, max, current, capacity;
	private SeekBar seek;

	private static long dirSize(File dir) {
		long result = 0;
		if (!dir.exists())
			return result;

		Stack<File> dirlist = new Stack<File>();
		dirlist.clear();

		dirlist.push(dir);

		while (!dirlist.isEmpty()) {
			File dirCurrent = dirlist.pop();

			File[] fileList = dirCurrent.listFiles();
			for (int i = 0; i < fileList.length; i++) {

				if (fileList[i].isDirectory())
					dirlist.push(fileList[i]);
				else
					result += fileList[i].length();
			}
		}

		return result;
	}

	// public static Long currentlyCached()
	// {
	// try{
	// // File path = new File(mImageFolderPath);
	// // long size = dirSize(path);
	//
	// File path = new File(mTracksFolderPath);
	// long size = dirSize(path);
	//
	//
	// // path = new File(mCampaignsFolderPath);
	// // size += dirSize(path);
	// if (MemoryInfoHelper.externalMemoryAvailable())
	// {
	// path = new File(mTracksExternalFolderPath);
	// size += dirSize(path);
	//
	// path = new File(mTracksUnecrryptedFolderPath);
	// size += dirSize(path);
	// }
	// return size;
	// }
	// catch(Exception e)
	// {
	// e.printStackTrace();
	// }
	// return (long) -1;
	// }

	// public void showCacheLimitAlertDialog(Activity activity){
	//
	// LayoutInflater factory = LayoutInflater.from(activity);
	// view = factory.inflate(R.layout.alert_dialog_cache_limit, null);
	//
	// CustomAlertDialog alert = new CustomAlertDialog(activity);
	//
	// alert.setTitle(R.string.cache_limit);
	// alert.setView(view);
	// min = (TextView)view.findViewById(R.id.cache_min);
	// diff = (TextView)view.findViewById(R.id.cache_diff);
	// max = (TextView)view.findViewById(R.id.cache_max);
	// current = (TextView)view.findViewById(R.id.current_cache_text);
	// capacity = (TextView)view.findViewById(R.id.cache_capacity);
	// seek = (SeekBar)view.findViewById(R.id.seek);
	//
	// Long totalMem
	// =MemoryInfoHelper.getTotalInternalMemorySizeLong()+MemoryInfoHelper.getTotalExternalMemorySizeLong();
	// current.setText(HungamaApplication.getContext().getResources().getString(R.string.currently_cached)+MemoryInfoHelper.formatSize(currentlyCached()));
	// capacity.setText(HungamaApplication.getContext().getResources().getString(R.string.capacity)+MemoryInfoHelper.formatSize(totalMem));
	// min.setText("0 MB");
	// //
	// seek.setProgress(HungamaApplication.getDatabaseManager().getCacheLimit());
	// max.setText(MemoryInfoHelper.formatSize(totalMem));
	// // diff.setText(MemoryInfoHelper.formatSize((long)
	// (HungamaApplication.getDatabaseManager().getCacheLimit()*.01*totalMem)));
	// seek.setOnSeekBarChangeListener( new OnSeekBarChangeListener()
	// {
	// public void onProgressChanged(SeekBar seekBar, int progress, boolean
	// fromUser)
	// {
	// diff.setText(MemoryInfoHelper.formatSize((long)
	// (progress*.01*(MemoryInfoHelper.getTotalInternalMemorySizeLong()+MemoryInfoHelper.getTotalExternalMemorySizeLong()))));
	// }
	//
	// public void onStartTrackingTouch(SeekBar seekBar)
	// {
	//
	// }
	//
	// public void onStopTrackingTouch(SeekBar seekBar)
	// {
	// // TODO Auto-generated method stub
	// }
	// });
	//
	// alert.setNegativeButton(R.string.alert_dialog_close, new
	// DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int whichButton) {
	// //
	// HungamaApplication.getDatabaseManager().setCacheLimit(seek.getProgress());
	// /* User clicked cancel so do some stuff */
	// // Do nothing
	// ///makeFreeSpace();
	// }
	// });
	//
	// alert.show();
	// }

	public static Boolean isInternalMemoryOverLimit() {

		String tmp = context.getResources().getString(
				R.string.internal_memory_limit);
		long internalMemoryLimit = Long.parseLong(tmp);

		long availableInternalMemorySize = MemoryInfoHelper
				.getAvailableInternalMemorySizeLong();

		if (availableInternalMemorySize <= internalMemoryLimit) {
			return true;
		} else {
			return false;
		}
	}

	public static Boolean isExternalMemoryOverLimit() {

		String tmp = context.getResources().getString(
				R.string.external_memory_limit);
		long externalMemoryLimit = Long.parseLong(tmp);

		long availableExternalMemorySize = MemoryInfoHelper
				.getAvailableExternalMemorySizeLong();

		if (availableExternalMemorySize <= externalMemoryLimit) {
			return true;
		} else {
			return false;
		}
	}

	// public static Boolean isExternalMemoryOverLimit(String path){
	//
	// String tmp =
	// context.getResources().getString(R.string.external_memory_limit);
	// long externalMemoryLimit = Long.parseLong(tmp);
	//
	// long availableExternalMemorySize =
	// MemoryInfoHelper.getAvailableExternalMemorySizeLong(path);
	//
	// if(availableExternalMemorySize <= externalMemoryLimit){
	// return true;
	// }else{
	// return false;
	// }
	// }

	private static final String PLAYLIST_CACHED = "com.hungama.myplay.activity.intent.action.playlist_cached";

	// public void onDoneSyncDataAction(){
	//
	// // Get the list from database of the cached tracks
	// // If track marked as cached and its file does not exist the track will
	// marked as un-cache
	// // checkDeletedCachedTracks();
	//
	// // if
	// (mDatabaseManager.getAppType().equalsIgnoreCase(GlobalApplicationData.INSENSE))
	// // {
	// // Intent playlistCached = new Intent(PLAYLIST_CACHED);
	// // Logger.i("SyncDataService", " SENDING BROADCAST PLAYLIST_CACHED");
	// // GlobalApplicationData.getContext().sendBroadcast(playlistCached);
	// //
	// // listOfItemable.clear();
	// // ArrayList<Itemable> updatedList = new ArrayList<Itemable>();
	// // Playlist dumbPlayList = new Playlist();
	// // Track dumbTrack = new Track();
	// // List<Itemable> playlists = mDatabaseManager.query(dumbPlayList, null,
	// // InventoryContract.Playlists.NAME);
	// // if (playlists != null && playlists.size() > 0)
	// // {
	// // for (int i = 0; i < playlists.size();i++)
	// // {
	// // Playlist playlist = (Playlist) playlists.get(i);
	// //
	// // String tempTrackList = playlist.getTrackList().replace(
	// // "  ", " ");
	// //
	// // tempTrackList = tempTrackList.trim();
	// // String trackList = tempTrackList.replace(" ", ",");
	// // List<Itemable> tmpList = mDatabaseManager
	// // .query(dumbTrack,
	// // InventoryContract.Tracks.ID
	// // + " IN ("
	// // + trackList
	// // + ") AND "
	// // + InventoryContract.Tracks.TRACK_STATE
	// // + " = " + Track.STREAMABLE_STATE, InventoryContract.Tracks.ID);
	// // if (tmpList != null){
	// // for (Itemable item : tmpList) {
	// //
	// // Track track = (Track) item;
	// // updatedList.add(track);
	// // }
	// // }
	// // }
	// // listOfItemable = new ArrayList<Itemable>(updatedList);
	// // }
	// //
	// //
	// //
	// // }else{
	// // // Get the list of uncached tracks from database and start caching...
	// // Track dumbTrack = new Track();
	// // listOfItemable =
	// // mDatabaseManager.joinQuery(true,dumbTrack, new TrackToAlbum(), null,
	// // InventoryContract.Tracks.TRACK_STATE + " = " + Track.STREAMABLE_STATE,
	// InventoryContract.TracksAlbums.TRACK_NUMBER);
	// // //query(dumbTrack, InventoryContract.Tracks.IS_CACHED + " = 0",
	// InventoryContract.Tracks.TRACK_NUMBER);
	// //
	// // ArrayList<Itemable> updatedList = new ArrayList<Itemable>();
	// //
	// // if (listOfItemable != null) {
	// // Track test = new Track(1011111110L, "Temp Hungama",
	// Track.STREAMABLE_STATE,
	// // 0, 0, 0, false, false, 0, 0);
	// // updatedList.add(test);
	// //// for (Itemable item : listOfItemable) {
	// ////
	// //// Track track = (Track) item;
	// //// updatedList.add(track);
	// //// }
	// //
	// // listOfItemable.clear();
	// // listOfItemable = new ArrayList<Itemable>(updatedList);
	// // }
	// // }
	//
	// ArrayList<Track> updatedList = new ArrayList<Track>();
	// Track test = new Track(20111542L, "Temp Hungama542", "","","","", null,
	// 0);
	// updatedList.add(test);
	// listOfItemable.clear();
	// listOfItemable = new ArrayList<Track>(updatedList);
	//
	// if(listOfItemable != null && listOfItemable.size()>0){
	// cacheTrackFromList(context, null);
	// }
	//
	//
	// }

	// public void onInternetConnectionChanged(Intent intent){
	//
	// //check connection and grayed out uncache tracks
	// if(Utils.isConnected()){
	//
	// onDoneSyncDataAction();
	// Logger.i(TAG, "Connected");
	// }else{
	// //Not connected.
	// Logger.i(TAG, "Not connected");
	// }
	// Logger.i("CachemanagerBroadcastReceiver: Connectivity Change",
	// getTracksStringsFromList());
	// }

	// public boolean isCaching(Track track) {
	// return mCachingTracks.containsKey(track.getId());
	// }

	// public synchronized void clearCachingTracks() {
	// mCachingTracks.clear();
	// }

	public synchronized void removeCachingTrack(Track track) {
		mCachingTracks.remove(track.getId());
	}

	public synchronized void addCachingTrack(Track track) {
		mCachingTracks.put(track.getId(), track);
	}

	public Track getCachingTrack(long trackId) {
		return mCachingTracks.get(trackId);
	}

	// public boolean isCaching(MediaItem mediaItem) {
	// return mCachingVideoTracks.containsKey(mediaItem.getId());
	// }

	// public synchronized void clearCachingVideoTracks() {
	// mCachingVideoTracks.clear();
	// }

	public synchronized void removeCachingVideoTrack(MediaItem mediaItem) {
		mCachingVideoTracks.remove(mediaItem.getId());
	}

	public synchronized void addCachingVideoTrack(MediaItem mediaItem) {
		mCachingVideoTracks.put(mediaItem.getId(), mediaItem);
	}

	// public MediaItem getCachingVideoTrack(long trackId) {
	// return mCachingVideoTracks.get(trackId);
	// }

	// public static void createExternalTracksFolder(){
	//
	// File externalTracksFolder =
	// new File(context.getApplicationContext().getExternalCacheDir() + "/" +
	// TRACKS_FOLDER_NAME);
	// if (!externalTracksFolder.exists()) {
	// externalTracksFolder.mkdirs();
	// }
	// }

	// public static void deleteExternalTracksFolder(){
	//
	// Logger.i(TAG, "deleteExternalTracksFolder");
	//
	// File externalTracksFolder =
	// new
	// File(context.getApplicationContext().getExternalCacheDir(),TRACKS_FOLDER_NAME);//
	// + "/" + TRACKS_FOLDER_NAME);
	//
	// if (externalTracksFolder.exists()) {
	//
	// // Delete all files in directory
	// File[] files = externalTracksFolder.listFiles();
	//
	// if(files == null || files.length == 0){
	// externalTracksFolder.delete();
	// }else{
	// for(File file : files){
	// file.delete();
	// }
	// }
	//
	// context.getApplicationContext().getExternalCacheDir().delete();
	// }
	// }

	// public static Boolean getIsWorking(){
	// return isWorking;
	// }

	// public static void setIsWorking(Boolean value){
	// isWorking = value;
	// }

	// public static void setIsActive(Boolean value){
	// isActive = value;
	// }

	// CacheManager will receive a Track/Album/Artist/Player and will start the
	// DownloadingMangerSevice each time with next track in line
	@Deprecated
	private LinkedList<Track> tracksQueue = new LinkedList<Track>();
	// private ConcurrentLinkedQueue<Album> albumArtQueue = new
	// ConcurrentLinkedQueue<Album>();
	@Deprecated
	private ConcurrentLinkedQueue<Track> preCacheQueue = new ConcurrentLinkedQueue<Track>();

	private volatile boolean cancelSave = false;

	public boolean getCancelSaveStatus() {
		return cancelSave;
	}

	// private SerialExecutor saveQueueExector = new SerialExecutor();
	/**
	 * Uses serial executor to process the data serially and insert track into
	 * db queue.
	 * 
	 * @param item
	 *            to insert into queue.
	 */
	// public void addTracksToQueue(final Track item){
	//
	// if(Utils.isConnected()) {
	// saveQueueExector.execute(
	// new Runnable() {
	//
	// @Override
	// public void run() {
	// List<Track> list = new ArrayList<Track>(getTrackList(item));
	//
	// if(list != null && !list.isEmpty()){
	// for (Track i :list)
	// {
	// synchronized (mTrackQueueReceiver) {
	// mTrackQueueReceiver.add(i.getId());
	// }
	// }
	//
	// }
	//
	// if(!cancelSave)
	// fireIntent();
	//
	// }
	// });
	// } else {
	// Toast.makeText(context, context.getString(R.string.offline_operation),
	// Toast.LENGTH_LONG).show();
	// }
	//
	// /*Iterator<Itemable> iterator = list.iterator();
	//
	// Track track;
	// while(iterator.hasNext()){
	//
	// track = new Track();
	// track = (Track) iterator.next();
	//
	// tracksQueue.add(track);
	// }*/
	//
	// }

	/**
	 * Uses serial executor to process the data serially and unlock Tracks.
	 * 
	 * @param item
	 *            to insert into queue.
	 */
	// public void unlockTracks(final Track item){
	// saveQueueExector.execute( new Runnable() {
	//
	// @Override
	// public void run() {
	// List<Track> list = new ArrayList<Track>(getTrackList(item));
	//
	// if(list != null && !list.isEmpty()){
	// Track track;
	// for (Track i :list){
	// track = new Track();
	// track = (Track)i;
	// mLockingManager.releaseSong(track);
	// if(mOnCachingTrackLister != null){
	// track.setTrackState(Track.CACHED_STATE);
	// mOnCachingTrackLister.onFinishCachingTrack(track);
	// }
	// }
	// }
	//
	// }
	// });
	// }

	// /**
	// * @param item to be added to albumArt downloading queue
	// * Method adds album to albumArt cache queue
	// */
	// public boolean addAlbumsToQueue(Track item){
	//
	// ArrayList<Track> listOfItemable = null;
	//
	// if (item instanceof Track) {
	// Track track = (Track) item;
	// listOfItemable = HungamaApplication.getDatabaseManager().joinQuery(true,
	// new Album(), new TrackToAlbum(),
	// null, InventoryContract.TracksAlbums.TRACK_ID + " = "
	// + track.getId(), null);
	//
	// if(listOfItemable != null && !listOfItemable.isEmpty()) {
	// Album album;
	// for(Itemable i : listOfItemable) {
	// album = new Album();
	// album = (Album) i;
	// albumArtQueue.add(album);
	// }
	// }
	// } else if (item instanceof Album) {
	// albumArtQueue.add((Album) item);
	// }
	//
	// if(Utils.isConnected()) {
	// //Online
	// // fireIntentForAlbumArt();
	// return true;
	// } else {
	// //Offline return failure.
	// return false;
	// }
	// }

	/**
	 * fires intent to DownloadingManagerService for downloading albumArt
	 */
	// public void fireIntentForAlbumArt(){
	// Intent intent = null;
	// if(albumArtQueue.peek() != null){
	// intent = new Intent(context, DownloaderService.class);
	// intent.putExtra(DownloaderService.DOWNLOAD_ITEMABLE,
	// albumArtQueue.poll());
	// intent.putExtra(DownloaderService.DOWNLOAD_TYPE,
	// DownloaderService.ALBUM_ART_SAVE);
	// context.startService(intent);
	// }
	// // fireIntent();
	// }
	/**
	 * marks song for precache IO - Sync & {@link fireIntent}
	 * 
	 * @param id
	 */
	// public void preCache(long id){
	// synchronized(mTrackQueueReceiver){
	// mTrackQueueReceiver.preCache(id);
	// if (!cancelSave)
	// fireIntent();
	// }
	//
	//
	// // ArrayList<Itemable> list = mDatabaseManager.query(new Track(),
	// InventoryContract.Tracks.ID + " = " + id, null);
	// // if (list != null && !list.isEmpty()){
	// // Track t = (Track)list.get(0);
	// // preCache(t);
	// // }
	// }

	// @Deprecated
	// public void preCache(Track track){
	// if (preCacheQueue != null){
	// preCacheQueue.clear();
	// preCacheQueue.add(track);
	// }
	// if (!cancelSave)
	// fireIntent();
	// }

	/**
	 * Insert to front of Save queue.
	 * 
	 * @param track
	 *            to be inserted to front
	 */
	@Deprecated
	public void addToFrontQueue(Track track) {
		if (tracksQueue != null && track != null) {
			Logger.i(TAG, tracksQueue.toString());
			synchronized (tracksQueue) {
				tracksQueue.addFirst(track);
			}
			Logger.i(TAG, tracksQueue.toString());
		}
	}

	private void fireIntent() {
		Intent intent = null;

		// synchronized (mTrackQueueReceiver) {
		//
		// long id = mTrackQueueReceiver.peek();
		//
		// if (id != TrackQueueReceiver.EMPTY_ID){
		// Logger.i(TAG, "Fired on - " + id);
		// Track trackIntent = new Track(id,"","","","","");
		// intent = new Intent(context, DownloaderService.class);
		// intent.putExtra(DownloaderService.DOWNLOAD_ITEMABLE, trackIntent);
		// if (mTrackQueueReceiver.preCacheFlag())
		// intent.putExtra(DownloaderService.DOWNLOAD_TYPE,
		// DownloaderService.TRACK_CACHE);
		//
		// context.startService(intent);
		// }
		// }

		// if(preCacheQueue.peek() != null){
		//
		// Logger.i(TAG, "PRECACHE - " + preCacheQueue.toString());
		// intent = new Intent(context, DownloaderService.class);
		// intent.putExtra(DownloaderService.DOWNLOAD_ITEMABLE,
		// preCacheQueue.poll());
		// intent.putExtra(DownloaderService.DOWNLOAD_TYPE,
		// DownloaderService.TRACK_CACHE);
		// context.startService(intent);
		// }else if (tracksQueue.peek() != null){
		// synchronized (tracksQueue) {
		// Logger.i(TAG, tracksQueue.toString());
		// intent = new Intent(context, DownloaderService.class);
		// intent.putExtra(DownloaderService.DOWNLOAD_ITEMABLE,
		// tracksQueue.poll());
		// context.startService(intent);
		// }
		//
		// }
	}

	public void cancelSave() {
		this.cancelSave = true;
		Logger.i(TAG, "cancelSave");
	}

	public void resumeSave() {
		Logger.i(TAG, "resumeSave");
		this.cancelSave = false;
		fireIntent();
	}

	/**
	 * @param item
	 *            Track, Album, Artist, Play-list
	 * @return List of Tracks to download (save)
	 */
	private List<Track> getTrackList(Track item) {

		List<Track> list = null;
		Track dumbTrack = new Track();

		if (item instanceof Track) {

			// list = mDatabaseManager.query(dumbTrack,
			// InventoryContract.Tracks.ID + " = " + item.getId() ,
			// null);

		}/*
		 * else if(item instanceof Album){
		 * 
		 * if(item.getId() < 0 && ((Album) item).getArtistId() > 0) { list =
		 * mDatabaseManager.query(dumbTrack, InventoryContract.Tracks.ARTIST_ID
		 * + " = " + ((Album) item).getArtistId(), null); } else { list =
		 * mDatabaseManager.joinQuery(true,dumbTrack, new TrackToAlbum(), null,
		 * InventoryContract.TracksAlbums.ALBUM_ID + " = " + item.getId() ,
		 * InventoryContract.TracksAlbums.TRACK_NUMBER); }
		 * 
		 * // query(dumbTrack, // InventoryContract.Tracks.ALBUM_ID + " = " +
		 * item.getId() , // InventoryContract.Tracks.TRACK_NUMBER);
		 * 
		 * }else if(item instanceof Artist){
		 * 
		 * list = mDatabaseManager.query(dumbTrack,
		 * InventoryContract.Tracks.ARTIST_ID + " = " + item.getId() , null);
		 * 
		 * }else if(item instanceof Playlist){
		 * 
		 * list = mDatabaseManager.getTracksListFromPlaylist((Playlist)item);
		 * }else if (item instanceof Genre){
		 * 
		 * Itemable[] items = {new Track(), new MediaGenre()}; list =
		 * mDatabaseManager.joinQuery(true, items,
		 * InventoryContract.MediaGenres.GENRE_ID + " = " + item.getId(), null);
		 * }
		 */

		return list;
	}

	@Override
	public void onStartSaveListtener(Track track) {

		if (mOnCachingTrackLister != null) {

			mOnCachingTrackLister.onStartCachingTrack(track);

			// mCachingTracks.put(track.getId()[Track.TRACK_ID_KEY_INDEX],
			// track);
			addCachingTrack(track);

		}
	}

	@Override
	public void onFinishSaveListener(Track track) {
		if (track == null) {
			Logger.i(TAG, "Track is null");
		}

		// mTrackQueueReceiver.poll(track.getId());
		// if(!cancelSave){
		// fireIntent();
		// }
		//
		// // cachedTracks.addLast(track);
		// // mDatabaseManager.saveCachedLinkedList(cachedTracks);
		// mLockingManager.lockSong(track);
		removeCachingTrack(track);
		removeCachedTrackFromList(track);

		// setCachedAlbumStatus(track.getPrimaryId());

		if (mOnCachingTrackLister != null) {
			track.setTrackState(Track.LOCKED_STATE);
			// mCachingTracks.remove(track.getId()[Track.TRACK_ID_KEY_INDEX]);

			mOnCachingTrackServiceLister.onFinishCachingTrack(track);
			mOnCachingTrackLister.onFinishCachingTrack(track);
		}
		ApsalarEvent.postEvent(HungamaApplication.getContext(), ApsalarEvent.SONG_CACHED);
		Intent TrackCached = new Intent(ACTION_TRACK_CACHED);
		Logger.i("onFinishedCaching", " SENDING BROADCAST TRACK_CACHED");
		HungamaApplication.getContext().sendBroadcast(TrackCached);
	}

	@Override
	public void onFailSaveListener(Track track) {
		// mCachingTracks.remove(track.getId()[Track.TRACK_ID_KEY_INDEX]);

		// if(!track.isDoNotRetry())
		// // song failed so retry
		// mTrackQueueReceiver.retry(track.getId());
		// else{
		// // if song was canceled check if clear precache
		// mTrackQueueReceiver.clearPreCache(track.getId());
		// }
		removeCachingTrack(track);
		if (!cancelSave) {
			fireIntent();
		}
		if (mOnCachingTrackLister != null) {
			mOnCachingTrackLister.onFailCachingTrack(track);
		}
	}

	@Override
	public void onUpdateSaveListener(Track track, int percent) {
		onUpdateCachingTrack(track, percent);
	}

	@Override
	public void onCachedSaveListener(Track track, boolean update) {

		// mTrackQueueReceiver.poll(track.getId());
		if (!cancelSave) {
			fireIntent();
		}
		if (update) {
			// mLockingManager.lockSong(track);
			if (mOnCachingTrackLister != null) {
				track.setTrackState(Track.LOCKED_STATE);
				mOnCachingTrackLister.onFinishCachingTrack(track);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.data.audiocaching.DownloaderService.
	 * OnSaveTrackListener
	 * #onStartSaveListtener(com.hungama.myplay.activity.data.
	 * dao.hungama.MediaItem)
	 */
	@Override
	public void onStartSaveListtener(MediaItem mediaItem) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.data.audiocaching.DownloaderService.
	 * OnSaveTrackListener
	 * #onFinishSaveListener(com.hungama.myplay.activity.data.
	 * dao.hungama.MediaItem)
	 */
	@Override
	public void onFinishSaveListener(MediaItem mediaItem) {
		if (mediaItem == null) {
			Logger.i(TAG, "Track is null");
		}

		removeCachingVideoTrack(mediaItem);
		// removeCachedTrackFromList(mediaItem);

		// if (mOnCachingTrackLister != null) {
		// track.setTrackState(Track.LOCKED_STATE);
		// //mCachingTracks.remove(track.getId()[Track.TRACK_ID_KEY_INDEX]);
		//
		//
		// mOnCachingTrackServiceLister.onFinishCachingTrack(track);
		// mOnCachingTrackLister.onFinishCachingTrack(track);
		// }
		ApsalarEvent.postEvent(HungamaApplication.getContext(), ApsalarEvent.VIDEO_CACHED);
		Intent TrackCached = new Intent(ACTION_VIDEO_TRACK_CACHED);
		Logger.i("onFinishedCaching",
				" SENDING BROADCAST ACTION_VIDEO_TRACK_CACHED");
		HungamaApplication.getContext().sendBroadcast(TrackCached);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.data.audiocaching.DownloaderService.
	 * OnSaveTrackListener
	 * #onFailSaveListener(com.hungama.myplay.activity.data.dao
	 * .hungama.MediaItem)
	 */
	@Override
	public void onFailSaveListener(MediaItem mediaItem) {
		removeCachingVideoTrack(mediaItem);
		if (!cancelSave) {
			fireIntent();
		}
		// if(mOnCachingTrackLister != null){
		// mOnCachingTrackLister.onFailCachingTrack(track);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.data.audiocaching.DownloaderService.
	 * OnSaveTrackListener
	 * #onUpdateSaveListener(com.hungama.myplay.activity.data.
	 * dao.hungama.MediaItem, int)
	 */
	@Override
	public void onUpdateSaveListener(MediaItem mediaItem, int percent) {
		onUpdateCachingTrack(mediaItem, percent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.data.audiocaching.DownloaderService.
	 * OnSaveTrackListener
	 * #onCachedSaveListener(com.hungama.myplay.activity.data.
	 * dao.hungama.MediaItem, boolean)
	 */
	@Override
	public void onCachedSaveListener(MediaItem mediaItem, boolean updateGUI) {
		if (!cancelSave) {
			fireIntent();
		}
		// if (update){
		// // mLockingManager.lockSong(track);
		// if (mOnCachingTrackLister != null){
		// track.setTrackState(Track.LOCKED_STATE);
		// mOnCachingTrackLister.onFinishCachingTrack(track);
		// }
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.data.audiocaching.DownloaderService.
	 * OnSyncTrackListener
	 * #onStartCachingTrack(com.hungama.myplay.activity.data.dao
	 * .hungama.MediaItem)
	 */
	@Override
	public void onStartCachingTrack(MediaItem mediaItem) {
		if (mediaItem == null)
			return;
		Logger.i(TAG, "Start Caching Track " + mediaItem.getTitle() + " "
				+ mediaItem.getId());

		addCachingVideoTrack(mediaItem);

		isWorking = true;

		cancelSave();
		// if we do double caching of the same track in different threads then
		// track will be deleted
		// from the map mCachingTracks resulting in bad UI behavior

		// if(mOnCachingTrackLister != null){
		// mOnCachingTrackLister.onStartCachingTrack(track);
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.data.audiocaching.DownloaderService.
	 * OnSyncTrackListener
	 * #onFinishCachingTrack(com.hungama.myplay.activity.data.
	 * dao.hungama.MediaItem)
	 */
	@Override
	public void onFinishCachingTrack(MediaItem mediaItem) {
		if (mediaItem == null)
			return;
		Logger.i(TAG, "Finish Caching Track " + mediaItem.getTitle() + " "
				+ mediaItem.getId());
		// DBOHandler.updateTrackListState(context, "" + track.getId(), "");
		numOfCached++;
		removeCachingVideoTrack(mediaItem);
		// removeCachedTrackFromList(track);

		if (isActive) {
			Logger.i(TAG, isActive + " Continue Pre-Caching");
			cacheTrackFromList(HungamaApplication.getContext(), null);
		} else {
			Logger.i(TAG, isActive + " Stop Pre-Caching");
		}
		isWorking = false;

		// if(mOnCachingTrackLister != null){
		// track.setTrackState(Track.CACHED_STATE);
		// mOnCachingTrackLister.onFinishCachingTrack(track);
		// mOnCachingTrackServiceLister.onFinishCachingTrack(track);
		// }
		resumeSave();
		ApsalarEvent.postEvent(HungamaApplication.getContext(), ApsalarEvent.VIDEO_CACHED);
		Intent TrackCached = new Intent(ACTION_VIDEO_TRACK_CACHED);
		Logger.i("onFinishedCaching",
				" SENDING BROADCAST ACTION_VIDEO_TRACK_CACHED");
		HungamaApplication.getContext().sendBroadcast(TrackCached);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.data.audiocaching.DownloaderService.
	 * OnSyncTrackListener
	 * #onUpdateCachingTrack(com.hungama.myplay.activity.data.
	 * dao.hungama.MediaItem, int)
	 */
	@Override
	public void onUpdateCachingTrack(MediaItem mediaItem, int percent) {
		if (mediaItem != null) {
			// MediaItem cachingTrack = getCachingVideoTrack(mediaItem.getId());
			// if(cachingTrack != null){
			// cachingTrack.setProgress(percent);
			// }
			if (mediaItem != null) {
				Logger.s("onUpdateCachingTrack :::: " + mediaItem.getId()
						+ " ::: " + percent);
				DBOHandler.updateVideoTrackCacheProgress(context, ""
						+ mediaItem.getId(), percent);
				// mediaItem.setProgress(percent);
			}

			// if (mOnCachingTrackLister != null) {
			// mOnCachingTrackLister.onUpdateCachingTrack(track, percent);
			// }
			// Intent TrackCached = new Intent(ACTION_VIDEO_UPDATED_CACHE);
			// Logger.i("onFinishedCaching",
			// " SENDING BROADCAST ACTION_VIDEO_UPDATED_CACHE");
			// HungamaApplication.getContext().sendBroadcast(TrackCached);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.hungama.myplay.activity.data.audiocaching.DownloaderService.
	 * OnSyncTrackListener
	 * #onFailCachingTrack(com.hungama.myplay.activity.data.dao
	 * .hungama.MediaItem, boolean)
	 */
	@Override
	public void onFailCachingTrack(MediaItem mediaItem, boolean update) {
		if (mediaItem == null)
			return;

		DBOHandler.updateVideoTrackCacheState(context, "" + mediaItem.getId(),
				DataBase.CacheState.NOT_CACHED.toString());
		// DBOHandler.updateTrackListFailedState(context, "" + track.getId(),
		// "");
		resumeSave();
		Logger.i(TAG, "Fail Caching Track " + mediaItem.getTitle() + " "
				+ mediaItem.getId());
		removeCachingVideoTrack(mediaItem);
		isWorking = false;

		// if (mOnCachingTrackLister != null && update) {
		// mOnCachingTrackLister.onFailCachingTrack(track);
		// }

		if (DBOHandler.isVideoTrackExist(HungamaApplication.getContext(), ""
				+ mediaItem.getId())) {
			Toast.makeText(
					HungamaApplication.getContext(),
					"Video "
							+ mediaItem.getTitle()
							+ " could not be downloaded due to low network bandwidth.",
					Toast.LENGTH_SHORT).show();
		}

		Intent TrackCached = new Intent(ACTION_VIDEO_TRACK_CACHED);
		Logger.i("onFinishedCaching",
				" SENDING BROADCAST ACTION_VIDEO_TRACK_CACHED Failed");
		HungamaApplication.getContext().sendBroadcast(TrackCached);
	}

	// public static long getAvailableCacheMemory(){
	// long size = MemoryInfoHelper.getTotalExternalMemorySizeLong();
	// if(size==-1){
	// size = MemoryInfoHelper.getTotalInternalMemorySizeLong();
	// }
	// size -= SAFETY_BUFFER;
	// return size;
	// }

	public static String getAvailableFormattedCacheMemory() {
		long size = MemoryInfoHelper.getTotalExternalMemorySizeLong();
		if (size == -1) {
			size = MemoryInfoHelper.getTotalInternalMemorySizeLong();
		}
		size -= SAFETY_BUFFER;
		return MemoryInfoHelper.formatSize(size);
	}

	// public static long getLimitedCacheMemory(Context context){
	// long size = MemoryInfoHelper.getTotalExternalMemorySizeLong();
	// if(size==-1){
	// size = MemoryInfoHelper.getTotalInternalMemorySizeLong();
	// }
	// size -= SAFETY_BUFFER;
	// ApplicationConfigurations appConfig =
	// ApplicationConfigurations.getInstance(context);
	// size = size * appConfig.getSaveOfflineMemoryAllocatedPercentage() / 100;
	// return size;
	// }

	public static String getLimitedFormattedCacheMemory(Context context) {
		long size = MemoryInfoHelper.getTotalExternalMemorySizeLong();
		if (size == -1) {
			size = MemoryInfoHelper.getTotalInternalMemorySizeLong();
		}
		size -= SAFETY_BUFFER;
		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(context);
		size = size * appConfig.getSaveOfflineMemoryAllocatedPercentage() / 100;
		return MemoryInfoHelper.formatSize(size);
	}

	public static boolean isCacheFull(Context context) {
		try {
			File cacheFolder;
			long size = 0;
			if (MemoryInfoHelper.externalMemoryAvailable()) {
				cacheFolder = context.getApplicationContext()
						.getExternalCacheDir().getAbsoluteFile();
				size = MemoryInfoHelper.getTotalExternalMemorySizeLong();
			} else {
				cacheFolder = context.getApplicationContext().getCacheDir()
						.getAbsoluteFile();
				size = MemoryInfoHelper.getTotalInternalMemorySizeLong();
			}
			if (cacheFolder.exists()) {
				long result1 = Utils.getFolderSize(cacheFolder);
				size -= SAFETY_BUFFER;
				ApplicationConfigurations appConfig = ApplicationConfigurations
						.getInstance(context);
				size = size
						* appConfig.getSaveOfflineMemoryAllocatedPercentage()
						/ 100;
				if (result1 >= size)
					return true;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	public static boolean isCacheSizeAllowed(Context context, int progress) {
		try {
			File cacheFolder;
			long size = 0;
			if (MemoryInfoHelper.externalMemoryAvailable()) {
				cacheFolder = context.getApplicationContext()
						.getExternalCacheDir().getAbsoluteFile();
				size = MemoryInfoHelper.getTotalExternalMemorySizeLong();
			} else {
				cacheFolder = context.getApplicationContext().getCacheDir()
						.getAbsoluteFile();
				size = MemoryInfoHelper.getTotalInternalMemorySizeLong();
			}
			if (cacheFolder.exists()) {
				long result1 = Utils.getFolderSize(cacheFolder);
				size -= SAFETY_BUFFER;
				// ApplicationConfigurations appConfig =
				// ApplicationConfigurations.getInstance(context);
				size = size * progress / 100;
				// result1 += SAFETY_BUFFER * 10;
				if (result1 >= size)
					return false;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return true;
	}

	public static boolean isMemoryFull() {
		long size = 0;
		if (MemoryInfoHelper.externalMemoryAvailable()) {
			size = MemoryInfoHelper.getAvailableExternalMemorySizeLong();
		} else {
			size = MemoryInfoHelper.getAvailableInternalMemorySizeLong();
		}
		if (size <= SAFETY_BUFFER) {
			return true;
		}
		return false;
	}

	public static boolean isProUser(Context context) {
//		 return true;
		try {
			Date userCurrentPlanValidityDate = null;
			Date today = new Date();
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(context);
//			if (userCurrentPlanValidityDate == null) {
				userCurrentPlanValidityDate = Utils
						.convertTimeStampToDate(mApplicationConfigurations
								.getUserSubscriptionPlanDate());

            if(!mApplicationConfigurations.isRealUser() && !Logger.allowPlanForSilentUser)
                return false;
//			}
			if (mApplicationConfigurations.isUserHasSubscriptionPlan()
					&& userCurrentPlanValidityDate != null
					&& (!today.after(userCurrentPlanValidityDate) ||
					DateUtils.isToday(userCurrentPlanValidityDate.getTime()))) {
				return true;
			}
			if (mApplicationConfigurations.isUserHasSubscriptionPlan()
					&& userCurrentPlanValidityDate != null
					&& !today.after(userCurrentPlanValidityDate)) {
				return true;
			}
			SubscriptionStatusResponse subscriptionStatusResponse = DataManager.getInstance(context).getStoredCurrentPlanNew();
			if (mApplicationConfigurations.isUserHasSubscriptionPlan()
					&& subscriptionStatusResponse != null && subscriptionStatusResponse.getSubscription()!=null
					&& subscriptionStatusResponse.getSubscription().getDaysRemaining()>0) {
				return true;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	public static boolean isTrialUser(Context context) {
		// return true;
		try {
			Date userCurrentPlanValidityDate = null;
			Date today = new Date();
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(context);
			if (userCurrentPlanValidityDate == null) {
				userCurrentPlanValidityDate = Utils
						.convertTimeStampToDate(mApplicationConfigurations
								.getUserSubscriptionPlanDate());
			}
			if (mApplicationConfigurations.isUserHasSubscriptionPlan()
					&& mApplicationConfigurations
							.isUserHasTrialSubscriptionPlan()
					&& userCurrentPlanValidityDate != null
					&& !today.after(userCurrentPlanValidityDate)) {
				return true;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	public static boolean isTrialOfferExpired(Context context) {
		// return true;
		try {
			Date userCurrentPlanValidityDate = null;
			Date today = new Date();
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(context);
			if (userCurrentPlanValidityDate == null) {
				userCurrentPlanValidityDate = Utils
						.convertTimeStampToDate(mApplicationConfigurations
								.getUserSubscriptionPlanDate());
			}
			if ((mApplicationConfigurations.isUserHasSubscriptionPlan()
					&& mApplicationConfigurations
							.isUserHasTrialSubscriptionPlan()
					&& userCurrentPlanValidityDate != null && today
						.after(userCurrentPlanValidityDate))
					|| mApplicationConfigurations
							.isUserTrialSubscriptionExpired()) {
				return true;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	// public static void showUpgradeDialog(Context context,
	// View.OnClickListener clickListener) {
	// // set up custom dialog
	// // Dialog upgradeDialog = new Dialog(context);
	// // upgradeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
	// //
	// upgradeDialog.setContentView(R.layout.dialog_upgrade_subscription_caching);
	// // upgradeDialog.setContentView(R.layout.dialog_upgrade_subscription);
	// // TextView title = (TextView)
	// upgradeDialog.findViewById(R.id.video_upgrade_custom_dialog_title_text);
	// //
	// title.setText(context.getResources().getString(R.string.video_player_upgrade_button_text).toUpperCase());
	// // TextView text = (TextView)
	// upgradeDialog.findViewById(R.id.video_upgrade_custom_dialog_text);
	// //
	// text.setText(context.getResources().getString(R.string.video_player_upgrade_text));
	// //
	// // ImageView closeButton = (ImageView)
	// upgradeDialog.findViewById(R.id.close_button);
	// // closeButton.setTag(upgradeDialog);
	// // closeButton.setOnClickListener(clickListener);
	// //
	// // Button upgradeButton = (Button)
	// upgradeDialog.findViewById(R.id.button_upgrade);
	// // upgradeButton.setTag(upgradeDialog);
	// // upgradeButton.setOnClickListener(clickListener);
	// Dialog upgradeDialog = new CustomUpgradeDialog(context,
	// R.style.Theme_Translucent_NoActionBar, clickListener);
	// //
	// upgradeDialog.findViewById(R.id.close_button).setOnClickListener(clickListener);
	// // upgradeDialog.findViewById(R.id.close_button).setTag(upgradeDialog);
	// //
	// upgradeDialog.findViewById(R.id.button_upgrade).setOnClickListener(clickListener);
	// // upgradeDialog.findViewById(R.id.button_upgrade).setTag(upgradeDialog);
	// upgradeDialog.setCancelable(true);
	// upgradeDialog.setCanceledOnTouchOutside(true);
	// upgradeDialog.show();
	// }

	// static class CustomUpgradeDialog extends Dialog{
	// private View.OnClickListener clickListener;
	// /**
	// * @param context
	// */
	// public CustomUpgradeDialog(Context context) {
	// super(context);
	// }
	//
	// /**
	// * @param context
	// * @param cancelable
	// * @param cancelListener
	// */
	// public CustomUpgradeDialog(Context context, boolean cancelable,
	// OnCancelListener cancelListener) {
	// super(context, cancelable, cancelListener);
	// }
	//
	// /**
	// * @param context
	// * @param theme
	// */
	// public CustomUpgradeDialog(Context context, int theme,
	// View.OnClickListener clickListener) {
	// super(context, theme);
	// this.clickListener = clickListener;
	// }
	//
	// @Override
	// protected void onCreate(Bundle savedInstanceState) {
	// super.onCreate(savedInstanceState);
	// // getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
	// WindowManager.LayoutParams.);
	// // getWindow().setDimAmount(0.7f);
	// requestWindowFeature(Window.FEATURE_NO_TITLE);
	// setContentView(R.layout.dialog_upgrade_subscription_caching);
	// getWindow().getDecorView().setBackgroundResource(R.color.upgrade_dialog_dim_color);
	// getWindow().setGravity(Gravity.CENTER);
	//
	// findViewById(R.id.close_button).setOnClickListener(clickListener);
	// findViewById(R.id.close_button).setTag(this);
	// findViewById(R.id.button_upgrade).setOnClickListener(clickListener);
	// findViewById(R.id.button_upgrade).setTag(this);
	// findViewById(R.id.button_try_it_free).setOnClickListener(clickListener);
	// findViewById(R.id.button_try_it_free).setTag(this);
	// }
	// }

	public static void saveOfflineAction(final Activity mContext,
			final MediaItem mediaItem, final Track track) {
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(context);
		if (CacheManager.isProUser(mContext)) {
			if (!Utils.isConnected()) {
				Utils.makeText(
						mContext,
						mContext.getResources()
								.getString(
										R.string.save_offline_error_network_connectivity),
						Toast.LENGTH_SHORT).show();
			} else if (!mApplicationConfigurations
					.getSaveOfflineOnCellularNetwork()
					&& !Utils.getNetworkType(mContext).equalsIgnoreCase(
							Utils.NETWORK_WIFI)) {
				// Toast.makeText(mContext,
				// mContext.getResources().getString(R.string.save_offline_error_network_connectivity),
				// Toast.LENGTH_SHORT).show();
				promptForCellular(mContext, null, mediaItem, track, false);
			} else if (CacheManager.isCacheFull(mContext)) {
				Utils.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.save_offline_error_cache_full),
						Toast.LENGTH_SHORT).show();
			} else if (CacheManager.isMemoryFull()) {
				Utils.makeText(
						mContext,
						mContext.getResources().getString(
								R.string.save_offline_error_memory_full),
						Toast.LENGTH_SHORT).show();
			} else {
				if (MediaCachingTaskNew.isEnabled)
					ThreadPoolManager.getInstance().submit(new MediaCachingTaskNew(mContext, mediaItem, track));
				else
					new MediaCachingTask(mContext, mediaItem, track).execute();

				if (mApplicationConfigurations
						.getSaveOfflineOnCellularNetwork()
						&& !Utils.getNetworkType(mContext).equalsIgnoreCase(
								Utils.NETWORK_WIFI)) {
					Utils.makeText(
							mContext,
							context.getResources().getString(
									R.string.cellular_turnned_on),
							Toast.LENGTH_SHORT).show();
				}
			}
		} else {

			CacheState cacheState = DBOHandler.getTrackCacheState(
					mContext.getApplicationContext(), "" + mediaItem.getId());
			if (cacheState != null
					&& (cacheState == CacheState.CACHED
							|| cacheState == CacheState.CACHING || cacheState == CacheState.QUEUED)) {
				return;
			}

			if (mediaItem != null
					&& mediaItem.getMediaContentType() != MediaContentType.VIDEO
					&& mediaItem.getMediaType() != MediaType.VIDEO
					&& DBOHandler.getAllTracks(mContext).size() < getFreeUserCacheLimit(mContext)
					&& mApplicationConfigurations.getFreeUserCacheCount() < getFreeUserCacheLimit(mContext)) {
				if (CacheManager.isMemoryFull()) {
					Utils.makeText(
							mContext,
							mContext.getResources().getString(
									R.string.save_offline_error_memory_full),
							Toast.LENGTH_SHORT).show();
				} else {
					if (MediaCachingTaskNew.isEnabled)
						ThreadPoolManager.getInstance().submit(new MediaCachingTaskNew(mContext, mediaItem, track,
								true));
					else
						new MediaCachingTask(mContext, mediaItem, track, true)
								.execute();
				}
			} else {
				Intent intent = new Intent(mContext, UpgradeActivity.class);
				intent.putExtra(UpgradeActivity.IS_TRIAL_PLANS, true);
				intent.putExtra(
						UpgradeActivity.EXTRA_DATA_ORIGIN_MEDIA_CONTENT_TYPE,
						(Serializable) mediaItem.getMediaContentType());
				intent.putExtra(UpgradeActivity.EXTRA_DATA_ORIGIN_MEDIA_ITEM,
						mediaItem);
				intent.putExtra(UpgradeActivity.EXTRA_DATA_ORIGIN_MEDIA_TRACK,
						track);
				mContext.startActivityForResult(intent,
						HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
			}
		}
	}

	@SuppressLint("DefaultLocale")
	public static void saveAllTracksOfflineAction(final Activity mContext,
			final List<Track> mTracks) {
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(context);
		if (CacheManager.isProUser(mContext)) {
			if (!Utils.isConnected()) {
				Utils.makeText(
						mContext,
						mContext.getResources()
								.getString(
										R.string.save_offline_error_network_connectivity),
						Toast.LENGTH_SHORT).show();
			} else if (!mApplicationConfigurations
					.getSaveOfflineOnCellularNetwork()
					&& !Utils.getNetworkType(mContext).equalsIgnoreCase(
							Utils.NETWORK_WIFI)) {
				// Toast.makeText(mContext,
				// mContext.getResources().getString(R.string.save_offline_error_network_connectivity),
				// Toast.LENGTH_SHORT).show();
				// promptForCellular(mContext);
				promptForCellular(mContext, mTracks, null, null, false);
			} else if (CacheManager.isCacheFull(mContext)) {
				Utils.makeText(
						mContext,
						Utils.getMultilanguageText(
								mContext,
								mContext.getResources().getString(
										R.string.save_offline_error_cache_full)),
						Toast.LENGTH_SHORT).show();
			} else if (CacheManager.isMemoryFull()) {
				Utils.makeText(
						mContext,
						Utils.getMultilanguageText(
								mContext,
								mContext.getResources()
										.getString(
												R.string.save_offline_error_memory_full)),
						Toast.LENGTH_SHORT).show();
			} else {
				for (Track track : mTracks) {
					MediaItem mediaItem = new MediaItem(track.getId(),
							track.getTitle(), track.getAlbumName(),
							track.getArtistName(), track.getImageUrl(),
							track.getBigImageUrl(), MediaType.TRACK.name()
									.toLowerCase(), 0, 0, track.getImages(),
							track.getAlbumId());
					if (MediaCachingTaskNew.isEnabled)
						ThreadPoolManager.getInstance().submit(new MediaCachingTaskNew(mContext, mediaItem, track));
					else
						new MediaCachingTask(mContext, mediaItem, track)
								.execute();
				}
				if (mApplicationConfigurations
						.getSaveOfflineOnCellularNetwork()
						&& !Utils.getNetworkType(mContext).equalsIgnoreCase(
								Utils.NETWORK_WIFI)) {
					Utils.makeText(
							mContext,
							context.getResources().getString(
									R.string.cellular_turnned_on),
							Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			int tracksCached = DBOHandler.getAllTracks(mContext).size();
			// if(tracksCached>0)
			// tracksCached--;
			if (tracksCached < getFreeUserCacheLimit(mContext)
					&& mApplicationConfigurations.getFreeUserCacheCount() < getFreeUserCacheLimit(mContext)) {
				if (CacheManager.isMemoryFull()) {
					try {
						Utils.makeText(
								mContext,
								Utils.getMultilanguageText(
										mContext,
										mContext.getResources()
												.getString(
														R.string.save_offline_error_memory_full)),
								Toast.LENGTH_SHORT).show();
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				} else {
					for (Track track : mTracks) {
						if (tracksCached < getFreeUserCacheLimit(mContext)
								&& DBOHandler.getTrackCacheState(mContext, ""
										+ track.getId()) == CacheState.NOT_CACHED
								&& mApplicationConfigurations
										.getFreeUserCacheCount() < getFreeUserCacheLimit(mContext)) {
							tracksCached++;
							MediaItem mediaItem = new MediaItem(track.getId(),
									track.getTitle(), track.getAlbumName(),
									track.getArtistName(), track.getImageUrl(),
									track.getBigImageUrl(), MediaType.TRACK
											.name().toLowerCase(), 0,
									track.getAlbumId());
							if (MediaCachingTaskNew.isEnabled)
								ThreadPoolManager.getInstance().submit(new MediaCachingTaskNew(mContext, mediaItem,
										track));
							else
								new MediaCachingTask(mContext, mediaItem, track)
										.execute();
						} else if (tracksCached == getFreeUserCacheLimit(mContext)) {
							// Intent intent = new Intent(mContext,
							// UpgradeActivity.class);
							// intent.putExtra(UpgradeActivity.EXTRA_DATA_ORIGIN_MEDIA_CONTENT_TYPE,
							// (Serializable) MediaContentType.MUSIC);
							// mContext.startActivityForResult(intent,
							// HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
							break;
						}
					}
				}
			} else {
                try {
                    if(mContext instanceof  HomeActivity)
                        ((MainActivity) mContext).isSkipResume=true;
                }catch (Exception e){
                    e.printStackTrace();
                }

				Intent intent = new Intent(mContext, UpgradeActivity.class);
				intent.putExtra(
						UpgradeActivity.EXTRA_DATA_ORIGIN_MEDIA_CONTENT_TYPE,
						(Serializable) MediaContentType.MUSIC);
				mContext.startActivityForResult(intent,
						HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
			}
		}
	}

	@SuppressLint("DefaultLocale")
	public static void saveAllTracksOfflineAction(final Context mContext,
			final List<Track> mTracks) {
		if (CacheManager.isProUser(mContext)) {
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(context);
			if (!Utils.isConnected()) {
				Toast.makeText(
						mContext,
						mContext.getResources()
								.getString(
										R.string.save_offline_error_network_connectivity),
						Toast.LENGTH_SHORT).show();
			} else if (!mApplicationConfigurations
					.getSaveOfflineOnCellularNetwork()
					&& !Utils.getNetworkType(mContext).equalsIgnoreCase(
							Utils.NETWORK_WIFI)) {
				// Toast.makeText(mContext,
				// mContext.getResources().getString(R.string.save_offline_error_network_connectivity),
				// Toast.LENGTH_SHORT).show();
				// promptForCellular(mContext);
				promptForCellular(mContext, mTracks, null, null, false);
			} else if (CacheManager.isCacheFull(mContext)) {
				Toast.makeText(
						mContext,
						Utils.getMultilanguageText(
								mContext,
								mContext.getResources().getString(
										R.string.save_offline_error_cache_full)),
						Toast.LENGTH_SHORT).show();
			} else if (CacheManager.isMemoryFull()) {
				Toast.makeText(
						mContext,
						Utils.getMultilanguageText(
								mContext,
								mContext.getResources()
										.getString(
												R.string.save_offline_error_memory_full)),
						Toast.LENGTH_SHORT).show();
			} else {
				for (Track track : mTracks) {
					MediaItem mediaItem = new MediaItem(track.getId(),
							track.getTitle(), track.getAlbumName(),
							track.getArtistName(), track.getImageUrl(),
							track.getBigImageUrl(), MediaType.TRACK.name()
									.toLowerCase(), 0, 0, track.getImages(),
							track.getAlbumId());
					if (MediaCachingTaskNew.isEnabled)
						ThreadPoolManager.getInstance().submit(new MediaCachingTaskNew(mContext, mediaItem, track));
					else
						new MediaCachingTask(mContext, mediaItem, track)
								.execute();
				}
				if (mApplicationConfigurations
						.getSaveOfflineOnCellularNetwork()
						&& !Utils.getNetworkType(mContext).equalsIgnoreCase(
								Utils.NETWORK_WIFI)) {
					Toast.makeText(
							context,
							context.getResources().getString(
									R.string.cellular_turnned_on),
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	public static void saveOfflineResultValidation(Activity activity,
			int requestCode, int resultCode, Intent data) {
		if (requestCode == HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE
				&& resultCode == Activity.RESULT_OK) {
			if (data != null) {
				MediaItem mediaItem = (MediaItem) data
						.getSerializableExtra(UpgradeActivity.EXTRA_DATA_ORIGIN_MEDIA_ITEM);
				Track track = (Track) data
						.getSerializableExtra(UpgradeActivity.EXTRA_DATA_ORIGIN_MEDIA_TRACK);
				if (mediaItem != null)
					Logger.s("MediaItem :::::::::: " + mediaItem.getTitle());
				if (track != null)
					Logger.s("Track :::::::::: " + track.getTitle());
				if (mediaItem != null || track != null)
					CacheManager.saveOfflineAction(activity, mediaItem, track);
			}
		}
	}

	public static void autoSaveOfflinePlayerQueue(final Context mContext,
			final MediaItem mediaItem, final Track track) {
		if (CacheManager.isProUser(mContext)) {
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(context);
			if (!Utils.isConnected()) {
				if (mApplicationConfigurations
						.getSaveOfflineOnCellularNetwork()
						&& !Utils.getNetworkType(mContext).equalsIgnoreCase(
								Utils.NETWORK_WIFI)) {
					Toast.makeText(
							context,
							context.getResources().getString(
									R.string.cellular_turnned_on),
							Toast.LENGTH_SHORT).show();
				}
				// Toast.makeText(mContext,
				// mContext.getResources().getString(R.string.save_offline_error_network_connectivity),
				// Toast.LENGTH_SHORT).show();
			} else if (!mApplicationConfigurations
					.getSaveOfflineOnCellularNetwork()
					&& !Utils.getNetworkType(mContext).equalsIgnoreCase(
							Utils.NETWORK_WIFI)) {
				// Toast.makeText(mContext,
				// mContext.getResources().getString(R.string.save_offline_error_network_connectivity),
				// Toast.LENGTH_SHORT).show();
				// promptForCellular(mContext);
				promptForCellular(mContext, null, mediaItem, track, true);
			} else if (CacheManager.isCacheFull(mContext)) {
				Toast.makeText(
						mContext,
						Utils.getMultilanguageText(
								mContext,
								mContext.getResources().getString(
										R.string.save_offline_error_cache_full)),
						Toast.LENGTH_SHORT).show();
			} else if (CacheManager.isMemoryFull()) {
				Toast.makeText(
						mContext,
						Utils.getMultilanguageText(
								mContext,
								Utils.getMultilanguageText(
										mContext,
										mContext.getResources()
												.getString(
														R.string.save_offline_error_memory_full))),
						Toast.LENGTH_SHORT).show();
			} else {
				if (MediaCachingTaskNew.isEnabled)
					ThreadPoolManager.getInstance().submit(new MediaCachingTaskNew(mContext, mediaItem, track, true));
				else
					new MediaCachingTask(mContext, mediaItem, track, true)
							.execute();
				if (mApplicationConfigurations
						.getSaveOfflineOnCellularNetwork()
						&& !Utils.getNetworkType(mContext).equalsIgnoreCase(
								Utils.NETWORK_WIFI)) {
					Toast.makeText(
							context,
							context.getResources().getString(
									R.string.cellular_turnned_on),
							Toast.LENGTH_SHORT).show();
				}
			}
		} else {
			if (!CacheManager.isMemoryFull()) {
				ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
						.getInstance(context);
				if (DBOHandler.getAllTracks(mContext).size() < getFreeUserCacheLimit(mContext)
						&& mApplicationConfigurations
								.getSaveOfflineAutoSaveFreeUser() == 0
						&& mApplicationConfigurations.getFreeUserCacheCount() < getFreeUserCacheLimit(mContext)) {
					// mApplicationConfigurations.setSaveOfflineAutoSaveFreeUser(mediaItem.getId());
					// new Thread(){
					// public void run() {
					// Looper.prepare();
					// new MediaCachingTask(mContext, mediaItem, track,
					// true).execute();
					// Looper.loop();
					// };
					// }.start();
					if (MediaCachingTaskNew.isEnabled)
						ThreadPoolManager.getInstance().submit(new MediaCachingTaskNew(mContext, mediaItem, track,
								true));
					else
						new MediaCachingTask(mContext, mediaItem, track, true)
								.execute();
				}
			}
		}
	}

	// private static void promptForCellular(final Context context){
	// try{
	// AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
	// alertBuilder.setMessage(context.getResources().getString(R.string.turn_on_cellular_message));
	// alertBuilder.setPositiveButton("OK",
	// new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface arg0, int arg1) {
	// ApplicationConfigurations mApplicationConfigurations =
	// ApplicationConfigurations.getInstance(context);
	// mApplicationConfigurations.setSaveOfflineOnCellularNetwork(true);
	// Toast.makeText(context,
	// context.getResources().getString(R.string.cellular_turnned_on),
	// Toast.LENGTH_SHORT).show();
	// }
	// });
	// alertBuilder.setNegativeButton(context.getResources().getString(R.string.caching_text_popup_button_cancel),
	// null);
	// alertBuilder.create().show();
	// } catch (Exception e) {
	// System.out.println("-----------------------------------10 " +
	// e.toString());
	// Logger.printStackTrace(e);
	// }
	// }

	private static void promptForCellular(final Context context,
			final List<Track> mTracks, final MediaItem mediaItem,
			final Track track, final boolean isAutoSave) {
		try {
			CustomAlertDialog alertBuilder = new CustomAlertDialog(context);
			alertBuilder.setMessage(Utils.getMultilanguageText(
					context,
					context.getResources().getString(
							R.string.turn_on_cellular_message_1))
					+ "\n"
					+ Utils.getMultilanguageText(
							context,
							context.getResources().getString(
									R.string.turn_on_cellular_message_2)));
			alertBuilder.setPositiveButton(
					Utils.getMultilanguageText(context, "OK"),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
									.getInstance(context);
							mApplicationConfigurations
									.setSaveOfflineOnCellularNetwork(true);
							Toast.makeText(
									context,
									context.getResources().getString(
											R.string.cellular_turnned_on),
									Toast.LENGTH_SHORT).show();
							if (mTracks != null) {
								saveAllTracksOfflineAction(context, mTracks);
							} else if (isAutoSave) {
								autoSaveOfflinePlayerQueue(context, mediaItem,
										track);
							} else {
								saveOfflineAction((Activity) context,
										mediaItem, track);
							}
						}
					});
			alertBuilder.setNegativeButton(Utils.getMultilanguageText(
					context,
					context.getResources().getString(
							R.string.caching_text_popup_button_cancel)), null);
			// alertBuilder.create();
			alertBuilder.show();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public static void loadNotCachedTrack(Context context) {
		List<MediaItem> mMediaItems = null;
		if (isProUser(context)) {
			mMediaItems = DBOHandler.getAllNotCachedTracks(context);
		} else {
			mMediaItems = DBOHandler.getAllNotCachedTracksForFreeUser(context);
		}
		if (mMediaItems != null && mMediaItems.size() > 0) {
			Logger.s("loadNotCachedTrack ::::::: " + mMediaItems.size());
			for (MediaItem mediaItem : mMediaItems) {
				Track track = new Track(mediaItem.getId(),
						mediaItem.getTitle(), mediaItem.getAlbumName(),
						mediaItem.getArtistName(), mediaItem.getImageUrl(),
						mediaItem.getBigImageUrl(), mediaItem.getImages(),
						mediaItem.getAlbumId());
				if (MediaCachingTaskNew.isEnabled)
					ThreadPoolManager.getInstance().submit(new MediaCachingTaskNew(context, mediaItem, track));
				else
					new MediaCachingTask(context, mediaItem, track).execute();
			}
		} else
			Logger.s("loadNotCachedTrack ::::::: null");

		if (isProUser(context)) {
			List<MediaItem> mMediaItemsVideo = DBOHandler
					.getAllNotCachedVideoTracks(context);
			for (MediaItem mediaItem : mMediaItemsVideo) {
				if (MediaCachingTaskNew.isEnabled)
					ThreadPoolManager.getInstance().submit(new MediaCachingTaskNew(context, mediaItem, null));
				else
					new MediaCachingTask(context, mediaItem, null).execute();
			}
		}
	}

	public static void resumeCachingStoppedTrack(Context context) {
		if (isProUser(context)) {
			DBOHandler.updateStateForCachingStoppedTracks(context);
			DBOHandler.updateStateForCachingStoppedVideoTracks(context);
		} else {
			DBOHandler.updateStateForCachingStoppedTracks(context);
		}
	}

	public void reset() {
		File tracksFolder = new File(context.getApplicationContext()
				.getCacheDir() + "/" + TRACKS_FOLDER_NAME);
		if (!tracksFolder.exists()) {
			tracksFolder.mkdirs();
		}

		File externalTracksFolder = null;
		try {
			externalTracksFolder = new File(
					Environment.getExternalStorageDirectory()
							+ "/.hungama/" + TRACKS_FOLDER_NAME);
			if (!externalTracksFolder.exists()) {
				externalTracksFolder.mkdirs();
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		File unecryptedExternalTracksFolder = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
						+ "/"
						+ context.getResources().getString(
								R.string.application_name));
		if (!unecryptedExternalTracksFolder.exists()) {
			unecryptedExternalTracksFolder.mkdirs();
		}

		// File albumsFolder = new File(imageFolder + "/" + ALBUMS_FOLDER_NAME);
		// if (!albumsFolder.exists()) {
		// albumsFolder.mkdir();
		// }
		//
		// File campaignsFolder = new File(imageFolder + "/" +
		// CAMPAIGNS_FOLDER_NAME);
		// if (!campaignsFolder.exists()) {
		// campaignsFolder.mkdir();
		// }
		//
		// mImageFolderPath = imageFolder.getAbsolutePath();
		mTracksFolderPath = tracksFolder.getAbsolutePath();
		if (externalTracksFolder != null)
			mTracksExternalFolderPath = externalTracksFolder.getAbsolutePath();
		mTracksUnecrryptedFolderPath = unecryptedExternalTracksFolder
				.getAbsolutePath();
		// mCampaignsFolderPath = campaignsFolder.getAbsolutePath();

		File videoTracksFolder = new File(context.getApplicationContext()
				.getCacheDir() + "/" + VIDEO_TRACKS_FOLDER_NAME);
		if (!videoTracksFolder.exists()) {
			videoTracksFolder.mkdirs();
		}

		try {
			File externalVideoTracksFolder;
			externalVideoTracksFolder = new File(
					Environment.getExternalStorageDirectory()
							+ "/.hungama/" + VIDEO_TRACKS_FOLDER_NAME);
			if (!externalVideoTracksFolder.exists()) {
				externalVideoTracksFolder.mkdirs();
			}
			mVideoTracksExternalFolderPath = externalVideoTracksFolder
					.getAbsolutePath();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}

		File unecryptedExternalVideoTracksFolder = new File(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
						+ "/"
						+ context.getResources().getString(
								R.string.application_name));
		if (!unecryptedExternalVideoTracksFolder.exists()) {
			unecryptedExternalVideoTracksFolder.mkdirs();
		}

		mVideoTracksFolderPath = videoTracksFolder.getAbsolutePath();
		mVideoTracksUnecrryptedFolderPath = unecryptedExternalVideoTracksFolder
				.getAbsolutePath();
	}

	public static final int getFreeUserCacheLimit(Context context) {
		return (ApplicationConfigurations.getInstance(context)
				.getFreeCacheLimit());
	}

	public static final void removeTrackFromCache(final Activity context,
			final long id, final MediaContentType mediaContentType) {
		try {
			CustomAlertDialog alertBuilder = new CustomAlertDialog(context);
			alertBuilder.setMessage(Utils.getMultilanguageText(
					context,
					context.getResources().getString(
							R.string.message_delete_track_from_cache)));
			alertBuilder.setPositiveButton(Utils.getMultilanguageText(
					context,
					context.getResources().getString(
							R.string.exit_dialog_text_yes)),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (mediaContentType == MediaContentType.MUSIC) {
								String path = DBOHandler.getTrackPathById(
										context, "" + id);
								if (path != null && path.length() > 0) {
									File file = new File(path);
									if (file.exists()) {
										file.delete();
									}
								}
								boolean isTracksDeleted = DBOHandler
										.deleteCachedTrack(context, "" + id);
								Logger.s(id + " ::::::delete:::::: "
										+ isTracksDeleted);
								if (isTracksDeleted) {
									Intent TrackCached = new Intent(
											CacheManager.ACTION_TRACK_CACHED);
									Logger.i("Update Cache State",
											" SENDING BROADCAST TRACK_CACHED");
									context.sendBroadcast(TrackCached);
									Utils.makeText(
											context,
											Utils.getMultilanguageText(
													context,
													context.getResources()
															.getString(
																	R.string.message_track_deleted)),
											Toast.LENGTH_SHORT).show();
								}
							} else if (mediaContentType == MediaContentType.VIDEO) {
								String path = DBOHandler.getVideoTrackPathById(
										context, "" + id);
								if (path != null && path.length() > 0) {
									File file = new File(path);
									if (file.exists()) {
										file.delete();
									}
								}
								boolean isTracksDeleted = DBOHandler
										.deleteCachedVideoTrack(context, ""
												+ id);
								Logger.s(id + " ::::::delete:::::: "
										+ isTracksDeleted);
								if (isTracksDeleted) {
									Intent TrackCached = new Intent(
											CacheManager.ACTION_VIDEO_TRACK_CACHED);
									Logger.i("Update Cache State",
											" SENDING BROADCAST VIDEO_TRACK_CACHED");
									context.sendBroadcast(TrackCached);
									Utils.makeText(
											context,
											Utils.getMultilanguageText(
													context,
													context.getResources()
															.getString(
																	R.string.message_track_deleted)),
											Toast.LENGTH_SHORT).show();
								}
							}
						}
					});
			alertBuilder.setNegativeButton(Utils.getMultilanguageText(
					context,
					context.getResources().getString(
							R.string.exit_dialog_text_no)), null);
			// alertBuilder.create();
			alertBuilder.show();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public static final void removePlayerBarTrackFromCache(
			final Activity context, final long id,
			final PlayerBarFragment playerBar) {
		try {
			CustomAlertDialog alertBuilder = new CustomAlertDialog(context);
			alertBuilder.setMessage(Utils.getMultilanguageText(
					context,
					context.getResources().getString(
							R.string.message_delete_track_from_cache)));
			alertBuilder.setPositiveButton(Utils.getMultilanguageText(
					context,
					context.getResources().getString(
							R.string.exit_dialog_text_yes)),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							String path = DBOHandler.getTrackPathById(context,
									"" + id);
							if (path != null && path.length() > 0) {
								File file = new File(path);
								if (file.exists()) {
									file.delete();
								}
							}
							boolean isTracksDeleted = DBOHandler
									.deleteCachedTrack(context, "" + id);
							Logger.s(id + " ::::::delete:::::: "
									+ isTracksDeleted);
							if (isTracksDeleted) {
								Intent TrackCached = new Intent(
										CacheManager.ACTION_TRACK_CACHED);
								Logger.i("Update Cache State",
										" SENDING BROADCAST TRACK_CACHED");
								context.sendBroadcast(TrackCached);
								Utils.makeText(
										context,
										Utils.getMultilanguageText(
												context,
												context.getResources()
														.getString(
																R.string.message_track_deleted)),
										Toast.LENGTH_SHORT).show();
								if (ApplicationConfigurations.getInstance(
										context).getSaveOfflineMode()) {
									ArrayList<Long> temp = new ArrayList<Long>();
									temp.add(id);
									playerBar.removeTrack(temp);
								}
							}
						}
					});
			alertBuilder.setNegativeButton(Utils.getMultilanguageText(
					context,
					context.getResources().getString(
							R.string.exit_dialog_text_no)), null);
			// alertBuilder.create();
			alertBuilder.show();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}
}