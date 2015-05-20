package com.dmasnig.udcreate.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.utilities.Config;
import com.dmasnig.udcreate.utilities.Lib;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ud on 10/10/14.
 */
public class TestimoniesFragment extends Fragment {

    View mainveiw  ;
    Context context ;
    TextView textbox ;
    String message ;
    ProgressDialog progress ;
    ListView listview ;
    List<testimoniesData> testimoniesDatas;
    String apikey ;
    private String TAG = "Volley Stuff" ;
    String urlJsonArry ;
    private listAdapter adapter ;

    private RequestQueue mRequestQueue;
    private Cache cache ;
    private Network network;
    private JsonArrayRequest req;
    private boolean adapterhasbeenset = false ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        // Inflate the layout for this fragment
        mainveiw = inflater.inflate(R.layout.activity_fragment_testimonials, container, false);
        listview = (ListView) mainveiw.findViewById(R.id.testimonies_list) ;

        textbox = (TextView) mainveiw.findViewById(R.id.textbox) ;

        /* Set volley Parameters */
        urlJsonArry = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_FETCH_TESTIMONY)
                .queryParam("counter",0)
                .build()
                .toUri().toString();

        cache = new DiskBasedCache(context.getExternalCacheDir(), 1024 * 1024); // 1MB cap
        cache.initialize();
        network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);

        /* end of volley parameters*/

        // Set onclick listener for button
        ImageButton sendBtn = (ImageButton) mainveiw.findViewById(R.id.send_btn);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                message = textbox.getText().toString() ;
                if(message.length() < 10) {
                    Lib.Toast("testimony must be at least 10 characters",context);
                }else{
                        /* Post Data to Server */
                    postTestimony();

                }

            }
        });

        apikey = Lib.getFromPrefs(Config.PROPERTY_API_KEY,context);
        progress = new ProgressDialog(getActivity());

        loadTestimonies();
        return mainveiw ;
    }

    @Override
    public void onAttach(Activity activity) {
        context = activity.getApplicationContext();
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private class listAdapter extends BaseAdapter {
        List<testimoniesData> messagesList = testimoniesDatas;

        @Override
        public int getCount() {
            return messagesList.size() ;
        }

        @Override
        public testimoniesData getItem(int position) {
            return messagesList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            testimoniesData testimoniesData = messagesList.get(position);


            if (convertView == null) {  // if it's not recycled, initialize some attributes

                convertView = inflater.inflate(R.layout.widget_testimonies_list_item, null);

                LinearLayout view = (LinearLayout) convertView.findViewById(R.id.testimonies_wrapper);

                TextView username = (TextView) convertView.findViewById(R.id.fullname);
                TextView message = (TextView) convertView.findViewById(R.id.message);
                TextView sendDate = (TextView) convertView.findViewById(R.id.send_date);

                // Apply fonts
                Typeface myTypeface = Typeface.createFromAsset( context.getAssets(), "fonts/droidsans.ttf");
                message.setTypeface(myTypeface);

                username.setText(testimoniesData.name);
                message.setText(testimoniesData.message);
                sendDate.setText(testimoniesData.time);

                //holder.wrapper.removeAllViews();
                return view ;

            } else {
                LinearLayout view = (LinearLayout) convertView;
                TextView username = (TextView) convertView.findViewById(R.id.fullname);
                TextView message = (TextView) convertView.findViewById(R.id.message);
                TextView sendDate = (TextView) convertView.findViewById(R.id.send_date);

                // Apply fonts
                Typeface myTypeface = Typeface.createFromAsset( context.getAssets(), "fonts/droidsans.ttf");
                message.setTypeface(myTypeface);

                username.setText(testimoniesData.name);
                message.setText(testimoniesData.message);
                sendDate.setText(testimoniesData.time);

                return convertView ;
            }

        }

    }

    private class testimoniesData {
        public String name;
        public String message ;
        public String time;
    }

    private void loadTestimonies(){
        /* First fetch from cache */
        if(mRequestQueue.getCache().get(urlJsonArry) != null){
            fetchFromCache();
        }

            /* check If its online */
        if( Lib.deviceIsConnected(context)){
            fetchFromServer();
        }else{
            if(mRequestQueue.getCache().get(urlJsonArry) == null){
                Lib.Toast("Unable to connect to the internet",context);
            }
        }
    }

    private void fetchFromCache(){
        Log.i(TAG,"getting from cache");
        String cachedData = new String(mRequestQueue.getCache().get(urlJsonArry).data);
        Gson gson = new Gson();
        List<testimoniesData> fileList;
        fileList = Arrays.asList(gson.fromJson(cachedData, testimoniesData[].class));
        testimoniesDatas = fileList ;
        setupAdapter();
    }

    private void fetchFromServer(){
        req = new JsonArrayRequest(urlJsonArry,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        parseJsonResponse(response) ;
                        setupAdapter();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progress.dismiss();
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        Lib.Toast(error.getMessage(),context);
                    }
                })
        {
            /* Set Custom Headers */
            @Override
            public HashMap<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("AUTHORIZATION", apikey);
                return headers;
            }
        };
        mRequestQueue.add(req);

        /* if first time then show dialog */
        if(mRequestQueue.getCache().get(urlJsonArry) == null) {
            progress.setMessage("Fetching data...");
            progress.show();
        }

        mRequestQueue.start();
    }

    private void setupAdapter(){
        progress.dismiss();
        adapter = new listAdapter() ;
        if(adapterhasbeenset){
            adapter.notifyDataSetChanged();
        }else{
            listview.setAdapter(adapter);
        }
    }


    private void parseJsonResponse(JSONArray response){
        progress.dismiss();
        String TAG = "Vollery Request" ;
        List<testimoniesData> fileList = new ArrayList<testimoniesData>();

        try {
            // Parsing json array response
            // loop through each json object
            for (int i = 0; i < response.length(); i++) {

                JSONObject person = (JSONObject) response.get(i);

                String name = person.getString("name");
                String message = person.getString("message");
                String time = person.getString("time");
                testimoniesData msgobj = new testimoniesData();
                msgobj.name = name ;
                msgobj.message = message ;
                msgobj.time = time ;
                // adding msg object to ArrayList
                fileList.add(msgobj);
            }

            testimoniesDatas = fileList ;

        } catch (JSONException e) {
            progress.dismiss();
            e.printStackTrace();
            Lib.Toast(e.getMessage(),context);
        }

    }


    private void postTestimony(){
        progress.setMessage("sending...");
        progress.show();
        String turl = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_POST_TESTIMONY)
                .queryParam("message",message)
                .build()
                .toUri().toString();

        RequestQueue mRequestQueue2;
        Cache cache2 = new DiskBasedCache(context.getExternalCacheDir(), 1024 * 1024); // 1MB cap
        cache2.initialize();
        Network network2 = new BasicNetwork(new HurlStack());
        mRequestQueue2 = new RequestQueue(cache2, network2);


            /* Fetch Testimonies with Volley */
        JsonObjectRequest jsonObjReq2 = new JsonObjectRequest(Request.Method.POST,
                turl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                Log.i(TAG,"Parsing response");
                try {
                    String error  = response.getString("error") ;
                    String message  = response.getString("message") ;
                    if( error.equalsIgnoreCase("true") ){
                        progress.dismiss();
                        Lib.Toast(message,context);
                    }else{
                        // Reload adapter
                        progress.dismiss();
                        textbox.setText("");
                        fetchFromServer();
                    }
                } catch (JSONException e) {
                    progress.dismiss();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
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
        mRequestQueue2.add(jsonObjReq2);
        mRequestQueue2.start();
    }

}
