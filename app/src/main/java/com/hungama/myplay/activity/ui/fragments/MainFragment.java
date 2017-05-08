package com.hungama.myplay.activity.ui.fragments;

import java.lang.ref.WeakReference;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.util.Logger;

/**
 * Adds to any descendant to the ability to show a loading dialog.
 */
public class MainFragment extends Fragment {
	private FragmentManager mFragmentManager;
	private LoadingDialogFragment mLoadingDialogFragment = null;

	private static WeakReference<MainActivity> wrActivity = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragmentManager = getFragmentManager();
		wrActivity = new WeakReference<MainActivity>(
				(MainActivity) getActivity());
	}

	// ======================================================
	// Dialog helper methods.
	// ======================================================

	public void showLoadingDialog(int messageResource) {
		try {
			if (mLoadingDialogFragment == null && getActivity() != null
					&& !getActivity().isFinishing()) {

				mLoadingDialogFragment = LoadingDialogFragment
						.newInstance(R.string.application_dialog_loading_content);
				mLoadingDialogFragment.setCancelable(true);
				mLoadingDialogFragment.show(mFragmentManager,
						LoadingDialogFragment.FRAGMENT_TAG);

				// // For avoiding perform an action after onSaveInstanceState.
				// new Handler().post(new Runnable() {
				//
				// public void run() {
				// mLoadingDialogFragment =
				// LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
				// mLoadingDialogFragment.setCancelable(true);
				// mLoadingDialogFragment.show(mFragmentManager,
				// LoadingDialogFragment.FRAGMENT_TAG);
				//
				// }
				// });
			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":54", e.toString());
		}catch (Error e) {
		}
	}

	// protected void showLoadingDialogWithoutVisibleCheck(int messageResource)
	// {
	//
	// try {
	// if (mLoadingDialogFragment == null && getActivity() != null &&
	// !getActivity().isFinishing()) {
	//
	// mLoadingDialogFragment =
	// LoadingDialogFragment.newInstance(R.string.application_dialog_loading_content);
	// mLoadingDialogFragment.setCancelable(true);
	// if ((wrActivity.get() != null) && !(wrActivity.get().isFinishing())) {
	// FragmentManager fm = wrActivity.get().getSupportFragmentManager();
	// mLoadingDialogFragment.show(fm, LoadingDialogFragment.FRAGMENT_TAG);
	// }
	//
	// // // For avoiding perform an action after onSaveInstanceState.
	// // new Handler().post(new Runnable() {
	// //
	// // public void run() {
	// //
	// //
	// // }
	// // });
	// }else{
	// if ((wrActivity.get() != null) && !(wrActivity.get().isFinishing())) {
	// FragmentManager fm = wrActivity.get().getSupportFragmentManager();
	// mLoadingDialogFragment.show(fm, LoadingDialogFragment.FRAGMENT_TAG);
	// }
	// }
	//
	// } catch (Exception e) {
	// }
	// }
	protected void hideLoadingDialog() {

		try {
			if (mLoadingDialogFragment != null && getActivity() != null
					&& !getActivity().isFinishing()) {

				// FragmentTransaction fragmentTransaction =
				// mFragmentManager.beginTransaction();
				// FragmentTransaction fragmentTransaction =
				// wrActivity.get().getSupportFragmentManager().beginTransaction();
				// fragmentTransaction.remove(mLoadingDialogFragment);
				// Fragment temp =
				// mFragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
				// if(temp!=null){
				// fragmentTransaction.remove(temp);
				// fragmentTransaction.commitAllowingStateLoss();
				// }

				FragmentTransaction fragmentTransaction = mFragmentManager
						.beginTransaction();
				Fragment temp = mFragmentManager
						.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
				if (temp != null) {
					fragmentTransaction.remove(temp);
					fragmentTransaction.commitAllowingStateLoss();
				} else {
					fragmentTransaction = wrActivity.get()
							.getSupportFragmentManager().beginTransaction();
					fragmentTransaction.remove(mLoadingDialogFragment);
					fragmentTransaction.commitAllowingStateLoss();
				}
				mLoadingDialogFragment = null;

				// // For avoiding perform an action after onSaveInstanceState.
				// new Handler().post(new Runnable() {
				//
				// public void run() {
				//
				//
				// }
				// });
			}
		} catch (Exception e) {
		}catch (Error e) {
		}
	}
	// private void showLoadingDialogFragment() {
	//
	// FragmentManager fragmentManager = getFragmentManager();
	//
	// if (fragmentManager != null) {
	//
	// Fragment fragment =
	// fragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
	//
	// if (fragment == null) {
	//
	// LoadingDialogFragment dialogFragment =
	// LoadingDialogFragment.newInstance(R.string.application_dialog_loading);
	// dialogFragment.setCancelable(true);
	// dialogFragment.show(fragmentManager, LoadingDialogFragment.FRAGMENT_TAG);
	// }
	// }
	// }
	//
	// private void hideLoadingDialogFragment() {
	//
	// FragmentManager fragmentManager = getFragmentManager();
	//
	// if (fragmentManager != null) {
	//
	// Fragment fragment =
	// fragmentManager.findFragmentByTag(LoadingDialogFragment.FRAGMENT_TAG);
	//
	// if (fragment != null) {
	//
	// DialogFragment fragmentDialog = (DialogFragment) fragment;
	// FragmentTransaction fragmentTransaction =
	// fragmentManager.beginTransaction();
	// fragmentTransaction.remove(fragmentDialog);
	// fragmentDialog.dismissAllowingStateLoss();
	// }
	// }
	// }

}
