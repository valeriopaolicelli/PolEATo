package com.mad.poleato.AuthentucatorD;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import androidx.navigation.Navigation;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.mad.poleato.R;
import com.onesignal.OneSignal;

public class Authenticator {


    public static void revokeAccess(Activity activity, View view) {
        // Firebase sign out
        //mAuth.signOut();
        GoogleSignInClient mGoogleSignInClient;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        /** Build a GoogleSignInClient with the options specified by gso. */
        mGoogleSignInClient = GoogleSignIn.getClient(activity, gso);

        Log.d("miche", "Logout");
        FirebaseAuth.getInstance().signOut();
        // Google revoke access
        mGoogleSignInClient.revokeAccess();

        OneSignal.setSubscription(false);

        /**
         *  GO TO LOGIN ****
         */
//        Navigation.findNavController(view).navigate(R.id.action_mainProfile_id_to_signInActivity);
//        getActivity().finish();
        Navigation.findNavController(view).navigate(R.id.action_global_signInActivity);
        activity.finish();
    }
}
