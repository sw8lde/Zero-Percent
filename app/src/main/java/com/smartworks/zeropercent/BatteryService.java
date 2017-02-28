package com.smartworks.zeropercent;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

public class BatteryService extends Service {
    private static final String TAG = "BatteryService";
    public static final String BATTERY_UPDATE = "battery";
    public static final String HANDLE_REBOOT = "reboot";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.hasExtra(BootReceiver.ACTION_BOOT)){
            AlarmReceiver.startAlarms(this.getApplicationContext());
        }
        if (intent != null && intent.hasExtra(BATTERY_UPDATE)){
            new BatteryCheckAsync().execute();
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class BatteryCheckAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... arg0) {
            //Battery State check - create log entries of current battery state
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = BatteryService.this.registerReceiver(null, ifilter);

            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            Log.i(TAG, "Battery is charging: " + isCharging);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            double percent = level / scale;
            Log.i(TAG, "Battery charge level: " + percent);
            if(percent < 1) {
                sendMessages();
            }
            return null;
        }

        protected void onPostExecute(){
            BatteryService.this.stopSelf();
        }
    }

    private void sendMessages() {

    }
}
