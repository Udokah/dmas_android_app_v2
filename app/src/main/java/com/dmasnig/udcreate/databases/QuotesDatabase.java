package com.dmasnig.udcreate.databases;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Quotes Table
 * Uses ORMLite package
 * http://ormlite.com/sqlite_java_android_orm.shtml
 *
 * Tutorial From
 * http://xtreamcoder.com/ormlite-tutorial/
 */

@DatabaseTable
public class QuotesDatabase {

    @DatabaseField(generatedId=true)
    private int id;

    @DatabaseField
    private String message;

    @DatabaseField
    private String date;

    @DatabaseField
    private boolean hasBeenRead;

    public void setId(int id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setHasBeenRead(boolean hasBeenRead) {
        this.hasBeenRead = hasBeenRead;
    }

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public boolean getHasBeenRead() {
        return hasBeenRead;
    }

}
