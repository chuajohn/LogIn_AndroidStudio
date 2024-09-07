package com.example.chuajohn_me3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chuajohn_me3.model.UserModel;
import com.example.chuajohn_me3.repository.AuthRepository;
import com.example.chuajohn_me3.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

public class MainActivity extends AppCompatActivity{

    Button logout;
    EditText nameView, ageView, addressView;
    ImageView profileImage;
    TextView name;
    Button selectImageButton, saveButton;
    Uri imageUri;
    String downloadURI;

    AuthRepository authRepository;
    UserRepository userRepository;
    FirebaseFirestore firebaseFirestore;
    StorageReference storageReference;
    FirebaseStorage firebaseStorage;

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if(currentUser == null){
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        imageUri = null;
        downloadURI = null;

        profileImage = findViewById(R.id.profileImage);
        nameView = findViewById(R.id.nameView);
        ageView = findViewById(R.id.ageView);
        addressView = findViewById(R.id.addressView);
        name = findViewById(R.id.name);
        selectImageButton = findViewById(R.id.selectImageButton);
        saveButton = findViewById(R.id.saveButton);
        logout = findViewById(R.id.logoutButton);

        authRepository = AuthRepository.getInstance();
        userRepository = UserRepository.getInstance();

        fetchUserData();

        selectImageButton.setOnClickListener(view -> {
            selectImage();
        });

        saveButton.setOnClickListener(view -> {
            saveUserProfile();
        });

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void fetchUserData() {
        String userId = authRepository.getUserID();
        firebaseFirestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot != null) {
                        UserModel user = documentSnapshot.toObject(UserModel.class);
                        Log.v("ReferenceString", user.getProfileURI());

                        StorageReference downloadProfile = firebaseStorage.getReferenceFromUrl(user.getProfileURI());

                        try{
                            File localFile = File.createTempFile("temp", "jpg");
                            downloadProfile.getFile(localFile)
                                    .addOnSuccessListener(taskSnapshot -> {
                                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                        profileImage.setImageBitmap(bitmap);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("Error", e.toString());
                                    });

                        } catch (Exception e){
                            throw new RuntimeException(e);
                        }

                        if (user != null) {
                            nameView.setText(user.getName());
                            ageView.setText(user.getAge());
                            addressView.setText(user.getAddress());
                            name.setText(user.getName());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching user data", e);
                });
    }

    public void selectImage(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);
        }
    }

    public void saveUserProfile(){
        String userId = authRepository.getUserID();
        String name = nameView.getText().toString();
        String age = ageView.getText().toString();
        String address = addressView.getText().toString();

        UserModel user = new UserModel(userId, name, age, address, downloadURI);

        firebaseFirestore.collection("users").document(userId).set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Firestore", "User profile updated successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error updating user profile", e);
                });

        if(imageUri != null) {
            StorageReference imageStorage = storageReference.child("images/" + userId);
            imageStorage.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        imageStorage.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    downloadURI = uri.toString();
                                    firebaseFirestore.collection("users").document(userId)
                                            .update("profileURI", downloadURI)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("Firestore", "Profile picture updated successfully.");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("Firestore", "Error updating profile picture", e);
                                            });
                                });
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Storage", "Error uploading image", e);
                    });
        }
    }
}
