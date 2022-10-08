package com.faizan.onefitness.SessionData;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class LatLngs {
    private List<LatLng> coordinates; // hold the user's locations

    public LatLngs(List<LatLng> coord) {
        coordinates = coord;
    } // initialise locations

    public List<LatLng> getCoordinates() {
        return coordinates;
    } // return the locations

    public void setCoordinates(List<LatLng> coordinates) {
        this.coordinates = coordinates;
    } // set  new locations
}

