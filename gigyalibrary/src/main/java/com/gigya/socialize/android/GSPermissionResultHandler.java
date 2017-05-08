package com.gigya.socialize.android;

import java.util.List;

/**
 * Interface for listening to permission requests responses. Passed to GSAPI.requestFacebookPublishPermissions
 */
public abstract interface GSPermissionResultHandler {
    public void onResult(boolean granted, Exception exception, List<String> declinedPermissions);
}