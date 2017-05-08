/**
 * 
 */
package com.hungama.myplay.activity.ui.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionPlan;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionUser;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.operations.hungama.SubscriptionOperation;
import com.hungama.myplay.activity.ui.SettingsActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.dialogs.MyProgressDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.Utils;

import org.json.JSONArray;
import org.xml.sax.XMLReader;

import java.util.HashMap;
import java.util.Map;

import io.techery.progresshint.ProgressHintDelegate;
import io.techery.progresshint.addition.widget.SeekBar;

/**
 * @author DavidSvilem
 * 
 */
public class MembershipDetailsFragment extends Fragment implements
		CommunicationOperationListener {

	public final String TAG = "MembershipDetailsFragment";

	public static final String SUCCESS = "1";

	// Managers
	private DataManager mDataManager;
	private ApplicationConfigurations mApplicationConfigurations;

	// Views
	private TextView planDate, startDate, endDate, lblStartDate, lblEndDate, txtUserPlan, txtUserName;
	private boolean planLoaded = false;
	private SeekBar seekBar;
	private MyProgressDialog mProgressDialog;
	private LanguageButton unsubscribeButton;
//	private Plan plan;
	private LanguageTextView txtBenefits;
	private ImageView ivProfilePic;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();
		Analytics.postCrashlitycsLog(getActivity(), MembershipDetailsFragment.class.getName());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Fetch the root view
		View rootView;
		if (mApplicationConfigurations.isUserHasSubscriptionPlan()) {
			rootView = inflater.inflate(R.layout.fragment_membership_details,
					container, false);
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}
			initializeMembershipPage(rootView);
		} else {
			rootView = inflater.inflate(
					R.layout.fragment_no_membership_details, container, false);
			if (mApplicationConfigurations.getUserSelectedLanguage() != 0) {
				Utils.traverseChild(rootView, getActivity());
			}
			initializeUpgradePage(rootView);
		}

		if (getActivity() instanceof SettingsActivity) {
			String title = getResources()
					.getString(R.string.premium_membership);
			((SettingsActivity) getActivity()).setTitleBarText(title);
		}

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String accountType = Utils.getAccountName(getActivity());

		mDataManager.getCurrentSubscriptionPlan(this, accountType);

		if (planDate != null) {
			planDate.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		Analytics.startSession(getActivity(), this);
//		System.out.println(" ::::::::: onStart ::::::::::: ");
		if(planLoaded && seekBar!=null){
			seekBar.setVisibility(View.VISIBLE);
			seekBar.getHintDelegate().showPopup();
		}
	}

	@Override
	public void onStop() {
		if(seekBar!=null){
			seekBar.setVisibility(View.INVISIBLE);
			seekBar.getHintDelegate().hidePopup();
		}
//		System.out.println(" ::::::::: onStop ::::::::::: ");
		super.onStop();
		Analytics.onEndSession(getActivity());
	}

	@Override
	public void onStart(int operationId) {
		try {
			showLoadingDialog(Utils
					.getMultilanguageTextHindi(getActivity(), getActivity()
							.getResources().getString(R.string.processing)));
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":141", e.toString());
		}
	}

	public class MyTagHandler implements Html.TagHandler {
		boolean first= true;
		String parent=null;
		int index=1;
		@Override
		public void handleTag(boolean opening, String tag, Editable output,
							  XMLReader xmlReader) {
			if(tag.equals("ul")) parent="ul";
			else if(tag.equals("ol")) parent="ol";
			if(tag.equals("li")){
				if(parent.equals("ul")){
					if(first){
						if(output.length()==0)
							output.append("\t• ");
						else
							output.append("\n\t• ");
						first= false;
					}else{
						first = true;
					}
				}
				else{
					if(first){
						if(output.length()==0)
							output.append("\t"+index+". ");
						else
							output.append("\n\t"+index+". ");
						first= false;
						index++;
					}else{
						first = true;
					}
				}
			}
		}
	}

	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK:
				SubscriptionStatusResponse subscriptionStatusResponse = (SubscriptionStatusResponse) responseObjects
						.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
				if (subscriptionStatusResponse != null
						&& subscriptionStatusResponse.getSubscription() != null) {
					SubscriptionPlan subscriptionPlan = subscriptionStatusResponse.getSubscription();
					startDate.setText(subscriptionPlan.getStartDate());
					endDate.setText(subscriptionPlan.getEndDate());
					seekBar.setMax(subscriptionPlan.getTotalDays());
					seekBar.setProgress(subscriptionPlan.getTotalDays() - subscriptionPlan.getDaysRemaining());
					seekBar.setVisibility(View.VISIBLE);
					planLoaded = true;

					lblStartDate.setVisibility(View.VISIBLE);
					lblEndDate.setVisibility(View.VISIBLE);

					txtUserPlan.setText(subscriptionPlan.getPlanName());
					txtBenefits.setText(Html.fromHtml(subscriptionPlan.getPlanDetails(), null, new MyTagHandler()));
					((LinearLayout) txtBenefits.getParent()).setVisibility(View.VISIBLE);
					if (subscriptionPlan.getUnsubButton()==1) {
						unsubscribeButton.setVisibility(View.VISIBLE);
					} else {
						unsubscribeButton.setVisibility(View.GONE);
					}
				}

				if (subscriptionStatusResponse != null
						&& subscriptionStatusResponse.getUser() != null) {
					setUserDetail(subscriptionStatusResponse.getUser());
//						txtUserName.setText(subscriptionStatusResponse.getUser().getFirstname() + " " +
//								subscriptionStatusResponse.getUser().getLastname());
				}
				hideLoadingDialog();
				break;
			case OperationDefinition.Hungama.OperationId.SUBSCRIPTION:

				SubscriptionResponse subscriptionResponse = (SubscriptionResponse) responseObjects
						.get(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION);

				if (subscriptionResponse != null
						&& subscriptionResponse.getCode().equalsIgnoreCase(
								SUCCESS)) {

					mApplicationConfigurations
							.setIsUserHasSubscriptionPlan(false);

					unsubscribeButton.setVisibility(View.GONE);
					Toast.makeText(getActivity().getApplicationContext(),
							subscriptionResponse.getMessage(),
							Toast.LENGTH_LONG).show();
					getActivity().onBackPressed();

				} else {
					unsubscribeButton.setVisibility(View.VISIBLE);

					unsubscribeButton.setEnabled(true);
					Toast.makeText(getActivity().getApplicationContext(),
							subscriptionResponse.getMessage(),
							Toast.LENGTH_LONG).show();

					// Flurry report: User served a message to upgrade
					Map<String, String> reportMap = new HashMap<String, String>();
					reportMap.put(FlurryConstants.FlurrySubscription.SourcePage
							.toString(),
							FlurryConstants.FlurrySubscription.Membership
									.toString());
					Analytics
							.logEvent(
									FlurryConstants.FlurrySubscription.SubscriptionMessgaeServed
											.toString(), reportMap);
				}

				hideLoadingDialog();

				break;

			default:
				break;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {
		hideLoadingDialog();

	}

	/**
	 * Initialize and fill all detail of user
	 * 
	 * @param rootView
	 */
	public void initializeMembershipPage(View rootView) {
		planDate = (TextView) rootView.findViewById(R.id.membership_plan_date);
		unsubscribeButton = (LanguageButton) rootView
				.findViewById(R.id.btn_unsubscribe);
		unsubscribeButton.setVisibility(View.GONE);
		unsubscribeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((Button) v).setEnabled(false);
//				if (plan != null) {
//					String code = mApplicationConfigurations
//							.getSubscriptionIABcode();
//					String purchaseToken = mApplicationConfigurations
//							.getSubscriptionIABpurchseToken();
//
//					String accountType = Utils.getAccountName(getActivity());
//					if (accountType != null) {
//						mDataManager.getSubscriptionCharge(plan.getPlanId(),
//								plan.getType(), SubscriptionType.UNSUBSCRIBE,
//								MembershipDetailsFragment.this, code,
//								purchaseToken, accountType, false, "", plan.getProductId());
//					}
//				}
			}
		});

		String texts = mApplicationConfigurations.getApplicationTextList();
		if (texts != null) {
			try {
				JSONArray array = new JSONArray(texts);
				String txtBemnefits = "";
				for (int i = 0; i < array.length(); i++) {
					txtBemnefits += getString(R.string.membership_benefit_prefix)
							+ array.get(i) + "\n";
				}
				((LanguageTextView) rootView.findViewById(R.id.text_benifits))
						.setText(txtBemnefits);
//				((LanguageTextView) rootView.findViewById(R.id.text_benifits_new))
//						.setText(txtBemnefits);
			} catch (Exception e) {
				Logger.printStackTrace(e);
			}
		}

