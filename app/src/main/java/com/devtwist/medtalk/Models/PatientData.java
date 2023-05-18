package com.devtwist.medtalk.Models;

public class PatientData {

    private String userId, token, username, gender, profileUrl, province, city, address, phone, email;
    private double longitude, latitude;
    private int badReviews;

    public PatientData() {
    }

    public PatientData(String userId, String token, String username, String gender, String profileUrl, String province, String city, String address, String phone, String email, double longitude, double latitude, int badReviews) {
        this.userId = userId;
        this.token = token;
        this.username = username;
        this.gender = gender;
        this.profileUrl = profileUrl;
        this.province = province;
        this.city = city;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.longitude = longitude;
        this.latitude = latitude;
        this.badReviews = badReviews;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getBadReviews() {
        return badReviews;
    }

    public void setBadReviews(int badReviews) {
        this.badReviews = badReviews;
    }
}
