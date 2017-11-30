package com.example.jieun.project2;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class ThingsToDo extends AppCompatActivity {
    String title;
    String content;
    public static Double latitude;
    public static Double longitude;
    LatLng position;
    Intent intent;

    int year, month, day, hour, minute;
    AppServer appServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_things_to_do);

        intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);

        appServer = new AppServer();
    }

    public void addThings(View view){
        EditText titleText = (EditText)findViewById(R.id.titleText);
        EditText contentText = (EditText)findViewById(R.id.contentText);
        title = titleText.getText().toString();
        content = contentText.getText().toString();

        intent.putExtra("mode", "insert");
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);

        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", day);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);

        setResult(Activity.RESULT_OK, intent);
        year=0; month = 0; day = 0; hour =0; minute =0;
        finish();
    }

    public void choosePress(View view){
        Intent sendIntent = new Intent(this, FriendList.class);
        startActivityForResult(sendIntent, 0);
    }

    String friendName;
    String friendToken;
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                if(intent.getIntExtra("year", 0) == 0){
                    friendName = intent.getStringExtra("friendName");
                    friendToken = intent.getStringExtra("friendToken");
                    TextView textView = (TextView)findViewById(R.id.friendName);
                    textView.setText(friendName);
                }

                if(intent.getStringExtra("friendName")==null){
                    year = intent.getIntExtra("year", 0);
                    month = intent.getIntExtra("month", 0);
                    day = intent.getIntExtra("day", 0);
                    hour = intent.getIntExtra("hour", 0);
                    minute = intent.getIntExtra("minute", 0);

                    TextView DateAndTime = (TextView)findViewById(R.id.dateTitle);
                    DateAndTime.setText(String.valueOf(year)+"/"+String.valueOf(month)+"/"+String.valueOf(day)+" "+
                            String.valueOf(hour)+":"+String.valueOf(minute));
                }

            }
        }
    }

    public void sendPress(View view){
        EditText titleText = (EditText)findViewById(R.id.titleText);
        EditText contentText = (EditText)findViewById(R.id.contentText);
        title = titleText.getText().toString();
        content = contentText.getText().toString();
        // 만약 날짜를 따로 입력안하면 0, 0, 0, 0, 0 이 넘어간다.
        appServer.sendMarkerWithTime(Constants.MY_NAME, friendName, friendToken, title, content, latitude, longitude
                                        ,year, month, day, hour, minute);
        // Renew
        year = 0; month = 0; day = 0; hour = 0; minute = 0;

        finish();
    }

    // Date and Time
    public void timePicker(View view){
        Intent timeIntent = new Intent(this, DateTime.class);
        startActivityForResult(timeIntent, 0);
    }
}
