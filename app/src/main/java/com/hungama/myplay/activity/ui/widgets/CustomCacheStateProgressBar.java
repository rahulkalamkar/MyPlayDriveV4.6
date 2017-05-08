package com.hungama.myplay.activity.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;

public class CustomCacheStateProgressBar extends RelativeLayout {

	private ImageButton buttonCacheState;
	private ProgressBar progressCache, progressCacheCircular;
	// private ImageView ivProgressCacheCircular;
	private CacheState cacheState = CacheState.NOT_CACHED;
	private boolean isNotCachedStateVisible = false;
	private boolean isCacheCountVisible = false;
	private boolean isProgressOnly = false;
	private boolean isDefualtImageGray = false;

	public CustomCacheStateProgressBar(Context context) {
		super(context);
		initialize(context);
	}

	public CustomCacheStateProgressBar(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initialize(context);
	}

	public CustomCacheStateProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize(context);
	}

	private void initialize(Context context) {
		View v = null;
		try {
			v = inflate(context, R.layout.layout_custom_progress_bar, null);
		} catch (Error e) {

			System.runFinalization();
			v = inflate(context, R.layout.layout_custom_progress_bar, null);
		}
		int width = (int) getResources().getDimension(
				R.dimen.home_media_cache_state_button_width);
		int height = (int) getResources().getDimension(
				R.dimen.home_media_cache_state_button_height);
		v.setLayoutParams(new RelativeLayout.LayoutParams(width, height));
		addView(v);

		buttonCacheState = (ImageButton) v
				.findViewById(R.id.button_cache_state);
		progressCache = (ProgressBar) v.findViewById(R.id.progress_cache_state);
		progressCacheCircular = (ProgressBar) v
				.findViewById(R.id.progress_cache_state_circular);
		// ivProgressCacheCircular = (ImageView)
		// v.findViewById(R.id.iv_cache_state_circular_progress);
		// ivProgressCacheCircular.bringToFront();

		setCacheState(cacheState);
		setProgress(0);
		isProgressOnly = false;
	}

	public void setProgress(int value) {
		progressCache.setProgress(value);
	}

	public int getProgress() {
		return progressCache.getProgress();
	}

	public void setCacheState(CacheState state) {
		cacheState = state;
		updateCacheState();
	}

	public CacheState getCacheState() {
		return cacheState;
	}

	public void setNotCachedStateVisibility(boolean value) {
		isNotCachedStateVisible = value;
	}

	public boolean getNotCachedStateVisibility() {
		return isNotCachedStateVisible;
	}

	public void setisDefualtImageGray(boolean value) {
		isDefualtImageGray = value;
	}

	public boolean getisDefualtImageGray() {
		return isDefualtImageGray;
	}

	public void setCacheCountVisibility(boolean value) {
		// isCacheCountVisible = value;
		isCacheCountVisible = false;
	}

	public boolean getCacheCountVisibility() {
		return isCacheCountVisible;
	}

	public void setCacheCount(String count) {
		// buttonCacheState.setText("");
		// buttonCacheState.setText(count);
	}

	public void showProgressOnly(boolean isProgressOnly) {
		this.isProgressOnly = isProgressOnly;
	}

	private void updateCacheState() {
		if (cacheState == CacheState.NOT_CACHED) {
			if (isProgressOnly) {
				if (isDefualtImageGray)
					buttonCacheState
							.setBackgroundResource(R.drawable.cache_state_not_cached_gray);
				else
					buttonCacheState
							.setBackgroundResource(R.drawable.cache_state_queued);
			} else {
				if (isDefualtImageGray)
					buttonCacheState
							.setBackgroundResource(R.drawable.cache_state_not_cached_gray);
				else
					buttonCacheState
							.setBackgroundResource(R.drawable.cache_state_not_cached);
			}
			if (isNotCachedStateVisible)
				buttonCacheState.setVisibility(View.VISIBLE);
			else
				buttonCacheState.setVisibility(View.INVISIBLE);
			// buttonCacheState.setText("");
			progressCache.setProgress(0);
			progressCache.setVisibility(View.INVISIBLE);
			progressCacheCircular.setVisibility(View.INVISIBLE);
			// ivProgressCacheCircular.setVisibility(View.INVISIBLE);
			// buttonCacheState.setImageResource(-1);
		} else if (cacheState == CacheState.QUEUED) {
			if (isProgressOnly)
				buttonCacheState
						.setBackgroundResource(R.drawable.cache_state_queued);
			else {
				if (isDefualtImageGray)
					buttonCacheState
							.setBackgroundResource(R.drawable.cache_state_not_cached_gray);
				else
					buttonCacheState
							.setBackgroundResource(R.drawable.cache_state_not_cached);
			}

			buttonCacheState.setVisibility(View.VISIBLE);
			// buttonCacheState.setText("");
			progressCache.setVisibility(View.INVISIBLE);
			progressCacheCircular.setVisibility(View.INVISIBLE);
			// ivProgressCacheCircular.setVisibility(View.INVISIBLE);
			// buttonCacheState.setImageResource(-1);
		} else if (cacheState == CacheState.CACHING) {
			if (isCacheCountVisible) {
				buttonCacheState
						.setBackgroundResource(R.drawable.cache_state_cached_count);
				buttonCacheState.setVisibility(View.VISIBLE);
				progressCache.setVisibility(View.INVISIBLE);
				progressCacheCircular.setVisibility(View.INVISIBLE);
				// ivProgressCacheCircular.setVisibility(View.INVISIBLE);
				// buttonCacheState.setImageResource(-1);
			} else {
				buttonCacheState
						.setBackgroundResource(R.drawable.cache_state_cached_count);
				buttonCacheState.setVisibility(View.VISIBLE);
				progressCache.setVisibility(View.INVISIBLE);
				progressCacheCircular.setVisibility(View.VISIBLE);
				// ivProgressCacheCircular.setVisibility(View.VISIBLE);
				// buttonCacheState.setImageResource(R.drawable.cache_progress_indeterminate);
				// progressCacheCircular.setVisibility(View.INVISIBLE);//Disable
				// progress
			}
		} else if (cacheState == CacheState.CACHED) {
			buttonCacheState
					.setBackgroundResource(R.drawable.cache_state_cached);
			// buttonCacheState.setText("");
			buttonCacheState.setVisibility(View.VISIBLE);
			progressCache.setVisibility(View.INVISIBLE);
			progressCacheCircular.setVisibility(View.INVISIBLE);
			// ivProgressCacheCircular.setVisibility(View.INVISIBLE);
			// buttonCacheState.setImageResource(-1);
		}
		// buttonCacheState.setVisibility(View.INVISIBLE);
		// progressCache.setVisibility(View.INVISIBLE);
	}
}
