package com.hungama.myplay.activity.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Constants;
import com.hungama.myplay.activity.util.Utils;

public class LoginForgotPasswordFragment extends Fragment implements
		OnClickListener {

	private static final String TAG = "LoginForgotPasswordFragment";

	// ======================================================
	// Public.
	// ======================================================

	public interface OnForgotPasswordSubmitListener {

		/**
		 * Invoked when the user submitted an identication string to get new
		 * password.
		 */
		public void onForgotPasswordSubmit(String identicationString);
	}

	public void setOnForgotPasswordSubmitListener(
			OnForgotPasswordSubmitListener listener) {
		mOnForgotPasswordSubmitListener = listener;
	}

	// ======================================================
	// Fragment life cycle and listeners.
	// ======================================================

	private OnForgotPasswordSubmitListener mOnForgotPasswordSubmitListener;

	private LanguageTextView mTextMessage;
	private EditText mTextIdenticationString;
	private LanguageButton mButtonSubmit;
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.postCrashlitycsLog(getActivity(), LoginForgotPasswordFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(
				R.layout.fragment_login_forgot_password, container, false);
		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		if (mApplicationConfigurations.getUserSelectedLanguage() != Constants.LANGUAGE_ENGLISH) {
			Utils.traverseChild(rootView, getActivity());
		}
		mTextMessage = (LanguageTextView) rootView
				.findViewById(R.id.login_forgot_password_test_message);
		mTextIdenticationString = (EditText) rootView
				.findViewById(R.id.login_forgot_password_text_identication);
		mButtonSubmit = (LanguageButton) rootView
				.findViewById(R.id.login_forgot_password_button_submit);
		mButtonSubmit.setOnClickListener(this);

		return rootView;
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();
		if (viewId == R.id.login_forgot_password_button_submit) {
			String identicationString = (mTextIdenticationString.getText() != null ? mTextIdenticationString
					.getText().toString() : null);

			if (TextUtils.isEmpty(identicationString)) {
				// field is empty.
				return;
			}

			// skips if the given string is not an email or mobile number.
			String validMailRegex = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}";
			String validMobileRegex = "[0-9]{10,10}";
			if (!identicationString.matches(validMailRegex)
					&& !identicationString.matches(validMobileRegex)) {
				Utils.makeText(getActivity(),

				getResources().getString(R.string.login_signup_error_email),
						Toast.LENGTH_LONG).show();
				return;
			}

			if (mOnForgotPasswordSubmitListener != null) {
				mOnForgotPasswordSubmitListener
						.onForgotPasswordSubmit(identicationString);
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		// FlurryAgent.setContinueSessionMillis(Constants.FLURRY_SESSION_LENGTH);
		// FlurryAgent.onStartSession(getActivity(),
		// getString(R.string.flurry_app_key));
		Analytics.startSession(getActivity(), this);
		Analytics.onPageView();
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

}
