package com.mad.poleato.Account;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad.poleato.LineLimiter;
import com.mad.poleato.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditProfileFragment extends Fragment {

    private static final int REQUEST_TAKE_PHOTO = 1;
    private static final int RESULT_LOAD_IMG = 2;
    private String currentPhotoPath;

    private Map<String, ImageButton> imageButtons;
    private Map<String, EditText> editTextFields;
    private Map<String, CheckBox> checkBoxes;
    private Set<String> checkedTypes;
    private DatabaseReference reference;

    private View v;
    private String image;
    private FloatingActionButton change_im;
    private ImageView profileImage;
    private Switch statusSwitch;
    //if the switch is not switched by user set it to true (default=false)
    private boolean isSwitchedByApp;

    private ProgressDialog progressDialog;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            progressDialog.dismiss();
        }
    };


    String loggedID;


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

        editTextFields = new HashMap<>();
        imageButtons = new HashMap<>();
        checkBoxes = new HashMap<>();
        checkedTypes = new HashSet<>();
        imageButtons = new HashMap<>();

        loggedID = "R00";

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.edit_account_layout, container, false);

//        v.findViewById(R.id.applyMod).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                saveChanges();
//            }
//        });

        //  change_im = findViewById(R.id.change_im);


        editTextFields.put("Name",(EditText) v.findViewById(R.id.editTextName));
        editTextFields.put("Info",(EditText) v.findViewById(R.id.editTextInfo));
        editTextFields.put("Open",(EditText) v.findViewById(R.id.editTextOpen));
        editTextFields.put("Address",(EditText) v.findViewById(R.id.editTextAddress));
        editTextFields.put("Email",(EditText) v.findViewById(R.id.editTextEmail));
        editTextFields.put("Phone",(EditText) v.findViewById(R.id.editTextPhone));
        //editTextFields.put("DeliveryCost",(EditText) v.findViewById(R.id.editTextDeliveryCost));


        imageButtons.put("Name", (ImageButton) v.findViewById(R.id.cancel_name));
        imageButtons.put("Info", (ImageButton) v.findViewById(R.id.cancel_info));
        imageButtons.put("Open", (ImageButton) v.findViewById(R.id.cancel_open));
        imageButtons.put("Address", (ImageButton) v.findViewById(R.id.cancel_address));
        imageButtons.put("Email", (ImageButton) v.findViewById(R.id.cancel_email));
        imageButtons.put("Phone", (ImageButton) v.findViewById(R.id.cancel_phone));
        //editTextFields.put("DeliveryCost",(EditText) v.findViewById(R.id.cancel_deliveryCost));


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
        SwitchListener switchListener = new SwitchListener();
        statusSwitch.setOnClickListener(switchListener);

        profileImage = v.findViewById(R.id.ivBackground);

        //fill the fields with initial values (uses FireBase)
        if(getActivity() != null)
            progressDialog = ProgressDialog.show(getActivity(), "", getString(R.string.loading));
        //start a new thread to process job
        new Thread(new Runnable() {
            @Override
            public void run() {
                fillFields();
            }
        }).start();

        // set the line limiter
        EditText edOpen = v.findViewById(R.id.editTextOpen);
        EditText edInfo = v.findViewById(R.id.editTextInfo);

        LineLimiter llOpen = new LineLimiter();
        llOpen.setView(edOpen);
        llOpen.setLines(7);

        LineLimiter llInfo = new LineLimiter();
        llInfo.setView(edInfo);
        llInfo.setLines(2);


        edOpen.addTextChangedListener(llOpen);
        edInfo.addTextChangedListener(llInfo);

        profileImage = v.findViewById(R.id.ivBackground);
        change_im = v.findViewById(R.id.change_im);
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
                saveChanges();
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

    }


    private void fillFields(){

        //Download text infos
        reference = FirebaseDatabase.getInstance().getReference("restaurants");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                DataSnapshot issue = dataSnapshot.child(loggedID);
                // it is setted to the first record (restaurant)
                // when the sign in and log in procedures will be handled, it will be the proper one
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children
                    for(DataSnapshot snap : issue.getChildren()){
                        if(editTextFields.containsKey(snap.getKey())){
                            if(snap.getKey().equals("DeliveryCost")){
                                DecimalFormat decimalFormat = new DecimalFormat("#.00"); //two decimal
                                String priceStr = decimalFormat.format(Double.parseDouble(snap.getValue().toString()));
                                editTextFields.get(snap.getKey()).setText(priceStr+"€");
                            }
                            else
                                editTextFields.get(snap.getKey()).setText(snap.getValue().toString());
                        }
                        else if(snap.getKey().equals("Type") && !snap.getValue().toString().isEmpty()){
                            String[] types = snap.getValue().toString().toLowerCase().split(",(\\s)*");
                            for(String t : types)
                                checkBoxes.get(t).setChecked(true);
                        }
                        else if(snap.getKey().equals("IsActive"))
                            statusSwitch.setChecked((Boolean) snap.getValue());
                    } //for end
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("matte", "onCancelled | ERROR: " + databaseError.getDetails() +
                        " | MESSAGE: " + databaseError.getMessage());
                Toast.makeText(getContext(), databaseError.getMessage().toString(), Toast.LENGTH_SHORT);
            }
        });


        //Download the profile pic
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference photoReference= storageReference.child(loggedID+"/ProfileImage/img.jpg");

        final long ONE_MEGABYTE = 1024 * 1024;
        photoReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                profileImage.setImageBitmap(bmp);
                //send message to main thread
                handler.sendEmptyMessage(0);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if(getActivity() != null)
                    Toast.makeText(getActivity(), "No Such file or Path found!!", Toast.LENGTH_LONG).show();
                else
                    Log.d("matte", "null context and profilePic download failed");
                //set predefined image
                profileImage.setImageResource(R.drawable.plate_fork);
                //send message to main thread
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final ScrollView mScrollView = v.findViewById(R.id.editScrollView);
        //restoring scrollview position
