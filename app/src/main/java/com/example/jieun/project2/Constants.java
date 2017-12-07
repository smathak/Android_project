package com.example.jieun.project2;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jieun on 11/14/2017.
 */


public final class Constants {
    public static final HashMap<String, LatLng> POPUP_MESSAGE = new HashMap<String, LatLng>();  // POPUP을 띄울때 필요한 MESSAGE와 경도, 위도 정보를 포함함
    public static ArrayList<String> friendsList = new ArrayList<String>();  // 친구 목록
    public static String MY_NAME = null;    // 나의 이름 상수
    public static String MY_TOKEN = null;   // 나의 토큰 상수(GCM 을 위해 필요함)
}
