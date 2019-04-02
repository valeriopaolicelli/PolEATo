package com.example.poleato;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by User on 2/28/2017.
 */

public class AccountFragment extends Fragment {
    private static final String TAG = "Tab1Fragment";

    private Button btnTEST;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_frag, container,false);
        btnTEST = (Button) view.findViewById(R.id.btnAccountFrag);

        btnTEST.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "TESTING BUTTON ACCOUNT",Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}