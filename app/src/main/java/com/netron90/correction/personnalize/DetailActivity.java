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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.netron90.correction.personnalize.Database.DocumentUser;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;

import org.json.JSONObject;

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

    private TextView montantTV, documentNameTV, documentPage, payementModeSwitch;

    public static TextView dateSelect;
    public static String documentNameDetail, montantFacture;
    private String fileName = null;

    public static int documentPageDetail = 0, montantTotal =0;

    public static final String PAYEMENT = "payementMode";
    private final String MONTANT_TOTAL_COMPLEMENT = "montantComplement";
    private String userName, userEmail, userPhone, userDocPath;
    private DeleteDocumentSend deleteDocumentSend;

    private SharedPreferences sharedPreferences;

    private FloatingActionButton fab;
    private AppCompatCheckBox miseEnPage, powerPoint;
    private Switch modePayement;
    private Toolbar toolbar;
    private EditText phoneNumber;
    private final int REQUEST_USER_INFO_TEAM = 1;
    List<DocumentUser> documentUserList = new ArrayList<>();
    private int montantTotalComplement = 0;
    private final int MONTANT_MISE_EN_PAGE = 2500, MONTANT_POWER_POINT = 10000, MONTANT_NBR_PAGE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        montantTV = (TextView) findViewById(R.id.montant);
        documentNameTV = (TextView) findViewById(R.id.doc_title);
        documentPage  = (TextView) findViewById(R.id.doc_page);
        dateSelect = (TextView) findViewById(R.id.date_select);

        payementModeSwitch = (TextView) findViewById(R.id.payement_text);
        toolbar            = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        modePayement = (Switch) findViewById(R.id.mode_payement);

        miseEnPage  = (AppCompatCheckBox) findViewById(R.id.checkBox_msPage);
        powerPoint  = (AppCompatCheckBox) findViewById(R.id.checkBox_power_point);

        userDocPath = getIntent().getStringExtra("document_path");

        userName = sharedPreferences.getString(MainActivity.USER_NAME, "NoBody");
        userEmail = sharedPreferences.getString(MainActivity.USER_EMAIL, "No Email");
        userPhone = sharedPreferences.getString(MainActivity.USER_PHONE, "No PhoneNumber");

        documentNameDetail = getIntent().getStringExtra("document_name");
        documentNameTV.setText(documentNameDetail);

        documentPageDetail = getIntent().getIntExtra("document_page", 0);
        documentPage.setText(documentPageDetail + " Pages");

        Log.d("PageDoc", "Document Page: "+documentPageDetail);
        montantTV.setText(String.valueOf(documentPageDetail * MONTANT_NBR_PAGE) + " f CFA");

        miseEnPage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked())
                {
//                    SharedPreferences.Editor editor = MainProcess.sharedPreferences.edit();
                    int montantMiseEnPageSave = montantTotalComplement;
                    if(montantMiseEnPageSave == 0)
                    {
                        int montant = (Integer.valueOf(documentPageDetail) * MONTANT_NBR_PAGE) + MONTANT_MISE_EN_PAGE;
                        montantTV.setText(String.valueOf(montant) + " f CFA");
                        montantTotalComplement = montant;
                    }
                    else
                    {
                        int getMontantMiseEnPage = montantTotalComplement;
                        int newMontant = getMontantMiseEnPage + MONTANT_MISE_EN_PAGE;
                        montantTotalComplement = newMontant;
                        montantTV.setText(String.valueOf(newMontant) + " f CFA");
                    }

                }
                else {

                        int getMontantMiseEnPage = montantTotalComplement;
                        int newMontant = getMontantMiseEnPage - MONTANT_MISE_EN_PAGE;
                        montantTotalComplement = newMontant;
                        montantTV.setText(String.valueOf(newMontant) + " f CFA");

                }
            }
        });

        powerPoint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked())
                {
                    int montantMiseEnPageSave = montantTotalComplement;
                    if(montantMiseEnPageSave == 0)
                    {
                        int montant = (Integer.valueOf(documentPageDetail) * MONTANT_NBR_PAGE) + MONTANT_POWER_POINT;
                        montantTV.setText(String.valueOf(montant) + " f CFA");
                        montantTotalComplement = montant;
                    }
                    else
                    {
                        int getMontantMiseEnPage = montantTotalComplement;
                        int newMontant = getMontantMiseEnPage + MONTANT_POWER_POINT;
                        montantTotalComplement = newMontant;
                        montantTV.setText(String.valueOf(newMontant) + " f CFA");
                    }
                }
                else {
                    int getMontantMiseEnPage = montantTotalComplement;
                    int newMontant = getMontantMiseEnPage - MONTANT_POWER_POINT;
                    montantTotalComplement = newMontant;
                    montantTV.setText(String.valueOf(newMontant) + " f CFA");
                }
            }
        });

        modePayement.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked())
                {
                    payementModeSwitch.setText("Payement par Paypal");
                    SharedPreferences.Editor editor = MainProcess.sharedPreferences.edit();
                    editor.putBoolean(PAYEMENT, true).commit();
                }
                else{
                    payementModeSwitch.setText("Payement par MTN/MOOV");
                    SharedPreferences.Editor editor = MainProcess.sharedPreferences.edit();
                    editor.putBoolean(PAYEMENT, false).commit();
                }
            }
        });

        dateSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment dateChooser = new SelectDate();
                dateChooser.show(getFragmentManager(), "DatePicker");
            }
        });
        fab = (FloatingActionButton) findViewById(R.id.send_document);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                GetSendInformation getSendInformation = new GetSendInformation();
