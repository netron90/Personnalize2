package com.netron90.correction.personnalize;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.google.firebase.firestore.DocumentReference;
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
    private ListenerRegistration registration;
    private DocumentAvailable document = null;
    private Context context;
    private InsertNewDocAvailable insertNewDocAvailable;
    private InsertNewDocAvailableNew insertNewDocAvailableNew;
    private RelativeLayout fileImage;
    private TextView textNothing;
    private RecyclerView recyclerView;
    private DiscussionDocAvailableAdapter docAdapter;
    private DocumentAvailable newDocumentGet;
    private GetAvailableDocument getAvailableDocument;
    List<DocumentAvailable> documentAvailables= new ArrayList<>();

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

        fileImage   = view.findViewById(R.id.file_empty);
        textNothing = view.findViewById(R.id.text_nothing);
        recyclerView = view.findViewById(R.id.docAvailableRecyclerView);

        boolean docAvailable = MainProcess.sharedPreferences.getBoolean(MainProcess.DOCUMENT_AVAILABLE, false);
        if(docAvailable == false)
        {
            //TODO: KEEP FRAGMENT FILE EMPTY ON INTERFACE

            dbFireStore  = FirebaseFirestore.getInstance();
            registration = dbFireStore.collection("Document")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {

                            if(e != null)
                            {
                                Log.d("LISTENE UPDATE", "Listen failed. ", e);
                                return;
                            }

                            if(value.isEmpty())
                            {}
                            else
                            {
                                //TODO: GET NEW DOCUMENT
                                for(QueryDocumentSnapshot doc : value)
                                {
                                    Log.d("VALUE SNAPCHAT", "Document firebase: " + doc);
                                    DocumentAvailable document = new DocumentAvailable();

                                    if(doc.get("id") != null)
                                    {
                                        document.idServer = (Long) doc.get("id");
                                    }

                                    if(doc.get("documentName") != null)
                                    {
                                        document.documentName = doc.getString("documentName");
                                    }
                                    if(doc.get("pageNumber") != null)
                                    {
                                        document.pageNumber = (Long) doc.get("pageNumber");
                                    }

                                    if(doc.get("powerPoint") != null)
                                    {
                                        document.powerPoint = (boolean) doc.get("powerPoint");
                                    }
                                    if(doc.get("miseEnForme") != null)
                                    {
                                        document.miseEnForme = (boolean) doc.get("miseEnForme");
                                    }
                                    if(doc.get("documentName") != null)
                                    {
                                        document.deliveryDate = doc.getString("deliveryDate");
                                    }
                                    if(doc.get("docEnd") != null)
                                    {
                                        document.docEnd = (boolean) doc.get("docEnd");
                                    }
                                    if(doc.get("docPaid") != null)
                                    {
                                        document.documentPaid = (boolean) doc.get("docPaid");
                                    }

                                    documentAvailables.add(document);
                                }

                                insertNewDocAvailable = new InsertNewDocAvailable();
                                insertNewDocAvailable.execute();

                            }

                        }
                    });
        }
        else
        {
            fileImage.setVisibility(View.GONE);
            textNothing.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);



            dbFireStore  = FirebaseFirestore.getInstance();
            getAvailableDocument = new GetAvailableDocument();
            getAvailableDocument.execute();
        }

        //final boolean documentAvailableFlag = MainProcess.sharedPreferences.getBoolean(MainProcess.DOCUMENT_AVAILABLE, false);


        return view;
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

    @Override
    public void onPause() {
        super.onPause();
        insertNewDocAvailable = null;
        //TODO: REMOVE LISTENER
        registration.remove();
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

    public class InsertNewDocAvailable extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();

            for(int i = 0; i < documentAvailables.size(); i++)
            {
                db.userDao().insertNewDocAvailable(documentAvailables.get(i));
            }

            db.close();


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            MainProcess.newDocumentServer.setText("" + documentAvailables.size());
            MainProcess.newDocumentServer.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = MainProcess.sharedPreferences.edit();
            editor.putBoolean(MainProcess.DOCUMENT_AVAILABLE, true).commit();

            fileImage.setVisibility(View.GONE);
            textNothing.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            docAdapter = new DiscussionDocAvailableAdapter(documentAvailables);
            recyclerView.setAdapter(docAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setItemAnimator(new DefaultItemAnimator());



//            //TODO: CHANGE FRAGMENT TO NEW DOCUMENT AVAILABLE FRAGMENT
//            DiscussionDocAvailableFragment docAvailableFragment = new DiscussionDocAvailableFragment();
//            MainProcess.fragmentManager.beginTransaction().replace(R.id.container_fragment_discussion_empty, docAvailableFragment).commit();

        }
    }


    public class GetAvailableDocument extends AsyncTask<Void, Void, List<DocumentAvailable>>
    {
        @Override
        protected List<DocumentAvailable> doInBackground(Void... voids) {

            PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();
            List<DocumentAvailable> d = db.userDao().selectListDocAvailable();
            Log.d("LIST DO AVAILABLE", "docavailable: " + d.get(0).documentName);

            db.close();
            return d;
        }

        @Override
        protected void onPostExecute(List<DocumentAvailable> documentAvailable) {
            super.onPostExecute(documentAvailable);

            docAdapter = new DiscussionDocAvailableAdapter(documentAvailable);
            recyclerView.setAdapter(docAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            registration = dbFireStore.collection("Document").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                    if(e != null)
                    {
                        Log.d("LISTENE UPDATE", "Listen failed. ", e);
                        return;
                    }

                    if(value.isEmpty())
                    {}
                    else
                    {
                        int compteur = 0;
                        for(QueryDocumentSnapshot doc : value)
                        {
                            if(compteur == 0)
                            {
                                Log.d("VALUE SNAPCHAT", "First call!!! Document firebase: " + doc);
                                compteur++;
                            }
                            else
                            {
                                if(value.size() - 1 == compteur)
                                {
                                    Log.d("VALUE SNAPCHAT", "Document firebase: " + doc);
                                    DocumentAvailable document = new DocumentAvailable();

                                    if(doc.get("id") != null)
                                    {
                                        document.idServer = (Long) doc.get("id");
                                    }

                                    if(doc.get("documentName") != null)
                                    {
                                        document.documentName = doc.getString("documentName");
                                    }
                                    if(doc.get("pageNumber") != null)
                                    {
                                        document.pageNumber = (Long) doc.get("pageNumber");
                                    }

                                    if(doc.get("powerPoint") != null)
                                    {
                                        document.powerPoint = (boolean) doc.get("powerPoint");
                                    }
                                    if(doc.get("miseEnForme") != null)
                                    {
                                        document.miseEnForme = (boolean) doc.get("miseEnForme");
                                    }
                                    if(doc.get("documentName") != null)
                                    {
                                        document.deliveryDate = doc.getString("deliveryDate");
                                    }
                                    if(doc.get("docEnd") != null)
                                    {
                                        document.docEnd = (boolean) doc.get("docEnd");
                                    }
                                    if(doc.get("docPaid") != null)
                                    {
                                        document.documentPaid = (boolean) doc.get("docPaid");
                                    }

//
                                    newDocumentGet = document;
                                    insertNewDocAvailableNew = new InsertNewDocAvailableNew();
                                    insertNewDocAvailable.execute();

                                }
                                else {
                                    compteur++;
                                }
                            }


                        }
                    }

                }
            });

        }
    }

    public class InsertNewDocAvailableNew extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();


                db.userDao().insertNewDocAvailable(newDocumentGet);


            db.close();
            fileImage.setVisibility(View.GONE);
            textNothing.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            DiscussionDocAvailableAdapter.listDocAvailable.add(newDocumentGet);
            docAdapter.notifyDataSetChanged();
        }
    }


}
