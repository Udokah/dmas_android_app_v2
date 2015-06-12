package com.dmasnig.udcreate.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dmasnig.udcreate.services.AlarmService;

/**
 * Created by udo on 6/11/15.
 */
public class AlarmReceiver extends BroadcastReceiver {

    // Start the AlarmService when the Alarm rings
    public void onReceive(Context context, Intent intent) {
        Intent resultIntent = new Intent(context, AlarmService.class);
        context.startService(resultIntent);
    }
}
