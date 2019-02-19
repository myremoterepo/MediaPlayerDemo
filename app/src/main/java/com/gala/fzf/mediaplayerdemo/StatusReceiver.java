package com.gala.fzf.mediaplayerdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.tvguo.gala.PSServiceManager;


public class StatusReceiver extends BroadcastReceiver {
    private static final String TAG = StatusReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                Log.d(TAG, "network is available");
                PSServiceManager.getInstance().startService();
            } else {
                Log.d(TAG, "network is unavailable");
                PSServiceManager.getInstance().stopService();
            }
        } else {
            Log.d(TAG, "get ConnectivityManager fail!");
        }
    }
}
