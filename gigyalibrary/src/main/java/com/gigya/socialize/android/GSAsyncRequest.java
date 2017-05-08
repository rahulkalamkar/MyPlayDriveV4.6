package com.gigya.socialize.android;

import android.os.AsyncTask;

import com.gigya.socialize.GSLogger;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.GSResponseListener;
import com.gigya.socialize.android.login.providers.LoginProvider;

/**
 * Async version of GSRequest, uses android.os.AsyncTask
 */
class GSAsyncRequest extends GSRequest {
    private GSResponseListener responseListener;
    private int timeoutMS;

    public GSAsyncRequest(String apiKey, String secret, String method, GSObject params, boolean useHTTPS, int timeoutMS, GSLogger logger) {
        super(apiKey, secret, null, method, params, useHTTPS);
        this.setLogger(logger);
        this.timeoutMS = timeoutMS;

        // load timestamp offset from storage
        GSRequest.timestampOffsetSec = GSAPI.getInstance().loadTimestampOffset();
    }

    @Override
    public void send(GSResponseListener listener, Object context) {
        responseListener = listener;
        beforeSend();

        new GSRequestTask(new GSResponseListener() {
            @Override
            public void onGSResponse(String method, GSResponse response, Object context) {
                afterResponse(response, context);

                if (responseListener != null) {
                    responseListener.onGSResponse(method, response, context);
                }
            }
        }, context).execute(this);
    }

    private void beforeSend() {
        if (apiMethod.contains("accounts.login") || apiMethod.contains("notifyLogin")) {
            GSAPI.getInstance().clearSession();
        }
    }

    private void afterResponse(GSResponse response, Object context) {
        if (!response.hasData()) return;

        if (response.getErrorCode() == 0 && this.apiMethod.contains(".logout")) {
            GSAPI.getInstance().clearSession();
            GSAPI.getInstance().invokeSocializeListeners("logout", context);
            GSAPI.getInstance().invokeAccountsListeners("logout", context);
        } else if (response.getErrorCode() == 0 &&
                (response.getObject("sessionInfo", null) != null) || (response.getString("sessionToken", null) != null)) {
            GSObject sessionInfo = response.getObject("sessionInfo", null);
            if (sessionInfo == null)
                sessionInfo = response.getData();

            String token = sessionInfo.getString("sessionToken", null);
            String secret = sessionInfo.getString("sessionSecret", null);
            long expiration = sessionInfo.getLong("expires_in", -1);

            // Getting "0" from the server means token should be valid for all eternity
            if (expiration == 0)
                expiration = -1;

            if (token != null && secret != null) {
                String provider = getParams().getString("provider", "site");
                GSSession newSession = new GSSession(token, secret, expiration);
                GSAPI.getInstance().setSession(newSession, provider, null, null);
            }
        } else if (response.getErrorCode() == 0 && this.apiMethod.contains(".removeConnection")) {
            String providerName = getParams().getString("provider", "");
            LoginProvider nativeProvider = GSAPI.getInstance().loginProviderFactory.getLoginProvider(providerName);
            nativeProvider.clearSession();
        }
    }

    private class GSRequestTask extends AsyncTask<GSRequest, Void, GSResponse> {
        GSResponseListener asyncListener;
        Object context;

        public GSRequestTask(GSResponseListener listener, Object context) {
            this.asyncListener = listener;
            this.context = context;
        }

        protected void onPostExecute(GSResponse response) {
            GSAPI.getInstance().showProgress(false);
            if (this.asyncListener != null) {
                if (response.getErrorCode() != 0) {
                    GSAPI.err(response.getLog());
                } else {
                    GSAPI.debug(response.getLog());
                }

                if (getParams().getBool("reportError", true)) {
                    GSAPI.getInstance().reportError(apiMethod, response);
                }

                this.asyncListener.onGSResponse(apiMethod, response, context);
            }
        }

        @Override
        protected GSResponse doInBackground(GSRequest... params) {
            GSRequest req = null;
            try {
                req = params[0];
                GSResponse res = req.send(timeoutMS);

                // save timestamp offset to storage
                GSAPI.getInstance().saveTimestampOffset(GSRequest.timestampOffsetSec);

                return res;
            } catch (Exception ex) {
                if (req != null) {
                    return new GSResponse(req.getMethod(), req.getParams(), 500000, ex.toString(), req.getLogger());
                } else {
                    return new GSResponse("", null, 500000, ex.toString(), null);
                }
            }
        }
    }
}
