package com.dmasnig.udcreate.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.dmasnig.udcreate.R;

/**
 * Created by ud on 10/11/14.
 */
public class AboutDivineMercy extends ActionBarActivity {

    ActionBar actionbar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_divine_mercy);

        actionbar = getSupportActionBar() ;
        actionbar.setTitle("About The Divine Mercy");

        // Enabling Up navigation
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/droidsans.ttf");
        TextView content = (TextView) findViewById(R.id.about_dm_content);
        content.setTypeface(myTypeface);
    }

}
