package com.hungama.myplay.activity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoryGenre;
import com.hungama.myplay.activity.data.dao.hungama.MyPreferencesResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.PreferencesSaveOperation;
import com.hungama.myplay.activity.ui.dialogs.GenreSelectionDialogNew;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransperentActivity extends FragmentActivity implements
		OnClickListener/*
						 * , OnCheckedChangeListener
						 */{

	CheckBox checkbox2, checkbox3, checkbox4;
	boolean isRadioFilter = false;
	LinearLayout llGenre;
	DataManager mDataManager;
	TextView tvGenre;
	LanguageTextView btnclose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		// getWindow().getAttributes().windowAnimations =
		// R.style.bottom_to_top_animation;
		setContentView(R.layout.activity_transperent);
		mDataManager = DataManager.getInstance(this);
		View rootView = findViewById(R.id.layoutToMove);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(this);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, this);
		}

		Bundle b = getIntent().getExtras();
		if (b != null) {
			isRadioFilter = b.getBoolean("isRadioFilter");
		}

		findViewById(R.id.btn_close).setOnClickListener(this);
		// checkbox1 = (CheckBox)findViewById(R.id.checkbox1);
		btnclose = (LanguageTextView) findViewById(R.id.btn_close);
		checkbox2 = (CheckBox) findViewById(R.id.checkbox2);
		checkbox3 = (CheckBox) findViewById(R.id.checkbox3);
		checkbox4 = (CheckBox) findViewById(R.id.checkbox4);
		if (mApplicationConfigurations.getUserSelectedLanguage() == 0)
			btnclose.setText(getString(R.string.alert_dialog_close)
					.toUpperCase());
		else {
			btnclose.setText(DBOHandler.getTextFromDb(
					getString(R.string.alert_dialog_close), this));
		}
		llGenre = (LinearLayout) findViewById(R.id.llGenre);
		llGenre.setOnClickListener(this);

		tvGenre = (TextView) findViewById(R.id.tvGenre);
		tvGenre.setText(mDataManager.getApplicationConfigurations()
				.getSelctedMusicGenre());

		// checkbox2.setOnCheckedChangeListener(this);
		// checkbox3.setOnCheckedChangeListener(this);
		// checkbox4.setOnCheckedChangeListener(this);

		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(this);

		if (isRadioFilter) {
			if (appConfig.getFilterLiveRadioOption())
				checkbox2.setChecked(true);
			if (appConfig.getFilterCelebRadioOption())
				checkbox3.setChecked(true);
			checkbox2.setText(getString(R.string.radio_live_radio));
			checkbox3.setText(getString(R.string.radio_top_artist_radio));
			checkbox4.setVisibility(View.GONE);
		} else {
			if (appConfig.getFilterSongsOption())
				checkbox2.setChecked(true);
			if (appConfig.getFilterAlbumsOption())
				checkbox3.setChecked(true);
			if (appConfig.getFilterPlaylistsOption())
				checkbox4.setChecked(true);
		}

		final String preferencesResponse = mDataManager
				.getApplicationConfigurations().getMusicPreferencesResponse();
		try {
			musicCategoriesResponse = new Gson().fromJson(
					preferencesResponse.toString(),
					MusicCategoriesResponse.class);

			if (musicCategoriesResponse != null) {
				List<MusicCategoryGenre> genres = musicCategoriesResponse
						.getGenres();
				if (genres != null && genres.size() > 0) {
					String storePreference = mDataManager
							.getApplicationConfigurations()
							.getSelctedMusicPreference();
					for (MusicCategoryGenre genre : genres) {
						if (storePreference.equals(genre.getCategory())) {
							findViewById(R.id.llGenre).setVisibility(
									View.VISIBLE);
							break;
						}
					}
				}
			}
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
		finish();
	}

	private MusicCategoriesResponse musicCategoriesResponse;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_close:
			if (isChange) {
				mDataManager.getApplicationConfigurations()
						.setSelctedMusicGenre(selected_genre);
				mDataManager.saveMyPreferences("", preferenceOperation,
						mDataManager.getApplicationConfigurations()
								.getSelctedMusicPreference(), selected_genre);
			} else
				finish();

			break;
		case R.id.llGenre:

			final String preferencesResponse = mDataManager
					.getApplicationConfigurations()
					.getMusicPreferencesResponse();
			try {
				if (musicCategoriesResponse == null)
					musicCategoriesResponse = new Gson().fromJson(
							preferencesResponse.toString(),
							MusicCategoriesResponse.class);
				List<MusicCategoryGenre> genres = musicCategoriesResponse
						.getGenres();
				if (genres != null && genres.size() > 0) {
					String storePreference = mDataManager
							.getApplicationConfigurations()
							.getSelctedMusicPreference();
					for (MusicCategoryGenre genre : genres) {
						if (storePreference.equals(genre.getCategory())) {
							showGenresForCategory(genre);
							return;
						}
					}
				}
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		default:
			break;
		}
	}

	boolean isChange;
	String selected_genre = "";

	private void showGenresForCategory(final MusicCategoryGenre categoryGenre) {
		GenreSelectionDialogNew genreSelectionDialogNew = GenreSelectionDialogNew
				.newInstance();
		genreSelectionDialogNew.setLangData(TransperentActivity.this,
				categoryGenre.getGenre(),
				new GenreSelectionDialogNew.GenereSelectionDialogListener() {
					@Override
					public void onGenreEditDialog(String genre) {
						if (genre != null) {
							if (!genre.equalsIgnoreCase(mDataManager
									.getApplicationConfigurations()
									.getSelctedMusicGenre())) {
								isChange = true;
								selected_genre = genre;
								tvGenre.setText(genre);
							}
						}
					}
				});
		FragmentManager mFragmentManager = getSupportFragmentManager();
		genreSelectionDialogNew.show(mFragmentManager, "GenreSelectionDialog");
	}

	private static final int SUCCESS = 1;
	private CommunicationOperationListener preferenceOperation = new CommunicationOperationListener() {
		@Override
		public void onSuccess(int operationId,
				final Map<String, Object> responseObjects) {
			try {

				if (operationId == OperationDefinition.Hungama.OperationId.PREFERENCES_SAVE) {
					Logger.i("transparent activity",
							"Successed saving my preferences.");

					MyPreferencesResponse myPreferencesResponse = (MyPreferencesResponse) responseObjects
							.get(PreferencesSaveOperation.RESPONSE_KEY_PREFERENCES_SAVE);

					if (myPreferencesResponse.getCode() == SUCCESS
							|| myPreferencesResponse.getCode() == 200) {

						Set<String> tags = Utils.getTags();
						for (MusicCategoryGenre category : musicCategoriesResponse
								.getGenres()) {
							for (String genreName : category.getGenre()) {
								tags.remove("genre_" + genreName);
							}
						}
						if (!TextUtils.isEmpty(selected_genre)) {
							tags.add("genre_" + selected_genre);
						}
						Utils.AddTag(tags);

						Map<String, String> reportMap2 = new HashMap<String, String>();
						reportMap2.put(
								FlurryConstants.FlurryKeys.Source.toString(),
								"Context Menu");
						reportMap2.put(FlurryConstants.FlurryKeys.NameOfGenre
								.toString(), selected_genre);
						Analytics
								.logEvent(
										FlurryConstants.FlurryEventName.ContextMenuGenre
												.toString(), reportMap2);

						if (!isFinishing()) {
							Utils.makeText(
									TransperentActivity.this,
									getResources()
											.getString(
													R.string.my_preferences_saved_categories),
									Toast.LENGTH_LONG).show();

						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Logger.printStackTrace(e);
			}
			finish();
		}

		@Override
		public void onStart(int operationId) {
			Logger.s("Operation id :::: " + operationId);
		}

		@Override
		public void onFailure(int operationId, ErrorType errorType,
				String errorMessage) {
			Logger.s("Operation id :::: " + operationId);
			finish();
		}
	};

	@Override
	public void finish() {
		if (!checkbox2.isChecked() && !checkbox3.isChecked()
				&& !checkbox4.isChecked()) {
			Utils.makeText(this,
					getString(R.string.message_filter_no_option_selected),
					Toast.LENGTH_SHORT).show();
			return;
		}

		boolean isFilterOptionChanged = false;
		ApplicationConfigurations appConfig = ApplicationConfigurations
				.getInstance(this);

		if (isRadioFilter) {
			if (checkbox2.isChecked() && !appConfig.getFilterLiveRadioOption()) {
				appConfig.setFilterLiveRadioOption(true);
				isFilterOptionChanged = true;
			} else if (!checkbox2.isChecked()
					&& appConfig.getFilterLiveRadioOption()) {
				appConfig.setFilterLiveRadioOption(false);
				isFilterOptionChanged = true;
			}

			if (checkbox3.isChecked() && !appConfig.getFilterCelebRadioOption()) {
				appConfig.setFilterCelebRadioOption(true);
				isFilterOptionChanged = true;
			} else if (!checkbox3.isChecked()
					&& appConfig.getFilterCelebRadioOption()) {
				appConfig.setFilterCelebRadioOption(false);
				isFilterOptionChanged = true;
			}
		} else {
			if (checkbox2.isChecked() && !appConfig.getFilterSongsOption()) {
				appConfig.setFilterSongsOption(true);
				isFilterOptionChanged = true;
			} else if (!checkbox2.isChecked()
					&& appConfig.getFilterSongsOption()) {
				appConfig.setFilterSongsOption(false);
				isFilterOptionChanged = true;
			}

			if (checkbox3.isChecked() && !appConfig.getFilterAlbumsOption()) {
				appConfig.setFilterAlbumsOption(true);
				isFilterOptionChanged = true;
			} else if (!checkbox3.isChecked()
					&& appConfig.getFilterAlbumsOption()) {
				appConfig.setFilterAlbumsOption(false);
				isFilterOptionChanged = true;
			}

			if (checkbox4.isChecked() && !appConfig.getFilterPlaylistsOption()) {
				appConfig.setFilterPlaylistsOption(true);
				isFilterOptionChanged = true;
			} else if (!checkbox4.isChecked()
					&& appConfig.getFilterPlaylistsOption()) {
				appConfig.setFilterPlaylistsOption(false);
				isFilterOptionChanged = true;
			}
		}

		if (isFilterOptionChanged) {

			String selectedOptions = "";
			if (appConfig.getFilterSongsOption())
				selectedOptions += "Songs";
			if (appConfig.getFilterAlbumsOption())
				selectedOptions += (selectedOptions.length() > 0 ? ", " : "")
						+ "Albums";
			if (appConfig.getFilterPlaylistsOption())
				selectedOptions += (selectedOptions.length() > 0 ? ", " : "")
						+ "Playlists";

			Map<String, String> map = new HashMap<String, String>();
			map.put(FlurryConstants.FlurryKeys.OptionSelected.toString(),
					selectedOptions);
			if (getIntent().getBooleanExtra("isMusicNew", false))
				Analytics.logEvent(
						FlurryConstants.FlurryEventName.ContextMenuonMusicNew
								.toString(), map);
			else
				Analytics
						.logEvent(
								FlurryConstants.FlurryEventName.ContectMenuonMusicPopular
										.toString(), map);

			setResult(RESULT_OK);
		} else if (isChange) {
			Intent i = new Intent();
			i.putExtra("isChangeGenre", "");
			setResult(RESULT_OK, i);

		} else {
			setResult(RESULT_CANCELED);
		}
		super.finish();
		overridePendingTransition(0, R.anim.push_out_to_bottom_discover);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// getWindow().getAttributes().windowAnimations =
		// R.style.bottom_to_top_animation;
	}

	// @Override
	// public void onCheckedChanged(CompoundButton buttonView, boolean
	// isChecked) {
	// ApplicationConfigurations appConfig = new
	// ApplicationConfigurations(this);
	// if(buttonView.getId()==R.id.checkbox2){
	// if(isChecked)
	// appConfig.setFilterSongsOption(true);
	// else
	// appConfig.setFilterSongsOption(false);
	// } else if(buttonView.getId()==R.id.checkbox3){
	// if(isChecked)
	// appConfig.setFilterAlbumsOption(true);
	// else
	// appConfig.setFilterAlbumsOption(false);
	// } else if(buttonView.getId()==R.id.checkbox4){
	// if(isChecked)
	// appConfig.setFilterPlaylistsOption(true);
	// else
	// appConfig.setFilterPlaylistsOption(false);
	// }
	// }

	@Override
	protected void onStart() {
		super.onStart();
		Analytics.startSession(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		Analytics.onEndSession(this);
	}
}
