package com.netron90.correction.personnalize.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by CHRISTIAN on 30/03/2019.
 */

@Entity(tableName = "user_message_db")
public class UserMessageDb {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_text_message")
    public String userTextMessage;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "team_id")
    public String teamId;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "message_time")
    public String messageTime;

    public UserMessageDb() {
    }

    public int getId() {
        return id;
    }

    public String getUserTextMessage() {
        return userTextMessage;
    }

    public String getUserId() {
        return userId;
    }

    public String getMessageTime() {
        return messageTime;
    }

    public String getTeamId() {
        return teamId;
    }

    public String getAuthor() {
        return author;
    }
}
