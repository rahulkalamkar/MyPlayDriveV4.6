package com.hungama.myplay.activity.ui.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class PlayerInfoFragment extends Fragment {

	// private static final String TAG = "PlayerInfoFragment";

	public static final String FRAGMENT_ARGUMENT_MEDIA_TRACK_DETAILS = "fragment_argument_media_track_details";

	public interface OnInfoItemSelectedListener {

		public void onInfoItemSelected(String infoItemText,
									   String rowDescription);
	}

	public void setOnInfoItemSelectedListener(
			OnInfoItemSelectedListener listener) {
		mOnInfoItemSelectedListener = listener;
	}

	private OnInfoItemSelectedListener mOnInfoItemSelectedListener;
	private MediaTrackDetails mMediaTrackDetails = null;

	// private TextView mTextAlbum;
	// private TextView mTextLanguageCategory;
	// private TextView mTextMood;
	// private TextView mTextGenre;
	// private TextView mTextMusic;
	// private TextView mTextSingers;
	// private TextView mTextCast;
	// private TextView mTextLyrics;

	private LanguageTextView tvRow1Left;
	private LanguageTextView tvRow2Left;
	private LanguageTextView tvRow3Left;
	private LanguageTextView tvRow4Left;
	private LanguageTextView tvRow5Left;
	private LanguageTextView tvRow6Left;
	private LanguageTextView tvRow7Left;
	private LanguageTextView tvRow8Left;
	private LanguageTextView tvRow9Left;

	// private RelativeLayout infoPage;
	// private Button infoPageButton;
	// private Button shareButton;
	// private boolean infoWasClicked = false;
	private LinearLayout infoAlbum;
	private LinearLayout infoLanguageCategory;
	private LinearLayout infoMood;
	private LinearLayout infoGenre;
	private LinearLayout infoMusic;
	private LinearLayout infoSingers;
	private LinearLayout infoCast;
	private LinearLayout infoLyrics;
	private LinearLayout infoLabel;

	private String backgroundLink;
	private Drawable backgroundImage;
	// private FileCache fileCache;
	private Handler h;
	private View rootView;
	private Placement placement;

	private int width;

	private int dpi;
	private Track mTrack = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle data = getArguments();
		if (data != null) {
			mMediaTrackDetails = (MediaTrackDetails) data
					.getSerializable(FRAGMENT_ARGUMENT_MEDIA_TRACK_DETAILS);
			// gets the track to load.

			mTrack = (Track) data
					.getSerializable(PlayerTriviaFragment.FRAGMENT_ARGUMENT_TRACK);

		}
		Analytics.postCrashlitycsLog(getActivity(), PlayerInfoFragment.class.getName());
	}

	RelativeLayout rlMainInfo;
	ProgressBar progressBar1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_player_info, container,
				false);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}
		progressBar1 = (ProgressBar) rootView.findViewById(R.id.progressBar1);
		progressBar1.setVisibility(View.VISIBLE);
		rlMainInfo = (RelativeLayout) rootView.findViewById(R.id.rlMainInfo);
		rlMainInfo.setVisibility(View.GONE);
		ImageView iv_close = (ImageView) rootView
				.findViewById(R.id.ivDownArrow);
		iv_close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				getActivity().onBackPressed();
			}
		});

		try {
			// RelativeLayout rlMainInfo=(RelativeLayout)
			// rootView.findViewById(R.id.rlMainInfo1);
			if (android.os.Build.VERSION.SDK_INT > 15) {
				// // only for gingerbread and newer
				rootView.setBackground(PlayerBarFragment.blurbitmap);
				// rlFlipView.invalidate();
			} else {
				rootView.setBackgroundDrawable(PlayerBarFragment.blurbitmap);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		ImageView mButtonShare = (ImageView) rootView
				.findViewById(R.id.player_lyrics_title_bar_button_share);
		mButtonShare.setVisibility(View.GONE);

		LanguageTextView mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_title_bar_text);

		if (mTrack != null)
			mTextTitle.setText(mTrack.getTitle());

		LanguageTextView mTextSubTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_sub_title_bar_text);
		mTextSubTitle.setText(Utils.getMultilanguageText(getActivity(),
				getString(R.string.video_player_info_page_top_row_upper_text)));

		CampaignsManager mCampaignsManager = CampaignsManager
				.getInstance(getActivity());
		placement = mCampaignsManager
				.getPlacementOfType(PlacementType.PLAYER_INFO);
		if (placement == null) {
			rootView.findViewById(R.id.llPlayerInfoAdHolder).setVisibility(
					View.GONE);
			commonHandler.postDelayed(runIfNoAds, 250);
			return rootView;
		}
		if (!Utils.isConnected()) {
			rootView.findViewById(R.id.llPlayerInfoAdHolder).setVisibility(
					View.GONE);
		}
		commonHandler.postDelayed(runIfAds, 250);
		return rootView;
	}

	@Override
	public void onResume() {
		showHideLoader(false);
		super.onResume();
	}

	public void showHideLoader(boolean needToShow) {
		if (needToShow)
			progressBar1.setVisibility(View.VISIBLE);
		else
			progressBar1.setVisibility(View.GONE);
	}

	Handler commonHandler = new Handler();
	Runnable runIfNoAds = new Runnable() {

		@Override
		public void run() {
			if (mMediaTrackDetails != null) {
				initializeUserControls(rootView);
				populateUserControls(rootView);
				rlMainInfo.setVisibility(View.VISIBLE);
			} else {
				rootView.setVisibility(View.INVISIBLE);
			}
			progressBar1.setVisibility(View.GONE);
		}
	};

	Runnable runIfAds = new Runnable() {

		@Override
		public void run() {
			try {
				DisplayMetrics metrics = new DisplayMetrics();

				// fileCache = new FileCache(getActivity());
				if (getActivity() != null)
					getActivity().getWindowManager().getDefaultDisplay()
							.getMetrics(metrics);

				width = metrics.widthPixels;
				dpi = metrics.densityDpi;
				backgroundLink = Utils.getDisplayProfile(metrics, placement);


				h = new Handler() {
					@SuppressWarnings("deprecation")
					public void handleMessage(android.os.Message msg) {
						if (backgroundImage != null) {
							try {

								backgroundImage = Utils.ResizeBitmap(
										getActivity(), dpi, width,
										backgroundImage);

								((ImageView) rootView
										.findViewById(R.id.ivHungamaPlayerInfo))
										.setVisibility(View.GONE);
								((ProgressBar) rootView
										.findViewById(R.id.pbHungamaPlayerInfo))
										.setVisibility(View.GONE);
								final ImageView adView = (ImageView) rootView
										.findViewById(R.id.ivAdPlayerInfo);

								adView.setBackgroundDrawable(backgroundImage);
								adView.setImageBitmap(null);
								// adView.setImageDrawable(backgroundImage);
								Utils.postViewEvent(getActivity(), placement);

								adView.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										try {
											Utils.performclickEvent(
													getActivity(), placement);
										} catch (Exception e) {
											Logger.printStackTrace(e);
										}
									}
								});
								adView.postDelayed(new Runnable() {
									public void run() {
										try {
											LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) adView
													.getLayoutParams();
											params.width = width;
											params.height = (backgroundImage
													.getIntrinsicHeight() * width)
													/ backgroundImage
													.getIntrinsicWidth();
											adView.setLayoutParams(params);
											adView.setVisibility(View.VISIBLE);
										} catch (Exception e) {
											Logger.printStackTrace(e);
										}
									}
								}, 100);
							} catch (Exception e) {
							} catch (Error e) {
							}
						} else {
							h.sendEmptyMessage(0);
						}
					}
				};

				if (backgroundLink != null) {
					// Log.i("AdURL", placement.getDisplayInfoLdpi());
					new Thread(new Runnable() {

						@Override
						public void run() {
							if (backgroundImage == null && getActivity() != null) {

								backgroundImage = Utils.getBitmap(
										getActivity(), width, backgroundLink);
							}
							if(h!=null)
								h.sendEmptyMessage(0);
						}
					}).start();
				}

				// sets a temporarily solution when the data is not available.
				// TODO: Sets an error message for not available information.
				if (mMediaTrackDetails != null) {
					initializeUserControls(rootView);
					populateUserControls(rootView);
					rlMainInfo.setVisibility(View.VISIBLE);
				} else {
					rootView.setVisibility(View.INVISIBLE);
				}
				progressBar1.setVisibility(View.GONE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private void initializeUserControls(View rootView) {

		// mTextAlbum = (TextView)
		// rootView.findViewById(R.id.textview_row_1_right);
		// mTextLanguageCategory = (TextView)
		// rootView.findViewById(R.id.textview_row_2_right);
		// mTextMood = (TextView)
		// rootView.findViewById(R.id.textview_row_3_right);
		// mTextGenre = (TextView)
		// rootView.findViewById(R.id.textview_row_4_right);
		// mTextMusic = (TextView)
		// rootView.findViewById(R.id.textview_row_5_right);
		// mTextSingers = (TextView)
		// rootView.findViewById(R.id.textview_row_6_right);
		// mTextCast = (TextView)
		// rootView.findViewById(R.id.textview_row_7_right);
		// mTextLyrics = (TextView)
		// rootView.findViewById(R.id.textview_row_8_right);
		try {
			tvRow1Left = (LanguageTextView) rootView
					.findViewById(R.id.textview_row_1_left);
			tvRow2Left = (LanguageTextView) rootView
					.findViewById(R.id.textview_row_2_left);
			tvRow3Left = (LanguageTextView) rootView
					.findViewById(R.id.textview_row_3_left);
			tvRow4Left = (LanguageTextView) rootView
					.findViewById(R.id.textview_row_4_left);
			tvRow5Left = (LanguageTextView) rootView
					.findViewById(R.id.textview_row_5_left);
			tvRow6Left = (LanguageTextView) rootView
					.findViewById(R.id.textview_row_6_left);
			tvRow7Left = (LanguageTextView) rootView
					.findViewById(R.id.textview_row_7_left);
			tvRow8Left = (LanguageTextView) rootView
					.findViewById(R.id.textview_row_8_left);
			tvRow9Left = (LanguageTextView) rootView
					.findViewById(R.id.textview_row_9_left);

			infoAlbum = (LinearLayout) rootView
					.findViewById(R.id.textview_row_1_right);
			// infoAlbum.setOnClickListener(this);
			infoLanguageCategory = (LinearLayout) rootView
					.findViewById(R.id.textview_row_2_right);
			// infoLanguageCategory.setOnClickListener(this);
			infoMood = (LinearLayout) rootView
					.findViewById(R.id.textview_row_3_right);
			// infoMood.setOnClickListener(this);
			infoGenre = (LinearLayout) rootView
					.findViewById(R.id.textview_row_4_right);
			// infoGenre.setOnClickListener(this);
			infoMusic = (LinearLayout) rootView
					.findViewById(R.id.textview_row_5_right);
			// infoMusic.setOnClickListener(this);
			infoSingers = (LinearLayout) rootView
					.findViewById(R.id.textview_row_6_right);
			// infoSingers.setOnClickListener(this);
			infoCast = (LinearLayout) rootView
					.findViewById(R.id.textview_row_7_right);
			// infoCast.setOnClickListener(this);
			infoLyrics = (LinearLayout) rootView
					.findViewById(R.id.textview_row_8_right);
			// infoLyrics.setOnClickListener(this);
			infoLabel = (LinearLayout) rootView
					.findViewById(R.id.textview_row_9_right);
			// infoLabel.setOnClickListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void populateUserControls(View rootView) {
		View seperator;
		try {
			if (!TextUtils.isEmpty(mMediaTrackDetails.getAlbumName())
					&& !TextUtils.isEmpty(mMediaTrackDetails.getReleaseYear())) {

				String albumAndYear = mMediaTrackDetails.getAlbumName() + " ("
						+ mMediaTrackDetails.getReleaseYear() + ")";
				setTextForTextViewButton(albumAndYear, infoAlbum, tvRow1Left
						.getText().toString(), true);
				// infoAlbum.setText();
			} else {
				hideTableRow(infoAlbum);
				seperator = (View) rootView.findViewById(R.id.seperator_1);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getLanguage())) {
				String language = mMediaTrackDetails.getLanguage();
				setTextForTextViewButton(language, infoLanguageCategory,
						tvRow2Left.getText().toString(), false);
				// infoLanguageCategory.setText(mMediaTrackDetails.getLanguage());
			} else {
				hideTableRow(infoLanguageCategory);
				seperator = (View) rootView.findViewById(R.id.seperator_2);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getMood())) {
				String mood = mMediaTrackDetails.getMood();
				setTextForTextViewButton(mood, infoMood, tvRow3Left.getText()
						.toString(), false);
				// infoMood.setText(mMediaTrackDetails.getMood());
			} else {
				hideTableRow(infoMood);
				seperator = (View) rootView.findViewById(R.id.seperator_3);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getGenre())) {
				String genre = mMediaTrackDetails.getGenre();
				setTextForTextViewButton(genre, infoGenre, tvRow4Left.getText()
						.toString(), false);
				// infoGenre.setText(mMediaTrackDetails.getGenre());
			} else {
				hideTableRow(infoGenre);
				seperator = (View) rootView.findViewById(R.id.seperator_4);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getMusicDirector())) {
				String musicDirector = mMediaTrackDetails.getMusicDirector();
				setTextForTextViewButton(musicDirector, infoMusic, tvRow5Left
						.getText().toString(), false);
				// infoMusic.setText(mMediaTrackDetails.getMusicDirector());
			} else {
				hideTableRow(infoMusic);
				seperator = (View) rootView.findViewById(R.id.seperator_5);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getSingers())) {
				String singers = mMediaTrackDetails.getSingers();
				setTextForTextViewButton(singers, infoSingers, tvRow6Left
						.getText().toString(), false);
			} else {
				hideTableRow(infoSingers);
				seperator = (View) rootView.findViewById(R.id.seperator_6);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getCast())) {
				String cast = mMediaTrackDetails.getCast();
				setTextForTextViewButton(cast, infoCast, tvRow7Left.getText()
						.toString(), false);
				// infoCast.setText(mMediaTrackDetails.getCast());
			} else {
				hideTableRow(infoCast);
				seperator = (View) rootView.findViewById(R.id.seperator_7);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getLyricist())) {
				String lyricist = mMediaTrackDetails.getLyricist();
				setTextForTextViewButton(lyricist, infoLyrics, tvRow8Left
						.getText().toString(), false);
				// infoLyrics.setText(mMediaTrackDetails.getLyricist());
			} else {
				hideTableRow(infoLyrics);
				seperator = (View) rootView.findViewById(R.id.seperator_8);
				seperator.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mMediaTrackDetails.getLabel())) {
				String label = mMediaTrackDetails.getLabel();
				setTextForTextViewButton(label, infoLabel, tvRow9Left.getText()
						.toString(), false, true);
				// infoLyrics.setText(mMediaTrackDetails.getLyricist());
			} else {
				hideTableRow(infoLabel);
				seperator = (View) rootView.findViewById(R.id.seperator_9);
				seperator.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	private void hideTableRow(View view) {
		LinearLayout tableRow = (LinearLayout) view.getParent();
		tableRow.setVisibility(View.GONE);
	}

	private void setTextForTextViewButton(String text, LinearLayout row,
										  String rowDescription, boolean isAlbum) {
		setTextForTextViewButton(text, row, rowDescription, isAlbum, false);
	}

	private void setTextForTextViewButton(String text, LinearLayout row,
										  String rowDescription, boolean isAlbum, boolean isInfo) {
		boolean isOneWord = true;
		LanguageTextView keywordButton = null;
		if (text.contains(",") && !isAlbum) {
			String[] parts = text.split(",");
			int i = 0;
			for (final String keyword : parts) {
				boolean lastPosition = i == parts.length - 1 ? true : false;
				if (lastPosition) {
					keywordButton = createTextViewButtonInfo(keyword,
							isOneWord, rowDescription, isInfo);
				} else {
					keywordButton = createTextViewButtonInfo(keyword,
							!isOneWord, rowDescription, isInfo);
				}
				row.addView(keywordButton);
				i++;
			}
		} else {
			keywordButton = createTextViewButtonInfo(text, isOneWord,
					rowDescription, isInfo);
			row.addView(keywordButton);
		}
		if (keywordButton != null && row.getId() == R.id.textview_row_9_right)
			keywordButton.setTextColor(Color.WHITE);
	}

	// private TextView createTextViewButtonInfo(final String keyword, boolean
	// isOneWord, final String rowDescription) {
	// return createTextViewButtonInfo(keyword,isOneWord, rowDescription,
	// false);
	// }

	private LanguageTextView createTextViewButtonInfo(final String keyword,
													  boolean isOneWord, final String rowDescription, boolean isInfo) {
		LanguageTextView keywordButton = new LanguageTextView(getActivity());
		if (!isInfo) {
			keywordButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mOnInfoItemSelectedListener != null) {

						Map<String, String> reportMap = new HashMap<String, String>();
						reportMap.put(
								FlurryConstants.FlurryKeys.WhichInfoTapped
										.toString(), rowDescription);
						reportMap.put(FlurryConstants.FlurryKeys.KeywordSearch
								.toString(), keyword);
						Analytics
								.logEvent(
										FlurryConstants.FlurryEventName.TappedOnInfoLink
												.toString(), reportMap);

						mOnInfoItemSelectedListener.onInfoItemSelected(keyword,
								rowDescription);
					}
					// openMainSearchFragment(keyword);
				}
			});
		}
		if (isOneWord) {
			keywordButton.setText(keyword);
		} else {
			keywordButton.setText(keyword + ",");
		}
		// if(!isInfo){
