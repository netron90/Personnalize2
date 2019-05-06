package com.netron90.correction.personnalize;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netron90.correction.personnalize.Database.UserMessageDb;

import java.util.List;

/**
 * Created by CHRISTIAN on 30/03/2019.
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

    public static List<UserMessageDb> listUserMessage;
    private Context context;
    private SharedPreferences sharedPreferences;

    public ChatMessageAdapter(List<UserMessageDb> listUserMessage) {
        ChatMessageAdapter.listUserMessage = listUserMessage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_model, parent, false);
        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        if(listUserMessage.get(position).getAuthor().equals(ChatActivity.sharedPreferences.getString(MainActivity.USER_NAME, "Inconnu")))
        {
            //String messageAuthor = sharedPreferences.getString(MainActivity.USER_NAME, "Inconnue");
            holder.paramsCard.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            holder.chatLayoutContent.setBackgroundColor(ContextCompat.getColor(context, R.color.chatBackgroundColor));
            holder.chatUserName.setText("Vous");
            holder.chatMessageContent.setText(listUserMessage.get(position).getUserTextMessage());
            holder.chatMessageTime.setText(listUserMessage.get(position).getMessageTime());
        }
        else
        {
            holder.paramsCard.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

            holder.chatLayoutContent.setBackgroundColor(ContextCompat.getColor(context, R.color.colorLigthWhite));
            holder.chatUserName.setText("Team Personnalize");
            holder.chatMessageContent.setText(listUserMessage.get(position).getUserTextMessage());
            holder.chatMessageTime.setText(listUserMessage.get(position).getMessageTime());
        }
    }

    @Override
    public int getItemCount() {
        return listUserMessage.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView chatUserName, chatMessageContent, chatMessageTime;
        private RelativeLayout.LayoutParams paramsCard;
        private CardView chatLayoutContent;

        public ViewHolder(View itemView) {
            super(itemView);

            chatUserName = itemView.findViewById(R.id.chat_user_name);
            chatMessageContent = itemView.findViewById(R.id.chat_content);
            chatMessageTime = itemView.findViewById(R.id.chat_time);
            chatLayoutContent = itemView.findViewById(R.id.cardView);
            paramsCard = (RelativeLayout.LayoutParams) chatLayoutContent.getLayoutParams();
        }
    }
}
