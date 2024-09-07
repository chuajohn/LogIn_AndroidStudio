package com.example.chuajohn_me3.repository;

import com.example.chuajohn_me3.model.UserModel;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {

    private static volatile UserRepository instance;

    private FirebaseFirestore dataSource;

    private UserModel currentUserData = null;

    private UserRepository() {
        dataSource = FirebaseFirestore.getInstance();
    }

    public static UserRepository getInstance() {
        UserRepository temp = instance;

        if (temp == null) {
            temp = instance;
            synchronized (UserRepository.class) {
                if (temp == null) {
                    temp = instance = new UserRepository();
                }
            }
        }

        return temp;
    }

    public UserModel getCurrentUserData() {
        return currentUserData;
    }

    public void setCurrentUserData(UserModel currentUserData) {
        this.currentUserData = currentUserData;
    }

    public Task<Void> insertUser(UserModel user) {
        return dataSource.collection("users").document(user.getUserId()).set(user);
    }

    public Task<DocumentSnapshot> getUserDataFromDB(String uid){
        return dataSource.collection("users").document(uid).get();
    }
}
