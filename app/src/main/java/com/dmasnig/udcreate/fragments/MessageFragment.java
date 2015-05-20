package com.dmasnig.udcreate.fragments;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dmasnig.udcreate.R;
import com.dmasnig.udcreate.databases.ORMDatabaseManager;
import com.dmasnig.udcreate.databases.QuotesDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link}
 * interface.
 */
public class MessageFragment extends ListFragment {

    private ListView listview ;

    private List<messageData> MESSAGE_QUOTES ;

    private OnFragmentInteractionListener mListener;
    private Context context;
    private View view;
    LayoutInflater customInflater ;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        this.customInflater = inflater ;
        // Attach the listview to the fragment
        view = inflater.inflate(R.layout.activity_fragment_messages, container, false);
        new LoadMessages().execute() ;

        // Set Onclick listener
       /* listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               *//* MyCustomAdapter myAdapter = new MyCustomAdapter();
                messageData selectedMessage = myAdapter.getMessageList(i);
                String theMessage = selectedMessage.name;*//*
                //Toast.makeText(context, "hello world", Toast.LENGTH_LONG).show();

            }
        });*/
        return view ;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
        this.context = activity.getApplicationContext() ;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id){
        MyCustomAdapter myAdapter = new MyCustomAdapter();
        messageData selectedMessage = myAdapter.getMessageList(position);
        String theMessage = selectedMessage.message ;
        TextView tv = (TextView) v.findViewById(R.id.snippet);
        tv.setText(theMessage);
        myAdapter.notifyDataSetChanged();
        //Toast.makeText(context, theMessage , Toast.LENGTH_LONG).show();
    }


    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(String id);
    }

    /* CUSTOM LISTVIEW ADAPTER CLASS */
    private class MyCustomAdapter extends BaseAdapter {
        List<messageData> messageList = MESSAGE_QUOTES ;
        String fulltext ;

        boolean i = true ;

        @Override
        public int getCount() {
            return messageList.size();
        }

        @Override
        public messageData getItem(int i) {
            return messageList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            messageData usermessages = messageList.get(position);
            fulltext = usermessages.message ;

            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.widget_messages_list_layout, parent, false);
                holder = new ViewHolder();
                holder.wrapper = (RelativeLayout) convertView.findViewById(R.id.ListWrapper);
                holder.Txtmsg = (TextView) convertView.findViewById(R.id.snippet) ;
                convertView.setTag(holder);
            }else{
                holder = (ViewHolder) convertView.getTag();
            }

            holder.wrapper.setTag(holder);
            holder.wrapper.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    ViewHolder holder1 = (ViewHolder)v.getTag();
                    holder1.Txtmsg.setText(fulltext);
                }
            });

            TextView messageText = (TextView) convertView.findViewById(R.id.snippet);
            TextView timeText = (TextView) convertView.findViewById(R.id.date);

            String substr = usermessages.message ;
            int textlength = 50 ;

            if( substr.length() > textlength ){
                substr = substr.substring(0, textlength) + "..." ;
            }

            // Apply fonts
            Typeface myTypeface = Typeface.createFromAsset( context.getAssets(), "fonts/droidsans.ttf");
            messageText.setTypeface(myTypeface);

            messageText.setText(substr);
            timeText.setText(usermessages.time);

            return convertView;
        }

        public messageData getMessageList(int position) {
            return messageList.get(position);
        }

    }

    private class messageData{
        String message ;
        String time ;
    }

    private class ViewHolder {
        RelativeLayout wrapper;
        TextView Txtmsg ;
    }

    private class LoadMessages extends AsyncTask<String , Integer , String> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... strings) {
            List<QuotesDatabase> quotesDatabaseList = ORMDatabaseManager.getInstance().getAllQuotes();
            List<messageData> messageList = new ArrayList<messageData>();
            for (int i =0 ; i<quotesDatabaseList.size() ; i++) {
                messageData msgobj = new messageData();

                /* Show only messages that have been read */
                if(quotesDatabaseList.get(i).getHasBeenRead()) {
                    msgobj.message = quotesDatabaseList.get(i).getMessage();
                    msgobj.time = quotesDatabaseList.get(i).getDate();
                    messageList.add(msgobj);
                }
            }
            MESSAGE_QUOTES = messageList ;
            return null ;
        }

        @Override
        protected void onPostExecute(String s) {
            MyCustomAdapter myCustomAdapter = new MyCustomAdapter();
            listview = (ListView) view.findViewById(android.R.id.list) ;
            listview.setAdapter(myCustomAdapter);
        }
    }

}
