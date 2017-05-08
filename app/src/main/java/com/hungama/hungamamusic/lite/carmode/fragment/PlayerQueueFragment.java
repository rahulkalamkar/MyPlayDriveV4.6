package com.hungama.hungamamusic.lite.carmode.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.PlayingTrackListAdapter;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.hungamamusic.lite.carmode.view.VerticalSeekBar;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayingQueue;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerQueueFragment extends Fragment implements CommunicationOperationListener {

    public static final String TAG = PlayerQueueFragment.class.getSimpleName();
    private static final int CODE_FAVORITE_SUCCESS = 1;
    private static final String MEDIA_TYPE_SONG = "song";

    public PlayerService mPlayerService = null;
    public DataManager mDataManager;
    private boolean isInForeground;
    private List<Track> mListPlayingTracks;
    private List<Long> mListFavorites;
    private List<Track> mListDefaultTracks;
    private List<Track> mListOfflineTracks;
    private PlayingTrackListAdapter mPlayingTrackAdapter;
    private CustomDialogLayout mCustomDialog;
    private IPlayerQueue mListener;
    private boolean isFromOffline = false;

    // UI Elements
    private View mRootView;
    private TextView tvTrackName;
    private TextView tvAlbumName;
    private ProgressBar pbFavorite;
    private ImageButton btnPlayerStartPause;
    private ImageView ivTrackAvatar;
    private ProgressBar pbPlayer;
    private ImageButton btnFav;
    PlayerService.PlayerStateListener IPlayerStateListener = new PlayerService.PlayerStateListener() {

        @Override
        public void onTrackLoadingBufferUpdated(Track track, int precent) {
        }

        @Override
        public void onStartPlayingTrack(Track track) {
            if (mPlayerService.getPlayMode() != PlayMode.LIVE_STATION_RADIO) {

                String imageURL = "";

                String[] images = ImagesManager.getImagesUrlArray(track.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE, mDataManager.getDisplayDensity());
                if (images != null && images.length > 0) {
                    imageURL = images[0];
                }
                if(TextUtils.isEmpty(imageURL))
                    imageURL = ImagesManager.getMusicArtSmallImageUrl(track.getImagesUrlArray());

                Picasso.with(getActivity()).cancelRequest(ivTrackAvatar);
                if (imageURL != null && !TextUtils.isEmpty(imageURL)) {
                    Picasso.with(getActivity()).load(imageURL).placeholder(R.drawable.background_home_tile_album_default).into(ivTrackAvatar);
                }

                if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                    mPlayerService.startProgressUpdater();
                }

                pbPlayer.setVisibility(View.GONE);
                btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_pause);
                tvTrackName.setText(track.getTitle());
                tvAlbumName.setText(track.getAlbumName());
            }
        }

        @Override
        public void onStartPlayingAd(Placement audioad) {

        }

        @Override
        public void onStartLoadingTrack(Track track) {
            if (mPlayerService.getPlayMode() != PlayMode.LIVE_STATION_RADIO) {

                ivTrackAvatar.setImageResource(R.drawable.background_home_tile_album_default);

                btnFav.setVisibility(View.VISIBLE);
                btnFav.setClickable(true);
                pbFavorite.setVisibility(View.GONE);

                if (mListFavorites != null) {
                    for (Long track_id : mListFavorites) {
                        if (track_id == track.getId()) {

                            // Update button state to "Added".
                            btnFav.setImageResource(R.drawable.icon_main_player_favorites_blue);
                            break;
                        }
                        btnFav.setImageResource(R.drawable.icon_main_player_favorites_white);
                    }
                }


                tvTrackName.setText(track.getTitle());
                tvAlbumName.setText(track.getAlbumName());
                updateDownloadState(track);
                pbPlayer.setVisibility(View.VISIBLE);
                btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
            }
        }

        @Override
        public void onSleepModePauseTrack(Track track) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onFinishPlayingTrack(Track track) {
            if (mPlayerService != null) {
                mPlayerService.stopProgressUpdater();

                btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
            }
        }

        @Override
        public void onFinishPlayingQueue() {
            if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                mPlayerService.stopProgressUpdater();
            }
        }

        @Override
        public void onErrorHappened(PlayerService.Error error) {
            Log.e(TAG, "onErrorHappened= " + error.getId());
        }

        @Override
        public void onAdCompletion() {
            // TODO Auto-generated method stub

        }
    };
    private ImageButton btnDownload;
    private ListView lvPlayingTracks;
    private VerticalSeekBar sbListViewProgress;

    public static PlayerQueueFragment newInstance(IPlayerQueue listener) {
        PlayerQueueFragment fragment = new PlayerQueueFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(IPlayerQueue listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (View) inflater.inflate(R.layout.carmode_fragment_player_queue, container, false);

            tvTrackName = (TextView) mRootView.findViewById(R.id.tv_track_name);
            tvAlbumName = (TextView) mRootView.findViewById(R.id.tv_album_name);
            pbFavorite = (ProgressBar) mRootView.findViewById(R.id.pb_favorite);
            btnFav = (ImageButton) mRootView.findViewById(R.id.btn_add_favorites);
            btnDownload = (ImageButton) mRootView.findViewById(R.id.btn_download);
            btnPlayerStartPause = (ImageButton) mRootView.findViewById(R.id.btn_player_start_pause);
            ivTrackAvatar = (ImageView) mRootView.findViewById(R.id.iv_track_avatar);
            lvPlayingTracks = (ListView) mRootView.findViewById(R.id.lv_list_playing_tracks);
            sbListViewProgress = (VerticalSeekBar) mRootView.findViewById(R.id.sb_listview_progress);
            pbPlayer = (ProgressBar) mRootView.findViewById(R.id.pb_player);
        }
        return mRootView;
    }

    private PlayerService.IUpdateCarmodePlayerUI mCarModeUIUpdate = new PlayerService.IUpdateCarmodePlayerUI() {
        @Override
        public void onUpdateUI(int keyCode) {
            final DataManager dataManager = DataManager.getInstance(getActivity());
            final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();

            if (isInForeground()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_pause);
                        break;

                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
                        break;

                    case KeyEvent.KEYCODE_MEDIA_NEXT:

                        if (PlayerService.service.getState() == PlayerService.State.INTIALIZED) {
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_please_wait), null);
                        } else {
                            if (appConfig.getSaveOfflineMode()) { // Offline
                                final int curPlayingPos = mPlayerService.getCurrentPlayingTrackPosition();
                                int foundPos = -1;
                                for (int pos = curPlayingPos + 1; pos < mListPlayingTracks.size(); pos++) {
                                    final Track track = mListPlayingTracks.get(pos);
                                    final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + track.getId());
                                    if (cacheState == DataBase.CacheState.CACHED) {
                                        foundPos = pos;
                                        break;
                                    }
                                }

                                if (foundPos == -1) { // Not found
                                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.caching_text_message_go_online_global_menu), null);
                                } else {
                                    mPlayerService.playFromPosition(foundPos);
                                    mPlayingTrackAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        if (PlayerService.service.getState() == PlayerService.State.INTIALIZED) {
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_please_wait), null);
                        } else {
                            if (appConfig.getSaveOfflineMode() || !Utils.isConnected()) { // Offline
                                final int curPlayingPos = mPlayerService.getCurrentPlayingTrackPosition();
                                int foundPos = -1;
                                for (int pos = curPlayingPos - 1; pos >= 0; pos--) {
                                    final Track track = mListPlayingTracks.get(pos);
                                    final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + track.getId());
                                    if (cacheState == DataBase.CacheState.CACHED) {
                                        foundPos = pos;
                                        break;
                                    }
                                }

                                if (foundPos == -1) { // Not found
                                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.caching_text_message_go_online_global_menu), null);
                                } else if (mPlayingTrackAdapter != null) {
                                    mPlayerService.playFromPosition(foundPos);
                                    mPlayingTrackAdapter.notifyDataSetChanged();
                                }
                            }
                        }

                        break;

                    default:
                        Log.v(TAG, "Unknown keycode");
                        break;
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve player service.
        mPlayerService = PlayerService.service;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        mPlayerService.registerPlayerStateListener(IPlayerStateListener);
