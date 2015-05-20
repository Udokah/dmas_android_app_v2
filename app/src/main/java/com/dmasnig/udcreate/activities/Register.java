package com.dmasnig.udcreate.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.databases.ORMDatabaseManager;
import com.dmasnig.udcreate.databases.QuotesDatabase;
import com.dmasnig.udcreate.utilities.Config;
import com.dmasnig.udcreate.utilities.Lib;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Created by ud on 10/2/14.
 */
public class Register extends ActionBarActivity{

    private EditText Tfullname, Temail , Tpassword , Tphone ;
    private Spinner callcode , usercountry ;
    private String fullname , email , phone , password , country ;
    private Context ctx = Register.this ;
    private GoogleCloudMessaging gcm;

    private String GCM_REG_ID;
    static final String TAG = "GCM-usage";
    private ProgressDialog progress;

    public static final String PROPERTY_REG_ID = Config.PROPERTY_REG_ID;
    private static final String PROPERTY_APP_VERSION = Config.PROPERTY_APP_VERSION;

    private RequestQueue mRequestQueue;
    private Cache cache ;
    private Network network;
    private JsonArrayRequest req;
    private JsonObjectRequest jsonObjReq ;

    /**
     * Substitute you own sender ID here. This is the project number you got
     * from the API Console
     */
    String SENDER_ID = Config.GCM_SENDER_ID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Create account");

        cache = new DiskBasedCache(getExternalCacheDir(), 1024 * 1024); // 1MB cap
        network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);

        progress = new ProgressDialog(ctx);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /*  From here contains the main Functionalities */

    public void signupBtnPressed(View v){
        if(!EmptyFields()){

            progress.setMessage("performing necessary tasks..");
            progress.show();

            // Register for GCM
            if( checkPlayServices(this) ){
                gcm = GoogleCloudMessaging.getInstance(this);
                GCM_REG_ID = Lib.getFromPrefs(PROPERTY_REG_ID, ctx);

                if (GCM_REG_ID.length() < 10 ) {
                   // Try to register in the background
                    new registerDeviceinBackground().execute() ;
                }else{
                    doSignUP();
                }

            }else {
                progress.dismiss();
                Toast.makeText(ctx, "No valid Google Play Services APK found.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * check for empty Field here
     * @return boolean
     */
    private boolean EmptyFields(){

        String errMsg = null ;
        boolean isEmpty = false ;

        Tfullname = (EditText) findViewById(R.id.fullname);
        Temail = (EditText) findViewById(R.id.email);
        callcode = (Spinner) findViewById(R.id.code) ;
        Tphone = (EditText) findViewById(R.id.phone);
        Tpassword = (EditText) findViewById(R.id.password);
        usercountry = (Spinner) findViewById(R.id.country) ;

        fullname =  Tfullname.getText().toString() ;
        email = Temail.getText().toString() ;
        phone = callcode.getSelectedItem().toString() + Tphone.getText().toString() ;
        password = Tpassword.getText().toString() ;
        country = usercountry.getSelectedItem().toString() ;

        if( fullname.length() < 4 ){
            errMsg = "Please enter a valid name" ;
            isEmpty = true ;
        }else if( email.length() < 4 ){
            errMsg = "Please enter your email";
            isEmpty = true ;
        }else if( phone.length() < 6 ){
            errMsg = "Please enter your phone number";
            isEmpty = true ;
        }else if( password.length() < 5 ){
            errMsg = "choose a password not less than 6 characters";
            isEmpty = true ;
        }else if( country.equalsIgnoreCase("choose country") ){
            errMsg = "Please choose a country";
            isEmpty = true ;
        }

        if(isEmpty){
            Toast.makeText(ctx, errMsg, Toast.LENGTH_LONG).show();
        }
        return isEmpty ;
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    public class registerDeviceinBackground extends AsyncTask<String , Integer , String> {
        boolean hasRegistered = false ;

        @Override
        protected void onPreExecute(){
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(ctx);
                }

                GCM_REG_ID = gcm.register(SENDER_ID);
                if(GCM_REG_ID.length() > 20){
                    hasRegistered = true ;
                    // Store in sharedPreferences
                    String appVersion = String.valueOf(Lib.getAppVersion(ctx));
                    Lib.storeinPrefs(PROPERTY_REG_ID, GCM_REG_ID, ctx);
                    Lib.storeinPrefs(PROPERTY_APP_VERSION, appVersion, ctx);


                }

            } catch (IOException ex) {
                String msg = "Error :" + ex.getMessage();
            }
            return GCM_REG_ID;
        }

        @Override
        protected void onPostExecute(String s) {
            if(hasRegistered){
                doSignUP();
            }else{
                progress.dismiss();
                Lib.Alert("Device registration failed !", ctx);
            }

        }
    }


    /**
     * perform sign up with volley
     */
    private void doSignUP(){
        String deviceID = Lib.getRegistrationId(ctx);
        String targetUrl = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_REGISTER)
                .queryParam("name", fullname)
                .queryParam("email", email)
                .queryParam("phone", phone)
                .queryParam("password", password)
                .queryParam("country", country)
                .queryParam("device_id", deviceID)  // New Device ID
                .build()
                .toUri().toString();

        Log.i(TAG, "Preparing request");
        jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                targetUrl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                Log.i(TAG,"Parsing response");
                parseJsonResponse(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Lib.Toast(error.getMessage(), ctx);
            }
        });

        /* Check if device has internet connectivity */
        if(Lib.deviceIsConnected(ctx)) {
            mRequestQueue.add(jsonObjReq);
            mRequestQueue.start();
        }else{
            Lib.Alert(Lib.connErr, ctx);
        }
    }


    /**
     * Parse the json response from server
     * @param response json
     */
    private void parseJsonResponse(JSONObject response){
        try {
            String error = response.getString("error");
            String apikey = response.getString("message");

            // Sign up success
            if (error.equalsIgnoreCase("false")) {
                // Store API key in preferences
                Lib.storeinPrefs(Config.PROPERTY_API_KEY, apikey, ctx) ;
                Lib.storeinPrefs(Config.PROPERTY_EMAIL, email, ctx) ;
                Lib.storeinPrefs(Config.PROPERTY_FULLNAME, fullname, ctx) ;

                Lib.Toast("Account creation successful", ctx);

                /* Add new entry in Database */
                QuotesDatabase quote = new QuotesDatabase() ;
                quote.setMessage("For I know the plans I have for you,\" declares the LORD, \"" +
                        "plans to prosper you and not to harm you, plans to give you hope and a future.");
                quote.setDate("Just now");
                quote.setHasBeenRead(true);

                ORMDatabaseManager.getInstance().addQuote(quote);
                Lib.setLoginStatus(true,ctx);

                // redirect to Base activity
                Intent i = new Intent(ctx, BaseActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }else{
                Lib.Alert( response.getString("message") , ctx);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     * @param activity
     */
    private boolean checkPlayServices(Register activity) {
         int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(ctx);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity ,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                //finish();
            }
            return false;
        }
        return true;
    }


}