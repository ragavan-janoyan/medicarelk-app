package com.example.medicarelk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medicarelk.db";
    private static final int DATABASE_VERSION = 1;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Users table
        db.execSQL(
                "CREATE TABLE users (" +
                        "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "full_name TEXT NOT NULL, " +
                        "date_of_birth TEXT, " +
                        "blood_group TEXT, " +
                        "email TEXT NOT NULL UNIQUE, " +
                        "phone TEXT NOT NULL, " +
                        "password_hash TEXT NOT NULL, " +
                        "gender TEXT NOT NULL, " +
                        "city TEXT, " +
                        "medical_history TEXT, " +
                        "emergency_contact TEXT)"
        );

        // Doctors table
        db.execSQL(
                "CREATE TABLE doctors (" +
                        "doctor_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "full_name TEXT NOT NULL, " +
                        "specialty TEXT NOT NULL, " +
                        "city TEXT NOT NULL, " +
                        "available_days TEXT, " +
                        "available_time TEXT, " +
                        "consultation_fee REAL NOT NULL)"
        );

        // Appointments table
        db.execSQL(
                "CREATE TABLE appointments (" +
                        "appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "doctor_id INTEGER NOT NULL, " +
                        "appointment_date TEXT NOT NULL, " +
                        "appointment_time TEXT NOT NULL, " +
                        "consultation_type TEXT DEFAULT 'In-person', " +
                        "status TEXT DEFAULT 'Pending', " +
                        "consultation_fee REAL NOT NULL, " +
                        "notes TEXT, " +
                        "FOREIGN KEY(user_id) REFERENCES users(user_id), " +
                        "FOREIGN KEY(doctor_id) REFERENCES doctors(doctor_id))"
        );

        // Hospitals table
        db.execSQL(
                "CREATE TABLE hospitals (" +
                        "hospital_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "hospital_name TEXT NOT NULL, " +
                        "city TEXT NOT NULL, " +
                        "address TEXT NOT NULL, " +
                        "phone TEXT NOT NULL)"
        );

        // Support inquiries table
        db.execSQL(
                "CREATE TABLE support_inquiries (" +
                        "inquiry_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER, " +
                        "sender_name TEXT NOT NULL, " +
                        "sender_email TEXT NOT NULL, " +
                        "question TEXT NOT NULL, " +
                        "status TEXT DEFAULT 'New', " +
                        "FOREIGN KEY(user_id) REFERENCES users(user_id))"
        );

        // Notifications table
        db.execSQL(
                "CREATE TABLE notifications (" +
                        "notification_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "user_id INTEGER NOT NULL, " +
                        "appointment_id INTEGER, " +
                        "title TEXT NOT NULL, " +
                        "message TEXT NOT NULL, " +
                        "is_read INTEGER DEFAULT 0, " +
                        "FOREIGN KEY(user_id) REFERENCES users(user_id), " +
                        "FOREIGN KEY(appointment_id) REFERENCES appointments(appointment_id))"
        );

        // Insert the five doctors displayed in the UI
        insertDoctor(db, "Dr. Nimali Perera", "Cardiologist",
                "Colombo", "Daily", "5:30 PM", 2000);

        insertDoctor(db, "Mr. Kasun Ferando", "General Practitioner",
                "Galle", "Sunday and Saturday", "4:00 PM", 1500);

        insertDoctor(db, "Dr. Suresh Jathu", "Dermatologist",
                "Kandy", "Tuesday and Friday", "6:00 PM", 3000);

        insertDoctor(db, "Dr. Tharushi Silva", "Neurologist",
                "Negombo", "Monday and Wednesday", "4:30 PM", 3500);

        insertDoctor(db, "Dr. Nimesh Singa", "Pediatrician",
                "Colombo", "Tuesday and Sunday", "5:00 PM", 2500);

        // Insert the five hospitals
        insertHospital(db, "National Hospital of Sri Lanka",
                "Colombo", "Regent Street, Colombo 10", "011 269 1111");

        insertHospital(db, "Colombo South Teaching Hospital",
                "Kalubowila", "Hospital Road, Kalubowila", "011 283 2950");

        insertHospital(db, "Teaching Hospital Kandy",
                "Kandy", "William Gopallawa Mawatha", "081 222 2261");

        insertHospital(db, "Teaching Hospital Karapitiya",
                "Galle", "Karapitiya, Galle", "091 223 2567");

        insertHospital(db, "Teaching Hospital Jaffna",
                "Jaffna", "Hospital Road, Jaffna", "021 222 2261");
    }

    private void insertDoctor(
            SQLiteDatabase db,
            String name,
            String specialty,
            String city,
            String days,
            String time,
            double fee) {

        ContentValues values = new ContentValues();

        values.put("full_name", name);
        values.put("specialty", specialty);
        values.put("city", city);
        values.put("available_days", days);
        values.put("available_time", time);
        values.put("consultation_fee", fee);

        db.insert("doctors", null, values);
    }

    private void insertHospital(
            SQLiteDatabase db,
            String name,
            String city,
            String address,
            String phone) {

        ContentValues values = new ContentValues();

        values.put("hospital_name", name);
        values.put("city", city);
        values.put("address", address);
        values.put("phone", phone);

        db.insert("hospitals", null, values);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion) {

        // Add database migration code here when the schema changes.
    }

    public boolean registerUser(
            String fullName,
            String dateOfBirth,
            String bloodGroup,
            String email,
            String phone,
            String passwordHash,
            String gender) {

        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put("full_name", fullName);
        values.put("date_of_birth", dateOfBirth);
        values.put("blood_group", bloodGroup);
        values.put("email", email);
        values.put("phone", phone);
        values.put("password_hash", passwordHash);
        values.put("gender", gender);

        long result = db.insert("users", null, values);

        return result != -1;
    }

    public Cursor getUserByEmail(String email) {

        SQLiteDatabase db = getReadableDatabase();

        return db.query(
                "users",
                new String[]{
                        "user_id",
                        "full_name",
                        "date_of_birth",
                        "blood_group",
                        "email",
                        "phone",
                        "gender",
                        "city",
                        "medical_history",
                        "emergency_contact"
                },
                "email = ?",
                new String[]{email},
                null,
                null,
                null
        );
    }
}
