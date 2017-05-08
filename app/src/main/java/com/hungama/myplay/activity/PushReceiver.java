/*
Copyright 2009-2014 Urban Airship Inc. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE URBAN AIRSHIP INC ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
EVENT SHALL URBAN AIRSHIP INC OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.hungama.myplay.activity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hungama.myplay.activity.ui.AlertActivity;
import com.hungama.myplay.activity.util.Logger;
import com.urbanairship.push.BaseIntentReceiver;
import com.urbanairship.push.PushMessage;

import java.util.List;

/**
 * Broadcast receiver to handle all push notifications
 *
 */
public class PushReceiver extends BaseIntentReceiver {

	private static final String TAG = "IntentReceiver";

	@Override
	protected void onChannelRegistrationSucceeded(Context context,
			String channelId) {
		Logger.i(TAG, "Channel registration updated. Channel Id:" + channelId
				+ ".");
	}

	@Override
	protected void onChannelRegistrationFailed(Context context) {
		Logger.i(TAG, "Channel registration failed.");
	}

	@Override
	protected void onPushReceived(Context context, PushMessage message,
			int notificationId) {
		Logger.i(TAG, "Received push notification. Alert: " + message.getAlert()
				+ ". Notification ID: " + notificationId);

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
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
		Bundle bundle = message.getPushBundle();

		if (running) {
			Intent lLaunch = new Intent(Intent.ACTION_MAIN);
			lLaunch.setClass(context, AlertActivity.class);
			lLaunch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			lLaunch.putExtras(message.getPushBundle());
			//lLaunch.putExtra("alert", message.getAlert());
			Logger.i(
					"App Running status ::::: ", "" + running);
			lLaunch.putExtra("isAppOpen", running);
			if (bundle.containsKey("big_picture")){
				if(bundle.containsKey("alt_text") && !bundle.getString("alt_text").toString().isEmpty()){
					lLaunch.putExtra("alert", bundle.getString("alt_text"));
				}else{
					lLaunch.putExtra("alert", message.getAlert());
				}
			}else{
				lLaunch.putExtra("alert", message.getAlert());
			}
			if (running)
				context.getApplicationContext().startActivity(lLaunch);
			else
				context.startActivity(lLaunch); // xtpl

			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(notificationId);
		}
	}

	@Override
	protected void onBackgroundPushReceived(Context context, PushMessage message) {
		Logger.i(TAG, "Received background push message: " + message);
	}

	@Override
	protected boolean onNotificationOpened(Context context,
			PushMessage message, int notificationId) {
		Logger.i(TAG, "User clicked notification. Alert: " + message.getAlert());

		// Intent messageIntent = new Intent(context, MainActivity.class);
		//
		// String messageId = message.getRichPushMessageId();
		// if (messageId != null && !messageId.isEmpty()) {
		// Logger.debug("Notified of a notification opened with ID " +
		// messageId);
		//
		// // Launch the main activity to the message in the inbox
		// messageIntent.putExtra(MainActivity.EXTRA_MESSAGE_ID, messageId);
		// messageIntent.putExtra(MainActivity.EXTRA_NAVIGATE_ITEM,
		// MainActivity.INBOX_ITEM);
		// }
		//
		// messageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
		// Intent.FLAG_ACTIVITY_SINGLE_TOP);
		// context.startActivity(messageIntent);

		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
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
		// Intent lLaunch = new Intent(Intent.ACTION_MAIN);
		// lLaunch.setClass(context,
		// AlertActivity.class);
		// if(running){

		Intent lLaunch = new Intent(context, AlertActivity.class);
		lLaunch.addFlags(/* Intent.FLAG_ACTIVITY_CLEAR_TOP | */Intent.FLAG_ACTIVITY_NEW_TASK);
		lLaunch.putExtras(message.getPushBundle());
		Logger.i("App Running status ::::: ",
				"" + running);
		lLaunch.putExtra("isAppOpen", running);
		lLaunch.putExtra("alert", message.getAlert());
		context.startActivity(lLaunch);





		// lLaunch.putExtra(ACTIVITY_NAME_KEY,
		// aIntent.getStringExtra(ACTIVITY_NAME_KEY));


		// } else{//LoadingDialogFragment
		// Intent lLaunch = new Intent(context, IntentReceiver.class);
		// lLaunch.setAction(PushManager.ACTION_NOTIFICATION_OPENED);
		// lLaunch.putExtras(message.getPushBundle());
		// context.sendBroadcast(lLaunch);
		// Intent lLaunch = new Intent(context,
		// AlertActivity.class);
		// lLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// lLaunch.putExtras(message.getPushBundle());
		// lLaunch.putExtra("alert", message.getAlert());
		// com.hungama.myplay.activity.util.Logger.i(
		// "App Running status ::::: ", "" + running);
		// // lLaunch.putExtra(ACTIVITY_NAME_KEY,
		// // aIntent.getStringExtra(ACTIVITY_NAME_KEY));
		// lLaunch.putExtra("isAppOpen", running);
		// // context.startActivity(lLaunch);
		// int mPendingIntentId = 123456;
		// PendingIntent mPendingIntent = PendingIntent.getActivity(context,
		// mPendingIntentId, lLaunch,
		// PendingIntent.FLAG_CANCEL_CURRENT);
		// AlarmManager mgr = (AlarmManager) context
		// .getSystemService(Context.ALARM_SERVICE);
		// mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
		// mPendingIntent);
		// }

		return true;
	}

	@Override
	protected boolean onNotificationActionOpened(Context context,
			PushMessage message, int notificationId, String buttonId,
			boolean isForeground) {
		Logger.i(TAG,
				"User clicked notification action button. Alert: "
						+ message.getAlert());
		return false;
	}

	@Override
	protected void onNotificationDismissed(Context context,
			PushMessage message, int notificationId) {
		Logger.i(TAG, "Notification dismissed. Alert: " + message.getAlert()
				+ ". Notification ID: " + notificationId);
	}
}
