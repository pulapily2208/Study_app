package com.example.study_app.Models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Deadline implements Serializable {
    private String tieuDe;
    private Date ngayBatDau;
    private Date ngayKetThuc;

    public Deadline(String tieuDe, Date ngayBatDau, Date ngayKetThuc) {
        this.tieuDe = tieuDe;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getTieuDe() { return tieuDe; }
    public Date getNgayBatDau() { return ngayBatDau; }
    public Date getNgayKetThuc() { return ngayKetThuc; }

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
