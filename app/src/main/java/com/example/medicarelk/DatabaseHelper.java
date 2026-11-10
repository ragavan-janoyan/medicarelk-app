package com.example.medicarelk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Single access point for the MediCare LK database.
 * SQLiteOpenHelper creates the file  medicarelk.db  automatically the first time
 * the app runs (data/data/com.example.medicarelk/databases/medicarelk.db).
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "medicarelk.db";
    private static final int DB_VERSION = 1;

    // Result codes for bookings
    public static final int BOOKING_OK = 1;
    public static final int BOOKING_SLOT_TAKEN = -2;
    public static final int BOOKING_ERROR = -1;

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ------------------------------------------------------------------
    // Simple model classes
    // ------------------------------------------------------------------
    public static class User {
        public long id;
        public String fullName, dateOfBirth, bloodGroup, email, phone, gender;
    }

    public static class Doctor {
        public long id;
        public String name, specialty, city, availableTime;
        public int fee;

        public String feeText() {
            return String.format(Locale.US, "LKR %,d", fee);
        }
    }

    public static class Booking {
        public long id;
        public String doctorName, specialty, city, availableTime;
        public int fee;
        public String appointmentDateTime; // yyyy-MM-dd HH:mm
        public String status;

        public String feeText() {
            return String.format(Locale.US, "LKR %,d", fee);
        }
    }

    public static class Hospital {
        public String name, address, phone;
    }

    public static class HealthTip {
        public String title, description;
    }

    public static class Notification {
        public String title, message, createdAt;
    }

    // ------------------------------------------------------------------
    // Create / upgrade
    // ------------------------------------------------------------------
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "full_name TEXT NOT NULL," +
                "date_of_birth TEXT NOT NULL," +
                "blood_group TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE COLLATE NOCASE," +
                "phone TEXT NOT NULL," +
                "gender TEXT NOT NULL," +
                "password_hash TEXT NOT NULL," +
                "salt TEXT NOT NULL," +
                "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime')))");

        db.execSQL("CREATE TABLE doctors (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "specialty TEXT NOT NULL," +
                "city TEXT NOT NULL," +
                "available_time TEXT NOT NULL," +
                "fee INTEGER NOT NULL)");

        db.execSQL("CREATE TABLE hospitals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "address TEXT NOT NULL," +
                "phone TEXT NOT NULL)");

        db.execSQL("CREATE TABLE health_tips (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT NOT NULL," +
                "description TEXT NOT NULL)");

        db.execSQL("CREATE TABLE bookings (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "doctor_id INTEGER NOT NULL," +
                "appointment_datetime TEXT NOT NULL," +
                "status TEXT NOT NULL DEFAULT 'Pending'," +
                "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE," +
                "FOREIGN KEY (doctor_id) REFERENCES doctors(id))");

        db.execSQL("CREATE TABLE notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "message TEXT NOT NULL," +
                "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE support_inquiries (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER," +
                "name TEXT NOT NULL," +
                "email TEXT NOT NULL," +
                "question TEXT NOT NULL," +
                "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))," +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL)");

        seedData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy for a student project: rebuild everything.
        db.execSQL("DROP TABLE IF EXISTS support_inquiries");
        db.execSQL("DROP TABLE IF EXISTS notifications");
        db.execSQL("DROP TABLE IF EXISTS bookings");
        db.execSQL("DROP TABLE IF EXISTS health_tips");
        db.execSQL("DROP TABLE IF EXISTS hospitals");
        db.execSQL("DROP TABLE IF EXISTS doctors");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    private void seedData(SQLiteDatabase db) {

        String[][] doctors = {
                {"Dr. Nimali Perera", "Cardiologist", "Colombo", "Daily - 5:30 PM", "2000"},
                {"Mr. Kasun Ferando", "General Practitioner", "Galle", "Sun and Sat - 4:00 PM", "1500"},
                {"Dr. Suresh Jathu", "Dermatologist", "Kandy", "Tue and Fri - 6:00 PM", "3000"},
                {"Dr. Tharushi Silva", "Neurologist", "Negombo", "Mon and Wed - 4:30 PM", "3500"},
                {"Dr. Nimesh Singa", "Pediatrician", "Colombo", "Tue and Sun - 5:00 PM", "2500"}
        };
        for (String[] d : doctors) {
            db.execSQL("INSERT INTO doctors(name,specialty,city,available_time,fee) VALUES (?,?,?,?,?)",
                    new Object[]{d[0], d[1], d[2], d[3], Integer.parseInt(d[4])});
        }

        String[][] hospitals = {
                {"National Hospital Of Srilanka", "Colombo - Regent Street, Colombo 10.", "011 269 1111"},
                {"Colombo South Teaching Hospital", "Kalubowila - Hospital Road, Kalubowila.", "011 283 2950"},
                {"Teaching Hospital Kandy", "Kandy - William Gopallawa Mawatha", "081 222 2261"},
                {"Teaching Hospital Karapitiya", "Galle - Karapitiya, Galle", "091 223 2567"},
                {"Teaching Hospital Jaffna", "Jaffna - Hospital Road, Jaffna", "021 222 2261"}
        };
        for (String[] h : hospitals) {
            db.execSQL("INSERT INTO hospitals(name,address,phone) VALUES (?,?,?)",
                    new Object[]{h[0], h[1], h[2]});
        }

        String[][] tips = {
                {"Sleep 7 to 8 hours", "Consistent sleep supports heart health."},
                {"Walk 30 minutes daily", "Light activity helps maintain healthy blood pressure."},
                {"Drink enough water", "Staying hydrated supports normal body functions."},
                {"Eat a balanced diet", "Include fruits, vegetables, whole grains, and protein."}
        };
        for (String[] t : tips) {
            db.execSQL("INSERT INTO health_tips(title,description) VALUES (?,?)",
                    new Object[]{t[0], t[1]});
        }
    }

    // ------------------------------------------------------------------
    // Password helpers (salted SHA-256, no external library needed)
    // ------------------------------------------------------------------
    private static String newSalt() {
        byte[] bytes = new byte[16];
        new SecureRandom().nextBytes(bytes);
        return toHex(bytes);
    }

    private static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes("UTF-8"));
            return toHex(md.digest(password.getBytes("UTF-8")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // USERS
    // ------------------------------------------------------------------
    public boolean emailExists(String email) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM users WHERE email = ?", new String[]{email})) {
            return c.moveToFirst();
        }
    }

    /** @return new user id, or -1 if the insert failed (e.g. duplicate email) */
    public long registerUser(String fullName, String dob, String bloodGroup,
                             String email, String phone, String gender, String password) {
        String salt = newSalt();
        ContentValues v = new ContentValues();
        v.put("full_name", fullName);
        v.put("date_of_birth", dob);
        v.put("blood_group", bloodGroup);
        v.put("email", email);
        v.put("phone", phone);
        v.put("gender", gender);
        v.put("salt", salt);
        v.put("password_hash", hash(password, salt));
        return getWritableDatabase().insert("users", null, v);
    }

    /** @return user id if the credentials are correct, otherwise -1 */
    public long loginUser(String email, String password) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, password_hash, salt FROM users WHERE email = ?",
                new String[]{email})) {
            if (c.moveToFirst()) {
                String expected = c.getString(1);
                String actual = hash(password, c.getString(2));
                if (expected.equals(actual)) return c.getLong(0);
            }
        }
        return -1;
    }

    public User getUser(long userId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, full_name, date_of_birth, blood_group, email, phone, gender " +
                        "FROM users WHERE id = ?", new String[]{String.valueOf(userId)})) {
            if (!c.moveToFirst()) return null;
            User u = new User();
            u.id = c.getLong(0);
            u.fullName = c.getString(1);
            u.dateOfBirth = c.getString(2);
            u.bloodGroup = c.getString(3);
            u.email = c.getString(4);
            u.phone = c.getString(5);
            u.gender = c.getString(6);
            return u;
        }
    }

    /** @return true if an account with this email existed and the password was changed */
    public boolean updatePassword(String email, String newPassword) {
        String salt = newSalt();
        ContentValues v = new ContentValues();
        v.put("salt", salt);
        v.put("password_hash", hash(newPassword, salt));
        return getWritableDatabase().update("users", v, "email = ?", new String[]{email}) > 0;
    }

    // ------------------------------------------------------------------
    // DOCTORS / HOSPITALS / TIPS
    // ------------------------------------------------------------------
    public List<Doctor> getDoctors() {
        List<Doctor> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT id, name, specialty, city, available_time, fee FROM doctors ORDER BY id", null)) {
            while (c.moveToNext()) {
                Doctor d = new Doctor();
                d.id = c.getLong(0);
                d.name = c.getString(1);
                d.specialty = c.getString(2);
                d.city = c.getString(3);
                d.availableTime = c.getString(4);
                d.fee = c.getInt(5);
                list.add(d);
            }
        }
        return list;
    }

    public List<Hospital> getHospitals() {
        List<Hospital> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT name, address, phone FROM hospitals ORDER BY id", null)) {
            while (c.moveToNext()) {
                Hospital h = new Hospital();
                h.name = c.getString(0);
                h.address = c.getString(1);
                h.phone = c.getString(2);
                list.add(h);
            }
        }
        return list;
    }

    public List<HealthTip> getHealthTips() {
        List<HealthTip> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT title, description FROM health_tips ORDER BY id", null)) {
            while (c.moveToNext()) {
                HealthTip t = new HealthTip();
                t.title = c.getString(0);
                t.description = c.getString(1);
                list.add(t);
            }
        }
        return list;
    }

    // ------------------------------------------------------------------
    // BOOKINGS
    // ------------------------------------------------------------------
    private boolean slotTaken(long doctorId, String dateTime, long ignoreBookingId) {
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM bookings WHERE doctor_id = ? AND appointment_datetime = ? " +
                        "AND status <> 'Cancelled' AND id <> ?",
                new String[]{String.valueOf(doctorId), dateTime, String.valueOf(ignoreBookingId)})) {
            return c.moveToFirst();
        }
    }

    public int addBooking(long userId, Doctor doctor, String dateTime) {
        if (slotTaken(doctor.id, dateTime, -1)) return BOOKING_SLOT_TAKEN;

        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("doctor_id", doctor.id);
        v.put("appointment_datetime", dateTime);
        v.put("status", "Pending");
        if (getWritableDatabase().insert("bookings", null, v) == -1) return BOOKING_ERROR;

        addNotification(userId, "Booking saved",
                doctor.name + " (" + doctor.specialty + ") on " + prettyDateTime(dateTime)
                        + ". Status: Pending.");
        return BOOKING_OK;
    }

    public List<Booking> getBookings(long userId) {
        List<Booking> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT b.id, d.name, d.specialty, d.city, d.available_time, d.fee, " +
                        "b.appointment_datetime, b.status " +
                        "FROM bookings b JOIN doctors d ON d.id = b.doctor_id " +
                        "WHERE b.user_id = ? ORDER BY b.id DESC",
                new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                Booking b = new Booking();
                b.id = c.getLong(0);
                b.doctorName = c.getString(1);
                b.specialty = c.getString(2);
                b.city = c.getString(3);
                b.availableTime = c.getString(4);
                b.fee = c.getInt(5);
                b.appointmentDateTime = c.getString(6);
                b.status = c.getString(7);
                list.add(b);
            }
        }
        return list;
    }

    public int rescheduleBooking(long userId, long bookingId, String newDateTime) {
        long doctorId = -1;
        String doctorName = "";
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT b.doctor_id, d.name FROM bookings b JOIN doctors d ON d.id = b.doctor_id " +
                        "WHERE b.id = ? AND b.user_id = ?",
                new String[]{String.valueOf(bookingId), String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                doctorId = c.getLong(0);
                doctorName = c.getString(1);
            }
        }
        if (doctorId == -1) return BOOKING_ERROR;
        if (slotTaken(doctorId, newDateTime, bookingId)) return BOOKING_SLOT_TAKEN;

        ContentValues v = new ContentValues();
        v.put("appointment_datetime", newDateTime);
        v.put("status", "Pending");
        int rows = getWritableDatabase().update("bookings", v, "id = ? AND user_id = ?",
                new String[]{String.valueOf(bookingId), String.valueOf(userId)});
        if (rows == 0) return BOOKING_ERROR;

        addNotification(userId, "Booking rescheduled",
                doctorName + " moved to " + prettyDateTime(newDateTime) + ". Status: Pending.");
        return BOOKING_OK;
    }

    public boolean cancelBooking(long userId, long bookingId, String doctorName) {
        ContentValues v = new ContentValues();
        v.put("status", "Cancelled");
        int rows = getWritableDatabase().update("bookings", v, "id = ? AND user_id = ?",
                new String[]{String.valueOf(bookingId), String.valueOf(userId)});
        if (rows > 0) {
            addNotification(userId, "Booking cancelled",
                    "Your appointment with " + doctorName + " was cancelled.");
        }
        return rows > 0;
    }

    // ------------------------------------------------------------------
    // NOTIFICATIONS
    // ------------------------------------------------------------------
    public void addNotification(long userId, String title, String message) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("title", title);
        v.put("message", message);
        getWritableDatabase().insert("notifications", null, v);
    }

    public List<Notification> getNotifications(long userId) {
        List<Notification> list = new ArrayList<>();
        try (Cursor c = getReadableDatabase().rawQuery(
                "SELECT title, message, created_at FROM notifications " +
                        "WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)})) {
            while (c.moveToNext()) {
                Notification n = new Notification();
                n.title = c.getString(0);
                n.message = c.getString(1);
                n.createdAt = c.getString(2);
                list.add(n);
            }
        }
        return list;
    }

    // ------------------------------------------------------------------
    // SUPPORT INQUIRIES
    // ------------------------------------------------------------------
    public boolean addInquiry(long userId, String name, String email, String question) {
        ContentValues v = new ContentValues();
        if (userId > 0) v.put("user_id", userId);
        v.put("name", name);
        v.put("email", email);
        v.put("question", question);
        return getWritableDatabase().insert("support_inquiries", null, v) != -1;
    }

    // ------------------------------------------------------------------
    // Date helpers (stored as yyyy-MM-dd HH:mm so they sort correctly)
    // ------------------------------------------------------------------
    public static String prettyDateTime(String stored) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse(stored);
            return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(d);
        } catch (Exception e) {
            return stored;
        }
    }
}
