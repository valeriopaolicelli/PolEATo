package com.mad.poleato.Statistics;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.GraphViewXML;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

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

    private static final int REVENUE_HOUR = 7;








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

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.fragment_statistics, container, false);

        collectFields();

        fillFields();

        return fragView;
    }


    private void collectFields(){

        tv_Fields.put("riderID", (TextView) fragView.findViewById(R.id.riderID_tv));
        tv_Fields.put("workingDays", (TextView) fragView.findViewById(R.id.workingDays_tv));
        tv_Fields.put("totRevenues", (TextView) fragView.findViewById(R.id.totRevenues_tv));
        tv_Fields.put("totKm", (TextView) fragView.findViewById(R.id.totKm_tv));
        tv_Fields.put("kmPerDay", (TextView) fragView.findViewById(R.id.kmPerDay_tv));
        tv_Fields.put("revenuesPerDay", (TextView) fragView.findViewById(R.id.revenuePerDay_tv));
        tv_Fields.put("revenuesPerHour", (TextView) fragView.findViewById(R.id.revenuePerHour_tv));

        graphView = (GraphView) fragView.findViewById(R.id.graphView);
    }


    private void fillFields(){


        setGraph();

        tv_Fields.get("revenuesPerHour").setText(REVENUE_HOUR+".00 €"); //constant


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


        graphView.setTitle(hostActivity.getString(R.string.chart_title));
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
