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
    // The listener service's onTokenRefresh method should be invoked if the GCM registration token has been refreshed:

    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
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
