package com.netron90.correction.personnalize;

/**
 * Created by CHRISTIAN on 30/03/2019.
 */

public class UserMessage {

    public String userTextMessage;
    public String userId;
    public String messageTime;

    public UserMessage() {
    }

    public UserMessage(String userTextMessage, String userId, String messageTime) {
        this.userTextMessage = userTextMessage;
        this.userId = userId;
        this.messageTime = messageTime;
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
}
