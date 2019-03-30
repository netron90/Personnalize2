package com.netron90.correction.personnalize.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by CHRISTIAN on 30/03/2019.
 */

@Entity(tableName = "user_message")

public class UserMessageDb {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_text_message")
    public String userTextMessage;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "message_time")
    public String messageTime;

    public UserMessageDb() {
    }
}
