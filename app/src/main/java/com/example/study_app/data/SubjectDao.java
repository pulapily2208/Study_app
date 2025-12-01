package com.example.study_app.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.study_app.ui.Subject.Model.Subject;

import java.util.ArrayList;

public class SubjectDao {

    private final DatabaseHelper dbHelper;

    public SubjectDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public ArrayList<String> getAllSemesterNames() {
        ArrayList<String> semesterNames = new ArrayList<>();
        String selectQuery = "SELECT ten_hoc_ky FROM hoc_ky ORDER BY nam_hoc DESC, id DESC";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(selectQuery, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int tenHocKyIndex = cursor.getColumnIndexOrThrow("ten_hoc_ky");
                do {
                    semesterNames.add(cursor.getString(tenHocKyIndex));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("SubjectDao", "Lỗi khi lấy danh sách học kỳ", e);
        }
        return semesterNames;
    }

    public int getSemesterIdByName(String semesterName) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int semesterId = -1;
        if (semesterName == null)
            return semesterId;
        try (Cursor cursor = db.query("hoc_ky", new String[] { "id" }, "ten_hoc_ky = ?", new String[] { semesterName },
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                semesterId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            }
        } catch (Exception e) {
            Log.e("SubjectDao", "Lỗi khi lấy ID học kỳ theo tên", e);
        }
        return semesterId;
    }

