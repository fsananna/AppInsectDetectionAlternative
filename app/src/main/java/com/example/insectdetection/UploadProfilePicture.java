package com.example.insectdetection;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class UploadProfilePicture extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;
    private ProgressBar progressBar;
    private ImageView imageViewUploadPic;
    private StorageReference storageReference;
    private Uri imageUri;
    String userId ;
    FirebaseDatabase db = FirebaseDatabase.getInstance("https://insectdetection-c56d4-default-rtdb.asia-southeast1.firebasedatabase.app/") ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_picture);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Upload Profile Picture");
        }

        Button buttonUploadPicChoose = findViewById(R.id.upload_pic_choose_button);
        Button buttonUploadPic = findViewById(R.id.upload_pic_button);
        progressBar = findViewById(R.id.progessBar);
        imageViewUploadPic = findViewById(R.id.imageView_profile_pic);
        storageReference = FirebaseStorage.getInstance().getReference().child("DisplayPics");
        userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        buttonUploadPicChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });
        buttonUploadPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageUri != null) {
                    saveImageStorage(imageUri);
                } else {
                    Toast.makeText(UploadProfilePicture.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGES_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imageViewUploadPic.setImageURI(imageUri);
        }
    }



    private void saveImageStorage(Uri imageUri) {
        // Show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        storageReference.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                Toast.makeText(UploadProfilePicture.this, "Image uploaded", Toast.LENGTH_SHORT).show();
                String imageUrl = uri.toString();

                updateProfileURL(userId,imageUrl);

                // Hide the progress bar
                progressBar.setVisibility(View.GONE);

                // Navigate to ProfileActivity
                Intent intent = new Intent(UploadProfilePicture.this, profileFragment.class);
                startActivity(intent);
                finish(); // Finish the current activity to prevent going back to it on back press
            }).addOnFailureListener(e -> {
                Toast.makeText(UploadProfilePicture.this, "Failed to get image URL", Toast.LENGTH_SHORT).show();
                // Hide the progress bar
                progressBar.setVisibility(View.GONE);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(UploadProfilePicture.this, "Image uploading failed!", Toast.LENGTH_SHORT).show();
            // Hide the progress bar
            progressBar.setVisibility(View.GONE);
        });
    }





    private void updateProfileURL(String userId, String newProfileURL) {

        db.getReference("users")
                .child(userId).child("userProfileImage").setValue(newProfileURL)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Profile URL updated successfully
                        Log.d("Firebase", "Profile URL updated successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to update profile URL
                        Log.e("Firebase", "Failed to update profile URL: " + e.getMessage());
                    }
                });
    }
}



