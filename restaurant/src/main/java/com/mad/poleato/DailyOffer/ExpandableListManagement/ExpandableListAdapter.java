package com.mad.poleato.DailyOffer.ExpandableListManagement;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mad.poleato.DailyOffer.DailyOfferFragmentDirections;
import com.mad.poleato.DailyOffer.DishCategoryTranslator;
import com.mad.poleato.DailyOffer.Food;
import com.mad.poleato.R;
import com.mad.poleato.View.ViewModel.MyViewModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


/**
 * The expandableList adapter for the restaurant menu
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private final Activity host;
    private final LayoutInflater inf;
    private List<String> _listDataGroup; // header titles
    private HashMap<String, List<Food>> _listDataChild; // child data in format of header title, child title
    private String localeShort;
    private DishCategoryTranslator translator;

    private ProgressDialog progressDialog;


    private String currentUserID;
    private FirebaseAuth mAuth;


    public ExpandableListAdapter(Activity host) {

        this.host = host;
        inf = LayoutInflater.from(host);
        this._listDataGroup = new ArrayList<>();
        this._listDataChild = new HashMap<>();
        Log.d("matte", "[Init]headers:" + _listDataGroup.toString());
        Log.d("matte", "[Init]childs:" + _listDataChild.toString());

        String locale = Locale.getDefault().toString();
        Log.d("matte", "LOCALE: "+locale);
        localeShort = locale.substring(0, 2);

        translator = new DishCategoryTranslator();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();
    }


    public void setAllGroup(List<String> strings) {
        this._listDataGroup = strings;
        notifyDataSetChanged();
    }

    public void setAllChild(HashMap<String, List<Food>> strings) {
        this._listDataChild = strings;
        notifyDataSetChanged();
    }


    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        Log.d("matte", "[Child]getview{ group:" + groupPosition + ", child:" + childPosition + ", view:" + convertView + "}");

        final FoodViewHolder holder; //recycler view pattern

        if (convertView == null) {
            convertView = inf.inflate(R.layout.layout_menu_child, parent, false);
            holder = new FoodViewHolder();
            holder.img = (ImageView) convertView.findViewById(R.id.cardImage);
            holder.name = (TextView) convertView.findViewById(R.id.cardName);
            holder.description = (TextView) convertView.findViewById(R.id.cardDescription);
            holder.price = (TextView) convertView.findViewById(R.id.cardPrice);
            holder.quantity = (TextView) convertView.findViewById(R.id.cardQuantity);
            convertView.setTag(holder);

        } else {
            holder = (FoodViewHolder) convertView.getTag();
        }

        holder.img.setImageBitmap(getChild(groupPosition, childPosition).getImg());
        holder.name.setText(getChild(groupPosition, childPosition).getName());
        holder.description.setText(getChild(groupPosition, childPosition).getDescription());
        /** price and currency */
        DecimalFormat decimalFormat = new DecimalFormat("#.00"); //two decimal
        String priceStr = decimalFormat.format(getChild(groupPosition, childPosition).getPrice());
        String currency = host.getString(R.string.currency);
        priceStr += currency;
        holder.price.setText(priceStr);
        /** quantity */
        String qntStr = "(qty " + getChild(groupPosition, childPosition).getQuantity() + ")";
        holder.quantity.setText(qntStr);

        /** three dots vertical menu */
        ImageButton settingsButton = convertView.findViewById(R.id.cardSettings);
        final View finalConvertView = convertView;
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("matte", "inside showPopup");
                PopupMenu popup = new PopupMenu(host, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.popup_cardview, popup.getMenu());
                popup.show();

                Menu itemList = popup.getMenu();

                MenuItem modify = itemList.getItem(0);
                modify.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        Log.d("matte", item.getTitle().toString());

                        Food f = getChild(groupPosition, childPosition);

                        /**
                         * GO TO EDIT_FOOD_FRAGMENT
                         */
                        DailyOfferFragmentDirections.ActionDailyOfferIdToEditFoodFragmentId action =
                                        DailyOfferFragmentDirections
                                                .actionDailyOfferIdToEditFoodFragmentId("ID", "Category");
                        action.setId(f.getId());
                        String category = _listDataGroup.get(groupPosition);
                        action.setCategory(category);
                        Navigation.findNavController(finalConvertView).navigate(action);

                        return false;
                    }
                });

                MenuItem remove = itemList.getItem(1);
                remove.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        if(host != null)
                            progressDialog = ProgressDialog.show(host, "", host.getString(R.string.loading));



                        Log.d("matte", item.getTitle().toString());
                        MyViewModel model = ViewModelProviders.of((FragmentActivity) host).get(MyViewModel.class);

                        //firstly remove the food from Firebase database
                        DatabaseReference reference = FirebaseDatabase.getInstance()
                                                        .getReference("restaurants/"+currentUserID+
                                                                "/Menu");
                        Food toRemove = model.getChild(groupPosition, childPosition);
                        reference.child(toRemove.getId()).removeValue();

                        //then remove it from the Storage
                        final StorageReference storageReference = FirebaseStorage
                                .getInstance()
                                .getReference()
                                .child(currentUserID +"/FoodImages/"+toRemove.getId()+".jpg");
                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                               if(progressDialog.isShowing())
                                   progressDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("matte", "Failure in image remove");
                                if(progressDialog.isShowing())
                                    progressDialog.dismiss();
                            }
                        });


                        //lastly remove it from the list
                        model.removeChild(groupPosition, childPosition);
                        notifyDataSetChanged(); //todo why it is needed even with the view model?
                        return true;
                    }
                });

            }
        });

        return convertView;
    }


    @Override
    public void onGroupExpanded(int groupPosition) {
        super.onGroupExpanded(groupPosition);

    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        Log.d("matte", "[Group]getview{ group:" + groupPosition + ", view:" + convertView + ", name:" + getGroup(groupPosition).toString() + "}");
        ViewHolder holder; //recycler view pattern

        String groupTitle = (String) getGroup(groupPosition);
        //if italian translate before printing
        if(localeShort.equals("it"))
            groupTitle = translator.translate(groupTitle);

        if (convertView == null) {
            convertView = inf.inflate(R.layout.layout_menu_group, parent, false);
            holder = new ViewHolder();
            holder.text = convertView.findViewById(R.id.groupView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(groupTitle);

        return convertView;
    }


    @Override
    public Food getChild(int groupPosition, int childPosition) {
        return this._listDataChild.get(this._listDataGroup.get(groupPosition)).get(childPosition);
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChild(groupPosition, childPosition).hashCode();
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        return this._listDataChild.get(this._listDataGroup.get(groupPosition))
                .size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataGroup.get(groupPosition);
    }


    @Override
    public int getGroupCount() {
        return this._listDataGroup.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return getGroup(groupPosition).hashCode();
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    private class FoodViewHolder {
        ImageView img;
        TextView name;
        TextView description;
        TextView price;
        TextView quantity;
    }

    private class ViewHolder {
        TextView text;
    }

}