package com.example.medicarelk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class Change_password extends AppCompatActivity {

    private EditText etRecoveryEmail;
    private EditText etNewPassword;
    private EditText etConfirmNewPassword;

    private Button btnChangePassword;
    private Button btnBackToLogin;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        db = DatabaseHelper.getInstance(this);

        etRecoveryEmail = findViewById(R.id.etRecoveryEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        btnBackToLogin.setOnClickListener(view -> goToLogin());

        btnChangePassword.setOnClickListener(view -> changePassword());
    }

    private void changePassword() {

        String email = etRecoveryEmail.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmNewPassword.getText().toString();

        if (email.isEmpty()) {
            etRecoveryEmail.setError("Please enter your registered email");
            etRecoveryEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRecoveryEmail.setError("Please enter a valid email");
            etRecoveryEmail.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Please enter your new password");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 8) {
            etNewPassword.setError("Password must be at least 8 characters");
            etNewPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmNewPassword.setError("Please confirm your new password");
            etConfirmNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmNewPassword.setError("Passwords do not match");
            etConfirmNewPassword.requestFocus();
            return;
        }

        // Database check + update
        if (!db.emailExists(email)) {
            etRecoveryEmail.setError("No account found with this email");
            etRecoveryEmail.requestFocus();
            return;
        }

        if (db.updatePassword(email, newPassword)) {
            Toast.makeText(this,
                    "Password changed successfully. Please log in.",
                    Toast.LENGTH_LONG).show();
            goToLogin();
        } else {
            Toast.makeText(this,
                    "Could not change the password. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void goToLogin() {
        Intent intent = new Intent(Change_password.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
