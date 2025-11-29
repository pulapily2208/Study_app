package com.example.study_app.ui.Notes.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class Note implements Parcelable {
    private int id;
    private int user_id;
    private String ma_hp;
    private String title;
    private String body;
    private int pinned;
    private String color_tag;
    private String created_at;
    private String updated_at;

    // NEW: danh sách nhiều ảnh
    private List<String> imagePaths = new ArrayList<>();
    private List<String> pdfPaths = new ArrayList<>();

    private List<String> audioPaths = new ArrayList<>();

    public Note() {}

    // Constructor đầy đủ
    public Note(int id, int user_id, String ma_hp, String title, String body, int pinned,
                String color_tag, String created_at, String updated_at, List<String> imagePaths, List<String> pdfPaths, List<String> audioPaths) {

        this.id = id;
        this.user_id = user_id;
        this.ma_hp = ma_hp;
        this.title = title;
        this.body = body;
        this.pinned = pinned;
        this.color_tag = color_tag;
        this.created_at = created_at;
        this.updated_at = updated_at;
        this.imagePaths = imagePaths;
        this.pdfPaths = pdfPaths;
        this.audioPaths = audioPaths;
    }

    // Constructor Parcel
    protected Note(Parcel in) {
        id = in.readInt();
        user_id = in.readInt();
        ma_hp = in.readString();
        title = in.readString();
        body = in.readString();
        pinned = in.readInt();
        color_tag = in.readString();
        created_at = in.readString();
        updated_at = in.readString();

        // NEW: đọc danh sách ảnh
        imagePaths = new ArrayList<>();
        in.readStringList(imagePaths);

        pdfPaths = new ArrayList<>();
        in.readStringList(pdfPaths);

        audioPaths = new ArrayList<>();
        in.readStringList(audioPaths);
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    // ===================== Getter / Setter ==========================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public String getMa_hp() { return ma_hp; }
    public void setMa_hp(String ma_hp) { this.ma_hp = ma_hp; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public int getPinned() { return pinned; }
    public void setPinned(int pinned) { this.pinned = pinned; }

    public String getColor_tag() { return color_tag; }
    public void setColor_tag(String color_tag) { this.color_tag = color_tag; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public String getUpdated_at() { return updated_at; }
    public void setUpdated_at(String updated_at) { this.updated_at = updated_at; }

    // NEW: nhiều ảnh
    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public List<String> getPdfPaths() {
        return pdfPaths;
    }

    public void setPdfPaths(List<String> pdfPaths) {
        this.pdfPaths = pdfPaths;
    }

    public List<String> getAudioPaths() {
        return audioPaths;
    }

    public void setAudioPaths(List<String> audioPaths) {
        this.audioPaths = audioPaths;
    }

    public void addImagePath(String imagePath) {
        if (this.imagePaths == null) this.imagePaths = new ArrayList<>();
        this.imagePaths.add(imagePath);
    }

    public void addPdfPath(String pdfPath) {
        this.pdfPaths.add(pdfPath); }

    public void addAudioPath(String audioPath) {
        this.audioPaths.add(audioPath); }


    // ====================== Parcelable ==========================

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(user_id);
        dest.writeString(ma_hp);
        dest.writeString(title);
        dest.writeString(body);
        dest.writeInt(pinned);
        dest.writeString(color_tag);
        dest.writeString(created_at);
        dest.writeString(updated_at);

        // NEW: ghi danh sách ảnh
        dest.writeStringList(imagePaths);
        dest.writeStringList(pdfPaths);
        dest.writeStringList(audioPaths);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // ====================== Khác ===============================

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", pinned=" + pinned +
                '}';
    }

    public void setTimestamp() {
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        if (this.created_at == null || this.created_at.isEmpty()) {
            this.created_at = currentTime;
        }
        this.updated_at = currentTime;
    }
}
