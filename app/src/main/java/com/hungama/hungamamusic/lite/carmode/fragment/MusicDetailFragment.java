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
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.MediaDetailListAdapter;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout.DialogType;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout.IDialogListener;
import com.hungama.hungamamusic.lite.carmode.view.VerticalSeekBar;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.Error;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.State;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.hungama.myplay.activity.player.PlayerService.State.PLAYING;

public class MusicDetailFragment extends Fragment implements CommunicationOperationListener {

    public static final String TAG = MusicDetailFragment.class.getSimpleName();
    public static final String MEDIA_ITEM = "media_item";
    private static final String MEDIA_TYPE_SONG = "song";

    private static final int CODE_FAVORITE_SUCCESS = 1;
    public PlayerService mPlayerService = null;
    private MediaItem mMediaItem;
    private MediaSetDetails mMediaSetDetails;
    private List<Track> mTracks;
    private MediaDetailListAdapter mMediaDetailListAdapter;
    private DataManager mDataManager;
    private IMusicDetailListener mListener;
    private boolean isAddedFavorite;
    private boolean isSaveOffline;
    private boolean isPlayingSongInList;

    // UI Elements
    private View mRootView;
    private RelativeLayout rlDetaillayout;
    private TextView tvTitle;
    private ProgressBar pbPlayerLoad;
    private ListView lvMusics;
    private ImageButton btnPlayPause;
    PlayerStateListener IPlayerStateListener = new PlayerStateListener() {

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
            btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
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
        public void onErrorHappened(Error error) {
            Log.e(TAG, "onErrorHappened= " + error.getId());
        }

        @Override
        public void onAdCompletion() {
            // TODO Auto-generated method stub
        }
    };
    private ImageButton btnFavorite;
    private ProgressBar pbFavorite;
    private ImageButton btnDownload;
    private VerticalSeekBar sbListViewProgress;
    private LinearLayout llListViewProgressCtrl;
    private CustomDialogLayout mDialog;

    public static final MusicDetailFragment newInstance(IMusicDetailListener listener) {
        MusicDetailFragment fragment = new MusicDetailFragment();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getArguments().isEmpty()) {
            mMediaItem = (MediaItem) getArguments().getSerializable(MEDIA_ITEM);
        }

