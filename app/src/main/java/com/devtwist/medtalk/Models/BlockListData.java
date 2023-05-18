package com.devtwist.medtalk.Models;

public class BlockListData {
    private String userType, userId;

    public BlockListData() {
    }

    public BlockListData(String userType, String userId) {
        this.userType = userType;
        this.userId = userId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
