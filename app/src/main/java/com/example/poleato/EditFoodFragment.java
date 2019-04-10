package com.example.poleato;

import android.app.Dialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class EditFoodFragment extends DialogFragment {
    private FragmentEListener fragmentEListener;
    private ImageView imageFood;
    private TextView editFood;
    private Spinner spinnerFood;
    private String plateType;
    private EditText editDescription;
    private EditText editPrice;
    private EditText editQuantity;

    private Button buttonSave;

    public interface  FragmentEListener {
        void onInputESent(String plateType, Food food);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.edit_food_fragment, container, false);

        spinnerFood = (Spinner) v.findViewById(R.id.spinnerFood);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.plates_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerFood.setAdapter(adapter);
        spinnerFood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                plateType = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        imageFood = v.findViewById(R.id.imageFood);
        editFood = v.findViewById(R.id.nameFood);
        editDescription = v.findViewById(R.id.editDescription);
        editPrice = v.findViewById(R.id.editPrice);
        editQuantity = v.findViewById(R.id.editQuantity);
        buttonSave = v.findViewById(R.id.button_frag_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Food food = new Food(BitmapFactory.decodeResource(getResources()
                        , imageFood.getId())
                        , editFood.getText().toString()
                        , editDescription.getText().toString()
                        , Double.valueOf(editPrice.getText().toString())
                        , Integer.valueOf(editQuantity.getText().toString()));
//                CharSequence input = editText.getText();
                fragmentEListener.onInputESent(plateType, food);
                dismiss();
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    public void updateEditText (CharSequence charSequence) {
//        editText.setText(charSequence);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof FragmentEListener){
            fragmentEListener = (FragmentEListener) context;
        }else {
            throw new RuntimeException(context.toString() + " must implement FragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentEListener = null;
    }

}
