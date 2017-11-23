package com.example.jieun.project2;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;

import java.util.Calendar;

/**
 * Created by jieun on 11/23/2017.
 */

public class DateTimeCheck {
    Handler handler;
    Calendar calendar = Calendar.getInstance();


    public DateTimeCheck(){
        handler = new Handler();
    }

    public void checkDateTime(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                    handler.postDelayed(this, 60000);
            }
        }, 60000);
    }
}
