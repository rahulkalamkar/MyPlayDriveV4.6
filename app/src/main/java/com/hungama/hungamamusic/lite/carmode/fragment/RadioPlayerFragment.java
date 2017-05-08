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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistSongsOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.Error;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class RadioPlayerFragment extends Fragment implements CommunicationOperationListener {

    public static final String TAG = RadioPlayerFragment.class.getSimpleName();
    public static final String RADIO_ITEM = "radio_item";
    private static final int CODE_FAVORITE_SUCCESS = 1;

    private MediaItem mRadio;
    private PlayerService mPlayerService;
    private DataManager mDataManager;
    private PlayMode mRadioMode;
    private boolean isAddedFavorite;
    private boolean isInForeground;

    // UI elements
    private TextView tvRadioTitle;
    private TextView tvTrackTitle;
    private TextView tvTrackSubTitle;
    private TextView tvNextTrackTitle;
    private TextView tvNextTrackSubTitle;
    private ImageView ivTrackAvatar;
    private ImageView ivNowPlaying;
    private ImageView ivComingNext;
    private RelativeLayout rlLoading;
    private ImageButton btnPlayPause;
    private ImageButton btnNext;
    PlayerStateListener IPlayerStateListener = new PlayerStateListener() {

        @Override
        public void onStartLoadingTrack(Track track) {
            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
            rlLoading.setVisibility(View.VISIBLE);
        }

        @Override
        public void onTrackLoadingBufferUpdated(Track track, int precent) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStartPlayingTrack(Track track) {
            rlLoading.setVisibility(View.GONE);
            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
            updateRadioInfo();
        }

        @Override
        public void onFinishPlayingTrack(Track track) {
            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
        }

        @Override
        public void onFinishPlayingQueue() {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSleepModePauseTrack(Track track) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onErrorHappened(Error error) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStartPlayingAd(Placement audioad) {
            // TODO Auto-generated method stub
            if (PlayerService.service == null)
                return;
            rlLoading.setVisibility(View.GONE);
            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
            if (PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
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
        public void onAdCompletion() {
            // TODO Auto-generated method stub

        }

    };
    private ProgressBar pbLoadRadioItems;
    private RelativeLayout rlRadioLayout;
    private CustomDialogLayout mDialog;
    private RelativeLayout rlFavorite;
    private ImageButton btnFavorite;
    private ProgressBar pbFavorite;
    private IRadioPlayer mListener;
    private View mRootView;

    public static RadioPlayerFragment newInstance(MediaItem radioItem, PlayMode radioMode, IRadioPlayer listener) {
        Bundle fragArgs = new Bundle();
        fragArgs.putSerializable(RadioPlayerFragment.RADIO_ITEM, (Serializable) radioItem);

        RadioPlayerFragment fragment = new RadioPlayerFragment();
        fragment.setRadioMode(radioMode);
        fragment.setArguments(fragArgs);
        fragment.setListener(listener);

        return fragment;
    }

    public void setRadioMode(PlayMode radioMode) {
        this.mRadioMode = radioMode;
    }

    public void setRadioItem(MediaItem radioItem) {
        this.mRadio = radioItem;
    }


    public void setListener(IRadioPlayer listener) {
        this.mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().isEmpty()) {
            mRadio = (MediaItem) getArguments().getSerializable(RADIO_ITEM);
        }

        this.mPlayerService = PlayerService.service;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.carmode_fragment_radio_player, container, false);

        tvRadioTitle = (TextView) mRootView.findViewById(R.id.tv_title_radio);
        rlLoading = (RelativeLayout) mRootView.findViewById(R.id.rl_load_avatar);
        btnPlayPause = (ImageButton) mRootView.findViewById(R.id.btn_play_pause);
        tvTrackTitle = (TextView) mRootView.findViewById(R.id.tv_now_title);
        tvTrackSubTitle = (TextView) mRootView.findViewById(R.id.tv_now_sub_title);
        ivTrackAvatar = (ImageView) mRootView.findViewById(R.id.iv_track_avatar);
        ivNowPlaying = (ImageView) mRootView.findViewById(R.id.iv_now_playing);

//        if (this.mRadioMode == PlayMode.TOP_ARTISTS_RADIO) {
        pbLoadRadioItems = (ProgressBar) mRootView.findViewById(R.id.pb_load_radio_items);
        rlRadioLayout = (RelativeLayout) mRootView.findViewById(R.id.rl_radio_layout);
        ivComingNext = (ImageView) mRootView.findViewById(R.id.iv_comming_next);
        tvNextTrackTitle = (TextView) mRootView.findViewById(R.id.tv_next_title);
        tvNextTrackSubTitle = (TextView) mRootView.findViewById(R.id.tv_next_sub_title);
        rlFavorite = (RelativeLayout) mRootView.findViewById(R.id.rl_favorite);
        btnFavorite = (ImageButton) mRootView.findViewById(R.id.btn_favorite);
        btnNext = (ImageButton) mRootView.findViewById(R.id.btn_play_next);
        pbFavorite = (ProgressBar) mRootView.findViewById(R.id.pb_favorite);
//        }

        return mRootView;
    }

    private void validateNextRadioView() {
        LinearLayout llNextTrack = (LinearLayout) mRootView.findViewById(R.id.ll_next_track);
        TextView tvNextTitle = (TextView) mRootView.findViewById(R.id.tv_title_coming_next);

        if (this.mRadioMode == PlayMode.TOP_ARTISTS_RADIO) {
            llNextTrack.setVisibility(View.VISIBLE);
            tvNextTitle.setVisibility(View.VISIBLE);
            ivComingNext.setVisibility(View.VISIBLE);
            rlFavorite.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.VISIBLE);

        } else if (this.mRadioMode == PlayMode.LIVE_STATION_RADIO) {
            llNextTrack.setVisibility(View.GONE);
            tvNextTitle.setVisibility(View.GONE);
            ivComingNext.setVisibility(View.GONE);
            rlFavorite.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
        }
    }

    public boolean isInForeground() {
        return this.isInForeground;
    }

    public void setInForeground(boolean bool) {
        this.isInForeground = bool;
        if(mPlayerService!=null && mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO && !this.isInForeground && bool){
            updateRadioInfo();
        }
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
                            updateRadioInfo();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.mDataManager = DataManager.getInstance(getActivity());
        setupRadio();
    }

    public void setupRadio() {
        if (mRadio != null) {
            this.tvRadioTitle.setText(mRadio.getTitle());
            String imageUrl = ImagesManager.getRadioArtImageUrl(mRadio.getImagesUrlArray());
            PicassoUtil.with(getActivity()).loadWithoutTag(null, imageUrl, ivTrackAvatar, R.drawable.icon_main_player_no_content);

            validateNextRadioView();

            switch (mRadioMode) {
                case LIVE_STATION_RADIO:
                    playLiveStation();

                    // Update Radio info.
                    updateRadioInfo();
                    break;

                case TOP_ARTISTS_RADIO:
                    mDataManager = DataManager.getInstance(getActivity());
                    mDataManager.getRadioTopArtistSongs(mRadio, this);
                    mDataManager.getFavorites(getActivity(), MediaType.ARTIST, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);
                    break;

                default:
                    break;
            }
        }
    }

    private void playLiveStation() {
        LiveStation liveStation = (LiveStation) mRadio;
        Track liveStationTrack = new Track(liveStation.getId(), liveStation.getTitle(), liveStation.getDescription(), null,
                liveStation.getImageUrl(), liveStation.getImageUrl(), liveStation.getImages(), liveStation.getAlbumId());
        liveStationTrack.setMediaHandle(liveStation.getStreamingUrl());
        liveStationTrack.setTag(liveStation);

//        List<Track> liveStationList = new ArrayList<Track>();
//        liveStationList.add(liveStationTrack);

		/*
         * sets to each track a reference to a copy of the original radio item.
		 * This to make sure that the player bar can get source Radio item
		 * without leaking this activity!
		 */
//        for (Track track : liveStationList) {
//            track.setTag(liveStation);
//        }

        // starts to play.
        mPlayerService.playRadio(Collections.singletonList(liveStationTrack), PlayMode.LIVE_STATION_RADIO);
    }

    private void vHandleMediaAvatar(Track track, ImageView ivAvatar) {
        Picasso.with(getActivity()).cancelRequest(ivAvatar);
        final String imageUrl = ImagesManager.getMusicArtSmallImageUrl(track.getImagesUrlArray());
        if (!TextUtils.isEmpty(imageUrl)) {
            Picasso.with(getActivity()).load(imageUrl).into(ivAvatar);
        }
    }

    private void updateRadioInfo() {
        final Track curRadio = mPlayerService.getCurrentPlayingTrack();

        if (curRadio != null) {
            //ivTrackAvatar.setImageResource(R.drawable.background_home_tile_album_default);
            ivNowPlaying.setImageResource(R.drawable.background_home_tile_album_default);

            // Now Playing.
            vHandleMediaAvatar(curRadio, ivNowPlaying);

            // Setup Track's title.
            tvTrackTitle.setText(curRadio.getTitle());
            tvTrackSubTitle.setText(curRadio.getAlbumName());

            if (mRadioMode == PlayMode.TOP_ARTISTS_RADIO) { // On-demand radio.
                ivComingNext.setImageResource(R.drawable.background_home_tile_album_default);

                Track nextTrack = mPlayerService.getNextTrack();
                if (nextTrack == null) { // End of list tracks.

                } else {
                    tvNextTrackTitle.setText(nextTrack.getTitle());
                    tvNextTrackSubTitle.setText(nextTrack.getAlbumName());
                    vHandleMediaAvatar(nextTrack, ivComingNext);
                }
                if(isAdPlaying())
                    loadAd(PlayerService.service.getAudioAdPlacement());
            }
        }
    }

    public void vHandleRadioPlayer(View v) {
        if(isAdPlaying())
            return;
        int view_id = v.getId();

        switch (view_id) {
            case R.id.btn_player_back:
                mListener.hideRadioPlayer();
                break;
            case R.id.btn_play_next:
                if (PlayerService.service.getState() == State.INTIALIZED) {
                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_please_wait), null);
                } else if (mPlayerService.fakeNext() != null) {
                    mPlayerService.play();
                    updateRadioInfo();
                }
                break;

            case R.id.btn_play_pause:
                State playerState = mPlayerService.getState();

                if (playerState == State.PLAYING) {
                    btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
                    mPlayerService.pause();
                } else if (playerState == State.PAUSED) {
                    btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
                    mPlayerService.play();
                }
                break;

            case R.id.btn_favorite:
                if (com.hungama.myplay.activity.util.Utils.isConnected()) { // Network is inactive.
                    if (isAddedFavorite) {
                        mDataManager.removeFromFavorites(String.valueOf(mRadio.getId()), "ondemandradio", this);
                    } else {
                        mDataManager.addToFavorites(String.valueOf(mRadio.getId()), "ondemandradio", this);
                    }
                } else {
                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.go_online_network_error), null);
                }
                break;

            default:
                break;
        }
    }

    public void updateRadioPlayerState() {
        State playerState = mPlayerService.getState();

        if (playerState == State.PLAYING) {
            btnPlayPause.setImageResource(R.drawable.carmode_btn_pause);
        } else if (playerState == State.PAUSED) {
            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
        }
    }


    @Override
    public void onStart(int operationId) {
        String msg = "";

        switch (operationId) {
            case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
                btnFavorite.setVisibility(View.INVISIBLE);
                btnFavorite.setClickable(false);
                pbFavorite.setVisibility(View.VISIBLE);
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

            case OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS:
                rlRadioLayout.setVisibility(View.GONE);
                pbLoadRadioItems.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        switch (operationId) {
            case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
                ProfileFavoriteMediaItems profileFavoriteMediaItems = (ProfileFavoriteMediaItems) responseObjects.get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS);
                if (profileFavoriteMediaItems != null) {
                    List<MediaItem> listItems = profileFavoriteMediaItems.mediaItems;

                    if ((listItems != null) && (listItems.size() > 0)) {
                        for (int i = 0; i < listItems.size(); i++) {
                            final long favMediaId = listItems.get(i).getId();
                            if (mRadio.getId() == favMediaId) {
                                isAddedFavorite = true;
                                break;
                            } else {
                                isAddedFavorite = false;
                            }
                        }
                    }
                }else{
                    isAddedFavorite = false;
                }

                if (isAddedFavorite) {
                    btnFavorite.setImageResource(R.drawable.carmode_btn_favorite_added);
                } else {
                    btnFavorite.setImageResource(R.drawable.carmode_btn_favorites);
                }

                btnFavorite.setVisibility(View.VISIBLE);
                btnFavorite.setClickable(true);
                pbFavorite.setVisibility(View.GONE);
                break;

            case OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE:
                BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);

                // has the item been added from favorites.
                if (addToFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS) {

                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, addToFavoriteResponse.getMessage(), null);
                    btnFavorite.setImageResource(R.drawable.carmode_btn_favorite_added);
                    isAddedFavorite = true;
                }

                btnFavorite.setVisibility(View.VISIBLE);
                btnFavorite.setClickable(true);
                pbFavorite.setVisibility(View.GONE);

                break;

            case OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE:
                BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects
                        .get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);

                // has the item been removed from favorites.
                if (removeFromFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS) {
                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, removeFromFavoriteResponse.getMessage(), null);
                    btnFavorite.setImageResource(R.drawable.carmode_btn_favorites);
                    isAddedFavorite = false;
                }

                btnFavorite.setVisibility(View.VISIBLE);
                btnFavorite.setClickable(true);
                pbFavorite.setVisibility(View.GONE);
                break;

            case OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS:
                // gets the radio tracks
                List<Track> radioTracks = (List<Track>) responseObjects.get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_TRACKS);
                MediaItem mediaItem = (MediaItem) responseObjects.get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_MEDIA_ITEM);

                for (Track track : radioTracks) {
                    track.setTag(mediaItem);
                }

                // starts to play.
                mPlayerService.playRadio(radioTracks, PlayMode.TOP_ARTISTS_RADIO);

                // Update Radio info.
                updateRadioInfo();

                rlRadioLayout.setVisibility(View.VISIBLE);
                pbLoadRadioItems.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
        if (mDialog != null && mDialog.isShown()) {
            mDialog.hide(); // Dismiss dialog.
        }
        GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, errorMessage, new CustomDialogLayout.IDialogListener() {
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
                mListener.hideRadioPlayer();
            }
        });

    }

    @Override
    public void onStart() {

        if(mPlayerService != null) {
            mPlayerService.registerPlayerStateListener(IPlayerStateListener);
            mPlayerService.setCarModeListener(mCarModeUIUpdate);
        }

        super.onStart();
    }

    @Override
    public void onStop() {

        if(this.mPlayerService != null) {
            this.mPlayerService.stopLiveRadioUpdater();
            this.mPlayerService.unregisterPlayerStateListener(IPlayerStateListener);
            this.mPlayerService.removeCarModeListener(mCarModeUIUpdate);
        }

        super.onStop();
    }

    public interface IRadioPlayer {
        void hideRadioPlayer();
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
