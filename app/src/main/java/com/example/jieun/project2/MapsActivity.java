package com.example.jieun.project2;

import android.*;
import android.Manifest;
import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
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

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        LocationListener, ResultCallback<Status>, OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    @Override
    public void onLocationChanged(Location location) {
//        Log.i("notice", "onLocationChanged: "+location.getLatitude()+", "+location.getLongitude());
        Double lat = location.getLatitude();
        Double lng = location.getLongitude();
        try{
            addresses = geocoder.getFromLocation(lat, lng, 1);
            String featureName = addresses.get(0).getFeatureName();

//            Toast.makeText(this, "OnMapActivity: Latitude: "+location.getLatitude()+ "\nLongitude: "+location.getLongitude()+
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

    String newContent;  // update variable

    // BroadCast Receiver
    BroadcastReceiver broadcastReceiver;
    IntentFilter filter;
    BroadcastReceiver gcmReceiver;
    IntentFilter gcmFilter;
    IntentFilter markerFilter;
    BroadcastReceiver markerReceiver;

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
    GeofencingClient geofencingClient;

    String myname;
    String friend;
    AppServer appServer;

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
//            googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            googleApiClient.connect();
        }

        locationRequest =  new LocationRequest();
        locationRequest.setInterval(20000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        filter = new IntentFilter();
        filter.addAction("my.broadcast.proximity");

        // GeofenceTransitionIntentService에서 Broadcast를 받으면
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String text = intent.getStringExtra("text");
                myService.sendNotification(text); // myService의 Notification을 호출
            }
        };
        registerReceiver(broadcastReceiver, filter);

        gcmFilter = new IntentFilter();
        gcmFilter.addAction("my.broadcast.gcmListener");

        gcmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String friendName = intent.getStringExtra("friendName");
                String friendToken = intent.getStringExtra("friendToken");  // 친구 이름과 그 토큰을 저장해야 함.
                Toast.makeText(getApplicationContext(), friendName, Toast.LENGTH_LONG).show();
                String message = intent.getStringExtra("acceptMessage");
                if(message!=null && message.equals("accepted your request")){
                    myService.sendNotification(friendName+" "+message);     // 수락 메세지 보내기
                }else{
                    myService.GcmNotification(friendName, friendToken);
                }
            }
        };
        registerReceiver(gcmReceiver, gcmFilter);

        markerFilter = new IntentFilter();
        markerFilter.addAction("my.broadcast.markerListener");

        markerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String title = intent.getStringExtra("title");
                String content = intent.getStringExtra("content");
                String featureName = intent.getStringExtra("featureName");
                String sender_name = intent.getStringExtra("sender_name");
                Double lat = intent.getDoubleExtra("latitude", 0.0);
                Double lng = intent.getDoubleExtra("longitude", 0.0);
                String message = "Title: "+title+"\nThings todo: "+content+"\nAt: "+featureName+"\nFrom: "+sender_name;
                myService.sendNotification(message);
                redraw_map("gcm");
            }
        };
        registerReceiver(markerReceiver, markerFilter);

        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());       // Geocode
        Intent serviceIntent = new Intent(getApplicationContext(), MyService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);

        geofenceList = new ArrayList<Geofence>();
        geofencingClient = LocationServices.getGeofencingClient(this);

        Intent gintent = new Intent(this, GeofenceTransitionIntentService.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, gintent, PendingIntent.FLAG_UPDATE_CURRENT);

        appServer = new AppServer();

        // Starts with myname
        Intent mainIntent = getIntent();
        myname = mainIntent.getStringExtra("myname");
        Constants.MY_NAME =myname;


        // 내 이름을 구독
        // 나(Client App)dl topic/myname 을 App server 로 부터 받으면 GcmListener 가 호출된다.

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

    public void onStart(){
        super.onStart();
        if (isGPSEnabled == true)  Log.i("notice", "gps turned on");

    }

    //    public void onStop(){
//        super.onStop();
//        if(mBound){
//            unbindService(serviceConnection);
//            mBound = false;
//        }
//        Log.i("notice", "onStop");
//    }

