package com.hungama.myplay.activity.data.audiocaching;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.Video;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.VideoStreamingOperationAdp;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * @author XTPL
 * 
 */
public class MediaCachingTaskNew implements Runnable {
	public static final boolean isEnabled = true;
	private Track track;
	private MediaItem mediaItem;
	private Context mContext;
	private ApplicationConfigurations mApplicationConfigurations;
	private static final String TAG = "MediaCachingTask";
	MediaSetDetails mMediaSetDetails;
	private boolean isVideoFileExist = false;
	private boolean isVideoFileCached = false;
	private boolean isAutoSave = false;

	public MediaCachingTaskNew(Context mContext, MediaItem mediaItem,
			Track track) {
		this.mContext = mContext;
		this.track = track;
		this.mediaItem = mediaItem;
		mApplicationConfigurations = DataManager.getInstance(mContext)
				.getApplicationConfigurations();
		// if(mediaItem!=null){
		// System.out.println("mediaItem getMediaContentType :::::::::::::: " +
		// mediaItem.getMediaContentType());
		// System.out.println("mediaItem getMediaType :::::::::::::: " +
		// mediaItem.getMediaType());
		// }
	}

	public MediaCachingTaskNew(Context mContext, MediaItem mediaItem,
			Track track, boolean isAutoSave) {
		this.mContext = mContext;
		this.track = track;
		this.mediaItem = mediaItem;
		this.isAutoSave = isAutoSave;
		mApplicationConfigurations = DataManager.getInstance(mContext)
				.getApplicationConfigurations();
		// if(mediaItem!=null){
		// System.out.println("mediaItem getMediaContentType :::::::::::::: " +
		// mediaItem.getMediaContentType());
		// System.out.println("mediaItem getMediaType :::::::::::::: " +
		// mediaItem.getMediaType());
		// }
	}

	public void run() {
		Thread.currentThread().setPriority(3);
		boolean result = doInBackground();
		onPostExecute(result);
	}

	protected Boolean doInBackground(Void... voids) {
		Logger.s("currentThread :::: " + Thread.currentThread());
		if (!isAutoSave
				&& (CacheManager.isProUser(mContext)
						&& !mApplicationConfigurations
								.getSaveOfflineOnCellularNetwork() && !Utils
						.getNetworkType(mContext).equalsIgnoreCase(
								Utils.NETWORK_WIFI)) || !Utils.isConnected()) {
			return false;
		} else if (!isAutoSave && CacheManager.isCacheFull(mContext)) {
			return false;
		} else if (CacheManager.isMemoryFull()) {
			return false;
		}

		try {
			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC
					&& (mediaItem.getMediaType() == MediaType.ALBUM || mediaItem
							.getMediaType() == MediaType.PLAYLIST)) {
				String album_id = "0", playlist_id = "0";
				if (mediaItem.getMediaType() == MediaType.ALBUM) {
					DBOHandler.insertAlbumToCache(mContext,
							"" + mediaItem.getId(), "");
					album_id = "" + mediaItem.getId();
				} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
					DBOHandler.insertPlaylistToCache(mContext,
							"" + mediaItem.getId(), "");
					playlist_id = "" + mediaItem.getId();
				}

				DataManager mDataManager = DataManager.getInstance(mContext);
				CommunicationManager communicationManager = new CommunicationManager();
				Map<String, Object> mediaHandleProperties = null;

				ServerConfigurations mServerConfigurations = mDataManager
						.getServerConfigurations();
				String mCMServerUrl = mServerConfigurations
						.getHungamaServerUrl_2();

				String images = ImagesManager.getImageSize(
						ImagesManager.MUSIC_ART_SMALL,
						DataManager.getInstance(mContext).getDisplayDensity())
						+ ","
						+ ImagesManager.getImageSize(
								ImagesManager.MUSIC_ART_BIG, DataManager
										.getInstance(mContext)
										.getDisplayDensity());
				MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
						mCMServerUrl,
						mServerConfigurations.getHungamaAuthKey(),
						mApplicationConfigurations.getPartnerUserId(),
						mediaItem, null, images);
				Response response = communicationManager.performOperationNew(
						mediaDetailsOperation, mContext);
				if (response.response != null && response.response.length() > 0) {
					if (mediaItem.getMediaType() == MediaType.ALBUM) {
						DBOHandler.updateAlbumCache(mContext, album_id,
								response.response,
								DataBase.CacheState.CACHING.toString());
						mContext.sendBroadcast(new Intent(
								CacheManager.ACTION_CACHE_STATE_UPDATED));
					} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
						DBOHandler.updatePlaylistCache(mContext, playlist_id,
								response.response,
								DataBase.CacheState.CACHING.toString());
						mContext.sendBroadcast(new Intent(
								CacheManager.ACTION_CACHE_STATE_UPDATED));
					}

