/*
 This file is part of Appirater.
 
 Copyright (c) 2010, Arash Payan
 All rights reserved.
 
 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation
 files (the "Software"), to deal in the Software without
 restriction, including without limitation the rights to use,
 copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following
 conditions:
 
 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 OTHER DEALINGS IN THE SOFTWARE.
 */
/*
 * Appirater.java
 * Port of Appirater to Android.
 * 
 * Original created by Arash Payan on 9/5/09.
 * http://arashpayan.com
 * Copyright 2010 Arash Payan. All rights reserved.
 * 
 * Ported by IJsbrand Slob on 3/7/11.
 * http://ijsbrandslob.com
 */

package com.hungama.myplay.activity.util;

// Idan1
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.images.AsyncTask;

import java.util.Date;

public class Appirater {
	/*
	 * Users will need to have the same version of your app installed for this
	 * many days before they will be prompted to rate it.
	 */
	private static final int DAYS_UNTIL_PROMPT = 1000;

	/*
	 * An example of a 'use' would be if the user launched the app. Bringing the
	 * app into the foreground (on devices that support it) would also be
	 * considered a 'use'. You tell Appirater about these events using the two
	 * methods: Appirater.appLaunched(); Appirater.appEnteredForeground();
	 * 
	 * Users need to 'use' the same version of the app this many times before
	 * before they will be prompted to rate it.
	 */
	private static final int USES_UNTIL_PROMPT = 4;

	/*
	 * A significant event can be anything you want to be in your app. In a
	 * telephone app, a significant event might be placing or receiving a call.
	 * In a game, it might be beating a level or a boss. This is just another
	 * layer of filtering that can be used to make sure that only the most loyal
	 * of your users are being prompted to rate you on the app store. If you
	 * leave this at a value of -1, then this won't be a criteria used for
	 * rating. To tell Appirater that the user has performed a significant
	 * event, call the method: Appirater.userDidSignificantEvent();
	 */
	private static final int SIG_EVENTS_UNTIL_PROMPT = 1;

	/*
	 * Once the rating alert is presented to the user, they might select 'Remind
	 * me later'. This value specifies how long (in days) Appirater will wait
	 * before reminding them.
	 */
	private static final int TIME_BEFORE_REMINDING = 2;

	/*
	 * 'true' will show the Appirater alert everytime. Useful for testing how
	 * your message looks and making sure the link to your app's review page
	 * works.
	 */
	private static final boolean DEBUG = false;

	private Context mContext;
	private Date mFirstUseDate;
	private Date mReminderRequestDate;
	private int mUseCount;
	private int mSignificantEventCount;
	private int mCurrentVersion;
	private boolean mRatedCurrentVersion;
	private boolean mDeclinedToRate;

	public Appirater(Context context) {
		mContext = context;
		loadSettings();
	}

	/*
	 * Tells Appirater that the app has launched. You should call this method at
	 * the end of your application main activity delegate's Activity.onStart()
	 * method.
	 * 
	 * If the app has been used enough to be rated (and enough significant
	 * events), you can suppress the rating alert by passing false for
	 * canPromptForRating. The rating alert will simply be postponed until it is
	 * called again with true for canPromptForRating. The rating alert can also
	 * be triggered by appEnteredForeground() and userDidSignificantEvent() (as
	 * long as you pass true for canPromptForRating in those methods).
	 */
	public void appLaunched(boolean canPromptForRating) {

		InternetCheck internetCheck = new InternetCheck(canPromptForRating);
		internetCheck.execute();
		// incrementAndRate( canPromptForRating );
	}

	/*
	 * Tells Appirater that the app was brought to the foreground on
	 * multitasking devices. You should call this method from the application
	 * delegate's Activity.onResume() method.
	 * 
	 * If the app has been used enough to be rated (and enough significant
	 * events), you can suppress the rating alert by passing false for
	 * canPromptForRating. The rating alert will simply be postponed until it is
	 * called again with true for canPromptForRating. The rating alert can also
	 * be triggered by appLaunched() and userDidSignificantEvent() (as long as
	 * you pass true for canPromptForRating in those methods).
	 */
	// public void appEnteredForeground(boolean canPromptForRating) {
	//
	// InternetCheck internetCheck = new InternetCheck(canPromptForRating);
	// internetCheck.execute();
	// // incrementAndRate( canPromptForRating );
	// }

