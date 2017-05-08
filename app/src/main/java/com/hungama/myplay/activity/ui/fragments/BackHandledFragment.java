package com.hungama.myplay.activity.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;


import com.hungama.hungamamusic.lite.R;
import com.hungama.myplay.activity.ui.MainActivity;
import com.hungama.myplay.activity.util.Logger;

import java.lang.ref.WeakReference;

/**
 * Adds to any descendant to the ability to show a loading dialog.
 */
public abstract class BackHandledFragment extends Fragment {
	private FragmentManager mFragmentManager;

	private LoadingDialogFragment mLoadingDialogFragment = null;

	private static WeakReference<MainActivity> wrActivity = null;

    protected BackHandlerInterface backHandlerInterface;

    public abstract boolean onBackPressed();
    public abstract void setTitle(boolean needOnlyHight,boolean needToSetTitle);

    public interface BackHandlerInterface {
        public void setSelectedFragment(BackHandledFragment mainFragment);
    }


    //name, margin, alpha,


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mFragmentManager = getFragmentManager();

		wrActivity = new WeakReference<MainActivity>(
				(MainActivity) getActivity());

        if(!(getActivity()  instanceof BackHandlerInterface)) {
            //throw new ClassCastException("Hosting activity must implement BackHandlerInterface");
        } else {
            backHandlerInterface = (BackHandlerInterface) getActivity();
        }
		// Mark this fragment as the selected Fragment.
		if(backHandlerInterface!=null)
			backHandlerInterface.setSelectedFragment(this);


	}

    @Override
    public void onStart() {
        super.onStart();


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

			}
		} catch (Exception e) {
			Logger.e(getClass().getName() + ":54", e.toString());
		}catch (Error e) {
		}
	}

	protected void hideLoadingDialog() {

		try {
			if (mLoadingDialogFragment != null && getActivity() != null
					&& !getActivity().isFinishing()) {


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
			}
		} catch (Exception e) {
		}catch (Error e) {
		}
	}
}