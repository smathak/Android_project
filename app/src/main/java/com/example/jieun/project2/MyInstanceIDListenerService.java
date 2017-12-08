package com.example.jieun.project2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import java.io.IOException;

/**
 * Created by jieun on 11/17/2017.
 */

public class MyInstanceIDListenerService extends InstanceIDListenerService {

    public MyInstanceIDListenerService() {
    }

//    private static final String TAG = "MyInstanceIDLS";

    @Override
    // Start refresh token
    // GCM Registration token이 갱신되었을 경우 이 함수가 호출되야한 한다.
    public void onTokenRefresh() {
        // Listener service를 부름
        Intent intent = new Intent(this, RegistrationService.class);
        startService(intent);
    }
    // end refresh token


//    @Override
//    public IBinder onBind(Intent intent) {
//        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
//    }
}
