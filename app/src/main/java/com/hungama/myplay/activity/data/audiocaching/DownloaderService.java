package com.hungama.myplay.activity.data.audiocaching;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.operations.catchmedia.CMDecoratorOperation;
import com.hungama.myplay.activity.operations.catchmedia.MediaHandleOperation;
import com.hungama.myplay.activity.operations.hungama.HungamaOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;

public class DownloaderService extends IntentService {
	// TAG
	private static final String TAG = "DownloaderService";
	// Intent Keys
	public static final String DOWNLOAD_TYPE = "download_type";
	public static final String DOWNLOAD_ITEMABLE = "download_itemable";
	public static final String DOWNLOAD_MEDIA_ITEM = "download_media_item";
	// Download Type
	public static final int TRACK_CACHE = 6;
	public static final int VIDEO_TRACK_CACHE = 7;
	public static final int TRACK_SAVE = 1;
	// public static final int ALBUM_ART_SAVE = 5; // downloadType for saving
	// albumArt
	// Sync Object
	public static final Object lock = new Object();

	// Members
	// private DatabaseManager mDatabaseManager;
	private CacheManager mCacheManager;

	// status vars
	private boolean started = false;
	private Track mCurrentTrack = null;
	private int type = 0;
	private Video mCurrentVideo;
	private MediaItem mCurrentMediaItem;
	private Handler handler;

	private final int LOW_INTERNET_TIME = 2 * 60 * 1000;

	public DownloaderService() {
		super(TAG);
		// mDatabaseManager = HungamaApplication.getDatabaseManager();
		mCacheManager = HungamaApplication.getCacheManager();
		Logger.i(TAG, "start");
		handler = new Handler();
	}

	@Override
	public void onDestroy() {
		if (handler != null)
			handler.removeCallbacks(runnableLowConnecitivity);

		if (started && mCurrentTrack != null) {
			// Service was killed restart later

			if (type == TRACK_SAVE) {
				mCacheManager.addToFrontQueue(mCurrentTrack);
				saveListener.onFailSaveListener(mCurrentTrack);
			} else if (type == TRACK_CACHE) {
				if (mListener != null)
					mListener.onFailCachingTrack(mCurrentTrack, false);

				if (saveListener != null)
					saveListener.onFailSaveListener(mCurrentTrack);
				else
					mCacheManager.removeCachingTrack(mCurrentTrack);
			}

			mCacheManager.deleteTrack(mCurrentTrack);
			started = false;
		}
		Logger.i(TAG, "stop");
	}

