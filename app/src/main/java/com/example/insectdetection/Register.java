package com.example.insectdetection;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    Spinner divisionSpinner, districtSpinner;
    ArrayAdapter<CharSequence> divisionAdapter, districtAdapter;
    EditText editTextEmail, editTextPassword, editTextCountry, editTextUserName;
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
        if (currentUser != null && currentUser.isEmailVerified()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Initialize spinners
        divisionSpinner = findViewById(R.id.divisionSpinner);
        districtSpinner = findViewById(R.id.districtSpinner);

        // Set up adapters for spinners
        divisionAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_divisions, android.R.layout.simple_spinner_item);
        divisionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        divisionSpinner.setAdapter(divisionAdapter);
        divisionSpinner.setOnItemSelectedListener(this);

        // Initially hide district spinner
        districtSpinner.setVisibility(View.GONE);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance("https://insectdetection-c56d4-default-rtdb.asia-southeast1.firebasedatabase.app/");

        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextUserName = findViewById(R.id.userName);
//        editTextCountry = findViewById(R.id.inputTV);
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
            String email, userName, password, division,district, Dob;
            email = String.valueOf(editTextEmail.getText());
            password = String.valueOf(editTextPassword.getText());
            division = String.valueOf(divisionSpinner.getSelectedItem());
            district = String.valueOf(districtSpinner.getSelectedItem());
            Dob = String.valueOf(registerDate.getText());
            userName = String.valueOf(editTextUserName.getText());

            // Check if division and district are selected
            if (TextUtils.isEmpty(division) || TextUtils.isEmpty(district)) {
                Toast.makeText(Register.this, "Please select division and district", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
                return;
            }

//            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(userName) || TextUtils.isEmpty(password) || TextUtils.isEmpty(country) || TextUtils.isEmpty(Dob)) {
//                Toast.makeText(Register.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
//                progressBar.setVisibility(View.GONE);
//                return;
//            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser currentUser = mAuth.getCurrentUser();

                            currentUser.sendEmailVerification().addOnCompleteListener(emailVerificationTask -> {
                                Toast.makeText(Register.this, "Verification email sent. Please verify your email address.", Toast.LENGTH_LONG).show();

                                if (emailVerificationTask.isSuccessful()) {
                                    // Save user data to Realtime Database
                                    User user = new User(userName, email, division, district, Dob);
                                    DatabaseReference usersRef = FirebaseDatabase.getInstance("https://insectdetection-c56d4-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

                                    usersRef.child(currentUser.getUid()).setValue(user)
                                            .addOnSuccessListener(aVoid -> {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(Register.this, "Account Created.", Toast.LENGTH_SHORT).show();

                                                // Check if email is verified
                                                if (currentUser.isEmailVerified()) {
                                                    // Redirect to home page only after email verification
                                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                } else {
                                                    Toast.makeText(Register.this, "Please verify your email address.", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                progressBar.setVisibility(View.GONE);
                                                Toast.makeText(Register.this, "Failed to create account.", Toast.LENGTH_SHORT).show();
                                                Log.e("RealtimeDatabase", "Failed to add user data: " + e.getMessage());
                                            });

                                } else {
                                    Toast.makeText(Register.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                    Log.e("EmailVerification", "Failed to send verification email: " + emailVerificationTask.getException().getMessage());
                                }
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
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == R.id.divisionSpinner) {
            // Show district spinner when a division is selected
            districtSpinner.setVisibility(View.VISIBLE);

            // Load districts based on selected division
            switch (position) {
                case 1: // Barisal
                    districtAdapter = ArrayAdapter.createFromResource(this,
                            R.array.array_districts_barisal, android.R.layout.simple_spinner_item);
                    break;
                case 2: // Chittagong
                    districtAdapter = ArrayAdapter.createFromResource(this,
                            R.array.array_districts_chittagong, android.R.layout.simple_spinner_item);
                    break;
                case 3: // Dhaka
                    districtAdapter = ArrayAdapter.createFromResource(this,
                            R.array.array_districts_dhaka, android.R.layout.simple_spinner_item);
                    break;
                // Add cases for other divisions as needed
                default:
                    // Hide district spinner or handle the default case according to your application logic
                    districtSpinner.setVisibility(View.GONE); // for example, hide the district spinner
                    return; // exit the method to prevent further execution
            }

            // Set the dropdown layout resource
            districtAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            // Set the adapter to the district spinner
            districtSpinner.setAdapter(districtAdapter);
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Handle the case where nothing is selected in the spinner
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
