package com.hungama.myplay.activity.util;

import android.content.Context;

import com.apsalar.sdk.Apsalar;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;

public class ApsalarEvent {

	public static final boolean ENABLED = true;

	/**
	 * App_launch
	 */
	public static final String APP_LAUNCH_EVENT = "APL";

	/**
	 * Signup (complete) : After completion of Signup process
	 */
	public static final String SIGNUP_COMPLITION = "SUC";

	/**
	 *Song Played Online : At the time of reporting the PlayEvent
	 */
	public static final String SONG_PLAYED_ONLINE = "SPO";

	/**
	 * Song Played Offline : At the time of reporting the PlayEvent of Offline Songs
	 */
	public static final String SONG_PLAYED_OFFLINE = "SPO";

	/**
	 * Video Played Online : At the time of reporting the PlayEvent
	 */
	public static final String VIDEO_PLAYED_ONLINE = "VPO";

	/**
	 * Video Played Offline : At the time of reporting the PlayEvent of Offline Videos
	 */
	public static final String VIDEO_PLAYED_OFFLINE = "VPO";

	/**
	 *  Radio Played : At the time of reporting the PlayEvent
	 */
	public static final String RADIO_PLAYED = "RPO";

	/**
	 *  Free trial taken
	 */
//	public static final String FREE_TRIAL_TAKEN = "FTT";

	/**
	 *  Transacted : Every single time user is paying to either purchase Subscription or Single Content
	 */
	public static final String TRANSACTED = "TPC";

	/**
	 *  Song cached : After caching the audio track
	 */
	public static final String SONG_CACHED = "SCC";

	/**
	 *  Video Cached : After caching the video track
	 */
	public static final String VIDEO_CACHED = "VCC";

	/**
	 *  Discovered Music (discovery feature) : On receiving the result set for the selected Discovery
	 */
//	public static final String DISCOVERED_MUSIC = "DMR";

	/**
	 *  Preference changed : On change of the Category
	 */
//	public static final String PREFERENCE_CHANGED = "PCC";

	/**
	 *  Redeemed Coins : On redeeming coins to  buy Song or Video
	 */
//	public static final String REDEEMED_COINS = "RCC";

	/**
	 *  Loading the Home Page after launching the app
	 */
//	public static final String LOADING_THE_HOME = "HPL";

	/**
	 *  Discovery Play : On click of PLAY button on Discovery Page
	 */
//	public static final String DISCOVERY_PLAY = "DPP";

	/**
	 *  Discovery Play Hash : On click of PLAY button on Discovery Hash
	 */
//	public static final String DISCOVERY_PLAY_HASH = "DPH";

	/**
	 *  Playlist Created / Saved
	 */
	public static final String PLAYLIST_CREATED = "PLC";

	/**
	 *  Login Completed (Social or Hungama)
	 */
//	public static final String LOGIN_COMPLETED = "LGC";

	/**
	 *  Chromecast connected (once user is connected to a chromecast)
	 */
//	public static final String CHROMECAST_CONNECTED = "CHC";

	/**
	 *  Airplay Connected (once user is connected to a Airplay Device)
	 */
//	public static final String AIRPLAY_CONNECTED = "APC";

	/**
	 *  Search performed
	 */
	public static final String SEARCH_PERFORMED = "SRP";
	public static final String LOADING_THE_HOME = "HPL";


	/**
	 *  Ecoupon_used
	 */
//	public static final String ECOUPON_USED = "ECU";

	/**
	 *  Player_queue_accessed
	 */
//	public static final String PLAYER_QUEUE_ACCESSED = "PQA";

