/**
 * 
 */
package com.hungama.myplay.activity.gcm;

/**
 * @author stas
 *
 */

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.hungama.myplay.activity.data.persistance.DatabaseManager;
import com.hungama.myplay.activity.ui.AlertActivity;
import com.hungama.myplay.activity.util.Logger;
import com.urbanairship.UAirship;
import com.urbanairship.push.PushManager;

import java.util.Date;
import java.util.List;
import java.util.Set;

public class IntentReceiver extends BroadcastReceiver {
	// public static final String ACTION_NOTIFICATIONS_UPDATE =
	// "com.hungama.myplay.activity.gcm.UPDATE";
	public static final String ACTIVITY_NAME_KEY = "activity";
	// public static final String IS_APP_RUNNING = "is_app_running";

	public static final String EXTRA_MESSAGE_ID_KEY = "_uamid";
	public static String alert;
	// public static final String ALERT_MARK = "from_alert";
	public static final String ARTIST_ID = "artist_id";
	public static final String CODE = "code";
	public static final String CHANNEL_INDEX = "channel_index";
	public static final String CATEGORY = "Category";
	public static final String CONTENT_ID = "content_id";
	public static final String STATION_ID = "Station_ID";
	public static final String CONTENT_TYPE = "content_type";// "ContentType";
	public static final String SUBSCRIPTION_PLANS = "Subscription_Plans";
	// private String StringClassname, message, code, extraName;
	// private Context mc;
	public static boolean running;

	public static final String EXTRA_ALERT = "com.urbanairship.push.ALERT";
	public static final String EXTRA_STRING_EXTRA = "com.urbanairship.push.STRING_EXTRA";

	// private static boolean isMessage;

