package com.gigya.socialize.android.event;

import com.gigya.socialize.GSObject;

/**
 * Listener for the dismissal of a plugin dialog, displayed by GSAPI.showPluginDialog
 */
public interface GSDialogListener {
    public void onDismiss(boolean wasCanceled, GSObject event);
}

