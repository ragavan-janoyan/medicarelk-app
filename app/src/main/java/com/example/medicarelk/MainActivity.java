package com.example.medicarelk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnCreateAccount;
    private TextView tvForgotPassword;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        // Already logged in? Skip the login screen.
        if (session.isLoggedIn() && db.getUser(session.getUserId()) != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // 1. LOGIN
        btnLogin.setOnClickListener(view -> loginUser());

        // 2. CREATE ACCOUNT
        btnCreateAccount.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, Register.class)));

        // 3. FORGOT PASSWORD
        tvForgotPassword.setClickable(true);
        tvForgotPassword.setFocusable(true);
        tvForgotPassword.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, Change_password.class)));
    }

    private void loginUser() {

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }

        // Check the credentials against the users table
        long userId = db.loginUser(email, password);

        if (userId == -1) {
            Toast.makeText(this,
                    "Incorrect email or password.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        session.login(userId);

        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
