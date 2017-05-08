package com.hungama.myplay.activity.operations.hungama;

import com.hungama.myplay.activity.communication.CommunicationOperation;
import com.hungama.myplay.activity.util.Logger;

/**
 * A Communication manager that performs in front of Hungama servers.
 */
public abstract class HungamaOperation extends CommunicationOperation {

	protected static final String COMMA = ",";
	public static final String AMPERSAND = "&";
	public static final String EQUALS = "=";
	protected static final String PARAMS = "?";

	/*
	 * Request parameters definition.
	 */
	protected static final String PARAMS_AUTH_KEY = "auth_key";
	public static final String PARAMS_USER_ID = "user_id";
	protected static final String PARAMS_DEVICE = "device";
	protected static final String PARAMS_SIZE = "size";
	protected static final String PARAMS_TRIAL = "trial";
	protected static final String PARAMS_WIDTH = "width";
	protected static final String PARAMS_HEIGHT = "height";
	protected static final String PARAMS_SUBSCRIPTION_ID = "subscription_id";
	public static final String PARAMS_DOWNLOAD_HARDWARE_ID = "hardware_id";
	public static final String PARAMS_DOWNLOAD_AFFILIATE_ID = "affiliate_id";
	public static final String PARAMS_TRANSACTION_SESSION = "transaction_session";
	public static final String PARAMS_IDENTITY = "identity";
	public static final String PARAMS_PRODUCT = "product";
	public static final String PARAMS_PLATFORM = "platform";
	public static final String PARAMS_AFF_CODE = "aff_code";

	public static final String PARAMS_CONTENT_ID = "content_id";
	public static final String PARAMS_ALBUM_ID = "album_id";
	public static final String PARAMS_PLAN_ID = "plan_id";
	protected static final String PARAMS_MSISDN = "msisdn";
	protected static final String PARAMS_PASSWORD = "password";
	protected static final String PARAMS_MEDIA_TYPE = "type";
	protected static final String PARAMS_REFERRAL_ID = "referral_id";
	protected static final String PARAMS_NETWORK_SPEED = "network_speed";
	protected static final String PARAMS_NETWORK_TYPE = "network_type";
	protected static final String PARAMS_PROTOCOL = "protocol";
	protected static final String PARAMS_OFFLINE_CACHING = "offline_caching";

	protected static final String PARAMS_CONTENT_FORMAT = "content_format";
	protected static final String PARAMS_START = "start";
	protected static final String PARAMS_LENGTH = "length";
	protected static final String PARAMS_CLIENT = "client";
	protected static final String PARAMS_CLIENT_VERSION = "client_version";
	protected static final String PARAMS_CODE = "code";
	protected static final String PARAMS_PURCHASE_TOKEN = "purchase_token";
	protected static final String PARAMS_GOOGLE_EMAIL_ID = "google_email_id";
	protected static final String PARAMS_COUPON_CODE = "coupon_code";
	protected static final String PARAMS_MOBILE = "mobile";
	protected static final String PARAMS_OTP_CODE = "otp_code";
	public static final String PARAMS_PLAYLIST_KEY = "playlist_key";
	protected static final String PARAMS_CATEGORY_NAME = "category_name";
	protected static final String PARAMS_GENER_NAME = "genre_name";
	protected static final String PARAMS_LANGUAGE_NAME = "language_name";

	protected static final String PARAMS_TYPE = "type";

	public static final String VALUE_DEVICE = "android";
	public static final String VALUE_PRODUCT = "hungamamusic";

	/*
	 * Urls segments definition.
	 */
	protected static final String URL_SEGMENT_CONTENT = "content/";
	protected static final String URL_SEGMENT_MUSIC = "music/";
	protected static final String URL_SEGMENT_VIDEO = "video/";
	protected static final String URL_SEGMENT_DETAILS_ALBUM = "album_details";
	protected static final String URL_SEGMENT_DETAILS_PLAYLIST = "playlist_details";
	protected static final String URL_SEGMENT_DETAILS_SONG = "song_details";
	protected static final String URL_SEGMENT_MEDIA_DETAILS = "metadetails?content_id=";
	protected static final String URL_SEGMENT_CATEGORIES = "categories";
	protected static final String URL_SEGMENT_DETAILS_VIDEO = "video_details";
	protected static final String URL_SEGMENT_RELATED_VIDEO = "related";
	protected static final String URL_SEGMENT_PLAYLIST_ID = "playlistid";
	protected static final String URL_SEGMENT_PROMO_UNIT = "settings/promo?";
	// search.
	protected static final String URL_SEGMENT_SEARCH_POPULAR_KEYWORD = "content/search/popular_keyword?";
	protected static final String URL_SEGMENT_SEARCH_AUTO_SUGGEST = "content/search/auto_suggest?";
	protected static final String URL_SEGMENT_SEARCH = "content/search/";

	// left menu
	protected static final String URL_SEGMENT_LEFT_MENU = "settings/left_menu?";

