package com.faizan.onefitness.journal;

import android.content.Context;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.faizan.onefitness.R;
import com.faizan.onefitness.SessionData.SessionData;
import com.faizan.onefitness.session.MySession;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class RecyclerSessionAdapter extends RecyclerView.Adapter<RecyclerSessionAdapter.ViewHolder> {
    private static final String TAG = "MyFit-RecyclerAdapter";  // used for debugging
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private Context context;
    private OnSessionListener onSessionListener;
    // hold the data for sessions
    private List<SessionData> sessionData;

    // hold all previous dates, will hold unique dates
    private List<String> dates = new ArrayList<>();
    // hold items that should always show the date
    private List<Integer> headerItems = new ArrayList<>();


    public RecyclerSessionAdapter(Context cntx, OnSessionListener onSessionListener) {
        context = cntx;
        this.onSessionListener = onSessionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate the adapter with items
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sessionitem, parent, false);
        ViewHolder holder = new ViewHolder(view, onSessionListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");

        // update for each item
        holder.titleText.setText(sessionData.get(position).getName());
        holder.timeText.setText(sessionData.get(position).getActiveTime());
        holder.distanceText.setText(sessionData.get(position).getDistance());
        // if the data type is cycling then update the image
        if (sessionData.get(position).getName().equals("Cycling")) {
            holder.image.setImageResource(R.drawable.cyclingImage);
        }


        // get the time from the current session
        long startTime = sessionData.get(position).getStartTime();
        // format the time to retrieve the date
        String date = dateFormat.format(startTime);


        // Only show the date if it does not already exist
        // or item previously showed the date
        if ((!dates.contains(date) || headerItems.contains(position))) {
            dates.add(date); // add because it is a unique date
            headerItems.add(position); // item should show date everytime

            // Show the header for the date
            holder.headingView.setVisibility(View.VISIBLE);
            // if date is today's date then set it to 'Today'
            if (date.equals(dateFormat.format(System.currentTimeMillis()))) {
                date = "Today";
            }
            // set the date text to the session's date
            holder.dateText.setText(date);
        } else {
            // remove the date if it's already shown
            holder.headingView.setVisibility(View.GONE);

        }

    }

    @Override
    public int getItemCount() {
        if (sessionData == null) {
            return 0; // return 0 if no data is loaded
        }
        return sessionData.size(); // else return number of items
    }

    public void setTasks(List<SessionData> sessions) {
        sessionData = sessions; // update data
        notifyDataSetChanged(); // update view
    }

    public List<SessionData> getTasks() {
        return sessionData;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // hold the view elements
        ImageView image;
        TextView titleText;
        TextView timeText;
        TextView distanceText;
        // variables to handle Date
        LinearLayout headingView;
        TextView dateText;

        OnSessionListener onSessionListener;

        public ViewHolder(@NonNull View itemView, OnSessionListener onSessionListener) {
            super(itemView);
            // get the elements from the UI
            image = (ImageView) itemView.findViewById(R.id.imageView);
            titleText = (TextView) itemView.findViewById(R.id.titleText);
            timeText = (TextView) itemView.findViewById(R.id.timeText);
            distanceText = (TextView) itemView.findViewById(R.id.distanceText);

            headingView = (LinearLayout) itemView.findViewById(R.id.dateView);
            dateText = (TextView) itemView.findViewById(R.id.dateText);

            this.onSessionListener = onSessionListener; // assign click listener
            itemView.setOnClickListener(this); // implement click listener
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: called");
            // call the interface method when item is clicked
            onSessionListener.onSessionClick(getAdapterPosition());
        }
    }

    public interface OnSessionListener {
        void onSessionClick(int position);
    }
}
