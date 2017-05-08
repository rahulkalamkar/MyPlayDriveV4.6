/**
 * 
 */
package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.SerializedName;

/**
 * @author offerperetz
 *
 */
public class UserProfileResponse {

	@SerializedName("code")
	private final int code;

	@SerializedName("user_id")
	private final long user_id;

	@SerializedName("username")
	private final String username;

	@SerializedName("first_name")
	private final String first_name;

	@SerializedName("last_name")
	private final String last_name;

	public UserProfileResponse(int code, long user_id, String username,
			String first_name, String last_name) {
		super();
		this.code = code;
		this.user_id = user_id;
		this.username = username;
		this.first_name = first_name;
		this.last_name = last_name;
	}

	public int getCode() {
		return code;
	}

	public long getUser_id() {
		return user_id;
	}

	public String getUsername() {
		return username;
	}

	public String getFirst_name() {
		return first_name;
	}

	public String getLast_name() {
		return last_name;
	}
}
