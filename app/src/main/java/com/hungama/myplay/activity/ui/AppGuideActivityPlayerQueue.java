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

public class AppGuideActivityPlayerQueue extends Activity {

	public static class HelpLeftDrawer extends Object {

		HelpView Settings;
		HelpView playerQueuOptions;
		HelpView dragHandle;
		HelpView listItem;

		public HelpLeftDrawer(HelpView settings, HelpView firstItem,
				HelpView upgradeButton, HelpView listItem) {
			super();

			this.playerQueuOptions = firstItem;
			this.Settings = settings;
			this.dragHandle = upgradeButton;
			this.listItem = listItem;
		}
	}

	public static class HelpView {
		public int xCordinateSettings;
		public int yCordinateSettings;
		Bitmap bitmap;

		// String text;
		// int textPosition;

		public HelpView(int xCordinateSettings, int yCordinateSettings,
				Bitmap bmp/* , String text, int textPosition */) {
			super();
			this.bitmap = bmp;
			this.xCordinateSettings = xCordinateSettings;
			this.yCordinateSettings = yCordinateSettings;
			// this.text = text;
			// this.textPosition = textPosition;
		}

	}

	public static Object classObject;

	// public static HelpView[] helpViews;

	// private CountDownTimer countDownTimer;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_app_guide_player_queue);

		// if (classObject != null) {
		HelpLeftDrawer object = (HelpLeftDrawer) classObject;

		// if (object.Settings != null) {
		// ImageView iv = (ImageView) findViewById(R.id.hint_settings1);
		//
		// LayoutParams params = (LayoutParams) iv.getLayoutParams();
		// params.leftMargin = object.Settings.xCordinateSettings;
		// params.topMargin = object.Settings.yCordinateSettings -
		// getStatusBarHeight();
		//
		// if (object.Settings.bitmap != null)
		// iv.setImageBitmap(object.Settings.bitmap);
		// iv.setLayoutParams(params);

		// new Handler().postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// final LinearLayout ll = (LinearLayout)
		// findViewById(R.id.ll_hint_settings);
		// LayoutParams lp = (LayoutParams) ll.getLayoutParams();
		// final HelpLeftDrawer object = (HelpLeftDrawer) classObject;
		// lp.leftMargin = object.Settings.xCordinateSettings - ll.getWidth();
		// lp.topMargin = object.Settings.yCordinateSettings +
		// ((object.Settings.bitmap.getHeight()-ll.getHeight())/2) -
		// getStatusBarHeight();
		// ll.setLayoutParams(lp);
		// ll.setVisibility(View.VISIBLE);
		// }
		// }, 1000);

		if (object != null && object.Settings != null) {
			ImageView iv = (ImageView) findViewById(R.id.hint_clear_queue);
			if (object.Settings.bitmap != null)
				iv.setImageBitmap(object.Settings.bitmap);
		}

		if (object != null && object.playerQueuOptions != null) {
			ImageView iv1 = (ImageView) findViewById(R.id.hint_player_queue_options);
			if (object.playerQueuOptions.bitmap != null)
				iv1.setImageBitmap(object.playerQueuOptions.bitmap);
		}

		if (object != null && object.dragHandle != null) {
			ImageView iv2 = (ImageView) findViewById(R.id.hint_player_queue_drag_handle);
			if (object.dragHandle.bitmap != null)
				iv2.setImageBitmap(object.dragHandle.bitmap);
		}