        mPlayerService = PlayerService.service;
    }

    public void setListener(IMusicDetailListener listener) {
        this.mListener = listener;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.carmode_fragment_music_detail, container, false);

        tvTitle = (TextView) mRootView.findViewById(R.id.tv_title);
        rlDetaillayout = (RelativeLayout) mRootView.findViewById(R.id.rl_detail_layout);
        lvMusics = (ListView) mRootView.findViewById(R.id.lv_musics);
        pbPlayerLoad = (ProgressBar) mRootView.findViewById(R.id.pb_player);
        sbListViewProgress = (VerticalSeekBar) mRootView.findViewById(R.id.sb_listview_progress);
        btnPlayPause = (ImageButton) mRootView.findViewById(R.id.btn_player_start_pause);
        btnFavorite = (ImageButton) mRootView.findViewById(R.id.btn_favorite);
        pbFavorite = (ProgressBar) mRootView.findViewById(R.id.pb_favorite);
        btnDownload = (ImageButton) mRootView.findViewById(R.id.btn_download);
        llListViewProgressCtrl = (LinearLayout) mRootView.findViewById(R.id.ll_listview_progress_control);

        return mRootView;
    }

    private PlayerService.IUpdateCarmodePlayerUI mCarModeUIUpdate = new PlayerService.IUpdateCarmodePlayerUI() {
        @Override
        public void onUpdateUI(int keyCode) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_PLAY:
//                    if (isPlayingSongInList) {
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

        mDataManager = DataManager.getInstance(getActivity());

        if (mMediaItem.getMediaType() == MediaType.TRACK) {
            final Track selectedTrack = new Track(mMediaItem.getId(), mMediaItem.getTitle(), mMediaItem.getAlbumName(), mMediaItem.getArtistName(), mMediaItem.getImageUrl(), mMediaItem.getBigImageUrl(), mMediaItem.getImages(), mMediaItem.getAlbumId());
            mTracks = Collections.singletonList(selectedTrack);
            mMediaDetailListAdapter = new MediaDetailListAdapter(getActivity(), mTracks);

            if (isVisible()) {
                lvMusics.setAdapter(mMediaDetailListAdapter);
            }

            // Visible Detail layout and hide dialog after finish initial steps.
            rlDetaillayout.setVisibility(View.VISIBLE);

            // Set title.
            tvTitle.setText(selectedTrack.getTitle());

            // Setup avatar.
            LinearLayout llPlaylistAvatar = (LinearLayout) mRootView.findViewById(R.id.ll_playlist_avatar);
            llPlaylistAvatar.setVisibility(View.GONE);
            final ImageView ivSingleAvatar = (ImageView) mRootView.findViewById(R.id.iv_single_avatar);
            ivSingleAvatar.setVisibility(View.VISIBLE);

            String[] images = ImagesManager.getImagesUrlArray(mMediaItem.getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL, DataManager.getDisplayDensityLabel());
            String imageURL = null;
            if (images != null && images.length > 0) {
                imageURL = images[0];
            }
            if(TextUtils.isEmpty(imageURL))
                imageURL = ImagesManager.getMusicArtSmallImageUrl(mMediaItem.getImagesUrlArray());

            if (!TextUtils.isEmpty(imageURL)) {
                GlobalFunction.downloadImage(getActivity(), imageURL, ivSingleAvatar);
            } else {
                ivSingleAvatar.setImageResource(R.drawable.background_home_tile_album_default);
            }

            // Check Cache state.
            handleCacheState();

            // Check state of music player.
            updateDetailPlayerUI();

        } else { // MediaType == Playlist/Album
            mDataManager.getMediaDetails(mMediaItem, null, this);
        }

        mDataManager.getFavorites(getActivity(), mMediaItem.getMediaType(), mDataManager.getApplicationConfigurations().getPartnerUserId(), this);

        lvMusics.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                playSelectedSong(position);
            }
        });

        lvMusics.setOnScrollListener(new OnScrollListener() {

                                         @Override
                                         public void onScrollStateChanged(AbsListView view, int scrollState) {
                                             // TODO Auto-generated method stub
                                         }

                                         @Override
                                         public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                                              int totalItemCount) {
                                             if (firstVisibleItem == 0) {
                                                 sbListViewProgress.setMax(totalItemCount - visibleItemCount);
                                             }
                                             sbListViewProgress.setProgress(firstVisibleItem);
                                         }
                                     }
        );

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
                                                                  lvMusics.setSelection(progress);
                                                              }
                                                          }
                                                      }
        );
    }

//    private void playSelectedSong(int position) {
//        mMediaDetailListAdapter.setSelectedTrack(position);
//        mMediaDetailListAdapter.notifyDataSetChanged();
//
//        final Track selectedTrack = mTracks.get(position);
//        final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();
//
//        // Check whether track in Queue or Not.
//        if (mPlayerService.getPlayMode() == PlayMode.MUSIC
//                && (tracksInQueue != null)
//                && (tracksInQueue.contains(selectedTrack))) {
//            final int trackPos = tracksInQueue.indexOf(selectedTrack);
//            mPlayerService.playFromPositionNew(trackPos);
//        } else {
//            mPlayerService.playNow(Collections.singletonList(selectedTrack));
//        }
//
//        mListener.onPlayTrackFromDetail();
//    }

    private void playSelectedSong(int position) {
        mMediaDetailListAdapter.setSelectedTrack(position);
        mMediaDetailListAdapter.notifyDataSetChanged();

        final Track selectedTrack = mTracks.get(position);
//        if (mPlayerService != null && mPlayerService.getPlayMode() == PlayMode.MUSIC && mPlayerService.isPlaying() &&
//                mPlayerService.getCurrentPlayingTrack().getId() == selectedTrack.getId()) {
//            if(mPlayerService.getState() == State.PLAYING) {
//                //Toast.makeText(getActivity(), "Already Playting track", Toast.LENGTH_SHORT).show();
//                CustomDialogLayout dialog = new CustomDialogLayout(getActivity(), DialogType.MESSAGE);
//                dialog.setMessage("Already Playing.");
//                dialog.show();
//            }else{
//                mPlayerService.play();
//                updateDetailPlayerUI();
//            }
//        }else{
            final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();

            // Check whether track in Queue or Not.
            if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                if(mPlayerService.isPlaying() &&
                        mPlayerService.getCurrentPlayingTrack().getId() == selectedTrack.getId()){
                    if(mPlayerService.getState() == State.PLAYING) {
                        //Toast.makeText(getActivity(), "Already Playting track", Toast.LENGTH_SHORT).show();
                        CustomDialogLayout dialog = new CustomDialogLayout(getActivity(), DialogType.MESSAGE);
                        dialog.setMessage("Already Playing.");
                        dialog.show();
                    }else{
                        mPlayerService.play();
                        updateDetailPlayerUI();
                    }
                } else if((tracksInQueue != null)
                    && (tracksInQueue.contains(selectedTrack))) {
                    final int trackPos = tracksInQueue.indexOf(selectedTrack);
                    mPlayerService.playFromPositionNew(trackPos);
                    updateDetailPlayerUI();
                } else {
                    mPlayerService.playNow(Collections.singletonList(selectedTrack));
                    updateDetailPlayerUI();
                }
            } else {
                mPlayerService.playNow(Collections.singletonList(selectedTrack));
                updateDetailPlayerUI();
            }

            mListener.onPlayTrackFromDetail();
