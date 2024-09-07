package com.example.chuajohn_me3;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chuajohn_me3.model.UserModel;
import com.example.chuajohn_me3.repository.AuthRepository;
import com.example.chuajohn_me3.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class RegisterActivity extends AppCompatActivity{

    AuthRepository authRepository;
    UserRepository userRepository;

    TextView toLoginButton;
    EditText email, password, name, address, age;
    TextView emailErrorTxt, emailErrorTxt2, passwordErrorTxt, nameErrorTxt, addressErrorTxt, ageErrorTxt;

    Button registerButton;
    Button selectImageButton;
    ImageView profileImage;
    Uri imageUri;
    String downloadURI;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    FirebaseFirestore firebaseFirestore;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        imageUri = null;
        downloadURI = null;

        profileImage = findViewById(R.id.imageView);
        selectImageButton = findViewById(R.id.selectImageButton);

        authRepository = AuthRepository.getInstance();
        userRepository = UserRepository.getInstance();
        toLoginButton = findViewById(R.id.toLoginButton);
        email = findViewById(R.id.emailInput);
        password = findViewById(R.id.passwordInput);
        name = findViewById(R.id.nameInput);
        address = findViewById(R.id.addressInput);
        age = findViewById(R.id.ageInput);
        registerButton = findViewById(R.id.registerButton);
        emailErrorTxt = findViewById(R.id.emailError);
        passwordErrorTxt = findViewById(R.id.passwordError);
        nameErrorTxt = findViewById(R.id.nameError);
        addressErrorTxt = findViewById(R.id.addressError);
        ageErrorTxt = findViewById(R.id.ageError);
        emailErrorTxt2 = findViewById(R.id.emailError2);
        progressBar = findViewById(R.id.progressBar);

        selectImageButton.setOnClickListener(view -> selectImage());

        toLoginButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        registerButton.setOnClickListener(v -> {
            String emailInput = email.getText().toString();
            String passwordInput = password.getText().toString();
            String nameInput = name.getText().toString();
            String addressInput = address.getText().toString();
            String ageInput = age.getText().toString();

            boolean emailValid = true, passwordValid = true, nameValid = true, addressValid = true, ageValid = true;

            if (emailInput.isEmpty()) {
                emailErrorTxt.setText("Email is required");
                emailValid = false;
            } else {
                emailErrorTxt.setText("");
                if (!Patterns.EMAIL_ADDRESS.matcher(emailInput).matches()) {
                    emailErrorTxt.setText("Email format is incorrect!");
                    emailValid = false;
                } else {
                    emailErrorTxt.setText("");
                }
            }

            if (passwordInput.isEmpty()) {
                passwordErrorTxt.setText("Password is required");
                passwordValid = false;
            } else {
                passwordErrorTxt.setText("");
            }

            if (nameInput.isEmpty()) {
                nameErrorTxt.setText("Name is required");
                nameValid = false;
            } else {
                nameErrorTxt.setText("");
            }

            if (addressInput.isEmpty()) {
                addressErrorTxt.setText("Address is required");
                addressValid = false;
            } else {
                addressErrorTxt.setText("");
            }

            if (ageInput.isEmpty()) {
                ageErrorTxt.setText("Age is required");
                ageValid = false;
            } else {
                ageErrorTxt.setText("");
            }

            if (emailValid && passwordValid && nameValid && addressValid && ageValid) {
                progressBar.setVisibility(View.VISIBLE);
                authRepository.registerUser(emailInput, passwordInput)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                if (imageUri != null) {
                                    StorageReference imageStorage = storageReference.child("images/" + authRepository.getUserID());
                                    imageStorage.putFile(imageUri)
                                            .addOnSuccessListener(taskSnapshot -> {
                                                imageStorage.getDownloadUrl()
                                                        .addOnSuccessListener(uri -> {
                                                            downloadURI = uri.toString();
                                                            Log.v("URI", downloadURI);

                                                            UserModel user = new UserModel(authRepository.getUserID(), nameInput, addressInput, ageInput, downloadURI);
                                                            final String accountID = UUID.randomUUID().toString();

                                                            firebaseFirestore.collection("users")
                                                                    .document(authRepository.getUserID())
                                                                    .set(user)
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        Log.v("INSERT", "Insert Success");
                                                                        userRepository.insertUser(user)
                                                                                .addOnSuccessListener(aVoid2 -> {
                                                                                    Log.v("Test", "Insert Success");
                                                                                    FirebaseAuth.getInstance().signOut();
                                                                                    progressBar.setVisibility(View.GONE);
                                                                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                                                    startActivity(intent);
                                                                                    finish();
                                                                                })
                                                                                .addOnFailureListener(e -> {
                                                                                    Log.v("Test", "Insert Failed");
                                                                                    progressBar.setVisibility(View.GONE);
                                                                                });
                                                                    })
                                                                    .addOnFailureListener(e -> Log.v("ERROR", e.getMessage()));
                                                        })
                                                        .addOnFailureListener(e -> Log.v("ERROR", e.getMessage()));
                                            })
                                            .addOnFailureListener(e -> Log.v("ERROR", e.getMessage()));
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                    //return;
                                }
                            } else {
                                progressBar.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(e -> {
                            if (e instanceof FirebaseAuthUserCollisionException) {
                                emailErrorTxt2.setText("Email has already been used.");
                                progressBar.setVisibility(View.GONE);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Log.v("RegisterError", e.getMessage());
                            }
                        });
            }
        });
    }

    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }
}
