package com.example.study_app.ui.Deadline.Models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Deadline implements Serializable {
    private int maDl; // Added field for the deadline ID
    private String tieuDe,noiDung;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private boolean completed;
    private int icon;

    // No-argument constructor required for instantiation from database
    public Deadline() {}

    public Deadline(String tieuDe,String noiDung, Date ngayBatDau, Date ngayKetThuc) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;

        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    // Getter and Setter for maDl
    public int getMaDl() {
        return maDl;
    }

    public void setMaDl(int maDl) {
        this.maDl = maDl;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getTieuDe() { return tieuDe; }
    public Date getNgayBatDau() { return ngayBatDau; }
    public Date getNgayKetThuc() { return ngayKetThuc; }

    public void setTieuDe(String tieuDe) {
        this.tieuDe = tieuDe;
    }

    public String getNoiDung() {
        return noiDung;
    }

    public void setNoiDung(String noiDung) {
        this.noiDung = noiDung;
    }

    public void setNgayBatDau(Date ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public void setNgayKetThuc(Date ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getConLai() {
        long diff = ngayKetThuc.getTime() - new Date().getTime();
        if (diff <= 0) return "Hết hạn";

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days >= 1) return "Còn " + days + " ngày";

        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        return "Còn " + hours + " giờ";
    }

    public String getNgayText() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
        return sdf.format(ngayBatDau) + " - " + sdf.format(ngayKetThuc);
    }


}
