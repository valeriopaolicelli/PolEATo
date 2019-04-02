package com.example.poleato;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by User on 2/28/2017.
 */

public class ReservationFragment extends Fragment {
    private static final String TAG = "Tab1Fragment";

    private Button btnOffNav;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout,container,false);
        btnOffNav = view.findViewById(R.id.btnNavOffFrag);

        btnOffNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "Go to offer Frag",Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).setViewPager(1);
            }
        });

        return view;
    }
}