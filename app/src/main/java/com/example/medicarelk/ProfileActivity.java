package com.example.medicarelk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfilePhone;
    private TextView tvProfileDOB;
    private TextView tvProfileEmail;
    private TextView tvProfileBloodGroup;
    private TextView tvGender;

    private Button btnLogout;
    private Button btnBackToHome;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.requireLogin(this)) return;

        setContentView(R.layout.activity_profile);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileDOB = findViewById(R.id.tvProfileDOB);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfileBloodGroup = findViewById(R.id.tvProfileBloodGroup);
        tvGender = findViewById(R.id.tvGender);

        btnLogout = findViewById(R.id.btnLogout);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        loadProfileDetails();

        // LOG OUT: clear the session and return to the login page
        btnLogout.setOnClickListener(v -> {
            session.logout();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // Load the logged-in patient's details from the users table
    private void loadProfileDetails() {

        DatabaseHelper.User user = db.getUser(session.getUserId());

        if (user == null) {
            // Account no longer exists (e.g. database was reset)
            Toast.makeText(this, "Please log in again.", Toast.LENGTH_SHORT).show();
            session.logout();
            SessionManager.requireLogin(this);
            return;
        }

        tvProfileName.setText(user.fullName);
        tvProfilePhone.setText(user.phone);
        tvProfileDOB.setText("Date of birth: " + user.dateOfBirth);
        tvProfileEmail.setText("Email: " + user.email);
        tvProfileBloodGroup.setText("Blood group: " + user.bloodGroup);
        tvGender.setText("Gender: " + user.gender);
    }
}