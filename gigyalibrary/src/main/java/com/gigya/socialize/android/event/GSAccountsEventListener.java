package com.gigya.socialize.android.event;

import com.gigya.socialize.GSObject;

/**
 * Listener for handling Accounts namespace events.
 */
public interface GSAccountsEventListener {
    /**
     * Invoked after an account was logged in to successfuly.
     *
     * @param account The logged in account object.
     * @param context The context object that was passed to the method initiating the login.
     */
    public void onLogin(GSObject account, Object context);

    /**
     * Invoked after an account has been logged out from.
     *
     * @param context The context object that was passed to the method initiating the logout.
     */
    public void onLogout(Object context);
}
