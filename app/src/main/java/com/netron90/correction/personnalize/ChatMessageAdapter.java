package com.netron90.correction.personnalize;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by CHRISTIAN on 30/03/2019.
 */

public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ViewHolder> {

    public static List<UserMessage> listUserMessage;

    public ChatMessageAdapter(List<UserMessage> listUserMessage) {
        this.listUserMessage = listUserMessage;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_model, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.chatUserName.setText(listUserMessage.get(position).getUserId());
        holder.chatMessageContent.setText(listUserMessage.get(position).getUserTextMessage());
        holder.chatMessageTime.setText(listUserMessage.get(position).getMessageTime());
    }

    @Override
    public int getItemCount() {
        return listUserMessage.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView chatUserName, chatMessageContent, chatMessageTime;

        public ViewHolder(View itemView) {
            super(itemView);

            chatUserName = (TextView) itemView.findViewById(R.id.chat_user_name);
            chatMessageContent = (TextView) itemView.findViewById(R.id.chat_content);
            chatMessageTime = (TextView) itemView.findViewById(R.id.chat_time);
        }
    }
}
