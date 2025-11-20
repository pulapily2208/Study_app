package com.example.study_app.ui.Curriculum.Model;

public class Curriculum {
    private String maHp;
    private String tenHp;
    private int soTinChi;
    private int soTietLyThuyet;
    private int soTietThucHanh;
    private String nhomTuChon;
    private int hocKy;
    private String loaiHp;
    private int khoaId;

    // Constructors, Getters, and Setters

    public Curriculum() {
    }

    public Curriculum(String maHp, String tenHp, int soTinChi, int soTietLyThuyet, int soTietThucHanh, String nhomTuChon, int hocKy, String loaiHp, int khoaId) {
        this.maHp = maHp;
        this.tenHp = tenHp;
        this.soTinChi = soTinChi;
        this.soTietLyThuyet = soTietLyThuyet;
        this.soTietThucHanh = soTietThucHanh;
        this.nhomTuChon = nhomTuChon;
        this.hocKy = hocKy;
        this.loaiHp = loaiHp;
        this.khoaId = khoaId;
    }

    public String getMaHp() {
        return maHp;
    }

    public void setMaHp(String maHp) {
        this.maHp = maHp;
    }

    public String getTenHp() {
        return tenHp;
    }

    public void setTenHp(String tenHp) {
        this.tenHp = tenHp;
    }

    public int getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(int soTinChi) {
        this.soTinChi = soTinChi;
    }

    public int getSoTietLyThuyet() {
        return soTietLyThuyet;
    }

    public void setSoTietLyThuyet(int soTietLyThuyet) {
        this.soTietLyThuyet = soTietLyThuyet;
    }

    public int getSoTietThucHanh() {
        return soTietThucHanh;
    }

    public void setSoTietThucHanh(int soTietThucHanh) {
        this.soTietThucHanh = soTietThucHanh;
    }

    public String getNhomTuChon() {
        return nhomTuChon;
    }

    public void setNhomTuChon(String nhomTuChon) {
        this.nhomTuChon = nhomTuChon;
    }

    public int getHocKy() {
        return hocKy;
    }

    public void setHocKy(int hocKy) {
        this.hocKy = hocKy;
    }

    public String getLoaiHp() {
        return loaiHp;
    }

    public void setLoaiHp(String loaiHp) {
        this.loaiHp = loaiHp;
    }

    public int getKhoaId() {
        return khoaId;
    }

    public void setKhoaId(int khoaId) {
        this.khoaId = khoaId;
    }
}
