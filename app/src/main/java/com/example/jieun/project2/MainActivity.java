package com.example.jieun.project2;

import android.*;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Marker;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener{
    // DB
    private SQLiteDatabase mDB;
    Cursor mCursor;

    // GPS
    private GoogleApiClient googleApiClient;
    private LocationManager locationManager;
    private Location lastLocation;
    private LocationRequest locationRequest;
    boolean isGPSEnabled;
    Handler handler;

    // Geocoding
    Geocoder geocoder;
    List<Address> addresses;

    TextView nameExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.INTERNET)!= PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            return;

        // DB
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        mDB = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(mDB);

        handler = new Handler();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        // For location tracking using GPS, GoogleAPIClient is necessary
        if(googleApiClient==null){
            googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).build();
        }
        googleApiClient.connect();

        locationRequest =  new LocationRequest();
        locationRequest.setInterval(20000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    public void startPress(View view){  // This is needed to get start location
        if (isGPSEnabled == true)  {
            Toast.makeText(this, "GPS is turned on", Toast.LENGTH_LONG);
        }else{
            // turn on GPS automatically
        }

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
        // GCM
        Intent regisIntent = new Intent(this, RegistrationService.class);
        startService(regisIntent);

        // Show google map
        Intent ToTheMapIntent = new Intent(this, MapsActivity.class);
        startActivity(ToTheMapIntent);
    }

    @Override
    public void onLocationChanged(Location location) {
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

//    public void onDestroy(){
//        super.onDestroy();
//        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, (LocationListener) this);
//        if(googleApiClient != null){
//            googleApiClient.disconnect();
//        }
//        Log.i("notice", "onDestroy");
//    }

     public void createName(View view){
         Intent idIntent = new Intent(this, IdIntent.class);
         startActivityForResult(idIntent, 0);
     }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0) {
            if (resultCode == Activity.RESULT_OK) {
                String nickname = intent.getStringExtra("nickname");
                ContentValues name = new ContentValues();
                name.put("myname", nickname);
                mDB.insert("myname_table", null, name);
            }
        }
    }

    public void startWithID(View view){

        EditText nameText = (EditText)findViewById(R.id.nameText) ;
        String myname = nameText.getText().toString();
        // 데이터 베이스에 해당 name이 있는 지 없는 지 확인
        mCursor = mDB.query("myname_table", new String[] {"myname"},
                "myname=?", new String[]{myname}, null, null, null);
        Log.i("notice", "test: "+mCursor.toString());
        if(mCursor != null) {   // 있으면 MapActivity 에 이름을 넘겨준다.
            if (mCursor.moveToFirst()) {
                myname = mCursor.getString(0);  // 이름만 구한다.
                Constants.MY_NAME = myname;
                // GCM
                Intent regisIntent = new Intent(this, RegistrationService.class);
                startService(regisIntent);

                // Show google map
                Intent ToTheMapIntent = new Intent(this, MapsActivity.class);
                ToTheMapIntent.putExtra("myname", myname);
                startActivity(ToTheMapIntent);
            } else {
                nameExist = (TextView) findViewById(R.id.nameExist);
                nameExist.setText("No such name");
            }
        }
    }
    public void onStart(){
        super.onStart();
        nameExist = (TextView) findViewById(R.id.nameExist);
        nameExist.setText(" ");
    }
}
