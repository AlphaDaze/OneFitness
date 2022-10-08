package com.faizan.onefitness.session;

import android.content.Context;
import android.util.Log;

import com.faizan.onefitness.SessionData.LatLngs;
import com.faizan.onefitness.SessionData.SessionData;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CyclingSession extends MySession {
    private static final String TAG = "MyFit-CyclingSession";  // used for debugging

    private float speed = 0;          // hold the pace


    public CyclingSession(Context activity, SessionFragment fragment) {
        super(activity, fragment, "Cycling");
    }


    @Override
    protected void updateSaveData() {
        sessionData = new SessionData(getName(), getStartTime(), getEndTime(), getActiveTime(), getDistance(), getCalories(), getSpeed(), new LatLngs(getTrack().getPoints()));
    }

    @Override
    public void updateData() {
        // get the current time
        newUpdateTime = Calendar.getInstance().getTime();

        // make a read request to get get the distance between the two time intervals
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .setTimeRange(lastUpdateTime.getTime(), newUpdateTime.getTime(), TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .build();

        // update the last update to current time
        lastUpdateTime = newUpdateTime;


        getBaseData(readRequest);
    }

    @Override
    protected void updateCustomData(DataReadResponse dataReadResponse) {
        // make sure distance is not 0 so we don't have 0 in a division
        if (totalDistance > 0) {
            // get the time since the session has started
            long time = newUpdateTime.getTime() - session.getStartTime(TimeUnit.MILLISECONDS);

            // calculate the speed by dividing total distance in km by the total time in hours
            float timeInHours = ((float) time) / 1000f / 3600f;
            float distanceInKm = totalDistance / 1000f;
            speed = distanceInKm / timeInHours;

            updateUIInterface.sendSpeed(); // update UI
        }

        Log.i(TAG, "Speed response " +  speed);    // log the speed
    }

    public String getSpeed() {
        return String.valueOf(Math.round(speed)); // return speed whole number
    }

}
