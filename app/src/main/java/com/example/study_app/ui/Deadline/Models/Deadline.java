package com.example.study_app.ui.Deadline.Models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Deadline implements Serializable {

    private int id; // ID duy nhất
    private String tieuDe, noiDung;
    private String maHp;
    private Date ngayBatDau;
    private Date ngayKetThuc;
    private boolean completed;
    private int icon;
    private String reminder;
    private String repeat;
    private boolean isPinned = false;
    private long duration;
    private String note;
    private int weekIndex;

    public Deadline() {}

    public Deadline(String tieuDe, String noiDung, Date ngayBatDau, Date ngayKetThuc, int icon) {
        this.tieuDe = tieuDe;
        this.noiDung = noiDung;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.icon = icon;
    }

    public Deadline(int id, String tieuDe, String moTa, Date ngayBatDau, Date ngayKetThuc,
                    boolean completed, String repeatText, String reminderText,
                    long duration, int icon, String note, int weekIndex, String maHp) {

        this.id = id;
        this.tieuDe = tieuDe;
        this.noiDung = moTa;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.completed = completed;
        this.repeat = repeatText;
        this.reminder = reminderText;
        this.duration = duration;
        this.icon = icon;
        this.note = note;
        this.weekIndex = weekIndex;
        this.maHp = maHp;
    }

    // ---------- GETTER & SETTER ----------

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

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

    public String getReminderText() { return reminder != null ? reminder : "Không có"; }
    public void setReminder(String reminder) { this.reminder = reminder; }

    public String getRepeatText() { return repeat != null ? repeat : "Sự kiện một lần"; }
    public void setRepeat(String repeat) { this.repeat = repeat; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
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
