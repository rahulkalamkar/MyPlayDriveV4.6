package com.hungama.myplay.activity.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.util.Utils;

/**
 * Tab Bar of the {@link HomeFragment}.
 */
public class GoOfflineTabBar extends LinearLayout implements OnClickListener {

	public static final int TAB_ID_SONGS = 3000001;
	public static final int TAB_ID_VIDOES = 3000002;
	public static final int TAB_ID_OPTIONS = 3000003;
	// public static final int TAB_ID_MY_STREAM = 1000004;

	private LanguageButton mButtonSongs;
	private LanguageButton mButtonVideos;
	private ImageButton mButtonOptions;
	// private Button mButtonMyStream;

	private OnTabSelectedListener mOnTabSelectedListener;
	private int mCurrentTabId;

	public GoOfflineTabBar(Context context) {
		super(context);
		initialize();
	}

	public GoOfflineTabBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		initialize();
	}

	public GoOfflineTabBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialize();
	}

	// ======================================================
	// PRIVATE HELPER METHODS.
	// ======================================================

	private void initialize() {

		this.setOrientation(LinearLayout.HORIZONTAL);

		Resources resources = getResources();

		// creates the separators between the tabs.
		View lineSeparator1 = new View(getContext());
		lineSeparator1.setBackgroundColor(resources
				.getColor(R.color.home_tabwidget_tab_separator));

		View lineSeparator2 = new View(getContext());
		lineSeparator2.setBackgroundColor(resources
				.getColor(R.color.home_tabwidget_tab_separator));

		View lineSeparator3 = new View(getContext());
		lineSeparator3.setBackgroundColor(resources
				.getColor(R.color.home_tabwidget_tab_separator));

		LinearLayout.LayoutParams separatorParams = new LinearLayout.LayoutParams(
				2, LayoutParams.MATCH_PARENT);

		float labelSize = resources
				.getDimensionPixelSize(R.dimen.large_text_size);

		// Latest.
		mButtonSongs = new LanguageButton(getContext());
		mButtonSongs.setId(TAB_ID_SONGS);
		mButtonSongs
				.setBackgroundResource(R.drawable.background_home_tabwidget_tab_regular_selector);
		mButtonSongs.setSingleLine(true);
		mButtonSongs.setGravity(Gravity.CENTER);
		mButtonSongs.setPadding(0, 0, 0, 8);
		mButtonSongs.setTextColor(resources
				.getColor(R.color.home_tabwidget_tab_label_regular_selected));
		mButtonSongs.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSize);
		mButtonSongs.setText(Utils.getMultilanguageText(
				getContext(),
				getResources().getString(
						R.string.social_profile_info_section_fav_songs_1))
				+ " (0)");
		mButtonSongs.setOnClickListener(this);

		// Featured.
		mButtonVideos = new LanguageButton(getContext());
		mButtonVideos.setId(TAB_ID_VIDOES);
		mButtonVideos
				.setBackgroundResource(R.drawable.background_home_tabwidget_tab_regular_selector);
		mButtonVideos.setSingleLine(true);
		mButtonVideos.setGravity(Gravity.CENTER);
		mButtonVideos.setPadding(0, 0, 0, 8);
		mButtonVideos.setTextColor(resources
				.getColor(R.color.home_tabwidget_tab_label_regular_selected));
		mButtonVideos.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSize);
		mButtonVideos
				.setText(Utils.getMultilanguageText(getContext(),
						getResources().getString(R.string.videos_splash_text))
						+ " (0)");
		mButtonVideos.setOnClickListener(this);

		// Recommended.
		mButtonOptions = new ImageButton(getContext());
		mButtonOptions.setId(TAB_ID_OPTIONS);
		mButtonOptions.setBackgroundResource(0);
		mButtonOptions
				.setImageResource(R.drawable.background_main_title_bar_button_options_selector);
		// mButtonOptions.setSingleLine(true);
		// mButtonOptions.setGravity(Gravity.CENTER);
		// mButtonOptions.setPadding(0, 0, 0, 0);
		// mButtonOptions.setTextColor(resources.getColor(R.color.home_tabwidget_tab_label_regular_selected));
		// mButtonOptions.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSize);
		// mButtonOptions.setText(R.string.home_tab_label_recommended);
		// mButtonOptions.setSingleLine(true);
		mButtonOptions.setOnClickListener(this);
		mButtonOptions.setVisibility(View.GONE);

		LinearLayout.LayoutParams optionsParams = new LinearLayout.LayoutParams(
				(int) getResources().getDimension(
						R.dimen.main_title_bar_options_button_width),
				LinearLayout.LayoutParams.MATCH_PARENT);

		// My Stream.
		// mButtonMyStream = new Button(getContext());
		// mButtonMyStream.setId(TAB_ID_MY_STREAM);
		// mButtonMyStream.setBackgroundResource(R.drawable.background_home_tabwidget_tab_stream_selector);
		// mButtonMyStream.setSingleLine(true);
		// mButtonMyStream.setGravity(Gravity.CENTER);
		// mButtonMyStream.setPadding(0, 0, 0, 0);
		// mButtonMyStream.setTextColor(resources.getColor(R.color.home_tabwidget_tab_label_stream_selected));
		// mButtonMyStream.setTextSize(TypedValue.COMPLEX_UNIT_PX, labelSize);
		// mButtonMyStream.setText(R.string.home_tab_label_my_stream);
		// mButtonMyStream.setSingleLine(true);
		// mButtonMyStream.setOnClickListener(this);

		LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		tabParams.weight = 1;

		this.addView(mButtonSongs, tabParams);
		this.addView(lineSeparator1, separatorParams);
		this.addView(mButtonVideos, tabParams);
		// this.addView(lineSeparator2, separatorParams);
		// this.addView(mButtonOptions, optionsParams);
		// this.addView(lineSeparator3, separatorParams);
		// this.addView(mButtonMyStream, tabParams);

		this.invalidate();

		setCurrentSelected(0);
	}

	@Override
	public void onClick(View view) {
		int tabId = view.getId();
		switch (tabId) {
		case TAB_ID_SONGS:

			if (view.isSelected()) {
				// updates the listener that the tab was retapped.
				if (mOnTabSelectedListener != null) {
					mOnTabSelectedListener.onTabReselected(TAB_ID_SONGS);
				}

			} else {
				// updates the tab's view.
				mButtonSongs.setSelected(true);
				mButtonVideos.setSelected(false);
				mButtonOptions.setSelected(false);
				// mButtonMyStream.setSelected(false);
				this.invalidate();

				mCurrentTabId = TAB_ID_SONGS;

				// updates the listener to the view.
				if (mOnTabSelectedListener != null) {
					mOnTabSelectedListener.onTabSelected(TAB_ID_SONGS);
				}
			}

			break;

		case TAB_ID_VIDOES:

			if (view.isSelected()) {
				// updates the listener that the tab was retapped.
				if (mOnTabSelectedListener != null) {
					mOnTabSelectedListener.onTabReselected(TAB_ID_VIDOES);
				}

			} else {
				// updates the tab's view.
				mButtonSongs.setSelected(false);
				mButtonVideos.setSelected(true);
				mButtonOptions.setSelected(false);
				// mButtonMyStream.setSelected(false);
				this.invalidate();

				mCurrentTabId = TAB_ID_VIDOES;

				// updates the listener to the view.
				if (mOnTabSelectedListener != null) {
					mOnTabSelectedListener.onTabSelected(TAB_ID_VIDOES);
				}
			}

			break;

		case TAB_ID_OPTIONS:

			if (view.isSelected()) {
				// updates the listener that the tab was retapped.
				if (mOnTabSelectedListener != null) {
					mOnTabSelectedListener.onTabReselected(TAB_ID_OPTIONS);
				}
				mButtonOptions.setSelected(false);
				this.invalidate();
			} else {
				// updates the tab's view.
				// mButtonSongs.setSelected(false);
				// mButtonVideos.setSelected(false);
				mButtonOptions.setSelected(true);
				// mButtonMyStream.setSelected(false);
				this.invalidate();

				// mCurrentTabId = TAB_ID_OPTIONS;

				// updates the listener to the view.
				if (mOnTabSelectedListener != null) {
					mOnTabSelectedListener.onTabSelected(TAB_ID_OPTIONS);
				}
			}

			break;

		// case TAB_ID_MY_STREAM:
		//
		// if (view.isSelected()) {
		// // updates the listener that the tab was retapped.
		// if (mOnTabSelectedListener != null) {
		// mOnTabSelectedListener.onTabReselected(TAB_ID_MY_STREAM);
		// }
		//
		// } else {
		// // updates the tab's view.
		// mButtonSongs.setSelected(false);
		// mButtonVideos.setSelected(false);
		// mButtonOptions.setSelected(false);
		// mButtonMyStream.setSelected(true);
		// this.invalidate();
		//
		// mCurrentTabId = TAB_ID_MY_STREAM;
		//
		// // updates the listener to the view.
		// if (mOnTabSelectedListener != null) {
		// mOnTabSelectedListener.onTabSelected(TAB_ID_MY_STREAM);
		// }
		// }
		//
		// break;
		}

	}

	// ======================================================
	// PUBLIC.
	// ======================================================

	/**
	 * Interface callbacks to be invoked when the user has interact with the
	 * tabs / selected a tab.
	 */
	public interface OnTabSelectedListener {

		/**
		 * Invoked when the tab was selected.
		 */
		public void onTabSelected(int tabId);

		/**
		 * Invoked when the tab was clicked when was already selected.
		 */
		public void onTabReselected(int tabId);
	}

	public void setOnTabSelectedListener(OnTabSelectedListener listener) {
		mOnTabSelectedListener = listener;
	}

	public void setCurrentSelected(int position) {
		boolean isClicked = false;
		switch (position) {
		case 0:
		case TAB_ID_SONGS:
			isClicked = mButtonSongs.performClick();
			break;

		case 1:
		case TAB_ID_VIDOES:
			isClicked = mButtonVideos.performClick();
			break;

		case 2:
		case TAB_ID_OPTIONS:
			isClicked = mButtonOptions.performClick();
			break;

		// case TAB_ID_MY_STREAM:
		// case 3:
		// isClicked = mButtonMyStream.performClick();
		// break;
		}
		com.hungama.myplay.activity.util.Logger.i("",
				"is click event called........" + isClicked);
	}

	// public void markSelectedTab(int tabId) {
	// switch (tabId) {
	// case TAB_ID_SONGS:
	// mButtonSongs.setSelected(true);
	// mButtonVideos.setSelected(false);
	// mButtonOptions.setSelected(false);
	// // mButtonMyStream.setSelected(false);
	// break;
	//
	// case TAB_ID_VIDOES:
	// mButtonSongs.setSelected(false);
	// mButtonVideos.setSelected(true);
	// mButtonOptions.setSelected(false);
	// // mButtonMyStream.setSelected(false);
	// break;
	//
	// case TAB_ID_OPTIONS:
	// mButtonSongs.setSelected(false);
	// mButtonVideos.setSelected(false);
	// mButtonOptions.setSelected(true);
	// // mButtonMyStream.setSelected(false);
	// break;
	//
	// // case TAB_ID_MY_STREAM:
	// // mButtonSongs.setSelected(false);
	// // mButtonVideos.setSelected(false);
	// // mButtonOptions.setSelected(false);
	// // mButtonMyStream.setSelected(true);
	// // break;
	// }
	// }

	public int getSelectedTab() {
		return mCurrentTabId;
	}

	public void updateTabCount(int tabId, int count) {
		switch (tabId) {
		case TAB_ID_SONGS:
			mButtonSongs.setText(Utils.getMultilanguageText(
					getContext(),
					getResources().getString(
							R.string.social_profile_info_section_fav_songs_1))
					+ " (" + count + ")");

			break;
		case TAB_ID_VIDOES:
			if (count == 0) {
				mButtonVideos.setEnabled(false);
				mButtonVideos.setAlpha(0.3f);
			} else {
				mButtonVideos.setEnabled(true);
				mButtonVideos.setAlpha(1f);
			}

			mButtonVideos.setText(Utils.getMultilanguageText(getContext(),
					getResources().getString(R.string.videos_splash_text))
					+ " (" + count + ")");
			break;
		}
	}
}
