package com.hungama.myplay.activity.ui.dialogs;

import java.io.Serializable;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.ui.TrendNowActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageCheckBox;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Utils;

public class DiscoveruOfTheDayTrendCustomDialog extends Dialog {

	private Track track;
	private Context context;

	public DiscoveruOfTheDayTrendCustomDialog(Context context, Track track) {
		super(context);
		this.context = context;
		this.track = track;
	}

	private LanguageCheckBox cbDoNotShowAgain;
	private LanguageTextView btnTrendThis, btnCancel;

	// private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_discovery_of_the_day_trend_dialog);
		// handler = new Handler();
		View rootView = findViewById(R.id.llMainLayout);
		DataManager mDataManager = DataManager.getInstance(getContext()
				.getApplicationContext());
		ApplicationConfigurations applicationConfigurations = mDataManager
				.getApplicationConfigurations();
		if (applicationConfigurations.getUserSelectedLanguage() != Constants.LANGUAGE_ENGLISH) {
			Utils.traverseChild(rootView, getContext());
		}

		btnTrendThis = (LanguageTextView) findViewById(R.id.btnTrendThis);
		btnCancel = (LanguageTextView) findViewById(R.id.btnCancel);
		cbDoNotShowAgain = (LanguageCheckBox) findViewById(R.id.cbDoNotShowAgain);
		final ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(context);
		btnTrendThis
				.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						// showNextTrivia();
						if (cbDoNotShowAgain.isChecked()) {
							mApplicationConfigurations
									.setNeedTrendDialogShowForTheSession(false);
						} else {
							mApplicationConfigurations
									.setNeedTrendDialogShowForTheSession(true);
						}
						dismiss();
						if (track != null) {
							MediaItem mediaItem = new MediaItem(track.getId(),
									track.getTitle(), track.getAlbumName(),
									track.getArtistName(), ImagesManager
											.getMusicArtSmallImageUrl(track
													.getImagesUrlArray()),
									track.getBigImageUrl(), MediaType.TRACK
											.name().toLowerCase(), 0, 0, track
											.getImages(), track.getAlbumId());
							Intent intent = new Intent(context,
									TrendNowActivity.class);
							Bundle args = new Bundle();
							args.putSerializable(
									TrendNowActivity.EXTRA_DATA_MEDIA_ITEM,
									(Serializable) mediaItem);
							intent.putExtras(args);
							context.startActivity(intent);
						}

					}
				});

		btnCancel.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (cbDoNotShowAgain.isChecked()) {
					mApplicationConfigurations
							.setNeedTrendDialogShowForTheSession(false);
				} else {
					mApplicationConfigurations
							.setNeedTrendDialogShowForTheSession(true);
				}
				dismiss();
			}
		});
		findViewById(R.id.tivia_dialog_close_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dismiss();
					}
				});
	}
}