package com.example.poleato;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    private ViewPager onViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar= findViewById(R.id.toolbar);
        TabLayout tabLayout= findViewById(R.id.tabs);


        setSupportActionBar(toolbar);
        if(getSupportActionBar()!= null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        else{
            Log.d("Error", "getSupportActionBar is null");
            finish();
        }
        onViewPager = findViewById(R.id.container);
        addFragmentToAdapter();

        tabLayout.setupWithViewPager(onViewPager);
    }

    private void addFragmentToAdapter(){
        PageAdapter adapter = new PageAdapter(getSupportFragmentManager());
        adapter.addFragment(new AccountFragment(), "Account");
        adapter.addFragment(new ReservationFragment(), "Reservation");
        adapter.addFragment(new DailyOfferFragment(), "DailyOffer");
        onViewPager.setAdapter(adapter);
    }

}
