package com.example.jieun.project2;

import android.app.Activity;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;

public class ThingsToDo extends AppCompatActivity {
    String title;
    String content;
    Double latitude;
    Double longitude;
    LatLng position;
    Intent intent;

    int year, month, day;
    int hour, minute;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_things_to_do);

        intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
//        Log.i("notice", position.toString());
    }

    public void pickPress(View view){
        Intent pickIntent = new Intent(this, DateTime.class);
        startActivityForResult(pickIntent, 0);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent pickIntent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0) {
            year = pickIntent.getIntExtra("year", 0);
            month = pickIntent.getIntExtra("month", 0);
            day = pickIntent.getIntExtra("day", 0);

            hour = pickIntent.getIntExtra("hour", 0);
            minute = pickIntent.getIntExtra("minute", 0);
        }
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
        finish();
    }

}
