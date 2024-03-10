package com.example.insectdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements View.OnClickListener {

    EditText editTextEmail, editTextPassword, editTextCountry ,editTextUserName;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    FirebaseUser currentUser;
    FirebaseDatabase db;

    private TextView registerDate;
    private DatePickerDialog datePickerDialog;

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://insectdetection-c56d4-default-rtdb.asia-southeast1.firebasedatabase.app/");

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextUserName = findViewById(R.id.userName);
        editTextCountry = findViewById(R.id.inputTV);
        buttonReg = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        registerDate = findViewById(R.id.idBtnPickDate);

        textView.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        registerDate.setOnClickListener(this);

        buttonReg.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            String email, userName, password, country, Dob;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());
            country = String.valueOf(editTextCountry.getText());
            Dob = String.valueOf(registerDate.getText());
            userName =String.valueOf(editTextUserName.getText());

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(country) || TextUtils.isEmpty(Dob)) {
                Toast.makeText(Register.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();


                            currentUser.sendEmailVerification().addOnCompleteListener(emailVerificationTask -> {
                                if (emailVerificationTask.isSuccessful()) {
                                    Toast.makeText(Register.this, "Verification email sent. Please verify your email address.", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(Register.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                    Log.e("EmailVerification", "Failed to send verification email: " + emailVerificationTask.getException().getMessage());
                                }
                            });

                            // Save user data to Realtime Database
                            //dekh bhai dekh
                            User user = new User(userName,email ,country, Dob);


                            DatabaseReference usersRef = FirebaseDatabase.getInstance("https://insectdetection-c56d4-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

                            usersRef.child(currentUser.getUid()).setValue(user)
                                    .addOnSuccessListener(aVoid -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(Register.this, "Account Created.", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        progressBar.setVisibility(View.GONE);
                                        Toast.makeText(Register.this, "Failed to create account.", Toast.LENGTH_SHORT).show();
                                        Log.e("RealtimeDatabase", "Failed to add user data: " + e.getMessage());
                                    });
                        } else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Register.this, "Failed to create account.", Toast.LENGTH_SHORT).show();
                            Log.e("AuthenticationError", "Authentication failed: " + task.getException().getMessage());
                        }
                    });


        });
    }

    @Override
    public void onClick(View v) {
        DatePicker datePicker = new DatePicker(this);
        int currentDay = datePicker.getDayOfMonth();
        int currentMonth = (datePicker.getMonth()) + 1;
        int currentYear = datePicker.getYear();

        datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> registerDate.setText(dayOfMonth + "/" + (month + 1) + "/" + year), currentYear, currentMonth, currentDay);
        datePickerDialog.show();
    }
}