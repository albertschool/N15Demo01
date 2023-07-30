package com.example.n15demo01;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.Calendar;
/**
 * @author		Levy Albert albert.school2015@gmail.com
 * @version     1.0
 * @since		30/7/2023
 * A basic demo application to show the use of four different Alarm Managers:
 * 1. Set to a difference (minute) if time from now
 * 2. Set to a desired Time Of Day
 * 3. Set a Daily reminder
 * 4. Set an alarm clock "with snooze"
 * The application alse show how to cancel a desired alarm
 */
public class MainActivity extends AppCompatActivity {

    private TextView tV;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    private int TIME_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tV = findViewById(R.id.tV);
    }

    public void minuteAlarm(View view) {
        TIME_REQUEST_CODE++;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg",String.valueOf(TIME_REQUEST_CODE)+" One minute");
        alarmIntent = PendingIntent.getBroadcast(this,
                TIME_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60*1000, alarmIntent);
        Calendar calNow = Calendar.getInstance();
        calNow.add(Calendar.MINUTE, 1);
        tV.setText(String.valueOf(TIME_REQUEST_CODE)+" Alarm in "+String.valueOf(calNow.getTime()));
    }

    public void todAlarm(View view) {
        openTimePickerDialog(true);
    }

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

    private void setAlarm(Calendar calSet) {
        TIME_REQUEST_CODE++;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg",String.valueOf(TIME_REQUEST_CODE)+" TOD");
        alarmIntent = PendingIntent.getBroadcast(this,
                TIME_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), alarmIntent);
        tV.setText(String.valueOf(TIME_REQUEST_CODE)+" Alarm in "+String.valueOf(calSet.getTime()));
    }

    public void dailyAlarm(View view) {
        TIME_REQUEST_CODE++;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg",String.valueOf(TIME_REQUEST_CODE)+" Daily");
        alarmIntent = PendingIntent.getBroadcast(this,
                TIME_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
// Set the alarm to start at approximately 4:00 PM
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
        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, alarmIntent);
        tV.setText(String.valueOf(TIME_REQUEST_CODE)+" Alarm in "+String.valueOf(calSet.getTime()));
    }

    public void clockAlarm(View view) {
        TIME_REQUEST_CODE++;
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("msg",String.valueOf(TIME_REQUEST_CODE)+" Clock");
        alarmIntent = PendingIntent.getBroadcast(this,
                TIME_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
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
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(),
                1000 * 60 * 5, alarmIntent);
        tV.setText(String.valueOf(TIME_REQUEST_CODE)+" Alarm in "+String.valueOf(calSet.getTime()));
    }

    public void cancelAlarm(View view) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(this,
                TIME_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmMgr = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(alarmIntent);
        tV.setText("Alarm "+String.valueOf(TIME_REQUEST_CODE)+" canceled!");
        TIME_REQUEST_CODE--;
    }
}