package com.hungama.myplay.activity.services;

import android.app.IntentService;
import android.content.Intent;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.ActionDefinition;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.player.PlayingQueue;
import com.hungama.myplay.activity.util.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class ReloadTracksDataService extends IntentService {

	public ReloadTracksDataService() {
		super("ReloadTracksDataService");
	}

	public ReloadTracksDataService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		PlayingQueue mPlayingQueue = DataManager.getInstance(this)
				.getStoredPlayingQueue(
						ApplicationConfigurations.getInstance(this));
		songLanguageChange(mPlayingQueue);
		videoLanguageChange();
	}

	private void songLanguageChange(PlayingQueue mPlayingQueue) {
		List<MediaItem> mediaItems = DBOHandler.getAllTracks(this);
		if (mPlayingQueue != null && mPlayingQueue.size() > 0) {
			for (Track track : mPlayingQueue.getCopy()) {
				if (!DBOHandler.isTrackExist(this, "" + track.getId())) {
					MediaItem mediaItem = new MediaItem(track.getId(),
							track.getTitle(), null, null, null, null,
							MediaType.TRACK.toString(), 0, track.getAlbumId());
					mediaItems.add(mediaItem);
				}
			}
		}
		if(mediaItems!=null)
			for (MediaItem mediaItem : mediaItems) {
				Logger.s("ReloadTracksDataService Download Media Details : "
						+ mediaItem.getTitle());
				CommunicationManager communicationManager = new CommunicationManager();
				// MediaItem mediaItem = new MediaItem(track.getId(), null,
				// null,
				// null, null, null, MediaType.TRACK.toString(), 0,
				// track.getAlbumId());
				ServerConfigurations mServerConfigurations = DataManager
						.getInstance(this).getServerConfigurations();
				String images = ImagesManager.getImageSize(
						ImagesManager.MUSIC_ART_SMALL,
						DataManager.getDisplayDensityLabel())
						+ ","
						+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_BIG,
								DataManager.getDisplayDensityLabel());
				try {
					Response response;
					MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
							mServerConfigurations.getHungamaServerUrl_2(),
							mServerConfigurations.getHungamaAuthKey(),
							ApplicationConfigurations.getInstance(this)
									.getPartnerUserId(), mediaItem, null, images);
					response = communicationManager.performOperationNew(
							mediaDetailsOperation, this);
					Logger.i("response",
							"ReloadTracksDataService Download Media Detail Response:"
									+ response);
					if (response != null) {
						Intent broadcastIntent = new Intent(
								ActionDefinition.ACTION_MEDIA_DETAIL_RELOADED);
						Map<String, Object> temp = mediaDetailsOperation
								.parseResponse(response);
						broadcastIntent
								.putExtra(
										MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS,
										(MediaTrackDetails) temp
												.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS));
						sendBroadcast(broadcastIntent);

						if (DBOHandler.isTrackExist(this, "" + mediaItem.getId())) {
							JSONObject jsonServerResponse;
							try {
								jsonServerResponse = new JSONObject(
										response.response);
								String savedResponse = DBOHandler.getTrackDetails(
										this, "" + mediaItem.getId());
								JSONObject jsonResponse = new JSONObject(
										savedResponse);
								JSONObject jsonCatalog = null;
								if (jsonResponse.has("catalog")) {
									jsonCatalog = jsonResponse.getJSONObject(
											"catalog").getJSONObject("content");
								} else {
									jsonCatalog = jsonResponse
											.getJSONObject("response");
								}
								if (jsonCatalog.has("delivery_id")) {
									JSONObject jsonServerCatalog = jsonServerResponse
											.getJSONObject("response");
									jsonServerCatalog.put("delivery_id",
											jsonCatalog.getLong("delivery_id"));
								}
								DBOHandler.updateTrack(this,
										"" + mediaItem.getId(), null,
										jsonServerResponse.toString(), null);
							} catch (JSONException e) {
								Logger.printStackTrace(e);
							} catch (NullPointerException e) {
								Logger.printStackTrace(e);
							}
						}
					}
				} catch (InvalidRequestException e) {
					e.printStackTrace();
				} catch (InvalidResponseDataException e) {
					e.printStackTrace();
				} catch (OperationCancelledException e) {
					e.printStackTrace();
				} catch (NoConnectivityException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}

	private void videoLanguageChange() {
		List<MediaItem> mediaItems = DBOHandler.getAllVideoTracks(this);
		for (MediaItem mediaItem : mediaItems) {
			Logger.s("ReloadTracksDataService Download Media Details : "
					+ mediaItem.getTitle());
			CommunicationManager communicationManager = new CommunicationManager();
			ServerConfigurations mServerConfigurations = DataManager
					.getInstance(this).getServerConfigurations();
			String images = ImagesManager.getImageSize(
					ImagesManager.MUSIC_ART_SMALL,
					DataManager.getDisplayDensityLabel())
					+ ","
					+ ImagesManager.getImageSize(ImagesManager.MUSIC_ART_BIG,
							DataManager.getDisplayDensityLabel());
			try {
				Response response;
				MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
						mServerConfigurations.getHungamaServerUrl_2(),
						mServerConfigurations.getHungamaAuthKey(),
						ApplicationConfigurations.getInstance(this)
								.getPartnerUserId(), mediaItem, null, images);
				response = communicationManager.performOperationNew(
						mediaDetailsOperation, this);
				Logger.i("response",
						"ReloadTracksDataService Download Video Media Detail Response:"
								+ response);
				if (response != null) {
					// Intent broadcastIntent = new Intent(
					// ActionDefinition.ACTION_MEDIA_DETAIL_RELOADED);
					// Map<String, Object> temp = mediaDetailsOperation
					// .parseResponse(response);
					// broadcastIntent
					// .putExtra(
					// MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS,
					// (MediaTrackDetails) temp
					// .get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS));
					// sendBroadcast(broadcastIntent);
					JSONObject jsonServerResponse;
					try {
						jsonServerResponse = new JSONObject(response.response);
						String savedResponse = DBOHandler.getVideoTrackDetails(
								this, "" + mediaItem.getId());
						JSONObject jsonResponse = new JSONObject(savedResponse);
						JSONObject jsonCatalog = null;
						if (jsonResponse.has("catalog")) {
							jsonCatalog = jsonResponse.getJSONObject("catalog")
									.getJSONObject("content");
						} else {
							jsonCatalog = jsonResponse
									.getJSONObject("response");
						}
						if (jsonCatalog.has("delivery_id")) {
							JSONObject jsonServerCatalog = jsonServerResponse
									.getJSONObject("response");
							jsonServerCatalog.put("delivery_id",
									jsonCatalog.getLong("delivery_id"));
						}
						DBOHandler.updateVideoTrack(this,
								"" + mediaItem.getId(), null,
								jsonServerResponse.toString(), null);
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
			} catch (InvalidRequestException e) {
				e.printStackTrace();
			} catch (InvalidResponseDataException e) {
				e.printStackTrace();
			} catch (OperationCancelledException e) {
				e.printStackTrace();
			} catch (NoConnectivityException e) {
				e.printStackTrace();
			}
		}
	}
}
