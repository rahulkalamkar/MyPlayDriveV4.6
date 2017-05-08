package com.hungama.hungamamusic.lite.carmode.fragment;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.bosch.myspin.serversdk.MySpinException;
import com.bosch.myspin.serversdk.MySpinServerSDK;
import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicListAdapter;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicListAdapter.MediaItemEvents;
import com.hungama.hungamamusic.lite.carmode.listeners.EndlessScrollListener;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout.DialogType;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout.IDialogListener;
import com.hungama.hungamamusic.lite.carmode.view.VerticalSeekBar;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.Category;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaItemsResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperationPaging;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.CarModeHomeActivity;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListMusicFragment extends ListFragment implements CommunicationOperationListener {

    public static final String TAG = ListMusicFragment.class.getSimpleName();

    private static final int CODE_FAVORITE_SUCCESS = 1;
    private static final String MEDIA_TYPE_SONG = "song";
    public PlayerService mPlayerService = null;
    // UI elements
    private View mRootView;
    private FrameLayout flList;
    //    private LinearLayout llMusic;
    private LinearLayout llMusicOptions;
    //    private RelativeLayout rlListMusics;
    private VerticalSeekBar sbListViewProgress;
    private SwipeRefreshLayout mListViewContainer;
    private LinearLayout llListViewProgressControl;
    private Button btnFavorite;
    private Button btnDownload;
    private Button btnAddQueue;
    private Button btnPlayNow;
    private ProgressBar pbDownloadInQueued;
    private ProgressBar pbLoadMedia;
    private ProgressBar pbFavorite;
    private MusicListAdapter mMusicListAdapter;
    private DataManager mDataManager;
    private List<MediaItem> mListMediaItems;
    private List<Long> mListFavorites;
    private List<Long> mListMediaInQueue;
    private MediaItem mSelectedMedia;
    private int mTotalMediaItems;
    private IListMusicListener mListener;
    private MediaCategoryType mCateType;
    private int mScreenWidthInPxl = 0;
    private int tmpSavedScrollerWidth = 0;
    private boolean isScrolling = true;
    private boolean isSaveOffline;
    private boolean isLoadMore;
    private boolean isExpanded;
    private boolean isDoneLoadFavorites;
    private CustomDialogLayout mDialog;
    private long currPlayingAlbumId = 0;

    public static final ListMusicFragment newInstance(IListMusicListener listener, MediaCategoryType cateType) {
        final ListMusicFragment fragment = new ListMusicFragment();
        fragment.setListener(listener);
        fragment.setMediaCategory(cateType);

        return fragment;
    }

    public void setListener(IListMusicListener listener) {
        this.mListener = listener;
    }

    public void setMediaCategory(MediaCategoryType cateType) {
        this.mCateType = cateType;
    }

    public void handleCacheState() {
        if (btnDownload != null && isExpanded) {
            CacheState cacheState = CacheState.NOT_CACHED;

            switch (mSelectedMedia.getMediaType()) {
                case ALBUM:
                    cacheState = DBOHandler.getAlbumCacheState(getActivity(), "" + mSelectedMedia.getId());
                    if (cacheState == CacheState.NOT_CACHED && (DBOHandler.getAlbumCachedCount(getActivity(), "" + mSelectedMedia.getId()) > 0)) {
                        cacheState = CacheState.CACHED;
                    }
                    break;

                case PLAYLIST:
                    cacheState = DBOHandler.getPlaylistCacheState(getActivity(), "" + mSelectedMedia.getId());
                    if (cacheState == CacheState.NOT_CACHED && (DBOHandler.getPlaylistCachedCount(getActivity(), "" + mSelectedMedia.getId()) > 0)) {
                        cacheState = CacheState.CACHED;
                    }
                    break;

                case TRACK:
                    cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + mSelectedMedia.getId());
                    break;
            }

            pbDownloadInQueued.setVisibility(View.GONE); // Reset before updating new state.

            switch (cacheState) {
                case CACHED:
                    btnDownload.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_cache_state_cached), null, null, null);
                    btnDownload.setText(R.string.caching_text_play_offline);
                    break;
                case CACHING:
                    btnDownload.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_saving_started), null, null, null);
                    pbDownloadInQueued.setVisibility(View.VISIBLE);
                    btnDownload.setText(R.string.caching_text_saving);
                    break;
                case FAILED:
                    break;
                case NOT_CACHED:
                    btnDownload.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_download), null, null, null);
                    btnDownload.setText(R.string.btn_download);
                    break;
                case QUEUED:
                    btnDownload.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_cache_state_queued), null, null, null);
                    btnDownload.setText("Queued");
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mDataManager = DataManager.getInstance(getActivity());
        this.mPlayerService = PlayerService.service;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (View) inflater.inflate(R.layout.carmode_fragment_list_musics, container, false);

            flList = (FrameLayout) mRootView.findViewById(R.id.fl_list);
