package com.netron90.correction.personnalize;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.OpenableColumns;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.aspose.words.Document;
import com.google.firebase.auth.FirebaseAuth;
import com.netron90.correction.personnalize.Database.DocumentUser;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by CHRISTIAN on 14/02/2019.
 */

public class MainProcess extends AppCompatActivity implements MemoireFragmentEmpty.OnFragmentInteractionListener,
        DiscussionFragment.OnFragmentInteractionListener, MemoireFragment.OnFragmentInteractionListener,
RootFragment.OnFragmentInteractionListener{

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
//    private DrawerLayout drawerLayout;
//    private NavigationView navigationView;
    private static final int FILE_BROWSER = 0, FILE_BROWSER_SEOND = 1;
    private String path, fileName;
    private Uri uriFile;
    private FileSearchData fileSearchData;
    private FileSearchDataSecond fileSearchDataSecond;
    private LoadData loadData;
    public static SharedPreferences sharedPreferences;
    public final static String DOCUMENT_EXIST = "documentExist";
    public static List<DocumentUser> documentUserQuery = null;
    public static FragmentManager fragmentManager = null;
    public static android.app.FragmentManager fragmentManagerDatePicker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
//        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        fragmentManager = getSupportFragmentManager();
        fragmentManagerDatePicker = getFragmentManager();

//        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(MenuItem item) {
//
//                int idMenu = item.getItemId();
//                if(idMenu == R.id.logout)
//                {
//                    drawerLayout.closeDrawers();
//                    Toast.makeText(MainProcess.this, "LogOut clicked", Toast.LENGTH_SHORT).show();
//                    FirebaseAuth.getInstance().signOut();
//                    Intent loginIntent = new Intent(MainProcess.this, MainActivity.class);
//                    finish();
//                    startActivity(loginIntent);
//                }
//                return true;
//            }
//        });

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean emptyFragment = sharedPreferences.getBoolean(DOCUMENT_EXIST, false);

        if(emptyFragment == false)
        {
            TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
            tabAdapter.addFragment(new RootFragment(), "MEMOIRES");
            tabAdapter.addFragment(new DiscussionFragment(), "DISCUSSION");
            viewPager.setAdapter(tabAdapter);
            tabLayout.setupWithViewPager(viewPager);
        }
        else
        {
            //TODO: Load database
            loadData = new LoadData();
            loadData.execute();
        }




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.logout)
        {
           // drawerLayout.openDrawer(GravityCompat.START);
            FirebaseAuth.getInstance().signOut();
            Intent loginIntent = new Intent(MainProcess.this, MainActivity.class);
            finish();
            startActivity(loginIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(int i) {
        if(i == 1)
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            startActivityForResult(Intent.createChooser(intent, "Chose your document"), FILE_BROWSER);
        }
        else if(i == 2)
        {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            startActivityForResult(Intent.createChooser(intent, "Chose your document"), FILE_BROWSER_SEOND);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FILE_BROWSER)
        {
            if(resultCode == RESULT_OK)
            {
                uriFile = data.getData();
                // Uri Scheme: content://com.android.providers.downloads.documents/document/831
                Log.d("Uri file", "Uri file string: " + uriFile.toString()+ " Uri path: "+ uriFile.getPath());

                fileSearchData = new FileSearchData();
                fileSearchData.execute();
            }
            else{
                if(MemoireFragmentEmpty.progressBar != null && MemoireFragmentEmpty.documentCreating != null)
                {
                    MemoireFragmentEmpty.progressBar.setVisibility(View.GONE);
                    MemoireFragmentEmpty.documentCreating.setVisibility(View.GONE);
                }
                else{}
            }


        }

        if(requestCode == FILE_BROWSER_SEOND)
        {

            if(resultCode == RESULT_OK)
            {
                uriFile = data.getData();
                Log.d("Uri file", "Uri file string: " + uriFile.toString()+ " Uri path: "+ uriFile.getPath());
                FileSearchDataSecond fileSearchDataSecond = new FileSearchDataSecond();
                fileSearchDataSecond.execute();
            }
           else
            {
                if(MemoireFragment.progressBar != null && MemoireFragment.documentCreating != null)
                {
                    MemoireFragment.progressBar.setVisibility(View.GONE);
                    MemoireFragment.documentCreating.setVisibility(View.GONE);
                }
                else{}
            }

        }


    }

    public class FileSearchData extends AsyncTask<Void, Void, List<DocumentUser>>{

        @Override
        protected List<DocumentUser> doInBackground(Void... voids) {
            //1) Exctrat file name
            String fileNameUri = "";
            FileInputStream fileInputStream = null;

            int pageCount = 0;
            Cursor cursor =  getContentResolver().query(uriFile, null, null, null, null);
            try
            {
                if(cursor != null && cursor.moveToFirst())
                {
                    fileNameUri = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    String arrayStringName[] = fileNameUri.split(".docx|.doc");
                    fileName = arrayStringName[0];
                }
            }finally {
                cursor.close();
            }
            Log.d("File name", "File name is: "+fileName);

            try {
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uriFile, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                fileInputStream = new FileInputStream(fileDescriptor);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            //2) get page number of document
            try {
                Document doc = new Document(fileInputStream);
                pageCount = doc.getPageCount();
                Log.d("PageNumber", "doc page number is: "+pageCount);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //TODO: save information into database
            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

            DocumentUser documentUser = new DocumentUser();
            documentUser.documentName = fileName;
            documentUser.pageNumber = pageCount;
            documentUser.documentPath = uriFile.toString();

            db.userDao().insertDocument(documentUser);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DOCUMENT_EXIST, true).commit();
            //TODO: SELECT ALL DOCUMENT AND SHOW DOCUMENT FRAGMENT

            List<DocumentUser> listDocument = db.userDao().selectAllDocument();
            return listDocument;
        }

        @Override
        protected void onPostExecute(List<DocumentUser> documentUsers) {
            super.onPostExecute(documentUsers);

            documentUserQuery = documentUsers;
            MemoireFragmentEmpty.progressBar.setVisibility(View.GONE);
            MemoireFragmentEmpty.documentCreating.setVisibility(View.GONE);


            Log.d("Fragment Loaded", "Fragment remplacement en cours...");
            FragmentManager fragmentManager = getSupportFragmentManager();

            MemoireFragment  fragment = new MemoireFragment();
            fragmentManager.beginTransaction().replace(R.id.container_fragment_root, fragment).commit();

            Log.d("Fragment Loaded", "Fragment remplace");


        }
    }

    public class FileSearchDataSecond extends AsyncTask<Void, Void, DocumentUser>{

        @Override
        protected DocumentUser doInBackground(Void... voids) {
            //1) Exctrat file name
            String fileNameUri = "";
            FileInputStream fileInputStream = null;

            int pageCount = 0;
            Cursor cursor =  getContentResolver().query(uriFile, null, null, null, null);
            try
            {
                if(cursor != null && cursor.moveToFirst())
                {
                    fileNameUri = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    String arrayStringName[] = fileNameUri.split(".docx|.doc");
                    fileName = arrayStringName[0];
                }
            }finally {
                cursor.close();
            }
            Log.d("File name", "File name is: "+fileName);

            try {
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uriFile, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                fileInputStream = new FileInputStream(fileDescriptor);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            //2) get page number of document
            try {
                Document doc = new Document(fileInputStream);
                pageCount = doc.getPageCount();
                Log.d("PageNumber", "doc page number is: "+pageCount);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //TODO: save information into database
            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();

            DocumentUser documentUser = new DocumentUser();
            documentUser.documentName = fileName;
            documentUser.pageNumber = pageCount;
            documentUser.documentPath = uriFile.toString();

            db.userDao().insertDocument(documentUser);

            //TODO: SELECT ALL DOCUMENT AND SHOW DOCUMENT FRAGMENT

            List<DocumentUser> listDocument = db.userDao().selectAllDocument();
            DocumentUser docUser = null;
            for(int i = 0; i < listDocument.size(); i++)
            {
                if(i == listDocument.size() - 1)
                {
                    docUser = listDocument.get(i);
                }
            }
            return docUser;
        }

        @Override
        protected void onPostExecute(DocumentUser documentUsers) {
            super.onPostExecute(documentUsers);

            MemoireFragment.progressBar.setVisibility(View.GONE);
            MemoireFragment.documentCreating.setVisibility(View.GONE);
            documentUserQuery.add(documentUsers);
            MemoireFragment.documentAdapter.notifyDataSetChanged();



        }
    }

    public class LoadData extends AsyncTask<Void,Void, List<DocumentUser>>{

        @Override
        protected List<DocumentUser> doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(getApplicationContext(),
                    PersonnalizeDatabase.class, "personnalize").build();
            List<DocumentUser> documentUsersListLoaded = db.userDao().selectAllDocument();
            Log.d("MainProcess", "documentUsersListLoaded size: "+documentUsersListLoaded.size());
            return documentUsersListLoaded;
        }

        @Override
        protected void onPostExecute(List<DocumentUser> documentUsers) {
            super.onPostExecute(documentUsers);

            documentUserQuery = documentUsers;

            TabAdapter tabAdapter = new TabAdapter(getSupportFragmentManager());
            tabAdapter.addFragment(new MemoireFragment(), "MEMOIRES");
            tabAdapter.addFragment(new DiscussionFragment(), "DISCUSSION");
            viewPager.setAdapter(tabAdapter);
            tabLayout.setupWithViewPager(viewPager);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

}
