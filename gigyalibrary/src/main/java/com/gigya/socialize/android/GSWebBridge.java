package com.gigya.socialize.android;

import android.app.Activity;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.GSResponseListener;
import com.gigya.socialize.android.GSLoginRequest.LoginRequestType;
import com.gigya.socialize.android.event.GSAccountsEventListener;
import com.gigya.socialize.android.event.GSSocializeEventListener;
import com.gigya.socialize.android.event.GSWebBridgeListener;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.UUID;

/**
 * <p>
 * GSWebBridge connects between the Gigya JavaScript SDK and the Gigya Android SDK.
 * Any WebView can be attached to the web bridge. Doing this gives the following benefits:
 * <ul>
 * <li>Session state will be synchronized. If the user is logged in and the session is active in the Android SDK - he will be automatically logged in in the JS SDK.</li>
 * <li>Any API requests by the JS SDK will be routed through the Android SDK, using the Android SDK session.</li>
 * <li>Any login process invoked by the JS SDK will be handled by the Android SDK, creating a seamless login experience - using the web browser or the provider’s native login.</li>
 * </ul>
 * </p>
 * <p>
 * To register a web view, follow these steps:
 * <ol>
 * <li>
 * Call the attach method before the web view has started loading:
 * <pre>
 *  {@code
 *  GSWebBridge.attach(webView, new GSWebBridgeListener() { ... }); }
 *             </pre>
 * </li>
 * <li>
 * Add the following code to the beginning of your WebViewClient implementation's shouldOverrideUrlLoading method:
 * <pre>
 *  {@code
 *  public boolean shouldOverrideUrlLoading(WebView webView, String url) {
 *      if (GSWebBridge.handleUrl(webView, url)) {
 *          return true;
 *      }
 *  } }
 *             </pre></li>
 * <li>
 * Detach the web view when finished:
 * <pre>
 *  {@code
 *  GSWebBridge.detach(webView); }
 *             </pre>
 * </li>
 * </ol>
 * </p>
 */
public class GSWebBridge {
    private enum GSWebBridgeActions {
        IS_SESSION_VALID,
        SEND_REQUEST,
        SEND_OAUTH_REQUEST,
        GET_IDS,
        ON_PLUGIN_EVENT,
        ON_CUSTOM_EVENT,
        REGISTER_FOR_NAMESPACE_EVENTS,
        ON_JS_EXCEPTION
    }

    protected static final String REDIRECT_URL_SCHEME = "gsapi";
    private static final String CALLBACK_JS_PATH = "gigya._.apiAdapters.mobile.mobileCallbacks";
    private static final String GLOBAL_EVENTS_JS_PATH = "gigya._.apiAdapter.onSDKEvent";
    private static final String WEB_BRIDGE_CONTEXT_PREFIX = "js_";
    private static ArrayList<GSWebBridge> bridges = new ArrayList<GSWebBridge>();

    private Activity activity;
    private WebView webView;
    private GSWebBridgeListener listener;
    private String bridgeId;

    private GSSocializeEventListener socializeListener;
    private GSAccountsEventListener accountsListener;

    // Static Methods

    /**
     * Attaches a web view to the web bridge. This method should be called before calling WebView's loadUrl method.
     *
     * @param webview  A web view.
     * @param listener A listener to receive GSWebBridge events.
     */
    public static void attach(Activity activity, WebView webview, GSWebBridgeListener listener) {
        if (findBridgeForWebView(webview) == null) {
            GSWebBridge bridge = new GSWebBridge(activity, webview, listener);
            bridges.add(bridge);

            webview.addJavascriptInterface(new Object() {
                @JavascriptInterface
                public String getAPIKey() {
                    return GSAPI.getInstance().getAPIKey();
                }

                @JavascriptInterface
                public String getAdapterName() {
                    return "mobile";
                }

                @JavascriptInterface
                public String getFeatures() {
                    JSONArray features = new JSONArray();
                    for (GSWebBridgeActions feature : GSWebBridgeActions.values()) {
                        features.put(feature.toString().toLowerCase());
                    }
                    return features.toString();
                }
            }, "__gigAPIAdapterSettings");
        }
    }

