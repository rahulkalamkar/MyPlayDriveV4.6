package com.hungama.hungamamusic.ford.carmode;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.hungama.myplay.activity.util.Logger;

/**
 * Broadcast Receiver for sync connection
 *
 * @Bluetooth
 * @WIFI
 */
public class SyncReceiver extends BroadcastReceiver {
    static final String TAG = "SyncMusicPlayer";

    public void onReceive(Context context, Intent intent) {
        Logger.e(TAG, "SyncReceiver.onReceive()");
        Logger.e(TAG, "Received Intent with action: " + intent.getAction());
        Log.i(TAG, "Received Intent with action: " + intent.getAction());
        final BluetoothDevice bluetoothDevice = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        BluetoothAdapter mBtAdapter;
        ProxyService serviceInstance = ProxyService.getInstance();
        LockScreenActivity mainActivityInstance = LockScreenActivity.getInstance();

        //open proxy when BT is on. Dispose and shutdown service when BT is off
        if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            if ((intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == (BluetoothAdapter.STATE_TURNING_OFF))) {
//					if (serviceInstance != null){
                Log.i(TAG, "Bt off stop service");
                Intent stopIntent = new Intent(context, ProxyService.class);
                stopIntent.putExtras(intent);
                context.stopService(stopIntent);
//					}
            } /*else if ((intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == (BluetoothAdapter.STATE_TURNING_ON))){
                        Log.i(TAG, "Bt on");
//                        if (serviceInstance == null){

                            Log.i(TAG, "Bt on start service");
                            Intent startIntent = new Intent(context, ProxyService.class);
                            startIntent.putExtras(intent);
                            context.startService(startIntent);
//                    }
//                    //if the service was already running when BT turned back on
//                     else {
//                            serviceInstance.reset();
//                    }
				}*/
            //Listen for phone reboot and start service
        } else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBtAdapter != null) {
                if (mBtAdapter.isEnabled()) {
                    Intent startIntent = new Intent(context, ProxyService.class);
                    startIntent.putExtras(intent);
                    context.startService(startIntent);
                }
            }
        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(intent.getAction())) {
            if (serviceInstance == null) {
                Log.i(TAG, "Bt on start service");
                Intent startIntent = new Intent(context, ProxyService.class);
                startIntent.putExtras(intent);
                context.startService(startIntent);
            }
            //if the service was already running when BT turned back on
            else {
                serviceInstance.reset();
            }
        }

        if (intent.getAction().compareTo(Intent.ACTION_MEDIA_BUTTON) == 0) {
            KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                abortBroadcast();
            }

        }

        if (intent.getAction().equals(android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            // signal your service to stop playback
            if (mainActivityInstance != null) {
                mainActivityInstance.pause();
//				PlayerService.service.pause();
            }
        }
    }
}
