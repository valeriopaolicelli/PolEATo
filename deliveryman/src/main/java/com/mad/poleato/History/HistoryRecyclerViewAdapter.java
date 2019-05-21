package com.mad.poleato.History;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mad.poleato.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyList;
    private Context context;

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView order_tv, restaurant_tv, restaurantAddress_tv;
        public TextView price_tv, date_tv, expectedTime_tv, deliveredTime_tv;
        public View itemView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView=itemView;
            this.order_tv = (TextView) itemView.findViewById(R.id.orderID_tv);
            this.restaurant_tv = (TextView) itemView.findViewById(R.id.restaurant_tv);
            this.restaurantAddress_tv = (TextView) itemView.findViewById(R.id.restaurantAddress_tv);
            this.price_tv = (TextView) itemView.findViewById(R.id.cost_tv);
            this.date_tv = (TextView) itemView.findViewById(R.id.date_tv);
            this.expectedTime_tv = (TextView) itemView.findViewById(R.id.expectedTime_tv);
            this.deliveredTime_tv = (TextView) itemView.findViewById(R.id.deliveredTime_tv);
        }
    }

    public HistoryRecyclerViewAdapter(Context context){
        this.context=context;
        this.historyList = new ArrayList<>();
    }

    public void setAllHistories(List<HistoryItem> histories) {
        this.historyList = histories;
        Collections.sort(this.historyList, HistoryItem.timeInverseComparator); //TODO inverse comparator
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public HistoryRecyclerViewAdapter.HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        final View listItem = layoutInflater.inflate(R.layout.history_layout, viewGroup, false);
        final HistoryViewHolder viewHolder = new HistoryViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final HistoryViewHolder historyViewHolder, final int position) {

        historyViewHolder.order_tv.setText(historyList.get(position).getOrderID());
        historyViewHolder.restaurant_tv.setText(historyList.get(position).getNameRestaurant());
        historyViewHolder.restaurantAddress_tv.setText(historyList.get(position).getAddressRestaurant());
        historyViewHolder.price_tv.setText(historyList.get(position).getTotalPrice());
        historyViewHolder.date_tv.setText(historyList.get(position).getDeliveredDate());
        historyViewHolder.expectedTime_tv.setText(historyList.get(position).getExpectedHour());
        historyViewHolder.deliveredTime_tv.setText(historyList.get(position).getDeliveredHour());

    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

}
