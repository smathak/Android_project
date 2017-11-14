package com.example.jieun.project2;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

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

    public void showpopup(Intent popupintent){
        popupintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(popupintent);
    }
}
