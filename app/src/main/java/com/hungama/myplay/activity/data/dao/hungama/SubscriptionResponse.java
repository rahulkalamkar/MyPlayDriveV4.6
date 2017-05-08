package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class SubscriptionResponse implements Serializable {

	public static final String KEY_CODE = "code";
	public static final String KEY_STATUS = "status";
//	public static final String KEY_ORDER_ID = "order_id";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
//	public static final String KEY_PLAN = "plan";
	// public static final String KEY_TYPE = "type";
//	public static final String KEY_TRANSACTION_SESSION = "transaction_session";
//	public static final String KEY_ORDER_STATUS = "order_status";

	@Expose
	@SerializedName(KEY_CODE)
	private final String code;
	@Expose
	@SerializedName(KEY_STATUS)
	private final String status;
//	@Expose
//	@SerializedName(KEY_ORDER_ID)
//	private final String order_id;
	@Expose
	@SerializedName(KEY_MESSAGE)
	private final String message;
	@Expose
	@SerializedName(KEY_DISPLAY)
	private final String display;

//	@Expose
//	@SerializedName(KEY_TRANSACTION_SESSION)
//	private String transaction_session;
//
//	@Expose
//	@SerializedName(KEY_PLAN)
//	private List<Plan> plan;
//
//	@Expose
//	@SerializedName(KEY_ORDER_STATUS)
//	private int order_status;

	private SubscriptionType subscriptionType = null;

	public SubscriptionResponse(String code, String status,// String order_id,
			String message, String display) {//}, List<Plan> plan) {
		this.code = code;
		this.status = status;
//		this.order_id = order_id;
		this.message = message;
		this.display = display;
//		this.plan = plan;
	}

	public String getCode() {
		return code;
	}

	public String getStatus() {
		if (status == null)
			return "";
		return status;
	}

//	public String getOrder_id() {
//		return order_id;
//	}

	public String getMessage() {
		if (message == null)
			return "";
		return message;
	}

	public String getDisplay() {
		if (display == null)
			return "0";
		return display;
	}

//	public List<Plan> getPlan() {
//		return plan;
//	}
//
//	public void setPlan(List<Plan> plan) {
//		this.plan = plan;
//	}

	public SubscriptionType getSubscriptionType() {
		return subscriptionType;
	}

	public void setSubscriptionType(SubscriptionType type) {
		this.subscriptionType = type;
	}

//	public String getTransaction_session() {
//		return transaction_session;
//	}
//
//	public int getOrder_status() {
//		return order_status;
//	}
}
