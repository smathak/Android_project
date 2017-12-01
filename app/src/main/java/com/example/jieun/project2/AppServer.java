package com.example.jieun.project2;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by jieun on 11/27/2017.
 * GCM을 하기 위하여 https://gcm-http.googleapis.com/gcm/send 와 http 통신을 하는 클래스이다.
 */

public class AppServer {

    String server_key = ""; // Server key(For app Server in Firebase)
    String sender_id = "";
    JSONObject jsonObject = new JSONObject();
    JSONObject down = new JSONObject();
    public static String token;
    // GCM Server HTTP 방식
    //        https://android.googleapis.com/gcm/notification
    //        https://gcm-http.googleapis.com/gcm/send

    public AppServer(){}

    public void connect(){

    }
    public void setToken(String t){
        this.token = t;
    }
    public String getToken(){
        return this.token;
    }

    // 친구에게 Message를 보내주는 함수(AsyncTask로 선언하였다.)
    public void sendAcceptMessage(final String myname, final String friendToken){

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
                        //                        jsonObject.accumulate("operation", "create");
                        //                        jsonObject.accumulate("notification_key_name", "appUser-jieun");
                        //                        jsonObject.accumulate("registrations_ids", registration_ids);

                        down.accumulate("to", friendToken);  // 친구 토큰을 이용하여 친구에게 보냄
                        JSONObject message = new JSONObject();
                        message.put("name", Constants.MY_NAME);    // 내 이름(나의 이름을 밝힘)
                        message.put("token", Constants.MY_TOKEN);   // 나의 토큰도 전달해야지 친구가 나에게도 메세지를 보낼 수 있음
                        message.put("message", "accepted your request"); // 수락 했다는 안내 메세지를 전달
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

    // 친구를 등록할 때 사용하는 함수
    public void registerFriend(final String message){
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
                        down.accumulate("to", "/topics/"+message);  // message는 친구 이름
                        JSONObject message = new JSONObject();
                        message.put("name", Constants.MY_NAME); // 나의 이름
                        message.put("token", getToken());   // 나의 토큰
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


    // 친구에게 메세지를 보내되 시간도 함께 보낼 때 사용하는 함수
    public void sendMarkerWithTime(final String myname, final String friendName, final String friendToken,
                                   final String title, final String content, final Double lat, final Double lng,
                                   final int year, final int month, final int day, final int hour, final int minute){
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
                        down.accumulate("to", friendToken);
                        JSONObject message = new JSONObject();
                        message.put("sender_name", myname); // 보낸 사람 이름
                        message.put("title", title);
                        message.put("content", content);
                        Log.i("notice", "com.example.jieun.project2.ThingsToDo.AppServer: "+lat);
                        message.put("latitude", String.valueOf(lat));
                        message.put("longitude", String.valueOf(lng));
                        message.put("category", "marker");
                        message.put("year", String.valueOf(year));
                        message.put("month", String.valueOf(month));
                        message.put("day", String.valueOf(day));
                        message.put("hour", String.valueOf(hour));
                        message.put("minute", String.valueOf(minute));
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
