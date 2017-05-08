package com.gigya.socialize.android.event;

import android.webkit.WebView;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.GSResponse;
import com.gigya.socialize.android.GSLoginRequest.LoginRequestType;

/**
 * A listener interface for receiving GSWebBridge events.
 *
 * @see com.gigya.socialize.android.GSWebBridge
 */
public abstract class GSWebBridgeListener {
    /**
     * Invoked before the web bridge begins a login process.
     *
     * @param webView     The attached web view that initiated the login.
     * @param requestType A LoginRequestType value specifying the login endpoing - either "login", "addConnection" or "socialLogin" (for accounts namespace).
     * @param params      The login request parameters.
     */
    public void beforeLogin(WebView webView, LoginRequestType requestType, GSObject params) { }

    /**
     * Invoked after the login process is finished.
     *
     * @param webView  The attached web view that initiated the login.
     * @param response A response object with the login result.
     */
    public void onLoginResponse(WebView webView, GSResponse response) { }

    /**
     * Invoked when a Gigya JavaScript SDK plugin fires a <a target="_blank" href="http://developers.gigya.com/display/GD/Events">custom event</a> inside the web view
     * (For example - commentUI's <a target="_blank" href="http://developers.gigya.com/display/GD/comments.showCommentsUI%20JS#comments.showCommentsUIJS-onCommentSubmittedEventData">commentSubmitted</a>).
     *
     * @param webView     The attached web view that contains the origin plugin.
     * @param event       The event object.
     * @param containerID The ID of the HTML element that contains the plugin.
     */
    public abstract void onPluginEvent(WebView webView, GSObject event, String containerID);
}
