package com.example.study_app.ui.Deadline.Models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Deadline implements Serializable {

    // Constants for repeat types
    public static final String REPEAT_TYPE_NONE = "Sự kiện một lần";
    public static final String REPEAT_TYPE_DAILY = "Hàng ngày";
    public static final String REPEAT_TYPE_WEEKLY = "Hàng tuần";
    public static final String REPEAT_TYPE_WEEKDAYS = "Hàng ngày trong tuần";

    private int id;
    private String tieuDe, noiDung;
    private String maHp;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private boolean completed;
    private int icon;
    private String reminder;
    private String repeat;
    private String note;
    private int weekIndex;
    private String tenMon;
    private long duration; // Kept for compatibility

    public Deadline() {}

    public Deadline(String tieuDe, String noiDung, Date ngayBatDau, Date ngayKetThuc, int icon) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.icon = icon;
        this.repeat = REPEAT_TYPE_NONE;
    }

    // ---------- GETTER & SETTER ----------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReminder() {
        return reminder;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public String getRepeat() {
        return repeat;
    }

    public int getWeekIndex() { return weekIndex; }
    public void setWeekIndex(int weekIndex) { this.weekIndex = weekIndex; }

    public String getMaHp() { return maHp; }
    public void setMaHp(String maHp) { this.maHp = maHp; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public Date getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(Date ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public Date getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(Date ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getIcon() { return icon; }
    public void setIcon(int icon) { this.icon = icon; }

    public String getReminderText() { return reminder != null ? reminder : "Không nhắc nhở"; }
    public void setReminder(String reminder) { this.reminder = reminder; }

    public String getRepeatText() { return repeat != null ? repeat : REPEAT_TYPE_NONE; }
    public void setRepeat(String repeat) { this.repeat = repeat; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getNgayText() {
        if (ngayBatDau == null || ngayKetThuc == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale.getDefault());
        return sdf.format(ngayBatDau) + " - " + sdf.format(ngayKetThuc);
    }

    public String getConLai() {
        if (ngayKetThuc == null) return "Không có ngày hết hạn";

        long diff = ngayKetThuc.getTime() - new Date().getTime();

        if (diff <= 0) {
            return "Đã hết hạn";
        }

        long days = TimeUnit.MILLISECONDS.toDays(diff);
        if (days > 0) {
            return "Còn " + days + " ngày";
        }

        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if (hours > 0) {
            return "Còn " + hours + " giờ";
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if (minutes < 1) {
            return "Sắp tới hạn";
        }
        return "Còn " + minutes + " phút";
    }
}
