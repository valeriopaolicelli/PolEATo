package com.mad.poleato.DailyOffer.EditFood;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.Navigator;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad.poleato.DailyOffer.DailyOfferFragmentDirections;
import com.mad.poleato.DailyOffer.DishCategoryTranslator;
import com.mad.poleato.DailyOffer.Food;
import com.mad.poleato.NavigatorActivity;
import com.mad.poleato.R;
import com.mad.poleato.View.ViewModel.MyViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;



import static android.app.Activity.RESULT_OK;

public class EditFoodFragment extends DialogFragment {

    private Toast myToast;

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_LOAD_IMG = 2;
    private String currentPhotoPath;
    private View v; //this view


    private FloatingActionButton change_im;
    private ImageView imageFood;
    private Spinner spinnerFood;
    private String dishCategory;

    private Map<String, ImageButton> imageButtons;
    private Map<String,EditText> editTextFields;

    private Button buttonSave;
    private DishCategoryTranslator translator;

    //price ranges
    private int firstRange = 7;
    private int secondRange = 15;
    private int thirdRange = 24;

    private DatabaseReference reference;
    private String localeShort;
    private String currentUserID;
    private FirebaseAuth mAuth;

    private MyViewModel model;

    private ProgressDialog progressDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    private String toModifyID, toModifyCategory;

    //to map each category name to the position inside the spinner
    private Map<String, Integer> spinnerCategoryPosition;




    /* ***********************************
     ********   ANDROID CALLBACKS   ****
     *********************************** */

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        //translate the food Category based on the current active Locale
        String locale = Locale.getDefault().toString();
        Log.d("matte", "LOCALE: "+locale);
        localeShort = locale.substring(0, 2);

        translator = new DishCategoryTranslator();
        spinnerCategoryPosition = new HashMap<>();
        spinnerCategoryPosition.put("Starters", 0);
        spinnerCategoryPosition.put("Firsts", 1);
        spinnerCategoryPosition.put("Seconds", 2);
        spinnerCategoryPosition.put("Desserts", 3);
        spinnerCategoryPosition.put("Drinks", 4);

        model = ViewModelProviders.of(getActivity()).get(MyViewModel.class);


