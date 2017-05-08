package com.hungama.myplay.activity.ui.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.MediaCachingTaskNew;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.catchmedia.JsonRPC2Methods;
import com.hungama.myplay.activity.playlist.PlaylistManager;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.ui.MediaDetailsActivityNew;
import com.hungama.myplay.activity.ui.PlaylistsActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.adapters.ComboMediaItem;
import com.hungama.myplay.activity.ui.adapters.MyAdapter;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ItemableTilesFragment extends MediaTileGridFragment implements
        OnMediaItemOptionSelectedListener, CommunicationOperationListener {

    public static final String TAG = "ItemableTilesFragment";

    private DataManager mDataManager;
    private PlaylistManager mPlaylistManager;
    private MediaType mMediaType;

    private RecyclerView mTilesRecyclerView;
    private int mTileSize = 0;

    private MyAdapter mMediaTilesAdapter;

    private List<Track> mTracks = new ArrayList<Track>();
    private List<Playlist> mPlaylists = new ArrayList<Playlist>();
    private List<MediaItem> mediaItems;

    private Playlist selectedPlaylist;

    private MyProgressDialog mProgressDialog;
    ApplicationConfigurations mApplicationConfigurations;
    int positionToDelete;
    private boolean isAppUser, isFromProfile;
    private CacheStateReceiver cacheStateReceiver;
    public ItemableTilesFragment() {
    }

    public void init(MediaType mediaType, Playlist selectedPlaylist) {
        init(mediaType, selectedPlaylist, true, false);
    }

    public void init(MediaType mediaType,
                     Playlist selectedPlaylist, boolean isAppUser, boolean isFromProfile) {
        this.mMediaType = mediaType;
        this.selectedPlaylist = selectedPlaylist;
        this.isAppUser = isAppUser;
        this.isFromProfile = isFromProfile;
    }

    public void setIsFromProfile(boolean isFromProfile) {
        this.isFromProfile = isFromProfile;
    }



    // ======================================================
    // Fragment callbacks.
    // ======================================================

    ProfileActivity profileActivity;

    public void setProfileActivity(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
    }

    PlaylistsActivity playlistsActivity;

    public void setPlaylistActivity(PlaylistsActivity playlistsActivity) {
        this.playlistsActivity = playlistsActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) getActivity()).lockDrawer();
        ((MainActivity) getActivity()).removeDrawerIconAndPreference();
        ((MainActivity) getActivity()).setNeedToOpenSearchActivity(false);
        mDataManager = DataManager.getInstance(getActivity()
                .getApplicationContext());
        mPlaylistManager = PlaylistManager.getInstance(getActivity()
                .getApplicationContext());
        super.setProfileActivity(profileActivity);
        super.setPlayList(playlistsActivity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int imageTileSpacing = getResources().getDimensionPixelSize(
                R.dimen.home_music_tile_margin);
        View rootView = inflater.inflate(R.layout.fragment_playlist_new,
                container, false);
        mTilesRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycleView);// new RecyclerView(getActivity());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            mTilesRecyclerView.setOverScrollMode(GridView.OVER_SCROLL_NEVER);
        }
        // sets the background.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mTilesRecyclerView.setBackground(null);
        } else {
            mTilesRecyclerView.setBackgroundDrawable(null);
        }

        if (isFromProfile) {
            rootView.setPadding(0, 0, 0, 0);
        }

        mTilesRecyclerView.setPadding(0, (int) getActivity().getResources()
                .getDimension(R.dimen.home_music_tile_margin_top_extra) - 5, 0, 0);

        /*mTilesRecyclerView.setPadding(0, (int) getActivity().getResources()
                .getDimension(R.dimen.home_music_tile_margin) - 5, 0, 0);

        mTilesRecyclerView.setBackgroundColor(getResources().getColor(
                R.color.application_background_grey));*/
		/*
		 * For placing the tiles correctly in the grid, calculates the maximum
		 * size that a tile can be and the column width.
		 */

        // measuring the device's screen width. and setting the grid column
        // width.
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        int screenWidth = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            screenWidth = display.getWidth();
        } else {
            Point displaySize = new Point();
            display.getSize(displaySize);
            screenWidth = displaySize.x;
        }

        mTileSize = (int) ((screenWidth - (imageTileSpacing + imageTileSpacing * 1.5)) / 2);

        Logger.i(TAG, "screenWidth: " + screenWidth + " mTileSize: "
                + mTileSize);
        mApplicationConfigurations = ApplicationConfigurations
                .getInstance(getActivity());
        if (cacheStateReceiver == null) {
            cacheStateReceiver = new CacheStateReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(CacheManager.ACTION_CACHE_STATE_UPDATED);
            filter.addAction(CacheManager.ACTION_TRACK_CACHED);
            filter.addAction(CacheManager.ACTION_UPDATED_CACHE);
            getActivity().registerReceiver(cacheStateReceiver, filter);
        }

        return rootView;
    }

    public void setTitle(boolean needOnlyHight, boolean needToSetTitle) {

        if (needToSetTitle) {
            if (mMediaType == MediaType.PLAYLIST) {
                mPlaylists = getPlaylists();
                String title = getString(R.string.itemable_text_title, "("
                        + mPlaylists.size() + ")");
                //updateTitle(title, mMediaType);
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
                        title, "");
            } else if (mMediaType == MediaType.TRACK) {
                // Get mTracks by PlayList
                mTracks = mPlaylistManager
                        .getTracksListByPlaylist(selectedPlaylist);
                String title = selectedPlaylist.getName() + " (" + mTracks.size()
                        + ")";
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
                        title, "");
            }
        }
        if(profileActivity!=null)
            profileActivity.setNavigationClick();

        if(playlistsActivity!=null)
            playlistsActivity.setNavigationClick();

        Utils.setToolbarColor(((MainActivity) getActivity()));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mMediaType == MediaType.PLAYLIST) {

            mPlaylists = getPlaylists();

            String title = getString(R.string.itemable_text_title, "("
                    + mPlaylists.size() + ")");
            if (isFromProfile)
                updateTitle(title, mMediaType);
            else
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
                        title, "");
        } else if (mMediaType == MediaType.TRACK) {

            // Get mTracks by PlayList
            mTracks = mPlaylistManager
                    .getTracksListByPlaylist(selectedPlaylist);

            String title = selectedPlaylist.getName() + " (" + mTracks.size()
                    + ")";
            if (isFromProfile)
                updateTitle(title, mMediaType);
            else
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
                        title, "");

        }
        if (!isFromProfile) {
            setNavigationClick();
        }
        buildMediaItemsList();

		/*
		 * Builds the Grids Adapter after we have the list of items to present.
		 */
        mTilesRecyclerView
                .setOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(RecyclerView view,
                                                     int scrollState) {
                        if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                            try {
                                mMediaTilesAdapter.postAdForPosition();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx,
                                           int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                    }

                });

        ArrayList<Object> mediaitemMusic = refreshList();

        mMediaTilesAdapter = new MyAdapter(mediaitemMusic, getActivity(), null,
                null, null, isAppUser,// true,
                flurrySubSectionDescription, mTilesRecyclerView);
        mMediaTilesAdapter.setIsPlaylistScreen(isPlaylistScreen);
        mMediaTilesAdapter.setOnMusicItemOptionSelectedListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(
                getActivity());
        mTilesRecyclerView.setLayoutManager(mLayoutManager);
        mTilesRecyclerView.setAdapter(mMediaTilesAdapter);

    }

    private void setNavigationClick(){
        try {
            ((MainActivity) getActivity()).mToolbar.setNavigationOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    boolean isPlaylistScreen = false;
    public void setIsPlaylistScreen(boolean isPlaylistScreen) {
        this.isPlaylistScreen = isPlaylistScreen;
    }
    /**
     * refresh and differentiate mediaitems(song, album and playlist)
     *
     * @return ArrayList<Object>
     */
    private ArrayList<Object> refreshList() {
        ArrayList<Object> mediaitemMusic = new ArrayList<Object>();
        mediaitemMusic.clear();

        List<MediaItem> tracks = new ArrayList<MediaItem>();
        List<MediaItem> playlists = new ArrayList<MediaItem>();

        if (mediaItems != null)
            for (MediaItem mediaItem : mediaItems) {
                if (mediaItem.getMediaType() == MediaType.TRACK)
                    tracks.add(mediaItem);
                else if (mediaItem.getMediaType() == MediaType.ALBUM)
                    tracks.add(mediaItem);
                else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
                    playlists.add(mediaItem);

            }
        ArrayList<MediaItem> serverSAA = new ArrayList<MediaItem>(tracks);
        int adcount = 0;

        if (adcount != 0)
            tracks = serverSAA;

        int blocks = (tracks.size()) / 2;

        if (playlists != null && playlists.size() > 0) {
            blocks = (playlists.size());
        } else if (tracks != null && tracks.size() > 0) {
            blocks = (tracks.size()) / 2;
            if ((tracks.size()) % 2 > 0)
                blocks += 1;
        } else if (playlists != null && playlists.size() > 0) {
            blocks = (tracks.size() + playlists.size()) / 5;
            if ((tracks.size() + playlists.size()) % 5 > 0)
                blocks += 1;
        }

        ComboMediaItem c;
        for (int i = 0; i < blocks; i++) {
            if (tracks.size() > 0) {
                c = new ComboMediaItem(tracks.get(0),
                        (tracks.size() > 1) ? tracks.get(1) : null);
                mediaitemMusic.add(c);
                tracks.remove(0);
                if (tracks.size() > 0)
                    tracks.remove(0);
            }

            if (tracks.size() > 0) {
                c = new ComboMediaItem(tracks.get(0),
                        (tracks.size() > 1) ? tracks.get(1) : null);
                mediaitemMusic.add(c);
                tracks.remove(0);
                if (tracks.size() > 0)
                    tracks.remove(0);
            }
            if (playlists.size() > 0) {
                mediaitemMusic.add(playlists.get(0));
                playlists.remove(0);
                if (tracks.size() == 0) {
                    for (MediaItem obj : playlists) {
                        mediaitemMusic.add(obj);
                    }
                    playlists.clear();
                }
            }
        }
        return mediaitemMusic;
    }

    // ======================================================
    // Tiles Adapter callbacks.
    // ======================================================

    @Override
    public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem,
                                                 int position) {
        if (mMediaType == MediaType.PLAYLIST) {
            // gets the selected playlist.
            Playlist playlist = getPlaylistById(mediaItem.getId());
            if (playlist != null) {
                // gets the playlist's track.
                List<Track> tracks = mPlaylistManager
                        .getTracksListByPlaylist(playlist);
                for (Track track : tracks) {
                    track.setTag(playlist);
                }
                if (!Utils.isListEmpty(tracks)) {
                    PlayerBarFragment playerBar = ((MainActivity) getActivity())
                            .getPlayerBar();
                    // playerBar.addToQueue(tracks);
                    playerBar.playNow(tracks, null, null);
                }
                ApsalarEvent.postEvent(getActivity(), ApsalarEvent.PLAYLIST_CREATED, ApsalarEvent.TYPE_PLAYLIST_PLAYED);
            }
        } else if (mMediaType == MediaType.TRACK) {
            // creates a track from the media item.
            Track track = new Track(mediaItem.getId(), mediaItem.getTitle(),
                    mediaItem.getAlbumName(), mediaItem.getArtistName(),
                    mediaItem.getImageUrl(), mediaItem.getBigImageUrl(),
                    mediaItem.getImages(), mediaItem.getAlbumId());
            if (selectedPlaylist != null)
                track.setTag(selectedPlaylist);
            List<Track> tracks = new ArrayList<Track>();
            tracks.add(track);
            // plays now the track.
            PlayerBarFragment playerBar = ((MainActivity) getActivity())
                    .getPlayerBar();
            playerBar.playNow(tracks, null, null);
        }
    }

    @Override
    public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
                                                  int position) {
    }

    @Override
    public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
                                                    int position) {
        if (mMediaType == MediaType.PLAYLIST) {
            // gets the selected playlist.
            Playlist playlist = getPlaylistById(mediaItem.getId());
            if (playlist != null) {
                // gets the playlist's track.
                List<Track> tracks = mPlaylistManager
                        .getTracksListByPlaylist(playlist);
                for (Track track : tracks) {
                    track.setTag(playlist);
                }
                if (!Utils.isListEmpty(tracks)) {
                    PlayerBarFragment playerBar = ((MainActivity) getActivity())
                            .getPlayerBar();
                    playerBar.addToQueue(tracks, null, null);
                }
            }

        } else if (mMediaType == MediaType.TRACK) {
            // creates a track from the media item.
            Track track = new Track(mediaItem.getId(), mediaItem.getTitle(),
                    mediaItem.getAlbumName(), mediaItem.getArtistName(),
                    mediaItem.getImageUrl(), mediaItem.getBigImageUrl(),
                    mediaItem.getImages(), mediaItem.getAlbumId());
            if (selectedPlaylist != null)
                track.setTag(selectedPlaylist);
            List<Track> tracks = new ArrayList<Track>();
            tracks.add(track);
            // plays now the track.
            PlayerBarFragment playerBar = ((MainActivity) getActivity())
                    .getPlayerBar();
            playerBar.addToQueue(tracks, null, null);
        }
    }

    @Override
    public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
                                                     int position) {

        if (mMediaType == MediaType.PLAYLIST) {

            // Show selected mTracks's play list
            ItemableTilesFragment mTilesFragment = new ItemableTilesFragment();
            mTilesFragment.init(MediaType.TRACK, (Playlist) mPlaylists.get(position));
            mTilesFragment.setIsFromProfile(isFromProfile);
			mTilesFragment.setProfileActivity(profileActivity);
            mTilesFragment.setPlaylistActivity(playlistsActivity);
            FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
                    R.anim.slide_left_exit, R.anim.slide_right_enter,
                    R.anim.slide_right_exit);
//            if (!isFromProfile)
//                fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
//                        mTilesFragment, "PlayListActivity");
//            else
            fragmentTransaction.add(R.id.main_fragmant_container,
                    mTilesFragment, "PlayListActivity");
            fragmentTransaction.addToBackStack("PlayListActivity");
            if(Constants.IS_COMMITALLOWSTATE)
                fragmentTransaction.commitAllowingStateLoss();
            else
                fragmentTransaction.commit();

        } else if (mMediaType == MediaType.TRACK) {
            // Show selected track details/
            FragmentManager mFragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();

            MediaDetailsActivityNew mediaDetailsFragment = new MediaDetailsActivityNew();

            Bundle bundle = new Bundle();

            bundle.putSerializable(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
                    (Serializable) mediaItem);
            bundle.putSerializable(MediaDetailsActivity.EXTRA_PLAYLIST_ITEM,
                    (Serializable) selectedPlaylist);

            bundle.putString(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
                    FlurryConstants.FlurrySourceSection.Playlists.toString());

            mediaDetailsFragment.setArguments(bundle);

            fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
                    mediaDetailsFragment, "MediaDetailsActivitySearch111");
            fragmentTransaction.addToBackStack("MediaDetailsActivitySearch111");
            if(Constants.IS_COMMITALLOWSTATE)
                fragmentTransaction.commitAllowingStateLoss();
            else
                fragmentTransaction.commit();

