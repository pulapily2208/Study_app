package com.example.study_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.study_app.R;
import com.example.study_app.ui.Notes.Model.Note;
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
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_app.db";
    // Tăng version để thêm cột ma_hp vào bảng deadline
    private static final int DB_VERSION = 9;

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
        Log.i("DatabaseHelper", "Nâng cấp database từ version " + oldVersion + " lên " + newVersion);

        if (oldVersion < 7) {
            List<String> existingColumns = getColumns(db, "mon_hoc");
            String[] requiredTextColumns = {
                "giang_vien", "phong_hoc", "ngay_bat_dau",
                "ngay_ket_thuc", "gio_bat_dau", "gio_ket_thuc", "ghi_chu"
            };

            for (String column : requiredTextColumns) {
                if (!existingColumns.contains(column)) {
                    try {
                        String sql = "ALTER TABLE mon_hoc ADD COLUMN " + column + " TEXT";
                        db.execSQL(sql);
                        Log.i("DatabaseHelper", "Đã thêm cột " + column + " vào bảng mon_hoc");
                    } catch (Exception e) {
                        Log.w("DatabaseHelper", "Không thể thêm cột " + column + ": " + e.getMessage());
                    }
                }
            }
        }

        if (oldVersion < 8) {
             List<String> existingColumns = getColumns(db, "mon_hoc");
             if (!existingColumns.contains("so_tuan")) {
                 try {
                     String sql = "ALTER TABLE mon_hoc ADD COLUMN so_tuan INTEGER";
                     db.execSQL(sql);
                     Log.i("DatabaseHelper", "Đã thêm cột so_tuan vào bảng mon_hoc");
                 } catch (Exception e) {
                     Log.w("DatabaseHelper", "Không thể thêm cột so_tuan: " + e.getMessage());
                 }
             }
        }
        
        if (oldVersion < 9) {
            List<String> existingColumns = getColumns(db, "deadline");
            if (!existingColumns.contains("ma_hp")) {
                try {
                    String sql = "ALTER TABLE deadline ADD COLUMN ma_hp TEXT";
                    db.execSQL(sql);
                    Log.i("DatabaseHelper", "Đã thêm cột ma_hp vào bảng deadline");
                } catch (Exception e) {
                    Log.w("DatabaseHelper", "Không thể thêm cột ma_hp: " + e.getMessage());
                }
            }
        }
    }

    private List<String> getColumns(SQLiteDatabase db, String tableName) {
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
            Log.e("DatabaseHelper", "Lỗi khi lấy thông tin cột của bảng " + tableName, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return columns;
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
    
    // --- Helpers Date/Time ---
    private Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            Log.e("DatabaseHelper", "Lỗi khi phân tích ngày: " + dateStr, e);
            return null;
        }
    }

    private Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            return timeFormat.parse(timeStr);
        } catch (ParseException e) {
            Log.e("DatabaseHelper", "Lỗi khi phân tích giờ: " + timeStr, e);
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

    // --- Quản lý Học kỳ (Semester) ---
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
            Log.e("DatabaseHelper", "Lỗi khi lấy ID học kỳ theo tên", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return semesterId;
    }

    // --- Quản lý Môn học (Subject) ---
    public long addSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues subjectValues = new ContentValues();
        subjectValues.put("ma_hp", subject.maHp);
        subjectValues.put("ten_hp", subject.tenHp);
        subjectValues.put("so_tin_chi", subject.soTc);
        subjectValues.put("loai_hp", subject.loaiMon);
        subjectValues.put("giang_vien", subject.tenGv);
        subjectValues.put("phong_hoc", subject.phongHoc);
        subjectValues.put("ngay_bat_dau", formatDate(subject.ngayBatDau));
        subjectValues.put("ngay_ket_thuc", formatDate(subject.ngayKetThuc));
        subjectValues.put("gio_bat_dau", formatTime(subject.gioBatDau));
        subjectValues.put("gio_ket_thuc", formatTime(subject.gioKetThuc));
        subjectValues.put("ghi_chu", subject.ghiChu);
        subjectValues.put("color_tag", subject.mauSac);
        subjectValues.put("so_tuan", subject.soTuan);

        db.beginTransaction();
        try {
            long subjectRowId = db.insertWithOnConflict("mon_hoc", null, subjectValues, SQLiteDatabase.CONFLICT_REPLACE);

            if (subjectRowId == -1) {
                return -1;
            }

            int semesterId = getSemesterIdByName(subject.tenHk);
            if (semesterId == -1) {
                return -1;
            }

            ContentValues enrollmentValues = new ContentValues();
            enrollmentValues.put("ma_hp", subject.maHp);
            enrollmentValues.put("hoc_ky", semesterId);

            db.insertWithOnConflict("enrollments", null, enrollmentValues, SQLiteDatabase.CONFLICT_IGNORE);

            db.setTransactionSuccessful();
            return subjectRowId;

        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi thêm môn học hoặc enrollment", e);
            return -1;
        } finally {
            db.endTransaction();
        }
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
        values.put("so_tuan", subject.soTuan);
        
        return db.update("mon_hoc", values, "ma_hp = ?", new String[]{subject.maHp});
    }

    public void deleteSubject(String maHp) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete("enrollments", "ma_hp = ?", new String[]{maHp});
            db.delete("mon_hoc", "ma_hp = ?", new String[]{maHp});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi xóa môn học", e);
        } finally {
            db.endTransaction();
        }
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
                    subject.soTuan = cursor.getInt(cursor.getColumnIndexOrThrow("so_tuan"));
                    subject.tenHk = semesterName;
                    subjectList.add(subject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách môn học theo học kỳ", e);
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
                subject.soTuan = cursor.getInt(cursor.getColumnIndexOrThrow("so_tuan"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy môn học theo mã HP", e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return subject;
    }

    // --- Quản lý Deadline ---

    public ArrayList<Deadline> getDeadlinesByMaHp(String maHp) {
        ArrayList<Deadline> deadlineList = new ArrayList<>();
        if (maHp == null || maHp.isEmpty()) {
            return deadlineList;
        }
        String selectQuery = "SELECT * FROM deadline WHERE ma_hp = ? ORDER BY ngay_ket_thuc";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, new String[]{maHp});
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Deadline deadline = new Deadline();
                    deadline.setMaDl(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    deadline.setTieuDe(cursor.getString(cursor.getColumnIndexOrThrow("tieu_de")));
                    deadline.setNoiDung(cursor.getString(cursor.getColumnIndexOrThrow("noi_dung")));
                    deadline.setNgayBatDau(parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_bat_dau"))));
                    deadline.setNgayKetThuc(parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc"))));
                    deadline.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1);

                    // Bật đọc ma_hp nếu tồn tại
                    int maHpIndex = cursor.getColumnIndex("ma_hp");
                    if (maHpIndex != -1 && !cursor.isNull(maHpIndex)) {
                        deadline.setMaHp(cursor.getString(maHpIndex));
                    }

                    deadlineList.add(deadline);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy deadlines theo mã HP: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return deadlineList;
    }

    public ArrayList<Deadline> getAllDeadlines() {
        ArrayList<Deadline> deadlineList = new ArrayList<>();
        String selectQuery = "SELECT * FROM deadline ORDER BY ngay_ket_thuc";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Deadline deadline = new Deadline();
                    deadline.setMaDl(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    deadline.setTieuDe(cursor.getString(cursor.getColumnIndexOrThrow("tieu_de")));
                    deadline.setNoiDung(cursor.getString(cursor.getColumnIndexOrThrow("noi_dung")));
                    deadline.setNgayBatDau(parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_bat_dau"))));
                    deadline.setNgayKetThuc(parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc"))));
                    deadline.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1);

                    int maHpIndex = cursor.getColumnIndex("ma_hp");
                    if (maHpIndex != -1 && !cursor.isNull(maHpIndex)) {
                        deadline.setMaHp(cursor.getString(maHpIndex));
                    }

                    deadlineList.add(deadline);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy tất cả deadlines: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        return deadlineList;
    }

    public long addDeadline(Deadline deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tieu_de", deadline.getTieuDe());
        values.put("noi_dung", deadline.getNoiDung());
        values.put("ngay_bat_dau", formatDate(deadline.getNgayBatDau()));
        values.put("ngay_ket_thuc", formatDate(deadline.getNgayKetThuc()));
        values.put("completed", deadline.isCompleted() ? 1 : 0);
        // Ghi ma_hp nếu có
        if (deadline.getMaHp() != null) {
            values.put("ma_hp", deadline.getMaHp());
        }
        long id = db.insert("deadline", null, values);
        return id;
    }

    public int updateDeadline(Deadline deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tieu_de", deadline.getTieuDe());
        values.put("noi_dung", deadline.getNoiDung());
        values.put("ngay_bat_dau", formatDate(deadline.getNgayBatDau()));
        values.put("ngay_ket_thuc", formatDate(deadline.getNgayKetThuc()));
        values.put("completed", deadline.isCompleted() ? 1 : 0);
        if (deadline.getMaHp() != null) {
            values.put("ma_hp", deadline.getMaHp());
        }

        return db.update("deadline", values, "id = ?", new String[]{String.valueOf(deadline.getMaDl())});
    }

    public void deleteDeadline(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("deadline", "id = ?", new String[]{String.valueOf(id)});
    }


    //    Quản lý note
    public ArrayList<Note> getAllNotes() {
        ArrayList<Note> notes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM notes ORDER BY id DESC", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Note note = new Note();
                    note.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    note.setUser_id(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                    note.setMa_hp(cursor.getString(cursor.getColumnIndexOrThrow("ma_hp")));
                    note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                    note.setBody(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                    note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow("pinned")));
                    note.setColor_tag(cursor.getString(cursor.getColumnIndexOrThrow("color_tag")));
                    note.setCreated_at(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                    note.setUpdated_at(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
                    note.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
                    notes.add(note);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy tất cả ghi chú", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return notes;
    }

    public long insertNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", note.getUser_id());
        values.put("ma_hp", note.getMa_hp());
        values.put("title", note.getTitle());
        values.put("body", note.getBody());
        values.put("pinned", note.getPinned());
        values.put("color_tag", note.getColor_tag());
        values.put("created_at", System.currentTimeMillis());
        values.put("updated_at", System.currentTimeMillis());
        values.put("image_path", note.getImagePath());
        return db.insert("notes", null, values);
    }

    public Note getNoteById(int noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        Note note = null;
        try {
            cursor = db.rawQuery("SELECT * FROM notes WHERE id = ?",
                    new String[]{String.valueOf(noteId)});
            if (cursor != null && cursor.moveToFirst()) {
                note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                note.setUser_id(cursor.getInt(cursor.getColumnIndexOrThrow("user_id")));
                note.setMa_hp(cursor.getString(cursor.getColumnIndexOrThrow("ma_hp")));
                note.setTitle(cursor.getString(cursor.getColumnIndexOrThrow("title")));
                note.setBody(cursor.getString(cursor.getColumnIndexOrThrow("body")));
                note.setPinned(cursor.getInt(cursor.getColumnIndexOrThrow("pinned")));
                note.setColor_tag(cursor.getString(cursor.getColumnIndexOrThrow("color_tag")));
                note.setCreated_at(cursor.getString(cursor.getColumnIndexOrThrow("created_at")));
                note.setUpdated_at(cursor.getString(cursor.getColumnIndexOrThrow("updated_at")));
                note.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow("image_path")));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy ghi chú theo ID", e);
            return null; // Trả về null nếu có lỗi
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return note;
    }
    public boolean updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Tự động cập nhật thời gian
        note.setTimestamp();

        ContentValues values = new ContentValues();
        values.put("title", note.getTitle());
        values.put("body", note.getBody());
        values.put("pinned", note.getPinned());
        values.put("color_tag", note.getColor_tag());
        values.put("created_at", note.getCreated_at());
        values.put("updated_at", note.getUpdated_at());
        values.put("image_path", note.getImagePath());
        values.put("ma_hp", note.getMa_hp());
        values.put("user_id", note.getUser_id());

        int rowsAffected = db.update(
                "notes",                    // tên bảng
                values,                     // dữ liệu cập nhật
                "id = ?",                   // điều kiện WHERE
                new String[]{String.valueOf(note.getId())} // giá trị điều kiện
        );

        db.close();
        return rowsAffected > 0;
    }



    public List<Curriculum> getAllCoursesForCurriculum() {
        List<Curriculum> courseList = new ArrayList<>();
        String selectQuery = "SELECT * FROM mon_hoc ORDER BY hoc_ky, ten_hp";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);
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
                    courseList.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy dữ liệu chương trình đào tạo", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return courseList;
    }

    // --- Curriculum Filter Helpers ---

    public Map<String, Integer> getFacultiesMap() {
        Map<String, Integer> faculties = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("khoa", new String[]{"id", "ten_khoa"}, null, null, null, null, "ten_khoa");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    faculties.put(cursor.getString(cursor.getColumnIndexOrThrow("ten_khoa")), cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách Khoa", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return faculties;
    }

    public List<String> getAllCourseGroups() {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Query both tables and merge distinct results
        String query = "SELECT ten_nhom FROM hoc_phan_tu_chon WHERE ten_nhom IS NOT NULL " +
                       "UNION " +
                       "SELECT nhom_tu_chon FROM mon_hoc WHERE nhom_tu_chon IS NOT NULL";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    groups.add(cursor.getString(0));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Lỗi khi lấy danh sách Nhóm học phần", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return groups;
    }
}
