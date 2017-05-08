package com.hungama.hungamamusic.lite.carmode.fragment;

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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.Error;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DiscoveryPlayerFragment extends Fragment {

    public static final String TAG = DiscoveryPlayerFragment.class.getSimpleName();
    public static final String MOOD_NAME = "mood_name";
    private final String MOOD_HEART_BROKEN = "Heart Broken";
    private final String MOOD_SAD = "Sad";
    private final String MOOD_CHILLED_OUT = "Chilled Out";
    private final String MOOD_HAPPY = "Happy";
    private final String MOOD_ECSTATIC = "Ecstatic";
    private final String MOOD_ROMANTIC = "Romantic";
    private final String MOOD_PARTY = "Party";
    public PlayerService mPlayerService;
    private String strMoodName;
    private List<Track> mListTracks;
    private DataManager mDataManager;
    private boolean isInForeground;
    // UI Elements
    private TextView tvTitleMood;
    private TextView tvTrackTitle;
    private TextView tvTrackSubTitle;
    private TextView tvNextTrackTitle;
    private TextView tvNextTrackSubTitle;
    private ImageView ivTrackAvatar;
    private ImageView ivMood;
    private ImageButton btnPlayPause;
    private RelativeLayout rlPlayerLoading;
    private FrameLayout frameLayoutMood;
    PlayerStateListener IPlayerStateListener = new PlayerStateListener() {

        @Override
        public void onTrackLoadingBufferUpdated(Track track, int precent) {
        }

        @Override
        public void onStartPlayingTrack(Track track) {
            rlPlayerLoading.setVisibility(View.GONE);
            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
            frameLayoutMood.setVisibility(View.VISIBLE);
            updateTrackInfo();
        }

        @Override
        public void onStartPlayingAd(Placement audioad) {
            if (PlayerService.service == null)
                return;
            frameLayoutMood.setVisibility(View.INVISIBLE);
            rlPlayerLoading.setVisibility(View.GONE);
            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
            if (PlayerService.service.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                try {
                    if (Utils.isCarMode()) {
                        loadAd(audioad);
                    }
                } catch (Exception e) {
                    Logger.printStackTrace(e);
                }
            }
        }

        @Override
        public void onStartLoadingTrack(Track track) {
            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
            rlPlayerLoading.setVisibility(View.VISIBLE);
        }

        @Override
        public void onSleepModePauseTrack(Track track) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onFinishPlayingTrack(Track track) {
            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
        }

        @Override
        public void onFinishPlayingQueue() {
        }

        @Override
        public void onErrorHappened(Error error) {
            Log.e(TAG, "onErrorHappened= " + error.getId());
        }

        @Override
        public void onAdCompletion() {
        }
    };
    private IDiscoveryPlayer mListener;

    public static DiscoveryPlayerFragment newInstance(String mood, List<Track> listTracks, IDiscoveryPlayer listener) {
        Bundle fragArgs = new Bundle();
        fragArgs.putString(MOOD_NAME, mood);

        DiscoveryPlayerFragment fragment = new DiscoveryPlayerFragment();
        fragment.setListTracks(listTracks);
        fragment.setListener(listener);
        fragment.setArguments(fragArgs);

        return fragment;
    }

    public void updateDiscoveryPlayerState() {
        State playerState = mPlayerService.getState();

        if (playerState == State.PLAYING) {
            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
        } else if (playerState == State.PAUSED) {
            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
        }
    }

    public void setListener(IDiscoveryPlayer listener) {
        this.mListener = listener;
    }

    public void setListTracks(List<Track> listTracks) {
        this.mListTracks = listTracks;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().isEmpty()) {
            strMoodName = getArguments().getString(MOOD_NAME);
        }

        this.mPlayerService = PlayerService.service;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.carmode_discovery_player, container, false);
        frameLayoutMood = (FrameLayout) view.findViewById(R.id.frameLayoutMood);
        tvTitleMood = (TextView) view.findViewById(R.id.tv_title_mood);
        tvTrackTitle = (TextView) view.findViewById(R.id.tv_discovery_title);
        tvTrackSubTitle = (TextView) view.findViewById(R.id.tv_discovery_sub_title);
        tvNextTrackTitle = (TextView) view.findViewById(R.id.tv_discovery_next_title);
        tvNextTrackSubTitle = (TextView) view.findViewById(R.id.tv_discovery_next_sub_title);
        ivTrackAvatar = (ImageView) view.findViewById(R.id.iv_track_avatar);
        ivMood = (ImageView) view.findViewById(R.id.iv_mood);
        btnPlayPause = (ImageButton) view.findViewById(R.id.btn_discovery_play_pause);
        rlPlayerLoading = (RelativeLayout) view.findViewById(R.id.rl_discovery_player_loading);

        initiateAndUpdateTileAndMode();

        return view;
    }

    private PlayerService.IUpdateCarmodePlayerUI mCarModeUIUpdate = new PlayerService.IUpdateCarmodePlayerUI() {
        @Override
        public void onUpdateUI(int keyCode) {
            if (isInForeground()) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_MEDIA_PLAY:
                        btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
                        break;

                    case KeyEvent.KEYCODE_MEDIA_PAUSE:
                        btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
                        break;

                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                        if (PlayerService.service.getState() == State.INTIALIZED) {
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_please_wait), null);
                        } else {
                            updateTrackInfo();
                        }
                        break;

                    default:
                        Log.v(TAG, "Unknown keycode");
                        break;
                }
            }
        }
    };


    private void initiateAndUpdateTileAndMode()
    {
        updateMood(strMoodName);
        this.mDataManager = DataManager.getInstance(getActivity());
        updateDiscoveryPlayerUI();
    }

    public void updateMood(String mood)
    {
        strMoodName = mood;
        if (strMoodName != null) {
            tvTitleMood.setText(strMoodName);

            if (strMoodName.equals(MOOD_HEART_BROKEN)) {
                ivMood.setImageResource(R.drawable.carmode_mood_selector_heart_broken);
            } else if (strMoodName.equals(MOOD_SAD)) {
                ivMood.setImageResource(R.drawable.carmode_mood_selector_sad);
            } else if (strMoodName.equals(MOOD_CHILLED_OUT)) {
                ivMood.setImageResource(R.drawable.carmode_mood_selector_chilled_out);
            } else if (strMoodName.equals(MOOD_HAPPY)) {
                ivMood.setImageResource(R.drawable.carmode_mood_selector_happy);
            } else if (strMoodName.equals(MOOD_ECSTATIC)) {
                ivMood.setImageResource(R.drawable.carmode_mood_selector_ecstatic);
            } else if (strMoodName.equals(MOOD_ROMANTIC)) {
                ivMood.setImageResource(R.drawable.carmode_mood_selector_romantic);
            } else if (strMoodName.equals(MOOD_PARTY)) {
                ivMood.setImageResource(R.drawable.carmode_mood_selector_party);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //		setupTrackInfo(mPlayerService.getCurrentPlayingTrack());
    }

    public void updateDiscoveryPlayerUI() {
        if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
            mPlayerService.stop();
        }

        mPlayerService.playDiscoverySongs(this.mListTracks, PlayMode.DISCOVERY_MUSIC);
        updateTrackInfo();

    }

    private void updateTrackInfo() {
        if(isAdPlaying()){
            loadAd(PlayerService.service.getAudioAdPlacement());
            return;
        }
        final Track curTrack = mPlayerService.getCurrentPlayingTrack();

        if (curTrack != null) {
            ivTrackAvatar.setImageResource(R.drawable.background_home_tile_album_default);

            String imageURL = "";
            String[] images = ImagesManager.getImagesUrlArray(curTrack.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE,
                    mDataManager.getDisplayDensity());
            if (images != null && images.length > 0) {
                imageURL = images[0];
            }

            if(TextUtils.isEmpty(imageURL))
                imageURL = ImagesManager.getMusicArtSmallImageUrl(curTrack.getImagesUrlArray());

            Picasso.with(getActivity()).cancelRequest(ivTrackAvatar);
            if (imageURL != null && !TextUtils.isEmpty(imageURL)) {
                Picasso.with(getActivity()).load(imageURL).placeholder(R.drawable.background_home_tile_album_default).into(ivTrackAvatar);
            }

            // Setup Track's title.
            tvTrackTitle.setText(curTrack.getTitle());
            tvTrackSubTitle.setText(curTrack.getAlbumName());

            final Track nextTrack = mPlayerService.getNextTrack();
            if (nextTrack == null) { // End of list tracks.
            } else {
                tvNextTrackTitle.setText(nextTrack.getTitle());
                tvNextTrackSubTitle.setText(nextTrack.getAlbumName());
            }
        }

    }

    private void updateAdsInfo() {
        final Track curTrack = mPlayerService.getCurrentPlayingTrack();

        if (curTrack != null) {
            ivTrackAvatar.setImageResource(R.drawable.background_home_tile_album_default);

            String imageURL = "";
            String[] images = ImagesManager.getImagesUrlArray(curTrack.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE,
                    mDataManager.getDisplayDensity());
            if (images != null && images.length > 0) {
                imageURL = images[0];
            }
            if(TextUtils.isEmpty(imageURL))
                imageURL = ImagesManager.getMusicArtSmallImageUrl(curTrack.getImagesUrlArray());

            Picasso.with(getActivity()).cancelRequest(ivTrackAvatar);
            if (imageURL != null && !TextUtils.isEmpty(imageURL)) {
                Picasso.with(getActivity()).load(imageURL).placeholder(R.drawable.background_home_tile_album_default).into(ivTrackAvatar);
            }

            // Setup Track's title.
            tvTrackTitle.setText(curTrack.getTitle());
            tvTrackSubTitle.setText(curTrack.getAlbumName());

            final Track nextTrack = mPlayerService.getNextTrack();
            if (nextTrack == null) { // End of list tracks.
            } else {
                tvNextTrackTitle.setText(nextTrack.getTitle());
                tvNextTrackSubTitle.setText(nextTrack.getAlbumName());
            }
        }
    }

    public boolean isInForeground() {
        return this.isInForeground;
    }

    public void setInForeground(boolean bool) {
        if(mPlayerService!=null && !this.isInForeground && bool){
            updateTrackInfo();
        }
        this.isInForeground = bool;
    }

    public void vHandleDiscoveryPlayer(View v) {
        if(isAdPlaying())
            return;
        int view_id = v.getId();

        switch (view_id) {
            case R.id.btn_player_back:
//                mPlayerService.stop();
//                mPlayerService.unregisterPlayerStateListener(IPlayerStateListener);
//                getFragmentManager().popBackStack();
                mListener.hideDiscoveryPlayer();
                break;

            case R.id.btn_discovery_play_pause:
                PlayerService.State playerState = mPlayerService.getState();

                if (playerState == State.PLAYING) {
                    btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
                    mPlayerService.pause();
                } else if (playerState == State.PAUSED) {
                    btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
                    mPlayerService.play();
                }

                break;

            case R.id.btn_play_next:
                if (PlayerService.service.getState() == State.INTIALIZED) {
                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_please_wait), null);
                } else if (mPlayerService.fakeNext() != null) {
                    mPlayerService.play();
                    updateTrackInfo();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onStart() {
        if(this.mPlayerService != null) {
            this.mPlayerService.registerPlayerStateListener(IPlayerStateListener);
            this.mPlayerService.setCarModeListener(mCarModeUIUpdate);
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        if (this.mPlayerService != null) {
            this.mPlayerService.unregisterPlayerStateListener(IPlayerStateListener);
            this.mPlayerService.removeCarModeListener(mCarModeUIUpdate);
        }

        super.onStop();
    }

    public interface IDiscoveryPlayer {
        void hideDiscoveryPlayer();
    }

    //---------------------Load ads-----------//
    public void loadAd(Placement audioAdPlacement){
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
                Logger.i("imageURL", "AdimageURL:" + imageURL);
                Picasso.with(getActivity()).cancelRequest(ivTrackAvatar);
                if (imageURL != null && !TextUtils.isEmpty(imageURL)) {
                    Picasso.with(getActivity()).load(imageURL).placeholder(R.drawable.background_home_tile_album_default).into(ivTrackAvatar);
                }
                tvTrackTitle.setText("Advertisement");
                tvTrackSubTitle.setText("");
            }
        } catch (Exception e) {
        } catch (java.lang.Error e) {
        }
    }

    public boolean isAdPlaying(){
        return (PlayerService.service!=null && PlayerService.service.isAdPlaying());
    }
}
