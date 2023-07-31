package com.example.n15demo01;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * @author		Levy Albert albert.school2015@gmail.com
 * @version     1.0
 * @since		30/7/2023
 * Alarm receiver to demonstrate different Alarms firing
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent ri) {
        String msg = ri.getStringExtra("msg");
        Toast.makeText(context, msg+" Alarm has been activated !", Toast.LENGTH_LONG).show();
    }
}