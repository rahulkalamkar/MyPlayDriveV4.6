/**
 * 
 */
package com.hungama.myplay.activity.util;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.hungama.myplay.activity.ui.dialogs.SleepModeDialog;

import java.util.Set;

/**
 * @author DavidSvilem
 */
public class SleepModeManager {

	private static final String TAG = "SleepModeManager";

	private static SleepModeManager sIntance;
	private SleepModeDialog dialog;

	private Context mContext;

	private CountDownTimer mCountDownTimer;

	public static final String COUNT_DOWN_TIMER_FINISH_INTENT = "com.hungama.myplay.activity.intent.action.count_down_finished";

	private long interval = 1000 * 60;

	private boolean isCounting;

	private String timeRemainString;
	TextView txtView;

	public static final synchronized SleepModeManager getInstance(
			Context applicationContext) {
		if (sIntance == null) {
			sIntance = new SleepModeManager(applicationContext);
		}
		return sIntance;
	}

	private SleepModeManager(Context context) {
		mContext = context;
	}

	public void setTextView(TextView txtView) {
		this.txtView = txtView;
	}

	public void setDialog(SleepModeDialog dialog) {
		this.dialog = dialog;
	}

	/**
	 * @param time
	 *            in minutes
	 */
	public void startAlarm(int time) {

		Set<String> tags = Utils.getTags();
		if (!tags.contains("sleep_mode_used")) {
			tags.add("sleep_mode_used");
			Utils.AddTag(tags);
		}

		// Set<String> tags = PushManager.shared().getTags();
		// if (!tags.contains("sleep_mode_used")) {
		// tags.add("sleep_mode_used");
		// PushManager.shared().setTags(tags);
		// }

		long timeLong = minTomilli(time);

		isCounting = true;

		mCountDownTimer = new CountDownTimer(timeLong, interval) {

			@Override
			public void onTick(long millisUntilFinished) {

				timeRemainString = timeLeftToString(millisUntilFinished);
				try {
					if (txtView != null)
						txtView.setText(timeRemainString);
				} catch (Exception e) {
					e.printStackTrace();
				}

				// Toast.makeText(mContext, "Time remain : " + timeRemainString,
				// Toast.LENGTH_LONG).show();
			}

			@Override
			public void onFinish() {
				// Toast.makeText(mContext, "Time finished",
				// Toast.LENGTH_LONG).show();

				Logger.i(TAG, "Time finished");

				Intent countDownFinished = new Intent();
				countDownFinished.setAction(COUNT_DOWN_TIMER_FINISH_INTENT);
				mContext.sendBroadcast(countDownFinished);

				isCounting = false;
				try {
					if (dialog != null)
						dialog.dismissAllowingStateLoss();
				}catch (Exception e){}

			}

		}.start();
	}

	private long minTomilli(int minutes) {

		long milliseconds;

		milliseconds = minutes * 60000;

		return milliseconds;
	}

	private String timeLeftToString(long milli) {

		int minutes = 0;
		int seconds = 0;

		String timeLeftInSeconds;
		String timeLeftInMinutes;

		seconds = (int) (milli / 1000);

		if (seconds < 60) {
			return timeLeftInSeconds = String.valueOf(seconds)
					+ " seconds to sleep";
		} else {
			minutes = seconds / 60;
			return timeLeftInMinutes = String.valueOf(minutes)
					+ " minutes to sleep";
		}
	}

	public boolean isCountingDown() {
		return isCounting;
	}

	public void cancelCounting() {
		if (mCountDownTimer != null) {
			mCountDownTimer.cancel();
			isCounting = false;
			timeRemainString = "";
		}
	}

	public String getTimeLeftStr() {
		return timeRemainString;
	}
}
