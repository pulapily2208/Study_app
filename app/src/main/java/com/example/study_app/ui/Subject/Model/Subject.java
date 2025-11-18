package com.example.study_app.ui.Subject.Model;

public class Subject {

    public String maHp;
    public String tenHp;
    public int soTinChi;
    public String phongHoc;
    public String loaiHp;
    public String ngayBatDau;
    public String ngayKetThuc;
    public int soTuanHoc;
    public String gioBatDau;
    public String gioKetThuc;
    public String giangVien;
    public String ghiChu;


    // Default constructor
    public Subject() {
    }

    // Constructor for SubjectAddActivity
    public Subject(String maHp, String tenHp, String phongHoc, String loaiHp, String ngayBatDau, String ngayKetThuc, int soTuanHoc, String gioBatDau, String gioKetThuc, String giangVien, int soTinChi, String ghiChu) {
        this.maHp = maHp;
        this.tenHp = tenHp;
        this.phongHoc = phongHoc;
        this.loaiHp = loaiHp;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.soTuanHoc = soTuanHoc;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.giangVien = giangVien;
        this.soTinChi = soTinChi;
        this.ghiChu = ghiChu;
    }


    // Getters and Setters
    public String getMaHp() {
        return maHp;
    }

    public String getTenHp() {
        return tenHp;
    }
}
