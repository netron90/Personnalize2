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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;
import com.netron90.correction.personnalize.Database.UserMessageDb;
import com.netron90.correction.personnalize.Services.NewMessageListener;

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
    public static SharedPreferences sharedPreferences;
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

        toolbar      = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerViewChat);
        textMessage  = findViewById(R.id.chat_text_msg);
        sendMessage  = findViewById(R.id.chat_send_btn);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.toolbar_title_chat));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        userId = sharedPreferences.getString(MainActivity.USER_ID, "");
        teamId = getIntent().getStringExtra("teamId");

        dbFirestore = FirebaseFirestore.getInstance();

        listUserMessage = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(listUserMessage);
        recyclerView.setAdapter(chatMessageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String textMessages = textMessage.getText().toString();
                textMessage.setText("");
                if(textMessages.equals(""))
                {
                    Toast.makeText(ChatActivity.this, "Veuillez saisir un message. ", Toast.LENGTH_SHORT).show();
                }else{
                    sendMessageMethod(textMessages, userId, teamId);
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
       // getMessageListener();
        createListener();
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
        isConnected = networkInfo != null && networkInfo.isConnected();
        return isConnected;
    }

    private void sendMessageMethod(String message, String userId, String teamId)
    {
        String currentDateFormat = new SimpleDateFormat("HH:mm").format(new Date());
        String messageAuthor = sharedPreferences.getString(MainActivity.USER_NAME, "Inconnue");
        UserMessage userMessage = new UserMessage(message, userId, teamId, currentDateFormat, messageAuthor);
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


    private void createListener()
    {


        registration = dbFirestore.collection("Message").orderBy("messageTime", Query.Direction.ASCENDING)
                .addSnapshotListener(MetadataChanges.INCLUDE, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable final QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {

                        //TODO: GET DATA FROM FIREBASE SERVER
                        if(isOnline())
                        {
                            getDataFromFirebaseServer(snapshots);
                        }
                        else
                        {
                            getDataFromFirebaseCache(snapshots);
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
            String userId = "";
            String teamId = "";
            String author = "";
            String time = "";

            if(dc.get("userTextMessage") != null)
            {
                textMessage = dc.getString("userTextMessage");
            }
            if(dc.get("userId") != null)
            {
                userId = dc.getString("userId");
            }
            if(dc.get("teamId") != null)
            {
                teamId = dc.getString("teamId");
            }
            if(dc.get("author") != null)
            {
                author = dc.getString("author");
            }
            if(dc.get("messageTime") != null)
            {
                time = dc.getString("messageTime");
            }

            userMessage.userTextMessage = textMessage;
            userMessage.userId = userId;
            userMessage.teamId = teamId;
            userMessage.messageTime = time;
            userMessage.author = author;
        }
        else
            userMessage = null;

        return userMessage;
    }



    /*private class SaveMessageInLocalDatabase extends AsyncTask<Void,Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

            //db.userDao().insertMessageUser(userMessageDb);

            if(sharedPreferences.getBoolean("firstMessage", false) == false)
            {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("firstMessage", true).apply();
                for(int i = 0; i < listUserMessage.size(); i++)
                {
                    db.userDao().insertMessageUser(listUserMessage.get(i));
                }

            }
            else
            {
                //db.userDao().insertMessageUser(userMessageDb);
                List<UserMessageDb> listLocal = db.userDao().selectAllMessage();
                if(listLocal.size() != ChatMessageAdapter.listUserMessage.size())
                {
                    for(int i = 0; i < ChatMessageAdapter.listUserMessage.size(); i++)
                    {
                        if(i < ChatMessageAdapter.listUserMessage.size() - 1)
                        {}
                        else {
                            db.userDao().insertMessageUser(ChatMessageAdapter.listUserMessage.get(i));
                        }
                    }
                }
            }

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
    }*/

    /*private void loadDataFromFirebase(QuerySnapshot snapshots)
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

    }*/

   /* public class LoadDataFromLocalDb extends AsyncTask<Void, Void, List<UserMessageDb>>
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
    }*/

    private void getDataFromFirebaseServer(QuerySnapshot snapshots)
    {
        final QuerySnapshot snapshot = snapshots;
        dbFirestore.enableNetwork().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {


                UserMessageDb userMsg = new UserMessageDb();
                if(snapshot != null && !snapshot.isEmpty())
                {
                    if(ChatMessageAdapter.listUserMessage.size() != snapshot.size())
                    {
                        for(DocumentChange dc : snapshot.getDocumentChanges())
                        {
                            if(dc.getType() == DocumentChange.Type.ADDED)
                            {
                                userMsg = getNewChatMessage(dc.getDocument());
                                if(userMsg != null)
                                {
                                    ChatMessageAdapter.listUserMessage.add(userMsg);
                                    int currentSize = sharedPreferences.getInt(NewMessageListener.CHECK_NEW_MESSAGE, 0);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putInt(NewMessageListener.CHECK_NEW_MESSAGE, currentSize + 1).apply();
                                }
                                else{
                                    ChatMessageAdapter.listUserMessage.add(userMsg);
                                    ChatMessageAdapter.listUserMessage.remove(ChatMessageAdapter.listUserMessage.size() -1);
                                }

                            }
                        }
                        chatMessageAdapter.notifyDataSetChanged();
                    }

                }
            }
        });
    }

    private void getDataFromFirebaseCache(QuerySnapshot snapshots)
    {
        final QuerySnapshot snapshot = snapshots;

        dbFirestore.disableNetwork().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                UserMessageDb userMsg = new UserMessageDb();
                if(snapshot != null && !snapshot.isEmpty())
                {
                    if(ChatMessageAdapter.listUserMessage.size() != snapshot.size())
                    {
                        for(DocumentChange dc : snapshot.getDocumentChanges())
                        {
                            if(dc.getType() == DocumentChange.Type.ADDED)
                            {
                                userMsg = getNewChatMessage(dc.getDocument());
                                if(userMsg != null)
                                {
                                    ChatMessageAdapter.listUserMessage.add(userMsg);
                                }
                                else{
                                    ChatMessageAdapter.listUserMessage.add(userMsg);
                                    ChatMessageAdapter.listUserMessage.remove(ChatMessageAdapter.listUserMessage.size() -1);
                                }

                            }
                        }
                        chatMessageAdapter.notifyDataSetChanged();
                    }

                }
            }
        });
    }

}
