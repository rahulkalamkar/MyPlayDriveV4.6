package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class SubscriptionCheckResponse implements Serializable {

	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
	public static final String KEY_TRIAL_PLAN_DISPLAY = "trial_plan_display";
	public static final String KEY_PLAN = "plan";
	public static final String KEY_PREVIOUS_PLAN = "previous_plan";

	@Expose
	@SerializedName(KEY_CODE)
	private final String code;
	@Expose
	@SerializedName(KEY_MESSAGE)
	private final String message;
	@Expose
	@SerializedName(KEY_DISPLAY)
	private final String display;
	@Expose
	@SerializedName(KEY_TRIAL_PLAN_DISPLAY)
	private final String trialPlanDisplay;
	@Expose
	@SerializedName(KEY_PLAN)
	private Plan plan;
	@Expose
	@SerializedName(KEY_PREVIOUS_PLAN)
	private Plan previousPlan;

	private SubscriptionType subscriptionType = null;

	public SubscriptionCheckResponse(String code, String status,
									 String order_id, String message, String display, Plan plan,
									 String trialPlanDisplay) {
		this.code = code;
		this.message = message;
		this.display = display;
		this.plan = plan;
		this.trialPlanDisplay = trialPlanDisplay;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public String getDisplay() {
		return display;
	}

	public String getTrialPlanDisplay() {
		return trialPlanDisplay;
	}

	public Plan getPlan() {
		return plan;
	}

	public void setPlan(Plan plan) {
		this.plan = plan;
	}

	public Plan getPreviousPlan() {
		return previousPlan;
	}

	public void setPreviousPlan(Plan plan) {
		this.previousPlan = plan;
	}

	public SubscriptionType getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(SubscriptionType type) {
		this.subscriptionType = type;
	}

}
