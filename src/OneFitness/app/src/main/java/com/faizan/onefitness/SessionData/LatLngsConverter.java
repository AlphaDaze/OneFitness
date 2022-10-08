package com.faizan.onefitness.SessionData;


import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LatLngsConverter {

    @TypeConverter
    public LatLngs toLatLngs(String coord) {
        // will hold all the coordinates
        List<LatLng> latlngs = new ArrayList<>();

        // split the string into seperate locations first
        List<String> coordinates = Arrays.asList(coord.split("\\s*:\\s*"));
        for (String crd : coordinates) {
            // split locations into latitude and longitude
            List<String> location = Arrays.asList(crd.split("\\s*,\\s*"));
            // add new location to list
            latlngs.add(new LatLng(Double.valueOf(location.get(0)), Double.valueOf(location.get(1))));
        }
        // return the locations in the LatLngs data type
        return new LatLngs(latlngs);
    }

    @TypeConverter
    public String storeCoordinates(LatLngs coordinate) {
        String value = ""; // holds the string value for the locations

        for (LatLng crd : coordinate.getCoordinates()) {
            // get the latitide
            value += String.valueOf(crd.latitude);
            // seperate lat and long by comma
            value += ",";
            value += String.valueOf(crd.longitude);
            // seperate each locatoin by colon
            value += ":";
        }
        return value; // return the whole data as string
    }
}
