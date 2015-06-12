package com.dmasnig.udcreate.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dmasnig.udcreate.services.TimeService;

/**
 * Created by udo on 6/11/15.
 */
public class BootReceiver extends BroadcastReceiver{
    public void onReceive(Context context, Intent intent) {

        // When the phone boots, start the TimeService so that it starts counting behind the scene
        context.startService(new Intent(context,TimeService.class));
    }
}
