package com.mad.poleato;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toast myToast;

    private EditText edPassword, edEmail;
    private Button signIn, signUp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signup_layout);
        myToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mAuth = FirebaseAuth.getInstance();

        //search for the views
        edPassword = (EditText) findViewById(R.id.edPassword);
        edEmail = (EditText) findViewById(R.id.edEmail);
        signIn = (Button) findViewById(R.id.ButtonSignIn);
        signUp = (Button) findViewById(R.id.ButtonSignUp);

        //set the listener
        signIn.setOnClickListener(signUpRoutine);
        signUp.setOnClickListener(signUpRoutine);

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



    public void signUp(String email, String password){

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("matte", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            myToast.setText(getString(R.string.creation_succ));
                            myToast.show();
                            access();
                        } else {
                            Log.d("matte", "createUserWithEmail:failure", task.getException());
                            myToast.setText(getString(R.string.creation_fail));
                            myToast.show();
                        }

                        // ...
                    }
                });
    }

    private View.OnClickListener signUpRoutine = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.ButtonSignUp:
                    if(!edEmail.getText().toString().isEmpty() && !edPassword.getText().toString().isEmpty())
                        signUp(edEmail.getText().toString(), edPassword.getText().toString());
                    else{
                        myToast.setText(getString(R.string.void_fields));
                        myToast.show();
                    }
                    break;

                case R.id.ButtonSignIn:
                    Intent myIntent = new Intent(SignUpActivity.this, SignInActivity.class);
                    SignUpActivity.this.startActivity(myIntent);
                    break;

            }
        }
    };
}
