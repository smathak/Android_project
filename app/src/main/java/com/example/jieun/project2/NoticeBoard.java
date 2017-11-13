package com.example.jieun.project2;

import android.app.Activity;
import android.app.ExpandableListActivity;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

public class NoticeBoard extends Activity {

    private Cursor mCursor;
    private SQLiteDatabase mDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_board);

        FeedReaderDbHelper mDBHelper = new FeedReaderDbHelper(this);
        mDB = mDBHelper.getReadableDatabase();
        mDBHelper.onCreate(mDB);

        ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
        mCursor = mDB.query("things_table", new String[]{"title", "content", "latitude", "longitude"},
                null, null, null, null, "_id");
        if(mCursor != null){
            if(mCursor.moveToFirst()){
                do{
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put(mCursor.getColumnName(0), mCursor.getString(0));
                    item.put(mCursor.getColumnName(1), mCursor.getString(1));
                    item.put(mCursor.getColumnName(2), String.valueOf(mCursor.getDouble(2)));
                    item.put(mCursor.getColumnName(3), String.valueOf(mCursor.getDouble(3)));
                    mList.add(item);
                }while(mCursor.moveToNext());
            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, mList, android.R.layout.simple_list_item_2,
                new String[]{"title", "content"}, new int[]{android.R.id.text1, android.R.id.text2});
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(simpleAdapter);
    }
}
