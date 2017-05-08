package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class SubscriptionStatusResponse implements Serializable {

	public static final String KEY_AFF_CODE = "aff_code";
	public static final String KEY_USER = "user";
	public static final String KEY_SUBSCRIPTION = "subscription";
	public static final String KEY_DOWNLOAD = "download";

	@Expose
	@SerializedName(KEY_AFF_CODE)
	private final String affCode;
	@Expose
	@SerializedName(KEY_USER)
	private final SubscriptionUser user;
	@Expose
	@SerializedName(KEY_SUBSCRIPTION)
	private final SubscriptionPlan subscription;
	@Expose
	@SerializedName(KEY_DOWNLOAD)
	private final SubscriptionDownloadPlan download;

	public SubscriptionStatusResponse(String affCode, SubscriptionUser user, SubscriptionPlan subscription,
									  SubscriptionDownloadPlan download) {
		this.affCode = affCode;
		this.user = user;
		this.subscription = subscription;
		this.download = download;
	}

	public String getAffCode() {
		return affCode;
	}

	public SubscriptionUser getUser() {
		return user;
	}

	public SubscriptionPlan getSubscription() {
		return subscription;
	}

	public SubscriptionDownloadPlan getDownload() {
		return download;
	}
}
