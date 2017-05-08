package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ImageView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.util.ScreenLockStatus;

public class AppGuideActivityPlayerBar extends Activity {

	public static Bitmap bitmapHelpView;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_guide_player_bar);

		if (bitmapHelpView != null && !bitmapHelpView.isRecycled()) {
			ImageView iv = (ImageView) findViewById(R.id.hint_player_bar);
			if (bitmapHelpView != null)
				iv.setImageBitmap(bitmapHelpView);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
	}

	@Override
	protected void onStop() {
		setResult(RESULT_CANCELED);
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		setResult(RESULT_CANCELED);
		finish();
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onBackPressed() {
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}
}