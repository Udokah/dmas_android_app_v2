package com.dmasnig.udcreate.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Gallery fragment
 *
 */
public class GalleryFragment extends Fragment {
    View mainveiw  ;
    Context context ;
    GridView gridView;
    ProgressDialog progressDialog ;
    String TAG = "Gallery work" ;
    List<imageData> picsFromServer ;

    RequestQueue mRequestQueue;
    Cache cache ;
    Network network ;
    String Jsonurl ;
    JsonObjectRequest jsonObjReq;
    private boolean adapterhasbeenset = false ;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        // Inflate the layout for this fragment
        mainveiw = inflater.inflate(R.layout.activity_fragment_gallery, container, false);
        gridView = (GridView) mainveiw.findViewById(R.id.gridview) ;
        progressDialog = new ProgressDialog(getActivity());

        fetchImages(); // Fetch images from server first

        /* Enable HTTP response Caching */
        /*try {
            File httpCacheDir = new File(context.getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024; // 10 MiB
            Class.forName("android.net.http.HttpResponseCache")
                    .getMethod("install", File.class, long.class)
                    .invoke(null, httpCacheDir, httpCacheSize);
        }
            catch (Exception httpResponseCacheNotAvailable) {
            }
*/
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


    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        ImageView imageView ;
        List<imageData> fileLists = picsFromServer ;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return fileLists.size();
        }

        public imageData getItem(int position) {
            return fileLists.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            View gridView ;
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

           ViewHolder holder;

            imageData filesDATA = fileLists.get(position);

            if (convertView == null) {  // if it's not recycled, initialize some attributes
                gridView = new View(context) ;

                gridView = inflater.inflate(R.layout.widget_gallery_item, null);

                holder = new ViewHolder();
                holder.wrapper = (LinearLayout) gridView.findViewById(R.id.panel_content);
                holder.picture = (ImageView) gridView.findViewById(R.id.grid_item_image) ;
                imageView = (ImageView) gridView.findViewById(R.id.grid_item_image);

                String imgurl = Config.GALLERY_PATH + '/' + filesDATA.files ;
                Log.i(TAG,"Fetch image with picasso url= " + imgurl);

                Picasso.with(context).load(imgurl).into(imageView);
                Drawable imageTag = imageView.getDrawable();
                imageView.setTag(imageTag);


            } else {
                holder = (ViewHolder) convertView.getTag();
                gridView = (View) convertView;
            }


            return gridView;
        }

        public imageData getFilesList(int position) {
            return fileLists.get(position);
        }
    }


    /* Start and show the Adapter */
    private void setupAdapter(){
        progressDialog.dismiss();
        ImageAdapter imageAdapter = new ImageAdapter(context);

        if(adapterhasbeenset){
            imageAdapter.notifyDataSetChanged();
        }else {
            adapterhasbeenset = true ;
            gridView.setAdapter(imageAdapter);
        /* Set On click Listener */
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ImageAdapter myAdapter = new ImageAdapter(context);
                    final imageData selectedItem = myAdapter.getFilesList(i);
                    final String imageFile = selectedItem.files;
                    showPopup(imageFile);
                }
            });
        }
    }

    /* Show Popup Preview */
    private void showPopup(String filename){
        final Dialog myDialog = new Dialog(getActivity());
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        myDialog.setContentView(R.layout.widget_gallery_dialog);
        myDialog.setCancelable(true);

        String imgurl = Config.GALLERY_PATH + '/' + filename ;
        ImageView image = (ImageView) myDialog.findViewById(R.id.selected_image);
        Picasso.with(context).load(imgurl).into(image);

        ImageButton login = (ImageButton) myDialog.findViewById(R.id.closebtn);
        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.show();
    }


    /* Parse Json String */
    private void parseJSON(JSONObject response){
        // Parsing json object response
        // response will be a json object
        try {
            JSONArray file = response.getJSONArray("files") ;
            for (int i = 0 ; i < file.length() ; i++){
                imageData resObj = new imageData();
                resObj.files = file.get(i).toString() ;
                picsFromServer.add(resObj) ;
                setupAdapter();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /*Fetch images object from webservice*/
    private void fetchImages(){

        picsFromServer = new ArrayList<imageData>();
        Jsonurl = Config.WEBSERVICE + Config.URL_GALLERY ;

        cache = new DiskBasedCache(context.getExternalCacheDir(), 1024 * 1024); // 1MB cap
        cache.initialize();
        network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network);

        Log.i(TAG,"Preparing request");
        jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                Jsonurl, null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, response.toString());
                Log.i(TAG,"Parsing response");
                parseJSON(response);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Lib.Toast(error.getMessage(),context);
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

        /* first if cache is available get from it */
        if(mRequestQueue.getCache().get(Jsonurl) != null){
            fetchFromCache();
        }

        /* Check if device is online */
        if(Lib.deviceIsConnected(context)){
            fetchFromServer();
        }else{
            if(mRequestQueue.getCache().get(Jsonurl) == null){
                Lib.Toast("Unable to connect to the internet",context);
            }
        }

    }

    private void fetchFromServer(){
        if(mRequestQueue.getCache().get(Jsonurl) == null) {
            progressDialog.setMessage("working...");
            progressDialog.show();
        }
            mRequestQueue.add(jsonObjReq);
            mRequestQueue.start();

    }

    private void fetchFromCache(){
        Log.i(TAG,"getting from cache");
        String cachedData = new String(mRequestQueue.getCache().get(Jsonurl).data);
        JSONObject response = null;
        try {
            response = new JSONObject(cachedData);
            parseJSON(response);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class ViewHolder {
        LinearLayout wrapper;
        ImageView picture ;
    }

    private class imageData{
        String files ;
    }

}
