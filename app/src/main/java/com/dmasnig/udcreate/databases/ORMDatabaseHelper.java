package com.dmasnig.udcreate.databases;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ORMLite Database helper class
 */

public class ORMDatabaseHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "QuotesDB.sqlite";
    private static final String APP_DIR_NAME = "DmasApp" ;

    // any time you make changes to your database objects, you may have to increase the database version
    private static final int DATABASE_VERSION = 1;

    // the DAO object we use to access the SimpleData table
    private Dao<QuotesDatabase, Integer> quotesDatabaseIntegerDao = null;

    /**
     * Create Database in external storage
     * @param context
     */
    public ORMDatabaseHelper(Context context) {
        super(context, Environment.getExternalStorageDirectory().getAbsolutePath()
                + File.separator + APP_DIR_NAME + File.separator + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database,ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, QuotesDatabase.class);
        } catch (SQLException e) {
            Log.e(ORMDatabaseHelper.class.getName(), "Can't create database", e);
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db,ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            List<String> allSql = new ArrayList<String>();
            switch(oldVersion)
            {
                case 1:
                    //allSql.add("alter table AdData add column `new_col` VARCHAR");
                    //allSql.add("alter table AdData add column `new_col2` VARCHAR");
            }
            for (String sql : allSql) {
                db.execSQL(sql);
            }
        } catch (SQLException e) {
            Log.e(ORMDatabaseHelper.class.getName(), "exception during onUpgrade", e);
            throw new RuntimeException(e);
        }

    }


    public Dao<QuotesDatabase, Integer> getQuotesDao(){
        if (null == quotesDatabaseIntegerDao) {
            try {
                quotesDatabaseIntegerDao = getDao(QuotesDatabase.class);
            }catch (java.sql.SQLException e) {
                e.printStackTrace();
            }
        }
        return quotesDatabaseIntegerDao;
    }

}
