package com.example.jieun.project2;

import android.provider.BaseColumns;

/**
 * Created by jieun on 11/3/2017.
 */

public final class FeedReaderContract {

    public FeedReaderContract(){};


    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "things_table";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }

    public static final String DATABASE_NAME = "reminderDB.db";
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + FeedEntry.TABLE_NAME + " ( " +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_TITLE + " text , " +
                    FeedEntry.COLUMN_NAME_CONTENT + " text , " +
                    FeedEntry.COLUMN_NAME_LATITUDE + " double , " +
                    FeedEntry.COLUMN_NAME_LONGITUDE + " double" + ") ";

    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

}

