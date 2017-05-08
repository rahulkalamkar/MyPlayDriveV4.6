package com.hungama.hungamamusic.ford.carmode;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestParametersException;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestTokenException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.DiscoverSearchResultIndexer;
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.MusicCategoriesResponse;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.social.ProfileFavoriteMediaItems;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.DiscoverSearchResultsOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.SocialProfileFavoriteMediaItemsOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.fragments.FavoritesFragment;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.TTSChunkFactory;
import com.smartdevicelink.proxy.rpc.Alert;
import com.smartdevicelink.proxy.rpc.TTSChunk;
import com.smartdevicelink.proxy.rpc.enums.SpeechCapabilities;
import com.smartdevicelink.proxy.rpc.enums.TextAlignment;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class LockScreenActivity extends Activity implements
        ServiceConnection, PlayerService.PlayerStateListener, CommunicationOperationListener, FavoritesFragment.OnMediaItemsLoadedListener {

    private static final String TAG = "LockScreenActivity";
    private static LockScreenActivity instance = null;

    public static LockScreenActivity getInstance() {
        return instance;
    }

    private PlayerServiceBindingManager.ServiceToken mServiceToken;
    public static String NO_CONNECTION = "no_connection";
    private NoConnectionReceiver noConnectionReceiver;

    private PlayerService.LoopMode currentLoopMode;
    public static final int FAVORITE_SUCCESS = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        instance = this;
        setContentView(R.layout.ford_lockscreen);
        /**
         * Reset sync
         * */
        final Button resetSYNCButton = (Button) findViewById(R.id.lockreset);
        resetSYNCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reset proxy; do not shut down service
                ProxyService serviceInstance = ProxyService.getInstance();
                if (serviceInstance != null) {
                    SdlProxyALM proxyInstance = serviceInstance.getProxyInstance();
                    if (proxyInstance != null) {
                        serviceInstance.reset();
                    } else {
                        serviceInstance.startProxy();
                    }
                }
                exit();
            }
        });
        mServiceToken = PlayerServiceBindingManager.bindToService(
                this, this);

        if (ProxyService.getInstance() != null)
            ProxyService.getInstance().setCurrentActivity(this);


        noConnectionReceiver = new NoConnectionReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NO_CONNECTION);
        registerReceiver(noConnectionReceiver, intentFilter);
//        FavoritesFragment.Instance.setOnMediaItemsLoadedListener(this);

    }

    //disable back button on lockscreen
    @Override
    public void onBackPressed() {
    }

    public void exit() {

        super.finish();
    }


    @Override
    protected void onDestroy() {
        instance = null;

        if (PlayerService.service != null)
            PlayerService.service.setLoopMode(currentLoopMode);

        if (PlayerService.service != null)
            PlayerService.service.unregisterPlayerStateListener(this);


        Utils.clearCache();
        try {
            PlayerServiceBindingManager.unbindFromService(mServiceToken);
        } catch (Exception e) {
        }

        Utils.setFordCarMode(false);
        if (noConnectionReceiver != null)
            this.unregisterReceiver(noConnectionReceiver);

        super.onDestroy();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        Logger.e("Player Service Connected", "Yes");
        if (PlayerService.service != null) {
            PlayerService.service.registerPlayerStateListener(this);
        }
        if (PlayerService.service != null)
            currentLoopMode = PlayerService.service.getLoopMode();

        setPlayLoop();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }


    // ford car mode

    /**
     * Shows a dialog where the user can select connection features (protocol
     * version, media flag, app name, language, HMI language, and transport
     * settings). Starts the proxy after selecting.
     */
    public void propertiesUI(Context context) {
//        if(ProxyService.getInstance()._syncProxy==null || !ProxyService.getInstance()._syncProxy.getIsConnected()){

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.ford_properties,
                (ViewGroup) findViewById(R.id.properties_Root));

        final CheckBox mediaCheckBox = (CheckBox) view
                .findViewById(R.id.properties_checkMedia);
        final EditText appNameEditText = (EditText) view
                .findViewById(R.id.properties_appName);
        final RadioGroup transportGroup = (RadioGroup) view
                .findViewById(R.id.properties_radioGroupTransport);
        final EditText ipAddressEditText = (EditText) view
                .findViewById(R.id.properties_ipAddr);
        final EditText tcpPortEditText = (EditText) view
                .findViewById(R.id.properties_tcpPort);
        final CheckBox autoReconnectCheckBox = (CheckBox) view
                .findViewById(R.id.properties_checkAutoReconnect);

        ipAddressEditText.setEnabled(false);
        tcpPortEditText.setEnabled(false);
        autoReconnectCheckBox.setEnabled(false);

        transportGroup
                .setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        boolean transportOptionsEnabled = checkedId == R.id.properties_radioWiFi;
                        ipAddressEditText.setEnabled(transportOptionsEnabled);
                        tcpPortEditText.setEnabled(transportOptionsEnabled);
                        autoReconnectCheckBox
                                .setEnabled(transportOptionsEnabled);
                    }
                });

        // display current configs
        final SharedPreferences prefs = getSharedPreferences(Const.PREFS_NAME,
                0);
        boolean isMedia = prefs.getBoolean(Const.PREFS_KEY_ISMEDIAAPP,
                Const.PREFS_DEFAULT_ISMEDIAAPP);
        String appName = prefs.getString(Const.PREFS_KEY_APPNAME,
                Const.PREFS_DEFAULT_APPNAME);
        int transportType = prefs.getInt(
                Const.Transport.PREFS_KEY_TRANSPORT_TYPE,
                Const.Transport.PREFS_DEFAULT_TRANSPORT_TYPE);
        String ipAddress = prefs.getString(
                Const.Transport.PREFS_KEY_TRANSPORT_IP,
                Const.Transport.PREFS_DEFAULT_TRANSPORT_IP);
        int tcpPort = prefs.getInt(Const.Transport.PREFS_KEY_TRANSPORT_PORT,
                Const.Transport.PREFS_DEFAULT_TRANSPORT_PORT);
        boolean autoReconnect = prefs.getBoolean(
                Const.Transport.PREFS_KEY_TRANSPORT_RECONNECT,
                Const.Transport.PREFS_DEFAULT_TRANSPORT_RECONNECT_DEFAULT);

        mediaCheckBox.setChecked(isMedia);
        appNameEditText.setText(appName);
        transportGroup
                .check(transportType == Const.Transport.KEY_TCP ? R.id.properties_radioWiFi
                        : R.id.properties_radioBT);
        ipAddressEditText.setText(ipAddress);
        tcpPortEditText.setText(String.valueOf(tcpPort));
        autoReconnectCheckBox.setChecked(autoReconnect);

        new AlertDialog.Builder(context)
                .setTitle("Please select ford_properties")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String appName = appNameEditText.getText().toString();
                        boolean isMedia = mediaCheckBox.isChecked();
                        int transportType = transportGroup
                                .getCheckedRadioButtonId() == R.id.properties_radioWiFi ? Const.Transport.KEY_TCP
                                : Const.Transport.KEY_BLUETOOTH;
                        String ipAddress = ipAddressEditText.getText()
                                .toString();
                        int tcpPort = Integer.parseInt(tcpPortEditText
                                .getText().toString());
                        boolean autoReconnect = autoReconnectCheckBox
                                .isChecked();

                        // save the configs
                        boolean success = prefs
                                .edit()
                                .putBoolean(Const.PREFS_KEY_ISMEDIAAPP, isMedia)
                                .putString(Const.PREFS_KEY_APPNAME, appName)
                                .putInt(Const.Transport.PREFS_KEY_TRANSPORT_TYPE,
                                        transportType)
                                .putString(
                                        Const.Transport.PREFS_KEY_TRANSPORT_IP,
                                        ipAddress)
                                .putInt(Const.Transport.PREFS_KEY_TRANSPORT_PORT,
                                        tcpPort)
                                .putBoolean(
                                        Const.Transport.PREFS_KEY_TRANSPORT_RECONNECT,
                                        autoReconnect).commit();
                        if (!success) {
                            Log.w(TAG,
                                    "Can't save ford_properties");
                        }

                        showPropertiesInTitle();

                        startSyncProxy();
                    }
                }).setView(view).show();
