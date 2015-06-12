package com.dmasnig.udcreate.databases;

import android.content.Context;
import com.j256.ormlite.table.TableUtils;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by ud on 10/25/14.
 */
public class ORMDatabaseManager {

    static private ORMDatabaseManager instance;
    static public int UNREAD = 0;
    static public int READ = 1;

    String Column_Read_Status = "hasBeenRead" ;

    static public void init(Context ctx) {
        if (null==instance) {
            instance = new ORMDatabaseManager(ctx);
        }
    }

    static public ORMDatabaseManager getInstance() {
        return instance;
    }

    private ORMDatabaseHelper helper;
    private ORMDatabaseManager(Context ctx) {
        helper = new ORMDatabaseHelper(ctx);
    }

    private ORMDatabaseHelper getHelper() {
        return helper;
    }

    public List<QuotesDatabase> getAllQuotes() {
        List<QuotesDatabase> QuotesDatabase = null;
        try {
            QuotesDatabase = getHelper().getQuotesDao().queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return QuotesDatabase;
    }

    public boolean clearAllQuotes(){
        boolean result = false ;
        try{
            TableUtils.clearTable(getHelper().getConnectionSource(),QuotesDatabase.class);
            result = true ;
        }catch (SQLException e){
            e.printStackTrace();
        }
        return result ;
    }

    public void addQuote(QuotesDatabase c) {
        try {
            getHelper().getQuotesDao().create(c);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public QuotesDatabase getLastQuote(){
        QuotesDatabase q = new QuotesDatabase();
        q.setHasBeenRead(false);
        List<QuotesDatabase> QuotesDatabase = null;
        try {
            QuotesDatabase = getHelper().getQuotesDao().queryForEq(Column_Read_Status,q);
            return QuotesDatabase.get(0) ;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return q;
    }


    public void updateQuoteStatus(QuotesDatabase updateObj){
        try {
            getHelper().getQuotesDao().update(updateObj);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
