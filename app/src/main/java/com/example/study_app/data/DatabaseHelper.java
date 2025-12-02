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
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
            Locale.getDefault());

    public static final String STATUS_NOT_ENROLLED = "NOT_ENROLLED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    /**
     * Expose the application context used by this helper so DAOs can resolve
     * the current user via helpers like UserSession without keeping extra
     * Context fields elsewhere.
     */
    public Context getContext() {
        return context;
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
        Log.w("DatabaseHelper", "Upgrading database from version " + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
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
        if (dateStr == null || dateStr.isEmpty())
            return null;

        // Try the primary app format first (dd/MM/yyyy)
        try {
            return dateFormat.parse(dateStr);
        } catch (ParseException ignored) {
        }

        // Try ISO-like yyyy-MM-dd which may come from some inputs
        try {
            SimpleDateFormat isoDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return isoDate.parse(dateStr);
        } catch (ParseException ignored) {
        }

        // Try full datetime format
        try {
            return dateTimeFormat.parse(dateStr);
        } catch (ParseException e) {
            Log.w("DatabaseHelper", "Could not parse date string: '" + dateStr + "'");
            return null;
        }
    }

    Date parseTime(String timeStr) {
        if (timeStr == null)
            return null;

        String original = timeStr;
        String s = timeStr.trim();
        if (s.isEmpty())
            return null;

        // Normalize common variants before parsing
        // 1) Replace '7h30' or '7 H 30' or '7 giờ 30' with '7:30'
        s = s.replaceAll("(?i)\\s*giờ\\s*", ":");
        s = s.replaceAll("(?i)(\\d)\\s*[hH]\\s*(\\d)", "$1:$2");
        // 2) Replace dot separator like '07.30' -> '07:30'
        s = s.replaceAll("(\\d)\\.(\\d)", "$1:$2");
        // 3) Collapse spaces
        s = s.replaceAll("\\s+", " ");

        // 4) Handle compact forms like 730 or 0730
        if (s.matches("^\\d{3,4}$")) {
            if (s.length() == 3) { // e.g., 730 -> 7:30
                s = s.charAt(0) + ":" + s.substring(1);
            } else { // 4 digits
                s = s.substring(0, 2) + ":" + s.substring(2);
            }
        }

        // 5) Handle AM/PM forms: '07:30 PM' or '7:30pm'
        if (s.matches("(?i).*[AP]M$")) {
            try {
                SimpleDateFormat ampm = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                return ampm.parse(s.toUpperCase(Locale.getDefault()));
            } catch (ParseException ignored) {
            }
            try {
                SimpleDateFormat ampm2 = new SimpleDateFormat("h:mm a", Locale.getDefault());
                return ampm2.parse(s.toUpperCase(Locale.getDefault()));
            } catch (ParseException ignored) {
            }
        }

        // Try preferred and fallback 24-hour patterns
        String[] patterns = new String[] {
                "HH:mm",
                "H:mm",
                "HH:mm:ss",
                "H:mm:ss",
                "HH:m",
                "H:m"
        };
        for (String p : patterns) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(p, Locale.getDefault());
                f.setLenient(false);
                return f.parse(s);
            } catch (ParseException ignored) {
            }
        }

        Log.w("DatabaseHelper", "Could not parse time string: '" + original + "' (normalized: '" + s + "')");
        return null;
    }

    String formatDate(Date date) {
        if (date == null)
            return null;
        return dateFormat.format(date);
    }

    String formatTime(Date time) {
        if (time == null)
            return null;
        return timeFormat.format(time);
    }

    Date parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty())
            return null;
        try {
            return dateTimeFormat.parse(dateTimeStr);
        } catch (ParseException e) {
            Log.e("DatabaseHelper", "Error parsing dateTime: " + dateTimeStr, e);
            return null;
        }
    }

    String formatDateTime(Date date) {
        if (date == null)
            return null;
        return dateTimeFormat.format(date);
    }

    private void runSqlFromRaw(SQLiteDatabase db, int resId) {
        try (InputStream inputStream = context.getResources().openRawResource(resId);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder sqlBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                // Remove inline SQL comments that start with --
                int commentIndex = line.indexOf("--");
                if (commentIndex >= 0) {
                    line = line.substring(0, commentIndex).trim();
                    if (line.isEmpty())
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
}