	Runnable runnableLowConnecitivity = new Runnable() {
		@Override
		public void run() {
			try {
				Toast.makeText(DownloaderService.this,
						R.string.message_low_internet_connectivity,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	};

	Runnable runnableCacheFullError = new Runnable() {
		@Override
		public void run() {
			try {
				Toast.makeText(
						DownloaderService.this,
						Utils.getMultilanguageText(
								getApplicationContext(),
								getString(R.string.save_offline_error_cache_full)),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	};

	Runnable runnableMemoryFullError = new Runnable() {
		@Override
		public void run() {
			try {
				Toast.makeText(
						DownloaderService.this,
						Utils.getMultilanguageText(
								getApplicationContext(),
								getString(R.string.save_offline_error_memory_full)),
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	};

	@Override
	protected void onHandleIntent(Intent intent) {
		Logger.i(TAG, "onHandleIntent");
		try {
			synchronized (lock) {
				mCurrentTrack = null;
				int downloadType = intent
						.getIntExtra(DOWNLOAD_TYPE, TRACK_SAVE);
				Logger.s(" ------------------------------------ "
						+ downloadType);
				// check download type
				// if(downloadType == ALBUM_ART_SAVE) {
				// Album album = (Album)
				// intent.getSerializableExtra(DOWNLOAD_ITEMABLE);
				// Logger.i(TAG, "Downloading AlbumArt " + album.getName());
				// downloadAlbumArtToCache(album);
				//
				// }else
				if (downloadType == TRACK_CACHE) {
					Track track = (Track) intent
							.getSerializableExtra(DOWNLOAD_ITEMABLE);
					handler.postDelayed(runnableLowConnecitivity,
							LOW_INTERNET_TIME);
					handleTrackDownload(track);
					// mCacheManager.addAlbumsToQueue(track);
					// if
					// (!GlobalApplicationData.getDatabaseManager().getAppType().equalsIgnoreCase(GlobalApplicationData.INSENSE))
					// if (mDatabaseManager.isLyrics())
					// handleTrackLyrics(track);

				} else if (downloadType == VIDEO_TRACK_CACHE) {
					Video video = (Video) intent
							.getSerializableExtra(DOWNLOAD_ITEMABLE);
					MediaItem mediaItem = (MediaItem) intent
							.getSerializableExtra(DOWNLOAD_MEDIA_ITEM);
					handleVideoTrackDownload(video, mediaItem);
					// mCacheManager.addAlbumsToQueue(track);
					// if
					// (!GlobalApplicationData.getDatabaseManager().getAppType().equalsIgnoreCase(GlobalApplicationData.INSENSE))
					// if (mDatabaseManager.isLyrics())
					// handleTrackLyrics(track);

				}
				// else {
				// Track track = (Track)
				// intent.getSerializableExtra(DOWNLOAD_ITEMABLE);
				//
				// // update the track status.
				// ArrayList<Itemable> tmp = mDatabaseManager.query(track,
				// InventoryContract.Tracks.ID + " = " +
				// track.getId()[Track.TRACK_ID_KEY_INDEX], null);
				// if (tmp != null && !tmp.isEmpty())
				// track = (Track)tmp.get(0);
				// else
				// return;
				//
				// mCurrentTrack = track;
				// Logger.i(TAG, "Downloading Track - " + track.getName());
				//
				// if (mCacheManager.getCancelSaveStatus()){
				// // mCacheManager.addToFrontQueue(track);
				// return;
				// }
				// String trackNameId = track.getName() + " " +
				// track.getId()[Track.TRACK_ID_KEY_INDEX];
				// String url = track.getStreamUrl();
				// boolean encrypt = track.isEncrypted();
				// // Check if Track is not local media -> if it is then no need
				// to save it (it's already store on device)
				// String localMediaPath =
				// MediaScanUtil.getLocalMediaPath(String.valueOf(track.getId()[Track.TRACK_ID_KEY_INDEX]));
				// if(localMediaPath.equalsIgnoreCase("")){
				// // If the path is empty means that the Track is not local
				// media
				// if (!mCurrentTrack.isDoNotCache()){
				// if (CacheManager.getCacheTracksFolderPath(encrypt) != null){
				// if (!mCurrentTrack.isCached()){
				// if(!mCurrentTrack.isLocked()){
				//
				// Logger.i(TAG, "start save tracksListToSave: " + trackNameId);
				// started = true;
				//
				// saveListener.onStartSaveListtener(mCurrentTrack);
				//
				// //boolean success = downloadTrack(track);
				// boolean success = downloadTrackToCache(mCurrentTrack,
				// encrypt, url);
				//
				// if(success){
				// // Update the database that the track has been saved
				// mCurrentTrack.setCached(true,encrypt);
				// mDatabaseManager.update(mCurrentTrack);
				// saveListener.onFinishSaveListener(mCurrentTrack);
				// started = false;
				// Logger.i(TAG, "done save tracksListToSave: " + trackNameId);
				// }else{
				// saveListener.onFailSaveListener(mCurrentTrack);
				// mCacheManager.deleteTrack(mCurrentTrack);
				// started = false;
				// }
				// }else{
				// saveListener.onCachedSaveListener(mCurrentTrack,false);
				// started = false;
				// }
				// }else{
				// saveListener.onCachedSaveListener(mCurrentTrack,true);
				// started = false;
				// }
				// }else{
				// Toast.makeText(this,getResources().getString(R.string.free_up_space_msg)+"",
				// Toast.LENGTH_SHORT).show();
				// saveListener.onCachedSaveListener(mCurrentTrack,false);
				// started = false;
				// }
				// }else{
				// Toast.makeText(this,getResources().getString(R.string.do_not_cache_msg)+"",
				// Toast.LENGTH_SHORT).show();
				// saveListener.onCachedSaveListener(mCurrentTrack,false);
				// started = false;
				// }
				//
				// }else
				// saveListener.onCachedSaveListener(mCurrentTrack,false);
				// started = false;
				// }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// private void downloadAlbumArtToCache(Album album){
	// String fileName = Long
	// .toString(album.getId()[Album.ALBUM_ID_KEY_INDEX])
	// + CacheManager.IMAGE_FORMAT;
	// String folderName = album.getTableName();
	//
	// // check if albumArt is already cached
	// if (!mCacheManager.isImageExist(fileName, folderName) &&
	// !MediaArtView.failMap.containsKey(album.getPrimaryId())) {
	// // get the media art from the server.
	// String mediaArtUrl = mDatabaseManager.getMediaArtUrl(album, "album",
	// "jpg",
	// 400, 400);
	// Logger.i("MediaArt Download Thread", "Media url is: "
	// + mediaArtUrl);
	//
	// if (mediaArtUrl != null) {
	// try {
	// Bitmap mediaArt = MediaArtManager
	// .getImageFromUrl(mediaArtUrl);
	// // cache it in the cache manager.
	// mCacheManager.insertImage(fileName, folderName,
	// mediaArt);
	// Logger.i("MediaArt Download Thread",
	// "AlbumArt caching successful");
	// } catch (MalformedURLException e) {
	// e.printStackTrace();
	// } catch (OutOfMemoryError e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// } else {
	// Logger.i("MediaArt Download Thread",
	// "URL null, failed, returning");
	// }
	// } else {
	// Logger.i("MediaArt Download Thread",
	// "AlbumArt already cached || MediaArt doesn't exist on server");
	// }
	//
	// //again give call to fire intent in order to cache next albumArt
	// mCacheManager.fireIntentForAlbumArt();
	// }

	/**
	 * Downloads a saved song.
	 * 
	 * @param track
	 *            to download
	 * @param encrypt
	 *            flag
	 * @return success or failure.
	 */
	// private boolean downloadTrackToCache(Track track, boolean encrypt, String
	// url) {
	//
	// String downloadUrl = url;
	//
	// boolean doNotCache = track.isDoNotCache();
	// byte[] blob = track.getOwnerBlob();
	//
	// File outputFile = null;
	// FileOutputStream fos = null;
	// InputStream is = null;
	// if (doNotCache)
	// {
	// Toast.makeText(this,getResources().getString(R.string.do_not_cache_msg)+"",
	// Toast.LENGTH_SHORT).show();
	// return false;
	// }
	// if (mCacheManager.getCancelSaveStatus()){
	// // mCacheManager.addToFrontQueue(track);
	// return false;
	// }
	//
	// if (downloadUrl == null) {
	// return false;
	// }
	//
	// HttpURLConnection urlConnection = null;
	//
	// try {
	//
	// int totalBytes = 0;
	// urlConnection = Utils.httpHandler(downloadUrl);
	//
	// is = urlConnection.getInputStream();
	// totalBytes = urlConnection.getContentLength();
	//
	// int status = urlConnection.getResponseCode();
	// if (status != HttpURLConnection.HTTP_OK) {
	// throw new IOException("HTTP STATUS ERROR - " + status);
	// }
	//
	// if (totalBytes <= 0)
	// throw new IOException("Content length <= 0");
	//
	//
	// String cachingTracksFolder =
	// CacheManager.getCacheTracksFolderPath(encrypt);
	// String baseFileName = track.getId() + CacheManager.TRACK_FORMAT;
	// String tmpFileName = baseFileName + CacheManager.TRACK_PART_FORMAT;
	// //supplying it
	// outputFile = new File(cachingTracksFolder, tmpFileName);
	// fos = new FileOutputStream(outputFile);
	// byte[] buffer = new byte[1024*100];
	// int offset = -1;
	// CMEncryptor cmEncryptor = new CMEncryptor(mDatabaseManager);
	// ByteArrayOutputStream os = new ByteArrayOutputStream();
	// long lastProgressTimestamp = -1;
	// int bytesRead = 0;
	// int writtenBytes = 0;
	// boolean id3Found = false;
	// boolean firstRun = true;
	// boolean headerWritten = false;
	// boolean inserted = false;
	//
	// if (encrypt)
	// Logger.i(TAG,"START ENCRYPT " + track.getTitle());
	// else
	// Logger.i(TAG,"START DOWNLOAD " + track.getTitle());
	//
	// while ((bytesRead = is.read(buffer)) != -1) {
	// if (mCacheManager.getCancelSaveStatus()){
	// // mCacheManager.addToFrontQueue(mCurrentTrack);
	// mCurrentTrack.setDoNotRetry(true);
	// throw new IOException("Cancel Save");
	// }
	//
	//
	// if (encrypt){
	// cmEncryptor.encrypt(buffer, 0, bytesRead);
	// fos.write(buffer, 0, bytesRead);
	// writtenBytes += bytesRead;
	//
	// }else{
	//
	// if (firstRun){
	// //if id3 was found continue the parsing
	// firstRun = false;
	// if ((buffer[0] == 73 && buffer[1] == 68 && buffer[2] == 51)){
	// id3Found = true;
	// }else{
	// // If no Id3 was found Create it and prepend it to file.
	// if (fos != null){
	// byte[] chunk = MusicUtils.createId3(track);
	// fos.write(chunk);
	// inserted = true;
	// }
	// }
	// }
	//
	// if (id3Found){
	// if (!inserted){
	// // Havn't inserted blob buffer so we can scan.
	// os.write(buffer,0,bytesRead);
	//
	// if (os.size() >= 10 && !headerWritten){
	// // Enough data has been buffered we can rewrite the tagsize
	// ByteArrayInputStream head = new ByteArrayInputStream(os.toByteArray());
	// byte[] data = os.toByteArray();
	// byte[] headerbuf = new byte[10];
	// head.read(headerbuf);
	// // Read TagSize
	// int tagsize = (headerbuf[9] & 0xFF) | ((headerbuf[8] & 0xFF) << 7 ) |
	// ((headerbuf[7] & 0xFF) << 14 ) | ((headerbuf[6] & 0xFF) << 21 ) + 10;
	// tagsize += blob.length;
	// // Create byte representation of tagsize + blob size
	// byte[] bytes =
	// ByteBuffer.allocate(4).putInt(MusicUtils.synchsafe(tagsize)).array();
	//
	// // Write to file.
	// fos.write(headerbuf, 0, 6);
	// writtenBytes += 6;
	// fos.write(bytes);
	// writtenBytes += bytes.length;
	// // parse data for offset
	// // If offset is in chunk insert blob mid chunk
	// offset = MusicUtils.parse(os,TAG);
	// if (offset != -1){
	// fos.write(data,10,offset-10);
	// writtenBytes += offset-10;
	// fos.write(blob);
	// writtenBytes += blob.length;
	// fos.write(data, offset, data.length-offset);
	// writtenBytes += data.length - offset;
	// inserted = true;
	// os.close();
	// }else{
	// // Offset not found keep scanning.
	// fos.write(data, 10, data.length - 10);
	// writtenBytes += data.length - 10;
	// }
	// head.close();
	// headerWritten = true;
	// }else if (headerWritten){
	// // Header written parse for offset
	// offset = MusicUtils.parse(os, TAG);
	// if (offset != -1){
	// if (writtenBytes > offset)
	// offset = writtenBytes;
	// int upToOffset = ((writtenBytes + bytesRead) - offset);
	// upToOffset = bytesRead - upToOffset;
	// fos.write(buffer, 0, upToOffset);
	// writtenBytes += upToOffset;
	// fos.write(blob);
	// writtenBytes += blob.length;
	// fos.write(buffer, upToOffset, bytesRead - upToOffset);
	// writtenBytes += bytesRead - upToOffset;
	// inserted = true;
	// os.close();
	// }else{
	// // Offset not found keep scanning.
	// fos.write(buffer, 0, bytesRead);
	// writtenBytes += bytesRead;
	// }
	//
	// }
	//
	//
	// }else{
	// // Offset not found keep scanning.
	// fos.write(buffer, 0, bytesRead);
	// writtenBytes += bytesRead;
	// }
	//
	// }else{
	// fos.write(buffer, 0, bytesRead);
	// writtenBytes += bytesRead;
	// }
	//
	// }
	//
	// if(totalBytes > 0) {
	// int progress = (int)(writtenBytes * 100. / totalBytes);
	// long curTimestamp = new Date().getTime();
	// if(lastProgressTimestamp < 0 ||
	// Math.abs(lastProgressTimestamp - curTimestamp) >=
	// CacheManager.UPDATE_FREQUENCY_DELAY_ON_DOWNLOAD_MS) {
	// lastProgressTimestamp = curTimestamp;
	// saveListener.onUpdateSaveListener(track, progress);
	// }
	// }
	// }
	// if (encrypt)
	// Logger.i(TAG,"END ENCRYPT " + track.getTitle());
	// else
	// Logger.i(TAG,"END DOWNLOAD " + track.getTitle());
	//
	// fos.close();
	// is.close();
	//
	// if (writtenBytes < totalBytes)
	// throw new IOException("Track " + track.getTitle() + " " + track.getId() +
	// " is not completely downloaded");
	// else{
	// Logger.d(TAG, "setting cache info");
	//
	// //remove the CacheManager.TRACK_PROXY_FORMAT appendix
	// File fileFinal = new File(cachingTracksFolder, baseFileName);
	// if(fileFinal.exists() && !fileFinal.delete()) {
	// Logger.i(TAG, "Failed deleting before rename operation: " +
	// track.getTitle()
	// + " ID: " + track.getId());
	// outputFile.delete();
	// return false;
	// }
	// if(outputFile != null && outputFile.renameTo(fileFinal)) {
	// return true;
	// }
	// else {
	// Logger.i(TAG, "Failed renaming: " + track.getTitle() + " ID: " +
	// track.getId());
	// return false;
	// }
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	//
	//
	// if (urlConnection != null){
	// urlConnection.disconnect();
	// }
	//
	// try {
	// if(fos != null){
	// fos.close();
	// }
	// if(is != null){
	// is.close();
	// }
	// if(outputFile != null){
	// outputFile.delete();
	// }
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	//
	// }
	//
	// return false;
	// }

	private boolean handleTrackDownload(Track track) {
		if (!Utils.isConnected()) {
			if (mListener != null)
				mListener.onFailCachingTrack(track, false);

			if (saveListener != null)
				saveListener.onFailSaveListener(track);
			else
				mCacheManager.removeCachingTrack(track);
			return false;
		}

		long trackId = track.getId();
		boolean success = false;
		if (!DBOHandler.isTrackExist(getApplicationContext(), "" + trackId))
			return false;

		ApplicationConfigurations mAppConfig = ApplicationConfigurations
				.getInstance(this);
		if (mAppConfig.getSaveOfflineAutoSaveFreeUser() == 0) {
			mAppConfig.setSaveOfflineAutoSaveFreeUser(track.getId());
		}

		DBOHandler.updateTrackCacheState(getApplicationContext(), "" + trackId,
				DataBase.CacheState.CACHING.toString());
		// ArrayList<Itemable> tmp = mDatabaseManager.query(track,
		// InventoryContract.Tracks.ID + " = " + trackId , null);
		// if (tmp != null && !tmp.isEmpty())
		// track = (Track)tmp.get(0);
		// else
		// return success;

		mCurrentTrack = track;
		Logger.i(TAG, "Downloading Track - " + track.getTitle());

		if (mCacheManager.getCancelSaveStatus())
			return success;

		CommunicationManager communicationManager = new CommunicationManager();
		if (MediaCachingTaskNew.isEnabled) {
			try {
				MediaItem mediaItem = new MediaItem(track.getId(), null, null,
						null, null, null, MediaType.TRACK.toString(), 0,
						track.getAlbumId());
				ServerConfigurations mServerConfigurations = DataManager
						.getInstance(this).getServerConfigurations();
				String images = ImagesManager.getImageSize(
						ImagesManager.MUSIC_ART_SMALL,
						DataManager.getDisplayDensityLabel())
						+ ","
						+ ImagesManager.getImageSize(
								ImagesManager.MUSIC_ART_BIG,
								DataManager.getDisplayDensityLabel());
				Response response = communicationManager.performOperationNew(
						new MediaDetailsOperation(mServerConfigurations
								.getHungamaServerUrl_2(), mServerConfigurations
								.getHungamaAuthKey(), ApplicationConfigurations
								.getInstance(this).getPartnerUserId(),
								mediaItem, null, images), this);
				// Response response = communicationManager
				// .performOperationNew(
				// new MediaDetailsOperation(
				// mServerConfigurations
				// .getHungamaServerUrl_2(),
				// mServerConfigurations
				// .getHungamaAuthKey(),
				// ApplicationConfigurations.getInstance(this)
				// .getPartnerUserId(),
				// mediaItem, null, null), this);
				Logger.i("response", "Download Media Detail Response:"
						+ response);
				if (response != null/* && !isFileCached */) {
					try {
						DBOHandler.updateTrack(this, "" + track.getId(), "",
								response.response,
								DataBase.CacheState.CACHING.toString());
						sendBroadcast(new Intent(
								CacheManager.ACTION_CACHE_STATE_UPDATED));
						String album_id = "0", playlist_id = "0";
						if (track.getTag() != null) {
							try {
								Logger.e("playlist_id", "222222222222 > 1");
								MediaItem item = (MediaItem) track.getTag();
								if (item.getMediaType() == MediaType.ALBUM) {
									album_id = "" + item.getId();
								} else if (item.getMediaType() == MediaType.PLAYLIST) {
									Logger.e("playlist_id", "222222222222 > 2");
									playlist_id = "" + item.getId();
								}
							} catch (Exception e) {
								Logger.e("playlist_id", "222222222222 > 3");
							}
						}
						if (album_id.equals("0")) {
							try {

								JSONParser jsonParser = new JSONParser();
								Map<String, Object> catalogMap = (Map<String, Object>) jsonParser
										.parse(response.response);
								String reponseString;
								if (catalogMap
										.containsKey(HungamaOperation.KEY_RESPONSE)) {
									reponseString = catalogMap.get(
											HungamaOperation.KEY_RESPONSE)
											.toString();
								} else {
									throw new InvalidResponseDataException(
											"Parsing error - no catalog available");
								}

								// response.response =
								// response.response.replace(
								// "{\"catalog\":{\"content\":", "");
								// response.response = response.response
								// .substring(0,
								// response.response.length() - 2);

								album_id = ""
										+ (new Gson().fromJson(reponseString,
												MediaTrackDetails.class)
												.getAlbumId());
							} catch (Exception e) {
							}
						}
						if (!album_id.equals("0") || !playlist_id.equals("0"))
							DBOHandler
									.insertTrackToTrackListTable(this, ""
											+ track.getId(), "" + album_id,
											playlist_id);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}

			} catch (InvalidRequestException e) {
				e.printStackTrace();
				return false;
			} catch (InvalidResponseDataException e) {
				e.printStackTrace();
				return false;
			} catch (OperationCancelledException e) {
				e.printStackTrace();
				return false;
			} catch (NoConnectivityException e) {
				e.printStackTrace();
				return false;
			}
		}

		Map<String, Object> mediaHandleProperties = null;
		String mCMServerUrl = DataManager.getInstance(this)
				.getServerConfigurations().getServerUrl();
		try {
			mediaHandleProperties = communicationManager
					.performOperation(
							new CMDecoratorOperation(mCMServerUrl,
									new MediaHandleOperation(this, track
											.getId(), true)), this);
			if(mediaHandleProperties == null){
				boolean isTracksDeleted = DBOHandler.deleteCachedTrack(this, ""
						+ track.getId());
				Logger.s(track.getTitle() + " ::::::delete:::::: "
						+ isTracksDeleted);
				if (isTracksDeleted) {
					if (mAppConfig.getSaveOfflineAutoSaveFreeUser() == track
							.getId())
						mAppConfig.setSaveOfflineAutoSaveFreeUser(0);
					Intent TrackCached = new Intent(
							CacheManager.ACTION_TRACK_CACHED);
					Logger.i("Update Cache State",
							" SENDING BROADCAST TRACK_CACHED");
					sendBroadcast(TrackCached);
					mAppConfig.decreaseFreeUserCacheCount();
				}
				return false;
			}
			// populates the track with its playing properties.
			track.setMediaHandle((String) mediaHandleProperties
					.get(MediaHandleOperation.RESPONSE_KEY_HANDLE));
			track.setDeliveryId((Long) mediaHandleProperties
					.get(MediaHandleOperation.RESPONSE_KEY_DELIVERY_ID));
			track.setDoNotCache((Boolean) mediaHandleProperties
					.get(MediaHandleOperation.RESPONSE_KEY_DO_NOT_CACHE));

			String response = DBOHandler.getTrackDetails(this,
					"" + track.getId());
			try {

				// JSONParser jsonParser = new JSONParser();
				// Map<String, Object> catalogMap = (Map<String, Object>)
				// jsonParser
				// .parse(response);
				// String reponseString;
				// if (catalogMap
				// .containsKey(HungamaOperation.KEY_RESPONSE)) {
				// reponseString = catalogMap.get(
				// HungamaOperation.KEY_RESPONSE)
				// .toString();
				// } else {
				// throw new InvalidResponseDataException(
				// "Parsing error - no catalog available");
				// }

				JSONObject jsonResponse = new JSONObject(response);
				JSONObject jsonCatalog = jsonResponse.getJSONObject("response");
				// JSONObject jsonContent = new JSONObject(reponseString);
				jsonCatalog.put("delivery_id", track.getDeliveryId());

				DBOHandler.updateTrack(this, "" + track.getId(), "",
						jsonResponse.toString(), null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// MediaItem mediaItem = new MediaItem(track.getId(),
			// "Dummy", "Dummy album", null, null, null,
			// MediaType.TRACK.toString(), 0);
			// Intent intent = new Intent(this,
			// DownloadFileService2.class);
			// intent.putExtra(DownloadFileService2.TRACK_KEY,
			// (Serializable) mediaItem);
			// intent.putExtra(DownloadFileService2.DOWNLOAD_URL,
			// track.getMediaHandle());
			// startService(intent);
		} catch (InvalidRequestException e1) {
			e1.printStackTrace();
			return false;
		} catch (InvalidResponseDataException e1) {
			e1.printStackTrace();
			boolean isTracksDeleted = DBOHandler.deleteCachedTrack(this, ""
					+ track.getId());
			Logger.s(track.getTitle() + " ::::::delete:::::: "
					+ isTracksDeleted);
			if (isTracksDeleted) {
				if (mAppConfig.getSaveOfflineAutoSaveFreeUser() == track
						.getId())
					mAppConfig.setSaveOfflineAutoSaveFreeUser(0);
				Intent TrackCached = new Intent(
						CacheManager.ACTION_TRACK_CACHED);
				Logger.i("Update Cache State",
						" SENDING BROADCAST TRACK_CACHED");
				sendBroadcast(TrackCached);
				mAppConfig.decreaseFreeUserCacheCount();
			}
			return false;
		} catch (OperationCancelledException e1) {
			e1.printStackTrace();
			return false;
		} catch (NoConnectivityException e1) {
			e1.printStackTrace();
			return false;
		}

		mCurrentTrack = track;
		// if (!track.isLocal()){
		if (!track.isDoNotCache()) {
			if (CacheManager.getCacheTracksFolderPath(true) != null) {
				int cachingAttemps = 1;
				// TODO:: remove if file exists since we don't know if its
				// encrypted or not.
				if (mCacheManager.getTrack(trackId) != null
						&& track.getTrackState() != Track.STREAMABLE_STATE) {
					success = true;
				} else {
					started = true;
					if (saveListener != null)
						saveListener.onStartSaveListtener(mCurrentTrack);
					boolean checkApp = true; // mDatabaseManager.getAppType().equalsIgnoreCase(GlobalApplicationData.INSENSE)
					// ||
					// mDatabaseManager.getAppType().equalsIgnoreCase(GlobalApplicationData.ALBUM);
					while (!(success = downloadTrackToCache(track)) && checkApp) {
						if (!DBOHandler.isTrackExist(getBaseContext(), ""
								+ track.getId())) {
							break;
						}
						cachingAttemps++;

						try {
							Thread.sleep(30000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						Logger.i(
								TAG,
								"downloadTrackToCache( "
										+ mCurrentTrack.getTitle() + " "
										+ mCurrentTrack.getId() + " )"
										+ " Attempt # " + cachingAttemps);

						if (cachingAttemps > 1) {
							break;
						}
					}
				}
				if (handler != null)
					handler.removeCallbacks(runnableLowConnecitivity);

				if (success) {
					// update task in database.
					// track.setCached(true);
					// mDatabaseManager.update(track);
					//
					// //Logger.i(TAG, "setCached(true) on Track: " + " "+
					// track.getName() + " ID: " + track.getId());
					// CacheManager.cachedTracks.addLast(track);
					// mDatabaseManager.saveCachedLinkedList(CacheManager.cachedTracks);
					//
					// mCacheManager.removeCachingTrack(track);

					started = false;
					if (mListener != null)
						mListener.onFinishCachingTrack(mCurrentTrack);

				} else {
					started = false;

					if (mListener != null)
						mListener.onFailCachingTrack(mCurrentTrack, false);

					if (saveListener != null)
						saveListener.onFailSaveListener(mCurrentTrack);
					else
						mCacheManager.removeCachingTrack(track);

					// if (mListener != null)
					// mListener.onFailCachingTrack(mCurrentTrack,true);
				}
			}
		}
		// }
		return success;
	}

	private boolean downloadTrackToCache(Track track) {
		boolean result = false;
		if (!Utils.isConnected())
			return result;
		String downloadUrl = track.getMediaHandle();// track.getStreamUrl();
		byte[] blob = track.getOwnerBlob();
		boolean encrypt = true;// track.isEncrypted();
		String deviceID = "630358525";// DataManager.getInstance(getApplicationContext()).getApplicationConfigurations().getDeviceID();
		CMEncryptor cmEncryptor = new CMEncryptor(deviceID);
		Logger.s("---------------------  " + deviceID);

		// Logger.i(TAG, "downloadUrl for caching: " + downloadUrl);
		File outputFile = null;
		FileOutputStream fos = null;
		InputStream is = null;

		if (mCacheManager.getCancelSaveStatus())
			return result;

		if (downloadUrl == null) {
			return result;
		}

		if (DBOHandler.getTrackCacheState(getBaseContext(), "" + track.getId()) == CacheState.CACHED) {
			return true;
		}

		HttpURLConnection httpConnection = null;
		try {
			int totalBytes = 0;
			httpConnection = Utils.httpHandler(downloadUrl, this);

			int status = httpConnection.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				throw new IOException("HTTP STATUS ERROR - " + status);
			}

			is = httpConnection.getInputStream();
			totalBytes = httpConnection.getContentLength();
			if (totalBytes <= 0)
				throw new IOException("Content length <= 0");

			long size = 0;
			if (CacheManager.isProUser(this)) {
				File cacheFolder;
				if (MemoryInfoHelper.externalMemoryAvailable()) {
					cacheFolder = getExternalCacheDir().getAbsoluteFile();
					size = MemoryInfoHelper.getTotalExternalMemorySizeLong();
				} else {
					cacheFolder = getCacheDir().getAbsoluteFile();
					size = MemoryInfoHelper.getTotalInternalMemorySizeLong();
				}
				if (cacheFolder.exists()) {
					long result1 = Utils.getFolderSize(cacheFolder);
					size -= CacheManager.SAFETY_BUFFER;
					ApplicationConfigurations appConfig = ApplicationConfigurations
							.getInstance(this);
					size = size
							* appConfig
									.getSaveOfflineMemoryAllocatedPercentage()
							/ 100;
					if ((result1 + totalBytes) >= size) {
						handler.post(runnableCacheFullError);
						return false;
					}
				}
			}

			size = 0;
			if (MemoryInfoHelper.externalMemoryAvailable()) {
				size = MemoryInfoHelper.getAvailableExternalMemorySizeLong();
			} else {
				size = MemoryInfoHelper.getAvailableInternalMemorySizeLong();
			}
			if ((size - totalBytes) <= CacheManager.SAFETY_BUFFER) {
				handler.post(runnableMemoryFullError);
				return false;
			}

			String cachingTracksFolder = CacheManager
					.getCacheTracksFolderPath(encrypt);
			String baseFileName = "audio_" + track.getId()
					+ CacheManager.TRACK_FORMAT;
			String tmpFileName = baseFileName + CacheManager.TRACK_PART_FORMAT;

			// FileUtils fileUtils = new FileUtils(getApplicationContext());
			// File mediaFolder =
			// fileUtils.getStoragePath(MediaContentType.MUSIC);
			// String cachingTracksFolder = mediaFolder.getAbsolutePath();
			// String baseFileName = "audio_" + track.getId() + ".mp3";
			// String tmpFileName = baseFileName +
			// CacheManager.TRACK_PART_FORMAT;

			File trackFolder = new File(cachingTracksFolder);
			if (!trackFolder.exists())
				trackFolder.mkdirs();

			outputFile = new File(cachingTracksFolder, tmpFileName);
			if (!outputFile.exists())
				outputFile.createNewFile();
			fos = new FileOutputStream(outputFile);
			Logger.s("outputFile ::: " + outputFile.getAbsolutePath());
			byte[] buffer = new byte[1024 * 100];
			boolean firstRun = true;
			boolean id3Found = false;
			boolean inserted = false;
			boolean headerWritten = false;
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			int offset = -1;
			int bytesRead = 0;
			// int deviceId = Integer.parseInt(deviceID);
			// int _k = ((((int) deviceId) >> 24) ^ (((int) deviceId) >> 16)
			// ^ (((int) deviceId) >> 8) ^ ((int) deviceId)) & 255;
			if (encrypt)
				Logger.i(TAG, "START ENCRYPT " + track.getTitle());
			else
				Logger.i(TAG, "START DOWNLOAD " + track.getTitle());
			// int totalBytesRead = 0;
			long lastProgressTimestamp = -1;
			int writtenBytes = 0;
			// int totalBytes = connection.getContentLength();

			while ((bytesRead = is.read(buffer)) != -1) {
				if (!DBOHandler.isTrackExist(getBaseContext(),
						"" + track.getId())) {
					break;
				}
				if (mCacheManager.getCancelSaveStatus())
					throw new IOException("Cancel Save");
				if (encrypt) {
					cmEncryptor.encrypt(buffer, 0, bytesRead);
					fos.write(buffer, 0, bytesRead);
					writtenBytes += bytesRead;
				} else {
					if (firstRun) {
						// if id3 was found continue the parsing
						firstRun = false;
						if ((buffer[0] == 73 && buffer[1] == 68 && buffer[2] == 51)) {
							id3Found = true;
						} else {
							// If no Id3 was found Create it and prepend it to
							// file.
							if (fos != null) {
								byte[] chunk = MusicUtils.createId3(track);
								fos.write(chunk);
								inserted = true;
							}
						}
					}

					if (id3Found) {
						if (!inserted) {
							// Havn't inserted blob buffer so we can scan.
							os.write(buffer, 0, bytesRead);

							if (os.size() >= 10 && !headerWritten) {
								// Enough data has been buffered we can rewrite
								// the tagsize
								ByteArrayInputStream head = new ByteArrayInputStream(
										os.toByteArray());
								byte[] data = os.toByteArray();
								byte[] headerbuf = new byte[10];
								head.read(headerbuf);
								// Read TagSize
								int tagsize = (headerbuf[9] & 0xFF)
										| ((headerbuf[8] & 0xFF) << 7)
										| ((headerbuf[7] & 0xFF) << 14)
										| ((headerbuf[6] & 0xFF) << 21) + 10;
								tagsize += blob.length;
								// Create byte representation of tagsize + blob
								// size
								byte[] bytes = ByteBuffer.allocate(4)
										.putInt(MusicUtils.synchsafe(tagsize))
										.array();

								// Write to file.
								fos.write(headerbuf, 0, 6);
								writtenBytes += 6;
								fos.write(bytes);
								writtenBytes += bytes.length;
								// parse data for offset
								// If offset is in chunk insert blob mid chunk
								offset = MusicUtils.parse(os, TAG);
								if (offset != -1) {
									fos.write(data, 10, offset - 10);
									writtenBytes += offset - 10;
									fos.write(blob);
									writtenBytes += blob.length;
									fos.write(data, offset, data.length
											- offset);
									writtenBytes += data.length - offset;
									inserted = true;
									os.close();
								} else {
									// Offset not found keep scanning.
									fos.write(data, 10, data.length - 10);
									writtenBytes += data.length - 10;
								}
								head.close();
								headerWritten = true;
							} else if (headerWritten) {
								// Header written parse for offset
								offset = MusicUtils.parse(os, TAG);
								if (offset != -1) {
									if (writtenBytes > offset)
										offset = writtenBytes;
									int upToOffset = ((writtenBytes + bytesRead) - offset);
									upToOffset = bytesRead - upToOffset;
									fos.write(buffer, 0, upToOffset);
									writtenBytes += upToOffset;
									fos.write(blob);
									writtenBytes += blob.length;
									fos.write(buffer, upToOffset, bytesRead
											- upToOffset);
									writtenBytes += bytesRead - upToOffset;
									inserted = true;
									os.close();
								} else {
									// Offset not found keep scanning.
									fos.write(buffer, 0, bytesRead);
									writtenBytes += bytesRead;
								}
							}
						} else {
							// Offset not found keep scanning.
							fos.write(buffer, 0, bytesRead);
							writtenBytes += bytesRead;
						}
					} else {
						fos.write(buffer, 0, bytesRead);
						writtenBytes += bytesRead;
					}
				}

				if (totalBytes > 0) {
					int progress = (int) (writtenBytes * 100. / totalBytes);
					// System.out.println("Download progress :::: " + progress);
					long curTimestamp = new Date().getTime();
					// System.out.println(lastProgressTimestamp +
					// " :::: onUpdateCachingTrack :::: " + curTimestamp +
					// " ::: " + (lastProgressTimestamp - curTimestamp));
					if (lastProgressTimestamp < 0
							|| Math.abs(lastProgressTimestamp - curTimestamp) >= CacheManager.UPDATE_FREQUENCY_DELAY_ON_DOWNLOAD_MS) {
						lastProgressTimestamp = curTimestamp;
						if (saveListener != null) {
							saveListener.onUpdateSaveListener(track, progress);
						}
					}
				}
			}
			if (encrypt)
				Logger.i(TAG, "END ENCRYPT " + track.getTitle());
			else
				Logger.i(TAG, "END DOWNLOAD " + track.getTitle());

			fos.close();
			is.close();

			if (writtenBytes < totalBytes) {
				throw new IOException("Track " + track.getTitle() + " "
						+ track.getId() + " is not completely downloaded");
			} else {
				/*
				 * result = true;
				 * 
				 * track.setCached(true,encrypt);
				 * mDatabaseManager.update(track);
				 * 
				 * Logger.i(TAG, "setCached(true) on Track: " + " "+
				 * track.getName() + " ID: " + track.getId()[0]);
				 * 
				 * return result;
				 */

				File fileFinal = new File(cachingTracksFolder, baseFileName);
				if (fileFinal.exists() && !fileFinal.delete()) {
					Logger.i(TAG, "Failed deleting before rename operation: "
							+ track.getTitle() + " ID: " + track.getId());
					outputFile.delete();
					return false;
				}
				if (outputFile.renameTo(fileFinal)) {
					track.setCached(true, encrypt);
					// mDatabaseManager.update(track);
					// DataBase db = new
					// DataBase(HungamaApplication.getContext());
					// db.open();
					// Cursor cursor = db.fetch(DataBase.Track_Cache_table,
					// DataBase.Track_Cache_int,
					// DataBase.tables[DataBase.Track_Cache_int][1] + "=" +
					// track.getId());
					// if(cursor!=null){
					// if(cursor.moveToFirst()){
					// ContentValues cv = new ContentValues();
					// cv.put(DataBase.tables[DataBase.Track_Cache_int][2],
					// fileFinal.getAbsolutePath());
					// cv.put(DataBase.tables[DataBase.Track_Cache_int][4],
					// DataBase.CacheState.CACHED.toString());
					// db.update(DataBase.Track_Cache_table,
					// DataBase.Track_Cache_int,
					// DataBase.tables[DataBase.Track_Cache_int][1] + "=" +
					// track.getId(), cv);
					// } else{
					// Gson gson = new Gson();
					// String json = gson.toJson(track);
					// db.insert(DataBase.Track_Cache_table,
					// DataBase.Track_Cache_int,
					// new String[]{""+track.getId(),
					// fileFinal.getAbsolutePath(), json,
					// DataBase.CacheState.CACHED.toString()});
					// }
					// cursor.close();
					// } else{
					// Gson gson = new Gson();
					// String json = gson.toJson(track);
					// db.insert(DataBase.Track_Cache_table,
					// DataBase.Track_Cache_int,
					// new String[]{""+track.getId(),
					// fileFinal.getAbsolutePath(), json,
					// DataBase.CacheState.CACHED.toString()});
					// }
					// db.close();
					// Gson gson = new Gson();
					// String json = gson.toJson(track);
					DBOHandler.updateTrack(getApplicationContext(),
							"" + track.getId(), fileFinal.getAbsolutePath(),
							null, DataBase.CacheState.CACHED.toString());
					Logger.i(TAG,
							"setCached(true) on Track: " + track.getTitle()
									+ " ID: " + track.getId());
					// if(CacheManager.isProUser(getApplicationContext()))
					// Toast.makeText(getApplicationContext(), track.getTitle()
					// + " saved offline successfully!",
					// Toast.LENGTH_SHORT).show();

					// try{
					// // new SingleMediaScanner(this, outputFile);
					// MediaScannerConnection.scanFile(getApplicationContext(),
					// new String[]{fileFinal.getAbsolutePath()}, null,
					// new MediaScannerConnection.OnScanCompletedListener() {
					// @Override
					// public void onScanCompleted(String path, Uri uri) {
					// System.out.println("Scanning complete:::::::::::::::" +
					// path);
					// }
					// });
					// } catch(Exception e){
					// System.out.println("Scanning exception::::::::::::::: " +
					// e);
					// Logger.printStackTrace(e);
					// }
					return true;
				} else {
					Logger.i(TAG, "Failed renaming: " + track.getTitle()
							+ " ID: " + track.getId());
					return false;
				}
			}
		} catch (Exception e) {
			Logger.i(TAG, "IOException: " + e.getMessage());
			e.printStackTrace();

			if (httpConnection != null) {
				httpConnection.disconnect();
			}

			if (outputFile != null) {
				outputFile.delete();
			}

			try {
				if (fos != null) {
					fos.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException ex) {
				Logger.i(TAG, "IOException: " + ex.getMessage());
			}
		}
		return result;
	}

	/**
	 * Downloads lyrics.
	 * 
	 * @param track
	 */
	// @SuppressWarnings({ "rawtypes", "unchecked" })
	// private void handleTrackLyrics(Track track){
	//
	// Locale local = Locale.getDefault();
	// String lyricsFileName =
	// DatabaseManager.LYRICS_FILE_NAME+"_"+track.getId()[0]+"_"+local.getLanguage();
	// String cachingTracksFolder = CacheManager.getCacheTracksFolderPath(true);
	//
	// mDatabaseManager = GlobalApplicationData.getDatabaseManager();
	// List<String> storedlyricsList =
	// mDatabaseManager.loadLyricss(cachingTracksFolder, lyricsFileName);
	// String mediaId = String.valueOf(track.getPrimaryId());
	// if (!DatabaseManager.lryicsFailMap.containsKey(mediaId))
	// if(storedlyricsList == null || storedlyricsList.size() == 0){
	//
	// Map result = (Map) mDatabaseManager.lyricsWSRequest(mediaId, "track",
	// local.getLanguage(), "0");
	//
	// if(result != null){
	//
	// String code = result.get(CommunicationManager.CODE).toString();
	//
	// if(code.equalsIgnoreCase(CommunicationManager.SUCCESS_CODE)){
	//
	// try{
	// Map data = (Map) result.get("data");
	//
	// if(data != null && !data.isEmpty()){
	// ArrayList<Map> lyricsMaps = (ArrayList<Map>) data.get("lyrics");
	// if(lyricsMaps != null){
	// mDatabaseManager.storeLyrics(getListLyrics(lyricsMaps),
	// cachingTracksFolder, lyricsFileName);
	// }
	// }else
	// DatabaseManager.lryicsFailMap.put(mediaId, true);
	// }catch (ClassCastException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// }
	// }

	// @SuppressWarnings("rawtypes")
	// public List<String> getListLyrics(List<Map> listMapLyrics){
	//
	// List<String> list = new ArrayList<String>();
	//
	// for(Map mapItm : listMapLyrics){
	//
	// String line = (String) mapItm.get("line");
	// list.add(line);
	// }
	//
	// return list;
	// }

	/**
	 * Interface for listening to {@link Track} caching process.
	 */
	public interface OnSyncTrackListener {
		public void onStartCachingTrack(Track track);

		public void onFinishCachingTrack(Track track);

		public void onUpdateCachingTrack(Track track, int percent);

		public void onFailCachingTrack(Track track, boolean update);

		public void onStartCachingTrack(MediaItem mediaItem);

		public void onFinishCachingTrack(MediaItem mediaItem);

		public void onUpdateCachingTrack(MediaItem mediaItem, int percent);

		public void onFailCachingTrack(MediaItem mediaItem, boolean update);
	}

	private static OnSyncTrackListener mListener;

	public static void setOnSyncTrackListener(OnSyncTrackListener listener) {
		mListener = listener;
	}

	/**
	 * Callback Interface
	 * 
	 * @author daniel
	 *
	 */
	public interface OnSaveTrackListener {
		public void onStartSaveListtener(Track track);

		public void onFinishSaveListener(Track track);

		public void onFailSaveListener(Track track);

		public void onUpdateSaveListener(Track track, int percent);

		public void onCachedSaveListener(Track track, boolean updateGUI);

		public void onStartSaveListtener(MediaItem mediaItem);

		public void onFinishSaveListener(MediaItem mediaItem);

		public void onFailSaveListener(MediaItem mediaItem);

		public void onUpdateSaveListener(MediaItem mediaItem, int percent);

		public void onCachedSaveListener(MediaItem mediaItem, boolean updateGUI);
	}

	public static OnSaveTrackListener saveListener;

	public static void setOnSaveTrackListener(OnSaveTrackListener l) {
		saveListener = l;
	}

	private boolean handleVideoTrackDownload(Video video, MediaItem mediaItem) {
		long trackId = mediaItem.getId();
		boolean success = false;
		DBOHandler.updateVideoTrackCacheState(getApplicationContext(), ""
				+ trackId, DataBase.CacheState.CACHING.toString());

		mCurrentVideo = video;
		mCurrentMediaItem = mediaItem;
		Logger.i(TAG, "Downloading Track - " + mediaItem.getTitle());

		if (mCacheManager.getCancelSaveStatus())
			return success;

		mCurrentTrack = null;
		if (!mCurrentVideo.isDoNotCache()) {
			if (CacheManager.getCacheVideoTracksFolderPath(true) != null) {
				int cachingAttemps = 1;

				if (mCacheManager.getVideoTrack(trackId) != null) {// &&
																	// mCurrentVideo.getTrackState()
																	// !=
																	// Track.STREAMABLE_STATE){
					success = true;
				} else {
					started = true;
					if (saveListener != null)
						saveListener.onStartSaveListtener(mCurrentMediaItem);

					boolean checkApp = true; // mDatabaseManager.getAppType().equalsIgnoreCase(GlobalApplicationData.INSENSE)
					// ||
					// mDatabaseManager.getAppType().equalsIgnoreCase(GlobalApplicationData.ALBUM);
					while (!(success = downloadVideoTrackToCache(video,
							mediaItem)) && checkApp) {
						if (!DBOHandler.isVideoTrackExist(getBaseContext(), ""
								+ mediaItem.getId())) {
							break;
						}
						cachingAttemps++;
						try {
							Thread.sleep(30000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Logger.i(TAG, "downloadTrackToCache( "
								+ mCurrentMediaItem.getTitle() + " "
								+ mCurrentMediaItem.getId() + " )"
								+ " Attempt # " + cachingAttemps);
						if (cachingAttemps > 1) {
							break;
						}
					}
				}
				if (success) {
					started = false;
					if (mListener != null)
						mListener.onFinishCachingTrack(mCurrentMediaItem);
				} else {
					started = false;
					if (mListener != null)
						mListener.onFailCachingTrack(mCurrentMediaItem, false);

					if (saveListener != null)
						saveListener.onFailSaveListener(mCurrentMediaItem);
					else
						mCacheManager.removeCachingVideoTrack(mediaItem);
				}
			}
		}
		// }
		return success;
	}

	private boolean downloadVideoTrackToCache(Video video, MediaItem mediaItem) {
		boolean result = false;
		String downloadUrl = video.getVideoUrl();
		// byte[] blob = new byte[0];// track.getOwnerBlob();
		boolean encrypt = true;// track.isEncrypted();
		String deviceID = "630358525";// DataManager.getInstance(getApplicationContext()).getApplicationConfigurations().getDeviceID();
		CMEncryptor cmEncryptor = new CMEncryptor(deviceID);
		Logger.s("---------------------  " + deviceID);

		// Logger.i(TAG, "downloadUrl for caching: " + downloadUrl);
		File outputFile = null;
		FileOutputStream fos = null;
		InputStream is = null;

		if (mCacheManager.getCancelSaveStatus())
			return result;

		if (downloadUrl == null) {
			return result;
		}

		HttpURLConnection httpConnection = null;
		try {
			int totalBytes = 0;
			httpConnection = Utils.httpHandler(downloadUrl, this);

			int status = httpConnection.getResponseCode();
			if (status != HttpURLConnection.HTTP_OK) {
				throw new IOException("HTTP STATUS ERROR - " + status);
			}

			is = httpConnection.getInputStream();
			totalBytes = httpConnection.getContentLength();
			if (totalBytes <= 0)
				throw new IOException("Content length <= 0");

			long size = 0;
			if (CacheManager.isProUser(this)) {
				File cacheFolder;// = new
									// File(CacheManager.getCacheTracksFolderPath(encrypt));
				if (MemoryInfoHelper.externalMemoryAvailable()) {
					cacheFolder = getExternalCacheDir().getAbsoluteFile();
					size = MemoryInfoHelper.getTotalExternalMemorySizeLong();
				} else {
					cacheFolder = getCacheDir().getAbsoluteFile();
					size = MemoryInfoHelper.getTotalInternalMemorySizeLong();
				}
				if (cacheFolder.exists()) {
					long result1 = Utils.getFolderSize(cacheFolder);
					size -= CacheManager.SAFETY_BUFFER;
					ApplicationConfigurations appConfig = ApplicationConfigurations
							.getInstance(this);
					size = size
							* appConfig
									.getSaveOfflineMemoryAllocatedPercentage()
							/ 100;
					if ((result1 + totalBytes) >= size) {
						handler.post(runnableCacheFullError);
						return false;
					}
				}
			}

			size = 0;
			if (MemoryInfoHelper.externalMemoryAvailable()) {
				size = MemoryInfoHelper.getAvailableExternalMemorySizeLong();
			} else {
				size = MemoryInfoHelper.getAvailableInternalMemorySizeLong();
			}
			if ((size - totalBytes) <= CacheManager.SAFETY_BUFFER) {
				handler.post(runnableMemoryFullError);
				return false;
			}

			String cachingTracksFolder = CacheManager
					.getCacheVideoTracksFolderPath(encrypt);
			String baseFileName = "video_" + mediaItem.getId()
					+ CacheManager.TRACK_FORMAT;
			String tmpFileName = baseFileName + CacheManager.TRACK_PART_FORMAT;

			File trackFolder = new File(cachingTracksFolder);
			if (!trackFolder.exists())
				trackFolder.mkdirs();

			outputFile = new File(cachingTracksFolder, tmpFileName);
			if (!outputFile.exists())
				outputFile.createNewFile();
			fos = new FileOutputStream(outputFile);
			Logger.s("outputFile ::: " + outputFile.getAbsolutePath());
			byte[] buffer;
			try {
				buffer = new byte[1024 * 100];
			} catch (Error e) {
				System.gc();
				System.runFinalization();
				System.gc();
				buffer = new byte[1024 * 50];
			}
			// boolean firstRun = true;
			// boolean id3Found = false;
			// boolean inserted = false;
			// boolean headerWritten = false;
			// ByteArrayOutputStream os = new ByteArrayOutputStream();
			// int offset = -1;
			int bytesRead = 0;
			// int deviceId = Integer.parseInt(deviceID);
			// int _k = ((((int) deviceId) >> 24) ^ (((int) deviceId) >> 16)
			// ^ (((int) deviceId) >> 8) ^ ((int) deviceId)) & 255;
			if (encrypt)
				Logger.i(TAG, "START ENCRYPT " + mediaItem.getTitle());
			else
				Logger.i(TAG, "START DOWNLOAD " + mediaItem.getTitle());
			// int totalBytesRead = 0;
			long lastProgressTimestamp = -1;
			int writtenBytes = 0;
			// int totalBytes = connection.getContentLength();

			while ((bytesRead = is.read(buffer)) != -1) {
				if (!DBOHandler.isVideoTrackExist(getBaseContext(), ""
						+ mediaItem.getId())) {
					break;
				}
				if (mCacheManager.getCancelSaveStatus())
					throw new IOException("Cancel Save");
				if (encrypt) {
					cmEncryptor.encrypt(buffer, 0, bytesRead);
					fos.write(buffer, 0, bytesRead);
					writtenBytes += bytesRead;
				} else {
					// if (firstRun){
					// //if id3 was found continue the parsing
					// firstRun = false;
					// if ((buffer[0] == 73 && buffer[1] == 68 && buffer[2] ==
					// 51)){
					// id3Found = true;
					// }else{
					// // If no Id3 was found Create it and prepend it to file.
					// if (fos != null){
					// byte[] chunk = MusicUtils.createId3(track);
					// fos.write(chunk);
					// inserted = true;
					// }
					// }
					// }
					//
					// if (id3Found){
					// if (!inserted){
					// // Havn't inserted blob buffer so we can scan.
					// os.write(buffer,0,bytesRead);
					//
					// if (os.size() >= 10 && !headerWritten){
					// // Enough data has been buffered we can rewrite the
					// tagsize
					// ByteArrayInputStream head = new
					// ByteArrayInputStream(os.toByteArray());
					// byte[] data = os.toByteArray();
					// byte[] headerbuf = new byte[10];
					// head.read(headerbuf);
					// // Read TagSize
					// int tagsize = (headerbuf[9] & 0xFF) | ((headerbuf[8] &
					// 0xFF) << 7 ) | ((headerbuf[7] & 0xFF) << 14 ) |
					// ((headerbuf[6] & 0xFF) << 21 ) + 10;
					// tagsize += blob.length;
					// // Create byte representation of tagsize + blob size
					// byte[] bytes =
					// ByteBuffer.allocate(4).putInt(MusicUtils.synchsafe(tagsize)).array();
					//
					// // Write to file.
					// fos.write(headerbuf, 0, 6);
					// writtenBytes += 6;
					// fos.write(bytes);
					// writtenBytes += bytes.length;
					// // parse data for offset
					// // If offset is in chunk insert blob mid chunk
					// offset = MusicUtils.parse(os,TAG);
					// if (offset != -1){
					// fos.write(data,10,offset-10);
					// writtenBytes += offset-10;
					// fos.write(blob);
					// writtenBytes += blob.length;
					// fos.write(data, offset, data.length-offset);
					// writtenBytes += data.length - offset;
					// inserted = true;
					// os.close();
					// }else{
					// // Offset not found keep scanning.
					// fos.write(data, 10, data.length - 10);
					// writtenBytes += data.length - 10;
					// }
					// head.close();
					// headerWritten = true;
					// }else if (headerWritten){
					// // Header written parse for offset
					// offset = MusicUtils.parse(os, TAG);
					// if (offset != -1){
					// if (writtenBytes > offset)
					// offset = writtenBytes;
					// int upToOffset = ((writtenBytes + bytesRead) - offset);
					// upToOffset = bytesRead - upToOffset;
					// fos.write(buffer, 0, upToOffset);
					// writtenBytes += upToOffset;
					// fos.write(blob);
					// writtenBytes += blob.length;
					// fos.write(buffer, upToOffset, bytesRead - upToOffset);
					// writtenBytes += bytesRead - upToOffset;
					// inserted = true;
					// os.close();
					// }else{
					// // Offset not found keep scanning.
					// fos.write(buffer, 0, bytesRead);
					// writtenBytes += bytesRead;
					// }
					// }
					// }else{
					// // Offset not found keep scanning.
					// fos.write(buffer, 0, bytesRead);
					// writtenBytes += bytesRead;
					// }
					// }else{
					fos.write(buffer, 0, bytesRead);
					writtenBytes += bytesRead;
					// }
				}

				if (totalBytes > 0) {
					int progress = (int) (writtenBytes * 100. / totalBytes);
					// System.out.println("Download progress :::: " + progress);
					long curTimestamp = new Date().getTime();
					// System.out.println(lastProgressTimestamp +
					// " :::: onUpdateCachingTrack :::: " + curTimestamp +
					// " ::: " + (lastProgressTimestamp - curTimestamp));
					if (lastProgressTimestamp < 0
							|| Math.abs(lastProgressTimestamp - curTimestamp) >= CacheManager.UPDATE_FREQUENCY_DELAY_ON_DOWNLOAD_MS) {
						lastProgressTimestamp = curTimestamp;
						if (saveListener != null) {
							saveListener.onUpdateSaveListener(mediaItem,
									progress);
						}
					}
				}
			}
			if (encrypt)
				Logger.i(TAG, "END ENCRYPT " + mediaItem.getTitle());
			else
				Logger.i(TAG, "END DOWNLOAD " + mediaItem.getTitle());

			fos.close();
			is.close();

			if (writtenBytes < totalBytes) {
				throw new IOException("Track " + mediaItem.getTitle() + " "
						+ mediaItem.getId() + " is not completely downloaded");
			} else {
				File fileFinal = new File(cachingTracksFolder, baseFileName);
				if (fileFinal.exists() && !fileFinal.delete()) {
					Logger.i(TAG,
							"Failed deleting before rename operation: "
									+ mediaItem.getTitle() + " ID: "
									+ mediaItem.getId());
					outputFile.delete();
					return false;
				}
				if (outputFile.renameTo(fileFinal)) {
					DBOHandler.updateVideoTrack(getApplicationContext(), ""
							+ mediaItem.getId(), fileFinal.getAbsolutePath(),
							null, DataBase.CacheState.CACHED.toString());
					Logger.i(TAG,
							"setCached(true) on Track: " + mediaItem.getTitle()
									+ " ID: " + mediaItem.getId());
					// if(CacheManager.isProUser(getApplicationContext()))
					// Toast.makeText(getApplicationContext(),
					// mediaItem.getTitle() + " saved offline successfully!",
					// Toast.LENGTH_SHORT).show();
					return true;
				} else {
					Logger.i(TAG, "Failed renaming: " + mediaItem.getTitle()
							+ " ID: " + mediaItem.getId());
					return false;
				}
			}
		} catch (Exception e) {
			Logger.i(TAG, "IOException: " + e.getMessage());
			e.printStackTrace();

			if (httpConnection != null) {
				httpConnection.disconnect();
			}

			if (outputFile != null) {
				outputFile.delete();
			}

			try {
				if (fos != null) {
					fos.close();
				}
				if (is != null) {
					is.close();
				}
			} catch (IOException ex) {
				Logger.i(TAG, "IOException: " + ex.getMessage());
			}
		}
		return result;
	}
}
