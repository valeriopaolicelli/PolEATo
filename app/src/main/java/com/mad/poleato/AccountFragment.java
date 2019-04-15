package com.mad.poleato;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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
        // Data persistency: setting initial values if empty file
        /*SharedPreferences fields= getActivity().getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE);
        if(!fields.contains("Name")) {
            SharedPreferences.Editor editor= getActivity().getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE).edit();
            editor.putString("Name", "Paninos");
            editor.putString("Type", "Pizza, kebab, panini");
            editor.putString("Info", "Locale casual, adatto a coppie e famiglie. Menù anche per vegani!");
            editor.putString("Open", "Lun-Merc 12-24 \nGiovedì chiuso \nDom 12-15 19-24");
            editor.putString("Address", "Via Barge 4");
            editor.putString("Email", "peppe.panino@example.com");
            editor.putString("Phone", "0123456789");
            editor.putString("Background", encodeTobase64());
            editor.apply();
        }

        String name= fields.getString("Name", "Nessun valore trovato");
        String type= fields.getString("Type", "Nessun valore trovato");
        String info= fields.getString("Info", "Nessun valore trovato");
        String open= fields.getString("Open", "Nessun valore trovato");
        String email= fields.getString("Email", "Nessun valore trovato");
        String address= fields.getString("Address", "Nessun valore trovato");
        String phone= fields.getString("Phone", "Nessun valore trovato");
        String image= fields.getString("Background", encodeTobase64());

        // Setting the textView contents with the values stored into SharedPreferences file
        tvNameField.setText(name);
        tvTypeField.setText(type);
        tvInfoField.setText(info);
        tvOpenField.setText(open);
        tvAddressField.setText(address);
        tvEmailField.setText(email);
        tvPhoneField.setText(phone);
        imageBackground.setImageBitmap(decodeBase64(image));*/


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference.child("user").orderByChild("ID");
        reference.child("user").child("Matteo").setValue("[ID, Value]");
        //reference.child("user").child("Matteo").child("ID").setValue("weee");
        Log.d("matte", query.toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children
                    for (DataSnapshot issue : dataSnapshot.getChildren()) {
                        // do something with the individual "issues"
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



        /*tvNameField.setText(name);
        tvTypeField.setText(type);
        tvInfoField.setText(info);
        tvOpenField.setText(open);
        tvAddressField.setText(address);
        tvEmailField.setText(email);
        tvPhoneField.setText(phone);
        imageBackground.setImageBitmap(decodeBase64(image));*/




    }

    public String encodeTobase64() {
        Bitmap image = ((BitmapDrawable)imageBackground.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    public Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
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