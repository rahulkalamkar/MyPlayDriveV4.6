package com.hungama.myplay.activity.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.communication.CommunicationManager.ErrorType;
import com.hungama.myplay.activity.communication.CommunicationOperationListener;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.audiocaching.CacheManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.dao.hungama.LeftMenuExtraData;
import com.hungama.myplay.activity.data.dao.hungama.LeftMenuItem;
import com.hungama.myplay.activity.data.dao.hungama.LeftMenuResponse;
import com.hungama.myplay.activity.data.dao.hungama.SubscriptionStatusResponse;
import com.hungama.myplay.activity.operations.OperationDefinition;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.ui.HomeActivity;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.ui.UpgradeActivity;
import com.hungama.myplay.activity.ui.widgets.CustomAlertDialog;
import com.hungama.myplay.activity.ui.widgets.LanguageButton;
import com.hungama.myplay.activity.ui.widgets.LanguageTextView;
import com.hungama.myplay.activity.util.Analytics;
import com.hungama.myplay.activity.util.FlurryConstants;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.PicassoUtil;
import com.hungama.myplay.activity.util.PicassoUtil.PicassoCallBack;
import com.hungama.myplay.activity.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//onResume() ::::::::::::
/**
 * Presents the "Global Menu" expandable list in the ActionBar
 */
public class GlobalMenuFragment extends Fragment implements
		ExpandableListView.OnChildClickListener,
		ExpandableListView.OnGroupClickListener,
		CommunicationOperationListener, OnItemClickListener {

	private static final String TAG = "GlobalMenuFragment";

	private Context mContext;
	private Resources mResources;
	private LayoutInflater mLayoutInflater;

	private ExpandableListView mExpandableListView;
	private ListView mListView;
	private SettingsAdapter mSettingsAdapter;
	private SettingsAdapterList mSettingsAdapterList;

	private List<Category> mCategories;
	private ApplicationConfigurations mApplicationConfigurations;
	private OnGlobalMenuItemSelectedListener mOnGlobalMenuItemSelectedListener;
	// Data members
	private String mSubscriptionPlan;
	private boolean mHasSubscriptionPlan;

//	private LanguageTextView textUpgradeTitle, textUpgradeTitlePro;
	private LanguageTextView /*textUpgradeMessage, */textUpgradeMessagePro;
	private LanguageButton buttonUpgrade;
	private Category categoryLogout;
	private boolean isLogoutMenuAdded;
	// RelativeLayout ll_sign_in;
	LinearLayout /*ll_status, */ll_status_pro;

	private LinearLayout ll_mdTextRootInner;
	private LeftMenuResponse mLeftMenuResponse;

	// ======================================================
	// FRAGMENTS LIFECYCLE AND PRIVATE HELPER METHODS.
	// ======================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mContext = getActivity().getApplicationContext();
		mResources = mContext.getResources();
		mLayoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		isLogoutMenuAdded = false;
		// creates all the menu items and their categories.

		mDataManager = DataManager.getInstance(getActivity()
				.getApplicationContext());
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		mHasSubscriptionPlan = mApplicationConfigurations
				.isUserHasSubscriptionPlan();

		String response = new com.hungama.myplay.activity.data.CacheManager(
				getActivity()).getLeftMenuResponse();

		try {
			mLeftMenuResponse = new Gson().fromJson(response,
					LeftMenuResponse.class);
		} catch (Exception e) {
			mLeftMenuResponse = null;
		}

		picasso = PicassoUtil.with(mContext);

		userId = mApplicationConfigurations.getPartnerUserId();

		if (mLeftMenuResponse == null)
			mDataManager.getLeftMenu(getActivity(), GlobalMenuFragment.this,
					null);
		else
			createSettings();
		setOnGlobalMenuItemSelectedListener((MainActivity)getActivity());
		Analytics.postCrashlitycsLog(getActivity(), GlobalMenuFragment.class.getName());
	}

	View rootView;

	private LanguageTextView mdTextPrimary;
	private ImageView mdImage;
	private LanguageTextView mdTextSecondary;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// if (rootView == null) {
		rootView = inflater.inflate(R.layout.fragment_main_global_menu,
				container, false);
		// catches the click on any other view except the list.
		rootView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});

		buttonUpgrade = (LanguageButton) rootView
				.findViewById(R.id.global_menu_button_upgrade);
		buttonUpgrade.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				if (isHandledActionOffline()) {
					return;
				}

				Boolean loggedIn = mApplicationConfigurations.isRealUser();
				Map<String, String> reportMap = new HashMap<String, String>();
				reportMap.put(FlurryConstants.FlurrySubscription.SourcePage
								.toString(),
						FlurryConstants.FlurrySubscription.LeftMenu
								.toString());
				reportMap.put(
						FlurryConstants.FlurrySubscription.LoggedIn.toString(),
						loggedIn.toString());
				Analytics.logEvent(
						FlurryConstants.FlurrySubscription.TapsOnUpgrade
								.toString(), reportMap);

				// if (mApplicationConfigurations.isuserLoggedIn()) {
				Intent intent = new Intent(getActivity(), UpgradeActivity.class);
				intent.putExtra(UpgradeActivity.IS_TRIAL_PLANS, true);
				intent.putExtra(UpgradeActivity.EXTRA_IS_GO_OFFLINE, false);
				intent.putExtra(
						UpgradeActivity.EXTRA_IS_FROM_NO_INTERNET_PROMT, false);
				getActivity().startActivityForResult(intent,
						HomeActivity.UPGRADE_ACTIVITY_RESULT_CODE);
				// } else {
				// Intent startLoginActivityIntent = new Intent(getActivity(),
				// LoginActivity.class);
				// startLoginActivityIntent.putExtra(
				// UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY,
				// "show_upgrade_activity");
				// startLoginActivityIntent
				// .putExtra(LoginActivity.FLURRY_SOURCE,
				// FlurryConstants.FlurryUserStatus.Upgrade
				// .toString());
				// getActivity().startActivityForResult(
				// startLoginActivityIntent,
				// UpgradeActivity.LOGIN_ACTIVITY_CODE);
				// }
			}
		});
//		textUpgradeTitle = (LanguageTextView) rootView
//				.findViewById(R.id.global_menu_text_upgrade_title);
//		textUpgradeMessage = (LanguageTextView) rootView
//				.findViewById(R.id.global_menu_text_upgrade_message);

		mdImage = (ImageView) rootView.findViewById(R.id.mdImage);
		mdTextPrimary = (LanguageTextView) rootView
				.findViewById(R.id.mdTextPrimary);
		mdTextSecondary = (LanguageTextView) rootView
				.findViewById(R.id.mdTextSecondary);

//		ll_status = (LinearLayout) rootView.findViewById(R.id.ll_status);

		ll_status_pro = (LinearLayout) rootView
				.findViewById(R.id.ll_status_pro);
		ll_status_pro.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (mOnGlobalMenuItemSelectedListener != null) 
					mOnGlobalMenuItemSelectedListener.onGlobalMenuItemSelected(
							null, MENU_ITEM_SUBSCRIPTION_STATUS_ACTION);
			}
		});
