package com.ne4istb.test.geofences;

import com.google.android.gms.location.Geofence;

import java.io.Serializable;

public class MyGeofence implements Serializable {

    private static final int ONE_MINUTE = 60000;

    private int id;
    private double latitude;
    private double longitude;
    private float radius;
    private int transitionType;

    public MyGeofence(int id, double latitude, double longitude, float radius, int transitionType) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.transitionType = transitionType;
    }

    public Geofence toGeofence() {
        return new Geofence.Builder()
                .setRequestId(String.valueOf(id))
                .setTransitionTypes(transitionType)
                .setCircularRegion(latitude, longitude, radius)
                .setExpirationDuration(ONE_MINUTE)
                .build();
    }
}
