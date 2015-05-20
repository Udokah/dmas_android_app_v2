package com.dmasnig.udcreate.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.databases.ORMDatabaseManager;
import com.dmasnig.udcreate.utilities.Lib;


public class Startup extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Initialise Database
         * Note: this should be done
         *       once in the application.
         */
        ORMDatabaseManager.init(this);

        boolean isLoggedIn = Lib.getLoginStatus(this);
        if(isLoggedIn){
            Intent i = new Intent(this,BaseActivity.class);
            startActivity(i);
        }else{
            setContentView(R.layout.activity_startup);
        }
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void actionRegister(View v){
        Intent i = new Intent(this, Register.class);
        startActivity(i);
    }

    public void actionLogin(View v){
        Intent i = new Intent(this, Login.class);
        startActivity(i);
    }
}
