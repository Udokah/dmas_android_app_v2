package com.dmasnig.udcreate.listeners;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.dmasnig.udcreate.recievers.ConnectivityReceiver;
import com.dmasnig.udcreate.services.BackgroundService;

import java.util.Calendar;

import static com.commonsware.cwac.wakeful.WakefulIntentService.AlarmListener;

/**
 * Created by ud on 10/25/14.
 */
public class DailyListener implements AlarmListener {

    public void scheduleAlarms(AlarmManager mgr, PendingIntent pi, Context context) {
        // register when enabled in preferences
            Log.i("DailyListener", "Schedule update check...");

            // every hour at :50 minutes
            Calendar calendar = Calendar.getInstance();
            // if it's after or equal :50 min schedule for next hour
            if (Calendar.getInstance().get(Calendar.MINUTE) >= 50) {
                calendar.add(Calendar.HOUR_OF_DAY, 1); // add, not set!
            }

            calendar.set(Calendar.MINUTE, 50);
            calendar.set(Calendar.SECOND, 0);

            mgr.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_HOUR, pi);
    }

    public void sendWakefulWork(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context

                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        // only when connected or while connecting...
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {

            // if we have mobile or wifi connectivity...
            if (((netInfo.getType() == ConnectivityManager.TYPE_MOBILE) )
                    || (netInfo.getType() == ConnectivityManager.TYPE_WIFI)) {
                Log.d("DailyListener", "We have internet, start update check directly now!");

                Intent backgroundIntent = new Intent(context, BackgroundService.class);
                WakefulIntentService.sendWakefulWork(context, backgroundIntent);
            } else {
                Log.d("DailyListener", "We have no internet, enable ConnectivityReceiver!");

                // enable receiver to schedule update when internet is available!
                ConnectivityReceiver.enableReceiver(context);
            }
        } else {
            Log.d("DailyListener", "We have no internet, enable ConnectivityReceiver!");

            // enable receiver to schedule update when internet is available!
            ConnectivityReceiver.enableReceiver(context);
        }
    }

    public long getMaxAge() {
        return (AlarmManager.INTERVAL_DAY + 60 * 1000);
    }
}