//    public void onDestroy(){
//        super.onDestroy();
//        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
//        if(googleApiClient != null){
//            googleApiClient.disconnect();
//        }
//        Log.i("notice", "onDestroy");
//    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    public GoogleMap getGoogleMap(){
        return mMap;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng latLng = new LatLng(37.222434, 127.186257);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Myoungji University"));
        float zoomLevel = 16.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

        // mDB query
        mCursor = mDB.query("things_table", new String[]{"title", "content", "latitude", "longitude", "featureName", "sender_name"},
                null, null, null, null, "_id");
        int i = 0;
        if(mCursor!=null) {
            if (mCursor.moveToPosition(0)) {
                do {
                    Double lat, lng;
                    String title, content, featureName, sender_name;
                    int year, month, day, hour, minute;
                    // title, content, lat, lng
                    title = mCursor.getString(i++);
                    content = mCursor.getString(i++);
                    lat = mCursor.getDouble(i++);
                    lng = mCursor.getDouble(i++);
                    featureName = mCursor.getString(i++);
                    sender_name = mCursor.getString(i);

                    sender_name = mCursor.getString(i);
                    if(sender_name != null){
                        Log.i("notice", "sender_name is: "+sender_name);
                        mMap.addMarker(new MarkerOptions().title(title).snippet(content+" from: "+sender_name)
                                .position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round)));
                    }else{

                        mMap.addMarker(new MarkerOptions().title(title).snippet(content)
                                .position(new LatLng(lat, lng)));
                    }
                    i = 0;
                } while (mCursor.moveToNext());
            }
        }

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
                Log.i("notice", "lat: "+lat+", lng: "+lng);
                LatLng position = new LatLng(lat, lng);
                String address;
                String featureName =" ";
                friend = intent.getStringExtra("friend");
                // 친구 등록 (친구 구독 - subscribe)
                if(friend!=null) {  // friend의 이름을 App server 에서 client app 으로 보낸다
                    appServer.registerFriend(friend);
                }

                ContentValues values = new ContentValues();
                values.put("title", title); values.put("content", content);
                values.put("latitude", lat); values.put("longitude", lng);

                try {
                    if (lat != 0.0 && lng != 0.0) {
                        addresses = geocoder.getFromLocation(lat, lng, 1);
                        address = addresses.get(0).getAddressLine(0);
                        featureName = addresses.get(0).getFeatureName();
                        Log.i("notice", address+", "+featureName);
                        values.put("featureName", featureName);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(mode!=null){
                    // Save it to SQLite
                    if(mode.equals("insert")){
                        mDB.insert("things_table", null, values);
                        renewed = mMap.addMarker(new MarkerOptions().title(title).snippet(content).position(position));
                        redraw_map("insert");
                    }

                    // Update marker
                    if(mode.equals("update")) {
                        String _id = intent.getStringExtra("_id");
                        mDB.update("things_table", values, "_id=" + _id, null);
                        newContent = content;
                        redraw_map("update");
                    }

                    // Delete marker
                    if(mode.equals("delete")){
                        String _id = intent.getStringExtra("_id");
                        mDB.delete("things_table", "_id="+_id, null);
                        redraw_map("delete");
                    }
                }

            }
        }
    }

    public void redraw_map(String mode){
        mMap.clear(); // clear map first
        Constants.POPUP_MESSAGE.clear(); // insert, update, delete 를 눌러도 모두 clear 해야 한다.
        geofenceList.clear();
        // 1. clear existed geofence;
        removeGeofences(); // 1. clear

        // redraw
        mCursor = mDB.query("things_table", new String[]{"title", "content", "latitude", "longitude", "featureName", "sender_name"},
                null, null, null, null, "_id");
        int i = 0;
        if(mCursor!=null)
            if (mCursor.moveToPosition(0)) {
                do {
                    Double lat, lng;
                    String title, content, featureName, sender_name;
                    int year, month, day, hour, minute;
                    // title, content, lat, lng
                    title = mCursor.getString(i++);
                    content = mCursor.getString(i++);
                    lat = mCursor.getDouble(i++);
                    lng = mCursor.getDouble(i++);
                    featureName = mCursor.getString(i++);
                    sender_name = mCursor.getString(i);
                    if(sender_name != null){
                        Log.i("notice", "sender_name is: "+sender_name);
                        mMap.addMarker(new MarkerOptions().title(title).snippet(content+"from: "+sender_name)
                                .position(new LatLng(lat, lng)).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_launcher_round)));
                        // renew Constants
                        Constants.POPUP_MESSAGE.put("Title: "+title+"\nThings todo: "+content+"\nAt: "+featureName+"\nFrom: "+sender_name, new LatLng(lat, lng));
                    }else{

                        mMap.addMarker(new MarkerOptions().title(title).snippet(content)
                                .position(new LatLng(lat, lng)));
                        // renew Constants
                        Constants.POPUP_MESSAGE.put("Title: "+title+"\nThings todo: "+content+"\nAt: "+featureName, new LatLng(lat, lng));
                    }

                    i = 0;
                } while (mCursor.moveToNext());
            }

        if(mode.equals("delete")){
            // do nothing
        }else{
            // 2. renew all the other geofences
            geofenceUpdate();
        }


    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.item:
                Intent intent = new Intent(this, NoticeBoard.class);
                startActivity(intent);
                return true;
            case R.id.request:
                try{
                    if(geofencingRequest != null){
                        LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(this);
                    }
                }catch(SecurityException e){
                    e.printStackTrace();
                }
                return true;
            case R.id.gcmFriend:
                Intent friendIntent = new Intent(this, FriendActivity.class);
                startActivityForResult(friendIntent, 0);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResult(@NonNull Status status) {}

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("notice", "googleApiClient connected");

        try{
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if(lastLocation == null){
                Log.i("notice", "last location is null");   // 가장 처음 시작할 때는 null 이다.
            }else{
                Log.i("notice", "MapActivity last location latitude: "+String.valueOf(lastLocation.getLatitude()));
//                Toast.makeText(this, "latitude test: "+String.valueOf(lastLocation.getLatitude()), Toast.LENGTH_LONG);
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        }catch(SecurityException e){
            e.printStackTrace();
        }

        // mDB query
        mCursor = mDB.query("things_table", new String[]{"title", "content", "latitude", "longitude", "featureName"}, null, null, null, null, "_id");
        int i = 0;
        if(mCursor!=null) {
            if (mCursor.moveToPosition(0)) {
                do {
                    Double lat, lng;
                    String title, content, featureName;
                    int year, month, day, hour, minute;
                    // title, content, lat, lng
                    title = mCursor.getString(i++);
                    content = mCursor.getString(i++);
                    lat = mCursor.getDouble(i++);
                    lng = mCursor.getDouble(i++);
                    featureName = mCursor.getString(i++);
                    Constants.POPUP_MESSAGE.put("Title: "+title+"\nThings todo: "+content+"\nAt: "+featureName, new LatLng(lat, lng));
                    i = 0;
                } while (mCursor.moveToNext());
            }
        }

        geofenceUpdate();
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

    public void geofenceUpdate(){
        for(Map.Entry<String, LatLng> entry : Constants.POPUP_MESSAGE.entrySet()) {
            geofenceList.add(new Geofence.Builder()
                    .setRequestId(entry.getKey())
                    .setCircularRegion(entry.getValue().latitude, entry.getValue().longitude, 45)
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());
        }

        Intent gintent = new Intent(this, GeofenceTransitionIntentService.class);
        pendingIntent = PendingIntent.getService(this, 0, gintent, PendingIntent.FLAG_UPDATE_CURRENT);

        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        try{
            geofencingRequest = builder.build();    // it will cause IllegalArumentException if geofenceList is empty
        }catch(IllegalArgumentException e){
            e.printStackTrace();
        }

        try{
            // Start tracking user's current location and compare with marker's position and send broadcast
            if(geofencingRequest != null){
                LocationServices.GeofencingApi.addGeofences(googleApiClient, geofencingRequest, pendingIntent).setResultCallback(this);
            }
        }catch(SecurityException e){
            e.printStackTrace();
        }catch(NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("notice", "suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i("notice", "onConnectionFailed: "+connectionResult.getErrorCode());
    }
}
