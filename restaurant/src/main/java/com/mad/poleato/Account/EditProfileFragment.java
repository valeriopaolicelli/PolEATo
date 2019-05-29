package com.mad.poleato.Account;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TimePicker;
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
import com.mad.poleato.LineLimiter;
import com.mad.poleato.MyDatabaseReference;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditProfileFragment extends Fragment implements TimePickerDialog.OnTimeSetListener {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_LOAD_IMG = 2;
    private String currentPhotoPath;
    private Toast myToast;

    private double latitude;
    private double longitude;

    private Map<String, ImageButton> imageButtons;
    private Map<String, EditText> editTextFields;
    private Map<String, CheckBox> checkBoxes;
    private Set<String> checkedTypes;

    private View v; //this view
    private FloatingActionButton change_im;
    private BottomNavigationView navigation;
    private ImageView profileImage;
    private Switch statusSwitch;
    private Switch switchPass; //for password

    private boolean rightPass;

    private ProgressDialog progressDialog;

    private String localeShort;
    private boolean priceRangeUninitialized;

    private String currentUserID;
    private FirebaseAuth mAuth;

    private int FLAG_OPEN_HOUR = 0;
    private int FLAG_CLOSE_HOUR = 1;
    private int FLAG_HOUR;

    private MyDatabaseReference profileReference;
    int indexReference;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public EditProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment EditProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static EditProfileFragment newInstance(String param1, String param2) {
        EditProfileFragment fragment = new EditProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        priceRangeUninitialized = false;

        //download Type based on the current active Locale
        String locale = Locale.getDefault().toString();
        Log.d("matte", "LOCALE: "+locale);
        localeShort = locale.substring(0, 2);


        myToast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);


        editTextFields = new HashMap<>();
        imageButtons = new HashMap<>();
        checkBoxes = new HashMap<>();
        checkedTypes = new HashSet<>();

        rightPass= true;

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

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.edit_account_layout, container, false);

        editTextFields.put("Name",(EditText) v.findViewById(R.id.editTextName));
        editTextFields.put("Info",(EditText) v.findViewById(R.id.editTextInfo));
        editTextFields.put("Open",(EditText) v.findViewById(R.id.editTextOpen));
        editTextFields.put("Close",(EditText) v.findViewById(R.id.editTextClose));
        editTextFields.put("Address",(EditText) v.findViewById(R.id.editTextAddress));
        editTextFields.put("Email",(EditText) v.findViewById(R.id.editTextEmail));
        editTextFields.put("Phone",(EditText) v.findViewById(R.id.editTextPhone));
        editTextFields.put("DeliveryCost",(EditText) v.findViewById(R.id.editTextDelivery));
        editTextFields.put("OldPassword", (EditText) v.findViewById(R.id.oldPass));
        editTextFields.put("NewPassword", (EditText) v.findViewById(R.id.newPass));
        editTextFields.put("ReNewPassword", (EditText) v.findViewById(R.id.reNewPass));


        imageButtons.put("Name", (ImageButton) v.findViewById(R.id.cancel_name));
        imageButtons.put("Info", (ImageButton) v.findViewById(R.id.cancel_info));
        imageButtons.put("Open", (ImageButton) v.findViewById(R.id.cancel_open));
        imageButtons.put("Close", (ImageButton) v.findViewById(R.id.cancel_close));
        imageButtons.put("Address", (ImageButton) v.findViewById(R.id.cancel_address));
        imageButtons.put("Email", (ImageButton) v.findViewById(R.id.cancel_email));
        imageButtons.put("Phone", (ImageButton) v.findViewById(R.id.cancel_phone));
        imageButtons.put("DeliveryCost",(ImageButton) v.findViewById(R.id.cancel_delivery));
        imageButtons.put("OldPassword", (ImageButton) v.findViewById(R.id.cancel_oldpass));
        imageButtons.put("NewPassword", (ImageButton) v.findViewById(R.id.cancel_newpass));
        imageButtons.put("ReNewPassword", (ImageButton) v.findViewById(R.id.cancel_renewpass));


        checkBoxes.put(getString(R.string.italian_cooking).toLowerCase(), (CheckBox)v.findViewById(R.id.italianCheckBox));
        checkBoxes.put(getString(R.string.pizza_cooking).toLowerCase(), (CheckBox)v.findViewById(R.id.pizzaCheckBox));
        checkBoxes.put(getString(R.string.kebab_cooking).toLowerCase(), (CheckBox)v.findViewById(R.id.kebabCheckBox));

        checkBoxes.put(getString(R.string.chinese_cooking).toLowerCase(), (CheckBox)v.findViewById(R.id.chineseCheckBox));
        checkBoxes.put(getString(R.string.japanese_cooking).toLowerCase(), (CheckBox)v.findViewById(R.id.japaneseCheckBox));
        checkBoxes.put(getString(R.string.thai_cooking).toLowerCase(), (CheckBox)v.findViewById(R.id.thaiCheckBox));

        checkBoxes.put(getString(R.string.hamburger_cooking).toLowerCase(), (CheckBox)v.findViewById(R.id.hamburgerCheckBox));
        checkBoxes.put(getString(R.string.american_cooking).toLowerCase(), (CheckBox)v.findViewById(R.id.americanCheckBox));
        checkBoxes.put(getString(R.string.mexican_cooking).toLowerCase(), (CheckBox)v.findViewById(R.id.mexicanCheckBox));

        statusSwitch = (Switch) v.findViewById(R.id.switchStatus);

        switchPass = (Switch) v.findViewById(R.id.switchPass);
        switchPass.setChecked(false);
        editTextFields.get("OldPassword").setEnabled(false);
        editTextFields.get("NewPassword").setEnabled(false);
        editTextFields.get("ReNewPassword").setEnabled(false);

        /** Hide bottomBar for this fragment*/
        navigation = getActivity().findViewById(R.id.navigation);
        navigation.setVisibility(View.GONE);

        //set the listener for all the checkbox
        CheckListener checkListener = new CheckListener();
        for(String t : checkBoxes.keySet())
            checkBoxes.get(t).setOnCheckedChangeListener(checkListener);

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

        //set listener to handle the switch tapping
        SwitchListener switchListener = new SwitchListener(getActivity());
        statusSwitch.setOnTouchListener(switchListener);

        profileImage = v.findViewById(R.id.ivBackground);

        // set the line limiter
        EditText edOpen = v.findViewById(R.id.editTextOpen);
        EditText edClose = v.findViewById(R.id.editTextClose);
        EditText edInfo = v.findViewById(R.id.editTextInfo);


        edOpen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(MotionEvent.ACTION_UP == motionEvent.getAction()){
                    DialogFragment timePicker = new TimePickerFragment();
                    ((TimePickerFragment) timePicker).setListener(EditProfileFragment.this);
                    FLAG_HOUR = FLAG_OPEN_HOUR;
                    timePicker.show(getActivity().getSupportFragmentManager(), "time picker open");
                }
                return false;
            }
        });

        edClose.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(MotionEvent.ACTION_UP == motionEvent.getAction()){
                    DialogFragment timePicker = new TimePickerFragment();
                    ((TimePickerFragment) timePicker).setListener(EditProfileFragment.this);
                    FLAG_HOUR = FLAG_CLOSE_HOUR;
                    timePicker.show(getActivity().getSupportFragmentManager(), "time picker close");
                }
                return false;
            }
        });
