package com.hungama.myplay.activity.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.MessageFromResponse;
import com.hungama.myplay.activity.data.dao.hungama.PromoUnit;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.HashTagListOperation;
import com.hungama.myplay.activity.operations.hungama.WebRadioOperation;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class BrowseRadioFragment extends Fragment implements OnClickListener,
		CommunicationOperationListener, OnMediaItemOptionSelectedListener {

	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;
	private Button mTabButtonLiveRadio;
	private Button mTabButtonTopArtistsRadio;
	private View viewRadioGrid;
	// private ListView listViewRadio;

	public List<MediaItem> mMediaItemsLiveRadio = null;
	public List<MediaItem> mMediaItemsTopArtists = null;
	public List<MediaItem> mMediaItemsDisplay = null;
	// public static boolean isTopCeleb;

	public static MediaCategoryType mCurrentMediaCategoryType = MediaCategoryType.LIVE_STATIONS;

	private Stack<Integer> mDataLoadingCountDown = null;
	private static final int COUNT_DONW_MAX = 2;

	private boolean enableListView = true;

	// ======================================================
	// FRAGMENT'S LIFE CYCLE.
	// ======================================================

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (onCreate) {
			onCreate = false;
			if (mMediaItemsLiveRadio == null) {
				// loads the Live Radio items.
				String timestamp_cache = mDataManager
						.getApplicationConfigurations().getLiveRadioTimeStamp();
				mDataManager.getRadioLiveStations(this, timestamp_cache);
			}

			if (mMediaItemsLiveRadio == null) {
				// loads the Top Artist items.
				String timestamp_cache = mDataManager
						.getApplicationConfigurations()
						.getOnDemandRadioTimeStamp();
				mDataManager.getRadioTopArtists(this, timestamp_cache);
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());

		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		setRetainInstance(true);
		/*
		 * This fragment loads the Live Radio and Top Artists asynchronously at
		 * the same time. To handle the "Loading" indication correctly, this
		 * fragment uses a count down that when it's size will reach zero, we
		 * will know for sure that both web services has been called and now it
		 * the time to show their data.
		 */
		mDataLoadingCountDown = new Stack<Integer>();
		mDataLoadingCountDown
				.add(OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS);
		mDataLoadingCountDown
				.add(OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS);
		onCreate = true;
		Analytics.postCrashlitycsLog(getActivity(), BrowseRadioFragment.class.getName());
	}

	View rootView;
	private boolean onCreate = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		try {
			if (rootView == null) {
				rootView = inflater.inflate(R.layout.fragment_radio, container,
						false);
				if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
					Utils.traverseChild(rootView, getActivity());
				}

				initializeControls(rootView);
			} else {
				ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
				parent.removeView(rootView);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return rootView;
	}

	boolean isRefresh;

	public void refreshData() {

		if (mMediaItemsTopArtists != null)
			mMediaItemsTopArtists = null;

		if (mMediaItemsLiveRadio != null)
			mMediaItemsLiveRadio = null;

		if (mMediaItemsDisplay != null)
			mMediaItemsDisplay = null;

		// mDataManager.getCacheManager().storeLiveRadioResponse("", null);
		// mDataManager.getCacheManager().storeCelebRadioResponse("", null);

		isRefresh = true;
		if (mMediaItemsLiveRadio == null) {
			// loads the Live Radio items.
			String timestamp_cache = mDataManager
					.getApplicationConfigurations().getLiveRadioTimeStamp();
			mDataManager.getRadioLiveStations(this, timestamp_cache);
		}

		if (mMediaItemsLiveRadio == null) {
			// loads the Top Artist items.
			String timestamp_cache = mDataManager
					.getApplicationConfigurations().getOnDemandRadioTimeStamp();
			mDataManager.getRadioTopArtists(this, timestamp_cache);
		}
	}

	public void reloadData() {
		// showLoadingDialog(R.string.application_dialog_loading_content);
		if (mMediaItemsTopArtists != null)
			mMediaItemsTopArtists = null;

		if (mMediaItemsLiveRadio != null)
			mMediaItemsLiveRadio = null;

		if (mMediaItemsDisplay != null)
			mMediaItemsDisplay = null;

		mDataManager.getCacheManager().storeLiveRadioResponse("", null);
		mDataManager.getCacheManager().storeCelebRadioResponse("", null);

		isRefresh = false;
		mDataLoadingCountDown = new Stack<Integer>();
		mDataLoadingCountDown
				.add(OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS);
		mDataLoadingCountDown
				.add(OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS);

		if (mMediaItemsLiveRadio == null) {
			// loads the Live Radio items.
			String timestamp_cache = mDataManager
					.getApplicationConfigurations().getLiveRadioTimeStamp();
			mDataManager.getRadioLiveStations(this, timestamp_cache);
		}

		if (mMediaItemsLiveRadio == null) {
			// loads the Top Artist items.
			String timestamp_cache = mDataManager
					.getApplicationConfigurations().getOnDemandRadioTimeStamp();
			mDataManager.getRadioTopArtists(this, timestamp_cache);
		}
	}

	@Override
	public void onStart() {
		super.onStart();

		if (getActivity().getIntent().getStringExtra("artist_id") != null
				|| getActivity().getIntent().getStringExtra("Station_ID") != null
				|| getActivity().getIntent().getBooleanExtra("top_celebs",
						false)) {
			mCurrentMediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;
		}

		Analytics.startSession(getActivity(), this);
	}

	@Override
	public void onStop() {
		super.onStop();

		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		if (viewId == R.id.connection_error_empty_view_button_play_offline) {
			try {
				((MainActivity) getActivity()).handleOfflineSwitchCase(true);
			} catch (Exception e) {
			}
			return;
		} else if (viewId == R.id.connection_error_empty_view_button_retry) {
			mDataLoadingCountDown = new Stack<Integer>();
			mDataLoadingCountDown
					.add(OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS);
			mDataLoadingCountDown
					.add(OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS);
			onStart();
			return;
		} else if (viewId == R.id.radio_tab_button_live) {
			selectLiveRadio();

		} else if (viewId == R.id.radio_tab_button_top_artist) {
			selectTopArtists();
		} else if (viewId == R.id.radio_filter_button_live) {
			if (mediaItemsGridFragment != null
					&& mediaItemsGridFragment.getGridView() != null
					&& mMediaItemsLiveRadio != null
					&& mMediaItemsLiveRadio.size() > 0) {
				LinearLayoutManager layoutManager = ((LinearLayoutManager) mediaItemsGridFragment
						.getGridView().getLayoutManager());
				layoutManager.setSmoothScrollbarEnabled(true);
				layoutManager.scrollToPositionWithOffset(0, 0);

				// mediaItemsGridFragment.getGridView().setSelection(0); //
				// smoothScrollToPosition(0);
				HashMap<String, String> flurryMap = new HashMap<String, String>();
				flurryMap.put(FlurryConstants.FlurryKeys.ButtonName.toString(),
						FlurryConstants.FlurryKeys.LiveRadio.toString());
				Analytics.logEvent(
						FlurryConstants.FlurryEventName.RadioTopButtons
								.toString(), flurryMap);

				mediaItemsGridFragment.getGridView().postDelayed(new Runnable() {
					@Override
					public void run() {
						postAd();
					}
				}, 500);
			}
		} else if (viewId == R.id.radio_filter_button_top_artist) {
			if (mediaItemsGridFragment != null
					&& mediaItemsGridFragment.getGridView() != null
					&& mMediaItemsLiveRadio != null
					&& mMediaItemsLiveRadio.size() > 0) {
				LinearLayoutManager layoutManager = ((LinearLayoutManager) mediaItemsGridFragment
						.getGridView().getLayoutManager());
				layoutManager.setSmoothScrollbarEnabled(true);
				layoutManager.scrollToPositionWithOffset(
						mMediaItemsLiveRadio.size(), 0);

				// mediaItemsGridFragment.getGridView().setSelection(
				// mMediaItemsLiveRadio.size());//
				// .smoothScrollToPositionFromTop(mMediaItemsLiveRadio.size(),
				// 0);
				HashMap<String, String> flurryMap = new HashMap<String, String>();
				flurryMap.put(FlurryConstants.FlurryKeys.ButtonName.toString(),
						FlurryConstants.FlurryKeys.CelebRadio.toString());
				Analytics.logEvent(
						FlurryConstants.FlurryEventName.RadioTopButtons
								.toString(), flurryMap);
				mediaItemsGridFragment.getGridView().postDelayed(new Runnable() {
					@Override
					public void run() {
						postAd();
					}
				}, 500);

			}
		}

		if (mMediaItemsLiveRadio == null && mMediaItemsTopArtists == null) {
			mDataLoadingCountDown = new Stack<Integer>();
			mDataLoadingCountDown
					.add(OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS);
			mDataLoadingCountDown
					.add(OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS);
			onStart();
		}
	}

	@Override
	public void onStart(int operationId) {

		if (operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS
				|| operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS) {
			/*
			 * shows the loading indication only if the count down is freshed
			 * new.
			 */
			if (mDataLoadingCountDown.size() == COUNT_DONW_MAX) {
				// showLoadingDialog(R.string.application_dialog_loading_content);
			}
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			if (operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS
					|| operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS) {

				if (getActivity() == null || getActivity().isFinishing()) {
					return;
				}

				if (responseObjects
						.containsKey(HashTagListOperation.RESULT_KEY_MESSAGE)) {
					MessageFromResponse message = (MessageFromResponse) responseObjects
							.get(HashTagListOperation.RESULT_KEY_MESSAGE);
					if (message.getShowMessage() == 1) {
						Utils.makeText(getActivity(), message.getMessageText(),
								Toast.LENGTH_SHORT).show();
					}
				}

				// gets the media items.
				List<MediaItem> mediaItems = (List<MediaItem>) responseObjects
						.get(WebRadioOperation.RESULT_KEY_OBJECT_MEDIA_ITEM);

				if (operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS) {
					// if(mMediaItemsLiveRadio==null)
					if (mMediaItemsLiveRadio != null)
						mMediaItemsLiveRadio.clear();

					mMediaItemsLiveRadio = new ArrayList<MediaItem>();
					// mMediaItemsLiveRadio.add(new MediaItem(-2, "Live Radio",
					// "", "", "", "", null, 0));
					for (MediaItem mediaItem : mediaItems)
						mediaItem.setMediaType(MediaType.LIVE);
					mMediaItemsLiveRadio.addAll(mediaItems);
					// mMediaItemsLiveRadio = mediaItems;
				} else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS) {
					// if(mMediaItemsTopArtists==null)
					if (mMediaItemsTopArtists != null)
						mMediaItemsTopArtists.clear();

					mMediaItemsTopArtists = new ArrayList<MediaItem>();
					// mMediaItemsTopArtists.add(new MediaItem(-2,
					// "Celeb Radio", "", "", "", "", null, 0));
					for (MediaItem mediaItem : mediaItems)
						mediaItem.setMediaType(MediaType.ARTIST);
					mMediaItemsTopArtists.addAll(mediaItems);
					// mMediaItemsTopArtists = mediaItems;
				}

				if (mMediaItemsLiveRadio != null
						&& mMediaItemsTopArtists != null) {
					List<MediaItem> tempList = new ArrayList<MediaItem>();
					tempList.addAll(mMediaItemsLiveRadio);
					for (MediaItem mediaItem : tempList) {
						if (mediaItem.getId() == -1 || mediaItem.getId() == -2) {
							mMediaItemsLiveRadio.remove(mediaItem);
						}
					}

					tempList = new ArrayList<MediaItem>();
					tempList.addAll(mMediaItemsTopArtists);
					for (MediaItem mediaItem : tempList) {
						if (mediaItem.getId() == -1 || mediaItem.getId() == -2) {
							mMediaItemsTopArtists.remove(mediaItem);
						}
					}

					Placement placementLiveRadio = CampaignsManager
							.getInstance(getActivity()).getPlacementOfType(
									PlacementType.LIVE_RADIO_BANNER);
					if (placementLiveRadio != null
							&& mMediaItemsLiveRadio != null) {
						for (int i = 4; i < mMediaItemsLiveRadio.size(); i += 5) {
							mMediaItemsLiveRadio
									.add(i,
											new MediaItem(
													-1,// i,
													"no",
													"no",
													"no",
													"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
													"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
													"no", 0, 0));
						}
					}

					Placement placementCelebRadio = CampaignsManager
							.getInstance(getActivity()).getPlacementOfType(
									PlacementType.DEMAND_RADIO_BANNER);
					if (placementCelebRadio != null
							&& mMediaItemsTopArtists != null) {
						for (int i = 4 - (mMediaItemsLiveRadio.size() % 5); i < mMediaItemsTopArtists
								.size(); i += 5) {
							mMediaItemsTopArtists
									.add(i,
											new MediaItem(
													-3,// i,
													"no",
													"no",
													"no",
													"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
													"https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
													"no", 0, 0));
						}
					}

					if (mMediaItemsLiveRadio.size() > 0)
						mMediaItemsLiveRadio.add(0, new MediaItem(-2,
								getString(R.string.radio_live_radio_capital),
								"", "", "", "", null, 0, 0));
					if (mMediaItemsTopArtists.size() > 0)
						mMediaItemsTopArtists
								.add(0,
										new MediaItem(
												-2,
												getString(R.string.radio_top_artist_radio_capital),
												"", "", "", "", null, 0, 0));

					// mMediaItemsLiveRadio.addAll(mMediaItemsTopArtists);
					if (mMediaItemsDisplay != null)
						mMediaItemsDisplay.clear();

					mMediaItemsDisplay = new ArrayList<MediaItem>();
					ApplicationConfigurations appConfig = ApplicationConfigurations
							.getInstance(getActivity());
					if (appConfig.getFilterLiveRadioOption()) {
						mMediaItemsDisplay.addAll(mMediaItemsLiveRadio);
					}
					if (appConfig.getFilterCelebRadioOption()) {
						mMediaItemsDisplay.addAll(mMediaItemsTopArtists);
					}

					if (!appConfig.getFilterLiveRadioOption()
							&& !appConfig.getFilterCelebRadioOption()) {
						mMediaItemsDisplay.addAll(mMediaItemsLiveRadio);
						mMediaItemsDisplay.addAll(mMediaItemsTopArtists);
					}

					setContentForLiveRadio();

					if (mMediaItemsTopArtists == null
							|| (mMediaItemsTopArtists != null && mMediaItemsTopArtists
									.size() == 0)) {
						rootView.findViewById(
								R.id.radio_filter_button_top_artist)
								.setEnabled(false);
						rootView.findViewById(
								R.id.radio_filter_button_top_artist)
								.setBackgroundColor(
										getResources()
												.getColor(
														R.color.bg_button_radio_list_filter_transparent));
					} else {
						rootView.findViewById(
								R.id.radio_filter_button_top_artist)
								.setEnabled(true);
						rootView.findViewById(
								R.id.radio_filter_button_top_artist)
								.setBackgroundDrawable(
										getResources()
												.getDrawable(
														R.drawable.background_radio_filter_button_selector));
					}
				}

				// pops our counter.
				if (!Utils.isListEmpty(mDataLoadingCountDown))
					mDataLoadingCountDown.pop();

				if (Utils.isListEmpty(mDataLoadingCountDown)) {

					// sets the Live Radio as current presented tab.
					if ((getActivity().getIntent().getStringExtra("artist_id") != null
							|| getActivity().getIntent().getStringExtra("Station_ID") != null)
							|| getActivity().getIntent().getBooleanExtra(
									"top_celebs", false)
							|| mCurrentMediaCategoryType == MediaCategoryType.TOP_ARTISTS_RADIO) {

						mTabButtonTopArtistsRadio.setSelected(true);
						// setContentForTopArtistRadio();
					} else {
						mTabButtonLiveRadio.setSelected(true);
						// setContentForLiveRadio();
					}

					// if(enableListView)
					// listViewRadio.setVisibility(View.VISIBLE);
					// else
					viewRadioGrid.setVisibility(View.VISIBLE);

					// hides the loading indicator.
					hideLoadingDialog();
				}
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATIONS
				|| operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS) {
			try {
				mDataLoadingCountDown.pop();
			} catch (Exception e) {
			}
			try {
				if (Utils.isListEmpty(mDataLoadingCountDown)) {
					hideLoadingDialog();

					viewRadioGrid.setVisibility(View.VISIBLE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// ======================================================
	// PRIVATE HELPER METHODS.
	// ======================================================

	private void initializeControls(View rootView) {

		((TextView) rootView.findViewById(R.id.txt_live_radio)).setText(Utils
				.getMultilanguageText(
						getActivity(),
						getResources().getString(
								R.string.radio_live_radio_capital))
				.toUpperCase());
		((TextView) rootView.findViewById(R.id.txt_top_artist)).setText(Utils
				.getMultilanguageText(
						getActivity(),
						getResources().getString(
								R.string.radio_top_artist_radio_capital))
				.toUpperCase());

		mTabButtonLiveRadio = (Button) rootView
				.findViewById(R.id.radio_tab_button_live);
		mTabButtonTopArtistsRadio = (Button) rootView
				.findViewById(R.id.radio_tab_button_top_artist);

		mTabButtonLiveRadio.setOnClickListener(this);
		mTabButtonTopArtistsRadio.setOnClickListener(this);

		// listViewRadio = (ListView)
		// rootView.findViewById(R.id.radio_list_view);
		viewRadioGrid = rootView.findViewById(R.id.radio_fragment_container);
		// if(enableListView){
		// listViewRadio.setVisibility(View.VISIBLE);
		// viewRadioGrid.setVisibility(View.GONE);
		// }else{
		viewRadioGrid.setVisibility(View.VISIBLE);
		// listViewRadio.setVisibility(View.GONE);
		// }

		rootView.findViewById(R.id.radio_filter_button_live)
				.setOnClickListener(this);
		rootView.findViewById(R.id.radio_filter_button_top_artist)
				.setOnClickListener(this);

		if(mMediaItemsDisplay==null){
			mMediaItemsDisplay = new ArrayList<>();
		}
		if(OnApplicationStartsActivity.mMediaItemsDisplay!=null && OnApplicationStartsActivity.mMediaItemsDisplay.size()>0 && mMediaItemsDisplay.size()==0)
		{
			mMediaItemsDisplay = new ArrayList<>(OnApplicationStartsActivity.mMediaItemsDisplay);
			mMediaItemsLiveRadio = new ArrayList<>(OnApplicationStartsActivity.mMediaItemsLiveRadio);
			mMediaItemsTopArtists = new ArrayList<>(OnApplicationStartsActivity.mMediaItemsTopArtists);
			OnApplicationStartsActivity.mMediaItemsDisplay.clear();
			OnApplicationStartsActivity.mMediaItemsLiveRadio.clear();
			OnApplicationStartsActivity.mMediaItemsTopArtists.clear();
		}
		setContentForLiveRadio();
	}


	public MediaTileListFragment mediaItemsGridFragment;

	private void setContentForLiveRadio() {
		mCurrentMediaCategoryType = MediaCategoryType.LIVE_STATIONS;

		if (enableListView) {
			// listViewRadio.setAdapter(new
			// RadioListAdapter(mMediaItemsLiveRadio));
			if (isRefresh) {
				isRefresh = false;
				mediaItemsGridFragment.setMediaItems(mMediaItemsDisplay);
			} else {
				if(mediaItemsGridFragment!=null){
					mediaItemsGridFragment.setMediaItems(mMediaItemsDisplay);
					if(getActivity().getIntent().getStringExtra("radio_id") != null
							|| getActivity().getIntent().getStringExtra("artist_id") != null
							|| getActivity().getIntent().getStringExtra("Station_ID") != null)
						mediaItemsGridFragment.updateDeepLink(mMediaItemsDisplay);
				}else {

					mediaItemsGridFragment = new MediaTileListFragment();
					mediaItemsGridFragment
							.setOnMediaItemOptionSelectedListener(this);

					Bundle arguments = new Bundle();
					arguments.putSerializable(
							MediaTileListFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS,
							(Serializable) mMediaItemsDisplay);
					arguments
							.putString(
									MediaTileListFragment.FLURRY_SUB_SECTION_DESCRIPTION,
									FlurryConstants.FlurrySubSectionDescription.LiveStationsRadio
											.toString());
					mediaItemsGridFragment.setArguments(arguments);

					FragmentTransaction fragmentTransaction = getChildFragmentManager()
							.beginTransaction();
					fragmentTransaction.replace(R.id.radio_fragment_container,
							mediaItemsGridFragment);
					if(Constants.IS_COMMITALLOWSTATE)
						fragmentTransaction.commitAllowingStateLoss();
					else
						fragmentTransaction.commit();
					setPromoUnit(mPromoUnit, mMediaItemsDisplay);
				}
			}

		} else {
			MediaTileGridFragment mediaItemsGridFragment = new MediaTileGridFragment();
			mediaItemsGridFragment.setOnMediaItemOptionSelectedListener(this);

			Bundle arguments = new Bundle();
			arguments.putSerializable(
					MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS,
					(Serializable) mMediaItemsLiveRadio);
			arguments
					.putString(
							MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
							FlurryConstants.FlurrySubSectionDescription.LiveStationsRadio
									.toString());
			mediaItemsGridFragment.setArguments(arguments);

			FragmentTransaction fragmentTransaction = getChildFragmentManager()
					.beginTransaction();
			fragmentTransaction.replace(R.id.radio_fragment_container,
					mediaItemsGridFragment);
			if(Constants.IS_COMMITALLOWSTATE)
				fragmentTransaction.commitAllowingStateLoss();
			else
				fragmentTransaction.commit();
		}
	}

	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem,
			int position) {
		MediaCategoryType mediaCategoryType = MediaCategoryType.LIVE_STATIONS;
		if (mediaItem.getMediaType() == MediaType.ARTIST)
			mediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;
		((HomeActivity) getActivity()).showDetailsOfRadio(mediaItem,
				mediaCategoryType);
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
			int position) {
		MediaCategoryType mediaCategoryType = MediaCategoryType.LIVE_STATIONS;
		if (mediaItem.getMediaType() == MediaType.ARTIST || mediaItem.getMediaType() == MediaType.ARTIST_OLD)
			mediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;
		((HomeActivity) getActivity()).showDetailsOfRadio(mediaItem,
				mediaCategoryType);

	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
			int position) {
		MediaCategoryType mediaCategoryType = MediaCategoryType.LIVE_STATIONS;
		if (mediaItem.getMediaType() == MediaType.ARTIST)
			mediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;
		((HomeActivity) getActivity()).showDetailsOfRadio(mediaItem,
				mediaCategoryType);
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
			int position) {
		MediaCategoryType mediaCategoryType = MediaCategoryType.LIVE_STATIONS;
		if (mediaItem.getMediaType() == MediaType.ARTIST)
			mediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;
		((HomeActivity) getActivity()).showDetailsOfRadio(mediaItem,
				mediaCategoryType);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
			int position) {
	}

	// ======================================================
	// Inner dialog
	// ======================================================

	private LoadingDialogFragment mLoadingDialogFragment = null;

	public void selectLiveRadio() {

		if (!mTabButtonLiveRadio.isSelected()) {
			mTabButtonLiveRadio.setSelected(true);
		}

		if (mTabButtonTopArtistsRadio.isSelected()) {
			mTabButtonTopArtistsRadio.setSelected(false);
		}

		setContentForLiveRadio();

		Analytics.logEvent("Live Radio");
	}

	public void selectTopArtists() {
		if (!mTabButtonTopArtistsRadio.isSelected()) {
			mTabButtonTopArtistsRadio.setSelected(true);
		}

		if (mTabButtonLiveRadio.isSelected()) {
			mTabButtonLiveRadio.setSelected(false);
		}

		Handler handler = new Handler();

		setContentForLiveRadio();

		handler.postDelayed(new Runnable() {
			@Override
			public void run() {

				rootView.findViewById(R.id.radio_filter_button_top_artist)
						.performClick();
			}
		}, 1000);
		// setContentForTopArtistRadio();

		Analytics.logEvent("Top Artist Radio");
	}

	protected void hideLoadingDialog() {
		try {
			if (mLoadingDialogFragment != null && getActivity() != null
					&& !getActivity().isFinishing()) {
				FragmentTransaction fragmentTransaction = getFragmentManager()
						.beginTransaction();
				fragmentTransaction.remove(mLoadingDialogFragment);
				fragmentTransaction.commitAllowingStateLoss();
				mLoadingDialogFragment = null;
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onMediaItemOptionSaveOfflineSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	public void postAd() {
		if (mediaItemsGridFragment != null)
			mediaItemsGridFragment.postAd();
	}

	@Override
	public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
			int position) {
		// TODO Auto-generated method stub
	}

	private PromoUnit mPromoUnit;

	public void setPromoUnit(PromoUnit mPromoUnit, List<MediaItem> mediaItems) {
		this.mPromoUnit = mPromoUnit;
		if (mediaItemsGridFragment != null)
			mediaItemsGridFragment.setPromoUnit(mPromoUnit, mediaItems);
	}

	public void showLiveRadio() {
		selectLiveRadio();
	}

	public void showArtistRadio() {
		selectTopArtists();
	}

	public void setGridView() {
		if (mediaItemsGridFragment != null)
			mediaItemsGridFragment.setGridView();
	}

	// public void removeTopMargin() {
	// setFilter();
	// }
}
