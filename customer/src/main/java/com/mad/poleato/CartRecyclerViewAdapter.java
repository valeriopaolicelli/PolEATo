package com.mad.poleato;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.List;

public class CartRecyclerViewAdapter extends RecyclerView.Adapter<CartRecyclerViewAdapter.CartViewHolder> {

    private List<Food> foodList;
    private Context context;
    private Order order;

    public static class CartViewHolder extends RecyclerView.ViewHolder{
        public ImageView img;
        public TextView name,price,quantity;
        public EditText notes;
        public ImageButton minum,plus,delete;


        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            this.img = (ImageView) itemView.findViewById(R.id.cardImage);
            this.name = (TextView) itemView.findViewById(R.id.cardName);
            this.price = (TextView) itemView.findViewById(R.id.cardPrice);
            this.quantity = (TextView) itemView.findViewById(R.id.quantity);
            this.notes = (EditText) itemView.findViewById(R.id.notesEditText);
            this.minum = (ImageButton) itemView.findViewById(R.id.decreaseBtn);
            this.plus = (ImageButton) itemView.findViewById(R.id.increaseBtn);
            this.delete = (ImageButton) itemView.findViewById(R.id.deleteBtn);
        }
    }

    public CartRecyclerViewAdapter(Context context, List<Food> foodList, Order order){
        this.foodList=foodList;
        this.context=context;
        this.order = order;
    }


    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        final View listItem = layoutInflater.inflate(R.layout.cart_item, viewGroup, false);
        final CartViewHolder viewHolder = new CartViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final CartViewHolder cartViewHolder, final int i) {
        cartViewHolder.name.setText(foodList.get(i).getName());
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        double p = Double.parseDouble(foodList.get(i).getPrice().toString());
        String priceStr = decimalFormat.format(p).toString()+"€";
        cartViewHolder.price.setText(priceStr);
        cartViewHolder.quantity.setText(Integer.toString(foodList.get(i).getSelectedQuantity()));
        cartViewHolder.notes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                foodList.get(i).setCustomerNotes(editable.toString());

            }
        });


        foodList.get(i).setCustomerNotes(cartViewHolder.notes.getText().toString());

        cartViewHolder.minum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedQuantity = foodList.get(i).getSelectedQuantity();
                if(selectedQuantity>0){
                    foodList.get(i).decreaseSelectedQuantity();
                    if(foodList.get(i).getSelectedQuantity()==0)
                        order.removeFoodFromOrder(foodList.get(i));
                    order.updateTotalPrice();
                    notifyDataSetChanged();
                }
                CartActivity.computeTotal(order.getTotalPrice());
            }
        });

        cartViewHolder.plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = foodList.get(i).getQuantity();
                int selectedQuantity = foodList.get(i).getSelectedQuantity();
                if(selectedQuantity<quantity){
                    foodList.get(i).increaseSelectedQuantity();
                    order.updateTotalPrice();
                    notifyDataSetChanged();
                }
                CartActivity.computeTotal(order.getTotalPrice());

            }
        });

        cartViewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                foodList.get(i).setSelectedQuantity(0);
                order.removeFoodFromOrder(foodList.get(i));
                order.updateTotalPrice();
                notifyDataSetChanged();
                CartActivity.computeTotal(order.getTotalPrice());
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }


}
