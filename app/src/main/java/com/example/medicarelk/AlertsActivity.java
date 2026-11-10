package com.example.medicarelk;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AlertsActivity extends AppCompatActivity {

    private LinearLayout notificationContainer;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.requireLogin(this)) return;

        setContentView(R.layout.activity_alerts);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        notificationContainer = findViewById(R.id.notificationContainer);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);

        displayNotifications();

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(AlertsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notificationContainer != null) {
            displayNotifications();
        }
    }

    // Read this user's notifications from the database (newest first)
    private void displayNotifications() {

        notificationContainer.removeAllViews();

        List<DatabaseHelper.Notification> notifications =
                db.getNotifications(session.getUserId());

        if (notifications.isEmpty()) {

            TextView emptyMessage = new TextView(this);
            emptyMessage.setText("No notifications yet.\n\n" + "Book a doctor to see your booking information here.");
            emptyMessage.setTextSize(16);
            emptyMessage.setTextColor(Color.GRAY);
            emptyMessage.setGravity(Gravity.CENTER);
            emptyMessage.setPadding(24, 40, 24, 40);

            notificationContainer.addView(emptyMessage);
            return;
        }

        for (DatabaseHelper.Notification n : notifications) {
            addNotificationCard(n.title, n.message, n.createdAt);
        }
    }

    private void addNotificationCard(String titleText, String messageText, String createdAt) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 20);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(12, 10, 12, 10);
        card.setLayoutParams(cardParams);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(24);
        background.setStroke(2, Color.rgb(231, 238, 236));
        card.setBackground(background);
        card.setElevation(4);

        TextView title = new TextView(this);
        title.setText(titleText);
        title.setTextSize(20);
        title.setTextColor(Color.rgb(24, 51, 47));
        title.setTypeface(null, Typeface.BOLD);
        card.addView(title);

        TextView message = new TextView(this);
        message.setText(messageText);
        message.setTextSize(15);
        message.setTextColor(Color.rgb(24, 51, 47));
        message.setPadding(0, 8, 0, 0);
        card.addView(message);

        TextView time = new TextView(this);
        time.setText(createdAt);
        time.setTextSize(12);
        time.setTextColor(Color.rgb(100, 115, 112));
        time.setPadding(0, 8, 0, 0);
        card.addView(time);

        notificationContainer.addView(card);
    }
}
