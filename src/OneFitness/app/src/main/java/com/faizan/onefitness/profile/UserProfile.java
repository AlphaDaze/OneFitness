package com.faizan.onefitness.profile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.faizan.onefitness.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.master.glideimageview.GlideImageView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserProfile extends Fragment {
    private static final String TAG = "MyFit-ProfileFragment";  // used for debugging

    public static final String STEPS_GOAL_TAG = "STEPS_GOAL";
    public static final String CALORIES_GOAL_TAG = "CALORIES_GOAL";
    private static final String WEIGHT_TAG = "WEIGHT_PROFILE";
    private static final String HEIGHT_TAG = "HEIGHT_PROFILE";
    private static final int STEPS_DIALOG = 5494;
    private static final int CALORIES_DIALOG = 8494;
    private static final int HEIGHT_DIALOG = 1848;
    private static final int WEIGHT_DIALOG = 2494;

    private static DecimalFormat twoDForm = new DecimalFormat("#.##");

    // access UI elements
    private TextView profileName;
    private GlideImageView profileImage;
    private TextView stepsGoalText;
    private TextView caloriesGoalText;
    private TextView weightText;
    private TextView heightText;

    private SharedPreferences sharedPrefs; // used to save and retrieve values
    private Double weight = 60.0;
    private Double height = 175.0;

    public UserProfile() {
        // Required empty public constructor
    }

    public static UserProfile newInstance() {
        UserProfile fragment = new UserProfile();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        getActivity().setTitle("Profile"); // set title

        // get UI elements
        profileName = (TextView) view.findViewById(R.id.profileTextView);
        profileImage = (GlideImageView) view.findViewById(R.id.profileImageView);
        stepsGoalText = (TextView) view.findViewById(R.id.stepsGoal);
        caloriesGoalText = (TextView) view.findViewById(R.id.caloriesGoal);
        weightText = (TextView) view.findViewById(R.id.weightText);
        heightText = (TextView) view.findViewById(R.id.heightText);

        // show dialog when goals are clicked
        stepsGoalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textAlertDialog(STEPS_DIALOG);
            }
        });

        caloriesGoalText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textAlertDialog(CALORIES_DIALOG);
            }
        });
        // open dialog when textviews are clicked
        weightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollAlertDialog(WEIGHT_DIALOG);
            }
        });

        heightText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollAlertDialog(HEIGHT_DIALOG);
            }
        });

        // update UI with user's info
        restoreUserData();

        return view;
    }
    
    private void restoreUserData() {
        // get data from sharedPreference
        sharedPrefs = getActivity().getSharedPreferences("com.faizan.myfitness", Context.MODE_PRIVATE);
        // retrieve data for steps and calories if stored or 0
        int steps = sharedPrefs.getInt(STEPS_GOAL_TAG, 0);
        int calories = sharedPrefs.getInt(CALORIES_GOAL_TAG, 0);
        // set the steps and calories text
        stepsGoalText.setText(String.valueOf(steps));
        caloriesGoalText.setText(String.valueOf(calories));

        // sign in with last account
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getContext());
        if (acct != null) {
            // get the accounts name and picture
            String personName = acct.getDisplayName();
            Uri personPhoto = acct.getPhotoUrl();

            // set the name and image on the UI elements
            profileName.setText(personName);
            if (personPhoto != null) {
                profileImage.loadImageUrl(personPhoto.toString());
            }
        }

        restoreFitData(); // get weight and height from api
    }

    // alert dialog to show an edit text view
    private void textAlertDialog(final int result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // get input from the user
        final EditText input = new EditText(getContext());
        // make dialog only accept
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(5)});
        // open alert dialog
        builder.setView(input);

        // set the text to the correct goal
        switch (result) {
            case STEPS_DIALOG:
                builder.setTitle("Set Steps Goal");
                input.setText(stepsGoalText.getText());
                break;
            case CALORIES_DIALOG:
                builder.setTitle("Set Calories Goal");
                input.setText(caloriesGoalText.getText());
                break;
        }

        // Set up the buttons
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // set the new steps/calories goal
                textAlertDialogResult(result, input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void textAlertDialogResult(final int result, String input) {
        // if the input is not empty then update it
        if (!input.isEmpty()) {
            // handle result from dialog for step or calories
            switch (result) {
                case STEPS_DIALOG:
                    Log.d(TAG, "Set steps dialog");
                    stepsGoalText.setText(input);
                    sharedPrefs.edit().putInt(STEPS_GOAL_TAG, Integer.parseInt(input)).apply();
                    break;
                case CALORIES_DIALOG:
                    Log.d(TAG, "Set calories dialog");
                    caloriesGoalText.setText(input);
                    sharedPrefs.edit().putInt(CALORIES_GOAL_TAG, Integer.parseInt(input)).apply();
                    break;
            }
        }
    }



    // dialog used for weight and height
    private void scrollAlertDialog(final int result) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(getContext());

        // inflate the dialog with a custom view
        View holder = View.inflate(getContext(), R.layout.spinners, null);
        // retrieve the UI elements
        final Spinner leftSpinner = holder.findViewById(R.id.leftSpinner);
        final Spinner rightSpinner = holder.findViewById(R.id.rightSpinner);
        TextView leftText = holder.findViewById(R.id.leftText);
        TextView rightText = holder.findViewById(R.id.rightText);

        // use to populate spinners
        List<String> leftSpinnerArray = new ArrayList<>();
        List<String> rightSpinnerArray = new ArrayList<>();

        // check if dialog was called on weight or height
        switch (result) {
            case WEIGHT_DIALOG:
                builderSingle.setTitle("Set Weight"); // set the title
                // populate the first spinner with values from 20 to 650
                for(int i = 20; i <= 650; i++){
                    leftSpinnerArray.add(String.valueOf(i));
                }
                // populate the second spinner with values from 0 to 99
                for(int i = 0; i <= 99; i++){
                    rightSpinnerArray.add(String.valueOf(i));
                }
                // set the first text to a decimal point
                leftText.setText(".");
                rightText.setText("kg"); // and the second to kg to show unit
                break;
            case HEIGHT_DIALOG:
                builderSingle.setTitle("Set Height"); // set title
                // populate left spinner from 30 to 300
                for(int i = 30; i <= 300; i++){
                    leftSpinnerArray.add(String.valueOf(i));
                }
                // set left text to cm for unit
                leftText.setText("cm");

                // remove both right spinner and TextView
                rightSpinner.setVisibility(View.GONE);
                rightText.setVisibility(View.GONE);
                break;
        }


        // Use an adapter to populate the spinner with the leftSpinnerArray and rightSpinnerArray
        ArrayAdapter<String> leftAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, leftSpinnerArray);
        ArrayAdapter<String> rightAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, rightSpinnerArray);
        // spinners should be drop down view's
        leftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // finially set the adapter so the values are set on the spinner
        leftSpinner.setAdapter(leftAdapter);
        rightSpinner.setAdapter(rightAdapter);

        // set the dialog values to those stored in the weight and height variables
        switch (result) {
            case WEIGHT_DIALOG:
                // round to two decimal points without whole number
                double roundOff = (double) Math.round((weight.doubleValue() - weight.intValue()) * 100) / 100;
                // get the two decimal points as a whole integer i.e 0.43 -> 43
                int decimalPart = (int) (100 * roundOff);

                // get right and left spinner positions using the values from the weight variable
                int rightSpinPos = rightAdapter.getPosition(String.valueOf(decimalPart));
                int leftSpinPos = leftAdapter.getPosition(String.valueOf(weight.intValue()));

                // finally set the spinners to show the above values as default
                leftSpinner.setSelection(leftSpinPos);
                rightSpinner.setSelection(rightSpinPos);
                break;
            case HEIGHT_DIALOG:
                // convert the height from meters to centi-meters
                int heightInCm = (int) (height * 100);
                // get the string value of the integer
                int pos = leftAdapter.getPosition(String.valueOf(heightInCm));
                // set leftSpinner to show the height as default value
                leftSpinner.setSelection(pos);
                break;
        }

        builderSingle.setView(holder); // set the view on the alert dialog
        // set the ok and cancel buttons
        builderSingle.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog,int which) {
                scrollAlertDialogResult(result, leftSpinner, rightSpinner);
            }
        });

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });


        builderSingle.show();
    }

    private void scrollAlertDialogResult(final int result, final Spinner leftSpinner, final Spinner rightSpinner) {
        switch (result) {
            case WEIGHT_DIALOG:
                // get the whole number, left spinner value as int
                int wholeNum = Integer.parseInt(leftSpinner.getSelectedItem().toString());
                // get decimal part of weight/rigth spinner
                int decimalNum = Integer.parseInt(rightSpinner.getSelectedItem().toString());
                // convert the value to double before dividing by 100 and add whole weight
                weight = wholeNum + (((double) decimalNum) / 100d);
                // set new weight in FIT API
                insertFitData(DataType.TYPE_WEIGHT, weight.floatValue());
                // save weight in app data
                sharedPrefs.edit().putFloat(WEIGHT_TAG, weight.floatValue()).apply();

                // log new weight
                Log.d(TAG, "new weight: " + weight);
                break;
            case HEIGHT_DIALOG:
                // get height selected by user
                int heightInCm = Integer.parseInt(leftSpinner.getSelectedItem().toString());
                height = ((double) heightInCm) / 100; // convert cm to metres by dividing by 100
                // set new weight in FIT API
                insertFitData(DataType.TYPE_HEIGHT, height.floatValue());
                // save height in app data
                sharedPrefs.edit().putFloat(HEIGHT_TAG, height.floatValue()).apply();

                // log new height
                Log.d(TAG, "new height: " + height);
                break;
        }
        restoreFitData(); // set new weight/height on profile screen
    }




    private void restoreFitData() {
        // retrieve the values, if they don't exist get -1
        float savedWeight = sharedPrefs.getFloat(WEIGHT_TAG, 0);
        float savedHeight = sharedPrefs.getFloat(HEIGHT_TAG, 0);

        if (savedWeight != 0) { // only set the value if it is saved
            weight = Double.valueOf(twoDForm.format(savedWeight));
            weightText.setText(weight + " kg"); // update UI
        } else { // else get the height from their google account
            // make a read request to get get the weight
            DataReadRequest readRequest = new DataReadRequest.Builder()
                    // the start time is set to 1 and end time is now
                    .setTimeRange(1, Calendar.getInstance().getTimeInMillis(), TimeUnit.MILLISECONDS)
                    .read(DataType.TYPE_WEIGHT)
                    .build();

            Fitness.getHistoryClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext())) // get the historyClient
                    .readData(readRequest)     // read weight and height from FIT
                    .addOnSuccessListener(                         // listener executed when method succeeds
                            new OnSuccessListener<DataReadResponse>() {

                                @Override
                                public void onSuccess(DataReadResponse dataReadResponse) {
                                    // get the weight data from the response from the API
                                    DataSet weightData = dataReadResponse.getDataSet(DataType.TYPE_WEIGHT);
                                    // get the weight converted as a float. If it's empty get the value 0
                                    float weightResponse = weightData.isEmpty() ? 0 : weightData.getDataPoints().get(0).getValue(Field.FIELD_WEIGHT).asFloat();

                                    // round the values to two decimal places
                                    weight = Double.valueOf(twoDForm.format(weightResponse));
                                    // update textViews with data
                                    weightText.setText(weight + " kg");
                                    // log data
                                    Log.d(TAG, "weight: " + weight);
                                }
                            })
                    .addOnFailureListener( // when method fails
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, "Error: " +  e); // log error
                                }
                            });
        }


        if (savedHeight != 0) { // only set the value if it is saved
            height = Double.valueOf(twoDForm.format(savedHeight));
            heightText.setText(height + " m"); // update UI
        } else { // else get their height from their google account
            // make a read request to get get the height
            DataReadRequest readRequest = new DataReadRequest.Builder()
                    // the start time is set to 1 and end time is now
                    .setTimeRange(1, Calendar.getInstance().getTimeInMillis(), TimeUnit.MILLISECONDS)
                    .read(DataType.TYPE_HEIGHT)
                    .build();

            Fitness.getHistoryClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext())) // get the historyClient
                    .readData(readRequest)     // read height from FIT
                    .addOnSuccessListener(      // listener executed when method succeeds
                            new OnSuccessListener<DataReadResponse>() {

                                @Override
                                public void onSuccess(DataReadResponse dataReadResponse) {
                                    // get the height data from the response from the API
                                    DataSet heightData = dataReadResponse.getDataSet(DataType.TYPE_HEIGHT);
                                    // get the weight converted as a float. If it's empty get the value 0
                                    float heightResponse = heightData.isEmpty() ? 0 : heightData.getDataPoints().get(0).getValue(Field.FIELD_HEIGHT).asFloat();

                                    // round the values to two decimal places
                                    height = Double.valueOf(twoDForm.format(heightResponse));
                                    // update UI
                                    heightText.setText(height + " m");

                                    // log data
                                    Log.d(TAG, "height: " + height);
                                }
                            })
                    .addOnFailureListener( // when method fails
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.i(TAG, "Error: " +  e); // log error
                                }
                            });
        }



    }


    private void insertFitData(DataType dataType, final float value) {
        long time = Calendar.getInstance().getTimeInMillis(); // get current time
        // build data type to add new api data to
        DataSource dataSource = new DataSource.Builder()
                .setAppPackageName(getContext())
                .setDataType(dataType)
                .setType(DataSource.TYPE_RAW)
                .build();
        // create data pint with the type of data and the value
        DataPoint dataPoint = DataPoint.builder(dataSource)
                .setTimeInterval(time, time, TimeUnit.MILLISECONDS)
                .setFloatValues(value)
                .build();
        // make the DataSet the API expects
        DataSet dataSet = DataSet.builder(dataSource)
                .add(dataPoint)
                .build();
        // finially get the history client and add the data to it
        Fitness.getHistoryClient(getContext(), GoogleSignIn.getLastSignedInAccount(getContext()))
                .insertData(dataSet)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Insert " + value); // log success
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to insert " + value); // log failiure
                    }
                });
    }
}
