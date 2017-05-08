package com.hungama.myplay.activity.player;

//import android.app.Notification;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import org.json.JSONObject;

import java.util.List;

public class PlayerUpdateWidgetService extends Service implements
		PlayerStateListener, ServiceConnection {
	private static final String LOG = "PlayerWidgetService";
	private static final String EXTRA_START = "start";
	public static final String EXTRA_COMMAND = "command";
	private static final String EXTRA_STOP = "stop";
	private static final String EXTRA_PREVIOUS = "previous";
	private static final String EXTRA_NEXT = "next";
	private static final String EXTRA_CLOSE = "close";
	public static final String EXTRA_STOP_SERVICE = "stop_service";

	public static final int NOTIFICATION_PLAYING_CODE = 5325;
	public static final String ACTION_PLAYER_QUEUE_UPDATED = "com.hungama.myplay.activity.player.player_queue_updated";

	private Bitmap backgroundImage;
	private String backgroundLink;
	// private static Handler h;

	// private boolean isAdPlaying = false;
	private String adImageLink = null;
	private PlayerQueueUpdateReceiver playerQueueUpdateReceiver;
	private boolean isFromStop = false;
	private PlayerServiceBindingManager.ServiceToken mServiceToken = null;

	@Override
	public void onCreate() {
		Logger.e("UpdateWidget", " on service onCreate------------");
		super.onCreate();
		mServiceToken = PlayerServiceBindingManager.bindToService(this, this);
		if (playerQueueUpdateReceiver == null) {
			playerQueueUpdateReceiver = new PlayerQueueUpdateReceiver();
			registerReceiver(playerQueueUpdateReceiver, new IntentFilter(
					ACTION_PLAYER_QUEUE_UPDATED));
		}
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Logger.s(" ::::::::::::::onTaskRemoved:::::::::::::::::::: ");
//		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		manager.cancel(NOTIFICATION_PLAYING_CODE);
		super.onTaskRemoved(rootIntent);
	}

	@Override
	public void onDestroy() {
		Logger.s(" ::::::::::::::onDestroy:::::::::::::::::::: ");
//		NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//		manager.cancel(NOTIFICATION_PLAYING_CODE);
		// disconnects from the player service.
		if (PlayerService.service != null)
			PlayerService.service.unregisterPlayerStateListener(this);

		if (playerQueueUpdateReceiver != null)
			unregisterReceiver(playerQueueUpdateReceiver);
		if(mServiceToken!=null)
			PlayerServiceBindingManager.unbindFromService(mServiceToken);
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intentq, int startId) {

		Logger.i(LOG, " onStart Called");
		super.onStart(intentq, startId);
		// System.out.println(" :::::::::::::::::: onStart");

		if (intentq != null && intentq.hasExtra(EXTRA_COMMAND)) {
			if (intentq.getStringExtra(EXTRA_COMMAND)
					.equals(EXTRA_STOP_SERVICE)) {
				isFromStop = true;
				update();
				stopSelf();
				return;
			}
			isFromStop = true;
			if (PlayerService.service != null) {
				try {
					String command = intentq.getStringExtra(EXTRA_COMMAND);
//					 System.out.println(" :::::::::::::::::: " + command);
					if (command.equals(EXTRA_START)) {
//						 System.out.println(" :::::::::::::::::: 1");
						Logger.i(LOG, " EXTRA_COMMAND =" + EXTRA_START);
						if (PlayerService.service.getState() != State.PLAYING) {
//							 System.out.println(" :::::::::::::::::: 2");
							if (PlayerService.service.getState() == State.PAUSED) {
//								 System.out.println(" :::::::::::::::::: 3");
								PlayerService.service.play();
								sendBroadcast(new Intent(
										PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
							} else {
//								 System.out.println(" :::::::::::::::::: 4");
								PlayerService.service
										.playFromPosition(PlayerService.service
												.getCurrentQueuePosition());
								sendBroadcast(new Intent(
										PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
							}
//							 System.out.println(" :::::::::::::::::: 5");
						}
					} else if (command.equals(EXTRA_STOP)) {
//						 System.out.println(" :::::::::::::::::: 6");
						Logger.i(LOG, " EXTRA_COMMAND =" + EXTRA_STOP);
						if (PlayerService.service.isPlaying()
								&& !PlayerService.service.isAdPlaying()) {
//							 System.out.println(" :::::::::::::::::: 7");
							PlayerService.service.pause();
							sendBroadcast(new Intent(
									PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
						}
//						 System.out.println(" :::::::::::::::::: 8");
					} else if (command.equals(EXTRA_PREVIOUS)) {
						Logger.i(LOG, " EXTRA_COMMAND =" + EXTRA_PREVIOUS);
						if (PlayerService.service != null
								&& PlayerService.service.isPlayerLoading) {
							// Toast.makeText(getApplicationContext(),
							// "Please wait...", Toast.LENGTH_SHORT)
							// .show();
							Utils.makeText(getApplicationContext(),
									"Please wait...", 0).show();
						} else if (PlayerService.service.hasPrevious()) {
							PlayerService.service.previous();
						}
					} else if (command.equals(EXTRA_NEXT)) {
						Logger.i(LOG, " EXTRA_COMMAND =" + EXTRA_NEXT);
						if (PlayerService.service != null
								&& PlayerService.service.isPlayerLoading) {
							// Toast.makeText(getApplicationContext(),
							// "Please wait...", Toast.LENGTH_SHORT)
							// .show();
							Utils.makeText(getApplicationContext(),
									"Please wait...", 0).show();
						} else if (PlayerService.service.hasNext()) {
							PlayerService.service.next();
						}
					} else if (command.equals(EXTRA_CLOSE)) {
						Logger.i(LOG, " EXTRA_COMMAND =" + EXTRA_CLOSE);
						sendBroadcast(new Intent(HomeActivity.ACTION_CLOSE_APP));
						stopSelf();
						// NotificationManager manager = (NotificationManager)
						// getSystemService(NOTIFICATION_SERVICE);
						// manager.cancel(NOTIFICATION_PLAYING_CODE);
					}
				} catch (Exception e) {
				}
				return;
			}

		}
		update();
	}

	protected boolean isRunningInForeground() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<ActivityManager.RunningTaskInfo> tasks = manager
				.getRunningTasks(1);
		if (tasks.isEmpty()) {
			return false;
		}
		String topActivityName = tasks.get(0).topActivity.getPackageName();
		return topActivityName.equalsIgnoreCase(getPackageName());
	}

	// private boolean isFirstTime = true;

	private void update() {
		try {
			AppWidgetManager appWidgetManager = AppWidgetManager
					.getInstance(this.getApplicationContext());
			ComponentName thisWidget = new ComponentName(
					getApplicationContext(), PlayerWidgetProvider.class);

			int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
			Logger.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));

			for (int widgetId : allWidgetIds) {
				final RemoteViews remoteViews = new RemoteViews(this
						.getApplicationContext().getPackageName(),
						R.layout.player_widget_layout);
				if (PlayerService.service != null) {
					Track track = PlayerService.service
							.getCurrentPlayingTrack();
					try {
						if (track != null) {
							if (trackDetailsInEnglish == null
									|| trackDetailsInEnglish.getId() != track
											.getId()) {
								loadDataInEnglishOnly(track);
							}
							if (PlayerService.service.isAdPlaying()) {
								remoteViews.setTextViewText(
										R.id.player_widget_song_title,
										"Advertisement");
								remoteViews.setTextViewText(
										R.id.player_widget_song_detail, "");
							} else {
								if (trackDetailsInEnglish != null
										&& trackDetailsInEnglish.getId() == track
												.getId()) {
									remoteViews.setTextViewText(
											R.id.player_widget_song_title,
											""
													+ trackDetailsInEnglish
															.getTitle());
									remoteViews.setTextViewText(
											R.id.player_widget_song_detail,
											""
													+ trackDetailsInEnglish
															.getAlbumName());
								} else {
									remoteViews.setTextViewText(
											R.id.player_widget_song_title, ""
													+ track.getTitle());
									remoteViews.setTextViewText(
											R.id.player_widget_song_detail, ""
													+ track.getAlbumName());
								}
							}

							// final String url = track.getBigImageUrl();
							String url = ImagesManager
									.getMusicArtBigImageUrl(track
											.getImagesUrlArray());
							if (TextUtils.isEmpty(url))
								url = ImagesManager
										.getMusicArtSmallImageUrl(track
												.getImagesUrlArray());
							// System.out.println(" ::::::::::::::::::: " +
							// url);
							if (PlayerService.service.isAdPlaying()
									&& !TextUtils.isEmpty(adImageLink))
								updateImage(remoteViews, adImageLink);
							else
								updateImage(remoteViews, url);
							// if (getBaseContext() != null &&
							// !TextUtils.isEmpty(url)) {
							// if (backgroundImage == null ||
							// !url.equals(backgroundLink)) {
							// System.out.println(" ::::::::::::::::::: 1 ");
							// backgroundImage = null;
							// backgroundLink = url;
							// new Thread(new Runnable() {//callback
							// @Override
							// public void run() {
							// // if (backgroundImage == null) {
							// System.out.println(" ::::::::::::::::::: 2 ");
							// backgroundImage = Utils.getBitmap(
							// getApplicationContext(), url);
							// System.out.println(" ::::::::::::::::::: 3 ");
							// update();
							// // }
							// // h.sendEmptyMessage(0);
							// }
							// }).start();
							// }
							// // h = new Handler() {
							// // public void handleMessage(android.os.Message
							// msg) {
							// if(backgroundImage != null &&
							// url.equals(backgroundLink)){
							// System.out.println(" ::::::::::::::::::: 4 " +
							// backgroundImage.getHeight());
							// remoteViews.setImageViewBitmap(
							// R.id.player_widget_image_poster,
							// backgroundImage);
							// System.out.println(" ::::::::::::::::::: 5 " +
							// backgroundImage.getWidth());
							// }
							// // }
							// // };
							// }
							if (PlayerService.service.getState() != State.PAUSED
									&& (PlayerService.service.isPlaying()
											|| PlayerService.service
													.isAdPlaying() || PlayerService.service
												.isLoading())) {
								// Player is in playing or loading state.
								remoteViews.setViewVisibility(
										R.id.player_widget_button_play,
										View.GONE);
								if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {

									remoteViews
											.setImageViewResource(
													R.id.player_widget_button_pause,
													R.drawable.icon_widget_player_stop_white);
									/*
									 * remoteViews.setViewVisibility(
									 * R.id.player_widget_button_next,
									 * View.GONE);
									 */

								} else {

									remoteViews
											.setImageViewResource(
													R.id.player_widget_button_pause,
													R.drawable.icon_widget_player_pause_white);

								}
								remoteViews.setViewVisibility(
										R.id.player_widget_button_pause,
										View.VISIBLE);
								Logger.e("AppWidgetManager", "1");

							} else {

								remoteViews.setViewVisibility(
										R.id.player_widget_button_play,
										View.VISIBLE);
								remoteViews.setViewVisibility(
										R.id.player_widget_button_pause,
										View.GONE);
								Logger.e("AppWidgetManager", "2");
							}

						} else {
							remoteViews.setTextViewText(
									R.id.player_widget_song_title,
									"No songs loaded");
							remoteViews.setTextViewText(
									R.id.player_widget_song_detail, "");
							remoteViews.setViewVisibility(
									R.id.player_widget_button_play, View.GONE);
							remoteViews.setViewVisibility(
									R.id.player_widget_button_pause, View.GONE);
							Logger.e("AppWidgetManager", "3");
						}
					} catch (Exception e) {
						Logger.e("AppWidgetManager", "4 " + e);
					}

					if (PlayerService.service.hasNext()
							&& !PlayerService.service.isAdPlaying()) {
						remoteViews.setViewVisibility(
								R.id.player_widget_button_next, View.VISIBLE);
						remoteViews.setBoolean(R.id.player_widget_button_next,
								"setEnabled", true);
					} else {
						if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
							remoteViews.setBoolean(
									R.id.player_widget_button_next,
									"setEnabled", false);
							remoteViews.setViewVisibility(
									R.id.player_widget_button_next, View.GONE);
						} else {
							remoteViews.setBoolean(
									R.id.player_widget_button_next,
									"setEnabled", false);
							remoteViews.setViewVisibility(
									R.id.player_widget_button_next,
									View.VISIBLE);
						}

						// remoteViews.setViewVisibility(
						// R.id.player_widget_button_next, View.INVISIBLE);

					}

					/*
					 * if (!PlayerService.service.isAdPlaying() &&
					 * PlayerService.service.getPlayMode() ==
					 * PlayMode.LIVE_STATION_RADIO) { //
					 * remoteViews.setViewVisibility( //
					 * R.id.player_widget_button_prev, View.VISIBLE);
					 * remoteViews.setBoolean(R.id.player_widget_button_prev,
					 * "setEnabled", false); } else { //
					 * remoteViews.setViewVisibility( //
					 * R.id.player_widget_button_prev, View.INVISIBLE);
					 * remoteViews.setBoolean(R.id.player_widget_button_prev,
					 * "setEnabled", false); }
					 */

					if (PlayerService.service.hasPrevious()
							&& !PlayerService.service.isAdPlaying()
							&& PlayerService.service.getPlayMode() == PlayMode.MUSIC) {
						// remoteViews.setViewVisibility(
						// R.id.player_widget_button_prev, View.VISIBLE);
						remoteViews.setViewVisibility(
								R.id.player_widget_button_prev, View.VISIBLE);
						remoteViews.setBoolean(R.id.player_widget_button_prev,
								"setEnabled", true);

					} else {
						// remoteViews.setViewVisibility(
						// R.id.player_widget_button_prev, View.INVISIBLE);
						if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO
								|| PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
								|| PlayerService.service.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
							remoteViews.setBoolean(
									R.id.player_widget_button_prev,
									"setEnabled", false);
							remoteViews.setViewVisibility(
									R.id.player_widget_button_prev, View.GONE);
						} else {
							remoteViews.setViewVisibility(
									R.id.player_widget_button_prev,
									View.VISIBLE);
							remoteViews.setBoolean(
									R.id.player_widget_button_prev,
									"setEnabled", false);

						}

					}
				} else {
					remoteViews.setTextViewText(R.id.player_widget_song_title,
							"No songs loaded");
					remoteViews.setTextViewText(R.id.player_widget_song_detail,
							"");
					remoteViews.setViewVisibility(
							R.id.player_widget_button_play, View.GONE);
					remoteViews.setViewVisibility(
							R.id.player_widget_button_pause, View.GONE);
					remoteViews.setViewVisibility(
							R.id.player_widget_button_prev, View.GONE);
					remoteViews.setViewVisibility(
							R.id.player_widget_button_next, View.GONE);
					Logger.e("AppWidgetManager", "3");
					updateImage(remoteViews, "default");
				}

				// Register an onClickListener
				Intent playclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				playclickIntent.putExtra(EXTRA_COMMAND, EXTRA_START);
				PendingIntent pendingIntent = PendingIntent.getService(
						getApplicationContext(), 5555, playclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(
						R.id.player_widget_button_play, pendingIntent);

				Intent pauseclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				pauseclickIntent.putExtra(EXTRA_COMMAND, EXTRA_STOP);
				pendingIntent = PendingIntent.getService(
						getApplicationContext(), 5556, pauseclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(
						R.id.player_widget_button_pause, pendingIntent);

				Intent prevclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				prevclickIntent.putExtra(EXTRA_COMMAND, EXTRA_PREVIOUS);
				PendingIntent pendingIntentPrev = PendingIntent.getService(
						getApplicationContext(), 5557, prevclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(
						R.id.player_widget_button_prev, pendingIntentPrev);

				Intent nextclickIntent = new Intent(getBaseContext(),
						PlayerUpdateWidgetService.class);
				nextclickIntent.putExtra(EXTRA_COMMAND, EXTRA_NEXT);
				PendingIntent pendingIntentNext = PendingIntent.getService(
						getApplicationContext(), 5558, nextclickIntent,
						PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews.setOnClickPendingIntent(
						R.id.player_widget_button_next, pendingIntentNext);

				Intent startHomeIntent = new Intent(this, HomeActivity.class);
				startHomeIntent.putExtra("donothing", true);
				startHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				PendingIntent startHomePendingIntent = PendingIntent
						.getActivity(this, NOTIFICATION_PLAYING_CODE,
								startHomeIntent,
								PendingIntent.FLAG_UPDATE_CURRENT);
				remoteViews
						.setOnClickPendingIntent(
								R.id.player_widget_image_poster,
								startHomePendingIntent);

				appWidgetManager.updateAppWidget(widgetId, remoteViews);
			}
		} catch (Exception e) {
			Logger.e("AppWidgetManager>>", "" + e);
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			Logger.e("AppWidgetManager>>", "" + e);
			e.printStackTrace();
		}

//		Boolean needNotToShowNotification = false;
//
//		RemoteViews remoteViewNotification;
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//			remoteViewNotification = new RemoteViews(this
//					.getApplicationContext().getPackageName(),
//					R.layout.player_widget_notification_new);
//		else
//			remoteViewNotification = new RemoteViews(this
//					.getApplicationContext().getPackageName(),
//					R.layout.player_widget_notification);
//
//		if (PlayerService.service != null) {
//			Track track = PlayerService.service.getCurrentPlayingTrack();
//
//			try {
//				if (track != null) {
//					if (trackDetailsInEnglish == null
//							|| trackDetailsInEnglish.getId() != track.getId()) {
//						loadDataInEnglishOnly(track);
//					}
//					if (PlayerService.service.isAdPlaying()) {
//						remoteViewNotification.setTextViewText(
//								R.id.player_widget_song_title, "Advertisement");
//						remoteViewNotification.setTextViewText(
//								R.id.player_widget_song_detail, "");
//					} else {
//						if (trackDetailsInEnglish != null
//								&& trackDetailsInEnglish.getId() == track
//										.getId()) {
//							remoteViewNotification.setTextViewText(
//									R.id.player_widget_song_title, ""
//											+ trackDetailsInEnglish.getTitle());
//							remoteViewNotification.setTextViewText(
//									R.id.player_widget_song_detail,
//									"" + trackDetailsInEnglish.getAlbumName());
//						} else {
//							remoteViewNotification.setTextViewText(
//									R.id.player_widget_song_title,
//									"" + track.getTitle());
//							remoteViewNotification.setTextViewText(
//									R.id.player_widget_song_detail,
//									"" + track.getAlbumName());
//						}
//					}
//
//					// final String url = track.getBigImageUrl();
//					String url = ImagesManager.getMusicArtBigImageUrl(track
//							.getImagesUrlArray());
//					if (TextUtils.isEmpty(url))
//						url = ImagesManager.getMusicArtSmallImageUrl(track
//								.getImagesUrlArray());
//					// System.out.println(" ::::::::::::::::::: notification :: "
//					// + url);
//					// remoteViewNotification.setImageViewUri(R.id.player_widget_image_poster,
//					// Uri.parse(url));
//					if (PlayerService.service.isAdPlaying()
//							&& !TextUtils.isEmpty(adImageLink))
//						updateImage(remoteViewNotification, adImageLink);
//					else
//						updateImage(remoteViewNotification, url);
//					// if (getBaseContext() != null && !TextUtils.isEmpty(url))
//					// {
//					// if (url != null) {
//					// System.out.println(" ::::::::::::::::::: 1 ");
//					// new Thread(new Runnable() {//callback
//					// @Override
//					// public void run() {
//					// if (backgroundImage == null) {
//					// System.out.println(" ::::::::::::::::::: 2 ");
//					// backgroundImage = Utils.getBitmap(
//					// getApplicationContext(), url);
//					// System.out.println(" ::::::::::::::::::: 3 ");
//					// }
//					// h.sendEmptyMessage(0);
//					// }
//					// }).start();
//					// }
//					// h = new Handler() {
//					// public void handleMessage(android.os.Message msg) {
//					// if(backgroundImage != null){
//					// FileCache fileCache = new
//					// FileCache(getApplicationContext());
//					// File f = fileCache.getFile(url);
//					// // String path = f.getAbsolutePath();
//					// // File file = new
//					// File(CommonFunctions.getBaseURL(mContext) + publishedId +
//					// "_poster.jpg");
//					// remoteViewNotification.setImageViewUri(R.id.player_widget_image_poster,
//					// Uri.fromFile(f));
//					// System.out.println(" ::::::::::::::::::: 4 " +
//					// backgroundImage.getHeight());
//					// // remoteViewNotification.setImageViewBitmap(
//					// // R.id.player_widget_image_poster, backgroundImage);
//					// System.out.println(" ::::::::::::::::::: 5 " +
//					// backgroundImage.getWidth());
//					// }
//					// }
//					// };
//					// }
//					Logger.e("AppWidgetManager", " Player State ::: "
//							+ PlayerService.service.getState());
//					if (PlayerService.service.getState() != State.PAUSED
//							&& (PlayerService.service.isPlaying()
//									|| PlayerService.service.isAdPlaying() || PlayerService.service
//										.isLoading())) {
//						// Player is in playing or loading state.
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_play, View.GONE);
//
//						if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
//							Logger.i("Test", "1");
//							remoteViewNotification.setImageViewResource(
//									R.id.player_widget_button_pause,
//									R.drawable.icon_widget_player_stop_white);
//							/*
//							 * remoteViewNotification.setViewVisibility(
//							 * R.id.player_widget_button_next, View.GONE);
//							 */
//
//						} else {
//							Logger.i("Test", "else");
//							remoteViewNotification.setImageViewResource(
//									R.id.player_widget_button_pause,
//									R.drawable.icon_widget_player_pause_white);
//
//						}
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_pause, View.VISIBLE);
//						Logger.e("AppWidgetManager", "1");
//
//					} else if (PlayerService.service.getState() == State.STOPPED) {
//						// needNotToShowNotification = true;
//						Logger.e("AppWidgetManager", "3");
//					} else {
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_play, View.VISIBLE);
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_pause, View.GONE);
//						Logger.e("AppWidgetManager", "2");
//					}
//
//				} else {
//					if (PlayerService.service.getPlayingQueue() == null
//							|| PlayerService.service.getPlayingQueue().size() == 0)
//						needNotToShowNotification = true;
//					// needNotToShowNotification = true;
//				}
//			} catch (Exception e) {
//				needNotToShowNotification = true;
//				Logger.e("AppWidgetManager", "4 " + e);
//			}
//
//			try {
//				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//					if (PlayerService.service.hasNext()
//							&& !PlayerService.service.isAdPlaying()
//							&& PlayerService.service.getPlayMode() != PlayMode.LIVE_STATION_RADIO) {
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_next, View.VISIBLE);
//
//						remoteViewNotification.setBoolean(
//								R.id.player_widget_button_next, "setEnabled",
//								true);
//					} else {
//						if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
//							remoteViewNotification.setBoolean(
//									R.id.player_widget_button_next,
//									"setEnabled", false);
//							remoteViewNotification.setViewVisibility(
//									R.id.player_widget_button_next,
//									View.INVISIBLE);
//						} else {
//							remoteViewNotification.setBoolean(
//									R.id.player_widget_button_next,
//									"setEnabled", false);
//						}
//
//					}
//
//					if (PlayerService.service.hasPrevious()
//							&& !PlayerService.service.isAdPlaying()
//							&& PlayerService.service.getPlayMode() != PlayMode.TOP_ARTISTS_RADIO
//							&& PlayerService.service.getPlayMode() != PlayMode.DISCOVERY_MUSIC
//							&& PlayerService.service.getPlayMode() != PlayMode.LIVE_STATION_RADIO) {
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_prev, View.VISIBLE);
//						remoteViewNotification.setBoolean(
//								R.id.player_widget_button_prev, "setEnabled",
//								true);
//
//					} else {
//
//						if (PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
//								|| PlayerService.service.getPlayMode() == PlayMode.DISCOVERY_MUSIC
//								|| PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
//							remoteViewNotification.setBoolean(
//									R.id.player_widget_button_prev,
//									"setEnabled", false);
//							remoteViewNotification.setViewVisibility(
//									R.id.player_widget_button_prev,
//									View.INVISIBLE);
//
//						} else {
//							remoteViewNotification.setViewVisibility(
//									R.id.player_widget_button_prev,
//									View.VISIBLE);
//							remoteViewNotification.setBoolean(
//									R.id.player_widget_button_prev,
//									"setEnabled", false);
//						}
//					}
//
//					/*
//					 * if (!PlayerService.service.isAdPlaying() &&
//					 * PlayerService.service.getPlayMode() ==
//					 * PlayMode.LIVE_STATION_RADIO ) {
//					 * remoteViewNotification.setBoolean(
//					 * R.id.player_widget_button_next, "setEnabled", false); }
//					 * else { remoteViewNotification.setBoolean(
//					 * R.id.player_widget_button_next, "setEnabled", false); }
//					 */
//				}
//			} catch (Exception e) {
//				Logger.printStackTrace(e);
//			}
//		} else {
//			needNotToShowNotification = true;
//		}
//
//		// Register an onClickListener
//		Intent playclickIntent = new Intent(getBaseContext(),
//				PlayerUpdateWidgetService.class);
//		playclickIntent.putExtra(EXTRA_COMMAND, EXTRA_START);
//		PendingIntent pendingIntent = PendingIntent.getService(
//				getApplicationContext(), 5555, playclickIntent,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//		remoteViewNotification.setOnClickPendingIntent(
//				R.id.player_widget_button_play, pendingIntent);
//
//		Intent pauseclickIntent = new Intent(getBaseContext(),
//				PlayerUpdateWidgetService.class);
//		pauseclickIntent.putExtra(EXTRA_COMMAND, EXTRA_STOP);
//		pendingIntent = PendingIntent.getService(getApplicationContext(), 5556,
//				pauseclickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//		remoteViewNotification.setOnClickPendingIntent(
//				R.id.player_widget_button_pause, pendingIntent);
//
//		try {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//				Intent prevclickIntent = new Intent(getBaseContext(),
//						PlayerUpdateWidgetService.class);
//				prevclickIntent.putExtra(EXTRA_COMMAND, EXTRA_PREVIOUS);
//				PendingIntent pendingIntentPrev = PendingIntent.getService(
//						getApplicationContext(), 5557, prevclickIntent,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				remoteViewNotification.setOnClickPendingIntent(
//						R.id.player_widget_button_prev, pendingIntentPrev);
//
//				Intent nextclickIntent = new Intent(getBaseContext(),
//						PlayerUpdateWidgetService.class);
//				nextclickIntent.putExtra(EXTRA_COMMAND, EXTRA_NEXT);
//				PendingIntent pendingIntentNext = PendingIntent.getService(
//						getApplicationContext(), 5558, nextclickIntent,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				remoteViewNotification.setOnClickPendingIntent(
//						R.id.player_widget_button_next, pendingIntentNext);
//
//				Intent closeclickIntent = new Intent(getBaseContext(),
//						PlayerUpdateWidgetService.class);
//				closeclickIntent.putExtra(EXTRA_COMMAND, EXTRA_CLOSE);
//				PendingIntent pendingIntentClose = PendingIntent.getService(
//						getApplicationContext(), 5559, closeclickIntent,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				remoteViewNotification.setOnClickPendingIntent(
//						R.id.player_widget_button_close, pendingIntentClose);
//			}
//		} catch (Exception e) {
//			Logger.printStackTrace(e);
//		}
//
//		// remoteViewNotification.setViewVisibility(
//		// R.id.player_widget_button_play, View.GONE);
//		// remoteViewNotification.setViewVisibility(
//		// R.id.player_widget_button_pause, View.GONE);
//
//		Notification notification;
//		// notification = new Notification.Builder(getBaseContext())
//		// .setContent(remoteViewNotification).setAutoCancel(false)
//		// .setOngoing(true).build();
//		notification = new NotificationCompat.Builder(getBaseContext())
//				.setContent(remoteViewNotification).setAutoCancel(false)
//				.setOngoing(true).build();
//
//		notification.icon = R.drawable.ic_notification;
//		// notification.setSmallIcon(R.drawable.icon_launcher);
//
//		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
//			notification = new Notification();
//			notification.contentView = remoteViewNotification;
//			notification.flags |= Notification.FLAG_ONGOING_EVENT;
//			notification.icon = R.drawable.ic_notification;
//		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//			notification.contentView = getSmallRemoteView();
//			notification.flags |= Notification.FLAG_ONGOING_EVENT;
//			notification.icon = R.drawable.ic_notification;
//			notification.bigContentView = remoteViewNotification;
//		}
//		try {
//			NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//			if (!needNotToShowNotification && (HomeActivity.Instance!=null || GoOfflineActivity.Instance!=null)) {
//				DataManager mDataManager = DataManager
//						.getInstance(getApplicationContext());
//				// ApplicationConfigurations mApplicationConfigurations =
//				// mDataManager
//				// .getApplicationConfigurations();
//				// Intent startHomeIntent = new Intent(this,
//				// HomeActivity.class);
//				// if (mApplicationConfigurations.getSaveOfflineMode()) {
//				// startHomeIntent = new Intent(this, GoOfflineActivity.class);
//				// }
//				// // else if (PlayerService.service.getPlayMode() ==
//				// PlayMode.TOP_ARTISTS_RADIO
//				// // || PlayerService.service.getPlayMode() ==
//				// PlayMode.LIVE_STATION_RADIO) {
//				// // startHomeIntent = new Intent(this, RadioActivity.class);
//				// // }
//				// // startHomeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//				// // | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				// startHomeIntent.putExtra("donothing", true);
//				// //
//				// startHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				// PendingIntent startHomePendingIntent = PendingIntent
//				// .getActivity(this, NOTIFICATION_PLAYING_CODE,
//				// startHomeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//				Intent startHomeIntent = new Intent(this,
//						NotificationActivity.class);
//				startHomeIntent.putExtra("donothing", true);
//				// startHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				PendingIntent startHomePendingIntent = PendingIntent
//						.getActivity(this, NOTIFICATION_PLAYING_CODE,
//								startHomeIntent,
//								PendingIntent.FLAG_UPDATE_CURRENT);
//
////				notification.flags |= Notification.FLAG_NO_CLEAR;
//				notification.flags = Notification.FLAG_FOREGROUND_SERVICE |
//						Notification.FLAG_NO_CLEAR |
//						Notification.FLAG_ONGOING_EVENT;
//				notification.contentIntent = startHomePendingIntent;
//				// notification.getNotification().flags |=
//				// Notification.FLAG_NO_CLEAR;
//				// notification.setContentIntent(startHomePendingIntent);
//				manager.notify(NOTIFICATION_PLAYING_CODE, notification);
//				startForeground(NOTIFICATION_PLAYING_CODE, notification);
//				Logger.e("Notification Notify", "@@@@@@@@@@@");
//			} else {
//				manager.cancel(NOTIFICATION_PLAYING_CODE);
//				Logger.e("Notification cancel", "@@@@@@@@@@@");
//			}
//		} catch (Exception e) {
//			Logger.printStackTrace(e);
//		} catch (OutOfMemoryError e) {
//			Logger.printStackTrace(e);
//		}
	}

	// stopSelf();
	// super.onStart(intent, startId);

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onStartLoadingTrack(Track track) {

	}

	@Override
	public void onTrackLoadingBufferUpdated(Track track, int precent) {

	}

	@Override
	public void onStartPlayingTrack(Track track) {
		update();

	}

	@Override
	public void onFinishPlayingTrack(Track track) {
		update();

	}

	@Override
	public void onFinishPlayingQueue() {
	}

	@Override
	public void onSleepModePauseTrack(Track track) {
	}

	@Override
	public void onStartPlayingAd(Placement p) {
		// isAdPlaying = true;
		adImageLink = Utils.getDisplayProfile(HomeActivity.metrics, p);
		update();
	}

	@Override
	public void onAdCompletion() {
		// isAdPlaying = false;
		adImageLink = null;
		update();
	}

	@Override
	public void onErrorHappened(
			com.hungama.myplay.activity.player.PlayerService.Error error) {

	}

	private void updateImage(RemoteViews remoteViews, final String url) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
				return;

			// System.out.println(" ::::::::::::::::::: " + url);
			if(!TextUtils.isEmpty(url) && url.equals("default")){
//				System.out.println("updateImage :::::::::: ");
				remoteViews.setImageViewResource(
						R.id.player_widget_image_poster,
						// R.drawable.icon_launcher);
						R.drawable.background_home_tile_album_default);
			} else if (getBaseContext() != null && !TextUtils.isEmpty(url)) {
//				System.out.println("updateImage :::::::::: 1");
				if (/* backgroundImage == null || */backgroundLink == null
						|| !url.equals(backgroundLink)) {
					// System.out.println(" ::::::::::::::::::: 1 ");
//					System.out.println("updateImage :::::::::: 2");
					backgroundImage = null;
					backgroundLink = url;
					remoteViews.setImageViewResource(
							R.id.player_widget_image_poster,
							// R.drawable.icon_launcher);
							R.drawable.background_home_tile_album_default);
					new Thread(new Runnable() {// callback
								@Override
								public void run() {
									// if (backgroundImage == null) {
									// System.out.println(" ::::::::::::::::::: 2 ");
									backgroundImage = Utils.getBitmap(
											getApplicationContext(), url);
									// System.out.println(" ::::::::::::::::::: 3 ");
									update();
									// }
									// h.sendEmptyMessage(0);
								}
							}).start();
				} else
//					System.out.println("updateImage :::::::::: 3");
				// h = new Handler() {
				// public void handleMessage(android.os.Message msg) {
				if (backgroundImage != null && url.equals(backgroundLink)) {
//					System.out.println("updateImage :::::::::: 4");
					// System.out.println(" ::::::::::::::::::: 4 " +
					// backgroundImage.getHeight());
					remoteViews.setImageViewBitmap(
							R.id.player_widget_image_poster, backgroundImage);
					// System.out.println(" ::::::::::::::::::: 5 " +
					// backgroundImage.getWidth());
				}
				// }
				// };
			} else {
//				System.out.println("updateImage :::::::::: 5");
				remoteViews.setImageViewResource(
						R.id.player_widget_image_poster,
						R.drawable.icon_launcher);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		/*
		 * we've establish a connection to the player service. if it plays,
		 * pause it.
		 */
		PlayerService.PlayerSericeBinder binder = (PlayerService.PlayerSericeBinder) service;
		PlayerService playerService = binder.getService();

		// does nothing, just holds the connection to the playing service.
	}

	@Override
	public void onServiceDisconnected(ComponentName name) {
		mServiceToken = null;
	}

	private class PlayerQueueUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			update();
		}
	}

