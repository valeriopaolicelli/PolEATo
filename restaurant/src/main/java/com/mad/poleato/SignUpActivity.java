package com.mad.poleato;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.navigation.Navigation;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mad.poleato.DailyOffer.Food;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toast myToast;
    private DatabaseReference dbReference;

    private Map<String, ImageButton> imageButtons;
    private Map<String, EditText> editTextFields;
    private Button signIn, signUp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signup_layout);
        myToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mAuth = FirebaseAuth.getInstance();
        dbReference = FirebaseDatabase.getInstance().getReference("users");


        //search for the views
        editTextFields = new HashMap<>();
        imageButtons = new HashMap<>();
        collectFields();

        signIn = (Button) findViewById(R.id.ButtonSignIn);
        signUp = (Button) findViewById(R.id.ButtonSignUp);

        //set the listener
        signIn.setOnClickListener(signUpRoutine);
        signUp.setOnClickListener(signUpRoutine);

    }

    @Override
    protected void onResume() {
        super.onResume();

        handleButton();
        buttonListener();
    }

    public void collectFields(){
        editTextFields.put("Name",(EditText)findViewById(R.id.edName));
        editTextFields.put("Address",(EditText)findViewById(R.id.edAddress));
        editTextFields.put("Email",(EditText)findViewById(R.id.edEmail));
        editTextFields.put("Phone",(EditText)findViewById(R.id.edPhone));
        editTextFields.put("Password",(EditText)findViewById(R.id.edPassword));

        imageButtons.put("Name", (ImageButton)findViewById(R.id.cancel_name));
        imageButtons.put("Address", (ImageButton)findViewById(R.id.cancel_address));
        imageButtons.put("Email", (ImageButton)findViewById(R.id.cancel_email));
        imageButtons.put("Phone", (ImageButton)findViewById(R.id.cancel_phone));
        imageButtons.put("Password", (ImageButton)findViewById(R.id.cancel_password));

    }

    public void clearText(View view) {
        if(view.getId() == R.id.cancel_email)
            editTextFields.get("Email").setText("");
        else if(view.getId() == R.id.cancel_password)
            editTextFields.get("Password").setText("");
        else if (view.getId() == R.id.cancel_name)
            editTextFields.get("Name").setText("");
        else if(view.getId() == R.id.cancel_address)
            editTextFields.get("Address").setText("");
        else if(view.getId() == R.id.cancel_phone)
            editTextFields.get("Phone").setText("");
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

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
            access();
    }

    //access to the app
    public void access(){
        Intent myIntent = new Intent(SignUpActivity.this, NavigatorActivity.class);
        SignUpActivity.this.startActivity(myIntent);
    }



    public void signUp(){

        mAuth.createUserWithEmailAndPassword(editTextFields.get("Email").getText().toString(),
                editTextFields.get("Password").getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("matte", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            dbReference.child(user.getUid()).setValue("restaurant");
                            myToast.setText(getString(R.string.creation_succ));
                            myToast.show();

                            /*
                             * save account details
                             */
                            DatabaseReference reference= FirebaseDatabase.getInstance()
                                    .getReference("restaurants")
                                    .child(user.getUid());
                            /*
                             * Capitalize first letter, clear spaces and store into db
                             */
                            String name= editTextFields.get("Name").getText().toString().substring(0,1).toUpperCase() +
                                    editTextFields.get("Name").getText().toString().substring(1);
                            name= name.trim().replaceAll(" +", " ");

                            String address= editTextFields.get("Address").getText().toString()
                                    .trim().replaceAll(" +", " ");

                            String email= editTextFields.get("Email").getText().toString()
                                    .trim().replaceAll(" +", "");

                            reference.child("Name").setValue(name);
                            reference.child("Email").setValue(email);
                            reference.child("Address").setValue(address);
                            reference.child("Phone").setValue(editTextFields.get("Phone").getText().toString());
                            uploadFile(user.getUid());
                            reference.child("DeliveryCost").setValue("0");
                            reference.child("IsActive").setValue(true);
                            reference.child("Info").setValue("");
                            reference.child("PriceRange").setValue("0");
                            reference.child("Type").child("it").setValue("");
                            reference.child("Type").child("en").setValue("");

                            access();
                        } else {
                            Log.d("matte", "createUserWithEmail:failure", task.getException());
                            myToast.setText(task.getException().getMessage());
                            myToast.show();
                        }

                        // ...
                    }
                });
    }

    private void uploadFile(final String currentUserID) {
        final StorageReference storageReference = FirebaseStorage
                .getInstance()
                .getReference()
                .child( currentUserID +"/ProfileImage/"+currentUserID+".jpg");

        Bitmap bitmap= BitmapFactory.decodeResource(getResources(), R.drawable.plate_fork);
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
                                        .child(currentUserID + "/photoUrl")
                                        .setValue(downloadUrl);
                            }
                        });
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        Uri downloadUrl = taskSnapshot.getUploadSessionUri();

                        String s = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();
                        Log.d("matte", "downloadUrl-->" + downloadUrl);

                        myToast.setText(getString(R.string.saved));
                        myToast.show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Log.d("matte", "Upload failed");
                        myToast.setText(getString(R.string.failure));
                        myToast.show();
                    }
                });
    }

    private View.OnClickListener signUpRoutine = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.ButtonSignUp:
                    if(!wrongFields())
                        signUp();
                    break;

                case R.id.ButtonSignIn:
                    Intent myIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                    SignUpActivity.this.startActivity(myIntent);
                    finish();
                    break;

            }
        }
    };

    public boolean wrongFields() {

        boolean wrongField = false;

        // fields cannot be empty
        for(String fieldName : editTextFields.keySet()){
            EditText ed = editTextFields.get(fieldName);
            if(ed != null){
                if(ed.getText().toString().equals("")){
                    myToast.setText("All fields must be filled");
                    myToast.show();
                    ed.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_wrong_field));
                    wrongField = true;
                }
                else
                    ed.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_right_field));
            }
            else {
                return false;
            }
        }

        // REGEX FOR FIELDS VALIDATION BEFORE COMMIT
        String accentedCharacters = new String("àèìòùÀÈÌÒÙáéíóúýÁÉÍÓÚÝâêîôûÂÊÎÔÛãñõÃÑÕäëïöüÿÄËÏÖÜŸçÇßØøÅåÆæœ");
        String accentedString = new String("[a-zA-Z"+accentedCharacters+"0-9]+");
        // regex for compound name (e.g. L'acqua)
        String compoundName = new String(accentedString+"((\\s)?'"+"(\\s)?"+accentedString+")?");
        //strings separated by space. Start with string and end with string.
        String nameRegex = new String(compoundName+"(\\s("+compoundName+"\\s)*"+compoundName+")?");

        //as above with the addition punctuation
        //String punctuationRegex = new String("[\\.,\\*\\:\\'\\(\\)]");
        String textRegex = new String("[^=&\\/\\s]+([^=&\\/]+)?[^=&\\/\\s]+");

        String emailRegex = new String("^.+@[^\\.].*\\.[a-z]{2,}$");

        if (!editTextFields.get("Name").getText().toString().matches(nameRegex)) {
            wrongField = true;
            myToast.setText("The name must start with letters and must end with letters. Space are allowed. Numbers are not allowed");
            myToast.show();
            editTextFields.get("Name").setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_wrong_field));
        }
        if (!editTextFields.get("Email").getText().toString().matches(emailRegex)) {
            wrongField = true;
            myToast.setText("Invalid Email");
            myToast.show();
            editTextFields.get("Email").setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.border_wrong_field));
        }
        return wrongField;
    }

}
