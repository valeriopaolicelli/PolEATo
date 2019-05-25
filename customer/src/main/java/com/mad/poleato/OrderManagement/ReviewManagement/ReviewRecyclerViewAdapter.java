package com.mad.poleato.OrderManagement.ReviewManagement;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.mad.poleato.Classes.Rating;
import com.mad.poleato.R;

import java.util.List;

public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<ReviewRecyclerViewAdapter.ReviewViewHolder> {

    private List<Rating> reviewList;
    private Context context;
    private FirebaseAuth mAuth;
    private boolean comments_flag;

    private Toast myToast;

        public static class ReviewViewHolder extends RecyclerView.ViewHolder{

            public RatingBar ratingBar;
            public TextView customerData,date,comment;
            public View itemView;

            public ReviewViewHolder(View itemView){
                super(itemView);
                this.itemView=itemView;
                this.ratingBar = (RatingBar) itemView.findViewById(R.id.rating_bar_review);
                this.customerData = (TextView) itemView.findViewById(R.id.customer_data_tv);
                this.date = (TextView) itemView.findViewById(R.id.date_review_tv);
                this.comment = (TextView) itemView.findViewById(R.id.customer_comment);
            }
        }
public ReviewRecyclerViewAdapter(Context context, List<Rating>reviewList){
    this.reviewList = reviewList;
    this.context = context;
    if(context != null)
                myToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);

}

    public void setComments_flag(boolean comments_flag) {
        this.comments_flag = comments_flag;
    }

    @Override
    public ReviewViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
    final View listItem = layoutInflater.inflate(R.layout.restaurantreviews_item,parent, false);
    final ReviewViewHolder viewHolder = new ReviewViewHolder(listItem);
    return viewHolder;
}

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder reviewViewHolder, int position) {
            if(comments_flag){ 
                if(!reviewList.get(position).getComment().equals("")){
                    String customerData = reviewList.get(position).getCustomerdata();
                    reviewViewHolder.customerData.setText(customerData);
                    reviewViewHolder.ratingBar.setRating(reviewList.get(position).getRate());
                    reviewViewHolder.date.setText(reviewList.get(position).getDate());
                    reviewViewHolder.comment.setText(reviewList.get(position).getComment());
                }
            }else{
                String customerData = reviewList.get(position).getCustomerdata();
                reviewViewHolder.customerData.setText(customerData);
                reviewViewHolder.ratingBar.setRating(reviewList.get(position).getRate());
                reviewViewHolder.date.setText(reviewList.get(position).getDate());
                reviewViewHolder.comment.setText(reviewList.get(position).getComment());
            }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }
}

