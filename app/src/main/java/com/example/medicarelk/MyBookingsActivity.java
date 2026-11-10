package com.example.medicarelk;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MyBookingsActivity extends AppCompatActivity {

    private LinearLayout bookingsContainer;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.requireLogin(this)) return;

        setContentView(R.layout.activity_my_bookings);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        bookingsContainer = findViewById(R.id.bookingsContainer);

        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(MyBookingsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        displayBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bookingsContainer != null) {
            displayBookings();
        }
    }

    // Read this user's bookings from the database and show them
    private void displayBookings() {

        bookingsContainer.removeAllViews();

        List<DatabaseHelper.Booking> bookings = db.getBookings(session.getUserId());

        if (bookings.isEmpty()) {
            showEmptyMessage();
            return;
        }

        for (DatabaseHelper.Booking b : bookings) {
            addBookingCard(b);
        }
    }

    private void showEmptyMessage() {

        TextView emptyMessage = new TextView(this);

        emptyMessage.setText("You have no bookings yet.\n\n" + "Visit the Doctors page to book an appointment.");
        emptyMessage.setTextSize(16);
        emptyMessage.setTextColor(Color.GRAY);
        emptyMessage.setGravity(Gravity.CENTER);
        emptyMessage.setPadding(24, 50, 24, 50);

        bookingsContainer.addView(emptyMessage);
    }

    private void addBookingCard(DatabaseHelper.Booking booking) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 20);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(8, 10, 8, 10);
        card.setLayoutParams(cardParams);

        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.WHITE);
        background.setCornerRadius(28);
        background.setStroke(2, Color.rgb(230, 235, 235));
        card.setBackground(background);
        card.setElevation(4);

        // Doctor name
        TextView name = new TextView(this);
        name.setText(booking.doctorName);
        name.setTextSize(20);
        name.setTextColor(Color.rgb(35, 55, 55));
        name.setTypeface(null, Typeface.BOLD);
        card.addView(name);

        // Status
        TextView statusText = new TextView(this);
        statusText.setText("Status: " + booking.status);
        statusText.setTextSize(14);
        statusText.setPadding(0, 8, 0, 8);

        if ("Cancelled".equalsIgnoreCase(booking.status)) {
            statusText.setTextColor(Color.RED);
        } else if ("Confirmed".equalsIgnoreCase(booking.status)) {
            statusText.setTextColor(Color.rgb(25, 118, 111));
        } else {
            statusText.setTextColor(Color.rgb(190, 100, 30));
        }
        card.addView(statusText);

        // Specialty and city
        TextView details = new TextView(this);
        details.setText(booking.specialty + " | " + booking.city);
        details.setTextSize(15);
        details.setTextColor(Color.DKGRAY);
        card.addView(details);

        // Appointment date and time (chosen by the patient)
        TextView appointment = new TextView(this);
        appointment.setText("Appointment: " + DatabaseHelper.prettyDateTime(booking.appointmentDateTime));
        appointment.setTextSize(15);
        appointment.setTextColor(Color.rgb(24, 51, 47));
        appointment.setTypeface(null, Typeface.BOLD);
        appointment.setPadding(0, 10, 0, 0);
        card.addView(appointment);

        // Doctor's usual availability
        TextView time = new TextView(this);
        time.setText("Doctor available: " + booking.availableTime);
        time.setTextSize(15);
        time.setTextColor(Color.DKGRAY);
        time.setPadding(0, 6, 0, 0);
        card.addView(time);

        // Fee
        TextView price = new TextView(this);
        price.setText("Consultation fee: " + booking.feeText());
        price.setTextSize(15);
        price.setTextColor(Color.DKGRAY);
        price.setPadding(0, 6, 0, 10);
        card.addView(price);

        // Buttons
        LinearLayout buttonRow = new LinearLayout(this);
        buttonRow.setOrientation(LinearLayout.HORIZONTAL);
        buttonRow.setGravity(Gravity.CENTER_VERTICAL);

        Button btnReschedule = new Button(this);
        btnReschedule.setText("Reschedule");
        btnReschedule.setTextSize(12);

        LinearLayout.LayoutParams rescheduleParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        rescheduleParams.setMargins(0, 0, 8, 0);
        buttonRow.addView(btnReschedule, rescheduleParams);

        Button btnCancel = new Button(this);
        btnCancel.setText("Cancel");
        btnCancel.setTextSize(12);
        btnCancel.setTextColor(Color.WHITE);
        btnCancel.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(190, 65, 35)));

        LinearLayout.LayoutParams cancelParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        buttonRow.addView(btnCancel, cancelParams);

        card.addView(buttonRow);

        boolean inactive = "Cancelled".equalsIgnoreCase(booking.status)
                || "Completed".equalsIgnoreCase(booking.status);
        btnReschedule.setEnabled(!inactive);
        btnCancel.setEnabled(!inactive);

        btnReschedule.setOnClickListener(v -> showRescheduleDialog(booking.id));
        btnCancel.setOnClickListener(v -> showCancelDialog(booking.id, booking.doctorName));

        bookingsContainer.addView(card);
    }


    // RESCHEDULE

    private void showRescheduleDialog(long bookingId) {

        Calendar now = Calendar.getInstance();

        DatePickerDialog datePicker = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {

                    TimePickerDialog timePicker = new TimePickerDialog(
                            this,
                            (timeView, hourOfDay, minute) -> {

                                Calendar appointment = Calendar.getInstance();
                                appointment.set(year, month, dayOfMonth, hourOfDay, minute, 0);
                                appointment.set(Calendar.MILLISECOND, 0);

                                if (appointment.before(Calendar.getInstance())) {
                                    Toast.makeText(this,
                                            "Please select a future time.",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                String newDateTime =
                                        new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                                                .format(appointment.getTime());

                                updateBooking(bookingId, newDateTime);
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            false
                    );
                    timePicker.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.setTitle("Select new appointment date");
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void updateBooking(long bookingId, String newDateTime) {

        int result = db.rescheduleBooking(session.getUserId(), bookingId, newDateTime);

        if (result == DatabaseHelper.BOOKING_OK) {
            Toast.makeText(this,
                    "Booking rescheduled. Confirmation is pending.",
                    Toast.LENGTH_LONG).show();
            displayBookings();

        } else if (result == DatabaseHelper.BOOKING_SLOT_TAKEN) {
            Toast.makeText(this,
                    "That time is already booked. Please choose another time.",
                    Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Unable to reschedule booking.", Toast.LENGTH_SHORT).show();
        }
    }


    // CANCEL

    private void showCancelDialog(long bookingId, String doctorName) {

        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel your booking with "
                        + doctorName + "?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes, Cancel", (dialog, which) ->
                        cancelBooking(bookingId, doctorName))
                .show();
    }

    private void cancelBooking(long bookingId, String doctorName) {

        if (db.cancelBooking(session.getUserId(), bookingId, doctorName)) {
            Toast.makeText(this, "Booking cancelled.", Toast.LENGTH_SHORT).show();
            displayBookings();
        } else {
            Toast.makeText(this, "Unable to cancel booking.", Toast.LENGTH_SHORT).show();
        }
    }
}