package com.devtwist.medtalk.Models;

public class DoctorData {

    private String userId, token, username, gender, hospital, education, extraQualification, certifications, category, biography,
                experience, sessionPrice, sessionTime,  profileUrl, phone, email, province, city, address;


    private double accBalance, rating, longitude, latitude, timestamp;
    private int patients, badReviews;

    public DoctorData() {

    }

    public DoctorData(String userId, String token, String username, String gender, String hospital, String education, String extraQualification, String certifications, String category, String biography, String experience, String sessionPrice, String sessionTime, String profileUrl, String phone, String email, String province, String city, String address, double accBalance, double rating, double longitude, double latitude, double timestamp, int patients, int badReviews) {
        this.userId = userId;
        this.token = token;
        this.username = username;
        this.gender = gender;
        this.hospital = hospital;
        this.education = education;
        this.extraQualification = extraQualification;
        this.certifications = certifications;
        this.category = category;
        this.biography = biography;
        this.experience = experience;
        this.sessionPrice = sessionPrice;
        this.sessionTime = sessionTime;
        this.profileUrl = profileUrl;
        this.phone = phone;
        this.email = email;
        this.province = province;
        this.city = city;
        this.address = address;
        this.accBalance = accBalance;
        this.rating = rating;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.patients = patients;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getSessionPrice() {
        return sessionPrice;
    }

    public void setSessionPrice(String sessionPrice) {
        this.sessionPrice = sessionPrice;
    }

    public String getSessionTime() {
        return sessionTime;
    }

    public void setSessionTime(String sessionTime) {
        this.sessionTime = sessionTime;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
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

    public double getAccBalance() {
        return accBalance;
    }

    public void setAccBalance(double accBalance) {
        this.accBalance = accBalance;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
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

    public double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(double timestamp) {
        this.timestamp = timestamp;
    }

    public int getPatients() {
        return patients;
    }

    public void setPatients(int patients) {
        this.patients = patients;
    }
}
