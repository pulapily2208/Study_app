package com.example.study_app.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class TimetableDao {
    private final DatabaseHelper dbHelper;

    public TimetableDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }



    // Lấy học kỳ dựa theo ngày hiện tại để thêm môn
    public Integer getSemesterIdBySelectedDate(int year, int month) {

        month += 1; // chỉnh 0–11 thành 1–12

        String schoolYear;
        String expectedName = null;

        // --- THÁNG 8 → 12: kỳ lẻ ---
        if (month >= 8 && month <= 12) {

            // năm học = year–(year+1)
            schoolYear = year + "-" + (year + 1);

            // năm học bắt đầu = số năm thứ mấy?
            int yearIndex = getYearIndex(year); // 1,2,3,4...

            int semesterNumber = (yearIndex - 1) * 2 + 1; // 1,3,5,7,...
            expectedName = "học kỳ " + semesterNumber;
        }

        // --- THÁNG 1 → 5: kỳ chẵn ---
        else if (month >= 1 && month <= 5) {

            schoolYear = (year - 1) + "-" + year;

            int yearIndex = getYearIndex(year - 1);

            int semesterNumber = (yearIndex - 1) * 2 + 2; // 2,4,6,8,...
            expectedName = "học kỳ " + semesterNumber;
        }

        // --- THÁNG 6–7: kỳ hè ---
        else {

            schoolYear = (year - 1) + "-" + year;

            int yearIndex = getYearIndex(year - 1);

            expectedName = "học kỳ hè năm " + yearIndex;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, ten_hoc_ky FROM hoc_ky WHERE nam_hoc = ?",
                new String[]{schoolYear}
        );

        if (cursor == null) return null;

        expectedName = expectedName.toLowerCase().trim();

        Integer semesterId = null;

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1).toLowerCase().trim();

            if (name.equals(expectedName)) {
                semesterId = id;
                break;
            }
        }

        cursor.close();
        return semesterId; // có thể null
    }


    // --- HÀM XÁC ĐỊNH NĂM THỨ MẤY TRONG CHƯƠNG TRÌNH ĐẠI HỌC ---
    private int getYearIndex(int startYear) {
        // năm 1: 2023–2024
        // năm 2: 2024–2025
        // năm 3: 2025–2026
        // năm 4: 2026–2027
        if (startYear == 2023) return 1;
        else if (startYear == 2024) return 2;
        else if (startYear == 2025) return 3;
        else if (startYear == 2026) return 4;
        return 1; // mặc định
    }

    public String getSemesterNameById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ten_hoc_ky FROM hoc_ky WHERE id = ?", new String[]{String.valueOf(id)});

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }

        return null;
    }


}
