package com.faizan.onefitness;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Toast;

import com.faizan.onefitness.journal.JournalFragment;
import com.faizan.onefitness.profile.UserProfile;
import com.faizan.onefitness.session.SessionFragment;
import com.faizan.onefitness.tracker.CalorieTracker;
import com.faizan.onefitness.tracker.DistanceTracker;
import com.faizan.onefitness.tracker.StepTracker;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MyFit-MainActivity";
    private static final int REQUEST_CODE_TRACKER = 4444;
    private static final int REQUEST_SETTINGS_CODE = 4712;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 4541;
    private static final int MY_PERMISSIONS_REQUEST_FILE_ACCESS = 2494;
    private static final int MY_PERMISSIONS_REQUEST_ACTIVITY = 5551;

    private BottomNavigationView navigation; // update UI element

    private StepTracker stepTracker;   // used to make subscription and read steps
    private CalorieTracker calorieTracker; // and calories
    private DistanceTracker distanceTracker; // and distance

    MainFragment mainFragment;   // hold reference to mainFragment

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                         // create superclass' activity(basic)
        setContentView(R.layout.activity_main);                     // set layout for this activity

        // get UI element and assign it
        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        // set the listener for when the item selected is updated to this class
        navigation.setOnNavigationItemSelectedListener(this);

        mainFragment = (MainFragment) new MainFragment(); // assign a new fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mainFragment);
        fragmentTransaction.commit();

        stepTracker = new StepTracker(this, mainFragment);                // create an object of StepTracker
        calorieTracker = new CalorieTracker(this, mainFragment);          // and CalorieTracker
        distanceTracker = new DistanceTracker(this, mainFragment);        // and DistanceTracker


        // type of data we want to request, both read and write
        FitnessOptions fitnessOptions = FitnessOptions.builder().addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                                                .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
                                                                .addDataType(DataType.TYPE_CALORIES_EXPENDED)
                                                                .addDataType(DataType.TYPE_DISTANCE_CUMULATIVE)
                                                                .addDataType(DataType.TYPE_DISTANCE_DELTA)

                                                                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT)
                                                                .addDataType(DataType.TYPE_LOCATION_SAMPLE)
                                                                .addDataType(DataType.TYPE_SPEED)
                                                                .addDataType(DataType.TYPE_POWER_SAMPLE)
                                                                .addDataType(DataType.TYPE_MOVE_MINUTES)
                                                                .addDataType(DataType.TYPE_LOCATION_TRACK)
                                                                .addDataType(DataType.TYPE_WORKOUT_EXERCISE)
                                                                .addDataType(DataType.TYPE_CYCLING_PEDALING_CADENCE)
                                                                .addDataType(DataType.TYPE_CYCLING_PEDALING_CUMULATIVE)
                                                                .addDataType(DataType.TYPE_CYCLING_WHEEL_REVOLUTION)
                                                                .addDataType(DataType.TYPE_CYCLING_WHEEL_RPM)
                                                                .addDataType(DataType.TYPE_WEIGHT)
                                                                .addDataType(DataType.TYPE_HEIGHT)


                                                                .addDataType(DataType.TYPE_ACTIVITY_SEGMENT, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_LOCATION_SAMPLE, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_SPEED, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_POWER_SAMPLE, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_MOVE_MINUTES, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_LOCATION_TRACK, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_CYCLING_PEDALING_CADENCE, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_CYCLING_PEDALING_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_CYCLING_WHEEL_REVOLUTION, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_CYCLING_WHEEL_RPM, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_WEIGHT, FitnessOptions.ACCESS_WRITE)
                                                                .addDataType(DataType.TYPE_HEIGHT, FitnessOptions.ACCESS_READ)

                .build();

        // get info for user's profile data
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .addExtension(fitnessOptions)
                .build();
        // get permission from user's sign in
        GoogleSignInClient signIn = GoogleSignIn.getClient(this, gso);
        // start the activity to sign into google
        Intent signInIntent = signIn.getSignInIntent();
        startActivityForResult(signInIntent, REQUEST_CODE_TRACKER);

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            // request permission to data if we don't have access
            GoogleSignIn.requestPermissions(this, REQUEST_CODE_TRACKER,
                                                    GoogleSignIn.getLastSignedInAccount(this),
                                                    fitnessOptions);
            Log.i(TAG, "Requested user permissions");
        } else {
            grantFilePermissions();         // request location permission if it has not been granted
        }
    }

    // run after an another activity closes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_TRACKER:              // if the log-in activity finishes
                Log.i(TAG, String.valueOf(resultCode));
                if (resultCode == Activity.RESULT_OK || resultCode == Activity.RESULT_FIRST_USER) {
                    grantFilePermissions();         // request location permission if it has not been granted
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // show message to user to grant permissions on restart
                    Toast.makeText(this, "Error: Restart app to grant permissions", Toast.LENGTH_LONG).show();
                }
                break;
            case REQUEST_SETTINGS_CODE:                 // if settings activity finishes
                grantFilePermissions();              // check for the permissions again
                break;
        }
    }


    // called when the permission dialogue returns with a result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FILE_ACCESS:
                // if permission has not been granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    grantLocationPerms(); // get location permissions next
                } else {
                    // ask the user to grant permissions manually
                    manualPermissionDialog(R.string.manual_permission_files);
                }
                break;
            case MY_PERMISSIONS_REQUEST_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    distanceTracker.subscribe();
                    grantActivityPerms();
                } else {
                    // permission was denied, ask to manually set it
                    manualPermissionDialog(R.string.manual_permission_location);
                }
                break;
            case MY_PERMISSIONS_REQUEST_ACTIVITY:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    calorieTracker.subscribe();
                    stepTracker.subscribe();
                } else {
                    // permission was denied, ask to manually set it
                    manualPermissionDialog(R.string.manual_permission_location);
                }
                break;

        }
    }

    private void grantFilePermissions() {
        // check if we already have file permission first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                manualPermissionDialog(R.string.manual_permission_files);
            } else {
                // No explanation needed; request the file permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_FILE_ACCESS);
            }
        } else {
            // make sure to get location permissions next
            grantLocationPerms();
        }

    }

    private void grantLocationPerms() {

        // check if we already have location permission first
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                manualPermissionDialog(R.string.manual_permission_location);
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            // Permission has already been granted
            distanceTracker.subscribe();
            grantActivityPerms();
        }
    }


    private void grantActivityPerms() {
        // check if we need to request the permission as it's only required after API 28
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            // check if we already have permissions
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                // have permissions been denied last time
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACTIVITY_RECOGNITION)) {
                    manualPermissionDialog(R.string.manual_permission_location);
                } else {
                    // No explanation needed; request the permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                            MY_PERMISSIONS_REQUEST_ACTIVITY);
                }
            } else {
                // retrieve data if we do have permissions
                calorieTracker.subscribe();
                stepTracker.subscribe();
            }
        } else {
            // retrieve data if we have a previous version of android
            calorieTracker.subscribe();
            stepTracker.subscribe();
        }
    }

    // create a dialogue to manually get permission from settings
    private void manualPermissionDialog(final int message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.manual_permission_title); // set dialogue title
        alertDialogBuilder
                .setMessage(message) // set the message
                .setCancelable(false)                   // can't be ignored
                .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {  // run when the SETTINGS button is clicked
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS); // create an intent to run settings
                        Uri uri = Uri.fromParts("package", getPackageName(), null); // with this apps page open
                        intent.setData(uri);                                                        // set the uri(settings page)
                        startActivityForResult(intent, REQUEST_SETTINGS_CODE);   // start the activity and handle result
                    }
                })
                .setNegativeButton("LATER", new DialogInterface.OnClickListener() {    // if LATER button is clicked
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();                                                    // cancel the dialogue
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create(); // create the dialogue
        alertDialog.show();                                    // activate it
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            // if the home was selected
            case R.id.navigation_home:
                // change to main fragment
                fragment = mainFragment;
                break;
            case R.id.navigation_journal:
                // change to new journal fragment
                fragment = new JournalFragment();
                break;

            case R.id.navigation_profile:
                // change to profile fragment
                fragment = new UserProfile();
                break;
        }
        // start the new fragment
        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            // create the fragment view by replacing it onto the FrameLayout
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.expand_in, R.anim.shrink_in, R.anim.expand_in, R.anim.shrink_in)
                    .replace(R.id.fragment_container, fragment);
            fragmentTransaction.commit();
            // fragment loaded successfully
            return true;
        }
        // fragment passed was null
        return false;
    }

    public void setNavigationVisibility(boolean visible) {
        if (navigation.isShown() && !visible) {
            // remove the navigation bar
            navigation.setVisibility(View.GONE);
        }
        else if (!navigation.isShown() && visible){
            // show the navigation bar
            navigation.setVisibility(View.VISIBLE);
        }
    }
}