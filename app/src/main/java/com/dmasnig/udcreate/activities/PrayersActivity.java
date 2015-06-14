package com.dmasnig.udcreate.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.widget.TextView;

import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.utilities.PrayerManager;

/**
 * Created by ud on 10/11/14.
 */

public class PrayersActivity extends ActionBarActivity {
    ActionBar actionbar ;
    PrayerManager prayer;
    int position;
    private static String key = "show-prayer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        position = extras.getInt(key);

        prayer = new PrayerManager(this);
        prayer.setPosition(position);

        setContentView(R.layout.activity_three_oclock_prayer);

        actionbar = getSupportActionBar() ;
        actionbar.setTitle(prayer.getTitle());

        // Enabling Up navigation
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeButtonEnabled(true);

        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/droidsans.ttf");
        TextView content = (TextView) findViewById(R.id.prayer_by_three);
        content.setText(prayer.getBody());
        content.setTypeface(myTypeface);
    }


}
