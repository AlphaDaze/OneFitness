package com.faizan.onefitness.session;

import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import android.os.CountDownTimer;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Spinner;
import android.widget.TextView;

import com.faizan.onefitness.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class SessionFragment extends Fragment implements OnMapReadyCallback, SessionFragmentInterface {
    private static final String TAG = "MyFit-SessionFragment";

    // for updating maps view
    private MapView mapView;
    private GoogleMap map;
    private FusedLocationProviderClient locProvider;
    private LocationRequest locRequest;
    private LocationCallback locCallback;



    private MySession session; // track running
    private String sessionName;

    private Spinner sessionType;    // used to get type of session
    private Button startButton;     // used to set behaviour of button
    private TextView countDownText; // used to show a counter to the user

    private ConstraintLayout trackingView; // layout to hold all tracking
    private TextView titleText;             // set title
    private Chronometer timer;              // show timer to user
    private TextView distanceText;          // show distance to user
    private TextView speedText;              // show pace to user
    private TextView caloriesText;          // show calories to user
    private Button stopButton;              // allows to stop session
    private TextView speedLabel;            // update unit to speed or pace
    private TextView constSpeed;            // update label to speed or pace

    ToneGenerator beepTone; // to play sounds


    public SessionFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new RunningSession(getContext(), this);
        locProvider = LocationServices.getFusedLocationProviderClient(getActivity()); // get location provider
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session, container, false);

        getActivity().setTitle("Session"); // set title

        mapView = (MapView) view.findViewById(R.id.mapView); // init mapView fragment
        mapView.onCreate(savedInstanceState);
        mapView.onResume();                                 // if there is a previous state
        mapView.getMapAsync(this);


        // assign UI items from view
        sessionType = (Spinner) view.findViewById(R.id.sessionType);
        startButton = (Button) view.findViewById(R.id.startButton);
        countDownText = (TextView) view.findViewById(R.id.countDownText);

        trackingView = (ConstraintLayout) view.findViewById(R.id.tracking_view);
        titleText = (TextView) view.findViewById(R.id.titleText);
        timer = (Chronometer) view.findViewById(R.id.chronoTimer); // initiate a chronometer
        distanceText = (TextView) view.findViewById(R.id.distanceText);
        speedText = (TextView) view.findViewById(R.id.speedText);
        caloriesText = (TextView) view.findViewById(R.id.calText);
        stopButton = (Button) view.findViewById(R.id.stopButton);
        speedLabel = (TextView) view.findViewById(R.id.constSpeedLbl);
        constSpeed = (TextView) view.findViewById(R.id.constSpeedText);

        // will be used to play sound for the count down
        beepTone = new ToneGenerator(AudioManager.STREAM_MUSIC, 65);

        sessionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sessionName = parent.getItemAtPosition(position).toString();

                if (sessionName.equals("Cycling")) {
                    updateSessionTexts("0", "km/h", "Speed");

                    session = new CyclingSession(getContext(), SessionFragment.this);
                } else {
                    updateSessionTexts("00:00", "/km", "Pace");
                    session = new RunningSession(getContext(), SessionFragment.this);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                
            }
        });

        // create a listener which will be called when the button is clicked
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startButton.setVisibility(View.INVISIBLE);  // if its not been started then hide the button
                sessionType.setVisibility(View.GONE);
                countDownText.setVisibility(View.VISIBLE); // and begin the count down
                startSession(); // start the session as well
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.stopSession();               // stop tracking session
                timer.stop();                               // stop timer
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (locProvider != null) {
            // set current location on the map and zoom in - update every 2 seconds
            requestLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (locProvider != null) {
            // stop tracking location when fragment is not being shown
            locProvider.removeLocationUpdates(locCallback);
        }
    }

    public void startSummaryFragment() {
        trackingView.setVisibility(View.INVISIBLE); // hide the tracking layout
        startButton.setVisibility(View.VISIBLE);    // show the start button
        sessionType.setVisibility(View.VISIBLE);

        Bundle args = new Bundle();
        args.putSerializable(SessionSummaryFragment.ARG_SESSION, session.getSessionData());
        //args.putString(SessionSummaryFragment.ARG_DISTANCE, session.getDistance());
        SessionSummaryFragment fragment = new SessionSummaryFragment();
        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                .replace(R.id.fragment_container, fragment, "summary") // replace the current fragment
                .addToBackStack(null);  // add this fragment to the stack

        fragmentTransaction.commit(); // execute the fragment call created above
    }


    private void requestLocationUpdates() {
        locRequest = LocationRequest.create();
        // create request for high priority
        locRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locRequest.setInterval(5 * 1000); // get location atleast every 20 seconds

        locCallback = new LocationCallback() { // called when location is retrieved
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult == null) { // if there is no location then do nothing
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // make sure location is retrieved
                    if (location != null) {
                        // get coordinates of the current location
                        LatLng coordinates = new LatLng(location.getLatitude(), location.getLongitude());

                        moveCamera(coordinates); // move camera to centre the user

                        Log.i(TAG, "Updated Location " + coordinates.toString());
                    }
                }
            }
        };

        locProvider.requestLocationUpdates(locRequest, locCallback, Looper.myLooper());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.setMyLocationEnabled(true);
    }

    private void moveCamera(LatLng coordinates) {
        // animate the camera to move to the camera smoothly
        map.animateCamera(CameraUpdateFactory.newLatLng(coordinates));
        // zoom in with level 15
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, 15));
    }


    @Override
    public void sendDistance() {
        distanceText.setText(session.getDistance()); // update UI element with total
    }

    @Override
    public void sendSpeed() {
        speedText.setText(session.getSpeed()); // update the speed/pace
    }

    @Override
    public void sendCalories() {
        caloriesText.setText(session.getCalories()); // update UI element with total
    }


    private void startSession() {
        // start a timer for 4 seconds with updates every second
        new CountDownTimer(3500, 1000) {
            // this method will be called every second
            @Override
            public void onTick(long millisUntilFinished) {
                beepTone.startTone(ToneGenerator.TONE_CDMA_PIP, 100); // play short beeps
                int seconds = (int) millisUntilFinished / 1000 % 60; // retrieve the amount of seconds remaining
                countDownText.setText(String.valueOf((seconds))); // update the UI with the seconds remaining

            }

            // this method will be called when the count down has finished.
            @Override
            public void onFinish() {
                beepTone.startTone(ToneGenerator.TONE_CDMA_ANSWER, 800); // play long sound
                countDownText.setVisibility(View.GONE); // remove the counter from the UI

                trackingView.setVisibility(View.VISIBLE);
                startTimer();                   // start timer for session
                session.startSession();          // start the running session
            }
        }.start(); // start the counter straight after it is created
    }

    private void startTimer() {
        timer.setBase(SystemClock.elapsedRealtime()); // reset the timer to current time
        timer.start(); // then start from 0

        // update info every 10 second
        timer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                // calculate the seconds elapsed by getting difference since started
                int elapsedSeconds = (int)(SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;

                if (elapsedSeconds % 2 == 0) { // every 2 seconds
                    // update user's location and data
                    session.updateData();
                }

            }
        });
    }

    private void updateSessionTexts(String spTxt, String spLbl, String spl) {
        titleText.setText(sessionName); // set to Running or Cycling
        speedText.setText(spTxt); // update to 0 or 00:00
        speedLabel.setText(spLbl); // update to km/h or /km
        constSpeed.setText(spl); // update label to Speed/Pace
    }


}

