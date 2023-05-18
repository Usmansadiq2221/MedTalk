package com.devtwist.medtalk.Models;

public class SavedDocData {

    private String docId, userId;
    private double timestamp;

    public SavedDocData() {
    }

    public SavedDocData(String docId, String userId, double timestamp) {
        this.docId = docId;
        this.userId = userId;
        this.timestamp = timestamp;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

}
