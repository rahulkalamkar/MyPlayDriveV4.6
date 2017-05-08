package com.hungama.myplay.activity.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.RedeemCouponResponse;
import com.hungama.myplay.activity.operations.hungama.RedeemCouponOperation;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageEditText;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Utils;

import java.util.Map;

public class OtpConfirmationDialog extends Dialog implements
		android.view.View.OnClickListener, CommunicationOperationListener {

	public static final int OTP_MOBILE_LOGIN_ACTIVITY = 2002;

	private String mobile_no;

	public OtpConfirmationDialog(Activity a, long mobile_no) {
		super(a);
		// TODO Auto-generated constructor stub
		this.mActivity = a;

		if (mobile_no != 0)
			this.mobile_no = "" + mobile_no;
		mDataManager = DataManager.getInstance(a);
	}

	public Activity mActivity;
	public Dialog d;
	LanguageEditText edt_otpcode;
	LanguageTextView btn_verify;
	LanguageTextView tv_otp_error;

	private DataManager mDataManager;
	private MyProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		// getWindow().getAttributes().windowAnimations =
		// R.style.dialog_animation;
		setContentView(R.layout.dialog_otp_confirmation);
		setCanceledOnTouchOutside(true);
		setCancelable(true);

		View rootView = findViewById(R.id.ll_main);

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getContext());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getContext());
		}

		edt_otpcode = (LanguageEditText) findViewById(R.id.edt_otpcode);
		btn_verify = (LanguageTextView) findViewById(R.id.btn_verify);

		tv_otp_error = (LanguageTextView) findViewById(R.id.tv_otp_error);

		btn_verify.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_verify:
			if (edt_otpcode.getText().length() > 0) {
				mDataManager.redeemValidateOtp(this, mobile_no, edt_otpcode
						.getText().toString().trim());
				dismiss();
			} else {
				if (edt_otpcode.getText().length() <= 0) {
					edt_otpcode.setError(mActivity.getResources().getString(
							R.string.otp_invalid_error));
					edt_otpcode.requestFocus();

				}
			}
			break;

		}
	}

	@Override
	public void onStart(int operationId) {
		if (mProgressDialog == null)
			mProgressDialog = new MyProgressDialog(mActivity);
		mProgressDialog.show();
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		RedeemCouponResponse redeemCouponResponse = (RedeemCouponResponse) responseObjects
				.get(RedeemCouponOperation.RESPONSE_KEY_REDEEM_COUPON);
		if (redeemCouponResponse.getCode() == 200) {
			// Toast.makeText(mActivity, redeemCouponResponse.getMessage(),
			// Toast.LENGTH_SHORT).show();
			Utils.customDialogWithOk(mActivity,
					redeemCouponResponse.getMessage());
			startLoginActivity();
			dismiss();
		} else {
			// Toast.makeText(mActivity, redeemCouponResponse.getMessage(),
			// Toast.LENGTH_SHORT).show();
			Utils.customDialogWithOk(mActivity,
					redeemCouponResponse.getMessage());
		}
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		if (mProgressDialog != null)
			mProgressDialog.dismiss();
	}

	public void startLoginActivity() {
		Intent startLoginActivityIntent = new Intent(mActivity,
				LoginActivity.class);
		startLoginActivityIntent.putExtra(
				UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY, "upgrade_activity");
		startLoginActivityIntent.putExtra(LoginActivity.IS_FROM_OTP, true);
		startLoginActivityIntent.putExtra(LoginActivity.OTP_MOBILE_NO,
				mobile_no);
		startLoginActivityIntent.putExtra(LoginActivity.FLURRY_SOURCE,
				FlurryConstants.FlurryUserStatus.RedeemCoupon.toString());
		mActivity.startActivityForResult(startLoginActivityIntent,
				OTP_MOBILE_LOGIN_ACTIVITY);
	}
}
