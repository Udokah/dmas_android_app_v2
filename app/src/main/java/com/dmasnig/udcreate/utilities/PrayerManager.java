package com.dmasnig.udcreate.utilities;

import android.content.Context;

import com.dmasnig.udcreate.R;

/**
 * Created by udo on 6/14/15.
 */
public class PrayerManager {

    private String title;
    private String body;
    private Context context;

    public PrayerManager(Context c){
        this.context = c;
    }

    public void setPosition(int position){
        String[] list = context.getResources().getStringArray(R.array.list_of_prayers);
        this.title = list[position];
        switch(position){
            case 1:
                this.body = context.getResources().getString(R.string.prayer_by_three);
                break;
            case 2:
                this.body = context.getResources().getString(R.string.the_miracle_prayer);
                break;
            case 3:
                this.body = context.getResources().getString(R.string.prayer_for_mercy_for_the_dying);
                break;
            case 4:
                this.body = context.getResources().getString(R.string.prayer_to_the_divine_mercy);
                break;
            case 5:
                this.body = context.getResources().getString(R.string.prayer_for_a_lonely_soul);
        }
    }

    public String getTitle(){
        return this.title;
    }

    public String getBody(){
        return this.body;
    }

}
