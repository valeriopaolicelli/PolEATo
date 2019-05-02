package com.mad.poleato;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toast myToast;

    private EditText edPassword, edEmail;
    private Button signIn, signUp;
    private SignInButton googleButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_layout);

        myToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mAuth = FirebaseAuth.getInstance();

        edPassword = (EditText) findViewById(R.id.edPassword);
        edEmail = (EditText) findViewById(R.id.edEmail);
        googleButton = (SignInButton) findViewById(R.id.google_button);
        signIn = (Button) findViewById(R.id.signInButton);
        signUp = (Button) findViewById(R.id.signUpButton);

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edEmail.getText().toString().isEmpty() && !edPassword.getText().toString().isEmpty())
                    signIn(edEmail.getText().toString(), edPassword.getText().toString());
                myToast.setText("Void");
                myToast.show();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(SignInActivity.this, SignUpActivity.class);
                SignInActivity.this.startActivity(myIntent);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
            access();
        //updateUI(currentUser);
    }


    public void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("matte", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            access();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("matte", "signInWithEmail:failure", task.getException());
                            myToast.setText("Authentication failed.");
                            myToast.show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });

    }

    //access to the app
    public void access(){
        Intent myIntent = new Intent(SignInActivity.this, NavigatorActivity.class);
        SignInActivity.this.startActivity(myIntent);

    }
}
