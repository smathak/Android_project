package com.example.jieun.project2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private SQLiteDatabase mDB;
    Cursor mCursor;

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

        // mDB query
        mCursor = mDB.query("things_table", new String[]{"title", "content", "latitude", "longitude"}, null, null, null, null, "_id");
        int i = 0;
        if(mCursor!=null) {
            if (mCursor.moveToPosition(0)) {
                Log.i("notice", "test moveToFirst onStart");
                do {
                    Double lat, lng;
                    String title, content;
                    // title, content, lat, lng
                    title = mCursor.getString(i++);
                    content = mCursor.getString(i++);
                    lat = mCursor.getDouble(i++);
                    lng = mCursor.getDouble(i);
                    mMap.addMarker(new MarkerOptions().title(title).snippet(content).position(new LatLng(lat, lng)));

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
    }

    /* Called when the user clicks a marker. */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Retrieve the data from the marker.
        String clickedMarker =  (String) marker.getTitle();     // It keeps return null

        if (clickedMarker != null) {
            Toast.makeText(this, clickedMarker + " has been clicked ", Toast.LENGTH_SHORT).show();
        }

        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    public void onStart(){
        super.onStart();
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
                Double lat = intent.getDoubleExtra("latitude", 0);
                Double lng = intent.getDoubleExtra("longitude", 0);
                LatLng position = new LatLng(lat, lng);
                String title = intent.getStringExtra("title");
                String content = intent.getStringExtra("content");
                mMap.addMarker(new MarkerOptions().title(title).snippet(content).position(position));

                // Save it to SQLite
                ContentValues values = new ContentValues();
                values.put("title", title); values.put("content", content);
                values.put("latitude", lat); values.put("longitude", lng);
                mDB.insert("things_table", null, values);
            }
        }
    }
}
