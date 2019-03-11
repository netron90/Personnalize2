package com.netron90.correction.personnalize;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netron90.correction.personnalize.Database.DiapositiveFormat;
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

    public DiapositiveAdapter(List<DiapositiveFormat> diapositiveFormats, Context context) {
        this.diapositiveFormatsList = diapositiveFormats;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.diapositive_model, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( ViewHolder holder, int position) {

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
    }

    @Override
    public int getItemCount() {
        return diapositiveFormatsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView deleteDiapo;
        private TextView diapoTitle, diapoContent, nbrImageDiapo;
        private RelativeLayout addNewPictures;

        public ViewHolder(View itemView) {
            super(itemView);

            deleteDiapo   = (ImageView)itemView.findViewById(R.id.delete_diapo);
            diapoTitle    = (TextView)itemView.findViewById(R.id.diapo_title);
            diapoContent  = (TextView) itemView.findViewById(R.id.diapo_text_content);
            nbrImageDiapo = (TextView) itemView.findViewById(R.id.image_diapo);
            addNewPictures= (RelativeLayout) itemView.findViewById(R.id.add_new_pictures);

            DiapositiveAdapter.diapoHolderTitle = diapoTitle;
            DiapositiveAdapter.diapoHolderContent = diapoContent;



            deleteDiapo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }


}
