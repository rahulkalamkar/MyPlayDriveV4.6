package com.gigya.socialize.android;

import android.app.Activity;
import android.text.TextUtils;

import com.gigya.socialize.GSArray;
import com.gigya.socialize.GSLogger;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.GSResponseListener;
import com.gigya.socialize.android.login.providers.GigyaWebViewProvider;
import com.gigya.socialize.android.login.providers.LoginProvider;
import com.gigya.socialize.android.login.providers.LoginProvider.ProviderCallback;

/**
 * Wrapper for the login process
 * Used internally by GSAPI
 */
public class GSLoginRequest {
    public enum LoginRequestType {login, addConnection, socialLogin}

    protected GSObject params;
    protected Object context;
    protected GSResponseListener responseListener; //user by Login, Connect
    protected LoginRequestType type;
    protected int id;
    protected String provider;
    protected Boolean silent;
    private String providerToken;
    private Activity activity;
    protected boolean canceled = true;
    public GSLogger logger = new GSLogger();

    protected GSLoginRequest(LoginRequestType type, final Activity activity, GSObject params, GSResponseListener responseListener, Object context) {
        this(type, activity, params, responseListener, false, context);
    }

    protected GSLoginRequest(LoginRequestType type, final Activity activity, GSObject params, GSResponseListener responseListener, Boolean silent, Object context) {
        this.type = type;
        this.activity = activity;
        this.responseListener = responseListener;
        this.context = context;
        this.params = params;
        this.silent = silent;

        if (params != null)
            this.provider = params.getString("provider", null);

        this.id = this.hashCode();
        logger.writeFormat("GSLoginRequest:\n\ttype=%s\\nprovider=%sn\tparams=%s", type, provider, params);
    }

    protected void send() throws IllegalArgumentException {
        if (provider == null || provider.equals("")) {
            throw new IllegalArgumentException("Missing \"provider\" parameter");
        }

        checkLoginProviderCompatibility();
        preProcessParameters();

        final String sProvider = params.getString("provider", "");
        String endPoint = null;
        if (type == LoginRequestType.login) {
            endPoint = "socialize.login";
        } else if (type == LoginRequestType.addConnection) {
            endPoint = "socialize.addConnection";
        } else if (type == LoginRequestType.socialLogin) {
            endPoint = "accounts.socialLogin";
        }

        params.put("endPoint", endPoint);
        params.put("gmid", GSAPI.getInstance().getGMID());

        final LoginProvider provider = GSAPI.getInstance().loginProviderFactory.getLoginProvider(sProvider);
        provider.login(activity, params, silent, new ProviderCallback() {
            @Override
            public void onResponse(GSObject response) {
                String providerToken = response.getString("providerToken", null);
                if (providerToken != null) { // SSO
                    ssoLogin(providerToken, response.getLong("providerExpiration", -1));
                } else {
                    onLoginResponse(response);
                }
            }
        });
    }

    private void checkLoginProviderCompatibility() throws IllegalArgumentException {
        if (provider.equals("facebook") && !GSAPI.getInstance().loginProviderFactory.hasLoginProvider("facebook")) {
            throw new IllegalArgumentException("Login with Facebook is supported only using Facebook SDK native login.");
        }
    }

    private void preProcessParameters() {
        if (GSAPI.getInstance().getConfig() == null) return;

        params.put("sdk", GSAPI.VERSION);
        params.put("ucid", GSAPI.getInstance().getUCID());

        GSObject permissions = GSAPI.getInstance().getConfig().getObject("permissions", null);
        if (permissions != null) {
            GSArray providerPerms = permissions.getArray(provider, null);

            if (providerPerms != null && providerPerms.length() != 0)
                params.put("defaultPermissions", TextUtils.join(",", providerPerms));
        }
    }

    private void ssoLogin(String providerToken, long providerExpiration) {
        GSObject params = this.params.clone();
        params.put("endPoint", this.params.getString("endPoint", "socialize.login"));
        params.put("provider", provider);
        params.put("providerToken", providerToken);
        this.providerToken = providerToken;
        if (providerExpiration != -1) params.put("providerExpiration", providerExpiration);

        GSSession session = GSAPI.getInstance().getSession();
        if (session != null && session.isValid())
            params.put("oauth_token", session.getToken());

        GigyaWebViewProvider webProvider = new GigyaWebViewProvider();
        webProvider.isTransparent = true;
        webProvider.login(activity, params, new ProviderCallback() {

            @Override
            public void onResponse(GSObject response) {
                onLoginResponse(response);
            }
        });
    }

    private void onLoginResponse(GSObject response) {
        int errorCode = response.getInt("errorCode", 0);
        String errorMessage = response.getString("errorMessage", GSResponse.getErrorMessage(errorCode));

        String regToken = response.getString("x_regToken", null);
        if (regToken != null) {
            response.remove("x_regToken");
            response.put("regToken", regToken);
        }

        String errorAndDesc = response.getString("error_description", null);
        if (errorAndDesc != null) {
            response.remove("error_description");
            response.remove("error");
            String[] parts = errorAndDesc.replace("+", "").split("-");
            errorCode = Integer.parseInt(parts[0].trim());
            errorMessage = parts[1];
        }

        if (errorCode == 0) {
            onSuccessResponse(response);
        } else {
            onFailResponse(response, errorCode, errorMessage);
        }
    }

    private void onFailResponse(GSObject response, int errorCode, String errorMessage) {
        if (responseListener != null) {
            final GSResponse finalResponse = new GSResponse(type.toString().toLowerCase(), response, errorCode, errorMessage, logger);
            String regToken = response.getString("regToken", null);

            // If error was pending registration - include profile data
            if (errorCode == 206001 && regToken != null) {
                GSObject params = new GSObject();
                params.put("regToken", regToken);

                GSAPI.getInstance().sendRequest("accounts.getAccountInfo", params, true, new GSResponseListener() {
                    @Override
                    public void onGSResponse(String method, GSResponse response, Object context) {
                        finalResponse.getData().put("data", response.getObject("data", null));
                        finalResponse.getData().put("profile", response.getObject("profile", null));
                        responseListener.onGSResponse(type.toString().toLowerCase(), finalResponse, context);
                    }
                }, null);
            } else {
                responseListener.onGSResponse(type.toString().toLowerCase(), finalResponse, context);
            }
        }
    }

    private void onSuccessResponse(GSObject response) {
        final GSAPI api = GSAPI.getInstance();

        GSSession newSession = api.getSession();
        if (response.containsKey("access_token")) {
            newSession = new GSSession(response);
        }

        api.setSession(newSession, provider, new GSResponseListener() {
            @Override
            public void onGSResponse(String method, GSResponse userInfoResponse, Object context) {
                if (responseListener != null) responseListener.onGSResponse(type.toString().toLowerCase(), userInfoResponse, context);

                if (userInfoResponse.getErrorCode() == 0) {
                    if (type.equals(LoginRequestType.login)) {
                        api.setLastLoginProvider(provider);
                    } else if (type.equals(LoginRequestType.addConnection)) {
                        api.invokeSocializeListeners("connectionAdded", provider, userInfoResponse.getData(), context);
                    }
                }
            }
        }, this.context);
    }
}

 