	// Discovery
	protected static final String URL_SEGMENT_HAHS_TAG_LIST = "content/hash/hash_list?";
	protected static final String URL_SEGMENT_HAHS_TAG_SEARCH = "content/hash/hash_search?";

	// video streaming.
	protected static final String URL_SEGMENT_VIDEO_STREAMING = "streaming/video?";
	protected static final String URL_SEGMENT_VIDEO_STREAMING_ADP = "streaming/adpvideo?";

	// discovery.
	protected static final String URL_SEGMENT_DISCOVER_OPTIONS = "user/discover/option?";
	protected static final String URL_SEGMENT_DISCOVER_SEARCH = "user/discover/search?";
	protected static final String URL_SEGMENT_DISCOVER_SAVE = "user/discover/save?";
	// protected static final String URL_SEGMENT_DISCOVER_RETREIVE =
	// "user/discover/retrieve?";

	// Subscription
	protected static final String URL_SEGMENT_SUBSCRIPTION_PLAN = "subscription_plans_new.php";
	protected static final String URL_SEGMENT_SUBSCRIPTION_CHARGE = "/webservice/notify_billing.php";
	protected static final String URL_SEGMENT_SUBSCRIPTION_UNSUBSCRIBE = "unsubscribe.php";
	protected static final String URL_SEGMENT_SUBSCRIPTION_ORDER_CHECK = "subscription_order_status.php";

	protected static final String URL_SEGMENT_SUBSCRIPTION_CHECK_SUBSCRIPTION = "/webservice/subscription_status.php";
	protected static final String URL_SEGMENT_VERSION_CHECK = "version/check.php";

	// Mobile verify
	protected static final String URL_SEGMENT_MOBILE_VERIFY = "mobile_verify.php";
	protected static final String URL_SEGMENT_MOBILE_PASSWORD_VERIFY = "mobile_password_verify.php";
	protected static final String URL_SEGMENT_RESEND_PASSWORD = "resend_sms.php";
	protected static final String URL_SEGMENT_COUNTRY_CHECK = "check_msisdn.php";

	// Download
	protected static final String URL_SEGMENT_CREDIT_BALANCE = "credit_balance.php";
	protected static final String URL_SEGMENT_SILENT_USER_DOWNLOAD = "silent_user_download.php";
	protected static final String URL_SEGMENT_BUY_PLANS = "download_plans.php";
	protected static final String URL_SEGMENT_DOWNLOAD_ORDER_STATUS = "download_order_status.php";
	protected static final String URL_SEGMENT_BUY_CHARGE = "notify_billing.php";
	protected static final String URL_SEGMENT_CONTENT_DELIVERY = "/webservice/content_delivery.php";
	protected static final String URL_SEGMENT_DOWNLOAD_APPLICATION_IMAGES = "settings/appimages.php";
	public static final String URL_SEGMENT_CONSENT_BILLING = "consent_billing.php"
			+ PARAMS;

	// player.
	protected static final String URL_SEGMENT_SIMILAR = "similar";
	protected static final String URL_SEGMENT_LYRICS = "lyrics";
	protected static final String URL_SEGMENT_TRIVIA = "trivia";

	protected static final String URL_SEGMENT_RADIO_LIVE_STATIONS = "content/radio/live_radiolist?";
	protected static final String URL_SEGMENT_RADIO_TOP_ARTISTS = "content/radio/ondemand_radiolist?";
	// protected static final String URL_SEGMENT_RADIO_TOP_ARTIST_SONGS =
	// "content/radio/top_artist_songs?";
	protected static final String URL_SEGMENT_RADIO_TOP_ARTIST_SONGS = "content/radio/ondemand_radiodetails?";
	protected static final String URL_SEGMENT_RADIO_TOP_CELEB_SONGS = "content/radio/celeb_radiodetails?artist_id=";
	protected static final String URL_SEGMENT_LIVE_RADIO_DETAIL = "content/radio/live_radiodetails?";

	// my preferences.
	// protected static final String URL_SEGMENT_PREFERENCES_SAVE =
	// "user/preferences/save?";
	// protected static final String URL_SEGMENT_PREFERENCES_RETREIVE =
	// "user/preferences/retrieve?";
	protected static final String URL_SEGMENT_PREFERENCES_SAVE = "user/preference/save_category?";
	protected static final String URL_SEGMENT_PREFERENCES_RETREIVE = "user/preference/get_category?";

	protected static final String URL_SEGMENT_PREFERENCES_VIDEO_SAVE = "user/preferences_video/save?";
	protected static final String URL_SEGMENT_PREFERENCES_VIDEO_RETREIVE = "user/preferences_video/retrieve?";

	// SOCIAL.

	protected static final String URL_SEGMENT_SOCIAL_COMMENT_POST = "user/comment/post_comment?";
	protected static final String URL_SEGMENT_SOCIAL_COMMENT_GET = "user/comment/get_comments?";

