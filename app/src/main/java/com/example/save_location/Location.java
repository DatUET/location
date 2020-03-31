package com.example.save_location;

public class Location {
    private String time;
    private double longitude, latitude;

    public Location(String time, double longitude, double latitude) {
        this.time = time;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
