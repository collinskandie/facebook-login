package com.example.collins1036602;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private TextView textView;
    private AccessTokenTracker accessTokenTracker;
    private ImageView imageView;
    private LoginButton loginButton;
    private static final String TAG="Facebook Authentication";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         mFirebaseAuth = FirebaseAuth.getInstance();
         FacebookSdk.sdkInitialize(getApplicationContext());

         textView =findViewById(R.id.profile_name);
         imageView = findViewById(R.id.profile_pic);
         loginButton = findViewById(R.id.login_button);
         loginButton.setReadPermissions("email","public_profile");
         callbackManager= CallbackManager.Factory.create();
         loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>(){
             @Override
             public void onSuccess(LoginResult  loginResult){
                 Log.d(TAG, "onSuccess" +loginResult);
                 handleFacebookToken(loginResult.getAccessToken());

             }
             @Override
             public void onCancel(){
                 Log.d(TAG, "onCancel" );
             }
             @Override
             public void onError(FacebookException error) {
                 Log.d(TAG, "onError" +error);

             }
         });

         authStateListener= new FirebaseAuth.AuthStateListener() {
             @Override
             public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                 FirebaseUser user= firebaseAuth.getCurrentUser();
                 updateUI(user);
             }
         };
         accessTokenTracker= new AccessTokenTracker() {
             @Override
             protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                 if (currentAccessToken == null){
                     mFirebaseAuth.signOut();
                 }
             }
         } ;

    }
    private void handleFacebookToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookToken" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Log.d(TAG, "Sign in Succesfull");
                    FirebaseUser user= mFirebaseAuth.getCurrentUser();
                    updateUI(user);
                }else
                {
                    Log.d(TAG,"Sign in failure");
                    Toast.makeText(MainActivity.this,"Authentication Failed",Toast.LENGTH_LONG).show();
                    updateUI(null);

                }

            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            textView.setText(user.getDisplayName());
            if (user.getPhotoUrl() != null) {
                String photoUrl = user.getPhotoUrl().toString();
                photoUrl= photoUrl + "?type=large";
                Picasso.get().load(photoUrl).into(imageView);

            }
        }
        else {
            textView.setText("");
            imageView.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
        }
        }

    @Override
    protected void onStart() {
        super.onStart();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener !=null){
            mFirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
