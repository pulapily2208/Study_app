package com.example.study_app.ui.Score.Models;

public class Score {
    private int id;
    private String maHp;
    private float chuyenCan;
    private float giuaKi;
    private float cuoiKi;
    private float gpa;

    public Score(int id, String maHp, float chuyenCan, float giuaKi, float cuoiKi, float gpa) {
        this.id = id;
        this.maHp = maHp;
        this.chuyenCan = chuyenCan;
        this.giuaKi = giuaKi;
        this.cuoiKi = cuoiKi;
        this.gpa = gpa;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMaHp() {
        return maHp;
    }

    public void setMaHp(String maHp) {
        this.maHp = maHp;
    }

    public float getChuyenCan() {
        return chuyenCan;
    }

    public void setChuyenCan(float chuyenCan) {
        this.chuyenCan = chuyenCan;
    }

    public float getGiuaKi() {
        return giuaKi;
    }

    public void setGiuaKi(float giuaKi) {
        this.giuaKi = giuaKi;
    }

    public float getGpa() {
        return gpa;
    }

    public void setGpa(float gpa) {
        this.gpa = gpa;
    }

    public float getCuoiKi() {
        return cuoiKi;
    }

    public void setCuoiKi(float cuoiKi) {
        this.cuoiKi = cuoiKi;
    }
}
