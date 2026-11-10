package com.example.medicarelk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class HospitalsActivity extends AppCompatActivity {

    private Button btnBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.requireLogin(this)) return;

        setContentView(R.layout.activity_hospitals);

        btnBackToHome = findViewById(R.id.btnBackToHome);

        loadHospitals();

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(HospitalsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // Fill the five existing hospital cards from the hospitals table
    private void loadHospitals() {

        // {name, address, phone} TextViews of each card in activity_hospitals.xml
        TextView[][] cards = {
                {findViewById(R.id.textView52), findViewById(R.id.textView53), findViewById(R.id.textView54)},
                {findViewById(R.id.textView55), findViewById(R.id.textView56), findViewById(R.id.textView57)},
                {findViewById(R.id.textView58), findViewById(R.id.textView59), findViewById(R.id.textView60)},
                {findViewById(R.id.textView61), findViewById(R.id.textView62), findViewById(R.id.textView63)},
                {findViewById(R.id.textView64), findViewById(R.id.textView65), findViewById(R.id.textView66)}
        };

        List<DatabaseHelper.Hospital> hospitals =
                DatabaseHelper.getInstance(this).getHospitals();

        for (int i = 0; i < cards.length && i < hospitals.size(); i++) {
            DatabaseHelper.Hospital h = hospitals.get(i);
            cards[i][0].setText(h.name);
            cards[i][1].setText(h.address);
            cards[i][2].setText("TP num - " + h.phone);
        }
    }
}