        toModifyID = EditFoodFragmentArgs.fromBundle(getArguments()).getId();
        toModifyCategory = EditFoodFragmentArgs.fromBundle(getArguments()).getCategory();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();


    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.edit_food_fragment, container, false);

        //collects the editText
        editTextFields = new HashMap<>();
        editTextFields.put("Name", (EditText) v.findViewById(R.id.nameFood));
        editTextFields.put("Description", (EditText) v.findViewById(R.id.editDescription));
        editTextFields.put("Price", (EditText) v.findViewById(R.id.editPrice));
        editTextFields.put("Quantity", (EditText) v.findViewById(R.id.editQuantity));

        //collects the X imageButtons
        imageButtons = new HashMap<>();
        imageButtons.put("Name", (ImageButton) v.findViewById(R.id.cancel_name));
        imageButtons.put("Description", (ImageButton) v.findViewById(R.id.cancel_description));
        imageButtons.put("Price", (ImageButton) v.findViewById(R.id.cancel_price));
        imageButtons.put("Quantity", (ImageButton) v.findViewById(R.id.cancel_quantity));

        //set the listener for the X imageButtons to clear the text
        for (ImageButton b : imageButtons.values()) {
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearText(v);
                }
            });
        }


        //retrieve the spinnerFood for the category
        spinnerFood = (Spinner) v.findViewById(R.id.spinnerFood);
        /** Create an ArrayAdapter using the string array and a default spinner layout */
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.plates_array, android.R.layout.simple_spinner_item);
        /** Specify the layout to use when the list of choices appears */
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        /** Apply the adapter to the spinner */
        spinnerFood.setAdapter(adapter);
        spinnerFood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //if italian then translate to eng before push to the DB
                if(localeShort.equals("it"))
                    dishCategory = translator.translate(parent.getItemAtPosition(position).toString());
                else
                    dishCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        change_im = v.findViewById(R.id.frag_change_im);
        imageFood = v.findViewById(R.id.imageFood);
        buttonSave = v.findViewById(R.id.button_frag_save);


        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveChanges();
            }
        });

        //set the listener to change the image
        change_im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(v);
            }
        });

        //fill the fields with initial values (uses FireBase)
        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

        fillFields();

        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handleButton();
        buttonListener();
    }



    private void fillFields(){

        //Download text infos
        reference = FirebaseDatabase.getInstance().getReference("restaurants/"+currentUserID+"/Menu/"+toModifyID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild("Name") &&
                        dataSnapshot.hasChild("Description") &&
                        dataSnapshot.hasChild("Price") &&
                        dataSnapshot.hasChild("Quantity") &&
                        dataSnapshot.hasChild("Category") &&
                        dataSnapshot.hasChild("photoUrl"))
                {
                    // it is setted to the first record (restaurant)
                    // when the sign in and log in procedures will be handled, it will be the proper one
                    if (dataSnapshot.exists()) {

                        // dataSnapshot is the "issue" node with all children
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {

                            if(editTextFields.containsKey(snap.getKey())) {
                                editTextFields.get(snap.getKey()).setText(snap.getValue().toString());
                            }
                            else if(snap.getKey().equals("Category")){
                                //set the correct spinner item
                             spinnerFood.setSelection(spinnerCategoryPosition.get(snap.getValue().toString()));
                            }
                        } //for end

                        //Download the food pic
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                        StorageReference photoReference= storageReference
                                .child(currentUserID +"/FoodImages/"+
                                        toModifyID+".jpg");

                        final long ONE_MEGABYTE = 1024 * 1024;
                        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                imageFood.setImageBitmap(bmp);
                                if(progressDialog.isShowing())
                                    handler.sendEmptyMessage(0);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.d("matte", "No image found. Default img setting");
                                //set default image if no image was set
                                imageFood.setImageResource(R.drawable.plate_fork);
                                if(progressDialog.isShowing())
                                    handler.sendEmptyMessage(0);
                            }
                        });

                    }
                } //end if

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte", "onCancelled | ERROR: " + databaseError.getDetails() +
                        " | MESSAGE: " + databaseError.getMessage());
                myToast.setText(databaseError.getMessage().toString());
                myToast.show();
            }
        });

    }


    private void saveChanges(){

        boolean wrongField= false;
        for (String fieldName : editTextFields.keySet()) {
            EditText field = editTextFields.get(fieldName);
            if(field != null){
                if (field.getText().toString().equals("")) {
                    Toast.makeText(getContext(), getContext().getString(R.string.empty_field), Toast.LENGTH_LONG).show();
                    field.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
                    wrongField = true;
                } else
                    field.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_right_field));
            }
            else
                return;
        }
        if(!editTextFields.get("Price").getText().toString().matches("[0-9]+([\\.\\,][0-9]+)?") ){
            Toast.makeText(getContext(), getContext().getString(R.string.error_format_price), Toast.LENGTH_LONG).show();
            editTextFields.get("Price").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
            wrongField = true;
        }
        if(!editTextFields.get("Quantity").getText().toString().matches("[0-9]+") ){
            Toast.makeText(getContext(), getContext().getString(R.string.error_format_quantity), Toast.LENGTH_LONG).show();
            editTextFields.get("Quantity").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
            wrongField = true;
        }
        if(!wrongField){

            //retrieve the inserted data
            String name = editTextFields.get("Name").getText().toString();
            String description = editTextFields.get("Description").getText().toString();
            String quantity = editTextFields.get("Quantity").getText().toString();
            String priceString = editTextFields.get("Price").getText().toString().replace(",", ".");
            String category = dishCategory;
            Bitmap img = ((BitmapDrawable) imageFood.getDrawable()).getBitmap();

            //insert the data into the DB
            reference.child("Name").setValue(name);
            reference.child("Description").setValue(description);
            reference.child("Quantity").setValue(quantity);
            reference.child("Price").setValue(priceString);
            reference.child("Category").setValue(category);


            //todo check if this food is present in another category (category changed)
            if(!toModifyCategory.equals(dishCategory))
                model.removeChild(toModifyCategory, toModifyID);
            Food f = new Food(toModifyID, null, name, description,
                                Double.parseDouble(priceString), Integer.parseInt(quantity), category);

            uploadFile(img, f);

            /**
             * GO TO DAILY_OFFER_FRAGMENT
             */
            Navigation.findNavController(v).navigate(R.id.action_editFoodFragment_id_to_daily_offer_id);
        }


    }





    private void uploadFile(final Bitmap bitmap, final Food f) {
        final StorageReference storageReference = FirebaseStorage
                .getInstance()
                .getReference()
                .child(currentUserID +"/FoodImages/"+f.getId()+".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageReference.putBytes(data);
        uploadTask
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //save the link to the image
                                final String downloadUrl =
                                        uri.toString();
                                FirebaseDatabase.getInstance()
                                        .getReference("restaurants")
                                        .child(currentUserID +"/Menu/"+f.getId()+"/photoUrl")
                                        .setValue(downloadUrl);
                                //set the image on the object
                                f.setImg(bitmap);
                            }
                        });
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getUploadSessionUri();

                        String s = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                        Log.d("matte", "downloadUrl-->" + downloadUrl);
                        if(getActivity() != null){
                            myToast.setText(getString(R.string.saved));
                            myToast.show();
                        }

                        /**
                         * SAVE ON MODEL_VIEW
                         */
                        model.insertChild(dishCategory, f);

                        //set the priceRange for the restaurant after the insertion
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                .child("restaurants/"+currentUserID+"/PriceRange");
                        double meanPrice = model.getMeanPrice();
                        if(meanPrice == 0)
                            reference.setValue(0);
                        else if(meanPrice < firstRange)
                            reference.setValue(1);
                        else if(meanPrice < secondRange)
                            reference.setValue(2);
                        else if(meanPrice < thirdRange)
                            reference.setValue(3);

                        if(progressDialog.isShowing())
                            handler.sendEmptyMessage(0);

                        /**
                         * GO TO DAILY_OFFER_FRAGMENT
                         */
                        Navigation.findNavController(v).navigate(R.id.action_addFoodFragment_to_daily_offer);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d("matte", "Upload failed");
                        if(getActivity() != null){
                            myToast.setText(getString(R.string.failure));
                            myToast.show();
                        }


                        /**
                         * SAVE ON MODEL_VIEW
                         */
                        //save anyway but with the default image
                        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.plate_fork);
                        f.setImg(bmp);
                        //here insert new food even without image
                        model.insertChild(dishCategory, f);

                        //set the priceRange for the restaurant after the insertion
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                                .child("restaurants/"+currentUserID+"/PriceRange");
                        double meanPrice = model.getMeanPrice();
                        if(meanPrice == 0)
                            reference.setValue(0);
                        else if(meanPrice < firstRange)
                            reference.setValue(1);
                        else if(meanPrice < secondRange)
                            reference.setValue(2);
                        else if(meanPrice < thirdRange)
                            reference.setValue(3);


                        if(progressDialog.isShowing())
                            handler.sendEmptyMessage(0);
                        /**
                         * GO TO ACCOUNT_FRAGMENT
                         */
                        Navigation.findNavController(v).navigate(R.id.action_addFoodFragment_to_daily_offer);
                        /**
                         *
                         */
                    }
                });

    }






















    public void clearText(View view) {
        if(view.getId() == R.id.cancel_name)
            editTextFields.get("Name").setText("");
        else if(view.getId() == R.id.cancel_description)
            editTextFields.get("Description").setText("");
        else if(view.getId() == R.id.cancel_price)
            editTextFields.get("Price").setText("");
        else if(view.getId() == R.id.cancel_quantity)
            editTextFields.get("Quantity").setText("");
    }

    public void handleButton(){
        for(ImageButton b : imageButtons.values())
            b.setVisibility(View.INVISIBLE);

        String[] fieldName= {"Name", "Description", "Price", "Quantity"};
        for (int i=0; i<fieldName.length; i++){
            final EditText field= editTextFields.get(fieldName[i]);
            final ImageButton button= imageButtons.get(fieldName[i]);
            if(field != null && button != null) {
                field.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if (hasFocus)
                            showButton(field, button);
                        else
                            hideButton(button);
                    }
                });

                field.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showButton(field, button);
                    }
                });
            }
        }
    }

    public void showButton(EditText field, ImageButton button){
        if(field.getText().toString().length()>0)
            button.setVisibility(View.VISIBLE);
        else
            button.setVisibility(View.INVISIBLE);
    }

    public void hideButton(ImageButton button){
        button.setVisibility(View.INVISIBLE);
    }

    public void buttonListener(){
        EditText field;
        String[] fieldName= {"Name", "Description", "Price", "Quantity"};
        for (int i=0; i<fieldName.length; i++){
            field= editTextFields.get(fieldName[i]);
            final ImageButton button= imageButtons.get(fieldName[i]);
            if(button!=null && field != null) {
                field.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        if (s.toString().trim().length() == 0) {
                            button.setVisibility(View.INVISIBLE);
                        } else {
                            button.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s.toString().trim().length() == 0) {
                            button.setVisibility(View.INVISIBLE);
                        } else {
                            button.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (s.toString().trim().length() == 0) {
                            button.setVisibility(View.INVISIBLE);
                        } else {
                            button.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            else
                return;
        }
    }

    public void collectFields(View v){

        editTextFields.put("Name",(EditText)v.findViewById(R.id.nameFood));
        editTextFields.put("Description",(EditText)v.findViewById(R.id.editDescription));
        editTextFields.put("Price",(EditText)v.findViewById(R.id.editPrice));
        editTextFields.put("Quantity",(EditText)v.findViewById(R.id.editQuantity));

        imageButtons.put("Name", (ImageButton)v.findViewById(R.id.cancel_name));
        imageButtons.put("Description", (ImageButton)v.findViewById(R.id.cancel_description));
        imageButtons.put("Price", (ImageButton)v.findViewById(R.id.cancel_price));
        imageButtons.put("Quantity", (ImageButton)v.findViewById(R.id.cancel_quantity));

        for(ImageButton b : imageButtons.values()){
            b.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    clearText(v);
                }
            });
        }
    }










    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*SharedPreferences fields = getContext().getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE);
        image = fields.getString("BackgroundTmpE", encodeTobase64());
        imageFood.setImageBitmap(decodeBase64(image));*/

    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        Dialog dialog = getDialog();
