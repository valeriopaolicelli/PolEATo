package com.mad.poleato;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends AppCompatActivity {

    private Toast myToast;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    private EditText edPassword, edEmail;
    private Button signInButton, signUpButton;
    private SignInButton googleButton;
    private LoginButton facebookButton;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.signin_layout);

        myToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        mAuth = FirebaseAuth.getInstance();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                //.requestIdToken("504775808769-jbk1ab9gqb7gsi149mmvqhre1v37ji2k.apps.googleusercontent.com")
                //.requestServerAuthCode("504775808769-jbk1ab9gqb7gsi149mmvqhre1v37ji2k.apps.googleusercontent.com", false)
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //search for the views
        edPassword = (EditText) findViewById(R.id.edPassword);
        edEmail = (EditText) findViewById(R.id.edEmail);
        googleButton = (SignInButton) findViewById(R.id.google_button);
        googleButton.setSize(SignInButton.SIZE_STANDARD);
        facebookButton = (LoginButton) findViewById(R.id.facebook_button);
        signInButton = (Button) findViewById(R.id.ButtonSignIn);
        signUpButton = (Button) findViewById(R.id.ButtonSignUp);

        //set the listener
        googleButton.setOnClickListener(signInRoutine);
        facebookButton.setOnClickListener(signInRoutine);
        signInButton.setOnClickListener(signInRoutine);
        signUpButton.setOnClickListener(signInRoutine);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null)
            access();
        //check if signed in with Google
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null)
            firebaseAuthWithGoogle(account);
    }

    //access to the app
    public void access(){
        Intent myIntent = new Intent(SignInActivity.this, NavigatorActivity.class);
        SignInActivity.this.startActivity(myIntent);
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
                            myToast.setText("Logged in!");
                            myToast.show();
                            access();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("matte", "signInWithEmail:failure", task.getException());
                            myToast.setText("Authentication failed.");
                            myToast.show();
                        }

                        // ...
                    }
                });

    }


    //request code for the Google sign in activity
    int RC_SIGN_IN = 0;
    private View.OnClickListener signInRoutine = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){

                case R.id.google_button:
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                    break;

                case R.id.facebook_button:

                    break;

                case R.id.ButtonSignIn:
                    if(!edEmail.getText().toString().isEmpty() && !edPassword.getText().toString().isEmpty())
                        signIn(edEmail.getText().toString(), edPassword.getText().toString());
                    else{
                        myToast.setText("Void");
                        myToast.show();
                    }

                    break;

                case R.id.ButtonSignUp:
                    Intent myIntent = new Intent(SignInActivity.this, SignUpActivity.class);
                    SignInActivity.this.startActivity(myIntent);
                    break;



            }
        }
    };


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
           firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("matte", "signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("matte", "firebaseAuthWithGoogle:" + account.getId());

        String s = account.getIdToken();

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("matte", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            access();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("matte", "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.signin_main_layout), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                    }//onComplete end
                });
    }




}
