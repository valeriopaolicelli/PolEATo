package com.example.poleato;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class ReservationFragment extends Fragment {
    private ListView lv;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.reservation_frag_layout,container,false);

        lv = view.findViewById(R.id.reservationslv);

        Customer c1 = new Customer("Fabio", "Ricciardi", "no piccante", "04/04/2019");
        Customer c2 = new Customer("Michelangelo", "Moncada", "sono gay", "04/04/2019");
        Customer c3 = new Customer("Valerio", "Paolicelli", "sono scarso", "04/04/2019");
        Customer c4 = new Customer("Matteo", "Pesciaiuolo", "forza lupi rosso blu", "04/04/2019");


        ArrayList<Customer>customers = new ArrayList<>();
        customers.add(c1);
        customers.add(c2);
        customers.add(c3);
        customers.add(c4);


        //creo istanza della classe myAdapter

        MyAdapter adapter = new MyAdapter(getActivity(),R.layout.reservation_row_layout,customers);

        //set adapter to list

        lv.setAdapter(adapter);

        //handle item clicks
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                if(position==0){
                    Toast.makeText(getActivity(), "Item one clicked", Toast.LENGTH_SHORT).show();
                }
            }
        });


        return view;
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

            Customer customer = new Customer(name, surname, notes, date);
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView =  inflater.inflate(resource, parent, false);

            TextView tvName = (TextView) convertView.findViewById(R.id.textView1);
            TextView tvSurname = (TextView) convertView.findViewById(R.id.textView2);
            TextView tvNotes = (TextView) convertView.findViewById(R.id.textView3);
            TextView tvDate = (TextView) convertView.findViewById(R.id.textView4);

            tvName.setText(name);
            tvSurname.setText(surname);
            tvDate.setText(date);
            tvNotes.setText(notes);

            return convertView;
        }
    }
}