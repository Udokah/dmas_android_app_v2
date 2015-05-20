package com.dmasnig.udcreate.activities;

import android.app.Notification;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.dmasnig.udcreate.R;

/**
 * Created by ud on 10/11/14.
 */
public class ThreeOclockPrayer extends ActionBarActivity {
    ActionBar actionbar ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_three_oclock_prayer);

        actionbar = getSupportActionBar() ;
        actionbar.setTitle("3 O'clock Prayer To The Divine Mercy");

        // Enabling Up navigation
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setDisplayShowHomeEnabled(true);

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/droidsans.ttf");
        TextView content = (TextView) findViewById(R.id.prayer_by_three);
        content.setTypeface(myTypeface);
    }
}
