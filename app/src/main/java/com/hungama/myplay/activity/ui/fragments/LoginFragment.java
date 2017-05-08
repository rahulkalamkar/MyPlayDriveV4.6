package com.hungama.myplay.activity.ui.fragments;

import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.catchmedia.SignOption;
import com.hungama.myplay.activity.data.dao.catchmedia.SignupField;
import com.hungama.myplay.activity.data.dao.hungama.SocialNetwork;
import com.hungama.myplay.activity.ui.DownloadActivity;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.LoginActivity;
import com.hungama.myplay.activity.ui.OnApplicationStartsActivity;
import com.hungama.myplay.activity.ui.ProfileActivity;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.ScreenLockStatus;
import com.hungama.myplay.activity.util.Utils;

import java.util.List;

public class LoginFragment extends Fragment implements OnClickListener {

	private static final String TAG = "LoginFragment";

	// ======================================================
	// Public.
	// ======================================================

	/**
	 * Interface definition to be invoked when the user has selected one of the
	 * sign in / up options from the screen.
	 */
	public interface OnLoginOptionSelectedListener {

		/**
		 * Invoked when the user has selected to sign up / login with a social
		 * network.
		 * 
		 * @param selectedSocialNetwork
		 *            to connect with.
		 */
		public void onConnectWithSocialNetworkSelected(
				SocialNetwork selectedSocialNetwork);

		/**
		 * Invoked when the user has selected to login with Hungama.
		 */
		public void onLoginWithHungamaSelected(List<SignupField> signupFields);

		/**
		 * Invoked when the user clicked on the "forgot password" in the Hungama
		 * login fields.
		 */
		public void onLoginWithHungamaForgotPasswordSelected();

		/**
		 * Invoked when the user has selected to sign up.
		 */
//		public void onSignUpSelected();

		/**
		 * Invoked when the user has selected to skip the process of login /
		 * sign up.
		 */
//		public void onSkipSelected();

        public void onPerformSignup(List<SignupField> signupFields);
	}

	public void setSignOprions(List<SignOption> signOptions) {
		mSignOptions = signOptions;
        setSignupFields(mSignOptions);
	}

    public void setSignupFields(List<SignOption> signOptions) {
        try {
            mSignupFields = mSignOptions.get(1).getSignupFields();
        } catch (Exception e){
            Logger.printStackTrace(e);
        }
    }

	/**
	 * Register a callback to be invoked when the user selected a signing
	 * option.
	 */
	public void setOnLoginOptionSelectedListener(
			OnLoginOptionSelectedListener listener) {
		mOnLoginOptionSelectedListener = listener;
	}

//    public void setOnSignupOptionSelectedListener(
//            LoginSignupFragment.OnSignupOptionSelectedListener listener) {
//        mOnSignupOptionSelectedListener = listener;
//    }

	// ======================================================
	// Fragment life cycle and listeners.
	// ======================================================

	private OnLoginOptionSelectedListener mOnLoginOptionSelectedListener;
//    private LoginSignupFragment.OnSignupOptionSelectedListener mOnSignupOptionSelectedListener;

	private List<SignOption> mSignOptions;
    private List<SignupField> mSignupFields;

	private LinearLayout mHungamaLoginFieldsContainer, mHungamaSignupFieldsContainer;

	private RelativeLayout mButtonConnectFacebook;
	private RelativeLayout mButtonConnectTwitter;
	private RelativeLayout mButtonConnectGoogle;

	private LanguageButton mButtonHungamaLogin, mButtonHungamaSignup;
	private LanguageTextView mButtonHungamaForgotPassword;

//	private LanguageTextView mButtonSignUp;
//	private LanguageButton mButtonSkip;

	private LinearLayout subtitleTextLayout, llLoginFields, llSignupFields;
	private LanguageTextView titleText;
//	private LinearLayout socialNetworkButtonsLayout;
	private RelativeLayout dividerLayout;

	private Bundle fromActivity;

	// Managers
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

