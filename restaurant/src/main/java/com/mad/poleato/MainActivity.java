package com.mad.poleato;

import android.accounts.Account;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DailyOfferFragment.FragmentShowAddListener,
        DailyOfferFragment.FragmentShowEditListener, AddFoodFragment.FragmentAddListener,
        EditFoodFragment.FragmentEditListener {

    private ViewPager onViewPager;
    private PageAdapter adapter;



    

    /* ********************************
     ********   FRAGMENTS    **********
     ******************************** */
    private AccountFragment accountFragment;
    private ReservationFragment reservationFragment;
    private DailyOfferFragment dailyOfferFragment;
    private AddFoodFragment addFoodFragment;
    private EditFoodFragment editFoodFragment;



    private void addFragmentToAdapter(){
        //init
        accountFragment = null;
        reservationFragment = null;
        dailyOfferFragment = null;

        //get the fragments list
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        adapter = new PageAdapter(fm);
        List<Fragment> fragmentList = fm.getFragments();
        if(fragmentList != null) {

            for (int idx = 0; idx < fragmentList.size(); idx++) {

                if (fragmentList.get(idx) instanceof AccountFragment)
                    accountFragment = (AccountFragment) fragmentList.get(idx);
                if (fragmentList.get(idx) instanceof ReservationFragment)
                    reservationFragment = (ReservationFragment) fragmentList.get(idx);
                if (fragmentList.get(idx) instanceof DailyOfferFragment)
                    dailyOfferFragment = (DailyOfferFragment) fragmentList.get(idx);
            }
        }

        if(accountFragment == null){
            accountFragment = new AccountFragment();
            Log.d("matte", "AccountFragment not found and recreated");
        }
        if(reservationFragment == null){
            reservationFragment = new ReservationFragment();
            Log.d("matte", "ReservationFragment not found and recreated");
        }
        if(dailyOfferFragment == null){
            dailyOfferFragment = new DailyOfferFragment();
            Log.d("matte", "DailyOfferFragment not found and recreated");
        }

        adapter.addFragment(accountFragment, "Account");
        adapter.addFragment(reservationFragment, "Reservation");
        adapter.addFragment(dailyOfferFragment, "DailyOffer");

        onViewPager.setAdapter(adapter);
    }



    /**
     *
     * Callback from DailyOfferFragment to show the AddFoodFragment
     */
    @Override
    public void onInputShowAddSent(Object o) {
//        getSupportFragmentManager().beginTransaction()
//                .add(addFoodFragment, "Blank Fragment")
//                .setTransitionStyle(R.style.FullScreenDialog)
//                .addToBackStack(null)
//                .commit();
        addFoodFragment = new AddFoodFragment();
        addFoodFragment.show(getSupportFragmentManager(), "addFoodFragment");
    }


    /**
     * Callback from DailyOfferFragment to show the EditFoodFragment
     * @param foodToModify
     */
    @Override
    public void onInputShowEditSent(Food foodToModify) {
        editFoodFragment = new EditFoodFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("foodToModify", foodToModify);
        editFoodFragment.setArguments(bundle);
        editFoodFragment.show(getSupportFragmentManager(), "editFoodFragment");
    }



    /**
     * Callback from AddFoodFragment to notify new Food item to dailyOfferFragment
     * @param plateType
     * @param food
     */
    @Override
    public void onInputAddSent(String plateType, Food food) {
        dailyOfferFragment.addFood(plateType, food);
    }

    /**
     *
     */
    @Override
    public void onInputEditSent() {
        dailyOfferFragment.notifyDataChange();
    }


    /* ***********************************
       ********   ANDROID CALLBACKS   ****
       *********************************** */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TabLayout tabLayout = findViewById(R.id.tabs);


        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setIcon(R.mipmap.delivery_icon);
        } else {
            Log.d("Error", "getSupportActionBar is null");
            finish();
        }
        onViewPager = findViewById(R.id.container);
        addFragmentToAdapter();

        tabLayout.setupWithViewPager(onViewPager);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}