//                getSendInformation.execute();

                //if MTN OR MOOV is selected
                boolean modePaie = sharedPreferences.getBoolean(PAYEMENT, false);
                if(!modePaie)
                {
                    Log.d("detail activity", "FAB Clicked");
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
                        alertDialog.setPositiveButton("Envoyer le document", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                userPhone = phoneNumber.getText().toString();
                                {
                                    if (userPhone.equals(""))
                                    {
                                        Toast.makeText(DetailActivity.this, "No phone number registering.", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        Log.d("detail activity", "phone number enter is: "+userPhone);
                                        //TODO:Save user phone number
                                        SharedPreferences.Editor editor = sharedPreferences.edit();
                                        editor.putString(MainActivity.USER_PHONE, userPhone).commit();
                                        if(checkConnectionState())
                                        {
                                            //TODO: recreate doc Uri

                                            Uri docUri = Uri.parse(userDocPath);


                                            InputStream inputStream = null;

                                            //create file with Uri
                                            try {

                                                //android.provider.MediaStore.Files.FileColumns.DATA
                                                inputStream = getContentResolver().openInputStream(docUri);
                                                AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
                                                RequestParams mRequestParams = new RequestParams();

                                                mRequestParams.put("user_name", userName);
                                                mRequestParams.put("user_email", userEmail);
                                                mRequestParams.put("user_phone", userPhone);
                                                mRequestParams.put("mise_page", miseEnPage.isChecked());
                                                mRequestParams.put("power_point", powerPoint.isChecked());
                                                mRequestParams.put("date_select", dateSelect.getText());
                                                mRequestParams.put("nombre_page", documentPageDetail);
                                                mRequestParams.put("documents", inputStream, fileName);

                                                mAsyncHttpClient.post("http://mighty-refuge-23480.herokuapp.com/memoire_api", mRequestParams, new JsonHttpResponseHandler() {
                                                    ProgressDialog pd;
                                                    @Override
                                                    public void onStart() {
                                                        String uploadingMessage = "Wait a moment";
                                                        pd = new ProgressDialog(DetailActivity.this);
                                                        pd.setTitle("Document uploading");
                                                        pd.setMessage(uploadingMessage);
                                                        pd.setIndeterminate(false);
                                                        pd.show();
                                                    }

                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                        super.onSuccess(statusCode, headers, response);
                                                        Log.d("Serveur response", "Status Code: " + statusCode + " Response body: " + response);
                                                        AsyncHttpClient sendMailClient = new AsyncHttpClient();
                                                        RequestParams sendMailParamsClient = new RequestParams();
                                                        sendMailParamsClient.put("fileNameParams", fileName);
                                                        sendMailClient.get("http://mighty-refuge-23480.herokuapp.com/send_mail_api", sendMailParamsClient, new JsonHttpResponseHandler(){
                                                            @Override
                                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                                super.onSuccess(statusCode, headers, response);
                                                                Log.d("Serveur responseMail", "Status Code: " + statusCode + "Mail Send Success and response: " + response);
                                                                pd.dismiss();
                                                                //TODO: save information into database
                                                                DeleteDocumentSend deleteDocumentSend = new DeleteDocumentSend();
                                                                deleteDocumentSend.execute();
//                                                                final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
//                                                                        PersonnalizeDatabase.class, "scodelux").build();
//                                                                db.userDao().deleteOneDocument(documentNameTV.getText().toString());
//                                                                Intent intent = new Intent(DetailActivity.this, MainProcess.class);
//                                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                                                                startActivity(intent);
                                                            }

                                                            @Override
                                                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                                                super.onFailure(statusCode, headers, responseString, throwable);
                                                                Log.d("Serveur responseMail", "Status Code: " + statusCode + "Mail Send Error");
                                                                pd.dismiss();
                                                            }
                                                        });

                                                    }

                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                                        super.onFailure(statusCode, headers, responseString, throwable);
                                                        Log.d("Serveur response", "Status Code: " + statusCode + " Response body: " + responseString + " throwable: " + throwable);
                                                        pd.dismiss();
                                                    }
                                                });
                                            }catch(FileNotFoundException e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                        else
                                        {
                                            Toast.makeText(DetailActivity.this, R.string.connection_network_failed, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                            }
                        });
                        alertDialog.setCancelable(true);
                        alertDialog.create().show();

                    }
                    else
                    {
//                        try
//                        {
//                            Log.d("detail activity", "phone number exist");
//                            //Log.d("detail activity", "phone number: "+userPhone);
//                            Log.d("detail activity", "Send mail");
//                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
//                            emailIntent.setType("message/rfc822");
//                            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"fredyannra@gmail.com"});
//                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Fichier mémoire reçu");
//                            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(new StringBuilder()
//                                    .append("<h1> Information du client sur son mémoire</h1>")
//                                    .append("<p><br>Nom: <strong>"+userName+"</strong>")
//                                    .append("<br>Email: <strong>"+userEmail+"</strong>")
//                                    .append("<br>Contact: <strong>"+userPhone+"</strong>")
//                                    .append("<br>Date de remise du rapport: <strong>"+dateSelect.getText().toString()+"</strong>")
//                                    .append("<br>Nombre de page du document: <strong>"+documentPage.getText() + " pages"+"</strong>")
//                                    .append("<br>Montant facturé: <strong>"+String.valueOf(documentPageDetail * 200)+"</strong></p>")
//                                    .append("<br>Mise en forme du document: <strong>"+miseEnPage.isChecked()+"</strong></p>")
//                                    .append("<br>Power Point du document: <strong>"+powerPoint.isChecked()+"</strong></p")
//                                    .toString()));
//                            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(userDocPath));
//                            startActivityForResult(Intent.createChooser(emailIntent, "Send mail with?"), REQUEST_USER_INFO_TEAM);
//                        }
//                        catch (Throwable t)
//                        {
//                            Toast.makeText(DetailActivity.this, "failed", Toast.LENGTH_SHORT).show();
//                            Toast.makeText(DetailActivity.this, "error: "+t.toString(), Toast.LENGTH_SHORT).show();
//                        }

                        if(checkConnectionState())
                        {
                            //TODO: recreate doc Uri

                            Uri docUri = Uri.parse(userDocPath);


                            InputStream inputStream = null;

                            //create file with Uri
                            try {

                                //android.provider.MediaStore.Files.FileColumns.DATA
                                inputStream = getContentResolver().openInputStream(docUri);
                                AsyncHttpClient mAsyncHttpClient = new AsyncHttpClient();
                                RequestParams mRequestParams = new RequestParams();

                                mRequestParams.put("user_name", userName);
                                mRequestParams.put("user_email", userEmail);
                                mRequestParams.put("user_phone", userPhone);
                                mRequestParams.put("mise_page", miseEnPage.isChecked());
                                mRequestParams.put("power_point", powerPoint.isChecked());
                                mRequestParams.put("date_select", dateSelect.getText());
                                mRequestParams.put("nombre_page", documentPageDetail);
                                mRequestParams.put("documents", inputStream, fileName);

                                mAsyncHttpClient.post("http://mighty-refuge-23480.herokuapp.com/memoire_api", mRequestParams, new JsonHttpResponseHandler() {
                                    ProgressDialog pd;
                                    @Override
                                    public void onStart() {
                                        String uploadingMessage = "Wait a moment";
                                        pd = new ProgressDialog(DetailActivity.this);
                                        pd.setTitle("Document uploading");
                                        pd.setMessage(uploadingMessage);
                                        pd.setIndeterminate(false);
                                        pd.show();
                                    }

                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                        super.onSuccess(statusCode, headers, response);
                                        Log.d("Serveur response", "Status Code: " + statusCode + " Response body: " + response);
                                        AsyncHttpClient sendMailClient = new AsyncHttpClient();
                                        RequestParams sendMailParamsClient = new RequestParams();
                                        sendMailParamsClient.put("fileNameParams", fileName);
                                        sendMailClient.get("http://mighty-refuge-23480.herokuapp.com/send_mail_api", sendMailParamsClient, new JsonHttpResponseHandler(){
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                                super.onSuccess(statusCode, headers, response);
                                                Log.d("Serveur responseMail", "Status Code: " + statusCode + "Mail Send Success and response: " + response);
                                                pd.dismiss();
                                                //TODO: save information into database
                                                DeleteDocumentSend deleteDocumentSend = new DeleteDocumentSend();
                                                deleteDocumentSend.execute();
                                            }

                                            @Override
                                            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                                super.onFailure(statusCode, headers, responseString, throwable);
                                                Log.d("Serveur responseMail", "Status Code: " + statusCode + "Mail Send Error");
                                                pd.dismiss();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                                        super.onFailure(statusCode, headers, responseString, throwable);
                                        Log.d("Serveur response", "Status Code: " + statusCode + " Response body: " + responseString + " throwable: " + throwable);
                                        pd.dismiss();
                                    }
                                });
                            }catch(FileNotFoundException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else
                        {
                            Toast.makeText(DetailActivity.this, R.string.connection_network_failed, Toast.LENGTH_SHORT).show();
                        }
                    }
                }

            }
        });

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

    public class DeleteDocumentSend extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {
            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "scodelux").build();
            db.userDao().deleteOneDocument(documentNameTV.getText().toString());

            documentUserList = db.userDao().selectAllDocument();
            //)remove element from arrayList of User documents
            //documentUserList.remove(position);
            if(documentUserList.size() == 0)
            {

                SharedPreferences.Editor editor = MainProcess.sharedPreferences.edit();
                editor.putBoolean(MainProcess.DOCUMENT_EXIST, false).commit();
                //return true;
            }
            else
            {
                //documentUserList.remove(position);
                //return false;
            }
           return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            if(aVoid == true)
