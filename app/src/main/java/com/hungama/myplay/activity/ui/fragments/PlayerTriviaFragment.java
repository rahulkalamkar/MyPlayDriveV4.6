package com.hungama.myplay.activity.ui.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.hungama.myplay.activity.data.dao.hungama.TrackTrivia;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.TrackTriviaOperation;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FileCache;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.HashMap;
import java.util.Map;

public class PlayerTriviaFragment extends Fragment implements
		CommunicationOperationListener {

	private static final String TAG = "PlayerTriviaFragment";

	public static final String FRAGMENT_ARGUMENT_TRACK = "fragment_argument_track";

	private DataManager mDataManager;
	private LayoutInflater mInflater;
	private ProgressBar mProgressBar;

	private Track mTrack = null;
	private TrackTrivia mTrackTrivia = null;

	private RelativeLayout mTitleBar;
	private LanguageTextView mTextTitle;
	private ListView mListBubbles;

	private BubblesAdapter mBubblesAdapter;

	private String backgroundLink;
	private Drawable backgroundImage;
	private FileCache fileCache;
	private static Handler h;
	private View rootView;
	private Placement placement;

	private int width;

	private int dpi;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mInflater = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);

		// gets the track to load.
		Bundle data = getArguments();
		if (data != null && data.containsKey(FRAGMENT_ARGUMENT_TRACK)) {
			mTrack = (Track) data.getSerializable(FRAGMENT_ARGUMENT_TRACK);
		}
		Analytics.postCrashlitycsLog(getActivity(), PlayerTriviaFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_player_trivia, container,
				false);
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}
		mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar1);
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

		CampaignsManager mCampaignsManager = CampaignsManager
				.getInstance(getActivity());
		placement = mCampaignsManager
				.getPlacementOfType(PlacementType.PLAYER_TRIVIA);
		if (placement == null) {
			rootView.findViewById(R.id.llPlayerTriviaAdHolder).setVisibility(
					View.GONE);

			mTitleBar = (RelativeLayout) rootView
					.findViewById(R.id.player_lyrics_title_bar);

			mListBubbles = (ListView) rootView
					.findViewById(R.id.player_trivia_list);

			mTextTitle = (LanguageTextView) rootView
					.findViewById(R.id.player_lyrics_title_bar_text);

			if (mTrack != null && !TextUtils.isEmpty(mTrack.getTitle()))
				mTextTitle.setText(mTrack.getTitle());

			LanguageTextView mTextSubTitle = (LanguageTextView) rootView
					.findViewById(R.id.player_lyrics_sub_title_bar_text);
			mTextSubTitle.setText(Utils.getMultilanguageText(getActivity(),
					getString(R.string.general_trivia)));

			mTitleBar.setVisibility(View.INVISIBLE);
			mListBubbles.setVisibility(View.INVISIBLE);

			return rootView;
		}

		DisplayMetrics metrics = new DisplayMetrics();

		fileCache = new FileCache(getActivity());
		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);
		width = metrics.widthPixels;
		dpi = metrics.densityDpi;
		backgroundLink = Utils.getDisplayProfile(metrics, placement);

		// switch(dpi){
		// case DisplayMetrics.DENSITY_LOW:
		// backgroundLink = placement.getDisplayInfoLdpi();
		// break;
		// case DisplayMetrics.DENSITY_MEDIUM:
		// backgroundLink = placement.getDisplayInfoMdpi();
		// break;
		// case DisplayMetrics.DENSITY_HIGH:
		// backgroundLink = placement.getDisplayInfoHdpi();
		// break;
		// case DisplayMetrics.DENSITY_XHIGH:
		// backgroundLink = placement.getDisplayInfoXdpi();
		// break;
		// }
		if (backgroundLink != null) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					if (backgroundImage == null) {

						backgroundImage = Utils.getBitmap(getActivity(), width,
								backgroundLink);
					}
					h.sendEmptyMessage(0);
				}
			}).start();
		}
		h = new Handler() {
			@SuppressWarnings("deprecation")
			public void handleMessage(android.os.Message msg) {
				if (backgroundImage != null) {
					try {
						Logger.i(
								"Size!",
								"Old width = "
										+ String.valueOf(backgroundImage
												.getIntrinsicWidth()));
						// int aspectRatio = backgroundImage.getIntrinsicWidth()
						// / backgroundImage.getIntrinsicHeight();

						// float density = (float) dpi / 160;
						// Log.i("Size!", "Density: " +
						// String.valueOf(density));
						// Bitmap resized =
						// Bitmap.createScaledBitmap(((BitmapDrawable)
						// backgroundImage).getBitmap(),
						// (int) (width * density),
						// (int) ((width * density) / aspectRatio),
						// false);
						backgroundImage = Utils.ResizeBitmap(getActivity(),
								dpi, width, backgroundImage);
						final LinearLayout adView = (LinearLayout) rootView
								.findViewById(R.id.llPlayerTriviaAdHolder);
						((ProgressBar) rootView
								.findViewById(R.id.pbHungamaPlayerTrivia))
								.setVisibility(View.GONE);
						((ImageView) rootView
								.findViewById(R.id.ivHungamaPlayerTrivia))
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
								try {
									LayoutParams params = (LayoutParams) adView
											.getLayoutParams();
									params.width = width;
									params.height = (backgroundImage
											.getIntrinsicHeight() * width)
											/ backgroundImage.getIntrinsicWidth();
									adView.setLayoutParams(params);
									adView.setVisibility(View.VISIBLE);
								}
								catch (ArithmeticException ae)
								{
									ae.printStackTrace();
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

		mTitleBar = (RelativeLayout) rootView
				.findViewById(R.id.player_lyrics_title_bar);

		mListBubbles = (ListView) rootView
				.findViewById(R.id.player_trivia_list);

		mTextTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_title_bar_text);

		if (mTrack != null && !TextUtils.isEmpty(mTrack.getTitle()))
			mTextTitle.setText(mTrack.getTitle());

		LanguageTextView mTextSubTitle = (LanguageTextView) rootView
				.findViewById(R.id.player_lyrics_sub_title_bar_text);
		mTextSubTitle.setText(getString(R.string.general_trivia));

		mTitleBar.setVisibility(View.INVISIBLE);
		mListBubbles.setVisibility(View.INVISIBLE);

		return rootView;
	}

	@Override
	public void onStart() {
		super.onStart();

		if (mTrack != null && mTrackTrivia == null) {
			mDataManager.getTrackTrivia(mTrack, this);
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		mDataManager.cancelGetTrackTrivia();
		if (mProgressBar != null
				&& mProgressBar.getVisibility() == View.VISIBLE) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	public void onStart(int operationId) {
		if (operationId == OperationDefinition.Hungama.OperationId.TRACK_TRIVIA) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.TRACK_TRIVIA) {
				mTrackTrivia = (TrackTrivia) responseObjects
						.get(TrackTriviaOperation.RESULT_KEY_OBJECT_TRACK_TRIVIA);
				if (mTrackTrivia != null) {
					// now we can make them visible.
					mTitleBar.setVisibility(View.VISIBLE);
					mListBubbles.setVisibility(View.VISIBLE);

					// title.
					// mTextTitle.setText(mTrackTrivia.title);

					// bubbles - let the party begin.
					mBubblesAdapter = new BubblesAdapter();
					mListBubbles.setAdapter(mBubblesAdapter);
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

	private class BubblesAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mTrackTrivia.trivia.size();
		}

		@Override
		public Object getItem(int position) {
			return mTrackTrivia.trivia.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {

			boolean isOddLocation = (position % 2 > 0) ? true : false;

			// if (isOddLocation) {
			// // inflates the left bubble.
			// convertView = mInflater.inflate(
			// R.layout.list_item_player_trivia_bubble_left,
			// container, false);
			//
			// } else {
			// inflates the right bubble.
			convertView = mInflater.inflate(
					R.layout.list_item_player_trivia_bubble_right, container,
					false);
			// }

			// populates the bubble's view with the trivia.
			TextView textTrivia = (TextView) convertView
					.findViewById(R.id.player_trivia_text);
			final String triviaString = (String) getItem(position);
			textTrivia.setText(triviaString);

			ImageView buttonShare = (ImageView) convertView
					.findViewById(R.id.player_trivia_button_share);
			// buttonShare.setText(Utils.getMultilanguageTextLayOut(getActivity(),
			// getString(R.string.general_capital_share)));
			buttonShare.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// Show ShareDialogFragment
					showShareDialogFragment(triviaString);
				}
			});

			return convertView;
		}
	}

	public void showShareDialogFragment(String textTrivia) {

		// Prepare data for ShareDialogFragmnet
		Map<String, Object> shareData = new HashMap<String, Object>();
		shareData.put(ShareDialogFragment.TITLE_DATA, mTrack.getTitle());
		shareData
				.put(ShareDialogFragment.SUB_TITLE_DATA, mTrack.getAlbumName());
		// shareData.put(ShareDialogFragment.THUMB_URL_DATA,
		// mTrack.getBigImageUrl());
		shareData.put(ShareDialogFragment.THUMB_URL_DATA, ImagesManager
				.getMusicArtBigImageUrl(mTrack.getImagesUrlArray()));
		shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA, MediaType.TRACK);
		shareData.put(ShareDialogFragment.EDIT_TEXT_DATA, textTrivia);
		shareData.put(ShareDialogFragment.CONTENT_ID_DATA, mTrack.getId());
		shareData
				.put(ShareDialogFragment.TYPE_DATA, ShareDialogFragment.TRIVIA);

		// Show ShareFragmentActivity
		ShareDialogFragment shareDialogFragment = ShareDialogFragment
				.newInstance(shareData,
						FlurryConstants.FlurryShare.Trivia.toString());

		FragmentManager mFragmentManager = getFragmentManager();
		shareDialogFragment.show(mFragmentManager,
				ShareDialogFragment.FRAGMENT_TAG);

	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	// private TwoStatesButton mDrawerActionTravia;

	// public void setTraviaButton(TwoStatesButton mDrawerActionTravia) {
	// this.mDrawerActionTravia = mDrawerActionTravia;
	// }
}
