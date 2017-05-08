package com.hungama.myplay.activity.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apsalar.sdk.Apsalar;
import com.bosch.myspin.serversdk.MySpinException;
import com.bosch.myspin.serversdk.MySpinServerSDK;
import com.hungama.hungamamusic.lite.R;
import com.hungama.hungamamusic.lite.carmode.fragment.CategorySelectionFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.DiscoveryPlayerFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.FavoriteFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.FavoriteTypeSelectionFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.FullScreenPlayerFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.MainMenuFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.MusicDetailFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.MusicFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.OfflineFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.PlayerQueueFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.PlaylistDetailFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.PlaylistFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.RadioFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.RadioPlayerFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.SearchFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.SearchResultFragment;
import com.hungama.hungamamusic.lite.carmode.fragment.SplashScreenFragment;
import com.hungama.hungamamusic.lite.carmode.util.GlobalFunction;
import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.myplay.activity.HungamaApplication;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.Playlist;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaSetDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.NewVersionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.PlayerOption;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionCheckResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.VersionCheckResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.NewVersionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.VersionCheckOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.services.ReloadTracksDataService;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.fragments.BackHandledFragment;
import com.hungama.myplay.activity.ui.fragments.PlayerBarFragment.TrackReloadReceiver;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;
import com.urbanairship.UAirship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author hungama2
 */

public class CarModeHomeActivity extends ActionBarActivity implements
        MySpinServerSDK.ConnectionStateListener,
        /*ServiceConnection, */CommunicationOperationListener, BackHandledFragment.BackHandlerInterface {
    //response:******
    private static final String TAG = "HomeActivity";
    public static final String ACTION_CLOSE_APP = "action_close_app";
    public static final int UPGRADE_ACTIVITY_RESULT_CODE = 1001;
    public static final int LOGIN_ACTIVITY_CODE = 1002;

    private CacheStateReceiver cacheStateReceiver;

    private OfflineModeReceiver offlineModeReceiver;

    private LanguageChangeReceiver languageChangeReceiver;
    private CloseAppReceiver closeAppReceiver;
    private TrackReloadReceiver mTrackReloadReceiver;

    private BackHandledFragment selectedFragment;

    public static CarModeHomeActivity Instance = null;

    private PlayerServiceBindingManager.ServiceToken mServiceToken = null;

    @Override
    public void setSelectedFragment(BackHandledFragment selectedFragment) {
        this.selectedFragment = selectedFragment;
    }

//    @Override
//    public void onServiceConnected(ComponentName name, IBinder service) {
//        Logger.v(TAG, "HomeActivity-----onServiceConnected");
//        PlayerService.PlayerSericeBinder binder = (PlayerService.PlayerSericeBinder) service;
//        binder.getService();
//    }
//
//    @Override
//    public void onServiceDisconnected(ComponentName name) {
//        Logger.d(TAG, "Player bar disconnected from service.");
//    }

    private Context mContext;
    private ApplicationConfigurations mApplicationConfigurations;

    /**
     * List of ongoing operations id. Used for showing progress bar.
     */
    private List<Integer> mOperationsList = new ArrayList<Integer>();

    private boolean mActivityStopped = false;

    private static boolean wasInBackground;

    private CampaignsManager mCampaignsManager;

    public final static String NOTIFICATION_MAIL = "mail";

    public static boolean set;
    public static DisplayMetrics metrics;

    private boolean mHasSubscriptionPlan;
    DataManager mDataManager;

    protected void setOverlayAction() {
        if (getWindow() != null)
            getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
    }

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.s(System.currentTimeMillis() + " :::::::::::::Stratup::::::::::::: " + getClass().getName());
        setOverlayAction();
        Logger.s(System.currentTimeMillis() + " :::::::::::::Stratup::::::::::::: 21 " + getClass().getName());
        Logger.s(System.currentTimeMillis()
                + " :::::::::::::SetContentView::::::::::::: Start");
        super.onCreate(savedInstanceState);

        if (isFinishing())
            return;
        bNeedToLoadMusicPlayer = true;
        curViewId = R.layout.carmode_activity_home;
        setContentView(R.layout.carmode_activity_home);

//        if (PlayerService.service != null) {
//            PlayerService.service.pause();
//        }

        Logger.s("Track HomeActivity onCreate");
        Logger.s("1 HomeTime:" + System.currentTimeMillis());

        Instance = this;

        mContext = getApplicationContext();
        mDataManager = DataManager.getInstance(mContext);
        Logger.s("5 HomeTime:" + System.currentTimeMillis());
        mApplicationConfigurations = mDataManager.getApplicationConfigurations();

        registerReceivers();
        Logger.s("registerReceivers");

        if (!mApplicationConfigurations.isVersionChecked()) {
            //mDataManager.newVersionCheck(CarModeHomeActivity.this);
        }

        // Check for user subscription
        String sesion = mApplicationConfigurations.getSessionID();
        boolean isRealUser = mApplicationConfigurations.isRealUser();
        if (!TextUtils.isEmpty(sesion) && isRealUser) {
            mHasSubscriptionPlan = mApplicationConfigurations.isUserHasSubscriptionPlan();
            String accountType = Utils.getAccountName(getApplicationContext());
            mDataManager.getCurrentSubscriptionPlan(CarModeHomeActivity.this, accountType);
        }


        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                getSupportFragment(false);
            }
        });

        /*if (savedInstanceState != null) {
            int backCount = getSupportFragmentManager().getBackStackEntryCount();
            for (int i = backCount; i > 0; i--) {
                Logger.e("backCount", "" + backCount);

                int backStackId = getSupportFragmentManager().getBackStackEntryAt(i - 1).getId();
                getSupportFragmentManager().popBackStackImmediate(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }
        if (OnApplicationStartsActivity.needToStartApsalrSession) {
            Apsalar.startSession(getApplicationContext(), getString(R.string.apsalar_api_key), getString(R.string.apsalar_secret));
            ApsalarEvent.postEvent(getApplicationContext(), ApsalarEvent.APP_LAUNCH_EVENT);
            OnApplicationStartsActivity.needToStartApsalrSession = false;
        }*/

        ApsalarEvent.postEvent(this, ApsalarEvent.LOADING_THE_HOME);
    }

    public void getSupportFragment(boolean isFromChild) {
        try {
            int backCount = getSupportFragmentManager().getBackStackEntryCount();

            if (Utils.isCarMode()) {
                FragmentManager.BackStackEntry backEntry = getSupportFragmentManager().getBackStackEntryAt(backCount - 1);
                fragMainCar = getSupportFragmentManager().findFragmentByTag(backEntry.getName());
            }

        } catch (Exception e) {

        } catch (Error e) {

        }
    }


    /**
     * Satellite menu option for Music New and Popular. It will open
     * TransperentActivity.
     */

    @Override
    protected void onStart() {

        super.onStart();

        mServiceToken = PlayerServiceBindingManager.bindToService(this);

        try {
            MySpinServerSDK.sharedInstance().registerConnectionStateListener(this);
        } catch (MySpinException e) {
            e.printStackTrace();
        }

        if (isFinishing())
            return;
        mActivityStopped = false;
        Analytics.startSession(this);
        Analytics.onPageView();
        // Flurry report: Status

        int registrationStatus = 0;
        int paidStatus = 0;

        if (mApplicationConfigurations.isRealUser()) {
            registrationStatus = 1;
        }
        if (mApplicationConfigurations.isUserHasSubscriptionPlan()) {
            paidStatus = 1;
        }

        Map<String, String> reportMap = new HashMap<String, String>();
        reportMap.put(
                FlurryConstants.FlurryUserStatus.RegistrationStatus.toString(),
                String.valueOf(registrationStatus));
        reportMap.put(FlurryConstants.FlurryUserStatus.PaidStatus.toString(),
                String.valueOf(paidStatus));
        Analytics.logEvent(FlurryConstants.FlurryUserStatus.Status.toString(),
                reportMap);

//		new Thread() {
//			public void run() {
        // Write UA APIID & tags to file on sdcard for debuging &
        // testing purpose
        String apid = UAirship.shared().getPushManager().getChannelId();
        Logger.s("PUSH APID: >>>>>>>>>>>>>>>>>" + apid);
        Logger.writetofile_("PUSH APID: >>>>>>>>>>>>>>>>>" + apid, false);
        String gcmtoken = UAirship.shared().getPushManager().getGcmToken();
        Logger.s("GCM TOKEN: >>>>>>>>>>>>>>>>>" + gcmtoken);
        call_gcmtoken(gcmtoken);
        Set<String> tagset = Utils.getTags();
        String tags = "";
        Iterator<String> itr = tagset.iterator();
        while (itr.hasNext()) {
            tags += itr.next() + ",";
        }

        Logger.s("UA TAGS: >>>>>>>>>>>>>>>>>" + tags);
        Logger.writetofile("UA TAGS: >>>>>>>>>>>>>>>>>" + tags, true);

//		String alias = UAirship.shared().getPushManager().getAlias();
        String alias = UAirship.shared().getPushManager().getNamedUser().getId();
        if (TextUtils.isEmpty(alias)) {
            String hardwareId = mDataManager.getDeviceConfigurations().getHardwareId();
            if (!TextUtils.isEmpty(mApplicationConfigurations.getHungamaEmail()))
                Utils.setAlias(mApplicationConfigurations.getHungamaEmail(), hardwareId);
            else if (!TextUtils.isEmpty(mApplicationConfigurations.getGigyaFBEmail()))
                Utils.setAlias(mApplicationConfigurations.getGigyaFBEmail(), hardwareId);
            else if (!TextUtils.isEmpty(mApplicationConfigurations.getGigyaTwitterEmail()))
                Utils.setAlias(mApplicationConfigurations.getGigyaTwitterEmail(), hardwareId);
            else if (!TextUtils.isEmpty(mApplicationConfigurations.getGigyaGoogleEmail()))
                Utils.setAlias(mApplicationConfigurations.getGigyaGoogleEmail(), hardwareId);
            else
                Utils.setAlias(null, hardwareId);
        } else {
            Logger.e("PUSH TAGS: ", "Alias: >>>>>>>>>>>>>>>>>" + alias);
            Logger.writetofile("Alias: >>>>>>>>>>>>>>>>>" + alias, true);
        }
//			}
//		}.start();

        Logger.s("4 HomeTime:onStart" + System.currentTimeMillis());
    }

    @Override
    protected void onResume() {
        Logger.s(System.currentTimeMillis()
                + " :::::::::::::Stratup:::::::::::::1 " + getClass().getName());
        HungamaApplication.activityResumed();
        super.onResume();
//        if (Utils.isCarMode() || isFinishing())
//            return;
        RelativeLayout rlDialogLayout = (RelativeLayout) findViewById(R.id.rl_custom_dialog);
        if (rlDialogLayout != null && rlDialogLayout.getVisibility() == View.VISIBLE)
            rlDialogLayout.setVisibility(View.GONE);
    }

