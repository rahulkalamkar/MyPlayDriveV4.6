package com.hungama.hungamamusic.lite.carmode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicListAdapter;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout.DialogType;
import com.hungama.hungamamusic.lite.carmode.view.VerticalSeekBar;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.playlist.PlaylistManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlaylistFragment extends ListFragment implements CommunicationOperationListener {

    public static final String TAG = PlaylistFragment.class.getSimpleName();
    public PlayerService mPlayerService = null;
    // UI elements
    private View mRootView;
    private LinearLayout llMusicOptions;
    private LinearLayout llListViewProgressControl;
    private CustomDialogLayout mDialog;
    private VerticalSeekBar sbListViewProgress;
    private Button btnDownload;
    private Button btnPlayNow;
    private ProgressBar pbLoadMedia;
    private DataManager mDataManager;
    private PlaylistManager mPlaylistManager;
    private List<Playlist> mListPlaylists;
    private List<MediaItem> mListMediaItems;
    private MusicListAdapter mPlaylistAdapter;
    private int mScreenWidthInPxl = 0;
    private int tmpSavedScrollerWidth = 0;
    private boolean isScrolling = true;
    //	private MediaItem mSelectedPlaylist;
    private int mSelectedPos = -1;
    private IPlaylistListener mListener;
    private boolean isExpanded;
    private boolean isSaveOffline;

    public static final PlaylistFragment newInstance(IPlaylistListener listener) {
        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setListener(listener);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mPlayerService = PlayerService.service;
    }

    public void setListener(IPlaylistListener listener) {
        this.mListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.carmode_fragment_playlists, container, false);

        llMusicOptions = (LinearLayout) mRootView.findViewById(R.id.ll_music_options);
        llListViewProgressControl = (LinearLayout) mRootView.findViewById(R.id.ll_listview_progress_control);
        sbListViewProgress = (VerticalSeekBar) mRootView.findViewById(R.id.sb_listview_progress);
        btnDownload = (Button) mRootView.findViewById(R.id.btn_download);
        btnPlayNow = (Button) mRootView.findViewById(R.id.btn_play_now);
        pbLoadMedia = (ProgressBar) mRootView.findViewById(R.id.pb_load_media);

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        vInitUI();

        mDataManager = DataManager.getInstance(getActivity());
        mPlaylistManager = PlaylistManager.getInstance(getActivity());
        loadPlaylists();
    }

    private void vInitUI() {
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

        // Listview's seekbar
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
    }

    private void loadPlaylists() {
        Map<Long, Playlist> map = mDataManager.getStoredPlaylists();

        // Convert from Map<Long, Playlist> to List<Itemable>
        if (map != null && map.size() > 0) {
            mListMediaItems = new ArrayList<MediaItem>();
            mListPlaylists = new ArrayList<Playlist>();

            for (Map.Entry<Long, Playlist> p : map.entrySet()) {
                final Playlist playlist = p.getValue();
                final MediaItem mediaItem = new MediaItem(playlist.getId(), playlist.getName(), null, null, null, null, MediaType.PLAYLIST.name()
                        .toLowerCase(), 0, 0);
                mediaItem.setMediaType(MediaType.PLAYLIST);
                mediaItem.setMusicTrackCount(((Playlist) playlist).getNumberOfTracks());
                mediaItem.setMediaContentType(MediaContentType.MUSIC);

                mListPlaylists.add(playlist);
                mListMediaItems.add(mediaItem);
            }

            mPlaylistAdapter = new MusicListAdapter(getActivity(), mListMediaItems);
            mPlaylistAdapter.setMediaItemEvents(new MusicListAdapter.MediaItemEvents() {

                @Override
                public void onExpandMenu(int mediaItemPos) {
                    mSelectedPos = mediaItemPos;
                    isScrolling = false; // Flag for disable scrolling.
                    isExpanded = true;
                    expandMusicOptions();
                }

                @Override
                public void onCollapseMenu() {
                    isScrolling = true; // Flag for disable scrolling.
                    isExpanded = false;
                    collapseMusicOptions();
                }
            });
            setListAdapter(mPlaylistAdapter);

            getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final Playlist playlist = mListPlaylists.get(position);
                    final List<Track> mTracks = mPlaylistManager.getTracksListByPlaylist(playlist);
                    mListener.gotoPlaylistDetail(playlist, mTracks);
                }
            });
        }
    }

    public void vHandlePlaylistClicks(View v) {
        final int view_id = v.getId();
        Playlist playlist;
        MediaItem selectedMedia;
        List<Track> tracks;

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
                isScrolling = true;
                playlist = mListPlaylists.get(mSelectedPos);
                tracks = mPlaylistManager.getTracksListByPlaylist(playlist);
                mListener.gotoPlaylistDetail(playlist, tracks);
                break;

            case R.id.btn_add_to_queue:
                playlist = mListPlaylists.get(mSelectedPos);
                tracks = mPlaylistManager.getTracksListByPlaylist(playlist);
                mListener.onAddTrackToQueue(tracks);
                break;

            case R.id.btn_download:
                selectedMedia = mListMediaItems.get(mSelectedPos);
                playlist = mListPlaylists.get(mSelectedPos);
                tracks = mPlaylistManager.getTracksListByPlaylist(playlist);

                String msg = "";
                if (selectedMedia.getTitle() != null && !selectedMedia.getTitle().isEmpty()) {
                    msg = getResources().getString(R.string.msg_save_offline, selectedMedia.getTitle());
                } else if (selectedMedia.getAlbumName() != null && !selectedMedia.getAlbumName().isEmpty()) {
                    msg = getResources().getString(R.string.msg_save_offline, selectedMedia.getAlbumName());
                }

                GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, msg, null);

                if (tracks != null && tracks.size() > 0) {
                    CacheManager.saveAllTracksOfflineAction(getActivity(), tracks);
                }

                break;

            case R.id.btn_play_now:
                if (mPlayerService != null) {

                    if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
                        mPlayerService.stop();

                        // Update button.
                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null, null);
                        btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
                    }


                    playlist = mListPlaylists.get(mSelectedPos);
                    tracks = mPlaylistManager.getTracksListByPlaylist(playlist);

                    // Play list of songs.
                    mPlayerService.playNow(tracks);
                    mPlaylistAdapter.vHandleCollapseOptions();

                    btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                    btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);

                    // Update Player's tracks.
                    mListener.onPlayNow();
                }

                break;
        }
    }

    // Reset layout before calling next fragment.
    public void resetMusicLayout() {
        isScrolling = true;
        llMusicOptions.setVisibility(View.GONE);
        llListViewProgressControl.setVisibility(View.VISIBLE);

        if (mPlaylistAdapter != null) {
            mPlaylistAdapter.setSelectedPos(-1);
            mPlaylistAdapter.setMediaMenuExpanded(false);
            mPlaylistAdapter.notifyDataSetChanged();
        }
    }

    public void handleCacheState() {
        if (btnDownload != null && isExpanded) {
            boolean isPlaylistDownloaded = false;
            final Playlist playlist = mListPlaylists.get(mSelectedPos);
            final List<Track> tracks = mPlaylistManager.getTracksListByPlaylist(playlist);
            for (Track track : tracks) {
                final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + track.getId());
                if (cacheState == DataBase.CacheState.CACHED) {
                    isPlaylistDownloaded = true;
                    break;
                }
            }

            if (isPlaylistDownloaded) {
                btnDownload.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_cache_state_cached), null, null, null);
                btnDownload.setText(R.string.caching_text_play_offline);
            } else {
                btnDownload.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_download), null, null, null);
                btnDownload.setText(R.string.btn_download);
            }
        }
    }

    private void updatePlayNowStates() {
        final Playlist playlist = mListPlaylists.get(mSelectedPos);
        final List<Track> tracks = mPlaylistManager.getTracksListByPlaylist(playlist);

        boolean isPlaying = false;

        // Start checking songs in Playlist.
        pbLoadMedia.setVisibility(View.VISIBLE);
        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        if (!mPlayerService.isQueueEmpty()
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
        }

        if (!isPlaying) { // Current Playing song is not belong to this Playlist.
            btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null, null);
            btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
        }

        pbLoadMedia.setVisibility(View.GONE);
    }

    private void expandMusicOptions() {
        handleCacheState();
        updatePlayNowStates();
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
//
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
        MediaItem selectedMedia;

        switch (operationId) {
            case OperationDefinition.Hungama.OperationId.MEDIA_DETAILS:
                if (isSaveOffline) {
                    selectedMedia = mListMediaItems.get(mSelectedPos);
                    if (selectedMedia.getTitle() != null && !selectedMedia.getTitle().isEmpty()) {
                        msg = getResources().getString(R.string.msg_save_offline, selectedMedia.getTitle());
                    } else if (selectedMedia.getAlbumName() != null && !selectedMedia.getAlbumName().isEmpty()) {
                        msg = getResources().getString(R.string.msg_save_offline, selectedMedia.getAlbumName());
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
                    }
                }
                break;
        }
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
        if (mDialog != null) {
            mDialog.hide(); // Dismiss dialog.
        }

        GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE, errorMessage, new CustomDialogLayout.IDialogListener() {

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

    public interface IPlaylistListener {
        void onAddTrackToQueue(List<Track> listTracks);

        void gotoPlaylistDetail(Playlist playlist, List<Track> listTracksOfPlaylist);

        void onGoToMusicPlayer();

        void onPlayNow();
    }
}
