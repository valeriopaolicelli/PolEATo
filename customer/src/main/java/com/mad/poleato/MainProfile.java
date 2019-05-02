package com.mad.poleato;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainProfile extends Fragment {

    private TextView tvNameField;
    private TextView tvSurnameField;
    private TextView tvAddressField;
    private TextView tvEmailField;
    private TextView tvPhoneField;
    private CircleImageView profileImage; //TODO retrieve image from DB
    private DatabaseReference reference;

    public MainProfile() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /** Inflate the layout for this fragment */
        View view = inflater.inflate(R.layout.fragment_main_profile, container, false);

        tvNameField = view.findViewById(R.id.tvNameField);
        tvSurnameField= view.findViewById(R.id.tvSurnameField);
        tvAddressField= view.findViewById(R.id.tvAddressField);
        tvEmailField= view.findViewById(R.id.tvEmailField);
        tvPhoneField= view.findViewById(R.id.tvPhoneField);
        profileImage = view.findViewById(R.id.profile_image);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        fillFields();
    }

//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu items for use in the action bar
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.my_menu, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    public void fillFields(){
        reference= FirebaseDatabase.getInstance().getReference("customers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot issue= dataSnapshot.child("C00");
                // TODO when log in and sign in will be enabled
                // it is fixed to the first record (customer)
                // when the sign in and log in procedures will be handled, it will be the proper one

                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children
                    tvNameField.setText(issue.child("Name").getValue().toString());
                    tvSurnameField.setText(issue.child("Surname").getValue().toString());
                    tvAddressField.setText(issue.child("Address").getValue().toString());
                    tvEmailField.setText(issue.child("Email").getValue().toString());
                    tvPhoneField.setText(issue.child("Phone").getValue().toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage().toString(), Toast.LENGTH_SHORT);
            }
        });
    }



    public static Bitmap decodeBase64(String input) {
        byte[] b = Base64.decode(input, Base64.DEFAULT);
        InputStream is = new ByteArrayInputStream(b);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return bitmap;
    }

    public void editProfile(MenuItem item){
        Intent i = new Intent(getContext(), EditProfile.class);
        startActivity(i);
    }


    public static String encodeTobase64( CircleImageView circleImageView) {
        Bitmap image = ((BitmapDrawable) circleImageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

}
