package com.example.poleato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ReservationFragment extends Fragment {
    private ExpandableListView lv;
    private ReservationExpandableListAdapter listAdapter;
    private List<Customer> reservations;
    private HashMap<String,List<Dish>> listHash = new HashMap<>();

    Customer c1;
    Customer c2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout,container,false);
        initData();

        lv = view.findViewById(R.id.reservationslv);
        listAdapter = new ReservationExpandableListAdapter(getActivity(),reservations,listHash);

        lv.setAdapter(listAdapter);
/*
        lv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                final Customer c = (Customer) parent.getItemAtPosition(childPosition);
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                // Se lo stato è REJECTED, il ristoratore non può cambiarlo
                if(c.getStatus() == Status.REJECTED){
                    builder.setTitle("Order Rejected");

                    builder.setMessage("Sorry, you've already rejected this order");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                }
                else if(c.getStatus() == Status.DELIVERY){
                    builder.setTitle("Order Delivered");

                    builder.setMessage("You've already delivered this order");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
                }
                else {

                    //se lo status è COOKING, il ristoratore può scegliere se far partire la consegna
                    if(c.getStatus() == Status.COOKING){
                        builder.setTitle("Deliver order");

                        builder.setMessage("Is everything ready? Status will pass into 'on delivery'");
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                c.setStatus(Status.DELIVERY);
                                listAdapter.notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                    }
                    else {
                        //Se lo stato è ACCEPTANCE, il ristoratore può accetare o rifiutare l'ordine
                        builder.setTitle("Confirm order");

                        builder.setMessage("Do you want to confirm or reject this order? Status will pass into 'on cooking'");

                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                c.setStatus(Status.COOKING);
                                listAdapter.notifyDataSetChanged();

                                //TODO: Aggiornare quantità menù
                            }
                        });
                        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                c.setStatus(Status.REJECTED);
                                listAdapter.notifyDataSetChanged();
                            }
                        });
                        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                    }
                }
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });
        */
        return view;
    }

    private void initData(){

        reservations = new ArrayList<>();
        listHash = new HashMap<>();

        c1 = new Customer("ID111111","Fabio", "Ricciardi", "no piccante", "04/04/2019");
        c2 = new Customer("ID222222","Michelangelo", "Moncada", "sono gay", "04/04/2019");

        reservations.add(c1);
        reservations.add(c2);


        Dish d1_c1= new Dish("Pasta carbonara", 1, "no cipolla");
        Dish d2_c1= new Dish("Pizza margherita", 2, "pizze tagliate");
        List<Dish> list1= new ArrayList<>();
        list1.add(d1_c1);
        list1.add(d2_c1);
        List<Dish> list2 = new ArrayList<>();
        list2.add(d1_c1);

        listHash.put(c1.getOrder_id(),list1);
        listHash.put(c2.getOrder_id(),list2);

    }
}