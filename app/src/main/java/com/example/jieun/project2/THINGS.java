package com.example.jieun.project2;

/**
 * Created by jieun on 11/14/2017.
 */

public class THINGS {
    String title;
    String content;
    Double lat;
    Double lng;
    public THINGS(String ti, String co, Double la, Double ln) {
        this.title = ti;
        this.content = co;
        this.lat = la;
        this.lng = ln;
    }
    public String getTitle(){
        return this.title;
    }
    public String getContent(){
        return this.content;
    }
    public Double lat(){
        return this.lat;
    }
    public Double lng(){
        return this.lng;
    }
}