    private ImageView ivLoginExpand, ivSignupExpand;
    private static final long ANIMATION_DURATION = 500;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Analytics.postCrashlitycsLog(getActivity(), LoginFragment.class.getName());
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_login, container,
				false);
		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
			Utils.traverseChild(rootView, getActivity());
		}

		fromActivity = getArguments();

		initializeUserControls(rootView);

		if (!Utils.isListEmpty(mSignOptions)) {
			LoginActivity.buildTextFieldsFromSignupFields(
					mHungamaLoginFieldsContainer, mSignOptions.get(0)
							.getSignupFields());
			// Utils.getSignOption(mSignOptions,
			// SignOption.SET_ID_MYPLAY_LOGIN).getSignupFields());
		} else {
            rootView.findViewById(R.id.divider_layout)
                    .setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_login_title_layout)
                    .setVisibility(View.GONE);
			rootView.findViewById(R.id.ll_login_fields)
					.setVisibility(View.GONE);
		}

        if (!Utils.isListEmpty(mSignupFields)) {
            LoginActivity.buildTextFieldsFromSignupFields(
                    mHungamaSignupFieldsContainer, mSignupFields);
            // Utils.getSignOption(mSignOptions,
            // SignOption.SET_ID_MYPLAY_LOGIN).getSignupFields());
        } else {
            rootView.findViewById(R.id.divider_layout_signup)
                    .setVisibility(View.GONE);
            rootView.findViewById(R.id.rl_signup_title_layout)
                    .setVisibility(View.GONE);
            rootView.findViewById(R.id.ll_signup_fields)
                    .setVisibility(View.GONE);
        }

		return rootView;
	}

	@Override
	public void onClick(View view) {
		int viewId = view.getId();

		if (viewId == R.id.login_button_facebook) {
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener
						.onConnectWithSocialNetworkSelected(SocialNetwork.FACEBOOK);
			}
		} else if (viewId == R.id.login_button_twitter) {
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener
						.onConnectWithSocialNetworkSelected(SocialNetwork.TWITTER);
			}
		} else if (viewId == R.id.login_button_google) {
			ScreenLockStatus.getInstance(getActivity()).dontShowAd();
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener
						.onConnectWithSocialNetworkSelected(SocialNetwork.GOOGLE);
			}
		} else if (viewId == R.id.login_hungama_login_button_login) {
			List<SignupField> signupFields = LoginActivity
					.generateSignupFieldsFromTextFields(mHungamaLoginFieldsContainer);
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener
						.onLoginWithHungamaSelected(signupFields);
			}

		} else if (viewId == R.id.login_hungama_login_button_forgot_password) {
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener
						.onLoginWithHungamaForgotPasswordSelected();
			}
		}/* else if (viewId == R.id.login_button_sign_up) {
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener.onSignUpSelected();
			}
		} else if (viewId == R.id.login_button_skip) {
			Logger.i(TAG, "Skipping.");
			if (mOnLoginOptionSelectedListener != null) {
				mOnLoginOptionSelectedListener.onSkipSelected();
			}
		} */else if (viewId == R.id.rl_login_title_layout) {
            Logger.i(TAG, "Expand Login.");
            if (!Utils.isListEmpty(mSignOptions)) {
                if (llLoginFields.getVisibility() == View.VISIBLE) {
                    // hides the content.
                    hideLoginContent();
                } else {
                    // shows the content.
                    showLoginContent();
                }
            }
        } else if (viewId == R.id.rl_signup_title_layout) {
            Logger.i(TAG, "Expand Signup.");
            if (!Utils.isListEmpty(mSignupFields)) {
                if (llSignupFields.getVisibility() == View.VISIBLE) {
                    // hides the content.
                    hideSignupContent();
                } else {
                    // shows the content.
                    showSignupContent();
                }
            }
        } else if (viewId == R.id.login_hungama_signup_button_login) {
            List<SignupField> signupFields = LoginActivity
                    .generateSignupFieldsFromTextFields(mHungamaSignupFieldsContainer);
            if (mOnLoginOptionSelectedListener != null) {
                mOnLoginOptionSelectedListener
                        .onPerformSignup(signupFields);
            }

        }
	}

	// ======================================================
	// Private helper methods.
	// ======================================================

	private void initializeUserControls(View rootView) {

		// initializes the Social networks buttons.
		mButtonConnectFacebook = (RelativeLayout) rootView
				.findViewById(R.id.login_button_facebook);
		mButtonConnectTwitter = (RelativeLayout) rootView
				.findViewById(R.id.login_button_twitter);
		mButtonConnectGoogle = (RelativeLayout) rootView
				.findViewById(R.id.login_button_google);

        ivLoginExpand = (ImageView) rootView.findViewById(R.id.iv_login_button_expand);
        rootView.findViewById(R.id.rl_login_title_layout).setOnClickListener(this);

        llLoginFields = (LinearLayout) rootView.findViewById(R.id.ll_login_fields);
        llLoginFields.setVisibility(View.GONE);

        ivSignupExpand = (ImageView) rootView.findViewById(R.id.iv_signup_button_expand);
        rootView.findViewById(R.id.rl_signup_title_layout).setOnClickListener(this);

        llSignupFields = (LinearLayout) rootView.findViewById(R.id.ll_signup_fields);
        llSignupFields.setVisibility(View.GONE);

		// generates their titles.
		Resources resources = getResources();
		LanguageTextView facebookTitle = (LanguageTextView) mButtonConnectFacebook
				.findViewById(R.id.login_button_facebook_title);
		if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
			facebookTitle.setText(Html.fromHtml(resources
					.getString(R.string.login_facebook_title_english)));

		} else {
			Utils.SetMultilanguageTextOnTextView(getActivity(), facebookTitle,
					resources.getString(R.string.login_facebook_title));
		}
		mButtonConnectFacebook.setOnClickListener(this);
		mButtonConnectTwitter.setOnClickListener(this);
		mButtonConnectGoogle.setOnClickListener(this);

		mHungamaLoginFieldsContainer = (LinearLayout) rootView
				.findViewById(R.id.login_hungama_login_fields_container);
		mButtonHungamaLogin = (LanguageButton) rootView
				.findViewById(R.id.login_hungama_login_button_login);
