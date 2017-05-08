package com.hungama.hungamamusic.lite.carmode.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

public class MessageDialogFragment extends DialogFragment {

	private int mMsgResId;
	private IMessageDialogListener mListener;

	public MessageDialogFragment(int msgResId, IMessageDialogListener listener) {
		this.mMsgResId = msgResId;
		this.mListener = listener;
	}

	@Override
	@NonNull
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(this.mMsgResId);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onPositiveBtnClick();
			}
		}).setNegativeButton(android.R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				mListener.onNegativeBtnClick();
			}
		});

		return builder.create();
	}

	public interface IMessageDialogListener {
		void onPositiveBtnClick();

		void onNegativeBtnClick();
	}

}