					mediaHandleProperties = mediaDetailsOperation
							.parseResponse(response);
					mMediaSetDetails = (MediaSetDetails) mediaHandleProperties
							.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
				}
			} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				String path = DBOHandler.getVideoTrackPathById(mContext, ""
						+ mediaItem.getId());
				if (path != null) {
					if (path.length() > 0)
						isVideoFileExist = true;
					else if (path.length() == 0)
						isVideoFileCached = true;
				}

				if (isVideoFileExist) {

				} else {
					if (!isVideoFileCached) {
						DBOHandler.insertVideoTrackToCache(mContext, ""
								+ mediaItem.getId(), "", "",
								DataBase.CacheState.QUEUED.toString());
					}
					CommunicationManager communicationManager = new CommunicationManager();
					Map<String, Object> mediaHandleProperties = null;
					try {
						DataManager mDataManager = DataManager
								.getInstance(mContext);
						ServerConfigurations mServerConfigurations = mDataManager
								.getServerConfigurations();
						if (!isVideoFileCached) {
							String images = ImagesManager.getImageSize(
									ImagesManager.MUSIC_ART_SMALL, DataManager
											.getInstance(mContext)
											.getDisplayDensity())
									+ ","
									+ ImagesManager.getImageSize(
											ImagesManager.HOME_VIDEO_TILE,
											DataManager.getInstance(mContext)
													.getDisplayDensity());
							Response response = communicationManager
									.performOperationNew(
											new MediaDetailsOperation(
													mServerConfigurations
															.getHungamaServerUrl_2(),
													mServerConfigurations
															.getHungamaAuthKey(),
													mApplicationConfigurations
															.getPartnerUserId(),
													mediaItem, null, images),
											mContext);
							if (response.response != null/* && !isFileCached */) {
								DBOHandler.updateVideoTrack(mContext, ""
										+ mediaItem.getId(), "",
										response.response,
										DataBase.CacheState.QUEUED.toString());
								mContext.sendBroadcast(new Intent(
										CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED));
							}
						}
						String accountType = Utils.getAccountName(mContext);
						String networkType = Utils.getNetworkType(mContext);
						String contentFormat = mApplicationConfigurations
								.getContentFormat();
						if (TextUtils.isEmpty(contentFormat))
							contentFormat = "high";
						int networkSpeed = getNetworkBandwidth(mDataManager,
								networkType);
						VideoStreamingOperationAdp videoStreamingOperationAdp = new VideoStreamingOperationAdp(
								mServerConfigurations.getHungamaServerUrl_2(),
								mApplicationConfigurations.getPartnerUserId(),
								String.valueOf(mediaItem.getId()),
								mDataManager.getDisplayDensity(),
								mServerConfigurations.getHungamaAuthKey(),
								networkSpeed, networkType, contentFormat,
								accountType, true);
						Response responseAdp = communicationManager
								.performOperationNew(
										videoStreamingOperationAdp, mContext);
						if (responseAdp.response != null
								&& responseAdp.response.length() > 0) {
							mediaHandleProperties = videoStreamingOperationAdp
									.parseResponse(responseAdp);
							if (mediaHandleProperties != null) {
								Video video = (Video) mediaHandleProperties
										.get(VideoStreamingOperationAdp.RESPONSE_KEY_VIDEO_STREAMING_ADP);
								if (video != null) {
									Logger.e(TAG, "uri for media item: "
											+ video.getVideoUrl());
									HungamaApplication.getCacheManager()
											.cacheVideoTrackFromList(mContext,
													video, mediaItem);
								}
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
			} else if (track != null) {
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	protected void onPostExecute(Boolean result) {
		if (result) {
			String album_id = "0", playlist_id = "0";

			if (mediaItem.getMediaContentType() == MediaContentType.MUSIC
					&& (mediaItem.getMediaType() == MediaType.ALBUM || mediaItem
							.getMediaType() == MediaType.PLAYLIST)) {
				if (mMediaSetDetails != null) {
					if (mMediaSetDetails.getTracks() != null
							&& mMediaSetDetails.getTracks().size() > 0) {
						if (mediaItem.getMediaType() == MediaType.ALBUM) {
							album_id = "" + mediaItem.getId();
						} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
							playlist_id = "" + mediaItem.getId();
						}
						if (CacheManager.isProUser(mContext)) {
							for (Track trackToCache : mMediaSetDetails
									.getTracks()) {
								DBOHandler.insertTrackToTrackListTable(
										mContext, "" + trackToCache.getId(),
										album_id, playlist_id);
								ThreadPoolManager.getInstance().submit(new MediaHandleLoaderTask(trackToCache,
										album_id, playlist_id));
							}
						} else {
							int tracksCached = DBOHandler
									.getAllTracks(mContext).size();
							if (tracksCached < CacheManager
									.getFreeUserCacheLimit(mContext)) {
								if (CacheManager.isMemoryFull()) {
									Toast.makeText(
											mContext,
											Utils.getMultilanguageText(
													mContext,
													mContext.getResources()
															.getString(
																	R.string.save_offline_error_memory_full)),
											Toast.LENGTH_SHORT).show();
								} else {
									for (Track trackToCache : mMediaSetDetails
											.getTracks()) {
										if (tracksCached < CacheManager
												.getFreeUserCacheLimit(mContext)) {
											if (DBOHandler.getTrackCacheState(
													mContext,
													"" + trackToCache.getId()) == CacheState.NOT_CACHED) {
												tracksCached++;
												DBOHandler
														.insertTrackToTrackListTable(
																mContext,
																""
																		+ trackToCache
																				.getId(),
																album_id,
																playlist_id);
												ThreadPoolManager.getInstance().submit(new MediaHandleLoaderTask(
														trackToCache, album_id,
														playlist_id));
											}
										} else {
											break;
										}
									}
								}
							} else {
							}
						}
					}
				}
			} else if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				if (isVideoFileExist) {
					showNotification(mediaItem.getTitle()
							+ " already saved offline.");
				}
			} else if (track != null) {
				if (mediaItem.tag != null) {

					try {
						Logger.e("playlist_id", "111111111111 >1 ");
						MediaItem item = (MediaItem) mediaItem.tag;
						if (item.getMediaType() == MediaType.ALBUM) {
							album_id = "" + item.getId();
						} else if (item.getMediaType() == MediaType.PLAYLIST) {
							playlist_id = "" + item.getId();
							Logger.e("playlist_id", "111111111111 >2 ");

						}
					} catch (Exception e) {
						Logger.e("playlist_id", "111111111111 >3 ");
					}
				} else
					Logger.e("playlist_id", "111111111111 >4 ");
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
				} else
					Logger.e("playlist_id", "222222222222 > 4");
				// loadMediaHandle(track, "0", "0");
				ThreadPoolManager.getInstance().submit(new MediaHandleLoaderTask(track, album_id, playlist_id));
			}
		} else {
			if ((CacheManager.isProUser(mContext)
					&& !mApplicationConfigurations
							.getSaveOfflineOnCellularNetwork() && !Utils
					.getNetworkType(mContext).equalsIgnoreCase(
							Utils.NETWORK_WIFI))
					|| !Utils.isConnected()) {
				showNotification(mContext.getResources().getString(
						R.string.save_offline_error_network_connectivity));
				return;
			} else if (CacheManager.isCacheFull(mContext)) {
				showNotification(Utils.getMultilanguageText(
						mContext,
						mContext.getResources().getString(
								R.string.save_offline_error_cache_full)));
				return;
			} else if (CacheManager.isMemoryFull()) {
				showNotification(mContext.getResources().getString(
						R.string.save_offline_error_memory_full));
				return;
			}
			if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
				DBOHandler.updateVideoTrackCacheState(mContext,
						"" + mediaItem.getId(),
						CacheState.NOT_CACHED.toString());
				mContext.sendBroadcast(new Intent(
						CacheManager.ACTION_CACHE_STATE_UPDATED));
			}
		}
	}

	private class MediaHandleLoaderTask implements Runnable
	{
		private final Track track;
		private String album_id, playlist_id;

		private boolean isFileExist = false;
		private boolean isFileAlreadyCached = false;

		public MediaHandleLoaderTask(final Track track, final String album_id,
				final String playlist_id) {
			this.track = track;

			this.album_id = album_id;
			this.playlist_id = playlist_id;
		}

		public void run() {
			Thread.currentThread().setPriority(3);
			boolean result = doInBackground();
			onPostExecute(result);
		}

		protected Boolean doInBackground(String... params) {
			Logger.s("currentThread 12 :::: " + Thread.currentThread());
			if (!isAutoSave
					&& (CacheManager.isProUser(mContext)
							&& !mApplicationConfigurations
									.getSaveOfflineOnCellularNetwork() && !Utils
							.getNetworkType(mContext).equalsIgnoreCase(
									Utils.NETWORK_WIFI))
					|| !Utils.isConnected()) {
				return false;
			} else if (!isAutoSave && CacheManager.isCacheFull(mContext)) {
				return false;
			} else if (CacheManager.isMemoryFull()) {
				return false;
			}
			if (mContext == null)
				Logger.s("-----------mContext null----------------");
			if (track == null)
				Logger.s("-----------track null----------------");
			String path = DBOHandler.getTrackPathById(mContext,
					"" + track.getId());
			if (path != null) {
				if (path.length() > 0) {
					isFileExist = true;
					if (DBOHandler.getTrackCacheState(mContext,
							"" + track.getId()) == CacheState.NOT_CACHED) {
						isFileExist = false;
						isFileAlreadyCached = true;
					}
				} else if (path.length() == 0) {
					isFileAlreadyCached = true;
					if (DBOHandler.getTrackCacheState(mContext,
							"" + track.getId()) != CacheState.NOT_CACHED) {
						isFileExist = true;
					}
				}
			}

			if (isAutoSave && !CacheManager.isProUser(mContext)) {
				if (!isFileAlreadyCached
						&& DBOHandler.getAllTracks(mContext).size() >= CacheManager
								.getFreeUserCacheLimit(mContext)) {
					return false;
				}
			}
			if (isFileExist) {
				DBOHandler.updateTrackListState(mContext, "" + track.getId(),
						"");
			} else {
				if (!isFileAlreadyCached) {
					String detail = "";
					try {
						JSONObject json = new JSONObject();
						JSONObject jsonResponse = new JSONObject();
						jsonResponse.put("content_id", track.getId());
						jsonResponse.put("title", track.getTitle());
						jsonResponse.put("album_name", track.getAlbumName());
						json.put("response", jsonResponse);
						detail = json.toString();
					} catch (JSONException e) {
						e.printStackTrace();
					}
					DBOHandler.insertTrackToCache(mContext, "" + track.getId(),
							"", detail, DataBase.CacheState.QUEUED.toString(),
							album_id, playlist_id);
					mApplicationConfigurations.increaseFreeUserCacheCount();
				} else {
					DBOHandler.updateTrackCacheState(mContext,
							"" + track.getId(),
							DataBase.CacheState.QUEUED.toString());
				}
				// if (TextUtils.isEmpty(track.getMediaHandle())) {
				// CommunicationManager communicationManager = new
				// CommunicationManager();
				// // Map<String, Object> mediaHandleProperties = null;
				// try {
				// MediaItem mediaItem = new MediaItem(track.getId(),
				// null, null, null, null, null,
				// MediaType.TRACK.toString(), 0);
				// ServerConfigurations mServerConfigurations = DataManager
				// .getInstance(mContext)
				// .getServerConfigurations();
				// String response = communicationManager
				// .performOperationNew(
				// new MediaDetailsOperation(
				// mServerConfigurations
				// .getHungamaServerUrl(),
				// mServerConfigurations
				// .getHungamaAuthKey(),
				// mApplicationConfigurations
				// .getPartnerUserId(),
				// mediaItem, null), mContext);
				// if (response != null/* && !isFileCached */) {
				// try {
				// DBOHandler.updateTrack(mContext,
				// "" + track.getId(), "", response,
				// DataBase.CacheState.QUEUED.toString());
				// mContext.sendBroadcast(new Intent(
				// CacheManager.ACTION_CACHE_STATE_UPDATED));
				//
				// if (album_id.equals("0")) {
				// try {
				// response = response.replace(
				// "{\"catalog\":{\"content\":",
				// "");
				// response = response.substring(0,
				// response.length() - 2);
				// album_id ="" + (new
				// Gson().fromJson(response,MediaTrackDetails.class)
				// .getAlbumId());
				// } catch (Exception e) {
				// }
				// }
				// if (!album_id.equals("0")
				// || !playlist_id.equals("0"))
				// DBOHandler.insertTrackToTrackListTable(
				// mContext, "" + track.getId(), ""
				// + album_id, playlist_id);
				// } catch (Exception e) {
				// Logger.printStackTrace(e);
				// }
				// }
				//
				// } catch (InvalidRequestException e) {
				// e.printStackTrace();
				// return false;
				// } catch (InvalidResponseDataException e) {
				// e.printStackTrace();
				// return false;
				// } catch (OperationCancelledException e) {
				// e.printStackTrace();
				// return false;
				// } catch (NoConnectivityException e) {
				// e.printStackTrace();
				// return false;
				// }
				HungamaApplication.getCacheManager().cacheTrackFromList(
						mContext, track);
			}
			return true;
		}

		protected void onPostExecute(Boolean result) {
			if (result) {
				if (isFileExist) {
					showNotification(track.getTitle()
							+ " already saved offline.");
				}
			} else {
				if ((CacheManager.isProUser(mContext)
						&& !mApplicationConfigurations
								.getSaveOfflineOnCellularNetwork() && !Utils
						.getNetworkType(mContext).equalsIgnoreCase(
								Utils.NETWORK_WIFI))
						|| !Utils.isConnected()) {
					showNotification(mContext.getResources().getString(
							R.string.save_offline_error_network_connectivity));
					return;
				} else if (CacheManager.isCacheFull(mContext)) {
					showNotification(mContext.getResources().getString(
							R.string.save_offline_error_cache_full));
					return;
				} else if (CacheManager.isMemoryFull()) {
					showNotification(mContext.getResources().getString(
							R.string.save_offline_error_memory_full));
					return;
				} else {
					DBOHandler.updateTrackCacheState(mContext,
							"" + track.getId(),
							DataBase.CacheState.NOT_CACHED.toString());
				}
				mContext.sendBroadcast(new Intent(
						CacheManager.ACTION_CACHE_STATE_UPDATED));
			}
		}

		public void execute() {
//			start();
		}
	}

	private int getNetworkBandwidth(DataManager mDataManager, String networkType) {
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		if (!TextUtils.isEmpty(networkType)) {
			long bandwidth = mApplicationConfigurations.getBandwidth();
			if (networkType.equalsIgnoreCase(Utils.NETWORK_WIFI)
					|| networkType.equalsIgnoreCase(Utils.NETWORK_3G)|| networkType.equalsIgnoreCase(Utils.NETWORK_4G)) {
				if (bandwidth == 0) {
					Logger.i(
							TAG,
							networkType
									+ " - First Time - 3G No bandwidth. bandwidth should be 192");
					return 192;
				} else {
					Logger.i(TAG, networkType + " - Bandwidth from previous = "
							+ bandwidth);
					return (int) bandwidth;
				}
			} else if (networkType.equalsIgnoreCase(Utils.NETWORK_2G)) {
				Logger.i(TAG, networkType + " - 2G - bandwidth should be 80");
				return 80;
			}
		}
		Logger.i(TAG, "Not WIFI & Not Mobile - bandwidth = 64");
		return 64; // Not WIFI & Not Mobile - bandwidth = 64
	}

	private void showNotification(String message) {
		// if (CacheManager.isProUser(mContext) && !isAutoSave)
		// Toast.makeText(
		// mContext,
		// mContext.getResources().getString(
		// R.string.save_offline_error_network_connectivity),
		// Toast.LENGTH_SHORT).show();
	}

	public void execute() {
//		start();
	}
}
