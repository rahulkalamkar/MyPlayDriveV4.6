package com.hungama.hungamamusic.lite.carmode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.MediaDetailListAdapter;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;

import java.util.Collections;
import java.util.List;

import static com.hungama.myplay.activity.player.PlayerService.State.PLAYING;

public class PlaylistDetailFragment extends Fragment {

    public static final String TAG = PlaylistDetailFragment.class.getSimpleName();
    public static final String LIST_MEDIA_ITEMS = "list_media_items";
    public static final String PLAYLIST = "playlist";
    public PlayerService mPlayerService = null;
    private boolean isPlayingSongInList;

    // UI Elements
    private View mRootView;
    private RelativeLayout rlDetaillayout;
    private TextView tvTitle;
    private ProgressBar pbPlayerLoad;
    private ListView lvMusics;
    private ImageButton btnPlayPause;
    PlayerService.PlayerStateListener IPlayerStateListener = new PlayerService.PlayerStateListener() {

        @Override
        public void onTrackLoadingBufferUpdated(Track track, int precent) {
        }

        @Override
        public void onStartPlayingTrack(Track track) {
            pbPlayerLoad.setVisibility(View.GONE);
            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
        }

        @Override
        public void onStartPlayingAd(Placement audioad) {

        }

        @Override
        public void onStartLoadingTrack(Track track) {
            pbPlayerLoad.setVisibility(View.VISIBLE);
            btnPlayPause.setImageResource(R.drawable.icon_play_new);
        }

        @Override
        public void onSleepModePauseTrack(Track track) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onFinishPlayingTrack(Track track) {
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
    private MediaDetailListAdapter mMediaDetailListAdapter;
    private IPlaylistDetailListener mListener;
    private List<Track> mListTracks;
    private Playlist mPlaylist;

    public static PlaylistDetailFragment newInstance(IPlaylistDetailListener listener) {
        PlaylistDetailFragment fragment = new PlaylistDetailFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(IPlaylistDetailListener listener) {
        this.mListener = listener;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().isEmpty()) {
            mListTracks = (List<Track>) getArguments().getSerializable(LIST_MEDIA_ITEMS);
            mPlaylist = (Playlist) getArguments().getSerializable(PLAYLIST);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.carmode_fragment_music_detail, container, false);

        tvTitle = (TextView) mRootView.findViewById(R.id.tv_title);
        rlDetaillayout = (RelativeLayout) mRootView.findViewById(R.id.rl_detail_layout);
        lvMusics = (ListView) mRootView.findViewById(R.id.lv_musics);
        pbPlayerLoad = (ProgressBar) mRootView.findViewById(R.id.pb_player);
        btnPlayPause = (ImageButton) mRootView.findViewById(R.id.btn_player_start_pause);
        btnDownload = (ImageButton) mRootView.findViewById(R.id.btn_download);
        btnDownload.setVisibility(View.GONE);

        RelativeLayout rlFavorite = (RelativeLayout) mRootView.findViewById(R.id.rl_favorite);
        rlFavorite.setVisibility(View.GONE);

        return mRootView;
    }

    private PlayerService.IUpdateCarmodePlayerUI mCarModeUIUpdate = new PlayerService.IUpdateCarmodePlayerUI() {
        @Override
        public void onUpdateUI(int keyCode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
//                    if(isPlayingSongInList) {
                    btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
//                    }
                    break;

                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
                    break;

                default:
                    Log.v(TAG, "Unknown keycode");
                    break;
            }
        }
    };

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPlayerService = PlayerService.service;
        mPlayerService.registerPlayerStateListener(IPlayerStateListener);
        mPlayerService.setCarModeListener(mCarModeUIUpdate);

        rlDetaillayout.setVisibility(View.VISIBLE);

        if (mListTracks != null) {
            initPlaylistAvatar();

            // Check Cache state.
            handleCacheState();

            tvTitle.setText(mPlaylist.getName());

            mMediaDetailListAdapter = new MediaDetailListAdapter(getActivity(), mListTracks);

            if (isVisible()) {
                lvMusics.setAdapter(mMediaDetailListAdapter);
                updateDetailPlayerUI();
            }

            lvMusics.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    playSelectedSong(position);
                }
            });

            // Check state of music player.
        }

    }

    private void playSelectedSong(int position) {
//        final Track selectedTrack = mListTracks.get(position);
//        final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();
//
//        if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
//            if (mPlayerService.getCurrentPlayingTrack() != null) {
//                final long currentPlayingTrackId = mPlayerService.getCurrentPlayingTrack().getId();
//                if (currentPlayingTrackId == selectedTrack.getId()) {
//
//                } else {
//                    // Check whether track in Queue or Not.
//                    if (tracksInQueue.contains(selectedTrack)) {
//                        final int trackPos = tracksInQueue.indexOf(selectedTrack);
//                        mPlayerService.removeFrom(trackPos);
//                    }
//                    mListener.onAddTrackToQueue(Collections.singletonList(selectedTrack));
//                }
//            }
//        } else {
//
//            // Check whether track in Queue or Not.
//            if (tracksInQueue.contains(selectedTrack)) {
//                final int trackPos = tracksInQueue.indexOf(selectedTrack);
//                mPlayerService.removeFrom(trackPos);
//            }
//
//            mPlayerService.playNow(Collections.singletonList(selectedTrack));
//        }
        mMediaDetailListAdapter.setSelectedTrack(position);
        mMediaDetailListAdapter.notifyDataSetChanged();
        final Track selectedTrack = mListTracks.get(position);
        final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();

        // Check whether track in Queue or Not.
        if (tracksInQueue.contains(selectedTrack)) {
            final int trackPos = tracksInQueue.indexOf(selectedTrack);
            mPlayerService.removeFrom(trackPos);
        }

        mPlayerService.playNow(Collections.singletonList(selectedTrack));

        mListener.onPlayTrackFromDetail();
    }

    private void initPlaylistAvatar() {
        if (mListTracks.size() == 1) {// Single track.
            LinearLayout llPlaylistAvatar = (LinearLayout) mRootView.findViewById(R.id.ll_playlist_avatar);
            llPlaylistAvatar.setVisibility(View.GONE);

            final ImageView ivSingleAvatar = (ImageView) mRootView.findViewById(R.id.iv_single_avatar);
            ivSingleAvatar.setVisibility(View.VISIBLE);
            String[] images = ImagesManager.getImagesUrlArray(mListTracks.get(0).getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL,
                    DataManager.getDisplayDensityLabel());
            if (images.length > 0) {
                String imageURL = null;
                if (images != null && images.length > 0) {
                    imageURL = images[0];
                }
                if(TextUtils.isEmpty(imageURL))
                    imageURL = ImagesManager.getMusicArtSmallImageUrl(mListTracks.get(0).getImagesUrlArray());

                GlobalFunction.downloadImage(getActivity(), imageURL, ivSingleAvatar);
            }
        } else {
            ImageView ivTrackOne = (ImageView) mRootView.findViewById(R.id.iv_track_avatar_one);
            ImageView ivTrackTwo = (ImageView) mRootView.findViewById(R.id.iv_track_avatar_two);
            ImageView ivTrackThree = (ImageView) mRootView.findViewById(R.id.iv_track_avatar_three);
            ImageView ivTrackFour = (ImageView) mRootView.findViewById(R.id.iv_track_avatar_four);

            if (mListTracks != null && mListTracks.size() > 0) {
                String[] images = ImagesManager.getImagesUrlArray(mListTracks.get(0).getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL,
                        DataManager.getDisplayDensityLabel());

                if (images.length > 0) {
                    String imageURL = null;
                    if (images != null && images.length > 0) {
                        imageURL = images[0];
                    }
                    if(TextUtils.isEmpty(imageURL))
                        imageURL = ImagesManager.getMusicArtSmallImageUrl(mListTracks.get(0).getImagesUrlArray());
                    GlobalFunction.downloadImage(getActivity(), imageURL, ivTrackOne);
                }
            }
            if (mListTracks != null && mListTracks.size() > 1) {
                String[] images = ImagesManager.getImagesUrlArray(mListTracks.get(1).getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL,
                        DataManager.getDisplayDensityLabel());
                if (images.length > 0) {
                    String imageURL = null;
                    if (images != null && images.length > 0) {
                        imageURL = images[0];
                    }
                    if(TextUtils.isEmpty(imageURL))
                        imageURL = ImagesManager.getMusicArtSmallImageUrl(mListTracks.get(1).getImagesUrlArray());

                    GlobalFunction.downloadImage(getActivity(), imageURL, ivTrackTwo);
                }
            }

            if (mListTracks != null && mListTracks.size() > 2) {
                String[] images = ImagesManager.getImagesUrlArray(mListTracks.get(2).getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL,
                        DataManager.getDisplayDensityLabel());
                if (images.length > 0) {
                    String imageURL = null;
                    if (images != null && images.length > 0) {
                        imageURL = images[0];
                    }
                    if(TextUtils.isEmpty(imageURL))
                        imageURL = ImagesManager.getMusicArtSmallImageUrl(mListTracks.get(2).getImagesUrlArray());
                    GlobalFunction.downloadImage(getActivity(), imageURL, ivTrackThree);
                }
            }

            if (mListTracks != null && mListTracks.size() > 3) {
                String[] images = ImagesManager.getImagesUrlArray(mListTracks.get(3).getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL,
                        DataManager.getDisplayDensityLabel());

                if (images.length > 0) {
                    String imageURL = null;
                    if (images != null && images.length > 0) {
                        imageURL = images[0];
                    }
                    if(TextUtils.isEmpty(imageURL))
                        imageURL = ImagesManager.getMusicArtSmallImageUrl(mListTracks.get(3).getImagesUrlArray());
                    GlobalFunction.downloadImage(getActivity(), imageURL, ivTrackFour);
                }
            }
        }
    }

    public void vHandlePlaylistDetailClicks(View v) {
        int view_id = v.getId();

        switch (view_id) {
            case R.id.btn_back_main_menu:
                getFragmentManager().popBackStack();
                break;

            case R.id.btn_universal_player:
                mListener.onGoToMusicPlayer();
                break;

            case R.id.btn_add_to_queue:
                mListener.onAddTrackToQueue(mListTracks);
                break;

            case R.id.btn_download:
                String msg = getResources().getString(R.string.msg_save_offline, mPlaylist.getName());

                GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, msg, null);

                if (mListTracks != null && mListTracks.size() > 0) {
                    CacheManager.saveAllTracksOfflineAction(getActivity(), mListTracks);
                }

                break;

            case R.id.btn_player_start_pause:
//                PlayerService.State playerState = mPlayerService.getState();
//
//                if (playerState == PlayerService.State.PLAYING) {
//                    btnPlayPause.setImageResource(R.drawable.icon_play_new);
//                    mPlayerService.pause();
//                } else if (playerState == PlayerService.State.PAUSED) {
//                    btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
//                    mPlayerService.play();
//                } else if (playerState == PlayerService.State.IDLE
//                        || playerState == PlayerService.State.STOPPED) {
//                    playSelectedSong(0);
//                }

                PlayerService.State playerState = mPlayerService.getState();

                if (isPlayingSongInList) {
                    switch (playerState) {
                        case PLAYING:
                            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
                            mPlayerService.pause();
                            break;

                        case PAUSED:
                            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
                            mPlayerService.play();
                            break;

                        case IDLE:
                        case STOPPED:
                            playSelectedSong(0);
                            break;

                        default:
                            break;
                    }
                } else {
                    playSelectedSong(0);
                }

                break;

            default:
                break;
        }
    }

    public void updateDetailPlayerUI() {
//        PlayerService.State playerState = mPlayerService.getState();
//
//        if (playerState == PlayerService.State.PLAYING) {
//            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
//        } else if (playerState == PlayerService.State.PAUSED
//                || playerState == PlayerService.State.STOPPED
//                || playerState == PlayerService.State.IDLE) {
//            btnPlayPause.setImageResource(R.drawable.icon_play_new);
//        }

        if (mPlayerService != null
                && (mPlayerService.getCurrentPlayingTrack() != null)
                && (!mPlayerService.isQueueEmpty())) {
            final Track curTrack = mPlayerService.getCurrentPlayingTrack();

            for (Track track : mListTracks) {
                if (curTrack.getId() == track.getId()) {
                    if (mPlayerService.isLoading()) {
                        pbPlayerLoad.setVisibility(View.VISIBLE);
                        btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
                    } else if (mPlayerService.isPlaying()) {
                        pbPlayerLoad.setVisibility(View.GONE);

                        if (mPlayerService.getState() == PlayerService.State.PAUSED
                                || mPlayerService.getState() == PlayerService.State.STOPPED
                                || mPlayerService.getState() == PlayerService.State.IDLE) {
                            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
                        } else if (mPlayerService.getState() == PLAYING) {
                            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
                        }
                    }

                    isPlayingSongInList = true;
                    break;
                } else {
                    isPlayingSongInList = false;
                }
            }
        }

        if (!isPlayingSongInList) {
            // Should reset icon to Play if Player is playing song that not in Detail list.
            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
        }
    }

    public void handleCacheState() {
        boolean isPlaylistDownloaded = false;
        for (Track track : mListTracks) {
            final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + track.getId());
            if (cacheState == DataBase.CacheState.CACHED) {
                isPlaylistDownloaded = true;
                break;
            }
        }

        if (isPlaylistDownloaded) {
            btnDownload.setImageResource(R.drawable.carmode_cache_state_cached);
        } else {
            btnDownload.setImageResource(R.drawable.carmode_btn_download);
        }

        // Update list of Tracks.
        if (mMediaDetailListAdapter != null) {
            mMediaDetailListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        mPlayerService.removeCarModeListener(mCarModeUIUpdate);
        super.onStop();
    }

    public interface IPlaylistDetailListener {
        void onGoToMusicPlayer();

        void onAddTrackToQueue(List<Track> tracks);

        void onPlayTrackFromDetail();
    }
}
