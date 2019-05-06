package com.netron90.correction.personnalize;

import android.arch.persistence.room.Room;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
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
import com.netron90.correction.personnalize.Database.DiapoImagePath;
import com.netron90.correction.personnalize.Database.DiapositiveFormat;
import com.netron90.correction.personnalize.Database.DocumentUser;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;

import java.util.ArrayList;
import java.util.List;

public class PowerPointForm extends AppCompatActivity implements AddPictureDiapoFragment.OnFragmentInteractionListener {

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
    public static int idDiapositive = 0;
    public static int diapositivePosition = 0;
    public static DocumentUser documentUser = null;
    private Uri imageUri = null;
    private ClipData mClipData = null;
    private  List<DiapoImagePath> diapoImagePathsSelected = null;
    private String previewTitle = "";
    private String previewContent = "";
    private int totalSize = 0;
    private boolean onLoad = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_point_form);
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.container_power_point_data);
        addDiapo = findViewById(R.id.fab_add_diapo);
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

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == DiapositiveAdapter.DIAPOSITIVE_BROWSER && resultCode == RESULT_CANCELED)
        {

        }
        else
        {
            Log.d("DIAPO IMAGE", "Diapo file broser OK");
            //idDiapositive = data.getIntExtra("diapoId", 0);
            Log.d("DIAPO IMAGE", "Diapositive ID: " + idDiapositive);
            Log.d("DIAPO IMAGE", "Diapositive position: " + diapositivePosition);
            if(requestCode == DiapositiveAdapter.DIAPOSITIVE_BROWSER && requestCode == RESULT_OK && null != data)
            {


            }
            else
            {
                Log.d("DIAPO IMAGE", "Multiple image");
                if(data.getClipData() != null)
                {
                    imageUri = null;
                    mClipData = data.getClipData();
                    SelectImageMultiple selectImageMultiple = new SelectImageMultiple();
                    selectImageMultiple.execute();
                }
                else {
                    imageUri = data.getData();
                    mClipData = null;
                    SelectImageMultiple selectImageMultiple = new SelectImageMultiple();
                    selectImageMultiple.execute();
                }
            }
        }

    }

    @Override
    public void onFragmentInteraction(int i) {
        if(i == 1)
        {
            Log.d("ADD IMAGE FRAG", "Add image diapositive");
        }
    }

    public class LoadDiapo extends AsyncTask<Void, Void, List<DiapositiveFormat>>
    {
        @Override
        protected List<DiapositiveFormat> doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

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
                for(int i = 0; i < allDiapo.size(); i++)
                {
                    List<DiapoImagePath> diapoPath = db.userDao().selectDiapoImagePath(allDiapo.get(i).id);
                    allDiapo.get(i).nbrImage = diapoPath.size();
                }
                return allDiapo;
            }
            else
            {
                //TODO: SELECT ALL DIAPO IN THIS DOCUMENT
                List<DiapositiveFormat> allDiapo = db.userDao().selectDiapos(idDocument);
                for(int i = 0; i < allDiapo.size(); i++)
                {
                    List<DiapoImagePath> imagePaths = db.userDao().selectDiapoImagePath(allDiapo.get(i).id);
                    Log.d("SIZE", "Image Path Size: " + imagePaths.size());
                    allDiapo.get(i).nbrImage = imagePaths.size();
                }

                diapositiveNumber = allDiapo.size();
                return allDiapo;
            }


        }

        @Override
        protected void onPostExecute(List<DiapositiveFormat> diapositiveFormats) {
            super.onPostExecute(diapositiveFormats);

            //diapositiveNumber++;
            if (onLoad == true)
            {
                diapositiveAdapter.notifyItemChanged(diapositivePosition);
                onLoad = false;
            }
            else
            {
                diapositiveAdapter = new DiapositiveAdapter(diapositiveFormats, PowerPointForm.this);
                recyclerView.setAdapter(diapositiveAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                recyclerView.setHasFixedSize(true);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            }


        }
    }

    public class AddDiapo extends AsyncTask<Void, Void, List<DiapositiveFormat>>
    {

        @Override
        protected List<DiapositiveFormat> doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                PersonnalizeDatabase.class, "personnalize").build();


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

                    Log.d("ADD DATA", "Add Data DIAPO id: " + DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                    Log.d("ADD DATA", "Diapositive Number: " + diapositiveNumber);
                    Log.d("ADD DATA", "Preview diapo title: " + titleDiapo);
                    Log.d("ADD DATA", "Preview diapo content: " + descContent);
                    Log.d("ADD DATA", "Preview diapo Image: " + nbrImage);

                    db.userDao().updateDiapoTitle(titleDiapo, DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                    db.userDao().updateDiapoDesc(descContent, DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                    db.userDao().updateDiapoImage(nbrImage, DiapositiveAdapter.diapositiveFormatsList.get(i).id);



                }
                diapositiveNumber++;
                //TODO: CREATE NEW DiapositiveFormat OBJCT
                DiapositiveFormat addNewDiapo = new DiapositiveFormat();
                addNewDiapo.idDocument = idDocument;
                addNewDiapo.diapoTitle = "Diapositive " + diapositiveNumber;
                addNewDiapo.diapoDesc  = "";
                addNewDiapo.nbrImage   = 0;

                db.userDao().insertDiapo(addNewDiapo);
                List<DiapositiveFormat> diapoSaved = db.userDao().selectDiapos(idDocument);
                DiapositiveAdapter.diapositiveFormatsList.clear();
                for(int i = 0; i < diapoSaved.size(); i++)
                {
//                    Log.d("DIAPO ADD", "Diapo id: " + diapoSaved.get(i).id);
//                    Log.d("DIAPO ADD", "Diapo idDocument: " + diapoSaved.get(i).idDocument);
//                    Log.d("DIAPO ADD", "Diapo Title: " + diapoSaved.get(i).diapoTitle);
//                    Log.d("DIAPO ADD", "Diapo Content: " + diapoSaved.get(i).diapoDesc);
                    DiapositiveFormat diapositiveFormat = new DiapositiveFormat();
                    diapositiveFormat.id = diapoSaved.get(i).id;
                    diapositiveFormat.idDocument = diapoSaved.get(i).idDocument;
                    diapositiveFormat.diapoTitle = diapoSaved.get(i).diapoTitle;
                    diapositiveFormat.diapoDesc = diapoSaved.get(i).diapoDesc;
                    diapositiveFormat.nbrImage = diapoSaved.get(i).nbrImage;
                    DiapositiveAdapter.diapositiveFormatsList.add(diapositiveFormat);
                }

                return DiapositiveAdapter.diapositiveFormatsList;
            }



        }

        @Override
        protected void onPostExecute(List<DiapositiveFormat> diapositiveFormats) {
            super.onPostExecute(diapositiveFormats);

            if(diapositiveFormats != null)
            {
                //DiapositiveAdapter.diapositiveFormatsList.addAll(diapositiveFormats);
                diapositiveAdapter.notifyDataSetChanged();
            }

        }
    }

    //TODO:METHOD FOR SAVE ALL DIAPOSITIVE DATA
    public class SaveDiapoData extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected Void doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();
            for(int i = 0; i < DiapositiveAdapter.diapositiveFormatsList.size(); i++)
            {


                String titleDiapo   = DiapositiveAdapter.diapositiveFormatsList.get(i).diapoTitle;
                String descContent  = DiapositiveAdapter.diapositiveFormatsList.get(i).diapoDesc;
                int nbrImage        = DiapositiveAdapter.diapositiveFormatsList.get(i).nbrImage;

                Log.d("SAVE DATA", "Add Data DIAPO id: " + DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                Log.d("SAVE DATA", "Diapositive Number: " + diapositiveNumber);
                Log.d("SAVE DATA", "Preview diapo title: " + titleDiapo);
                Log.d("SAVE DATA", "Preview diapo content: " + descContent);
                Log.d("SAVE DATA", "Preview diapo Image: " + nbrImage);

                diapositiveAdapter.notifyItemChanged(i);
                titleDiapo   = DiapositiveAdapter.diapositiveFormatsList.get(i).diapoTitle;
                descContent  = DiapositiveAdapter.diapositiveFormatsList.get(i).diapoDesc;
                nbrImage        = DiapositiveAdapter.diapositiveFormatsList.get(i).nbrImage;

                Log.d("SAVE DATA", "Add Data DIAPO id notify: " + DiapositiveAdapter.diapositiveFormatsList.get(i).id);
                Log.d("SAVE DATA", "Diapositive Number notify: " + diapositiveNumber);
                Log.d("SAVE DATA", "Preview diapo title notify: " + titleDiapo);
                Log.d("SAVE DATA", "Preview diapo content notify: " + descContent);
                Log.d("SAVE DATA", "Preview diapo Image notify: " + nbrImage);

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


    public class SelectImageMultiple extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

            List<DiapositiveFormat> listTotal = new ArrayList<>();
            ArrayList<Uri> imageSelectedUri = new ArrayList<>();
//            String[] filePath = {MediaStore.Images.Media.DATA};


            if(imageUri != null)
            {
                String imagePath = imageUri.toString();
                Log.d("IMAGE PATH", "Chemin de l'image: "+ imagePath);

                DiapositiveFormat currentDiapo = db.userDao().selectDiapo(idDiapositive);
                if(sharedPreferences.getBoolean("diapo_path_added", false) == false)
                {
                    //TODO:SAVE IMAGE PATH

                    DiapoImagePath diapoImagePath = new DiapoImagePath();

                    diapoImagePath.idPath = currentDiapo.id;
                    diapoImagePath.imagePath = imagePath;

                    db.userDao().insertDiapoImagePath(diapoImagePath);

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("diapo_path_added", true).commit();

                    //TODO: SELECT ALL DIAPOSITIVE IMAGES
                    diapoImagePathsSelected = db.userDao().selectDiapoImagePath(currentDiapo.id);

                    //TODO: GET PREVIEW INFO OF CURRENT DIAPO
                    DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).nbrImage = diapoImagePathsSelected.size();

                   // String diapositiveTitle = DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).diapoTitle;
                    String diapositiveDescription = DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).diapoDesc;
                    DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).diapoDesc = diapositiveDescription;

                    onLoad = true;


                   return null;

                }
                else
                {
                    //TODO: DELETE ALL DiapoImagePAth WHERE idPath == "currentDiapo.id"
                    db.userDao().deleteDiapoImagePath(currentDiapo.id);

                    //TODO: RECREATE NEW DiapoImagePAth
                    DiapoImagePath diapoImagePath = new DiapoImagePath();

                    diapoImagePath.idPath = currentDiapo.id;
                    diapoImagePath.imagePath = imagePath;

                    db.userDao().insertDiapoImagePath(diapoImagePath);

                    //TODO: SELECT ALL DIAPOSITIVE IMAGES
                    diapoImagePathsSelected = db.userDao().selectDiapoImagePath(currentDiapo.id);

                    //TODO: GET PREVIEW INFO OF CURRENT DIAPO
                    DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).nbrImage = diapoImagePathsSelected.size();
                    String diapositiveDescription = DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).diapoDesc;
                    DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).diapoDesc = diapositiveDescription;

                    onLoad = true;

                    return null;

                }

            }
            else
            {

                if(mClipData.getItemCount() > 5)
                {
                    //DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).nbrImage = mClipData.getItemCount();
                    totalSize = mClipData.getItemCount();
                }
                else
                {
                    DiapositiveFormat currentDiapo = db.userDao().selectDiapo(idDiapositive);
                    if(sharedPreferences.getBoolean("diapo_path_added", false) == false)
                    {
                        for(int i = 0; i < mClipData.getItemCount(); i++)
                        {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uris = item.getUri();
                            imageSelectedUri.add(uris);
                            String imagePath = imageSelectedUri.get(i).toString();
                            Log.d("IMAGE PATH", "Chemin de l'image: "+ imagePath);

                            DiapoImagePath diapoImagePath = new DiapoImagePath();

                            diapoImagePath.idPath = currentDiapo.id;
                            diapoImagePath.imagePath = imagePath;

                            db.userDao().insertDiapoImagePath(diapoImagePath);


                        }

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putBoolean("diapo_path_added", true).commit();

                        //TODO: SELECT ALL DIAPOSITIVE IMAGES
                        diapoImagePathsSelected = db.userDao().selectDiapoImagePath(currentDiapo.id);

                        //TODO: GET PREVIEW INFO OF CURRENT DIAPO
                        DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).nbrImage = diapoImagePathsSelected.size();
                        String diapositiveDescription = DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).diapoDesc;
                        DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).diapoDesc = diapositiveDescription;

                        onLoad = true;


                    }
                    else
                    {
                        //TODO: DELETE ALL DiapoImagePAth WHERE idPath == "currentDiapo.id"
                        db.userDao().deleteDiapoImagePath(currentDiapo.id);

                        for(int i = 0; i < mClipData.getItemCount(); i++)
                        {

                            //TODO: RECREATE NEW DiapoImagePAth
                            DiapoImagePath diapoImagePath = new DiapoImagePath();
                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uris = item.getUri();
                            imageSelectedUri.add(uris);
                            String imagePath = imageSelectedUri.get(i).toString();
                            Log.d("IMAGE PATH", "Chemin de l'image: "+ imagePath);

                            diapoImagePath.idPath = currentDiapo.id;
                            diapoImagePath.imagePath = imagePath;

                            db.userDao().insertDiapoImagePath(diapoImagePath);


                        }
                        //TODO: SELECT ALL DIAPOSITIVE IMAGES
                        diapoImagePathsSelected = db.userDao().selectDiapoImagePath(currentDiapo.id);

                        //TODO: GET PREVIEW INFO OF CURRENT DIAPO
                        DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).nbrImage = diapoImagePathsSelected.size();
                        String diapositiveDescription = DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).diapoDesc;
                        DiapositiveAdapter.diapositiveFormatsList.get(diapositivePosition).diapoDesc = diapositiveDescription;

                        onLoad = true;


                    }


                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
//            Log.d("IMAGE DIAPO", "Nombre d'image: " + diapositiveFormats.get(diapositivePosition).nbrImage);
//            diapositiveAdapter.notifyDataSetChanged();
            if(totalSize > 5)
            {
                Toast.makeText(PowerPointForm.this, "Vous pouvez uniquement ajouter jusqu'à 5 images pour une diapo.", Toast.LENGTH_SHORT).show();
                totalSize = 0;
            }
//            else
//            {
//                diapositiveAdapter.notifyItemChanged(diapositivePosition);
//            }


        }
    }
}