//            Intent intent = new Intent(getActivity(),
//                    MediaDetailsActivity.class);
//            intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
//                    (Serializable) mediaItem);
//            intent.putExtra(MediaDetailsActivity.EXTRA_PLAYLIST_ITEM,
//                    (Serializable) selectedPlaylist);
//            intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
//                    FlurryConstants.FlurrySourceSection.Playlists.toString());
//            startActivity(intent);
        }
    }

    @Override
    public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
                                                int position) {

        if (mMediaType == MediaType.PLAYLIST) {

            positionToDelete = position;

            final CustomAlertDialog clearDialogBuilder = new CustomAlertDialog(
                    getActivity());
            // clearDialogBuilder.setTitle(Utils.TEXT_EMPTY);
            clearDialogBuilder.setMessage(Utils
                    .getMultilanguageTextHindi(getActivity(),
                            getString(R.string.playlists_message_delete)));
            clearDialogBuilder.setCancelable(true);
            // sets the OK button.
            clearDialogBuilder.setPositiveButton(Utils
                            .getMultilanguageTextHindi(getActivity(),
                                    getString(R.string.playlists_message_delete_ok)),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // // Delete selected play list

                            if (positionToDelete >= 0
                                    && positionToDelete < mPlaylists.size()) {

                                // Updates the server with the deletion.
                                Playlist playlist = (Playlist) mPlaylists
                                        .get(positionToDelete);
                                mDataManager.playlistOperation(
                                        ItemableTilesFragment.this,
                                        playlist.getId(), null, null,
                                        JsonRPC2Methods.DELETE);
                               // setTitle(true,true);
                            }
                        }
                    });

            clearDialogBuilder.show();

        } else if (mMediaType == MediaType.TRACK) {
            try {
                positionToDelete = position;
                // Delete selected track from play list
                Track track = (Track) mTracks.get(position);
                selectedPlaylist.removeTrack(track.getId());
                mDataManager
                        .playlistOperation(this, selectedPlaylist.getId(),
                                selectedPlaylist.getName(),
                                selectedPlaylist.getTrackList(),
                                JsonRPC2Methods.UPDATE);
               // setTitle(true,true);

            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
        }

    }

    // ======================================================
    // Operations callbacks.
    // ======================================================

    @Override
    public void onStart(int operationId) {
        showLoadingDialog(Utils.getMultilanguageTextHindi(getActivity(),
                getActivity().getString(R.string.processing)));
    }

    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        try {
            switch (operationId) {
                case (OperationDefinition.CatchMedia.OperationId.PLAYLIST):

                    hideLoadingDialog();

				/*
				 * Updates list items and the title.
				 */
                    if (mMediaType == MediaType.PLAYLIST) {
                        mPlaylists = getPlaylists();

                        int size = mPlaylists != null ? mPlaylists.size() : 0;
                        String title = getString(R.string.itemable_text_title, "("
                                + size + ")");
                        updateTitle(title, mMediaType);

                    } else if (mMediaType == MediaType.TRACK) {
                        mTracks = mPlaylistManager
                                .getTracksListByPlaylist(selectedPlaylist);

                        int size = mTracks != null ? mTracks.size() : 0;
                        String title = selectedPlaylist.getName() + " (" + size
                                + ")";
                        updateTitle(title, mMediaType);
                    }

                    // Delete selected item and updates the grid.
                    mediaItems.remove(positionToDelete);
                    ArrayList<Object> mediaitemMusic = refreshList();
                    mMediaTilesAdapter.setMediaItems(mediaitemMusic);
                    mMediaTilesAdapter.notifyDataSetChanged();
                    ApsalarEvent.postEvent(getActivity(), ApsalarEvent.PLAYLIST_CREATED, ApsalarEvent.TYPE_PLAYLIST_SAVED);
                    break;

                default:
                    break;
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType,
                          String errorMessage) {

        hideLoadingDialog();

    }

    // ======================================================
    // helper methods.
    // ======================================================

    private Playlist getPlaylistById(long id) {
        for (Playlist playlist : mPlaylists) {
            if (playlist.getId() == id)
                return playlist;
        }
        return null;
    }

    public void showLoadingDialog(String message) {
        if (!getActivity().isFinishing()) {
            if (mProgressDialog == null) {
                mProgressDialog = new MyProgressDialog(getActivity());
            }
        }
    }

    public void hideLoadingDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    /**
     * Building MediaItems list from list of Playlist/mTracks (mPlaylists)
     */
    private void buildMediaItemsList() {

        mediaItems = new ArrayList<MediaItem>();
        MediaItem mediaItem;

        if (mMediaType == MediaType.PLAYLIST) {
            if (!Utils.isListEmpty(mPlaylists)) {
                for (Playlist item : mPlaylists) {

                    mediaItem = new MediaItem(item.getId(), item.getName(),
                            null, null, null, null, mMediaType.name()
                            .toLowerCase(), 0, 0);

                    mediaItem.setMediaType(MediaType.PLAYLIST);
                    mediaItem.setMusicTrackCount(((Playlist) item)
                            .getNumberOfTracks());
                    mediaItem.setMediaContentType(MediaContentType.MUSIC);

                    mediaItems.add(mediaItem);
                }
            }

        } else if (mMediaType == MediaType.TRACK) {
            if (!Utils.isListEmpty(mTracks)) {
                for (Track track : mTracks) {

                    mediaItem = new MediaItem(track.getId(), track.getTitle(),
                            track.getAlbumName(), track.getArtistName(),
                            track.getImageUrl(), track.getBigImageUrl(),
                            mMediaType.name().toLowerCase(), 0, 0,
                            track.getImages(), track.getAlbumId());

                    mediaItem.setMediaType(MediaType.TRACK);
                    mediaItem.setMediaContentType(MediaContentType.MUSIC);

                    mediaItems.add(mediaItem);
                }
            }
        }
    }

    /**
     * set title for action bar according to media item
     *
     * @param title
     * @param mediaType
     */
    private void updateTitle(String title, MediaType mediaType) {
        Activity activity = getActivity();
       /* if (activity instanceof PlaylistsActivity) {
            PlaylistsActivity playlistsActivity = (PlaylistsActivity) activity;
            playlistsActivity.showBackButtonWithTitle(title, "");
            playlistsActivity.stack_text.push(title);
            playlistsActivity.getMainTitleBarText().setText(title);
            if (mediaType == MediaType.TRACK) {
                ImageView mainTitleBarButtonOptions = playlistsActivity
                        .getMainTitleBarButtonOptions();
                if (mTracks.size() > 0) {
                    mainTitleBarButtonOptions.setVisibility(View.VISIBLE);
                }

            } else if (mediaType == MediaType.PLAYLIST) {
                ImageView mainTitleBarButtonOptions = playlistsActivity
                        .getMainTitleBarButtonOptions();
                mainTitleBarButtonOptions.setVisibility(View.GONE);
            }

        } else */

            if (mMediaType == MediaType.PLAYLIST) {
                mPlaylists = getPlaylists();
                String settitle = getString(R.string.itemable_text_title, "("
                        + mPlaylists.size() + ")");
                //updateTitle(title, mMediaType);
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
                        settitle, "");
            } else if (mMediaType == MediaType.TRACK) {
                // Get mTracks by PlayList
                mTracks = mPlaylistManager
                        .getTracksListByPlaylist(selectedPlaylist);
                String settitle = selectedPlaylist.getName() + " (" + mTracks.size()
                        + ")";
                ((MainActivity) getActivity()).showBackButtonWithTitleMediaDetail(
                        settitle, "");
            }


        if (profileActivity != null && profileActivity instanceof ProfileActivity) {
            profileActivity.setTitleBarText(title);
        }
    }

    /**
     * get playlist
     *
     * @return List<Playlist>
     */
    private List<Playlist> getPlaylists() {

        // Get all playlists
        Map<Long, Playlist> map = mDataManager.getStoredPlaylists();
        List<Playlist> UpdatedPlaylists = new ArrayList<Playlist>();

        // Convert from Map<Long, Playlist> to List<Itemable>
        if (map != null && map.size() > 0) {
            for (Map.Entry<Long, Playlist> p : map.entrySet()) {
                UpdatedPlaylists.add(p.getValue());
            }
        }
        if (UpdatedPlaylists != null && UpdatedPlaylists.size() > 0)
            Collections.reverse(UpdatedPlaylists);
        return UpdatedPlaylists;
    }

    public List<Track> getTracksToPlayAll() {
        if (mTracks != null) {
            return mTracks;
        }
        return null;
    }

    @Override
    public void onStart() {
        super.onStart();
//        Analytics.startSession(getActivity(), this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Analytics.onEndSession(getActivity());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener
     * #
     * onMediaItemOptionSaveOfflineSelected(com.hungama.myplay.activity.data.dao
     * .hungama.MediaItem, int)
     */
    @Override
    public void onMediaItemOptionSaveOfflineSelected(MediaItem mediaItem,
                                                     int position) {
        Logger.i(TAG, "Save Offline: " + mediaItem.getId());
        if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {
            if (mediaItem.getMediaType() == MediaType.TRACK) {
                Track track = new Track(mediaItem.getId(),
                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                        mediaItem.getAlbumId());
                CacheManager.saveOfflineAction(getActivity(), mediaItem, track);

                Utils.saveOfflineFlurryEvent(getActivity(),
                        FlurryConstants.FlurryCaching.LongPressMenuSong
                                .toString(), mediaItem);
            } else if (mediaItem.getMediaType() == MediaType.ALBUM
                    || mediaItem.getMediaType() == MediaType.PLAYLIST) {
                if (MediaCachingTaskNew.isEnabled) {
                    Playlist playlist = getPlaylistById(mediaItem.getId());
                    if (playlist != null) {
                        List<Track> tracks = mPlaylistManager
                                .getTracksListByPlaylist(playlist);
                        if (!Utils.isListEmpty(tracks)) {
                            CacheManager.saveAllTracksOfflineAction(
                                    getActivity(), tracks);
                        }
                    }
                } else {
                    CacheManager.saveOfflineAction(getActivity(), mediaItem,
                            null);
                }

                if (mediaItem.getMediaType() == MediaType.ALBUM)
                    Utils.saveOfflineFlurryEvent(getActivity(),
                            FlurryConstants.FlurryCaching.LongPressMenuAlbum
                                    .toString(), mediaItem);
                else
                    Utils.saveOfflineFlurryEvent(getActivity(),
                            FlurryConstants.FlurryCaching.LongPressMenuPlaylist
                                    .toString(), mediaItem);
            }
        }
    }

    /**
     * notify adapter for change
     */
    public void updateTrackCacheState() {
        if(mMediaTilesAdapter!=null)
            mMediaTilesAdapter.notifyDataSetChanged();
    }


    public void refreshplaylist() {
        buildMediaItemsList();

		/*
		 * Builds the Grids Adapter after we have the list of items to present.
		 */
        mTilesRecyclerView
                .setOnScrollListener(new RecyclerView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(RecyclerView view,
                                                     int scrollState) {
                        if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                            try {
                                mMediaTilesAdapter.postAdForPosition();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx,
                                           int dy) {
                        super.onScrolled(recyclerView, dx, dy);
                    }

                });

        ArrayList<Object> mediaitemMusic = refreshList();

        mMediaTilesAdapter = new MyAdapter(mediaitemMusic, getActivity(), null,
                null, null, isAppUser,// true,
                flurrySubSectionDescription, mTilesRecyclerView);
        mMediaTilesAdapter.setIsPlaylistScreen(isPlaylistScreen);
        mMediaTilesAdapter.setOnMusicItemOptionSelectedListener(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(
                getActivity());
        mTilesRecyclerView.setLayoutManager(mLayoutManager);
        mTilesRecyclerView.setAdapter(mMediaTilesAdapter);


    }



    @Override
    public void onDestroyView() {
//        System.gc();
//        System.runFinalization();
        if (cacheStateReceiver != null)
            getActivity().unregisterReceiver(cacheStateReceiver);
        cacheStateReceiver = null;
        super.onDestroyView();
    }

    @Override
    public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
                                                     int position) {
    }

    class CacheStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Logger.s("========================= cachestateupdatereceived ========"
                    + arg1.getAction());
            if (arg1.getAction()
                    .equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
                    || arg1.getAction()
                    .equals(CacheManager.ACTION_TRACK_CACHED)) {
                try {
                     updateTrackCacheState();
                } catch (Exception e) {
                    Logger.printStackTrace(e);
                }
            } else if (arg1.getAction().equals(
                    CacheManager.ACTION_UPDATED_CACHE)) {
            }
        }
    }

    Handler handler = new Handler();

    public void openOfflineGuide() {

        if (mApplicationConfigurations.isEnabledHomeGuidePage3Offline()) {
            mApplicationConfigurations
                    .setIsEnabledHomeGuidePage_3OFFLINE(false);
            mApplicationConfigurations.setIsSongCatched(false);

            try {
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        try {

                            // SaveOfflineHelpDialog dialog = new
                            // SaveOfflineHelpDialog(
                            // currentRunningActivity);
                            // dialog.show();
                            getActivity().sendBroadcast(new Intent(
                                    getString(R.string.inapp_prompt_action_saveofflinehelpdialog)));
                        } catch (Exception e) {
                            Logger.printStackTrace(e);
                            mApplicationConfigurations
                                    .setIsEnabledHomeGuidePage_3OFFLINE(true);
                        } catch (Error e) {
                            Logger.printStackTrace(e);
                            mApplicationConfigurations
                                    .setIsEnabledHomeGuidePage_3OFFLINE(true);
                        }
                    }
                }, 300);
            } catch (Exception e) {
                Logger.printStackTrace(e);
                mApplicationConfigurations
                        .setIsEnabledHomeGuidePage_3OFFLINE(true);
            }

        }
    }

}
