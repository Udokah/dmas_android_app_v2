package com.dmasnig.udcreate.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.activities.AboutDivineMercy;
import com.dmasnig.udcreate.activities.NovenaPrayers;
import com.dmasnig.udcreate.activities.ThreeOclockPrayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ud on 10/11/14.
 */
public class PrayersFragment extends Fragment{

    private View mainveiw  ;
    private Context context ;
    private String[] mPrayers  ;

    private int[] mIcons = new int[]{
            R.drawable.ic_image_faustina,
            R.drawable.ic_image_prayer,
            R.drawable.ic_image_dm
    };

    private LinearLayout layout;
    private ListView listView ;
    private SimpleAdapter simpleAdapter ;
    private List<HashMap<String, String>> prayerList;
    final private String PRAYER = "menuItem";
    final private String ICONS = "menuIcon";
    private Intent intent ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){

        mPrayers = getResources().getStringArray(R.array.list_of_prayers) ;
        // Inflate the layout for this fragment
        mainveiw = inflater.inflate(R.layout.activity_fragment_prayers, container, false);
        listView = (ListView) mainveiw.findViewById(R.id.prayers_list) ;
        layout = (LinearLayout) mainveiw.findViewById(R.id.listview_layout) ;

        prayerList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < 3; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put(PRAYER, mPrayers[i]);
            hm.put(ICONS, Integer.toString(mIcons[i]));
            prayerList.add(hm);
        }

        // Keys used in Hashmap
        String[] from = {PRAYER, ICONS};

        // Ids of views in listview_layout
        int[] to = {R.id.menuItem, R.id.menuIcon};

        simpleAdapter = new SimpleAdapter(context, prayerList, R.layout.widget_listview_item, from, to);

        listView.setAdapter(simpleAdapter);
        // ItemClick event handler for the drawer mItems
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

                switch (position){
                    case 0: intent = new Intent(context, NovenaPrayers.class);
                        break;
                    case 1: intent = new Intent(context, ThreeOclockPrayer.class);
                        break;
                    case 2: intent = new Intent(context, AboutDivineMercy.class);
                        break;
                    default: intent = new Intent(context, AboutDivineMercy.class);
                        break;
                }

                startActivity(intent);
            }
        });

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





}
