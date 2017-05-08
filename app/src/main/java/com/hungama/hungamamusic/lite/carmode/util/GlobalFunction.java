package com.hungama.hungamamusic.lite.carmode.util;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.hungama.hungamamusic.lite.carmode.view.CustomDialogLayout;
import com.hungama.myplay.activity.util.PicassoUtil;

import java.net.InetAddress;

/**
 * Created by KL81HC on 8/18/2015.
 */
public class GlobalFunction {

    public static void downloadImage(Activity act, String url1, ImageView iv) {
        try {
            if (!TextUtils.isEmpty(url1)) {
                PicassoUtil.with(act).loadWithFit(null, url1, iv, -1);
            }
        } catch (Exception e) {
            Log.e(act.getClass().getSimpleName() + ":701", e.toString());
        }
    }

    public static CustomDialogLayout showMessageDialog(Activity act, CustomDialogLayout.DialogType type, String msg, CustomDialogLayout.IDialogListener listener) {
        if (act != null) {
            CustomDialogLayout mCustomDialog = new CustomDialogLayout(act, type);
            mCustomDialog.setMessage(msg);
            mCustomDialog.setListener(listener);
            mCustomDialog.show();

            return mCustomDialog;
        }

        return null;
    }

    public static CustomDialogLayout showMessageDialog(Activity act, CustomDialogLayout.DialogType type, int msg, CustomDialogLayout.IDialogListener listener) {
        if (act != null) {
            CustomDialogLayout mCustomDialog = new CustomDialogLayout(act, type);
            mCustomDialog.setMessage(msg);
            mCustomDialog.setListener(listener);
            mCustomDialog.show();

            return mCustomDialog;
        }

        return null;
    }

    public static boolean isInternetAvailable() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com"); //You can replace it with your name

            if (ipAddr.equals("")) {
                return false;
            } else {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

    }
}
