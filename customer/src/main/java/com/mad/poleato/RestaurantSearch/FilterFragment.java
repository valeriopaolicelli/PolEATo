package com.mad.poleato.RestaurantSearch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.mad.poleato.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * This is the fragment showing the filter dialog
 */
public class FilterFragment extends DialogFragment {

    View fragview;

    Set<String> checkedType;
    List<CheckBox> checkBoxes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragview = inflater.inflate(R.layout.filter_window, container, false);

        checkBoxes = new ArrayList<>();
        checkedType = new HashSet<String>();

        checkBoxes.add((CheckBox) fragview.findViewById(R.id.italianCheckBox));
        checkBoxes.add((CheckBox) fragview.findViewById(R.id.pizzaCheckBox));
        checkBoxes.add((CheckBox) fragview.findViewById(R.id.kebabCheckBox));
        checkBoxes.add((CheckBox) fragview.findViewById(R.id.chineseCheckBox));
        checkBoxes.add((CheckBox) fragview.findViewById(R.id.japaneseCheckBox));
        checkBoxes.add((CheckBox) fragview.findViewById(R.id.thaiCheckBox));
        checkBoxes.add((CheckBox) fragview.findViewById(R.id.hamburgerCheckBox));
        checkBoxes.add((CheckBox) fragview.findViewById(R.id.americanCheckBox));
        checkBoxes.add((CheckBox) fragview.findViewById(R.id.mexicanCheckBox));

        Bundle bundle = getArguments();
        checkedType = (HashSet<String>)bundle.getSerializable("checkbox_state");
        for(String s: checkedType)
            for(CheckBox cb : checkBoxes)
                if(cb.getText().toString().toLowerCase().equals(s))
                    cb.setChecked(true);
        return fragview;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        CheckListener cListener = new CheckListener();

        for(CheckBox cb : checkBoxes)
            cb.setOnCheckedChangeListener(cListener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("matte", "[FILTER FRAGMENT] OnDestroy");

        HashMap<String, String> e = new HashMap<>();
        Intent i = new Intent().putExtra("checked_types", (HashSet<String>)checkedType);
        getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, i);
        dismiss();
    }

    private class CheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String thisFoodType = buttonView.getText().toString().toLowerCase();

            if(isChecked)
                checkedType.add(thisFoodType);
            else
                checkedType.remove(thisFoodType);
        }
    }

}