//		textUpgradeTitlePro = (LanguageTextView) rootView
//				.findViewById(R.id.global_menu_text_upgrade_title_pro);
		textUpgradeMessagePro = (LanguageTextView) rootView
				.findViewById(R.id.global_menu_text_upgrade_message_pro);

		mListView = (ListView) rootView
				.findViewById(R.id.main_settings_listview);
		mExpandableListView = (ExpandableListView) rootView
				.findViewById(R.id.main_settings_expandablelistview);

		if (!mApplicationConfigurations.isuserLoggedIn()) {
			mListView.setVisibility(View.GONE);
			mExpandableListView.setVisibility(View.VISIBLE);
			// rootView.findViewById(R.id.main_setting_category_expand_indicator)
			// .setVisibility(View.GONE);
		} else {

			rootView.findViewById(R.id.main_setting_category_expand_indicator)
					.setVisibility(View.VISIBLE);
		}

		rootView.findViewById(R.id.main_setting_category_expand_indicator)
				.setOnClickListener(onclick);

		ll_mdTextRootInner = (LinearLayout) rootView
				.findViewById(R.id.ll_mdTextRootInner);
		ll_mdTextRootInner.setOnClickListener(onclick);

		setUpAdapter();
		// } else {
		// ViewGroup parent = (ViewGroup) Utils.getParentView(rootView);
		// parent.removeView(rootView);
		// }

		// if(mSettingsAdapter==null){
		// View rootView = getView();

		// }else
		// mSettingsAdapter.notifyDataSetChanged();

		return rootView;
	}

	public boolean isHandledActionOffline() {
		if (mApplicationConfigurations.getSaveOfflineMode()) {
			CustomAlertDialog alertBuilder = new CustomAlertDialog(getActivity());
			alertBuilder.setMessage(Utils.getMultilanguageText(
					mContext,
					getResources().getString(
							R.string.caching_text_message_go_online_player)));
			alertBuilder.setPositiveButton(Utils.getMultilanguageText(
							mContext,
							getResources().getString(
									R.string.caching_text_popup_title_go_online)),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (Utils.isConnected()) {
								mApplicationConfigurations
										.setSaveOfflineMode(false);

								Map<String, String> reportMap = new HashMap<String, String>();
								reportMap.put(
										FlurryConstants.FlurryCaching.Source
												.toString(),
										FlurryConstants.FlurryCaching.Prompt
												.toString());
								reportMap
										.put(FlurryConstants.FlurryCaching.UserStatus
												.toString(), Utils
												.getUserState(getActivity()));
								Analytics.logEvent(
										FlurryConstants.FlurryCaching.GoOnline
												.toString(), reportMap);

								Intent i = new Intent(
										MainActivity.ACTION_OFFLINE_MODE_CHANGED);
								i.putExtra(
										MainActivity.SELECTED_GLOBAL_MENU_ID,
										GlobalMenuFragment.MENU_ITEM_UPGRADE_ACTION);
								mContext.sendBroadcast(i);
							} else {
								CustomAlertDialog alertBuilder = new CustomAlertDialog(
										getActivity());
								alertBuilder.setMessage(Utils
										.getMultilanguageText(
												mContext,
												getResources()
														.getString(
																R.string.go_online_network_error)));
								alertBuilder.setNegativeButton(Utils
												.getMultilanguageText(mContext, "OK"),
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												startActivity(new Intent(
														android.provider.Settings.ACTION_SETTINGS));
											}
										});
								alertBuilder.show();
							}
						}
					});
			alertBuilder.setNegativeButton(Utils.getMultilanguageText(
					mContext,
					getResources().getString(
							R.string.caching_text_popup_button_cancel)), null);
			alertBuilder.show();
			return true;
		} else {
			return false;
		}
	}

	OnClickListener onclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.ll_mdTextRootInner) {
				if (mApplicationConfigurations.isuserLoggedIn()) {
					if (mListView.getVisibility() == View.GONE) {
						mListView.setVisibility(View.VISIBLE);
						mListView.startAnimation(AnimationUtils.loadAnimation(
								getActivity(), R.anim.abc_fade_in));
						mExpandableListView.setVisibility(View.GONE);
						ImageView img = (ImageView) rootView
								.findViewById(R.id.main_setting_category_expand_indicator);
						img.setImageResource(R.drawable.left_panel_arrow_up);
					} else {
						ImageView img = (ImageView) rootView
								.findViewById(R.id.main_setting_category_expand_indicator);
						img.setImageResource(R.drawable.left_panel_arrow_down);
						mListView.startAnimation(AnimationUtils.loadAnimation(
								getActivity(), R.anim.abc_fade_out));
						mListView.setVisibility(View.GONE);
						mExpandableListView.setVisibility(View.VISIBLE);
					}
				} else {
					// ImageView img = (ImageView) rootView
					// .findViewById(R.id.main_setting_category_expand_indicator);
					// img.setImageResource(R.drawable.left_panel_arrow_down);
//					Intent startLoginActivityIntent = new Intent(getActivity(),
//							LoginActivity.class);
//					startLoginActivityIntent.putExtra(
//							UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY,
//							"upgrade_activity");
//					startLoginActivityIntent.putExtra(
//							LoginActivity.FLURRY_SOURCE,
//							FlurryConstants.FlurryUserStatus.GlobleMenu
//									.toString());
//					getActivity().startActivityForResult(
//							startLoginActivityIntent,
//							MainActivity.LOGIN_ACTIVITY_CODE);

                    if (mOnGlobalMenuItemSelectedListener != null)
                        mOnGlobalMenuItemSelectedListener.onGlobalMenuItemSelected(
                                null, MENU_ITEM_MY_PROFILE_ACTION);
				}
			}
			if (v.getId() == R.id.main_setting_category_expand_indicator) {
				if (mListView.getVisibility() == View.GONE) {
					mListView.setVisibility(View.VISIBLE);
					mListView.startAnimation(AnimationUtils.loadAnimation(
							getActivity(), R.anim.abc_fade_in));
					mExpandableListView.setVisibility(View.GONE);
					ImageView img = (ImageView) rootView
							.findViewById(R.id.main_setting_category_expand_indicator);
					img.setImageResource(R.drawable.left_panel_arrow_up);
				} else {
					ImageView img = (ImageView) rootView
							.findViewById(R.id.main_setting_category_expand_indicator);
					img.setImageResource(R.drawable.left_panel_arrow_down);
					mListView.startAnimation(AnimationUtils.loadAnimation(
							getActivity(), R.anim.abc_fade_out));
					mListView.setVisibility(View.GONE);
					mExpandableListView.setVisibility(View.VISIBLE);
				}
			}
		}
	};

	private void setUpAdapter() {

		if (mCategories != null && mCategories.size() > 0) {
			if (myPlayItems == null)
				myPlayItems = new ArrayList<GlobalMenuFragment.MenuItem>();
		}

		mExpandableListView
				.setChildDivider(mResources
						.getDrawable(R.drawable.main_actionbar_settings_menu_item_separator));
		mExpandableListView
				.setDivider(mResources
						.getDrawable(R.drawable.main_actionbar_settings_menu_item_separator));
		mExpandableListView.setDividerHeight(1);
		mExpandableListView.setOnChildClickListener(this);
		mExpandableListView.setOnGroupClickListener(this);
		mExpandableListView
				.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
		mSettingsAdapter = new SettingsAdapter();

		mExpandableListView.setAdapter(mSettingsAdapter);

		mListView
				.setDivider(mResources
						.getDrawable(R.drawable.main_actionbar_settings_menu_item_separator));
		mListView.setDividerHeight(1);
		mSettingsAdapterList = new SettingsAdapterList();
		mListView.setAdapter(mSettingsAdapterList);
		mListView.setOnItemClickListener(this);
	}

	private DataManager mDataManager;
	private String userId = "";

	@Override
	public void onResume() {
		super.onResume();

//		ll_status.setVisibility(View.VISIBLE);
//		ll_status_pro.setVisibility(View.GONE);
		if (mApplicationConfigurations.isuserLoggedIn()) {
			mdTextSecondary.setVisibility(View.VISIBLE);
			if (!mApplicationConfigurations.getGigyaFBFirstName().equals("")
					|| !mApplicationConfigurations.getGigyaFBLastName().equals(
							"")) {
				mdTextPrimary.setText(mApplicationConfigurations
						.getGigyaFBFirstName()
						+ " "
						+ (mApplicationConfigurations.getGigyaFBLastName()));
				mdTextSecondary.setText(mApplicationConfigurations
						.getHungamaEmail());
			} else if (!mApplicationConfigurations.getGigyaGoogleFirstName()
					.equals("")
					|| !mApplicationConfigurations.getGigyaGoogleLastName()
							.equals("")) {
				mdTextPrimary
						.setText(mApplicationConfigurations
								.getGigyaGoogleFirstName()
								+ " "
								+ (mApplicationConfigurations
										.getGigyaGoogleLastName()));
				mdTextSecondary.setText(mApplicationConfigurations
						.getHungamaEmail());
			} else if (!mApplicationConfigurations.getGigyaTwitterFirstName()
					.equals("")
					|| !mApplicationConfigurations.getGigyaTwitterLastName()
							.equals("")) {
				mdTextPrimary
						.setText(mApplicationConfigurations
								.getGigyaTwitterFirstName()
								+ " "
								+ (mApplicationConfigurations
										.getGigyaTwitterLastName()));
				mdTextSecondary.setText(mApplicationConfigurations
						.getHungamaEmail());
			} else if (!mApplicationConfigurations.getHungmaFirstName().equals(
					"")
					|| !mApplicationConfigurations.getHungamaLastName().equals(
							"")) {
				mdTextPrimary.setText(mApplicationConfigurations
						.getHungmaFirstName()
						+ " "
						+ (mApplicationConfigurations.getHungamaLastName()));
				mdTextSecondary.setText(mApplicationConfigurations
						.getHungamaEmail());
			} else {
				mdTextPrimary.setText(mApplicationConfigurations
						.getHungamaEmail());
				mdTextSecondary.setVisibility(View.GONE);
			}

			if (!TextUtils.isEmpty(mApplicationConfigurations
					.getGiGyaFBThumbUrl())) {
				picasso.load(
						new PicassoCallBack() {
							@Override
							public void onSuccess() {
								try {
									BitmapDrawable dra = (BitmapDrawable) mdImage
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
									mdImage.setImageBitmap(getRoundedShape(bmp,
											getActivity()));
								} catch (Exception e) {
									Logger.printStackTrace(e);
								}
							}

							@Override
							public void onError() {
							}
						}, mApplicationConfigurations.getGiGyaFBThumbUrl(),
						mdImage,
						R.drawable.user_icon);
			} else if (!TextUtils.isEmpty(mApplicationConfigurations
					.getGiGyaTwitterThumbUrl())) {
				picasso.load(
						new PicassoCallBack() {
							@Override
							public void onSuccess() {
								try {
									BitmapDrawable dra = (BitmapDrawable) mdImage
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
									mdImage.setImageBitmap(getRoundedShape(bmp,
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
						mdImage, R.drawable.user_icon);
			} else {
				mdImage.setImageBitmap(null);
				mdImage.setBackgroundResource(R.drawable.user_icon);
			}
		} else {
			mdImage.setImageBitmap(null);
			mdImage.setBackgroundResource(R.drawable.user_icon);

			mdTextPrimary.setText(Utils.getMultilanguageTextLayOut(
					getActivity(),
					getResources().getString(
							R.string.global_menu_button_sign_in1)));
			mdTextSecondary.setText(Utils.getMultilanguageTextLayOut(
					getActivity(),
					getResources().getString(
							R.string.global_menu_button_sign_in2)));

			String sesion = mApplicationConfigurations
					.getSessionID();
			boolean isRealUser = mApplicationConfigurations
					.isRealUser();
			if (!TextUtils.isEmpty(sesion) && isRealUser) {
			}else{
				if (!mApplicationConfigurations.getSaveOfflineMode()) {
					if(mSettingsAdapter!=null)
						mSettingsAdapter.notifyDataSetChanged();
//					((Switch) buttonView).setChecked(false);
				}
			}
		}

//		if(!mApplicationConfigurations.getPartnerUserId().equals(userId)) {
//			userId = mApplicationConfigurations.getPartnerUserId();
			if (mApplicationConfigurations.isuserLoggedIn() || Logger.allowPlanForSilentUser) {

				// rootView.findViewById(R.id.main_setting_category_expand_indicator)
				// .setVisibility(View.VISIBLE);

				// ll_sign_in.setVisibility(View.GONE);
				buttonUpgrade.setText(Utils.getMultilanguageTextLayOut(
						getActivity(),
						getActivity().getString(
								R.string.global_menu_button_upgrade_to_pro)));
				if (CacheManager.isProUser(getActivity())) {
//					ll_status.setVisibility(View.GONE);
//					ll_status_pro.setVisibility(View.VISIBLE);
//					ll_status_pro.setOrientation(LinearLayout.HORIZONTAL);
//					textUpgradeTitlePro.setPadding(0, 0, 8, 0);
					buttonUpgrade.setVisibility(View.GONE);
					String title = Utils
							.getMultilanguageTextLayOut(
									getActivity(),
									getActivity()
											.getString(
													R.string.settings_membershipstatus_title_english));

//					textUpgradeTitlePro.setText(title);
//					textUpgradeTitlePro.setVisibility(View.GONE);
				} else if (CacheManager.isTrialUser(getActivity())) {
					buttonUpgrade.setVisibility(View.GONE);
//					String title = Utils
//							.getMultilanguageTextLayOut(
//									getActivity(),
//									getActivity()
//											.getString(
//													R.string.settings_membershipstatus_title_english));
//					textUpgradeTitle.setText(title);
//					textUpgradeTitle.setVisibility(View.GONE);
				} else {
					buttonUpgrade.setVisibility(View.VISIBLE);
//					String title = Utils
//							.getMultilanguageTextLayOut(
//									getActivity(),
//									getActivity()
//											.getString(
//													R.string.settings_membershipstatus_title_english));
//					textUpgradeTitle.setText(title);
//					textUpgradeTitle.setVisibility(View.GONE);
				}
				setMembershipText();
				// if (!isLogoutMenuAdded) {
				// mCategories.add(categoryLogout);
				// isLogoutMenuAdded = true;
				// }
//			if (mSettingsAdapter != null)
//				mSettingsAdapter.notifyDataSetChanged();
//			if (mSettingsAdapterList != null)
//				mSettingsAdapterList.notifyDataSetChanged();
//				mDataManager.getLeftMenu(getActivity(), GlobalMenuFragment.this,
//						null);
			} else {

				// mListView.setVisibility(View.GONE);
				// mExpandableListView.setVisibility(View.VISIBLE);

				// rootView.findViewById(R.id.main_setting_category_expand_indicator)
				// .setVisibility(View.GONE);
				// ll_sign_in.setVisibility(View.VISIBLE);
				buttonUpgrade.setText(Utils.getMultilanguageTextLayOut(
						getActivity(),
						getActivity().getString(
								R.string.global_menu_button_upgrade_now)));
				buttonUpgrade.setVisibility(View.VISIBLE);
//				String title = Utils.getMultilanguageTextLayOut(
//						getActivity(),
//						getActivity().getString(
//								R.string.global_menu_button_upgrade_title));
//				textUpgradeTitle.setText(title);
//				textUpgradeTitle.setVisibility(View.VISIBLE);
//				textUpgradeMessage.setText(Utils.getMultilanguageTextLayOut(
//						getActivity(),
//						getActivity().getString(
//								R.string.global_menu_button_upgrade_message)));
				mSubscriptionPlan = Utils.getMultilanguageTextLayOut(
						getActivity(),
						getResources().getString(R.string.txt_you_are_free_user));

				textUpgradeMessagePro.setText(mSubscriptionPlan);
//					textUpgradeMessage.setVisibility(View.GONE);
				textUpgradeMessagePro.setVisibility(View.VISIBLE);

				if (mCategories != null && mCategories.size() > 0)
					mCategories.remove(categoryLogout);
				isLogoutMenuAdded = false;
//			if (mSettingsAdapter != null)
//				mSettingsAdapter.notifyDataSetChanged();
//			if (mSettingsAdapterList != null)
//				mSettingsAdapterList.notifyDataSetChanged();
//				mDataManager.getLeftMenu(getActivity(), GlobalMenuFragment.this,
//						null);
			}
//		}
		if(!mApplicationConfigurations.getPartnerUserId().equals(userId)) {
			userId = mApplicationConfigurations.getPartnerUserId();
			mDataManager.getLeftMenu(getActivity(), GlobalMenuFragment.this,
					null);
		}

		if ((mHasSubscriptionPlan
				&& !mApplicationConfigurations.isUserHasSubscriptionPlan()) && (mApplicationConfigurations.isRealUser()
				|| Logger.allowPlanForSilentUser)) {
			String accountType = Utils.getAccountName(getActivity());
			mDataManager.getCurrentSubscriptionPlan(this, accountType);
		}
	}

	private void setMembershipText() {
		try {
			mHasSubscriptionPlan = mApplicationConfigurations
					.isUserHasSubscriptionPlan();

			if (getActivity() != null) {
				if (mHasSubscriptionPlan) {
					if (mApplicationConfigurations
							.isUserHasTrialSubscriptionPlan()) {
						mSubscriptionPlan = Utils.getMultilanguageTextLayOut(
								getActivity(),
								getActivity().getString(
										R.string.left_menu_premium_membership));
						textUpgradeMessagePro.setText(" " + mSubscriptionPlan);
//						textUpgradeMessage.setText(mSubscriptionPlan);
//						textUpgradeMessage.setVisibility(View.VISIBLE);
						textUpgradeMessagePro.setVisibility(View.VISIBLE);
					} else {
						mSubscriptionPlan = Utils.getMultilanguageTextLayOut(
								getActivity(),
								getActivity().getString(
										R.string.left_menu_premium_membership));
						textUpgradeMessagePro.setText(" " + mSubscriptionPlan);
						// textUpgradeMessage.setTypeface(Typeface.DEFAULT_BOLD);
						// textUpgradeMessage.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(R.dimen.xlarge_text_size));
						// textUpgradeMessage.setTextSize(getResources().getDimensionPixelSize(R.dimen.xlarge_text_size));
//						textUpgradeMessage.setVisibility(View.GONE);
						textUpgradeMessagePro.setVisibility(View.VISIBLE);
					}
				} else {
					mSubscriptionPlan = Utils.getMultilanguageTextLayOut(
							getActivity(),
							getResources().getString(R.string.txt_you_are_free_user));

					textUpgradeMessagePro.setText(mSubscriptionPlan);
//					textUpgradeMessage.setVisibility(View.GONE);
					textUpgradeMessagePro.setVisibility(View.VISIBLE);
				}
				// Add language specific text

			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	List<MenuItem> myPlayItems;

	private void createSettings() {
		mCategories = new ArrayList<GlobalMenuFragment.Category>();
		if (mLeftMenuResponse != null) {
			List<LeftMenuItem> menu = mLeftMenuResponse.getLeftMenuItems();
			for (int i = 1; i < menu.size(); i++) {
				LeftMenuItem temp_meu = menu.get(i);
				String file_path = "";

				if (temp_meu.getImages() != null)
					if (mDataManager.getDisplayDensity().equals("ldpi")) {
						file_path = temp_meu.getImages().getLdpi();
					} else if (mDataManager.getDisplayDensity().equals("mdpi")) {
						file_path = temp_meu.getImages().getMdpi();
					} else if (mDataManager.getDisplayDensity().equals("hdpi")) {
						file_path = temp_meu.getImages().getHdpi();
					} else if (mDataManager.getDisplayDensity().equals("xdpi")) {
						file_path = temp_meu.getImages().getXdpi();
					}

				List<LeftMenuItem> sub_menu = temp_meu.getSubMenu();
				List<MenuItem> offlineItems = new ArrayList<GlobalMenuFragment.MenuItem>();
				if (sub_menu != null && sub_menu.size() > 0) {
					offlineItems = createSubmenu(sub_menu);
				}

				if (temp_meu.getMainMenu() != null
						&& !temp_meu.getMainMenu().equals(""))
					mCategories.add(new Category(temp_meu.getMainMenu(),
							offlineItems, file_path,
							temp_meu.getInapp_action(),
							temp_meu.getLink_type(), temp_meu.getHtmlURL(),
							temp_meu.getPopUpMessage(), temp_meu.getExtra_data()));
				else if (temp_meu.getMenu_title() != null
						&& !temp_meu.getMenu_title().equals(""))
					mCategories.add(new Category(temp_meu.getMenu_title(),
							offlineItems, file_path,
							temp_meu.getInapp_action(),
							temp_meu.getLink_type(), temp_meu.getHtmlURL(),
							temp_meu.getPopUpMessage(), temp_meu.getExtra_data()));
			}

			// List<MenuItem> moreItems = createMoreSettings();
			//
			// List<MenuItem> languageSettingsItems =
			// createLanguageSettingsItems();
			// List<MenuItem> itemsSettingItems = createItemsSettingItems();
			// List<MenuItem> itemsRewards = createItemsLogout();
			//
			// List<MenuItem> itemsSettingOptions = createItemsSettingOptions();
			myPlayItems = new ArrayList<GlobalMenuFragment.MenuItem>();
			// if (mApplicationConfigurations.isuserLoggedIn()) {
			// mCategories.add(new Category(CATEGORY_MY_PLAY,
			// R.string.main_actionbar_settings_category_signin_options,
			// myPlayItems, R.drawable.icon_main_user));
			LeftMenuItem temp_meu = menu.get(0);

			List<LeftMenuItem> sub_menu = temp_meu.getSubMenu();
			if (sub_menu != null && sub_menu.size() > 0) {
				myPlayItems = createSubmenu(sub_menu);
			}
		}
		// mCategories.add(new Category(MENU_ITEM_FREE_RECHARGE,
		// R.string.main_actionbar_settings_menu_item_free_recharge,
		// offlineItems, R.drawable.free_recharge));
		//
		// mCategories.add(new Category(MENU_ITEM_REEDEEM_COUPON,
		// R.string.main_actionbar_settings_menu_item_redeem_coupon,
		// offlineItems, R.drawable.redeem_coupon));
		//
		// mCategories.add(new Category(MENU_ITEM_LANGUAGE_SETTING,
		// R.string.main_actionbar_settings_menu_item_language_settings,
		// languageSettingsItems,
		// R.drawable.icon_main_settings_my_preferences));
		// mCategories.add(new Category(MENU_ITEM_SUBSCRIPTION_PLAN,
		// R.string.main_actionbar_settings_menu_item_subscription_plan,
		// itemsSettingItems, R.drawable.icon_main_subscription_plan));
		//
		// mCategories.add(new Category(MENU_ITEM_REWARDS,
		// R.string.main_actionbar_settings_menu_item_rewards,
		// itemsRewards, R.drawable.icon_main_settings_rewards));
		//
		// mCategories
		// .add(new Category(
		// MENU_ITEM_SETTINGS_AND_ACCOUNTS,
		// R.string.main_actionbar_settings_menu_item_settings_and_accounts,
		// itemsSettingOptions,
		// R.drawable.icon_main_settings_settings_and_accounts));
		//
		// mCategories.add(new Category(CATEGORY_MORE,
		// R.string.main_actionbar_settings_category_others, moreItems,
		// R.drawable.abc_ic_menu_moreoverflow_hor));

		if (mApplicationConfigurations.isuserLoggedIn()) {
			// mCategories.add(categoryLogout);
			isLogoutMenuAdded = true;
		}
	}

	public void collepseGroups() {
		createSettings();
		if (mExpandableListView != null)
			mExpandableListView.collapseGroup(0);
	}

	// private List<MenuItem> createQuickLinksSettings() {
	// List<MenuItem> quickLinksItems = new
	// ArrayList<GlobalMenuFragment.MenuItem>();
	//
	// // - Music
	// // - Videos
	// // - Radio
	// // - Discover
	// // - Offline Music
	// // - Settings
	//
	// quickLinksItems.add(new MenuItem(MENU_ITEM_MUSIC,
	// R.string.main_actionbar_settings_menu_item_music,
	// R.drawable.icon_main_settings_music));
	// quickLinksItems.add(new MenuItem(MENU_ITEM_VIDEOS,
	// R.string.main_actionbar_settings_menu_item_videos,
	// R.drawable.icon_main_settings_videos));
	// quickLinksItems.add(new MenuItem(MENU_ITEM_LIVE_RADIO,
	// R.string.main_actionbar_settings_menu_item_live_radio,
	// R.drawable.icon_main_settings_live_radio));
	// quickLinksItems.add(new MenuItem(MENU_ITEM_DISCOVER,
	// R.string.main_actionbar_settings_menu_item_discover,
	// R.drawable.icon_main_settings_discover));
	//
	// return quickLinksItems;
	// }

	private List<MenuItem> createSubmenu(List<LeftMenuItem> sub_menu) {
		List<MenuItem> myPlayItems = new ArrayList<GlobalMenuFragment.MenuItem>();
		for (int j = 0; j < sub_menu.size(); j++) {
			LeftMenuItem temp_meu = sub_menu.get(j);

			String file_path = "";
			if (temp_meu.getImages() != null)
				if (mDataManager.getDisplayDensity().equals("ldpi")) {
					file_path = temp_meu.getImages().getLdpi();
				} else if (mDataManager.getDisplayDensity().equals("mdpi")) {
					file_path = temp_meu.getImages().getMdpi();
				} else if (mDataManager.getDisplayDensity().equals("hdpi")) {
					file_path = temp_meu.getImages().getHdpi();
				} else if (mDataManager.getDisplayDensity().equals("xdpi")) {
					file_path = temp_meu.getImages().getXdpi();
				}

			if (temp_meu.getMainMenu() != null
					&& !temp_meu.getMainMenu().equals(""))
				myPlayItems.add(new MenuItem(temp_meu.getMainMenu(), file_path,
						temp_meu.getInapp_action(), temp_meu.getLink_type(),
						temp_meu.getHtmlURL(), temp_meu.getPopUpMessage(), temp_meu.getExtra_data()));
			else if (temp_meu.getMenu_title() != null
					&& !temp_meu.getMenu_title().equals(""))
				myPlayItems.add(new MenuItem(temp_meu.getMenu_title(),
						file_path, temp_meu.getInapp_action(), temp_meu
								.getLink_type(), temp_meu.getHtmlURL(),
						temp_meu.getPopUpMessage(), temp_meu.getExtra_data()));

		}

		return myPlayItems;
	}



	// private List<MenuItem> createMyPlaySettings() {
	// List<MenuItem> myPlayItems = new
	// ArrayList<GlobalMenuFragment.MenuItem>();
	//
	// // - My Profile
	// // - Downloads
	// // - My Language
	// // - Favorites
	// // - My Playlists
	// // - My Stream
	// // - My Discoveries
	// // - Notifications
	// myPlayItems.add(new MenuItem(MENU_ITEM_MY_PROFILE,
	// R.string.main_actionbar_settings_menu_item_my_profile,
	// R.drawable.icon_main_settings_my_profile));
	//
	// myPlayItems.add(new MenuItem(MENU_ITEM_MY_COLLECTIONS,
	// R.string.main_actionbar_settings_menu_item_my_collections,
	// R.drawable.icon_main_settings_my_collection));
	//
	// myPlayItems.add(new MenuItem(MENU_ITEM_MY_FAVORITES,
	// R.string.main_actionbar_settings_menu_item_my_favorites,
	// R.drawable.icon_main_settings_my_favorites));
	// myPlayItems.add(new MenuItem(MENU_ITEM_MY_PLAYLISTS,
	// R.string.main_actionbar_settings_menu_item_my_playlists,
	// R.drawable.icon_main_settings_my_playlists));
	//
	// myPlayItems.add(new MenuItem(MENU_ITEM_MY_STREAM,
	// R.string.main_actionbar_settings_menu_item_my_stream,
	// R.drawable.icon_main_settings_my_stream));
	//
	// myPlayItems.add(new MenuItem(MENU_ITEM_LOGOUT,
	// R.string.main_actionbar_settings_menu_item_logout, -1));
	//
	// return myPlayItems;
	// }
	//
	// private List<MenuItem> createMoreSettings() {
	// List<MenuItem> moreItems = new ArrayList<GlobalMenuFragment.MenuItem>();
	//
	//
	// moreItems.add(new MenuItem(MENU_ITEM_ABOUT,
	// R.string.main_actionbar_settings_menu_item_about,
	// R.drawable.icon_main_settings_about));
	// moreItems.add(new MenuItem(MENU_ITEM_HELP_FAQ,
	// R.string.main_actionbar_settings_menu_item_help_faq,
	// R.drawable.icon_main_settings_help_faq));
	// moreItems.add(new MenuItem(MENU_ITEM_APP_TOUR,
	// R.string.main_actionbar_settings_menu_item_app_tour,
	// R.drawable.icon_main_settings_app_tour));
	//
	// moreItems.add(new MenuItem(MENU_ITEM_RATE_THIS_APP,
	// R.string.main_actionbar_settings_menu_item_rate_this_app,
	// R.drawable.icon_main_settings_rate_this_app));
	// moreItems.add(new MenuItem(MENU_ITEM_GIVE_FEEDBACK,
	// R.string.main_actionbar_settings_menu_item_give_feedback,
	// R.drawable.icon_main_settings_give_feedback));
	//
	// return moreItems;
	// }

	// ======================================================
	// PRIVATE HELPER CLASSES.
	// ======================================================

	private static class MenuItemViewHolder {
		ImageView icon, img_temp;
		LanguageTextView label;
		View separator;
	}

	private static class CategoryViewHolder {
		LanguageTextView label;
		TextView expandIndicator;
		ImageView icon, img_temp;
		View separator;
		RelativeLayout rl_main;
		Switch toggleButton;
	}

	private final class SettingsAdapter extends BaseExpandableListAdapter {

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mCategories.get(groupPosition).getMenuItems()
					.get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			MenuItemViewHolder viewHolder;

			if (convertView == null) {
				convertView = mLayoutInflater.inflate(
						R.layout.list_item_main_actionbar_settings_menu_item,
						parent, false);

				viewHolder = new MenuItemViewHolder();
				viewHolder.icon = (ImageView) convertView
						.findViewById(R.id.main_setting_menu_item_icon);

				viewHolder.label = (LanguageTextView) convertView
						.findViewById(R.id.main_setting_menu_item_label);
				viewHolder.separator = (View) convertView
						.findViewById(R.id.main_setting_menu_item_separator);

				viewHolder.img_temp = (ImageView) convertView
						.findViewById(R.id.img_temp);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (MenuItemViewHolder) convertView.getTag();
			}
			viewHolder.img_temp.setVisibility(View.INVISIBLE);
			MenuItem menuItem = mCategories.get(groupPosition).getMenuItems()
					.get(childPosition);

			// viewHolder.label.setTextColor(
			// mResources.getColor(R.color.main_actionbar_settings_menu_item_text));
			viewHolder.label.setTextColor(mResources
					.getColor(R.color.main_actionbar_settings_category_color));
			// viewHolder.label.setTextColor(
			// mResources.getColor(R.color.main_actionbar_settings_menu_item_text_grey));

			// In case of new messages, Notifications menu item also has
			// different text color.
			// DatabaseManager dbM = DatabaseManager.getInstance(mContext);
			// Cursor unreadNotifCurs = dbM.getAllUnreadMessagesTitles();

			// viewHolder.label.setTextColor(
			// mResources.getColor(R.color.main_actionbar_settings_menu_item_text));
			viewHolder.label.setTextColor(mResources
					.getColor(R.color.main_actionbar_settings_category_color));
			// viewHolder.label.setTextColor(
			// mResources.getColor(R.color.main_actionbar_settings_menu_item_text_grey));
			viewHolder.label.setText(Utils.getMultilanguageTextLayOut(mContext,
					menuItem.getLabelResource()));

			picasso.load(new PicassoCallBack() {

				@Override
				public void onSuccess() {

				}

				@Override
				public void onError() {
				}
			}, menuItem.getIconResourcePath(), viewHolder.icon, -1);

			// if
			// (ApplicationConfigurations.getInstance(mContext).getSaveOfflineMode())
			// {
			// viewHolder.label.setTextColor(mResources
			// .getColor(R.color.membership_detail_text_color));
			// }
			// dbM.close();

			return convertView;
		}



		@Override
		public int getChildrenCount(int groupPosition) {
			return mCategories.get(groupPosition).getMenuItems().size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return mCategories.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			if (mCategories == null)
				return 0;
			return mCategories.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			Category category = mCategories.get(groupPosition);
			if (category.getMenuItems() == null
					|| category.getMenuItems().size() <= 0) {
				CategoryViewHolder viewHolder;
				// if (convertView == null) {

				convertView = mLayoutInflater.inflate(
						R.layout.list_item_main_actionbar_settings_menu_item,
						parent, false);

				viewHolder = new CategoryViewHolder();
				viewHolder.icon = (ImageView) convertView
						.findViewById(R.id.main_setting_menu_item_icon);
				viewHolder.label = (LanguageTextView) convertView
						.findViewById(R.id.main_setting_menu_item_label);
				viewHolder.toggleButton = (Switch) convertView
						.findViewById(R.id.toggleButton);
				viewHolder.separator = (View) convertView
						.findViewById(R.id.main_setting_menu_item_separator);
				viewHolder.img_temp = (ImageView) convertView
						.findViewById(R.id.img_temp);
				convertView.setTag(viewHolder);

				viewHolder.img_temp.setVisibility(View.GONE);

				viewHolder.label
						.setTextColor(mResources
								.getColor(R.color.main_actionbar_settings_category_color));

				if (groupPosition == 0) {
					viewHolder.toggleButton.setVisibility(View.VISIBLE);
					if (mApplicationConfigurations.getSaveOfflineMode()) {
						viewHolder.toggleButton.setChecked(true);
					} else
						viewHolder.toggleButton.setChecked(false);

					viewHolder.toggleButton
							.setOnCheckedChangeListener(new OnCheckedChangeListener() {
								@Override
								public void onCheckedChanged(
										final CompoundButton buttonView,
										boolean isChecked) {
									if (mApplicationConfigurations
											.getSaveOfflineMode()
											&& !Utils.isConnected()
											&& !isChecked) {
										buttonView.setChecked(true);
									}

									if (mOnGlobalMenuItemSelectedListener != null)
										mOnGlobalMenuItemSelectedListener
												.onGlobalMenuItemSelected(null,
														MENU_ITEM_OFFLINE_MUSCI_TOGGLE_ACTION);
								}
							});
				} else
					viewHolder.toggleButton.setVisibility(View.GONE);

				// viewHolder.label.setTextColor(
				// mResources.getColor(R.color.main_actionbar_settings_menu_item_text));
				viewHolder.label
						.setTextColor(mResources
								.getColor(R.color.main_actionbar_settings_category_color));

				viewHolder.label.setText(Utils.getMultilanguageTextLayOut(
						mContext, category.getLabelResourceId()));

				picasso.load(new PicassoCallBack() {

					@Override
					public void onSuccess() {

					}

					@Override
					public void onError() {
					}
				}, category.getIconResourceId(), viewHolder.icon, -1);

				if (ApplicationConfigurations.getInstance(mContext)
						.getSaveOfflineMode()) {
					viewHolder.label.setTextColor(mResources
							.getColor(R.color.membership_detail_text_color));
				}

			} else {
				CategoryViewHolder viewHolder;

				// if (convertView == null) {
				convertView = mLayoutInflater.inflate(
						R.layout.list_item_main_actionbar_settings_category,
						parent, false);

				viewHolder = new CategoryViewHolder();
				viewHolder.label = (LanguageTextView) convertView
						.findViewById(R.id.main_setting_category_label);
				viewHolder.icon = (ImageView) convertView
						.findViewById(R.id.main_setting_menu_item_icon);
				viewHolder.expandIndicator = (TextView) convertView
						.findViewById(R.id.main_setting_category_expand_indicator);

				convertView.setTag(viewHolder);

				// if (getResources()
				// .getString(category.getLabelResourceId())
				// .equals(getResources()
				// .getString(
				// R.string.main_actionbar_settings_category_signin_options))) {

				// if (mApplicationConfigurations.isuserLoggedIn()) {
				// viewHolder.label.setTextColor(getResources().getColor(
				// R.color.profile_name_text_color));
				// if (!mApplicationConfigurations.getGigyaFBFirstName()
				// .equals("")
				// && !mApplicationConfigurations
				// .getGigyaFBLastName().equals(""))
				// viewHolder.label.setText(mApplicationConfigurations
				// .getGigyaFBFirstName()
				// + " "
				// + (mApplicationConfigurations
				// .getGigyaFBLastName()));
				// else if (!mApplicationConfigurations
				// .getGigyaGoogleFirstName().equals("")
				// && !mApplicationConfigurations
				// .getGigyaGoogleLastName().equals(""))
				// viewHolder.label.setText(mApplicationConfigurations
				// .getGigyaGoogleFirstName()
				// + " "
				// + (mApplicationConfigurations
				// .getGigyaGoogleLastName()));
				// else if (!mApplicationConfigurations
				// .getGigyaTwitterFirstName().equals("")
				// && !mApplicationConfigurations
				// .getGigyaTwitterLastName().equals(""))
				// viewHolder.label.setText(mApplicationConfigurations
				// .getGigyaTwitterFirstName()
				// + " "
				// + (mApplicationConfigurations
				// .getGigyaTwitterLastName()));
				// else if (!mApplicationConfigurations
				// .getHungmaFirstName().equals("")
				// && !mApplicationConfigurations
				// .getHungamaLastName().equals(""))
				// viewHolder.label.setText(mApplicationConfigurations
				// .getHungmaFirstName()
				// + " "
				// + (mApplicationConfigurations
				// .getHungamaLastName()));
				// else
				// viewHolder.label.setText(mApplicationConfigurations
				// .getHungamaEmail());
				//
				// } else {
				// viewHolder.label.setText(Utils
				// .getMultilanguageTextLayOut(mContext,
				// category.getLabelResourceId()));
				// }
				// } else {
				viewHolder.label.setText(Utils.getMultilanguageTextLayOut(
						mContext, category.getLabelResourceId()));
				// }

				picasso.load(new PicassoCallBack() {

					@Override
					public void onSuccess() {

					}

					@Override
					public void onError() {
					}
				}, category.getIconResourceId(), viewHolder.icon, -1);

				if (isExpanded) {
					viewHolder.expandIndicator
							.setText(R.string.main_actionbar_settings_category_expanded);
				} else {
					viewHolder.expandIndicator
							.setText(R.string.main_actionbar_settings_category_collapsed);
				}
			}

			// dbM.close();

			return convertView;

		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		@Override
		public void onGroupExpanded(int groupPosition) {
			// closes all the other groups.
			int size = mCategories.size();
			for (int i = 0; i < size; i++) {
				if (i != groupPosition) {
					mExpandableListView.collapseGroup(i);
				}
			}

			super.onGroupExpanded(groupPosition);
		}
	}

	private final class SettingsAdapterList extends BaseAdapter {

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public int getCount() {
			if (!mApplicationConfigurations.isuserLoggedIn()) {
				if (myPlayItems != null)
					return myPlayItems.size() - 1;
				else
					return 0;
			} else {
				if (myPlayItems != null)
					return myPlayItems.size();
				else
					return 0;
			}

		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			CategoryViewHolder viewHolder;
			// if (convertView == null) {

			convertView = mLayoutInflater.inflate(
					R.layout.list_item_main_actionbar_settings_menu_item,
					parent, false);

			viewHolder = new CategoryViewHolder();
			viewHolder.icon = (ImageView) convertView
					.findViewById(R.id.main_setting_menu_item_icon);
			viewHolder.toggleButton = (Switch) convertView
					.findViewById(R.id.toggleButton);
			viewHolder.rl_main = (RelativeLayout) convertView
					.findViewById(R.id.rl_main);
			viewHolder.label = (LanguageTextView) convertView
					.findViewById(R.id.main_setting_menu_item_label);
			viewHolder.separator = (View) convertView
					.findViewById(R.id.main_setting_menu_item_separator);
			viewHolder.img_temp = (ImageView) convertView
					.findViewById(R.id.img_temp);
			convertView.setTag(viewHolder);

			viewHolder.img_temp.setVisibility(View.GONE);
			MenuItem menuItem = myPlayItems.get(position);

			viewHolder.label.setTextColor(mResources
					.getColor(R.color.main_actionbar_settings_category_color));

			viewHolder.toggleButton.setVisibility(View.GONE);

			// viewHolder.label.setTextColor(
			// mResources.getColor(R.color.main_actionbar_settings_menu_item_text));
			if (menuItem
					.getLabelResource()
					.equalsIgnoreCase(
							getString(R.string.main_actionbar_settings_menu_item_logout))) {
				// setMembershipText(viewHolder.label);
				// viewHolder.label
				// .setTextColor(mResources
				// .getColor(R.color.white));
				viewHolder.rl_main.setBackgroundColor(mContext.getResources()
						.getColor(R.color.global_menu_logout_color));
				// viewHolder.rl_main.setAlpha(0.5f);
				viewHolder.label
						.setTextColor(mResources
								.getColor(R.color.main_actionbar_settings_menu_item_text_grey));
				// convertView.setBackgroundColor(mContext.getResources().getColor(R.color.global_menu_logout_color));
			} else {

				viewHolder.label
						.setTextColor(mResources
								.getColor(R.color.main_actionbar_settings_category_color));

				// viewHolder.rl_main
				// .setBackgroundResource(R.drawable.background_main_actionbar_settings_menu_item_selector);
				viewHolder.rl_main.setAlpha(1.0f);
			}

			// if (!mApplicationConfigurations.isuserLoggedIn()) {
			// if (menuItem.getLabelResource()!=null &&
			// menuItem.getLabelResource().equalsIgnoreCase(getString(R.string.main_actionbar_settings_menu_item_logout)))
			// {
			// convertView.setVisibility(View.GONE);
			// }
			// }else
			// convertView.setVisibility(View.VISIBLE);

			viewHolder.label.setText(Utils.getMultilanguageText(mContext,
					menuItem.getLabelResource()));

			picasso.load(new PicassoCallBack() {

				@Override
				public void onSuccess() {

				}

				@Override
				public void onError() {
				}
			}, menuItem.getIconResourcePath(), viewHolder.icon, -1);

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

	}

	private PicassoUtil picasso;

	/*
	 * represents an item in the menu.
	 */
	public final class MenuItem {

		private final String labelResource;
		private final String inapaction;
		private final String iconResourcePath;
		private final String link_type;
		private final String html_url;
		private final String popup_message;
		private final LeftMenuExtraData extraData;

		public MenuItem(String labelResource, String iconResourcePath,
				String inapaction, String link_type, String html_url,
				String popup_message, LeftMenuExtraData extraData) {
			this.labelResource = labelResource;
			this.inapaction = inapaction;
			this.iconResourcePath = iconResourcePath;
			this.link_type = link_type;
			this.html_url = html_url;
			this.popup_message = popup_message;
			this.extraData = extraData;
		}

		public String getPopUpMsg() {
			return popup_message;
		}

		public String getInAppAction() {
			return inapaction;
		}

		public String getLinkType() {
			return link_type;
		}

		public String getHtmlURL() {
			return html_url;
		}

		/**
		 * Retrieves the resource id for the String label.
		 */
		public String getLabelResource() {
			return labelResource;
		}

		/**
		 * Retrieves the resource id foe the Drawable icon.
		 */
		public String getIconResourcePath() {
			return iconResourcePath;
		}

		public LeftMenuExtraData getExtraData() {
			return extraData;
		}
	}

	/*
	 * represents a category contains items in the settings.
	 */
	public final class Category {

		// private final int id;
		private final String labelResource;
		private final String inapaction;
		private final List<MenuItem> menuItems;
		// private final int iconResourceId;
		private final String iconResourcePath;
		private final String link_type;
		private final String html_url;
		private final String popup_message;
		private final LeftMenuExtraData extraData;

		// public Category(int id, int labelResourceId, List<MenuItem>
		// menuItems,
		// int iconResourceId) {
		// this.id = id;
		// this.labelResourceId = labelResourceId;
		// this.menuItems = menuItems;
		// this.iconResourceId = iconResourceId;
		// }

		public Category(String labelResourceId, List<MenuItem> menuItems,
				String iconResourcePath, String inapaction, String link_type,
				String html_url, String popup_message, LeftMenuExtraData extraData) {
			// this.id = id;
			this.labelResource = labelResourceId;
			this.menuItems = menuItems;
			this.iconResourcePath = iconResourcePath;
			this.inapaction = inapaction;
			this.link_type = link_type;
			this.html_url = html_url;
			this.popup_message = popup_message;
			this.extraData = extraData;
		}

		public String getPopUpMsg() {
			return popup_message;
		}

		// public int getId() {
		// return id;
		// }

		public String getLinkType() {
			return link_type;
		}

		public String getHtmlURL() {
			return html_url;
		}

		/**
		 * Retrieves the resource id contains the String lable.
		 */
		public String getLabelResourceId() {
			return labelResource;
		}

		public List<MenuItem> getMenuItems() {
			return menuItems;
		}

		/**
		 * Retrieves the resource id foe the Drawable icon.
		 */
		public String getIconResourceId() {
			return iconResourcePath;
		}

		/**
		 * Retrieves the resource id foe the Drawable icon.
		 */
		public String getInapAction() {
			return inapaction;
		}

		public LeftMenuExtraData getExtraData() {
			return extraData;
		}
	}

	// ======================================================
	// PUBLIC.
	// ======================================================

	// public static final int CATEGORY_QUICK_LINKS = 100;
	// public static final int CATEGORY_MY_PLAY = 200;
	// public static final int CATEGORY_MORE = 300;
	// // QUICK LINKS
	// public static final int MENU_ITEM_SPECIALS = 101;
	// public static final int MENU_ITEM_APP_TOUR = 102;
	// public static final int MENU_ITEM_MUSIC = 103;
	// public static final int MENU_ITEM_VIDEOS = 104;
	// public static final int MENU_ITEM_LIVE_RADIO = 105;
	// public static final int MENU_ITEM_DISCOVER = 106;
	// public static final int MENU_ITEM_MY_STREAM = 107;
	// public static final int MENU_ITEM_NOTIFICATIONS = 108;
	// // MY PLAY
	// public static final int MENU_ITEM_MY_PROFILE = 201;
	// public static final int MENU_ITEM_MY_COLLECTIONS = 202;
	// public static final int MENU_ITEM_MY_FAVORITES = 203;
	// public static final int MENU_ITEM_MY_PLAYLISTS = 204;
	// public static final int MENU_ITEM_MY_DISCOVERIES = 205;
	// public static final int MENU_ITEM_MY_PREFERENCES = 206;
	// public static final int MENU_ITEM_SETTINGS_AND_ACCOUNTS = 207;
	// public static final int MENU_ITEM_OFFLINE_MUSIC = 208;
	// public static final int MENU_ITEM_FREE_RECHARGE = 213;
	// public static final int MENU_ITEM_REEDEEM_COUPON = 214;
	// public static final int MENU_ITEM_OFFLINE_MUSIC_TOGGLE = 212;
	// public static final int MENU_ITEM_UPGRATE_PRO = 209;
	// public static final int MENU_ITEM_LANGUAGE_SETTING = 210;
	// public static final int MENU_ITEM_SUBSCRIPTION_PLAN = 211;
	// // MORE
	// public static final int MENU_ITEM_REWARDS = 301;
	// public static final int MENU_ITEM_INVITE_FRIENDS = 302;
	// public static final int MENU_ITEM_RATE_THIS_APP = 303;
	// public static final int MENU_ITEM_GIVE_FEEDBACK = 304;
	// public static final int MENU_ITEM_HELP_FAQ = 305;
	// public static final int MENU_ITEM_ABOUT = 306;
	// // LOGOUT
	// public static final int MENU_ITEM_LOGOUT = 400;

	// public static final int MENU_ITEM_UPGRADE = 500;

	// in app action

	// public static final String MENU_ITEM_MY_PLAY_ACTION = "my_play";
	public static final String MENU_ITEM_MY_PROFILE_ACTION = "my_profile";
	public static final String MENU_ITEM_DOWNLOADS_ACTION = "downloads";
	public static final String MENU_ITEM_MY_FAVORITES_ACTION = "my_favorites";
	public static final String MENU_ITEM_MY_PLAYLISTS_ACTION = "my_playlists";
	public static final String MENU_ITEM_MY_STREAM_ACTION = "my_stream";
	public static final String MENU_ITEM_LOGOUT_ACTION = "logout";

	public static final String MENU_ITEM_OFFLINE_MUSCI_ACTION = "offline_music";
	public static final String MENU_ITEM_OFFLINE_MUSCI_TOGGLE_ACTION = "offline_music_toggle";
	public static final String MENU_ITEM_HTML_ACTION = "html";
	public static final String MENU_ITEM_REDEEM_COUPON_ACTION = "redeem_coupon";
	public static final String MENU_ITEM_LANGUAGE_SETTINGS_ACTION = "language_settings";
	public static final String MENU_ITEM_SUBSCRIPTION_PLAN_ACTION = "subscription_plan";
	public static final String MENU_ITEM_REWARDS_ACTION = "rewards";
	public static final String MENU_ITEM_SETTINGS_ACTION = "settings";

	// public static final String MENU_ITEM_MORE_ACTION = "more";
	public static final String MENU_ITEM_ABOUT_ACTION = "about";
	public static final String MENU_ITEM_HELP_FAQ_ACTION = "help_faq";
	public static final String MENU_ITEM_APP_TOUR_ACTION = "app_tour";
	public static final String MENU_ITEM_RATE_OUT_APP_ACTION = "rate_our_app";
	public static final String MENU_ITEM_YOUR_FEEDBACK_ACTION = "your_feedback";

	public static final String MENU_ITEM_UPGRADE_ACTION = "upgrade";
	public static final String MENU_ITEM_SUBSCRIPTION_STATUS_ACTION = "subscription_status";
	public static final String MENU_ITEM_MOBILE_RECHARGE_ACTION = "mobile_recharge";

	/**
	 * Interface definition to be invoked when the user has selected an item.
	 */
	public interface OnGlobalMenuItemSelectedListener {

		public void onGlobalMenuItemSelected(Object obj, String action);

	}

	public void setOnGlobalMenuItemSelectedListener(
			OnGlobalMenuItemSelectedListener listener) {
		mOnGlobalMenuItemSelectedListener = listener;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View view,
			int groupPosition, int childPosition, long id) {
		view.setSelected(true);
		// if (id == GlobalMenuFragment.MENU_ITEM_LIVE_RADIO
		// || id == GlobalMenuFragment.MENU_ITEM_MUSIC
		// || id == GlobalMenuFragment.MENU_ITEM_VIDEOS) {
		// SongCatcherFragment.isSongCatcherOpen = false;
		// }

		if (mOnGlobalMenuItemSelectedListener != null) {
			mOnGlobalMenuItemSelectedListener.onGlobalMenuItemSelected(
					mCategories.get(groupPosition).getMenuItems()
							.get(childPosition), null);
		}
		return true;
	}

	@Override
	public void onDestroyView() {
//		System.gc();
//		System.runFinalization();
		super.onDestroyView();
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v,
			int groupPosition, long id) {

		if (mCategories.get(groupPosition).getMenuItems() == null
				|| mCategories.get(groupPosition).getMenuItems().size() <= 0) {
			// Toast.makeText(mContext, "" + id, Toast.LENGTH_SHORT).show();
			if (mOnGlobalMenuItemSelectedListener != null) {
				mOnGlobalMenuItemSelectedListener.onGlobalMenuItemSelected(
						mCategories.get(groupPosition), null);
			}
			return true;
		} /*
		 * else if (groupPosition == 0) {
		 * 
		 * if (!mApplicationConfigurations.isuserLoggedIn()) { if
		 * (!parent.isGroupExpanded(0)) { parent.collapseGroup(0); Intent
		 * startLoginActivityIntent = new Intent(getActivity(),
		 * LoginActivity.class); startLoginActivityIntent.putExtra(
		 * UpgradeActivity.ARGUMENT_UPGRADE_ACTIVITY, "upgrade_activity");
		 * startLoginActivityIntent.putExtra( LoginActivity.FLURRY_SOURCE,
		 * FlurryConstants.FlurryUserStatus.GlobleMenu .toString());
		 * getActivity().startActivityForResult( startLoginActivityIntent,
		 * MainActivity.LOGIN_ACTIVITY_CODE); } } else { createSettings(); if
		 * (mSettingsAdapter != null) mSettingsAdapter.notifyDataSetChanged(); }
		 * }
		 */
		return false;
	}

	// ======================================================
	// Operations callbacks.
	// ======================================================

	@Override
	public void onStart(int operationId) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onSuccess(int operationId, Map<String, Object> responseObjects) {
		try {
			switch (operationId) {
			case (OperationDefinition.Hungama.OperationId.LET_MENU):
				String response = new com.hungama.myplay.activity.data.CacheManager(
						getActivity()).getLeftMenuResponse();
				mLeftMenuResponse = new Gson().fromJson(response,
						LeftMenuResponse.class);

				picasso = PicassoUtil.with(mContext);

				if (mLeftMenuResponse != null) {
					createSettings();
					setUpAdapter();
				}

				break;
			case (OperationDefinition.Hungama.OperationId.SUBSCRIPTION_CHECK):
				SubscriptionStatusResponse subscriptionsubscriptionStatusResponse = (SubscriptionStatusResponse) responseObjects
						.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);

				if (subscriptionsubscriptionStatusResponse != null
						&& subscriptionsubscriptionStatusResponse.getSubscription() != null
						&& subscriptionsubscriptionStatusResponse.getSubscription().getSubscriptionStatus()==1) {
					mApplicationConfigurations
							.setIsUserHasSubscriptionPlan(true);
					mApplicationConfigurations
							.setIsUserHasTrialSubscriptionPlan(subscriptionsubscriptionStatusResponse
									.getSubscription().isTrial());
					if (subscriptionsubscriptionStatusResponse.getSubscription().isTrial()) {
						mApplicationConfigurations
								.setTrialExpiryDaysLeft(subscriptionsubscriptionStatusResponse
										.getSubscription().getDaysRemaining());
						buttonUpgrade
								.setText(R.string.global_menu_button_upgrade_to_pro);
						buttonUpgrade.setVisibility(View.VISIBLE);
					} else
						mApplicationConfigurations.setTrialExpiryDaysLeft(0);
					buttonUpgrade.setVisibility(View.GONE);
				} else {
					buttonUpgrade
							.setText(R.string.global_menu_button_upgrade_to_pro);
					buttonUpgrade.setVisibility(View.VISIBLE);
					mApplicationConfigurations
							.setIsUserHasSubscriptionPlan(false);
					mApplicationConfigurations
							.setIsUserHasTrialSubscriptionPlan(false);
					mApplicationConfigurations.setTrialExpiryDaysLeft(0);
				}

				if (mSettingsAdapter != null)
					mSettingsAdapter.notifyDataSetChanged();
				break;
			}
		} catch (Exception e) {
			Logger.printStackTrace(e);
		}
		// hideLoadingDialogFragment();
	}

	@Override
	public void onFailure(int operationId, ErrorType errorType,
			String errorMessage) {

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {

		if (mOnGlobalMenuItemSelectedListener != null) {
			mOnGlobalMenuItemSelectedListener.onGlobalMenuItemSelected(
					myPlayItems.get(position), null);
		}
	}

	public static Bitmap getRoundedShape(Bitmap scaleBitmapImage,
			Context context) {
		int padding = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources()
						.getDisplayMetrics());
		int image_size = (int) context.getResources().getDimension(
				R.dimen.left_drawer_user_size);
		int targetWidth = /* iv.getWidth() */image_size - padding;// 46;
		int targetHeight = /* iv.getHeight() */image_size - padding;// 46;
		Logger.s(targetWidth + " :: " + targetHeight);

		Bitmap targetBitmap = Bitmap.createBitmap(targetWidth, targetHeight,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(targetBitmap);
		Path path = new Path();
		path.addCircle(((float) targetWidth) / 2, ((float) targetHeight) / 2,
				(Math.min(((float) targetWidth), ((float) targetHeight)) / 2),
				Path.Direction.CW);
		Paint paint = new Paint();
		paint.setColor(Color.GRAY);
		// paint.setStyle(Paint.Style.STROKE);
		paint.setStyle(Paint.Style.FILL);
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawOval(new RectF(0, 0, targetWidth, targetHeight), paint);
		// paint.setColor(Color.TRANSPARENT);
		canvas.clipPath(path);
		Bitmap sourceBitmap = scaleBitmapImage;
		canvas.drawBitmap(sourceBitmap, new Rect(0, 0, sourceBitmap.getWidth(),
				sourceBitmap.getHeight()), new RectF(0, 0, targetWidth,
				targetHeight), paint);
		return targetBitmap;
	}
}