    public ArrayList<Subject> getSubjectsBySemester(String semesterName) {
        ArrayList<Subject> subjectList = new ArrayList<>();
        int semesterId = getSemesterIdByName(semesterName);
        if (semesterId == -1)
            return subjectList;

        String selectQuery = "SELECT m.* FROM mon_hoc m " +
                "INNER JOIN enrollments e ON m.ma_hp = e.ma_hp " +
                "WHERE e.hoc_ky = ?";

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.rawQuery(selectQuery, new String[] { String.valueOf(semesterId) })) {
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
                    subject.tenHk = semesterName;
                    subjectList.add(subject);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("SubjectDao", "Lỗi khi lấy danh sách môn học theo học kỳ", e);
        }
        return subjectList;
    }

    public Subject getSubjectByMaHp(String maHp) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Subject subject = null;
        String query = "SELECT m.*, hk.ten_hoc_ky FROM mon_hoc m " +
                "INNER JOIN enrollments e ON m.ma_hp = e.ma_hp " +
                "INNER JOIN hoc_ky hk ON e.hoc_ky = hk.id " +
                "WHERE m.ma_hp = ?";
        try (Cursor cursor = db.rawQuery(query, new String[] { maHp })) {
            if (cursor != null && cursor.moveToFirst()) {
                subject = new Subject();
                subject.maHp = cursor.getString(cursor.getColumnIndexOrThrow("ma_hp"));
                subject.tenHp = cursor.getString(cursor.getColumnIndexOrThrow("ten_hp"));
                subject.soTc = cursor.getInt(cursor.getColumnIndexOrThrow("so_tin_chi"));
                subject.loaiMon = cursor.getString(cursor.getColumnIndexOrThrow("loai_hp"));
                subject.tenGv = cursor.getString(cursor.getColumnIndexOrThrow("giang_vien"));
                subject.phongHoc = cursor.getString(cursor.getColumnIndexOrThrow("phong_hoc"));
                subject.ngayBatDau = dbHelper.parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_bat_dau")));
                subject.ngayKetThuc = dbHelper
                        .parseDate(cursor.getString(cursor.getColumnIndexOrThrow("ngay_ket_thuc")));
                subject.gioBatDau = dbHelper.parseTime(cursor.getString(cursor.getColumnIndexOrThrow("gio_bat_dau")));
                subject.gioKetThuc = dbHelper.parseTime(cursor.getString(cursor.getColumnIndexOrThrow("gio_ket_thuc")));
                subject.ghiChu = cursor.getString(cursor.getColumnIndexOrThrow("ghi_chu"));
                subject.mauSac = cursor.getString(cursor.getColumnIndexOrThrow("color_tag"));
                subject.soTuan = cursor.getInt(cursor.getColumnIndexOrThrow("so_tuan"));
                subject.tenHk = cursor.getString(cursor.getColumnIndexOrThrow("ten_hoc_ky"));
            }
        } catch (Exception e) {
            Log.e("SubjectDao", "Lỗi khi lấy chi tiết môn học", e);
        }
        return subject;
    }

    /**
     * Tìm định nghĩa một môn học chỉ từ bảng mon_hoc.
     * Được sử dụng để kiểm tra sự tồn tại trước khi thêm mới hoặc cập nhật.
     */
    private Subject getSubjectDefinition(String maHp) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Subject subject = null;
        try (Cursor cursor = db.query("mon_hoc", new String[] { "ma_hp", "ten_hp" }, "ma_hp = ?", new String[] { maHp },
                null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                subject = new Subject();
                subject.maHp = cursor.getString(cursor.getColumnIndexOrThrow("ma_hp"));
                subject.tenHp = cursor.getString(cursor.getColumnIndexOrThrow("ten_hp"));
            }
        } catch (Exception e) {
            Log.e("SubjectDao", "Lỗi khi lấy định nghĩa môn học", e);
        }
        return subject;
    }

    public long addOrEnrollSubject(Subject subject) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        long resultId = -1;

        int semesterId = getSemesterIdByName(subject.tenHk);
        if (semesterId == -1) {
            Log.e("SubjectDao", "Không tìm thấy học kỳ: " + subject.tenHk);
            return -1;
        }

        Subject existing = getSubjectDefinition(subject.maHp); // Sử dụng phương thức kiểm tra đã sửa
        db.beginTransaction();
        try {
            if (existing == null) {
                ContentValues values = new ContentValues();
                values.put("ma_hp", subject.maHp);
                values.put("ten_hp", subject.tenHp);
                values.put("so_tin_chi", subject.soTc);
                values.put("loai_hp", subject.loaiMon);
                values.put("giang_vien", subject.tenGv);
                values.put("phong_hoc", subject.phongHoc);
                values.put("ngay_bat_dau", dbHelper.formatDate(subject.ngayBatDau));
                values.put("ngay_ket_thuc", dbHelper.formatDate(subject.ngayKetThuc));
                values.put("gio_bat_dau", dbHelper.formatTime(subject.gioBatDau));
                values.put("gio_ket_thuc", dbHelper.formatTime(subject.gioKetThuc));
                values.put("ghi_chu", subject.ghiChu);
                values.put("color_tag", subject.mauSac);
                values.put("so_tuan", subject.soTuan);

                resultId = db.insertOrThrow("mon_hoc", null, values);
            } else {
                ContentValues update = new ContentValues();
                if (subject.tenGv != null && !subject.tenGv.isEmpty())
                    update.put("giang_vien", subject.tenGv);
                if (subject.phongHoc != null && !subject.phongHoc.isEmpty())
                    update.put("phong_hoc", subject.phongHoc);
                if (subject.ngayBatDau != null)
                    update.put("ngay_bat_dau", dbHelper.formatDate(subject.ngayBatDau));
                if (subject.ngayKetThuc != null)
                    update.put("ngay_ket_thuc", dbHelper.formatDate(subject.ngayKetThuc));
                if (subject.gioBatDau != null)
                    update.put("gio_bat_dau", dbHelper.formatTime(subject.gioBatDau));
                if (subject.gioKetThuc != null)
                    update.put("gio_ket_thuc", dbHelper.formatTime(subject.gioKetThuc));
                if (subject.ghiChu != null)
                    update.put("ghi_chu", subject.ghiChu);
                if (subject.mauSac != null)
                    update.put("color_tag", subject.mauSac);
                if (subject.soTuan > 0)
                    update.put("so_tuan", subject.soTuan);
                if (update.size() > 0) {
                    db.update("mon_hoc", update, "ma_hp = ?", new String[] { subject.maHp });
                }
                resultId = 1; // Đánh dấu thành công
            }

            enrollSubjectInSemester(subject.maHp, semesterId);

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("SubjectDao", "Thêm hoặc ghi danh môn học thất bại", e);
            resultId = -1;
        } finally {
            db.endTransaction();
        }
        return resultId;
    }

    public void enrollSubjectInSemester(String maHp, int semesterId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Resolve current user id using the already-open database to avoid
        // opening a second DB connection while a transaction is in progress.
        int currentUserId = getCurrentUserIdFromDb(db);
        values.put("user_id", currentUserId);
        values.put("ma_hp", maHp);
        values.put("hoc_ky", semesterId);
        db.insertWithOnConflict("enrollments", null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /**
     * Resolve current user id using the provided SQLiteDatabase instance.
     * This prevents creating another DatabaseHelper / SQLiteDatabase while
     * a transaction is active on the caller's DB.
     */
    private int getCurrentUserIdFromDb(SQLiteDatabase db) {
        int userId = 1; // fallback
        try (android.database.Cursor c = db.rawQuery("SELECT id FROM users WHERE is_active = 1 LIMIT 1", null)) {
            if (c != null && c.moveToFirst()) {
                userId = c.getInt(0);
                return userId;
            }
        } catch (Exception e) {
            Log.w("SubjectDao", "Could not query active user", e);
        }

        try (android.database.Cursor c2 = db.rawQuery("SELECT id FROM users ORDER BY id LIMIT 1", null)) {
            if (c2 != null && c2.moveToFirst()) {
                userId = c2.getInt(0);
            }
        } catch (Exception e) {
            Log.w("SubjectDao", "Could not query fallback user", e);
        }

        return userId;
    }

    public int updateSubject(Subject subject) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsAffected = 0;
        int semesterId = getSemesterIdByName(subject.tenHk);
        if (semesterId == -1) {
            Log.e("SubjectDao", "Không thể cập nhật môn học. Không tìm thấy học kỳ: " + subject.tenHk);
            return 0;
        }

        ContentValues values = new ContentValues();
        values.put("ten_hp", subject.tenHp);
        values.put("so_tin_chi", subject.soTc);
        values.put("loai_hp", subject.loaiMon);
        values.put("giang_vien", subject.tenGv);
        values.put("phong_hoc", subject.phongHoc);
        values.put("ngay_bat_dau", dbHelper.formatDate(subject.ngayBatDau));
        values.put("ngay_ket_thuc", dbHelper.formatDate(subject.ngayKetThuc));
        values.put("gio_bat_dau", dbHelper.formatTime(subject.gioBatDau));
        values.put("gio_ket_thuc", dbHelper.formatTime(subject.gioKetThuc));
        values.put("ghi_chu", subject.ghiChu);
        values.put("color_tag", subject.mauSac);
        values.put("so_tuan", subject.soTuan);

        db.beginTransaction();
        try {
            rowsAffected = db.update("mon_hoc", values, "ma_hp = ?", new String[] { subject.maHp });

            // Update enrollment to new semester without deleting history
            int currentUserId = getCurrentUserIdFromDb(db);
            ContentValues enrollUpdate = new ContentValues();
            enrollUpdate.put("hoc_ky", semesterId);
            int updated = db.update(
                    "enrollments",
                    enrollUpdate,
                    "ma_hp = ? AND user_id = ?",
                    new String[] { subject.maHp, String.valueOf(currentUserId) });
            if (updated == 0) {
                // If no existing enrollment row, insert one
                enrollSubjectInSemester(subject.maHp, semesterId);
            }

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("SubjectDao", "Cập nhật môn học hoặc ghi danh thất bại", e);
            rowsAffected = 0; // Đảm bảo báo cáo thất bại
        } finally {
            db.endTransaction();
        }
        return rowsAffected;
    }

    public void deleteSubject(String maHp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("mon_hoc", "ma_hp = ?", new String[] { maHp });
    }
}
