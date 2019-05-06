package com.netron90.correction.personnalize;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.netron90.correction.personnalize.Database.DiapositiveFormat;
import com.netron90.correction.personnalize.Database.DocumentUser;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;

import java.util.Calendar;
import java.util.List;

/**
 * Created by CHRISTIAN on 17/02/2019.
 */

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    public static List<DocumentUser> documentUserList;
    private DeleteDocument deleteDocument;
    private int position;
    private final int COMPTEUR = 1;
    private Context context;
    private Boolean powerPointFlag = false, miseEnFormeFlag = false;
    public static TextView deliveryDate;
    private FragmentManager fragmentManager = null;


    public DocumentAdapter(List<DocumentUser> documentUser) {
        documentUserList = documentUser;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_model_activity, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.documentName.setText(documentUserList.get(position).documentName);
        holder.documentPage.setText(documentUserList.get(position).pageNumber + " Pages");
        holder.miseEnFormeSwitch.setChecked(documentUserList.get(position).miseEnForme);
        holder.powerPointSwitch.setChecked(documentUserList.get(position).powerPoint);
        holder.deliveryDates.setText(documentUserList.get(position).deliveryDate);
    }

    @Override
    public int getItemCount() {
        return documentUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconDelete;
        private TextView documentName, documentPage, deliveryDates;
        ImageView iconEditPowerPoint;
        private Switch powerPointSwitch, miseEnFormeSwitch;
        private RelativeLayout editPowerPoint, editDate, iconSend;



        public ViewHolder(View itemView) {
            super(itemView);

            //position = getLayoutPosition();
            context            = itemView.getContext();
            iconDelete         = itemView.findViewById(R.id.delete_doc);
            documentName       = itemView.findViewById(R.id.document_name);
            documentPage       = itemView.findViewById(R.id.document_pages);
            powerPointSwitch   = itemView.findViewById(R.id.switch_power_point_option);
            miseEnFormeSwitch  = itemView.findViewById(R.id.switch_mise_en_forme_option);
            iconEditPowerPoint = itemView.findViewById(R.id.icon_edit_power_point);
            deliveryDates       = itemView.findViewById(R.id.text_date);
            deliveryDate       = itemView.findViewById(R.id.text_date);
            editPowerPoint     = itemView.findViewById(R.id.edit_power_point);
            editDate           = itemView.findViewById(R.id.edit_date);
            iconSend           = itemView.findViewById(R.id.send_document);

            powerPointSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(powerPointSwitch.isChecked())
                    {
                        //TODO: 1) Enable edit power point button
                        iconEditPowerPoint.setImageResource(R.drawable.ic_mode_edit_black_active_24dp);

                        //TODO:2) Set into database powerpoint true
                        powerPointFlag = true;
                        UpdatePowerPoint updatePowerPoint = new UpdatePowerPoint();
                        int position = getLayoutPosition();
                        updatePowerPoint.execute(position);
                    }
                    else
                    {
                        //TODO: 1) Disable edit power point button
                        iconEditPowerPoint.setImageResource(R.drawable.ic_mode_edit_black_24dp);

                        //TODO:2) Set into database powerpoint false
                        powerPointFlag = false;
                        UpdatePowerPoint updatePowerPoint = new UpdatePowerPoint();
                        int position = getLayoutPosition();
                        updatePowerPoint.execute(position);
                    }
                }
            });

            miseEnFormeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(miseEnFormeSwitch.isChecked())
                    {
                        //TODO:1) Set into database mise en forme true
                        miseEnFormeFlag = true;
                        UpdateMiseEnForme updateMiseEnForme = new UpdateMiseEnForme();
                        int position = getLayoutPosition();
                        updateMiseEnForme.execute(position);
                    }
                    else{
                        //TODO:1) Set into database mise en forme false
                        miseEnFormeFlag = false;
                        UpdateMiseEnForme updateMiseEnForme = new UpdateMiseEnForme();
                        int position = getLayoutPosition();
                        updateMiseEnForme.execute(position);
                    }
                }
            });

            editDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    DialogFragment dateChooser = new SelectDate();
                    SelectDate.context = context;
                    SelectDate.docId = documentUserList.get(position).id;
                    dateChooser.show(MainProcess.fragmentManagerDatePicker, "DatePicker");
                }
            });

            editPowerPoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(powerPointSwitch.isChecked())
                    {
                        Intent powerPointEdit = new Intent(context, PowerPointForm.class);
                        Log.d("POWERPOINT FORM", "Position: " + getLayoutPosition());
                        powerPointEdit.putExtra("itemPosition", documentUserList.get(getLayoutPosition()).id);
                        Log.d("DOCUMENT ID", "Document Id selected: " + documentUserList.get(getLayoutPosition()).id);
                        context.startActivity(powerPointEdit);
                    }
                }
            });

            iconSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(deliveryDate.getText().equals(""))
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                        builder.setTitle("Date missed");
                        builder.setMessage("Veuillez selectionner une date.");
                        builder.setPositiveButton("Ok", null);
                        builder.create();
                        builder.show();
                    }
                    else
                    {
                        position = getLayoutPosition();
                        Intent intent = new Intent(context, DetailActivity.class);
                        String documentName = documentUserList.get(position).documentName;
                        Log.d("PageDoc", "Document Name Adapter: "+documentName);
                        int documentPage = documentUserList.get(position).pageNumber;

                        String userEmail    = documentUserList.get(position).emailUser;
                        String userPhone    = documentUserList.get(position).phoneUser;
                        String documentPath = documentUserList.get(position).documentPath;
                        Log.d("PageDoc", "Document Page Adapter: "+documentPage);
                        Log.d("PageDoc", "Chemin du document: "+documentPath);
                        DocumentUser userDoc = new DocumentUser();
                        userDoc.id = documentUserList.get(position).id;
                        userDoc.documentName = documentName;
                        userDoc.pageNumber   = documentPage;
                        userDoc.lastNameUser = documentUserList.get(position).lastNameUser;
                        userDoc.firstNameUSer = documentUserList.get(position).firstNameUSer;
                        userDoc.emailUser     = userEmail;
                        userDoc.phoneUser     = userPhone;
                        userDoc.documentPath  = documentPath;
                        userDoc.miseEnForme   = miseEnFormeSwitch.isChecked();
                        userDoc.powerPoint    = powerPointSwitch.isChecked();
                        userDoc.deliveryDate  = deliveryDates.getText().toString();
                        intent.putExtra("documentInfo", userDoc);
                        intent.putExtra("document_position", position);

                        context.startActivity(intent);
                    }

                }
            });

            iconDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("IconDelete", "icon Delete cliked");
                    position = getLayoutPosition();
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                    alertDialog.setTitle(R.string.dialog_builder_title);
                    alertDialog.setMessage(R.string.dialog_builder_message);
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            deleteDocument = new DeleteDocument();
                            deleteDocument.execute(documentUserList.get(position).id);
                            Log.d("Item Position", "item Position: " + position);
                        }
                    });
                    alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alertDialog.create().show();
                }
            });

        }
    }

    public class DeleteDocument extends AsyncTask<Integer, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Integer... integers) {

            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();

            //1) delete all diapo
            List<DiapositiveFormat>diapositiveFormats = db.userDao().selectDiapos(integers[0]);
            for(int i = 0; i < diapositiveFormats.size(); i++)
            {
                db.userDao().deleteDiapoImagePath(diapositiveFormats.get(i).id);
            }
            db.userDao().deleteAllDiapos(integers[0]);
//            SharedPreferences.Editor editor = MainProcess.sharedPreferences.edit();
//            editor.putBoolean(PowerPointForm.FIRST_INSERTION, false).commit();

//            List<DocumentUser> documentUsers = db.userDao().selectAllDocument();
//            Log.d("DOCUMENT SIZE", "Taille du document supprime: " + documentUsers.size());
//            for(int i = 1; i <= documentUsers.size(); i++)
//            {
//                List<DiapositiveFormat>diapositiveFormats = db.userDao().selectDiapos(i);
//            }

            //2) Delete one document from database
            db.userDao().deleteOneDocument(documentUserList.get(position).documentName);
            documentUserList.clear();
            //3)select all document
            List<DocumentUser> documentSelect = db.userDao().selectAllDocument();

            for(int i = 0; i < documentSelect.size(); i++)
            {
                DocumentUser doc = new DocumentUser();
                doc.id = documentSelect.get(i).id;
                doc.documentName = documentSelect.get(i).documentName;
                doc.pageNumber = documentSelect.get(i).pageNumber;
                doc.lastNameUser = documentSelect.get(i).lastNameUser;
                doc.firstNameUSer = documentSelect.get(i).firstNameUSer;
                doc.emailUser = documentSelect.get(i).emailUser;
                doc.phoneUser = documentSelect.get(i).phoneUser;
                doc.documentPath = documentSelect.get(i).documentPath;
                doc.powerPoint = documentSelect.get(i).powerPoint;
                doc.miseEnForme = documentSelect.get(i).miseEnForme;
                doc.deliveryDate = documentSelect.get(i).deliveryDate;
                documentUserList.add(doc);
            }
            //)remove element from arrayList of User documents
            //documentUserList.remove(position);
            //                editor  = MainProcess.sharedPreferences.edit();
//                editor.putBoolean(MainProcess.DOCUMENT_EXIST, false).commit();
            return documentUserList.size() == 0;


            //return null;
        }

        @Override
        protected void onPostExecute(Boolean fragmentState) {
            super.onPostExecute(fragmentState);
            if(fragmentState == true)
            {
                MemoireFragment.documentAdapter.notifyDataSetChanged();
                //Create a empty fragment
                RootFragment rootFragment = new RootFragment();
                //MemoireFragmentEmpty memoireFragmentEmpty = new MemoireFragmentEmpty();
                MainProcess.fragmentManager.beginTransaction().replace(R.id.memoire_fragment_container, rootFragment).commit();
            }
            else{
                MemoireFragment.documentAdapter.notifyDataSetChanged();
            }

        }
    }

    public class UpdatePowerPoint extends AsyncTask<Integer, Void, Void>
    {

        @Override
        protected Void doInBackground(Integer... integers) {
            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();

            db.userDao().updatePowerPoint(powerPointFlag, documentUserList.get(integers[0]).id);
            List<DocumentUser> documentUsers = db.userDao().selectAllDocument();
            Log.d("POWER POINT VALUE", "Edit power PointEdit power Point: " + documentUsers.get(integers[0]).powerPoint + " powerPoin Flag: "+ powerPointFlag + " Position: "+ integers[0]);
            //position = 0;
            return null;
        }
    }

    public class UpdateMiseEnForme extends AsyncTask<Integer, Void, Void>
    {
        @Override
        protected Void doInBackground(Integer... integers) {
            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();
            db.userDao().updateMiseEnForme(miseEnFormeFlag, documentUserList.get(integers[0]).id);
            List<DocumentUser> documentUsers = db.userDao().selectAllDocument();
            Log.d("MISE EN FORME", "Edit mise en forme: " + documentUsers.get(integers[0]).miseEnForme);

            return null;
        }
    }

    public static class SelectDate extends DialogFragment implements DatePickerDialog.OnDateSetListener{

        private String deliveryDateText = "";
        public static Context context;
        public static int docId;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {


            if(month >= 1 && month < 10)
            {
                deliveryDate.setText(day + "/" + "0"+month + "/" + year);
                deliveryDateText = deliveryDate.getText().toString();
                UpdateDocumentDate updateDocumentDate = new UpdateDocumentDate();
                updateDocumentDate.execute();
            }
            else
            {
                deliveryDate.setText(day + "/" + month + "/" + year);
                deliveryDateText = deliveryDate.getText().toString();
                UpdateDocumentDate updateDocumentDate = new UpdateDocumentDate();
                updateDocumentDate.execute();
            }

        }

        public class UpdateDocumentDate extends AsyncTask<Void, Void, Void>
        {
            @Override
            protected Void doInBackground(Void... voids) {
                final PersonnalizeDatabase db = Room.databaseBuilder(context,
                        PersonnalizeDatabase.class, "personnalize").build();

                db.userDao().updateDocumentDate(deliveryDateText, docId);
                return null;
            }
        }
    }


}
