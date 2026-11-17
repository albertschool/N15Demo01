package com.example.n15demo01;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.Map;

public class BootReceiver extends BroadcastReceiver {

    /**
     * Receives broadcast intents, specifically checking for {@link Intent#ACTION_BOOT_COMPLETED}.
     * Reschedules active alarms or clears stale alarms if their target time has passed while
     * the device was off.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        // Verify it's the right intent
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals("android.intent.action.QUICKBOOT_POWERON")) {
            // Recover the alarms
            SharedPreferences alarmsPrefs = context.getSharedPreferences("AlarmsPrefs", Context.MODE_PRIVATE);
            Map<String, ?> allEntries = alarmsPrefs.getAll();
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            long currentTime = System.currentTimeMillis();

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();

                // Filtering the keys that belong only to once alarms
                if (key.startsWith("alarm_time_")) {
                    long alarmTime = (Long) entry.getValue();
                    int requestCode = Integer.parseInt(key.replace("alarm_time_", ""));
                    // Check if the alert time has already passed while the device was off
                    if (alarmTime > currentTime) {
                        // Redefining the Alarm
                        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                        alarmIntent.putExtra("msg", String.valueOf(requestCode) + " TOD");

                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                requestCode, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

                        alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                    } else {
                        // Time has passed
                        clearStaleAlarm(context, alarmsPrefs, requestCode);
                    }
                    // Filtering the keys that belong to the repeat alarms
                } else if (key.startsWith("repeat_alarm_time_")) {
                    long alarmTime = (Long) entry.getValue();
                    int requestCode = Integer.parseInt(key.replace("repeat_alarm_time_", ""));

                    // Restoring repeating alarm
                    Intent alarmIntent = new Intent(context, AlarmReceiver.class);
                    alarmIntent.putExtra("msg", String.valueOf(requestCode) + " Repeat");

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                            requestCode, alarmIntent, PendingIntent.FLAG_IMMUTABLE);

                    alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
                            AlarmManager.INTERVAL_DAY, pendingIntent);
                }
            }
        }
    }

    /**
     * Deletes the expired alarm record from SharedPreferences and shows a toast
     * indicating that an expired notification was deleted after a restart.
     *
     * @param context     The Context in which the method is running.
     * @param prefs       The SharedPreferences instance containing alarm data.
     * @param requestCode The request code of the passed alarm.
     */
    private void clearStaleAlarm(Context context, SharedPreferences prefs, int requestCode) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("alarm_time_" + requestCode);
        editor.remove("repeat_alarm_time_" + requestCode);
        editor.apply();

        Toast.makeText(context, "An expired notification was deleted after a restart.", Toast.LENGTH_SHORT).show();
    }
}