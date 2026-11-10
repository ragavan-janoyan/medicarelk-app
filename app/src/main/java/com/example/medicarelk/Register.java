package com.example.medicarelk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Register extends AppCompatActivity {

    private static final List<String> BLOOD_GROUPS =
            Arrays.asList("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-");

    private EditText etFullName;
    private EditText etDateOfBirth;
    private EditText etBloodGroup;
    private EditText etRegisterEmail;
    private EditText etRegisterPhone;
    private EditText etRegisterPassword;
    private EditText etConfirmPassword;

    private RadioGroup rgGender;

    private Button btnBackToLogin;
    private Button btnRegister;

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        db = DatabaseHelper.getInstance(this);

        etFullName = findViewById(R.id.etFullName);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etBloodGroup = findViewById(R.id.etBloodGroup);
        etRegisterEmail = findViewById(R.id.etRegisterEmail);
        etRegisterPhone = findViewById(R.id.etRegisterPhone);
        etRegisterPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        rgGender = findViewById(R.id.rgGender);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnRegister = findViewById(R.id.btnRegister);

        // Hints so the user knows the expected format
        etDateOfBirth.setHint("dd/MM/yyyy");
        etBloodGroup.setHint("e.g. O+");

        btnBackToLogin.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, MainActivity.class));
            finish();
        });

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {

        String fullName = etFullName.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String bloodGroup = etBloodGroup.getText().toString().trim().toUpperCase(Locale.ROOT);
        String email = etRegisterEmail.getText().toString().trim();
        String phone = etRegisterPhone.getText().toString().trim();
        String password = etRegisterPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // ---------- Validation ----------
        if (fullName.isEmpty()) {
            etFullName.setError("Please enter your full name");
            etFullName.requestFocus();
            return;
        }

        if (dateOfBirth.isEmpty()) {
            etDateOfBirth.setError("Please enter your date of birth");
            etDateOfBirth.requestFocus();
            return;
        }

        if (!isValidDateOfBirth(dateOfBirth)) {
            etDateOfBirth.setError("Use the format dd/MM/yyyy (not a future date)");
            etDateOfBirth.requestFocus();
            return;
        }

        if (bloodGroup.isEmpty()) {
            etBloodGroup.setError("Please enter your blood group");
            etBloodGroup.requestFocus();
            return;
        }

        if (!BLOOD_GROUPS.contains(bloodGroup)) {
            etBloodGroup.setError("Enter a valid blood group (A+, A-, B+, B-, AB+, AB-, O+, O-)");
            etBloodGroup.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etRegisterEmail.setError("Please enter your email");
            etRegisterEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etRegisterEmail.setError("Please enter a valid email address");
            etRegisterEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etRegisterPhone.setError("Please enter your phone number");
            etRegisterPhone.requestFocus();
            return;
        }

        if (!phone.matches("^(\\+94|0)?[0-9 ]{9,11}$")) {
            etRegisterPhone.setError("Please enter a valid phone number");
            etRegisterPhone.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etRegisterPassword.setError("Please enter a password");
            etRegisterPassword.requestFocus();
            return;
        }

        if (password.length() < 8) {
            etRegisterPassword.setError("Password must contain at least 8 characters");
            etRegisterPassword.requestFocus();
            return;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();

        if (selectedGenderId == -1) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedGender = findViewById(selectedGenderId);
        String gender = selectedGender.getText().toString();

        // ---------- Database ----------
        if (db.emailExists(email)) {
            etRegisterEmail.setError("An account with this email already exists");
            etRegisterEmail.requestFocus();
            return;
        }

        long newId = db.registerUser(fullName, dateOfBirth, bloodGroup,
                email, phone, gender, password);

        if (newId == -1) {
            Toast.makeText(this,
                    "Registration failed. Please try again.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        Toast.makeText(this,
                "Account created! Please log in.",
                Toast.LENGTH_LONG).show();

        Intent intent = new Intent(Register.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private boolean isValidDateOfBirth(String text) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            format.setLenient(false);
            Date dob = format.parse(text);
            return dob != null && !dob.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}