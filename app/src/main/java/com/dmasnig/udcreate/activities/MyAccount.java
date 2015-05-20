package com.dmasnig.udcreate.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.utilities.Config;
import com.dmasnig.udcreate.utilities.Lib;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by ud on 10/12/14.
 */
public class MyAccount extends ActionBarActivity {

    private ActionBar actionbar ;
    private Context context ;
    private String TAG = "volley stuff";
    private String last_subscription , subscription_type , expiry_date  ;
    private int subscription_status ;

    private String fetchSubscriptionInfoURL;
    private RequestQueue mRequestQueue;
    private Cache cache  ;
    private Network network ;
    private JsonObjectRequest jsonObjReq ;

    private TextView LAST;
    private TextView TYPE;
    private TextView EXPIRES;
    private TextView STATUS;

    private final static int cacheExpires = 60 * 12 ; // After 12hrs
    private final static int defPinLength = 10 ;

    private ProgressDialog progressDialog ;
    private String apikey ;
    private TextView pincodeTextview ;

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MyAccount.this ;
        setContentView(R.layout.activity_my_account);
        actionbar = getSupportActionBar() ;
        actionbar.setTitle("My Account");
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowHomeEnabled(true);

        progressDialog = new ProgressDialog(this);
        apikey = Lib.getFromPrefs(Config.PROPERTY_API_KEY,context);

        pincodeTextview = (TextView) findViewById(R.id.pincode);

        fetchSubscriptionInfoURL = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_SUB_INFO)
                .build()
                .toUri().toString();

        cache = new DiskBasedCache(context.getExternalCacheDir(), 1024 * 1024); // 1MB cap
        cache.initialize();
        network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);

        /* Now run update */
        runUpdater() ;
    }

    /* create refresh button */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.my_account_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_refresh:
                refreshAccount();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Run the updates and
     * decide whether to fetch from
     * cache or server based on time
     */
    private void runUpdater(){
        boolean cacheisValid = false ;
        // If Cache exists
        if(mRequestQueue.getCache().get(fetchSubscriptionInfoURL) != null) {
            Log.i(TAG, "cache exists");
                long serverDate = mRequestQueue.getCache().get(fetchSubscriptionInfoURL).serverDate;
                Calendar calendar = Calendar.getInstance();
                // If cache has not exceeded expiry date
            long diff = getMinutesDifference(serverDate, calendar.getTimeInMillis());
            Log.i(TAG, "difference is: " + diff );

                if ( diff >= cacheExpires) {
                    // remove cache if it has expired
                    Log.i(TAG, "cache has expired");
                mRequestQueue.getCache().invalidate(fetchSubscriptionInfoURL, true);

            }else{
                    Log.i(TAG, "cache is valid");
                cacheisValid = true ;
            }
        }

        /*if(cacheisValid){
            fetchFromCache();
        }else{
            if (Lib.deviceIsConnected(this)) {
                fetchFromServer();
            } else {
                Lib.Alert("Please enable internet connectivity to update your account details", this);
                fetchFromCache();
            }
        }*/

        /* Fetch account update from server */
        if (Lib.deviceIsConnected(this)) {
            fetchFromServer();
        } else {
            Lib.Alert("Please enable internet connectivity to update your account details", this);
        }
    }


    private static long getMinutesDifference(long timeStart,long timeStop){
        long diff = timeStop - timeStart;
        long diffMinutes = diff / (60 * 1000);
        return  diffMinutes;
    }


    /**
     * Parse Json response
     * @param response jsonObject from server
     */
    private void parseJSON(JSONObject response){
        try {
            last_subscription = response.getString("last") ;
            subscription_type = response.getString("type") ;
            expiry_date = response.getString("expires") ;
            subscription_status = Integer.parseInt(response.getString("status"));

            updateView(); // Update the view
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Fetch new account info
     * from the server
     */
    private void fetchFromServer(){

         /* Prepare Volley Request */
        Log.i(TAG, "Preparing request");
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                fetchSubscriptionInfoURL, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                Log.i(TAG,"Parsing response");
                parseJSON(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Lib.Toast(error.getMessage(), context);
            }
        }){
            /* Set Custom Headers */
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AUTHORIZATION", apikey);
                return headers;
            }
        };
        mRequestQueue.add(jsonObjReq);
        mRequestQueue.start();
    }


    /**
     * Fetch Account info from Disk Cache
     */
    private void fetchFromCache(){
        Log.i(TAG,"getting from cache");
        String cachedData = new String(mRequestQueue.getCache().get(fetchSubscriptionInfoURL).data);

        try {
            JSONObject response = new JSONObject(cachedData);
            parseJSON(response);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void updateView(){
        LAST = (TextView) findViewById(R.id.last_subscribed);
        TYPE = (TextView) findViewById(R.id.subscription_type);
        EXPIRES = (TextView) findViewById(R.id.subscription_expires);
        STATUS = (TextView) findViewById(R.id.account_status);

        LAST.setText("Last Subscription: " + last_subscription);
        TYPE.setText("Subscription type: " + subscription_type);
        EXPIRES.setText("Expires: " + expiry_date);

        if( subscription_status == 1 ){ // is active
            STATUS.setTextAppearance(this,R.style.account_active_bar);
            STATUS.setText("Active");
            STATUS.setBackgroundResource(R.drawable.account_active_bg);
        }else{
            STATUS.setTextAppearance(this,R.style.account_inactive_bar);
            STATUS.setText("Inactive");
            STATUS.setBackgroundResource(R.drawable.account_inactive_bg);
        }
        STATUS.setPadding(20,20,20,20);
    }


    /**
     * Subscribe button is pressed
     * @param v
     */
    public void subscribeBtnPressed(View v){
        String pincode = pincodeTextview.getText().toString();

        if( pincode.length() < defPinLength){
            Lib.Toast("Invalid pincode",this);
        }else{
            if(Lib.deviceIsConnected(this)){
                subscribeUser(pincode) ;
            }else{
                Lib.Toast(Config.CONNECTIVITY_SERVICE,this);
            }
        }
    }


    /**
     * Subscribe the new user using volley
     * @param pincode pincode of the user
     */
    private void subscribeUser(final String pincode){
        progressDialog.setMessage("working...");
        progressDialog.show();

        String Url = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_SUBSCRIBE)
                .build()
                .toUri().toString();

         /* Prepare Volley Request */
        Log.i(TAG, "Preparing request");
        jsonObjReq = new JsonObjectRequest(Request.Method.PUT,
                Url, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                Log.i(TAG,"Parsing response");
                parseSubscriptionResponse(response) ;
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Lib.Toast(error.getMessage(), context);
            }
        }){
            /* Set Custom Headers */
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AUTHORIZATION", apikey);
                headers.put("PINCODE", pincode);
                return headers;
            }
        };
        mRequestQueue.add(jsonObjReq);
        mRequestQueue.start();
    }


    private void parseSubscriptionResponse(JSONObject response){
        progressDialog.dismiss();
        try {
            String error = response.getString("error") ;
            String message = response.getString("message") ;

            if(error.equalsIgnoreCase("false")){
                pincodeTextview.setText("");
                fetchFromServer(); //Update account info
            }

            Lib.Toast(message,this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /* refresh account */
    public void refreshAccount() {
        Lib.Toast("refresh baby", this);
    }
}
