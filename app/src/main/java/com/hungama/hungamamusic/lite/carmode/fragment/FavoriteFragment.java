package com.hungama.hungamamusic.lite.carmode.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicListAdapter;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicListAdapter.MediaItemEvents;
import com.hungama.hungamamusic.lite.carmode.adapters.RadioListAdapter;
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
import com.hungama.myplay.activity.data.audiocaching.DataBase;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.CarModeHomeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FavoriteFragment extends ListFragment implements CommunicationOperationListener {

    public final static String TAG = FavoriteFragment.class.getSimpleName();
    public final static String KEY_MEDIA_TYPE = "media_type";

    public final static int CODE_SELECTED_MEDIA_TYPE = 1000;
    private static final int CODE_FAVORITE_SUCCESS = 1;
    public PlayerService mPlayerService = null;
    private DataManager mDataManager;
    private List<MediaItem> mListFavoriteMedia = new ArrayList<MediaItem>();
    private MusicListAdapter mFavoriteListAdapter;
    private RadioListAdapter mRadioListAdapter;
    private IFavoriteListener mListener;
    private MediaType mCurMediaType = MediaType.ALBUM;
    private int tmpSavedScrollerWidth = 0;
    private int mScreenWidthInPxl = 0;
    private MediaItem mSelectedMedia;
    private boolean isScrolling = true;
    private CustomDialogLayout mDialog;
    private String strTitle = "";
    private boolean isExpanded;
    private boolean isSaveOffline;

    // UI Elements
    private View mRootView;
    private FrameLayout flList;
    private LinearLayout llMusicOptions;
    private LinearLayout llListViewProgressControl;
    private Button btnMediaType;
    private Button btnDownload;
    private Button btnPlayNow;
    private ProgressBar pbLoadMedia;
    private VerticalSeekBar sbListViewProgress;
    private ProgressBar pbDownloadInQueued;
    private TextView txtempty;

    public static FavoriteFragment newInstance(IFavoriteListener listener) {
        final FavoriteFragment fragment = new FavoriteFragment();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mPlayerService = PlayerService.service;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void setListener(IFavoriteListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (View) inflater.inflate(R.layout.carmode_fragment_favorite, container, false);

            flList = (FrameLayout) mRootView.findViewById(R.id.fl_list);
            llMusicOptions = (LinearLayout) mRootView.findViewById(R.id.ll_music_options);
            llListViewProgressControl = (LinearLayout) mRootView.findViewById(R.id.ll_listview_progress_control);
            btnMediaType = (Button) mRootView.findViewById(R.id.btn_media_type);
            btnDownload = (Button) mRootView.findViewById(R.id.btn_download);
            btnPlayNow = (Button) mRootView.findViewById(R.id.btn_play_now);
            pbLoadMedia = (ProgressBar) mRootView.findViewById(R.id.pb_load_media);
            sbListViewProgress = (VerticalSeekBar) mRootView.findViewById(R.id.sb_listview_progress);
            pbDownloadInQueued = (ProgressBar) mRootView.findViewById(R.id.pb_download_queued);
            txtempty=(TextView) mRootView.findViewById(R.id.empty);
            txtempty.setVisibility(View.GONE);
        }
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loadTitle();
//        vInitUI();

        mDataManager = DataManager.getInstance(getActivity());
        mDataManager.getFavorites(getActivity(), mCurMediaType, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);

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


        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final MediaItem mediaItem = mListFavoriteMedia.get(position);
                final MediaType mediaType = mediaItem.getMediaType();

                if (mediaType == MediaType.ALBUM || mediaType == MediaType.PLAYLIST) {
                    mListener.goToMusicDetail(mediaItem);
                } else if (mediaType == MediaType.TRACK) {
                    mListener.goToMusicDetail(mediaItem);
                } else if (mediaType == MediaType.ARTIST) {
                    final MediaItem radioItem = (MediaItem) parent.getItemAtPosition(position);
                    mListener.onGoToRadioPlayer(radioItem, PlayMode.TOP_ARTISTS_RADIO);
                }

            }
        });

        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    sbListViewProgress.setMax(totalItemCount - visibleItemCount);
                }
                sbListViewProgress.setProgress(firstVisibleItem);
            }
        });

        sbListViewProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

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
    }