//	private RemoteViews getSmallRemoteView() {
//		RemoteViews remoteViewNotification;
//		// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//		// remoteViewNotification = new RemoteViews(this
//		// .getApplicationContext().getPackageName(),
//		// R.layout.player_widget_notification_new);
//		// else
//		remoteViewNotification = new RemoteViews(this.getApplicationContext()
//				.getPackageName(),
//				R.layout.player_widget_notification_new_small);
//
//		if (PlayerService.service != null) {
//			Track track=null;
//			try{
//				track = PlayerService.service.getCurrentPlayingTrack();
//			}catch (Exception e){}
//
//
//			try {
//				if (track != null) {
//					if (trackDetailsInEnglish == null
//							|| trackDetailsInEnglish.getId() != track.getId()) {
//						loadDataInEnglishOnly(track);
//					}
//					if (PlayerService.service.isAdPlaying()) {
//						remoteViewNotification.setTextViewText(
//								R.id.player_widget_song_title, "Advertisement");
//						remoteViewNotification.setTextViewText(
//								R.id.player_widget_song_detail, "");
//					} else {
//						if (trackDetailsInEnglish != null
//								&& trackDetailsInEnglish.getId() == track
//										.getId()) {
//							remoteViewNotification.setTextViewText(
//									R.id.player_widget_song_title, ""
//											+ trackDetailsInEnglish.getTitle());
//							remoteViewNotification.setTextViewText(
//									R.id.player_widget_song_detail,
//									"" + trackDetailsInEnglish.getAlbumName());
//						} else {
//							remoteViewNotification.setTextViewText(
//									R.id.player_widget_song_title,
//									"" + track.getTitle());
//							remoteViewNotification.setTextViewText(
//									R.id.player_widget_song_detail,
//									"" + track.getAlbumName());
//						}
//					}
//
//					// final String url = track.getBigImageUrl();
//					String url = ImagesManager.getMusicArtBigImageUrl(track
//							.getImagesUrlArray());
//					if (TextUtils.isEmpty(url))
//						url = ImagesManager.getMusicArtSmallImageUrl(track
//								.getImagesUrlArray());
//					// System.out.println(" ::::::::::::::::::: notification :: "
//					// + url);
//					// remoteViewNotification.setImageViewUri(R.id.player_widget_image_poster,
//					// Uri.parse(url));
//					if (PlayerService.service.isAdPlaying()
//							&& !TextUtils.isEmpty(adImageLink))
//						updateImage(remoteViewNotification, adImageLink);
//					else
//						updateImage(remoteViewNotification, url);
//
//					Logger.e("AppWidgetManager", "Player State ::: "
//							+ PlayerService.service.getState());
//					if (PlayerService.service.getState() != State.PAUSED
//							&& (PlayerService.service.isPlaying()
//									|| PlayerService.service.isAdPlaying() || PlayerService.service
//										.isLoading())) {
//						// Player is in playing or loading state.
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_play, View.GONE);
//
//						if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
//
//							remoteViewNotification.setImageViewResource(
//									R.id.player_widget_button_pause,
//									R.drawable.icon_widget_player_stop_white);
//							/*
//							 * remoteViewNotification.setViewVisibility(
//							 * R.id.player_widget_button_next, View.GONE);
//							 * remoteViewNotification.setViewVisibility(
//							 * R.id.player_widget_button_prev, View.GONE);
//							 */
//						} else {
//							remoteViewNotification.setImageViewResource(
//									R.id.player_widget_button_pause,
//									R.drawable.icon_widget_player_pause_white);
//
//						}
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_pause, View.VISIBLE);
//						Logger.e("AppWidgetManager", "1");
//
//					} else if (PlayerService.service.getState() == State.STOPPED) {
//						// needNotToShowNotification = true;
//						Logger.e("AppWidgetManager", "3");
//					} else {
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_play, View.VISIBLE);
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_pause, View.GONE);
//						Logger.e("AppWidgetManager", "2");
//					}
//
//				} else {
//					// needNotToShowNotification = true;
//				}
//			} catch (Exception e) {
//				// needNotToShowNotification = true;
//				Logger.e("AppWidgetManager", "4 " + e);
//			}
//
//			try {
//
//				/*
//				 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//				 * { if (PlayerService.service.hasNext() &&
//				 * !PlayerService.service.isAdPlaying()) {
//				 * remoteViewNotification.setBoolean(
//				 * R.id.player_widget_button_next, "setEnabled", true); } else {
//				 *
//				 * remoteViewNotification.setBoolean(
//				 * R.id.player_widget_button_next, "setEnabled", false);
//				 *
//				 *
//				 * }
//				 *
//				 * if (PlayerService.service.hasPrevious() &&
//				 * !PlayerService.service.isAdPlaying() &&
//				 * PlayerService.service.getPlayMode() !=
//				 * PlayMode.TOP_ARTISTS_RADIO) {
//				 * remoteViewNotification.setBoolean(
//				 * R.id.player_widget_button_prev, "setEnabled", true); } else {
//				 * remoteViewNotification.setBoolean(
//				 * R.id.player_widget_button_prev, "setEnabled", false); } }
//				 */
//				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//					if (PlayerService.service.hasNext()
//							&& !PlayerService.service.isAdPlaying()
//							&& PlayerService.service.getPlayMode() != PlayMode.LIVE_STATION_RADIO) {
//						remoteViewNotification.setViewVisibility(
//								R.id.player_widget_button_next, View.VISIBLE);
//
//						remoteViewNotification.setBoolean(
//								R.id.player_widget_button_next, "setEnabled",
//								true);
//					} else {
//						if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
//							remoteViewNotification.setBoolean(
//									R.id.player_widget_button_next,
//									"setEnabled", false);
//							remoteViewNotification.setViewVisibility(
//									R.id.player_widget_button_next, View.GONE);
//						} else {
//							remoteViewNotification.setBoolean(
//									R.id.player_widget_button_next,
//									"setEnabled", false);
//						}
//
//					}
//
//					// if (PlayerService.service.hasPrevious()
//					// && !PlayerService.service.isAdPlaying()
//					// && PlayerService.service.getPlayMode() !=
//					// PlayMode.TOP_ARTISTS_RADIO
//					// && PlayerService.service.getPlayMode() !=
//					// PlayMode.DISCOVERY_MUSIC
//					// && PlayerService.service.getPlayMode() !=
//					// PlayMode.LIVE_STATION_RADIO) {
//					// remoteViewNotification.setViewVisibility(
//					// R.id.player_widget_button_prev, View.VISIBLE);
//					// remoteViewNotification.setBoolean(
//					// R.id.player_widget_button_prev, "setEnabled",
//					// true);
//					//
//					// } else {
//					//
//					// if (PlayerService.service.getPlayMode() ==
//					// PlayMode.TOP_ARTISTS_RADIO
//					// || PlayerService.service.getPlayMode() ==
//					// PlayMode.DISCOVERY_MUSIC
//					// ||PlayerService.service.getPlayMode() ==
//					// PlayMode.LIVE_STATION_RADIO) {
//					// remoteViewNotification.setBoolean(
//					// R.id.player_widget_button_prev,
//					// "setEnabled", false);
//					// remoteViewNotification.setViewVisibility(
//					// R.id.player_widget_button_prev, View.GONE);
//					//
//					// } else {
//					// remoteViewNotification.setViewVisibility(
//					// R.id.player_widget_button_prev, View.VISIBLE);
//					// remoteViewNotification.setBoolean(
//					// R.id.player_widget_button_prev,
//					// "setEnabled", false);
//					// }
//					// }
//
//					/*
//					 * if (!PlayerService.service.isAdPlaying() &&
//					 * PlayerService.service.getPlayMode() ==
//					 * PlayMode.LIVE_STATION_RADIO) {
//					 * remoteViewNotification.setBoolean(
//					 * R.id.player_widget_button_next, "setEnabled", false); }
//					 * else { remoteViewNotification.setBoolean(
//					 * R.id.player_widget_button_next, "setEnabled", false); }
//					 */
//				}
//			} catch (Exception e) {
//				Logger.printStackTrace(e);
//			}
//		} else {
//			// needNotToShowNotification = true;
//		}
//
//		// Register an onClickListener
//		Intent playclickIntent = new Intent(getBaseContext(),
//				PlayerUpdateWidgetService.class);
//		playclickIntent.putExtra(EXTRA_COMMAND, EXTRA_START);
//		PendingIntent pendingIntent = PendingIntent.getService(
//				getApplicationContext(), 5555, playclickIntent,
//				PendingIntent.FLAG_UPDATE_CURRENT);
//		remoteViewNotification.setOnClickPendingIntent(
//				R.id.player_widget_button_play, pendingIntent);
//
//		Intent pauseclickIntent = new Intent(getBaseContext(),
//				PlayerUpdateWidgetService.class);
//		pauseclickIntent.putExtra(EXTRA_COMMAND, EXTRA_STOP);
//		pendingIntent = PendingIntent.getService(getApplicationContext(), 5556,
//				pauseclickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//		remoteViewNotification.setOnClickPendingIntent(
//				R.id.player_widget_button_pause, pendingIntent);
//
//		try {
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//				Intent prevclickIntent = new Intent(getBaseContext(),
//						PlayerUpdateWidgetService.class);
//				prevclickIntent.putExtra(EXTRA_COMMAND, EXTRA_PREVIOUS);
//				PendingIntent pendingIntentPrev = PendingIntent.getService(
//						getApplicationContext(), 5557, prevclickIntent,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				remoteViewNotification.setOnClickPendingIntent(
//						R.id.player_widget_button_prev, pendingIntentPrev);
//
//				Intent nextclickIntent = new Intent(getBaseContext(),
//						PlayerUpdateWidgetService.class);
//				nextclickIntent.putExtra(EXTRA_COMMAND, EXTRA_NEXT);
//				PendingIntent pendingIntentNext = PendingIntent.getService(
//						getApplicationContext(), 5558, nextclickIntent,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				remoteViewNotification.setOnClickPendingIntent(
//						R.id.player_widget_button_next, pendingIntentNext);
//
//				Intent closeclickIntent = new Intent(getBaseContext(),
//						PlayerUpdateWidgetService.class);
//				closeclickIntent.putExtra(EXTRA_COMMAND, EXTRA_CLOSE);
//				PendingIntent pendingIntentClose = PendingIntent.getService(
//						getApplicationContext(), 5559, closeclickIntent,
//						PendingIntent.FLAG_UPDATE_CURRENT);
//				remoteViewNotification.setOnClickPendingIntent(
//						R.id.player_widget_button_close, pendingIntentClose);
//
//				Intent startHomeIntent = new Intent(this, HomeActivity.class);
//				startHomeIntent.putExtra("donothing", true);
//				startHomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				PendingIntent startHomePendingIntent = PendingIntent
//						.getActivity(this, NOTIFICATION_PLAYING_CODE,
//								startHomeIntent,
//								PendingIntent.FLAG_UPDATE_CURRENT);
//				remoteViewNotification
//						.setOnClickPendingIntent(
//								R.id.player_widget_image_poster,
//								startHomePendingIntent);
//				remoteViewNotification.setOnClickPendingIntent(
//						R.id.player_widget_song_title, startHomePendingIntent);
//				remoteViewNotification.setOnClickPendingIntent(
//						R.id.player_widget_song_detail, startHomePendingIntent);
//
//			}
//		} catch (Exception e) {
//			Logger.printStackTrace(e);
//		}
//		return remoteViewNotification;
//	}

	private MediaTrackDetails trackDetailsInEnglish;

	private void loadDataInEnglishOnly(final Track track) {
		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(PlayerUpdateWidgetService.this);
		if (appConfig.getUserSelectedLanguage() != 0
				&& !appConfig.isLanguageSupportedForWidget()) {
			ThreadPoolManager.getInstance().submit(new Runnable() {
				@Override
				public void run() {
//					super.run();
					CommunicationManager communicationManager = new CommunicationManager();
					try {
						MediaItem mediaItem = new MediaItem(track.getId(),
								null, null, null, null, null,
								MediaType.TRACK.toString(), 0,
								track.getAlbumId());
						ServerConfigurations mServerConfigurations = DataManager
								.getInstance(PlayerUpdateWidgetService.this)
								.getServerConfigurations();
						Response response = communicationManager
								.performOperationNew(
										new MediaDetailsOperation(
												mServerConfigurations
														.getHungamaServerUrl_2(),
												mServerConfigurations
														.getHungamaAuthKey(),
												ApplicationConfigurations
														.getInstance(
																PlayerUpdateWidgetService.this)
														.getPartnerUserId(),
												mediaItem, null, null,
												"english"),
										PlayerUpdateWidgetService.this);
						Gson gson = new GsonBuilder()
								.excludeFieldsWithoutExposeAnnotation()
								.create();
						trackDetailsInEnglish = (MediaTrackDetails) gson
								.fromJson(new JSONObject(response.response)
										.getJSONObject("response").toString(),
										MediaTrackDetails.class);
						update();
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
			});
		}
	}
}