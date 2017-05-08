package com.hungama.myplay.activity.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RadioGroup;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.widgets.LanguageRadioButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Utils;

public class TransperentActivityMyCollection extends Activity implements
		OnClickListener/*
						 * , OnCheckedChangeListener
						 */{

	private LanguageRadioButton radio_collection_song, radio_collection_video;
	private RadioGroup radioGroupFav;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().getAttributes().windowAnimations =
		// R.style.bottom_to_top_animation;
		setContentView(R.layout.activity_transperent_mycollection);

		// Bundle b = getIntent().getExtras();
		// if (b != null) {
		// isRadioFilter = b.getBoolean("isRadioFilter");
		// }

		View rootView = findViewById(R.id.layoutToMove);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(this);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, this);
		}

		LanguageTextView btnClose = (LanguageTextView) findViewById(R.id.btn_close);
		btnClose.setOnClickListener(this);
		// checkbox1 = (CheckBox)findViewById(R.id.checkbox1);
		radio_collection_song = (LanguageRadioButton) findViewById(R.id.radio_collection_song);
		radio_collection_video = (LanguageRadioButton) findViewById(R.id.radio_collection_video);

		radioGroupFav = (RadioGroup) findViewById(R.id.radioGroupFav);

		radio_collection_song
				.setText(getString(R.string.favorite_dialog_whitespace)
						+ radio_collection_song.getText());
		radio_collection_video
				.setText(getString(R.string.favorite_dialog_whitespace)
						+ radio_collection_video.getText());

		if (mApplicationConfigurations.getUserSelectedLanguage() == 0)
			btnClose.setText(getString(R.string.alert_dialog_close)
					.toUpperCase());
		else {
			btnClose.setText(DBOHandler.getTextFromDb(
					getString(R.string.alert_dialog_close), this));
		}

		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(this);
		if (appConfig.getMyCollectionSelection() == 0)
			radio_collection_song.setChecked(true);
		else if (appConfig.getMyCollectionSelection() == 1)
			radio_collection_video.setChecked(true);
		else
			radio_collection_song.setChecked(true);

		// ApplicationConfigurations appConfig =
		// ApplicationConfigurations.getInstance(
		// this);

	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_close:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void finish() {
		boolean isFilterOptionChanged = false;
		int pos = -1;

		int selectedId = radioGroupFav.getCheckedRadioButtonId();

		if (selectedId == R.id.radio_collection_song) {
			pos = 0;
		} else if (selectedId == R.id.radio_collection_video) {
			pos = 1;
		}

		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(this);
		if (appConfig.getMyCollectionSelection() != pos) {
			isFilterOptionChanged = true;
		}

		appConfig.setMyCollectionSelection(pos);

		if (pos != -1 && isFilterOptionChanged) {
			setResult(RESULT_OK);
		} else {
			setResult(RESULT_CANCELED);
		}
		super.finish();
		overridePendingTransition(0, R.anim.push_out_to_bottom_discover);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
