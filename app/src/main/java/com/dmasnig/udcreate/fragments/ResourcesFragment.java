package com.dmasnig.udcreate.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ud on 10/10/14.
 */
public class ResourcesFragment extends Fragment {

    private Context context ;
    private View mainveiw  ;
    private String TAG = "Volley Stuff" ;
    private List<resourcesData> dataList;
    private final String TITLE = "title" ;
    private final String PRICE = "price" ;
    private final String LINK = "link" ;
    private final String PICTURE = "picture" ;
    private final String RESOURCES = "resources" ;
    private ProgressDialog progressDialog ;
    private GridView gridView ;

    RequestQueue mRequestQueue;
    Cache cache ;
    Network network;
    JsonObjectRequest jsonObjReq ;
    String Jsonurl;
    private boolean adapterhasBeenset = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
        // Inflate the layout for this fragment
        mainveiw = inflater.inflate(R.layout.activity_fragment_resources, container, false);
        gridView = (GridView) mainveiw.findViewById(R.id.resources_grid) ;
        progressDialog = new ProgressDialog(getActivity());

        fetchResources(); // From server with Volley

        return mainveiw ;
    }

    @Override
    public void onAttach(Activity activity) {
        context = activity.getApplicationContext();
        super.onAttach(activity);
    }


    private void setupAdapter(){
        progressDialog.dismiss();
        CustomGridAdapter adapter = new CustomGridAdapter() ;

        if(adapterhasBeenset){
            adapter.notifyDataSetChanged();
        }else{
            adapterhasBeenset = true ;
            gridView.setAdapter(adapter);
               /* Set On click Listener */
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    CustomGridAdapter customGridAdapter = new CustomGridAdapter();
                    final resourcesData selectedItem = customGridAdapter.getResourcesDataList(i);
                    final String url = selectedItem.link;
                /* Open url in browser */
                    Intent bi = new Intent(Intent.ACTION_VIEW);
                    bi.setData(Uri.parse(url));
                    startActivity(bi);
                }
            });
        }
    }


    /* Custom Gridveiw Adapter */
    private class CustomGridAdapter extends BaseAdapter {

        List<resourcesData> resourcesDataList = dataList ;


        public View getView(int position, View convertView, ViewGroup parent) {
            resourcesData useData = resourcesDataList.get(position) ;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.widget_resources_grid_item, null);
                LinearLayout view = (LinearLayout) convertView.findViewById(R.id.grid_wrapper);

                String imgurl = Config.RESOURCES_PATH + '/' + useData.picture ;

                ImageView itemImage = (ImageView) convertView.findViewById(R.id.item_image);
                TextView itemTitle = (TextView) convertView.findViewById(R.id.item_title);
                TextView itemPrice = (TextView) convertView.findViewById(R.id.item_price);

                Picasso.with(context).load(imgurl).into(itemImage);
                itemTitle.setText(useData.title);
                itemPrice.setText( "Price: N" + useData.price );
                return view ;
            }else{
                LinearLayout view = (LinearLayout) convertView.findViewById(R.id.grid_wrapper);
                String imgurl = Config.RESOURCES_PATH + '/' + useData.picture ;

                ImageView itemImage = (ImageView) convertView.findViewById(R.id.item_image);
                TextView itemTitle = (TextView) convertView.findViewById(R.id.item_title);
                TextView itemPrice = (TextView) convertView.findViewById(R.id.item_price);

                Picasso.with(context).load(imgurl).into(itemImage);
                itemTitle.setText(useData.title);
                itemPrice.setText( "Price: N" + useData.price );
                return convertView ;
            }
        }

        @Override
        public int getCount() {
            return resourcesDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return resourcesDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public resourcesData getResourcesDataList(int position){
            return resourcesDataList.get(position);
        }
    }

    private void parseJSON(JSONObject response){
        try {
            // Parsing json object response
            // response will be a json object
            JSONArray jsonArr = response.getJSONArray(RESOURCES) ;
            for (int i = 0 ; i < jsonArr.length() ; i++){
                JSONObject jsonObject = (JSONObject) jsonArr.get(i);
                resourcesData resObj = new resourcesData();
                resObj.title = jsonObject.getString(TITLE);
                resObj.price = jsonObject.getString(PRICE);
                resObj.link = jsonObject.getString(LINK);
                resObj.picture = jsonObject.getString(PICTURE);
                dataList.add(resObj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /* Fetch Data with Volley */
    private void fetchResources(){
        Jsonurl = UriComponentsBuilder.fromUriString(Config.WEBSERVICE)
                .path(Config.URL_RESOURCES)
                .build()
                .toUri().toString();

        dataList = new ArrayList<resourcesData>();

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
                setupAdapter() ;
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

        if(mRequestQueue.getCache().get(Jsonurl)!=null) {
            fetchFromCache();
        }

        if( Lib.deviceIsConnected(context)){
            fetchFromServer();
        }else{
            if(mRequestQueue.getCache().get(Jsonurl)==null) {
                Lib.Toast(Config.CONNECTIVITY_SERVICE,context);
            }
        }
    }

    private void fetchFromServer(){
        if(mRequestQueue.getCache().get(Jsonurl)==null) {
            progressDialog.setMessage("working...");
            progressDialog.show();
        }
        mRequestQueue.add(jsonObjReq);
        mRequestQueue.start();
        setupAdapter() ;
    }

    private void fetchFromCache(){
        Log.i(TAG, "getting from cache");
        String cachedData = new String(mRequestQueue.getCache().get(Jsonurl).data);
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(cachedData);
            parseJSON(jsonObj);
            setupAdapter() ;
        } catch (JSONException e) {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }

    /* Resources Object */
    private class resourcesData{
        String title ;
        String price ;
        String link ;
        String picture ;
    }

}