//            {
//                Log.d("DetaiActivity Size Tab", "Element restants: " +  documentUserList.size());
//                Intent mainProcessActivityIntent = new Intent (DetailActivity.this, MainProcess.class);
//                mainProcessActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                finish();
//                startActivity(mainProcessActivityIntent);
//            }
//            else {
//                Log.d("DetaiActivity Size Tab", "Element restants: " +  documentUserList.size());
//                Intent mainProcessActivityIntent = new Intent (DetailActivity.this, MainProcess.class);
//                mainProcessActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                finish();
//                startActivity(mainProcessActivityIntent);
//            }
            Intent intent = new Intent(DetailActivity.this, MainProcess.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            if(requestCode == REQUEST_USER_INFO_TEAM)
            {
                if(miseEnPage.isChecked() && powerPoint.isChecked())
                {
                    //TODO: Send reglementation to user with API CALL
                    //TODO: Delete document send from data base
                    deleteDocumentSend = new DeleteDocumentSend();
                    deleteDocumentSend.execute();
                }
                else if(!miseEnPage.isChecked() && powerPoint.isChecked())
                {
                    //TODO: Send reglementation to user with API CALL
                    //TODO: Delete document send from data base
                    deleteDocumentSend = new DeleteDocumentSend();
                    deleteDocumentSend.execute();
                }
                else if(miseEnPage.isChecked() && !powerPoint.isChecked())
                {
                    //TODO: Send reglementation to user with API CALL
                    //TODO: Delete document send from data base
                    deleteDocumentSend = new DeleteDocumentSend();
                    deleteDocumentSend.execute();
                }
                else if(!miseEnPage.isChecked() && !powerPoint.isChecked())
                {
                    //TODO: Send reglementation to user with API CALL
                    //TODO: Delete document send from data base
                    deleteDocumentSend = new DeleteDocumentSend();
                    deleteDocumentSend.execute();
                }
                else {}

            }
        }
    }

    public class GetDocumentUri extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            Uri docUri = Uri.parse(userDocPath);
            try {
                URI document = new URI("file://", null, docUri.getPath(), docUri.getQuery(), docUri.getFragment());
                File file = new File(document);
                Log.e("GET PATH", "getRealPathFromURI: " + file.getAbsolutePath());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
//            Uri docUri = Uri.parse(userDocPath);
//            String[] filePathColumn = { MediaStore.Images.Media.DATA };
//            Log.d("File URI","uri toString: "+ docUri.toString()+" uri Path: "+docUri.getPath());
//
//            Cursor cursor = getContentResolver().query(docUri, filePathColumn, null, null, null);
//            cursor.moveToFirst();
//            int columIndex = cursor.getColumnIndex(filePathColumn[0]);
//            String filePath  = cursor.getString(columIndex);
//            cursor.close();
//            Log.d("File Path","Chosen path = "+ filePath);


            return null;
        }
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Files.FileColumns.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            Log.e("GET PATH ERROR", "getRealPathFromURI Exception : " + e.toString());
            return "";
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Boolean checkConnectionState()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
            return true;
        else
            return false;
    }
}
