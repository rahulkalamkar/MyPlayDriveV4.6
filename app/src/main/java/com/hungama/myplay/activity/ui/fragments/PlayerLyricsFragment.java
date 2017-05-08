package com.hungama.myplay.activity.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.TrackLyrics;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.TrackLyricsOperation;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.ui.widgets.TwoStatesButton;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FileCache;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class PlayerLyricsFragment extends Fragment implements
		CommunicationOperationListener {

	private static final String TAG = "PlayerLyricsFragment";

	public static final String FRAGMENT_ARGUMENT_TRACK = "fragment_argument_track";

	private DataManager mDataManager;
	private Track mTrack = null;
	private TrackLyrics mTrackLyrics = null;
	private ProgressBar mProgressBar;

	private RelativeLayout mTitleBar;
	private LanguageTextView mTextTitle;
	private TextView mTextLyrics;
	private ImageView mButtonShare;

	private String backgroundLink;
	private Drawable backgroundImage;
	private FileCache fileCache;
	private static Handler h;
	private View rootView;
	private Placement placement;

	private int width;

	private int dpi;
	TwoStatesButton mDrawerActionTravia;

	public void setTraviaButton(TwoStatesButton mDrawerActionTravia) {
		this.mDrawerActionTravia = mDrawerActionTravia;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());

		// gets the track to load.
		Bundle data = getArguments();
		if (data != null && data.containsKey(FRAGMENT_ARGUMENT_TRACK)) {
			mTrack = (Track) data.getSerializable(FRAGMENT_ARGUMENT_TRACK);
		}
		Analytics.postCrashlitycsLog(getActivity(), PlayerLyricsFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_player_lyrics, container,
				false);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar1);
		ImageView ivDownArrow = (ImageView) rootView
				.findViewById(R.id.ivDownArrow);
		ivDownArrow.setOnClickListener(new OnClickListener() {

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

		CampaignsManager mCampaignsManager = CampaignsManager
				.getInstance(getActivity());
		placement = mCampaignsManager
				.getPlacementOfType(PlacementType.PLAYER_LYRICS);
		if (placement != null) {
			DisplayMetrics metrics = new DisplayMetrics();

			fileCache = new FileCache(getActivity());
			getActivity().getWindowManager().getDefaultDisplay()
					.getMetrics(metrics);
			width = metrics.widthPixels;
			dpi = metrics.densityDpi;
			//
			backgroundLink = Utils.getDisplayProfile(metrics, placement);

			if (backgroundLink != null) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							if (backgroundImage == null) {
								backgroundImage = Utils.getBitmap(
										getActivity(), width, backgroundLink);
							}
							h.sendEmptyMessage(0);
						} catch (Exception e) {
							Logger.printStackTrace(e);
						}
					}
				}).start();
			}
			h = new Handler() {
				@SuppressWarnings("deprecation")
				public void handleMessage(android.os.Message msg) {
					if (backgroundImage != null
							&& backgroundImage.getIntrinsicHeight() != 0) {
						try {
							backgroundImage = Utils.ResizeBitmap(getActivity(),
									dpi, width, backgroundImage);
							// Log.i("Size!", "Old width = " +
							// String.valueOf(backgroundImage.getIntrinsicWidth()));
							// int aspectRatio =
							// backgroundImage.getIntrinsicWidth()/backgroundImage.getIntrinsicHeight();
							// float density = (float)dpi/160;
							// Log.i("Size!", "Density: " +
							// String.valueOf(density));
							// Bitmap resized =
							// Bitmap.createScaledBitmap(((BitmapDrawable)backgroundImage).getBitmap(),
							// (int)(width * density), (int)((width *
							// density)/aspectRatio), false);
							final LinearLayout adView = (LinearLayout) rootView
									.findViewById(R.id.llPlayerLyricsAdHolder);
							adView.setVisibility(View.VISIBLE);
							((ProgressBar) rootView
									.findViewById(R.id.pbHungamaPlayerLyrics))
									.setVisibility(View.GONE);
							((ImageView) rootView
									.findViewById(R.id.ivHungamaPlayerLyrics))
									.setVisibility(View.GONE);
							adView.setBackgroundDrawable(backgroundImage);
							Utils.postViewEvent(getActivity(), placement);
							adView.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									try {
										Utils.performclickEvent(getActivity(),
												placement);

										// Intent browserIntent = new
										// Intent(Intent.ACTION_VIEW,
										// Uri.parse(placement.getActions().get(0).action));
										// startActivity(browserIntent);
									} catch (Exception e) {
										Logger.printStackTrace(e);
									}
								}
							});

							adView.postDelayed(new Runnable() {
								public void run() {
									LayoutParams params = (LayoutParams) adView
											.getLayoutParams();
									params.width = width;
									params.height = (backgroundImage
											.getIntrinsicHeight() * width)
											/ backgroundImage
													.getIntrinsicWidth();
									adView.setLayoutParams(params);
									adView.setVisibility(View.VISIBLE);
								}
							}, 100);
						} catch (Exception e) {
						} catch (Error e) {
						}
						// Log.i("Size!", "New width = " + String.valueOf((new
						// BitmapDrawable(resized)).getIntrinsicWidth()));
					} else {
						h.sendEmptyMessage(0);
					}
				}
			};
		} else {
			rootView.findViewById(R.id.llPlayerLyricsAdHolder).setVisibility(
					View.GONE);
		}

		mTitleBar = (RelativeLayout) rootView
				.findViewById(R.id.player_lyrics_title_bar);
		mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_title_bar_text);
		mTextLyrics = (TextView) rootView.findViewById(R.id.player_lyrics_text);
		mButtonShare = (ImageView) rootView
				.findViewById(R.id.player_lyrics_title_bar_button_share);

		mTextTitle.setText(mTrack.getTitle());

		mTitleBar.setVisibility(View.INVISIBLE);

		mButtonShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				try {
					// Prepare data for ShareDialogFragmnet
					Map<String, Object> shareData = new HashMap<String, Object>();
					shareData.put(ShareDialogFragment.TITLE_DATA,
							mTrack.getTitle());
					shareData.put(ShareDialogFragment.SUB_TITLE_DATA,
							mTrack.getAlbumName());
					// shareData.put(ShareDialogFragment.THUMB_URL_DATA,
					// mTrack.getBigImageUrl());
					shareData.put(ShareDialogFragment.THUMB_URL_DATA,
							ImagesManager.getMusicArtBigImageUrl(mTrack
									.getImagesUrlArray()));
					shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA,
							MediaType.TRACK);
					shareData.put(ShareDialogFragment.EDIT_TEXT_DATA,
							mTextLyrics.getText());
					shareData.put(ShareDialogFragment.CONTENT_ID_DATA,
							mTrack.getId());
					shareData.put(ShareDialogFragment.TYPE_DATA,
							ShareDialogFragment.LYRICS);

					// Show ShareFragmentActivity
					ShareDialogFragment shareDialogFragment = ShareDialogFragment
							.newInstance(shareData,
									FlurryConstants.FlurryShare.Lyrics
											.toString());

					FragmentManager mFragmentManager = getFragmentManager();
					shareDialogFragment.show(mFragmentManager,
							ShareDialogFragment.FRAGMENT_TAG);
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
			}
		});

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mTrack != null && mTrackLyrics == null) {
			mDataManager.getTrackLyrics(mTrack, this);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();

		mDataManager.cancelGetTrackLyrics();
		if (mProgressBar != null
				&& mProgressBar.getVisibility() == View.VISIBLE) {
			mProgressBar.setVisibility(View.GONE);
		}
//		System.gc();
//		System.runFinalization();
	}

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.TRACK_LYRICS) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.TRACK_LYRICS) {
				mTrackLyrics = (TrackLyrics) responseObjects
						.get(TrackLyricsOperation.RESPONSE_KEY_TRACK_LYRICS);
				if (mTrackLyrics != null) {
					mTitleBar.setVisibility(View.VISIBLE);

					mTextLyrics.setText(mTrackLyrics.getLyrics());
				}
				mProgressBar.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.TRACK_TRIVIA) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

}
