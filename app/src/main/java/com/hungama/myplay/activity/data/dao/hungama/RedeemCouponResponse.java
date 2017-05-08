package com.hungama.myplay.activity.data.dao.hungama;

import java.io.Serializable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Media item represent any form of media that can be presented in the
 * application.
 */
public class RedeemCouponResponse implements Serializable {

	public static final String KEY_CODE = "code";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_MOBILE = "mobile";
	public static final String KEY_COUPON_CODE = "coupon_code";

	@Expose
	@SerializedName(KEY_CODE)
	private final int code;
	@Expose
	@SerializedName(KEY_MESSAGE)
	private final String message;
	@Expose
	@SerializedName(KEY_MOBILE)
	private final long mobile;
	@Expose
	@SerializedName(KEY_COUPON_CODE)
	private String coupon_code;

	private RedeemCouponType redeemCouponType = null;

	public RedeemCouponResponse(int code, String message, String display,
			long mobile, String coupon_code) {
		this.code = code;
		this.message = message;
		this.mobile = mobile;
		this.coupon_code = coupon_code;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		if (message == null)
			return "";
		return message;
	}

	public long getMobile() {
		return mobile;
	}

	public RedeemCouponType getRedeemCouponType() {
		return redeemCouponType;
	}

	public void setRedeemCouponType(RedeemCouponType type) {
		this.redeemCouponType = type;
	}

	public String getCouponCode() {
		return coupon_code;
	}
}
