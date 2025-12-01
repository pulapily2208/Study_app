package com.example.study_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class ScoreDao extends SQLiteOpenHelper {
    private static final String DB_NAME = "study_app.db";
    private static final int DB_VERSION = 11;

    private final Context context;

    public ScoreDao(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS diem_mon_hoc (\n" +
                        "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                        "    ma_hp TEXT NOT NULL,\n" +
                        "    diem_chuyen_can REAL,\n" +
                        "    diem_giua_ki REAL,\n" +
                        "    diem_cuoi_ki REAL,\n" +
                        "    gpa REAL,\n" +
                        "    FOREIGN KEY (ma_hp) REFERENCES mon_hoc(ma_hp) ON DELETE CASCADE\n" +
                        ");"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        if (oldVersion < 11) {
            db.execSQL("ALTER TABLE deadline RENAME COLUMN reminder_time TO reminder_time_old;");
            db.execSQL("ALTER TABLE deadline ADD COLUMN reminder_time TEXT;");
            db.execSQL("UPDATE deadline SET reminder_time = reminder_time_old;");
            db.execSQL("ALTER TABLE deadline DROP COLUMN reminder_time_old;");
        }
        db.execSQL("DROP TABLE IF EXISTS hoc_ky");
        db.execSQL("DROP TABLE IF EXISTS khoa");
        db.execSQL("DROP TABLE IF EXISTS hoc_phan_tu_chon");
        db.execSQL("DROP TABLE IF EXISTS mon_hoc");
        db.execSQL("DROP TABLE IF EXISTS hoc_phan_tien_quyet");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS deadline");
        db.execSQL("DROP TABLE IF EXISTS notes");
        db.execSQL("DROP TABLE IF EXISTS attachments");
        db.execSQL("DROP TABLE IF EXISTS timetable_sessions");
        db.execSQL("DROP TABLE IF EXISTS enrollments");
        db.execSQL("DROP TABLE IF EXISTS notification_schedules");
        db.execSQL("DROP TABLE IF EXISTS mon_hoc_tu_chon_map");
        onCreate(db);
    }


    public boolean saveScore(String string, Float cc, Float gk, Float ck, Float gpa) {
        SQLiteDatabase db = getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM diem_mon_hoc WHERE ma_hp = ?", new String[]{string});

        ContentValues values = new ContentValues();
        values.put("ma_hp", string);
        values.put("diem_chuyen_can", cc);
        values.put("diem_giua_ki", gk);
        values.put("diem_cuoi_ki", ck);
        if (gpa != null) {
            float roundedGpa = Math.round(gpa * 10) / 10.0f;
            values.put("gpa", roundedGpa);
        } else {
            values.putNull("gpa");
        }

        boolean result;

        if (cursor.getCount() > 0) {
            result = db.update("diem_mon_hoc", values, "ma_hp = ?", new String[]{string}) > 0;
        } else {
            result = db.insert("diem_mon_hoc", null, values) != -1;
        }

        cursor.close();
        db.close();
        return result;

    }

    public Cursor getScore(String maMon) {

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM diem_mon_hoc WHERE ma_hp = ?", new String[]{maMon});
        return cursor;
    }

    public Float getGpa(String maMon) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT gpa FROM diem_mon_hoc WHERE ma_hp = ?", new String[]{maMon});
        Float gpa = null;
        if (cursor.moveToFirst()) {
            int gpaIndex = cursor.getColumnIndex("gpa");
            if (!cursor.isNull(gpaIndex)) {
                gpa = cursor.getFloat(gpaIndex);
            }
        }
        cursor.close();
        return gpa;
    }
}