//        }

    }

    /**
     * Starts the sync proxy at startup after selecting protocol features.
     */
    public void startSyncProxy() {
        if (ProxyService.getInstance() == null) {
            Intent startService = new Intent(this, ProxyService.class);
            Log.i(TAG, "Calling start Service to Start the Service");
            startService(startService);
        } else {
            ProxyService.getInstance().setCurrentActivity(this);
        }
    }

    /**
     * Called when a connection to a SYNC device has been closed.
     */
    public void onProxyClosed() {
        PlayerService.service.stop();
//		syncPlayer.reset();
        Log.i(TAG, "Disconnected");
    }


    /**
     * Displays the current protocol ford_properties in the activity's title.
     */
    public void showPropertiesInTitle() {
        final SharedPreferences prefs = getSharedPreferences(Const.PREFS_NAME,
                0);
        boolean isMedia = prefs.getBoolean(Const.PREFS_KEY_ISMEDIAAPP,
                Const.PREFS_DEFAULT_ISMEDIAAPP);
        String transportType = prefs.getInt(
                Const.Transport.PREFS_KEY_TRANSPORT_TYPE,
                Const.Transport.PREFS_DEFAULT_TRANSPORT_TYPE) == Const.Transport.KEY_TCP ? "WiFi"
                : "BT";
        setTitle(getResources().getString(R.string.app_name) + " ("
                + (isMedia ? "" : "non-") + "media, "
                + transportType + ")");
    }

    /**
     * Go to previous Song from current one
     */
    public void jumpToPreviousSong() {
        if (PlayerService.service != null && (PlayerService.service.getPlayMode() == PlayMode.DISCOVERY_MUSIC || PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO)) {
            return;
        }
        if (!PlayerService.service.hasPrevious()) {
            Thread thread = new Thread() {
                public void run() {


                    final Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Do Work
                            if (PlayerService.service != null && PlayerService.service.getPlayMode() == PlayMode.MUSIC) {
                                PlayerService.service.playFromPositionNew(PlayerService.service.getPlayingQueue().size() - 1);
//							setTitleToFord();
                            }
                        }
                    });

                }
            };
            thread.start();
        } else {
            Thread thread = new Thread() {
                public void run() {


                    final Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Do Work
                            if (PlayerService.service != null) {
                                PlayerService.service.previous();
//							setTitleToFord();
                            }
                        }
                    });

                }
            };
            thread.start();

        }
    }

    /**
     * GO to next Song from the current one
     */

    public void jumpToNextSong() {
        if (PlayerService.service != null && PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
            return;
        }
        if (PlayerService.service != null && !PlayerService.service.hasNext()) {
            Thread thread = new Thread() {
                public void run() {


                    final Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Do Work
                            if (PlayerService.service != null && PlayerService.service.getPlayMode() == PlayMode.MUSIC) {
                                PlayerService.service.playFromPositionNew(0);
                            }

                        }
                    });

                }
            };
            thread.start();

        } else {

            Thread thread = new Thread() {
                public void run() {


                    final Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Do Work
                            if (PlayerService.service != null) {
                                PlayerService.service.next();
//							setTitleToFord();
                            }

                        }
                    });

                }
            };
            thread.start();
        }

    }

    public void seekBackwardCurrentPlayingSong() {
        // get current song position
//		int currentPosition = syncPlayer.getCurrentPosition();
//		// check if seekBackward time is greater than 0 sec
//		if(currentPosition - seekBackwardTime >= 0){
//			// forward song
//			syncPlayer.seekTo(currentPosition - seekBackwardTime);
//		}else{
//			// backward to starting position
//			syncPlayer.seekTo(0);
//		}
    }

    public void seekForwardCurrentPlayingSong() {

//		// get current song position
//		int currentPosition = syncPlayer.getCurrentPosition();
//		// check if seekForward time is lesser than song duration
//		if(currentPosition + seekForwardTime <= syncPlayer.getDuration()){
//			// forward song
//			syncPlayer.seekTo(currentPosition + seekForwardTime);
//		}else{
//			// forward to end position
//			syncPlayer.seekTo(syncPlayer.getDuration());
//		}
    }

    public void setPlayLoop() {
        if (PlayerService.service != null) {
            PlayerService.service.setLoopMode(PlayerService.LoopMode.ON);

        }
    }

    /**
     * Play/Pause current playing song
     */
    public void playPauseCurrentPlayingSong() {
        if (PlayerService.service != null && PlayerService.service.getState() == PlayerService.State.PLAYING) {
            ProxyService.getInstance().playingAudio = false;
            Thread thread = new Thread() {
                public void run() {


                    final Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Do Work
                            if (PlayerService.service != null) {
                                PlayerService.service.pause();
//								setTitleToFord();
                            }

                        }
                    });

                }
            };
            thread.start();

        }/*else if(PlayerService.service != null && PlayerService.service.getState()== PlayerService.State.STOPPED){
            ProxyService.getInstance().playingAudio = true;
            PlayerService.service.
        } */ else {
            ProxyService.getInstance().playingAudio = true;
            Thread thread = new Thread() {
                public void run() {


                    final Handler handler = new Handler(getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            // Do Work
                            if (PlayerService.service != null) {
                                PlayerService.service.play();
//								setTitleToFord();
                            }
                        }
                    });

                }
            };
            thread.start();

        }
    }

    /**
     * Title regarding current menu,album,radio type and song details
     */
    public void setTitleToFord() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {

                    {
                        Track track = PlayerService.service.getCurrentPlayingTrack();

                        if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO)
                            ProxyService.getProxyInstance().show("Live Radio -:", track.getTitle(), TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
                        else if (PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO)
                            ProxyService.getProxyInstance().show("OnDemand Radio -:", track.getTitle(), TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
                        else
                            ProxyService.getProxyInstance().show("Track No -:" + PlayerService.service.getCurrentPlayingTrackPosition(), track.getTitle(), TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
                    }


                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (SdlException e) {
                } catch (Exception e) {
                }
            }
        });
    }

    /**
     * Alert message regarding operation
     */
    public void showAlert(String txt) {
        Alert next = new Alert();
        next.setAlertText1(txt);
        next.setPlayTone(true);
        next.setDuration(1000);
        next.setCorrelationID(ProxyService.getInstance().nextCorrID());
        Vector<TTSChunk> ttsChunks = new Vector<TTSChunk>();
        ttsChunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT,
                txt));
        next.setTtsChunks(ttsChunks);
        try {
            ProxyService.getInstance()._syncProxy.sendRPCRequest(next);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Offline song list
     */
    public List<MediaItem> getOfflineSongs() {
        List<MediaItem> mMediaItemsMusic;
        if (CacheManager.isProUser(this))
            mMediaItemsMusic = DBOHandler.getAllCachedTracks(this);
        else
            mMediaItemsMusic = DBOHandler
                    .getAllOfflineTracksForFreeUser(this);
        return mMediaItemsMusic;
    }

    /**
     * Shuffle current list
     */
    public void setShuffle() {
        try {
            Analytics.logEvent(FlurryConstants.FlurryAllPlayer.Shuffle
                    .toString());

            String toastMessage = null;
            if (PlayerService.service.isShuffling()) {
                // sets any loop mode - OFF.
                toastMessage = Utils.getMultilanguageText(this, getResources()
                        .getString(R.string.player_shuffle_mode_off));
                PlayerService.service.stopShuffle();
            } else {
                // sets any loop mode - REPLAY SONG.
                toastMessage = Utils.getMultilanguageText(this, getResources()
                        .getString(R.string.player_shuffle_mode_on));
                PlayerService.service.startShuffle();
            }
            showAlert(toastMessage);
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    int lastPleaseWaitCorrelationId;

    /**
     * show alert before any operation
     */
    public void pleaseWaitMessage() {


        lastPleaseWaitCorrelationId = ProxyService.getInstance().nextCorrID();
        try {
            ProxyService.getProxyInstance().show("Please wait...", "", TextAlignment.CENTERED, lastPleaseWaitCorrelationId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show message on screen for empty queue
     */
    public void noSongsInPlayerQueue() {
        try {
            ProxyService.getProxyInstance().show("No song(s) in player queue", "", TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show message on screen for empty playlist
     */
    public void noPlayListToShow() {
        try {
            ProxyService.getProxyInstance().show("No playlist to show", "", TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show message on screen for empty mood
     */
    public void noMoodsToShow() {
        try {
            ProxyService.getProxyInstance().show("No moods to show", "", TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show message on screen for empty favorites list
     */
    public void noFavoritesToShow() {
        try {
            ProxyService.getProxyInstance().show("No favorites to show", "", TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show message on screen for empty offline list
     */
    public void noOfflineTracks() {
        try {
            ProxyService.getProxyInstance().show("No offline songs", "", TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMediaItemsLoaded(MediaType mediaType, String userId, List<MediaItem> mediaItems) {

    }

    /**
     * Receiver for connection
     */
    public class NoConnectionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            showNoConnectionMessage();
        }
    }

    /**
     * show message on screen for connection details
     */
    public void showNoConnectionMessage() {
        try {
            ProxyService.getProxyInstance().show("No internet connection", "", TextAlignment.CENTERED, ProxyService.getInstance().nextCorrID());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Title regarding current menu,album,radio type and song details
     */
    public void setTitleToFord(final Track track) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                try {
                    if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO)
                        ProxyService.getProxyInstance().show("Live Radio -:", track.getTitle(), TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
                    else if (PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO)
                        ProxyService.getProxyInstance().show("OnDemand Radio -:", track.getTitle(), TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
                    else {
                        ProxyService.getProxyInstance().show("Track No -:" + PlayerService.service.getCurrentPlayingTrackPosition(), track.getTitle(), TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (SdlException e) {
                }
            }
        });
    }

    /**
     * Playing song which is paused i.e. current song in list for music player mostly with name
     */
    public void playCurrentSong(final boolean isTextDisplay) {
        Thread thread = new Thread() {
            public void run() {

                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Do Work
                        if (PlayerService.service != null && PlayerService.service.getState() == PlayerService.State.PAUSED) {
                            PlayerService.service.play();
                        }
                        if (isTextDisplay)
                            setTitleToFord();
                    }
                });
            }
        };
        thread.start();

    }

    /**
     * Playing song which is paused i.e. current song in list for radio mostly without name
     */
    public void playCurrentSongWithoutText() {
        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Do Work
                        if (PlayerService.service != null && PlayerService.service.getState() != PlayerService.State.
                                PLAYING) {
                            PlayerService.service.play();
                        }
                    }
                });
            }
        };
        thread.start();

    }


    /**
     * Pause song which is playing i.e. current song
     */
    public void pauseCurrentSong() {
        pause();
    }

    /**
     * Release connection with media player on finish
     */
    public void release() {
        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Do Work
                        if (PlayerService.service != null)
                            PlayerService.service.destroyMediaPlayer();
                    }
                });

            }
        };
        thread.start();

        //}
    }

    /**
     * Pause song which is playing i.e. current song in foreground with handler
     */
    public void pause() {
        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Do Work
                        if (PlayerService.service != null) {
                            PlayerService.service.pause();
                        }

                    }
                });

            }
        };
        thread.start();

    }

    /**
     * Blocked else part to check number sequence error
     * currently making it work on 0 - 9 only
     **/
    /**
     * Hardware button click 0-9
     * Play track with particular track number
     * Check size of current list
     */
    public void playTrackNumber(final int trackNo) {

        if (PlayerService.service != null && PlayerService.service.getPlayingQueue().size() <= trackNo) {
            return;
        }

        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Do Work
                        if (PlayerService.service != null) {
                            PlayerService.service.playFromPosition(trackNo);
                        }
                    }
                });
            }
        };
        thread.start();
    }

    /**
     * Playing song with reference of track
     */
    public void playSelectedSong(final Track selectedTrack) {


        final List<Track> tracksInQueue = PlayerService.service.getPlayingQueue();

        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Do Work
                        // Check whether track in Queue or Not.
                        if (PlayerService.service.getPlayMode() == PlayMode.MUSIC) {
                            if (PlayerService.service.isPlaying() &&
                                    PlayerService.service.getCurrentPlayingTrack().getId() == selectedTrack.getId()) {
                                if (PlayerService.service.getState() == PlayerService.State.PLAYING) {
                                    //Toast.makeText(getActivity(), "Already Playting track", Toast.LENGTH_SHORT).show();
                                } else {
                                    PlayerService.service.play();
                                }
                            } else if ((tracksInQueue != null)
                                    && (tracksInQueue.contains(selectedTrack))) {
                                final int trackPos = tracksInQueue.indexOf(selectedTrack);
                                PlayerService.service.playFromPositionNew(trackPos);
                            } else {
                                PlayerService.service.playNow(Collections.singletonList(selectedTrack));
                            }
                        } else {
                            PlayerService.service.playNow(Collections.singletonList(selectedTrack));
                        }
                    }
                });
            }
        };
        thread.start();

        setTitleToFord(selectedTrack);
