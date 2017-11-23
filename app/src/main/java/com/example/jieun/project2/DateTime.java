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
    DatePicker datePicker;
    TimePicker timePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time);

        datePicker = (DatePicker)findViewById(R.id.datePicker);
        timePicker = (TimePicker)findViewById(R.id.timePicker);
    }

    public void addPress(View view){
        year = datePicker.getYear();
        month = datePicker.getMonth();
        day = datePicker.getDayOfMonth();

        hour = timePicker.getCurrentHour();
        minute = timePicker.getCurrentMinute();

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
