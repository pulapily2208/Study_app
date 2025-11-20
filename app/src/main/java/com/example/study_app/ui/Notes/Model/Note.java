package com.example.study_app.ui.Notes.Model;

public class Note {
    private int id;
    private int user_id;
    private String ma_hp;
    private String title;
    private String body;
    private int pinned;
    private String color_tag;
    private String created_at;
    private String updated_at;
    private String imagePath;

    public Note() {
        this.id = id;
        this.user_id = user_id;
        this.title = title;
        this.ma_hp = ma_hp;
        this.body = body;
        this.pinned = pinned;
        this.color_tag = color_tag;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.imagePath = imagePath;

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getMa_hp() {
        return ma_hp;
    }

    public void setMa_hp(String ma_hp) {
        this.ma_hp = ma_hp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getPinned() {
        return pinned;
    }

    public void setPinned(int pinned) {
        this.pinned = pinned;
    }

    public String getColor_tag() {
        return color_tag;
    }

    public void setColor_tag(String color_tag) {
        this.color_tag = color_tag;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
