package com.gigya.socialize.android.event;

import com.gigya.socialize.GSObject;

/**
 * Listener for session events.
 *
 * @deprecated use {@link com.gigya.socialize.android.event.GSSocializeEventListener} instead.
 */
@Deprecated
public interface GSEventListener {
    public void onLogin(String provider, GSObject user, Object context);

    public void onLogout(Object context);

    public void onConnectionAdded(String provider, GSObject user, Object context);

    public void onConnectionRemoved(String provider, Object context);
}