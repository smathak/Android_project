package com.example.jieun.project2;

import android.provider.BaseColumns;

/**
 * Created by jieun on 11/3/2017.
 */

public final class FeedReaderContract {

    public FeedReaderContract(){};


    public static abstract class FeedEntry implements BaseColumns {
        // things_table: 제목, 내용, 경도, 위도, 지명, 보낸 사람, 년, 월, 일, 시, 분 column을 가진다.
        public static final String TABLE_NAME = "things_table";

        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_FEATURENAME = "featureName";
        public static final String COLUMN_NAME_SENDER = "sender_name";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_DAY = "day";
        public static final String COLUMN_NAME_HOUR = "hour";
        public static final String COLUMN_NAME_MINUTE = "minute";


        // myname_table은 내 이름, 친구 목록, 친구 토큰 목록을 스키마로 가진다.
        public static final String MYNAME_TABLE = "myname_table";

        public static final String COLUMN_NAME_MYNAME = "myname";
        public static final String COLUMN_NAME_FRIEND_NAME = "friendName";
        public static final String COLUMN_NAME_FRIEND_TOKEN = "friendToken";
    }

    public static final String DATABASE_NAME = "reminderDB.db";

    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + FeedEntry.TABLE_NAME + " ( " +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_TITLE + " text, " +
                    FeedEntry.COLUMN_NAME_CONTENT + " text, " +
                    FeedEntry.COLUMN_NAME_LATITUDE + " double, " +
                    FeedEntry.COLUMN_NAME_LONGITUDE + " double, " +
                    FeedEntry.COLUMN_NAME_FEATURENAME + " text, " +
                    FeedEntry.COLUMN_NAME_SENDER + " text, "+
                    FeedEntry.COLUMN_NAME_YEAR + " int, " +
                    FeedEntry.COLUMN_NAME_MONTH+ " int, "+
                    FeedEntry.COLUMN_NAME_DAY+ " int, "+
                    FeedEntry.COLUMN_NAME_HOUR+ " int, "+
                    FeedEntry.COLUMN_NAME_MINUTE+ " int" + ")";

    public static final String SQL_CREATE_MYNAME_TABLE =
            "CREATE TABLE IF NOT EXISTS " + FeedEntry.MYNAME_TABLE + " ( " +
                    FeedEntry._ID + " INTEGER PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_MYNAME + " text, " +
                    FeedEntry.COLUMN_NAME_FRIEND_NAME + " text, "+
                    FeedEntry.COLUMN_NAME_FRIEND_TOKEN + " text" + ")";



    public static final String SQL_DELETE_TABLE =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

    public static final String SQL_DELETE_MY_NAME_TABLE =
            "DROP TABLE IF EXISTS " + FeedEntry.MYNAME_TABLE;

}