	/**
	 *  Media Played (Songs, Radio & Video)
	 */
	public static final String MEDIA_PLAYED = "MPO";


//	public static final String SONG_PLAY = "Song Play";
//	public static final String SONG_PLAY_MUSIC = "Music";
//	public static final String SONG_PLAY_VIDEO = "Video";
//	public static final String SONG_PLAY_RADIO = "Radio";

//	Type, PayType
	private static final String PARAMS_TYPE = "Type";
	private static final String PARAMS_PAYTYPE = "PayType";

//	Type : Hungama, Facebook, Twitter, Google
	public static final String TYPE_LOGIN_HUNGAMA = "Hungama";
//	public static final String TYPE_LOGIN_FACEBOOK = "Facebook";
//	public static final String TYPE_LOGIN_TWITTER = "Twitter";
//	public static final String TYPE_LOGIN_GOOGLE = "Google";

//	Type : Online, Offline
	public static final String TYPE_PLAY_ONLINE = "Online";
	public static final String TYPE_PLAY_OFFLINE = "Offline";
	public static final String TYPE_PLAY_CHROME_CAST = "ChromeCast";

//	Type : Upgrade, Download, Subscription
//	public static final String TYPE_TRANSACT_DOWNLOAD = "Download";
//	public static final String TYPE_TRANSACT_SUBSCRIPTION = "Subscription";

//	PayType : Google, Mobile, Wallet
//	public static final String TYPE_PLAN_GOOGLE = "Google";
//	public static final String TYPE_PLAN_MOBILE = "Mobile";
//	public static final String TYPE_PLAN_WALLET = "Wallet";

//	Audio, Video
//	public static final String TYPE_REDEEM_AUDIO = "Audio";
//	public static final String TYPE_REDEEM_VIDEO = "Video";

//	Type : Created, Saved, Played
	public static final String TYPE_PLAYLIST_CREATED = "Created";
	public static final String TYPE_PLAYLIST_SAVED = "Saved";
	public static final String TYPE_PLAYLIST_PLAYED = "Played";

	/**
	 *  Deep Link Launch
	 */
	public static final String DEEP_LINK_LAUNCH = "DLL";

	// public static final String DEEP_LINK_LAUNCH = "DeepLinkLaunch";

	public static final void postEvent(Context context, String eventName) {
		if (ENABLED) {
			try {
//				AdXConnect.getAdXConnectEventInstance(context, eventName, "", "");
				Logger.writetofileApsalar("Apsalar event ::: " + eventName, true);
				Apsalar.event(eventName);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			} catch (Error e) {
					Logger.printStackTrace(e);
			}

		}
	}

	private static final void postEventWithParams(Context context, String eventName, Object... subParameter) {
		if (ENABLED) {
			try{
//				AdXConnect.getAdXConnectEventInstance(context, eventName,
//						subParameter, "");
				Apsalar.event(eventName, subParameter);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			} catch (Error e) {
				Logger.printStackTrace(e);
			}
		}
	}

	public static final void postEvent(Context context, String eventName, String type) {
		if (ENABLED) {
			try{
				Logger.writetofileApsalar("Apsalar event ::: " + eventName + " :: " + type, true);
				postEventWithParams(context, eventName, ApsalarEvent.PARAMS_TYPE, type);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			} catch (Error e) {
				Logger.printStackTrace(e);
			}
		}
	}

	public static final void postEvent(Context context, String eventName, String type, String payType) {
		if (ENABLED) {
			try{
				Logger.writetofileApsalar("Apsalar event ::: " + eventName + " :: " + type + " :: " + payType, true);
				postEventWithParams(context, eventName, ApsalarEvent.PARAMS_TYPE, type, ApsalarEvent.PARAMS_PAYTYPE, payType);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			} catch (Error e) {
				Logger.printStackTrace(e);
			}
		}
	}

	public static final void postLoginEvent(SocialNetwork provider){
//		if(provider!=null && provider == SocialNetwork.FACEBOOK) {
//			ApsalarEvent.postEvent(null, ApsalarEvent.LOGIN_COMPLETED, ApsalarEvent.TYPE_LOGIN_FACEBOOK);
//		} else if(provider!=null && provider == SocialNetwork.GOOGLE) {
//			ApsalarEvent.postEvent(null, ApsalarEvent.LOGIN_COMPLETED, ApsalarEvent.TYPE_LOGIN_GOOGLE);
//		} else if(provider!=null && provider == SocialNetwork.TWITTER) {
//			ApsalarEvent.postEvent(null, ApsalarEvent.LOGIN_COMPLETED, ApsalarEvent.TYPE_LOGIN_TWITTER);
//		} else {
//			ApsalarEvent.postEvent(null, ApsalarEvent.LOGIN_COMPLETED, ApsalarEvent.TYPE_LOGIN_HUNGAMA);
//		}
	}
}