		if (object != null && object.listItem != null) {
			ImageView iv2 = (ImageView) findViewById(R.id.hint_player_queue_list_item);
			if (object.listItem.bitmap != null)
				iv2.setImageBitmap(object.listItem.bitmap);
		}

//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				final HelpLeftDrawer object = (HelpLeftDrawer) classObject;
				try {
					if (object.Settings != null) {
						final LinearLayout ll = (LinearLayout) findViewById(R.id.ll_hint_clear_queue);
						LayoutParams lp = (LayoutParams) ll.getLayoutParams();
						// final HelpLeftDrawer object = (HelpLeftDrawer)
						// classObject;
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
					if (object.playerQueuOptions != null) {
						final LinearLayout ll1 = (LinearLayout) findViewById(R.id.ll_hint_player_queue_options);
						LayoutParams lp1 = (LayoutParams) ll1.getLayoutParams();
						lp1.leftMargin = object.playerQueuOptions.xCordinateSettings
								+ object.playerQueuOptions.bitmap.getWidth()
								- ll1.getWidth();
						lp1.topMargin = object.playerQueuOptions.yCordinateSettings
								- getStatusBarHeight();
						ll1.setLayoutParams(lp1);
						ll1.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				try {
					if (object.dragHandle != null) {
						final LinearLayout ll1 = (LinearLayout) findViewById(R.id.ll_hint_player_queue_drag_handle);
						LayoutParams lp1 = (LayoutParams) ll1.getLayoutParams();
						lp1.leftMargin = object.dragHandle.xCordinateSettings;
						lp1.topMargin = object.dragHandle.yCordinateSettings
								- getStatusBarHeight();
						ll1.setLayoutParams(lp1);
						ll1.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				try {
					if (object.listItem != null) {
						final RelativeLayout ll1 = (RelativeLayout) findViewById(R.id.rl_hint_player_queue_list_item);
						LayoutParams lp1 = (LayoutParams) ll1.getLayoutParams();
						// if(object.listItem.yCordinateSettings-object.dragHandle.yCordinateSettings<0)
						// lp1.topMargin = (4 *
						// object.listItem.yCordinateSettings) -
						// getStatusBarHeight();
						// else
						lp1.topMargin = object.listItem.yCordinateSettings
								- getStatusBarHeight();
						ll1.setLayoutParams(lp1);
						ll1.setVisibility(View.VISIBLE);
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}

				// if (object.dragHandle != null) {
				// final LinearLayout ll2 = (LinearLayout)
				// findViewById(R.id.ll_hint_button_upgrade);
				// LayoutParams lp2 = (LayoutParams) ll2.getLayoutParams();
				// lp2.leftMargin = object.dragHandle.xCordinateSettings;
				// lp2.topMargin = object.dragHandle.yCordinateSettings +
				// object.dragHandle.bitmap.getHeight() - ll2.getHeight() -
				// getStatusBarHeight();
				// ll2.setLayoutParams(lp2);
				// ll2.setVisibility(View.VISIBLE);
				//
				// final TextView textHint = (TextView)
				// findViewById(R.id.txt_hint_button_upgrade);
				// LayoutParams txtlp = (LayoutParams)
				// textHint.getLayoutParams();
				// txtlp.leftMargin = 50;
				// txtlp.topMargin = lp2.topMargin - (2 * getStatusBarHeight());
				// textHint.setLayoutParams(txtlp);
				// textHint.setVisibility(View.VISIBLE);
				// }
//			}
//		}, 1000);
		// }
		//
		// if (object.firstItem != null) {
		// ImageView iv = (ImageView) findViewById(R.id.first_menu_settings);
		// LayoutParams params = (LayoutParams) iv.getLayoutParams();
		// params.leftMargin = object.firstItem.xCordinateSettings;
		// params.topMargin = object.firstItem.yCordinateSettings;
		//
		// if (object.firstItem.bitmap != null)
		// iv.setImageBitmap(object.firstItem.bitmap);
		// iv.setLayoutParams(params);
		// }
		//
		// // findViewById(R.id.act)
		//
		// // iv.setLayoutParams(params);
		//
		// } else{
		// if(helpViews!=null && helpViews.length>0){
		// int count = 0;
		// for(HelpView helpView : helpViews){
		// ImageView iv = new ImageView(this);
		// iv.setId(count);
		//
		// LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT);
		// params.leftMargin = helpView.xCordinateSettings;
		// params.topMargin = helpView.yCordinateSettings -
		// getStatusBarHeight();
		//
		// if (helpView.bitmap != null)
		// iv.setImageBitmap(helpView.bitmap);
		// iv.setLayoutParams(params);
		// ((RelativeLayout) findViewById(R.id.home_overlay)).addView(iv);
		//
		// TextView tv = new TextView(this);
		// tv.setText(helpView.text);
		//
		// LayoutParams tvparams = new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT);
		// switch (helpView.textPosition) {
		// case 0:
		// tvparams.addRule(RelativeLayout.LEFT_OF, count);
		// break;
		// case 1:
		// tvparams.addRule(RelativeLayout.ABOVE, count);
		// break;
		// case 2:
		// tvparams.addRule(RelativeLayout.RIGHT_OF, count);
		// break;
		// case 3:
		// tvparams.addRule(RelativeLayout.BELOW, count);
		// break;
		// default:
		// break;
		// }
		// tv.setLayoutParams(tvparams);
		// ((RelativeLayout) findViewById(R.id.home_overlay)).addView(tv);
		//
		// count++;
		// }
		// }
		// }
		// finish();
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

		// new Handler().postDelayed(new Runnable() {
		// @Override
		// public void run() {
		// final LinearLayout ll = (LinearLayout)
		// findViewById(R.id.ll_hint_settings);
		// LayoutParams lp = (LayoutParams) ll.getLayoutParams();
		// final HelpLeftDrawer object = (HelpLeftDrawer) classObject;
		// lp.leftMargin = object.Settings.xCordinateSettings - ll.getWidth();
		// lp.topMargin = object.Settings.yCordinateSettings;
		// ll.setLayoutParams(lp);
		// }
		// }, 1000);
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