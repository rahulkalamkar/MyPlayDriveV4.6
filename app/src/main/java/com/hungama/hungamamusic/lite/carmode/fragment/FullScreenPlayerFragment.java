package com.hungama.hungamamusic.lite.carmode.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout.DialogType;
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
import com.hungama.myplay.activity.player.PlayerService.Error;
import com.hungama.myplay.activity.player.PlayerService.LoopMode;
import com.hungama.myplay.activity.player.PlayerService.PlayerBarUpdateListener;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FullScreenPlayerFragment extends Fragment implements CommunicationOperationListener {

    public static final String TAG = FullScreenPlayerFragment.class.getSimpleName();
    private static final int CODE_FAVORITE_SUCCESS = 1;
    private static final String MEDIA_TYPE_SONG = "song";

    public PlayerService mPlayerService = null;
    public DataManager mDataManager;
    private List<Track> mListPlayingTracks;
    private List<Track> mListDefaultTracks;
    private List<Track> mListOfflineTracks;
    private List<Long> mListFavorites;
    private TrackAdapter mPlayingTrackAdapter;
    private boolean isStartTracking;
    private boolean isInForeground = false;
    private int mSelectPos = -1;
    private boolean isFromOffline = false;

    // UI Elements
    private ProgressBar pbFavorite;
    private TextView tvTrackName;
    private TextView tvAlbumName;
    private SeekBar sbPlayerProgress;
    private ImageButton btnPlayerStartPause;
    private ImageView ivTrackAvatar;
    private ProgressBar pbPlayer;
    private ImageButton btnPrevious;
    private ImageButton btnNext;
    private ImageButton btnFav;
    private ImageButton btnDownload;
    PlayerStateListener IPlayerStateListener = new PlayerStateListener() {

        @Override
        public void onTrackLoadingBufferUpdated(Track track, int precent) {
            sbPlayerProgress.setSecondaryProgress(precent);
        }

        @Override
        public void onStartPlayingTrack(Track track) {
            vResetPlayer();
            String imageURL = "";
            String[] images = ImagesManager.getImagesUrlArray(track.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE, mDataManager.getDisplayDensity());
            if (images != null && images.length > 0) {
                imageURL = images[0];
            }
            if(TextUtils.isEmpty(imageURL))
                imageURL = ImagesManager.getMusicArtSmallImageUrl(track.getImagesUrlArray());

            //Picasso.with(getActivity()).cancelRequest(ivTrackAvatar);
            if (imageURL != null && !TextUtils.isEmpty(imageURL)) {
                Picasso.with(getActivity()).load(imageURL).placeholder(R.drawable.background_home_tile_album_default).into(ivTrackAvatar);
            }

            sbPlayerProgress.setProgress(0);
            sbPlayerProgress.setSecondaryProgress(0);

            if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                mPlayerService.startProgressUpdater();
            }

            pbPlayer.setVisibility(View.GONE);
            btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_pause);
            tvTrackName.setText(track.getTitle());
            tvAlbumName.setText(track.getAlbumName());
            mPlayingTrackAdapter.notifyDataSetChanged();

            Log.d("Khoa", "onStartPlayingTrack----title= " + track.getTitle());
            Log.d("Khoa", "onStartPlayingTrack----album= " + track.getAlbumName());
        }

        @Override
        public void onStartPlayingAd(Placement audioad) {
            if(PlayerService.service==null)
                return;
            vResetPlayer();
            pbPlayer.setVisibility(View.GONE);
            btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_pause);
            if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                mPlayerService.startProgressUpdater();
            }
            try {
                if (PlayerService.service.getPlayMode() == PlayMode.MUSIC) {
                    try {
                        if (Utils.isCarMode()) {
                            loadAdForMusic(audioad);
                        }
                    } catch (Exception e) {
                        Logger.printStackTrace(e);
                    }
                }
            /*if (PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                final RadioDetailsFragment temp = (RadioDetailsFragment) mFragmentManager
                        .findFragmentByTag(RadioDetailsFragment.TAG);
                commonHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (temp != null) {
                            temp.loadAudioAd(audioad);
                        }
                    }
                }, 1000);
            } else if (getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                final DiscoveryPlayDetailsFragment temp = (DiscoveryPlayDetailsFragment) mFragmentManager
                        .findFragmentByTag(DiscoveryPlayDetailsFragment.TAG);
                commonHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (temp != null) {
                            temp.loadAudioAd(audioad);
                        }
                    }
                }, 1000);
            }*/
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
        }

        @Override
        public void onStartLoadingTrack(Track track) {
            if (mPlayerService != null && mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                mPlayerService.stopProgressUpdater();
                mPlayerService.stopLiveRadioUpdater();
            }

            //Reset seekbar of Player
            sbPlayerProgress.setProgress(0);
            sbPlayerProgress.setSecondaryProgress(0);

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

            pbPlayer.setVisibility(View.VISIBLE);
            btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
            updatePlayerActionButton();
            updateDownloadState(track);

            mPlayingTrackAdapter.notifyDataSetChanged();
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
                btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
            }
        }

        @Override
        public void onErrorHappened(Error error) {
            Log.e(TAG, "onErrorHappened= " + error.getId());
        }

        @Override
        public void onAdCompletion() {
            // TODO Auto-generated method stub
            if (mPlayerService != null && mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                mPlayerService.stopProgressUpdater();
                mPlayerService.stopLiveRadioUpdater();
            }
        }
    };
    private ImageButton btnShuffle;
    private ImageButton btnRepeat;
    private ListView lvPlayingTracks;
    private IMusicPlayer mListener;

    public static FullScreenPlayerFragment newInstance(IMusicPlayer listener) {
        FullScreenPlayerFragment fragment = new FullScreenPlayerFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(IMusicPlayer listener) {
        this.mListener = listener;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = (View) inflater.inflate(R.layout.carmode_fragment_fullscreen_player, container, false);

        tvTrackName = (TextView) mRootView.findViewById(R.id.tv_track_name);
        tvAlbumName = (TextView) mRootView.findViewById(R.id.tv_album_name);
        sbPlayerProgress = (SeekBar) mRootView.findViewById(R.id.sb_player);
        ivTrackAvatar = (ImageView) mRootView.findViewById(R.id.iv_track_avatar);
        pbPlayer = (ProgressBar) mRootView.findViewById(R.id.pb_player);
        btnPlayerStartPause = (ImageButton) mRootView.findViewById(R.id.btn_player_start_pause);
        btnPrevious = (ImageButton) mRootView.findViewById(R.id.btn_player_previous);
        btnNext = (ImageButton) mRootView.findViewById(R.id.btn_player_next);
        btnFav = (ImageButton) mRootView.findViewById(R.id.btn_add_favorites);
        btnDownload = (ImageButton) mRootView.findViewById(R.id.btn_download);
        btnShuffle = (ImageButton) mRootView.findViewById(R.id.btn_shuffle_play);
        btnRepeat = (ImageButton) mRootView.findViewById(R.id.btn_repeat);
        lvPlayingTracks = (ListView) mRootView.findViewById(R.id.lv_tracks);
        pbFavorite = (ProgressBar) mRootView.findViewById(R.id.pb_favorite);

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

                        if (mPlayingTrackAdapter != null) {
                            mPlayingTrackAdapter.notifyDataSetChanged();
                        }
                        break;

                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
                        if (mPlayingTrackAdapter != null) {
                            mPlayingTrackAdapter.notifyDataSetChanged();
                        }
                        break;

                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                        if (PlayerService.service.getState() == State.INTIALIZED) {
                            GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_please_wait), null);
                        } else {
                            if (mDataManager.getApplicationConfigurations().getSaveOfflineMode()) {
                                final List<Track> trackList = mPlayerService.getPlayingQueue();
                                final int startPosition = mPlayerService.getCurrentQueuePosition();
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
                                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.caching_text_message_go_online_global_menu), null);
                                } else {
                                    mPlayerService.playFromPositionNew(position);
                                }
                            }
                            updatePlayerActionButton();
                        }

                        break;
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        if (PlayerService.service.getState() == State.INTIALIZED) {
                            GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_please_wait), null);
                        } else { // Online
                            if (mDataManager.getApplicationConfigurations().getSaveOfflineMode()) {
                                final List<Track> trackList = mPlayerService.getPlayingQueue();
                                final int startPosition = mPlayerService.getCurrentQueuePosition();
                                int position = -1;
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
                                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.caching_text_message_go_online_global_menu), null);
                                } else {
                                    mPlayerService.playFromPositionNew(position);
                                }
                            }

                            updatePlayerActionButton();
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve player service.
        mPlayerService = PlayerService.service;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mDataManager = DataManager.getInstance(getActivity());
        this.sbPlayerProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(isAdPlaying())
                    return;
                isStartTracking = false;
                seekBarChange(seekBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(isAdPlaying())
                    return;
                isStartTracking = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(isAdPlaying())
                    return;
                if (fromUser && !isStartTracking) {
                    seekBarChange(seekBar);
                }
            }
        });


        vRegisterPlayerService();
        if (!mDataManager.getApplicationConfigurations().getSaveOfflineMode() && Utils.isConnected()) {
            mDataManager.getFavorites(getActivity(), MediaType.TRACK, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);
        } else {
            btnFav.setVisibility(View.VISIBLE);
            btnFav.setClickable(true);
            pbFavorite.setVisibility(View.GONE);
        }
    }

    public boolean isInForeground() {
        return this.isInForeground;
    }

    public void setInForeground(boolean bool) {
        this.isInForeground = bool;
        if (mPlayerService != null) {
            if (bool) {
                mPlayerService.registerPlayerStateListener(IPlayerStateListener);
                mPlayerService.setCarModeListener(mCarModeUIUpdate);
                if(isAdPlaying())
                {
                    loadAdForMusic(PlayerService.service.getAudioAdPlacement());
                }
            } else {
                mPlayerService.unregisterPlayerStateListener(IPlayerStateListener);
                mPlayerService.removeCarModeListener(mCarModeUIUpdate);
            }
        }
    }

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
//                    mListDefaultTracks.add((track));
//
//                    if (cacheState == DataBase.CacheState.CACHED) {
//                        mListOfflineTracks.add(track);
//                    }
//                }
//            }
//
//            // Change queue to Offline list.
//            if (mPlayerService != null) {
//                if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
//                    mPlayerService.stop();
//                }
//                mPlayerService.setPlayingQueue(new PlayingQueue(mListOfflineTracks, 0, mPlayerService));
//            }
//
//            refreshMusicPlayer();
//            isFromOffline = true;
//        }
//    }

