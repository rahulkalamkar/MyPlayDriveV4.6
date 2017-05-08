package com.hungama.myplay.activity.util;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.crashlytics.android.answers.LoginEvent;
import com.flurry.android.FlurryAgent;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.PromoUnit;
import com.hungama.myplay.activity.data.events.AppEvent;

import java.util.HashMap;
import java.util.Map;

public class Analytics {

	private static GoogleAnalytics ga;
	private static Tracker tracker;
//	private static Context context;

    public static final void init(Application appcontext) {
        try {
            if (appcontext != null) {
                FlurryAgent.setLogEnabled(true);
                FlurryAgent.setLogEvents(true);
                FlurryAgent.setLogLevel(Log.VERBOSE);
                FlurryAgent.init(appcontext, appcontext.getString(R.string.flurry_app_key));
                ga = GoogleAnalytics.getInstance(appcontext);
                ga.enableAutoActivityReports(appcontext);
                tracker = ga.newTracker(R.xml.global_tracker);
            }
        } catch (Exception e) {
        } catch (Error e) {
        }
    }

    public static final void startActivitySession(Activity activity) {
        ga.reportActivityStart(activity);
    }

    public static final void stopActivitySession(Activity activity) {
        ga.reportActivityStop(activity);
    }

    public static final void startGASession() {
        if(tracker == null)
            tracker = ga.newTracker(R.xml.global_tracker);
        tracker.send(new HitBuilders.ScreenViewBuilder().setNewSession().build());
        Log.i("Google Analytics::", "starts");
    }

    public static final void startSession(Context context) {
        try {
//			Analytics.context = context;
            FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
            FlurryAgent.onStartSession(context,
                    context.getString(R.string.flurry_app_key));
            if (context instanceof Activity) {
                if (tracker == null) {
                    init(((Activity) context).getApplication());
                }
                startActivitySession((Activity) context);
                tracker.setScreenName(context.getClass().getName());
                tracker.send(new HitBuilders.ScreenViewBuilder().build());
                Logger.e("GA", "startSession " + context.getClass().getName());
            }
           Crashlytics.log("screen: " + context.getClass().getName());
            Crashlytics.setString("last_screen", context.getClass().getName());
        } catch (Exception e) {
        }
    }

    public static final void startSession(Context context, Fragment fragment) {
        try {
//			Analytics.context = context;
            FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
            FlurryAgent.onStartSession(context,
                    context.getString(R.string.flurry_app_key));
            if (context instanceof Activity) {
                if (tracker == null) {
                    init(((Activity) context).getApplication());
                }
                startActivitySession((Activity) context);
                if(fragment!=null) {
//                    tracker.setScreenName(fragment.getClass().getName());
//                    tracker.send(new HitBuilders.ScreenViewBuilder().build());
                    Logger.e("GA", "startSession " + fragment.getClass().getSimpleName());
                } else {
                    tracker.setScreenName(context.getClass().getName());
                    tracker.send(new HitBuilders.ScreenViewBuilder().build());
                    Logger.e("GA", "startSession " + context.getClass().getName());
                }
            }
            Crashlytics.log("screen: " + context.getClass().getName());
            Crashlytics.setString("last_screen", context.getClass().getName());
        } catch (Exception e) {
        }
    }

    public static void postCrashlitycsLog(Context context,String value){
        try{
          Crashlytics.setString("last_screen_fragment", value);
           Crashlytics.log("screen_fragment: " + value);
        }catch (Exception e){}
    }

    public static final void onPageView() {
        try {
            FlurryAgent.onPageView();
        } catch (Exception e) {
        }
    }

    public static final void onEndSession(Context context) {
        try {
            FlurryAgent.onEndSession(context);
            if (context instanceof Activity) {
                stopActivitySession((Activity) context);
            }
        } catch (Exception e) {
        }
    }