//            llMusic = (LinearLayout) mRootView.findViewById(R.id.ll_music);
            llMusicOptions = (LinearLayout) mRootView.findViewById(R.id.ll_music_options);
//            rlListMusics = (RelativeLayout) mRootView.findViewById(R.id.rl_list_musics);
            sbListViewProgress = (VerticalSeekBar) mRootView.findViewById(R.id.sb_listview_progress);
            mListViewContainer = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipeRefreshLayout_listView);
            llListViewProgressControl = (LinearLayout) mRootView.findViewById(R.id.ll_listview_progress_control);
            btnFavorite = (Button) mRootView.findViewById(R.id.btn_favorite);
            btnDownload = (Button) mRootView.findViewById(R.id.btn_download);
            btnAddQueue = (Button) mRootView.findViewById(R.id.btn_add_to_queue);
            btnPlayNow = (Button) mRootView.findViewById(R.id.btn_play_now);
            pbLoadMedia = (ProgressBar) mRootView.findViewById(R.id.pb_load_media);
            pbDownloadInQueued = (ProgressBar) mRootView.findViewById(R.id.pb_download_queued);
            pbFavorite = (ProgressBar) mRootView.findViewById(R.id.pb_favorite);
        }

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        vInitUI();

        // List View's seekbar
        sbListViewProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // only scroll list when seekbar is changed by user
                if (fromUser == true) {
                    getListView().setSelection(progress);
                }
            }
        });

        if (mMusicListAdapter == null) { // First load data.
            vLoadData(1);
        }

        getListView().setOnScrollListener(new EndlessScrollListener(mListViewContainer) {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (totalItemsCount == mTotalMediaItems) {
                    mMusicListAdapter.setReachedTotalItems(true);
                    mMusicListAdapter.notifyDataSetChanged();
                } else {
                    isLoadMore = true;
                    vLoadData(page);
                }
            }

            @Override
            public void onSyncWithSeekbar(int firstVisibleItem) {
                sbListViewProgress.setProgress(firstVisibleItem);
            }

        });

        mListViewContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (mListMediaItems != null) {
                    mListMediaItems.clear(); // Reset data before fetching data.
                }
                vLoadData(1); // Fetching Data.
            }
        });

        mListViewContainer.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_orange_light, android.R.color.holo_green_light, android.R.color.holo_red_light);

        getListView().setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (isScrolling) {
                    return false;
                } else {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            }
        });

        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final MediaItem mediaItem = mListMediaItems.get(position);
                final MediaType mediaType = mediaItem.getMediaType();

//                if (mediaType == MediaType.ALBUM || mediaType == MediaType.PLAYLIST) {
                mListener.goToMusicDetail(mediaItem);
//                } else if (mediaType == MediaType.TRACK) {

