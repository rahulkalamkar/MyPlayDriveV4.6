package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class SubscriptionPlan implements Serializable {

	public static final String KEY_PLAN_ID = "plan_id";
	public static final String KEY_PLAN_NAME = "plan_name";
	public static final String KEY_SUBSCRIPTION_STATUS = "subscription_status";
	public static final String KEY_START_DATE = "start_date";
	public static final String KEY_END_DATE = "end_date";
	public static final String KEY_TOTAL_DAYS = "total_days";
	public static final String KEY_DAYS_REMAINING = "days_remaining";
	public static final String KEY_UNSUB_BUTTON = "unsub_button";
	public static final String KEY_PLAN_DETAILS = "plan_details";
	public static final String KEY_TRIAL = "trial";
	public static final String KEY_DAYS_LEFT = "trial_expiry_days_left";
	public static final String KEY_TRIAL_PLAN = "trial_plan";
	public static final String KEY_SHOW_ADS = "show_ads";

	@Expose
	@SerializedName(KEY_PLAN_ID)
	private final int planId;
	@Expose
	@SerializedName(KEY_PLAN_NAME)
	private final String planName;
	@Expose
	@SerializedName(KEY_SUBSCRIPTION_STATUS)
	private final int subscriptionStatus;
	@Expose
	@SerializedName(KEY_START_DATE)
	private final String startDate;
	@Expose
	@SerializedName(KEY_END_DATE)
	private final String endDate;
	@Expose
	@SerializedName(KEY_TOTAL_DAYS)
	private final int totalDays;
	@Expose
	@SerializedName(KEY_DAYS_REMAINING)
	private final int daysRemaining;
	@Expose
	@SerializedName(KEY_UNSUB_BUTTON)
	private final int unsubButton;
	@Expose
	@SerializedName(KEY_PLAN_DETAILS)
	private final String planDetails;
	@Expose
	@SerializedName(KEY_TRIAL)
	private String trial;
	@Expose
	@SerializedName(KEY_DAYS_LEFT)
	private int daysLeft;
	@Expose
	@SerializedName(KEY_TRIAL_PLAN)
	private int trialPlan;
	@Expose
	@SerializedName(KEY_SHOW_ADS)
	private int showAds;

	public SubscriptionPlan(int planId, String planName, int subscriptionStatus, String startDate, String endDate, int
			totalDays, int daysRemaining, int unsubButton, String planDetails) {
		this.planId = planId;
		this.planName = planName;
		this.subscriptionStatus = subscriptionStatus;
		this.startDate = startDate;
		this.endDate = endDate;
		this.totalDays = totalDays;
		this.daysRemaining = daysRemaining;
		this.unsubButton = unsubButton;
		this.planDetails = planDetails;
	}

	public int getPlanId() {
		return planId;
	}

	public String getPlanName() {
		return planName;
	}

	public int getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public String getStartDate() {
		return startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public int getTotalDays() {
		return totalDays;
	}

	public int getDaysRemaining() {
		return daysRemaining;
	}

	public int getUnsubButton() {
		return unsubButton;
	}

	public String getPlanDetails() {
		return planDetails;
	}

	public boolean isTrial() {
		if (getTrialPlan() == 1) {
			return true;
		}
		return false;
	}

	public int getTrailExpiryDaysLeft() {
		return daysLeft;
	}

	public int getTrialPlan() {
		return trialPlan;
	}

	public int getShowAds() {
		return showAds;
	}

	public boolean isShowAds() {
		if (getShowAds() == 1) {
			return true;
		}
		return false;
	}
}
