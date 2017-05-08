package com.hungama.myplay.activity.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ScrollView;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.TrackLyrics;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Utils;

public class LyricsCustomDialog extends Dialog {

	TrackLyrics mTrackLyrics;
	Track track;
	Context context;

	public LyricsCustomDialog(Context context, TrackLyrics mTrackLyrics,
			Track track) {
		super(context);
		this.context = context;
		this.mTrackLyrics = mTrackLyrics;
		this.track = track;
	}

	CheckBox cbDoNotShowAgain;
	LanguageTextView tvSongName, tvAlbumName, tvLyricsDetails,// tvBtnShowMeMore,
			tvBtnNotNow;
	ScrollView mScrollView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_player_lyrics_dialog);

		View rootView = findViewById(R.id.llMainLayout);
		DataManager mDataManager = DataManager.getInstance(getContext()
				.getApplicationContext());
		ApplicationConfigurations applicationConfigurations = mDataManager
				.getApplicationConfigurations();
		if (applicationConfigurations.getUserSelectedLanguage() != Constants.LANGUAGE_ENGLISH) {
			Utils.traverseChild(rootView, getContext());
		}

		tvSongName = (LanguageTextView) findViewById(R.id.tvSongName);
		tvAlbumName = (LanguageTextView) findViewById(R.id.tvAlbumName);
		tvLyricsDetails = (LanguageTextView) findViewById(R.id.tvLyricsDetails);
		tvBtnNotNow = (LanguageTextView) findViewById(R.id.btnNotNow);
		cbDoNotShowAgain = (CheckBox) findViewById(R.id.cbDoNotShowAgain);
		tvBtnNotNow.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
						.getInstance(context);
				if (cbDoNotShowAgain.isChecked()) {
					mApplicationConfigurations.setLyricsShow(false);
					showThankPrompt();
				} else {
					mApplicationConfigurations.setLyricsShow(true);
					mApplicationConfigurations.increaseLyricsNotNowCount();
					if (mApplicationConfigurations.getLyricsNotNowCount() == 3) {
						mApplicationConfigurations.setLyricsShow(false);
						mApplicationConfigurations.resetLyricsNotNowCount();
					}
				}
				dismiss();
			}
		});

		cbDoNotShowAgain
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							tvBtnNotNow.setText(Utils.getMultilanguageText(
									context,
									context.getResources().getString(
											R.string.txt_lyrics_btn_ok)));
							// tvBtnNotNow.setText(context.getResources()
							// .getString(R.string.txt_lyrics_btn_ok));
						} else {
							tvBtnNotNow.setText(Utils.getMultilanguageText(
									context,
									context.getResources().getString(
											R.string.txt_lyrics_btn_not_now)));
							// tvBtnNotNow
							// .setText(context.getResources().getString(
							// R.string.txt_lyrics_btn_not_now));
						}
					}
				});

		findViewById(R.id.lyrics_dialog_close_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dismiss();
					}
				});
		tvSongName
				.setText(track.getTitle() + " (" + track.getAlbumName() + ")");
		tvAlbumName.setText(track.getAlbumName());
		// triviaList = mTrackLyrics.getLyrics();
		if (mTrackLyrics != null && mTrackLyrics.getLyrics() != null) {
			setTriviaText(mTrackLyrics.getLyrics().trim());
		}

		mScrollView = (ScrollView) tvLyricsDetails.getParent();
		final int maxHeight = (int) (HomeActivity.metrics.heightPixels * 0.6);
		mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (mScrollView.getHeight() > maxHeight) {
							mScrollView.getLayoutParams().height = maxHeight;
						}
					}
				});
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}

	@Override
	public void show() {
		super.show();
	}

	private void setTriviaText(String text) {
		tvLyricsDetails.setText(text.trim());
	}

	private void showThankPrompt() {
		CustomAlertDialog thankYouPrompt = new CustomAlertDialog(context);
		thankYouPrompt.setTitle(R.string.txt_prompt_thank_you_title);
		thankYouPrompt.setMessage(R.string.txt_prompt_thank_you_message);
		thankYouPrompt.setNegativeButton(R.string.txt_trivia_btn_ok, null);
		thankYouPrompt.show();
	}
}