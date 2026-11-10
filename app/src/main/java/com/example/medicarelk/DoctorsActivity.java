package com.example.medicarelk;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DoctorsActivity extends AppCompatActivity {

    private SearchView etSearchDoctors;
    private Button btnBackToHome;

    private LinearLayout[] doctorCards;
    private Button[] bookButtons;

    // TextViews of each card: [name, "specialty | city", available time, fee]
    private TextView[][] cardTexts;

    // Doctors loaded from the database (same order as the cards)
    private List<DatabaseHelper.Doctor> doctors;

    private DatabaseHelper db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!SessionManager.requireLogin(this)) return;

        setContentView(R.layout.activity_doctors);

        db = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        etSearchDoctors = findViewById(R.id.etSearchDoctors);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        doctorCards = new LinearLayout[]{
                findViewById(R.id.doctorCard1),
                findViewById(R.id.doctorCard2),
                findViewById(R.id.doctorCard3),
                findViewById(R.id.doctorCard4),
                findViewById(R.id.doctorCard5)
        };

        bookButtons = new Button[]{
                findViewById(R.id.btnBookDoctor1),
                findViewById(R.id.btnBookDoctor2),
                findViewById(R.id.btnBookDoctor3),
                findViewById(R.id.btnBookDoctor4),
                findViewById(R.id.btnBookDoctor5)
        };

        cardTexts = new TextView[][]{
                {findViewById(R.id.textView19), findViewById(R.id.textView22),
                        findViewById(R.id.textView23), findViewById(R.id.textView24)},
                {findViewById(R.id.textView25), findViewById(R.id.textView26),
                        findViewById(R.id.textView27), findViewById(R.id.textView28)},
                {findViewById(R.id.textView29), findViewById(R.id.textView30),
                        findViewById(R.id.textView31), findViewById(R.id.textView32)},
                {findViewById(R.id.textView33), findViewById(R.id.textView34),
                        findViewById(R.id.textView35), findViewById(R.id.textView36)},
                {findViewById(R.id.textView37), findViewById(R.id.textView38),
                        findViewById(R.id.textView39), findViewById(R.id.textView40)}
        };

        loadDoctorsFromDatabase();
        setupBookingButtons();
        setupDoctorSearch();

        btnBackToHome.setOnClickListener(view -> {
            Intent intent = new Intent(DoctorsActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    // -----------------------------------------
    // LOAD DOCTORS FROM DATABASE
    // -----------------------------------------
    private void loadDoctorsFromDatabase() {

        doctors = db.getDoctors();

        for (int i = 0; i < doctorCards.length; i++) {

            if (i < doctors.size()) {
                DatabaseHelper.Doctor d = doctors.get(i);

                cardTexts[i][0].setText(d.name);
                cardTexts[i][1].setText(d.specialty + " | " + d.city);
                cardTexts[i][2].setText(d.availableTime);
                cardTexts[i][3].setText(d.feeText());
            } else {
                // The layout has 5 cards; hide any that have no doctor in the database
                doctorCards[i].setVisibility(View.GONE);
                bookButtons[i].setVisibility(View.GONE);
            }
        }
    }

    // -----------------------------------------
    // BOOKING: pick date -> pick time -> save
    // -----------------------------------------
    private void setupBookingButtons() {
        for (int i = 0; i < bookButtons.length; i++) {
            final int position = i;
            bookButtons[i].setOnClickListener(view -> {
                if (position < doctors.size()) {
                    pickDateAndTime(doctors.get(position));
                }
            });
        }
    }

    private void pickDateAndTime(DatabaseHelper.Doctor doctor) {

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

                                saveBooking(doctor, appointment);
                            },
                            now.get(Calendar.HOUR_OF_DAY),
                            now.get(Calendar.MINUTE),
                            false
                    );
                    timePicker.setTitle("Select appointment time");
                    timePicker.show();
                },
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH)
        );

        datePicker.setTitle("Select appointment date");
        datePicker.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePicker.show();
    }

    private void saveBooking(DatabaseHelper.Doctor doctor, Calendar appointment) {

        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                .format(appointment.getTime());

        int result = db.addBooking(session.getUserId(), doctor, dateTime);

        if (result == DatabaseHelper.BOOKING_OK) {
            Toast.makeText(this,
                    doctor.name + " booked for " + DatabaseHelper.prettyDateTime(dateTime),
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(DoctorsActivity.this, MyBookingsActivity.class));

        } else if (result == DatabaseHelper.BOOKING_SLOT_TAKEN) {
            Toast.makeText(this,
                    "That time is already booked. Please choose another time.",
                    Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Unable to save booking", Toast.LENGTH_LONG).show();
        }
    }

    // -----------------------------------------
    // SEARCH (name, specialty or city)
    // -----------------------------------------
    private void setupDoctorSearch() {

        etSearchDoctors.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterDoctors(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterDoctors(newText);
                return true;
            }
        });
    }

    private void filterDoctors(String searchText) {

        String query = searchText.trim().toLowerCase(Locale.ROOT);

        for (int i = 0; i < doctors.size() && i < doctorCards.length; i++) {

            DatabaseHelper.Doctor d = doctors.get(i);

            boolean matches = d.name.toLowerCase(Locale.ROOT).contains(query)
                    || d.specialty.toLowerCase(Locale.ROOT).contains(query)
                    || d.city.toLowerCase(Locale.ROOT).contains(query);

            doctorCards[i].setVisibility(matches ? View.VISIBLE : View.GONE);
            bookButtons[i].setVisibility(matches ? View.VISIBLE : View.GONE);
        }
    }
}
