package com.devtwist.medtalk.Models;

public class MessageData {

    private String messageId, messageText, senderId, fileUrl, receiverRoom, time, date, type;
    private long timestamp;
    private boolean isSeen;

    public MessageData() {

    }

    public MessageData(String messageId, String messageText, String senderId, String fileUrl, String receiverRoom, String time, String date, long timestamp, boolean isSeen, String type) {
        this.messageId = messageId;
        this.messageText = messageText;
        this.senderId = senderId;
        this.fileUrl = fileUrl;
        this.receiverRoom = receiverRoom;
        this.time = time;
        this.date = date;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
        this.type = type;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getReceiverRoom() {
        return receiverRoom;
    }

    public void setReceiverRoom(String receiverRoom) {
        this.receiverRoom = receiverRoom;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
