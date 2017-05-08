package com.hungama.myplay.activity.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.HungamaApplication;

import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.ui.fragments.BackHandledFragment;
import com.hungama.myplay.activity.ui.fragments.MediaDetailsFragment;
import com.hungama.myplay.activity.ui.fragments.MediaTileGridFragment;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.QuickActionMediaDetail.OnMediaSelectedListener;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for presenting details of the given MediaItem.
 */
public class MediaDetailsActivityNew extends BackHandledFragment implements
		OnMediaItemOptionSelectedListener, OnMediaSelectedListener {

	private static final String TAG = "MediaDetailsActivity";

	public static final String EXTRA_MEDIA_ITEM = "EXTRA_MEDIA_ITEM";

	public static final String FLURRY_SOURCE_SECTION = "flurry_source_section";

	private FragmentManager mFragmentManager;
	// private PlayerBarFragment mPlayerBarFragment;
	private MediaItem mMediaItem, mMediaItemTrack;

	private TextView mTitleBarText;

	private Dialog dialog;

	private String mFlurrySubSectionDescription;

	private String flurrySourceSection;
	private MediaDetailsFragment mediaDetailsFragment;
	private CacheStateReceiver cacheStateReceiver;
	public ArrayList<String> listTitle = new ArrayList<String>();
    public ArrayList<Integer> alpha = new ArrayList<Integer>();
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity)getActivity()).lockDrawer();
        ((MainActivity)getActivity()).removeDrawerIconAndPreference();
        ((MainActivity)getActivity()).setNeedToOpenSearchActivity(false);
