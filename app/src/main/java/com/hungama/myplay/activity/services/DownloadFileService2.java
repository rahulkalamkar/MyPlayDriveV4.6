package com.hungama.myplay.activity.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.DeviceConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.events.PlayEvent;
import com.hungama.myplay.activity.data.events.PlayEvent.PlayingSourceType;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FileUtils;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ToastExpander;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

//import org.apache.http.HttpEntity;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * Service that takes place as track downloading queue, only one client can
 * follow the process.
 */
public class DownloadFileService2 extends IntentService implements
		DownloadCompleteListner {
	private static final String TAG = "DownloadFileService2";
	public static final String DOWNLOAD_URL = "download_url";
	public static final String TRACK_KEY = "track_key";
	private Handler mHandler;
	private FileUtils fileUtils;
	private File mediaFolder;

	public DownloadFileService2() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
		Logger.i(TAG, "Start");
		fileUtils = new FileUtils(getApplicationContext());
		notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (downloadVector == null)
			downloadVector = new Vector<DownloadFileService2.DownloadObject>();
	}

	static Vector<DownloadObject> downloadVector;

	class DownloadObject {
		public DownloadObject(MediaItem mCurrentMediaItem2,
				String responseDownloadUrl2) {
			this.mCurrentMediaItem = mCurrentMediaItem2;
			this.responseDownloadUrl = responseDownloadUrl2;
		}

		MediaItem mCurrentMediaItem;
		String responseDownloadUrl;
	}

	static DownloadFile downloader = null;

	@Override
	protected void onHandleIntent(Intent intent) {
		if (downloadVector == null)
			downloadVector = new Vector<DownloadFileService2.DownloadObject>();
		if (intent == null)
			return;
		String responseDownloadUrl = intent.getStringExtra(DOWNLOAD_URL);
		if (!intent.hasExtra(TRACK_KEY)) {
			Logger.e(TAG, "No track_key");
			return;
		}

		MediaItem mCurrentMediaItem = (MediaItem) intent
				.getSerializableExtra(TRACK_KEY);
		if (intent.getBooleanExtra("is_cache_file", false)) {
			mediaFolder = Utils.createDefaultCacheDir(getApplicationContext());
		} else {
			mediaFolder = fileUtils.getStoragePath(mCurrentMediaItem
					.getMediaContentType());
		}

		downloadVector.add(new DownloadObject(mCurrentMediaItem,
				responseDownloadUrl));
		if (downloader == null) {

			downloader = new DownloadFile(downloadVector.get(0), this);
			ThreadPoolManager.getInstance().submit(downloader);
		}
		// if (handleTrackDownload(mCurrentMediaItem)) {
		// String successMsg =
		// getResources().getString(R.string.download_media_succeded_toast,
		// hungamaFolder);
		// mHandler.post(new DisplayToast(successMsg));
		// // Toast.makeText(getApplicationContext(),
		// // getResources().getString(R.string.download_media_succeded_toast),
		// // Toast.LENGTH_LONG).show();
		//
		// Set<String> tags = Utils.getTags();
		// if (!tags.contains("download_done")) {
		// tags.add("download_done");
		// Utils.AddTag(tags);
		// }
		// // Flurry report: download complete event
		// FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		// FlurryAgent.logEvent(FlurryConstants.FlurryDownloadPlansParams.DownloadCompleteEvent.toString());
		// FlurryAgent.onEndSession(this);
		//
		// // download Event
		// DeviceConfigurations config = new
		// DeviceConfigurations(getApplicationContext());
		// String mEventStartTimestamp = config.getTimeStampDelta();
		//
		// DataManager mDataManager =
		// DataManager.getInstance(getApplicationContext());
		// int consumerId =
		// mDataManager.getApplicationConfigurations().getConsumerID();
		// String deviceId =
		// mDataManager.getApplicationConfigurations().getDeviceID();
		//
		// PlayEvent playEvent = new PlayEvent(consumerId,
		// deviceId,
		// 0,
		// false,
		// 0,
		// mEventStartTimestamp,
		// 0,
		// 0,
		// mCurrentMediaItem.getId(),
		// MediaContentType.getMediaKind(mCurrentMediaItem.getMediaContentType()),
		// PlayingSourceType.DOWNLOAD,
		// 0,
		// 0);
		//
		// mDataManager.addEvent(playEvent);
		//
		// } else {
		// mHandler.post(new
		// DisplayToast(getResources().getString(R.string.download_media_unsucceded_toast)));
		// // Toast.makeText(getApplicationContext(),
		// //
		// getResources().getString(R.string.download_media_unsucceded_toast),
		// // Toast.LENGTH_LONG).show();
		// }
	}

	@Override
	public void onDestroy() {
//		System.gc();
		super.onDestroy();
		try {
			if (inpogressProcess.size() > 0) {
				for (Integer id : inpogressProcess) {
					notifyManager.cancel(id);
				}
			}
		} catch (Exception e) {
		}
		Logger.i(TAG, "Stop");
	}

	// Handler handle = new Handler();

	Vector<Integer> inpogressProcess = new Vector<Integer>();
	NotificationManager notifyManager;

	private class DownloadFile implements Runnable {
		Notification notification;
		Integer notificationCode;
		DownloadObject obj;
		private long mediaId;
		private String mediaTitle;

		DownloadCompleteListner listner;

		DownloadFile(DownloadObject obj, DownloadCompleteListner listner) {

			this.listner = listner;
			this.obj = obj;
			if (obj.mCurrentMediaItem != null) {
				if (obj.mCurrentMediaItem.getId() > -1) {
					mediaId = obj.mCurrentMediaItem.getId();
				}
				if (obj.mCurrentMediaItem.getTitle() != null) {
					mediaTitle = obj.mCurrentMediaItem.getTitle();
				}
			}

			notificationCode = new Random().nextInt(36000);
			notification = new Notification();
			notification.icon = R.drawable.ic_downloads;
			notification.tickerText = "Downloading.. " + mediaTitle;
			notification.when = System.currentTimeMillis();
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			notification.contentView = new RemoteViews(getPackageName(),
					R.layout.notification_layout_progress);
			notification.contentView.setProgressBar(R.id.pbar_progress, 100, 2,
					false);
			notification.contentView.setTextViewText(R.id.txt_progress, ""
					+ mediaTitle);
			notification.contentView.setTextViewText(R.id.txt_sub_progress,
					"0%");
			notification.contentIntent = PendingIntent.getActivity(
					getBaseContext(), notificationCode, new Intent(),
					PendingIntent.FLAG_CANCEL_CURRENT);
			notifyManager.notify(notificationCode, notification);
			inpogressProcess.add(notificationCode);
		}

		protected void onPostExecute(Boolean result) {
			try {
				inpogressProcess.remove(notificationCode);
				notifyManager.cancel(notificationCode);

				notification = new Notification();
				notification.icon = R.drawable.ic_downloads;
				notification.tickerText = "Download completed";
				notification.when = System.currentTimeMillis();
				notification.flags = Notification.FLAG_AUTO_CANCEL;
				notification.contentView = new RemoteViews(getPackageName(),
						R.layout.notification_layout_progress);
				notification.contentView.setViewVisibility(R.id.pbar_progress,
						View.GONE);
				notification.contentView.setTextViewText(R.id.txt_progress, ""
						+ mediaTitle);
				notification.contentView.setViewVisibility(R.id.txt_progress2,
						View.VISIBLE);
				notification.contentView.setTextViewText(R.id.txt_sub_progress,
						"");

				if (result) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					if (outputFile.getName().toLowerCase().endsWith(".mp3"))
						intent.setDataAndType(Uri.fromFile(outputFile),
								"audio/*");
					else
						intent.setDataAndType(Uri.fromFile(outputFile),
								"video/*");

					notification.contentIntent = PendingIntent.getActivity(
							DownloadFileService2.this, notificationCode,
							intent, PendingIntent.FLAG_UPDATE_CURRENT);
					notifyManager.notify(notificationCode, notification);

					Set<String> tags = Utils.getTags();
					if (!tags.contains("download_done")) {
						tags.add("download_done");
						Utils.AddTag(tags);
					}

					if (!tags.contains("download_Working")) {
						tags.add("download_Working");
						Utils.AddTag(tags);
					}

					// FlurryAgent
					// .setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
					// // Flurry report: download complete event
					// FlurryAgent.onStartSession(DownloadFileService2.this,
					// getString(R.string.flurry_app_key));
					Analytics.startSession(DownloadFileService2.this);
					Analytics
							.logEvent(FlurryConstants.FlurryDownloadPlansParams.DownloadCompleteEvent
									.toString());
					Analytics.onEndSession(DownloadFileService2.this);

					// download Event
					DeviceConfigurations config = DeviceConfigurations
							.getInstance(getApplicationContext());
					String mEventStartTimestamp = config.getTimeStampDelta();

					DataManager mDataManager = DataManager
							.getInstance(getApplicationContext());
					int consumerId = mDataManager
							.getApplicationConfigurations().getConsumerID();
					String deviceId = mDataManager
							.getApplicationConfigurations().getDeviceID();

					PlayEvent playEvent = new PlayEvent(consumerId, deviceId,
							0, false, 0, mEventStartTimestamp, 0, 0,
							obj.mCurrentMediaItem.getId(),
							MediaContentType.getMediaKind(obj.mCurrentMediaItem
									.getMediaContentType()),
							PlayingSourceType.DOWNLOAD, 0, 0);
					Looper.prepare();
					try {
						mDataManager.addEvent(playEvent);
					} catch (Exception e) {
					}
					String successMsg = getResources().getString(
							R.string.download_media_succeded_toast,
							mediaFolder.getName());
					mHandler.post(new DisplayToast(successMsg));

					try {
						// new SingleMediaScanner(this, outputFile);
						MediaScannerConnection
								.scanFile(
										getApplicationContext(),
										new String[] { outputFile
												.getAbsolutePath() },
										null,
										new MediaScannerConnection.OnScanCompletedListener() {
											@Override
											public void onScanCompleted(
													String path, Uri uri) {
												Logger.s("Scanning complete:::::::::::::::"
														+ path);
											}
										});
					} catch (Exception e) {
						Logger.s("Scanning exception::::::::::::::: "
								+ e);
						Logger.printStackTrace(e);
					}
				} else {
					notification.contentIntent = PendingIntent.getActivity(
							getBaseContext(), notificationCode, new Intent(),
							PendingIntent.FLAG_CANCEL_CURRENT);
					notification.contentView
							.setTextViewText(
									R.id.txt_progress2,
									getString(R.string.download_notification_download_failed));
					notifyManager.notify(notificationCode, notification);
					mHandler.post(new DisplayToast(getResources().getString(
							R.string.download_media_unsucceded_toast)));
				}
			} catch (Exception e) {
				Logger.printStackTrace(e);
				if (!result) {
					try {
						notification.contentView
								.setTextViewText(
										R.id.txt_progress2,
										getString(R.string.download_notification_download_failed));
						notifyManager.notify(notificationCode, notification);
						mHandler.post(new DisplayToast(
								getResources()
										.getString(
												R.string.download_media_unsucceded_toast)));
						notification.contentIntent = PendingIntent
								.getActivity(getBaseContext(),
										notificationCode, new Intent(),
										PendingIntent.FLAG_CANCEL_CURRENT);
					} catch (Exception ee) {
						Logger.printStackTrace(ee);
					}
				}
			}
			downloadVector.remove(obj);
			// downloader = null;
			listner.downloadloadCompleted();
		}

		long lastProgress = 0;
		private long contentLength;

		protected void publishProgress(Integer progress) {
			lastProgress = progress;
			int per = (int) (progress * 100 / contentLength);
			notification.contentView.setProgressBar(R.id.pbar_progress, 100,
					per, false);
			notification.contentView.setTextViewText(R.id.txt_sub_progress,
					(per) + "%");
			notifyManager.notify(notificationCode, notification);
		}

		File outputFile;

		protected void doInBackground() {
			boolean downloadComplete = false;
			int trial = 10;
			try {
				do {
					try {
						trial--;
						String fileName;
						if (obj.mCurrentMediaItem.getMediaContentType() == MediaContentType.VIDEO) {
							fileName = mediaTitle + "_"
									+ String.valueOf(mediaId) + ".mp4";
						} else {
							fileName = mediaTitle + "_"
									+ String.valueOf(mediaId) + ".mp3";
						}
						String encodedFileName = HungamaApplication.encodeURL(
								fileName, "UTF-8");
						if (!mediaFolder.exists())
							mediaFolder.mkdirs();

						outputFile = new File(mediaFolder, encodedFileName);
						if (!outputFile.exists())
							outputFile.createNewFile();

						OutputStream output = new BufferedOutputStream(
								new FileOutputStream(outputFile, true));
						byte data[] = new byte[1024 * 10];
						long total = 0;
						lastProgress = total;
						int count;

						// commented for supporting Android M
//						HttpClient client = new DefaultHttpClient();
//						HttpGet get = new HttpGet(obj.responseDownloadUrl);
//						client.getConnectionManager().closeExpiredConnections();
//						client.getParams().setParameter("http.socket.timeout",
//								20000);
//
//						HttpEntity entity = client.execute(get).getEntity();
//						InputStream is = new BufferedInputStream(
//								entity.getContent());
//						contentLength = entity.getContentLength();
//						long thresold = (contentLength / 100) * 2;
//						BufferedInputStream input = new BufferedInputStream(is);

						OkHttpClient client = CommunicationManager.getUnsafeOkHttpClient();
//						Request.Builder requestBuilder = new Request.Builder();
						URL url = new URL(obj.responseDownloadUrl);
//						requestBuilder.url(url);
						Request.Builder requestBuilder = CommunicationManager.getRequestBuilder(DownloadFileService2.this, url);
						client.setConnectTimeout(CommunicationManager.getConnectionTimeout(DownloadFileService2.this), TimeUnit.MILLISECONDS);
						client.setReadTimeout(CommunicationManager.getConnectionTimeout(DownloadFileService2.this), TimeUnit.MILLISECONDS);
						Response responseOk = client.newCall(requestBuilder.build()).execute();
						ResponseBody body = responseOk.body();
						contentLength = body.contentLength();
						long thresold = (contentLength / 100) * 2;
						InputStream input = body.byteStream();

						while ((count = input.read(data)) != -1) {
							total += count;
							if (total - lastProgress > thresold)
								publishProgress((int) total);
							output.write(data, 0, count);
						}
						output.flush();
						output.close();
						input.close();
						downloadComplete = true;
					} catch (Exception e) {
						e.printStackTrace();
					}
				} while (!downloadComplete && trial > 0);
			} catch (Exception e) {

			}
			onPostExecute(downloadComplete);
		}

		@Override
		public void run() {
			doInBackground();
		}
	}

	private class DisplayToast implements Runnable {

		String mText;

		public DisplayToast(String text) {
			this.mText = text;
		}

		@Override
		public void run() {

			Toast toast = new Toast(getApplicationContext());
			toast = Toast.makeText(getApplicationContext(), mText,
					Toast.LENGTH_LONG);

			ToastExpander.showFor(toast, 6000);
		}
	}

	@Override
	public void downloadloadCompleted() {
		if (downloadVector.size() > 0) {
			downloader = new DownloadFile(downloadVector.get(0), this);
			ThreadPoolManager.getInstance().submit(	downloader);
		} else
			downloader = null;
	}
}