//        final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
//        if(position != null)
//            mScrollView.post(new Runnable() {
//                public void run() {
//                    mScrollView.scrollTo(position[0], position[1]);
//                }
//            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                setPic(currentPhotoPath);
            } else
                profileImage.setImageBitmap(decodeBase64(image));
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
                    Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                }

            } else
                profileImage.setImageBitmap(decodeBase64(image));
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


    public void saveChanges() {

        boolean wrongField = false;
        Toast.makeText(getContext(), "Saving_changes", Toast.LENGTH_LONG).show();




        // fields cannot be empty

        for(String fieldName : editTextFields.keySet()){
            EditText ed = editTextFields.get(fieldName);
            if(ed != null){
                if(ed.getText().toString().equals("")){
                    Toast.makeText(getContext(), "All fields must be filled", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getContext(), "The name must start with letters and must end with letters. Space are allowed. Numbers are not allowed", Toast.LENGTH_LONG).show();
            editTextFields.get("Name").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
        }
        if (!editTextFields.get("Info").getText().toString().matches(textRegex)) {
            wrongField = true;
            Toast.makeText(getContext(), "The description must start with letters and must end with letters. Space are allowed. Numbers are not allowed", Toast.LENGTH_LONG).show();
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
            Toast.makeText(getContext(), "Invalid Email", Toast.LENGTH_LONG).show();
            editTextFields.get("Email").setBackground(ContextCompat.getDrawable(getContext(), R.drawable.border_wrong_field));
        }

        if(!wrongField){

            // TODO here save all the data to the DB
            String types = "";
            if(!checkedTypes.isEmpty()){
                //create the type string
                for(String t : checkedTypes){
                    types += t+", ";
                }
                //remove the last comma
                types = types.substring(0, types.length()-2);
            }
            reference.child(loggedID).child("Type").setValue(types);

            reference.child(loggedID).child("IsActive").setValue(statusSwitch.isChecked());

            for(String fieldName : editTextFields.keySet()){
                EditText ed = editTextFields.get(fieldName);
                reference.child(loggedID).child(fieldName).setValue(ed.getText().toString());
            }

            // Save profile pic to the DB
            Bitmap img = ((BitmapDrawable) profileImage.getDrawable()).getBitmap();
            uploadFile(img);


            //wait 2secs before come back to the AccountFragment. The image must be loaded totally to FireBase
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            Toast.makeText(getContext(), "Saved", Toast.LENGTH_LONG).show();

            /**
             * GO TO ACCOUNT_FRAGMENT
             */
            Navigation.findNavController(v).navigate(R.id.action_editProfile_id_to_account_id);
            /**
             *
             */
        }
    }



    private void uploadFile(Bitmap bitmap) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageReference.child(loggedID+"/ProfileImage/img.jpg").putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Log.d("matte", "Upload failed");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                Log.d("matte", "downloadUrl-->" + downloadUrl);
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




    public void changeImage(View view) {
        android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(view.getContext(), change_im);
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


    private class SwitchListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if(getActivity() == null){
                Log.d("matte", "NULL context in OnClick of the Switch");
                return;
            }
            final Boolean isChecked = statusSwitch.isChecked();
            String msg = "";
            //restore previous value to block the change before AlertDialog
            statusSwitch.setChecked(!isChecked);
            if(isChecked)
                msg += getString(R.string.go_active_message);
            else
                msg += getString(R.string.go_inactive_message);

            new AlertDialog.Builder(getActivity()).setMessage(msg)
                    .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                            statusSwitch.setChecked(isChecked);
                        }
                    })
                    .setNegativeButton(getString(R.string.no), null)
                    .show();
        }
    }

}
