package com.mad.poleato.DailyOffer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mad.poleato.DailyOffer.ExpandableListManagement.ExpandableListAdapter;
import com.mad.poleato.NavigatorActivity;
import com.mad.poleato.R;
import com.mad.poleato.View.ViewModel.MyViewModel;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.List;


/**
 * The fragment to show the restaurant menu
 */
public class DailyOfferFragment extends Fragment {


    private View fragView;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private FloatingActionButton floatingActionButton;
    private Display display;
    private Point size;
    int width;
    private int lastExpandedPosition = -1;

    private MyViewModel model;


    private String currentUserID;
    private FirebaseAuth mAuth;

    public DailyOfferFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getActivity()!=null)
            NavigatorActivity.hideKeyboard(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /** Calculate position of ExpandableListView indicator. */
        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();


        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);

        /** Listeners to update UI Expandable list from VIEW_MODEL list group and child*/
        model = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model.getListG().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(@Nullable List<String> strings) {
                listAdapter.setAllGroup(strings);
            }
        });
        model.getListC().observe(this, new Observer<HashMap<String, List<Food>>>() {
            @Override
            public void onChanged(@Nullable HashMap<String, List<Food>> stringListHashMap) {
                listAdapter.setAllChild(stringListHashMap);
            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /** Inflate layout */
        fragView = inflater.inflate(R.layout.dailyoffer_frag_layout, container, false);
        /** Get the listview */
        expListView = (ExpandableListView) fragView.findViewById(R.id.menuList);
        /** fix position of ExpandableListView indicator. */
        expListView.setIndicatorBounds(width-GetDipsFromPixel(35), width-GetDipsFromPixel(5));

        floatingActionButton = fragView.findViewById(R.id.floatingButton);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * GO TO ADD_FOOD_FRAGMENT
                 */
                Navigation.findNavController(v).navigate(R.id.action_daily_offer_to_addFoodFragment);

            }
        });

        /** list Adapter of ExpandableList */
        listAdapter = new ExpandableListAdapter(getActivity());

        /** setting list adapter */
        expListView.setAdapter(listAdapter);

        return fragView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        /** to collapse all groups except the one tapped */
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedPosition != -1
                        && groupPosition != lastExpandedPosition) {
                    expListView.collapseGroup(lastExpandedPosition);
                }
                lastExpandedPosition = groupPosition;
            }
        });

        //download the menu to display
        model.downloadMenu(getActivity());

    }


    private int GetDipsFromPixel(float pixels){
        /** Get the screen's density scale */
        final float scale = getResources().getDisplayMetrics().density;
        /** Convert the dps to pixels, based on density scale */
        return (int) (pixels * scale + 0.5f);
    }

    @Override
    public void onStop() {
        super.onStop();
       // model.detachListeners();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
      //  model.detachListeners();
    }
}
