package com.netron90.correction.personnalize;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.netron90.correction.personnalize.Database.DocumentAvailable;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DiscussionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DiscussionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DiscussionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FirebaseFirestore dbFireStore;
    private DocumentReference documentReference;
    private SharedPreferences sharedPreferences;
    private ListenerRegistration registration, docEndRegistration, docPaidRegistration;
    private Context context;
    private RelativeLayout fileImage;
    private TextView textNothing;
    private RecyclerView recyclerView;
    private DiscussionDocAvailableAdapter docAdapter;
    private DocumentAvailable newDocumentGet;
    List<DocumentAvailable> documentAvail = new ArrayList<>();
    private boolean onCreateFlag = false;
    private String userId = "";
    private int firstCallBack = 1;
    private boolean docAvailableFla = false;

    private OnFragmentInteractionListener mListener;

    public DiscussionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DiscussionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DiscussionFragment newInstance(String param1, String param2) {
        DiscussionFragment fragment = new DiscussionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_discussion, container, false);
        context = container.getContext();
        Log.d("FRAGMENT START", "Le fragment est lance");

        fileImage   = view.findViewById(R.id.file_empty);
        textNothing = view.findViewById(R.id.text_nothing);
        recyclerView = view.findViewById(R.id.docAvailableRecyclerView);
        dbFireStore = FirebaseFirestore.getInstance();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        userId = sharedPreferences.getString(MainActivity.USER_ID, UUID.randomUUID().toString());


        docAvailableFla = MainProcess.sharedPreferences.getBoolean(MainProcess.DOCUMENT_AVAILABLE, false);
        onCreateFlag = false;
        //createListener();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        registration.remove();
    }

    @Override
    public void onStart() {
        super.onStart();
//        GetDatabase getDatabase = new GetDatabase();
//        getDatabase.execute();
//        if(onCreateFlag == true){}
//        else{
//
//        }
        createListener();
    }

    @Override
    public void onStop() {
        super.onStop();
        onCreateFlag = false;
    }

    private boolean isOnline()
    {
        boolean isConnected = false;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        isConnected = networkInfo != null && networkInfo.isConnected();
        return isConnected;
    }
    private void getRealTimeDocument(QueryDocumentSnapshot doc)
    {
        if(doc.get("documentName") != null)
        {
            newDocumentGet.documentName = doc.getString("documentName");
        }

        if(doc.get("pageNumber") != null)
        {
            newDocumentGet.pageNumber =(long) doc.get("pageNumber");
        }

        if(doc.get("id") != null)
        {
            newDocumentGet.idServer =(long) doc.get("id");
        }

        if(doc.get("nameUser") != null)
        {
            newDocumentGet.nameUser = doc.getString("nameUser");
        }

        if(doc.get("emailUser") != null)
        {
            newDocumentGet.emailUser = doc.getString("emailUser");
        }

        if(doc.get("phoneUser") != null)
        {
            newDocumentGet.phoneUser = doc.getString("phoneUser");
        }

        if(doc.get("documentPath") != null)
        {
            newDocumentGet.documentPath = doc.getString("documentPath");
        }

        if(doc.get("powerPoint") != null)
        {
            newDocumentGet.powerPoint = (boolean) doc.get("powerPoint");
        }

        if(doc.get("miseEnForme") != null)
        {
            newDocumentGet.miseEnForme = (boolean) doc.get("miseEnForme");
        }

        if(doc.get("docEnd") != null)
        {
            newDocumentGet.docEnd = (boolean) doc.get("docEnd");
        }

        if(doc.get("documentPaid") != null)
        {
            newDocumentGet.documentPaid = (boolean) doc.get("documentPaid");
        }

        if(doc.get("deliveryDate") != null)
        {
            newDocumentGet.deliveryDate = doc.getString("deliveryDate");
        }

        if(doc.get("userId") != null)
        {
            newDocumentGet.userId = doc.getString("userId");
        }

        if(doc.get("teamId") != null)
        {
            newDocumentGet.teamId = doc.getString("teamId");
        }

        else
        {
            newDocumentGet.teamId = "";
        }
    }

    private void createListener()
    {
        registration = dbFireStore.collection("Document")
                .whereEqualTo("userId", userId)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {

                        if(e != null)
                        {
                            return;
                        }

                        //TODO: CHECK IF USER IS ONLINE
                        if(isOnline())
                        {
                            //TODO: CHECK IF ADAPTER IS NULL
                            if(docAdapter == null)
                            {
                                //TODO:GET DATA FROM SERVER
                                int compteur = 0;
                                documentAvail = new ArrayList<>();
                                newDocumentGet = new DocumentAvailable();
                                if(snapshots.isEmpty())
                                {
                                    fileImage.setVisibility(View.VISIBLE);
                                    textNothing.setVisibility(View.VISIBLE);
                                    recyclerView.setVisibility(View.GONE);
                                }
                                else
                                {
                                    MainProcess.newDocumentServer.setText("1");
                                    documentAvail = new ArrayList<>();
                                    int i = 0;
                                    for (QueryDocumentSnapshot doc : snapshots)
                                    {
                                        newDocumentGet = new DocumentAvailable();
                                        getRealTimeDocument(doc);
                                        documentAvail.add(newDocumentGet);
                                        Log.d("DATA", "Current data: " + documentAvail.get(i).documentName);
                                        i++;
                                    }
                                    List<DocumentAvailable> docu = new ArrayList<>();
                                    for(int j = documentAvail.size(); j > 0; j--)
                                    {
                                        docu.add(documentAvail.get(j - 1));
                                    }

                                    textNothing.setVisibility(View.GONE);
                                    fileImage.setVisibility(View.GONE);
                                    recyclerView.setVisibility(View.VISIBLE);
                                    docAdapter = new DiscussionDocAvailableAdapter(docu);
                                    recyclerView.setAdapter(docAdapter);
                                    recyclerView.setHasFixedSize(true);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                                    recyclerView.setItemAnimator(new DefaultItemAnimator());
                                    GetDatabase getDatabase = new GetDatabase();
                                    getDatabase.execute();
                                }
                            }
                        }
                        else
                        {
                            if(onCreateFlag == false)
                            {
                                onCreateFlag = true;
                                LoadData loadData = new LoadData();
                                loadData.execute();
                            }
                            else{}
                        }
                    }
                });
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(int i) {
        if (mListener != null) {
            mListener.onFragmentInteraction(i);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int i);
    }

    public class SaveFirstDocument extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();

            Log.d("BACKGROUND TASK", "Background task launch");
            Log.d("BACKGROUND TASK", "Taille dela liste du premier appel: " + documentAvail.size());

            for(int i = 0; i < documentAvail.size(); i++)
            {
                db.userDao().insertNewDocAvailable(documentAvail.get(i));
            }

            //List<DocumentAvailable> list = db.userDao().selectListDocAvailable();
            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainProcess.newDocumentServer.setText("1");
            MainProcess.newDocumentServer.setVisibility(View.VISIBLE);
        }
        //        @Override
