package com.example.study_app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * DAO điểm sử dụng chung DatabaseHelper để tránh tạo thêm SQLiteOpenHelper
 * riêng cho cùng một cơ sở dữ liệu, giảm rủi ro rò rỉ kết nối và xung đột
 * lược đồ khi nâng cấp.
 */
public class ScoreDao {
    private final DatabaseHelper dbHelper;

    public ScoreDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        ensureTable();
    }

    private void ensureTable() {
        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL(
                    "CREATE TABLE IF NOT EXISTS diem_mon_hoc (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "ma_hp TEXT NOT NULL," +
                            "diem_chuyen_can REAL," +
                            "diem_giua_ki REAL," +
                            "diem_cuoi_ki REAL," +
                            "gpa REAL," +
                            "FOREIGN KEY (ma_hp) REFERENCES mon_hoc(ma_hp) ON DELETE CASCADE" +
                            ");");
        } catch (Exception e) {
            Log.e("ScoreDao", "ensureTable error", e);
        }
    }

    public boolean saveScore(String maHp, Float cc, Float gk, Float ck, Float gpa) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean result;
        try (Cursor cursor = db.rawQuery("SELECT 1 FROM diem_mon_hoc WHERE ma_hp = ?", new String[] { maHp })) {
            ContentValues values = new ContentValues();
            values.put("ma_hp", maHp);
            values.put("diem_chuyen_can", cc);
            values.put("diem_giua_ki", gk);
            values.put("diem_cuoi_ki", ck);
            if (gpa != null) {
                float roundedGpa = Math.round(gpa * 10) / 10.0f;
                values.put("gpa", roundedGpa);
            } else {
                values.putNull("gpa");
            }

            if (cursor != null && cursor.moveToFirst()) {
                result = db.update("diem_mon_hoc", values, "ma_hp = ?", new String[] { maHp }) > 0;
            } else {
                result = db.insert("diem_mon_hoc", null, values) != -1;
            }
        }
        return result;
    }

    /**
     * Trả về GPA của một môn; đóng Cursor ngay sau khi đọc để tránh rò rỉ.
     */
    public Float getGpa(String maMon) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Float gpa = null;
        try (Cursor cursor = db.rawQuery("SELECT gpa FROM diem_mon_hoc WHERE ma_hp = ?", new String[] { maMon })) {
            if (cursor != null && cursor.moveToFirst()) {
                int gpaIndex = cursor.getColumnIndex("gpa");
                if (gpaIndex >= 0 && !cursor.isNull(gpaIndex)) {
                    gpa = cursor.getFloat(gpaIndex);
                }
            }
        }
        return gpa;
    }

    /**
     * Lấy chi tiết điểm của một môn học.
     */
    public ScoreDetails getScoreDetails(String maMon) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ScoreDetails details = null;
        try (Cursor cursor = db.rawQuery(
                "SELECT diem_chuyen_can, diem_giua_ki, diem_cuoi_ki, gpa FROM diem_mon_hoc WHERE ma_hp = ?",
                new String[] { maMon })) {
            if (cursor != null && cursor.moveToFirst()) {
                details = new ScoreDetails();
                int ccIdx = cursor.getColumnIndex("diem_chuyen_can");
                int gkIdx = cursor.getColumnIndex("diem_giua_ki");
                int ckIdx = cursor.getColumnIndex("diem_cuoi_ki");
                int gpaIdx = cursor.getColumnIndex("gpa");
                if (ccIdx >= 0 && !cursor.isNull(ccIdx))
                    details.cc = cursor.getFloat(ccIdx);
                if (gkIdx >= 0 && !cursor.isNull(gkIdx))
                    details.gk = cursor.getFloat(gkIdx);
                if (ckIdx >= 0 && !cursor.isNull(ckIdx))
                    details.ck = cursor.getFloat(ckIdx);
                if (gpaIdx >= 0 && !cursor.isNull(gpaIdx))
                    details.gpa = cursor.getFloat(gpaIdx);
            }
        }
        return details;
    }

    public static class ScoreDetails {
        public Float cc;
        public Float gk;
        public Float ck;
        public Float gpa;
    }

    /**
     * Lấy toàn bộ GPA của các môn đã có.
     */
    public java.util.Map<String, Float> getAllGpaMap() {
        java.util.Map<String, Float> map = new java.util.HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery("SELECT ma_hp, gpa FROM diem_mon_hoc", null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String maHp = cursor.getString(0);
                    int gpaIdx = cursor.getColumnIndex("gpa");
                    if (gpaIdx >= 0 && !cursor.isNull(gpaIdx)) {
                        map.put(maHp, cursor.getFloat(gpaIdx));
                    }
                } while (cursor.moveToNext());
            }
        }
        return map;
    }
}