//        setRetainInstance(true);
        Analytics.postCrashlitycsLog(getActivity(), MediaDetailsActivityNew.class.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Logger.s("-------------------MediaDetailsActivity onCreate---------------------");

        super.onCreate(savedInstanceState);
        Bundle data = getArguments();//intent.getExtras();
//        if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
            if (rootView == null) {
                try {
                    rootView = inflater.inflate(R.layout.activity_main_with_title_transparent_new,
                            container, false);
                    Utils.traverseChild(rootView, getActivity());

                } catch (Error e) {
                    System.gc();
                    System.runFinalization();
                    System.gc();
                }
                // retrieves the given Media item for the activity.
                mMediaItem = (MediaItem) data.getSerializable(EXTRA_MEDIA_ITEM);
                if (mMediaItem.getMediaContentType() == MediaContentType.MUSIC) {
                    // SetS title bar
                    mTitleBarText = (TextView) rootView.findViewById(R.id.main_title_bar_text);
                    mTitleBarText.setSelected(true);// xtpl
                    String title = null;
                    if (mMediaItem.getTitle() != null)
                        title = mMediaItem.getTitle();
                    else if (mMediaItem.getAlbumName() != null)
                        title = mMediaItem.getAlbumName();

                    mTitleBarText.setText(mMediaItem.getTitle());
                    listTitle.add(title);
                    alpha.add(160);
                }

                flurrySourceSection = "No Flurry Source Section";
                if (data != null && data.containsKey(FLURRY_SOURCE_SECTION)) {
                    flurrySourceSection = (String) data.get(FLURRY_SOURCE_SECTION);
                }

                if (mMediaItem.getMediaType() == MediaType.ALBUM) {
                    mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.AlbumDetail
                            .toString();
                } else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
                    mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.PlaylistDetail
                            .toString();
                } else if (mMediaItem.getMediaType() == MediaType.TRACK) {
                    mFlurrySubSectionDescription = FlurryConstants.FlurrySubSectionDescription.SongDetail
                            .toString();
                }

                mFragmentManager = getChildFragmentManager();//getActivity().getSupportFragmentManager();

                FragmentTransaction fragmentTransaction = mFragmentManager
                        .beginTransaction();
                Bundle detailsData = new Bundle();
                detailsData.putSerializable(MediaDetailsFragment.ARGUMENT_MEDIAITEM,
                        (Serializable) mMediaItem);
                detailsData.putString(FLURRY_SOURCE_SECTION, flurrySourceSection);

                mediaDetailsFragment = new MediaDetailsFragment();
                mediaDetailsFragment.setMediaDetailsActivityNew(this);
                mediaDetailsFragment.onMediaItemOptionSelectedListener(this);
                mediaDetailsFragment.onRootViewParent(rootView);
                mediaDetailsFragment.setArguments(detailsData);
                mediaDetailsFragment.onMediaListener(this);
//                View id=rootView.findViewById(R.id.main_fragmant_container_media_detail);
                fragmentTransaction.add(R.id.main_fragmant_container_media_detail,
                        mediaDetailsFragment);
                fragmentTransaction.disallowAddToBackStack();
//            fragmentTransaction.addToBackStack("media_details_fragment");
                if(Constants.IS_COMMITALLOWSTATE)
                    fragmentTransaction.commitAllowingStateLoss();
                else
                    fragmentTransaction.commit();

                if (cacheStateReceiver == null) {
                    cacheStateReceiver = new CacheStateReceiver();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(CacheManager.ACTION_CACHE_STATE_UPDATED);
                    filter.addAction(CacheManager.ACTION_TRACK_CACHED);
                    filter.addAction(CacheManager.ACTION_UPDATED_CACHE);
                    getActivity().registerReceiver(cacheStateReceiver, filter);
                }

                rootView.findViewById(R.id.main_title_bar).setVisibility(View.GONE);

                getChildFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
                    @Override
                    public void onBackStackChanged() {
                        setChildFragment(false);
                    }
                });
            } else {
                Logger.e(TAG, "No MediaItem set for the given Activity.");
                ViewGroup parent = (ViewGroup) Utils.getParentViewCustom(rootView);
                parent.removeView(rootView);
            }
        setNavigationClick();
        return rootView;
    }

    public boolean setChildFragment(boolean isFromHome){
        boolean ishandle;
        int backCount = getChildFragmentManager().getBackStackEntryCount();
        Logger.i(TAG, "back stack changed " + backCount);
        if (backCount == 0) {
            if(!isFromHome) {
                if(getActivity()!=null){
                    if(getActivity() instanceof  HomeActivity){
                        ((HomeActivity) getActivity()).getSupportFragment(true);
                    }else if(getActivity() instanceof  DownloadConnectingActivity){
                        ((DownloadConnectingActivity) getActivity()).getSupportFragment(true);
                    }
                }

            }
            return false;
        }
        if (fragmentCount > backCount || isFromHome) {
            FragmentManager.BackStackEntry backEntry = (FragmentManager.BackStackEntry) getChildFragmentManager().getBackStackEntryAt(getChildFragmentManager().getBackStackEntryCount() - 1);
            String str = backEntry.getName();
            Logger.i(TAG, "back stack name " + str);
            Fragment fragment = getChildFragmentManager().findFragmentByTag(str);
            if(getActivity()!=null){
                if(getActivity() instanceof  HomeActivity){
                    ((HomeActivity)getActivity()).resetCurrentFragment(fragment);
                }else if(getActivity() instanceof  DownloadConnectingActivity){
                    ((DownloadConnectingActivity) getActivity()).resetCurrentFragment(fragment);
                }
            }

            return true;
        }
        fragmentCount = backCount;
        return false;
    }

    public int fragmentCount = 0;
	@Override
	public void onResume() {
		super.onResume();
		Logger.s("onResume MediaDetailsActivity");
		HungamaApplication.activityResumed();
		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getActivity().getBaseContext());
		if (mApplicationConfigurations.isSongCatched()) {
            ((MainActivity) getActivity()).openOfflineGuide();
		}

        setTitle(false, true);

		if (((MainActivity) getActivity()).isSkipResume) {
            ((MainActivity) getActivity()).isSkipResume = false;
			return;
		}
	}

    private void setNavigationClick(){
        try {
            ((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setTitle(boolean needOnlyHight,boolean needToSetTitle){

        setNavigationClick();

        if(needToSetTitle){
            if (listTitle!=null && listTitle.size() > 0) {

                ColorDrawable cd = new ColorDrawable(getResources().getColor(
                        R.color.primaryColorDark));
                cd.setAlpha(alpha.get(alpha
                        .size() - 1));

                updateTitleColor(cd, false);
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
                        listTitle.get(listTitle.size() - 1), "");
            }
        }
        if(needOnlyHight){
            FrameLayout layout = (FrameLayout) rootView.findViewById(R.id.main_fragmant_container_media_detail);
            MarginLayoutParams params = (MarginLayoutParams) layout
                    .getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            if (listTitle.size() > 0) {

                ColorDrawable cd = new ColorDrawable(getResources().getColor(
                        R.color.primaryColorDark));
                cd.setAlpha(alpha.get(alpha
                        .size() - 1));

                updateTitleColor(cd, false);
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
                        listTitle.get(listTitle.size() - 1), "");
            }
            return;
        }
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			onBackPressed();
			return true;
		}/* else if(itemId == R.id.media_route_menu_item){
            return true;
        } */else
			return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onBackPressed() {
        if (getActivity()==null) {
            return false;
        }

		if (((MainActivity) getActivity()).closeDrawerIfOpen()) {
			return false;
		}

		if (((MainActivity) getActivity()).mPlayerBarFragment != null && ((MainActivity) getActivity()).mPlayerBarFragment.isContentOpened()) {
			// Minimize player
			if (!((MainActivity) getActivity()).mPlayerBarFragment.removeAllFragments())
                ((MainActivity) getActivity()).mPlayerBarFragment.closeContent();
		} else {
//			int lastFragmentCount = ((MainActivity) getActivity()).getSupportFragmentManager()
//					.getBackStackEntryCount();

//			if (lastFragmentCount > 2) {

				// new Handler().postDelayed(new Runnable() {
				// @Override
				// public void run() {
				if (mediaDetailsFragment.isVideoInsideOpen) {
//                    ((MainActivity) getActivity()).getSupportFragmentManager().popBackStack();
                    getChildFragmentManager().popBackStack();
                    try {
                        mediaDetailsFragment.isVideoInsideOpen = false;
                        FrameLayout layout = (FrameLayout) rootView.findViewById(R.id.main_fragmant_container_media_detail);
                        MarginLayoutParams params = (MarginLayoutParams) layout
                                .getLayoutParams();
                        params.setMargins(0, 0, 0, 0);
                    } catch (Exception e) {
                    }
                    return true;
                }
				if (mediaDetailsFragment.isMediaInsideOpen) {
//                    ((MainActivity) getActivity()).getSupportFragmentManager().popBackStack();
                    getChildFragmentManager().popBackStack();
					try {
						if (listTitle.size() > 0) {
							if (listTitle.size() > 1) {
								listTitle.remove(listTitle.size() - 1);
                                alpha.remove(alpha.size() - 1);
							}
							Logger.e("listTitle", "" + listTitle);
							mTitleBarText.setText(listTitle.get(listTitle
									.size() - 1));

						} else
							mTitleBarText.setText(mMediaItem.getTitle());
					} catch (Exception e) {
						e.printStackTrace();
					}

                    mediaDetailsFragment.isMediaInsideOpen=false;
                    return true;
				}

				// }
				// }, 100);
//				return true;
//			}
			if (mMediaItemTrack != null)
				mMediaItemTrack = null;

//			if (SongCatcherFragment.isSongCatcherOpen)
//				SongCatcherFragment.isSongCatcherOpen = false;

            if (listTitle.size() > 0) {
                if (listTitle.size() > 1) {
                    listTitle.remove(listTitle.size() - 1);
                    alpha.remove(alpha.size() - 1);
                }
                mTitleBarText.setText(listTitle.get(listTitle.size() - 1));
                listTitle.remove(listTitle.size() - 1);
                alpha.remove(alpha.size() - 1);
            } else
                mTitleBarText.setText(mMediaItem.getTitle());

            try{
                HomeActivity.videoInAlbumSet = false;
                if(getChildFragmentManager().getBackStackEntryCount()>0)
                    getChildFragmentManager().popBackStack();
                else
                    getActivity().getSupportFragmentManager().popBackStack();
            }catch (Exception e){
            }

//            getActivity().finish();
		}
        return true;
		// super.onBackPressed();
	}

    public void openVideoPage(Bundle detailsDataVideos,OnMediaItemOptionSelectedListener mOnMediaItemOptionSelectedListener){

        MediaTileGridFragment mediaTileGridFragment = new MediaTileGridFragment();
        mediaTileGridFragment.setOnMediaItemOptionSelectedListener(mOnMediaItemOptionSelectedListener);
        mediaTileGridFragment.setArguments(detailsDataVideos);
        mediaTileGridFragment.setIsMarginTopRequire(false);
        mediaTileGridFragment.setMediaDetailsActivityNew(this);

        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
                R.anim.slide_left_exit, R.anim.slide_right_enter,
                R.anim.slide_right_exit);

        fragmentTransaction.add(R.id.main_fragmant_container_media_detail,
                mediaTileGridFragment, "video");
        fragmentTransaction.addToBackStack("video");
        if(Constants.IS_COMMITALLOWSTATE)
            fragmentTransaction.commitAllowingStateLoss();
        else
            fragmentTransaction.commit();

        FrameLayout layout = (FrameLayout) rootView.findViewById(R.id.main_fragmant_container_media_detail);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) layout
                .getLayoutParams();
        params.setMargins(0, ((MainActivity) getActivity()).getActionBarHeight(), 0, 0);
        Utils.setToolbarColor(((MainActivity) getActivity()));
    }

    public void openTrackPage(MediaItem trackMediaItem) {

        if(getActivity()==null)
            return;

		mMediaItemTrack = trackMediaItem;
		mediaDetailsFragment.isMediaInsideOpen = true;
//        mediaDetailsFragment.updateTitleSubtitle(trackMediaItem.getTitle(), "",255);

        mTitleBarText.setText(trackMediaItem.getTitle());
        listTitle.add(trackMediaItem.getTitle());
        alpha.add(mediaDetailsFragment.alpha);

		Bundle detailsDataTrack = new Bundle();
		detailsDataTrack.putSerializable(
                MediaDetailsFragment.ARGUMENT_MEDIAITEM,
                (Serializable) trackMediaItem);

		MediaDetailsFragment mediaDetailsFragmentTrack = new MediaDetailsFragment();
		mediaDetailsFragmentTrack.setArguments(detailsDataTrack);
        mediaDetailsFragmentTrack.setMediaDetailsActivityNew(this);
        mediaDetailsFragmentTrack.onMediaItemOptionSelectedListener(this);
        mediaDetailsFragmentTrack.onRootViewParent(rootView);
		mediaDetailsFragmentTrack.onMediaListener(this);
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
                R.anim.slide_left_exit, R.anim.slide_right_enter,
                R.anim.slide_right_exit);

		fragmentTransaction.add(R.id.main_fragmant_container_media_detail,
                mediaDetailsFragmentTrack, "MediaDetailsFragment_inner");
		fragmentTransaction.addToBackStack("MediaDetailsFragment_inner");
        if(Constants.IS_COMMITALLOWSTATE)
            fragmentTransaction.commitAllowingStateLoss();
        else
            fragmentTransaction.commit();
	}

	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK && requestCode == 100) {
			if (mMediaItem.getTitle() != null)
				mTitleBarText.setText(mMediaItem.getTitle());
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void addToQueueButtonClickActivity(List<Track> trackList,
			String flurryEventName, String flurrySourceSection) {
		if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
			for (Track track : trackList) {
				track.setTag(mMediaItem);
			}
		} else if (mMediaItem.getMediaType() == MediaType.ALBUM) {
			for (Track track : trackList) {
				track.setTag(mMediaItem);
			}
		}
		// mPlayerBarFragment = getPlayerBar();
        if(getActivity()!=null && ((MainActivity) getActivity()).mPlayerBarFragment!=null)
            ((MainActivity) getActivity()).mPlayerBarFragment.addToQueue(trackList, flurryEventName,
                    flurrySourceSection);
	}

	public void addToPlaylistButtonClickActivity(List<Track> trackList) {
        ((MainActivity) getActivity()).mPlayerBarFragment = ((MainActivity) getActivity()).getPlayerBar();
	}

	@Override
	public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
	public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
	public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
	public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
			int position) {
        ((MainActivity) getActivity()).isSkipResume=true;
		Intent intent = new Intent(getActivity(), VideoActivity.class);
		intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
				(Serializable) mediaItem);
		if (mediaDetailsFragment != null
				&& mediaDetailsFragment.mMediaSetDetails != null
				&& mediaDetailsFragment.mMediaSetDetails.getVideos() != null
				&& mediaDetailsFragment.mMediaSetDetails.getVideos().size() > 0) {
			try {
				intent.putExtra(VideoActivity.EXTRA_MEDIA_LIST_VIDEO,
						(Serializable) mediaDetailsFragment.mMediaSetDetails
								.getVideos());
				intent.putExtra(VideoActivity.EXTRA_MEDIA_POS_VIDEO, position);
			} catch (Exception e) {
			}
		}
		startActivity(intent);
	}

	@Override
	public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
			int position) {
	}

	@Override
    public void onStart() {
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(this, getString(R.string.flurry_app_key));
		Analytics.startSession(getActivity(), this);
//		Button btn_preference = (Button) rootView.findViewById(R.id.btn_preferences);
//		btn_preference.setVisibility(View.GONE);

		try {
			if (mMediaItem == null) {
				Bundle data = getArguments();
				if (data != null && data.containsKey(EXTRA_MEDIA_ITEM)) {
					// retrieves the given Media item for the activity.
					mMediaItem = (MediaItem) data
							.getSerializable(EXTRA_MEDIA_ITEM);
				} else {
					Logger.e(TAG, "No MediaItem set for the given Activity.");
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (mMediaItem != null) {
			if (mMediaItem.getMediaType() == MediaType.ALBUM
					&& !TextUtils.isEmpty(mMediaItem.getAlbumName()))
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(mMediaItem.getAlbumName(),
						"");
			else
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(mMediaItem.getTitle(), "");
		}

        ((MainActivity)getActivity()). getSupportActionBar().setHomeAsUpIndicator(
                R.drawable.abc_ic_ab_back_mtrl_am_alpha_normal);
        ((MainActivity)getActivity()). getSupportActionBar().setHomeButtonEnabled(true);
        ((MainActivity)getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(getActivity()!=null)
                    onBackPressed();
            }
        });

	}

	@Override
    public void onStop() {
		super.onStop();
		Logger.s("onStop MediaDetailsActivity");
		// HungamaApplication.activityStoped();
		Analytics.onEndSession(getActivity());
	}

	@Override
    public void onPause() {
		Logger.s("onPause MediaDetailsActivity");
		HungamaApplication.activityPaused();

		super.onPause();
	}

//	ColorDrawable cd_main;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void updateTitleColor(ColorDrawable cd, boolean needalpha) {

		if (needalpha)
			cd.setAlpha(160);
        int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
            ((MainActivity) getActivity()).mToolbar.setBackgroundDrawable(cd);
        } else {
            ((MainActivity) getActivity()).mToolbar.setBackground(cd);
        }
	}


	@Override
	public void onMediaItemOptionSaveOfflineSelected(MediaItem mediaItem,
			int position) {
		if (mMediaItem != null)
			mediaItem.tag = mMediaItem;
		else if (mMediaItemTrack != null)
			mediaItem.tag = mMediaItemTrack;

		if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
			CacheManager.saveOfflineAction(getActivity(), mediaItem, null);
			Utils.saveOfflineFlurryEvent(
                    getActivity(),
					FlurryConstants.FlurryCaching.LongPressMenuVideo.toString(),
					mediaItem);
		}
	}

	class CacheStateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Logger.s("========================= cachestateupdatereceived ========"
					+ arg1.getAction());
			if (arg1.getAction()
					.equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
					|| arg1.getAction()
							.equals(CacheManager.ACTION_TRACK_CACHED)) {
				try {
					// ((BaseAdapter)
					// mediaDetailsFragment.text_view_media_details_list.getAdapter())
					// .notifyDataSetChanged();
					ListView mList = (ListView) rootView.findViewById(R.id.text_view_media_details_list);
//					((BaseAdapter) mList.getAdapter()).notifyDataSetChanged();
                    ((BaseAdapter) ((HeaderViewListAdapter) mList.getAdapter()).getWrappedAdapter()).notifyDataSetChanged();

				} catch (Exception e) {
					Logger.printStackTrace(e);
					// break;
				}
				try {
					// final Button mActionButtonSaveOffline = (Button)
					// findViewById(R.id.button_media_details_save_offline);
					final LinearLayout mActionButtonSaveOffline = (LinearLayout) rootView.findViewById(R.id.rl_media_details_save_offline);
					final TextView tvCacheState = (TextView) mActionButtonSaveOffline
							.findViewById(R.id.media_details_text_cache_state);
					final CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) mActionButtonSaveOffline
							.findViewById(R.id.media_details_progress_cache_state);
					MediaItem mediaItem = null;
					if (mMediaItemTrack != null)
						mediaItem = mMediaItemTrack;
					else
						mediaItem = mMediaItem;
					if (mActionButtonSaveOffline != null && mediaItem != null) {
						CacheState cacheState = null;
						Logger.s("MediaType ::::::::::::: "
								+ mediaItem.getMediaType() + " ::: "
								+ mediaItem.getTitle());
						if (mediaItem.getMediaType() == MediaType.ALBUM) {
							progressCacheState.setCacheCountVisibility(true);
							progressCacheState.setCacheCount(""
									+ DBOHandler.getAlbumCachedCount(
                                    getActivity(), ""
													+ mediaItem.getId()));
							cacheState = DBOHandler.getAlbumCacheState(
                                    getActivity(),
									"" + mediaItem.getId());
						} else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
							progressCacheState.setCacheCountVisibility(true);
							progressCacheState.setCacheCount(""
									+ DBOHandler.getPlaylistCachedCount(
                                    getActivity(), ""
													+ mediaItem.getId()));
							cacheState = DBOHandler.getPlaylistCacheState(
                                    getActivity(),
									"" + mediaItem.getId());
						} else if (mediaItem.getMediaType() == MediaType.TRACK) {
							cacheState = DBOHandler.getTrackCacheState(
                                    getActivity(),
									"" + mediaItem.getId());
						}
						if (cacheState != null) {
							Logger.s("cacheState :::: " + cacheState);
							// if(prevCacheState!=cacheState){
							if (cacheState == CacheState.CACHED) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (mMediaItem.getMediaType() == MediaType.ALBUM) {
                                            int trackCacheCount = DBOHandler
                                                    .getAlbumCachedCount(
                                                            getActivity(),
                                                            ""
                                                                    + mMediaItem
                                                                    .getId());
                                            if (trackCacheCount >= mMediaItem
                                                    .getMusicTrackCount())
                                                mActionButtonSaveOffline
                                                        .setTag(true);
                                        } else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
                                            int trackCacheCount = DBOHandler
                                                    .getPlaylistCachedCount(
                                                            getActivity(),
                                                            ""
                                                                    + mMediaItem
                                                                    .getId());
                                            if (trackCacheCount >= mMediaItem
                                                    .getMusicTrackCount())
                                                mActionButtonSaveOffline
                                                        .setTag(true);
                                        } else {
                                            mActionButtonSaveOffline
                                                    .setTag(true);
                                        }

                                        tvCacheState
                                                .setText(getResources()
                                                        .getString(
                                                                R.string.caching_text_play_offline_capital));
                                    }
                                });
								// if(isCachingComplete())
								// break;
							} else if (cacheState == CacheState.CACHING
									|| cacheState == CacheState.QUEUED) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        mActionButtonSaveOffline.setTag(null);

                                        tvCacheState
                                                .setText(getResources()
                                                        .getString(
                                                                R.string.caching_text_saving_capital));
                                    }
                                });
							} else {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        mActionButtonSaveOffline.setTag(false);
                                        tvCacheState
                                                .setText(getResources()
                                                        .getString(
                                                                R.string.media_details_custom_dialog_long_click_general_save_offline_caps));
                                    }
                                });
							}
							progressCacheState.setCacheState(cacheState);
							if (mediaItem.getMediaType() == MediaType.ALBUM
									&& cacheState == CacheState.NOT_CACHED
									&& DBOHandler.getAlbumCachedCount(
                                    getActivity(), ""
													+ mediaItem.getId()) > 0) {
								progressCacheState.setCacheCount(""
										+ DBOHandler.getAlbumCachedCount(
                                        getActivity(), ""
														+ mediaItem.getId()));
								progressCacheState
										.setCacheState(CacheState.CACHED);
								tvCacheState
										.setText(getResources()
												.getString(
														R.string.caching_text_play_offline_capital));
							}
							if (mediaItem.getMediaType() == MediaType.TRACK)
								progressCacheState.setProgress(DBOHandler
										.getTrackCacheProgress(
                                                getActivity(), ""
														+ mediaItem.getId()));
							// prevCacheState = cacheState;
							// }
						}
					} else {
						Logger.s(" :::: cacheState is null :::: ");
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
					// break;
				}
				try {
					if (dialog != null && dialog.isShowing()) {
						CustomCacheStateProgressBar progressCacheState = (CustomCacheStateProgressBar) dialog
								.findViewById(R.id.long_click_custom_dialog_save_offline_progress_cache_state);
						Track track = (Track) progressCacheState
								.getTag(R.id.view_tag_object);
						if (track != null) {
							CacheState cacheState;
							int progress = 0;
							cacheState = DBOHandler.getTrackCacheState(
                                    getActivity(),
									"" + track.getId());
							progress = DBOHandler.getTrackCacheProgress(
                                    getActivity(),
									"" + track.getId());
							LinearLayout llSaveOffline = (LinearLayout) dialog
									.findViewById(R.id.long_click_custom_dialog_save_offline_row);
							if (cacheState == CacheState.CACHED) {
								llSaveOffline.setTag(true);
								((TextView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(getResources()
												.getString(
														R.string.caching_text_play_offline));
								((ImageView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saved);
							} else if (cacheState == CacheState.CACHING
									|| cacheState == CacheState.QUEUED) {
								llSaveOffline.setTag(null);
								((TextView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(getResources().getString(
												R.string.caching_text_saving));
								((ImageView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saving);
							} else if (cacheState == CacheState.NOT_CACHED) {
								llSaveOffline.setTag(false);
								((TextView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_text))
										.setText(getResources()
												.getString(
														R.string.caching_text_save_offline));
								((ImageView) dialog
										.findViewById(R.id.long_click_custom_dialog_save_offline_image))
										.setImageResource(R.drawable.icon_media_details_saveoffline);
							}
							progressCacheState.setCacheState(cacheState);
							progressCacheState.setProgress(progress);
						}
					}
				} catch (Exception e) {
					Logger.printStackTrace(e);
				}
				if (((MainActivity) getActivity()).mPlayerBarFragment != null)
                    ((MainActivity) getActivity()).mPlayerBarFragment.updatedCurrentTrackCacheState();
			} else if (arg1.getAction().equals(
					CacheManager.ACTION_UPDATED_CACHE)) {

			}
		}
	}

    @Override
	public void onDestroyView() {
		super.onDestroy();
		if (cacheStateReceiver != null)
            getActivity().unregisterReceiver(cacheStateReceiver);
		cacheStateReceiver = null;
	}

	@Override
	public void onMediaItemSelected(Track track, String item) {
	}

	@Override
	public void onMediaItemSelectedPosition(Track track, int id) {
		switch (id) {
            case 0:
                MediaItem trackMediaItem = new MediaItem(track.getId(),
                        track.getTitle(), track.getAlbumName(),
                        track.getArtistName(), track.getImageUrl(),
                        track.getBigImageUrl(), MediaType.TRACK.toString(), 0, 0,
                        track.getImages(), track.getAlbumId());
                Intent intent = new Intent(
                        getActivity(),
                        TrendNowActivity.class);
                intent.putExtra(
                        TrendNowActivity.EXTRA_DATA_MEDIA_ITEM,
                        (Serializable) trackMediaItem);

                startActivity(intent);
                break;
		case 1:
			// download
			if(getActivity()!=null){
				((MainActivity) getActivity()).isSkipResume=true;
				trackMediaItem = new MediaItem(track.getId(),
						track.getTitle(), track.getAlbumName(),
						track.getArtistName(), track.getImageUrl(),
						track.getBigImageUrl(), MediaType.TRACK.toString(), 0, 0,
						track.getImages(), track.getAlbumId());
				intent = new Intent(getActivity().getBaseContext(),
						DownloadConnectingActivity.class);
				intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
						(Serializable) trackMediaItem);
				startActivity(intent);

				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
						track.getTitle());
				reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
						mFlurrySubSectionDescription);
				Analytics.logEvent(
						FlurryConstants.FlurryEventName.Download.toString(),
						reportMap);
			}

			break;

		case 2:
			List<Track> tracks = new ArrayList<Track>();
			tracks.add(track);
			addToQueueButtonClickActivity(tracks, null, null);

			Map<String, String> reportMap = new HashMap<String, String>();

			reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
					track.getTitle());
			reportMap.put(mMediaItem.getMediaType().toString(),
					Utils.toWhomSongBelongto(mMediaItem));
			reportMap
					.put(FlurryConstants.FlurryKeys.Source.toString(),
							FlurryConstants.FlurrySourceDescription.TapOnAddToQueueInContextualMenu
									.toString());
			reportMap.put(FlurryConstants.FlurryKeys.SubSection.toString(),
					mFlurrySubSectionDescription);

			Analytics.logEvent(
					FlurryConstants.FlurryEventName.SongSelectedForPlay
							.toString(), reportMap);
			break;
		case 3:
			trackMediaItem = new MediaItem(track.getId(), track.getTitle(),
					null, null, null, null, MediaType.TRACK.toString(), 0,
					track.getAlbumId());
			trackMediaItem.setAlbumId(track.getAlbumId());
			trackMediaItem.setMediaContentType(MediaContentType.MUSIC);
			trackMediaItem.setMediaType(MediaType.TRACK);
            trackMediaItem.setImagesUrlArray(track.getImages());


			openTrackPage(trackMediaItem);
			break;

		default:
			break;
		}
	}

	@Override
	public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
			int position) {
	}

}
