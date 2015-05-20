package com.dmasnig.udcreate.utilities;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.activities.Register;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * custom Library Holds generally used methods
 * that are useful throughout the application
 */
public class Lib {

   public final static String connErr = "Please make sure you are connected to the internet" ;
   public final static String subs_success = "You have been successfully subscribed to the divine mercy daily sms service" ;


    /**
     * @return Application's {@code SharedPreferences}.
     */
    public static SharedPreferences getDevicesPreferences(Context context) {
        SharedPreferences sharedpreferences = context.getSharedPreferences(Config.PREFERENCES, context.MODE_PRIVATE);
        return sharedpreferences;
    }


    /**
     * Set the login status at any point in the application
     * @param status boolean
     * @param context of the application
     */
    public static void setLoginStatus(boolean status,Context context){
        SharedPreferences prefs = Lib.getDevicesPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Config.PROPERTY_LOGIN_STATUS,status);
        editor.commit();
    }


    public static boolean getLoginStatus(Context context){
        final SharedPreferences prefs = Lib.getDevicesPreferences(context);
        return prefs.getBoolean(Config.PROPERTY_LOGIN_STATUS, false);
    }


    /**
     * Store item in Device Preferences
     * @param key the key to store with
     * @param value the value to store
     */
    public static void storeinPrefs(String key, String value, Context context){
        final SharedPreferences prefs = Lib.getDevicesPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * Fetch item From preferences
     * @param key of the item to fetch
     * @return value stored in preferences
     */
    public static String getFromPrefs(String key, Context context){
        final SharedPreferences prefs = Lib.getDevicesPreferences(context);
        return prefs.getString(key, "0");
    }


    /**
     * Check if the device has internet connectivity
     * @return boolean true/false
     */
    public static boolean deviceIsConnected(Context context){
        boolean result ;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if( activeNetworkInfo == null ){
            result = false ;
        }else{
            if(activeNetworkInfo.isConnected()){
                result = true ;
            }else{
                result = true ;
            }
        }
        return result ;
    }



    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    public static String getRegistrationId(Context context){

         final String PROPERTY_REG_ID = Config.PROPERTY_REG_ID;
         final String PROPERTY_APP_VERSION = Config.PROPERTY_APP_VERSION;
         String TAG = "GCM-usage";

        String registrationId = Lib.getFromPrefs(PROPERTY_REG_ID, context);
        if (registrationId.isEmpty()) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = Integer.parseInt(Lib.getFromPrefs(PROPERTY_APP_VERSION, context));
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    public static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static Date setExpiryDate(int h){
        Calendar cal = Calendar.getInstance(); // creates calendar
        cal.setTime(new Date()); // sets calendar time/date
        cal.add(Calendar.HOUR_OF_DAY, h); // adds one hour
        return cal.getTime(); // returns new date object, one hour in the future
    }


    /**
     * custom Toast method
     * @param S string to be displayed
     */
    public static void Toast(String S , Context context){
        Toast.makeText(context, S, Toast.LENGTH_LONG).show();
    }


    /**
     * Custom dialog alert
     * @param message to be displayed
     */
    public static void Alert(String message, Context context){
        final Dialog myDialog ;
        myDialog = new Dialog(context);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        myDialog.setContentView(R.layout.widget_custom_holo_alert);
        myDialog.setCancelable(false);

        TextView dialogContent = (TextView) myDialog.findViewById(R.id.alert_content);
        dialogContent.setText(message);

        Button close_btn = (Button) myDialog.findViewById(R.id.close_button);
        close_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.show();
    }


  /*  public void setLoggedInStatus(boolean status){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Config.PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putBoolean(Config.prefs_LogInStatus, status);
        editor.commit();
    }

    public boolean isLoggedin(){
        SharedPreferences sharedpreferences = context.getSharedPreferences(Config.PREFERENCES, Context.MODE_PRIVATE);
        boolean isLoggedIn = sharedpreferences.getBoolean(Config.prefs_LogInStatus, false);
        return isLoggedIn;
    }
*/



}
