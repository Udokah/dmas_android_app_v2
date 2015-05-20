package com.dmasnig.udcreate.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.dmasnig.udcreate.R;

/**
 * Created by ud on 10/12/14.
 */
public class About extends ActionBarActivity {

    ActionBar actionbar ;
    Context context ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = About.this ;
        setContentView(R.layout.activity_about);
        actionbar = getSupportActionBar() ;
        actionbar.setTitle("About");
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowHomeEnabled(true);

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/droidsans.ttf");
        TextView textview4 = (TextView) findViewById(R.id.textview4);
        TextView textview3 = (TextView) findViewById(R.id.textview3);
        TextView textview2 = (TextView) findViewById(R.id.textview2);
        TextView textview1 = (TextView) findViewById(R.id.textview1);
        textview4.setTypeface(myTypeface);
        textview3.setTypeface(myTypeface);
        textview2.setTypeface(myTypeface);
        textview1.setTypeface(myTypeface);
    }

}
