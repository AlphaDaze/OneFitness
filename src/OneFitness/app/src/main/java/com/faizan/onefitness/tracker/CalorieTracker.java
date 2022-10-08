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



public class CalorieTracker extends Tracker {
    private static final String TAG = "MyFit-CalorieTracker";  // used for debugging

    public CalorieTracker(MainActivity activity, MainFragment fragment) {
        super(activity, fragment);
    }

    // method will subscribe to get a live counter for calories, the subscription will run even when the app is stopped
    public void subscribe() {
        subscribeToRecordingClient(DataType.TYPE_CALORIES_EXPENDED, TAG);
    }


    // will retrieve daily calories expended
    void update() {
        Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED)     // read daily calories using CALORIES_EXPENDED
                .addOnSuccessListener(                         // listener executed when method succeeds
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {    // get the total on success
                                // get the float value and then cast it to an int
                                int total = dataSet.isEmpty() ? 0 : (int) dataSet.getDataPoints().get(0).getValue(Field.FIELD_CALORIES).asFloat();

                                updateUIInterface.sendCalories(total);
                                Log.i(TAG, "Total calories: " + total);    // log the calories
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {   // if it failed to retrieve then log a warning
                                Log.w(TAG, "Could not get the calorie count", e);
                            }
                        });
    }
}

