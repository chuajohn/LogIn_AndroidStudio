package com.example.chuajohn_me3.model;

public class UserModel {
    String userId;
    String name;
    String address;
    String age;
    String profileURI;

    public UserModel() {}

    public UserModel(String userId, String name, String address, String age, String profileURI) {
        this.userId = userId;
        this.name = name;
        this.address = address;
        this.age = age;
        this.profileURI = profileURI;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileURI() {
        return profileURI;
    }
    public void setProfileURI(String profileURI) {
        this.profileURI = profileURI;
    }
}