//		keywordButton.setTextAppearance(getActivity(),
//				R.style.playerBarFragmentItemTextColor);
//		keywordButton.setTextSize(getResources().getDimensionPixelSize(R.dimen.normal_text_size));
		keywordButton.setTextColor(getResources().getColorStateList(R.color.info_text_selector));
		keywordButton.setClickable(true);
		// } else{
		// keywordButton.setTextAppearance(getActivity(),
		// R.style.videoPlayeInfoRowLabelText);
		// }
		keywordButton.setTypeface(keywordButton.getTypeface(), Typeface.BOLD);
		keywordButton.setSingleLine(false);
		return keywordButton;
	}

	// @Override
	// public void onClick(View view) {
	// int viewId = view.getId();
	// if (viewId == R.id.textview_row_1_right
	// || viewId == R.id.textview_row_2_right
	// || viewId == R.id.textview_row_3_right
	// || viewId == R.id.textview_row_4_right
	// || viewId == R.id.textview_row_5_right
	// || viewId == R.id.textview_row_6_right
	// || viewId == R.id.textview_row_7_right
	// || viewId == R.id.textview_row_8_right) {
	//
	// if (mOnInfoItemSelectedListener != null) {
	// mOnInfoItemSelectedListener.onInfoItemSelected(
	// ((TextView) view).getText().toString(), "");
	// }
	// }
	// }

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	Button mDrawerActionInfo;

	public void setInfoButton(Button mDrawerActionInfo) {
		this.mDrawerActionInfo = mDrawerActionInfo;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (rootView != null) {
			try {
				int version = Integer.parseInt(""
						+ android.os.Build.VERSION.SDK_INT);
				Utils.unbindDrawables(rootView, version);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}
	}
}
