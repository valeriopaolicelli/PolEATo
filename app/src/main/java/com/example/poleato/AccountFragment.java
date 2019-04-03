package com.example.poleato;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

public class AccountFragment extends Fragment {

    private TextView tvNameField;
    private TextView tvTypeField;
    private TextView tvInfoField;
    private TextView tvOpenField;
    private TextView tvAddressField;
    private TextView tvEmailField;
    private TextView tvPhoneField;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_frag_layout, container,false);

        tvNameField = view.findViewById(R.id.tvNameField);
        tvTypeField = view.findViewById(R.id.tvTypeField);
        tvInfoField = view.findViewById(R.id.tvInfoField);
        tvOpenField = view.findViewById(R.id.tvOpenField);
        tvAddressField= view.findViewById(R.id.tvAddressField);
        tvEmailField= view.findViewById(R.id.tvEmailField);
        tvPhoneField= view.findViewById(R.id.tvPhoneField);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //fill the views fields
        fillFields();
    }

    public void fillFields(){
        // Data persistency: setting initial values if empty file
        SharedPreferences fields= getActivity().getSharedPreferences("ProfileDataRestaurateur", Context.MODE_PRIVATE);
        if(!fields.contains("Name")) {
            SharedPreferences.Editor editor= getActivity().getSharedPreferences("ProfileDataRestaurateur", Context.MODE_PRIVATE).edit();
            editor.putString("Name", "Paninos");
            editor.putString("Type", "Pizza, kebab, panini");
            editor.putString("Info", "Locale casual, adatto a coppie e famiglie. Menù anche per vegani!");
            editor.putString("Open", "Lun-Merc 12-24 \nGiovedì chiuso \nDom 12-15 19-24");
            editor.putString("Address", "Via Barge 4");
            editor.putString("Email", "peppe.panino@example.com");
            editor.putString("Phone", "0123456789");
            editor.apply();
        }

        String name= fields.getString("Name", "Nessun valore trovato");
        String type= fields.getString("Type", "Nessun valore trovato");
        String info= fields.getString("Info", "Nessun valore trovato");
        String open= fields.getString("Open", "Nessun valore trovato");
        String email= fields.getString("Email", "Nessun valore trovato");
        String address= fields.getString("Address", "Nessun valore trovato");
        String phone= fields.getString("Phone", "Nessun valore trovato");


        // Setting the textView contents with the values stored into SharedPreferences file
        tvNameField.setText(name);
        tvTypeField.setText(type);
        tvInfoField.setText(info);
        tvOpenField.setText(open);
        tvAddressField.setText(address);
        tvEmailField.setText(email);
        tvPhoneField.setText(phone);

    }
/*

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final ScrollView mScrollView = getView().findViewById(R.id.mainScrollView);
        //saving scrollView position when rotate the screen
        outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()});
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final ScrollView mScrollView = getView().findViewById(R.id.mainScrollView);
        //restoring scrollview position
        final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
        if(position != null)
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(position[0], position[1]);
                }
            });
    }*/
}