package com.mad.poleato;

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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfile extends AppCompatActivity {

    private TreeMap<String,ImageButton> imageButtons= new TreeMap<>();
    private TreeMap<String,EditText> editTextFields= new TreeMap<>();
    private DatabaseReference reference;

    static final int REQUEST_TAKE_PHOTO = 1;
    private static int RESULT_LOAD_IMG = 2;

    private String currentPhotoPath;
    private String image;
    private static CircleImageView profileImage;
    private FloatingActionButton change_im;
    private Switch switchPass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.edit);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.mipmap.baseline_edit_white_24dp);
        } else {
            Log.d("Exit", "getSupportActionBar() is null");
            finish();
        }
        profileImage = findViewById(R.id.profile_image);
        change_im = findViewById(R.id.change_im);
        //fill the maps
        collectFields();
        //fill the fields
        fillFields();

    }

    @Override
    protected void onResume() {
        super.onResume();
        handleButton();
        buttonListener();
        handleSwitch();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_edit_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /*TextView nameField = findViewById(R.id.editTextName),
                surnameField = findViewById(R.id.editTextSurname),
                addressField = findViewById(R.id.editTextAddress),
                emailField = findViewById(R.id.editTextEmail),
                phoneField = findViewById(R.id.editTextPhone);

        outState.putString("name_field", nameField.getText().toString());
        outState.putString("surname_field", surnameField.getText().toString());
        outState.putString("address_field", addressField.getText().toString());
        outState.putString("email_field", emailField.getText().toString());
        outState.putString("phone_field", phoneField.getText().toString());*/

        final ScrollView mScrollView = findViewById(R.id.editScrollView);
        //saving scrollView position
        outState.putIntArray("ARTICLE_SCROLL_POSITION",
            new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()});
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        /*TextView nameField = findViewById(R.id.editTextName),
                surnameField = findViewById(R.id.editTextSurname),
                addressField = findViewById(R.id.editTextAddress),
                emailField = findViewById(R.id.editTextEmail),
                phoneField = findViewById(R.id.editTextPhone);

        nameField.setText(savedInstanceState.getCharSequence("name_field"));
        surnameField.setText(savedInstanceState.getCharSequence("surname_field"));
        addressField.setText(savedInstanceState.getCharSequence("address_field"));
        emailField.setText(savedInstanceState.getCharSequence("email_field"));
        phoneField.setText(savedInstanceState.getCharSequence("phone_field"));*/

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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO){
            if (resultCode == RESULT_OK) {
                setPic(currentPhotoPath);
            }
            else {
                SharedPreferences fields= this.getSharedPreferences("ProfileDataDeliveryMan", Context.MODE_PRIVATE);
                image= fields.getString("ProfileImage", encodeTobase64());
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
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                }

            } else {
                SharedPreferences fields= this.getSharedPreferences("ProfileDataDeliveryMan", Context.MODE_PRIVATE);
                image= fields.getString("ProfileImage", encodeTobase64());
                profileImage.setImageBitmap(decodeBase64(image));
            }
        }
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

    public static String encodeTobase64() {
        Bitmap image = ((BitmapDrawable)profileImage.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public void collectFields(){
        editTextFields.put("Name",(EditText)findViewById(R.id.editTextName));
        editTextFields.put("Surname",(EditText)findViewById(R.id.editTextSurname));
        editTextFields.put("Address",(EditText)findViewById(R.id.editTextAddress));
        editTextFields.put("Email",(EditText)findViewById(R.id.editTextEmail));
        editTextFields.put("Phone",(EditText)findViewById(R.id.editTextPhone));
        editTextFields.put("OldPassword", (EditText)findViewById(R.id.oldPass));
        editTextFields.put("NewPassword", (EditText)findViewById(R.id.newPass));
        editTextFields.put("ReNewPassword", (EditText)findViewById(R.id.reNewPass));

        imageButtons.put("Name", (ImageButton)findViewById(R.id.cancel_name));
        imageButtons.put("Surname", (ImageButton)findViewById(R.id.cancel_surname));
        imageButtons.put("Address", (ImageButton)findViewById(R.id.cancel_address));
        imageButtons.put("Email", (ImageButton)findViewById(R.id.cancel_email));
        imageButtons.put("Phone", (ImageButton)findViewById(R.id.cancel_phone));
        imageButtons.put("OldPassword", (ImageButton)findViewById(R.id.cancel_oldpass));
        imageButtons.put("NewPassword", (ImageButton)findViewById(R.id.cancel_newpass));
        imageButtons.put("ReNewPassword", (ImageButton)findViewById(R.id.cancel_renewpass));

        switchPass= findViewById(R.id.switchPass);
    }

    private void fillFields() {
        reference= FirebaseDatabase.getInstance().getReference("deliveryman");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot issue= dataSnapshot.child("D00");
                // TODO when log in and sign in will be enabled
                // it is fixed to the first record (customer)
                // when the sign in and log in procedures will be handled, it will be the proper one

                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children
                    String[] fieldName = {"Name", "Surname", "Address", "Email", "Phone"};
                    for (int i = 0; i < fieldName.length; i++) {
                        EditText field = editTextFields.get(fieldName[i]);
                        field.setText(issue.child(fieldName[i]).getValue().toString());
                    }
                    editTextFields.get("Name").setText(issue.child("Name").getValue().toString());
                    editTextFields.get("Surname").setText(issue.child("Surname").getValue().toString());
                    editTextFields.get("Address").setText(issue.child("Address").getValue().toString());
                    editTextFields.get("Email").setText(issue.child("Email").getValue().toString());
                    editTextFields.get("Phone").setText(issue.child("Phone").getValue().toString());
                    //TODO retrieve the profile image from DB
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage().toString(), Toast.LENGTH_SHORT);
            }
        });

        //TODO retrieve the profile image from DB
        //image= fields.getString("ProfileImage", "Immagine non trovata");
        //profileImage.setImageBitmap(decodeBase64(image));

        //TODO store password in the DB (how from security pov?!)
        switchPass.setChecked(false);

        editTextFields.get("OldPassword").setEnabled(false);
        editTextFields.get("NewPassword").setEnabled(false);
        editTextFields.get("ReNewPassword").setEnabled(false);

    }

    public void saveChanges(MenuItem item) {
        boolean wrongField = false;
        // fields cannot be empty
        String[] fieldName = {"Name", "Surname", "Address", "Email", "Phone"};
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

        String emailRegex = new String("^.+@[^\\.].*\\.[a-z]{2,}$");

        String passRegex = new String("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$");

        if (!editTextFields.get("Name").getText().toString().matches(nameRegex)) {
            wrongField = true;
            Toast.makeText(this, "Only letters are allowed for the name", Toast.LENGTH_LONG).show();
            editTextFields.get("Name").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
        }
        if (!editTextFields.get("Surname").getText().toString().matches(nameRegex)) {
            wrongField = true;
            Toast.makeText(this, "Only letters are allowed for the surname", Toast.LENGTH_LONG).show();
            editTextFields.get("Surname").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
        }
        if (!editTextFields.get("Email").getText().toString().matches(emailRegex)) {
            wrongField = true;
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_LONG).show();
            editTextFields.get("Email").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
        }

        if (switchPass.isChecked()) {
            String newPass = editTextFields.get("NewPassword").getText().toString();
            String reNewPass = editTextFields.get("ReNewPassword").getText().toString();
            String oldPass = editTextFields.get("OldPassword").getText().toString();

            if(!newPass.matches(passRegex)){
                wrongField = true;
                Toast.makeText(this, "Password must contain at least 1 lowercase 1 uppercase and 1 digit", Toast.LENGTH_LONG).show();
                editTextFields.get("NewPassword").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
            }

            if (!newPass.equals(reNewPass)) {
                wrongField = true;
                Toast.makeText(this, "New password are different", Toast.LENGTH_LONG).show();
                editTextFields.get("NewPassword").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
                editTextFields.get("ReNewPassword").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
            }

            //TODO check old password on DB
            /*
                MessageDigest digest = null;
                try {
                    digest = MessageDigest.getInstance("SHA-256");
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                byte[] hash = digest.digest(editTextFields.get("OldPassword").getText().toString().getBytes(StandardCharsets.UTF_8));
            */

            if (oldPass.equals("")) {
                wrongField = true;
                Toast.makeText(this, "Old password must be filled", Toast.LENGTH_LONG).show();
                editTextFields.get("OldPassword").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
            }

            if (newPass.equals("")) {
                wrongField = true;
                Toast.makeText(this, "New password must be filled", Toast.LENGTH_LONG).show();
                editTextFields.get("NewPassword").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
            }

            if (reNewPass.equals("")) {
                wrongField = true;
                Toast.makeText(this, "Re-insert new password", Toast.LENGTH_LONG).show();
                editTextFields.get("ReNewPassword").setBackground(ContextCompat.getDrawable(this, R.drawable.border_wrong_field));
            }
        }

        if(!wrongField){
            for (int i = 0; i < fieldName.length; i++) {
                EditText field = editTextFields.get(fieldName[i]);
                reference.child("D00").child(fieldName[i]).setValue(field.getText().toString()); //TODO when the log in will be enabled,
            }
            // TODO save image into DB

            Toast.makeText(this, "Saved", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void changeImage(View view) {
        PopupMenu popup = new PopupMenu(EditProfile.this, change_im);
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
                        "com.example.android.fileproviderD",
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

    public void clearText(View view) {
        if (view.getId() == R.id.cancel_name)
            editTextFields.get("Name").setText("");
        else if(view.getId() == R.id.cancel_surname)
            editTextFields.get("Surname").setText("");
        else if(view.getId() == R.id.cancel_address)
            editTextFields.get("Address").setText("");
        else if(view.getId() == R.id.cancel_email)
            editTextFields.get("Email").setText("");
        else if(view.getId() == R.id.cancel_phone)
            editTextFields.get("Phone").setText("");
        else if(view.getId() == R.id.cancel_oldpass)
            editTextFields.get("OldPassword").setText("");
        else if(view.getId() == R.id.cancel_newpass)
            editTextFields.get("NewPassword").setText("");
        else if(view.getId() == R.id.cancel_renewpass)
            editTextFields.get("ReNewPassword").setText("");
    }

    public void removeProfileImage(){
        profileImage.setImageResource(R.drawable.image_empty);
    }

    public void handleButton(){
        for(ImageButton b : imageButtons.values())
            b.setVisibility(View.INVISIBLE);

        String[] fieldName= {"Name", "Surname", "Address", "Email", "Phone", "OldPassword", "NewPassword", "ReNewPassword"};
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
        String[] fieldName= {"Name", "Surname", "Address", "Email", "Phone", "OldPassword", "NewPassword", "ReNewPassword"};
        for (int i=0; i<fieldName.length; i++){
            final EditText field= editTextFields.get(fieldName[i]);
            final ImageButton button= imageButtons.get(fieldName[i]);
            if(button!=null && field != null) {
                field.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                        if(field.isFocused())
                            showButton(field, button);
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if(field.isFocused())
                            showButton(field, button);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if(field.isFocused())
                            showButton(field, button);
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

    public void handleSwitch(){
        if(switchPass.isChecked()){
            editTextFields.get("OldPassword").setEnabled(true);
            editTextFields.get("NewPassword").setEnabled(true);
            editTextFields.get("ReNewPassword").setEnabled(true);
        }
        else{
            editTextFields.get("OldPassword").clearFocus();
            editTextFields.get("NewPassword").clearFocus();
            editTextFields.get("ReNewPassword").clearFocus();
            editTextFields.get("OldPassword").setEnabled(false);
            editTextFields.get("NewPassword").setEnabled(false);
            editTextFields.get("ReNewPassword").setEnabled(false);
        }

        switchPass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bChecked) {
                if (bChecked) {
                    editTextFields.get("OldPassword").setEnabled(true);
                    editTextFields.get("NewPassword").setEnabled(true);
                    editTextFields.get("ReNewPassword").setEnabled(true);
                } else {
                    editTextFields.get("OldPassword").clearFocus();
                    editTextFields.get("NewPassword").clearFocus();
                    editTextFields.get("ReNewPassword").clearFocus();
                    editTextFields.get("OldPassword").setEnabled(false);
                    editTextFields.get("NewPassword").setEnabled(false);
                    editTextFields.get("ReNewPassword").setEnabled(false);
                }
            }
        });
    }
}

