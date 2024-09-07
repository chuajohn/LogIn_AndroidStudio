package com.example.chuajohn_me3.repository;

import android.content.Intent;

import com.example.chuajohn_me3.LoginActivity;
import com.example.chuajohn_me3.MainActivity;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class AuthRepository {

    private static volatile AuthRepository instance;

    private FirebaseAuth dataSource;

    private AuthRepository() {
        dataSource = FirebaseAuth.getInstance();
    }

    public static AuthRepository getInstance() {
        AuthRepository temp = instance;

        if(temp == null){
            temp = instance;
            synchronized (AuthRepository.class) {
                if (temp == null) {
                    temp = instance = new AuthRepository();
                }
            }
        }

        return temp;

    }

    public Task<AuthResult> login(String email, String password){
        return dataSource.signInWithEmailAndPassword(email, password);

    }

    public FirebaseUser getCurrentUser(){
        return dataSource.getCurrentUser();
    }

    public Task<AuthResult> registerUser(String email, String password){
        return dataSource.createUserWithEmailAndPassword(email, password);
    }

    public void logout(){
        dataSource.signOut();
    }

    public String getUserID(){
        return Objects.requireNonNull(dataSource.getCurrentUser()).getUid();
    }
}
