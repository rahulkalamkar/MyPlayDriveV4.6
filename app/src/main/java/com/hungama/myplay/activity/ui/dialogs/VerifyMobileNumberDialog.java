package com.hungama.myplay.activity.ui.dialogs;

import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageEditText;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Utils;

public class VerifyMobileNumberDialog extends Dialog implements
		android.view.View.OnClickListener, CommunicationOperationListener {

	public static final int VERIFY_MOBILE_LOGIN_ACTIVITY = 2001;

	private String mobile_no;

	public VerifyMobileNumberDialog(Activity a, long mobile_no) {
		super(a);
		this.mActivity = a;
		if (mobile_no != 0)
			this.mobile_no = "" + mobile_no;
		mDataManager = DataManager.getInstance(a);
	}

	public Activity mActivity;
	public Dialog d;
	LanguageEditText edt_mobileno;
	LanguageButton btn_submit;
	LanguageTextView txt_already_have_account;
	LanguageTextView txt_login_with;

	private DataManager mDataManager;
	private MyProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(R.layout.dialog_verify_account);
		setCanceledOnTouchOutside(true);
		setCancelable(true);

		View rootView = findViewById(R.id.ll_main);

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getContext());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getContext());
		}

		edt_mobileno = (LanguageEditText) findViewById(R.id.edt_mobileno);
		btn_submit = (LanguageButton) findViewById(R.id.btn_submit);

		txt_already_have_account = (LanguageTextView) findViewById(R.id.txt_already_have_account);
		txt_login_with = (LanguageTextView) findViewById(R.id.txt_login_with_account);

		btn_submit.setOnClickListener(this);
		txt_login_with.setOnClickListener(this);

		if (!TextUtils.isEmpty(mobile_no)) {
			edt_mobileno.setText(mobile_no);
		}

		String session = mDataManager.getApplicationConfigurations()
				.getSessionID();
		Boolean isRealUser = mDataManager.getApplicationConfigurations()
				.isRealUser();
		if (!TextUtils.isEmpty(session) && isRealUser) {
			txt_already_have_account.setVisibility(View.GONE);
			txt_login_with.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txt_login_with_account:
			startLoginActivity();
			break;
		case R.id.btn_submit:
			if (edt_mobileno.getText().toString().trim().length() == 10) {
				mDataManager.redeemSendOtp(this, edt_mobileno.getText()
						.toString().trim());
				dismiss();
			} else {
				if (edt_mobileno.getText().length() <= 0) {
					edt_mobileno.setError("Please enter Mobile number first");
					edt_mobileno.requestFocus();// setFocus();
				} else if (edt_mobileno.getText().length() < 10
						|| edt_mobileno.getText().length() > 10) {
					edt_mobileno.setError("Please enter valid Mobile number");
					edt_mobileno.requestFocus();// setFocus();
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
			OtpConfirmationDialog otp = new OtpConfirmationDialog(mActivity,
					redeemCouponResponse.getMobile());
			otp.show();
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

	private void startLoginActivity() {
		Intent startLoginActivityIntent = new Intent(mActivity,
				LoginActivity.class);
		startLoginActivityIntent.putExtra(
				UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY, "upgrade_activity");
		startLoginActivityIntent.putExtra(LoginActivity.FLURRY_SOURCE,
				FlurryConstants.FlurryUserStatus.RedeemCoupon.toString());
		mActivity.startActivityForResult(startLoginActivityIntent,
				VERIFY_MOBILE_LOGIN_ACTIVITY);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VerifyMobileNumberDialog.VERIFY_MOBILE_LOGIN_ACTIVITY
				&& resultCode == Activity.RESULT_OK) {
			dismiss();
			return;
		}
	}
}
