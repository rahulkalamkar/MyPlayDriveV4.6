package com.hungama.hungamamusic.ford.carmode;

import android.content.Context;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RadioTopArtistSongsOperation;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment;
import com.hungama.myplay.activity.util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hungama on 1/2/16.
 */
public class GetAubumDetails  {

    private DataManager mDataManager;
    public MediaSetDetails mMediaSetDetails;
    private MediaTrackDetails mMediaTrackDetails;
    List<Track> tracks = new ArrayList<>();

    public interface AlbumDetailCallBack{
        public void onDetailCallback(List<Track> tracks);
        public void onTopArtistPlay(List<Track> tracks);
        public void onConnectionFailure(CommunicationManager.ErrorType errorType);
    }

    public void setDetailCallBack(AlbumDetailCallBack detailCallBack) {
        this.detailCallBack = detailCallBack;
    }

    AlbumDetailCallBack detailCallBack;
    public void getAlbumDetail(final Context mContext, final MediaItem mediaItem){

        mDataManager = DataManager.getInstance(mContext);
        mDataManager.getMediaDetails(mediaItem, null, new CommunicationOperationListener() {
            @Override
            public void onStart(int operationId) {

            }

            @Override
            public void onSuccess(int operationId, Map<String, Object> responseObjects) {
                try {


                    if (mediaItem != null) {
                        if (mediaItem.getMediaType() == MediaType.ALBUM
                                || mediaItem.getMediaType() == MediaType.PLAYLIST) {
                            try {
                                // get details for albums / playlists.
                                mMediaSetDetails = (MediaSetDetails) responseObjects
                                        .get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);

                                tracks = mMediaSetDetails.getTracks();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else if (mediaItem.getMediaType() == MediaType.TRACK) {
                            try {
                                // get details for track (song).
                                mMediaTrackDetails = (MediaTrackDetails) responseObjects
                                        .get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);

                                Track track = new Track(mMediaTrackDetails.getId(),
                                        mMediaTrackDetails.getTitle(), mMediaTrackDetails.getAlbumName(),
                                        mMediaTrackDetails.getSingers(),
                                        mMediaTrackDetails.getImageUrl(),
                                        mMediaTrackDetails.getBigImageUrl(),
                                        mMediaTrackDetails.getImages(), mMediaTrackDetails.getAlbumId());

                                tracks.add(track);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        detailCallBack.onDetailCallback(tracks);

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(final int operationId, CommunicationManager.ErrorType errorType, String errorMessage) {
                if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
                    Logger.i("Hungmama", "Failed loading media details");

                    detailCallBack.onConnectionFailure(errorType);

                    /*((MainActivity) mContext)
                            .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                                @Override
                                public void onRetryButtonClicked() {
                                    onStart(operationId);
                                }
                            });*/
                }
            }
        });


    }

    public void getArtistSong(final Context mContext,MediaItem mediaItem){
        mDataManager = DataManager.getInstance(mContext);
        mDataManager.getRadioTopArtistSongs(mediaItem, new CommunicationOperationListener() {
            @Override
            public void onStart(int operationId) {

            }

            @Override
            public void onSuccess(int operationId, Map<String, Object> responseObjects) {
                try {

                    try {
                        // gets the radio tracks
                        List<Track> radioTracks = (List<Track>) responseObjects
                                .get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_TRACKS);
                        MediaItem mediaItem = (MediaItem) responseObjects
                                .get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_MEDIA_ITEM);
                        int userFav = (Integer) responseObjects
                                .get(RadioTopArtistSongsOperation.RESULT_KEY_OBJECT_USER_FAVORITE);
					/*
					 * sets to each track a reference to a copy of the original
					 * radio item. This to make sure that the player bar can get
					 * source Radio item without leaking this activity!
					 */
                        for (Track track : radioTracks) {
                            track.setTag(mediaItem);
                        }
                        // starts to play.
                        PlayerBarFragment.setArtistRadioId(mediaItem.getId());
                        PlayerBarFragment.setArtistUserFav(userFav);
                        //PlayerBarFragment playerBar = getPlayerBar();
                        //playerBar.playRadio(radioTracks, PlayMode.TOP_ARTISTS_RADIO);

                        detailCallBack.onTopArtistPlay(radioTracks);


                    } catch (Exception e) {
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(final int operationId, CommunicationManager.ErrorType errorType, String errorMessage) {
                if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS) {
                    Logger.i("Hungmama", "Failed loading media details"+errorType);


                    detailCallBack.onConnectionFailure(errorType);
                    /*((MainActivity) mContext)
                            .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                                @Override
                                public void onRetryButtonClicked() {
                                    onStart(operationId);
                                }
                            });*/
                }
            }
        });
    }



}
