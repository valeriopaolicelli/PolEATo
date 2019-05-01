package com.mad.poleato;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements Interface {

    private Order order;
    private boolean flag=false;
    private static TextView tvTotal;
    private static TextView tvEmptyCart;
    private DatabaseReference dbReference;
    private CartRecyclerViewAdapter recyclerAdapter;
    private Button orderBtn;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView rv;
    private List<Food> foodList;
    private EditText time, date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_layout);
        order = (Order) getIntent().getSerializableExtra("order");
        dbReference = FirebaseDatabase.getInstance().getReference("restaurants");

        foodList = order.getSelectedFoods();

        tvTotal = (TextView) findViewById(R.id.tv_total);
        tvEmptyCart = (TextView) findViewById(R.id.empty_cart);

        time= findViewById(R.id.input_time);
        date= findViewById(R.id.input_date);

        orderBtn = (Button) findViewById(R.id.btn_placeorder);
        rv = (RecyclerView) findViewById(R.id.recycler_cart);
        rv.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);

        this.recyclerAdapter = new CartRecyclerViewAdapter(getApplicationContext(), foodList, order);
        rv.setAdapter(recyclerAdapter);

        DividerItemDecoration itemDecoration = new DividerItemDecoration(getApplicationContext(), 1 );
        rv.addItemDecoration(itemDecoration);

        if(order.getSelectedFoods().isEmpty()) {
            rv.setVisibility(View.GONE);
            tvEmptyCart.setVisibility(View.VISIBLE);
        }else {
            rv.setVisibility(View.VISIBLE);
            tvEmptyCart.setVisibility(View.GONE);

        }

        computeTotal(order.getTotalPrice());
        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean wrongField= false;
                if(order.getSelectedFoods().isEmpty()){
                    wrongField= true;
                    Toast.makeText(getApplicationContext(),"Cart is Empty", Toast.LENGTH_SHORT).show();
                }

                if(time.getText().toString().equals("")) {
                    wrongField=true;
                    Toast.makeText(getApplicationContext(), "Specify the time to receive the order", Toast.LENGTH_LONG).show();
                    time.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_wrong_field));
                }
                if(date.getText().toString().equals("")){
                    wrongField= true;
                    Toast.makeText(getApplicationContext(), "Specify the date to receive the order", Toast.LENGTH_LONG).show();
                    date.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_wrong_field));
                }
                if(!wrongField){
                    AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                    builder.setTitle("Confirm order");
                    builder.setMessage("Proceed with order request?");
                    builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //                            DatabaseReference reservation =  dbReference.child(order.getRestaurantID()).child("reservations").push();
                            //                            reservation.setValue(order);
                            //                            String dbkey = reservation.getKey();
                            //                            dbReference.child(order.getRestaurantID()).child("reservations").child(dbkey).child("dishes").setValue(order.getSelectedFoods());

                            order.setDate(date.getText().toString());
                            order.setTime(time.getText().toString());

                            order.uploadOrder();
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_OK, returnIntent);
                            flag = true;
                            finish();
                        }
                    });

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if(!flag){
            Intent resultIntent= new Intent();
            resultIntent.putExtra("old_order", order);
            setResult(Activity.RESULT_CANCELED,resultIntent);
        }
        super.onBackPressed();

    }

    @Override
    public Order getOrder() {
        return order;
    }

    public static void computeTotal(Double price){
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        String priceStr = decimalFormat.format(price).toString()+"€";
        tvTotal.setText(priceStr);

        if(price==0){
            rv.setVisibility(View.GONE);
            tvEmptyCart.setVisibility(View.VISIBLE);
        }
        else{
            rv.setVisibility(View.VISIBLE);
            tvEmptyCart.setVisibility(View.GONE);
        }
    }
}
