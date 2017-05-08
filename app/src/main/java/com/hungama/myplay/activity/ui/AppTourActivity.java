package com.hungama.myplay.activity.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.fragments.AppTourFragment;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.viewpageindicator.CirclePageIndicator;
import com.hungama.myplay.activity.util.viewpageindicator.PageIndicator;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AppTourActivity extends SecondaryActivity {

	private AppTourDetailsAdapter mAdapter;
	private ViewPager mPager;
	private PageIndicator mIndicator;

	private List<Bitmap> mListOfPrevImages;
	private List<String> mListOfTextTitles;
	private List<String> mListOfTextBody;

	// public static final int PERIOD = 3 * 1000;
	private CountDownTimer countDownTimer;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.activity_app_tour);

			initFirstPages();

			mAdapter = new AppTourDetailsAdapter(getSupportFragmentManager(),
					this);
			mPager = (ViewPager) findViewById(R.id.view_pager);
			mPager.setAdapter(mAdapter);

			mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
			mIndicator.setViewPager(mPager);

			mPager.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {

					if (countDownTimer != null) {
						countDownTimer.cancel();
					}

					return false;
				}
			});

			mPager.setCurrentItem(0);
			mIndicator.setCurrentItem(0);

			ImageButton skipButton = (ImageButton) findViewById(R.id.app_tour_skip_button);
			skipButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setResult(RESULT_OK);
					finish();

				}
			});

			int i = 1;
			newCountdownTimer(i, AppTourDetailsAdapter.NUM_ITEMS);
		} catch (Error e) {
			finish();
		}
	}

	private void newCountdownTimer(final int i, final int numOfPages) {
		countDownTimer = new CountDownTimer(3000, 1000) {

			public void onTick(long millisUntilFinished) {
			}

			public void onFinish() {
				mPager.setCurrentItem(i);
				cancel();
				if (i < numOfPages) {
					int k = i + 1;
					newCountdownTimer(k, numOfPages);
				} else {
					setResult(RESULT_OK);
					finish();
				}
			}
		}.start();
	}

	@Override
	protected void onStart() {
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		Analytics.startSession(this);
		Analytics.logEvent("App tour");
	}

	@Override
	protected void onStop() {
		if (countDownTimer != null) {
			countDownTimer.cancel();
			countDownTimer = null;
		}
		super.onStop();

		Analytics.onEndSession(this);
	}

	private void initFirstPages() {
 		Resources res = getResources();
		// <string name="music_splash_text">MUSIC</string>
		// <string name="videos_splash_text">VIDEOS</string>
		// Prepare the Titles
		String mTextTitle0 = Utils.getMultilanguageText(
				getApplicationContext(),
				res.getString(R.string.music_splash_text))
				+ " & "
				+ Utils.getMultilanguageText(getApplicationContext(),
						res.getString(R.string.videos_splash_text));
		String mTextTitle1 = Utils.getMultilanguageText(
				getApplicationContext(),
				res.getString(R.string.app_tour_text_title_1));
		String mTextTitle2 = Utils.getMultilanguageText(
				getApplicationContext(),
				res.getString(R.string.app_tour_text_title_2));
		String mTextTitle3 = Utils.getMultilanguageText(
				getApplicationContext(),
				res.getString(R.string.app_tour_text_title_3));
		String mTextTitle4 = Utils.getMultilanguageText(
				getApplicationContext(),
				res.getString(R.string.app_tour_text_title_4));

		// Prepare the Body Text
		String mTextBody0 = Utils.getMultilanguageText(getApplicationContext(),
				res.getString(R.string.app_tour_text_body_0));
		String mTextBody1 = Utils.getMultilanguageText(getApplicationContext(),
				res.getString(R.string.app_tour_text_body_1));
		String mTextBody2 = Utils.getMultilanguageText(getApplicationContext(),
				res.getString(R.string.app_tour_text_body_2));
		String mTextBody3 = Utils.getMultilanguageText(getApplicationContext(),
				res.getString(R.string.app_tour_text_body_3));
		String mTextBody4 = Utils.getMultilanguageText(getApplicationContext(),
				res.getString(R.string.app_tour_text_body_4));

		// Prepare the Images
		Bitmap mImage0 = getBitmapFromAsset(this,
				"icon_app_tour_music_video.png");
		Bitmap mImage1 = getBitmapFromAsset(this, "icon_app_tour_discover.png");
		Bitmap mImage2 = getBitmapFromAsset(this,
				"icon_app_tour_gamification.png");
		Bitmap mImage3 = getBitmapFromAsset(this, "icon_app_tour_radio.png");
		Bitmap mImage4 = getBitmapFromAsset(this, "icon_app_tour_others.png");

		// Add Images to a new list
		mListOfPrevImages = new ArrayList<Bitmap>();
		mListOfPrevImages.add(mImage0);
		mListOfPrevImages.add(mImage1);
		mListOfPrevImages.add(mImage2);
		mListOfPrevImages.add(mImage3);
		mListOfPrevImages.add(mImage4);

		// Add Titles to a new list
		mListOfTextTitles = new ArrayList<String>();
		mListOfTextTitles.add(mTextTitle0);
		mListOfTextTitles.add(mTextTitle1);
		mListOfTextTitles.add(mTextTitle2);
		mListOfTextTitles.add(mTextTitle3);
		mListOfTextTitles.add(mTextTitle4);

		// Add Body Text to a new list
		mListOfTextBody = new ArrayList<String>();
		mListOfTextBody.add(mTextBody0);
		mListOfTextBody.add(mTextBody1);
		mListOfTextBody.add(mTextBody2);
		mListOfTextBody.add(mTextBody3);
		mListOfTextBody.add(mTextBody4);
	}

	public static Bitmap getBitmapFromAsset(Context context, String strName) {
		AssetManager assetManager = context.getAssets();
		InputStream istr;
		Bitmap bitmap = null;
		try {
			// Now we will compress image not to receive the OutOfMemoryError on
			// hdpi and xhdpi screens.
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2;
			istr = assetManager.open(strName);
			bitmap = BitmapFactory.decodeStream(istr, null, options);
		} catch (IOException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
		return bitmap;
	}

	private class AppTourDetailsAdapter extends FragmentPagerAdapter {

		public static final int NUM_ITEMS = 5;

		public AppTourDetailsAdapter(FragmentManager fm, Context context) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return AppTourFragment.newInstance(mListOfPrevImages.get(position),
					mListOfTextTitles.get(position),
					mListOfTextBody.get(position));
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			int version = Integer.parseInt(""
					+ android.os.Build.VERSION.SDK_INT);
			mListOfPrevImages.clear();
			mListOfTextBody.clear();
			mListOfTextTitles.clear();
			Utils.unbindDrawables(findViewById(R.id.rl_tour_main), version);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}
}