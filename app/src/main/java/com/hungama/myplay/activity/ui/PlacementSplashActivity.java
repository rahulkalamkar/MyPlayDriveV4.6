package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.ui.SplashScreenActivity.GifDataDownloader;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoCallBack;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.gifview.GifDecoderViewOld;

import java.io.InputStream;

/**
 * @author DavidSvilem
 * 
 */
public class PlacementSplashActivity extends Activity {

	private Placement placement;
	DisplayMetrics metrics;
	private String backgroundLink;
	private Handler h;
	CampaignsManager mCampaignsManager;
	private boolean adLoaded = false;
	GifDecoderViewOld gifView;

	boolean skipped = false, clickedOnAd = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Utils.clearCache();
		Logger.s("Track PlacementSplashActivity onCreate");
		mCampaignsManager = CampaignsManager.getInstance(this);
		metrics = new DisplayMetrics();
		ScreenLockStatus.getInstance(getApplicationContext()).reset();
		ScreenLockStatus.getInstance(getApplicationContext()).dontShowAd();
		try {
			setContentView(R.layout.activity_splash_advertisement_layout);
		} catch (Error e) {

		}

		gifView = (GifDecoderViewOld) findViewById(R.id.ivSplashAd);
		Button skip = (Button) findViewById(R.id.bSkipToHungama);
		skip.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				findViewById(R.id.footer_tap).setVisibility(View.INVISIBLE);
				skipped = true;
				setResult(RESULT_OK);
				finish();
			}
		});
		findViewById(R.id.footer_tap).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				findViewById(R.id.footer_tap).setVisibility(View.INVISIBLE);
				skipped = true;
				setResult(RESULT_OK);
				finish();
			}
		});

		loadAd();
	}

	private void loadAd() {
		try {
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		CampaignsManager mCampaignsManager = CampaignsManager.getInstance(this);
		Logger.i("Placement", "Splash :: " + System.currentTimeMillis());
		placement = mCampaignsManager
				.getPlacementOfType(PlacementType.SPLASH_SCREEN);
		if (placement != null)
			backgroundLink = Utils.getDisplayProfile(metrics, placement);
		h = new Handler();
		if (backgroundLink != null) {

			if (backgroundLink.toLowerCase().endsWith(".gif")) {

				new GifDataDownloader(this) {
					@Override
					protected void onPostExecute(final InputStream stream) {
						if (stream == null)
							onBackPressed();
						else {
//							gifView.setBytes(bytes);
//							gifView.startAnimation();
							gifView.playGif(stream);
//							Log.d("Splash",
//									"GIF width is " + gifView.getGifWidth());
//							Log.d("Splash",
//									"GIF height is " + gifView.getGifHeight());
							onAdLoad();
						}
					}
				}.execute(backgroundLink);
			} else {
				PicassoUtil.with(PlacementSplashActivity.this).load(
						new PicassoCallBack() {

							@Override
							public void onSuccess() {
								onAdLoad();
							}

							@Override
							public void onError() {
								onBackPressed();
							}
						}, backgroundLink, gifView, -1);
			}
			int max = ApplicationConfigurations.getInstance(
					PlacementSplashActivity.this)
					.getAppConfigSplashAdAutoSkip();// 10
			h.postDelayed(runnableSkip, max * 1000);
		} else {
			onBackPressed();
		}

		ScreenLockStatus.getInstance(getBaseContext()).dontShowAd();
	}

	private void onAdLoad() {
		if(!isFinishing() && !skipped) {
			try {
				Logger.i("Placement", "2 Splash :: " + System.currentTimeMillis());
				HungamaApplication.splashAdDisplyedCount++;
				// (findViewById(R.id.ivSplashAd)).setClickable(true);
				// (findViewById(R.id.llSplashAd)).setClickable(true);

				findViewById(R.id.llSplashAd).setOnClickListener(
						new OnClickListener() {

							@Override
							public void onClick(View v) {
								if (placement != null) {
									Utils.performclickEvent(
											getApplicationContext(), placement);
									clickedOnAd = true;
								}
							}
						});

				adLoaded = true;
				Utils.postViewEvent(getApplicationContext(), placement);

				PlacementSplashActivity.this.findViewById(R.id.ivHungamaSplash)
						.setVisibility(View.GONE);
				PlacementSplashActivity.this.findViewById(R.id.pbHungamaSplash)
						.setVisibility(View.GONE);
			} catch (Exception e) {
				h.removeCallbacks(runnableSkip);
				skipped = true;
				setResult(RESULT_OK);
				finish();
			}
		}
	}

	Runnable runnableSkip = new Runnable() {
		@Override
		public void run() {
			if (!adLoaded && !skipped)
				onBackPressed();
		}
	};

	public void onBackPressed() {
		try {
			skipped = true;
			setResult(RESULT_OK);
			finish();
			super.onBackPressed();
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	public void finish() {
		if (h != null)
			h.removeCallbacks(runnableSkip);
		super.finish();
	};

	@Override
	protected void onStop() {
		super.onStop();

	}

	@Override
	protected void onUserLeaveHint() {
		super.onUserLeaveHint();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(clickedOnAd)
			onBackPressed();
	}

	@Override
	protected void onDestroy() {
		Utils.unbindDrawables((RelativeLayout) findViewById(R.id.llSplashAd));
		Utils.clearCache();
		if (gifView != null && backgroundLink != null
				&& backgroundLink.toLowerCase().endsWith(".gif"))
			gifView.clear();
		super.onDestroy();
	}

}
