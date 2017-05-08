package com.hungama.myplay.activity.data.configurations;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.hungama.BadgesAndCoins;
import com.hungama.myplay.activity.services.InventoryLightService;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.Locale;

public class ApplicationConfigurations {

	private static final String PREFERENCES_APPLICATION_CONFIGURATIONS = "preferences_application_configurations";
	private static ApplicationConfigurations sIntance;
	private SharedPreferences mPreferences;
	private Context mContext;

	public static final synchronized ApplicationConfigurations getInstance(
			Context applicationContext) {
		if (sIntance == null) {
			if (applicationContext != null)
				sIntance = new ApplicationConfigurations(
						applicationContext.getApplicationContext());
		}
		return sIntance;
	}

	public void destroyConfig(){
		sIntance=null;
	}
	private ApplicationConfigurations(Context context) {
		mContext = context;
		try {

			try {
				mPreferences = context.getSharedPreferences(
						PREFERENCES_APPLICATION_CONFIGURATIONS,
						Context.MODE_PRIVATE);
			} catch (Exception e) {
				mPreferences = context.getSharedPreferences(
						PREFERENCES_APPLICATION_CONFIGURATIONS,
						Context.MODE_PRIVATE);
			} catch (Error e) {
				System.gc();
				System.runFinalization();
				System.gc();
				mPreferences = context.getSharedPreferences(
						PREFERENCES_APPLICATION_CONFIGURATIONS,
						Context.MODE_PRIVATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final String REGISTRATION_ID = "registration_id";
	public static final String PASSKEY = "passkey";
	public static final String SESSION_ID = "session_id";
	public static final String CONSUMER_ID = "consumer_id";
	public static final String HOUSEHOLD_ID = "household_id";
    public static final String SILENT_USER_SESSION_ID = "silent_user_session_id";
    public static final String SILENT_USER_HOUSEHOLD_ID = "silent_user_household_id";
    public static final String SILENT_USER_CONSUMER_ID = "silent_user_consumer_id";
    public static final String SILENT_USER_PASSKEY = "silent_user_passkey";
	// public static final String HOUSEHOLD_CLIENT_REVISION =
	// "household_client_revision";
	// public static final String CONSUMER_CLIENT_REVISION =
	// "consumer_client_revision";
	public static final String CONSUMER_SERVER_REVISION = "consumer_server_revision";
	public static final String LOCALE_TIME = "locale_time";
	public static final String HOUSEHOLD_REVISION = "household_revision";
	public static final String HOUSEHOLD_SERVER_REVISION = "household_server_revision";
	public static final String CONSUMER_REVISION = "consumer_revision";
	public static final String DEVICE_ID = "device_id";
	public static final String EXISTING_DEVICE = "existing_device";
	private static final String EXISTING_DEVICE_EMAIL = "existing_device_email";
	public static final String CLIENT_TYPE = "client_type";
	public static final String ACTIVATION_CODE = "activation_code";
	public static final String PARTNER_USER_ID = "partner_user_id";
    public static final String SILENT_PARTNER_USER_ID = "silent_partner_user_id";
	private static final String SKIPPED_PARTNER_USER_ID = "skipped_partner_user_id";
	public static final String IS_REAL_USER = "real_user";
	private static final String IS_USER_REGISTERED = "is_user_registered";
	private static final String USER_LOGIN_PHONE_NUMNBER = "user_login_phone_numnber";
    private static final String SILENT_USER_LOGIN_PHONE_NUMNBER = "silent_user_login_phone_numnber";
	private static final String IS_USER_HAS_SUBSCRIPTION_PLAN = "user_has_subscription_plan";
	private static final String IS_USER_HAS_TRIAL_SUBSCRIPTION_PLAN = "user_has_trial_subscription_plan";
	private static final String IS_USER_TRIAL_SUBSCRIPTION_EXPIRED = "is_user_trial_subscription_plan_expired";
	private static final String IS_SHOW_ADS = "is_show_ads";
	private static final String USER_TRIAL_SUBSCRIPTION_DAYS_LEFT = "user_trial_subscription_days_left";
	private static final String USER_SUBSCRIPTION_PLAN_DATE = "user_subscription_plan_date";
	private static final String USER_SUBSCRIPTION_PLAN_DATE_PURCHASE = "user_subscription_plan_date_purchase";
	private static final String USER_SUBSCRIPTION_PLAN_DETAILS = "user_subscription_plan_details";
	public static final String CAMPAIGN_IDS = "campaign_ids";
	public static final String MEDIA_ID_NS = "media_id_ns";
	public static final String PAGE_MAX = "page_max";
	public static final String PAGE_MIN = "page_min";
	public static final String PAGE_OPTIMAL = "page_optimal";
	private static final String IS_FIRST_VISIT_TO_APP = "first_visit_to_app";
	private static final String IS_INVENTORY_FETCH = "is_inventory_fetch";
	private static final String IS_FIRST_VISIT_TO_HOME_TILE_PAGE = "is_first_visit_to_home_tile_page";
	private static final String IS_FIRST_VISIT_TO_SEARCH_PAGE = "is_first_visit_to_search_page";
	private static final String IS_FIRST_VISIT_TO_FULL_PLAYER = "is_first_visit_to_full_player";
	private static final String IS_HOME_HINT_SHOWN_IN_THIS_SESSION = "is_home_hint_shown_in_this_session";
	private static final String IS_SEARCH_FILTER_SHOWN_IN_THIS_SESSION = "is_search_filter_shown_in_this_session";
	private static final String IS_PLAYER_QUEUE_HINT_SHOWN_IN_THIS_SESSION = "is_player_queue_hint_shown_in_this_session";
	private static final String IS_ENABLED_HOME_GUIDE_PAGE = "is_enabled_home_guide_page_new";
	private static final String IS_ENABLED_HOME_GUIDE_PAGE_3_OFFLINE = "is_enabled_home_guide_3_offline";
	private static final String IS_ENABLED_PRO_GUIDE_PAGE_3_OFFLINE = "is_enabled_pro_guide_3_offline";
	private static final String IS_SONG_CATCHED = "is_song_catched";
	private static final String IS_ENABLED_GYM_MODE_GUIDE_PAGE = "is_enabled_gym_mode_guide_page";
	private static final String IS_ENABLED_DISCOVER_GUIDE_PAGE = "is_enabled_discover_guide_page";
	private static final String IS_ENABLED_DOWNLOAD_DELETE_GUIDE_PAGE = "is_enabled_download_delete_guide_page";
	private static final String GIGYA_SIGNUP = "gigya_signup";
	private static final String GIGYA_FB_FIRST_NAME = "gigya_fb_first_name";
	private static final String GIGYA_FB_LAST_NAME = "gigya_fb_last_name";
	private static final String GIGYA_FB_EMAIL = "gigya_fb_EMAIL";
	private static final String GIGYA_TWITTER_FIRST_NAME = "gigya_twitter_first_name";
	private static final String GIGYA_TWITTER_LAST_NAME = "gigya_twitter_last_name";
	private static final String GIGYA_TWITTER_EMAIL = "gigya_twitter_email";
	private static final String GIGYA_GOOGLE_FIRST_NAME = "gigya_google_first_name";
	private static final String GIGYA_GOOGLE_LAST_NAME = "gigya_google_last_name";
	private static final String GIGYA_GOOGLE_EMAIL = "gigya_google_email";
	private static final String HUNGAMA_FIRST_NAME = "hungama_first_name";
	private static final String HUNGAMA_LAST_NAME = "hungama_last_name";
	private static final String HUNGAMA_EMAIL = "hungama_email";
	private static final String PLAYER_VOLUME = "player_volume";
	private static final String BITRATE_STATE = "bitrate_state";
	private static final String BITRATE = "bitrate";
	private static final String BANDWIDTH = "bandwidth";
	private static final String FILE_SIZE_FOR_BITRATE_CALCULATION = "file_size_for_bitrate_calculation";
	private static final String GIGYA_FB_THUMB_URL = "gigya_fb_thumb_url";
	private static final String GIGYA_TWITTER_THUMB_URL = "gigya_twitter_thumb_url";
	private static final String HINTS_STATE = "hints_state";
	private static final String TIME_READ_DELTA = "time_read_delta";
	private static final String IS_VERSION_CHECKED = "is_version_checked";
	private static final String CONTENT_FORMAT = "content_format";
	private static final String SUBSCRIPTION_IAB_CODE = "subscription_iab_code";
	private static final String SUBSCRIPTION_IAB_PURCHSE_TOKEN = "subscription_iab_purchse_token";

	private static final String DISCOVER_PREFETCH_MOODS_SUCCESS_FLAG = "discover_prefetch_moods_success_flag";
	// public static final String MOBILE_NUMBER = "mobile_number";

	private static final String IS_FIRST_BADGE_DISPLAYED = "is_first_badge_displayed";

	public static final String GIGYA_LOGIN_SESSION_TOKEN = "gigya_login_session_token";
	public static final String GIGYA_LOGIN_SESSION_SECRET = "gigya_login_session_secret";
    public static final String SILENT_USER_GIGYA_LOGIN_SESSION_TOKEN = "silent_user_gigya_login_session_token";
    public static final String SILENT_USER_GIGYA_LOGIN_SESSION_SECRET = "silent_user_gigya_login_session_secret";

	private static final String SELECTED_PREFERENCES = "selected_preferences";
	private static final String SELECTED_PREFERENCES_VIDEO = "selected_preferences_video";
	private static final String USER_SELECTED_LANGUAGE = "user_selected_language";
	private static final String USER_SELECTED_LANGUAGE_TEXT = "user_selected_language_text";
    private static final String SILENT_USER_SELECTED_LANGUAGE = "silent_user_selected_language";
    private static final String SILENT_USER_SELECTED_LANGUAGE_TEXT = "silent_user_selected_language_text";
	private static final String IS_LANGUAGE_SUPPORTED_FOR_WIDGET = "is_language_supported_for_widget";
	private static final String STRING_KEYS = "string_keys_urls";
	private static final String STRING_VALUES = "string_values";

	/*
	 * Media Handle and Play Event additional properties.
	 */
	public static final String KEY_MEDIA_ID_NS = "media_id_ns";
	public static final String VALUE_MEDIA_ID_NS = "hungama";

	public static final int BITRATE_HD = 320;
	public static final int BITRATE_HIGH = 128;
	public static final int BITRATE_MEDIUM = 64;
	public static final int BITRATE_LOW = 32;
	public static final int BITRATE_AUTO = 1;
	private static final int BITRATE_NONE = 0;

	private static final String DOWNLOAD_PLAN_ID = "download_plan_id";
	private static final String DOWNLOAD_PLAN_TYPE = "download_plan_type";

	private static final String DOWNLOAD_PLAN_NAME = "download_plan_name";
	private static final String DOWNLOAD_PLAN_PRICE = "download_plan_price";
	private static final String DOWNLOAD_PLAN_CURRENCY = "download_plan_currency";
	private static final String DOWNLOAD_PLAN_DURATION = "download_plan_duration";
	private static final String DOWNLOAD_MSISDN = "download_msisdn";
	private static final String DOWNLOAD_SUBSCRIPTION_STATUS = "download_subscription_status";
	private static final String DOWNLOAD_VALIDITY_DATE = "download_validity_date";
	private static final String DOWNLOAD_TRIAL = "download_trial";
	private static final String DOWNLOAD_DAYS_LEFT = "download_days_left";
	private static final String DOWNLOAD_PRODUCT_ID = "download_product_id";

	// Badges and coins object
	private static final String BADGES_AND_COINS_BADGES_EARNED = "badges_and_coins_badges_earned";
	private static final String BADGES_AND_COINS_DISPLAY_CASE = "badges_and_coins_display_case";
	private static final String BADGES_AND_COINS_POINTS_EARNED = "badges_and_coins_points_earned";
	private static final String BADGES_AND_COINS_BADGE_NAME = "badges_and_coins_badge_name";
	private static final String BADGES_AND_COINS_BADGE_URL = "badges_and_coins_badge_url";
	private static final String BADGES_AND_COINS_MESSAGE = "badges_and_coins_message";
	private static final String BADGES_AND_COINS_NEXT_DESCRIPTION = "badges_and_coins_next_description";

	/**
	 * Preference's key for boolean flag if the prefetching of the moods has
	 * successed or not.
	 */
	private static final String MOODS_PREFETCHING_SUCCESS = "moods_prefetching_success";
	private static final String APPLICATION_IMAGES_PREFETCHING_SUCCESS = "application_images_prefetching_success";
	private static final String APPLICATION_TEXT_LISTS = "application_text_list";
	private static final String APPLICATION_AD_REFRESH_INTERVAL = "application_ad_refresh_interval";
	private static final String APPLICATION_FREE_CACHE_LIMIT = "application_free_cache_limit";
	private static final String APPLICATION_FREE_USER_DELETE_COUNT = "application_free_user_delete_count";
	private static final String APPLICATION_FREE_USER_CACHE_COUNT = "application_free_user_cache_count";
	private static final String APPLICATION_TIME_OUT = "application_timeout";
	private static final String APPLICATION_RETRY = "application_retry";
	private static final String APPLICATION_SPLASHAD_TIME_WAIT = "application_splashad_time_wait";

	private static final String SAVE_OFFLINE_MODE = "save_offline_mode";
	private static final String SAVE_OFFLINE_AUTO_SAVE = "save_offline_auto_save";
	private static final String SAVE_OFFLINE_CELLULAR_NETWORK = "save_offline_on_cellular_network";
	private static final String SAVE_OFFLINE_MEMORY_MAX = "save_offline_memory_max";
	private static final String SAVE_OFFLINE_MEMORY_ALLOCATED = "save_offline_memory_allocated";
	private static final String SAVE_OFFLINE_UPGRADE_POPUP = "save_offline_upgrade_popup";
	private static final String SAVE_OFFLINE_AUTO_SAVE_FIRST_TIME = "save_offline_auto_save_first_time";
	private static final String SAVE_OFFLINE_AUTO_SAVE_FREE_USER = "save_offline_auto_save_free_user";

	private static final String PLAYER_QUEUE = "player_queue";
	private static final String NAWRAS_MSISDN = "nawras_msisdn";
	private static final String APPLICATION_VERSION = "application_version";
	private static final String API_DATE = "api_date";
	private static final String UA_TAG_DATE = "ua_tag_date";
	private static final String UA_TAG_SONG_CONSUMPTION = "ua_tag_song_consumption";
	private static final String UA_TAG_VIDEO_CONSUMPTION = "ua_tag_video_consumption";
	private static final String UA_TAG_USER_POINT = "ua_tag_user_point";
	private static final String MUSIC_PREFERENCES_RESPONSE = "music_preferences_response";
	// public static final String SEARCH_POPULAR_KEYWORD =
	// "search_popular_keyword";
	// public static final String SELECTED_MUSIC_PREFERENCES_RESPONSE =
	// "selected_music_preferences_response";
	private static final String VIDEO_PREFERENCES_RESPONSE = "video_preferences_response";
	// public static final String SELECTED_VIDEO_PREFERENCES_RESPONSE =
	// "selected_video_preferences_response";
	private static final String IS_ENABLED_SONG_CATCHER_GUIDE_PAGE = "is_enabled_song_catcher_guide";
	private static final String IS_ENABLED_LANGUAGE_GUIDE_PAGE = "is_enabled_language_guide";
	private static final String APP_OPEN_COUNT = "application_open_count";
	private static final String APP_CHECK_PRO_USER = "application_check_pro_user";
	private static final String APP_TRIAL_CHECKED_FOR_USER_ID = "application_trial_checked_for_user_id";
	// public static final String APP_CHECK_AUDIO_AD_COUNT_LIMIT =
	// "application_check_audio_ad_count_limit";
	// public static final String APP_CHECK_SPLASH_AD_COUNT_LIMIT =
	// "application_check_splash_ad_count_limit";
	private static final String APP_CHECK_FOR_AUDIO_AD_DURATION = "application_check_for_audio_ad_duration";

	private static final String APP_CONFIG_SPLASH_LAUNCH = "app_config_splash_launch";
	private static final String APP_CONFIG_SPLASH_RELAUNCH = "app_config_splash_relaunch";
	private static final String APP_CONFIG_SPLASH_UNLOCK = "app_config_splash_unlock";
	private static final String APP_CONFIG_SPLASH_MAXIMIZE = "app_config_splash_maximize";
	private static final String APP_CONFIG_SPLASH_SESSION_LIMIT = "app_config_splash_session_limit";
	private static final String APP_CONFIG_SPLASH_REFRESH_LIMIT = "app_config_splash_refresh_limit";
	private static final String APP_CONFIG_SPLASH_AUTO_SKIP = "app_config_splash_auto_skip";

	private static final String APP_CONFIG_PLAYER_OVERLAY_REFRESH = "app_config_player_overlay_refresh";
	private static final String APP_CONFIG_PLAYER_OVERLAY_START = "app_config_player_overlay_start";
	private static final String APP_CONFIG_PLAYER_OVERLAY_FLIP_BACK_DURATION = "app_config_player_overlay_flip_back_duration";

	private static final String APP_CONFIG_AUDIO_AD_FREQUENCY = "app_config_audio_ad_frequency";
	private static final String APP_CONFIG_AUDIO_ADD_RULE = "app_config_audio_add_rule";
	private static final String APP_CONFIG_AUDIO_ADD_SESSION_LIMIT = "app_config_audio_add_session_limit";

	private static final String APP_CONFIG_VIDEO_ADD_PLAY = "app_config_video_add_play";
	private static final String APP_CONFIG_VIDEO_ADD_SESSION_LIMIT = "app_config_video_add_session_limit";
	private static final String APP_CONFIG_VIDEO_AD_PLAYBACK_COUNTER = "app_config_video_ad_playback_counter";

	private static final String APP_CONFIG_REFRESH_ADS = "app_config_refresh_ads";

	private static final String APP_SELECTED_MUSIC_PREFERENCE = "application_selected_music";
	private static final String APP_SELECTED_MUSIC_GENRE = "application_selected_music_genre";
    private static final String APP_SILENT_USER_SELECTED_MUSIC_PREFERENCE = "application_silent_user_selected_music";
    private static final String APP_SILENT_USER_SELECTED_MUSIC_GENRE = "application_silent_user_selected_music_genre";
	private static final String APP_SELECTED_VIDEO_PREFERENCE = "application_selected_video";

	private static final String APP_CHECK_LEFT_MENU_HINT = "application_check_left_menu_hint";
	private static final String APP_CHECK_PLAYER_QUEUE_HINT = "application_check_player_queue_hint";
	private static final String APP_CHECK_PLAYER_BAR_HINT = "application_check_player_bar_hint";
	private static final String APP_CHECK_OFFLINE_MUSIC_HINT = "application_check_offline_music_hint";
	private static final String HOME_SCREEN_LANDING_HELP = "home_screen_landing_help";
	private static final String FULL_PLAYER_DRAWER_HELP = "full_player_drawer_help";
	private static final String FULL_PLAYER_DRAWER_HELP2 = "full_player_drawer_help2";
	private static final String FULL_PLAYER_DRAG_HELP = "full_player_drag_help";
	private static final String FILTER_SONGS = "filter_songs";
	private static final String FILTER_ALBUMS = "filter_albums";
	private static final String FILTER_PLAYLISTS = "filter_playlists";
	// public static final String PLAYER_BAR_HEIGHT = "player_bar_height";

	private static final String FILTER_TOP_RADIO = "filter_top_radio";
	private static final String FILTER_CELEB_RADIO = "filter_celeb_radio";
	private static final String MY_STREAM_SETTINGS_TRIVIA_SHOW = "my_stream_settings_trivia_show";
	private static final String TRIVIA_SHOW_NOT_NOW_COUNTER = "trivia_show_not_now_counter";
	private static final String MY_STREAM_SETTINGS_LYRICS_SHOW = "my_stream_settings_lyrics_show";
	private static final String LYRICS_SHOW_NOT_NOW_COUNTER = "lyrics_show_not_now_counter";
	private static final String TRIVIA_ALERT_DISPLAY = "trivia_alert_disply";
	private static final String NEED_TO_SHOW_SAVE_OFFLINE_HELP = "need_to_show_save_offline_help";
	private static final String SAVE_OFFLINE_AUTO_MODE = "save_offline_auto_mode";
	private static final String SAVE_OFFLINE_AUTO_MODE_REMEMBER = "save_offline_auto_mode_remember";

	private static final String FAVORITE_SELECTION = "fav_selection";
	private static final String MYCOLLECTION_SELECTION = "mycollection_selection";

	// timestame cache
	private static final String MUSIC_LATEST_TIMESTAMP = "music_latest_timestamp";
	private static final String MUSIC_POPULAR_TIMESTAMP = "music_popular_timestamp";
	private static final String VIDEO_LATEST_TIMESTAMP = "video_latest_timestamp";
	private static final String RADIO_LIVE_TIMESTAMP = "live_radio_timestamp";
	private static final String RADIO_ONDEMAND_TIMESTAMP = "ondemand_radio_timestamp";
	private static final String SEARCH_POPULAR_TIMESTAMP = "search_popular_timestamp";
	private static final String CATEGORIES_GENER_TIMESTAMP = "categories_gener_timestamp";
	private static final String PREF_GET_CATEGORY_TIMESTAMP = "pref_get_category_timestamp";
	private static final String PREF_GET_ALL_LANGUAGES_TIMESTAMP = "pref_get_all_languages_timestamp";
	private static final String PREF_GET_USER_LANGUAGES_TIMESTAMP = "pref_get_user_languages_timestamp";
	private static final String LEFT_MENU_TIMESTAMP = "left_menu_timestamp";
	private static final String FEEDBACK_TIMESTAMP = "feedback_timestamp";

	private static final String TOTAL_SESSION = "total_session";
	private static final String TOTAL_LAST_REGISTER_DISPLAY_SESSION = "last_session_register";
	private static final String TOTAL_LAST_SOCIAL_LOGIN_DISPLAY_SESSION = "last_session_social_login";
	private static final String TOTAL_LAST_OFFLINE_3RD_SONG = "last_session_offline_3rd_song";
	private static final String TIME_LAST_OFFLINE_TRIAL_EXPIRED_SHOWN = "last_session_offline_trial_expired";
	private static final String TOTAL_LAST_EXIT_SESSION = "last_exit_session";
	private static final String CATEGORY_PREF_SELECTION_GENRIC = "category_pref_selection_generic";

	private static final String APP_START_TIMESTAMP = "app_start_timestamp";
	private static final String APP_IMAGES_TIMESTAMP = "app_images_timestamp";
	private static final String USER_PROFILE_TIMESTAMP = "user_profile_timestamp";
	private static final String USER_PROFILE_MY_DOWNLOAD_TIMESTAMP = "user_profile_mydownload_timestamp";
	private static final String USER_PROFILE_LEADER_BOARD_TIMESTAMP = "user_profile_leaderboard_timestamp";
	private static final String USER_PROFILE_BADGES_TIMESTAMP = "user_profile_badges_timestamp";

	private static final String USER_PROFILE_MY_FAVORITE_TIMESTAMP = "user_profile_my_favorite_timestamp";
	private static final String USER_PROFILE_MY_FAVORITE_ALBUM_TIMESTAMP = "user_profile_my_favorite_album_timestamp";
	private static final String USER_PROFILE_MY_FAVORITE_PLAYLIST_TIMESTAMP = "user_profile_my_favorite_playlist_timestamp";
	private static final String USER_PROFILE_MY_FAVORITE_VIDEOS_TIMESTAMP = "user_profile_my_favorite_videos_timestamp";
	private static final String USER_PROFILE_MY_FAVORITE_ARTIST_TIMESTAMP = "user_profile_my_favorite_artist_timestamp";

	private static final String USER_NQ_HISTORY_TIMESTAMP = "user_nq_history_timestamp";
	private static final String TREND_DIALOG_SHOW_FOR_DISCOVERY_OF_THE_DAY = "trend_dialog_show";
	private static final String TREND_DIALOG_DONT_SHOW_FOR_DISCOVERY_OF_THE_DAY = "trend_dialog_dont_show";
	private static final String REDEEM_URL = "redeem_url";
	private static final String TWEET_LIMIT = "tweet_limit";

	private static final String SUBSCRIPTION_AFF_CODE = "subscription_aff_code";
	private static final String APSALAR_ATTRIBUTION_DONE = "apsalar_attribution_done";
    private static final String IS_GCM_TOKEN = "is_gcm_token";
	private static final String DEFAULT_USER_AGENT = "default_user_agent";

	public String getNQHistoryTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(USER_NQ_HISTORY_TIMESTAMP, null);
	}

	public void setNQHistoryTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_NQ_HISTORY_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getUserProfileFavoriteAlbumTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(USER_PROFILE_MY_FAVORITE_ALBUM_TIMESTAMP,
				null);
	}

	public void setUserProfileFavoriteAlbumTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_PROFILE_MY_FAVORITE_ALBUM_TIMESTAMP,
				timestamp_cache);
		editor.commit();
	}

	public String getUserProfileFavoritePlaylistTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(
				USER_PROFILE_MY_FAVORITE_PLAYLIST_TIMESTAMP, null);
	}

	public void setUserProfileFavoritePlaylistTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_PROFILE_MY_FAVORITE_PLAYLIST_TIMESTAMP,
				timestamp_cache);
		editor.commit();
	}

	public String getUserProfileFavoriteVideosTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(
				USER_PROFILE_MY_FAVORITE_VIDEOS_TIMESTAMP, null);
	}

	public void setUserProfileFavoriteVideosTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_PROFILE_MY_FAVORITE_VIDEOS_TIMESTAMP,
				timestamp_cache);
		editor.commit();
	}

	public String getUserProfileFavoriteArtistTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(
				USER_PROFILE_MY_FAVORITE_ARTIST_TIMESTAMP, null);
	}

	public void setUserProfileFavoriteArtistTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_PROFILE_MY_FAVORITE_ARTIST_TIMESTAMP,
				timestamp_cache);
		editor.commit();
	}

	public String getUserProfileBadgesTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(USER_PROFILE_BADGES_TIMESTAMP, null);
	}

	public void setUserProfileBadgesTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_PROFILE_BADGES_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getUserProfileDownloadTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(USER_PROFILE_MY_DOWNLOAD_TIMESTAMP, null);
	}

	public void setUserProfileDownloadTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_PROFILE_MY_DOWNLOAD_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getUserProfileFavoriteTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(USER_PROFILE_MY_FAVORITE_TIMESTAMP, null);
	}

	public void setUserProfileFavoriteTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_PROFILE_MY_FAVORITE_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getUserProfileLeaderboardTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences
				.getString(USER_PROFILE_LEADER_BOARD_TIMESTAMP, null);
	}

	public void setUserProfileLeaderboardTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_PROFILE_LEADER_BOARD_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getUserProfileTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(USER_PROFILE_TIMESTAMP, null);
	}

	public void setUserProfileTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(USER_PROFILE_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public void setAppStartTimestamp(long value) {
		Editor editor = mPreferences.edit();
		editor.putLong(APP_START_TIMESTAMP, value);
		editor.commit();
	}

	public long getAppStartTimestamp() {
		long value = mPreferences.getLong(APP_START_TIMESTAMP, 0);
		return value;
	}

	public void setAppImagesTimestamp(long value) {
		Editor editor = mPreferences.edit();
		editor.putLong(APP_IMAGES_TIMESTAMP, value);
		editor.commit();
	}

	public long getAppImagesTimestamp() {
		long value = mPreferences.getLong(APP_IMAGES_TIMESTAMP, 0);
		return value;
	}

	public void setTotalSession(int session) {
		Editor editor = mPreferences.edit();
		editor.putInt(TOTAL_SESSION, session);
		editor.commit();
	}

	public int getTotalSession() {
		return mPreferences.getInt(TOTAL_SESSION, 0);
	}

	public void setLastSessionRegister_1(int session) {
		Editor editor = mPreferences.edit();
		editor.putInt(TOTAL_LAST_REGISTER_DISPLAY_SESSION, session);
		editor.commit();
	}

	public int getLastSessionRegister_1() {
		return mPreferences.getInt(TOTAL_LAST_REGISTER_DISPLAY_SESSION, 0);
	}

	public void setLastSessionSocialLogin2(int session) {
		Editor editor = mPreferences.edit();
		editor.putInt(TOTAL_LAST_SOCIAL_LOGIN_DISPLAY_SESSION, session);
		editor.commit();
	}

	public int getLastSessionSocialLogin2() {
		return mPreferences.getInt(TOTAL_LAST_SOCIAL_LOGIN_DISPLAY_SESSION, 0);
	}

	public void setLastSessionOffline3rdSong10(int session) {
		Editor editor = mPreferences.edit();
		editor.putInt(TOTAL_LAST_OFFLINE_3RD_SONG, session);
		editor.commit();
	}

	public int getLastSessionOffline3rdSong10() {
		return mPreferences.getInt(TOTAL_LAST_OFFLINE_3RD_SONG, 0);
	}

	public void setTimeLastOfflineTrialExpiredShown9(long session) {
		Editor editor = mPreferences.edit();
		editor.putLong(TIME_LAST_OFFLINE_TRIAL_EXPIRED_SHOWN, session);
		editor.commit();
	}

	public long getTimeLastOfflineTrialExpiredShown9() {
		return mPreferences.getLong(TIME_LAST_OFFLINE_TRIAL_EXPIRED_SHOWN, 0);
	}

	public void setCategoryPrefSelectionGeneric6(boolean session) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(CATEGORY_PREF_SELECTION_GENRIC, session);
		editor.commit();
	}

	public boolean getCategoryPrefSelectionGeneric6() {
		return mPreferences.getBoolean(CATEGORY_PREF_SELECTION_GENRIC, false);
	}

	public void setLastSessionExit3(int session) {
		Editor editor = mPreferences.edit();
		editor.putInt(TOTAL_LAST_EXIT_SESSION, session);
		editor.commit();
	}

	public int getLastSessionExit3() {
		return mPreferences.getInt(TOTAL_LAST_EXIT_SESSION, 0);
	}

	public String getMusicLatestTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null);
	}

	public void setMusicLatestTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(MUSIC_LATEST_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getVideoLatestTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(VIDEO_LATEST_TIMESTAMP, null);
	}

	public void setVideoLatestTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(VIDEO_LATEST_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getLiveRadioTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(RADIO_LIVE_TIMESTAMP, null);
	}

	public void setLiveRadioTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(RADIO_LIVE_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getOnDemandRadioTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(RADIO_ONDEMAND_TIMESTAMP, null);
	}

	public void setOnDemandTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(RADIO_ONDEMAND_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getSearchPopularTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(SEARCH_POPULAR_TIMESTAMP, null);
	}

	public void setSearchPopularTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(SEARCH_POPULAR_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getCategoriesGenerTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(CATEGORIES_GENER_TIMESTAMP, null);
	}

	public void setCategoriesGenerTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(CATEGORIES_GENER_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getLeftMenuTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(LEFT_MENU_TIMESTAMP, null);
	}

	public void setLeftMenuTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(LEFT_MENU_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getFeedbackTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(FEEDBACK_TIMESTAMP, null);
	}

	public void setFeedbackTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(FEEDBACK_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getPreferenceGetCategoryTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(PREF_GET_CATEGORY_TIMESTAMP, null);
	}

	public void setPreferenceGetCategoryTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(PREF_GET_CATEGORY_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getAllLanguagesTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(PREF_GET_ALL_LANGUAGES_TIMESTAMP, null);
	}

	public void setAllLanguagesTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(PREF_GET_ALL_LANGUAGES_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getUserLanguageTimeStamp() {
		// Logger.e("mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null)",
		// mPreferences.getString(MUSIC_LATEST_TIMESTAMP, null));
		return mPreferences.getString(PREF_GET_USER_LANGUAGES_TIMESTAMP, null);
	}

	public void setUserLanguageTimeStamp(String timestamp_cache) {
		// Logger.e("mPreferences.timestamp_cache", timestamp_cache);
		Editor editor = mPreferences.edit();
		editor.putString(PREF_GET_USER_LANGUAGES_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getMusicPopularTimeStamp() {
		return mPreferences.getString(MUSIC_POPULAR_TIMESTAMP, null);
	}

	public void setMusicPopularTimeStamp(String timestamp_cache) {
		Editor editor = mPreferences.edit();
		editor.putString(MUSIC_POPULAR_TIMESTAMP, timestamp_cache);
		editor.commit();
	}

	public String getMediaIdNs() {
		return "hungama";
	}

	public int getPageMax() {
		return 200000;
	}

	public int getPageMin() {
		return 100000;
	}

	public int getPageOptimal() {
		return 150000;
	}

	public String getRegistrationId() {

		return mPreferences.getString(REGISTRATION_ID, null);
	}

	public void setRegistrationId(String registrationId) {

		Editor editor = mPreferences.edit();
		editor.putString(REGISTRATION_ID, registrationId);
		editor.commit();
	}

	public String getPasskey() {
		return mPreferences.getString(PASSKEY, null);
	}

	public void setPasskey(String passkey) {
		Editor editor = mPreferences.edit();
		editor.putString(PASSKEY, passkey);
		editor.commit();
	}

    public String getSilentUserPasskey() {
        return mPreferences.getString(SILENT_USER_PASSKEY, null);
    }

    public void setSilentUserPasskey(String passkey) {
        Editor editor = mPreferences.edit();
        editor.putString(SILENT_USER_PASSKEY, passkey);
        editor.commit();
    }

	public String getSessionID() {
		return mPreferences.getString(SESSION_ID, null);
	}

	public void setSessionID(String sessionID) {

		// String prev_session=getSessionID();

		Editor editor = mPreferences.edit();
		editor.putString(SESSION_ID, sessionID);
		editor.commit();

		if (sessionID == null) {
			setConsumerID(0);
			setConsumerRevision(0);
			setHouseholdID(0);
			setHouseholdRevision(0);
			setPasskey(null);
			setIsRealUser(false);
		}
		// if(prev_session==null || (!TextUtils.isEmpty(sessionID) &&
		// !prev_session.equals(sessionID))){
		// mContext.getApplicationContext().startService(new
		// Intent(mContext.getApplicationContext(),
		// InventoryLightService.class));
		// }
	}

    public String getSilentUserSessionID() {
        return mPreferences.getString(SILENT_USER_SESSION_ID, null);
    }

    public void setSilentUserSessionID(String sessionID) {
        Editor editor = mPreferences.edit();
        editor.putString(SILENT_USER_SESSION_ID, sessionID);
        editor.commit();
    }

	public int getConsumerID() {
		return mPreferences.getInt(CONSUMER_ID, 0);
	}

	public void setFavSelection(int consumerID) {
		Editor editor = mPreferences.edit();
		editor.putInt(FAVORITE_SELECTION, consumerID);
		editor.commit();
	}

	public int getFavSelection() {
		return mPreferences.getInt(FAVORITE_SELECTION, -1);
	}

	public void setMyCollectionSelection(int selection) {
		Editor editor = mPreferences.edit();
		editor.putInt(MYCOLLECTION_SELECTION, selection);
		editor.commit();
	}

	public int getMyCollectionSelection() {
		return mPreferences.getInt(MYCOLLECTION_SELECTION, -1);
	}

	public void setConsumerID(int consumerID) {
		Editor editor = mPreferences.edit();
		editor.putInt(CONSUMER_ID, consumerID);
		editor.commit();

		if (needToCallInventory) {
			needToCallInventory = false;
			// setConsumerID(0);
			setConsumerRevision(0);
			// setHouseholdID(0);
			setHouseholdRevision(0);
			InventoryLightService.callService(mContext, true);

		}
	}

    public int getSilentUserConsumerID() {
        return mPreferences.getInt(SILENT_USER_CONSUMER_ID, 0);
    }

    public void setSilentUserConsumerID(int consumerID) {
        Editor editor = mPreferences.edit();
        editor.putInt(SILENT_USER_CONSUMER_ID, consumerID);
        editor.commit();
    }

	public int getHouseholdID() {
		return mPreferences.getInt(HOUSEHOLD_ID, 0);
	}

	public void setHouseholdID(int householdID) {
		Editor editor = mPreferences.edit();
		editor.putInt(HOUSEHOLD_ID, householdID);
		editor.commit();
	}

    public int getSilentUserHouseholdID() {
        return mPreferences.getInt(SILENT_USER_HOUSEHOLD_ID, 0);
    }

    public void setSilentUserHouseholdID(int householdID) {
        Editor editor = mPreferences.edit();
        editor.putInt(SILENT_USER_HOUSEHOLD_ID, householdID);
        editor.commit();
    }

	public String getSelctedMusicPreference() {
		return mPreferences.getString(APP_SELECTED_MUSIC_PREFERENCE,
				"Editors Picks");
	}

	public void setSelctedMusicPreference(String musicPreference) {
		Editor editor = mPreferences.edit();
		editor.putString(APP_SELECTED_MUSIC_PREFERENCE, musicPreference);
		editor.commit();
        if(!isRealUser()){
            setSilentUserSelctedMusicPreference(musicPreference);
        }
	}

	public String getSelctedMusicGenre() {
		return mPreferences.getString(APP_SELECTED_MUSIC_GENRE, "");
	}

	public void setSelctedMusicGenre(String musicGenre) {
		Editor editor = mPreferences.edit();
		editor.putString(APP_SELECTED_MUSIC_GENRE, musicGenre);
		editor.commit();
        if(!isRealUser()){
            setSilentUserSelctedMusicGenre(musicGenre);
        }
	}

    public String getSilentUserSelctedMusicPreference() {
//        return mPreferences.getString(APP_SILENT_USER_SELECTED_MUSIC_PREFERENCE,
//                "Editors Picks");
		return "Editors Picks";
    }

    public void setSilentUserSelctedMusicPreference(String musicPreference) {
        Editor editor = mPreferences.edit();
        editor.putString(APP_SILENT_USER_SELECTED_MUSIC_PREFERENCE, musicPreference);
        editor.commit();
    }

    public String getSilentUserSelctedMusicGenre() {
//        return mPreferences.getString(APP_SILENT_USER_SELECTED_MUSIC_GENRE, "");
		return "";
    }

    public void setSilentUserSelctedMusicGenre(String musicGenre) {
        Editor editor = mPreferences.edit();
        editor.putString(APP_SILENT_USER_SELECTED_MUSIC_GENRE, musicGenre);
        editor.commit();
    }

	public String getSelctedVideoPreference() {
		return mPreferences.getString(APP_SELECTED_VIDEO_PREFERENCE,
				"Editors Picks");
	}

	public void setSelctedVideoPreference(String musicPreference) {
		Editor editor = mPreferences.edit();
		editor.putString(APP_SELECTED_VIDEO_PREFERENCE, musicPreference);
		editor.commit();
	}

	// public int getHouseholdClientRevision() {
	// return mPreferences.getInt(HOUSEHOLD_CLIENT_REVISION, 0);
	// }
	//
	// public void setHouseholdClientRevision(int value) {
	// Editor editor = mPreferences.edit();
	// editor.putInt(HOUSEHOLD_CLIENT_REVISION, value);
	// editor.commit();
	// }

	public int getHouseholdRevision() {
		return mPreferences.getInt(HOUSEHOLD_REVISION, 0);
	}

	public void setHouseholdRevision(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(HOUSEHOLD_REVISION, value);
		editor.commit();
	}

	public String getApiDate() {
		return mPreferences.getString(API_DATE, "");
	}

	public void setApiDate(String string_files) {
		Editor editor = mPreferences.edit();
		editor.putString(API_DATE, string_files);
		editor.commit();
	}

	public String getUATagDate() {
		return mPreferences.getString(UA_TAG_DATE, "");
	}

	public void setUATagDate(String string_files) {
		Editor editor = mPreferences.edit();
		editor.putString(UA_TAG_DATE, string_files);
		editor.commit();
	}

	public String getUaTagSongConsumption() {
		return mPreferences.getString(UA_TAG_SONG_CONSUMPTION, "");
	}

	public void setUaTagSongConsumption(String string_files) {
		Editor editor = mPreferences.edit();
		editor.putString(UA_TAG_SONG_CONSUMPTION, string_files);
		editor.commit();
	}

	public String getUaTagVideoConsumption() {
		return mPreferences.getString(UA_TAG_VIDEO_CONSUMPTION, "");
	}

	public void setUaTagVideoConsumption(String string_files) {
		Editor editor = mPreferences.edit();
		editor.putString(UA_TAG_VIDEO_CONSUMPTION, string_files);
		editor.commit();
	}

	public String getUaTagUserPoint() {
		return mPreferences.getString(UA_TAG_USER_POINT, "");
	}

	public void setUaTagUserPoint(String string_files) {
		Editor editor = mPreferences.edit();
		editor.putString(UA_TAG_USER_POINT, string_files);
		editor.commit();
	}

	// TODO: ??? static ???

	// public int getConsumerClientRevision() {
	// return mPreferences.getInt(CONSUMER_CLIENT_REVISION, 0);
	// }
	//
	// public void setConsumerClientRevision(int value) {
	// Editor editor = mPreferences.edit();
	// editor.putInt(CONSUMER_CLIENT_REVISION, value);
	// editor.commit();
	// }

	public int getConsumerRevision() {
		return mPreferences.getInt(CONSUMER_REVISION, 0);
	}

	public void setConsumerRevision(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(CONSUMER_REVISION, value);
		editor.commit();
	}

	public String getDeviceID() {
		return mPreferences.getString(DEVICE_ID, null);
	}

	public void setDeviceID(String deviceID) {
		Editor editor = mPreferences.edit();
		editor.putString(DEVICE_ID, deviceID);
		editor.commit();
	}

	public void setIfDeviceExist(boolean isDeviceExist) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(EXISTING_DEVICE, isDeviceExist);
		editor.commit();
	}

	public boolean isDeviceExist() {
		return mPreferences.getBoolean(EXISTING_DEVICE, false);
	}

	public String getExistingDeviceEmail() {
		return mPreferences.getString(EXISTING_DEVICE_EMAIL, "");
	}

	public void setExistingDeviceEmail(String email) {
		Editor editor = mPreferences.edit();
		editor.putString(EXISTING_DEVICE_EMAIL, email);
		editor.commit();
	}

	// TODO: implement this.
	public String getClientType() {
		return "full";
	}

	public String getPartnerUserId() {
		return mPreferences.getString(PARTNER_USER_ID, "");
	}

	public void setPartnerUserId(String partnerUserId) {
		Editor editor = mPreferences.edit();
		editor.putString(PARTNER_USER_ID, partnerUserId);
		editor.commit();
	}

    public String getSilentPartnerUserId() {
        return mPreferences.getString(SILENT_PARTNER_USER_ID, "");
    }

    public void setSilentPartnerUserId(String partnerUserId) {
        Editor editor = mPreferences.edit();
        editor.putString(SILENT_PARTNER_USER_ID, partnerUserId);
        editor.commit();
    }

	public String getSkippedPartnerUserId() {
		return mPreferences.getString(SKIPPED_PARTNER_USER_ID, "");
	}

	public void setSkippedPartnerUserId(String partnerUserId) {
		Editor editor = mPreferences.edit();
		editor.putString(SKIPPED_PARTNER_USER_ID, partnerUserId);
		editor.commit();
	}

	private boolean needToCallInventory;

	// static Object mutext=new Object();
	public void setIsRealUser(boolean isRealUser) {
		Logger.e("setIsRealUser***", "" + isRealUser);

		Logger.e("setIsRealUser inner***", "" + isRealUser);
		// boolean existing_state=isRealUser();
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_REAL_USER, isRealUser);
		editor.commit();

		// if(!TextUtils.isEmpty(getSessionID()) && isRealUser &&
		// !existing_state){
		needToCallInventory = true;
		// }

	}

	public boolean isRealUser() {

		return mPreferences.getBoolean(IS_REAL_USER, false);

	}

	public void setIsUserRegistered(boolean isRealUser) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_USER_REGISTERED, isRealUser);
		editor.commit();
	}

	public boolean isUserRegistered() {
		return mPreferences.getBoolean(IS_USER_REGISTERED, false);
	}

	public String getUserLoginPhoneNumber() {
		return mPreferences.getString(USER_LOGIN_PHONE_NUMNBER, "");
	}

	public void setUserLoginPhoneNumber(String phoneNumber) {
		Editor editor = mPreferences.edit();
		editor.putString(USER_LOGIN_PHONE_NUMNBER, phoneNumber);
		editor.commit();
	}

    public String getSilentUserLoginPhoneNumber() {
        return mPreferences.getString(SILENT_USER_LOGIN_PHONE_NUMNBER, "");
    }

    public void setSilentUserLoginPhoneNumber(String phoneNumber) {
        Editor editor = mPreferences.edit();
        editor.putString(SILENT_USER_LOGIN_PHONE_NUMNBER, phoneNumber);
        editor.commit();
    }

	public synchronized void setIsUserHasSubscriptionPlan(
			boolean isUserHasSubscriptionPlan) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_USER_HAS_SUBSCRIPTION_PLAN,
				isUserHasSubscriptionPlan);
		editor.commit();
	}

	public boolean isUserHasSubscriptionPlan() {
		// return true;
		return mPreferences.getBoolean(IS_USER_HAS_SUBSCRIPTION_PLAN, false);
	}

	public synchronized void setIsUserHasTrialSubscriptionPlan(
			boolean isUserHasSubscriptionPlan) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_USER_HAS_TRIAL_SUBSCRIPTION_PLAN,
				isUserHasSubscriptionPlan);
		editor.commit();
	}

	public boolean isUserHasTrialSubscriptionPlan() {
		return mPreferences.getBoolean(IS_USER_HAS_TRIAL_SUBSCRIPTION_PLAN,
				false);// true;//
	}

	public synchronized void setIsShowAds(
			boolean isUserHasSubscriptionPlan) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_SHOW_ADS, isUserHasSubscriptionPlan);
		editor.commit();
	}

	public boolean isShowAds() {
		return mPreferences.getBoolean(IS_SHOW_ADS, true);
	}

	public synchronized void setIsUserTrialSubscriptionExpired(
			boolean isUserSubscriptionExpired) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_USER_TRIAL_SUBSCRIPTION_EXPIRED,
				isUserSubscriptionExpired);
		editor.commit();
	}

	public boolean isUserTrialSubscriptionExpired() {
		return mPreferences.getBoolean(IS_USER_TRIAL_SUBSCRIPTION_EXPIRED,
				false);
	}

	public synchronized void setTrialExpiryDaysLeft(int daysLeft) {
		Editor editor = mPreferences.edit();
		editor.putInt(USER_TRIAL_SUBSCRIPTION_DAYS_LEFT, daysLeft);
		editor.commit();
	}

	public int getTrialSubscriptionDaysLeft() {
		return mPreferences.getInt(USER_TRIAL_SUBSCRIPTION_DAYS_LEFT, 0);
	}

	public synchronized void setUserSubscriptionPlanDate(
			String userSubscriptionPlanDate) {
		Editor editor = mPreferences.edit();
		editor.putString(USER_SUBSCRIPTION_PLAN_DATE, userSubscriptionPlanDate);
		editor.commit();
	}

	public String getUserSubscriptionPlanDate() {
		return mPreferences.getString(USER_SUBSCRIPTION_PLAN_DATE, "");
	}

	public synchronized void setUserSubscriptionPlanDatePurchase(
			String userSubscriptionPlanDatePurchase) {
		Editor editor = mPreferences.edit();
		editor.putString(USER_SUBSCRIPTION_PLAN_DATE_PURCHASE,
				userSubscriptionPlanDatePurchase);
		editor.commit();
	}

	public String getUserSubscriptionPlanDatePurchase() {
		return mPreferences.getString(USER_SUBSCRIPTION_PLAN_DATE_PURCHASE, "");
	}

	public synchronized void setUserSubscriptionPlanDetails(
			String userSubscriptionPlanDatePurchase) {
		Editor editor = mPreferences.edit();
		editor.putString(USER_SUBSCRIPTION_PLAN_DETAILS,
				userSubscriptionPlanDatePurchase);
		editor.commit();
	}

	public String getUserSubscriptionPlanDetails() {
		return mPreferences.getString(USER_SUBSCRIPTION_PLAN_DETAILS, "");
	}

	public void setIsFirstVisitToApp(boolean isFirstVisitToApp) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_VISIT_TO_APP, isFirstVisitToApp);
		editor.commit();
	}

	public boolean isFirstVisitToApp() {
		return mPreferences.getBoolean(IS_FIRST_VISIT_TO_APP, true);
	}

	public void setIsInventoryFetch(boolean isInventoryFetch) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_INVENTORY_FETCH, isInventoryFetch);
		editor.commit();
	}

	public boolean isInventoryFetch() {
		return mPreferences.getBoolean(IS_INVENTORY_FETCH, false);
	}

	public void setIsFirstVisitToHomeTilePage(boolean isFirstVisitToHomeTilePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_VISIT_TO_HOME_TILE_PAGE,
				isFirstVisitToHomeTilePage);
		editor.commit();
	}

	public boolean isFirstVisitToHomeTilePage() {
		return mPreferences.getBoolean(IS_FIRST_VISIT_TO_HOME_TILE_PAGE, true);
	}

	public void setIsHomeHintShownInThisSession(
			boolean isHomeHintShownInThisSession) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_HOME_HINT_SHOWN_IN_THIS_SESSION,
				isHomeHintShownInThisSession);
		editor.commit();
	}

    public void setGcmToken(
            boolean isgcmtoken) {
        Editor editor = mPreferences.edit();
        editor.putBoolean(IS_GCM_TOKEN,
                isgcmtoken);
        editor.commit();
    }
    public boolean getGcmToken() {
        return mPreferences.getBoolean(IS_GCM_TOKEN,
                false);
    }

	public boolean isHomeHintShownInThisSession() {
		return mPreferences.getBoolean(IS_HOME_HINT_SHOWN_IN_THIS_SESSION,
				false);
	}

	public void setIsSearchFilterShownInThisSession(
			boolean isSearchFilterShownInThisSession) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_SEARCH_FILTER_SHOWN_IN_THIS_SESSION,
				isSearchFilterShownInThisSession);
		editor.commit();
	}

	public boolean isSearchFilterShownInThisSession() {
		return mPreferences.getBoolean(IS_SEARCH_FILTER_SHOWN_IN_THIS_SESSION,
				false);
	}

	public void setIsPlayerQueueHintShownInThisSession(
			boolean isPlayerQueueHintShownInThisSession) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_PLAYER_QUEUE_HINT_SHOWN_IN_THIS_SESSION,
				isPlayerQueueHintShownInThisSession);
		editor.commit();
	}

	public boolean isPlayerQueueHintShownInThisSession() {
		return mPreferences.getBoolean(
				IS_PLAYER_QUEUE_HINT_SHOWN_IN_THIS_SESSION, false);
	}

	public void setIsFirstVisitToSearchPage(boolean isFirstVisitToSearchPage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_VISIT_TO_SEARCH_PAGE,
				isFirstVisitToSearchPage);
		editor.commit();
	}

	public boolean isFirstVisitToSearchPage() {
		return mPreferences.getBoolean(IS_FIRST_VISIT_TO_SEARCH_PAGE, true);
	}

	public void setIsFirstVisitToFullPlayer(boolean isFirstVisitToFullPlayer) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_VISIT_TO_FULL_PLAYER,
				isFirstVisitToFullPlayer);
		editor.commit();
	}

	public boolean isFirstVisitToFullPlayer() {
		return mPreferences.getBoolean(IS_FIRST_VISIT_TO_FULL_PLAYER, true);
	}

	public void setIsEnabledHomeGuidePage(boolean isEnabledHomeGuidePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_HOME_GUIDE_PAGE, isEnabledHomeGuidePage);
		editor.commit();
	}

	public void setIsEnabledHomeGuidePage_3OFFLINE(
			boolean isEnabledHomeGuidePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_HOME_GUIDE_PAGE_3_OFFLINE,
				isEnabledHomeGuidePage);
		editor.commit();
	}

	public void setIsSongCatched(boolean isSongCatched) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_SONG_CATCHED, isSongCatched);
		editor.commit();
	}

	public void setIsEnabledProGuidePage_3OFFLINE(boolean isEnabledHomeGuidePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_PRO_GUIDE_PAGE_3_OFFLINE,
				isEnabledHomeGuidePage);
		editor.commit();
	}

	public boolean isEnabledHomeGuidePage3Offline() {
		return mPreferences.getBoolean(IS_ENABLED_HOME_GUIDE_PAGE_3_OFFLINE,
				true);
	}

	public boolean isSongCatched() {
		return mPreferences.getBoolean(IS_SONG_CATCHED, false);
	}

	public boolean isEnabledProGuidePage3Offline() {
		return mPreferences.getBoolean(IS_ENABLED_PRO_GUIDE_PAGE_3_OFFLINE,
				true);
	}

	public boolean isEnabledHomeGuidePage() {
		return mPreferences.getBoolean(IS_ENABLED_HOME_GUIDE_PAGE, true);
	}

	public void setIsEnabledGymModeGuidePage(boolean isEnabledGymModeGuidePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_GYM_MODE_GUIDE_PAGE,
				isEnabledGymModeGuidePage);
		editor.commit();
	}

	public boolean isEnabledGymModeGuidePage() {
		return mPreferences.getBoolean(IS_ENABLED_GYM_MODE_GUIDE_PAGE, true);
	}

	public void setIsEnabledDiscoverGuidePage(boolean isEnabledDiscoverGuidePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_DISCOVER_GUIDE_PAGE,
				isEnabledDiscoverGuidePage);
		editor.commit();
	}

	public boolean isEnabledDiscoverGuidePage() {
		return mPreferences.getBoolean(IS_ENABLED_DISCOVER_GUIDE_PAGE, true);
	}

	public void setIsEnabledDownloadDeleteGuidePage(
			boolean isEnabledDownloadDeleteGuidePage) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_DOWNLOAD_DELETE_GUIDE_PAGE,
				isEnabledDownloadDeleteGuidePage);
		editor.commit();
	}

	public boolean isEnabledDownloadDeleteGuidePage() {
		return mPreferences.getBoolean(IS_ENABLED_DOWNLOAD_DELETE_GUIDE_PAGE,
				true);
	}

	public void setGigyaSignup(SignOption value) {
		Gson gson = new Gson();
		String json = gson.toJson(value);

		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_SIGNUP, json);
		editor.commit();
	}

	public SignOption getGigyaSignup() {
		SignOption signOption = null;
		Gson gson = new Gson();

		String json = mPreferences.getString(GIGYA_SIGNUP, "");
		signOption = gson.fromJson(json, SignOption.class);
		return signOption;
	}

	// FaceBook first name and last name
	public void setGigyaFBFirstName(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_FB_FIRST_NAME, value);
		editor.commit();
	}

	public String getGigyaFBFirstName() {
		String value = mPreferences.getString(GIGYA_FB_FIRST_NAME, "");
		return value;
	}

	public void setGigyaFBLastName(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_FB_LAST_NAME, value);
		editor.commit();
	}

	public String getGigyaFBLastName() {
		String value = mPreferences.getString(GIGYA_FB_LAST_NAME, "");
		return value;
	}

	public void setGigyaFBEmail(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_FB_EMAIL, value);
		editor.commit();
	}

	public String getGigyaFBEmail() {
		String value = mPreferences.getString(GIGYA_FB_EMAIL, "");
		return value;
	}

	public void setGigyaSessionToken(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_LOGIN_SESSION_TOKEN, value);
		editor.commit();
	}

	public String getGigyaSessionToken() {
		String value = mPreferences.getString(GIGYA_LOGIN_SESSION_TOKEN, "");
		return value;
	}

	public void setGigyaSessionSecret(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_LOGIN_SESSION_SECRET, value);
		editor.commit();
	}

	public String getGigyaSessionSecret() {
		String value = mPreferences.getString(GIGYA_LOGIN_SESSION_SECRET, "");
		return value;
	}

    public void setSilentUserGigyaSessionToken(String value) {
        Editor editor = mPreferences.edit();
        editor.putString(SILENT_USER_GIGYA_LOGIN_SESSION_TOKEN, value);
        editor.commit();
    }

    public String getSilentUserGigyaSessionToken() {
        String value = mPreferences.getString(SILENT_USER_GIGYA_LOGIN_SESSION_TOKEN, "");
        return value;
    }

    public void setSilentUserGigyaSessionSecret(String value) {
        Editor editor = mPreferences.edit();
        editor.putString(SILENT_USER_GIGYA_LOGIN_SESSION_SECRET, value);
        editor.commit();
    }

    public String getSilentUserGigyaSessionSecret() {
        String value = mPreferences.getString(SILENT_USER_GIGYA_LOGIN_SESSION_SECRET, "");
        return value;
    }

	//

	// Twitter first name and last name
	public void setGigyaTwitterFirstName(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_TWITTER_FIRST_NAME, value);
		editor.commit();
	}

	public String getGigyaTwitterFirstName() {
		String value = mPreferences.getString(GIGYA_TWITTER_FIRST_NAME, "");
		return value;
	}

	public void setGigyaTwitterLastName(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_TWITTER_LAST_NAME, value);
		editor.commit();
	}

	public String getGigyaTwitterLastName() {
		String value = mPreferences.getString(GIGYA_TWITTER_LAST_NAME, "");
		return value;
	}

	public void setGigyaTwitterEmail(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_TWITTER_EMAIL, value);
		editor.commit();
	}

	public String getGigyaTwitterEmail() {
		String value = mPreferences.getString(GIGYA_TWITTER_EMAIL, "");
		return value;
	}

	//

	// Google first name and last name
	public void setGigyaGoogleFirstName(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_GOOGLE_FIRST_NAME, value);
		editor.commit();
	}

	public String getGigyaGoogleFirstName() {
		String value = mPreferences.getString(GIGYA_GOOGLE_FIRST_NAME, "");
		return value;
	}

	public void setGigyaGoogleLastName(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_GOOGLE_LAST_NAME, value);
		editor.commit();
	}

	public String getGigyaGoogleLastName() {
		String value = mPreferences.getString(GIGYA_GOOGLE_LAST_NAME, "");
		return value;
	}

	public void setGigyaGoogleEmail(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_GOOGLE_EMAIL, value);
		editor.commit();
	}

	public String getGigyaGoogleEmail() {
		String value = mPreferences.getString(GIGYA_GOOGLE_EMAIL, "");
		return value;
	}

	//

	// Hungama first name and last name
	public void setHungamaFirstName(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(HUNGAMA_FIRST_NAME, value);
		editor.commit();
	}

	public String getHungmaFirstName() {
		String value = mPreferences.getString(HUNGAMA_FIRST_NAME, "");
		return value;
	}

	public void setHungamaLastName(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(HUNGAMA_LAST_NAME, value);
		editor.commit();
	}

	public String getHungamaLastName() {
		String value = mPreferences.getString(HUNGAMA_LAST_NAME, "");
		return value;
	}

	public void setHungamaEmail(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(HUNGAMA_EMAIL, value);
		editor.commit();
	}

	public String getHungamaEmail() {
		String value = mPreferences.getString(HUNGAMA_EMAIL, "");
		return value;
	}

	public void setGiGyaFBThumbUrl(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_FB_THUMB_URL, value);
		editor.commit();
	}

	public String getGiGyaFBThumbUrl() {
		String value = mPreferences.getString(GIGYA_FB_THUMB_URL, "");
		return value;
	}

	public void setGiGyaTwitterThumbUrl(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(GIGYA_TWITTER_THUMB_URL, value);
		editor.commit();
	}

	public String getGiGyaTwitterThumbUrl() {
		String value = mPreferences.getString(GIGYA_TWITTER_THUMB_URL, "");
		return value;
	}

	public void setPlayerVolume(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(PLAYER_VOLUME, value);
		editor.commit();
	}

	public int getPlayerVolume() {
		int value = mPreferences.getInt(PLAYER_VOLUME, 50);
		return value;
	}

	public void setBitRateState(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(BITRATE_STATE, value);
		editor.commit();
	}

	public int getBitRateState() {
		int value = mPreferences.getInt(BITRATE_STATE, BITRATE_AUTO);
		if (value == BITRATE_HD
				&& (!isUserHasSubscriptionPlan() && !isUserHasTrialSubscriptionPlan())) {
			setBitRateState(ApplicationConfigurations.BITRATE_AUTO);
			value = BITRATE_AUTO;
		}
		if (Logger.defalutLowBitrate)
			value = BITRATE_LOW;
		return value;
	}

	public void setBitRate(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(BITRATE, value);
		editor.commit();
	}

	public int getBitRate() {
		int value = mPreferences.getInt(BITRATE, BITRATE_NONE);
		return value;
	}

	public void setBandwidth(long value) {
		Editor editor = mPreferences.edit();
		editor.putLong(BANDWIDTH, value);
		editor.commit();
	}

	public long getBandwidth() {
		long value = mPreferences.getLong(BANDWIDTH, 0);
		return value;
	}

	public void setFileSizeForBitrateCalculation(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(FILE_SIZE_FOR_BITRATE_CALCULATION, value);
		editor.commit();
	}

	public int getFileSizeForBitrateCalculation() {
		int value = mPreferences.getInt(FILE_SIZE_FOR_BITRATE_CALCULATION,
				BITRATE_NONE);
		return value;
	}

	public void setHintsState(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(HINTS_STATE, value);
		editor.commit();
	}

	public boolean getHintsState() {
		boolean value = mPreferences.getBoolean(HINTS_STATE, false);
		return value;
	}

	// ======================================================
	// Settings for syncing prefetched moods, due to heavy long process.
	// ======================================================

	private Object mPrefetchMoodsSuccessFlagMutext = new Object();

	public void setDiscoverPrefetchMoodsSuccess(boolean isSucces) {
		synchronized (mPrefetchMoodsSuccessFlagMutext) {
			Editor editor = mPreferences.edit();
			editor.putBoolean(DISCOVER_PREFETCH_MOODS_SUCCESS_FLAG, isSucces);
			editor.commit();
		}
	}

	public boolean isDiscoverPrefetchMoodsSuccess() {
		synchronized (mPrefetchMoodsSuccessFlagMutext) {
			return mPreferences.getBoolean(
					DISCOVER_PREFETCH_MOODS_SUCCESS_FLAG, false);
		}
	}

	// ======================================================
	// Mobile Verified - in Download and Upgrade
	// ======================================================

	// public void setMobileNumber(String mobile, int isVerified) {
	//
	// Editor editor = mPreferences.edit();
	// editor.putInt(mobile, isVerified);
	// editor.commit();
	// }

