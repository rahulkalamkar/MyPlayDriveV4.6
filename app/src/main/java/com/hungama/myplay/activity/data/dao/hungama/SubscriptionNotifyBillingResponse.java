package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class SubscriptionNotifyBillingResponse implements Serializable {

	public static final String KEY_VALID_TOKEN = "valid_token";
	public static final String KEY_STATUS = "status";

	@Expose
	@SerializedName(KEY_VALID_TOKEN)
	private final int validToken;
	@Expose
	@SerializedName(KEY_STATUS)
	private final String status;

	public SubscriptionNotifyBillingResponse(int validToken, String status) {
		this.validToken = validToken;
		this.status = status;
	}

	public int getValidToken() {
		return validToken;
	}

	public String getStatus() {
		return status;
	}
}
