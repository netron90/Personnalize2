package com.netron90.correction.personnalize;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.netron90.correction.personnalize.Database.DiapoImagePath;
import com.netron90.correction.personnalize.Database.DiapositiveFormat;
import com.netron90.correction.personnalize.Database.DocumentUser;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

public class DetailActivity extends AppCompatActivity {

    private TextView documentNameTv, documentDeliveryDateTv, documentPageTv, powerPointFlagTv, miseEnFormeFlagTv,
    factureTotalTv, correctionFauteFactureTv, correctionPowerPointFactureTv, correctionMiseEnFormeFactureTv;

    private Button buttonSend;
    private ImageView iconMiseEnForme, iconPowerPoint;
    private  DocumentUser userDoc;

    public static TextView dateSelect;
    public static String documentNameDetail, montantFacture;
    private String fileName = null;

    public static int documentPageDetail = 0, montantTotal =0;

    public static final String PAYEMENT = "payementMode";
    private final String MONTANT_TOTAL_COMPLEMENT = "montantComplement";
    private String userName, userEmail, userPhone, userDocPath, userId;
//    private DeleteDocumentSend deleteDocumentSend;

    private SharedPreferences sharedPreferences;

    private List<DiapositiveFormat> diaposDocument = null;
    private List<List<DiapoImagePath>> imageDiapoPath = null;

    private FloatingActionButton fab;
    private AppCompatCheckBox miseEnPage, powerPoint;
    private Switch modePayement;
    private Toolbar toolbar;
    private EditText phoneNumber;
    private ProgressDialog pd;
    private final int REQUEST_USER_INFO_TEAM = 1;
    List<DiapositiveFormat> documentDiapoList;
    List<List<DiapoImagePath>>  diapoImagePath;
    private int montantTotalComplement = 0;
    private final int MONTANT_MISE_EN_PAGE = 2500, MONTANT_POWER_POINT = 10000, MONTANT_NBR_PAGE = 200;
    private FirebaseStorage storage;
    private FirebaseFirestore dbFireStore;
    private List<Task<Uri>> urlDownload = new ArrayList<>();

    private int compteurDiapoDoc = 0;
    private int compteurDiapoImage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        sharedPreferences              = PreferenceManager.getDefaultSharedPreferences(this);
        toolbar                        = (Toolbar) findViewById(R.id.toolbar);
        documentNameTv                 = (TextView) findViewById(R.id.detail_document_name);
        documentDeliveryDateTv         = (TextView) findViewById(R.id.detail_delivery_date);
        documentPageTv                 = (TextView) findViewById(R.id.detail_document_page);
        powerPointFlagTv               = (TextView) findViewById(R.id.nbr_diapo_power_point);
        miseEnFormeFlagTv              = (TextView) findViewById(R.id.nbr_page_mise_en_forme);
        factureTotalTv                 = (TextView) findViewById(R.id.facture);
        correctionFauteFactureTv       = (TextView) findViewById(R.id.facture_correction);
        correctionPowerPointFactureTv  = (TextView) findViewById(R.id.facture_power_point);
        correctionMiseEnFormeFactureTv = (TextView) findViewById(R.id.facture_mise_en_forme);
        buttonSend                     = (Button) findViewById(R.id.button_send);
        iconPowerPoint                 = (ImageView) findViewById(R.id.detail_icon_power_point);
        iconMiseEnForme                = (ImageView) findViewById(R.id.detail_icon_mise_en_forme);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Finalisation");

        userDoc  = getIntent().getParcelableExtra("documentInfo");


        userName = sharedPreferences.getString(MainActivity.USER_NAME, "NoBody");
        userEmail = sharedPreferences.getString(MainActivity.USER_EMAIL, "No Email");
        userPhone = sharedPreferences.getString(MainActivity.USER_PHONE, "No PhoneNumber");
        userId = sharedPreferences.getString(MainActivity.USER_ID, UUID.randomUUID().toString());

        documentNameTv.setText(userDoc.documentName);
        documentDeliveryDateTv.setText(userDoc.deliveryDate);
        documentPageTv.setText(String.valueOf(userDoc.pageNumber) + " Page(s)");
        storage = FirebaseStorage.getInstance();
        dbFireStore      = FirebaseFirestore.getInstance();


        correctionFauteFactureTv.setText("2000 f CFA");


        if(userDoc.powerPoint == false)
        {
            iconPowerPoint.setImageResource(R.drawable.ic_clear_black_24dp);
            powerPointFlagTv.setText("Non");
            correctionPowerPointFactureTv.setText("-");
        }
        else
        {
            //TODO: SELECT ALL DOCUMENT DIAPO IN DATABASE
            GetDiapositive getDiapositive = new GetDiapositive();
            getDiapositive.execute();
        }

