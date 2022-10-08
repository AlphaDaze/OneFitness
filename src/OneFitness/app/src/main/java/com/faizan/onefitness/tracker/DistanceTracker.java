package com.faizan.onefitness.tracker;

import android.util.Log;

import androidx.annotation.NonNull;

import com.faizan.onefitness.MainActivity;
import com.faizan.onefitness.MainFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class DistanceTracker extends Tracker {
    private static final String TAG = "MyFit-DistanceTracker";  // used for debugging

    public DistanceTracker(MainActivity activity, MainFragment fragment) {
        super(activity, fragment);
    }

    // method will subscribe to get a live counter for calories, the subscription will run even when the app is stopped
    public void subscribe() {
        subscribeToRecordingClient(DataType.TYPE_DISTANCE_CUMULATIVE, TAG);
    }


    // will retrieve daily calories expended
    void update() {
        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context)) // get the historyClient
                .readDailyTotal(DataType.TYPE_DISTANCE_DELTA)     // read daily calories using DISTANCE_DELTA
                .addOnSuccessListener(                         // listener executed when method succeeds
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {    // get the total distance on success
                                // get the float value for distance
                                float total = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_DISTANCE).asFloat();
                                updateUIInterface.sendDistance(total);
                                Log.i(TAG, "Total distance: " + total);    // log the calories
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {   // if it failed to retrieve then log a warning
                                Log.w(TAG, "Could not get the distance count", e);
                            }
                        });
    }
}
