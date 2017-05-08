package com.hungama.myplay.activity.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PromoUnit;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.adapters.MediaListAdapter;
import com.hungama.myplay.activity.ui.adapters.MyAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents {@link MediaItem}s in a grid.
 */
public class MediaTileListFragment extends MainFragment implements
		SwipeRefreshLayout.OnRefreshListener {

	private static final String TAG = "MediaTileListFragment";

	public static final String FRAGMENT_ARGUMENT_MEDIA_ITEMS = "fragment_argument_media_items";
	public static final String FLURRY_SUB_SECTION_DESCRIPTION = "flurry_sub_section_description";

	// private ListView mTilesGridView;
	// private MediaListAdapter mHomeMediaTilesAdapter;
	private RecyclerView mTilesGridView;
	private MediaListAdapter mHomeMediaTilesAdapter;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	private CampaignsManager mCampaignsManager;

	// ======================================================
	// PABLIC.
	// ======================================================

	/**
	 * Registers a callback to be invoked when the user has selected an action
	 * upon a {@link MediaItem}.
	 * 
	 * @param listener
	 */
	public void setOnMediaItemOptionSelectedListener(
			OnMediaItemOptionSelectedListener listener) {
		mOnMediaItemOptionSelectedListener = listener;
	}

	public void setMediaItems(List<MediaItem> mediaItems) {
		List<Object> temp = new ArrayList<Object>();
		if(mediaItems==null)
			mediaItems=new ArrayList<MediaItem>();
		temp.addAll(mediaItems);
		if (mPromoUnit != null && mHomeMediaTilesAdapter!=null
				&& (((HomeActivity) getActivity()).isPromoUnit())) {
			if (mHomeMediaTilesAdapter.getItemCount() > 0
					&& mHomeMediaTilesAdapter.getItemViewType(0) == MyAdapter.PROMOUNIT)
				temp.set(0, mPromoUnit);
			else
				temp.add(0, mPromoUnit);
		}
		if(mHomeMediaTilesAdapter!=null) {
			mHomeMediaTilesAdapter.setMediaItems(temp);
			mHomeMediaTilesAdapter.setGridView(mTilesGridView);
			mHomeMediaTilesAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Retrieves the adapter which binds the MediaItems with the GridView.
	 */
	// protected BaseAdapter getAdapter() {
	// return mHomeMediaTilesAdapter;
	// }

	// ======================================================
	// FRAGMENT'S LIFE CYCLE.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.postCrashlitycsLog(getActivity(), MediaTileListFragment.class.getName());
	}

	boolean isRefresh;

	private void onCreateSwipeToRefresh(SwipeRefreshLayout refreshLayout) {

		refreshLayout.setOnRefreshListener(this);

		refreshLayout.setColorScheme(android.R.color.holo_blue_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_green_light,
				android.R.color.holo_red_light);

		refreshLayout.setDistanceToTriggerSync(200);
	}

	@Override
	public void onRefresh() {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				try {
					mListViewContainer.setRefreshing(false);
					isRefresh = true;

					Intent new_intent = new Intent();
					new_intent.setAction(HomeActivity.ACTION_RADIO_DATA_CHANGE);

					getActivity().sendBroadcast(new_intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1000);
	}

	private SwipeRefreshLayout mListViewContainer;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mListViewContainer = new SwipeRefreshLayout(getActivity());
		// mTilesGridView = new ListView(getActivity());
		mTilesGridView = new RecyclerView(getActivity());
		// mTilesGridView.setDivider(new ColorDrawable(getResources().getColor(
		// R.color.player_queue_listview_seperator_color)));
		// mTilesGridView.setDividerHeight(1);

		mListViewContainer.addView(mTilesGridView);

		onCreateSwipeToRefresh(mListViewContainer);

		mCampaignsManager = CampaignsManager.getInstance(getActivity());

		initializeTiles();

		return mListViewContainer;
	}

	private void initializeTiles() {

		// sets the gird's properties.
		// mTilesGridView.setGravity(Gravity.CENTER_HORIZONTAL);
		// mTilesGridView.setVerticalSpacing(imageTileSpacing);
		// mTilesGridView.setNumColumns(GridView.AUTO_FIT);
		// mTilesGridView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mTilesGridView.setOverScrollMode(GridView.OVER_SCROLL_NEVER);
		}

		// sets the background.
		Logger.v(
				TAG,
				"The device build number is: "
						+ Integer.toString(Build.VERSION.SDK_INT));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mTilesGridView.setBackground(null);
		} else {
			mTilesGridView.setBackgroundDrawable(null);
		}

		// sets the gridview's cool margin.
		/*
		 * GridView.MarginLayoutParams params = new GridView.MarginLayoutParams(
		 * GridView.MarginLayoutParams.MATCH_PARENT,
		 * GridView.MarginLayoutParams.MATCH_PARENT);
		 * params.setMargins(imageTileSpacing, imageTileSpacing,
		 * imageTileSpacing, imageTileSpacing);
		 */

		// FrameLayout.LayoutParams params =
		// new FrameLayout.LayoutParams(
		// FrameLayout.LayoutParams.MATCH_PARENT,
		// FrameLayout.LayoutParams.MATCH_PARENT);
		//
		// params.leftMargin = imageTileSpacing;
		// params.topMargin = imageTileSpacing;
		// params.rightMargin = imageTileSpacing;
		// params.bottomMargin = imageTileSpacing;
		/*
		 * mTilesGridView.setLayoutParams(params); mTilesGridView.setPadding(0,
		 * imageTileSpacing, 0, imageTileSpacing);
		 */

		/*
		 * For placing the tiles correctly in the grid, calculates the maximum
		 * size that a tile can be and the column width.
		 */

		// measuring the device's screen width. and setting the grid column
		// width.
		Display display = getActivity().getWindowManager().getDefaultDisplay();
		int screenWidth = 0;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
			screenWidth = display.getWidth();
		} else {
			Point displaySize = new Point();
			display.getSize(displaySize);
			screenWidth = displaySize.x;
		}

		// mTilesGridView.setNumColumns(2);
		// mTilesGridView.setColumnWidth(mTileSize);

		/*
		 * gets the list of the media items from the arguments, and sets it as
		 * the source to the adapter.
		 */
		List<MediaItem> mediaItems = null;
		String flurrySubSectionDescription = "";
		// mHomeMediaTilesAdapter = null;

		Bundle data = getArguments();
		try {
			if (data != null
					&& data.containsKey(MediaTileListFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS)) {
				mediaItems = new ArrayList<MediaItem>();
				List<MediaItem> temp = (List<MediaItem>) data
						.getSerializable(MediaTileListFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS);
				if (temp == null)
					temp = new ArrayList<MediaItem>();
				mediaItems.addAll(temp);
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":211", e.toString());
		}

		// if (placement != null && mediaItems != null) {
		// for (int i = 3; i < mediaItems.size(); i += 6) {
		// Logger.i("Hint", String.valueOf(i));
		// // mediaItems.add(i, new MediaItem(i, "no", "no", "no",
		// // backgroundLink, backgroundLink, "album", 0));
		// mediaItems.add(i, new MediaItem(-1, "no", "no", "no",
		// backgroundLink, backgroundLink, "album", 0));
		// }
		// }
		// if (placement != null && mediaItems != null) {
		// for (int i = 4; i < mediaItems.size(); i += 5) {
		// mediaItems
		// .add(i,
		// new MediaItem(
		// -1,//i,
		// "no",
		// "no",
		// "no",
		// "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
		// "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQ1tMe2aczrJBma6ii3qgKhwzIGa-Vrf6lD1LyVimnxHaQ-oiWLsA",
		// "no", 0));
		// }
		// }
		// } else {
		// isMusicLoaded = false;
		// }

		if (data != null
				&& data.containsKey(MediaTileListFragment.FLURRY_SUB_SECTION_DESCRIPTION)) {
			flurrySubSectionDescription = data
					.getString(MediaTileListFragment.FLURRY_SUB_SECTION_DESCRIPTION);
		}

		// if(mediaItems==null){
		// LinearLayout emptyView = (LinearLayout)
		// getActivity().getLayoutInflater().
		// inflate(R.layout.layout_connection_error_empty_view, null);
		// // .findViewById(R.id.connection_error_empty_view);
		// Button retryButton = (Button) emptyView
		// .findViewById(R.id.connection_error_empty_view_button_retry);
		// retryButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// // if (mOnRetryButtonClickedLister != null) {
		// // mOnRetryButtonClickedLister.onRetryButtonClicked(view);
		// // }
		// }
		// });
		//
		// Button offlineButton = (Button) emptyView
		// .findViewById(R.id.connection_error_empty_view_button_play_offline);
		// offlineButton.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// try {
		// MainActivity act = (MainActivity) getActivity();
		// act.handleOfflineSwitchCase();
		// } catch (Exception e) {
		// }
		// }
		// });
		// mTilesGridView.setEmptyView(emptyView);
		// }
		mTilesGridView.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				// TODO Auto-generated method stub
				super.onScrolled(recyclerView, dx, dy);
			}

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView,
					int scrollState) {
				// TODO Auto-generated method stub
				if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
					PicassoUtil.with(getActivity()).resumeTag(
							PicassoUtil.PICASSO_RADIO_LIST_TAG);
					try {
						mHomeMediaTilesAdapter.postAdForPosition();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					PicassoUtil.with(getActivity()).pauseTag(
							PicassoUtil.PICASSO_RADIO_LIST_TAG);
				}
				super.onScrollStateChanged(recyclerView, scrollState);
			}
		});

		if (mHomeMediaTilesAdapter == null) {
			mHomeMediaTilesAdapter = new MediaListAdapter(getActivity(),
					mTilesGridView, this.getClass().getCanonicalName(), null,
					null, mCampaignsManager, mediaItems, false,
					flurrySubSectionDescription);
			if (mPromoUnit != null
					&& (((HomeActivity) getActivity()).isPromoUnit()))
				setMediaItems(mediaItems);
		}
		int scrollPosition = -1;
