package com.dmasnig.udcreate.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.activities.BaseActivity;
import com.dmasnig.udcreate.databases.ORMDatabaseManager;
import com.dmasnig.udcreate.databases.QuotesDatabase;
import com.dmasnig.udcreate.utilities.Config;
import com.dmasnig.udcreate.utilities.Lib;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by ud on 10/25/14.
 */
public class BackgroundService extends WakefulIntentService {

    Context context = this ;
    String TAG = "Background Volley stuff" ;

    public BackgroundService() {
        super("BackgroundService");
    }

    /**
     * Asynchronous background operations of service, with wakelock
     */
    @Override
    public void doWakefulWork(Intent intent){

        /* Get today's message if it has not been fetched */
        if(!hasFetchedToday()){
            if(Lib.deviceIsConnected(context)) {
                fetchMessageFromServer();
            }
        }

        /**
         * If it is 2:50pm
         * and if today's message has been
         * fetched from server
         * show the notification
         */
        if( itsTime() && hasFetchedToday() ){
            // Get Unread message today and set to read
            QuotesDatabase quoteObj = ORMDatabaseManager.getInstance().getLastQuote();
            quoteObj.setHasBeenRead(true);

            /* Set quote as Read */
            ORMDatabaseManager.getInstance().updateQuoteStatus(quoteObj);

            /* Create Notification */
            CreateNotification( quoteObj.getMessage() ) ;

        }
    }


    private void CreateNotification(String message){

        Log.i("Notification","creating notification");
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_notification_icon);

        /* Get new sound file for divine mercy notification */
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        mBuilder.setSound(uri);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        mBuilder.setVibrate(pattern);
        mBuilder.setContentTitle("New Divine Mercy Quote");
        mBuilder.setContentText(message);

        Intent resultIntent = new Intent(context, BaseActivity.class);
        resultIntent.putExtra("showfragment" , 1) ;

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(BaseActivity.class);

        // Add the intent that starts the activity at the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultpendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultpendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        notificationManager.notify(Config.NotificationID,mBuilder.build());

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
        Log.i(TAG, "Preparing request");
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                fetchSubscriptionInfoURL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                Log.i(TAG, "Parsing response");
                parseJSON(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Lib.Toast(error.getMessage(), context);
            }
        }
        ) {
            /* Set Custom Headers */
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                String apikey = Lib.getFromPrefs(Config.PROPERTY_API_KEY,context);
                headers.put("AUTHORIZATION", apikey);
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
    private void parseJSON(JSONObject response){
        try {
            String message = response.getString("message") ;
            String date = response.getString("date") ;

            /* Store in Database */
            QuotesDatabase quote = new QuotesDatabase();
            quote.setMessage(message);
            quote.setDate(date);
            quote.setHasBeenRead(false);
            ORMDatabaseManager.getInstance().addQuote(quote);

            /*Update preferences set fetched to true */
            SharedPreferences prefs = Lib.getDevicesPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Config.PROPERTY_FETCHED_TODAY,true);
            editor.commit();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * check if its time 2:50pm
     * and it is not sunday to
     * @return
     */
    private boolean itsTime(){
        boolean ret = false ;
        if( Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 14 ){
            if( Calendar.getInstance().get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY  ){
                ret = true ;
            }
        }
        return ret ;
    }

    /**
     * Check if today's message has been fetched
     * @return boolean
     */
    private boolean hasFetchedToday(){
        SharedPreferences prefs = Lib.getDevicesPreferences(this);
        return prefs.getBoolean(Config.PROPERTY_FETCHED_TODAY,false) ;
    }
}
