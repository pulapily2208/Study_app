package com.example.study_app.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Simple helper to resolve current user id for apps without full auth flow.
 * It looks for an active user (is_active = 1) and returns its id, otherwise
 * falls back to the first user row or 1.
 */
public class UserSession {

    public static int getCurrentUserId(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // Try to find active user first
            try (Cursor c = db.rawQuery("SELECT id FROM users WHERE is_active = 1 LIMIT 1", null)) {
                if (c != null && c.moveToFirst()) {
                    return c.getInt(0);
                }
            } catch (Exception ignored) {
            }

            // Fallback: first user row
            try (Cursor c = db.rawQuery("SELECT id FROM users ORDER BY id LIMIT 1", null)) {
                if (c != null && c.moveToFirst()) {
                    return c.getInt(0);
                }
            } catch (Exception ignored) {
            }

            // Final fallback: 1
            return 1;
        } finally {
            // Ensure the helper (and its DB) is closed to avoid leaked SQLite connections
            try {
                dbHelper.close();
            } catch (Exception ignored) {
            }
        }
    }
}
