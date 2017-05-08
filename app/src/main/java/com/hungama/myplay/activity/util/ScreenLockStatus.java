package com.hungama.myplay.activity.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.AlertActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.PlacementSplashActivity;

public class ScreenLockStatus {

	private static ScreenLockStatus obj;
	private static long lastSplashShownTime = 0;
	Context appContext;

	Activity activityLastFocused;

	private ScreenLockStatus(Context appContext) {
		this.appContext = appContext.getApplicationContext();
	}

	public static ScreenLockStatus getInstance(Context appContext) {
		if (obj == null) {
			obj = new ScreenLockStatus(appContext);
		}
		return obj;
	}

	boolean needtoShowAdLocked;
	static boolean needtoShowAdhomePress;

	Handler handle = new Handler();

	public void onStop() {
		com.hungama.myplay.activity.util.Logger.s("onStop1 ScreenLock");
		handle.postDelayed(new Runnable() {

			@Override
			public void run() {
				needtoShowAdLocked = isScreenLock();
				com.hungama.myplay.activity.util.Logger.s(needtoShowAdLocked
						+ "needtoShowAdLocked ScreenLock");
				if (needtoShowAdLocked)
					lastSplashShownTime = System.currentTimeMillis();
				Logger.s("onStop() :::::::::::: " + lastSplashShownTime);
			}
		}, 500);
	}

	public void onStop(boolean onuserHint, Activity act) {
		this.activityLastFocused = act;
		com.hungama.myplay.activity.util.Logger.s("onStop ScreenLock");

		needtoShowAdhomePress = onuserHint;
		com.hungama.myplay.activity.util.Logger.s(needtoShowAdhomePress
				+ "needtoShowAdhomePress ScreenLock");

		if (needtoShowAdhomePress)
			lastSplashShownTime = System.currentTimeMillis();
		Logger.s("onStop1() :::::::::::: " + lastSplashShownTime);
	}

	public static boolean isHomePressed() {
		return needtoShowAdhomePress;
	}

	public void onResume(final Context context, Activity act) {
		// this.act = act;
		if(activityLastFocused==null){
			Logger.s(" :::::::::::: recent act null ScreenLock");
			if(HungamaApplication.isAppReCreated && !act.getIntent().getBooleanExtra(AlertActivity.ALERT_NOTI, false)){
				Intent i = new Intent(act, HomeActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("finish_restart", true);
				act.startActivity(i);
				act.getIntent().removeExtra(AlertActivity.ALERT_NOTI);
			}
		} else {
			Logger.s(" :::::::::::: recent act not null ScreenLock");
		}

		if (DontShowAd) {
			DontShowAd = false;
			reset();
			return;
		}

		Logger.s((this.activityLastFocused == act) + "act ScreenLock");
		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(appContext);
		if (needtoShowAdLocked) {
			needtoShowAdLocked = appConfig.getAppConfigSplashUnlock();
		}
		if (needtoShowAdhomePress) {
			needtoShowAdhomePress = appConfig.getAppConfigSplashMaximize();
//			ApsalarEvent.postEvent(context, ApsalarEvent.APP_LAUNCH_EVENT);
		}

		if (needtoShowAdLocked || needtoShowAdhomePress) {
			Logger.s(System.currentTimeMillis() + " onResume() :::::::::::: "
					+ lastSplashShownTime);
			if ((System.currentTimeMillis() - lastSplashShownTime) < appConfig
					.getAppConfigSplashAdRefreshLimit() * 1000) {
				reset();
			}
		}

		if (((this.activityLastFocused == act && needtoShowAdhomePress) || needtoShowAdLocked)
				&& HungamaApplication.splashAdDisplyedCount < appConfig
						.getSplashAdCountLimit())
			handle.postDelayed(new Runnable() {

				@Override
				public void run() {
					if ((needtoShowAdhomePress || needtoShowAdLocked)
							&& !CacheManager.isProUser(context)) {
						Intent intent = new Intent(context,
								PlacementSplashActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
						needtoShowAdhomePress = false;
						needtoShowAdLocked = false;
						lastSplashShownTime = System.currentTimeMillis();
					}
				}
			}, 0);
		else {
			reset();
		}
	}

	public void reset() {
		this.activityLastFocused = null;
		needtoShowAdhomePress = false;
		needtoShowAdLocked = false;
	}

	boolean DontShowAd = false;

	public void dontShowAd() {
		DontShowAd = true;
	}

	public boolean isScreenLock() {
		PowerManager powerManager = (PowerManager) appContext
				.getSystemService(Context.POWER_SERVICE);
		return !powerManager.isScreenOn();
	}

}
