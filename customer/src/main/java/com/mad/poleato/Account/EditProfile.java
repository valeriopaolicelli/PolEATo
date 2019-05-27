package com.mad.poleato.Account;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
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
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.PopupMenu;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfile extends Fragment {

    private Map<String, ImageButton> imageButtons;
    private Map<String, EditText> editTextFields;
    private DatabaseReference reference;
    private Toast myToast;

    static final int REQUEST_TAKE_PHOTO = 1;
    private static int RESULT_LOAD_IMG = 2;

    private String currentPhotoPath;
    private Bitmap image;

    private View v;
    private static CircleImageView profileImage;
    private FloatingActionButton change_im;
    private Switch switchPass; //for password

    private boolean rightPass;

    private ProgressDialog progressDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };

    private String currentUserID;
    private FirebaseAuth mAuth;

    private double latitude;
    private double longitude;

    private HashMap<String, MyDatabaseReference> dbReferenceList;

    public EditProfile() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        currentUserID = currentUser.getUid();

        // OneSignal is used to send notifications between applications

        OneSignal.startInit(getContext())
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        OneSignal.setSubscription(true);

        OneSignal.sendTag("User_ID", currentUserID);

        myToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);

        editTextFields = new HashMap<>();
        imageButtons = new HashMap<>();
        imageButtons = new HashMap<>();

        dbReferenceList= new HashMap<>();

        rightPass= true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        editTextFields.put("Name",(EditText) v.findViewById(R.id.editTextName));
        editTextFields.put("Surname",(EditText) v.findViewById(R.id.editTextSurname));
        editTextFields.put("Address",(EditText) v.findViewById(R.id.editTextAddress));
        editTextFields.put("Email",(EditText) v.findViewById(R.id.editTextEmail));
        editTextFields.put("Phone",(EditText) v.findViewById(R.id.editTextPhone));
        editTextFields.put("OldPassword", (EditText) v.findViewById(R.id.oldPass));
        editTextFields.put("NewPassword", (EditText) v.findViewById(R.id.newPass));
        editTextFields.put("ReNewPassword", (EditText) v.findViewById(R.id.reNewPass));


        imageButtons.put("Name", (ImageButton) v.findViewById(R.id.cancel_name));
        imageButtons.put("Surname",(ImageButton) v.findViewById(R.id.cancel_surname));
        imageButtons.put("Address", (ImageButton) v.findViewById(R.id.cancel_address));
        imageButtons.put("Email", (ImageButton) v.findViewById(R.id.cancel_email));
        imageButtons.put("Phone", (ImageButton) v.findViewById(R.id.cancel_phone));
        imageButtons.put("OldPassword", (ImageButton) v.findViewById(R.id.cancel_oldpass));
        imageButtons.put("NewPassword", (ImageButton) v.findViewById(R.id.cancel_newpass));
        imageButtons.put("ReNewPassword", (ImageButton) v.findViewById(R.id.cancel_renewpass));

        switchPass = (Switch) v.findViewById(R.id.switchPass);
        switchPass.setChecked(false);
        editTextFields.get("OldPassword").setEnabled(false);
        editTextFields.get("NewPassword").setEnabled(false);
        editTextFields.get("ReNewPassword").setEnabled(false);

        //set listener for all the X button to clear the text
        ClearListener clearListener = new ClearListener();
        for(String s : imageButtons.keySet())
            imageButtons.get(s).setOnClickListener(clearListener);

        //set listener to change the photo
        v.findViewById(R.id.change_im).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage();
            }
        });


        profileImage = v.findViewById(R.id.profile_image);
        change_im = v.findViewById(R.id.change_im);

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
        handleSwitch();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.my_edit_menu, menu);
        menu.findItem(R.id.applyMod).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                saveChanges();
                return true;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    private void fillFields(){

        //Download text infos
        reference = FirebaseDatabase.getInstance().getReference("customers/"+ currentUserID);
        dbReferenceList.put("customer", new MyDatabaseReference(reference));

        dbReferenceList.get("customer").setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild("Name") &&
                        dataSnapshot.hasChild("Surname") &&
                        dataSnapshot.hasChild("Address") &&
                        dataSnapshot.hasChild("Email") &&
                        dataSnapshot.hasChild("Phone")) {
                    // it is setted to the first record (restaurant)
                    // when the sign in and log in procedures will be handled, it will be the proper one
                    if (dataSnapshot.exists()) {

                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            if (editTextFields.containsKey(snap.getKey())) {

                                    editTextFields.get(snap.getKey()).setText(snap.getValue().toString());
                            }
                        } //end for
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

        //Download the profile pic
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference= storageReference.child(currentUserID +"/ProfileImage/img.jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(bmp);
                image = bmp;
                handler.sendEmptyMessage(0);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("matte", "No image found. Default img setting");
                //set default image if no image was set
                profileImage.setImageResource(R.drawable.image_empty);
                handler.sendEmptyMessage(0);
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        final ScrollView mScrollView = v.findViewById(R.id.editScrollView);
        //saving scrollView position
        outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{ mScrollView.getScrollX(), mScrollView.getScrollY()});

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                setPic(currentPhotoPath);
            }
        }
        if (requestCode == RESULT_LOAD_IMG) {
            if (resultCode == RESULT_OK) {
                try {
                    final Uri imageUri = data.getData();
                    final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    profileImage.setImageBitmap(selectedImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    if(getActivity() != null){
                        myToast.setText(getString(R.string.failure));
                        myToast.show();
                    }
                }

            }
        }
    }

    public void changeImage() {
        android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(getContext(), change_im);
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
                        "com.example.android.fileproviderC",
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
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

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
        profileImage.setImageResource(R.drawable.image_empty);
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

    public void saveChanges() {

        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getActivity().getString(R.string.loading));


        boolean wrongField = false;
        if(getActivity() != null){
            myToast.setText(getString(R.string.saving));
            myToast.show();
        }

        // fields cannot be empty

        for(String fieldName : editTextFields.keySet()){
            if(!fieldName.equals("OldPassword") &&
                    !fieldName.equals("NewPassword") &&
                    !fieldName.equals("ReNewPassword")){
                EditText ed = editTextFields.get(fieldName);
                if(ed != null){
                    if(ed.getText().toString().equals("") ){
                        myToast.setText("All fields must be filled");
                        myToast.show();
                        ed.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
                        wrongField = true;
                    }
                    else
                        ed.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_right_field));
                }
                else {
                    return;
                }
            }
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
            myToast.setText("Only letters are allowed for the name");
            myToast.show();
            editTextFields.get("Name").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
        }
        if (!editTextFields.get("Surname").getText().toString().matches(nameRegex)) {
            wrongField = true;
            myToast.setText("Only letters are allowed for the surname");
            myToast.show();
            editTextFields.get("Surname").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
        }
        if (!editTextFields.get("Email").getText().toString().matches(emailRegex)) {
            wrongField = true;
            myToast.setText("Invalid Email");
            myToast.show();
            editTextFields.get("Email").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
        }
        if (switchPass.isChecked()) {
            String newPass = editTextFields.get("NewPassword").getText().toString();
            String reNewPass = editTextFields.get("ReNewPassword").getText().toString();
            String oldPass = editTextFields.get("OldPassword").getText().toString();

            if(!newPass.matches(passRegex)){
                wrongField = true;
                myToast.setText("Password must contain at least 1 lowercase 1 uppercase and 1 digit");
                myToast.show();
                editTextFields.get("NewPassword").setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border_wrong_field));
            }

            if (!newPass.equals(reNewPass)) {
                wrongField = true;
                myToast.setText("New password are different");
                myToast.show();
                editTextFields.get("NewPassword").setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border_wrong_field));
                editTextFields.get("ReNewPassword").setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border_wrong_field));
            }

            if (oldPass.equals("")) {
                wrongField = true;
                myToast.setText("Old password must be filled");
                myToast.show();
                editTextFields.get("OldPassword").setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border_wrong_field));
            }

            if (newPass.equals("")) {
                wrongField = true;
                myToast.setText("New password must be filled");
                myToast.show();
                editTextFields.get("NewPassword").setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border_wrong_field));
            }

            if (reNewPass.equals("")) {
                wrongField = true;
                myToast.setText("Re-insert new password");
                myToast.show();
                editTextFields.get("ReNewPassword").setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border_wrong_field));
            }
        }

        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(editTextFields.get("Address").getText().toString(), 1);

            if(addresses.size() > 0) {
                if(addresses.get(0).getThoroughfare() == null) {
                    wrongField = true;
                    myToast.setText("Invalid Address");
                    myToast.show();
                }
                else {
                    latitude = addresses.get(0).getLatitude();
                    longitude = addresses.get(0).getLongitude();
                }
            }
            else{
                wrongField = true;
                myToast.setText("Invalid Address");
                myToast.show();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if(!wrongField && switchPass.isChecked()) {
            /*
             * if all the other fields are correct and user want to change the password,
             * try to do this, if failed also the other changes will be abort
             */
            String newPass = editTextFields.get("NewPassword").getText().toString();
            String oldPass = editTextFields.get("OldPassword").getText().toString();
            wrongField = !updatePassword(oldPass, newPass);
        }

        /* --------------- SAVING TO FIREBASE --------------- */
        if(!wrongField) {
            updateFields();
        }
        else{
            if(progressDialog.isShowing())
                handler.sendEmptyMessage(0);
        }
    }

    private boolean updatePassword(String oldPass, final String newPass) {
        final FirebaseUser user = mAuth.getCurrentUser();
        if(user == null){
            myToast.setText("User not logged");
            myToast.show();
            rightPass= false;
        }

        String email= mAuth.getCurrentUser().getEmail();
// Get auth credentials from the user for re-authentication. The example below shows
// email and password credentials but there are multiple possible providers,
// such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential= null;
        if (email != null)
            credential = EmailAuthProvider.getCredential(email, oldPass);

        if (user != null && credential != null) {
// Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("valerioPassword", "Password updated");
                                    updateFields();
                                } else {
                                    myToast.setText("Error password not updated");
                                    myToast.show();
                                    Log.d("valerioPassword", "Error password not updated");
                                    rightPass= false;
                                }
                            }
                        });
                    } else {
                        myToast.setText("Old password wrong");
                        myToast.show();
                        Log.d("valerioPassword", "Error old password inserted");
                        rightPass= false;
                    }
                }
            });
        }
        return false;
    }

    public void updateFields(){
        EditText ed;
        for (String fieldName : editTextFields.keySet()) {
            if (!fieldName.equals("OldPassword")
                    && !fieldName.equals("NewPassword")
                    && !fieldName.equals("ReNewPassword")) {
                ed = editTextFields.get(fieldName);
                reference.child(fieldName).setValue(ed.getText().toString());
            }
        }

        /*
         * save latitude and longitude of inserted address
         */
        reference.child("Latitude").setValue(latitude);
        reference.child("Longitude").setValue(longitude);

        // Save profile pic to the DB
        Bitmap img = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
            /*Navigation controller is moved inside this method. The image must be loaded totally to FireBase
                before come back to the AccountFragment. This is due to the fact that the image download is async */
        uploadFile(img);
    }

    private void uploadFile(Bitmap bitmap) {
        final StorageReference storageReference = FirebaseStorage
                .getInstance()
                .getReference()
                .child(currentUserID +"/ProfileImage/img.jpg");

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
                                final String downloadUrl =
                                        uri.toString();
                                FirebaseDatabase.getInstance()
                                        .getReference("customers")
                                        .child(currentUserID +"/photoUrl")
                                        .setValue(downloadUrl);

                                if(progressDialog.isShowing())
                                    handler.sendEmptyMessage(0);
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
                         * GO TO ACCOUNT_FRAGMENT
                         */
                        Navigation.findNavController(v).navigate(R.id.action_editProfile_id_to_mainProfile_id);
                        /**
                         *
                         */
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

                            if(progressDialog.isShowing())
                                handler.sendEmptyMessage(0);
                        }
                        /**
                         * GO TO ACCOUNT_FRAGMENT
                         */
                        Navigation.findNavController(v).navigate(R.id.action_editProfile_id_to_mainProfile_id);
                        /**
                         *
                         */
                    }
                });

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

    public void handleButton(){
        for(ImageButton b : imageButtons.values())
            b.setVisibility(View.INVISIBLE);

        for (String fieldName : editTextFields.keySet()){
            final EditText field= editTextFields.get(fieldName);
            final ImageButton button= imageButtons.get(fieldName);
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

        for (String fieldName : editTextFields.keySet()){
            final EditText field= editTextFields.get(fieldName);
            final ImageButton button= imageButtons.get(fieldName);
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

    private class ClearListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            clearText(v);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for(MyDatabaseReference ref : dbReferenceList.values())
            ref.removeAllListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        for (MyDatabaseReference my_ref : dbReferenceList.values())
            my_ref.removeAllListener();
    }
}
