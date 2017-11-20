package com.example.jieun.project2;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Text;

public class ThingsToDo extends AppCompatActivity {
    String title;
    String content;
    public static Double latitude;
    public static Double longitude;
    LatLng position;
    Intent intent;

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

        setResult(Activity.RESULT_OK, intent);
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
                friendName = intent.getStringExtra("friendName");
                friendToken = intent.getStringExtra("friendToken");
                TextView textView = (TextView)findViewById(R.id.friendName);
                textView.setText(friendName);
            }
        }
    }

    public void sendPress(View view){
        EditText titleText = (EditText)findViewById(R.id.titleText);
        EditText contentText = (EditText)findViewById(R.id.contentText);
        title = titleText.getText().toString();
        content = contentText.getText().toString();

        appServer.sendMarker(Constants.MY_NAME, friendName, friendToken, title, content, latitude, longitude);
        finish();
    }
}
