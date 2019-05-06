package com.netron90.correction.personnalize.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
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

public class DocEndListener extends Service {

    private String userId;
    private FirebaseFirestore dbFireStore;
    public static ListenerRegistration docEndRegistration;
    private SharedPreferences sharedPreferences;

    public static final String CHANNE_ID = "channel_id";
    private boolean docEndFlag;
    private long id;
    private String docName, teamId;



    @Override
    public void onCreate() {
        super.onCreate();
        dbFireStore = FirebaseFirestore.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userId = sharedPreferences.getString(MainActivity.USER_ID, UUID.randomUUID().toString());
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        docEndListener();

        return START_STICKY;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(),0, rootIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                pendingIntent);
    }

    private void docEndListener()
    {

        docEndRegistration = dbFireStore.collection("Document")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {
                        if(e != null)
                        {
                            return;
                        }

                        final QuerySnapshot snapshot = snapshots;

                        if(snapshot.getMetadata().hasPendingWrites() == false)
                        {
                            for(DocumentChange dc : snapshot.getDocumentChanges())
                            {
                                DocumentAvailable documentAvailable = new DocumentAvailable();
                                switch (dc.getType())
                                {
                                    case MODIFIED:
                                        updateUserDocEndField(dc);
                                        break;
                                }

                            }


                        }

                    }
                });


       /* if(flagDocEnd)
        {
            docEndRegistration = dbFireStore.collection("Document")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                            if(e != null)
                            {
                                return;
                            }
                            final QuerySnapshot snapshot = snapshots;
                            Log.d("DOC END EVENT", "One Doc event come");
                            if (isOnline())
                            {
                                boolean restartService = false;
                                dbFireStore.enableNetwork().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        //TODO: CHECK IF SOURCE OF EVENT COME FROM SERVER
                                        for(DocumentChange dc : snapshot.getDocumentChanges()){
                                            if(dc.getType() == DocumentChange.Type.MODIFIED)
                                            {

                                            }
                                        }


                                        if (snapshot.getMetadata().hasPendingWrites() == false)
                                        {
                                            Log.d("DOC END EVENT", "Doc event comefrom server");
                                            //TODO: CHECK IF "docEnd" IS SET TO TRUE
                                            for(DocumentChange dc : snapshot.getDocumentChanges())
                                            {
                                                DocumentAvailable documentAvailable = new DocumentAvailable();
                                                switch (dc.getType())
                                                {
                                                    case MODIFIED:
                                                        docEndFlag = dc.getDocument().getBoolean("docEnd");
                                                        id            = (long)dc.getDocument().get("id");
                                                        docName     = dc.getDocument().getString("documentName");
                                                        teamId      = dc.getDocument().getString("teamId");

                                                        if(docEndFlag)
                                                        {
                                                            UpdateField updateField = new UpdateField();
                                                            updateField.execute();

                                                        }

                                                        break;

                                                }
                                            }

                                        }
                                    }
                                });


                            }

                        }
                    });
        }
        else{
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("NEW_DOC_END", true).apply();
        }*/

    }

    private void createNotification(String documentName)
    {
        String textContent = getString(R.string.notification_correction_termine_text, documentName);
        Intent intent = new Intent(getApplicationContext(), MainProcess.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNE_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(getResources().getString(R.string.notification_correction_termine_title))
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

            db.userDao().updateDocEndField(docEndFlag, (int)id);
            db.userDao().updateTeamIdField(teamId, (int)id);

            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            createNotification(docName);
        }
    }

    private void updateUserDocEndField(DocumentChange dc)
    {
        docEndFlag    = dc.getDocument().getBoolean("docEnd");
        id            = (long)dc.getDocument().get("id");
        docName       = dc.getDocument().getString("documentName");
        teamId        = dc.getDocument().getString("teamId");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("teamIdGetFromServer", teamId).apply();

        if(docEndFlag)
        {
            UpdateField updateField = new UpdateField();
            updateField.execute();

        }
    }
}
