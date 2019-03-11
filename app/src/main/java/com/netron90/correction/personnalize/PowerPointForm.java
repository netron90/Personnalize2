package com.netron90.correction.personnalize;

import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import java.util.List;

public class PowerPointForm extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton addDiapo;
    private SharedPreferences sharedPreferences;
    public static final String FIRST_INSERTION = "first_insertion";
    private int diapositiveNumber = 1;
    private int position = 0;
    private DiapositiveAdapter diapositiveAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_point_form);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.container_power_point_data);
        addDiapo = (FloatingActionButton) findViewById(R.id.fab_add_diapo);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        position = getIntent().getIntExtra("itemPosition", 0);
//        position = position + 1;

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

            Boolean firstInsert = sharedPreferences.getBoolean(FIRST_INSERTION, false);
            if(!firstInsert)
            {
                Log.d("FIRST INSERTION", "first insertion start");
                //TODO: GET SPECIFIC DOCUMENT AT SPECIFIC POSITION
                final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                        PersonnalizeDatabase.class, "scodelux").build();

                DocumentUser documentUser = db.userDao().selectDocument(DocumentAdapter.documentUserList.get(position).id);
                Log.d("FIRST INSERTION ID", "first insertion id: " + DocumentAdapter.documentUserList.get(position).id);
                //TODO: INSER FIRST DIAPO
                DiapositiveFormat diapositiveFormat = new DiapositiveFormat();
                diapositiveFormat.idDocument = documentUser.id;
                Log.d("DIAPOSITIVE", "diapo id: " + diapositiveNumber);
                diapositiveFormat.diapoTitle = "Diapositive "+ diapositiveNumber;
                Log.d("DIAPOSITIVE", "diapo title: " + diapositiveFormat.diapoTitle);
                diapositiveFormat.diapoDesc  = "";
                Log.d("DIAPOSITIVE", "diapo description: " + diapositiveFormat.diapoDesc);
                diapositiveFormat.nbrImage   = 0;
                Log.d("DIAPOSITIVE", "diapo image: " + diapositiveFormat.nbrImage);

                db.userDao().insertDiapo(diapositiveFormat);

                List<DiapositiveFormat> diapositiveFormats = db.userDao().selectDiapos(DocumentAdapter.documentUserList.get(position).id);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(FIRST_INSERTION, true).commit();

