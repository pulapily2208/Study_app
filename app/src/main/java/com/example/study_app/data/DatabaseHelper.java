package com.example.study_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.study_app.R;
import com.example.study_app.ui.Curriculum.Model.Curriculum;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_app.db";
    // Sửa lỗi: Nâng cấp phiên bản DB để khớp hoặc cao hơn phiên bản trên máy
    private static final int DB_VERSION = 9;

    private final Context context;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


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
        // Chiến lược nâng cấp an toàn cho môi trường phát triển:
        // Xóa hết bảng cũ và tạo lại.
        Log.w("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS hoc_ky");
        db.execSQL("DROP TABLE IF EXISTS khoa");
        db.execSQL("DROP TABLE IF EXISTS hoc_phan_tu_chon");
        db.execSQL("DROP TABLE IF EXISTS mon_hoc");
        db.execSQL("DROP TABLE IF EXISTS hoc_phan_tien_quyet");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS Weeks");
        db.execSQL("DROP TABLE IF EXISTS Icons");
        db.execSQL("DROP TABLE IF EXISTS Colors");
        db.execSQL("DROP TABLE IF EXISTS Deadlines");
        db.execSQL("DROP TABLE IF EXISTS Reminders");
        db.execSQL("DROP TABLE IF EXISTS Notifications");
        db.execSQL("DROP TABLE IF EXISTS notes");
        db.execSQL("DROP TABLE IF EXISTS attachments");
        db.execSQL("DROP TABLE IF EXISTS timetable_sessions");
        db.execSQL("DROP TABLE IF EXISTS enrollments");
        db.execSQL("DROP TABLE IF EXISTS notification_schedules");
        db.execSQL("DROP TABLE IF EXISTS mon_hoc_tu_chon_map");
        onCreate(db);
    }

    private List<String> getColumns(SQLiteDatabase db, String tableName) {
        List<String> columns = new ArrayList<>();
        try (Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null)) {
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

    private Date parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        try {
            return dateTimeFormat.parse(dateTimeStr);
        } catch (ParseException e) {
            Log.e("DatabaseHelper", "Error parsing dateTime: " + dateTimeStr, e);
            return null;
        }
    }

    private String formatDateTime(Date date) {
        if (date == null) return null;
        return dateTimeFormat.format(date);
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
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int tenHocKyIndex = cursor.getColumnIndexOrThrow("ten_hoc_ky");
                do {
                    semesterNames.add(cursor.getString(tenHocKyIndex));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting semesters", e);
        }
        return semesterNames;
    }

    public int getSemesterIdByName(String semesterName) {
        SQLiteDatabase db = this.getReadableDatabase();
        int semesterId = -1;
        try (Cursor cursor = db.query("hoc_ky", new String[]{"id"}, "ten_hoc_ky = ?", new String[]{semesterName}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                semesterId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting semester ID", e);
        }
        return semesterId;
    }

    public ArrayList<Subject> getSubjectsBySemester(String semesterName) {
        ArrayList<Subject> subjectList = new ArrayList<>();
        String selectQuery = "SELECT m.*, w.num_of_weeks FROM mon_hoc m " +
                "INNER JOIN enrollments e ON m.ma_hp = e.ma_hp " +
                "INNER JOIN hoc_ky hk ON e.hoc_ky = hk.id " +
                "LEFT JOIN Weeks w ON m.ma_hp = w.ma_hp " + 
                "WHERE hk.ten_hoc_ky = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(selectQuery, new String[]{semesterName})) {
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
                    subject.soTuan = cursor.getInt(cursor.getColumnIndexOrThrow("num_of_weeks")); 
                    subject.tenHk = semesterName;
                    subjectList.add(subject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting subjects", e);
        }
        return subjectList;
    }

    public Subject getSubjectByMaHp(String maHp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Subject subject = null;
        String query = "SELECT m.*, w.num_of_weeks FROM mon_hoc m LEFT JOIN Weeks w ON m.ma_hp = w.ma_hp WHERE m.ma_hp = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{maHp})) {
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
                subject.soTuan = cursor.getInt(cursor.getColumnIndexOrThrow("num_of_weeks"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting subject detail", e);
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
            return db.insertOrThrow("mon_hoc", null, values);
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
    
    public void addOrUpdateWeekForSubject(Subject subject) {
        if (subject == null || subject.maHp == null) return;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ma_hp", subject.maHp);
        values.put("start_date", formatDate(subject.ngayBatDau));
        values.put("num_of_weeks", subject.soTuan);
        values.put("end_date", formatDate(subject.ngayKetThuc));

        int weekId = getWeekIdForSubject(subject.maHp);
        
        if (weekId != -1) {
            db.update("Weeks", values, "week_id = ?", new String[]{String.valueOf(weekId)});
             Log.d("DatabaseHelper", "Updated week for " + subject.maHp);
        } else {
            db.insert("Weeks", null, values);
            Log.d("DatabaseHelper", "Inserted new week for " + subject.maHp);
        }
    }

    public int getWeekIdForSubject(String maHp) {
        SQLiteDatabase db = this.getReadableDatabase();
        int weekId = -1;
        try (Cursor cursor = db.query("Weeks", new String[]{"week_id"}, "ma_hp = ?", new String[]{maHp}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                weekId = cursor.getInt(cursor.getColumnIndexOrThrow("week_id"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting week ID for " + maHp, e);
        }
        return weekId;
    }

    public ArrayList<Deadline> getDeadlinesByMaHp(String maHp) {
        ArrayList<Deadline> deadlineList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT d.* FROM Deadlines d JOIN Weeks w ON d.week_id = w.week_id WHERE w.ma_hp = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{maHp})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                    String startDateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("start_datetime"));
                    String endDateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("end_datetime"));
                    int iconId = cursor.getInt(cursor.getColumnIndexOrThrow("icon_id"));
                    
                    Deadline deadline = new Deadline(title, description, parseDateTime(startDateTimeStr), parseDateTime(endDateTimeStr), iconId);
                    
                    deadline.setMaDl(cursor.getInt(cursor.getColumnIndexOrThrow("deadline_id")));
                    deadline.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1);
                    deadline.setRepeat(cursor.getString(cursor.getColumnIndexOrThrow("repeat_type")));
                    deadline.setReminder(cursor.getString(cursor.getColumnIndexOrThrow("repeat_days")));

                    deadlineList.add(deadline);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
             Log.e("DatabaseHelper", "Error getting deadlines for " + maHp, e);
        }
        return deadlineList;
    }

    public long addDeadline(Deadline deadline, String maHp) {
        int weekId = getWeekIdForSubject(maHp);
        if (weekId == -1) {
            Log.e("DatabaseHelper", "Cannot add deadline, no week found for subject: " + maHp);
            return -1;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("week_id", weekId);
        values.put("title", deadline.getTieuDe());
        values.put("note", deadline.getNoiDung());
        values.put("start_datetime", formatDateTime(deadline.getNgayBatDau()));
        values.put("end_datetime", formatDateTime(deadline.getNgayKetThuc()));
        
        String repeatType = deadline.getRepeat();
        if (repeatType == null || repeatType.isEmpty()) {
            repeatType = "once";
        }
        values.put("repeat_type", repeatType);

        values.put("repeat_days", deadline.getReminder()); 
        
        values.put("completed", deadline.isCompleted() ? 1 : 0);
        values.put("icon_id", deadline.getIcon());
        
        try {
            return db.insertOrThrow("Deadlines", null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to add deadline", e);
            return -1;
        }
    }

    public int updateDeadline(Deadline deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", deadline.getTieuDe());
        values.put("note", deadline.getNoiDung());
        values.put("start_datetime", formatDateTime(deadline.getNgayBatDau()));
        values.put("end_datetime", formatDateTime(deadline.getNgayKetThuc()));
        
        String repeatType = deadline.getRepeat();
        if (repeatType == null || repeatType.isEmpty()) {
            repeatType = "once";
        }
        values.put("repeat_type", repeatType);

        values.put("repeat_days", deadline.getReminder());
        
        values.put("completed", deadline.isCompleted() ? 1 : 0);
        values.put("icon_id", deadline.getIcon());

        return db.update("Deadlines", values, "deadline_id = ?", new String[]{String.valueOf(deadline.getMaDl())});
    }

    public void deleteDeadline(int deadlineId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("Deadlines", "deadline_id = ?", new String[]{String.valueOf(deadlineId)});
    }

    public Map<String, Integer> getFacultiesMap() {
        Map<String, Integer> faculties = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query("khoa", new String[]{"id", "ten_khoa"}, null, null, null, null, "ten_khoa ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    faculties.put(cursor.getString(cursor.getColumnIndexOrThrow("ten_khoa")),
                                  cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting faculties map", e);
        }
        return faculties;
    }

    public List<String> getAllCourseGroups() {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT DISTINCT nhom_tu_chon FROM mon_hoc WHERE nhom_tu_chon IS NOT NULL AND nhom_tu_chon != '' " +
                       "UNION " +
                       "SELECT DISTINCT ten_nhom FROM hoc_phan_tu_chon WHERE ten_nhom IS NOT NULL AND ten_nhom != ''";
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    groups.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting course groups", e);
        }
        return groups;
    }

    public List<Curriculum> getAllCoursesForCurriculum() {
        List<Curriculum> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM mon_hoc";
        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Curriculum course = new Curriculum();
                    course.setMaHp(cursor.getString(cursor.getColumnIndexOrThrow("ma_hp")));
                    course.setTenHp(cursor.getString(cursor.getColumnIndexOrThrow("ten_hp")));
                    course.setSoTinChi(cursor.getInt(cursor.getColumnIndexOrThrow("so_tin_chi")));
                    course.setSoTietLyThuyet(cursor.getInt(cursor.getColumnIndexOrThrow("so_tiet_ly_thuyet")));
                    course.setSoTietThucHanh(cursor.getInt(cursor.getColumnIndexOrThrow("so_tiet_thuc_hanh")));
                    course.setNhomTuChon(cursor.getString(cursor.getColumnIndexOrThrow("nhom_tu_chon")));
                    course.setHocKy(cursor.getInt(cursor.getColumnIndexOrThrow("hoc_ky")));
                    course.setLoaiHp(cursor.getString(cursor.getColumnIndexOrThrow("loai_hp")));
                    course.setKhoaId(cursor.getInt(cursor.getColumnIndexOrThrow("khoa_id")));
                    courses.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting all courses for curriculum", e);
        }
        return courses;
    }
}
