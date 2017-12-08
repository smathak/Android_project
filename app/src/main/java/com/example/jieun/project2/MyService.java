package com.example.jieun.project2;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;

public class MyService extends Service {
    private final IBinder iBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        MyService getService(){
            return MyService.this;
        }
    }
    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return iBinder;
    }

// Marker와 위치가 근접했을 때 Notification을 보내주는 함수
    public void sendNotification(String text){

        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

        // Notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, Popup.class);
        notificationIntent.putExtra("text", text);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("MjRemider: You have things to do here")
                .setContentText(text)
                .setSmallIcon(R.mipmap.icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(800);

        notificationManager.notify(1234, builder.build());

    }

    // 친구 신청, 수락, 메세지와 관련된 모든 Notification
    public void GcmNotification(String friendName, String friendToken){

        // Notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification을 누르면  팝업창이 뜬다.
        Intent notificationIntent = new Intent(this, FriendPopup.class);
        notificationIntent.putExtra("friendName", friendName);
        notificationIntent.putExtra("friendToken", friendToken);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("MjRemider: Friend Request!!")
                .setContentText(friendName)
                .setSmallIcon(R.mipmap.icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(800);

        notificationManager.notify(1234, builder.build());

    }

    Handler handler;
    Calendar calendar;
    public void dateTimeCheck(){

        calendar = Calendar.getInstance();
        handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("notice", "my service dateTimeCheck test");
                handler.postDelayed(this, 5000);
            }
        }, 5000);
    }
}