    public static final void logEvent(String arg0) {
        try {
            FlurryAgent.logEvent(arg0);
            try {
                if (arg0 != null) {
                    tracker.send(new HitBuilders.EventBuilder(arg0, "No Action").build());
					//Bundle parameters = new Bundle();
					//parameters.putString(arg0, "No Action");
					//loggerAppEvents.logEvent(arg0, parameters);
					if(Logger.enableFabricEvents)
						Answers.getInstance().logCustom(new CustomEvent(arg0).putCustomAttribute(arg0, "No Action"));
				}
                Logger.e("GA", "LogEvent" + arg0);
            } catch (Exception e) {
            }
            postCMAppEvent(arg0, null);
        } catch (Exception e) {
        }
    }

    public static final void logEvent(String arg0, Map<String, String> arg1) {
        try {
            FlurryAgent.logEvent(arg0, arg1);
			CustomEvent customEvent= new CustomEvent(arg0);
			//Bundle parameters = new Bundle();
            try {
                if (arg0 != null) {
                    if (arg1 != null) {
                        for (String str : arg1.keySet()) {
                            tracker.send(new HitBuilders.EventBuilder()
                                    .setCategory(arg0).setAction(str)
                                    .setLabel(arg1.get(str)).build());
							//parameters.putString(str, arg1.get(str));
							customEvent.putCustomAttribute(str, arg1.get(str));
                        }
                    }
                }
                Logger.e("GA", "LogEvent" + arg0 + " :" + arg1);
            } catch (Exception e) {
            }
            postCMAppEvent(arg0, arg1);
			//loggerAppEvents.logEvent(arg0, parameters);
			if(Logger.enableFabricEvents)
				Answers.getInstance().logCustom(customEvent);
        } catch (Exception e) {
        }
    }

    public static final void logEvent(String arg0, boolean arg1) {
        try {
            FlurryAgent.logEvent(arg0, arg1);
            Map<String, String> extraData = new HashMap<>();
            extraData.put("status", arg1 ? "true" : "false");
            postCMAppEvent(arg0, extraData);

			CustomEvent customEvent= new CustomEvent(arg0);
			customEvent.putCustomAttribute("status", arg1 ? "true" : "false");
			if(Logger.enableFabricEvents)
				Answers.getInstance().logCustom(customEvent);
        } catch (Exception e) {
        }
    }

    private static final void postCMAppEvent(String event, Map<String, String> extraData) {
//		try{
//			DataManager mDataManager = DataManager.getInstance(context);
//			int consumerId = mDataManager.getApplicationConfigurations()
//					.getConsumerID();
//			String deviceId = mDataManager.getApplicationConfigurations()
//					.getDeviceID();
//			String timeStamp = mDataManager.getDeviceConfigurations()
//					.getTimeStampDelta();
//			AppEvent campaignPlayEvent = new AppEvent(
//					consumerId, deviceId, event, timeStamp, 0, 0, extraData);
//			mDataManager.addEvent(campaignPlayEvent);
//		} catch (Exception e) {
//			Logger.printStackTrace(e);
//		}
    }

	public static void loginEvent(String method, boolean isSuccess){
		if(Logger.enableFabricEvents)
			Answers.getInstance().logLogin(new LoginEvent().putMethod(method)
				.putSuccess(isSuccess));
	}

    public static final void postPromoAppEvent(Activity mActivity, PromoUnit mPromoUnit, String eventType, String section) {
        try{
            DataManager mDataManager = DataManager.getInstance(mActivity);
            int consumerId = mDataManager.getApplicationConfigurations()
                    .getConsumerID();
            String deviceId = mDataManager.getApplicationConfigurations()
                    .getDeviceID();

            String timeStamp = mDataManager.getDeviceConfigurations()
                    .getTimeStampDelta();

            Map<String, Object> extraData = new HashMap<String, Object>();
            extraData.put("action", eventType);
            extraData.put("banner_id", mPromoUnit.getPromo_id());
            extraData.put("banner_name", mPromoUnit.getPromo_name());
            extraData.put("banner_location", section);
            AppEvent campaignPlayEvent = new AppEvent(
                    consumerId, deviceId, "app_action", timeStamp, 0, 0, extraData);
            mDataManager.addEvent(campaignPlayEvent);
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }
}