    /**
     * Detaches a web view from the web bridge. This method must be called when the web view is discarded.
     *
     * @param webview A web view that was already attached using the attach method.
     */
    public static void detach(WebView webview) {
        GSWebBridge bridge = findBridgeForWebView(webview);
        bridges.remove(bridge);
        GSAPI.getInstance().removeAccountsListener(bridge.accountsListener);
        GSAPI.getInstance().removeSocializeListener(bridge.socializeListener);
    }

    /**
     * Routes a request from an attached web view. Should be called in WebViewClient's shouldOverrideUrlLoading method.
     *
     * @param webView A web view that was already attached using the attach method.
     * @param url     The URL string as received in shouldOverrideUrlLoading.
     * @return A Boolean value indicating whether the web bridge has handled the request.
     */
    public static boolean handleUrl(WebView webView, String url) {
        if (url.startsWith(REDIRECT_URL_SCHEME + "://")) {
            GSWebBridge bridge = findBridgeForWebView(webView);

            if (bridge != null) {
                return bridge.handleUrl(url);
            }
        }

        return false;
    }

    private static GSWebBridge findBridgeForWebView(WebView webView) {
        for (GSWebBridge bridge : bridges) {
            if (bridge.webView == webView) {
                return bridge;
            }
        }

        return null;
    }

    // Ctor
    private GSWebBridge(Activity activity, WebView webView, GSWebBridgeListener listener) {
        this.activity = activity;
        this.webView = webView;
        this.listener = listener;
        this.bridgeId = WEB_BRIDGE_CONTEXT_PREFIX + UUID.randomUUID().toString();
    }

