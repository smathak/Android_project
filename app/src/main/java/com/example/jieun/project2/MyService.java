package com.example.jieun.project2;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

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

    public void test(){
        Log.i("notice", "test service");
    }


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

    public void GcmNotification(String message){

        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

        // Notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Notification을 누르면  팝업창이 뜬다.
        Intent notificationIntent = new Intent(this, FriendPopup.class);
        notificationIntent.putExtra("text", message);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("MjRemider: Friend Request!!")
                .setContentText(message)
                .setSmallIcon(R.mipmap.icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MAX);

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(800);

        notificationManager.notify(1234, builder.build());

    }
}
