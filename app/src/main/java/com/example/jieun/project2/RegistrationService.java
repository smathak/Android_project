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
    private static final String ACTION_FOO = "com.example.jieun.project2.action.FOO";
    private static final String ACTION_BAZ = "com.example.jieun.project2.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.jieun.project2.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.jieun.project2.extra.PARAM2";

    public RegistrationService() {
        super("RegistrationService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, RegistrationService.class);
        intent.setAction(ACTION_FOO);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, RegistrationService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

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
                    Log.i("notice", "check token: "+token);
                    registration_ids.put(Arrays.asList(token));

//                    AppServer appServer = new AppServer();
//                    appServer.send(token, registration_ids);

                    pubSub = GcmPubSub.getInstance(mContext);
                    pubSub.subscribe(token, "/topics/jiuen", null);
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

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
