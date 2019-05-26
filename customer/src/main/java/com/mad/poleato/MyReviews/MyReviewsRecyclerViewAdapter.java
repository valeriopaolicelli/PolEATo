package com.mad.poleato.MyReviews;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.mad.poleato.Classes.Rating;
import com.mad.poleato.FavoriteRestaurants.FavoriteRestaurantRecyclerViewAdapter;
import com.mad.poleato.R;

import java.util.List;
import java.util.ListIterator;

public class MyReviewsRecyclerViewAdapter extends RecyclerView.Adapter<MyReviewsRecyclerViewAdapter.MyReviewsViewHolder> {

    private List<Rating> ratingList; //current displayed list


    public MyReviewsRecyclerViewAdapter(List<Rating> ratingList) {
        this.ratingList = ratingList;
    }

    @NonNull
    @Override
    public MyReviewsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        final View listItem = layoutInflater.inflate(R.layout.fragment_my_reviews_child, viewGroup, false);
        final MyReviewsViewHolder viewHolder = new MyReviewsRecyclerViewAdapter.MyReviewsViewHolder(listItem);

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull MyReviewsViewHolder myReviewsViewHolder, int i) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        myReviewsViewHolder.textRestaurant.setText(ratingList.get(i).getRestaurantID());
        myReviewsViewHolder.ratingBar.setNumStars(ratingList.get(i).getRate());
        myReviewsViewHolder.showReview.setText(ratingList.get(i).getComment());
    }

    @Override
    public int getItemCount() {
        return ratingList.size();
    }


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class MyReviewsViewHolder extends RecyclerView.ViewHolder {

        private TextView textRestaurant, showReview;
        private RatingBar ratingBar;

        public MyReviewsViewHolder(@NonNull View itemView) {
            super(itemView);

            this.textRestaurant = itemView.findViewById(R.id.textRestaurant);
            this.showReview = itemView.findViewById(R.id.showReview);
            this.ratingBar = itemView.findViewById(R.id.ratingBar);


        }
    }
}
