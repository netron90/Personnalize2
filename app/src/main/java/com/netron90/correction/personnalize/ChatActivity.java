package com.netron90.correction.personnalize;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;
import com.netron90.correction.personnalize.Database.UserMessageDb;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity {


    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private EditText textMessage;
    private ImageView sendMessage;
    private ListenerRegistration registration;
    private FirebaseFirestore dbFirestore;
    private SharedPreferences sharedPreferences;
    public static String userId;
    public static String teamId;
    private ChatMessageAdapter chatMessageAdapter = null;
    private final String MESSAGE_DATABASE = "messageDataBase";
    private UserMessage userNewMessage;
    private UserMessageDb userMessageDb;
    private boolean eventFirstLaunch = false;
    private List<UserMessageDb> listUserMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        toolbar      = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewChat);
        textMessage  = (EditText) findViewById(R.id.chat_text_msg);
        sendMessage  = (ImageView) findViewById(R.id.chat_send_btn);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.toolbar_title_chat));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        userId = sharedPreferences.getString(MainActivity.USER_ID, "");
        teamId = getIntent().getStringExtra("teamId");

        dbFirestore = FirebaseFirestore.getInstance();

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textMessages = textMessage.getText().toString();
                textMessage.setText("");
                sendMessageMethod(textMessages, userId);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getMessageListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        registration.remove();
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

    private void sendMessageMethod(String message, String userId)
    {
        String currentDateFormat = new SimpleDateFormat("HH:mm").format(new Date());
        UserMessage userMessage = new UserMessage(message, userId, currentDateFormat);
        dbFirestore.collection("Message").add(userMessage)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        //textMessage.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, getString(R.string.chat_send_message_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getMessageListener()
    {
        registration = dbFirestore.collection("Message")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {

                        if(e != null)
                        {
                            return;
                        }

                        if(isOnline())
                        {
                            if(chatMessageAdapter == null)
                            {
                                Log.d("FIRST CALL", "First call: Adaptater null");
                                Log.d("FIRST CALL", "Snapshot size: " + snapshots);
                                if(!snapshots.isEmpty())
                                {
                                    Log.d("FIRST CALL", "First call: Load data");
                                    loadDataFromFirebase(snapshots);
                                }

                            }
                            else {
                                //TODO: CHECK IF SOURCE OF EVENT COME FROM SERVE
                                Log.d("NEW DOCUMENT", "One document just written on server");
                                getOneNewMessage(snapshots);
                            }
                        }
                        else
                        {
                            if(chatMessageAdapter == null)
                            {
                                //TODO: LOAD DATA FROM LOCAL DATABASE
                                LoadDataFromLocalDb loadDataFromLocalDb = new LoadDataFromLocalDb();
                                loadDataFromLocalDb.execute();
                            }
                        }
                    }
                });
    }

    private UserMessageDb getNewChatMessage(QueryDocumentSnapshot dc)
    {
        UserMessageDb userMessage = new UserMessageDb();
        if(dc.get("userId") != null &&
                (dc.getString("userId").equals(userId) || dc.getString("userId").equals(teamId)))
        {
            String textMessage = "";
            String author = "";
            String time = "";

            if(dc.get("userTextMessage") != null)
            {
                textMessage = dc.getString("userTextMessage");
            }
            if(dc.get("userId") != null)
            {
                author = dc.getString("userId");
            }
            if(dc.get("messageTime") != null)
            {
                time = dc.getString("messageTime");
            }

            userMessage.userTextMessage = textMessage;
            userMessage.userId = author;
            userMessage.messageTime = time;
        }

        return userMessage;
    }

    private class SaveMessageInLocalDatabase extends AsyncTask<Void,Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

            db.userDao().insertMessageUser(userMessageDb);
            db.close();

            return null;
        }
    }

    private void getOneNewMessage(QuerySnapshot snapshots)
    {

        if(snapshots.getMetadata().hasPendingWrites() == true)
        {
            for(DocumentChange dc : snapshots.getDocumentChanges())
            {
                Log.d("NEW DOCUMENT", "One document just written on server");
                switch (dc.getType())
                {
                    case ADDED:
                        userMessageDb = null;
                        userMessageDb = getNewChatMessage(dc.getDocument());
                        ChatMessageAdapter.listUserMessage.add(userMessageDb);
                        chatMessageAdapter.notifyDataSetChanged();
                        SaveMessageInLocalDatabase saveMessageInLocalDatabase = new SaveMessageInLocalDatabase();
                        saveMessageInLocalDatabase.execute();
                        break;
                }
            }
        }
    }

    private void loadDataFromFirebase(QuerySnapshot snapshots)
    {
        Log.d("ADAPTER NULL", "LoadDataFromFirebase Method fire");
        List<UserMessageDb> userMessagesList = new ArrayList<>();
        List<UserMessageDb> userMessagesListNew = new ArrayList<>();

        for(QueryDocumentSnapshot doc : snapshots)
        {
            userMessagesList.add(getNewChatMessage(doc));
            if(sharedPreferences.getBoolean("firstMessage", false) == false)
            {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("firstMessage", true).apply();
                userMessageDb = getNewChatMessage(doc);
                SaveMessageInLocalDatabase saveMessageInLocalDatabase = new SaveMessageInLocalDatabase();
                saveMessageInLocalDatabase.execute();
            }else{}
        }
        for(int i = userMessagesList.size(); i > 0; i--)
        {
            userMessagesListNew.add(userMessagesList.get(i-1));
        }

        chatMessageAdapter = new ChatMessageAdapter(userMessagesListNew);
        recyclerView.setAdapter(chatMessageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    public class LoadDataFromLocalDb extends AsyncTask<Void, Void, List<UserMessageDb>>
    {
        @Override
        protected List<UserMessageDb> doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

            List<UserMessageDb> userMessagesList = new ArrayList<>();
            List<UserMessageDb> userMsgList = db.userDao().selectAllMessage();

            for(int i = userMsgList.size(); i > 0; i--)
            {
                userMessagesList.add(userMsgList.get(i-1));
            }

            return userMsgList;
        }
        @Override
        protected void onPostExecute(List<UserMessageDb> userMessages) {
            super.onPostExecute(userMessages);

            chatMessageAdapter = new ChatMessageAdapter(userMessages);
            recyclerView.setAdapter(chatMessageAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }
    }

}
