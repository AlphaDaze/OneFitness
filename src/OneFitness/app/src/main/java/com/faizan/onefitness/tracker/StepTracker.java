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

public class StepTracker extends Tracker {
    private static final String TAG = "MyFit-StepTracker";  // used for debugging

    public StepTracker(MainActivity activity, MainFragment fragment) {
        super(activity, fragment);
    }

    // method will subscribe to get a live counter for steps, the subscription will run even when the app is stopped
    public void subscribe() {
        subscribeToRecordingClient(DataType.TYPE_STEP_COUNT_CUMULATIVE, TAG);
    }

    // will retrieve daily steps
    void update() {
        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)    // read daily steps using COUNT_DELTA
                .addOnSuccessListener(                         // listener executed when method succeeds
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {    // get the total on success
                                long total = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();

                                updateUIInterface.sendSteps(total);        // send total steps to MainActivity, to display
                                Log.i(TAG, "Total steps: " + total);    // log the steps
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {   // if it failed to retrieve then log a warning
                                Log.w(TAG, "Could not get the step count", e);
                            }
                        });
    }
}


