package com.mad.poleato.History.RatingManagement;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mad.poleato.Classes.Rating;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class RatingFragment extends Fragment {

    private Toast myToast;
    private String currentUserID;
    private String restaurantName;
    private String restaurantID;
    private String orderID;
    private String dateOrder;

    private FirebaseAuth mAuth;
    private Activity hostActivity;

    private TextView ratingLabel;
    private RatingBar ratingBar;
    private EditText review_et;
    private Button submitBtn;
    private TextView ratingScale;

    private Context context;
    private DatabaseReference restaurantReference;
    private DatabaseReference customerReference;
    private View fragview;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context=context;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getActivity() != null)
        myToast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);

        //Get restaurant info
        Bundle arguments = getArguments();
        if(arguments!=null){
            restaurantID = arguments.getString("restaurantID");
            restaurantName = arguments.getString("restaurantName");
            orderID = arguments.getString("orderID");
            dateOrder= arguments.getString("date");
        }

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);
        OneSignal.sendTag("User_ID", currentUserID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragview = inflater.inflate(R.layout.rating_fragment_layout, container, false);

        return fragview;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        restaurantReference = FirebaseDatabase.getInstance().getReference("restaurants/"+restaurantID+"/Ratings");
        customerReference = FirebaseDatabase.getInstance().getReference("customers/"+currentUserID+"/Ratings");
        ratingLabel = (TextView) view.findViewById(R.id.ratingLabel);
        ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        ratingScale = (TextView) view.findViewById(R.id.tvRatingScale);
        review_et =(EditText) view.findViewById(R.id.textReview);
        submitBtn = (Button) view.findViewById(R.id.btnSubmit);

        String ratingL = context.getString(R.string.reviewLabel) + " " + restaurantName;
        ratingLabel.setText(ratingL);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                ratingScale.setText(String.valueOf(v));
                switch ((int) ratingBar.getRating()){
                    case 1:
                        ratingScale.setText(context.getString(R.string.very_bad));
                        break;
                    case 2:
                        ratingScale.setText(context.getString(R.string.not_good));
                        break;
                    case 3:
                        ratingScale.setText(context.getString(R.string.neutral));
                        break;
                    case 4:
                        ratingScale.setText(context.getString(R.string.good));
                        break;
                    case 5:
                        ratingScale.setText(context.getString(R.string.very_good));
                        break;
                }
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((int)ratingBar.getRating() == 0){
                    ratingBar.setBackground(ContextCompat.getDrawable(context, R.drawable.border_wrong_field));
                    myToast.setText("Insert a rating to submit a review");
                    myToast.show();
                }
                else {
                    Rating rating = new Rating(currentUserID,(int)ratingBar.getRating(),
                                                            review_et.getText().toString(),restaurantID,orderID, dateOrder);
                    DatabaseReference newRatingR = restaurantReference.child(orderID);
                    newRatingR.setValue(rating);
                    //Set flag of reservation=true => Customer has reviewed that order
                    DatabaseReference dbReference = FirebaseDatabase.getInstance()
                            .getReference("customers/"+currentUserID+"/reservations/"+orderID+"/reviewFlag");
                    dbReference.setValue("true");
                    //Upload new rating for customer
                    DatabaseReference newRatingC = customerReference.child(orderID);
                    newRatingC.setValue(rating);

                    Navigation.findNavController(fragview).navigate(R.id.action_rating_fragment_id_to_restaurantSearchFragment_id);
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
