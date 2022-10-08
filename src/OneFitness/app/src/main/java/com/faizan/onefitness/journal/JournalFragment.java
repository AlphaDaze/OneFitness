package com.faizan.onefitness.journal;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.faizan.onefitness.HelperClasses.AppExecutors;
import com.faizan.onefitness.R;
import com.faizan.onefitness.SessionData.SessionData;
import com.faizan.onefitness.SessionData.SessionDatabase;
import com.faizan.onefitness.session.SessionSummaryFragment;

import java.util.List;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class JournalFragment extends Fragment implements RecyclerSessionAdapter.OnSessionListener{
    private static final String TAG = "MyFit-JournalFragment";  // used for debugging
    private static final int REQUEST_WRITE_CODE = 7846; // used to identify intent
    private static final int REQUEST_READ_CODE = 3845; // used to identify intent

    private RecyclerView recyclerView; // hold scrollable view
    private RecyclerSessionAdapter adapter; // adapter to handle view
    private SessionDatabase mDb; // database to retrieve data

    private Button exportButton; // allow user to save data
    private Button importButton; // allow user to import data

    List<SessionData> sessions; // hold sessions from database

    public JournalFragment() {
        // Required empty public constructor
    }

    public static JournalFragment newInstance() {
        JournalFragment fragment = new JournalFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_journal, container, false);

        getActivity().setTitle("Journal");

        // retrieve UI buttons and recyclerview
        exportButton = (Button) view.findViewById(R.id.exportButton);
        importButton = (Button) view.findViewById(R.id.importButton);
        recyclerView = view.findViewById(R.id.recyclerView);

        initRecyclerView();

        mDb = SessionDatabase.getInstance(getContext());

        // when the export button is presssed
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start an intent to ask for permission to write to user's files
                // this will show the user a tree view
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                startActivityForResult(intent, REQUEST_WRITE_CODE);
            }
        });

        // when the import button is presssed
        importButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start an intent to select the database file
                // this will show the user a tree view
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("multipart/form-data"); // set the type of file to open
                intent.addCategory(Intent.CATEGORY_OPENABLE); // make sure there is read permissions
                startActivityForResult(intent, REQUEST_READ_CODE);
            }
        });
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        switch (requestCode) {
            case REQUEST_WRITE_CODE:              // if the save location is chosen
                if (resultCode == Activity.RESULT_OK) { // location chosen with success
                    Uri treeUri = resultData.getData(); // get the Uri for the selected directory

                    Log.d(TAG, "save location: " + treeUri.getPath()); // log it for debug

                    mDb.save(treeUri); // copy the database over to the user's directory
                }
                break;
            case REQUEST_READ_CODE:                 // if file is opened
                if (resultCode == Activity.RESULT_OK) { // file is read successfully
                    Uri fileUri = resultData.getData(); // get the URI of the file

                    Log.d(TAG, "load file: " + fileUri); // log for debugging

                    mDb.load(fileUri); // load the file into the database
                    updateView(); // refresh view
                }
                break;
        }

    }

    @Override
    public void onResume() {
        // called when fragment is shown
        super.onResume();
        // get all the sessions from the database
        retrieveTasks();
    }

    private void retrieveTasks() {
        // start a new thread
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                // update the data in the background thread
                sessions = mDb.sessionDataDao().getSessionDataListSorted();
                // update the view in the UI thread
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.setTasks(sessions);
                    }
                });
            }
        });
    }

    private void initRecyclerView() {
        // Use simple linear layout
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // set left and right swipe action
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        // Create a new adapter to hold all the items
        adapter = new RecyclerSessionAdapter(getContext(), this);
        // set the adapter on the UI element
        recyclerView.setAdapter(adapter);
        // add divider lines
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                getResources().getConfiguration().orientation);
        recyclerView.addItemDecoration(dividerItemDecoration);
    }

    @Override
    public void onSessionClick(int position) {
        Bundle args = new Bundle();
        args.putSerializable(SessionSummaryFragment.ARG_SESSION, sessions.get(position));

        SessionSummaryFragment fragment = new SessionSummaryFragment();
        fragment.setArguments(args);
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_right_to_left, R.anim.exit_right_to_left, R.anim.enter_left_to_right, R.anim.exit_left_to_right)
                .replace(R.id.fragment_container, fragment, "summary") // replace the current fragment
                .addToBackStack(null);  // add this fragment to the stack

        fragmentTransaction.commit(); // execute the fragment call created above
    }

    private void updateView() {
        // get the new database
        mDb = SessionDatabase.getInstance(getContext());
        // remove the recyclerView
        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(null);
        // setup a new recyclerView
        initRecyclerView();
        retrieveTasks();
        adapter.notifyDataSetChanged(); // get new data
        // force a redraw of the whole fragment
        getView().invalidate(); // redraw
    }

    // we will get a callback when an item is swipred left or right
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false; // implement this later
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int swipedPos = viewHolder.getAdapterPosition(); // get the item swiped
            Log.d(TAG, "swiped item: " + swipedPos); // debug purpose

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.sessionDataDao().deleteSessionData(sessions.get(swipedPos)); // delete the item from the database
                }
            });
            retrieveTasks(); // update the adapter with the new list
            initRecyclerView(); // force redraw
            //adapter.notifyItemRemoved(swipedPos); // update the recyclerView adapter so the item is removed in the GUI
        }

        // draw the background when item is swiped
        @Override
        public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                 float dX, float dY, int actionState, boolean isCurrentlyActive){

            // set the background to an orange red
            new RecyclerViewSwipeDecorator.Builder(getContext(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimary))
                    .addActionIcon(R.drawable.ic_bin) // draw a bin icon
                    .create()
                    .decorate(); // add it to view
            // draw parent's items implmented by android
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}
