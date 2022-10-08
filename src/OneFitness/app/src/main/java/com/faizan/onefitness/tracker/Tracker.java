package com.faizan.onefitness.tracker;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.faizan.onefitness.MainFragment;
import com.faizan.onefitness.MainFragmentInterface;
import com.faizan.onefitness.MainActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public abstract class Tracker {
    protected Context context;    // get parent activities context to pass into methods
    protected MainFragmentInterface updateUIInterface;    // used to send update to the UI


    public Tracker(Context activity, MainFragment fragment) {
        this.context = activity; // initialise context
        this.updateUIInterface = fragment; // initialise interface
    }

    protected void subscribeToRecordingClient(final DataType dataType, final String TAG) {
        Fitness.getRecordingClient(context, GoogleSignIn.getLastSignedInAccount(context))
                .subscribe(dataType)                             // subscribe to record data type throughout the day
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i(TAG, "Successfully subscribed!");       // has completed successfully, so log message
                                    update();                                          // call the update method to retrieve daily data
                                } else {
                                    Log.w(TAG, "Problem subscribing", task.getException()); // has failed, so log message
                                }
                            }
                        });
    }

    abstract void subscribe();
    abstract void update();
}

