package com.hungama.myplay.activity.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.TrackTrivia;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageCheckBox;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Utils;

import java.util.List;

public class TriviaCustomDialog extends Dialog {

	private TrackTrivia mTrackTrivia;
	private Track track;
	private Context context;

	public TriviaCustomDialog(Context context, TrackTrivia mTrackTrivia,
			Track track) {
		super(context);
		this.context = context;
		this.mTrackTrivia = mTrackTrivia;
		this.track = track;
	}

	private LanguageCheckBox cbDoNotShowAgain;
	private LanguageTextView tvSongName, tvAlbumName, tvTriviaDetails,
			tvBtnShowMeMore, tvBtnNotNow;
	private int currentTriviaPos = 0;
	private List<String> triviaList;

	// private Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.custom_player_trivia_dialog);

		// handler = new Handler();

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
		tvTriviaDetails = (LanguageTextView) findViewById(R.id.tvTriviaDetails);
		tvBtnShowMeMore = (LanguageTextView) findViewById(R.id.btnShowMeMore);
		tvBtnNotNow = (LanguageTextView) findViewById(R.id.btnNotNow);
		cbDoNotShowAgain = (LanguageCheckBox) findViewById(R.id.cbDoNotShowAgain);
		tvBtnShowMeMore
				.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						showNextTrivia();
					}
				});
		final ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(context);
		tvBtnNotNow.setOnClickListener(new android.view.View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (cbDoNotShowAgain.isChecked()) {
					mApplicationConfigurations.setTriviaShow(false);
					showThankPrompt();
				} else {
					mApplicationConfigurations.setTriviaShow(true);
					mApplicationConfigurations.increaseTriviaNotNowCount();
					if (mApplicationConfigurations.getTriviaNotNowCount() == 3) {
						mApplicationConfigurations.setTriviaShow(false);
						mApplicationConfigurations.resetTriviaNotNowCount();
					}
					OnApplicationStartsActivity.needToShowTriviaForSession = false;
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
											R.string.txt_trivia_btn_ok)));
						} else {
							tvBtnNotNow.setText(Utils.getMultilanguageText(
									context,
									context.getResources().getString(
											R.string.txt_trivia_btn_not_now)));
						}
					}
				});

		findViewById(R.id.tivia_dialog_close_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						dismiss();
					}
				});
		if(track!=null){
			tvSongName.setText(track.getTitle() + " (" + track.getAlbumName()
					+ ")");
			tvAlbumName.setText(track.getAlbumName());
		}
		if (mTrackTrivia != null)
			triviaList = mTrackTrivia.trivia;
		if (triviaList != null) {
			setTriviaText(triviaList.get(0).toString());
		}
	}

	@Override
	public void dismiss() {
		// handler.removeCallbacks(dismissPopup);
		super.dismiss();
	}

	@Override
	public void show() {
		super.show();
	}

	private void showNextTrivia() {
		// handler.removeCallbacks(dismissPopup);
		if (triviaList != null && currentTriviaPos + 1 < triviaList.size()) {
			currentTriviaPos++;
			setTriviaText(triviaList.get(currentTriviaPos).toString());
			if (currentTriviaPos == triviaList.size() - 1) {
				tvBtnShowMeMore.setEnabled(false);
			} else {
				tvBtnShowMeMore.setEnabled(true);
			}
		} else {
			Utils.makeText((Activity) context, "No more trivia available", 0)
					.show();
		}
	}

	private void setTriviaText(String text) {
		tvTriviaDetails.setText(text.trim());
	}

	// Runnable dismissPopup = new Runnable() {
	// @Override
	// public void run() {
	// if(isShowing())
	// dismiss();
	// }
	// };

	private void showThankPrompt() {
		CustomAlertDialog thankYouPrompt = new CustomAlertDialog(context);
		thankYouPrompt.setTitle(R.string.txt_prompt_thank_you_title);
		thankYouPrompt.setMessage(R.string.txt_prompt_thank_you_message);
		thankYouPrompt.setNegativeButton(R.string.txt_trivia_btn_ok, null);
		thankYouPrompt.show();
	}
}