        if(userDoc.miseEnForme == false)
        {
            iconMiseEnForme.setImageResource(R.drawable.ic_clear_black_24dp);
            miseEnFormeFlagTv.setText("Non");
            correctionMiseEnFormeFactureTv.setText("-");
        }
        else
        {
            iconMiseEnForme.setImageResource(R.drawable.ic_done_black_24dp);
            miseEnFormeFlagTv.setText("Oui");
            correctionMiseEnFormeFactureTv.setText(String.valueOf(userDoc.pageNumber * 100) + " f CFA");
        }

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(checkConnectionState())
                {
                    //TODO: CHECK IF USER PHONE NUMBER IS DEFINE
                    if(userPhone.equals("No PhoneNumber") || userPhone.equals(""))
                    {
                        fileName = "document_api_"+UUID.randomUUID().toString()+".docx";
                        Log.d("detail activity", "No phone number");
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());
                        alertDialog.setTitle(R.string.dialog_builder_title_phone);
                        alertDialog.setMessage(R.string.dialog_builder_message_phone);

                        View v = View.inflate(getApplicationContext(), R.layout.phone_number_dialog, null);
                        phoneNumber = (EditText)v.findViewById(R.id.phoneNum);

                        alertDialog.setView(v);
                        alertDialog.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });

                        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //TODO: SEND DOCUMENT TASK BACKGROUND


                                String uploadingMessage = "Wait a moment";
                                pd = new ProgressDialog(DetailActivity.this);
                                pd.setTitle("Document uploading");
                                pd.setMessage(uploadingMessage);
                                pd.setIndeterminate(false);
                                pd.show();

                                userPhone = phoneNumber.getText().toString();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString(MainActivity.USER_PHONE, userPhone).commit();


                                FirestoreUserDoc firestoreUserDoc = new FirestoreUserDoc();

                                firestoreUserDoc.id = userDoc.id;
                                firestoreUserDoc.documentName = userDoc.documentName;
                                firestoreUserDoc.pageNumber = userDoc.pageNumber;
                                firestoreUserDoc.nameUser = userName;
                                firestoreUserDoc.emailUser = userEmail;
                                firestoreUserDoc.phoneUser = userPhone;
                                firestoreUserDoc.documentPath = userDoc.documentPath;
                                firestoreUserDoc.powerPoint = userDoc.powerPoint;
                                firestoreUserDoc.miseEnForme = userDoc.miseEnForme;
                                firestoreUserDoc.deliveryDate = userDoc.deliveryDate;
                                firestoreUserDoc.docEnd       = false;

                                dbFireStore.collection("Document").document(userId).set(firestoreUserDoc)
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("DOCUMENT SENT", "Document sent failed!");

                                            }
                                        })
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(DetailActivity.this, "Document sent correctly!", Toast.LENGTH_SHORT).show();
                                                Log.d("DOCUMENT SENT", "Document sent correctly!");

                                                //TODO: GET AND SEND INFO FOR OTHER DOCUMENT INFO TO FIREBASE FIRESTORE
                                                SendDocumentTaskBackground sendDocumentTaskBackground = new SendDocumentTaskBackground();
                                                sendDocumentTaskBackground.execute();

                                            }
                                        });

//                                SendDocumentTaskBackground sendDocumentTaskBackground = new SendDocumentTaskBackground();
//                                sendDocumentTaskBackground.execute();
                            }
                        });
                        alertDialog.setCancelable(true);
                        alertDialog.create().show();
                    }
                    else
                    {
                        String uploadingMessage = "Wait a moment";
                        pd = new ProgressDialog(DetailActivity.this);
                        pd.setTitle("Document uploading");
                        pd.setMessage(uploadingMessage);
                        pd.setIndeterminate(false);
                        pd.show();
//                        SendDocumentTaskBackground sendDocumentTaskBackground = new SendDocumentTaskBackground();
//                        sendDocumentTaskBackground.execute();

                        FirestoreUserDoc firestoreUserDoc = new FirestoreUserDoc();

                        firestoreUserDoc.id = userDoc.id;
                        firestoreUserDoc.documentName = userDoc.documentName;
                        firestoreUserDoc.pageNumber = userDoc.pageNumber;
                        firestoreUserDoc.nameUser = userName;
                        firestoreUserDoc.emailUser = userEmail;
                        firestoreUserDoc.phoneUser = userPhone;
                        firestoreUserDoc.documentPath = userDoc.documentPath;
                        firestoreUserDoc.powerPoint = userDoc.powerPoint;
                        firestoreUserDoc.miseEnForme = userDoc.miseEnForme;
                        firestoreUserDoc.deliveryDate = userDoc.deliveryDate;
                        firestoreUserDoc.docEnd       = false;

                        dbFireStore.collection("Document").document(userId).set(firestoreUserDoc)
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("DOCUMENT SENT", "Document sent failed!");

                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(DetailActivity.this, "Document sent correctly!", Toast.LENGTH_SHORT).show();
                                        //Log.d("DOCUMENT SENT", "Document sent correctly!");
                                        SendDocumentTaskBackground sendDocumentTaskBackground = new SendDocumentTaskBackground();
                                        sendDocumentTaskBackground.execute();
                                    }
                                });
                    }
                }
                else
                {
                    Toast.makeText(DetailActivity.this, "Impossible de se connecter aux serveurs. Vérifier votre connexion internet et réessayer.", Toast.LENGTH_SHORT).show();
                }


            }
        });

