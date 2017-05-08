package com.gigya.socialize.android.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseArray;

import com.gigya.socialize.android.GSAPI;


public class HostActivity extends FragmentActivity {
    public interface HostActivityHandler {
        public void onActivityResult(FragmentActivity activity, int requestCode, int resultCode, Intent data);

        public void onCreate(FragmentActivity activity, Bundle savedInstanceState);

        public void onCancel(FragmentActivity activity);

        public void onStart(FragmentActivity activity);
    }

    private static SparseArray<HostActivity> activities = new SparseArray<HostActivity>();
    private static SparseArray<HostActivityHandler> handlers = new SparseArray<HostActivityHandler>();
    private int id;
    private HostActivityHandler handler;
    private ProgressDialog progress;
    private boolean showingProgress;
    private String progressTitle;

    public static HostActivity getActivity(Integer id) {
        return activities.get(id);
    }

    public static void removeActivity(Integer id) {
        activities.remove(id);
    }

    public static void addActivity(Integer id, HostActivity activity) {
        activities.put(id, activity);
    }

    public static HostActivityHandler getHandler(Integer id) {
        return handlers.get(id);
    }

    public static void removeHandler(Integer id) {
        handlers.remove(id);
    }

    public static void addHandler(Integer id, HostActivityHandler handler) {
        handlers.put(id, handler);
    }

    public static Integer create(Context context, HostActivityHandler handler) {
        if (context == null)
            return 0;

        int id = handler.hashCode();
        addHandler(id, handler);
        Intent intent = new Intent(context, HostActivity.class);
        intent.putExtra("id", id);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        return id;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            id = getIntent().getIntExtra("id", 0);
        } else {
            id = savedInstanceState.getInt("handlerId");
            showingProgress = savedInstanceState.getBoolean("showingProgress");
            progressTitle = savedInstanceState.getString("progressTitle");
        }

        addActivity(id, this);
        this.handler = getHandler(id);
        if (this.handler != null) this.handler.onCreate(this, savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        GSAPI.getInstance().handleAndroidPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (this.handler != null) this.handler.onActivityResult(this, requestCode, resultCode, data);
    }

    @Override
    public void finish() {
        removeHandler(id);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (this.handler != null) this.handler.onCancel(this);
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        if (this.handler != null) this.handler.onStart(this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (showingProgress && progress != null && !progress.isShowing()) {
            showProgressDialog(progressTitle);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("handlerId", id);
        outState.putBoolean("showingProgress", showingProgress);
        outState.putString("progressTitle", progressTitle);
    }

    public void showProgressDialog(String title) {
        showingProgress = true;
        progress = ProgressDialog.show(this, null, title);
        progressTitle = title;
    }

    public void dismissProgressDialog() {
        if (showingProgress && progress.isShowing()) {
            progress.dismiss();
        }

        showingProgress = false;
        progressTitle = null;
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        removeActivity(id);
        super.onDestroy();
    }
}
