package com.mad.poleato;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MainProfile2 extends AppCompatActivity {

    private TextView tvNameField;
    private TextView tvSurnameField;
    private TextView tvAddressField;
    private TextView tvEmailField;
    private TextView tvPhoneField;
    private CircleImageView profileImage; //TODO retrieve image from DB
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_main_profile2);

        tvNameField = findViewById(R.id.tvNameField);
        tvSurnameField= findViewById(R.id.tvSurnameField);
        tvAddressField= findViewById(R.id.tvAddressField);
        tvEmailField= findViewById(R.id.tvEmailField);
        tvPhoneField= findViewById(R.id.tvPhoneField);
        profileImage = findViewById(R.id.profile_image);

        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.account_icon);
            getSupportActionBar().setTitle(R.string.title_bar);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        fillFields();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void fillFields(){
        reference= FirebaseDatabase.getInstance().getReference("customers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot issue= dataSnapshot.child("C00");
                // TODO when log in and sign in will be enabled
                // it is fixed to the first record (customer)
                // when the sign in and log in procedures will be handled, it will be the proper one

                if (dataSnapshot.exists() &&
                        issue.exists() &&
                        issue.hasChild("Name") &&
                        issue.hasChild("Surname") &&
                        issue.hasChild("Address") &&
                        issue.hasChild("Email") &&
                        issue.hasChild("Phone")) {
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
                Toast.makeText(getApplicationContext(), databaseError.getMessage().toString(), Toast.LENGTH_SHORT);
            }
        });
    }


    public void editProfile(MenuItem item){
        Intent i = new Intent(getApplicationContext(), EditProfile.class);
        startActivity(i);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final ScrollView mScrollView = findViewById(R.id.mainScrollView);
        //saving scrollView position
        outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()});
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final ScrollView mScrollView = findViewById(R.id.mainScrollView);
        //restoring scrollview position
        final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
        if(position != null)
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(position[0], position[1]);
                }
            });
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
