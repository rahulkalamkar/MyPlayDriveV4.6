package com.hungama.myplay.activity.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.CategoryTypeObject;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.Era;
import com.hungama.myplay.activity.data.dao.hungama.Genre;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoryGenre;
import com.hungama.myplay.activity.data.dao.hungama.Tempo;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.dialogs.EraSelectedDialog;
import com.hungama.myplay.activity.ui.dialogs.GenreSelectionDialogNew;
import com.hungama.myplay.activity.ui.dialogs.LanguageSelectedDialog;
import com.hungama.myplay.activity.ui.dialogs.MoodSelectedDialog;
import com.hungama.myplay.activity.ui.dialogs.TempoSelectedDialog;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DiscoveryPlayerContextMenuActivity extends ActionBarActivity
		implements OnClickListener, PrefrenceDialogListener {

	LinearLayout llEra, llTempo, llMood, llLanguage, llGenre, llArtist;
	TextView tvEra, tvTempo, tvMood, tvLanguage, tvGenre;// , tvArtist;

	Discover mDiscover;

	private DataManager mDataManager;
	// private List<Category> mCategories;
	private MusicCategoriesResponse musicCategoriesResponse;
	FragmentManager mFragmentManager;
	private int selectedMoodPosition=-1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_discovery_player_context);

		View rootView = findViewById(R.id.layoutToMove);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(this);
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, this);
		}

		mFragmentManager = getSupportFragmentManager();

		mDataManager = DataManager.getInstance(this);
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		Intent intent = this.getIntent();
		Bundle bundle = intent.getExtras();

		mDiscover = (Discover) bundle
				.getSerializable(DiscoveryActivity.ARGUMENT_MOOD);

		if (PlayerService.service != null && mDiscover == null)
			mDiscover = PlayerService.service.mDiscover;

		initializeComponents();
	}

	private void initializeComponents() {
		llEra = (LinearLayout) findViewById(R.id.llEra);
		llTempo = (LinearLayout) findViewById(R.id.llTempo);
		llMood = (LinearLayout) findViewById(R.id.llMood);
		llLanguage = (LinearLayout) findViewById(R.id.llLanguage);
		llGenre = (LinearLayout) findViewById(R.id.llGenre);
		llArtist = (LinearLayout) findViewById(R.id.llArtist);

		tvEra = (TextView) findViewById(R.id.tvEra);
		tvTempo = (TextView) findViewById(R.id.tvTempo);
		tvMood = (TextView) findViewById(R.id.tvMood);
		tvLanguage = (TextView) findViewById(R.id.tvLanguage);
		tvGenre = (TextView) findViewById(R.id.tvGenre);
		// tvArtist = (TextView) findViewById(R.id.tvArtist);

		llEra.setOnClickListener(this);
		llTempo.setOnClickListener(this);
		llMood.setOnClickListener(this);
		llLanguage.setOnClickListener(this);
		llGenre.setOnClickListener(this);
		llArtist.setOnClickListener(this);

		TextView btn_close = (TextView) findViewById(R.id.btn_close);
		btn_close.setOnClickListener(this);

		if (ApplicationConfigurations.getInstance(this)
				.getUserSelectedLanguage() == 0)
			btn_close.setText(getString(R.string.alert_dialog_close)
					.toUpperCase());
		else {
			btn_close.setText(DBOHandler.getTextFromDb(
					getString(R.string.alert_dialog_close), this));
		}

		fillUpContent();
	}

	private void fillUpContent() {
		if (mDiscover != null) {
			tvEra.setText(mDiscover.getEra().getFrom() + "-"
					+ mDiscover.getEra().getTo());
			tvTempo.setText(mDiscover.getTempos().get(0).name());
			if (mDiscover.getMood() != null)
				tvMood.setText(mDiscover.getMood().getName());

			// tvLanguage.setText(mDiscover.getCategories().get(0).getName());
			// if (mDiscover.getGenres() != null
			// && mDiscover.getGenres().size() > 0
			// && mDiscover.getGenres().get(0).getName() != null
			// && !mDiscover.getGenres().get(0).getName().equals("")) {
			//
			// tvGenre.setText(mDiscover.getGenres().get(0).getName());
			// } else
			// llGenre.setVisibility(View.GONE);
			//
			// if
			// (!mDiscover.getCategories().get(0).getName().equals("English"))
			// llGenre.setVisibility(View.GONE);
			// else
			// llGenre.setVisibility(View.VISIBLE);

			tvLanguage.setText(mDiscover.getCategory());
			if (mDiscover.getGenre() != null
					&& !mDiscover.getGenre().equals("")) {
				tvGenre.setText(mDiscover.getGenre());
			} else
				llGenre.setVisibility(View.GONE);

			if (mDiscover.getCategory() == null
					|| !mDiscover.getCategory().equals("English"))
				llGenre.setVisibility(View.GONE);
			else
				llGenre.setVisibility(View.VISIBLE);

			llArtist.setVisibility(View.GONE);
		}
	}

	private void callActivityResultBack() {
		if (isChange) {
			isChange = false;
			Intent new_intent = new Intent();
			new_intent.putExtra(DiscoveryActivity.ARGUMENT_MOOD,
					(Serializable) mDiscover);
			new_intent
					.setAction(DiscoveryActivity.ACTION_DISVERY_PREFERENCE_CHANGE);
			sendBroadcast(new_intent);
		}


		finish();
	}

	private void setClickEnable(boolean isClickEnable){
		this.isClickEnable=isClickEnable;
	}

	boolean isClickEnable=true;

	@Override
	public void onBackPressed() {
		callActivityResultBack();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_close:
			callActivityResultBack();
			break;
		case R.id.llEra:
			if (isClickEnable && mDiscover != null) {
				setClickEnable(false);
				Era era = mDiscover.getEra();
				if (era == null) {
					era = new Era(Era.getDefaultFrom(), Era.getDefaultTo());
				}
				EraSelectedDialog dialogn = new EraSelectedDialog();
				dialogn.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						setClickEnable(true);
					}
				});
                dialogn.init(era, this);
				dialogn.show(mFragmentManager, ShareDialogFragment.FRAGMENT_TAG);
			}
			break;
		case R.id.llTempo:

			if (isClickEnable && mDiscover != null) {
				setClickEnable(false);
				TempoSelectedDialog dialogn_tempo = new TempoSelectedDialog();
				dialogn_tempo.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						setClickEnable(true);
					}
				});
                dialogn_tempo.init(mDiscover.getTempos(), this);
				dialogn_tempo.show(mFragmentManager, "tempo dialog");
			}
			break;
		case R.id.llMood:
			if (isClickEnable && mDiscover != null) {
				setClickEnable(false);
				// MoodSelectedDialog dialog_lang = new MoodSelectedDialog(this,
				// this);
				MoodSelectedDialog editNameDialog = MoodSelectedDialog
						.newInstance();
				editNameDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						setClickEnable(true);
					}
				});
				editNameDialog.setLangData(this, this);
				// editNameDialog.openMoodDialog();
				editNameDialog.show(mFragmentManager, "MoodSelectedDialog");
			}
			break;
		case R.id.llLanguage:
			if (isClickEnable && mDiscover != null) {
				setClickEnable(false);
				LanguageSelectedDialog editNameDialog = LanguageSelectedDialog
						.newInstance();
				editNameDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						setClickEnable(true);
					}
				});
				// editNameDialog.setLangData(this, mCategories, this);
				final String preferencesResponse = mDataManager
						.getApplicationConfigurations()
						.getMusicPreferencesResponse();
				try {
					musicCategoriesResponse = new Gson().fromJson(
							preferencesResponse.toString(),
							MusicCategoriesResponse.class);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				editNameDialog.setLangData(this, musicCategoriesResponse, this);
				editNameDialog.show(mFragmentManager, "LanguageSelectedDialog");

				// LanguageSelectedDialog dialog_lang = new
				// LanguageSelectedDialog(
				// this, mCategories, this);
				// dialog_lang.getCategoriesAndShow();
			}
			break;
		case R.id.llGenre:
			if (isClickEnable && mDiscover != null) {
				final String preferencesResponse = mDataManager
						.getApplicationConfigurations()
						.getMusicPreferencesResponse();
				try {
					musicCategoriesResponse = new Gson().fromJson(
							preferencesResponse.toString(),
							MusicCategoriesResponse.class);
				} catch (JsonSyntaxException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
				List<MusicCategoryGenre> genres = musicCategoriesResponse
						.getGenres();
				if (genres != null && genres.size() > 0) {
					for (MusicCategoryGenre genre : genres) {
						String mCategory = mDiscover.getCategory();
						if (mCategory.equals(genre.getCategory())) {
							setClickEnable(false);
							showGenresForCategory(genre);
							return;
						}
					}
				}
			}
			break;
		case R.id.llArtist:
			break;

		default:
			break;
		}
	}

	@Override
	public void finish() {
		if(selectedMoodPosition!=-1) {
			Intent intent = new Intent(DiscoveryActivity.ACTION_DISCOVERY_CHANGE);
			intent.putExtra("selectedMood", selectedMoodPosition);
			sendBroadcast(intent);
		}
		setResult(RESULT_OK);
		super.finish();
		// overridePendingTransition(0, R.anim.push_out_to_bottom_discover);
	}

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

	// ======================================================
	// Fragment's Communication Manager callbacks.
	// ======================================================
	// CustomProgressDialog prgress;

	private List<Genre> getGenres(List<CategoryTypeObject> mCategoryTypeObjects) {
		if (!Utils.isListEmpty(mCategoryTypeObjects)) {
			List<Genre> genres = new ArrayList<Genre>();
			for (CategoryTypeObject categoryTypeObject : mCategoryTypeObjects) {
				if (categoryTypeObject.getType().equals(
						CategoryTypeObject.TYPE_GENRE)) {
					genres.add((Genre) categoryTypeObject);
				}
			}
			return genres;
		}
		return null;
	}

	private List<Category> getCategories(
			List<CategoryTypeObject> mCategoryTypeObjects) {
		if (!Utils.isListEmpty(mCategoryTypeObjects)) {
			List<Category> categories = new ArrayList<Category>();
			for (CategoryTypeObject categoryTypeObject : mCategoryTypeObjects) {
				if (categoryTypeObject.getType().equals(
						CategoryTypeObject.TYPE_CATEGORY)) {
					categories.add((Category) categoryTypeObject);
				}
			}
			return categories;
		}
		return null;
	}

	boolean isChange;

	@Override
	public void onTempoEditDialog(ArrayList<Tempo> temp) {
		isChange = true;
		mDiscover.setTempos(temp);
		fillUpContent();
	}

	@Override
	public void onEraEditDialog(Era era) {
		isChange = true;
		mDiscover.setEra(era);
		fillUpContent();
	}

	@Override
	public void onLangaugeEditDialog(String mCategory) {
		// if (mCategories != null && mCategories.size() > 0) {
		// isChange = true;
		// mDiscover.setCategories(getCategories(mCategories));
		// mDiscover.setGenres(getGenres(mCategories));
		// fillUpContent();
		// }
		setClickEnable(true);
		if (!TextUtils.isEmpty(mCategory)) {
			isChange = true;
			mDiscover.setCategory(mCategory);
			mDiscover.setGenre("");
			fillUpContent();
		}
	}

	private void showGenresForCategory(final MusicCategoryGenre categoryGenre) {
		GenreSelectionDialogNew genreSelectionDialogNew = GenreSelectionDialogNew
				.newInstance();

		genreSelectionDialogNew.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				setClickEnable(true);
			}
		});
		genreSelectionDialogNew.setLangData(
				DiscoveryPlayerContextMenuActivity.this,
				categoryGenre.getGenre(),
				new GenreSelectionDialogNew.GenereSelectionDialogListener() {
					@Override
					public void onGenreEditDialog(String genre) {
						if (genre != null) {
							isChange = true;
							mDiscover.setCategory(categoryGenre.getCategory());
							mDiscover.setGenre(genre);
							fillUpContent();
						}
					}
				});
		genreSelectionDialogNew.show(mFragmentManager, "GenreSelectionDialog");
	}

	@Override
	public void onMoodEditDialog(Mood mood,int position) {
		isChange = true;
		selectedMoodPosition=position;
		mDiscover.setMood(mood);
		fillUpContent();
	}

	@Override
	public void onGenreEditDialog(Genre genre) {
	}
}
