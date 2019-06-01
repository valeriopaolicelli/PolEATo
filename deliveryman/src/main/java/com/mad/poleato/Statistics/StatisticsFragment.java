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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
    private Spinner spinner;
    private LineChart dailyChart;                //statistics graph
    private BarChart monthlyChart;

    private static final double REVENUE_HOUR = 7.00;
    private static final int NUM_DAYS_GRAPH = 7; //num of days to show on a single graph window


    private Map<Date, Long> workingHoursPerDay; //contains <Day, sum(Millis)>
    private Map<Date, Double> dailyRevenues; //contains <Day, sum($)>
    private Map<Integer, Double> monthlyRevenues; //contains <Month, sum($)>
    private List<String> monthList;
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
        if (currentUser == null)
            logout();

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);

        tv_Fields = new HashMap<>();
        workingHoursPerDay = new TreeMap<>();
        dailyRevenues = new TreeMap<>();
        monthlyRevenues = new TreeMap<>();

        fillMonthList();

        totKm = 0;

    }

    private void fillMonthList(){

        monthList = new ArrayList<>();
        monthList.add(getString(R.string.jan)); monthList.add(getString(R.string.feb));
        monthList.add(getString(R.string.mar)); monthList.add(getString(R.string.apr));
        monthList.add(getString(R.string.may)); monthList.add(getString(R.string.jun));
        monthList.add(getString(R.string.jul)); monthList.add(getString(R.string.aug));
        monthList.add(getString(R.string.sep)); monthList.add(getString(R.string.oct));
        monthList.add(getString(R.string.nov)); monthList.add(getString(R.string.dec));
    }


    private void logout() {
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

        //attach the listener to the spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0)
                    setDailyChart();
                else
                    setMonthlyChart();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


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
        monthlyChart = (BarChart) fragView.findViewById(R.id.barChart);
        spinner = (Spinner) fragView.findViewById(R.id.chart_spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(hostActivity,
                                                    R.array.chart_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


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

                        if (workingHoursPerDay.containsKey(day)) {
                            //then increment the working hours value
                            Long prevMillis = workingHoursPerDay.get(day);
                            workHour += prevMillis;
                            workingHoursPerDay.put(day, workHour);

                        } else
                            workingHoursPerDay.put(day, workHour);

                        //count km
                        double curr_km = Double.parseDouble(historyItem.child("totKm").getValue().toString());
                        totKm += curr_km;


                    } catch (Exception e) {
                        Log.d("matte", e.getMessage());
                    }


                }

                computeStatistics(); //fill the TextViews

                computeDailyRevenues();//fill the daily revenues map

                computeMonthlyRevenues(); //fill the monthly revenues map

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    /**
     * computes all the following statistics (based on the history values) and the updates the cardView:
     *          - total working days
     *          - total working hours
     *          - total revenues
     *          - avg revenues per day
     *          - total km
     *          - avg total km per day
     */
    private void computeStatistics() {

        String workingDays = "" + workingHoursPerDay.keySet().size() + "d";
        Long sumMillis = 0L;
        for (Long l : workingHoursPerDay.values())
            sumMillis += l;

        double effectiveHours = sumMillis / (60 * 60 * 1000) % 24;
        double minutes = sumMillis / (60 * 1000) % 60;
        DecimalFormat format = new DecimalFormat("00");
        String totHours = (int) effectiveHours + ":" + format.format(minutes);

        double totRevenues = effectiveHours * REVENUE_HOUR;

        double dailyRevenues = totRevenues / workingHoursPerDay.keySet().size();

        double kmPerDay = totKm / workingHoursPerDay.keySet().size();


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
    private void computeDailyRevenues() {

        for (Date day : workingHoursPerDay.keySet()) {
            //compute the total revenues for each single day
            long totMillis = workingHoursPerDay.get(day);
            long totHours = totMillis / (60 * 60 * 1000) % 24;
            double rev = totHours * REVENUE_HOUR;
            dailyRevenues.put(day, rev);
        }

    }


    private void computeMonthlyRevenues(){

        for (Date day : dailyRevenues.keySet()){

            int month_idx = day.getMonth();
            if(!monthlyRevenues.containsKey(month_idx))
                monthlyRevenues.put(month_idx, dailyRevenues.get(day));
            else{
                double curr_rev = monthlyRevenues.get(month_idx);
                monthlyRevenues.put(month_idx, curr_rev + dailyRevenues.get(day));
            }
        }

    }


    /**
     * Initializes the chart with all the collected data
     */
    private void setDailyChart() {

        if(dailyRevenues == null)
            return;

        List<Entry> entries = new ArrayList<Entry>();

        //creates entries such: < day and month, revenues for this day>
        xValues = new ArrayList<>();
        for (Date day : dailyRevenues.keySet()) {

            //entries.add(new Entry((float) day.getTime(), revenues.get(day).floatValue()));
            entries.add(new Entry(xValues.size(), dailyRevenues.get(day).floatValue()));
            xValues.add(day.getTime());
        }

        LineDataSet dataSet = new LineDataSet(entries, "");
        dataSet.setLineWidth(2f);
        dataSet.setColor(getResources().getColor(R.color.colorPrimaryDark));
        dataSet.setCircleColor(getResources().getColor(R.color.colorPrimaryDark));
        dataSet.setCircleRadius(7f);
        dataSet.setCircleHoleRadius(5f);
        LineData line = new LineData(dataSet);


        //format the X labels
        XAxis xAxis = dailyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new XDailyValueFormatter());

        xAxis.setGranularityEnabled(true);
        //xAxis.setGranularity(8.64e+7f);
        xAxis.setGranularity(1f);

        xAxis.setLabelCount(entries.size()); //to show all x labels
        xAxis.setSpaceMax(.3f);

        //format the Y labels
        YAxis yAxis = dailyChart.getAxisLeft();
        yAxis.setValueFormatter(new YDailyValueFormatter());

        dailyChart.setData(line);
        dailyChart.invalidate();


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


        showDailyChart();
    }


    private void setMonthlyChart(){

        if(monthlyRevenues == null)
            return;

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        int i = 0;
        for(int month_idx : monthlyRevenues.keySet())
            entries.add(new BarEntry(month_idx, monthlyRevenues.get(month_idx).floatValue()));

        BarDataSet dataset = new BarDataSet(entries, "");

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(dataset);

        BarData data = new BarData(dataSets);

        dataset.setColors(ColorTemplate.COLORFUL_COLORS);
        data.setBarWidth(0.9f);



        //format the X labels
        XAxis xAxis = monthlyChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new XMonthlyValueFormatter());

        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1f);

        xAxis.setLabelCount(12); //to show all x labels
        xAxis.setSpaceMax(1f);

        xAxis.setAxisMinimum(-1);
        xAxis.setAxisMaximum(12);

        //format the Y labels
        YAxis yAxis = monthlyChart.getAxisLeft();
        yAxis.setValueFormatter(new YMonthlyValueFormatter());



        //remove the right Y label
        monthlyChart.getAxisRight().setEnabled(false);

        //to remove the description from the bottom right corner
        monthlyChart.getDescription().setEnabled(false);
        monthlyChart.getLegend().setEnabled(false);

        //monthlyChart.setVisibleXRange(1, 12);
        //monthlyChart.setVisibleYRange(100, 3000, YAxis.AxisDependency.LEFT);

        //to remove negative values on y axis
        yAxis.setAxisMinimum(0);
        yAxis.setAxisMaximum(1500);
        yAxis.setLabelCount(9);
        yAxis.setGranularity(250f);

        monthlyChart.setScaleEnabled(false);
        monthlyChart.setHorizontalScrollBarEnabled(true);

        monthlyChart.setData(data);
        monthlyChart.invalidate();

        showMonthlyChar();
    }


    /**
     * The formatter for the X values.
     * Format: month / day
     */
    private class XDailyValueFormatter extends ValueFormatter{

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
    private class YDailyValueFormatter extends ValueFormatter{

        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            //y value
            Double v = new Double(value);
            DecimalFormat decimalFormat = new DecimalFormat("#0"); //two decimal
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

    /**
     * The formatter for the X values.
     * Format: <month_string>
     */
    private class XMonthlyValueFormatter extends  ValueFormatter{

        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            int month_idx = (int) value;
            if(month_idx >= 12 || month_idx < 0)
                return "";
            String month = monthList.get(month_idx);
            return month;
        }
    }


    /**
     * The formatter for the Y values.
     * Format: x.xx$
     */
    private class YMonthlyValueFormatter extends ValueFormatter{

        @Override
        public String getAxisLabel(float value, AxisBase axis) {

            //y value
            Double v = new Double(value);
            DecimalFormat decimalFormat = new DecimalFormat("#0"); //two decimal
            double d = Double.parseDouble(v.toString());
            String priceStr = decimalFormat.format(d);
            Integer i = v.intValue();
            Boolean b1 = value % 1 == 0; //must not have decimal part
            Boolean b2 = i % 250 == 0; //must be a multiple of 250
            if(b1 && b2)
                return priceStr+"€";
            return "";
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

    private void showDailyChart(){

        monthlyChart.setVisibility(View.GONE);
        dailyChart.setVisibility(View.VISIBLE);
    }

    private void showMonthlyChar(){

        dailyChart.setVisibility(View.GONE);
        monthlyChart.setVisibility(View.VISIBLE);
    }
}
