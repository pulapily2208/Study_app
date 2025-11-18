package com.example.study_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.study_app.R;
import com.example.study_app.ui.Subject.Model.Subject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_app.db";
    private static final int DB_VERSION = 1;

    // Table and column names for mon_hoc
    private static final String TABLE_MON_HOC = "mon_hoc";
    private static final String KEY_MA_HP = "ma_hp";
    private static final String KEY_TEN_HP = "ten_hp";
    private static final String KEY_SO_TIN_CHI = "so_tin_chi";
    private static final String KEY_LOAI_HP = "loai_hp";


    private Context context;

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
        // This is a basic implementation. For a real app, you would need a robust
        // migration strategy that doesn't lose user data.
        // For our debugging purpose, we can try to drop all tables.
        // A better way would be to query sqlite_master for all table names and drop them.
        try {
            // This is a simplification. A full implementation is more complex.
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MON_HOC);
            db.execSQL("DROP TABLE IF EXISTS hoc_ky");
            db.execSQL("DROP TABLE IF EXISTS khoa");
            db.execSQL("DROP TABLE IF EXISTS hoc_phan_tu_chon");
            db.execSQL("DROP TABLE IF EXISTS hoc_phan_tien_quyet");
            db.execSQL("DROP TABLE IF EXISTS users");
            db.execSQL("DROP TABLE IF EXISTS deadlines");
            db.execSQL("DROP TABLE IF EXISTS notes");
            db.execSQL("DROP TABLE IF EXISTS attachments");
            db.execSQL("DROP TABLE IF EXISTS timetable_sessions");
            db.execSQL("DROP TABLE IF EXISTS enrollments");
            db.execSQL("DROP TABLE IF EXISTS notification_schedules");
            db.execSQL("DROP TABLE IF EXISTS mon_hoc_tu_chon_map");
        } catch (Exception e) {
            // Ignore errors during drop
        }
        onCreate(db);
    }

    private void runSqlFromRaw(SQLiteDatabase db, int resId) {
        InputStream input = context.getResources().openRawResource(resId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        StringBuilder sql = new StringBuilder();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                // Ignore comments and empty lines
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                    continue;
                }
                
                // Append the line and a space to separate statements
                sql.append(trimmedLine);
                sql.append(" ");

                if (trimmedLine.endsWith(";")) {
                    // Execute the accumulated SQL statement
                    db.execSQL(sql.toString());
                    // Reset for the next statement
                    sql.setLength(0);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading SQL file", e);
        }
    }

    // --- CRUD Operations for Subjects ---

    /**
     * Adds a new subject to the mon_hoc table.
     * Note: This only saves fields present in the 'mon_hoc' table.
     * Other fields from the Subject object (like times, location) are not saved here.
     * @param subject The subject to add.
     */
    public void addSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_MA_HP, subject.maMon);
        values.put(KEY_TEN_HP, subject.tenMon);
        values.put(KEY_SO_TIN_CHI, subject.soTinChi);
        values.put(KEY_LOAI_HP, subject.loaiMon);
        // ghiChu, phongHoc, giangVien etc. are not part of the 'mon_hoc' table schema
        // and will be handled separately if needed.

        // Inserting Row
        db.insert(TABLE_MON_HOC, null, values);
    }

    /**
     * Gets all subjects from the mon_hoc table.
     * @return A list of all subjects.
     */
    public ArrayList<Subject> getAllSubjects() {
        ArrayList<Subject> subjectList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_MON_HOC;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Column indices
        int maHpIndex = cursor.getColumnIndex(KEY_MA_HP);
        int tenHpIndex = cursor.getColumnIndex(KEY_TEN_HP);
        int soTinChiIndex = cursor.getColumnIndex(KEY_SO_TIN_CHI);
        int loaiHpIndex = cursor.getColumnIndex(KEY_LOAI_HP);

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Subject subject = new Subject();
                if(maHpIndex != -1) subject.maMon = cursor.getString(maHpIndex);
                if(tenHpIndex != -1) subject.tenMon = cursor.getString(tenHpIndex);
                if(soTinChiIndex != -1) subject.soTinChi = cursor.getInt(soTinChiIndex);
                if(loaiHpIndex != -1) subject.loaiMon = cursor.getString(loaiHpIndex);

                // Other fields in the Subject object will remain default/null
                // as they are not stored in the 'mon_hoc' table.

                subjectList.add(subject);
            } while (cursor.moveToNext());
        }

        cursor.close();

        return subjectList;
    }
}
