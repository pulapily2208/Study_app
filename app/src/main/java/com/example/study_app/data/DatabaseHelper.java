
package com.example.study_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Subject.Model.Subject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_app.db";
    // Increment version to ensure onUpgrade is called if needed.
    private static final int DB_VERSION = 5; // Incremented version

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        runSqlFromRaw(db, R.raw.study_app);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This is a destructive upgrade. A real app would need data migration.
        // For development, this is fine. It ensures a clean slate.
        try {
            db.execSQL("DROP TABLE IF EXISTS mon_hoc_tu_chon_map;");
            db.execSQL("DROP TABLE IF EXISTS notification_schedules;");
            db.execSQL("DROP TABLE IF EXISTS enrollments;");
            db.execSQL("DROP TABLE IF EXISTS timetable_sessions;");
            db.execSQL("DROP TABLE IF EXISTS attachments;");
            db.execSQL("DROP TABLE IF EXISTS notes;");
            db.execSQL("DROP TABLE IF EXISTS deadlines;");
            db.execSQL("DROP TABLE IF EXISTS users;");
            db.execSQL("DROP TABLE IF EXISTS hoc_phan_tien_quyet;");
            db.execSQL("DROP TABLE IF EXISTS mon_hoc;");
            db.execSQL("DROP TABLE IF EXISTS hoc_phan_tu_chon;");
            db.execSQL("DROP TABLE IF EXISTS khoa;");
            db.execSQL("DROP TABLE IF EXISTS hoc_ky;");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error dropping tables.", e);
        }
        onCreate(db);
    }

    /**
     * Executes a multi-statement SQL script from a raw resource file.
     * Each statement must be separated by a semicolon (;).
     * This version correctly handles multi-line statements.
     */
    private void runSqlFromRaw(SQLiteDatabase db, int resId) {
        try (InputStream inputStream = context.getResources().openRawResource(resId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Ignore comments and empty lines
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sqlBuilder.append(line);
                // If the line ends with a semicolon, it's the end of a statement
                if (line.endsWith(";")) {
                    try {
                        db.execSQL(sqlBuilder.toString());
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Failed to execute SQL: " + sqlBuilder.toString(), e);
                    }
                    // Reset for the next statement
                    sqlBuilder.setLength(0);
                } else {
                    // Append a space for statements that span multiple lines
                    sqlBuilder.append(" ");
                }
            }
        } catch (IOException e) {
            // This is a fatal error during setup, so crashing is acceptable.
            throw new RuntimeException("Error reading or executing SQL file.", e);
        }
    }


    public ArrayList<String> getAllSemesterNames() {
        ArrayList<String> semesterNames = new ArrayList<>();
        // Fixed query to order correctly
        String selectQuery = "SELECT ten_hoc_ky FROM hoc_ky ORDER BY nam_hoc, id";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor != null && cursor.moveToFirst()) {
                int tenHocKyIndex = cursor.getColumnIndexOrThrow("ten_hoc_ky");
                do {
                    semesterNames.add(cursor.getString(tenHocKyIndex));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while getting semester names", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return semesterNames;
    }

    /**
     * Gets all subjects regardless of enrollment for demonstration purposes.
     * This avoids the empty list issue caused by the empty 'enrollments' table.
     * @param semesterName The name of the semester (e.g., "Học kỳ 1")
     * @return A list of Subject objects.
     */
    public ArrayList<Subject> getSubjectsBySemester(String semesterName) {
        ArrayList<Subject> subjectList = new ArrayList<>();
        // TEMPORARY FIX: This query gets subjects based on the 'hoc_ky' field in 'mon_hoc' table
        // instead of joining with the empty 'enrollments' table.
        String selectQuery = "SELECT m.* FROM mon_hoc m " +
                             "INNER JOIN hoc_ky hk ON m.hoc_ky = hk.id " +
                             "WHERE hk.ten_hoc_ky = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, new String[]{semesterName});

            if (cursor != null && cursor.moveToFirst()) {
                int maHpIndex = cursor.getColumnIndexOrThrow("ma_hp");
                int tenHpIndex = cursor.getColumnIndexOrThrow("ten_hp");
                int soTinChiIndex = cursor.getColumnIndexOrThrow("so_tin_chi");
                int loaiHpIndex = cursor.getColumnIndexOrThrow("loai_hp");

                do {
                    Subject subject = new Subject();
                    subject.maHp = cursor.getString(maHpIndex);
                    subject.tenHp = cursor.getString(tenHpIndex);
                    subject.soTinChi = cursor.getInt(soTinChiIndex);
                    subject.loaiHp = cursor.getString(loaiHpIndex);
                    subjectList.add(subject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while trying to get subjects by semester", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return subjectList;
    }

    public ArrayList<Deadline> getDeadlinesByMaHp(String maHp) {
        ArrayList<Deadline> deadlineList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        // Using epoch time (INTEGER) requires parsing as long, not Date string
        
        try {
             // Corrected query to use proper column names
            String selectQuery = "SELECT * FROM deadlines WHERE ma_hp = ?";
            cursor = db.rawQuery(selectQuery, new String[]{maHp});

            if (cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow("id");
                int titleIndex = cursor.getColumnIndexOrThrow("title");
                int descriptionIndex = cursor.getColumnIndexOrThrow("description");
                int dueDateIndex = cursor.getColumnIndexOrThrow("due_date");

                do {
                    // Create deadline from new schema
                    // Note: The Deadline model needs to be updated to handle epoch time (long)
                    // For now, we are just creating a dummy object to avoid crashing
                    String title = cursor.getString(titleIndex);
                    String description = cursor.getString(descriptionIndex);
                    long dueDate = cursor.getLong(dueDateIndex);
                    
                    // The Deadline constructor (String, String, Date, Date) is now incorrect.
                    // This will need to be fixed later. For now, creating with dummy dates.
                    Deadline deadline = new Deadline(title, description, new Date(dueDate * 1000), new Date(dueDate * 1000));
                    deadline.setMaDl(cursor.getInt(idIndex));

                    deadlineList.add(deadline);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
             Log.e("DatabaseHelper", "Error getting deadlines", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return deadlineList;
    }
    
    public void addSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ma_hp", subject.maHp);
        values.put("ten_hp", subject.tenHp);
        values.put("so_tin_chi", subject.soTinChi);
        values.put("loai_hp", subject.loaiHp);
        db.insert("mon_hoc", null, values);
    }
}
