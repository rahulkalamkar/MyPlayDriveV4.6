package com.gigya.socialize.android.login.providers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.GSResponseListener;
import com.gigya.socialize.android.GSAPI;


public class GigyaBrowserProvider extends GigyaProvider {

    @Override
    public void login(final Activity activity, final GSObject params, final LoginProvider.ProviderCallback callback) {
        // Removing the gmid and getting a gmidTicket from the server - in order to not pass the gmid to the browser
        params.remove("gmid");
        GSAPI.getInstance().sendRequest("socialize.getGmidTicket", null, true, new GSResponseListener() {
            @Override
            public void onGSResponse(String method, GSResponse response, Object context) {
                if (response.getErrorCode() == 0) {
                    params.put("gmidTicket", response.getString("gmidTicket", ""));
                }

                String url = getUrl(true, GSAPI.getInstance().getContext().getPackageName() + "://gsapi/login_result",
                        params.getString("endPoint", "socialize.login"), GSAPI.getInstance().getAPIDomain(), params);

                WebLoginActivity.setCallback(new WebLoginActivity.WebLoginActivityCallback() {
                    @Override
                    public void onResponse(GSObject response) {
                        if (callback != null)
                            callback.onResponse(response);
                    }
                });

                Context appContext = GSAPI.getInstance().getContext();
                Intent loginIntent = new Intent(appContext, WebLoginActivity.class);
                loginIntent.putExtra("url", url);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(loginIntent);
            }
        }, null);
    }
}
