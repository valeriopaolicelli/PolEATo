package com.mad.poleato;

import com.google.android.gms.maps.model.Marker;

import java.util.Comparator;

public class Rider {
    private String id;
    private Double distance;
    private Marker marker;
    private double latitude;
    private double longitude;

    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    public Rider(String id, double latitude, double longitude, double latitudeRest, double longitudeRest) {
        this.id = id;
        this.latitude= latitude;
        this.longitude= longitude;
        setDistance(latitudeRest, longitudeRest);
        marker= null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(double latitudeRest, double longitudeRest) {
        double startLat= latitudeRest;
        double endLat= this.latitude;

        double startLong= longitudeRest;
        double endLong= this.longitude;

        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        this.distance= EARTH_RADIUS * c; // <-- d
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }

    public static Comparator<Rider> distanceComparator= new Comparator<Rider>() {
        @Override
        public int compare(Rider r1, Rider r2) {
            return r1.getDistance().compareTo(r2.getDistance()) > 0 ? 1 :
                    r1.getDistance().equals(r2.getDistance()) ? 0 : -1;
        }
    };
}