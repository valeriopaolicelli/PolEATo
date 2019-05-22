package com.mad.poleato.History;


import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mad.poleato.FirebaseData.MyFirebaseData;
import com.mad.poleato.R;
import com.mad.poleato.View.ViewModel.MyViewModel;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private View view;


    //auth
    private String currentUserID;
    private FirebaseAuth mAuth;


    public HistoryFragment() {
        // Required empty public constructor
    }

    private Toast myToast;

    private Activity hostActivity;
    private View fragView;
    private HistoryRecyclerViewAdapter historyAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;

    private MyViewModel model;

    private ProgressDialog progressDialog;



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
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);
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

        if (getActivity() != null) {
            progressDialog = ProgressDialog.show(getActivity(), "", hostActivity.getString(R.string.loading));
        }



    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.history_recyclerview, container, false);

        /**myFirebaseData.fillFieldsHistory();*/

        rv = (RecyclerView) fragView.findViewById(R.id.history_recyclerview);
        rv.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.historyAdapter = new HistoryRecyclerViewAdapter(this.hostActivity);
        rv.setAdapter(historyAdapter);
        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

        return fragView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /**myFirebaseData.fillFieldsHistory();*/

        /** Listeners to update UI Expandable list from VIEW_MODEL list child */
        model = ViewModelProviders.of(getActivity()).get(MyViewModel.class);
        model.getListH().observe(this, new Observer<HashMap<String, HistoryItem>>() {
            @Override
            public void onChanged(@Nullable HashMap<String, HistoryItem> stringHistoryHashMap) {
                if(stringHistoryHashMap.values() != null)
                    historyAdapter.setAllHistories( new ArrayList<HistoryItem>(stringHistoryHashMap.values()));
                    if(progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
            }
        });
    }
}
