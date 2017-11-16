package com.example.jieun.project2;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.wearable.CapabilityApi;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener, ResultCallback<Status> {

    @Override
    public void onLocationChanged(Location location) {
        Log.i("notice", "onLocationChanged: "+location.getLatitude()+", "+location.getLongitude());
        Double lat = location.getLatitude();
        Double lng = location.getLongitude();
        try{
            addresses = geocoder.getFromLocation(lat, lng, 1);
            String featureName = addresses.get(0).getFeatureName();

//            Toast.makeText(this, "Latitude: "+location.getLatitude()+ "\nLongitude: "+location.getLongitude()+
//                    "\nCurrent location: "+featureName, Toast.LENGTH_LONG).show();
        }catch(IOException e) {
            e.printStackTrace();
        }

    }

    private GoogleMap mMap;
    private SQLiteDatabase mDB;
    Cursor mCursor;
    Marker renewed;

    private GoogleApiClient googleApiClient;
    private LocationManager locationManager;
    private Location lastLocation;
    private LocationRequest locationRequest;
    boolean isGPSEnabled;
    Handler handler;

    String newTitle;
    String newContent;

    // BroadCast Receiver
    BroadcastReceiver broadcastReceiver;
    IntentFilter filter;

    // Popup
    Intent popupIntent;
    Vibrator v;

    // Geocoding
    Geocoder geocoder;
    List<Address> addresses;

    // Service
    MyService myService;
    boolean mBound = false;

    //Geofence
    ArrayList<Geofence> geofenceList;
    PendingIntent pendingIntent;
    GeofencingRequest geofencingRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        String[] permissions = new String[]{android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                int result = PermissionChecker.checkSelfPermission(this, permission);
                if (result == PermissionChecker.PERMISSION_GRANTED);
                else ActivityCompat.requestPermissions(this, permissions, 1);
            }
       }

        if (Build.VERSION.SDK_INT>=23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            return;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // create SQLite database
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        mDB = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(mDB);

        handler = new Handler();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(googleApiClient==null){
            googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        }
        googleApiClient.connect();

        locationRequest =  new LocationRequest();
        locationRequest.setInterval(20000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        popupIntent = new Intent(getApplicationContext(), Popup.class);
        v = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        filter = new IntentFilter();
        filter.addAction("my.broadcast.proximity");

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("notice", "broadcast");
                String text = intent.getStringExtra("text");
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

                sendNotification(text);
            }
        };
        registerReceiver(broadcastReceiver, filter);

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        Intent serviceIntent = new Intent(getApplicationContext(), MyService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void sendNotification(String text){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, Popup.class);
        notificationIntent.putExtra("text", text);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("MjRemider: You have things to do here")
                .setContentText(text)
                .setSmallIcon(R.mipmap.icon)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MAX);

        notificationManager.notify(1234, builder.build());
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            MyService.LocalBinder binder = (MyService.LocalBinder) iBinder;
            myService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    public void onStop(){
        super.onStop();
        if(mBound){
            unbindService(serviceConnection);
            mBound = false;
        }
        Log.i("notice", "onStop");
    }

    public void onDestroy(){
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        if(googleApiClient != null){
            googleApiClient.disconnect();
        }
        Log.i("notice", "onDestroy");
    }

    public void onStart(){
        super.onStart();
        if (isGPSEnabled == true)  Log.i("notice", "gps turned on");

        try{
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if(lastLocation == null){
                Log.i("notice", "last location is null");   // 가장 처음 시작할 때는 null 이다.
            }else{
                Log.i("notice", "last location latitude: "+String.valueOf(lastLocation.getLatitude()));
                Toast.makeText(this, "latitude test: "+String.valueOf(lastLocation.getLatitude()), Toast.LENGTH_LONG);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    public void removeGeofences(){
        try{
            Intent intent = new Intent(this, GeofenceTransitionIntentService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            LocationServices.GeofencingApi.removeGeofences(googleApiClient, pendingIntent).setResultCallback(this);
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        geofenceList = new ArrayList<Geofence>();

        // Add a marker in Sydney and move the camera
        LatLng latLng = new LatLng(37.222434, 127.186257);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Myoungji University"));
        float zoomLevel = 16.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

        // mDB query
        mCursor = mDB.query("things_table", new String[]{"title", "content", "latitude", "longitude", "featureName"}, null, null, null, null, "_id");
        int i = 0;
        if(mCursor!=null) {
            if (mCursor.moveToPosition(0)) {
                do {
                    Double lat, lng;
                    String title, content, featureName;
                    // title, content, lat, lng
                    title = mCursor.getString(i++);
                    content = mCursor.getString(i++);
                    lat = mCursor.getDouble(i++);
                    lng = mCursor.getDouble(i);
                    featureName = mCursor.getString(i++);
                    mMap.addMarker(new MarkerOptions().title(title).snippet(content).position(new LatLng(lat, lng)));
                    Constants.POPUP_MESSAGE.put("Title: "+title+"\nThings todo: "+content+"\nAt: "+featureName, new LatLng(lat, lng));
                    i = 0;  
                } while (mCursor.moveToNext());
            }
        }

        for(Map.Entry<String, LatLng> entry : Constants.POPUP_MESSAGE.entrySet())
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(entry.getValue().latitude, entry.getValue().longitude, 30)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

        Intent gintent = new Intent(this, GeofenceTransitionIntentService.class);
//        gintent.setAction("my.broadcast.proximity");
        pendingIntent = PendingIntent.getBroadcast(this, 0, gintent, PendingIntent.FLAG_UPDATE_CURRENT);

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        try{
            geofencingRequest = builder.build();
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }
        Log.i("notice", "onMapReady called");
//        try{
//            LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(this);
//        }catch(SecurityException e){
//            e.printStackTrace();
//        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener(){
            @Override
            public void onMapLongClick(LatLng position){
                Log.i("notice", position.toString());
                // 새로운 할 일(Things to do)를 SQLite에 추가할 때, LatLng 를 키 값으로 쓴다.
                add(position.latitude, position.longitude);
            }
        });

        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowLongClickListener(new GoogleMap.OnInfoWindowLongClickListener(){
            @Override
            public void onInfoWindowLongClick(Marker marker){
                edit_delete_func(marker, marker.getTitle(), marker.getSnippet(), marker.getPosition());
            }
        });

        try{
            mMap.setMyLocationEnabled(true);
        }catch(SecurityException e){
            e.printStackTrace();
        }

    }

    public void edit_delete_func(Marker marker, String title, String content, LatLng position){
        renewed = marker; // get the marker to change
        Intent intent = new Intent(this, edit_delete.class);
        String _id;
        mCursor = mDB.query("things_table", new String[] {"_id"},
                "title=?", new String[]{title}, null, null, null);

        if(mCursor!=null){
            if(mCursor.moveToFirst()){
                do{
                    _id = mCursor.getString(0);
                    intent.putExtra("_id", _id);
                }while(mCursor.moveToNext());
            }
        }
        intent.putExtra("title", title);
        intent.putExtra("content", content);
        intent.putExtra("latitude", position.latitude);
        intent.putExtra("longitude", position.longitude);
        startActivityForResult(intent, 0);
    }
    /* Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Retrieve the data from the marker.
        String clickedMarker =  (String) marker.getTitle();     // It keeps return null
        if (clickedMarker != null) {
            Toast.makeText(this, clickedMarker + " has been clicked ", Toast.LENGTH_SHORT).show();
        }
        // false로 설정하면 Default로 지정된 결과가 실행됨 (title과 snippet을 보여줌)
        return false;
    }

    public void add(double latitude, double longitude){
        Intent intent = new Intent(this, ThingsToDo.class);
        Double lat = latitude;
        Double lng = longitude;
        intent.putExtra("latitude", lat);
        intent.putExtra("longitude", lng);
        startActivityForResult(intent, 0);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        if(requestCode == 0){
            if(resultCode == Activity.RESULT_OK){
                String title = intent.getStringExtra("title");
                String content = intent.getStringExtra("content");
                String mode = intent.getStringExtra("mode");
                Double lat = intent.getDoubleExtra("latitude", 0);
                Double lng = intent.getDoubleExtra("longitude", 0);
                LatLng position = new LatLng(lat, lng);
                String address;
                String featureName =" ";

                ContentValues values = new ContentValues();
                values.put("title", title); values.put("content", content);
                values.put("latitude", lat); values.put("longitude", lng);

                try {
                    addresses = geocoder.getFromLocation(lat, lng, 1);
                    address = addresses.get(0).getAddressLine(0);
                    featureName = addresses.get(0).getFeatureName();
                    Log.i("notice", address+", "+featureName);
                    values.put("featureName", featureName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Save it to SQLite
                if(mode.equals("insert")){
                    mDB.insert("things_table", null, values);
                    renewed = mMap.addMarker(new MarkerOptions().title(title).snippet(content).position(position));
                    redraw_map(renewed, "insert");
                }

                // Update marker
                if(mode.equals("update")) {
                    String _id = intent.getStringExtra("_id");
                    mDB.update("things_table", values, "_id=" + _id, null);
                    newContent = content;
                    redraw_map(renewed, "update");
                }

                // Delete marker
                if(mode.equals("delete")){
                    String _id = intent.getStringExtra("_id");
                    mDB.delete("things_table", "_id="+_id, null);
                    redraw_map(renewed, "delete");
                }
            }
        }
    }

    public void redraw_map(Marker renewed, String mode){
        mMap.clear(); // clear map first
        Constants.POPUP_MESSAGE.clear(); // insert, update, delete 를 눌러도 모두 clear 해야 한다.
        geofenceList.clear();

        // redraw
        mCursor = mDB.query("things_table", new String[]{"title", "content", "latitude", "longitude", "featureName"}, null, null, null, null, "_id");
        int i = 0;
        if(mCursor!=null)
            if (mCursor.moveToPosition(0)) {
                do {
                    Double lat, lng;
                    String title, content, featureName;
                    // title, content, lat, lng
                    title = mCursor.getString(i++);
                    content = mCursor.getString(i++);
                    lat = mCursor.getDouble(i++);
                    lng = mCursor.getDouble(i++);
                    featureName = mCursor.getString(i);

                    mMap.addMarker(new MarkerOptions().title(title).snippet(content)
                            .position(new LatLng(lat, lng)));
                    // renew Constants
                    Constants.POPUP_MESSAGE.put("Title: " + title + "\nThings todo: " + content+"\nAt: "+featureName, new LatLng(lat, lng));
                    i = 0;
                } while (mCursor.moveToNext());
            }
        // Delete를 한 경우
        if(mode.equals("delete")){
            // 1. clear existed geofence;
            removeGeofences(); // 1. clear

            // 2. renew all the other geofences
            for(Map.Entry<String, LatLng> entry : Constants.POPUP_MESSAGE.entrySet())   // 2. renew
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                        .setCircularRegion(entry.getValue().latitude, entry.getValue().longitude, 30)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());

            Intent gintent = new Intent(this, GeofenceTransitionIntentService.class);
            pendingIntent = PendingIntent.getService(this, 0, gintent, PendingIntent.FLAG_UPDATE_CURRENT);

            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(geofenceList);
            try{
                geofencingRequest = builder.build();
            }catch(IllegalArgumentException e){
                e.printStackTrace();
            }
        }

        // Insert, Udpate, Request를 한 경우
        if(mode.equals("insert") | mode.equals("update") | mode.equals("request")){
            removeGeofences(); // 1. clear

            for(Map.Entry<String, LatLng> entry : Constants.POPUP_MESSAGE.entrySet())   // 2. renew
                geofenceList.add(new Geofence.Builder()
                        .setRequestId(entry.getKey())
                        .setCircularRegion(entry.getValue().latitude, entry.getValue().longitude, 30)
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());

            Intent gintent = new Intent(this, GeofenceTransitionIntentService.class);
            pendingIntent = PendingIntent.getService(this, 0, gintent, PendingIntent.FLAG_UPDATE_CURRENT);

            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            builder.addGeofences(geofenceList);
            try{
                geofencingRequest = builder.build();
            }catch(IllegalArgumentException e){
                e.printStackTrace();
            }

            try{ // 3. restart(insert, update, request 에서만)
                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(this);
            }catch(SecurityException e){
                e.printStackTrace();
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void onRestart(){
        super.onRestart();
        try{
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if(lastLocation == null){
                Log.i("notice", "last location is null");   // 가장 처음 시작할 때는 null 이다.
            }else{
                Log.i("notice", "last location latitude: "+String.valueOf(lastLocation.getLatitude()));
            }
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.item:
                Intent intent = new Intent(this, NoticeBoard.class);
                startActivity(intent);
                return true;
            case R.id.request:
                try{
                    onRestart();
                    redraw_map(renewed, "request");
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

//                    try{
//                        LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(this);
//                    }catch(SecurityException e){
//                        e.printStackTrace();
//                    }
                }catch(SecurityException e){
                    e.printStackTrace();
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResult(@NonNull Status status) {

    }
}