//        mPlayerService.setCarModeListener(mCarModeUIUpdate);

        this.mDataManager = DataManager.getInstance(getActivity());

        this.sbListViewProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                    lvPlayingTracks.setSelection(progress);
                }
            }

        });


        // Preparing list of tracks.
        setupPlayingTrackList();

        if (!mDataManager.getApplicationConfigurations().getSaveOfflineMode() && Utils.isConnected()) {
            mDataManager.getFavorites(getActivity(), MediaType.TRACK, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);
        } else {
            btnFav.setVisibility(View.VISIBLE);
            btnFav.setClickable(true);
            pbFavorite.setVisibility(View.GONE);
        }
    }

    private void setupPlayingTrackList() {
        if(mPlayerService!=null)
            mListPlayingTracks = mPlayerService.getPlayingQueue();
        else
            mListPlayingTracks = new ArrayList<>();
        mPlayingTrackAdapter = new PlayingTrackListAdapter(getActivity(), mListPlayingTracks);
        mPlayingTrackAdapter.setListener(new PlayingTrackListAdapter.IPlayingTrackAdapterListener() {

            @Override
            public void onTrackDelete(final int position) {
                mCustomDialog = GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.CONFIRMATION, getString(R.string.msg_track_delete_confirmation), new CustomDialogLayout.IDialogListener() {

                    @Override
                    public void onPositiveBtnClick() {
                        final int currentPosition = mPlayerService.getCurrentPlayingTrackPosition();
                        mPlayerService.removeFrom(position);

                        if (currentPosition == position) { // Current playing
                            btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
                            mPlayerService.stop();

                            if (mPlayerService.getPlayingQueue().size() == 0) {
                                //							clearQueue();
                            } else {
                                if (mDataManager.getApplicationConfigurations().getSaveOfflineMode()) {
                                    findOfflineSong();
                                } else {
                                    mPlayerService.playFromPositionNew(mPlayerService.getCurrentPlayingTrackPosition());
                                }
                            }
                        }

                        // Refresh List.
                        mListPlayingTracks.remove(position);
                        mPlayingTrackAdapter.notifyDataSetChanged();

                        mCustomDialog.hide();

                        if (mListPlayingTracks.size() == 0) {
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_player_queue_empty), new CustomDialogLayout.IDialogListener() {
                                @Override
                                public void onPositiveBtnClick() {
                                }

                                @Override
                                public void onNegativeBtnClick() {
                                }

                                @Override
                                public void onNegativeBtnClick(CustomDialogLayout layout) {
                                }

                                @Override
                                public void onMessageAction() {
                                    mPlayerService.stop();
                                    mPlayerService.clearAd();
                                    mPlayerService.setPlayingQueue(new PlayingQueue(null, 0, mPlayerService));

                                    mListener.closeMusicPlayer();
                                }
                            });
                        }
                    }

                    @Override
                    public void onNegativeBtnClick() {
                    }

                    @Override
                    public void onNegativeBtnClick(CustomDialogLayout dialog) {
                        dialog.hide();
                    }

                    @Override
                    public void onMessageAction() {
                    }
                });

            }
        });

        if (isVisible()) {
            lvPlayingTracks.setAdapter(mPlayingTrackAdapter);
        }

        lvPlayingTracks.setOnScrollListener(new AbsListView.OnScrollListener() {

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

        lvPlayingTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final Track currPlaying = mPlayerService.getCurrentPlayingTrack();
                if (currPlaying != null) {
                    DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + currPlaying.getId());
                    switch (cacheState) {
                        case CACHED:
                            btnDownload.setImageResource(R.drawable.carmode_cache_state_cached_large);
                            break;
                        case CACHING:
                            btnDownload.setImageResource(R.drawable.carmode_saving_started_large);
                            break;
                        case NOT_CACHED:
                            btnDownload.setImageResource(R.drawable.carmode_btn_download_large);
                            break;

                        case QUEUED:
                            btnDownload.setImageResource(R.drawable.carmode_cache_state_queued_large);
                            break;
                        default:
                            break;
                    }
                    final DataManager dataManager = DataManager.getInstance(getActivity());
                    final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();

                    if (!appConfig.getSaveOfflineMode() && Utils.isConnected()) {
                        mPlayingTrackAdapter.setSelectedTrack(position);
                        mPlayingTrackAdapter.notifyDataSetChanged();

                        mPlayerService.playNowFromPosition(mListPlayingTracks, position);
                    } else if (cacheState == DataBase.CacheState.CACHED) {
                        mPlayingTrackAdapter.setSelectedTrack(position);
                        mPlayingTrackAdapter.notifyDataSetChanged();

                        mPlayerService.playNowFromPosition(mListPlayingTracks, position);
                    }
                }
            }
        });

        sbListViewProgress.setMax(mListPlayingTracks.size());
    }

    private void findOfflineSong() {
        List<Track> trackList = mPlayerService.getPlayingQueue();
        int startPosition = mPlayerService.getCurrentQueuePosition();
        int position = -1;
        for (int i = startPosition + 1; i < trackList.size(); i++) {
            Track trackTemp = trackList.get(i);
            if (trackTemp != null) {
                DataBase.CacheState trackCacheState = DBOHandler.getTrackCacheState(getActivity(), "" + trackTemp.getId());
                if (trackCacheState == DataBase.CacheState.CACHED) {
                    position = i;
                    break;
                }
            }
        }

        if (position == -1) {
            trackList = mPlayerService.getPlayingQueue();
            startPosition = mPlayerService.getCurrentQueuePosition();
            position = -1;
            for (int i = startPosition - 1; i >= 0; i--) {
                Track trackTemp = trackList.get(i);
                if (trackTemp != null) {
                    DataBase.CacheState trackCacheState = DBOHandler.getTrackCacheState(getActivity(), "" + trackTemp.getId());
                    if (trackCacheState == DataBase.CacheState.CACHED) {
                        position = i;
                        break;
                    }
                }
            }

            if (position == -1) {
                GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.no_offline_songs_1), null);
            } else {
                mPlayerService.playFromPositionNew(position);
            }
        } else {
            mPlayerService.playFromPositionNew(position);
        }
    }

    public void refreshMusicPlayer() {
        if (!mDataManager.getApplicationConfigurations().getSaveOfflineMode() && Utils.isConnected()) {
            // Get List of Favorites:
            mDataManager.getFavorites(getActivity(), MediaType.TRACK, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);
        } else {
            btnFav.setVisibility(View.VISIBLE);
            btnFav.setClickable(true);
            pbFavorite.setVisibility(View.GONE);
        }

        // update track list.
        if (mListPlayingTracks == null) {
            // Preparing list of tracks.
            setupPlayingTrackList();
        } else {
            mListPlayingTracks.clear();
//            mListPlayingTracks.addAll(mDataManager.getStoredPlayingQueue(mDataManager.getApplicationConfigurations()).getCopy());
            mListPlayingTracks.addAll(mPlayerService.getPlayingQueue());
            mPlayingTrackAdapter.notifyDataSetChanged();
        }

        // Update player UI.
        updatePlayerState();

        Track currPlaying = mPlayerService.getCurrentPlayingTrack();
        if (currPlaying != null) {
            updateDownloadState(currPlaying);

            String imageURL = "";

            String[] images = ImagesManager.getImagesUrlArray(currPlaying.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE, mDataManager.getDisplayDensity());
            if (images != null && images.length > 0) {
                imageURL = images[0];
            }
            if(TextUtils.isEmpty(imageURL))
                imageURL = ImagesManager.getMusicArtSmallImageUrl(currPlaying.getImagesUrlArray());

            Picasso.with(getActivity()).cancelRequest(ivTrackAvatar);
            if (imageURL != null && !TextUtils.isEmpty(imageURL)) {
                Picasso.with(getActivity()).load(imageURL).placeholder(R.drawable.background_home_tile_album_default).into(ivTrackAvatar);
            }

            tvTrackName.setText(currPlaying.getTitle());
            tvAlbumName.setText(currPlaying.getAlbumName());
        }
    }

    public void updateOfflineState(){
        if(mPlayerService!=null) {
            Track currPlaying = mPlayerService.getCurrentPlayingTrack();
            if (currPlaying != null) {
                updateDownloadState(currPlaying);
            }
        }
    }

    public void updatePlayerState() {
        if (mPlayerService != null) {
            PlayerService.State playerState = mPlayerService.getState();
            if (playerState == PlayerService.State.PLAYING) {
                btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_pause);
                pbPlayer.setVisibility(View.GONE);

            } else if (playerState == PlayerService.State.PAUSED) {
                btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
            }
        }
    }

    public void vHandlePlayerQueueClick(View v) {
        int view_id = v.getId();
        final DataManager dataManager = DataManager.getInstance(getActivity());
        final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();

        switch (view_id) {
            case R.id.btn_scroll_up:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() - 1);
                lvPlayingTracks.setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_scroll_down:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() + 1);
                lvPlayingTracks.setSelection(sbListViewProgress.getProgress());
                break;
            case R.id.btn_player_start_pause:
                PlayerService.State playerState = mPlayerService.getState();
                if (appConfig.getSaveOfflineMode() || !Utils.isConnected()) { // Offline
                    if (playerState == PlayerService.State.PLAYING) {
                        btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
                        mPlayerService.pause();
                    } else if (playerState == PlayerService.State.PAUSED) {
                        btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_pause);
                        mPlayerService.play();
                    } else if (playerState == PlayerService.State.STOPPED || playerState == PlayerService.State.IDLE) { // In offline mode.
                        int foundPos = -1;
                        for (int pos = 0; pos < mListPlayingTracks.size(); pos++) {
                            final Track track = mListPlayingTracks.get(pos);
                            final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + track.getId());
                            if (cacheState == DataBase.CacheState.CACHED) {
                                foundPos = pos;
                                break;
                            }
                        }
                        if (foundPos == -1) { // Not found.
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.caching_text_message_go_online_global_menu), null);
                        } else {
                            mPlayerService.playFromPosition(foundPos);
                            mPlayingTrackAdapter.notifyDataSetChanged();
                        }
                    }

                } else { // Online
                    if (playerState == PlayerService.State.PLAYING) {
                        btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
                        mPlayerService.pause();
                    } else if (playerState == PlayerService.State.PAUSED || playerState == PlayerService.State.IDLE) {
                        btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_pause);
                        mPlayerService.play();
                    }
                }

                break;

            case R.id.btn_remove_all:
                GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.CONFIRMATION, getString(R.string.player_queue_message_confirm_clear_all), new CustomDialogLayout.IDialogListener() {

                    @Override
                    public void onPositiveBtnClick() {
                        mPlayerService.stop();
                        mPlayerService.clearAd();
                        mPlayerService.setPlayingQueue(new PlayingQueue(null, 0, mPlayerService));

                        mListPlayingTracks.clear();
                        mPlayingTrackAdapter.notifyDataSetChanged();

                        // Prompting after delete all then return to MainMenu.
                        GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_delete_all_success), new CustomDialogLayout.IDialogListener() {

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
                                mListener.closeMusicPlayer();
                            }
                        });
                    }

                    @Override
                    public void onNegativeBtnClick() {
                    }

                    @Override
                    public void onMessageAction() {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public void onNegativeBtnClick(CustomDialogLayout layout) {
                        layout.hide();
                    }
                });
                break;

            case R.id.btn_player_back:
                mListener.backToMusicPlayer();
                break;

            case R.id.btn_add_favorites:

                if (!com.hungama.myplay.activity.util.Utils.isConnected()) { // Network is inactive.
                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.go_online_network_error), null);
                } else if (appConfig.getSaveOfflineMode()) {
                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_offline_add_favorite), null);
                } else if (mPlayerService != null && mPlayerService.getCurrentPlayingTrack() != null) {
                    final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();
                    if (mListFavorites == null) {
                        mListFavorites = new ArrayList<Long>();
                    }

                    if (mListFavorites.contains(curPlayingTrack.getId())) {
                        mDataManager.removeFromFavorites(String.valueOf(curPlayingTrack.getId()), MEDIA_TYPE_SONG, this);
                    } else {
                        mDataManager.addToFavorites(String.valueOf(curPlayingTrack.getId()), MEDIA_TYPE_SONG, this);
                    }
                }

                break;

            case R.id.btn_download:
                if (mPlayerService.getCurrentPlayingTrack() != null) {

                    final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();
                    final DataBase.CacheState trackCacheState = DBOHandler.getTrackCacheState(getActivity(), "" + curPlayingTrack.getId());

                    if (!com.hungama.myplay.activity.util.Utils.isConnected()) { // Network is inactive.
                        GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.go_online_network_error), null);
                    } else if (appConfig.getSaveOfflineMode()) {
                        GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.toast_offline), null);
                    } else if (trackCacheState == DataBase.CacheState.CACHED) {
                        GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_already_download), null);
                    } else if (trackCacheState == DataBase.CacheState.CACHING) {
                        GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_already_downloading), null);
                    } else if (trackCacheState == DataBase.CacheState.QUEUED) {
                        GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_already_download_queue), null);
                    } else {
                        final String msg = getResources().getString(R.string.msg_save_offline, curPlayingTrack.getTitle());
                        GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, msg, null);

                        final MediaItem trackMediaItem = new MediaItem(curPlayingTrack.getId(),
                                curPlayingTrack.getTitle(), curPlayingTrack.getAlbumName(),
                                curPlayingTrack.getArtistName(), getImageUrl(curPlayingTrack),
                                curPlayingTrack.getBigImageUrl(), MediaType.TRACK.toString(), 0, 0,
                                curPlayingTrack.getImages(), curPlayingTrack.getAlbumId());

                        CacheManager.saveOfflineAction(getActivity(), trackMediaItem, curPlayingTrack);
                    }
                }

                break;
        }
    }

    private String getImageUrl(Track track) {
        if (mPlayerService.getCurrentPlayingTrack() != null) {
            MediaTrackDetails mCurrentTrackDetails = mPlayerService.getCurrentPlayingTrack().details;
            if (mCurrentTrackDetails != null
                    && track.getId() == mCurrentTrackDetails.getId()
                    && track.getImagesUrlArray() == null) {
                return ImagesManager.getMusicArtSmallImageUrl(mCurrentTrackDetails
                        .getImagesUrlArray());
            }
        }
        return ImagesManager
                .getMusicArtSmallImageUrl(track.getImagesUrlArray());
    }

    public boolean isInForeground() {
        return this.isInForeground;
    }

    public void setInForeground(boolean bool) {
        this.isInForeground = bool;

        if (bool) {
            mPlayerService.registerPlayerStateListener(IPlayerStateListener);
            mPlayerService.setCarModeListener(mCarModeUIUpdate);
        } else {
            mPlayerService.unregisterPlayerStateListener(IPlayerStateListener);
            mPlayerService.removeCarModeListener(mCarModeUIUpdate);
        }
    }

