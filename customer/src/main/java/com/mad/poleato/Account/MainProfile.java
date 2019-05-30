package com.mad.poleato.Account;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainProfile extends Fragment {
    /**
     * This class...
     */

    private Toast myToast;

    private FloatingActionButton buttEdit;
    private CircleImageView profileImage;
    private DatabaseReference reference;
    private Map<String, TextView> tvFields;
    private View view;

    private ProgressDialog progressDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    private String currentUserID;
    private FirebaseAuth mAuth;

    private HashMap<String, MyDatabaseReference> dbReferenceList;

    public MainProfile() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

// OneSignal is used to send notifications between applications

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        OneSignal.sendTag("User_ID", currentUserID);

        dbReferenceList= new HashMap<>();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.popup_account_settings, menu);
        menu.findItem(R.id.logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //logout
                for (MyDatabaseReference my_ref : dbReferenceList.values())
                    my_ref.removeAllListener();
                Log.d("matte", "Logout");
                FirebaseAuth.getInstance().signOut();
                //                OneSignal.sendTag("User_ID", "");
                OneSignal.setSubscription(false);

                /**
                 *  GO TO LOGIN ****
                 */
                Navigation.findNavController(view).navigate(R.id.action_mainProfile_id_to_signInActivity);
                getActivity().finish();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /** Inflate the layout for this fragment */
        view = inflater.inflate(R.layout.fragment_main_profile, container, false);
        // Retrieve all fields (restaurant details) in the xml file
        tvFields = new HashMap<>();
        tvFields.put("Name", (TextView) view.findViewById(R.id.tvNameField));
        tvFields.put("Surname", (TextView) view.findViewById(R.id.tvSurnameField));
        tvFields.put("Address", (TextView) view.findViewById(R.id.tvAddressField));
        tvFields.put("Email", (TextView) view.findViewById(R.id.tvEmailField));
        tvFields.put("Phone", (TextView) view.findViewById(R.id.tvPhoneField));

        profileImage = view.findViewById(R.id.profile_image);

        // Button to edit the restaurant details
        buttEdit = view.findViewById(R.id.buttEdit);
        buttEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /**
                 * GO TO EDIT_PROFILE_FRAGMENT
                 */
                Navigation.findNavController(v).navigate(R.id.action_mainProfile_id_to_editProfile_id);
            }
        });
        return view;
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

        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("customers/" + currentUserID);
        dbReferenceList.put("customer", new MyDatabaseReference(reference));

        dbReferenceList.get("customer").setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // dataSnapshot is the "issue" node with all children

                if (dataSnapshot.hasChild("Name") &&
                        dataSnapshot.hasChild("Surname") &&
                        dataSnapshot.hasChild("Address") &&
                        dataSnapshot.hasChild("Email") &&
                        dataSnapshot.hasChild("Phone")) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if (tvFields.containsKey(snap.getKey())) {

                            tvFields.get(snap.getKey()).setText(snap.getValue().toString());
                        }
                    } //end for
                } //end if

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d("matte", "onCancelled | ERROR: " + databaseError.getDetails() +
//                        " | MESSAGE: " + databaseError.getMessage());
//                myToast.setText(databaseError.getMessage().toString());
//                myToast.show();
            }
        });

        //Download the profile pic
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference= storageReference.child(currentUserID +"/ProfileImage/img.jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(bmp);
                //send message to main thread
                if(progressDialog.isShowing())
                    handler.sendEmptyMessage(0);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("matte", "No image found. Default img setting");
                //set predefined image
                profileImage.setImageResource(R.drawable.image_empty);
                //send message to main thread
                if(progressDialog.isShowing())
                    handler.sendEmptyMessage(0);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(MyDatabaseReference ref : dbReferenceList.values())
            ref.removeAllListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        for (MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }
}
