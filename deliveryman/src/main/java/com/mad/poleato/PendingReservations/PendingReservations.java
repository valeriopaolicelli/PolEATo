package com.mad.poleato.PendingReservations;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mad.poleato.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class PendingReservations extends Fragment {


    public PendingReservations() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pending_reservations, container, false);
    }

}
