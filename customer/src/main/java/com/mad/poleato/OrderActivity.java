package com.mad.poleato;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;


public class OrderActivity extends AppCompatActivity implements Interface {

    private ViewPager onViewPager;
    private PageAdapter adapter;
    private Order order;
    private DatabaseReference dbReferece;

    /* *************************
    ********* FRAGMENTS ********
    *************************** */

    private MenuFragment menuFragment;
    private InfoFragment infoFragment;

    private void addFragmentToAdapter(Bundle bundle){
        infoFragment = null;
        menuFragment = null;

        FragmentManager fm = getSupportFragmentManager();
        adapter = new PageAdapter(fm);
        List<Fragment> fragmentList = fm.getFragments();
        if(fragmentList != null){
            for (int idx = 0; idx < fragmentList.size(); idx++){

                if( fragmentList.get(idx) instanceof MenuFragment)
                    menuFragment = (MenuFragment) fragmentList.get(idx);
                if( fragmentList.get(idx) instanceof InfoFragment)
                    infoFragment = (InfoFragment) fragmentList.get(idx);
            }
        }
        if(menuFragment == null){
            menuFragment = new MenuFragment();
            menuFragment.setArguments(bundle);
        }
        if(infoFragment == null){
            infoFragment = new InfoFragment();
            infoFragment.setArguments(bundle);
        }
        adapter.addFragment(menuFragment, "Menu");
        adapter.addFragment(infoFragment, "Info");

        onViewPager.setAdapter(adapter);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_layout);

        order = new Order();
        //gettin id of restaurant selected by user
        Bundle bundle = getIntent().getExtras();

        Locale locale = Locale.getDefault();
        // get "en" or "it"
        final String localeShort = locale.toString().substring(0, 2);

        order.setRestaurantID(bundle.getString("id"));
        dbReferece = FirebaseDatabase.getInstance().getReference("restaurants").child(order.getRestaurantID());
        dbReferece.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String id = dataSnapshot.getKey();
                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.image_empty); // TODO: make it dynamic
                String name = dataSnapshot.child("Name").getValue().toString();
                String type = dataSnapshot.child("Type").child(localeShort).getValue().toString();
                Boolean isOpen = (Boolean) dataSnapshot.child("IsActive").getValue();
                int priceRange = Integer.parseInt(dataSnapshot.child("PriceRange").getValue().toString());
                double deliveryCost = Double.parseDouble(dataSnapshot.child("DeliveryCost").getValue().toString().replace(",", "."));

                Restaurant resObj = new Restaurant(id, img, name, type, isOpen, priceRange, deliveryCost);

                order.setR(resObj);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar_order);
        TabLayout tabLayout = findViewById(R.id.tabs);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        } else {
            Log.d("Error", "getSupportActionBar is null");
            finish();
        }
        onViewPager = findViewById(R.id.container);
        addFragmentToAdapter(bundle);

        tabLayout.setupWithViewPager(onViewPager);
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.cart_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void goToCart(MenuItem item) {
        Intent intent = new Intent(this,CartActivity.class);
        intent.putExtra("order",order);

        startActivityForResult(intent,1);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        if(requestCode ==1){
            if(resultCode == Activity.RESULT_OK)
                finish();
            else if(resultCode == Activity.RESULT_CANCELED){
                order = (Order) data.getExtras().getSerializable("old_order");
            }
        }
    }

    public void setOrder(Order order){
        this.order=order;
    }

    @Override
    public Order getOrder() {
        return order;
    }
}