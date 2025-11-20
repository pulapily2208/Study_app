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
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_app.db";
    private static final int DB_VERSION = 7; 

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
            Log.i("DatabaseHelper", "Upgrading from version " + oldVersion + " to " + newVersion);
            try {
                List<String> existingColumns = getColumns(db, "mon_hoc");
                
                String[] requiredColumns = {
                    "giang_vien", "phong_hoc", "ngay_bat_dau", "ngay_ket_thuc",
                    "gio_bat_dau", "gio_ket_thuc", "ghi_chu"
                };
                
                for (String column : requiredColumns) {
                    if (!existingColumns.contains(column)) {
                        try {
                            String alterQuery = "ALTER TABLE mon_hoc ADD COLUMN " + column + " TEXT";
                            db.execSQL(alterQuery);
                            Log.i("DatabaseHelper", "Added column: " + column);
                        } catch (Exception e) {
                            Log.e("DatabaseHelper", "Error adding column " + column, e);
                        }
                    }
                }
                Log.i("DatabaseHelper", "Migration completed");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Migration failed", e);
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
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting columns for table: " + tableName, e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return columns;
    }

    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    private Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            return timeFormat.parse(timeStr);
        } catch (ParseException e) {
            return null;
        }
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
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sqlBuilder.append(line);
                if (line.endsWith(";")) {
                    try {
                        db.execSQL(sqlBuilder.toString());
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "SQL Error: " + sqlBuilder.toString(), e);
                    }
                    sqlBuilder.setLength(0);
                } else {
                    sqlBuilder.append(" ");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading SQL file", e);
        }
    }

    public ArrayList<String> getAllSemesterNames() {
        ArrayList<String> semesterNames = new ArrayList<>();
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
            Log.e("DatabaseHelper", "Error getting semesters", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return semesterNames;
    }

    public int getSemesterIdByName(String semesterName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int semesterId = -1; 
        try {
            cursor = db.query("hoc_ky", new String[]{"id"}, "ten_hoc_ky = ?", new String[]{semesterName}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                semesterId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting semester ID", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return semesterId;
    }

    public ArrayList<Subject> getSubjectsBySemester(String semesterName) {
        ArrayList<Subject> subjectList = new ArrayList<>();
        String selectQuery = "SELECT m.* FROM mon_hoc m " +
                "INNER JOIN enrollments e ON m.ma_hp = e.ma_hp " +
                "INNER JOIN hoc_ky hk ON e.hoc_ky = hk.id " +
                "WHERE hk.ten_hoc_ky = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, new String[]{semesterName});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Subject subject = new Subject();
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
                    subject.tenHk = semesterName; 
                    subjectList.add(subject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting subjects", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return subjectList;
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
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting subject detail", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return subject;
    }

    public long addSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ma_hp", subject.maHp);
        values.put("ten_hp", subject.tenHp);
        values.put("so_tin_chi", subject.soTc);
        values.put("loai_hp", subject.loaiMon);
        values.put("giang_vien", subject.tenGv);
        values.put("phong_hoc", subject.phongHoc);
        values.put("ngay_bat_dau", formatDate(subject.ngayBatDau));
        values.put("ngay_ket_thuc", formatDate(subject.ngayKetThuc));
        values.put("gio_bat_dau", formatTime(subject.gioBatDau));
        values.put("gio_ket_thuc", formatTime(subject.gioKetThuc));
        values.put("ghi_chu", subject.ghiChu);
        values.put("color_tag", subject.mauSac);
        
        try {
            long newRowId = db.insertOrThrow("mon_hoc", null, values);
            return newRowId;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to add subject", e);
            return -1;
        }
    }

    public void enrollSubjectInSemester(String maHp, int semesterId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", 1); 
        values.put("ma_hp", maHp);
        values.put("hoc_ky", semesterId);
        db.insert("enrollments", null, values);
    }

    public int updateSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ten_hp", subject.tenHp);
        values.put("so_tin_chi", subject.soTc);
        values.put("loai_hp", subject.loaiMon);
        values.put("giang_vien", subject.tenGv);
        values.put("phong_hoc", subject.phongHoc);
        values.put("ngay_bat_dau", formatDate(subject.ngayBatDau));
        values.put("ngay_ket_thuc", formatDate(subject.ngayKetThuc));
        values.put("gio_bat_dau", formatTime(subject.gioBatDau));
        values.put("gio_ket_thuc", formatTime(subject.gioKetThuc));
        values.put("ghi_chu", subject.ghiChu);
        values.put("color_tag", subject.mauSac);
        return db.update("mon_hoc", values, "ma_hp = ?", new String[]{subject.maHp});
    }

    public void deleteSubject(String maHp) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("mon_hoc", "ma_hp = ?", new String[]{maHp});
    }

    public ArrayList<Deadline> getDeadlinesByMaHp(String maHp) {
        ArrayList<Deadline> deadlineList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String selectQuery = "SELECT * FROM deadlines WHERE ma_hp = ?";
            cursor = db.rawQuery(selectQuery, new String[]{maHp});

            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    long dueDate = cursor.getLong(cursor.getColumnIndexOrThrow("due_date"));
                    
                    Deadline deadline = new Deadline(title, description, new Date(dueDate * 1000), new Date(dueDate * 1000), 0);
                    
                    deadline.setMaDl(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    deadlineList.add(deadline);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
             Log.e("DatabaseHelper", "Error getting deadlines", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return deadlineList;
    }
}