//    private void openMyCollection() {
//        MyCollectionActivity mTilesFragment = new MyCollectionActivity();
//
//        FragmentManager mFragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = mFragmentManager
//                .beginTransaction();
//        fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
//                R.anim.slide_left_exit, R.anim.slide_right_enter,
//                R.anim.slide_right_exit);
//        fragmentTransaction.add(R.id.home_browse_by_fragmant_container,
//                mTilesFragment, "MyCollectionActivity");
//        fragmentTransaction.addToBackStack("MyCollectionActivity");
//        if (Constants.IS_COMMITALLOWSTATE)
//            fragmentTransaction.commitAllowingStateLoss();
//        else
//            fragmentTransaction.commit();
//        findViewById(R.id.home_browse_by_fragmant_container).setVisibility(View.VISIBLE);
//    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
    }

    private class CheckCachedTracksAvailablility extends Thread {
        private Context context;

        public CheckCachedTracksAvailablility(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            super.run();
            try {
                Thread.sleep(5000);

                HungamaApplication.reset();
                Logger.s("CM ---------- 2");
                Logger.s(" :::::::::::::checkTracksAvailability:::::::::::::::  started");
                DBOHandler.checkTracksAvailability(context);
                Logger.s(" :::::::::::::checkVideoTracksAvailability:::::::::::::::  started");
                DBOHandler.checkVideoTracksAvailability(context);

                isServiceRunning();
                CacheManager.loadNotCachedTrack(context);

                Intent TrackCached = new Intent(
                        CacheManager.ACTION_CACHE_STATE_UPDATED);
                HungamaApplication.getContext().sendBroadcast(TrackCached);
//				}
                Logger.s("CM ---------- 3");
            } catch (Exception e) {
                Logger.printStackTrace(e);
            } catch (Error e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDetachedFromWindow() {
        Logger.s("onDetachedFromWindow HomeScreen");
        // HungamaApplication.activityPaused();
        super.onDetachedFromWindow();
    }

    @Override
    public void onAttachedToWindow() {
        Logger.s("onAttachedToWindow HomeScreen");
        // HungamaApplication.activityResumed();
        super.onAttachedToWindow();
    }

    @Override
    protected void onPause() {
        Logger.s("onPause HomeScreen");
        HungamaApplication.activityPaused();
        /*
         * No matter what, remove any existing dialog from the activity. Will be
		 * resumed only if the activity is visible and content is still being
		 * loaded to it.
		 */
        hideLoadingDialog();
        RelativeLayout rlDialogLayout = (RelativeLayout) findViewById(R.id.rl_custom_dialog);
        if (rlDialogLayout != null && rlDialogLayout.getVisibility() == View.VISIBLE)
            rlDialogLayout.setVisibility(View.GONE);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Logger.s("onStop HomeScreen");
        // HungamaApplication.activityStoped();
        mActivityStopped = true;
        PlayerServiceBindingManager.unbindFromService(mServiceToken);
        try {
            MySpinServerSDK.sharedInstance().unregisterConnectionStateListener(this);
        } catch (MySpinException e) {
            e.printStackTrace();
        }

        super.onStop();
//		Apsalar.endSession();
        Analytics.onEndSession(this);
    }

    protected void onDestroy() {
        super.onDestroy();
        Utils.unbindDrawables((RelativeLayout) findViewById(R.id.homeScreenMain));
        wasInBackground = false;
        Instance = null;
        try {
            if (cacheStateReceiver != null)
                unregisterReceiver(cacheStateReceiver);
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        cacheStateReceiver = null;

        try {
            if (offlineModeReceiver != null)
                unregisterReceiver(offlineModeReceiver);
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        offlineModeReceiver = null;

        try {
            if (languageChangeReceiver != null)
                unregisterReceiver(languageChangeReceiver);
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        languageChangeReceiver = null;

        try {
            if (closeAppReceiver != null)
                unregisterReceiver(closeAppReceiver);
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        closeAppReceiver = null;


        try {
            if (AireplaneModeReceiver != null)
                unregisterReceiver(AireplaneModeReceiver);
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }

        try {
            if (mTrackReloadReceiver != null)
                unregisterReceiver(mTrackReloadReceiver);
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        mTrackReloadReceiver = null;

        if (ApsalarEvent.ENABLED) {
            Apsalar.unregisterApsalarReceiver(getApplicationContext());
            Apsalar.endSession();
        }

        if (!Utils.isCarMode() && !getIntent().getBooleanExtra("from_home", false)) {
            int pid = android.os.Process.myPid();
            android.os.Process.killProcess(pid);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        Utils.clearCache();
        int count = getSupportFragmentManager()
                .getBackStackEntryCount();
        Logger.e("count:*", "count**" + count);
        super.onBackPressed();
    }

    /**
     * Show app update dialog if new version available. It will be customized
     * based on api response.
     *
     * @param newVersionCheckResponse response from NEW_VERSION_CHECK api.
     */
    private void showNewUpdateDialog(
            final NewVersionCheckResponse newVersionCheckResponse) {
        try {
            CustomAlertDialog alertDialogBuilder = new CustomAlertDialog(
                    CarModeHomeActivity.this);

            // set title
            alertDialogBuilder.setTitle(Utils.getMultilanguageText(mContext,
                    getResources().getString(R.string.new_version_title)));

            // set dialog message
            alertDialogBuilder.setMessage(Utils.getMultilanguageText(mContext,
                    getResources().getString(R.string.new_version_message)));
            if (newVersionCheckResponse.isMandatory()) {
                alertDialogBuilder.setCancelable(false);
            } else {
                alertDialogBuilder.setCancelable(true);
            }

            alertDialogBuilder.setPositiveButton(Utils.getMultilanguageText(
                            mContext,
                            getResources().getString(R.string.upgrade_now_button)),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            if (!newVersionCheckResponse.getUrl().startsWith(
                                    "http")) {
                                Intent browserIntent = new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(getResources().getString(
                                                R.string.google_play_url)));
                                startActivity(browserIntent);
                            } else {
                                Intent browserIntent = new Intent(
                                        Intent.ACTION_VIEW, Uri
                                        .parse(newVersionCheckResponse
                                                .getUrl()));
                                startActivity(browserIntent);
                            }

                        }
                    });
            if (!newVersionCheckResponse.isMandatory()) {
                alertDialogBuilder.setNegativeButton(Utils
                                .getMultilanguageText(mContext, getResources()
                                        .getString(R.string.remind_me_later_button)),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });
            }
            alertDialogBuilder.show();
        } catch (Exception e) {
        } catch (Error e) {
        }
    }

    // ======================================================
    // Communication Operation listeners.
    // ======================================================

    @Override
    public void onStart(int operationId) {
        /*if (mDeafultOpenedTab == HomeTabBar.TAB_ID_DISCOVER
                || mDeafultOpenedTab == HomeTabBar.TAB_ID_RADIO) {
            return;
        }*/
        if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
            mOperationsList.add(operationId);
        } else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CATEGORIES) {
            mOperationsList.add(operationId);
        } else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS
                || operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATION_DETAIL) {
            showLoadingDialog(R.string.application_dialog_loading_content);
            mOperationsList.add(operationId);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        try {

            Logger.s("1 HomeTime:   onSuccess" + operationId);

            // mIsLoading = false;
            if (operationId == OperationDefinition.Hungama.OperationId.VERSION_CHECK) {
                // showRegIdDialog(); // for testing
                VersionCheckResponse versionCheckResponse = (VersionCheckResponse) responseObjects
                        .get(VersionCheckOperation.RESPONSE_KEY_VERSION_CHECK);
                if (versionCheckResponse != null) {
                    mApplicationConfigurations.setisVersionChecked(true);
                    if (!versionCheckResponse.getVersion().equalsIgnoreCase(
                            mDataManager.getServerConfigurations()
                                    .getAppVersion())
                            && !getIntent().getBooleanExtra(NOTIFICATION_MAIL,
                            false)) {
                        //showUpdateDialog();
                    }
                }

            } else if (operationId == OperationDefinition.Hungama.OperationId.NEW_VERSION_CHECK) {
                NewVersionCheckResponse newVersionCheckResponse = (NewVersionCheckResponse) responseObjects
                        .get(NewVersionCheckOperation.RESPONSE_KEY_VERSION_CHECK);
                if (newVersionCheckResponse != null) {
                    if (!newVersionCheckResponse.isMandatory()) {
                        mApplicationConfigurations.setisVersionChecked(true);
                    }
                    if (!getIntent().getBooleanExtra(NOTIFICATION_MAIL, false))
                        showNewUpdateDialog(newVersionCheckResponse);
                }

            } else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
                try {
                    try {
                        findViewById(R.id.progressbar).setVisibility(View.GONE);
                    } catch (Exception e) {
                    }
                    // findViewById(R.id.progressbar).setVisibility(View.GONE);setAlias
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
                        /*if (playerOptions == PlayerOption.OPTION_PLAY_NOW) {
                            mPlayerBar.playNow(tracks, null, null);

                        } else if (playerOptions == PlayerOption.OPTION_PLAY_NOW_AND_OPEN) {
                            mPlayerBar.playNow(tracks, null, null);
                        } else if (playerOptions == PlayerOption.OPTION_PLAY_NEXT) {
                            mPlayerBar.playNext(tracks);

                        } else if (playerOptions == PlayerOption.OPTION_ADD_TO_QUEUE) {
                            mPlayerBar.addToQueue(tracks, null, null);
                        } else */
                        if (playerOptions == PlayerOption.OPTION_SAVE_OFFLINE) {
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

                if (!mActivityStopped) {
                    hideLoadingDialog();
                }

            } else if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK) {
                if (Logger.enableHungamaPay) {
                    SubscriptionStatusResponse subscriptionsubscriptionStatusResponse = (SubscriptionStatusResponse) responseObjects
                            .get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
                    if (subscriptionsubscriptionStatusResponse != null) {
                        if (subscriptionsubscriptionStatusResponse.getSubscription() != null &&
                                subscriptionsubscriptionStatusResponse.getSubscription().getSubscriptionStatus() == 1) {
                            if (!mHasSubscriptionPlan && mApplicationConfigurations.isUserHasSubscriptionPlan()) {
                                mHasSubscriptionPlan = true;
                            }

//							if (subscriptionsubscriptionStatusResponse.getPlan().isTrial()) {
//								Utils.makeText(
//										this,
//										subscriptionsubscriptionStatusResponse.getPlan()
//												.getTrailExpiryDaysLeft()
//												+ Utils.getMultilanguageText(
//												mContext, " days left."),
//										Toast.LENGTH_SHORT).show();
//							}
                        }
                    }
                } else {
                    SubscriptionCheckResponse subscriptionCheckResponse = (SubscriptionCheckResponse) responseObjects
                            .get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
                    if (subscriptionCheckResponse != null) {
                        if (subscriptionCheckResponse.getCode().equalsIgnoreCase(
                                UpgradeActivity.PASSWORD_SMS_SENT)) {
                            if (!mHasSubscriptionPlan && mApplicationConfigurations.isUserHasSubscriptionPlan()) {
                                mHasSubscriptionPlan = true;
                            }

                            if (subscriptionCheckResponse.getPlan().isTrial()) {
                                Utils.makeText(
                                        this,
                                        subscriptionCheckResponse.getPlan()
                                                .getTrailExpiryDaysLeft()
                                                + Utils.getMultilanguageText(
                                                mContext, " days left."),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            } else if (operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS) {
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }

        mOperationsList.remove(Integer.valueOf(operationId));
        if (mOperationsList.isEmpty()) {
            if (!mActivityStopped) {
                hideLoadingDialog();
            }
        }
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType,
                          String errorMessage) {
        // mIsLoading = false;
        try {
            findViewById(R.id.progressbar).setVisibility(View.GONE);
        } catch (Exception e) {
        }
        Logger.e(TAG, "Failed to load media content " + errorMessage);

        if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS
                || operationId == OperationDefinition.Hungama.OperationId.RADIO_TOP_ARTISTS_SONGS
                || operationId == OperationDefinition.Hungama.OperationId.RADIO_LIVE_STATION_DETAIL) {
            Logger.i(TAG, "Failed loading media details");
            /*internetConnectivityPopup(new OnRetryClickListener() {
                @Override
                public void onRetryButtonClicked() {
                    mDataManager.getMediaDetails(DataManager.mediaItem,
                            DataManager.playerOption, DataManager.listener);
                }
            });*/
        }

        mOperationsList.remove(Integer.valueOf(operationId));

        if (!mActivityStopped) {
            hideLoadingDialog();
        }
    }

    /**
     * It will receive when offline state changes event for song, album,
     * playlist, video caching.
     *
     * @author hungama2
     */
    private class CacheStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            Logger.s("========================= cachestateupdatereceived ========"
                    + arg1.getAction());
            if (arg1.getAction()
                    .equals(CacheManager.ACTION_CACHE_STATE_UPDATED)
                    || arg1.getAction()
                    .equals(CacheManager.ACTION_TRACK_CACHED)) {
                try {
                    Log.d("Khoa", "CacheStateReceiver-------fragMainCar-----" + fragMainCar.getClass().getSimpleName());
                    Log.d("Khoa", "CacheStateReceiver-------fragPlayer----" + fragPlayer.getClass().getSimpleName());
                    if (Utils.isCarMode()) {
                        if (fragMainCar instanceof MusicFragment) {
                            ((MusicFragment) fragMainCar).handleCacheState();
                        } else if (fragMainCar instanceof FavoriteFragment) {
                            ((FavoriteFragment) fragMainCar).handleCacheState();
                        } else if (fragMainCar instanceof MusicDetailFragment) {
                            ((MusicDetailFragment) fragMainCar).handleCacheState();
                        } else if (fragMainCar instanceof PlaylistDetailFragment) {
                            ((PlaylistDetailFragment) fragMainCar).handleCacheState();
                        } else if (fragMainCar instanceof SearchResultFragment) {
                            ((SearchResultFragment) fragMainCar).handleCacheState();
                        } else if (fragMainCar instanceof PlaylistFragment) {
                            ((PlaylistFragment) fragMainCar).handleCacheState();
                        }

                        if (fragPlayer instanceof FullScreenPlayerFragment) {
                            //((FullScreenPlayerFragment) fragPlayer).refreshMusicPlayer();
                            ((FullScreenPlayerFragment) fragPlayer).updateOfflineState();
                        }
                        if (fragPlayerQueue instanceof PlayerQueueFragment) {
                            //((PlayerQueueFragment) fragPlayerQueue).refreshMusicPlayer();
                            ((PlayerQueueFragment) fragPlayerQueue).updateOfflineState();

                        }
                    }
                } catch (Exception e) {
                    Logger.printStackTrace(e);
                }

                boolean isPopupShown = false;
                try {
                    long id = mApplicationConfigurations.getSaveOfflineAutoSaveFreeUser();
                    if (arg1.getAction().equals(CacheManager.ACTION_TRACK_CACHED) && DBOHandler.getTrackCacheState(CarModeHomeActivity.this, "" + id) == CacheState.CACHED) {
                        Logger.s("songcatched HomeScreen");
                        mApplicationConfigurations.setIsSongCatched(true);
                        Logger.s("songcatched ifActivityVisible HomeScreen");
                        if (Utils.isCarMode()) {
                            OfflineGuide();
                            isPopupShown = true;
                        }
                    }
                } catch (Exception e) {
                    Logger.printStackTrace(e);
                }

                try {
                    if (!isPopupShown && !Utils.isCarMode() && arg1.getAction().equals(
                            CacheManager.ACTION_TRACK_CACHED)
                            && DBOHandler.getAllCachedTracks(mContext).size() == CacheManager
                            .getFreeUserCacheLimit(mContext)
                            && mApplicationConfigurations
                            .getFreeUserCacheCount() == CacheManager
                            .getFreeUserCacheLimit(mContext)) {

                        sendBroadcast(new Intent(
                                getString(R.string.inapp_prompt_action_apppromptofflinecaching3rdsong)));
                    }
                } catch (Exception e) {
                    Logger.printStackTrace(e);
                }
            } else if (arg1.getAction().equals(
                    CacheManager.ACTION_UPDATED_CACHE)) {
            } else if (arg1.getAction().equals(
                    CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED)
                    || arg1.getAction().equals(
                    CacheManager.ACTION_VIDEO_TRACK_CACHED)) {

            } else if (arg1.getAction().equals(
                    CacheManager.ACTION_VIDEO_UPDATED_CACHE)) {
            }
        }
    }

    private class OfflineModeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            boolean offlineMode = (ApplicationConfigurations
                    .getInstance(getApplicationContext())).getSaveOfflineMode();
            if (offlineMode && !Utils.isCarMode()) {
                try {
                    findViewById(R.id.progressbar).setVisibility(View.VISIBLE);
                } catch (Exception e) {
                }

                // new SwitchToOfflineMode().execute();
                Intent i = new Intent(CarModeHomeActivity.this, CarModeHomeActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("finish_all", true);
                i.putExtra("open_upgrade_popup",
                        arg1.getBooleanExtra("open_upgrade_popup", false));
                startActivity(i);
                hideLoadingDialog();
            }
        }
    }

    private class LanguageChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
//			mDataManager.getCacheManager().storeMusicLatestResponse("", null);
//			mDataManager.getCacheManager().storeMusicFeaturedResponse("", null);
//			mDataManager.getCacheManager().storeVideoLatestResponse("", null);
//			mDataManager.getCacheManager().storeLiveRadioResponse("", null);
//			mDataManager.getCacheManager().storeCelebRadioResponse("", null);

            mApplicationConfigurations.setMusicLatestTimeStamp(null);
            mApplicationConfigurations.setMusicPopularTimeStamp(null);
            mApplicationConfigurations.setVideoLatestTimeStamp(null);
            mApplicationConfigurations.setLiveRadioTimeStamp(null);
            mApplicationConfigurations.setOnDemandTimeStamp(null);

            startService(new Intent(CarModeHomeActivity.this,
                    ReloadTracksDataService.class));

            Intent i = new Intent(CarModeHomeActivity.this, CarModeHomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.putExtra("finish_restart", true);
            startActivity(i);
            hideLoadingDialog();
        }
    }

    private class CloseAppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            //mPlayerBar.explicitStop();

            // reset the inner boolean for showing home
            // tile hints.
            mApplicationConfigurations.setIsHomeHintShownInThisSession(false);
            mApplicationConfigurations
                    .setIsSearchFilterShownInThisSession(false);
            mApplicationConfigurations
                    .setIsPlayerQueueHintShownInThisSession(false);
            // if this button is clicked, close
            // current activity
            CarModeHomeActivity.super.onBackPressed();
            CarModeHomeActivity.this.finish();

            hideLoadingDialog();
        }
    }

    /**
     * Checks weather any offline caching is going on or not. If no caching is
     * going on, it will check for stopped track in previous session and restart
     * it.
     */
    private void isServiceRunning() {
        final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        boolean isServiceFound = false;
        for (int i = 0; i < services.size(); i++) {


            if ("com.hungama.myplay.activity".equals(services.get(i).service
                    .getPackageName())) {

                if ("com.hungama.myplay.activity.data.audiocaching.DownloaderService"
                        .equals(services.get(i).service.getClassName())) {
                    // Logger.d(TAG,
                    // " ::::: Service Nr. -- getClassName stimmt �berein !!!");
                    isServiceFound = true;
                    break;
                }
            }
        }
        try {
            if (!isServiceFound) {
                CacheManager.resumeCachingStoppedTrack(this);
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }


    private MyProgressDialog mProgressDialog;

    public void showLoadingDialog(int message) {
        try {
            if (!isFinishing()) {
                if (mProgressDialog == null) {
                    mProgressDialog = new MyProgressDialog(this);
                    mProgressDialog.setCancelable(true);
                    mProgressDialog.setCanceledOnTouchOutside(false);
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    public void hideLoadingDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void registerReceivers() {
        /*IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PREFERENCE_CHANGE);
        registerReceiver(preference_update, filter);*/

        /*IntentFilter filter_notify = new IntentFilter();
        filter_notify.addAction(ACTION_NOTIFY_ADAPTER);
        registerReceiver(reciver_notify, filter_notify);

        IntentFilter filter_listener_update = new IntentFilter();
        filter_listener_update.addAction(ACTION_LISTENER);
        registerReceiver(listener_update, filter_listener_update);*/

        if (cacheStateReceiver == null && Logger.isSaveOffline) {
            cacheStateReceiver = new CacheStateReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(CacheManager.ACTION_CACHE_STATE_UPDATED);
            filter.addAction(CacheManager.ACTION_TRACK_CACHED);
            filter.addAction(CacheManager.ACTION_UPDATED_CACHE);
            filter.addAction(CacheManager.ACTION_VIDEO_CACHE_STATE_UPDATED);
            filter.addAction(CacheManager.ACTION_VIDEO_TRACK_CACHED);
            filter.addAction(CacheManager.ACTION_VIDEO_UPDATED_CACHE);
            registerReceiver(cacheStateReceiver, filter);
        }

        if (languageChangeReceiver == null) {
            languageChangeReceiver = new LanguageChangeReceiver();
            IntentFilter filter_lang = new IntentFilter();
            filter_lang.addAction(MainActivity.ACTION_LANGUAGE_CHANGED);
            registerReceiver(languageChangeReceiver, filter_lang);
        }

        if (closeAppReceiver == null) {
            closeAppReceiver = new CloseAppReceiver();
            IntentFilter filter_close = new IntentFilter();
            filter_close.addAction(ACTION_CLOSE_APP);
            registerReceiver(closeAppReceiver, filter_close);
        }

        IntentFilter airplaneMode = new IntentFilter();
        airplaneMode.addAction("android.intent.action.AIRPLANE_MODE");
        registerReceiver(AireplaneModeReceiver, airplaneMode);
    }

    public static boolean needToShowAirplaneDialog = false;

    private BroadcastReceiver AireplaneModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Utils.isDeviceAirplaneModeActive(context)) {
                if (HungamaApplication.isActivityVisible()) {
                    needToShowAirplaneDialog = false;
                    Intent i = new Intent(context, OfflineAlertActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                } else {
                    needToShowAirplaneDialog = true;
                }
            }
        }
    };


    /**
     * Used for app initialization purpose. It will reload previous session
     * playlist in player and handles push notification depplinking. Call it
     * after media content loaded, so it will do remaining operation after
     * content load.
     */

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Logger.e("onTrimMemory", "onTrimMemory" + level);
        if (level != Activity.TRIM_MEMORY_RUNNING_MODERATE
                && level != Activity.TRIM_MEMORY_MODERATE)
            Utils.clearCache(true);
    }

    private void updateTrackLanguage() {

        if (getIntent().getBooleanExtra("skip_ad", false))
            startService(new Intent(this, ReloadTracksDataService.class));
    }

    private void call_gcmtoken(String gcmtoken) {

        try {

            if (!mApplicationConfigurations.getGcmToken()) {

                final String regId = gcmtoken;

                if (regId == null || TextUtils.isEmpty(regId)) {


                } else {
                    //ApplicationConfigurations appConfig = new ApplicationConfigurations(this);
                    mDataManager = DataManager.getInstance(mContext);
                    String sessionId = mApplicationConfigurations.getSessionID();
                    if (sessionId != null
                            && (sessionId.length() != 0
                            && !sessionId.equalsIgnoreCase("null") && !sessionId
                            .equalsIgnoreCase("none"))) {
                        mDataManager.getTokenUpdate(regId,
                                new CommunicationOperationListener() {

                                    @Override
                                    public void onSuccess(int operationId,
                                                          Map<String, Object> responseObjects) {

                                        mApplicationConfigurations.setGcmToken(true);
                                    }

                                    @Override
                                    public void onStart(int operationId) {

                                    }

                                    @Override
                                    public void onFailure(int operationId,
                                                          ErrorType errorType,
                                                          String errorMessage) {
                                    }
                                });
                        Logger.i("gcmtoken", "1");
                    }
                }
            }
        } catch (Exception e) {
            Logger.i("gcmtoken", "2");
            e.printStackTrace();
        }
    }


    /*================================================================================== CARMODE ==================================================================================*/
    private RelativeLayout rlCarmode;
    private RelativeLayout rlPlayer;
    private RelativeLayout rlPlayerQueue;
    private RelativeLayout rlCustomDialog;
    private RelativeLayout rlLoadingDialog;
    private RelativeLayout rlNoNetworkDialog;
    private int curViewId = 0;
    private boolean isPlayerShown = false;
    private static boolean bNeedToLoadMusicPlayer = false;
    private static boolean enableInternetCheck = true;
    private Fragment fragMainCar;
    private Fragment fragPlayer;
    private Fragment fragPlayerQueue;
    private Handler handlerInternetCheck;
    private Runnable runnableInternetCheck = new Runnable() {
        @Override
        public void run() {

            if (Utils.isConnected() && !mApplicationConfigurations.getSaveOfflineMode()) {
                rlNoNetworkDialog.setVisibility(View.GONE);
            } else if (enableInternetCheck && fragMainCar != null && fragPlayerQueue != null && fragPlayer != null) {
                if ((fragPlayerQueue instanceof PlayerQueueFragment) && ((PlayerQueueFragment) fragPlayerQueue).isInForeground()) {
                    showNoNetworkDialog();
                } else if ((fragPlayer instanceof FullScreenPlayerFragment) && ((FullScreenPlayerFragment) fragPlayer).isInForeground()) {
                    showNoNetworkDialog();
                } else if ((fragPlayer instanceof DiscoveryPlayerFragment) && ((DiscoveryPlayerFragment) fragPlayer).isInForeground()) {
                    showNoNetworkDialog();
                } else if ((fragPlayer instanceof RadioPlayerFragment) && ((RadioPlayerFragment) fragPlayer).isInForeground()) {
                    showNoNetworkDialog();
                } else if (!(fragMainCar instanceof MainMenuFragment)) {
                    showNoNetworkDialog();
                }

                if (PlayerService.service.isLoading() || PlayerService.service.isPlaying()) {
                    PlayerService.service.stop();


                    if (PlayerService.service.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                        PlayerService.service.stopLiveRadioUpdater();
                    }
                }
            }


            // Re-run check internet
            handlerInternetCheck.postDelayed(runnableInternetCheck, 1000);
        }
    };

    public static void setFlagNeedToLoadMusicPlayer(boolean bool) {
        CarModeHomeActivity.bNeedToLoadMusicPlayer = bool;
    }

    public static void setEnableInternetCheck(boolean bool) {
        CarModeHomeActivity.enableInternetCheck = bool;
    }

    private CountDownTimer timerNoNetworkDialog;

    public void showNoNetworkDialog() {
        rlCustomDialog.setVisibility(View.GONE);
        rlNoNetworkDialog.setVisibility(View.VISIBLE);

        final TextView tvMsg = (TextView) rlNoNetworkDialog.findViewById(R.id.tv_dialog_title);
        if (!Utils.isConnected()) {
            tvMsg.setText(R.string.msg_no_network);
        } else if (mApplicationConfigurations.getSaveOfflineMode()) {
            tvMsg.setText(R.string.msg_in_offline_mode);
        }

        final Button btnOk = (Button) rlNoNetworkDialog.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timerNoNetworkDialog != null) {
                    timerNoNetworkDialog.cancel();
                }
                emptyBackStackAndReturnToHomeScreen();
            }
        });

        timerNoNetworkDialog = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
//                final StringBuilder strBtn = new StringBuilder();
//                strBtn.append(getString(android.R.string.ok));
//                strBtn.append("(");
//                strBtn.append(millisUntilFinished / 1000);
//                strBtn.append(")");
//                btnOk.setText(strBtn.toString());
            }

            @Override
            public void onFinish() {
                emptyBackStackAndReturnToHomeScreen();
            }
        };

        timerNoNetworkDialog.start();
    }

    private void emptyBackStackAndReturnToHomeScreen() {
        if (!(fragMainCar instanceof MainMenuFragment)) {
            final FragmentManager fm = getSupportFragmentManager();

            for (int index = 0; index < fm.getBackStackEntryCount(); index++) {
                FragmentManager.BackStackEntry backEntry = fm.getBackStackEntryAt(index);
                if (backEntry.getName().equals(MainMenuFragment.TAG)) {
                    final String nextEntryName = fm.getBackStackEntryAt(index + 1).getName();

                    try {
                        fm.popBackStack(nextEntryName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                }
            }
        }

        if (isPlayerShown) {
            hideMusicPlayer();
        }
        rlNoNetworkDialog.setVisibility(View.GONE);
    }

    public void showLoadDialog() {
        if (rlLoadingDialog != null) {
            rlLoadingDialog.setVisibility(View.VISIBLE);
        }
    }

    public void hideLoadDialog() {
        if (rlLoadingDialog != null) {
            rlLoadingDialog.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (Utils.isCarMode()) {
            int action = event.getAction();
            int keyCode = event.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    if ((action == KeyEvent.ACTION_DOWN)
                            && (PlayerService.service != null)) {
                        int vol = PlayerService.service.increaseVolume();

                        // Resume play if paused.
                        if (vol == 1 && PlayerService.service.getState() == PlayerService.State.PAUSED) {
                            PlayerService.service.play();

                            if (fragPlayer instanceof FullScreenPlayerFragment) {
                                ((FullScreenPlayerFragment) fragPlayer).updatePlayerState();
                            } else if (fragPlayer instanceof RadioPlayerFragment) {
                                ((RadioPlayerFragment) fragPlayer).updateRadioPlayerState();
                            } else if (fragPlayer instanceof DiscoveryPlayerFragment) {
                                ((DiscoveryPlayerFragment) fragPlayer).updateDiscoveryPlayerState();
                            }

                            if (fragMainCar instanceof MusicDetailFragment) {
                                ((MusicDetailFragment) fragMainCar).updateDetailPlayerUI();
                            }
                        }
                    }
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    if ((action == KeyEvent.ACTION_DOWN)
                            && (PlayerService.service != null)) {
                        int vol = PlayerService.service.descreaseVolume();

                        // Pause if volume reach 0.
                        if (vol == 0 && PlayerService.service.getState() == PlayerService.State.PLAYING) { // mute.
                            PlayerService.service.pause();

                            if (fragPlayer instanceof FullScreenPlayerFragment) {
                                ((FullScreenPlayerFragment) fragPlayer).updatePlayerState();
                            } else if (fragPlayer instanceof RadioPlayerFragment) {
                                ((RadioPlayerFragment) fragPlayer).updateRadioPlayerState();
                            } else if (fragPlayer instanceof DiscoveryPlayerFragment) {
                                ((DiscoveryPlayerFragment) fragPlayer).updateDiscoveryPlayerState();
                            }

                            if (fragMainCar instanceof MusicDetailFragment) {
                                ((MusicDetailFragment) fragMainCar).updateDetailPlayerUI();
                            }
                        }
                    }
                    return true;

                case KeyEvent.KEYCODE_HOME:
                    return true;

                default:
                    return super.dispatchKeyEvent(event);
            }
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public void onConnectionStateChanged(boolean b) {
        updateUI();
    }

    private void updateUI() {
        if (Utils.isCarMode()) {

            if (curViewId != R.layout.carmode_activity_home) {
                setContentView(R.layout.carmode_activity_home);
                curViewId = R.layout.carmode_activity_home;
            }

            if (rlCarmode == null) {
                rlCarmode = (RelativeLayout) findViewById(R.id.rl_carmode);
            }

            if (rlPlayer == null) {
                rlPlayer = (RelativeLayout) findViewById(R.id.rl_player);
            }

            if (rlPlayerQueue == null) {
                rlPlayerQueue = (RelativeLayout) findViewById(R.id.rl_player_queue);
            }

            if (rlCustomDialog == null) {
                rlCustomDialog = (RelativeLayout) findViewById(R.id.rl_custom_dialog);
            }

            if (rlLoadingDialog == null) {
                rlLoadingDialog = (RelativeLayout) findViewById(R.id.rl_loading_dialog);
            }

            if (rlNoNetworkDialog == null) {
                rlNoNetworkDialog = (RelativeLayout) findViewById(R.id.rl_no_network_dialog);
            }

            if (fragMainCar == null) { // Show Splash screen at first launch.
                fragMainCar = SplashScreenFragment.newInstance();
                nextFragmentTransaction(false, SplashScreenFragment.TAG);

                int delay = 3000;
                if (getIntent().getBooleanExtra("from_home", false)) {
                    delay = 500;
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadCarMode();
                        updatePlayerUI();

                        if (handlerInternetCheck == null) {
                            handlerInternetCheck = new Handler();
                            handlerInternetCheck.postDelayed(runnableInternetCheck, 1000);
                        }

                    }
                }, delay);
            } else {
                if (PlayerService.service != null && PlayerService.service.getState() == PlayerService.State.PAUSED) {
                    PlayerService.service.play();
                }
            }
        } else {
            HungamaApplication.getCacheManager().setOnCachingTrackLister(null);

            if (fragMainCar != null) {
                getSupportFragmentManager().beginTransaction().remove(fragMainCar).commit();
            }

            if (fragPlayer != null) {
                getSupportFragmentManager().beginTransaction().remove(fragPlayer).commit();
            }

            if (fragPlayerQueue != null) {
                getSupportFragmentManager().beginTransaction().remove(fragPlayerQueue).commit();
            }

            if (getIntent().getBooleanExtra("from_home", false)) {
                startActivity(new Intent(this, OnApplicationStartsActivity.class));
                finish();
            } else {
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
            }
        }
    }


    private void updatePlayerUI() {
        if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
            setupMusicPlayer();
            setupPlayer(FullScreenPlayerFragment.TAG);
        }

        if (fragPlayerQueue == null || !(fragPlayerQueue instanceof PlayerQueueFragment)) {
            setupMusicPlayerQueue();
        }
    }

    private void setupMusicPlayer() {
        fragPlayer = FullScreenPlayerFragment.newInstance(new FullScreenPlayerFragment.IMusicPlayer() {
            @Override
            public void hideMusicPlayer() {
                CarModeHomeActivity.this.hideMusicPlayer();
            }

            @Override
            public void openPlayerQueue() {
                if (fragPlayerQueue == null || !(fragPlayerQueue instanceof PlayerQueueFragment)) {
                    setupMusicPlayerQueue();
                }

//                if (mApplicationConfigurations.getSaveOfflineMode() // In offline mode
//                        || !Utils.isConnected()) {// Or in No Network connection.
//                    ((PlayerQueueFragment) fragPlayerQueue).prepareOfflineList();
//                } else {
//                    ((PlayerQueueFragment) fragPlayerQueue).restoreOnlineList();
//                }

                ((PlayerQueueFragment) fragPlayerQueue).refreshMusicPlayer();
                ((PlayerQueueFragment) fragPlayerQueue).setInForeground(true);
                rlPlayerQueue.setVisibility(View.VISIBLE);

                ((FullScreenPlayerFragment) fragPlayer).setInForeground(false);
            }
        });
    }

    private void loadCarMode() {
        fragMainCar = MainMenuFragment.newInstance(new MainMenuFragment.IMainMenuListener() {

            @Override
            public void vNavigateToMusic() {
                fragMainCar = new MusicFragment();
                nextFragmentTransaction(false, MusicFragment.TAG);

                ((MusicFragment) fragMainCar).setMusicListener(new MusicFragment.IMusicListener() {

                    @Override
                    public void onAddTrackToQueue(List<Track> listTracks) {
                        vHandleAddToQueue(listTracks);
                    }

                    @Override
                    public void onGoToDiscoveryPlayer(String mood, List<Track> listTracks) {
                        if (fragPlayer instanceof DiscoveryPlayerFragment) {
                            ((DiscoveryPlayerFragment) fragPlayer).setListTracks(listTracks);
                            ((DiscoveryPlayerFragment) fragPlayer).updateMood(mood);
                            ((DiscoveryPlayerFragment) fragPlayer).updateDiscoveryPlayerUI();
                        } else {
                            fragPlayer = DiscoveryPlayerFragment.newInstance(mood, listTracks, new DiscoveryPlayerFragment.IDiscoveryPlayer() {
                                @Override
                                public void hideDiscoveryPlayer() {
                                    hideMusicPlayer();
                                }
                            });

                            setupPlayer(DiscoveryPlayerFragment.TAG);
                        }

                        showMusicPlayer();
                    }

                    @Override
                    public void onGoToMusicPlayer() {
                        showMusicPlayer();
                    }

                    @Override
                    public void onGoToMusicDetail(MediaItem selectedMediaItem) {
                        loadMusicDetail(selectedMediaItem);
                    }

                    @Override
                    public void onPlayNow() {
                        if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
                            setupMusicPlayer();
                            setupPlayer(FullScreenPlayerFragment.TAG);
                        }

                        bNeedToLoadMusicPlayer = true;
                    }
                });
            }

            @Override
            public void vNavigateToRadio() {
                fragMainCar = RadioFragment.newInstance(new RadioFragment.IRadioFragmentListener() {

                    @Override
                    public void onGoToRadioPlayer(MediaItem radioItem, PlayMode radioMode) {
                        openRadioPlayer(radioItem, radioMode);
                        showMusicPlayer();
                    }

                    @Override
                    public void onGoToMusicPlayer() {
                        showMusicPlayer();
                    }
                });
                nextFragmentTransaction(false, RadioFragment.TAG);
            }

            @Override
            public void vOnUniversalPlayerClick() {
                showMusicPlayer();
            }

            @Override
            public void onCategorySelection() {
                fragMainCar = new CategorySelectionFragment();
                nextFragmentTransaction(false, CategorySelectionFragment.TAG);
            }

            @Override
            public void onShowNoNetwork() {
                showNoNetworkDialog();
            }

            @Override
            public void vNavigateToSearch() {
                fragMainCar = SearchFragment.newInstance(new SearchFragment.ISearchListener() {

                    @Override
                    public void onGoToSearchResult(String queryText) {
                        Bundle args = new Bundle();
                        args.putString(SearchResultFragment.QUERY_TEXT, queryText);

                        fragMainCar = SearchResultFragment.newInstance(new SearchResultFragment.ISearchResultListener() {

                            @Override
                            public void onGoToMusicPlayer() {
                                showMusicPlayer();
                            }

                            @Override
                            public void onAddTrackToQueue(List<Track> listTrack) {
                                vHandleAddToQueue(listTrack);
                            }

                            @Override
                            public void onGoToMusicDetail(MediaItem selectedMediaItem) {
                                loadMusicDetail(selectedMediaItem);
                            }

                            @Override
                            public void onPlayNow() {
                                if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
                                    setupMusicPlayer();
                                    setupPlayer(FullScreenPlayerFragment.TAG);
                                }

                                bNeedToLoadMusicPlayer = true;
                            }
                        });
                        fragMainCar.setArguments(args);
                        nextFragmentTransaction(false, SearchResultFragment.TAG);
                    }

                    @Override
                    public void onGoToMusicPlayer() {
                        showMusicPlayer();
                    }

                });
                nextFragmentTransaction(false, SearchFragment.TAG);
            }

            @Override
            public void vNavigateToFavorite() {
                fragMainCar = FavoriteFragment.newInstance(new FavoriteFragment.IFavoriteListener() {

                    @Override
                    public void onGoToMusicPlayer() {
                        showMusicPlayer();
                        bNeedToLoadMusicPlayer = true;
                    }

                    @Override
                    public void onGoToRadioPlayer(MediaItem radioItem, PlayMode radioMode) {
                        openRadioPlayer(radioItem, radioMode);
                        showMusicPlayer();
                    }

                    @Override
                    public void onAddTrackToQueue(List<Track> listTracks) {
                        vHandleAddToQueue(listTracks);
                    }

                    @Override
                    public void goToMusicDetail(MediaItem selectedMedia) {
                        loadMusicDetail(selectedMedia);
                    }

                    @Override
                    public void onPlayNow() {
                        if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
                            setupMusicPlayer();
                            setupPlayer(FullScreenPlayerFragment.TAG);
                        }

                        bNeedToLoadMusicPlayer = true;
                    }

                    @Override
                    public void onGoToMediaTypeSelection(int curMediaType, Fragment targetFragment) {

                        fragMainCar = FavoriteTypeSelectionFragment.newInstance(new FavoriteTypeSelectionFragment.IFavCategoryListener() {
                            @Override
                            public void onGoToMusicPlayer() {
                                showMusicPlayer();
                            }
                        }, curMediaType, targetFragment);

                        nextFragmentTransaction(false, FavoriteTypeSelectionFragment.TAG);
                    }

                });
                nextFragmentTransaction(false, FavoriteFragment.TAG);
            }

            @Override
            public void vNavigateToPlaylist() {
                fragMainCar = PlaylistFragment.newInstance(new PlaylistFragment.IPlaylistListener() {

                    @Override
                    public void onAddTrackToQueue(List<Track> listTracks) {
                        vHandleAddToQueue(listTracks);
                    }

                    @Override
                    public void gotoPlaylistDetail(Playlist playlist, List<Track> listTracksOfPlaylist) {
                        loadMusicDetailForPlaylist(playlist, listTracksOfPlaylist);
                    }

                    @Override
                    public void onGoToMusicPlayer() {
                        showMusicPlayer();
                    }

                    @Override
                    public void onPlayNow() {
                        if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
                            setupMusicPlayer();
                            setupPlayer(FullScreenPlayerFragment.TAG);
                        }

                        bNeedToLoadMusicPlayer = true;
                    }

                });

                nextFragmentTransaction(false, PlaylistFragment.TAG);
            }

            @Override
            public void vNavigateToOfflineMode() {
                // Change Offline queue.
                if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
                    setupMusicPlayer();
                    setupPlayer(FullScreenPlayerFragment.TAG);
                }

                fragMainCar = OfflineFragment.newInstance(new OfflineFragment.IOfflineListener() {

                    @Override
                    public void onAddTrackToQueue(List<Track> listTracks) {
                        vHandleAddToQueue(listTracks);
                    }

                    @Override
                    public void onGoToMusicPlayer() {
                        // Disable Internet check.
                        if (!mApplicationConfigurations.getSaveOfflineMode() || !Utils.isConnected()) {
                            setEnableInternetCheck(false);
                        }

                        bNeedToLoadMusicPlayer = true;
                        showMusicPlayer();
                    }

                    @Override
                    public void onPlayNow() {
//                        if(fragPlayer != null
//                                && (fragPlayer instanceof  FullScreenPlayerFragment)) {
//                            ((FullScreenPlayerFragment) fragPlayer).prepareOfflineList();
//                        }

                        bNeedToLoadMusicPlayer = true;
                    }

                    @Override
                    public void onGoOnline() {
                        bNeedToLoadMusicPlayer = true;
//                        ((FullScreenPlayerFragment) fragPlayer).setFromOffline(true);
//                        ((FullScreenPlayerFragment) fragPlayer).restoreOnlineList();
                    }

                });
                nextFragmentTransaction(false, OfflineFragment.TAG);
            }

        });

        nextFragmentTransaction(false, MainMenuFragment.TAG);
    }

    private void openRadioPlayer(MediaItem radioItem, PlayMode radioMode) {
        if (fragPlayer instanceof RadioPlayerFragment) {
            ((RadioPlayerFragment) fragPlayer).setRadioMode(radioMode);
            ((RadioPlayerFragment) fragPlayer).setRadioItem(radioItem);
            ((RadioPlayerFragment) fragPlayer).setupRadio();
        } else {
            fragPlayer = RadioPlayerFragment.newInstance(radioItem, radioMode, new RadioPlayerFragment.IRadioPlayer() {
                @Override
                public void hideRadioPlayer() {
                    hideMusicPlayer();
                }
            });

            setupPlayer(RadioPlayerFragment.TAG);
        }
    }

    public void vHandleClicks(View v) {
        PlayerQueueFragment playerQueueFragment = (PlayerQueueFragment) getSupportFragmentManager().findFragmentByTag(PlayerQueueFragment.TAG);
        if (playerQueueFragment != null && playerQueueFragment.isVisible() && playerQueueFragment.isInForeground()) {
            playerQueueFragment.vHandlePlayerQueueClick(v);
            return;
        }

        FullScreenPlayerFragment fullScreenPlayerFragment = (FullScreenPlayerFragment) getSupportFragmentManager().findFragmentByTag(FullScreenPlayerFragment.TAG);
        if (fullScreenPlayerFragment != null && fullScreenPlayerFragment.isVisible() && fullScreenPlayerFragment.isInForeground()) {
            fullScreenPlayerFragment.vHandlePlayerClick(v);
            return;
        }


        RadioPlayerFragment radioPlayerFragment = (RadioPlayerFragment) getSupportFragmentManager().findFragmentByTag(RadioPlayerFragment.TAG);
        if (radioPlayerFragment != null && radioPlayerFragment.isVisible() && radioPlayerFragment.isInForeground()) {
            radioPlayerFragment.vHandleRadioPlayer(v);
            return;
        }

        DiscoveryPlayerFragment discoveryPlayerFragment = (DiscoveryPlayerFragment) getSupportFragmentManager().findFragmentByTag(DiscoveryPlayerFragment.TAG);
        if (discoveryPlayerFragment != null && discoveryPlayerFragment.isVisible() && discoveryPlayerFragment.isInForeground()) {
            discoveryPlayerFragment.vHandleDiscoveryPlayer(v);
            return;
        }

        MainMenuFragment menuFragment = (MainMenuFragment) getSupportFragmentManager().findFragmentByTag(MainMenuFragment.TAG);
        if (menuFragment != null && menuFragment.isVisible()) {
            menuFragment.vHandleMainMenuClicks(v);
            return;
        }

        MusicFragment musicFragment = (MusicFragment) getSupportFragmentManager().findFragmentByTag(MusicFragment.TAG);
        if (musicFragment != null && musicFragment.isVisible()) {
            musicFragment.vHandleMusicClicks(v);
            return;
        }

        MusicDetailFragment musicDetailFragment = (MusicDetailFragment) getSupportFragmentManager().findFragmentByTag(MusicDetailFragment.TAG);
        if (musicDetailFragment != null && musicDetailFragment.isVisible()) {
            musicDetailFragment.vHandleMusicDetailClicks(v);
            return;
        }

        RadioFragment radioFragment = (RadioFragment) getSupportFragmentManager().findFragmentByTag(RadioFragment.TAG);
        if (radioFragment != null && radioFragment.isVisible()) {
            radioFragment.vHandleRadioClicks(v);
            return;
        }


        CategorySelectionFragment cateFragment = (CategorySelectionFragment) getSupportFragmentManager().findFragmentByTag(CategorySelectionFragment.TAG);
        if (cateFragment != null && cateFragment.isVisible()) {
            cateFragment.vHandleCategoryClick(v);
            return;
        }

        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentByTag(SearchFragment.TAG);
        if (searchFragment != null && searchFragment.isVisible()) {
            searchFragment.vHandleSearchClicks(v);
            return;
        }

        SearchResultFragment searchResultFragment = (SearchResultFragment) getSupportFragmentManager().findFragmentByTag(SearchResultFragment.TAG);
        if (searchResultFragment != null && searchResultFragment.isVisible()) {
            searchResultFragment.vHandleSearchResultClicks(v);
            return;
        }

        FavoriteFragment favoriteFragment = (FavoriteFragment) getSupportFragmentManager().findFragmentByTag(FavoriteFragment.TAG);
        if (favoriteFragment != null && favoriteFragment.isVisible()) {
            favoriteFragment.vHandleFavoriteClicks(v);
            return;
        }

        FavoriteTypeSelectionFragment favoriteTypeFragment = (FavoriteTypeSelectionFragment) getSupportFragmentManager().findFragmentByTag(FavoriteTypeSelectionFragment.TAG);
        if (favoriteTypeFragment != null && favoriteTypeFragment.isVisible()) {
            favoriteTypeFragment.vHandleFavCategoryClicks(v);
            return;
        }

        PlaylistFragment playlistFragment = (PlaylistFragment) getSupportFragmentManager().findFragmentByTag(PlaylistFragment.TAG);
        if (playlistFragment != null && playlistFragment.isVisible()) {
            playlistFragment.vHandlePlaylistClicks(v);
            return;
        }

        PlaylistDetailFragment playlistDetailFragment = (PlaylistDetailFragment) getSupportFragmentManager().findFragmentByTag(PlaylistDetailFragment.TAG);
        if (playlistDetailFragment != null && playlistDetailFragment.isVisible()) {
            playlistDetailFragment.vHandlePlaylistDetailClicks(v);
            return;
        }

        OfflineFragment offlineFragment = (OfflineFragment) getSupportFragmentManager().findFragmentByTag(OfflineFragment.TAG);
        if (offlineFragment != null && offlineFragment.isVisible()) {
            offlineFragment.vHandleOfflineClicks(v);
            return;
        }
    }

    private void nextFragmentTransaction(boolean isAnim, String name) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//			getSupportFragmentManager().addOnBackStackChangedListener(this);

