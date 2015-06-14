package com.dmasnig.udcreate.activities;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.support.v7.app.ActionBar ;

import com.dmasnig.udcreate.R;

/**
 * Created by ud on 10/11/14.
 */
public class NovenaPrayer extends ActionBarActivity {

        // When requested, this adapter returns a DemoObjectFragment,
        // representing an object in the collection.
        TabPagerAdapter mPagerAdapter;
        ViewPager mViewPager;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_faustina_novena);

            ActionBar actionBar = getSupportActionBar();

            actionBar.setTitle("Nine Days Novena To St. Faustina");

            // ViewPager and its adapters use support library
            // fragments, so use getSupportFragmentManager.
            mPagerAdapter = new TabPagerAdapter(getSupportFragmentManager());
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mPagerAdapter);

            // Create a tab listener that is called when the user changes tabs.
            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // When the tab is selected, switch to the
                    // corresponding page in the ViewPager.
                    mViewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

                }

                @Override
                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

                }

            };

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);

        }


 /*--------------------------------------------------------------------------------------------------*/
     /* Custom Pager Adapter*/
     private class TabPagerAdapter extends FragmentStatePagerAdapter {

     private String[] novenaDays ;

         public TabPagerAdapter(FragmentManager fm) {
             super(fm);
         }

     @Override
     public CharSequence getPageTitle(int position){
         novenaDays = getResources().getStringArray(R.array.novenadays) ;
         return novenaDays[position];
     }

         @Override
         public Fragment getItem(int i) {
             NovenaFragments novenaFragments = new NovenaFragments() ;
             Bundle args = new Bundle();
             args.putInt("DAY", i);
             novenaFragments.setArguments(args);
             return novenaFragments ;
         }

         @Override
         public int getCount() {
             return 9; //No of Tabs
         }

     }


/*--------------------------------------------------------------------------------------------------*/
     /* Custom Fragment */

    public static class NovenaFragments extends Fragment {
        public int day ;

        private String[] novenaTitles ;
        private String[] novenaDiaryEntries;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            String ARG_OBJECT = "DAY";
            Bundle args = getArguments();
            day = args.getInt(ARG_OBJECT) ;


            // Apply fonts
            Typeface myTypeface = Typeface.createFromAsset( getActivity().getAssets(), "fonts/droidsans.ttf");

             View mainview = inflater.inflate(R.layout.activity_prayers_novena_fragments, container, false);

            ((TextView) mainview.findViewById(R.id.textview4)).setTypeface(myTypeface);
            ((TextView) mainview.findViewById(R.id.textview3)).setTypeface(myTypeface);
            ((TextView) mainview.findViewById(R.id.textview2)).setTypeface(myTypeface);
            ((TextView) mainview.findViewById(R.id.textview1)).setTypeface(myTypeface);

            /* get all resources */
            novenaTitles = getResources().getStringArray(R.array.novenaDayTitles) ;
            novenaDiaryEntries = getResources().getStringArray(R.array.novenaDiaryEntries) ;

            // Set Prayer day title
            TextView pTitle = (TextView) mainview.findViewById(R.id.novena_prayer_title) ;
            pTitle.setText(novenaTitles[day]);

            // Set diary entry
            TextView pEntry = (TextView) mainview.findViewById(R.id.novena_diary_entry) ;
            pEntry.setText(novenaDiaryEntries[day]);
            pEntry.setTypeface(myTypeface);

            if(day == 8){
                // show closing prayer on 9th day
                TextView pClosing = (TextView) mainview.findViewById(R.id.closing_prayer) ;
                pClosing.setVisibility(View.VISIBLE);
                pClosing.setTypeface(myTypeface);
            }

            /*pagerTitleStrip = NovenaFragments.class.cast(mainview.findViewById(R.id.pager_title_strip));
            pagerTitleStrip.setTextSize(18, (float) 0.8);*/
            return mainview;
        }

    }


}