//		mListener.onPlayTrackFromDetail();
//        }

    }

    /**
     * Play current song with reference number if available
     */
    public void playCurrentSong(int songIndex) {
        //Capturing the current song number
        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Do Work
                        if (PlayerService.service != null) {
                            PlayerService.service.play();
//							setTitleToFord();
                        }

                    }
                });


            }
        };
        thread.start();
    }

    @Override
    public void onStartLoadingTrack(Track track) {
        pleaseWaitMessage();
    }

    @Override
    public void onTrackLoadingBufferUpdated(Track track, int precent) {

    }

    @Override
    public void onStartPlayingTrack(Track track) {
        setTitleToFord();
    }

    @Override
    public void onFinishPlayingTrack(Track track) {

    }

    @Override
    public void onFinishPlayingQueue() {

    }

    @Override
    public void onSleepModePauseTrack(Track track) {

    }

    @Override
    public void onErrorHappened(PlayerService.Error error) {

        Logger.e("On Error", " On error" + error);
        if (error == PlayerService.Error.NO_CONNECTIVITY) {
            ProxyService serviceInstance = ProxyService.getInstance();
            serviceInstance.showNoConnectionMessage();
        } else if (error == PlayerService.Error.TRACK_SKIPPED) {
            if (PlayerService.service != null)
                if (PlayerService.service.getPlayingQueue().size() - 1 == PlayerService.service.getCurrentPlayingTrackPosition()) {
                    if (!Utils.isConnected()) {
                        ProxyService serviceInstance = ProxyService.getInstance();
                        serviceInstance.showNoConnectionMessage();
                    }
                }
        }
    }

    @Override
    public void onStartPlayingAd(Placement audioad) {

    }

    @Override
    public void onAdCompletion() {

    }

    /**
     * Live radio
     */
    public void playLiveStation(final MediaItem mediaItem) {

        final LiveStation liveStation = (LiveStation) mediaItem;
        final Track liveStationTrack;
        liveStationTrack = new Track(liveStation.getId(),
                liveStation.getTitle(), liveStation.getDescription(), null,
                liveStation.getImageUrl(), liveStation.getImageUrl(),
                mediaItem.getImages(), mediaItem.getAlbumId());
        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {


                        if (CacheManager.isProUser(LockScreenActivity.this)
                                && !TextUtils.isEmpty(liveStation.getStreamingUrl_320())) {
                            liveStationTrack.setMediaHandle(liveStation.getStreamingUrl_320());
                        } else if (CacheManager.isProUser(LockScreenActivity.this)
                                && !TextUtils.isEmpty(liveStation.getStreamingUrl_128())) {
                            liveStationTrack.setMediaHandle(liveStation.getStreamingUrl_128());
                        } else {
                            liveStationTrack.setMediaHandle(liveStation.getStreamingUrl());
                        }

                        List<Track> liveStationList = new ArrayList<Track>();
                        liveStationList.add(liveStationTrack);

		/*
         * sets to each track a reference to a copy of the original radio item.
		 * This to make sure that the player bar can get source Radio item
		 * without leaking this activity!
		 */
                        for (Track track : liveStationList) {
                            track.setTag(liveStation);
                        }

                        // starts to play.
                        PlayerService.service.playRadio(liveStationList, PlayMode.LIVE_STATION_RADIO);
                        setTitleToFord(liveStationTrack);

                    }
                });
            }
        };
        thread.start();

    }

    /**
     * On demand radio
     */
    public void playOndemandRadio(final List<Track> trackList) {

        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {


                        // starts to play.
                        PlayerService.service.playRadio(trackList, PlayMode.TOP_ARTISTS_RADIO);
                        setTitleToFord(trackList.get(0));

                    }
                });
            }
        };
        thread.start();

    }

    /**
     * Favorite by media
     */
    public void favoriteItem(final MediaItem mediaItem) {
        Thread thread = new Thread() {
            public void run() {

                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        HomeActivity.Instance.mDataManager.addToFavorites(
                                String.valueOf(mediaItem.getId()), mediaItem.getMediaType().toString(),
                                LockScreenActivity.this);
                    }
                });
            }
        };
        thread.start();
    }


    /**
     * Favorite by type and id
     */
    public void favoriteItem(final long id, final String type) {

        Thread thread = new Thread() {
            public void run() {

                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        HomeActivity.Instance.mDataManager.addToFavorites(
                                String.valueOf(id), type,
                                LockScreenActivity.this);
                    }
                });
            }
        };
        thread.start();
    }

    /**
     * Offline music by media
     */
    public void offlineMusic(final MediaItem mMediaItem) {

        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        if (CacheManager.isProUser(LockScreenActivity.this)) {
                            if (mMediaItem.getMediaType() == MediaType.TRACK) {
                                DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(
                                        LockScreenActivity.this, "" + mMediaItem.getId());
                                if (cacheState == DataBase.CacheState.CACHED) {
//                        mActionButtonSaveOffline.setTag(true);


                                } else if (cacheState == DataBase.CacheState.CACHING
                                        || cacheState == DataBase.CacheState.QUEUED) {
//                        mActionButtonSaveOffline.setTag(null);

                                } else {
                                    Track track = new Track(mMediaItem.getId(),
                                            mMediaItem.getTitle(), mMediaItem.getAlbumName(),
                                            mMediaItem.getArtistName(), mMediaItem.getImageUrl(),
                                            mMediaItem.getBigImageUrl(), mMediaItem.getImages(),
                                            mMediaItem.getAlbumId());
                                    CacheManager.saveOfflineAction(LockScreenActivity.this, mMediaItem, track);
                                }

                            } else if (mMediaItem.getMediaType() == MediaType.ALBUM) {
                                DataBase.CacheState cacheState = DBOHandler.getAlbumCacheState(
                                        LockScreenActivity.this, "" + mMediaItem.getId());
                                if (cacheState == DataBase.CacheState.CACHED) {
                                    int trackCacheCount = DBOHandler.getAlbumCachedCount(
                                            LockScreenActivity.this, "" + mMediaItem.getId());
                                    if (trackCacheCount >= mMediaItem.getMusicTrackCount()) {
//                            mActionButtonSaveOffline.setTag(true);
                                    }


                                } else if (cacheState == DataBase.CacheState.CACHING
                                        || cacheState == DataBase.CacheState.QUEUED) {
//                        mActionButtonSaveOffline.setTag(null);

                                } else {
                                    HomeActivity.Instance.mDataManager.getMediaDetails(mMediaItem,
                                            PlayerOption.OPTION_SAVE_OFFLINE, LockScreenActivity.this);
                                }

                            } else if (mMediaItem.getMediaType() == MediaType.PLAYLIST) {
                                DataBase.CacheState cacheState = DBOHandler.getPlaylistCacheState(
                                        LockScreenActivity.this, "" + mMediaItem.getId());
                                // System.out.println("PLAYLIST :::::::::::::::::::::::: " +
                                // cacheState);
                                if (cacheState == DataBase.CacheState.CACHED) {

                                    int trackCacheCount = DBOHandler
                                            .getPlaylistCachedCount(LockScreenActivity.this, ""
                                                    + mMediaItem.getId());
                                    if (trackCacheCount >= mMediaItem.getMusicTrackCount()) {
//                            mActionButtonSaveOffline.setTag(true);
                                    }


                                } else if (cacheState == DataBase.CacheState.CACHING
                                        || cacheState == DataBase.CacheState.QUEUED) {
//                        mActionButtonSaveOffline.setTag(null);


                                } else {
                                    HomeActivity.Instance.mDataManager.getMediaDetails(mMediaItem,
                                            PlayerOption.OPTION_SAVE_OFFLINE, LockScreenActivity.this);
                                }
                            }
                        } else {
                            // // TODO: 26/5/16
                            showMessageGoPro();
                        }


                    }
                });
            }
        };
        thread.start();

    }

    /**
     * Saving track to offline by track
     */
    public void saveTrackOffline(final Track track) {

        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        MediaItem mediaItem = new MediaItem(track.getId(), track.getTitle(),
                                track.getAlbumName(), track.getArtistName(),
                                track.getImageUrl(), track.getBigImageUrl(), MediaType.TRACK
                                .name().toLowerCase(), 0, 0, track.getImages(),
                                track.getAlbumId());

                        if (CacheManager.isProUser(LockScreenActivity.this)) {
                            if (mediaItem.getMediaType() == MediaType.TRACK) {
                                DataBase.CacheState cacheState = DBOHandler.getTrackCacheState(
                                        LockScreenActivity.this, "" + mediaItem.getId());
                                if (cacheState == DataBase.CacheState.CACHED) {
//                        mActionButtonSaveOffline.setTag(true);


                                } else if (cacheState == DataBase.CacheState.CACHING
                                        || cacheState == DataBase.CacheState.QUEUED) {
//                        mActionButtonSaveOffline.setTag(null);

                                } else {
                                    Track track = new Track(mediaItem.getId(),
                                            mediaItem.getTitle(), mediaItem.getAlbumName(),
                                            mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                            mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                            mediaItem.getAlbumId());
                                    CacheManager.saveOfflineAction(LockScreenActivity.this, mediaItem, track);
                                }

                            } else if (mediaItem.getMediaType() == MediaType.ALBUM) {
                                DataBase.CacheState cacheState = DBOHandler.getAlbumCacheState(
                                        LockScreenActivity.this, "" + mediaItem.getId());
                                if (cacheState == DataBase.CacheState.CACHED) {
                                    int trackCacheCount = DBOHandler.getAlbumCachedCount(
                                            LockScreenActivity.this, "" + mediaItem.getId());
                                    if (trackCacheCount >= mediaItem.getMusicTrackCount()) {
//                            mActionButtonSaveOffline.setTag(true);
                                    }


                                } else if (cacheState == DataBase.CacheState.CACHING
                                        || cacheState == DataBase.CacheState.QUEUED) {
//                        mActionButtonSaveOffline.setTag(null);

                                } else {
                                    HomeActivity.Instance.mDataManager.getMediaDetails(mediaItem,
                                            PlayerOption.OPTION_SAVE_OFFLINE, LockScreenActivity.this);
                                }

                            } else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
                                DataBase.CacheState cacheState = DBOHandler.getPlaylistCacheState(
                                        LockScreenActivity.this, "" + mediaItem.getId());
                                // System.out.println("PLAYLIST :::::::::::::::::::::::: " +
                                // cacheState);
                                if (cacheState == DataBase.CacheState.CACHED) {

                                    int trackCacheCount = DBOHandler
                                            .getPlaylistCachedCount(LockScreenActivity.this, ""
                                                    + mediaItem.getId());
                                    if (trackCacheCount >= mediaItem.getMusicTrackCount()) {
//                            mActionButtonSaveOffline.setTag(true);
                                    }


                                } else if (cacheState == DataBase.CacheState.CACHING
                                        || cacheState == DataBase.CacheState.QUEUED) {
//                        mActionButtonSaveOffline.setTag(null);


                                } else {
                                    HomeActivity.Instance.mDataManager.getMediaDetails(mediaItem,
                                            PlayerOption.OPTION_SAVE_OFFLINE, LockScreenActivity.this);
                                }
                            }
                        } else {
                            // // TODO: 26/5/16

                            showMessageGoPro();

                        }


                    }
                });
            }
        };
        thread.start();


    }

    /**
     * Saving track to offline by track list
     */
    public void saveAllTracksOffline(List<Track> tracksList) {

        if (CacheManager.isProUser(LockScreenActivity.this)) {
            if (tracksList != null && tracksList.size() > 0) {
                CacheManager.saveAllTracksOfflineAction(LockScreenActivity.this, tracksList);
            }
        } else {
            showMessageGoPro();

        }

    }

    /**
     * Message regarding user status
     */
    private void showMessageGoPro() {

        try {

            ProxyService.getProxyInstance().show("Not Pro User.", "", TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_IMAGE_BIG = "image_big";
    private static final String KEY_IMAGE_SMALL = "image_small";

    /**
     * Fetch all moods available
     */
    public List<Mood> getMoods() {
        List<Mood> mMoods = new ArrayList<Mood>();
        mMoods = HomeActivity.Instance.mDataManager.getStoredMoods();

        if (mMoods == null || Utils.isListEmpty(mMoods)) {
            mMoods = new ArrayList<Mood>();
            JSONParser jsonParser = new JSONParser();
            String response = readFileFromAssets();


            Map<String, Object> reponseMap = null;
            try {
                reponseMap = (Map<String, Object>) jsonParser
                        .parse(response);


                reponseMap = (Map<String, Object>) reponseMap.get("response");
                Map<String, Object> moodsMap = (Map<String, Object>) reponseMap
                        .get("moods");
                Map<String, Object> tagsMap = (Map<String, Object>) reponseMap
                        .get("tags");

                List<Map<String, Object>> moodsMapList = (List<Map<String, Object>>) moodsMap
                        .get("mood");
                List<Map<String, Object>> tagsMapList = (List<Map<String, Object>>) tagsMap
                        .get("tag");

                int id;
                String name;
                String bigImageUrl;
                String smallImageUrl;

                for (Map<String, Object> map : moodsMapList) {
                    id = ((Long) map.get(KEY_ID)).intValue();
                    name = (String) map.get(KEY_NAME);
                    bigImageUrl = (String) map.get(KEY_IMAGE_BIG);
                    smallImageUrl = (String) map.get(KEY_IMAGE_SMALL);
                    mMoods.add(new Mood(id, name, bigImageUrl, smallImageUrl));
                }

                for (Map<String, Object> map : tagsMapList) {
                    id = 0; // tags don't contain any id.
                    name = (String) map.get(KEY_NAME);
                    bigImageUrl = (String) map.get(KEY_IMAGE_BIG);
                    smallImageUrl = (String) map.get(KEY_IMAGE_SMALL);
                    mMoods.add(new Mood(id, name, bigImageUrl, smallImageUrl));
                }
                HomeActivity.Instance.mDataManager.storeMoods(mMoods);
                return mMoods;
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            return mMoods;
        }
        return null;
    }

    Discover discover;
    private DiscoverSearchResultIndexer mDiscoverSearchResultIndexer;

    /**
     * fetching list according to moods for discover
     */
    public void moodViseApiCAll(Mood mood) {
        if (mood != null) {
            discover = Discover.createNewDiscover();
            discover.setMood(mood);
            discover.setHashTag(null);
            HomeActivity.Instance.mDataManager.getDiscoverSearchResult(discover,
                    mDiscoverSearchResultIndexer, this);

        }
    }

    /**
     * Files from local storage
     */
    private String readFileFromAssets() {

        try {
            InputStream input = getAssets().open("moods.json");

            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            // byte buffer into a string
            String text = new String(buffer);
            return text;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Adding all list to queue and playing first
     */
    public void onMediaItemOptionPlayNowAllSelected(final MediaItem mediaItem, final boolean isAddToQueue) {

        Thread thread = new Thread() {
            public void run() {


                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {


                        Logger.i(TAG, "Add to queue: " + mediaItem.getId());
                        if (mediaItem.getMediaContentType() == MediaContentType.MUSIC) {

                            boolean isCached = false;
                            String mediaDeatils = null;
                            if (mediaItem.getMediaType() == MediaType.ALBUM)
                                mediaDeatils = DBOHandler.getAlbumDetails(LockScreenActivity.this, ""
                                        + mediaItem.getId());
                            else if (mediaItem.getMediaType() == MediaType.PLAYLIST)
                                mediaDeatils = DBOHandler.getPlaylistDetails(LockScreenActivity.this, ""
                                        + mediaItem.getId());
                            if (mediaDeatils != null && mediaDeatils.length() > 0) {
                                MediaDetailsOperation mediaDetailsOperation;
                                if (isAddToQueue) {
                                    mediaDetailsOperation = new MediaDetailsOperation(
                                            "", "", "", mediaItem,
                                            PlayerOption.OPTION_ADD_TO_QUEUE, null);
                                } else {
                                    mediaDetailsOperation = new MediaDetailsOperation(
                                            "", "", "", mediaItem,
                                            PlayerOption.OPTION_PLAY_NOW_AND_OPEN, null);
                                }

                                try {
                                    CommunicationManager.Response res = new CommunicationManager.Response();
                                    res.response = mediaDeatils;
                                    res.responseCode = CommunicationManager.RESPONSE_SUCCESS_200;
                                    onSuccess(mediaDetailsOperation.getOperationId(),
                                            mediaDetailsOperation.parseResponse(res));
                                } catch (InvalidRequestParametersException e) {
                                    e.printStackTrace();
                                } catch (InvalidRequestTokenException e) {
                                    e.printStackTrace();
                                } catch (InvalidResponseDataException e) {
                                    e.printStackTrace();
                                } catch (OperationCancelledException e) {
                                    e.printStackTrace();
                                }
                                isCached = true;
                            }

                            if (!isCached) {
                                if (isAddToQueue)
                                    HomeActivity.Instance.mDataManager.getMediaDetails(mediaItem,
                                            PlayerOption.OPTION_ADD_TO_QUEUE, LockScreenActivity.this);
                                else
                                    HomeActivity.Instance.mDataManager.getMediaDetails(mediaItem,
                                            PlayerOption.OPTION_PLAY_NOW_AND_OPEN, LockScreenActivity.this);
                            }
                        }
                    }
                });
            }
        };
        thread.start();
    }

    /**
     * Soft button on screen corresponding to current play
     */
    public void setMusicSoftbutton() {
        Handler handlerPopular = new Handler(getMainLooper());
        handlerPopular.postDelayed(new Runnable() {
            @Override
            public void run() {
                ProxyService.getInstance().showSoftButtonsOnScreen();

            }
        }, 2000);
    }

    // ======================================================
    // Communication Operation listeners.
    // ======================================================
    public MediaType mediaType = null;


    @Override
    public void onStart(int operationId) {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        try {

            Logger.s("1 HomeTime:   onSuccess" + operationId);

            if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
                try {

                    // findViewById(R.id.progressbar).setVisibility(View.GONE);
                    Logger.i("MediaTilesAdapter",
                            "Play button click: Media detail OnSuccess 4");
                    MediaItem mediaItem = (MediaItem) responseObjects
                            .get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_ITEM);

                    if (mediaItem != null
                            && (mediaItem.getMediaType() == MediaType.ALBUM || mediaItem
                            .getMediaType() == MediaType.PLAYLIST)) {
                        MediaSetDetails setDetails = (MediaSetDetails) responseObjects
                                .get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
                        PlayerOption playerOptions = (PlayerOption) responseObjects
                                .get(MediaDetailsOperation.RESPONSE_KEY_PLAYER_OPTION);
                        Logger.i("MediaTilesAdapter",
                                "Play button click: Media detail OnSuccess 5");
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
                        Logger.i("MediaTilesAdapter",
                                "Play button click: Media detail OnSuccess 6");
                        if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {
                            HomeActivity.Instance.mPlayerBar.playNow(tracks, null, null);
                            setMusicSoftbutton();
                        } else if (playerOptions == PlayerOption.OPTION_PLAY_NOW_AND_OPEN) {
                            HomeActivity.Instance.mPlayerBar.playNow(tracks, null, null);
                            setMusicSoftbutton();
                        } else if (playerOptions == PlayerOption.OPTION_PLAY_NEXT) {
                            HomeActivity.Instance.mPlayerBar.playNext(tracks);
                            setMusicSoftbutton();
                        } else if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
                            HomeActivity.Instance.mPlayerBar.addToQueue(tracks, null, null);
//                            resources
//                                    .getString(R.string.main_player_bar_message_songs_added_to_queue)
                        } else if (playerOptions == PlayerOption.OPTION_SAVE_OFFLINE) {
                            if (mediaItem.getMediaType() == MediaType.ALBUM) {
                                for (Track track : tracks) {
                                    track.setTag(mediaItem);
                                }
                            }
                            CacheManager.saveAllTracksOfflineAction(this,
                                    tracks);
                        }
                    }
                } catch (Exception e) {
                    Logger.e(getClass().getName() + ":438", e.toString());
                }
            } else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
                BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects
                        .get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);
                // has the item been added from favorites.
                if (addToFavoriteResponse.getCode() == FAVORITE_SUCCESS) {
                    showAlert(addToFavoriteResponse.getMessage());
                } else {
                    showAlert("Unable to Favorite");
                }

            } else if (operationId == OperationDefinition.Hungama.OperationId.SOCIAL_PROFILE_FAVORITE_MEDIA_ITEMS) {
                MediaType mediType;


                try {
                    ProfileFavoriteMediaItems profileFavoriteMediaItems = (ProfileFavoriteMediaItems) responseObjects
                            .get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_PROFILE_FAVORITE_MEDIA_ITEMS);
                    List<MediaItem> mMediaItems = profileFavoriteMediaItems.mediaItems;
                    if (mMediaItems != null) {
                        for (MediaItem item : mMediaItems) {
                            if (item.getMediaType() == MediaType.ARTIST)
                                item.setMediaContentType(MediaContentType.RADIO);
                        }
                    } else {
                        mediType = mediaType;

                        return;
                    }
                    Logger.e("mMediaItems size", "" + mMediaItems.size());

                    ArrayList<Object> mediaitemMusic = new ArrayList<Object>();
                    mediaitemMusic.clear();

                    List<MediaItem> tracks = new ArrayList<MediaItem>();
                    List<MediaItem> playlists = new ArrayList<MediaItem>();

                    for (MediaItem mediaItem : mMediaItems) {
                        if (mediaItem.getMediaType() == MediaType.TRACK) {
                            tracks.add(mediaItem);
                        } else if (mediaItem.getMediaType() == MediaType.ALBUM) {
                            tracks.add(mediaItem);
                        } else if (mediaItem.getMediaType() == MediaType.ARTIST) {
                            tracks.add(mediaItem);
                        } else if (mediaItem.getMediaType() == MediaType.PLAYLIST) {
                            playlists.add(mediaItem);
                        }
                    }

                    List<MediaItem> favoriteMediaItemsList = new ArrayList<>();
                    if (tracks != null && tracks.size() > 0) {
                        favoriteMediaItemsList = tracks;
                    }

                    if (playlists != null && playlists.size() > 0) {
                        favoriteMediaItemsList = playlists;
                    }

                    ProxyService.getInstance().addFavoriteItemsToChoiceSet(favoriteMediaItemsList);


                    mediType = (MediaType) responseObjects
                            .get(SocialProfileFavoriteMediaItemsOperation.RESULT_KEY_MEDIA_TYPE);
//                    setMediaItems(mediaitemMusic);
                    // updates the tile's grid.


                } catch (Exception e) {
                }
            } else if (operationId == OperationDefinition.Hungama.OperationId.DISCOVER_SEARCH_RESULT) {
                mDiscoverSearchResultIndexer = (DiscoverSearchResultIndexer) responseObjects
                        .get(DiscoverSearchResultsOperation.RESULT_KEY_DISCOVER_SEARCH_RESULT_INDEXER);
                List<MediaItem> mediaItems;
                mediaItems = (List<MediaItem>) responseObjects
                        .get(DiscoverSearchResultsOperation.RESULT_KEY_MEDIA_ITEMS);

                Set<String> tags = Utils.getTags();
                if (!tags.contains("discover_used")) {
                    tags.add("discover_used");
                    Utils.AddTag(tags);
                }
                mMediaItems = mediaItems;
                if (PlayerService.service != null
                        && PlayerService.service.mDiscover != null)
                    PlayerService.service.prevDiscover = PlayerService.service.mDiscover
                            .newCopy();
                if (PlayerService.service != null)
                    PlayerService.service.mDiscover = discover
                            .newCopy();

                Thread thread = new Thread() {
                    public void run() {
                        final Handler handler = new Handler(getMainLooper());
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                HomeActivity.Instance.mPlayerBar.setDiscovery(discover);
                                HomeActivity.Instance.mPlayerBar.playDiscoveryMusic(
                                        getTracks(), PlayMode.DISCOVERY_MUSIC);
                                setMusicSoftbutton();

                            }
                        });
                    }
                };
                thread.start();


            }


        } catch (Exception e) {
            Logger.printStackTrace(e);
        }

    }


    List<MediaItem> mMediaItems;

    /**
     * Fetch all track list random
     */
    private List<Track> getTracks() {
        if (!Utils.isListEmpty(mMediaItems)) {
            List<Track> tracks = new ArrayList<Track>();
            for (MediaItem mediaItem : mMediaItems) {
                if (!TextUtils.isEmpty(mediaItem.getTitle())
                        && mediaItem.getTitle().equalsIgnoreCase("no")
                        && !TextUtils.isEmpty(mediaItem.getAlbumName())
                        && mediaItem.getAlbumName().equalsIgnoreCase("no")
                        && !TextUtils.isEmpty(mediaItem.getArtistName())
                        && mediaItem.getArtistName().equalsIgnoreCase("no")) {
                } else {
                    Track track = new Track(mediaItem.getId(),
                            mediaItem.getTitle(), mediaItem.getAlbumName(),
                            mediaItem.getArtistName(), mediaItem.getImageUrl(),
                            mediaItem.getBigImageUrl(), mediaItem.getImages(),
                            mediaItem.getAlbumId());
                    tracks.add(track);
                }
            }

            return tracks;
        }

        return null;
    }


    @Override
    public void onFailure(int operationId, CommunicationManager.ErrorType errorType, String errorMessage) {

    }


