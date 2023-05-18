package com.devtwist.medtalk.Models;

public class StudentData {

    private String userId, token, username, gender, hospital, biography, education, extraQualification, certifications,
            profileUrl, category, city, address, phone, email;
    private double longitude, latitude;
    private int badReviews;

    public StudentData() {
    }

    public StudentData(String userId, String token, String username, String gender, String hospital, String biography, String education, String extraQualification, String certifications, String profileUrl, String category, String city, String address, String phone, String email, double longitude, double latitude, int badReviews) {
        this.userId = userId;
        this.token = token;
        this.username = username;
        this.gender = gender;
        this.hospital = hospital;
        this.biography = biography;
        this.education = education;
        this.extraQualification = extraQualification;
        this.certifications = certifications;
        this.profileUrl = profileUrl;
        this.category = category;
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

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public String getExtraQualification() {
        return extraQualification;
    }

    public void setExtraQualification(String extraQualification) {
        this.extraQualification = extraQualification;
    }

    public String getCertifications() {
        return certifications;
    }

    public void setCertifications(String certifications) {
        this.certifications = certifications;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
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
