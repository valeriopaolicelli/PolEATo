package com.mad.poleato.View.ViewModel;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
import com.mad.poleato.DailyOffer.DishCategoryTranslator;
import com.mad.poleato.R;
import com.mad.poleato.DailyOffer.Food;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MyViewModel extends ViewModel {
    private MutableLiveData<List<String>> _listDataGroup = new MutableLiveData<>(); // header titles
    private MutableLiveData<HashMap<String, List<Food>>> _listDataChild = new MutableLiveData<>(); // child data in format of header title, child title


    private ProgressDialog progressDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };


    public LiveData<HashMap<String, List<Food>>> getListC() {
        return _listDataChild;
    }
    public LiveData<List<String>> getListG() {
        return _listDataGroup;
    }

    public void setData(List<String> listDataGroup, HashMap<String, List<Food>> listDataChild) {
        _listDataGroup.setValue(listDataGroup);
        _listDataChild.postValue(listDataChild);
    }

    public void setImg(String groupTag, int idx, Bitmap img){
        if(idx < _listDataChild.getValue().get(groupTag).size())
            _listDataChild.getValue().get(groupTag).get(idx).setImg(img);

    }

    public Food getChild(final int groupPosition, final int childPosition){
        return this._listDataChild.getValue().get(getGroup(groupPosition).toString()).get(childPosition);
    }


    public void insertChild(final int groupPosition, Food food) {
        this._listDataChild.getValue().get(getGroup(groupPosition).toString()).add(food);
    }

    public void insertChild(String groupTag, Food food) {
        this._listDataChild.getValue().get(groupTag).add(food);
    }

    public void removeChild(final int groupPosition, final int childPosition) {
        this._listDataChild.getValue().get(getGroup(groupPosition).toString()).remove(childPosition);
    }

    public void removeChild(String groupTag, final int childPosition){
        this._listDataChild.getValue().get(groupTag).remove(childPosition);

    }

    public void removeChild(String groupTag, String childID){
        int toRemove = 0;
        for(Food f : _listDataChild.getValue().get(groupTag)){
            if(f.getId().equals(childID)){
                break;
            }
            toRemove ++;
        }

        removeChild(groupTag, toRemove);
    }

    public Object getGroup(int groupPosition) {
        return this._listDataGroup.getValue().get(groupPosition);
    }

    /**
     * Used to establish the priceRange for the restaurant
     * @return The mean price
     */
    public double getMeanPrice(){

        double sum = 0;
        int count = 0;

        for(String s : _listDataChild.getValue().keySet()){

            for(Food f : _listDataChild.getValue().get(s)){
                sum += f.getPrice();
                count ++;
            }
        }
        return sum/count;
    }


    private void initGroup(Context context){
        List<String> l = new ArrayList<>();
        l.add("Starters");
        l.add("Firsts");
        l.add("Seconds");
        l.add("Desserts");
        l.add("Drinks");

        _listDataGroup.setValue(l);
    }

    private void initChild() {

        _listDataChild.setValue(new HashMap<String, List<Food>>());
        for (String s : _listDataGroup.getValue())
            _listDataChild.getValue().put(s, new ArrayList<Food>());

    }


    public void downloadMenu(final Context context){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String currentUserID = mAuth.getCurrentUser().getUid();

        initGroup(context);
        initChild();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants/"+ currentUserID +"/Menu");

        if(context != null)
            progressDialog = ProgressDialog.show(context, "", context.getString(R.string.loading));


        //This is called after the OnChildAdded so it notify the end of downloads
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(progressDialog.isShowing())
                    handler.sendEmptyMessage(0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(progressDialog.isShowing())
                    handler.sendEmptyMessage(0);
            }
        });

        reference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.hasChild("Name") &&
                    dataSnapshot.hasChild("Quantity") &&
                    dataSnapshot.hasChild("Description") &&
                    dataSnapshot.hasChild("Price") &&
                    dataSnapshot.hasChild("Category") &&
                    dataSnapshot.hasChild("photoUrl"))
                {
                    //set default image initially, then change if download is successful
                    final String id = dataSnapshot.getKey();
                    Bitmap img = BitmapFactory.decodeResource(context.getResources(), R.drawable.plate_fork);
                    String name = dataSnapshot.child("Name").getValue().toString();
                    int quantity = Integer.parseInt(dataSnapshot.child("Quantity").getValue().toString());
                    String description = dataSnapshot.child("Description").getValue().toString();
                    double price = Double.parseDouble(dataSnapshot.child("Price")
                            .getValue()
                            .toString()
                            .replace(",", "."));
                    final String category = dataSnapshot.child("Category").getValue().toString();
                    final String imageUrl = dataSnapshot.child("photoUrl").getValue().toString();

                    Food f = new Food(id, img, name, description, price, quantity);
                    insertChild(category, f);
                    final int curr_index = _listDataChild.getValue().get(category).size() - 1;

                    StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            String s = imageUrl;
                            Log.d("matte", "onSuccess");
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            setImg(category, curr_index, bmp);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            String s = imageUrl;
                            Log.d("matte", "onFailure() : excp -> "+exception.getMessage()
                                    +"| restaurantID: "+id);
                        }
                    });


                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if(dataSnapshot.hasChild("Name") &&
                        dataSnapshot.hasChild("Quantity") &&
                        dataSnapshot.hasChild("Description") &&
                        dataSnapshot.hasChild("Price") &&
                        dataSnapshot.hasChild("Category") &&
                        dataSnapshot.hasChild("photoUrl")){

                    final String id = dataSnapshot.getKey();
                    Bitmap img = BitmapFactory.decodeResource(context.getResources(), R.drawable.plate_fork);
                    String name = dataSnapshot.child("Name").getValue().toString();
                    int quantity = Integer.parseInt(dataSnapshot.child("Quantity").getValue().toString());
                    String description = dataSnapshot.child("Description").getValue().toString();
                    double price = Double.parseDouble(dataSnapshot.child("Price")
                            .getValue()
                            .toString()
                            .replace(",", "."));
                    final String category = dataSnapshot.child("Category").getValue().toString();
                    final String imageUrl = dataSnapshot.child("photoUrl").getValue().toString();

                    int toDelete = -1;
                    for(int idx = 0; idx < _listDataChild.getValue().get(category).size(); idx ++){
                        if(_listDataChild.getValue().get(category).get(idx).getId().equals(id)){
                            toDelete = idx;
                            break;
                        }
                    }
                    if(toDelete != -1)
                        removeChild(category, toDelete);


                    Food f = new Food(id, img, name, description, price, quantity);
                    insertChild(category, f);
                    final int curr_index = _listDataChild.getValue().get(category).size()-1;

                    StorageReference photoReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            String s = imageUrl;
                            Log.d("matte", "onSuccess");
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            setImg(category, curr_index, bmp);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            String s = imageUrl;
                            Log.d("matte", "onFailure() : excp -> "+exception.getMessage()
                                    +"| restaurantID: "+id);
                        }
                    });

                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("Category")){
                    String category = dataSnapshot.child("Category").getValue().toString();
                    String id = dataSnapshot.getKey();

                    int toDelete = -1;
                    for(int idx = 0; idx < _listDataChild.getValue().get(category).size(); idx ++){
                        if(_listDataChild.getValue().get(category).get(idx).getId().equals(id)){
                            toDelete = idx;
                            break;
                        }
                    }
                    if(toDelete != -1)
                        removeChild(category, toDelete);
                }

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
