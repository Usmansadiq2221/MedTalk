package com.devtwist.medtalk.Models;

public class AppointmentListData {

    String userId, appointId, clientType, appointStatus;
    double timestamp;

    public AppointmentListData() {
    }

    public AppointmentListData(String userId, String appointId, String clientType, String appointStatus, double timestamp) {
        this.userId = userId;
        this.appointId = appointId;
        this.clientType = clientType;
        this.appointStatus = appointStatus;
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAppointId() {
        return appointId;
    }

    public void setAppointId(String appointId) {
        this.appointId = appointId;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getAppointStatus() {
        return appointStatus;
    }

    public void setAppointStatus(String appointStatus) {
        this.appointStatus = appointStatus;
    }

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }
}
