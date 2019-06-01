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

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.AuthentucatorD.Authenticator;
import com.mad.poleato.Firebase.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatisticsFragment extends Fragment {

    /**
     * The inflated view object for this fragment
     */
    private View fragView;              //the view for this fragment

    /**
     * The activity that hosts this fragment
     */
    private Activity hostActivity;     //the activity that host this fragment

    private Toast myToast;

    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;

    private MyDatabaseReference historyFireBaseReference;


    private Map<String, TextView> tv_Fields;    //TextView map
    private LineChart dailyChart;                //statistics graph

    private static final double REVENUE_HOUR = 7.00;
    private static final int NUM_DAYS_GRAPH = 7; //num of days to show on a single graph window

    //contains <Day, sum(Millis)>
    private Map<Date, Long> workingHourPerDay;
    private Map<Date, Double> revenues;
    private double totKm;


    //mapping index -> day
    List<Long> xValues;


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

//
//    private void logout() {
//        FirebaseAuth.getInstance().signOut();
//        OneSignal.setSubscription(false);
//
//        //logout
//        Navigation.findNavController(fragView).navigate(R.id.action_statisticsFragment_to_signInActivity); //TODO mich
//        getActivity().finish();
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.statistics_layout, container, false);

        /** Logout a priori if access is revoked */
        if (currentUserID == null)
            Authenticator.revokeAccess(Objects.requireNonNull(getActivity()), fragView);

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


    /**
     * Collect all the view inside the map tv_Fields
     */
    private void collectFields() {

        tv_Fields.put("today", (TextView) fragView.findViewById(R.id.today_tv));

        tv_Fields.put("workingDays", (TextView) fragView.findViewById(R.id.workingDays_tv));
        tv_Fields.put("workingHours", (TextView) fragView.findViewById(R.id.totHours_tv));

        tv_Fields.put("totRevenues", (TextView) fragView.findViewById(R.id.totRevenues_tv));
        tv_Fields.put("revenuesPerDay", (TextView) fragView.findViewById(R.id.revenuesPerDay_tv));

        tv_Fields.put("totKm", (TextView) fragView.findViewById(R.id.totKm_tv));
        tv_Fields.put("kmPerDay", (TextView) fragView.findViewById(R.id.kmPerDay_tv));

        dailyChart = (LineChart) fragView.findViewById(R.id.lineChart);

        //set current day in the upper TextView
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MM/dd");
        String today = dateFormat.format(new Date());
        tv_Fields.get("today").setText(today);
    }


    /**
     * read all the history table from Firebase
     */
    private void readHistory() {

        historyFireBaseReference = new MyDatabaseReference(FirebaseDatabase.getInstance().getReference("deliveryman")
                .child(currentUserID + "/history"));

        historyFireBaseReference.setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists())
                    return;

                for (DataSnapshot historyItem : dataSnapshot.getChildren()) {

                    try {
                        //a day is considered working days if the rider accepts at least 1 reservation no matter if it is delivered after 00:00
                        String workDay = historyItem.child("startTime").getValue().toString().split(" ")[0];
                        String startTime = historyItem.child("startTime").getValue().toString();
                        String endTime = historyItem.child("endTime").getValue().toString();

                        Long workHour = timeDiff(startTime, endTime);

                        Date day = new SimpleDateFormat("yyyy/MM/dd").parse(workDay);

                        if (workingHourPerDay.containsKey(day)) {
                            //then increment the working hours value
                            Long prevMillis = workingHourPerDay.get(day);
                            workHour += prevMillis;
                            workingHourPerDay.put(day, workHour);

                        } else
                            workingHourPerDay.put(day, workHour);

                        //count km
                        double curr_km = Double.parseDouble(historyItem.child("totKm").getValue().toString());
                        totKm += curr_km;


                    } catch (Exception e) {
                        Log.d("matte", e.getMessage());
                    }


                }

                computeStatistics(); //fill the TextViews

                computeRevenues();//fill the revenues map

                setChart(); //draw data on graph

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * computes all the following statistics (based on the history values) and the updates the textViews:
     *          - total working days
     *          - total working hours
     *          - total revenues
     *          - avg revenues per day
     *          - total km
     *          - avg total km per day
     */
    private void computeStatistics() {

        String workingDays = "" + workingHourPerDay.keySet().size() + "d";
        Long sumMillis = 0L;
        for (Long l : workingHourPerDay.values())
            sumMillis += l;

        double effectiveHours = sumMillis / (60 * 60 * 1000) % 24;
        double minutes = sumMillis / (60 * 1000) % 60;
        DecimalFormat format = new DecimalFormat("00");
        String totHours = (int) effectiveHours + ":" + format.format(minutes);

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


    /**
     * computes the revenues for each single day and insert it in the revenues map
     */
    private void computeRevenues() {

        for (Date day : workingHourPerDay.keySet()) {
            //compute the total revenues for each single day
            long totMillis = workingHourPerDay.get(day);
            long totHours = totMillis / (60 * 60 * 1000) % 24;
            double rev = totHours * REVENUE_HOUR;
            revenues.put(day, rev);
        }

    }


    /**
     * Computes the millis elapsed between start and end
     * @param start
     * @param end
     * @return total elapsed millis
     */
    private Long timeDiff(String start, String end) {

        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
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

        } catch (Exception e) {
            Log.d("matte", e.getMessage());
        }


        return difference;
    }


    /**
     * Initializes the chart with all the collected data
     */
    private void setChart() {

        List<Entry> entries = new ArrayList<Entry>();

        //creates entries such: < day and month, revenues for this day>
        xValues = new ArrayList<>();
        for (Date day : revenues.keySet()) {

            //entries.add(new Entry((float) day.getTime(), revenues.get(day).floatValue()));
            entries.add(new Entry(xValues.size(), revenues.get(day).floatValue()));
            xValues.add(day.getTime());
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setLineWidth(2f);
        dataSet.setColor(getResources().getColor(R.color.colorPrimaryDark));
        dataSet.setCircleColor(getResources().getColor(R.color.colorPrimaryDark));
        dataSet.setCircleRadius(7f);
        dataSet.setCircleHoleRadius(5f);
        LineData line = new LineData(dataSet);

        dailyChart.setData(line);
        dailyChart.invalidate();


        //format the X labels
        XAxis xAxis = dailyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new XValueFormatter());

        xAxis.setGranularityEnabled(true);
        //xAxis.setGranularity(8.64e+7f);
        xAxis.setGranularity(1f);

        xAxis.setLabelCount(entries.size()); //to show all x labels
        xAxis.setSpaceMax(.3f);

        //format the Y labels
        YAxis yAxis = dailyChart.getAxisLeft();
        yAxis.setValueFormatter(new YValueFormatter());




        //remove the right Y label
        dailyChart.getAxisRight().setEnabled(false);

        //to remove the description from the bottom right corner
        dailyChart.getDescription().setEnabled(false);
        dailyChart.getLegend().setEnabled(false);

        //dailyChart.setVisibleXRangeMaximum(8.64e+7f * 7);
        dailyChart.setVisibleXRange(1, 7);
        dailyChart.setVisibleYRange(1, 80, YAxis.AxisDependency.LEFT);

        //to remove negative values on y axis
        yAxis.setAxisMinimum(0);
        yAxis.setGranularity(1f);

        dailyChart.setScaleEnabled(false);
        dailyChart.setHorizontalScrollBarEnabled(true);


    }


    /**
     * The formatter for the X values.
     * Format: month / day
     */
    private class XValueFormatter extends ValueFormatter{

        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            long millis = xValues.get((int) value);
            SimpleDateFormat format = new SimpleDateFormat("MM/dd");
            //long millis = (long) value;
            String date = format.format(new Date(millis));
            Log.d("matte", date);
            return date;
        }
    }


    /**
     * The formatter for the Y values.
     * Format: x.xx$
     */
    private class YValueFormatter extends ValueFormatter{

        @Override
        public String getAxisLabel(float value, AxisBase axis) {

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
            return "";
        }

    }



}