//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
////                GetSendInformation getSendInformation = new GetSendInformation();
////                getSendInformation.execute();
//
//                //if MTN OR MOOV is selected
//                boolean modePaie = sharedPreferences.getBoolean(PAYEMENT, false);
//                if(!modePaie)
//                {
//                    Log.d("detail activity", "FAB Clicked");
//                    if(userPhone.equals("No PhoneNumber") || userPhone.equals(""))
//                    {
//                        fileName = "document_api_"+UUID.randomUUID().toString()+".docx";
//                        Log.d("detail activity", "No phone number");
//                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());
//                        alertDialog.setTitle(R.string.dialog_builder_title_phone);
//                        alertDialog.setMessage(R.string.dialog_builder_message_phone);
//
//                        View v = View.inflate(getApplicationContext(), R.layout.phone_number_dialog, null);
//                        phoneNumber = (EditText)v.findViewById(R.id.phoneNum);
//
//                        alertDialog.setView(v);
//                        alertDialog.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//
//                            }
//                        });
//                        alertDialog.setPositiveButton("Envoyer le document", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                userPhone = phoneNumber.getText().toString();
//                                {
//                                    if (userPhone.equals(""))
//                                    {
//                                        Toast.makeText(DetailActivity.this, "No phone number registering.", Toast.LENGTH_SHORT).show();
//                                    }
//                                    else
//                                    {
//                                        Log.d("detail activity", "phone number enter is: "+userPhone);
//                                        //TODO:Save user phone number
//                                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                                        editor.putString(MainActivity.USER_PHONE, userPhone).commit();
//                                        if(checkConnectionState())
//                                        {
//                                            //TODO: recreate doc Uri
//
//                                            Uri docUri = Uri.parse(userDocPath);
//
//
//                                            InputStream inputStream = null;
//
//                                            //create file with Uri
//                                            try {
//
//                                                //android.provider.MediaStore.Files.FileColumns.DATA
//                                                inputStream = getContentResolver().openInputStream(docUri);
//                                                AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
//                                                RequestParams mRequestParams = new RequestParams();
//
//                                                mRequestParams.put("user_name", userName);
//                                                mRequestParams.put("user_email", userEmail);
//                                                mRequestParams.put("user_phone", userPhone);
//                                                mRequestParams.put("mise_page", miseEnPage.isChecked());
//                                                mRequestParams.put("power_point", powerPoint.isChecked());
//                                                mRequestParams.put("date_select", dateSelect.getText());
//                                                mRequestParams.put("nombre_page", documentPageDetail);
//                                                mRequestParams.put("documents", inputStream, fileName);
//
//                                                mAsyncHttpClient.post("http://mighty-refuge-23480.herokuapp.com/memoire_api", mRequestParams, new JsonHttpResponseHandler() {
//                                                    ProgressDialog pd;
//                                                    @Override
//                                                    public void onStart() {
//                                                        String uploadingMessage = "Wait a moment";
//                                                        pd = new ProgressDialog(DetailActivity.this);
//                                                        pd.setTitle("Document uploading");
//                                                        pd.setMessage(uploadingMessage);
//                                                        pd.setIndeterminate(false);
//                                                        pd.show();
//                                                    }
//
//                                                    @Override
//                                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                                                        super.onSuccess(statusCode, headers, response);
//                                                        Log.d("Serveur response", "Status Code: " + statusCode + " Response body: " + response);
//                                                        AsyncHttpClient sendMailClient = new AsyncHttpClient();
//                                                        RequestParams sendMailParamsClient = new RequestParams();
//                                                        sendMailParamsClient.put("fileNameParams", fileName);
//                                                        sendMailClient.get("http://mighty-refuge-23480.herokuapp.com/send_mail_api", sendMailParamsClient, new JsonHttpResponseHandler(){
//                                                            @Override
//                                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                                                                super.onSuccess(statusCode, headers, response);
//                                                                Log.d("Serveur responseMail", "Status Code: " + statusCode + "Mail Send Success and response: " + response);
//                                                                pd.dismiss();
//                                                                //TODO: save information into database
//                                                                DeleteDocumentSend deleteDocumentSend = new DeleteDocumentSend();
//                                                                deleteDocumentSend.execute();
////                                                                final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
////                                                                        PersonnalizeDatabase.class, "scodelux").build();
////                                                                db.userDao().deleteOneDocument(documentNameTV.getText().toString());
////                                                                Intent intent = new Intent(DetailActivity.this, MainProcess.class);
////                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
////                                                                startActivity(intent);
//                                                            }
//
//                                                            @Override
//                                                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                                                super.onFailure(statusCode, headers, responseString, throwable);
//                                                                Log.d("Serveur responseMail", "Status Code: " + statusCode + "Mail Send Error");
//                                                                pd.dismiss();
//                                                            }
//                                                        });
//
//                                                    }
//
//                                                    @Override
//                                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                                        super.onFailure(statusCode, headers, responseString, throwable);
//                                                        Log.d("Serveur response", "Status Code: " + statusCode + " Response body: " + responseString + " throwable: " + throwable);
//                                                        pd.dismiss();
//                                                    }
//                                                });
//                                            }catch(FileNotFoundException e)
//                                            {
//                                                e.printStackTrace();
//                                            }
//                                        }
//                                        else
//                                        {
//                                            Toast.makeText(DetailActivity.this, R.string.connection_network_failed, Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                }
//
//                            }
//                        });
//                        alertDialog.setCancelable(true);
//                        alertDialog.create().show();
//
//                    }
//                    else
//                    {
////                        try
////                        {
////                            Log.d("detail activity", "phone number exist");
////                            //Log.d("detail activity", "phone number: "+userPhone);
////                            Log.d("detail activity", "Send mail");
////                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
////                            emailIntent.setType("message/rfc822");
////                            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"fredyannra@gmail.com"});
////                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Fichier mémoire reçu");
////                            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new StringBuilder()
////                                    .append("<h1> Information du client sur son mémoire</h1>")
////                                    .append("<p><br>Nom: <strong>"+userName+"</strong>")
////                                    .append("<br>Email: <strong>"+userEmail+"</strong>")
////                                    .append("<br>Contact: <strong>"+userPhone+"</strong>")
////                                    .append("<br>Date de remise du rapport: <strong>"+dateSelect.getText().toString()+"</strong>")
////                                    .append("<br>Nombre de page du document: <strong>"+documentPage.getText() + " pages"+"</strong>")
////                                    .append("<br>Montant facturé: <strong>"+String.valueOf(documentPageDetail * 200)+"</strong></p>")
////                                    .append("<br>Mise en forme du document: <strong>"+miseEnPage.isChecked()+"</strong></p>")
////                                    .append("<br>Power Point du document: <strong>"+powerPoint.isChecked()+"</strong></p")
////                                    .toString()));
////                            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(userDocPath));
////                            startActivityForResult(Intent.createChooser(emailIntent, "Send mail with?"), REQUEST_USER_INFO_TEAM);
////                        }
////                        catch (Throwable t)
////                        {
////                            Toast.makeText(DetailActivity.this, "failed", Toast.LENGTH_SHORT).show();
////                            Toast.makeText(DetailActivity.this, "error: "+t.toString(), Toast.LENGTH_SHORT).show();
////                        }
//
//                        if(checkConnectionState())
//                        {
//                            //TODO: recreate doc Uri
//
//                            Uri docUri = Uri.parse(userDocPath);
//
//
//                            InputStream inputStream = null;
//
//                            //create file with Uri
//                            try {
//
//                                //android.provider.MediaStore.Files.FileColumns.DATA
//                                inputStream = getContentResolver().openInputStream(docUri);
//                                AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
//                                RequestParams mRequestParams = new RequestParams();
//
//                                mRequestParams.put("user_name", userName);
//                                mRequestParams.put("user_email", userEmail);
//                                mRequestParams.put("user_phone", userPhone);
//                                mRequestParams.put("mise_page", miseEnPage.isChecked());
//                                mRequestParams.put("power_point", powerPoint.isChecked());
//                                mRequestParams.put("date_select", dateSelect.getText());
//                                mRequestParams.put("nombre_page", documentPageDetail);
//                                mRequestParams.put("documents", inputStream, fileName);
//
//                                mAsyncHttpClient.post("http://mighty-refuge-23480.herokuapp.com/memoire_api", mRequestParams, new JsonHttpResponseHandler() {
//                                    ProgressDialog pd;
//                                    @Override
//                                    public void onStart() {
//                                        String uploadingMessage = "Wait a moment";
//                                        pd = new ProgressDialog(DetailActivity.this);
//                                        pd.setTitle("Document uploading");
//                                        pd.setMessage(uploadingMessage);
//                                        pd.setIndeterminate(false);
//                                        pd.show();
//                                    }
//
//                                    @Override
//                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                                        super.onSuccess(statusCode, headers, response);
//                                        Log.d("Serveur response", "Status Code: " + statusCode + " Response body: " + response);
//                                        AsyncHttpClient sendMailClient = new AsyncHttpClient();
//                                        RequestParams sendMailParamsClient = new RequestParams();
//                                        sendMailParamsClient.put("fileNameParams", fileName);
//                                        sendMailClient.get("http://mighty-refuge-23480.herokuapp.com/send_mail_api", sendMailParamsClient, new JsonHttpResponseHandler(){
//                                            @Override
//                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
//                                                super.onSuccess(statusCode, headers, response);
//                                                Log.d("Serveur responseMail", "Status Code: " + statusCode + "Mail Send Success and response: " + response);
//                                                pd.dismiss();
//                                                //TODO: save information into database
//                                                DeleteDocumentSend deleteDocumentSend = new DeleteDocumentSend();
//                                                deleteDocumentSend.execute();
//                                            }
//
//                                            @Override
//                                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                                super.onFailure(statusCode, headers, responseString, throwable);
//                                                Log.d("Serveur responseMail", "Status Code: " + statusCode + "Mail Send Error");
//                                                pd.dismiss();
//                                            }
//                                        });
//
//                                    }
//
//                                    @Override
//                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
//                                        super.onFailure(statusCode, headers, responseString, throwable);
//                                        Log.d("Serveur response", "Status Code: " + statusCode + " Response body: " + responseString + " throwable: " + throwable);
//                                        pd.dismiss();
//                                    }
//                                });
//                            }catch(FileNotFoundException e)
//                            {
//                                e.printStackTrace();
//                            }
//                        }
//                        else
//                        {
//                            Toast.makeText(DetailActivity.this, R.string.connection_network_failed, Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                }
//
//            }
//        });

    }

    public static class SelectDate extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {


            if(month >= 1 && month < 10)
            {
                dateSelect.setText(day + "/" + "0"+month + "/" + year);
            }
            else
            {
                dateSelect.setText(day + "/" + month + "/" + year);
            }

        }
    }

