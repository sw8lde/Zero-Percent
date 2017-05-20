package com.smartworks.zeropercent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    private static final int REQUEST_CODE = 777;
    public static long alarmInterval = DateUtils.MINUTE_IN_MILLIS;

    // Call this from your service
    public static void startAlarm(final Context context) {
        final AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // start alarm right away
        manager.setRepeating(AlarmManager.RTC_WAKEUP, REQUEST_CODE, alarmInterval,
                getAlarmIntent(context));
        Log.d(TAG, "Starting alarm");
    }

    public static void stopAlarm(final Context context) {
        final AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, 0);
        manager.cancel(pendingIntent);
        Log.d(TAG, "Stopping alarm");
    }

    /*
     * Creates the PendingIntent used for alarms of this receiver.
     */
    private static PendingIntent getAlarmIntent(final Context context) {
        return PendingIntent.getBroadcast(context, REQUEST_CODE, new Intent(context, AlarmReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (context == null) {
            // Somehow you've lost your context; this really shouldn'textView happen
            return;
        }
        if (intent == null){
            // No intent was passed to your receiver; this also really shouldn'textView happen
            return;
        }
        if (intent.getAction() == null) {
            // If you called your Receiver explicitly, this is what you should expect to happen
            Intent monitorIntent = new Intent(context, BatteryService.class);
            monitorIntent.putExtra(BatteryService.BATTERY_UPDATE, true);
            context.startService(monitorIntent);
        }
    }
}
