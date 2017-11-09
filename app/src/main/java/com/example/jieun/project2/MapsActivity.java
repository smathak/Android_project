package com.example.jieun.project2;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private SQLiteDatabase mDB;
    Cursor mCursor;
    Marker renewed;

    LocationManager locationManager;
    Location lastLocation;
    GPSListener gpsListener;
    boolean isGPSEnabled;
    Handler handler;

    String newTitle;
    String newContent;

    ArrayList<PendingIntent> mPendingIntentList;
    BroadcastReceiver broadcastReceiver;
    IntentFilter filter;

    public class GPSListener implements LocationListener {
        public void onLocationChanged(Location location) {
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();

            String msg = "Latitude: " + latitude + "\nLongitude: " + longitude;
            Log.i("notice", msg);
        }

        public void onProviderDisabled(String provider){}

        public void onProviderEnabled(String provider){}

        public void onStatusChanged(String provider, int status, Bundle extras){}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // create SQLite database
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        mDB = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(mDB);

        handler = new Handler();
        gpsListener = new GPSListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        mPendingIntentList = new ArrayList();
        filter = new IntentFilter();
        filter.addAction("my.broadcast.proximity");
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.i("notice", "I am near marker");
                String title = intent.getStringExtra("title");
                String content = intent.getStringExtra("content");
                String msg = "Title: "+title+"\n Things todo: "+content;
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        };
        registerReceiver(broadcastReceiver, filter);
    }

    public void onStart(){
        super.onStart();
        if (isGPSEnabled == true) {
            Log.i("notice", "gps ok");
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    //Location lastLocation = locationManager.getLastKnownLocation(Context.GPS_PROVIDERS);
                    lastLocation = getLastKnownLocation(locationManager);
                    if (lastLocation != null) {
                        Double latitude = lastLocation.getLatitude();
                        Double longitude = lastLocation.getLongitude();
                        String msg = "Latitude: " + latitude + "\nLongitude: " + longitude;
                        //Log.i("notice", "test lastlocation: " + msg);
                        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    } else if (lastLocation == null) {
                        Log.i("notice", "lastlocation is null");
                    }

                    long minTime = 100; // 0.1초
                    float minDistance = 0;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);

                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private Location getLastKnownLocation(LocationManager locationManager) {
        List<String> providers = locationManager.getProviders(true);
        Location lastLocation = null;
        for (String provider : providers) {
            try{
                Location location = locationManager.getLastKnownLocation(provider);
                if (location == null) {
                    continue;
                }
                if (lastLocation == null || location.getAccuracy() < lastLocation.getAccuracy()) {
                    lastLocation = location;
                }
            }catch(SecurityException e){
                e.printStackTrace();
            }
        }
        return lastLocation;
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

        // Add a marker in Sydney and move the camera
        LatLng latLng = new LatLng(37.222434, 127.186257);
        mMap.addMarker(new MarkerOptions().position(latLng).title("Myoungji University"));
        float zoomLevel = 16.0f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));

        int id = 0;
        // mDB query
        mCursor = mDB.query("things_table", new String[]{"title", "content", "latitude", "longitude"}, null, null, null, null, "_id");
        int i = 0;
        if(mCursor!=null) {
            if (mCursor.moveToPosition(0)) {
                do {
                    Double lat, lng;
                    String title, content;
                    // title, content, lat, lng
                    title = mCursor.getString(i++);
                    content = mCursor.getString(i++);
                    lat = mCursor.getDouble(i++);
                    lng = mCursor.getDouble(i);
                    mMap.addMarker(new MarkerOptions().title(title).snippet(content)
                            .position(new LatLng(lat, lng)));
                    try{
                        Intent proximityIntent = new Intent("my.broadcast.proximity");
                        proximityIntent.putExtra("id", id);
                        proximityIntent.putExtra("latitude", lat);
                        proximityIntent.putExtra("longitude", lng);
                        proximityIntent.putExtra("title", title);
                        proximityIntent.putExtra("content", content);
                        PendingIntent pendingintent = PendingIntent.getBroadcast(this, id++, proximityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        locationManager.addProximityAlert(lat, lng, 50, -1, pendingintent);
                        mPendingIntentList.add(pendingintent);

                    }catch(SecurityException e){
                        e.printStackTrace();
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
                LatLng position = new LatLng(lat, lng);

                ContentValues values = new ContentValues();
                values.put("title", title); values.put("content", content);
                values.put("latitude", lat); values.put("longitude", lng);

                // Save it to SQLite
                if(mode.equals("insert")){
                    mDB.insert("things_table", null, values);
                    mMap.addMarker(new MarkerOptions().title(title).snippet(content).position(position));
                }

                // Update marker
                if(mode.equals("update")) {
                    String _id = intent.getStringExtra("_id");
                    mDB.update("things_table", values, "_id=" + _id, null);
                    newContent = content;
                    redraw_map(renewed);
                }

                // Delete marker
                if(mode.equals("delete")){
                    String _id = intent.getStringExtra("_id");
                    mDB.delete("things_table", "_id="+_id, null);
                    redraw_map(renewed);
                }
            }
        }
    }

    public void redraw_map(Marker renewed){
        mMap.clear(); // clear map first

        // redraw
        mCursor = mDB.query("things_table", new String[]{"title", "content", "latitude", "longitude"}, null, null, null, null, "_id");
        int i = 0;
        if(mCursor!=null) {
            if (mCursor.moveToPosition(0)) {
                do {
                    Double lat, lng;
                    String title, content;
                    // title, content, lat, lng
                    title = mCursor.getString(i++);
                    content = mCursor.getString(i++);
                    lat = mCursor.getDouble(i++);
                    lng = mCursor.getDouble(i);
                    mMap.addMarker(new MarkerOptions().title(title).snippet(content)
                            .position(new LatLng(lat, lng)));

                    i = 0;
                } while (mCursor.moveToNext());
            }
        }
        onMarkerClick(renewed);
    }

    public void startLocationService(){


    }
}
