package com.dmasnig.udcreate.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

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
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

public class Login extends ActionBarActivity {

    private EditText formEmail ;
    private EditText formPassword  ;
    private String email,password ;
    private Context ctx;

    private String GCM_REG_ID;
    static final String TAG = "GCM-usage";
    private GoogleCloudMessaging gcm;
    private ProgressDialog progress;
    private String targetUrl ;

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
    private String SENDER_ID = Config.GCM_SENDER_ID ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Sign in");
        ctx = Login.this ;
        progress = new ProgressDialog(ctx);

        cache = new DiskBasedCache(getExternalCacheDir(), 1024 * 1024); // 1MB cap
        network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loginBtnPressed(View v){
        boolean isEmpty = false ;
        String errMsg = null ;

        formEmail = (EditText) findViewById(R.id.email) ;
        formPassword = (EditText) findViewById(R.id.password) ;

        email = formEmail.getText().toString() ;
        password = formPassword.getText().toString() ;

        if( email.length() < 3 ){
            errMsg = "Enter a valid email address" ;
            isEmpty = true ;
        }else if( password.length() < 3 ){
            errMsg = "enter your password" ;
            isEmpty = true ;
        }

        if(isEmpty){
            Lib.Toast(errMsg,ctx);
        }else{

            progress.setMessage("Working...");
            progress.show();
            if(Lib.deviceIsConnected(ctx)){  /*if device has internet connectivity */
                // Try to login
                authenticateLogin();
            }else{
                progress.dismiss();
                Lib.Alert(Lib.connErr, ctx);
            }

        }

    }


    /**
     * Authenticate Login with volley
     */
    private void authenticateLogin(){
        progress.setMessage("checking...");
        progress.show();

        targetUrl = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_LOGIN)
                .queryParam("email", email)
                .queryParam("password", password)
                .queryParam("device_id", GCM_REG_ID) // Send GCM id to server
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
        }){
            /* Set Custom Post parameters */
            /*@Override
            public Map<String, String> getParams(){
                Map<String, String> post = new HashMap<String, String>();
                post.put("email", email) ;
                post.put("password", password);
                post.put("device_id", GCM_REG_ID); // Send GCM id to server
                return post;
            }*/
        };
        mRequestQueue.add(jsonObjReq);
        mRequestQueue.start();
    }


    /**
     * Parse JsonObject response
     * @param response to be parsed
     */
    private void parseJsonResponse(JSONObject response){
        progress.dismiss();
        try {
            String error = response.getString("error") ;
            String apikey = response.getString("apikey") ;
            String name = response.getString("name") ;

            /// If Login is good, save api key
            if(error.equalsIgnoreCase("false")) {
                Lib.storeinPrefs(Config.PROPERTY_API_KEY, apikey, ctx) ;
                Lib.storeinPrefs(Config.PROPERTY_EMAIL, email, ctx) ;
                Lib.storeinPrefs(Config.PROPERTY_FULLNAME, name, ctx) ;

                /* Add new entry in Database */
                QuotesDatabase quote = new QuotesDatabase() ;
                quote.setMessage("For I know the plans I have for you,\" declares the LORD, \"" +
                        "plans to prosper you and not to harm you, plans to give you hope and a future.");
                quote.setDate("Just now");
                quote.setHasBeenRead(true);

                ORMDatabaseManager.getInstance().addQuote(quote);
                Lib.setLoginStatus(true,ctx);

                Lib.Toast("Login successful",ctx);
                // redirect to Base activity
                Intent i = new Intent(ctx,BaseActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
                startActivity(i);
                finish();
            }else{
                Lib.Alert("Wrong username or password",ctx);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* Register Device for GCM */
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    /*private class registerDeviceinBackground extends AsyncTask<String , Integer , String> {
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
                Log.i(TAG,msg) ;
            }
            return GCM_REG_ID;
        }

        @Override
        protected void onPostExecute(String s) {
            if(hasRegistered){
                authenticateLogin() ;
            }else{
                progress.dismiss();
                Lib.Alert("Device registration failed !", ctx);
            }

        }
    }*/

    /**
     * check if Google Play service is installed on device
     * @param activity Login activity
     * @return boolean
     */
    /*private boolean checkPlayServices(Login activity){
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
    }*/

}

