package com.netron90.correction.personnalize;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
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
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private RelativeLayout comptePersonnalize;
    private LoginButton facebookButton;
    private SignInButton buttonGmail;
    private CallbackManager mCallbackManager;
    private FirebaseAuth mAuth;
    public static FirebaseUser user;
    public static final String USER_NAME = "username", USER_PHONE = "user_phone", USER_EMAIL = "user_email",
    USER_ID = "user_id";
    private SharedPreferences sharedPreferences;

    private GoogleSignInClient mGoogleSignInClient;

    private static final int RC_SIGN_IN = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //b3:0d:46:2f:9b:f8:ec:15:e3:24:5c:c5:cd:fd:d5:79:0b:3b:a8:c8
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        comptePersonnalize = findViewById(R.id.compte_personnalize);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        //Connect with Personnalize account
        comptePersonnalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectWithPersonnalize();
            }
        });

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);



        facebookButton = findViewById(R.id.login_button);
        buttonGmail = findViewById(R.id.gmail_singin_button);

        facebookButton.setReadPermissions("email", "public_profile");
        facebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Connexion Suceded
                Log.d("Facebook Connexion", "Connexction Success");

                Log.d("FacebookSuccess!!!", "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }



            @Override
            public void onCancel() {
                //Connexion Cancel
                Log.d("Facebook Connexion", "Connexction Cancel");
            }

            @Override
            public void onError(FacebookException e) {
                //Connexion Error
                Log.d("Facebook Connexion", "Connexction Error");
            }
        });


        //gmailButton    = (RelativeLayout) findViewById(R.id.gmail_layout);


        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });

        buttonGmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void connectWithPersonnalize() {
        Intent intent = new Intent(MainActivity.this, PersonnalizeForm.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser() != null || sharedPreferences.getBoolean(PersonnalizeForm.PERSONNALIZE_COMPTE, false) == true)
        {
            Log.d("Personnalize", "Facebook btn clicked!");
            Intent intent = new Intent(MainActivity.this, MainProcess.class);
            finish();
            startActivity(intent);
        }

    }

    private void handleFacebookAccessToken(final AccessToken accessToken) {
        Log.d("FacebookSuccess!!!", "handleFacebookAccessToken:" + accessToken);

        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Log.d("Facebook singIn", "Facebook Sing In Successful");
                    user = mAuth.getCurrentUser();
                    Log.d("Facebook singIn", "User info: Name: " + user.getDisplayName() + "\n: phone: "+user.getPhoneNumber() + "\n email: "+user.getEmail());
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(USER_NAME, user.getDisplayName()).commit();
                    editor.putString(USER_EMAIL, user.getEmail()).commit();
                    editor.putString(USER_PHONE, user.getPhoneNumber()).commit();
                    editor.putString(USER_ID, accessToken.getUserId()).commit();
                    Intent intent = new Intent(MainActivity.this, MainProcess.class);
                    finish();
                    startActivity(intent);
                }
                else {
                    Log.d("Facebook singIn Failed", "Facebook Sing In Failed");
                    Toast.makeText(MainActivity.this, "Facebook Sign Failed. Verify your credential and try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }
        else
        {
            // Pass the activity result back to the Facebook SDK
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }


    //GOOGLE Sign In method
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    //Handle google sign In
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("Google Sign In", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, "Connexion Failed! Try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("Google Sign In", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Google Sign In", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Log.w("Google Sign In", "Username acc: "+ acct.getDisplayName()+" Username: Firebase: "+user.getDisplayName());
                            Log.w("Google Sign In", "UserEmail acc: "+ acct.getEmail()+" Username: Firebase: "+user.getEmail());
                            Log.w("Google Sign In", "UserFamilyName acc:"+ acct.getFamilyName());
                            Log.w("Google Sign In", "UserGivenName acc:"+ acct.getGivenName());

                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(USER_NAME, user.getDisplayName()).commit();
                            editor.putString(USER_EMAIL, user.getEmail()).commit();
                            editor.putString(USER_PHONE, user.getPhoneNumber()).commit();
                            editor.putString(USER_ID, acct.getId()).commit();
                            Intent intent = new Intent(MainActivity.this, MainProcess.class);
                            finish();
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Google Sign In", "signInWithCredential:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Connexion Failed! Try again", Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

}
