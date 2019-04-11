package com.example.poleato;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import static android.app.Activity.RESULT_OK;

public class AddFoodFragment extends DialogFragment {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_LOAD_IMG = 2;
    private String currentPhotoPath;

    private FragmentEListener fragmentEListener;
    private FloatingActionButton change_im;
    private ImageView imageFood;
    private Spinner spinnerFood;
    private String plateType;

    private TreeMap<String, ImageButton> imageButtons= new TreeMap<>();
    private TreeMap<String,EditText> editTextFields= new TreeMap<>();

    private Button buttonSave;

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

    public interface  FragmentEListener {
        void onInputESent(String plateType, Food food);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialog);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_food_fragment, container, false);

        spinnerFood = (Spinner) v.findViewById(R.id.spinnerFood);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.plates_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerFood.setAdapter(adapter);
        spinnerFood.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                plateType = (String) parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        change_im = v.findViewById(R.id.frag_change_im);
        imageFood = v.findViewById(R.id.imageFood);
        buttonSave = v.findViewById(R.id.button_frag_save);
        collectFields(v);
        handleButton();
        buttonListener();
        // Set listener to send DATA to main activity that sends them to DailyOfferFragment

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wrongField= false;
                String[] fieldName = {"Name", "Description", "Price", "Quantity"};
                for (int i = 0; i < fieldName.length; i++) {
                    EditText field = editTextFields.get(fieldName[i]);
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
                if(!editTextFields.get("Price").getText().toString().matches("[0-9]+(\\.[0-9]+)?") ){
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
                    Food food = new Food(((BitmapDrawable) imageFood.getDrawable()).getBitmap()
                            , editTextFields.get("Name").getText().toString()
                            , editTextFields.get("Description").getText().toString()
                            , Double.valueOf(editTextFields.get("Price").getText().toString())
                            , Integer.valueOf(editTextFields.get("Quantity").getText().toString()));

                    // I send even plateType to now where insert new food

                    fragmentEListener.onInputESent(plateType, food);
                    dismiss();
                }
            }
        });

        change_im.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(v);
            }
        });

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();

        // Set dialog Full screen (You have to control even style.xml to change all params)
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    public void updateEditText (CharSequence charSequence) {
//        editText.setText(charSequence);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if(context instanceof FragmentEListener){
            fragmentEListener = (FragmentEListener) context;
        }else {
            throw new RuntimeException(context.toString() + " must implement FragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentEListener = null;
    }

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
//                profileImage.setImageBitmap(decodeBase64(image));
            }
        }
        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    imageFood.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                }

            } else {
//                SharedPreferences fields= getContext().getSharedPreferences("ProfileDataCustomer", Context.MODE_PRIVATE);
//                image= fields.getString("ProfileImage", encodeTobase64());
//                profileImage.setImageBitmap(decodeBase64(image));
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
                        "com.example.android.fileproviderFood",
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
        imageFood.setImageResource(R.drawable.food_default);
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
