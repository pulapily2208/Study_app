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
import com.example.study_app.ui.Notes.Model.Note;
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
    private static final int DB_VERSION = 11; // Incremented version

    private final Context context;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    // Status constants for curriculum display
    public static final String STATUS_NOT_ENROLLED = "NOT_ENROLLED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";

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
        Log.w("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        // Drop all tables
        db.execSQL("DROP TABLE IF EXISTS hoc_ky");
        db.execSQL("DROP TABLE IF EXISTS khoa");
        db.execSQL("DROP TABLE IF EXISTS hoc_phan_tu_chon");
        db.execSQL("DROP TABLE IF EXISTS mon_hoc");
        db.execSQL("DROP TABLE IF EXISTS hoc_phan_tien_quyet");
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS deadline"); // Corrected table name
        db.execSQL("DROP TABLE IF EXISTS notes");
        db.execSQL("DROP TABLE IF EXISTS attachments");
        db.execSQL("DROP TABLE IF EXISTS timetable_sessions");
        db.execSQL("DROP TABLE IF EXISTS enrollments");
        db.execSQL("DROP TABLE IF EXISTS notification_schedules");
        db.execSQL("DROP TABLE IF EXISTS mon_hoc_tu_chon_map");
        onCreate(db);
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

    // SUBJECT
    public ArrayList<String> getAllSemesterNames() {
        ArrayList<String> semesterNames = new ArrayList<>();
        String selectQuery = "SELECT ten_hoc_ky FROM hoc_ky ORDER BY nam_hoc DESC, id DESC";
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
        if (semesterName == null) return semesterId;
        try (Cursor cursor = db.query("hoc_ky", new String[]{"id"}, "ten_hoc_ky = ?", new String[]{semesterName}, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                semesterId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting semester ID by name", e);
        }
        return semesterId;
    }

    public ArrayList<Subject> getSubjectsBySemester(String semesterName) {
        ArrayList<Subject> subjectList = new ArrayList<>();
        int semesterId = getSemesterIdByName(semesterName);
        if (semesterId == -1) return subjectList;

        String selectQuery = "SELECT m.* FROM mon_hoc m " +
                "INNER JOIN enrollments e ON m.ma_hp = e.ma_hp " +
                "WHERE e.hoc_ky = ?";

        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(semesterId)})) {
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
            Log.e("DatabaseHelper", "Error getting subjects by semester", e);
        }
        return subjectList;
    }

    public Subject getSubjectByMaHp(String maHp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Subject subject = null;
        String query = "SELECT * FROM mon_hoc WHERE ma_hp = ?";
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
                subject.soTuan = cursor.getInt(cursor.getColumnIndexOrThrow("so_tuan"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting subject detail", e);
        }
        return subject;
    }

    public long addSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        long newRowId = -1;

        int semesterId = getSemesterIdByName(subject.tenHk);
        if (semesterId == -1) {
            Log.e("DatabaseHelper", "Cannot add subject. Semester not found: " + subject.tenHk);
            return -1;
        }

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
        values.put("so_tuan", subject.soTuan);
        // We don't save hoc_ky directly in mon_hoc table as per original schema
        // It is managed via the enrollments table.

        db.beginTransaction();
        try {
            newRowId = db.insertOrThrow("mon_hoc", null, values);

            // Now, create the enrollment
            if (newRowId != -1) {
                enrollSubjectInSemester(subject.maHp, semesterId);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to add subject or enrollment", e);
            newRowId = -1; // Ensure failure is reported
        } finally {
            db.endTransaction();
        }
        return newRowId;
    }

    // NEW: Add or only enroll if subject code already exists
    public long addOrEnrollSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        long resultId = -1;

        int semesterId = getSemesterIdByName(subject.tenHk);
        if (semesterId == -1) {
            Log.e("DatabaseHelper", "Semester not found: " + subject.tenHk);
            return -1;
        }

        Subject existing = getSubjectByMaHp(subject.maHp);
        db.beginTransaction();
        try {
            if (existing == null) {
                // Insert new subject (same as addSubject but consolidated here)
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
                values.put("so_tuan", subject.soTuan);

                resultId = db.insertOrThrow("mon_hoc", null, values);
            } else {
                // Subject already exists in curriculum → only optionally update personal fields
                ContentValues update = new ContentValues();
                if (subject.tenGv != null && !subject.tenGv.isEmpty()) update.put("giang_vien", subject.tenGv);
                if (subject.phongHoc != null && !subject.phongHoc.isEmpty()) update.put("phong_hoc", subject.phongHoc);
                if (subject.ngayBatDau != null) update.put("ngay_bat_dau", formatDate(subject.ngayBatDau));
                if (subject.ngayKetThuc != null) update.put("ngay_ket_thuc", formatDate(subject.ngayKetThuc));
                if (subject.gioBatDau != null) update.put("gio_bat_dau", formatTime(subject.gioBatDau));
                if (subject.gioKetThuc != null) update.put("gio_ket_thuc", formatTime(subject.gioKetThuc));
                if (subject.ghiChu != null) update.put("ghi_chu", subject.ghiChu);
                if (subject.mauSac != null) update.put("color_tag", subject.mauSac);
                if (subject.soTuan > 0) update.put("so_tuan", subject.soTuan);
                if (update.size() > 0) {
                    db.update("mon_hoc", update, "ma_hp = ?", new String[]{subject.maHp});
                }
                resultId = 1; // mark success
            }

            // Ensure enrollment
            enrollSubjectInSemester(subject.maHp, semesterId);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "addOrEnrollSubject failed", e);
            resultId = -1;
        } finally {
            db.endTransaction();
        }
        return resultId;
    }

    public void enrollSubjectInSemester(String maHp, int semesterId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", 1);
        values.put("ma_hp", maHp);
        values.put("hoc_ky", semesterId);
        // Use insert with conflict algorithm to avoid duplicates if user tries to add the same subject to the same semester
        db.insertWithOnConflict("enrollments", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public int updateSubject(Subject subject) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = 0;
        int semesterId = getSemesterIdByName(subject.tenHk);
        if (semesterId == -1) {
            Log.e("DatabaseHelper", "Cannot update subject. Semester not found: " + subject.tenHk);
            return 0;
        }

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

        db.beginTransaction();
        try {
            rowsAffected = db.update("mon_hoc", values, "ma_hp = ?", new String[]{subject.maHp});

            // Update enrollment: remove old ones and add the new one to handle semester changes
            db.delete("enrollments", "ma_hp = ?", new String[]{subject.maHp});
            enrollSubjectInSemester(subject.maHp, semesterId);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to update subject or enrollment", e);
            rowsAffected = 0; // Ensure failure is reported
        } finally {
            db.endTransaction();
        }
        return rowsAffected;
    }

    public void deleteSubject(String maHp) {
        SQLiteDatabase db = this.getWritableDatabase();
        // The CASCADE constraint on the enrollments table will automatically remove the corresponding enrollments.
        db.delete("mon_hoc", "ma_hp = ?", new String[]{maHp});
    }

    // DEADLINE
    public ArrayList<Deadline> getDeadlinesByMaHp(String maHp) {
        ArrayList<Deadline> deadlineList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // The query should be on the 'deadline' table based on your schema
        String query = "SELECT * FROM deadline WHERE ma_hp = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{maHp})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // Assuming Deadline constructor and setters match the 'deadline' table
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("tieu_de"));
                    String description = cursor.getString(cursor.getColumnIndexOrThrow("noi_dung"));
                    String startDateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("ngay_bat_dau")); // Assuming this is a full dateTime
                    String endDateTimeStr = cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc"));   // Assuming this is a full dateTime

                    // You might need to adjust the Deadline model if it doesn't fit this structure
                    Deadline deadline = new Deadline(title, description, parseDateTime(startDateTimeStr), parseDateTime(endDateTimeStr), 0); // Assuming no iconId in deadline table

                    deadline.setMaDl(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                    deadline.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1);
                    // Add other fields if present in your Deadline model and 'deadline' table

                    deadlineList.add(deadline);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting deadlines for " + maHp, e);
        }
        return deadlineList;
    }

    public long addDeadline(Deadline deadline, String maHp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("ma_hp", maHp);
        values.put("tieu_de", deadline.getTieuDe());
        values.put("noi_dung", deadline.getNoiDung());
        values.put("ngay_bat_dau", formatDateTime(deadline.getNgayBatDau()));
        values.put("ngay_ket_thuc", formatDateTime(deadline.getNgayKetThuc()));
        values.put("completed", deadline.isCompleted() ? 1 : 0);
        // Add other deadline fields if they exist in the 'deadline' table

        try {
            return db.insertOrThrow("deadline", null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to add deadline", e);
            return -1;
        }
    }

    public int updateDeadline(Deadline deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("tieu_de", deadline.getTieuDe());
        values.put("noi_dung", deadline.getNoiDung());
        values.put("ngay_bat_dau", formatDateTime(deadline.getNgayBatDau()));
        values.put("ngay_ket_thuc", formatDateTime(deadline.getNgayKetThuc()));
        values.put("completed", deadline.isCompleted() ? 1 : 0);
        // Add other deadline fields

        return db.update("deadline", values, "id = ?", new String[]{String.valueOf(deadline.getMaDl())});
    }

    public void deleteDeadline(int deadlineId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("deadline", "id = ?", new String[]{String.valueOf(deadlineId)});
    }

    // NOTE
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
                    note.setImagePaths(getNoteImages(note.getId()));
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
        long noteId = db.insert("notes", null, values);
        if (note.getImagePaths() != null) {
            for (String path : note.getImagePaths()) {
                ContentValues imgValue = new ContentValues();
                imgValue.put("note_id", noteId);
                imgValue.put("image_path", path);
                db.insert("note_images", null, imgValue);

            }
        }

        return noteId;
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
                note.setImagePaths(getNoteImages(note.getId()));
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
        values.put("ma_hp", note.getMa_hp());
        values.put("user_id", note.getUser_id());
        int rows = db.update("notes", values, "id=?", new String[]{String.valueOf(note.getId())});

        // XÓA ảnh cũ
        db.delete("note_images", "note_id=?", new String[]{String.valueOf(note.getId())});

        // THÊM ảnh mới
        if (note.getImagePaths() != null) {
            for (String path : note.getImagePaths()) {
                ContentValues img = new ContentValues();
                img.put("note_id", note.getId());
                img.put("image_path", path);
                img.put("created_at", System.currentTimeMillis());
                db.insert("note_images", null, img);
            }
        }

        return rows > 0;
    }

    public boolean deleteNote(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("notes", "id = ?", new String[]{String.valueOf(id)});
        return rowsDeleted > 0;
    }

    public List<String> getNoteImages(int noteId) {
        List<String> images = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT image_path FROM note_images WHERE note_id=?",
                new String[]{String.valueOf(noteId)});

        if (cursor.moveToFirst()) {
            do {
                images.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return images;
    }

    // CHƯƠNG TRÌNH ĐÀO TẠO
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

    // 1. Lấy danh sách tên các nhóm để hiển thị lên Header/Menu
    public List<String> getAllCourseGroups() {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Lấy trực tiếp từ bảng định nghĩa nhóm (hoc_phan_tu_chon)
        // Sắp xếp theo ID để thứ tự hiển thị logic (Đại cương -> Chuyên ngành -> Tốt nghiệp)
        String query = "SELECT ten_nhom FROM hoc_phan_tu_chon ORDER BY id ASC";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String groupName = cursor.getString(0);
                    if (groupName != null && !groupName.isEmpty()) {
                        groups.add(groupName);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting course groups", e);
        }
        return groups;
    }

    // 2. Lấy danh sách môn học kèm theo Tên Nhóm (Thay vì số ID)
    public List<Curriculum> getAllCoursesForCurriculum() {
        List<Curriculum> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // CÂU TRUY VẤN QUAN TRỌNG:
        // Kết nối bảng mon_hoc (m) với bảng hoc_phan_tu_chon (h)
        // Nếu m.nhom_tu_chon khớp với h.id, ta lấy h.ten_nhom
        String query = "SELECT m.*, h.ten_nhom AS ten_nhom_day_du " +
                "FROM mon_hoc m " +
                "LEFT JOIN hoc_phan_tu_chon h ON m.nhom_tu_chon = h.id " +
                "ORDER BY m.hoc_ky ASC, m.ma_hp ASC";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Curriculum course = new Curriculum();

                    // Các trường cơ bản
                    course.setMaHp(cursor.getString(cursor.getColumnIndexOrThrow("ma_hp")));
                    course.setTenHp(cursor.getString(cursor.getColumnIndexOrThrow("ten_hp")));
                    course.setSoTinChi(cursor.getInt(cursor.getColumnIndexOrThrow("so_tin_chi")));
                    course.setSoTietLyThuyet(cursor.getInt(cursor.getColumnIndexOrThrow("so_tiet_ly_thuyet")));
                    course.setSoTietThucHanh(cursor.getInt(cursor.getColumnIndexOrThrow("so_tiet_thuc_hanh")));
                    course.setHocKy(cursor.getInt(cursor.getColumnIndexOrThrow("hoc_ky")));
                    course.setLoaiHp(cursor.getString(cursor.getColumnIndexOrThrow("loai_hp")));
                    course.setKhoaId(cursor.getInt(cursor.getColumnIndexOrThrow("khoa_id")));

                    // XỬ LÝ NHÓM TỰ CHỌN (LOGIC QUAN TRỌNG)
                    // Lấy tên nhóm từ bảng JOIN (cột ten_nhom_day_du)
                    int nameIndex = cursor.getColumnIndex("ten_nhom_day_du");
                    String realGroupName = "";

                    if (nameIndex != -1) {
                        realGroupName = cursor.getString(nameIndex);
                    }

                    // Nếu lấy được tên nhóm từ bảng join thì set vào
                    if (realGroupName != null && !realGroupName.isEmpty()) {
                        course.setNhomTuChon(realGroupName);
                    } else {
                        // Trường hợp môn bắt buộc (nhóm là NULL) hoặc không khớp ID
                        // Ta lấy giá trị gốc, hoặc để trống nếu null
                        String originalValue = cursor.getString(cursor.getColumnIndexOrThrow("nhom_tu_chon"));
                        course.setNhomTuChon(originalValue != null ? originalValue : "");
                    }

                    courses.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting all courses for curriculum", e);
        }
        return courses;
    }

    // NEW: Map enrolled subjects for a given user
    public Map<String, Integer> getEnrolledSubjectsMap(int userId) {
        Map<String, Integer> map = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT ma_hp, hoc_ky FROM enrollments WHERE user_id = ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                do {
                    map.put(c.getString(c.getColumnIndexOrThrow("ma_hp")),
                            c.getInt(c.getColumnIndexOrThrow("hoc_ky")));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "getEnrolledSubjectsMap error", e);
        }
        return map;
    }

    // NEW: Compute status based on enrollment and end date
    private String computeSubjectStatus(boolean enrolled, Date endDate) {
        if (!enrolled) return STATUS_NOT_ENROLLED;
        if (endDate != null) {
            Date today = new Date();
            if (endDate.before(today)) return STATUS_COMPLETED;
            return STATUS_IN_PROGRESS;
        }
        return STATUS_IN_PROGRESS;
    }

    // NEW: Get curriculum with status for a given user
    public List<Curriculum> getAllCoursesForCurriculumWithStatus(int userId) {
        List<Curriculum> courses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Map<String, Integer> enrolledMap = getEnrolledSubjectsMap(userId);

        String query = "SELECT m.*, h.ten_nhom AS ten_nhom_day_du " +
                "FROM mon_hoc m " +
                "LEFT JOIN hoc_phan_tu_chon h ON m.nhom_tu_chon = h.id " +
                "ORDER BY m.hoc_ky ASC, m.ma_hp ASC";

        try (Cursor cursor = db.rawQuery(query, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Curriculum course = new Curriculum();
                    course.setMaHp(cursor.getString(cursor.getColumnIndexOrThrow("ma_hp")));
                    course.setTenHp(cursor.getString(cursor.getColumnIndexOrThrow("ten_hp")));
                    course.setSoTinChi(cursor.getInt(cursor.getColumnIndexOrThrow("so_tin_chi")));
                    course.setSoTietLyThuyet(cursor.getInt(cursor.getColumnIndexOrThrow("so_tiet_ly_thuyet")));
                    course.setSoTietThucHanh(cursor.getInt(cursor.getColumnIndexOrThrow("so_tiet_thuc_hanh")));
                    course.setHocKy(cursor.getInt(cursor.getColumnIndexOrThrow("hoc_ky")));
                    course.setLoaiHp(cursor.getString(cursor.getColumnIndexOrThrow("loai_hp")));
                    course.setKhoaId(cursor.getInt(cursor.getColumnIndexOrThrow("khoa_id")));

                    // FIX: Safely get the column index before retrieving the string
                    int nameIndex = cursor.getColumnIndex("ten_nhom_day_du");
                    String realGroupName = null;
                    if (nameIndex != -1) {
                        realGroupName = cursor.getString(nameIndex);
                    }

                    if (realGroupName != null && !realGroupName.isEmpty()) {
                        course.setNhomTuChon(realGroupName);
                    } else {
                        String originalValue = cursor.getString(cursor.getColumnIndexOrThrow("nhom_tu_chon"));
                        course.setNhomTuChon(originalValue != null ? originalValue : "");
                    }

                    String endDateStr = cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc"));
                    Date endDate = parseDate(endDateStr);

                    boolean enrolled = enrolledMap.containsKey(course.getMaHp());
                    String status = computeSubjectStatus(enrolled, endDate);

                    // Inject status via reflection to Curriculum if available
                    try {
                        // If Curriculum has setStatus, call it (newer version supports it)
                        Curriculum.class.getMethod("setStatus", String.class).invoke(course, status);
                    } catch (Exception ignored) {
                        // Older Curriculum class without status -> ignore
                    }

                    courses.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getAllCoursesForCurriculumWithStatus", e);
        }
        return courses;
    }

    /**
     * Searches for subject codes (ma_hp) in the curriculum that start with a given prefix.
     * This is used for the AutoCompleteTextView suggestions.
     * @param prefix The prefix to search for.
     * @return A list of matching subject codes.
     */
    public List<String> searchSubjectCodes(String prefix) {
        List<String> subjectCodes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // The LIKE operator with '%' will find any values that start with the prefix.
        // The LIMIT 20 is to avoid returning too many results.
        String query = "SELECT ma_hp FROM mon_hoc WHERE ma_hp LIKE ? ORDER BY ma_hp LIMIT 20";
        try (Cursor cursor = db.rawQuery(query, new String[]{prefix + "%"})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    subjectCodes.add(cursor.getString(cursor.getColumnIndexOrThrow("ma_hp")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error searching subject codes", e);
        }
        return subjectCodes;
    }

    /**
     * Retrieves the details of a specific course from the base curriculum table (mon_hoc).
     * This is used to auto-fill the subject creation form.
     * @param maHp The course code to look for.
     * @return A Curriculum object with the course details, or null if not found.
     */
    public Curriculum getCurriculumDetailsByMaHp(String maHp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Curriculum curriculum = null;
        // UPDATED QUERY: Join with hoc_phan_tu_chon to get the group name
        String query = "SELECT m.*, h.ten_nhom AS ten_nhom_day_du " +
                "FROM mon_hoc m " +
                "LEFT JOIN hoc_phan_tu_chon h ON m.nhom_tu_chon = h.id " +
                "WHERE m.ma_hp = ?";

        try (Cursor cursor = db.rawQuery(query, new String[]{maHp})) {
            if (cursor != null && cursor.moveToFirst()) {
                curriculum = new Curriculum();
                curriculum.setMaHp(cursor.getString(cursor.getColumnIndexOrThrow("ma_hp")));
                curriculum.setTenHp(cursor.getString(cursor.getColumnIndexOrThrow("ten_hp")));
                curriculum.setSoTinChi(cursor.getInt(cursor.getColumnIndexOrThrow("so_tin_chi")));
                curriculum.setSoTietLyThuyet(cursor.getInt(cursor.getColumnIndexOrThrow("so_tiet_ly_thuyet")));
                curriculum.setSoTietThucHanh(cursor.getInt(cursor.getColumnIndexOrThrow("so_tiet_thuc_hanh")));
                curriculum.setHocKy(cursor.getInt(cursor.getColumnIndexOrThrow("hoc_ky")));
                curriculum.setLoaiHp(cursor.getString(cursor.getColumnIndexOrThrow("loai_hp")));
                curriculum.setKhoaId(cursor.getInt(cursor.getColumnIndexOrThrow("khoa_id")));

                // Get the full group name from the join
                int nameIndex = cursor.getColumnIndex("ten_nhom_day_du");
                String realGroupName = null;
                if (nameIndex != -1) {
                    realGroupName = cursor.getString(nameIndex);
                }

                if (realGroupName != null && !realGroupName.isEmpty()) {
                    curriculum.setNhomTuChon(realGroupName);
                } else {
                    // Fallback for compulsory subjects or if the ID doesn't match
                    curriculum.setNhomTuChon(""); // Set to empty string if not a choice group
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting curriculum details by maHp", e);
        }
        return curriculum;
    }
    /**
     * Retrieves the list of prerequisite course codes for a given subject.
     * @param maHp The course code to check.
     * @return A list of prerequisite course codes.
     */
    public List<String> getPrerequisites(String maHp) {
        List<String> prerequisites = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT ma_hp_tien_quyet FROM hoc_phan_tien_quyet WHERE ma_hp = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{maHp})) {
            if (cursor != null && cursor.moveToFirst()) {
                int preReqIndex = cursor.getColumnIndexOrThrow("ma_hp_tien_quyet");
                do {
                    prerequisites.add(cursor.getString(preReqIndex));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting prerequisites for " + maHp, e);
        }
        return prerequisites;
    }

    /**
     * Checks if all prerequisites for a given course have been COMPLETED (status = STATUS_COMPLETED).
     * @param maHp The course code to check.
     * @param userId The ID of the user (currently hardcoded as 1 in usage).
     * @return True if all prerequisites are completed or if there are no prerequisites. False otherwise.
     */
    public boolean checkPrerequisiteStatus(String maHp, int userId) {
        List<String> prerequisites = getPrerequisites(maHp);
        if (prerequisites.isEmpty()) {
            return true; // No prerequisites, so condition is met.
        }

        // Lấy danh sách các môn đã đăng ký của user (bao gồm cả end date)
        Map<String, Integer> enrolledMap = getEnrolledSubjectsMap(userId);

        for (String preReqMaHp : prerequisites) {
            if (!enrolledMap.containsKey(preReqMaHp)) {
                // Môn tiên quyết chưa từng được đăng ký/học
                return false;
            }

            // Môn tiên quyết đã đăng ký, kiểm tra trạng thái hoàn thành
            Subject preReqSubject = getSubjectByMaHp(preReqMaHp);

            // Nếu không tìm thấy môn học hoặc ngày kết thúc chưa có, coi như chưa hoàn thành
            if (preReqSubject == null || preReqSubject.ngayKetThuc == null) {
                return false;
            }

            // Kiểm tra trạng thái: chỉ cần là STATUS_COMPLETED mới được chấp nhận
            String status = computeSubjectStatus(true, preReqSubject.ngayKetThuc);
            if (!status.equals(STATUS_COMPLETED)) {
                Log.d("DatabaseHelper", "Prerequisite " + preReqMaHp + " is not completed. Status: " + status);
                return false;
            }
        }

        return true; // Tất cả các môn tiên quyết đều đã hoàn thành.
    }
}