	/*
	 * Tells Appirater that the user performed a significant event. A
	 * significant event is whatever you want it to be. If you're app is used to
	 * make VoIP calls, then you might want to call this method whenever the
	 * user places a call. If it's a game, you might want to call this whenever
	 * the user beats a level boss.
	 * 
	 * If the user has performed enough significant events and used the app
	 * enough, you can suppress the rating alert by passing false for
	 * canPromptForRating. The rating alert will simply be postponed until it is
	 * called again with true for canPromptForRating. The rating alert can also
	 * be triggered by appLaunched() and appEnteredForeground() (as long as you
	 * pass true for canPromptForRating in those methods).
	 */
	public void userDidSignificantEvent(boolean canPromptForRating) {
		incrementSignificantEventAndRate(canPromptForRating);
	}

	// private void incrementAndRate( boolean canPromptForRating ) {
	// incrementUseCount();
	// if (canPromptForRating && ratingConditionsHaveBeenMet()
	// && connectedToNetwork()) {
	// showRatingAlert();
	// }
	// }

	private void incrementSignificantEventAndRate(boolean canPromptForRating) {
		incrementSignificantEventCount();
		if (canPromptForRating && ratingConditionsHaveBeenMet()
				&& connectedToNetwork()) {
			showRatingAlert();
		}
	}

	private class InternetCheck extends AsyncTask<Void, Void, Void> {

		private boolean canPromptForRating;
		private boolean success;

		public InternetCheck(boolean _canPromptForRating) {

			canPromptForRating = _canPromptForRating;
		}

		@Override
		protected Void doInBackground(Void... params) {

			success = connectedToNetwork();

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			incrementUseCount();
			if (canPromptForRating && ratingConditionsHaveBeenMet() && success) {

				showRatingAlert();
			}
		}
	}

