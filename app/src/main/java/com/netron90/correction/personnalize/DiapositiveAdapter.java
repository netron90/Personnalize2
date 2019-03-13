package com.netron90.correction.personnalize;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
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

public class DiapositiveAdapter extends RecyclerView.Adapter<DiapositiveAdapter.ViewHolder> {

    public static List<DiapositiveFormat> diapositiveFormatsList;
    private Context context;
    public static TextView diapoHolderTitle, diapoHolderContent;
    public static int    diapoHolderImage = 0;
    private int layoutPosition = 0;
    private ViewHolder viewHolder;

    public DiapositiveAdapter(List<DiapositiveFormat> diapositiveFormats, Context context) {
        this.diapositiveFormatsList = diapositiveFormats;
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

        holder.diapoTitle.setText(diapositiveFormatsList.get(position).diapoTitle);
        holder.diapoContent.setText(diapositiveFormatsList.get(position).diapoDesc);
        if(diapositiveFormatsList.get(position).nbrImage == 0)
        {
            holder.nbrImageDiapo.setText(R.string.diapo_img);
            //diapoHolderImage = 0;
        }
        else {
            holder.nbrImageDiapo.setText(diapositiveFormatsList.get(position).nbrImage + " " + R.string.nbr_image_diapo);
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
                diapositiveFormatsList.get(position).diapoDesc = editable.toString();
            }
        });
//        refreshData(position);

    }

    @Override
    public int getItemCount() {
        return diapositiveFormatsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView deleteDiapo;
        private TextView diapoTitle, diapoContent, nbrImageDiapo;
        private RelativeLayout addNewPictures;

        public ImageView getDeleteDiapo() {
            return deleteDiapo;
        }

        public TextView getDiapoTitle() {
            return diapoTitle;
        }

        public TextView getDiapoContent() {
            return diapoContent;
        }

        public TextView getNbrImageDiapo() {
            return nbrImageDiapo;
        }

        public RelativeLayout getAddNewPictures() {
            return addNewPictures;
        }

        public ViewHolder(View itemView) {
            super(itemView);

            deleteDiapo   = (ImageView)itemView.findViewById(R.id.delete_diapo);
            diapoTitle    = (TextView)itemView.findViewById(R.id.diapo_title);
            diapoContent  = (TextView) itemView.findViewById(R.id.diapo_text_content);
            nbrImageDiapo = (TextView) itemView.findViewById(R.id.image_diapo);
            addNewPictures= (RelativeLayout) itemView.findViewById(R.id.add_new_pictures);


            DiapositiveAdapter.diapoHolderTitle = diapoTitle;
            DiapositiveAdapter.diapoHolderContent = diapoContent;



//            diapoContent.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable editable) {
//                    DiapositiveAdapter.this.notifyItemChanged(getLayoutPosition());
//                }
//            });


            deleteDiapo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    layoutPosition = getLayoutPosition();
                    DeleteOneDiapo deleteOneDiapo = new DeleteOneDiapo();
                    deleteOneDiapo.execute(layoutPosition);
                }
            });
        }
    }

    public class DeleteOneDiapo extends AsyncTask<Integer, Void, Boolean>
    {
        @Override
        protected Boolean doInBackground(Integer... integers) {
            final PersonnalizeDatabase db = Room.databaseBuilder(context,
                    PersonnalizeDatabase.class, "scodelux").build();

            //TODO: DCHECK IF THERE ARE ONLY ONE ELEMENT IN THE LIST
            if(DiapositiveAdapter.diapositiveFormatsList.size() == 1)
            {
                return false;
            }else
            {
                //TODO: DELETE ONE DIAPOSITIVEFROM DATABASE
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
                    diapositiveFormat.diapoTitle = "Diapositive " + String.valueOf(PowerPointForm.diapositiveNumber);
                    diapositiveFormat.diapoDesc  = allDiapo.get(i).diapoDesc;
                    diapositiveFormat.nbrImage   = allDiapo.get(i).nbrImage;
                    DiapositiveAdapter.diapositiveFormatsList.add(diapositiveFormat);
                }
                return true;
            }



//
//            int idDiapositive = diapositiveFormatsList.get(integers[0]).id;
////            for(int i = 0; i < diapositiveFormatsList.size(); i++)
////            {
////                Log.d("DELETE DIAPO", "ID item selected: " + diapositiveFormatsList.get(integers[0]).id);
////            }
//            if(diapositiveFormatsList.size() == 1)
//            {
//                return false;
//            }
//            else {
//                db.userDao().deleteOneDiapo(diapositiveFormatsList.get(integers[0]).id);
//
//                PowerPointForm.diapositiveNumber = PowerPointForm.diapositiveNumber - 1;
//                diapositiveFormatsList.clear();
//
//                List<DiapositiveFormat> allDiapo = db.userDao().selectDiapos(PowerPointForm.documentUser.id);
//                for(int i = 0; i < allDiapo.size(); i++)
//                {
//                    // Log.d("DELETE DIAPO", "ID item selected: " + diapositiveFormatsList.get(integers[0]).id);
//                    DiapositiveFormat diapositiveFormat = new DiapositiveFormat();
//                    diapositiveFormat.id = allDiapo.get(i).id;
//                    diapositiveFormat.diapoTitle = "Diapositive " + String.valueOf(i+1);
//                    db.userDao().updateDiapoTitle(diapositiveFormat.diapoTitle, idDiapositive);
//                    diapositiveFormat.diapoDesc = allDiapo.get(i).diapoDesc;
//                    diapositiveFormat.nbrImage = allDiapo.get(i).nbrImage;
//                    diapositiveFormatsList.add(diapositiveFormat);
//                }
//                return true;
//            }
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

    public void refreshData(int position)
    {
        notifyItemChanged(position);
    }

    public ViewHolder getViewHolder() {
        return viewHolder;
    }
}
