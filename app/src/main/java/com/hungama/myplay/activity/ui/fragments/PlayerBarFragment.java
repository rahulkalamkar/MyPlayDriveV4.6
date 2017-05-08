package com.hungama.myplay.activity.ui.fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Virtualizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationManager.Response;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.data.ActionDefinition;
import com.hungama.myplay.activity.data.CampaignsManager;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.ImagesManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.audiocaching.DBOHandler;
import com.hungama.myplay.activity.data.audiocaching.DataBase.CacheState;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.campaigns.Placement;
import com.hungama.myplay.activity.data.dao.campaigns.PlacementType;
import com.hungama.myplay.activity.data.dao.hungama.BaseHungamaResponse;
import com.hungama.myplay.activity.data.dao.hungama.Discover;
import com.hungama.myplay.activity.data.dao.hungama.LiveStation;
import com.hungama.myplay.activity.data.dao.hungama.LiveStationDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaCategoryType;
import com.hungama.myplay.activity.data.dao.hungama.MediaContentType;
import com.hungama.myplay.activity.data.dao.hungama.MediaItem;
import com.hungama.myplay.activity.data.dao.hungama.MediaTrackDetails;
import com.hungama.myplay.activity.data.dao.hungama.MediaType;
import com.hungama.myplay.activity.data.dao.hungama.Track;
import com.hungama.myplay.activity.data.dao.hungama.TrackLyrics;
import com.hungama.myplay.activity.data.dao.hungama.TrackTrivia;
import com.hungama.myplay.activity.gigya.ShareDialogFragment;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.AddToFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.MediaContentOperation;
import com.hungama.myplay.activity.operations.hungama.MediaDetailsOperation;
import com.hungama.myplay.activity.operations.hungama.RemoveFromFavoriteOperation;
import com.hungama.myplay.activity.operations.hungama.SocialBadgeAlertOperation;
import com.hungama.myplay.activity.operations.hungama.TrackLyricsOperation;
import com.hungama.myplay.activity.operations.hungama.TrackTriviaOperation;
import com.hungama.myplay.activity.player.PlayMode;
import com.hungama.myplay.activity.player.PlayerService;
import com.hungama.myplay.activity.player.PlayerService.Error;
import com.hungama.myplay.activity.player.PlayerService.LoopMode;
import com.hungama.myplay.activity.player.PlayerService.PlayerBarUpdateListener;
import com.hungama.myplay.activity.player.PlayerService.PlayerSericeBinder;
import com.hungama.myplay.activity.player.PlayerService.PlayerStateListener;
import com.hungama.myplay.activity.player.PlayerService.RadioBarUpdateListener;
import com.hungama.myplay.activity.player.PlayerService.State;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager;
import com.hungama.myplay.activity.player.PlayerServiceBindingManager.ServiceToken;
import com.hungama.myplay.activity.player.PlayerUpdateWidgetService;
import com.hungama.myplay.activity.player.PlayingQueue;
import com.hungama.myplay.activity.ui.ActivityMainSearchResult;
import com.hungama.myplay.activity.ui.AppGuideActivityPlayerBar;
import com.hungama.myplay.activity.ui.CommentsActivity;
import com.hungama.myplay.activity.ui.DiscoveryActivity;
import com.hungama.myplay.activity.ui.DownloadConnectingActivity;
import com.hungama.myplay.activity.ui.FavoritesActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.MainActivity.NavigationItem;
import com.hungama.myplay.activity.ui.MediaDetailsActivity;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.ui.PlayerQueueActivity;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.TrendNowActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.VideoActivity;
import com.hungama.myplay.activity.ui.dialogs.DiscoveruOfTheDayTrendCustomDialog;
import com.hungama.myplay.activity.ui.dialogs.LyricsCustomDialog;
import com.hungama.myplay.activity.ui.dialogs.PlaylistDialogFragment;
import com.hungama.myplay.activity.ui.dialogs.RadioFullPlayerInfoDialog;
import com.hungama.myplay.activity.ui.dialogs.SleepModeDialog;
import com.hungama.myplay.activity.ui.dialogs.TriviaCustomDialog;
import com.hungama.myplay.activity.ui.fragments.PlayerGymModeFragment.OnGymModeExitClickedListener;
import com.hungama.myplay.activity.ui.fragments.PlayerGymModeFragment.OnPlayButtonStateChangedListener;
import com.hungama.myplay.activity.ui.fragments.PlayerInfoFragment.OnInfoItemSelectedListener;
import com.hungama.myplay.activity.ui.inappprompts.AppPromptOfflineCachingTrialExpired;
import com.hungama.myplay.activity.ui.listeners.OnLoadMenuItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.listeners.OnMediaItemOptionSelectedListener;
import com.hungama.myplay.activity.ui.widgets.ActiveButton;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.CustomCacheStateProgressBar;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.ui.widgets.ThreeStatesActiveButton;
import com.hungama.myplay.activity.ui.widgets.ThreeStatesActiveButton.OnStateChangedListener;
import com.hungama.myplay.activity.ui.widgets.TwoStatesActiveButton;
import com.hungama.myplay.activity.ui.widgets.TwoStatesButton;
import com.hungama.myplay.activity.util.ActionCounter;
import com.hungama.myplay.activity.util.ActionCounter.OnActionCounterPerform;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Appirater;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.FlurryConstants.FlurryFullPlayerParams;
import com.hungama.myplay.activity.util.LockableScrollView;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoTarget;
import com.hungama.myplay.activity.util.QuickActionFullPlayerMore;
import com.hungama.myplay.activity.util.QuickActionFullPlayerMore.OnMoreSelectedListener;
import com.hungama.myplay.activity.util.QuickActionFullPlayerSetting;
import com.hungama.myplay.activity.util.QuickActionFullPlayerSetting.OnEditerPicsSelectedListener;
import com.hungama.myplay.activity.util.QuickActionRadioFullPlayerMore;
import com.hungama.myplay.activity.util.QuickActionRadioFullPlayerMore.OnRadioFullPlayerMoreSelectedListener;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;
import com.hungama.myplay.activity.util.coverflow.CoverFlow;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates and manages the main application's player bar in the bottom of the
 * screen.
 */
public class PlayerBarFragment extends MainFragment implements OnClickListener,
        ServiceConnection, PlayerStateListener, OnActionCounterPerform,
        PanelSlideListener, CommunicationOperationListener,
        OnLoadMenuItemOptionSelectedListener, OnGymModeExitClickedListener,
        PlayerBarUpdateListener, RadioBarUpdateListener,
        OnSeekBarChangeListener, OnEditerPicsSelectedListener,
        OnMoreSelectedListener, OnRadioFullPlayerMoreSelectedListener {
    private static final String TAG = "PlayerBarFragment";

    private static final int ACTION_INTERVAL_MS = 200;

    private static final int ACTION_MESSAGE_PREVIOUS = 100002;

    private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_TRIVIA = 1004;
    private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_INFO = 1000;
    private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_SIMILAR = 1001;
    private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_VIDEO = 1002;
    private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_LYRICS = 1003;
    private static final int DRAWER_CONTENT_ACTION_BUTTON_ID_ALBUM = 1005;

    public static final int ACTION_FAVORITE = 1010;
    public static final int ACTION_COMMENT = 1011;
    public static final int ACTION_DOWNLOAD = 1012;
    public static final int ACTION_SHARE = 1013;
    public static final int ACTION_PLAYLIST = 1014;
    public static final int ACTION_INFO = 1015;
    public static final int ACTION_SAVE_OFFLINE = 1016;
    public static final int ACTION_TWEET_THIS = 1017;
    public static final int ACTION_PLAY = 1018;

    public static final String ACTION_PLAY_STATE_CHANGED = "com.hungama.myplay.activity.intent.action.play_state_changed";

    private static final String DRAWER_CONTENT_ACTION_BUTTON_FRAGMENT_TAG = "drawer_content_action_button_fragment_tag";

    private static final String MEDIA_TYPE_SONG = "song";
    private static final String MEDIA_TYPE_ONDEMANRADIO = "ondemandradio";
    public static final int FAVORITE_SUCCESS = 1;

    private String backgroundLink;
    private static BitmapDrawable backgroundImage;
    private View rootView;
    private RelativeLayout dontWant;
    private Placement placement;

    private int width;

    public static boolean isbackFromUpgrade;
    private static boolean isUpgrading;
    private boolean userClickedLoadButton = false;
    private boolean userClickedQueueButton = false;
    private boolean userClickedTextButton = false;

    private static long artistRadioId = 0;
    private static int artistUserFav = 0;

    int width_coverFlow;
    private boolean isGymMode = false;
    private RelativeLayout rlPlayerDrawerHeaderNew, rlPlayerBarHandle;
    private TrackTrivia mTrackTrivia = null;
    private TrackLyrics mTrackLyrics = null;
    private String advertiseTxt;
    Handler commonHandler = new Handler();
    private PicassoUtil picasso;

    FragmentActivity activity;

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        this.activity = (FragmentActivity) act;
        mDataManager = DataManager.getInstance(this.activity);

        mApplicationConfigurations = mDataManager
                .getApplicationConfigurations();
        isEnglish = mApplicationConfigurations.isLanguageSupportedForWidget();

        View mini_player_layout = activity
                .findViewById(R.id.mini_player_layout);
        View mini_player_layout_support_lang = activity
                .findViewById(R.id.mini_player_layout_support_lang);
        if(mini_player_layout==null)
            return;
        if (isEnglish) {
            mini_player_layout.setVisibility(View.GONE);
            mini_player_layout_support_lang.setVisibility(View.VISIBLE);
            miniPlayerView = mini_player_layout_support_lang;
        } else {
            mini_player_layout.setVisibility(View.VISIBLE);
            mini_player_layout_support_lang.setVisibility(View.GONE);
            miniPlayerView = mini_player_layout;
        }
    }

    ;

    /**
     * Auto save alert dialog
     *
     * @param List<Track> Track List
     */

    public void askForAutoSave(final List<Track> tracks) {
        if (activity == null)
            return;
        CustomAlertDialog boardingAlert = new CustomAlertDialog(activity);
        boardingAlert.setCancelable(false);
        boardingAlert.setTitle(Utils.getMultilanguageText(mContext,
                getResources().getString(R.string.auto_save_first_time_title)));
        boardingAlert.setMessage(Utils
                .getMultilanguageText(
                        mContext,
                        getResources().getString(
                                R.string.auto_save_first_time_message)));
        boardingAlert
                .setPositiveButton(
                        Utils.getMultilanguageText(
                                mContext,
                                getResources()
                                        .getString(
                                                R.string.auto_save_first_time_button_positive)),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mApplicationConfigurations
                                        .setSaveOfflineAutoSaveMode(true);
                                if (mPlayerService != null)
                                    mPlayerService
                                            .startAutoSavingTracks(tracks);
                            }
                        });
        boardingAlert
                .setNegativeButton(
                        Utils.getMultilanguageText(
                                mContext,
                                getResources()
                                        .getString(
                                                R.string.auto_save_first_time_button_negative)),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                mApplicationConfigurations
                                        .setSaveOfflineAutoSaveMode(false);
                            }
                        });
        boardingAlert.show();
    }

    /**
     * Instant Play track
     *
     * @param tracks
     * @param flurryEventName
     * @param flurrySourceSection
     */
    public void playNow(final List<Track> tracks, final String flurryEventName,
                        final String flurrySourceSection) {
        try {
            if (!Utils.isConnected()
                    && !mApplicationConfigurations.getSaveOfflineMode()) {

                ((MainActivity) activity)
                        .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                            @Override
                            public void onRetryButtonClicked() {
                                playNow(tracks, flurryEventName,
                                        flurrySourceSection);
                            }
                        });
            } else {
                Logger.i("MediaTilesAdapter", "Play button click: PlayNow 7");
                playNowNew(tracks, flurryEventName, flurrySourceSection);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void playNowNew(final List<Track> tracks,
                           final String flurryEventName, final String flurrySourceSection) {
        showTrialOfferExpiredPopup();

        if(mPlayerService.isAdPlaying())
            return;

        Logger.i("MediaTilesAdapter", "Play button click: PlayNow 8");
        if (!Utils.isListEmpty(tracks)) {
            Logger.i("MediaTilesAdapter", "Play button click: PlayNow 9");
            try {
                try {
                    if (mPlayerService.getPlayMode() != PlayMode.MUSIC) {

                        if (mPlayerService.isAdPlaying())
                            mPlayerService.clearAd();
                        clearQueue();
                        mPlayerService.clearQueue();
                        adjustBarWhenOpenedAndNotPlaying();
                        removeRadioDetails();
                        removeDiscoveryDetails();
                        setFullPlayerBottomHeightForMusic();
                    }
                } catch (Exception e) {
                    // Logger.printStackTrace;
                }

                List<Track> queue = getCurrentPlayingList();

                /*
				 * identifies if the given list of tracks was for playing a
				 * single track or not. single track will not be played if it
				 * already in the queue.
				 */
                Logger.i("MediaTilesAdapter", "Play button click: PlayNow 10");
                if (tracks.size() > 1) {

                    StringBuilder tracksNotInQueueStr = new StringBuilder();


                    /*ArrayList<Track> tracksNotInQueue = new ArrayList<Track>();
                    for (Track track : tracks) {
                        try {
                            if (!queue.contains(track)) {
                                tracksNotInQueue.add(track);
                                tracksNotInQueueStr.append(track.getTitle())
                                        .append(", ");
                            }
                        } catch (Exception e) {
                            Logger.e(getClass().getName() + ":208",
                                    e.toString());
                        }
                    }*/

                    currentPlayingTrackTemp = mPlayerService
                            .getCurrentPlayingTrack();
                    Logger.s(" ::::::::::::: addToQueue 2");
                    final ArrayList<Track> tracksNotInQueue = new ArrayList<Track>();
                    for (Track track : tracks) {
                        if (!queue.contains(track)) {
                            tracksNotInQueue.add(track);
                            tracksNotInQueueStr.append(track.getTitle()).append(",");
                        } else {
                            int removePos = getCurrentPlayingList().indexOf(track);
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
                            helperAddToQueue(tracks);
                            mPlayerService.setCurrentPos(currentPlayingTrackTemp);
                            if(mPlayerService.getState() != State.PLAYING){
                                mPlayerService.play();
                                updatePlayerPlayIcon();
                            }else if(mPlayerService.isPlaying() && mPlayerService.getState() == State.PLAYING){
                                Utils.makeText(getActivity(),Utils.getMultilanguageText(getActivity(), getString(R.string.queue_bottom_text_now_playing)), Toast.LENGTH_SHORT).show();
                            }
                            try {
                                activity.findViewById(R.id.progressbar)
                                        .setVisibility(View.GONE);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else if (trackPosition == -1) {
                            //helperPlayNow(tracksNotInQueue);
                            helperPlayNow(tracks);
                            //mPlayerService.setCurrentPos(currentPlayingTrackTemp);
                        }
                        else {
                            //mPlayerService.playNowFromPosition(tracksNotInQueue, trackPosition);
                            mPlayerService.playNowFromPosition(
                                    tracks, trackPosition);
                            //mPlayerService.setCurrentPos(currentPlayingTrackTemp);
                        }
                        // helperPlayNowFromPosition(tracksNotInQueue,
                        // trackPosition);
                        // helperPlayNow(tracksNotInQueue);

                        Utils.makeText(activity, mMessageSongsToQueue,
                                Toast.LENGTH_SHORT).show();

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

                    } else {
                        /*int trackPosition = queue.lastIndexOf(tracks.get(0));
                        helperPlayNowFromPosition(tracksNotInQueue,
                                trackPosition);
                        updatePlayerPlayIcon();*/
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
                        if(mPlayerService.getState() != State.PLAYING){
                            mPlayerService.play();
                            updatePlayerPlayIcon();
                        }else if(mPlayerService.isPlaying() && mPlayerService.getState() == State.PLAYING){
                            Utils.makeText(getActivity(),Utils.getMultilanguageText(getActivity(), getString(R.string.queue_bottom_text_now_playing)), Toast.LENGTH_SHORT).show();
                        }
                        try {
                            activity.findViewById(R.id.progressbar)
                                    .setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }

                    if(tracks.size()==1){
                        int pos = getCurrentPlayingList().indexOf(tracks.get(0));
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
                            int removePos = getCurrentPlayingList().indexOf(track);
                            mPlayerService.removeFromQueueWhenAddToQueue(removePos);
                        }
                    }

                    if (mPlayerService != null
                            && currentPlayingTrackTemp != null
                            && tracks.get(0).getId() == currentPlayingTrackTemp.getId()) {
                        //helperAddToQueue(tracksNotInQueue);
                        helperAddToQueue(tracks);
                        mPlayerService.setCurrentPos(currentPlayingTrackTemp);
                        if(mPlayerService.getState() != State.PLAYING){
                            mPlayerService.play();
                            updatePlayerPlayIcon();
                        }else if(mPlayerService.isPlaying() && mPlayerService.getState() == State.PLAYING){
                            Utils.makeText(getActivity(),Utils.getMultilanguageText(getActivity(), getString(R.string.queue_bottom_text_now_playing)), Toast.LENGTH_SHORT).show();
                        }
                        try {
                            activity.findViewById(R.id.progressbar)
                                    .setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{

                    /*if (queue.contains(tracks.get(0))) {
                        int trackPosition = queue.lastIndexOf(tracks.get(0));
                        helperPlayNowFromPosition(tracks, trackPosition);
                        updatePlayerPlayIcon();
                        try {
                            activity.findViewById(R.id.progressbar)
                                    .setVisibility(View.GONE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {*/
                        helperPlayNow(tracks);

                        // Flurry report :song add to queue
                        Map<String, String> reportMap = new HashMap<String, String>();
                        reportMap.put(FlurryConstants.FlurryKeys.SourceSection
                                .toString(), flurrySourceSection);
                        reportMap.put(
                                FlurryConstants.FlurryKeys.SongsAddedToQueue
                                        .toString(), tracks.get(0).getTitle());
                        Analytics.logEvent(flurryEventName, reportMap);

                        Utils.makeText(activity, mMessageSongsToQueue,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            } catch (Exception e) {
                Logger.e(getClass().getName() + ":252", e.toString());
            }
        }
    }

    /**
     * Add Track to Queue
     *
     * @param tracks
     * @param flurryEventName
     * @param flurrySourceSection
     */
    public void addToQueue(final List<Track> tracks,
                           final String flurryEventName, final String flurrySourceSection) {
        if (!Utils.isConnected()
                && !mApplicationConfigurations.getSaveOfflineMode()) {
            ((MainActivity) activity)
                    .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                        @Override
                        public void onRetryButtonClicked() {
                            addToQueue(tracks, flurryEventName,
                                    flurrySourceSection);
                        }
                    });
        } else {
            addToQueue(tracks, flurryEventName, flurrySourceSection, false,
                    false);
        }
    }

    /**
     * For first time, Add Track to Queue
     *
     * @param tracks
     * @param flurryEventName
     * @param flurrySourceSection
     */
    public void addToQueueFirstTime(final List<Track> tracks,
                                    final String flurryEventName, final String flurrySourceSection) {
        if (!Utils.isConnected()
                && !mApplicationConfigurations.getSaveOfflineMode()) {

            ((MainActivity) activity)
                    .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                        @Override
                        public void onRetryButtonClicked() {
                            addToQueueFirstTime(tracks, flurryEventName,
                                    flurrySourceSection);
                        }
                    });
        } else {
            addToQueue(tracks, flurryEventName, flurrySourceSection, false,
                    true);
        }
    }

    /**
     * Check Service connection / Service started or not
     *
     * @return
     */
    public boolean isPlayerServiceAvailable() {
        return (mPlayerService != null);
    }

    Track currentPlayingTrackTemp = null;

    public void addToQueue(final List<Track> tracks, String flurryEventName,
                           String flurrySourceSection, boolean isChangingMode,
                           boolean isFirstTimeLoading) {
        showTrialOfferExpiredPopup();
        //isPlayerLoading = false;
        if (!Utils.isListEmpty(tracks)) {

            final List<Track> queue = getCurrentPlayingList();

            StringBuilder songsAddedToQueue = new StringBuilder();
            if (Utils.isListEmpty(queue) || getPlayMode() != PlayMode.MUSIC) {
                if (isFirstTimeLoading)
                    helperPlayNowForFirstTime(tracks);
                else
                    helperPlayNow(tracks);

                try {
                    if (tracks.size() > 1) {
                        Utils.makeText(activity, mMessageSongsToQueue,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Utils.makeText(activity, mMessageSongToQueue,
                                Toast.LENGTH_SHORT).show();
                    }

                    for (Track t : tracks) {
                        songsAddedToQueue.append(t.getTitle()).append(",");
                    }

                    Map<String, String> reportMap = new HashMap<String, String>();
                    reportMap
                            .put(FlurryConstants.FlurryKeys.SourceSection
                                    .toString(), flurrySourceSection);
                    reportMap.put(FlurryConstants.FlurryKeys.SongsAddedToQueue
                            .toString(), songsAddedToQueue.toString());
                    Analytics.logEvent(flurryEventName, reportMap);
                } catch (Exception e) {
                    Logger.e(getClass().getName() + ":278", e.toString());
                }
            } else {
				/*
				 * checks if the tracks in the list(to be added) are already in
				 * the queue. tracks which are not in the queue will be added.
				 * if all tracks are already in the queue none will be added.
				 */
                currentPlayingTrackTemp = mPlayerService
                        .getCurrentPlayingTrack();
                Logger.s(" ::::::::::::: addToQueue 2");
                final ArrayList<Track> tracksNotInQueue = new ArrayList<Track>();
                for (Track track : tracks) {
                    if (!queue.contains(track)) {
                        tracksNotInQueue.add(track);
                        songsAddedToQueue.append(track.getTitle()).append(",");
                    } else {
                        int removePos = getCurrentPlayingList().indexOf(track);
                        mPlayerService.removeFromQueueWhenAddToQueue(removePos);
                    }
                }
                try {
                    if (tracks.size() > 0) {
                        if (!TextUtils.isEmpty(songsAddedToQueue.toString())) {
                            Map<String, String> reportMap = new HashMap<String, String>();
                            reportMap.put(
                                    FlurryConstants.FlurryKeys.SourceSection
                                            .toString(), flurrySourceSection);
                            reportMap
                                    .put(FlurryConstants.FlurryKeys.SongsAddedToQueue
                                            .toString(), songsAddedToQueue
                                            .toString());
                            Analytics.logEvent(flurryEventName, reportMap);
                        }
                        Logger.s(" ::::::::::::: addToQueue 3");
                        Utils.makeText(activity, mMessageSongsToQueue,
                                Toast.LENGTH_SHORT).show();

                        helperAddToQueue(/* tracksNotInQueue */tracks);

                        Logger.s(" ::::::::::::: addToQueue 4");
                    } else {
                        Toast.makeText(activity, mMessageSongInQueue,
                                Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    Logger.e(getClass().getName() + ":314", e.toString());
                }
            }

            setDrawerPanelHeight();
        }
    }

    /**
     * Play next song
     *
     * @param tracks
     */

    public void playNext(final List<Track> tracks) {
        if (!Utils.isConnected()
                && !mApplicationConfigurations.getSaveOfflineMode()) {
            ((MainActivity) activity)
                    .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                        @Override
                        public void onRetryButtonClicked() {
                            playNext(tracks);
                        }
                    });
        } else {
            playNextNew(tracks);
        }
    }

    public void playNextNew(List<Track> tracks) {
        showTrialOfferExpiredPopup();

        try {
            if (!Utils.isListEmpty(tracks)) {

                if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                        || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC
                        || mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {

                    initializeBarForMusic();
                }

                mPlayerService.playNext(tracks);
                // adding new tracks should update the next / prev buttons.
                updateNextPrevButtonsIfPlaying();
            }
        } catch (Exception e) {
        }
    }

    /**
     * Start Radio Player
     *
     * @param radioTracks
     * @param playMode
     */

    public void playRadio(final List<Track> radioTracks, final PlayMode playMode) {
        if (!Utils.isConnected()
                && !mApplicationConfigurations.getSaveOfflineMode()) {
            ((MainActivity) activity)
                    .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                        @Override
                        public void onRetryButtonClicked() {
                            playRadio(radioTracks, playMode);
                        }
                    });
        } else {
            playRadioNew(radioTracks, playMode);
        }
    }

    public void playRadioNew(List<Track> radioTracks, PlayMode playMode) {
        showTrialOfferExpiredPopup();
        try {
            if (!Utils.isListEmpty(radioTracks)) {
                if (mPlayerButtonFavorites != null) {
                    mPlayerButtonFavorites.setSelected(false);
                    mPlayerButtonFavoritesHandle.setSelected(false);
                }
                if (mPlayerService.isAdPlaying())
                    mPlayerService.clearAd();

                stopProgressUpdater();

                clearPlayer();
                if (mPlayerService != null) {

                    mPlayerService.playRadio(radioTracks, playMode);
                    initializeBarForRadio();

                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Start Discovery music player first time
     *
     * @param tracks
     * @param playMode
     */
    public void playDiscoveryMusic(List<Track> tracks, PlayMode playMode) {
        showTrialOfferExpiredPopup();
        try {
            if (!Utils.isListEmpty(tracks)) {
                if (mPlayerButtonFavorites != null) {
                    mPlayerButtonFavorites.setSelected(false);
                    mPlayerButtonFavoritesHandle.setSelected(false);
                }
                if (mPlayerService.isAdPlaying())
                    mPlayerService.clearAd();

                stopProgressUpdater();

                clearPlayer();
                if (mPlayerService != null) {
                    mPlayerService.playDiscoverySongs(tracks, playMode);
                    initializeBarForRadio();
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Radio Artist Id
     *
     * @param id
     */

    public static void setArtistRadioId(long id) {
        artistRadioId = id;
    }

    public static void setArtistUserFav(int userFav) {
        artistUserFav = userFav;
    }

    /**
     * Indicates whatever the player bar is opened with full mode.
     */
    public boolean isContentOpened() {
        try {
            return mDrawer.isPanelExpanded();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes the content of the player bar from full mode to mini mode.
     */
    public void closeContent() {

        mPlayerButtonEffects.setState(fxbuttonstate);

        closeGymMode();

        if (!isContentFragmentOpen() && mDrawer.isPanelExpanded())
            mDrawer.collapsePanel();
        if (isContentFragmentOpen())
            clearActionButtons();
    }

    /**
     * Closes the content of the player bar without collapse Full player.
     */
    public void closeContentWithoutCollapsedPanel() {
        mPlayerButtonEffects.setState(fxbuttonstate);
        closeGymMode();
    }

    /**
     * Get Current player mode from Music, Live Radio, On Demand Radio and
     * Discovery Music Player
     *
     * @return
     */
    public PlayMode getPlayMode() {
        if (mPlayerService != null)
            return mPlayerService.getPlayMode();

        return PlayMode.MUSIC;
    }

    /**
     * Check Music Player Running or not
     *
     * @return
     */
    public boolean isPlaying() {
        if (mPlayerService != null) {
            return mPlayerService.isPlaying();
        }
        return false;
    }

    /**
     * Get Current Player State
     *
     * @return
     */
    public State getPlayerState() {
        if (mPlayerService != null) {
            return mPlayerService.getState();
        }
        return State.IDLE;
    }

    public boolean isPlayingForExit() {
        if (mPlayerService != null) {
            return mPlayerService.isPlayingForExit();
        }
        return false;
    }

    public boolean isLoading() {
        if (mPlayerService != null) {
            mPlayerService.isLoading();
        }
        return false;
    }

    /**
     * Current Track List From Queue
     *
     * @return
     */
    public List<Track> getCurrentPlayingList() {
        if (mPlayerService != null) {
            /*if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC)
                return (mDataManager
                        .getStoredPlayingQueue(mApplicationConfigurations)).getCopy();
            else*/
                return mPlayerService.getPlayingQueue();
        }
        return null;
    }

    /**
     * Player Next Song Track details
     *
     * @return
     */
    public Track getNextTrack() {
        if (mPlayerService != null) {
            return mPlayerService.getNextTrack();
        }
        return null;
    }

    public int getCurrentPlayingInQueuePosition() {
        if (mPlayerService != null) {
            return mPlayerService.getCurrentQueuePosition();
        }
        return PlayingQueue.POSITION_NOT_AVAILABLE;
    }

    /**
     * Play Music Player
     */
    public void play() {
        if (mPlayerService != null) {
            if (!mPlayerService.isLoading())
                mPlayerService.play();
            else
                Utils.makeText(
                        activity,
                        getResources().getString(
                                R.string.application_dialog_loading), 0).show();
        }
    }

    /**
     * Play any selected Song from Queue using Position
     *
     * @param newPosition - Position in Queue list
     */
    public void playFromPosition(int newPosition) {
        showTrialOfferExpiredPopup();
        if (mPlayerService != null) {
            mPlayerService.playFromPosition(newPosition);
        }
    }

    public void pause() {
        if (mPlayerService != null) {
            mPlayerService.pause();
        }
    }

    /**
     * Stops playing the music and closes the service, and removes the
     * notification. Call this only when explicitly exiting the application,
     */
    public void explicitStop() {
        if (mPlayerService != null) {
            mPlayerService.explicitStop();
        }
    }

    /**
     * Remove Track from Queue List Using position
     *
     * @param position
     * @return
     */
    public Track removeFrom(int position) {
        if (mPlayerService != null) {
            final int currentPosition = mPlayerService
                    .getCurrentPlayingTrackPosition();
            if (currentPosition == position) {
                mPlayerService.stop();
                mPlayerService.removeFrom(position);
                if (mPlayerService.getPlayingQueue().size() == 0)
                    clearQueue();
                else
                    mPlayerService.playFromPositionNew(mPlayerService
                            .getCurrentPlayingTrackPosition());
            } else {
                mPlayerService.removeFrom(position);
            }
            activity.sendBroadcast(new Intent(
                    PlayerUpdateWidgetService.ACTION_PLAYER_QUEUE_UPDATED));
        }
        return null;
    }

    /**
     * Remove multiple tracks through track ids
     *
     * @param idsToRemove - Array of Track Id
     * @return
     */
    public Track removeTrack(ArrayList<Long> idsToRemove) {
        try {
            if (mPlayerService != null) {
                boolean removedCurrentPlayingSong = false;
                for (long trackId : idsToRemove) {
                    List<Track> trackList = getCurrentPlayingList();
                    int position = -1;
                    if (trackList != null) {
                        for (int i = 0; i < trackList.size(); i++) {

                            if (trackList.get(i).getId() == trackId) {

                                position = i;
                                break;
                            }
                        }
                    }

                    if (position > -1) {
                        final int currentPosition = mPlayerService
                                .getCurrentPlayingTrackPosition();

                        if (currentPosition == position) {
                            removedCurrentPlayingSong = true;
                            mPlayerService.stop();
                            mPlayerService.removeFrom(position);
                        } else {
                            mPlayerService.removeFrom(position);
                        }
                    }
                }
                if (idsToRemove.size() > 0) {
                    if (mPlayerService.getPlayingQueue().size() == 0)
                        clearQueue();
                    else if (removedCurrentPlayingSong)
                        mPlayerService.playFromPositionNew(mPlayerService
                                .getCurrentPlayingTrackPosition());
                    activity.sendBroadcast(new Intent(
                            PlayerUpdateWidgetService.ACTION_PLAYER_QUEUE_UPDATED));
                }
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        return null;
    }

    /**
     * Clear Ads, Stop Previous Playing, Clear Player Layout and Initialize
     * Music Player Layout
     */
    public void clearQueue() {
        if (mPlayerService != null) {
            mPlayerService.stop();
            mPlayerService.clearAd();
            clearPlayer();
            initializeBarForMusic();
        }
    }

    // ======================================================
    // Public helper methods.
    // ======================================================
    private void helperPlayNow(List<Track> tracks) {
        try {
            if (mPlayerService.isAdPlaying()) {
                // mPlayerService.clearAd();
                mPlayerService.addToQueueAfterCurrentPosition(tracks);
                return;
            }
            // resets the views for music if the player was playing in different
            // mode.
            if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                initializeBarForMusic();
            }
            stopProgressUpdater();
            clearPlayer();
            if (mPlayerService != null) {
                mPlayerService.registerPlayerStateListener(this);
            }
            mPlayerService.playNow(tracks);
            // adding new tracks should update the next / prev buttons.
            updateNextPrevButtonsIfPlaying();
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    List<Track> firstTimkeTracks;

    // ======================================================
    // Public helper methods.
    // ======================================================
    private void helperPlayNowForFirstTime(List<Track> tracks) {
        try {
            if (mPlayerService.isAdPlaying())
                mPlayerService.clearAd();
            // resets the views for music if the player was playing in different
            // mode.
            if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC
                    || mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                initializeBarForMusic();
            }
            stopProgressUpdater();
            clearPlayer();
            if (mPlayerService != null) {
                mPlayerService.registerPlayerStateListener(this);
            }
            mPlayerService.AddFirstTimeQueue(tracks);
            LoadFirstTimeTrack(mPlayerService.getCurrentPlayingTrack());
            // adding new tracks should update the next / prev buttons.
            updateNextPrevButtonsIfPlaying();
        } catch (Exception e) {

            if (mPlayerService == null) {
                firstTimkeTracks = tracks;
            }
            Logger.printStackTrace(e);
        }
    }

    private void helperPlayNowFromPosition(List<Track> tracks, int trackPosition) {
        try {
            if (mPlayerService.isAdPlaying()) {
                mPlayerService.addToQueueAfterCurrentPosition(tracks,
                        trackPosition);
                return;
            }
            // resets the views for music if the player was playing in different
            // mode.
            Logger.i("MediaTilesAdapter", "Play button click: PlayNow 13");
            if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC
                    || mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {

                initializeBarForMusic();
            } else {
                PlayingQueue mPlayingQueue = mPlayerService
                        .getPlayerQueueObject();
                if (mPlayerService.getPlayingQueue() != null
                        && mPlayingQueue.getCurrentPosition() == trackPosition) {
                    if (mPlayerService.getState() != State.PLAYING)
                        mPlayerService.play();
                    else
                        Utils.makeText(
                                activity,
                                getResources().getString(
                                        R.string.queue_bottom_text_now_playing),
                                0).show();

                    return;
                } else {
                    Utils.makeText(
                            activity,
                            getResources().getString(
                                    R.string.queue_bottom_text_now_playing1),
                            Toast.LENGTH_SHORT).show();
                }
            }
            stopProgressUpdater();
            clearPlayer();
            Logger.i("MediaTilesAdapter", "Play button click: PlayNow 14");
            mPlayerService.playNowFromPosition(tracks, trackPosition);
            updateNextPrevButtonsIfPlaying();
        } catch (Exception e) {
        }
    }

    private void helperAddToQueue(List<Track> tracks) {
        // resets the views for music if the player was playing in different
        // mode.
        if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC
                || mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {

            initializeBarForMusic();
        }

        mPlayerService.addToQueue(tracks);

        updateNextPrevButtonsIfPlaying();
        mPlayerService.setCurrentPos(currentPlayingTrackTemp);
        refreshFlipImages();

    }

    private String mSearchActionSelected = "No search action selected";

    /**
     * Open search screen with search text
     *
     * @param videoQuery
     */
    protected void openMainSearchFragment(String videoQuery) {
        Bundle arguments = new Bundle();
        arguments.putString(MainSearchResultsFragment.FRAGMENT_ARGUMENT_QUERY,
                videoQuery);
        arguments.putString(MainSearchResultsFragment.FRAGMENT_ARGUMENT_TYPE,
                "");
        arguments.putString(
                MainSearchResultsFragment.FLURRY_SEARCH_ACTION_SELECTED,
                mSearchActionSelected);
        arguments.putBoolean(MainSearchResultsFragment.FROM_FULL_PLAYER, true);
        Intent intent = new Intent(activity, ActivityMainSearchResult.class);
        intent.putExtras(arguments);
        startActivity(intent);
    }

    /**
     * Open Comment page
     *
     * @param mediaItem
     */
    protected void openCommentsPage(final MediaItem mediaItem) {
        Intent showComments = new Intent(activity.getApplicationContext(),
                CommentsActivity.class);
        showComments.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        showComments.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        // sets the args.
        Bundle args = new Bundle();
        args.putSerializable(CommentsActivity.EXTRA_DATA_MEDIA_ITEM,
                (Serializable) mediaItem);
        args.putBoolean(CommentsActivity.EXTRA_DATA_DO_SHOW_TITLE, true);
        args.putString(CommentsActivity.FLURRY_SOURCE_SECTION,
                FlurryConstants.FlurryComments.FullPlayer.toString());

        showComments.putExtras(args);
        activity.startActivity(showComments);

//        FragmentManager.BackStackEntry backEntry=getFragmentManager().getBackStackEntryAt(getActivity().getSupportFragmentManager().getBackStackEntryCount()-1);
//        String str=backEntry.getName();
//        Fragment fragment=getFragmentManager().findFragmentByTag(str);
//        if(!(fragment instanceof CommentsFragment)){
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//
//                    ((MainActivity) getActivity()).CallCommemtfragment(mediaItem);
//
//                }
//            }, 500);
//        }
//
//        collapseexpandplayerbar(false);


    }

    // ======================================================
    // Fragment's public events.
    // ======================================================

    /**
     * Next Player Track Listener
     */
    public interface NextTrackUpdateListener {

        public void onNextTrackUpdateListener(Track track);
    }

    /**
     * Live radio details update Listener
     */
    public interface LiveRadioUpdateListener {

        public void onLiveRadioUpdateListener(LiveStationDetails detail);

        public void onLiveRadioUpdateFailedListener();
    }

    /**
     * Register for Next Track Update Listener
     *
     * @param listener
     */
    public void registerToNextTrackUpdateListener(
            NextTrackUpdateListener listener) {
        if (mNextTrackUpdateListeners != null
                && !mNextTrackUpdateListeners.contains(listener))
            mNextTrackUpdateListeners.add(listener);
    }

    /**
     * Unregister for Next Track Update Listener
     *
     * @param listener
     */
    public void unregisterToNextTrackUpdateListener(
            NextTrackUpdateListener listener) {
        mNextTrackUpdateListeners.remove(listener);
    }

    /**
     * Register for Live radio detail Update Listener
     *
     * @param listener
     */
    public void registerLiveRadioUpdateListener(LiveRadioUpdateListener listener) {
        mLiveRadioUpdateListeners.add(listener);
    }

    /**
     * Unregister for Live radio detail Update Listener
     *
     * @param listener
     */
    public void unregisterLiveRadioUpdateListener(
            LiveRadioUpdateListener listener) {
        mLiveRadioUpdateListeners.remove(listener);
    }

    /**
     * Playing Event Listener- onTrackLoad, onTrackPlay, onTrackFinish
     */
    public interface PlayingEventListener {

        public void onTrackLoad();

        public void onTrackPlay();

        public void onTrackFinish();
    }

    /**
     * Register Playing Event Listener
     *
     * @param listener
     */
    public void setPlayingEventListener(PlayingEventListener listener) {
        mPlayingEventListener = listener;
    }

    private Context mContext;
    private Resources mResources;

    private DataManager mDataManager;
    private ApplicationConfigurations mApplicationConfigurations;
    private FragmentManager mFragmentManager;

    // player connection;
    private ServiceToken mServiceToken = null;
    public PlayerService mPlayerService = null;

    private String mMessageSongsToQueue;
    private String mMessageSongToQueue;
    private String mMessageSongInQueue;

    /*
	 * Action counter for rapidly actions performs on the bar, like Next and
	 * Previous buttons handling.
	 */
    private ActionCounter mActionCounter;

    // player bar user controls.
    private RelativeLayout mPlayerSeekBar, mPlayerSeekBarHandle;
    private SeekBar mPlayerSeekBarProgress, mPlayerSeekBarProgressHandle;
    private TextView mPlayerTextCurrent;
    private TextView mPlayerTextDuration;

    // player bar text.

    private TextView mPlayerTextTitleHandle;
    private View viewVerticalLine;

    private TextView mDrawerTextAdditionalHandle;

    // player bar buttons
    private ActiveButton mPlayerButtonPlay, mPlayerButtonPlayRadio,
            mPlayerButtonPlayHandle, mPlayerButtonPlayRadioHandle;
    private ActiveButton mPlayerButtonPrevious;
    private ActiveButton mPlayerButtonNext, mPlayerButtonNextHandle;
    private ActiveButton mPlayerButtonFavorites, mPlayerButtonFavoritesHandle;
    private ImageView mPlayerButtonQueueHandle;
    private ImageView mPlayerButtonQueue;
    private TwoStatesActiveButton mPlayerButtonShuffle;
    private ThreeStatesActiveButton mPlayerButtonLoop;
    private ActiveButton mPlayerButtonSettings;
    private Button mPlayerButtonLoad;
    private LinearLayout mPlayerBarText;
    private RelativeLayout mPlayerBarLayoutCacheState,
            mPlayerBarLayoutCacheStateHandle;
    private CustomCacheStateProgressBar playerBarProgressCacheState;

    private Button close;

    private SlidingUpPanelLayout mDrawer;
    private RelativeLayout mDrawerInfoBar;
    private RelativeLayout mDrawerInfoBarRadio;

    private ImageView mDrawerMediaArt;

    private LinearLayout mDrawerActionsBar;
    // Player content controllers when playing.
    private TextView mDrawerTextTitle, mDrawerTextTitleHandle,
            mDrawerTextTitleRadio, mDrawerTextAdditionalHandleRadio;

    private Button mDrawerButtonAddFavorites;

    private ImageView mDrawerButtonSettings;
    private ImageButton mDrawerButtonSettingsRadio;
    private RelativeLayout adHolder, adHolderOuter;
    private ImageView mHeaderInfo;
    private Button mDrawerActionShare;
    private Button mDrawerActionPlaylist;
    private TwoStatesButton mDrawerActionTrivia;
    private Button mDrawerActionMore;
    private Button mDrawerActionSimilar;
    private Button mDrawerActionAlbum;

    private Button mDrawerActionInfo;
    private Button mDrawerActionVideo;
    private TwoStatesButton mDrawerActionLyrics;

    private MediaTrackDetails mCurrentTrackDetails = null;

    private List<NextTrackUpdateListener> mNextTrackUpdateListeners = new ArrayList<NextTrackUpdateListener>();
    private List<LiveRadioUpdateListener> mLiveRadioUpdateListeners = new ArrayList<LiveRadioUpdateListener>();

    private PlayingEventListener mPlayingEventListener;

    private String blueTitleLiveRadio, blueTitleTopOnDemandRadio;

    private LocalBroadcastManager mLocalBroadcastManager;
    private MediaItemFavoriteStateReceiver mMediaItemFavoriteStateReceiver;
    private TrackReloadReceiver mTrackReloadReceiver;
    private int aspectRatio;
    private static boolean hideAd;
    public static final String HIDE_AD = "hide";
    public static final String SHOW_AD = "show";
    private PlayerStateReceiver playerStateReceiver;

    static int ObjectCouner = 0;

    private boolean isEnglish;
    boolean isFromCreate;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ObjectCouner++;
        // Logger.i(TAG, "Parent Activity " + activity);
        picasso = PicassoUtil.with(activity);
        mFragmentManager = activity.getSupportFragmentManager();

        mContext = activity;
        mResources = getResources();

        // (mApplicationConfigurations.getUserSelectedLanguage() == 0);
        // sets volume controls.
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(mContext);
        mMediaItemFavoriteStateReceiver = new MediaItemFavoriteStateReceiver(
                this);
        mTrackReloadReceiver = new TrackReloadReceiver();

        Resources resources = getResources();
        mMessageSongToQueue = resources
                .getString(R.string.main_player_bar_message_song_added_to_queue);
        mMessageSongsToQueue = resources
                .getString(R.string.main_player_bar_message_songs_added_to_queue);
        mMessageSongInQueue = resources
                .getString(R.string.main_player_bar_message_song_already_in_queue);

        blueTitleLiveRadio = Utils.getMultilanguageText(mContext,
                resources.getString(R.string.live_radio_blue_title));

        blueTitleTopOnDemandRadio = Utils.getMultilanguageText(mContext,
                resources.getString(R.string.radio_top_artist_radio));

        IntentFilter filter = new IntentFilter(PlayerService.TRACK_FINISHED);
        activity.registerReceiver(trackReceiver, filter);
        IntentFilter adFilter = new IntentFilter(HIDE_AD);
        adFilter.addAction(SHOW_AD);
        adhider = new HideAdReceiver();
        activity.registerReceiver(adhider, adFilter);

        headsetPlugReceiver = new HeadSetReceiver();
        IntentFilter headsetFilter = new IntentFilter(
                Intent.ACTION_HEADSET_PLUG);
        activity.registerReceiver(headsetPlugReceiver, headsetFilter);


        IntentFilter castActOpenFilter = new IntentFilter("castActOpen");
        activity.registerReceiver(mCastDialogClickReceiver, castActOpenFilter);

        //Patibandha
        //InItmediaRouterButton();
    }

    BroadcastReceiver trackReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Logger.i("Track", "Finished!");
            if (mDrawer != null && mDrawer.isPanelExpanded())
                initializeAds(true);
        }
    };

    HideAdReceiver adhider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Utils.clearCache();
        ((MainActivity) activity).getSupportActionBar()
                .setShowHideAnimationEnabled(true);
        mApplicationConfigurations = ApplicationConfigurations
                .getInstance(getActivity());
        if (isEnglish) {
            try {
                rootView = inflater
                        .inflate(
                                R.layout.fragment_main_player_bar_new_supported_language,
                                container, false);
                if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
                    Utils.traverseChild(rootView, activity);
                }
            } catch (java.lang.Error e) {
                Utils.clearCache();
                rootView = inflater
                        .inflate(
                                R.layout.fragment_main_player_bar_new_supported_language,
                                container, false);
                if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
                    Utils.traverseChild(rootView, activity);
                }
            }
        } else {
            try {
                rootView = inflater
                        .inflate(R.layout.fragment_main_player_bar_new,
                                container, false);
                if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
                    Utils.traverseChild(rootView, activity);
                }
            } catch (java.lang.Error e) {
                Utils.clearCache();
                rootView = inflater
                        .inflate(R.layout.fragment_main_player_bar_new,
                                container, false);
                if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
                    Utils.traverseChild(rootView, activity);
                }
            }
        }
        isFromCreate = true;

        initializeUserControls(rootView);
        getDrawerPanelHeightFirstTime();
//        setRetainInstance(true);
        return rootView;
    }

/*    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }*/

    /**
     * Broadcast Receiver HideAdReceiver
     */

    class HideAdReceiver extends BroadcastReceiver {

        /*
		 * (non-Javadoc)
		 * 
		 * @see
		 * android.content.BroadcastReceiver#onReceive(android.content.Context,
		 * android.content.Intent)
		 */
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(HIDE_AD)) {
                hideAd = true;
                initializeAds(false);
            } else if (intent.getAction().equals(SHOW_AD)) {
                hideAd = false;
                initializeAds(false);
            }
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Player Ads initialize (Music, Radio, On Demand radio, Discovery Radio)
     *
     * @param next
     */
    private void initializeAds(boolean next) {
        Utils.clearCache();
        try {
            if (adHolder != null)
                adHolder.setVisibility(View.INVISIBLE);
            if (adHolderOuter != null)
                adHolderOuter.setVisibility(View.INVISIBLE);
            if (dontWant != null)
                dontWant.setVisibility(View.INVISIBLE);
            // dontWant.setVisibility(View.GONE);
            if (mPlayerService.isQueueEmpty() || isContentFragmentOpen() || mPlayerService.isAdPlaying()) {
                //adhandler.postDelayed(refreshAd, 1000);
                startRefreshAdsHandler();
                return;
            }
            CampaignsManager mCampaignsManager = CampaignsManager
                    .getInstance(activity);

            if (mPlayerService != null
                    && (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || mPlayerService
                    .getPlayMode() == PlayMode.DISCOVERY_MUSIC))
                // placement = mCampaignsManager
                // .getPlacementOfType(PlacementType.RADIO_CHANNEL);
                return;
            else
                placement = mCampaignsManager
                        .getPlacementOfType(PlacementType.PLAYER_ALBUM_ART_OVERLAY_FULL);

            if (placement != null) {
                DisplayMetrics metrics = null;
                if (HomeActivity.metrics == null) {
                    metrics = new DisplayMetrics();
                    activity.getWindowManager().getDefaultDisplay()
                            .getMetrics(metrics);
                } else {
                    metrics = HomeActivity.metrics;
                }

                // fileCache = new FileCache(activity);

                width = metrics.widthPixels;
                if (next) {
                    try {
                        if (backgroundImage != null) {
                            if (adHolder != null) {
                                adHolder.setBackgroundColor(Color.TRANSPARENT);
                            }
                            // backgroundImage.getBitmap().recycle();
                            backgroundImage.setCallback(null);
                        }
                    } catch (Exception e) {
                        Logger.printStackTrace(e);
                    }
                    backgroundImage = null;
                }
                backgroundLink = Utils.getDisplayProfile(metrics, placement);
                if (backgroundLink != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (backgroundImage == null) {
                                    backgroundImage = Utils.getBitmap(activity,
                                            width, backgroundLink);
                                }
                                width = width / 2;
                                if (!isUpgrading && backgroundImage != null) {
                                    adhandler.sendEmptyMessage(0);
                                    Logger.i("Message", "Sent");
                                }
                            } catch (Exception e) {
                                Logger.printStackTrace(e);
                            }
                        }
                    }).start();
                }
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        } catch (java.lang.Error e) {
            Logger.printStackTrace(e);
        }
    }

    /**
     * Check any Content Fragment open or not like (Similar, Album, Info)
     *
     * @return
     */
    public boolean isContentFragmentOpen() {
        Fragment lastOpenedFragment = mFragmentManager
                .findFragmentByTag(DRAWER_CONTENT_ACTION_BUTTON_FRAGMENT_TAG);
        return (lastOpenedFragment != null);
    }

    private long lastAdViewEventReportingTime;

    Handler adhandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            // adhandler.removeMessages(0);
            adhandler.removeCallbacks(refreshAd);
            final int adRefreshInterval = mApplicationConfigurations
                    .getAppConfigPlayerOverlayRefresh();// .getAdRefreshInterval();
            if (backgroundImage != null
                    && backgroundImage.getIntrinsicHeight() > 0) {
                try {
                    if (!isVisible()
                            || isDetached()
                            || isUpgrading
                            || (mPlayerService != null && mPlayerService
                            .isAdPlaying())) {
                        if (mPlayerService != null
                                && mPlayerService.isAdPlaying()) {
                            if (adHolder != null
                                    && (adHolder.getVisibility() == View.VISIBLE)) {
                                adHolder.setVisibility(View.INVISIBLE);
                                adHolderOuter.setVisibility(View.INVISIBLE);
                                dontWant.setVisibility(View.VISIBLE);
                                // displayFlipAds(null);
                                // dontWant.setVisibility(View.GONE);
                            }
                        }
                        adhandler.postDelayed(refreshAd, 5 * 1000);
                        return;
                    }

                    View adParentView;
                    if (mPlayerService != null
                            && mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                        RadioDetailsFragment radioDetailsFragment = (RadioDetailsFragment) mFragmentManager
                                .findFragmentByTag(RadioDetailsFragment.TAG);
                        adParentView = radioDetailsFragment.getView();
                    } else {
                        adParentView = rootView;
                    }
                    Logger.i("Message", "used");

                    if (adHolder == null) {
                        adHolder = (RelativeLayout) adParentView
                                .findViewById(R.id.main_player_drawer_ad);
                    }
                    if (adHolderOuter == null) {
                        adHolderOuter = (RelativeLayout) adParentView
                                .findViewById(R.id.main_player_drawer_ad_outer);
                    }

                    if (dontWant == null) {
                        dontWant = (RelativeLayout) adParentView
                                .findViewById(R.id.main_player_dont_want_ads);
                    }

                    if (hideAd) {
                        ((RelativeLayout) getView().findViewById(
                                R.id.main_player_drawer_ad))
                                .setVisibility(View.INVISIBLE);
                        ((RelativeLayout) getView().findViewById(
                                R.id.main_player_drawer_ad_outer))
                                .setVisibility(View.INVISIBLE);
                        rootView.findViewById(R.id.main_player_dont_want_ads)
                                .setVisibility(View.INVISIBLE);
                        return;
                    } else {
                        if (adHolder != null
                                && (adHolder.getVisibility() == View.INVISIBLE)) {
                            adHolder.setVisibility(View.VISIBLE);
                            // adHolderOuter.setVisibility(View.VISIBLE);
                            adHolderOuter.setVisibility(View.INVISIBLE);
                            dontWant.setVisibility(View.GONE);
                        }
                    }

                    aspectRatio = backgroundImage.getIntrinsicWidth()
                            / backgroundImage.getIntrinsicHeight();

                    adHolder.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            try {
                                Utils.performclickEvent(activity, placement);
                            } catch (Exception e) {
                                Logger.printStackTrace(e);
                            }
                        }
                    });

                    if (mApplicationConfigurations
                            .isUserHasTrialSubscriptionPlan()) {
                        LanguageTextView adParenttext = (LanguageTextView) adParentView
                                .findViewById(R.id.main_player_text_dont_want_ads);
                        Utils.SetMultilanguageTextOnTextView(
                                mContext,
                                adParenttext,
                                getResources()
                                        .getString(
                                                R.string.dont_want_ad_message_trial_user));
                        dontWant.setOnClickListener(null);
                    } else {
                        dontWant.setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                try {
                                    Intent upgradeIntent = new Intent(activity,
                                            UpgradeActivity.class);
                                    upgradeIntent.putExtra("player", true);
                                    startActivity(upgradeIntent);
                                    isUpgrading = true;
                                } catch (Exception e) {
                                    Logger.printStackTrace(e);
                                }

                            }
                        });
                    }

                    final ViewGroup.LayoutParams params = adHolder
                            .getLayoutParams();
                    // params.height = (int) ((width /** density */
                    // ) / aspectRatio);
                    // params.width = (int) ((width /** density */
                    // ));
                    params.height = (int) ((backgroundImage.getIntrinsicWidth()) / aspectRatio);
                    params.width = (int) ((backgroundImage.getIntrinsicWidth()));
                    adHolder.setVisibility(View.INVISIBLE);
                    adHolderOuter.setVisibility(View.INVISIBLE);
                    adhandler.postDelayed(new Runnable() {
                        public void run() {
                            if (adHolder != null) {
                                params.height = (int) (adHolder.getWidth() / aspectRatio);
                                // params.height = (int) (width / aspectRatio);
                                adHolder.setLayoutParams(params);
                                adHolder.setVisibility(View.VISIBLE);
                                adHolderOuter.setVisibility(View.VISIBLE);
                                adHolderOuter.setVisibility(View.INVISIBLE);
                            }
                        }
                    }, 100);
                    adHolder.setLayoutParams(params);
                    // adHolder.setBackgroundDrawable(backgroundImage);
                    displayFlipAds(backgroundImage);
                    if (isVisible() && mDrawer != null && mDrawer.isPanelExpanded()
                            && !isPaused && !isContentFragmentOpen() && isContentOpened()) {
                        if (mPlayerService != null
                                && mPlayerService.getPlayMode() != PlayMode.TOP_ARTISTS_RADIO
                                && mPlayerService.getPlayMode() != PlayMode.DISCOVERY_MUSIC)
                            if (lastAdViewEventReportingTime == 0
                                    || (System.currentTimeMillis() - lastAdViewEventReportingTime) >= (adRefreshInterval - 2) * 1000) {
                                lastAdViewEventReportingTime = System
                                        .currentTimeMillis();
                                Utils.postViewEvent(activity, placement);
                            }
                        // Utils.postViewEvent(activity, placement);
                    }

                    Logger.i("Message", "set");
                    Logger.i(
                            "Message",
                            "visibility:"
                                    + String.valueOf(adHolder.getVisibility()));
                    close = (Button) adParentView
                            .findViewById(R.id.bCloseVideoAd);
                    close.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View arg0) {
                            try {
                                adHolder.setVisibility(View.INVISIBLE);
                                adHolderOuter.setVisibility(View.INVISIBLE);
                                // dontWant.setVisibility(View.VISIBLE);
                                dontWant.setVisibility(View.GONE);
                                displayFlipAds(null);
                                // dontWant.setVisibility(View.GONE);
                                if (!isUpgrading) {
                                    adhandler.removeMessages(0);
                                    adhandler.removeCallbacks(refreshAd);
                                    adhandler.postDelayed(refreshAd,
                                            adRefreshInterval * 1000);
                                }
                            } catch (Exception e) {
                                Logger.printStackTrace(e);
                            }
                        }
                    });

                    adhandler.postDelayed(refreshAd, adRefreshInterval * 1000);

                } catch (Exception e) {
                } catch (java.lang.Error e) {
                }

            } else {
                adhandler.sendEmptyMessage(0);
            }
        }
    };

    public void startRefreshAdsHandler() {
        final int adRefreshInterval = mApplicationConfigurations
                .getAppConfigPlayerOverlayRefresh();//Start();// getAdRefreshInterval();
        adhandler.removeCallbacks(refreshAd);
        adhandler.postDelayed(refreshAd, adRefreshInterval * 1000);
    }

    /**
     * Refresh Ads
     */
    Runnable refreshAd = new Runnable() {
        @Override
        public void run() {
            if (isVisible())
                initializeAds(true);
        }
    };

    private boolean isPaused;

    @Override
    public void onStart() {
        try {
            super.onStart();
            isNextPrevBtnClick = false;
            mServiceToken = PlayerServiceBindingManager.bindToService(activity,
                    this);

            // listen to changes in the media item's favorition state.
            IntentFilter filter = new IntentFilter(
                    ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
            mLocalBroadcastManager.registerReceiver(
                    mMediaItemFavoriteStateReceiver, filter);
            IntentFilter filterReload = new IntentFilter(
                    ActionDefinition.ACTION_MEDIA_DETAIL_RELOADED);
            mLocalBroadcastManager.registerReceiver(mTrackReloadReceiver,
                    filterReload);

//            Analytics.startSession(activity, this);
        } catch (Exception e) {
        } catch (java.lang.Error e) {
            Utils.clearCache();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Patibandha
        //onResumeMediaRouter();

        // bgImgUrl = null;
        isPaused = false;
        isUpdated = false;
        isUpdatedFullScreen = false;
        // creates the action counter.
        mActionCounter = new ActionCounter(ACTION_INTERVAL_MS);
        mActionCounter.setOnActionCounterPerform(this);

        if (advertiseTxt == null)
            advertiseTxt = Utils.getMultilanguageTextLayOut(activity,
                    "Advertisement");
        try {
            if (playerStateReceiver == null) {
                playerStateReceiver = new PlayerStateReceiver();
                activity.registerReceiver(playerStateReceiver,
                        new IntentFilter(ACTION_PLAY_STATE_CHANGED));
            }
        } catch (Exception e) {
        } catch (java.lang.Error e) {
            Utils.clearCache();
        }
        if (mPlayerService != null)
            mPlayerService.registerPlayerUpdateListeners(this);

        if (((MainActivity) activity).isSkipResume) {
            return;
        }
        try {
            if (isbackFromUpgrade) {
                initializeAds(false);

                if (close != null)
                    close.performClick();
                isbackFromUpgrade = false;
                isUpgrading = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        startProgressUpdater();

        if (mDrawer!=null && mDrawer.isPanelExpanded())
            startTitleHandler();
    }

    public void getDrawerPanelHeightFirstTime() {
        commonHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
                setDrawerPanelHeight();
            }
        }, 1000);
    }

    public void setDrawerPanelHeight() {
        if (getActivity() instanceof CommentsActivity) {
            if (mDrawer != null)
                mDrawer.hidePanel();
            return;
        }
        List<Track> queue = getCurrentPlayingList();
        if (mDrawer != null)
            if (queue != null && queue.size() > 0) {
                if (!(activity instanceof PlayerQueueActivity)
                        && !(activity instanceof VideoActivity))
                    mDrawer.showPanel();

                Logger.i("hidePanel", "setDrawerPanelHeight showPanel");
            } else if (queue == null || (queue != null && queue.size() == 0)) {
                if (mDrawer.isPanelExpanded())
                    mDrawer.collapsePanel();
                else
                    mDrawer.hidePanel();
                Logger.i("hidePanel", "setDrawerPanelHeight hidePanel");
            }
    }

    public void hideAndShowPlayerDrawer(boolean needToSHow) {
        if (needToSHow) {
            setDrawerPanelHeight();
        } else {
            mDrawer.hidePanel();
        }
    }

    public void collapseexpandplayerbar(boolean value) {
        if (value) {

            mDrawer.expandPanel();

        } else {
            mDrawer.collapsePanel();

        }
    }

    @Override
    public void onPause() {
        // Patibandha
        //onPauseMediaRouter();

        isPaused = true;
        isFromCreate = false;
        isNextPrevBtnClick = false;
        // stops and destroys the action counter.
        mActionCounter.setOnActionCounterPerform(null);
        mActionCounter.cancelAnyAction();
        mActionCounter = null;
        if (mPlayerService != null)
            mPlayerService.unregisterPlayerUpdateListeners(this);

        super.onPause();
    }

    @Override
    public void onStop() {
        PlayerServiceBindingManager.unbindFromService(mServiceToken);
        mLocalBroadcastManager
                .unregisterReceiver(mMediaItemFavoriteStateReceiver);
        mLocalBroadcastManager.unregisterReceiver(mTrackReloadReceiver);
        try {
            activity.unregisterReceiver(trackReceiver);
        } catch (Exception e) {
        }

        try {
            activity.unregisterReceiver(adhider);
        } catch (Exception e) {
        }

        try {
            activity.unregisterReceiver(headsetPlugReceiver);
        } catch (Exception e) {
        }

        try {
            if (playerStateReceiver != null) {
                activity.unregisterReceiver(playerStateReceiver);
                playerStateReceiver = null;
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }

        removeTitleHandler(true);
        super.onStop();
        Analytics.onEndSession(activity);
    }

    @Override
    public void onDestroyView() {

        cancelLoadingMediaDetails();
        ObjectCouner--;
        if (mPlayerService != null)
            mPlayerService.unregisterPlayerStateListener(this);

        if (ObjectCouner == 0) {
            // remove static reference
            if (backgroundImage != null && backgroundImage.getBitmap() != null
                    && !backgroundImage.getBitmap().isRecycled()) {
                backgroundImage.getBitmap().recycle();
                backgroundImage = null;
            }
        }
        try {
            if (mCastDialogClickReceiver != null) {
                activity.unregisterReceiver(mCastDialogClickReceiver);
                mCastDialogClickReceiver = null;
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (rootView != null) {
            try {
                int version = Integer.parseInt(""
                        + Build.VERSION.SDK_INT);
                Utils.unbindDrawables(rootView, version);
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if (mPlayerService != null && mPlayerService.isAdPlaying())
            return;

        int viewId = view.getId();
        if (viewId == R.id.main_player_bar_text_container_handle
                || viewId == R.id.rlExpandHandle
                || viewId == R.id.main_player_bar_handle
                || viewId == R.id.main_player_content_info_bar_radio_inner) {
            isManualClicked = false;
            if (mDrawer.isPanelExpanded()) {
                mDrawer.unlock();
                mDrawer.collapsePanel();
            } else {
                mDrawer.expandPanel();
                userClickedTextButton = true;
            }

        } else if (viewId == R.id.main_player_bar_button_play
                || viewId == R.id.main_player_bar_button_play_handle
                || viewId == R.id.main_player_bar_button_play_radio
                || viewId == R.id.main_player_bar_button_play_radio_handle) {
            try {
                if (mPlayerService != null && !mPlayerService.isQueueEmpty()) {

                    if (mPlayerService.getState() == State.COMPLETED_QUEUE) {
                        mPlayerService.replay();

                    } else {
                        // normal play.
                        onPlayerPlayClicked(view.isSelected());
                    }
                    // updates the play button.
                    togglePlayerPlayIcon(view.isSelected());

                    if (mApplicationConfigurations.getSaveOfflineMode()) {
                        activity.sendBroadcast(new Intent(
                                PlayerBarFragment.ACTION_PLAY_STATE_CHANGED));
                    }
                }
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
        } else if (viewId == R.id.main_player_bar_button_previous) {
            if ((PlayerService.service != null && PlayerService.service
                    .getState() == State.INTIALIZED) || isPlayerLoading) {
                Utils.makeText(activity, "Please wait...", 0).show();
                return;
            }
            isNextPrevBtnClick = true;
            if (mPlayerService != null && !mPlayerService.isQueueEmpty()
                    && mPlayerService.hasPrevious()) {
                // set previous faked track.
                Track track = null;
                if (mApplicationConfigurations.getSaveOfflineMode()) {
                    int position = -1;
                    List<Track> trackList = getCurrentPlayingList();
                    int startPosition = getCurrentPlayingInQueuePosition();
                    for (int i = startPosition - 1; i >= 0; i--) {
                        Track trackTemp = trackList.get(i);
                        if (trackTemp != null) {
                            CacheState trackCacheState = DBOHandler
                                    .getTrackCacheState(
                                            activity.getApplicationContext(),
                                            "" + trackTemp.getId());
                            if (trackCacheState == CacheState.CACHED) {
                                position = i;
                                break;
                            }
                        }
                    }
                    if (position != -1) {
                        mPlayerService.playFromPositionNew(position);
                    } else if (mPlayerService != null && isPlaying()) {
                        mPlayerButtonPlay.performClick();
                        mPlayerButtonPlayRadio.performClick();
                        mPlayerButtonPlayRadioHandle.performClick();
                        mPlayerButtonPlayHandle.performClick();
                    }
                } else {
                    track = mPlayerService.fakePrevious();
                }
                if (track != null) {
                    try {
                        mDrawerButtonAddFavorites
                                .setClickable(true);
                        mPlayerButtonFavorites
                                .setClickable(true);
                        mPlayerButtonFavoritesHandle
                                .setClickable(true);
                        mActionCounter.performAction(ACTION_MESSAGE_PREVIOUS);
                    } catch (Exception e) {
                        Logger.e(getClass().getName() + ":1139", e.toString());
                    }
                }
            }

            Analytics
                    .logEvent(FlurryConstants.FlurryAllPlayer.PrevFromFullPlayer
                            .toString());

        } else if (viewId == R.id.main_player_bar_button_next
                || viewId == R.id.main_player_bar_button_next_handle) {

            if ((PlayerService.service != null && PlayerService.service
                    .getState() == State.INTIALIZED) || isPlayerLoading) {
                Utils.makeText(activity, "Please wait...", 0).show();
                return;
            }
            isNextPrevBtnClick = true;
            if (mPlayerService != null && !mPlayerService.isQueueEmpty()
                    && mPlayerService.hasNext()) {
                // set next faked track.
                Track track = null;// = mPlayerService.fakeNext();
                if (mApplicationConfigurations.getSaveOfflineMode()) {
                    int position = -1;
                    List<Track> trackList = getCurrentPlayingList();
                    int startPosition = getCurrentPlayingInQueuePosition();
                    for (int i = startPosition + 1; i < trackList.size(); i++) {
                        Track trackTemp = trackList.get(i);
                        if (trackTemp != null) {
                            CacheState trackCacheState = DBOHandler
                                    .getTrackCacheState(
                                            activity.getApplicationContext(),
                                            "" + trackTemp.getId());
                            if (trackCacheState == CacheState.CACHED) {
                                position = i;
                                break;
                            }
                        }
                    }
                    if (position != -1) {
                        mPlayerService.playFromPositionNew(position);
                    } else if (mPlayerService != null && isPlaying()) {
                        if (mDrawer.isPanelExpanded()) {
                            mPlayerButtonPlayHandle.performClick();
                        } else
                            mPlayerButtonPlay.performClick();
                    }
                } else {
                    track = mPlayerService.fakeNext();
                    if (track != null) {
                        mPlayerService.play();
                    }
                }
                mDrawerButtonAddFavorites
                        .setClickable(true);
                mPlayerButtonFavorites
                        .setClickable(true);
                mPlayerButtonFavoritesHandle
                        .setClickable(true);
            }

            if (mDrawer.isPanelExpanded()) {
                Analytics
                        .logEvent(FlurryConstants.FlurryAllPlayer.NextFromFullPlayer
                                .toString());
            } else {
                Analytics
                        .logEvent(FlurryConstants.FlurryAllPlayer.NextFromMiniPlayer
                                .toString());
            }

        } else if (viewId == R.id.main_player_bar_button_load) {
            // toggles the open / close state of the drawer.
            if (mDrawer.isPanelExpanded()) {
                mDrawer.collapsePanel();
            } else {
                mDrawer.expandPanel();
                userClickedLoadButton = true;
            }

        } else if (viewId == R.id.main_player_bar_button_loop) {
            try {
                Analytics.logEvent(FlurryConstants.FlurryAllPlayer.OnLoop
                        .toString());

                // gets the new state of the button.
                ThreeStatesActiveButton.State state = ((ThreeStatesActiveButton) view)
                        .getState();
                String toastMessage = null;

                if (state == ThreeStatesActiveButton.State.ACTIVE) {
                    // sets any loop mode - OFF.
                    toastMessage = Utils.getMultilanguageText(
                            mContext,
                            getResources().getString(
                                    R.string.player_loop_mode_off));
                    mPlayerService.setLoopMode(LoopMode.OFF);

                } else if (state == ThreeStatesActiveButton.State.SECOND) {
                    // sets any loop mode - REPLAY SONG.
                    toastMessage = Utils.getMultilanguageText(
                            mContext,
                            getResources().getString(
                                    R.string.player_loop_mode_replay_song));
                    mPlayerService.setLoopMode(LoopMode.REAPLAY_SONG);

                } else {
                    // sets any loop mode - ON.
                    toastMessage = getResources().getString(
                            R.string.player_loop_mode_on);
                    mPlayerService.setLoopMode(LoopMode.ON);
                }

                Utils.makeText(activity, toastMessage, Toast.LENGTH_SHORT)
                        .show();
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
        } else if (viewId == R.id.main_player_bar_button_shuffle) {
            try {
                Analytics.logEvent(FlurryConstants.FlurryAllPlayer.Shuffle
                        .toString());

                // gets the new state of the button.
                TwoStatesActiveButton.State state = ((TwoStatesActiveButton) view)
                        .getState();
                String toastMessage = null;

                if (state == TwoStatesActiveButton.State.ACTIVE) {
                    // sets any loop mode - OFF.
                    toastMessage = Utils.getMultilanguageText(
                            mContext,
                            getResources().getString(
                                    R.string.player_shuffle_mode_off));
                    mPlayerService.stopShuffle();

                } else if (state == TwoStatesActiveButton.State.SECOND) {
                    // sets any loop mode - REPLAY SONG.
                    toastMessage = Utils.getMultilanguageText(
                            mContext,
                            getResources().getString(
                                    R.string.player_shuffle_mode_on));
                    mPlayerService.startShuffle();

                } else {
                    // sets any loop mode - ON.
                    toastMessage = getResources().getString(
                            R.string.player_loop_mode_on);
                    mPlayerService.setLoopMode(LoopMode.ON);
                }
                refreshFlipImages();
                // shows a message to indicate the user.
                Utils.makeText(activity, toastMessage, Toast.LENGTH_SHORT)
                        .show();

                // updates the next / prev buttons to the new situation.
                updateNextPrevButtonsIfPlaying();

            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
        } else if (viewId == R.id.main_player_bar_button_queue
                || viewId == R.id.main_player_bar_button_queue_handle) {

            openQueue();

        } else if (viewId == R.id.main_player_content_actions_bar_button_share) {
            if (!isHandledActionOffline(ACTION_SHARE)) {
                if (mPlayerService != null) {
                    if (mDataManager.isDeviceOnLine()) {
                        try {
                            Track track = mPlayerService
                                    .getCurrentPlayingTrack();
                            // Prepare data for ShareDialogFragmnet
                            Map<String, Object> shareData = new HashMap<String, Object>();
                            shareData.put(ShareDialogFragment.TITLE_DATA,
                                    track.getTitle());
                            shareData.put(ShareDialogFragment.SUB_TITLE_DATA,
                                    track.getAlbumName());
                            shareData.put(ShareDialogFragment.THUMB_URL_DATA,
                                    ImagesManager.getMusicArtBigImageUrl(track
                                            .getImagesUrlArray()));
                            shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA,
                                    MediaType.TRACK);
                            shareData.put(ShareDialogFragment.CONTENT_ID_DATA,
                                    track.getId());

                            // Show ShareFragmentActivity
                            ShareDialogFragment shareDialogFragment = ShareDialogFragment
                                    .newInstance(
                                            shareData,
                                            FlurryConstants.FlurryShare.FullPlayer
                                                    .toString());
                            shareDialogFragment.show(mFragmentManager,
                                    ShareDialogFragment.FRAGMENT_TAG);
                        } catch (Exception e) {
                        }
                    } else {
                        Utils.makeText(
                                activity,
                                getResources().getString(
                                        R.string.player_error_no_connectivity),
                                Toast.LENGTH_LONG).show();
                    }

                }
            }
        } else if (viewId == R.id.main_player_content_actions_bar_button_playlist) {
            if (!isHandledActionOffline(ACTION_PLAYLIST)) {
                showAddToPlaylistDialog();
            }
        } else if (viewId == R.id.main_player_content_info_bar_button_view_settings
                || viewId == R.id.main_player_discovery_player_radio) {
            if (getPlayMode() == PlayMode.LIVE_STATION_RADIO
                    || getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                openRadioFullPlayerMoreOptions(view);
            } else {
                openSettings(view);
            }
        } else if (viewId == R.id.main_player_content_info_bar_button_comment) {
            if (!isHandledActionOffline(ACTION_COMMENT)) {
                if (mPlayerService != null) {
                    // gets the current playing track and shows the comments for
                    // it.
                    Track track = mPlayerService.getCurrentPlayingTrack();
                    if (track != null) {
                        MediaItem mediaItem = new MediaItem(track.getId(),
                                track.getTitle(), track.getAlbumName(),
                                track.getArtistName(), getImgUrl(track),
                                track.getBigImageUrl(), MediaType.TRACK.name()
                                .toLowerCase(), 0, 0,
                                track.getImages(), track.getAlbumId());
                        openCommentsPage(mediaItem);
                    }
                    closeContentWithoutCollapsedPanel();
                }
            }
        } else if (viewId == R.id.main_player_content_info_bar_button_favorite
                || viewId == R.id.main_player_bar_button_add_to_favorites
                || viewId == R.id.main_player_bar_button_add_to_favorites_handle) {
            if (!isHandledActionOffline(ACTION_FAVORITE)) {
                if (mDataManager.isDeviceOnLine()) {
                    try {
                        if (mPlayerService != null) {
                            view.setClickable(false);
                            Track track = mPlayerService
                                    .getCurrentPlayingTrack();
                            if (view.isSelected()) {
                                mDrawerButtonAddFavorites
                                        .setCompoundDrawablesWithIntrinsicBounds(
                                                null,
                                                getResources()
                                                        .getDrawable(
                                                                R.drawable.icon_media_details_fav_white),
                                                null, null);
                                mPlayerButtonFavorites
                                        .setImageDrawable(getResources()
                                                .getDrawable(
                                                        R.drawable.icon_main_player_favorites_white));
                                mPlayerButtonFavoritesHandle
                                        .setImageDrawable(getResources()
                                                .getDrawable(
                                                        R.drawable.icon_main_player_favorites_white));
                                if (mCurrentTrackDetails == null
                                        && track != null) {
                                    mCurrentTrackDetails = PlayerService.service
                                            .getCurrentPlayingTrack().details;
                                }
                                if (mCurrentTrackDetails != null) {
                                    mDrawerButtonAddFavorites.setText(Utils
                                            .roundTheCount(mCurrentTrackDetails
                                                    .getNumOfFav() - 1));
                                    mPlayerService.getCurrentPlayingTrack()
                                            .setFavorite(false);
                                    PlayerService.service
                                            .getCurrentPlayingTrack()
                                            .setFavorite(false);
                                }
                                if (getPlayMode() == PlayMode.TOP_ARTISTS_RADIO)
                                    mDataManager.removeFromFavorites(
                                            String.valueOf(artistRadioId),
                                            MEDIA_TYPE_ONDEMANRADIO,
                                            PlayerBarFragment.this);
                                else
                                    mDataManager.removeFromFavorites(
                                            String.valueOf(track.getId()),
                                            MEDIA_TYPE_SONG,
                                            PlayerBarFragment.this);
                            } else {
                                mDrawerButtonAddFavorites
                                        .setCompoundDrawablesWithIntrinsicBounds(
                                                null,
                                                getResources()
                                                        .getDrawable(
                                                                R.drawable.icon_media_details_fav_blue),
                                                null, null);
                                mPlayerButtonFavorites
                                        .setImageDrawable(getResources()
                                                .getDrawable(
                                                        R.drawable.icon_main_player_favorites_blue));
                                mPlayerButtonFavoritesHandle
                                        .setImageDrawable(getResources()
                                                .getDrawable(
                                                        R.drawable.icon_main_player_favorites_blue));
                                if (mCurrentTrackDetails == null
                                        && track != null) {
                                    mCurrentTrackDetails = PlayerService.service
                                            .getCurrentPlayingTrack().details;
                                }
                                if (mCurrentTrackDetails != null) {
                                    mDrawerButtonAddFavorites.setText(Utils
                                            .roundTheCount(mCurrentTrackDetails
                                                    .getNumOfFav() + 1));
                                    mPlayerService.getCurrentPlayingTrack()
                                            .setFavorite(true);
                                    PlayerService.service
                                            .getCurrentPlayingTrack()
                                            .setFavorite(true);
                                }
                                if (getPlayMode() == PlayMode.TOP_ARTISTS_RADIO)
                                    mDataManager.addToFavorites(
                                            String.valueOf(artistRadioId),
                                            MEDIA_TYPE_ONDEMANRADIO,
                                            PlayerBarFragment.this);
                                else
                                    mDataManager.addToFavorites(
                                            String.valueOf(track.getId()),
                                            MEDIA_TYPE_SONG,
                                            PlayerBarFragment.this);

                                Appirater appirater = new Appirater(mContext);
                                appirater.userDidSignificantEvent(true);
                            }

                            Map<String, String> reportMap = new HashMap<String, String>();
                            PlayMode pm = mPlayerService.getPlayMode();

                            reportMap.put(
                                    FlurryConstants.FlurryKeys.TitleContentID
                                            .toString(), mPlayerService
                                            .getCurrentPlayingTrack()
                                            .getTitle()
                                            + "_"
                                            + mPlayerService
                                            .getCurrentPlayingTrack()
                                            .getId());
                            reportMap.put(
                                    FlurryConstants.FlurryKeys.Type.toString(),
                                    pm.toString());

                            // Flurry report
                            if (viewId == R.id.main_player_content_info_bar_button_favorite) {
                                reportMap
                                        .put(FlurryConstants.FlurryKeys.Source
                                                        .toString(),
                                                FlurryConstants.FlurrySubSectionDescription.FullPlayer
                                                        .toString());
                            } else if (viewId == R.id.main_player_bar_button_add_to_favorites) {
                                reportMap
                                        .put(FlurryConstants.FlurryKeys.Source
                                                        .toString(),
                                                FlurryConstants.FlurrySubSectionDescription.MiniPlayer
                                                        .toString());
                            }

                            Analytics
                                    .logEvent(
                                            FlurryConstants.FlurryEventName.FavoriteButton
                                                    .toString(), reportMap);
                        }
                    } catch (Exception e) {
                        Logger.e(getClass().getName() + ":1345", e.toString());
                    }
                } else {
                    ((MainActivity) activity)
                            .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                                @Override
                                public void onRetryButtonClicked() {
                                    mPlayerButtonFavorites.performClick();
                                }
                            });
                }
            }
        } else if (viewId == R.id.player_gym_mode_exit_button) {
            closeGymMode();

        } else if (viewId == R.id.main_player_bar_button_settings) {
            openSettings(view);
        } else if (viewId == R.id.main_player_bar_button_audio_effects) {
            launchMusicFXGUI();
            ScreenLockStatus.getInstance(activity).dontShowAd();

        } else if (viewId == R.id.main_player_content_actions_bar_button_save_offline
                || viewId == R.id.main_player_content_actions_bar_rl_save_offline
                || viewId == R.id.main_player_bar_button_rl_save_offline_handle
                || viewId == R.id.main_player_bar_button_rl_save_offline) {
            Logger.i("Offline Tag", "view.getTag():" + view.getTag());

            View offline_handle = activity
                    .findViewById(R.id.main_player_bar_button_rl_save_offline_handle);
            Object tag = view.getTag();
            if (tag == null)
                tag = offline_handle.getTag();

            if (mPlayerService != null && (tag != null)) {
                if (!((Boolean) tag)) {
                    if (!isHandledActionOffline(ACTION_SAVE_OFFLINE)) {
                        Track track = mPlayerService.getCurrentPlayingTrack();
                        if (track != null) {
                            MediaItem mediaItem = new MediaItem(track.getId(),
                                    track.getTitle(), track.getAlbumName(),
                                    track.getArtistName(), getImgUrl(track),
                                    track.getBigImageUrl(), MediaType.TRACK
                                    .name().toLowerCase(), 0, 0,
                                    track.getImages(), track.getAlbumId());
                            CacheManager.saveOfflineAction(activity, mediaItem,
                                    track);
                            playerBarProgressCacheState.showProgressOnly(true);
                            playerBarProgressCacheStateHandle
                                    .showProgressOnly(true);
                            // HomeActivity.refreshOfflineState = true;
                            Utils.saveOfflineFlurryEvent(activity,
                                    FlurryConstants.FlurryCaching.FullPlayer
                                            .toString(), mediaItem);

                        }
                    }
                } else if ((Boolean) tag) {
                    Track track = mPlayerService.getCurrentPlayingTrack();
                    if (track != null) {
                        CacheManager.removePlayerBarTrackFromCache(activity,
                                track.getId(), this);
                    }
                }
            }
        } else if (viewId == R.id.main_player_content_actions_bar_button_header_info) {
            int buttonId = DRAWER_CONTENT_ACTION_BUTTON_ID_INFO;
            openConentFor(buttonId);
        }
    }

    private void downloadSong() {
        if (!isHandledActionOffline(ACTION_DOWNLOAD)) {
            if (mPlayerService != null) {
                try {
                    downloadCurrentTrack();
                } catch (Exception e) {
                    Logger.printStackTrace(e);
                }
            }
        }
    }

    @Override
    public void onActionCounterPerform(int actionId) {
        mPlayerService.play();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        // gets the reference to the service.

        PlayerSericeBinder binder = (PlayerSericeBinder) service;
        mPlayerService = binder.getService();
        mPlayerService.registerPlayerStateListener(this);
        mPlayerService.registerPlayerUpdateListeners(this);
        mPlayerService.setRadioBarUpdateListener(this);
        mPlayerService.setPlayerBarFragment(this);
        Logger.d(TAG, "Player bar connected to service.");
        if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            ((RelativeLayout) getView()
                    .findViewById(R.id.main_player_drawer_ad))
                    .setVisibility(View.GONE);
        }
        if (firstTimkeTracks != null && firstTimkeTracks.size() > 0) {
            helperPlayNowForFirstTime(firstTimkeTracks);
            firstTimkeTracks = null;
        }
        if (isFromCreate)
            setBlurImage();

        if (mPlayerService != null
                && mPlayerService.getPlayingQueue().size() > 0) {
            // if (isFromCreate)
            commonHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        adjustBarWhenClosedAndPlaying(true);
                    } catch (Exception e) {
                    }
                }
            }, 300);
        } else {
/*            if(mDrawer==null){
                getActivity().sendBroadcast(new Intent(MainActivity.ACTION_LANGUAGE_CHANGED));
                return;
            }*/
            adjustCurrentPlayerState();
        }
        if (isFromCreate) {
            startProgressUpdater();
            getDrawerPanelHeightFirstTime();
        }
        if (PlayerService.service != null && mDiscover != null)
            PlayerService.service.mDiscover = mDiscover;
        else if (PlayerService.service != null && mDiscover == null)
            mDiscover = PlayerService.service.mDiscover;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Logger.d(TAG, "Player bar disconnected from service.");
        try {
            mPlayerService.unregisterPlayerUpdateListeners(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPlayerService = null;
    }

    // ======================================================
    // Playing Events.
    // ======================================================

    /**
     * Load Track First Time
     *
     * @param track
     */
    public void LoadFirstTimeTrack(Track track) {
        try {
            if (track == null)
                return;
            Logger.i(TAG, "Starts loading track: " + track.getId());

            if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                // stops any running updater.
                stopProgressUpdater();
                stopLiveRadioUpdater();
                // clears fields.
                clearPlayer();
                clearActionButtons();
                // update the text.
                populateForFakeTrack(track);
                // sets the loading indicator.
                if (mDrawer.isPanelExpanded()) {
                    if (isVisible()) {
                        // mDrawerLoadingIndicator.setVisibility(View.VISIBLE);
                        // mDrawerLoadingIndicatorHandle
                        // .setVisibility(View.VISIBLE);
                    }
                } else {
                    mPlayerButtonLoad.setVisibility(View.GONE);
                    mPlayerButtonFavorites.setVisibility(View.VISIBLE);
                    mPlayerButtonQueue.setVisibility(View.VISIBLE);
                    mPlayerButtonFavoritesHandle.setVisibility(View.VISIBLE);
                    mPlayerButtonQueueHandle.setVisibility(View.VISIBLE);
                    mPlayerBarLayoutCacheStateHandle
                            .setVisibility(View.VISIBLE);
                    mPlayerBarLayoutCacheState.setVisibility(View.VISIBLE);
                }

                startLoadingMediaDetails(track);

            } else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    mPlayerBarLayoutCacheState.setVisibility(View.VISIBLE);
                    mPlayerBarLayoutCacheStateHandle
                            .setVisibility(View.VISIBLE);
                } else {
                    mPlayerBarLayoutCacheState.setVisibility(View.GONE);
                    mPlayerBarLayoutCacheStateHandle.setVisibility(View.GONE);
                }
                // update the text.
                populateForFakeTrack(track);

                if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                        || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    startLoadingMediaDetails(track);
                }
            }

            // disables the click on the play button.
            mPlayerButtonPlay.setClickable(true);
            mPlayerButtonPlayRadio.setClickable(true);
            mPlayerButtonPlayHandle.setClickable(true);
            mPlayerButtonPlayRadioHandle.setClickable(true);
            if (mPlayingEventListener != null) {
                mPlayingEventListener.onTrackLoad();
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    boolean isPlayerLoading = false;
    boolean isNextPrevBtnClick = false;

    /**
     * Update header visibility based on Live Radio/OnDemand Radio Player, Music
     * Player
     */
    private void setheaderVisibility() {
        if (mPlayerService.getPlayMode() == PlayMode.MUSIC
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC
                || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
            // sets the visibility.
            if (mDrawerInfoBar.getVisibility() != View.VISIBLE) {
                mDrawerInfoBar.setVisibility(View.VISIBLE);
                mDrawerInfoBarRadio.setVisibility(hideView);
            }
            if (mDrawerActionsBar.getVisibility() != View.VISIBLE
                    && mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                mDrawerActionsBar.setVisibility(View.VISIBLE);
            }
            if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                mDrawerButtonSettings.setVisibility(View.GONE);
            } else {
                mDrawerButtonSettings.setVisibility(View.VISIBLE);
            }
        } else {
            if (mDrawerInfoBarRadio.getVisibility() != View.VISIBLE) {
                mDrawerInfoBar.setVisibility(hideView);
                mDrawerInfoBarRadio.setVisibility(View.VISIBLE);
            }
            if (mDrawerActionsBar.getVisibility() == View.VISIBLE) {
                mDrawerActionsBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    /**
     * StartLoading is Callback method from PlayerService. It will call when
     * Player Start Buffering from URL
     */
    @Override
    public void onStartLoadingTrack(Track track) {
        try {
            //	blurbitmap = null;
            if(!mPlayerService.isPlaying())
                isPlayerLoading = true;
            mDrawerMediaArt.setVisibility(View.GONE);
            Logger.i("OnStartLoading Ads", "Starts playing Ad1:");
            if (track == null)
                return;

            Logger.i(TAG, "Starts loading track: " + track.getId());
            setheaderVisibility();
            if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                resetFlip();
                removeTitleHandler(true);
                removeRadioDetails();
                removeDiscoveryDetails();

                fancyCoverFlow.setVisibility(View.VISIBLE);
                fancyCoverFlow
                        .setOnItemSelectedListener(new OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {
                                resetFlip();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
                // stops any running updater.
                stopProgressUpdater();
                stopLiveRadioUpdater();
                // clears fields.
                clearPlayer();
                clearActionButtons();
                // update the text.
                populateForFakeTrack(track);
                // sets the loading indicator.
                if (mDrawer.isPanelExpanded()) {
                    if (isVisible()) {
                        // mDrawerLoadingIndicator.setVisibility(View.VISIBLE);
                        // mDrawerLoadingIndicatorHandle
                        // .setVisibility(View.VISIBLE);
                    }
                } else {
                    mPlayerButtonLoad.setVisibility(View.GONE);
                    mPlayerButtonFavorites.setVisibility(View.VISIBLE);
                    mPlayerButtonQueue.setVisibility(View.VISIBLE);
                    mPlayerButtonFavoritesHandle.setVisibility(View.VISIBLE);
                    mPlayerButtonQueueHandle.setVisibility(View.VISIBLE);
                    mPlayerBarLayoutCacheStateHandle
                            .setVisibility(View.VISIBLE);
                    mPlayerBarLayoutCacheState.setVisibility(View.VISIBLE);
                }
                startLoadingMediaDetails(track);
                refreshFlipImages();
            } else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                detail = null;
                fancyCoverFlow.setVisibility(View.INVISIBLE);
                mPlayerBarLayoutCacheStateHandle.setVisibility(View.GONE);
                mPlayerBarLayoutCacheState.setVisibility(View.GONE);
                // update the text.
                removeDiscoveryDetails();
                populateForFakeTrack(track);
                clearActionButtons();
                if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                        || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    startLoadingMediaDetails(track);
                }

                RadioDetailsFragment radioDetailsFragment = null;
                try {
                    radioDetailsFragment = (RadioDetailsFragment) mFragmentManager
                            .findFragmentByTag(RadioDetailsFragment.TAG);
                    if (radioDetailsFragment != null) {
                        radioDetailsFragment.setDefault();
                        MediaItem mediaItem = null;
                        MediaCategoryType mediaCategoryType = null;

                        Track currentTrack = mPlayerService
                                .getCurrentPlayingTrack();
                        mediaItem = (MediaItem) currentTrack.getTag();

                        if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                            mediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;

                        } else {
                            mediaCategoryType = MediaCategoryType.LIVE_STATIONS;
                        }

                        if (mediaItem != null) {
                            mediaItem
                                    .setMediaContentType(MediaContentType.RADIO);
                        }
                        Bundle arguments = new Bundle();
                        arguments.putSerializable(
                                RadioDetailsFragment.EXTRA_CATEGORY_TYPE,
                                (Serializable) mediaCategoryType);
                        arguments.putSerializable(
                                RadioDetailsFragment.EXTRA_MEDIA_ITEM,
                                (Serializable) mediaItem);
                        arguments.putBoolean(
                                RadioDetailsFragment.IS_FOR_PLAYER_BAR, true);

                        arguments.putBoolean(
                                RadioDetailsFragment.EXTRA_DO_SHOW_TITLE_BAR,
                                false);
                        arguments.putBoolean(
                                RadioDetailsFragment.IS_FOR_PLAYER_BAR, true);

                        radioDetailsFragment
                                .updateInfoDetails(arguments, false);
                    }
                } catch (Exception e) {

                }
                setRadioBlurImage(track);
            } else {
                removeRadioDetails();
                fancyCoverFlow.setVisibility(View.INVISIBLE);
                mPlayerBarLayoutCacheState.setVisibility(View.VISIBLE);
                mPlayerBarLayoutCacheStateHandle.setVisibility(View.VISIBLE);
                // update the text.
                populateForFakeTrack(track);
                clearActionButtons();
                if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    startLoadingMediaDetails(track);
                }
                mDrawerActionsBar.setVisibility(View.INVISIBLE);
                setRadioBlurImage(track);
                DiscoveryPlayDetailsFragment discoveryPlayFragment = null;
                try {
                    discoveryPlayFragment = (DiscoveryPlayDetailsFragment) mFragmentManager
                            .findFragmentByTag(DiscoveryPlayDetailsFragment.TAG);
                    if (discoveryPlayFragment != null) {
                        discoveryPlayFragment.setDefault();
                        discoveryPlayFragment.updateInfoDetails();
                    }
                } catch (Exception e) {
                }
            }

            // disables the click on the play button.
            mPlayerButtonPlay.setClickable(false);
            mPlayerButtonPlayRadio.setClickable(false);
            mPlayerButtonPlayRadioHandle.setClickable(false);
            mPlayerButtonPlayHandle.setClickable(false);
            if (mPlayingEventListener != null) {
                mPlayingEventListener.onTrackLoad();
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        playButtonImageUpdate();

        if (!mApplicationConfigurations.isFullPlayerDragHelp())
            openDrawerWithAction(0);
        if (mPlayerService != null && getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            ((View) playerBarProgressCacheState.getParent())
                    .setVisibility(View.VISIBLE);
        }
    }

    /**
     * TrackLoadingBufferUpdate is Callback method from PlayerService with Track
     * details and Percentage of buffering complete
     */
    @Override
    public void onTrackLoadingBufferUpdated(Track track, int precent) {
        // track is always null.
        mPlayerSeekBarProgress.setSecondaryProgress(precent);
        mPlayerSeekBarProgressHandle.setSecondaryProgress(precent);
    }

    /**
     * StartPlayingTrack is Callback method from PlayerService. It will call
     * when Player Start Playing
     */
    @Override
    public void onStartPlayingTrack(Track track) {
        Logger.i(TAG, "Starts playing track: " + track.getId());
        if (!isAdded())
            return;
        isNextPrevBtnClick = false;
        isPlayerLoading = false;
        // sets the play button icon and enables it.
        mPlayerButtonPlay.activate();
        mPlayerButtonPlay.setClickable(true);

        mPlayerButtonPlayRadio.activate();
        mPlayerButtonPlayRadio.setClickable(true);
        mPlayerButtonPlayRadioHandle.activate();
        mPlayerButtonPlayRadioHandle.setClickable(true);
        mPlayerButtonPlayHandle.activate();
        mPlayerButtonPlayHandle.setClickable(true);
        togglePlayerPlayIcon(true);

        mPlayerSeekBarProgress.setProgress(0);
        mPlayerSeekBarProgress.setSecondaryProgress(0);
        mPlayerSeekBarProgress.setEnabled(true);

        mPlayerSeekBarProgressHandle.setProgress(0);
        mPlayerSeekBarProgressHandle.setSecondaryProgress(0);
        mPlayerSeekBarProgressHandle.setEnabled(true);

        if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
            // starts running the playing progress.
            startProgressUpdater();

            mPlayerTextDuration.setText(Utils.secondsToString(mPlayerService
                    .getDuration() / 1000));

        } else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {

            // updates any observer with the next track.
            if (mPlayerService.getPlayMode() != PlayMode.DISCOVERY_MUSIC) {
                for (NextTrackUpdateListener listener : mNextTrackUpdateListeners) {
                    if (listener != null)
                        listener.onNextTrackUpdateListener(mPlayerService
                                .getNextTrack());
                }
            }
            if (getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                DiscoveryPlayDetailsFragment discoveryPlayFragment = null;
                try {
                    discoveryPlayFragment = (DiscoveryPlayDetailsFragment) mFragmentManager
                            .findFragmentByTag(DiscoveryPlayDetailsFragment.TAG);
                    if (discoveryPlayFragment != null) {
                        discoveryPlayFragment.setDefault();
                        discoveryPlayFragment.updateInfoDetails();
                    }
                } catch (Exception e) {
                }
                openTrendDialogForDiscvoeryOfTheDay();
            }
            setRadioBlurImage(track);
            // startLiveRadioUpdater(track.getTitle());
        }

        if (mPlayingEventListener != null) {
            mPlayingEventListener.onTrackPlay();
        }

        adjustCurrentPlayerState();
        refreshFlipImages();
        startTitleHandler();
        playButtonImageUpdate();
        restartTriviaTimer();
        startAdsHandler();
        // startAdsFlipTimer();
    }

    DiscoveruOfTheDayTrendCustomDialog dialog;

    private void openTrendDialogForDiscvoeryOfTheDay() {
        boolean needToShowTredDialogForTheSession = mApplicationConfigurations
                .needTrendDialogShowForTheSession();
        if (!needToShowTredDialogForTheSession)
            return;
        boolean needToShowTredDialog = mApplicationConfigurations
                .needTrendDialogShow();
        if (!needToShowTredDialog)
            return;
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (mDiscover != null
                        && !TextUtils.isEmpty(mDiscover.getHashTag())) {
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();
                    dialog = new DiscoveruOfTheDayTrendCustomDialog(activity,
                            mPlayerService.getCurrentPlayingTrack());
                    dialog.setCancelable(true);
                    dialog.getWindow().setBackgroundDrawable(
                            new ColorDrawable(
                                    Color.TRANSPARENT));
                    mApplicationConfigurations.setNeedTrendDialogShow(false);
                    dialog.show();
                }
            }
        });
    }

    /**
     * FinishPlayingTack is Callback method from PlayerService when any song
     * complete or user cancel song or Start next song
     */
    @Override
    public void onFinishPlayingTrack(Track track) {
        if (triviaDialog != null && triviaDialog.isShowing())
            triviaDialog.dismiss();

        if (lyricsDialog != null && lyricsDialog.isShowing())
            lyricsDialog.dismiss();

        isPlayerLoading = false;
        Logger.i(TAG, "Finished playing track: " + track.getId());
        // sclearBlurBg();
        if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
            stopProgressUpdater();
            // closeOpenedContent();
        }else if(mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO){
            RadioDetailsFragment radioDetailsFragment = null;
                radioDetailsFragment = (RadioDetailsFragment) mFragmentManager
                        .findFragmentByTag(RadioDetailsFragment.TAG);
                if (radioDetailsFragment != null) {
                    radioDetailsFragment.setDefault();
                }
        }
        togglePlayerPlayIcon(false);

        if (mPlayingEventListener != null) {
            mPlayingEventListener.onTrackFinish();
        }
        playButtonImageUpdate();
        stopAdsHandler();
        stopAdsFlipTimer();
        detail = null;
    }

    /**
     * Clear background Bur Image when song switched
     */
    private void clearBlurBg() {
        try {
            RelativeLayout rlFlipView = (RelativeLayout) activity
                    .findViewById(R.id.dragView);

            rlFlipView.setBackgroundColor(Color.BLACK);
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        Utils.clearCache();
    }

    /**
     * onFinishPlayingQueue will call from PlayerService when Player Queue list
     * complete to Play
     */
    @Override
    public void onFinishPlayingQueue() {
        if (triviaDialog != null && triviaDialog.isShowing())
            triviaDialog.dismiss();

        if (lyricsDialog != null && lyricsDialog.isShowing())
            lyricsDialog.dismiss();

        isPlayerLoading = false;
        Logger.i(TAG, "Done with the party, finished playing the queue.");
        // clearBlurBg();
        if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
            stopProgressUpdater();
            showReplayButtonAsPlay();
        }
        refreshFlipImages();
        removeTitleHandler(true);
        setDrawerPanelHeight();
        playButtonImageUpdate();
        /*if(mPlayerService!=null)
            mPlayerService.StopCastPlaying();*/
    }

    /**
     * It will Handle error in Playing song or buffering
     */
    @Override
    public void onErrorHappened(Error error) {
        Logger.i(TAG, "An Error occured while playing: " + error.toString());
        isPlayerLoading = false;
        if (error == Error.NO_CONNECTIVITY) {
            if (!HomeActivity.needToShowAirplaneDialog) {

                ((MainActivity) activity)
                        .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                            @Override
                            public void onRetryButtonClicked() {
                                mPlayerService.play();
                            }
                        });
            }
            //return;
        } else if (error == Error.DATA_ERROR || error == Error.SERVER_ERROR) {
        }

        mPlayerButtonPlay.setClickable(true);
        mPlayerButtonPlayRadio.setClickable(true);
        mPlayerButtonPlayRadioHandle.setClickable(true);
        mPlayerButtonPlayHandle.setClickable(true);
        if (error == Error.SERVER_ERROR) {
            mPlayerButtonNext.performClick();
            mPlayerButtonNextHandle.performClick();
        }
        playButtonImageUpdate();
    }

    @Override
    public void onSleepModePauseTrack(Track track) {
		/*
		 * If the player is visible, the music will be paused and this method
		 * will be invoked, updates the play button icon.
		 */
        togglePlayerPlayIcon(false);
    }

    // ======================================================
    // Initialization.
    // ======================================================
    boolean isManualClicked = false;
    boolean isHandlerRunning = false;
    int hideView = View.INVISIBLE;

    private void showHideHandlPart(boolean needToHide, float slideOffset) {

        if (!isManualClicked) {
            rlPlayerDrawerHeaderNew.setAlpha(slideOffset);
            rlPlayerBarHandle.setAlpha(1 - slideOffset);
        }
        Logger.i("slideOffset", "slideOffset:" + slideOffset);
        if (slideOffset == 1.0) {
            showHideHandlPart2(true);
        } else if (slideOffset == 0.0) {
            showHideHandlPart2(false);
            if (rlPlayerBarHandle.getVisibility() == hideView) {
                rlPlayerBarHandle.setVisibility(View.VISIBLE);
            }
        } else {
            if (isHandlerRunning)
                removeTitleHandler(false);
        }
    }

    private void showHideHandlPart2(boolean needToHide) {
        if (needToHide) {
            if (rlPlayerBarHandle.getVisibility() != hideView) {
                rlPlayerDrawerHeaderNew.setVisibility(View.VISIBLE);
                rlPlayerBarHandle.setVisibility(hideView);
                mDrawer.requestLayout();
            }
        } else {
            if (rlPlayerBarHandle.getVisibility() != View.VISIBLE) {
                rlPlayerDrawerHeaderNew.setVisibility(View.VISIBLE);
                rlPlayerBarHandle.setVisibility(View.VISIBLE);
                mDrawer.requestLayout();
            }
        }
    }

    private void initializeUserControls(View rootView) {
        try{
            rlPlayerDrawerHeaderNew = (RelativeLayout) miniPlayerView
                    .findViewById(R.id.main_player_drawer_header_new);
            rlPlayerBarHandle = (RelativeLayout) miniPlayerView
                    .findViewById(R.id.main_player_bar_handle);
            rlPlayerBarHandle.setOnClickListener(this);
            RelativeLayout rlRadioBarHandleInner = (RelativeLayout) miniPlayerView
                    .findViewById(R.id.main_player_content_info_bar_radio_inner);
            rlRadioBarHandleInner.setOnClickListener(this);
            showHideHandlPart(false, 0);
            initializePlayerBar(rootView);
            initializePlayerContent(rootView);
        }catch (Exception e){
        }
    }

    /**
     * Initialize Player Controls First Time
     *
     * @param rootView
     * - Parent View
     */
    View miniPlayerView;

    private void initializePlayerBar(View rootView) {

        // progress bar.
        mPlayerSeekBar = (RelativeLayout) rootView
                .findViewById(R.id.main_player_bar_progress_bar);
        mPlayerSeekBarProgress = (SeekBar) rootView
                .findViewById(R.id.main_player_bar_progress_bar_seek_bar);
        mPlayerTextCurrent = (TextView) rootView
                .findViewById(R.id.main_player_bar_progress_bar_scale_text_current);
        mPlayerTextDuration = (TextView) rootView
                .findViewById(R.id.main_player_bar_progress_bar_scale_text_length);

        mPlayerSeekBarHandle = (RelativeLayout) miniPlayerView
                .findViewById(R.id.main_player_bar_progress_bar_handle);
        mPlayerSeekBarProgressHandle = (SeekBar) miniPlayerView
                .findViewById(R.id.main_player_bar_progress_bar_seek_bar_handle);

        mPlayerTextTitleHandle = (TextView) miniPlayerView
                .findViewById(R.id.main_player_bar_text_title_handle);
        mPlayerTextTitleHandle.setSelected(true);
        viewVerticalLine = (View) miniPlayerView
                .findViewById(R.id.viewVerticalLine);

        // control buttons.
        mPlayerButtonPlay = (ActiveButton) rootView
                .findViewById(R.id.main_player_bar_button_play);
        mPlayerButtonPlayRadio = (ActiveButton) rootView
                .findViewById(R.id.main_player_bar_button_play_radio);
        mPlayerButtonPlayRadioHandle = (ActiveButton) miniPlayerView
                .findViewById(R.id.main_player_bar_button_play_radio_handle);

        mPlayerButtonPrevious = (ActiveButton) rootView
                .findViewById(R.id.main_player_bar_button_previous);
        mPlayerButtonNext = (ActiveButton) rootView
                .findViewById(R.id.main_player_bar_button_next);
        mPlayerButtonFavorites = (ActiveButton) rootView
                .findViewById(R.id.main_player_bar_button_add_to_favorites);
        mPlayerButtonQueue = (ImageView) rootView
                .findViewById(R.id.main_player_bar_button_queue);
        mPlayerButtonShuffle = (TwoStatesActiveButton) rootView
                .findViewById(R.id.main_player_bar_button_shuffle);
        mPlayerButtonLoop = (ThreeStatesActiveButton) rootView
                .findViewById(R.id.main_player_bar_button_loop);
        mPlayerButtonSettings = (ActiveButton) rootView
                .findViewById(R.id.main_player_bar_button_settings);
        mPlayerButtonLoad = (Button) rootView
                .findViewById(R.id.main_player_bar_button_load);
        mPlayerBarText = (LinearLayout) miniPlayerView
                .findViewById(R.id.main_player_bar_text_container_handle);

        mPlayerButtonEffects = (TwoStatesActiveButton) rootView
                .findViewById(R.id.main_player_bar_button_audio_effects);

        // Handle Control Buttons

        mPlayerButtonPlayHandle = (ActiveButton) miniPlayerView
                .findViewById(R.id.main_player_bar_button_play_handle);

        mPlayerButtonNextHandle = (ActiveButton) miniPlayerView
                .findViewById(R.id.main_player_bar_button_next_handle);
        mPlayerButtonFavoritesHandle = (ActiveButton) miniPlayerView
                .findViewById(R.id.main_player_bar_button_add_to_favorites_handle);
        mPlayerButtonQueueHandle = (ImageView) miniPlayerView
                .findViewById(R.id.main_player_bar_button_queue_handle);

        RelativeLayout rlExpandHandle = (RelativeLayout) miniPlayerView
                .findViewById(R.id.rlExpandHandle);
        rlExpandHandle.setOnClickListener(this);

        // this is 100%, from 0 to 99.
        mPlayerSeekBarProgress.setMax(99);
        mPlayerSeekBarProgress
                .setOnSeekBarChangeListener(PlayerBarFragment.this);
        mPlayerSeekBarProgressHandle.setMax(99);
        mPlayerSeekBarProgressHandle
                .setOnSeekBarChangeListener(PlayerBarFragment.this);

        mPlayerButtonPlay.setOnClickListener(this);
        mPlayerButtonPlayRadio.setOnClickListener(this);
        mPlayerButtonPlayRadioHandle.setOnClickListener(this);
        mPlayerButtonPlayHandle.setOnClickListener(this);
        mPlayerButtonPrevious.setOnClickListener(this);
        mPlayerButtonNext.setOnClickListener(this);
        mPlayerButtonFavorites.setOnClickListener(this);
        mPlayerButtonQueue.setOnClickListener(this);
        mPlayerButtonNextHandle.setOnClickListener(this);
        mPlayerButtonFavoritesHandle.setOnClickListener(this);
        mPlayerButtonQueueHandle.setOnClickListener(this);
        mPlayerButtonShuffle.setOnClickListener(this);
        mPlayerButtonLoop.setOnClickListener(this);
        mPlayerButtonSettings.setOnClickListener(this);
        mPlayerButtonLoad.setOnClickListener(this);
        mPlayerBarText.setOnClickListener(this);

        mPlayerButtonPlay.setSelected(false);
        mPlayerButtonPlayRadio.setSelected(false);
        mPlayerButtonPlayRadioHandle.setSelected(false);
        mPlayerButtonPlayHandle.setSelected(false);
        mPlayerButtonEffects.setOnClickListener(this);
        if (!isExistMusicFX()) {
            mPlayerButtonEffects.setVisibility(View.GONE);
        } else
            mPlayerButtonEffects.setState(fxbuttonstate);

		/*
		 * Sets a state listener to the loop button to reset the player service
		 * loop state when the button is inactive, not playing at all.
		 */
        mPlayerButtonLoop
                .setOnStateChangedListener(new OnStateChangedListener() {

                    @Override
                    public void onThirdState(View view) {
                    }

                    @Override
                    public void onSecondState(View view) {
                    }

                    @Override
                    public void onActiveState(View view) {
                    }

                    @Override
                    public void onInactiveState(View view) {
                        if (mPlayerService != null) {
                            mPlayerService.setLoopMode(LoopMode.OFF);
                        }
                    }
                });

        playerBarProgressCacheState = (CustomCacheStateProgressBar) miniPlayerView
                .findViewById(R.id.main_player_bar_progress_cache_state_handle);
        // playerBarProgressCacheState.setNotCachedStateVisibility(true);
        playerBarProgressCacheState.showProgressOnly(true);
        playerBarProgressCacheState.setCacheState(CacheState.NOT_CACHED);
        playerBarProgressCacheState.setProgress(0);

        playerBarProgressCacheStateHandle = (CustomCacheStateProgressBar) rootView
                .findViewById(R.id.main_player_bar_progress_cache_state);
        playerBarProgressCacheStateHandle.setNotCachedStateVisibility(true);
        // playerBarProgressCacheState.showProgressOnly(true);
        playerBarProgressCacheStateHandle.setCacheState(CacheState.NOT_CACHED);
        playerBarProgressCacheStateHandle.setProgress(0);

        mPlayerBarLayoutCacheState = (RelativeLayout) playerBarProgressCacheState
                .getParent();
        mPlayerBarLayoutCacheState.setOnClickListener(this);

    }

    CustomCacheStateProgressBar playerBarProgressCacheStateHandle;

    CoverFlow fancyCoverFlow;
    CoverFlowAdapterNew coverFlowAdapter;
    LockableScrollView coverflowScrollView;

    private void initializePlayerContent(View rootView) {

        mDrawer = (SlidingUpPanelLayout) activity
                .findViewById(R.id.sliding_layout);

        // Set True to enable child click
        mDrawer.setEnableDragViewTouchEvents(true);
        mDrawer.setPanelSlideListener(this);

		/*
		 * to open the drawer we handles it by clicking on the text.
		 */

		/*
		 * gets internal drawer's members that play role in the details of the
		 * playing status.
		 */
        mDrawerInfoBar = (RelativeLayout) miniPlayerView
                .findViewById(R.id.main_player_content_info_bar_content);
        mDrawerInfoBarRadio = (RelativeLayout) miniPlayerView
                .findViewById(R.id.main_player_content_info_bar_radio);

        mDrawerMediaArt = (ImageView) rootView
                .findViewById(R.id.main_player_content_media_art);

        mDrawerActionsBar = (LinearLayout) rootView
                .findViewById(R.id.main_player_content_actions);

        Display dis = activity.getWindowManager().getDefaultDisplay();
        int orgWidth = dis.getWidth();
        int spacing = -(orgWidth / 35);
        Logger.i("Spacing", "Spacing:" + spacing);
        width_coverFlow = (int) (orgWidth - (orgWidth / 4));

        fancyCoverFlow = (CoverFlow) rootView.findViewById(R.id.fancyCoverFlow);
        coverflowScrollView = (LockableScrollView) rootView
                .findViewById(R.id.coverflowScrollView);
        coverflowScrollView.setScrollingEnabled(true);

        coverFlowAdapter = new CoverFlowAdapterNew();
        // fancyCoverFlow.setAdapter(coverFlowAdapter);

        fancyCoverFlow.setAdapter(coverFlowAdapter);

        // fancyCoverFlow.setSpacing(spacing);
        fancyCoverFlow.setSpacing(spacing);
        fancyCoverFlow.setSelection(0, true);
        fancyCoverFlow.setMaxRotationAngle(30);
        fancyCoverFlow.setAnimationDuration(1000);
        //fancyCoverFlow.setRotation(90);
        // ImageView ivBack = (ImageView) findViewById(R.id.ivBlur);
        mDrawerInfoBarRadio.setVisibility(View.INVISIBLE);

        mDrawerActionsBar.setVisibility(View.INVISIBLE);

        mDrawerTextTitle = (TextView) miniPlayerView
                .findViewById(R.id.main_player_content_info_bar_text_title_handle);
        mDrawerTextTitleHandle = (TextView) miniPlayerView
                .findViewById(R.id.main_player_content_info_bar_text_title_handle);
        mDrawerTextTitleRadio = (TextView) miniPlayerView
                .findViewById(R.id.main_player_content_info_bar_text_title_handle_radio);
        mDrawerTextAdditionalHandleRadio = (TextView) miniPlayerView
                .findViewById(R.id.main_player_content_info_bar_text_additional_handle_radio);

        mDrawerTextAdditionalHandle = (TextView) miniPlayerView
                .findViewById(R.id.main_player_content_info_bar_text_additional_handle);

        mDrawerButtonSettings = (ImageView) miniPlayerView
                .findViewById(R.id.main_player_content_info_bar_button_view_settings);
        mDrawerButtonSettingsRadio = (ImageButton) miniPlayerView
                .findViewById(R.id.main_player_discovery_player_radio);
        mDrawerButtonAddFavorites = (Button) miniPlayerView
                .findViewById(R.id.main_player_content_info_bar_button_favorite);

        mDrawerButtonSettings.setOnClickListener(this);
        mDrawerButtonSettingsRadio.setOnClickListener(this);

        mDrawerButtonAddFavorites.setOnClickListener(this);

        // Action Buttons.
        mHeaderInfo = (ImageView) miniPlayerView
                .findViewById(R.id.main_player_content_actions_bar_button_header_info);
        mDrawerActionShare = (Button) rootView
                .findViewById(R.id.main_player_content_actions_bar_button_share);
        mDrawerActionTrivia = (TwoStatesButton) rootView
                .findViewById(R.id.main_player_content_actions_bar_button_trivia);
        if (isEnglish) {
            mDrawerActionMore = (Button) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_more);
            mDrawerActionInfo = (Button) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_info);
            mDrawerActionSimilar = (Button) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_similar);
            mDrawerActionAlbum = (Button) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_album);
            mDrawerActionVideo = (Button) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_video);
            mDrawerActionPlaylist = (Button) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_playlist);
            mDrawerActionMore.setText(Utils.getMultilanguageText(
                    mContext,
                    getResources().getString(
                            R.string.main_player_content_action_button_more)));
            // mDrawerActionInfo.setText(Utils.getMultilanguageText(mContext,
            // getResources().getString(R.string.player_shuffle_mode_on)));
            mDrawerActionSimilar
                    .setText(Utils
                            .getMultilanguageText(
                                    mContext,
                                    getResources()
                                            .getString(
                                                    R.string.main_player_content_action_button_similar)));
            mDrawerActionAlbum.setText(Utils.getMultilanguageText(
                    mContext,
                    getResources().getString(
                            R.string.main_player_content_action_button_album)));
            mDrawerActionVideo
                    .setText(Utils
                            .getMultilanguageText(
                                    mContext,
                                    getString(R.string.main_player_content_action_button_video)));
            mDrawerActionPlaylist.setText(Utils.getMultilanguageText(
                    mContext,
                    getResources().getString(
                            R.string.media_details_add_to_playlist)));
        } else {
            mDrawerActionMore = (LanguageButton) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_more);
            mDrawerActionInfo = (LanguageButton) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_info);
            mDrawerActionSimilar = (LanguageButton) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_similar);
            mDrawerActionAlbum = (LanguageButton) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_album);
            mDrawerActionVideo = (LanguageButton) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_video);
            mDrawerActionPlaylist = (LanguageButton) rootView
                    .findViewById(R.id.main_player_content_actions_bar_button_playlist);
        }
        mDrawerActionLyrics = (TwoStatesButton) rootView
                .findViewById(R.id.main_player_content_actions_bar_button_lyrics);
        mPlayerBarLayoutCacheStateHandle = (RelativeLayout) rootView
                .findViewById(R.id.main_player_bar_button_rl_save_offline);
        mPlayerBarLayoutCacheStateHandle.setVisibility(View.VISIBLE);
        // Clicks
        mHeaderInfo.setOnClickListener(this);
        mDrawerActionShare.setOnClickListener(this);
        mDrawerActionPlaylist.setOnClickListener(this);
        mPlayerBarLayoutCacheStateHandle.setOnClickListener(this);
        // default visibility - only share and playlist are visible by default.

        mHeaderInfo.setVisibility(View.VISIBLE);
        mDrawerActionShare.setVisibility(View.GONE);
        mDrawerActionPlaylist.setVisibility(View.VISIBLE);

        mDrawerActionTrivia.setVisibility(View.GONE);
        // mDrawerActionInfo.setVisibility(View.VISIBLE);
        mDrawerActionInfo.setVisibility(View.GONE);
        mDrawerActionSimilar.setVisibility(View.GONE);
        mDrawerActionAlbum.setVisibility(View.VISIBLE);
        mDrawerActionVideo.setVisibility(View.GONE);
        mDrawerActionLyrics.setVisibility(View.GONE);

        mDrawerActionTrivia.setTag(Integer
                .valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_TRIVIA));
        mDrawerActionInfo.setTag(Integer
                .valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_INFO));
        mDrawerActionAlbum.setTag(Integer
                .valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_ALBUM));
        mDrawerActionSimilar.setTag(Integer
                .valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_SIMILAR));

        mDrawerActionVideo.setTag(Integer
                .valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_VIDEO));
        mDrawerActionLyrics.setTag(Integer
                .valueOf(DRAWER_CONTENT_ACTION_BUTTON_ID_LYRICS));

        mDrawerActionMore.setOnClickListener(mDrawerActionsClickListener);

        mDrawerActionTrivia.setOnClickListener(mDrawerActionsClickListener);
        mDrawerActionSimilar.setOnClickListener(mDrawerActionsClickListener);
        mDrawerActionAlbum.setOnClickListener(mDrawerActionsClickListener);

        mDrawerActionInfo.setOnClickListener(mDrawerActionsClickListener);
        mDrawerActionVideo.setOnClickListener(mDrawerActionsClickListener);
        mDrawerActionLyrics.setOnClickListener(mDrawerActionsClickListener);

    }

    // ======================================================
    // State based methods and helper methods.
    // ======================================================

    /**
     * false = is not playing = shows the play icon. true = is playing shows the
     * pause icon.
     *
     * @param isSelected
     */
    private void togglePlayerPlayIcon(boolean isSelected) {
try{
        if (isSelected) {
            if (mApplicationConfigurations.getSaveOfflineMode()) {
                Track currentTrack = mPlayerService.getCurrentPlayingTrack();
                if (currentTrack == null) {
                    return;
                }
                CacheState cacheState = DBOHandler.getTrackCacheState(activity,
                        "" + currentTrack.getId());
                if (cacheState == CacheState.CACHED) {
                    mPlayerButtonPlay.setImageDrawable(mResources
                            .getDrawable(R.drawable.icon_pause_new));
                    mPlayerButtonPlay.setSelected(false);
                    mPlayerButtonPlayRadio.setImageDrawable(mResources
                            .getDrawable(R.drawable.icon_pause_new));
                    mPlayerButtonPlayRadio.setSelected(false);
                    mPlayerButtonPlayRadioHandle.setImageDrawable(mResources
                            .getDrawable(R.drawable.icon_pause_new));
                    mPlayerButtonPlayRadioHandle.setSelected(false);
                    mPlayerButtonPlayHandle.setImageDrawable(mResources
                            .getDrawable(R.drawable.icon_pause_new));
                    mPlayerButtonPlayHandle.setSelected(false);
                }
            } else {
                if (getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                    mPlayerButtonPlay
                            .setImageDrawable(mResources
                                    .getDrawable(R.drawable.icon_main_player_stop_white));
                    mPlayerButtonPlayRadio
                            .setImageDrawable(mResources
                                    .getDrawable(R.drawable.icon_main_player_stop_white));
                    mPlayerButtonPlayRadioHandle
                            .setImageDrawable(mResources
                                    .getDrawable(R.drawable.icon_main_player_stop_white));
                    mPlayerButtonPlayHandle
                            .setImageDrawable(mResources
                                    .getDrawable(R.drawable.icon_main_player_stop_white));
                } else {
                    mPlayerButtonPlay.setImageDrawable(mResources
                            .getDrawable(R.drawable.icon_pause_new));
                    mPlayerButtonPlayRadio.setImageDrawable(mResources
                            .getDrawable(R.drawable.icon_pause_new));
                    mPlayerButtonPlayRadioHandle.setImageDrawable(mResources
                            .getDrawable(R.drawable.icon_pause_new));

                    mPlayerButtonPlayHandle.setImageDrawable(mResources
                            .getDrawable(R.drawable.icon_pause_new));
                }
                mPlayerButtonPlay.setSelected(false);
                mPlayerButtonPlayRadio.setSelected(false);
                mPlayerButtonPlayRadioHandle.setSelected(false);
                mPlayerButtonPlayHandle.setSelected(false);
            }
        } else {
            mPlayerButtonPlay.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_play_new));
            mPlayerButtonPlay.setSelected(true);
            mPlayerButtonPlayRadio.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_play_new));
            mPlayerButtonPlayRadio.setSelected(true);
            mPlayerButtonPlayRadioHandle.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_play_new));
            mPlayerButtonPlayRadioHandle.setSelected(true);
            mPlayerButtonPlayHandle.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_play_new));
            mPlayerButtonPlayHandle.setSelected(true);

        }

        mPlayerButtonPlay.invalidate();
        mPlayerButtonPlayRadio.invalidate();
        mPlayerButtonPlayRadioHandle.invalidate();
        mPlayerButtonPlayHandle.invalidate();
}catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updatePlayerPlayIcon() {
        if (getPlayerState() == State.PAUSED) {
            mPlayerButtonPlay.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_play_new));
            mPlayerButtonPlay.setSelected(true);
            mPlayerButtonPlayRadio.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_play_new));
            mPlayerButtonPlayRadio.setSelected(true);
            mPlayerButtonPlayRadioHandle.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_play_new));
            mPlayerButtonPlayRadioHandle.setSelected(true);
            mPlayerButtonPlayHandle.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_play_new));
            mPlayerButtonPlayHandle.setSelected(true);
        } else {
            mPlayerButtonPlay.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_pause_new));
            mPlayerButtonPlay.setSelected(false);
            mPlayerButtonPlayRadio.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_pause_new));
            mPlayerButtonPlayRadio.setSelected(false);
            mPlayerButtonPlayRadioHandle.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_pause_new));
            mPlayerButtonPlayRadioHandle.setSelected(false);
            mPlayerButtonPlayHandle.setImageDrawable(mResources
                    .getDrawable(R.drawable.icon_pause_new));
            mPlayerButtonPlayHandle.setSelected(false);
        }
        mPlayerButtonPlay.invalidate();
        mPlayerButtonPlayRadio.invalidate();
        mPlayerButtonPlayRadioHandle.invalidate();
        mPlayerButtonPlayHandle.invalidate();
    }

    private void showReplayButtonAsPlay() {
        // show the replay icon
        mPlayerButtonPlay.setImageDrawable(mResources
                .getDrawable(R.drawable.icon_main_player_repeat_white));
        mPlayerButtonPlay.setSelected(true);
        mPlayerButtonPlayRadio.setImageDrawable(mResources
                .getDrawable(R.drawable.icon_main_player_repeat_white));
        mPlayerButtonPlayRadio.setSelected(true);
        mPlayerButtonPlayRadioHandle.setImageDrawable(mResources
                .getDrawable(R.drawable.icon_main_player_repeat_white));
        mPlayerButtonPlayRadioHandle.setSelected(true);
        mPlayerButtonPlayHandle.setImageDrawable(mResources
                .getDrawable(R.drawable.icon_main_player_repeat_white));
        mPlayerButtonPlayHandle.setSelected(true);

    }

    private void updateNextPrevButtonsIfPlaying() {
        if (mPlayerService.isPlaying() || mPlayerService.isLoading()) {
            if (mPlayerService.hasPrevious()) {
                mPlayerButtonPrevious.activate();
            } else {
                mPlayerButtonPrevious.deactivate();
            }

            if (mPlayerService.hasNext()) {
                mPlayerButtonNext.activate();
                mPlayerButtonNextHandle.activate();
            } else {
                mPlayerButtonNext.deactivate();
                mPlayerButtonNextHandle.deactivate();
            }

            activity.sendBroadcast(new Intent(
                    PlayerUpdateWidgetService.ACTION_PLAYER_QUEUE_UPDATED));
        }
    }

    private void clearPlayer() {
        Logger.i(TAG, "CLEAR PLAYER");
/*        if(mPlayerSeekBarProgress == null) {
            Logger.i(TAG, "CLEAR PLAYER :: " + (rootView==null));
            onAttach(getActivity());
            initializeUserControls(rootView);
        }*/
        mPlayerSeekBarProgress.setProgress(0);
        mPlayerSeekBarProgress.setSecondaryProgress(0);
        mPlayerTextCurrent
                .setText(mResources
                        .getString(R.string.main_player_bar_progress_bar_scale_text_current));
        mPlayerTextDuration
                .setText(mResources
                        .getString(R.string.main_player_bar_progress_bar_scale_text_length));

        mPlayerSeekBarProgressHandle.setProgress(0);
        mPlayerSeekBarProgressHandle.setSecondaryProgress(0);

        changeMiniPlayerTitleText(Utils.TEXT_EMPTY, Utils.TEXT_EMPTY);
        viewVerticalLine.setVisibility(View.INVISIBLE);

        togglePlayerPlayIcon(false);
    }

    /**
     * Populates the player bar's fields based on the given track that is not
     * even been loaded - while the user has clicked (prev / next) fast.
     *
     * @param track
     */
    private void populateForFakeTrack(Track track) {
        try {
            // checks if the player is still visible.
            if (isAdded()) {
                // titles.
                if (mDrawer.isPanelExpanded()) {
                    if (mCurrentTrackDetails != null
                            && mCurrentTrackDetails.getId() == track.getId()
                            && mCurrentTrackDetails.getIntl_content() == 1) {
                        changeMiniPlayerTitleText(track.getTitle(),
                                mCurrentTrackDetails.getSingers());
                    } else {
                        changeMiniPlayerTitleText(track.getTitle(),
                                track.getAlbumName());
                    }
                    mDrawerButtonAddFavorites.setText("");
                    cancelLoadingMediaDetails();
                    mCurrentTrackDetails = null;
                } else {
                    String additionalText = "";
                    if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {

                        String artistName = track.getArtistName();
                        additionalText = artistName;
                    } else if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                        additionalText = track.getAlbumName();
                    } else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                        SpannableStringBuilder sb = buildSemiColorString(
                                blueTitleLiveRadio,
                                "",
                                getResources().getColor(
                                        R.color.radio_blue_title_prefix),
                                getResources().getColor(R.color.white));
                        additionalText = sb.toString();
                    } else {
                        if (mCurrentTrackDetails != null
                                && mCurrentTrackDetails.getId() == track
                                .getId()
                                && mCurrentTrackDetails.getIntl_content() == 1) {
                            additionalText = mCurrentTrackDetails.getSingers();
                        } else {
                            additionalText = track.getAlbumName();
                        }
                    }
                    if (additionalText != null && !additionalText.equals("")) {
                        changeMiniPlayerTitleText(track.getTitle(),
                                additionalText);
                    }
                }

                // control buttons.
                if (mPlayerService.hasPrevious()) {
                    mPlayerButtonPrevious.activate();
                } else {
                    mPlayerButtonPrevious.deactivate();
                }

                if (mPlayerService.hasNext()) {
                    mPlayerButtonNext.activate();
                    mPlayerButtonNextHandle.activate();
                } else {
                    mPlayerButtonNext.deactivate();
                    mPlayerButtonNextHandle.deactivate();
                }

                playerBarProgressCacheState
                        .setCacheState(CacheState.NOT_CACHED);
                playerBarProgressCacheState.setProgress(0);

                playerBarProgressCacheStateHandle
                        .setCacheState(CacheState.NOT_CACHED);
                playerBarProgressCacheStateHandle.setProgress(0);

            }
        } catch (Exception e) {
        }
    }

    private SpannableStringBuilder buildSemiColorString(String prefixStr,
                                                        String suffixStr, int prefixColor, int suffixColor) {

        suffixStr = " " + suffixStr;
        int prefLen = prefixStr.length();
        int suffLen = suffixStr.length();

        final SpannableStringBuilder sb = new SpannableStringBuilder(prefixStr
                + suffixStr);
        final ForegroundColorSpan fcsPrefix = new ForegroundColorSpan(
                prefixColor);
        final ForegroundColorSpan fcsSuffix = new ForegroundColorSpan(
                suffixColor);

        try {
            // Set the text color for love word
            sb.setSpan(fcsPrefix, 0, prefLen,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(fcsSuffix, prefLen, prefLen + suffLen,
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        } catch (Exception e) {
            // TODO: handle exception
        }

        return sb;
    }

    private void startProgressUpdater() {
        Logger.i(TAG, "startProgressUpdater");
        if (mPlayerService != null)
            mPlayerService.startProgressUpdater();
    }

    private void stopProgressUpdater() {
        if (mPlayerService != null) {
            mPlayerService.stopProgressUpdater();
        }
    }

    private void onPlayerPlayClicked(boolean isSelected) {
        if ((isSelected || !(mPlayerService != null && mPlayerService
                .isPlaying()))) {
            if (mApplicationConfigurations.getSaveOfflineMode()) {
                Track currentTrack = mPlayerService.getCurrentPlayingTrack();
                if (currentTrack == null) {
                    return;
                }
                CacheState cacheState = DBOHandler.getTrackCacheState(activity,
                        "" + currentTrack.getId());
                if (cacheState == CacheState.CACHED) {
                    mPlayerService.play();
                } else {
                    if (!isHandledActionOffline(ACTION_PLAY)) {
                    }
                }
            } else
                mPlayerService.play();
        } else {
            mPlayerService.pause();
        }
    }

    private void downloadCurrentTrack() {
        Track track = mPlayerService.getCurrentPlayingTrack();
        if (track != null) {
            closeContentWithoutCollapsedPanel();
            // lunches the activity manages the track downloading.
            MediaItem trackMediaItem = new MediaItem(track.getId(),
                    track.getTitle(), track.getAlbumName(),
                    track.getArtistName(), getImgUrl(track),
                    track.getBigImageUrl(), MediaType.TRACK.toString(), 0, 0,
                    track.getImages(), track.getAlbumId());
            Intent intent = new Intent(mContext,
                    DownloadConnectingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(DownloadConnectingActivity.EXTRA_MEDIA_ITEM,
                    (Serializable) trackMediaItem);
            startActivity(intent);

            Map<String, String> reportMap = new HashMap<String, String>();
            reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
                    track.getTitle());
            reportMap.put(FlurryConstants.FlurryKeys.SourceSection.toString(),
                    FlurryConstants.FlurryKeys.Fullplayer.toString());
            Analytics.logEvent(
                    FlurryConstants.FlurryEventName.Download.toString(),
                    reportMap);
        }
    }

    // ======================================================
    // Seek bar callbacks.
    // ======================================================
    boolean isStartTracking = false;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (fromUser && !isStartTracking)
            seekBarChange(seekBar);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        isStartTracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        isStartTracking = false;
        seekBarChange(seekBar);
    }

    private void seekBarChange(SeekBar seekBar) {
        if (mPlayerService != null && mPlayerService.isAdPlaying())
            return;

        Logger.d(TAG, "Seek bar touched.");
        try {
			/*
			 * avoiding seeking when the user didn't finish selecting from where
			 * he wants to play.
			 */
            if (mPlayerService != null && mPlayerService.isPlaying()) {
                int timeMilliseconds = (mPlayerService.getDuration() / 100)
                        * seekBar.getProgress();
                mPlayerService.seekTo(timeMilliseconds);
                mPlayerTextDuration
                        .setText(Utils.secondsToString(mPlayerService
                                .getDuration() / 1000));

                // reports badges and coins for the given playing track.
                if (timeMilliseconds >= PlayerService.TIME_REPORT_BADGES_MILLIES)
                    mPlayerService.reportBadgesAndCoins();
            }
        } catch (Exception e) {
        }
    }

    // ======================================================
    // Drawer's callbacks.
    // ======================================================

    @Override
    public void onPanelExpanded(View panel) {
        isManualClicked = false;
        Logger.i("onPanelExpand", "onPanelExpand: 1");
        mPlayerSeekBarProgressHandle.setEnabled(false);
        Logger.i("onPanelExpand", "onPanelExpand: 2");
        // send broadcast to lock sidenavigation
        Intent new_intent = new Intent();
        new_intent.setAction(HomeActivity.ACTION_PLAYER_DRAWER_OPEN);
        new_intent.putExtra("isDrawerOpen", true);
        mContext.sendBroadcast(new_intent);
        String playModeType = "No type";
        isGymMode = false;

        if (mPlayerService != null) {
            PlayMode pm = mPlayerService.getPlayMode();
            Logger.s(" :::::::::::::::::::onDrawerOpened:::::::::::::::::: 1 "
                    + pm);
            // Setting the play mode Music/Radio
            if (pm == PlayMode.MUSIC) {
                playModeType = FlurryFullPlayerParams.Music.toString();
                fancyCoverFlow.setVisibility(View.VISIBLE);
            } else if (pm == PlayMode.LIVE_STATION_RADIO
                    || pm == PlayMode.TOP_ARTISTS_RADIO) {
                playModeType = FlurryFullPlayerParams.Radio.toString();
                fancyCoverFlow.setVisibility(View.INVISIBLE);
                RadioDetailsFragment radioDetailsFragment = (RadioDetailsFragment) mFragmentManager
                        .findFragmentByTag(RadioDetailsFragment.TAG);
                if (radioDetailsFragment != null) {
                    radioDetailsFragment.setDefault();
                }
            } else if (pm == PlayMode.DISCOVERY_MUSIC) {
                playModeType = FlurryFullPlayerParams.DiscoveryMusic.toString();
                fancyCoverFlow.setVisibility(View.INVISIBLE);
            }
        }
        Logger.i("onPanelExpand", "onPanelExpand: 4");

        Map<String, String> reportMap = new HashMap<String, String>();

        reportMap.put(FlurryFullPlayerParams.Type.toString(),
                playModeType.toString());
        // Set how the user opened the drawer
        if (userClickedLoadButton) {
            userClickedLoadButton = false;
            reportMap.put(FlurryFullPlayerParams.ActionDone
                    .toString(), FlurryFullPlayerParams.LoadButtonClicked
                    .toString());

        } else if (userClickedQueueButton) {
            userClickedQueueButton = false;
            reportMap.put(FlurryFullPlayerParams.ActionDone
                    .toString(), FlurryFullPlayerParams.QueueButtonClicked
                    .toString());
        } else if (userClickedTextButton) {
            userClickedTextButton = false;
            reportMap.put(FlurryFullPlayerParams.ActionDone
                    .toString(), FlurryFullPlayerParams.TextButtonClicked
                    .toString());
        } else {

            reportMap.put(FlurryFullPlayerParams.ActionDone
                    .toString(), FlurryFullPlayerParams.Drag.toString());
        }

        Analytics.logEvent(
                FlurryConstants.FlurryEventName.FullPlayer.toString(),
                reportMap);
        Logger.i("onPanelExpand", "onPanelExpand: 5");
        if (mPlayerService != null) {
            Logger.s(" :::::::::::::::::::onDrawerOpened:::::::::::::::::: 3 "
                    + mPlayerService.getState());
            Logger.i("onPanelExpand", "onPanelExpand: 6");
            if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
                // adjust player buttons for the playing mode.
                adjustBarWhenOpenedAndPlaying();
                Logger.i("onPanelExpand", "onPanelExpand: 7");
                // adjust drawer's content.
                adjustDrawerContentWhenPlaying();
                Logger.i("onPanelExpand", "onPanelExpand: 8");
                if (mApplicationConfigurations.isFirstVisitToFullPlayer()) {
                    mApplicationConfigurations
                            .setIsFirstVisitToFullPlayer(false);
                    // showPlayerQueueHint();
                } else if (mApplicationConfigurations.getHintsState()) {
                    if (!mApplicationConfigurations
                            .isPlayerQueueHintShownInThisSession()) {
                        mApplicationConfigurations
                                .setIsPlayerQueueHintShownInThisSession(true);
                    } else {
                    }
                } else {
                }
                Logger.i("onPanelExpand", "onPanelExpand: 9");
            } else if (mPlayerService.getState() == State.IDLE
                    && mPlayerService.getPlayingQueue().size() > 0) {
                // adjust player buttons for the playing mode.
                Logger.i("onPanelExpand", "onPanelExpand: 10");
                adjustBarWhenOpenedAndPlaying();
                Logger.i("onPanelExpand", "onPanelExpand: 11");
                // adjust drawer's content.
                adjustDrawerContentWhenPlaying();
                Logger.i("onPanelExpand", "onPanelExpand: 12");
            } else {
                // adjust player buttons for the not playing mode.
                adjustBarWhenOpenedAndNotPlaying();
                // adjust drawer's content.
                adjustDrawerContentWhenNotPlaying();
            }
        }

        Logger.i("onPanelExpand", "onPanelExpand: 13");
        if (mPlayerService != null && !mPlayerService.isAdPlaying()) {
            lastAdViewEventReportingTime = 0;
            startAdsHandler();
        }
        Logger.i("onPanelExpand", "onPanelExpand: 14");
        showFirstTimePlayerDragHelp();
        showHideCastIcon();
    }

    private void hideActionBar(final boolean needToHide) {

        try {
            if (needToHide)
                ((MainActivity) activity).getSupportActionBar().hide();
            else
                ((MainActivity) activity).getSupportActionBar().show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean collapsedPanel1() {
        if (mDrawer!=null && mDrawer.isPanelExpanded()) {
            mDrawer.collapsePanel();
            return true;
        }
        return false;
    }

    public boolean removeAllFragments() {
        boolean flag = false;
        Fragment lastOpenedFragment = mFragmentManager
                .findFragmentByTag(DRAWER_CONTENT_ACTION_BUTTON_FRAGMENT_TAG);

        if (lastOpenedFragment != null) {
            flag = true;
        }
        LinearLayout parent = (LinearLayout) mDrawerActionTrivia.getParent();
        View child = null;
        TwoStatesButton button = null;
        int count = 0;
        if (parent != null)
            count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            child = parent.getChildAt(i);

            if (child instanceof TwoStatesButton) {
                Logger.i("removeAllFragments", "removeAllFragments:" + i);
                button = ((TwoStatesButton) child);
                button.setUnselected();
                closeOpenedContent(true);
            }
        }
        needToUnlockDrawerAgain = true;

        return flag;
    }

    private void startAdsHandler() {
        if (!mDrawer.isPanelExpanded())
            return;

        final int adRefreshInterval = mApplicationConfigurations
                .getAppConfigPlayerOverlayStart();
        adhandler.removeCallbacks(refreshAd);
        if (getPlayMode() == PlayMode.MUSIC) {
            adhandler.postDelayed(refreshAd, adRefreshInterval * 1000);
            startTitleHandler();
        } else if (getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            startTitleHandler();
            DiscoveryPlayDetailsFragment discoveryPlayerFragment = (DiscoveryPlayDetailsFragment) mFragmentManager
                    .findFragmentByTag(DiscoveryPlayDetailsFragment.TAG);
            if (discoveryPlayerFragment != null) {
                discoveryPlayerFragment.startRefreshAdsHandler();
            }
        } else if (getPlayMode() == PlayMode.LIVE_STATION_RADIO
                || getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
            removeTitleHandler(true);
            RadioDetailsFragment radioDetailsFragment = (RadioDetailsFragment) mFragmentManager
                    .findFragmentByTag(RadioDetailsFragment.TAG);
            if (radioDetailsFragment != null) {
                radioDetailsFragment.startRefreshAdsHandler();
            }
        }
    }

    private void resetFlip() {
        if (isFlip) {
            isFlip = false;
            stopAdsFlipTimer();
            flipPos = -1;
            coverFlowAdapter.notifyDataSetChanged();
        }
    }

    private void stopAdsHandler() {
        if (getPlayMode() == PlayMode.MUSIC) {
            resetFlip();
        } else if (getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            DiscoveryPlayDetailsFragment discoveryPlayerFragment = (DiscoveryPlayDetailsFragment) mFragmentManager
                    .findFragmentByTag(DiscoveryPlayDetailsFragment.TAG);
            if (discoveryPlayerFragment != null) {
                if (discoveryPlayerFragment.isFlip) {
                    discoveryPlayerFragment.setDefault();
                }
                discoveryPlayerFragment.stopAdsFlipTimer();
                discoveryPlayerFragment.stoprefreshAdsHandle();
            }
        } else {
            RadioDetailsFragment radioDetailsFragment = (RadioDetailsFragment) mFragmentManager
                    .findFragmentByTag(RadioDetailsFragment.TAG);
            if (radioDetailsFragment != null) {
                if (radioDetailsFragment.isFlip) {
                    radioDetailsFragment.setDefault();
                }
                radioDetailsFragment.stopAdsFlipTimer();
                radioDetailsFragment.stoprefreshAdsHandle();
            }
        }
    }

    @Override
    public void onPanelCollapsed(View panel) {
        removeTitleHandler(true);
        stopAdsHandler();
        // send broadcast to lock sidenavigation
        if(getActivity()!=null && ((MainActivity)getActivity()).isDrawerIndicatorEnable()){
            Intent new_intent = new Intent();
            new_intent.setAction(HomeActivity.ACTION_PLAYER_DRAWER_OPEN);
            new_intent.putExtra("isDrawerOpen", false);
            mContext.sendBroadcast(new_intent);
        }


        isManualClicked = false;
        if(mPlayerSeekBarProgressHandle!=null)
            mPlayerSeekBarProgressHandle.setEnabled(true);

        if (mPlayerButtonEffects != null) {
            mPlayerButtonEffects.setState(fxbuttonstate);
        }

        try {
            if (mPlayerService != null
                    && (mPlayerService.isLoading()
                    || mPlayerService.isPlaying() || (mPlayerService
                    .getState() == State.IDLE && mPlayerService
                    .getPlayingQueue().size() > 0))) {
                // adjust player buttons for the playing mode.
                adjustBarWhenClosedAndPlaying(false);

            } else {
                // adjust player buttons for the not playing mode.
                adjustBarWhenClosedAndNotPlaying();
            }

            // removes content.
            if (mPlayerService != null
                    && (mPlayerService.getPlayMode() == PlayMode.MUSIC
                    || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || mPlayerService
                    .getPlayMode() == PlayMode.DISCOVERY_MUSIC)) {
                closeOpenedContent(true);
                mHeaderInfo.setVisibility(View.VISIBLE);
                if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO)
                    mDrawerButtonSettingsRadio.setVisibility(View.INVISIBLE);
                else
                    mDrawerButtonSettingsRadio.setVisibility(View.VISIBLE);
            } else if (mPlayerService != null
                    && (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO)) {
                mHeaderInfo.setVisibility(View.GONE);
                setheaderVisibility();
                if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC)
                    mDrawerButtonSettingsRadio.setVisibility(View.VISIBLE);
                else
                    mDrawerButtonSettingsRadio.setVisibility(View.INVISIBLE);
            }
        } catch (Exception e) {
            Logger.e(getClass().getName() + ":2322", e.toString());
        }
        hideActionBar(false);
        removeAllFragments();
    }

    // ======================================================
    // player bar and drawer adjustments methods.
    // ======================================================

    private void adjustCurrentPlayerState() {
        try {
            if (mPlayerService != null) {
                mPlayerService.registerPlayerStateListener(this);
                if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {
					/*
					 * adjusts the player bar and its content when playing.
					 */
                    Logger.d(TAG, "Resuming while playing.");
                    if (mDrawer.isPanelExpanded()) {
                        adjustBarWhenOpenedAndPlaying();
                        adjustDrawerContentWhenPlaying();
                    } else {
                        adjustBarWhenClosedAndPlaying(false);
                        clearDrawerContent();
                    }
                    startProgressUpdater();
                } else {
					/*
					 * Not playing and not bears. shows default buttons.
					 */
                    Logger.d(TAG, "Resuming while not playing.");
                    if (mDrawer.isPanelExpanded()) {
                        Logger.d(TAG, "Drawer is opened");
                        adjustBarWhenOpenedAndNotPlaying();
                        adjustDrawerContentWhenNotPlaying();
                    } else {
                        Logger.d(TAG, "Drawer is closed");
                        adjustBarWhenClosedAndNotPlaying();
                        clearDrawerContent();
                    }
                }
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    private void initializeBarForMusic() {
        stopLiveRadioUpdater();
        showLiveRadioPlayButton(false);
        mPlayerSeekBar.setVisibility(View.VISIBLE);
        mPlayerSeekBarHandle.setVisibility(View.VISIBLE);
        mPlayerButtonNext.setVisibility(View.VISIBLE);
        mPlayerButtonNextHandle.setVisibility(View.VISIBLE);
        mPlayerButtonPrevious.setVisibility(View.GONE);
        mPlayerButtonFavorites.setVisibility(View.GONE);

        if (getCurrentPlayingList() != null
                && getCurrentPlayingList().size() > 0) {
            mPlayerButtonQueue.setVisibility(View.VISIBLE);
            mPlayerButtonQueueHandle.setVisibility(View.VISIBLE);
        } else {
            mPlayerButtonQueue.setVisibility(View.GONE);
            mPlayerButtonQueueHandle.setVisibility(View.GONE);
        }
        mPlayerButtonShuffle.setVisibility(View.GONE);
        mPlayerButtonLoop.setVisibility(View.GONE);
        mPlayerButtonLoad.setVisibility(View.GONE);
        mPlayerButtonSettings.setVisibility(View.GONE);
        mPlayerButtonEffects.setVisibility(View.GONE);
        mPlayerBarLayoutCacheStateHandle.setVisibility(View.VISIBLE);
        mPlayerButtonPlayRadio.deactivate();
        mPlayerButtonPlayRadioHandle.deactivate();
        mPlayerButtonNext.deactivate();
        mPlayerButtonPlayHandle.deactivate();
        mPlayerButtonNextHandle.deactivate();
        List<Track> trackList = getCurrentPlayingList();
        if (trackList != null && trackList.size() > 0) {
            Track track = trackList.get(0);
            String additionalText = Utils.getMultilanguageTextLayOut(activity,
                    track.getAlbumName());
            changeMiniPlayerTitleText(track.getTitle(), additionalText);
            if (!(activity instanceof PlayerQueueActivity)
                    && !(activity instanceof VideoActivity))
                mDrawer.showPanel();
        } else {
            String notPlayingTxt = Utils
                    .getMultilanguageTextLayOut(
                            activity,
                            mResources
                                    .getString(R.string.main_player_bar_text_not_playing));
            changeMiniPlayerTitleText(notPlayingTxt, Utils.TEXT_EMPTY);
        }
        Logger.i("setDrawerPanelHeight", "setDrawerPanelHeight init");
        commonHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setDrawerPanelHeight();
            }
        }, 1000);

        // progress bar.
        mPlayerSeekBarProgress.setProgress(0);
        mPlayerSeekBarProgress.setSecondaryProgress(0);
        mPlayerSeekBarProgress.setEnabled(false);
        mPlayerSeekBarProgressHandle.setProgress(0);
        mPlayerSeekBarProgressHandle.setSecondaryProgress(0);
        mPlayerSeekBarProgressHandle.setEnabled(false);
    }

    private void showLiveRadioPlayButton(boolean needToShow) {
        if (needToShow) {
            mPlayerButtonPlayRadio.setVisibility(View.VISIBLE);
            mPlayerButtonPlayRadioHandle.setVisibility(View.VISIBLE);
            mPlayerButtonPlay.setVisibility(View.GONE);
            mPlayerButtonPlayHandle.setVisibility(View.GONE);
        } else {
            mPlayerButtonPlayRadio.setVisibility(View.GONE);
            mPlayerButtonPlayRadioHandle.setVisibility(View.GONE);
            mPlayerButtonPlay.setVisibility(View.VISIBLE);
            mPlayerButtonPlayHandle.setVisibility(View.VISIBLE);
        }
    }

    private void initializeBarForRadio() {
        mPlayerSeekBar.setVisibility(View.GONE);
        mPlayerSeekBarHandle.setVisibility(View.INVISIBLE);
        if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            mPlayerButtonNext.setVisibility(View.VISIBLE);
            mPlayerButtonNextHandle.setVisibility(View.VISIBLE);
            if (mPlayerService.hasNext()) {
                mPlayerButtonNext.activate();
                mPlayerButtonNextHandle.activate();
            } else {
                mPlayerButtonNext.deactivate();
                mPlayerButtonNextHandle.deactivate();
            }
        } else {
            mPlayerButtonNext.setVisibility(View.GONE);
            mPlayerButtonNextHandle.setVisibility(View.GONE);
        }
        mPlayerButtonPrevious.setVisibility(View.GONE);
        if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            mPlayerButtonFavorites.setVisibility(View.VISIBLE);
            mPlayerButtonFavoritesHandle.setVisibility(View.VISIBLE);
            showLiveRadioPlayButton(false);
        } else {
            mPlayerButtonFavorites.setVisibility(View.GONE);
            mPlayerButtonFavoritesHandle.setVisibility(View.GONE);
            showLiveRadioPlayButton(true);
        }
        if (getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            mPlayerBarLayoutCacheStateHandle.setVisibility(View.VISIBLE);
            mPlayerBarLayoutCacheState.setVisibility(View.VISIBLE);
        } else {
            mPlayerBarLayoutCacheStateHandle.setVisibility(View.GONE);
            mPlayerBarLayoutCacheState.setVisibility(View.GONE);
        }
        mPlayerButtonQueue.setVisibility(View.GONE);
        mPlayerButtonQueueHandle.setVisibility(View.GONE);
        mPlayerButtonShuffle.setVisibility(View.GONE);
        mPlayerButtonLoop.setVisibility(View.GONE);
        mPlayerButtonSettings.setVisibility(View.GONE);
        mPlayerButtonLoad.setVisibility(View.GONE);

        mPlayerButtonPlay.deactivate();
        mPlayerButtonPlayRadio.deactivate();
        mPlayerButtonPlayRadioHandle.deactivate();
        mPlayerButtonPlayHandle.deactivate();

        String notPlayingTxt = Utils
                .getMultilanguageTextLayOut(mContext, mResources
                        .getString(R.string.main_player_bar_text_not_playing));
        changeMiniPlayerTitleText(notPlayingTxt, Utils.TEXT_EMPTY);

        if (!(activity instanceof PlayerQueueActivity)
                && !(activity instanceof VideoActivity) && mDrawer.isPanelHidden())
            mDrawer.showPanel();

        viewVerticalLine.setVisibility(View.INVISIBLE);
        mPlayerSeekBarProgress.setProgress(0);
        mPlayerSeekBarProgress.setSecondaryProgress(0);
        mPlayerSeekBarProgressHandle.setProgress(0);
        mPlayerSeekBarProgressHandle.setSecondaryProgress(0);
    }

    private void adjustBarWhenClosedAndNotPlaying() {

        if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
            initializeBarForMusic();
        } else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {

            initializeBarForRadio();
        }
    }

    private void updateOfflineProgress() {
        Track currentTrack = mPlayerService.getCurrentPlayingTrack();
        if (currentTrack == null) {
            return;
        }
        setPlayerButtonFavorite();

        CacheState cacheState = DBOHandler.getTrackCacheState(mContext, ""
                + currentTrack.getId());
        if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
            playerBarProgressCacheState.setNotCachedStateVisibility(true);
            playerBarProgressCacheStateHandle.setNotCachedStateVisibility(true);

            if (cacheState == CacheState.CACHED) {
                mPlayerBarLayoutCacheState.setTag(true);
                mPlayerBarLayoutCacheStateHandle.setTag(true);
            } else if (cacheState == CacheState.QUEUED) {
                playerBarProgressCacheState.showProgressOnly(true);
                playerBarProgressCacheStateHandle.showProgressOnly(true);
                mPlayerBarLayoutCacheState.setTag(null);
                mPlayerBarLayoutCacheStateHandle.setTag(null);
                playerBarProgressCacheState.setProgress(DBOHandler
                        .getTrackCacheProgress(mContext,
                                "" + currentTrack.getId()));
                playerBarProgressCacheStateHandle.setProgress(DBOHandler
                        .getTrackCacheProgress(mContext,
                                "" + currentTrack.getId()));

            } else if (cacheState == CacheState.CACHING) {
                mPlayerBarLayoutCacheState.setTag(null);
                mPlayerBarLayoutCacheStateHandle.setTag(null);
                playerBarProgressCacheState.setProgress(DBOHandler
                        .getTrackCacheProgress(mContext,
                                "" + currentTrack.getId()));
                playerBarProgressCacheStateHandle.setProgress(DBOHandler
                        .getTrackCacheProgress(mContext,
                                "" + currentTrack.getId()));

            } else {
                playerBarProgressCacheState.showProgressOnly(false);
                playerBarProgressCacheStateHandle.showProgressOnly(false);
                mPlayerBarLayoutCacheState.setTag(false);
                mPlayerBarLayoutCacheStateHandle.setTag(false);
            }
            playerBarProgressCacheState.setCacheState(cacheState);
            playerBarProgressCacheStateHandle.setCacheState(cacheState);
        }
    }

    boolean isUpdated = false, isUpdatedFullScreen = false;

    private void adjustBarWhenClosedAndPlaying(boolean isFromServiceConnect) {
        if (mPlayerService != null
                && mPlayerService.getPlayMode() == PlayMode.MUSIC) {
            removeRadioDetails();
            removeDiscoveryDetails();
            mPlayerButtonPlayRadio.setVisibility(View.GONE);
            mPlayerButtonPlayRadioHandle.setVisibility(View.GONE);
            if (PlayerService.service != null
                    && PlayerService.service.getState() == State.INTIALIZED) {
                mPlayerButtonPlay.setVisibility(View.INVISIBLE);
                mPlayerButtonPlayHandle.setVisibility(View.INVISIBLE);
            } else {
                mPlayerButtonPlay.setVisibility(View.VISIBLE);
                mPlayerButtonPlayHandle.setVisibility(View.VISIBLE);
            }
            mPlayerButtonNext.setVisibility(View.VISIBLE);
            mPlayerButtonNextHandle.setVisibility(View.VISIBLE);
            mPlayerButtonQueue.setVisibility(View.VISIBLE);
            mPlayerButtonQueueHandle.setVisibility(View.VISIBLE);
            mPlayerButtonShuffle.setVisibility(View.GONE);
            mPlayerButtonLoop.setVisibility(View.GONE);
            mPlayerButtonSettings.setVisibility(View.GONE);
            mPlayerButtonEffects.setVisibility(View.GONE);
            mPlayerBarLayoutCacheStateHandle.setVisibility(View.VISIBLE);
            mPlayerBarLayoutCacheState.setVisibility(View.VISIBLE);

            // activate / deactivate buttons.
            mPlayerButtonPlay.activate();
            mPlayerButtonPlayRadio.activate();
            mPlayerButtonPlayRadioHandle.activate();
            mPlayerButtonPlayHandle.activate();

            if (mPlayerService.hasNext()) {
                mPlayerButtonNext.activate();
                mPlayerButtonNextHandle.activate();
            } else {
                mPlayerButtonNext.deactivate();
                mPlayerButtonNextHandle.deactivate();
            }
            // progress bar.
            mPlayerSeekBarProgress.setEnabled(true);
            mPlayerSeekBarProgress.setVisibility(View.VISIBLE);
            mPlayerSeekBarProgressHandle.setEnabled(true);
            mPlayerSeekBarProgressHandle.setVisibility(View.VISIBLE);
            mPlayerSeekBar.setVisibility(View.VISIBLE);
            mPlayerSeekBarHandle.setVisibility(View.VISIBLE);
            Track currentTrack = mPlayerService.getCurrentPlayingTrack();

            if (currentTrack == null) {
                return;
            }
            // update the text.
            if (mPlayerService.isAdPlaying()) {
                changeMiniPlayerTitleText(advertiseTxt, "");
                viewVerticalLine.setVisibility(View.INVISIBLE);
            } else {
                viewVerticalLine.setVisibility(View.VISIBLE);
                String albumName;
                if (mCurrentTrackDetails != null
                        && mCurrentTrackDetails.getId() == currentTrack.getId()
                        && mCurrentTrackDetails.getIntl_content() == 1) {
                    albumName = Utils.getMultilanguageTextLayOut(mContext,
                            mCurrentTrackDetails.getSingers());

                } else {
                    albumName = Utils.getMultilanguageTextLayOut(mContext,
                            currentTrack.getAlbumName());
                }
                changeMiniPlayerTitleText(currentTrack.getTitle(), albumName);
                viewVerticalLine.setVisibility(View.VISIBLE);
            }

            // sets the play button.
            if (mPlayerService.getState() == State.PLAYING) {
                togglePlayerPlayIcon(true);
                // sets the duration of the track.
                mPlayerTextDuration.setText(Utils
                        .secondsToString(mPlayerService.getDuration() / 1000));

            } else if (mPlayerService.getState() == State.PAUSED) {
                togglePlayerPlayIcon(false);
                // sets the current position in the progress.
                int progress = (int) (((float) mPlayerService
                        .getCurrentPlayerPosition() / mPlayerService
                        .getDuration()) * 100);
                mPlayerSeekBarProgress.setProgress(progress);
                mPlayerTextDuration.setText(Utils
                        .secondsToString(mPlayerService.getDuration() / 1000));

                mPlayerSeekBarProgressHandle.setProgress(progress);
            } else if (mPlayerService.getState() == State.COMPLETED_QUEUE) {
                showReplayButtonAsPlay();
            }
            updateOfflineProgress();
            refreshFlipImages();
        } else if (mPlayerService != null
                && mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {

            Track currentTrack = mPlayerService.getCurrentPlayingTrack();
            String title, additionalTitle = "";
            if (mPlayerService.isAdPlaying()) {
                title = advertiseTxt;
                additionalTitle = "";
                viewVerticalLine.setVisibility(View.INVISIBLE);
                changeMiniPlayerTitleText(title, additionalTitle);
            } else {
                title = currentTrack.getTitle();
                additionalTitle = currentTrack.getAlbumName();
            }

            if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                if (!mPlayerService.isAdPlaying()) {
                    additionalTitle = currentTrack.getArtistName();
                }
            } else if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                if (!mPlayerService.isAdPlaying()) {
                    additionalTitle = currentTrack.getAlbumName();
                }
            } else {

                if (detail == null) {
                    SpannableStringBuilder sb = buildSemiColorString(
                            blueTitleLiveRadio,
                            "",
                            getResources().getColor(
                                    R.color.radio_blue_title_prefix),
                            getResources().getColor(R.color.white));
                    additionalTitle = Utils.getMultilanguageTextLayOut(
                            mContext, sb.toString());
                } else {
                    title = Utils.getMultilanguageTextLayOut(mContext,
                            detail.getTrack());
                    additionalTitle = Utils.getMultilanguageTextLayOut(
                            mContext, detail.getAlbum());
                }
                viewVerticalLine.setVisibility(View.VISIBLE);
            }

            if (!mPlayerService.isAdPlaying()) {
                if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                    Track track = mPlayerService.getCurrentPlayingTrack();
                    MediaItem mediaItem = (MediaItem) track.getTag();

                    if (mediaItem != null) {
                        String title1 = mediaItem.getAlbumName();
                        if (title1 != null) {
                            title = Utils.getMultilanguageTextLayOut(mContext,
                                    title1);
                        }
                    }
                } else if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    title = currentTrack.getTitle();
                } else {
                    if (title == null || (title != null && title.equals("")))
                        title = blueTitleTopOnDemandRadio;
                }
            }
            changeMiniPlayerTitleText(title, additionalTitle);

            if ((mPlayerService.isLoading() || mPlayerService.isPlaying())
                    && mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {

                CacheState cacheState = DBOHandler.getTrackCacheState(mContext,
                        "" + currentTrack.getId());
                playerBarProgressCacheState.setNotCachedStateVisibility(true);
                playerBarProgressCacheStateHandle
                        .setNotCachedStateVisibility(true);
                if (cacheState == CacheState.CACHED) {
                    mPlayerBarLayoutCacheState.setTag(true);
                    mPlayerBarLayoutCacheStateHandle.setTag(true);
                } else if (cacheState == CacheState.QUEUED) {
                    playerBarProgressCacheState.showProgressOnly(true);
                    playerBarProgressCacheStateHandle.showProgressOnly(true);
                    mPlayerBarLayoutCacheState.setTag(null);
                    mPlayerBarLayoutCacheStateHandle.setTag(null);
                    playerBarProgressCacheState.setProgress(DBOHandler
                            .getTrackCacheProgress(mContext,
                                    "" + currentTrack.getId()));
                    playerBarProgressCacheStateHandle.setProgress(DBOHandler
                            .getTrackCacheProgress(mContext,
                                    "" + currentTrack.getId()));

                } else if (cacheState == CacheState.CACHING) {
                    mPlayerBarLayoutCacheState.setTag(null);
                    mPlayerBarLayoutCacheStateHandle.setTag(null);
                    playerBarProgressCacheState.setProgress(DBOHandler
                            .getTrackCacheProgress(mContext,
                                    "" + currentTrack.getId()));
                    playerBarProgressCacheStateHandle.setProgress(DBOHandler
                            .getTrackCacheProgress(mContext,
                                    "" + currentTrack.getId()));

                } else {
                    playerBarProgressCacheState.showProgressOnly(false);
                    playerBarProgressCacheStateHandle.showProgressOnly(false);
                    mPlayerBarLayoutCacheState.setTag(false);
                    mPlayerBarLayoutCacheStateHandle.setTag(false);
                }
                playerBarProgressCacheState.setCacheState(cacheState);
                playerBarProgressCacheStateHandle.setCacheState(cacheState);
            }

            if (isUpdated)
                return;
            else
                isUpdated = true;

            mPlayerSeekBar.setVisibility(View.GONE);
            mPlayerSeekBarHandle.setVisibility(View.INVISIBLE);

            if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                mPlayerButtonNext.setVisibility(View.VISIBLE);
                mPlayerButtonNextHandle.setVisibility(View.VISIBLE);
                if (mPlayerService.hasNext()) {
                    mPlayerButtonNext.activate();
                    mPlayerButtonNextHandle.activate();
                } else {
                    mPlayerButtonNext.deactivate();
                    mPlayerButtonNextHandle.deactivate();
                }
            } else {
                mPlayerButtonNext.setVisibility(View.GONE);
                mPlayerButtonNextHandle.setVisibility(View.GONE);
            }
            mPlayerButtonPrevious.setVisibility(View.GONE);
            mPlayerButtonQueue.setVisibility(View.GONE);
            mPlayerButtonQueueHandle.setVisibility(View.GONE);
            mPlayerButtonShuffle.setVisibility(View.GONE);
            mPlayerButtonLoop.setVisibility(View.GONE);
            mPlayerButtonSettings.setVisibility(View.GONE);
            mPlayerButtonLoad.setVisibility(View.GONE);
            if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                mPlayerBarLayoutCacheState.setVisibility(View.VISIBLE);
                mPlayerBarLayoutCacheStateHandle.setVisibility(View.VISIBLE);
            } else {
                mPlayerBarLayoutCacheState.setVisibility(View.GONE);
                mPlayerBarLayoutCacheStateHandle.setVisibility(View.GONE);
            }
            // favorite button.
            if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                mPlayerButtonPlayRadio.setVisibility(View.GONE);
                mPlayerButtonPlayRadioHandle.setVisibility(View.GONE);
                mPlayerButtonFavorites.setVisibility(View.VISIBLE);
                mPlayerButtonFavoritesHandle.setVisibility(View.VISIBLE);
                if (PlayerService.service != null
                        && PlayerService.service.getState() == State.INTIALIZED) {
                    mPlayerButtonPlay.setVisibility(View.INVISIBLE);
                    mPlayerButtonPlayHandle.setVisibility(View.INVISIBLE);
                } else {
                    mPlayerButtonPlay.setVisibility(View.VISIBLE);
                    mPlayerButtonPlayHandle.setVisibility(View.VISIBLE);
                }
            } else {
                if (PlayerService.service != null
                        && PlayerService.service.getState() == State.INTIALIZED) {
                    mPlayerButtonPlayRadio.setVisibility(View.INVISIBLE);
                    mPlayerButtonPlayRadioHandle.setVisibility(View.INVISIBLE);
                } else {
                    mPlayerButtonPlayRadio.setVisibility(View.VISIBLE);
                    mPlayerButtonPlayRadioHandle.setVisibility(View.VISIBLE);
                }
                mPlayerButtonPlay.setVisibility(View.GONE);
                mPlayerButtonPlayHandle.setVisibility(View.GONE);
                mPlayerButtonFavorites.setVisibility(View.GONE);
                mPlayerButtonFavoritesHandle.setVisibility(View.GONE);
            }

            mPlayerButtonPlay.activate();
            mPlayerButtonPlayRadio.activate();
            mPlayerButtonPlayRadioHandle.activate();
            mPlayerButtonPlayHandle.activate();

            if (mPlayerService.getState() == State.PLAYING) {
                togglePlayerPlayIcon(true);
            } else if (mPlayerService.getState() == State.PAUSED) {
                togglePlayerPlayIcon(false);
            }

            if (getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                setPlayerButtonFavorite();
            }
        }
        setFullPlayerBottomHeight();
    }

    private void adjustBarWhenOpenedAndNotPlaying() {
        try {
            setFullPlayerBottomHeight();
            if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                // adjust player bar buttons.
                mPlayerButtonPlayRadio.setVisibility(View.GONE);
                mPlayerButtonPlayRadioHandle.setVisibility(View.GONE);
                if (PlayerService.service != null
                        && PlayerService.service.getState() == State.INTIALIZED) {
                    mPlayerButtonPlay.setVisibility(View.INVISIBLE);
                    mPlayerButtonPlayHandle.setVisibility(View.INVISIBLE);
                } else {
                    mPlayerButtonPlay.setVisibility(View.VISIBLE);
                    mPlayerButtonPlayHandle.setVisibility(View.VISIBLE);
                }

                mPlayerSeekBar.setVisibility(View.VISIBLE);
                mPlayerSeekBarHandle.setVisibility(View.VISIBLE);

                mPlayerButtonNext.setVisibility(View.VISIBLE);
                mPlayerButtonNextHandle.setVisibility(View.VISIBLE);
                mPlayerButtonPrevious.setVisibility(View.VISIBLE);
                mPlayerButtonFavorites.setVisibility(View.GONE);
                mPlayerButtonQueue.setVisibility(View.GONE);
                mPlayerButtonFavoritesHandle.setVisibility(View.GONE);
                mPlayerButtonQueueHandle.setVisibility(View.GONE);
                mPlayerButtonShuffle.setVisibility(View.GONE);
                mPlayerButtonLoop.setVisibility(View.GONE);
                mPlayerButtonSettings.setVisibility(View.GONE);
                mPlayerButtonLoad.setVisibility(View.GONE);
                if (isExistMusicFX()) {
                    mPlayerButtonEffects.setVisibility(View.GONE);
                    mPlayerButtonEffects.setState(fxbuttonstate);
                }
                clearPlayer();
                // mPlayerButtonPlay.deactivate();
                mPlayerButtonPlayRadio.deactivate();
                mPlayerButtonPlayRadioHandle.deactivate();
                mPlayerButtonNext.deactivate();
                mPlayerButtonPlayHandle.deactivate();
                mPlayerButtonNextHandle.deactivate();
                mPlayerButtonPrevious.deactivate();

                mPlayerButtonShuffle.deactivate();
                mPlayerButtonLoop.deactivate();
                mPlayerButtonSettings.deactivate();

                mPlayerSeekBarProgress.setEnabled(false);
                mPlayerSeekBarProgressHandle.setEnabled(false);

            } else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                    || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {

                // mPlayerSeekBar.setVisibility(View.INVISIBLE);
                mPlayerSeekBar.setVisibility(View.GONE);
                mPlayerSeekBarHandle.setVisibility(View.INVISIBLE);
                if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                        || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    mPlayerButtonNext.setVisibility(View.VISIBLE);
                    mPlayerButtonNextHandle.setVisibility(View.VISIBLE);
                    if (mPlayerService.hasNext()) {
                        mPlayerButtonNext.activate();
                        mPlayerButtonNextHandle.activate();
                    } else {
                        mPlayerButtonNext.deactivate();
                        mPlayerButtonNextHandle.deactivate();
                    }
                } else {
                    mPlayerButtonNext.setVisibility(View.GONE);
                    mPlayerButtonNextHandle.setVisibility(View.GONE);
                }
                mPlayerButtonPrevious.setVisibility(View.GONE);
                mPlayerButtonFavorites.setVisibility(View.GONE);
                mPlayerButtonFavoritesHandle.setVisibility(View.GONE);
                mPlayerButtonShuffle.setVisibility(View.GONE);
                mPlayerButtonLoop.setVisibility(View.GONE);
                mPlayerButtonSettings.setVisibility(View.GONE);
                mPlayerButtonLoad.setVisibility(View.GONE);

                if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                        || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    mPlayerButtonPlayRadio.setVisibility(View.GONE);
                    mPlayerButtonPlayRadioHandle.setVisibility(View.GONE);
                    mPlayerButtonPlay.setVisibility(View.VISIBLE);
                    mPlayerButtonPlayHandle.setVisibility(View.VISIBLE);
                    mPlayerButtonFavorites.setVisibility(View.VISIBLE);
                    mPlayerButtonFavoritesHandle.setVisibility(View.VISIBLE);
                } else {
                    mPlayerButtonPlayRadio.setVisibility(View.VISIBLE);
                    mPlayerButtonPlayRadioHandle.setVisibility(View.VISIBLE);
                    mPlayerButtonPlay.setVisibility(View.GONE);
                    mPlayerButtonPlayHandle.setVisibility(View.GONE);
                    mPlayerButtonFavorites.setVisibility(View.GONE);
                    mPlayerButtonFavoritesHandle.setVisibility(View.GONE);
                }

                mPlayerButtonPlay.activate();
                mPlayerButtonPlayRadio.activate();
                mPlayerButtonPlayRadioHandle.activate();
                mPlayerButtonPlayHandle.activate();
                try {
                    // update the text.
                    Track currentTrack = mPlayerService
                            .getCurrentPlayingTrack();
                    String additionalTitle;
                    if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {

                        String artistName = currentTrack.getArtistName();
                        additionalTitle = Utils.getMultilanguageTextLayOut(
                                mContext, artistName);
                        viewVerticalLine.setVisibility(View.VISIBLE);
                    } else if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                        additionalTitle = currentTrack.getAlbumName();
                        viewVerticalLine.setVisibility(View.VISIBLE);
                    } else {
                        SpannableStringBuilder sb = buildSemiColorString(
                                blueTitleLiveRadio,
                                "",
                                getResources().getColor(
                                        R.color.radio_blue_title_prefix),
                                getResources().getColor(R.color.white));
                        additionalTitle = Utils.getMultilanguageTextLayOut(
                                mContext, sb.toString());
                        viewVerticalLine.setVisibility(View.VISIBLE);
                    }
                    changeMiniPlayerTitleText(currentTrack.getTitle(),
                            additionalTitle);
                } catch (Exception e) {
                    Logger.printStackTrace(e);
                }
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    private void adjustBarWhenOpenedAndPlaying() {
        if (mPlayerService != null
                && mPlayerService.getPlayMode() == PlayMode.MUSIC) {
            try {
                removeRadioDetails();
                removeDiscoveryDetails();
                // adjust player bar buttons.
                mPlayerButtonPlayRadio.setVisibility(View.GONE);
                mPlayerButtonPlayRadioHandle.setVisibility(View.GONE);
                if (PlayerService.service != null
                        && PlayerService.service.getState() == State.INTIALIZED)
                    mPlayerButtonPlay.setVisibility(View.INVISIBLE);
                else
                    mPlayerButtonPlay.setVisibility(View.VISIBLE);

                mPlayerSeekBar.setVisibility(View.VISIBLE);
                mPlayerSeekBarHandle.setVisibility(View.VISIBLE);
                mPlayerButtonNext.setVisibility(View.VISIBLE);
                mPlayerButtonNextHandle.setVisibility(View.VISIBLE);
                mPlayerButtonPrevious.setVisibility(View.VISIBLE);
                // mPlayerButtonFavorites.setVisibility(View.GONE);
                mPlayerButtonFavorites.setVisibility(View.VISIBLE);
                mPlayerButtonFavoritesHandle.setVisibility(View.VISIBLE);
                // mPlayerButtonQueue.setVisibility(View.GONE);
                mPlayerButtonQueue.setVisibility(View.VISIBLE);
                mPlayerButtonQueueHandle.setVisibility(View.VISIBLE);
                mPlayerButtonShuffle.setVisibility(View.GONE);
                mPlayerButtonLoop.setVisibility(View.GONE);
                mPlayerButtonSettings.setVisibility(View.GONE);
                mPlayerButtonLoad.setVisibility(View.GONE);
                if (isExistMusicFX()) {
                    mPlayerButtonEffects.setVisibility(View.GONE);
                    mPlayerButtonEffects.setState(fxbuttonstate);
                }
                // enable buttons.
                mPlayerButtonPlay.activate();
                mPlayerButtonPlayRadio.activate();
                mPlayerButtonPlayRadioHandle.activate();
                mPlayerButtonPlayHandle.activate();

                if (mPlayerService.hasPrevious()) {
                    mPlayerButtonPrevious.activate();
                } else {
                    mPlayerButtonPrevious.deactivate();
                }

                if (mPlayerService.hasNext()) {
                    mPlayerButtonNext.activate();
                    mPlayerButtonNextHandle.activate();
                } else {
                    mPlayerButtonNext.deactivate();
                    mPlayerButtonNextHandle.deactivate();
                }
                refreshFlipImages();
                mPlayerButtonShuffle.activate();
                mPlayerButtonLoop.activate();
                mPlayerButtonSettings.activate();

                LoopMode loopMode = mPlayerService.getLoopMode();
                if (loopMode == LoopMode.REAPLAY_SONG) {
                    // sets the single loop icon.
                    mPlayerButtonLoop
                            .setState(ThreeStatesActiveButton.State.SECOND);

                } else if (loopMode == LoopMode.ON) {
                    // sets whole player queue to be looped.
                    mPlayerButtonLoop
                            .setState(ThreeStatesActiveButton.State.THIRD);
                }
                // sets the suffle button.
                if (mPlayerService.isShuffling()) {
                    mPlayerButtonShuffle
                            .setState(TwoStatesActiveButton.State.SECOND);
                } else {
                    mPlayerButtonShuffle
                            .setState(TwoStatesActiveButton.State.ACTIVE);
                }
                // sets the play button.
                if (mPlayerService.getState() == State.PLAYING) {
                    togglePlayerPlayIcon(true);
                    // sets the duration of the track.
                    mPlayerTextDuration
                            .setText(Utils.secondsToString(mPlayerService
                                    .getDuration() / 1000));

                } else if (mPlayerService.getState() == State.PAUSED) {
                    togglePlayerPlayIcon(false);
                    // sets the current position in the progress.
                    int progress = (int) (((float) mPlayerService
                            .getCurrentPlayerPosition() / mPlayerService
                            .getDuration()) * 100);
                    mPlayerSeekBarProgress.setProgress(progress);
                    // sets the duration of the track.
                    mPlayerTextDuration
                            .setText(Utils.secondsToString(mPlayerService
                                    .getDuration() / 1000));
                    mPlayerSeekBarProgressHandle.setProgress(progress);
                } else if (mPlayerService.getState() == State.COMPLETED_QUEUE) {
                    showReplayButtonAsPlay();
                }

                viewVerticalLine.setVisibility(View.INVISIBLE);
                if (mPlayerService.isLoading() || mPlayerService.isPlaying()) {

                    setPlayerButtonFavorite();
                    Track currentTrack = mPlayerService
                            .getCurrentPlayingTrack();
                    CacheState cacheState = DBOHandler.getTrackCacheState(
                            mContext, "" + currentTrack.getId());
                    if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                        playerBarProgressCacheState
                                .setNotCachedStateVisibility(true);
                        playerBarProgressCacheStateHandle
                                .setNotCachedStateVisibility(true);

                        if (cacheState == CacheState.CACHED) {
                            mPlayerBarLayoutCacheState.setTag(true);
                            mPlayerBarLayoutCacheStateHandle.setTag(true);
                        } else if (cacheState == CacheState.QUEUED) {
                            playerBarProgressCacheState.showProgressOnly(true);
                            playerBarProgressCacheStateHandle
                                    .showProgressOnly(true);
                            mPlayerBarLayoutCacheState.setTag(null);
                            mPlayerBarLayoutCacheStateHandle.setTag(null);
                            playerBarProgressCacheState.setProgress(DBOHandler
                                    .getTrackCacheProgress(mContext, ""
                                            + currentTrack.getId()));
                            playerBarProgressCacheStateHandle
                                    .setProgress(DBOHandler
                                            .getTrackCacheProgress(mContext, ""
                                                    + currentTrack.getId()));

                        } else if (cacheState == CacheState.CACHING) {
                            mPlayerBarLayoutCacheState.setTag(null);
                            mPlayerBarLayoutCacheStateHandle.setTag(null);
                            playerBarProgressCacheState.setProgress(DBOHandler
                                    .getTrackCacheProgress(mContext, ""
                                            + currentTrack.getId()));
                            playerBarProgressCacheStateHandle
                                    .setProgress(DBOHandler
                                            .getTrackCacheProgress(mContext, ""
                                                    + currentTrack.getId()));

                        } else {
                            playerBarProgressCacheState.showProgressOnly(false);
                            playerBarProgressCacheStateHandle
                                    .showProgressOnly(false);
                            mPlayerBarLayoutCacheState.setTag(false);
                            mPlayerBarLayoutCacheStateHandle.setTag(false);
                        }
                        playerBarProgressCacheState.setCacheState(cacheState);
                        playerBarProgressCacheStateHandle
                                .setCacheState(cacheState);
                    }
                }
            } catch (Exception e) {
            }
        } else if (mPlayerService != null
                && (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || mPlayerService
                .getPlayMode() == PlayMode.DISCOVERY_MUSIC)) {
            try {
                mPlayerSeekBar.setVisibility(View.GONE);
                mPlayerSeekBarHandle.setVisibility(View.INVISIBLE);

                if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                        || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    mPlayerButtonNext.setVisibility(View.VISIBLE);
                    mPlayerButtonNextHandle.setVisibility(View.VISIBLE);
                    if (mPlayerService.hasNext()) {
                        mPlayerButtonNext.activate();
                        mPlayerButtonNextHandle.activate();
                    } else {
                        mPlayerButtonNext.deactivate();
                        mPlayerButtonNextHandle.deactivate();
                    }
                } else {
                    mPlayerButtonNext.setVisibility(View.GONE);
                    mPlayerButtonNextHandle.setVisibility(View.GONE);
                }
                mPlayerButtonPrevious.setVisibility(View.GONE);
                mPlayerButtonShuffle.setVisibility(View.GONE);
                mPlayerButtonLoop.setVisibility(View.GONE);
                mPlayerButtonSettings.setVisibility(View.GONE);
                mPlayerButtonLoad.setVisibility(View.GONE);

                // favorite button.
                if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                        || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    mPlayerButtonPlayRadio.setVisibility(View.GONE);
                    mPlayerButtonPlayRadioHandle.setVisibility(View.GONE);
                    mPlayerButtonFavorites.setVisibility(View.VISIBLE);
                    mPlayerButtonFavoritesHandle.setVisibility(View.VISIBLE);
                    if (PlayerService.service != null
                            && PlayerService.service.getState() == State.INTIALIZED)
                        mPlayerButtonPlay.setVisibility(View.INVISIBLE);
                    else
                        mPlayerButtonPlay.setVisibility(View.VISIBLE);
                } else {
                    mPlayerButtonPlay.setVisibility(View.GONE);
                    mPlayerButtonFavorites.setVisibility(View.GONE);
                    mPlayerButtonFavoritesHandle.setVisibility(View.GONE);
                    if (PlayerService.service != null
                            && PlayerService.service.getState() == State.INTIALIZED) {
                        mPlayerButtonPlayRadio.setVisibility(View.INVISIBLE);
                        mPlayerButtonPlayRadioHandle
                                .setVisibility(View.INVISIBLE);
                    } else {
                        mPlayerButtonPlayRadio.setVisibility(View.VISIBLE);
                        mPlayerButtonPlayRadioHandle
                                .setVisibility(View.VISIBLE);
                    }
                }
                mPlayerButtonPlay.activate();
                mPlayerButtonPlayRadio.activate();
                mPlayerButtonPlayRadioHandle.activate();
                mPlayerButtonPlayHandle.activate();

                Track currentTrack = mPlayerService.getCurrentPlayingTrack();
                String title, additionalTitle = "";
                if (mPlayerService.isAdPlaying()) {
                    title = advertiseTxt;
                    additionalTitle = "";
                    viewVerticalLine.setVisibility(View.INVISIBLE);
                } else {
                    title = Utils.getMultilanguageTextLayOut(mContext,
                            currentTrack.getTitle());
                    viewVerticalLine.setVisibility(View.VISIBLE);
                }

                if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                    if (!mPlayerService.isAdPlaying()) {
                        String artistName = currentTrack.getArtistName();
                        title = Utils
                                .getMultilanguageText(mContext, artistName);
                    }
                } else if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    if (!mPlayerService.isAdPlaying()) {
                        title = currentTrack.getTitle();
                        additionalTitle = currentTrack.getAlbumName();
                    }
                } else {
                    if (detail == null) {
                        SpannableStringBuilder sb = buildSemiColorString(
                                blueTitleLiveRadio,
                                "",
                                getResources().getColor(
                                        R.color.radio_blue_title_prefix),
                                getResources().getColor(R.color.white));
                        additionalTitle = Utils.getMultilanguageText(mContext,
                                sb.toString());
                    } else {
                        title = Utils.getMultilanguageTextLayOut(mContext,
                                detail.getTrack());
                        additionalTitle = Utils.getMultilanguageText(mContext,
                                detail.getAlbum());
                    }
                }
                changeMiniPlayerTitleText(title, additionalTitle);

                if ((mPlayerService.isLoading() || mPlayerService.isPlaying())
                        && mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {

                    CacheState cacheState = DBOHandler.getTrackCacheState(
                            mContext, "" + currentTrack.getId());
                    playerBarProgressCacheState
                            .setNotCachedStateVisibility(true);
                    playerBarProgressCacheStateHandle
                            .setNotCachedStateVisibility(true);
                    if (cacheState == CacheState.CACHED) {
                        mPlayerBarLayoutCacheState.setTag(true);
                        mPlayerBarLayoutCacheStateHandle.setTag(true);
                    } else if (cacheState == CacheState.QUEUED) {
                        playerBarProgressCacheState.showProgressOnly(true);
                        playerBarProgressCacheStateHandle
                                .showProgressOnly(true);
                        mPlayerBarLayoutCacheState.setTag(null);
                        mPlayerBarLayoutCacheStateHandle.setTag(null);
                        playerBarProgressCacheState.setProgress(DBOHandler
                                .getTrackCacheProgress(mContext, ""
                                        + currentTrack.getId()));
                        playerBarProgressCacheStateHandle
                                .setProgress(DBOHandler.getTrackCacheProgress(
                                        mContext, "" + currentTrack.getId()));

                    } else if (cacheState == CacheState.CACHING) {
                        mPlayerBarLayoutCacheState.setTag(null);
                        mPlayerBarLayoutCacheStateHandle.setTag(null);
                        playerBarProgressCacheState.setProgress(DBOHandler
                                .getTrackCacheProgress(mContext, ""
                                        + currentTrack.getId()));
                        playerBarProgressCacheStateHandle
                                .setProgress(DBOHandler.getTrackCacheProgress(
                                        mContext, "" + currentTrack.getId()));

                    } else {
                        playerBarProgressCacheState.showProgressOnly(false);
                        playerBarProgressCacheStateHandle
                                .showProgressOnly(false);
                        mPlayerBarLayoutCacheState.setTag(false);
                        mPlayerBarLayoutCacheStateHandle.setTag(false);
                    }
                    playerBarProgressCacheState.setCacheState(cacheState);
                    playerBarProgressCacheStateHandle.setCacheState(cacheState);
                }

                // sets the play button.
                if (mPlayerService.getState() == State.PLAYING) {
                    togglePlayerPlayIcon(true);
                } else if (mPlayerService.getState() == State.PAUSED) {
                    togglePlayerPlayIcon(false);
                }
            } catch (Exception e) {
            }

        }
        setFullPlayerBottomHeight();
    }

    private void adjustDrawerContentWhenPlaying() {
        if (mPlayerService.getPlayMode() == PlayMode.MUSIC
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            Track track = mPlayerService.getCurrentPlayingTrack();
            if (track == null) {
                return;
            }
            if (mPlayerService.isLoading()
                    || mPlayerService.isPlaying()
                    || (mPlayerService.getState() == State.IDLE && mPlayerService
                    .getPlayingQueue().size() > 0)) {
                if (mCurrentTrackDetails == null
                        || mCurrentTrackDetails.getId() != track.getId()) {
                    startLoadingMediaDetails(track);
                } else {
                    adjustActionButtonsVisibility(mCurrentTrackDetails);
                    if (mCurrentTrackDetails.getId() == track.getId()) {
                        setDrawerButtonFavorite();
                    }
                }
            }
        }
        if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
            // sets the visibility.
            setheaderVisibility();
            mDrawerActionsBar.setVisibility(View.VISIBLE);

            Track track = mPlayerService.getCurrentPlayingTrack();
            if (track == null) {
                return;
            }
            String title, additionalTitle;
            // sets the titles.
            if (mPlayerService.isAdPlaying()) {
                title = advertiseTxt;
                additionalTitle = "";
            } else {
                title = track.getTitle();
                if (mCurrentTrackDetails != null
                        && mCurrentTrackDetails.getId() == track.getId()
                        && mCurrentTrackDetails.getIntl_content() == 1) {
                    additionalTitle = mCurrentTrackDetails.getSingers();
                } else {
                    additionalTitle = track.getAlbumName();
                }
            }

            changeMiniPlayerTitleText(title, additionalTitle);
            // sets loading indicator.

            mDrawerButtonSettings.setClickable(true);
            mDrawerButtonAddFavorites.setClickable(true);
            mHeaderInfo.setVisibility(View.VISIBLE);
        } else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {

            setheaderVisibility();
            if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO)
                mHeaderInfo.setVisibility(View.GONE);
            else
                mHeaderInfo.setVisibility(View.VISIBLE);
            if (getPlayMode() == PlayMode.DISCOVERY_MUSIC)
                mDrawerButtonSettingsRadio.setVisibility(View.VISIBLE);
            else
                mDrawerButtonSettingsRadio.setVisibility(View.INVISIBLE);

            mDrawerActionsBar.setVisibility(View.INVISIBLE);
            if (!mPlayerService.isAdPlaying()) {
                Track track = mPlayerService.getCurrentPlayingTrack();
                if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                    MediaItem mediaItem = (MediaItem) track.getTag();

                    if (mediaItem != null) {
                        String title = mediaItem.getTitle();
                        if (title != null) {
                            changeMiniPlayerTitleText(title, "");
                        }
                    }

                } else if (mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    MediaItem mediaItem = (MediaItem) track.getTag();

                    if (mediaItem != null) {
                        String title = mediaItem.getTitle();
                        if (title != null) {
                            changeMiniPlayerTitleText(title,
                                    track.getAlbumName());
                        }
                    }
                } else {
                    changeMiniPlayerTitleText(blueTitleTopOnDemandRadio,
                            track.getArtistName());
                }
            }
            if (getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                showDiscoveryDetails();
            } else {
                showRadioDetails();
            }

            if (/*
				 * mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC ||
				 */mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                mDrawerButtonSettingsRadio.setClickable(true);
            } else {
                mDrawerButtonSettingsRadio.setClickable(false);
            }
            mDrawerButtonSettings.setClickable(true);
            mDrawerButtonAddFavorites.setClickable(false);
        }

    }

    private void adjustDrawerContentWhenNotPlaying() {
        setFullPlayerBottomHeight();
        if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
            refreshFlipImages();
            setheaderVisibility();
            List<Track> queue = getCurrentPlayingList();
            if (queue != null && queue.size() > 0)
                mDrawerActionsBar.setVisibility(View.VISIBLE);
            else
                mDrawerActionsBar.setVisibility(View.INVISIBLE);

            List<Track> trackList = getCurrentPlayingList();
            String title, additionalTitle;
            if (trackList != null && trackList.size() > 0) {
                Track track = trackList.get(0);
                title = track.getTitle();
                additionalTitle = Utils.getMultilanguageTextLayOut(activity,
                        track.getAlbumName());
            } else {
                title = Utils.getMultilanguageTextLayOut(activity, mResources
                        .getString(R.string.main_player_bar_text_not_playing));
                additionalTitle = Utils.TEXT_EMPTY;
            }

            changeMiniPlayerTitleText(title, additionalTitle);

            mDrawerButtonAddFavorites.setClickable(false);

        } else if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO
                || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                || mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            mDrawerActionsBar.setVisibility(View.INVISIBLE);

            if (getPlayMode() == PlayMode.DISCOVERY_MUSIC)
                removeDiscoveryDetails();
            else
                removeRadioDetails();

        }
    }

    public void clearDrawerContent() {
        if (getPlayMode() == PlayMode.MUSIC)
            mDrawerActionsBar.setVisibility(View.VISIBLE);
    }

    // ======================================================
    // Media Details. loading.
    // ======================================================

    private void cancelLoadingMediaDetails() {
        mDataManager.cancelGetMediaDetails();
    }

    private void startLoadingMediaDetails(Track track) {
        try {
            // cancel any running loading.
            mDataManager.cancelGetMediaDetails();
            boolean isCached = false;
            String mediaDeatils = DBOHandler.getTrackDetails(mContext, ""
                    + track.getId());
            if (DBOHandler.getTrackCacheState(getActivity(),track.getId()+"")== CacheState.CACHED && mediaDeatils != null &&
                    mediaDeatils.length() > 0) {
                MediaItem mediaItem = new MediaItem(track.getId(), null, null,
                        null, null, null, MediaType.TRACK.toString(), 0,
                        track.getAlbumId());
                mediaItem.setAlbumId(track.getAlbumId());
                MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
                        "", "", "", mediaItem, null, null);
                Response res = new Response();
                res.response = mediaDeatils;
                res.responseCode = CommunicationManager.RESPONSE_SUCCESS_200;
                onSuccess(mediaDetailsOperation.getOperationId(),
                        mediaDetailsOperation.parseResponse(res));
                isCached = true;
            }

            if (!isCached) {
                MediaItem mediaItem = new MediaItem(track.getId(), null, null,
                        null, null, null, MediaType.TRACK.toString(), 0,
                        track.getAlbumId());
                mediaItem.setAlbumId(track.getAlbumId());

                if (mCurrentTrackDetails != null
                        && mCurrentTrackDetails.getId() == track.getId()) {
                    MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
                            "", "", "", mediaItem, null, null);
                    Map<String, Object> resultMap = new HashMap<String, Object>();
                    resultMap.put(
                            MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS,
                            mCurrentTrackDetails);
                    onSuccess(mediaDetailsOperation.getOperationId(), resultMap);
                } else if (track.details != null) {
                    MediaDetailsOperation mediaDetailsOperation = new MediaDetailsOperation(
                            "", "", "", mediaItem, null, null);
                    Map<String, Object> resultMap = new HashMap<String, Object>();
                    resultMap.put(
                            MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS,
                            track.details);
                    onSuccess(mediaDetailsOperation.getOperationId(), resultMap);
                } else
                    mDataManager.getMediaDetails(mediaItem, null, this);
            }
        } catch (Exception e) {
        }
    }

    // ======================================================
    // Communication Operations events.
    // ======================================================

    @Override
    public void onStart(int operationId) {
        if (!isDetached() && !isRemoving() && activity != null
                && !activity.isFinishing()) {
            if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
                Logger.d(TAG, "Loading media details");
                clearActionButtons();
            } else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
                Logger.d(TAG, "Adding to Favorites");
            } else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
                Logger.d(TAG, "Removing from favorites");
            } else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED) {
                showLoadingDialog(R.string.application_dialog_loading_content);
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onSuccess(int operationId, Map<String, Object> responseObjects) {
        try {
            try {
                activity.findViewById(R.id.progressbar)
                        .setVisibility(View.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
                Logger.d(TAG, "Success loading media details");
                if (mPlayerService != null
                        && getCurrentPlayingList().size() == 0) {
                    clearQueue();
                    return;
                }

                try {
                    mCurrentTrackDetails = (MediaTrackDetails) responseObjects
                            .get(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
                    mPlayerService.getCurrentPlayingTrack().setFavorite(
                            mCurrentTrackDetails.IsFavorite());
                    if (mPlayerService.getCurrentPlayingTrack().details == null) {
                        mPlayerService.getCurrentPlayingTrack().details = mCurrentTrackDetails;
                    }

                    if (mCurrentTrackDetails != null) {
                        String imgs = mPlayerService.getCurrentPlayingTrack()
                                .getImagesUrlArray();
                        imgs = ImagesManager.getMusicArtSmallImageUrl(imgs);
                        if (TextUtils.isEmpty(imgs)) {
                            String imageURLArray = mCurrentTrackDetails
                                    .getImagesUrlArray();
                            String imageURL = ImagesManager
                                    .getMusicArtSmallImageUrl(imageURLArray);
                            if (!TextUtils.isEmpty(imageURL)) {
                                mPlayerService.getCurrentPlayingTrack()
                                        .setImagesUrlArray(
                                                mCurrentTrackDetails
                                                        .getImages());
                                if (coverFlowAdapter != null)
                                    coverFlowAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                    if (!isDetached() && !isRemoving() && activity != null
                            && !activity.isFinishing()) {
                        if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                            adjustActionButtonsVisibility(mCurrentTrackDetails);
                        }
                        setDrawerButtonFavorite();
                        setPlayerButtonFavorite();

                        CacheState cacheState = DBOHandler.getTrackCacheState(
                                mContext, "" + mCurrentTrackDetails.getId());
                        if (mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                            playerBarProgressCacheState
                                    .setNotCachedStateVisibility(true);
                            playerBarProgressCacheStateHandle
                                    .setNotCachedStateVisibility(true);
                            if (cacheState == CacheState.CACHED) {
                                mPlayerBarLayoutCacheState.setTag(true);
                                mPlayerBarLayoutCacheStateHandle.setTag(true);
                            } else if (cacheState == CacheState.QUEUED) {
                                playerBarProgressCacheState
                                        .showProgressOnly(true);
                                playerBarProgressCacheStateHandle
                                        .showProgressOnly(true);
                                mPlayerBarLayoutCacheState.setTag(null);
                                mPlayerBarLayoutCacheStateHandle.setTag(null);
                                playerBarProgressCacheState
                                        .setProgress(DBOHandler
                                                .getTrackCacheProgress(
                                                        mContext,
                                                        ""
                                                                + mCurrentTrackDetails
                                                                .getId()));
                                playerBarProgressCacheStateHandle
                                        .setProgress(DBOHandler
                                                .getTrackCacheProgress(
                                                        mContext,
                                                        ""
                                                                + mCurrentTrackDetails
                                                                .getId()));
                            } else if (cacheState == CacheState.CACHING) {
                                mPlayerBarLayoutCacheState.setTag(null);
                                mPlayerBarLayoutCacheStateHandle.setTag(null);
                                playerBarProgressCacheState
                                        .setProgress(DBOHandler
                                                .getTrackCacheProgress(
                                                        mContext,
                                                        ""
                                                                + mCurrentTrackDetails
                                                                .getId()));
                                playerBarProgressCacheStateHandle
                                        .setProgress(DBOHandler
                                                .getTrackCacheProgress(
                                                        mContext,
                                                        ""
                                                                + mCurrentTrackDetails
                                                                .getId()));
                            } else {
                                playerBarProgressCacheState
                                        .showProgressOnly(false);
                                playerBarProgressCacheStateHandle
                                        .showProgressOnly(false);
                                mPlayerBarLayoutCacheState.setTag(false);
                                mPlayerBarLayoutCacheStateHandle.setTag(null);
                            }
                            playerBarProgressCacheState
                                    .setCacheState(cacheState);
                            playerBarProgressCacheStateHandle
                                    .setCacheState(cacheState);

                        }
                    }
                } catch (Exception e) {
                    Logger.e("PlayerBarFragment:2955", e.toString());
                } catch (java.lang.Error e1) {
                    Logger.e("PlayerBarFragment:2955", e1.toString());
                }

            } else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
                try {
                    BaseHungamaResponse addToFavoriteResponse = (BaseHungamaResponse) responseObjects
                            .get(AddToFavoriteOperation.RESULT_KEY_ADD_TO_FAVORITE);

                    Track track = mPlayerService.getCurrentPlayingTrack();

                    if (!isDetached() && !isRemoving() && activity != null
                            && !activity.isFinishing()) {
                        if (addToFavoriteResponse.getCode() == FAVORITE_SUCCESS) {
                            Utils.makeText(activity,
                                    addToFavoriteResponse.getMessage(),
                                    Toast.LENGTH_LONG).show();

                            if (mCurrentTrackDetails != null
                                    && mCurrentTrackDetails.getId() == track
                                    .getId()) {

                                MediaItem mediaItem = new MediaItem(
                                        track.getId(), track.getTitle(),
                                        track.getAlbumName(),
                                        track.getArtistName(),
                                        getImgUrl(track),
                                        track.getBigImageUrl(), MediaType.TRACK
                                        .toString().toLowerCase(), 0,
                                        0, track.getImages(),
                                        track.getAlbumId());

                                try {
                                    mCurrentTrackDetails
                                            .setNumOfFav(mCurrentTrackDetails
                                                    .getNumOfFav() + 1);
                                    mCurrentTrackDetails.setIsFavorite(true);
                                    mPlayerService.getCurrentPlayingTrack()
                                            .setFavorite(true);
                                } catch (Exception e) {
                                    Logger.e(getClass().getName() + ":3029",
                                            e.toString());
                                }
                                Intent intent = new Intent(
                                        ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
                                Bundle extras = new Bundle();
                                extras.putSerializable(
                                        ActionDefinition.EXTRA_MEDIA_ITEM,
                                        (Serializable) mediaItem);
                                extras.putBoolean(
                                        ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE,
                                        true);
                                if (mCurrentTrackDetails != null)
                                    extras.putInt(
                                            ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT,
                                            mCurrentTrackDetails.getNumOfFav());
                                intent.putExtras(extras);

                                mLocalBroadcastManager.sendBroadcast(intent);
                            }

                            if (getPlayMode() == PlayMode.TOP_ARTISTS_RADIO)
                                mDataManager
                                        .checkBadgesAlert(
                                                "" + artistRadioId,
                                                MEDIA_TYPE_ONDEMANRADIO,
                                                SocialBadgeAlertOperation.ACTION_FAVORITE,
                                                PlayerBarFragment.this);
                            else
                                mDataManager
                                        .checkBadgesAlert(
                                                "" + track.getId(),
                                                MEDIA_TYPE_SONG,
                                                SocialBadgeAlertOperation.ACTION_FAVORITE,
                                                this);
                        } else {
                            if (track != null && track.getTitle() != null) {
                                Utils.makeText(
                                        activity,
                                        getResources().getString(
                                                R.string.favorite_error_saving,
                                                track.getTitle()),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } catch (Exception e) {
                }

            } else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
                // done removing from favorites.
                BaseHungamaResponse removeFromFavoriteResponse = (BaseHungamaResponse) responseObjects
                        .get(RemoveFromFavoriteOperation.RESULT_KEY_REMOVE_FROM_FAVORITE);

                Track track = mPlayerService.getCurrentPlayingTrack();

                if (!isRemoving() && activity != null
                        && !activity.isFinishing()) {
                    if (removeFromFavoriteResponse.getCode() == FAVORITE_SUCCESS) {

                        Utils.makeText(activity,
                                removeFromFavoriteResponse.getMessage(),
                                Toast.LENGTH_LONG).show();

                        MediaItem mediaItem = new MediaItem(track.getId(),
                                track.getTitle(), track.getAlbumName(),
                                track.getArtistName(), getImgUrl(track),
                                track.getBigImageUrl(), MediaType.TRACK
                                .toString().toLowerCase(), 0, 0,
                                track.getImages(), track.getAlbumId());

                        if (mCurrentTrackDetails != null)
                            mCurrentTrackDetails
                                    .setNumOfFav(mCurrentTrackDetails
                                            .getNumOfFav() - 1);
                        mPlayerService.getCurrentPlayingTrack().setFavorite(
                                false);

                        // packs an added media item intent action.
                        Intent intent = new Intent(
                                ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED);
                        Bundle extras = new Bundle();
                        extras.putSerializable(
                                ActionDefinition.EXTRA_MEDIA_ITEM,
                                (Serializable) mediaItem);
                        extras.putBoolean(
                                ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE,
                                false);
                        if (mCurrentTrackDetails != null)
                            extras.putInt(
                                    ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT,
                                    mCurrentTrackDetails.getNumOfFav());
                        intent.putExtras(extras);

                        mLocalBroadcastManager.sendBroadcast(intent);

                    } else {
                        if (track != null && track.getTitle() != null) {
                            Utils.makeText(
                                    activity,
                                    getResources().getString(
                                            R.string.favorite_error_removing,
                                            track.getTitle()),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED) {
                // refreshList
                try {

                    List<MediaItem> mediaItems = (List<MediaItem>) responseObjects
                            .get(MediaContentOperation.RESULT_KEY_OBJECT_MEDIA_ITEMS);
                    int trackCounter = 0;
                    ArrayList<Track> mTracksTop10 = new ArrayList<Track>();
                    for (MediaItem mediaItem : mediaItems) {
                        if (mediaItem.getMediaType() == MediaType.TRACK) {
                            Track track = new Track(mediaItem.getId(),
                                    mediaItem.getTitle(),
                                    mediaItem.getAlbumName(),
                                    mediaItem.getArtistName(),
                                    mediaItem.getImageUrl(),
                                    mediaItem.getBigImageUrl(),
                                    mediaItem.getImages(),
                                    mediaItem.getAlbumId());
                            mTracksTop10.add(track);
                            trackCounter++;
                            if (trackCounter == 10) {
                                break;
                            }
                        }
                    }
                    if (mTracksTop10.size() > 0) {
                        onLoadMenuTop10Selected(mTracksTop10);
                    }
                } catch (Exception e) {
                    Logger.printStackTrace(e);
                }
            } else if (operationId == OperationDefinition.Hungama.OperationId.TRACK_TRIVIA) {
                mTrackTrivia = (TrackTrivia) responseObjects
                        .get(TrackTriviaOperation.RESULT_KEY_OBJECT_TRACK_TRIVIA);
            } else if (operationId == OperationDefinition.Hungama.OperationId.TRACK_LYRICS) {
                mTrackLyrics = (TrackLyrics) responseObjects
                        .get(TrackLyricsOperation.RESPONSE_KEY_TRACK_LYRICS);
            }
            hideLoadingDialog();
        } catch (Exception e) {
        }
    }

    @Override
    public void onFailure(int operationId, ErrorType errorType,
                          String errorMessage) {
        if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_DETAILS) {
            Logger.d(TAG,
                    "Failed loading media details: " + errorType.toString()
                            + " " + errorMessage);
            clearActionButtons();
        } else if (operationId == OperationDefinition.Hungama.OperationId.ADD_TO_FAVORITE) {
            Logger.d(TAG, "Failed Adding to Favorites");
            mDrawerButtonAddFavorites.setClickable(true);
            mPlayerButtonFavorites.setClickable(true);
            mPlayerButtonFavoritesHandle.setClickable(true);
            hideLoadingDialog();

        } else if (operationId == OperationDefinition.Hungama.OperationId.REMOVE_FROM_FAVORITE) {
            Logger.d(TAG, "Failed Removing from favorites");
            mDrawerButtonAddFavorites.setClickable(true);
            mPlayerButtonFavorites.setClickable(true);
            mPlayerButtonFavoritesHandle.setClickable(true);
            hideLoadingDialog();
        } else if (operationId == OperationDefinition.Hungama.OperationId.MEDIA_CONTENT_FEATURED) {
            hideLoadingDialog();
        }
    }

    // ======================================================
    // Drawer Actions.
    // ======================================================
    boolean hasInfo = false, hasSimilar = false, hasLyrics = true,
            hasTrivia = true, hasAlbum = true;

    private void adjustActionButtonsVisibility(
            MediaTrackDetails mediaTrackDetails) {
        try {
            // adjust action buttons in the drawer's content.
            // mDrawerActionInfo.setVisibility(View.VISIBLE);
            mDrawerActionInfo.setVisibility(View.GONE);
            mDrawerActionSimilar.setVisibility(View.VISIBLE);
            // mDrawerActionAlbum.setVisibility(View.VISIBLE);
            mDrawerActionAlbum.setVisibility(View.VISIBLE);
            hasSimilar = false;
            hasLyrics = true;
            hasTrivia = true;
            hasAlbum = true;
            ArrayList<Button> buttons = new ArrayList<Button>();
            // buttons.add(mDrawerActionAlbum);

            if (mCurrentTrackDetails.hasVideo()) {
                mDrawerActionVideo.setVisibility(View.VISIBLE);
                buttons.add(mDrawerActionVideo);
            } else {
                mDrawerActionVideo.setVisibility(View.GONE);
            }

            if (mCurrentTrackDetails.hasLyrics()) {
                hasLyrics = true;
                mDrawerActionLyrics.setVisibility(View.GONE);
                buttons.add(mDrawerActionLyrics);
            } else {
                hasLyrics = false;
                mDrawerActionLyrics.setVisibility(View.GONE);
            }

            if (mCurrentTrackDetails.hasTrivia()) {
                hasTrivia = true;
                mDrawerActionTrivia.setVisibility(View.GONE);
                buttons.add(mDrawerActionTrivia);
            } else {
                hasTrivia = false;
                mDrawerActionTrivia.setVisibility(View.GONE);
            }

            if (mApplicationConfigurations.getSaveOfflineMode()) {
                hasTrivia = false;
                hasAlbum = false;
                hasLyrics = false;
                hasSimilar = false;
                mDrawerActionTrivia.setVisibility(View.GONE);
                mDrawerActionLyrics.setVisibility(View.GONE);
                mDrawerActionSimilar.setVisibility(View.GONE);
                mDrawerActionVideo.setVisibility(View.GONE);

            }
        } catch (Exception e) {
            Logger.e(getClass().getName() + ":3191", e.toString());
        }

        ShowHint();
    }

    private static boolean isHintVisible = false;

    private void ShowHint() {
        if (!mApplicationConfigurations.isFullPlayerDrawerHelp()
                && mApplicationConfigurations.isFullPlayerDragHelp()
                && !isHintVisible && getPlayMode() == PlayMode.MUSIC) {
            if (!mApplicationConfigurations.isFullPlayerDrawerHelp()
                    && !isHintVisible && mDrawer!=null && mDrawer.isPanelExpanded()
                    && getPlayMode() == PlayMode.MUSIC) {
                isHintVisible = true;
                mDrawer.lock();
                RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                int margin = ((Number) (getResources().getDisplayMetrics().density * 12))
                        .intValue();
                lps.setMargins(2 * margin, margin, 2 * margin,
                        mDataManager.getShowcaseButtonMargin() * margin);

                ViewTarget target = new ViewTarget(
                        miniPlayerView
                                .findViewById(R.id.main_player_content_info_bar_button_view_settings));
                ShowcaseView sv = new ShowcaseView.Builder(activity, false)
                        .setTarget(target)
                        .setContentTitle(
                                R.string.showcase_full_player_get_mp3_title)
                        .setContentText(
                                R.string.showcase_full_player_get_mp3_message)
                        .setStyle(R.style.CustomShowcaseTheme2)
                        .hideOnTouchOutside().build();
                sv.setOnShowcaseEventListener(onShowcaseEventListener);
                sv.setBlockShowCaseTouches(true);
                sv.setButtonPosition(lps);
                mApplicationConfigurations.setFullPlayerDrawerHelp(true);
            }
        }
        if (mApplicationConfigurations.getAppOpenCount() >= 2
                && !mApplicationConfigurations.isFullPlayerDrawerHelp2()
                && mApplicationConfigurations.isFullPlayerDrawerHelp()
                && !mApplicationConfigurations.isEnabledHomeGuidePage3Offline()
                && !isHintVisible && getPlayMode() == PlayMode.MUSIC
                && mDrawer.isPanelExpanded()) {
            isHintVisible = true;
            mDrawer.lock();
            RelativeLayout.LayoutParams lps = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            lps.addRule(RelativeLayout.CENTER_IN_PARENT);
            lps.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            int margin = ((Number) (getResources().getDisplayMetrics().density * 12))
                    .intValue();
            lps.setMargins(2 * margin, margin, 2 * margin,
                    mDataManager.getShowcaseButtonMargin() * margin);

            ViewTarget target = new ViewTarget(
                    activity.findViewById(R.id.main_player_bar_button_rl_save_offline));
            ShowcaseView sv = new ShowcaseView.Builder(activity, false)
                    .setTarget(target)
                    .setContentTitle(
                            R.string.showcase_full_player_save_offline_title)
                    .setContentText(
                            R.string.showcase_full_player_save_offline_message)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .hideOnTouchOutside().build();
            sv.setOnShowcaseEventListener(onShowcaseEventListener);
            sv.setBlockShowCaseTouches(true);
            sv.setButtonPosition(lps);
            mApplicationConfigurations.setFullPlayerDrawerHelp2(true);
        }
    }

    private OnShowcaseEventListener onShowcaseEventListener = new OnShowcaseEventListener() {
        @Override
        public void onShowcaseViewShow(ShowcaseView showcaseView) {
            isHintVisible = true;
        }

        @Override
        public void onShowcaseViewHide(ShowcaseView showcaseView) {
            mDrawer.unlock();
            isHintVisible = false;
        }

        @Override
        public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
        }
    };

    private void clearActionButtons() {
        closeOpenedContent(false);
        hasTrivia = false;
        hasAlbum = false;
        hasLyrics = false;
        hasSimilar = false;
    }

    private void openConentFor(int actionContentId) {
        try {
            Logger.i("openConentFor", "Info call: openConentFor");
            // creates the related fragment to the button id.
            Fragment fragment = createFragmentForAction(actionContentId);
            if (fragment != null) {
                mDrawer.lock();
                FragmentTransaction fragmentTransaction = mFragmentManager
                        .beginTransaction();

                fragmentTransaction.add(R.id.main_player_container_addtional,
                        fragment, DRAWER_CONTENT_ACTION_BUTTON_FRAGMENT_TAG);
                Logger.i("openConentFor", "Info call: openConentFor");
                fragmentTransaction.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.printStackTrace(e);
        }
    }

    private void closeOpenedContent(boolean needToClose) {

        try {
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            Fragment lastOpenedFragment = mFragmentManager
                    .findFragmentByTag(DRAWER_CONTENT_ACTION_BUTTON_FRAGMENT_TAG);

            if (!mPlayerService.isAdPlaying()
                    && (mPlayerService.getState() == State.INTIALIZED)
                    && lastOpenedFragment != null
                    && (lastOpenedFragment instanceof PlayerAlbumFragment || lastOpenedFragment instanceof PlayerSimilarFragment))
                return;

            if (lastOpenedFragment != null) {
                if (needToUnlockDrawerAgain)
                    mDrawer.unlock();
                fragmentTransaction.remove(lastOpenedFragment);
                fragmentTransaction.commitAllowingStateLoss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Fragment createFragmentForAction(int actionContentId) {

        if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_INFO) {
            if (!isHandledActionOffline(ACTION_INFO)) {
                final PlayerInfoFragment playerInfoFragment = new PlayerInfoFragment();
                // adds the current track's details as argument.
                Bundle data = new Bundle();
                data.putSerializable(
                        PlayerInfoFragment.FRAGMENT_ARGUMENT_MEDIA_TRACK_DETAILS,
                        (Serializable) mCurrentTrackDetails);
                Track track = mPlayerService.getCurrentPlayingTrack();
                data.putSerializable(
                        PlayerSimilarFragment.FRAGMENT_ARGUMENT_TRACK,
                        (Serializable) track);
                playerInfoFragment.setArguments(data);
                playerInfoFragment
                        .setOnInfoItemSelectedListener(new OnInfoItemSelectedListener() {
                            @Override
                            public void onInfoItemSelected(String infoItemText,
                                                           String rowDescription) {
                                try {
                                    closeContent();

                                    if (infoItemText.contains("(")) {
                                        int startPosition = infoItemText
                                                .indexOf("(");
                                        int endPosition = infoItemText
                                                .indexOf(")");
                                        if (endPosition > startPosition) {
                                            String album = infoItemText
                                                    .substring(0, startPosition);
                                            String year = infoItemText
                                                    .substring(
                                                            startPosition + 1,
                                                            endPosition);
                                            playerInfoFragment
                                                    .showHideLoader(true);
                                            if (TextUtils.isDigitsOnly(year)) {
                                                openMainSearchFragment(album);
                                            } else {
                                                openMainSearchFragment(infoItemText
                                                        .substring(
                                                                0,
                                                                infoItemText
                                                                        .indexOf(")")));
                                            }
                                        }
                                    } else {
                                        playerInfoFragment.showHideLoader(true);
                                        openMainSearchFragment(infoItemText);
                                    }
                                    // openMainSearchFragment(infoItemText);
                                } catch (Exception e) {
                                    Logger.printStackTrace(e);
                                }
                            }
                        });

                try {
                    // Flurry report:
                    Map<String, String> reportMap = new HashMap<String, String>();
                    reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
                            .toString(), mPlayerService
                            .getCurrentPlayingTrack().getTitle());
                    Analytics.logEvent(
                            FlurryConstants.FlurryEventName.InfoTab.toString(),
                            reportMap);
                } catch (Exception e) {
                    Logger.e(getClass().getName() + ":3275", e.toString());
                }
                playerInfoFragment.setInfoButton(mDrawerActionInfo);
                return playerInfoFragment;
            } else {
                return null;
            }
        } else if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_SIMILAR) {
            // sets listener for the tile's options.
            PlayerSimilarFragment playerSimilarFragment = new PlayerSimilarFragment();
            playerSimilarFragment
                    .setOnMediaItemOptionSelectedListener(mOnSimilarMediaItemOptionSelectedListener);
            // passes in the current track.
            Track track = mPlayerService.getCurrentPlayingTrack();
            Bundle data = new Bundle();
            data.putSerializable(PlayerSimilarFragment.FRAGMENT_ARGUMENT_TRACK,
                    (Serializable) track);
            data.putString(
                    MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
                    FlurryConstants.FlurrySubSectionDescription.FullPlayerSimilarSongs
                            .toString());
            playerSimilarFragment.setArguments(data);

            try {
                // Flurry report:
                Map<String, String> reportMap = new HashMap<String, String>();
                reportMap.put(
                        FlurryConstants.FlurryKeys.TitleOfTheSong.toString(),
                        track.getTitle());
                Analytics
                        .logEvent(FlurryConstants.FlurryEventName.SimilarSongs
                                .toString(), reportMap);
            } catch (Exception e) {
                Logger.e(getClass().getName() + ":3298", e.toString());
            }
            return playerSimilarFragment;
        } else if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_ALBUM) {
            if (!isHandledActionOffline(ACTION_INFO)) {
                // sets listener for the tile's options.
                PlayerAlbumFragment playerAlbumFragment = new PlayerAlbumFragment();
                playerAlbumFragment
                        .setOnMediaItemOptionSelectedListener(mOnSimilarMediaItemOptionSelectedListener);
                // passes in the current track.
                Bundle data = new Bundle();
                data.putSerializable(
                        PlayerInfoFragment.FRAGMENT_ARGUMENT_MEDIA_TRACK_DETAILS,
                        (Serializable) mCurrentTrackDetails);
                Track track = mPlayerService.getCurrentPlayingTrack();
                data.putSerializable(
                        PlayerSimilarFragment.FRAGMENT_ARGUMENT_TRACK,
                        (Serializable) track);
                data.putString(
                        MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
                        FlurryConstants.FlurryKeys.AlbumFullPlayer.toString());
                playerAlbumFragment.setArguments(data);

                try {
                    // Flurry report:
                    Map<String, String> reportMap = new HashMap<String, String>();
                    reportMap.put(FlurryConstants.FlurryKeys.TitleOfTheSong
                            .toString(), mCurrentTrackDetails.getTitle());
                    Analytics.logEvent(
                            FlurryConstants.FlurryEventName.AlbumSongs
                                    .toString(), reportMap);
                } catch (Exception e) {
                    Logger.e(getClass().getName() + ":3298", e.toString());
                }
                playerAlbumFragment.setAlbumButton(mDrawerActionAlbum);
                return playerAlbumFragment;
            } else {
                return null;
            }
        } else if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_VIDEO) {

            PlayerVideoFragment playerVideoFragment = new PlayerVideoFragment();
            playerVideoFragment
                    .setOnMediaItemOptionSelectedListener(mOnVideoMediaItemOptionSelectedListener);
            playerVideoFragment.setIsNeedToChangeTextColor(true);

            // adds the current track's details as argument.
            Bundle data = new Bundle();
            data.putSerializable(
                    PlayerVideoFragment.FRAGMENT_ARGUMENT_TRACK_DETAILS,
                    (Serializable) mCurrentTrackDetails);
            Track track = mPlayerService.getCurrentPlayingTrack();
            data.putSerializable(PlayerSimilarFragment.FRAGMENT_ARGUMENT_TRACK,
                    (Serializable) track);
            data.putString(
                    MediaTileGridFragment.FLURRY_SUB_SECTION_DESCRIPTION,
                    FlurryConstants.FlurrySubSectionDescription.VideoRelatedAudio
                            .toString());
            playerVideoFragment.setArguments(data);

            try {
                // Flurry report
                Map<String, String> reportMap = new HashMap<String, String>();
                reportMap
                        .put(FlurryFullPlayerParams.Title
                                .toString(), mPlayerService
                                .getCurrentPlayingTrack().getTitle());
                reportMap
                        .put(FlurryFullPlayerParams.Type
                                        .toString(),
                                FlurryFullPlayerParams.Music
                                        .toString());
                Analytics.logEvent(
                        FlurryConstants.FlurryEventName.RelatedVideos
                                .toString(), reportMap);
            } catch (Exception e) {
                Logger.e(getClass().getName() + ":3322", e.toString());
            }

            return playerVideoFragment;

        } else if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_LYRICS) {
            try {
                // Flurry report
                Map<String, String> reportMap = new HashMap<String, String>();
                reportMap
                        .put(FlurryFullPlayerParams.Title
                                .toString(), mPlayerService
                                .getCurrentPlayingTrack().getTitle());
                Analytics.logEvent(
                        FlurryConstants.FlurryEventName.FullPlayerLyrics
                                .toString(), reportMap);
            } catch (Exception e) {
                Logger.e(getClass().getName() + ":3334", e.toString());
            }

            PlayerLyricsFragment playerLyricsFragment = new PlayerLyricsFragment();

            Track track = mPlayerService.getCurrentPlayingTrack();
            Bundle data = new Bundle();
            data.putSerializable(PlayerSimilarFragment.FRAGMENT_ARGUMENT_TRACK,
                    (Serializable) track);
            playerLyricsFragment.setTraviaButton(mDrawerActionLyrics);
            playerLyricsFragment.setArguments(data);

            return playerLyricsFragment;

        } else if (actionContentId == DRAWER_CONTENT_ACTION_BUTTON_ID_TRIVIA) {
            try {
                // Flurry report
                Map<String, String> reportMap = new HashMap<String, String>();
                reportMap
                        .put(FlurryFullPlayerParams.Title
                                .toString(), mPlayerService
                                .getCurrentPlayingTrack().getTitle());
                Analytics.logEvent(
                        FlurryConstants.FlurryEventName.FullPlayerTrivia
                                .toString(), reportMap);
            } catch (Exception e) {
                Logger.e(getClass().getName() + ":3354", e.toString());
            }
            PlayerTriviaFragment playerTriviaFragment = new PlayerTriviaFragment();

            Track track = mPlayerService.getCurrentPlayingTrack();
            Bundle data = new Bundle();
            data.putSerializable(PlayerSimilarFragment.FRAGMENT_ARGUMENT_TRACK,
                    (Serializable) track);

            playerTriviaFragment.setArguments(data);
            return playerTriviaFragment;
        }

        return null;
    }

    boolean needToUnlockDrawerAgain = false;
    private OnClickListener mDrawerActionsClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            Logger.i("openConentFor", "Info call: onCreateView");
            if (mPlayerService != null && mPlayerService.isAdPlaying()) {
                return;
            }
            if ((PlayerService.service != null && PlayerService.service
                    .getState() == State.INTIALIZED) || isPlayerLoading || isNextPrevBtnClick) {
                Utils.makeText(getActivity(), "Please wait...", 0).show();
                return;
            }
            if (isHandledActionOffline(ACTION_INFO)) {
                return;
            }
            isNextPrevBtnClick = false;
            if (view.getId() == R.id.main_player_content_actions_bar_button_more) {
                if (!isHandledActionOffline(ACTION_INFO)) {
                    openMoreMenu(view);
                }
            } else {
                LinearLayout parent = (LinearLayout) view.getParent();

                View child = null;
                Button button = null;
                int count = parent.getChildCount();
                needToUnlockDrawerAgain = true;
                for (int i = 0; i < count; i++) {
                    child = parent.getChildAt(i);
                    if ((child instanceof LanguageButton)
                            || (isEnglish && (child instanceof Button))) {
                        button = (Button) child;

                        if (!child.equals(view)) {
                        } else {
                            int buttonId = ((Integer) button.getTag())
                                    .intValue();
                            if (mDataManager.isDeviceOnLine()) {
                                needToUnlockDrawerAgain = false;
                                openConentFor(buttonId);
                            } else {
                                final Button selectedButton = button;
                                ((MainActivity) activity)
                                        .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                                            @Override
                                            public void onRetryButtonClicked() {
                                                selectedButton.performClick();
                                            }
                                        });
                            }
                        }
                    }
                }
                needToUnlockDrawerAgain = true;
            }
        }
    };

    private OnMediaItemOptionSelectedListener mOnSimilarMediaItemOptionSelectedListener = new OnMediaItemOptionSelectedListener() {

        @Override
        public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
                                                         int position) {
            Intent intent = new Intent(activity, MediaDetailsActivity.class);
            intent.putExtra(MediaDetailsActivity.EXTRA_MEDIA_ITEM,
                    (Serializable) mediaItem);
            intent.putExtra(MediaDetailsActivity.FLURRY_SOURCE_SECTION,
                    FlurryConstants.FlurrySourceSection.Search.toString());
            startActivity(intent);
        }

        @Override
        public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
                                                    int position) {
        }

        @Override
        public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem,
                                                     int position) {
            if (mediaItem.getMediaType() == MediaType.TRACK) {
                Track track = new Track(mediaItem.getId(),
                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                        mediaItem.getAlbumId());
                List<Track> tracks = new ArrayList<Track>();
                tracks.add(track);
                playNow(tracks, null, null);
            }
        }

        @Override
        public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
                                                      int position) {
            if (mediaItem.getMediaType() == MediaType.TRACK) {
                Track track = new Track(mediaItem.getId(),
                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                        mediaItem.getAlbumId());
                List<Track> tracks = new ArrayList<Track>();
                tracks.add(track);
                playNext(tracks);
            }
        }

        @Override
        public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
                                                        int position) {
            if (mediaItem.getMediaType() == MediaType.TRACK) {
                Track track = new Track(mediaItem.getId(),
                        mediaItem.getTitle(), mediaItem.getAlbumName(),
                        mediaItem.getArtistName(), mediaItem.getImageUrl(),
                        mediaItem.getBigImageUrl(), mediaItem.getImages(),
                        mediaItem.getAlbumId());
                List<Track> tracks = new ArrayList<Track>();
                tracks.add(track);
                addToQueue(tracks, null, null);
            }
        }

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
                    CacheManager.saveOfflineAction(activity, mediaItem, track);
                    Utils.saveOfflineFlurryEvent(activity,
                            FlurryConstants.FlurryCaching.LongPressMenuSong
                                    .toString(), mediaItem);
                } else if (mediaItem.getMediaType() == MediaType.ALBUM
                        || mediaItem.getMediaType() == MediaType.PLAYLIST) {
                    CacheManager.saveOfflineAction(activity, mediaItem, null);

                    if (mediaItem.getMediaType() == MediaType.ALBUM)
                        Utils.saveOfflineFlurryEvent(
                                activity,
                                FlurryConstants.FlurryCaching.LongPressMenuAlbum
                                        .toString(), mediaItem);
                    else
                        Utils.saveOfflineFlurryEvent(
                                activity,
                                FlurryConstants.FlurryCaching.LongPressMenuPlaylist
                                        .toString(), mediaItem);
                }
            } else {
                // new MediaCachingTask(this, mediaItem, null).execute();
                CacheManager.saveOfflineAction(activity, mediaItem, null);
                Utils.saveOfflineFlurryEvent(activity,
                        FlurryConstants.FlurryCaching.LongPressMenuVideo
                                .toString(), mediaItem);
            }

        }

        @Override
        public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
                                                         int position) {

        }
    };

    private OnMediaItemOptionSelectedListener mOnVideoMediaItemOptionSelectedListener = new OnMediaItemOptionSelectedListener() {

        @Override
        public void onMediaItemOptionPlayNowSelected(MediaItem mediaItem,
                                                     int position) {
        }

        @Override
        public void onMediaItemOptionPlayNextSelected(MediaItem mediaItem,
                                                      int position) {
        }

        @Override
        public void onMediaItemOptionAddToQueueSelected(MediaItem mediaItem,
                                                        int position) {
        }

        @Override
        public void onMediaItemOptionRemoveSelected(MediaItem mediaItem,
                                                    int position) {
        }

        @Override
        public void onMediaItemOptionShowDetailsSelected(MediaItem mediaItem,
                                                         int position) {
            if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
                // closes the drawer.
                closeContent();
                Fragment videoFragment = mFragmentManager
                        .findFragmentByTag(DRAWER_CONTENT_ACTION_BUTTON_FRAGMENT_TAG);
                List<MediaItem> list = new ArrayList<MediaItem>();
                if (videoFragment instanceof PlayerVideoFragment) {
                    list = ((PlayerVideoFragment) videoFragment).mMediaItems;
                }
                // fires the View Video Activity.
                Intent intent = new Intent(activity, VideoActivity.class);
                intent.putExtra(VideoActivity.EXTRA_MEDIA_ITEM_VIDEO,
                        (Serializable) mediaItem);
                intent.putExtra(VideoActivity.EXTRA_MEDIA_LIST_VIDEO,
                        (Serializable) list);
                intent.putExtra(VideoActivity.EXTRA_MEDIA_POS_VIDEO, position);
                activity.startActivity(intent);
            }
        }

        @Override
        public void onMediaItemOptionSaveOfflineSelected(MediaItem mediaItem,
                                                         int position) {
            if (mediaItem.getMediaContentType() == MediaContentType.VIDEO) {
                CacheManager.saveOfflineAction(activity, mediaItem, null);
                Utils.saveOfflineFlurryEvent(activity,
                        FlurryConstants.FlurryCaching.LongPressMenuVideo
                                .toString(), mediaItem);
            }

        }

        @Override
        public void onMediaItemOptionPlayAndOpenSelected(MediaItem mediaItem,
                                                         int position) {

        }

    };

    // ======================================================
    // Drawer Actions - Radio.
    // ======================================================
    public MediaItem currentRadioMediaItem = null;
    private void showRadioDetails() {
        // checks if it's already visible.
        RadioDetailsFragment radioDetailsFragment = null;
        try {
            radioDetailsFragment = (RadioDetailsFragment) mFragmentManager
                    .findFragmentByTag(RadioDetailsFragment.TAG);

            MediaItem mediaItem = null;
            MediaCategoryType mediaCategoryType = null;
            if (mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                mediaCategoryType = MediaCategoryType.TOP_ARTISTS_RADIO;
            } else {
                mediaCategoryType = MediaCategoryType.LIVE_STATIONS;
            }
            Track currentTrack = mPlayerService.getCurrentPlayingTrack();
            mediaItem = (MediaItem) currentTrack.getTag();
            if(mediaCategoryType == MediaCategoryType.LIVE_STATIONS) {
                if (mediaItem != null)
                    currentRadioMediaItem = mediaItem;

                if (mediaItem == null)
                    mediaItem = currentRadioMediaItem;
            }else{
                currentRadioMediaItem = null;
            }
            if(mediaItem==null && currentTrack!=null){
                mediaItem = new MediaItem(currentTrack.getId(),
                        currentTrack.getTitle(), currentTrack.getAlbumName(),
                        currentTrack.getArtistName(), getImgUrl(currentTrack),
                        currentTrack.getBigImageUrl(), MediaType.TRACK.toString()
                        .toLowerCase(), 0, 0,
                        currentTrack.getImages(), currentTrack.getAlbumId());
                if(PlayerService.service!=null && PlayerService.service.getCurrentPlayingTrack()!=null)
                    PlayerService.service.getCurrentPlayingTrack().setTag(mediaItem);

            }else{
                if(PlayerService.service!=null && currentTrack.getTag()==null){
                    PlayerService.service.getCurrentPlayingTrack().setTag(mediaItem);
                }
            }

            if (mediaItem != null) {
                mediaItem.setMediaContentType(MediaContentType.RADIO);
            }

            Bundle arguments = new Bundle();
            arguments.putSerializable(RadioDetailsFragment.EXTRA_CATEGORY_TYPE,
                    (Serializable) mediaCategoryType);
            arguments.putSerializable(RadioDetailsFragment.EXTRA_MEDIA_ITEM,
                    (Serializable) mediaItem);
            arguments.putBoolean(RadioDetailsFragment.IS_FOR_PLAYER_BAR, true);

            arguments.putBoolean(RadioDetailsFragment.EXTRA_DO_SHOW_TITLE_BAR,
                    false);
            arguments.putBoolean(RadioDetailsFragment.IS_FOR_PLAYER_BAR, true);

            if (radioDetailsFragment == null) {
                Utils.clearCache(true);
                FragmentTransaction fragmentTransaction = mFragmentManager
                        .beginTransaction();
                radioDetailsFragment = new RadioDetailsFragment();

                if (mPlayerService.getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                    if (radioDetailsFragment != null && detail != null)
                        arguments.putSerializable(
                                RadioDetailsFragment.EXTRA_COMING_UP_NEXT,
                                (Serializable) detail.getNextTrack());
                }
                radioDetailsFragment.setArguments(arguments);
                fragmentTransaction.add(R.id.main_player_drawer_content,
                        radioDetailsFragment, RadioDetailsFragment.TAG);
                fragmentTransaction.commit();
            } else {
                radioDetailsFragment.updateInfoDetails(arguments, false);
            }
            setVisibilityOfInfoForRadio(mediaItem);
            radioDetailsFragment.setTransparentBg();
            try {
                if (getPlayMode() == PlayMode.TOP_ARTISTS_RADIO
                        && mPlayerService.isAdPlaying()) {
                    final RadioDetailsFragment temp = radioDetailsFragment;
                    commonHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (temp != null) {
                                temp.loadAudioAd(mPlayerService
                                        .getAudioAdPlacement());
                            }
                        }
                    }, 1000);
                }
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    private void setVisibilityOfInfoForRadio(MediaItem mMediaItem) {
        if (mMediaItem instanceof LiveStation) {
            LiveStation liveStation = (LiveStation) mMediaItem;
            String descriptions = liveStation.getDescription();
            if (descriptions != null && !descriptions.equals("")) {
                mDrawerButtonSettingsRadio.setVisibility(View.VISIBLE);
            } else {
                mDrawerButtonSettingsRadio.setVisibility(View.GONE);
            }
        } else {
            mDrawerButtonSettingsRadio.setVisibility(View.GONE);
        }
    }

    private void removeRadioDetails() {
        RadioDetailsFragment radioDetailsFragment = (RadioDetailsFragment) mFragmentManager
                .findFragmentByTag(RadioDetailsFragment.TAG);

        if (radioDetailsFragment != null) {
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            fragmentTransaction.remove(radioDetailsFragment);
            fragmentTransaction.commitAllowingStateLoss();
            adHolder = null;
            adHolderOuter = null;
            dontWant = null;
            Utils.clearCache(true);
        }
    }

    private Discover mDiscover;

    public void setDiscovery(Discover mDiscover) {
        this.mDiscover = mDiscover;
    }

    private void showDiscoveryDetails() {
        Logger.i("Discovery Player", "Discovery Player : 1");
        DiscoveryPlayDetailsFragment discoveryPlayFragment = null;
        try {
            discoveryPlayFragment = (DiscoveryPlayDetailsFragment) mFragmentManager
                    .findFragmentByTag(DiscoveryPlayDetailsFragment.TAG);

            if (discoveryPlayFragment == null) {
                Utils.clearCache(true);
                Bundle arguments = new Bundle();
                arguments.putBoolean(
                        DiscoveryPlayDetailsFragment.IS_FOR_PLAYER_BAR, true);

                FragmentTransaction fragmentTransaction = mFragmentManager
                        .beginTransaction();
                discoveryPlayFragment = new DiscoveryPlayDetailsFragment();

                arguments.putSerializable(DiscoveryActivity.ARGUMENT_MOOD,
                        (Serializable) mDiscover);

                discoveryPlayFragment.setArguments(arguments);

                fragmentTransaction
                        .add(R.id.main_player_drawer_content,
                                discoveryPlayFragment,
                                DiscoveryPlayDetailsFragment.TAG);
                fragmentTransaction.commit();
            } else {
                discoveryPlayFragment.setDiscovery(mDiscover);
            }
            try {
                if (mPlayerService.isAdPlaying()) {
                    final DiscoveryPlayDetailsFragment temp = discoveryPlayFragment;
//					if (mDrawer.isPanelExpanded()) {
//						if (temp != null) {
//							temp.loadAudioAd(mPlayerService
//									.getAudioAdPlacement());
//						}
//					} else {
                    commonHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (temp != null) {
                                temp.loadAudioAd(mPlayerService
                                        .getAudioAdPlacement());
                            }
                        }
                    }, 1000);
//					}
                }
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
        } catch (Exception e) {

        }
    }

    private void removeDiscoveryDetails() {
        DiscoveryPlayDetailsFragment radioDetailsFragment = (DiscoveryPlayDetailsFragment) mFragmentManager
                .findFragmentByTag(DiscoveryPlayDetailsFragment.TAG);

        if (radioDetailsFragment != null) {
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            fragmentTransaction.remove(radioDetailsFragment);
            fragmentTransaction.commitAllowingStateLoss();
            adHolder = null;
            adHolderOuter = null;
            dontWant = null;
            Utils.clearCache(true);
        }
    }

    // ======================================================
    // Load Menu.
    // ======================================================

    @Override
    public void onLoadMenuTop10Selected(List<Track> topTenMediaItems) {
        if (!Utils.isListEmpty(topTenMediaItems)) {
            addToQueue(topTenMediaItems, null, null);
            adjustBarWhenOpenedAndPlaying();
            adjustDrawerContentWhenPlaying();
        }
    }

    @Override
    public void onLoadMenuRadioSelected() {
        if (isContentOpened()) {
            closeContent();
        }
        ((MainActivity) activity)
                .setNavigationItemSelected(NavigationItem.RADIO);

    }

    @Override
    public void onLoadMenuMyPlaylistSelected() {
        // Show playlist Dialog
        List<Track> tracks = new ArrayList<Track>();
        boolean isFromLoadMenu = true;
        FragmentManager fm = activity.getSupportFragmentManager();
        PlaylistDialogFragment playlistDialogFragment = PlaylistDialogFragment
                .newInstance(tracks, isFromLoadMenu,
                        FlurryConstants.FlurryPlaylists.FullPlayer.toString());

        playlistDialogFragment.setOnLoadMenuItemOptionSelectedListener(this);
        playlistDialogFragment.show(fm, "PlaylistDialogFragment");
    }

    @Override
    public void onLoadMenuMyFavoritesSelected() {
        // closes the player bar content.
        closeContent();
        // shows the favorite activity.
        Intent favoritesActivityIntent = new Intent(
                activity.getApplicationContext(), FavoritesActivity.class);
        startActivity(favoritesActivityIntent);
    }

    @Override
    public void onLoadPlaylistFromDialogSelected(List<Track> tracks) {
        if (!Utils.isListEmpty(tracks)) {
            addToQueue(tracks, null, null);
            adjustBarWhenOpenedAndPlaying();
            adjustDrawerContentWhenPlaying();
        }
    }

    // ======================================================
    // Favorites
    // ======================================================

    private void setDrawerButtonFavorite() {
        try {
            if (mCurrentTrackDetails.IsFavorite()) {
                mDrawerButtonAddFavorites
                        .setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                getResources().getDrawable(
                                        R.drawable.icon_media_details_fav_blue),
                                null, null);
                mDrawerButtonAddFavorites.setSelected(true);
            } else {
                mDrawerButtonAddFavorites
                        .setCompoundDrawablesWithIntrinsicBounds(
                                null,
                                getResources()
                                        .getDrawable(
                                                R.drawable.icon_media_details_fav_white),
                                null, null);
                mDrawerButtonAddFavorites.setSelected(false);
            }
            if (mCurrentTrackDetails != null)
                mDrawerButtonAddFavorites.setText(Utils
                        .roundTheCount(mCurrentTrackDetails.getNumOfFav()));
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    private void setPlayerButtonFavorite() {
        // Logger.s(artistUserFav + " ::::: getPlayMode() ::::: " +
        // getPlayMode() + " :::: " + mPlayerButtonFavorites.isSelected());
        if (getPlayMode() == PlayMode.MUSIC
                || getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            if (PlayerService.service != null
                    && PlayerService.service.getCurrentPlayingTrack() != null) {
                mCurrentTrackDetails = PlayerService.service
                        .getCurrentPlayingTrack().details;
            }
            boolean flag = (mCurrentTrackDetails != null && mCurrentTrackDetails
                    .IsFavorite())
                    || (mPlayerService != null && mPlayerService
                    .getCurrentPlayingTrack().isFavorite());
            if (!flag)
                flag = (mPlayerService != null
                        && mPlayerService.getCurrentPlayingTrack() != null
                        && mPlayerService.getCurrentPlayingTrack().details != null && mPlayerService
                        .getCurrentPlayingTrack().details.IsFavorite());

            if (flag) {
                mPlayerButtonFavorites
                        .setImageDrawable(getResources().getDrawable(
                                R.drawable.icon_main_player_favorites_blue));
                mPlayerButtonFavoritesHandle
                        .setImageDrawable(getResources().getDrawable(
                                R.drawable.icon_main_player_favorites_blue));
                mPlayerButtonFavorites.setSelected(true);
                mPlayerButtonFavoritesHandle.setSelected(true);
            } else {
                mPlayerButtonFavorites.setImageDrawable(getResources()
                        .getDrawable(
                                R.drawable.icon_main_player_favorites_white));
                mPlayerButtonFavorites.setSelected(false);
                mPlayerButtonFavoritesHandle.setImageDrawable(getResources()
                        .getDrawable(
                                R.drawable.icon_main_player_favorites_white));
                mPlayerButtonFavoritesHandle.setSelected(false);

            }
        } else {
            if (artistUserFav == 1 || mPlayerButtonFavorites.isSelected()
                    || mPlayerButtonFavoritesHandle.isSelected()) {
                mPlayerButtonFavorites
                        .setImageDrawable(getResources().getDrawable(
                                R.drawable.icon_main_player_favorites_blue));
                mPlayerButtonFavorites.setSelected(true);
                mPlayerButtonFavoritesHandle
                        .setImageDrawable(getResources().getDrawable(
                                R.drawable.icon_main_player_favorites_blue));
                mPlayerButtonFavoritesHandle.setSelected(true);

            } else {
                mPlayerButtonFavorites.setImageDrawable(getResources()
                        .getDrawable(
                                R.drawable.icon_main_player_favorites_white));
                mPlayerButtonFavorites.setSelected(false);
                mPlayerButtonFavoritesHandle.setImageDrawable(getResources()
                        .getDrawable(
                                R.drawable.icon_main_player_favorites_white));
                mPlayerButtonFavoritesHandle.setSelected(false);

            }
        }
    }

    /**
     * Handles changes in the favorite state of Media Items, marks the button
     * accordingly.
     */
    private static final class MediaItemFavoriteStateReceiver extends
            BroadcastReceiver {

        private final WeakReference<PlayerBarFragment> playerBarFragmentReference;

        MediaItemFavoriteStateReceiver(PlayerBarFragment playerBarFragment) {
            this.playerBarFragmentReference = new WeakReference<PlayerBarFragment>(
                    playerBarFragment);
        }

        PlayerService service;

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (ActionDefinition.ACTION_MEDIA_ITEM_FAVORITE_STATE_CHANGED
                        .equalsIgnoreCase(intent.getAction())) {
                    Bundle extras = intent.getExtras();
                    MediaItem mediaItem = (MediaItem) extras
                            .getSerializable(ActionDefinition.EXTRA_MEDIA_ITEM);
                    boolean isFavorite = extras
                            .getBoolean(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_IS_FAVORITE);
                    int count = extras
                            .getInt(ActionDefinition.EXTRA_MEDIA_ITEM_FAVORITE_COUNT);
                    updateFaoriteStateInCacheTable(context, mediaItem, isFavorite);
                    PlayerBarFragment playerBarFragment = playerBarFragmentReference
                            .get();
                    if (playerBarFragment == null) {
                        return;
                    }

                    if (playerBarFragment.mCurrentTrackDetails != null
                            && playerBarFragment.mCurrentTrackDetails.getId() == mediaItem
                            .getId()) {

                        Resources resources = playerBarFragment.getResources();
                        if (count == 0) {
                            if (isFavorite)
                                count = playerBarFragment.mCurrentTrackDetails
                                        .getNumOfFav() + 1;
                            else
                                count = playerBarFragment.mCurrentTrackDetails
                                        .getNumOfFav() - 1;
                        }

                        // updates the given media details.
                        playerBarFragment.mCurrentTrackDetails
                                .setNumOfFav(count);

                        if (isFavorite) {

                            Drawable d = resources
                                    .getDrawable(R.drawable.icon_media_details_fav_blue);
                            // updates the view.
                            playerBarFragment.mDrawerButtonAddFavorites
                                    .setCompoundDrawablesWithIntrinsicBounds(
                                            null, d, null, null);
                            playerBarFragment.mDrawerButtonAddFavorites
                                    .setSelected(true);
                            playerBarFragment.mDrawerButtonAddFavorites
                                    .setText(Utils.roundTheCount(count));

                            playerBarFragment.mPlayerButtonFavorites
                                    .setSelected(true);
                            playerBarFragment.mPlayerButtonFavoritesHandle
                                    .setSelected(true);
                            playerBarFragment.mPlayerButtonFavorites
                                    .setImageDrawable(resources
                                            .getDrawable(R.drawable.icon_main_player_favorites_blue));
                            playerBarFragment.mPlayerButtonFavoritesHandle
                                    .setImageDrawable(resources
                                            .getDrawable(R.drawable.icon_main_player_favorites_blue));

                            playerBarFragment.mCurrentTrackDetails
                                    .setIsFavorite(true);

                            if (playerBarFragment.getPlayMode() != PlayMode.MUSIC) {
                                PlayerBarFragment.setArtistUserFav(1);
                            }
                            try {
                                if (PlayerService.service != null) {
                                    PlayerService.service
                                            .getCurrentPlayingTrack()
                                            .setFavorite(true);
                                    if (PlayerService.service
                                            .getCurrentPlayingTrack().details != null)
                                        PlayerService.service
                                                .getCurrentPlayingTrack().details
                                                .setIsFavorite(true);

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {

                            // updates the view.
                            playerBarFragment.mDrawerButtonAddFavorites
                                    .setCompoundDrawablesWithIntrinsicBounds(
                                            null,
                                            resources
                                                    .getDrawable(R.drawable.icon_media_details_fav_white),
                                            null, null);

                            playerBarFragment.mDrawerButtonAddFavorites
                                    .setText(Utils.roundTheCount(count));
                            playerBarFragment.mDrawerButtonAddFavorites
                                    .setSelected(false);

                            playerBarFragment.mPlayerButtonFavorites
                                    .setImageDrawable(resources
                                            .getDrawable(R.drawable.icon_main_player_favorites_white));
                            playerBarFragment.mPlayerButtonFavorites
                                    .setSelected(false);
                            playerBarFragment.mPlayerButtonFavoritesHandle
                                    .setImageDrawable(resources
                                            .getDrawable(R.drawable.icon_main_player_favorites_white));
                            playerBarFragment.mPlayerButtonFavoritesHandle
                                    .setSelected(false);

                            playerBarFragment.mCurrentTrackDetails
                                    .setIsFavorite(false);

                            if (playerBarFragment.getPlayMode() != PlayMode.MUSIC) {
                                PlayerBarFragment.setArtistUserFav(0);
                            }

                            try {
                                if (PlayerService.service != null) {
                                    PlayerService.service
                                            .getCurrentPlayingTrack()
                                            .setFavorite(false);
                                    if (PlayerService.service
                                            .getCurrentPlayingTrack().details != null)
                                        PlayerService.service
                                                .getCurrentPlayingTrack().details
                                                .setIsFavorite(false);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                        playerBarFragment.mDrawerButtonAddFavorites
                                .setClickable(true);
                        playerBarFragment.mPlayerButtonFavorites
                                .setClickable(true);
                        playerBarFragment.mPlayerButtonFavoritesHandle
                                .setClickable(true);
                    }
                    Track track = new Track(mediaItem.getId(), mediaItem.getTitle(),
                            mediaItem.getAlbumName(), mediaItem.getArtistName(),
                            mediaItem.getImageUrl(), mediaItem.getBigImageUrl(),
                            mediaItem.getImages(), mediaItem.getAlbumId());
                    PlayingQueue mPlayingQueue = playerBarFragment.mPlayerService
                            .getPlayerQueueObject();
                    if (playerBarFragment.getCurrentPlayingList() != null &&
                            playerBarFragment.getCurrentPlayingList().indexOf(track) != -1) {
                        int index = playerBarFragment.getCurrentPlayingList().indexOf(track);
                        Track queueTrack = mPlayingQueue.getTrack(index);
                        if (isFavorite) {
                            queueTrack.setFavorite(true);
                            if (queueTrack.details != null)
                                queueTrack.details.setIsFavorite(true);
                        } else {
                            queueTrack.setFavorite(false);
                            if (queueTrack.details != null)
                                queueTrack.details.setIsFavorite(false);
                        }
                    }
                    mPlayingQueue.savePlayerQueue();
                }
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }
        }

        private void updateFaoriteStateInCacheTable(Context context, MediaItem mediaItem, boolean favorite) {
            try {
                String savedResponse = DBOHandler.getTrackDetails(
                        context, "" + mediaItem.getId());
                JSONObject jsonResponse = new JSONObject(
                        savedResponse);
                JSONObject jsonCatalog = null;
                if (jsonResponse.has("catalog")) {
                    jsonCatalog = jsonResponse.getJSONObject(
                            "catalog").getJSONObject("content");
                } else {
                    jsonCatalog = jsonResponse
                            .getJSONObject("response");
                }
                if (jsonCatalog.has("user_fav")) {
                    jsonCatalog.put("user_fav",
                            favorite ? 1 : 0);
                }
                DBOHandler.updateTrack(context,
                        "" + mediaItem.getId(), null,
                        jsonResponse.toString(), null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ======================================================
    // Queue.
    // ======================================================

    private void openQueue() {
        Intent intent = new Intent(activity, PlayerQueueActivity.class);
        startActivity(intent);

    }

    // ======================================================
    // Settings.
    // ======================================================

    private void openSettings(final View v) {
        try {
            QuickActionFullPlayerSetting quickAction;
            boolean needToSetDownloadOption = false;
            if (mCurrentTrackDetails != null
                    && mCurrentTrackDetails.hasDownload()) {
                needToSetDownloadOption = true;
            } else {
                needToSetDownloadOption = false;
            }

            boolean isDiscovery = (getPlayMode() != null && getPlayMode() == PlayMode.DISCOVERY_MUSIC) ? true
                    : false;

            quickAction = new QuickActionFullPlayerSetting(mContext, v,
                    needToSetDownloadOption, isDiscovery);
            quickAction.setOnEditerPicsSelectedListener(this);
            quickAction.show(v);
            v.setEnabled(false);
            quickAction
                    .setOnDismissListener(new QuickActionFullPlayerSetting.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            v.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    // ======================================================
    // More Menu.
    // ======================================================

    private void openMoreMenu(final View view) {
        try {
            QuickActionFullPlayerMore quickAction;
            quickAction = new QuickActionFullPlayerMore(mContext, hasInfo,
                    hasSimilar, hasLyrics, hasTrivia, hasAlbum);
            quickAction.setOnMoreSelectedListener(this);
            quickAction.show(view);
            view.setEnabled(false);
            quickAction
                    .setOnDismissListener(new QuickActionFullPlayerMore.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            view.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    // ======================================================
    // Sleep mode / Gym mode.
    // ======================================================

    public void onSleepModeSelected() {
        // Flurry Event
        Map<String, String> reportMap = new HashMap<String, String>();
        reportMap.put(
                FlurryFullPlayerParams.SleepMode.toString(),
                "Sleep Mode");
        Analytics.logEvent(
                FlurryFullPlayerParams.SleepMode.toString(),
                reportMap);
    }

    public void onGymModeSelected() {
        // Flurry Event
        Map<String, String> reportMap = new HashMap<String, String>();
        reportMap.put(
                FlurryFullPlayerParams.SleepMode.toString(),
                "Gym Mode");
        Analytics.logEvent(
                FlurryConstants.FlurryEventName.FullPlayerTrivia.toString(),
                reportMap);
        openGymMode();
    }

    public void onAudioSettingSelected() {
        Intent intent = new Intent(activity, SettingsActivity.class);
        intent.putExtra("isAudioSetting", true);
        startActivity(intent);
    }

    public void onEquilizerSettingSelected() {
        mPlayerButtonEffects.performClick();
    }

    private void openGymMode() {
        // removeAllFragments();
        isGymMode = true;
        refreshFlipImages();
        gymModeStartTime = System.currentTimeMillis();

        mDrawerButtonAddFavorites.setClickable(false);

        // locks the drawer to avoid it moving while swiping the gym mode.
        mDrawer.lock();

        // shows the Gym Mode content.
        PlayerGymModeFragment playerGymModeFragment = new PlayerGymModeFragment();
        playerGymModeFragment
                .setOnPlayButtonStateChangedListener(new OnPlayButtonStateChangedListener() {

                    @Override
                    public void onPlayClicked() {
                        togglePlayerPlayIcon(true);
                    }

                    @Override
                    public void onPauseClicked() {
                        togglePlayerPlayIcon(false);
                    }
                });
        playerGymModeFragment.setOnGymModeExitClickedListener(this);

        FragmentTransaction fragmentTransaction = mFragmentManager
                .beginTransaction();

        fragmentTransaction.add(R.id.main_player_container_addtional,
                playerGymModeFragment, PlayerGymModeFragment.TAG);
        fragmentTransaction
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    long gymModeStartTime = 0;

    private TwoStatesActiveButton mPlayerButtonEffects;

    private void closeGymMode() {
        isGymMode = false;
        refreshFlipImages();
        Fragment fragment = mFragmentManager
                .findFragmentByTag(PlayerGymModeFragment.TAG);
        if (fragment != null && fragment.isVisible()) {

            PlayerGymModeFragment playerGymModeFragment = (PlayerGymModeFragment) fragment;
            playerGymModeFragment.setOnPlayButtonStateChangedListener(null);
            playerGymModeFragment.setOnGymModeExitClickedListener(null);

            // it was opened. closes it.
            FragmentTransaction fragmentTransaction = mFragmentManager
                    .beginTransaction();
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_and_show_bottom_enter,
                    R.anim.slide_and_show_bottom_exit,
                    R.anim.slide_and_show_bottom_enter,
                    R.anim.slide_and_show_bottom_exit);

            fragmentTransaction.remove(fragment);
            fragmentTransaction.commit();
            mDrawer.unlock();
            mDrawerButtonAddFavorites.setClickable(true);
        }
        if (gymModeStartTime != 0) {
            int duration = (int) ((System.currentTimeMillis() - gymModeStartTime) / (60 * 1000));
            Map<String, String> reportMap = new HashMap<String, String>();
            reportMap.put(FlurryConstants.FlurryAllPlayer.TimeOfDay.toString(),
                    Utils.getTimeOfDay());
            reportMap.put(FlurryConstants.FlurryAllPlayer.Duration.toString(),
                    "" + duration);
            Analytics.logEvent(
                    FlurryConstants.FlurryAllPlayer.GymModeUsed.toString(),
                    reportMap);
        }
    }

    @Override
    public void onGymModeExit() {
        closeGymMode();
    }

    // ======================================================
    // Add to playlists.
    // ======================================================

    private void showAddToPlaylistDialog() {
        try {
            if (mDataManager.isDeviceOnLine()) {
                List<Track> playingTrack = new ArrayList<Track>();
                playingTrack.add(mPlayerService.getCurrentPlayingTrack());
                PlaylistDialogFragment editNameDialog = PlaylistDialogFragment
                        .newInstance(playingTrack, false,
                                FlurryConstants.FlurryPlaylists.FullPlayer
                                        .toString());
                editNameDialog.show(mFragmentManager, "PlaylistDialogFragment");
            } else {
                ((MainActivity) activity)
                        .internetConnectivityPopup(new MainActivity.OnRetryClickListener() {
                            @Override
                            public void onRetryButtonClicked() {
                                mDrawerActionPlaylist.performClick();
                            }
                        });
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.player.PlayerService.PlayerBarUpdateListener
	 * #OnPlayerBarUpdate(int, java.lang.String)
	 */
    @Override
    public void OnPlayerBarUpdate(final int progress, final String label2) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if(mPlayerService
//                        .getDuration()>=0 /*&& mPlayerService.isCastConnected()*/) {
//                    mPlayerTextDuration.setText(Utils.secondsToString(mPlayerService
//                            .getDuration() / 1000));
//                }
                if (mDrawer!=null && mDrawer.isPanelExpanded()) {
                    String label = label2.replace("/", "");
                    mPlayerTextCurrent.setText(label);
                    if (!isStartTracking && mPlayerSeekBarProgress!=null) {
                        mPlayerSeekBarProgress.setProgress(progress);
                    }
                } else {
                    if (!isStartTracking && mPlayerSeekBarProgressHandle!=null) {
                        mPlayerSeekBarProgressHandle.setProgress(progress);
                    }
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private boolean isExistMusicFX() {
        try {
            if (Build.VERSION.SDK_INT >= 9) {
                Intent i = new Intent(
                        AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                return (activity.getPackageManager().resolveActivity(i, 0) != null);
            } else {
                return false;
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private boolean isMusicFxOn() {
        try {
            if (Build.VERSION.SDK_INT >= 9 && mPlayerService != null) {
                int sessionId = mPlayerService.getAudioSessionId();
                boolean enabled = new BassBoost(0, sessionId).getEnabled();
                if (!enabled)
                    enabled = new Virtualizer(0, sessionId).getEnabled();
                return enabled;
            } else
                return false;
        } catch (Exception e) {
        }
        return false;
    }

    private static TwoStatesActiveButton.State fxbuttonstate = TwoStatesActiveButton.State.ACTIVE;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void launchMusicFXGUI() {
        Logger.s("------------------- " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 9) {
            try {
                Intent i = new Intent(
                        AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                i.putExtra(AudioEffect.EXTRA_AUDIO_SESSION,
                        mPlayerService.getAudioSessionId());
                i.putExtra(AudioEffect.EXTRA_PACKAGE_NAME,
                        activity.getPackageName());
                startActivityForResult(i, 1);
                Logger.s("get ---------fxbuttonstate---------- "
                        + mPlayerButtonEffects.getState());
                if (fxbuttonstate == TwoStatesActiveButton.State.SECOND)
                    fxbuttonstate = TwoStatesActiveButton.State.ACTIVE;
                else
                    fxbuttonstate = TwoStatesActiveButton.State.SECOND;
                mPlayerButtonEffects.setState(fxbuttonstate);
            } catch (Exception e) {
            }
        }
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.player.PlayerService.PlayerStateListener#
	 * onAdCompletion()
	 */
    @Override
    public void onAdCompletion() {
        Logger.s("-------------onAdCompletion--------------");
        stopProgressUpdater();
        togglePlayerPlayIcon(false);
        if (mPlayingEventListener != null) {
            mPlayingEventListener.onTrackFinish();
        }

        if (mDrawer.isPanelExpanded()) {
            if (mPlayerService != null && !mPlayerService.isAdPlaying()) {
                //initializeAds(false);
				startRefreshAdsHandler();
            }
        }

        if (getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
            RadioDetailsFragment radioDetailsFragment = (RadioDetailsFragment) mFragmentManager
                    .findFragmentByTag(RadioDetailsFragment.TAG);
            if (radioDetailsFragment != null) {
                radioDetailsFragment.completeAudioAd();
            }
        } else if (getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
            DiscoveryPlayDetailsFragment radioDetailsFragment = (DiscoveryPlayDetailsFragment) mFragmentManager
                    .findFragmentByTag(DiscoveryPlayDetailsFragment.TAG);
            if (radioDetailsFragment != null) {
                radioDetailsFragment.completeAudioAd();
            }
        }

        adjustCurrentPlayerState();

        lastAdReportingTime = 0;
        showHideCastIcon();
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.hungama.myplay.activity.player.PlayerService.PlayerStateListener#
	 * onStartPlayingAd()
	 */
    @Override
    public void onStartPlayingAd(final Placement audioad) {
        Utils.clearCache(true);
        stopAdsHandler();
        try {
            activity.findViewById(R.id.progressbar).setVisibility(View.GONE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stopTriviaTimer();
        removeTitleHandler(true);
        Logger.i(TAG, "Starts playing Ad: ");
        changeMiniPlayerTitleText(advertiseTxt, "");

        // sets the play button icon and enables it.
        mPlayerButtonPlay.activate();
        mPlayerButtonPlayRadio.activate();
        mPlayerButtonPlayRadioHandle.activate();
        mPlayerButtonPlayHandle.activate();
        mPlayerButtonPlay.setClickable(true);
        mPlayerButtonPlayRadio.setClickable(true);
        mPlayerButtonPlayRadioHandle.setClickable(true);
        mPlayerButtonPlayHandle.setClickable(true);
        togglePlayerPlayIcon(true);
        mPlayerSeekBarProgress.setProgress(0);
        mPlayerSeekBarProgress.setSecondaryProgress(0);
        mPlayerSeekBarProgress.setEnabled(true);
        mPlayerSeekBarProgressHandle.setProgress(0);
        mPlayerSeekBarProgressHandle.setSecondaryProgress(0);
        mPlayerSeekBarProgressHandle.setEnabled(true);
        startProgressUpdater();
        mPlayerTextDuration.setText(Utils.secondsToString(mPlayerService
                .getDuration() / 1000));
        if (mPlayingEventListener != null) {
            mPlayingEventListener.onTrackPlay();
        }
        if (!mDrawer.isPanelExpanded()) {
            mDrawer.expandPanel();
            adjustBarWhenOpenedAndPlaying();
        } else {
            adjustBarWhenOpenedAndPlaying();
            try {
                if (getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                    final RadioDetailsFragment temp = (RadioDetailsFragment) mFragmentManager
                            .findFragmentByTag(RadioDetailsFragment.TAG);
                    commonHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (temp != null) {
                                temp.loadAudioAd(audioad);
                            }
                        }
                    }, 1000);
                } else if (getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    final DiscoveryPlayDetailsFragment temp = (DiscoveryPlayDetailsFragment) mFragmentManager
                            .findFragmentByTag(DiscoveryPlayDetailsFragment.TAG);
                    commonHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (temp != null) {
                                temp.loadAudioAd(audioad);
                            }
                        }
                    }, 1000);
                }
            } catch (Exception e) {
                Logger.printStackTrace(e);
            }

        }

        if (getPlayMode() == PlayMode.MUSIC)
            setAudioAdImage(audioad);
        playButtonImageUpdate();
        showHideCastIcon();
    }

    private static long lastAdReportingTime = 0;

    public void updatedCurrentTrackCacheState() {
        try {
            if (mCurrentTrackDetails != null
                    && (getPlayMode() == PlayMode.MUSIC || getPlayMode() == PlayMode.DISCOVERY_MUSIC)
			/* && CacheManager.isProUser(mContext) */) {
                CacheState cacheState = DBOHandler.getTrackCacheState(mContext,
                        "" + mCurrentTrackDetails.getId());
                Logger.s("-------SaveOffline-------- " + cacheState);
                if (cacheState != null) {

                    playerBarProgressCacheStateHandle.setCacheState(cacheState);
                    playerBarProgressCacheState.setCacheState(cacheState);
                    if (cacheState == CacheState.CACHED) {
                        mPlayerBarLayoutCacheStateHandle.setTag(true);
                        mPlayerBarLayoutCacheState.setTag(true);

                    } else if (cacheState == CacheState.QUEUED) {
                        playerBarProgressCacheState.showProgressOnly(true);
                        playerBarProgressCacheStateHandle
                                .showProgressOnly(true);
                        mPlayerBarLayoutCacheStateHandle.setTag(null);
                        mPlayerBarLayoutCacheState.setTag(null);

                        playerBarProgressCacheStateHandle
                                .setProgress(DBOHandler.getTrackCacheProgress(
                                        mContext,
                                        "" + mCurrentTrackDetails.getId()));
                        playerBarProgressCacheState.setProgress(DBOHandler
                                .getTrackCacheProgress(mContext, ""
                                        + mCurrentTrackDetails.getId()));

                    } else if (cacheState == CacheState.CACHING) {

                        mPlayerBarLayoutCacheStateHandle.setTag(null);
                        mPlayerBarLayoutCacheState.setTag(null);

                        playerBarProgressCacheStateHandle
                                .setProgress(DBOHandler.getTrackCacheProgress(
                                        mContext,
                                        "" + mCurrentTrackDetails.getId()));
                        playerBarProgressCacheState.setProgress(DBOHandler
                                .getTrackCacheProgress(mContext, ""
                                        + mCurrentTrackDetails.getId()));

                    } else {
                        playerBarProgressCacheState.showProgressOnly(false);
                        playerBarProgressCacheStateHandle
                                .showProgressOnly(false);
                        mPlayerBarLayoutCacheStateHandle.setTag(false);
                        mPlayerBarLayoutCacheState.setTag(false);
                    }
                }
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    public void trackDragAndDrop(int from, int to) {
        if (mPlayerService != null) {
            mPlayerService.trackDragAndDrop(from, to);
        }
    }

    public void updateNotificationForOffflineMode() {
        if (mPlayerService != null)
            mPlayerService.updateNotificationForOffflineMode();
    }

    public void stopLiveRadioUpdater() {
        if (mPlayerService != null)
            mPlayerService.stopLiveRadioUpdater();
    }

    LiveStationDetails detail;

    @Override
    public void OnRadioBarUpdate(LiveStationDetails detail) {
        try {

            this.detail = detail;
            if (detail != null) {
                if (getPlayMode() == PlayMode.LIVE_STATION_RADIO
                        && mPlayerService != null) {

                    Track track = mPlayerService.getCurrentPlayingTrack();
                    MediaItem mediaItem = (MediaItem) track.getTag();
                    String title = Utils.getMultilanguageTextLayOut(mContext,
                            track.getTitle()), additionalText;
                    if (mediaItem != null) {
                        String title1 = mediaItem.getTitle();
                        if (title1 != null) {
                            title = Utils.getMultilanguageTextLayOut(mContext,
                                    title1);
                        }
                    }
                    additionalText = Utils.getMultilanguageTextLayOut(mContext,
                            detail.getAlbum());
                    // changeTitleAdditionalText(additionalText, false, true);
                    changeMiniPlayerTitleText(title, additionalText);
                    viewVerticalLine.setVisibility(View.VISIBLE);
                    for (LiveRadioUpdateListener listener : mLiveRadioUpdateListeners) {
                        if (listener != null)
                            listener.onLiveRadioUpdateListener(detail
                                    .getNextTrack());
                    }
                }
            } else {
                for (LiveRadioUpdateListener listener : mLiveRadioUpdateListeners) {
                    if (listener != null)
                        listener.onLiveRadioUpdateFailedListener();
                }
            }
        } catch (Exception e) {
            for (LiveRadioUpdateListener listener : mLiveRadioUpdateListeners) {
                if (listener != null)
                    listener.onLiveRadioUpdateFailedListener();
            }
            Logger.printStackTrace(e);
        }
    }

    @Override
    public void onLoadMenuMyOfflineSongs() {
        List<MediaItem> mMediaItems = null;

        if (CacheManager.isProUser(activity))
            mMediaItems = DBOHandler.getAllCachedTracks(activity);
        else
            mMediaItems = DBOHandler.getAllTracksForFreeUser(activity);

        List<Track> tracks = new ArrayList<Track>();
        for (MediaItem mediaItem : mMediaItems) {
            Track track = new Track(mediaItem.getId(), mediaItem.getTitle(),
                    mediaItem.getAlbumName(), mediaItem.getArtistName(),
                    mediaItem.getImageUrl(), mediaItem.getBigImageUrl(),
                    mediaItem.getImages(), mediaItem.getAlbumId());
            tracks.add(track);
        }
        if (tracks.size() > 0) {
            addToQueue(tracks, null, null);
        } else {
            displayNoDataDialog();
        }
        if (mDrawer!=null && mDrawer.isPanelExpanded())
            mDrawer.collapsePanel();
    }

    private void displayNoDataDialog() {
        try {
            CustomAlertDialog alertBuilder = new CustomAlertDialog(activity);
            alertBuilder
                    .setMessage(Utils.getMultilanguageTextLayOut(
                            mContext,
                            getResources().getString(
                                    R.string.no_offline_songs_for_online_1))
                            + "\n"
                            + Utils.getMultilanguageText(
                            mContext,
                            getResources()
                                    .getString(
                                            R.string.no_offline_songs_for_online_2)));
            alertBuilder.setNegativeButton(Utils.getMultilanguageTextLayOut(
                    mContext, getResources().getString(R.string.ok)), null);
            alertBuilder.show();
        } catch (Exception e) {
            Logger.printStackTrace(e);
        } catch (java.lang.Error e) {
            e.printStackTrace();
        }
    }

    public void updateTrackForOfflinePlay() {
        try {
            if (mPlayerService != null && getPlayMode() == PlayMode.MUSIC) {

                if (mPlayerService.isAdPlaying())
                    mPlayerService.clearAd();

                Track currentTrack = mPlayerService.getCurrentPlayingTrack();
                if (currentTrack == null) {
                    return;
                }

                CacheState cacheState = DBOHandler.getTrackCacheState(
                        activity.getApplicationContext(),
                        "" + currentTrack.getId());
                if (cacheState != CacheState.CACHED) {
                    if (mPlayerService != null
                            && !mPlayerService.isQueueEmpty()) {
                        int position = -1;
                        List<Track> trackList = getCurrentPlayingList();
                        for (int i = 0; i < trackList.size(); i++) {
                            Track track = trackList.get(i);
                            if (track != null) {
                                CacheState trackCacheState = DBOHandler
                                        .getTrackCacheState(activity
                                                .getApplicationContext(), ""
                                                + track.getId());
                                if (trackCacheState == CacheState.CACHED) {
                                    position = i;
                                    break;
                                }
                            }
                        }
                        if (position != -1) {
                            mPlayerService.playFromPositionNew(position);
                        } else if (mPlayerService != null && isPlaying()) {
                            mPlayerButtonPlay.performClick();
                            /*mPlayerButtonPlayRadio.performClick();
                            mPlayerButtonPlayRadioHandle.performClick();
                            mPlayerButtonPlayHandle.performClick();*/
                        }
                    } else {
                        if (mPlayerService != null && isPlaying()) {
                            mPlayerButtonPlay.performClick();
                            /*mPlayerButtonPlayRadio.performClick();
                            mPlayerButtonPlayRadioHandle.performClick();
                            mPlayerButtonPlayHandle.performClick();*/
                        }
                    }
                }
            } else {
                clearQueue();
                if (mPlayerService != null)
                    mPlayerService.addToQueue(new ArrayList<Track>());
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    public boolean isHandledActionOffline(final int action) {
        if (mApplicationConfigurations.getSaveOfflineMode()) {
            CustomAlertDialog alertBuilder = new CustomAlertDialog(activity);
            alertBuilder.setMessage(Utils.getMultilanguageText(
                    mContext,
                    getResources().getString(
                            R.string.caching_text_message_go_online_player)));
            alertBuilder.setPositiveButton(Utils.getMultilanguageText(
                            mContext,
                            getResources().getString(
                                    R.string.caching_text_popup_title_go_online)),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            if (Utils.isConnected()) {
                                mApplicationConfigurations
                                        .setSaveOfflineMode(false);

                                Map<String, String> reportMap = new HashMap<String, String>();
                                reportMap.put(
                                        FlurryConstants.FlurryCaching.Source
                                                .toString(),
                                        FlurryConstants.FlurryCaching.Prompt
                                                .toString());
                                reportMap
                                        .put(FlurryConstants.FlurryCaching.UserStatus
                                                .toString(), Utils
                                                .getUserState(activity));
                                Analytics.logEvent(
                                        FlurryConstants.FlurryCaching.GoOnline
                                                .toString(), reportMap);

                                Intent i = new Intent(
                                        MainActivity.ACTION_OFFLINE_MODE_CHANGED);
                                i.putExtra(MainActivity.IS_FROM_PLAYER_BAR,
                                        true);
                                i.putExtra(MainActivity.PLAYER_BAR_ACTION,
                                        action);
                                mContext.sendBroadcast(i);
                            } else {
                                CustomAlertDialog alertBuilder = new CustomAlertDialog(
                                        activity);
                                alertBuilder.setMessage(Utils
                                        .getMultilanguageText(
                                                mContext,
                                                getResources()
                                                        .getString(
                                                                R.string.go_online_network_error)));
                                alertBuilder.setNegativeButton(Utils
                                                .getMultilanguageText(mContext, "OK"),
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(
                                                    DialogInterface dialog,
                                                    int which) {
                                                startActivity(new Intent(
                                                        android.provider.Settings.ACTION_SETTINGS));
                                            }
                                        });
                                alertBuilder.show();
                            }
                        }
                    });
            alertBuilder.setNegativeButton(Utils.getMultilanguageText(
                    mContext,
                    getResources().getString(
                            R.string.caching_text_popup_button_cancel)), null);
            alertBuilder.show();
            return true;
        } else {
            return false;
        }
    }

    public void openDrawerWithAction(final int action) {
        if (mDrawer != null && !mDrawer.isPanelExpanded()) {
            mDrawer.expandPanel();
        }
    }

    public void startAutoSavingPlayerQueue() {
        if (mPlayerService != null)
            mPlayerService.startAutoSavingPlayerQueue();
    }

    @SuppressLint("NewApi")
    private void setAudioAdImage(final Placement audioAdPlacement) {
        try {
            if (audioAdPlacement != null) {
                DisplayMetrics metrics = null;
                if (HomeActivity.metrics == null) {
                    metrics = new DisplayMetrics();
                    activity.getWindowManager().getDefaultDisplay()
                            .getMetrics(metrics);
                } else {
                    metrics = HomeActivity.metrics;
                }

                String adImageUrl = Utils.getDisplayProfile(metrics,
                        audioAdPlacement);
                Logger.i("adImageUrl", "adImageUrl:" + adImageUrl);
                if (adImageUrl != null) {
                    if (mContext != null && !TextUtils.isEmpty(adImageUrl)) {
                        mDrawerMediaArt.setVisibility(View.VISIBLE);
                        Picasso.with(mContext)
                                .load(adImageUrl)
                                .placeholder(
                                        R.drawable.icon_main_player_no_content)
                                .into(mDrawerMediaArt, new Callback() {
                                    @Override
                                    public void onSuccess() {
                                        if (lastAdReportingTime == 0
                                                || (System.currentTimeMillis() - lastAdReportingTime) >= (mApplicationConfigurations
                                                .getAppConfigPlayerOverlayRefresh() - 2) * 1000) {
                                            lastAdReportingTime = System
                                                    .currentTimeMillis();
                                            Utils.postViewEvent(activity,
                                                    audioAdPlacement);
                                        }

                                        mDrawerMediaArt
                                                .setOnClickListener(new OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        try {
                                                            Utils.performclickEvent(
                                                                    activity,
                                                                    audioAdPlacement);
                                                        } catch (Exception e) {
                                                            Logger.printStackTrace(e);
                                                        }
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onError() {

                                    }
                                });
                    }
                    if (adHolder != null)
                        adHolder.setVisibility(View.INVISIBLE);
                    if (adHolderOuter != null)
                        adHolderOuter.setVisibility(View.INVISIBLE);
                    if (dontWant != null)
                        dontWant.setVisibility(View.INVISIBLE);

                }
            }
        } catch (Exception e) {
        } catch (java.lang.Error e) {
        }
    }

    class PlayerStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_PLAY_STATE_CHANGED)) {
                if (getPlayerState() == State.PLAYING) {
                    togglePlayerPlayIcon(true);
                } else if (getPlayerState() == State.PAUSED || getPlayerState() == State.STOPPED) {
                    togglePlayerPlayIcon(false);
                }
            }
        }
    }

    // --------------------AddedOn24thNov2014_By_Kautik-------------------//

    private void refreshFlipImages() {
        PlayMode pm = null;
        if (mPlayerService != null)
            pm = mPlayerService.getPlayMode();
        Utils.clearCache();
        Logger.i("refreshFlipImages", "refreshFlipImages Called");
        if (mPlayerService != null) {
            setDrawerPanelHeight();

            if (pm == PlayMode.MUSIC) {
                fancyCoverFlow.setVisibility(View.VISIBLE);
                List<Track> queue = getCurrentPlayingList();
                // if (mDrawer.isPanelExpanded()) {
                if (queue != null && queue.size() > 0) {
                    coverFlowAdapter.notifyDataSetChanged();
                    int pos;
                    if (mPlayerService.isPlaying()) {
                        pos = getCurrentPlayingInQueuePosition();
                        if (pos < queue.size() && coverFlowAdapter != null) {
                            fancyCoverFlow.setSelection(pos, true);
                            setBlurImage();
                        }
                    }
                } else {
                    coverFlowAdapter.notifyDataSetChanged();
                }
            } else {
                if (pm == PlayMode.MUSIC) {
                    fancyCoverFlow.setVisibility(View.GONE);
                    coverFlowAdapter.notifyDataSetChanged();
                }
            }
        } else {
            if (pm != null && pm == PlayMode.MUSIC) {
                coverFlowAdapter.notifyDataSetChanged();
            }
            clearBlurBg();
        }
        Utils.clearCache();
    }

    private void playButtonImageUpdate() {
        ProgressBar pbPlay = (ProgressBar) rootView.findViewById(R.id.pbPlay);
        ProgressBar pbPlayRadio = (ProgressBar) rootView
                .findViewById(R.id.pbPlayRadio);
        ProgressBar pbPlayMiniPlayer = (ProgressBar) miniPlayerView
                .findViewById(R.id.pbPlayMiniPlayer);
        ProgressBar pbPlayMiniPlayerRadio = (ProgressBar) miniPlayerView
                .findViewById(R.id.pbPlayMiniPlayerRadio);
        if (mPlayerService.isAdPlaying()) {
            pbPlay.setVisibility(View.INVISIBLE);
            pbPlayRadio.setVisibility(View.INVISIBLE);
            pbPlayMiniPlayer.setVisibility(View.INVISIBLE);
            pbPlayMiniPlayerRadio.setVisibility(View.INVISIBLE);
            if (PlayerService.service.getState() == State.INTIALIZED) {
                mPlayerButtonPlay.setVisibility(View.INVISIBLE);

                if (mPlayerService != null
                        && (mPlayerService.getPlayMode() == PlayMode.MUSIC
                        || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || mPlayerService
                        .getPlayMode() == PlayMode.DISCOVERY_MUSIC))
                    pbPlay.setVisibility(View.VISIBLE);
                else
                    pbPlayRadio.setVisibility(View.VISIBLE);
                mPlayerButtonPlayHandle.setVisibility(View.INVISIBLE);
                pbPlayMiniPlayer.setVisibility(View.VISIBLE);
            } else {
                if (mPlayerService != null
                        && (mPlayerService.getPlayMode() == PlayMode.MUSIC
                        || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || mPlayerService
                        .getPlayMode() == PlayMode.DISCOVERY_MUSIC))
                    mPlayerButtonPlayHandle.setVisibility(View.VISIBLE);
                else
                    mPlayerButtonPlayHandle.setVisibility(View.INVISIBLE);
            }
            return;
        }
        List<Track> queue = getCurrentPlayingList();
        if (queue != null && queue.size() > 0 && mPlayerService != null) {
            pbPlay.setVisibility(View.INVISIBLE);
            pbPlayRadio.setVisibility(View.INVISIBLE);
            pbPlayMiniPlayer.setVisibility(View.INVISIBLE);
            pbPlayMiniPlayerRadio.setVisibility(View.INVISIBLE);
            if (PlayerService.service.getState() == State.INTIALIZED) {
                mPlayerButtonPlay.setVisibility(View.INVISIBLE);
                mPlayerButtonPlayRadioHandle.setVisibility(View.INVISIBLE);
                if (mPlayerService != null
                        && (mPlayerService.getPlayMode() == PlayMode.MUSIC
                        || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || mPlayerService
                        .getPlayMode() == PlayMode.DISCOVERY_MUSIC))
                    pbPlay.setVisibility(View.VISIBLE);
                else
                    pbPlayRadio.setVisibility(View.VISIBLE);

                mPlayerButtonPlayHandle.setVisibility(View.INVISIBLE);

                if (mPlayerService != null
                        && (mPlayerService.getPlayMode() == PlayMode.MUSIC
                        || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || mPlayerService
                        .getPlayMode() == PlayMode.DISCOVERY_MUSIC))
                    pbPlayMiniPlayer.setVisibility(View.VISIBLE);
                else
                    pbPlayMiniPlayerRadio.setVisibility(View.VISIBLE);
            } else {

                if (mPlayerService != null
                        && (mPlayerService.getPlayMode() == PlayMode.MUSIC
                        || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || mPlayerService
                        .getPlayMode() == PlayMode.DISCOVERY_MUSIC)) {
                    mPlayerButtonPlayRadioHandle.setVisibility(View.INVISIBLE);
                    mPlayerButtonPlayHandle.setVisibility(View.VISIBLE);
                    mPlayerButtonPlay.setVisibility(View.VISIBLE);
                } else {
                    mPlayerButtonPlayRadioHandle.setVisibility(View.VISIBLE);
                    mPlayerButtonPlayHandle.setVisibility(View.INVISIBLE);
                    mPlayerButtonPlay.setVisibility(View.INVISIBLE);
                }
            }
        } else {
            pbPlay.setVisibility(View.INVISIBLE);
            pbPlayRadio.setVisibility(View.INVISIBLE);
            mPlayerButtonPlay.setVisibility(View.VISIBLE);
            mPlayerButtonPlayHandle.setVisibility(View.INVISIBLE);
            pbPlayMiniPlayer.setVisibility(View.INVISIBLE);
            pbPlayMiniPlayerRadio.setVisibility(View.INVISIBLE);
            if (mPlayerService != null
                    && (mPlayerService.getPlayMode() == PlayMode.MUSIC
                    || mPlayerService.getPlayMode() == PlayMode.TOP_ARTISTS_RADIO || mPlayerService
                    .getPlayMode() == PlayMode.DISCOVERY_MUSIC))
                mPlayerButtonPlayHandle.setVisibility(View.VISIBLE);
            else
                mPlayerButtonPlayHandle.setVisibility(View.INVISIBLE);
        }
    }

    private String bgImgUrl;

    public void setBlurImage() {

        Utils.clearCache();
        if (mPlayerService != null && mPlayerService.getPlayingQueue() != null
                && mPlayerService.getPlayingQueue().size() > 0) {
            try {
                Track track = mPlayerService.getCurrentPlayingTrack();
                String imgUrl = getImageUrl(track);
                if (bgImgUrl == null
                        || (bgImgUrl != null && !bgImgUrl.equals(imgUrl))) {
                    Logger.i("setBlurImage", "setBlurImage");
                    loadBlurImgBitmap(imgUrl);
                }
            } catch (Exception e) {
                clearBlurBg();
            }
        } else
            clearBlurBg();
    }

    private String getImgUrl(Track track) {
        return getImageUrl(track);
    }

    private void setRadioBlurImage(Track track) {
        Logger.i("setRadioBlurImage", "setRadioBlurImage");
        Utils.clearCache();
        try {
            String imgUrl = getImageUrl(track);
            loadBlurImgBitmap(imgUrl);
        } catch (Exception e) {
            Logger.printStackTrace(e);
            clearBlurBg();
        }
    }

    GetAndSetBlurImage getAndSetBlurImage;

    static String loadedURL = "";
    static Drawable blurbitmap;
    String url;

    // static Bitmap normalBitmap;

    private void loadBlurImgBitmap(final String url) {
        this.url = url;
        if (!TextUtils.isEmpty(url))
            PicassoUtil.with(activity).loadWithoutConfig8888(url,
                    width_coverFlow, width_coverFlow, target);
    }

    PicassoTarget target = new PicassoTarget() {

        @Override
        public void onPrepareLoad(Drawable arg0) {

        }

        @Override
        public void onBitmapLoaded(Bitmap arg0, LoadedFrom arg1) {
            loadedURL = url;
            // Log.i("Bitmap", "BlurImgBitmap:" + arg0 + " ::: url:" + url);
            getAndSetBlurImage = new GetAndSetBlurImage(arg0, url);
            ThreadPoolManager.getInstance().submit(getAndSetBlurImage);
//            getAndSetBlurImage.start();
        }

        @Override
        public void onBitmapFailed(Drawable arg0) {
            // TODO Auto-generated method stub
            bgImgUrl = null;
            Logger.i("Bitmap", "Error BlurImgBitmap");
        }
    };

    void loadBlurBG(final Bitmap bitmap1, final Drawable loadBitmap,
                    final String url) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    RelativeLayout rlFlipView = (RelativeLayout) activity
                            .findViewById(R.id.dragView);
                    if (Build.VERSION.SDK_INT > 15) {
                        rlFlipView.setBackground(loadBitmap);
                    } else {
                        rlFlipView.setBackgroundDrawable(loadBitmap);
                    }
//					if (blurbitmap != loadBitmap) {
                    blurbitmap = loadBitmap;
                    loadedURL = url;

//					}
                } catch (Exception e) {
                    Logger.printStackTrace(e);
                }
            }
        });

    }

    private class GetAndSetBlurImage implements Runnable {
        Bitmap bitmap;

        String url;

        public GetAndSetBlurImage(Bitmap bitmap, String url) {
            this.bitmap = bitmap;
            this.url = url;
        }

        protected Drawable doInBackground(String... urls) {
            try {
                int oldBitmpWidth = bitmap.getWidth();
                Display dis = activity.getWindowManager().getDefaultDisplay();
                float screenWidthRatio = (float) dis.getHeight()
                        / (float) dis.getWidth();
                int newBitmpWidth = (int) (bitmap.getWidth() / screenWidthRatio);
                bitmap = Bitmap.createBitmap(bitmap,
                        ((oldBitmpWidth - newBitmpWidth) / 2), 0,
                        newBitmpWidth, bitmap.getHeight());

                Bitmap loadBitmap = bitmap;
                try {
                    loadBitmap = Utils.fastblur1(loadBitmap,
                            Constants.BLUR_IMG_RADIUS, activity);
                } catch (OutOfMemoryError e) {
                    System.gc();
                } catch (Exception e) {
                    loadBitmap = bitmap;
                }
                Bitmap displayBitmp = loadBitmap;
                Drawable dr = new BitmapDrawable(displayBitmp);
                bgImgUrl = url;
                return dr;
            } catch (java.lang.Error e) {
            }
            return null;
        }

        @Override
        public void run() {
            try {
                final Drawable loadBitmap = doInBackground();
                if (loadBitmap != null && activity != null)
                    loadBlurBG(bitmap, loadBitmap, url);
                else {
                    loadedURL = null;
                    blurbitmap = null;
                    // normalBitmap=null;
                }
            } catch (Exception e) {
            }
        }

    }

    class ViewHolderItem {
        ImageView ivArt, ivAdd, iv_item_player_shadow;
        ImageButton item_player_content_media_play;
        TextView tvTitle;
        TextView tvAdditionalInfo;
        RelativeLayout rlCoverItemInner, rlItemInfoCover;
        ProgressBar progressBar1;
        Button bClosedAds;
        public RelativeLayout rlItemInfoCoverEnglish;
    }

    public class CoverFlowAdapterNew extends BaseAdapter {
        int mGalleryItemBackground;
        LayoutInflater inflater;

        public int getCount() {
            if (mPlayerService != null
                    && mPlayerService.getPlayingQueue().size() > 0) {
                return mPlayerService.getPlayingQueue().size();
            } else {
                return 0;
            }
        }

        CoverFlowAdapterNew() {
            inflater = (activity).getLayoutInflater();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderItem viewHolder;

            if (convertView == null) {

                // inflate the layout

                convertView = inflater.inflate(
                        R.layout.item_full_player_cover_flow, parent, false);

                // well set up the ViewHolder
                viewHolder = new ViewHolderItem();
                viewHolder.ivArt = (ImageView) convertView
                        .findViewById(R.id.item_player_content_media_art);
                viewHolder.ivAdd = (ImageView) convertView
                        .findViewById(R.id.item_player_content_add);
                viewHolder.iv_item_player_shadow = (ImageView) convertView
                        .findViewById(R.id.iv_item_player_shadow);
                viewHolder.item_player_content_media_play = (ImageButton) convertView
                        .findViewById(R.id.item_player_content_media_play);

                viewHolder.rlItemInfoCover = (RelativeLayout) convertView
                        .findViewById(R.id.rlItemInfoCover);
                viewHolder.rlItemInfoCoverEnglish = (RelativeLayout) convertView
                        .findViewById(R.id.rlItemInfoCover_English);
                if (isEnglish) {
                    viewHolder.tvTitle = (TextView) viewHolder.rlItemInfoCoverEnglish
                            .findViewById(R.id.item_content_info_bar_text_title_english);
                    viewHolder.tvAdditionalInfo = (TextView) viewHolder.rlItemInfoCoverEnglish
                            .findViewById(R.id.item_content_info_bar_text_additional_english);
                    viewHolder.rlItemInfoCover.setVisibility(View.INVISIBLE);
                    viewHolder.rlItemInfoCoverEnglish
                            .setVisibility(View.VISIBLE);
                } else {
                    viewHolder.tvTitle = (LanguageTextView) viewHolder.rlItemInfoCover
                            .findViewById(R.id.item_content_info_bar_text_title);
                    viewHolder.tvAdditionalInfo = (LanguageTextView) viewHolder.rlItemInfoCover
                            .findViewById(R.id.item_content_info_bar_text_additional);
                    viewHolder.rlItemInfoCover.setVisibility(View.VISIBLE);
                    viewHolder.rlItemInfoCoverEnglish
                            .setVisibility(View.INVISIBLE);
                }
                viewHolder.rlCoverItemInner = (RelativeLayout) convertView
                        .findViewById(R.id.rlCoverItemInner);

                viewHolder.progressBar1 = (ProgressBar) convertView
                        .findViewById(R.id.progressBar1);
                viewHolder.bClosedAds = (Button) convertView
                        .findViewById(R.id.bCloseAd);
                // store the holder with the view.
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }
            viewHolder.rlCoverItemInner.getLayoutParams().height = width_coverFlow;
            viewHolder.rlCoverItemInner.getLayoutParams().width = width_coverFlow;
            final ImageView ivArt = viewHolder.ivArt;
            ImageView ivAdd = viewHolder.ivAdd;
            ImageView iv_item_player_shadow = viewHolder.iv_item_player_shadow;

            convertView.setLayoutParams(new CoverFlow.LayoutParams(
                    width_coverFlow, width_coverFlow));

            // convertView.setBackgroundColor(Color.WHITE);
            iv_item_player_shadow.setVisibility(View.VISIBLE);
            ivAdd.setVisibility(View.GONE);
            try {

                final Track track = mPlayerService.getPlayingQueue().get(
                        position);
                String imgUrl = getImgUrl(track);

                ImageButton item_player_content_media_play = viewHolder.item_player_content_media_play;
                int currentPos = mPlayerService.getCurrentQueuePosition();
                if (isGymMode) {
                    item_player_content_media_play.setVisibility(View.GONE);
                    viewHolder.progressBar1.setVisibility(View.GONE);
                } else {
                    if (currentPos == position) {
                        item_player_content_media_play.setVisibility(View.GONE);
                        if (PlayerService.service != null
                                && PlayerService.service.getState() == State.INTIALIZED)
                            viewHolder.progressBar1.setVisibility(View.VISIBLE);
                        else
                            viewHolder.progressBar1.setVisibility(View.GONE);
                    } else {
                        item_player_content_media_play
                                .setVisibility(View.VISIBLE);
                        viewHolder.progressBar1.setVisibility(View.GONE);
                    }
                }

                if ((flipPos == position) && isFlip && backgroundImage != null) {
                    ivArt.setImageDrawable(backgroundImage);
                    viewHolder.bClosedAds.setVisibility(View.VISIBLE);
                    viewHolder.ivArt.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Utils.performclickEvent(activity, placement);
                        }
                    });
                    viewHolder.rlItemInfoCover.setVisibility(View.INVISIBLE);
                    viewHolder.rlItemInfoCoverEnglish
                            .setVisibility(View.INVISIBLE);
                    viewHolder.iv_item_player_shadow
                            .setVisibility(View.INVISIBLE);
                    item_player_content_media_play
                            .setVisibility(View.INVISIBLE);
                } else {
                    if (imgUrl != null && !TextUtils.isEmpty(imgUrl)) {
                        try {
                            if (picasso == null)
                                picasso = PicassoUtil.with(activity);

                            picasso.load(null, imgUrl, ivArt,
                                    R.drawable.icon_main_player_no_content);

                        } catch (OutOfMemoryError e) {
                            Utils.clearCache();
                        }
                    } else {
                        ivArt.setImageResource(R.drawable.icon_main_player_no_content);
                    }
                    viewHolder.bClosedAds.setVisibility(View.GONE);
                    viewHolder.ivArt.setOnClickListener(null);
                    viewHolder.rlItemInfoCover.setVisibility(View.VISIBLE);
                    viewHolder.iv_item_player_shadow
                            .setVisibility(View.VISIBLE);
                    if (currentPos == position)
                        item_player_content_media_play.setVisibility(View.GONE);
                    else
                        item_player_content_media_play
                                .setVisibility(View.VISIBLE);
                }

                if (isFlip) {
                    viewHolder.ivArt.setClickable(true);
                } else {
                    viewHolder.ivArt.setClickable(false);
                }

                viewHolder.bClosedAds.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            adHolder.setVisibility(View.INVISIBLE);
                            adHolderOuter.setVisibility(View.INVISIBLE);
                            displayFlipAds(null);
                            if (!isUpgrading) {
                                startAdsFlipTimer();
                            }
                        } catch (Exception e) {
                            Logger.printStackTrace(e);
                        }
                    }
                });

                final int i = position;
                item_player_content_media_play
                        .setOnClickListener(new OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if (mPlayerService != null
                                        && mPlayerService.isAdPlaying())
                                    return;
                                CacheState cacheState = DBOHandler
                                        .getTrackCacheState(activity
                                                .getApplicationContext(), ""
                                                + track.getId());
                                if (mApplicationConfigurations
                                        .getSaveOfflineMode()
                                        && cacheState != CacheState.CACHED) {
                                    displayOfflineDialog(i);
                                    return;
                                }
                                try {
                                    if (mPlayerService.isPlaying())
                                        playFromPosition(i);
                                    else
                                        mPlayerService.playFromPositionNew(i);
                                    Map<String, String> reportMap1 = new HashMap<String, String>();
                                    reportMap1
                                            .put(FlurryFullPlayerParams.SongName
                                                    .toString(), track
                                                    .getTitle());
                                    Analytics
                                            .logEvent(
                                                    FlurryFullPlayerParams.FullPlayerPlay
                                                            .toString(),
                                                    reportMap1);
                                } catch (Exception e) {
                                }
                            }
                        });

                if (activity != null) {
                    if (isEnglish) {
                        viewHolder.tvTitle.setText(track.getTitle());
                        viewHolder.tvAdditionalInfo.setText(track
                                .getAlbumName());
                    } else {
                        viewHolder.tvTitle.setText(Utils
                                .getMultilanguageTextLayOut(activity,
                                        track.getTitle()));
                        viewHolder.tvAdditionalInfo.setText(Utils
                                .getMultilanguageTextLayOut(activity,
                                        track.getAlbumName()));
                    }
                }
            } catch (Exception e) {
            }
            return convertView;
        }

        private void displayOfflineDialog(final int position) {
            final ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
                    .getInstance(mContext);
            if (mApplicationConfigurations.getSaveOfflineMode()) {
                CustomAlertDialog alertBuilder = new CustomAlertDialog(activity);
                alertBuilder
                        .setMessage(Utils
                                .getMultilanguageTextHindi(
                                        mContext,
                                        getResources()
                                                .getString(
                                                        R.string.caching_text_message_go_online_player)));
                alertBuilder.setPositiveButton(Utils.getMultilanguageTextHindi(
                                mContext,
                                getResources().getString(
                                        R.string.caching_text_popup_title_go_online)),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                if (Utils.isConnected()) {
                                    mApplicationConfigurations
                                            .setSaveOfflineMode(false);

                                    Map<String, String> reportMap = new HashMap<String, String>();
                                    reportMap
                                            .put(FlurryConstants.FlurryCaching.Source
                                                            .toString(),
                                                    FlurryConstants.FlurryCaching.Prompt
                                                            .toString());
                                    reportMap
                                            .put(FlurryConstants.FlurryCaching.UserStatus
                                                    .toString(), Utils
                                                    .getUserState(activity));
                                    Analytics
                                            .logEvent(
                                                    FlurryConstants.FlurryCaching.GoOnline
                                                            .toString(),
                                                    reportMap);

                                    Intent i = new Intent(
                                            MainActivity.ACTION_OFFLINE_MODE_CHANGED);
                                    i.putExtra(
                                            MainActivity.IS_FROM_PLAYER_QUEUE,
                                            true);
                                    i.putExtra(MainActivity.PLAY_FROM_POSITION,
                                            position);
                                    mContext.sendBroadcast(i);
                                } else {
                                    CustomAlertDialog alertBuilder = new CustomAlertDialog(
                                            activity);
                                    alertBuilder.setMessage(Utils
                                            .getMultilanguageText(
                                                    mContext,
                                                    getResources()
                                                            .getString(
                                                                    R.string.go_online_network_error)));
                                    alertBuilder.setNegativeButton(
                                            Utils.getMultilanguageText(
                                                    mContext, "OK"),
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(
                                                        DialogInterface dialog,
                                                        int which) {
                                                    startActivity(new Intent(
                                                            android.provider.Settings.ACTION_SETTINGS));
                                                }
                                            });
                                    alertBuilder.show();
                                }
                            }
                        });
                alertBuilder.setNegativeButton(Utils.getMultilanguageText(
                                mContext,
                                getResources().getString(
                                        R.string.caching_text_popup_button_cancel)),
                        null);
                alertBuilder.show();
            }
        }
    }

    // -------------------MoreMenuItemSelection--------------------//

    public void onMoreMenuShareSelected() {
        if (!isHandledActionOffline(ACTION_SHARE)) {
            if (mPlayerService != null) {
                if (mDataManager.isDeviceOnLine()) {
                    try {
                        Track track = mPlayerService.getCurrentPlayingTrack();

                        // Prepare data for ShareDialogFragmnet
                        Map<String, Object> shareData = new HashMap<String, Object>();
                        shareData.put(ShareDialogFragment.TITLE_DATA,
                                track.getTitle());
                        shareData.put(ShareDialogFragment.SUB_TITLE_DATA,
                                track.getAlbumName());
                        // shareData.put(ShareDialogFragment.THUMB_URL_DATA,
                        // track.getBigImageUrl());
                        shareData.put(ShareDialogFragment.THUMB_URL_DATA,
                                ImagesManager.getMusicArtBigImageUrl(track
                                        .getImagesUrlArray()));
                        shareData.put(ShareDialogFragment.MEDIA_TYPE_DATA,
                                MediaType.TRACK);
                        shareData.put(ShareDialogFragment.CONTENT_ID_DATA,
                                track.getId());

                        // Show ShareFragmentActivity
                        ShareDialogFragment shareDialogFragment = ShareDialogFragment
                                .newInstance(shareData,
                                        FlurryConstants.FlurryShare.FullPlayer
                                                .toString());
                        shareDialogFragment.show(mFragmentManager,
                                ShareDialogFragment.FRAGMENT_TAG);

                        Map<String, String> reportMap1 = new HashMap<String, String>();
                        reportMap1
                                .put(FlurryFullPlayerParams.OptionSelected
                                                .toString(),
                                        FlurryFullPlayerParams.Share
                                                .toString());
                        Analytics
                                .logEvent(
                                        FlurryFullPlayerParams.FullPlayerMore
                                                .toString(), reportMap1);
                    } catch (Exception e) {
                    }
                } else {
                    Utils.makeText(
                            activity,
                            getResources().getString(
                                    R.string.player_error_no_connectivity),
                            Toast.LENGTH_LONG).show();
                }

            }
        }
    }

    public void onMoreMenuSimilarSelected() {
        mDrawerActionSimilar.performClick();
        Map<String, String> reportMap1 = new HashMap<String, String>();
        reportMap1.put(FlurryFullPlayerParams.OptionSelected
                .toString(), FlurryFullPlayerParams.Similar
                .toString());
        Analytics.logEvent(
                FlurryFullPlayerParams.FullPlayerMore
                        .toString(), reportMap1);
    }

    public void onMoreMenuTriviaSelected() {
        mDrawerActionTrivia.performClick();
        Map<String, String> reportMap1 = new HashMap<String, String>();
        reportMap1.put(FlurryFullPlayerParams.OptionSelected
                .toString(), FlurryFullPlayerParams.Trivia
                .toString());
        Analytics.logEvent(
                FlurryFullPlayerParams.FullPlayerMore
                        .toString(), reportMap1);
    }

    public void onMoreMenuLyricsSelected() {
        mDrawerActionLyrics.performClick();
        Map<String, String> reportMap1 = new HashMap<String, String>();
        reportMap1.put(FlurryFullPlayerParams.OptionSelected
                .toString(), FlurryFullPlayerParams.Lyrics
                .toString());
        Analytics.logEvent(
                FlurryFullPlayerParams.FullPlayerMore
                        .toString(), reportMap1);
    }

    public void onMoreMenuInfoSelected() {
        mDrawerActionInfo.performClick();
        Map<String, String> reportMap1 = new HashMap<String, String>();
        reportMap1.put(FlurryFullPlayerParams.OptionSelected
                .toString(), FlurryFullPlayerParams.Info
                .toString());
        Analytics.logEvent(
                FlurryFullPlayerParams.FullPlayerMore
                        .toString(), reportMap1);
    }

    public void onMoreMenuAlbumSelected() {
        mDrawerActionAlbum.performClick();
    }

    public void onMoreMenuCommentSelected() {
        if (!isHandledActionOffline(ACTION_COMMENT)) {
            if (mPlayerService != null) {
                // gets the current playing track and shows the comments for
                // it.
                Track track = mPlayerService.getCurrentPlayingTrack();
                if (track != null) {
                    MediaItem mediaItem = new MediaItem(track.getId(),
                            track.getTitle(), track.getAlbumName(),
                            track.getArtistName(), getImgUrl(track),
                            track.getBigImageUrl(), MediaType.TRACK.name()
                            .toLowerCase(), 0, 0, track.getImages(),
                            track.getAlbumId());
                    openCommentsPage(mediaItem);
                }
                // closeContent();
                closeContentWithoutCollapsedPanel();
            }
            Map<String, String> reportMap1 = new HashMap<String, String>();
            reportMap1.put(
                    FlurryFullPlayerParams.OptionSelected
                            .toString(),
                    FlurryFullPlayerParams.Comment.toString());
            Analytics.logEvent(
                    FlurryFullPlayerParams.FullPlayerMore
                            .toString(), reportMap1);
        }
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
        Logger.i("onPanelSlide", "onPanelSlide: SlideOffset:" + slideOffset);
        if (slideOffset > 0.5) {
            ActionBar actionBar = ((MainActivity) activity)
                    .getSupportActionBar();
            if (actionBar.isShowing())
                actionBar.hide();

        } else {
            ActionBar actionBar = ((MainActivity) activity)
                    .getSupportActionBar();
            if (!actionBar.isShowing())
                actionBar.show();
        }
        showHideHandlPart(true, slideOffset);
    }

    @Override
    public void onPanelAnchored(View panel) {

    }

    @Override
    public void onPanelHidden(View panel) {
    }

    private void showFirstTimePlayerDragHelp() {
        if (!mApplicationConfigurations.isFullPlayerDragHelp()) {
            commonHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    if (mDrawer!=null && mDrawer.isPanelExpanded()) {
                        try {
                            mDrawer.lock();
                            View Settings = miniPlayerView
                                    .findViewById(R.id.main_player_content_info_bar);
                            Settings.setDrawingCacheEnabled(false);
                            Settings.setDrawingCacheEnabled(true);
                            Bitmap bmp = Settings.getDrawingCache();

                            AppGuideActivityPlayerBar.bitmapHelpView = bmp;

                            if (mDrawer.isPanelExpanded()) {
                                Intent intent = new Intent(activity,
                                        AppGuideActivityPlayerBar.class);
                                startActivityForResult(intent, 10001);
                                mApplicationConfigurations
                                        .setFullPlayerDragHelp(true);
                                isHintVisible = true;
                            }
                        } catch (Exception e) {
                            Logger.printStackTrace(e);
                        }
                    }
                }
            }, 1000);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 10001) {
            if (mDrawer != null)
                mDrawer.unlock();
            isHintVisible = false;
            ShowHint();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void changeMiniPlayerTitleText(String title, String titleAdditional) {
        if (getActivity() == null || activity == null)
            return;
        if (mPlayerService != null && mPlayerService.isAdPlaying()) {
            mPlayerTextTitleHandle.setText(advertiseTxt);
            mDrawerTextTitle.setText(advertiseTxt);
            mDrawerTextTitleHandle.setText(advertiseTxt);
            mDrawerTextTitleRadio.setText(advertiseTxt);
            mDrawerTextAdditionalHandle.setVisibility(View.GONE);
            mDrawerTextAdditionalHandleRadio.setVisibility(View.GONE);
            lastUsedTrackDetailForTitleFlip = -1;
            lastUsedTrackDetailForMiniPlayer = -1;
            return;
        }

        if (getPlayMode() != PlayMode.LIVE_STATION_RADIO
                && mCurrentTrackDetails != null
                && !TextUtils.isEmpty(title)
                && !title
                .equals(activity.getResources().getString(R.string.main_player_bar_text_not_playing))) {
            if (lastUsedTrackDetailForMiniPlayer == mCurrentTrackDetails
                    .getId())
                return;
            lastUsedTrackDetailForMiniPlayer = mCurrentTrackDetails.getId();
        } else {
            lastUsedTrackDetailForMiniPlayer = -1;
        }
        if (title != null && title.equals(Utils.TEXT_EMPTY)) {
            mDrawerTextAdditionalHandle.setVisibility(View.VISIBLE);
            mDrawerTextAdditionalHandleRadio.setVisibility(View.VISIBLE);
            mDrawerTextAdditionalHandle.setText(Utils.TEXT_EMPTY);
            mDrawerTextAdditionalHandleRadio.setText(Utils.TEXT_EMPTY);
            mPlayerTextTitleHandle.setText(Utils.TEXT_EMPTY);
            mDrawerTextTitle.setText(Utils.TEXT_EMPTY);
            mDrawerTextTitleHandle.setText(Utils.TEXT_EMPTY);
            mDrawerTextTitleRadio.setText(Utils.TEXT_EMPTY);
            return;
        }
        if (title != null && !title.equals("")) {

            String miniPlayertext = "";
            if (mPlayerService != null
                    && mPlayerService.getCurrentPlayingTrack() != null
                    && getPlayMode() == PlayMode.LIVE_STATION_RADIO
                    && detail != null) {
                Track track = mPlayerService.getCurrentPlayingTrack();
                String temp_title = "";
                MediaItem mediaItem = (MediaItem) track.getTag();
                if (mediaItem != null) {
                    String title1 = mediaItem.getTitle();
                    if (title1 != null) {
                        temp_title = Utils.getMultilanguageTextLayOut(mContext,
                                title1);
                        temp_title = temp_title + " - ";
                    }
                }
                String trackName = detail.getTrack();
                if (mediaItem != null)
                    title = mediaItem.getTitle();
                titleAdditional = detail.getAlbum();
                miniPlayertext = temp_title + titleAdditional + " - "
                        + trackName;
                titleAdditional = blueTitleLiveRadio + " - " + titleAdditional;
            } else if (mPlayerService != null
                    && mPlayerService.getCurrentPlayingTrack() != null
                    && getPlayMode() == PlayMode.TOP_ARTISTS_RADIO) {
                titleAdditional = mPlayerService.getCurrentPlayingTrack()
                        .getArtistName();
                String titleTemp = mPlayerService.getCurrentPlayingTrack()
                        .getTitle();
                if (titleAdditional != null && !titleAdditional.equals(""))
                    miniPlayertext = blueTitleTopOnDemandRadio + " - "
                            + titleAdditional + " - " + titleTemp;
                else
                    miniPlayertext = title;
                title = blueTitleTopOnDemandRadio;

            } else {
                if (titleAdditional != null && !titleAdditional.equals(""))
                    miniPlayertext = title + " - " + titleAdditional;
                else
                    miniPlayertext = title;
            }
            mPlayerTextTitleHandle.setText(miniPlayertext);
            mDrawerTextTitle.setText(title);
            mDrawerTextTitleHandle.setText(title);
            mDrawerTextTitleRadio.setText(title);
        } else {
            if (mPlayerService != null
                    && mPlayerService.getCurrentPlayingTrack() != null
                    && getPlayMode() == PlayMode.LIVE_STATION_RADIO) {
                Track track = mPlayerService.getCurrentPlayingTrack();
                String temp_title = "";
                MediaItem mediaItem = (MediaItem) track.getTag();
                if (mediaItem != null) {
                    String title1 = mediaItem.getTitle();
                    if (title1 != null) {
                        temp_title = Utils.getMultilanguageTextLayOut(mContext,
                                title1);
                    }
                }
                String text = temp_title + " - " + titleAdditional;
                titleAdditional = blueTitleLiveRadio;

                mPlayerTextTitleHandle.setText(text);
                mDrawerTextTitle.setText(title);
                mDrawerTextTitleHandle.setText(title);
                mDrawerTextTitleRadio.setText(title);
            }
        }
        if (titleAdditional != null && !titleAdditional.equals("")) {
            mDrawerTextAdditionalHandle.setVisibility(View.VISIBLE);
            mDrawerTextAdditionalHandleRadio.setVisibility(View.VISIBLE);
            mDrawerTextAdditionalHandle.setText(titleAdditional);
            mDrawerTextAdditionalHandleRadio.setText(titleAdditional);
        } else {
            mDrawerTextAdditionalHandle.setText(Utils.TEXT_EMPTY);
            mDrawerTextAdditionalHandleRadio.setText(Utils.TEXT_EMPTY);
        }
        if (!mPlayerTextTitleHandle.isSelected()) {
            mPlayerTextTitleHandle.setSelected(true);
            mPlayerTextTitleHandle.setSingleLine();
            mPlayerTextTitleHandle.setEllipsize(TruncateAt.MARQUEE);
            mPlayerTextTitleHandle.setHorizontallyScrolling(true);
        }
    }

    String currentTitle;

    @Override
    public void onMoreItemSelected(String item) {
        if (item.equals("Share")) {
            onMoreMenuShareSelected();
        } else if (item.equals("Similar")) {
            onMoreMenuSimilarSelected();
        } else if (item.equals("Trivia")) {
            onMoreMenuTriviaSelected();
        } else if (item.equals("Lyrics")) {
            onMoreMenuLyricsSelected();
        } else if (item.equals("Info")) {
            onMoreMenuInfoSelected();
        } else if (item.equals("Comments")) {
            onMoreMenuCommentSelected();
        } else if (item.equals("Album")) {
            onMoreMenuAlbumSelected();
        }
    }

    @Override
    public void onMoreItemSelectedPosition(int id) {
    }

    @Override
    public void onItemSelected(String item) {
        String txtAudioSettings = activity
                .getString(R.string.full_player_setting_menu_Audio_Settings);
        String txtEqualizerSettings = activity
                .getString(R.string.full_player_setting_menu_Equalizer_Settings);
        String txtSleepModeSettings = activity
                .getString(R.string.full_player_setting_menu_Sleep_Mode);
        String txtGymModeSettings = activity
                .getString(R.string.full_player_setting_menu_Gym_Mode);
        String txtDownloadMp3 = activity
                .getString(R.string.full_player_setting_menu_Download_Mp3);
        String txtTrendThis = activity
                .getString(R.string.full_player_setting_menu_Trend_This);

        if (item.equals(txtAudioSettings)) {
            if (!isHandledActionOffline(ACTION_INFO)) {
                onAudioSettingSelected();
            }
        } else if (item.equals(txtEqualizerSettings)) {
            onEquilizerSettingSelected();
        } else if (item.equals(txtSleepModeSettings)) {
            SleepModeDialog shareDialogFragment = SleepModeDialog.newInstance();
            FragmentManager mFragmentManager = getFragmentManager();
            shareDialogFragment.show(mFragmentManager,
                    ShareDialogFragment.FRAGMENT_TAG);
            onSleepModeSelected();
            // Flurry report: Sleep mode used
            Analytics.logEvent(FlurryConstants.FlurryAllPlayer.SleepModeUsed
                    .toString());

        } else if (item.equals(txtGymModeSettings)) {
            // Flurry report: Gym mode used
            Analytics.logEvent(FlurryConstants.FlurryAllPlayer.GymModeUsed
                    .toString());
            onGymModeSelected();
        } else if (item.equals(txtDownloadMp3)) {
            downloadSong();
        } else if (item.equals(txtTrendThis)) {
            if (!isHandledActionOffline(ACTION_TWEET_THIS)) {
                Track track = mPlayerService.getCurrentPlayingTrack();
                if (track != null) {
                    MediaItem mediaItem = new MediaItem(track.getId(),
                            track.getTitle(), track.getAlbumName(),
                            track.getArtistName(), getImgUrl(track),
                            track.getBigImageUrl(), MediaType.TRACK.name()
                            .toLowerCase(), 0, 0, track.getImages(),
                            track.getAlbumId());
                    Intent intent = new Intent(getActivity(),
                            TrendNowActivity.class);
                    Bundle args = new Bundle();
                    args.putSerializable(TrendNowActivity.EXTRA_DATA_MEDIA_ITEM,
                            (Serializable) mediaItem);
                    intent.putExtras(args);
                    if (mPlayerService != null && mPlayerService.getPlayMode() == PlayMode.DISCOVERY_MUSIC && mDiscover != null && !TextUtils.isEmpty(mDiscover.getHashTag()))
                        intent.putExtra("hashTag", mDiscover.getHashTag());
                    startActivity(intent);
                }
            }

        }
    }

    @Override
    public void onItemSelectedPosition(int id) {
    }

    // -----------------------DrawerExpandTitleChange-------//

    Runnable runTitle = new Runnable() {

        @Override
        public void run() {
            try {
                setTitleHandler();
                commonHandler.removeCallbacks(this);
                if (getPlayMode() == PlayMode.MUSIC
                        || getPlayMode() == PlayMode.DISCOVERY_MUSIC) {
                    if (mDrawer.isPanelExpanded() && mPlayerService != null
                            && mPlayerService.getPlayingQueue().size() > 0
                            && mPlayerService.isPlaying()) {
                        isHandlerRunning = true;
                        commonHandler.postDelayed(this, 6000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    private long lastUsedTrackDetailForTitleFlip,
            lastUsedTrackDetailForMiniPlayer;

    void removeTitleHandler(boolean needToSetDefault) {
        commonHandler.removeCallbacks(runTitle);
        isHandlerRunning = false;
        if (needToSetDefault) {
            try {
                RelativeLayout rlExpandHandleFavorite = (RelativeLayout) miniPlayerView
                        .findViewById(R.id.rlExpandHandleFavorite);
                RelativeLayout rlExpandHandleTitle = (RelativeLayout) miniPlayerView
                        .findViewById(R.id.rlExpandHandleTitle);
                rlExpandHandleTitle.setVisibility(View.VISIBLE);
                rlExpandHandleFavorite.setVisibility(View.INVISIBLE);
            } catch (Exception e) {
            }
        }
    }

    void startTitleHandler() {
        commonHandler.postDelayed(runTitle, 6000);
    }

    String txtPlays;

    private void setTitleHandler() {
        try {
            if (txtPlays == null)
                txtPlays = Utils.getMultilanguageText(activity, getResources()
                        .getString(R.string.media_details_no_of_play));

            int pos = getCurrentPlayingInQueuePosition();
            RelativeLayout rlExpandHandleFavorite = (RelativeLayout) miniPlayerView
                    .findViewById(R.id.rlExpandHandleFavorite);
            RelativeLayout rlExpandHandleTitle = (RelativeLayout) miniPlayerView
                    .findViewById(R.id.rlExpandHandleTitle);

            if (pos != -1) {
                if (rlExpandHandleTitle.getVisibility() == View.VISIBLE) {

                    if (mCurrentTrackDetails != null) {
                        rlExpandHandleTitle.setVisibility(View.INVISIBLE);
                        rlExpandHandleFavorite.setVisibility(View.VISIBLE);

                        if (getPlayMode() == PlayMode.LIVE_STATION_RADIO
                                || lastUsedTrackDetailForTitleFlip != mCurrentTrackDetails
                                .getId()) {
                            lastUsedTrackDetailForTitleFlip = mCurrentTrackDetails
                                    .getId();

                            String totalPlayed = mCurrentTrackDetails
                                    .getNumOfPlays() + "";
                            String totalFavorite = mCurrentTrackDetails
                                    .getNumOfFav() + "";

							TextView text_played = (TextView) miniPlayerView
									.findViewById(R.id.main_player_content_info_bar_text_played);
							TextView text_played1 = (TextView) miniPlayerView
									.findViewById(R.id.main_player_content_info_bar_text_played1);
							TextView text_favorite = (TextView) miniPlayerView
									.findViewById(R.id.main_player_content_info_bar_text_favorite);
							Logger.i("value", "totalPlayed value:"
									+ totalPlayed);
							if (totalFavorite.length() > 3) {
								totalFavorite = Utils.numberToStringConvert(
										totalFavorite, 0);
							}
							if (totalPlayed.length() > 3) {
								totalPlayed = Utils.numberToStringConvert(
										totalPlayed, 0);
								Logger.i("value", "totalPlayed value new:"
										+ totalPlayed);
							}
//							totalPlayed = totalPlayed + " " + txtPlays;
//							if(text_played1 instanceof  LanguageTextView) {
//								((LanguageTextView) text_played1).setText(txtPlays);
//							}
							text_played1.setText(txtPlays);
							text_played.setText(totalPlayed);
							text_favorite.setText(totalFavorite + "");
							rlExpandHandleFavorite.invalidate();
							rlExpandHandleTitle.invalidate();
						}
					}
				} else {
					rlExpandHandleTitle.setVisibility(View.VISIBLE);
					rlExpandHandleFavorite.setVisibility(View.INVISIBLE);
				}
			} else {
				if (rlExpandHandleTitle.getVisibility() != View.VISIBLE) {
					rlExpandHandleTitle.setVisibility(View.VISIBLE);
					rlExpandHandleFavorite.setVisibility(View.INVISIBLE);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    // -----------------------Trivia display timer------------//
    TriviaCustomDialog triviaDialog;
    LyricsCustomDialog lyricsDialog;
    private boolean isTriviaTimerStarted = false;
    private int currentTotalSecond = 0, totalSecond = 15;
    private Runnable runTrivia = new Runnable() {

        @Override
        public void run() {
            if (mCurrentTrackDetails != null) {
                hasTrivia = mCurrentTrackDetails.hasTrivia();
                hasLyrics = mCurrentTrackDetails.hasLyrics();
                if (hasTrivia || hasLyrics) {
                    if (currentTotalSecond < totalSecond) {
                        Logger.i("currentTotalSecond", "currentTotalSecond:"
                                + currentTotalSecond);
                        currentTotalSecond = currentTotalSecond + 1;
                        commonHandler.postDelayed(runTrivia, 1000);
                    } else if (currentTotalSecond == 15) {
                        if (isHintVisible
                                || !mApplicationConfigurations
                                .isFullPlayerDrawerHelp()) {
                            commonHandler.postDelayed(runTrivia, 2000);
                            return;
                        }
                        if ((hasLyrics || hasTrivia) && mPlayerService != null
                                && mPlayerService.isPlaying()) {
                            if (mPlayerService != null
                                    && mPlayerService.getPlayMode() == PlayMode.MUSIC) {
                                if (!isContentFragmentOpen()
                                        && !isPaused
                                        && !mApplicationConfigurations
                                        .getSaveOfflineMode()) {
                                    if (triviaDialog != null
                                            && triviaDialog.isShowing())
                                        triviaDialog.dismiss();

                                    if (lyricsDialog != null
                                            && lyricsDialog.isShowing())
                                        lyricsDialog.dismiss();

                                    if (hasLyrics
                                            && mApplicationConfigurations
                                            .needToShowLyrics()
                                            && OnApplicationStartsActivity.needToShowLyricsForSession
                                            && (System.currentTimeMillis() - OnApplicationStartsActivity.AppOpenningTime) >= 10 * 60 * 1000) {
                                        if (mTrackLyrics != null) {
                                            lyricsDialog = new LyricsCustomDialog(
                                                    activity,
                                                    mTrackLyrics,
                                                    mPlayerService
                                                            .getCurrentPlayingTrack());
                                            lyricsDialog.setCancelable(true);
                                            lyricsDialog
                                                    .getWindow()
                                                    .setBackgroundDrawable(
                                                            new ColorDrawable(
                                                                    Color.TRANSPARENT));
                                            lyricsDialog.show();
                                            OnApplicationStartsActivity.needToShowLyricsForSession = false;
                                        }
                                    } else if (hasTrivia
                                            && mApplicationConfigurations
                                            .needToShowTrivia()
                                            && OnApplicationStartsActivity.needToShowTriviaForSession) {
                                        if (activity != null) {
                                            triviaDialog = new TriviaCustomDialog(
                                                    activity,
                                                    mTrackTrivia,
                                                    mPlayerService
                                                            .getCurrentPlayingTrack());
                                            triviaDialog.setCancelable(true);
                                            triviaDialog
                                                    .getWindow()
                                                    .setBackgroundDrawable(
                                                            new ColorDrawable(
                                                                    Color.TRANSPARENT));
                                            triviaDialog.show();
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        stopTriviaTimer();
                    }
                }
            }
        }
    };

    private void startTriviaTimer() {
        try {
            boolean needToShowTrivia = mApplicationConfigurations
                    .needToShowTrivia() && mCurrentTrackDetails.hasTrivia();
            boolean needToShowLyrics = mApplicationConfigurations
                    .needToShowLyrics() && mCurrentTrackDetails.hasLyrics();
            if (!isTriviaTimerStarted && (needToShowTrivia || needToShowLyrics)
                    && mPlayerService != null && mPlayerService.isPlaying()
                    && mPlayerService.getPlayMode() == PlayMode.MUSIC
                    && !mApplicationConfigurations.getSaveOfflineMode()) {
                commonHandler.removeCallbacks(runTrivia);
                currentTotalSecond = 0;
                isTriviaTimerStarted = true;
                Track track = mPlayerService.getCurrentPlayingTrack();
                if (track != null) {
                    if (needToShowTrivia)
                        mDataManager.getTrackTrivia(track, this);
                    if (needToShowLyrics)
                        mDataManager.getTrackLyrics(track, this);
                    commonHandler.post(runTrivia);
                }
            }
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    public void stopTriviaTimer() {
        currentTotalSecond = 0;
        isTriviaTimerStarted = false;
        commonHandler.removeCallbacks(runTrivia);
    }

    Runnable startTrivia = new Runnable() {

        @Override
        public void run() {
            startTriviaTimer();
        }
    };

    private void restartTriviaTimer() {
        if (getPlayMode() == PlayMode.MUSIC) {
            commonHandler.removeCallbacks(startTrivia);
            stopTriviaTimer();
            commonHandler.postDelayed(startTrivia, 2000);
        }
    }

    private HeadSetReceiver headsetPlugReceiver;

    private class HeadSetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)
                    && !isInitialStickyBroadcast()) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        Logger.d(TAG, "Headset unplugged");
                        try {
                            updatePlayerPlayIcon();
                            Toast.makeText(context, "Headset unplugged",
                                    Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Logger.printStackTrace(e);
                        }
                        break;
                    case 1:
                        Logger.d(TAG, "Headset plugged");
                        break;
                }
            }
        }
    }

    int adsFlipInterval = -1;

    private void startAdsFlipTimer() {
        stopAdsFlipTimer();
        if (adsFlipInterval == -1) {
            adsFlipInterval = mApplicationConfigurations
                    .getAppConfigPlayerOverlayFlipBackDuration();
        }
        if (adsFlipInterval <= 0)
            adsFlipInterval = 10;
        if (mPlayerService != null && !mPlayerService.isAdPlaying()
                && getPlayMode() == PlayMode.MUSIC
                && !CacheManager.isProUser(activity)
                && mDrawer.isPanelExpanded()) {
            commonHandler.postDelayed(runAdsFlip, adsFlipInterval * 1000);
        }
    }

    private void stopAdsFlipTimer() {
        commonHandler.removeCallbacks(runAdsFlip);
    }

    Runnable runAdsFlip = new Runnable() {

        @Override
        public void run() {
            if (!isFlip && backgroundImage != null)
                displayFlipAds(backgroundImage);
            else {
                displayFlipAds(null);
                stopAdsFlipTimer();
            }
        }
    };

    public void displayFlipAds(BitmapDrawable backgroundImage) {
        try {
            if (getPlayMode() == PlayMode.MUSIC) {
                List<Track> queue = getCurrentPlayingList();
                // if (mDrawer.isPanelExpanded()) {
                if (queue != null && queue.size() > 0) {
                    int pos;
                    if (mPlayerService.isPlaying()) {
                        pos = getCurrentPlayingInQueuePosition();
                        if (pos < queue.size() && coverFlowAdapter != null) {
                            // fancyCoverFlow.setSelection(pos, true);
                            Track track = queue.get(fancyCoverFlow
                                    .getSelectedItemPosition());
                            View wantedView = fancyCoverFlow.getSelectedView();// fancyCoverFlow.getChildAt(pos);
                            flipPos = fancyCoverFlow.getSelectedItemPosition();
                            // iv = (ImageView) wantedView
                            // .findViewById(R.id.item_player_content_media_art);
                            removeTitleHandler(false);

                            ImageView ivArt = (ImageView) wantedView
                                    .findViewById(R.id.item_player_content_media_art);
                            Button ivClose = (Button) wantedView
                                    .findViewById(R.id.bCloseAd);
                            ImageView iv_item_player_shadow = (ImageView) wantedView
                                    .findViewById(R.id.iv_item_player_shadow);
                            ImageView item_player_content_media_play = (ImageView) wantedView
                                    .findViewById(R.id.item_player_content_media_play);
                            RelativeLayout rlItemInfoCover = (RelativeLayout) wantedView
                                    .findViewById(R.id.rlItemInfoCover);
                            iv_item_player_shadow.setVisibility(View.INVISIBLE);
                            RelativeLayout rlItemInfoCover_english = (RelativeLayout) wantedView
                                    .findViewById(R.id.rlItemInfoCover_English);
                            rlItemInfoCover.setVisibility(View.INVISIBLE);
                            rlItemInfoCover_english
                                    .setVisibility(View.INVISIBLE);
                            ivClose.setVisibility(View.INVISIBLE);
                            item_player_content_media_play
                                    .setVisibility(View.INVISIBLE);
                            if (isFlip) {
                                new FlipAnimationListener(wantedView, ivArt,
                                        null, track, true);
                            } else if (!isFlip && backgroundImage != null) {
                                new FlipAnimationListener(wantedView, ivArt,
                                        backgroundImage, track, true);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // ---------------------Flip animation - 11 Feb 2015----------//

    boolean isFlip = false;
    int flipPos = -1;

    private class FlipAnimationListener implements AnimationListener {
        private Animation animation1;
        private Animation animation2;
        // private boolean isBackOfCardShowing = true;
        View view;
        BitmapDrawable adBitmap;
        Track track;

        public FlipAnimationListener(View wantedView, View view,
                                     BitmapDrawable adUrl, Track track, boolean needToRestartFlip) {

            if (activity != null) {
                animation1 = AnimationUtils.loadAnimation(activity,
                        R.anim.to_middle_player);
                animation1.setAnimationListener(this);
                animation2 = AnimationUtils.loadAnimation(activity,
                        R.anim.from_middle_player);
                animation2.setAnimationListener(this);
                this.adBitmap = adUrl;
                this.view = view;
                this.track = track;
                view.clearAnimation();
                view.setAnimation(animation1);
                view.startAnimation(animation1);
            }
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (activity != null) {
                if (animation == animation1) {
                    if (!isFlip) {
                        isFlip = true;
                        ((ImageView) view).setImageDrawable(adBitmap);
                    } else {
                        isFlip = false;
                        if (picasso == null)
                            picasso = PicassoUtil.with(activity);
                        picasso.load(null, getImageUrl(track),
                                (ImageView) view,
                                R.drawable.icon_main_player_no_content);
                    }
                    view.clearAnimation();
                    view.setAnimation(animation2);
                    view.startAnimation(animation2);

                } else if (animation == animation2) {
                    // isFlip = false;
                    view.clearAnimation();
                    animation1.setFillAfter(false);
                    animation2.setFillAfter(false);
                    view.setClickable(false);
                    startTitleHandler();
                    // startAdsFlipTimer();
                    if (isFlip) {
                        if (mDrawer != null && mDrawer.isPanelExpanded())
                            startAdsFlipTimer();
                        else {
                            isFlip = false;
                            stopAdsFlipTimer();
                            flipPos = -1;
                        }
                    }
                    coverflowScrollView.setScrollingEnabled(!isFlip);
                    disableScrollView();
                    coverFlowAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }
    }

    private void disableScrollView() {
        mDrawer.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                return isFlip;
            }
        });
    }

    private void showTrialOfferExpiredPopup() {
        if (CacheManager.isTrialOfferExpired(activity)) {
            AppPromptOfflineCachingTrialExpired prompt9 = new AppPromptOfflineCachingTrialExpired(
                    activity);
            prompt9.appLaunched(true);
        }
    }

    private void openRadioFullPlayerMoreOptions(final View view) {
        try {
            QuickActionRadioFullPlayerMore quickAction;
            quickAction = new QuickActionRadioFullPlayerMore(mContext);
            quickAction.setOnRadioFullPlayerMoreSelectedListener(this);
            quickAction.show(view);
            view.setEnabled(false);
            quickAction
                    .setOnDismissListener(new QuickActionRadioFullPlayerMore.OnDismissListener() {
                        @Override
                        public void onDismiss() {
                            view.setEnabled(true);
                        }
                    });
        } catch (Exception e) {
            Logger.printStackTrace(e);
        }
    }

    @Override
    public void onRadioFullPlayerMoreItemSelected(final String item) {
        if (item.equals("Info")) {
            Track currentTrack = mPlayerService.getCurrentPlayingTrack();
            MediaItem mMediaItem = (MediaItem) currentTrack.getTag();
            if (mMediaItem instanceof LiveStation) {
                LiveStation liveStation = (LiveStation) mMediaItem;
                String descriptions = liveStation.getDescription();
                if (descriptions != null && !descriptions.equals("")) {
                    RadioFullPlayerInfoDialog radioFullPlayerMoreDialog = new RadioFullPlayerInfoDialog(
                            activity, descriptions);
                    radioFullPlayerMoreDialog.setCancelable(false);
                    radioFullPlayerMoreDialog.show();
                }
            }
        }
    }

    private String getImageUrl(Track track) {
        if (mCurrentTrackDetails != null
                && track.getId() == mCurrentTrackDetails.getId()
                && track.getImagesUrlArray() == null) {
            return ImagesManager.getMusicArtSmallImageUrl(mCurrentTrackDetails
                    .getImagesUrlArray());
        }
        return ImagesManager
                .getMusicArtSmallImageUrl(track.getImagesUrlArray());
    }

    private void setFullPlayerBottomHeight() {
        try {
            if (activity != null && rootView != null) {
                int height = activity.getResources().getDimensionPixelSize(
                        R.dimen.main_player_bar_height);
                if (getPlayMode() == PlayMode.MUSIC) {
                    height = activity.getResources().getDimensionPixelSize(
                            R.dimen.main_player_bar_height_full);
                }
                RelativeLayout rlMiniPlayerFull = (RelativeLayout) rootView
                        .findViewById(R.id.main_player_bar);
                rlMiniPlayerFull.getLayoutParams().height = height;
            }
        } catch (Exception e) {
        }
    }

    private void setFullPlayerBottomHeightForMusic() {
        int height = activity.getResources().getDimensionPixelSize(
                R.dimen.main_player_bar_height_full);
        RelativeLayout rlMiniPlayerFull = (RelativeLayout) rootView
                .findViewById(R.id.main_player_bar);
        rlMiniPlayerFull.getLayoutParams().height = height;
    }

    public void updateTrack(Track track) {
        if (mCurrentTrackDetails != null
                && mCurrentTrackDetails.getId() == track.getId()
                && mCurrentTrackDetails.getIntl_content() == 1) {
            changeMiniPlayerTitleText(track.getTitle(),
                    mCurrentTrackDetails.getSingers());
        } else {
            changeMiniPlayerTitleText(track.getTitle(), track.getAlbumName());
        }
    }

    public class TrackReloadReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            try {
                MediaTrackDetails trackDetails = (MediaTrackDetails) arg1
                        .getSerializableExtra(MediaDetailsOperation.RESPONSE_KEY_MEDIA_DETAILS);
                if (trackDetails != null) {
                    Track track = new Track(trackDetails.getId(),
                            trackDetails.getTitle(), trackDetails.getAlbumName(),
                            trackDetails.getSingers(), trackDetails.getImageUrl(),
                            trackDetails.getBigImageUrl(),
                            trackDetails.getImages(), trackDetails.getAlbumId());
                    try {
                        if (PlayerService.service != null)
                            PlayerService.service.getPlayerQueueObject()
                                    .updateTrack(track);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (mCurrentTrackDetails != null
                            && trackDetails.getId() == mCurrentTrackDetails.getId()) {
                        mCurrentTrackDetails = trackDetails;
                        updateTrack(track);
                    }
                }
            } catch (Exception e){
                Logger.printStackTrace(e);
            }
        }
    }

    // 20151127
    /*private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private MediaRouteButton mMediaRouteButton;
    private MediaRouteButton mMediaRouteButtonRadio;
    private CastDevice mSelectedDevice;
    private int mRouteCount = 0;
    public void InItmediaRouterButton(){
        mMediaRouter = MediaRouter.getInstance(getActivity());
        // Create a MediaRouteSelector for the type of routes your app supports
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(
                        CastMediaControlIntent.categoryForCast(getResources()
                                .getString(R.string.crome_cast_app_id))).build();
        // Create a MediaRouter callback for discovery events
        mMediaRouterCallback = new MyMediaRouterCallback();

        // Set the MediaRouteButton selector for device discovery.
        mMediaRouteButton = (MediaRouteButton)miniPlayerView.findViewById(R.id.media_route_button);
        mMediaRouteButton.setRouteSelector(mMediaRouteSelector);


        // Set the MediaRouteButtonRadio selector for device discovery.
        mMediaRouteButtonRadio = (MediaRouteButton)miniPlayerView.findViewById(R.id.media_route_button_radio);
        mMediaRouteButtonRadio.setRouteSelector(mMediaRouteSelector);
    }

    public void onResumeMediaRouter(){
        // Add the callback to start device discovery
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    public void onPauseMediaRouter(){
        // Remove the callback to stop device discovery
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onPause();
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {
        @Override
        public void onRouteAdded(MediaRouter router, MediaRouter.RouteInfo route) {
            Logger.d(TAG, "onRouteAdded");
            if (++mRouteCount == 1) {
                // Show the button when a device is discovered.
                mMediaRouteButton.setVisibility(View.VISIBLE);
                mMediaRouteButtonRadio.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onRouteRemoved(MediaRouter router, MediaRouter.RouteInfo route) {
            Logger.d(TAG, "onRouteRemoved");
            if (--mRouteCount == 0) {
                // Hide the button if there are no devices discovered.
                mMediaRouteButton.setVisibility(View.GONE);
                mMediaRouteButtonRadio.setVisibility(View.GONE);
            }
        }

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            Logger.d(TAG, "onRouteSelected");
            // Handle route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

            // Just display a message for now; In a real app this would be the
            // hook to connect to the selected device and launch the receiver
            // app
            //Toast.makeText(getActivity(), getString(R.string.todo_connect), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            Logger.d(TAG, "onRouteUnselected: info=" + info);
            mSelectedDevice = null;
        }
    }*/


    private BroadcastReceiver mCastDialogClickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            List<Track> queue = mPlayerService.getPlayingQueue();
            if (mDrawer != null && !mDrawer.isPanelExpanded() && queue!=null && !queue.isEmpty()) {
                mDrawer.expandPanel();
            }
        }
    };

    private void showHideCastIcon(){
        /*if(mPlayerService.isAdPlaying() || mApplicationConfigurations.getSaveOfflineMode()){
            mMediaRouteButton.setVisibility(View.GONE);
            mMediaRouteButtonRadio.setVisibility(View.GONE);
        }else{
            mMediaRouteButton.setVisibility(View.VISIBLE);
            mMediaRouteButtonRadio.setVisibility(View.VISIBLE);
        }*/
    }

}
