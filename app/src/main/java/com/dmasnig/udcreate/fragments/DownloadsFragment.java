package com.dmasnig.udcreate.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import java.util.List;

/**
 * Created by ud on 10/9/14.
 */
public class DownloadsFragment extends Fragment {

    View mainveiw  ;
    Context context ;
    List<downloadsData> downloadsFromServer ;
    ProgressDialog progressDialog ;
    ListView listview ;
    private Dialog myDialog ;
    private static final String DL_ID = "downloadId";
    private SharedPreferences prefs;
    private DownloadManager dm;
    String TAG = "volley request" ;
    String Jsonurl ;
    private listAdapter adapter ;

    RequestQueue mRequestQueue;
    Cache cache;
    Network network ;
    JsonObjectRequest jsonObjReq;
    private boolean adapterhasbeenset = false ; /*drove me crazy for hours*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        // Inflate the layout for this fragment
        mainveiw = inflater.inflate(R.layout.activity_fragment_downloads, container, false);
        listview = (ListView) mainveiw.findViewById(android.R.id.list) ;
        progressDialog = new ProgressDialog(context);

                    /* Prepare Volley Request */
        downloadsFromServer = new ArrayList<downloadsData>() ;
        Jsonurl = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_DOWNLOADS)
                .build()
                .toUri().toString();

        cache = new DiskBasedCache(context.getExternalCacheDir(), 1024 * 1024); // 1MB cap
        cache.initialize();
        network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);

        getDownloads();

        /* Downloads Broadcast Receiver */
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        dm = (DownloadManager) getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);

        return mainveiw ;
    }


    private void startDownload(String filename,String title){
        String requestUri = UriComponentsBuilder.fromUriString(Config.DOWNLOADS_PATH)
                .path("/" + filename)
                .build()
                .toUri().toString() ;
        Log.i("DOWNLOAD URL", requestUri);

        if(!prefs.contains(DL_ID)) {
            Uri resource = Uri.parse(requestUri);
            DownloadManager.Request request = new DownloadManager.Request(resource);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setAllowedOverRoaming(false);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
            request.setTitle(title);
            long id = dm.enqueue(request);
            prefs.edit().putLong(DL_ID, id).commit();
        } else {
            queryDownloadStatus();
        }
        getActivity().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            queryDownloadStatus();
        }
    };

    private void queryDownloadStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(prefs.getLong(DL_ID, 0));
        Cursor c = dm.query(query);
        if(c.moveToFirst()) {
            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
            Log.d("DM Sample","Status Check: "+status);
            switch(status) {
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_RUNNING:
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    Lib.Toast("file download successful", context);
                    break;
                case DownloadManager.STATUS_FAILED:
                    Lib.Toast("file download failed",context);
                    dm.remove(prefs.getLong(DL_ID, 0));
                    prefs.edit().clear().commit();
                    break;
            }
        }
    }


    @Override
    public void onAttach(Activity activity) {
        context = activity.getApplicationContext();
        progressDialog = new ProgressDialog(context);
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private class listAdapter extends BaseAdapter {

        List<downloadsData> downloadsList = downloadsFromServer ;

        @Override
        public int getCount() {
            return downloadsList.size() ;
        }

        @Override
        public downloadsData getItem(int position) {
            return downloadsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            downloadsData downloadsData = downloadsList.get(position);

            if (convertView == null) {  // if it's not recycled, initialize some attributes
                LayoutInflater inflater = (LayoutInflater) context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.widget_downloads_item, null);
            }

                TextView downloadsTitle = (TextView) convertView.findViewById(R.id.download_title);

                // Apply fonts
                Typeface myTypeface = Typeface.createFromAsset( context.getAssets(), "fonts/droidsans.ttf");
                downloadsTitle.setTypeface(myTypeface);

                downloadsTitle.setText(downloadsData.title);

            return convertView ;
        }

        public downloadsData getDownloadsList(int position) {
            return downloadsList.get(position);
        }

    }


    private class downloadsData {
        public String title;
        public String file;
    }


    public void getDownloads(){


            Log.i(TAG, "Preparing request");
            jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                    Jsonurl, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, response.toString());
                    Log.i(TAG,"Parsing response");
                    parseJson(response);
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

        /* First fetch from cache */
        if(mRequestQueue.getCache().get(Jsonurl) != null){
            fetchFromCache();
        }

            /* check If its online */
            if( Lib.deviceIsConnected(context)){
                fetchFromServer();
            }else{
                if(mRequestQueue.getCache().get(Jsonurl) == null){
                    Lib.Toast("Unable to connect to the internet",context);
                }
            }
    }

    private void fetchFromServer(){
        if(mRequestQueue.getCache().get(Jsonurl) == null) {
            progressDialog.setMessage("Working...");
            progressDialog.show();
        }
        mRequestQueue.add(jsonObjReq);
        Log.i(TAG,"Fetching from server") ;
        mRequestQueue.start();
    }

    private void fetchFromCache(){
        Log.i(TAG,"getting from cache");
        String cachedData = new String(mRequestQueue.getCache().get(Jsonurl).data);
        try {
            JSONObject jsonObj = new JSONObject(cachedData);
            Log.i(TAG, "CACHED-DATA: " + jsonObj.toString()) ;
            parseJson(jsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void parseJson(JSONObject jsonObj){
        progressDialog.dismiss();
        try {
            JSONArray files = jsonObj.getJSONArray("downloads");
            for (int i = 0; i < files.length(); i++) {
                JSONObject fileDownload = (JSONObject) files.get(i);
                String file = fileDownload.getString("file");
                String title = fileDownload.getString("title");

                downloadsData msgobj = new downloadsData();
                msgobj.file =  file ;
                msgobj.title =  title ;

                downloadsFromServer.add(msgobj);
            }
            setupAdapter();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupAdapter(){
        progressDialog.dismiss();
        listAdapter customAdapter = new listAdapter() ;
        if(adapterhasbeenset){
            customAdapter.notifyDataSetChanged();
            listview.invalidate();
            listview.notify();
        }else {
            adapterhasbeenset = true;
            listview.setAdapter(customAdapter);
        }

        /* Set On click Listener */
                listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        listAdapter myAdapter = new listAdapter();
                        final downloadsData selectedItem = myAdapter.getDownloadsList(i);
                        final String title = selectedItem.title;
                        final String file = selectedItem.file;

                        // Confirm Download
                        myDialog = new Dialog(getActivity());
                        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        myDialog.setContentView(R.layout.widget_custom_holo_dialog);
                        myDialog.setCancelable(false);

                        TextView dialogTitle = (TextView) myDialog.findViewById(R.id.dialog_title);
                        dialogTitle.setText("Download File ?");

                        TextView dialogContent = (TextView) myDialog.findViewById(R.id.dialog_content);
                        dialogContent.setText(title);

                        Button no_button = (Button) myDialog.findViewById(R.id.no_button);
                        no_button.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                myDialog.dismiss();
                            }
                        });

                        Button yes_button = (Button) myDialog.findViewById(R.id.yes_button);
                        yes_button.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                // Clear quotes
                                myDialog.dismiss();
                                if (Lib.deviceIsConnected(context)) {
                                    startDownload(file, title);
                                } else {
                                    Lib.Alert("Not network available", context);
                                }

                            }
                        });

                        myDialog.show();
                    }
                });
    }


}
