package com.gigya.socialize.android;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.android.event.GSDialogListener;
import com.gigya.socialize.android.event.GSPluginListener;
import com.gigya.socialize.android.event.GSWebBridgeListener;

/**
 * <p>GSPluginFragment is a custom fragment that renders Gigya <a target="_blank" href="http://developers.gigya.com/display/GD/Plugins">JS Plugins</a>
 * and integrates them seamlessly with the Gigya Android SDK.</p>
 * <p/>
 */
public class GSPluginFragment extends DialogFragment {
    private static final String ON_JS_LOAD_ERROR = "on_js_load_error";
    private static final String ON_JS_EXCEPTION = "on_js_exception";
    private static final String PLUGIN_BUNDLE_KEY = "pluginName";
    private static final String PARAMS_BUNDLE_KEY = "pluginParams";
    private static final String SHOW_DIALOG_BUNDLE_KEY = "showAsDialog";
    private static final String CONTAINER_ID = "pluginContainer";

    private GSPluginListener listener;
    private GSDialogListener dismissListener;
    private ProgressBar progressBar;
    private WebView webView;
    private String plugin;
    private GSObject params;
    private boolean startedLoadingPlugin = false;
    private boolean showLoadingProgress = true;
    private boolean showLoginProgress = true;
    private boolean webViewWrapContent = false;
    private int jsLoadingTimeout = 10000;

    // Static Creators

    /**
     * Creates and returns a new GSPluginFragment instance.
     *
     * @param plugin A plugin name, as specified in the above list.
     * @param params The parameters to pass to the plugin.
     * @return A new instance of GSPluginFragment.
     */
    public static GSPluginFragment newInstance(String plugin, GSObject params) {
        return GSPluginFragment.newInstance(plugin, params, false);
    }

    /**
     * Creates and returns a new GSPluginFragment instance.
     *
     * @param plugin       A plugin name, as specified in the above list.
     * @param params       The parameters to pass to the plugin.
     * @param showAsDialog Determines whether the fragment will be displayed as dialog.
     * @return A new instance of GSPluginFragment.
     */
    public static GSPluginFragment newInstance(String plugin, GSObject params, boolean showAsDialog) {
        GSPluginFragment fragment = new GSPluginFragment();

        Bundle args = fragment.getArguments();
        if (args == null) {
            args = new Bundle();
        }

        if (params == null) {
            params = new GSObject();
        }

        args.putString(PLUGIN_BUNDLE_KEY, plugin);
        args.putString(PARAMS_BUNDLE_KEY, params.toJsonString());
        args.putBoolean(SHOW_DIALOG_BUNDLE_KEY, showAsDialog);
        fragment.setArguments(args);

        return fragment;
    }

    // Ctors

    public GSPluginFragment() {
        super();
    }

    public GSPluginFragment(Bundle args) {
        super();
        this.setArguments(args);
    }

    // Properties

    /**
     * Gets the listener that will receive plugin events.
     */
    public GSPluginListener getPluginListener() {
        return listener;
    }

    /**
     * Sets the listener that will receive plugin events.
     */
    public void setPluginListener(GSPluginListener listener) {
        this.listener = listener;
    }

    /**
     * Gets the listener that will be invoked when the dialog is dismissed, if the fragment is displayed as dialog.
     */
    public GSDialogListener getOnDismissListener() {
        return dismissListener;
    }

