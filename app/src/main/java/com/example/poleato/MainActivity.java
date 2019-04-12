package com.example.poleato;

import android.accounts.Account;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

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


    private void addFragmentToAdapter() {
        adapter = new PageAdapter(getSupportFragmentManager());
        adapter.addFragment(new AccountFragment(), "Account");
        adapter.addFragment(new ReservationFragment(), "Reservation");
        adapter.addFragment(new DailyOfferFragment(), "DailyOffer");
        onViewPager.setAdapter(adapter);

        // set the local attributes to easily access
        accountFragment = (AccountFragment) adapter.getItem(0);
        reservationFragment = (ReservationFragment) adapter.getItem(1);
        dailyOfferFragment = (DailyOfferFragment) adapter.getItem(2);
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
        addFoodFragment.show(getSupportFragmentManager(), "addFoodFragment");
    }


    /**
     * Callback from DailyOfferFragment to show the EditFoodFragment
     * @param foodToModify
     */
    @Override
    public void onInputShowEditSent(Food foodToModify) {
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

        addFoodFragment = new AddFoodFragment();
        editFoodFragment = new EditFoodFragment();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }


}
