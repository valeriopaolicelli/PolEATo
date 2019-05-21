package com.mad.poleato.Statistics;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mad.poleato.History.HistoryComparator;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment {


    private View fragView;              //the view for this fragment
    private Activity hostActivity;     //the activity that host this fragment
    private Toast myToast;

    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;


    private Map<String, TextView> tv_Fields;    //TextView map
    private GraphView graphView;                //statistics graph

    private static final double REVENUE_HOUR = 7.50;

    //contains <Day, sum(Millis)>
    private Map<String, Long> workingHourPerDay;
    private double totKm;








    public StatisticsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if (hostActivity != null) {
            myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_LONG);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //authenticate the user
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
        if(currentUser == null)
            logout();

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);

        tv_Fields = new HashMap<>();
        workingHourPerDay = new HashMap<>();

        totKm = 0;

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.statistics_layout, container, false);

        collectFields();

        readHistory();

        setGraph();

        return fragView;
    }


    private void collectFields(){

        tv_Fields.put("riderID", (TextView) fragView.findViewById(R.id.riderID_tv));

        tv_Fields.put("workingDays", (TextView) fragView.findViewById(R.id.workingDays_tv));
        tv_Fields.put("workingHours", (TextView) fragView.findViewById(R.id.totHours_tv));

        tv_Fields.put("totRevenues", (TextView) fragView.findViewById(R.id.totRevenues_tv));
        tv_Fields.put("revenuesPerDay", (TextView) fragView.findViewById(R.id.revenuesPerDay_tv));

        tv_Fields.put("totKm", (TextView) fragView.findViewById(R.id.totKm_tv));
        tv_Fields.put("kmPerDay", (TextView) fragView.findViewById(R.id.kmPerDay_tv));

        graphView = (GraphView) fragView.findViewById(R.id.graphView);
    }


    private void readHistory(){


        DatabaseReference historyReference = FirebaseDatabase.getInstance().getReference("deliveryman")
                                                                .child(currentUserID+"/history");

        historyReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists())
                    return;

                for(DataSnapshot historyItem : dataSnapshot.getChildren()){

                    //a day is considered working days if the rider receives at least 1 reservation no matter if it is delivered after 00:00
                    String workDay = historyItem.child("notifiedTime").getValue().toString().split(" ")[0];
                    String startTime = historyItem.child("notifiedTime").getValue().toString();
                    String endTime = historyItem.child("deliveredTime").getValue().toString();

                    Long workHour = timeDiff(startTime, endTime);

                    if(workingHourPerDay.containsKey(workDay)){
                        //then increment the working hours value
                        Long prevMillis = workingHourPerDay.get(workDay);
                        workHour += prevMillis;
                        workingHourPerDay.put(workDay, workHour);

                    }
                    else
                        workingHourPerDay.put(workDay, workHour);

                    //count km
                    double curr_km = Double.parseDouble(historyItem.child("totKm").getValue().toString());
                    totKm += curr_km;

                }

                computeStatistics();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void computeStatistics(){

        String workingDays = "" + workingHourPerDay.keySet().size() + "d";
        Long sumMillis = 0L;
        for(Long l : workingHourPerDay.values())
            sumMillis += l;

        double effectiveHours = sumMillis / (60 * 60 * 1000) % 24;
        String totHours = (int)effectiveHours + ":" + sumMillis/(60 * 1000) % 60;

        double totRevenues = effectiveHours * REVENUE_HOUR;

        double dailyRevenues = totRevenues / workingHourPerDay.keySet().size();

        double kmPerDay = totKm / workingHourPerDay.keySet().size();


        //set the TextViews
        tv_Fields.get("workingDays").setText(workingDays);
        totHours += "h";
        tv_Fields.get("workingHours").setText(totHours);
        DecimalFormat decimalFormat = new DecimalFormat("#0.00"); //two decimal
        String revenuesStr = decimalFormat.format(totRevenues) + "€";
        tv_Fields.get("totRevenues").setText(revenuesStr);
        String dailyRevStr = decimalFormat.format(dailyRevenues) + "€";
        tv_Fields.get("revenuesPerDay").setText(dailyRevStr);
        String totKmStr = decimalFormat.format(totKm) + "km";
        tv_Fields.get("totKm").setText(totKmStr);
        String dailyKmStr = decimalFormat.format(kmPerDay) + "km";
        tv_Fields.get("kmPerDay").setText(dailyKmStr);

    }


    private void setGraph(){

        // activate horizontal zooming and scrolling
        /*graphView.getViewport().setScalable(true);

        // activate horizontal scrolling
        graphView.getViewport().setScrollable(true);

        // activate horizontal and vertical zooming and scrolling
        graphView.getViewport().setScalableY(true);

        // activate vertical scrolling
        graphView.getViewport().setScrollableY(true);*/


        // set manual X bounds
        graphView.getViewport().setXAxisBoundsManual(true);
        graphView.getViewport().setMinX(0);
        graphView.getViewport().setMaxX(10);

        // set manual Y bounds
        graphView.getViewport().setYAxisBoundsManual(true);
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(100);

        // enable scaling and scrolling
        graphView.getViewport().setScalable(true);
        graphView.getViewport().setScalableY(true);

        DecimalFormat decimalFormat = new DecimalFormat("#0.00"); //two decimal
        String revenueHourStr = decimalFormat.format(REVENUE_HOUR);
        graphView.setTitle(hostActivity.getString(R.string.chart_title) +" (" + revenueHourStr + "€)");
        graphView.setTitleTextSize(80);
        //graphView.getGridLabelRenderer().setHorizontalAxisTitle("WEEEEEEEEEEEE");
        //graphView.getGridLabelRenderer().setVerticalAxisTitle("y axis");

        graphView.getGridLabelRenderer().setHorizontalLabelsVisible(true);
        //graphView.getGridLabelRenderer().setLabelsSpace(100);
        graphView.getGridLabelRenderer().setNumHorizontalLabels(5);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });

        graphView.addSeries(series);
    }


    private Long timeDiff(String start, String end){

        SimpleDateFormat format = new SimpleDateFormat("YYYY/mm/dd HH:mm");
        long difference = 0;
        try {
            Date startTime = format.parse(start);
            Date endTime = format.parse(end);
            difference = endTime.getTime() - startTime.getTime();

            //debug
            long diffSeconds = difference / 1000 % 60;
            long diffMinutes = difference / (60 * 1000) % 60;
            long diffHours = difference / (60 * 60 * 1000) % 24;
            long diffDays = difference / (24 * 60 * 60 * 1000);

        } catch (Exception e){
            Log.d("matte", e.getMessage());
        }


        return difference;


    }


    private void logout(){
        FirebaseAuth.getInstance().signOut();
        //                OneSignal.sendTag("User_ID", "");
        OneSignal.setSubscription(false);

        /**
         *  GO TO LOGIN ****
         */
        Navigation.findNavController(fragView).navigate(R.id.action_statisticsFragment_to_signInActivity);
        getActivity().finish();
    }

}
