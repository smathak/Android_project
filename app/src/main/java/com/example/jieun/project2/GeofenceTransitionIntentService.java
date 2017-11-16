package com.example.jieun.project2;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class GeofenceTransitionIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FOO = "com.example.jieun.project2.action.FOO";
    private static final String ACTION_BAZ = "com.example.jieun.project2.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "com.example.jieun.project2.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.example.jieun.project2.extra.PARAM2";

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionFoo(Context context, String param1, String param2) {
        Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
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
        Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public GeofenceTransitionIntentService() {
        super("GeofenceTransitionIntentService");
    }

      String text;

    @Override
    protected void onHandleIntent(Intent intent) {
        synchronized (this){
            GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            ArrayList triggeringGeofencingIdsList = new ArrayList();

            for(Geofence geofence : triggeringGeofences  ){
                text = geofence.getRequestId();
                triggeringGeofencingIdsList.add(geofence.getRequestId());
            }
            String IDs = TextUtils.join(", ", triggeringGeofencingIdsList);

            int transitiontype = geofencingEvent.getGeofenceTransition();
            if(transitiontype == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.i("notice", text);

                // Invoke popup
                Intent popupintent = new Intent(this, Popup.class);
                popupintent.putExtra("text", text);
                popupintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(popupintent);
                Vibrator v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(800);

                // Broadcast
                Intent broadIntent = new Intent();
                broadIntent.setAction("my.broadcast.proximity");
                broadIntent.putExtra("text", text);
                sendBroadcast(broadIntent);
            }
        }
    }
}
