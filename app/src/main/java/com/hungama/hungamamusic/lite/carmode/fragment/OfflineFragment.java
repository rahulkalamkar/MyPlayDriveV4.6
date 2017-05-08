package com.hungama.hungamamusic.lite.carmode.fragment;

import android.app.Activity;
import android.content.res.Resources;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicListAdapter;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicListAdapter.MediaItemEvents;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.hungamamusic.lite.carmode.view.VerticalSeekBar;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.CarModeHomeActivity;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.images.AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfflineFragment extends ListFragment {

    public final static String TAG = OfflineFragment.class.getSimpleName();

    // UI elements
    private LinearLayout llMusicOptions;
    private VerticalSeekBar sbListViewProgress;
    private LinearLayout llListViewProgressControl;
    private ImageButton btnBack;
    private Button btnGoOnOffLine;
    private Button btnPlayNow;

    private MusicListAdapter mOfflineListAdapter;
    private IOfflineListener mListener;
    private boolean isScrolling = true;
    private List<MediaItem> mListOfflineMedia;
    private MediaItem mSelectedMedia;
    private PlayerService mPlayerService;
    private TextView txtempty;

    public static OfflineFragment newInstance(IOfflineListener listener) {
        final OfflineFragment fragment = new OfflineFragment();
        fragment.setListener(listener);
        return fragment;
    }

    public void setListener(IOfflineListener listener) {
        this.mListener = listener;
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mRootView = inflater.inflate(R.layout.carmode_fragment_offline, container, false);

        llMusicOptions = (LinearLayout) mRootView.findViewById(R.id.ll_music_options);
        sbListViewProgress = (VerticalSeekBar) mRootView.findViewById(R.id.sb_listview_progress);
        llListViewProgressControl = (LinearLayout) mRootView.findViewById(R.id.ll_listview_progress_control);
        btnBack = (ImageButton) mRootView.findViewById(R.id.btn_back_main_menu);
        btnGoOnOffLine = (Button) mRootView.findViewById(R.id.btn_go_on_off_line);
        btnPlayNow = (Button) mRootView.findViewById(R.id.btn_play_now);
        txtempty=(TextView) mRootView.findViewById(R.id.empty);
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        CarModeHomeActivity.setEnableInternetCheck(false);
        mPlayerService = PlayerService.service;

        final DataManager dataManager = DataManager.getInstance(getActivity());
        final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();
        appConfig.setSaveOfflineMode(true);
        btnGoOnOffLine.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.carmode_go_online), null, null);
        btnGoOnOffLine.setText(R.string.caching_text_popup_title_go_online);
        btnBack.setVisibility(View.GONE);

        // Reset Player queue.
        if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
            mPlayerService.stop();
        }

        vInitUI();
        vLoadOfflineMusics();
    }

    private void vLoadOfflineMusics() {
        new LoadOfflineTask(getActivity()).execute();
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
                if (fromUser) {
                    getListView().setSelection(progress);
                }
            }
        });


        getListView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                sbListViewProgress.setProgress(firstVisibleItem);
            }
        });
    }

    public void vHandleOfflineClicks(View v) {
        final int view_id = v.getId();
        Track track;
        List<Track> tmpTracks;

        switch (view_id) {
            case R.id.btn_back_main_menu:
                getFragmentManager().popBackStack();
                CarModeHomeActivity.setEnableInternetCheck(true);
                break;

            case R.id.btn_universal_player:
                resetMusicLayout();
                mListener.onGoToMusicPlayer();
                break;
            case R.id.btn_scroll_up:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() - 1);
                getListView().setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_scroll_down:
                sbListViewProgress.setProgress(sbListViewProgress.getProgress() + 1);
                getListView().setSelection(sbListViewProgress.getProgress());
                break;

            case R.id.btn_play_all:
                handlePlayAllOffline();
                break;

            case R.id.btn_play_now:
                if (mPlayerService != null) {

                    boolean allowToPlay = true;
                    if (mPlayerService.getCurrentPlayingTrack() == null
                            || mPlayerService.getState() == PlayerService.State.IDLE
                            || mPlayerService.getState() == PlayerService.State.STOPPED
                            || mPlayerService.getState() == PlayerService.State.PAUSED) { // Play Now if Player service is stopped or not started yet.
                        allowToPlay = true;
                    } else {

                        allowToPlay = mPlayerService.getCurrentPlayingTrack().getId() != mSelectedMedia.getId(); // Play Now if not same as track

                    }

                    if (!allowToPlay && (mPlayerService.isLoading() || mPlayerService.isPlaying()) && mPlayerService.getState() == PlayerService.State.PLAYING) {
                        mPlayerService.pause();
                        // Update button.
                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null, null);
                        btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
                    }

                    if(allowToPlay){

                        if (mSelectedMedia.getMediaType() == MediaType.TRACK && allowToPlay) {
                            final Track selectedTrack = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(), mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(), mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());
                            // Check whether track in Queue or Not.
                            /*if (tracksInQueue.contains(selectedTrack)) {
                                final int trackPos = tracksInQueue.indexOf(selectedTrack);
                                mPlayerService.playFromPosition(trackPos);
                            } else {
                                mPlayerService.playNow(Collections.singletonList(selectedTrack));
                            }*/
                            playNowNew(Collections.singletonList(selectedTrack), null, null);

                            mOfflineListAdapter.vHandleCollapseOptions();

                            btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                            btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);
                        }

                        mListener.onPlayNow();
