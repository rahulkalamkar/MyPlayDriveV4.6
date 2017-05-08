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
public enum Links {
	__PUSH_Login(-1,
			"com.hungama.myplay.activity.ui.HomeActivity"),
	__PUSH_None(0, ""), __PUSH_Audio_latest(1,
			"com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Audio_featured(
			2, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Audio_recommended(
			3, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Video_latest(
			4, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Video_featured(
			5, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Video_recommended(
			6, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Audio_details_view(
			7, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Video_details_view(
			8, "com.hungama.myplay.activity.ui.HomeActivity"),
	// __PUSH_Specials(9, "com.hungama.myplay.activity.ui.HomeActivity"),
	__PUSH_App_tour(10, "com.hungama.myplay.activity.ui.HomeActivity"),
	// __PUSH_Live_radio(11,"com.hungama.myplay.activity.ui.RadioActivity"),
	// __PUSH_Top_celebs_radio(12,
	// "com.hungama.myplay.activity.ui.RadioActivity"),
	// __PUSH_Live_radio_details_view(13,
	// "com.hungama.myplay.activity.ui.RadioActivity"),
	// __PUSH_Top_celebs_radio_details_view(14,"com.hungama.myplay.activity.ui.RadioActivity"),
	__PUSH_Live_radio(11, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Top_celebs_radio(
			12, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Live_radio_details_view(
			13, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Top_celebs_radio_details_view(
			14, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Discover(
			15, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_My_stream(
			16, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_My_profile(
			17, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_My_collection(
			18, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Offline_music_page(
			19, "com.hungama.myplay.activity.ui.GoOfflineActivity"), __PUSH_Membership_Page(
			20, "com.hungama.myplay.activity.ui.HomeActivity"),
	/*
	 * __PUSH_My_favorites(19, "com.hungama.myplay.activity.ui.HomeActivity"),
	 * __PUSH_Favorite_songs(20, "com.hungama.myplay.activity.ui.HomeActivity"),
	 * __PUSH_Favorite_Albums(21,
	 * "com.hungama.myplay.activity.ui.HomeActivity"),
	 * __PUSH_Favorite_Playlists(22,
	 * "com.hungama.myplay.activity.ui.HomeActivity"),
	 * __PUSH_Favorite_videos(23,
	 * "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_My_playlists(24,
	 * "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_My_discoveries(25,
	 * "com.hungama.myplay.activity.ui.HomeActivity"),
	 */
	__PUSH_My_preferences(26, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Settings(
			27, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Rewards(
			28, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Invite_friends(
			29, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Rate_app(
			30, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Feedback(
			31, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Help_FAQ(
			32, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_About(
			33, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Comments(
			34, null), __PUSH_Search(35,
			"com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Videos_In_Album(
			36, "com.hungama.myplay.activity.ui.HomeActivity"), // ;
	__PUSH_Multi_Lingual(40, "com.hungama.myplay.activity.ui.HomeActivity"), // ;
	__PUSH_Song_Catcher(41, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Browse_By(
			42, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_Hash_Discover(
			43, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_On_Demand_radio_Detail(
			44, "com.hungama.myplay.activity.ui.HomeActivity"), __PUSH_On_Demand_radio(
			45, "com.hungama.myplay.activity.ui.HomeActivity");//, __PUSH_Subscription_Plans(
//			47, "com.hungama.myplay.activity.ui.HomeActivity");

	private int code;
	private String action;
	/**
	 * @param code
	 * @param action
	 */

	private static Map<Integer, Links> codeToActionMapping;

	private Links(int code, String action) {
		this.code = code;
		this.action = action;
	}

	public static Links getLinks(int i) {
		if (codeToActionMapping == null) {
			initMapping();
		}
		return codeToActionMapping.get(i);
	}

	private static void initMapping() {
		codeToActionMapping = new HashMap<Integer, Links>();
		for (Links s : values()) {
			codeToActionMapping.put(s.code, s);
		}
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}