//    private void vInitUI() {
//        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
//
//        mScreenWidthInPxl = (int) (metrics.heightPixels / 1.43);
//
//        this.llMusic.getLayoutParams().width = mScreenWidthInPxl;
//        tmpSavedScrollerWidth = this.llListViewProgressControl.getLayoutParams().width;
//        this.rlListMusics.getLayoutParams().width = mScreenWidthInPxl - tmpSavedScrollerWidth;
//    }


    public void updateFavoriteUI() {
        if (mCurMediaType == MediaType.ARTIST || mCurMediaType == MediaType.TRACK) {
            mDataManager.getFavorites(getActivity(), mCurMediaType, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);
        }
    }


    @Override
    public void onStart(int operationId) {
        String msg = "";

        switch (operationId) {
            case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
//                mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE,
//                        getResources().getString(R.string.msg_load_favorites, strTitle), null);
                if (getActivity() != null) {
                    ((CarModeHomeActivity) getActivity()).showLoadDialog();
                }

                break;

            case OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE:
                if (mSelectedMedia.getTitle() != null && !mSelectedMedia.getTitle().isEmpty()) {
                    msg = getResources().getString(R.string.msg_remove_favorites, mSelectedMedia.getTitle());
                } else if (mSelectedMedia.getAlbumName() != null && !mSelectedMedia.getAlbumName().isEmpty()) {
                    msg = getResources().getString(R.string.msg_remove_favorites, mSelectedMedia.getAlbumName());
                }
                mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, msg, null);
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
            case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
                ProfileFavoriteMediaItems profileFavoriteMediaItems = (ProfileFavoriteMediaItems) responseObjects
                        .get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS);
                try {

                    mListFavoriteMedia.clear(); // Clear list

                    if (profileFavoriteMediaItems == null) { // Return empty list.
                        if (getListAdapter() != null) {
                            if ((mCurMediaType == MediaType.ARTIST)
                                    && (getListAdapter() instanceof RadioListAdapter)) {
                                ((RadioListAdapter) getListAdapter()).notifyDataSetChanged();
                            } else if((mCurMediaType == MediaType.ARTIST) &&
                                    !(getListAdapter() instanceof RadioListAdapter)) {
                                setupRadioListAdapter();
                            } else if((mCurMediaType != MediaType.ARTIST) &&
                                    (getListAdapter() instanceof RadioListAdapter)) {
                                setupMusicListAdapter();
                            } else {
                                ((MusicListAdapter) getListAdapter()).notifyDataSetChanged();
                            }
                        }
                    } else {
                        for (MediaItem item : mListFavoriteMedia) {
                            if (item.getMediaType() == MediaType.ARTIST)
                                item.setMediaContentType(MediaContentType.RADIO);
                        }

                        mListFavoriteMedia.addAll(profileFavoriteMediaItems.mediaItems);

                        if (mCurMediaType == MediaType.ARTIST) {
                            setupRadioListAdapter();
                        } else {
                            setupMusicListAdapter();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                flList.setVisibility(View.VISIBLE);
                if (getActivity() != null) {
                    ((CarModeHomeActivity) getActivity()).hideLoadDialog();
                }
                if(mListFavoriteMedia.size()>0){
                    txtempty.setVisibility(View.GONE);
                }else{
                    txtempty.setVisibility(View.VISIBLE);
                }
                break;

            case OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE:
                BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects
                        .get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);

                // has the item been removed from favorites.
                if (removeFromFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS) {
                    GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, removeFromFavoriteResponse.getMessage(), null);

                    mListFavoriteMedia.remove(mSelectedMedia);

                    final MusicListAdapter adapter = ((MusicListAdapter) getListAdapter());
                    adapter.vHandleCollapseOptions();
                    adapter.notifyDataSetChanged();
                }

                mDialog.hide(); // Dismiss Favorite dialog.
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
                        isSaveOffline = false;
                        mDialog.hide();
                        CacheManager.saveAllTracksOfflineAction(getActivity(), tracks);
                    } else if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {

                        // Play list of songs.
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

                        ListMusicFragment.playNowNew(getActivity(), tracks, null, null);
                        mFavoriteListAdapter.vHandleCollapseOptions();

                        pbLoadMedia.setVisibility(View.GONE);
                        btnPlayNow.setClickable(true);

                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                        btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);
                    } else if (playerOptions == PlayerOption.OPTION_PLAY_NOW_AND_OPEN) { // Check if current PlayList/Album is playing
                        boolean isPlaying = false;

                        /*if (!mPlayerService.isQueueEmpty()
                                && (mPlayerService.isPlaying() || mPlayerService.isLoading())
                                && (mPlayerService.getCurrentPlayingTrack() != null)) {
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

            default:
                break;
        }
    }

    private void setupMusicListAdapter() {
        if (mFavoriteListAdapter == null) {
            mFavoriteListAdapter = new MusicListAdapter(getActivity(), mListFavoriteMedia);
            mFavoriteListAdapter.setMediaItemEvents(new MediaItemEvents() {

                @Override
                public void onExpandMenu(int mediaItemPos) {
                    mSelectedMedia = mListFavoriteMedia.get(mediaItemPos);
                    isScrolling = false; // Flag for disable scrolling.
                    isExpanded = true;

                    expandMusicOptions();
                    mFavoriteListAdapter.enableItemEvent(false);

//                    Button btnViewDetail = (Button) mRootView.findViewById(R.id.btn_view_detail);
//                    if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
//                        btnViewDetail.setVisibility(View.GONE);
//                        rlPlayNow.setVisibility(View.VISIBLE);
//                    } else {
//                        btnViewDetail.setVisibility(View.VISIBLE);
//                        rlPlayNow.setVisibility(View.GONE);
//                    }

                }

                @Override
                public void onCollapseMenu() {
                    mFavoriteListAdapter.enableItemEvent(true);
                    collapseMusicOptions();
                    isScrolling = true;
                    isExpanded = false;

//                    if (mPlayerService.getState() == PlayerService.State.PAUSED
//                            || mPlayerService.getState() == PlayerService.State.PLAYING
//                            || mPlayerService.getState() == PlayerService.State.INTIALIZED) {
//                        mPlayerService.stop();
//
//                        pbLoadMedia.setVisibility(View.INVISIBLE);
//                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null,
//                                null);
//                        btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
//
//                        for (Track track : mPlayerService.getPlayingQueue()) {
//                            if (track.getId() == mSelectedMedia.getId()) {
//                                final int pos = mPlayerService.getPlayingQueue().indexOf(track);
//                                mPlayerService.removeFrom(pos);
//                                break;
//                            }
//                        }
//                    }
                }

            });
            setListAdapter(mFavoriteListAdapter);
        } else {
            ((MusicListAdapter) getListAdapter()).notifyDataSetChanged();
        }
    }

    private void setupRadioListAdapter() {
        mRadioListAdapter = new RadioListAdapter(getActivity(), mListFavoriteMedia);
        mRadioListAdapter.setListener(new RadioListAdapter.IRadioListAdapterListener() {

            @Override
            public void onGoToRadioPlayer(MediaItem radioItem) {
                mListener.onGoToRadioPlayer(radioItem, PlayMode.TOP_ARTISTS_RADIO);
            }
        });

        setListAdapter(mRadioListAdapter);
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
        flList.setVisibility(View.VISIBLE);
        if (mDialog != null) {
            mDialog.hide();
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
                    getFragmentManager().popBackStack();
                }
            });
        }
    }

    public void handleCacheState() {
        if (btnDownload != null && isExpanded) {
            DataBase.CacheState cacheState = DataBase.CacheState.NOT_CACHED;

            switch (mSelectedMedia.getMediaType()) {
                case ALBUM:
                    cacheState = DBOHandler.getAlbumCacheState(getActivity(), "" + mSelectedMedia.getId());
                    if (cacheState == DataBase.CacheState.NOT_CACHED && (DBOHandler.getAlbumCachedCount(getActivity(), "" + mSelectedMedia.getId()) > 0)) {
                        cacheState = DataBase.CacheState.CACHED;
                    }
                    break;

                case TRACK:
                    cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + mSelectedMedia.getId());
                    break;

                case PLAYLIST:
                    cacheState = DBOHandler.getPlaylistCacheState(getActivity(), "" + mSelectedMedia.getId());
                    if (cacheState == DataBase.CacheState.NOT_CACHED && (DBOHandler.getPlaylistCachedCount(getActivity(), "" + mSelectedMedia.getId()) > 0)) {
                        cacheState = DataBase.CacheState.CACHED;
                    }
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

    public void vHandleFavoriteClicks(View v) {
        int view_id = v.getId();
        Drawable drawable = null;

        switch (view_id) {
            case R.id.btn_scroll_up:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() - 1);
                getListView().setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_scroll_down:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() + 1);
                getListView().setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_back_main_menu:
                getFragmentManager().popBackStack();
                break;

            case R.id.btn_universal_player:
                resetMusicLayout();
                mListener.onGoToMusicPlayer();
                break;

            case R.id.btn_view_detail:
                resetMusicLayout();
                mListener.goToMusicDetail(mSelectedMedia);
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
                        Track track = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(),
                                mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(),
                                mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());
                        CacheManager.saveOfflineAction(getActivity(), mSelectedMedia, track);

                        GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, msg, null);

                    } else if (mSelectedMedia.getMediaType() == MediaType.ALBUM || mSelectedMedia.getMediaType() == MediaType.PLAYLIST) {
                        if (MediaCachingTaskNew.isEnabled) {
                            isSaveOffline = true;
                            mDataManager.getMediaDetails(mSelectedMedia, PlayerOption.OPTION_SAVE_OFFLINE, this);
                        } else {
                            CacheManager.saveOfflineAction(getActivity(), mSelectedMedia, null);
                            GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, msg, null);
                        }
                    }
                }
                break;

            case R.id.btn_media_type:
                resetMusicLayout();
                mListener.onGoToMediaTypeSelection(mCurMediaType.ordinal(), this);
                txtempty.setVisibility(View.GONE);
                break;

            case R.id.btn_favorite:
                String msg = mSelectedMedia.getMediaType().toString();
                if (mSelectedMedia.getMediaType() == MediaType.ARTIST) {
                    msg = "ondemandradio";
                }
                mDataManager.removeFromFavorites(String.valueOf(mSelectedMedia.getId()), msg, this);
                break;

            case R.id.btn_play_now:
                if (mPlayerService != null) {
                    /*boolean allowToPlay;
                    if (mPlayerService.getCurrentPlayingTrack() == null
                            || mPlayerService.getState() == PlayerService.State.IDLE
                            || mPlayerService.getState() == PlayerService.State.STOPPED) { // Play Now if Player service is stopped or not started yet.
                        allowToPlay = true;
                    } else {
                        if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
                            allowToPlay = mPlayerService.getCurrentPlayingTrack().getId() != mSelectedMedia.getId(); // Play Now if not same as track
                        } else {
                            allowToPlay = mPlayerService.getCurrentPlayingTrack().getAlbumId() != mSelectedMedia.getId(); // Play Now if not same as album/playlist
                        }
                    }

                    if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
                        mPlayerService.stop();

                        // Update button.
                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null, null);
                        btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
                    }

                    if (mSelectedMedia.getMediaType() == MediaType.TRACK && allowToPlay) {
                        final Track selectedTrack = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(), mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(), mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());
                        final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();

                        // Check whether track in Queue or Not.
                        if (tracksInQueue.contains(selectedTrack)) {
                            final int trackPos = tracksInQueue.indexOf(selectedTrack);
                            mPlayerService.playFromPosition(trackPos);
                        } else {
                            mPlayerService.playNow(Collections.singletonList(selectedTrack));
                        }

                        mFavoriteListAdapter.vHandleCollapseOptions();

                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                        btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);
                    } else if (allowToPlay) { // MediaType == Album/Playlist
                        mDataManager.getMediaDetails(mSelectedMedia, PlayerOption.OPTION_PLAY_NOW, this);

                        pbLoadMedia.setVisibility(View.VISIBLE);
                        btnPlayNow.setClickable(false);
                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                    }
                    mListener.onPlayNow();*/
                    boolean allowToPlay = true;
                    if(allowToPlay){

                        if (mSelectedMedia.getMediaType() == MediaType.TRACK && allowToPlay) {
                            final Track selectedTrack = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(), mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(), mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());

                            /*if(mPlayerService.getCurrentPlayingTrack()!=null && mPlayerService.getCurrentPlayingTrack().getId() == selectedTrack.getId() && mPlayerService.getState()== PlayerService.State.PLAYING){
                                mPlayerService.pause();
                                btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null, null);
                                btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
                                return;
                            }*/

                            ListMusicFragment.playNowNew(getActivity(), Collections.singletonList(selectedTrack), null, null);

                            mFavoriteListAdapter.vHandleCollapseOptions();

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

            case R.id.btn_add_to_queue:
                if (mSelectedMedia.getMediaContentType() == MediaContentType.MUSIC) {
                    if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
                        Track track = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(),
                                mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(),
                                mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());

                        List<Track> listTracks = new ArrayList<Track>();
                        listTracks.add(track);
                        mListener.onAddTrackToQueue(listTracks);
                    } else {
                        mDataManager.getMediaDetails(mSelectedMedia, PlayerOption.OPTION_ADD_TO_QUEUE, this);
                    }
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CODE_SELECTED_MEDIA_TYPE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                int value = data.getIntExtra(FavoriteTypeSelectionFragment.MEDIA_TYPE, 0);
                mCurMediaType = MediaType.values()[value];
                loadTitle();

                // Reset listview
                mFavoriteListAdapter = null;
            }
        }
    }

    private void loadTitle() {
        switch (mCurMediaType) {
            case ALBUM:
                strTitle = getResources().getString(R.string.title_favorite_albums);
                break;
            case PLAYLIST:
                strTitle = getResources().getString(R.string.title_favorite_playlists);
                break;
            case TRACK:
                strTitle = getResources().getString(R.string.title_favorite_songs);
                break;
            case ARTIST:
                strTitle = getResources().getString(R.string.title_favorite_radio);
                break;
            default:
                break;
        }

        btnMediaType.setText(strTitle);
    }

    // Reset layout before calling next fragment.
    public void resetMusicLayout() {
        isScrolling = true;
        llMusicOptions.setVisibility(View.GONE);
        llListViewProgressControl.setVisibility(View.VISIBLE);
//        llListViewProgressControl.setAlpha(1);
//        llListViewProgressControl.getLayoutParams().width = tmpSavedScrollerWidth;
//        llMusic.getLayoutParams().width = mScreenWidthInPxl;
//        mRootView.requestLayout();

        if (mFavoriteListAdapter != null) {
            mFavoriteListAdapter.setSelectedPos(-1);
            mFavoriteListAdapter.setMediaMenuExpanded(false);
            mFavoriteListAdapter.notifyDataSetChanged();
        }
    }

    private void expandMusicOptions() {
        handleCacheState();

        if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
            /*if (!mPlayerService.isQueueEmpty()
                    && (mPlayerService.isPlaying() || mPlayerService.isLoading())
                    && (mPlayerService.getCurrentPlayingTrack() != null)
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

        llMusicOptions.setVisibility(View.VISIBLE);
        llListViewProgressControl.setVisibility(View.GONE);

//        final int targetMusicOptionsWidth = mScreenWidthInPxl / 3;
//        final int initMusicWidth = this.llMusic.getWidth();
//        final int initMusicControlWidth = this.llListViewProgressControl.getWidth();
//
//        llMusicOptions.setVisibility(View.VISIBLE);
//
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
    public void onStop() {
        resetMusicLayout();
        super.onStop();
    }


    public interface IFavoriteListener {
        void onGoToMediaTypeSelection(int curMediaType, Fragment targetFragment);

        void onGoToMusicPlayer();

        void onGoToRadioPlayer(MediaItem radioItem, PlayMode radioMode);

        void onAddTrackToQueue(List<Track> listTracks);

        void goToMusicDetail(MediaItem selectedMedia);

        void onPlayNow();
    }
}
