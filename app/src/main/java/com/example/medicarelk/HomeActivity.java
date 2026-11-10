package com.example.medicarelk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private Button btnMyBookings;
    private Button btnHealthTips;
    private Button btnHospitals;
    private Button btnFAQs;

    private TextView tvDoctors;
    private TextView tvAlerts;
    private TextView tvHome;
    private TextView tvProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Only logged-in users may see the Home page
        if (!SessionManager.requireLogin(this)) return;

        setContentView(R.layout.activity_home);

        btnMyBookings = findViewById(R.id.btnMyBookings);
        btnHealthTips = findViewById(R.id.btnHealthandTips);
        btnHospitals = findViewById(R.id.btnHospitals);
        btnFAQs = findViewById(R.id.btnFAQs);

        tvDoctors = findViewById(R.id.tvDoctors);
        tvAlerts = findViewById(R.id.tvAlerts);
        tvHome = findViewById(R.id.tvHome);
        tvProfile = findViewById(R.id.tvProfile);

        btnMyBookings.setOnClickListener(v -> openActivity(MyBookingsActivity.class));
        btnHealthTips.setOnClickListener(v -> openActivity(HealthandTipsActivity.class));
        btnHospitals.setOnClickListener(v -> openActivity(HospitalsActivity.class));
        btnFAQs.setOnClickListener(v -> openActivity(FAQsActivity.class));

        tvDoctors.setOnClickListener(v -> openActivity(DoctorsActivity.class));
        tvAlerts.setOnClickListener(v -> openActivity(AlertsActivity.class));
        tvHome.setOnClickListener(v -> {
            // Already on the Home page
        });
        tvProfile.setOnClickListener(v -> openActivity(ProfileActivity.class));
    }

    private void openActivity(Class<?> activityClass) {
        startActivity(new Intent(HomeActivity.this, activityClass));
    }
}

