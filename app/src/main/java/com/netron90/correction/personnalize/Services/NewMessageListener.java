package com.netron90.correction.personnalize.Services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.netron90.correction.personnalize.ChatActivity;
import com.netron90.correction.personnalize.MainActivity;
import com.netron90.correction.personnalize.MainProcess;
import com.netron90.correction.personnalize.R;

import java.util.UUID;

/**
 * Created by CHRISTIAN on 30/03/2019.
 */

public class NewMessageListener extends IntentService {

    private ListenerRegistration registration;
    private FirebaseFirestore dbFirestore;
    private String userId, teamId;
    private SharedPreferences sharedPreferences;
    private final String CHANNE_ID = "channel_id";
    private final int TIME_ELLAPSE = 300000;

    public NewMessageListener(String name) {
        super("service");
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        userId = sharedPreferences.getString(MainActivity.USER_ID, UUID.randomUUID().toString());
        teamId = sharedPreferences.getString("teamId","");
        dbFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        getMessageListener();
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

    private void getMessageListener()
    {
        registration = dbFirestore.collection("Message")
                .whereEqualTo("userId", userId).whereEqualTo("teamId", teamId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@javax.annotation.Nullable QuerySnapshot snapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                        if(e != null)
                        {
                            return;
                        }

                        if(isOnline())
                        {
                            //TODO: GET ALL MESSAGE
                            if(!snapshots.isEmpty())
                            {
                                getOneNewMessage(snapshots);
                            }
                        }
                        else
                        {
                            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                            Intent intent = new Intent(getApplicationContext(), DocPaidListener.class);
                            PendingIntent pendingIntent = PendingIntent
                                    .getService(getApplicationContext(),
                                            0, intent, 0);

                            alarmManager.set(AlarmManager.ELAPSED_REALTIME, TIME_ELLAPSE, pendingIntent);
                        }
                    }
                });
    }

    private void getOneNewMessage(QuerySnapshot snapshots)
    {
        if(snapshots.getMetadata().hasPendingWrites() == false)
        {
            for(DocumentChange dc : snapshots.getDocumentChanges())
            {
                switch (dc.getType())
                {
                    case ADDED:
                        createNotification();
                        break;
                }
            }
        }
        if(!isOnline())
        {
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), DocPaidListener.class);
            PendingIntent pendingIntent = PendingIntent
                    .getService(getApplicationContext(),
                            0, intent, 0);

            alarmManager.set(AlarmManager.ELAPSED_REALTIME, TIME_ELLAPSE, pendingIntent);
        }
        else
        {
            Intent intent = new Intent(getApplicationContext(), DocPaidListener.class);
            startService(intent);

        }
    }

    private void createNotification()
    {
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
