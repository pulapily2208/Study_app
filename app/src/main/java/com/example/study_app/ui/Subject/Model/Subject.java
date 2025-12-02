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

}
