/**
 * 
 */
package com.hungama.myplay.activity.util;

import java.util.HashMap;
import java.util.Map;

/**
 * @author stas
 *
 */
public enum Extras {
	__PUSH_Login(-1, "login"),
	__PUSH_None(0, null),
	__PUSH_Audio_latest(1, "audio_latest"),
	__PUSH_Audio_featured(2, "audio_featured"),
	__PUSH_Audio_recommended(3, "audio_recommended"),
	__PUSH_Video_latest(4, "video_latest"),
	__PUSH_Video_featured(5, "video_featured"),
	__PUSH_Video_recommended(6, "video_recommended"),
	__PUSH_Audio_details_view(7, "content_id"),
	__PUSH_Video_details_view(8, "content_id"),
	// __PUSH_Specials(9, "specs"),
	__PUSH_App_tour(10, "app_tour"),
	// __PUSH_Live_radio(11,null),
	__PUSH_Live_radio(11, "live_radio"),
	__PUSH_Top_celebs_radio(12, "top_celebs"),
	// __PUSH_Live_radio_details_view(13, "channel_index"),
	__PUSH_Live_radio_details_view(13, "radio_id"),
	__PUSH_Top_celebs_radio_details_view(14, "artist_id"),
	__PUSH_Discover(15, "discover"),
	__PUSH_My_stream(16, "my_stream"),
	__PUSH_My_profile(17, "my_profile"),
	__PUSH_My_collection(18, "my_collection"),
	__PUSH_Offline_music_page(19, "go_offline"),
	__PUSH_Membership_Page(20, "show_membership"),
	/*
	 * __PUSH_My_favorites(19, "my_favourites"), __PUSH_Favorite_songs(20,
	 * "fav_songs"), __PUSH_Favorite_Albums(21, "fav_albums"),
	 * __PUSH_Favorite_Playlists(22, "fav_playlists"),
	 * __PUSH_Favorite_videos(23, "fav_videos"), __PUSH_My_playlists(24,
	 * "my_playlists"), __PUSH_My_discoveries(25, "my_discoveries"),
	 */
	__PUSH_My_preferences(26, "my_preferences"),
	__PUSH_Settings(27, "settings"),
	__PUSH_Rewards(28, "rewards"),
	__PUSH_Invite_friends(29, "invite"),
	__PUSH_Rate_app(30, "rate"),
	__PUSH_Feedback(31, "feedback"),
	__PUSH_Help_FAQ(32, "faq"),
	__PUSH_About(33, "about"),
	__PUSH_Comments(34, null),
	__PUSH_Search(35, "search"),
	__PUSH_Videos_In_Album(36, "content_id"), // ;
	__PUSH_Multi_Lingual(40, "show_languages"),
	__PUSH_Song_Catcher(41, "song_catcher"),
	__PUSH_Browse_By(42, "content_type"),
	__PUSH_Hsh_Discover(43, "discover_hash"),
	__PUSH_on_demand_radio_details_view(44, "Station_ID"),//"artist_id"
	__PUSH_on_demand_radio(45, "top_celebs");//,
//	__PUSH_Subscription_Plans(47, "Subscription_Plans");

	private int code;
	private String extra;
	/**
	 * @param code
	 * @param extra
	 */

	private static Map<Integer, Extras> codeToExtraMapping;

	private Extras(int code, String extra) {
		this.code = code;
		this.extra = extra;
	}

	public static Extras getExtras(int i) {
		if (codeToExtraMapping == null) {
			initMapping();
		}
		return codeToExtraMapping.get(i);
	}

	private static void initMapping() {
		codeToExtraMapping = new HashMap<Integer, Extras>();
		for (Extras s : values()) {
			codeToExtraMapping.put(s.code, s);
		}
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

}
