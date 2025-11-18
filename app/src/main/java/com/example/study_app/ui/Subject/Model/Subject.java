package com.example.study_app.ui.Subject.Model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subjects")
public class Subject {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String maMon;
    public String tenMon;
    public String phongHoc;
    public String loaiMon; // "Chuyên ngành" hoặc "Môn chung"
    public String ngayBatDau;
    public String ngayKetThuc;
    public int soTuanHoc;
    public String gioBatDau;
    public String gioKetThuc;
    public String giangVien;
    public int soTinChi;
    public String ghiChu;

    // Constructors
    public Subject() {
        // Default constructor required for Room
    }

    public Subject(String maMon, String tenMon, String phongHoc, String loaiMon, String ngayBatDau, String ngayKetThuc, int soTuanHoc, String gioBatDau, String gioKetThuc, String giangVien, int soTinChi, String ghiChu) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.phongHoc = phongHoc;
        this.loaiMon = loaiMon;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.soTuanHoc = soTuanHoc;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.giangVien = giangVien;
        this.soTinChi = soTinChi;
        this.ghiChu = ghiChu;
    }
}
