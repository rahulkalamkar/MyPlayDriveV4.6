package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * DownloadResponse a response for all types of download flow web service call.
 */
public class DownloadResponse implements Serializable {

	public static final String KEY_CODE = "code";
	public static final String KEY_STATUS = "status";
	public static final String KEY_ORDER_ID = "order_id";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_DISPLAY = "display";
//	public static final String KEY_PLAN = "plan";
//	public static final String KEY_PLAN_ID = "plan_id";
//	public static final String KEY_REMAINING_DOWNLOAD_COUNT = "remaining_download_count";
//	public static final String KEY_BALANCE_CREDIT_LIMIT = "balance_credit_limit";
	public static final String KEY_URL = "url";
//	public static final String KEY_MSISDN = "msisdn";
//	public static final String KEY_TYPE = "type";
//	public static final String KEY_CONSENT = "consent";
//	public static final String KEY_TRANSACTION_SESSION = "transaction_session";
//	public static final String KEY_ORDER_STATUS = "order_status";

	@Expose
	@SerializedName(KEY_CODE)
	private final String code;
	@Expose
	@SerializedName(KEY_STATUS)
	private final String status;

//	@Expose
//	@SerializedName(KEY_ORDER_STATUS)
//	private int order_status;
	@Expose
	@SerializedName(KEY_ORDER_ID)
	private final String order_id;
	@Expose
	@SerializedName(KEY_MESSAGE)
	private final String message;
	@Expose
	@SerializedName(KEY_DISPLAY)
	private final String display;
//	@Expose
//	@SerializedName(KEY_PLAN)
//	private List<DownloadPlan> plan;
//	@Expose
//	@SerializedName(KEY_PLAN_ID)
//	private final int planId;
//	@Expose
//	@SerializedName(KEY_REMAINING_DOWNLOAD_COUNT)
//	private final int remainingDownloadCount;
//	@Expose
//	@SerializedName(KEY_BALANCE_CREDIT_LIMIT)
//	private final int balanceCreditLimit;
	@Expose
	@SerializedName(KEY_URL)
	private final String url;
//	@Expose
//	@SerializedName(KEY_MSISDN)
//	private final String msisdn;
//	@Expose
//	@SerializedName(KEY_TYPE)
//	private final String type;
//
//	@Expose
//	@SerializedName(KEY_CONSENT)
//	private int consent;
//
//	@Expose
//	@SerializedName(KEY_TRANSACTION_SESSION)
//	private final String transaction_session;

	private DownloadOperationType downloadType = null;

	public DownloadResponse(String code, String status, String order_id,
			String message, String display,// List<DownloadPlan> plan,
			//int planId, int remainingDownloadCount, int balanceCreditLimit,
			String url) {//}), String msisdn, String type, String session) {
		this.code = code;
		this.status = status;
		this.order_id = order_id;
		this.message = message;
		this.display = display;
//		this.plan = plan;
//		this.planId = planId;
//		this.remainingDownloadCount = remainingDownloadCount;
//		this.balanceCreditLimit = balanceCreditLimit;
		this.url = url;
//		this.msisdn = msisdn;
//		this.type = type;
//		this.transaction_session = session;
	}

	public String getCode() {
		return code;
	}

	public String getStatus() {
		return status;
	}

	public String getOrder_id() {
		return order_id;
	}

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

//	public List<DownloadPlan> getPlan() {
//		return plan;
//	}
//
//	public void setPlan(List<DownloadPlan> plan) {
//		this.plan = plan;
//	}
//
//	public int getPlanId() {
//		return planId;
//	}
//
//	public int getRemainingDownloadCount() {
//		return remainingDownloadCount;
//	}
//
//	public int getBalanceCreditLimit() {
//		return balanceCreditLimit;
//	}

	public String getUrl() {
		return url;
	}

//	public String getMsisdn() {
//		return msisdn;
//	}
//
//	public String getType() {
//		return type;
//	}
//
//	public boolean isConsent() {
//		return (consent == 1);
//	}

	public DownloadOperationType getDownloadType() {
		return downloadType;
	}

	public void setDownloadType(DownloadOperationType downloadType) {
		this.downloadType = downloadType;
	}

//	public String getTransaction_session() {
//		return transaction_session;
//	}
//
//	public int getOrder_status() {
//		return order_status;
//	}

}
