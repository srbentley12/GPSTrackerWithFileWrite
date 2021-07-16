package com.example.GpsTrackerWithFileWrite;

public class SubSample {

    String lon;
    String lat;
    String time;

    public SubSample(String lon, String lat, String time) {
        this.lon = lon;
        this.lat = lat;
        this.time = time;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