	@Override
	public void onReceive(Context aContext, Intent aIntent) {
		Logger.e("onReceive--->1",
				aIntent.getAction() + "::" + aIntent.getExtras());
		try {
			// mc = aContext;
			String action = aIntent.getAction();
			ActivityManager activityManager = (ActivityManager) aContext
					.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> procInfos = activityManager
					.getRunningTasks(1);
			running = false;
			for (int i = 0; i < procInfos.size(); i++) {
				ComponentName componentInfo = procInfos.get(0).topActivity;
				if (componentInfo.getPackageName().equals(
						"com.hungama.myplay.activity")) {
					running = true;
					break;
				}
			}

			Logger.e("onReceive--->2",
					aIntent.getAction() + "::" + aIntent.getExtras());
			if (action.equals(PushManager.ACTION_NOTIFICATION_OPENED)) {
				// user opened the notification so we launch the application
				com.hungama.myplay.activity.util.Logger.i("Message!!!",
						"Opened");

				// xtpl
				// // This intent is; what will be used to launch the activity
				// in
				// our application
				// Intent lLaunch = new Intent(Intent.ACTION_MAIN);
				//
				// // Main.class can be substituted any activity in your android
				// project that you wish
				// // to be launched when the user selects the notification from
				// the
				// Notifications drop down
				// lLaunch.setClass(UAirship.shared().getApplicationContext(),
				// AlertActivity.class);
				// lLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//
				// // copy the intent data from the incoming intent to the
				// intent
				// // that we are going to launch
				// lLaunch.putExtras(aIntent);
				// lLaunch.putExtra("alert", alert);
				// ActivityManager activityManager = (ActivityManager)
				// aContext.getSystemService(aContext.ACTIVITY_SERVICE );
				// List<RunningTaskInfo> procInfos =
				// activityManager.getRunningTasks(1);
				// running = false;
				// for(int i = 0; i < procInfos.size(); i++){
				// ComponentName componentInfo = procInfos.get(0).topActivity;
				// if(componentInfo.getPackageName().equals("com.hungama.myplay.activity")){
				// running = true;
				// break;
				// }
				// }
				// lLaunch.putExtra(ACTIVITY_NAME_KEY,
				// aIntent.getStringExtra(ACTIVITY_NAME_KEY));
				// //
				// UAirship.shared().getApplicationContext().startActivity(lLaunch);
				// xtpl

				Set<String> keyset = aIntent.getExtras().keySet();
				for (String key : keyset) {
					com.hungama.myplay.activity.util.Logger.i("Key", key);
				}
				// Intent update = new Intent();
				// update.setAction(ACTION_NOTIFICATIONS_UPDATE);
				// aContext.sendBroadcast(update);

				DatabaseManager dbM = DatabaseManager.getInstance(aContext);
				long l = dbM.addNotification(
						aIntent.getStringExtra(EXTRA_ALERT),
						aIntent.getStringExtra(CODE) == null ? -1 : Integer
								.parseInt(aIntent.getStringExtra(CODE)),
						aIntent.getStringExtra(CATEGORY),
						aIntent.getStringExtra(CONTENT_ID),
						aIntent.getStringExtra(CONTENT_TYPE) == null ? -1
								: Integer.parseInt(aIntent
										.getStringExtra(CONTENT_TYPE)), aIntent
								.getStringExtra(CHANNEL_INDEX), aIntent
								.getStringExtra(ARTIST_ID), null, aIntent
								.getStringExtra(EXTRA_MESSAGE_ID_KEY),
						new Date());
				dbM.close();
				com.hungama.myplay.activity.util.Logger.v("Notification",
						"Database long key ::" + l);
				int id = aIntent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID,
						0);
				alert = aIntent.getStringExtra(EXTRA_ALERT);
				String messageId = aIntent.getStringExtra(EXTRA_MESSAGE_ID_KEY);
				com.hungama.myplay.activity.util.Logger.v("Notification",
						"Notified of a notification opened with id "
								+ messageId);
				com.hungama.myplay.activity.util.Logger.v(
						"Notification",
						"Received push notification. Alert: "
								+ aIntent.getStringExtra(EXTRA_ALERT)
								+ ". Payload: " + ". Payload: "
								+ aIntent.getStringExtra(EXTRA_STRING_EXTRA)
								+ ". NotificationID=" + id + ". MessageID="
								+ messageId + ". code: "
								+ aIntent.getStringExtra(CODE) + ", artist_id:"
								+ aIntent.getStringExtra(ARTIST_ID)
								+ ", channel_id:"
								+ aIntent.getStringExtra(CHANNEL_INDEX));

				// This intent is; what will be used to launch the activity in
				// our
				// application
				Intent lLaunch = new Intent(Intent.ACTION_MAIN);
				// Main.class can be substituted any activity in your android
				// project that you wish
				// to be launched when the user selects the notification from
				// the
				// Notifications drop down
				lLaunch.setClass(UAirship.shared().getApplicationContext(),
						AlertActivity.class);
				lLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK/*
															 * | Intent.
															 * FLAG_ACTIVITY_CLEAR_TOP
															 */);
				// copy the intent data from the incoming intent to the intent
				// that we are going to launch
				lLaunch.putExtras(aIntent);
				lLaunch.putExtra("alert", alert);
				// ActivityManager activityManager = (ActivityManager)
				// aContext.getSystemService(Context.ACTIVITY_SERVICE);
				// List<RunningTaskInfo> procInfos =
				// activityManager.getRunningTasks(1);
				// running = false;
				// for (int i = 0; i < procInfos.size(); i++)
				// {
				// ComponentName componentInfo = procInfos.get(0).topActivity;
				// if
				// (componentInfo.getPackageName().equals("com.hungama.myplay.activity"))
				// {
				// running = true;
				// break;
				// }
				// }
				com.hungama.myplay.activity.util.Logger.i(
						"App Running status ::::: ", "" + running);
				lLaunch.putExtra(ACTIVITY_NAME_KEY,
						aIntent.getStringExtra(ACTIVITY_NAME_KEY));
				lLaunch.putExtra("isAppOpen", running);
				UAirship.shared().getApplicationContext()
						.startActivity(lLaunch); // xtpl
			} else if (action.equals(PushManager.ACTION_PUSH_RECEIVED)
					|| action.equals("com.app.hungamaPush")) {
				Logger.e("onReceive--->3", "1111111111>>>>>>>>>>>>>>");
				// push notification received, perhaps store it in a db
				com.hungama.myplay.activity.util.Logger.i("Message!!!",
						"Received");

				int id = aIntent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID,
						0);

