package com.smartworks.zeropercent;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

        if(percent < prefs.getInt("crit_percent", 5) && !prefs.getBoolean("sent_message", false) && !isCharging) {
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

        if(prefs.getBoolean("add_loc", false) &&
                ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, new LocationListener() {
                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {}
                @Override
                public void onProviderEnabled(String s) {}
                @Override
                public void onProviderDisabled(String s) {}
                @Override
                public void onLocationChanged(final Location location) {}
            });
            Location myLocation = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
            double longitude = myLocation.getLongitude();
            double latitude = myLocation.getLatitude();
            message += "\nmaps.google.com/maps?q=" + latitude + "," + longitude;
        }

        try {
            for(Contact c: cons) {
                SmsManager sms = SmsManager.getDefault();
                sms.sendTextMessage(c.phone, null, message, null, null);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}