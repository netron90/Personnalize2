package com.netron90.correction.personnalize;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.netron90.correction.personnalize.Database.DocumentAvailable;

import java.util.List;

/**
 * Created by CHRISTIAN on 23/03/2019.
 */

public class DiscussionDocAvailableAdapter extends RecyclerView.Adapter<DiscussionDocAvailableAdapter.ViewHolder> {

    public static List<DocumentAvailable> listDocAvailable;
    private Context context;

    public DiscussionDocAvailableAdapter(List<DocumentAvailable> docAvailable) {
        listDocAvailable = docAvailable;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.document_available_model, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.documentName.setText(listDocAvailable.get(position).documentName);
        holder.documentPage.setText(String.valueOf(listDocAvailable.get(position).pageNumber) + " Pages");
        holder.deliveryDates.setText(listDocAvailable.get(position).deliveryDate);
        if(listDocAvailable.get(position).miseEnForme == true)
        {
            holder.miseEnFormeSwitch.setText("Oui");
        }
        else{
            holder.miseEnFormeSwitch.setText("Non");
        }
        if(listDocAvailable.get(position).powerPoint == true)
        {
            holder.powerPointSwitch.setText("Oui");
        }
        else{
            holder.powerPointSwitch.setText("Non");
        }

        if(listDocAvailable.get(position).docEnd == false)
        {
            holder.iconDocReady.setImageResource(R.drawable.ic_done_doc_ready_off_24dp);
        }
        else
        {
            holder.iconDocReady.setImageResource(R.drawable.ic_done_doc_ready_on_24dp);
        }

        if(listDocAvailable.get(position).documentPaid == false)
        {
            holder.iconDocPaid.setImageResource(R.drawable.ic_attach_money_off_24dp);
        }
        else
        {
            holder.iconDocPaid.setImageResource(R.drawable.ic_attach_money_on_24dp);
        }

        if(listDocAvailable.get(position).documentPaid && listDocAvailable.get(position).docEnd)
        {
            holder.icon_chat.setImageResource(R.drawable.ic_chat_on_24dp);
        }
        else
        {
            holder.icon_chat.setImageResource(R.drawable.ic_chat_off_24dp);
        }

    }

    @Override
    public int getItemCount()
    {
        return listDocAvailable.size();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView documentName, documentPage, deliveryDates, powerPointSwitch, miseEnFormeSwitch;
        ImageView iconDocReady, iconDocPaid, icon_chat;

        private RelativeLayout docReady, docPaidLayout, startDiscussion, deleteBlock;

        public ViewHolder(View itemView) {
            super(itemView);

            context            = itemView.getContext();
            documentName       = itemView.findViewById(R.id.document_name);
            documentPage       = itemView.findViewById(R.id.document_pages);
            powerPointSwitch   = itemView.findViewById(R.id.switch_power_point_option);
            miseEnFormeSwitch  = itemView.findViewById(R.id.switch_mise_en_forme_option);
            deliveryDates      = itemView.findViewById(R.id.text_date);

            iconDocReady       = itemView.findViewById(R.id.icon_doc_ready);
            iconDocPaid        = itemView.findViewById(R.id.icon_doc_paid);
            icon_chat          = itemView.findViewById(R.id.icon_chat);
            docReady           = itemView.findViewById(R.id.doc_ready_layout);
            docPaidLayout      = itemView.findViewById(R.id.doc_paid_layout);

            deleteBlock        = itemView.findViewById(R.id.block_delete_doc);

            startDiscussion    = itemView.findViewById(R.id.start_discussion);

            docReady.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listDocAvailable.get(getLayoutPosition()).docEnd == false)
                    {
                        Toast.makeText(context, "discussion_doc_not_ready", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(context, "discussion_doc_ready", Toast.LENGTH_SHORT).show();
                        //iconDocPaid.setImageResource(R.drawable.ic_done_doc_ready_on_24dp);
                    }
                }
            });

            docPaidLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(listDocAvailable.get(getLayoutPosition()).documentPaid == false)
                    {
                        Toast.makeText(context, R.string.discussion_doc_not_paid, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(context, R.string.discussion_doc_paid, Toast.LENGTH_SHORT).show();
                        iconDocPaid.setImageResource(R.drawable.ic_attach_money_on_24dp);
                    }
                }
            });

            startDiscussion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if((listDocAvailable.get(getLayoutPosition()).documentPaid == false &&
                            listDocAvailable.get(getLayoutPosition()).docEnd== false) ||
                            (listDocAvailable.get(getLayoutPosition()).documentPaid == false &&
                                    listDocAvailable.get(getLayoutPosition()).docEnd== true) )
                    {

                        Toast.makeText(context, R.string.discussion_send_doc_paid_ready_false, Toast.LENGTH_SHORT).show();
                    }
                    else if(listDocAvailable.get(getLayoutPosition()).documentPaid == true &&
                            listDocAvailable.get(getLayoutPosition()).docEnd== false)
                    {
                        Toast.makeText(context, R.string.discussion_send_doc_paid_true_ready_false, Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        //Toast.makeText(context, "PARTIE CHAT DISPONIBLE", Toast.LENGTH_SHORT).show();
                        Intent messageIntent = new Intent(context, ChatActivity.class);
                        messageIntent.putExtra("teamId", listDocAvailable.get(getLayoutPosition()).teamId);
                        context.startActivity(messageIntent);
                    }
                }
            });
        }
    }
}
