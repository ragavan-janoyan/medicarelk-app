package com.example.medicarelk;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class HealthandTipsActivity extends AppCompatActivity {

    private Button btnBackToHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.requireLogin(this)) return;

        setContentView(R.layout.activity_healthand_tips);

        btnBackToHome = findViewById(R.id.btnBackToHome);

        loadHealthTips();

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(HealthandTipsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // Fill the four existing tip cards from the health_tips table
    private void loadHealthTips() {

        // {title, description} TextViews of each card in activity_healthand_tips.xml
        TextView[][] cards = {
                {findViewById(R.id.textView42), findViewById(R.id.textView43)},
                {findViewById(R.id.textView44), findViewById(R.id.textView45)},
                {findViewById(R.id.textView46), findViewById(R.id.textView47)},
                {findViewById(R.id.textView48), findViewById(R.id.textView49)}
        };

        List<DatabaseHelper.HealthTip> tips =
                DatabaseHelper.getInstance(this).getHealthTips();

        for (int i = 0; i < cards.length && i < tips.size(); i++) {
            cards[i][0].setText(tips.get(i).title);
            cards[i][1].setText(tips.get(i).description);
        }
    }
}