package com.example.jieun.project2;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;

public class FriendPopup extends AppCompatActivity {

    String friendName;
    String friendToken;
    AppServer appServer;

    // DB
    private SQLiteDatabase mDB;
    Cursor mCursor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_popup);
        // DB
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        mDB = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(mDB);
        // App Server
        appServer = new AppServer();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height =  displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        Intent intent = getIntent();
        friendName = intent.getStringExtra("friendName");   // 친구 이름
        friendToken = intent.getStringExtra("friendToken"); // 친구 토큰 -> DB에 저장하기

        TextView titleText = (TextView)findViewById(R.id.friendName);
        titleText.setText(friendName+" want to be your friend");
    }

    public void pressAccept(View view){
        // 친구를 myname_table에 저장
        ContentValues values = new ContentValues();
        values.put("friendName", friendName);
        values.put("friendToken", friendToken);     // 친구에게 메시지를 보낼때는 이 friendToken을 이용한다.
        mDB.insert("myname_table", null, values);

        // 수락했음을 알려줌. => 나의 이름과 나의 token 과 수락 메세지를 친구의 token으로 보내줌
        AppServer appServer = new AppServer();
        appServer.sendAcceptMessage(Constants.MY_NAME, friendToken); // MapsActivity 에서 친구 토큰을 받아오기
        finish();
    }

    public void pressReject(View view){
        finish();
    }
}
