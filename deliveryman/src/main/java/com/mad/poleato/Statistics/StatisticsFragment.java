package com.mad.poleato.Statistics;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.PointsGraphSeries;
import com.jjoe64.graphview.series.Series;
import com.mad.poleato.Firebase.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


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

    private MyDatabaseReference historyFireBaseReference;


    private Map<String, TextView> tv_Fields;    //TextView map
    private GraphView graphView;                //statistics graph

    private static final double REVENUE_HOUR = 7.00;
    private static final int NUM_DAYS_GRAPH = 7; //num of days to show on a single graph window

    //contains <Day, sum(Millis)>
    private Map<Date, Long> workingHourPerDay;
    private Map<Date, Double> revenues;
    private double totKm;








    public StatisticsFragment() {
        // Required empty public constructor
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if (hostActivity != null) {
            myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_SHORT);
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
        workingHourPerDay = new TreeMap<>();
        revenues = new TreeMap<>();

        totKm = 0;

    }


    private void logout(){
        FirebaseAuth.getInstance().signOut();
        OneSignal.setSubscription(false);

        //logout
        Navigation.findNavController(fragView).navigate(R.id.action_statisticsFragment_to_signInActivity); //TODO mich
        getActivity().finish();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.statistics_layout, container, false);

        collectFields();

        readHistory();


        return fragView;
    }


    @Override
    public void onStop() {
        super.onStop();
        historyFireBaseReference.removeAllListener();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        historyFireBaseReference.removeAllListener();
    }


    private void collectFields(){

        tv_Fields.put("today", (TextView) fragView.findViewById(R.id.today_tv));

        tv_Fields.put("workingDays", (TextView) fragView.findViewById(R.id.workingDays_tv));
        tv_Fields.put("workingHours", (TextView) fragView.findViewById(R.id.totHours_tv));

        tv_Fields.put("totRevenues", (TextView) fragView.findViewById(R.id.totRevenues_tv));
        tv_Fields.put("revenuesPerDay", (TextView) fragView.findViewById(R.id.revenuesPerDay_tv));

        tv_Fields.put("totKm", (TextView) fragView.findViewById(R.id.totKm_tv));
        tv_Fields.put("kmPerDay", (TextView) fragView.findViewById(R.id.kmPerDay_tv));

        graphView = (GraphView) fragView.findViewById(R.id.graphView);

        //set current day in the upper TextView
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MM/dd");
        String today = dateFormat.format(new Date());
        tv_Fields.get("today").setText(today);
    }


    private void readHistory(){

        historyFireBaseReference = new MyDatabaseReference(FirebaseDatabase.getInstance().getReference("deliveryman")
                                                        .child(currentUserID+"/history"));

        historyFireBaseReference.setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(!dataSnapshot.exists())
                    return;

                for(DataSnapshot historyItem : dataSnapshot.getChildren()){

                    try{
                        //a day is considered working days if the rider accepts at least 1 reservation no matter if it is delivered after 00:00
                        String workDay = historyItem.child("startTime").getValue().toString().split(" ")[0];
                        String startTime = historyItem.child("startTime").getValue().toString();
                        String endTime = historyItem.child("endTime").getValue().toString();

                        Long workHour = timeDiff(startTime, endTime);

                        Date day = new SimpleDateFormat("yyyy/MM/dd").parse(workDay);

                        if(workingHourPerDay.containsKey(day)){
                            //then increment the working hours value
                            Long prevMillis = workingHourPerDay.get(day);
                            workHour += prevMillis;
                            workingHourPerDay.put(day, workHour);

                        }
                        else
                            workingHourPerDay.put(day, workHour);

                        //count km
                        double curr_km = Double.parseDouble(historyItem.child("totKm").getValue().toString());
                        totKm += curr_km;


                    }catch(Exception e){
                        Log.d("matte", e.getMessage());
                    }


                }

                computeStatistics(); //fill the TextViews

                computeRevenues();//fill the revenues map

                setGraph(); //draw data on graph

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


    private void computeRevenues(){

        for(Date day : workingHourPerDay.keySet()){
            //compute the total revenues for each single day
            long totMillis = workingHourPerDay.get(day);
            long totHours = totMillis / (60 * 60 * 1000) % 24;
            double rev = totHours * REVENUE_HOUR;
            revenues.put(day, rev);
        }

    }


    private void setGraph(){

        // enable scaling and scrolling
        graphView.getViewport().setScalable(true);
        //graphView.getViewport().setScalableY(true);

        DecimalFormat decimalFormat = new DecimalFormat("#0.00"); //two decimal
        String revenueHourStr = decimalFormat.format(REVENUE_HOUR);
        graphView.setTitle(hostActivity.getString(R.string.chart_title) +" (" + revenueHourStr + "€/h)");
        graphView.setTitleTextSize(40);
        graphView.setTitleColor(Color.GRAY);


        DataPoint[] dp = new DataPoint[revenues.size()];
        int curr_idx = 0;
        for(Date day : revenues.keySet()){

            dp[curr_idx] = new DataPoint(day.getTime(), revenues.get(day));
            curr_idx ++;
        }

        LineGraphSeries<DataPoint> lines = new LineGraphSeries<>(dp);
        PointsGraphSeries<DataPoint> points = new PointsGraphSeries<>(dp);

        points.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {

                Log.d("matte", "clicked");
                DateFormat simple = new SimpleDateFormat("MM/dd");
                long millis = (long)dataPoint.getX();

                // Creating date from milliseconds
                // using Date() constructor
                Date result = new Date(millis);

                //price
                DecimalFormat decimalFormat = new DecimalFormat("#0.00"); //two decimal
                double d = Double.parseDouble(((Double) dataPoint.getY()).toString());
                String priceStr = decimalFormat.format(d);

                myToast.setText(simple.format(result) + " -> " + priceStr + " €");
                myToast.show();
            }
        });

        graphView.addSeries(lines);
        graphView.addSeries(points);


        // set date label formatter
        graphView.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));

        // set manual x bounds to have nice steps
        Date minDate, maxDate;
        List<Date> dates = new ArrayList<>(revenues.keySet());
        if(dates.size() >= NUM_DAYS_GRAPH) {

            minDate = dates.get(dates.size() - NUM_DAYS_GRAPH);
            maxDate = dates.get(dates.size() - 1);
        }
        else{

            minDate = dates.get(0);
            maxDate = dates.get(dates.size() - 1);
        }
        graphView.getViewport().setMinX(minDate.getTime());
        graphView.getViewport().setMaxX(maxDate.getTime());
        graphView.getViewport().setXAxisBoundsManual(true);

        // set manual Y bounds
        graphView.getViewport().setMinY(0);
        graphView.getViewport().setMaxY(110);
        graphView.getViewport().setYAxisBoundsManual(true);

        // as we use dates as labels, the human rounding to nice readable numbers
        // is not necessary
        graphView.getGridLabelRenderer().setHumanRounding(false);
        graphView.getGridLabelRenderer().setNumVerticalLabels(10);
        //graphView.getGridLabelRenderer().setHighlightZeroLines(true);
        graphView.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.VERTICAL);

        graphView.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter(){

            @Override
            public String formatLabel(double value, boolean isValueX){

                if(isValueX){
                    SimpleDateFormat format = new SimpleDateFormat("mm/dd");
                    long millis = (long) value;
                    String date = format.format(new Date(millis));
                    return date;
                }

                //y value
                Double v = new Double(value);
                DecimalFormat decimalFormat = new DecimalFormat("#0.00"); //two decimal
                double d = Double.parseDouble(v.toString());
                String priceStr = decimalFormat.format(d);
                Integer i = v.intValue();
                Boolean b1 = value % 1 == 0; //must not have decimal part
                Boolean b2 = i % 10 == 0; //must be a multiple of 10
                if(b1 && b2)
                    return priceStr+"€";
                return null;

            }
        });


    }


    private Long timeDiff(String start, String end){

        SimpleDateFormat format = new SimpleDateFormat("yyy/mm/dd HH:mm");
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


}
