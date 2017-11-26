package com.example.jieun.project2;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.util.Calendar;

public class DateTimeService extends IntentService {
    // DB
    private SQLiteDatabase mDB;
    Cursor mCursor;
    private final Context mContext = this;

    public DateTimeService() {
        super("DateTimeService");

    }

    Handler handler = new Handler();
    Calendar calendar;
    @Override
    protected void onHandleIntent(Intent intent) {
        // DB 초기화
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(mContext);
        mDB = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(mDB);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                handler.postDelayed(new Runnable() {
                    // 60초에 한번씩
                    public void run() {
                        // DB를 query 하면서
                        calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR); int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH); int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);
                        int second = calendar.get(Calendar.SECOND);
                        Log.i("notice", "DateTimeService: "+year+"/"+month+"/"+day+" "+hour+":"+minute+":"+second);
                        mCursor = mDB.query("things_table", new String[] {"title", "content", "featureName", "year", "month", "day", "hour", "minute"},
                                null, null, null, null, "_id");
                        int i = 0;
                        if(mCursor!=null){
                            if(mCursor.moveToFirst()){
                                do{
                                    String title = mCursor.getString(i++);
                                    String content = mCursor.getString(i++);
                                    String featureName = mCursor.getString(i++);
                                    int dbYear = mCursor.getInt(i++);
                                    int dbMonth = mCursor.getInt(i++);
                                    int dbDay = mCursor.getInt(i++);
                                    int dbHour = mCursor.getInt(i++);
                                    int dbMinute = mCursor.getInt(i);
                                    i=0;
                                    Log.i("notice", "DateTimeService DB: "+dbYear+"/"+dbMonth+"/"+dbDay+" "+dbHour+":"+dbMinute);
                                    // 현재 시간과 일치하는 메세지가 있으면
                                    // Notification을 보낸다. (sendNotification)
                                    if(year == dbYear && month == dbMonth && day == dbDay && hour == dbHour && minute == dbMinute){
                                        Log.i("notice", "DateTimeService: same");
                                        Intent dateTimeIntent = new Intent();
                                        dateTimeIntent.putExtra("text", "Title: "+title+"\nThings To do: "+content
                                                +"\nAt: "+featureName+"\nAt: "+dbYear+"/"+dbMonth+"/"+dbDay+" "+dbHour+":"+dbMinute);
                                        dateTimeIntent.setAction("my.broadcast.proximity");
                                        sendBroadcast(dateTimeIntent);
                                    }
                                }while(mCursor.moveToNext());
                            }
                        }
                        handler.postDelayed(this, 20000);
                    }
                }, 20000);
                return null;
            }
        }.execute();

    }
}