//		if (mApplicationConfigurations.getUserSelectedLanguage() == 0) {
//			mButtonHungamaLogin.setText(getResources().getString(
//					R.string.login_hungama_login_login));
//		} else {
//			Utils.SetMultilanguageTextOnButton(
//					getActivity(),
//					mButtonHungamaLogin,
//					getResources().getString(
//							R.string.login_hungama_login_login_lowercase));
//		}
		mButtonHungamaForgotPassword = (LanguageTextView) rootView
				.findViewById(R.id.login_hungama_login_button_forgot_password);
//		mButtonSignUp = (LanguageTextView) rootView
//				.findViewById(R.id.login_button_sign_up);
//		mButtonSkip = (LanguageButton) rootView
//				.findViewById(R.id.login_button_skip);

        mHungamaSignupFieldsContainer = (LinearLayout) rootView
                .findViewById(R.id.login_hungama_signup_fields_container);
        mButtonHungamaSignup = (LanguageButton) rootView
                .findViewById(R.id.login_hungama_signup_button_login);

		mButtonHungamaLogin.setOnClickListener(this);
        mButtonHungamaSignup.setOnClickListener(this);
		mButtonHungamaForgotPassword.setOnClickListener(this);
//		mButtonSignUp.setOnClickListener(this);

		// initializes the titles / texts.
		titleText = (LanguageTextView) rootView.findViewById(R.id.title_text);
//		titleText.setText(Utils.getMultilanguageTextLayOut(getActivity(),
//				getString(R.string.login_header_title_line_one))
//				+ " & "
//				+ Utils.getMultilanguageTextLayOut(getActivity(),
//						getString(R.string.login_header_title_line_one_2)));
        titleText.setText(Utils.getMultilanguageTextLayOut(getActivity(),
                getString(R.string.login_header_title_line_new)));
		subtitleTextLayout = (LinearLayout) rootView
				.findViewById(R.id.sub_title_text_layout);