//	public int isMobileNumberVerified(String mobile) {
//		return 1;
//		// return mPreferences.getInt(mobile, -1);
//	}
//
//	public void setTempClickedDownloadPlan(DownloadPlan clickedPlan) {
//
//		Editor editor = mPreferences.edit();
//		editor.putInt(DOWNLOAD_PLAN_ID, clickedPlan.getPlanId());
//		editor.putString(DOWNLOAD_PLAN_TYPE, clickedPlan.getType());
//		editor.commit();
//	}

//	public DownloadPlan getTempClickedDownloadPlan() {
//
//		int planId = mPreferences.getInt(DOWNLOAD_PLAN_ID, 0);
//		String type = mPreferences.getString(DOWNLOAD_PLAN_TYPE,
//				Utils.TEXT_EMPTY);
//
//		if (planId != 0 && !TextUtils.isEmpty(type)) {
//			return new DownloadPlan(planId, Utils.TEXT_EMPTY, Utils.TEXT_EMPTY,
//					Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, 0, 0,
//					type, 0);
//		}
//		return null;
//	}

//	public void setTempClickedPlan(Plan clickedPlan) {
//
//		Editor editor = mPreferences.edit();
//		editor.putInt(DOWNLOAD_PLAN_ID, clickedPlan.getPlanId());
//		editor.putString(DOWNLOAD_PLAN_TYPE, clickedPlan.getType());
//		editor.putString(DOWNLOAD_PLAN_NAME, clickedPlan.getPlanName());
//		editor.putString(DOWNLOAD_PLAN_PRICE, clickedPlan.getPlanPrice());
//		editor.putString(DOWNLOAD_PLAN_CURRENCY, clickedPlan.getPlanCurrency());
//		editor.putInt(DOWNLOAD_PLAN_DURATION, clickedPlan.getPlanDuration());
//		editor.putString(DOWNLOAD_MSISDN, clickedPlan.getMsisdn());
//		editor.putString(DOWNLOAD_SUBSCRIPTION_STATUS,
//				clickedPlan.getSubscriptionStatus());
//		editor.putString(DOWNLOAD_VALIDITY_DATE, clickedPlan.getValidityDate());
//		if (clickedPlan.isTrial()
//				|| clickedPlan.getPlanName().equalsIgnoreCase(
//						PlanType.TRIAL.toString()))
//			editor.putString(DOWNLOAD_TRIAL, "Y");
//		else
//			editor.putString(DOWNLOAD_TRIAL, "N");
//		editor.putString(DOWNLOAD_PRODUCT_ID, clickedPlan.getProductId());
//		editor.commit();
//	}
//
//	public Plan getTempClickedPlan() {
//
//		int planId = mPreferences.getInt(DOWNLOAD_PLAN_ID, 0);
//		String type = mPreferences.getString(DOWNLOAD_PLAN_TYPE,
//				Utils.TEXT_EMPTY);
//
//		String PlanName = mPreferences.getString(DOWNLOAD_PLAN_NAME,
//				Utils.TEXT_EMPTY);
//		String planPrice = mPreferences.getString(DOWNLOAD_PLAN_PRICE,
//				Utils.TEXT_EMPTY);
//		String planCurrency = mPreferences.getString(DOWNLOAD_PLAN_CURRENCY,
//				Utils.TEXT_EMPTY);
//		int planDuration = mPreferences.getInt(DOWNLOAD_PLAN_DURATION, 0);
//		String msisdn = mPreferences.getString(DOWNLOAD_MSISDN,
//				Utils.TEXT_EMPTY);
//		String subscriptionStatus = mPreferences.getString(
//				DOWNLOAD_SUBSCRIPTION_STATUS, Utils.TEXT_EMPTY);
//		String validityDate = mPreferences.getString(DOWNLOAD_VALIDITY_DATE,
//				Utils.TEXT_EMPTY);
//		String trial = mPreferences.getString(DOWNLOAD_TRIAL, Utils.TEXT_EMPTY);
//		int daysLeft = mPreferences.getInt(DOWNLOAD_DAYS_LEFT, 0);
//		String productId = mPreferences.getString(DOWNLOAD_PRODUCT_ID, "");
//
//		if (planId != 0 && !TextUtils.isEmpty(type)) {
//			return new Plan(planId, PlanName, planPrice, planCurrency,
//					planDuration, msisdn, subscriptionStatus, Utils.TEXT_EMPTY,
//					validityDate, Utils.TEXT_EMPTY, Utils.TEXT_EMPTY, type,
//					trial, daysLeft, productId);
//		}
//		return null;
//	}

	public void setBadgesAndCoinsForVideoActivity(
			BadgesAndCoins objFromOperation) {
		Editor editor = mPreferences.edit();
		editor.putInt(BADGES_AND_COINS_BADGES_EARNED,
				objFromOperation.getBadgesEarned());
		editor.putInt(BADGES_AND_COINS_DISPLAY_CASE,
				objFromOperation.getDisplayCase());
		editor.putInt(BADGES_AND_COINS_POINTS_EARNED,
				objFromOperation.getPointsEarned());
		editor.putString(BADGES_AND_COINS_BADGE_NAME,
				objFromOperation.getBadgeName());
		editor.putString(BADGES_AND_COINS_BADGE_URL,
				objFromOperation.getBadgeUrl());
		editor.putString(BADGES_AND_COINS_MESSAGE,
				objFromOperation.getMessage());
		editor.putString(BADGES_AND_COINS_NEXT_DESCRIPTION,
				objFromOperation.getNextDescription());
		editor.commit();
	}

	public BadgesAndCoins getBadgesAndCoinsForVideoActivity() {
		int badgesEarned = mPreferences.getInt(BADGES_AND_COINS_BADGES_EARNED,
				0);
		int displayCase = mPreferences.getInt(BADGES_AND_COINS_DISPLAY_CASE, 0);
		int pointsEarned = mPreferences.getInt(BADGES_AND_COINS_POINTS_EARNED,
				0);
		String badgeName = mPreferences.getString(BADGES_AND_COINS_BADGE_NAME,
				Utils.TEXT_EMPTY);
		String badgeUrl = mPreferences.getString(BADGES_AND_COINS_BADGE_URL,
				Utils.TEXT_EMPTY);
		String message = mPreferences.getString(BADGES_AND_COINS_MESSAGE,
				Utils.TEXT_EMPTY);
		String nextDescription = mPreferences.getString(
				BADGES_AND_COINS_NEXT_DESCRIPTION, Utils.TEXT_EMPTY);

		BadgesAndCoins badgesAndCoins = new BadgesAndCoins();
		badgesAndCoins.setBadgeName(badgeName);
		badgesAndCoins.setBadgesEarned(badgesEarned);
		badgesAndCoins.setBadgeUrl(badgeUrl);
		badgesAndCoins.setDisplayCase(displayCase);
		badgesAndCoins.setMessage(message);
		badgesAndCoins.setNextDescription(nextDescription);
		badgesAndCoins.setPointsEarned(pointsEarned);

		if (!badgesAndCoins.getMessage().equalsIgnoreCase(Utils.TEXT_EMPTY)) {
			return badgesAndCoins;
		}
		return null;
	}

	// ======================================================
	// One Time Badge appirater
	// ======================================================

	public void setIsFirstBadgeDisplayed(boolean isFirstBadgeDisplayed) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_BADGE_DISPLAYED, isFirstBadgeDisplayed);
		editor.commit();
	}

	public boolean isFirstBadgeDisplayed() {
		return mPreferences.getBoolean(IS_FIRST_BADGE_DISPLAYED, true);
	}

	// ======================================================
	// Moods Prefetching
	// ======================================================

	public void setHasSuccessedPrefetchingMoods(boolean hasSuccess) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(MOODS_PREFETCHING_SUCCESS, hasSuccess);
		editor.commit();
	}

	public boolean hasSuccessedPrefetchingMoods() {
		return mPreferences.getBoolean(MOODS_PREFETCHING_SUCCESS, false);
	}

	// ======================================================
	// Time Read from CM
	// ======================================================

	public void setTimeReadDelta(long value) {
		Editor editor = mPreferences.edit();
		editor.putLong(TIME_READ_DELTA, value);
		editor.commit();
	}

	public long getTimeReadDelta() {
		// Date date = new Date();
		long value = mPreferences.getLong(TIME_READ_DELTA, 0);
		return value;
	}

	// ======================================================
	// Version Check
	// ======================================================

	public synchronized void setisVersionChecked(boolean isVersionChecked) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_VERSION_CHECKED, isVersionChecked);
		editor.commit();
	}

	public boolean isVersionChecked() {
		return mPreferences.getBoolean(IS_VERSION_CHECKED, false);
	}

	// ======================================================
	// Time Read from CM
	// ======================================================

	public void setContentFormat(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(CONTENT_FORMAT, value);
		editor.commit();
	}

	public String getContentFormat() {
		String value = mPreferences.getString(CONTENT_FORMAT, Utils.TEXT_EMPTY);
		return value;
	}

	public void setSubscriptionIABcode(String code) {
		Editor editor = mPreferences.edit();
		editor.putString(SUBSCRIPTION_IAB_CODE, code);
		editor.commit();
	}

	public String getSubscriptionIABcode() {
		String code = mPreferences.getString(SUBSCRIPTION_IAB_CODE,
				Utils.TEXT_EMPTY);
		return code;
	}

	public void setSubscriptionIABpurchseToken(String purchseToken) {
		Editor editor = mPreferences.edit();
		editor.putString(SUBSCRIPTION_IAB_PURCHSE_TOKEN, purchseToken);
		editor.commit();
	}

	public String getSubscriptionIABpurchseToken() {
		String purchseToken = mPreferences.getString(
				SUBSCRIPTION_IAB_PURCHSE_TOKEN, Utils.TEXT_EMPTY);
		return purchseToken;
	}

	public void setSaveOfflineMode(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(SAVE_OFFLINE_MODE, value);
		editor.commit();
	}

	public boolean getSaveOfflineMode() {
		try {
			if (getSaveOfflineAutoMode())
				return true;
			boolean mode = mPreferences.getBoolean(SAVE_OFFLINE_MODE, false);
			return mode;
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	public void setSaveOfflineAutoSaveMode(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(SAVE_OFFLINE_AUTO_SAVE, value);
		editor.commit();
	}

	public boolean getSaveOfflineAutoSaveMode() {
		boolean mode = mPreferences.getBoolean(SAVE_OFFLINE_AUTO_SAVE, false);
		return mode;
	}

	public void setSaveOfflineAutoSaveFirstTime(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(SAVE_OFFLINE_AUTO_SAVE_FIRST_TIME, value);
		editor.commit();
	}

	public boolean getSaveOfflineAutoSaveModeFirstTime() {
		boolean mode = mPreferences.getBoolean(
				SAVE_OFFLINE_AUTO_SAVE_FIRST_TIME, true);
		return mode;
	}

	public void setSaveOfflineAutoSaveFreeUser(long value) {
		Editor editor = mPreferences.edit();
		editor.putLong(SAVE_OFFLINE_AUTO_SAVE_FREE_USER, value);
		editor.commit();
	}

	public long getSaveOfflineAutoSaveFreeUser() {
		long mode = mPreferences.getLong(SAVE_OFFLINE_AUTO_SAVE_FREE_USER, 0);
		return mode;
	}

	public void setSaveOfflineOnCellularNetwork(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(SAVE_OFFLINE_CELLULAR_NETWORK, value);
		editor.commit();
	}

	public boolean getSaveOfflineOnCellularNetwork() {
		boolean mode = mPreferences.getBoolean(SAVE_OFFLINE_CELLULAR_NETWORK,
				false);
		return mode;
	}

	public void setSaveOfflineMemoryAllocatedPercentage(int percentage) {
		Editor editor = mPreferences.edit();
		editor.putInt(SAVE_OFFLINE_MEMORY_ALLOCATED, percentage);
		editor.commit();
	}

	public int getSaveOfflineMemoryAllocatedPercentage() {
		int percentage = mPreferences.getInt(SAVE_OFFLINE_MEMORY_ALLOCATED, 30);
		return percentage;
	}

	public void setSaveOfflineMaximumMemoryAllocated(long maxSize) {
		Editor editor = mPreferences.edit();
		editor.putLong(SAVE_OFFLINE_MEMORY_MAX, maxSize);
		editor.commit();
	}

	public long getSaveOfflineMaximumMemoryAllocated() {
		long maxSize = mPreferences.getInt(SAVE_OFFLINE_MEMORY_MAX, 0);
		return maxSize;
	}

	public void setSaveOfflineUpgradePopupSeen(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(SAVE_OFFLINE_UPGRADE_POPUP, value);
		editor.commit();
	}

	public boolean isSaveOfflineUpgradePopupSeen() {
		boolean mode = mPreferences.getBoolean(SAVE_OFFLINE_UPGRADE_POPUP,
				false);
		return mode;
	}

	public void setPlayerQueue(String tracks) {
		Editor editor = mPreferences.edit();
		editor.putString(PLAYER_QUEUE, tracks);
		editor.commit();
	}

	public String getPlayerQueue() {
		return mPreferences.getString(PLAYER_QUEUE, null);
	}

	// ======================================================
	// Application Images Prefetching
	// ======================================================

	public void setHasSuccessedPrefetchingApplicationImages(boolean hasSuccess) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APPLICATION_IMAGES_PREFETCHING_SUCCESS, hasSuccess);
		editor.commit();
	}

	public boolean hasSuccessedPrefetchingApplicationImages() {
		return mPreferences.getBoolean(APPLICATION_IMAGES_PREFETCHING_SUCCESS,
				false);
	}

	public void setApplicationTextList(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(APPLICATION_TEXT_LISTS, value);
		editor.commit();
	}

	public String getApplicationTextList() {
		return mPreferences.getString(APPLICATION_TEXT_LISTS, null);
	}

	public void setAdRefreshInterval(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APPLICATION_AD_REFRESH_INTERVAL, value);
		editor.commit();
	}

	public int getAdRefreshInterval() {
		int value = mPreferences.getInt(APPLICATION_AD_REFRESH_INTERVAL, 30);
		if (value > 0)
			return value;
		else
			return 30;
	}

	public void setFreeCacheLimit(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APPLICATION_FREE_CACHE_LIMIT, value);
		editor.commit();
	}

	public int getFreeCacheLimit() {
		int value = mPreferences.getInt(APPLICATION_FREE_CACHE_LIMIT,
				CacheManager.FREE_USER_CACHE_LIMIT);
		return value;
	}

	public void increaseFreeUserCacheCount() {
		Editor editor = mPreferences.edit();
		editor.putInt(APPLICATION_FREE_USER_CACHE_COUNT,
				getFreeUserCacheCount() + 1);
		editor.commit();
	}

	public void decreaseFreeUserCacheCount() {
		Editor editor = mPreferences.edit();
		if (getFreeUserCacheCount() > 0) {
			editor.putInt(APPLICATION_FREE_USER_CACHE_COUNT,
					getFreeUserCacheCount() - 1);
			editor.commit();
		}
	}

	public void setFreeUserDeleteCount(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APPLICATION_FREE_USER_DELETE_COUNT, value);
		editor.commit();
	}

	public int getFreeUserDeleteCount() {
		int value = mPreferences.getInt(APPLICATION_FREE_USER_DELETE_COUNT,
				0);
		return value;
	}

	public void increaseFreeUserDeleteCount() {
		Editor editor = mPreferences.edit();
		editor.putInt(APPLICATION_FREE_USER_DELETE_COUNT,
				getFreeUserDeleteCount() + 1);
		editor.commit();
	}

	public void decreaseFreeUserDeleteCount() {
		Editor editor = mPreferences.edit();
		if (getFreeUserDeleteCount() > 0) {
			editor.putInt(APPLICATION_FREE_USER_DELETE_COUNT,
					getFreeUserDeleteCount() - 1);
			editor.commit();
		}
	}

	public int getFreeUserCacheCount() {
		int value = mPreferences.getInt(APPLICATION_FREE_USER_CACHE_COUNT, 0);
		return value;
	}

	public void setFreeUserCacheCountFirstTime(int count) {
		Editor editor = mPreferences.edit();
		editor.putInt(APPLICATION_FREE_USER_CACHE_COUNT,count);
		editor.commit();
	}

	public void setTimeout(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APPLICATION_TIME_OUT, value);
		editor.commit();
	}

	public int getTimeout() {
		int value = mPreferences.getInt(APPLICATION_TIME_OUT,
				CommunicationManager.CONNECTION_TIMEOUT_INTERVAL_MILLISECONDS);
		return value;
	}

	public void setRetry(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APPLICATION_RETRY, value);
		editor.commit();
	}

	public int getRetry() {
		int value = mPreferences.getInt(APPLICATION_RETRY, 3);
		return value;
	}

	public void setSplashAdTimeWait(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APPLICATION_SPLASHAD_TIME_WAIT, value);
		editor.commit();
	}

	public int getSplashAdTimeWait() {
		int value = mPreferences.getInt(APPLICATION_SPLASHAD_TIME_WAIT, 5);
		return value;
	}

	public String getNawrasMsisdn() {
		return mPreferences.getString(NAWRAS_MSISDN, "");
	}

	public void setNawrasMsisdn(String nawrasMsisdn) {
		Editor editor = mPreferences.edit();
		editor.putString(NAWRAS_MSISDN, nawrasMsisdn);
		editor.commit();
	}

	public void setSeletedPreferences(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(SELECTED_PREFERENCES, value);
		editor.commit();
	}

	public String getSeletedPreferences() {
		String value = mPreferences.getString(SELECTED_PREFERENCES,
				Utils.TEXT_EMPTY);
		return value;
	}

	public void setSeletedPreferencesVideo(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(SELECTED_PREFERENCES_VIDEO, value);
		editor.commit();
	}

	public String getSeletedPreferencesVideo() {
		String value = mPreferences.getString(SELECTED_PREFERENCES_VIDEO,
				Utils.TEXT_EMPTY);
		return value;
	}

	public int getUserSelectedLanguage() {
		if (mPreferences != null)
			return mPreferences.getInt(USER_SELECTED_LANGUAGE, 0);
		return 0;
	}

	public void setUserSelectedLanguage(int userLanguage) {
		Editor editor = mPreferences.edit();
		editor.putInt(USER_SELECTED_LANGUAGE, userLanguage);
		editor.commit();
		setIsLanguageSupportedForWidget(userLanguage);
        if(!isRealUser()){
            setSilentUserSelectedLanguage(userLanguage);
        }
	}

    public int getSilentUserSelectedLanguage() {
//        if (mPreferences != null)
//            return mPreferences.getInt(SILENT_USER_SELECTED_LANGUAGE, 0);
        return 0;
    }

    public void setSilentUserSelectedLanguage(int userLanguage) {
        Editor editor = mPreferences.edit();
        editor.putInt(SILENT_USER_SELECTED_LANGUAGE, userLanguage);
        editor.commit();
    }

	public boolean isLanguageSupportedForWidget() {
		if (mPreferences != null)
			return mPreferences.getBoolean(IS_LANGUAGE_SUPPORTED_FOR_WIDGET,
					false);
		return false;
	}

	public void setIsLanguageSupportedForWidget(int userLanguage) {
		boolean value = false;
		String languageCode = "";
		if (userLanguage == Constants.LANGUAGE_HINDI) {
			languageCode = Constants.LANGUAGE_CODE_HINDI;
		} else if (userLanguage == Constants.LANGUAGE_PUNJABI) {
			languageCode = Constants.LANGUAGE_CODE_PUNJABI;
		} else if (userLanguage == Constants.LANGUAGE_TAMIL) {
			languageCode = Constants.LANGUAGE_CODE_TAMIL;
		} else if (userLanguage == Constants.LANGUAGE_TELUGU) {
			languageCode = Constants.LANGUAGE_CODE_TELUGU;
		} else {
			value = true;
		}
		if (!TextUtils.isEmpty(languageCode)) {
			final Locale[] availableLocales = Locale.getAvailableLocales();
			for (final Locale locale : availableLocales) {
				if (locale.getCountry().equalsIgnoreCase("IN")
						&& locale.getLanguage().equalsIgnoreCase(languageCode)) {
					Logger.d(
							"AppConfig",
							"language ::- " + locale.getDisplayName() + ":"
									+ locale.getLanguage() + ":"
									+ locale.getCountry() + ":values-"
									+ locale.toString().replace("_", "-r"));
					value = true;
					break;
				}
			}
		}
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_LANGUAGE_SUPPORTED_FOR_WIDGET, value);
		editor.commit();
	}

	public String getUserSelectedLanguageText() {
		String language = "";
		if (mPreferences != null)
			language = mPreferences.getString(USER_SELECTED_LANGUAGE_TEXT, "");
		if(TextUtils.isEmpty(language)){
			language = Utils.getLanguageString(getUserSelectedLanguage(), mContext);
		}
		return language;
	}

	public void setUserSelectedLanguageText(String userLanguage) {
		Editor editor = mPreferences.edit();
		editor.putString(USER_SELECTED_LANGUAGE_TEXT, userLanguage);
		editor.commit();
        if(!isRealUser()){
            setSilentUserSelectedLanguageText(userLanguage);
        }
	}

    public String getSilentUserSelectedLanguageText() {
//        if (mPreferences != null)
//            return mPreferences.getString(SILENT_USER_SELECTED_LANGUAGE_TEXT, "");
        return "English";
    }

    public void setSilentUserSelectedLanguageText(String userLanguage) {
        Editor editor = mPreferences.edit();
        editor.putString(SILENT_USER_SELECTED_LANGUAGE_TEXT, userLanguage);
        editor.commit();
    }

	public String getStringUrls() {
		return mPreferences.getString(STRING_KEYS, "");
	}

	public void setStringUrls(String string_files) {
		Editor editor = mPreferences.edit();
		editor.putString(STRING_KEYS, string_files);
		editor.commit();
	}

	public String getStringValues() {
		return mPreferences.getString(STRING_VALUES, "");
	}

	public void setStringValues(String string_files) {
		Editor editor = mPreferences.edit();
		editor.putString(STRING_VALUES, string_files);
		editor.commit();
	}

	private static final String IS_FIRST_TIME_APP_LAUNCH = "is_first_app_launch";
	public static final String PARAMS_SIZE = "size";

	public void setIsFirstTimeAppLaunch(boolean isFirstTimeLaunch) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_FIRST_TIME_APP_LAUNCH, isFirstTimeLaunch);
		editor.commit();
	}

	public boolean isFirstTimeAppLaunch() {
		return mPreferences.getBoolean(IS_FIRST_TIME_APP_LAUNCH, true);
	}

	public void setApplicationVersion(String appVersion) {
		Editor editor = mPreferences.edit();
		editor.putString(APPLICATION_VERSION, appVersion);
		editor.commit();
	}

	public String getApplicationVersion() {
		return mPreferences.getString(APPLICATION_VERSION, "");
	}

	public void setMusicPreferencesResponse(String response) {
		Editor editor = mPreferences.edit();
		editor.putString(MUSIC_PREFERENCES_RESPONSE, response);
		editor.commit();
	}

	public String getMusicPreferencesResponse() {
		return mPreferences.getString(MUSIC_PREFERENCES_RESPONSE, "");
	}

	// public void setSearchPopularResponse(String response) {
	// Editor editor = mPreferences.edit();
	// editor.putString(SEARCH_POPULAR_KEYWORD, response);
	// editor.commit();
	// }
	// public String getSearchPopularResponse() {
	// return mPreferences.getString(SEARCH_POPULAR_KEYWORD, "");
	// }

	public void setVideoPreferencesResponse(String response) {
		Editor editor = mPreferences.edit();
		editor.putString(VIDEO_PREFERENCES_RESPONSE, response);
		editor.commit();
	}

	public String getVideoPreferencesResponse() {
		return mPreferences.getString(VIDEO_PREFERENCES_RESPONSE, "");
	}

	public void setIsEnabledSongCatcherGuidePage(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_SONG_CATCHER_GUIDE_PAGE, value);
		editor.commit();
	}

	public boolean isEnabledSongCatcherGuidePage() {
		return mPreferences
				.getBoolean(IS_ENABLED_SONG_CATCHER_GUIDE_PAGE, true);
	}

	public void setIsEnabledLanguageGuidePage(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(IS_ENABLED_LANGUAGE_GUIDE_PAGE, value);
		editor.commit();
	}

	public boolean isEnabledLanguageGuidePage() {
		return mPreferences.getBoolean(IS_ENABLED_LANGUAGE_GUIDE_PAGE, true);
	}

	public void increaseAppOpenCount() {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_OPEN_COUNT, getAppOpenCount() + 1);
		editor.commit();
	}

	public int getAppOpenCount() {
		return mPreferences.getInt(APP_OPEN_COUNT, 0);
	}

	public void checkForProUser(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APP_CHECK_PRO_USER, value);
		editor.commit();
	}

	public boolean isCheckForProUser() {
		return mPreferences.getBoolean(APP_CHECK_PRO_USER, false);
	}

	public void setSplashAdCountLimit(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_SPLASH_SESSION_LIMIT, value);
		editor.commit();
	}

	public int getSplashAdCountLimit() {
		return mPreferences.getInt(APP_CONFIG_SPLASH_SESSION_LIMIT, 3);
	}

	public void setAudioAdDurationCheck(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CHECK_FOR_AUDIO_AD_DURATION, value);
		editor.commit();
	}

	public int getAudioAdDurationCheck() {
		return mPreferences.getInt(APP_CHECK_FOR_AUDIO_AD_DURATION, 30);
	}

	public void setAppConfigSplashLaunch(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APP_CONFIG_SPLASH_LAUNCH, value);
		editor.commit();
	}

	public boolean getAppConfigSplashLaunch() {
		return mPreferences.getBoolean(APP_CONFIG_SPLASH_LAUNCH, true);
	}

	public void setAppConfigSplashReLaunch(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APP_CONFIG_SPLASH_RELAUNCH, value);
		editor.commit();
	}

	public boolean getAppConfigSplashReLaunch() {
		return mPreferences.getBoolean(APP_CONFIG_SPLASH_RELAUNCH, true);
	}

	public void setAppConfigSplashUnlock(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APP_CONFIG_SPLASH_UNLOCK, value);
		editor.commit();
	}

	public boolean getAppConfigSplashUnlock() {
		return mPreferences.getBoolean(APP_CONFIG_SPLASH_UNLOCK, true);
	}

	public void setAppConfigSplashMaximize(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APP_CONFIG_SPLASH_MAXIMIZE, value);
		editor.commit();
	}

	public boolean getAppConfigSplashMaximize() {
		return mPreferences.getBoolean(APP_CONFIG_SPLASH_MAXIMIZE, true);
	}

	public void setAppConfigSplashAdRefreshLimit(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_SPLASH_REFRESH_LIMIT, value);
		editor.commit();
	}

	public int getAppConfigSplashAdRefreshLimit() {
		return mPreferences.getInt(APP_CONFIG_SPLASH_REFRESH_LIMIT, 300);
		// return 5;
	}

	public void setAppConfigSplashAdAutoSkip(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_SPLASH_AUTO_SKIP, value);
		editor.commit();
	}

	public int getAppConfigSplashAdAutoSkip() {
		return mPreferences.getInt(APP_CONFIG_SPLASH_AUTO_SKIP, 5);
	}

	public void setAppConfigPlayerOverlayRefresh(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_PLAYER_OVERLAY_REFRESH, value);
		editor.commit();
	}

	public int getAppConfigPlayerOverlayRefresh() {
		return mPreferences.getInt(APP_CONFIG_PLAYER_OVERLAY_REFRESH, 30);
	}

	public void setAppConfigPlayerOverlayStart(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_PLAYER_OVERLAY_START, value);
		editor.commit();
	}

	public int getAppConfigPlayerOverlayStart() {
		return mPreferences.getInt(APP_CONFIG_PLAYER_OVERLAY_START, 30);
	}

	public void setAppConfigPlayerOverlayFlipBackDuration(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_PLAYER_OVERLAY_FLIP_BACK_DURATION, value);
		editor.commit();
	}

	public int getAppConfigPlayerOverlayFlipBackDuration() {
		return mPreferences.getInt(
				APP_CONFIG_PLAYER_OVERLAY_FLIP_BACK_DURATION, 10);
	}

	public void setAppConfigAudioAdFrequency(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_AUDIO_AD_FREQUENCY, value);
		editor.commit();
	}

	public int getAppConfigAudioAdFrequency() {
		return mPreferences.getInt(APP_CONFIG_AUDIO_AD_FREQUENCY, 30);
	}

	public void setAppConfigAudioAdRule(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(APP_CONFIG_AUDIO_ADD_RULE, value);
		editor.commit();
	}

	public String getAppConfigAudioAdRule() {
		return mPreferences.getString(APP_CONFIG_AUDIO_ADD_RULE, "1,3");
		// return "1,3";
	}

	public void setAppConfigAudioAdSessionLimit(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_AUDIO_ADD_SESSION_LIMIT, value);
		editor.commit();
	}

	public int getAppConfigAudioAdSessionLimit() {
		return mPreferences.getInt(APP_CONFIG_AUDIO_ADD_SESSION_LIMIT, 3);
	}

	public void setAppConfigVideoAdPlay(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_VIDEO_ADD_PLAY, value);
		editor.commit();
	}

	public int getAppConfigVideoAdPlay() {
		return mPreferences.getInt(APP_CONFIG_VIDEO_ADD_PLAY, 3);
	}

	public void setAppConfigVideoAdSessionLimit(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_VIDEO_ADD_SESSION_LIMIT, value);
		editor.commit();
	}

	public int getAppConfigVideoAdSessionLimit() {
		return mPreferences.getInt(APP_CONFIG_VIDEO_ADD_SESSION_LIMIT, 3);
	}

	public int getVideoPlayBackCounter() {
		int value = mPreferences
				.getInt(APP_CONFIG_VIDEO_AD_PLAYBACK_COUNTER, 0);
		return value;
	}

	public void increaseVideoPlayBackCounter() {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_VIDEO_AD_PLAYBACK_COUNTER,
				getVideoPlayBackCounter() + 1);
		editor.commit();
	}

	public void resetVideoPlayBackCounter() {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_VIDEO_AD_PLAYBACK_COUNTER, 0);
		editor.commit();
	}

	public void setAppConfigRefreshAds(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(APP_CONFIG_REFRESH_ADS, value);
		editor.commit();
	}

	public int getAppConfigRefreshAds() {
		return mPreferences.getInt(APP_CONFIG_REFRESH_ADS, 1800);
	}

	public void setTrialCheckedForUserId(String userId) {
		String prvIds = mPreferences.getString(APP_TRIAL_CHECKED_FOR_USER_ID,
				"");
		Editor editor = mPreferences.edit();
		if (prvIds.length() == 0)
			editor.putString(APP_TRIAL_CHECKED_FOR_USER_ID, userId);
		else
			editor.putString(APP_TRIAL_CHECKED_FOR_USER_ID, prvIds + ","
					+ userId);
		editor.commit();
	}

	public boolean isTrialCheckedForUserId(String userId) {
		return mPreferences.getString(APP_TRIAL_CHECKED_FOR_USER_ID, "")
				.contains(userId);
	}

	public boolean isuserLoggedIn() {
		if (!TextUtils.isEmpty(getSessionID()) && isRealUser()) {
			return true;

		}
		return false;
	}

	public void setLeftMenuHintChecked(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APP_CHECK_LEFT_MENU_HINT, value);
		editor.commit();
	}

	public boolean isLeftMenuHintChecked() {
		return mPreferences.getBoolean(APP_CHECK_LEFT_MENU_HINT, true);
	}

	public void setPlayerQueueHintChecked(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APP_CHECK_PLAYER_QUEUE_HINT, value);
		editor.commit();
	}

	public boolean isPlayerQueueHintChecked() {
		return mPreferences.getBoolean(APP_CHECK_PLAYER_QUEUE_HINT, false);
	}

	public void setPlayerBarHintChecked(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APP_CHECK_PLAYER_BAR_HINT, value);
		editor.commit();
	}

	public boolean isPlayerBarHintChecked() {
		return mPreferences.getBoolean(APP_CHECK_PLAYER_BAR_HINT, true);
	}

	public void setOfflineMusicHintChecked(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APP_CHECK_OFFLINE_MUSIC_HINT, value);
		editor.commit();
	}

	public boolean isOfflineMusicHintChecked() {
		return mPreferences.getBoolean(APP_CHECK_OFFLINE_MUSIC_HINT, false);
	}

	public void setHomeLandingHelpDisplayed(boolean isHelpDisplayed) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(HOME_SCREEN_LANDING_HELP, isHelpDisplayed);
		editor.commit();
	}

	public boolean isHomeLandingHelpDisplayed() {
		return mPreferences.getBoolean(HOME_SCREEN_LANDING_HELP, false);
	}

	public void setFullPlayerDrawerHelp(boolean isHelpDisplayed) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(FULL_PLAYER_DRAWER_HELP, isHelpDisplayed);
		editor.commit();
	}

	public boolean isFullPlayerDrawerHelp() {
		return mPreferences.getBoolean(FULL_PLAYER_DRAWER_HELP, false);
	}

	public void setFullPlayerDrawerHelp2(boolean isHelpDisplayed) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(FULL_PLAYER_DRAWER_HELP2, isHelpDisplayed);
		editor.commit();
	}

	public boolean isFullPlayerDrawerHelp2() {
		return mPreferences.getBoolean(FULL_PLAYER_DRAWER_HELP2, false);
	}

	public void setFullPlayerDragHelp(boolean isHelpDisplayed) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(FULL_PLAYER_DRAG_HELP, isHelpDisplayed);
		editor.commit();
	}

	public boolean isFullPlayerDragHelp() {
		return mPreferences.getBoolean(FULL_PLAYER_DRAG_HELP, false);
	}

	public void setFilterSongsOption(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(FILTER_SONGS, value);
		editor.commit();
	}

	public boolean getFilterSongsOption() {
		return mPreferences.getBoolean(FILTER_SONGS, false);
	}

	public void setFilterAlbumsOption(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(FILTER_ALBUMS, value);
		editor.commit();
	}

	public boolean getFilterAlbumsOption() {
		return mPreferences.getBoolean(FILTER_ALBUMS, true);
	}

	public void setFilterPlaylistsOption(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(FILTER_PLAYLISTS, value);
		editor.commit();
	}

	public boolean getFilterPlaylistsOption() {
		return mPreferences.getBoolean(FILTER_PLAYLISTS, true);
	}

	public void setFilterLiveRadioOption(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(FILTER_TOP_RADIO, value);
		editor.commit();
	}

	public boolean getFilterLiveRadioOption() {
		return mPreferences.getBoolean(FILTER_TOP_RADIO, true);
	}

	public void setFilterCelebRadioOption(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(FILTER_CELEB_RADIO, value);
		editor.commit();
	}

	public boolean getFilterCelebRadioOption() {
		return mPreferences.getBoolean(FILTER_CELEB_RADIO, true);
	}

	public void setTriviaShow(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(MY_STREAM_SETTINGS_TRIVIA_SHOW, value);
		editor.commit();
	}

	public boolean needToShowTrivia() {
		return mPreferences.getBoolean(MY_STREAM_SETTINGS_TRIVIA_SHOW, true);
	}

	public void setLyricsShow(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(MY_STREAM_SETTINGS_LYRICS_SHOW, value);
		editor.commit();
	}

	public boolean needToShowLyrics() {
		return mPreferences.getBoolean(MY_STREAM_SETTINGS_LYRICS_SHOW, true);
	}

	public void setTriviaAlertDisply(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(TRIVIA_ALERT_DISPLAY, value);
		editor.commit();
	}

	public boolean isTriviaAlertDisplay() {
		return mPreferences.getBoolean(TRIVIA_ALERT_DISPLAY, false);
	}

	// public void setPlayerBarHeight(int value) {
	// Editor editor = mPreferences.edit();
	// editor.putInt(PLAYER_BAR_HEIGHT, value);
	// editor.commit();
	// }
	//
	// public int getPlayerBarHeight() {
	// return mPreferences.getInt(PLAYER_BAR_HEIGHT, 0);
	// }

	private static final String MUSIC_PREFERENCES_RESPONSE_LOADED = "music_preferences_response_loaded";
	private static final String VIDEO_PREFERENCES_RESPONSE_LOADED = "video_preferences_response_loaded";
	private static final String NEXT_VIDEO_AUTO_PLAY = "next_video_auto_play";

	public void setIsMusicPreferencesResponseLoaded(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(MUSIC_PREFERENCES_RESPONSE_LOADED, value);
		editor.commit();
	}

	public boolean isMusicPreferencesResponseLoaded() {
		return mPreferences
				.getBoolean(MUSIC_PREFERENCES_RESPONSE_LOADED, false);
	}

	public void setIsVideoPreferencesResponseLoaded(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(VIDEO_PREFERENCES_RESPONSE_LOADED, value);
		editor.commit();
	}

	public boolean isVideoPreferencesResponseLoaded() {
		return mPreferences
				.getBoolean(VIDEO_PREFERENCES_RESPONSE_LOADED, false);
	}

	public void setNextVideoAutoPlay(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(NEXT_VIDEO_AUTO_PLAY, value);
		editor.commit();
	}

	public boolean isNextVideoAutoPlay() {
		return mPreferences.getBoolean(NEXT_VIDEO_AUTO_PLAY, true);
	}

	public void setNeedToShowSaveOfflineHelp(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(NEED_TO_SHOW_SAVE_OFFLINE_HELP, value);
		editor.commit();
	}

	public boolean idNeedToShowSaveOfflineHelp() {
		try {
			boolean mode = mPreferences.getBoolean(
					NEED_TO_SHOW_SAVE_OFFLINE_HELP, true);
			return mode;
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	public void setSaveOfflineAutoMode(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(SAVE_OFFLINE_AUTO_MODE, value);
		editor.commit();
	}

	public boolean getSaveOfflineAutoMode() {
		try {
			boolean mode = mPreferences.getBoolean(SAVE_OFFLINE_AUTO_MODE,
					false);
			return mode;
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	public void setSaveOfflineAutoModeRemember(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(SAVE_OFFLINE_AUTO_MODE_REMEMBER, value);
		editor.commit();
	}

	public boolean getSaveOfflineAutoModeRemember() {
		try {
			boolean mode = mPreferences.getBoolean(
					SAVE_OFFLINE_AUTO_MODE_REMEMBER, false);
			return mode;
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	public void increaseTriviaNotNowCount() {
		Editor editor = mPreferences.edit();
		editor.putInt(TRIVIA_SHOW_NOT_NOW_COUNTER, getTriviaNotNowCount() + 1);
		editor.commit();
	}

	public void resetTriviaNotNowCount() {
		Editor editor = mPreferences.edit();
		editor.putInt(TRIVIA_SHOW_NOT_NOW_COUNTER, 0);
		editor.commit();
	}

	public int getTriviaNotNowCount() {
		int value = mPreferences.getInt(TRIVIA_SHOW_NOT_NOW_COUNTER, 0);
		return value;
	}

	public void increaseLyricsNotNowCount() {
		Editor editor = mPreferences.edit();
		editor.putInt(LYRICS_SHOW_NOT_NOW_COUNTER, getLyricsNotNowCount() + 1);
		editor.commit();
	}

	public void resetLyricsNotNowCount() {
		Editor editor = mPreferences.edit();
		editor.putInt(LYRICS_SHOW_NOT_NOW_COUNTER, 0);
		editor.commit();
	}

	public int getLyricsNotNowCount() {
		int value = mPreferences.getInt(LYRICS_SHOW_NOT_NOW_COUNTER, 0);
		return value;
	}

 	public void clearAllTimestamps() {
        setMusicLatestTimeStamp("0");
        setMusicPopularTimeStamp("0");
        setVideoLatestTimeStamp("0");
        setLiveRadioTimeStamp("0");
        setOnDemandTimeStamp("0");
        setSearchPopularTimeStamp("0");
        setCategoriesGenerTimeStamp("0");
        setPreferenceGetCategoryTimeStamp("0");
        setAllLanguagesTimeStamp("0");
        setUserLanguageTimeStamp("0");
        setLeftMenuTimeStamp("0");
        setFeedbackTimeStamp("0");
    }
	public void setNeedTrendDialogShow(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(TREND_DIALOG_SHOW_FOR_DISCOVERY_OF_THE_DAY, value);
		editor.commit();
	}

	public boolean needTrendDialogShow() {
		try {
			boolean mode = mPreferences.getBoolean(
					TREND_DIALOG_SHOW_FOR_DISCOVERY_OF_THE_DAY, true);
			return mode;
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return true;
	}

	public void setNeedTrendDialogShowForTheSession(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(TREND_DIALOG_DONT_SHOW_FOR_DISCOVERY_OF_THE_DAY,
				value);
		editor.commit();
	}

	public boolean needTrendDialogShowForTheSession() {
		try {
			boolean mode = mPreferences.getBoolean(
					TREND_DIALOG_DONT_SHOW_FOR_DISCOVERY_OF_THE_DAY, true);
			return mode;
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return true;
	}

	public String getRedeemUrl() {
		return mPreferences.getString(REDEEM_URL, null);
	}

	public void setRedeemUrl(String redeemUrl) {
		Editor editor = mPreferences.edit();
		editor.putString(REDEEM_URL, redeemUrl);
		editor.commit();
	}

	public int getTweetLimit() {
		return mPreferences.getInt(TWEET_LIMIT, 118);
	}

	public void setTweetLimit(int value) {
		Editor editor = mPreferences.edit();
		editor.putInt(TWEET_LIMIT, value);
		editor.commit();
	}

	public String getSubscriptionAffCode() {
		return mPreferences.getString(SUBSCRIPTION_AFF_CODE, null);
	}

	public void setSubscriptionAffCode(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(SUBSCRIPTION_AFF_CODE, value);
		editor.commit();
	}

	public void setApsalarAttributionStatus(boolean value) {
		Editor editor = mPreferences.edit();
		editor.putBoolean(APSALAR_ATTRIBUTION_DONE, value);
		editor.commit();
	}

	public boolean getApsalarAttributionStatus() {
		try {
			return mPreferences.getBoolean(APSALAR_ATTRIBUTION_DONE, false);
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return false;
	}

	public void setDefaultUserAgent(String value) {
		Editor editor = mPreferences.edit();
		editor.putString(DEFAULT_USER_AGENT, value);
		editor.commit();
	}

	public String getDefaultUserAgent() {
		try {
			return mPreferences.getString(DEFAULT_USER_AGENT, "");
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		return "";
	}
}
