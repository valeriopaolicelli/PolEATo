package com.mad.poleato;

import android.content.Intent;
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

import java.util.List;


public class OrderActivity extends AppCompatActivity implements Interface {

    private ViewPager onViewPager;
    private PageAdapter adapter;
    private Order order;

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
        order.setRestaurantID(bundle.getString("id"));
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

    public void goToChart(MenuItem item) {
        Intent intent = new Intent(this,CartActivity.class);
        intent.putExtra("order",order);
        startActivity(intent);
    }


    public void setOrder(Order order){
        this.order=order;
    }

    @Override
    public Order getOrder() {
        return order;
    }
}
