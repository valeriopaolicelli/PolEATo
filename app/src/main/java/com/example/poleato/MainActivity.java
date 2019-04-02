package com.example.poleato;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    private ViewPager onViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar= (Toolbar) findViewById(R.id.toolbar);
        TabLayout tabLayout= (TabLayout) findViewById(R.id.tabs);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        onViewPager = findViewById(R.id.container);
        setUpViewPager(onViewPager);

        tabLayout.setupWithViewPager(onViewPager);
    }

    private void setUpViewPager( ViewPager pager){
        PageAdapter adapter = new PageAdapter(getSupportFragmentManager());
        adapter.addFragment(new AccountFragment(), "Account");
        adapter.addFragment(new ReservationFragment(), "Reservation");
        adapter.addFragment(new DailyOfferFragment(), "DailyOffer");
        onViewPager.setAdapter(adapter);
    }

}
