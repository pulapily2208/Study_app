package com.example.study_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.study_app.R;
import com.example.study_app.ui.Dealine.Models.Deadline;
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
    private static final int DB_VERSION = 3; // Keep version 3

    private final Context context;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        runSqlFromRaw(db, R.raw.study_app);
        // Add new columns that are not in the initial SQL script
        // This ensures fresh installs have the correct schema
        addMissingColumns(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This handles upgrades from previous versions
        if (oldVersion < 3) {
            addMissingColumns(db);
        }
    }

    // A dedicated method to add columns to prevent errors
    private void addMissingColumns(SQLiteDatabase db) {
        try {
            db.execSQL("ALTER TABLE mon_hoc ADD COLUMN phong_hoc TEXT");
            db.execSQL("ALTER TABLE mon_hoc ADD COLUMN ngay_bat_dau TEXT");
            db.execSQL("ALTER TABLE mon_hoc ADD COLUMN ngay_ket_thuc TEXT");
            db.execSQL("ALTER TABLE mon_hoc ADD COLUMN so_tuan_hoc INTEGER");
            db.execSQL("ALTER TABLE mon_hoc ADD COLUMN gio_bat_dau TEXT");
            db.execSQL("ALTER TABLE mon_hoc ADD COLUMN gio_ket_thuc TEXT");
            db.execSQL("ALTER TABLE mon_hoc ADD COLUMN giang_vien TEXT");
            db.execSQL("ALTER TABLE mon_hoc ADD COLUMN ghi_chu TEXT");
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding columns, maybe they already exist.", e);
        }
    }


    // Method to add a new subject
    public void addSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ma_hp", subject.maHp);
        values.put("ten_hp", subject.tenHp);
        values.put("so_tin_chi", subject.soTinChi);
        values.put("phong_hoc", subject.phongHoc);
        values.put("loai_hp", subject.loaiHp);
        values.put("ngay_bat_dau", subject.ngayBatDau);
        values.put("ngay_ket_thuc", subject.ngayKetThuc);
        values.put("so_tuan_hoc", subject.soTuanHoc);
        values.put("gio_bat_dau", subject.gioBatDau);
        values.put("gio_ket_thuc", subject.gioKetThuc);
        values.put("giang_vien", subject.giangVien);
        values.put("ghi_chu", subject.ghiChu);

        db.insert("mon_hoc", null, values);
        // No need to close db, the helper manages its lifecycle.
    }


    private void runSqlFromRaw(SQLiteDatabase db, int resId) {
        try (InputStream inputStream = context.getResources().openRawResource(resId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sqlBuilder.append(line);
                sqlBuilder.append("\n");
            }

            String sqlScript = sqlBuilder.toString();
            String[] statements = sqlScript.split(";");

            db.beginTransaction();
            try {
                for (String statement : statements) {
                    String trimmedStatement = statement.trim();
                    if (!trimmedStatement.isEmpty()) {
                        db.execSQL(trimmedStatement + ";");
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading or executing SQL file.", e);
        }
    }

    public ArrayList<Subject> getAllSubjects() {
        ArrayList<Subject> subjectList = new ArrayList<>();
        String selectQuery = "SELECT * FROM mon_hoc";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                // Get column indices once, before the loop
                int maHpIndex = cursor.getColumnIndexOrThrow("ma_hp");
                int tenHpIndex = cursor.getColumnIndexOrThrow("ten_hp");
                int soTinChiIndex = cursor.getColumnIndexOrThrow("so_tin_chi");
                int loaiHpIndex = cursor.getColumnIndexOrThrow("loai_hp");
                int phongHocIndex = cursor.getColumnIndexOrThrow("phong_hoc");
                int ngayBatDauIndex = cursor.getColumnIndexOrThrow("ngay_bat_dau");
                int ngayKetThucIndex = cursor.getColumnIndexOrThrow("ngay_ket_thuc");
                int soTuanHocIndex = cursor.getColumnIndexOrThrow("so_tuan_hoc");
                int gioBatDauIndex = cursor.getColumnIndexOrThrow("gio_bat_dau");
                int gioKetThucIndex = cursor.getColumnIndexOrThrow("gio_ket_thuc");
                int giangVienIndex = cursor.getColumnIndexOrThrow("giang_vien");
                int ghiChuIndex = cursor.getColumnIndexOrThrow("ghi_chu");

                do {
                    Subject subject = new Subject(); // Uses the no-argument constructor

                    // Populate ALL fields from the cursor
                    subject.maHp = cursor.getString(maHpIndex);
                    subject.tenHp = cursor.getString(tenHpIndex);
                    subject.soTinChi = cursor.getInt(soTinChiIndex);
                    subject.loaiHp = cursor.getString(loaiHpIndex);
                    subject.phongHoc = cursor.getString(phongHocIndex);
                    subject.ngayBatDau = cursor.getString(ngayBatDauIndex);
                    subject.ngayKetThuc = cursor.getString(ngayKetThucIndex);
                    subject.soTuanHoc = cursor.getInt(soTuanHocIndex);
                    subject.gioBatDau = cursor.getString(gioBatDauIndex);
                    subject.gioKetThuc = cursor.getString(gioKetThucIndex);
                    subject.giangVien = cursor.getString(giangVienIndex);
                    subject.ghiChu = cursor.getString(ghiChuIndex);

                    subjectList.add(subject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error while trying to get subjects from database", e);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

        try {
            String selectQuery = "SELECT * FROM deadlines WHERE ma_hp = ?";
            cursor = db.rawQuery(selectQuery, new String[]{maHp});

            if (cursor.moveToFirst()) {
                int maDlIndex = cursor.getColumnIndexOrThrow("ma_dl");
                int tieuDeIndex = cursor.getColumnIndexOrThrow("tieu_de");
                int noiDungIndex = cursor.getColumnIndexOrThrow("noi_dung");
                int ngayBatDauIndex = cursor.getColumnIndexOrThrow("ngay_bat_dau");
                int ngayKetThucIndex = cursor.getColumnIndexOrThrow("ngay_ket_thuc");

                do {
                    int maDl = cursor.getInt(maDlIndex);
                    String tieuDe = cursor.getString(tieuDeIndex);
                    String noiDung = cursor.getString(noiDungIndex);
                    String ngayBatDauStr = cursor.getString(ngayBatDauIndex);
                    String ngayKetThucStr = cursor.getString(ngayKetThucIndex);

                    try {
                        Date ngayBatDau = dateFormat.parse(ngayBatDauStr);
                        Date ngayKetThuc = dateFormat.parse(ngayKetThucStr);

                        Deadline deadline = new Deadline(tieuDe, noiDung, ngayBatDau, ngayKetThuc);
                        deadline.setMaDl(maDl);

                        deadlineList.add(deadline);
                    } catch (ParseException e) {
                        Log.e("DatabaseHelper", "Failed to parse date string: " + ngayBatDauStr + " or " + ngayKetThucStr, e);
                    }

                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return deadlineList;
    }
}
