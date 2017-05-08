package com.gigya.socialize.android;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.gigya.socialize.GSArray;
import com.gigya.socialize.GSLogger;
import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.GSResponseListener;
import com.gigya.socialize.android.GSLoginRequest.LoginRequestType;
import com.gigya.socialize.android.event.GSAccountsEventListener;
import com.gigya.socialize.android.event.GSAndroidPermissionListener;
import com.gigya.socialize.android.event.GSConnectUIListener;
import com.gigya.socialize.android.event.GSDialogListener;
import com.gigya.socialize.android.event.GSEventListener;
import com.gigya.socialize.android.event.GSLoginUIListener;
import com.gigya.socialize.android.event.GSPluginListener;
import com.gigya.socialize.android.event.GSSocializeEventListener;
import com.gigya.socialize.android.event.GSUIListener;
import com.gigya.socialize.android.login.LoginProviderFactory;
import com.gigya.socialize.android.login.ProviderSelection;
import com.gigya.socialize.android.login.ProviderSelection.ProviderSelectionHandler;
import com.gigya.socialize.android.login.providers.FacebookProvider;
import com.gigya.socialize.android.login.providers.LoginProvider;
import com.gigya.socialize.android.ui.HostActivity;
import com.gigya.socialize.android.ui.HostActivity.HostActivityHandler;
import com.gigya.socialize.android.ui.PluginPresentor;
import com.gigya.socialize.android.utils.SimpleRunnableQueue;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.impl.client.DefaultHttpClient;

import java.lang.reflect.Method;
import java.net.URL;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * The central class of the Gigya Android SDK. Instances of the GSAPI class provide access to the Gigya service.
 */
public class GSAPI {
    /**
     * An enum specifying the desired login behavior.
     */
    public enum LoginBehavior {
        /**
         * Login will switch to the browser then return to the app (default).
         */
        BROWSER,

        /**
         * Login will show a web view dialog and won't leave the app.
         */
        WEBVIEW_DIALOG,
    }

    public static final String VERSION = "android_3.3.0";
    protected static String LOGTAG = "GSAPI";
    private static final String DEFAULT_API_DOMAIN = "us1.gigya.com";

    /**
     * Determines whether tracing mode is activated.
     */
    public static boolean OPTION_TRACE = false;
    /**
     * Determines whether Internet connectivity will be checked before requests.
     */
    public static boolean OPTION_CHECK_CONNECTIVITY = true;
    /**
     * Determines whether a progress bar will be displayed upon API requests.
     */
    public static boolean OPTION_SHOW_PROGRESS_ON_REQUEST = false;
    /**
     * Determines the time in milliseconds before a request times out. Default is 20000 ms.
     */
    public static int OPTION_REQUEST_TIMEOUT_MS = 20000;
    /**
     * Determines whether requests will be sent over HTTPS or HTTP. Default is HTTPS (true)
     */
    public static boolean OPTION_HTTPS_ENABLED = true;

    public static boolean __DEBUG_OPTION_ENABLE_TEST_NETWORKS = false;


    private static GSAPI instance = null;
    protected LoginProviderFactory loginProviderFactory;
    private Context appContext;
    private GSObject config;
    private GSSession session;
    private String apiDomain;
    private String apiKey;
    private String ucid;
    private String gmid;
    private SharedPreferences settings;
    private ProgressDialog progress;
    private FragmentActivity progressActivity;
    private GSEventListener eventListener;
    private GSSocializeEventListener socializeEventListener;
    private GSAccountsEventListener accountsEventListener;
    private ArrayList<GSSocializeEventListener> socializeEventListenersArray;
    private ArrayList<GSAccountsEventListener> accountsEventListenersArray;
    private SimpleRunnableQueue requestsQueue;
    private LoginBehavior loginBehavior = LoginBehavior.BROWSER;
    private int androidPermissionsRequestCode = 10000000;
    private Map<Integer, GSAndroidPermissionListener> androidPermissionListeners;

    // Singleton Methods
    private GSAPI() {
        socializeEventListenersArray = new ArrayList<GSSocializeEventListener>();
        accountsEventListenersArray = new ArrayList<GSAccountsEventListener>();
        requestsQueue = new SimpleRunnableQueue();
        androidPermissionListeners = new HashMap<Integer, GSAndroidPermissionListener>();
    }

    /**
     * Returns the GSAPI singleton instance.
     */
    public static GSAPI getInstance() {
        if (instance == null) {
            instance = new GSAPI();
        }

        return instance;
    }

    // Properties

    /**
     * Retrieves the current session.
     */
    public GSSession getSession() {
        return this.session;
    }

    protected void setSession(GSSession session, final String loginProvider, final GSResponseListener userInfoListener, final Object context) {
        GSSession currentSession = this.session;
        final boolean sessionChanged = !(currentSession != null && session != null && currentSession.getToken().equals(session.getToken()));

        this.session = session;
        saveSession();

        if (userInfoListener != null || eventListener != null || !socializeEventListenersArray.isEmpty()) {
            GSObject params = new GSObject();
            if (GSAPI.__DEBUG_OPTION_ENABLE_TEST_NETWORKS)
                params.put("enabledProviders", "*,testnetwork3,testnetwork4");

            sendRequest("socialize.getUserInfo", params, new GSResponseListener() {
                @Override
                public void onGSResponse(String method, GSResponse response, Object context) {
                    if (userInfoListener != null) {
                        userInfoListener.onGSResponse(method, response, context);
                    }

                    if (response.getErrorCode() == 0 && sessionChanged) {
                        invokeSocializeListeners("login", loginProvider, response.getData(), context);
                    }
                }
            }, context);
        }

        if (!accountsEventListenersArray.isEmpty()) {
            GSObject params = new GSObject();
            if (GSAPI.__DEBUG_OPTION_ENABLE_TEST_NETWORKS)
                params.put("enabledProviders", "*,testnetwork3,testnetwork4");

            sendRequest("accounts.getAccountInfo", params, new GSResponseListener() {
                @Override
                public void onGSResponse(String method, GSResponse response, Object context) {
                    if (response.getErrorCode() == 0 && sessionChanged) {
                        invokeAccountsListeners("login", response.getData(), context);
                    }
                }
            }, context);
        }
    }

    /**
     * Store the session in the app's local storage. This method is designated for the case in which a user logs in to your app using your own login system (the app's username and password) instead of Gigya's social login. Learn more in <a target="_blank" href="http://developers.gigya.com/display/GD/Android#Android-SiteLogin-SynchronizingwithGigyaService" rel="internal">Site Login</a> guide.
     *
     * @param session the new session. This is a GSSession object that must be initialized with the sessionToken and sessionSecret you have received from Gigya (in the response of a socialize.notifyLogin API call).
     */
    public void setSession(GSSession session) {
        setSession(session, null, null, null);
    }

