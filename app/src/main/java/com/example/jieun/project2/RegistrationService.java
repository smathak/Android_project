package com.example.jieun.project2;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RegistrationService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS

    public RegistrationService() {
        super("RegistrationService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    private final Context mContext = this;
    private static final String[] TOPICS = {"friendrequest"};
    JSONArray registration_ids = new JSONArray();
    String token;
    GcmPubSub pubSub;
    @Override
    public void onHandleIntent(Intent intent) {
//        providing the app server's sender ID: This is necessary for client app to register with GCM connection server
//        to receive messages from app server

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                InstanceID instanceID = InstanceID.getInstance(mContext);
                try {
                    // Registration token(For client App) - 앱을 켜자마자 Client App이 downstram message를 받기 위한 App server의 token을 받음
                    token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    registration_ids.put(Arrays.asList(token));

//                    AppServer appServer = new AppServer();
//                    appServer.send(token, registration_ids);

                    pubSub = GcmPubSub.getInstance(mContext);
                    pubSub.subscribe(token, "/topics/"+Constants.MY_NAME, null);    // 나에게 오는 친구 신청을 받는다.

                    // myname_table에서 친구 리스트를 가져와 subscribe 한다.
                    // 친구 에게서 오는 gcm message를 받는다.

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();


//       Once you've received your registration token, make sure to send it to your server.

    }

    public void subscribeTopics(String friend) throws IOException {
        pubSub.subscribe(token, "/topics/" + friend, null);
    }
}
