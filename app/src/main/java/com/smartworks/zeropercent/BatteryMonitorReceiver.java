package com.smartworks.zeropercent;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class BatteryMonitorReceiver extends BroadcastReceiver {
    private static final String TAG = "BatteryMonitorReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

        int percent = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        Log.d(TAG, "Level: " + percent + "\nScale: " + scale + "\nCharging: " + isCharging);

        if(percent < prefs.getInt("crit_percent", 5) && !prefs.getBoolean("sent_message", false)) {
            sendMessage(context, prefs);
        } else if(prefs.getBoolean("sent_message", false) && percent > prefs.getInt("crit_percent", 5)) {
            Log.d(TAG, "Reset send message");
            prefs.edit().putBoolean("sent_message", false).apply();
        }
    }

    private void sendMessage(Context context, SharedPreferences prefs) {
        prefs.edit().putBoolean("sent_message", true).apply();
        Log.d(TAG, "Send Message");

        ArrayList<Contact> cons = SelectContactsActivity.getSelectedContacts(context);
        String message = prefs.getString("message", context.getString(R.string.default_message));

        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage("2246781810", null, message, null, null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}