package com.example.jieun.project2;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

public class DateTime extends AppCompatActivity {
    int year, month, day;
    int hour, minute;
    DatePicker dp;
    EditText hourText;
    EditText minuteText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time);

        dp = (DatePicker)findViewById(R.id.datePicker);
        hourText = (EditText)findViewById(R.id.hourText);
        minuteText = (EditText)findViewById(R.id.minuteText);
    }

    public void addPress(View view){
        year = dp.getYear();
        month = dp.getMonth();
        day = dp.getDayOfMonth();

        hour = Integer.parseInt(hourText.getText().toString());
        minute = Integer.parseInt(minuteText.getText().toString());

        Intent intent = getIntent();
        intent.putExtra("year", year);
        intent.putExtra("month", month);
        intent.putExtra("day", day);

        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);

        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
