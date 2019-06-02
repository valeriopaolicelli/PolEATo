package com.mad.poleato;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.onesignal.OneSignal;

import static com.google.android.gms.common.util.CollectionUtils.setOf;

public class NavigatorActivity extends AppCompatActivity{
    BottomNavigationView navigation;
    private NavController navController;
    private FirebaseAuth mAuth;
    private String currentUserID;
    ConnectionManager connectionManager;


//    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
//            = new BottomNavigationView.OnNavigationItemSelectedListener() {
//
//        @Override
//        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.navigation_home:
//                    mTextMessage.setText(R.string.title_home);
//                    return true;
//                case R.id.navigation_dashboard:
//                    mTextMessage.setText(R.string.title_dashboard);
//                    return true;v
//                case R.id.navigation_notifications:
//                    mTextMessage.setText(R.string.title_notifications);
//                    return true;
//            }
//            return false;
//        }
//    };

    BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(!connectionManager.haveNetworkConnection(context))
                connectionManager.showDialog(context);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.navigator_layout);
        connectionManager = new ConnectionManager();

        // OneSignal Initialization
        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        OneSignal.sendTag("User_ID", currentUserID);

        navigation = findViewById(R.id.navigation);


        navController = Navigation.findNavController(this,R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navigation, navController);

        /** Custom action bar to hide back Icon */
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(R.id.restaurantSearchFragment_id,
                R.id.favoriteRestaurantFragment_id,
                R.id.holder_history_id,
                R.id.myReviewsFragment_id,
                R.id.mainProfile_id).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver,filter);
    }

    @Override
    protected void onStop() {
        unregisterReceiver(networkReceiver);
        super.onStop();
    }
    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }


    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View currentFocusedView = activity.getCurrentFocus();
        if (currentFocusedView != null) {
            inputManager.hideSoftInputFromWindow(((View) currentFocusedView).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