//
//        // Set dialog Full screen (You have to control even style.xml to change all params)
//        if (dialog != null) {
//            int width = ViewGroup.LayoutParams.MATCH_PARENT;
//            int height = ViewGroup.LayoutParams.MATCH_PARENT;
//            dialog.getWindow().setLayout(width, height);
//        }
//    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO){
            if (resultCode == RESULT_OK) {
                setPic(currentPhotoPath);
            }
            else {
//                SharedPreferences fields= getContext().getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE);
//                image= fields.getString("ProfileImage", encodeTobase64());
//                imageFood.setImageBitmap(decodeBase64(image));
            }
        }
        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageFood.setImageBitmap(selectedImage);
                    /*SharedPreferences.Editor editor =
                            getContext().getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE).edit();
                    editor.putString("BackgroundTmpE", encodeTobase64());
                    editor.apply();*/
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                }

            } else {
               /* SharedPreferences fields = getContext().getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE);
                image = fields.getString("BackgroundTmpE", encodeTobase64());
                imageFood.setImageBitmap(decodeBase64(image));*/
            }
        }
    }

    public void changeImage(View view) {
        PopupMenu popup = new PopupMenu(getContext(), change_im);
        popup.getMenuInflater().inflate(
                R.menu.popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            // implement click listener.
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.camera:
                        // create Intent with photoFile
                        dispatchTakePictureIntent();
                        return true;
                    case R.id.gallery:
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
                        return true;
                    case R.id.removeImage:
                        removeProfileImage();
                        return true;

                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    // create Intent with photoFile

    private void dispatchTakePictureIntent() {
        Uri photoURI;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileproviderR",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // Function to create image file with ExternalFilesDir

    private File createImageFile() throws IOException {
        // Create an image file name
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + "profile";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void removeProfileImage(){
        imageFood.setImageResource(R.drawable.plate_fork);
    }

    private void setPic(String currentPhotoPath) {
        // Get the dimensions of the View
        int targetW = imageFood.getWidth();
        int targetH = imageFood.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath, bmOptions);

        if(bitmap != null) {

            try {
                bitmap = rotateImageIfRequired(bitmap, currentPhotoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            imageFood.setImageBitmap(bitmap);
        }
    }

    private static Bitmap rotateImageIfRequired(Bitmap img, String currentPhotoPath) throws IOException {

        ExifInterface ei = new ExifInterface(currentPhotoPath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private String encodeTobase64() {
        Bitmap image = ((BitmapDrawable) imageFood.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    public Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}