    // JS -> Mobile
    private boolean handleUrl(String urlString) {
        Uri url = Uri.parse(urlString);
        GSObject data = new GSObject();
        data.parseQueryString(url.getEncodedQuery());

        GSWebBridgeActions action;
        try {
            action = GSWebBridgeActions.valueOf(url.getHost().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return false;
        }
        String method = url.getPath().replace("/", "");
        String callbackId = data.getString("callbackID", null);

        GSObject params = new GSObject();
        params.parseQueryString(data.getString("params", null));
        GSObject settings = new GSObject();
        settings.parseQueryString(data.getString("settings", null));

        switch (action) {
            case IS_SESSION_VALID:
                GSSession session = GSAPI.getInstance().getSession();
                boolean isSessionValid = (session != null && session.isValid());
                invokeCallback(callbackId, isSessionValid);
                break;
            case SEND_REQUEST:
                sendRequest(method, params, settings, callbackId);
                break;
            case SEND_OAUTH_REQUEST:
                sendOAuthRequest(method, params, settings, callbackId);
                break;
            case GET_IDS:
                getIDs(callbackId);
                break;
            case ON_PLUGIN_EVENT:
                receivePluginEvent(params);
                break;
            case REGISTER_FOR_NAMESPACE_EVENTS:
                registerForNamespaceEvents(params);
                break;
        }

        return true;
    }

    private void sendRequest(String method, GSObject params, GSObject settings, final String callbackId) {
        boolean forceHttps = settings.getBool("forceHttps", false) || settings.getBool("requiresSession", false);
        params.put("ctag", "webbridge");

        GSAPI.getInstance().sendRequest(method, params, GSAPI.OPTION_HTTPS_ENABLED || forceHttps, new GSResponseListener() {
            @Override
            public void onGSResponse(String method, GSResponse response, Object context) {
                invokeCallback(callbackId, response);
            }
        }, bridgeId);
    }

    private void sendOAuthRequest(String method, GSObject params, GSObject settings, final String callbackId) {
        String[] methodParts = method.split("\\.");
        LoginRequestType requestType;
        try {
            requestType = LoginRequestType.valueOf(methodParts[methodParts.length - 1]);
        } catch (Exception ex) {
            requestType = LoginRequestType.login;
        }

        if (listener != null)
            listener.beforeLogin(webView, requestType, params);

        GSLoginRequest request = new GSLoginRequest(requestType, activity, params, new GSResponseListener() {
            @Override
            public void onGSResponse(String method, final GSResponse response, Object context) {
                if (response.getErrorCode() != 0) {
                    response.getData().put("errorCode", response.getErrorCode());
                    invokeCallback(callbackId, response);
                } else {
                    GSObject user = new GSObject();
                    user.put("userInfo", response.getData());
                    user.put("errorCode", response.getErrorCode());
                    invokeCallback(callbackId, user);
                }

                if (listener != null)
                    listener.onLoginResponse(webView, response);
            }
        }, bridgeId);

        try {
            request.send();
        } catch (Exception ex) {
            GSResponse response = new GSResponse(method, params, 400122, ex.getMessage(), null);
            invokeCallback(callbackId, response);

            if (listener != null)
                listener.onLoginResponse(webView, response);
        }
    }

    private void getIDs(String callbackId) {
        GSObject ids = new GSObject();
        ids.put("ucid", GSAPI.getInstance().getUCID());
        ids.put("gcid", GSAPI.getInstance().getGMID());
        invokeCallback(callbackId, ids);
    }

    private void receivePluginEvent(GSObject event) {
        String containerId = event.getString("sourceContainerID", null);

        if (listener != null && containerId != null) {
            listener.onPluginEvent(webView, event, containerId);
        }
    }

    private void registerForNamespaceEvents(GSObject params) {
        String namespace = params.getString("namespace", "");

        if ("socialize".equals(namespace) && socializeListener == null) {
            socializeListener = new GSSocializeEventListener() {
                @Override
                public void onLogin(String provider, GSObject user, Object context) {
                    if (context == null || !context.equals(bridgeId)) {
                        GSObject event = new GSObject();
                        event.put("user", user);
                        event.put("provider", provider);
                        invokeGlobalEvent("socialize.login", event);
                    }
                }

                @Override
                public void onLogout(Object context) {
                    if (context == null || !context.equals(bridgeId)) {
                        invokeGlobalEvent("socialize.logout", null);
                        invokeGlobalEvent("accounts.logout", null);
                    }
                }

                @Override
                public void onConnectionAdded(String provider, GSObject user, Object context) {
                    if (context == null || !context.equals(bridgeId)) {
                        GSObject event = new GSObject();
                        event.put("user", user);
                        event.put("provider", provider);
                        invokeGlobalEvent("socialize.connectionAdded", event);
                    }
                }

                @Override
                public void onConnectionRemoved(String provider, Object context) {
                    if (context == null || !context.equals(bridgeId)) {
                        GSObject event = new GSObject();
                        event.put("provider", provider);
                        invokeGlobalEvent("socialize.connectionRemoved", event);
                    }
                }
            };

            GSAPI.getInstance().addSocializeListener(socializeListener);
        } else if ("accounts".equals(namespace) && accountsListener == null) {
            accountsListener = new GSAccountsEventListener() {
                @Override
                public void onLogin(GSObject account, Object context) {
                    if (context == null || !context.equals(bridgeId)) {
                        invokeGlobalEvent("accounts.login", account.clone());
                    }
                }

                @Override
                public void onLogout(Object context) {
                    if (context == null || !context.equals(bridgeId)) {
                        invokeGlobalEvent("socialize.logout", null);
                        invokeGlobalEvent("accounts.logout", null);
                    }
                }
            };

            GSAPI.getInstance().addAccountsListener(accountsListener);
        }
    }

    // Mobile -> JS
    private void invokeCallback(String callbackId, Object result) {
        String value;
        if (result instanceof GSResponse) {
            value = ((GSResponse) result).getData().toJsonString();
        } else {
            value = result.toString();
        }

        String invocation = String.format("javascript:%s['%s'](%s);", CALLBACK_JS_PATH, callbackId, value);
        webView.loadUrl(invocation);
    }

    private void invokeGlobalEvent(String eventName, GSObject params) {
        if (params == null)
            params = new GSObject();

        params.put("eventName", eventName);
        String invocation = String.format("javascript:%s(%s);", GLOBAL_EVENTS_JS_PATH, params);
        webView.loadUrl(invocation);
    }
}