//            if (isAnim) {
//                ft.setCustomAnimations(R.anim.slide_in_left,
//                        R.anim.slide_out_left,
//                        R.anim.slide_in_right,
//                        R.anim.slide_out_right);
//            }

            ft.replace(R.id.carmode_container, fragMainCar, name);
            ft.addToBackStack(name);

            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void vHandleAddToQueue(List<Track> listTracks) {
        if (PlayerService.service != null && !Utils.isListEmpty(listTracks)) {
            // Marked as NeedToLoadMusicPlayer flag.
            if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
                setupMusicPlayer();
                setupPlayer(FullScreenPlayerFragment.TAG);
            }
            bNeedToLoadMusicPlayer = true;

//            final List<Track> queue = PlayerService.service.getPlayingQueue();
            final List<Track> queue = mDataManager.getStoredPlayingQueue(mDataManager.getApplicationConfigurations()).getCopy();
            if (Utils.isListEmpty(queue) || PlayerService.service.getPlayMode() != PlayMode.MUSIC) {
                //PlayerService.service.addToQueue(listTracks);
                PlayerService.service.playNow(listTracks);
                if (listTracks.size() > 1) {
                    GlobalFunction.showMessageDialog(CarModeHomeActivity.this, CustomDialogLayout.DialogType.MESSAGE, getResources().getString(R.string.main_player_bar_message_songs_added_to_queue), null);
                } else {
                    GlobalFunction.showMessageDialog(CarModeHomeActivity.this, CustomDialogLayout.DialogType.MESSAGE, getResources().getString(R.string.main_player_bar_message_song_added_to_queue), null);
                }

            } else {
                final ArrayList<Track> tracksNotInQueue = new ArrayList<Track>();
                for (Track tempTrack : listTracks) {
                    if (!queue.contains(tempTrack)) {
                        tracksNotInQueue.add(tempTrack);
                    }
                }

                if (tracksNotInQueue.size() > 0) {
                    PlayerService.service.addToQueue(tracksNotInQueue);
                    GlobalFunction.showMessageDialog(CarModeHomeActivity.this, CustomDialogLayout.DialogType.MESSAGE, getResources().getString(R.string.main_player_bar_message_songs_added_to_queue), null);
                } else {
                    GlobalFunction.showMessageDialog(CarModeHomeActivity.this, CustomDialogLayout.DialogType.MESSAGE, getResources().getString(R.string.main_player_bar_message_song_already_in_queue), null);
                }
            }
        }
    }


    private void showMusicPlayer() {
        if (PlayerService.service != null) {
            if (fragPlayer instanceof FullScreenPlayerFragment) {
                final FullScreenPlayerFragment fragment = (FullScreenPlayerFragment) fragPlayer;
                if (PlayerService.service.getPlayMode() != PlayMode.MUSIC) {
                    GlobalFunction.showMessageDialog(CarModeHomeActivity.this, CustomDialogLayout.DialogType.MESSAGE, "Music player is not available. Start playing song to activate Music Player", null);
                } else if (PlayerService.service.isQueueEmpty()) {
                    GlobalFunction.showMessageDialog(CarModeHomeActivity.this, CustomDialogLayout.DialogType.MESSAGE, getString(R.string.msg_player_queue_empty), null);
                } else {
                    isPlayerShown = true;
                    if (bNeedToLoadMusicPlayer) {
                        fragment.refreshMusicPlayer();
                        bNeedToLoadMusicPlayer = false;
                    }
                    fragment.setInForeground(isPlayerShown);
                    rlPlayer.setVisibility(View.VISIBLE);
                }

            } else if (fragPlayer instanceof RadioPlayerFragment) {
                isPlayerShown = true;
                final RadioPlayerFragment fragment = ((RadioPlayerFragment) fragPlayer);
                fragment.setInForeground(isPlayerShown);
                rlPlayer.setVisibility(View.VISIBLE);
            } else if (fragPlayer instanceof DiscoveryPlayerFragment) {
                isPlayerShown = true;
                final DiscoveryPlayerFragment fragment = ((DiscoveryPlayerFragment) fragPlayer);
                fragment.setInForeground(isPlayerShown);
                rlPlayer.setVisibility(View.VISIBLE);
            }

        }
    }

    private void hideMusicPlayer() {
        isPlayerShown = false;

        if (fragPlayer instanceof FullScreenPlayerFragment) {
            ((FullScreenPlayerFragment) fragPlayer).setInForeground(false);
        } else if (fragPlayer instanceof RadioPlayerFragment) {
            ((RadioPlayerFragment) fragPlayer).setInForeground(false);
        } else if (fragPlayer instanceof DiscoveryPlayerFragment) {
            ((DiscoveryPlayerFragment) fragPlayer).setInForeground(false);
        }
        rlPlayer.setVisibility(View.GONE);

        // Hide PlayerQueue.
        ((PlayerQueueFragment) fragPlayerQueue).setInForeground(false);
        rlPlayerQueue.setVisibility(View.GONE);

        if (fragMainCar instanceof MusicDetailFragment) {
            ((MusicDetailFragment) fragMainCar).updateDetailPlayerUI();
        } else if (fragMainCar instanceof PlaylistDetailFragment) {
            ((PlaylistDetailFragment) fragMainCar).updateDetailPlayerUI();
        } else if (fragMainCar instanceof FavoriteFragment) {
            ((FavoriteFragment) fragMainCar).updateFavoriteUI();
        }
    }

    private void setupPlayer(String name) {
        try {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.player_container, fragPlayer, name);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMusicPlayerQueue() {
        try {
            fragPlayerQueue = PlayerQueueFragment.newInstance(new PlayerQueueFragment.IPlayerQueue() {
                @Override
                public void backToMusicPlayer() {
                    if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
                        setupMusicPlayer();
                        setupPlayer(FullScreenPlayerFragment.TAG);
                    }

                    ((FullScreenPlayerFragment) fragPlayer).refreshMusicPlayer();
                    ((FullScreenPlayerFragment) fragPlayer).setInForeground(true);

                    ((PlayerQueueFragment) fragPlayerQueue).setInForeground(false);
                    rlPlayerQueue.setVisibility(View.GONE);

                }

                @Override
                public void closeMusicPlayer() {
//                    ((PlayerQueueFragment) fragPlayerQueue).setInForeground(false);
//                    rlPlayerQueue.setVisibility(View.GONE);
                    hideMusicPlayer();
                }
            });

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.player_queue_container, fragPlayerQueue, PlayerQueueFragment.TAG);
            ft.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMusicDetail(MediaItem selectedMediaItem) {
        Bundle fragArgs = new Bundle();
        fragArgs.putSerializable(MusicDetailFragment.MEDIA_ITEM, (Serializable) selectedMediaItem);

        fragMainCar = MusicDetailFragment.newInstance(new MusicDetailFragment.IMusicDetailListener() {

            @Override
            public void onGoToMusicPlayer() {
                bNeedToLoadMusicPlayer = true;
                showMusicPlayer();
            }

            @Override
            public void onAddTrackToQueue(List<Track> listTracks) {
                vHandleAddToQueue(listTracks);
            }

            @Override
            public void onPlayTrackFromDetail() {
                if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
                    setupMusicPlayer();
                    setupPlayer(FullScreenPlayerFragment.TAG);
                }

                bNeedToLoadMusicPlayer = true;
            }
        });

        fragMainCar.setArguments(fragArgs);
        nextFragmentTransaction(false, MusicDetailFragment.TAG);
    }

    private void loadMusicDetailForPlaylist(Playlist playlist, List<Track> listTracks) {
        Bundle fragArgs = new Bundle();
        fragArgs.putSerializable(PlaylistDetailFragment.LIST_MEDIA_ITEMS, (Serializable) listTracks);
        fragArgs.putSerializable(PlaylistDetailFragment.PLAYLIST, playlist);

        fragMainCar = PlaylistDetailFragment.newInstance(new PlaylistDetailFragment.IPlaylistDetailListener() {

            @Override
            public void onGoToMusicPlayer() {
                showMusicPlayer();

            }

            @Override
            public void onAddTrackToQueue(List<Track> listTracks) {
                vHandleAddToQueue(listTracks);
            }

            @Override
            public void onPlayTrackFromDetail() {
                if (fragPlayer == null || !(fragPlayer instanceof FullScreenPlayerFragment)) {
                    setupMusicPlayer();
                    setupPlayer(FullScreenPlayerFragment.TAG);
                }

                bNeedToLoadMusicPlayer = true;
            }
        });

        fragMainCar.setArguments(fragArgs);
        nextFragmentTransaction(false, PlaylistDetailFragment.TAG);
    }

    public void OfflineGuide() {

        if (mApplicationConfigurations.isEnabledHomeGuidePage3Offline()) {
            mApplicationConfigurations
                    .setIsEnabledHomeGuidePage_3OFFLINE(false);
            mApplicationConfigurations.setIsSongCatched(false);
            GlobalFunction.showMessageDialog(CarModeHomeActivity.this, CustomDialogLayout.DialogType.MESSAGE, getString(R.string.carmode_save_offline_dialog_message), null).setTitle(getString(R.string.save_offline_dialog_title));


        }
    }

}
