package com.devtwist.medtalk.Models;

public class FeedbackData {

    private String feedbackId, userId,  feedbackMsg, clientType;
    private double rating, timestamp;

    public FeedbackData() {
    }

    public FeedbackData(String feedbackId, String userId, String feedbackMsg, String clientType, double rating, double timestamp) {
        this.feedbackId = feedbackId;
        this.userId = userId;
        this.feedbackMsg = feedbackMsg;
        this.clientType = clientType;
        this.rating = rating;
        this.timestamp = timestamp;
    }

    public String getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(String feedbackId) {
        this.feedbackId = feedbackId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFeedbackMsg() {
        return feedbackMsg;
    }

    public void setFeedbackMsg(String feedbackMsg) {
        this.feedbackMsg = feedbackMsg;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }
}