	private boolean connectedToNetwork() {
		// try {
		// HttpClient httpclient = new DefaultHttpClient();
		// HttpGet request = new HttpGet( "http://www.google.com/" );
		// HttpResponse result = httpclient.execute( request );
		// int statusCode = result.getStatusLine().getStatusCode();
		// if( statusCode < 400 ) {
		// return true;
		// }
		// } catch( Exception ex ) {
		// Logger.e("ERROR: ", ex.getMessage());
		// }
		ConnectivityManager cm = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		} else if (netInfo != null && !netInfo.isConnectedOrConnecting()) {
			Logger.e("ERROR: ", netInfo.getReason());
		} else if (netInfo == null) {
			Logger.e("ERROR: ", "No available network");
		}
		return false;
	}

	private void showRatingAlert() {
		try {
			final Dialog rateDialog = new Dialog(mContext);

			rateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			final Resources res = mContext.getResources();

			CharSequence appname = "unknown";
			try {
				appname = mContext.getPackageManager().getApplicationLabel(
						mContext.getPackageManager().getApplicationInfo(
								mContext.getPackageName(), 0));
			} catch (NameNotFoundException ex) {
			}
			/*
			 * rateDialog.setTitle(Utils.getMultilanguageTextLayOut(mContext,
			 * res.getString(R.string.APPIRATER_RATE_BUTTON_NEW)) + " " +
			 * Utils.getMultilanguageTextLayOut(mContext,
			 * res.getString(R.string.application_name)));
			 */
			ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
					.getInstance(mContext);
			// LinearLayout root = (LinearLayout) LayoutInflater.from(mContext)
			// .inflate(R.layout.dialog_appirater, null);
			rateDialog.setContentView(R.layout.dialog_appirater_new);
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rateDialog.getWindow().getDecorView(),
						mContext);
			}
			rateDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
			LanguageTextView title = (LanguageTextView) rateDialog
					.findViewById(R.id.appirater_title);
			title.setText(Utils.getMultilanguageTextLayOut(mContext,
					res.getString(R.string.APPIRATER_RATE_BUTTON_NEW))
					+ " "
					+ Utils.getMultilanguageTextLayOut(mContext,
							res.getString(R.string.application_name)));
			LanguageTextView messageArea = (LanguageTextView) rateDialog
					.findViewById(R.id.appirater_message_area);
			messageArea.setText(Utils.getMultilanguageTextLayOut(mContext,
					res.getString(R.string.APPIRATER_MESSAGE)));

			LanguageButton rateButton = (LanguageButton) rateDialog
					.findViewById(R.id.appirater_rate_button);
			rateButton.setText(Utils.getMultilanguageTextLayOut(mContext,
					res.getString(R.string.APPIRATER_RATE_BUTTON_NEW))
					+ " "
					+ Utils.getMultilanguageTextLayOut(mContext,
							res.getString(R.string.application_name)));
			LanguageButton remindLaterButton = (LanguageButton) rateDialog
					.findViewById(R.id.appirater_rate_later_button);
			LanguageButton cancelButton = (LanguageButton) rateDialog
					.findViewById(R.id.appirater_cancel_button);

			rateButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						rateAppClick(mContext);
						rateDialog.dismiss();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			remindLaterButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						mReminderRequestDate = new Date();
						saveSettings();
						rateDialog.dismiss();
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
			});

			cancelButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mDeclinedToRate = true;
					saveSettings();
					rateDialog.dismiss();
				}
			});
			rateDialog.show();
		} catch (Exception e) {
		}
	}

	private boolean ratingConditionsHaveBeenMet() {
		try {
			if (DEBUG) {
				return true;
			}

			// has the user previously declined to rate this version of the app?
			if (mDeclinedToRate) {
				return false;
			}

			// has the user already rated the app?
			if (mRatedCurrentVersion) {
				return false;
			}

			Date now = new Date();

			// if the user wanted to be reminded later, has enough time passed?
			if (null != mReminderRequestDate) {
				long timeSinceReminderRequest = mReminderRequestDate.getTime()
						- now.getTime();
				long timeUntilReminder = 1000 * 60 * 60 * 24
						* TIME_BEFORE_REMINDING;
				if (timeSinceReminderRequest < timeUntilReminder) {
					return false;
				}
			}

			long timeSinceFirstLaunch = now.getTime() - mFirstUseDate.getTime();
			long timeUntilRate = 1000 * 60 * 60 * 24 * DAYS_UNTIL_PROMPT;
			if (timeSinceFirstLaunch >= timeUntilRate
					|| (mUseCount >= USES_UNTIL_PROMPT && mSignificantEventCount >= SIG_EVENTS_UNTIL_PROMPT)) {
				return true;
			}

			// // check if the app has been used enough
			// if( mUseCount < USES_UNTIL_PROMPT ) {
			// return false;
			// }
			//
			// // check if the user has done enough significant events
			// if( mSignificantEventCount < SIG_EVENTS_UNTIL_PROMPT ) {
			// return false;
			// }
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	private void incrementUseCount() {
		// get the app's version
		int version = -1;
		try {
			version = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException ex) {
		}

		// get the version number that we've been tracking
		if (mCurrentVersion == -1) {
			mCurrentVersion = version;
		}

		if (DEBUG) {
			Logger.s(String.format("APPIRATER Tracking version: %d",
					mCurrentVersion));
		}

		if (mCurrentVersion == version) {
			// check if the first use date has been set. if not, set it.
			if (mFirstUseDate == null) {
				mFirstUseDate = new Date();
			}

			// increment the use count
			++mUseCount;

			if (DEBUG) {
				Logger.s(String.format("APPIRATER Use count: %d",
						mUseCount));
			}
		} else {
			// it's a new version of the app, so restart tracking
			mCurrentVersion = version;
			mFirstUseDate = new Date();
			mUseCount = 1;
			mSignificantEventCount = 0;
			mRatedCurrentVersion = false;
			mDeclinedToRate = false;
			mReminderRequestDate = null;
		}

		saveSettings();
	}

	private void incrementSignificantEventCount() {
		// get the app's version
		int version = -1;
		try {
			version = mContext.getPackageManager().getPackageInfo(
					mContext.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException ex) {
		}

		// get the version number that we've been tracking
		if (mCurrentVersion == -1) {
			mCurrentVersion = version;
		}

		if (DEBUG) {
			Logger.s(String.format("APPIRATER Tracking version: %d",
					mCurrentVersion));
		}

		if (mCurrentVersion == version) {
			// check if the first use date has been set. if not, set it.
			if (mFirstUseDate == null) {
				mFirstUseDate = new Date();
			}

			// increment the significant event count
			++mSignificantEventCount;

			if (DEBUG) {
				Logger.s(String.format(
						"APPIRATER Significant event count: %d",
						mSignificantEventCount));
			}
		} else {
			mCurrentVersion = version;
			mFirstUseDate = null;
			mUseCount = 0;
			mSignificantEventCount = 1;
			mRatedCurrentVersion = false;
			mDeclinedToRate = false;
			mReminderRequestDate = null;
		}

		saveSettings();
	}

	// Settings
	private static final String APPIRATER_FIRST_USE_DATE = "APPIRATER_FIRST_USE_DATE";
	private static final String APPIRATER_REMINDER_REQUEST_DATE = "APPIRATER_REMINDER_REQUEST_DATE";
	private static final String APPIRATER_USE_COUNT = "APPIRATER_USE_COUNT";
	private static final String APPIRATER_SIG_EVENT_COUNT = "APPIRATER_SIG_EVENT_COUNT";
	private static final String APPIRATER_CURRENT_VERSION = "APPIRATER_CURRENT_VERSION";
	private static final String APPIRATER_RATED_CURRENT_VERSION = "APPIRATER_RATED_CURRENT_VERSION";
	private static final String APPIRATER_DECLINED_TO_RATE = "APPIRATER_DECLINED_TO_RATE";

	private void loadSettings() {
		Resources res = mContext.getResources();
		SharedPreferences settings = mContext.getSharedPreferences(
				mContext.getPackageName(), Context.MODE_PRIVATE);

		// Did we save settings before?
		if (settings.contains(APPIRATER_FIRST_USE_DATE)) {
			long firstUseDate = settings.getLong(APPIRATER_FIRST_USE_DATE, -1);
			if (-1 != firstUseDate) {
				mFirstUseDate = new Date(firstUseDate);
			}

			long reminderRequestDate = settings.getLong(
					APPIRATER_REMINDER_REQUEST_DATE, -1);
			if (-1 != reminderRequestDate) {
				mReminderRequestDate = new Date(reminderRequestDate);
			}

			mUseCount = settings.getInt(APPIRATER_USE_COUNT, 0);
			mSignificantEventCount = settings.getInt(APPIRATER_SIG_EVENT_COUNT,
					0);
			mCurrentVersion = settings.getInt(APPIRATER_CURRENT_VERSION, 0);
			mRatedCurrentVersion = settings.getBoolean(
					APPIRATER_RATED_CURRENT_VERSION, false);
			mDeclinedToRate = settings.getBoolean(APPIRATER_DECLINED_TO_RATE,
					false);
		}
	}

	private void saveSettings() {
		Resources res = mContext.getResources();
		SharedPreferences prefs = mContext.getSharedPreferences(
				mContext.getPackageName(), Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		long firstUseDate = -1;
		if (mFirstUseDate != null) {
			firstUseDate = mFirstUseDate.getTime();
		}
		editor.putLong(APPIRATER_FIRST_USE_DATE, firstUseDate);

		long reminderRequestDate = -1;
		if (mReminderRequestDate != null) {
			reminderRequestDate = mReminderRequestDate.getTime();
		}
		editor.putLong(APPIRATER_REMINDER_REQUEST_DATE, reminderRequestDate);

		editor.putInt(APPIRATER_USE_COUNT, mUseCount);
		editor.putInt(APPIRATER_SIG_EVENT_COUNT, mSignificantEventCount);
		editor.putInt(APPIRATER_CURRENT_VERSION, mCurrentVersion);
		editor.putBoolean(APPIRATER_RATED_CURRENT_VERSION, mRatedCurrentVersion);
		editor.putBoolean(APPIRATER_DECLINED_TO_RATE, mDeclinedToRate);

		editor.commit();
	}

	public void rateAppClick(Context mContext) {
		try {

			final String appPackageName = mContext.getPackageName();
			try {
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("market://details?id=" + appPackageName)));
			} catch (android.content.ActivityNotFoundException anfe) {
				mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse("https://play.google.com/store/apps/details?id="
								+ appPackageName)));
			}

			// Uri marketUri = Uri.parse(String.format("market://details?id=%s",
			// mContext.getPackageName()));
			// Intent marketIntent = new
			// Intent(Intent.ACTION_VIEW).setData(marketUri);
			// mContext.startActivity(marketIntent);
			mRatedCurrentVersion = true;
			saveSettings();
		} catch (Exception e) {
			Logger.printStackTrace(e);
			e.printStackTrace();
		}
	}
}
