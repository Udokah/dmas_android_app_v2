package com.dmasnig.udcreate.utilities;

import android.app.Activity;

/**
 * Created by UDSWAGZ on 5/30/2014.
 */
public class Config extends Activity{
    //public static final String server = "http://192.168.43.100/Dmas-Nig" ;
    public static final String server = "http://dmas-nig.com" ;
    public static final String WEBSERVICE = server + "/engine/webservice/v1" ;
    public static final String payment_url = "/subscribe-online";

    public static final String URL_REGISTER = "/register" ;
    public static final String URL_LOGIN =  "/login" ;
    public static final String URL_SUB_INFO = "/subscription" ;
    public static final String URL_SUBSCRIBE = "/subscribe" ;
    public static final String URL_DAILY_MESSAGE = "/daily";
    public static final String URL_POST_TESTIMONY = "/testimony/post" ;
    public static final String URL_FETCH_TESTIMONY = "/testimony" ;
    public static final String URL_GALLERY = "/gallery" ;
    public static final String URL_RESOURCES = "/resources" ;
    public static final String URL_DOWNLOADS = "/downloads" ;
    public static final String URL_TICKER = "/ticker" ;

    public static final String DOWNLOADS_PATH = server + "/_app/uploads" ;
    public static final String GALLERY_PATH = server + "/_app/gallery" ;
    public static final String RESOURCES_PATH = server + "/_app/resources" ;

    public static final int NotificationID = 199208 ;

    /* Preferences Values */
    public static final String PROPERTY_LOGIN_STATUS = "login_status" ;
    public static final String PROPERTY_FETCHED_TODAY = "has_fetched_today";
    public static final String PREFERENCES = "DEVICE_PREFERENCES" ;
    public static final String PROPERTY_API_KEY = "ApiKey" ;
    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_EMAIL = "accountEmail" ;
    public static final String PROPERTY_FULLNAME = "accountFullname";

    /* Google Cloud Messenger Stuff */
    public static final String GCM_SENDER_ID = "172649105065";
}
