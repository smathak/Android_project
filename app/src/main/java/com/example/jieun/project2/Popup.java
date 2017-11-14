package com.example.jieun.project2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Popup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popup);
        Log.i("notice", "popup");

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height =  displayMetrics.heightPixels;

        getWindow().setLayout((int)(width*0.8), (int)(height*0.6));

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String content = intent.getStringExtra("content");
        String feature = intent.getStringExtra("featureName");

        TextView titleText = (TextView)findViewById(R.id.titleText);
        TextView contentText = (TextView)findViewById(R.id.contentText);
        TextView featureName = (TextView)findViewById(R.id.featureName);

        titleText.setText("Title: "+title);
        contentText.setText("Things to do: "+content);
        featureName.setText("At: "+feature);
    }

    public void pressClose(View view){
        finish();
    }
}
