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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


public class ReservationFragment extends Fragment {
    private ListView lv;
    Customer c1;
    Customer c2;
    Customer c3;
    Customer c4;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout,container,false);

        lv = view.findViewById(R.id.reservationslv);

        c1 = new Customer("Fabio", "Ricciardi", "no piccante", "04/04/2019");
        c2 = new Customer("Michelangelo", "Moncada", "sono gay", "04/04/2019");
        c3 = new Customer("Valerio", "Paolicelli", "sono scarso", "04/04/2019");
        c4 = new Customer("Matteo", "Pesciaiuolo", "forza lupi rosso blu", "04/04/2019");


        final ArrayList<Customer>customers = new ArrayList<>();
        customers.add(c1);
        customers.add(c2);
        customers.add(c3);
        customers.add(c4);


        //creo istanza della classe myAdapter

        final MyAdapter adapter = new MyAdapter(getActivity(),R.layout.reservation_row_layout,customers);

        //set adapter to list

        lv.setAdapter(adapter);

        //handle item clicks
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final Customer c = (Customer) adapterView.getItemAtPosition(position);
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
                else {

                    //se lo status è COOKING, il ristoratore può scegliere se far partire la consegna
                    if(c.getStatus() == Status.COOKING){
                        builder.setTitle("Deliver order");

                        builder.setMessage("Is everything ready? Status will pass into 'on delivery'");
                        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                c.setStatus(Status.DELIVERY);
                                adapter.notifyDataSetChanged();
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
                                adapter.notifyDataSetChanged();
                            }
                        });
                        builder.setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                c.setStatus(Status.REJECTED);
//                        FragmentTransaction ft = getFragmentManager().beginTransaction();
//                        ft.detach(ReservationFragment.this).attach(ReservationFragment.this).commit();
                                adapter.notifyDataSetChanged();
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
            }
        });



        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private class MyAdapter extends ArrayAdapter<Customer>{
        Context context;
        int resource;
        ArrayList<Customer>customers;

        MyAdapter(Context context, int resource, ArrayList<Customer> customers){
            super(context,resource,customers);
            this.context=context;
            this.resource = resource;
            this.customers = customers;
        }

        @NonNull
        @Override
        //responsable for getting the view and attaching it to the ListView
        public View getView(int position, View convertView, ViewGroup parent) {
            String name = getItem(position).getName();
            String surname = getItem(position).getSurname();
            String notes = getItem(position).getNotes();
            String date = getItem(position).getDate();
            String status = getItem(position).getStat();

            Customer customer = new Customer(name, surname, notes, date);
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView =  inflater.inflate(resource, parent, false);

            TextView tvName = (TextView) convertView.findViewById(R.id.textView1);
            TextView tvSurname = (TextView) convertView.findViewById(R.id.textView2);
            TextView tvNotes = (TextView) convertView.findViewById(R.id.textView3);
            TextView tvDate = (TextView) convertView.findViewById(R.id.textView4);
            TextView tvStatus = (TextView) convertView.findViewById(R.id.textView5);

            tvName.setText(name);
            tvSurname.setText(surname);
            tvDate.setText(date);
            tvNotes.setText(notes);
            tvStatus.setText(status);

            return convertView;
        }
    }
}