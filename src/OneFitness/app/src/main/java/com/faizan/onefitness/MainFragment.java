package com.faizan.onefitness;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.faizan.onefitness.profile.UserProfile;
import com.faizan.onefitness.session.SessionFragment;

import java.math.RoundingMode;
import java.text.DecimalFormat;


public class MainFragment extends Fragment implements MainFragmentInterface {
    private static final String TAG = "MyFit-MainFragment";
    private static final String STEPS_TAG = "STEPS";
    private static final String CALORIES_TAG = "CALORIES";
    private static final String DISTANCE_TAG = "DISTANCE";

    final private DecimalFormat df = new DecimalFormat("0.00");

    private TextView stepsCountText;   // used to update steps counter on UI
    private TextView caloriesCountText; // used to update calories counter on UI
    private TextView distanceCountText; // used to update distance counter on UI

    private ImageButton sessionButton; // used to start the session fragment

    private ProgressBar stepsBar; // used to show progress of goal
    private ProgressBar caloriesBar; // used to show progress of goal
    // store the goals set in the UserProfile
    private int stepsGoal = 0;
    private int caloriesGoal = 0;

    private SharedPreferences sharedPrefs; // used to save and retrieve values

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // inflate the class with the xml view and get a reference to that view
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        getActivity().setTitle("OneFitness"); // set title
        // get UI widgets
        stepsCountText = (TextView) view.findViewById(R.id.stepsCnt);    // assigned UI element stepsCnt
        caloriesCountText = (TextView) view.findViewById(R.id.caloriesCnt); // assign UI element caloriesCnt
        distanceCountText = (TextView) view.findViewById(R.id.distanceCnt); // assing UI element distanceCnt

        sessionButton = (ImageButton) view.findViewById(R.id.sessionButton); // assign the button to this element

        stepsBar = (ProgressBar) view.findViewById(R.id.stepsProgress);
        caloriesBar = (ProgressBar) view.findViewById(R.id.caloriesProgress);

        // start the session fragment when button is clicked
        sessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                        .replace(R.id.fragment_container, new SessionFragment(), "session") // replace the current fragment
                        .addToBackStack(null);  // add this fragment to the stack
                // remove the navigation bar
                ((MainActivity)getActivity()).setNavigationVisibility(false);
                fragmentTransaction.commit(); // execute the fragment call created above
            }
        });



        // show the navigation menu if not shown
        ((MainActivity)getActivity()).setNavigationVisibility(true);

        restoreValues(); // set the UI with saved values

        // required to return the fragment when called by activity
        return view;
    }


    private void restoreValues() {
        // initialise sharedPrefs using my apps name and using the private context
        sharedPrefs = getActivity().getSharedPreferences("com.faizan.myfitness", Context.MODE_PRIVATE);

        // retrieve the values, if they don't exist get -1
        long steps = sharedPrefs.getLong(STEPS_TAG, 0);
        int cals = sharedPrefs.getInt(CALORIES_TAG, 0);
        float distance = sharedPrefs.getFloat(DISTANCE_TAG, 0);

        if (steps != 0) { // only set the value if it is saved
            //stepsCountText.setText(String.valueOf(steps));
            stepsCountText.setText(String.valueOf(steps));  // update UI element with total
            Log.i(TAG, "Steps restored");
        }
        if (cals != 0) { // only set the value if it is saved
            caloriesCountText.setText(String.valueOf(cals)); // update UI element with total
            Log.i(TAG, "calories restored");
        }
        if (distance != 0) { // only set the value if it is saved
            distanceCountText.setText(df.format(distance / 1000)); // update UI element with total
            Log.i(TAG, "Distnace restored");
        }

        getGoals(); // get goals set by the user
        updateStepsGoal(steps); // update the progress bar for steps
        updateCaloriesGoal(cals); // update the progress bar for calories
    }


    @Override
    public void sendSteps(long totalSteps) {
        stepsCountText.setText(String.valueOf(totalSteps));  // update UI element with total
        sharedPrefs.edit().putLong(STEPS_TAG, totalSteps).apply(); // save the steps
        updateStepsGoal(totalSteps); // update progress bar
    }

    @Override
    public void sendCalories(int totalCalories) {
        caloriesCountText.setText(String.valueOf(totalCalories)); // update UI element with total
        sharedPrefs.edit().putInt(CALORIES_TAG, totalCalories).apply(); // save the calories
        updateCaloriesGoal(totalCalories); // update progress bar
    }

    @Override
    public void sendDistance(float distance) {
        distanceCountText.setText(df.format(distance / 1000)); // update UI element with total
        sharedPrefs.edit().putFloat(DISTANCE_TAG, distance).apply(); // save the distance
    }



    private void getGoals() {
        // retrieve data for steps and calories if stored or 0
        stepsGoal = sharedPrefs.getInt(UserProfile.STEPS_GOAL_TAG, 0);
        caloriesGoal = sharedPrefs.getInt(UserProfile.CALORIES_GOAL_TAG, 0);
    }

    private void updateStepsGoal(final long totalSteps) {
        // update the step's progress
        updateGoalProgress(stepsBar, totalSteps, stepsGoal);
    }


    private void updateCaloriesGoal(final int totalCalories) {
        // update the calorie's progress
        updateGoalProgress(caloriesBar, totalCalories, caloriesGoal);
    }

    private void updateGoalProgress(final ProgressBar bar, final double total, final double goal) {
        // run this in a background thread so the UI is updated when it runs
        // post() gets a handler to run a new thread for the progressBar
        bar.post(new Runnable() {
            @Override
            public void run() {
                // make sure we don't divide by 0
                if (total != 0 || goal != 0) {
                    // get progress from 0 to 100
                    int progress = (int) (((double) total) / ((double) goal) * 100);

                    if (progress < 100) { // if progress is in range
                        bar.setProgress(progress);
                    } else { // else set to max
                        bar.setProgress(100);
                    }
                } else { // set progress to 0 if any values are 0
                    bar.setProgress(0);
                }
            }
        });
    }
}