//                }

            }
        });

        mListViewContainer.setDistanceToTriggerSync(100);
    }

    private void vInitUI() {
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();

        int width = 0;
        try {
            final Point screenSize = MySpinServerSDK.sharedInstance().getScreenSize();
            width = screenSize.x;
        } catch (MySpinException e) {
            e.printStackTrace();
        }

        if (width >= 760) {
            mScreenWidthInPxl = (int) (metrics.heightPixels / 1.08);
        } else if (width >= 570) {
            mScreenWidthInPxl = (int) (metrics.heightPixels / 1.43);
        } else {
            mScreenWidthInPxl = (int) (metrics.widthPixels);
        }

//        this.llMusic.getLayoutParams().width = mScreenWidthInPxl;
        tmpSavedScrollerWidth = this.llListViewProgressControl.getLayoutParams().width;
//        this.rlListMusics.getLayoutParams().width = mScreenWidthInPxl - tmpSavedScrollerWidth;
    }

    private void vLoadData(int offset) {
        String timestamp_cache = mDataManager.getApplicationConfigurations().getMusicPopularTimeStamp();
        String length = String.valueOf(Constants.LOADING_CHUNK_NUMBER);
        String start = String.valueOf((offset == 1) ? offset : (offset - 1) * Constants.LOADING_CHUNK_NUMBER);
        String strSelectedCategory = mDataManager.getApplicationConfigurations().getSelctedMusicPreference();
        Category savedCategory = new Category(-1, strSelectedCategory, null);

        // Get Musics by Category.
        mDataManager.getMediaItemsPaging(MediaContentType.MUSIC, this.mCateType, savedCategory, start, length, this, timestamp_cache);

        // Get Favorites for al
        mDataManager.getFavorites(getActivity(), MediaType.ALBUM, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);

        // Get Media in Queue
        mListMediaInQueue = new ArrayList<Long>();
        for (int i = 0; i < mPlayerService.getPlayingQueue().size(); i++) {
            mListMediaInQueue.add(mPlayerService.getPlayingQueue().get(i).getId());
        }
    }

    public void vHandlePopularMusicClicks(View v) {
        final int view_id = v.getId();

        switch (view_id) {

            case R.id.btn_scroll_up:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() - 1);
                getListView().setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_scroll_down:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() + 1);
                getListView().setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_view_detail:
                resetMusicLayout();
                mListener.goToMusicDetail(mSelectedMedia);
                break;

            case R.id.btn_play_now:
                if (mPlayerService != null) {

                    boolean allowToPlay = true;
                    /*if (mPlayerService.getCurrentPlayingTrack() == null
                            || mPlayerService.getState() == PlayerService.State.IDLE
                            || mPlayerService.getState() == PlayerService.State.STOPPED
                            || mPlayerService.getState() == PlayerService.State.PAUSED) { // Play Now if Player service is stopped or not started yet.
                        allowToPlay = true;
                    } else {
                        if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
                            allowToPlay = mPlayerService.getCurrentPlayingTrack().getId() != mSelectedMedia.getId(); // Play Now if not same as track
                        } else {
                            allowToPlay = mPlayerService.getCurrentPlayingTrack().getAlbumId() != mSelectedMedia.getId(); // Play Now if not same as album/playlist
                        }
                    }

                    if (!allowToPlay && (mPlayerService.isLoading() || mPlayerService.isPlaying()) && mPlayerService.getState() == PlayerService.State.PLAYING) {
                        mPlayerService.pause();
                        // Update button.
                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null, null);
                        btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
                    }*/

                    if(allowToPlay){

                        if (mSelectedMedia.getMediaType() == MediaType.TRACK && allowToPlay) {
                            final Track selectedTrack = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(), mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(), mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());
                            final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();

                            // Check whether track in Queue or Not.
                            /*if (tracksInQueue.contains(selectedTrack)) {
                                final int trackPos = tracksInQueue.indexOf(selectedTrack);
                                mPlayerService.playFromPosition(trackPos);
                            } else {
                                mPlayerService.playNow(Collections.singletonList(selectedTrack));
                            }*/
                            playNowNew(getActivity(), Collections.singletonList(selectedTrack), null, null);

                            mMusicListAdapter.vHandleCollapseOptions();

                            btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                            btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);
                        } else /*if (allowToPlay)*/ { // MediaType == Album/Playlist
                            mDataManager.getMediaDetails(mSelectedMedia, PlayerOption.OPTION_PLAY_NOW, this);

                            pbLoadMedia.setVisibility(View.VISIBLE);
                            btnPlayNow.setClickable(false);
                            btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                        }

                        mListener.onPlayNow();
                    }
                }

                break;

            case R.id.btn_download:
                if (mSelectedMedia.getMediaContentType() == MediaContentType.MUSIC) {
                    String msg = "";
                    if (mSelectedMedia.getTitle() != null && !mSelectedMedia.getTitle().isEmpty()) {
                        msg = getResources().getString(R.string.msg_save_offline, mSelectedMedia.getTitle());
                    } else if (mSelectedMedia.getAlbumName() != null && !mSelectedMedia.getAlbumName().isEmpty()) {
                        msg = getResources().getString(R.string.msg_save_offline, mSelectedMedia.getAlbumName());
                    }

                    if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
                        GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, msg, null);

                        Track track = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(), mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(), mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());
                        CacheManager.saveOfflineAction(getActivity(), mSelectedMedia, track);
                    } else if (mSelectedMedia.getMediaType() == MediaType.ALBUM || mSelectedMedia.getMediaType() == MediaType.PLAYLIST) {
                        if (MediaCachingTaskNew.isEnabled) {
                            isSaveOffline = true;
                            mDataManager.getMediaDetails(mSelectedMedia, PlayerOption.OPTION_SAVE_OFFLINE, this);
                        } else {
                            GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, msg, null);
                            CacheManager.saveOfflineAction(getActivity(), mSelectedMedia, null);
                        }
                    }
                }

                break;

            case R.id.btn_add_to_queue:

                if (mListMediaInQueue.contains(mSelectedMedia.getId())) {

                } else {
                    if (mSelectedMedia.getMediaContentType() == MediaContentType.MUSIC) {
                        if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
                            Track track = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(), mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(), mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());

                            List<Track> listTracks = new ArrayList<Track>();
                            listTracks.add(track);
                            mListener.onAddTrackToQueue(listTracks);
                        } else {
                            mDataManager.getMediaDetails(mSelectedMedia, PlayerOption.OPTION_ADD_TO_QUEUE, this);
                        }
                    }
                }

                break;

            case R.id.btn_favorite:
                String mediaType = mSelectedMedia.getMediaType().toString();

                if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
                    mediaType = MEDIA_TYPE_SONG;
                }

                if (mListFavorites != null && mListFavorites.contains(mSelectedMedia.getId())) {
                    mDataManager.removeFromFavorites(String.valueOf(mSelectedMedia.getId()), mediaType, this);
                } else {
                    mDataManager.addToFavorites(String.valueOf(mSelectedMedia.getId()), mediaType, this);
                }

                //Set updating Player flag.
                CarModeHomeActivity.setFlagNeedToLoadMusicPlayer(true);

                break;

            default:
                Log.e(TAG, "Cannot find view's id!!");
                break;
        }
    }

    @Override
    public void onStop() {
        mDataManager.cancelGetMediaItemsPaging(); // Cancel request.
        super.onStop();
    }

    // Reset layout before calling next fragment.
    public void resetMusicLayout() {
        if (isAdded()) {
            isScrolling = true;
            llMusicOptions.setVisibility(View.GONE);
            llListViewProgressControl.setVisibility(View.VISIBLE);
//        llListViewProgressControl.setAlpha(1);
//        llListViewProgressControl.getLayoutParams().width = tmpSavedScrollerWidth;
//        llMusic.getLayoutParams().width = mScreenWidthInPxl;
//        mRootView.requestLayout();

            if (mMusicListAdapter != null) {
                mMusicListAdapter.setSelectedPos(-1);
                mMusicListAdapter.setMediaMenuExpanded(false);
                mMusicListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void expandMusicOptions() {
        handleCacheState();
        updateFavoriteBtn();
        updatePlayNowStates();

//        final int targetMusicOptionsWidth = mScreenWidthInPxl / 3;
//        final int initMusicWidth = this.llMusic.getWidth();
//        final int initMusicControlWidth = this.llListViewProgressControl.getWidth();

        llMusicOptions.setVisibility(View.VISIBLE);
        llListViewProgressControl.setVisibility(View.GONE);

//        Animation a = new Animation() {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                llMusicOptions.getLayoutParams().width = (int) (targetMusicOptionsWidth * interpolatedTime);
//                llMusicOptions.setAlpha(interpolatedTime);
//                llMusic.getLayoutParams().width = (int) (initMusicWidth - (targetMusicOptionsWidth * interpolatedTime));
//
//                llListViewProgressControl.getLayoutParams().width = (int) (initMusicControlWidth * (1 - interpolatedTime));
//                llListViewProgressControl.setAlpha(1 - interpolatedTime);
//
//                if (interpolatedTime == 1) {
//                    llListViewProgressControl.setVisibility(View.GONE);
//                }
//
//                mRootView.requestLayout();
//            }
//
//            @Override
//            public boolean willChangeBounds() {
//                return true;
//            }
//        };
//
//        a.setDuration(500);
//        mRootView.startAnimation(a);
    }

    private void updatePlayNowStates() {
        if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
            /*if (!mPlayerService.isQueueEmpty()
                    && (mPlayerService.isPlaying() || mPlayerService.isLoading())
                    && (mSelectedMedia.getId() == mPlayerService.getCurrentPlayingTrack().getId())) {
                btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);
            } else {*/
                btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null, null);
                btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
            //}
            pbLoadMedia.setVisibility(View.GONE);
        } else { // MediaType == Album/Playlist
            pbLoadMedia.setVisibility(View.VISIBLE);
            btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mDataManager.getMediaDetails(mSelectedMedia, PlayerOption.OPTION_PLAY_NOW_AND_OPEN, this);
        }
    }

    private void updateFavoriteBtn() {
        if (mListFavorites != null && mListFavorites.contains(mSelectedMedia.getId())) {
            btnFavorite.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_favorite_added), null, null, null);
            btnFavorite.setText(R.string.btn_remove_favorite);
        } else {
            btnFavorite.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_favorites), null, null, null);
            btnFavorite.setText(R.string.btn_add_favorite);
        }

        if (isDoneLoadFavorites) {
            btnFavorite.setVisibility(View.VISIBLE);
            btnFavorite.setClickable(true);
            pbFavorite.setVisibility(View.GONE);
        } else {
            btnFavorite.setVisibility(View.INVISIBLE);
            btnFavorite.setClickable(false);
            pbFavorite.setVisibility(View.VISIBLE);
        }
    }

    private void collapseMusicOptions() {
        llMusicOptions.setVisibility(View.GONE);
        llListViewProgressControl.setVisibility(View.VISIBLE);
//        final int initMusicWidth = this.llMusic.getWidth();
//        final int initMusicOptionsWidth = this.llMusicOptions.getWidth();
//
//        final int targetMusicControlWidth = tmpSavedScrollerWidth;
//        final int targetMusicWidth = mScreenWidthInPxl;
//
//        llListViewProgressControl.setVisibility(View.VISIBLE);
//        Animation a = new Animation() {
//            @Override
//            protected void applyTransformation(float interpolatedTime, Transformation t) {
//                llMusicOptions.getLayoutParams().width = (int) (initMusicOptionsWidth * (1 - interpolatedTime));
//                llMusicOptions.setAlpha(1 - interpolatedTime);
//                llMusic.getLayoutParams().width = (int) (initMusicWidth + (targetMusicWidth - initMusicWidth) * interpolatedTime);
//                llListViewProgressControl.getLayoutParams().width = (int) (targetMusicControlWidth * interpolatedTime);
//                llListViewProgressControl.setAlpha(interpolatedTime);
//
//                if (interpolatedTime == 1) {
//                    llMusicOptions.setVisibility(View.GONE);
//                }
//
//                mRootView.requestLayout();
//            }
//
//            @Override
//            public boolean willChangeBounds() {
//                return true;
//            }
//        };
//
//        a.setDuration(500);
//        mRootView.startAnimation(a);
    }

    @Override
    public void onStart(int operationId) {
        String msg = "";

        switch (operationId) {
            case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST:
            case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED:
                if (!isLoadMore) {
                    flList.setVisibility(View.INVISIBLE);

                    if (getActivity() != null) {
                        ((CarModeHomeActivity) getActivity()).showLoadDialog();
                    }
                }
//                mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, getResources().getString(R.string.msg_fetching_data), null);


                break;
            case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
                isDoneLoadFavorites = false;
                break;

            case OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE:
                btnFavorite.setVisibility(View.INVISIBLE);
                btnFavorite.setClickable(false);
                pbFavorite.setVisibility(View.VISIBLE);
                break;

            case OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE:
                btnFavorite.setVisibility(View.INVISIBLE);
                btnFavorite.setClickable(false);
                pbFavorite.setVisibility(View.VISIBLE);
                break;
            case OperationDefinition.Hungama.OperationId.MEDIA_DETAILS:
                if (isSaveOffline) {
                    if (mSelectedMedia.getTitle() != null && !mSelectedMedia.getTitle().isEmpty()) {
                        msg = getResources().getString(R.string.msg_save_offline, mSelectedMedia.getTitle());
                    } else if (mSelectedMedia.getAlbumName() != null && !mSelectedMedia.getAlbumName().isEmpty()) {
                        msg = getResources().getString(R.string.msg_save_offline, mSelectedMedia.getAlbumName());
                    }
                    mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, msg, null);
                }
                break;

            default:
                break;
        }

    }

    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {

        switch (operationId) {
            case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_LATEST:

            case OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED:
                MediaItemsResponse mediaItemsResponse = (MediaItemsResponse) responseObjects.get(MediaContentOperationPaging.RESPONSE_KEY_OBJECT_MEDIA_ITEMS_RESPONSE);
                mTotalMediaItems = mediaItemsResponse.getTotal();

                if (mMusicListAdapter == null) {
                    mListMediaItems = mediaItemsResponse.getContent();
                    mMusicListAdapter = new MusicListAdapter(getActivity(), mListMediaItems);
                    mMusicListAdapter.setMediaItemEvents(new MediaItemEvents() {

                        @Override
                        public void onExpandMenu(int mediaItemPos) {
                            mSelectedMedia = mListMediaItems.get(mediaItemPos);
                            isExpanded = true;
                            isScrolling = false; // Flag for disable scrolling.

                            expandMusicOptions();
                        }

                        @Override
                        public void onCollapseMenu() {
                            collapseMusicOptions();
                            isScrolling = true;
                            isExpanded = false;
                        }

                    });

                    if (isVisible()) {
                        getListView().setAdapter(mMusicListAdapter);
                    }
                } else {
                    mListMediaItems.addAll(mediaItemsResponse.getContent());

                    if (isVisible()) {
                        mMusicListAdapter.notifyDataSetChanged();
                    }
                }

                sbListViewProgress.setMax(mListMediaItems.size());
                mListViewContainer.setRefreshing(false);

                flList.setVisibility(View.VISIBLE);

                if (getActivity() != null) {
                    ((CarModeHomeActivity) getActivity()).hideLoadDialog();
                }

                break;

            case OperationDefinition.Hungama.OperationId.MEDIA_DETAILS:
                MediaItem mediaItem = (MediaItem) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);
                if (mediaItem != null && (mediaItem.getMediaType() == MediaType.ALBUM || mediaItem.getMediaType() == MediaType.PLAYLIST)) {
                    MediaSetDetails setDetails = (MediaSetDetails) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
                    PlayerOption playerOptions = (PlayerOption) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_PLAYER_OPTION);

                    List<Track> tracks = setDetails.getTracks();
                    if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
                        for (Track track : tracks) {
                            track.setTag(mediaItem);
                        }
                    } else if (mediaItem.getMediaType() == MediaType.ALBUM) {
                        for (Track track : tracks) {
                            track.setAlbumId(setDetails.getContentId());
                        }
                    }

                    if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
                        mListener.onAddTrackToQueue(tracks);
                    } else if (playerOptions == PlayerOption.OPTION_SAVE_OFFLINE) {
                        if (mediaItem.getMediaType() == MediaType.ALBUM) {
                            for (Track track : tracks) {
                                track.setTag(mediaItem);
                            }
                        }
                        mDialog.hide();
                        isSaveOffline = false;

                        CacheManager.saveAllTracksOfflineAction(getActivity(), tracks);
                    } else if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {

                        // Play list of songs.
                        playNowNew(getActivity(), tracks, null, null);
                        /*final List<Track> queue = mPlayerService.getPlayingQueue();
                        final ArrayList<Track> tracksNotInQueue = new ArrayList<Track>();
                        for (Track tempTrack : tracks) {
                            if (!queue.contains(tempTrack)) {
                                tracksNotInQueue.add(tempTrack);
                            }
                        }

                        if (tracksNotInQueue.size() > 0) {
                            mPlayerService.playNow(tracksNotInQueue);
                        }*/
                        mMusicListAdapter.vHandleCollapseOptions();

                        pbLoadMedia.setVisibility(View.GONE);
                        btnPlayNow.setClickable(true);
                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                        btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);
                    } else if (playerOptions == PlayerOption.OPTION_PLAY_NOW_AND_OPEN) { // Check if current PlayList/Album is playing
                        boolean isPlaying = false;

                        /*if (!mPlayerService.isQueueEmpty()
                                && (mPlayerService.getCurrentPlayingTrack() != null)
                                && (mPlayerService.isPlaying() || mPlayerService.isLoading())) {
                            final long curTrackId = mPlayerService.getCurrentPlayingTrack().getId();
                            for (Track track : tracks) {
                                if (track.getId() == curTrackId) {
                                    btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                                    btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);
                                    isPlaying = true;
                                    break;
                                }
                            }
                        }*/

                        if (!isPlaying) { // Current Playing song is not belong to this Album/Playlist.
                            btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null, null);
                            btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
                        }

                        pbLoadMedia.setVisibility(View.GONE);
                    }
                }

                break;

            case OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE:
                BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);

                // has the item been added from favorites.
                if (addToFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS) {
                    GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, addToFavoriteResponse.getMessage(), null);
                    btnFavorite.setText(R.string.btn_remove_favorite); // Update button title.
                    btnFavorite.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_favorite_added), null, null, null);

                    if (mListFavorites == null) {
                        mListFavorites = new ArrayList<Long>();
                    }

                    mListFavorites.add(mSelectedMedia.getId());
                }

                btnFavorite.setVisibility(View.VISIBLE);
                btnFavorite.setClickable(true);
                pbFavorite.setVisibility(View.GONE);
                break;

            case OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE:
                BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);

                // has the item been removed from favorites.
                if (removeFromFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS) {
                    GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, removeFromFavoriteResponse.getMessage(), null);
                    btnFavorite.setText(R.string.btn_add_favorite); // Update button title.
                    btnFavorite.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_favorites), null, null, null);
                    mListFavorites.remove(mSelectedMedia.getId());
                }

                btnFavorite.setVisibility(View.VISIBLE);
                btnFavorite.setClickable(true);
                pbFavorite.setVisibility(View.GONE);

                break;

            case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
                ProfileFavoriteMediaItems profileFavoriteMediaItems = (ProfileFavoriteMediaItems) responseObjects.get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS);
                if (profileFavoriteMediaItems == null) {
//                    mDialog.hide(); // Dismiss Fetching dialog.
                    isDoneLoadFavorites = true;
                    if (isExpanded) {
                        updateFavoriteBtn();
                    }
                } else {
                    List<MediaItem> listItems = profileFavoriteMediaItems.mediaItems;

                    if ((listItems != null) && (listItems.size() > 0)) {
                        if (mListFavorites == null) {
                            mListFavorites = new ArrayList<Long>();
                        }

                        for (int i = 0; i < listItems.size(); i++) {
                            mListFavorites.add(listItems.get(i).getId());
                        }

                        if (listItems.get(0).getMediaType() == MediaType.ALBUM) { // After done with Album favorite, continue with Playlist
                            mDataManager.getFavorites(getActivity(), MediaType.PLAYLIST, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);
                        } else if (listItems.get(0).getMediaType() == MediaType.PLAYLIST) {  // After done with Playlist favorite, continue with Song
                            mDataManager.getFavorites(getActivity(), MediaType.TRACK, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);
                        } else {
                            isDoneLoadFavorites = true;

                            if (isExpanded) {
                                updateFavoriteBtn();
                            }
                        }
                    } else {
                        isDoneLoadFavorites = true;
                        if (isExpanded) {
                            updateFavoriteBtn();
                        }
                    }
                }

                break;

            default:
                break;
        }
    }



    public static void playNowNew(Activity act,  final List<Track> tracks,
                           final String flurryEventName, final String flurrySourceSection) {
        Resources resources = act.getResources();
        PlayerService mPlayerService = PlayerService.service;
        if(mPlayerService==null)
            return;
        String mMessageSongToQueue = resources
                .getString(R.string.main_player_bar_message_song_added_to_queue);
        String mMessageSongsToQueue = resources
                .getString(R.string.main_player_bar_message_songs_added_to_queue);
        String mMessageSongInQueue = resources
                .getString(R.string.main_player_bar_message_song_already_in_queue);
        Track currentPlayingTrackTemp;
        Logger.i("MediaTilesAdapter", "Play button click: PlayNow 8");
        if (!Utils.isListEmpty(tracks)) {
            Logger.i("MediaTilesAdapter", "Play button click: PlayNow 9");
            try {
                final List<Track> queue = mPlayerService.getPlayingQueue();
                Logger.i("MediaTilesAdapter", "Play button click: PlayNow 10");
                if (tracks.size() > 1) {

                    StringBuilder tracksNotInQueueStr = new StringBuilder();

                    currentPlayingTrackTemp = mPlayerService
                            .getCurrentPlayingTrack();
                    Logger.s(" ::::::::::::: addToQueue 2");
                    final ArrayList<Track> tracksNotInQueue = new ArrayList<Track>();
                    for (Track track : tracks) {
                        if (!queue.contains(track)) {
                            tracksNotInQueue.add(track);
                            tracksNotInQueueStr.append(track.getTitle()).append(",");
                        } else {
                            int removePos = mPlayerService.getPlayingQueue().indexOf(track);
                            mPlayerService.removeFromQueueWhenAddToQueue(removePos);
                        }
                    }

                    Logger.i("MediaTilesAdapter",
                            "Play button click: PlayNow 11");
                    if (tracks.size() > 0) {
                        Track firstTrack = tracks.get(0);
                        int trackPosition = -1;//queue.lastIndexOf(tracks.get(0));
                        if (mPlayerService != null
                                && currentPlayingTrackTemp != null
                                && firstTrack.getId() == currentPlayingTrackTemp.getId()) {
                            //helperAddToQueue(tracksNotInQueue);
                            //helperAddToQueue(tracks);
                            mPlayerService.addToQueue(tracks);
                            mPlayerService.setCurrentPos(currentPlayingTrackTemp);
                            if(mPlayerService.getState() != PlayerService.State.PLAYING){
                                mPlayerService.play();
                                GlobalFunction.showMessageDialog(act, DialogType.MESSAGE, mMessageSongsToQueue, null);
                            }else if(mPlayerService.isPlaying() && mPlayerService.getState() == PlayerService.State.PLAYING){
                                GlobalFunction.showMessageDialog(act, DialogType.MESSAGE, act.getString(R.string.queue_bottom_text_now_playing), null);
                                /*Utils.makeText(mPlayerService,Utils.getMultilanguageText(act,
                                        act.getString(R.string.queue_bottom_text_now_playing)), Toast.LENGTH_SHORT).show();*/
                            }
                        }
                        else if (trackPosition == -1) {
                            mPlayerService.playNow(tracks);
                            //helperPlayNow(tracks);
                            GlobalFunction.showMessageDialog(act, DialogType.MESSAGE, mMessageSongsToQueue, null);
                        }
                        else {
                            mPlayerService.playNowFromPosition(
                                    tracks, trackPosition);
                            GlobalFunction.showMessageDialog(act, DialogType.MESSAGE, mMessageSongsToQueue, null);
                        }

                        /*Utils.makeText(act, mMessageSongsToQueue,
                                Toast.LENGTH_SHORT).show();*/
                        if (flurryEventName != null
                                && flurrySourceSection != null) {

                            // Flurry report :songs add to queue
                            Map<String, String> reportMap = new HashMap<String, String>();
                            reportMap.put(
                                    FlurryConstants.FlurryKeys.SourceSection
                                            .toString(), flurrySourceSection);
                            reportMap
                                    .put(FlurryConstants.FlurryKeys.SongsAddedToQueue
                                            .toString(), tracksNotInQueueStr
                                            .toString());
                            Analytics.logEvent(flurryEventName, reportMap);
                        }
                    }
                    Logger.i("MediaTilesAdapter",
                            "Play button click: PlayNow 12");
                } else {
                    currentPlayingTrackTemp = mPlayerService
                            .getCurrentPlayingTrack();
                    if (mPlayerService != null
                            && currentPlayingTrackTemp != null && tracks.size()==1
                            && tracks.get(0).getId() == currentPlayingTrackTemp.getId()) {
                        //helperAddToQueue(tracksNotInQueue);
                        if(mPlayerService.getState() != PlayerService.State.PLAYING){
                            mPlayerService.play();
                        }else if(mPlayerService.isPlaying() && mPlayerService.getState() == PlayerService.State.PLAYING){
                            GlobalFunction.showMessageDialog(act, DialogType.MESSAGE, act.getString(R.string.queue_bottom_text_now_playing), null);
                            //Utils.makeText(act,Utils.getMultilanguageText(act, act.getString(R.string.queue_bottom_text_now_playing)), Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }

                    if(tracks.size()==1){
                        int pos = mPlayerService.getPlayingQueue().indexOf(tracks.get(0));
                        if(pos>=0){
                            mPlayerService.playFromPosition(pos);
                            return;
                        }
                    }

                    final ArrayList<Track> tracksNotInQueue = new ArrayList<Track>();
                    for (Track track : tracks) {
                        if (!queue.contains(track)) {
                            tracksNotInQueue.add(track);
                            //tracksNotInQueueStr.append(track.getTitle()).append(",");
                        } else {
                            int removePos = mPlayerService.getPlayingQueue().indexOf(track);
                            mPlayerService.removeFromQueueWhenAddToQueue(removePos);
                        }
                    }

                    if (mPlayerService != null
                            && currentPlayingTrackTemp != null
                            && tracks.get(0).getId() == currentPlayingTrackTemp.getId()) {
                        //helperAddToQueue(tracksNotInQueue);
                        //helperAddToQueue(tracks);
                        mPlayerService.addToQueue(tracks);
                        mPlayerService.setCurrentPos(currentPlayingTrackTemp);
                        if(mPlayerService.getState() != PlayerService.State.PLAYING){
                            mPlayerService.play();
                        }else if(mPlayerService.isPlaying() && mPlayerService.getState() == PlayerService.State.PLAYING){
                            GlobalFunction.showMessageDialog(act, DialogType.MESSAGE, act.getString(R.string.queue_bottom_text_now_playing), null);
                            //Utils.makeText(act,Utils.getMultilanguageText(act, act.getString(R.string.queue_bottom_text_now_playing)), Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        //helperPlayNow(tracks);
                        mPlayerService.playNow(tracks);
                        // Flurry report :song add to queue
                        Map<String, String> reportMap = new HashMap<String, String>();
                        reportMap.put(FlurryConstants.FlurryKeys.SourceSection
                                .toString(), flurrySourceSection);
                        reportMap.put(
                                FlurryConstants.FlurryKeys.SongsAddedToQueue
                                        .toString(), tracks.get(0).getTitle());
                        Analytics.logEvent(flurryEventName, reportMap);
                        GlobalFunction.showMessageDialog(act, DialogType.MESSAGE, mMessageSongsToQueue, null);
                       /* Utils.makeText(act, mMessageSongsToQueue,
                                Toast.LENGTH_SHORT).show();*/
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
        mListViewContainer.setRefreshing(false);
        flList.setVisibility(View.VISIBLE);

        if (mDialog != null) {
            mDialog.hide(); // Dismiss dialog.
        }

        if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS) {
            isDoneLoadFavorites = true;
            if (isExpanded) {
                updateFavoriteBtn();
            }
        }


        if (getActivity() != null) {
            ((CarModeHomeActivity) getActivity()).hideLoadDialog();
        }

        if (errorType != ErrorType.NO_CONNECTIVITY) {
            GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, errorMessage, new IDialogListener() {

                @Override
                public void onPositiveBtnClick() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onNegativeBtnClick() {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onNegativeBtnClick(CustomDialogLayout layout) {

                }

                @Override
                public void onMessageAction() {
                    mListener.popBackStack();
                }
            });
        }

    }

    public interface IListMusicListener {
        void onAddTrackToQueue(List<Track> listTracks);

        void goToMusicDetail(MediaItem selectedMedia);

        void popBackStack();

        void onPlayNow();
    }


}


