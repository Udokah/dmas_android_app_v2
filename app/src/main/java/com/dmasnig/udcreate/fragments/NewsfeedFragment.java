package com.dmasnig.udcreate.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ud on 10/5/14.
 */
public class NewsfeedFragment extends Fragment {

    private Context context ;
    private String TAG = "Volley Stuff" ;
    private String TICKER ;
    TextView scrolltext ;
    String Jsonurl ;

    RequestQueue mRequestQueue;
    Cache cache;
    Network network ;
    JsonObjectRequest jsonObjReq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

        // Creating view corresponding to the fragment
        View v = inflater.inflate(R.layout.activity_fragment_newsfeed, container, false);
        scrolltext = (TextView) v.findViewById(R.id.scrolltext) ;
        scrolltext.setSelected(true);

        WebView webView = (WebView) v.findViewById(R.id.webview1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return true;
            }
        });

        if(!Lib.deviceIsConnected(context)){
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        webView.loadUrl("http://dmas-nig.com/news/?cat=9");

        fetchScrollText();
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        context = activity.getApplicationContext();
        super.onAttach(activity);
    }

    /* Update scrolling ticker */
    private void updateTicker(){
        scrolltext.setText(TICKER);
        scrolltext.setSelected(true);
    }

    /** Parse Json String
     * @param response jsonobject
     */
    private void parseJSON(JSONObject response){
        // Parsing json object response
        // response will be a json object
        try {
            JSONArray tickers = response.getJSONArray("tickers") ;
            for (int i = 0 ; i < tickers.length() ; i++){
                TICKER += " | " + tickers.get(i).toString() ;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // Get text from WE and put in textview
    private void fetchScrollText(){
        Jsonurl = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_TICKER)
                .build()
                .toUri().toString();

        cache = new DiskBasedCache(context.getExternalCacheDir(), 1024 * 1024); // 1MB cap
        cache.initialize();
        network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);

        Log.i(TAG, "Preparing request");
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                Jsonurl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                Log.i(TAG,"Parsing response");
                parseJSON(response);
                updateTicker() ;
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
                String apikey = Lib.getFromPrefs(Config.PROPERTY_API_KEY,context);
                headers.put("AUTHORIZATION", apikey);
                return headers;
            }
        };

        if(mRequestQueue.getCache().get(Jsonurl)!=null){
            fetchFromCache();
        }

        if( Lib.deviceIsConnected(context) ){
            /* If device is online fetch new from server */
            fetchFromServer();
        }

    }

    private void fetchFromServer(){
        mRequestQueue.add(jsonObjReq);
        mRequestQueue.start();
    }

    private void fetchFromCache(){
        Log.i(TAG,"getting from cache");
        String cachedData = new String(mRequestQueue.getCache().get(Jsonurl).data);
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(cachedData);
            parseJSON(jsonObj);
            updateTicker() ;
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}