package com.example.poleato;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewParent;

public class MainActivity extends AppCompatActivity {

    private PageAdapter onPageAdapter;
    private ViewPager onViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onPageAdapter = new PageAdapter(getSupportFragmentManager());
        onViewPager = findViewById(R.id.container);

        setUpViewPager(onViewPager);


        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Your Restaurant");
        }

    }
    private void setUpViewPager( ViewPager pager){
        PageAdapter adapter = new PageAdapter(getSupportFragmentManager());
        adapter.addFragment(new ReservationFragment(), "Reservation");
        adapter.addFragment(new DailyOfferFragment(), "DailyOffer");
        adapter.addFragment(new AccountFragment(), "Account");
        onViewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragNumber){
        onViewPager.setCurrentItem(fragNumber);
    }
}
