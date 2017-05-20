package com.smartworks.zeropercent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class OnBootReceiver extends BroadcastReceiver {
    private static final String TAG = "OnBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        if(intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) && prefs.getBoolean("autostart", false)) {
            Intent serviceIntent = new Intent(context, BatteryService.class);
            context.startService(serviceIntent);
        }
    }
}
