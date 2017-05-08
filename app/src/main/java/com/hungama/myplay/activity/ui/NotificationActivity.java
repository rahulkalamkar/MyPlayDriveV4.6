package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.hungama.myplay.activity.util.Logger;

import java.util.List;

public class NotificationActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//		List<ActivityManager.RunningTaskInfo> procInfos = activityManager
//				.getRunningTasks(1);
		boolean running = false;
		if(HomeActivity.Instance != null || GoOfflineActivity.Instance != null){
			running = true;
		}
//		for (int i = 0; i < procInfos.size(); i++) {
//			ComponentName componentInfo = procInfos.get(0).topActivity;
//			if (componentInfo.getPackageName().equals(
//					"com.hungama.myplay.activity")) {
//				running = true;
//				break;
//			}
//		}

		Logger.s(" ::::::::::::: NotificationActivity :::::::::::: " + running);
		if(!running) {
			startActivity(new Intent(this, OnApplicationStartsActivity.class));
		}

		// Now finish, which will drop the user in to the activity that was at
		// the top
		// of the task stack
		finish();
	}
}
