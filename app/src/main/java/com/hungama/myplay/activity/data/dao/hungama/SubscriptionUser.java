package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class SubscriptionUser implements Serializable {

	public static final String KEY_IDENTITY = "identity";
	public static final String KEY_FIRSTNAME = "firstname";
	public static final String KEY_LASTNAME = "lastname";
	public static final String KEY_PROFILE_IMAGE = "profile_image";

	@Expose
	@SerializedName(KEY_IDENTITY)
	private final long identity;
	@Expose
	@SerializedName(KEY_FIRSTNAME)
	private final String firstname;
	@Expose
	@SerializedName(KEY_LASTNAME)
	private final String lastname;
	@Expose
	@SerializedName(KEY_PROFILE_IMAGE)
	private final String profileImage;

	public SubscriptionUser(long identity, String firstname, String lastname, String profileImage) {
		this.identity = identity;
		this.firstname = firstname;
		this.lastname = lastname;
		this.profileImage = profileImage;
	}

	public long getIdentity() {
		return identity;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getProfileImage() {
		return profileImage;
	}
}
