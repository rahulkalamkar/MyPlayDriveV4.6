package com.gigya.socialize.android.login.providers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.DefaultAudience;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.GSResponseListener;
import com.gigya.socialize.android.GSAPI;
import com.gigya.socialize.android.GSPermissionResultHandler;
import com.gigya.socialize.android.GSSession;
import com.gigya.socialize.android.ui.HostActivity.HostActivityHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class FacebookProvider extends LoginProvider {
    private static final String[] PUBLISH_PERMISSIONS = {"ads_management", "create_event", "manage_friendlists", "manage_notifications",
            "publish_actions", "publish_stream", "rsvp_event", "publish_pages", "manage_pages"};
    private static final String[] DEFAULT_FACEBOOK_READ_PERMISSIONS = {"email"};
    private GSPermissionResultHandler permissionsHandler;
    private List<String> requestedPermissions;
    private FragmentActivity permissionsActivity;
    private LoginManager fbLoginManager;
    private CallbackManager fbCallbackManager;


    @Override
    public void clearSession() {
        if (getAccessToken() != null)
            fbLoginManager.logOut();
    }

    public FacebookProvider() throws Exception {
        try {
            FacebookSdk.sdkInitialize(GSAPI.getInstance().getContext());
            fbLoginManager = LoginManager.getInstance();
            fbCallbackManager = CallbackManager.Factory.create();

            // listen to access-token changes
            new AccessTokenTracker() {
                @Override
                protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                    if (permissionsActivity != null) {
                        permissionsActivity.finish();
                        permissionsActivity = null;
                    }

                    permissionsHandler = null;

                    GSSession session = GSAPI.getInstance().getSession();
                    if (!isLoggedIn() || session == null || !session.isValid())
                        return;

                    reportExtendedToken(currentAccessToken);
                }
            };
        } catch (Exception ex) {
            // error while initiating FacebookSDK
        }
    }

    public static boolean isConfigured() {
        try {
            Context context = GSAPI.getInstance().getContext();
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String fbAppId = (String) appInfo.metaData.get("com.facebook.sdk.ApplicationId");
            return (isClassExist("com.facebook.login.LoginManager") && fbAppId != null);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isLoggedIn() {
        return (getAccessToken() != null);
    }

    public void login(final Activity activity, final GSObject params, Boolean silent, final ProviderCallback callback) {
        if (silent) {
            fail(callback, "Silent login is not supported for this provider.");
            return;
        }

        final List<String> permissions = mergeLoginReadPermissions(params);

        AccessToken fbAccessToken = getAccessToken();
        if (fbAccessToken != null && isPermissionsGranted(permissions)) {
            success(callback, fbAccessToken.getToken(), fbAccessToken.getExpires().getTime());
            return;
        }

        final AccessToken fbAccessTokenBeforeLogin = fbAccessToken;

        this.createActivity(new HostActivityHandler() {
            @Override
            public void onCreate(final FragmentActivity activity, Bundle savedInstanceState) {
                try {
                    fbLoginManager.setLoginBehavior(extractLoginBehavior(params));

                    fbLoginManager.registerCallback(fbCallbackManager,
                            new FacebookCallback<LoginResult>() {
                                @Override
                                public void onSuccess(LoginResult loginResult) {
                                    activity.finish();

                                    GSSession session = GSAPI.getInstance().getSession();
                                    AccessToken fbAccessToken = getAccessToken();
                                    boolean appChanged = true;
                                    boolean userChanged = true;
                                    if (fbAccessTokenBeforeLogin != null) {
                                        appChanged = !fbAccessToken.getApplicationId().equals(fbAccessTokenBeforeLogin.getApplicationId());
                                        userChanged = !fbAccessToken.getUserId().equals(fbAccessTokenBeforeLogin.getUserId());
                                    }

                                    if (appChanged || userChanged || session == null || !session.isValid())
                                        success(callback, fbAccessToken.getToken(), fbAccessToken.getExpires().getTime());
                                    else
                                        reportExtendedToken(fbAccessToken);

                                    fbLoginManager.registerCallback(fbCallbackManager, null);
                                }

                                @Override
                                public void onCancel() {
                                    activity.finish();
                                    cancel(callback);
                                    fbLoginManager.registerCallback(fbCallbackManager, null);
                                }

                                @Override
                                public void onError(FacebookException exception) {
                                    activity.finish();
                                    fail(callback, exception.getMessage());
                                    fbLoginManager.registerCallback(fbCallbackManager, null);
                                }
                            });

                    fbLoginManager.logInWithReadPermissions(activity, permissions);
                } catch (Exception exception) {
                    activity.finish();
                    fail(callback, exception.getMessage());
                }
            }

            @Override
            public void onActivityResult(FragmentActivity activity, int requestCode, int resultCode, Intent data) {
                fbCallbackManager.onActivityResult(requestCode, resultCode, data);
            }

            @Override
            public void onCancel(FragmentActivity activity) {
                cancel(callback);
            }

            @Override
            public void onStart(FragmentActivity activity) {}
        });
    }

    private LoginBehavior extractLoginBehavior(GSObject params) {
        String defaultBehavior = "SSO_WITH_FALLBACK";//"NATIVE_WITH_FALLBACK";//
//        if (GSAPI.getInstance().getLoginBehavior() == GSAPI.LoginBehavior.WEBVIEW_DIALOG) {
//            defaultBehavior = "WEB_ONLY";
//        }

        String loginBehavior = params.getString("facebookLoginBehavior", defaultBehavior);
        return LoginBehavior.valueOf(loginBehavior);
    }

    public void requestPermissions(final String type, final List<String> permissions, final GSPermissionResultHandler callback) {
        AccessToken fbAccessToken = getAccessToken();
        if (fbAccessToken == null) {
            callback.onResult(false, null, null); // TODO: write to logger: "Facebook session is closed, you must login first"
        } else {
            if (isPermissionsGranted(permissions)) {
                callback.onResult(true, null, new ArrayList<String>());
            } else {
                permissionsHandler = callback;
                this.createActivity(new HostActivityHandler() {
                    @Override
                    public void onCreate(final FragmentActivity activity, Bundle savedInstanceState) {
                        FacebookProvider.this.permissionsActivity = activity;
                        requestedPermissions = new ArrayList<String>(permissions);
                        fbLoginManager.setDefaultAudience(DefaultAudience.FRIENDS);

                        if (type.equals("publish"))
                            fbLoginManager.logInWithPublishPermissions(activity, permissions);
                        else
                            fbLoginManager.logInWithReadPermissions(activity, permissions);
                    }

                    @Override
                    public void onActivityResult(FragmentActivity activity, int requestCode, int resultCode, Intent data) {
                        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
                    }

                    @Override
                    public void onCancel(FragmentActivity activity) {
                        callback.onResult(false, null, null);
                    }

                    @Override
                    public void onStart(FragmentActivity activity) {}
                });
            }
        }
    }

    public void reportDeepLink(Intent intent) {
        if (intent.getAction().contains("com.facebook.application")) {
            GSAPI.getInstance().reportURIReferral(intent.getData(), "facebook");
        }
    }

    private void reportExtendedToken(final AccessToken fbAccessToken) {
        GSObject params;
        try {
            params = new GSObject();
            params.put("providerSession", "{\"facebook\": { \"authToken\": \"" + fbAccessToken.getToken() + "\", \"tokenExpiration\": " + fbAccessToken.getExpires().getTime() + "}}");
            GSAPI.getInstance().sendRequest("refreshProviderSession", params, new GSResponseListener() {
                @Override
                public void onGSResponse(String method, GSResponse response, Object context) {
                    if (permissionsHandler != null) {
                        Boolean granted = true;

                        if (response.getErrorCode() == 0) {
                            List<String> declinedPermissions = new ArrayList<String>();
                            if (requestedPermissions != null) {
                                declinedPermissions = getDeclinedPermissionsFromArray(requestedPermissions);

                                if (requestedPermissions.size() == declinedPermissions.size())
                                    granted = false;
                            }

                            permissionsHandler.onResult(granted, null, declinedPermissions);
                        } else {
                            permissionsHandler.onResult(false, new Exception(response.getErrorMessage()), null);
                        }
                    }
                }
            }, null);
        } catch (Exception e) {
            if (permissionsHandler != null)
                permissionsHandler.onResult(false, null, null);
        }
    }

    private boolean isPermissionsGranted(List<String> permissions) {
        AccessToken fbAccessToken = getAccessToken();
        Set<String> grantedPermissions = fbAccessToken.getPermissions();

        for (String permission : permissions) {
            if (!grantedPermissions.contains(permission))
                return false;
        }
        return true;
    }

    private List<String> getDeclinedPermissionsFromArray(List<String> permissions) {
        List<String> declinedPermissions = new ArrayList<String>();
        Set<String> allDeclinedPermissions = getAccessToken().getDeclinedPermissions();

        for (String permission : permissions) {
            if (allDeclinedPermissions.contains(permission))
                declinedPermissions.add(permission);
        }

        return declinedPermissions;
    }

    private List<String> mergeLoginReadPermissions(final GSObject params) {
        List<String> permissions = new ArrayList<String>();
        permissions.addAll(Arrays.asList(DEFAULT_FACEBOOK_READ_PERMISSIONS));

        String defaultPermissionsParam = params.getString("defaultPermissions", null);
        if (defaultPermissionsParam != null)
            mergePermissions(permissions, Arrays.asList(defaultPermissionsParam.split(",")));

        String extraPermissionsParam = params.getString("facebookReadPermissions", null);
        if (extraPermissionsParam != null)
            mergePermissions(permissions, Arrays.asList(extraPermissionsParam.split(",")));

        permissions.removeAll(Arrays.asList(PUBLISH_PERMISSIONS));

        return permissions;
    }

    private List<String> mergePermissions(List<String> permissions, List<String> extraPermissions) {
        for (String permission : extraPermissions) {
            if (!permissions.contains(permission))
                permissions.add(permission);
        }
        return permissions;
    }

    private static AccessToken getAccessToken() {
        return AccessToken.getCurrentAccessToken();
    }

    @Override
    protected void finish() {}
}