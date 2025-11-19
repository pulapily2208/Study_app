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
    private static final int DB_VERSION = 7; // Tăng phiên bản để migration

    private final Context context;
    // Định dạng ngày và giờ
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
        // Migration an toàn: thêm cột thiếu vào bảng mon_hoc nếu cần
        if (oldVersion < 7) {
            Log.i("DatabaseHelper", "Đang thực hiện migration từ phiên bản " + oldVersion + " lên " + newVersion);
            try {
                // Lấy danh sách các cột hiện tại trong bảng mon_hoc
                List<String> existingColumns = getColumns(db, "mon_hoc");
                
                // Danh sách các cột cần có
                String[] requiredColumns = {
                    "giang_vien", "phong_hoc", "ngay_bat_dau", "ngay_ket_thuc",
                    "gio_bat_dau", "gio_ket_thuc", "ghi_chu"
                };
                
                // Thêm từng cột nếu chưa tồn tại
                for (String column : requiredColumns) {
                    if (!existingColumns.contains(column)) {
                        try {
                            String alterQuery = "ALTER TABLE mon_hoc ADD COLUMN " + column + " TEXT";
                            db.execSQL(alterQuery);
                            Log.i("DatabaseHelper", "Đã thêm cột: " + column);
                        } catch (Exception e) {
                            Log.e("DatabaseHelper", "Lỗi khi thêm cột " + column, e);
                        }
                    }
                }
                
                Log.i("DatabaseHelper", "Migration hoàn tất thành công");
            } catch (Exception e) {
                Log.e("DatabaseHelper", "Lỗi trong quá trình migration", e);
            }
        }
    }

    // Helper method để lấy danh sách cột từ một bảng
    private List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> columns = new ArrayList<>();
        Cursor cursor = null;
        try {
            // Safe migration for version 7: add missing columns to mon_hoc table
            if (oldVersion < 7) {
                Log.i("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion);
                
                // Get existing columns in mon_hoc table
                ArrayList<String> existingCols = getColumns(db, "mon_hoc");
                
                // Add missing columns if they don't exist
                if (!existingCols.contains("giang_vien")) {
                    try {
                        db.execSQL("ALTER TABLE mon_hoc ADD COLUMN giang_vien TEXT");
                        Log.i("DatabaseHelper", "Added column: giang_vien");
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error adding column giang_vien", e);
                    }
                }
                
                if (!existingCols.contains("phong_hoc")) {
                    try {
                        db.execSQL("ALTER TABLE mon_hoc ADD COLUMN phong_hoc TEXT");
                        Log.i("DatabaseHelper", "Added column: phong_hoc");
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error adding column phong_hoc", e);
                    }
                }
                
                if (!existingCols.contains("ngay_bat_dau")) {
                    try {
                        db.execSQL("ALTER TABLE mon_hoc ADD COLUMN ngay_bat_dau TEXT");
                        Log.i("DatabaseHelper", "Added column: ngay_bat_dau");
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error adding column ngay_bat_dau", e);
                    }
                }
                
                if (!existingCols.contains("ngay_ket_thuc")) {
                    try {
                        db.execSQL("ALTER TABLE mon_hoc ADD COLUMN ngay_ket_thuc TEXT");
                        Log.i("DatabaseHelper", "Added column: ngay_ket_thuc");
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error adding column ngay_ket_thuc", e);
                    }
                }
                
                if (!existingCols.contains("gio_bat_dau")) {
                    try {
                        db.execSQL("ALTER TABLE mon_hoc ADD COLUMN gio_bat_dau TEXT");
                        Log.i("DatabaseHelper", "Added column: gio_bat_dau");
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error adding column gio_bat_dau", e);
                    }
                }
                
                if (!existingCols.contains("gio_ket_thuc")) {
                    try {
                        db.execSQL("ALTER TABLE mon_hoc ADD COLUMN gio_ket_thuc TEXT");
                        Log.i("DatabaseHelper", "Added column: gio_ket_thuc");
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error adding column gio_ket_thuc", e);
                    }
                }
                
                if (!existingCols.contains("ghi_chu")) {
                    try {
                        db.execSQL("ALTER TABLE mon_hoc ADD COLUMN ghi_chu TEXT");
                        Log.i("DatabaseHelper", "Added column: ghi_chu");
                    } catch (Exception e) {
                        Log.e("DatabaseHelper", "Error adding column ghi_chu", e);
                    }
                }
                
                Log.i("DatabaseHelper", "Database upgrade completed successfully");
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error during database upgrade", e);
        }
    }

    // Helper method to get existing columns in a table using PRAGMA table_info
    private ArrayList<String> getColumns(SQLiteDatabase db, String tableName) {
        ArrayList<String> columns = new ArrayList<>();
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
            if (cursor != null) {
                cursor.close();
            }
        }
        return columns;
    }

    // Helper để chuyển đổi chuỗi ngày thành đối tượng Date
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Log.e("DatabaseHelper", "Lỗi khi phân tích ngày: " + dateStr, e);
            return null;
        }
    }

    // Helper để chuyển đổi chuỗi giờ thành đối tượng Date
    private Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            return timeFormat.parse(timeStr);
        } catch (ParseException e) {
            Log.e("DatabaseHelper", "Lỗi khi phân tích giờ: " + timeStr, e);
            return null;
        }
    }

    // Helper để định dạng đối tượng Date thành chuỗi ngày
    private String formatDate(Date date) {
        if (date == null) return null;
        return dateFormat.format(date);
    }

    // Helper để định dạng đối tượng Date thành chuỗi giờ
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
                        Log.e("DatabaseHelper", "Thất bại khi thực thi SQL: " + sqlBuilder.toString(), e);
                    }
                    sqlBuilder.setLength(0);
                } else {
                    sqlBuilder.append(" ");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi đọc hoặc thực thi file SQL.", e);
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
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách tên học kỳ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return semesterNames;
    }
    public int getSemesterIdByName(String semesterName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int semesterId = -1; // Mặc định là ID không hợp lệ
        try {
            cursor = db.query("hoc_ky", new String[]{"id"}, "ten_hoc_ky = ?", new String[]{semesterName}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                semesterId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy ID học kỳ theo tên", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
                    subject.tenHk = semesterName; // Gán tên học kỳ
                    subjectList.add(subject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách môn học theo học kỳ. Đảm bảo tất cả các cột tồn tại trong bảng 'mon_hoc'.", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
            Log.e("DatabaseHelper", "Lỗi khi lấy thông tin môn học theo mã học phần. Đảm bảo tất cả các cột tồn tại trong bảng 'mon_hoc'.", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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
            Log.i("DatabaseHelper", "Successfully added subject with ma_hp: " + subject.maHp);
            return newRowId;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to add subject with ma_hp: " + subject.maHp, e);
            return -1;
        }
    }

    public void enrollSubjectInSemester(String maHp, int semesterId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", 1); // Giả định user_id mặc định là 1 hiện tại
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
                    // Giả định constructor Deadline nhận title, description, startDate, và endDate
                    Deadline deadline = new Deadline(title, description, new Date(dueDate * 1000), new Date(dueDate * 1000));
                    deadline.setMaDl(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    deadlineList.add(deadline);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
             Log.e("DatabaseHelper", "Lỗi khi lấy danh sách deadline", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return deadlineList;
    }
}
