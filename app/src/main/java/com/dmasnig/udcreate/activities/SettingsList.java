package com.dmasnig.udcreate.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.databases.ORMDatabaseManager;
import com.dmasnig.udcreate.utilities.Config;
import com.dmasnig.udcreate.utilities.Lib;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ud on 10/11/14.
 */
public class SettingsList extends ActionBarActivity {

    private Context context ;
    private String[] menuList;

    private int[] mIcons = new int[]{
            R.drawable.ic_account,
            R.drawable.ic_pay,
            R.drawable.ic_delete,
            R.drawable.ic_log_out ,
            R.drawable.ic_about
    };

    private LinearLayout layout;
    private ListView listView ;
    private SimpleAdapter simpleAdapter ;
    private List<HashMap<String, String>> prayerList;
    final private String ITEM = "menuItem";
    final private String ICONS = "menuIcon";
    private Intent intent ;

    ActionBar actionbar ;
    Dialog myDialog ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = SettingsList.this ;

        menuList = getResources().getStringArray(R.array.settings_menu) ;

        setContentView(R.layout.activity_settings);
        actionbar = getSupportActionBar() ;
        actionbar.setTitle("Settings");

        // Enabling Up navigation
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowHomeEnabled(true);

        listView = (ListView) findViewById(R.id.settings_list);

        /* Use prayer list layout instead of using new one */
        layout = (LinearLayout) findViewById(R.id.listview_layout);

        prayerList = new ArrayList<HashMap<String, String>>();
        for (int i = 0; i < 4; i++){
            HashMap<String, String> hm = new HashMap<String, String>();
            hm.put(ITEM, menuList[i]);
            hm.put(ICONS, Integer.toString(mIcons[i]));
            prayerList.add(hm);
        }

        // Keys used in Hashmap
        String[] from = {ITEM, ICONS};

        // Ids of views in listview_layout
        int[] to = {R.id.menuItem, R.id.menuIcon};

        /* Use Prayer list widget */
        simpleAdapter = new SimpleAdapter(context, prayerList, R.layout.widget_listview_item, from, to);

        listView.setAdapter(simpleAdapter);
        // ItemClick event handler for the drawer mItems
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {

                switch (position){
                    case 0: intent = new Intent(context, MyAccount.class);
                        startActivity(intent);
                        break;
                    case 1: openWebLink();
                        break;
                    case 2: confirmDelete();
                        break;
                    case 3: intent = new Intent(context, AboutDivineMercy.class);
                        logout();
                        break;
                    case 4: intent = new Intent(context, About.class);
                        startActivity(intent);
                        break;
                }
            }
        });

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

    /**
     * Open Link to Payment gateway where users
     * can pay
     */
    private void openWebLink(){
        Uri uri = Uri.parse(Config.server + Config.payment_url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    private void confirmDelete(){
        myDialog = new Dialog(context);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        myDialog.setContentView(R.layout.widget_custom_holo_dialog);
        myDialog.setCancelable(false);

        TextView dialogTitle = (TextView) myDialog.findViewById(R.id.dialog_title);
        dialogTitle.setText("Delete all quotes ?");

        TextView dialogContent = (TextView) myDialog.findViewById(R.id.dialog_content);
        dialogContent.setText("this action will delete all the daily quotes you have received ");

        Button no_button = (Button) myDialog.findViewById(R.id.no_button);
        no_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });

        Button yes_button = (Button) myDialog.findViewById(R.id.yes_button);
        yes_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                myDialog.dismiss();
                ORMDatabaseManager.getInstance().clearAllQuotes();
            }
        });

        myDialog.show();
    }


    private void logout(){
        /* Clear stored preferences */
        SharedPreferences prefs = Lib.getDevicesPreferences(context) ;
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();

        /* Clear messages in the database */
        ORMDatabaseManager.getInstance().clearAllQuotes();

        /* Clear all caches */
        deleteDirectoryTree(context.getCacheDir());

        /* redirect to start screen */
        Intent i = new Intent(context,Startup.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK) ;
        startActivity(i);
        finish();
    }

    /**
     * Deletes a directory tree recursively.
     */
    public static void deleteDirectoryTree(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteDirectoryTree(child);
            }
        }

        fileOrDirectory.delete();
    }




}