    protected String getUCID() {
        if (ucid == null)
            ucid = settings.getString("ucid", null);
        if (ucid == null)
            ucid = Secure.getString(appContext.getContentResolver(), Secure.ANDROID_ID);

        return ucid;
    }

    private void setUCID(String ucid) {
        this.ucid = ucid;
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("ucid", ucid);
        editor.commit();
    }

    protected String getGMID() {
        if (gmid == null)
            gmid = settings.getString("gmid", null);

        return gmid;
    }

    private void setGMID(String gmid) {
        this.gmid = gmid;
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("gmid", gmid);
        editor.commit();
    }

    /**
     * Returns the Context object provided in initialization.
     */
    public Context getContext() {
        return appContext;
    }

    /**
     * Returns the Gigya API domain used for requests.
     */
    public String getAPIDomain() {
        return apiDomain;
    }

    /**
     * Returns the Gigya site API key.
     */
    public String getAPIKey() {
        return apiKey;
    }

    /**
     * Returns the login behavior.
     *
     * @see com.gigya.socialize.android.GSAPI.LoginBehavior
     */
    public LoginBehavior getLoginBehavior() {
        return loginBehavior;
    }

    /**
     * Sets the login behavior.
     *
     * @see com.gigya.socialize.android.GSAPI.LoginBehavior
     */
    public void setLoginBehavior(LoginBehavior loginBehavior) {
        this.loginBehavior = loginBehavior;
        loginProviderFactory.updateWebProvider();
    }

    protected GSObject getConfig() {
        return config;
    }

    /**
     * Sets a global event listener.
     *
     * @param listener an object implementing GSEventListener, for listening to global events.
     * @deprecated Deprecated starting version 3.0, use setSocializeEventListener instead.
     */
    public void setEventListener(GSEventListener listener) {
        eventListener = listener;
    }

    /**
     * Returns the Socialize listener.
     */
    public GSSocializeEventListener getSocializeEventListener() {
        return socializeEventListener;
    }

    /**
     * Sets a listener for handling Socialize namespace events.
     */
    public void setSocializeEventListener(GSSocializeEventListener listener) {
        removeSocializeListener(socializeEventListener);

        if (listener != null) {
            addSocializeListener(listener);
            socializeEventListener = listener;
        }
    }

    /**
     * Returns the Socialize listener.
     */
    public GSAccountsEventListener getAccountsEventListener() {
        return accountsEventListener;
    }

    /**
     * Sets a listener for handling Accounts namespace events.
     */
    public void setAccountsEventListener(GSAccountsEventListener listener) {
        removeAccountsListener(accountsEventListener);

        if (listener != null) {
            addAccountsListener(listener);
            accountsEventListener = listener;
        }
    }

    protected void addSocializeListener(GSSocializeEventListener listener) {
        if (!socializeEventListenersArray.contains(listener)) {
            socializeEventListenersArray.add(listener);
        }
    }

    protected void removeSocializeListener(GSSocializeEventListener listener) {
        socializeEventListenersArray.remove(listener);
    }

    protected void addAccountsListener(GSAccountsEventListener listener) {
        if (!accountsEventListenersArray.contains(listener)) {
            accountsEventListenersArray.add(listener);
        }
    }

    protected void removeAccountsListener(GSAccountsEventListener listener) {
        accountsEventListenersArray.remove(listener);
    }

    // API Methods

    /**
     * Initializes the Gigya SDK and sets your partner API key.
     *
     * @param context An Android activity to use as context.
     * @param apiKey  Your Gigya API key.
     */
    public void initialize(Context context, String apiKey) {
        initialize(context, apiKey, DEFAULT_API_DOMAIN);
    }

    /**
     * Initializes the Gigya SDK and sets your partner API key.
     *
     * @param context   An Android activity to use as context.
     * @param apiKey    Your Gigya API key.
     * @param apiDomain The Gigya API domain to use for requests.
     */
    public void initialize(Context context, String apiKey, String apiDomain) {
        try {
            ((Activity) context).getWindow().requestFeature(Window.FEATURE_PROGRESS);
        } catch (Exception ignored) {}

        if (apiKey == null)
            throw new IllegalArgumentException("Gigya API key must be specified.");

        this.apiKey = apiKey;
        this.apiDomain = apiDomain;
        this.appContext = context.getApplicationContext();
        this.settings = context.getSharedPreferences("GSLIB", 0);
        this.loginProviderFactory = new LoginProviderFactory();

        loadSession();
        loadConfig();
    }

    /**
     * Login the user to a specified provider.
     * Opens a WebDialog. The provider name is passed via the params.
     *
     * @param activity the current foreground activity
     * @param params   a GSObject object that contains the parameters for the Gigya API method to call. The supported parameters are:
     *                 <ul><ul>
     *                 <li> provider (mandatory) - the provider that will be used for authenticating the user, e.g. "facebook", "twitter", etc. Please refer to <a target="_blank" href="http://wiki.gigya.com/032_SDKs/Android#Logging_in_the_User" >SDKs guide</a> for the list of supported providers.</li>
     *                 <li> cid (optional) - a string of maximum 100 characters length. This string will be associated with each transaction and will later appear on <a target="_blank" href="http://www.gigya.com/site/partners/Reports/GSReporter.aspx" >reports generated by Gigya</a>, in the "Context ID" combo box. The cid allows you to associate the report information with your own internal data. The "Context ID" combo box lets you filter the report data by application appContext.</li>
     *                 <li> forceAuthentication (optional) - The default value of this parameter is 'false'. If set to 'true', the user will be forced to provide her social network credentials during the login, even if she is already connected to the social network. Please note, that the behavior of the various social networks may be slightly different. For example, Facebook expects the current user to enter his password, and would not accept a different user name.</li>
     *                 <li> facebookReadPermissions (optional) - A comma delimited list of Facebook extended permissions to request from the user when using native login. This parameter gives the possibility to request extended permissions in addition to the permissions that Gigya is already requesting. Please refer to Facebook's <a href="https://developers.facebook.com/docs/reference/login/#permissions">permissions</a> documentation for the complete list of read permissions. Note: you should only include read permissions, otherwise Facebook will fail the login.
     *                 <li> facebookLoginBehavior (optional) - A string with a value from Facebook's <a href="https://developers.facebook.com/docs/reference/android/3.0/class/SessionLoginBehavior/">Ses1sionLoginBehavior</a></li>
     *                 <li> loginMode (optional) - The type of login being performed
     *                 <ul>
     *                 <li> standard - (default) the user is logging into an existing account.</li>
     *                 <li> link - the user is linking a social network to an existing account. The account being used to login will become the primary account. When passing loginMode='link', regToken must also be passed to identify the account being linked. This is obtained from the initial login call response.</li>
     *                 <li> reAuth - the user is proving ownership of an existing account by logging into it. The loginID will be ignored and the password verified.</li>
     *                 </ul>
     *                 </li>
     *                 <li> regToken	- (optional) This parameter is required for completing the link accounts flow. Once the initial login has failed, call the login method with loginMode=link and the regToken returned from the initial call to complete the linking. For more information go to the <a target="_blank" href="http://developers.gigya.com/display/GD/Linking+Social+Accounts">social account linking guide</a>.</li>
     *                 </ul></ul>
     * @param listener an object implementing GSResponseListener, that will be invoked when the login process completes. The listener object should handle Gigya's response.
     * @param context  a developer-created object that will be passed back unchanged to the application as one of the parameters of the GSResponseListener.onGSResponse method.
     */
    public GSLoginRequest login(final Activity activity, GSObject params, GSResponseListener listener, Object context) throws Exception {
        return login(activity, params, listener, false, context);
    }

