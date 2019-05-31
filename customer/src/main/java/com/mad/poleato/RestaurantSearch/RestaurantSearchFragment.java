package com.mad.poleato.RestaurantSearch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.ToggleButton;


import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.mad.poleato.Classes.Restaurant;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class RestaurantSearchFragment extends Fragment {

    private Toast myToast;
    private Activity hostActivity;
    private View fragView;
    private RestaurantRecyclerViewAdapter recyclerAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView rv;
    private SearchView sv;
    private ImageButton sortBtn;
    private ImageButton filterBtn;
    private DatabaseReference dbReference;

    private HashMap<String, Restaurant> restaurantMap;
    private List<Restaurant> restaurantList; //original list of all restaurants
    private List<Restaurant> currDisplayedList; //list of filtered elements displayed on the screen
    private Set<String> typesToFilter;

    private ProgressDialog progressDialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    private String currentUserID;
    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    private boolean allFields;

    private ConstraintLayout main_view;
    private ImageView empty_view;

    //id for the filter fragment
    public static final int FILTER_FRAGMENT = 26;

    // list to collect the firebase reference and its listener (to remove listener at the end of this fragment)
    private HashMap<String, MyDatabaseReference> dbReferenceList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.hostActivity = this.getActivity();

        if(hostActivity != null)
            myToast = Toast.makeText(hostActivity, "", Toast.LENGTH_LONG);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
//        //in order to create the logout menu (don't move!)
//        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        /** GoogleSignInOptions */
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        /** Build a GoogleSignInClient with the options specified by gso. */
        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        allFields= false;

        restaurantMap = new HashMap<>();
        restaurantList = new ArrayList<>();
        typesToFilter = new HashSet<>();
        currDisplayedList = new ArrayList<>();

        dbReferenceList= new HashMap<>();

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.restaurant_recyclerview, container, false);

        main_view = (ConstraintLayout) fragView.findViewById(R.id.search_main_view);
        empty_view = (ImageView) fragView.findViewById(R.id.search_empty_view);

        show_empty_view();

        return fragView;
    }

    private void show_main_view(){

        empty_view.setVisibility(View.GONE);
        main_view.setVisibility(View.VISIBLE);
    }

    private void show_empty_view(){

        main_view.setVisibility(View.GONE);
        empty_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sv = (SearchView) fragView.findViewById(R.id.searchView);
        sv.setFocusable(false);

        checkUser(view);
    }

    private void checkUser(final View view) {

        Log.d("Valerio_login", "Email: " + mAuth.getCurrentUser().getEmail());
        Log.d("Valerio_login", currentUserID);
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("customers");
        dbReferenceList.put("customers", new MyDatabaseReference(reference));

        dbReferenceList.get("customers").setSingleValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(currentUserID)){
                    if(!dataSnapshot.child(currentUserID).hasChild("Address") ||
                        !dataSnapshot.child(currentUserID).hasChild("Name") ||
                        !dataSnapshot.child(currentUserID).hasChild("Surname")){

                        /*
                         * incomplete account profile
                         */
                        new AlertDialog.Builder(view.getContext())
                                .setCancelable(false)
                                .setTitle(view.getContext().getString(R.string.missing_fields_title))
                                .setMessage(view.getContext().getString(R.string.missing_fields_body))
                                .setPositiveButton(view.getContext().getString(R.string.go_to_edit),
                                        new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        /**
                                         * GO TO EditProfile
                                         */
                                        Navigation.findNavController(view).navigate(R.id.action_restaurantSearchFragment_id_to_editProfile_id);
                                    }
                                })
                                .setNegativeButton(view.getContext().getString(R.string.logout),
                                        new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        for(MyDatabaseReference my_ref : dbReferenceList.values())
                                            my_ref.removeAllListener();
                                        //logout

                                        //TODO miche logout
                                        /** logout */
                                        revokeAccess();
                                    }
                                })
                                .show();
                    }
                    else
                        composeView();
                }
                else{
                    FirebaseDatabase.getInstance().getReference("users")
                            .child(currentUserID).setValue("customer");
                    FirebaseDatabase.getInstance().getReference("customers")
                            .child(currentUserID+"/Email")
                            .setValue(mAuth.getCurrentUser().getEmail());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void composeView(){
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            int previousLen, currLen;

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("matte", "queryTextSubmit | QUERY: " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("matte", "queryTextChange | QUERY: " + newText);

                if (newText == null)
                    return false;
                currLen = newText.length();

                //void query: reset the list
                if (currLen == 0) {
                    restoreOriginalList();
                    filterDisplay();
                } else {
                    //if text removed from query: search from the whole list
                    if (currLen < previousLen) {
                        restoreOriginalList();
                        filterDisplay();
                    }
                    //text added to query: search from previous list
                    Iterator<Restaurant> rIterator = currDisplayedList.iterator();
                    while (rIterator.hasNext()) {
                        Restaurant s = rIterator.next(); // must be called before you can call i.remove()
                        if (!s.getName().toLowerCase().contains(newText.toLowerCase()))
                            rIterator.remove();
                    }
                }
                //display the updated list
                recyclerAdapter.display(currDisplayedList);

                Log.d("matte", "queryTextChange | displayed list size: " + currDisplayedList.size());
                previousLen = currLen;
                return true;
            }
        });


        rv = (RecyclerView) fragView.findViewById(R.id.recyclerView);
        rv.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this.hostActivity);
        rv.setLayoutManager(layoutManager);

        this.recyclerAdapter = new RestaurantRecyclerViewAdapter(this.hostActivity, this.currDisplayedList);
        rv.setAdapter(recyclerAdapter);

        //add separator between list items
        DividerItemDecoration itemDecor = new DividerItemDecoration(hostActivity, 1); // 1 means HORIZONTAL
        rv.addItemDecoration(itemDecor);

        sortBtn = (ImageButton) fragView.findViewById(R.id.sortButton);
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(hostActivity, sortBtn);
                popup.getMenuInflater().inflate(R.menu.sort_menu, popup.getMenu());

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        String id = fragView.getResources().getResourceName(item.getItemId());
                        Log.d("matte", "Popup pressed | ID: " + id);

                        String[] itemName = id.split("/");
                        if (itemName.length > 2) //error
                            return false;
                        Log.d("matte", "NAME: " + itemName[1]);

                        if (itemName[1].equals("sortByName")) {
                            Log.d("matte", "SORT: byName");
                            recyclerAdapter.sortByName();
                            return true;
                        }

                        if (itemName[1].equals("sortByPrice")) {
                            Log.d("matte", "SORT: byPrice");
                            recyclerAdapter.sortByPrice();
                            return true;
                        }

                        if (itemName[1].equals("sortByPriceInverse")) {
                            Log.d("matte", "SORT: byPriceInverse");
                            recyclerAdapter.sortByPriceInverse();
                            return true;
                        }

                        if (itemName[1].equals("sortByDelivery")) {
                            Log.d("matte", "SORT: byDelivery");
                            recyclerAdapter.sortByDelivery();
                            return true;
                        }

                        if (itemName[1].equals("sortByStar")) {
                            Log.d("matte", "SORT: byStar");
                            recyclerAdapter.sortByStar();
                            return true;
                        }

                        return false;
                    }
                });

                popup.show();
            }
        });


        filterBtn = (ImageButton) fragView.findViewById(R.id.filterButton);
        final Fragment f = this;
        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("filter_fragment");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                FilterFragment filterFrag = new FilterFragment();
                Bundle bundle = new Bundle();
                //pass the current checkbox state to the fragment
                bundle.putSerializable("checkbox_state", (HashSet<String>) typesToFilter);
                filterFrag.setArguments(bundle);
                filterFrag.setTargetFragment(f, FILTER_FRAGMENT);
                filterFrag.show(ft, "filter_fragment");
            }
        });

        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
        fillFields();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILTER_FRAGMENT:
                if (resultCode == Activity.RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    typesToFilter = (HashSet<String>) bundle.getSerializable("checked_types");

                    restoreOriginalList();
                    if(!typesToFilter.isEmpty())
                        filterDisplay();

                    recyclerAdapter.display(currDisplayedList);
                }
                break;
        }

    }

    private void fillFields() {
        Locale locale = Locale.getDefault();
        // get "en" or "it"
        final String localeShort = locale.toString().substring(0, 2);

        dbReference = FirebaseDatabase.getInstance().getReference("restaurants");

        /**
         *         This listener is guaranteed to be called only after "ChildEvent".
         *         Thus it notifies the end of the children
         */
        dbReferenceList.put("restaurants", new MyDatabaseReference(dbReference));

        dbReferenceList.get("restaurants").setValueListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte", "ValueEventiListener : OnCancelled() invoked");
                handler.sendEmptyMessage(0);
            }
        });

        dbReferenceList.get("restaurants").setChildListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("matte", "onChildAdded | PREVIOUS CHILD: " + s);


                if(dataSnapshot.hasChild("Name") &&
                        dataSnapshot.hasChild("Type") &&
                        dataSnapshot.child("Type").hasChild("it") &&
                        dataSnapshot.child("Type").hasChild("en") &&
                        dataSnapshot.hasChild("IsActive") &&
                        dataSnapshot.hasChild("PriceRange") &&
                        dataSnapshot.hasChild("DeliveryCost") &&
                        dataSnapshot.hasChild("photoUrl")
                )
                {

                    final String id = dataSnapshot.getKey();
                    String name = dataSnapshot.child("Name").getValue().toString();
                    String type = dataSnapshot.child("Type").child(localeShort).getValue().toString();
                    Boolean isOpen = (Boolean) dataSnapshot.child("IsActive").getValue();
                    int priceRange = Integer.parseInt(dataSnapshot.child("PriceRange").getValue().toString());
                    double deliveryCost = Double.parseDouble(dataSnapshot.child("DeliveryCost").getValue().toString().replace(",", "."));
                    final String imageUrl = dataSnapshot.child("photoUrl").getValue().toString();
                    //insert before the download because otherwise it can happen that the download finish before
                    //  and the put raise exception
                    Restaurant resObj = new Restaurant(id, "", name, type, isOpen, priceRange, deliveryCost);

                    //Check if restaurant has reviews
                    if(!dataSnapshot.hasChild("Ratings")){
                        resObj.setTotalReviews((long)0);
                        resObj.computeAvgStars(0);
                    }else{
                        //If yes, compute avg rating
                        int totalStars = 0;
                        resObj.setTotalReviews(dataSnapshot.child("Ratings").getChildrenCount());
                        for (DataSnapshot ds : dataSnapshot.child("Ratings").getChildren()){
                            totalStars += Integer.parseInt(ds.child("rate").getValue().toString());
                        }
                        resObj.computeAvgStars(totalStars);
                    }

                    //add to the original list
                    restaurantMap.put(id, resObj);
                    restaurantList.add(resObj);

                    StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            String s = imageUrl;
                            Log.d("matte", "onSuccess | restaurantID: "+id);
                            // Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            restaurantMap.get(id).setImage(s);
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            String s = "";
                            Log.d("matte", "onFailure() : excp -> "+exception.getMessage()
                                    +"| restaurantID: "+id);
                        }
                    });



                    //check the filter before display
                    if (isValidToDisplay(resObj))
                        addToDisplay(resObj);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("matte", "onChildChanged | PREVIOUS CHILD: " + s);

                if(dataSnapshot.hasChild("Name") &&
                        dataSnapshot.hasChild("Type") &&
                        dataSnapshot.child("Type").hasChild("it") &&
                        dataSnapshot.child("Type").hasChild("en") &&
                        dataSnapshot.hasChild("IsActive") &&
                        dataSnapshot.hasChild("PriceRange") &&
                        dataSnapshot.hasChild("DeliveryCost") &&
                        dataSnapshot.hasChild("photoUrl")
                )
                {
                    final String id = dataSnapshot.getKey();
                    String name = dataSnapshot.child("Name").getValue().toString();
                    String type = dataSnapshot.child("Type").child(localeShort).getValue().toString();
                    Boolean isOpen = (Boolean) dataSnapshot.child("IsActive").getValue();
                    int priceRange = Integer.parseInt(dataSnapshot.child("PriceRange").getValue().toString());
                    double deliveryCost = Double.parseDouble(dataSnapshot.child("DeliveryCost").getValue().toString().replace(",", "."));
                    final String imageUrl = dataSnapshot.child("photoUrl").getValue().toString();
                    int totalStars = 0;
                    long totalReviews;

                    //Check if restaurant has reviews
                    if(!dataSnapshot.hasChild("Ratings")){
                        totalReviews=0;
                        totalStars=0;
                    }else{
                        //If yes, compute avg rating
                        totalReviews = dataSnapshot.child("Ratings").getChildrenCount();
                        for (DataSnapshot ds : dataSnapshot.child("Ratings").getChildren()){
                            totalStars += Integer.parseInt(ds.child("rate").getValue().toString());
                        }
                    }

                    Restaurant resObj;
                    if(restaurantMap.containsKey(id)) {
                        resObj = restaurantMap.get(id);
                        resObj.setName(name);
                        resObj.setType(type);
                        resObj.setIsOpen(isOpen);
                        resObj.setPriceRange(priceRange);
                        resObj.setDeliveryCost(deliveryCost);
                        resObj.setTotalReviews(totalReviews);
                        resObj.computeAvgStars(totalStars);
                        //recyclerAdapter.updateLayout();
                        //insert the element by keeping the actual order after checking the filter
                        if (isValidToDisplay(resObj)) {
                            if(!currDisplayedList.contains(resObj))
                                addToDisplay(resObj);
                            else
                                recyclerAdapter.updateLayout();
                        }
                        else if(currDisplayedList.contains(resObj))
                            removeFromDisplay(resObj);
                    }
                    else {
                        resObj= new Restaurant(id, "", name, type, isOpen, priceRange, deliveryCost);
                        resObj.setTotalReviews(totalReviews);
                        resObj.computeAvgStars(totalStars);
                        restaurantMap.put(id, resObj);
                        restaurantList.add(resObj);
                        if (isValidToDisplay(resObj))
                            addToDisplay(resObj);
                    }

                    //check the new image
                    StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Log.d("matte", "onSuccess");
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            restaurantMap.get(id).setImage(imageUrl);
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d("matte", "onFailure() : excp -> "+exception.getMessage());
                            restaurantMap.get(id).setImage("");
                        }
                    });

                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Log.d("matte", "onChildRemoved");

                String id = dataSnapshot.getKey();
                Restaurant toRemove = restaurantMap.get(id);
                restaurantMap.remove(id);
                restaurantList.remove(toRemove);
                removeFromDisplay(toRemove);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Log.d("matte", "onChildMoved | PREVIOUS CHILD: " + s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte", "onCancelled | ERROR: " + databaseError.getDetails() +
                        " | MESSAGE: " + databaseError.getMessage());
            }
        });
    }

    private void restoreOriginalList() {
        currDisplayedList.clear();
        for (Restaurant r : restaurantList)
            currDisplayedList.add(r);
        show_main_view();
    }

    private void addToDisplay(Restaurant r) {
        //add new item to the displayed list
        currDisplayedList.add(r);
        //notify the adapter to show it by keeping the actual order
        recyclerAdapter.updateLayout();
        show_main_view();
    }

    private void removeFromDisplay(Restaurant r) {

        currDisplayedList.remove(r);
        if(currDisplayedList.isEmpty())
            show_empty_view();
        // simply update. No order is compromised by removing an item
        recyclerAdapter.notifyDataSetChanged();
    }

    private void filterDisplay() {
        //no filters to apply
        if (typesToFilter.isEmpty())
            return;

        Iterator<Restaurant> rIterator = currDisplayedList.iterator();
        while (rIterator.hasNext()) {
            Restaurant s = rIterator.next();
            if (!isValidToDisplay(s))
                rIterator.remove();
        }

    }

    private boolean isValidToDisplay(Restaurant r) {
        //no filters to apply -> always valid
        if (typesToFilter.isEmpty())
            return true;

        String[] types = r.getType().toLowerCase().split(",(\\s)*");
        for (String t : types) {
            if (typesToFilter.contains(t)) {
                return true;
            }
        }
        return false;
    }

    private void revokeAccess() {
        // Firebase sign out
        //mAuth.signOut();

        Log.d("miche", "Logout");
        FirebaseAuth.getInstance().signOut();
        // Google revoke access
        mGoogleSignInClient.revokeAccess();

        OneSignal.setSubscription(false);

        /**
         *  GO TO LOGIN ****
         */
        Navigation.findNavController(fragView).navigate(R.id.action_restaurantSearchFragment_id_to_signInActivity);
        getActivity().finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        for (MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }
}
