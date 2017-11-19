package com.example.jieun.project2;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jieun on 11/19/2017.
 */



public class AppServer {

    String server_key = "AAAAgkUXc-0:APA91bHpxOFapwTWJATAGNYLlQ0HcXW3k4RrsfekWb-VMx-LYrjBSJM2UMWTegoSfPbollQY0svzv7MioTp-JpA5niHD2YhpM19PvwU14_fu4EUyU1yNw6WuLa5PiWdhikKXNvzCND-n"; // Server key(For app Server in Firebase)
    String sender_id = "559504913389";
    JSONObject jsonObject = new JSONObject();
    JSONObject down = new JSONObject();
    // GCM Server HTTP 방식
//        https://android.googleapis.com/gcm/notification
//        https://gcm-http.googleapis.com/gcm/send

    public AppServer(){}

    public void connect(){

    }

    public void send(final String token, final JSONArray registration_ids){

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url_down = new URL(" https://gcm-http.googleapis.com/gcm/send");
//                    URL url_noti = new URL("https://gcm-http.googleapis.com/gcm/notification");
                    HttpsURLConnection http = (HttpsURLConnection)url_down.openConnection();
                    http.setRequestProperty("Content-Type", "application/json");
                    http.setRequestProperty("Authorization", "key="+server_key);
                    http.setRequestMethod("POST");
                    http.setRequestProperty("project_id", sender_id);

                    http.setDoOutput(true);
                    http.setDoInput(true);
                    http.connect();

                    OutputStream output = http.getOutputStream();
                    try {
                        jsonObject.accumulate("operation", "create");
                        jsonObject.accumulate("notification_key_name", "appUser-jieun");
                        jsonObject.accumulate("registrations_ids", registration_ids);

                        // 내가 원하는 친구의 이름을 넣어 보낸다.
                        down.accumulate("to", "/topics/"+"jieun");  // 테스트용(추후 수정)
                        JSONObject message = new JSONObject();
                        message.put("message", "jieun");
                        down.put("data", message);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String json = jsonObject.toString();
                    String downstream = down.toString();
                    Log.i("notice", json);

                    output.write(downstream.getBytes("UTF-8"));
                    output.flush();
                    output.close();
                    Log.i("notice", "test http request post: "+http.getResponseCode());
                    Log.i("notice", "test http request message: "+http.getResponseMessage());

                    InputStream input = http.getInputStream();
                    byte[] buffer = new byte[1024];
                    while(input.available() > 0){
                        int readCount = input.read(buffer);
                        if(readCount>0){
                            String read = new String(buffer, 0, readCount);
                            Log.i("notice", read);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();

    }

    public void registerFriend(final String friend){
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url_down = new URL(" https://gcm-http.googleapis.com/gcm/send");
                    HttpsURLConnection http = (HttpsURLConnection)url_down.openConnection();
                    http.setRequestProperty("Content-Type", "application/json");
                    http.setRequestProperty("Authorization", "key="+server_key);
                    http.setRequestMethod("POST");
                    http.setRequestProperty("project_id", sender_id);

                    http.setDoOutput(true);
                    http.setDoInput(true);
                    http.connect();

                    OutputStream output = http.getOutputStream();
                    try {
                        // 친구가 jieun 이라 가정. 나중에 friend로 바꾼다.
                        down.accumulate("to", "/topics/"+friend);
                        JSONObject message = new JSONObject();
                        message.put("message", friend);
                        down.put("data", message);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String json = jsonObject.toString();
                    String downstream = down.toString();
                    Log.i("notice", json);

                    output.write(downstream.getBytes("UTF-8"));
                    output.flush();
                    output.close();
                    Log.i("notice", "test http request post: "+http.getResponseCode());
                    Log.i("notice", "test http request message: "+http.getResponseMessage());

                    InputStream input = http.getInputStream();
                    byte[] buffer = new byte[1024];
                    while(input.available() > 0){
                        int readCount = input.read(buffer);
                        if(readCount>0){
                            String read = new String(buffer, 0, readCount);
                            Log.i("notice", read);
                        }
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }

                return null;
            }
        }.execute();
    }


}


//            String json = "{\"operation\": "+"\"create\""+ ","+
//                    "\"notification_key_name\": "+"\"appUser-jieun\""+","+
//                    "\"registration_ids\": "+"[\""+token+"\"]}";

//            String json = "{\"to\": "+"\""+token+"\""+","+
//                    "\"data\": {\"message\": \"test http json request\"}}";