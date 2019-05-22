package com.mad.poleato.Rides;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mad.poleato.R;

import java.util.HashMap;
import java.util.Map;

public class ShowMoreFragment extends DialogFragment {

    private View fragView;
    private String rName;
    private String rAddress;
    private String rPhone;

    Map<String, TextView> tv_Fields;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getArguments();
        rName = b.get("name").toString();
        rAddress = b.get("address").toString();
        rPhone = b.get("phone").toString();

        tv_Fields = new HashMap<>();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        fragView = inflater.inflate(R.layout.show_more_window, container, false);

        tv_Fields.put("name", (TextView) fragView.findViewById(R.id.name_tv));
        tv_Fields.put("address", (TextView) fragView.findViewById(R.id.address_tv));
        tv_Fields.put("phone", (TextView) fragView.findViewById(R.id.phone_tv));

        return fragView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_Fields.get("name").setText(rName);
        tv_Fields.get("address").setText(rAddress);
        tv_Fields.get("phone").setText(rPhone);

    }

}
