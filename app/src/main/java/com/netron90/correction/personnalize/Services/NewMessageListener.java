package com.netron90.correction.personnalize.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.netron90.correction.personnalize.ChatActivity;
import com.netron90.correction.personnalize.MainActivity;
import com.netron90.correction.personnalize.MainProcess;
import com.netron90.correction.personnalize.R;

import java.util.UUID;

/**
 * Created by CHRISTIAN on 30/03/2019.
 */

public class NewMessageListener extends Service {

    public static ListenerRegistration registration;
    private FirebaseFirestore dbFirestore;
    private String userId, teamId;
    private final String CHANNE_ID = "channel_id";
    private final int TIME_ELLAPSE = 1 * 60 * 1000;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    public static final String CHECK_NEW_MESSAGE = "check_new_message";
    private boolean flagMessage = false;


    @Override
    public void onCreate() {
        super.onCreate();

        dbFirestore = FirebaseFirestore.getInstance();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPreferences.edit();
        userId = sharedPreferences.getString(MainActivity.USER_ID, UUID.randomUUID().toString());
        teamId = sharedPreferences.getString("teamIdGetFromServer", "");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        registration = dbFirestore.collection("Message")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        checkNewMessage(snapshots, e);
                    }
                });
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


    private void checkNewMessage(QuerySnapshot snapshots, FirebaseFirestoreException e)
    {
        Log.d("SERVICE", "Service is running");

       // boolean firstCall = flagMessage;

        if(e != null)
        {
            return;
        }

        final QuerySnapshot snapshot = snapshots;


            if(snapshot.getMetadata().hasPendingWrites() == false)
            {
                for(DocumentChange dc : snapshot.getDocumentChanges())
                {
                    if(snapshot.size() != sharedPreferences.getInt(CHECK_NEW_MESSAGE, 0))
                    {
                        if(dc.getType() == DocumentChange.Type.ADDED)
                        {
                            editor.putInt(CHECK_NEW_MESSAGE, snapshot.size()).apply();
                            String textContent = getString(R.string.notification_message_from_team);
                            Intent intent = new Intent(getApplicationContext(), MainProcess.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNE_ID)
                                    .setSmallIcon(R.drawable.ic_notification_icon)
                                    .setContentTitle(getResources().getString(R.string.notification_message_from_team_title))
                                    .setContentText(textContent)
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true);

                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                            notificationManager.notify(1, builder.build());
                        }
                    }

                }
            }





       /* if(firstCall)
        {
            if(e != null)
            {
                return;
            }

            if(isOnline())
            {
                final QuerySnapshot snapshot = snapshots;
                dbFirestore.enableNetwork().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (snapshot.getMetadata().hasPendingWrites() == false)
                        {
//                        if(snapshot != null && !snapshot.isEmpty())
//                        {
                            for(DocumentChange dc : snapshot.getDocumentChanges())
                            {
                                if(dc.getType() == DocumentChange.Type.ADDED)
                                {
                                    Log.d("SERVICE", "Service is running.New Message Receive");
                                    String textContent = getString(R.string.notification_message_from_team);
                                    Intent intent = new Intent(getApplicationContext(), MainProcess.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNE_ID)
                                            .setSmallIcon(R.drawable.ic_stat_personnalize)
                                            .setContentTitle(getResources().getString(R.string.notification_message_from_team_title))
                                            .setContentText(textContent)
                                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                            .setContentIntent(pendingIntent)
                                            .setAutoCancel(true);

                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                                    notificationManager.notify(1, builder.build());
                                }
                            }
                        }
                    }
                });
            }

        }
        else
        {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(CHECK_NEW_MESSAGE, true).apply();
        }*/
    }


}
