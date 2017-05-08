package com.gigya.socialize.android.login.providers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.android.GSAPI;


public class WebLoginActivity extends Activity {
    protected interface WebLoginActivityCallback {
        public void onResponse(GSObject response);
    }

    protected static WebLoginActivityCallback callback;
    private Uri loginUrl;
    private Boolean justCreated;

    public static void setCallback(WebLoginActivityCallback callback) {
        WebLoginActivity.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        justCreated = true;

        if (savedInstanceState == null && getIntent() != null) {
            String urlString = getIntent().getExtras().getString("url");
            if (urlString != null) {
                loginUrl = Uri.parse(urlString);
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, loginUrl);
                browserIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(browserIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!justCreated) {
            if (callback != null) {
                GSObject result = new GSObject();
                result.put("errorCode", 200001);
                result.put("errorMessage", "Login process did not complete");
                callback.onResponse(result);
            }

            finish();
        } else {
            justCreated = false;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String packageName = GSAPI.getInstance().getContext().getPackageName();
        Uri data = intent.getData();

        if (data != null && data.getScheme().equals(packageName) && data.getHost().equals("gsapi")) {
            if (callback != null) {
                GSObject response = new GSObject();
                response.parseQueryString(data.getEncodedFragment());
                callback.onResponse(response);
                finish();
            }
        }
    }
}