    /**
     * Login the user to a specified provider.
     * Opens a WebDialog. The provider name is passed via the params.
     *
     * @param activity the current foreground activity
     * @param params   a GSObject object that contains the parameters for the Gigya API method to call. The supported parameters are:
     *                 <ul><ul>
     *                 <li> provider (mandatory) - the provider that will be used for authenticating the user, e.g. "facebook", "twitter", etc. Please refer to <a target="_blank" href="http://wiki.gigya.com/032_SDKs/Android#Logging_in_the_User" >SDKs guide</a> for the list of supported providers.</li>
     *                 <li> cid (optional) - a string of maximum 100 characters length. This string will be associated with each transaction and will later appear on <a target="_blank" href="http://www.gigya.com/site/partners/Reports/GSReporter.aspx" >reports generated by Gigya</a>, in the "Context ID" combo box. The cid allows you to associate the report information with your own internal data. The "Context ID" combo box lets you filter the report data by application appContext.</li>
     *                 <li> forceAuthentication (optional) - The default value of this parameter is 'false'. If set to 'true', the user will be forced to provide her social network credentials during the login, even if she is already connected to the social network. Please note, that the behavior of the various social networks may be slightly different. For example, Facebook expects the current user to enter his password, and would not accept a different user name.</li>
     *                 <li> facebookReadPermissions (optional) - A comma delimited list of Facebook extended permissions to request from the user when using native login. This parameter gives the possibility to request extended permissions in addition to the permissions that Gigya is already requesting. Please refer to Facebook's <a href="https://developers.facebook.com/docs/reference/login/#permissions">permissions</a> documentation for the complete list of read permissions. Note: you should only include read permissions, otherwise Facebook will fail the login.
     *                 <li> facebookLoginBehavior (optional) - A string with a value from Facebook's <a href="https://developers.facebook.com/docs/reference/android/3.0/class/SessionLoginBehavior/">SessionLoginBehavior</a></li>
     *                 </ul></ul>
     * @param listener an object implementing GSResponseListener, that will be invoked when the login process completes. The listener object should handle Gigya's response.
     * @param silent   when set to true, login will be attempted without displaying any user interface (currently supported only with Google+ cross platform auth).
     * @param context  a developer-created object that will be passed back unchanged to the application as one of the parameters of the GSResponseListener.onGSResponse method.
     */
    public GSLoginRequest login(final Activity activity, final GSObject params, final GSResponseListener listener, boolean silent, final Object context) throws Exception {
        // check conectivity
        if (OPTION_CHECK_CONNECTIVITY && !isInetConnected()) {
            notifyResponse(listener, "login", new GSResponse("login", params, 500026, null), context);
            return null;
        }

        final GSLoginRequest req = new GSLoginRequest(LoginRequestType.login, activity, params, listener, silent, context);

        requestsQueue.enqueue(new Runnable() {
            @Override
            public void run() {
                clearSession();

                try {
                    req.send();
                } catch (IllegalArgumentException ex) {
                    notifyResponse(listener, "login", new GSResponse("login", params, 400006, ex.getMessage(), null), context);
                }
            }
        });

        return req;
    }

    /**
     * Displays a dialog for selecting a social network to login to.
     * The UI includes all the available providers' icons as login options, enabling the user to login via his social network / webmail account.
     *
     * @param params     a GSObject object that contains the parameters for the Gigya API method to call. The supported parameters are:
     *                   <ul><ul>
     *                   <li> enabledProviders (optional) - a comma delimited list of providers that should be displayed on the UI. The list also defines the order in which the icons will be presented. Please refer to <a target="_blank" href="http://wiki.gigya.com/032_s/Android#Logging_in_the_User" >s guide</a> for the list of supported providers.
     *                   <li> disabledProviders (optional) - a comma delimited list of providers that should not be displayed on the UI.
     *                   <li> captionText (optional) - sets the caption text.
     *                   <li> cid (optional) - a string of maximum 100 characters length. This string will be associated with each transaction and will later appear on <a target="_blank" href="http://www.gigya.com/site/partners/Reports/GSReporter.aspx" >reports generated by Gigya</a>, in the "Context ID" combo box. The cid allows you to associate the report information with your own internal data. The "Context ID" combo box lets you filter the report data by application appContext.</li>
     *                   <li> forceAuthentication (optional) - The default value of this parameter is 'false'. If set to 'true', the user will be forced to provide her social network credentials during the login, even if she is already connected to the social network. Please note, that the behavior of the various social networks may be slightly different. For example, Facebook expects the current user to enter his password, and would not accept a different user name.</li>
     *                   <li> facebookReadPermissions (optional) - A comma delimited list of Facebook extended permissions to request from the user when using native login. This parameter gives the possibility to request extended permissions in addition to the permissions that Gigya is already requesting. Please refer to Facebook’s <a href="https://developers.facebook.com/docs/reference/login/#permissions">permissions</a> documentation for the complete list of read permissions. Note: you should only include read permissions, otherwise Facebook will fail the login.
     *                   <li> facebookLoginBehavior (optional) - A string with a value from Facebook's <a href="https://developers.facebook.com/docs/reference/android/3.0/class/SessionLoginBehavior/">SessionLoginBehavior</a></li>
     *                   </ul></ul>
     * @param uiListener an object implementing GSLoginUIListener, that will be invoked when the login process completes. The listener object will receive from Gigya the login response. On successful login, the response will include user data.
     * @param context    a developer-created object that will be passed back unchanged to the application as one of the parameters of the GSResponseListener.onGSResponse method.
     */
    public void showLoginUI(final GSObject params, final GSLoginUIListener uiListener, final Object context) {
        showUI("showLoginUI", LoginRequestType.login, params, uiListener, context);
    }

