package com.example.n15demo01;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.Date;

/**
 * @author		Levy Albert albert.school2015@gmail.com
 * @version     3.0
 * @since		30/7/2023
 * A basic demo application to show the use of four different Alarm Managers:
 * 1. Set to a difference (30 seconds) if time from now
 * 2. Set to a desired Time Of Day
 * 3. Set a Daily reminder
 * 4. Set an alarm clock "with snooze"
 * The application also show how to cancel a desired alarm
 */
public class MainActivity extends AppCompatActivity {
    private String TAG = "MainActivity";
    private TextView tV;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private int ALARM_RQST_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tV = findViewById(R.id.tV);
        Log.d(TAG, "onCreate: Activity created");
    }

    /**
     * Called when the activity enters the resumed state.
     * <p>Loads the latest alarm request code from SharedPreferences to ensure unique
     * alarm request code continuity.
     * </p>
     */
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences alarmsPrefs = getSharedPreferences("AlarmsPrefs", MODE_PRIVATE);
        ALARM_RQST_CODE = alarmsPrefs.getInt("last_request_code", 0);
        Log.d(TAG, "onResume: Loaded last_request_code = " + ALARM_RQST_CODE);
    }

    /**
     * Set to a difference (30 seconds) if time from now Alarm method
     * <p> Demonstrate elapsed realtime alarm after 30 seconds
     * </p>
     *
     * @param view the view that triggered the method
     */
    public void hMinuteAlarm(View view) {
        ALARM_RQST_CODE++;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg",String.valueOf(ALARM_RQST_CODE)+" 30 seconds");
        alarmIntent = PendingIntent.getBroadcast(this,
                ALARM_RQST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 30*1000, alarmIntent);
        Calendar calNow = Calendar.getInstance();
        calNow.add(Calendar.SECOND, 30);
        tV.setText(String.valueOf(ALARM_RQST_CODE)+" Alarm in "+String.valueOf(calNow.getTime()));
        Log.i(TAG, "hMinuteAlarm: Set 30-second alarm with requestCode = " + ALARM_RQST_CODE);
    }

    /**
     * Set to a desired Time Of Day Alarm method
     * <p> Demonstrate time of day alarm selected by user
     * </p>
     *
     * @param view the view that triggered the method
     */
    public void todAlarm(View view) {
        openTimePickerDialog(true);
    }

    /**
     * openTimePickerDialog method
     * <p> Open dialog to use to pick time of day
     * </p>
     *
     * @param is24r boolean indicating whether time picker dialog for 24-hour
     */
    private void openTimePickerDialog(boolean is24r) {
        Calendar calendar = Calendar.getInstance();

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), is24r);
        timePickerDialog.setTitle("Choose time");
        timePickerDialog.show();
    }
    TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        /**
         * onTimeSet method
         * <p> Return the time of day picked by the user
         * </p>
         *
         * @param view the time picker view that triggered the method
         * @param hourOfDay the hour the user picked
         * @param minute the minute the user picked
         */
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            Calendar calNow = Calendar.getInstance();
            Calendar calSet = (Calendar) calNow.clone();

            calSet.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calSet.set(Calendar.MINUTE, minute);
            calSet.set(Calendar.SECOND, 0);
            calSet.set(Calendar.MILLISECOND, 0);

            if (calSet.compareTo(calNow) <= 0) {
                calSet.add(Calendar.DATE, 1);
            }
            setAlarm(calSet);
        }
    };

    /**
     * setAlarm method
     * <p> Set the alarm by the time of day thr user picked
     * </p>
     *
     * @param calSet the Calendar object that represent the tod the user selected
     */
    private void setAlarm(Calendar calSet) {
        ALARM_RQST_CODE++;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg",String.valueOf(ALARM_RQST_CODE)+" TOD");
        alarmIntent = PendingIntent.getBroadcast(this,
                ALARM_RQST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP,
                calSet.getTimeInMillis(), alarmIntent);
        saveAlarmParameters(ALARM_RQST_CODE, calSet.getTimeInMillis(), false, false);
        tV.setText(String.valueOf(ALARM_RQST_CODE)+" Alarm in "+String.valueOf(calSet.getTime()));
        Log.i(TAG, "setAlarm: Set TOD alarm with requestCode = " + ALARM_RQST_CODE + " for " + calSet.getTime());
    }

    /**
     * Set a Daily reminder Alarm method
     * <p> Demonstrate inexact repeating daily rtc (4:00 PM) alarm
     * </p>
     *
     * @param view the view that triggered the method
     */
    public void dailyAlarm(View view) {
        ALARM_RQST_CODE++;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg",String.valueOf(ALARM_RQST_CODE)+" Daily");
        alarmIntent = PendingIntent.getBroadcast(this,
                ALARM_RQST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        // Set the alarm to start at approximately 2:00 PM
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();

        calSet.setTimeInMillis(System.currentTimeMillis());
        calSet.set(Calendar.HOUR_OF_DAY, 14);
        calSet.set(Calendar.MINUTE, 0);
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);
        if (calSet.compareTo(calNow) <= 0) {
            calSet.add(Calendar.DATE, 1);
        }
        // Set inexact repeating & interval to AlarmManager.INTERVAL_DAY
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calSet.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
        saveAlarmParameters(ALARM_RQST_CODE, calSet.getTimeInMillis(), true, false);
        tV.setText(String.valueOf(ALARM_RQST_CODE)+" Alarm in "+String.valueOf(calSet.getTime()));
        Log.i(TAG, "dailyAlarm: Set daily repeating alarm with requestCode = " + ALARM_RQST_CODE + " for " + calSet.getTime());
    }

    /**
     * Set an alarm clock "with snooze" Alarm method
     * <p> Demonstrate rtc alarm clock (7:00 AM) repeating every 5 minutes
     * </p>
     *
     * @param view the view that triggered the method
     */
    public void clockAlarm(View view) {
        ALARM_RQST_CODE++;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg",String.valueOf(ALARM_RQST_CODE)+" Clock");
        alarmIntent = PendingIntent.getBroadcast(this,
                ALARM_RQST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        // Set the alarm to start at 07:00 AM
        Calendar calNow = Calendar.getInstance();
        Calendar calSet = (Calendar) calNow.clone();

        calSet.setTimeInMillis(System.currentTimeMillis());
        calSet.set(Calendar.HOUR_OF_DAY, 7);
        calSet.set(Calendar.MINUTE, 0);
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);
        if (calSet.compareTo(calNow) <= 0) {
            calSet.add(Calendar.DATE, 1);
        }
        // Set the repeating interval to 5 minutes
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                calSet.getTimeInMillis(),
                1000 * 60 * 5, alarmIntent);
        saveAlarmParameters(ALARM_RQST_CODE, calSet.getTimeInMillis(), true, true);
        tV.setText(String.valueOf(ALARM_RQST_CODE)+" Alarm in "+String.valueOf(calSet.getTime()));
        Log.i(TAG, "clockAlarm: Set snooze repeating alarm with requestCode = " + ALARM_RQST_CODE + " for " + calSet.getTime());
    }


    /**
     * Saves alarm parameters into SharedPreferences for persistence across application restarts.
     * <p>Stores the alarm execution time under a key corresponding to its type (one-time,
     * daily repeat, or snooze repeat) incorporating the request code, and updates the latest
     * request code.
     * </p>
     *
     * @param requestCode  The unique request code identifying the alarm.
     * @param timeInMillis The scheduled trigger time for the alarm in milliseconds.
     * @param repeat       Boolean indicating whether the alarm is a repeating alarm.
     * @param snooze       Boolean indicating whether the repeating alarm has a snooze interval.
     */
    private void saveAlarmParameters(int requestCode, long timeInMillis, boolean repeat, boolean snooze) {
        SharedPreferences alarmsPrefs = getSharedPreferences("AlarmsPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = alarmsPrefs.edit();
        // Storing the alarm under a unique key that incorporates the request code
        if (repeat) {
            if (snooze) {
                editor.putLong("snooze_repeat_alarm_time_" + requestCode, timeInMillis);
            } else {
                editor.putLong("repeat_alarm_time_" + requestCode, timeInMillis);
            }
        } else {
            editor.putLong("alarm_time_" + requestCode, timeInMillis);
        }
        // Storing the latest request code to prevent future identifier collisions
        editor.putInt("last_request_code", requestCode);
        editor.apply();
        Log.d(TAG, "saveAlarmParameters: Saved alarm [requestCode=" + requestCode + ", timeInMillis=" + timeInMillis + ", repeat=" + repeat + ", snooze=" + snooze + "]");
    }

    /**
     * cancel Alarm method
     * <p> Demonstrate canceling the last alarm that was set
     * </p>
     *
     * @param view the view that triggered the method
     */
    public void cancelAlarm(View view) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this,
                ALARM_RQST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);

        // Deleting the alarm from SharedPreferences when canceling it
        SharedPreferences alarmsPrefs = getSharedPreferences("AlarmsPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = alarmsPrefs.edit();
        editor.remove("alarm_time_" + ALARM_RQST_CODE);
        editor.remove("repeat_alarm_time_" + ALARM_RQST_CODE);
        editor.remove("snooze_repeat_alarm_time_" + ALARM_RQST_CODE);
        editor.apply();

        tV.setText("Alarm "+String.valueOf(ALARM_RQST_CODE)+" canceled!");
        Log.w(TAG, "cancelAlarm: Canceled alarm with requestCode = " + ALARM_RQST_CODE);
        if (ALARM_RQST_CODE > 1) {
            ALARM_RQST_CODE--;
        }
    }
}