package com.mad.poleato;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;

public class AccountFragment extends Fragment {

    private TextView tvNameField;
    private TextView tvTypeField;
    private TextView tvInfoField;
    private TextView tvOpenField;
    private TextView tvAddressField;
    private TextView tvEmailField;
    private TextView tvPhoneField;
    private FloatingActionButton buttEdit;
    private ImageView imageBackground;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_frag_layout, container,false);

        // Retrieve all fields (restaurant details) in the xml file

        tvNameField = view.findViewById(R.id.tvNameField);
        tvTypeField = view.findViewById(R.id.tvTypeField);
        tvInfoField = view.findViewById(R.id.tvInfoField);
        tvOpenField = view.findViewById(R.id.tvOpenField);
        tvAddressField = view.findViewById(R.id.tvAddressField);
        tvEmailField = view.findViewById(R.id.tvEmailField);
        tvPhoneField = view.findViewById(R.id.tvPhoneField);
        imageBackground = view.findViewById(R.id.ivBackground2);

        // Button to edit the restaurant details
        buttEdit= view.findViewById(R.id.buttEdit);
        buttEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // new activity -> EditProfile
                Intent i = new Intent(v.getContext(),EditProfile.class);
                startActivity(i);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        //fill the views fields
        fillFields();
    }

    public void collectFields(){

    }

    public void fillFields(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                        tvNameField.setText(issue.child("Name").getValue().toString());
                        tvAddressField.setText(issue.child("Address").getValue().toString());
                        tvEmailField.setText(issue.child("Email").getValue().toString());
                        tvInfoField.setText(issue.child("Description").getValue().toString());
                        tvOpenField.setText(issue.child("Open").getValue().toString());
                        tvTypeField.setText(issue.child("Type").getValue().toString());
                        tvPhoneField.setText(issue.child("Phone").getValue().toString());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage().toString(), Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

//        final ScrollView mScrollView = getView().findViewById(R.id.mainScrollView);
//        //saving scrollView position when rotate the screen
//        outState.putIntArray("ARTICLE_SCROLL_POSITION",
//                new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()});
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ScrollView mScrollView = getView().findViewById(R.id.mainScrollView);
        //restoring scrollview position
//        final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
//        if (position != null) {
//            mScrollView.post(new Runnable() {
//                public void run() {
//                    mScrollView.scrollTo(position[0], position[1]);
//                }
//            });
//        }

    }

}