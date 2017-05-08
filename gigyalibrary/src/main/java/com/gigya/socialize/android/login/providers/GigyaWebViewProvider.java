package com.gigya.socialize.android.login.providers;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.android.GSAPI;
import com.gigya.socialize.android.ui.HostActivity;
import com.gigya.socialize.android.ui.WebViewFragment;


public class GigyaWebViewProvider extends GigyaProvider {
    private WebViewFragment webviewDialog;
    public Boolean isTransparent = false;

    @Override
    public void login(final Activity activity, final GSObject params, final ProviderCallback callback) {
        this.createActivity(new HostActivity.HostActivityHandler() {
            @Override
            public void onCreate(final FragmentActivity activity, Bundle savedInstanceState) {
                String fragmentTag = params.getString("provider", "provider") + "WebViewFragment";

                if (activity.getSupportFragmentManager().findFragmentByTag(fragmentTag) == null) {
                    String url = getUrl(true, "gsapi://login_result", params.getString("endPoint", "socialize.login"), GSAPI.getInstance().getAPIDomain(), params);
                    if(!TextUtils.isEmpty(url)) {
                        webviewDialog = WebViewFragment.create(activity, fragmentTag, params.getString("captionText", ""), url,
                                "gsapi://login_result", new WebViewFragment.WebViewFragmentHandler() {
                                    @Override
                                    public void onResult(GSObject result) {
                                        callback.onResponse(result);
                                        activity.finish();
                                    }
                                }, isTransparent);
                        webviewDialog.setRetainInstance(true);
                    }
                }
            }

            @Override
            public void onActivityResult(FragmentActivity activity, int requestCode,
                                         int resultCode, Intent data) {

            }

            @Override
            public void onCancel(FragmentActivity activity) {
                cancel(callback);
            }

            @Override
            public void onStart(FragmentActivity activity) {
            }
        });
    }
}
