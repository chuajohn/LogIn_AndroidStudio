package com.example.chuajohn_me3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.chuajohn_me3.model.UserModel;
import com.example.chuajohn_me3.repository.AuthRepository;
import com.example.chuajohn_me3.repository.UserRepository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    TextView toRegisterButton;
    EditText emailInput, passwordInput;
    TextView emailErrorTxt, passwordErrorTxt;

    Button loginButton;
    //FirebaseAuth mAuth;

    AuthRepository authRepository;
    UserRepository userRepository;

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if(currentUser != null){
            userRepository.getUserDataFromDB(authRepository.getUserID())
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot != null) {
                            UserModel user = documentSnapshot.toObject(UserModel.class);
                            userRepository.setCurrentUserData(user);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("ERROR", "USER DATA RETRIEVAL FAILED");
                    });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        //mAuth = FirebaseAuth.getInstance();
        authRepository = AuthRepository.getInstance();
        userRepository = UserRepository.getInstance();

        toRegisterButton = findViewById(R.id.toRegisterBtn);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginBtn);
        emailErrorTxt = findViewById(R.id.emailError);
        passwordErrorTxt = findViewById(R.id.passwordError);

        toRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = String.valueOf(emailInput.getText());
                String password = String.valueOf(passwordInput.getText());
                boolean emailValid = true, passwordValid = true;

                if (email.isEmpty()) {
                    emailErrorTxt.setText("Email is required");
                    emailValid = false;
                } else {
                    emailErrorTxt.setText("");
                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        emailErrorTxt.setText("Email format is incorrect!");
                        emailValid = false;
                    } else {
                        emailErrorTxt.setText("");
                    }
                }

                if (password.isEmpty()) {
                    passwordErrorTxt.setText("Password is required");
                    passwordValid = false;
                } else {
                    passwordErrorTxt.setText("");
                }

                if (emailValid && passwordValid) {
                    authRepository.login(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    userRepository.getUserDataFromDB(authRepository.getUserID())
                                            .addOnSuccessListener(documentSnapshot -> {
                                                if (documentSnapshot != null) {
                                                    UserModel user = documentSnapshot.toObject(UserModel.class);
                                                    userRepository.setCurrentUserData(user);
                                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("ERROR", "USER DATA RETRIEVAL FAILED");
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                if(e instanceof FirebaseAuthInvalidCredentialsException){
                                    passwordErrorTxt.setText("Invalid Credentials Provided");
                                }
                            });
                }
            }
        });
    }
}