//                for(int i = 0; i < diapositiveFormats.size(); i++)
//                {
//                    diapositiveNumber = diapositiveFormats.get(i).id;
//                }
                return diapositiveFormats;
            }
            else{
                Log.d("SECOND INSERTION", "second insertion start");
                //TODO: LOAD ALL DIAPO
                final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                        PersonnalizeDatabase.class, "scodelux").build();

                DocumentUser documentUser = db.userDao().selectDocument(DocumentAdapter.documentUserList.get(position).id);

                List<DiapositiveFormat> diapositiveFormats = db.userDao().selectDiapos(documentUser.id);
                if(diapositiveFormats.size() == 0)
                {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(FIRST_INSERTION, true).commit();
                    return null;
                }
                else
                {
                    for(int i = 0; i < diapositiveFormats.size(); i++)
                    {
                        diapositiveNumber = diapositiveFormats.get(i).id;
                    }
                    return diapositiveFormats;
                }

            }
        }

        @Override
        protected void onPostExecute(List<DiapositiveFormat> diapositiveFormats) {
            super.onPostExecute(diapositiveFormats);


            if(diapositiveFormats == null)
            {
                LoadDiapo loadDiapo = new LoadDiapo();
                loadDiapo.execute();
            }
            else
            {
                diapositiveAdapter = new DiapositiveAdapter(diapositiveFormats,getApplicationContext());
                recyclerView.setAdapter(diapositiveAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setHasFixedSize(true);
                diapositiveNumber++;
            }


        }
    }

    public class AddDiapo extends AsyncTask<Void, Void, List<DiapositiveFormat>>
    {

        @Override
        protected List<DiapositiveFormat> doInBackground(Void... voids) {
            //TODO: GET SPECIFIC DOCUMENT AT SPECIFIC POSITION
            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "scodelux").build();

            //TODO: save first what is done previewsly
            if(TextUtils.isEmpty(DiapositiveAdapter.diapoHolderContent.getText().toString()))
            {
                return null;
            }
            else
            {
                List<DiapositiveFormat> diapoList = DiapositiveAdapter.diapositiveFormatsList;
                //TODO: GET DIAPOSITIVE TITLE AND SAVE IT INTO DATABASE
                //String diapoTitle  = diapoList.get(i).diapoTitle;
                db.userDao().updateDiapoTitle(DiapositiveAdapter.diapoHolderTitle.getText().toString(), diapoList.get(position).id);
              //  Log.d("DIAPOSITIVE", "diapo Title saved: " + diapoTitle + " taille: "+ diapoList.size() + " ID: " + diapoList.get(i).id);

                //TODO: GET DIAPOSITIVE DESCRIPTION AND SAVE IT INTO DATABASE
               // String diapoContent = diapoList.get(i).diapoDesc;
                db.userDao().updateDiapoDesc(DiapositiveAdapter.diapoHolderContent.getText().toString(), diapoList.get(position).id);
                //Log.d("DIAPOSITIVE", "diapo Content saved: " + diapoContent + " taille: "+ diapoList.size() + " ID: " + diapoList.get(i).id);

                //TODO: GET DIAPOSITIVE IMAGE NUMBER AND SAVE IT INTO DATABASE
                //int diapoImage  = diapoList.get(i).nbrImage;
                db.userDao().updateDiapoImage(DiapositiveAdapter.diapoHolderImage, diapoList.get(position).id);
               // Log.d("DIAPOSITIVE", "diapo Image saved: " + diapoImage + " taille: "+ diapoList.size() + " ID: " + diapoList.get(i).id);

                DocumentUser documentUser = db.userDao().selectDocument(DocumentAdapter.documentUserList.get(position).id);

                //TODO: INSER FIRST DIAPO
                DiapositiveFormat diapositiveFormat = new DiapositiveFormat();
                diapositiveFormat.idDocument = documentUser.id;
                diapositiveFormat.diapoTitle = "Diapositive "+ diapositiveNumber;
                diapositiveFormat.diapoDesc  = "";
                diapositiveFormat.nbrImage   = 0;

                db.userDao().insertDiapo(diapositiveFormat);

                List<DiapositiveFormat> diapositiveFormats = db.userDao().selectDiapos(DocumentAdapter.documentUserList.get(position).id);
                DiapositiveAdapter.diapositiveFormatsList.clear();
                DiapositiveAdapter.diapositiveFormatsList.addAll(diapositiveFormats);
                return diapositiveFormats;
            }


        }

        @Override
        protected void onPostExecute(List<DiapositiveFormat> diapositiveFormats) {
            super.onPostExecute(diapositiveFormats);

            if(diapositiveFormats == null)
            {
                Toast.makeText(PowerPointForm.this, "Veuillez remplir d'abord les informations de la présente diapositive", Toast.LENGTH_SHORT).show();
            }
            else {

                diapositiveAdapter.notifyDataSetChanged();
                diapositiveNumber++;
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

            for(int i = 0; i < diapoList.size(); i++)
            {
                //TODO: GET DIAPOSITIVE TITLE AND SAVE IT INTO DATABASE
                String diapoTitle  = diapoList.get(i).diapoTitle;
                db.userDao().updateDiapoTitle(DiapositiveAdapter.diapoHolderTitle.getText().toString(), diapoList.get(i).id);
                Log.d("DIAPOSITIVE", "diapo Title saved: " + diapoTitle + " taille: "+ diapoList.size() + " ID: " + diapoList.get(i).id);

                //TODO: GET DIAPOSITIVE DESCRIPTION AND SAVE IT INTO DATABASE
                String diapoContent = diapoList.get(i).diapoDesc;
                db.userDao().updateDiapoDesc(DiapositiveAdapter.diapoHolderContent.getText().toString(), diapoList.get(i).id);
                Log.d("DIAPOSITIVE", "diapo Content saved: " + diapoContent + " taille: "+ diapoList.size() + " ID: " + diapoList.get(i).id);

                //TODO: GET DIAPOSITIVE IMAGE NUMBER AND SAVE IT INTO DATABASE
                int diapoImage  = diapoList.get(i).nbrImage;
                db.userDao().updateDiapoImage(DiapositiveAdapter.diapoHolderImage, diapoList.get(i).id);
                Log.d("DIAPOSITIVE", "diapo Image saved: " + diapoImage + " taille: "+ diapoList.size() + " ID: " + diapoList.get(i).id);
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
