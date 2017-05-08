package com.hungama.myplay.activity.ui.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivityNew;
import com.hungama.myplay.activity.ui.PlaylistsActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.adapters.ComboMediaItem;
import com.hungama.myplay.activity.ui.adapters.MediaTilesAdapterVideo;
import com.hungama.myplay.activity.ui.adapters.MyAdapter;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents {@link MediaItem}s in a grid.
 */
public class MediaTileGridFragment extends BackHandledFragment {

	private static final String TAG = "MediaTileGridFragment";

	public static final String FRAGMENT_ARGUMENT_MEDIA_ITEMS = "fragment_argument_media_items";
	public static final String FLURRY_SUB_SECTION_DESCRIPTION = "flurry_sub_section_description";

	public RecyclerView mTilesListView;
	private int mTileSize = 0;
	private MyAdapter mHomeMediaTilesAdapter;
	private MediaTilesAdapterVideo mHomeMediaTilesAdapter_video;

	private OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener;

	private String backgroundLink;

	private Placement placement;
	private CampaignsManager mCampaignsManager;

	public boolean isDeleteShowing = false;
	private boolean isMarginRequere = true;

	String flurrySubSectionDescription = "";

	ProgressBar pb;
	String title;
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

	public void setMediaItems(List<Object> mediaItems) {
		mHomeMediaTilesAdapter.setMediaItems(mediaItems);
		mHomeMediaTilesAdapter.notifyDataSetChanged();
	}

    ProfileActivity profileActivity;
    public void setProfileActivity(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }

	boolean showDownloadOption = true;

	public void setshowDownloadOption(boolean showDownloadOption) {
		this.showDownloadOption = showDownloadOption;
	}

	public void setIsMarginTopRequire(boolean isMarginRequere) {
		this.isMarginRequere = isMarginRequere;
	}

	MediaDetailsActivityNew detailsActivity;

	public void setMediaDetailsActivityNew(MediaDetailsActivityNew detailsActivity) {
		this.detailsActivity = detailsActivity;
	}

    PlaylistsActivity mPlayList;
    public void setPlayList(PlaylistsActivity mPlayList){
        this.mPlayList=mPlayList;
    }

	/**
	 * set media items for video and fill in adapter
	 * 
	 * @param mediaItems
	 */
	public void setMediaItemsVideo(List<MediaItem> mediaItems) {

		mHomeMediaTilesAdapter = null;

		mHomeMediaTilesAdapter_video = new MediaTilesAdapterVideo(
				getActivity(), null, mTileSize, this.getClass()
						.getCanonicalName(), null, null, mCampaignsManager,
				mediaItems, isDeleteShowing, flurrySubSectionDescription);
		mHomeMediaTilesAdapter_video.setDownloadOption(showDownloadOption);
		mHomeMediaTilesAdapter_video
				.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
		LinearLayoutManager mLayoutManager = new LinearLayoutManager(
				getActivity());
		mTilesListView.setLayoutManager(mLayoutManager);
		mTilesListView.setAdapter(mHomeMediaTilesAdapter_video);

	}

	// ======================================================
	// FRAGMENT'S LIFE CYCLE.
	// ======================================================

    @Override
    public boolean onBackPressed() {

        if (getActivity()==null) {
            return false;
        }

		if (((MainActivity) getActivity()).mPlayerBarFragment != null && ((MainActivity) getActivity()).mPlayerBarFragment.isContentOpened()) {
			// Minimize player
			if (!((MainActivity) getActivity()).mPlayerBarFragment.removeAllFragments())
				((MainActivity) getActivity()).mPlayerBarFragment.closeContent();
			return true;
		} else {
			if(profileActivity!=null) {
				profileActivity.onBackPressed();
				return true;
			}
			if(getActivity() instanceof MediaDetailsActivity) {
				((MediaDetailsActivity) getActivity()).onBackPressed();
				return true;
			}
			if (((MainActivity) getActivity()).closeDrawerIfOpen()) {
				return false;
			}

			if (((MainActivity) getActivity()).mPlayerBarFragment != null && ((MainActivity) getActivity()).mPlayerBarFragment.isContentOpened()) {
				// Minimize player
				if (!((MainActivity) getActivity()).mPlayerBarFragment.removeAllFragments())
					((MainActivity) getActivity()).mPlayerBarFragment.closeContent();
				return true;
			} else {
				if(detailsActivity!=null)
					detailsActivity.onBackPressed();
				else if(mPlayList!=null)
					mPlayList.onBackPressed();
				else {
					if(getActivity()!=null)
						getActivity().getSupportFragmentManager().popBackStack();
				}
				return true;
			}
		}

//            getActivity().getSupportFragmentManager().popBackStack();

    }



