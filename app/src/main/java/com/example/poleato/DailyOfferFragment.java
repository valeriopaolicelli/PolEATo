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

public class DailyOfferFragment extends Fragment {
    private static final String TAG = "Tab1Fragment";

    private Button btnTEST;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.offer_frag_layout,container,false);
        btnTEST = (Button) view.findViewById(R.id.btnNavOffFrag);

        btnTEST.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "TESTING BUTTON DAILY OFFER",Toast.LENGTH_SHORT).show();
                ((MainActivity)getActivity()).setViewPager(2);
            }
        });

        return view;
    }
}