//        LineLimiter llOpen = new LineLimiter();
//        llOpen.setView(edOpen);
//        llOpen.setLines(7);

        LineLimiter llInfo = new LineLimiter();
        llInfo.setView(edInfo);
        llInfo.setLines(2);


        //edOpen.addTextChangedListener(llOpen);
        edInfo.addTextChangedListener(llInfo);

        profileImage = v.findViewById(R.id.ivBackground);
        change_im = v.findViewById(R.id.change_im);

        //fill the fields with initial values (uses FireBase)
        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));

        fillFields();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.my_edit_menu, menu);
        menu.findItem(R.id.applyMod).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    saveChanges();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        handleButton();
        buttonListener();
        handleSwitch();
    }

    private void fillFields(){

        //Download text infos
        profileReference = new MyDatabaseReference(FirebaseDatabase.getInstance()
                                                .getReference("restaurants/"+ currentUserID));
        profileReference.setValueListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //if not set upload it at the end (set it to 0)
                if(!dataSnapshot.hasChild("PriceRange"))
                    priceRangeUninitialized = true;

                if(dataSnapshot.hasChild("DeliveryCost") &&
                        dataSnapshot.hasChild("IsActive") &&
                        //dataSnapshot.hasChild("PriceRange") &&
                        dataSnapshot.hasChild("Type") &&
                        dataSnapshot.child("Type").hasChild("it") &&
                        dataSnapshot.child("Type").hasChild("en"))
                {
                    // it is setted to the first record (restaurant)
                    // when the sign in and log in procedures will be handled, it will be the proper one
                    if (dataSnapshot.exists()) {

                        //if already set do not touch it during upload phase
                        if(dataSnapshot.hasChild("PriceRange"))
                            priceRangeUninitialized = false;

                        // dataSnapshot is the "issue" node with all children
                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            if (editTextFields.containsKey(snap.getKey())) {
                                if (snap.getKey().equals("DeliveryCost")) {
                                    editTextFields.get(snap.getKey()).setText(snap.getValue().toString());
                                } else
                                    editTextFields.get(snap.getKey()).setText(snap.getValue().toString());
                            } else if (snap.getKey().equals("Type") && !snap.child(localeShort).getValue().toString().isEmpty()) {

                                String[] types = snap.child(localeShort).getValue().toString().toLowerCase().split(",(\\s)*");
                                for (String t : types)
                                    checkBoxes.get(t).setChecked(true);
                            } else if (snap.getKey().equals("IsActive")) {
                                statusSwitch.setChecked((Boolean) snap.getValue());
                            }
                        } //for end

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
                if(progressDialog.isShowing())
                    progressDialog.dismiss();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d("matte", "No image found. Default img setting");
                //set default image if no image was set
                profileImage.setImageResource(R.drawable.plate_fork);
                if(progressDialog.isShowing())
                    progressDialog.dismiss();
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
        profileImage.setImageResource(R.drawable.plate_fork);
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

    public void saveChanges() throws ParseException {

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

        //as above with the addition punctuation
        //String punctuationRegex = new String("[\\.,\\*\\:\\'\\(\\)]");
        String textRegex = new String("[^=&\\/\\s]+([^=&\\/]+)?[^=&\\/\\s]+");

        String emailRegex = new String("^.+@[^\\.].*\\.[a-z]{2,}$");

        String priceRegex = new String("\\.?[0-9]+([\\.,][0-9][0.9])?");

        String passRegex = new String("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$");


        if (!editTextFields.get("Name").getText().toString().matches(nameRegex)) {
            wrongField = true;
            myToast.setText("The name must start with letters and must end with letters. Space are allowed. Numbers are not allowed");
            myToast.show();
            editTextFields.get("Name").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
        }

        if (!editTextFields.get("Info").getText().toString().matches(textRegex)) {
            wrongField = true;
            myToast.setText("The description must start with letters and must end with letters. Space are allowed. Numbers are not allowed");
            myToast.show();
            editTextFields.get("Info").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
        }
        else{
            /*
             * valid content, check if there are some spaces or new lines removable
             * remove all useless spaces
             * remove all useless blank lines
             */
            int firstChar=0;
            int lastChar=0;
            String clearedString = editTextFields.get("Info").getText().toString().trim().replaceAll(" +", " ");
            int lenght= clearedString.length();
            if(clearedString.startsWith(" ")){
                firstChar= 1;
            }
            if(clearedString.endsWith(" ")){
                lastChar= lenght-1;
            }
            editTextFields.get("Info").setText(clearedString.substring(firstChar,lenght-lastChar));
        }

        /* REGEX FOR OPENING HOURS MISSING BECAUSE THE INSERTION MUST BE GUIDED, THE RESTAURATEUR CANNOT WRITE DIRECTLY IN THIS FIELD */

        if (!editTextFields.get("Email").getText().toString().matches(emailRegex)) {
            wrongField = true;
            myToast.setText("Invalid Email");
            myToast.show();
            editTextFields.get("Email").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
        }

        if (!editTextFields.get("DeliveryCost").getText().toString().matches(priceRegex)) {
            wrongField = true;
            myToast.setText("Invalid Price");
            myToast.show();
            editTextFields.get("DeliveryCost").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
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
        if(!wrongField){
            updateFields();
        }else{
            if(progressDialog.isShowing())
                progressDialog.dismiss();
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
        String otherLocale = "";

        if(localeShort.equals("it"))
            otherLocale = "en";
        else
            otherLocale = "it";


        TypeTranslator translator = new TypeTranslator();
        String types = "",
                translatedTypes = "";

        if(!checkedTypes.isEmpty()){
            //create the type string
            for(String t : checkedTypes){
                types += t+", ";
                translatedTypes += translator.translate(t)+", ";
            }
            //remove the last comma
            types = types.substring(0, types.length()-2);
            translatedTypes = translatedTypes.substring(0, translatedTypes.length()-2);

        }
        //insert both it and en
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("restaurants")
                .child(currentUserID);
        reference.child("Type").child(localeShort).setValue(types);
        reference.child("Type").child(otherLocale).setValue(translatedTypes);

        /*
         * save latitude and longitude of inserted address
         */

        reference.child("Coordinates").child("Geo").child("Latitude").setValue(latitude);
        reference.child("Coordinates").child("Geo").child("Longitude").setValue(longitude);

        reference.child("IsActive").setValue(statusSwitch.isChecked());
        EditText ed;
        for(String fieldName : editTextFields.keySet()){
            if (!fieldName.equals("OldPassword")
                    && !fieldName.equals("NewPassword")
                    && !fieldName.equals("ReNewPassword")) {
                ed = editTextFields.get(fieldName);
                if (fieldName.equals("DeliveryCost")) {
                    DecimalFormat decimalFormat = new DecimalFormat("#0.00"); //two decimal
                    String s = ed.getText().toString().replace(",", ".");
                    double d = Double.parseDouble(s);
                    String priceStr = decimalFormat.format(d);
                    reference.child(fieldName).setValue(priceStr);
                } else
                    reference.child(fieldName).setValue(ed.getText().toString());
            }
        }

        //if already set do not touch it during upload phase. Otherwise set it to 0
        if(priceRangeUninitialized)
            reference.child("PriceRange").setValue("0");

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
                                        .getReference("restaurants")
                                        .child(currentUserID +"/photoUrl")
                                        .setValue(downloadUrl);
                                if(progressDialog.isShowing())
                                    progressDialog.dismiss();
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
                        Navigation.findNavController(v).navigate(R.id.action_editProfile_id_to_account_id);
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
                                progressDialog.dismiss();
                        }
                        /**
                         * GO TO ACCOUNT_FRAGMENT
                         */
                        Navigation.findNavController(v).navigate(R.id.action_editProfile_id_to_account_id);
                        /**
                         *
                         */
                    }
        });

    }

    public void clearText(View view) {
        if (view.getId() == R.id.cancel_name)
            editTextFields.get("Name").setText("");
        else if(view.getId() == R.id.cancel_info)
            editTextFields.get("Info").setText("");
        else if(view.getId() == R.id.cancel_open)
            editTextFields.get("Open").setText("");
        else if(view.getId() == R.id.cancel_close)
            editTextFields.get("Close").setText("");
        else if(view.getId() == R.id.cancel_address)
            editTextFields.get("Address").setText("");
        else if(view.getId() == R.id.cancel_email)
            editTextFields.get("Email").setText("");
        else if(view.getId() == R.id.cancel_phone)
            editTextFields.get("Phone").setText("");
        else if(view.getId() == R.id.cancel_delivery)
            editTextFields.get("DeliveryCost").setText("");
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

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        String hourStr;
        String minStr;

        //convert to format HH:mm
        if(hourOfDay < 10)
            hourStr = "0" + hourOfDay;
        else
            hourStr = "" + hourOfDay;

        if(minute < 10)
            minStr = "0" + minute;
        else
            minStr = "" + minute;

        if(FLAG_HOUR == 0){
            EditText editText = (EditText) v.findViewById(R.id.editTextOpen);
            editText.setText(hourStr + ":" + minStr);
        }
        else{
            EditText editText = (EditText) v.findViewById(R.id.editTextClose);
            editText.setText(hourStr + ":" + minStr);
        }
    }

    //listener for all the checkbox
    private class CheckListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String thisFoodType = buttonView.getText().toString().toLowerCase();

            if(isChecked)
                checkedTypes.add(thisFoodType);
            else
                checkedTypes.remove(thisFoodType);
        }
    }

    private class ClearListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            clearText(v);
        }
    }

    private class SwitchListener extends OnSwipeTouchListener{

        public SwitchListener(Context c) {
            super(c);
        }

        @Override
        public void onClick() {
            super.onClick();
            Log.d("matte", "Switch :: OnClick");
            showDialog();
        }


        @Override
        public void onSwipeLeft() {
            super.onSwipeLeft();
            Log.d("matte", "Switch :: OnSwipeLeft");
            showDialog();
        }

        @Override
        public void onSwipeRight() {
            super.onSwipeRight();
            Log.d("matte", "Switch :: OnSwipeRight");
            showDialog();
        }


        private void showDialog(){
            String msg = "";
            //restore previous value to block the change before AlertDialog
            final boolean isChecked = statusSwitch.isChecked();

            if(!isChecked)
                msg += getString(R.string.go_active_message);
            else
                msg += getString(R.string.go_inactive_message);

            new AlertDialog.Builder(getActivity()).setMessage(msg)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                            statusSwitch.setChecked(!isChecked);
                            //release the lock after the last switch change
                            //statusSwitch.setClickable(false);
                        }
                    })
                    .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //release the lock after the last switch change
                            //statusSwitch.setClickable(false);
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        navigation.setVisibility(View.VISIBLE);

        profileReference.removeAllListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        profileReference.removeAllListener();
    }
}
