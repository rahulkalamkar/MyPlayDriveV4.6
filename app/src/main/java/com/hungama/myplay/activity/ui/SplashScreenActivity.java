package com.hungama.myplay.activity.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
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
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.util.FileCache;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoCallBack;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.gifview.GifDecoderViewOld;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Shows the application's splash screen when opening it from the launcher
 * application.
 */

public class SplashScreenActivity extends Activity {

	private String backgroundLink;
	// private Drawable backgroundImage;
	private Handler h = new Handler();
	private Placement placement;
	private static DisplayMetrics metrics;
	private boolean adLoaded = false, skipped = false;
	static SplashScreenActivity object;
//	GifDecoderView gifView;
	GifDecoderViewOld gifView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Logger.i("Splash Screen","Track SplashScreenActivity onCreate");
		object = this;
		Utils.clearCache();

		ScreenLockStatus.getInstance(getBaseContext()).reset();
		ScreenLockStatus.getInstance(getApplicationContext()).dontShowAd();
		finishSplash();
	}

	@Override
	public void onBackPressed() {
		h.removeCallbacks(runnableSkip);
		Utils.clearCache();
		finish();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void finishSplash() {
		// finishes the Slash screen, returning back.
		CampaignsManager mCampaignsManager = CampaignsManager.getInstance(this);
		Logger.i("Placement", "Splash :: " + System.currentTimeMillis());
		placement = mCampaignsManager
				.getPlacementOfType(PlacementType.SPLASH_SCREEN);
		Logger.i("Placement", "1 Splash :: " + System.currentTimeMillis());
		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(this);
		if (placement == null
				|| getIntent().getBooleanExtra("skip_ad", false)
				|| (HungamaApplication.splashAdDisplyedCount == 0 && !appConfig
						.getAppConfigSplashLaunch())
				|| (HungamaApplication.splashAdDisplyedCount > 0 && !appConfig
						.getAppConfigSplashReLaunch())
				|| HungamaApplication.splashAdDisplyedCount >= appConfig
						.getSplashAdCountLimit() || !Utils.isConnected()) {
			skipped = true;
			setResult(RESULT_OK);
			finish();
		} else {
			if (!Utils.isConnected()
					&& !ApplicationConfigurations.getInstance(this)
							.getSaveOfflineMode()) {
				setContentView(R.layout.application_splash_layout);
				CustomAlertDialog alertBuilder = new CustomAlertDialog(this);
				alertBuilder
						.setMessage(R.string.connection_error_empty_view_title);
				alertBuilder.setPositiveButton(
						R.string.connection_error_empty_view_button_retry,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								finishSplash();
							}
						});
				alertBuilder
						.setNegativeButton(
								R.string.connection_error_empty_view_button_play_offline,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										try {
											skipped = true;
											ApplicationConfigurations
													.getInstance(
															SplashScreenActivity.this)
													.setSaveOfflineMode(true);
											setResult(RESULT_OK);
											finish();
										} catch (Exception e) {

										}
									}
								});
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
						&& !isDestroyed()) {
					try {
						alertBuilder.show();
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				} else {
					try {
						alertBuilder.show();
					} catch (Exception e) {
						Logger.printStackTrace(e);
					}
				}
				return;
			}

			setContentView(R.layout.activity_splash_advertisement_layout);
			gifView = (GifDecoderViewOld) findViewById(R.id.ivSplashAd);
			metrics = new DisplayMetrics();
			gifView.setClickable(false);
			Button skip = (Button) findViewById(R.id.bSkipToHungama);
			skip.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					skipped = true;
					findViewById(R.id.llSplashAd).setVisibility(View.INVISIBLE);
					findViewById(R.id.footer_tap).setVisibility(View.INVISIBLE);
					setResult(RESULT_OK);
					finish();
				}
			});
			findViewById(R.id.footer_tap).setOnClickListener(
					new OnClickListener() {

						@Override
						public void onClick(View v) {
							skipped = true;
							findViewById(R.id.llSplashAd).setVisibility(
									View.INVISIBLE);
							findViewById(R.id.footer_tap).setVisibility(
									View.INVISIBLE);
							setResult(RESULT_OK);
							finish();
						}
					});
			getWindowManager().getDefaultDisplay().getMetrics(metrics);
			backgroundLink = Utils.getDisplayProfile(metrics, placement);// + "andoganim_e0.gif";

			Logger.i("backgroundLink", "backgroundLink:" + backgroundLink);
			if (backgroundLink != null) {

				if (backgroundLink.toLowerCase().endsWith(".gif")) {

					new GifDataDownloader(object) {
						@Override
						protected void onPostExecute(final InputStream stream) {
							if (stream == null)
								skip();
							else {
								try {
//									gifView.setBytes(bytes);
//									gifView.startAnimation();
									gifView.playGif(stream);
								} catch (Exception e2) {
									// TODO: handle exception
								}

								// Log.d("Splash",
								// "GIF width is " + gifView.getGifWidth());
								// Log.d("Splash",
								// "GIF height is "
								// + gifView.getGifHeight());
								onAdLoad();
							}
						}
					}.execute(backgroundLink);
				} else {
					PicassoUtil.with(SplashScreenActivity.this).load(
							new PicassoCallBack() {

								@Override
								public void onSuccess() {
									onAdLoad();
								}

								@Override
								public void onError() {
									skip();
								}
							}, backgroundLink, gifView, -1);
				}
				int max = ApplicationConfigurations.getInstance(
						SplashScreenActivity.this)
						.getAppConfigSplashAdAutoSkip();// 10
				h.postDelayed(runnableSkip, max * 1000);
			} else {
				skip();
			}

		}
	}

	private void skip() {
		h.removeCallbacks(runnableSkip);
		skipped = true;
		setResult(RESULT_OK);
		finish();
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

				SplashScreenActivity.this.findViewById(R.id.ivHungamaSplash)
						.setVisibility(View.GONE);
				SplashScreenActivity.this.findViewById(R.id.pbHungamaSplash)
						.setVisibility(View.GONE);
			} catch (Exception e) {
				h.removeCallbacks(runnableSkip);
				skipped = true;
				setResult(RESULT_OK);
				finish();
			}
		}
	}

	private Runnable runnableSkip = new Runnable() {
		@Override
		public void run() {
			if (!adLoaded && !skipped) {
				skipped = true;
				findViewById(R.id.llSplashAd).setVisibility(View.INVISIBLE);
				findViewById(R.id.footer_tap).setVisibility(View.INVISIBLE);
				setResult(RESULT_OK);
				finish();
			}
		}
	};

	public void finish() {
		try {
			if (h != null)
				h.removeCallbacks(runnableSkip);
		} catch (Exception e) {
		}

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
		if(clickedOnAd){
			skipSplashAd();
		}
	}

	@Override
	protected void onDestroy() {
		object = null;
		Utils.clearCache();
		Utils.unbindDrawables((RelativeLayout) findViewById(R.id.llSplashAd));

		if (gifView != null && backgroundLink != null
				&& backgroundLink.toLowerCase().endsWith(".gif"))
			gifView.clear();
		super.onDestroy();
	}

	public static class GifDataDownloader extends
			AsyncTask<String, Void, InputStream> {
		private static final String TAG = "GifDataDownloader";
		private Context context;

		public GifDataDownloader(Context context) {
			this.context = context;
		}

		@Override
		protected InputStream doInBackground(final String... params) {
			final String gifUrl = params[0];
			if (gifUrl == null)
				return null;
			InputStream gif = null;
			try {
				Utils.getBitmap(
						context,
						metrics.widthPixels, gifUrl);
				FileCache fileCache = new FileCache(
						context);
				File f = fileCache.getFile(gifUrl);
				String path = f.getAbsolutePath();
				gif = new FileInputStream(path);

//				gif = getBuffer(gifUrl);
//				gif = FileUtils.readFileStreamFromAssets(object, "552311.gif");
			} catch (OutOfMemoryError e) {
				Logger.e(TAG, "GifDecode OOM: " + gifUrl + "  " + e);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return gif;
		}

		public InputStream getBuffer(final String urlString) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				final URL url = new URL(urlString);
				URLConnection conn = url.openConnection();
				InputStream input = conn.getInputStream();
//				byte[] buff = new byte[1024];
//				int length = 0;
//				while ((length = input.read(buff)) > 0) {
//					os.write(buff, 0, length);
//				}
				return input;//os.toByteArray();
			} catch (final MalformedURLException e) {
				Logger.d("Splash", "Malformed URL");
				Logger.printStackTrace(e);
			} catch (final OutOfMemoryError e) {
				Logger.d("Splash", "Out of memory");
				Logger.printStackTrace(e);
			} catch (final UnsupportedEncodingException e) {
				Logger.d("Splash", "Unsupported encoding");
				Logger.printStackTrace(e);
			} catch (final IOException e) {
				Logger.d("Splash", "IO exception");
				Logger.printStackTrace(e);
			} finally {
			}
			return null;
		}
	}

	private boolean clickedOnAd = false;

	private void skipSplashAd(){
		skipped = true;
		findViewById(R.id.llSplashAd).setVisibility(
				View.INVISIBLE);
		findViewById(R.id.footer_tap).setVisibility(
				View.INVISIBLE);
		setResult(RESULT_OK);
		finish();
	}
}