// ======================================================
// Get Playlist
// ======================================================

    public ArrayList<Playlist> loadPlayList() {
        ArrayList<MediaItem> mListMediaItems = new ArrayList<MediaItem>();
        ArrayList<Playlist> mListPlaylists = new ArrayList<Playlist>();

        Map<Long, Playlist> map = HomeActivity.Instance.mDataManager.getStoredPlaylists();
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

        }

        return mListPlaylists;

    }

    /**
     * Fetch all track list of current playlist
     */
    public List<Track> getTracksListByPlaylist(Playlist playlist) {

        List<Track> playlistTracks = new ArrayList<Track>();

        // Get all Tracks
        Map<Long, Track> allTracks = HomeActivity.Instance.mDataManager.getStoredTracks();

        // Get the selected playlist's tracks
        String tracks = playlist.getTrackList();
        String tracksArr[] = null;
        if (!TextUtils.isEmpty(tracks)) {
            tracksArr = tracks.split(" ");
        }

        // Loop all tracks and add to itemables the tracks that belong to the
        // selected playlist
        if (allTracks != null) {
            if (tracksArr != null && tracksArr.length > 0) {

                for (int index = 0; tracksArr.length > index; index++) {
                    if (TextUtils.isEmpty(tracksArr[index]))
                        continue;
                    long id = Long.parseLong(tracksArr[index]);
                    Track t = allTracks.get(id);
                    if (t != null) {
                        playlistTracks.add(t);
                        // System.out.println("t1 list ::::::::::::: " + new
                        // Gson().toJson(t));
                    }
                }
            }
        }
        // System.out.println("playlist list ::::::::::::: " + new
        // Gson().toJson(playlistTracks));
        return playlistTracks;
    }


