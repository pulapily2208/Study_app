package com.example.study_app.Models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Deadline implements Serializable {
    private String tieuDe,noiDung;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private boolean completed;

    public Deadline(String tieuDe,String noiDung, Date ngayBatDau, Date ngayKetThuc) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;

        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
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
