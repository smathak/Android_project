package com.example.jieun.project2;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

// Client
public class MyGcmListenerService extends GcmListenerService {
    public MyGcmListenerService() {
    }

//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        if (from.startsWith( "/topics/jieun")) {
            // message received from some topic.
            Log.i("notice", "onMessageReceived TOPIC: "+message);
        } else {
            // normal downstream message.
            Log.i("notice", "onMessageReceived NOT TOPIC: " + message);
        }

        Intent gcmIntent = new Intent();
        gcmIntent.setAction("my.broadcast.gcmListener");
        gcmIntent.putExtra("gcmRegister", message);
        sendBroadcast(gcmIntent);
    }
}