//        }

    }

    @Override
    public void onStart(int operationId) {
        String msg = "";
        if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
            rlDetaillayout.setVisibility(View.GONE);
            if (mMediaItem == null) {
                msg = getString(R.string.msg_load_music_detail);
            } else {
                switch (mMediaItem.getMediaType()) {
                    case ALBUM:
                        if (mMediaItem.getAlbumName() != null && !mMediaItem.getAlbumName().isEmpty()) {
                            msg = getResources().getString(R.string.msg_load_music_detail_with_name, mMediaItem.getAlbumName());
                        } else if (mMediaItem.getTitle() != null && !mMediaItem.getTitle().isEmpty()) {
                            msg = getResources().getString(R.string.msg_load_music_detail_with_name, mMediaItem.getTitle());
                        }
                        break;
                    case PLAYLIST:
                        if (mMediaItem.getAlbumName() != null && !mMediaItem.getAlbumName().isEmpty()) {
                            msg = getResources().getString(R.string.msg_load_music_detail_with_name, mMediaItem.getAlbumName());
                        } else if (mMediaItem.getTitle() != null && !mMediaItem.getTitle().isEmpty()) {
                            msg = getResources().getString(R.string.msg_load_music_detail_with_name, mMediaItem.getTitle());
                        }
                        break;
                    default:
                        msg = getString(R.string.msg_load_music_detail);
                        break;
                }
            }


            mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, msg, null);
        } else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
//            if (mMediaItem.getTitle() != null && !mMediaItem.getTitle().isEmpty()) {
//                msg = getResources().getString(R.string.msg_add_favorites, mMediaItem.getTitle());
//            } else if (mMediaItem.getAlbumName() != null && !mMediaItem.getAlbumName().isEmpty()) {
//                msg = getResources().getString(R.string.msg_add_favorites, mMediaItem.getAlbumName());
//            }
//            mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, msg, null);
            btnFavorite.setVisibility(View.INVISIBLE);
            btnFavorite.setClickable(false);
            pbFavorite.setVisibility(View.VISIBLE);
        } else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
