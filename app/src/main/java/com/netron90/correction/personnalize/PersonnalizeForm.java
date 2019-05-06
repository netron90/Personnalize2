package com.netron90.correction.personnalize;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class PersonnalizeForm extends AppCompatActivity {

    private EditText userPseudo, userPassword;
    private Button personnalizeLoginBtn;
    private SharedPreferences sharedPreferences;
    public static final String PERSONNALIZE_COMPTE = "personnalize_compte";
    private Toolbar toolbar;
    private ProgressDialog pd;
    private FirebaseFirestore dbFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personnalize_form);

        userPseudo = findViewById(R.id.username);
        userPassword = findViewById(R.id.password);
        personnalizeLoginBtn = findViewById(R.id.personnalize_login);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Se connecter");

        dbFirestore = FirebaseFirestore.getInstance();

        personnalizeLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String uploadingMessage = "Connexion";
                pd = new ProgressDialog(PersonnalizeForm.this);
                pd.setTitle("Authentification de l'utilisateur");
                pd.setMessage(uploadingMessage);
                pd.setIndeterminate(false);
                pd.show();

                String pseudo = userPseudo.getText().toString();
                String password = userPassword.getText().toString();

                boolean isConnected = sharedPreferences.getBoolean(PERSONNALIZE_COMPTE, false);
                personnalizeLogin(pseudo, password, isConnected);
            }
        });

    }

    private void personnalizeLogin(String pseudo, String password, final boolean isConnectedApi) {

        //boolean isConnected = sharedPreferences.getBoolean(PERSONNALIZE_COMPTE, false);
        Log.d("PERSONNALIZE SERVER LOG", "Bouton login presses: isConnected: " + isConnectedApi);
        if(pseudo.equals("") && password.equals(""))
        {
            pd.cancel();
            Toast.makeText(PersonnalizeForm.this, "Veuillez remplir les champs", Toast.LENGTH_SHORT).show();
        }
        else {
            if(checkConnection())
            {
                AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();

                RequestParams mRequestParam = new RequestParams();
                mRequestParam.put("username", pseudo);
                mRequestParam.put("password", password);


                mAsyncHttpClient.post("http://mighty-refuge-23480.herokuapp.com/login-api", mRequestParam, new JsonHttpResponseHandler()
                {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        pd.cancel();
                        Log.d("SERVEUR RES ", "isConnected value on Success " + isConnectedApi);
                        Log.d("SERVEUR RES SUCCES", "Serveur login response success!: " + response);
                        if(isConnectedApi == false)
                        {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(PERSONNALIZE_COMPTE, true).apply();
                            try {
                               // Log.d("SERVEUR RES ", "Nom : " + response.get("nom").toString());

                               // Log.d("SERVEUR RES ", "Email : " + response.get("nom").toString());
                                if((response.get("error").toString()).equals("true"))
                                {
                                    Toast.makeText(PersonnalizeForm.this, "Une erreur est survenue. Vérifier vos informations de compter et réessayer.", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    UsersApp usersApp = new UsersApp();

                                    String nomUser = response.getJSONObject("userInfo").get("nom").toString() + " " + response.getJSONObject("userInfo").get("prenom").toString();
                                    String emailUser = response.getJSONObject("userInfo").get("email").toString();
                                    String contactUser = response.getJSONObject("userInfo").get("contact").toString();
                                    String idUser = response.getJSONObject("userInfo").get("userId").toString();

                                    Log.d("SERVEUR RES ", "Nom : " + nomUser);
                                    Log.d("SERVEUR RES ", "Email : " + emailUser);
                                    Log.d("SERVEUR RES ", "contact : " + contactUser);
                                    Log.d("SERVEUR RES ", "UserId : " + idUser);

                                    editor.putString(MainActivity.USER_NAME, nomUser).commit();
                                    editor.putString(MainActivity.USER_EMAIL, emailUser).commit();
                                    editor.putString(MainActivity.USER_PHONE, contactUser).commit();
                                    editor.putString(MainActivity.USER_ID, idUser).commit();

                                    usersApp.userIdApp = idUser;
                                    usersApp.nameUserApp = nomUser;
                                    usersApp.emailUserApp = emailUser;
                                    usersApp.contactUserapp = contactUser;
                                    usersApp.userAppFlag = true;

                                    dbFirestore.collection("users").document(idUser).set(usersApp)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent intent = new Intent(PersonnalizeForm.this, MainProcess.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK) ;
                                                    finish();
                                                    startActivity(intent);
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
//                                            Log.d("SERVEUR RES SUCCES", "Serveur login response failed!: Status code: " + statusCode + " " +
//                                                    "response String: " + responseString + " Throwable: " + throwable);
                                            Toast.makeText(PersonnalizeForm.this, "Une erreur est survenue. Vérifier vos informations de compte et réessayer.", Toast.LENGTH_SHORT).show();

                                        }
                                    });


                                }
//                                editor.putString(MainActivity.USER_NAME, (String)response.getJSONObject()get("nom")+" "+(String)response.get("prenom")).commit();
//                                editor.putString(MainActivity.USER_EMAIL, (String)response.get("email")).commit();
//                                editor.putString(MainActivity.USER_PHONE, (String)response.get("contact")).commit();
//                                editor.putString(MainActivity.USER_ID, (String)response.get("userId")).commit();
//                                Intent intent = new Intent(PersonnalizeForm.this, MainProcess.class);
//                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK) ;
//                                finish();
//                                startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }else{
                            Intent intent = new Intent(PersonnalizeForm.this, MainProcess.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK) ;
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.d("SERVEUR RES SUCCES", "Serveur login response failed!: Status code: " + statusCode + " " +
                                "response String: " + responseString + " Throwable: " + throwable);
                        Toast.makeText(PersonnalizeForm.this, "Une erreur est survenue. Vérifier vos informations de compter et réessayer.", Toast.LENGTH_SHORT).show();
                    }
                });
            }else
            {
                pd.cancel();
                Toast.makeText(this, "Aucune connexion internet disponible.", Toast.LENGTH_SHORT).show();
            }
            

        }

    }
    
    private boolean checkConnection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
