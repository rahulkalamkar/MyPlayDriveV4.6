package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class SubscriptionDownloadPlan implements Serializable {

	public static final String KEY_PLAN_ID = "plan_id";
	public static final String KEY_PLAN_NAME = "plan_name";
	public static final String KEY_BALANCE_CREDIT = "balance_credit";
	public static final String KEY_REPEAT_DOWNLOAD = "repeat_download";

	@Expose
	@SerializedName(KEY_PLAN_ID)
	private final int planId;
	@Expose
	@SerializedName(KEY_PLAN_NAME)
	private final String planName;
	@Expose
	@SerializedName(KEY_BALANCE_CREDIT)
	private final int balanceCredit;
	@Expose
	@SerializedName(KEY_REPEAT_DOWNLOAD)
	private final int repeatDownload;

	public SubscriptionDownloadPlan(int planId, String planName, int balanceCredit, int repeatDownload) {
		this.planId = planId;
		this.planName = planName;
		this.balanceCredit = balanceCredit;
		this.repeatDownload = repeatDownload;
	}

	public int getPlanId() {
		return planId;
	}

	public String getPlanName() {
		return planName;
	}

	public int getBalanceCredit() {
		return balanceCredit;
	}

	public int getRepeatDownload() {
		return repeatDownload;
	}
}
