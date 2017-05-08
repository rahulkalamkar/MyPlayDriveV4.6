package com.hungama.myplay.activity.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

import com.hungama.myplay.activity.communication.CommunicationManager;
import com.hungama.myplay.activity.communication.ThreadPoolManager;
import com.hungama.myplay.activity.communication.exceptions.InvalidRequestException;
import com.hungama.myplay.activity.communication.exceptions.InvalidResponseDataException;
import com.hungama.myplay.activity.communication.exceptions.NoConnectivityException;
import com.hungama.myplay.activity.communication.exceptions.OperationCancelledException;
import com.hungama.myplay.activity.data.CacheManager.ReadCallback;
import com.hungama.myplay.activity.data.DataManager;
import com.hungama.myplay.activity.data.configurations.ApplicationConfigurations;
import com.hungama.myplay.activity.data.configurations.ServerConfigurations;
import com.hungama.myplay.activity.operations.hungama.SubscriptionCheckOperation;
import com.hungama.myplay.activity.util.Logger;
import com.hungama.myplay.activity.util.Utils;

import java.util.Map;

/**
 * Checks if the User is subscribed to the application and caches his
 * subscription plans.
 */
public class SubscriptionService extends Service {

	private static final String TAG = "SubscriptionService";

	private DataManager mDataManager;

	private ApplicationConfigurations mApplicationConfigurations;

	private String mHungamaSubscriptionServerUrl;
	private String mPartnerUserId;
	private String mAuthKey;

	// public SubscriptionService() {
	// super(TAG);
	// }
	//
	// public SubscriptionService(String name) {
	// super(TAG);
	// }

	@Override
	public void onCreate() {
		super.onCreate();

		mDataManager = DataManager.getInstance(getApplicationContext());

		ServerConfigurations serverConfigurations = mDataManager
				.getServerConfigurations();
		mApplicationConfigurations = mDataManager
				.getApplicationConfigurations();

		mHungamaSubscriptionServerUrl = serverConfigurations
				.getHungamaPayUrl();
		mPartnerUserId = mApplicationConfigurations.getPartnerUserId();
		mAuthKey = serverConfigurations.getHungamaAuthKey();

		onHandleIntent();
	}

