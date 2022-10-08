package com.faizan.onefitness.session;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;

import com.faizan.onefitness.HelperClasses.AppExecutors;
import com.faizan.onefitness.R;
import com.faizan.onefitness.SessionData.SessionData;
import com.faizan.onefitness.SessionData.SessionDatabase;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public abstract class MySession implements Serializable {
    private static final String TAG = "MyFit-MySession";  // used for debugging
    final protected DecimalFormat df = new DecimalFormat("0.00"); // convert distance
    final protected SimpleDateFormat sdf = new SimpleDateFormat("mm:ss"); // convert time

    protected Context context;    // get parent activities context to pass into methods

    protected String sessionName;
    protected SessionFragmentInterface updateUIInterface;

    protected Session session; // tracking session data

    protected Date lastUpdateTime;    // hold the time from last api request
    protected Date newUpdateTime;     // hold the current time

    protected float totalDistance = 0; // hold total distance by accumulating it
    protected float totalCalories = 0; // hold the total calories

    private Location lastLocation; // holds user's previous location

    protected PolylineOptions track; // hold coordinates to draw

    // hold database and data to store
    private SessionDatabase mDb;
    protected SessionData sessionData;


    public MySession(Context activity, SessionFragment fragment, String name) {
        this.context = activity;
        this.updateUIInterface = fragment;
        this.sessionName = name; // get session type using name

        mDb = SessionDatabase.getInstance(context);
    }

    public void startSession() {
        String activityType = FitnessActivities.RUNNING; // set default activity to running
        // if session is cycling
        if (sessionName == "Cycling") {
            activityType = FitnessActivities.BIKING; // change session type to cycling
        }
        // get the current time
        Date startTime = Calendar.getInstance().getTime();
        session = new Session.Builder()
                .setName(sessionName) // set the name
                .setIdentifier(R.string.app_name + String.valueOf(startTime.getTime())) // set unique identifier using time
                .setDescription(sessionName + " Session") // set description of the session
                .setStartTime(startTime.getTime(), TimeUnit.MILLISECONDS) // set the start time
                .setActivity(activityType) // session is from current app
                .build();
        // start the session by registering it in the API
        Task<Void> response = Fitness.getSessionsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .startSession(session);

        Log.i(TAG, "Started " + sessionName + " Session");
        // reset the session to make sure previous session data is overwritten
        lastUpdateTime = startTime;
        totalCalories = 0;
        totalDistance = 0;

        // reset route
        lastLocation = null;
        track = new PolylineOptions();
        trackLocation();
    }

    public void stopSession() {
        updateSaveData();
        Task<List<com.google.android.gms.fitness.data.Session>> response = Fitness.getSessionsClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .stopSession(session.getIdentifier());
        Log.i(TAG, "Stopped " + sessionName + " Session");
        response.addOnSuccessListener(new OnSuccessListener<List<com.google.android.gms.fitness.data.Session>>() {
            @Override
            public void onSuccess(List<com.google.android.gms.fitness.data.Session> sessions) {
                Log.i(TAG, sessions.toString());
                Session session = sessions.get(0);

                updateSaveData();

                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.sessionDataDao().insertSessionData(sessionData);
                    }
                });

                updateUIInterface.startSummaryFragment();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, e.toString());
                    }
                });
    }

    protected abstract void updateSaveData();

    public abstract void updateData();

    protected void getBaseData(DataReadRequest readRequest) {
        trackLocation();
        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)) // get the historyClient
                .readData(readRequest)     // read distance covered since session was started
                .addOnSuccessListener(                         // listener executed when method succeeds
                        new OnSuccessListener<DataReadResponse>() {

                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                // get the calories data from the response from the API
                                DataSet caloriesData = dataReadResponse.getDataSet(DataType.TYPE_CALORIES_EXPENDED);
                                // get the calories covered as a float. If it's empty get the value 0
                                float calories = caloriesData.isEmpty() ? 0 : caloriesData.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();

                                totalCalories += calories; // update the total calories
                                updateUIInterface.sendCalories(); // update UI with total calories

                                updateCustomData(dataReadResponse);

                                Log.i(TAG, "Current Calories response " +  calories);    // log the calories
                                Log.i(TAG, "Total Calories " +  totalCalories);    // log the total calories

                            }
                        })
                .addOnFailureListener( // when method fails
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i(TAG, "Error: " +  e); // show error
                            }
                        });


    }

    protected abstract void updateCustomData(DataReadResponse dataReadResponse);

    private void trackLocation() {
        // client run in the background to not slow down GUI when getting location
        FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        // start background task
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        // validation: wait for task to complete before moving camera
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // update route and set new lastLocation
                updateDistance(location);
                lastLocation = location; // update last location

                // get coordinates of the current location and add it to route
                LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());
                track.add(coordinates);

                Log.i(TAG, "Location Tracked: " + coordinates.toString());
            }
        });
    }

    private void updateDistance(final Location newLocation) {
        float[] result = new float[1]; // used to hold the distance result
        // make sure the new location and old location are valid
        if (newLocation != null && lastLocation != null) {
            // get the distance between the two and store is result
            Location.distanceBetween(newLocation.getLatitude(), newLocation.getLongitude(),
                                    lastLocation.getLatitude(), lastLocation.getLongitude(), result);
        }

        totalDistance += result[0]; // update total distance by adding the current distance
        updateUIInterface.sendDistance(); // update UI with total distance

        Log.i(TAG, "new distance " + result[0]); // log for dubug
        Log.i(TAG, "Total Distance " +  totalDistance); // log the total distance
    }


    public String getDistance() {
        return df.format(totalDistance / 1000); // return distance
    }

    public String getCalories() {
        return String.valueOf(Math.round(totalCalories)); // return calories whole number
    }

    public abstract String getSpeed();

    public PolylineOptions getTrack() {
        return track;
    }

    public String getName() {
        if (session != null) { // make sure a session has started a
            return session.getName();
        }
        return "";
    }

    public long getStartTime() {
        if (session != null) { // make sure a session has started
            return session.getStartTime(TimeUnit.MILLISECONDS);
        }
        return -1;
    }

    public long getEndTime() {
        if (session != null) { // make sure a session has started
            // get end time from session
            long endTime = session.getEndTime(TimeUnit.MILLISECONDS);

            // if session is just ending
            if (endTime == 0) {
                // set the end time to the current time
                endTime = System.currentTimeMillis();
            }

            return endTime;
        }
        return -1;
    }

    public String getActiveTime() {
        // get the time the session has been running for
        long timeDiff = this.getEndTime() - this.getStartTime();
        // convert this time to the correct format of mm:ss
        String time = String.format(Locale.UK, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(timeDiff),
                TimeUnit.MILLISECONDS.toSeconds(timeDiff) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeDiff))
        );
        return time;
    }


    public SessionData getSessionData() {
        return sessionData;
    }
}
