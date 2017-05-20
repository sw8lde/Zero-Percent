package com.smartworks.zeropercent;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.util.Log;

public class BatteryService extends Service {
    private static final String TAG = "BatteryService";
    public static final String BATTERY_UPDATE = "battery";
    public static final String HANDLE_REBOOT = "reboot";
    public static Boolean isRunning = false;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;

        if(intent != null && intent.hasExtra(BootReceiver.ACTION_BOOT)){
           // AlarmReceiver.startAlarm(this.getApplicationContext());
        }
        if(intent != null && intent.hasExtra(BATTERY_UPDATE)){
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
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(getPackageName(), MODE_PRIVATE);

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
            Log.i(TAG, "Battery charge level: " + level + " , " + scale);

            if(percent > prefs.getInt("crit_percent", 5)) {
                AlarmReceiver.alarmInterval = prefs.getInt("normal_freq", 5);
            } else {
                AlarmReceiver.alarmInterval = prefs.getInt("crit_freq", 5);
            }

            Log.d(TAG, "freq: " + AlarmReceiver.alarmInterval);

            if(percent < 1) {
                sendMessages();
            }
            return null;
        }

        protected void onPostExecute(){
            Log.d(TAG, "Stopping service");
            BatteryService.this.stopSelf();
        }
    }

    private void sendMessages() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }
}
