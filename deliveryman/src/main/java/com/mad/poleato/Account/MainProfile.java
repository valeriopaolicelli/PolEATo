package com.mad.poleato.Account;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mad.poleato.R;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainProfile extends Fragment {

    private Toast myToast;

    private TextView tvNameField;
    private TextView tvSurnameField;
    private TextView tvAddressField;
    private TextView tvEmailField;
    private TextView tvPhoneField;
    private TextView tvIdField;
    private CircleImageView profileImage;
    private DatabaseReference reference;
    private FloatingActionButton buttEdit;

    private String currentUserID;
    private FirebaseAuth mAuth;

    private ProgressDialog progressDialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };


    public MainProfile() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragView = inflater.inflate(R.layout.fragment_profile, container, false);

        tvNameField = fragView.findViewById(R.id.tvNameField);
        tvSurnameField = fragView.findViewById(R.id.tvSurnameField);
        tvAddressField = fragView.findViewById(R.id.tvAddressField);
        tvEmailField = fragView.findViewById(R.id.tvEmailField);
        tvPhoneField = fragView.findViewById(R.id.tvPhoneField);
        tvIdField = fragView.findViewById(R.id.tvIdField);
        profileImage = fragView.findViewById(R.id.profile_image);


        // Button to edit the restaurant details
        buttEdit = fragView.findViewById(R.id.buttEdit);
        buttEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * GO TO EDIT_PROFILE_FRAGMENT
                 */
                Navigation.findNavController(v).navigate(R.id.action_mainProfile_id_to_editProfile_id);
            }
        });

        return fragView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //fill the views fields
        if (getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

        //start a new thread to process job
        fillFields();

    }

    public void fillFields() {

        reference= FirebaseDatabase.getInstance().getReference("deliveryman");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot issue= dataSnapshot.child(currentUserID);
                // it is fixed to the first record (customer)
                // when the sign in and log in procedures will be handled, it will be the proper one

                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children
                    tvNameField.setText(issue.child("Name").getValue().toString());
                    tvSurnameField.setText(issue.child("Surname").getValue().toString());
                    tvAddressField.setText(issue.child("Address").getValue().toString());
                    tvEmailField.setText(issue.child("Email").getValue().toString());
                    tvPhoneField.setText(issue.child("Phone").getValue().toString());
                    tvIdField.setText(issue.child("ID").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte", "onCancelled | ERROR: " + databaseError.getDetails() +
                        " | MESSAGE: " + databaseError.getMessage());
                myToast.setText(databaseError.getMessage().toString());
                myToast.show();
            }
        });
    }
}