//			rootView.findViewById(R.id.ll_hungama_pay_details).setVisibility(View.VISIBLE);
		rootView.findViewById(R.id.ll_subscription_old).setVisibility(View.GONE);
		seekBar = (SeekBar) rootView.findViewById(R.id.main_membership_progress_bar_seek_bar_handle);
//		seekBar.setMax(31);
//		seekBar.setProgress(2);
		((ProgressHintDelegate.SeekBarHintDelegateHolder) seekBar).getHintDelegate()
				.setHintAdapter(new ProgressHintDelegate.SeekBarHintAdapter() {
					@Override
					public String getHint(android.widget.SeekBar seekBar, int progress) {
						if ((seekBar.getMax() - progress) > 1)
							return (seekBar.getMax() - progress) + " days remaining";
						else
							return (seekBar.getMax() - progress) + " day remaining";
					}
				});
		seekBar.setVisibility(View.INVISIBLE);
		startDate = (TextView) rootView.findViewById(R.id.txt_start_date);
		endDate = (TextView) rootView.findViewById(R.id.txt_end_date);
		lblStartDate = (TextView) rootView.findViewById(R.id.lbl_start_date);
		lblStartDate.setVisibility(View.INVISIBLE);
		lblEndDate = (TextView) rootView.findViewById(R.id.lbl_end_date);
		lblEndDate.setVisibility(View.INVISIBLE);

		txtBenefits = (LanguageTextView) rootView.findViewById(R.id.text_benifits_new);
		txtUserName = (TextView) rootView.findViewById(R.id.txt_user_name);
		txtUserPlan = (TextView) rootView.findViewById(R.id.txt_user_plan);
		ivProfilePic = (ImageView) rootView.findViewById(R.id.iv_profile_pic);
	}

	/**
	 * initialize and open upgrade screen in case of free user
	 * 
	 * @param rootView
	 */
	public void initializeUpgradePage(View rootView) {
		unsubscribeButton = (LanguageButton) rootView
				.findViewById(R.id.upgrade_button_subscribe);
		unsubscribeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// Flurry report: upgrade clicked
				Boolean loggedIn = mApplicationConfigurations.isRealUser();
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(FlurryConstants.FlurrySubscription.SourcePage
								.toString(),
						FlurryConstants.FlurrySubscription.Membership
								.toString());
				reportMap.put(
						FlurryConstants.FlurrySubscription.LoggedIn.toString(),
						loggedIn.toString());
				Analytics.logEvent(
						FlurryConstants.FlurrySubscription.TapsOnUpgrade
								.toString(), reportMap);

				((Button) v).setEnabled(false);
				Intent intent = new Intent(getActivity(), UpgradeActivity.class);
				startActivityForResult(intent, 0);

			}
		});
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SettingsActivity.LOGIN_ACTIVITY_CODE
				&& resultCode == SettingsActivity.RESULT_OK) {
		}
		// if(mApplicationConfigurations.isUserHasSubscriptionPlan()){
		String accountType = Utils.getAccountName(getActivity());

		mDataManager.getCurrentSubscriptionPlan(this, accountType);

		getActivity().getSupportFragmentManager().popBackStack();
		addMembershipDetailsFragment();
		// } else{
		// getFragmentManager().popBackStack();
		// }
	};

	/**
	 * open and refresh membership detail after login
	 */
	private void addMembershipDetailsFragment() {
		FragmentManager mFragmentManager = getFragmentManager();
		FragmentTransaction fragmentTransaction = mFragmentManager
				.beginTransaction();
		fragmentTransaction.setCustomAnimations(R.anim.slide_left_enter,
				R.anim.slide_left_exit, R.anim.slide_right_enter,
				R.anim.slide_right_exit);

		MembershipDetailsFragment membershipDetailsFragment = new MembershipDetailsFragment();
		fragmentTransaction.replace(R.id.main_fragmant_container,
				membershipDetailsFragment);
		fragmentTransaction.addToBackStack(null);
		fragmentTransaction.commit();
	}

	public void showLoadingDialog(String message) {
		if (!getActivity().isFinishing()) {
			if (mProgressDialog == null) {
				mProgressDialog = new MyProgressDialog(getActivity());
			}
		}
	}

	public void hideLoadingDialog() {
		if (mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	@Override
	public void onDestroyView() {
//		System.out.println(" ::::::::: onDestroyView ::::::::::: ");
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	private void setUserDetail(SubscriptionUser user){
//		TextView txtUserName = (TextView) rootView.findViewById(R.id.txt_user_name);
//		final ImageView ivProfilePic = (ImageView) rootView.findViewById(R.id.iv_profile_pic);
		if (mApplicationConfigurations.isuserLoggedIn() || Logger.allowPlanForSilentUser) {
			if (!TextUtils.isEmpty(user.getFirstname())
					|| !TextUtils.isEmpty(user.getLastname())) {
				txtUserName.setText(user.getFirstname()
						+ " "
						+ user.getLastname());
			} else if (!mApplicationConfigurations.getGigyaFBFirstName().equals("")
					|| !mApplicationConfigurations.getGigyaFBLastName().equals(
					"")) {
				txtUserName.setText(mApplicationConfigurations
						.getGigyaFBFirstName()
						+ " "
						+ (mApplicationConfigurations.getGigyaFBLastName()));
			} else if (!mApplicationConfigurations.getGigyaGoogleFirstName()
					.equals("")
					|| !mApplicationConfigurations.getGigyaGoogleLastName()
					.equals("")) {
				txtUserName
						.setText(mApplicationConfigurations
								.getGigyaGoogleFirstName()
								+ " "
								+ (mApplicationConfigurations
								.getGigyaGoogleLastName()));
			} else if (!mApplicationConfigurations.getGigyaTwitterFirstName()
					.equals("")
					|| !mApplicationConfigurations.getGigyaTwitterLastName()
					.equals("")) {
				txtUserName
						.setText(mApplicationConfigurations
								.getGigyaTwitterFirstName()
								+ " "
								+ (mApplicationConfigurations
								.getGigyaTwitterLastName()));
			} else if (!mApplicationConfigurations.getHungmaFirstName().equals(
					"")
					|| !mApplicationConfigurations.getHungamaLastName().equals(
					"")) {
				txtUserName.setText(mApplicationConfigurations
						.getHungmaFirstName()
						+ " "
						+ (mApplicationConfigurations.getHungamaLastName()));
			} else {
				txtUserName.setText(Utils.getMultilanguageTextLayOut(
						getActivity(),
						getResources().getString(
								R.string.upgrade_benefit_default_user_name)));
			}

			if (!TextUtils.isEmpty(user.getProfileImage())) {
				PicassoUtil picasso = PicassoUtil.with(getActivity());
				picasso.loadWithFit(
						new PicassoUtil.PicassoCallBack() {
							@Override
							public void onSuccess() {
								try {
									BitmapDrawable dra = (BitmapDrawable) ivProfilePic
											.getDrawable();
									Bitmap bmp = dra.getBitmap();
									if (bmp.getWidth() < bmp.getHeight())
										bmp = Bitmap.createBitmap(bmp, 0,
												(bmp.getHeight() - bmp
														.getWidth()) / 2, bmp
														.getWidth(), bmp
														.getWidth());
									else if (bmp.getWidth() > bmp.getHeight())
										bmp = Bitmap.createBitmap(bmp,
												(bmp.getWidth() - bmp
														.getHeight()) / 2, 0,
												bmp.getHeight(), bmp
														.getHeight());
									ivProfilePic.setImageBitmap(GlobalMenuFragment.getRoundedShape(bmp,
											getActivity()));
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
							}

							@Override
							public void onError() {
							}
						}, user.getProfileImage(),
						ivProfilePic,
						R.drawable.user_icon);
			} else if (!TextUtils.isEmpty(mApplicationConfigurations
					.getGiGyaFBThumbUrl())) {
				PicassoUtil picasso = PicassoUtil.with(getActivity());
				picasso.load(
						new PicassoUtil.PicassoCallBack() {
							@Override
							public void onSuccess() {
								try {
									BitmapDrawable dra = (BitmapDrawable) ivProfilePic
											.getDrawable();
									Bitmap bmp = dra.getBitmap();
									if (bmp.getWidth() < bmp.getHeight())
										bmp = Bitmap.createBitmap(bmp, 0,
												(bmp.getHeight() - bmp
														.getWidth()) / 2, bmp
														.getWidth(), bmp
														.getWidth());
									else if (bmp.getWidth() > bmp.getHeight())
										bmp = Bitmap.createBitmap(bmp,
												(bmp.getWidth() - bmp
														.getHeight()) / 2, 0,
												bmp.getHeight(), bmp
														.getHeight());
									ivProfilePic.setImageBitmap(GlobalMenuFragment.getRoundedShape(bmp,
											getActivity()));
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
							}

							@Override
							public void onError() {
							}
						}, mApplicationConfigurations.getGiGyaFBThumbUrl(),
						ivProfilePic,
						R.drawable.user_icon);
			} else if (!TextUtils.isEmpty(mApplicationConfigurations
					.getGiGyaTwitterThumbUrl())) {
				PicassoUtil picasso = PicassoUtil.with(getActivity());
				picasso.load(
						new PicassoUtil.PicassoCallBack() {
							@Override
							public void onSuccess() {
								try {
									BitmapDrawable dra = (BitmapDrawable) ivProfilePic
											.getDrawable();
									Bitmap bmp = dra.getBitmap();
									if (bmp.getWidth() < bmp.getHeight())
										bmp = Bitmap.createBitmap(bmp, 0,
												(bmp.getHeight() - bmp
														.getWidth()) / 2, bmp
														.getWidth(), bmp
														.getWidth());
									else if (bmp.getWidth() > bmp.getHeight())
										bmp = Bitmap.createBitmap(bmp,
												(bmp.getWidth() - bmp
														.getHeight()) / 2, 0,
												bmp.getHeight(), bmp
														.getHeight());
									ivProfilePic.setImageBitmap(GlobalMenuFragment.getRoundedShape(bmp,
											getActivity()));
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
							}

							@Override
							public void onError() {
							}
						},
						mApplicationConfigurations.getGiGyaTwitterThumbUrl(),
						ivProfilePic, R.drawable.user_icon);
			} else {
				ivProfilePic.setImageBitmap(null);
				ivProfilePic.setBackgroundResource(R.drawable.user_icon);
			}
		} else {
			ivProfilePic.setImageBitmap(null);
			ivProfilePic.setBackgroundResource(R.drawable.user_icon);

			txtUserName.setText(Utils.getMultilanguageTextLayOut(
					getActivity(),
					getResources().getString(
							R.string.upgrade_benefit_default_user_name)));
		}
	}
}
