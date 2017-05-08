package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.fragments.PlayerGymModeFragment;
import com.hungama.myplay.activity.util.ScreenLockStatus;

public class AppGuideActivity extends Activity {

	private static final String TAG = "AppGuideActivity";

	// public static final String ARGUMENT_APP_GUIDE_ACTIVITY =
	// "argument_app_guide_activity";

	private static final int PERIOD = 20 * 1000;

	private CountDownTimer countDownTimer;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_app_guide_home);
		String fromGymModeFragment = getIntent().getStringExtra(
				PlayerGymModeFragment.ARGUMENT_GYM_MODE_FRAGMENT);
		String fromHomeActivity = getIntent().getStringExtra(
				HomeActivity.ARGUMENT_HOME_ACTIVITY);
		String fromHomeActivityOffline = getIntent().getStringExtra(
				HomeActivity.ARGUMENT_Offline_ACTIVITY);

		if (fromHomeActivityOffline != null) {
			setContentView(R.layout.activity_app_guide_home_3offline);
		} else if (fromHomeActivity != null) {
			setContentView(R.layout.activity_app_guide_home);
			setResult(RESULT_OK);
		} else if (fromGymModeFragment != null) {
			setContentView(R.layout.activity_app_guide_gym_mode);
		}
	}

	@Override
	protected void onUserLeaveHint() {
		ScreenLockStatus.getInstance(getBaseContext()).onStop(true, this);
		super.onUserLeaveHint();
	}

	@Override
	protected void onResume() {
		super.onResume();
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
		countDownTimer = new CountDownTimer(PERIOD, 1000) {

			public void onTick(long millisUntilFinished) {

			}

			public void onFinish() {
				stopCounter();
				finish();
			}
		}.start();
	}

	@Override
	protected void onPause() {
		stopCounter();
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		stopCounter();
		finish();
		return super.onTouchEvent(event);

	}

	private void stopCounter() {
		if (countDownTimer != null) {
			countDownTimer.cancel();
			countDownTimer = null;
		}
	}

}