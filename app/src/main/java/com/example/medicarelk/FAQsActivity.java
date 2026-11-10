package com.example.medicarelk;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class FAQsActivity extends AppCompatActivity {

    private EditText etSupportName;
    private EditText etSupportEmail;
    private EditText etSupportQuestion;

    private Button btnContactSupport;
    private Button btnBackToHome;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.requireLogin(this)) return;

        setContentView(R.layout.activity_faqs);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        etSupportName = findViewById(R.id.etSupportName);
        etSupportEmail = findViewById(R.id.etSupportEmail);
        etSupportQuestion = findViewById(R.id.etSupportQuestion);

        btnContactSupport = findViewById(R.id.btnContactSupport);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        btnContactSupport.setOnClickListener(v -> submitInquiry());

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(FAQsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void submitInquiry() {

        String name = etSupportName.getText().toString().trim();
        String email = etSupportEmail.getText().toString().trim();
        String question = etSupportQuestion.getText().toString().trim();

        if (name.isEmpty()) {
            etSupportName.setError("Please enter your name");
            etSupportName.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            etSupportEmail.setError("Please enter your email");
            etSupportEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etSupportEmail.setError("Please enter a valid email address");
            etSupportEmail.requestFocus();
            return;
        }

        if (question.isEmpty()) {
            etSupportQuestion.setError("Please enter your question");
            etSupportQuestion.requestFocus();
            return;
        }

        // Save the inquiry in the support_inquiries table
        boolean saved = db.addInquiry(session.getUserId(), name, email, question);

        if (saved) {
            Toast.makeText(this,
                    "Thank you, " + name + "! Your inquiry has been submitted.",
                    Toast.LENGTH_LONG).show();

            etSupportName.setText("");
            etSupportEmail.setText("");
            etSupportQuestion.setText("");
        } else {
            Toast.makeText(this,
                    "Unable to submit your inquiry. Please try again.",
                    Toast.LENGTH_LONG).show();
        }
    }
}