//    public class DeleteDocumentSend extends AsyncTask<Void, Void, Void>
//    {
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
//                    PersonnalizeDatabase.class, "scodelux").build();
//            db.userDao().deleteOneDocument(documentNameTV.getText().toString());
//
//            documentUserList = db.userDao().selectAllDocument();
//            //)remove element from arrayList of User documents
//            //documentUserList.remove(position);
//            if(documentUserList.size() == 0)
//            {
//
//                SharedPreferences.Editor editor = MainProcess.sharedPreferences.edit();
//                editor.putBoolean(MainProcess.DOCUMENT_EXIST, false).commit();
//                //return true;
//            }
//            else
//            {
//                //documentUserList.remove(position);
//                //return false;
//            }
//           return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
////            if(aVoid == true)
////            {
////                Log.d("DetaiActivity Size Tab", "Element restants: " +  documentUserList.size());
////                Intent mainProcessActivityIntent = new Intent (DetailActivity.this, MainProcess.class);
////                mainProcessActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                finish();
////                startActivity(mainProcessActivityIntent);
////            }
////            else {
////                Log.d("DetaiActivity Size Tab", "Element restants: " +  documentUserList.size());
////                Intent mainProcessActivityIntent = new Intent (DetailActivity.this, MainProcess.class);
////                mainProcessActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                finish();
////                startActivity(mainProcessActivityIntent);
////            }
//            Intent intent = new Intent(DetailActivity.this, MainProcess.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            finish();
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(resultCode == RESULT_OK)
//        {
//            if(requestCode == REQUEST_USER_INFO_TEAM)
//            {
//                if(miseEnPage.isChecked() && powerPoint.isChecked())
//                {
//                    //TODO: Send reglementation to user with API CALL
//                    //TODO: Delete document send from data base
//                    deleteDocumentSend = new DeleteDocumentSend();
//                    deleteDocumentSend.execute();
//                }
//                else if(!miseEnPage.isChecked() && powerPoint.isChecked())
//                {
//                    //TODO: Send reglementation to user with API CALL
//                    //TODO: Delete document send from data base
//                    deleteDocumentSend = new DeleteDocumentSend();
//                    deleteDocumentSend.execute();
//                }
//                else if(miseEnPage.isChecked() && !powerPoint.isChecked())
//                {
//                    //TODO: Send reglementation to user with API CALL
//                    //TODO: Delete document send from data base
//                    deleteDocumentSend = new DeleteDocumentSend();
//                    deleteDocumentSend.execute();
//                }
//                else if(!miseEnPage.isChecked() && !powerPoint.isChecked())
//                {
//                    //TODO: Send reglementation to user with API CALL
//                    //TODO: Delete document send from data base
//                    deleteDocumentSend = new DeleteDocumentSend();
//                    deleteDocumentSend.execute();
//                }
//                else {}
//
//            }
//        }
//    }