// ================================
// Get Favorite Details
// ================================

    String mUserId = "";

    /**
     * fetching my favorite track list
     */
    public void getFavoriteList(final MediaType mediaType) {
        final String userID = HomeActivity.Instance.mDataManager.getApplicationConfigurations().getPartnerUserId();
        mUserId = userID;

        Thread thread = new Thread() {
            public void run() {
                final Handler handler = new Handler(getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        HomeActivity.Instance.mDataManager.getFavorites(getApplicationContext(), mediaType, userID, LockScreenActivity.this);
                    }
                });
            }
        };
        thread.start();

    }

    //===========================
    // Change Language
    //===========================

    /**
     * Setting current language selected
     */
    public void setEditorPics(int itemPosition) {
        ((MainActivity) HomeActivity.Instance).onItemSelectedPosition(itemPosition);
    }

    /**
     * Saving current language selected to shared preference
     */
    public void saveLanguageName(int itemPosition, String languageName) {
        HomeActivity.Instance.mDataManager.getApplicationConfigurations()
                .setSelctedMusicPreference(languageName);
        HomeActivity.Instance.savePreferences(itemPosition, languageName, null, "", true);

    }

    public List<String> getEditorPics() {

        List<String> mCategories = new ArrayList<>();
        MusicCategoriesResponse musicCategoriesResponse = null;
        DataManager mDataManager = DataManager.getInstance(this);
        final String preferencesResponse = mDataManager
                .getApplicationConfigurations()
                .getMusicPreferencesResponse();
        try {
            musicCategoriesResponse = new Gson().fromJson(
                    preferencesResponse.toString(),
                    MusicCategoriesResponse.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (musicCategoriesResponse != null)
            mCategories = musicCategoriesResponse.getCategories();
        return mCategories;
    }


    //==================================================
    //Repeat Mode
    //==================================================

    /**
     * Repeting current song
     */
    public void setRepeat() {
        try {
            Analytics.logEvent(FlurryConstants.FlurryAllPlayer.OnLoop
                    .toString());

            // gets the new state of the button.
            PlayerService.LoopMode mode = PlayerService.service.getLoopMode();

           /* if (mode == PlayerService.LoopMode.ON) {
                // sets any loop mode - OFF.
                PlayerService.service.setLoopMode(PlayerService.LoopMode.OFF);

            } else if (mode == PlayerService.LoopMode.OFF) {
                // sets any loop mode - REPLAY SONG.
                PlayerService.service.setLoopMode(PlayerService.LoopMode.REAPLAY_SONG);

            } else {
                // sets any loop mode - ON.
                PlayerService.service.setLoopMode(PlayerService.LoopMode.ON);
            }*/

            if (mode == PlayerService.LoopMode.ON) {
                PlayerService.service.setLoopMode(PlayerService.LoopMode.ON);

            } else if (mode == PlayerService.LoopMode.REAPLAY_SONG) {
                PlayerService.service.setLoopMode(PlayerService.LoopMode.REAPLAY_SONG);

            } else if (mode == PlayerService.LoopMode.OFF) {
                PlayerService.service.setLoopMode(PlayerService.LoopMode.OFF);

            }
//            List<Track> mTracks = PlayerService.service.getPlayingQueue();
//            mQueueAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

}