//        protected void onPostExecute(List<DocumentAvailable> docServer) {
//            super.onPostExecute(docServer);
//
//            if(docServer.size() != 0)
//            {
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putBoolean(MainProcess.DOCUMENT_AVAILABLE, true).apply();
//
//                MainProcess.newDocumentServer.setText("1");
//                fileImage.setVisibility(View.GONE);
//                textNothing.setVisibility(View.GONE);
//                recyclerView.setVisibility(View.VISIBLE);
//                Log.d("BACKGROUND TASK", "create adapter before");
//                docAdapter = new DiscussionDocAvailableAdapter(docServer);
//                Log.d("BACKGROUND TASK", "create adapter after");
//                Log.d("BACKGROUND TASK", "list " + docServer);
//                recyclerView.setAdapter(docAdapter);
//                recyclerView.setLayoutManager(new LinearLayoutManager(context));
//                recyclerView.setHasFixedSize(true);
//                recyclerView.setItemAnimator(new DefaultItemAnimator());
//                docAvailableFla = sharedPreferences.getBoolean(MainProcess.DOCUMENT_AVAILABLE, false);
//                registration.remove();
//                createListener(docAvailableFla);
//                //firstCallBack++;
//            }
//            else
//            {
//                fileImage.setVisibility(View.VISIBLE);
//                textNothing.setVisibility(View.VISIBLE);
//                recyclerView.setVisibility(View.GONE);
//            }
//        }
    }

    public class SaveUpdateDocument extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();

            db.userDao().insertNewDocAvailable(newDocumentGet);
            db.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainProcess.newDocumentServer.setText("1");
        }
    }

    public class LoadData extends AsyncTask<Void, Void, List<DocumentAvailable>>
    {
        @Override
        protected List<DocumentAvailable> doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();

            List<DocumentAvailable> list = db.userDao().selectListDocAvailable();

            db.close();

            return list;
        }

        @Override
        protected void onPostExecute(List<DocumentAvailable> documentAvailables) {
            super.onPostExecute(documentAvailables);

            if(documentAvailables.size() == 0)
            {
                fileImage.setVisibility(View.VISIBLE);
                textNothing.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                onCreateFlag = true;
            }
            else
            {
                fileImage.setVisibility(View.GONE);
                textNothing.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                docAdapter = new DiscussionDocAvailableAdapter(documentAvail);
                recyclerView.setAdapter(docAdapter);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                onCreateFlag = true;
            }

            //createListener();
        }
    }

    public class GetDatabase extends AsyncTask<Void, Void, List<DocumentAvailable>>
    {
        @Override
        protected List<DocumentAvailable> doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();

            List<DocumentAvailable> list = db.userDao().selectListDocAvailable();
            Log.d("LOCAL DATABASE", "Taille base de donnee locale" +list.size());

            if(documentAvail.size() != list.size())
            {


                for(int i = 0; i < documentAvail.size(); i++)
                {
                    Log.d("ARRAY DOC", "Array Doc Background task: " + documentAvail.get(i).documentName + " Valeur de compt: "+ i);
                    if(i < documentAvail.size() - 1)
                    {}
                    else
                    {
                        db.userDao().insertNewDocAvailable(documentAvail.get(i));
                    }
                }
            }

            //list = db.userDao().selectListDocAvailable();

            db.close();

            return null;
        }

        @Override
        protected void onPostExecute(List<DocumentAvailable> documentAvailables) {
            super.onPostExecute(documentAvailables);
            //TODO: CREATE RECYCLER VIEW WITH DATA
//
//            fileImage.setVisibility(View.GONE);
//            textNothing.setVisibility(View.GONE);
//            recyclerView.setVisibility(View.VISIBLE);
//            docAdapter = new DiscussionDocAvailableAdapter(documentAvail);
//            recyclerView.setAdapter(docAdapter);
//            recyclerView.setLayoutManager(new LinearLayoutManager(context));
//            recyclerView.setHasFixedSize(true);
//            recyclerView.setItemAnimator(new DefaultItemAnimator());

        }
    }

}
