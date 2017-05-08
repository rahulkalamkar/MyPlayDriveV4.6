package com.hungama.myplay.activity.data.dao.hungama.social;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ProfileLeaderboard {

	// period all / 7days
	// type self / friend / other

	public enum TYPE {
		FRIENDS("friend"), EVERYONE("other");

		public final String name;

		TYPE(String name) {
			this.name = name;
		}
	}

	public enum PERIOD {
		SEVEN("7days"), ALL("all");

		public final String name;

		PERIOD(String name) {
			this.name = name;
		}
	}

	public static final String KEY_LEADERBOARD_USERS = "data";

	@SerializedName(KEY_LEADERBOARD_USERS)
	public final List<LeaderBoardUser> leaderBoardUsers;

	public ProfileLeaderboard(List<LeaderBoardUser> leaderBoardUsers) {
		this.leaderBoardUsers = leaderBoardUsers;
	}
}
