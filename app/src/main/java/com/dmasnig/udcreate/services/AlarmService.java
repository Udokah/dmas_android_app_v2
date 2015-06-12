package com.dmasnig.udcreate.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.activities.BaseActivity;
import com.dmasnig.udcreate.databases.ORMDatabaseManager;
import com.dmasnig.udcreate.databases.QuotesDatabase;
import com.dmasnig.udcreate.utilities.Config;
import com.dmasnig.udcreate.utilities.Lib;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by udo on 6/11/15.
 */

public class AlarmService extends Service {
    private Handler mHandler = new Handler();

    Context context = this;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.removeCallbacks(r);
        mHandler.post(r);
        return super.onStartCommand(intent, flags, startId);
    }

    private final int mNotificationId = 5514;

    // Build your notification widget
    void createNotification(String message) {

        // Specify the intent to be triggered when the Notification is clicked on
        // IMPORTANT: should open them message fragment
        Intent resultIntent = new Intent(this, BaseActivity.class);
        resultIntent.putExtra("show-fragment", 1) ; // show messages fragment
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        long[] pattern = {500,500,500,500,500,500,500,500,500};

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("New Prayer Message")
                        .setAutoCancel(true)
                        .setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.song))
                        //.setDefaults(Notification.DEFAULT_SOUND)
                        .setVibrate(pattern)
                        .setNumber(1)
                        .setPriority(2)
                        .setContentText(message);
        mBuilder.setContentIntent(resultPendingIntent);

        // Get Notification manager service
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // mNotificationId and ID used to represent Notifications from our application
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
    }


    /**
     * Fetch and store message from server in Database
     */
    private void fetchMessageFromServer(){
        String fetchSubscriptionInfoURL = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_DAILY_MESSAGE)
                .build()
                .toUri().toString();

        DiskBasedCache cache = new DiskBasedCache(getExternalCacheDir(), 1024 * 1024); // 1MB cap
        BasicNetwork network = new BasicNetwork(new HurlStack());
        RequestQueue mRequestQueue = new RequestQueue(cache, network);
         /* Prepare Volley Request */
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                fetchSubscriptionInfoURL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                parseServerResponse(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Lib.Toast(error.getMessage(), context);
            }
        }
        ) {
            /* Set Custom Headers */
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AUTHORIZATION", Lib.getFromPrefs(Config.PROPERTY_API_KEY,context));
                return headers;
            }
        };
        mRequestQueue.add(jsonObjReq);
        mRequestQueue.start();
    }

    /**
     * Parse Json response
     * @param response jsonObject from server
     */
    private void parseServerResponse(JSONObject response){
        try {
            String message = response.getString("message") ;
            Date now = new Date();
            String date = new SimpleDateFormat("EEE, d MMM yyyy").format(now);

            /* Store in Database */
            QuotesDatabase quote = new QuotesDatabase();
            quote.setMessage(message);
            quote.setDate(date);
            quote.setHasBeenRead(true);
            ORMDatabaseManager.getInstance().addQuote(quote);

            createNotification(message); // create notification here

            /*Update preferences set fetched to true */
           /* SharedPreferences prefs = Lib.getDevicesPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Config.PROPERTY_FETCHED_TODAY,true);
            editor.commit();*/

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * check if its time 2:50pm
     * and it is not sunday
     * @return boolean
     */
    private boolean itsTime(){
        Calendar now = Calendar.getInstance();
        if(now.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && now.get(Calendar.HOUR_OF_DAY) == 14 && now.get(Calendar.MINUTE) == 50){
            return true;
        }else{
            return false;
        }
    }

    /**
     * get Last message from database
     * @return String
     */
    /*private String getLastMessage(){
        QuotesDatabase quoteObj = ORMDatabaseManager.getInstance().getLastQuote();
        quoteObj.setHasBeenRead(true);
        *//* Set quote as Read *//*
        ORMDatabaseManager.getInstance().updateQuoteStatus(quoteObj);
        return quoteObj.getMessage();
    }*/

    // A runnable to perform actions when the Alarm is fired
    private Runnable r = new Runnable() {
        public void run() {
            new Thread(new Runnable() {
                public void run() {
                    if(itsTime()){
                        fetchMessageFromServer();
                    }
                }
            }).start();
        }
    };
}
