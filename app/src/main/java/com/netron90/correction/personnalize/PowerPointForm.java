package com.netron90.correction.personnalize;

import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.netron90.correction.personnalize.Database.DiapositiveFormat;
import com.netron90.correction.personnalize.Database.DocumentUser;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;

import java.util.ArrayList;
import java.util.List;

public class PowerPointForm extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton addDiapo;
    private SharedPreferences sharedPreferences;
    public static final String FIRST_INSERTION = "first_insertion";
    public static int diapositiveNumber = 1;
    private int position = 0;
    public static DiapositiveAdapter diapositiveAdapter;
    private final String DIAPOSITIVE_TITLE = "Diapositive 1";
    public static int idDocument = 0;
    public static DocumentUser documentUser = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_point_form);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.container_power_point_data);
        addDiapo = (FloatingActionButton) findViewById(R.id.fab_add_diapo);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        position = getIntent().getIntExtra("itemPosition", 0);
        diapositiveNumber = 1;
        //position = position + 1;

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        addDiapo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddDiapo addDiapo = new AddDiapo();
                addDiapo.execute();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        LoadDiapo loadDiapo = new LoadDiapo();
        loadDiapo.execute();

    }

    public class LoadDiapo extends AsyncTask<Void, Void, List<DiapositiveFormat>>
    {
        @Override
        protected List<DiapositiveFormat> doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "scodelux").build();

            //TODO: SELECT DOCUMENT AT SPECIFIC POSITION
            documentUser = db.userDao().selectDocument(position);
            idDocument = documentUser.id;

            documentUser = db.userDao().selectDocument(idDocument);
            Log.d("LOAD DATA", "Document ID: " + documentUser.id);
            Log.d("LOAD DATA", "Document Name: " + documentUser.documentName);
            Log.d("LOAD DATA", "Document Mise en forme: " + documentUser.miseEnForme);
            Log.d("LOAD DATA", "Document Power Point: " + documentUser.powerPoint);

            //TODO:GET ALL DIAPOSITIVES WHERE "idDocument == documentUser.id"
            List<DiapositiveFormat> diapoDocument = db.userDao().selectDiapos(idDocument);

            //TODO:CHECK IF "diapoDocument.size() == 0"
            if (diapoDocument.size() == 0)
            {
                Log.d("LOAD DATA", "There are no diapositive in this document");

                //TODO: CREATE DiapositiveFormat OBJECT
                DiapositiveFormat firstDiapo = new DiapositiveFormat();
                firstDiapo.idDocument = idDocument;
                firstDiapo.diapoTitle = DIAPOSITIVE_TITLE;
                firstDiapo.diapoDesc  = "";
                firstDiapo.nbrImage   = 0;

                db.userDao().insertDiapo(firstDiapo);

                //TODO: SELECT ALL DIAPO IN THIS DOCUMENT
                List<DiapositiveFormat> allDiapo = db.userDao().selectDiapos(idDocument);
                return allDiapo;
            }
            else
            {
                //TODO: SELECT ALL DIAPO IN THIS DOCUMENT
                List<DiapositiveFormat> allDiapo = db.userDao().selectDiapos(idDocument);
                diapositiveNumber = allDiapo.size();
                return allDiapo;
            }


        }

        @Override
        protected void onPostExecute(List<DiapositiveFormat> diapositiveFormats) {
            super.onPostExecute(diapositiveFormats);

            diapositiveNumber++;
            diapositiveAdapter = new DiapositiveAdapter(diapositiveFormats, getApplicationContext());
            recyclerView.setAdapter(diapositiveAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

        }
    }

    public class AddDiapo extends AsyncTask<Void, Void, List<DiapositiveFormat>>
    {

        @Override
        protected List<DiapositiveFormat> doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                PersonnalizeDatabase.class, "scodelux").build();


            //TODO: SAVE PREVIEW INSERTION
            if(diapositiveAdapter == null)
            {
                Log.d("ADD DATA", "Adapter is null: ");
                return  null;
            }
            else{
                for(int i = 0; i < DiapositiveAdapter.diapositiveFormatsList.size(); i++)
                {

                    diapositiveAdapter.notifyItemChanged(i);
                    String titleDiapo   = DiapositiveAdapter.diapositiveFormatsList.get(i).diapoTitle;
                    String descContent  = DiapositiveAdapter.diapositiveFormatsList.get(i).diapoDesc;
                    int nbrImage        = DiapositiveAdapter.diapositiveFormatsList.get(i).nbrImage;

                    Log.d("SAVE DATA", "Add Data DIAPO id: " + DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                    Log.d("ADD DATA", "Diapositive Number: " + diapositiveNumber);
                    Log.d("ADD DATA", "Preview diapo title: " + titleDiapo);
                    Log.d("ADD DATA", "Preview diapo content: " + descContent);
                    Log.d("ADD DATA", "Preview diapo Image: " + nbrImage);

                    db.userDao().updateDiapoTitle(titleDiapo, DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                    db.userDao().updateDiapoDesc(descContent, DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                    db.userDao().updateDiapoImage(nbrImage, DiapositiveAdapter.diapositiveFormatsList.get(i).id);

                }
                //TODO: CREATE NEW DiapositiveFormat OBJCT
                DiapositiveFormat addNewDiapo = new DiapositiveFormat();
                addNewDiapo.idDocument = idDocument;
                addNewDiapo.diapoTitle = "Diapositive " + diapositiveNumber;
                addNewDiapo.diapoDesc  = "";
                addNewDiapo.nbrImage   = 0;

                db.userDao().insertDiapo(addNewDiapo);
                List<DiapositiveFormat> diapoSaved = db.userDao().selectDiapos(idDocument);
                DiapositiveAdapter.diapositiveFormatsList.clear();

                return diapoSaved;
            }



        }

        @Override
        protected void onPostExecute(List<DiapositiveFormat> diapositiveFormats) {
            super.onPostExecute(diapositiveFormats);

            if(diapositiveFormats != null)
            {
                DiapositiveAdapter.diapositiveFormatsList.addAll(diapositiveFormats);
                diapositiveAdapter.notifyDataSetChanged();
            }

        }
    }

    //TODO:METHOD FOR SAVE ALL DIAPOSITIVE DATA
    public class SaveDiapoData extends AsyncTask<Void, Void, Void>
    {
        List<DiapositiveFormat> diapoList = DiapositiveAdapter.diapositiveFormatsList;
        @Override
        protected Void doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "scodelux").build();
            Log.d("SAVE DATA", "Taille de la liste des diao: " + DiapositiveAdapter.diapositiveFormatsList.size());
            diapositiveAdapter.notifyItemChanged(diapositiveNumber);

            //List<DiapositiveFormat> getDiapoSaved = db.userDao().selectDiapos(idDocument);
            for(int i = 0; i < DiapositiveAdapter.diapositiveFormatsList.size(); i++)
            {

                //diapositiveAdapter.notifyItemChanged(i);
                String titleDiapo   = DiapositiveAdapter.diapositiveFormatsList.get(i).diapoTitle;
                String descContent  = DiapositiveAdapter.diapositiveFormatsList.get(i).diapoDesc;
                int nbrImage        = DiapositiveAdapter.diapositiveFormatsList.get(i).nbrImage;

                Log.d("SAVE DATA", "DIAPO id: " + DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                Log.d("SAVE DATA", "Diapositive Number: " + diapositiveNumber);
                Log.d("SAVE DATA", "Preview diapo title: " + titleDiapo);
                Log.d("SAVE DATA", "Preview diapo content: " + descContent);
                Log.d("SAVE DATA", "Preview diapo Image: " + nbrImage);

                db.userDao().updateDiapoTitle(titleDiapo, DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                db.userDao().updateDiapoDesc(descContent, DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                db.userDao().updateDiapoImage(nbrImage, DiapositiveAdapter.diapositiveFormatsList.get(i).id);

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.save_diapo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.save)
        {
            SaveDiapoData saveDiapoData = new SaveDiapoData();
            saveDiapoData.execute();
        }
        return super.onOptionsItemSelected(item);
    }
}
