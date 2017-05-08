package com.hungama.hungamamusic.lite.carmode.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicListAdapter;
import com.hungama.hungamamusic.lite.carmode.adapters.MusicListAdapter.MediaItemEvents;
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
import com.hungama.myplay.activity.data.dao.hungama.SearchResponse;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SearchKeyboardOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.CarModeHomeActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SearchResultListFragment extends ListFragment implements CommunicationOperationListener {

    public final static String TAG = SearchResultListFragment.class.getSimpleName();

    private static final int CODE_FAVORITE_SUCCESS = 1;
    private static final String MEDIA_TYPE_SONG = "song";

    private static final int RESULT_MINIMUM_INDEX = 1;
    private static final int RESULT_TO_PRESENT = 30;
    private static final int TRIGGER_LOAD_NO = 5;
    private static final String SEARCH_FILTER_TYPE_ALL = "";
    private static final String SEARCH_FILTER_TYPE_SONGS = "Song";
    private static final String SEARCH_FILTER_TYPE_ALBUMS = "Album";
    private static final String SEARCH_FILTER_TYPE_PLAYLISTS = "Playlist";
    public PlayerService mPlayerService = null;
    // UI Elements
    private View mRootView;
    private CustomDialogLayout mDialog;
    private FrameLayout flList;
    private LinearLayout llMusicOptions;
    private VerticalSeekBar sbListViewProgress;
    private LinearLayout llListViewProgressControl;
    private SwipeRefreshLayout mListViewContainer;
    private Button btnFavorite;
    private Button btnDownload;
    private Button btnPlayNow;
    private ProgressBar pbLoadMedia;
    private boolean isExpanded;
    private ProgressBar pbDownloadInQueued;
    private ProgressBar pbFavorite;

    private IListSearchResultListener mListener;
    private MusicListAdapter mResultListAdapter;
    private List<MediaItem> mListMediaItems;
    private List<Long> mListFavorites;
    private List<Long> mListMediaInQueue;
    private String strQueryText;
    private DataManager mDataManager;
    private MediaType mMediaType;
    private boolean isScrolling = true;
    private boolean isLoadMore = false;
    private boolean isSaveOffline;
    private String strSearchType = SEARCH_FILTER_TYPE_ALL;
    private int mScreenWidthInPxl = 0;
    private int tmpSavedScrollerWidth = 0;
    private int mCurrentStartIndex = RESULT_MINIMUM_INDEX;
    private int mTotalCounts = 0;
    private MediaItem mSelectedMedia;
    private boolean isDoneLoadFavorites;
    private TextView txtempty;

    public static SearchResultListFragment newInstance(IListSearchResultListener listener, String queryText, MediaType type) {
        final SearchResultListFragment fragment = new SearchResultListFragment();
        fragment.setQueryText(queryText);
        fragment.setMediaType(type);
        fragment.setListener(listener);

        return fragment;
    }

    public void setListener(IListSearchResultListener listener) {
        this.mListener = listener;
    }

    public void setQueryText(String queryText) {
        this.strQueryText = queryText;
    }

    public void setMediaType(MediaType type) {
        this.mMediaType = type;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mDataManager = DataManager.getInstance(getActivity());
    }

    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = (View) inflater.inflate(R.layout.carmode_fragment_list_musics, container, false);
            flList = (FrameLayout) mRootView.findViewById(R.id.fl_list);
            llMusicOptions = (LinearLayout) mRootView.findViewById(R.id.ll_music_options);
            sbListViewProgress = (VerticalSeekBar) mRootView.findViewById(R.id.sb_listview_progress);
            llListViewProgressControl = (LinearLayout) mRootView.findViewById(R.id.ll_listview_progress_control);
            mListViewContainer = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipeRefreshLayout_listView);
            btnFavorite = (Button) mRootView.findViewById(R.id.btn_favorite);
            btnDownload = (Button) mRootView.findViewById(R.id.btn_download);
            btnPlayNow = (Button) mRootView.findViewById(R.id.btn_play_now);
            pbLoadMedia = (ProgressBar) mRootView.findViewById(R.id.pb_load_media);
            pbDownloadInQueued = (ProgressBar) mRootView.findViewById(R.id.pb_download_queued);
            pbFavorite = (ProgressBar) mRootView.findViewById(R.id.pb_favorite);
            txtempty=(TextView) mRootView.findViewById(R.id.empty);

        }

        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        vInitUI();

        switch (mMediaType) {
            case ALBUM:
                strSearchType = SEARCH_FILTER_TYPE_ALBUMS;
                break;
            case PLAYLIST:
                strSearchType = SEARCH_FILTER_TYPE_PLAYLISTS;
                break;
            case TRACK:
                strSearchType = SEARCH_FILTER_TYPE_SONGS;
                break;
            default:
                break;
        }

        // Get Media in Queue
        this.mPlayerService = PlayerService.service;
        mListMediaInQueue = new ArrayList<Long>();
        for (int i = 0; i < mPlayerService.getPlayingQueue().size(); i++) {
            mListMediaInQueue.add(mPlayerService.getPlayingQueue().get(i).getId());
        }

        mDataManager = DataManager.getInstance(getActivity());
        loadData();
    }

    private void loadData() {
        if ((mTotalCounts == 0) // First fetching searches.
                || (mCurrentStartIndex < mTotalCounts)) {
            mDataManager.getSearchKeyboard(strQueryText, strSearchType, String.valueOf(mCurrentStartIndex), String.valueOf(RESULT_TO_PRESENT), this);

            // Get List of Favorites:
            mDataManager.getFavorites(getActivity(), mMediaType, mDataManager.getApplicationConfigurations().getPartnerUserId(), this);
        }
    }

    private void vInitUI() {
        // Listview's seekbar
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

        getListView().setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Enable/Disable SwipeToRefresh view
                if (mListViewContainer != null) {
                    if (firstVisibleItem == 0) {
                        mListViewContainer.setEnabled(true);
                    } else {
                        mListViewContainer.setEnabled(false);
                    }
                }

                // Load more trigger.
                final int noOfRemainItems = totalItemCount - (firstVisibleItem + visibleItemCount);
                if (noOfRemainItems == TRIGGER_LOAD_NO && !isLoadMore) {
                    isLoadMore = true;
                    loadData();
                }


                // Update progress for Seekbar.
                sbListViewProgress.setProgress(firstVisibleItem);
            }
        });

        mListViewContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                if (mListMediaItems != null) {// Reset data before fetching data.
                    mListMediaItems.clear();
                }
                mCurrentStartIndex = RESULT_MINIMUM_INDEX; // Reset currentStartIndex
                loadData(); // Fetching Data.
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

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final MediaItem mediaItem = mListMediaItems.get(position);
                final MediaType mediaType = mediaItem.getMediaType();
                mListener.goToMusicDetail(mediaItem);
            }
        });

        mListViewContainer.setDistanceToTriggerSync(100);
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

                case PLAYLIST:
                    cacheState = DBOHandler.getPlaylistCacheState(getActivity(), "" + mSelectedMedia.getId());
                    if (cacheState == DataBase.CacheState.NOT_CACHED && (DBOHandler.getPlaylistCachedCount(getActivity(), "" + mSelectedMedia.getId()) > 0)) {
                        cacheState = DataBase.CacheState.CACHED;
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
    public void onStart(int operationId) {
        String msg = "";

        switch (operationId) {
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
            case OperationDefinition.Hungama.OperationId.SEARCH:
                if (!isLoadMore) {
//                    mDialog = GlobalFunction.showMessageDialog(getActivity(), DialogType.MESSAGE_FORCE_CLOSE, getResources().getString(R.string.msg_search_result), null);
                    if (getActivity() != null) {
                        ((CarModeHomeActivity) getActivity()).showLoadDialog();
                    }
                }

                break;

            case OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS:
                isDoneLoadFavorites = false;
                break;

            default:
                break;
        }

    }

    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        if (isVisible()) {
            switch (operationId) {
                case OperationDefinition.Hungama.OperationId.SEARCH:

                    if (responseObjects.containsKey(SearchKeyboardOperation.RESPONSE_KEY_TOAST)) { // Message from Server.
                        String responseMsg = (String) responseObjects.get(SearchKeyboardOperation.RESPONSE_KEY_TOAST);
                        showMessageDialog(responseMsg);
                    } else { // Show Results.
                        final SearchResponse mSearchResponse = (SearchResponse) responseObjects.get(SearchKeyboardOperation.RESPONSE_KEY_SEARCH);
                        String type = (String) responseObjects.get(SearchKeyboardOperation.RESPONSE_KEY_TYPE);

                        final List<MediaItem> listSearches = mSearchResponse.getContent();

                        if ((listSearches != null) && (listSearches.size() > 0) && type.equals(strSearchType)) {
                            if (mResultListAdapter == null) {
                                mListMediaItems = listSearches;
                                sbListViewProgress.setMax(mSearchResponse.getTotalCount());
                                mResultListAdapter = new MusicListAdapter(getActivity(), mListMediaItems);
                                mResultListAdapter.setMediaItemEvents(new MediaItemEvents() {

                                    @Override
                                    public void onExpandMenu(int mediaItemPos) {
                                        mSelectedMedia = mListMediaItems.get(mediaItemPos);
                                        isExpanded = true;
                                        isScrolling = false; // Flag for disable scrolling.
                                        expandMusicOptions();
                                        mResultListAdapter.enableItemEvent(false);
                                    }

                                    @Override
                                    public void onCollapseMenu() {
                                        collapseMusicOptions();
                                        isScrolling = true;
                                        isExpanded = false;
                                        mResultListAdapter.enableItemEvent(true);
                                    }

                                });

                                setListAdapter(mResultListAdapter);
                            } else {
                                mListMediaItems.addAll(listSearches);
                                mResultListAdapter.notifyDataSetChanged();
                            }

                            mCurrentStartIndex += mSearchResponse.getLength();
                            mTotalCounts = mSearchResponse.getTotalCount();
                            mListViewContainer.setRefreshing(false);

                        }else{
                            txtempty.setVisibility(View.VISIBLE);
                        }

                        if (!isLoadMore) {
                            isLoadMore = false;
                        }

                        if (getActivity() != null) {
                            ((CarModeHomeActivity) getActivity()).hideLoadDialog();
                        }
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

                            CacheManager.saveAllTracksOfflineAction(getActivity(), tracks);
                            isSaveOffline = false;
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
                            mResultListAdapter.vHandleCollapseOptions();

                            pbLoadMedia.setVisibility(View.GONE);
                            btnPlayNow.setClickable(true);
                            btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause), null, null, null);
                            btnPlayNow.setText(R.string.queue_bottom_text_now_playing1);
                        } else if (playerOptions == PlayerOption.OPTION_PLAY_NOW_AND_OPEN) { // Check if current PlayList/Album is playing
                            boolean isPlaying = false;

                            /*if (!mPlayerService.isQueueEmpty()
                                    && (mPlayerService.isPlaying() || mPlayerService.isLoading())
                                    && (mPlayerService.getCurrentPlayingTrack() != null)) {
                                final long curTrackId = mPlayerService.getCurrentPlayingTrack().getId();
                                for (Track track : tracks) {
                                    if (track.getId() == curTrackId) {
                                        btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause), null, null, null);
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
                        // Dismiss Fetching dialog.
                        // mDialog.hide();
                        isDoneLoadFavorites = true;
                    } else {
                        List<MediaItem> listItems = profileFavoriteMediaItems.mediaItems;

                        if ((listItems != null) && (listItems.size() > 0)) {
                            if (mListFavorites == null) {
                                mListFavorites = new ArrayList<Long>();
                            }

                            for (int i = 0; i < listItems.size(); i++) {
                                mListFavorites.add(listItems.get(i).getId());
                            }

                            isDoneLoadFavorites = true;
                            if (isExpanded) {
                                updateFavoriteBtn();
                            }

                        }
                    }


                default:
                    break;
            }

            flList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType, String errorMessage) {
        flList.setVisibility(View.VISIBLE);

        if (getActivity() != null) {
            ((CarModeHomeActivity) getActivity()).hideLoadDialog();
        }

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

    public CustomDialogLayout showMessageDialog(String msg) {
        CustomDialogLayout dialog = new CustomDialogLayout(getActivity(), DialogType.MESSAGE);
        dialog.setMessage(msg);

        return dialog;
    }

    // Reset layout before calling next fragment.
    public void resetMusicLayout() {
        if(mRootView!=null) {
            isScrolling = true;
            llMusicOptions.setVisibility(View.GONE);
            llListViewProgressControl.setVisibility(View.VISIBLE);
            mRootView.requestLayout();

            if (mResultListAdapter != null) {
                mResultListAdapter.setSelectedPos(-1);
                mResultListAdapter.setMediaMenuExpanded(false);
                mResultListAdapter.notifyDataSetChanged();
            }
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

    private void expandMusicOptions() {
        handleCacheState();
        updateFavoriteBtn();

        if (mSelectedMedia.getMediaType() == MediaType.TRACK) {
            /*if (!mPlayerService.isQueueEmpty()
                    && (mPlayerService.isPlaying() || mPlayerService.isLoading())
                    && (mPlayerService.getCurrentPlayingTrack() != null)
                    && (mSelectedMedia.getId() == mPlayerService.getCurrentPlayingTrack().getId())) {
                btnPlayNow.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.carmode_btn_pause), null, null, null);
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
        llMusicOptions.setVisibility(View.GONE);
        llListViewProgressControl.setVisibility(View.VISIBLE);
    }

    public void vHandleListSearchResultClicks(View v) {
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

                        mResultListAdapter.vHandleCollapseOptions();

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
                            final List<Track> tracksInQueue = mPlayerService.getPlayingQueue();

                            // Check whether track in Queue or Not.
                            /*if (tracksInQueue.contains(selectedTrack)) {
                                final int trackPos = tracksInQueue.indexOf(selectedTrack);
                                mPlayerService.playFromPosition(trackPos);
                            } else {
                                mPlayerService.playNow(Collections.singletonList(selectedTrack));
                            }*/
                            ListMusicFragment.playNowNew(getActivity(), Collections.singletonList(selectedTrack), null, null);

                            mResultListAdapter.vHandleCollapseOptions();

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

                break;
        }
    }


    public interface IListSearchResultListener {
        void onAddTrackToQueue(List<Track> listTracks);

        void goToMusicDetail(MediaItem selectedMedia);

        void popBackStack();

        void onPlayNow();
    }
}