//		mHomeMediaTilesAdapter.setArtist_id(getActivity().getIntent()
//				.getStringExtra("artist_id"));
		Logger.i("First parent", "Chanell #"
				+ getActivity().getIntent().getStringExtra("radio_id"));// "channel_index"
		mHomeMediaTilesAdapter.setChannel_id(getActivity().getIntent()
				.getStringExtra("radio_id"));// "channel_index"
		mHomeMediaTilesAdapter
				.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(
				getActivity());
		mTilesGridView.setLayoutManager(mLayoutManager);
		mTilesGridView.setAdapter(mHomeMediaTilesAdapter);

		long id = 0;
		if (getActivity().getIntent().getStringExtra("radio_id") != null) {// "channel_index"
			id = Long.parseLong(getActivity().getIntent().getStringExtra(
					"radio_id"));// "channel_index"
		} else if (getActivity().getIntent().getStringExtra("artist_id") != null) {
			id = Long.parseLong(getActivity().getIntent().getStringExtra(
					"artist_id"));
		} else if (getActivity().getIntent().getStringExtra("Station_ID") != null) {
			id = Long.parseLong(getActivity().getIntent().getStringExtra(
					"Station_ID"));
		}
		if (mediaItems != null) {
			for (MediaItem mi : mediaItems) {
				if (mi.getId() == id) {
					scrollPosition = mediaItems.indexOf(mi);
					break;
				}
			}
			if (getActivity().getIntent()
					.getStringExtra("artist_id") != null) {
				scrollPosition = -1;
			}
			if (scrollPosition != -1) {
				if (getActivity().getIntent().getStringExtra("radio_id") != null) {// "channel_index"
					getActivity().getIntent().removeExtra("radio_id");// "channel_index"
					mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null)
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItems.get(scrollPosition),
										scrollPosition);

				} else if (getActivity().getIntent()
						.getStringExtra("artist_id") != null) {
					getActivity().getIntent().removeExtra("artist_id");
					mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null)
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItems.get(scrollPosition),
										scrollPosition);

				} else if (getActivity().getIntent()
						.getStringExtra("Station_ID") != null) {
					getActivity().getIntent().removeExtra("Station_ID");
					mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null)
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItems.get(scrollPosition),
										scrollPosition);

				}
			} else {
				if (getActivity().getIntent().getStringExtra("artist_id") != null) {
					getActivity().getIntent().removeExtra("artist_id");
					// mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null) {
						MediaItem mediaItem = new MediaItem(id, "Celeb Radio",
								"", "", "", "", MediaType.ARTIST_OLD.toString(), 0,
								0);
						mediaItem.setMediaContentType(MediaContentType.RADIO);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, scrollPosition);
					}
				} else if (getActivity().getIntent().getStringExtra("Station_ID") != null) {
					getActivity().getIntent().removeExtra("Station_ID");
					// mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null) {
						MediaItem mediaItem = new MediaItem(id, "Celeb Radio",
								"", "", "", "", MediaType.ARTIST.toString(), 0,
								0);
						mediaItem.setMediaContentType(MediaContentType.RADIO);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, scrollPosition);
					}
				} else if (getActivity().getIntent().getStringExtra("radio_id") != null) {
					getActivity().getIntent().removeExtra("radio_id");
					// mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null) {
						MediaItem mediaItem = new MediaItem(id, "Live Radio",
								"", "", "", "", MediaType.LIVE.toString(), 0, 0);
						mediaItem.setMediaContentType(MediaContentType.RADIO);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, scrollPosition);
					}
				}
				Logger.d(TAG, "Position " + String.valueOf(id)
						+ " is not in the media list!");
			}

		}
	}

	public void updateDeepLink(List<MediaItem> mediaItems){
		int scrollPosition = -1;
//		mHomeMediaTilesAdapter.setArtist_id(getActivity().getIntent()
//				.getStringExtra("artist_id"));
		Logger.i("First parent", "Chanell #"
				+ getActivity().getIntent().getStringExtra("radio_id"));// "channel_index"
		mHomeMediaTilesAdapter.setChannel_id(getActivity().getIntent()
				.getStringExtra("radio_id"));// "channel_index"
		mHomeMediaTilesAdapter
				.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(
				getActivity());
		mTilesGridView.setLayoutManager(mLayoutManager);
		mTilesGridView.setAdapter(mHomeMediaTilesAdapter);

		long id = 0;
		if (getActivity().getIntent().getStringExtra("radio_id") != null) {// "channel_index"
			id = Long.parseLong(getActivity().getIntent().getStringExtra(
					"radio_id"));// "channel_index"
		} else if (getActivity().getIntent().getStringExtra("artist_id") != null) {
			id = Long.parseLong(getActivity().getIntent().getStringExtra(
					"artist_id"));
		} else if (getActivity().getIntent().getStringExtra("Station_ID") != null) {
			id = Long.parseLong(getActivity().getIntent().getStringExtra(
					"Station_ID"));
		}
		if (mediaItems != null) {
			for (MediaItem mi : mediaItems) {
				if (mi.getId() == id) {
					scrollPosition = mediaItems.indexOf(mi);
					break;
				}
			}
			if (getActivity().getIntent()
					.getStringExtra("artist_id") != null) {
				scrollPosition = -1;
			}
			if (scrollPosition != -1) {
				if (getActivity().getIntent().getStringExtra("radio_id") != null) {// "channel_index"
					getActivity().getIntent().removeExtra("radio_id");// "channel_index"
					mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null)
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItems.get(scrollPosition),
										scrollPosition);

				} else if (getActivity().getIntent()
						.getStringExtra("artist_id") != null) {
					getActivity().getIntent().removeExtra("artist_id");
					mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null)
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItems.get(scrollPosition),
										scrollPosition);

				} else if (getActivity().getIntent()
						.getStringExtra("Station_ID") != null) {
					getActivity().getIntent().removeExtra("Station_ID");
					mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null)
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItems.get(scrollPosition),
										scrollPosition);

				}
			} else {
				if (getActivity().getIntent().getStringExtra("artist_id") != null) {
					getActivity().getIntent().removeExtra("artist_id");
					// mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null) {
						MediaItem mediaItem = new MediaItem(id, "Celeb Radio",
								"", "", "", "", MediaType.ARTIST_OLD.toString(), 0,
								0);
						mediaItem.setMediaContentType(MediaContentType.RADIO);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, scrollPosition);
					}
				} else if (getActivity().getIntent().getStringExtra("Station_ID") != null) {
					getActivity().getIntent().removeExtra("Station_ID");
					// mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null) {
						MediaItem mediaItem = new MediaItem(id, "Celeb Radio",
								"", "", "", "", MediaType.ARTIST.toString(), 0,
								0);
						mediaItem.setMediaContentType(MediaContentType.RADIO);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, scrollPosition);
					}
				} else if (getActivity().getIntent().getStringExtra("radio_id") != null) {
					getActivity().getIntent().removeExtra("radio_id");
					// mTilesGridView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null) {
						MediaItem mediaItem = new MediaItem(id, "Live Radio",
								"", "", "", "", MediaType.LIVE.toString(), 0, 0);
						mediaItem.setMediaContentType(MediaContentType.RADIO);
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItem, scrollPosition);
					}
				}
				Logger.d(TAG, "Position " + String.valueOf(id)
						+ " is not in the media list!");
			}

		}
	}

	private CacheStateReceiver cacheStateReceiver;

	public void setGridView() {
		if(mHomeMediaTilesAdapter!=null)
			mHomeMediaTilesAdapter.setGridView(mTilesGridView);
	}

	class CacheStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (mHomeMediaTilesAdapter != null)
				mHomeMediaTilesAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onAttach(Activity activity) {
		if (cacheStateReceiver == null) {
			cacheStateReceiver = new CacheStateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(CacheManager.ACTION_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_UPDATED_CACHE);
			filter.addAction(CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED);
			filter.addAction(CacheManager.ACTION_VIDEO_TRACK_CACHED);
			filter.addAction(CacheManager.ACTION_VIDEO_UPDATED_CACHE);
			getActivity().registerReceiver(cacheStateReceiver, filter);
		}
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		if (cacheStateReceiver != null) {
			getActivity().unregisterReceiver(cacheStateReceiver);
			cacheStateReceiver = null;
		}
		super.onDetach();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.resumeLoadingImages();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.stopLoadingImages();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.releaseLoadingImages();
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	// protected void ListScrollToTop() {
	// if (mTilesGridView != null)
	// mTilesGridView.setSelection(0);
	// }

	public RecyclerView getGridView() {
		return mTilesGridView;
	}

	// protected void setListView(RecyclerView gridView) {
	// mTilesGridView = gridView;
	// }

	@Override
	public void onStart() {
		// if(mHomeMediaTilesAdapter != null){
		// mHomeMediaTilesAdapter.startFlurrySession();
		// }
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(getActivity(),
		// getString(R.string.flurry_app_key));
//		Analytics.startSession(getActivity(), this);
	}

	@Override
	public void onStop() {
		// if(mHomeMediaTilesAdapter != null){
		// mHomeMediaTilesAdapter.endFlurrySession();
		// }
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	// public void updateMediaItemCacheState() {
	// if (mTilesGridView != null && mHomeMediaTilesAdapter != null) {
	// mHomeMediaTilesAdapter.notifyDataSetChanged();
	// }
	// }

	public void postAd() {
		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.clearAdPositions();
			mHomeMediaTilesAdapter.postAdForPosition();
		}
	}

	private PromoUnit mPromoUnit;

	public void setPromoUnit(PromoUnit mPromoUnit, List<MediaItem> mediaItems) {
		this.mPromoUnit = mPromoUnit;
		if (getActivity() != null && mPromoUnit != null
				&& (((HomeActivity) getActivity()).isPromoUnit()))
			setMediaItems(mediaItems);
	}
}
