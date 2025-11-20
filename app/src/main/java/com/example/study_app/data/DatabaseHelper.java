package com.example.study_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.Models.*;
import com.example.study_app.ui.Subject.Model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_app.db";
    private static final int DB_VERSION = 8; // tăng version lên 8 để chạy migration
    private final Context context;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

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
        if (oldVersion < 7) {
            // giữ nguyên migration cũ nếu cần
        }

        if (oldVersion < 8) {
            try {
                List<String> existingColumns = getColumns(db, "mon_hoc");
                if (!existingColumns.contains("so_tuan")) {
                    db.execSQL("ALTER TABLE mon_hoc ADD COLUMN so_tuan INTEGER DEFAULT 15");
                    Log.i("DatabaseHelper", "Added column so_tuan with default 15");
                }
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Error adding so_tuan column", e);
            }
        }
    }

    private List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex("name");
                if (nameIndex != -1) {
                    do {
                        columns.add(cursor.getString(nameIndex));
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return columns;
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try { return dateFormat.parse(dateStr); } catch (ParseException e) { return null; }
    }

    private Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try { return timeFormat.parse(timeStr); } catch (ParseException e) { return null; }
    }

    private String formatDate(Date date) {
        if (date == null) return null;
        return dateFormat.format(date);
    }

    private String formatTime(Date time) {
        if (time == null) return null;
        return timeFormat.format(time);
    }

    private void runSqlFromRaw(SQLiteDatabase db, int resId) {
        try (InputStream inputStream = context.getResources().openRawResource(resId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) continue;
                sqlBuilder.append(line);
                if (line.endsWith(";")) {
                    try { db.execSQL(sqlBuilder.toString()); } catch (Exception e) { Log.e("DatabaseHelper", "SQL Error: " + sqlBuilder.toString(), e); }
                    sqlBuilder.setLength(0);
                } else {
                    sqlBuilder.append(" ");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading SQL file", e);
        }
    }

    public Subject getSubjectByMaHp(String maHp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Subject subject = null;
        try {
            cursor = db.query("mon_hoc", null, "ma_hp = ?", new String[]{maHp}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                subject = new Subject();
                subject.maHp = cursor.getString(cursor.getColumnIndexOrThrow("ma_hp"));
                subject.tenHp = cursor.getString(cursor.getColumnIndexOrThrow("ten_hp"));
                subject.soTc = cursor.getInt(cursor.getColumnIndexOrThrow("so_tin_chi"));
                subject.loaiMon = cursor.getString(cursor.getColumnIndexOrThrow("loai_hp"));
                subject.tenGv = cursor.getString(cursor.getColumnIndexOrThrow("giang_vien"));
                subject.phongHoc = cursor.getString(cursor.getColumnIndexOrThrow("phong_hoc"));
                subject.ngayBatDau = parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_bat_dau")));
                subject.ngayKetThuc = parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc")));
                subject.gioBatDau = parseTime(cursor.getString(cursor.getColumnIndexOrThrow("gio_bat_dau")));
                subject.gioKetThuc = parseTime(cursor.getString(cursor.getColumnIndexOrThrow("gio_ket_thuc")));
                subject.ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu"));
                subject.mauSac = cursor.getString(cursor.getColumnIndexOrThrow("color_tag"));

                // Lấy số tuần từ DB (so_tuan)
                int soTuan = 0;
                int soTuanIndex = cursor.getColumnIndex("so_tuan");
                if (soTuanIndex != -1) {
                    soTuan = cursor.getInt(soTuanIndex);
                }
                subject.soTuanHoc = soTuan > 0 ? soTuan : 15; // default 15 tuần
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting subject detail", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return subject;
    }

    // --- Các phương thức khác: getDeadlinesByMaHp, addSubject, updateSubject, deleteSubject ---
    // Bạn giữ nguyên code cũ, chỉ cần chắc chắn AdapterDeadLine và MainDeadLine gọi subject.soTuanHoc
}
