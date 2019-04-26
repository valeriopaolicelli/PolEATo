package com.mad.poleato.DailyOffer;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.BitmapFactory;
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

import com.mad.poleato.DailyOffer.ExpandableListManagement.ExpandableListAdapter;
import com.mad.poleato.R;
import com.mad.poleato.View.ViewModel.MyViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/*
 * A simple {@link Fragment} subclass.
 * Use the {@link DailyOfferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DailyOfferFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private View fragView;
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private List<String> listDataGroup;
    private HashMap<String, List<Food>> listDataChild;
    private FloatingActionButton floatingActionButton;
    private Display display;
    private Point size;
    int width;
    private int lastExpandedPosition = -1;

    private MyViewModel model;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public DailyOfferFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DailyOfferFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DailyOfferFragment newInstance(String param1, String param2) {
        DailyOfferFragment fragment = new DailyOfferFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        /** Calculate position of ExpandableListView indicator. */
        display = getActivity().getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        width = size.x;

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
                /**
                 *
                 */
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

        model.prepareListData(getContext());

    }



    private int GetDipsFromPixel(float pixels){
        /** Get the screen's density scale */
        final float scale = getResources().getDisplayMetrics().density;
        /** Convert the dps to pixels, based on density scale */
        return (int) (pixels * scale + 0.5f);
    }

}
