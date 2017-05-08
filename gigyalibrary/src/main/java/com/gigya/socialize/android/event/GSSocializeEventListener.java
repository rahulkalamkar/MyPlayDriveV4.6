package com.gigya.socialize.android.event;

import com.gigya.socialize.GSObject;

/**
 * Listener for handling Socialize namespace events.
 */
public interface GSSocializeEventListener {
    /**
     * Invoked after an account was logged in to successfuly.
     *
     * @param provider The provider that was used for authentication.
     * @param user     The logged in user object.
     * @param context  The context object that was passed to the method initiating the login.
     */
    public void onLogin(String provider, GSObject user, Object context);

    /**
     * Invoked after a user has logged out.
     *
     * @param context The context object that was passed to the method initiating the logout.
     */
    public void onLogout(Object context);

    /**
     * Invoked after a new provider connection was added to the user.
     *
     * @param provider The provider that was used for authentication.
     * @param user     The updated user object.
     * @param context  The context object that was passed to the method initiating the action.
     */
    public void onConnectionAdded(String provider, GSObject user, Object context);

    /**
     * Invoked after a new provider connection was removed from the user.
     *
     * @param provider The provider that was disconnected from the user.
     * @param context  The context object that was passed to the method initiating the action.
     */
    public void onConnectionRemoved(String provider, Object context);
}