    @Override
    public void setTitle(boolean needOnlyHight,boolean needToSetTitle) {
        try {
			Utils.setToolbarColor( ((MainActivity) getActivity()));
            if(TextUtils.isEmpty(title))
                title=getArguments().getString("title");
            ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
                    title, "");

        }catch (Exception e){

        }
        ((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(profileActivity!=null) {
					profileActivity.setNavigationClick();
					profileActivity.onBackPressed();
				}
                else{
					onBackPressed();
				}

            }
        });
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.postCrashlitycsLog(getActivity(), MediaTileGridFragment.class.getName());
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mTilesListView = new RecyclerView(getActivity());

		LinearLayoutManager mLayoutManager = new LinearLayoutManager(
				getActivity());
		mTilesListView.setLayoutManager(mLayoutManager);

		pb = new ProgressBar(getActivity());
		pb.getIndeterminateDrawable().setColorFilter(
		// new LightingColorFilter(0xFF000000, 0xFFFFFF));
				Color.GRAY, Mode.SRC_ATOP);
		LinearLayout.LayoutParams pbParam = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		pb.setLayoutParams(pbParam);

		RelativeLayout rel = new RelativeLayout(getActivity());
		rel.addView(mTilesListView);

		RelativeLayout.LayoutParams relParam = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		relParam.addRule(RelativeLayout.CENTER_IN_PARENT);
		rel.addView(pb, relParam);

		rel.setBackgroundColor(getResources().getColor(
				R.color.application_background_grey));

		pb.setVisibility(View.GONE);

		mCampaignsManager = CampaignsManager.getInstance(getActivity());

		initializeTiles();

        setTitle(false,true);

		return rel;
	}

	/**
	 * initialize listview, adapter and media item
	 */
	private void initializeTiles() {
		int imageTileSpacing = getResources().getDimensionPixelSize(
				R.dimen.home_tiles_spacing_vertical);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			mTilesListView.setOverScrollMode(GridView.OVER_SCROLL_NEVER);
		}

		// sets the background.
		Logger.v(
				TAG,
				"The device build number is: "
						+ Integer.toString(Build.VERSION.SDK_INT));
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mTilesListView.setBackground(null);
		} else {
			mTilesListView.setBackgroundDrawable(null);
		}

		if (isMarginRequere)
			mTilesListView.setPadding(0, (int) getActivity().getResources()
					.getDimension(R.dimen.home_music_tile_margin) - 5, 0, 0);

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
		mTileSize = (int) ((screenWidth - (imageTileSpacing + imageTileSpacing * 1.5)) / 2);

		Logger.i(TAG, "screenWidth: " + screenWidth + " mTileSize: "
				+ mTileSize);

		/*
		 * gets the list of the media items from the arguments, and sets it as
		 * the source to the adapter.
		 */
		List<MediaItem> mediaItems = null;

		mHomeMediaTilesAdapter = null;

		Bundle data = getArguments();
		title=data.getString("title");
		try {
			if (data != null
					&& data.containsKey(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS)) {
				mediaItems = new ArrayList<MediaItem>();
				mediaItems
						.addAll((List<MediaItem>) data
								.getSerializable(MediaTileGridFragment.FRAGMENT_ARGUMENT_MEDIA_ITEMS));
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":211", e.toString());
        }

		ArrayList<Object> mediaitemMusic = new ArrayList<Object>();
		mediaitemMusic.clear();

		List<MediaItem> tracks = new ArrayList<MediaItem>();
		List<MediaItem> playlists = new ArrayList<MediaItem>();

		if (mediaItems != null)
			for (MediaItem mediaItem : mediaItems) {
				if (mediaItem.getMediaType() == MediaType.TRACK)
					tracks.add(mediaItem);
				else if (mediaItem.getMediaType() == MediaType.ALBUM)
					tracks.add(mediaItem);
				else if (mediaItem.getMediaType() == MediaType.ARTIST)
					tracks.add(mediaItem);
				else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
					playlists.add(mediaItem);

			}
		ArrayList<MediaItem> serverSAA = new ArrayList<MediaItem>(tracks);
		int adcount = 0;
		if (placement != null) {
			for (int i = 0; i < tracks.size(); i++) {
				if (i > 0 && i % 3 == 0) {
					// System.out.println("add ad ");
					serverSAA.add(i + adcount,
							new MediaItem(i, "no", "no", "no", backgroundLink,
									backgroundLink, "album", 0, 0));
					adcount++;
				}
			}
		}

		if (adcount != 0)
			tracks = serverSAA;

		int blocks = (tracks.size()) / 2;

		if (playlists != null && playlists.size() > 0) {
			blocks = (playlists.size());
		} else if (tracks != null && tracks.size() > 0) {
			blocks = (tracks.size()) / 2;
			if ((tracks.size()) % 2 > 0)
				blocks += 1;
		} else if (playlists != null && playlists.size() > 0) {
			blocks = (tracks.size() + playlists.size()) / 5;
			if ((tracks.size() + playlists.size()) % 5 > 0)
				blocks += 1;
		}

		ComboMediaItem c;
		for (int i = 0; i < blocks; i++) {
			if (tracks.size() > 0) {
				c = new ComboMediaItem(tracks.get(0),
						(tracks.size() > 1) ? tracks.get(1) : null);
				mediaitemMusic.add(c);
				tracks.remove(0);
				if (tracks.size() > 0)
					tracks.remove(0);
			}

			if (tracks.size() > 0) {
				c = new ComboMediaItem(tracks.get(0),
						(tracks.size() > 1) ? tracks.get(1) : null);
				mediaitemMusic.add(c);
				tracks.remove(0);
				if (tracks.size() > 0)
					tracks.remove(0);
			}
			if (playlists.size() > 0) {
				mediaitemMusic.add(playlists.get(0));
				playlists.remove(0);
				if (tracks.size() == 0) {
					for (MediaItem obj : playlists) {
						mediaitemMusic.add(obj);
					}
					playlists.clear();
				}
			}
		}

		if (data != null
				&& data.containsKey(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION)) {
			flurrySubSectionDescription = data
					.getString(MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION);
		}
		mTilesListView.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(RecyclerView view, int scrollState) {
				if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
					PicassoUtil.with(getActivity()).resumeTag();
					try {
						mHomeMediaTilesAdapter.postAdForPosition();
					} catch (Exception e) {
						e.printStackTrace();
					}
					try {
						mHomeMediaTilesAdapter_video.postAdForPosition();
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					PicassoUtil.with(getActivity()).pauseTag();
				}
			}
		});

		if (mHomeMediaTilesAdapter_video == null && mediaItems != null
				&& mediaItems.size() > 0
				&& mediaItems.get(0).getMediaType() == MediaType.VIDEO) {

			if (isMarginRequere)
				mTilesListView.setPadding(0, (int) getActivity().getResources()
						.getDimension(R.dimen.home_music_tile_margin), 0, 0);

			mHomeMediaTilesAdapter_video = new MediaTilesAdapterVideo(
					getActivity(), null, mTileSize, this.getClass()
							.getCanonicalName(), null, null, mCampaignsManager,
					mediaItems, isDeleteShowing, flurrySubSectionDescription);

			mHomeMediaTilesAdapter_video.setDownloadOption(showDownloadOption);

			mHomeMediaTilesAdapter_video
					.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);

			mTilesListView.setAdapter(mHomeMediaTilesAdapter_video);

		} else {
			mHomeMediaTilesAdapter = new MyAdapter(mediaitemMusic,
					getActivity(), null, null, mCampaignsManager,
					isDeleteShowing, flurrySubSectionDescription,
					mTilesListView);
			mHomeMediaTilesAdapter.showDownloadOption(showDownloadOption);

			mHomeMediaTilesAdapter
					.setOnMusicItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
			mTilesListView.setAdapter(mHomeMediaTilesAdapter);
		}
		int scrollPosition = -1;

		long id = 0;
		if (getActivity().getIntent().getStringExtra("channel_index") != null) {
			id = Long.parseLong(getActivity().getIntent().getStringExtra(
					"channel_index"));
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
			if (scrollPosition != -1) {
				if (getActivity().getIntent().getStringExtra("channel_index") != null) {
					getActivity().getIntent().removeExtra("channel_index");
					mTilesListView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null)
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItems.get(scrollPosition),
										scrollPosition);

				} else if (getActivity().getIntent()
						.getStringExtra("artist_id") != null) {
					getActivity().getIntent().removeExtra("artist_id");
					mTilesListView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null)
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItems.get(scrollPosition),
										scrollPosition);

				} else if (getActivity().getIntent()
						.getStringExtra("Station_ID") != null) {
					getActivity().getIntent().removeExtra("Station_ID");
					mTilesListView.smoothScrollToPosition(scrollPosition);
					if (mOnMediaItemOptionSelectedListener != null)
						mOnMediaItemOptionSelectedListener
								.onMediaItemOptionShowDetailsSelected(
										mediaItems.get(scrollPosition),
										scrollPosition);

				}
			} else {
				Logger.d(TAG, "Position " + String.valueOf(id)
						+ " is not in the media list!");
			}

		}
	}

	private CacheStateReceiver cacheStateReceiver;

	class CacheStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (mHomeMediaTilesAdapter != null)
				mHomeMediaTilesAdapter.notifyDataSetChanged();
			if (mHomeMediaTilesAdapter_video != null)
				mHomeMediaTilesAdapter_video.notifyDataSetChanged();
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
		super.onDetach();
	}

	@Override
	public void onResume() {
		super.onResume();

		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.resumeLoadingImages();
		}
		if (mHomeMediaTilesAdapter_video != null) {
			mHomeMediaTilesAdapter_video.resumeLoadingImages();
		}
		Utils.setToolbarColor( ((MainActivity) getActivity()));
	}

	@Override
	public void onPause() {
		super.onPause();
		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.stopLoadingImages();
		}
		if (mHomeMediaTilesAdapter_video != null) {
			mHomeMediaTilesAdapter_video.stopLoadingImages();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.releaseLoadingImages();
		}

		if (mHomeMediaTilesAdapter_video != null) {
			mHomeMediaTilesAdapter_video.releaseLoadingImages();
		}
		if (cacheStateReceiver != null) {
			getActivity().unregisterReceiver(cacheStateReceiver);
			cacheStateReceiver = null;
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	// protected RecyclerView getGridView() {
	// return mTilesListView;
	// }

	// protected void setGridView(RecyclerView gridView) {
	// mTilesListView = gridView;
	// }

	@Override
	public void onStart() {
		super.onStart();
		Analytics.startSession(getActivity(), this);
	}

	@Override
	public void onStop() {
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	/**
	 * notify adapter for data change
	 */
	public void updateMediaItemCacheState() {
		if (mTilesListView != null && mHomeMediaTilesAdapter != null) {
			mHomeMediaTilesAdapter.notifyDataSetChanged();
		}

		if (mTilesListView != null && mHomeMediaTilesAdapter_video != null) {
			mHomeMediaTilesAdapter_video.notifyDataSetChanged();
		}
	}

	/**
	 * post playevent for ad
	 */
	// public void postAd() {
	// if (mHomeMediaTilesAdapter != null)
	// mHomeMediaTilesAdapter.postAdForPosition();
	//
	// if (mHomeMediaTilesAdapter_video != null)
	// mHomeMediaTilesAdapter_video.postAdForPosition();
	// }
}
