package com.mad.poleato.Account;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.poleato.AuthentucatorD.Authenticator;
import com.mad.poleato.Firebase.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class MainProfile extends Fragment {

    private Toast myToast;

    private Map<String, TextView> tvFields;

    private FloatingActionButton buttEdit;
    private ImageView profileImage;

    private ProgressDialog progressDialog;


    private View view;
    private String currentUserID;
    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    private MyDatabaseReference deliveryProfileReference;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //in order to create the logout menu (don't move!)
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getActivity() != null)
            myToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        deliveryProfileReference = new MyDatabaseReference(FirebaseDatabase.getInstance()
                .getReference("deliveryman/" + currentUserID));

        /** GoogleSignInOptions */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        /** Build a GoogleSignInClient with the options specified by gso. */
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }


    private void logout() {
        //logout
        Log.d("matte", "Logout");
        FirebaseAuth.getInstance().signOut();
        OneSignal.setSubscription(false);

        //go to login
        Navigation.findNavController(view).navigate(R.id.action_mainProfile_id_to_signInActivity);
        getActivity().finish();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.popup_account_settings, menu);
        menu.findItem(R.id.logout).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                deliveryProfileReference.setSingleValueListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("Busy") && dataSnapshot.hasChild("IsActive")) {
                            Boolean busy = (Boolean) dataSnapshot.child("Busy").getValue();
                            if (!busy) {

                                FirebaseDatabase.getInstance().getReference("deliveryman/")
                                        .child(currentUserID+"/IsActive").setValue(false); //set inactive
                            }

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                deliveryProfileReference.removeAllListener();

                /** logout */
                Authenticator.revokeAccess(getActivity(), view);
                return true;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        /** Logout a priori if access is revoked */
        if (currentUserID == null)
            Authenticator.revokeAccess(Objects.requireNonNull(getActivity()), view);

        // Retrieve all fields (restaurant details) in the xml file
        tvFields = new HashMap<>();
        tvFields.put("Name", (TextView) view.findViewById(R.id.tvNameField));
        tvFields.put("Surname", (TextView) view.findViewById(R.id.tvSurnameField));
        tvFields.put("Address", (TextView) view.findViewById(R.id.tvAddressField));
        tvFields.put("Email", (TextView) view.findViewById(R.id.tvEmailField));
        tvFields.put("Phone", (TextView) view.findViewById(R.id.tvPhoneField));
        tvFields.put("ID", (TextView) view.findViewById(R.id.tvIdField));
        tvFields.put("IsActive", (TextView) view.findViewById(R.id.tvStatusField));

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

    private void fillFields() {

        deliveryProfileReference.setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                // dataSnapshot is the "issue" node with all children

                if(dataSnapshot.hasChild("Email"))
                    tvFields.get("Email").setText(dataSnapshot.child("Email").getValue().toString());

                if (dataSnapshot.hasChild("Name") &&
                        dataSnapshot.hasChild("Surname") &&
                        dataSnapshot.hasChild("Address") &&
                        dataSnapshot.hasChild("Phone") &&
                        dataSnapshot.hasChild("IsActive")) {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        if (tvFields.containsKey(snap.getKey())) {

                            if (snap.getKey().equals("IsActive") && getActivity() != null) {
                                if ((Boolean) snap.getValue())
                                    tvFields.get(snap.getKey()).setText(getString(R.string.active_status));
                                else
                                    tvFields.get(snap.getKey()).setText(getString(R.string.inactive_status));
                            } else
                                tvFields.get(snap.getKey()).setText(snap.getValue().toString());
                        }
                    } //end for
                } //end if

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte_logout", "onCancelled | ERROR: " + databaseError.getDetails() +
                        " | MESSAGE: " + databaseError.getMessage());
                myToast.setText(databaseError.getMessage().toString());
                myToast.show();
            }
        });


        //Download the profile pic
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference = storageReference.child(currentUserID + "/ProfileImage/img.jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(bmp);
                //send message to main thread
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("matte", "No image found. Default img setting");
                //set predefined image
                profileImage.setImageResource(R.drawable.image_empty);
                //send message to main thread
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });

    }

//    private void revokeAccess() {
//        // Firebase sign out
//        //mAuth.signOut();
//
//        Log.d("miche", "Logout");
//        FirebaseAuth.getInstance().signOut();
//        // Google revoke access
//        mGoogleSignInClient.revokeAccess();
//
//        OneSignal.setSubscription(false);
//
//        /**
//         *  GO TO LOGIN ****
//         */
//        Navigation.findNavController(view).navigate(R.id.action_mainProfile_id_to_signInActivity);
//        getActivity().finish();
//    }


    @Override
    public void onStop() {
        super.onStop();
        deliveryProfileReference.removeAllListener();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        deliveryProfileReference.removeAllListener();
    }
}

