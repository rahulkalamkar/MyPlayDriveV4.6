package com.gigya.socialize.android.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.android.GSAPI;
import com.gigya.socialize.android.GSLoginRequest.LoginRequestType;
import com.gigya.socialize.android.ui.HostActivity;
import com.gigya.socialize.android.ui.HostActivity.HostActivityHandler;
import com.gigya.socialize.android.ui.WebViewFragment;
import com.gigya.socialize.android.ui.WebViewFragment.WebViewFragmentHandler;


public class ProviderSelection {
    public interface ProviderSelectionHandler {
        public void onSelect(ProviderSelection selector, FragmentActivity activity, String provider, String providerDisplayName);

        public void onCancel(ProviderSelection selector, FragmentActivity activity);

        public void onShow(ProviderSelection selector, FragmentActivity activity);

        public void onError(ProviderSelection selector, FragmentActivity activity, GSObject error);
    }

    private static final String GSAPIURL_RESULT = "gsapi://result/";
    private boolean disableSelection = false;
    private WebViewFragment fragment;
    private Integer hostActivityId;

    public void setDisableSelection(boolean disableSelection) {
        this.disableSelection = disableSelection;
    }

    public String getUrl(LoginRequestType mode, GSObject params) {
        GSObject serverParams = new GSObject();
        serverParams.put("apiKey", GSAPI.getInstance().getAPIKey());
        serverParams.put("requestType", mode.toString());
        serverParams.put("enabledProviders", params.getString("enabledProviders", null));
        serverParams.put("disabledProviders", params.getString("disabledProviders", null));
        serverParams.put("lang", params.getString("lang", null));
        serverParams.put("cid", params.getString("cid", null));
        serverParams.put("sdk", GSAPI.VERSION);
        serverParams.put("lastLoginProvider", GSAPI.getInstance().getLastLoginProvider());
        serverParams.put("redirect_uri", GSAPIURL_RESULT);
        serverParams.put("sdk", GSAPI.VERSION);

        if (mode.equals(LoginRequestType.addConnection)) {
            serverParams.put("oauth_token", GSAPI.getInstance().getSession().getToken());
        }

        String endpoint = "gs/mobile/loginui.aspx";
        String protocol = "https";
        String domainPrefix = "socialize";
        String qs = GSRequest.buildQS(serverParams);
        return String.format("%s://%s.%s/%s?%s", protocol, domainPrefix, GSAPI.getInstance().getAPIDomain(), endpoint, qs);
    }

    public void show() {
        HostActivity activity = HostActivity.getActivity(hostActivityId);
        fragment.show(activity);
    }

    public void finish() {
        HostActivity activity = HostActivity.getActivity(hostActivityId);
        activity.finish();
    }

    public void showProgressDialog(String title) {
        HostActivity activity = HostActivity.getActivity(hostActivityId);
        activity.showProgressDialog(title);
    }

    public void dismissProgressDialog() {
        HostActivity activity = HostActivity.getActivity(hostActivityId);
        activity.dismissProgressDialog();
    }

    public void show(final LoginRequestType mode, final GSObject params, final ProviderSelectionHandler callback) {
        hostActivityId = HostActivity.create(GSAPI.getInstance().getContext(), new HostActivityHandler() {

            @Override
            public void onCreate(final FragmentActivity activity, Bundle savedInstanceState) {
                if (savedInstanceState == null) {
                    String defaultTitle = mode.equals(LoginRequestType.addConnection) ? "Add A Connection" : "Sign In";
                    String title = params.getString("captionText", defaultTitle);

                    fragment = (WebViewFragment) activity.getSupportFragmentManager().findFragmentByTag("GigyaWebViewFragment");
                    if (fragment == null) {
                        fragment = WebViewFragment.create(activity, "GigyaWebViewFragment", title, getUrl(mode, params), GSAPIURL_RESULT, new WebViewFragmentHandler() {
                            @Override
                            public void onResult(GSObject result) {
                                if (result.getInt("errorCode", 0) != 0) {
                                    callback.onError(ProviderSelection.this, activity, result);
                                } else if (!disableSelection) {
                                    callback.onSelect(ProviderSelection.this, activity, result.getString("provider", ""), result.getString("displayName", ""));
                                }
                            }
                        }, false);
                        fragment.setRetainInstance(true);
                    }
                }
            }

            @Override
            public void onCancel(FragmentActivity activity) {
                callback.onCancel(ProviderSelection.this, activity);
            }

            @Override
            public void onActivityResult(FragmentActivity activity, int requestCode,
                                         int resultCode, Intent data) {
            }

            @Override
            public void onStart(final FragmentActivity activity) {
                callback.onShow(ProviderSelection.this, activity);
            }
        });
    }
}
