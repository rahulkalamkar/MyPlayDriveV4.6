package com.gigya.socialize.android.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ProgressBar;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.android.GSAPI;


public class WebViewFragment extends DialogFragment {
    public interface WebViewFragmentHandler {
        public void onResult(GSObject result);
    }

    private static SparseArray<WebViewFragmentHandler> handlers = new SparseArray<WebViewFragmentHandler>();
    private int handlerId;
    private WebView webview;
    private String resultPrefix;
    private String url;
    private String title;
    private String tag;
    private boolean isTransparent;
    private boolean webViewWrapContent = false;
    private ProgressBar progressBar;

    public WebViewFragment() {
        super();
    }

    public static WebViewFragment create(FragmentActivity activity, String tag, String title, String url, String resultPrefix, WebViewFragmentHandler handler, Boolean isTransparent) {
        WebViewFragment webview = new WebViewFragment();
        webview.title = title;
        webview.url = url;
        webview.resultPrefix = resultPrefix;
        webview.tag = tag;
        webview.isTransparent = isTransparent;

        int id = handler.hashCode();
        handlers.put(id, handler);
        webview.handlerId = id;

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(webview, tag);
        ft.commit();
        return webview;
    }

    public void show(FragmentActivity activity) {
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        }

        setShowsDialog(!isTransparent);

        webview = new WebView(getActivity());
        webview.setVerticalScrollBarEnabled(true);
        webview.setHorizontalScrollBarEnabled(true);

        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setUseWideViewPort(true);
        webview.setInitialScale(1);
        webview.setFocusable(true);

        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return true;
            }
        });

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (GSAPI.OPTION_TRACE) {
                    Log.d("GigyaWebViewFragment", "Navigating to " + url);
                }

                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                if (getShowsDialog() && !webViewWrapContent)
                    redrawWebViewInDialog(webview);

                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(resultPrefix)) {
                    GSObject resultParams = new GSObject();
                    resultParams.parseURL(url.replace("gsapi", "http"));
                    finish(resultParams);
                    return true;
                }

                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                GSObject resultParams = new GSObject();
                resultParams.put("errorCode", errorCode);
                resultParams.put("description", description);
                resultParams.put("failingUrl", failingUrl);
                finish(resultParams);
            }

//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                handler.proceed();
//            }
        });

        webview.loadUrl(url);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        Activity activity = getActivity();
        Display display = getDisplay();
        if (activity == null || display == null)
            return null;

        if (getShowsDialog()) {
            int webViewWidth = Math.min(display.getWidth(), display.getHeight()) * 9 / 10;
            int webViewHeight = (savedInstanceState == null) ? display.getHeight() * 9 / 10 : LayoutParams.WRAP_CONTENT;
            webview.setLayoutParams(new LayoutParams(webViewWidth, webViewHeight));
        } else {
            webview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }

        FrameLayout layout = new FrameLayout(activity);
        layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        if (webview.getParent() != null)
            ((FrameLayout) webview.getParent()).removeView(webview);

        layout.addView(webview);

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
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        if (title != null && title.length() > 0) {
            dialog.setTitle(title);
        } else {
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if(getActivity()!=null)
            getActivity().onBackPressed();
        try{
            super.onCancel(dialog);
        }catch (Exception e){}

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("url", url);
        outState.putBoolean("isTransparent", isTransparent);
        outState.putString("title", title);
        outState.putString("tag", tag);
        outState.putString("resultPrefix", resultPrefix);
        outState.putInt("handlerId", handlerId);
    }

    private void restoreInstanceState(Bundle state) {
        url = state.getString("url");
        isTransparent = state.getBoolean("isTransparent");
        title = state.getString("title");
        tag = state.getString("tag");
        resultPrefix = state.getString("resultPrefix");
        handlerId = state.getInt("handlerId");
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();

        if (dialog != null && getRetainInstance())
            dialog.setDismissMessage(null);

        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (webview != null) {
            webview.setWebChromeClient(null);
            webview.stopLoading();
        }

        super.onDestroy();
    }

    public void finish(GSObject resultParams) {
        try{
            if (GSAPI.OPTION_TRACE) {
                Log.d("GigyaWebViewFragment", "Finished with result: " + resultParams.toJsonString());
            }

        	WebViewFragmentHandler handler = handlers.get(handlerId);
        	if(handler!=null){
          	  handler.onResult(resultParams);
        	}

			if(getActivity()!=null){
		        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
    		    transaction.remove(this);
    		    transaction.commitAllowingStateLoss();
    		    getActivity().getSupportFragmentManager().executePendingTransactions();
			}
        }catch (Exception e){}
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
}
