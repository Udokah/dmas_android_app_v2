package com.dmasnig.udcreate.activities;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.databases.ORMDatabaseManager;
import com.dmasnig.udcreate.databases.QuotesDatabase;
import com.dmasnig.udcreate.fragments.DownloadsFragment;
import com.dmasnig.udcreate.fragments.GalleryFragment;
import com.dmasnig.udcreate.fragments.MessageFragment;
import com.dmasnig.udcreate.fragments.NewsfeedFragment;
import com.dmasnig.udcreate.fragments.PrayersFragment;
import com.dmasnig.udcreate.fragments.ResourcesFragment;
import com.dmasnig.udcreate.fragments.TestimoniesFragment;
import com.dmasnig.udcreate.services.TimeService;
import com.dmasnig.udcreate.utilities.Config;
import com.dmasnig.udcreate.utilities.Lib;
import com.rampo.updatechecker.UpdateChecker;
import com.rampo.updatechecker.notice.Notice;
import com.rampo.updatechecker.store.Store;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ud on 10/2/14.
 */
public class BaseActivity extends ActionBarActivity implements MessageFragment.OnFragmentInteractionListener {
    // Array of strings storing country names
    String[] mItems;
    String mTitle = "";
    private Context ctx ;

    // Array of integers points to images stored in /res/drawable-ldpi/
    int[] mIcons = new int[]{
            R.drawable.ic_menu_newsfeed,
            R.drawable.ic_bible_open,
            R.drawable.ic_menu_gallery,
//            R.drawable.ic_download_start,
            R.drawable.ic_menu_messages,
//            R.drawable.ic_menu_shop,
            R.drawable.ic_menu_prayers
    };

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout mDrawer;
    private List<HashMap<String, String>> mList;
    private SimpleAdapter mAdapter;
    final private String ITEMS = "menuItem";
    final private String ICONS = "menuIcon";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        /* Start Alarm Reciever */
        //WakefulIntentService.scheduleAlarms(new DailyListener(), this, false);

        /**
         * Start The Alarm Service
         */
        startService(new Intent(this, TimeService.class));
        registerReceiver();


        setContentView(R.layout.activity_base);
        getSupportActionBar().setTitle("News Feed");
        ctx = BaseActivity.this ;

        // Getting an array of country names
        mItems = getResources().getStringArray(R.array.menu_items);

        // Getting a reference to the drawer listview
        mDrawerList = (ListView) findViewById(R.id.drawer_list);
        mDrawerList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Getting a reference to the sidebar drawer ( Title + ListView )
        mDrawer = (LinearLayout) findViewById(R.id.drawer);