//    public void setFromOffline(boolean bool) {
//        this.isFromOffline = bool;
//    }
//
//    public void prepareOfflineList() {
//        if (!isFromOffline) {
//            if (mListPlayingTracks != null && mListPlayingTracks.size() > 0) {
//                if (mListOfflineTracks == null) {
//                    mListOfflineTracks = new ArrayList<>();
//                } else {
//                    mListOfflineTracks.clear();
//                }
//
//                if (mListDefaultTracks == null) {
//                    mListDefaultTracks = new ArrayList<>();
//                } else {
//                    mListDefaultTracks.clear();
//                }
//
//                for (Track track : mListPlayingTracks) {
//                    final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + track.getId());
//                    if (cacheState == DataBase.CacheState.CACHED) {
//                        mListOfflineTracks.add(track);
//                    }
//                    mListDefaultTracks.add((track));
//                }
//            }
//
//            // Change queue to Offline list.
//            if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
//                mPlayerService.stop();
//            }
//
//            mPlayerService.setPlayingQueue(new PlayingQueue(mListOfflineTracks, 0, mPlayerService));
//            refreshMusicPlayer();
//            isFromOffline = true;
//        }
//    }
//
//    public void restoreOnlineList() {
//        // Change queue to Online list.
//        if (isFromOffline) {
//            // Change queue to Offline list.
//            if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
//                mPlayerService.stop();
//            }
//
//            mPlayerService.setPlayingQueue(new PlayingQueue(mListDefaultTracks, 0, mPlayerService));
//
//            refreshMusicPlayer();
//            isFromOffline = false;
//        }
//    }

    @Override
    public void onStart(int operationId) {
        String msg = "";

        switch (operationId) {
            case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
                if (this.isInForeground) {
                    btnFav.setVisibility(View.INVISIBLE);
                    btnFav.setClickable(false);
                    pbFavorite.setVisibility(View.VISIBLE);
                }
                break;

            case OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE:
                btnFav.setVisibility(View.INVISIBLE);
                btnFav.setClickable(false);
                pbFavorite.setVisibility(View.VISIBLE);
                break;

            case OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE:
                btnFav.setVisibility(View.INVISIBLE);
                btnFav.setClickable(false);
                pbFavorite.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }
    }

    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        switch (operationId) {
            case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
                ProfileFavoriteMediaItems profileFavoriteMediaItems = (ProfileFavoriteMediaItems) responseObjects.get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS);
                mListFavorites = new ArrayList<Long>();
                if (profileFavoriteMediaItems != null && !mPlayerService.isQueueEmpty()) {
                    List<MediaItem> listItems = profileFavoriteMediaItems.mediaItems;
                    boolean isFavorite = false;

                    if ((listItems != null)
                            && (listItems.size() > 0)
                            && mPlayerService.getCurrentPlayingTrack() != null) {

                        final long trackId = mPlayerService.getCurrentPlayingTrack().getId();
                        for (int i = 0; i < listItems.size(); i++) {
                            mListFavorites.add(listItems.get(i).getId());

                            if (trackId == listItems.get(i).getId()) {
                                btnFav.setImageResource(R.drawable.icon_main_player_favorites_blue);
                                isFavorite = true;
                            }
                        }
                    }

                    if (!isFavorite) {
                        btnFav.setImageResource(R.drawable.icon_main_player_favorites_white);
                    }

                }else{
                    btnFav.setImageResource(R.drawable.icon_main_player_favorites_white);
                }

                btnFav.setVisibility(View.VISIBLE);
                btnFav.setClickable(true);
                pbFavorite.setVisibility(View.GONE);

                break;

            case OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE:
                BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);

                // has the item been added from favorites.
                if (addToFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS
                        && mPlayerService != null
                        && mPlayerService.getCurrentPlayingTrack() != null) {

                    final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();

                    // Update button state to "Added".
                    btnFav.setImageResource(R.drawable.icon_main_player_favorites_blue);
                    mListFavorites.add(curPlayingTrack.getId());

                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, addToFavoriteResponse.getMessage(), null);
                }

                btnFav.setVisibility(View.VISIBLE);
                btnFav.setClickable(true);
                pbFavorite.setVisibility(View.GONE);
                break;

            case OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE:
                BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);

                // has the item been removed from favorites.
                if (removeFromFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS
                        && mPlayerService != null
                        && mPlayerService.getCurrentPlayingTrack() != null) {

                    // Update button state to "Removed".
                    btnFav.setImageResource(R.drawable.icon_main_player_favorites_white);
                    final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();
                    mListFavorites.remove(curPlayingTrack.getId());
                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, removeFromFavoriteResponse.getMessage(), null);
                }

                btnFav.setVisibility(View.VISIBLE);
                btnFav.setClickable(true);
                pbFavorite.setVisibility(View.GONE);

                break;

            default:
                break;
        }
    }

    @Override
    public void onFailure(int operationId, CommunicationManager.ErrorType errorType, String errorMessage) {
        if (isInForeground && errorType != CommunicationManager.ErrorType.NO_CONNECTIVITY) {
            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, errorMessage, null);
        }

        btnFav.setVisibility(View.VISIBLE);
        btnFav.setClickable(true);
        pbFavorite.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {

        if (mPlayerService != null) {
            mPlayerService.registerPlayerStateListener(IPlayerStateListener);
            mPlayerService.setCarModeListener(mCarModeUIUpdate);
        }

        super.onStart();
    }

    @Override
    public void onStop() {
        mDataManager.cancelGetListFavorites();

        if (mPlayerService != null) {
            mPlayerService.removeCarModeListener(mCarModeUIUpdate);
            mPlayerService.unregisterPlayerStateListener(IPlayerStateListener);
        }

        super.onStop();
    }

    public interface IPlayerQueue {
        void backToMusicPlayer();

        void closeMusicPlayer();
    }

    public void updateDownloadState(Track currPlaying) {
        DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + currPlaying.getId());
        switch (cacheState) {
            case CACHED:
                btnDownload.setImageResource(R.drawable.carmode_cache_state_cached_large);
                break;
            case NOT_CACHED:
                btnDownload.setImageResource(R.drawable.carmode_btn_download_large);
                break;
            case CACHING:
                btnDownload.setImageResource(R.drawable.carmode_saving_started_large);
                break;
            case QUEUED:
                btnDownload.setImageResource(R.drawable.carmode_icon_media_details_saving_queue_large);
                break;
            default:
                break;
        }
    }



}
