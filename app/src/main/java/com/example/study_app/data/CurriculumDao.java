package com.example.study_app.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.study_app.ui.Curriculum.Model.Curriculum;
import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurriculumDao {

    private final DatabaseHelper dbHelper;

    public CurriculumDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Map<String, Integer> getFacultiesMap() {
        Map<String, Integer> faculties = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query("khoa", new String[]{"id", "ten_khoa"}, null, null, null, null, "ten_khoa ASC")) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    faculties.put(cursor.getString(cursor.getColumnIndexOrThrow("ten_khoa")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CurriculumDao", "Error getting faculties map", e);
        }
        return faculties;
    }

    public List<String> getAllCourseGroups() {
        List<String> groups = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
            Log.e("CurriculumDao", "Error getting course groups", e);
        }
        return groups;
    }

    public List<Curriculum> getAllCoursesForCurriculum() {
        List<Curriculum> courses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
                    int nameIndex = cursor.getColumnIndex("ten_nhom_day_du");
                    String realGroupName = "";
                    if (nameIndex != -1) {
                        realGroupName = cursor.getString(nameIndex);
                    }
                    if (realGroupName != null && !realGroupName.isEmpty()) {
                        course.setNhomTuChon(realGroupName);
                    } else {
                        String originalValue = cursor.getString(cursor.getColumnIndexOrThrow("nhom_tu_chon"));
                        course.setNhomTuChon(originalValue != null ? originalValue : "");
                    }
                    courses.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CurriculumDao", "Error getting all courses for curriculum", e);
        }
        return courses;
    }

    public Map<String, Integer> getEnrolledSubjectsMap(int userId) {
        Map<String, Integer> map = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT ma_hp, hoc_ky FROM enrollments WHERE user_id = ?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                do {
                    map.put(c.getString(c.getColumnIndexOrThrow("ma_hp")),
                            c.getInt(c.getColumnIndexOrThrow("hoc_ky")));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CurriculumDao", "getEnrolledSubjectsMap error", e);
        }
        return map;
    }

    private String computeSubjectStatus(boolean enrolled, Date endDate) {
        if (!enrolled) return DatabaseHelper.STATUS_NOT_ENROLLED;
        if (endDate != null) {
            Date today = new Date();
            if (endDate.before(today)) return DatabaseHelper.STATUS_COMPLETED;
            return DatabaseHelper.STATUS_IN_PROGRESS;
        }
        return DatabaseHelper.STATUS_IN_PROGRESS;
    }

    public List<Curriculum> getAllCoursesForCurriculumWithStatus(int userId) {
        List<Curriculum> courses = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
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
                    Date endDate = dbHelper.parseDate(endDateStr);
                    boolean enrolled = enrolledMap.containsKey(course.getMaHp());
                    String status = computeSubjectStatus(enrolled, endDate);
                    try {
                        Curriculum.class.getMethod("setStatus", String.class).invoke(course, status);
                    } catch (Exception ignored) {
                    }
                    courses.add(course);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CurriculumDao", "Error getAllCoursesForCurriculumWithStatus", e);
        }
        return courses;
    }

    public List<String> searchSubjectCodes(String prefix) {
        List<String> subjectCodes = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT ma_hp FROM mon_hoc WHERE ma_hp LIKE ? ORDER BY ma_hp LIMIT 20";
        try (Cursor cursor = db.rawQuery(query, new String[]{prefix + "%"})) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    subjectCodes.add(cursor.getString(cursor.getColumnIndexOrThrow("ma_hp")));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CurriculumDao", "Error searching subject codes", e);
        }
        return subjectCodes;
    }

    public Curriculum getCurriculumDetailsByMaHp(String maHp) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Curriculum curriculum = null;
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
                int nameIndex = cursor.getColumnIndex("ten_nhom_day_du");
                String realGroupName = null;
                if (nameIndex != -1) {
                    realGroupName = cursor.getString(nameIndex);
                }
                if (realGroupName != null && !realGroupName.isEmpty()) {
                    curriculum.setNhomTuChon(realGroupName);
                } else {
                    curriculum.setNhomTuChon("");
                }
            }
        } catch (Exception e) {
            Log.e("CurriculumDao", "Error getting curriculum details by maHp", e);
        }
        return curriculum;
    }

    public List<String> getPrerequisites(String maHp) {
        List<String> prerequisites = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String query = "SELECT ma_hp_tien_quyet FROM hoc_phan_tien_quyet WHERE ma_hp = ?";
        try (Cursor cursor = db.rawQuery(query, new String[]{maHp})) {
            if (cursor != null && cursor.moveToFirst()) {
                int preReqIndex = cursor.getColumnIndexOrThrow("ma_hp_tien_quyet");
                do {
                    prerequisites.add(cursor.getString(preReqIndex));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("CurriculumDao", "Error getting prerequisites for " + maHp, e);
        }
        return prerequisites;
    }

    public boolean checkPrerequisiteStatus(String maHp, int userId, SubjectDao subjectDao) {
        List<String> prerequisites = getPrerequisites(maHp);
        if (prerequisites.isEmpty()) {
            return true;
        }
        Map<String, Integer> enrolledMap = getEnrolledSubjectsMap(userId);
        for (String preReqMaHp : prerequisites) {
            if (!enrolledMap.containsKey(preReqMaHp)) {
                return false;
            }
            Subject preReqSubject = subjectDao.getSubjectByMaHp(preReqMaHp);
            if (preReqSubject == null || preReqSubject.ngayKetThuc == null) {
                return false;
            }
            String status = computeSubjectStatus(true, preReqSubject.ngayKetThuc);
            if (!status.equals(DatabaseHelper.STATUS_COMPLETED)) {
                Log.d("CurriculumDao", "Prerequisite " + preReqMaHp + " is not completed. Status: " + status);
                return false;
            }
        }
        return true;
    }

    public ArrayList<Curriculum> getSubjectsForNote(int userId) {
        ArrayList<Curriculum> list = new ArrayList<>();
        for (Curriculum c : getAllCoursesForCurriculumWithStatus(userId)) {
            if ("IN_PROGRESS".equals(c.getStatus()) || "COMPLETED".equals(c.getStatus())) {
                list.add(c);
            }
        }
        return list;
    }

}
