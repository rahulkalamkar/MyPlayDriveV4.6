package com.hungama.myplay.activity.ui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.Window;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.LeftMenuExtraData;
import com.hungama.myplay.activity.data.dao.hungama.RedeemCouponResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.RedeemCouponOperation;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageEditText;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.ApsalarEvent;
import com.hungama.myplay.activity.util.Utils;

import java.util.Map;

public class RedeeomCouponDialog extends Dialog implements
		android.view.View.OnClickListener, CommunicationOperationListener {

	private LeftMenuExtraData extraData;

	public RedeeomCouponDialog(Activity activity) {
		super(activity);
		this.mActivity = activity;
		mDataManager = DataManager.getInstance(activity);
	}

	public Activity mActivity;
	LanguageEditText edt_coupencode;
	LanguageTextView btn_apply, txtExtra;
	private DataManager mDataManager;
	private MyProgressDialog mProgressDialog;

	private VerifyMobileNumberDialog verify;

	/**
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setContentView(R.layout.dialog_coupencode_layout);
		setCanceledOnTouchOutside(true);
		setCancelable(true);

		View rootView = findViewById(R.id.ll_main);

		ApplicationConfigurations mApplicationConfigurations = ApplicationConfigurations
				.getInstance(getContext());
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getContext());
		}

		edt_coupencode = (LanguageEditText) findViewById(R.id.edt_oupencode);
		btn_apply = (LanguageTextView) findViewById(R.id.btn_apply);
		btn_apply.setOnClickListener(this);

		txtExtra = (LanguageTextView) findViewById(R.id.text_redeem_extra);
		txtExtra.setMovementMethod(LinkMovementMethod.getInstance());
		txtExtra.setHighlightColor(Color.TRANSPARENT);
		if(extraData!=null && !TextUtils.isEmpty(extraData.getLink_text())){
			SpannableString ss = new SpannableString(extraData.getLink_text());
			ClickableSpan clickableSpan = new ClickableSpan() {
				@Override
				public void onClick(View textView) {
//					Intent i = new Intent(mActivity, WebviewNativeActivity.class);
//					i.putExtra("url", extraData.getClickable_link());
//					i.putExtra("title_menu", extraData.getTitle());
//					mActivity.startActivity(i);
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(extraData.getClickable_link()));
					mActivity.startActivity(i);
				}

				@Override
				public void updateDrawState(TextPaint ds) {
					super.updateDrawState(ds);
					ds.setUnderlineText(false);
				}
			};
			int start = extraData.getLink_text().indexOf(extraData.getClickable_text());
			int end = start + extraData.getClickable_text().length();
			ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			txtExtra.setText(ss);
			txtExtra.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_apply:
			if (edt_coupencode.getText().toString().trim().length() > 0) {
				mDataManager.redeemValidateCoupon(this, edt_coupencode
						.getText().toString().trim());
			} else {
				if (edt_coupencode.getText().length() <= 0) {
					edt_coupencode.setError("Please enter coupon code first");
					edt_coupencode.requestFocus();// setFocus();
				}
			}

			break;
		}
	}

	public void clickOnRedeemBtn(){
		if(verify!=null)
			verify.dismiss();
		if(btn_apply!=null)
			btn_apply.performClick();
	}

	@Override
	public void onStart(int operationId) {
		if (mProgressDialog == null)
			mProgressDialog = new MyProgressDialog(mActivity);
		mProgressDialog.show();
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		if (operationId == OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK) {
			if (mProgressDialog != null)
				mProgressDialog.dismiss();

			if (mActivity instanceof MainActivity) {
				if (((MainActivity) mActivity).mainSettingsFragment != null) {
					((MainActivity) mActivity).mainSettingsFragment
							.collepseGroups();
					((MainActivity) mActivity).mainSettingsFragment.onResume();
				}
			}

			Intent new_intent = new Intent();
			new_intent.setAction(HomeActivity.ACTION_NOTIFY_ADAPTER);
			mActivity.sendBroadcast(new_intent);
			try {
				dismiss();
			} catch (final Exception e) {
			}
			return;
		}
		RedeemCouponResponse redeemCouponResponse = (RedeemCouponResponse) responseObjects
				.get(RedeemCouponOperation.RESPONSE_KEY_REDEEM_COUPON);
		if (redeemCouponResponse.getCode() == 200) {
			String session = mDataManager.getApplicationConfigurations()
					.getSessionID();
			Boolean isRealUser = mDataManager.getApplicationConfigurations()
					.isRealUser();
			if (!TextUtils.isEmpty(session) && isRealUser) {
				String accountType = Utils.getAccountName(mActivity);
				mDataManager.getCurrentSubscriptionPlan(this, accountType);
			} else {
				if (mProgressDialog != null)
					mProgressDialog.dismiss();
			}
//			ApsalarEvent.postEvent(mActivity, ApsalarEvent.ECOUPON_USED);
			// Toast.makeText(mActivity, redeemCouponResponse.getMessage(),
			// Toast.LENGTH_SHORT).show();
			Utils.customDialogWithOk(mActivity,
					redeemCouponResponse.getMessage());
			try {
				dismiss();
			} catch (final Exception e) {
			}
		} else if (redeemCouponResponse.getCode() == 201
				|| redeemCouponResponse.getCode() == 400) {
			// Toast.makeText(mActivity, redeemCouponResponse.getMessage(),
			// Toast.LENGTH_SHORT).show();
			Utils.customDialogWithOk(mActivity,
					redeemCouponResponse.getMessage());
			if (mProgressDialog != null)
				mProgressDialog.dismiss();
			try {
				dismiss();
			} catch (final Exception e) {
			}

		} else if (redeemCouponResponse.getCode() == 202) {
			// Toast.makeText(mActivity, redeemCouponResponse.getMessage(),
			// Toast.LENGTH_SHORT).show();
			verify = new VerifyMobileNumberDialog(mActivity,
					redeemCouponResponse.getMobile());
			verify.show();
			// dismiss();
			if (mProgressDialog != null)
				mProgressDialog.dismiss();
		} else if (redeemCouponResponse.getCode() == 401) {
			// Toast.makeText(mActivity, redeemCouponResponse.getMessage(),
			// Toast.LENGTH_SHORT).show();
			Utils.customDialogWithOk(mActivity,
					redeemCouponResponse.getMessage());
			if (mProgressDialog != null)
				mProgressDialog.dismiss();
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		try{
			if ((mProgressDialog != null) && mProgressDialog.isShowing())
				mProgressDialog.dismiss();
		}catch (Exception e){}

	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (verify != null
				&& requestCode == VerifyMobileNumberDialog.VERIFY_MOBILE_LOGIN_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				// verify.onActivityResult(requestCode, resultCode, data);
				verify.dismiss();
				mDataManager.redeemValidateCoupon(this, edt_coupencode
						.getText().toString().trim());
			}
			return;
		} else if (requestCode == OtpConfirmationDialog.OTP_MOBILE_LOGIN_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				mDataManager.redeemValidateCoupon(this, edt_coupencode
						.getText().toString().trim());
			}
			return;
		}
	}

	public void setExtraData(LeftMenuExtraData extraData){
		this.extraData = extraData;
	}
}
