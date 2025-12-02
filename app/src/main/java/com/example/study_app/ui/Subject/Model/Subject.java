package com.example.study_app.ui.Subject.Model;

import com.example.study_app.ui.Deadline.Models.Deadline;

import java.util.ArrayList;
import java.util.Date;

public class Subject {
    public ArrayList<Deadline> deadline=new ArrayList<>();

    public String maHp;
    public String tenHp;
    public String tenGv;
    public int soTc;
    public String ghiChu;
    public String phongHoc;
    public Date ngayBatDau;
    public Date ngayKetThuc;
    public Date gioBatDau;
    public Date gioKetThuc;
    public String loaiMon;
    public String mauSac;
    public String tenHk;
    public int soTuan;


    // Default constructor
    public Subject() {
    }

    public ArrayList<Deadline> getDeadline() {
        return deadline;
    }

    public void setDeadline(ArrayList<Deadline> deadline) {
        this.deadline = deadline;
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

    public String getTenGv() {
        return tenGv;
    }

    public void setTenGv(String tenGv) {
        this.tenGv = tenGv;
    }

    public int getSoTc() {
        return soTc;
    }

    public void setSoTc(int soTc) {
        this.soTc = soTc;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public String getPhongHoc() {
        return phongHoc;
    }

    public void setPhongHoc(String phongHoc) {
        this.phongHoc = phongHoc;
    }

    public Date getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public Date getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(Date ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public Date getGioBatDau() {
        return gioBatDau;
    }

    public void setGioBatDau(Date gioBatDau) {
        this.gioBatDau = gioBatDau;
    }

    public Date getGioKetThuc() {
        return gioKetThuc;
    }

    public void setGioKetThuc(Date gioKetThuc) {
        this.gioKetThuc = gioKetThuc;
    }

    public String getLoaiMon() {
        return loaiMon;
    }

    public void setLoaiMon(String loaiMon) {
        this.loaiMon = loaiMon;
    }

    public String getMauSac() {
        return mauSac;
    }

    public void setMauSac(String mauSac) {
        this.mauSac = mauSac;
    }

    public String getTenHk() {
        return tenHk;
    }

    public void setTenHk(String tenHk) {
        this.tenHk = tenHk;
    }

    public int getSoTuan() {
        return soTuan;
    }

    public void setSoTuan(int soTuan) {
        this.soTuan = soTuan;
    }
}
