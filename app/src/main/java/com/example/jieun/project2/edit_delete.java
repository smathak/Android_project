package com.example.jieun.project2;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class edit_delete extends AppCompatActivity {
    Intent intent;
    String title;
    String content;
    Double latitude;
    Double longitude;
    String _id;

    int year, month, day;
    int hour, minute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_delete);

        intent = getIntent();
        title = intent.getStringExtra("title");
        content = intent.getStringExtra("content");
        latitude = intent.getDoubleExtra("latitude", 0);
        longitude = intent.getDoubleExtra("longitude", 0);
        _id = intent.getStringExtra("_id");

        EditText titleText = (EditText)findViewById(R.id.titleText);
        EditText contentText = (EditText)findViewById(R.id.contentText);
        titleText.setText(title);
        contentText.setText(content);
    }

    // 수정하기 위한 정보를 MapsActivity로 반환
    public void editThing(View view){
        EditText titleText = (EditText)findViewById(R.id.titleText);
        EditText contentText = (EditText)findViewById(R.id.contentText);
        title = titleText.getText().toString();
        content = contentText.getText().toString();

        intent.putExtra("mode", "update");
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("_id", _id);


        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    // 삭제하기 위해 _id를 반환
    public void deleteThing(View view){
        intent.putExtra("mode", "delete");
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("_id", _id);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
