package com.faizan.onefitness.session;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.faizan.onefitness.R;
import com.faizan.onefitness.SessionData.LatLngs;
import com.faizan.onefitness.SessionData.SessionData;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessActivities;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Session;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.Cap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RunningSession extends MySession {
    private static final String TAG = "MyFit-RunningSession";  // used for debugging

    private float pace = 0;          // hold the pace
    private int totalSteps = 0; // hold the total steps


    public RunningSession(Context activity, SessionFragment fragment) {
        super(activity, fragment, "Running");
    }

    @Override
    protected void updateSaveData() {
        sessionData = new SessionData(getName(), getStartTime(), getEndTime(), getActiveTime(), getDistance(), getCalories(), getSpeed(), new LatLngs(getTrack().getPoints()), getSteps());
    }

    @Override
    public void updateData() {
        // get the current time
        newUpdateTime = Calendar.getInstance().getTime();

        // make a read request to get get the distance between the two time intervals
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .setTimeRange(lastUpdateTime.getTime(), newUpdateTime.getTime(), TimeUnit.MILLISECONDS)
                .read(DataType.TYPE_CALORIES_EXPENDED)
                .read(DataType.TYPE_STEP_COUNT_DELTA)
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
            // calculate the speed in m/s by dividing total distance by the total time
            float speed = totalDistance / (time / 1000);
            // calculate the pace by dividing 16.66 by the speed(m/s)
            pace = 16.66666666666f / speed;
            updateUIInterface.sendSpeed(); // update UI
        }

        Log.i(TAG, "Pace response " +  pace);    // log pace

        // get data for steps
        DataSet stepsData = dataReadResponse.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
        // retrieve steps value from data set
        long steps = stepsData.isEmpty() ? 0 : stepsData.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
        totalSteps += steps; // add to total
        Log.i(TAG, "Steps " +  steps);    // log the steps
    }

    public String getSpeed() {
        // make sure pace is less than an hour
        if (pace < 60) {
            // convert the pace to milliseconds
            long timeInMilliSeconds = (long) Math.floor(pace * 60 * 1000);
            // create a date with the time in ms
            Date date = new Date(timeInMilliSeconds);
            // return pace in correct format mm:ss
            return sdf.format(date);
        } else {
            // if more than an hour then return pace as 0
            return "00:00";
        }
    }

    public String getSteps() {
        return String.valueOf(totalSteps);
    }
}