    private void checkDisabledProviders(GSObject params) {
        if (!loginProviderFactory.hasLoginProvider("facebook")) {
            String disabledProviders = params.getString("disabledProviders", "");
            String newDisabledProviders = "facebook";

            if (disabledProviders.length() > 0) {
                newDisabledProviders = newDisabledProviders + "," + disabledProviders;
            }

            params.put("disabledProviders", newDisabledProviders);
        }
    }

    private void showUI(final String method, final LoginRequestType mode, GSObject uiParams, final GSUIListener uiListener, final Object context) {
        if (OPTION_CHECK_CONNECTIVITY && !isInetConnected()) {
            if (uiListener != null)
                uiListener.onError(new GSResponse("showLoginUI", new GSObject(), 500026, GSResponse.getErrorMessage(500026), null), context);
            return;
        }

        final GSObject params = (uiParams != null ? uiParams : new GSObject());
        checkDisabledProviders(params);

        ProviderSelectionHandler selectionHandler = new ProviderSelectionHandler() {
            @Override
            public void onSelect(final ProviderSelection selector, final FragmentActivity selectorActivity, final String provider, final String providerDisplayName) {
                if (provider == null || provider.length() == 0) return;

                selector.showProgressDialog("Logging In");
                selector.setDisableSelection(true);

                try {
                    params.put("provider", provider);
                    params.put("captionText", providerDisplayName);

                    GSResponseListener responseListener = new GSResponseListener() {
                        @Override
                        public void onGSResponse(String method, GSResponse response, Object context) {
                            selector.dismissProgressDialog();

                            boolean cancel = response.getErrorCode() == 200001;
                            if (cancel) {
                                selector.show();
                            } else {
                                selector.finish();
                                if (uiListener != null)
                                    uiListener.onClose(false, context);
                            }

                            if (uiListener != null) {
                                if (response.getErrorCode() == 0) {
                                    if (mode == LoginRequestType.login) {
                                        ((GSLoginUIListener) uiListener).onLogin(provider, response.getData(), context);
                                    } else if (mode == LoginRequestType.addConnection) {
                                        ((GSConnectUIListener) uiListener).onConnectionAdded(provider, response.getData(), context);
                                    }
                                } else if (!cancel) {
                                    uiListener.onError(response, context);
                                }
                            }

                            selector.setDisableSelection(false);
                        }
                    };

                    if (mode == LoginRequestType.addConnection)
                        addConnection(selectorActivity, params, responseListener, context);
                    else
                        login(selectorActivity, params, responseListener, context);

                } catch (Exception e) {
                    if (uiListener != null)
                        uiListener.onError(new GSResponse(method, new GSObject(), 500001, GSResponse.getErrorMessage(500001), null), context);
                    selector.setDisableSelection(false);
                }
            }

            @Override
            public void onCancel(final ProviderSelection selector, FragmentActivity selectorActivity) {
                selectorActivity.finish();
                if (uiListener != null)
                    uiListener.onClose(true, context);
            }

            @Override
            public void onError(ProviderSelection selector, FragmentActivity selectorActivity, GSObject error) {
                selectorActivity.finish();
                if (uiListener != null)
                    uiListener.onClose(false, context);
            }

            @Override
            public void onShow(final ProviderSelection selector, FragmentActivity selectorActivity) {
                if (uiListener != null)
                    uiListener.onLoad(context);
            }
        };

        new ProviderSelection().show(mode, params, selectionHandler);
    }

    /**
     * Uses {@link com.gigya.socialize.android.GSPluginFragment} to render a Gigya JS plugin and display it modally.
     * To set the dialog's title, pass the desired title as the "captionText" parameter.
     *
     * @param pluginName     A plugin name of a supported plugin, see GSPluginFragment for a list.
     * @param params         The parameters passed to the plugin.
     * @param pluginListener A listener that will receive plugin events.
     * @param dialogListener A listener that will be invoked when the dialog is dismissed.
     * @see com.gigya.socialize.android.GSPluginFragment
     */
    public void showPluginDialog(String pluginName, GSObject params, GSPluginListener pluginListener, GSDialogListener dialogListener) {
        new PluginPresentor().show(pluginName, params, pluginListener, dialogListener);
    }

    /**
     * Tracks and counts deep link referral traffic, to be used after opening deep links. The accumulation of the referral tracking is presented in the <a target="_blank" href="http://platform.gigya.com/Site/partners/SocializeReports.aspx#cmd%3Dtraffic.referredTraffic" >Referred Traffic</a> report on Gigya's website.
     *
     * @param intent The deep link intent the app was opened with.
     */
    public void reportDeepLink(final Intent intent) {
        requestsQueue.enqueue(new Runnable() {
            @Override
            public void run() {
                LoginProvider provider = loginProviderFactory.getLoginProvider("facebook");

                if (provider.getClass() == FacebookProvider.class) {
                    ((FacebookProvider) provider).reportDeepLink(intent);
                }
            }
        });
    }

