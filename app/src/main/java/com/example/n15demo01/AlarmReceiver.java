package com.example.n15demo01;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent ri) {
        String msg = ri.getStringExtra("msg");
        Toast.makeText(context, msg+" Alarm has been activated !", Toast.LENGTH_LONG).show();
    }
}