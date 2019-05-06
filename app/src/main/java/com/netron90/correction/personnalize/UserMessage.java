package com.netron90.correction.personnalize;

/**
 * Created by CHRISTIAN on 30/03/2019.
 */

public class UserMessage {

    public String userTextMessage;
    public String userId;
    public String teamId;
    public String messageTime;
    public String author;

    public UserMessage() {
    }

    public UserMessage(String userTextMessage, String userId, String teamId, String messageTime, String author) {
        this.userTextMessage = userTextMessage;
        this.userId = userId;
        this.messageTime = messageTime;
        this.teamId = teamId;
        this.author = author;
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
