package com.gigya.socialize.android.login.providers;

import android.app.Activity;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.android.GSAPI;
import com.gigya.socialize.android.ui.HostActivity;
import com.gigya.socialize.android.ui.HostActivity.HostActivityHandler;


public abstract class LoginProvider {

    public interface ProviderCallback {
        public void onResponse(GSObject response);
    }

    public abstract void login(Activity activity, GSObject params, Boolean silent, final ProviderCallback callback);

    public void clearSession() {
    }

    protected abstract void finish();

    protected void createActivity(HostActivityHandler handler) {
        HostActivity.create(GSAPI.getInstance().getContext(), handler);
    }

    protected static boolean isClassExist(String className) {
        try {
            return Class.forName(className) != null;
        } catch (Exception ex) {
            return false;
        }
    }

    protected void success(ProviderCallback callback, String token, long expiration) {
        finish();
        GSObject response = new GSObject();
        response.put("providerToken", token);
        if (expiration != -1) response.put("providerTokenExpiration", expiration);
        callback.onResponse(response);
    }

    protected void fail(ProviderCallback callback, String errorMessage) {
        GSObject response = new GSObject();
        response.put("errorCode", 500023);
        response.put("errorMessage", errorMessage);
        fail(callback, response);
    }

    protected void fail(ProviderCallback callback, GSObject response) {
        finish();
        callback.onResponse(response);
    }

    protected void cancel(ProviderCallback callback) {
        finish();
        GSObject response = new GSObject();
        response.put("errorCode", 200001);
        response.put("errorMessage", "Operation canceled");
        callback.onResponse(response);
    }
}