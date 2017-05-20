package com.smartworks.zeropercent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryMonitorReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryMonitorReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        int percent = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        Log.d(TAG, "Level: " + percent + "\nCharging: " + isCharging);

        if(percent < prefs.getInt("crit_percent", 5)) {
            sendMessage(context, prefs);
        }
    }

    private void sendMessage(Context context, SharedPreferences prefs) {

    }
}