	protected static final String URL_SEGMENT_SOCIAL_MY_STREAM = "user/my_stream.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE = "user/my_profile.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_BADGES = "user/my_badges.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_LEADERBOARD = "user/leaderboard.php?";

	// protected static final String URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_ALBUMES
	// = "fav_albums.php?";
	// protected static final String URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_SONGS =
	// "fav_songs.php?";
	// protected static final String URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_VIDEOS
	// = "fav_videos.php?";
	// protected static final String
	// URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_PLAYLISTS = "fav_playlists.php?";
	// protected static final String URL_SEGMENT_SOCIAL_PROFILE_FAVORITE_ARTISTS
	// = "fav_artists.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_FAVORITE = "user/my_favorites.php?";

	// http://apistaging.hungama.com/webservice/hungama/user/share/post_share

	// protected static final String URL_SEGMENT_SOCIAL_PROFILE_MY_COLLECTION =
	// "my_collection.php?";
	protected static final String URL_SEGMENT_SOCIAL_PROFILE_MY_COLLECTION = "user/my_downloads?";

	protected static final String URL_SEGMENT_SOCIAL_SHARE = "user/share/post_share?";
	// protected static final String URL_SEGMENT_SOCIAL_BADGE_ALERT =
	// "badge_alert.php?";
	protected static final String URL_SEGMENT_SOCIAL_BADGE_ALERT = "user/badge_alert?";
	// http://apistaging.hungama.com/webservice/hungama/user/share_url

	protected static final String URL_SEGMENT_SOCIAL_GET_URL = "user/share_url?";

	protected static final String URL_SEGMENT_SOCIAL_ADD_TO_FAVORITE = "user/favorite/add_favorite?";
	protected static final String URL_SEGMENT_SOCIAL_REMOVE_FROM_FAVORITE = "user/favorite/remove_favorite?";
	// protected static final String URL_SEGMENT_GCM_REG_ID =
	// "user/device_register?";
	protected static final String URL_SEGMENT_SHARE_SETTINGS = "user/settings/get_share_settings?";
	protected static final String URL_SEGMENT_SHARE_SETTINGS_UPDATE = "user/settings/save_share_settings?";
	// protected static final String URL_SEGMENT_MY_STREAM_SETTINGS =
	// "user/stream_settings/retrieve_stream?";
	protected static final String URL_SEGMENT_MY_STREAM_SETTINGS = "user/settings/get_mystream_settings?";

	protected static final String URL_SEGMENT_MY_STREAM_SETTINGS_UPDATE = "user/settings/save_mystream_settings?";

	// protected static final String URL_SEGMENT_LANGUAGE_SETTINGS_LIST =
	// "user/language/languagelist?";
	// protected static final String URL_SEGMENT_LANGUAGE_SETTINGS_POST =
	// "user/language/save?";
	// protected static final String URL_SEGMENT_LANGUAGE_SETTINGS_GET =
	// "user/language/retrieve?";
	protected static final String URL_SEGMENT_LANGUAGE_SETTINGS_LIST = "content/music/languages?";
	protected static final String URL_SEGMENT_LANGUAGE_SETTINGS_POST = "user/preference/save_language?";
	protected static final String URL_SEGMENT_LANGUAGE_SETTINGS_GET = "user/preference/get_language?";

	// FEEDBACK.
	protected static final String URL_SEGMENT_FEEDBACK_SUBJECTS = "user/feedback/subject?";
	protected static final String URL_SEGMENT_FEEDBACK_SAVE = "user/feedback/save?";

	/*
	 * General parsing keys.
	 */
	protected static final String KEY_CATALOG = "catalog";
	public static final String KEY_CONTENT = "content";
	// protected static final String KEY_Toast= "toast";
	protected static final String KEY_ID = "id";
	protected static final String KEY_NAME = "name";
	protected static final String KEY_ATTRIBUTES = "@attributes";
	protected static final String KEY_USER_FAV = "user_fav";

	protected static final String KEY_LAST_MODIFIED = "last_modified";
	/*
	 * Error parsing keys.
	 */
	public static final String KEY_RESPONSE = "response";
	protected static final String KEY_CODE = "code";
	protected static final String KEY_MESSAGE = "message";
	protected static final String KEY_DISPLAY = "display";

	protected static final String URL_SEGMENT_MULTI_SONG_DETAIL_GET = "content/music/multi_song_details?";
	// http://apistaging.hungama.com/myplay2/v2/content/music/nqhistory/17028774?auth_key=4bbaa8370603ed0556eda28ab7c4573a&device=android&size=xdpi
	protected static final String URL_SEGMENT_MULTI_SONG_HISTORY_GET = "content/music/nqhistory?";

	// Redeem Coupon
	protected static final String URL_SEGMENT_REDEEM_VALIDATE_COUPON = "validate_coupon.php";
	protected static final String URL_SEGMENT_REDEEM_SEND_MOBILE_OTP = "send_mobile_otp.php";
	protected static final String URL_SEGMENT_REDEEM_VALIDATE_MOBILE_OTP = "validate_mobile_otp.php";

}