//            if (mMediaItem.getTitle() != null && !mMediaItem.getTitle().isEmpty()) {
//                msg = getResources().getString(R.string.msg_remove_favorites, mMediaItem.getTitle());
//            } else if (mMediaItem.getAlbumName() != null && !mMediaItem.getAlbumName().isEmpty()) {
//                msg = getResources().getString(R.string.msg_remove_favorites, mMediaItem.getAlbumName());
//            }
//            mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, msg, null);
            btnFavorite.setVisibility(View.INVISIBLE);
            btnFavorite.setClickable(false);
            pbFavorite.setVisibility(View.VISIBLE);
        } else if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS) {
            btnFavorite.setVisibility(View.INVISIBLE);
            btnFavorite.setClickable(false);
            pbFavorite.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {

        String strTitle = "";
        if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
            mMediaItem = (MediaItem) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);

            if (mMediaItem != null) {

                switch (mMediaItem.getMediaType()) {
                    case ALBUM:
                        if (mMediaItem.getAlbumName() != null && !mMediaItem.getAlbumName().isEmpty()) {
                            strTitle = mMediaItem.getAlbumName();
                        } else if (mMediaItem.getTitle() != null && !mMediaItem.getTitle().isEmpty()) {
                            strTitle = mMediaItem.getTitle();
                        }
                        break;

                    case PLAYLIST:
                        if (mMediaItem.getAlbumName() != null && !mMediaItem.getAlbumName().isEmpty()) {
                            strTitle = mMediaItem.getAlbumName();
                        } else if (mMediaItem.getTitle() != null && !mMediaItem.getTitle().isEmpty()) {
                            strTitle = mMediaItem.getTitle();
                        }
                        break;
                    default:
                        break;
                }

                tvTitle.setText(strTitle);

                mMediaSetDetails = (MediaSetDetails) responseObjects.get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);

                if (mMediaSetDetails != null) {
                    mTracks = mMediaSetDetails.getTracks();

                    if (mTracks != null && mTracks.size() > 0) {
                        mMediaDetailListAdapter = new MediaDetailListAdapter(getActivity(), mTracks);

                        if (isVisible()) {
                            lvMusics.setAdapter(mMediaDetailListAdapter);
                        }


                        lvMusics.post(new Runnable() {
                            public void run() {
								if(lvMusics!=null) {
	                                int last = lvMusics.getLastVisiblePosition();
    	                            if (last == lvMusics.getCount() - 1 && lvMusics.getChildAt(last).getBottom() <= lvMusics.getHeight()) {
    	                                llListViewProgressCtrl.setVisibility(View.GONE);
    	                            } else {
    	                                llListViewProgressCtrl.setVisibility(View.VISIBLE);
    	                            }
								}
                            }
                        });

                        // Check state of music player.
                        updateDetailPlayerUI();
                    } else if (isVisible()) {
                        lvMusics.setAdapter(null);
                    }

                    // Check Cache state.
                    handleCacheState();

                    String[] images = ImagesManager.getImagesUrlArray(mMediaSetDetails.getImagesUrlArray(), ImagesManager.HOME_MUSIC_TILE,DataManager.getDisplayDensityLabel());
                    //String[] images = ImagesManager.getImagesUrlArray(mMediaSetDetails.getImagesUrlArray(), ImagesManager.MUSIC_ART_SMALL, DataManager.getDisplayDensityLabel());

                    if (images.length == 1) { // Single track.
                        LinearLayout llPlaylistAvatar = (LinearLayout) mRootView.findViewById(R.id.ll_playlist_avatar);
                        llPlaylistAvatar.setVisibility(View.GONE);

                        final ImageView ivSingleAvatar = (ImageView) mRootView.findViewById(R.id.iv_single_avatar);
                        ivSingleAvatar.setVisibility(View.VISIBLE);
                        String imageURL = null;
                        if (images != null && images.length > 0) {
                            imageURL = images[0];
                        }
                        if(TextUtils.isEmpty(imageURL))
                            imageURL = ImagesManager.getMusicArtSmallImageUrl(mMediaSetDetails.getImagesUrlArray());

                        GlobalFunction.downloadImage(getActivity(), imageURL, ivSingleAvatar);
                    } else {
                        ImageView ivTrackOne = (ImageView) mRootView.findViewById(R.id.iv_track_avatar_one);
                        ImageView ivTrackTwo = (ImageView) mRootView.findViewById(R.id.iv_track_avatar_two);
                        ImageView ivTrackThree = (ImageView) mRootView.findViewById(R.id.iv_track_avatar_three);
                        ImageView ivTrackFour = (ImageView) mRootView.findViewById(R.id.iv_track_avatar_four);

                        if (images.length > 0)
                            GlobalFunction.downloadImage(getActivity(), images[0], ivTrackOne);
                        else
                            ivTrackOne.setImageBitmap(null);

                        if (images.length > 1)
                            GlobalFunction.downloadImage(getActivity(), images[1], ivTrackTwo);
                        else
                            ivTrackTwo.setImageBitmap(null);

                        if (images.length > 2)
                            GlobalFunction.downloadImage(getActivity(), images[2], ivTrackThree);
                        else
                            ivTrackThree.setImageBitmap(null);

                        if (images.length > 3)
                            GlobalFunction.downloadImage(getActivity(), images[3], ivTrackFour);
                        else
                            ivTrackThree.setImageBitmap(null);
                    }
                }
            } else if (isVisible()) {
                lvMusics.setAdapter(null);
            }

            if (mMediaItem == null || mTracks == null || mTracks.size() == 0) { // Back to List music screen if no tracks found in Album/Playlist.
                GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, getString(R.string.msg_load_music_detail_empty, strTitle), new IDialogListener() {

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
            } else { // Continue to request for List of Favorites
                //  mDataManager.getFavorites(getActivity(), MediaType.ALBUM, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);
            }
            // Visible Detail layout and hide dialog after finish initial steps.
            rlDetaillayout.setVisibility(View.VISIBLE);
            mDialog.hide();
        } else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
            BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects.get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);

            // has the item been added from favorites.
            if (addToFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS) {
                GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, addToFavoriteResponse.getMessage(), null);
                btnFavorite.setImageResource(R.drawable.carmode_btn_favorite_added);
                isAddedFavorite = true;
            }

            btnFavorite.setVisibility(View.VISIBLE);
            btnFavorite.setClickable(true);
            pbFavorite.setVisibility(View.GONE);
        } else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
            BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects.get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);

            // has the item been removed from favorites.
            if (removeFromFavoriteResponse.getCode() == CODE_FAVORITE_SUCCESS) {
                GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, removeFromFavoriteResponse.getMessage(), null);
                btnFavorite.setImageResource(R.drawable.carmode_btn_favorites);
                isAddedFavorite = false;
            }

            btnFavorite.setVisibility(View.VISIBLE);
            btnFavorite.setClickable(true);
            pbFavorite.setVisibility(View.GONE);
        } else if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS) {
            ProfileFavoriteMediaItems profileFavoriteMediaItems = (ProfileFavoriteMediaItems) responseObjects.get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS);
            if (profileFavoriteMediaItems != null) {
                List<MediaItem> listItems = profileFavoriteMediaItems.mediaItems;

                if ((listItems != null) && (listItems.size() > 0)) {
                    for (int i = 0; i < listItems.size(); i++) {
                        final MediaItem item = listItems.get(i);

                        if (item.getId() == mMediaItem.getId()) { // Found this item in Favorite List.
                            btnFavorite.setImageResource(R.drawable.carmode_btn_favorite_added);
                            isAddedFavorite = true;
                            break;
                        }
                    }
                }
            }else{
                btnFavorite.setImageResource(R.drawable.carmode_btn_favorites);
                isAddedFavorite = false;
            }

            btnFavorite.setVisibility(View.VISIBLE);
            btnFavorite.setClickable(true);
            pbFavorite.setVisibility(View.GONE);
        }
    }

    public void updateDetailPlayerUI() {
        if (mPlayerService != null
                && (mPlayerService.getCurrentPlayingTrack() != null)
                && (!mPlayerService.isQueueEmpty())) {
            final Track curTrack = mPlayerService.getCurrentPlayingTrack();

            for (Track track : mTracks) {
                if (curTrack.getId() == track.getId()) {
                    if (mPlayerService.isLoading()) {
                        pbPlayerLoad.setVisibility(View.VISIBLE);
                        btnPlayPause.setImageResource(R.drawable.carmode_btn_play_large);
                    } else if (mPlayerService.isPlaying()) {
                        pbPlayerLoad.setVisibility(View.GONE);

                        if (mPlayerService.getState() == State.PAUSED
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

    @Override
    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
        mDialog.hide();

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
        DataBase.CacheState cacheState = DataBase.CacheState.NOT_CACHED;

        switch (mMediaItem.getMediaType()) {
            case ALBUM:
                cacheState = DBOHandler.getAlbumCacheState(getActivity(), "" + mMediaItem.getId());
                if (cacheState == DataBase.CacheState.NOT_CACHED && (DBOHandler.getAlbumCachedCount(getActivity(), "" + mMediaItem.getId()) > 0)) {
                    cacheState = DataBase.CacheState.CACHED;
                }
                break;

            case PLAYLIST:
                cacheState = DBOHandler.getPlaylistCacheState(getActivity(), "" + mMediaItem.getId());
                if (cacheState == DataBase.CacheState.NOT_CACHED && (DBOHandler.getPlaylistCachedCount(getActivity(), "" + mMediaItem.getId()) > 0)) {
                    cacheState = DataBase.CacheState.CACHED;
                }
                break;
        }

        switch (cacheState) {
            case CACHED:
                btnDownload.setImageResource(R.drawable.carmode_cache_state_cached);
                break;

            case NOT_CACHED:
                btnDownload.setImageResource(R.drawable.carmode_btn_download);
                break;
        }

        // Update list of Tracks.
        if (mMediaDetailListAdapter != null) {
            mMediaDetailListAdapter.notifyDataSetChanged();
        }
    }

    public void vHandleMusicDetailClicks(View v) {
        int view_id = v.getId();
        String msg = "";

        switch (view_id) {
            case R.id.btn_back_main_menu:
                getFragmentManager().popBackStack();
                break;

            case R.id.btn_universal_player:
                mListener.onGoToMusicPlayer();
                break;

            case R.id.btn_favorite:
                if (isAddedFavorite) { // Already in Favorite list.
                    mDataManager.removeFromFavorites(String.valueOf(mMediaItem.getId()), mMediaItem.getMediaType().toString(), this);
                } else {
                    final String mediaTypeFav = (mMediaItem.getMediaType() == MediaType.TRACK) ? MEDIA_TYPE_SONG : mMediaItem.getMediaType().toString();
                    mDataManager.addToFavorites(String.valueOf(mMediaItem.getId()), mediaTypeFav, this);
                }
                break;

            case R.id.btn_player_start_pause:
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

            case R.id.btn_download:
                if (mMediaItem.getTitle() != null && !mMediaItem.getTitle().isEmpty()) {
                    msg = getResources().getString(R.string.msg_save_offline, mMediaItem.getTitle());
                } else if (mMediaItem.getAlbumName() != null && !mMediaItem.getAlbumName().isEmpty()) {
                    msg = getResources().getString(R.string.msg_save_offline, mMediaItem.getAlbumName());
                }

                if (mMediaItem.getMediaType() == MediaType.ALBUM || mMediaItem.getMediaType() == MediaType.PLAYLIST) {
                    GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, msg, null);

                    if (MediaCachingTaskNew.isEnabled) {
                        //                        mDataManager.getMediaDetails(mMediaItem, PlayerOption.OPTION_SAVE_OFFLINE, this);
                        for (Track track : mTracks) {
                            track.setTag(mMediaItem);
                        }
                        CacheManager.saveAllTracksOfflineAction(getActivity(), mTracks);
                    } else {
                        CacheManager.saveOfflineAction(getActivity(), mMediaItem, null);
                    }

                }

                break;

            case R.id.btn_add_to_queue:
                if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
                    msg = getString(R.string.msg_adding_all_to_queue, "playlist of " + mMediaItem.getTitle());
                } else if (mMediaItem.getMediaType() == MediaType.ALBUM) {
                    msg = getString(R.string.msg_adding_all_to_queue, "album of " + mMediaItem.getAlbumName());
                }
                final CustomDialogLayout tempDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, msg, null);

                final List<Track> tracksNeedToAdd = new ArrayList<Track>();
                final List<Track> listTracksInQueue = mPlayerService.getPlayingQueue();

                // Track in Queue process.
                for (int i = 0; i < mTracks.size(); i++) {
                    final Track trackInDetail = mTracks.get(i);
                    boolean isInQueue = false;

                    // Check whether this track is in Queue or not.
                    for (int j = 0; j < listTracksInQueue.size(); j++) {
                        final Track trackInQueue = listTracksInQueue.get(j);
                        if (trackInQueue.getId() == trackInDetail.getId()) {
                            isInQueue = true;
                            break;
                        }
                    }

                    if (!isInQueue) { // This track is not in Queue.
                        if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
                            trackInDetail.setTag(mMediaItem);
                        } else if (mMediaItem.getMediaType() == MediaType.ALBUM) {
                            trackInDetail.setAlbumId(mMediaSetDetails.getContentId());
                        }
                        tracksNeedToAdd.add(trackInDetail);
                    }
                }

                tempDialog.hide(); // Hide before showing Complete dialog.
                mListener.onAddTrackToQueue(tracksNeedToAdd);

                break;
            case R.id.btn_scroll_up:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() - 1);
                lvMusics.setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_scroll_down:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() + 1);
                lvMusics.setSelection(sbListViewProgress.getProgress());
                break;
            default:
                break;
        }
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

        if (mPlayerService != null) {
            mPlayerService.unregisterPlayerStateListener(IPlayerStateListener);
            mPlayerService.removeCarModeListener(mCarModeUIUpdate);
        }

        super.onStop();
    }


    public interface IMusicDetailListener {
        void onGoToMusicPlayer();

        void onAddTrackToQueue(List<Track> tracks);

        void onPlayTrackFromDetail();
    }
}
