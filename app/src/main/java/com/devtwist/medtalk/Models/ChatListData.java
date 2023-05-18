package com.devtwist.medtalk.Models;

public class ChatListData {
    private String userId, username, lastMessage, userType, msgType;
    private double timeStamp;

    public ChatListData() {
    }

    public ChatListData(String userId, String username, String lastMessage, String userType, String msgType, double timeStamp) {
        this.userId = userId;
        this.username = username;
        this.lastMessage = lastMessage;
        this.userType = userType;
        this.msgType = msgType;
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public double getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(double timeStamp) {
        this.timeStamp = timeStamp;
    }
}
