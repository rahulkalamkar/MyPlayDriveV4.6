package com.gigya.socialize.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.gigya.socialize.GSObject;
import com.gigya.socialize.android.GSAPI;
import com.gigya.socialize.android.GSPluginFragment;
import com.gigya.socialize.android.event.GSDialogListener;
import com.gigya.socialize.android.event.GSPluginListener;


public class PluginPresentor {
    public void show(final String plugin, final GSObject params, final GSPluginListener pluginListener, final GSDialogListener dialogListener) {
        HostActivity.create(GSAPI.getInstance().getContext(), new HostActivity.HostActivityHandler() {
            @Override
            public void onCreate(final FragmentActivity activity, Bundle savedInstanceState) {
                GSPluginFragment pluginFragment = (GSPluginFragment) activity.getSupportFragmentManager().findFragmentByTag("PluginDialog");
                if (pluginFragment == null) {
                    pluginFragment = GSPluginFragment.newInstance(plugin, params, true);
                    pluginFragment.show(activity.getSupportFragmentManager(), "PluginDialog");
                }

                pluginFragment.setRetainInstance(true);
                pluginFragment.setPluginListener(pluginListener);
                pluginFragment.setOnDismissListener(new GSDialogListener() {
                    @Override
                    public void onDismiss(boolean wasCanceled, GSObject event) {
                        activity.finish();
                        if (dialogListener != null) {
                            dialogListener.onDismiss(wasCanceled, event);
                        }
                    }
                });
            }

            @Override
            public void onActivityResult(FragmentActivity activity, int requestCode, int resultCode, Intent data) {
                Log.d("", "");
            }

            @Override
            public void onCancel(FragmentActivity activity) {
            }

            @Override
            public void onStart(FragmentActivity activity) {
            }
        });
    }

}
