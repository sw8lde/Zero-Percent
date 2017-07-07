package com.smartworks.zeropercent;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class BatteryMonitorService extends Service {
    private static final String TAG = "BatteryMonitorService";
    public static boolean isRunning = false;
    private BatteryMonitorReceiver batteryMonitorReceiver;

    public BatteryMonitorService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        batteryMonitorReceiver = new BatteryMonitorReceiver();

        IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        batteryFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        registerReceiver(batteryMonitorReceiver, batteryFilter);

        isRunning = true;

        Log.d(TAG, "Started");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batteryMonitorReceiver);
        isRunning = false;
        Log.d(TAG, "Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
