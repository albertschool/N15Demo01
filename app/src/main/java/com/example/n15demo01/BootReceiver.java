package com.example.n15demo01;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Map;


/**
 * @author		Levy Albert albert.school2015@gmail.com
 * @version     2.0
 * @since		17/11/2026
 * Alarm receiver to handle reboot event
 */
public class BootReceiver extends BroadcastReceiver {
    private String TAG = "BootReceiver";
    private SharedPreferences alarmsPrefs;
    private AlarmManager alarmMgr;
    private long currentTime, alarmTime;
    private int requestCode;
    private Intent alarmIntent;
    private PendingIntent pendingIntent;

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
        Log.i(TAG, "onReceive: Received broadcast action = " + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals("android.intent.action.QUICKBOOT_POWERON")) {
            // Recover the alarms
            alarmsPrefs = context.getSharedPreferences("AlarmsPrefs", Context.MODE_PRIVATE);
            Map<String, ?> allEntries = alarmsPrefs.getAll();
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Log.d(TAG, "onReceive: Recovering " + allEntries.size() + " entries from SharedPreferences");

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                String key = entry.getKey();
                Object obj = entry.getValue();
                if (obj instanceof Integer) {
                    continue;
                } else {
                    alarmTime = (Long) obj;
                }
                currentTime = System.currentTimeMillis();

                // Checking if time has passed
                if (currentTime > alarmTime) {
                    if (key.startsWith("repeat_alarm_time_")) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(currentTime);
                        // Add 1 day to the calendar, preserving the same hour, minute, and second
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                        alarmTime = calendar.getTimeInMillis();
                        Log.d(TAG, "onReceive: Repeating alarm time updated to next day for key = " + key);
                    } else {
                        // clear the alarm for TOD or snooze alarm
                        Log.w(TAG, "onReceive: Alarm time passed for key = " + key + ", clearing stale alarm.");
                        clearStaleAlarm(context, alarmsPrefs, requestCode);
                        continue;
                    }
                }

                // Filtering the keys that belong only to once alarms
                if (key.startsWith("alarm_time_")) {
                    requestCode = Integer.parseInt(key.replace("alarm_time_", ""));
                    // Redefining the Alarm
                    alarmIntent = new Intent(context, AlarmReceiver.class);
                    alarmIntent.putExtra("msg", String.valueOf(requestCode) + " TOD");
                    pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                            alarmIntent, PendingIntent.FLAG_IMMUTABLE);
                    alarmMgr.set(AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);
                    Log.i(TAG, "onReceive: Restored TOD alarm, requestCode = " + requestCode);
                // Filtering the keys that belong to the repeat alarms
                } else if (key.startsWith("repeat_alarm_time_")) {
                    requestCode = Integer.parseInt(key.replace("repeat_alarm_time_", ""));
                    // Restoring repeating alarm
                    alarmIntent = new Intent(context, AlarmReceiver.class);
                    alarmIntent.putExtra("msg",String.valueOf(requestCode)+" Daily");
                    pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                            alarmIntent, PendingIntent.FLAG_IMMUTABLE);
                    alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
                            AlarmManager.INTERVAL_DAY, pendingIntent);
                    Log.i(TAG, "onReceive: Restored Daily repeating alarm, requestCode = " + requestCode);
                // Filtering the keys that belong only to snooze alarms
                } else {
                    requestCode = Integer.parseInt(key.replace("snooze_repeat_alarm_time_", ""));
                    // Restoring snooze alarm
                    alarmIntent = new Intent(context, AlarmReceiver.class);
                    alarmIntent.putExtra("msg",String.valueOf(requestCode)+" Clock");
                    pendingIntent = PendingIntent.getBroadcast(context, requestCode,
                            alarmIntent, PendingIntent.FLAG_IMMUTABLE);
                    alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime,
                            1000 * 60 * 5, pendingIntent);
                    Log.i(TAG, "onReceive: Restored Clock snooze alarm, requestCode = " + requestCode);
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
        Log.w(TAG, "clearStaleAlarm: Removing stale alarm entries for requestCode = " + requestCode);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("alarm_time_" + requestCode);
        editor.remove("repeat_alarm_time_" + requestCode);
        editor.remove("snooze_repeat_alarm_time_" + requestCode);
        editor.apply();

        Toast.makeText(context, "An expired notification was deleted after a restart.", Toast.LENGTH_SHORT).show();
    }
}