package com.example.tsp;

import com.google.android.gms.maps.model.LatLng;

public class Lugar {
    private String name;
    private double latitude;
    private double longitude;
    private LatLng latLng;
    private boolean origen;

    public Lugar() {
    }

    public Lugar(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public boolean isOrigen() {
        return origen;
    }

    public void setOrigen(boolean origen) {
        this.origen = origen;
    }
}
