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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DeadlineDao extends SQLiteOpenHelper {

    private static final String DB_NAME = "study_app.db";
    private static final int DB_VERSION = 11;

    private final Context context;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


    public DeadlineDao(Context context) {
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
        db.execSQL("DROP TABLE IF EXISTS mon_hoc");
        db.execSQL("DROP TABLE IF EXISTS deadline");

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
    private void sortDeadlines(ArrayList<Deadline> list) {
        Date now = new Date();

        list.sort((d1, d2) -> {
            int prio1 = getPriority(d1, now);
            int prio2 = getPriority(d2, now);

            // So sánh theo độ ưu tiên 3 nhóm
            if (prio1 != prio2) {
                return Integer.compare(prio1, prio2);
            }

            // Nếu cùng nhóm thì so theo ngày kết thúc
            if (d1.getNgayKetThuc() != null && d2.getNgayKetThuc() != null) {
                return d1.getNgayKetThuc().compareTo(d2.getNgayKetThuc());
            }

            return 0;
        });
    }

    private int getPriority(Deadline d, Date now) {
        boolean completed = d.isCompleted();
        boolean expired = d.getNgayKetThuc() != null && d.getNgayKetThuc().before(now);

        if (completed) return 3;      // thấp nhất
        if (expired) return 2;        // giữa
        return 1;                     // cao nhất
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

    public Subject getSubjectByMaHp(String maHp) {
        SQLiteDatabase db = this.getReadableDatabase();
        Subject subject = null;
        String query = "SELECT * FROM mon_hoc WHERE ma_hp = ? ";
        try (Cursor cursor = db.rawQuery(query, new String[]{maHp})) {
            if (cursor != null && cursor.moveToFirst()) {
                subject = new Subject();
                subject.maHp = cursor.getString(cursor.getColumnIndexOrThrow("ma_hp"));
                subject.tenHp = cursor.getString(cursor.getColumnIndexOrThrow("ten_hp"));
                subject.ngayBatDau = parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_bat_dau")));
                subject.ngayKetThuc = parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc")));
                subject.soTuan = cursor.getInt(cursor.getColumnIndexOrThrow("so_tuan"));
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting subject detail", e);
        }
        return subject;
    }
  public ArrayList<Deadline> getDeadlinesByMaHp(String maHp) {
        ArrayList<Deadline> deadlineList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM deadline WHERE ma_hp = ? ORDER BY ngay_ket_thuc ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{maHp})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Deadline deadline = cursorToDeadline(cursor);
                    deadlineList.add(deadline);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting deadlines for maHp: " + maHp, e);
        }

        sortDeadlines(deadlineList);

        return deadlineList;
    }

    public ArrayList<Deadline> getDeadlinesByWeek(String maHp, Date subjectStartDate, int weekIndex) {
        ArrayList<Deadline> list = new ArrayList<>();
        if (subjectStartDate == null) return list;

        SQLiteDatabase db = this.getReadableDatabase();

        Calendar cal = Calendar.getInstance();
        cal.setTime(subjectStartDate);
        cal.add(Calendar.DATE, weekIndex * 7);
        String weekStartStr = formatDateTime(cal.getTime());

        cal.add(Calendar.DATE, 7);
        String weekEndStr = formatDateTime(cal.getTime());

        String query = "SELECT * FROM deadline WHERE ma_hp = ? AND ngay_bat_dau >= ? AND ngay_bat_dau < ? ORDER BY ngay_bat_dau ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{maHp, weekStartStr, weekEndStr})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Deadline d = cursorToDeadline(cursor);
                    list.add(d);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting deadlines by week", e);
        }

        sortDeadlines(list);

        return list;
    }

    public long addDeadline(Deadline deadline, String maHp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = deadlineToContentValues(deadline);
        values.put("ma_hp", maHp);
        try {
            return db.insertOrThrow("deadline", null, values);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Failed to add deadline", e);
            return -1;
        }
    }

    public int updateDeadline(Deadline deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = deadlineToContentValues(deadline);
        return db.update("deadline", values, "id = ?", new String[]{String.valueOf(deadline.getId())});
    }

    public boolean deleteDeadline(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete("deadline", "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    // Helper Methods
    private Deadline cursorToDeadline(Cursor cursor) {
        Deadline d = new Deadline();
        d.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        d.setTieuDe(cursor.getString(cursor.getColumnIndexOrThrow("tieu_de")));
        d.setNoiDung(cursor.getString(cursor.getColumnIndexOrThrow("noi_dung")));
        d.setNgayBatDau(parseDateTime(cursor.getString(cursor.getColumnIndexOrThrow("ngay_bat_dau"))));
        d.setNgayKetThuc(parseDateTime(cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc"))));
        d.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow("completed")) == 1);
        d.setRepeat(cursor.getString(cursor.getColumnIndexOrThrow("repeat_type")));
        d.setReminder(cursor.getString(cursor.getColumnIndexOrThrow("reminder_time")));
        d.setIcon(cursor.getInt(cursor.getColumnIndexOrThrow("icon")));
        d.setNote(cursor.getString(cursor.getColumnIndexOrThrow("notes")));
        d.setWeekIndex(cursor.getInt(cursor.getColumnIndexOrThrow("weekIndex")));
        d.setMaHp(cursor.getString(cursor.getColumnIndexOrThrow("ma_hp")));
        return d;
    }

    private ContentValues deadlineToContentValues(Deadline deadline) {

        ContentValues values = new ContentValues();

        values.put("tieu_de", deadline.getTieuDe());
        values.put("noi_dung", deadline.getNoiDung());
        values.put("ngay_bat_dau", formatDateTime(deadline.getNgayBatDau()));
        values.put("ngay_ket_thuc", formatDateTime(deadline.getNgayKetThuc()));
        values.put("completed", deadline.isCompleted() ? 1 : 0);
        values.put("repeat_type", deadline.getRepeatText());
        values.put("reminder_time", deadline.getReminderText());
        values.put("icon", deadline.getIcon());
        values.put("weekIndex", deadline.getWeekIndex());

        return values;
    }


    public ArrayList<Subject> getSubjectsWithDeadlines() {
    ArrayList<Subject> list = new ArrayList<>();
    SQLiteDatabase db = this.getReadableDatabase();

    // Chỉ lấy môn học có deadline chưa hoàn thành
    String distinctMaHpQuery =
            "SELECT DISTINCT ma_hp FROM deadline WHERE completed = 0";
//        String query = "SELECT * FROM mon_hoc m JOIN deadline d ON m.ma_hp = d.ma_hp WHERE m.ma_hp = ? AND d.completed = 0";
    try (Cursor maHpCursor = db.rawQuery(distinctMaHpQuery, null)) {
        if (maHpCursor != null && maHpCursor.moveToFirst()) {
            do {
                String maHp = maHpCursor.getString(0);
                Subject subject = getSubjectByMaHp(maHp);
                if (subject != null) {
                    list.add(subject);
                }
            } while (maHpCursor.moveToNext());
        }
    } catch (Exception e) {
        Log.e("DatabaseHelper", "Error getting subjects with deadlines", e);
    }

    return list;
}
    public ArrayList<Deadline> getTodaysDeadlines() {
        ArrayList<Deadline> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Bắt đầu hôm nay 00:00:00
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.HOUR_OF_DAY, 0);
        calStart.set(Calendar.MINUTE, 0);
        calStart.set(Calendar.SECOND, 0);
        String startOfTodayStr = formatDateTime(calStart.getTime());

        // Kết thúc hôm nay 23:59:59
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.HOUR_OF_DAY, 23);
        calEnd.set(Calendar.MINUTE, 59);
        calEnd.set(Calendar.SECOND, 59);
        String endOfTodayStr = formatDateTime(calEnd.getTime());

        String query = "SELECT d.*, s.ten_hp " +
                "FROM deadline d " +
                "LEFT JOIN mon_hoc s ON d.ma_hp = s.ma_hp " +
                "WHERE d.completed = 0 AND d.ngay_ket_thuc BETWEEN ? AND ? " +
                "ORDER BY d.ngay_ket_thuc ASC";

        try (Cursor cursor = db.rawQuery(query, new String[]{startOfTodayStr, endOfTodayStr})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Deadline d = cursorToDeadline(cursor);

                    // Gán tên môn học vào Deadline nếu muốn
                    int tenHpIndex = cursor.getColumnIndex("ten_hp");
                    if (tenHpIndex != -1) {
                        d.setTieuDe(cursor.getString(tenHpIndex)+"\nDeadline: "+d.getTieuDe()); // thêm setter setTenMon trong Deadline

                    }

                    list.add(d);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting today's deadlines", e);
        } finally {
            db.close();
        }

        sortDeadlines(list);
        return list;
    }
}