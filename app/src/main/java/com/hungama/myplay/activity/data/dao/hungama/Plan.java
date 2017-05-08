package com.hungama.myplay.activity.data.dao.hungama;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.hungama.myplay.activity.util.billing.SkuDetails;

import java.io.Serializable;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class Plan implements Serializable {

	public static final String KEY_PLAN_ID = "plan_id";
	public static final String KEY_PLAN_NAME = "plan_name";
	public static final String KEY_PLAN_PRICE = "plan_price";
	public static final String KEY_PLAN_CURRENCY = "plan_currency";
	public static final String KEY_PLAN_DURATION = "plan_duration";
	public static final String KEY_MSISDN = "msisdn";
	public static final String KEY_SUBSCRIPTION_STATUS = "subscription_status";
	public static final String KEY_PURCHASE_DATE = "purchase_date";
	public static final String KEY_VALIDITY_DATE = "validity_date";
	public static final String KEY_PLAN_TYPE = "type";
	public static final String KEY_TRIAL = "trial";
	public static final String KEY_DAYS_LEFT = "trial_expiry_days_left";
	public static final String KEY_PLAN_IMAGE = "plan_image_key";
	public static final String KEY_UNSUBSCRIBE_BUTTON = "unsub";
	public static final String KEY_consent = "consent";
	public static final String KEY_PRODUCT_ID = "product_id";

	@Expose
	@SerializedName(KEY_PLAN_ID)
	private final int planId;
	@Expose
	@SerializedName(KEY_PLAN_NAME)
	private final String planName;
	@Expose
	@SerializedName(KEY_PLAN_PRICE)
	private final String planPrice;
	@Expose
	@SerializedName(KEY_PLAN_CURRENCY)
	private final String planCurrency;
	@Expose
	@SerializedName(KEY_PLAN_DURATION)
	private final int planDuration;
	@Expose
	@SerializedName(KEY_MSISDN)
	private final String msisdn;
	@Expose
	@SerializedName(KEY_SUBSCRIPTION_STATUS)
	private final String subscriptionStatus;
	@Expose
	@SerializedName(KEY_PURCHASE_DATE)
	private String purchaseDate;
	@Expose
	@SerializedName(KEY_VALIDITY_DATE)
	private String validityDate;
	@Expose
	@SerializedName(KEY_PLAN_TYPE)
	private String type;
	@Expose
	@SerializedName(KEY_TRIAL)
	private String trial;
	@Expose
	@SerializedName(KEY_DAYS_LEFT)
	private int daysLeft;
	@Expose
	@SerializedName(KEY_PLAN_IMAGE)
	private String planImageKey;
	@Expose
	@SerializedName(KEY_UNSUBSCRIBE_BUTTON)
	private String unsub;
	@Expose
	@SerializedName(KEY_PRODUCT_ID)
	private String productId;

	private PlanType planType;

	private SkuDetails skudetails;

	@Expose
	@SerializedName(KEY_consent)
	private int consent;

	public Plan(int plan_id, String plan_name, String plan_price,
				String plan_currency, int plan_duration, String msisdn,
				String subscription_status, String purchase_date,
				String validity_date, String creditBalance, String status,
				String type, String trial, int daysLeft, String productId) {
		this.planId = plan_id;
		this.planName = plan_name;
		this.planPrice = plan_price;
		this.planCurrency = plan_currency;
		this.planDuration = plan_duration;
		this.msisdn = msisdn;
		this.subscriptionStatus = subscription_status;
		this.purchaseDate = purchase_date;
		this.validityDate = validity_date;
		this.type = type;
		this.trial = trial;
		this.daysLeft = daysLeft;
		this.productId = productId;
	}

	public int getPlanId() {
		return planId;
	}

	public String getPlanName() {
		return planName;
	}

	public String getPlanPrice() {
		return planPrice;
	}

	public String getPlanCurrency() {
		return planCurrency;
	}

	public int getPlanDuration() {
		return planDuration;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public String getSubscriptionStatus() {
		return subscriptionStatus;
	}

	public String getPurchaseDate() {
		return purchaseDate;
	}

	public void setPurchaseDate(String purchaseDate) {
		this.purchaseDate = purchaseDate;

	}

	public String getValidityDate() {
		return validityDate;
	}

	public void setValidityDate(String validityDate) {
		this.validityDate = validityDate;

	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setPlanType(PlanType planType) {
		this.planType = planType;
	}

	public PlanType getPlanType() {
		if (planType == null)
			planType = PlanType.getPlanByName(type);

		return planType;
	}

	public SkuDetails getSkudetails() {
		return skudetails;
	}

	public void setSkudetails(SkuDetails skudetails) {
		this.skudetails = skudetails;
	}

	public boolean isTrial() {
		if (this.trial != null && this.trial.equalsIgnoreCase("Y")) {
			return true;
		}
		return false;
	}

	public boolean isUnssubscribeEnabled() {
		if (this.unsub != null && this.unsub.equalsIgnoreCase("Y")) {
			return true;
		}
		return false;
	}

	public int getTrailExpiryDaysLeft() {
		return daysLeft;
	}

	public String getPlanImageKey() {
		return planImageKey;
	}

	public String getProductId() {
		return productId;
	}
}
