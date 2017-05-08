package com.gigya.socialize.android.login.providers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.android.GSAPI;
import com.gigya.socialize.android.event.GSAndroidPermissionListener;
import com.gigya.socialize.android.ui.HostActivity.HostActivityHandler;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GooglePlusProvider extends LoginProvider {
    private static int GPLUS_CODE_RESOLVE_ERR = 32667;

    GoogleApiClient googleClient;
    Activity currentActivity;

    public GooglePlusProvider() throws Exception {
    }

    public static boolean isConfigured() {
        try {
            Context context = GSAPI.getInstance().getContext();
            Boolean exists = isClassExist("com.google.android.gms.common.GooglePlayServicesUtil") &&
                    GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
            return (exists && Build.VERSION.SDK_INT >= 8);
        } catch (Throwable t) {
            return false;
        }
    }

    public void login(final Activity activity, final GSObject params, final Boolean silent, final ProviderCallback callback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            int requestCode = GSAPI.getInstance().getNextAndroidPermissionsRequestCode(
                    new GSAndroidPermissionListener() {
                        @Override
                        public void onAndroidPermissionsResult(String permissions[], int[] grantResults) {
                            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                login(activity, params, silent, callback);
                            } else {
                                GSObject error = new GSObject();
                                error.put("errorCode", 403007);
                                error.put("errorMessage", "Login failed - user denied permission to get Google+ accounts");
                                fail(callback, error);
                            }
                        }
                    });

            activity.requestPermissions(new String[]{Manifest.permission.GET_ACCOUNTS}, requestCode);
            return;
        }

        if (silent) {
            trySilentLogin(activity, params, callback);
        } else {
            if (currentActivity != null) currentActivity.finish();
            this.createActivity(new HostActivityHandler() {
                @Override
                public void onCreate(FragmentActivity fragmentActivity, Bundle savedInstanceState) {
                    currentActivity = fragmentActivity;
                    googleClient = buildGoogleApiClient(params, false, callback);
                    googleClient.connect();
                }

                @Override
                public void onActivityResult(FragmentActivity fragmentActivity, int requestCode, int resultCode, Intent data) {
                    if (requestCode == GPLUS_CODE_RESOLVE_ERR) {
                        if (resultCode == Activity.RESULT_OK) { // resolved error
                            googleClient.connect();
                        } else if (resultCode == Activity.RESULT_CANCELED) {
                            cancel(callback);
                        } else {
                            fail(callback, data.toString());
                        }
                    }
                }

                @Override
                public void onCancel(FragmentActivity fragmentActivity) {
                    cancel(callback);
                }

                @Override
                public void onStart(FragmentActivity fragmentActivity) {
                }
            });
        }
    }

    private GoogleApiClient buildGoogleApiClient(final GSObject params, final Boolean silent, final ProviderCallback callback) {
        GoogleApiClient.Builder builder = new GoogleApiClient.Builder(GSAPI.getInstance().getContext(),
                new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        getGplusToken(params, getGplusAccount(), silent, callback);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                },
                new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        if (!silent) {
                            tryResolveConnectionFailed(connectionResult, callback);
                        } else {
                            GSObject error = new GSObject();
                            error.put("errorCode", 403012);
                            error.put("errorMessage", "Login failed - user has not authorized Google+ app.");
                            fail(callback, error);
                        }
                    }
                });

        builder.addApi(Plus.API);

        for (String scope : this.getScopes(params)) {
            builder.addScope(new Scope(scope.trim()));
        }

        return builder.build();
    }

    public void trySilentLogin(final Activity activity, final GSObject params, final ProviderCallback callback) {
        googleClient = buildGoogleApiClient(params, true, callback);
        googleClient.connect();
    }

    private void tryResolveConnectionFailed(final ConnectionResult result, final ProviderCallback callback) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(currentActivity, GPLUS_CODE_RESOLVE_ERR);
            } catch (SendIntentException e) {
                fail(callback, e.getMessage());
            }
        } else {
            fail(callback, result.toString());
        }
    }

    private String getGPlusTokenBlocking(GSObject params, String email) throws UserRecoverableAuthException, IOException, GoogleAuthException {
        String scope = "oauth2:" + TextUtils.join(" ", this.getScopes(params));
        return GoogleAuthUtil.getToken(GSAPI.getInstance().getContext(), email, scope);
    }

    private List<String> getScopes(GSObject params) {
        List<String> scopes = new ArrayList<String>(Arrays.asList(params.getString("defaultPermissions",
                Scopes.PLUS_LOGIN + ",https://www.googleapis.com/auth/userinfo.email").split(",")));
        String extraPermissionsParam = params.getString("googlePlusExtraPermissions", null);
        if (extraPermissionsParam != null) {
            List<String> extraPermissions = Arrays.asList(extraPermissionsParam.split(","));
            scopes.addAll(extraPermissions);
        }

        return scopes;
    }

    private boolean validateGplusToken(String token) throws Exception {
        URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token=" + token);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        int serverCode = con.getResponseCode();
        if (serverCode == 401) {
            return false;
        }

        return true;
    }

    private String getGplusAccount() {
        return Plus.AccountApi.getAccountName(googleClient);
    }

    private void getGplusToken(final GSObject params, final String email, final Boolean silent, final ProviderCallback callback) {
        new AsyncTask<Void, Void, Object>() {
            protected Object doInBackground(Void... params1) {
                String token = null;
                try {
                    token = getGPlusTokenBlocking(params, email);
                } catch (UserRecoverableAuthException authEx) {
                    return authEx;
                } catch (IOException e) {} catch (GoogleAuthException e) {}
                try {
                    if (token != null && !validateGplusToken(token)) {
                        GoogleAuthUtil.invalidateToken(GSAPI.getInstance().getContext(), token);
                        token = getGPlusTokenBlocking(params, email);
                    }
                    return token;
                } catch (Exception e) {
                    return e;
                }
            }

            protected void onPostExecute(final Object result) {
                if (result != null) {
                    if (result instanceof UserRecoverableAuthException && !silent) {
                        if (googleClient.isConnected()) // fix issue when GPlus activity is re-opened
                            googleClient.disconnect();

                        currentActivity.startActivityForResult(((UserRecoverableAuthException) result).getIntent(), GPLUS_CODE_RESOLVE_ERR);
                    } else if (result instanceof Exception) {
                        fail(callback, ((Exception) result).toString());
                    } else if (result instanceof String) {
                        success(callback, (String) result, -1);
                    } else {
                        fail(callback, "");
                    }
                } else {
                    fail(callback, "");
                }
            }
        }.execute();
    }

    @Override
    protected void finish() {
        if (googleClient != null && googleClient.isConnected()) {
            googleClient.disconnect();
        }

        if (currentActivity != null) {
            currentActivity.finish();
            currentActivity = null;
        }
    }
}