    /**
     * Sets the listener that will be invoked when the dialog is dismissed, if the fragment is displayed as dialog.
     */
    public void setOnDismissListener(GSDialogListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    /**
     * Gets the Boolean value that specifies whether to show a progress indicator while the plugin is loaded.
     */
    public boolean getShowLoadingProgress() {
        return showLoadingProgress;
    }

    /**
     * Sets the Boolean value that specifies whether to show a progress indicator while the plugin is loaded.
     */
    public void setShowLoadingProgress(boolean showLoadingProgress) {
        this.showLoadingProgress = showLoadingProgress;
    }

    /**
     * Gets the Boolean value that specifies whether to show a progress indicator during a login process initiated from the plugin view.
     */
    public boolean getShowLoginProgress() {
        return showLoginProgress;
    }

    /**
     * Sets the Boolean value that specifies whether to show a progress indicator during a login process initiated from the plugin view.
     */
    public void setShowLoginProgress(boolean showLoginProgress) {
        this.showLoginProgress = showLoginProgress;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShowsDialog(getArguments().getBoolean(SHOW_DIALOG_BUNDLE_KEY));

        Activity activity = getActivity();
        webView = new WebView(activity);
        webView.getSettings().setJavaScriptEnabled(true);

        GSWebBridge.attach(activity, webView, new GSWebBridgeListener() {
            @Override
            public void beforeLogin(WebView webView, GSLoginRequest.LoginRequestType requestType, GSObject params) {
                if (showLoginProgress)
                    progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoginResponse(WebView webView, GSResponse response) {
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPluginEvent(WebView webView, final GSObject event, String containerID) {
                if (containerID.equals(CONTAINER_ID)) {
                    String eventName = event.getString("eventName", "");

                    if (eventName.equals("load"))
                        progressBar.setVisibility(View.INVISIBLE);

                    if (getShowsDialog() && !webViewWrapContent) {
                        if (plugin.equals("accounts.screenSet")) { // TODO: removed specific plugin reference when plugin-feature-detection feature completes: https://gigya.tpondemand.com/entity/3033
                            if (eventName.equals("afterScreenLoad"))
                                redrawWebViewInDialog(webView);
                        } else if (eventName.equals("load")) {
                            redrawWebViewInDialog(webView);
                        }
                    }

                    if (listener != null) {
                        if (eventName.equals("load")) {
                            listener.onLoad(GSPluginFragment.this, event);
                        } else if (eventName.equals("error")) {
                            listener.onError(GSPluginFragment.this, event);
                        } else {
                            listener.onEvent(GSPluginFragment.this, event);
                        }
                    }

                    if ((eventName.equals("hide") || eventName.equals("close")) && getShowsDialog()) {
                        GSPluginFragment.this.dismiss();
                        if (dismissListener != null) {
                            dismissListener.onDismiss(false, event);
                        }
                    }
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlString) {
                Uri url = Uri.parse(urlString);
                if (url.getScheme().equals(GSWebBridge.REDIRECT_URL_SCHEME) && url.getHost().equals(ON_JS_LOAD_ERROR)) {
                    GSObject error = new GSObject();
                    error.put("errorCode", 500032);
                    error.put("description", "Failed loading socialize.js");
                    onError(error);
                } else if (url.getScheme().equals(GSWebBridge.REDIRECT_URL_SCHEME) && url.getHost().equals(ON_JS_EXCEPTION)) {
                    GSObject data = new GSObject();
                    data.parseQueryString(url.getQuery());
                    String exceptionString = data.getString("ex", "");

                    GSObject error = new GSObject();
                    error.put("errorCode", 405001);
                    error.put("description", "Javascript error while loading plugin. Please make sure the plugin name is correct.");
                    error.put("jsError", exceptionString);
                    onError(error);
                } else if (!GSWebBridge.handleUrl(view, urlString)) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, url);
                    startActivity(browserIntent);
                }

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (showLoadingProgress)
                    progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                GSObject error = new GSObject();
                error.put("errorCode", 500032);
                error.put("description", "Failed loading " + failingUrl);
                onError(error);
            }

//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                handler.proceed();
//            }
        });

        loadFromBundle(getArguments());
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        String title = params.getString("captionText", null);
        if (title == null) {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        } else {
            dialog.setTitle(title);
        }

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Activity activity = getActivity();
        Display display = getDisplay();
        if (activity == null || display == null)
            return null;

        if (getShowsDialog()) {
            int webViewWidth = Math.min(display.getWidth(), display.getHeight()) * 9 / 10;
            int webViewHeight = (savedInstanceState == null) ? display.getHeight() * 9 / 10 : LayoutParams.WRAP_CONTENT;
            webView.setLayoutParams(new LayoutParams(webViewWidth, webViewHeight));
        } else {
            webView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        FrameLayout layout = new FrameLayout(activity);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (webView.getParent() != null)
            ((FrameLayout) webView.getParent()).removeView(webView);

        layout.addView(webView);

        LayoutParams progressLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        progressLayout.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        progressBar = new ProgressBar(activity);
        progressBar.setIndeterminate(true);
        progressBar.setLayoutParams(progressLayout);
        progressBar.setVisibility(View.INVISIBLE);
        layout.addView(progressBar);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!startedLoadingPlugin) {
            getView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int fragmentWidth = getView().getWidth();
                    if (fragmentWidth > 0 && !startedLoadingPlugin) {
                        String html = buildPluginHTML();
                        webView.loadDataWithBaseURL("http://www.gigya.com", html, "text/html", "utf-8", null);
                        startedLoadingPlugin = true;
                        getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (dismissListener != null) {
            dismissListener.onDismiss(true, null);
        }
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        if (getRetainInstance() && dialog != null)
            dialog.setDismissMessage(null);

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (webView != null) {
            webView.loadUrl("about:blank");
            webView.setWebViewClient(null);
            webView.setWebChromeClient(null);
            GSWebBridge.detach(webView);
        }

        super.onDestroy();
    }

    private Display getDisplay() {
        Activity activity = getActivity();
        if (activity == null)
            return null;

        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        return windowManager.getDefaultDisplay();
    }

    private void redrawWebViewInDialog(WebView webView) {
        Display display = getDisplay();
        if (display == null)
            return;

        int webViewWidth = Math.min(display.getWidth(), display.getHeight()) * 9 / 10;
        int webViewHeight = LayoutParams.WRAP_CONTENT;
        webView.setLayoutParams(new LayoutParams(webViewWidth, webViewHeight));
        webViewWrapContent = true;
    }

    private void loadFromBundle(Bundle args) {
        if (args != null) {
            webView.restoreState(args);
            plugin = args.getString(PLUGIN_BUNDLE_KEY);

            try {
                params = new GSObject(args.getString(PARAMS_BUNDLE_KEY));
            } catch (Exception ex) {
                params = new GSObject();
            }
        }
    }

    private void prepareParams() {
        params.put("containerID", CONTAINER_ID);

        if (params.getString("deviceType", null) == null)
            params.put("deviceType", "mobile");

        if (plugin.contains("commentsUI") && params.getInt("version", -1) == -1)
            params.put("version", 2);

        if (plugin.contains("commentsUI"))
            params.put("hideShareButtons", true);

        if (plugin.contains("RatingUI") && params.getString("showCommentButton", null) == null)
            params.put("showCommentButton", false);

        // Calculating the real size of the webview. -16 is because of default <body> margin(8px)
        float density = getActivity().getResources().getDisplayMetrics().density;
        params.put("width", this.getView().getWidth() / density - 16);

        if (!GSAPI.getInstance().loginProviderFactory.hasLoginProvider("facebook")) {
            String disabledProviders = params.getString("disabledProviders", "");
            disabledProviders = "facebook," + disabledProviders;
            params.put("disabledProviders", disabledProviders);
        }
    }

    private String buildPluginHTML() {
        prepareParams();

        String enableTestNetworksScript = "";
        if (GSAPI.__DEBUG_OPTION_ENABLE_TEST_NETWORKS) {
            enableTestNetworksScript = "gigya._.providers.arProviders.push(new gigya._.providers.Provider(6016, 'testnetwork3', 650, 400, 'login,friends,actions,status,photos,places,checkins', true));" +
                                        "gigya._.providers.arProviders.push(new gigya._.providers.Provider(6017, 'testnetwork4', 650, 400, 'login,friends,actions,status,photos,places,checkins', true));";
        }


        String template =
                "<head>" +
                    "<meta name='viewport' content='initial-scale=1,maximum-scale=1,user-scalable=no' />" +
                    "<script>" +
                        "function onJSException(ex) {" +
                            "document.location.href = '%s://%s?ex=' + encodeURIComponent(ex);" +
                        "}" +
                        "function onJSLoad() {" +
                            "if (gigya && gigya.isGigya)" +
                                "window.__wasSocializeLoaded = true;" +
                        "}" +
                        "setTimeout(function() {" +
                            "if (!window.__wasSocializeLoaded)" +
                              "document.location.href = '%s://%s';" +
                        "}, %s);" +
                    "</script>" +
                    "<script src='http://cdn.gigya.com/JS/gigya.js?apikey=%s' type='text/javascript' onLoad='onJSLoad();'>" +
                    "{" +
                        "deviceType: 'mobile'" +
                    "}" +
                    "</script>" +
                "</head>" +
                "<body>" +
                    "<div id='%s'></div>" +
                    "<script>" +
                        "%s" +
                        "try {" +
                            "gigya._.apiAdapters.mobile.showPlugin('%s', %s);" +
                        "} catch (ex) { onJSException(ex); }" +
                    "</script>" +
                "</body>";

        return String.format(template, GSWebBridge.REDIRECT_URL_SCHEME, ON_JS_EXCEPTION, GSWebBridge.REDIRECT_URL_SCHEME, ON_JS_LOAD_ERROR,
                jsLoadingTimeout, GSAPI.getInstance().getAPIKey(), CONTAINER_ID, enableTestNetworksScript, plugin, params);
    }

    private void onError(GSObject error) {
        if (listener != null)
            listener.onError(this, error);
    }
}
