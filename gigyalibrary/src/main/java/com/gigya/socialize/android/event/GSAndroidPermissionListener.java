package com.gigya.socialize.android.event;

/**
 * Listener for handling Android run-time permission request
 */
public interface GSAndroidPermissionListener {
    void onAndroidPermissionsResult(String permissions[], int[] grantResults);
}
