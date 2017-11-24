package com.example.jieun.project2;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

// Client
public class MyGcmListenerService extends GcmListenerService {
    // DB
    private SQLiteDatabase mDB;
    Cursor mCursor;
    Geocoder geocoder;
    List<Address> addresses;

    public MyGcmListenerService() {
    }

    @Override
    public void onMessageReceived(String from, Bundle data) {
        // DB
        FeedReaderDbHelper mDbHelper = new FeedReaderDbHelper(this);
        mDB = mDbHelper.getWritableDatabase();
        mDbHelper.onCreate(mDB);
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());       // Geocode

        String friendName = data.getString("name");     // 친구의 이름을 받음
        String friendToken = data.getString("token");     // 친구로부터 친구의 토큰을 받음(친구에게 메시지를 보내기 위해 저장해야 함)
        String message = data.getString("message");     // 나에게 온 수락 메세지일 경우 ("accepted your request") 라는 메세지가 온다.

        String sender_name = data.getString("sender_name");
        String title = data.getString("title");
        String content = data.getString("content");
        String category = data.getString("category");
        String lat = data.getString("latitude");
        String lng = data.getString("longitude");

        String year = data.getString("year");
        String month = data.getString("month");
        String day = data.getString("day");
        String hour = data.getString("hour");
        String minute = data.getString("minute");

        if(category != null && category.equals("marker")){
            Double latitude = Double.parseDouble(lat);
            Double longitude = Double.parseDouble(lng);

            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
                String featureName = addresses.get(0).getFeatureName();

                // Marker의 내용을 친구로 부터 받았으면 이를 지도에 표시하기
                // 단, 내가 표시한 Marker와 친구가 표시한 Marker를 구분해서 해야함
                // 먼저 DB의 things_table에 contentValues를 저장해줘야 한다. (title, content, latitude, longitude AND "friend name")
                ContentValues values = new ContentValues();
                values.put("title", title); values.put("content", content);
                values.put("latitude", latitude); values.put("longitude", longitude);
                values.put("featureName", featureName);
                values.put("sender_name", sender_name);
                values.put("year", Integer.parseInt(year));     values.put("month", Integer.parseInt(month));
                values.put("day", Integer.parseInt(day));     values.put("hour", Integer.parseInt(hour));
                values.put("minute", Integer.parseInt(minute));
                mDB.insert("things_table", null, values);

                // Notification을 주고 redraw_map을 한다.
                Intent markerIntent = new Intent();
                markerIntent.putExtra("title", title);
                markerIntent.putExtra("content", content);
                markerIntent.putExtra("latitude", latitude);
                markerIntent.putExtra("longitude", longitude);
                markerIntent.putExtra("featureName", featureName);
                markerIntent.putExtra("sender_name", sender_name);
                if(Integer.parseInt(year) != 0){
                    markerIntent.putExtra("dateAndTime", year+"/"+month+"/"+day+" "+hour+":"+minute);
                }
                markerIntent.setAction("my.broadcast.markerListener");
                sendBroadcast(markerIntent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(message != null && message.equals("accepted your request")){    // 친구로부터 온 수락메세지일 경우
            // 친구가 수락했음을 알리는 Pop up 메세지 띄우기
            Intent acceptIntent = new Intent();
            acceptIntent.putExtra("friendName", friendName);
            acceptIntent.putExtra("friendToken", friendToken);
            acceptIntent.putExtra("acceptMessage", message);
            acceptIntent.setAction("my.broadcast.gcmListener");
            sendBroadcast(acceptIntent);

            // 친구의 이름과 Token 을 DB 의 myname_table에 저장한다.
            ContentValues values = new ContentValues();
            values.put("friendName", friendName);   // 수락을 허락한 친구의 이름을 저장
            values.put("friendToken", friendToken);     // 친구의 토큰도 저장 -> 앞으로 친구에게 Marker를 보낼 때 이 토큰으로 보내면 된다.
            mDB.insert("myname_table", null, values);
            Log.i("notice", "test accepted your accept");
            // 친구 신청 끝.
        }

        // /topics/myname으로 한다. 그러면 Client App은 App server 에서 온 message 중 내 이름을 topic으로 보낸 메시지만 받는다.
        if (from.startsWith( "/topics/"+Constants.MY_NAME)) {
            // message received from some topic.
            Log.i("notice", "onMessageReceived TOPIC name : "+friendName);
            Log.i("notice", "onMessageReceived TOPIC token : "+friendToken);

            Intent gcmIntent = new Intent();
            gcmIntent.setAction("my.broadcast.gcmListener");
            gcmIntent.putExtra("friendName", friendName);
            gcmIntent.putExtra("friendToken", friendToken);
            sendBroadcast(gcmIntent);   // 나에게 친구 신청이 왔다는 팝업창을 띄운다.
        } else{
            // normal downstream message.
            Log.i("notice", "onMessageReceived NOT TOPIC: " + friendName);
        }


    }
}
