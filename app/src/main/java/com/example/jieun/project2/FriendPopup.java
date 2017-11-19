package com.example.jieun.project2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;

public class FriendPopup extends AppCompatActivity {

    String text;
    AppServer appServer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_popup);
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
        text = intent.getStringExtra("text");

        TextView titleText = (TextView)findViewById(R.id.textView);

        titleText.setText(text+" want to be your friend");
    }

    public void pressAccept(View view){
        // 신청한 친구를 구독한다.(친구가 보내는 메세지를 받음)
        RegistrationService registrationService = new RegistrationService();
        try {
            registrationService.subscribeTopics(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 친구 리스트에  추가

        // 나의 수락 메세지도 보내준다.(친구도 나의 메세지를 받음)
//        appServer.registerFriend(Constants.MyNickName);
    }

    public void pressReject(View view){
        finish();
    }
}
