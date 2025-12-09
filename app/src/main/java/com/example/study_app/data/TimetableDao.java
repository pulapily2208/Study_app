package com.example.study_app.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

        // --- THÁNG 9 → 12: kỳ LẺ (1,3,5,7) ---
        if (month >= 9 && month <= 12) {

            // năm học = year–(year+1)
            schoolYear = year + "-" + (year + 1);

            // năm học bắt đầu = số năm thứ mấy?
            int yearIndex = getYearIndex(year); // 1,2,3,4...
            int semesterNumber = (yearIndex - 1) * 2 + 1; // 1,3,5,7
            expectedName = "Học kỳ " + semesterNumber;
        }

        // --- THÁNG 1 → 5: kỳ CHẴN (2,4,6,8) ---
        else if (month >= 1 && month <= 5) {

            schoolYear = (year - 1) + "-" + year;

            int yearIndex = getYearIndex(year - 1);
            int semesterNumber = (yearIndex - 1) * 2 + 2; // 2,4,6,8
            expectedName = "Học kỳ " + semesterNumber;
        }

        // --- THÁNG 6–8: kỳ hè ---
        else { // months 6,7,8

            schoolYear = (year - 1) + "-" + year;

            int yearIndex = getYearIndex(year - 1);
            expectedName = "Học kỳ hè năm " + yearIndex;
        }

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT id, ten_hoc_ky FROM hoc_ky WHERE nam_hoc = ?",
                new String[] { schoolYear });

        if (cursor == null)
            return null;

        expectedName = expectedName.trim();

        Integer semesterId = null;

        while (cursor.moveToNext()) {
            int id = cursor.getInt(0);
            String name = cursor.getString(1).trim();

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
        if (startYear == 2023)
            return 1;
        else if (startYear == 2024)
            return 2;
        else if (startYear == 2025)
            return 3;
        else if (startYear == 2026)
            return 4;
        return 1; // mặc định
    }

    public String getSemesterNameById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT ten_hoc_ky FROM hoc_ky WHERE id = ?", new String[] { String.valueOf(id) });

        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            return name;
        }

        return null;
    }

    // lấy all môn học để hiển thi lên bảng
    public ArrayList<Subject> getAllSubjects() {
        ArrayList<Subject> subjectList = new ArrayList<>();

        String selectQuery = "SELECT m.*, e.hoc_ky " +
                "FROM mon_hoc m " +
                "LEFT JOIN enrollments e ON m.ma_hp = e.ma_hp";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Subject subject = new Subject();
                    subject.maHp = cursor.getString(cursor.getColumnIndexOrThrow("ma_hp"));
                    subject.tenHp = cursor.getString(cursor.getColumnIndexOrThrow("ten_hp"));
                    subject.soTc = cursor.getInt(cursor.getColumnIndexOrThrow("so_tin_chi"));
                    subject.loaiMon = cursor.getString(cursor.getColumnIndexOrThrow("loai_hp"));
                    subject.tenGv = cursor.getString(cursor.getColumnIndexOrThrow("giang_vien"));
                    subject.phongHoc = cursor.getString(cursor.getColumnIndexOrThrow("phong_hoc"));

                    // Chuyển String ngày/giờ sang Date
                    subject.ngayBatDau = dbHelper
                            .parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_bat_dau")));
                    subject.ngayKetThuc = dbHelper
                            .parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc")));
                    subject.gioBatDau = dbHelper
                            .parseTime(cursor.getString(cursor.getColumnIndexOrThrow("gio_bat_dau")));
                    subject.gioKetThuc = dbHelper
                            .parseTime(cursor.getString(cursor.getColumnIndexOrThrow("gio_ket_thuc")));

                    subject.ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu"));
                    subject.mauSac = cursor.getString(cursor.getColumnIndexOrThrow("color_tag"));
                    subject.soTuan = cursor.getInt(cursor.getColumnIndexOrThrow("so_tuan"));

                    // Tên học kỳ nếu cần
                    int hocKyId = cursor.getInt(cursor.getColumnIndexOrThrow("hoc_ky"));
                    // subject.tenHk = getSemesterNameById(hocKyId);

                    subjectList.add(subject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting all subjects", e);
        }

        return subjectList;
    }

    /**
     * Trả về danh sách môn để hiển thị lên thời khóa biểu:
     * - Chỉ các môn mà user đã ghi danh (enrollments.user_id = userId)
     * - Và chỉ các môn có trạng thái IN_PROGRESS (Đang học) hoặc COMPLETED (Đã học)
     * Trạng thái được tính dựa trên ngày_ket_thuc (nếu có).
     */
    public ArrayList<Subject> getSubjectsForTimetable(int userId) {
        ArrayList<Subject> subjectList = new ArrayList<>();
        // Use CurriculumDao to compute statuses and pick only IN_PROGRESS or COMPLETED
        CurriculumDao curriculumDao = new CurriculumDao(dbHelper);
        java.util.List<com.example.study_app.ui.Curriculum.Model.Curriculum> courses = curriculumDao
                .getAllCoursesForCurriculumWithStatus(userId);

        java.util.Set<String> allowedMaHp = new java.util.HashSet<>();
        for (com.example.study_app.ui.Curriculum.Model.Curriculum c : courses) {
            try {
                java.lang.reflect.Method m = c.getClass().getMethod("getStatus");
                Object statusObj = m.invoke(c);
                String status = statusObj != null ? statusObj.toString() : null;
                if (DatabaseHelper.STATUS_IN_PROGRESS.equals(status)
                        || DatabaseHelper.STATUS_COMPLETED.equals(status)) {
                    allowedMaHp.add(c.getMaHp());
                }
            } catch (NoSuchMethodException nsme) {
                // if Curriculum doesn't have getStatus, fallback: include if enrolled
                // curriculumDao already sets status via reflection; so ignore
            } catch (Exception ignored) {
            }
        }

        if (allowedMaHp.isEmpty())
            return subjectList;

        // Build placeholders for IN clause
        StringBuilder inClause = new StringBuilder();
        String[] params = new String[allowedMaHp.size()];
        int i = 0;
        for (String code : allowedMaHp) {
            if (i > 0)
                inClause.append(",");
            inClause.append("?");
            params[i++] = code;
        }

        String selectQuery2 = "SELECT m.* FROM mon_hoc m WHERE m.ma_hp IN (" + inClause.toString() + ") " +
                "AND m.ngay_bat_dau IS NOT NULL AND m.gio_bat_dau IS NOT NULL AND m.gio_ket_thuc IS NOT NULL";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(selectQuery2, params)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    Subject subject = new Subject();
                    subject.maHp = cursor.getString(cursor.getColumnIndexOrThrow("ma_hp"));
                    subject.tenHp = cursor.getString(cursor.getColumnIndexOrThrow("ten_hp"));
                    subject.soTc = cursor.getInt(cursor.getColumnIndexOrThrow("so_tin_chi"));
                    subject.loaiMon = cursor.getString(cursor.getColumnIndexOrThrow("loai_hp"));
                    subject.tenGv = cursor.getString(cursor.getColumnIndexOrThrow("giang_vien"));
                    subject.phongHoc = cursor.getString(cursor.getColumnIndexOrThrow("phong_hoc"));

                    subject.ngayBatDau = dbHelper
                            .parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_bat_dau")));
                    subject.ngayKetThuc = dbHelper
                            .parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc")));
                    subject.gioBatDau = dbHelper
                            .parseTime(cursor.getString(cursor.getColumnIndexOrThrow("gio_bat_dau")));
                    subject.gioKetThuc = dbHelper
                            .parseTime(cursor.getString(cursor.getColumnIndexOrThrow("gio_ket_thuc")));

                    subject.ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu"));
                    subject.mauSac = cursor.getString(cursor.getColumnIndexOrThrow("color_tag"));
                    subject.soTuan = cursor.getInt(cursor.getColumnIndexOrThrow("so_tuan"));

                    subjectList.add(subject);
                    android.util.Log.d("TimetableDao",
                            "Loaded subject for timetable: " + subject.maHp + " title=" + subject.tenHp
                                    + " start=" + dbHelper.formatDate(subject.ngayBatDau) + " end="
                                    + dbHelper.formatDate(subject.ngayKetThuc)
                                    + " startTime=" + dbHelper.formatTime(subject.gioBatDau) + " endTime="
                                    + dbHelper.formatTime(subject.gioKetThuc));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            android.util.Log.e("TimetableDao", "Error getting subjects for timetable (by status)", e);
        }

        return subjectList;
    }

}
