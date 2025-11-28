package com.example.study_app.ui.Deadline.Models;

import java.io.Serializable;

public class Icon implements Serializable {
    private int id_icon;
    private String name_icon;
    private String src_icon; // Corresponds to `icon_path` in DB

    // Sửa lỗi: Thêm public để có thể truy cập từ package khác
    public Icon() {}

    public int getId_icon() {
        return id_icon;
    }

    public void setId_icon(int id_icon) {
        this.id_icon = id_icon;
    }

    public String getName_icon() {
        return name_icon;
    }

    public void setName_icon(String name_icon) {
        this.name_icon = name_icon;
    }

    public String getSrc_icon() {
        return src_icon;
    }

    public void setSrc_icon(String src_icon) {
        this.src_icon = src_icon;
    }
}