    /**
     * Tracks and counts referral traffic. The accumulation of the referral tracking is presented in the <a target="_blank" href="http://platform.gigya.com/Site/partners/SocializeReports.aspx#cmd%3Dtraffic.referredTraffic" >Referred Traffic</a> report on Gigya's website.
     *
     * @param uri      The URI used to open the app.
     * @param provider The social network the referral originated from.
     */
    public void reportURIReferral(Uri uri, String provider) {
        GSObject params = new GSObject();
        params.put("f", "re");
        params.put("e", "linkback");
        params.put("url", uri.toString());
        params.put("sn", provider);
        params.put("sdk", VERSION);
        params.put("ak", getAPIKey());
        String reportURL = String.format("http://gscounters.%s/gs/api.ashx?%s", getAPIDomain(), GSRequest.buildQS(params));

        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String[] urls) {
//                HttpClient client = new DefaultHttpClient();
//                HttpGet httpGet = new HttpGet(urls[0]);

                try {
//                    client.execute(httpGet);
                    URL url = new URL(urls[0]);
                    OkHttpClient client = getUnsafeOkHttpClient();
                    Request.Builder requestBuilder = new Request.Builder();
                    requestBuilder.url(url);
                    com.squareup.okhttp.Response responseOk = client.newCall(requestBuilder.build()).execute();
                } catch (Exception ex) {
                    Log.e(LOGTAG, "Error reporting deeplink referral");
                }

                return null;
            }
        }.execute(reportURL);
    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient okHttpClient = new OkHttpClient();
            okHttpClient.setSslSocketFactory(sslSocketFactory);
            okHttpClient.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Logs out the current user, clearing the sessions both for Gigya and native login providers.
     */
    public void logout() {
        // clear cookies
        CookieSyncManager.createInstance(appContext);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        // ask server to logout
        if (gotValidSession())
            sendRequest("socialize.logout", null, null, null);
    }

    /**
     * Add a social network connection to the current user.
     * Technically speaking, a connection is an established session with the social network and it expires according to the social network policy. A valid and active connection will give your application access to the user's social graph and ability to perform various social actions, such as publishing a newsfeed report to the connected social network.
     *
     * @param activity the current foreground activity
     * @param params   a GSObject object that contains the parameters for the Gigya API method to call. The supported parameters are:
     *                 <ul><ul>
     *                 <li> provider (mandatory) - the name of the social network to connect to. e.g. "facebook", "twitter", etc.</li>
     *                 <li> cid (optional) - a string of maximum 100 characters length. This string will be associated with each transaction and will later appear on <a target="_blank" href="http://www.gigya.com/site/partners/Reports/GSReporter.aspx" >reports generated by Gigya</a>, in the "Context ID" combo box. The cid allows you to associate the report information with your own internal data. The "Context ID" combo box lets you filter the report data by application appContext.</li>
     *                 <li> facebookReadPermissions (optional) - A comma delimited list of Facebook extended permissions to request from the user when using native login. This parameter gives the possibility to request extended permissions in addition to the permissions that Gigya is already requesting. Please refer to Facebook's <a href="https://developers.facebook.com/docs/reference/login/#permissions">permissions</a> documentation for the complete list of read permissions. Note: you should only include read permissions, otherwise Facebook will fail the login.
     *                 <li> facebookLoginBehavior (optional) - A string with a value from Facebook's <a href="https://developers.facebook.com/docs/reference/android/3.0/class/SessionLoginBehavior/">SessionLoginBehavior</a></li>
     *                 </ul></ul>
     * @param listener an object implementing GSResponseListener, that will be invoked when the add connection process completes. The listener object should handle Gigya's response.
     * @param context  a developer-created object that will be passed back unchanged to the application as one of the parameters of the GSResponseListener.onGSResponse method.
     */
    public GSLoginRequest addConnection(final Activity activity, final GSObject params, final GSResponseListener listener, final Object context) throws IllegalArgumentException {
        // check conectivity
        if (OPTION_CHECK_CONNECTIVITY && !isInetConnected()) {
            notifyResponse(listener, "addConnection", new GSResponse("addConnection", params, 500026, null), context);
            return null;
        }

        if (!gotValidSession()) {
            notifyResponse(listener, "addConnection", new GSResponse("addConnection", params, 403000, null), context);
            return null;
        }

        final GSLoginRequest req = new GSLoginRequest(LoginRequestType.addConnection, activity, params, listener, context);

        requestsQueue.enqueue(new Runnable() {
            @Override
            public void run() {
                try {
                    req.send();
                } catch (IllegalArgumentException ex) {
                    notifyResponse(listener, "login", new GSResponse("login", params, 400006, ex.getMessage(), null), context);
                }
            }
        });

        return req;
    }

    /**
     * Removes an existing Social Network connection from the current user (the provider is specified via params).
     *
     * @param params   a GSObject object that contains the parameters for the Gigya API method to call. The supported parameters are:
     *                 <ul><ul>
     *                 <li> provider (mandatory) - the name of the provider to disconnect from, e.g. "facebook", "twitter", etc.</li>
     *                 <li> cid (optional) - a string of maximum 100 characters length. This string will be associated with each transaction and will later appear on <a target="_blank" href="http://www.gigya.com/site/partners/Reports/GSReporter.aspx" >reports generated by Gigya</a>, in the "Context ID" combo box. The cid allows you to associate the report information with your own internal data. The "Context ID" combo box lets you filter the report data by application appContext.</li>
     *                 </ul></ul>
     * @param listener an object implementing GSResponseListener, that will be invoked when the remove connection process completes. The listener object should handle Gigya's response.
     * @param context  a developer-created object that will be passed back unchanged to the application as one of the parameters of the GSResponseListener.onGSResponse method.
     */
    public void removeConnection(GSObject params, final GSResponseListener listener, Object context) {
        if (OPTION_CHECK_CONNECTIVITY && !isInetConnected()) {
            notifyResponse(listener, "removeConnection", new GSResponse("removeConnection", params, 500026, null), context);
            return;
        }

        if (!gotValidSession()) {
            notifyResponse(listener, "removeConnection", new GSResponse("removeConnection", params, 403000, null), context);
            return;
        }

        if (!params.containsKey("provider")) {
            notifyResponse(listener, "removeConnection", new GSResponse("removeConnection", params, 400002, null), context);
            return;
        }
        final String provider = params.getString("provider", null);

        final Object ctx = context;
        // prepare disconnect handler that will raise event and call listener
        GSResponseListener onDisconnect = new GSResponseListener() {
            public void onGSResponse(String method, GSResponse response, Object context) {
                if (response.getErrorCode() == 0) {
                    // notify listeners on logout event
                    invokeSocializeListeners("connectionRemoved", provider, ctx);
                }

                notifyResponse(listener, "removeConnection", response, context);
            }
        };

        sendRequest("socialize.removeConnection", params, onDisconnect, null);
    }

    /**
     * Displays an "Add Connections" UI, which enables establishing connections to social networks.
     * The UI presents the available social network icons as connect options.
     *
     * @param params     a GSObject object that contains the parameters for the Gigya API method to call. The supported parameters are:
     *                   <ul><ul>
     *                   <li> enabledProviders (optional) - a comma delimited list of providers that should be displayed on the UI. The list also defines the order in which the icons will be presented. Please refer to <a target="_blank" href="http://wiki.gigya.com/030_API_reference/010_Client_API/020_Methods/socialize.showAddConnectionsUI" >Gigya's wiki</a> for the list of supported providers.
     *                   <li> disabledProviders (optional) - a comma delimited list of providers that should not be displayed on the UI.
     *                   <li> captionText (optional) - sets the caption text.
     *                   <li> cid (optional) - a string of maximum 100 characters length. This string will be associated with each transaction and will later appear on <a target="_blank" href="http://www.gigya.com/site/partners/Reports/GSReporter.aspx" >reports generated by Gigya</a>, in the "Context ID" combo box. The cid allows you to associate the report information with your own internal data. The "Context ID" combo box lets you filter the report data by application appContext.</li>
     *                   <li> facebookReadPermissions (optional) - A comma delimited list of Facebook extended permissions to request from the user when using native login. This parameter gives the possibility to request extended permissions in addition to the permissions that Gigya is already requesting. Please refer to Facebook's <a href="https://developers.facebook.com/docs/reference/login/#permissions">permissions</a> documentation for the complete list of read permissions. Note: you should only include read permissions, otherwise Facebook will fail the login.
     *                   <li> facebookLoginBehavior (optional) - A string with a value from Facebook's <a href="https://developers.facebook.com/docs/reference/android/3.0/class/SessionLoginBehavior/">SessionLoginBehavior</a></li>
     *                   </ul></ul>
     * @param uiListener an object implementing GSConnectUIListener, for listening to UI events. The listener will also be invoked when the connect process completes. On successful connect, the connect response will include user data.
     * @param context    a developer-created object that will be passed back unchanged to the application as one of the parameters of the GSResponseListener.onGSResponse method.
     */
    public void showAddConnectionsUI(GSObject params, GSConnectUIListener uiListener, Object context) {
        if (!gotValidSession()) {
            GSResponse errorRes = new GSResponse("showAddConnectionsUI", new GSObject(), 403000, GSResponse.getErrorMessage(403000), null);

            if (uiListener != null)
                uiListener.onError(errorRes, context);

            return;
        }
        showUI("showAddConnectionsUI", LoginRequestType.addConnection, params, uiListener, context);
    }

    /**
     * Sends a request to Gigya server. This method is used for invoking any of the methods supported by Gigya's <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API">REST API</a>
     *
     * @param method   the Gigya API method to call, including namespace. For example: "socialize.getUserInfo". Please refer to our <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API" >REST API reference</a> for the list of available methods.
     * @param params   a GSObject object that contains the parameters for the Gigya API method to call. Please refer to our <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API" >REST API reference</a> and find in the specific method reference the list of method parameters.
     * @param listener an object implementing GSResponseListener, that will be invoked along with response data when Gigya completes to process the request. The listener object should handle Gigya's response.
     * @param context  this object will be passed untouched and received back in the response.
     */
    public void sendRequest(String method, GSObject params, GSResponseListener listener, Object context) {
        sendRequest(method, params, OPTION_HTTPS_ENABLED, listener, context, OPTION_REQUEST_TIMEOUT_MS);
    }

    /**
     * Sends a request to Gigya server. This method is used for invoking any of the methods supported by Gigya's <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API">REST API</a>.
     *
     * @param method    the Gigya API method to call, including namespace. For example: "socialize.getUserInfo". Please refer to our <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API" >REST API reference</a> for the list of available methods.
     * @param params    a GSObject object that contains the parameters for the Gigya API method to call. Please refer to our <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API" >REST API reference</a> and find in the specific method reference the list of method parameters.
     * @param listener  an object implementing GSResponseListener, that will be invoked along with response data when Gigya completes to process the request. The listener object should handle Gigya's response.
     * @param context   this object will be passed untouched and received back in the response.
     * @param timeoutMS this determines the time in milliseconds before the request will time out without a response. Setting this parameter will override the global value set in OPTION_REQUEST_TIMEOUT_MS
     */
    public void sendRequest(String method, GSObject params, GSResponseListener listener, Object context, final int timeoutMS) {
        sendRequest(method, params, OPTION_HTTPS_ENABLED, listener, context, timeoutMS);
    }

    /**
     * Sends a request to Gigya server. This method is used for invoking any of the methods supported by Gigya's <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API">REST API</a>.
     *
     * @param method   the Gigya API method to call, including namespace. For example: "socialize.getUserInfo". Please refer to our <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API" >REST API reference</a> for the list of available methods.
     * @param params   a GSObject object that contains the parameters for the Gigya API method to call. Please refer to our <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API" >REST API reference</a> and find in the specific method reference the list of method parameters.
     * @param useHTTPS this parameter determines whether the request to Gigya will be sent over HTTP or HTTPS. default is HTTPS (true).
     * @param listener an object implementing GSResponseListener, that will be invoked along with response data when Gigya completes to process the request. The listener object should handle Gigya's response.
     * @param context  this object will be passed untouched and received back in the response.
     */
    public void sendRequest(String method, GSObject params, boolean useHTTPS, GSResponseListener listener, Object context) {
        sendRequest(method, params, useHTTPS, OPTION_REQUEST_TIMEOUT_MS, listener, context, null);
    }

    /**
     * Sends a request to Gigya server. This method is used for invoking any of the methods supported by Gigya's <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API">REST API</a>.
     *
     * @param method    the Gigya API method to call, including namespace. For example: "socialize.getUserInfo". Please refer to our <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API" >REST API reference</a> for the list of available methods.
     * @param params    a GSObject object that contains the parameters for the Gigya API method to call. Please refer to our <a target="_blank" href="http://wiki.gigya.com/030_API_reference/020_REST_API" >REST API reference</a> and find in the specific method reference the list of method parameters.
     * @param useHTTPS  this parameter determines whether the request to Gigya will be sent over HTTP or HTTPS. default is HTTPS (true).
     * @param listener  an object implementing GSResponseListener, that will be invoked along with response data when Gigya completes to process the request. The listener object should handle Gigya's response.
     * @param context   this object will be passed untouched and received back in the response.
     * @param timeoutMS this determines the time in milliseconds before the request will time out without a response. Setting this parameter will override the global value set in OPTION_REQUEST_TIMEOUT_MS
     */
    public void sendRequest(String method, GSObject params, boolean useHTTPS, GSResponseListener listener, Object context, final int timeoutMS) {
        sendRequest(method, params, useHTTPS, timeoutMS, listener, context, null);
    }

    protected void sendRequest(final String method, final GSObject params, final boolean useHTTPS, final int timeoutMS,
                               final GSResponseListener listener, final Object context, final GSLogger trace) {

        if (listener != null && OPTION_CHECK_CONNECTIVITY && !isInetConnected()) {
            notifyResponse(listener, method, new GSResponse(method, params, 500026, null), context);
            return;
        }

        if (method == null || method.length() == 0) {
            notifyResponse(listener, method, new GSResponse(method, params, 400002, null), context);
            return;
        }

        if (OPTION_SHOW_PROGRESS_ON_REQUEST && !method.toLowerCase().equals("reportsdkerror") && !method.toLowerCase().equals("getsdkconfig")) {
            showProgress(true);
        }

        Runnable reqTask = new Runnable() {
            @Override
            public void run() {
                final GSObject params2 = params == null ? new GSObject() : params;

                requestPermissionsIfNeeded(method, params2, new GSPermissionResultHandler() {
                    @Override
                    public void onResult(boolean granted, Exception exception, List<String> declinedPermissions) {
                        params2.put("sdk", VERSION);
                        params2.put("targetEnv", "mobile");
                        params2.put("ucid", getUCID());

                        boolean noAuth = params2.getBool("noAuth", false);

                        GSAsyncRequest req;
                        if (noAuth) {
                            params2.remove("noAuth");
                            Boolean forceHttps = params2.getString("include", "").contains(",ids");
                            req = new GSAsyncRequest(GSAPI.getInstance().apiKey, null, method, params2, useHTTPS || forceHttps, timeoutMS, trace);
                        } else {
                            boolean forceHttps = params2.getString("regToken", null) != null;

                            if (useHTTPS || forceHttps) {
                                params2.put("gmid", getGMID());
                            }

                            if (session != null && session.isValid()) {
                                req = new GSAsyncRequest(session.getToken(), session.getSecret(), method, params2, useHTTPS || forceHttps, timeoutMS, trace);
                            } else {
                                req = new GSAsyncRequest(GSAPI.getInstance().apiKey, null, method, params2, useHTTPS || forceHttps, timeoutMS, trace);
                            }
                        }

                        req.setAPIDomain(apiDomain);
                        req.send(listener, context);
                    }
                });
            }
        };

        if (!method.toLowerCase().equals("getsdkconfig"))
            requestsQueue.enqueue(reqTask);
        else
            reqTask.run();
    }

    protected void loadConfig() {
        String include = "permissions";
        if (getGMID() == null || getUCID() == null)
            include += ",ids";

        GSObject params = new GSObject();
        params.put("include", include);
        params.put("apiKey", apiKey);
        params.put("noAuth", true);
        params.put("enabledProviders", "");

        sendRequest("getSDKConfig", params, true, 5000, new GSResponseListener() {
            public void onGSResponse(String method, GSResponse response, Object context) {
                if (response.getErrorCode() != 0 || response.getData() == null) {
                    config = new GSObject();
                    Log.e(LOGTAG, "Unable to load config from server:" + response.getLog());
                } else {
                    config = response.getData();
                    GSObject ids = config.getObject("ids", null);
                    if (ids != null) {
                        setUCID(ids.getString("ucid", null));
                        setGMID(ids.getString("gmid", null));
                    }
                }

                GSAPI.this.loginProviderFactory.validatePermissions(config);
                requestsQueue.release();
            }
        }, null, null);
    }

    protected void reportError(String apiCall, GSResponse res) {
        try {
            if (config == null) return;

            boolean shouldReport = false;
            apiCall = apiCall.toLowerCase();
            GSArray rules = config.getArray("errorReportRules");
            if (rules == null) return;
            String ruleMethod;
            String ruleError;
            String resError = Integer.toString(res.getErrorCode());
            for (int index = 0; index < rules.length(); index++) {
                GSObject rule = rules.getObject(index);
                ruleMethod = rule.getString("method");
                ruleError = rule.getString("error");
                if (ruleMethod.toLowerCase().equals(apiCall) || ruleMethod.equals("*")) {
                    if (ruleError.equals(resError) || ruleError.equals("*")) {
                        shouldReport = true;
                        break;
                    }
                }
            }
            if (!shouldReport) return;

            GSObject params = new GSObject();
            params.put("apiKey", apiKey);
            params.put("log", res.getLog());
            params.put("info", res.getErrorCode());
            params.put("reportError", false);
            sendRequest("reportSDKError", params, new GSResponseListener() {
                public void onGSResponse(String method, GSResponse response, Object context) {
                    if (response.getErrorCode() != 0)
                        Log.e(LOGTAG, "Unable to report SDK error." + response.getLog());
                }
            }, null);

        } catch (Exception ex) {
            Log.e(LOGTAG, ex.getMessage());
            showProgress(false);
        }
    }

    protected HashMap<String, String> getCookies(String url) {
        HashMap<String, String> map = new HashMap<String, String>();
        String cookie = CookieManager.getInstance().getCookie(url);
        if (cookie == null) return map;

        String[] keyValueSets = cookie.split(";");
        for (String cstr : keyValueSets) {
            String[] keyValue = cstr.split("=");
            String key = keyValue[0];
            String value = "";
            if (keyValue.length > 1) value = keyValue[1];
            map.put(key, value);
        }
        return map;
    }

    protected void notifyResponse(final GSResponseListener listener, final String method, final GSResponse response, final Object context) {
        if (listener != null) {
            new Thread(new Runnable() {
                public void run() {
                    if (response.getErrorCode() != 0)
                        Log.e(LOGTAG, "Error Response: \n" + response.getLog());
                    listener.onGSResponse(method, response, context);
                }
            }).run();
        }
    }

    /**
     * Returns the provider used for last login.
     */
    public String getLastLoginProvider() {
        return settings.getString("lastLoginProvider", null);
    }

    protected void setLastLoginProvider(String provider) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lastLoginProvider", provider);
        editor.commit();
    }

    protected boolean isInetConnected() {
        try {
            ConnectivityManager con = (ConnectivityManager) appContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = con.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnected();
        } catch (Exception ex) {
            err("Unable to detect inet connection status", ex);
            return true; // if we can't detect, say yes :)
        }
    }

    protected void saveTimestampOffset(long offset) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("tsOffset", offset);
        editor.commit();
    }

    protected long loadTimestampOffset() {
        return settings.getLong("tsOffset", 0);
    }

    protected void saveSession() {
        if (session == null) return;
        //	save secret and token in app's preference
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("session.Token", session.getToken());
        editor.putString("session.Secret", session.getSecret());
        editor.putLong("session.ExpirationTime", session.getExpirationTime());
        editor.commit();
    }

    protected void loadSession() {
        GSSession s = new GSSession();
        s.setToken(settings.getString("session.Token", null));
        s.setSecret(settings.getString("session.Secret", null));
        s.setExpirationTime(settings.getLong("session.ExpirationTime", -1));
        if (s.isValid())
            session = s;
    }

    protected void clearSession() {
        session = null;
        SharedPreferences.Editor editor = settings.edit();
        editor.remove("session.Token");
        editor.remove("session.Secret");
        editor.remove("session.ExpirationTime");
        editor.commit();

        // clear all providers sessions
        for (LoginProvider provider : loginProviderFactory.getLoginProviders().values()) {
            provider.clearSession();
        }
    }

    protected boolean gotValidSession() {
        return session != null && session.isValid();
    }

    /**
     * get unique request-code for an Android permissions request
     *
     * @param callback an object implementing GSAndroidPermissionListener, that will be invoked after user had responded to the permission request dialog
     * @return request-code
     */
    public int getNextAndroidPermissionsRequestCode(GSAndroidPermissionListener callback) {
        int requestCode = androidPermissionsRequestCode++;
        androidPermissionListeners.put(requestCode, callback);

        return requestCode;
    }

    /**
     * handle user response after requesting an Android permissions
     *
     * @param requestCode  request-code of the permissions request
     * @param permissions  requested permissions
     * @param grantResults whether permissions were approved by the user
     * @return whether the permissions result was handled
     */
    public boolean handleAndroidPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (androidPermissionListeners.containsKey(requestCode)) {
            androidPermissionListeners.get(requestCode).onAndroidPermissionsResult(permissions, grantResults);
            return true;
        }

        return false;
    }

    /**
     * Asks the user for Facebook publish permissions. This method works only with Facebook native login, and must be called after the user logged in.
     *
     * @param permissions A comma delimited list of Facebook publish permissions to request from the user. Please refer to Facebook's documentation for a complete list of possible values.
     * @param callback    completion handler that will be invoked when the process if finished.
     */
    public void requestNewFacebookPublishPermissions(final List<String> permissions, final GSPermissionResultHandler callback) throws Exception {
        requestsQueue.enqueue(new Runnable() {
            @Override
            public void run() {
                LoginProvider fbProvider = loginProviderFactory.getLoginProvider("facebook");
                if (loginProviderFactory.getLoginProvider("facebook").getClass() == FacebookProvider.class) {
                    ((FacebookProvider) fbProvider).requestPermissions("publish", permissions, callback);
                } else if (callback != null) {
                    callback.onResult(false, new Exception("App isn't configured for Facebook native login."), null);
                }
            }
        });
    }

    /**
     * Asks the user for Facebook read permissions. This method works only with Facebook native login, and must be called after the user logged in.
     *
     * @param permissions A comma delimited list of Facebook publish permissions to request from the user. Please refer to Facebook's documentation for a complete list of possible values.
     * @param callback    completion handler that will be invoked when the process if finished.
     */
    public void requestNewFacebookReadPermissions(final List<String> permissions, final GSPermissionResultHandler callback) throws Exception {
        requestsQueue.enqueue(new Runnable() {
            @Override
            public void run() {
                LoginProvider fbProvider = loginProviderFactory.getLoginProvider("facebook");
                if (loginProviderFactory.getLoginProvider("facebook").getClass() == FacebookProvider.class) {
                    ((FacebookProvider) fbProvider).requestPermissions("read", permissions, callback);
                } else if (callback != null) {
                    callback.onResult(false, new Exception("App isn't configured for Facebook native login."), null);
                }
            }
        });
    }

    protected void requestPermissionsIfNeeded(String method, GSObject params, GSPermissionResultHandler callback) {
        List<String> permissions = new ArrayList<String>();
        String enabledProviders = params.getString("enabledProviders", "*");
        if (enabledProviders.indexOf("facebook") != -1 || enabledProviders.indexOf("*") != -1) {
            LoginProvider fbProvider = loginProviderFactory.getLoginProvider("facebook");
            if (fbProvider.getClass() == FacebookProvider.class && FacebookProvider.isLoggedIn()) {
                if (method.contains("publishUserAction") || method.contains("setStatus") || method.contains("checkin")) {
                    permissions.add("publish_actions");
                }
                ((FacebookProvider) fbProvider).requestPermissions("publish", permissions, callback);
                return;
            }
        }

        callback.onResult(true, null, new ArrayList<String>());
    }

    protected void showProgress(Boolean show) {
        showProgress(show, null);
    }

    protected void showProgress(boolean show, final String text) {
        if (show) {
            if (progressActivity == null) {
                HostActivity.create(appContext, new HostActivityHandler() {

                    @Override
                    public void onStart(FragmentActivity activity) {
                    }

                    @Override
                    public void onCreate(FragmentActivity activity, Bundle savedInstanceState) {
                        if (progressActivity != null) {
                            showProgress(false);
                        }
                        progressActivity = activity;
                        try {
                            progress = ProgressDialog.show(progressActivity, "", text != null && !text.equals("") ? text : "Please wait...", true);
                            progress.setOnKeyListener(new Dialog.OnKeyListener() {
                                public boolean onKey(DialogInterface d, int keyCode, KeyEvent event) {
                                    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
                                        if (progress != null) progress.dismiss();
                                        if (progressActivity != null) progressActivity.finish();
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            });
                        } catch (Exception ex) {
                        }
                    }

                    @Override
                    public void onCancel(FragmentActivity activity) {
                        progress.dismiss();
                        progressActivity = null;
                    }

                    @Override
                    public void onActivityResult(FragmentActivity activity, int requestCode,
                                                 int resultCode, Intent data) {
                    }
                });
            }
        } else {
            if (progress != null) progress.dismiss();
            if (progressActivity != null) progressActivity.finish();
            progressActivity = null;
        }
    }

    protected void invokeSocializeListeners(String event, Object... params) {
        String methodName = "on" + event.substring(0, 1).toUpperCase() + event.substring(1);
        Method method = findMethodInClass(GSSocializeEventListener.class, methodName);

        // Backwards support. Need to remove this once GSEventDelegate is removed.
        if (eventListener != null) {
            try {
                method.invoke(eventListener, params);
            } catch (Exception ex) {
            }
        }

        for (GSSocializeEventListener listener : socializeEventListenersArray) {
            try {
                method.invoke(listener, params);
            } catch (Exception ex) {
            }
        }
    }

    protected void invokeAccountsListeners(String event, Object... params) {
        String methodName = "on" + event.substring(0, 1).toUpperCase() + event.substring(1);
        Method method = findMethodInClass(GSAccountsEventListener.class, methodName);

        for (GSAccountsEventListener listener : accountsEventListenersArray) {
            try {
                method.invoke(listener, params);
            } catch (Exception ex) {
            }
        }
    }

    private Method findMethodInClass(Class classObj, String methodName) {
        Method[] methods = classObj.getMethods();
        Method method = null;
        for (Method curr : methods) {
            if (curr.getName().equals(methodName)) {
                method = curr;
                break;
            }
        }

        return method;
    }

    protected static void err(String msg) {
        err(msg, null);
    }

    protected static void err(Throwable t) {
        err("", t);
    }

    protected static void err(String msg, Throwable t) {
        if (OPTION_TRACE)
            Log.e(LOGTAG, msg, t);
    }

    protected static void debug(String format, Object... args) {
        debug(String.format(format, args));
    }

    protected static void debug(String msg) {
        if (OPTION_TRACE)
            Log.d(LOGTAG, msg);
    }
}