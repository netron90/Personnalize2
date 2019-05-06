package com.netron90.correction.personnalize;

import android.app.Activity;
import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.ActivityChooserView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.netron90.correction.personnalize.Database.DiapositiveFormat;
import com.netron90.correction.personnalize.Database.DocumentUser;
import com.netron90.correction.personnalize.Database.PersonnalizeDatabase;

import java.util.List;

/**
 * Created by CHRISTIAN on 10/03/2019.
 */

public class DiapositiveAdapter extends RecyclerView.Adapter<DiapositiveAdapter.ViewHolder> implements AddPictureDiapoFragment.OnFragmentInteractionListener{

    public static List<DiapositiveFormat> diapositiveFormatsList;
    private Context context;
    public static final int DIAPOSITIVE_BROWSER = 3;
    public static TextView diapoHolderTitle, diapoHolderContent;
    public static int    diapoHolderImage = 0;
    private int layoutPosition = 0;
    private int positionList = 0;
    private ViewHolder viewHolder;

    public DiapositiveAdapter(List<DiapositiveFormat> diapositiveFormats, Context context) {
        diapositiveFormatsList = diapositiveFormats;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diapositive_model, null, false);
        viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        positionList = position;
        holder.diapoTitle.setText(diapositiveFormatsList.get(position).diapoTitle);
        holder.diapoContent.setText(diapositiveFormatsList.get(position).diapoDesc);
        if(diapositiveFormatsList.get(position).nbrImage == 0)
        {
            holder.nbrImageDiapo.setText(R.string.diapo_img);
            //diapoHolderImage = 0;
        }
        else {
            Log.d("SIZE", "Image Path Size in Adapter: " + diapositiveFormatsList.get(position).nbrImage);
            int image = diapositiveFormatsList.get(position).nbrImage;
            holder.nbrImageDiapo.setText(String.valueOf(image) + " " + context.getResources().getString(R.string.nbr_image_diapo));
            //diapoHolderImage = diapositiveFormatsList.get(position).nbrImage;
        }


        holder.diapoContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Cannot call this method while RecyclerView is computing a layout or scrolling
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("TEXT CHANGE", "Le texte a change a la position: "+ positionList + ".\n Le texte est: " + editable.toString());

                diapositiveFormatsList.get(positionList).diapoDesc = editable.toString();
                //notifyItemChanged(positionList);
            }
        });
//        refreshData(position);

    }

    @Override
    public int getItemCount() {
        return diapositiveFormatsList.size();
    }

    @Override
    public void onFragmentInteraction(int i) {
        if(i == 1)
        {
//            Log.d("ADD IMAGE FRAG", "Add image diapositive");
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView deleteDiapo;
        private TextView diapoTitle, diapoContent, nbrImageDiapo;
        private RelativeLayout addNewPictures;



        public ViewHolder(View itemView) {
            super(itemView);

            deleteDiapo   = itemView.findViewById(R.id.delete_diapo);
            diapoTitle    = itemView.findViewById(R.id.diapo_title);
            diapoContent  = itemView.findViewById(R.id.diapo_text_content);
            nbrImageDiapo = itemView.findViewById(R.id.image_diapo);
//            AddPictureDiapoFragment addPictureDiapoFragment = new AddPictureDiapoFragment();
//            MainProcess.fragmentManager.beginTransaction().replace(R.id.add_new_pictures, addPictureDiapoFragment).commit();
            addNewPictures= itemView.findViewById(R.id.add_new_pictures);


            DiapositiveAdapter.diapoHolderTitle = diapoTitle;
            DiapositiveAdapter.diapoHolderContent = diapoContent;





            deleteDiapo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    layoutPosition = getLayoutPosition();
                    DeleteOneDiapo deleteOneDiapo = new DeleteOneDiapo();
                    deleteOneDiapo.execute(layoutPosition);
                }
            });

            addNewPictures.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(context.getClass().equals(PowerPointForm.class))
                    {
                        Intent intent;
                        if(Build.VERSION.SDK_INT < 19)
                        {
                            Log.d("CHOOSE IMAGE", "iS CONTEXT");
                            intent = new Intent(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            PowerPointForm.idDiapositive = diapositiveFormatsList.get(getLayoutPosition()).id;
                            PowerPointForm.diapositivePosition = getLayoutPosition();
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            //Log.d("DiapoId", "DiapoId is: " + diapositiveFormatsList.get(getLayoutPosition()).id);

                            ((PowerPointForm) context).startActivityForResult(Intent.createChooser(intent, "Chose your document"), DIAPOSITIVE_BROWSER);
                        }
                        else
                        {
                            Log.d("CHOOSE IMAGE", "iS CONTEXT");
                            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intent.addCategory(Intent.CATEGORY_OPENABLE);
                            intent.setType("image/*");
                            PowerPointForm.idDiapositive = diapositiveFormatsList.get(getLayoutPosition()).id;
                            PowerPointForm.diapositivePosition = getLayoutPosition();
                            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            //Log.d("DiapoId", "DiapoId is: " + diapositiveFormatsList.get(getLayoutPosition()).id);

                            ((PowerPointForm) context).startActivityForResult(Intent.createChooser(intent, "Chose your document"), DIAPOSITIVE_BROWSER);

                        }


                    }

                }
            });
        }
    }

    public class DeleteOneDiapo extends AsyncTask<Integer, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Integer... integers) {
            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "personnalize").build();

            //TODO: DCHECK IF THERE ARE ONLY ONE ELEMENT IN THE LIST
            if(DiapositiveAdapter.diapositiveFormatsList.size() == 1)
            {
                return false;
            }else
            {
                //TODO: DELETE ONE DIAPOSITIVEFROM DATABASE
                db.userDao().deleteDiapoImagePath(DiapositiveAdapter.diapositiveFormatsList.get(integers[0]).id);
                db.userDao().deleteOneDiapo(DiapositiveAdapter.diapositiveFormatsList.get(integers[0]).id);
                PowerPointForm.diapositiveNumber--;

                //TODO: REMOVE ALL ELEMENT IN THE LIST
                DiapositiveAdapter.diapositiveFormatsList.clear();

                //TODO: RECREATE LIST
                List<DiapositiveFormat> allDiapo = db.userDao().selectDiapos(PowerPointForm.idDocument);
                for(int i = 0; i < allDiapo.size(); i++)
                {
                    DiapositiveFormat diapositiveFormat = new DiapositiveFormat();
                    diapositiveFormat.idDocument = PowerPointForm.idDocument;
                    diapositiveFormat.diapoTitle = "Diapositive " + String.valueOf(i + 1);
                    db.userDao().updateDiapoTitle(diapositiveFormat.diapoTitle, allDiapo.get(i).id);
                    diapositiveFormat.diapoDesc  = allDiapo.get(i).diapoDesc;
                    diapositiveFormat.nbrImage   = allDiapo.get(i).nbrImage;
                    DiapositiveAdapter.diapositiveFormatsList.add(diapositiveFormat);
                }
                return true;
            }


        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            if(aVoid == false)
            {
                Toast.makeText(context, "Impossible de supprimer cet élément.", Toast.LENGTH_SHORT).show();
            }
            else{
                PowerPointForm.diapositiveAdapter.notifyDataSetChanged();
            }

        }
    }

}
