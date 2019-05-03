package com.mad.poleato;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Authenticator {

    //instance of firebase auth
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //private constructor to use this class as static
    private Authenticator(){

    }

    public static FirebaseUser  retrieveIfEmailSigned(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser;
    }

    /**
     *   Check for existing Google Sign In account, if the user is already signed in
     *   the GoogleSignInAccount will be non-null.
     * @param context
     * @return user or null
     */
    public static GoogleSignInAccount retrieveIfGoogleSigned(Context context){

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(context);
        return account;
    }











}