        // Each row in the list stores country name, count and flag
        mList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < 5; i++) {
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put(ITEMS, mItems[i]);
            hm.put(ICONS, Integer.toString(mIcons[i]));
            mList.add(hm);
        }

        // Keys used in Hashmap
        String[] from = {ITEMS, ICONS};

        // Ids of views in listview_layout
        int[] to = {R.id.menuItem, R.id.menuIcon};

        // Instantiating an adapter to store each mItems
        // R.layout.widget_drawer_layout defines the layout of each ITEMS
        mAdapter = new SimpleAdapter(this, mList, R.layout.widget_drawer_layout, from, to);

        // Getting reference to DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Creating a ToggleButton for NavigationDrawer with drawer event listener
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close) {

            /** Called when drawer is closed */
            public void onDrawerClosed(View view) {
                highlightSelectedItem();
                supportInvalidateOptionsMenu();
            }

            /** Called when a drawer is opened */
            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }
        };
        // Setting event listener for the drawer
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // ItemClick event handler for the drawer mItems
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
                mAdapter.notifyDataSetChanged();
                // Closing the drawer
                mDrawerLayout.closeDrawer(mDrawer);
                //v.setBackgroundColor(getResources().getColor(R.color.drawer_bg_light));
                showFragment(position);
            }
        });

        // Enabling Up navigation
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Setting the adapter to the listView
        mDrawerList.setAdapter(mAdapter);

      /*  // Apply fonts
        Typeface myTypeface = Typeface.createFromAsset( getAssets(), "fonts/droidsans.ttf");
        TextView myTextView = (TextView) findViewById(R.id.menuItem);
        myTextView.setTypeface(myTypeface);*/

        TextView accountName = (TextView) findViewById(R.id.account_name);
        TextView accountEmail = (TextView) findViewById(R.id.account_email);

        String ACCOUNTNAME = Lib.getFromPrefs(Config.PROPERTY_FULLNAME,ctx) ;
        String ACCOUNTEMAIL = Lib.getFromPrefs(Config.PROPERTY_EMAIL,ctx) ;

        accountName.setText(ACCOUNTNAME);
        accountEmail.setText(ACCOUNTEMAIL);

        String not_message, not_date;
        if(savedInstanceState == null){
            Bundle extras = getIntent().getExtras();
            if(extras == null){
                not_message = not_date = null;
            } else {
                not_message = extras.getString("message");
                not_date = extras.getString("date");
            }

        } else {
            not_message = (String) savedInstanceState.getSerializable("message");
            not_date = (String) savedInstanceState.getSerializable("date");
        }

        if(not_message != null && not_date != null){
            saveMessage(not_message,not_date);
        }

    }

    private void saveMessage(String message, String date){
        Log.i(date,message);
        QuotesDatabase quote = new QuotesDatabase();
        quote.setMessage(message);
        quote.setDate(date);
        quote.setHasBeenRead(false);
        ORMDatabaseManager.getInstance().addQuote(quote);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_settings){
            Intent i = new Intent(this,SettingsList.class);
            startActivity(i);
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds mItems to the action bar if it is present.
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showFragment(int position) {

        getSupportActionBar().setTitle(mItems[position]);

        // Getting reference to the FragmentManager
        FragmentManager fragmentManager = getSupportFragmentManager();

        // Creating a fragment transaction
        FragmentTransaction ft = fragmentManager.beginTransaction();

        // Creating a fragment object with generics based on selection
        switch (position) {
            case 0: NewsfeedFragment newsfeedFragment = new NewsfeedFragment() ;
                    ft.replace(R.id.content_frame, newsfeedFragment );
                break;

            case 1: MessageFragment messagesFragment = new MessageFragment();
                    ft.replace(R.id.content_frame, messagesFragment);
                break;

            case 2: GalleryFragment galleryFragment = new GalleryFragment();
                ft.replace(R.id.content_frame, galleryFragment);
                break;

//            case 3: DownloadsFragment downloadsFragment = new DownloadsFragment();
//                ft.replace(R.id.content_frame, downloadsFragment);
//                break;

            case 3: TestimoniesFragment testimoniesFragment = new TestimoniesFragment();
                ft.replace(R.id.content_frame, testimoniesFragment);
                break;

//            case 5: ResourcesFragment resourcesFragment = new ResourcesFragment();
//                ft.replace(R.id.content_frame, resourcesFragment);
//                break;

            case 5: PrayersFragment prayersFragment = new PrayersFragment();
                ft.replace(R.id.content_frame, prayersFragment);
                break;

            default: NewsfeedFragment defaultFragment = new NewsfeedFragment() ;
                     ft.replace(R.id.content_frame, defaultFragment );
                break;
        }
        // Committing the transaction
        ft.commit();
    }

    // Highlight the selected country
    public void highlightSelectedItem(){
        int selectedItem = mDrawerList.getCheckedItemPosition();
            mDrawerList.setItemChecked(selectedItem, true);
    }


    @Override
    public void onFragmentInteraction(String id){

    }


    /**
     * Alarm Stuff
     */
    private final String BROADCAST_ACTION = "com.example.VIEW_ACTION";

    // Receive the action from the notification item when its clicked
    // This receiver can be used to receive intents from other applications as well not just our Notification
    BroadcastReceiver notifyServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //intent, from the arguments will contain the parameters from the Notification used to trigger our IntentFilter
            startActivityIfNeeded(new Intent(getApplicationContext(), BaseActivity.class), 1);
        }
    };

    // Register the Intent Receiver with tha Broadcast action it's to be called with
    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION);
        registerReceiver(notifyServiceReceiver, intentFilter);
    }

    private void unregisterReceiver() {
        try {
            this.unregisterReceiver(notifyServiceReceiver);
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        unregisterReceiver();
        super.onPause();
    }

    @Override
    protected void onResume() {
        registerReceiver();
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            showFragment(1);
        }else{
            showFragment(0);
        }
        super.onResume();
    }
}


