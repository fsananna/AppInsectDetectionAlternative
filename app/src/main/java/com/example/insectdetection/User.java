package com.example.insectdetection;

import androidx.annotation.NonNull;

public class User {
    private String email;
    private String password;
    private String division;
    private String district;
    private String Dob;

    private String userName;
    // Default constructor (required for Firebase)
    public User() {
    }

    // Parameterized constructor
    public User(String userName,String email, String division,String district, String dob ) {
        this.userName= userName;
        this.email = email;
        this.division = division;
        this.district = district;
        this.Dob = dob;

    }



    // Getters and setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String division) {
        this.district = district;
    }

    public String getDob() {
        return Dob;
    }

    public void setDob(String dob) {
        this.Dob = dob;
    }


    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", division='" + division + '\'' +
                ", district='" + district + '\'' +
                ", dob='" + Dob + '\'' +
                '}';
    }


}
