package com.smartworks.zeropercent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED) &&
                context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE)
                        .getBoolean("autostart", false)) {
            Log.d(TAG, "Boot completed, autostarting");
            Intent monitorIntent = new Intent(context, BatteryMonitorService.class);
            context.startService(monitorIntent);
        }
    }
}
