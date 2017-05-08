package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;

public class AppGuideActivityOfflineMusic extends Activity {

	public static class HelpLeftDrawer extends Object {

		HelpView Settings;
		HelpView listItem;

		public HelpLeftDrawer(HelpView settings, HelpView listItem) {
			super();
			this.Settings = settings;
			this.listItem = listItem;
		}
	}

	public static class HelpView {
		public int xCordinateSettings;
		public int yCordinateSettings;
		Bitmap bitmap;

		public HelpView(int xCordinateSettings, int yCordinateSettings,
				Bitmap bmp) {
			super();
			this.bitmap = bmp;
			this.xCordinateSettings = xCordinateSettings;
			this.yCordinateSettings = yCordinateSettings;
		}
	}

	public static Object classObject;

	// public static HelpView[] helpViews;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		try {
		setContentView(R.layout.activity_app_guide_offline_music);

		HelpLeftDrawer object = (HelpLeftDrawer) classObject;

		if (object.Settings != null) {
			ImageView iv = (ImageView) findViewById(R.id.hint_options);
			if (object.Settings.bitmap != null)
				iv.setImageBitmap(object.Settings.bitmap);
		}

		if (object.listItem != null) {
			ImageView iv2 = (ImageView) findViewById(R.id.hint_offline_music_list_item);
			if (object.listItem.bitmap != null)
				iv2.setImageBitmap(object.listItem.bitmap);
		}

//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				final HelpLeftDrawer object = (HelpLeftDrawer) classObject;
				try {
					if (object.Settings != null) {
						final LinearLayout ll = (LinearLayout) findViewById(R.id.ll_hint_options);
						LayoutParams lp = (LayoutParams) ll.getLayoutParams();
						lp.leftMargin = object.Settings.xCordinateSettings
								+ object.Settings.bitmap.getWidth()
								- ll.getWidth();
						lp.topMargin = object.Settings.yCordinateSettings
								- getStatusBarHeight();
						ll.setLayoutParams(lp);
						ll.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				try {
					if (object.listItem != null) {
						final RelativeLayout ll1 = (RelativeLayout) findViewById(R.id.rl_hint_offline_music_list_item);
						LayoutParams lp1 = (LayoutParams) ll1.getLayoutParams();
						lp1.topMargin = object.listItem.yCordinateSettings
								- getStatusBarHeight();
						ll1.setLayoutParams(lp1);
						ll1.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
//			}
//		}, 1000);
		} catch (Exception e) {
			AppGuideActivityOfflineMusic.this.finish();
		} catch (Error e) {
			AppGuideActivityOfflineMusic.this.finish();
		}
	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height",
				"dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	@Override
	protected void onResume() {
		super.onResume();
		ScreenLockStatus.getInstance(getBaseContext()).onResume(this, this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		ScreenLockStatus.getInstance(getBaseContext()).onStop();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		finish();
		return super.dispatchTouchEvent(ev);
	}
}