	protected void onHandleIntent() {
		ThreadPoolManager.getInstance().submit(new Runnable() {

			@Override
			public void run() {
				try {

					android.os.Process
							.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

					Logger.i(TAG, "Getting subscription plans");

					final CommunicationManager communicationManager = new CommunicationManager();
					final SubscriptionCheckOperation subscriptionCheckOperation;
					String accountType = Utils
							.getAccountName(getApplicationContext());
					if (accountType != null) {
						subscriptionCheckOperation = new SubscriptionCheckOperation(
								getApplicationContext(),
								mHungamaSubscriptionServerUrl, mPartnerUserId,
								mAuthKey, accountType, null);
					} else {
						// Toast.makeText(getApplicationContext(),
						// "There is no Google account on this device",
						// Toast.LENGTH_LONG).show();
						stopSelf();
						return;
					}

					/*
					 * Checks if the user has got already subscription plan - is
					 * subscribed, if not gets it.
					 */
//					mDataManager.getStoredCurrentPlan(new ReadCallback() {
//
//						@Override
//						public void onRead(Object respose) {
//							try {
//								if (respose == null) {
//									// checks if the user is subscribed.
//									Map<String, Object> currentSubscriptionPlanResult;
//									currentSubscriptionPlanResult = communicationManager
//											.performOperation(
//													subscriptionCheckOperation,
//													getApplicationContext());
//
//									SubscriptionCheckResponse subscriptionCheckResponse = (SubscriptionCheckResponse) currentSubscriptionPlanResult
//											.get(SubscriptionCheckOperation.RESPONSE_KEY_SUBSCRIPTION_CHECK);
//
//									// if store in cache succeeded - set
//									// UserHasSubscriptionPlan=true
//									// and plan validity date in
//									// ApplicationConfigurations
//									if (mDataManager
//											.storeSubscriptionCurrentPlan(subscriptionCheckResponse)) {
//
//										mApplicationConfigurations
//												.setIsUserHasSubscriptionPlan(true);
//										mApplicationConfigurations
//												.setUserSubscriptionPlanDate(subscriptionCheckResponse
//														.getPlan()
//														.getValidityDate());
//										mApplicationConfigurations
//												.setIsUserHasTrialSubscriptionPlan(subscriptionCheckResponse
//														.getPlan().isTrial());
//										mApplicationConfigurations
//												.setTrialExpiryDaysLeft(subscriptionCheckResponse
//														.getPlan()
//														.getTrailExpiryDaysLeft());
//									} else {
//										mApplicationConfigurations
//												.setIsUserHasSubscriptionPlan(false);
//										mApplicationConfigurations
//												.setIsUserHasTrialSubscriptionPlan(false);
//										mApplicationConfigurations
//												.setTrialExpiryDaysLeft(0);
//									}
//								}
//
//								// /*
//								// * Checks if there are subscription plans
//								// * available for the user, if not gets them.
//								// */
//								// mDataManager
//								// .getStoredSubscriptionPlans(new
//								// ReadCallback() {
//								//
//								// @Override
//								// public void onRead(Object respose) {
//								// try {
//								// List<Plan> subscriptionPlans = null;
//								// if (respose != null) {
//								//
//								// subscriptionPlans = (List<Plan>) respose;
//								//
//								// if (Utils
//								// .isListEmpty(subscriptionPlans)) {
//								// subscriptionPlans = null;
//								// }
//								// }
//								//
//								// if (respose == null) {
//								//
//								// final SubscriptionOperation
//								// subscriptionOperation = new
//								// SubscriptionOperation(
//								// getBaseContext(),
//								// mHungamaSubscriptionServerUrl,
//								// String.valueOf(0),
//								// Utils.TEXT_EMPTY,
//								// mPartnerUserId,
//								// SubscriptionType.PLAN,
//								// mAuthKey, null,
//								// null, null,
//								// false, "");
//								//
//								// Map<String, Object> SubscriptionPlansResult =
//								// communicationManager
//								// .performOperation(
//								// subscriptionOperation,
//								// getApplicationContext());
//								//
//								// SubscriptionResponse subscriptionResponse =
//								// (SubscriptionResponse)
//								// SubscriptionPlansResult
//								// .get(SubscriptionOperation.RESPONSE_KEY_SUBSCRIPTION);
//								//
//								// if (subscriptionResponse != null
//								// && subscriptionResponse
//								// .getPlan() != null
//								// && subscriptionResponse
//								// .getPlan()
//								// .size() > 0) {
//								// if (subscriptionResponse
//								// .getSubscriptionType() ==
//								// SubscriptionType.PLAN) {
//								// mDataManager
//								// .storeSubscriptionPlans(subscriptionResponse
//								// .getPlan());
//								// }
//								// }
//								// }
//								// } catch (InvalidRequestException e) {
//								// e.printStackTrace();
//								// } catch (InvalidResponseDataException e) {
//								// e.printStackTrace();
//								// } catch (OperationCancelledException e) {
//								// e.printStackTrace();
//								// } catch (NoConnectivityException e) {
//								// e.printStackTrace();
//								// }
//								//
//								// }
//								// });
//
//							} catch (InvalidRequestException e) {
//								e.printStackTrace();
//							} catch (InvalidResponseDataException e) {
//								e.printStackTrace();
//							} catch (OperationCancelledException e) {
//								e.printStackTrace();
//							} catch (NoConnectivityException e) {
//								e.printStackTrace();
//							} finally {
//							}
//							stopSelf();
//						}
//					});

				} catch (Exception ex) {
				} catch (Error ex1) {
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		// System.gc();
		super.onDestroy();
	}
}
