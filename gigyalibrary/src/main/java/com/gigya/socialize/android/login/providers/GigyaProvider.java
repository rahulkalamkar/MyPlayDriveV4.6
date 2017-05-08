package com.gigya.socialize.android.login.providers;

import android.app.Activity;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.android.GSAPI;


public abstract class GigyaProvider extends LoginProvider {
    public String getUrl(boolean secure, String redirectUri, String endpoint, String apiDomain, GSObject params) {
        String provider = params.getString("provider", "").toLowerCase();
        GSObject serverParams = new GSObject();
        GSObject originalParams = params.clone();

        // Handling [provider]ExtraPermissions param
        String xperm = originalParams.getString(provider + "ExtraPermissions", null);
        if (xperm != null) {
            originalParams.remove(provider + "ExtraPermissions");
            serverParams.put("x_extraPermissions", xperm);
        }

        // General parameters
        serverParams.put("redirect_uri", redirectUri);
        serverParams.put("response_type", "token");
        serverParams.put("client_id", GSAPI.getInstance().getAPIKey());

        String gmid = originalParams.getString("gmid", null);
        if (gmid == null)
            serverParams.put("gmidTicket", originalParams.getString("gmidTicket", null));
        else
            serverParams.put("gmid", gmid);

        serverParams.put("ucid", originalParams.getString("ucid", null));

        originalParams.remove("gmidTicket");
        originalParams.remove("gmid");
        originalParams.remove("ucid");

        // Since there's no "browser session" with token
        originalParams.remove("sessionExpiration");

        if (endpoint.equals("socialize.addConnection")) {
            try {
                serverParams.put("oauth_token", GSAPI.getInstance().getSession().getToken());
                serverParams.put("getPerms", originalParams.getInt("getPerms", 0));
                originalParams.remove("getPerms");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            serverParams.put("x_secret_type", "oauth1");
        }

        // Add all other params as x_paramName
        for (String paramName : originalParams.getKeys()) {
            Object value = originalParams.get(paramName, null);

            if (paramName.startsWith("x_"))
                serverParams.put(paramName, value);
            else
                serverParams.put("x_" + paramName, value);
        }

        String protocol = "http";
        if (secure) {
            protocol = "https";
        }

        String domainPrefix = "socialize";
        return String.format("%s://%s.%s/%s?%s", protocol, domainPrefix, apiDomain, endpoint, GSRequest.buildQS(serverParams));
    }

    public abstract void login(final Activity activity, final GSObject params, final LoginProvider.ProviderCallback callback);

    public void login(final Activity activity, final GSObject params, Boolean silent, final LoginProvider.ProviderCallback callback) {
        if (silent) {
            fail(callback, "Silent login is not supported for this provider.");
        } else {
            login(activity, params, callback);
        }
    }

    @Override
    protected void finish() {
    }
}
