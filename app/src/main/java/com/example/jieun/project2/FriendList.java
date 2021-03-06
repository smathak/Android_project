package com.example.jieun.project2;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;

// CHOOSE FRIEND 버튼을 누르면 열리는 친구 목록 리스트를 보여주는 클래스
public class FriendList extends Activity {
    private Cursor mCursor;
    private SQLiteDatabase mDB;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        intent = getIntent();

        FeedReaderDbHelper mDBHelper = new FeedReaderDbHelper(this);
        mDB = mDBHelper.getReadableDatabase();
        mDBHelper.onCreate(mDB);

        // "myname_table 에서 query
        ArrayList<HashMap<String, String>> mList = new ArrayList<HashMap<String, String>>();
        mCursor = mDB.query("myname_table", new String[]{"friendName", "friendToken"},
                null, null, null, null, null);
        if(mCursor != null){
            if(mCursor.moveToFirst()){
                do{
                    HashMap<String, String> item = new HashMap<String, String>();
                    item.put(mCursor.getColumnName(0), mCursor.getString(0));   // friend
//                    item.put(mCursor.getColumnName(1), mCursor.getString(1));   // friendToken
                    mList.add(item);
                }while(mCursor.moveToNext());
            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(this, mList, android.R.layout.simple_list_item_2,
                new String[]{"friendName", "friendToken"}, new int[]{android.R.id.text1, android.R.id.text2});
        ListView listView = (ListView)findViewById(R.id.listView);
        listView.setAdapter(simpleAdapter);

        // 리스트를 클릭하면
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCursor = mDB.query("myname_table", new String[]{"friendName", "friendToken"}, null, null, null, null, "_id");
                if(mCursor!=null){
                    mCursor.moveToFirst();
                }
                int i=0;
                while(i<position){
                    mCursor.moveToNext();
                    i++;
                }
                //해당 친구의 이름과 토큰을 을 MapsActivity로 반환
                String friendName = mCursor.getString(0);
                String friendToken = mCursor.getString(1);
                intent.putExtra("friendName", friendName);
                intent.putExtra("friendToken", friendToken);
                //-> MapsActivity에서 ->AppServer로 보낸다
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
