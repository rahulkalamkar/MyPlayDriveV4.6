package com.hungama.hungamamusic.lite.carmode.view;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hungama.hungamamusic.lite.R;

public class CustomDialogLayout {

    private RelativeLayout rlDialogLayout;
    private LinearLayout llDialogButtons;
    private TextView tvTitle;
    private TextView tvMsg;
    private Button btnPositive;
    private Button btnNegative;
    private DialogType mDialogType;
    private IDialogListener mListener;

    public CustomDialogLayout(Activity act, DialogType type) {
        rlDialogLayout = (RelativeLayout) act.findViewById(R.id.rl_custom_dialog);
        llDialogButtons = (LinearLayout) act.findViewById(R.id.ll_dialog_buttons);
        tvTitle = (TextView) act.findViewById(R.id.tv_dialog_title);
        tvMsg = (TextView) act.findViewById(R.id.tv_msg);
        btnPositive = (Button) act.findViewById(R.id.btn_positive);
        btnNegative = (Button) act.findViewById(R.id.btn_negative);

        mDialogType = type;

        switch (type) {
            case CONFIRMATION:
                tvTitle.setText(R.string.title_message);
                llDialogButtons.setVisibility(View.VISIBLE);
                btnPositive.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mListener.onPositiveBtnClick();
                    }
                });

                btnNegative.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mListener.onNegativeBtnClick(CustomDialogLayout.this);
                    }
                });

                break;

            case MESSAGE:
            case MESSAGE_FORCE_CLOSE:
                tvTitle.setText(R.string.title_message);
                llDialogButtons.setVisibility(View.GONE);
                break;

            default:
                break;
        }

    }

    public void show() {
        rlDialogLayout.setVisibility(View.VISIBLE);

        if (mDialogType == DialogType.MESSAGE) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    hide();

                    if (mListener != null) {
                        mListener.onMessageAction();
                    }
                }
            }, 2500);
        }

    }

    public void hide() {
        if (isShown()) {
            rlDialogLayout.setVisibility(View.GONE);
        }
    }

    public void setMessage(int msg) {
        tvMsg.setText(msg);
    }

    public void setMessage(String msg) {
        tvMsg.setText(msg);
    }

    public void setListener(IDialogListener listener) {
        this.mListener = listener;
    }

    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    public boolean isShown() {
        return this.rlDialogLayout.isShown();
    }


    public enum DialogType {
        MESSAGE, MESSAGE_FORCE_CLOSE, CONFIRMATION
    }

    public interface IDialogListener {
        void onPositiveBtnClick();

        void onNegativeBtnClick();

        void onNegativeBtnClick(CustomDialogLayout layout);

        void onMessageAction();
    }

}
