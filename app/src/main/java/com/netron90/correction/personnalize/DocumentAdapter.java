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

import com.netron90.correction.personnalize.Database.DocumentUser;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;

import java.util.Calendar;
import java.util.List;

/**
 * Created by CHRISTIAN on 17/02/2019.
 */

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.ViewHolder> {

    List<DocumentUser> documentUserList;
    private DeleteDocument deleteDocument;
    private int position;
    private Context context;
    private Boolean powerPointFlag = false, miseEnFormeFlag = false;
    public static TextView deliveryDate;
    private FragmentManager fragmentManager = null;

    public DocumentAdapter(List<DocumentUser> documentUser) {
        this.documentUserList = documentUser;
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
    }

    @Override
    public int getItemCount() {
        return documentUserList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView iconDelete;
        private TextView documentName, documentPage;
        ImageView iconEditPowerPoint;
        private Switch powerPointSwitch, miseEnFormeSwitch;
        private RelativeLayout editPowerPoint, editDate, iconSend;



        public ViewHolder(View itemView) {
            super(itemView);

            //position = getLayoutPosition();
            context            = itemView.getContext();
            iconDelete         = (ImageView) itemView.findViewById(R.id.delete_doc);
            documentName       = (TextView) itemView.findViewById(R.id.document_name);
            documentPage       = (TextView) itemView.findViewById(R.id.document_pages);
            powerPointSwitch   = (Switch) itemView.findViewById(R.id.switch_power_point_option);
            miseEnFormeSwitch  = (Switch) itemView.findViewById(R.id.switch_mise_en_forme_option);
            iconEditPowerPoint = (ImageView) itemView.findViewById(R.id.icon_edit_power_point);
            deliveryDate       = (TextView) itemView.findViewById(R.id.text_date);
            editPowerPoint     = (RelativeLayout) itemView.findViewById(R.id.edit_power_point);
            editDate           = (RelativeLayout) itemView.findViewById(R.id.edit_date);
            iconSend           = (RelativeLayout) itemView.findViewById(R.id.send_document);

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
                    dateChooser.show(MainProcess.fragmentManagerDatePicker, "DatePicker");
                }
            });

            editPowerPoint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(powerPointSwitch.isChecked())
                    {
                        Intent powerPointEdit = new Intent(context, PowerPointForm.class);
                        powerPointEdit.putExtra("itemPosition", getLayoutPosition());
                        context.startActivity(powerPointEdit);
                    }
                }
            });

            iconSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    position = getLayoutPosition();
                    Intent intent = new Intent(context, DetailActivity.class);
                    String documentName = documentUserList.get(position).documentName;
                    int documentPage = documentUserList.get(position).pageNumber;
                    Log.d("PageDoc", "Document Page Adapter: "+documentPage);
                    String documentPath = documentUserList.get(position).documentPath;
                    intent.putExtra("document_name", documentName);
                    intent.putExtra("document_page", documentPage);
                    intent.putExtra("document_path", documentPath);
                    context.startActivity(intent);
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
                            deleteDocument.execute();
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

    public class DeleteDocument extends AsyncTask<Void, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(Void... voids) {

            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "scodelux").build();

            //1) Delete one document from database
            db.userDao().deleteOneDocument(documentUserList.get(position).documentName);
            //2)select all document
            documentUserList = db.userDao().selectAllDocument();
            //)remove element from arrayList of User documents
            //documentUserList.remove(position);
            if(documentUserList.size() == 0)
            {

                SharedPreferences.Editor editor = MainProcess.sharedPreferences.edit();
                editor.putBoolean(MainProcess.DOCUMENT_EXIST, false).commit();
                return true;
            }
            else
            {
                //documentUserList.remove(position);
                return false;
            }


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
                    PersonnalizeDatabase.class, "scodelux").build();
            db.userDao().updatePowerPoint(powerPointFlag, integers[0]);
            List<DocumentUser> documentUsers = db.userDao().selectAllDocument();
            Log.d("POWER POINT VALUE", "Edit power Point: " + documentUsers.get(integers[0]).powerPoint);
            return null;
        }
    }

    public class UpdateMiseEnForme extends AsyncTask<Integer, Void, Void>
    {
        @Override
        protected Void doInBackground(Integer... integers) {
            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "scodelux").build();
            db.userDao().updateMiseEnForme(miseEnFormeFlag, integers[0]);
            List<DocumentUser> documentUsers = db.userDao().selectAllDocument();
            Log.d("MISE EN FORME", "Edit mise en forme: " + documentUsers.get(integers[0]).miseEnForme);
            return null;
        }
    }

    public static class SelectDate extends DialogFragment implements DatePickerDialog.OnDateSetListener{

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
            }
            else
            {
                deliveryDate.setText(day + "/" + month + "/" + year);
            }

        }
    }
}