//    public class GetDocumentUri extends AsyncTask<Void, Void, Void>{
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//
//            Uri docUri = Uri.parse(userDocPath);
//            try {
//                URI document = new URI("file://", null, docUri.getPath(), docUri.getQuery(), docUri.getFragment());
//                File file = new File(document);
//                Log.e("GET PATH", "getRealPathFromURI: " + file.getAbsolutePath());
//            } catch (URISyntaxException e) {
//                e.printStackTrace();
//            }
////            Uri docUri = Uri.parse(userDocPath);
////            String[] filePathColumn = { MediaStore.Images.Media.DATA };
////            Log.d("File URI","uri toString: "+ docUri.toString()+" uri Path: "+docUri.getPath());
////
////            Cursor cursor = getContentResolver().query(docUri, filePathColumn, null, null, null);
////            cursor.moveToFirst();
////            int columIndex = cursor.getColumnIndex(filePathColumn[0]);
////            String filePath  = cursor.getString(columIndex);
////            cursor.close();
////            Log.d("File Path","Chosen path = "+ filePath);
//
//
//            return null;
//        }
//    }

//    private String getRealPathFromURI(Context context, Uri contentUri) {
//        Cursor cursor = null;
//        try {
//            String[] proj = { MediaStore.Files.FileColumns.DATA };
//            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
//            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
//            cursor.moveToFirst();
//            return cursor.getString(column_index);
//        } catch (Exception e) {
//            Log.e("GET PATH ERROR", "getRealPathFromURI Exception : " + e.toString());
//            return "";
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//            }
//        }
//    }

    private Boolean checkConnectionState()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }

    public class GetDiapositive extends AsyncTask<Void, Void, List<DiapositiveFormat>>
    {
        @Override
        protected List<DiapositiveFormat> doInBackground(Void... voids) {
            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

            List<DiapositiveFormat> diapositiveFormats = db.userDao().selectDiapos(userDoc.id);
            return diapositiveFormats;
        }

        @Override
        protected void onPostExecute(List<DiapositiveFormat> diapositiveFormats) {
            super.onPostExecute(diapositiveFormats);
            iconPowerPoint.setImageResource(R.drawable.ic_done_black_24dp);
            powerPointFlagTv.setText(diapositiveFormats.size() + " diapos");
            if(diapositiveFormats.size() > 25)
            {
                correctionPowerPointFactureTv.setText(String.valueOf(diapositiveFormats.size() * 200) + " f CFA");
                if(userDoc.powerPoint == true && userDoc.miseEnForme == true)
                {
                    factureTotalTv.setText(String.valueOf(2000 + (diapositiveFormats.size() * 200) + (userDoc.pageNumber * 100)) + " f CFA");
                }
                else if(userDoc.powerPoint == true && userDoc.miseEnForme == false)
                {
                    factureTotalTv.setText(String.valueOf(2000 + (diapositiveFormats.size() * 200 )+ " f CFA"));
                }
                else if(userDoc.powerPoint == false && userDoc.miseEnForme == true)
                {
                    factureTotalTv.setText(String.valueOf(2000 + (userDoc.pageNumber * 100)) + " f CFA");
                }
                else if(userDoc.powerPoint == false && userDoc.miseEnForme == false)
                {
                    factureTotalTv.setText(String.valueOf(2000 + " f CFA"));
                }
                else{}
            }
            else
            {
                correctionPowerPointFactureTv.setText(String.valueOf(diapositiveFormats.size() * 100) + " f CFA");
                if(userDoc.powerPoint == true && userDoc.miseEnForme == true)
                {
                    factureTotalTv.setText(String.valueOf(2000 + (diapositiveFormats.size() * 100) + (userDoc.pageNumber * 100)) + " f CFA");
                }
                else if(userDoc.powerPoint == true && userDoc.miseEnForme == false)
                {
                    factureTotalTv.setText(String.valueOf(2000 + (diapositiveFormats.size() * 100 )+ " f CFA"));
                }
                else if(userDoc.powerPoint == false && userDoc.miseEnForme == true)
                {
                    factureTotalTv.setText(String.valueOf(2000 + (userDoc.pageNumber * 100)) + " f CFA");
                }
                else if(userDoc.powerPoint == false && userDoc.miseEnForme == false)
                {
                    factureTotalTv.setText(String.valueOf(2000 + " f CFA"));
                }
                else{}
            }

        }
    }

    public class SendDocumentTaskBackground extends  AsyncTask<Void, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Void... voids) {

            PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

            //TODO: GET ALL DIAPOSITIVE OF DOCUMENT
            documentDiapoList = db.userDao().selectDiapos(userDoc.id);
            diapoImagePath = new ArrayList<>();
            for(int i = 0; i < documentDiapoList.size(); i++)
            {
//                Log.d("DIAPO DOC", "Diapo Document: taille" + diapoDocument.size());
//                Log.d("DIAPO DOC", "Diapo Document: Title: " + diapoDocument.get(i).diapoTitle);
//                Log.d("DIAPO DOC", "Diapo Document: Content: " + diapoDocument.get(i).diapoDesc);
                //TODO: GET ALL IMAGE DIAPO
//                diapoImagePath.add()
                  List<DiapoImagePath> diapoImagePaths = db.userDao().selectDiapoImagePath(documentDiapoList.get(i).id);
                for(int j = 0; j < diapoImagePaths.size(); j++)
                {
                    List<DiapoImagePath> d = db.userDao().selectDiapoImagePath(documentDiapoList.get(i).id);
                    diapoImagePath.add(d);
//                    Log.d("DIAPO IMAGE", "Diapo Image: taille" + diapoImagePath.size());
//                    Log.d("DIAPO IMAGE", "Diapo Image: Image Path: " + diapoImagePath.get(j).imagePath);
//                    diapoImagePath.add();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);



            try
            {
                sendDataPerDiapo(compteurDiapoDoc);

            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
                pd.dismiss();
                Toast.makeText(DetailActivity.this, "Impossible d'établir une connexion avec le serveur. Réessayer plus tard.", Toast.LENGTH_SHORT).show();
            }



//            for(int i = 0; i < documentDiapoList.size(); i++)
//            {
//                posDiapo = i;
//                if(endResult1[0] == false)
//                {
//                    urlDownload = null;
//                    urlDownload = new ArrayList<>();
//
//                    //TODO: GET ALL IMAGE DIAPO
//                    for(int j = 0; j < diapoImagePath.size(); j++)
//                    {
//
//                        final int pos = j;
//                        if (endResult1[0] == false)
//                        {
//                            Log.d("DIAPO IMAGE", "Diapo Image: taille" + diapoImagePath.size());
//                            Log.d("DIAPO IMAGE", "Diapo Image: Image Path: " + diapoImagePath.get(j).imagePath);
//
//
//                            Uri uri = Uri.parse(diapoImagePath.get(j).imagePath);
//                            InputStream stream = null;
//                            try {
//
//                                stream = getContentResolver().openInputStream(uri);
//
//                                StorageReference storageRef = storage.getReference();
//                                StorageReference diapoPlaceImage = storageRef.child("diapo_image");
//                                UploadTask uploadTask = diapoPlaceImage.putStream(stream);
//
//                                final int finalPosDiapo = posDiapo;
//                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                                    @Override
//                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                                        endResult1[0] = false;
//                                        Log.d("URL DOWNLOAD", "Url download at position " + pos + " is: " + taskSnapshot.getStorage().getDownloadUrl());
//                                        urlDownload.add(taskSnapshot.getStorage().getDownloadUrl());
//
//                                        FireStoreDiapo fireStoreDiapo = new FireStoreDiapo();
//                                        fireStoreDiapo.diapoTitle = documentDiapoList.get(finalPosDiapo).diapoTitle;
//                                        fireStoreDiapo.diapoContent = documentDiapoList.get(finalPosDiapo).diapoDesc;
//                                        fireStoreDiapo.urlDownload = urlDownload;
//
//                                        dbFireStore.collection("Document").document(userId).collection("Image_diapo")
//                                                .document(userId).set(fireStoreDiapo)
//                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                    @Override
//                                                    public void onSuccess(Void aVoid) {
//                                                        endResult1[0] = false;
//                                                    }
//                                                })
//                                                .addOnFailureListener(new OnFailureListener() {
//                                                    @Override
//                                                    public void onFailure(@NonNull Exception e) {
//                                                        endResult1[0] = true;
//                                                    }
//                                                });
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//                                        endResult1[0] = true;
//                                    }
//                                });
//                            } catch (FileNotFoundException e) {
//                                e.printStackTrace();
//                                endResult1[0] = true;
//                            }
//                        }
//                        else
//                        {
//                            break;
//                        }
//
//                    }
//
//
//                }
//                else
//                {
//                    //Toast.makeText(DetailActivity.this, "Impossible d'établir une connexion avec le serveur. Réessayer plus tard.", Toast.LENGTH_SHORT).show();
//                    break;
//                }
//
//            }
//            if (endResult1[0] == false)
//            {
//                pd.dismiss();
//                Toast.makeText(DetailActivity.this, "Document Envoyé avec succès.", Toast.LENGTH_SHORT).show();
//            }
//            else {
//                pd.dismiss();
//                Toast.makeText(DetailActivity.this, "Impossible d'établir une connexion avec le serveur. Réessayer plus tard.", Toast.LENGTH_SHORT).show();
//            }
        }
    }

    private void sendDataPerDiapo(int compteurDiapo) throws FileNotFoundException {

        sendDataPerImage(compteurDiapoImage);

    }

    private void sendDataPerImage(final int positionDiapo) throws FileNotFoundException {

        Log.d("SEND IMAGE", "Image at position: " + positionDiapo);
        final int position = positionDiapo;
        final boolean[] endResul = {false};
        urlDownload = null;
        urlDownload = new ArrayList<>();

        if(diapoImagePath.get(compteurDiapoDoc).get(position).imagePath.isEmpty())
        {
            if(position < diapoImagePath.get(compteurDiapoDoc).size() - 1)
            {
                compteurDiapoImage++;
                try {
                    sendDataPerImage(compteurDiapoImage);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                FireStoreDiapo fireStoreDiapo = new FireStoreDiapo();
                fireStoreDiapo.diapoTitle = documentDiapoList.get(compteurDiapoDoc).diapoTitle;
                fireStoreDiapo.diapoContent = documentDiapoList.get(compteurDiapoDoc).diapoDesc;
                fireStoreDiapo.urlDownload = urlDownload;
                dbFireStore.collection("Document").document(userId)
                        .collection("DiapoImage").document("diapos")
                        .set(fireStoreDiapo)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                if(compteurDiapoDoc < documentDiapoList.size() - 1)
                                {
                                    compteurDiapoDoc++;
                                    try {
                                        sendDataPerDiapo(compteurDiapoDoc);
                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else
                                {
                                    pd.dismiss();
                                    Toast.makeText(DetailActivity.this, "Document Envoyé avec succès.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                                Toast.makeText(DetailActivity.this, "Impossible d'établir une connexion avec le serveur. Réessayer plus tard.", Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        }
        else
        {
            Uri uri = Uri.parse(diapoImagePath.get(compteurDiapoDoc).get(position).imagePath);
            InputStream inputStream = null;
            inputStream = getContentResolver().openInputStream(uri);
            StorageReference storageReference = storage.getReference();
            final StorageReference diapoRef         = storageReference.child("image_diapo/diapo"+String.valueOf(compteurDiapoDoc + 1)+"image"+String.valueOf(compteurDiapoImage + 1)+".jpg");
            final UploadTask uploadTask = diapoRef.putStream(inputStream);

            final Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if(!task.isSuccessful())
                    {
                        throw  task.getException();
                    }
                    Log.d("URL DOWNLOAD", "Url download: " + diapoRef.getDownloadUrl() + " Compteur diapo: " + compteurDiapoDoc + " compteur Image: " + compteurDiapoImage);
                    urlDownload.add(diapoRef.getDownloadUrl());
                    Log.d("URL DOWNLOAD", "Url download: "+ urlDownload.get(compteurDiapoImage));
                    return diapoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    endResul[0] = true;
                    if(position < diapoImagePath.get(compteurDiapoDoc).size() - 1)
                    {
                        Log.d("URL DOWNLOAD", "Upload Image continue ");
                        compteurDiapoImage++;
                        try {
                            sendDataPerImage(compteurDiapoImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        Log.d("URL DOWNLOAD", "Upload Image End!!!");
                        FireStoreDiapo fireStoreDiapo = new FireStoreDiapo();

                        fireStoreDiapo.diapoTitle = documentDiapoList.get(compteurDiapoDoc).diapoTitle;
                        fireStoreDiapo.diapoContent = documentDiapoList.get(compteurDiapoDoc).diapoDesc;
                        fireStoreDiapo.urlDownload = urlDownload;

                        dbFireStore.collection("Document").document(userId)
                                .collection("DiapoImage").document(userId)
                                .set(fireStoreDiapo)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(compteurDiapoDoc < documentDiapoList.size() - 1)
                                        {
                                            compteurDiapoDoc++;
                                            try {
                                                sendDataPerDiapo(compteurDiapoDoc);
                                            } catch (FileNotFoundException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        else
                                        {
                                            pd.dismiss();
                                            Toast.makeText(DetailActivity.this, "Document Envoyé avec succès.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Toast.makeText(DetailActivity.this, "Impossible d'établir une connexion avec le serveur. Réessayer plus tard.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    pd.dismiss();
                    Toast.makeText(DetailActivity.this, "Impossible d'établir une connexion avec le serveur. Réessayer plus tard.", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

}