				com.hungama.myplay.activity.util.Logger.i(
						"Hungama",
						">>>>>>Received push notification. Alert: "
								+ aIntent.getStringExtra(EXTRA_ALERT)
								+ " [NotificationID=" + id + "]");
				Logger.e("onReceive--->4", "1111111111>>>>>>>>>>>>>>");
				if (running || action.equals("com.app.hungamaPush")) {
					Set<String> keyset = aIntent.getExtras().keySet();
					for (String key : keyset) {
						com.hungama.myplay.activity.util.Logger.i("Key", key);
					}

					DatabaseManager dbM = DatabaseManager.getInstance(aContext);
					long l = dbM.addNotification(
							aIntent.getStringExtra(EXTRA_ALERT),
							aIntent.getStringExtra(CODE) == null ? -1 : Integer
									.parseInt(aIntent.getStringExtra(CODE)),
							aIntent.getStringExtra(CATEGORY),
							aIntent.getStringExtra(CONTENT_ID),
							aIntent.getStringExtra(CONTENT_TYPE) == null ? -1
									: Integer.parseInt(aIntent
											.getStringExtra(CONTENT_TYPE)),
							aIntent.getStringExtra(CHANNEL_INDEX), aIntent
									.getStringExtra(ARTIST_ID), null, aIntent
									.getStringExtra(EXTRA_MESSAGE_ID_KEY),
							new Date());
					dbM.close();
					com.hungama.myplay.activity.util.Logger.v("Notification",
							"Database long key ::" + l);
					// int id =
					// aIntent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID,
					// 0);
					alert = aIntent.getStringExtra(EXTRA_ALERT);
					String messageId = aIntent
							.getStringExtra(EXTRA_MESSAGE_ID_KEY);
					com.hungama.myplay.activity.util.Logger.v("Notification",
							"Notified of a notification opened with id "
									+ messageId);
					com.hungama.myplay.activity.util.Logger.v(
							"Notification",
							"Received push notification. Alert: "
									+ aIntent.getStringExtra(EXTRA_ALERT)
									+ ". Payload: "
									+ ". Payload: "
									+ aIntent
											.getStringExtra(EXTRA_STRING_EXTRA)
									+ ". NotificationID=" + id + ". MessageID="
									+ messageId + ". code: "
									+ aIntent.getStringExtra(CODE)
									+ ", artist_id:"
									+ aIntent.getStringExtra(ARTIST_ID)
									+ ", channel_id:"
									+ aIntent.getStringExtra(CHANNEL_INDEX));

					// This intent is; what will be used to launch the activity
					// in
					// our
					// application
					Intent lLaunch = new Intent(Intent.ACTION_MAIN);
					// Main.class can be substituted any activity in your
					// android
					// project that you wish
					// to be launched when the user selects the notification
					// from the
					// Notifications drop down
					lLaunch.setClass(UAirship.shared().getApplicationContext(),
							AlertActivity.class);
					lLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK/*
																 * | Intent.
																 * FLAG_ACTIVITY_CLEAR_TOP
																 */);
					// copy the intent data from the incoming intent to the
					// intent
					// that we are going to launch
					lLaunch.putExtras(aIntent);
					lLaunch.putExtra("alert", alert);
					com.hungama.myplay.activity.util.Logger.i(
							"App Running status ::::: ", "" + running);
					lLaunch.putExtra(ACTIVITY_NAME_KEY,
							aIntent.getStringExtra(ACTIVITY_NAME_KEY));

					lLaunch.putExtra("isAppOpen", running);
					if (running)
						aContext.getApplicationContext().startActivity(lLaunch);
					else
						UAirship.shared().getApplicationContext()
								.startActivity(lLaunch); // xtpl

