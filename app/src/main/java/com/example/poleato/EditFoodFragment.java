package com.example.poleato;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class EditFoodFragment extends DialogFragment {
    private FragmentEListener fragmentEListener;
    private EditText editText;
    private Button button;

    public interface  FragmentEListener {
        void onInputESent(CharSequence input);
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

        editText = v.findViewById(R.id.editDescription);
        button = v.findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence input = editText.getText();
                fragmentEListener.onInputESent(input);
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    public void updateEditText (CharSequence charSequence) {
        editText.setText(charSequence);
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
