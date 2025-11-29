package com.example.study_app.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.study_app.R;
import com.example.study_app.ui.Deadline.Models.Deadline;
import com.example.study_app.ui.Notes.Model.Note;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_app.db";
    private static final int DB_VERSION = 11;

    private final Context context;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static final String STATUS_NOT_ENROLLED = "NOT_ENROLLED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DEADLINE_TABLE = "CREATE TABLE deadline (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tieu_de TEXT," +
                "noi_dung TEXT," +
                "ngay_bat_dau TEXT," +
                "ngay_ket_thuc TEXT," +
                "completed INTEGER," +
                "ma_hp TEXT," +
                "repeat_type TEXT," +
                "reminder_time TEXT," +
                "icon INTEGER," +
                "notes TEXT," +
                "weekIndex INTEGER" +
                ")";
        db.execSQL(CREATE_DEADLINE_TABLE);
        runSqlFromRaw(db, R.raw.study_app);
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

    // Package-private so DAOs can use them
    Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    Date parseTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return null;
        try {
            return timeFormat.parse(timeStr);
        } catch (ParseException e) {
            return null;
        }
    }

    String formatDate(Date date) {
        if (date == null) return null;
        return dateFormat.format(date);
    }

    String formatTime(Date time) {
        if (time == null) return null;
        return timeFormat.format(time);
    }

     Date parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        try {
            return dateTimeFormat.parse(dateTimeStr);
        } catch (ParseException e) {
            Log.e("DatabaseHelper", "Error parsing dateTime: " + dateTimeStr, e);
            return null;
        }
    }

    String formatDateTime(Date date) {
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

    // NOTE METHODS
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
                    note.setPdfPaths(getNotePdfs(note.getId()));
                    note.setAudioPaths(getNoteAudios(note.getId()));
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

        if (note.getPdfPaths() != null){
            for (String path : note.getPdfPaths()){
                ContentValues pdfValue = new ContentValues();
                pdfValue.put("note_id", noteId);
                pdfValue.put("pdf_path", path);
                db.insert("note_pdfs", null, pdfValue);
            }
        }

        if (note.getAudioPaths() != null){
            for (String path : note.getAudioPaths()){
                ContentValues audioValue = new ContentValues();
                audioValue.put("note_id", noteId);
                audioValue.put("audio_path", path);
                db.insert("note_audios", null, audioValue);
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
                note.setPdfPaths(getNotePdfs(note.getId()));
                note.setAudioPaths(getNoteAudios(note.getId()));
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

        if (note.getPdfPaths() != null) {
            for (String path : note.getPdfPaths()) {
                ContentValues pdf = new ContentValues();
                pdf.put("note_id", note.getId());
                pdf.put("pdf_path", path);
                pdf.put("created_at", System.currentTimeMillis());
                db.insert("note_pdfs", null, pdf);
            }
        }

        if(note.getAudioPaths() != null) {
            for (String path : note.getAudioPaths()) {
                ContentValues audio = new ContentValues();
                audio.put("note_id", note.getId());
                audio.put("audio_path", path);
                audio.put("created_at", System.currentTimeMillis());
                db.insert("note_audios", null, audio);
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

    public List<String> getNotePdfs(int noteId){
        List<String> pdfs = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT pdf_path FROM note_pdfs WHERE note_id=?",
                new String[]{String.valueOf(noteId)});

        if (cursor.moveToFirst()) {
            do {
                pdfs.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pdfs;
    }

    public List<String> getNoteAudios(int noteId) {
        List<String> audios = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT audio_path FROM note_audios WHERE note_id=?",
                new String[]{String.valueOf(noteId)});

        if (cursor.moveToFirst()) {
            do {
                audios.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return audios;
    }
}