					NotificationManager mNotificationManager = (NotificationManager) UAirship
							.shared().getApplicationContext()
							.getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(id);
				}

				// Set<String> keyset = aIntent.getExtras().keySet();
				// for (String key : keyset) {
				// com.hungama.myplay.activity.util.Logger.i("Key", key);
				// }
				// // Intent update = new Intent();
				// // update.setAction(ACTION_NOTIFICATIONS_UPDATE);
				// // aContext.sendBroadcast(update);
				// DatabaseManager dbM = DatabaseManager.getInstance(aContext);
				// long l = dbM.addNotification(
				// aIntent.getStringExtra(PushManager.EXTRA_ALERT),
				// aIntent.getStringExtra(CODE) == null ? -1 : Integer
				// .parseInt(aIntent.getStringExtra(CODE)),
				// aIntent.getStringExtra(CATEGORY),
				// aIntent.getStringExtra(CONTENT_ID),
				// aIntent.getStringExtra(CONTENT_TYPE) == null ? -1 : Integer
				// .parseInt(aIntent.getStringExtra(CONTENT_TYPE)),
				// aIntent.getStringExtra(CHANNEL_INDEX), aIntent
				// .getStringExtra(ARTIST_ID), null, aIntent
				// .getStringExtra(EXTRA_MESSAGE_ID_KEY), new Date());
				// dbM.close();
				// com.hungama.myplay.activity.util.Logger.v("Notification","Database long key ::"
				// + l);
				// int id =
				// aIntent.getIntExtra(PushManager.EXTRA_NOTIFICATION_ID,
				// 0);
				// alert = aIntent.getStringExtra(PushManager.EXTRA_ALERT);
				// String messageId =
				// aIntent.getStringExtra(EXTRA_MESSAGE_ID_KEY);
				// com.hungama.myplay.activity.util.Logger.v("Notification",
				// "Notified of a notification opened with id " + messageId);
				// com.hungama.myplay.activity.util.Logger
				// .v("Notification",
				// "Received push notification. Alert: "
				// + aIntent
				// .getStringExtra(PushManager.EXTRA_ALERT)
				// + ". Payload: "
				// + ". Payload: "
				// + aIntent
				// .getStringExtra(PushManager.EXTRA_STRING_EXTRA)
				// + ". NotificationID=" + id + ". MessageID="
				// + messageId + ". code: "
				// + aIntent.getStringExtra(CODE)
				// + ", artist_id:"
				// + aIntent.getStringExtra(ARTIST_ID)
				// + ", channel_id:"
				// + aIntent.getStringExtra(CHANNEL_INDEX));
				//
				// // This intent is; what will be used to launch the activity
				// in
				// our
				// // application
				// Intent lLaunch = new Intent(Intent.ACTION_MAIN);
				//
				// // Main.class can be substituted any activity in your android
				// // project that you wish
				// // to be launched when the user selects the notification from
				// the
				// // Notifications drop down
				// lLaunch.setClass(UAirship.shared().getApplicationContext(),
				// AlertActivity.class);
				// lLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				// | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//
				// // copy the intent data from the incoming intent to the
				// intent
				// // that we are going to launch
				// lLaunch.putExtras(aIntent);
				// lLaunch.putExtra("alert", alert);
				// ActivityManager activityManager = (ActivityManager) aContext
				// .getSystemService(aContext.ACTIVITY_SERVICE);
				// List<RunningTaskInfo> procInfos = activityManager
				// .getRunningTasks(1);
				// running = false;
				// for (int i = 0; i < procInfos.size(); i++) {
				// ComponentName componentInfo = procInfos.get(0).topActivity;
				// if (componentInfo.getPackageName().equals(
				// "com.hungama.myplay.activity")) {
				// running = true;
				// break;
				// }
				// }
				// com.hungama.myplay.activity.util.Logger.i(
				// "App Running status ::::: ", "" + running);
				// lLaunch.putExtra(ACTIVITY_NAME_KEY,
				// aIntent.getStringExtra(ACTIVITY_NAME_KEY));
				// // // if(running)
				// UAirship.shared().getApplicationContext().startActivity(lLaunch);
				// //
				// xtpl

				// NotificationManager mNotificationManager =
				// (NotificationManager)
				// UAirship
				// .shared().getApplicationContext()
				// .getSystemService(Context.NOTIFICATION_SERVICE);
				// mNotificationManager.cancel(1010);
				// else{
				// lLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
				// Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				// UAirship.shared().getApplicationContext().startActivity(lLaunch);
				// }
			}
		} catch (Exception e) {
		}
	}
}
