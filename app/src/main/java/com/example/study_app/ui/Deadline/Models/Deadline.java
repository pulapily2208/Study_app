package com.example.study_app.ui.Deadline.Models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Deadline implements Serializable {
    private String tieuDe, noiDung;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private boolean completed;
    private int icon;
    private String reminder; // ví dụ: "Trước 5 phút"
    private String repeat;   // ví dụ: "Một lần", "Hằng tuần"
    private boolean isPinned = false;

    public Deadline(String tieuDe, String noiDung, Date ngayBatDau, Date ngayKetThuc, int icon) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.icon = icon;
    }

    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }

    public String getReminderText() {
        return reminder != null ? reminder : "Không có";
    }

    public void setReminder(String reminder) {
        this.reminder = reminder;
    }

    public String getRepeatText() {
        return repeat != null ? repeat : "Sự kiện một lần";
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public int getIcon() { return icon; }
    public void setIcon(int icon) { this.icon = icon; }

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
