package com.faizan.onefitness.session;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.faizan.onefitness.R;
import com.faizan.onefitness.SessionData.SessionData;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.text.SimpleDateFormat;

public class SessionSummaryFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MyFit-SessionSummary";
    public static final String ARG_SESSION = "SESSION";
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    // show google maps
    private MapView mapView;
    private GoogleMap map;


    private SessionData sessionData; // hold current sessions data

    private TextView titleText;             // set title
    private TextView dateText;              // set date
    private TextView timeText;
    private TextView distanceText;          // show distance to user
    private TextView speedText;              // show pace to user
    private TextView caloriesText;          // show calories to user
    private TextView constSpeed;            // update const labels
    private TextView speedLabel;
    private ImageView stepsImage;           // show or hide step data
    private TextView constSteps;
    private TextView stepsText;

    public SessionSummaryFragment() {
        // Required empty public constructor
    }


    public static SessionSummaryFragment newInstance(SessionData ses) {
        SessionSummaryFragment fragment = new SessionSummaryFragment();

        Bundle args = new Bundle();
        // get sessionData passed through argument
        args.putSerializable(ARG_SESSION, ses);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // get the argument with data
            sessionData = (SessionData) getArguments().getSerializable(ARG_SESSION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session_summary, container, false);

        // Inflate the layout for this fragment
        mapView = (MapView) view.findViewById(R.id.mapView); // init mapView fragment
        mapView.onCreate(savedInstanceState);
        mapView.onResume();                                 // if there is a previous state
        mapView.getMapAsync(this);


        // get all UI items
        titleText = (TextView) view.findViewById(R.id.titleText);
        dateText = (TextView) view.findViewById(R.id.dateText);
        timeText = (TextView) view.findViewById(R.id.timeText);
        distanceText = (TextView) view.findViewById(R.id.distanceText);
        speedText = (TextView) view.findViewById(R.id.speedText);
        caloriesText = (TextView) view.findViewById(R.id.calText);
        constSpeed = (TextView) view.findViewById(R.id.constSpeedText);
        speedLabel = (TextView) view.findViewById(R.id.constSpeedLbl);

        // update the UI texts with session info
        titleText.setText(sessionData.getName());
        distanceText.setText(sessionData.getDistance());
        speedText.setText(sessionData.getSpeed());
        caloriesText.setText(sessionData.getCalories());

        getActivity().setTitle(sessionData.getName() + " Summary"); // set title

        if (sessionData.getName().equals("Cycling")) {
            constSpeed.setText("Speed"); // update the UI elements
            speedLabel.setText("km/h");
        } else {
            // get UI elements for steps data
            stepsImage = (ImageView) view.findViewById(R.id.stepsImageView);
            constSteps = (TextView) view.findViewById(R.id.constStepsText);
            stepsText = (TextView) view.findViewById(R.id.stepText);

            // set UI elements visible
            stepsImage.setVisibility(View.VISIBLE);
            constSteps.setVisibility(View.VISIBLE);
            stepsText.setVisibility(View.VISIBLE);
            stepsText.setText(sessionData.getSteps());
        }

        setDate();

        return view;
    }

    private void setDate() {
        // gets the start and end time in the format dd/MM/yyyy, hh:mm - hh:mm
        String date = dateFormat.format(sessionData.getStartTime()) + ", "
                    + timeFormat.format(sessionData.getStartTime()) + " - " + timeFormat.format(sessionData.getEndTime());
        dateText.setText(date);

        // set the new time
        timeText.setText(sessionData.getActiveTime());
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        // make map constant
        map.getUiSettings().setAllGesturesEnabled(false);
        showTrack(); // show user's locations
    }


    private void showTrack() {
        // line that will be drawn with coordinates
        PolylineOptions track = new PolylineOptions().addAll(sessionData.getTrack().getCoordinates());
        track.color(Color.rgb(244, 67, 54)); // set color to orange
        track.width(13); // set the line width to 13
        track.endCap(new RoundCap()); // make ends round
        // draw a line of all the locations tracked
        map.addPolyline(track);

        // used to centre map on coordinates
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // add all track coordinates to builder
        for (int i = 0; i < track.getPoints().size(); i++) {
            builder.include(track.getPoints().get(i));
        }

        // get the devices density
        float scale = getContext().getResources().getDisplayMetrics().density;
        int padding = (int) (40 * scale + 0.5f); // used to add padding depending on screen size
        // centre camera to include the whole of track with padding
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(), padding);
        // animate the camera to move to the right location
        map.animateCamera(cu);
    }
}

