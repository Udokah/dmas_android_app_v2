package com.dmasnig.udcreate.services;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.dmasnig.udcreate.databases.ORMDatabaseManager;
import com.dmasnig.udcreate.recievers.AlarmReceiver;

/**
 * Created by udo on 6/11/15.
 */
public class TimeService extends Service {
    // Time in milliseconds for service to run, currently set to 1 hour seconds
    public static final long NOTIFY_INTERVAL = 60000;


    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();

    public IBinder onBind(Intent intent) {
        return null;
    }

    // When the service starts, we want to stop the alarm and start it up again inorder to avoid two concurrent Alarms
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.removeCallbacks(rs);
        try {
            OFFalarm();
        } catch (Exception e) {

        }
        mHandler.post(rs);
        return super.onStartCommand(intent, flags, startId);
    }

    Runnable rs = new Runnable() {
        public void run() {
            new Thread(new Runnable() {
                public void run() {
                    ONalarm();
                }
            }).start();
        }
    };

    // Register the service to be called by the Alarm, in this case, AlarmService
    private PendingIntent getAlarm() {
        Intent myAlarm = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent recurringAlarm = PendingIntent.getBroadcast(getApplicationContext(), 0, myAlarm, PendingIntent.FLAG_CANCEL_CURRENT);
        return recurringAlarm;
    }

    // Turn on the Alarm, and start counting
    void ONalarm() {
        PendingIntent recurringAlarm = getAlarm();
        Calendar updateTime = Calendar.getInstance();
        AlarmManager alarms = getAlarmMAnager();
        alarms.setInexactRepeating(AlarmManager.RTC_WAKEUP, updateTime.getTimeInMillis() + (NOTIFY_INTERVAL / 3), NOTIFY_INTERVAL, recurringAlarm);
    }

    // Get and return the Alarm Manager System service of the app's instance
    private AlarmManager getAlarmMAnager() {
        AlarmManager alarms = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        return alarms;
    }

    // cancel any running alarms to avoid duplicates
    void OFFalarm() {
        PendingIntent recurringAlarm = getAlarm();
        AlarmManager alarms = getAlarmMAnager();
        alarms.cancel(recurringAlarm);
    }

}