//    public void setFromOffline(boolean bool) {
//        this.isFromOffline = bool;
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
//            if (mListDefaultTracks == null) {
//                mListDefaultTracks = new ArrayList<>();
//            }
//
//            for (Track track : mListPlayingTracks) {
//                if (!mListDefaultTracks.contains(track)) {
//                    mListDefaultTracks.add((track));
//                }
//            }
//
//            mPlayerService.setPlayingQueue(new PlayingQueue(mListDefaultTracks, 0, mPlayerService));
//
//            refreshMusicPlayer();
//            isFromOffline = false;
//        }
//    }

    public void refreshMusicPlayer() {
        // Get List of Favorites.
        if (mDataManager != null
                && !mDataManager.getApplicationConfigurations().getSaveOfflineMode()
                && Utils.isConnected()) {
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
            mListPlayingTracks.addAll(mPlayerService.getPlayingQueue());
            mPlayingTrackAdapter.notifyDataSetChanged();
        }

        // Update player UI.
        updatePlayerState();

        Track currPlaying = mPlayerService.getCurrentPlayingTrack();

        if (currPlaying != null) {
            updateDownloadState(currPlaying);
            updatePlayerActionButton();
            updateTrackInfo(currPlaying);
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

    private void updateTrackInfo(Track currPlaying) {
        ivTrackAvatar.setImageResource(R.drawable.background_home_tile_album_default);

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

    public void updatePlayerState() {
        if (mPlayerService != null) {
            State playerState = mPlayerService.getState();
            if (playerState == State.PLAYING) {
                btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_pause);
                pbPlayer.setVisibility(View.GONE);

            } else if (playerState == State.PAUSED || playerState == State.STOPPED || playerState == State.IDLE) {
                btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
            }
        }
    }

    private void seekBarChange(SeekBar seekBar) {
        if (mPlayerService != null && mPlayerService.isPlaying()) {
            int timeMilliseconds = (mPlayerService.getDuration() / 100) * seekBar.getProgress();
            mPlayerService.seekTo(timeMilliseconds);

            // reports badges and coins for the given playing track.
            if (timeMilliseconds >= PlayerService.TIME_REPORT_BADGES_MILLIES) {
                mPlayerService.reportBadgesAndCoins();
            }
        }
    }

    private void vRegisterPlayerService() {
//        if(mPlayerService != null && (mPlayerService.isPlaying() || mPlayerService.isLoading())) {
//            mPlayerService.stop();
//        }

        if(mPlayerService != null) {
            mPlayerService.registerPlayerUpdateListeners(new PlayerBarUpdateListener() {

                @Override
                public void OnPlayerBarUpdate(final int progress, String label) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!isStartTracking) {
                                sbPlayerProgress.setProgress(progress);
                            }
                        }
                    });
                }
            });
        }

        if (mPlayerService != null && mPlayerService.isQueueEmpty()) {
            if (this.isInForeground) {
                GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_player_queue_empty), null);
            }
        } else {
            // Preparing list of tracks.
            setupPlayingTrackList();
        }
    }

    public void playMusic() {
        if (mPlayerService.getState() == State.COMPLETED_QUEUE) {
            mPlayerService.replay();
        } else if (!mPlayerService.isPlaying()) {
            mPlayerService.play();
        }
    }

    private void setupPlayingTrackList() {
        if (mPlayerService != null && isVisible()) {
            mListPlayingTracks = mDataManager.getStoredPlayingQueue(mDataManager.getApplicationConfigurations()).getCopy();

            mPlayingTrackAdapter = new TrackAdapter(getActivity(), mListPlayingTracks);
            lvPlayingTracks.setAdapter(mPlayingTrackAdapter);

            lvPlayingTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(isAdPlaying())
                        return;
                    Track currPlaying = mListPlayingTracks.get(position);
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

                        if (!appConfig.getSaveOfflineMode()) {
                            mPlayerService.playNowFromPosition(mListPlayingTracks, position);
                            mSelectPos = position;
                            mPlayingTrackAdapter.notifyDataSetChanged();
                        } else if (cacheState == DataBase.CacheState.CACHED) {
                            mPlayerService.playNowFromPosition(mListPlayingTracks, position);
                            mSelectPos = position;
                            mPlayingTrackAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    public void vHandlePlayerClick(View v) {
        if(isAdPlaying())
            return;

        final DataManager dataManager = DataManager.getInstance(getActivity());
        final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();

        int view_id = v.getId();
        switch (view_id) {
            case R.id.btn_shuffle_play:

                if (mPlayerService.isShuffling()) {
                    mPlayerService.stopShuffle();
                    mListPlayingTracks.clear();
                    mListPlayingTracks.addAll(mPlayerService.getPlayingQueue());
                    mPlayingTrackAdapter.notifyDataSetChanged();
                    btnShuffle.setImageResource(R.drawable.icon_main_player_shuffle_white);
                    updatePlayerActionButton();
                } else {
                    mPlayerService.startShuffle();
                    mListPlayingTracks.clear();
                    mListPlayingTracks.addAll(mPlayerService.getPlayingQueue());
                    mPlayingTrackAdapter.notifyDataSetChanged();
                    btnShuffle.setImageResource(R.drawable.icon_main_player_suffle_blue);
                    updatePlayerActionButton();
                }
                break;

            case R.id.btn_repeat:

                switch (mPlayerService.getLoopMode()) {
                    case OFF:
                        mPlayerService.setLoopMode(LoopMode.REAPLAY_SONG);
                        btnRepeat.setImageResource(R.drawable.icon_main_player_loop_single_blue);
                        break;

                    case ON:
                        mPlayerService.setLoopMode(LoopMode.OFF);
                        btnRepeat.setImageResource(R.drawable.icon_main_player_loop_white);
                        break;

                    case REAPLAY_SONG:
                        mPlayerService.setLoopMode(LoopMode.ON);
                        btnRepeat.setImageResource(R.drawable.icon_main_player_loop_blue);
                        break;
                    default:
                        break;
                }

                break;

            case R.id.btn_player_previous:

                if (mPlayerService != null) {
                    if (PlayerService.service.getState() == State.INTIALIZED) {
                        GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_please_wait), null);
                    } else {
                        if (mDataManager.getApplicationConfigurations().getSaveOfflineMode()) {
                            final List<Track> trackList = mPlayerService.getPlayingQueue();

                            for(Track track : trackList) {
                                Log.d("Khoa", "track.name= " + track.getTitle());
                            }

                            final int startPosition = mPlayerService.getCurrentQueuePosition();
                            int position = -1;
                            for (int i = startPosition - 1; i >= 0; i--) {
                                Track trackTemp = trackList.get(i);
                                if (trackTemp != null) {
                                    DataBase.CacheState trackCacheState = DBOHandler.getTrackCacheState(getActivity(), "" + trackTemp.getId());
                                    if (trackCacheState == DataBase.CacheState.CACHED) {
                                        position = i;

                                        Log.d("Khoa", "Track.position= " + position);
                                        Log.d("Khoa", "btn_player_previous----title= " + trackTemp.getTitle());
                                        Log.d("Khoa", "btn_player_previous----album= " + trackTemp.getAlbumName());
                                        break;
                                    }
                                }
                            }

                            if (position == -1) {
                                GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.caching_text_message_go_online_global_menu), null);
                            } else {
                                mPlayerService.playFromPositionNew(position);
                            }

                        } else if (mPlayerService.fakePrevious() != null) {
                            mPlayerService.play();
                            vResetPlayer();
                        }
                    }

                    updatePlayerActionButton();
                }

                break;

            case R.id.btn_player_start_pause:

                State playerState = mPlayerService.getState();

                if (playerState == State.PLAYING) {
                    btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);
                    mPlayingTrackAdapter.notifyDataSetChanged();
                    mPlayerService.pause();
                } else if (playerState == State.PAUSED) {
                    btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_pause);
                    mPlayingTrackAdapter.notifyDataSetChanged();
                    mPlayerService.play();
                } else if (playerState == State.IDLE
                        || playerState == State.STOPPED
                        || playerState == State.COMPLETED_QUEUE
                        || playerState == State.COMPLETED) {
                    if (mDataManager.getApplicationConfigurations().getSaveOfflineMode()) {
                        final List<Track> trackList = mPlayerService.getPlayingQueue();
                        int position = -1;
                        for (int i = 0; i < trackList.size(); i++) {
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
                            mPlayerService.stop();
                        } else {
                            mPlayerService.playFromPositionNew(position);
                            mPlayingTrackAdapter.notifyDataSetChanged();
                        }
                    } else {
                        mPlayerService.play();
                        mPlayingTrackAdapter.notifyDataSetChanged();
                    }
                }

                break;

            case R.id.btn_player_next:

                if (PlayerService.service.getState() == State.INTIALIZED) {
                    GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_please_wait), null);
                } else {
                    if (mDataManager.getApplicationConfigurations().getSaveOfflineMode()) {
                        final List<Track> trackList = mPlayerService.getPlayingQueue();
                        final int startPosition = mPlayerService.getCurrentQueuePosition();
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
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.caching_text_message_go_online_global_menu), null);
                        } else {
                            mPlayerService.playFromPositionNew(position);
                        }
                    } else if (mPlayerService.fakeNext() != null) {
                        mPlayerService.play();
                        vResetPlayer();
                    }

                    updatePlayerActionButton();
                }
                break;

            case R.id.btn_player_back:

                mListener.hideMusicPlayer();

                break;

            case R.id.btn_action:

                mListener.openPlayerQueue();

                break;

            case R.id.btn_add_favorites:

                if (!com.hungama.myplay.activity.util.Utils.isConnected()) { // Network is inactive.
                    GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.go_online_network_error), null);
                } else if (appConfig.getSaveOfflineMode()) {
                    GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_offline_add_favorite), null);
                } else if (mPlayerService != null) {
                    final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();

                    if (curPlayingTrack != null) {
                        if (mListFavorites == null) {
                            mListFavorites = new ArrayList<>();
                        }

                        if (mListFavorites.contains(curPlayingTrack.getId())) {
                            mDataManager.removeFromFavorites(String.valueOf(curPlayingTrack.getId()), MEDIA_TYPE_SONG, this);
                        } else {
                            mDataManager.addToFavorites(String.valueOf(curPlayingTrack.getId()), MEDIA_TYPE_SONG, this);
                        }
                    }
                }

                break;

            case R.id.btn_download:

                final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();

                if (curPlayingTrack != null) {
                    final DataBase.CacheState trackCacheState = DBOHandler.getTrackCacheState(getActivity(), "" + curPlayingTrack.getId());

                    if (!com.hungama.myplay.activity.util.Utils.isConnected()) { // Network is inactive.
                        GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.go_online_network_error), null);
                    } else if (appConfig.getSaveOfflineMode()) {
                        GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.toast_offline), null);
                    } else if (trackCacheState == DataBase.CacheState.CACHED) {
                        GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_already_download), null);
                    } else if (trackCacheState == DataBase.CacheState.CACHING) {
                        //GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_already_downloading), null);
                    } else if (trackCacheState == DataBase.CacheState.QUEUED) {
                        //GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_already_download_queue), null);
                    } else {
                        final String msg = getResources().getString(R.string.msg_save_offline, curPlayingTrack.getTitle());
                        GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, msg, null);

                        final MediaItem trackMediaItem = new MediaItem(curPlayingTrack.getId(),
                                curPlayingTrack.getTitle(), curPlayingTrack.getAlbumName(),
                                curPlayingTrack.getArtistName(), getImageUrl(curPlayingTrack),
                                curPlayingTrack.getBigImageUrl(), MediaType.TRACK.toString(), 0, 0,
                                curPlayingTrack.getImages(), curPlayingTrack.getAlbumId());

                        CacheManager.saveOfflineAction(getActivity(), trackMediaItem, curPlayingTrack);

                    }
                }

                break;

            default:
                break;
        }
    }

    private void updatePlayerActionButton() {
        try {
            final int heightListTracks = lvPlayingTracks.getHeight();
            final int heighTrackItem = getResources().getDimensionPixelOffset(R.dimen.carmode_music_item_height);
            mSelectPos = mPlayerService.getCurrentPlayingTrackPosition();
            if (heightListTracks > 0
                    && mSelectPos > -1) {
                lvPlayingTracks.setSelectionFromTop(mSelectPos, heightListTracks / 2 - heighTrackItem / 2);
            }

            // Reset.
            btnNext.setEnabled(true);
            btnNext.setBackgroundResource(R.drawable.carmode_button_states);
            btnNext.setVisibility(View.VISIBLE);

            btnPrevious.setEnabled(true);
            btnPrevious.setBackgroundResource(R.drawable.carmode_button_states);
            btnPrevious.setVisibility(View.VISIBLE);

            final int lastTrackPos = mPlayerService.getPlayingQueue().size() - 1;
            if (mPlayerService.getPlayingQueue().size() == 1) {
                btnPrevious.setEnabled(false);
                btnPrevious.setVisibility(View.INVISIBLE);
                btnNext.setVisibility(View.INVISIBLE);
                btnNext.setEnabled(false);
            } else if (mSelectPos == 0) {
                btnPrevious.setEnabled(false);
                btnPrevious.setVisibility(View.INVISIBLE);
            } else if (mSelectPos == lastTrackPos) {
                btnNext.setVisibility(View.INVISIBLE);
                btnNext.setEnabled(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private void vResetPlayer() {
        mPlayingTrackAdapter.notifyDataSetChanged();

        btnNext.setClickable(mPlayerService.hasNext());
        btnPrevious.setClickable(mPlayerService.hasPrevious());
        btnPlayerStartPause.setImageResource(R.drawable.carmode_btn_play_large);

        sbPlayerProgress.setProgress(0);
        sbPlayerProgress.setSecondaryProgress(0);
    }

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
                if(profileFavoriteMediaItems==null){
                    btnFav.setImageResource(R.drawable.icon_main_player_favorites_white);
                }else {

                    if (profileFavoriteMediaItems != null && mPlayerService != null && !mPlayerService.isQueueEmpty()) {
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

                    }
                }
                btnFav.setVisibility(View.VISIBLE);
                btnFav.setClickable(true);
                pbFavorite.setVisibility(View.GONE);
                break;

            case OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE:
                BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);

                // has the item been added from favorites.
                if (addToFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS
                        && mPlayerService != null) {

                    // Update button state to "Added".
                    btnFav.setImageResource(R.drawable.icon_main_player_favorites_blue);
                    final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();
                    if (curPlayingTrack != null && mListFavorites != null) {
                        mListFavorites.add(curPlayingTrack.getId());
                    }

                    GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, addToFavoriteResponse.getMessage(), null);
                }

                btnFav.setVisibility(View.VISIBLE);
                btnFav.setClickable(true);
                pbFavorite.setVisibility(View.GONE);
                break;

            case OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE:
                BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);

                // has the item been removed from favorites.
                if (removeFromFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS
                        && mPlayerService != null) {

                    // Update button state to "Removed".
                    btnFav.setImageResource(R.drawable.icon_main_player_favorites_white);
                    final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();

                    if (curPlayingTrack != null && mListFavorites != null) {
                        mListFavorites.remove(curPlayingTrack.getId());
                    }
                    GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, removeFromFavoriteResponse.getMessage(), null);
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
            GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, errorMessage, null);
        }

        btnFav.setVisibility(View.VISIBLE);
        btnFav.setClickable(true);
        pbFavorite.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        if (this.mPlayerService != null) {
            mPlayerService.registerPlayerStateListener(IPlayerStateListener);
            mPlayerService.setCarModeListener(mCarModeUIUpdate);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        mDataManager.cancelGetListFavorites();

        if (mPlayerService != null) {
            mPlayerService.unregisterPlayerStateListener(IPlayerStateListener);
            mPlayerService.removeCarModeListener(mCarModeUIUpdate);
        }

        super.onStop();
    }

    public interface IMusicPlayer {
        void hideMusicPlayer();

        void openPlayerQueue();
    }

    private class TrackAdapter extends BaseAdapter {

        private Context mCtx;
        private List<Track> mListTracks;

        public TrackAdapter(Context ctx, List<Track> listTracks) {
            this.mListTracks = listTracks;
            this.mCtx = ctx;
        }

        @Override
        public int getCount() {
            return this.mListTracks.size();
        }

        @Override
        public Object getItem(int position) {
            return this.mListTracks.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RelativeLayout rlView = new RelativeLayout(mCtx);
            rlView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.carmode_music_item_height)));

            ImageView ivTrackAvatar = new ImageView(mCtx);
            ivTrackAvatar.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, getResources().getDimensionPixelOffset(R.dimen.carmode_music_item_height)));
            ivTrackAvatar.setBackgroundResource(R.drawable.background_home_tile_album_default);
            rlView.addView(ivTrackAvatar);

            ImageView ivPlayPause = new ImageView(mCtx);
            RelativeLayout.LayoutParams paramsIvPlayPause = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            paramsIvPlayPause.addRule(RelativeLayout.CENTER_IN_PARENT);
            ivPlayPause.setLayoutParams(paramsIvPlayPause);
            rlView.addView(ivPlayPause);

            ImageView ivOfflineView = new ImageView(mCtx);
            RelativeLayout.LayoutParams paramsIvOffline = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            ivOfflineView.setBackgroundResource(R.color.carmode_disable_bg);
            ivOfflineView.setLayoutParams(paramsIvOffline);
            rlView.addView(ivOfflineView);

            final Track track = (Track) getItem(position);
            if (track != null) {
                String[] images = ImagesManager.getImagesUrlArray(track.getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL,
                        DataManager.getDisplayDensityLabel());
                String imageURL = null;
                if (images != null && images.length > 0) {
                    imageURL = images[0];
                }
                if(TextUtils.isEmpty(imageURL))
                    imageURL = ImagesManager.getMusicArtSmallImageUrl(track.getImagesUrlArray());

                if (!TextUtils.isEmpty(imageURL)) {
                    PicassoUtil.with(mCtx).loadWithFit(null, imageURL, ivTrackAvatar, -1, TAG);
                }

                if (mPlayerService != null) {
                    final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();

                    if (curPlayingTrack != null
                            && (curPlayingTrack.getId() == track.getId())) {
                        ivTrackAvatar.setAlpha(0.5f);
                        ivPlayPause.setVisibility(View.VISIBLE);
                        switch (mPlayerService.getState()) {
                            case PAUSED:
                                ivPlayPause.setBackgroundResource(R.drawable.carmode_btn_play_grey);
                                break;
                            case PLAYING:
                                ivPlayPause.setBackgroundResource(R.drawable.carmode_btn_pause_grey);
                                break;
                            case IDLE:
                            case STOPPED:
                            case INTIALIZED:
                            case COMPLETED:
                            case COMPLETED_QUEUE:
                                ivPlayPause.setBackgroundResource(R.drawable.carmode_btn_play_grey);
                                break;

                            default:
                                ivPlayPause.setBackgroundResource(R.drawable.carmode_btn_pause_grey);
                                break;
                        }
                    } else {
                        ivTrackAvatar.setAlpha(1f);
                        ivPlayPause.setVisibility(View.GONE);
                    }
                }

                final DataManager dataManager = DataManager.getInstance(getActivity());
                final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();
                try {
                    final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + track.getId());
                    if ((appConfig.getSaveOfflineMode() || !Utils.isConnected()) && cacheState != DataBase.CacheState.CACHED) { // Stored downloaded track.
                        ivOfflineView.setVisibility(View.VISIBLE);
                        ivOfflineView.setClickable(true);
                        ivOfflineView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                GlobalFunction.showMessageDialog((Activity) mCtx, CustomDialogLayout.DialogType.MESSAGE, getString(R.string.caching_text_message_go_online_global_menu), null);
                            }
                        });
                    } else {
                        ivOfflineView.setClickable(false);
                        ivOfflineView.setOnClickListener(null);
                        ivOfflineView.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            return rlView;
        }

    }

    //----------------------Display Ad For Music------------------//
    public void loadAdForMusic(Placement audioAdPlacement){
        try {
            if (audioAdPlacement != null) {
                DisplayMetrics metrics = null;
                if (HomeActivity.metrics == null) {
                    metrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay()
                            .getMetrics(metrics);
                } else {
                    metrics = HomeActivity.metrics;
                }
                String imageURL = Utils.getDisplayProfile(metrics,
                        audioAdPlacement);
                Logger.i("imageURL","AdimageURL:"+imageURL);
                Picasso.with(getActivity()).cancelRequest(ivTrackAvatar);
                if (imageURL != null && !TextUtils.isEmpty(imageURL)) {
                    Picasso.with(getActivity()).load(imageURL).placeholder(R.drawable.background_home_tile_album_default).into(ivTrackAvatar);
                }
                tvTrackName.setText("Advertisement");
                tvAlbumName.setText("");
            }
        } catch (Exception e) {
        } catch (java.lang.Error e) {
        }
    }

    public boolean isAdPlaying(){
        return (PlayerService.service!=null && PlayerService.service.isAdPlaying());
    }
}
