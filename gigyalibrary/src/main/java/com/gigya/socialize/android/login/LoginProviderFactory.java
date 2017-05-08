package com.gigya.socialize.android.login;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.android.GSAPI;
import com.gigya.socialize.android.login.providers.FacebookProvider;
import com.gigya.socialize.android.login.providers.GigyaBrowserProvider;
import com.gigya.socialize.android.login.providers.GigyaProvider;
import com.gigya.socialize.android.login.providers.GigyaWebViewProvider;
import com.gigya.socialize.android.login.providers.GooglePlusProvider;
import com.gigya.socialize.android.login.providers.LoginProvider;

import java.util.concurrent.ConcurrentHashMap;


public class LoginProviderFactory {
    private ConcurrentHashMap<String, LoginProvider> providers = new ConcurrentHashMap<String, LoginProvider>();
    private GigyaProvider webProvider;

    public LoginProviderFactory() {
        if (FacebookProvider.isConfigured()) {
            try {
                providers.put("facebook", new FacebookProvider());
            } catch (Exception e) {
            }
        }
        if (GooglePlusProvider.isConfigured()) {
            try {
                providers.put("googleplus", new GooglePlusProvider());
            } catch (Exception e) {

            }
        }

        updateWebProvider();
    }

    public void updateWebProvider() {
        GSAPI.LoginBehavior behavior = GSAPI.getInstance().getLoginBehavior();
        if (behavior == GSAPI.LoginBehavior.BROWSER) {
            webProvider = new GigyaBrowserProvider();
        } else if (behavior == GSAPI.LoginBehavior.WEBVIEW_DIALOG) {
            webProvider = new GigyaWebViewProvider();
        }
    }

    public void validatePermissions(GSObject config) {
        for (String providerName : providers.keySet()) {
            GSObject permissions = config.getObject("permissions", null);
            Boolean hasPermissions = permissions != null && permissions.getArray(providerName, null) != null;

            if (!hasPermissions) {
                providers.remove(providerName);
            }
        }
    }

    public ConcurrentHashMap<String, LoginProvider> getLoginProviders() {
        return providers;
    }

    public LoginProvider getLoginProvider(String name) {
        LoginProvider provider = providers.get(name.toLowerCase());
        if (provider != null) {
            return providers.get(name.toLowerCase());
        } else {
            return webProvider;
        }
    }

    public boolean hasLoginProvider(String name) {
        LoginProvider provider = providers.get(name.toLowerCase());
        return (provider != null);
    }
}