//                        mOfflineListAdapter.vHandleCollapseOptions();
                        mOfflineListAdapter.notifyDataSetChanged();
                    }
                }
                /*if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
                    mPlayerService.stop();
                }

                track = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(), mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(), mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());
                final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();

                // Check whether track in Queue or Not.
                boolean isTrackInQueue = false;
                for (Track tempTrack : tracksInQueue) {
                    if (tempTrack.getId() == track.getId()) {
                        isTrackInQueue = true;
                        break;
                    }
                }

                if (isTrackInQueue) {
                    final int trackPos = tracksInQueue.indexOf(track);
                    mPlayerService.playFromPosition(trackPos);
                } else {
                    mPlayerService.playNow(Collections.singletonList(track));
                }

                btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);

                mListener.onPlayNow();

                // Close Music option layout.
                mOfflineListAdapter.vHandleCollapseOptions();
                mOfflineListAdapter.notifyDataSetChanged();*/

                break;

            case R.id.btn_add_to_queue:
                if (mSelectedMedia.getMediaContentType() == MediaContentType.MUSIC) {
                    if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
                        final List<Track> queue = mPlayerService.getPlayingQueue();
                        track = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(), mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(), mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());
                        tmpTracks = new ArrayList<Track>();

                        if (queue.contains(track)) {
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.main_player_bar_message_song_already_in_queue), null);
                        } else {
                            tmpTracks.add(track);
                            mListener.onAddTrackToQueue(tmpTracks);
                        }
                    }
                }

                break;

            case R.id.btn_go_on_off_line:
                final DataManager dataManager = DataManager.getInstance(getActivity());
                final ApplicationConfigurations appConfig = dataManager.getApplicationConfigurations();
                if (Utils.isConnected()) { // Network is active.
                    CarModeHomeActivity.setEnableInternetCheck(true);
                    appConfig.setSaveOfflineMode(false);
                    appConfig.setSaveOfflineAutoMode(false);
                    mListener.onGoOnline();
                    getFragmentManager().popBackStack();
                } else { // Network is inactive.
                    GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.go_online_network_error), null);
                }

                break;

            case R.id.btn_delete_offline:
                GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.CONFIRMATION, getString(R.string.message_delete_track_from_cache), new CustomDialogLayout.IDialogListener() {
                    @Override
                    public void onPositiveBtnClick() {
                        final String path = DBOHandler.getTrackPathById(getActivity(), "" + mSelectedMedia.getId());
                        boolean isTracksDeleted = false;
                        if (path != null && path.length() > 0) {
                            File file = new File(path);
                            if (file.exists() && file.delete()) {
                                isTracksDeleted = DBOHandler.deleteCachedTrack(getActivity(), "" + mSelectedMedia.getId());
                            }

                            if (isTracksDeleted) {
                                GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.message_track_deleted), null);
                                mListOfflineMedia.remove(mSelectedMedia);

                                // Close Music option layout.
                                mOfflineListAdapter.vHandleCollapseOptions();
                                mOfflineListAdapter.notifyDataSetChanged();

                                // Stop music if current song is playing.
                                final Track curPlayingSong = mPlayerService.getCurrentPlayingTrack();
                                if (curPlayingSong != null
                                        && curPlayingSong.getId() == mSelectedMedia.getId()) {
                                    mPlayerService.stop();
                                }

                                // Remove deleted song from Playing queue.
                                final List<Track> listPlayQueue = mPlayerService.getPlayingQueue();
                                if (!Utils.isListEmpty(listPlayQueue)) {
                                    for (int pos = 0; pos < listPlayQueue.size(); pos++) {
                                        final Track track = listPlayQueue.get(pos);
                                        if (track.getId() == mSelectedMedia.getId()) {
                                            mPlayerService.removeFrom(pos);
                                            break;
                                        }
                                    }
                                }


                            } else {
                                GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.message_track_delete_failed), null);
                            }
                        }
                    }

                    @Override
                    public void onNegativeBtnClick() {
                    }

                    @Override
                    public void onNegativeBtnClick(CustomDialogLayout layout) {
                        layout.hide();
                    }

                    @Override
                    public void onMessageAction() {
                    }
                });
                break;

            default:
                break;
        }
    }

    private void handlePlayAllOffline() {
        if (mListOfflineMedia != null && mListOfflineMedia.size() > 0) {
            final List<Track> tracks = new ArrayList<Track>();
            for (MediaItem mediaItem : mListOfflineMedia) {
                final DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(getActivity(), "" + mediaItem.getId());
                if (cacheState == DataBase.CacheState.CACHED) {
                    final Track track = new Track(mediaItem.getId(), mediaItem.getTitle(), mediaItem.getAlbumName(), mediaItem.getArtistName(), mediaItem.getImageUrl(), mediaItem.getBigImageUrl(), mediaItem.getImages(), mediaItem.getAlbumId());
                    tracks.add(track);
                }
            }

            if (tracks.size() > 0) {
                mListener.onPlayNow(); // Change Offline queue if from Online.
                if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
                    mPlayerService.stop();
                }

                // Close Music option layout.
                mOfflineListAdapter.vHandleCollapseOptions();
                mOfflineListAdapter.notifyDataSetChanged();

//                mListener.onAddTrackToQueue(tracks);

                final List<Track> trackNotInQueue = new ArrayList<>();
                if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                    final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();
                    if (Utils.isListEmpty(tracksInQueue)) {
                        trackNotInQueue.addAll(tracks);
                    } else {
                        for (Track tempTrack : tracks) {
                            if (!tracksInQueue.contains(tempTrack)) {
                                trackNotInQueue.add(tempTrack);
                            }
                        }
                    }


                } else {
                    trackNotInQueue.addAll(tracks);
                }



                mPlayerService.playNow(trackNotInQueue);
            }
        }
    }

    private void handleOfflineSwitch(boolean isOfflineEnabled, ApplicationConfigurations appConfig) {
//        if (isOfflineEnabled) {
//            appConfig.setSaveOfflineMode(true);
//            btnBack.setEnabled(false);
//            btnBack.setVisibility(View.GONE);
//            btnGoOnOffLine.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.carmode_go_online), null, null);
//            btnGoOnOffLine.setText(R.string.caching_text_popup_title_go_online);

        // Reset Player queue.
//            mPlayerService.stop();
//            mPlayerService.setPlayingQueue(new PlayingQueue(null, 0, mPlayerService));

//            final Track curPlayingTrack = mPlayerService.getCurrentPlayingTrack();
//            if (curPlayingTrack != null && !curPlayingTrack.isCached() && mPlayerService.isPlaying()) {
//                mPlayerService.stop();
//            }
//        }
//        else {
//        HomeActivity.setEnableInternetCheck(true);
//        appConfig.setSaveOfflineMode(false);
//        mListener.onGoOnline();
//            btnBack.setEnabled(true);
//            btnBack.setVisibility(View.VISIBLE);
//            btnGoOnOffLine.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.carmode_go_offline), null, null);
//            btnGoOnOffLine.setText(R.string.main_actionbar_go_offline);

//        getFragmentManager().popBackStack();
//        }
    }

    // Reset layout before calling next fragment.
    public void resetMusicLayout() {
        isScrolling = true;
        llMusicOptions.setVisibility(View.GONE);
        llListViewProgressControl.setVisibility(View.VISIBLE);

        if (mOfflineListAdapter != null) {
            mOfflineListAdapter.setSelectedPos(-1);
            mOfflineListAdapter.setMediaMenuExpanded(false);
            mOfflineListAdapter.notifyDataSetChanged();
        }
    }

    private void expandMusicOptions() {
        // Update layout.
        final Track currentPlaying = mPlayerService.getCurrentPlayingTrack();
        if (mPlayerService.isPlaying()
                && currentPlaying != null
                && currentPlaying.getId() == mSelectedMedia.getId()) {
            btnPlayNow.setText(R.string.player_queue_text_now_playing);
            btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
        } else {
            btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_play), null, null, null);
            btnPlayNow.setText(R.string.media_details_custom_dialog_long_click_play_now);
        }

        llMusicOptions.setVisibility(View.VISIBLE);
        llListViewProgressControl.setVisibility(View.GONE);

    }

    private void collapseMusicOptions() {
        llMusicOptions.setVisibility(View.GONE);
        llListViewProgressControl.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (getActivity() != null) {
            ((CarModeHomeActivity) getActivity()).hideLoadDialog();
        }
    }

    public interface IOfflineListener {
        void onAddTrackToQueue(List<Track> listTracks);

        void onGoToMusicPlayer();

        void onPlayNow();

        void onGoOnline();
    }

    private class LoadOfflineTask extends AsyncTask<Void, Void, Void> {

        private Activity mActivity;

        public LoadOfflineTask(Activity activity) {
            this.mActivity = activity;
            if (getActivity() != null) {
                ((CarModeHomeActivity) getActivity()).showLoadDialog();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                if (CacheManager.isProUser(this.mActivity)) {
                    mListOfflineMedia = DBOHandler.getAllCachedTracks(this.mActivity);
                } else {
                    mListOfflineMedia = DBOHandler.getAllOfflineTracksForFreeUser(this.mActivity);
                }

                if (mListOfflineMedia == null) {
                    cancel(true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();

            if (getActivity() != null) {
                ((CarModeHomeActivity) getActivity()).hideLoadDialog();
            }

            GlobalFunction.showMessageDialog(mActivity, CustomDialogLayout.DialogType.MESSAGE_FORCE_CLOSE, mActivity.getResources().getString(R.string.msg_load_offline_fail), null);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (getActivity() != null) {
                ((CarModeHomeActivity) getActivity()).hideLoadDialog();
            }

            if(!(mListOfflineMedia.size()>0))
                txtempty.setVisibility(View.VISIBLE);

            if (!isCancelled() && isVisible()) {
                sbListViewProgress.setMax(mListOfflineMedia.size());
                mOfflineListAdapter = new MusicListAdapter(getActivity(), mListOfflineMedia);
                mOfflineListAdapter.setOfflineMode(true);
                mOfflineListAdapter.setMediaItemEvents(new MediaItemEvents() {

                    @Override
                    public void onExpandMenu(int mediaItemPos) {
                        mSelectedMedia = mListOfflineMedia.get(mediaItemPos);
                        expandMusicOptions();
                        isScrolling = false; // Flag for disable scrolling.
                    }

                    @Override
                    public void onCollapseMenu() {
                        collapseMusicOptions();
                        isScrolling = true; // Flag for disable scrolling.
                    }
                });

                setListAdapter(mOfflineListAdapter);

                getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        mSelectedMedia = (MediaItem) parent.getItemAtPosition(position);

                        if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
                            mPlayerService.stop();
                        }

                        Track track = new Track(mSelectedMedia.getId(), mSelectedMedia.getTitle(), mSelectedMedia.getAlbumName(), mSelectedMedia.getArtistName(), mSelectedMedia.getImageUrl(), mSelectedMedia.getBigImageUrl(), mSelectedMedia.getImages(), mSelectedMedia.getAlbumId());
                        final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();

                        // Check whether track in Queue or Not.
                        boolean isTrackInQueue = false;
                        for (Track tempTrack : tracksInQueue) {
                            if (tempTrack.getId() == track.getId()) {
                                isTrackInQueue = true;
                                break;
                            }
                        }

                        if (isTrackInQueue) {
                            final int trackPos = tracksInQueue.indexOf(track);
                            mPlayerService.playFromPosition(trackPos);
                        } else {
                            mPlayerService.playNow(Collections.singletonList(track));
                        }

                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause_small), null, null, null);
                        btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);

                        mListener.onPlayNow();

                        // Close Music option layout.
                        mOfflineListAdapter.vHandleCollapseOptions();
                        mOfflineListAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    public void playNowNew(final List<Track> tracks,
                           final String flurryEventName, final String flurrySourceSection) {
        Resources resources = getResources();
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
                            }else if(mPlayerService.isPlaying() && mPlayerService.getState() == PlayerService.State.PLAYING){
                                //Utils.makeText(getActivity(),Utils.getMultilanguageText(getActivity(), getString(R.string.queue_bottom_text_now_playing)), Toast.LENGTH_SHORT).show();
                                GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.queue_bottom_text_now_playing), null);
                            }
                        }
                        else if (trackPosition == -1) {
                            mPlayerService.playNow(tracks);
                            //helperPlayNow(tracks);
                        }
                        else {
                            mPlayerService.playNowFromPosition(
                                    tracks, trackPosition);
                        }

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
                            //Utils.makeText(getActivity(),Utils.getMultilanguageText(getActivity(), getString(R.string.queue_bottom_text_now_playing)), Toast.LENGTH_SHORT).show();
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.queue_bottom_text_now_playing), null);
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
                            // Utils.makeText(getActivity(),Utils.getMultilanguageText(getActivity(), getString(R.string.queue_bottom_text_now_playing)), Toast.LENGTH_SHORT).show();
                            GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, getString(R.string.queue_bottom_text_now_playing), null);
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

//                        Utils.makeText(getActivity(), mMessageSongsToQueue,
//                                Toast.LENGTH_SHORT).show();
                        GlobalFunction.showMessageDialog(getActivity(), CustomDialogLayout.DialogType.MESSAGE, mMessageSongsToQueue, null);
                    }
                }
            } catch (Exception e) {
                Logger.e(getClass().getName() + ":252", e.toString());
            }
        }
    }

}
