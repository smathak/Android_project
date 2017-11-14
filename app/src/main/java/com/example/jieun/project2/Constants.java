package com.example.jieun.project2;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jieun on 11/14/2017.
 */


public final class Constants {
    public static final HashMap<String, THINGS> POPUP_MESSAGE = new HashMap<String, THINGS>();

    public void addPopupMessage(String title, String content, Double lat, Double lng, String featureName){
        THINGS thing = new THINGS(title, content, lat, lng);
        POPUP_MESSAGE.put(featureName, thing);
    }
}
