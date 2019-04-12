package com.example.poleato;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

public class EditProfile extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_LOAD_IMG = 2;
    private String currentPhotoPath;

    private TreeMap<String,ImageButton> imageButtons= new TreeMap<>();
    private TreeMap<String,EditText> editTextFields= new TreeMap<>();

    private String image;
    private FloatingActionButton change_im;
    private ImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_account_layout);

        Toolbar toolbar= findViewById(R.id.toolbar_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.edit);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.drawable.edit_icon);
        } else {
            Log.d("Exit", "getSupportActionBar() is null");
            finish();
        }

        //fill the maps
        collectFields();
        //fill the fields
        fillFields();

        EditText edOpen = findViewById(R.id.editTextOpen);
        EditText edType = findViewById(R.id.editTextType);
        EditText edInfo = findViewById(R.id.editTextInfo);

        LineLimiter llOpen = new LineLimiter();
        llOpen.setView(edOpen);
        llOpen.setLines(7);

        LineLimiter llType = new LineLimiter();
        llType.setView(edType);
        llType.setLines(2);

        LineLimiter llInfo = new LineLimiter();
        llInfo.setView(edInfo);
        llInfo.setLines(2);

        // set the line limiter
        edOpen.addTextChangedListener(llOpen);
        edType.addTextChangedListener(llType);
        edInfo.addTextChangedListener(llInfo);

        profileImage = findViewById(R.id.ivBackground);
        change_im = findViewById(R.id.change_im);

    }

    @Override
    protected void onResume() {
        super.onResume();
        handleButton();
        buttonListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my_edit_menu, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final ScrollView mScrollView = findViewById(R.id.editScrollView);
        //saving scrollView position
        outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()});

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        final ScrollView mScrollView = findViewById(R.id.editScrollView);
        //restoring scrollview position
        final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
        if(position != null)
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(position[0], position[1]);
                }
            });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO){
            if (resultCode == RESULT_OK) {
                setPic(currentPhotoPath);
            }
            else {
                SharedPreferences fields= getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE);
                image = fields.getString("BackgroundTmp", encodeTobase64());
                profileImage.setImageBitmap(decodeBase64(image));
            }
        }
        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    profileImage.setImageBitmap(selectedImage);
                    SharedPreferences.Editor editor =
                            this.getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE).edit();
                    editor.putString("BackgroundTmp", encodeTobase64());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                }

            } else {
                SharedPreferences fields = getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE);
                image = fields.getString("BackgroundTmp", encodeTobase64());
                profileImage.setImageBitmap(decodeBase64(image));
            }
        }
    }

    public void changeImage(View view) {
        android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(this, change_im);
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
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
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
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

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
        profileImage.setImageResource(R.drawable.food_default);
    }

    private void setPic(String currentPhotoPath) {
        // Get the dimensions of the View
        int targetW = profileImage.getWidth();
        int targetH = profileImage.getHeight();

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

            profileImage.setImageBitmap(bitmap);
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
        Bitmap image = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
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

    public void collectFields(){
        editTextFields.put("Name",(EditText)findViewById(R.id.editTextName));
        editTextFields.put("Type",(EditText)findViewById(R.id.editTextType));
        editTextFields.put("Info",(EditText)findViewById(R.id.editTextInfo));
        editTextFields.put("Open",(EditText)findViewById(R.id.editTextOpen));
        editTextFields.put("Address",(EditText)findViewById(R.id.editTextAddress));
        editTextFields.put("Email",(EditText)findViewById(R.id.editTextEmail));
        editTextFields.put("Phone",(EditText)findViewById(R.id.editTextPhone));

        imageButtons.put("Name", (ImageButton)findViewById(R.id.cancel_name));
        imageButtons.put("Type", (ImageButton)findViewById(R.id.cancel_type));
        imageButtons.put("Info", (ImageButton)findViewById(R.id.cancel_info));
        imageButtons.put("Open", (ImageButton)findViewById(R.id.cancel_open));
        imageButtons.put("Address", (ImageButton)findViewById(R.id.cancel_address));
        imageButtons.put("Email", (ImageButton)findViewById(R.id.cancel_email));
        imageButtons.put("Phone", (ImageButton)findViewById(R.id.cancel_phone));

        profileImage = findViewById(R.id.ivBackground);
    }

    private void fillFields() {
        SharedPreferences fields = this.getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE);
        String name = fields.getString("Name", "Nessun valore trovato");
        String type = fields.getString("Type", "Nessun valore trovato");
        String info = fields.getString("Info", "Nessun valore trovato");
        String open = fields.getString("Open", "Nessun valore trovato");
        String email = fields.getString("Email", "Nessun valore trovato");
        String address = fields.getString("Address", "Nessun valore trovato");
        String phone = fields.getString("Phone", "Nessun valore trovato");
        image= fields.getString("Background", "Nessun valore trovato");

        editTextFields.get("Name").setText(name);
        editTextFields.get("Type").setText(type);
        editTextFields.get("Info").setText(info);
        editTextFields.get("Open").setText(open);
        editTextFields.get("Address").setText(address);
        editTextFields.get("Email").setText(email);
        editTextFields.get("Phone").setText(phone);
        if(image.equals("Nessun valore trovato"))
            profileImage.setImageResource(R.mipmap.new_york_restaurant);
        else
            profileImage.setImageBitmap(decodeBase64(image));
    }

    public void saveChanges(MenuItem item) {
        boolean wrongField = false;
        // fields cannot be empty
        String[] fieldName = {"Name", "Type", "Info", "Open", "Address", "Email", "Phone"};
        for (int i = 0; i < fieldName.length; i++) {
            EditText field = editTextFields.get(fieldName[i]);
            if(field != null){
                if (field.getText().toString().equals("")) {
                    Toast.makeText(this, "All fields must be filled", Toast.LENGTH_LONG).show();
                    field.setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
                    wrongField = true;
                } else
                    field.setBackground(ContextCompat.getDrawable(this, R.drawable.border_right_field));
            }
            else
                return;
        }


        // REGEX FOR FIELDS VALIDATION BEFORE COMMIT
        String accentedCharacters = new String("àèìòùÀÈÌÒÙáéíóúýÁÉÍÓÚÝâêîôûÂÊÎÔÛãñõÃÑÕäëïöüÿÄËÏÖÜŸçÇßØøÅåÆæœ");
        String accentedString = new String("[a-zA-Z"+accentedCharacters+"]+");
        // regex for compound name (e.g. L'acqua)
        String compoundName = new String(accentedString+"((\\s)?'"+"(\\s)?"+accentedString+")?");
        //strings separated by space. Start with string and end with string.
        String nameRegex = new String(compoundName+"(\\s("+compoundName+"\\s)*"+compoundName+")?");

        //as above with the addition punctuation
        //String punctuationRegex = new String("[\\.,\\*\\:\\'\\(\\)]");
        String textRegex = new String("[^=&%\\/\\s]+([^=&%\\/]+)?[^=&%\\/\\s]+");

        String emailRegex = new String("^.+@[^\\.].*\\.[a-z]{2,}$");

        if (!editTextFields.get("Name").getText().toString().matches(nameRegex)) {
            wrongField = true;
            Toast.makeText(this, "The name must start with letters and must end with letters. Space are allowed. Numbers are not allowed", Toast.LENGTH_LONG).show();
            editTextFields.get("Name").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
        }
        if (!editTextFields.get("Type").getText().toString().matches(textRegex)) {
            wrongField = true;
            Toast.makeText(this, "The restaurant type must start with letters and must end with letters. Space are allowed. Numbers are not allowed", Toast.LENGTH_LONG).show();
            editTextFields.get("Type").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
        }
        if (!editTextFields.get("Info").getText().toString().matches(textRegex)) {
            wrongField = true;
            Toast.makeText(this, "The description must start with letters and must end with letters. Space are allowed. Numbers are not allowed", Toast.LENGTH_LONG).show();
            editTextFields.get("Info").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
        }

        /* REGEX FOR OPENING HOURS MISSING BECAUSE THE INSERTION MUST BE GUIDED, THE RESTAURATEUR CANNOT WRITE DIRECTLY IN THIS FIELD */

        if (!editTextFields.get("Email").getText().toString().matches(emailRegex)) {
            wrongField = true;
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_LONG).show();
            editTextFields.get("Email").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
        }

        if(!wrongField){
            SharedPreferences.Editor editor = this.getSharedPreferences("ProfileDataRestaurant", Context.MODE_PRIVATE).edit();

            editor.putString("Name", editTextFields.get("Name").getText().toString());
            editor.putString("Type", editTextFields.get("Type").getText().toString());
            editor.putString("Info", editTextFields.get("Info").getText().toString());
            editor.putString("Open", editTextFields.get("Open").getText().toString());
            editor.putString("Address", editTextFields.get("Address").getText().toString());
            editor.putString("Email", editTextFields.get("Email").getText().toString());
            editor.putString("Phone", editTextFields.get("Phone").getText().toString());
            editor.putString("Background", encodeTobase64());
            editor.apply();
            Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();

            finish();
        }
    }

    public void clearText(View view) {
        if (view.getId() == R.id.cancel_name)
            editTextFields.get("Name").setText("");
        else if(view.getId() == R.id.cancel_type)
            editTextFields.get("Type").setText("");
        else if(view.getId() == R.id.cancel_info)
            editTextFields.get("Info").setText("");
        else if(view.getId() == R.id.cancel_open)
            editTextFields.get("Open").setText("");
        else if(view.getId() == R.id.cancel_address)
            editTextFields.get("Address").setText("");
        else if(view.getId() == R.id.cancel_email)
            editTextFields.get("Email").setText("");
        else if(view.getId() == R.id.cancel_phone)
            editTextFields.get("Phone").setText("");
    }
/*
    public void removeProfileImage(){
        profileImage.setImageResource(R.drawable.empty_background);
    }
*/
    public void handleButton(){
        for(ImageButton b : imageButtons.values())
            b.setVisibility(View.INVISIBLE);

        String[] fieldName= {"Name", "Type", "Info", "Open", "Address", "Email", "Phone"};
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

    public void buttonListener(){
        EditText field;
        String[] fieldName= {"Name", "Type", "Info", "Open", "Address", "Email", "Phone"};
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

    public void showButton(EditText field, ImageButton button){
        if(field.getText().toString().length()>0)
            button.setVisibility(View.VISIBLE);
        else
            button.setVisibility(View.INVISIBLE);
    }

    public void hideButton(ImageButton button){
        button.setVisibility(View.INVISIBLE);
    }

}