//		socialNetworkButtonsLayout = (LinearLayout) rootView
//				.findViewById(R.id.social_network_buttons_layout);
		dividerLayout = (RelativeLayout) rootView
				.findViewById(R.id.divider_layout);

		// sets the title.
		// final SpannableStringBuilder sb = new
		// SpannableStringBuilder(getResources().getString(R.string.login_header_title_line_one));
		// final ForegroundColorSpan fcs = new
		// ForegroundColorSpan(getResources().getColor(R.color.login_title_text_love_color));
		// final ForegroundColorSpan fcsBegin = new
		// ForegroundColorSpan(getResources().getColor(R.color.login_text_color));
		// // Set the text color for love word
		// sb.setSpan(fcs, 33, 37, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		// sb.setSpan(fcsBegin, 0, 32, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
		// // make them also bold
		// titleText.setText(sb);

		// titleText.setText(R.string.login_header_title_line_one);

		/*
		 * The Login page can be shown from different contexts of the
		 * application toggles visibility of specific views based on it.
		 */
		if (fromActivity != null
				&& (fromActivity
						.getString(UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY) != null
						|| fromActivity
								.getString(DownloadActivity.ARGUMENT_DOWNLOAD_ACTIVITY) != null
						|| fromActivity
								.getString(ProfileActivity.ARGUMENT_PROFILE_ACTIVITY) != null || fromActivity
						.getString(HomeActivity.ARGUMENT_HOME_ACTIVITY) != null)) {
//			mButtonSkip.setVisibility(View.GONE);
		} else if (fromActivity != null
				&& fromActivity
						.getString(OnApplicationStartsActivity.ARGUMENT_ON_APPLICATION_START_ACTIVITY) != null) {
//			mButtonSkip.setVisibility(View.VISIBLE);
//			mButtonSkip.setOnClickListener(this);
		} else if (fromActivity != null
				&& fromActivity
						.getString(SettingsActivity.ARGUMENT_SETTINGS_ACTIVITY) != null) {
//			mButtonSkip.setVisibility(View.GONE);
			titleText.setVisibility(View.GONE);
			subtitleTextLayout.setVisibility(View.GONE);
			mButtonConnectFacebook.setVisibility(View.GONE);
            mButtonConnectGoogle.setVisibility(View.GONE);
            mButtonConnectTwitter.setVisibility(View.GONE);
//			socialNetworkButtonsLayout.setVisibility(View.GONE);
			dividerLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

    private void showLoginContent(){
        llLoginFields.setVisibility(View.VISIBLE);
        ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(
                ivLoginExpand, "rotation", 0f, 90f);
        imageViewObjectAnimator.setDuration(ANIMATION_DURATION); // miliseconds
        imageViewObjectAnimator.start();
        ((ScrollView) llLoginFields.getParent().getParent()).post(new Runnable() {
            @Override
            public void run() {
                ((ScrollView) llLoginFields.getParent().getParent()).fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void hideLoginContent(){
        llLoginFields.setVisibility(View.GONE);
        ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(
                ivLoginExpand, "rotation", 90f, 0f);
        imageViewObjectAnimator.setDuration(ANIMATION_DURATION); // miliseconds
        imageViewObjectAnimator.start();
    }

    private void showSignupContent(){
        llSignupFields.setVisibility(View.VISIBLE);
        ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(
                ivSignupExpand, "rotation", 0f, 90f);
        imageViewObjectAnimator.setDuration(ANIMATION_DURATION); // miliseconds
        imageViewObjectAnimator.start();
        ((ScrollView) llSignupFields.getParent().getParent()).post(new Runnable() {
            @Override
            public void run() {
                ((ScrollView) llSignupFields.getParent().getParent()).fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void hideSignupContent(){
        llSignupFields.setVisibility(View.GONE);
        ObjectAnimator imageViewObjectAnimator = ObjectAnimator.ofFloat(
                ivSignupExpand, "rotation", 90f, 0f);
        imageViewObjectAnimator.setDuration(ANIMATION_DURATION); // miliseconds
        imageViewObjectAnimator.start();
    }
}
