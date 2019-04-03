package com.netron90.correction.personnalize.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.netron90.correction.personnalize.Database.DocumentAvailable;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;
import com.netron90.correction.personnalize.MainActivity;
import com.netron90.correction.personnalize.MainProcess;
import com.netron90.correction.personnalize.R;

import java.util.UUID;

/**
 * Created by CHRISTIAN on 29/03/2019.
 */

public class DocPaidListener extends IntentService {

    private String userId;
    private FirebaseFirestore dbFireStore;
    public static ListenerRegistration docPaidRegistration;
    private SharedPreferences sharedPreferences;
    private final int TIME_ELLAPSE = 5 * 60 * 1000;
    public static final String CHANNE_ID = "channel_id";
    private boolean docPaidFlag;
    private long id;
    private String docName;

    public DocPaidListener() {
        super("name");



    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        dbFireStore = FirebaseFirestore.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userId = sharedPreferences.getString(MainActivity.USER_ID, UUID.randomUUID().toString());
        docEndListener();
    }

    private boolean isOnline()
    {
        boolean isConnected = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null && networkInfo.isConnected())
        {
            isConnected = true;
        }
        else
        {
            isConnected = false;
        }
        return isConnected;
    }

    private void docEndListener()
    {

        docPaidRegistration = dbFireStore.collection("Document")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(e != null)
                        {
                            return;
                        }

                        if (isOnline())
                        {
                            boolean restartService = false;

                            //TODO: CHECK IF SOURCE OF EVENT COME FROM SERVER
                            if (snapshot.getMetadata().hasPendingWrites() == false)
                            {
                                //TODO: CHECK IF "docEnd" IS SET TO TRUE
                                for(DocumentChange dc : snapshot.getDocumentChanges())
                                {
                                    DocumentAvailable documentAvailable = new DocumentAvailable();
                                    switch (dc.getType())
                                    {
                                        case MODIFIED:
                                            docPaidFlag = dc.getDocument().getBoolean("documentPaid");
                                            id            = (long)dc.getDocument().get("id");
                                            docName     = dc.getDocument().getString("documentName");
                                            if(docPaidFlag)
                                            {
                                                UpdateField updateField = new UpdateField();
                                                updateField.execute();
                                            }

                                            break;

                                    }
                                }

                            }

                        }

                    }
                });
    }

    private void createNotification(String documentName)
    {
        String textContent = getString(R.string.notification_document_payer_text, documentName);
        Intent intent = new Intent(getApplicationContext(), MainProcess.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNE_ID)
                .setSmallIcon(R.drawable.ic_stat_personnalize)
                .setContentTitle(getResources().getString(R.string.notification_document_payer_title))
                .setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, builder.build());

    }

    public class UpdateField extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

            db.userDao().updateDocPaidField(docPaidFlag, (int)id);

            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            createNotification(docName);
        }
    }


}
