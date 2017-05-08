package com.hungama.hungamamusic.ford.carmode;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Mood;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.fragments.BrowseRadioFragment;
import com.hungama.myplay.activity.ui.fragments.HomeMediaTileGridFragmentNew;
import com.hungama.myplay.activity.ui.widgets.HomeTabBar;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.exception.SdlExceptionCause;
import com.smartdevicelink.proxy.RPCMessage;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCRequestFactory;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.TTSChunkFactory;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.Alert;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
import com.smartdevicelink.proxy.rpc.ChangeRegistrationResponse;
import com.smartdevicelink.proxy.rpc.Choice;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteCommandResponse;
import com.smartdevicelink.proxy.rpc.DeleteFileResponse;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteSubMenuResponse;
import com.smartdevicelink.proxy.rpc.DiagnosticMessageResponse;
import com.smartdevicelink.proxy.rpc.DialNumberResponse;
import com.smartdevicelink.proxy.rpc.EndAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.GenericResponse;
import com.smartdevicelink.proxy.rpc.GetDTCsResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.ListFilesResponse;
import com.smartdevicelink.proxy.rpc.OnAudioPassThru;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnButtonPress;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnDriverDistraction;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnHashChange;
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
import com.smartdevicelink.proxy.rpc.OnStreamRPC;
import com.smartdevicelink.proxy.rpc.OnSystemRequest;
import com.smartdevicelink.proxy.rpc.OnTBTClientState;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.PerformInteractionResponse;
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SoftButton;
import com.smartdevicelink.proxy.rpc.Speak;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.TTSChunk;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.enums.ButtonName;
import com.smartdevicelink.proxy.rpc.enums.InteractionMode;
import com.smartdevicelink.proxy.rpc.enums.Language;
import com.smartdevicelink.proxy.rpc.enums.Result;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.proxy.rpc.enums.SoftButtonType;
import com.smartdevicelink.proxy.rpc.enums.SpeechCapabilities;
import com.smartdevicelink.proxy.rpc.enums.SystemAction;
import com.smartdevicelink.proxy.rpc.enums.TextAlignment;
import com.smartdevicelink.transport.TCPTransportConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class ProxyService extends Service implements IProxyListenerALM, GetAubumDetails.AlbumDetailCallBack {
    static final String TAG = "SyncMusciPlayer";
    private Integer autoIncCorrId = 1;
    AudioManager audioManager;
    private static ProxyService _instance;
    private static LockScreenActivity _mainInstance;
    public static SdlProxyALM _syncProxy;
    private BluetoothAdapter mBtAdapter;
    public Boolean playingAudio = false;
    protected SyncReceiver mediaButtonReceiver;
    //variable to contain the current state of the lockscreen
    private boolean lockscreenUP = false;
    private boolean firstHMIStatusChange = true;
    private static boolean waitingForResponse = false;

    public int trackNumber = 1;
    //Voice cmd implementation
    private Integer autoIncCNDCorrId = 1001;
    private Integer choiceId = 1010;
    private Integer choiceSetId = 1020;
    private Integer interactionChoiceSetID = 1030;
    //    private int lastIndexOfSongChoiceId;
    private SoftButton next, previous, /*appInfo,*/
            Shuffle, Repeat, Queue, favorite, offline, change_language;/*, cmdInfo, scrollableMsg, APTHCheck, vehicleData*/
    ;


    //private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

    private List<Track> mTracks;
    private Integer playerQueueIdLast = 0;
    private int corrRelIdLiveRadio = 0;
    private int corrRelIDAlbumPopular = 0;
    private int corrRelIdOnDemandRadio = 0;

    boolean isPauseFromVoiceCommand = false;

    private int MUSIC_CHOICESET_ID = 104;
    private int MUSIC_NEWMUSIC_CHOICESET_ID = 1041;
    private int MUSIC_POPULAR_CHOICESET_ID = 1042;
    private int MUSIC_DISCOVERY_CHOICESET_ID = 1043;

    private int CONTROL_CHOICESET_ID = 600011;
    private int CONTROL_PLAY_CHOICESET_ID = 00001;
    private int CONTROL_PAUSE_CHOICESET_ID = 00002;
    private int CONTROL_NEXT_CHOICESET_ID = 00003;
    private int CONTROL_PREVIOUS_CHOICESET_ID = 00004;


    private int MUSIC_PLAYER_CHOICE_SET_ID_QUEUE = 10041;
    private int MUSIC_PLAYER_CHOICE_SET_ID_FAV = 10042;
    private int MUSIC_PLAYER_CHOICE_SET_ID_OTHERS = 10043;


    private int RADIO_CHOICESET_ID = 105;
    private int RADIO_LIVE_CHOICESET_ID = 1051;
    private int RADIO_ON_DEMAND_CHOICESET_ID = 1052;

    private int PLAYLIST_CHOICESET_ID = 106;
    private int MY_PLAYLIST_SELECTION_ID = 1006;

    private int FAVORITE_CHOICESET_ID = 108;
    private int FAVORITE_ALBUM_CHOICESET_ID = 1081;
    private int FAVORITE_SONGS_CHOICESET_ID = 1082;
    private int FAVORITE_PLAYLIST_CHOICESET_ID = 1083;
    private int FAVORITE_ARTIST_RADIO_CHOICESET_ID = 1084;

    private int MOODS_CHOICESET_ID = 110;
    private int OFFLINE_CHOICESET_ID = 109;
    int OFFLINE_PLAYALL_CHOICEID = 1091;
    private int OFFLINE_SECTION_ID = 10091;


    private int CHANGE_LANGUAGE_CHOICESET_ID = 111;
    int CHANGE_LANGUAGE_SELECTION_CHOICESET_ID = 1111;
    int LANGUAGE_SELECTION_ID = 1112;

    private int REPEAT_CHOICESET_ID = 112;


    SELECTION currentSelection;

    public enum SELECTION {
        NEW_MUSIC, POPULAR_MUSIC, DISCOVER, QUEUE;
    }

    FAVORITE_SELECTION favoriteSelection;

    public enum FAVORITE_SELECTION {
        ALBUM, SONGS, PLAYLIST, ARTIST_RADIO;
    }

    MOODS_SELECTION moodSelection;

    public enum MOODS_SELECTION {
        HEART_BROKEN, SAD, CHILLED_OUT, HAPPY, ECSTATIC, ROMANTIC, PARTY;
    }

    MY_PLAYLIST_SECTION my_playlist_section;

    public enum MY_PLAYLIST_SECTION {
        MY_PLAYLIST;
    }

    OFFLINE_SONGS_SELECTION offline_songs_selection;

    public enum OFFLINE_SONGS_SELECTION {
        OFFLNE_SONGS;
    }

    LANGUAGE_SELECTION languageSelection;

    public enum LANGUAGE_SELECTION {
        SAVE;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Control is on OnCreate if Service is not created, Create it");

//        IntentFilter mediaIntentFilter = new IntentFilter();
//        mediaIntentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
//        mediaButtonReceiver = new SyncReceiver();
//        registerReceiver(mediaButtonReceiver, mediaIntentFilter);

        Log.i(TAG, "ProxyService.onCreate()");
        _instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "ProxyService.onStartCommand()");
        Log.i(TAG, "Control is on OnStartCommand");
        startProxyIfNetworkConnected();

//        setCurrentActivity(LockScreenActivity.getInstance());

        return START_STICKY;

    }

    /**
     * Increase the ID
     */

    public int nextCorrID() {
        autoIncCorrId++;
        return autoIncCorrId;
    }

    int corrRelIdAlbum, corrRelIdSong, corrRelIdPlayerOption, corrRelIdQueue, corrRelIdPlayList,
            corrRelIdFavAlbum, corrRelIdFavSongs, corrRelIdFavPlaylist, corrRelIdFavArtistRadio,
            corrRelIdMoodsLIst, corrRelIdOfflineSongs, corrRelIdLanguages, corrRelIdRepeatMode;

    public int getlastCorrId() {
        return autoIncCorrId;
    }

    protected int nextCMDCorrID() {
        autoIncCNDCorrId++;
        return autoIncCNDCorrId;
    }

    protected int nextChoiceCorrID() {
        choiceId++;
        return choiceId;
    }

    protected int nextChoiceSetCorrID() {
        choiceSetId++;
        return choiceSetId;
    }

    protected int nextInteractionChoiceCorrID() {
        interactionChoiceSetID++;
        return interactionChoiceSetID;
    }

    /**
     * Check network and connection available to start proxy service
     * Save connection type to shared preference(Bluetooth/WIFI Network connection)
     */
    private void startProxyIfNetworkConnected() {
        Log.i(TAG, "startProxyIfNetworkConnected()");
        final SharedPreferences prefs = getSharedPreferences(Const.PREFS_NAME,
                MODE_PRIVATE);
        final int transportType = prefs.getInt(
                Const.Transport.PREFS_KEY_TRANSPORT_TYPE,
                Const.Transport.PREFS_DEFAULT_TRANSPORT_TYPE);

        if (transportType == Const.Transport.KEY_BLUETOOTH) {
            Log.d(TAG, "ProxyService. onStartCommand(). Transport = Bluetooth.");
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            Log.d(TAG, "mBtAdapter" + mBtAdapter);
            if (mBtAdapter != null) {
                Log.d(TAG, "mBtAdapter.isEnabled()" + mBtAdapter.isEnabled());
                if (mBtAdapter.isEnabled()) {
                    startProxy();
                }
            }
        } else {
            startProxy();
        }
    }

    /**
     * Save preferences and start proxy
     * Initialize proxy
     * Application id : 8675309
     */
    public void startProxy() {
        Log.i(TAG, "ProxyService.startProxy()" + _syncProxy);

        if (_syncProxy == null) {
            try {
                SharedPreferences settings = getSharedPreferences(
                        Const.PREFS_NAME, 0);
                boolean isMediaApp = settings.getBoolean(
                        Const.PREFS_KEY_ISMEDIAAPP,
                        Const.PREFS_DEFAULT_ISMEDIAAPP);
                String appName = settings.getString(Const.PREFS_KEY_APPNAME,
                        Const.PREFS_DEFAULT_APPNAME);
                int transportType = settings.getInt(
                        Const.Transport.PREFS_KEY_TRANSPORT_TYPE,
                        Const.Transport.PREFS_DEFAULT_TRANSPORT_TYPE);
                String ipAddress = settings.getString(
                        Const.Transport.PREFS_KEY_TRANSPORT_IP,
                        Const.Transport.PREFS_DEFAULT_TRANSPORT_IP);
                int tcpPort = settings.getInt(
                        Const.Transport.PREFS_KEY_TRANSPORT_PORT,
                        Const.Transport.PREFS_DEFAULT_TRANSPORT_PORT);
                boolean autoReconnect = settings
                        .getBoolean(
                                Const.Transport.PREFS_KEY_TRANSPORT_RECONNECT,
                                Const.Transport.PREFS_DEFAULT_TRANSPORT_RECONNECT_DEFAULT);

                if (transportType == Const.Transport.KEY_BLUETOOTH) {
                    //_syncProxy = new SyncProxyALM(this, appName, isMediaApp);
                    _syncProxy = new SdlProxyALM(this, appName, isMediaApp, Language.EN_US, Language.EN_US, "8675309");//584421907" //"584421907"
                } else {
                    //_syncProxy = new SyncProxyALM(this, appName, isMediaApp, new TCPTransportConfig(tcpPort, ipAddress, autoReconnect));
                    _syncProxy = new SdlProxyALM(this, appName, isMediaApp, Language.EN_US, Language.EN_US, "8675309", new TCPTransportConfig(tcpPort, ipAddress, autoReconnect));
                }
            } catch (SdlException e) {
                e.printStackTrace();
                //error creating proxy, returned proxy = null
                if (_syncProxy == null) {
                    stopSelf();
                }
            }
        }
        Log.i(TAG, "ProxyService.startProxy() returning");
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

        try {
            Log.i(TAG, "ProxyService.onDestroy()");

            disposeSyncProxy();

            clearlockscreen();
            _instance = null;
            /**
             * Unregister Sync broadcast listner
             * */
            unregisterReceiver(mediaButtonReceiver);
        } catch (Exception e) {

        }

        super.onDestroy();

    }

    /**
     * Reset sync service
     */
    public void disposeSyncProxy() {
        Log.i(TAG, "ProxyService.disposeSyncProxy()");

        if (_syncProxy != null) {
            try {
                _syncProxy.dispose();
            } catch (SdlException e) {
                e.printStackTrace();
            }
            _syncProxy = null;
            clearlockscreen();
        }
    }

    /**
     * Instance of Proxy service
     */
    public static ProxyService getInstance() {
        return _instance;
    }

    /**
     * Instance of Lock screen Activity to perform foreground i.e. UI thread operation
     */
    public void setCurrentActivity(LockScreenActivity currentActivity) {
        this._mainInstance = currentActivity;
    }

    /**
     * Instance of Proxy sync
     */
    public static SdlProxyALM getProxyInstance() {
        return _syncProxy;
    }

    public static void waiting(boolean waiting) {
        waitingForResponse = waiting;
    }


    /**
     * Wel come message appears on start of application
     */
    private void showWelcomeMessage() {

        try {
//            _syncProxy.show("Welcome to Hungama Music...", "", TextAlignment.LEFT_ALIGNED, nextCorrID());
            _syncProxy.show("Welcome to", "Hungama music", TextAlignment.LEFT_ALIGNED, ProxyService.getInstance().nextCorrID());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Check Internet connection of device and display message on screen
     */
    public void showNoConnectionMessage() {

        try {
            _syncProxy.show("No internet connection", "", TextAlignment.CENTERED, nextCorrID());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Invoke lock screen activity as soon as sync connected to device
     */
    public void lockAppsScreen() {
        Intent lockScreen;
        Utils.setFordCarMode(true);
        if (PlayerService.service != null)
            PlayerService.service.updatewidget();
        lockScreen = new Intent(this, LockScreenActivity.class);
        lockScreen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(lockScreen);
    }

    /**
     * Initialize application buttons
     */
    private void initializeTheApp() {
        /**
         *display welcome message on screen
         */
        showWelcomeMessage();
        /**
         *Intent for lock screen
         */
        lockAppsScreen();

        /**
         *needs handler for applying delay in performing operation
         * */
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                // check proxy connection instance
                if (_syncProxy == null)
                    return;
                // initialize buttons
                initializeButtonsToBeSubscribed();
                /**
                 * Check play mode
                 *  @Music
                 *  @Radio
                 *  for soft button display on screen
                 * */
                if (PlayerService.service.getPlayMode() == PlayMode.MUSIC || PlayerService.service.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    showSoftButtonsOnScreen();
                }
                if (PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                    showSoftButtonsOnDemandRadio();
                } else if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                    showSoftButtonsLiveOnRadio();
                }

                /**
                 * Menu initialization and VR-command related to it
                 * */
                //Menu Command
                initializeSubMenuCommandForSyncPlayer();

                updateHMIStatus();
                isInitialize = true;
                Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (_syncProxy == null)
                            return;

                        _mainInstance.setTitleToFord();

                        if (PlayerService.service == null || PlayerService.service.getPlayingQueue() == null || PlayerService.service.getPlayingQueue().size() == 0) {
                            _mainInstance.noSongsInPlayerQueue();
                        }
                    }
                }, 1000);


            }
        }, 3000);


    }


    private void initializeVoiceCommand() {
        try {
            _syncProxy.addCommand(nextCMDCorrID(), "Play", new Vector<String>(Arrays.asList(new String[]{"Play", "Play Song"})), nextCorrID());
            _syncProxy.addCommand(nextCMDCorrID(), "Pause", new Vector<String>(Arrays.asList(new String[]{"Pause", "Pause Song"})), nextCorrID());
            _syncProxy.addCommand(nextCMDCorrID(), "Next", new Vector<String>(Arrays.asList(new String[]{"Next", "Next Song"})), nextCorrID());
            _syncProxy.addCommand(nextCMDCorrID(), "Previous", new Vector<String>(Arrays.asList(new String[]{"Previous", "Previous Song"})), nextCorrID());
         /*   _syncProxy.addCommand(nextCMDCorrID(), "Backward", new Vector<String>(Arrays.asList(new String[]{"Backward", "Seek Backward"})), nextCorrID());
            _syncProxy.addCommand(nextCMDCorrID(), "Forward", new Vector<String>(Arrays.asList(new String[]{"Forward", "Seek Forward"})), nextCorrID());
            _syncProxy.addCommand(nextCMDCorrID(), "Select Song", new Vector<String>(Arrays.asList(new String[]{"Select Song"})), nextCorrID());
            _syncProxy.addCommand(nextCMDCorrID(), "info", new Vector<String>(Arrays.asList(new String[]{"info"})), nextCorrID());*/
        } catch (SdlException e) {
            Log.e(TAG, "Error adding AddCommands", e);
        }
    }

    /**
     * Hardware button subscription
     */
    private void initializeButtonsToBeSubscribed() {
        try {
            _syncProxy.subscribeButton(ButtonName.OK, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.TUNEUP, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.TUNEDOWN, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.SEEKLEFT, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.SEEKRIGHT, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_0, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_1, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_2, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_3, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_4, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_5, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_6, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_7, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_8, nextCorrID());
            _syncProxy.subscribeButton(ButtonName.PRESET_9, nextCorrID());
        } catch (SdlException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    OnHMIStatus notification;


    @Override
    public void onOnHMIStatus(OnHMIStatus notification) {
        // TODO Auto-generated method stub
        this.notification = notification;
        Log.i(TAG, "" + notification);
        switch (notification.getSystemContext()) {
            case SYSCTXT_MAIN:
                break;
            case SYSCTXT_VRSESSION:
                break;
            case SYSCTXT_MENU:
                break;
            default:
                return;
        }


        switch (notification.getHmiLevel()) {
            case HMI_FULL:
                if (_syncProxy.getAppInterfaceRegistered()) {
                    if (notification.getFirstRun()) {
                        initializeTheApp();

                    }
                }
//			if (_syncProxy.getAppInterfaceRegistered()) {
//				// if (hmiFull) {
//				if (notification.getFirstRun()) {
//
//					try {
//						_syncProxy.show("Sync Music Player", "", TextAlignment.CENTERED, nextCorrID());
//						// MainActivity.getInstance().playPauseCurrentPlayingSong();
//						Log.d(TAG, "First run");
//						initializeTheApp();
//					} catch (SdlException e) {
//					}
//
//				} else {
//					try {
//						if (!mbWaitingForResponse) {
//							_syncProxy.show("Sync Music Player", "", TextAlignment.CENTERED, nextCorrID());
//						}
//					} catch (SdlException e) {
//
//					}
//				}
//				// }
//
//			}

                break;
            case HMI_LIMITED:
                break;
            case HMI_BACKGROUND:
                break;
            case HMI_NONE:


                break;
            default:
                return;
        }


        if (isInitialize)
            updateHMIStatus();


    }

    private boolean isInitialize = false;

    private void updateHMIStatus() {

        switch (notification.getAudioStreamingState()) {
            case AUDIBLE:
                Log.e("AUDIBLE", "AUDIBLE" + playingAudio + " PlayerService.service" + PlayerService.service + " isPauseFromVoiceCommand" + isPauseFromVoiceCommand);
                if (!playingAudio && _mainInstance != null && !isPauseFromVoiceCommand) {
                    _mainInstance.playCurrentSongWithoutText();
                    playingAudio = true;
                }
                if (isPauseFromVoiceCommand) {
                    playingAudio = true;
                    isPauseFromVoiceCommand = false;
                }
                break;
            case NOT_AUDIBLE:
                Log.e("NOT_AUDIBLE", "NOT_AUDIBLE" + playingAudio + " PlayerService.service" + PlayerService.service + " isPauseFromVoiceCommand" + isPauseFromVoiceCommand);
                if (playingAudio) {
                    playingAudio = false;
                    _mainInstance.pauseCurrentSong();
                } else if (!isInitialize) {
                    _mainInstance.pauseCurrentSong();
                }
                break;
            default:
                return;
        }
    }

    @Override
    public void onAddCommandResponse(AddCommandResponse addCmdResponse) {
        // TODO Auto-generated method stub
        Log.i(TAG, addCmdResponse.toString());

    }

    @Override
    public void onProxyClosed(String arg0, Exception e, SdlDisconnectedReason reason) {
        // TODO Auto-generated method stub
        Log.e(TAG, "onProxyClosed: " + arg0, e);

        boolean wasConnected = !firstHMIStatusChange;
        firstHMIStatusChange = true;
        if (wasConnected) {
            final LockScreenActivity mainActivity = LockScreenActivity.getInstance();
            if (mainActivity != null) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainActivity.onProxyClosed();
                    }
                });
            } else {
                Log.w(TAG, "mainActivity not found");
            }
        }

        if (((SdlException) e).getSdlExceptionCause() != SdlExceptionCause.SDL_PROXY_CYCLED
                && ((SdlException) e).getSdlExceptionCause() != SdlExceptionCause.BLUETOOTH_DISABLED) {
            reset();
        }


        //PlayerService.service.updatewidgetforFord(false);
        if (LockScreenActivity.getInstance() != null)
            LockScreenActivity.getInstance().finish();

    }

    @Override
    public void onServiceEnded(OnServiceEnded onServiceEnded) {

    }

    @Override
    public void onServiceNACKed(OnServiceNACKed onServiceNACKed) {

    }

    @Override
    public void onOnStreamRPC(OnStreamRPC onStreamRPC) {

    }

    @Override
    public void onStreamRPCResponse(StreamRPCResponse streamRPCResponse) {

    }

    /**
     * Resetting sync connections
     */
    public void reset() {
        try {
//            stopSelf();
//
            if (_syncProxy != null)
                _syncProxy.resetProxy();
            else
                startProxyIfNetworkConnected();
        } catch (SdlException e1) {
            e1.printStackTrace();
            //something goes wrong, & the proxy returns as null, stop the service.
            //do not want a running service with a null proxy
            if (_syncProxy == null) {
                stopSelf();
            }
        }
    }

    /**
     * Restarting SyncProxyALM. For example after changing transport type
     */
    public void restart() {
        Log.i(TAG, "ProxyService.Restart SyncProxyALM.");
        disposeSyncProxy();
        startProxyIfNetworkConnected();
    }

    @Override
    public void onError(String info, Exception e) {
        // TODO Auto-generated method stub
        Log.e(TAG, "******onProxyError******");
        Log.e(TAG, "ERROR: " + info, e);
    }

    @Override
    public void onAddSubMenuResponse(AddSubMenuResponse subMenuResponse) {
        // TODO Auto-generated method stub
        Log.i(TAG, subMenuResponse.toString());

    }

    @Override
    public void onAlertResponse(AlertResponse arg0) {
        // TODO Auto-generated method stubdfs
        Log.e(TAG, arg0.toString());
        Log.e(TAG, arg0.getFunctionName());

//            _mainInstance.playCurrentSong(true);

    }

    @Override
    public void onCreateInteractionChoiceSetResponse(
            CreateInteractionChoiceSetResponse interactionChoiceSetResponse) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteCommandResponse(DeleteCommandResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteInteractionChoiceSetResponse(
            DeleteInteractionChoiceSetResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteSubMenuResponse(DeleteSubMenuResponse arg0) {
        // TODO Auto-generated method stub

    }

	/*@Override
    public void onEncodedSyncPDataResponse(EncodedSyncPDataResponse arg0) {
		// TODO Auto-generated method stub
		
	}*/


    @Override
    public void onGenericResponse(GenericResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOnButtonEvent(OnButtonEvent notification) {
        // TODO Auto-generated method stub
        Log.i(TAG, "" + notification);
    }

    /**
     * Hardware & Soft button click events
     */

    @Override
    public void onOnButtonPress(OnButtonPress notification) {
        // TODO Auto-generated method stub
        Log.i(TAG, "" + notification);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        switch (notification.getButtonName()) {
            case OK:
                _mainInstance.playPauseCurrentPlayingSong();
                break;
            case SEEKLEFT:
                _mainInstance.seekBackwardCurrentPlayingSong();
                break;
            case SEEKRIGHT:
                _mainInstance.seekForwardCurrentPlayingSong();
                break;
            case TUNEUP:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                break;
            case TUNEDOWN:
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                break;
            case PRESET_0:
//				SubscribeVehicleDataClass.getInstance(ProxyService.this, 0).getVehicleData();
                 _mainInstance.playTrackNumber(0);

                break;
            case PRESET_1:
//				SubscribeVehicleDataClass.getInstance(ProxyService.this, 1).getVehicleData();
                _mainInstance.playTrackNumber(1);
                break;
            case PRESET_2:
//				SubscribeVehicleDataClass.getInstance(ProxyService.this, 2).getVehicleData();
                _mainInstance.playTrackNumber(2);
                break;
            case PRESET_3:
                _mainInstance.playTrackNumber(3);
                break;
            case PRESET_4:
                _mainInstance.playTrackNumber(4);
                break;
            case PRESET_5:
                _mainInstance.playTrackNumber(5);
                break;
            case PRESET_6:
                _mainInstance.playTrackNumber(6);
                break;
            case PRESET_7:
                _mainInstance.playTrackNumber(7);
                break;
            case PRESET_8:
                _mainInstance.playTrackNumber(8);
                break;
            case PRESET_9:
                _mainInstance.playTrackNumber(9);
                break;

            default:
                break;
        }

        // Handling softButtons notifications-- 6 softbuttons cmd are albumList,
        // SongList, Song info, app info, applink info, command info

        if (notification.getCustomButtonName() != null && notification.getCustomButtonName().equals(100)) {
            if (PlayerService.service.getPlayMode() == PlayMode.MUSIC) {
                LockScreenActivity.getInstance().jumpToNextSong();
            }
            if (PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                LockScreenActivity.getInstance().jumpToNextSong();
            }

            Alert next = new Alert();
            next.setAlertText1("Next");
            next.setDuration(1000);
            next.setCorrelationID(nextCorrID());
            Vector<TTSChunk> ttsChunks = new Vector<TTSChunk>();
            ttsChunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT,
                    "Next"));
            next.setTtsChunks(ttsChunks);
            try {
                _syncProxy.sendRPCRequest(next);
            } catch (SdlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (notification.getCustomButtonName() != null && notification.getCustomButtonName().equals(101)) {
            LockScreenActivity.getInstance().jumpToPreviousSong();
            Alert previous = new Alert();
            previous.setAlertText1("Previous");
            previous.setDuration(1000);
            previous.setCorrelationID(nextCorrID());
            Vector<TTSChunk> ttsChunks = new Vector<TTSChunk>();
            ttsChunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT,
                    "Previous"));
            previous.setTtsChunks(ttsChunks);
            try {
                _syncProxy.sendRPCRequest(previous);
            } catch (SdlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else if (notification.getCustomButtonName() != null && notification.getCustomButtonName().equals(102)) {

            _mainInstance.setShuffle();
        } /*else if (notification.getCustomButtonName().equals(102)) {
                    MainActivity.getInstance().seekForwardCurrentPlayingSong();
					Alert forward = new Alert();
					forward.setAlertText1("Forward");
					forward.setDuration(1000);
					forward.setCorrelationID(nextCorrID());
					Vector<TTSChunk> ttsChunks = new Vector<TTSChunk>();
					ttsChunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT,
							"Seek Forward"));
					forward.setTtsChunks(ttsChunks);
					try {
						_syncProxy.sendRPCRequest(forward);
					} catch (SdlException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (notification.getCustomButtonName().equals(103)) {
					MainActivity.getInstance().seekBackwardCurrentPlayingSong();
					Alert backward = new Alert();
					backward.setAlertText1("Backward");
					backward.setDuration(1000);
					backward.setCorrelationID(nextCorrID());
					Vector<TTSChunk> ttsChunks = new Vector<TTSChunk>();
					ttsChunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT,
							"Seek Backward"));
					backward.setTtsChunks(ttsChunks);
					try {
						_syncProxy.sendRPCRequest(backward);
					} catch (SdlException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}*/ else if (notification.getCustomButtonName() != null && notification.getCustomButtonName().equals(103)) {

            Alert applinkinfo = new Alert();
            applinkinfo.setAlertText1("App Info");
            applinkinfo.setDuration(3000);
            applinkinfo.setParameters("alert", "yes");
            applinkinfo.setCorrelationID(nextCorrID());
            Vector<TTSChunk> ttsChunks = new Vector<TTSChunk>();
            ttsChunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT,
                    "This is Hungama Music App specially designed for Ford."));
            applinkinfo.setTtsChunks(ttsChunks);
            try {
                _syncProxy.sendRPCRequest(applinkinfo);
            } catch (SdlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        } else if (notification.getCustomButtonName() != null && notification.getCustomButtonName().equals(104)) {
            // live radio soft button click
            /*RPCMessage req;
            req = RPCRequestFactory.buildPerformInteraction("Live Radio", "Available live radio", 1033, nextCorrID());
            corrRelIdLiveRadio = getlastCorrId();

            try {
                _syncProxy.sendRPCRequest((RPCRequest) req);
            } catch (SdlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/
            addRepeatOptions();

            //addRadioToChoiceSet();

        } else if (notification.getCustomButtonName() != null && notification.getCustomButtonName().equals(102)) {


           /* RPCMessage req;
            req = RPCRequestFactory.buildPerformInteraction("songs","Available Tracks", 1031, nextCorrID());
            try {
                _syncProxy.sendRPCRequest((RPCRequest) req);
            } catch (SdlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }*/

//            PerformInteraction();
            RPCMessage req;
            req = RPCRequestFactory.buildPerformInteraction("New Music", "Available Tracks", 1031, nextCorrID());
            corrRelIdAlbum = getlastCorrId();

            try {
                _syncProxy.sendRPCRequest((RPCRequest) req);
            } catch (SdlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


        } else if (notification.getCustomButtonName() != null && notification.getCustomButtonName().equals(105)) {

            currentSelection = SELECTION.QUEUE;
            favoriteSelection = null;
            my_playlist_section = null;
            offline_songs_selection = null;
            if (PlayerService.service != null && PlayerService.service.getPlayMode() == PlayMode.MUSIC) {
                addPlayerQueueToMenu();
            }
//            addPlayerQueueToMenu();

        } else if (notification.getCustomButtonName() != null && notification.getCustomButtonName().equals(108)) {
            // to do
//					PerformAudioPassThruClass.getInstance(ProxyService.this).show();
        } else if (notification.getCustomButtonName() != null && notification.getCustomButtonName().equals(109)) {
            try {
                _syncProxy.show("Vehicle Data", "Coming Soon", TextAlignment.CENTERED, nextCorrID());
            } catch (SdlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    /**
     * VR Command
     * Handling menu event and VR Command for the same
     */
    @Override
    public void onOnCommand(OnCommand notification) {
        // TODO Auto-generated method stub
        //Handling Menu Events
        handlingMenuCommandEvents(notification);

        //Handling voice command
        handlingVoiceCommandForSync(notification);

    }

    @Override
    public void onOnPermissionsChange(OnPermissionsChange arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPerformInteractionResponse(PerformInteractionResponse interactionResponse) {
        // TODO Auto-generated method stub
        perfomChoiceSetSelection(interactionResponse);
    }


    @Override
    public void onResetGlobalPropertiesResponse(
            ResetGlobalPropertiesResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse arg0) {
        // TODO Auto-generated method stub
        SetGlobalPropertiesResponse sgpd = new SetGlobalPropertiesResponse();

        //_syncProxy.resetGlobalProperties(properties, nextCorrID());

    }

    @Override
    public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onShowResponse(ShowResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSpeakResponse(SpeakResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSubscribeButtonResponse(SubscribeButtonResponse response) {
        // TODO Auto-generated method stub
        Log.i(TAG, "" + response);
    }

    @Override
    public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {
        // TODO Auto-generated method stub
        Log.i(TAG, "" + response);
    }

    @Override
    public void onOnDriverDistraction(OnDriverDistraction arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOnTBTClientState(OnTBTClientState onTBTClientState) {

    }

    @Override
    public void onOnSystemRequest(OnSystemRequest onSystemRequest) {

    }

    @Override
    public void onSystemRequestResponse(SystemRequestResponse systemRequestResponse) {

    }

    @Override
    public void onOnKeyboardInput(OnKeyboardInput onKeyboardInput) {

    }

    @Override
    public void onOnTouchEvent(OnTouchEvent onTouchEvent) {

    }

    @Override
    public void onDiagnosticMessageResponse(DiagnosticMessageResponse diagnosticMessageResponse) {

    }

	/*@Override
    public void onOnEncodedSyncPData(OnEncodedSyncPData arg0) {
		// TODO Auto-generated method stub
		
	}*/

	/*@Override
    public void onOnTBTClientState(OnTBTClientState arg0) {
		// TODO Auto-generated method stub
		
	}*/

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return new Binder();
    }

    public void deleteAndUpdatePlayerQueueToMenu() {
        try {
            if (PlayerService.service != null) {
                for (int i = playerQueueIdLast; i >= 0; i--) {
                    Log.e(TAG + "i", "" + i);
                    _syncProxy.deleteCommand(i, nextCorrID());
                }
                _syncProxy.deleteCommand(100, nextCorrID());
                _syncProxy.deleteCommand(101, nextCorrID());
                _syncProxy.deleteCommand(102, nextCorrID());
                _syncProxy.deleteCommand(103, nextCorrID());

            }
        } catch (SdlException e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }


    public void initializeSubMenuCommandForSyncPlayer() {
        try {

//            String mnPlayCmd = new String("Play Song");
//            _syncProxy.addCommand(100, mnPlayCmd, new Vector<String>(Arrays.asList(new String[]{"Play", "Play Song"})), nextCorrID());
//
//            String mnPauseCmd = new String("Pause Song");
//            _syncProxy.addCommand(101, mnPauseCmd, new Vector<String>(Arrays.asList(new String[]{"Pause", "Pause Song"})), nextCorrID());
//
//            String mnNextCmd = new String("Next Song");
//            _syncProxy.addCommand(102, mnNextCmd, new Vector<String>(Arrays.asList(new String[]{"Next", "Next Song"})), nextCorrID());
//
//            String mnPreviousCmd = new String("Previous Song");
//            _syncProxy.addCommand(103, mnPreviousCmd, new Vector<String>(Arrays.asList(new String[]{"Previous", "Previous Song"})), nextCorrID());

            String mnNewMusic = new String("Music");
            _syncProxy.addSubMenu(MUSIC_CHOICESET_ID, mnNewMusic, nextCorrID());
            _syncProxy.addCommand(MUSIC_NEWMUSIC_CHOICESET_ID, "New Music", MUSIC_CHOICESET_ID, 0, new Vector<String>(Arrays.asList(new String[]{"New Music"})), nextCorrID());
            _syncProxy.addCommand(MUSIC_POPULAR_CHOICESET_ID, "Popular Music", MUSIC_CHOICESET_ID, 1, new Vector<String>(Arrays.asList(new String[]{"Popular Music"})), nextCorrID());
            _syncProxy.addCommand(MUSIC_DISCOVERY_CHOICESET_ID, "Discover", MUSIC_CHOICESET_ID, 2, new Vector<String>(Arrays.asList(new String[]{"Discover"})), nextCorrID());

            String mnLiveMusic = new String("Radio");
            _syncProxy.addSubMenu(RADIO_CHOICESET_ID, mnLiveMusic, nextCorrID());
            _syncProxy.addCommand(RADIO_LIVE_CHOICESET_ID, "Live Radio", RADIO_CHOICESET_ID, 0, new Vector<String>(Arrays.asList(new String[]{"Live radio"})), nextCorrID());
            _syncProxy.addCommand(RADIO_ON_DEMAND_CHOICESET_ID, "On Demand", RADIO_CHOICESET_ID, 1, new Vector<String>(Arrays.asList(new String[]{"On demand radio"})), nextCorrID());

            String mnOnDemandRadio = new String("My Playlist");
            _syncProxy.addCommand(PLAYLIST_CHOICESET_ID, mnOnDemandRadio, new Vector<String>(Arrays.asList(new String[]{"My PlayList"})), nextCorrID());

//            String mnAppInfo = new String("App Info");
//            _syncProxy.addCommand(107, mnAppInfo, new Vector<String>(Arrays.asList(new String[]{"App Info", "App Info"})), nextCorrID());

            String mnFavorite = new String("Favorite");
            _syncProxy.addSubMenu(FAVORITE_CHOICESET_ID, mnFavorite, nextCorrID());
            _syncProxy.addCommand(FAVORITE_ALBUM_CHOICESET_ID, "Album", FAVORITE_CHOICESET_ID, 0, new Vector<String>(Arrays.asList(new String[]{"Album"})), nextCorrID());
            _syncProxy.addCommand(FAVORITE_SONGS_CHOICESET_ID, "Songs", FAVORITE_CHOICESET_ID, 1, new Vector<String>(Arrays.asList(new String[]{"Songs"})), nextCorrID());
            _syncProxy.addCommand(FAVORITE_PLAYLIST_CHOICESET_ID, "Playlist", FAVORITE_CHOICESET_ID, 2, new Vector<String>(Arrays.asList(new String[]{"Playlist"})), nextCorrID());
            _syncProxy.addCommand(FAVORITE_ARTIST_RADIO_CHOICESET_ID, "Artist radio", FAVORITE_CHOICESET_ID, 3, new Vector<String>(Arrays.asList(new String[]{"Artist radio"})), nextCorrID());

            String mnOffline = new String("Offline");
            _syncProxy.addCommand(OFFLINE_CHOICESET_ID, mnOffline, new Vector<String>(Arrays.asList(new String[]{"Offline", "Offline"})), nextCorrID());

            String mnChangeLanguage = new String("Change Language");
            _syncProxy.addCommand(CHANGE_LANGUAGE_CHOICESET_ID, mnChangeLanguage, new Vector<String>(Arrays.asList(new String[]{"Change Language", "Change Language"})), nextCorrID());

            // Trial for play/pause VR Command
            String mnControls = new String("Control");
            _syncProxy.addSubMenu(CONTROL_CHOICESET_ID, mnControls, nextCorrID());
            _syncProxy.addCommand(CONTROL_PLAY_CHOICESET_ID, "Play", CONTROL_CHOICESET_ID, 0, new Vector<String>(Arrays.asList(new String[]{"Play song", "Play"})), nextCorrID());
            _syncProxy.addCommand(CONTROL_PAUSE_CHOICESET_ID, "Pause", CONTROL_CHOICESET_ID, 1, new Vector<String>(Arrays.asList(new String[]{"Pause song", "Pause"})), nextCorrID());
            _syncProxy.addCommand(CONTROL_NEXT_CHOICESET_ID, "Next", CONTROL_CHOICESET_ID, 2, new Vector<String>(Arrays.asList(new String[]{"Next song", "Next"})), nextCorrID());
            _syncProxy.addCommand(CONTROL_PREVIOUS_CHOICESET_ID, "Previous", CONTROL_CHOICESET_ID, 3, new Vector<String>(Arrays.asList(new String[]{"Previous", "Previous song"})), nextCorrID());

        /*    String mnSeekBackwardCmd = new String("Seek Backward");
            _syncProxy.addCommand(104, mnSeekBackwardCmd, nextCorrID());
            String mnSeekForwardCmd = new String("Seek Forward");
            _syncProxy.addCommand(105, mnSeekForwardCmd, nextCorrID());*/

        } catch (SdlException e) {
            Log.e(TAG, e.toString());
        }

    }

    /**
     * Handling Menu Events
     */
    private void handlingMenuCommandEvents(OnCommand notification) {

        switch (notification.getCmdID()) {
            case 100:
                Log.e(TAG, "handlingMenuCommandEvents play");
                _mainInstance.playCurrentSong(true);
                break;
            case 101:
                Log.e(TAG, "handlingMenuCommandEvents pause");
                isPauseFromVoiceCommand = true;
                _mainInstance.pauseCurrentSong();
                break;
            case 102:
                _mainInstance.jumpToNextSong();
                break;
            case 103:
                _mainInstance.jumpToPreviousSong();
                break;
            case 1041:
                currentSelection = SELECTION.NEW_MUSIC;
                favoriteSelection = null;
                my_playlist_section = null;
                offline_songs_selection = null;

                addSongsToChoiceSet();


                break;

            case 1042:
                currentSelection = SELECTION.POPULAR_MUSIC;
                favoriteSelection = null;
                my_playlist_section = null;
                offline_songs_selection = null;
                addPopularSongsToChoiceSet();

                break;
            case 1043:
                currentSelection = SELECTION.DISCOVER;
                favoriteSelection = null;
                my_playlist_section = null;
                offline_songs_selection = null;
                addMoodsListToChoiceSet();

                break;

            case 1051:
                //_mainInstance.seekBackwardCurrentPlayingSong();
                addRadioToChoiceSet();

                break;
            case 1052:
                // _mainInstance.seekForwardCurrentPlayingSong();
                addOnDemandRadioToChoiceSet();

                break;

            case 106:
                my_playlist_section = MY_PLAYLIST_SECTION.MY_PLAYLIST;
                favoriteSelection = null;
                currentSelection = null;
                offline_songs_selection = null;
                addPlaylistToChoiceSet();

                break;

            case 1081:
                favoriteSelection = FAVORITE_SELECTION.ALBUM;
                currentSelection = null;
                my_playlist_section = null;
                offline_songs_selection = null;
                _mainInstance.getFavoriteList(MediaType.getMediaItemByName(MediaType.ALBUM.toString()));

                break;

            case 1082:
                favoriteSelection = FAVORITE_SELECTION.SONGS;
                currentSelection = null;
                my_playlist_section = null;
                offline_songs_selection = null;
                _mainInstance.getFavoriteList(MediaType.getMediaItemByName(MediaType.TRACK.toString()));


                break;
            case 1083:
                favoriteSelection = FAVORITE_SELECTION.PLAYLIST;
                currentSelection = null;
                my_playlist_section = null;
                offline_songs_selection = null;
                _mainInstance.getFavoriteList(MediaType.getMediaItemByName(MediaType.PLAYLIST.toString()));


                break;
            case 1084:
                favoriteSelection = FAVORITE_SELECTION.ARTIST_RADIO;
                currentSelection = null;
                my_playlist_section = null;
                offline_songs_selection = null;
                _mainInstance.getFavoriteList(MediaType.getMediaItemByName(MediaType.ARTIST.toString()));


                break;

            case 107:

                Alert applinkinfo = new Alert();
                applinkinfo.setAlertText1("App Info");
                applinkinfo.setDuration(3000);
                applinkinfo.setParameters("alert", "yes");
                applinkinfo.setCorrelationID(nextCorrID());
                Vector<TTSChunk> ttsChunks = new Vector<TTSChunk>();
                ttsChunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT,
                        "This is Hungama Music App specially designed for Ford."));
                applinkinfo.setTtsChunks(ttsChunks);
                try {
                    _syncProxy.sendRPCRequest(applinkinfo);
                } catch (SdlException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }


                break;

            case 109:
                offline_songs_selection = OFFLINE_SONGS_SELECTION.OFFLNE_SONGS;
                favoriteSelection = null;
                currentSelection = null;
                my_playlist_section = null;

                addOfflineToChoiceSet();

                break;
            case 111:

                languageSelection = LANGUAGE_SELECTION.SAVE;
                favoriteSelection = null;
                currentSelection = null;
                my_playlist_section = null;
                offline_songs_selection = null;
                addLanguagePreferenceList();

                break;
// Rahul -------------------------
            // play song
            case 00001:

                if (_mainInstance != null)
                    _mainInstance.playCurrentSong(false);
                break;
// pause song
            case 00002:

                if (_mainInstance != null) {
                    isPauseFromVoiceCommand = true;
                    _mainInstance.pauseCurrentSong();
                }

                break;
            // Next song
            case 00003:

                if (_mainInstance != null)
                    _mainInstance.jumpToNextSong();
                break;

            case 00004:

                if (_mainInstance != null)
                    _mainInstance.jumpToPreviousSong();

                break;
            //----------------------------
            default:
                if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO || PlayerService.service.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                    if (mTracks != null)
                        _mainInstance.playSelectedSong(mTracks.get(notification.getCmdID()));
                } else {
                    _mainInstance.playTrackNumber(notification.getCmdID());

                }
                break;
        }
    }

    /**
     * Handling voice command
     */
    private void handlingVoiceCommandForSync(OnCommand notification) {
        switch (notification.getCmdID()) {
            case 1002:
                Log.e(TAG, "handlingVoiceCommandForSync play");
                _mainInstance.playCurrentSong(true);
                break;
            case 1003:
                Log.e(TAG, "handlingVoiceCommandForSync pause");
                isPauseFromVoiceCommand = true;
                _mainInstance.pauseCurrentSong();
                break;
            case 1004:
                _mainInstance.jumpToNextSong();
                break;
            case 1005:
                _mainInstance.jumpToPreviousSong();
                break;
            case 1006:
                _mainInstance.seekBackwardCurrentPlayingSong();
                break;
            case 1007:
                _mainInstance.seekForwardCurrentPlayingSong();
                break;
            case 1008:   // for Choice set
                PerformInteraction();
                break;
            case 1009:   // for text to speech
                PerformTTsInteraction();
                break;
            // Rahul -------------------------
            case 00001:
                if (_mainInstance != null)
                    _mainInstance.playCurrentSong(false);
                break;

            case 00002:
                if (_mainInstance != null)
                    isPauseFromVoiceCommand = true;
                _mainInstance.pauseCurrentSong();

                break;
            // Next song
            case 00003:

                if (_mainInstance != null)
                    _mainInstance.jumpToNextSong();
                break;

            case 00004:

                if (_mainInstance != null)
                    _mainInstance.jumpToPreviousSong();

                break;
            //----------------------------
            default:
                break;
        }
    }

//	private void createInteractionChoiceSet(){
//		int i;
//		Vector<Choice> choiceVector = new Vector<Choice>();
//
//		SongsManager mgr = new SongsManager();
//		songsList = mgr.getPlayList();
//		for(i = 0; i <songsList.size(); i++){
//
//			Choice choice1 = new Choice();
//			choice1.setChoiceID(i);
//			//choice1.setMenuName("Track Number" +i);
//			//Displaying song title as Interaction choice set displayable
//			choice1.setMenuName(songsList.get(i).get("songTitle"));
//			choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{songsList.get(i).get("songTitle"), "Track " + i})));
//			choiceVector.addElement(choice1);
//
//		}
//		lastIndexOfSongChoiceId = i;
//		setLastIndexOfSongChoiceId(lastIndexOfSongChoiceId);
//		RPCMessage trackMsg;
//		trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choiceVector, nextInteractionChoiceCorrID(), nextCorrID());
//
//		try {
//			_syncProxy.sendRPCRequest((RPCRequest) trackMsg);
//
//		} catch (SdlException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		//Choice set for Info to be used TTS and TTs chunk
//		Vector<Choice> ttsVector = new Vector<Choice>();
//			Choice choice1 = new Choice();
//			choice1.setChoiceID(lastIndexOfSongChoiceId+1);
//			choice1.setMenuName("Info1");
//			choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{"Application"})));
//			ttsVector.addElement(choice1);
//
//
//			Choice choice2 = new Choice();
//			choice2.setChoiceID(lastIndexOfSongChoiceId+2);
//			choice2.setMenuName("Info2");
//			choice2.setVrCommands(new Vector<String>(Arrays.asList(new String[]{"Features"})));
//			ttsVector.addElement(choice2);
//
//
//			Choice choice3 = new Choice();
//			choice3.setChoiceID(lastIndexOfSongChoiceId+3);
//			choice3.setMenuName("Info3");
//			choice3.setVrCommands(new Vector<String>(Arrays.asList(new String[]{"Applink"})));
//			ttsVector.addElement(choice3);
//
//			RPCMessage infoMsg;
//			infoMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(ttsVector, nextInteractionChoiceCorrID(), nextCorrID());
//			try {
//				_syncProxy.sendRPCRequest((RPCRequest) infoMsg);
//
//			} catch (SdlException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//	}

    private void PerformInteraction() {
        Vector<TTSChunk> initChunks = TTSChunkFactory.createSimpleTTSChunks("Say track number, or, song title");
        Vector<TTSChunk> helpChunks = TTSChunkFactory
                .createSimpleTTSChunks("Please Select a song");
        Vector<TTSChunk> timeoutChunks = TTSChunkFactory
                .createSimpleTTSChunks("Time's up! Try Again!");
        Vector<Integer> interactionChoiceSetIdList = new Vector<Integer>();
        interactionChoiceSetIdList.addElement(1031);
        RPCMessage req;
        req = RPCRequestFactory.buildPerformInteraction(initChunks, "Available Tracks", interactionChoiceSetIdList, helpChunks, timeoutChunks, InteractionMode.VR_ONLY, 10000, nextCorrID());
        try {
            _syncProxy.sendRPCRequest((RPCRequest) req);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void PerformTTsInteraction() {
        Vector<TTSChunk> initChunks = TTSChunkFactory.createSimpleTTSChunks("Available voice commands under info are, Features, Application, and, Applink");
        Vector<TTSChunk> helpChunks = TTSChunkFactory
                .createSimpleTTSChunks("Please Select your option");
        Vector<TTSChunk> timeoutChunks = TTSChunkFactory
                .createSimpleTTSChunks("Time's up! Try Again!");
        Vector<Integer> interactionChoiceSetIdList = new Vector<Integer>();
        interactionChoiceSetIdList.addElement(1032);
        RPCMessage req;
        req = RPCRequestFactory.buildPerformInteraction(initChunks, "Get Information", interactionChoiceSetIdList, helpChunks, timeoutChunks, InteractionMode.VR_ONLY, 10000, nextCorrID());
        try {
            _syncProxy.sendRPCRequest((RPCRequest) req);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void updateQueuMenu() {
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                ProxyService.getInstance().deleteAndUpdatePlayerQueueToMenu();
                ProxyService.getInstance().addPlayerQueueToMenu();
                ProxyService.getInstance().initializeSubMenuCommandForSyncPlayer();
            }
        }, 4000);
    }

    private int lastChoiceIdForMusic, lastChoiceIdForPopularMusic, lastChoiceIdForQueue, lastChoiceIdForMyPlaylist, lastChoiceIdForFavTrack, lastChoiceIdForFavAlbum,
            lastChoiceIdForFavRadio, lastChoiceIdForFavPlaylist, lastChoiceIdForOfflineSongs, lastChoiceIdForLanguageSelection, lastChoiceIdForRepeat;
    private boolean isPopularMusic;

    /**
     * Interaction response
     */
    private void perfomChoiceSetSelection(PerformInteractionResponse interactionResponse) {
        try {

            if (interactionResponse.getResultCode() == Result.ABORTED) {
                _mainInstance.setTitleToFord();
                return;
            }

            Speak msg = new Speak();
            final int choice = interactionResponse.getChoiceID();
            Vector<TTSChunk> chunks = new Vector<TTSChunk>();

            if (interactionResponse.getCorrelationID() == corrRelIDAlbumPopular) {
                lastChoiceIdForPopularMusic = choice;
                addPlayerOptionToChoiceSet(MUSIC_PLAYER_CHOICE_SET_ID_OTHERS);
                return;
            }
            if (interactionResponse.getCorrelationID() == corrRelIdAlbum) {
                lastChoiceIdForMusic = choice;

                addPlayerOptionToChoiceSet(MUSIC_PLAYER_CHOICE_SET_ID_OTHERS);
                return;
            }
            if (interactionResponse.getCorrelationID() == corrRelIdPlayerOption) {

                if (choice == 0) {
                    if (currentSelection != null && currentSelection == SELECTION.NEW_MUSIC) {
                        if (mediaItems_albums != null && mediaItems_albums.size() > 0) {
                            if (mediaItems_albums.get(lastChoiceIdForMusic).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItems_albums.get(interactionResponse.getChoiceID());
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);

                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItems_albums.get(lastChoiceIdForMusic), false);
                            }
                        }
                    } else if (currentSelection != null && currentSelection == SELECTION.POPULAR_MUSIC) {
                        if (mediaItemsPopular_albums != null && mediaItemsPopular_albums.size() > 0) {
                            if (mediaItemsPopular_albums.get(lastChoiceIdForPopularMusic).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItemsPopular_albums.get(interactionResponse.getChoiceID());
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);

                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemsPopular_albums.get(lastChoiceIdForPopularMusic), false);
                            }
                        }
                    } else if (currentSelection != null && currentSelection == SELECTION.QUEUE) {

                        Thread thread = new Thread() {
                            public void run() {
                                final Handler handler = new Handler(getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (tracks_queue != null && tracks_queue.size() > 0) {
                                            _mainInstance.playSelectedSong(tracks_queue.get(lastChoiceIdForQueue));
                                            _mainInstance.setMusicSoftbutton();
                                        }
                                    }
                                });
                            }
                        };
                        thread.start();

                    } else if (favoriteSelection != null && favoriteSelection == FAVORITE_SELECTION.ALBUM) {
                        if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                            if (mediaItemListFavorite.get(lastChoiceIdForFavAlbum).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItemListFavorite.get(interactionResponse.getChoiceID());
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);
                                _mainInstance.setMusicSoftbutton();

                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemListFavorite.get(lastChoiceIdForFavAlbum), false);
                            }
                        }

                    } else if (favoriteSelection != null && favoriteSelection == FAVORITE_SELECTION.SONGS) {
                        if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                            if (mediaItemListFavorite.get(lastChoiceIdForFavTrack).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItemListFavorite.get(lastChoiceIdForFavTrack);
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);
                                _mainInstance.setMusicSoftbutton();


                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemListFavorite.get(lastChoiceIdForFavTrack), false);
                            }
                        }
                    } else if (favoriteSelection != null && favoriteSelection == FAVORITE_SELECTION.PLAYLIST) {
                        if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                            if (mediaItemListFavorite.get(lastChoiceIdForFavPlaylist).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItemListFavorite.get(interactionResponse.getChoiceID());
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);
                                _mainInstance.setMusicSoftbutton();


                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemListFavorite.get(lastChoiceIdForFavPlaylist), false);
                            }
                        }

                    } else if (my_playlist_section != null && my_playlist_section == MY_PLAYLIST_SECTION.MY_PLAYLIST) {

                        if (playlistList != null && playlistList.size() > 0) {
                            // to do
                            final List<Track> mTracks = _mainInstance.getTracksListByPlaylist(playlistList.get(lastChoiceIdForMyPlaylist));
                            Thread thread = new Thread() {
                                public void run() {


                                    final Handler handler = new Handler(getMainLooper());
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {

                                            HomeActivity.Instance.mPlayerBar.playNow(mTracks, null, null);
                                            _mainInstance.setMusicSoftbutton();


                                        }
                                    });
                                }
                            };
                            thread.start();

                        }
                        return;
                    } else if (offline_songs_selection != null && offline_songs_selection == OFFLINE_SONGS_SELECTION.OFFLNE_SONGS) {

                        if (mediaItemOfflineTracks != null && mediaItemOfflineTracks.size() > 0) {
                            if (mediaItemOfflineTracks.get(lastChoiceIdForOfflineSongs).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItemOfflineTracks.get(lastChoiceIdForOfflineSongs);
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);
                                _mainInstance.setMusicSoftbutton();


                            }
                        }
                    } else if (languageSelection != null && languageSelection == LANGUAGE_SELECTION.SAVE) {
                        if (languagePreferenceList != null && languagePreferenceList.size() > 0) {
//                            _mainInstance.setEditorPics(lastChoiceIdForLanguageSelection);
                            final String selectedLanguageName = languagePreferenceList.get(lastChoiceIdForLanguageSelection);
                            if (!TextUtils.isEmpty(selectedLanguageName)) {
                                Thread thread = new Thread() {
                                    public void run() {


                                        final Handler handler = new Handler(getMainLooper());
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                _mainInstance.saveLanguageName(lastChoiceIdForLanguageSelection, selectedLanguageName);

                                            }
                                        });
                                    }
                                };
                                thread.start();

                            }

                        }
                    }

                } else if (choice == 1) {
                    //add to que
                    if (currentSelection != null && currentSelection == SELECTION.NEW_MUSIC) {
                        if (mediaItems_albums != null && mediaItems_albums.size() > 0) {
                            if (mediaItems_albums.get(lastChoiceIdForMusic).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItems_albums.get(interactionResponse.getChoiceID());
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);
                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItems_albums.get(lastChoiceIdForMusic), true);
                            }
                        }
                    } else if (currentSelection != null && currentSelection == SELECTION.POPULAR_MUSIC) {
                        if (mediaItemsPopular_albums != null && mediaItemsPopular_albums.size() > 0) {
                            if (mediaItemsPopular_albums.get(lastChoiceIdForPopularMusic).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItemsPopular_albums.get(interactionResponse.getChoiceID());
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);
                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemsPopular_albums.get(lastChoiceIdForPopularMusic), true);
                            }
                        }
                    } else if (favoriteSelection != null && favoriteSelection == FAVORITE_SELECTION.ALBUM) {
                        if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                            if (mediaItemListFavorite.get(lastChoiceIdForFavAlbum).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItemListFavorite.get(interactionResponse.getChoiceID());
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);
                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemListFavorite.get(lastChoiceIdForFavAlbum), true);
                            }
                        }
                    } else if (favoriteSelection != null && favoriteSelection == FAVORITE_SELECTION.SONGS) {
                        if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                            _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemListFavorite.get(lastChoiceIdForFavTrack), true);

                           /* if (mediaItemListFavorite.get(lastChoiceIdForFavTrack).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItemListFavorite.get(interactionResponse.getChoiceID());
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);

                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemListFavorite.get(lastChoiceIdForFavTrack), false);
                            }*/
                        }

                    } else if (favoriteSelection != null && favoriteSelection == FAVORITE_SELECTION.PLAYLIST) {
                        if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                            if (mediaItemListFavorite.get(lastChoiceIdForFavPlaylist).getMediaType() == MediaType.TRACK) {
                                MediaItem mediaItem = mediaItemListFavorite.get(interactionResponse.getChoiceID());
                                Track track = new Track(mediaItem.getId(),
                                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                        mediaItem.getAlbumId());
                                _mainInstance.playSelectedSong(track);

                            } else {
                                _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemListFavorite.get(lastChoiceIdForFavPlaylist), true);
                            }
                        }

                    } else if (my_playlist_section != null && my_playlist_section == MY_PLAYLIST_SECTION.MY_PLAYLIST) {

                        if (playlistList != null && playlistList.size() > 0) {
                            // to do
                            final List<Track> mTracks = _mainInstance.getTracksListByPlaylist(playlistList.get(lastChoiceIdForMyPlaylist));
                            PlayerService.service.addToQueue(mTracks);

                        }
                    } else if (currentSelection != null && currentSelection == SELECTION.QUEUE) {
                        if (tracks_queue != null && tracks_queue.size() > 0) {

                            long id = 0;
                            id = tracks_queue.get(lastChoiceIdForQueue).getId();
                            _mainInstance.favoriteItem(id, "song");
                        }
                    } else if (offline_songs_selection != null && offline_songs_selection == OFFLINE_SONGS_SELECTION.OFFLNE_SONGS) {
                        if (mediaItemOfflineTracks != null && mediaItemOfflineTracks.size() > 0) {
                            _mainInstance.onMediaItemOptionPlayNowAllSelected(mediaItemOfflineTracks.get(lastChoiceIdForOfflineSongs), true);
                        }
                    }

                } else if (choice == 2) {
                    // add to favorite
                    if (currentSelection != null && currentSelection == SELECTION.NEW_MUSIC) {
                        if (mediaItems_albums != null && mediaItems_albums.size() > 0) {
                            _mainInstance.favoriteItem(mediaItems_albums.get(lastChoiceIdForMusic));
                        }
                    } else if (currentSelection != null && currentSelection == SELECTION.POPULAR_MUSIC) {
                        if (mediaItemsPopular_albums != null && mediaItemsPopular_albums.size() > 0) {
                            _mainInstance.favoriteItem(mediaItemsPopular_albums.get(lastChoiceIdForPopularMusic));
                        }
                    } else if (currentSelection != null && currentSelection == SELECTION.QUEUE) {
                        if (tracks_queue != null && tracks_queue.size() > 0) {
                            _mainInstance.saveTrackOffline(tracks_queue.get(lastChoiceIdForQueue));
                        }
                    } else if (favoriteSelection != null && favoriteSelection == FAVORITE_SELECTION.ALBUM) {
                        if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                            _mainInstance.offlineMusic(mediaItemListFavorite.get(lastChoiceIdForFavAlbum));
                        }
                    } else if (favoriteSelection != null && favoriteSelection == FAVORITE_SELECTION.SONGS) {
                        if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                            _mainInstance.offlineMusic(mediaItemListFavorite.get(lastChoiceIdForFavTrack));
                        }
                    } else if (favoriteSelection != null && favoriteSelection == FAVORITE_SELECTION.PLAYLIST) {
                        if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                            _mainInstance.offlineMusic(mediaItemListFavorite.get(lastChoiceIdForFavPlaylist));
                        }
                    } else if (my_playlist_section != null && my_playlist_section == MY_PLAYLIST_SECTION.MY_PLAYLIST) {

                        if (playlistList != null && playlistList.size() > 0) {
                            // to do
                            final List<Track> mTracks = _mainInstance.getTracksListByPlaylist(playlistList.get(lastChoiceIdForMyPlaylist));
                            _mainInstance.saveAllTracksOffline(mTracks);
                        }
                    }

                } else if (choice == 3) {
                    //download
                    if (currentSelection != null && currentSelection == SELECTION.NEW_MUSIC) {
                        if (mediaItems_albums != null && mediaItems_albums.size() > 0) {
                            _mainInstance.offlineMusic(mediaItems_albums.get(lastChoiceIdForMusic));
                        }
                    } else if (currentSelection != null && currentSelection == SELECTION.POPULAR_MUSIC) {
                        if (mediaItemsPopular_albums != null && mediaItemsPopular_albums.size() > 0) {
                            _mainInstance.offlineMusic(mediaItemsPopular_albums.get(lastChoiceIdForPopularMusic));
                        }
                    }
                }
                return;
            } else if (interactionResponse.getCorrelationID() == corrRelIdSong) {
//                _mainInstance.playTrackNumber(choice);
                // Open Option menu
                addPlayerOptionToChoiceSet(MUSIC_PLAYER_CHOICE_SET_ID_OTHERS);
                if (tracks_latest != null && tracks_latest.size() > 0) {
                    _mainInstance.playSelectedSong(tracks_latest.get(choice));
                    updateQueuMenu();
                }
                return;
            } else if (interactionResponse.getCorrelationID() == corrRelIdQueue) {
                lastChoiceIdForQueue = choice;
                // Open Option menu
//                currentSelection = SELECTION.QUEUE;
                addPlayerOptionToChoiceSet(MUSIC_PLAYER_CHOICE_SET_ID_QUEUE);
               /* if (tracks_queue != null && tracks_queue.size() > 0) {
                    _mainInstance.playSelectedSong(tracks_queue.get(choice));
//                    updateQueuMenu();
                }*/
                return;

            } else if (interactionResponse.getCorrelationID() == corrRelIdFavAlbum || interactionResponse.getCorrelationID() == corrRelIdFavSongs
                    || interactionResponse.getCorrelationID() == corrRelIdFavArtistRadio || interactionResponse.getCorrelationID() == corrRelIdFavPlaylist) {

                if (interactionResponse.getCorrelationID() == corrRelIdFavPlaylist) {
                    lastChoiceIdForFavPlaylist = choice;
                    // Open Option menu
//                    favoriteSelection = FAVORITE_SELECTION.PLAYLIST;
                    addPlayerOptionToChoiceSet(MUSIC_PLAYER_CHOICE_SET_ID_FAV);

                } else if (interactionResponse.getCorrelationID() == corrRelIdFavArtistRadio) {
                    lastChoiceIdForFavRadio = choice;
                    if (mediaItemListFavorite != null && mediaItemListFavorite.size() > 0) {
                        new Handler(getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                GetAubumDetails getAlbum = new GetAubumDetails();
                                getAlbum.setDetailCallBack(ProxyService.this);
                                if (Utils.isConnected()) {
                                    _mainInstance.pleaseWaitMessage();
                                    MediaItem artistItem = mediaItemListFavorite.get(lastChoiceIdForFavRadio);
                                    getAlbum.getArtistSong(getApplicationContext(), artistItem);
                                }
                            }
                        });
                    }
                    return;
                } else if (interactionResponse.getCorrelationID() == corrRelIdFavSongs) {
                    lastChoiceIdForFavTrack = choice;
//                    favoriteSelection = FAVORITE_SELECTION.ALBUM;
                    addPlayerOptionToChoiceSet(MUSIC_PLAYER_CHOICE_SET_ID_FAV);
                } else if (interactionResponse.getCorrelationID() == corrRelIdFavAlbum) {
                    lastChoiceIdForFavAlbum = choice;
//                    favoriteSelection = FAVORITE_SELECTION.ALBUM;
                    addPlayerOptionToChoiceSet(MUSIC_PLAYER_CHOICE_SET_ID_FAV);
                }

                return;

            } else if (interactionResponse.getCorrelationID() == corrRelIdPlayList) {
                lastChoiceIdForMyPlaylist = choice;
                my_playlist_section = MY_PLAYLIST_SECTION.MY_PLAYLIST;
                favoriteSelection = null;
                currentSelection = null;
                offline_songs_selection = null;
                addPlayerOptionToChoiceSet(MY_PLAYLIST_SELECTION_ID);

            } else if (interactionResponse.getCorrelationID() == corrRelIdMoodsLIst) {
                if (moodList != null && moodList.size() > 0) {
                    final Mood mood = moodList.get(choice);
                    Thread thread = new Thread() {
                        public void run() {
                            final Handler handler = new Handler(getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    _mainInstance.moodViseApiCAll(mood);

                                }
                            });
                        }
                    };
                    thread.start();

                }


                return;
            } else if (interactionResponse.getCorrelationID() == corrRelIdLiveRadio) {
                // radio click
                if (list_mediaItem_radio != null && list_mediaItem_radio.size() > 0) {
                    if (PlayerService.service != null && PlayerService.service.getPlayMode() == PlayMode.MUSIC) {
//                        ProxyService.getInstance().initializeSubMenuCommandForSyncPlayer();
                    }
//                    Handler handlerPopular = new Handler(getMainLooper());
//                    handlerPopular.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {+

                    if (_syncProxy == null)
                        return;
                    showSoftButtonsLiveOnRadio();

//                        }
//                    }, 2000);

                    _mainInstance.playLiveStation(list_mediaItem_radio.get(choice));
                }
                return;

            } else if (interactionResponse.getCorrelationID() == corrRelIdOfflineSongs) {
                if (choice == OFFLINE_PLAYALL_CHOICEID) {

                    lastChoiceIdForOfflineSongs = OFFLINE_PLAYALL_CHOICEID;
                    final List<Track> tracks = new ArrayList<Track>();
                    if (mediaItemOfflineTracks != null && mediaItemOfflineTracks.size() > 0) {
                        for (MediaItem mediaItem : mediaItemOfflineTracks) {
                            Track track = new Track(mediaItem.getId(),
                                    mediaItem.getTitle(), mediaItem.getAlbumName(),
                                    mediaItem.getArtistName(), mediaItem.getImageUrl(),
                                    mediaItem.getBigImageUrl(), mediaItem.getImages(),
                                    mediaItem.getAlbumId());
                            tracks.add(track);
                        }
                    }
                    if (tracks.size() > 0) {
                        //mPlayerBarFragment.addToQueue(tracks, null, null);
                        Thread thread = new Thread() {
                            public void run() {
                                final Handler handler = new Handler(getMainLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (tracks != null && tracks.size() > 0) {
                                            HomeActivity.Instance.mPlayerBarFragment.playNowNew(tracks, null, null);
                                        }
                                    }
                                });
                            }
                        };
                        thread.start();

                    }

                } else {
                    lastChoiceIdForOfflineSongs = choice;
                    offline_songs_selection = OFFLINE_SONGS_SELECTION.OFFLNE_SONGS;

                    favoriteSelection = null;
                    currentSelection = null;
                    my_playlist_section = null;

                    addPlayerOptionToChoiceSet(OFFLINE_SECTION_ID);

                }

            } else if (interactionResponse.getCorrelationID() == corrRelIdLanguages) {
                lastChoiceIdForLanguageSelection = choice;
                languageSelection = LANGUAGE_SELECTION.SAVE;
                favoriteSelection = null;
                currentSelection = null;
                my_playlist_section = null;
                offline_songs_selection = null;

                addPlayerOptionToChoiceSet(LANGUAGE_SELECTION_ID);


            } else if (interactionResponse.getCorrelationID() == corrRelIdRepeatMode) {
                lastChoiceIdForRepeat = choice;
                String repeatMode = repeatList.get(lastChoiceIdForRepeat);
                PlayerService.service.setLoopMode(PlayerService.LoopMode.valueOf(repeatMode));
                _mainInstance.setRepeat();


            } else if (interactionResponse.getCorrelationID() == corrRelIdOnDemandRadio) {
                //  on Demand Radio Click
                /*if (list_mediaItem_onDemandRadio != null && list_mediaItem_onDemandRadio.size() > 0)
                    _mainInstance.playLiveStation(list_mediaItem_radio.get(choice));
                return;*/
                Logger.e("OnDemandClick", "clicked");
                if (list_mediaItem_onDemandRadio != null && list_mediaItem_onDemandRadio.size() > 0) {
                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            GetAubumDetails getAlbum = new GetAubumDetails();
                            getAlbum.setDetailCallBack(ProxyService.this);
                            if (Utils.isConnected()) {
                                _mainInstance.pleaseWaitMessage();
                                MediaItem artistItem = list_mediaItem_onDemandRadio.get(choice);
                                getAlbum.getArtistSong(getApplicationContext(), artistItem);
                                //getAlbum.getAlbumDetail(getApplicationContext(), mediaItems.get(content_pos));
                            }
//                                addedsong++;
                        }
                    });
                }
                return;

            } else {

                _mainInstance.playCurrentSong(choice);
                Handler handlerPopular = new Handler(getMainLooper());
                handlerPopular.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (_syncProxy == null)
                            return;
                        showSoftButtonsOnScreen();

                    }
                }, 2000);
            }

            msg.setTtsChunks(chunks);
            msg.setCorrelationID(nextCorrID());
            try {
                _syncProxy.sendRPCRequest(msg);
            } catch (SdlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    /*public void onAlertManeuverResponse(AlertManeuverResponse arg0) {
        // TODO Auto-generated method stub
		
	}*/

    //@Override
    public void onChangeRegistrationResponse(ChangeRegistrationResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteFileResponse(DeleteFileResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDialNumberResponse(DialNumberResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSendLocationResponse(SendLocationResponse sendLocationResponse) {

    }

    @Override
    public void onShowConstantTbtResponse(ShowConstantTbtResponse showConstantTbtResponse) {

    }

    @Override
    public void onAlertManeuverResponse(AlertManeuverResponse alertManeuverResponse) {

    }

    @Override
    public void onUpdateTurnListResponse(UpdateTurnListResponse updateTurnListResponse) {

    }

    @Override
    public void onServiceDataACK() {

    }

    @Override
    public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {
        // TODO Auto-generated method stub

        Log.i("EndAudioPassThru", "-" + response.toString());

        final LockScreenActivity mainActivity = LockScreenActivity.getInstance();
        final Result result = response.getResultCode();
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // to do
//				RecordingAudio.getInstance().endAudioPassThruResponse(result);
            }
        });

    }

    @Override
    public void onGetDTCsResponse(GetDTCsResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOnLockScreenNotification(OnLockScreenStatus onLockScreenStatus) {

    }

    @Override
    public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
        // TODO Auto-generated method stub
        Log.i("onGetVehicleData", response.toString());
    }

    @Override
    public void onListFilesResponse(ListFilesResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOnAudioPassThru(OnAudioPassThru notification) {
        // TODO Auto-generated method stub
        Log.i("OnAudioPassThruNotif", "-" + notification.toString());

        final byte[] aptData = notification.getAPTData();
        LockScreenActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // to do
//		RecordingAudio.getInstance().audioPassThru(aptData);
            }
        });

    }

    @Override
    public void onOnLanguageChange(OnLanguageChange arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onOnHashChange(OnHashChange onHashChange) {

    }

    @Override
    public void onOnVehicleData(OnVehicleData arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {
        // TODO Auto-generated method stub
        Log.i("PerformAudioPassThru", "-" + response);

        final Result result = response.getResultCode();
        LockScreenActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // to do
//				RecordingAudio.getInstance().performAudioPassThruResponse(
//						result);
            }
        });

    }

    @Override
    public void onPutFileResponse(PutFileResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onReadDIDResponse(ReadDIDResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onScrollableMessageResponse(ScrollableMessageResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSetAppIconResponse(SetAppIconResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse arg0) {
        // TODO Auto-generated method stub

    }

    //@Override
    /*public void onShowConstantTBTResponse(ShowConstantTBTResponse arg0) {
        // TODO Auto-generated method stub
		
	}*/

    @Override
    public void onSliderResponse(SliderResponse arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {
        // TODO Auto-generated method stub
        Log.i("onSubscribeVehicledata", response.toString());

    }

    @Override
    public void onUnsubscribeVehicleDataResponse(
            UnsubscribeVehicleDataResponse response) {
        // TODO Auto-generated method stub
        Log.i("onUnSubscribeVehicldata", response.toString());

    }

	/*@Override
    public void onUpdateTurnListResponse(UpdateTurnListResponse arg0) {
		// TODO Auto-generated method stub
		
	}*/

    /**
     * Softbutton for music
     */
    public void showSoftButtonsOnScreen() {

        next = new SoftButton();
        next.setText("Next");
        next.setSoftButtonID(100);
        next.setType(SoftButtonType.SBT_TEXT);
        next.setSystemAction(SystemAction.DEFAULT_ACTION);

        previous = new SoftButton();
        previous.setText("Previous");
        previous.setSoftButtonID(101);
        previous.setType(SoftButtonType.SBT_TEXT);
        previous.setSystemAction(SystemAction.DEFAULT_ACTION);

		/*forward = new SoftButton();
        forward.setText("Forward");
		forward.setSoftButtonID(102);
		forward.setType(SoftButtonType.SBT_TEXT);
		forward.setSystemAction(SystemAction.DEFAULT_ACTION);

		backward = new SoftButton();
		backward.setText("Backward");
		backward.setSoftButtonID(103);
		backward.setType(SoftButtonType.SBT_TEXT);
		backward.setSystemAction(SystemAction.DEFAULT_ACTION);*/

        //new music
        Shuffle = new SoftButton();
        Shuffle.setText("Shuffle");
        Shuffle.setSoftButtonID(102);
        Shuffle.setType(SoftButtonType.SBT_TEXT);
        Shuffle.setSystemAction(SystemAction.DEFAULT_ACTION);

        // live radio
        Repeat = new SoftButton();
        Repeat.setText("Repeat");
        Repeat.setSoftButtonID(104);
        Repeat.setType(SoftButtonType.SBT_TEXT);
        Repeat.setSystemAction(SystemAction.DEFAULT_ACTION);

        // On Demand Radio

        Queue = new SoftButton();
        Queue.setText("Queue");
        Queue.setSoftButtonID(105);
        Queue.setType(SoftButtonType.SBT_TEXT);
        Queue.setSystemAction(SystemAction.DEFAULT_ACTION);

        //AppInfo
//        appInfo = new SoftButton();
//        appInfo.setText("AppInfo");
//        appInfo.setSoftButtonID(103);
//        appInfo.setType(SoftButtonType.SBT_TEXT);
//        appInfo.setSystemAction(SystemAction.DEFAULT_ACTION);

       /* //Favorite
        favorite = new SoftButton();
        favorite.setText("Favorite");
        favorite.setSoftButtonID(106);
        favorite.setType(SoftButtonType.SBT_TEXT);
        favorite.setSystemAction(SystemAction.DEFAULT_ACTION);

        //Offline
        offline = new SoftButton();
        offline.setText("Offline");
        offline.setSoftButtonID(108);
        offline.setType(SoftButtonType.SBT_TEXT);
        offline.setSystemAction(SystemAction.DEFAULT_ACTION);

        //Change language
        change_language = new SoftButton();
        change_language.setText("Change Language");
        change_language.setSoftButtonID(109);
        change_language.setType(SoftButtonType.SBT_TEXT);
        change_language.setSystemAction(SystemAction.DEFAULT_ACTION);
*/

//        cmdInfo = new SoftButton();
//        cmdInfo.setText("CommandInfo");
//        cmdInfo.setSoftButtonID(106);
//        cmdInfo.setType(SoftButtonType.SBT_TEXT);
//        cmdInfo.setSystemAction(SystemAction.DEFAULT_ACTION);
//
//        scrollableMsg = new SoftButton();
//        scrollableMsg.setText("ScrollMsg");
//        scrollableMsg.setSoftButtonID(107);
//        scrollableMsg.setType(SoftButtonType.SBT_TEXT);
//        scrollableMsg.setSystemAction(SystemAction.DEFAULT_ACTION);
//
//        APTHCheck = new SoftButton();
//        APTHCheck.setText("Record");
//        APTHCheck.setSoftButtonID(108);
//        APTHCheck.setType(SoftButtonType.SBT_TEXT);
//        APTHCheck.setSystemAction(SystemAction.DEFAULT_ACTION);
//
//        vehicleData = new SoftButton();
//        vehicleData.setText("Vehicle");
//        vehicleData.setSoftButtonID(109);
//        vehicleData.setType(SoftButtonType.SBT_TEXT);
//        vehicleData.setSystemAction(SystemAction.DEFAULT_ACTION);


        //Send Show RPC:
        Vector<SoftButton> buttons = new Vector<SoftButton>();
        buttons.add(previous);
        buttons.add(next);

        //buttons.add(forward);
        //buttons.add(backward);
        buttons.add(Shuffle);
        buttons.add(Repeat);
        buttons.add(Queue);
//        buttons.add(appInfo);
       /* buttons.add(favorite);
        buttons.add(offline);
        buttons.add(change_language);*/

//        buttons.add(cmdInfo);
//        buttons.add(scrollableMsg);
//        buttons.add(APTHCheck);
//        buttons.add(vehicleData);
        try {
            _syncProxy.show("", "",
                    "", "", null, buttons, null,
                    null, nextCorrID());
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * SoftButton for live radio
     */
    public void showSoftButtonsLiveOnRadio() {
        //Send Show RPC:
        Vector<SoftButton> buttons = new Vector<SoftButton>();

        next = new SoftButton();
        next.setText("-");
        next.setSoftButtonID(10000);
        next.setType(SoftButtonType.SBT_TEXT);
        next.setSystemAction(SystemAction.DEFAULT_ACTION);

        buttons.add(next);

        try {
            _syncProxy.show("", "",
                    "", "", null, buttons, null,
                    null, nextCorrID());
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * SoftButton for on demand radio
     */
    public void showSoftButtonsOnDemandRadio() {

        next = new SoftButton();
        next.setText("Next");
        next.setSoftButtonID(100);
        next.setType(SoftButtonType.SBT_TEXT);
        next.setSystemAction(SystemAction.DEFAULT_ACTION);

        //Send Show RPC:
        Vector<SoftButton> buttons = new Vector<SoftButton>();

        buttons.add(next);

        try {
            _syncProxy.show("", "",
                    "", "", null, buttons, null,
                    null, nextCorrID());
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Remove lock screen from foreground
     */
    private void clearlockscreen() {
        if (LockScreenActivity.getInstance() != null) {
            LockScreenActivity.getInstance().exit();
        }
        lockscreenUP = false;
    }

    private void PerformVoiceRecordingInteraction() {
        Alert alert = new Alert();
        alert.setAlertText1("Voice Recording");
        alert.setAlertText2("Start in 3 seconds");
        alert.setDuration(3000);
        alert.setCorrelationID(nextCorrID());
        Vector<TTSChunk> ttsChunks = new Vector<TTSChunk>();
        ttsChunks.add(TTSChunkFactory.createChunk(SpeechCapabilities.TEXT,
                "Speak to SYNC microphone to record! "));
        alert.setTtsChunks(ttsChunks);
        try {
            _syncProxy.sendRPCRequest(alert);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private boolean isAd(MediaItem mediaItem) {
        if (mediaItem != null) {
            String title = mediaItem.getTitle();
            String albumname = mediaItem.getAlbumName();
            String artistname = mediaItem.getArtistName();
            if (!TextUtils.isEmpty(title) && title.equalsIgnoreCase("no")
                    && !TextUtils.isEmpty(albumname)
                    && albumname.equalsIgnoreCase("no")
                    && !TextUtils.isEmpty(artistname)
                    && artistname.equalsIgnoreCase("no")) {
                return true;
            }
        }
        return false;
    }


    // ============================================================== Player List ================================================================ //

    /**
     * Sub Menu (Options)for songs,radio,album,offline mode,language
     */
    private void addPlayerOptionToChoiceSet(int choiceId) {
        ArrayList<String> musicOptions;
        if (currentSelection == SELECTION.QUEUE && choiceId == MUSIC_PLAYER_CHOICE_SET_ID_QUEUE) {
            musicOptions = new ArrayList<>(Arrays.asList("Play Now", "Add to favorite", "Download"));
            setChoiceSet(musicOptions, MUSIC_PLAYER_CHOICE_SET_ID_QUEUE);
        } else if (favoriteSelection == FAVORITE_SELECTION.ALBUM || favoriteSelection == FAVORITE_SELECTION.SONGS ||
                favoriteSelection == FAVORITE_SELECTION.PLAYLIST && choiceId == MUSIC_PLAYER_CHOICE_SET_ID_FAV) {
            musicOptions = new ArrayList<>(Arrays.asList("Play Now", "Add to queue", "Download"));
            setChoiceSet(musicOptions, MUSIC_PLAYER_CHOICE_SET_ID_FAV);
        } else if (choiceId == MY_PLAYLIST_SELECTION_ID) {
            musicOptions = new ArrayList<>(Arrays.asList("Play Now", "Add to queue", "Download"));
            setChoiceSet(musicOptions, MY_PLAYLIST_SELECTION_ID);
        } else if (choiceId == MUSIC_PLAYER_CHOICE_SET_ID_OTHERS) {
            musicOptions = new ArrayList<>(Arrays.asList("Play Now", "Add to queue", "Add to favorite", "Download"));
            setChoiceSet(musicOptions, MUSIC_PLAYER_CHOICE_SET_ID_OTHERS);
        } else if (choiceId == OFFLINE_SECTION_ID) {
            musicOptions = new ArrayList<>(Arrays.asList("Play Now", "Add to queue"));
            setChoiceSet(musicOptions, OFFLINE_SECTION_ID);
        } else if (choiceId == LANGUAGE_SELECTION_ID) {
            musicOptions = new ArrayList<>(Arrays.asList("Save"));
            setChoiceSet(musicOptions, LANGUAGE_SELECTION_ID);
        }
    }

    String mnPlayerNmae;

    /**
     * setting choice set i.e. VR Command
     */
    private void setChoiceSet(ArrayList<String> options, final int choiceId) {
        Vector<Choice> choice_songs = new Vector<Choice>();
        for (int i = 0; i < options.size(); i++) {
            mnPlayerNmae = options.get(i);

            if (TextUtils.isEmpty(mnPlayerNmae)) {
                mnPlayerNmae = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + mnPlayerNmae;
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{mnPlayerNmae, "PlayerOptions " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total options  :  " + options.size() + "  :: count :  " + (i + 1) + " :  Option_name : " + mnPlayCmd_final);

        }

        Log.d(TAG, "Option vector size  :  " + choice_songs.size());

        // set new list

        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, choiceId, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Available Options", "Available Options", choiceId, nextCorrID());
                corrRelIdPlayerOption = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);

    }


    // =============================================================== New Music =============================================================== //
    List<MediaItem> mediaItems_albums;
    List<MediaItem> list_mediaItem;

    /**
     * Fetch list of songs
     */
    private void addSongsToChoiceSet() {
        if (HomeActivity.Instance != null && HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC) != null) {
            HomeMediaTileGridFragmentNew musicfragment = (HomeMediaTileGridFragmentNew) HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC);
            mediaItems_albums = musicfragment.mediaItems_final;
            if (mediaItems_albums != null && mediaItems_albums.size() > 0) {
//                if (mediaItems.size() > addedsong) {
                if (mediaItems_albums != null && mediaItems_albums.size() > 0) {
                    int count = 0;
                    list_mediaItem = new ArrayList<>();
                    for (int i = 0; i < mediaItems_albums.size(); i++) {
                        if (!isAd(mediaItems_albums.get(i))) {
                            list_mediaItem.add(mediaItems_albums.get(i));
                            count++;
//                                addedsong++;
//                                if (count == 17)
//                                    break;
                        }
                    }
                    setChoiceSet(list_mediaItem);
                }
//                }
            }
        }


    }

    /**
     * List of songs and creating its VR Command
     */
    private void setChoiceSet(List<MediaItem> mediaItems) {
        Vector<Choice> choice_songs = new Vector<Choice>();

        // Delete previous list
//        try {
//            trackMsg = RPCRequestFactory.buildDeleteInteractionChoiceSet(interactionChoiceSetID, nextCorrID());
//            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
//        } catch (SdlException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//            Log.d("Patibandha :: ", "ERrro :: " + e.getMessage());
//        }


        for (int i = 0; i < mediaItems.size(); i++) {
            String mnPlayCmd = mediaItems.get(i).getTitle();

            if (TextUtils.isEmpty(mnPlayCmd))
                mnPlayCmd = mediaItems.get(i).getAlbumName();


            if (TextUtils.isEmpty(mnPlayCmd)) {
                mnPlayCmd = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + mnPlayCmd;
            mnPlayCmd_final = mnPlayCmd_final.replace("-", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("?", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("'", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("(", "");
            mnPlayCmd_final = mnPlayCmd_final.replace(")", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("[", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("]", "");
            mnPlayCmd_final = mnPlayCmd_final.replace(".", "");
            mnPlayCmd_final = mnPlayCmd_final.replace(":", "");
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);

            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{mnPlayCmd, "Album " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total song  :  " + mediaItems.size() + "  ::  Added song count :  " + (i + 1) + " :  Song name : " + mnPlayCmd_final);

        }

        Log.d(TAG, "Added song vector size  :  " + choice_songs.size());

        // set new list

        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, 1031, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "ERrro 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("New Music", "Available Tracks", 1031, nextCorrID());
                corrRelIdAlbum = getlastCorrId();

                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }, 2000);


    }

    private List<Track> tracks_latest;
    private List<String> dulp_tracks_list;
    String albumName;

    /**
     * creating VR Command for tracks List
     */
    private void setChoiceSetTrack(List<Track> tracks) {

        dulp_tracks_list = new ArrayList<>();


        tracks_latest = tracks;
        Vector<Choice> choice_songs = new Vector<Choice>();

        // Delete previous list
        try {
            RPCMessage trackMsg = RPCRequestFactory.buildDeleteInteractionChoiceSet(1032, nextCorrID());
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "ERrro :: " + e.getMessage());
        }


        for (int i = 0; i < tracks.size(); i++) {
            Track track = tracks.get(i);
            String mnPlayCmd = track.getTitle();

            if (!dulp_tracks_list.contains(mnPlayCmd)) {

                dulp_tracks_list.add(mnPlayCmd);

                if (TextUtils.isEmpty(mnPlayCmd))
                    mnPlayCmd = track.getAlbumName();

                Logger.i("Album Name ::", mnPlayCmd);

                if (TextUtils.isEmpty(mnPlayCmd)) {
                    mnPlayCmd = "Hungama Ad " + (i);
                }
                String mnPlayCmd_final = (i + 1) + " " + mnPlayCmd;
                mnPlayCmd_final = mnPlayCmd_final.replace("-", "");
                mnPlayCmd_final = mnPlayCmd_final.replace("?", "");
                mnPlayCmd_final = mnPlayCmd_final.replace("'", "");

                Choice choice1 = new Choice();
                choice1.setChoiceID(i);
                choice1.setMenuName(mnPlayCmd_final);
                choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{mnPlayCmd, "Track " + (i + 1)})));
                choice_songs.addElement(choice1);
                Log.d(TAG, "total song  :  " + tracks.size() + "  ::  Added song count :  " + (i + 1) + " :  Song name : " + mnPlayCmd_final);


            } else {
                tracks_latest.remove(i);
            }


        }

        Log.d(TAG, "Added song vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, 1032, nextCorrID());
        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("Patibandha :: ", "ERrro 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction(albumName + "songs", "Available Tracks", 1032, nextCorrID());
                req.setParameters("songs_list", "songs");
                corrRelIdSong = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }, 2000);
    }


    private void getSongAndMakeChoiceSet(final int content_pos) {

        if (HomeActivity.Instance != null && HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC) != null) {
            HomeMediaTileGridFragmentNew musicfragment = (HomeMediaTileGridFragmentNew) HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC);
            final List<MediaItem> mediaItems = musicfragment.mediaItems_final;
            final List<Track> tracks = new ArrayList<>();
            if (mediaItems != null && mediaItems.size() > 0) {
//                if (mediaItems.size() > addedsong) {
                if (mediaItems != null && mediaItems.size() > 0) {

                    new Handler(getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            GetAubumDetails getAlbum = new GetAubumDetails();
                            getAlbum.setDetailCallBack(ProxyService.this);
                            if (Utils.isConnected()) {
                                _mainInstance.pleaseWaitMessage();
                                MediaItem mediaItemValue = mediaItems.get(content_pos);
                                getAlbum.getAlbumDetail(getApplicationContext(), mediaItemValue);

                                albumName = mediaItemValue.getTitle();

                                if (TextUtils.isEmpty(albumName))
                                    albumName = mediaItemValue.getAlbumName();

                                if (TextUtils.isEmpty(albumName)) {
                                    albumName = "Hungama Ad " + (content_pos);
                                }


                                //albumName = mediaItems.get(content_pos).getAlbumName();
                            }
//                                addedsong++;
                        }
                    });
                }
                //setChoiceSet(list_mediaItem);
//            }
            }
        }
    }

    // ==================================================================== Popular Music ========================================================= //

    List<MediaItem> mediaItemsPopular_albums;
    List<MediaItem> list_mediaItemPopular;

    /**
     * Adding list of popular Album
     */
    private void addPopularSongsToChoiceSet() {
        if (HomeActivity.Instance != null && HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC_POPULAR) != null) {
            HomeMediaTileGridFragmentNew musicfragment = (HomeMediaTileGridFragmentNew) HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_MUSIC_POPULAR);
            mediaItemsPopular_albums = musicfragment.mediaItems_final;
            if (mediaItemsPopular_albums != null && mediaItemsPopular_albums.size() > 0) {
//                if (mediaItems.size() > addedsong) {
                if (mediaItemsPopular_albums != null && mediaItemsPopular_albums.size() > 0) {
                    int count = 0;
                    list_mediaItemPopular = new ArrayList<>();
                    for (int i = 0; i < mediaItemsPopular_albums.size(); i++) {
                        if (!isAd(mediaItemsPopular_albums.get(i))) {
                            list_mediaItemPopular.add(mediaItemsPopular_albums.get(i));
                            count++;
//                                addedsong++;
//                                if (count == 5)
//                                    break;
                        }
                    }
                    setPopularMusicChoiceSet(list_mediaItemPopular);
                }
//                }
            }
        }
    }

    /**
     * Setting choice set for popular music
     */
    private void setPopularMusicChoiceSet(List<MediaItem> mediaItems) {
        Vector<Choice> choice_songs = new Vector<Choice>();

        for (int i = 0; i < mediaItems.size(); i++) {
            String mnPlayCmd = mediaItems.get(i).getTitle();

            if (TextUtils.isEmpty(mnPlayCmd))
                mnPlayCmd = mediaItems.get(i).getAlbumName();


            if (TextUtils.isEmpty(mnPlayCmd)) {
                mnPlayCmd = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + mnPlayCmd;
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{mnPlayCmd, "Music " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total popular song  :  " + mediaItems.size() + "  ::  Added song count :  " + (i + 1) + " :  Song name : " + mnPlayCmd_final);

        }

        Log.d(TAG, "Added popular song vector size  :  " + choice_songs.size());

        // set new list

        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, 1035, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "ERrro 222 :: " + e.getMessage());
        }
        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req2;
                req2 = RPCRequestFactory.buildPerformInteraction("Popular Music", "Available Tracks", 1035, nextCorrID());
                corrRelIDAlbumPopular = getlastCorrId();

                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req2);
                } catch (SdlException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }, 2000);


    }

    private List<Track> popular_tracks_latest;
    private List<String> popular_dulp_tracks_list;
    String popular_albumName;

    /**
     * Creatign VR Command for list of traks
     */
    private void setChoiceSetTrackPopular(List<Track> tracks) {

        popular_dulp_tracks_list = new ArrayList<>();
        popular_tracks_latest = tracks;
        Vector<Choice> choice_songs = new Vector<Choice>();

        // Delete previous list
        try {
            RPCMessage trackMsg = RPCRequestFactory.buildDeleteInteractionChoiceSet(1036, nextCorrID());
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "ERrro :: " + e.getMessage());
        }


        for (int i = 0; i < tracks.size(); i++) {
            Track track = tracks.get(i);
            String mnPlayCmd = track.getTitle();

            if (!popular_dulp_tracks_list.contains(mnPlayCmd)) {

                popular_dulp_tracks_list.add(mnPlayCmd);

                if (TextUtils.isEmpty(mnPlayCmd))
                    mnPlayCmd = track.getAlbumName();

                Logger.i("Album Name ::", mnPlayCmd);

                if (TextUtils.isEmpty(mnPlayCmd)) {
                    mnPlayCmd = "Hungama Ad " + (i);
                }
                String mnPlayCmd_final = (i + 1) + " " + mnPlayCmd;
                mnPlayCmd_final = mnPlayCmd_final.replace("-", "");
                mnPlayCmd_final = mnPlayCmd_final.replace("?", "");
                mnPlayCmd_final = mnPlayCmd_final.replace("'", "");

                Choice choice1 = new Choice();
                choice1.setChoiceID(i);
                choice1.setMenuName(mnPlayCmd_final);
                choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{mnPlayCmd, "Track " + (i + 1)})));
                choice_songs.addElement(choice1);
                Log.d(TAG, "total song  :  " + tracks.size() + "  ::  Added song count :  " + (i + 1) + " :  Song name : " + mnPlayCmd_final);


            } else {
                popular_tracks_latest.remove(i);
            }


        }

        Log.d(TAG, "Added song vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, 1036, nextCorrID());
        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("Patibandha :: ", "ERrro 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction(popular_albumName + "songs", "Available Tracks", 1036, nextCorrID());
                req.setParameters("songs_list", "songs");
                corrRelIdSong = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }, 2000);
    }


    // ==================================================================== live radio ============================================================ //
    List<MediaItem> list_mediaItem_radio;
    List<MediaItem> mediaItems_liveradio;

    /**
     * Creating choice set for live radio
     */
    private void addRadioToChoiceSet() {
        if (HomeActivity.Instance != null && HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO) != null) {
            BrowseRadioFragment musicfragment = (BrowseRadioFragment) HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO);
            mediaItems_liveradio = musicfragment.mMediaItemsLiveRadio;
            if (mediaItems_liveradio != null && mediaItems_liveradio.size() > 0) {
//                if (mediaItems.size() > addedsong) {
                if (mediaItems_liveradio != null && mediaItems_liveradio.size() > 0) {
                    int count = 0;
                    list_mediaItem_radio = new ArrayList<>();

                    List<String> titles = new ArrayList<>();
                    for (int i = 0; i < mediaItems_liveradio.size(); i++) {
                        MediaContentType mediaContentType = mediaItems_liveradio.get(i).getMediaContentType();
                        if (!isAd(mediaItems_liveradio.get(i)) && mediaContentType == MediaContentType.RADIO) {
                            /*if (!list_mediaItem_radio.contains(mediaItems_liveradio.get(i))) {
                                list_mediaItem_radio.add(mediaItems_liveradio.get(i));
                            }
                            count++;*/

                            if (!titles.contains(mediaItems_liveradio.get(i).getTitle())) {
                                list_mediaItem_radio.add(mediaItems_liveradio.get(i));
                                titles.add(mediaItems_liveradio.get(i).getTitle());
                            }
                            count++;

//                                addedsong++;
//                                if (count == 17)
//                                    break;
                        }
                    }
                    setChoiceSetRadio(list_mediaItem_radio);
                }
//                }
            }
        }


    }

    /**
     * Setting choice set for live radio
     */
    private void setChoiceSetRadio(List<MediaItem> mediaItems) {
        Vector<Choice> choice_radio = new Vector<Choice>();

        // Delete previous list
        try {
            RPCMessage trackMsg = RPCRequestFactory.buildDeleteInteractionChoiceSet(1033, nextCorrID());
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("Patibandha :: ", "ERrro :: " + e.getMessage());
        }


        for (int j = 0; j < mediaItems.size(); j++) {
//            if(j==25)
//                break;
            String title_radio = mediaItems.get(j).getTitle();

            if (TextUtils.isEmpty(title_radio))
                title_radio = mediaItems.get(j).getAlbumName();

            if (TextUtils.isEmpty(title_radio)) {
                title_radio = "Hungama Ad " + (j);
            }
            String title_radio_final = (j + 1) + " " + title_radio;
            Choice radio_item = new Choice();
            radio_item.setChoiceID(j);
            radio_item.setMenuName(title_radio_final);
            radio_item.setVrCommands(new Vector<String>(Arrays.asList(new String[]{title_radio, "Radio " + (j + 1)})));
            choice_radio.addElement(radio_item);
            Log.d(TAG, "total Radio  :  " + mediaItems.size() + "  ::  Added Radio count :  " + (j + 1) + " :  Radio name : " + title_radio_final);

        }

        Log.d(TAG, "Added Radio vector size  :  " + choice_radio.size());

        // set new list

        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_radio, 1033, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Live Radio", "Available live radio", 1033, nextCorrID());
                corrRelIdLiveRadio = getlastCorrId();

                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }, 2000);

    }


    //================================================== On Demand radio section======================================================//


    List<MediaItem> list_mediaItem_onDemandRadio;
    List<MediaItem> mediaItems_onDemandRadio;

    /**
     * Creating choice set for on demand radio
     */
    private void addOnDemandRadioToChoiceSet() {
        if (HomeActivity.Instance != null && HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO) != null) {
            BrowseRadioFragment musicfragment = (BrowseRadioFragment) HomeActivity.Instance.adapter.getCurrentFragment(HomeTabBar.TAB_INDEX_RADIO);
            mediaItems_onDemandRadio = musicfragment.mMediaItemsTopArtists;
            if (mediaItems_onDemandRadio != null && mediaItems_onDemandRadio.size() > 0) {
//                if (mediaItems.size() > addedsong) {
                if (mediaItems_onDemandRadio != null && mediaItems_onDemandRadio.size() > 0) {
                    int count = 0;
                    list_mediaItem_onDemandRadio = new ArrayList<>();

                    List<String> titles = new ArrayList<>();
                    for (int i = 0; i < mediaItems_onDemandRadio.size(); i++) {
                        MediaContentType mediaContentType = mediaItems_onDemandRadio.get(i).getMediaContentType();

                        if (!isAd(mediaItems_onDemandRadio.get(i)) && mediaContentType == MediaContentType.RADIO) {
                            /*if (!list_mediaItem_radio.contains(mediaItems_liveradio.get(i))) {
                                list_mediaItem_radio.add(mediaItems_liveradio.get(i));
                            }
                            count++;*/

                            if (!titles.contains(mediaItems_onDemandRadio.get(i).getTitle())) {
                                list_mediaItem_onDemandRadio.add(mediaItems_onDemandRadio.get(i));
                                titles.add(mediaItems_onDemandRadio.get(i).getTitle());
                            }
                            count++;

//                                addedsong++;
//                                if (count == 17)
//                                    break;
                        }
                    }
                    setChoiceSetOnDemandRadio(list_mediaItem_onDemandRadio);
                }
//                }
            }
        }


    }

    /**
     * Setting VR Command for on demand radio
     */
    private void setChoiceSetOnDemandRadio(List<MediaItem> mediaItems) {
        Vector<Choice> choice_radio = new Vector<Choice>();

        // Delete previous list
        try {
            RPCMessage trackMsg = RPCRequestFactory.buildDeleteInteractionChoiceSet(1034, nextCorrID());
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("Error :: ", "Error :: " + e.getMessage());
        }


        for (int j = 0; j < mediaItems.size(); j++) {
//            if(j==25)
//                break;
            String title_radio = mediaItems.get(j).getTitle();

            if (TextUtils.isEmpty(title_radio))
                title_radio = mediaItems.get(j).getAlbumName();

            if (TextUtils.isEmpty(title_radio)) {
                title_radio = "Hungama Ad " + (j);
            }
            String title_radio_final = (j + 1) + " " + title_radio;
            Choice radio_item = new Choice();
            radio_item.setChoiceID(j);
            radio_item.setMenuName(title_radio_final);
            radio_item.setVrCommands(new Vector<String>(Arrays.asList(new String[]{title_radio, "On Demand Radio " + (j + 1)})));
            choice_radio.addElement(radio_item);
            Log.d(TAG, "total ondemand Radio  :  " + mediaItems.size() + "  ::  Added Ondemand Radio count :  " + (j + 1) + " : OnDemand Radio name : " + title_radio_final);

        }

        Log.d(TAG, "Added On demand Radio vector size  :  " + choice_radio.size());

        // set new list

        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_radio, 1034, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }


        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("OnDemand Radio", "Available OnDemand radio", 1034, nextCorrID());
                corrRelIdOnDemandRadio = getlastCorrId();

                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }, 2000);


    }


    // ===================================================== Queue Listing and Play ================================================================== //

    /**
     * Creating choice set for player queue
     */
    public void addPlayerQueueToMenu() {
        try {
            if (PlayerService.service != null) {
                mTracks = PlayerService.service.getPlayingQueue();
                if (mTracks != null && mTracks.size() > 0) {
//                    for (int i = 0; i < mTracks.size(); i++) {
//                        String mnPlayCmd = new String(mTracks.get(i).getTitle());
//                        _syncProxy.addCommand(i, mnPlayCmd, nextCorrID());
//                        Logger.e("mnPlayCmd add player queue", mnPlayCmd);
//                        playerQueueIdLast = i;
//                        try {
//                            Thread.sleep(100);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
                    setChoiceSetForQueue(mTracks);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }


    private List<Track> tracks_queue;
    private List<String> queue_tracks_list;

    /**
     * Setting choice set for player queue i.e VR Command
     */
    private void setChoiceSetForQueue(List<Track> tracks) {

        queue_tracks_list = new ArrayList<>();


        tracks_queue = tracks;
        Vector<Choice> choice_songs = new Vector<Choice>();

        // Delete previous list
        try {
            RPCMessage trackMsg = RPCRequestFactory.buildDeleteInteractionChoiceSet(1037, nextCorrID());
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d(TAG, "Error :: " + e.getMessage());
        }


        for (int i = 0; i < tracks.size(); i++) {
            Track track = tracks.get(i);
            String mnPlayCmd = track.getTitle();

            if (!queue_tracks_list.contains(mnPlayCmd)) {

                queue_tracks_list.add(mnPlayCmd);

                Logger.i("Album Name ::", mnPlayCmd);

                if (TextUtils.isEmpty(mnPlayCmd)) {
                    mnPlayCmd = "Hungama Ad " + (i);
                }
                String mnPlayCmd_final = (i + 1) + " " + mnPlayCmd;
                mnPlayCmd_final = mnPlayCmd_final.replace("-", "");
                mnPlayCmd_final = mnPlayCmd_final.replace("?", "");
                mnPlayCmd_final = mnPlayCmd_final.replace("'", "");

                Choice choice1 = new Choice();
                choice1.setChoiceID(i);
                choice1.setMenuName(mnPlayCmd_final);
                choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{mnPlayCmd, "Track " + (i + 1)})));
                choice_songs.addElement(choice1);
                Log.d(TAG, "total song in queue  :  " + tracks.size() + "  ::  Song added in queue count :  " + (i + 1) + " :  Queue Song name : " + mnPlayCmd_final);


            } else {
                tracks_queue.remove(i);
            }
        }

        Log.d(TAG, "Added Queue song vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, 1037, nextCorrID());
        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d("Error :: ", "ERrro 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Player Queue", "Available Tracks", 1037, nextCorrID());
                corrRelIdQueue = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }
            }
        }, 2000);


    }


    // ================================================ Load Playlist ============================================================================= //

    ArrayList<Playlist> playlistList;

    /**
     * Create choice set for playlist
     */
    private void addPlaylistToChoiceSet() {
        playlistList = _mainInstance.loadPlayList();
        if (playlistList != null && playlistList.size() > 0) {
            setPlaylistChoiceSet(playlistList);
        } else {

            _mainInstance.noPlayListToShow();

        }

    }

    String mnPlayListName;

    /**
     * Setting VR Command for playlist
     */
    private void setPlaylistChoiceSet(ArrayList<Playlist> options) {
        Vector<Choice> choice_songs = new Vector<Choice>();
        for (int i = 0; i < options.size(); i++) {
            mnPlayListName = options.get(i).getName();

            if (TextUtils.isEmpty(mnPlayListName)) {
                mnPlayListName = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + mnPlayListName;
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{mnPlayListName, "My PlayList " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total playlist  :  " + options.size() + "  :: count :  " + (i + 1) + " :  My_playlist_name : " + mnPlayCmd_final);

        }

        Log.d(TAG, "playlist vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, PLAYLIST_CHOICESET_ID, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("My PlayList", "My PlayList", PLAYLIST_CHOICESET_ID, nextCorrID());
                corrRelIdPlayList = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);

    }

// ===================================================== Discovery Mood List ============================================================================ //

    List<Mood> moodList;

    /**
     * Creating choice set for discover(moods)
     */
    private void addMoodsListToChoiceSet() {
        moodList = _mainInstance.getMoods();
        if (moodList != null && moodList.size() > 0) {
            setMoodListChoiceSet(moodList);
        } else {
            _mainInstance.noMoodsToShow();
        }
    }

    private String mnMoodName;

    /**
     * Setting VR Command for moods
     */
    private void setMoodListChoiceSet(List<Mood> moodList) {


        Vector<Choice> choice_songs = new Vector<Choice>();
        for (int i = 0; i < moodList.size(); i++) {
            mnMoodName = moodList.get(i).getName();

            if (TextUtils.isEmpty(mnMoodName)) {
                mnMoodName = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + mnMoodName;
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{mnMoodName, "Moods " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total Moods  :  " + moodList.size() + "  :: count :  " + (i + 1) + " :  Mood_name : " + mnPlayCmd_final);

        }

        Log.d(TAG, "Moods vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, MOODS_CHOICESET_ID, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Moods", "Moods", MOODS_CHOICESET_ID, nextCorrID());
                corrRelIdMoodsLIst = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);

    }

    // ============================================================= Favorite Listing and Playing ================================================== //

    private List<MediaItem> mediaItemListFavorite;
    private String favoriteAlbumName;

    /**
     * Creating choice set for favorite list
     */
    public void addFavoriteItemsToChoiceSet(List<MediaItem> mediaItemListFav) {

        if (mediaItemListFav != null && mediaItemListFav.size() > 0) {
            mediaItemListFavorite = mediaItemListFav;
            if (favoriteSelection == FAVORITE_SELECTION.ALBUM)
                setFavoriteChoiceSet(mediaItemListFavorite);
            else if (favoriteSelection == FAVORITE_SELECTION.SONGS)
                setFavoriteSongsChoiceSet(mediaItemListFavorite);
            else if (favoriteSelection == FAVORITE_SELECTION.ARTIST_RADIO)
                setFavoriteRadioChoiceSet(mediaItemListFavorite);
            else if (favoriteSelection == FAVORITE_SELECTION.PLAYLIST)
                setFavoritePlaylistChoiceSet(mediaItemListFavorite);

        } else {
            _mainInstance.noFavoritesToShow();
        }
    }

    /**
     * Setting VR Command for favorite list
     */
    private void setFavoriteChoiceSet(List<MediaItem> mediaItemFav) {

        Vector<Choice> choice_songs = new Vector<Choice>();
        for (int i = 0; i < mediaItemFav.size(); i++) {
            favoriteAlbumName = mediaItemFav.get(i).getAlbumName();

            if (TextUtils.isEmpty(favoriteAlbumName)) {
                favoriteAlbumName = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + favoriteAlbumName;
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{favoriteAlbumName, "FavList " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total FavList  :  " + mediaItemFav.size() + "  :: count :  " + (i + 1) + " :  Fav_name : " + mnPlayCmd_final);

        }

        Log.d(TAG, "Fav vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, FAVORITE_ALBUM_CHOICESET_ID, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Favorite Album", "Favorite Album", FAVORITE_ALBUM_CHOICESET_ID, nextCorrID());
                corrRelIdFavAlbum = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);
    }


    private String favoriteSongsName;

    /**
     * Setting favorite songs VR Command
     */
    private void setFavoriteSongsChoiceSet(List<MediaItem> mediaItemFav) {

        Vector<Choice> choice_songs = new Vector<Choice>();
        for (int i = 0; i < mediaItemFav.size(); i++) {
            favoriteSongsName = mediaItemFav.get(i).getTitle();

            if (TextUtils.isEmpty(favoriteSongsName)) {
                favoriteSongsName = "Hungama Ad " + (i);
            }

            String mnPlayCmd_final = (i + 1) + " " + favoriteSongsName;
            mnPlayCmd_final = mnPlayCmd_final.replace("-", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("?", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("'", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("(", "");
            mnPlayCmd_final = mnPlayCmd_final.replace(")", "");
            mnPlayCmd_final = mnPlayCmd_final.replace(".", "");

            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{favoriteSongsName, "FavList " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total FavList  :  " + mediaItemFav.size() + "  :: count :  " + (i + 1) + " :  Fav_name : " + mnPlayCmd_final);
//            if(i==2)
//                break;

        }

        Log.d(TAG, "Fav vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, FAVORITE_SONGS_CHOICESET_ID, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Favorite Songs", "Favorite Songs", FAVORITE_SONGS_CHOICESET_ID, nextCorrID());
                corrRelIdFavSongs = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);
    }

    String favoriteArtistName;

    /**
     * Setting favorite radio choice list
     */

    private void setFavoriteRadioChoiceSet(List<MediaItem> mediaItemFav) {

        Vector<Choice> choice_songs = new Vector<Choice>();
        for (int i = 0; i < mediaItemFav.size(); i++) {
            favoriteArtistName = mediaItemFav.get(i).getTitle();

            if (TextUtils.isEmpty(favoriteArtistName)) {
                favoriteArtistName = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + favoriteArtistName;
            mnPlayCmd_final = mnPlayCmd_final.replace("-", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("?", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("'", "");
            mnPlayCmd_final = mnPlayCmd_final.replace("(", "");
            mnPlayCmd_final = mnPlayCmd_final.replace(")", "");
            mnPlayCmd_final = mnPlayCmd_final.replace(".", "");

            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{favoriteArtistName, "FavList " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total FavList  :  " + mediaItemFav.size() + "  :: count :  " + (i + 1) + " :  Fav_name : " + mnPlayCmd_final);

        }

        Log.d(TAG, "Fav vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, FAVORITE_ARTIST_RADIO_CHOICESET_ID, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Favorite Artist", "Favorite Artist", FAVORITE_ARTIST_RADIO_CHOICESET_ID, nextCorrID());
                corrRelIdFavArtistRadio = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);
    }

    private String FavoritePlaylistName;

    /**
     * Setting favorite playist choice set i.e. VR Command
     */
    private void setFavoritePlaylistChoiceSet(List<MediaItem> mediaItemFav) {

        Vector<Choice> choice_songs = new Vector<Choice>();
        for (int i = 0; i < mediaItemFav.size(); i++) {
            FavoritePlaylistName = mediaItemFav.get(i).getTitle();

            if (TextUtils.isEmpty(FavoritePlaylistName)) {
                FavoritePlaylistName = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + FavoritePlaylistName;
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{FavoritePlaylistName, "FavList " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total FavList  :  " + mediaItemFav.size() + "  :: count :  " + (i + 1) + " :  Fav_name : " + mnPlayCmd_final);

        }

        Log.d(TAG, "Fav vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, FAVORITE_PLAYLIST_CHOICESET_ID, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Favorite Playlist", "Favorite Playlist", FAVORITE_PLAYLIST_CHOICESET_ID, nextCorrID());
                corrRelIdFavPlaylist = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);
    }

    // ========================================================= Offline section ============================================================= //

    List<MediaItem> mediaItemOfflineTracks;

    /**
     * Creating choice set for offline songs
     */
    private void addOfflineToChoiceSet() {
        mediaItemOfflineTracks = _mainInstance.getOfflineSongs();
        if (mediaItemOfflineTracks != null && mediaItemOfflineTracks.size() > 0) {
            setOfflineSongChoiceSet(mediaItemOfflineTracks);
        } else {
            _mainInstance.noOfflineTracks();
        }
    }

    String offlineSongsName;

    /**
     * Setting VR Command for offline songs
     */
    private void setOfflineSongChoiceSet(List<MediaItem> mediaItemList) {

        Vector<Choice> choice_songs = new Vector<Choice>();

        String mnPlayAll = (1) + " " + "Play All";
        Choice choicePlayAll = new Choice();
        choicePlayAll.setChoiceID(OFFLINE_PLAYALL_CHOICEID);
        choicePlayAll.setMenuName(mnPlayAll);
        choicePlayAll.setVrCommands(new Vector<String>(Arrays.asList(new String[]{mnPlayAll, "Offline songs " + (1)})));
        choice_songs.addElement(choicePlayAll);
        Log.d(TAG, "total offline songs  :  " + mediaItemList.size() + "  :: count :  " + (1) + " :  Offline songs : " + mnPlayAll);

        for (int i = 0; i < mediaItemList.size(); i++) {
            offlineSongsName = mediaItemList.get(i).getTitle();
            if (TextUtils.isEmpty(offlineSongsName)) {
                offlineSongsName = "Hungama Ad " + (i);
            }
            int j = i + 1;
            String mnPlayCmd_final = (j + 1) + " " + offlineSongsName;
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{offlineSongsName, "Offline songs " + (j + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total offline songs  :  " + mediaItemList.size() + "  :: count :  " + (j + 1) + " :  Offline songs : " + mnPlayCmd_final);

        }

        Log.d(TAG, "offline songs vector size  :  " + choice_songs.size());

        // set new list
        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, OFFLINE_CHOICESET_ID, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Offline songs", "Offline songs", OFFLINE_CHOICESET_ID, nextCorrID());
                corrRelIdOfflineSongs = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);
    }

    // ================================================ Change Language Section ==================================================================== //

    List<String> languagePreferenceList;

    /**
     * Creating choice set for change preference(Language)
     */
    private void addLanguagePreferenceList() {
        languagePreferenceList = _mainInstance.getEditorPics();
        if (languagePreferenceList != null && languagePreferenceList.size() > 0) {

            languageSelection = LANGUAGE_SELECTION.SAVE;
            setLangaugaePreferenceChoiceSet(languagePreferenceList);
        }
    }

    String languageName;

    /**
     * Setting VR Command for change preference(Language)
     */
    private void setLangaugaePreferenceChoiceSet(List<String> languageList) {

        Vector<Choice> choice_songs = new Vector<Choice>();
        for (int i = 0; i < languageList.size(); i++) {
            languageName = languageList.get(i);

            if (TextUtils.isEmpty(languageName)) {
                languageName = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + languageName;
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{languageName, "Language name " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "total Names  :  " + languageList.size() + "  :: count :  " + (i + 1) + " :  Language_name : " + mnPlayCmd_final);

        }

        Log.d(TAG, " Languages vector size  :  " + choice_songs.size());

        // set new list

        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, CHANGE_LANGUAGE_SELECTION_CHOICESET_ID, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Select language", "Select language", CHANGE_LANGUAGE_SELECTION_CHOICESET_ID, nextCorrID());
                corrRelIdLanguages = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);
    }


    @Override
    public void onDetailCallback(List<Track> tracks) {
        setChoiceSetTrack(tracks);
        setChoiceSetTrackPopular(tracks);
    }

    @Override
    public void onTopArtistPlay(List<Track> tracks) {
        if (PlayerService.service.getPlayMode() == PlayMode.MUSIC) {
//            ProxyService.getInstance().deleteAndUpdatePlayerQueueToMenu();
            ProxyService.getInstance().initializeSubMenuCommandForSyncPlayer();
        }
        _mainInstance.playOndemandRadio(tracks);
        Handler handlerPopular = new Handler(getMainLooper());
        handlerPopular.postDelayed(new Runnable() {
            @Override
            public void run() {

                if (_syncProxy == null)
                    return;
                showSoftButtonsOnDemandRadio();

            }
        }, 2000);
    }

    // ======================================================= Repeat Function ============================================== //

    private List<String> repeatList;

    /**
     * Repeating current song
     */
    private void addRepeatOptions() {
        repeatList = new ArrayList<>(Arrays.asList("ON", "REAPLAY_SONG", "OFF"));
        setRepeatChoiceSet(repeatList);

    }

    String repeatOptionsName;

    /**
     * VR Command Repeating current song
     */
    private void setRepeatChoiceSet(List<String> options) {

        Vector<Choice> choice_songs = new Vector<Choice>();
        for (int i = 0; i < options.size(); i++) {
            repeatOptionsName = options.get(i);

            if (TextUtils.isEmpty(repeatOptionsName)) {
                repeatOptionsName = "Hungama Ad " + (i);
            }
            String mnPlayCmd_final = (i + 1) + " " + repeatOptionsName;
            Choice choice1 = new Choice();
            choice1.setChoiceID(i);
            choice1.setMenuName(mnPlayCmd_final);
            choice1.setVrCommands(new Vector<String>(Arrays.asList(new String[]{repeatOptionsName, "Repeat name " + (i + 1)})));
            choice_songs.addElement(choice1);
            Log.d(TAG, "Repeat Names  :  " + options.size() + "  :: count :  " + (i + 1) + " :  Repeat_name : " + mnPlayCmd_final);

        }

        Log.d(TAG, " Repeat vector size  :  " + choice_songs.size());

        // set new list

        RPCMessage trackMsg = RPCRequestFactory.buildCreateInteractionChoiceSet(choice_songs, REPEAT_CHOICESET_ID, nextCorrID());

        try {
            _syncProxy.sendRPCRequest((RPCRequest) trackMsg);
        } catch (SdlException e) {
            e.printStackTrace();
            Log.d(TAG, "Error 222 :: " + e.getMessage());
        }

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                RPCMessage req;
                req = RPCRequestFactory.buildPerformInteraction("Select repeat mode", "Select repeat mode", REPEAT_CHOICESET_ID, nextCorrID());
                corrRelIdRepeatMode = getlastCorrId();
                try {
                    _syncProxy.sendRPCRequest((RPCRequest) req);
                } catch (SdlException e) {
                    e.printStackTrace();
                }

            }
        }, 2000);
    }

    /**
     * Check connection of device and display message on screen
     */
    @Override
    public void onConnectionFailure(CommunicationManager.ErrorType errorType) {

        Logger.e("errorType", "" + errorType);
        if (errorType == CommunicationManager.ErrorType.NO_CONNECTIVITY) {
            showNoConnectionMessage();
        } else if (errorType == CommunicationManager.ErrorType.INTERNAL_SERVER_APPLICATION_ERROR) {

        } else if (errorType == CommunicationManager.ErrorType.CONTENT_NOT_AVAILABLE) {

        } else if (errorType == CommunicationManager.ErrorType.EXPIRED_REQUEST_TOKEN) {

        } else if (errorType == CommunicationManager.ErrorType.INVALID_REQUEST_PARAMETERS) {

        } else if (errorType == CommunicationManager.ErrorType.OPERATION_CANCELLED) {

        }
    }


    public void deletePleaseWaitCommand() {
        try {
            RPCMessage rpcMessage;
            rpcMessage = RPCRequestFactory.buildDeleteInteractionChoiceSet(_mainInstance.lastPleaseWaitCorrelationId, nextCorrID());
            _syncProxy.sendRPCRequest((RPCRequest) rpcMessage);
        } catch (SdlException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("Error :: ", "Error :: " + e.getMessage());
        }
    }


}
